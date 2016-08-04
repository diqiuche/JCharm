/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.jcharm.common.ObjectPool;

/**
 * The Class Transport.
 */
public final class Transport {

	/** The Constant DEFAULT_PROTOCOL. */
	public static final String DEFAULT_PROTOCOL = "TCP";

	/** The Constant MAX_POOL_LIMIT. */
	protected static final int MAX_POOL_LIMIT = Runtime.getRuntime().availableProcessors() * 16;

	/** The Constant supportTcpNoDelay. */
	protected static final boolean supportTcpNoDelay;

	static {
		boolean tcpNoDelay = false;
		try {
			final AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			tcpNoDelay = channel.supportedOptions().contains(StandardSocketOptions.TCP_NODELAY);
			channel.close();
		} catch (final Exception e) {
		}
		supportTcpNoDelay = tcpNoDelay;
	}

	/** The name. */
	protected final String name; // 即<group>的name属性

	/** The tcp. */
	protected final boolean tcp;

	/** The protocol. */
	protected final String protocol;

	/** The group. */
	protected final AsynchronousChannelGroup group;

	/** The client address. */
	protected final InetSocketAddress clientAddress;

	/** The remote addres. */
	protected InetSocketAddress[] remoteAddres = new InetSocketAddress[0];

	/** The buffer pool. */
	protected final ObjectPool<ByteBuffer> bufferPool;

	/** The conn pool. */
	protected final ConcurrentHashMap<SocketAddress, BlockingQueue<AsyncConnection>> connPool = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new transport.
	 *
	 * @param name the name
	 * @param transportBufferPool the transport buffer pool
	 * @param transportChannelGroup the transport channel group
	 * @param clientAddress the client address
	 * @param addresses the addresses
	 */
	public Transport(final String name, final ObjectPool<ByteBuffer> transportBufferPool, final AsynchronousChannelGroup transportChannelGroup, final InetSocketAddress clientAddress, final Collection<InetSocketAddress> addresses) {
		this(name, Transport.DEFAULT_PROTOCOL, transportBufferPool, transportChannelGroup, clientAddress, addresses);
	}

	/**
	 * Instantiates a new transport.
	 *
	 * @param name the name
	 * @param protocol the protocol
	 * @param transportBufferPool the transport buffer pool
	 * @param transportChannelGroup the transport channel group
	 * @param clientAddress the client address
	 * @param addresses the addresses
	 */
	public Transport(final String name, final String protocol, final ObjectPool<ByteBuffer> transportBufferPool, final AsynchronousChannelGroup transportChannelGroup, final InetSocketAddress clientAddress, final Collection<InetSocketAddress> addresses) {
		this.name = name;
		this.protocol = protocol;
		this.tcp = "TCP".equalsIgnoreCase(protocol);
		this.group = transportChannelGroup;
		this.bufferPool = transportBufferPool;
		this.clientAddress = clientAddress;
		this.updateRemoteAddresses(addresses);
	}

	/**
	 * Instantiates a new transport.
	 *
	 * @param transports the transports
	 */
	public Transport(final Collection<Transport> transports) {
		Transport first = null;
		final List<String> tmpgroup = new ArrayList<>();
		for (final Transport t : transports) {
			if (first == null) {
				first = t;
			}
			tmpgroup.add(t.name);
		}
		// 必须按字母排列顺序确保，相同内容的transport列表组合的name相同，而不会因为list的顺序不同产生不同的name
		this.name = tmpgroup.stream().sorted().collect(Collectors.joining(";"));
		this.protocol = first.protocol;
		this.tcp = "TCP".equalsIgnoreCase(first.protocol);
		this.group = first.group;
		this.bufferPool = first.bufferPool;
		this.clientAddress = first.clientAddress;
		final Set<InetSocketAddress> addrs = new HashSet<>();
		transports.forEach(t -> addrs.addAll(Arrays.asList(t.getRemoteAddresses())));
		this.updateRemoteAddresses(addrs);
	}

	/**
	 * Update remote addresses.
	 *
	 * @param addresses the addresses
	 * @return the inet socket address[]
	 */
	public final InetSocketAddress[] updateRemoteAddresses(final Collection<InetSocketAddress> addresses) {
		final InetSocketAddress[] oldAddresses = this.remoteAddres;
		final List<InetSocketAddress> list = new ArrayList<>();
		if (addresses != null) {
			for (final InetSocketAddress addr : addresses) {
				if ((this.clientAddress != null) && this.clientAddress.equals(addr)) {
					continue;
				}
				list.add(addr);
			}
		}
		this.remoteAddres = list.toArray(new InetSocketAddress[list.size()]);
		return oldAddresses;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Close.
	 */
	public void close() {
		this.connPool.forEach((k, v) -> v.forEach(c -> c.dispose()));
	}

	/**
	 * Gets the client address.
	 *
	 * @return the client address
	 */
	public InetSocketAddress getClientAddress() {
		return this.clientAddress;
	}

	/**
	 * Gets the remote addresses.
	 *
	 * @return the remote addresses
	 */
	public InetSocketAddress[] getRemoteAddresses() {
		return this.remoteAddres;
	}

	@Override
	public String toString() {
		return Transport.class.getSimpleName() + "{name = " + this.name + ", protocol = " + this.protocol + ", clientAddress = " + this.clientAddress + ", remoteAddres = " + Arrays.toString(this.remoteAddres) + "}";
	}

	/**
	 * Poll buffer.
	 *
	 * @return the byte buffer
	 */
	public ByteBuffer pollBuffer() {
		return this.bufferPool.get();
	}

	/**
	 * Gets the buffer supplier.
	 *
	 * @return the buffer supplier
	 */
	public Supplier<ByteBuffer> getBufferSupplier() {
		return this.bufferPool;
	}

	/**
	 * Offer buffer.
	 *
	 * @param buffer the buffer
	 */
	public void offerBuffer(final ByteBuffer buffer) {
		this.bufferPool.offer(buffer);
	}

	/**
	 * Offer buffer.
	 *
	 * @param buffers the buffers
	 */
	public void offerBuffer(final ByteBuffer... buffers) {
		for (final ByteBuffer buffer : buffers) {
			this.offerBuffer(buffer);
		}
	}

	/**
	 * Checks if is tcp.
	 *
	 * @return true, if is tcp
	 */
	public boolean isTCP() {
		return this.tcp;
	}

	/**
	 * Poll connection.
	 *
	 * @param addr the addr
	 * @return the async connection
	 */
	public AsyncConnection pollConnection(SocketAddress addr) {
		if ((addr == null) && (this.remoteAddres.length == 1)) {
			addr = this.remoteAddres[0];
		}
		final boolean rand = addr == null;
		if (rand && (this.remoteAddres.length < 1)) {
			throw new RuntimeException("Transport (" + this.name + ") has no remoteAddress list");
		}
		try {
			if (this.tcp) {
				AsynchronousSocketChannel channel = null;
				if (rand) { // 取地址
					for (int i = 0; i < this.remoteAddres.length; i++) {
						addr = this.remoteAddres[i];
						final BlockingQueue<AsyncConnection> queue = this.connPool.get(addr);
						if ((queue != null) && !queue.isEmpty()) {
							AsyncConnection conn;
							while ((conn = queue.poll()) != null) {
								if (conn.isOpen()) {
									return conn;
								}
							}
						}
						if (channel == null) {
							channel = AsynchronousSocketChannel.open(this.group);
							if (Transport.supportTcpNoDelay) {
								channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
							}
						}
						try {
							channel.connect(addr).get(2, TimeUnit.SECONDS);
							break;
						} catch (final Exception iex) {
							iex.printStackTrace();
							if (i == (this.remoteAddres.length - 1)) {
								channel = null;
							}
						}
					}
				} else {
					channel = AsynchronousSocketChannel.open(this.group);
					if (Transport.supportTcpNoDelay) {
						channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
					}
					channel.connect(addr).get(2, TimeUnit.SECONDS);
				}
				if (channel == null) {
					return null;
				}
				return AsyncConnection.create(channel, addr, 3000, 3000);
			} else { // UDP
				if (rand) {
					addr = this.remoteAddres[0];
				}
				final DatagramChannel channel = DatagramChannel.open();
				channel.configureBlocking(true);
				channel.connect(addr);
				return AsyncConnection.create(channel, addr, true, 3000, 3000);
			}
		} catch (final Exception ex) {
			throw new RuntimeException("transport address = " + addr, ex);
		}
	}

	/**
	 * Offer connection.
	 *
	 * @param forceClose the force close
	 * @param conn the conn
	 */
	public void offerConnection(final boolean forceClose, final AsyncConnection conn) {
		if (!forceClose && conn.isTCP()) {
			if (conn.isOpen()) {
				BlockingQueue<AsyncConnection> queue = this.connPool.get(conn.getRemoteAddress());
				if (queue == null) {
					queue = new ArrayBlockingQueue<>(Transport.MAX_POOL_LIMIT);
					this.connPool.put(conn.getRemoteAddress(), queue);
				}
				if (!queue.offer(conn)) {
					conn.dispose();
				}
			}
		} else {
			conn.dispose();
		}
	}

	/**
	 * Async.
	 *
	 * @param <A> the generic type
	 * @param addr the addr
	 * @param buffer the buffer
	 * @param att the att
	 * @param handler the handler
	 */
	public <A> void async(final SocketAddress addr, final ByteBuffer buffer, final A att, final CompletionHandler<Integer, A> handler) {
		final AsyncConnection conn = this.pollConnection(addr);
		conn.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(final Integer result, final ByteBuffer attachment) {
				buffer.clear();
				conn.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

					@Override
					public void completed(final Integer result, final ByteBuffer attachment) {
						if (handler != null) {
							handler.completed(result, att);
						}
						Transport.this.offerBuffer(buffer);
						Transport.this.offerConnection(false, conn);
					}

					@Override
					public void failed(final Throwable exc, final ByteBuffer attachment) {
						Transport.this.offerBuffer(buffer);
						Transport.this.offerConnection(true, conn);
					}
				});

			}

			@Override
			public void failed(final Throwable exc, final ByteBuffer attachment) {
				Transport.this.offerBuffer(buffer);
				Transport.this.offerConnection(true, conn);
			}
		});
	}

}
