/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.common;

import org.junit.Before;
import org.junit.Test;

import io.github.jcharm.common.FieldAttribute;

/**
 * FieldAttribute测试类.
 */
public class TestFieldAttriubte {

	private SimpleBean simpleBean;

	/**
	 * Inits the.
	 */
	@Before
	public void init() {
		this.simpleBean = new SimpleBean("DanielWang", 28, false);
	}

	/**
	 * Test create.
	 */
	@Test
	public void testCreate() {
		final FieldAttribute fieldAttribute_1 = FieldAttribute.create(SimpleBean.class, "testName");
		System.out.println(fieldAttribute_1);
		System.out.println("修改前Bean类中testName的值: " + fieldAttribute_1.getFieldValue(this.simpleBean));
		fieldAttribute_1.setFieldValue(this.simpleBean, "Wang Wei");
		System.out.println("修改后Bean类中testName的值: " + this.simpleBean.getTestName());
		final FieldAttribute fieldAttribute_2 = FieldAttribute.create(SimpleBean.class, "testAge");
		System.out.println(fieldAttribute_2);
		System.out.println("修改前Bean类中testAge的值: " + fieldAttribute_2.getFieldValue(this.simpleBean));
		fieldAttribute_2.setFieldValue(this.simpleBean, 40);
		System.out.println("修改后Bean类中testAge的值: " + this.simpleBean.getTestAge());
		final FieldAttribute fieldAttribute_3 = FieldAttribute.create(SimpleBean.class, "testXieBie");
		System.out.println(fieldAttribute_3);
		System.out.println("修改前Bean类中testXieBie的值: " + fieldAttribute_3.getFieldValue(this.simpleBean));
		fieldAttribute_3.setFieldValue(this.simpleBean, true);
		System.out.println("修改后Bean类中testXieBie的值: " + this.simpleBean.isTestXieBie());
		final FieldAttribute fieldAttribute_4 = FieldAttribute.create(SimpleBean.class, "testDouble");
		System.out.println(fieldAttribute_4);
		System.out.println("修改前Bean类中testDouble的值: " + fieldAttribute_4.getFieldValue(this.simpleBean));
		fieldAttribute_4.setFieldValue(this.simpleBean, 22.5);
		System.out.println("修改后Bean类中testDouble的值: " + this.simpleBean.getTestDouble());
	}

	/**
	 * Test create array.
	 */
	@Test
	public void testCreateArray() {
		final FieldAttribute[] fieldAttributes = FieldAttribute.create(SimpleBean.class);
		for (final FieldAttribute fieldAttribute : fieldAttributes) {
			System.out.println(fieldAttribute);
		}
	}

}
