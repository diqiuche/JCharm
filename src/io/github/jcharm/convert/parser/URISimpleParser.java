/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.net.URI;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * URI的双向序列化解析器.
 *
 * @param <R> the 反序列化输入流
 * @param <W> the 序列化输出流
 */
public final class URISimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, URI> {

	/** 实例对象. */
	public static final URISimpleParser INSTANCE = new URISimpleParser();

	@Override
	public void convertTo(final W out, final URI value) {
		if (value == null) {
			out.writeNull();
		} else {
			out.writeString(value.toString());
		}
	}

	@Override
	public URI convertFrom(final R in) {
		final String str = in.readString();
		if (str == null) {
			return null;
		}
		try {
			return new URI(str);
		} catch (final Exception e) {
			return null;
		}
	}

}
