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
 * 标识Entity类中字段与数据表中字段的对应关系.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityColumn {

	/**
	 * 字段在数据库表中所对应的名称.
	 *
	 * @return String
	 */
	public String name() default "";

	/**
	 * 在使用INSERT脚本插入数据时, 是否需要插入该字段的值.
	 *
	 * @return boolean
	 */
	public boolean insertable() default true;

	/**
	 * 在使用UPDATE脚本插入数据时, 是否需要更新该字段的值.
	 *
	 * @return boolean
	 */
	public boolean updatable() default true;

}
