/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * char[]的双向序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class CharArraySimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, char[]> {

	/** 实例对象. */
	public static final CharArraySimpleParser INSTANCE = new CharArraySimpleParser();

	@Override
	public void convertTo(final W out, final char[] values) {
		if (values == null) {
			out.writeNull();
			return;
		}
		out.writeArrayBegin(values.length);
		boolean flag = false;
		for (final char v : values) {
			if (flag) {
				out.writeArrayMark();
			}
			out.writeChar(v);
			flag = true;
		}
		out.writeArrayEnd();
	}

	@Override
	public char[] convertFrom(final R in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			int size = 0;
			char[] data = new char[8];
			while (in.hasNext()) {
				if (size >= data.length) {
					final char[] newdata = new char[data.length + 4];
					System.arraycopy(data, 0, newdata, 0, size);
					data = newdata;
				}
				data[size++] = in.readChar();
			}
			in.readArrayEnd();
			final char[] newdata = new char[size];
			System.arraycopy(data, 0, newdata, 0, size);
			return newdata;
		} else {
			final char[] values = new char[length];
			for (int i = 0; i < values.length; i++) {
				values[i] = in.readChar();
			}
			in.readArrayEnd();
			return values;
		}
	}

}
