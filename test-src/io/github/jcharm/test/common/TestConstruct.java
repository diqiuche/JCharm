/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.common;

import org.junit.Test;

import io.github.jcharm.common.ConstructCreator;

/**
 * Construct测试类.
 */
public class TestConstruct {

	/**
	 * Construct.
	 */
	@Test
	public void construct() {
		final SimpleBean simpleBean = ConstructCreator.create(SimpleBean.class).construct("DaielWang", 28, true);
		System.out.println(simpleBean.getTestName());
		System.out.println(simpleBean.getTestAge());
	}

}
