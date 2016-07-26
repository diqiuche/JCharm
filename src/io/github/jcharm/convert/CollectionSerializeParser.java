/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * 对象集合的序列化解析器.
 *
 * @param <T> 序列化数据类型
 */
public final class CollectionSerializeParser<T> implements SerializeParser<SerializeWriter, Collection<T>> {

	private final Type type;

	private final SerializeParser<SerializeWriter, Object> serializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public CollectionSerializeParser(final ConvertFactory convertFactory, final Type type) {
		this.type = type;
		// ParameterizedType : 表示参数化类型, 如 Collection<String>
		if (type instanceof ParameterizedType) {
			// getActualTypeArguments : 返回表示此类型实际类型参数的Type对象的数组
			final Type parameterizedType = ((ParameterizedType) type).getActualTypeArguments()[0];
			if (parameterizedType instanceof TypeVariable) {
				this.serializeParser = convertFactory.getAnySerializeParser();
			} else {
				this.serializeParser = convertFactory.loadSerializeParser(parameterizedType);
			}
		} else {
			this.serializeParser = convertFactory.getAnySerializeParser();
		}
	}

	@Override
	public void convertTo(final SerializeWriter out, final Collection<T> value) {
		if (value == null) {
			out.writeNull();
			return;
		}
		if (value.isEmpty()) {
			out.writeArrayBegin(0);
			out.writeArrayEnd();
			return;
		}
		out.writeArrayBegin(value.size());
		boolean first = true;
		for (final Object obj : value) {
			if (!first) {
				out.writeArrayMark();
			}
			this.serializeParser.convertTo(out, obj);
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
