/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.source;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.logging.LogManager;

import io.github.jcharm.source.DataDefaultSource;
import io.github.jcharm.source.DataSource;

/**
 * The Class JDBCTest.
 */
public class JDBCTest {

	public static void main(String[] args) throws Exception {
		InputStream in = JDBCTest.class.getResourceAsStream("logging.properties");
		LogManager.getLogManager().readConfiguration(in);
		DataSource dataSource = new DataDefaultSource("", JDBCTest.class.getResource("persistence.xml"));
		int count = 100;
		LoginLogTestEntity last = null;
		long s = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			LoginLogTestEntity record = new LoginLogTestEntity();
			record.setUserId(i);
			record.setLoginAgent("win7");
			record.setLoginIP("127.0.0.1");
			record.setLoginTime(LocalDateTime.now());
			record.setLogoutTime(LocalDateTime.now());
			dataSource.insert(record);
			last = record;
			System.out.println(last);
		}
		long e = System.currentTimeMillis() - s;
		System.out.println("耗时：" + e);
	}

}
