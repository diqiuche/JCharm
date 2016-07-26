/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * byte[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class ByteArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, byte[]> {

	/** 实例对象. */
	public static final ByteArraySimpleParser INSTANCE = new ByteArraySimpleParser();

	@Override
	public void convertTo(final W out, final byte[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final byte value : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeByte(value);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public byte[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			byte[] data = new byte[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final byte[] newData = new byte[data.length + 4];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
				}
				data[size++] = in.readByte();
			}
			in.readArrayEnd();
			final byte[] newData = new byte[size];
			System.arraycopy(data, 0, newData, 0, size);
			return newData;
		} else {
			final byte[] values = new byte[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readByte();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
