/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * LocalDateTime的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class LocalDateTimeSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, LocalDateTime> {

	/** 实例对象. */
	public static final LocalDateTimeSimpleParser INSTANCE = new LocalDateTimeSimpleParser();

	@Override
	public void convertTo(final W out, final LocalDateTime value) {
		out.writeSmallString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	}

	@Override
	public LocalDateTime convertFrom(final R in) {
		try {
			return LocalDateTime.parse(in.readSmallString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (final Exception e) {
			return null;
		}
	}

}
