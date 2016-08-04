/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.io.IOException;

import io.github.jcharm.common.ConfigValue;

/**
 * The Class Servlet.
 *
 * @param <C> the generic type
 * @param <R> the generic type
 * @param
 * 			<P>
 *            the generic type
 */
public abstract class Servlet<C extends Context, R extends Request<C>, P extends Response<C, R>> {

	/** 当前Servlet的配置. */
	ConfigValue _conf;

	/**
	 * Inits the.
	 *
	 * @param context the context
	 * @param config the config
	 */
	public void init(final C context, final ConfigValue config) {
	}

	/**
	 * Execute.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void execute(R request, P response) throws IOException;

	/**
	 * Destroy.
	 *
	 * @param context the context
	 * @param config the config
	 */
	public void destroy(final C context, final ConfigValue config) {
	}

}
