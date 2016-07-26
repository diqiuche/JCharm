/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Float及相应简单数据类型的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class FloatSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Float> {

	/** 实例对象. */
	public static final FloatSimpleParser INSTANCE = new FloatSimpleParser();

	@Override
	public void convertTo(final W out, final Float value) {
		out.writeFloat(value);
	}

	@Override
	public Float convertFrom(final R in) {
		return in.readFloat();
	}

}
