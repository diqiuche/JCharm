/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Supplier;

import io.github.jcharm.common.CommonUtils;
import io.github.jcharm.convert.ConvertException;

/**
 * JSON ByteBuffer序列化输出流.
 */
public class JsonByteBufferSerializeWriter extends JsonSerializeWriter {

	/** The Constant UTF8. */
	protected static final Charset UTF8 = Charset.forName("UTF-8");

	/** The charset. */
	protected Charset charset;

	private final Supplier<ByteBuffer> supplier;

	private ByteBuffer[] buffers;

	private int index;

	/**
	 * 构造函数.
	 *
	 * @param charset Charset
	 * @param supplier Supplier
	 */
	protected JsonByteBufferSerializeWriter(final Charset charset, final Supplier<ByteBuffer> supplier) {
		this.charset = JsonByteBufferSerializeWriter.UTF8.equals(charset) ? null : charset;
		this.supplier = supplier;
	}

	/**
	 * 构造函数.
	 *
	 * @param supplier Supplier
	 */
	protected JsonByteBufferSerializeWriter(final Supplier<ByteBuffer> supplier) {
		this(null, supplier);
	}

	@Override
	protected boolean recycle() {
		this.index = 0;
		this.charset = null;
		this.buffers = null;
		return false;
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
	public int getCount() {
		if (this.buffers == null) {
			return 0;
		}
		int len = 0;
		for (final ByteBuffer buffer : this.buffers) {
			len += buffer.remaining();
		}
		return len;
	}

	private int expand(final int byteLength) {
		if (this.buffers == null) {
			this.index = 0;
			this.buffers = new ByteBuffer[] { this.supplier.get() };
		}
		ByteBuffer buffer = this.buffers[this.index];
		// hasRemaining : 告知在当前位置和限制之间是否有元素
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
	public void writeTo(final char ch) {
		if (ch > Byte.MAX_VALUE) {
			throw new ConvertException("writeTo char(int.value = " + (int) ch + ") must be less 127");
		}
		this.expand(1);
		this.buffers[this.index].put((byte) ch);
	}

	private ByteBuffer nextByteBuffer() {
		this.buffers[this.index].flip();
		return this.buffers[++this.index];
	}

	private ByteBuffer putChar(ByteBuffer buffer, final char c) {
		if (c < 0x80) {
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) c);
		} else if (c < 0x800) {
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) (0xc0 | (c >> 6)));
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) (0x80 | (c & 0x3f)));
		} else {
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) (0xe0 | ((c >> 12))));
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) (0x80 | ((c >> 6) & 0x3f)));
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) (0x80 | (c & 0x3f)));
		}
		return buffer;
	}

	private void writeTo(int expandsize, final boolean quote, final char[] chs, final int start, final int len) {
		int byteLength = quote ? 2 : 0;
		ByteBuffer bb = null;
		if (this.charset == null) {
			byteLength += CommonUtils.encodeUTF8Length(chs, start, len);
		} else {
			bb = this.charset.encode(CharBuffer.wrap(chs, start, len));
			byteLength += bb.remaining();
		}
		if (expandsize < 0) {
			expandsize = this.expand(byteLength);
		}
		if (expandsize == 0) { // 只需要一个buffer
			final ByteBuffer buffer = this.buffers[this.index];
			if (quote) {
				buffer.put((byte) '"');
			}
			if (this.charset == null) { // UTF-8
				final int limit = start + len;
				for (int i = start; i < limit; i++) {
					final char c = chs[i];
					if (c < 0x80) {
						buffer.put((byte) c);
					} else if (c < 0x800) {
						buffer.put((byte) (0xc0 | (c >> 6)));
						buffer.put((byte) (0x80 | (c & 0x3f)));
					} else {
						buffer.put((byte) (0xe0 | ((c >> 12))));
						buffer.put((byte) (0x80 | ((c >> 6) & 0x3f)));
						buffer.put((byte) (0x80 | (c & 0x3f)));
					}
				}
			} else {
				buffer.put(bb);
			}
			if (quote) {
				buffer.put((byte) '"');
			}
			return;
		}
		ByteBuffer buffer = this.buffers[this.index];
		if (quote) {
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) '"');
		}
		if (this.charset == null) { // UTF-8
			final int limit = start + len;
			for (int i = start; i < limit; i++) {
				buffer = this.putChar(buffer, chs[i]);
			}
		} else {
			while (bb.hasRemaining()) {
				if (!buffer.hasRemaining()) {
					buffer = this.nextByteBuffer();
				}
				buffer.put(bb.get());
			}
		}
		if (quote) {
			if (!buffer.hasRemaining()) {
				buffer = this.nextByteBuffer();
			}
			buffer.put((byte) '"');
		}
	}

	@Override
	public void writeTo(final boolean quote, final String value) {
		final char[] chs = value.toCharArray();
		this.writeTo(-1, quote, chs, 0, chs.length);
	}

	@Override
	public void writeInt(final int value) {
		this.writeTo(false, String.valueOf(value));
	}

	@Override
	public void writeLong(final long value) {
		this.writeTo(false, String.valueOf(value));
	}

	@Override
	public void writeString(final String value) {
		if (value == null) {
			this.writeNull();
			return;
		}
		final char[] chs = value.toCharArray();
		int len = 0;
		for (final char ch : chs) {
			switch (ch) {
			case '\n':
				len += 2;
				break;
			case '\r':
				len += 2;
				break;
			case '\t':
				len += 2;
				break;
			case '\\':
				len += 2;
				break;
			case '"':
				len += 2;
				break;
			default:
				len++;
				break;
			}
		}
		if (len == chs.length) {
			this.writeTo(-1, true, chs, 0, len);
			return;
		}
		int expandsize = -1;
		if (this.charset == null) { // UTF-8
			final int byteLength = 2 + CommonUtils.encodeEscapeUTF8Length(chs, 0, chs.length);
			expandsize = this.expand(byteLength);
			if (expandsize == 0) { // 只需要一个buffer
				final ByteBuffer buffer = this.buffers[this.index];
				buffer.put((byte) '"');
				for (final char c : chs) {
					switch (c) {
					case '\n':
						buffer.put((byte) '\\').put((byte) 'n');
						break;
					case '\r':
						buffer.put((byte) '\\').put((byte) 'r');
						break;
					case '\t':
						buffer.put((byte) '\\').put((byte) 't');
						break;
					case '\\':
						buffer.put((byte) '\\').put((byte) '\\');
						break;
					case '"':
						buffer.put((byte) '\\').put((byte) '"');
						break;
					default:
						if (c < 0x80) {
							buffer.put((byte) c);
						} else if (c < 0x800) {
							buffer.put((byte) (0xc0 | (c >> 6)));
							buffer.put((byte) (0x80 | (c & 0x3f)));
						} else {
							buffer.put((byte) (0xe0 | ((c >> 12))));
							buffer.put((byte) (0x80 | ((c >> 6) & 0x3f)));
							buffer.put((byte) (0x80 | (c & 0x3f)));
						}
						break;
					}
				}
				buffer.put((byte) '"');
				return;
			}
		}
		final StringBuilder sb = new StringBuilder(len);
		for (final char ch : chs) {
			switch (ch) {
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '"':
				sb.append("\\\"");
				break;
			default:
				sb.append(ch);
				break;
			}
		}
		final char[] cs = sb.toString().toCharArray();
		this.writeTo(expandsize, true, cs, 0, sb.length());
	}

	@Override
	public String toString() {
		return Objects.toString(this);
	}

}
