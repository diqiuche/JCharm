/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Integer及相应简单数据类型的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化的输出流
 */
public final class IntegerSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Integer> {

	/** 实例对象. */
	public static final IntegerSimpleParser INSTANCE = new IntegerSimpleParser();

	@Override
	public void convertTo(final W out, final Integer value) {
		out.writeInt(value);
	}

	@Override
	public Integer convertFrom(final R in) {
		return in.readInt();
	}

}
