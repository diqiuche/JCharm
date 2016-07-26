/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * BSON ByteBuffer序列化输出流.
 */
public class BsonByteBufferSerializeWriter extends BsonSerializeWriter {

	private final Supplier<ByteBuffer> supplier;

	private ByteBuffer[] buffers;

	private int index;

	/**
	 * 构造函数.
	 *
	 * @param supplier Supplier
	 */
	public BsonByteBufferSerializeWriter(final Supplier<ByteBuffer> supplier) {
		super((byte[]) null);
		this.supplier = supplier;
	}

	@Override
	public ByteBuffer[] toBuffers() {
		if (this.buffers == null) {
			return new ByteBuffer[0];
		}
		for (int i = this.index; i < this.buffers.length; i++) {
			final ByteBuffer buf = this.buffers[i];
			if (buf.position() != 0) {
				buf.flip();
			}
		}
		return this.buffers;
	}

	@Override
	public byte[] toArray() {
		if (this.buffers == null) {
			return new byte[0];
		}
		int pos = 0;
		final byte[] bytes = new byte[this.count];
		for (final ByteBuffer buf : this.toBuffers()) {
			final int r = buf.remaining();
			buf.get(bytes, pos, r);
			buf.flip();
			pos += r;
		}
		return bytes;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[count=" + this.count + "]";
	}

	@Override
	protected int expand(final int byteLength) {
		if (this.buffers == null) {
			this.index = 0;
			this.buffers = new ByteBuffer[] { this.supplier.get() };
		}
		ByteBuffer buffer = this.buffers[this.index];
		if (!buffer.hasRemaining()) {
			buffer.flip();
			buffer = this.supplier.get();
			final ByteBuffer[] bufs = new ByteBuffer[this.buffers.length + 1];
			System.arraycopy(this.buffers, 0, bufs, 0, this.buffers.length);
			bufs[this.buffers.length] = buffer;
			this.buffers = bufs;
			this.index++;
		}
		int len = buffer.remaining();
		int size = 0;
		while (len < byteLength) {
			buffer = this.supplier.get();
			final ByteBuffer[] bufs = new ByteBuffer[this.buffers.length + 1];
			System.arraycopy(this.buffers, 0, bufs, 0, this.buffers.length);
			bufs[this.buffers.length] = buffer;
			this.buffers = bufs;
			len += buffer.remaining();
			size++;
		}
		return size;
	}

	@Override
	public void writeTo(final byte[] chs, final int start, final int len) {
		if (this.expand(len) == 0) {
			this.buffers[this.index].put(chs, start, len);
		} else {
			ByteBuffer buffer = this.buffers[this.index];
			final int end = start + len;
			int remain = len; // 还剩多少没有写
			while (remain > 0) {
				final int br = buffer.remaining();
				if (remain > br) { // 一个buffer写不完
					buffer.put(chs, end - remain, br);
					buffer = this.nextByteBuffer();
					remain -= br;
				} else {
					buffer.put(chs, end - remain, remain);
					remain = 0;
				}
			}
		}
		this.count += len;
	}

	private ByteBuffer nextByteBuffer() {
		this.buffers[this.index].flip();
		return this.buffers[++this.index];
	}

	@Override
	public void writeTo(final byte ch) {
		this.expand(1);
		this.buffers[this.index].put(ch);
		this.count++;
	}

	@Override
	protected boolean recycle() {
		this.index = 0;
		this.buffers = null;
		return false;
	}

}
