/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.io.IOException;

import io.github.jcharm.common.ConfigValue;

/**
 * Servlet抽象类.
 *
 * @param <C> Context子类型
 * @param <R> Request子类型
 * @param <N> Response子类型
 */
public abstract class Servlet<C extends Context, R extends Request<C>, N extends Response<C, R>> {

	/** 当前Servlet配置. */
	ConfigValue configValue;

	/**
	 * 初始化方法.
	 *
	 * @param context Context子类型
	 * @param configValue ConfigValue
	 */
	public void init(final C context, final ConfigValue configValue) {
	}

	/**
	 * 执行方法.
	 *
	 * @param request Request子类型
	 * @param response Response子类型
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void execute(R request, N response) throws IOException;

	/**
	 * 销毁方法.
	 *
	 * @param context Context子类型
	 * @param configValue ConfigValue
	 */
	public void destroy(final C context, final ConfigValue configValue) {
	}

}
