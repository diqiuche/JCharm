/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.util.concurrent.ExecutorService;

/**
 * 工作线程.
 */
public class WorkThread extends Thread {

	private final ExecutorService executorService; // 线程池

	/**
	 * 构造函数.
	 *
	 * @param executorService ExecutorService
	 * @param runnable Runnable
	 */
	public WorkThread(final ExecutorService executorService, final Runnable runnable) {
		super(runnable);
		this.executorService = executorService;
		this.setDaemon(true); // 将该线程标记为守护线程
	}

	/**
	 * 提交一个Runnable任务用于执行.
	 *
	 * @param runnable Runnable
	 */
	public void submit(final Runnable runnable) {
		this.executorService.submit(runnable); // 提交一个Runnable任务用于执行, 并返回一个表示该任务的Future
	}

	/**
	 * 获取线程池对象.
	 *
	 * @return ExecutorService
	 */
	public ExecutorService getExecutorService() {
		return this.executorService;
	}

}
