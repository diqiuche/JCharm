/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import io.github.jcharm.common.ConfigValue;

/**
 * The Class PrepareServlet.
 *
 * @param <K> the key type
 * @param <C> the generic type
 * @param <R> the generic type
 * @param
 * 			<P>
 *            the generic type
 * @param <S> the generic type
 */
public abstract class PrepareServlet<K extends Serializable, C extends Context, R extends Request<C>, P extends Response<C, R>, S extends Servlet<C, R, P>> extends Servlet<C, R, P> {

	protected final AtomicLong executeCounter = new AtomicLong(); // 执行请求次数

	protected final AtomicLong illRequestCounter = new AtomicLong(); // 错误请求次数

	protected final Set<S> servlets = new HashSet<>();

	protected final Map<K, S> mappings = new HashMap<>();

	public abstract void addServlet(S servlet, Object attachment, ConfigValue conf, K... mappings);

	public final void prepare(final ByteBuffer buffer, final R request, final P response) throws IOException {
		this.executeCounter.incrementAndGet();
		final int rs = request.readHeader(buffer);
		if (rs < 0) {
			response.context.offerBuffer(buffer);
			if (rs != Integer.MIN_VALUE) {
				this.illRequestCounter.incrementAndGet();
			}
			response.finish(true);
		} else if (rs == 0) {
			response.context.offerBuffer(buffer);
			request.prepare();
			this.execute(request, response);
		} else {
			buffer.clear();
			final AtomicInteger ai = new AtomicInteger(rs);
			request.channel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

				@Override
				public void completed(final Integer result, final ByteBuffer attachment) {
					buffer.flip();
					ai.addAndGet(-request.readBody(buffer));
					if (ai.get() > 0) {
						buffer.clear();
						request.channel.read(buffer, buffer, this);
					} else {
						response.context.offerBuffer(buffer);
						request.prepare();
						try {
							PrepareServlet.this.execute(request, response);
						} catch (final Exception e) {
							PrepareServlet.this.illRequestCounter.incrementAndGet();
							response.finish(true);
							request.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", e);
						}
					}
				}

				@Override
				public void failed(final Throwable exc, final ByteBuffer attachment) {
					PrepareServlet.this.illRequestCounter.incrementAndGet();
					response.context.offerBuffer(buffer);
					response.finish(true);
					if (exc != null) {
						request.context.logger.log(Level.FINE, "Servlet read channel erroneous, forece to close channel ", exc);
					}
				}
			});
		}
	}

	protected ConfigValue getServletConf(final Servlet servlet) {
		return servlet._conf;
	}

	protected void setServletConf(final Servlet servlet, final ConfigValue conf) {
		servlet._conf = conf;
	}

}
