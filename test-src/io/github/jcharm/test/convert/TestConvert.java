/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.convert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.github.jcharm.common.GenericsType;
import io.github.jcharm.convert.bson.BsonConvert;
import io.github.jcharm.convert.json.JsonConvert;
import io.github.jcharm.convert.json.JsonConvertFactory;

/**
 * The Class TestConvert.
 */
public class TestConvert {

	private SimpleBean simpleBean;

	/**
	 * Inits the.
	 */
	@Before
	public void init() {
		this.simpleBean = new SimpleBean();
		this.simpleBean.setName("王维");
		this.simpleBean.setAge(28);
		this.simpleBean.setSex(true);
		this.simpleBean.setBirthDate(LocalDate.of(1986, 1, 6));
		final List<String> list = new ArrayList();
		list.add("读书");
		list.add("打球");
		this.simpleBean.setHobbies(list);
		final Map<String, Integer> books = new HashMap();
		books.put("计算机原理", 32);
		books.put("JAVA编程", 74);
		this.simpleBean.setBooks(books);
		final SimpleBook simpleBook = new SimpleBook();
		simpleBook.setBookName("十万个为什么");
		simpleBook.setBookPrice(47.5);
		this.simpleBean.setSimpleBook(simpleBook);
	}

	/**
	 * Simple json convert.
	 */
	@Test
	public void simpleJsonConvert() {
		final JsonConvert convert = JsonConvert.instance();
		final String jsonStr = convert.convertTo(this.simpleBean);
		System.out.println(jsonStr);
	}

	/**
	 * Simple json column convert.
	 */
	@Test
	public void simpleJsonColumnConvert() {
		final JsonConvertFactory jsonConvertFactory = JsonConvertFactory.instance();
		jsonConvertFactory.registerConvertColumn(SimpleBean.class, false, "name");
		jsonConvertFactory.reloadParser(SimpleBean.class);
		final JsonConvert jsonConvert = jsonConvertFactory.getConvert();
		final String jsonStr = jsonConvert.convertTo(this.simpleBean);
		System.out.println(jsonStr);
	}

	/**
	 * Simple json to object.
	 */
	@Test
	public void simpleJsonToObject() {
		final JsonConvert convert = JsonConvert.instance();
		final String jsonStr = "{\"realName\":\"DanielWang\",\"age\":28,\"birthDate\":\"1986-01-06\",\"books\":{\"JAVA编程\":74,\"计算机原理\":32},\"hobbies\":[\"读书\",\"打球\"],\"sex\":true,\"simpleBook\":{\"bookName\":\"十万个为什么\",\"bookPrice\":47.5}}";
		final SimpleBean sb = convert.convertFrom(SimpleBean.class, jsonStr);
		System.out.println(sb.toString());
	}

	/**
	 * Simple map convert.
	 */
	@Test
	public void simpleMapConvert() {
		final Map<String, Integer> books = new HashMap();
		books.put("计算机原理", 32);
		books.put("JAVA编程", 74);
		final JsonConvert convert = JsonConvert.instance();
		final String jsonStr = convert.convertTo(books);
		System.out.println(jsonStr);
		final Map<String, Integer> map = convert.convertFrom(new GenericsType<Map<String, Integer>>() {
		}.getType(), jsonStr);
		System.out.println(map.toString());
	}

	@Test
	public void simpleBsonConvert() {
		final BsonConvert convert = BsonConvert.instance();
		final byte[] bytes = convert.convertTo(this.simpleBean);
		System.out.println(bytes);
		final SimpleBean sBean = convert.convertFrom(SimpleBean.class, bytes);
		System.out.println(sBean.toString());
	}

}
