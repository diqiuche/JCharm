/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.ConvertException;
import io.github.jcharm.convert.DeSerializeMember;
import io.github.jcharm.convert.DeserializeReader;

/**
 * JSON反序列化输入流.
 */
public class JsonDeserializeReader extends DeserializeReader {

	/** 位置下标. */
	protected int position = -1;

	private char[] text;

	private int limit;

	/**
	 * 构造函数.
	 */
	public JsonDeserializeReader() {
	}

	/**
	 * 构造函数.
	 *
	 * @param json String
	 */
	public JsonDeserializeReader(final String json) {
		this.setText(json);
	}

	/**
	 * 构造函数.
	 *
	 * @param text char[]
	 */
	public JsonDeserializeReader(final char[] text) {
		this.setText(text, 0, text.length);
	}

	/**
	 * 构造函数.
	 *
	 * @param text char[]
	 * @param start int
	 * @param len int
	 */
	public JsonDeserializeReader(final char[] text, final int start, final int len) {
		this.setText(text, start, len);
	}

	/**
	 * 创建一个存放JsonDeserializeReader的对象池.
	 *
	 * @param max 对象池存放对象的最大值
	 * @return ObjectPool
	 */
	public static ObjectPool<JsonDeserializeReader> createPool(final int max) {
		return new ObjectPool<>(max, (final Object... params) -> new JsonDeserializeReader(), null, (final JsonDeserializeReader t) -> t.recycle());
	}

	/**
	 * 是否允重复利用对象池中存储的对象.
	 *
	 * @return boolean
	 */
	protected boolean recycle() {
		this.position = -1;
		this.limit = -1;
		this.text = null;
		return true;
	}

	/**
	 * 将指定范围的字符数组赋值给内容字符数组.
	 *
	 * @param text char[]
	 * @param start int
	 * @param len int
	 */
	public final void setText(final char[] text, final int start, final int len) {
		this.text = text;
		this.position = start - 1;
		this.limit = (start + len) - 1;
	}

	/**
	 * 将字符数组赋值给内容字符数组.
	 *
	 * @param text char[]
	 */
	public final void setText(final char[] text) {
		this.setText(text, 0, text.length);
	}

	/**
	 * 将字符串赋值给内容字符数组.
	 *
	 * @param text char[]
	 */
	public final void setText(final String text) {
		this.setText(text.toCharArray());
	}

	/**
	 * 读取下一个字符, 不跳过空白字符.
	 *
	 * @return 空白字符或有效字符
	 */
	protected char nextChar() {
		return this.text[++this.position];
	}

	/**
	 * 跳过空白字符, 返回一个非空白字符.
	 *
	 * @return 有效字符
	 */
	protected char nextGoodChar() {
		char c = this.nextChar();
		if (c > ' ') {
			return c;
		}
		for (;;) {
			c = this.nextChar();
			if (c > ' ') {
				return c;
			}
		}
	}

	/**
	 * 回退最后读取的字符.
	 *
	 * @param ch 后退的字符
	 */
	protected void backChar(final char ch) {
		this.position--;
	}

	/**
	 * 找到指定的属性值 例如: {id : 1, data : { name : 'a', items : [1,2,3]}} seek('data.items') 直接跳转到 [1,2,3].
	 *
	 * @param key 指定的属性名
	 */
	public final void seek(final String key) {
		if ((key == null) || (key.length() < 1)) {
			return;
		}
		final String[] keys = key.split("\\.");// 根据点符号分隔属性名
		this.nextGoodChar();
		for (final String key1 : keys) {
			while (this.hasNext()) {
				final String field = this.readSmallString();
				this.readBlank();// 判断下一个非空白字符是否:
				if (key1.equals(field)) {
					break;
				}
				this.skipValue();// 跳过属性的值
			}
		}
	}

	@Override
	public final boolean readBoolean() {
		return "true".equalsIgnoreCase(this.readSmallString());
	}

	@Override
	public final byte readByte() {
		return (byte) this.readInt();
	}

	@Override
	public final char readChar() {
		return (char) this.readInt();
	}

	@Override
	public final double readDouble() {
		final String chars = this.readSmallString();
		if ((chars == null) || chars.isEmpty()) {
			return 0.0;
		}
		return Double.parseDouble(chars);
	}

	@Override
	public final float readFloat() {
		final String chars = this.readSmallString();
		if ((chars == null) || chars.isEmpty()) {
			return 0.f;
		}
		return Float.parseFloat(chars);
	}

	@Override
	public int readInt() {
		final char[] text0 = this.text;
		final int eof = this.limit;
		int currpos = this.position;
		char firstchar = text0[++currpos];
		if (firstchar <= ' ') {
			for (;;) {
				firstchar = text0[++currpos];
				if (firstchar > ' ') {
					break;
				}
			}
		}
		if ((firstchar == '"') || (firstchar == '\'')) {
			firstchar = text0[++currpos];
			if ((firstchar == '"') || (firstchar == '\'')) {
				this.position = currpos;
				return 0;
			}
		}
		int value = 0;
		final boolean negative = firstchar == '-';
		if (!negative) {
			if ((firstchar < '0') || (firstchar > '9')) {
				throw new ConvertException("illegal escape(" + firstchar + ") (position = " + currpos + ") in (" + new String(this.text) + ")");
			}
			value = firstchar - '0';
		}
		for (;;) {
			if (currpos == eof) {
				break;
			}
			final char ch = text0[++currpos];
			final int val = JsonDeserializeReader.DIGITS[ch];
			if (val == -3) {
				break;
			}
			if (val == -1) {
				throw new ConvertException("illegal escape(" + ch + ") (position = " + currpos + ") but '" + ch + "' in (" + new String(this.text) + ")");
			}
			if (val != -2) {
				value = (value * 10) + val;
			}
		}
		this.position = currpos - 1;
		return negative ? -value : value;
	}

	@Override
	public long readLong() {
		final char[] text0 = this.text;
		final int eof = this.limit;
		int currpos = this.position;
		char firstchar = text0[++currpos];
		if (firstchar <= ' ') {
			for (;;) {
				firstchar = text0[++currpos];
				if (firstchar > ' ') {
					break;
				}
			}
		}
		if ((firstchar == '"') || (firstchar == '\'')) {
			firstchar = text0[++currpos];
			if ((firstchar == '"') || (firstchar == '\'')) {
				this.position = currpos;
				return 0L;
			}
		}
		long value = 0;
		final boolean negative = firstchar == '-';
		if (!negative) {
			if ((firstchar < '0') || (firstchar > '9')) {
				throw new ConvertException("illegal escape(" + firstchar + ") (position = " + currpos + ") in (" + new String(this.text) + ")");
			}
			value = firstchar - '0';
		}
		for (;;) {
			if (currpos == eof) {
				break;
			}
			final char ch = text0[++currpos];
			final int val = JsonDeserializeReader.DIGITS[ch];
			if (val == -3) {
				break;
			}
			if (val == -1) {
				throw new ConvertException("illegal escape(" + ch + ") (position = " + currpos + ") but '" + ch + "' in (" + new String(this.text) + ")");
			}
			if (val != -2) {
				value = (value * 10) + val;
			}
		}
		this.position = currpos - 1;
		return negative ? -value : value;
	}

	@Override
	public final short readShort() {
		return (short) this.readInt();
	}

	@Override
	public String readSmallString() {
		final int eof = this.limit;
		if (this.position == eof) {
			return null;
		}
		final char[] text0 = this.text;
		int currpos = this.position;
		char ch = text0[++currpos];
		if (ch <= ' ') {
			for (;;) {
				ch = text0[++currpos];
				if (ch > ' ') {
					break;
				}
			}
		}
		if ((ch == '"') || (ch == '\'')) {
			final char quote = ch;
			final int start = currpos + 1;
			for (;;) {
				ch = text0[++currpos];
				if (ch == '\\') {
					this.position = currpos - 1;
					return this.readEscapeValue(quote, start);
				} else if (ch == quote) {
					break;
				}
			}
			this.position = currpos;
			final char[] chs = new char[currpos - start];
			System.arraycopy(text0, start, chs, 0, chs.length);
			return new String(chs);
		} else {
			final int start = currpos;
			for (;;) {
				if (currpos == eof) {
					break;
				}
				ch = text0[++currpos];
				if ((ch == ',') || (ch == ']') || (ch == '}') || (ch <= ' ') || (ch == ':')) {
					break;
				}
			}
			final int len = currpos - start;
			if (len < 1) {
				this.position = currpos;
				return String.valueOf(ch);
			}
			this.position = currpos - 1;
			if ((len == 4) && (text0[start] == 'n') && (text0[start + 1] == 'u') && (text0[start + 2] == 'l') && (text0[start + 3] == 'l')) {
				return null;
			}
			return new String(text0, start, len);
		}
	}

	@Override
	public String readString() {
		final char[] text0 = this.text;
		int currpos = this.position;
		char expected = text0[++currpos];
		if (expected <= ' ') {
			for (;;) {
				expected = text0[++currpos];
				if (expected > ' ') {
					break;
				}
			}
		}
		if ((expected != '"') && (expected != '\'')) {
			if ((expected == 'n') && (text0.length > (currpos + 3))) {
				if ((text0[++currpos] == 'u') && (text0[++currpos] == 'l') && (text0[++currpos] == 'l')) {
					this.position = currpos;
					if (text0.length > (currpos + 4)) {
						final char ch = text0[currpos + 1];
						if ((ch == ',') || (ch <= ' ') || (ch == '}') || (ch == ']') || (ch == ':')) {
							return null;
						}
					} else {
						return null;
					}
				}
			} else {
				final int start = currpos;
				for (;;) {
					final char ch = text0[currpos];
					if ((ch == ',') || (ch <= ' ') || (ch == '}') || (ch == ']') || (ch == ':')) {
						break;
					}
					currpos++;
				}
				if (currpos == start) {
					throw new ConvertException("expected a string after a key but '" + text0[this.position] + "' (position = " + this.position + ") in (" + new String(this.text) + ")");
				}
				this.position = currpos - 1;
				return new String(text0, start, currpos - start);
			}
			this.position = currpos;
			throw new ConvertException("expected a ':' after a key but '" + text0[this.position] + "' (position = " + this.position + ") in (" + new String(this.text) + ")");
		}
		final int start = ++currpos;
		for (;;) {
			final char ch = text0[currpos];
			if (ch == expected) {
				break;
			} else if (ch == '\\') {
				this.position = currpos - 1;
				return this.readEscapeValue(expected, start);
			}
			currpos++;
		}
		this.position = currpos;
		return new String(text0, start, currpos - start);
	}

	@Override
	public int readArrayBegin() {
		char ch = this.text[++this.position];
		if (ch == '[') {
			return DeserializeReader.SIGN_NOLENGTH;
		}
		if (ch == '{') {
			return DeserializeReader.SIGN_NOLENGTH;
		}
		if (ch <= ' ') {
			for (;;) {
				ch = this.text[++this.position];
				if (ch > ' ') {
					break;
				}
			}
			if (ch == '[') {
				return DeserializeReader.SIGN_NOLENGTH;
			}
			if (ch == '{') {
				return DeserializeReader.SIGN_NOLENGTH;
			}
		}
		if ((ch == 'n') && (this.text[++this.position] == 'u') && (this.text[++this.position] == 'l') && (this.text[++this.position] == 'l')) {
			return DeserializeReader.SIGN_NULL;
		}
		if ((ch == 'N') && (this.text[++this.position] == 'U') && (this.text[++this.position] == 'L') && (this.text[++this.position] == 'L')) {
			return DeserializeReader.SIGN_NULL;
		}
		throw new ConvertException("a json array text must begin with '[' (position = " + this.position + ") but '" + ch + "' in (" + new String(this.text) + ")");
	}

	@Override
	public final void readArrayEnd() {
	}

	@Override
	public final int readMapBegin() {
		return this.readArrayBegin();
	}

	@Override
	public final void readMapEnd() {
	}

	@Override
	public String readObjectBegin(final Class clazz) {
		this.fieldIndex = 0; // 必须要重置为0
		char ch = this.text[++this.position];
		if (ch == '{') {
			return "";
		}
		if (ch <= ' ') {
			for (;;) {
				ch = this.text[++this.position];
				if (ch > ' ') {
					break;
				}
			}
			if (ch == '{') {
				return "";
			}
		}
		if ((ch == 'n') && (this.text[++this.position] == 'u') && (this.text[++this.position] == 'l') && (this.text[++this.position] == 'l')) {
			return null;
		}
		if ((ch == 'N') && (this.text[++this.position] == 'U') && (this.text[++this.position] == 'L') && (this.text[++this.position] == 'L')) {
			return null;
		}
		throw new ConvertException("a json object text must begin with '{' (position = " + this.position + ") but '" + ch + "' in (" + new String(this.text) + ")");
	}

	@Override
	public final void readObjectEnd(final Class clazz) {
	}

	@Override
	public boolean hasNext() {
		char ch = this.text[++this.position];
		if (ch == ',') {
			return true;
		}
		if ((ch == '}') || (ch == ']')) {
			return false;
		}
		if (ch <= ' ') {
			for (;;) {
				ch = this.text[++this.position];
				if (ch > ' ') {
					break;
				}
			}
			if (ch == ',') {
				return true;
			}
			if ((ch == '}') || (ch == ']')) {
				return false;
			}
		}
		this.position--;
		return true;
	}

	@Override
	public final void skipValue() {
		final char ch = this.nextGoodChar();
		switch (ch) {
		case '"':
		case '\'':
			this.backChar(ch);
			this.readString();
			break;
		case '{':
			while (this.hasNext()) {
				this.readSmallString();
				this.readBlank();
				this.skipValue();
			}
			break;
		case '[':
			while (this.hasNext()) {
				this.skipValue();
			}
			break;
		default:
			char c;
			for (;;) {
				c = this.nextChar();
				if (c <= ' ') {
					return;
				}
				if ((c == '}') || (c == ']') || (c == ',') || (c == ':')) {
					this.backChar(c);
					return;
				}
			}
		}
	}

	@Override
	public void readBlank() {
		char ch = this.text[++this.position];
		if (ch == ':') {
			return;
		}
		if (ch <= ' ') {
			for (;;) {
				ch = this.text[++this.position];
				if (ch > ' ') {
					break;
				}
			}
			if (ch == ':') {
				return;
			}
		}
		throw new ConvertException("'" + new String(this.text) + "'expected a ':' but '" + ch + "'(position = " + this.position + ") in (" + new String(this.text) + ")");
	}

	@Override
	public final String readClassName() {
		return null;
	}

	@Override
	public final DeSerializeMember readFieldName(final DeSerializeMember[] deSerializeMembers) {
		final String exceptedfield = this.readSmallString();
		final int len = deSerializeMembers.length;
		if (this.fieldIndex >= len) {
			this.fieldIndex = 0;
		}
		for (int k = this.fieldIndex; k < len; k++) {
			if (exceptedfield.equals(deSerializeMembers[k].getFieldAttribute().getFieldAliasName())) {
				this.fieldIndex = k;
				return deSerializeMembers[k];
			}
		}
		for (int k = 0; k < this.fieldIndex; k++) {
			if (exceptedfield.equals(deSerializeMembers[k].getFieldAttribute().getFieldAliasName())) {
				this.fieldIndex = k;
				return deSerializeMembers[k];
			}
		}
		return null;
	}

	private String readEscapeValue(final char expected, final int start) {
		final StringBuilder array = new StringBuilder();
		final char[] text0 = this.text;
		int pos = this.position;
		array.append(text0, start, (pos + 1) - start);
		char c;
		for (;;) {
			c = text0[++pos];
			if (c == expected) {
				this.position = pos;
				return array.toString();
			} else if (c == '\\') {
				c = text0[++pos];
				switch (c) {
				case '"':
				case '\'':
				case '\\':
				case '/':
					array.append(c);
					break;
				case 'n':
					array.append('\n');
					break;
				case 'r':
					array.append('\r');
					break;
				case 'u':
					array.append((char) Integer.parseInt(new String(new char[] { text0[++pos], text0[++pos], text0[++pos], text0[++pos] }), 16));
					break;
				case 't':
					array.append('\t');
					break;
				case 'b':
					array.append('\b');
					break;
				case 'f':
					array.append('\f');
					break;
				default:
					this.position = pos;
					throw new ConvertException("illegal escape(" + c + ") (position = " + this.position + ") in (" + new String(this.text) + ")");
				}
			} else {
				array.append(c);
			}
		}
	}

	/** The Constant DIGITS. */
	final static int[] DIGITS = new int[255];

	static {
		for (int i = 0; i < JsonDeserializeReader.DIGITS.length; i++) {
			JsonDeserializeReader.DIGITS[i] = -1; // -1 错误
		}
		for (int i = '0'; i <= '9'; i++) {
			JsonDeserializeReader.DIGITS[i] = i - '0';
		}
		for (int i = 'a'; i <= 'f'; i++) {
			JsonDeserializeReader.DIGITS[i] = (i - 'a') + 10;
		}
		for (int i = 'A'; i <= 'F'; i++) {
			JsonDeserializeReader.DIGITS[i] = (i - 'A') + 10;
		}
		JsonDeserializeReader.DIGITS['"'] = JsonDeserializeReader.DIGITS['\''] = -2; // -2 跳过
		JsonDeserializeReader.DIGITS[','] = JsonDeserializeReader.DIGITS['}'] = JsonDeserializeReader.DIGITS[']'] = JsonDeserializeReader.DIGITS[' '] = JsonDeserializeReader.DIGITS['\t'] = JsonDeserializeReader.DIGITS['\r'] = JsonDeserializeReader.DIGITS['\n'] = JsonDeserializeReader.DIGITS[':'] = -3; // -3退出
	}

}
