/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Field;

import io.github.jcharm.common.FieldAttribute;

/**
 * 反序列化成员.
 *
 * @param <R> 反序列化输入流
 * @param <C> 声明字段的类
 * @param <F> 字段的数据类型
 */
public final class DeSerializeMember<R extends DeserializeReader, C, F> implements Comparable<DeSerializeMember<R, C, F>> {

	/** ASM动态字段映射类. */
	protected final FieldAttribute<C, F> fieldAttribute;

	/** 反序列化解析器. */
	protected DeSerializeParser<R, F> deSerializeParser;

	/**
	 * 构造函数.
	 *
	 * @param fieldAttribute FieldAttribute
	 * @param deSerializeParser DeSerializeParser
	 */
	public DeSerializeMember(final FieldAttribute<C, F> fieldAttribute, final DeSerializeParser<R, F> deSerializeParser) {
		this.fieldAttribute = fieldAttribute;
		this.deSerializeParser = deSerializeParser;
	}

	/**
	 * 根据ConvertFactory和声明字段的类及字段名称创建DeSerializeMember.
	 *
	 * @param <R> 反序列化输入流
	 * @param <T> 声明字段类
	 * @param <F> 字段的数据类型
	 * @param convertFactory ConvertFactory
	 * @param clazz 声明字段的类
	 * @param fieldName 字段名称
	 * @return DeSerializeMember
	 */
	public static <R extends DeserializeReader, T, F> DeSerializeMember<R, T, F> create(final ConvertFactory convertFactory, final Class<T> clazz, final String fieldName) {
		try {
			// getDeclaredField : 返回一个Field对象, 该对象反映此Class对象所表示的类或接口的指定已声明字段
			final Field field = clazz.getDeclaredField(fieldName);
			// getGenericType : 返回一个Type对象, 它表示此Field对象所表示字段的声明类型
			return new DeSerializeMember(FieldAttribute.create(field), convertFactory.loadDeSerializeParser(field.getGenericType()));
		} catch (final Exception e) {
			throw new ConvertException(e);
		}
	}

	/**
	 * 根据ConvertFactory和动态映射类及字段类型创建DeSerializeMember.
	 *
	 * @param <R> 反序列化输入流
	 * @param <C> 声明字段类
	 * @param <F> 字段的数据类型
	 * @param fieldAttribute FieldAttribute
	 * @param convertFactory ConvertFactory
	 * @param fieldType 字段类型
	 * @return DeSerializeMember
	 */
	public static <R extends DeserializeReader, C, F> DeSerializeMember<R, C, F> create(final FieldAttribute<C, F> fieldAttribute, final ConvertFactory convertFactory, final Class<F> fieldType) {
		return new DeSerializeMember(fieldAttribute, convertFactory.loadDeSerializeParser(fieldType));
	}

	/**
	 * 根据字段名判断是否匹配反序列化成员.
	 *
	 * @param fieldName 字段名称
	 * @return boolean
	 */
	public final boolean match(final String fieldName) {
		return this.fieldAttribute.getFieldAliasName().equals(fieldName);
	}

	/**
	 * 反序列化解析并赋值给动态映射指定的对象类.
	 *
	 * @param in 反序列化输入流
	 * @param obj 指定的对象
	 */
	public final void read(final R in, final C obj) {
		this.fieldAttribute.setFieldValue(obj, this.deSerializeParser.convertFrom(in));
	}

	/**
	 * 获取反序列化后的数据类型.
	 *
	 * @param in 反序列化输入流
	 * @return 反序列化数据类型
	 */
	public final F read(final R in) {
		return this.deSerializeParser.convertFrom(in);
	}

	/**
	 * 获取动态映射类.
	 *
	 * @return FieldAttribute
	 */
	public FieldAttribute<C, F> getFieldAttribute() {
		return this.fieldAttribute;
	}

	@Override
	public int compareTo(final DeSerializeMember<R, C, F> o) {
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
		if (!(obj instanceof DeSerializeMember)) {
			return false;
		}
		final DeSerializeMember member = (DeSerializeMember) obj;
		return this.compareTo(member) == 0;
	}

	@Override
	public String toString() {
		return "DeSerializeMember{" + "fieldAttribute=" + this.fieldAttribute.getFieldAliasName() + ", DeSerializeParser=" + this.deSerializeParser + "}";
	}

}
