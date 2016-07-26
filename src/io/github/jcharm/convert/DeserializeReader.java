/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

/**
 * 反序列化输入流.
 */
public abstract class DeserializeReader {

	/** 当前对象字段名的游标. */
	protected int fieldIndex;

	/** 数组为null的标识 */
	public static final short SIGN_NULL = -1;

	/** 数组存在的标识. */
	public static final short SIGN_NOLENGTH = -2;

	/**
	 * 读取一个boolean值.
	 *
	 * @return boolean
	 */
	public abstract boolean readBoolean();

	/**
	 * 读取一个byte值.
	 *
	 * @return byte
	 */
	public abstract byte readByte();

	/**
	 * 读取一个char值.
	 *
	 * @return char
	 */
	public abstract char readChar();

	/**
	 * 读取一个double值.
	 *
	 * @return double
	 */
	public abstract double readDouble();

	/**
	 * 读取一个float值.
	 *
	 * @return float
	 */
	public abstract float readFloat();

	/**
	 * 读取一个int值.
	 *
	 * @return int
	 */
	public abstract int readInt();

	/**
	 * 读取一个long值.
	 *
	 * @return long
	 */
	public abstract long readLong();

	/**
	 * 读取一个short值.
	 *
	 * @return short
	 */
	public abstract short readShort();

	/**
	 * 读取一个无转义字符长度不超过255的字符串, 例如枚举值、字段名、类名字符串等.
	 *
	 * @return the string
	 */
	public abstract String readSmallString();

	/**
	 * 读取一个String值.
	 *
	 * @return String
	 */
	public abstract String readString();

	/**
	 * 读取数组开头并返回数组长度.
	 *
	 * @return int
	 */
	public abstract int readArrayBegin();

	/**
	 * 读取数组结尾.
	 */
	public abstract void readArrayEnd();

	/**
	 * 读取Map开头并返回Map的size.
	 *
	 * @return int
	 */
	public abstract int readMapBegin();

	/**
	 * 读取Map的结尾.
	 */
	public abstract void readMapEnd();

	/**
	 * 读取对象开头, 返回null表示对象为null, 返回空字符串表示当前class与返回的class一致, 返回非空字符串表示class是当前class的子类.
	 *
	 * @param clazz Class
	 * @return String
	 */
	public String readObjectBegin(final Class clazz) {
		this.fieldIndex = 0;
		return null;
	}

	/**
	 * 读取对象结尾.
	 *
	 * @param clazz Class
	 */
	public abstract void readObjectEnd(Class clazz);

	/**
	 * 是否还存在下个元素或字段.
	 *
	 * @return boolean
	 */
	public abstract boolean hasNext();

	/**
	 * 跳过值(不包含值前面的字段).
	 */
	public abstract void skipValue();

	/**
	 * 跳过字段与值之间的多余内容, JSON是跳过<code>:</code>符号.
	 */
	public abstract void readBlank();

	/**
	 * 读取反序列化对象类名.
	 *
	 * @return the string
	 */
	public abstract String readClassName();

	/**
	 * 根据字段读取字段对应的DeSerializeMember.
	 *
	 * @param deSerializeMembers DeSerializeMember集合
	 * @return DeSerializeMember
	 */
	public abstract DeSerializeMember readFieldName(DeSerializeMember[] deSerializeMembers);

}
