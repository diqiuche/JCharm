/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import io.github.jcharm.convert.ConvertException;

/**
 * JSON Stream序列化输出流.
 */
class JsonStreamSerializeWriter extends JsonByteBufferSerializeWriter {

	private OutputStream out;

	/**
	 * 构造函数.
	 *
	 * @param charset Charset
	 * @param out OutputStream
	 */
	protected JsonStreamSerializeWriter(final Charset charset, final OutputStream out) {
		super(charset, null);
		this.out = out;
	}

	/**
	 * 构造函数.
	 *
	 * @param out OutputStream
	 */
	protected JsonStreamSerializeWriter(final OutputStream out) {
		this(null, out);
	}

	@Override
	protected boolean recycle() {
		super.recycle();
		this.out = null;
		return false;
	}

	@Override
	public void writeTo(final char ch) {
		if (ch > Byte.MAX_VALUE) {
			throw new ConvertException("writeTo char(int.value = " + (int) ch + ") must be less 127");
		}
		try {
			this.out.write((byte) ch);
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

	private void writeTo(final boolean quote, final char[] chs, final int start, final int len) {
		try {
			if (quote) {
				this.out.write('"');
			}
			if (this.charset == null) { // UTF-8
				final int limit = start + len;
				for (int i = start; i < limit; i++) {
					final char c = chs[i];
					if (c < 0x80) {
						this.out.write((byte) c);
					} else if (c < 0x800) {
						this.out.write((byte) (0xc0 | (c >> 6)));
						this.out.write((byte) (0x80 | (c & 0x3f)));
					} else {
						this.out.write((byte) (0xe0 | ((c >> 12))));
						this.out.write((byte) (0x80 | ((c >> 6) & 0x3f)));
						this.out.write((byte) (0x80 | (c & 0x3f)));
					}
				}
			} else {
				final ByteBuffer bb = this.charset.encode(CharBuffer.wrap(chs, start, len));
				this.out.write(bb.array());
			}
			if (quote) {
				this.out.write('"');
			}
		} catch (final IOException e) {
			throw new ConvertException(e);
		}
	}

	@Override
	public void writeTo(final char[] chs, final int start, final int len) {
		this.writeTo(false, chs, start, len);
	}

	@Override
	public void writeTo(final boolean quote, final String value) {
		final char[] chs = value.toCharArray();
		this.writeTo(quote, chs, 0, chs.length);
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
			this.writeTo(true, chs, 0, len);
			return;
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
		this.writeTo(true, cs, 0, sb.length());
	}

}
