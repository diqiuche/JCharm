/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.source;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.github.jcharm.convert.json.JsonConvert;
import io.github.jcharm.source.EntityInfo;
import io.github.jcharm.source.FilterBuild;
import io.github.jcharm.source.FilterExpress;
import io.github.jcharm.source.PageData;
import io.github.jcharm.source.PageTurn;
import io.github.jcharm.source.annotation.EntityCacheable;
import io.github.jcharm.source.annotation.EntityId;
import io.github.jcharm.source.annotation.EntityVirtual;

/**
 * The Class TestVirtualEntity.
 */
@EntityVirtual
@EntityCacheable
public class TestVirtualEntity {

	@EntityId
	private int userid;

	private String username;

	private long createtime = System.currentTimeMillis();

	/**
	 * Instantiates a new test virtual entity.
	 */
	public TestVirtualEntity() {

	}

	/**
	 * Instantiates a new test virtual entity.
	 *
	 * @param userid the userid
	 * @param username the username
	 */
	public TestVirtualEntity(final int userid, final String username) {
		this.userid = userid;
		this.username = username;
	}

	/**
	 * Gets the userid.
	 *
	 * @return the userid
	 */
	public int getUserid() {
		return this.userid;
	}

	/**
	 * Sets the userid.
	 *
	 * @param userid the new userid
	 */
	public void setUserid(final int userid) {
		this.userid = userid;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * Gets the createtime.
	 *
	 * @return the createtime
	 */
	public long getCreatetime() {
		return this.createtime;
	}

	/**
	 * Sets the createtime.
	 *
	 * @param createtime the new createtime
	 */
	public void setCreatetime(final long createtime) {
		this.createtime = createtime;
	}

	@Override
	public String toString() {
		return JsonConvert.instance().convertTo(this);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(final String[] args) throws Exception {
		final EntityInfo<TestVirtualEntity> info = EntityInfo.load(TestVirtualEntity.class, 0, false, new Properties(), null);
		final TestVirtualEntity[] entitys = new TestVirtualEntity[100000];
		for (int i = 0; i < entitys.length; i++) {
			entitys[i] = new TestVirtualEntity(i + 1, "用户_" + (i + 1));
		}
		long s = System.currentTimeMillis();
		for (final TestVirtualEntity en : entitys) {
			info.getCache().insert(en);
		}
		long e = System.currentTimeMillis() - s;
		System.out.println("插入十万条记录耗时： " + (e / 1000.0) + " 秒");
		s = System.currentTimeMillis();
		final TestVirtualEntity one = info.getCache().find(9999);
		e = System.currentTimeMillis() - s;
		System.out.println("十万条数据中查询一条记录耗时： " + (e / 1000.0) + " 秒 " + one);
		final PageTurn pageTurn = new PageTurn(2);
		pageTurn.setSort("userid DESC, createtime DESC");
		final FilterBuild filterBuild = FilterBuild.create("userid", FilterExpress.GREATERTHAN, 1000).and("username", FilterExpress.LIKE, "用户");
		System.out.println("filterBuild = " + filterBuild);
		final PageData<TestVirtualEntity> pageData = info.getCache().queryPage(null, pageTurn, filterBuild);
		System.out.println(pageData);
		// CountDownLatch : 一个同步辅助类, 在完成一组正在其他线程中执行的操作之前, 它允许一个或多个线程一直等待
		final CountDownLatch cdl = new CountDownLatch(100);
		s = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			new Thread() {

				@Override
				public void run() {
					for (int k = 0; k < 10; k++) {
						info.getCache().queryPage(true, null, pageTurn, filterBuild);
					}
					// countDown : 递减锁存器的计数, 如果计数到达零, 则释放所有等待的线程.
					cdl.countDown();
				}
			}.start();
		}
		// await : 使当前线程在锁存器倒计数至零之前一直等待, 除非线程被中断.
		cdl.await();
		e = System.currentTimeMillis() - s;
		System.out.println("十万条数据中查询一页记录耗时： " + (e / 1000.0) + " 秒 " + pageData);
	}

}
