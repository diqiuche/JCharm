/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.io.IOException;
import java.io.OutputStream;

import io.github.jcharm.convert.ConvertException;

/**
 * BSON Stream序列化输出流.
 */
class BsonStreamSerializeWriter extends BsonByteBufferSerializeWriter {

	private OutputStream out;

	/**
	 * 构造函数.
	 *
	 * @param out OutputStream
	 */
	protected BsonStreamSerializeWriter(final OutputStream out) {
		super(null);
		this.out = out;
	}

	@Override
	protected boolean recycle() {
		super.recycle();
		this.out = null;
		return false;
	}

	@Override
	public void writeTo(final byte[] chs, final int start, final int len) {
		try {
			this.out.write(chs, start, len);
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

	@Override
	public void writeTo(final byte ch) {
		try {
			this.out.write(ch);
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

}
