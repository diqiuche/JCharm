/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.source;

import java.time.LocalDateTime;

import io.github.jcharm.convert.json.JsonConvert;
import io.github.jcharm.source.annotation.EntityDistributeGenerator;
import io.github.jcharm.source.annotation.EntityId;
import io.github.jcharm.source.annotation.EntityTable;

/**
 * The Class LoginLogTestEntity.
 */
@EntityTable(name = "LoginLog")
public class LoginLogTestEntity {

	@EntityId
	@EntityDistributeGenerator
	private int sessionId;

	private int userId;

	private String loginAgent;

	private String loginIP;

	private LocalDateTime loginTime;

	private LocalDateTime logoutTime;

	public int getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(final int sessionId) {
		this.sessionId = sessionId;
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(final int userId) {
		this.userId = userId;
	}

	public String getLoginAgent() {
		return this.loginAgent;
	}

	public void setLoginAgent(final String loginAgent) {
		this.loginAgent = loginAgent;
	}

	public String getLoginIP() {
		return this.loginIP;
	}

	public void setLoginIP(final String loginIP) {
		this.loginIP = loginIP;
	}

	public LocalDateTime getLoginTime() {
		return this.loginTime;
	}

	public void setLoginTime(final LocalDateTime loginTime) {
		this.loginTime = loginTime;
	}

	public LocalDateTime getLogoutTime() {
		return this.logoutTime;
	}

	public void setLogoutTime(final LocalDateTime logoutTime) {
		this.logoutTime = logoutTime;
	}

	@Override
	public String toString() {
		return JsonConvert.instance().convertTo(this);
	}

}
