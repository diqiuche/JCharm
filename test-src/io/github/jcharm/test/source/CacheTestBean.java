/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.source.EntityCache;
import io.github.jcharm.source.EntityInfo;
import io.github.jcharm.source.FilterFunction;
import io.github.jcharm.source.annotation.EntityId;

/**
 * The Class CacheTestBean.
 */
public class CacheTestBean {

	@EntityId
	private long pkgid;

	private String name;

	private long price;

	/**
	 * Instantiates a new cache test bean.
	 */
	public CacheTestBean() {
	}

	/**
	 * Instantiates a new cache test bean.
	 *
	 * @param pkgid the pkgid
	 * @param name the name
	 * @param price the price
	 */
	public CacheTestBean(final long pkgid, final String name, final long price) {
		this.pkgid = pkgid;
		this.name = name;
		this.price = price;
	}

	/**
	 * Gets the pkgid.
	 *
	 * @return the pkgid
	 */
	public long getPkgid() {
		return this.pkgid;
	}

	/**
	 * Sets the pkgid.
	 *
	 * @param pkgid the new pkgid
	 */
	public void setPkgid(final long pkgid) {
		this.pkgid = pkgid;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the price.
	 *
	 * @return the price
	 */
	public long getPrice() {
		return this.price;
	}

	/**
	 * Sets the price.
	 *
	 * @param price the new price
	 */
	public void setPrice(final long price) {
		this.price = price;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {
		final List<CacheTestBean> list = new ArrayList<>();
		list.add(new CacheTestBean(1, "a", 12));
		list.add(new CacheTestBean(1, "a", 18));
		list.add(new CacheTestBean(2, "b", 20));
		list.add(new CacheTestBean(2, "bb", 60));
		FieldAttribute.create(CacheTestBean.class, "pkgid");
		FieldAttribute.create(CacheTestBean.class, "name");
		FieldAttribute.create(CacheTestBean.class, "price");
		final Function<Class, List> fullloader = (z) -> list;
		final EntityCache<CacheTestBean> cache = new EntityCache(EntityInfo.load(CacheTestBean.class, 0, true, new Properties(), fullloader));
		cache.fullLoad();
		System.out.println(cache.getMapResult("pkgid", FilterFunction.COUNT, "name", null));
		System.out.println(cache.getMapResult("pkgid", FilterFunction.DISTINCTCOUNT, "name", null));
		System.out.println(cache.getMapResult("pkgid", FilterFunction.AVG, "price", null));
		System.out.println(cache.getMapResult("pkgid", FilterFunction.SUM, "price", null));
		System.out.println(cache.getMapResult("pkgid", FilterFunction.MAX, "price", null));
		System.out.println(cache.getMapResult("pkgid", FilterFunction.MIN, "price", null));
	}

}
