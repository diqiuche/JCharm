/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

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
 * 传输客户端.
 */
public final class Transport {

	/** 缺省的应用传输协议. */
	public static final String DEFAULT_PROTOCOL = "TCP";

	/** 池的最大值. */
	protected static final int MAX_POOL_LIMIT = Runtime.getRuntime().availableProcessors() * 16;

	/** 是否支持TCP_NODELAY. */
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

	/** group的name属性. */
	protected String name;

	/** 是否TCP. */
	protected boolean tcp;

	/** 应用协议名称. */
	protected String protocol;

	/** AsynchronousChannelGroup. */
	protected AsynchronousChannelGroup group;

	/** Client Address. */
	protected InetSocketAddress clientAddress;

	/** Remote Addres. */
	protected InetSocketAddress[] remoteAddres = new InetSocketAddress[0];

	/** ByteBuffer池对象. */
	protected ObjectPool<ByteBuffer> bufferPool;

	/** ConcurrentHashMap. */
	protected final ConcurrentHashMap<SocketAddress, BlockingQueue<AsyncConnection>> connPool = new ConcurrentHashMap<>();

	/**
	 * 构造函数.
	 *
	 * @param name String
	 * @param transportBufferPool ObjectPool
	 * @param transportChannelGroup AsynchronousChannelGroup
	 * @param clientAddress InetSocketAddress
	 * @param addresses Collection
	 */
	public Transport(final String name, final ObjectPool<ByteBuffer> transportBufferPool, final AsynchronousChannelGroup transportChannelGroup, final InetSocketAddress clientAddress, final Collection<InetSocketAddress> addresses) {
		this(name, Transport.DEFAULT_PROTOCOL, transportBufferPool, transportChannelGroup, clientAddress, addresses);
	}

	/**
	 * 构造函数.
	 *
	 * @param name String
	 * @param protocol String
	 * @param transportBufferPool ObjectPool
	 * @param transportChannelGroup AsynchronousChannelGroup
	 * @param clientAddress InetSocketAddress
	 * @param addresses Collection
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
	 * 构造函数.
	 *
	 * @param transports Collection
	 */
	public Transport(final Collection<Transport> transports) {
		Transport first = null;
		final List<String> tmpgroup = new ArrayList();
		for (final Transport t : transports) {
			if (first == null) {
				first = t;
			}
			tmpgroup.add(t.name);
		}
		// 必须按字母排列顺序确保, 相同内容的transport列表组合的name相同, 而不会因为list的顺序不同产生不同的name
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
	 * 更新远程地址.
	 *
	 * @param addresses Collection
	 * @return InetSocketAddress[]
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
	 * 获取客户端名称.
	 *
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 关闭客户端.
	 */
	public void close() {
		this.connPool.forEach((k, v) -> v.forEach(c -> c.dispose()));
	}

	/**
	 * 获取客户端地址.
	 *
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getClientAddress() {
		return this.clientAddress;
	}

	/**
	 * 获取远程地址.
	 *
	 * @return InetSocketAddress[]
	 */
	public InetSocketAddress[] getRemoteAddresses() {
		return this.remoteAddres;
	}

	@Override
	public String toString() {
		return Transport.class.getSimpleName() + "{name = " + this.name + ", protocol = " + this.protocol + ", clientAddress = " + this.clientAddress + ", remoteAddres = " + Arrays.toString(this.remoteAddres) + "}";
	}

	/**
	 * 从ByteBuffer池中获取ByteBuffer.
	 *
	 * @return ByteBuffer
	 */
	public ByteBuffer pollBuffer() {
		return this.bufferPool.get();
	}

	/**
	 * 从ByteBuffer池中获取Supplier函数.
	 *
	 * @return Supplier
	 */
	public Supplier<ByteBuffer> getBufferSupplier() {
		return this.bufferPool;
	}

	/**
	 * 将byteBuffer存入对象池中.
	 *
	 * @param buffer ByteBuffer
	 */
	public void offerBuffer(final ByteBuffer buffer) {
		this.bufferPool.offer(buffer);
	}

	/**
	 * 将多个byteBuffer存入对象池中.
	 *
	 * @param buffers ByteBuffer
	 */
	public void offerBuffer(final ByteBuffer... buffers) {
		for (final ByteBuffer buffer : buffers) {
			this.offerBuffer(buffer);
		}
	}

	/**
	 * 判断是否TCP协议.
	 *
	 * @return boolean
	 */
	public boolean isTCP() {
		return this.tcp;
	}

	/**
	 * 获取AsyncConnection.
	 *
	 * @param addr SocketAddress
	 * @return AsyncConnection
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
	 * 存放AsyncConnection.
	 *
	 * @param forceClose boolean
	 * @param conn AsyncConnection
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
	 * 异步通信.
	 *
	 * @param <A> IO操作的类型
	 * @param addr SocketAddress
	 * @param buffer ByteBuffer
	 * @param att IO操作
	 * @param handler CompletionHandler
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
