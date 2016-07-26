/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.github.jcharm.convert.DeSerializeParser;
import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeParser;
import io.github.jcharm.convert.SerializeWriter;

/**
 * 双向序列化抽象解析器, 继承该类实现对简单数据类型和特定对象解析.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 * @param <T> 序列化/反序列化数据类型
 */
public abstract class AbstractSimpleParser<R extends DeserializeReader, W extends SerializeWriter, T> implements DeSerializeParser<R, T>, SerializeParser<W, T> {

	private Type type;

	@Override
	public abstract void convertTo(final W out, final T value);

	@Override
	public abstract T convertFrom(final R in);

	@Override
	public Class<T> getType() {
		if (this.type == null) {
			// getGenericSuperclass : 返回表示此Class所表示的实体(类、接口、基本类型或 void)的直接超类的Type
			// ParameterizedType : 表示参数化类型, 如Collection<String>
			// getActualTypeArguments : 返回表示此类型实际类型参数的Type对象的数组
			final Type[] ts = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
			this.type = ts[ts.length - 1];
		}
		return (Class<T>) this.type;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
