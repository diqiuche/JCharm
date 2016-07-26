/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * LocalDate的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class LocalDateSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, LocalDate> {

	/** 实例对象. */
	public static final LocalDateSimpleParser INSTANCE = new LocalDateSimpleParser();

	@Override
	public void convertTo(final W out, final LocalDate value) {
		out.writeSmallString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	@Override
	public LocalDate convertFrom(final R in) {
		try {
			return LocalDate.parse(in.readSmallString());
		} catch (final Exception e) {
			return null;
		}
	}

}
