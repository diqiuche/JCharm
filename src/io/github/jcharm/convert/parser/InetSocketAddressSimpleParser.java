/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * InetSocketAddress的双向序列化解析器, InetSocketAddress此类实现IP套接字地址(IP 地址 + 端口号).
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class InetSocketAddressSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, InetSocketAddress> {

	/** 实例对象. */
	public static final InetSocketAddressSimpleParser INSTANCE = new InetSocketAddressSimpleParser();

	@Override
	public void convertTo(final W out, final InetSocketAddress value) {
		if (value == null) {
			out.writeNull();
			return;
		}
		ByteArraySimpleParser.INSTANCE.convertTo(out, value.getAddress().getAddress());
		out.writeInt(value.getPort());
	}

	@Override
	public InetSocketAddress convertFrom(final R in) {
		final byte[] bytes = ByteArraySimpleParser.INSTANCE.convertFrom(in);
		if (bytes == null) {
			return null;
		}
		final int port = in.readInt();
		try {
			return new InetSocketAddress(InetAddress.getByAddress(bytes), port);
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * InetSocketAddress的JsonSimpleParser实现.
	 *
	 * @param <R> 反序列化输入流
	 * @param <W> 序列化输出流
	 */
	public final static class InetSocketAddressJsonSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, InetSocketAddress> {

		/** 实例对象. */
		public static final InetSocketAddressJsonSimpleParser INSTANCE = new InetSocketAddressJsonSimpleParser();

		@Override
		public void convertTo(final W out, final InetSocketAddress value) {
			if (value == null) {
				out.writeNull();
				return;
			}
			StringSimpleParser.INSTANCE.convertTo(out, value.getHostString() + ":" + value.getPort());
		}

		@Override
		public InetSocketAddress convertFrom(final R in) {
			final String str = StringSimpleParser.INSTANCE.convertFrom(in);
			if (str == null) {
				return null;
			}
			try {
				final int pos = str.indexOf(':');
				return new InetSocketAddress(str.substring(0, pos), Integer.parseInt(str.substring(pos + 1)));
			} catch (final Exception ex) {
				return null;
			}
		}

	}

}
