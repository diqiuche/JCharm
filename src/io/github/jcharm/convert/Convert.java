/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

/**
 * 双向序列化类.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public abstract class Convert<R extends DeserializeReader, W extends SerializeWriter> {

	/** 双向序列化工厂类. */
	protected final ConvertFactory<R, W> convertFactory;

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 */
	protected Convert(final ConvertFactory<R, W> convertFactory) {
		this.convertFactory = convertFactory;
	}

	/**
	 * 返回双向序列化工厂类.
	 *
	 * @return ConvertFactory
	 */
	public ConvertFactory<R, W> getConvertFactory() {
		return this.convertFactory;
	}

}
