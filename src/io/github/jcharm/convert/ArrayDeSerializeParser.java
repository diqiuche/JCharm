/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * 对象数组反序列化解析器, 不包含int[]、long[]这样的primitive class数组.
 *
 * @param <T> 反序列化数据类型
 */
public final class ArrayDeSerializeParser<T> implements DeSerializeParser<DeserializeReader, T[]> {

	private final Type type;

	private final Type componentType;

	private final Class componentClass;

	private final DeSerializeParser<DeserializeReader, T> deSerializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public ArrayDeSerializeParser(final ConvertFactory convertFactory, final Type type) {
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
		// ParameterizedType : 表示参数化类型, 如 Collection<String>
		if (this.componentType instanceof ParameterizedType) {
			// getRawType : 返回Type对象, 表示声明此类型的类或接口
			this.componentClass = (Class) ((ParameterizedType) this.componentType).getRawType();
		} else {
			this.componentClass = (Class) this.componentType;
		}
		convertFactory.registerDeSerializeParser(type, this);
		this.deSerializeParser = convertFactory.loadDeSerializeParser(this.componentType);
	}

	@Override
	public T[] convertFrom(final DeserializeReader in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		final List<T> result = new ArrayList();
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			while (in.hasNext()) {
				result.add(this.deSerializeParser.convertFrom(in));
			}
		} else {
			for (int i = 0; i < length; i++) {
				result.add(this.deSerializeParser.convertFrom(in));
			}
		}
		in.readArrayEnd();
		// Array : 提供了动态创建和访问Java数组的方法, 同时允许在执行 get 或 set 操作期间进行扩展转换
		// newInstance : 创建一个具有指定的组件类型和长度的新数组
		final T[] rs = (T[]) Array.newInstance(this.componentClass, result.size());
		// toArray : 返回按适当顺序(从第一个元素到最后一个元素)包含列表中所有元素的数组, 返回数组的运行时类型是指定数组的运行时类型
		return result.toArray(rs);
	}

	@Override
	public Type getType() {
		return this.type;
	}

}
