/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * Boolean及相应基本类型的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class BooleanSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, Boolean> {

	/** 实例对象. */
	public static final BooleanSimpleParser INSTANCE = new BooleanSimpleParser<>();

	@Override
	public void convertTo(final W out, final Boolean value) {
		out.writeBoolean(value);
	}

	@Override
	public Boolean convertFrom(final R in) {
		return in.readBoolean();
	}

}
