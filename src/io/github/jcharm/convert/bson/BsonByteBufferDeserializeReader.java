/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.nio.ByteBuffer;

import io.github.jcharm.common.CommonUtils;
import io.github.jcharm.convert.DeserializeReader;

/**
 * BSON ByteBuffer反序列化输入流.
 */
public class BsonByteBufferDeserializeReader extends BsonDeserializeReader {

	private ByteBuffer[] buffers;

	private int currentIndex = 0;

	private ByteBuffer currentBuffer;

	protected BsonByteBufferDeserializeReader(final ByteBuffer... buffers) {
		this.buffers = buffers;
		if ((buffers != null) && (buffers.length > 0)) {
			this.currentBuffer = buffers[this.currentIndex];
		}
	}

	@Override
	protected boolean recycle() {
		super.recycle(); // this.position 初始化值为-1
		this.currentIndex = 0;
		this.currentBuffer = null;
		this.buffers = null;
		return false;
	}

	@Override
	protected byte currentByte() {
		return this.currentBuffer.get(this.currentBuffer.position());
	}

	@Override
	public final int readArrayBegin() {
		final short bt = this.readShort();
		if (bt == DeserializeReader.SIGN_NULL) {
			return bt;
		}
		final short lt = this.readShort();
		return ((bt & 0xffff) << 16) | (lt & 0xffff);
	}

	@Override
	public final boolean readBoolean() {
		return this.readByte() == 1;
	}

	@Override
	public byte readByte() {
		if (this.currentBuffer.hasRemaining()) {
			this.position++;
			return this.currentBuffer.get();
		}
		for (;;) {
			this.currentBuffer = this.buffers[++this.currentIndex];
			if (this.currentBuffer.hasRemaining()) {
				this.position++;
				return this.currentBuffer.get();
			}
		}
	}

	@Override
	public final char readChar() {
		if (this.currentBuffer != null) {
			final int remain = this.currentBuffer.remaining();
			if (remain >= 2) {
				this.position += 2;
				return this.currentBuffer.getChar();
			}
		}
		return (char) ((0xff00 & (this.readByte() << 8)) | (0xff & this.readByte()));
	}

	@Override
	public final short readShort() {
		if (this.currentBuffer != null) {
			final int remain = this.currentBuffer.remaining();
			if (remain >= 2) {
				this.position += 2;
				return this.currentBuffer.getShort();
			}
		}
		return (short) ((0xff00 & (this.readByte() << 8)) | (0xff & this.readByte()));
	}

	@Override
	public final int readInt() {
		if (this.currentBuffer != null) {
			final int remain = this.currentBuffer.remaining();
			if (remain >= 4) {
				this.position += 4;
				return this.currentBuffer.getInt();
			}
		}
		return ((this.readByte() & 0xff) << 24) | ((this.readByte() & 0xff) << 16) | ((this.readByte() & 0xff) << 8) | (this.readByte() & 0xff);
	}

	@Override
	public final long readLong() {
		if (this.currentBuffer != null) {
			final int remain = this.currentBuffer.remaining();
			if (remain >= 8) {
				this.position += 8;
				return this.currentBuffer.getLong();
			}
		}
		return ((((long) this.readByte() & 0xff) << 56) | (((long) this.readByte() & 0xff) << 48) | (((long) this.readByte() & 0xff) << 40) | (((long) this.readByte() & 0xff) << 32) | (((long) this.readByte() & 0xff) << 24) | (((long) this.readByte() & 0xff) << 16)
				| (((long) this.readByte() & 0xff) << 8) | (((long) this.readByte() & 0xff)));
	}

	protected byte[] read(final int len) {
		final byte[] bs = new byte[len];
		this.read(bs, 0);
		return bs;
	}

	private void read(final byte[] bs, final int pos) {
		final int remain = this.currentBuffer.remaining();
		if (remain < 1) {
			this.currentBuffer = this.buffers[++this.currentIndex];
			this.read(bs, pos);
			return;
		}
		final int len = bs.length - pos;
		if (remain >= len) {
			this.position += len;
			this.currentBuffer.get(bs, pos, len);
			return;
		}
		this.currentBuffer.get(bs, pos, remain);
		this.position += remain;
		this.currentBuffer = this.buffers[++this.currentIndex];
		this.read(bs, pos + remain);
	}

	@Override
	public final String readSmallString() {
		final int len = 0xff & this.readByte();
		if (len == 0) {
			return "";
		}
		return new String(this.read(len));
	}

	@Override
	public final String readString() {
		final int len = this.readInt();
		if (len == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (len == 0) {
			return "";
		}
		return new String(CommonUtils.decodeUTF8ToChars(this.read(len)));
	}

}
