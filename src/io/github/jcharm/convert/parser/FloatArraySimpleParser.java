/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * float[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class FloatArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, float[]> {

	/** 实例对象. */
	public static final FloatArraySimpleParser INSTANCE = new FloatArraySimpleParser();

	@Override
	public void convertTo(final W out, final float[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final float v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeFloat(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public float[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			float[] data = new float[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final float[] newdata = new float[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readFloat();
			}
			in.readArrayEnd();
			final float[] newdata = new float[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final float[] values = new float[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readFloat();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
