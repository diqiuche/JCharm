/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.  
 */
package io.github.jcharm.test.common;

import io.github.jcharm.common.ConstructCreator;

/**
 * Construct通过ASM产生的示例类.
 */
public final class CreatorConstruct implements ConstructCreator<SimpleBean> {

	@Override
	@ConstructorParameters({ "testName", "testAge", "testXieBie" })
	public SimpleBean construct(final Object... params) {
		return new SimpleBean((String) params[0], (Integer) params[1], (Boolean) params[2]);
	}

}
