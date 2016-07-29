/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.jcharm.convert.ConvertType;

/**
 * 定义在字段、getter、setter上的注解, 实现其简单配置.
 */
@Inherited
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertColumn {

	/**
	 * 给字段定义别名, 该参数定义在字段上起作用, 定义在方法上无意义.
	 *
	 * @return String
	 */
	public String name() default "";

	/**
	 * 双向序列化时是否屏蔽该字段.
	 *
	 * @return boolean
	 */
	public boolean ignore() default false;

	/**
	 * 指定在哪种双向序列化方式下ignore的值起作用.
	 *
	 * @return ConvertType
	 */
	public ConvertType type() default ConvertType.ALL;

}
