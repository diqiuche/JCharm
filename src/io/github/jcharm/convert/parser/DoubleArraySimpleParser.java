/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * double[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class DoubleArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, double[]> {

	/** 实例对象. */
	public static final DoubleArraySimpleParser INSTANCE = new DoubleArraySimpleParser();

	@Override
	public void convertTo(final W out, final double[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final double v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeDouble(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public double[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			double[] data = new double[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final double[] newdata = new double[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readDouble();
			}
			in.readArrayEnd();
			final double[] newdata = new double[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final double[] values = new double[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readDouble();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
