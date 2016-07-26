/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Enum的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 * @param <T> Enum的子类
 */
public final class EnumSimpleParser<R extends DeserializeReader, W extends SerializeWriter, T extends Enum> extends AbstractSimpleParser<R, W, T> {

	private final Class<T> type;

	/**
	 * 构造函数.
	 *
	 * @param type Class<T>
	 */
	public EnumSimpleParser(final Class<T> type) {
		this.type = type;
	}

	@Override
	public void convertTo(final W out, final T value) {
		if (value == null) {
			out.writeNull();
		} else {
			out.writeSmallString(value.toString());
		}
	}

	@Override
	public T convertFrom(final R in) {
		final String value = in.readSmallString();
		if (value == null) {
			return null;
		}
		return (T) Enum.valueOf(this.type, value);
	}

}
