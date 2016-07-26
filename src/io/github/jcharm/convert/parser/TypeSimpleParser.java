/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Class的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class TypeSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Class> {

	/** 实例对象. */
	public static final TypeSimpleParser INSTANCE = new TypeSimpleParser();

	@Override
	public void convertTo(final W out, final Class value) {
		if (value == null) {
			out.writeNull();
		} else {
			out.writeSmallString(value.getName());
		}
	}

	@Override
	public Class convertFrom(final R in) {
		final String str = in.readSmallString();
		if (str == null) {
			return null;
		}
		try {
			return Class.forName(str);
		} catch (final Exception e) {
			return null;
		}
	}

}
