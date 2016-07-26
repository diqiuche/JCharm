/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * int[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class IntegerArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, int[]> {

	/** 实例对象. */
	public static final IntegerArraySimpleParser INSTANCE = new IntegerArraySimpleParser();

	@Override
	public void convertTo(final W out, final int[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final int v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeInt(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public int[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			int[] data = new int[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final int[] newdata = new int[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readInt();
			}
			in.readArrayEnd();
			final int[] newdata = new int[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final int[] values = new int[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readInt();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
