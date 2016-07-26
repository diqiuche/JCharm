/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Byte及相应基本数据类型的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class ByteSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Byte> {

	/** 实例对象. */
	public static final ByteSimpleParser INSTANCE = new ByteSimpleParser<>();

	@Override
	public void convertTo(final W out, final Byte value) {
		out.writeByte(value);
	}

	@Override
	public Byte convertFrom(final R in) {
		return in.readByte();
	}

}
