/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.io.IOException;
import java.io.InputStream;

import io.github.jcharm.convert.ConvertException;

/**
 * JSON Stream反序列化输入流.
 */
class JsonStreamDeserializeReader extends JsonByteBufferDeserializeReader {

	private InputStream in;

	/**
	 * 构造函数.
	 *
	 * @param in InputStream
	 */
	protected JsonStreamDeserializeReader(final InputStream in) {
		this.in = in;
	}

	@Override
	protected boolean recycle() {
		super.recycle(); // this.position 初始化值为-1
		this.in = null;
		return false;
	}

	@Override
	protected byte nextByte() {
		try {
			final byte b = (byte) this.in.read();
			this.position++;
			return b;
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

}
