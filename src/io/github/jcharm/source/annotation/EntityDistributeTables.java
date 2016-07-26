/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当使用EntityDistributeGenerator控制主键值时, 如果表A与表AHistory使用同一主键时, 就需要将表A的class标记EntityDistributeTables({AHistory.class}).
 * <p>
 * 这样EntityDistributeTables将从A、B表中取最大值来初始化主键值, 常见场景就是表B是数据表A对应的历史表.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityDistributeTables {

	/**
	 * Entity类Class.
	 *
	 * @return Class[]
	 */
	public Class[] value();

}
