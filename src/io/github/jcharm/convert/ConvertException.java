/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

/**
 * 双向序列化异常.
 */
public class ConvertException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造函数.
	 */
	public ConvertException() {
		super();
	}

	/**
	 * 构造函数.
	 *
	 * @param message String
	 */
	public ConvertException(final String message) {
		super(message);
	}

	/**
	 * 构造函数.
	 *
	 * @param cause Throwable
	 */
	public ConvertException(final Throwable cause) {
		super(cause);
	}

	/**
	 * 构造函数.
	 *
	 * @param message String
	 * @param cause Throwable
	 */
	public ConvertException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
