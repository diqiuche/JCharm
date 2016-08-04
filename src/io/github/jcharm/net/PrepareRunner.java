/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;

import io.github.jcharm.common.ObjectPool;

/**
 * The Class PrepareRunner.
 */
public final class PrepareRunner implements Runnable {

	private final AsyncConnection channel;

	private final Context context;

	private final ByteBuffer data;

	/**
	 * Instantiates a new prepare runner.
	 *
	 * @param context the context
	 * @param channel the channel
	 * @param data the data
	 */
	public PrepareRunner(final Context context, final AsyncConnection channel, final ByteBuffer data) {
		this.context = context;
		this.channel = channel;
		this.data = data;
	}

	@Override
	public void run() {
		final PrepareServlet prepare = this.context.prepare;
		final ObjectPool<? extends Response> responsePool = this.context.responsePool;
		if (this.data != null) {
			final Response response = responsePool.get();
			response.init(this.channel);
			try {
				prepare.prepare(this.data, response.request, response);
			} catch (final Throwable t) {
				this.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", t);
				response.finish(true);
			}
			return;
		}
		final ByteBuffer buffer = this.context.pollBuffer();
		try {
			this.channel.read(buffer, null, new CompletionHandler<Integer, Void>() {

				@Override
				public void completed(final Integer count, final Void attachment1) {
					if ((count < 1) && (buffer.remaining() == buffer.limit())) {
						try {
							PrepareRunner.this.context.offerBuffer(buffer);
							PrepareRunner.this.channel.close();
						} catch (final Exception e) {
							PrepareRunner.this.context.logger.log(Level.FINE, "PrepareRunner close channel erroneous on no read bytes", e);
						}
						return;
					}
					buffer.flip();
					final Response response = responsePool.get();
					response.init(PrepareRunner.this.channel);
					try {
						prepare.prepare(buffer, response.request, response);
					} catch (final Throwable t) { // 此处不可 context.offerBuffer(buffer); 以免prepare.prepare内部异常导致重复 offerBuffer
						PrepareRunner.this.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", t);
						response.finish(true);
					}
				}

				@Override
				public void failed(final Throwable exc, final Void attachment2) {
					PrepareRunner.this.context.offerBuffer(buffer);
					try {
						PrepareRunner.this.channel.close();
					} catch (final Exception e) {
					}
					if (exc != null) {
						PrepareRunner.this.context.logger.log(Level.FINE, "Servlet Handler read channel erroneous, forece to close channel ", exc);
					}
				}
			});
		} catch (final Exception te) {
			this.context.offerBuffer(buffer);
			try {
				this.channel.close();
			} catch (final Exception e) {
			}
			if (te != null) {
				this.context.logger.log(Level.FINE, "Servlet read channel erroneous, forece to close channel ", te);
			}
		}
	}

}
