/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import io.github.jcharm.common.ConstructCreator;

/**
 * Map类型反序列化解析器.
 *
 * @param <K> Map.key的数据类型
 * @param <V> Map.value的数据类型
 */
public final class MapDeSerializeParser<K, V> implements DeSerializeParser<DeserializeReader, Map<K, V>> {

	private final Type type;

	private final Type keyType;

	private final Type valueType;

	/** ASM动态映射类构造方法. */
	protected ConstructCreator<Map<K, V>> constructCreator;

	private final DeSerializeParser<DeserializeReader, K> keyDeSerializeParser;

	private final DeSerializeParser<DeserializeReader, V> valueDeSerializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public MapDeSerializeParser(final ConvertFactory convertFactory, final Type type) {
		this.type = type;
		// ParameterizedType : 表示参数化类型, 如Collection<String>
		if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			// getActualTypeArguments : 返回表示此类型实际类型参数的Type对象的数组
			this.keyType = parameterizedType.getActualTypeArguments()[0];
			this.valueType = parameterizedType.getActualTypeArguments()[1];
			// getRawType : 返回Type对象, 表示声明此类型的类或接口
			this.constructCreator = convertFactory.loadConstructCreator((Class) parameterizedType.getRawType());
			convertFactory.registerDeSerializeParser(type, this);
			this.keyDeSerializeParser = convertFactory.loadDeSerializeParser(this.keyType);
			this.valueDeSerializeParser = convertFactory.loadDeSerializeParser(this.valueType);
		} else {
			throw new ConvertException("MapDeSerializeParser not support the type (" + type + ")");
		}
	}

	@Override
	public Map<K, V> convertFrom(final DeserializeReader in) {
		final int length = in.readMapBegin();
		if (length == DeserializeReader.SIGN_NULL) {
			return null;
		}
		final Map<K, V> result = this.constructCreator.construct();
		if (length == DeserializeReader.SIGN_NOLENGTH) {
			while (in.hasNext()) {
				final K key = this.keyDeSerializeParser.convertFrom(in);
				in.readBlank();
				final V value = this.valueDeSerializeParser.convertFrom(in);
				result.put(key, value);
			}
		} else {
			for (int i = 0; i < length; i++) {
				final K key = this.keyDeSerializeParser.convertFrom(in);
				in.readBlank();
				final V value = this.valueDeSerializeParser.convertFrom(in);
				result.put(key, value);
			}
		}
		in.readMapEnd();
		return result;
	}

	@Override
	public Type getType() {
		return this.type;
	}
}
