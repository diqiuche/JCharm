/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.github.jcharm.convert.bson.BsonConvert;
import io.github.jcharm.convert.json.JsonConvert;

/**
 * Request抽象类.
 *
 * @param <C> Context子类型
 */
public abstract class Request<C extends Context> {

	/** 上下文对象. */
	protected C context;

	/** BSON双向序列化对象. */
	protected BsonConvert bsonConvert;

	/** JSON双向序列化对象. */
	protected JsonConvert jsonConvert;

	/** 请求创建时间. */
	protected LocalDateTime createtime;

	/** 是否保持连接. */
	protected boolean keepAlive;

	/** 异步连接. */
	protected AsyncConnection asyncConnection;

	/** properties与attributes的区别在于 : 调用recycle时, attributes会被清空而properties会保留. */
	private final Map<String, Object> properties = new HashMap(); // properties通常存放需要永久绑定在request里的一些对象

	/** properties与attributes的区别在于 : 调用recycle时, attributes会被清空而properties会保留. */
	protected final Map<String, Object> attributes = new HashMap();

	/**
	 * 构造函数.
	 *
	 * @param context Context子类型
	 */
	protected Request(final C context) {
		this.context = context;
		this.bsonConvert = context.getBsonConvert();
		this.jsonConvert = context.getJsonConvert();
	}

	/**
	 * 返回值 : Integer.MIN_VALUE : 帧数据, -1 : 数据不合法, 0 : 解析完毕, >0 : 需再读取的字节数.
	 *
	 * @param byteBuffer ByteBuffer
	 * @return int
	 */
	protected abstract int readHeader(ByteBuffer byteBuffer);

	/**
	 * 读取byteBuffer, 并返回读取的有效数据长度.
	 *
	 * @param byteBuffer ByteBuffer
	 * @return the int
	 */
	protected abstract int readBody(ByteBuffer byteBuffer);

	/**
	 * 准备Request.
	 */
	protected abstract void prepare();

	/**
	 * 重置Request.
	 */
	protected void recycle() {
		this.createtime = null;
		this.keepAlive = false;
		this.attributes.clear();
		this.asyncConnection = null; // close it by response
	}

	/**
	 * 设置property.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @param value 泛型
	 * @return T
	 */
	protected <T> T setProperty(final String name, final T value) {
		this.properties.put(name, value);
		return value;
	}

	/**
	 * 获取property.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @return T
	 */
	protected <T> T getProperty(final String name) {
		return (T) this.properties.get(name);
	}

	/**
	 * 移除property.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @return T
	 */
	protected <T> T removeProperty(final String name) {
		return (T) this.properties.remove(name);
	}

	/**
	 * 获取property映射集.
	 *
	 * @return Map
	 */
	protected Map<String, Object> getProperties() {
		return this.properties;
	}

	/**
	 * 设置attribute.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @param value T
	 * @return T
	 */
	public <T> T setAttribute(final String name, final T value) {
		this.attributes.put(name, value);
		return value;
	}

	/**
	 * 获取attribute.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @return T
	 */
	public <T> T getAttribute(final String name) {
		return (T) this.attributes.get(name);
	}

	/**
	 * 移除attribute.
	 *
	 * @param <T> 泛型
	 * @param name String
	 * @return T
	 */
	public <T> T removeAttribute(final String name) {
		return (T) this.attributes.remove(name);
	}

	/**
	 * 获取attribute映射集.
	 *
	 * @return Map
	 */
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	/**
	 * 获取Context子类.
	 *
	 * @return C
	 */
	public C getContext() {
		return this.context;
	}

	/**
	 * 获取请求创建时间.
	 *
	 * @return LocalDateTime
	 */
	public LocalDateTime getCreatetime() {
		return this.createtime;
	}

}
