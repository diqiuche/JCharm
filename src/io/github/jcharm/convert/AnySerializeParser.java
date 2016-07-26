/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Type;

/**
 * 对不明类型的对象进行序列化的解析器, BSON序列化时将对象的类名写入, JSON则不写入.
 *
 * @param <T> 序列化数据类型
 */
public final class AnySerializeParser<T> implements SerializeParser<SerializeWriter, T> {

	/** 双向序列化工厂类. */
	final ConvertFactory convertFactory;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 */
	AnySerializeParser(final ConvertFactory convertFactory) {
		this.convertFactory = convertFactory;
	}

	@Override
	public void convertTo(final SerializeWriter out, final T value) {
		if (value == null) {
			out.writeClassName(null);
			out.writeNull();
			return;
		} else {
			if (out.needWriteClassName()) {
				out.writeClassName(value.getClass());
			}
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(out, value);
		}
	}

	@Override
	public Type getType() {
		return Object.class;
	}

}
