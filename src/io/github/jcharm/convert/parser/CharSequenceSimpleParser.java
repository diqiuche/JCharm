/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * CharSequence的双向序列化解析器, CharSequence是char值的一个可读序列, CharSequence接口对许多不同种类的char序列提供统一的只读访问.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class CharSequenceSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, CharSequence> {

	/** 实例对象. */
	public static final CharSequenceSimpleParser INSTANCE = new CharSequenceSimpleParser();

	@Override
	public void convertTo(final W out, final CharSequence value) {
		out.writeString(value == null ? null : value.toString());
	}

	@Override
	public CharSequence convertFrom(final R in) {
		return in.readString();
	}

}
