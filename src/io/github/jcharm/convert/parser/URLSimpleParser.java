/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.net.URL;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * URL的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class URLSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, URL> {

	/** 实例对象. */
	public static final URLSimpleParser INSTANCE = new URLSimpleParser();

	@Override
	public void convertTo(final W out, final URL value) {
		if (value == null) {
			out.writeNull();
		} else {
			out.writeString(value.toString());
		}
	}

	@Override
	public URL convertFrom(final R in) {
		final String str = in.readString();
		if (str == null) {
			return null;
		}
		try {
			return new URL(str);
		} catch (final Exception e) {
			return null;
		}
	}

}
