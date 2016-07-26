/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * String的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class StringSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, String> {

	/** 实例对象. */
	public static final StringSimpleParser INSTANCE = new StringSimpleParser();

	@Override
	public void convertTo(final W out, final String value) {
		out.writeString(value);
	}

	@Override
	public String convertFrom(final R in) {
		return in.readString();
	}

}
