/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import io.github.jcharm.common.FieldAttribute;

/**
 * 序列化输出流.
 */
public abstract class SerializeWriter {

	/** 当前对象输出字段名之前是否需要分隔符, JSON字段间的分隔符为逗号. */
	protected boolean isComma;

	/**
	 * 输出一个boolean值.
	 *
	 * @param value boolean
	 */
	public abstract void writeBoolean(boolean value);

	/**
	 * 输出一个byte值.
	 *
	 * @param value byte
	 */
	public abstract void writeByte(byte value);

	/**
	 * 输出一个char值.
	 *
	 * @param value char
	 */
	public abstract void writeChar(char value);

	/**
	 * 输出一个double值.
	 *
	 * @param value double
	 */
	public abstract void writeDouble(double value);

	/**
	 * 输出一个float值.
	 *
	 * @param value float
	 */
	public abstract void writeFloat(float value);

	/**
	 * 输出一个int值.
	 *
	 * @param value int
	 */
	public abstract void writeInt(int value);

	/**
	 * 输出一个long值.
	 *
	 * @param value long
	 */
	public abstract void writeLong(long value);

	/**
	 * 输出一个short值.
	 *
	 * @param value short
	 */
	public abstract void writeShort(short value);

	/**
	 * 输出一个无转义字符长度不超过255的字符串, 例如枚举值、字段名、类名字符串等.
	 *
	 * @param value String
	 */
	public abstract void writeSmallString(String value);

	/**
	 * 输出一个String值.
	 *
	 * @param value String
	 */
	public abstract void writeString(String value);

	/**
	 * 输出一个数组前操作.
	 *
	 * @param size int
	 */
	public abstract void writeArrayBegin(int size);

	/**
	 * 输出数组元素间的间隔符.
	 */
	public abstract void writeArrayMark();

	/**
	 * 输出一个数组后的操作.
	 */
	public abstract void writeArrayEnd();

	/**
	 * 输出一个Map前的操作.
	 *
	 * @param size int
	 */
	public abstract void writeMapBegin(int size);

	/**
	 * 输出一个Map中key和value间的间隔符.
	 */
	public abstract void writeMapMark();

	/**
	 * 输出一个Map后的操作.
	 */
	public abstract void writeMapEnd();

	/**
	 * 输出一个对象前的操作, 覆盖此方法必须要先调用父方法super.writeObjectBegin(obj).
	 *
	 * @param obj Object
	 */
	public void writeObjectBegin(final Object obj) {
		this.isComma = false;
	}

	/**
	 * 输出一个对象后的操作.
	 *
	 * @param obj Object
	 */
	public abstract void writeObjectEnd(Object obj);

	/**
	 * 输出一个null值.
	 */
	public abstract void writeNull();

	/**
	 * 输出一个字段名.
	 *
	 * @param fieldAttribute FieldAttribute
	 */
	public abstract void writeFieldName(FieldAttribute fieldAttribute);

	/**
	 * 输入一个类名, JSON是不需要类名的, 但是BSON需要类名.
	 *
	 * @param clazz Class
	 */
	public abstract void writeClassName(Class clazz);

	/**
	 * 序列化是否需要写入类名, JSON不需要, BSON需要.
	 *
	 * @return boolean
	 */
	public abstract boolean needWriteClassName();

	/**
	 * 输出一个为null的对象.
	 */
	public void writeObjectNull() {
		this.writeClassName(null);
		this.writeNull();
	}

	/**
	 * 输出一个对象的某个字段.
	 *
	 * @param serializeMember 序列化成员
	 * @param obj 指定对象
	 */
	public void writeObjectField(final SerializeMember serializeMember, final Object obj) {
		final Object value = serializeMember.getFieldAttribute().getFieldValue(obj);
		if (value == null) {
			return;
		}
		this.writeFieldName(serializeMember.getFieldAttribute());
		serializeMember.getSerializeParser().convertTo(this, value);
		this.isComma = true;
	}

}
