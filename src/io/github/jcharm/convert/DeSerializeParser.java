/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Type;

/**
 * 反序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <T> 反序列化数据类型
 */
public interface DeSerializeParser<R extends DeserializeReader, T> {

	/**
	 * 反序列化解析.
	 *
	 * @param in 反序列化输入流
	 * @return 反序列化数据类型
	 */
	public T convertFrom(R in);

	/**
	 * 泛型映射接口.
	 *
	 * @return 反序列化数据类型
	 */
	public Type getType();

}
