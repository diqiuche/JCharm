/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.util.regex.Pattern;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Pattern的双向序列化解析器, Pattern是正则表达式的编译表示形式.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class PatternSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Pattern> {

	/** 实例对象. */
	public static final PatternSimpleParser INSTANCE = new PatternSimpleParser();

	@Override
	public void convertTo(final W out, final Pattern value) {
		if (value == null) {
			out.writeNull();
		} else {
			out.writeString(value.flags() + "," + value.pattern());
		}
	}

	@Override
	public Pattern convertFrom(final R in) {
		final String value = in.readString();
		if (value == null) {
			return null;
		}
		final int pos = value.indexOf(',');
		return Pattern.compile(value.substring(pos + 1), Integer.parseInt(value.substring(0, pos)));
	}

}
