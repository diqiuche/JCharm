/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map类型序列化解析器.
 *
 * @param <K> Map.key的数据类型
 * @param <V> Map.value的数据类型
 */
public final class MapSerializeParser<K, V> implements SerializeParser<SerializeWriter, Map<K, V>> {

	private final Type type;

	private final SerializeParser<SerializeWriter, K> keySerializeParser;

	private final SerializeParser<SerializeWriter, V> valueSerializeParser;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 * @param type Type
	 */
	public MapSerializeParser(final ConvertFactory convertFactory, final Type type) {
		this.type = type;
		// ParameterizedType : 表示参数化类型, 如Collection<String>
		if (type instanceof ParameterizedType) {
			// getActualTypeArguments : 返回表示此类型实际类型参数的Type对象的数组
			final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
			this.keySerializeParser = convertFactory.loadSerializeParser(typeArguments[0]);
			this.valueSerializeParser = convertFactory.loadSerializeParser(typeArguments[1]);
		} else {
			this.keySerializeParser = convertFactory.getAnySerializeParser();
			this.valueSerializeParser = convertFactory.getAnySerializeParser();
		}
	}

	@Override
	public void convertTo(final SerializeWriter out, final Map<K, V> value) {
		final Map<K, V> map = value;
		if (map == null) {
			out.writeNull();
			return;
		}
		out.writeMapBegin(map.size());
		boolean first = true;
		for (final Map.Entry<K, V> en : map.entrySet()) {
			if (!first) {
				out.writeArrayMark();
			}
			this.keySerializeParser.convertTo(out, en.getKey());
			out.writeMapMark();
			this.valueSerializeParser.convertTo(out, en.getValue());
			if (first) {
				first = false;
			}
		}
		out.writeMapEnd();
	}

	@Override
	public Type getType() {
		return this.type;
	}

}
