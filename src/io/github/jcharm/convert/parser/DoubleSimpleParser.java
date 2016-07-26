/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Double及相应简单数据类型的双向解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class DoubleSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Double> {

	/** 实例对象. */
	public static final DoubleSimpleParser INSTANCE = new DoubleSimpleParser();

	@Override
	public void convertTo(final W out, final Double value) {
		out.writeDouble(value);
	}

	@Override
	public Double convertFrom(final R in) {
		return in.readDouble();
	}

}
