/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步连接.
 */
public abstract class AsyncConnection implements AsynchronousByteChannel, AutoCloseable {// AsynchronousByteChannel : 异步通道, 可以读取和写入字节

	/** 用于存储绑定在Connection上的对象集合. */
	protected Map<String, Object> attributes;

	/** 用于存储绑定在Connection上的对象, 同attributes, 只绑定单个对象时尽量使用subobject而非attributes. */
	protected Object subobject;

	/**
	 * Checks if is tcp.
	 *
	 * @return true, if is tcp
	 */
	public abstract boolean isTCP();

	/**
	 * Gets the remote address.
	 *
	 * @return the remote address
	 */
	public abstract SocketAddress getRemoteAddress();

	/**
	 * Gets the local address.
	 *
	 * @return the local address
	 */
	public abstract SocketAddress getLocalAddress();

	/**
	 * Gets the read timeout second.
	 *
	 * @return the read timeout second
	 */
	public abstract int getReadTimeoutSecond();

	/**
	 * Gets the write timeout second.
	 *
	 * @return the write timeout second
	 */
	public abstract int getWriteTimeoutSecond();

	/**
	 * Sets the read timeout second.
	 *
	 * @param readTimeoutSecond the new read timeout second
	 */
	public abstract void setReadTimeoutSecond(int readTimeoutSecond);

	/**
	 * Sets the write timeout second.
	 *
	 * @param writeTimeoutSecond the new write timeout second
	 */
	public abstract void setWriteTimeoutSecond(int writeTimeoutSecond);

	/**
	 * Write.
	 *
	 * @param <A> the generic type
	 * @param srcs the srcs
	 * @param attachment the attachment
	 * @param handler the handler
	 */
	public final <A> void write(final ByteBuffer[] srcs, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
		this.write(srcs, 0, srcs.length, attachment, handler);
	}

	/**
	 * Write.
	 *
	 * @param <A> the generic type
	 * @param srcs the srcs
	 * @param offset the offset
	 * @param length the length
	 * @param attachment the attachment
	 * @param handler the handler
	 */
	protected abstract <A> void write(ByteBuffer[] srcs, int offset, int length, A attachment, CompletionHandler<Integer, ? super A> handler);

	/**
	 * 同close, 只是去掉throws IOException.
	 */
	public void dispose() {
		try {
			this.close();
		} catch (final IOException io) {
		}
	}

	@Override
	public void close() throws IOException {
		if (this.attributes == null) {
			return;
		}
		try {
			for (final Object obj : this.attributes.values()) {
				if (obj instanceof AutoCloseable) {
					((AutoCloseable) obj).close();
				}
			}
		} catch (final Exception io) {
		}
	}

	/**
	 * Gets the subobject.
	 *
	 * @param <T> the generic type
	 * @return the subobject
	 */
	public final <T> T getSubobject() {
		return (T) this.subobject;
	}

	/**
	 * Sets the subobject.
	 *
	 * @param value the new subobject
	 */
	public void setSubobject(final Object value) {
		this.subobject = value;
	}

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(final String name, final Object value) {
		if (this.attributes == null) {
			this.attributes = new HashMap();
		}
		this.attributes.put(name, value);
	}

	/**
	 * Gets the attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the attribute
	 */
	public final <T> T getAttribute(final String name) {
		return (T) (this.attributes == null ? null : this.attributes.get(name));
	}

	/**
	 * Removes the attribute.
	 *
	 * @param name the name
	 */
	public final void removeAttribute(final String name) {
		if (this.attributes != null) {
			this.attributes.remove(name);
		}
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public final Map<String, Object> getAttributes() {
		return this.attributes;
	}

	/**
	 * Clear attribute.
	 */
	public final void clearAttribute() {
		if (this.attributes != null) {
			this.attributes.clear();
		}
	}

	/**
	 * Creates the.
	 *
	 * @param protocol the protocol
	 * @param group the group
	 * @param address the address
	 * @return the async connection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AsyncConnection create(final String protocol, final AsynchronousChannelGroup group, final SocketAddress address) throws IOException {
		return AsyncConnection.create(protocol, group, address, 0, 0);
	}

	/**
	 * Creates the.
	 *
	 * @param protocol the protocol
	 * @param group the group
	 * @param address the address
	 * @param readTimeoutSecond0 the read timeout second0
	 * @param writeTimeoutSecond0 the write timeout second0
	 * @return the async connection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AsyncConnection create(final String protocol, final AsynchronousChannelGroup group, final SocketAddress address, final int readTimeoutSecond0, final int writeTimeoutSecond0) throws IOException {
		if ("TCP".equalsIgnoreCase(protocol)) {
			final AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
			try {
				channel.connect(address).get(3, TimeUnit.SECONDS);
			} catch (final Exception e) {
				throw new IOException("AsyncConnection connect " + address, e);
			}
			return AsyncConnection.create(channel, address, readTimeoutSecond0, writeTimeoutSecond0);
		} else if ("UDP".equalsIgnoreCase(protocol)) {
			final DatagramChannel channel = DatagramChannel.open();
			channel.configureBlocking(true);
			channel.connect(address);
			return AsyncConnection.create(channel, address, true, readTimeoutSecond0, writeTimeoutSecond0);
		} else {
			throw new RuntimeException("AsyncConnection not support protocol " + protocol);
		}
	}

	/**
	 * Creates the.
	 *
	 * @param ch the ch
	 * @param addr the addr
	 * @param client0 the client0
	 * @param readTimeoutSecond0 the read timeout second0
	 * @param writeTimeoutSecond0 the write timeout second0
	 * @return the async connection
	 */
	public static AsyncConnection create(final DatagramChannel ch, final SocketAddress addr, final boolean client0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
		return new BIOUDPAsyncConnection(ch, addr, client0, readTimeoutSecond0, writeTimeoutSecond0);
	}

	/**
	 * Creates the.
	 *
	 * @param socket the socket
	 * @return the async connection
	 */
	public static AsyncConnection create(final Socket socket) {
		return AsyncConnection.create(socket, null, 0, 0);
	}

	/**
	 * Creates the.
	 *
	 * @param socket the socket
	 * @param addr0 the addr0
	 * @param readTimeoutSecond0 the read timeout second0
	 * @param writeTimeoutSecond0 the write timeout second0
	 * @return the async connection
	 */
	public static AsyncConnection create(final Socket socket, final SocketAddress addr0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
		return new BIOTCPAsyncConnection(socket, addr0, readTimeoutSecond0, writeTimeoutSecond0);
	}

	/**
	 * Creates the.
	 *
	 * @param ch the ch
	 * @return the async connection
	 */
	public static AsyncConnection create(final AsynchronousSocketChannel ch) {
		return AsyncConnection.create(ch, null, 0, 0);
	}

	/**
	 * Creates the.
	 *
	 * @param ch the ch
	 * @param addr0 the addr0
	 * @param readTimeoutSecond0 the read timeout second0
	 * @param writeTimeoutSecond0 the write timeout second0
	 * @return the async connection
	 */
	public static AsyncConnection create(final AsynchronousSocketChannel ch, final SocketAddress addr0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
		return new AIOTCPAsyncConnection(ch, addr0, readTimeoutSecond0, writeTimeoutSecond0);
	}

	private static class BIOUDPAsyncConnection extends AsyncConnection {

		private int readTimeoutSecond;

		private int writeTimeoutSecond;

		private final DatagramChannel channel;

		private final SocketAddress remoteAddress;

		private final boolean client;

		public BIOUDPAsyncConnection(final DatagramChannel ch, final SocketAddress addr, final boolean client0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
			this.channel = ch;
			this.client = client0;
			this.readTimeoutSecond = readTimeoutSecond0;
			this.writeTimeoutSecond = writeTimeoutSecond0;
			this.remoteAddress = addr;
		}

		@Override
		public <A> void read(final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				final int rs = this.channel.read(dst);
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

		@Override
		public Future<Integer> read(final ByteBuffer dst) {
			try {
				final int rs = this.channel.read(dst);
				return new SimpleFuture(rs);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public <A> void write(final ByteBuffer src, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				final int rs = this.channel.send(src, this.remoteAddress);
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

		@Override
		public Future<Integer> write(final ByteBuffer src) {
			try {
				final int rs = this.channel.send(src, this.remoteAddress);
				return new SimpleFuture(rs);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public final void close() throws IOException {
			super.close();
			if (this.client) {
				this.channel.close();
			}
		}

		@Override
		public boolean isOpen() {
			return this.channel.isOpen();
		}

		@Override
		public boolean isTCP() {
			return false;
		}

		@Override
		public SocketAddress getRemoteAddress() {
			return this.remoteAddress;
		}

		@Override
		public SocketAddress getLocalAddress() {
			try {
				return this.channel.getLocalAddress();
			} catch (final IOException e) {
				return null;
			}
		}

		@Override
		public int getReadTimeoutSecond() {
			return this.readTimeoutSecond;
		}

		@Override
		public int getWriteTimeoutSecond() {
			return this.writeTimeoutSecond;
		}

		@Override
		public void setReadTimeoutSecond(final int readTimeoutSecond) {
			this.readTimeoutSecond = readTimeoutSecond;
		}

		@Override
		public void setWriteTimeoutSecond(final int writeTimeoutSecond) {
			this.writeTimeoutSecond = writeTimeoutSecond;
		}

		@Override
		protected <A> void write(final ByteBuffer[] srcs, final int offset, final int length, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				int rs = 0;
				for (int i = offset; i < (offset + length); i++) {
					rs += this.channel.send(srcs[i], this.remoteAddress);
					if (i != offset) {
						Thread.sleep(10);
					}
				}
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final Exception e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

	}

	private static class SimpleFuture implements Future<Integer> {

		private final int rs;

		public SimpleFuture(final int rs) {
			this.rs = rs;
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return true;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public Integer get() throws InterruptedException, ExecutionException {
			return this.rs;
		}

		@Override
		public Integer get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return this.rs;
		}

	}

	private static class BIOTCPAsyncConnection extends AsyncConnection {

		private int readTimeoutSecond;

		private int writeTimeoutSecond;

		private final Socket socket;

		private final ReadableByteChannel readChannel;

		private final WritableByteChannel writeChannel;

		private final SocketAddress remoteAddress;

		public BIOTCPAsyncConnection(final Socket socket, final SocketAddress addr0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
			this.socket = socket;
			ReadableByteChannel rc = null;
			WritableByteChannel wc = null;
			try {
				socket.setSoTimeout(Math.max(readTimeoutSecond0, writeTimeoutSecond0));
				rc = Channels.newChannel(socket.getInputStream());
				wc = Channels.newChannel(socket.getOutputStream());
			} catch (final IOException e) {
				e.printStackTrace();
			}
			this.readChannel = rc;
			this.writeChannel = wc;
			this.readTimeoutSecond = readTimeoutSecond0;
			this.writeTimeoutSecond = writeTimeoutSecond0;
			SocketAddress addr = addr0;
			if (addr == null) {
				try {
					addr = socket.getRemoteSocketAddress();
				} catch (final Exception e) {// do nothing
				}
			}
			this.remoteAddress = addr;
		}

		@Override
		public <A> void read(final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				final int rs = this.readChannel.read(dst);
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

		@Override
		public Future<Integer> read(final ByteBuffer dst) {
			try {
				final int rs = this.readChannel.read(dst);
				return new SimpleFuture(rs);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public <A> void write(final ByteBuffer src, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				final int rs = this.writeChannel.write(src);
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

		@Override
		public Future<Integer> write(final ByteBuffer src) {
			try {
				final int rs = this.writeChannel.write(src);
				return new SimpleFuture(rs);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() throws IOException {
			super.close();
			this.socket.close();
		}

		@Override
		public boolean isOpen() {
			return !this.socket.isClosed();
		}

		@Override
		public boolean isTCP() {
			return true;
		}

		@Override
		public SocketAddress getRemoteAddress() {
			return this.remoteAddress;
		}

		@Override
		public SocketAddress getLocalAddress() {
			return this.socket.getLocalSocketAddress();
		}

		@Override
		public int getReadTimeoutSecond() {
			return this.readTimeoutSecond;
		}

		@Override
		public int getWriteTimeoutSecond() {
			return this.writeTimeoutSecond;
		}

		@Override
		public void setReadTimeoutSecond(final int readTimeoutSecond) {
			this.readTimeoutSecond = readTimeoutSecond;
		}

		@Override
		public void setWriteTimeoutSecond(final int writeTimeoutSecond) {
			this.writeTimeoutSecond = writeTimeoutSecond;
		}

		@Override
		protected <A> void write(final ByteBuffer[] srcs, final int offset, final int length, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				int rs = 0;
				for (int i = offset; i < (offset + length); i++) {
					rs += this.writeChannel.write(srcs[i]);
				}
				if (handler != null) {
					handler.completed(rs, attachment);
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment);
				}
			}
		}

	}

	private static class AIOTCPAsyncConnection extends AsyncConnection {

		private int readTimeoutSecond;

		private int writeTimeoutSecond;

		private final AsynchronousSocketChannel channel;

		private final SocketAddress remoteAddress;

		public AIOTCPAsyncConnection(final AsynchronousSocketChannel ch, final SocketAddress addr0, final int readTimeoutSecond0, final int writeTimeoutSecond0) {
			this.channel = ch;
			this.readTimeoutSecond = readTimeoutSecond0;
			this.writeTimeoutSecond = writeTimeoutSecond0;
			SocketAddress addr = addr0;
			if (addr == null) {
				try {
					addr = ch.getRemoteAddress();
				} catch (final Exception e) {// do nothing
				}
			}
			this.remoteAddress = addr;
		}

		@Override
		public <A> void read(final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			if (this.readTimeoutSecond > 0) {
				this.channel.read(dst, this.readTimeoutSecond, TimeUnit.SECONDS, attachment, handler);
			} else {
				this.channel.read(dst, attachment, handler);
			}
		}

		@Override
		public Future<Integer> read(final ByteBuffer dst) {
			return this.channel.read(dst);
		}

		@Override
		public <A> void write(final ByteBuffer src, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			if (this.writeTimeoutSecond > 0) {
				this.channel.write(src, this.writeTimeoutSecond, TimeUnit.SECONDS, attachment, handler);
			} else {
				this.channel.write(src, attachment, handler);
			}
		}

		@Override
		public Future<Integer> write(final ByteBuffer src) {
			return this.channel.write(src);
		}

		@Override
		public final void close() throws IOException {
			super.close();
			this.channel.close();
		}

		@Override
		public boolean isOpen() {
			return this.channel.isOpen();
		}

		@Override
		public boolean isTCP() {
			return true;
		}

		@Override
		public SocketAddress getRemoteAddress() {
			return this.remoteAddress;
		}

		@Override
		public SocketAddress getLocalAddress() {
			try {
				return this.channel.getLocalAddress();
			} catch (final IOException e) {
				return null;
			}
		}

		@Override
		public int getReadTimeoutSecond() {
			return this.readTimeoutSecond;
		}

		@Override
		public int getWriteTimeoutSecond() {
			return this.writeTimeoutSecond;
		}

		@Override
		public void setReadTimeoutSecond(final int readTimeoutSecond) {
			this.readTimeoutSecond = readTimeoutSecond;
		}

		@Override
		public void setWriteTimeoutSecond(final int writeTimeoutSecond) {
			this.writeTimeoutSecond = writeTimeoutSecond;
		}

		@Override
		protected <A> void write(final ByteBuffer[] srcs, final int offset, final int length, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			this.channel.write(srcs, offset, length, this.writeTimeoutSecond > 0 ? this.writeTimeoutSecond : 60, TimeUnit.SECONDS, attachment, new CompletionHandler<Long, A>() {

				@Override
				public void completed(final Long result, final A attachment) {
					handler.completed(result.intValue(), attachment);
				}

				@Override
				public void failed(final Throwable exc, final A attachment) {
					handler.failed(exc, attachment);
				}

			});
		}

	}

}
