/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.nio.ByteBuffer;

import io.github.jcharm.common.CommonUtils;
import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.SerializeWriter;

/**
 * JSON序列化输出流.
 */
public class JsonSerializeWriter extends SerializeWriter {

	private static final int defaultSize = 1024;

	private int count;

	private char[] content;

	/**
	 * 构造函数.
	 */
	public JsonSerializeWriter() {
		this(JsonSerializeWriter.defaultSize);
	}

	/**
	 * 构造函数.
	 *
	 * @param size 初始化字符数组大小
	 */
	public JsonSerializeWriter(final int size) {
		this.content = new char[size > 128 ? size : 128];
	}

	/**
	 * 创建一个存放JsonSerializeWriter的对象池.
	 *
	 * @param max 对象池存放对象的最大值
	 * @return ObjectPool
	 */
	public static ObjectPool<JsonSerializeWriter> createPool(final int max) {
		return new ObjectPool<>(max, (final Object... params) -> new JsonSerializeWriter(), null, (final JsonSerializeWriter t) -> t.recycle());
	}

	/**
	 * 是否允重复利用对象池中存储的对象.
	 *
	 * @return boolean
	 */
	protected boolean recycle() {
		this.count = 0;
		if (this.content.length > JsonSerializeWriter.defaultSize) {
			this.content = new char[JsonSerializeWriter.defaultSize];
		}
		return true;
	}

	private char[] expand(final int len) {// 扩展现有字符数组的大小
		final int newcount = this.count + len;
		if (newcount <= this.content.length) {
			return this.content;
		}
		final char[] newdata = new char[Math.max((this.content.length * 3) / 2, newcount)];
		// arraycopy : 从指定源数组中复制一个数组, 复制从指定的位置开始, 到目标数组的指定位置结束
		System.arraycopy(this.content, 0, newdata, 0, this.count);
		this.content = newdata;
		return newdata;
	}

	/**
	 * 将一个字符输出到内容字符数组中.
	 *
	 * @param ch the ch
	 */
	public void writeTo(final char ch) {// 只能是 0 - 127 的字符
		this.expand(1);
		this.content[this.count++] = ch;
	}

	/**
	 * 将字符数组中指定位置和大小的数组输出到内容字符数组中.
	 *
	 * @param chs char[]
	 * @param start int
	 * @param len int
	 */
	public void writeTo(final char[] chs, final int start, final int len) { // 只能是 0 - 127 的字符
		this.expand(len);// 扩容了内容字符数组长度
		// arraycopy : 从指定源数组中复制一个数组, 复制从指定的位置开始, 到目标数组的指定位置结束
		System.arraycopy(chs, start, this.content, this.count, len);
		this.count += len;
	}

	/**
	 * 该String值不能为null且不会进行转义, 只用于不含需要转义字符的字符串.
	 *
	 * @param quote 是否加双引号
	 * @param value 非null且不含需要转义的字符的String值
	 */
	public void writeTo(final boolean quote, final String value) {
		final int len = value.length();
		this.expand(len + (quote ? 2 : 0));
		if (quote) {
			this.content[this.count++] = '"';
		}
		// getChars : 将字符从此字符串复制到目标字符数组.
		value.getChars(0, len, this.content, this.count);
		this.count += len;
		if (quote) {
			this.content[this.count++] = '"';
		}
	}

	/**
	 * 将字符数组中指定位置和大小的数组输出到内容字符数组中.
	 *
	 * @param chs chars
	 */
	public final void writeTo(final char... chs) { // 只能是 0 - 127 的字符
		this.writeTo(chs, 0, chs.length);
	}

	/**
	 * 将内容字符数组包装到缓冲区.
	 *
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] toBuffers() {
		// wrap : 将 byte 数组包装到缓冲区中
		return new ByteBuffer[] { ByteBuffer.wrap(CommonUtils.encodeUTF8ToBytes(this.content, 0, this.count)) };
	}

	/**
	 * 返回内容字符数组大小.
	 *
	 * @return int
	 */
	public int getCount() {
		return this.count;
	}

	@Override
	public final void writeBoolean(final boolean value) {
		this.writeTo(value ? "true".toCharArray() : "false".toCharArray());
	}

	@Override
	public final void writeByte(final byte value) {
		this.writeInt(value);
	}

	@Override
	public final void writeChar(final char value) {
		this.writeInt(value);
	}

	@Override
	public final void writeDouble(final double value) {
		this.writeTo(false, String.valueOf(value));
	}

	@Override
	public final void writeFloat(final float value) {
		this.writeTo(false, String.valueOf(value));
	}

	@Override
	public void writeInt(int value) {
		final char sign = value >= 0 ? 0 : '-';
		if (value < 0) {
			value = -value;
		}
		int size;
		for (int i = 0;; i++) {
			if (value <= JsonSerializeWriter.SIZETABLE[i]) {
				size = i + 1;
				break;
			}
		}
		if (sign != 0) {
			size++; // 负数
		}
		this.expand(size);
		int q, r;
		int charPos = this.count + size;
		while (value >= 65536) {
			q = value / 100;
			r = value - ((q << 6) + (q << 5) + (q << 2));
			value = q;
			this.content[--charPos] = JsonSerializeWriter.DIGITONES[r];
			this.content[--charPos] = JsonSerializeWriter.DIGITTENS[r];
		}
		for (;;) {
			q = (value * 52429) >>> (16 + 3);
			r = value - ((q << 3) + (q << 1));
			this.content[--charPos] = JsonSerializeWriter.DIGITS[r];
			value = q;
			if (value == 0) {
				break;
			}
		}
		if (sign != 0) {
			this.content[--charPos] = sign;
		}
		this.count += size;
	}

	@Override
	public void writeLong(long value) {
		final char sign = value >= 0 ? 0 : '-';
		if (value < 0) {
			value = -value;
		}
		int size = 19;
		long p = 10;
		for (int i = 1; i < 19; i++) {
			if (value < p) {
				size = i;
				break;
			}
			p = 10 * p;
		}
		if (sign != 0) {
			size++; // 负数
		}
		this.expand(size);
		long q;
		int r;
		int charPos = this.count + size;
		while (value > Integer.MAX_VALUE) {
			q = value / 100;
			r = (int) (value - ((q << 6) + (q << 5) + (q << 2)));
			value = q;
			this.content[--charPos] = JsonSerializeWriter.DIGITONES[r];
			this.content[--charPos] = JsonSerializeWriter.DIGITTENS[r];
		}
		int q2;
		int i2 = (int) value;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			this.content[--charPos] = JsonSerializeWriter.DIGITONES[r];
			this.content[--charPos] = JsonSerializeWriter.DIGITTENS[r];
		}
		for (;;) {
			q2 = (i2 * 52429) >>> (16 + 3);
			r = i2 - ((q2 << 3) + (q2 << 1));
			this.content[--charPos] = JsonSerializeWriter.DIGITS[r];
			i2 = q2;
			if (i2 == 0) {
				break;
			}
		}
		if (sign != 0) {
			this.content[--charPos] = sign;
		}
		this.count += size;
	}

	@Override
	public final void writeShort(final short value) {
		this.writeInt(value);
	}

	@Override
	public final void writeSmallString(final String value) {
		this.writeTo(true, value);
	}

	@Override
	public void writeString(final String value) {
		if (value == null) {
			this.writeNull();
			return;
		}
		this.expand((value.length() * 2) + 2);
		this.content[this.count++] = '"';
		for (final char ch : value.toCharArray()) {
			switch (ch) {
			case '\n':
				this.content[this.count++] = '\\';
				this.content[this.count++] = 'n';
				break;
			case '\r':
				this.content[this.count++] = '\\';
				this.content[this.count++] = 'r';
				break;
			case '\t':
				this.content[this.count++] = '\\';
				this.content[this.count++] = 't';
				break;
			case '\\':
				this.content[this.count++] = '\\';
				this.content[this.count++] = ch;
				break;
			case '"':
				this.content[this.count++] = '\\';
				this.content[this.count++] = ch;
				break;
			default:
				this.content[this.count++] = ch;
				break;
			}
		}
		this.content[this.count++] = '"';
	}

	@Override
	public final void writeArrayBegin(final int size) {
		this.writeTo('[');
	}

	@Override
	public final void writeArrayMark() {
		this.writeTo(',');
	}

	@Override
	public final void writeArrayEnd() {
		this.writeTo(']');
	}

	@Override
	public final void writeMapBegin(final int size) {
		this.writeTo('{');
	}

	@Override
	public final void writeMapMark() {
		this.writeTo(':');
	}

	@Override
	public final void writeMapEnd() {
		this.writeTo('}');
	}

	@Override
	public final void writeObjectBegin(final Object obj) {
		super.writeObjectBegin(obj);
		this.writeTo('{');
	}

	@Override
	public final void writeObjectEnd(final Object obj) {
		this.writeTo('}');
	}

	@Override
	public final void writeNull() {
		this.writeTo('n', 'u', 'l', 'l');
	}

	@Override
	public final void writeFieldName(final FieldAttribute fieldAttribute) {
		if (this.isComma) {// 添加字段分隔符
			this.writeTo(',');
		}
		this.writeTo(true, fieldAttribute.getFieldAliasName());
		this.writeTo(':');
	}

	@Override
	public final void writeClassName(final Class clazz) {
	}

	@Override
	public final boolean needWriteClassName() {
		return false;
	}

	@Override
	public String toString() {
		return new String(this.content, 0, this.count);
	}

	final static char[] DIGITTENS = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5',
			'5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9' };

	final static char[] DIGITONES = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
			'2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	final static int[] SIZETABLE = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

}
