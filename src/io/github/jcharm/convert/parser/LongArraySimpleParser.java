/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * long[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class LongArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, long[]> {

	/** 实例对象. */
	public static final LongArraySimpleParser INSTANCE = new LongArraySimpleParser();

	@Override
	public void convertTo(final W out, final long[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final long v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeLong(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public long[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			long[] data = new long[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final long[] newdata = new long[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readLong();
			}
			in.readArrayEnd();
			final long[] newdata = new long[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final long[] values = new long[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readLong();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
