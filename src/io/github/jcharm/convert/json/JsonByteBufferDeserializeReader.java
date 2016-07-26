/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.nio.ByteBuffer;
import java.nio.charset.UnmappableCharacterException;

import io.github.jcharm.convert.ConvertException;
import io.github.jcharm.convert.DeserializeReader;

/**
 * JSON ByteBuffer反序列化输入流.
 */
public class JsonByteBufferDeserializeReader extends JsonDeserializeReader {

	private char currentChar;

	private ByteBuffer[] buffers;

	private int currentIndex = 0;

	private ByteBuffer currentBuffer;

	/**
	 * 构造函数.
	 *
	 * @param buffers ByteBuffer
	 */
	protected JsonByteBufferDeserializeReader(final ByteBuffer... buffers) {
		this.buffers = buffers;
		if ((buffers != null) && (buffers.length > 0)) {
			this.currentBuffer = buffers[this.currentIndex];
		}
	}

	@Override
	protected boolean recycle() {
		super.recycle(); // this.position 初始化值为-1
		this.currentIndex = 0;
		this.currentChar = 0;
		this.currentBuffer = null;
		this.buffers = null;
		return false;
	}

	/**
	 * 获取下一个字节.
	 *
	 * @return byte
	 */
	protected byte nextByte() {
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
	protected final char nextChar() {
		if (this.currentChar != 0) {
			final char ch = this.currentChar;
			this.currentChar = 0;
			return ch;
		}
		if (this.currentBuffer != null) {
			final int remain = this.currentBuffer.remaining();
			if ((remain == 0) && ((this.currentIndex + 1) >= this.buffers.length)) {
				return 0;
			}
		}
		final byte b1 = this.nextByte();
		if (b1 >= 0) {// 1 byte, 7 bits: 0xxxxxxx
			return (char) b1;
		} else if (((b1 >> 5) == -2) && ((b1 & 0x1e) != 0)) { // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
			return (char) (((b1 << 6) ^ this.nextByte()) ^ (((byte) 0xC0 << 6) ^ ((byte) 0x80)));
		} else if ((b1 >> 4) == -2) { // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
			return (char) ((b1 << 12) ^ (this.nextByte() << 6) ^ (this.nextByte() ^ (((byte) 0xE0 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80))));
		} else { // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			throw new RuntimeException(new UnmappableCharacterException(4));
		}
	}

	@Override
	protected final char nextGoodChar() {
		char c = this.nextChar();
		if ((c > ' ') || (c == 0)) {// 0 表示buffer结尾了
			return c;
		}
		for (;;) {
			c = this.nextChar();
			if ((c > ' ') || (c == 0)) {
				return c;
			}
		}
	}

	@Override
	protected final void backChar(final char ch) {
		this.currentChar = ch;
	}

	@Override
	public String readObjectBegin(final Class clazz) {
		final char ch = this.nextGoodChar();
		if (ch == '{') {
			return "";
		}
		if ((ch == 'n') && (this.nextChar() == 'u') && (this.nextChar() == 'l') && (this.nextChar() == 'l')) {
			return null;
		}
		if ((ch == 'N') && (this.nextChar() == 'U') && (this.nextChar() == 'L') && (this.nextChar() == 'L')) {
			return null;
		}
		throw new ConvertException("a json object text must begin with '{' (position = " + this.position + ") but '" + ch + "'");
	}

	@Override
	public final int readArrayBegin() {
		final char ch = this.nextGoodChar();
		if ((ch == '[') || (ch == '{')) {
			return DeserializeReader.SIGN_NOLENGTH;
		}
		if ((ch == 'n') && (this.nextChar() == 'u') && (this.nextChar() == 'l') && (this.nextChar() == 'l')) {
			return DeserializeReader.SIGN_NULL;
		}
		if ((ch == 'N') && (this.nextChar() == 'U') && (this.nextChar() == 'L') && (this.nextChar() == 'L')) {
			return DeserializeReader.SIGN_NULL;
		}
		throw new ConvertException("a json array text must begin with '[' (position = " + this.position + ") but '" + ch + "'");
	}

	@Override
	public final void readBlank() {
		final char ch = this.nextGoodChar();
		if (ch == ':') {
			return;
		}
		throw new ConvertException("expected a ':' but '" + ch + "'(position = " + this.position + ")");
	}

	@Override
	public final boolean hasNext() {
		final char ch = this.nextGoodChar();
		if (ch == ',') {
			return true;
		}
		if ((ch == '}') || (ch == ']')) {
			return false;
		}
		this.backChar(ch);
		return true;
	}

	@Override
	public final String readSmallString() {
		char ch = this.nextGoodChar();
		if (ch == 0) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		if ((ch == '"') || (ch == '\'')) {
			final char quote = ch;
			for (;;) {
				ch = this.nextChar();
				if (ch == '\\') {
					final char c = this.nextChar();
					switch (c) {
					case '"':
					case '\'':
					case '\\':
					case '/':
						sb.append(c);
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 'u':
						sb.append((char) Integer.parseInt(new String(new char[] { this.nextChar(), this.nextChar(), this.nextChar(), this.nextChar() }), 16));
						break;
					case 't':
						sb.append('\t');
						break;
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					default:
						throw new ConvertException("illegal escape(" + c + ") (position = " + this.position + ")");
					}
				} else if ((ch == quote) || (ch == 0)) {
					break;
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		} else {
			sb.append(ch);
			for (;;) {
				ch = this.nextChar();
				if (ch == '\\') {
					final char c = this.nextChar();
					switch (c) {
					case '"':
					case '\'':
					case '\\':
					case '/':
						sb.append(c);
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 'u':
						sb.append((char) Integer.parseInt(new String(new char[] { this.nextChar(), this.nextChar(), this.nextChar(), this.nextChar() }), 16));
						break;
					case 't':
						sb.append('\t');
						break;
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					default:
						throw new ConvertException("illegal escape(" + c + ") (position = " + this.position + ")");
					}
				} else if ((ch == ',') || (ch == ']') || (ch == '}') || (ch <= ' ') || (ch == ':')) { // ch <= ' ' 包含 0
					break;
				} else {
					sb.append(ch);
				}
			}
			final String rs = sb.toString();
			return "null".equalsIgnoreCase(rs) ? null : rs;
		}
	}

	@Override
	public final int readInt() {
		char firstchar = this.nextGoodChar();
		if ((firstchar == '"') || (firstchar == '\'')) {
			firstchar = this.nextChar();
			if ((firstchar == '"') || (firstchar == '\'')) {
				return 0;
			}
		}
		int value = 0;
		final boolean negative = firstchar == '-';
		if (!negative) {
			if ((firstchar < '0') || (firstchar > '9')) {
				throw new ConvertException("illegal escape(" + firstchar + ") (position = " + this.position + ")");
			}
			value = firstchar - '0';
		}
		for (;;) {
			final char ch = this.nextChar();
			if (ch == 0) {
				break;
			}
			if ((ch >= '0') && (ch <= '9')) {
				value = (value << 3) + (value << 1) + (ch - '0');
			} else if ((ch == '"') || (ch == '\'')) {
			} else if ((ch == ',') || (ch == '}') || (ch == ']') || (ch <= ' ') || (ch == ':')) {
				this.backChar(ch);
				break;
			} else {
				throw new ConvertException("illegal escape(" + ch + ") (position = " + this.position + ")");
			}
		}
		return negative ? -value : value;
	}

	@Override
	public final long readLong() {
		char firstchar = this.nextGoodChar();
		if ((firstchar == '"') || (firstchar == '\'')) {
			firstchar = this.nextChar();
			if ((firstchar == '"') || (firstchar == '\'')) {
				return 0L;
			}
		}
		long value = 0;
		final boolean negative = firstchar == '-';
		if (!negative) {
			if ((firstchar < '0') || (firstchar > '9')) {
				throw new ConvertException("illegal escape(" + firstchar + ") (position = " + this.position + ")");
			}
			value = firstchar - '0';
		}
		for (;;) {
			final char ch = this.nextChar();
			if (ch == 0) {
				break;
			}
			if ((ch >= '0') && (ch <= '9')) {
				value = (value << 3) + (value << 1) + (ch - '0');
			} else if ((ch == '"') || (ch == '\'')) {
			} else if ((ch == ',') || (ch == '}') || (ch == ']') || (ch <= ' ') || (ch == ':')) {
				this.backChar(ch);
				break;
			} else {
				throw new ConvertException("illegal escape(" + ch + ") (position = " + this.position + ")");
			}
		}
		return negative ? -value : value;
	}

	@Override
	public final String readString() {
		return this.readSmallString();
	}

}
