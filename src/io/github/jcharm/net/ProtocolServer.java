/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

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
 * The Class ProtocolServer.
 */
public abstract class ProtocolServer {

	/**
	 * Open.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void open() throws IOException;

	/**
	 * Bind.
	 *
	 * @param local the local
	 * @param backlog the backlog
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void bind(SocketAddress local, int backlog) throws IOException;

	/**
	 * Supported options.
	 *
	 * @param <T> the generic type
	 * @return the sets the
	 */
	public abstract <T> Set<SocketOption<?>> supportedOptions();

	/**
	 * Sets the option.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract <T> void setOption(SocketOption<T> name, T value) throws IOException;

	/**
	 * Accept.
	 */
	public abstract void accept();

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void close() throws IOException;

	/**
	 * Gets the channel group.
	 *
	 * @return the channel group
	 */
	public abstract AsynchronousChannelGroup getChannelGroup();

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
			ch.configureBlocking(true);
			this.serverChannel = ch;
		}

		@Override
		public void bind(final SocketAddress local, final int backlog) throws IOException {
			this.serverChannel.bind(local);
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
			final CountDownLatch cdl = new CountDownLatch(1);
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

	private static final class ProtocolTCPServer extends ProtocolServer {

		private final Context context;

		private AsynchronousChannelGroup group;

		private AsynchronousServerSocketChannel serverChannel;

		public ProtocolTCPServer(final Context context) {
			this.context = context;
		}

		@Override
		public void open() throws IOException {
			this.group = AsynchronousChannelGroup.withCachedThreadPool(this.context.executor, 1);
			this.serverChannel = AsynchronousServerSocketChannel.open(this.group);
		}

		@Override
		public void bind(final SocketAddress local, final int backlog) throws IOException {
			this.serverChannel.bind(local, backlog);
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

}
