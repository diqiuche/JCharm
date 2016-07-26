/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.github.jcharm.convert.ConvertFactory;
import io.github.jcharm.convert.ConvertType;
import io.github.jcharm.convert.parser.BigIntegerSimpleParser;
import io.github.jcharm.convert.parser.InetAddressSimpleParser;
import io.github.jcharm.convert.parser.InetSocketAddressSimpleParser;

/**
 * JSON的双向序列化工厂类.
 */
public final class JsonConvertFactory extends ConvertFactory<JsonDeserializeReader, JsonSerializeWriter> {

	private static final JsonConvertFactory INSTANCE = new JsonConvertFactory(null);

	static {
		JsonConvertFactory.INSTANCE.registerParser(InetAddress.class, InetAddressSimpleParser.InetAddressJsonSimpleParser.INSTANCE);
		JsonConvertFactory.INSTANCE.registerParser(InetSocketAddress.class, InetSocketAddressSimpleParser.InetSocketAddressJsonSimpleParser.INSTANCE);
		JsonConvertFactory.INSTANCE.registerParser(BigInteger.class, BigIntegerSimpleParser.BigIntegerJsonSimpleParser.INSTANCE);
		JsonConvertFactory.INSTANCE.registerSerializeParser(Serializable.class, JsonConvertFactory.INSTANCE.loadSerializeParser(Object.class));
	}

	private JsonConvertFactory(final JsonConvertFactory parent) {
		super(parent);
	}

	/**
	 * 获取双向序列化工厂实例.
	 *
	 * @return JsonConvertFactory
	 */
	public static JsonConvertFactory instance() {
		return JsonConvertFactory.INSTANCE;
	}

	/**
	 * 创建新的双向序列化工厂类.
	 *
	 * @return JsonConvertFactory
	 */
	public static JsonConvertFactory createFactory() {
		return new JsonConvertFactory(null);
	}

	@Override
	public ConvertType getConvertType() {
		return ConvertType.JSON;
	}

	@Override
	public ConvertFactory createChildFactory() {
		return new JsonConvertFactory(this);
	}

	@Override
	public final JsonConvert getConvert() {
		if (this.convert == null) {
			this.convert = new JsonConvert(this);
		}
		return (JsonConvert) this.convert;
	}
}
