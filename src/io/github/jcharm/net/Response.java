/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

/**
 * Response响应.
 *
 * @param <C> Context上下文
 * @param <R> Request请求
 */
public abstract class Response<C extends Context, R extends Request<C>> {

	/** The context. */
	protected final C context;

	/** The request. */
	protected final R request;

	/** The channel. */
	protected AsyncConnection channel;

	private boolean inited = true;

	/** The recycle listener. */
	protected BiConsumer<R, Response<C, R>> recycleListener;

	private final CompletionHandler finishHandler = new CompletionHandler<Integer, ByteBuffer>() {

		@Override
		public void completed(final Integer result, final ByteBuffer attachment) {
			if (attachment.hasRemaining()) {
				Response.this.channel.write(attachment, attachment, this);
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

	private final CompletionHandler finishHandler2 = new CompletionHandler<Integer, ByteBuffer[]>() {

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
				Response.this.channel.write(attachments, attachments, this);
			} else if (index > 0) {
				final ByteBuffer[] newattachs = new ByteBuffer[attachments.length - index];
				System.arraycopy(attachments, index, newattachs, 0, newattachs.length);
				Response.this.channel.write(newattachs, newattachs, this);
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
	 * Instantiates a new response.
	 *
	 * @param context the context
	 * @param request the request
	 */
	protected Response(final C context, final R request) {
		this.context = context;
		this.request = request;
	}

	/**
	 * Removes the channel.
	 *
	 * @return the async connection
	 */
	protected AsyncConnection removeChannel() {
		final AsyncConnection ch = this.channel;
		this.channel = null;
		this.request.channel = null;
		return ch;
	}

	/**
	 * Prepare.
	 */
	protected void prepare() {
		this.inited = true;
	}

	/**
	 * Recycle.
	 *
	 * @return true, if successful
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
		if (this.channel != null) {
			if (keepAlive) {
				this.context.submit(new PrepareRunner(this.context, this.channel, null));
			} else {
				try {
					if (this.channel.isOpen()) {
						this.channel.close();
					}
				} catch (final Exception e) {
				}
			}
			this.channel = null;
		}
		this.inited = false;
		return true;
	}

	/**
	 * Refuse alive.
	 */
	protected void refuseAlive() {
		this.request.keepAlive = false;
	}

	/**
	 * Inits the.
	 *
	 * @param channel the channel
	 */
	protected void init(final AsyncConnection channel) {
		this.channel = channel;
		this.request.channel = channel;
		this.request.createtime = System.currentTimeMillis();
	}

	/**
	 * Sets the recycle listener.
	 *
	 * @param recycleListener the recycle listener
	 */
	public void setRecycleListener(final BiConsumer<R, Response<C, R>> recycleListener) {
		this.recycleListener = recycleListener;
	}

	/**
	 * Finish.
	 */
	public void finish() {
		this.finish(false);
	}

	/**
	 * Finish.
	 *
	 * @param kill the kill
	 */
	public void finish(final boolean kill) {
		if (kill) {
			this.refuseAlive();
		}
		this.context.responsePool.offer(this);
	}

	/**
	 * Finish.
	 *
	 * @param buffer the buffer
	 */
	public void finish(final ByteBuffer buffer) {
		this.channel.write(buffer, buffer, this.finishHandler);
	}

	/**
	 * Finish.
	 *
	 * @param kill the kill
	 * @param buffer the buffer
	 */
	public void finish(final boolean kill, final ByteBuffer buffer) {
		if (kill) {
			this.refuseAlive();
		}
		this.channel.write(buffer, buffer, this.finishHandler);
	}

	/**
	 * Finish.
	 *
	 * @param buffers the buffers
	 */
	public void finish(final ByteBuffer... buffers) {
		this.channel.write(buffers, buffers, this.finishHandler2);
	}

	/**
	 * Finish.
	 *
	 * @param kill the kill
	 * @param buffers the buffers
	 */
	public void finish(final boolean kill, final ByteBuffer... buffers) {
		if (kill) {
			this.refuseAlive();
		}
		this.channel.write(buffers, buffers, this.finishHandler2);
	}

	/**
	 * Send.
	 *
	 * @param <A> the generic type
	 * @param buffer the buffer
	 * @param attachment the attachment
	 * @param handler the handler
	 */
	protected <A> void send(final ByteBuffer buffer, final A attachment, final CompletionHandler<Integer, A> handler) {
		this.channel.write(buffer, attachment, new CompletionHandler<Integer, A>() {

			@Override
			public void completed(final Integer result, final A attachment) {
				if (buffer.hasRemaining()) {
					Response.this.channel.write(buffer, attachment, this);
				} else {
					Response.this.context.offerBuffer(buffer);
					if (handler != null) {
						handler.completed(result, attachment);
					}
				}
			}

			@Override
			public void failed(final Throwable exc, final A attachment) {
				Response.this.context.offerBuffer(buffer);
				if (handler != null) {
					handler.failed(exc, attachment);
				}
			}

		});
	}

	/**
	 * Send.
	 *
	 * @param <A> the generic type
	 * @param buffers the buffers
	 * @param attachment the attachment
	 * @param handler the handler
	 */
	protected <A> void send(final ByteBuffer[] buffers, final A attachment, final CompletionHandler<Integer, A> handler) {
		this.channel.write(buffers, attachment, new CompletionHandler<Integer, A>() {

			@Override
			public void completed(final Integer result, final A attachment) {
				int index = -1;
				for (int i = 0; i < buffers.length; i++) {
					if (buffers[i].hasRemaining()) {
						index = i;
						break;
					}
					Response.this.context.offerBuffer(buffers[i]);
				}
				if (index == 0) {
					Response.this.channel.write(buffers, attachment, this);
				} else if (index > 0) {
					final ByteBuffer[] newattachs = new ByteBuffer[buffers.length - index];
					System.arraycopy(buffers, index, newattachs, 0, newattachs.length);
					Response.this.channel.write(newattachs, attachment, this);
				} else if (handler != null) {
					handler.completed(result, attachment);
				}
			}

			@Override
			public void failed(final Throwable exc, final A attachment) {
				for (final ByteBuffer buffer : buffers) {
					Response.this.context.offerBuffer(buffer);
				}
				if (handler != null) {
					handler.failed(exc, attachment);
				}
			}

		});
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public C getContext() {
		return this.context;
	}

}
