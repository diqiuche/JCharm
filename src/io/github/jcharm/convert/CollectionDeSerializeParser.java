/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import io.github.jcharm.common.ConstructCreator;

/**
 * 对象集合的反序列化解析器.
 *
 * @param <T> 反序列化数据类型
 */
public final class CollectionDeSerializeParser<T> implements DeSerializeParser<DeserializeReader, Collection<T>> {

	private final Type type;

	private final Type componentType;

	protected ConstructCreator<Collection<T>> constructCreator;

	private final DeSerializeParser<DeserializeReader, T> deSerializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public CollectionDeSerializeParser(final ConvertFactory convertFactory, final Type type) {
		this.type = type;
		// ParameterizedType : 表示参数化类型, 如 Collection<String>
		if (type instanceof ParameterizedType) {
			this.componentType = ((ParameterizedType) type).getActualTypeArguments()[0];
			// getRawType : 返回Type对象, 表示声明此类型的类或接口
			this.constructCreator = convertFactory.loadConstructCreator((Class) ((ParameterizedType) type).getRawType());
			convertFactory.registerDeSerializeParser(type, this);
			this.deSerializeParser = convertFactory.loadDeSerializeParser(this.componentType);
		} else {
			throw new ConvertException("CollectionDeSerializeParser not support the type (" + type + ")");
		}
	}

	@Override
	public Collection<T> convertFrom(final DeserializeReader in) {
		final int length = in.readArrayBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		final Collection<T> result = this.constructCreator.construct();
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
		return result;
	}

	@Override
	public Type getType() {
		return this.type;
	}

}
