/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

/**
 * Response抽象类.
 *
 * @param <C> Context子类型
 * @param <R> Request子类型
 */
public abstract class Response<C extends Context, R extends Request<C>> {

	/** 上下文对象. */
	protected C context;

	/** Request对象. */
	protected R request;

	/** 异步连接. */
	protected AsyncConnection asyncConnection;

	private boolean inited = true;

	/** 重置监听. */
	protected BiConsumer<R, Response<C, R>> recycleListener;

	private final CompletionHandler finishHandler = new CompletionHandler<Integer, ByteBuffer>() {

		@Override
		public void completed(final Integer result, final ByteBuffer attachment) {
			if (attachment.hasRemaining()) {
				Response.this.asyncConnection.write(attachment, attachment, this);
			} else {
				Response.this.context.offerBuffer(attachment);
				Response.this.finish();
			}
		}

		@Override
		public void failed(final Throwable exc, final ByteBuffer attachment) {
			Response.this.context.offerBuffer(attachment);
			Response.this.finish(true);
		}
	};

	private final CompletionHandler finishHandlerTwo = new CompletionHandler<Integer, ByteBuffer[]>() {

		@Override
		public void completed(final Integer result, final ByteBuffer[] attachments) {
			int index = -1;
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i].hasRemaining()) {
					index = i;
					break;
				} else {
					Response.this.context.offerBuffer(attachments[i]);
				}
			}
			if (index == 0) {
				Response.this.asyncConnection.write(attachments, attachments, this);
			} else if (index > 0) {
				final ByteBuffer[] newattachs = new ByteBuffer[attachments.length - index];
				System.arraycopy(attachments, index, newattachs, 0, newattachs.length);
				Response.this.asyncConnection.write(newattachs, newattachs, this);
			} else {
				Response.this.finish();
			}
		}

		@Override
		public void failed(final Throwable exc, final ByteBuffer[] attachments) {
			for (final ByteBuffer attachment : attachments) {
				Response.this.context.offerBuffer(attachment);
			}
			Response.this.finish(true);
		}

	};

	/**
	 * 构造函数.
	 *
	 * @param context Context子类型
	 * @param request Request子类型
	 */
	protected Response(final C context, final R request) {
		this.context = context;
		this.request = request;
	}

	/**
	 * 移除AsyncConnection.
	 *
	 * @return AsyncConnection
	 */
	protected AsyncConnection removeAsyncConnection() {
		final AsyncConnection ac = this.asyncConnection;
		this.asyncConnection = null;
		this.request.asyncConnection = null;
		return ac;
	}

	/**
	 * 准备Response.
	 */
	protected void prepare() {
		this.inited = true;
	}

	/**
	 * 重置Response.
	 *
	 * @return boolean
	 */
	protected boolean recycle() {
		if (!this.inited) {
			return false;
		}
		final boolean keepAlive = this.request.keepAlive;
		if (this.recycleListener != null) {
			try {
				this.recycleListener.accept(this.request, this);
			} catch (final Exception e) {
				System.err.println(this.request);
				e.printStackTrace();
			}
			this.recycleListener = null;
		}
		this.request.recycle();
		if (this.asyncConnection != null) {
			if (keepAlive) {
				this.context.submit(new PrepareRunner(this.context, this.asyncConnection, null));
			} else {
				try {
					if (this.asyncConnection.isOpen()) {
						this.asyncConnection.close();
					}
				} catch (final Exception e) {
				}
			}
			this.asyncConnection = null;
		}
		this.inited = false;
		return true;
	}

	/**
	 * 废止保持的连接.
	 */
	protected void refuseAlive() {
		this.request.keepAlive = false;
	}

	/**
	 * 初始化方法.
	 *
	 * @param asyncConnection AsyncConnection
	 */
	protected void init(final AsyncConnection asyncConnection) {
		this.asyncConnection = asyncConnection;
		this.request.asyncConnection = asyncConnection;
		this.request.createtime = LocalDateTime.now();
	}

	/**
	 * 设置重置监听.
	 *
	 * @param recycleListener BiConsumer
	 */
	public void setRecycleListener(final BiConsumer<R, Response<C, R>> recycleListener) {
		this.recycleListener = recycleListener;
	}

	/**
	 * 获取Context上下文.
	 *
	 * @return C
	 */
	public C getContext() {
		return this.context;
	}

	/**
	 * 关闭连接, 如果是keep-alive则不强制关闭.
	 */
	public void finish() {
		this.finish(false);
	}

	/**
	 * 强制关闭连接.
	 *
	 * @param kill boolean
	 */
	public void finish(final boolean kill) {
		if (kill) {
			this.refuseAlive();
		}
		this.context.responsePool.offer(this);
	}

	/**
	 * 将指定ByteBuffer按响应结果输出.
	 *
	 * @param byteBuffer ByteBuffer
	 */
	public void finish(final ByteBuffer byteBuffer) {
		this.asyncConnection.write(byteBuffer, byteBuffer, this.finishHandler);
	}

	/**
	 * 将指定ByteBuffer按响应结果输出, 输出后是否强制关闭连接.
	 *
	 * @param kill boolean
	 * @param byteBuffer ByteBuffer
	 */
	public void finish(final boolean kill, final ByteBuffer byteBuffer) {
		if (kill) {
			this.refuseAlive();
		}
		this.asyncConnection.write(byteBuffer, byteBuffer, this.finishHandler);
	}

	/**
	 * 将多个ByteBuffer按响应结果输出.
	 *
	 * @param byteBuffers ByteBuffer
	 */
	public void finish(final ByteBuffer... byteBuffers) {
		this.asyncConnection.write(byteBuffers, byteBuffers, this.finishHandlerTwo);
	}

	/**
	 * 将多个ByteBuffer按响应结果输出, 输出后是否强制关闭连接.
	 *
	 * @param kill boolean
	 * @param byteBuffers ByteBuffer
	 */
	public void finish(final boolean kill, final ByteBuffer... byteBuffers) {
		if (kill) {
			this.refuseAlive();
		}
		this.asyncConnection.write(byteBuffers, byteBuffers, this.finishHandlerTwo);
	}

	/**
	 * 发送数据.
	 *
	 * @param <A> IO操作对象的类型
	 * @param byteBuffer ByteBuffer
	 * @param attachment IO操作
	 * @param handler CompletionHandler
	 */
	protected <A> void send(final ByteBuffer byteBuffer, final A attachment, final CompletionHandler<Integer, A> handler) {
		this.asyncConnection.write(byteBuffer, attachment, new CompletionHandler<Integer, A>() {

			@Override
			public void completed(final Integer result, final A attachment) {
				if (byteBuffer.hasRemaining()) {
					Response.this.asyncConnection.write(byteBuffer, attachment, this);
				} else {
					Response.this.context.offerBuffer(byteBuffer);
					if (handler != null) {
						handler.completed(result, attachment);
					}
				}
			}

			@Override
			public void failed(final Throwable exc, final A attachment) {
				Response.this.context.offerBuffer(byteBuffer);
				if (handler != null) {
					handler.failed(exc, attachment);
				}
			}

		});
	}

	/**
	 * 发送数据.
	 *
	 * @param <A> IO操作对象的类型
	 * @param byteBuffers ByteBuffer[]
	 * @param attachment IO操作
	 * @param handler CompletionHandler
	 */
	protected <A> void send(final ByteBuffer[] byteBuffers, final A attachment, final CompletionHandler<Integer, A> handler) {
		this.asyncConnection.write(byteBuffers, attachment, new CompletionHandler<Integer, A>() {

			@Override
			public void completed(final Integer result, final A attachment) {
				int index = -1;
				for (int i = 0; i < byteBuffers.length; i++) {
					if (byteBuffers[i].hasRemaining()) {
						index = i;
						break;
					}
					Response.this.context.offerBuffer(byteBuffers[i]);
				}
				if (index == 0) {
					Response.this.asyncConnection.write(byteBuffers, attachment, this);
				} else if (index > 0) {
					final ByteBuffer[] newattachs = new ByteBuffer[byteBuffers.length - index];
					System.arraycopy(byteBuffers, index, newattachs, 0, newattachs.length);
					Response.this.asyncConnection.write(newattachs, attachment, this);
				} else if (handler != null) {
					handler.completed(result, attachment);
				}
			}

			@Override
			public void failed(final Throwable exc, final A attachment) {
				for (final ByteBuffer buffer : byteBuffers) {
					Response.this.context.offerBuffer(buffer);
				}
				if (handler != null) {
					handler.failed(exc, attachment);
				}
			}

		});
	}

}
