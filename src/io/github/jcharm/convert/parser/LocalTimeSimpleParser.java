/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * LocalTime的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class LocalTimeSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, LocalTime> {

	/** 实例对象. */
	public static final LocalTimeSimpleParser INSTANCE = new LocalTimeSimpleParser();

	@Override
	public void convertTo(final W out, final LocalTime value) {
		out.writeSmallString(value.format(DateTimeFormatter.ISO_LOCAL_TIME));
	}

	@Override
	public LocalTime convertFrom(final R in) {
		try {
			return LocalTime.parse(in.readSmallString());
		} catch (final Exception e) {
			return null;
		}
	}

}
