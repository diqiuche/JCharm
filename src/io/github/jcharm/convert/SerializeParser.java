/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Type;

/**
 * 序列化解析器.
 *
 * @param <W> 序列化输出流
 * @param <T> 序列化数据类型
 */
public interface SerializeParser<W extends SerializeWriter, T> {

	/**
	 * 序列化解析.
	 *
	 * @param out 序列化输出流
	 * @param value 序列化数据类型
	 */
	public void convertTo(W out, T value);

	/**
	 * 泛型映射接口.
	 *
	 * @return 返回序列化对象类的数据类型
	 */
	public Type getType();

}
