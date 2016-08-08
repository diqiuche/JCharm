/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 传输层协议的服务抽象类.
 */
public abstract class ProtocolServer {

	/**
	 * 开启服务.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void open() throws IOException;

	/**
	 * 服务绑定.
	 *
	 * @param socketAddress SocketAddress
	 * @param backlog int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void bind(SocketAddress socketAddress, int backlog) throws IOException;

	/**
	 * 获取通道支持的Socket选项集.
	 *
	 * @param <T> 泛型
	 * @return Set
	 */
	public abstract <T> Set<SocketOption<?>> supportedOptions();

	/**
	 * 设置Socket选项的值.
	 *
	 * @param <T> 泛型
	 * @param name SocketOption
	 * @param value T
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract <T> void setOption(SocketOption<T> name, T value) throws IOException;

	/**
	 * 接受连接.
	 */
	public abstract void accept();

	/**
	 * 关闭服务.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void close() throws IOException;

	/**
	 * 获取AsynchronousChannelGroup.
	 *
	 * @return AsynchronousChannelGroup
	 */
	public abstract AsynchronousChannelGroup getChannelGroup(); // 异步通道组, 共享一个java线程池

	/**
	 * Creates the.
	 *
	 * @param protocol the protocol
	 * @param context the context
	 * @return the protocol server
	 */
	public static ProtocolServer create(final String protocol, final Context context) {
		if ("TCP".equalsIgnoreCase(protocol)) {
			return new ProtocolTCPServer(context);
		}
		if ("UDP".equalsIgnoreCase(protocol)) {
			return new ProtocolUDPServer(context);
		}
		throw new RuntimeException("ProtocolServer not support protocol " + protocol);
	}

	private static final class ProtocolTCPServer extends ProtocolServer {

		private final Context context;

		private AsynchronousChannelGroup group;

		private AsynchronousServerSocketChannel serverChannel;

		public ProtocolTCPServer(final Context context) {
			this.context = context;
		}

		@Override
		public void open() throws IOException {
			this.group = AsynchronousChannelGroup.withCachedThreadPool(this.context.executorService, 1);
			this.serverChannel = AsynchronousServerSocketChannel.open(this.group);
		}

		@Override
		public void bind(final SocketAddress socketAddress, final int backlog) throws IOException {
			this.serverChannel.bind(socketAddress, backlog);
		}

		@Override
		public <T> Set<SocketOption<?>> supportedOptions() {
			return this.serverChannel.supportedOptions();
		}

		@Override
		public <T> void setOption(final SocketOption<T> name, final T value) throws IOException {
			this.serverChannel.setOption(name, value);
		}

		@Override
		public void accept() {
			final AsynchronousServerSocketChannel serchannel = this.serverChannel;
			serchannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(final AsynchronousSocketChannel channel, final Void attachment) {
					serchannel.accept(null, this);
					ProtocolTCPServer.this.context.submit(new PrepareRunner(ProtocolTCPServer.this.context, AsyncConnection.create(channel, null, ProtocolTCPServer.this.context.readTimeoutSecond, ProtocolTCPServer.this.context.writeTimeoutSecond), null));
				}

				@Override
				public void failed(final Throwable exc, final Void attachment) {
					serchannel.accept(null, this);
				}
			});
		}

		@Override
		public void close() throws IOException {
			this.serverChannel.close();
		}

		@Override
		public AsynchronousChannelGroup getChannelGroup() {
			return this.group;
		}

	}

	private static final class ProtocolUDPServer extends ProtocolServer {

		private boolean running;

		private final Context context;

		private DatagramChannel serverChannel;

		public ProtocolUDPServer(final Context context) {
			this.context = context;
		}

		@Override
		public void open() throws IOException {
			final DatagramChannel ch = DatagramChannel.open();
			ch.configureBlocking(true); // 通道为阻塞模式
			this.serverChannel = ch;
		}

		@Override
		public void bind(final SocketAddress socketAddress, final int backlog) throws IOException {
			this.serverChannel.bind(socketAddress);
		}

		@Override
		public <T> Set<SocketOption<?>> supportedOptions() {
			return this.serverChannel.supportedOptions();
		}

		@Override
		public <T> void setOption(final SocketOption<T> name, final T value) throws IOException {
			this.serverChannel.setOption(name, value);
		}

		@Override
		public void accept() {
			final DatagramChannel serchannel = this.serverChannel;
			final int readTimeoutSecond = this.context.readTimeoutSecond;
			final int writeTimeoutSecond = this.context.writeTimeoutSecond;
			final CountDownLatch cdl = new CountDownLatch(1); // 一个同步辅助类, 在完成一组正在其他线程中执行的操作之前, 它允许一个或多个线程一直等待
			this.running = true;
			new Thread() {

				@Override
				public void run() {
					cdl.countDown();
					while (ProtocolUDPServer.this.running) {
						final ByteBuffer buffer = ProtocolUDPServer.this.context.pollBuffer();
						try {
							final SocketAddress address = serchannel.receive(buffer);
							buffer.flip();
							final AsyncConnection conn = AsyncConnection.create(serchannel, address, false, readTimeoutSecond, writeTimeoutSecond);
							ProtocolUDPServer.this.context.submit(new PrepareRunner(ProtocolUDPServer.this.context, conn, buffer));
						} catch (final Exception e) {
							ProtocolUDPServer.this.context.offerBuffer(buffer);
						}
					}
				}
			}.start();
			try {
				cdl.await();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws IOException {
			this.running = false;
			this.serverChannel.close();
		}

		@Override
		public AsynchronousChannelGroup getChannelGroup() {
			return null;
		}

	}

}
