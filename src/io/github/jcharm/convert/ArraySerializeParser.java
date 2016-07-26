/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 对象数组序列化解析器, 不包含int[]、long[]这样的primitive class数组.
 *
 * @param <T> 序列化的数据类型
 */
public final class ArraySerializeParser<T> implements SerializeParser<SerializeWriter, T[]> {

	private final Type type;

	private final Type componentType;

	private final SerializeParser anySerializeParser;

	private final SerializeParser<SerializeWriter, Object> serializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public ArraySerializeParser(final ConvertFactory convertFactory, final Type type) {
		this.type = type;
		// GenericArrayType : 表示一种数组类型, 其组件类型为参数化类型或类型变量
		if (type instanceof GenericArrayType) {
			// 返回表示此数组的组件类型的Type对象
			final Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
			// TypeVariable : 是各种类型变量的公共高级接口
			this.componentType = genericComponentType instanceof TypeVariable ? Object.class : genericComponentType;
		} else if ((type instanceof Class) && ((Class) type).isArray()) {
			// 返回表示数组组件类型的Class, 如果此类不表示数组类, 则此方法返回null
			this.componentType = ((Class) type).getComponentType();
		} else {
			throw new ConvertException("(" + type + ") is not a array type");
		}
		convertFactory.registerSerializeParser(type, this);
		this.serializeParser = convertFactory.loadSerializeParser(this.componentType);
		this.anySerializeParser = convertFactory.getAnySerializeParser();
	}

	@Override
	public void convertTo(final SerializeWriter out, final T[] value) {
		if (value == null) {
			out.writeNull();
			return;
		}
		if (value.length == 0) {
			out.writeArrayBegin(0);
			out.writeArrayEnd();
			return;
		}
		out.writeArrayBegin(value.length);
		final Type componentType = this.componentType;
		boolean first = true;
		for (final Object obj : value) {
			if (!first) {
				out.writeArrayMark();
			}
			(((obj != null) && (obj.getClass() == componentType)) ? this.serializeParser : this.anySerializeParser).convertTo(out, obj);
			if (first) {
				first = false;
			}
		}
		out.writeArrayEnd();
	}

	@Override
	public Type getType() {
		return this.type;
	}

}
