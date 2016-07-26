/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Character及相应简单数据类型的双向解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class CharSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Character> {

	/** 实例对象. */
	public static final CharSimpleParser INSTANCE = new CharSimpleParser<>();

	@Override
	public void convertTo(final W out, final Character value) {
		out.writeChar(value);
	}

	@Override
	public Character convertFrom(final R in) {
		return in.readChar();
	}

}
