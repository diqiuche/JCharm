/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;

import io.github.jcharm.common.ObjectPool;

/**
 * 通过线程执行PrepareServlet.
 */
public final class PrepareRunner implements Runnable {

	private final AsyncConnection asyncConnection;

	private final Context context;

	private final ByteBuffer byteBuffer;

	/**
	 * 构造函数.
	 *
	 * @param context Context
	 * @param asyncConnection AsyncConnection
	 * @param byteBuffer ByteBuffer
	 */
	public PrepareRunner(final Context context, final AsyncConnection asyncConnection, final ByteBuffer byteBuffer) {
		this.context = context;
		this.asyncConnection = asyncConnection;
		this.byteBuffer = byteBuffer;
	}

	@Override
	public void run() {
		final PrepareServlet prepareServlet = this.context.prepareServlet;
		final ObjectPool<? extends Response> responsePool = this.context.responsePool;
		if (this.byteBuffer != null) {
			final Response response = responsePool.get();
			response.init(this.asyncConnection);
			try {
				prepareServlet.prepare(this.byteBuffer, response.request, response);
			} catch (final Throwable t) {
				this.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", t);
				response.finish(true);
			}
			return;
		}
		final ByteBuffer byteBuffer = this.context.pollBuffer();
		try {
			this.asyncConnection.read(byteBuffer, null, new CompletionHandler<Integer, Void>() {

				@Override
				public void completed(final Integer count, final Void attachment1) {
					if ((count < 1) && (byteBuffer.remaining() == byteBuffer.limit())) {
						try {
							PrepareRunner.this.context.offerBuffer(byteBuffer);
							PrepareRunner.this.asyncConnection.close();
						} catch (final Exception e) {
							PrepareRunner.this.context.logger.log(Level.FINE, "PrepareRunner close channel erroneous on no read bytes", e);
						}
						return;
					}

					byteBuffer.flip();
					final Response response = responsePool.get();
					response.init(PrepareRunner.this.asyncConnection);
					try {
						prepareServlet.prepare(byteBuffer, response.request, response);
					} catch (final Throwable t) {
						PrepareRunner.this.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", t);
						response.finish(true);
					}
				}

				@Override
				public void failed(final Throwable exc, final Void attachment2) {
					PrepareRunner.this.context.offerBuffer(byteBuffer);
					try {
						PrepareRunner.this.asyncConnection.close();
					} catch (final Exception e) {
					}
					if (exc != null) {
						PrepareRunner.this.context.logger.log(Level.FINE, "Servlet Handler read channel erroneous, forece to close channel ", exc);
					}
				}
			});
		} catch (final Exception te) {
			this.context.offerBuffer(byteBuffer);
			try {
				this.asyncConnection.close();
			} catch (final Exception e) {
			}
			if (te != null) {
				this.context.logger.log(Level.FINE, "Servlet read channel erroneous, forece to close channel ", te);
			}
		}
	}

}
