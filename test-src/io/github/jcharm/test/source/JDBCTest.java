/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.source;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.Before;
import org.junit.Test;

import io.github.jcharm.source.DataDefaultSource;
import io.github.jcharm.source.DataSource;
import io.github.jcharm.source.FilterBuild;
import io.github.jcharm.source.FilterField;
import io.github.jcharm.source.FilterRange;
import io.github.jcharm.source.PageData;
import io.github.jcharm.source.PageTurn;

/**
 * The Class JDBCTest.
 */
public class JDBCTest {

	private DataSource dataSource;

	@Before
	public void init() throws Exception {
		final InputStream in = JDBCTest.class.getResourceAsStream("logging.properties");
		LogManager.getLogManager().readConfiguration(in);
		this.dataSource = new DataDefaultSource("", JDBCTest.class.getResource("persistence.xml"));
	}

	@Test
	public void insertTest() {
		final int count = 100;
		LoginLogTestEntity last = null;
		final long s = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			final LoginLogTestEntity record = new LoginLogTestEntity();
			record.setUserId(i);
			record.setLoginAgent("win7");
			record.setLoginIP("127.0.0.1");
			record.setLoginTime(LocalDateTime.now());
			record.setLogoutTime(LocalDateTime.now());
			this.dataSource.insert(record);
			last = record;
			System.out.println(last);
		}
		final long e = System.currentTimeMillis() - s;
		System.out.println("耗时：" + e);
	}

	@Test
	public void queryListTest() {
		final List<LoginLogTestEntity> list = this.dataSource.queryList(LoginLogTestEntity.class, FilterBuild.create("loginTime", new FilterField("logoutTime")));
		System.out.println(list);
	}

	@Test
	public void queryListTestTwo() {
		final List<LoginLogTestEntity> list = this.dataSource.queryList(LoginLogTestEntity.class, FilterBuild.create("userId", new FilterRange.FilterIntRange(10, 20)));
		System.out.println(list);
	}

	@Test
	public void queryListTestThree() {
		final LocalDateTime min = LocalDateTime.of(2016, 7, 27, 11, 14, 28);
		final LocalDateTime max = LocalDateTime.of(2016, 7, 27, 11, 19, 12);
		final List<LoginLogTestEntity> list = this.dataSource.queryList(LoginLogTestEntity.class, FilterBuild.create("loginTime", new FilterRange.FilterLocalDateTimeRange(min, max)));
		System.out.println(list);
	}

	@Test
	public void findTest() {
		final LocalDateTime localDateTime = LocalDateTime.of(2016, 7, 27, 11, 14, 28);
		final LoginLogTestEntity record = this.dataSource.find(LoginLogTestEntity.class, FilterBuild.create("loginTime", localDateTime));
		System.out.println(record);
	}

	@Test
	public void findTestTwo() {
		final LocalDateTime localDateTime = LocalDateTime.of(2016, 7, 27, 11, 19, 18);
		final LoginLogTestEntity record = this.dataSource.find(LoginLogTestEntity.class, FilterBuild.create("loginTime", localDateTime), "userId DESC");
		System.out.println(record);
	}

	@Test
	public void queryPage() {
		PageTurn pageTurn = new PageTurn(5, 10, "loginTime DESC");
		PageData<LoginLogTestEntity> pageData = this.dataSource.queryPage(LoginLogTestEntity.class, pageTurn, null);
		System.out.println(pageData);
	}

}
