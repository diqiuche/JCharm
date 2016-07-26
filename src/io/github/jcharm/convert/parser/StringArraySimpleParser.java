/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * String[]的双向序列化解析器.
 *
 * @param <R> 反系列化输入流
 * @param <W> 序列化输出流
 */
public final class StringArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, String[]> {

	/** 实例对象. */
	public static final StringArraySimpleParser INSTANCE = new StringArraySimpleParser();

	@Override
	public void convertTo(final W out, final String[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final String v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeString(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public String[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			String[] data = new String[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final String[] newdata = new String[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readString();
			}
			in.readArrayEnd();
			final String[] newdata = new String[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final String[] values = new String[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readString();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
