/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 对象池类.
 *
 * @param <T> 对象池元素的数据类型
 */
public final class ObjectPool<T> implements Supplier<T> {

	private ConstructCreator<T> constructCreator;

	private final Queue<T> queue;

	private final Consumer<T> prepare;

	private final Predicate<T> recycler;

	private final AtomicLong creatCounter;

	private final AtomicLong cycleCounter;

	/**
	 * 构造函数.
	 *
	 * @param creatCounter AtomicLong
	 * @param cycleCounter AtomicLong
	 * @param max int
	 * @param construct the construct
	 * @param prepare Consumer
	 * @param recycler Predicate
	 */
	public ObjectPool(final AtomicLong creatCounter, final AtomicLong cycleCounter, final int max, final ConstructCreator<T> construct, final Consumer<T> prepare, final Predicate<T> recycler) {
		this.creatCounter = creatCounter;
		this.cycleCounter = cycleCounter;
		this.constructCreator = construct;
		this.prepare = prepare;
		this.recycler = recycler;
		this.queue = new LinkedBlockingQueue(Math.max(Runtime.getRuntime().availableProcessors() * 2, max));
	}

	/**
	 * 构造函数.
	 *
	 * @param max int
	 * @param construct the construct
	 * @param prepare Consumer
	 * @param recycler Predicate
	 */
	public ObjectPool(final int max, final ConstructCreator<T> construct, final Consumer<T> prepare, final Predicate<T> recycler) {
		this(null, null, max, construct, prepare, recycler);
	}

	/**
	 * 构造函数.
	 *
	 * @param construct the construct
	 * @param prepare Consumer
	 * @param recycler Predicate
	 */
	public ObjectPool(final ConstructCreator<T> construct, final Consumer<T> prepare, final Predicate<T> recycler) {
		this(2, construct, prepare, recycler);
	}

	/**
	 * 构造函数.
	 *
	 * @param max int
	 * @param clazz Class
	 * @param prepare Consumer
	 * @param recycler Predicate
	 */
	public ObjectPool(final int max, final Class<T> clazz, final Consumer<T> prepare, final Predicate<T> recycler) {
		this(2, ConstructCreator.create(clazz), prepare, recycler);
	}

	/**
	 * 构造函数.
	 *
	 * @param clazz Class
	 * @param prepare Consumer
	 * @param recycler Predicate
	 */
	public ObjectPool(final Class<T> clazz, final Consumer<T> prepare, final Predicate<T> recycler) {
		this(2, clazz, prepare, recycler);
	}

	@Override
	public T get() {
		// poll : 获取并移除此队列的头
		T result = this.queue.poll();
		if (result == null) {
			if (this.creatCounter != null) {
				// incrementAndGet : 以原子方式将当前值加1
				this.creatCounter.incrementAndGet();
			}
			result = this.constructCreator.construct();
		}
		if (this.prepare != null) {
			this.prepare.accept(result);
		}
		return result;
	}

	/**
	 * 将指定的元素插入池中.
	 *
	 * @param element T
	 */
	public void offer(final T element) {
		if ((element != null) && this.recycler.test(element)) {
			if (this.cycleCounter != null) {
				this.cycleCounter.incrementAndGet();
			}
			this.queue.offer(element);
		}
	}

	/**
	 * 获取对象池中对象创建次数.
	 *
	 * @return AtomicLong
	 */
	public AtomicLong getCreatCounter() {
		return this.creatCounter;
	}

	/**
	 * 获取对象池中对象重复利用的次数.
	 *
	 * @return the cycle counter
	 */
	public AtomicLong getCycleCounter() {
		return this.cycleCounter;
	}

}
