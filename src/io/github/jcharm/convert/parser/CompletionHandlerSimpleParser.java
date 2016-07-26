/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.parser;

import java.nio.channels.CompletionHandler;

import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.SerializeWriter;

/**
 * CompletionHandler的双向序列化解析器, CompletionHandler是异步IO操作结果的回调接口, 用于定义在IO操作完成后所作的回调工作.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public final class CompletionHandlerSimpleParser<R extends DeserializeReader, W extends SerializeWriter> extends AbstractSimpleParser<R, W, CompletionHandler> {

	/** 实例对象. */
	public static final CompletionHandlerSimpleParser INSTANCE = new CompletionHandlerSimpleParser();

	@Override
	public void convertTo(final W out, final CompletionHandler value) {
		out.writeObjectNull();
	}

	@Override
	public CompletionHandler convertFrom(final R in) {
		in.readObjectBegin(CompletionHandler.class);
		return null;
	}

}
