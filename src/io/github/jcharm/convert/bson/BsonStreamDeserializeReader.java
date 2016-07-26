/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.io.IOException;
import java.io.InputStream;

import io.github.jcharm.convert.ConvertException;

/**
 * BSON Stream反序列化输入流.
 */
class BsonStreamDeserializeReader extends BsonByteBufferDeserializeReader {

	private InputStream in;

	private byte currByte;

	/**
	 * 构造函数.
	 *
	 * @param in InputStream
	 */
	protected BsonStreamDeserializeReader(final InputStream in) {
		this.in = in;
	}

	@Override
	protected boolean recycle() {
		super.recycle(); // this.position 初始化值为-1
		this.in = null;
		this.currByte = 0;
		return false;
	}

	@Override
	public byte readByte() {
		try {
			final byte b = (this.currByte = (byte) this.in.read());
			this.position++;
			return b;
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

	@Override
	protected byte currentByte() {
		return this.currByte;
	}

	@Override
	protected byte[] read(final int len) {
		final byte[] bs = new byte[len];
		try {
			this.in.read(bs);
			this.position += len;
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
		return bs;
	}

}
