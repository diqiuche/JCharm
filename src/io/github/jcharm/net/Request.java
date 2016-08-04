/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import io.github.jcharm.convert.bson.BsonConvert;
import io.github.jcharm.convert.json.JsonConvert;

/**
 * Request请求.
 *
 * @param <C> Context上下文
 */
public abstract class Request<C extends Context> {

	/** The context. */
	protected final C context;

	/** The bson convert. */
	protected final BsonConvert bsonConvert;

	/** The json convert. */
	protected final JsonConvert jsonConvert;

	/** The createtime. */
	protected long createtime;

	/** The keep alive. */
	protected boolean keepAlive;

	/** The channel. */
	protected AsyncConnection channel;

	private final Map<String, Object> properties = new HashMap<>();

	/** The attributes. */
	protected final Map<String, Object> attributes = new HashMap<>();

	/**
	 * Instantiates a new request.
	 *
	 * @param context the context
	 */
	protected Request(final C context) {
		this.context = context;
		this.bsonConvert = context.getBsonConvert();
		this.jsonConvert = context.getJsonConvert();
	}

	/**
	 * Read header.
	 *
	 * @param buffer the buffer
	 * @return the int
	 */
	protected abstract int readHeader(ByteBuffer buffer);

	/**
	 * Read body.
	 *
	 * @param buffer the buffer
	 * @return the int
	 */
	protected abstract int readBody(ByteBuffer buffer);

	/**
	 * Prepare.
	 */
	protected abstract void prepare();

	/**
	 * Recycle.
	 */
	protected void recycle() {
		this.createtime = 0;
		this.keepAlive = false;
		this.attributes.clear();
		this.channel = null; // close it by response
	}

	/**
	 * Sets the property.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param value the value
	 * @return the t
	 */
	protected <T> T setProperty(final String name, final T value) {
		this.properties.put(name, value);
		return value;
	}

	/**
	 * Gets the property.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the property
	 */
	protected <T> T getProperty(final String name) {
		return (T) this.properties.get(name);
	}

	/**
	 * Removes the property.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the t
	 */
	protected <T> T removeProperty(final String name) {
		return (T) this.properties.remove(name);
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	protected Map<String, Object> getProperties() {
		return this.properties;
	}

	/**
	 * Sets the attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param value the value
	 * @return the t
	 */
	public <T> T setAttribute(final String name, final T value) {
		this.attributes.put(name, value);
		return value;
	}

	/**
	 * Gets the attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(final String name) {
		return (T) this.attributes.get(name);
	}

	/**
	 * Removes the attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the t
	 */
	public <T> T removeAttribute(final String name) {
		return (T) this.attributes.remove(name);
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public C getContext() {
		return this.context;
	}

	/**
	 * Gets the createtime.
	 *
	 * @return the createtime
	 */
	public long getCreatetime() {
		return this.createtime;
	}

}
