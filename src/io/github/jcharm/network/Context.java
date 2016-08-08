/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Logger;

import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.bson.BsonConvert;
import io.github.jcharm.convert.bson.BsonConvertFactory;
import io.github.jcharm.convert.json.JsonConvert;
import io.github.jcharm.convert.json.JsonConvertFactory;

/**
 * 上下文对象.
 */
public class Context {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	/** 服务的启动时间. */
	protected LocalDateTime serverStartTime;

	/** 线程池对象. */
	protected ExecutorService executorService;

	/** ByteBuffer的容量大小. */
	protected int bufferCapacity;

	/** ByteBuffer池. */
	protected ObjectPool<ByteBuffer> bufferPool;

	/** Response池. */
	protected ObjectPool<Response> responsePool;

	/** 服务内置Servlet. */
	protected PrepareServlet prepareServlet;

	private final InetSocketAddress inetSocketAddress;

	/** 服务数据的编解码. */
	protected Charset charset;

	/** 请求包大小的上限. */
	protected int maxbody;

	/** IO读取的超时秒数. */
	protected int readTimeoutSecond;

	/** IO写入的超时秒数. */
	protected int writeTimeoutSecond;

	/** 日志对象. */
	protected Logger logger;

	/** BSON双向序列化工厂对象. */
	protected BsonConvertFactory bsonConvertFactory;

	/** JSON双向序列化工厂对象. */
	protected JsonConvertFactory jsonConvertFactory;

	/**
	 * 构造函数.
	 *
	 * @param serverStartTime LocalDateTime
	 * @param logger Logger
	 * @param executorService ExecutorService
	 * @param bufferCapacity int
	 * @param bufferPool ObjectPool
	 * @param responsePool ObjectPool
	 * @param maxbody int
	 * @param charset Charset
	 * @param inetSocketAddress InetSocketAddress
	 * @param prepareServlet PrepareServlet
	 * @param readTimeoutSecond int
	 * @param writeTimeoutSecond int
	 */
	public Context(final LocalDateTime serverStartTime, final Logger logger, final ExecutorService executorService, final int bufferCapacity, final ObjectPool<ByteBuffer> bufferPool, final ObjectPool<Response> responsePool, final int maxbody, final Charset charset, final InetSocketAddress inetSocketAddress, final PrepareServlet prepareServlet,
			final int readTimeoutSecond, final int writeTimeoutSecond) {
		this.serverStartTime = serverStartTime;
		this.logger = logger;
		this.executorService = executorService;
		this.bufferCapacity = bufferCapacity;
		this.bufferPool = bufferPool;
		this.responsePool = responsePool;
		this.maxbody = maxbody;
		this.charset = Context.UTF8.equals(charset) ? null : charset;
		this.inetSocketAddress = inetSocketAddress;
		this.prepareServlet = prepareServlet;
		this.readTimeoutSecond = readTimeoutSecond;
		this.writeTimeoutSecond = writeTimeoutSecond;
		this.jsonConvertFactory = JsonConvertFactory.instance();
		this.bsonConvertFactory = BsonConvertFactory.instance();
	}

	/**
	 * 提交一个Runnable任务用于执行.
	 *
	 * @param runnable Runnable
	 */
	public void submit(final Runnable runnable) {
		this.executorService.submit(runnable);
	}

	/**
	 * 获取服务的启动时间.
	 *
	 * @return LocalDateTime
	 */
	public LocalDateTime getServerStartTime() {
		return this.serverStartTime;
	}

	/**
	 * 获取ByteBuffer的容量大小.
	 *
	 * @return int
	 */
	public int getBufferCapacity() {
		return this.bufferCapacity;
	}

	/**
	 * 获取ByteBuffer池, 包装到Supplier函数对象.
	 *
	 * @return Supplier
	 */
	public Supplier<ByteBuffer> getBufferSupplier() {
		return this.bufferPool;
	}

	/**
	 * 获取ByteBuffer池中的ByteBuffer.
	 *
	 * @return ByteBuffer
	 */
	public ByteBuffer pollBuffer() {
		return this.bufferPool.get();
	}

	/**
	 * 将指定的ByteBuffer插入到ByteBuffer池中.
	 *
	 * @param byteBuffer ByteBuffer
	 */
	public void offerBuffer(final ByteBuffer byteBuffer) {
		this.bufferPool.offer(byteBuffer);
	}

	/**
	 * 获取服务的InetSocketAddress.
	 *
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getServerAddress() {
		return this.inetSocketAddress;
	}

	/**
	 * 获取服务数据的编解码.
	 *
	 * @return Charset
	 */
	public Charset getCharset() {
		return this.charset;
	}

	/**
	 * 获取请求包大小的上限.
	 *
	 * @return int
	 */
	public int getMaxbody() {
		return this.maxbody;
	}

	/**
	 * 获取IO读取的超时秒数.
	 *
	 * @return the int
	 */
	public int getReadTimeoutSecond() {
		return this.readTimeoutSecond;
	}

	/**
	 * 获取IO写入的超时秒数.
	 *
	 * @return int
	 */
	public int getWriteTimeoutSecond() {
		return this.writeTimeoutSecond;
	}

	/**
	 * 获取日志对象.
	 *
	 * @return Logger
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * 获取BSON双向序列化对象.
	 *
	 * @return BsonConvert
	 */
	public BsonConvert getBsonConvert() {
		return this.bsonConvertFactory.getConvert();
	}

	/**
	 * 获取JSON双向序列化对象.
	 *
	 * @return JsonConvert
	 */
	public JsonConvert getJsonConvert() {
		return this.jsonConvertFactory.getConvert();
	}

}
