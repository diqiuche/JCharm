/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Field;

import io.github.jcharm.common.FieldAttribute;

/**
 * 序列化成员.
 *
 * @param <W> 序列化输出流
 * @param <C> 声明字段的类
 * @param <F> 字段的数据类型
 */
public final class SerializeMember<W extends SerializeWriter, C, F> implements Comparable<SerializeMember<W, C, F>> {

	/** ASM动态字段映射类. */
	final FieldAttribute<C, F> fieldAttribute;

	/** 序列化解析器. */
	final SerializeParser<W, F> serializeParser;

	/**
	 * 构造函数.
	 *
	 * @param fieldAttribute FieldAttribute
	 * @param serializeParser SerializeParser
	 */
	public SerializeMember(final FieldAttribute<C, F> fieldAttribute, final SerializeParser<W, F> serializeParser) {
		this.fieldAttribute = fieldAttribute;
		this.serializeParser = serializeParser;
	}

	/**
	 * 根据ConvertFactory和字段声明类及字段名称创建SerializeMember.
	 *
	 * @param <W> 序列化输出流
	 * @param <T> 字段声明类
	 * @param <F> 字段数据类型
	 * @param convertFactory ConvertFactory
	 * @param clazz 字段声明类
	 * @param fieldName 字段名称
	 * @return SerializeMember
	 */
	public static <W extends SerializeWriter, T, F> SerializeMember<W, T, F> create(final ConvertFactory convertFactory, final Class<T> clazz, final String fieldName) {
		try {
			// getDeclaredField : 返回一个Field对象, 该对象反映此Class对象所表示的类或接口的指定已声明字段
			final Field field = clazz.getDeclaredField(fieldName);
			// getGenericType : 返回一个Type对象, 它表示此Field对象所表示字段的声明类型
			return new SerializeMember(FieldAttribute.create(field), convertFactory.loadSerializeParser(field.getGenericType()));
		} catch (final Exception e) {
			throw new ConvertException(e);
		}
	}

	/**
	 * 根据ConvertFactory和动态映射类及字段类型创建SerializeMember.
	 *
	 * @param <W> 序列化输出流
	 * @param <C> 声明字段的类
	 * @param <F> 字段数据类型
	 * @param fieldAttribute FieldAttribute
	 * @param convertFactory ConvertFactory
	 * @param filedType 字段类型
	 * @return SerializeMember
	 */
	public static <W extends SerializeWriter, C, F> SerializeMember<W, C, F> create(final FieldAttribute<C, F> fieldAttribute, final ConvertFactory convertFactory, final Class<F> filedType) {
		return new SerializeMember<>(fieldAttribute, convertFactory.loadSerializeParser(filedType));
	}

	/**
	 * 根据字段名称判断是否匹配序列化成员.
	 *
	 * @param fieldName 字段名称
	 * @return boolean
	 */
	public final boolean match(final String fieldName) {
		return this.fieldAttribute.getFieldAliasName().equals(fieldName);
	}

	/**
	 * 获取动态映射类.
	 *
	 * @return FieldAttribute
	 */
	public FieldAttribute<C, F> getFieldAttribute() {
		return this.fieldAttribute;
	}

	/**
	 * 获取序列化解析器.
	 *
	 * @return SerializeParser
	 */
	public SerializeParser<W, F> getSerializeParser() {
		return this.serializeParser;
	}

	@Override
	public final int compareTo(final SerializeMember<W, C, F> o) {
		// 比较此对象与指定对象的顺序, 如果该对象小于、等于或大于指定对象, 则分别返回负整数、零或正整数
		if (o == null) {
			return 1;
		}
		return this.fieldAttribute.getFieldAliasName().compareTo(o.fieldAttribute.getFieldAliasName());
	}

	@Override
	public int hashCode() {
		return this.fieldAttribute.getFieldAliasName().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SerializeMember)) {
			return false;
		}
		final SerializeMember member = (SerializeMember) obj;
		return this.compareTo(member) == 0;
	}

	@Override
	public String toString() {
		return "SerializeMember{" + "fieldAttribute=" + this.fieldAttribute.getFieldAliasName() + ", serializeParser=" + this.serializeParser + "}";
	}

}
