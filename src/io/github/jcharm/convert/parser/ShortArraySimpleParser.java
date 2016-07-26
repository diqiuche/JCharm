/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * short[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class ShortArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, short[]> {

	/** 实例对象. */
	public static final ShortArraySimpleParser INSTANCE = new ShortArraySimpleParser();

	@Override
	public void convertTo(final W out, final short[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final short v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeShort(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public short[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			short[] data = new short[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final short[] newdata = new short[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readShort();
			}
			in.readArrayEnd();
			final short[] newdata = new short[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final short[] values = new short[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readShort();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
