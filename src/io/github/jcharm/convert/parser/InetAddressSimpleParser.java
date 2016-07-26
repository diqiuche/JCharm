/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.net.InetAddress;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * InetAddress的双向解析器, InetAddress此类表示互联网协议(IP)地址.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class InetAddressSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, InetAddress> {

	/** 实例对象. */
	public static final InetAddressSimpleParser INSTANCE = new InetAddressSimpleParser();

	@Override
	public void convertTo(final W out, final InetAddress value) {
		if (value == null) {
			out.writeNull();
			return;
		}
		ByteArraySimpleParser.INSTANCE.convertTo(out, value.getAddress());
	}

	@Override
	public InetAddress convertFrom(final R in) {
		final byte[] bytes = ByteArraySimpleParser.INSTANCE.convertFrom(in);
		if (bytes == null) {
			return null;
		}
		try {
			return InetAddress.getByAddress(bytes);
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * InetAddress的JsonSimpleParser实现
	 *
	 * @param <R> 反序列化输入流
	 * @param <W> 序列化输出流
	 */
	public final static class InetAddressJsonSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, InetAddress> {

		/** 实例对象. */
		public static final InetAddressJsonSimpleParser INSTANCE = new InetAddressJsonSimpleParser();

		@Override
		public void convertTo(final W out, final InetAddress value) {
			if (value == null) {
				out.writeNull();
				return;
			}
			StringSimpleParser.INSTANCE.convertTo(out, value.getHostAddress());
		}

		@Override
		public InetAddress convertFrom(final R in) {
			final String str = StringSimpleParser.INSTANCE.convertFrom(in);
			if (str == null) {
				return null;
			}
			try {
				return InetAddress.getByName(str);
			} catch (final Exception ex) {
				return null;
			}
		}

	}

}
