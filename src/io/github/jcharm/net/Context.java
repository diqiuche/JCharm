/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Logger;

import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.bson.BsonConvert;
import io.github.jcharm.convert.bson.BsonConvertFactory;
import io.github.jcharm.convert.json.JsonConvert;
import io.github.jcharm.convert.json.JsonConvertFactory;

/**
 * Context上下文.
 */
public class Context {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	protected final long serverStartTime;

	protected final ExecutorService executor;

	protected final int bufferCapacity;

	protected final ObjectPool<ByteBuffer> bufferPool;

	protected final ObjectPool<Response> responsePool;

	protected final PrepareServlet prepare;

	private final InetSocketAddress address;

	protected final Charset charset;

	protected final int maxbody;

	protected final int readTimeoutSecond;

	protected final int writeTimeoutSecond;

	protected final Logger logger;

	protected final BsonConvertFactory bsonConvertFactory;

	protected final JsonConvertFactory jsonConvertFactory;

	public Context(final long serverStartTime, final Logger logger, final ExecutorService executor, final int bufferCapacity, final ObjectPool<ByteBuffer> bufferPool, final ObjectPool<Response> responsePool, final int maxbody, final Charset charset, final InetSocketAddress address,
			final PrepareServlet prepare, final int readTimeoutSecond, final int writeTimeoutSecond) {
		this.serverStartTime = serverStartTime;
		this.logger = logger;
		this.executor = executor;
		this.bufferCapacity = bufferCapacity;
		this.bufferPool = bufferPool;
		this.responsePool = responsePool;
		this.maxbody = maxbody;
		this.charset = Context.UTF8.equals(charset) ? null : charset;
		this.address = address;
		this.prepare = prepare;
		this.readTimeoutSecond = readTimeoutSecond;
		this.writeTimeoutSecond = writeTimeoutSecond;
		this.jsonConvertFactory = JsonConvertFactory.instance();
		this.bsonConvertFactory = BsonConvertFactory.instance();
	}

	public int getMaxbody() {
		return this.maxbody;
	}

	public InetSocketAddress getServerAddress() {
		return this.address;
	}

	public long getServerStartTime() {
		return this.serverStartTime;
	}

	public Charset getCharset() {
		return this.charset;
	}

	public void submit(final Runnable r) {
		this.executor.submit(r);
	}

	public int getBufferCapacity() {
		return this.bufferCapacity;
	}

	public Supplier<ByteBuffer> getBufferSupplier() {
		return this.bufferPool;
	}

	public ByteBuffer pollBuffer() {
		return this.bufferPool.get();
	}

	public void offerBuffer(final ByteBuffer buffer) {
		this.bufferPool.offer(buffer);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public int getReadTimeoutSecond() {
		return this.readTimeoutSecond;
	}

	public int getWriteTimeoutSecond() {
		return this.writeTimeoutSecond;
	}

	public JsonConvert getJsonConvert() {
		return this.jsonConvertFactory.getConvert();
	}

	public BsonConvert getBsonConvert() {
		return this.bsonConvertFactory.getConvert();
	}

}
