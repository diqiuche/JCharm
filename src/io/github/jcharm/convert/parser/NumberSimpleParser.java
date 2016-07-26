/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Number的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 系列化输出流
 */
public final class NumberSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Number> {

	/** 实例对象. */
	public static final NumberSimpleParser INSTANCE = new NumberSimpleParser();

	@Override
	public void convertTo(final W out, final Number value) {
		out.writeLong(value == null ? 0L : value.longValue());
	}

	@Override
	public Number convertFrom(final R in) {
		return in.readLong();
	}

}
