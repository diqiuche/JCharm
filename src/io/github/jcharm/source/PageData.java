/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 页集合, 结构由一个total总数和一个List列表组合而成.
 *
 * @param <T> 集合元素的数据类型
 */
public class PageData<T> implements java.io.Serializable, Iterable<T> {

	private static final long serialVersionUID = 1L;

	private long total;

	private Collection<T> rows;

	/**
	 * 构造函数.
	 */
	public PageData() {
		super();
	}

	/**
	 * 构造函数.
	 *
	 * @param total 总数
	 * @param data 数据集合
	 */
	public PageData(final long total, final Collection<? extends T> data) {
		this.total = total;
		this.rows = (Collection<T>) data;
	}

	/**
	 * 将数据集合转为页集合.
	 *
	 * @param <E> 集合元素的数据类型
	 * @param data 数据集合
	 * @return PageData
	 */
	public static <E> PageData<E> asSheet(final Collection<E> data) {
		return data == null ? new PageData() : new PageData(data.size(), data);
	}

	/**
	 * 页集合拷贝.
	 *
	 * @param copy 页集合
	 * @return PageData
	 */
	public PageData<T> copyTo(final PageData<T> copy) {
		if (copy == null) {
			return copy;
		}
		copy.total = this.total;
		if (this.getRows() != null) {
			copy.setRows(new ArrayList(this.getRows()));
		} else {
			copy.rows = null;
		}
		return copy;
	}

	/**
	 * 判断数据集合是否为空.
	 *
	 * @return boolean
	 */
	public boolean isEmpty() {
		return (this.rows == null) || this.rows.isEmpty();
	}

	@Override
	public String toString() {
		return "PageData[total=" + this.total + ", rows=" + this.rows + "]";
	}

	/**
	 * 获取数据集合总数.
	 *
	 * @return long
	 */
	public long getTotal() {
		return this.total;
	}

	/**
	 * 设置数据集合总数.
	 *
	 * @param total 总数
	 */
	public void setTotal(final long total) {
		this.total = total;
	}

	/**
	 * 获取数据集合.
	 *
	 * @return Collection
	 */
	public Collection<T> getRows() {
		return this.rows;
	}

	/**
	 * 获取数据List集合.
	 *
	 * @return List
	 */
	public List<T> list() {
		return this.list(false);
	}

	/**
	 * 获取数据List集合, 如果集合为null是否进行创建.
	 *
	 * @param created 是否创建
	 * @return List
	 */
	public List<T> list(final boolean created) {
		if (this.rows == null) {
			return created ? new ArrayList() : null;
		}
		return (this.rows instanceof List) ? (List<T>) this.rows : new ArrayList(this.rows);
	}

	/**
	 * 设置数据集合.
	 *
	 * @param data 数据集合
	 */
	public void setRows(final Collection<? extends T> data) {
		this.rows = (Collection<T>) data;
	}

	@Override
	public Iterator<T> iterator() {
		return (this.rows == null) ? new ArrayList<T>().iterator() : this.rows.iterator();
	}

	@Override
	public void forEach(final Consumer<? super T> consumer) {
		if ((consumer != null) && (this.rows != null) && !this.rows.isEmpty()) {
			this.rows.forEach(consumer);
		}
	}

	@Override
	public Spliterator<T> spliterator() {
		return (this.rows == null) ? new ArrayList<T>().spliterator() : this.rows.spliterator();
	}

	/**
	 * 获取Stream集合.
	 *
	 * @return Stream
	 */
	public Stream<T> stream() {
		return (this.rows == null) ? new ArrayList<T>().stream() : this.rows.stream();
	}

	/**
	 * 获取并行Stream集合.
	 *
	 * @return Stream
	 */
	public Stream<T> parallelStream() {
		return (this.rows == null) ? new ArrayList<T>().parallelStream() : this.rows.parallelStream();
	}

	/**
	 * 返回数据集合的对象数组.
	 *
	 * @return Object[]
	 */
	public Object[] toArray() {
		return (this.rows == null) ? new ArrayList<T>().toArray() : this.rows.toArray();
	}

	/**
	 * 返回数据集合的对象数组.
	 *
	 * @param a 泛型数组
	 * @return 泛型数组
	 */
	public T[] toArray(final T[] a) {
		return (this.rows == null) ? new ArrayList<T>().toArray(a) : this.rows.toArray(a);
	}
}
