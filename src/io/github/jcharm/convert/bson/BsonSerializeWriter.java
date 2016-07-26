/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import io.github.jcharm.common.CommonUtils;
import io.github.jcharm.common.ConstructCreator;
import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.ConvertException;
import io.github.jcharm.convert.SerializeWriter;

/**
 * BSON序列化输出流.
 */
public class BsonSerializeWriter extends SerializeWriter {

	private static final int defaultSize = 1024;

	private static final short SIGN_OBJECTB = (short) 0xBB;

	private static final short SIGN_OBJECTE = (short) 0xEE;

	private static final byte SIGN_NONEXT = 0;

	private static final byte SIGN_HASNEXT = 1;

	private static final short SIGN_NULL = -1;

	private byte[] content;

	/** 字节内容数组大小. */
	protected int count;

	/**
	 * 构造函数.
	 *
	 * @param bs byte[]
	 */
	protected BsonSerializeWriter(final byte[] bs) {
		this.content = bs;
	}

	/**
	 * 构造函数.
	 *
	 * @param size int
	 */
	public BsonSerializeWriter(final int size) {
		this.content = new byte[size > 128 ? size : 128];
	}

	/**
	 * 构造函数.
	 */
	public BsonSerializeWriter() {
		this(BsonSerializeWriter.defaultSize);
	}

	/**
	 * 创建一个存放BsonSerializeWriter的对象池.
	 *
	 * @param max 对象池存放对象的最大值
	 * @return ObjectPool
	 */
	public static ObjectPool<BsonSerializeWriter> createPool(final int max) {
		return new ObjectPool(max, new ConstructCreator<BsonSerializeWriter>() {

			@Override
			public BsonSerializeWriter construct(final Object... params) {
				return new BsonSerializeWriter();
			}
		}, null, new Predicate<BsonSerializeWriter>() {

			@Override
			public boolean test(final BsonSerializeWriter t) {
				return t.recycle();
			}
		});
	}

	/**
	 * 输出字节数组.
	 *
	 * @return byte[]
	 */
	public byte[] toArray() {
		if (this.count == this.content.length) {
			return this.content;
		}
		final byte[] newdata = new byte[this.count];
		System.arraycopy(this.content, 0, newdata, 0, this.count);
		return newdata;
	}

	/**
	 * 将字节内容数组包装到缓存区.
	 *
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] toBuffers() {
		// wrap : 将 byte 数组包装到缓冲区中
		return new ByteBuffer[] { ByteBuffer.wrap(this.content, 0, this.count) };
	}

	/**
	 * 扩充指定长度的缓冲区.
	 *
	 * @param len int
	 * @return int
	 */
	protected int expand(final int len) {
		final int newcount = this.count + len;
		if (newcount <= this.content.length) {
			return 0;
		}
		final byte[] newdata = new byte[Math.max((this.content.length * 3) / 2, newcount)];
		System.arraycopy(this.content, 0, newdata, 0, this.count);
		this.content = newdata;
		return 0;
	}

	/**
	 * 将一个字节输出到字节内容数组中.
	 *
	 * @param bt byte
	 */
	public void writeTo(final byte bt) {
		this.expand(1);
		this.content[this.count++] = bt;
	}

	/**
	 * 将字节数值中指定位置及长度的数组输出到字节内容数组中.
	 *
	 * @param bts byte[]
	 * @param start int
	 * @param len int
	 */
	public void writeTo(final byte[] bts, final int start, final int len) {
		this.expand(len);
		System.arraycopy(bts, start, this.content, this.count, len);
		this.count += len;
	}

	/**
	 * 将字节数组输出到字节内容数组中.
	 *
	 * @param bts bytes
	 */
	public final void writeTo(final byte... bts) {
		this.writeTo(bts, 0, bts.length);
	}

	/**
	 * 是否允重复利用对象池中存储的对象.
	 *
	 * @return boolean
	 */
	protected boolean recycle() {
		this.count = 0;
		if (this.content.length > BsonSerializeWriter.defaultSize) {
			this.content = new byte[BsonSerializeWriter.defaultSize];
		}
		return true;
	}

	@Override
	public void writeBoolean(final boolean value) {
		this.writeTo(value ? (byte) 1 : (byte) 0);
	}

	@Override
	public void writeByte(final byte value) {
		this.writeTo(value);
	}

	@Override
	public void writeChar(final char value) {
		this.writeTo((byte) ((value & 0xFF00) >> 8), (byte) (value & 0xFF));
	}

	@Override
	public void writeDouble(final double value) {
		this.writeLong(Double.doubleToLongBits(value));
	}

	@Override
	public void writeFloat(final float value) {
		this.writeInt(Float.floatToIntBits(value));
	}

	@Override
	public void writeInt(final int value) {
		this.writeTo((byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value);
	}

	@Override
	public void writeLong(final long value) {
		this.writeTo((byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value);
	}

	@Override
	public void writeShort(final short value) {
		this.writeTo((byte) (value >> 8), (byte) value);
	}

	@Override
	public void writeSmallString(final String value) {
		if (value.isEmpty()) {
			this.writeTo((byte) 0);
			return;
		}
		final char[] chars = value.toCharArray();
		if (chars.length > 255) {
			throw new ConvertException("'" + value + "' has  very long length");
		}
		final byte[] bytes = new byte[chars.length + 1];
		bytes[0] = (byte) chars.length;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] > Byte.MAX_VALUE) {
				throw new ConvertException("'" + value + "'  has double-word");
			}
			bytes[i + 1] = (byte) chars[i];
		}
		this.writeTo(bytes);
	}

	@Override
	public void writeString(final String value) {
		if (value == null) {
			this.writeInt(BsonSerializeWriter.SIGN_NULL);
			return;
		} else if (value.isEmpty()) {
			this.writeInt(0);
			return;
		}
		final char[] chars = value.toCharArray();
		final byte[] bytes = CommonUtils.encodeUTF8ToBytes(chars, 0, chars.length);
		this.writeInt(bytes.length);
		this.writeTo(bytes);
	}

	@Override
	public void writeArrayBegin(final int size) {
		this.writeInt(size);
	}

	@Override
	public void writeArrayMark() {
	}

	@Override
	public void writeArrayEnd() {
	}

	@Override
	public void writeMapBegin(final int size) {
		this.writeArrayBegin(size);
	}

	@Override
	public void writeMapMark() {
	}

	@Override
	public void writeMapEnd() {
	}

	@Override
	public void writeObjectBegin(final Object obj) {
		super.writeObjectBegin(obj);
		this.writeSmallString("");
		this.writeShort(BsonSerializeWriter.SIGN_OBJECTB);
	}

	@Override
	public void writeObjectEnd(final Object obj) {
		this.writeByte(BsonSerializeWriter.SIGN_NONEXT);
		this.writeShort(BsonSerializeWriter.SIGN_OBJECTE);
	}

	@Override
	public void writeNull() {
		this.writeShort(BsonSerializeWriter.SIGN_NULL);
	}

	@Override
	public void writeFieldName(final FieldAttribute fieldAttribute) {
		this.writeByte(BsonSerializeWriter.SIGN_HASNEXT);
		this.writeSmallString(fieldAttribute.getFieldAliasName());
		byte typeval = 127; // 字段的类型值
		final Class type = fieldAttribute.getFieldType();
		if ((type == boolean.class) || (type == Boolean.class)) {
			typeval = 1;
		} else if ((type == byte.class) || (type == Byte.class)) {
			typeval = 2;
		} else if ((type == short.class) || (type == Short.class)) {
			typeval = 3;
		} else if ((type == char.class) || (type == Character.class)) {
			typeval = 4;
		} else if ((type == int.class) || (type == Integer.class)) {
			typeval = 5;
		} else if ((type == long.class) || (type == Long.class)) {
			typeval = 6;
		} else if ((type == float.class) || (type == Float.class)) {
			typeval = 7;
		} else if ((type == double.class) || (type == Double.class)) {
			typeval = 8;
		} else if (type == String.class) {
			typeval = 9;
		} else if ((type == boolean[].class) || (type == Boolean[].class)) {
			typeval = 101;
		} else if ((type == byte[].class) || (type == Byte[].class)) {
			typeval = 102;
		} else if ((type == short[].class) || (type == Short[].class)) {
			typeval = 103;
		} else if ((type == char[].class) || (type == Character[].class)) {
			typeval = 104;
		} else if ((type == int[].class) || (type == Integer[].class)) {
			typeval = 105;
		} else if ((type == long[].class) || (type == Long[].class)) {
			typeval = 106;
		} else if ((type == float[].class) || (type == Float[].class)) {
			typeval = 107;
		} else if ((type == double[].class) || (type == Double[].class)) {
			typeval = 108;
		} else if (type == String[].class) {
			typeval = 109;
		}
		this.writeByte(typeval);
	}

	@Override
	public void writeClassName(final Class clazz) {
		this.writeSmallString(clazz == null ? "" : clazz.getName());
	}

	@Override
	public boolean needWriteClassName() {
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[count=" + this.count + "]";
	}

}
