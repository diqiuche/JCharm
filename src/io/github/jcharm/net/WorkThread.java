/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

import java.util.concurrent.ExecutorService;

/**
 * 工作线程.
 */
public class WorkThread extends Thread {

	private final ExecutorService executor; // 线程池对象

	/**
	 * 构造函数.
	 *
	 * @param executor ExecutorService
	 * @param runner Runnable
	 */
	public WorkThread(final ExecutorService executor, final Runnable runner) {
		super(runner);
		this.executor = executor;
		this.setDaemon(true);
	}

	/**
	 * 提交线程.
	 *
	 * @param runner Runnable
	 */
	public void submit(final Runnable runner) {
		this.executor.submit(runner);
	}

	/**
	 * 获取线程执行者.
	 *
	 * @return ExecutorService
	 */
	public ExecutorService getExecutor() {
		return this.executor;
	}

}
