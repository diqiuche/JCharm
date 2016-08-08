/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

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
public abstract class AsyncConnection implements AutoCloseable, AsynchronousByteChannel {

	/** 用于存储绑定在Connection上的对象集合. */
	protected Map<String, Object> attributes;

	/** 用于存储绑定在Connection上的对象, 只绑定单个对象时尽量使用subObject而非attributes. */
	protected Object subObject;

	/**
	 * 判断是否使用TCP传输协议.
	 *
	 * @return boolean
	 */
	public abstract boolean isTCP();

	/**
	 * 获取远程的SocketAddress.
	 *
	 * @return SocketAddress
	 */
	public abstract SocketAddress getRemoteAddress();

	/**
	 * 获取本地的SocketAddress.
	 *
	 * @return SocketAddress
	 */
	public abstract SocketAddress getLocalAddress();

	/**
	 * 获取IO读取的超时秒数.
	 *
	 * @return int
	 */
	public abstract int getReadTimeoutSecond();

	/**
	 * 获取IO写入的超时秒数.
	 *
	 * @return int
	 */
	public abstract int getWriteTimeoutSecond();

	/**
	 * 设置IO读取的超时秒数.
	 *
	 * @param readTimeoutSecond int
	 */
	public abstract void setReadTimeoutSecond(int readTimeoutSecond);

	/**
	 * 设置IO写入的超时秒数.
	 *
	 * @param writeTimeoutSecond int
	 */
	public abstract void setWriteTimeoutSecond(int writeTimeoutSecond);

	/**
	 * 向异步网络通道写缓冲区中的数据.
	 *
	 * @param <A> IO操作对象的类型
	 * @param srcs ByteBuffer[]
	 * @param offset int
	 * @param length int
	 * @param attachment IO操作
	 * @param handler CompletionHandler
	 */
	protected abstract <A> void write(ByteBuffer[] srcs, int offset, int length, A attachment, CompletionHandler<Integer, ? super A> handler);

	/**
	 * 向异步网络通道写缓冲区中的数据.
	 *
	 * @param <A> IO操作对象的类型
	 * @param srcs ByteBuffer[]
	 * @param attachment IO操作
	 * @param handler CompletionHandler
	 */
	public final <A> void write(final ByteBuffer[] srcs, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
		this.write(srcs, 0, srcs.length, attachment, handler);
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
		} catch (final Exception e) {
		}
	}

	/**
	 * 同close, 只是去掉throws IOException.
	 */
	public void dispose() {
		try {
			this.close();
		} catch (final IOException e) {
		}
	}

	/**
	 * 获取绑定在Connection上的对象.
	 *
	 * @param <T> 泛型
	 * @return T
	 */
	public final <T> T getSubobject() {
		return (T) this.subObject;
	}

	/**
	 * 设置绑定在Connection上的对象.
	 *
	 * @param value Object
	 */
	public void setSubobject(final Object value) {
		this.subObject = value;
	}

	/**
	 * 设置绑定在Connection上的对象集合.
	 *
	 * @param name String
	 * @param value Object
	 */
	public void setAttribute(final String name, final Object value) {
		if (this.attributes == null) {
			this.attributes = new HashMap();
		}
		this.attributes.put(name, value);
	}

	/**
	 * 获取绑定在Connection上的对象.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @return T
	 */
	public final <T> T getAttribute(final String name) {
		return (T) (this.attributes == null ? null : this.attributes.get(name));
	}

	/**
	 * 获取绑定在Connection上的对象集合.
	 *
	 * @return Map
	 */
	public final Map<String, Object> getAttributes() {
		return this.attributes;
	}

	/**
	 * 移除绑定在Connection上的对象.
	 *
	 * @param name String
	 */
	public final void removeAttribute(final String name) {
		if (this.attributes != null) {
			this.attributes.remove(name);
		}
	}

	/**
	 * 清空绑定在Connection上的对象集合.
	 */
	public final void clearAttribute() {
		if (this.attributes != null) {
			this.attributes.clear();
		}
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param protocol 连接类型, TCP或UDP
	 * @param group 异步通道组, 共享一个java线程池
	 * @param address SocketAddress
	 * @param readTimeoutSecond int
	 * @param writeTimeoutSecond int
	 * @return AsyncConnection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AsyncConnection create(final String protocol, final AsynchronousChannelGroup group, final SocketAddress address, final int readTimeoutSecond, final int writeTimeoutSecond) throws IOException {
		if ("TCP".equalsIgnoreCase(protocol)) {
			final AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
			try {
				channel.connect(address).get(3, TimeUnit.SECONDS);
			} catch (final Exception e) {
				throw new IOException("AsyncConnection connect " + address, e);
			}
			return AsyncConnection.create(channel, address, readTimeoutSecond, writeTimeoutSecond);
		} else if ("UDP".equalsIgnoreCase(protocol)) {
			final DatagramChannel channel = DatagramChannel.open();
			channel.configureBlocking(true); // 阻塞模式
			channel.connect(address);
			return AsyncConnection.create(channel, address, true, readTimeoutSecond, writeTimeoutSecond);
		} else {
			throw new RuntimeException("AsyncConnection not support protocol " + protocol);
		}
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param protocol 连接类型, TCP或UDP
	 * @param group 异步通道组, 共享一个java线程池
	 * @param address SocketAddress
	 * @return AsyncConnection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AsyncConnection create(final String protocol, final AsynchronousChannelGroup group, final SocketAddress address) throws IOException {
		return AsyncConnection.create(protocol, group, address, 0, 0);
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param ch DatagramChannel
	 * @param addr SocketAddress
	 * @param client boolean
	 * @param readTimeoutSecond int
	 * @param writeTimeoutSecond int
	 * @return AsyncConnection
	 */
	public static AsyncConnection create(final DatagramChannel ch, final SocketAddress addr, final boolean client, final int readTimeoutSecond, final int writeTimeoutSecond) {
		return new BIOUDPAsyncConnection(ch, addr, client, readTimeoutSecond, writeTimeoutSecond);
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param socket Socket
	 * @param addr SocketAddress
	 * @param readTimeoutSecond int
	 * @param writeTimeoutSecond int
	 * @return AsyncConnection
	 */
	public static AsyncConnection create(final Socket socket, final SocketAddress addr, final int readTimeoutSecond, final int writeTimeoutSecond) {
		return new BIOTCPAsyncConnection(socket, addr, readTimeoutSecond, writeTimeoutSecond);
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param ch AsynchronousSocketChannel
	 * @return AsyncConnection
	 */
	public static AsyncConnection create(final AsynchronousSocketChannel ch) {
		return AsyncConnection.create(ch, null, 0, 0);
	}

	/**
	 * 创建客户端连接.
	 *
	 * @param ch AsynchronousSocketChannel
	 * @param addr SocketAddress
	 * @param readTimeoutSecond int
	 * @param writeTimeoutSecond int
	 * @return AsyncConnection
	 */
	public static AsyncConnection create(final AsynchronousSocketChannel ch, final SocketAddress addr, final int readTimeoutSecond, final int writeTimeoutSecond) {
		return new AIOTCPAsyncConnection(ch, addr, readTimeoutSecond, writeTimeoutSecond);
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

	private static class BIOUDPAsyncConnection extends AsyncConnection {

		private int readTimeoutSecond;

		private int writeTimeoutSecond;

		private final DatagramChannel channel;

		private final SocketAddress remoteAddress;

		private final boolean client;

		public BIOUDPAsyncConnection(final DatagramChannel ch, final SocketAddress addr, final boolean client, final int readTimeoutSecond, final int writeTimeoutSecond) {
			this.channel = ch;
			this.client = client;
			this.readTimeoutSecond = readTimeoutSecond;
			this.writeTimeoutSecond = writeTimeoutSecond;
			this.remoteAddress = addr;
		}

		@Override
		public <A> void read(final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, ? super A> handler) {
			try {
				final int rs = this.channel.read(dst);
				if (handler != null) {
					handler.completed(rs, attachment); // 当操作完成后被调用, rs参数表示操作结果, attachment参数表示提交操作请求时的参数
				}
			} catch (final IOException e) {
				if (handler != null) {
					handler.failed(e, attachment); // 当操作失败是调用, e参数表示失败原因, attachment参数表示提交操作请求时的参数
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

	private static class BIOTCPAsyncConnection extends AsyncConnection {

		private int readTimeoutSecond;

		private int writeTimeoutSecond;

		private final Socket socket;

		private final ReadableByteChannel readChannel;

		private final WritableByteChannel writeChannel;

		private final SocketAddress remoteAddress;

		public BIOTCPAsyncConnection(final Socket socket, final SocketAddress addr, final int readTimeoutSecond, final int writeTimeoutSecond) {
			this.socket = socket;
			ReadableByteChannel rc = null;
			WritableByteChannel wc = null;
			try {
				socket.setSoTimeout(Math.max(readTimeoutSecond, writeTimeoutSecond));
				rc = Channels.newChannel(socket.getInputStream());
				wc = Channels.newChannel(socket.getOutputStream());
			} catch (final Exception e) {
				e.printStackTrace();
			}
			this.readChannel = rc;
			this.writeChannel = wc;
			this.readTimeoutSecond = readTimeoutSecond;
			this.writeTimeoutSecond = writeTimeoutSecond;
			SocketAddress add = addr;
			if (add == null) {
				try {
					add = socket.getRemoteSocketAddress();
				} catch (final Exception e) {
				}
			}
			this.remoteAddress = add;
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

		public AIOTCPAsyncConnection(final AsynchronousSocketChannel ch, final SocketAddress addr, final int readTimeoutSecond, final int writeTimeoutSecond) {
			this.channel = ch;
			this.readTimeoutSecond = readTimeoutSecond;
			this.writeTimeoutSecond = writeTimeoutSecond;
			SocketAddress add = addr;
			if (add == null) {
				try {
					add = ch.getRemoteAddress();
				} catch (final Exception e) {
				}
			}
			this.remoteAddress = add;
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
