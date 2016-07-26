/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * boolean[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 系列化输出流
 */
public final class BooleanArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, boolean[]> {

	/** 实例对象. */
	public static final BooleanArraySimpleParser INSTANCE = new BooleanArraySimpleParser();

	@Override
	public void convertTo(final W out, final boolean[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final boolean value : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeBoolean(value);
			flag = true;
		}
	}

	@Override
	public boolean[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			boolean[] data = new boolean[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final boolean[] newData = new boolean[data.length + 4];
					System.arraycopy(data, 0, newData, 0, size);
					data = newData;
				}
				data[size++] = in.readBoolean();
			}
			in.readArrayEnd();
			final boolean[] newdata = new boolean[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final boolean[] values = new boolean[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readBoolean();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
