/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.io.Serializable;

import io.github.jcharm.convert.ConvertFactory;
import io.github.jcharm.convert.ConvertType;
import io.github.jcharm.convert.DeSerializeParser;
import io.github.jcharm.convert.SerializeParser;

/**
 * BSON双向序列化工厂类.
 */
public final class BsonConvertFactory extends ConvertFactory<BsonDeserializeReader, BsonSerializeWriter> {

	private static final BsonConvertFactory INSTANCE = new BsonConvertFactory(null);

	/** The Constant objectDeSerializeParser. */
	static final DeSerializeParser objectDeSerializeParser = BsonConvertFactory.INSTANCE.loadDeSerializeParser(Object.class);

	/** The Constant objectSerializeParser. */
	static final SerializeParser objectSerializeParser = BsonConvertFactory.INSTANCE.loadSerializeParser(Object.class);

	static {
		BsonConvertFactory.INSTANCE.registerDeSerializeParser(Serializable.class, BsonConvertFactory.objectDeSerializeParser);
		BsonConvertFactory.INSTANCE.registerSerializeParser(Serializable.class, BsonConvertFactory.objectSerializeParser);
	}

	private BsonConvertFactory(final ConvertFactory parentConvertFactory) {
		super(parentConvertFactory);
	}

	/**
	 * 获取双向序列化工厂实例.
	 *
	 * @return BsonConvertFactory
	 */
	public static BsonConvertFactory instance() {
		return BsonConvertFactory.INSTANCE;
	}

	/**
	 * 创建新的双向序列化工厂类.
	 *
	 * @return BsonConvertFactory
	 */
	public static BsonConvertFactory createFactory() {
		return new BsonConvertFactory(null);
	}

	@Override
	public ConvertType getConvertType() {
		return ConvertType.BSON;
	}

	@Override
	public ConvertFactory createChildFactory() {
		return new BsonConvertFactory(this);
	}

	@Override
	public BsonConvert getConvert() {
		if (this.convert == null) {
			this.convert = new BsonConvert(this);
		}
		return (BsonConvert) this.convert;
	}

}
