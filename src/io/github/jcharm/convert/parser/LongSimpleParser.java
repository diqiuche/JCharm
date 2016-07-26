/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Long及相应简单数据类型的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class LongSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Long> {

	/** 实例对象. */
	public static final LongSimpleParser INSTANCE = new LongSimpleParser();

	@Override
	public void convertTo(final W out, final Long value) {
		out.writeLong(value);
	}

	@Override
	public Long convertFrom(final R in) {
		return in.readLong();
	}

}
