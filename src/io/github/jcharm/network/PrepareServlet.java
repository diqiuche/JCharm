/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

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
 * 服务内置Servlet, 继承了Servlet抽象类.
 *
 * @param <K> SessionID的类型
 * @param <C> Context的子类型
 * @param <R> Request的子类型
 * @param <N> Response的子类型
 * @param <S> Servlet的子类型
 */
public abstract class PrepareServlet<K extends Serializable, C extends Context, R extends Request<C>, N extends Response<C, R>, S extends Servlet<C, R, N>> extends Servlet<C, R, N> {

	/** 执行请求次数. */
	protected final AtomicLong executeCounter = new AtomicLong();

	/** 错误请求次数. */
	protected final AtomicLong errorCounter = new AtomicLong();

	/** Servlet的集合. */
	protected final Set<S> servlets = new HashSet();

	/** Servlet映射集. */
	protected final Map<K, S> mappings = new HashMap();

	/**
	 * 添加Servlet.
	 *
	 * @param servlet Servlet的子类型
	 * @param attachment IO操作
	 * @param configValue ConfigValue
	 * @param mappings SessionID的类型
	 */
	public abstract void addServlet(S servlet, Object attachment, ConfigValue configValue, K... mappings);

	/**
	 * 获取指定Servlet的ConfigValue.
	 *
	 * @param servlet Servlet
	 * @return ConfigValue
	 */
	protected ConfigValue getServletConf(final Servlet servlet) {
		return servlet.configValue;
	}

	/**
	 * 设置指定Servlet的ConfigValue.
	 *
	 * @param servlet Servlet
	 * @param configValue ConfigValue
	 */
	protected void setServletConf(final Servlet servlet, final ConfigValue configValue) {
		servlet.configValue = configValue;
	}

	/**
	 * 准备Servlet.
	 *
	 * @param byteBuffer ByteBuffer
	 * @param request Request的子类型
	 * @param response Response的子类型
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void prepare(final ByteBuffer byteBuffer, final R request, final N response) throws IOException {
		this.executeCounter.incrementAndGet();
		final int rs = request.readHeader(byteBuffer);
		if (rs < 0) {
			response.context.offerBuffer(byteBuffer);
			if (rs != Integer.MIN_VALUE) {
				this.errorCounter.incrementAndGet();
			}
			response.finish(true);
		} else if (rs == 0) {
			response.context.offerBuffer(byteBuffer);
			request.prepare();
			this.execute(request, response);
		} else {
			byteBuffer.clear();
			final AtomicInteger ai = new AtomicInteger(rs);
			request.asyncConnection.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {

				@Override
				public void completed(final Integer result, final ByteBuffer attachment) {
					byteBuffer.flip();
					ai.addAndGet(-request.readBody(byteBuffer));
					if (ai.get() > 0) {
						byteBuffer.clear();
						request.asyncConnection.read(byteBuffer, byteBuffer, this);
					} else {
						response.context.offerBuffer(byteBuffer);
						request.prepare();
						try {
							PrepareServlet.this.execute(request, response);
						} catch (final Exception e) {
							PrepareServlet.this.errorCounter.incrementAndGet();
							response.finish(true);
							request.context.logger.log(Level.WARNING, "prepare servlet abort, forece to close channel ", e);
						}
					}
				}

				@Override
				public void failed(final Throwable exc, final ByteBuffer attachment) {
					PrepareServlet.this.errorCounter.incrementAndGet();
					response.context.offerBuffer(byteBuffer);
					response.finish(true);
					if (exc != null) {
						request.context.logger.log(Level.FINE, "Servlet read channel erroneous, forece to close channel ", exc);
					}
				}
			});
		}
	}

}
