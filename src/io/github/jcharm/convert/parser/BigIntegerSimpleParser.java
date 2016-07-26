/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.math.BigInteger;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * BigInteger的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class BigIntegerSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, BigInteger> {

	/** 实例对象. */
	public static final BigIntegerSimpleParser INSTANCE = new BigIntegerSimpleParser();

	@Override
	public void convertTo(final W out, final BigInteger value) {
		if (value == null) {
			out.writeNull();
			return;
		}
		ByteArraySimpleParser.INSTANCE.convertTo(out, value.toByteArray());
	}

	@Override
	public BigInteger convertFrom(final R in) {
		final String value = in.readString();
		if (value == null) {
			return null;
		}
		return new BigInteger(value);
	}

	/**
	 * BigInteger的JsonSimpleParser实现
	 *
	 * @param <R> 反序列化输入流
	 * @param <W> 序列化输出流
	 */
	public final static class BigIntegerJsonSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, BigInteger> {

		/** 实例对象. */
		public static final BigIntegerJsonSimpleParser INSTANCE = new BigIntegerJsonSimpleParser();

		@Override
		public void convertTo(final W out, final BigInteger value) {
			if (value == null) {
				out.writeNull();
			} else {
				out.writeString(value.toString());
			}
		}

		@Override
		public BigInteger convertFrom(final R in) {
			final String str = in.readString();
			if (str == null) {
				return null;
			}
			return new BigInteger(str);
		}

	}

}
