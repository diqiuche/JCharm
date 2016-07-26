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
 * 标识Entity类与数据表的对应关系.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityTable {

	/**
	 * 对应数据表的名称.
	 *
	 * @return String
	 */
	public String name() default "";

	/**
	 * 对应数据表Catalog名称(部分数据库无该内容).
	 *
	 * @return String
	 */
	public String catalog() default "";

	/**
	 * 对应数据库Schema名称(部分数据库无该内容).
	 *
	 * @return String
	 */
	public String schema() default "";

}
