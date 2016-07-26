/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jcharm.common.ConstructCreator;
import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.common.ObjectCopy;
import io.github.jcharm.source.annotation.EntityColumn;
import io.github.jcharm.source.annotation.EntityTransient;

/**
 * Entity类缓存.
 *
 * @param <T> Entity类泛型
 */
public final class EntityCache<T> {

	private final ConcurrentHashMap<Serializable, T> map = new ConcurrentHashMap();

	// CopyOnWriteArrayList 插入慢、查询快; 10w数据插入需要3.2秒; ConcurrentLinkedQueue 插入快、查询慢; 10w数据插入需要 0.062秒, 查询慢40%;
	private final Collection<T> list = new ConcurrentLinkedQueue();// 一个基于链接节点的无界线程安全队列, 此队列按照FIFO(先进先出)原则对元素进行排序

	private final Map<String, Comparator<T>> sortComparators = new ConcurrentHashMap<>();

	private final Class<T> type;

	private final boolean needcopy;

	private final ConstructCreator<T> creator;

	private final FieldAttribute<T, Serializable> primary;

	private final ObjectCopy<T, T> newReproduce;

	private final ObjectCopy<T, T> chgReproduce;

	private volatile boolean fullloaded;

	/** Entity类信息. */
	final EntityInfo<T> info;

	/**
	 * 构造函数.
	 *
	 * @param info EntityInfo
	 */
	public EntityCache(final EntityInfo<T> info) {
		this.info = info;
		this.type = info.getType();
		this.creator = info.getCreator();
		this.primary = info.primary;
		this.needcopy = true;
		this.newReproduce = ObjectCopy.create(this.type, this.type, (m) -> {
			try {
				return this.type.getDeclaredField(m).getAnnotation(EntityTransient.class) == null;
			} catch (final Exception e) {
				return true;
			}
		});
		this.chgReproduce = ObjectCopy.create(this.type, this.type, (m) -> {
			try {
				final Field field = this.type.getDeclaredField(m);
				if (field.getAnnotation(EntityTransient.class) != null) {
					return false;
				}
				final EntityColumn column = field.getAnnotation(EntityColumn.class);
				if ((column != null) && !column.updatable()) {
					return false;
				}
				return true;
			} catch (final Exception e) {
				return true;
			}
		});
	}

	/**
	 * 完全加载.
	 */
	public void fullLoad() {
		if (this.info.fullloader == null) {
			return;
		}
		this.clear();
		final List<T> all = this.info.fullloader.apply(this.type);
		all.stream().filter(x -> x != null).forEach(x -> {
			this.map.put(this.primary.getFieldValue(x), x);
		});
		this.list.addAll(all);
		this.fullloaded = true;
	}

	/**
	 * 获取Entity类的Class.
	 *
	 * @return Class
	 */
	public Class<T> getType() {
		return this.type;
	}

	/**
	 * 清理相关缓存集合.
	 */
	public void clear() {
		this.fullloaded = false;
		this.list.clear();
		this.map.clear();
	}

	/**
	 * 判断是否完全加载.
	 *
	 * @return boolean
	 */
	public boolean isFullLoaded() {
		return this.fullloaded;
	}

	/**
	 * Find.
	 *
	 * @param id the id
	 * @return the t
	 */
	public T find(final Serializable id) {
		if (id == null) {
			return null;
		}
		final T rs = this.map.get(id);
		return rs == null ? null : (this.needcopy ? this.newReproduce.copy(this.creator.construct(), rs) : rs);
	}

	/**
	 * 根据主键值和SelectColumn从缓存中数据.
	 *
	 * @param selects SelectColumn
	 * @param id Serializable
	 * @return T
	 */
	public T find(final SelectColumn selects, final Serializable id) {
		if (id == null) {
			return null;
		}
		final T rs = this.map.get(id);
		if (rs == null) {
			return null;
		}
		if (selects == null) {
			return (this.needcopy ? this.newReproduce.copy(this.creator.construct(), rs) : rs);
		}
		final T t = this.creator.construct();
		for (final FieldAttribute attr : this.info.attributes) {
			if (selects.test(attr.getFieldDefaultName())) {
				attr.setFieldValue(t, attr.getFieldValue(rs));
			}
		}
		return t;
	}

	/**
	 * 根据FilterBuild和SelectColumn从缓存中获取数据.
	 *
	 * @param selects SelectColumn
	 * @param filterBuild FilterBuild
	 * @return T
	 */
	public T find(final SelectColumn selects, final FilterBuild filterBuild) {
		final Predicate<T> filter = filterBuild == null ? null : filterBuild.createPredicate(this);
		Stream<T> stream = this.list.stream();
		if (filter != null) {
			stream = stream.filter(filter);
		}
		final Optional<T> opt = stream.findFirst();
		if (!opt.isPresent()) {
			return null;
		}
		if (selects == null) {
			return (this.needcopy ? this.newReproduce.copy(this.creator.construct(), opt.get()) : opt.get());
		}
		final T rs = opt.get();
		final T t = this.creator.construct();
		for (final FieldAttribute attr : this.info.attributes) {
			if (selects.test(attr.getFieldDefaultName())) {
				attr.setFieldValue(t, attr.getFieldValue(rs));
			}
		}
		return t;
	}

	/**
	 * 判断主键值是否在缓存中存在.
	 *
	 * @param id Serializable
	 * @return boolean
	 */
	public boolean exists(Serializable id) {
		if (id == null) {
			return false;
		}
		final Class atype = this.primary.getFieldType();
		if ((id.getClass() != atype) && (id instanceof Number)) {
			if ((atype == int.class) || (atype == Integer.class)) {
				id = ((Number) id).intValue();
			} else if ((atype == long.class) || (atype == Long.class)) {
				id = ((Number) id).longValue();
			} else if ((atype == short.class) || (atype == Short.class)) {
				id = ((Number) id).shortValue();
			} else if ((atype == float.class) || (atype == Float.class)) {
				id = ((Number) id).floatValue();
			} else if ((atype == byte.class) || (atype == Byte.class)) {
				id = ((Number) id).byteValue();
			} else if ((atype == double.class) || (atype == Double.class)) {
				id = ((Number) id).doubleValue();
			}
		}
		return this.map.containsKey(id);
	}

	/**
	 * 根据FilterBuild判断数据是否存在于缓存中.
	 *
	 * @param filterBuild FilterBuild
	 * @return boolean
	 */
	public boolean exists(final FilterBuild filterBuild) {
		final Predicate<T> filter = filterBuild == null ? null : filterBuild.createPredicate(this);
		Stream<T> stream = this.list.stream();
		if (filter != null) {
			stream = stream.filter(filter);
		}
		return stream.findFirst().isPresent();
	}

	/**
	 * 根据Predicate判断数据是否存在于缓存中.
	 *
	 * @param filter Predicate
	 * @return boolean
	 */
	public boolean exists(final Predicate<T> filter) {
		return (filter != null) && this.list.stream().filter(filter).findFirst().isPresent();
	}

	/**
	 * 根据FilterFunction和FilterBuild对指定列从缓存中进行统计查询.
	 *
	 * @param <V> value类型
	 * @param filterFunction FilterFunction
	 * @param column Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Number
	 */
	public <V> Number getNumberResult(final FilterFunction filterFunction, final String column, final FilterBuild filterBuild) {
		final FieldAttribute<T, Serializable> attr = column == null ? null : this.info.getAttribute(column);
		final Predicate<T> filter = filterBuild == null ? null : filterBuild.createPredicate(this);
		Stream<T> stream = this.list.stream();
		if (filter != null) {
			stream = stream.filter(filter);
		}
		switch (filterFunction) {
		case AVG:
			if ((attr.getFieldType() == int.class) || (attr.getFieldType() == Integer.class)) {
				return (int) stream.mapToInt(x -> (Integer) attr.getFieldValue(x)).average().orElse(0);
			} else if ((attr.getFieldType() == long.class) || (attr.getFieldType() == Long.class)) {
				return (long) stream.mapToLong(x -> (Long) attr.getFieldValue(x)).average().orElse(0);
			} else if ((attr.getFieldType() == short.class) || (attr.getFieldType() == Short.class)) {
				return (short) stream.mapToInt(x -> ((Short) attr.getFieldValue(x)).intValue()).average().orElse(0);
			} else if ((attr.getFieldType() == float.class) || (attr.getFieldType() == Float.class)) {
				return (float) stream.mapToDouble(x -> ((Float) attr.getFieldValue(x)).doubleValue()).average().orElse(0);
			} else if ((attr.getFieldType() == double.class) || (attr.getFieldType() == Double.class)) {
				return stream.mapToDouble(x -> (Double) attr.getFieldValue(x)).average().orElse(0);
			}
			throw new RuntimeException("getNumberResult error(type:" + this.type + ", attr.declaringClass: " + attr.getDeclaringClass() + ", attr.field: " + attr.getFieldDefaultName() + ", attr.type: " + attr.getFieldType());
		case COUNT:
			return stream.count();
		case DISTINCTCOUNT:
			return stream.map(x -> attr.getFieldValue(x)).distinct().count();

		case MAX:
			if ((attr.getFieldType() == int.class) || (attr.getFieldType() == Integer.class)) {
				return stream.mapToInt(x -> (Integer) attr.getFieldValue(x)).max().orElse(0);
			} else if ((attr.getFieldType() == long.class) || (attr.getFieldType() == Long.class)) {
				return stream.mapToLong(x -> (Long) attr.getFieldValue(x)).max().orElse(0);
			} else if ((attr.getFieldType() == short.class) || (attr.getFieldType() == Short.class)) {
				return (short) stream.mapToInt(x -> ((Short) attr.getFieldValue(x)).intValue()).max().orElse(0);
			} else if ((attr.getFieldType() == float.class) || (attr.getFieldType() == Float.class)) {
				return (float) stream.mapToDouble(x -> ((Float) attr.getFieldValue(x)).doubleValue()).max().orElse(0);
			} else if ((attr.getFieldType() == double.class) || (attr.getFieldType() == Double.class)) {
				return stream.mapToDouble(x -> (Double) attr.getFieldValue(x)).max().orElse(0);
			}
			throw new RuntimeException("getNumberResult error(type:" + this.type + ", attr.declaringClass: " + attr.getDeclaringClass() + ", attr.field: " + attr.getFieldDefaultName() + ", attr.type: " + attr.getFieldType());

		case MIN:
			if ((attr.getFieldType() == int.class) || (attr.getFieldType() == Integer.class)) {
				return stream.mapToInt(x -> (Integer) attr.getFieldValue(x)).min().orElse(0);
			} else if ((attr.getFieldType() == long.class) || (attr.getFieldType() == Long.class)) {
				return stream.mapToLong(x -> (Long) attr.getFieldValue(x)).min().orElse(0);
			} else if ((attr.getFieldType() == short.class) || (attr.getFieldType() == Short.class)) {
				return (short) stream.mapToInt(x -> ((Short) attr.getFieldValue(x)).intValue()).min().orElse(0);
			} else if ((attr.getFieldType() == float.class) || (attr.getFieldType() == Float.class)) {
				return (float) stream.mapToDouble(x -> ((Float) attr.getFieldValue(x)).doubleValue()).min().orElse(0);
			} else if ((attr.getFieldType() == double.class) || (attr.getFieldType() == Double.class)) {
				return stream.mapToDouble(x -> (Double) attr.getFieldValue(x)).min().orElse(0);
			}
			throw new RuntimeException("getNumberResult error(type:" + this.type + ", attr.declaringClass: " + attr.getDeclaringClass() + ", attr.field: " + attr.getFieldDefaultName() + ", attr.type: " + attr.getFieldType());

		case SUM:
			if ((attr.getFieldType() == int.class) || (attr.getFieldType() == Integer.class)) {
				return stream.mapToInt(x -> (Integer) attr.getFieldValue(x)).sum();
			} else if ((attr.getFieldType() == long.class) || (attr.getFieldType() == Long.class)) {
				return stream.mapToLong(x -> (Long) attr.getFieldValue(x)).sum();
			} else if ((attr.getFieldType() == short.class) || (attr.getFieldType() == Short.class)) {
				return (short) stream.mapToInt(x -> ((Short) attr.getFieldValue(x)).intValue()).sum();
			} else if ((attr.getFieldType() == float.class) || (attr.getFieldType() == Float.class)) {
				return (float) stream.mapToDouble(x -> ((Float) attr.getFieldValue(x)).doubleValue()).sum();
			} else if ((attr.getFieldType() == double.class) || (attr.getFieldType() == Double.class)) {
				return stream.mapToDouble(x -> (Double) attr.getFieldValue(x)).sum();
			}
			throw new RuntimeException("getNumberResult error(type:" + this.type + ", attr.declaringClass: " + attr.getDeclaringClass() + ", attr.field: " + attr.getFieldDefaultName() + ", attr.type: " + attr.getFieldType());
		}
		return -1;
	}

	/**
	 * 根据FilterFunction和FilterBuild对指定列根据keyColumn从缓存中分组统计查询.
	 *
	 * @param <K> key类型
	 * @param <V> value类型
	 * @param keyColumn Entity类字段名
	 * @param filterFunction FilterFunction
	 * @param funcColumn Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Map
	 */
	public <K, V> Map<Serializable, Number> getMapResult(final String keyColumn, final FilterFunction filterFunction, final String funcColumn, final FilterBuild filterBuild) {
		final FieldAttribute<T, Serializable> keyAttr = this.info.getAttribute(keyColumn);
		final Predicate filter = filterBuild == null ? null : filterBuild.createPredicate(this);
		final FieldAttribute funcAttr = funcColumn == null ? null : this.info.getAttribute(funcColumn);
		Stream<T> stream = this.list.stream();
		if (filter != null) {
			stream = stream.filter(filter);
		}
		Collector<T, Map, ?> collector = null;
		final Class valtype = funcAttr == null ? null : funcAttr.getFieldType();
		switch (filterFunction) {
		case AVG:
			if ((valtype == float.class) || (valtype == Float.class) || (valtype == double.class) || (valtype == Double.class)) {
				collector = (Collector<T, Map, ?>) Collectors.averagingDouble((final T t) -> ((Number) funcAttr.getFieldValue(t)).doubleValue());
			} else {
				collector = (Collector<T, Map, ?>) Collectors.averagingLong((final T t) -> ((Number) funcAttr.getFieldValue(t)).longValue());
			}
			break;
		case COUNT:
			collector = (Collector<T, Map, ?>) Collectors.counting();
			break;
		case DISTINCTCOUNT:
			collector = (Collector<T, Map, ?>) Collectors.mapping((t) -> funcAttr.getFieldValue(t), Collectors.toSet());
			break;
		case MAX:
		case MIN:
			final Comparator<T> comp = (o1, o2) -> o1 == null ? (o2 == null ? 0 : -1) : ((Comparable) funcAttr.getFieldValue(o1)).compareTo(funcAttr.getFieldValue(o2));
			collector = (Collector<T, Map, ?>) ((filterFunction == FilterFunction.MAX) ? Collectors.maxBy(comp) : Collectors.minBy(comp));
			break;
		case SUM:
			if ((valtype == float.class) || (valtype == Float.class) || (valtype == double.class) || (valtype == Double.class)) {
				collector = (Collector<T, Map, ?>) Collectors.summingDouble((final T t) -> ((Number) funcAttr.getFieldValue(t)).doubleValue());
			} else {
				collector = (Collector<T, Map, ?>) Collectors.summingLong((final T t) -> ((Number) funcAttr.getFieldValue(t)).longValue());
			}
			break;
		}
		Map rs = stream.collect(Collectors.groupingBy(t -> keyAttr.getFieldValue(t), LinkedHashMap::new, collector));
		if ((filterFunction == FilterFunction.MAX) || (filterFunction == FilterFunction.MIN)) {
			final Map rs2 = new LinkedHashMap();
			rs.forEach((x, y) -> {
				if (((Optional) y).isPresent()) {
					rs2.put(x, funcAttr.getFieldValue(((Optional) y).get()));
				}
			});
			rs = rs2;
		} else if (filterFunction == FilterFunction.DISTINCTCOUNT) {
			final Map rs2 = new LinkedHashMap();
			rs.forEach((x, y) -> rs2.put(x, ((Set) y).size()));
			rs = rs2;
		}
		return rs;
	}

	/**
	 * 从缓存中获取数据页集合.
	 *
	 * @param selects SelectColumn
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 * @return PageData
	 */
	public PageData<T> queryPage(final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild) {
		return this.queryPage(true, selects, pageTurn, filterBuild);
	}

	/**
	 * 从缓存中获取数据页集合.
	 *
	 * @param needtotal boolean
	 * @param selects SelectColumn
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 * @return PageData
	 */
	public PageData<T> queryPage(final boolean needtotal, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final Predicate<T> filter = filterBuild == null ? null : filterBuild.createPredicate(this);
		final Comparator<T> comparator = this.createComparator(pageTurn);
		long total = 0;
		if (needtotal) {
			Stream<T> stream = this.list.stream();
			if (filter != null) {
				stream = stream.filter(filter);
			}
			total = stream.count();
		}
		if (needtotal && (total == 0)) {
			return new PageData();
		}
		Stream<T> stream = this.list.stream();
		if (filter != null) {
			stream = stream.filter(filter);
		}
		if (comparator != null) {
			stream = stream.sorted(comparator);
		}
		if (pageTurn != null) {
			stream = stream.skip(pageTurn.getOffset()).limit(pageTurn.getLimit());
		}
		final List<T> rs = new ArrayList<>();
		if (selects == null) {
			final Consumer<? super T> action = x -> rs.add(this.needcopy ? this.newReproduce.copy(this.creator.construct(), x) : x);
			if (comparator != null) {
				stream.forEachOrdered(action);
			} else {
				stream.forEach(action);
			}
		} else {
			final List<FieldAttribute<T, Serializable>> attrs = new ArrayList<>();
			this.info.forEachAttribute((k, v) -> {
				if (selects.test(k)) {
					attrs.add(v);
				}
			});
			final Consumer<? super T> action = x -> {
				final T item = this.creator.construct();
				for (final FieldAttribute attr : attrs) {
					attr.setFieldValue(item, attr.getFieldValue(x));
				}
				rs.add(item);
			};
			if (comparator != null) {
				stream.forEachOrdered(action);
			} else {
				stream.forEach(action);
			}
		}
		if (!needtotal) {
			total = rs.size();
		}
		return new PageData(total, rs);
	}

	/**
	 * 插入缓存.
	 *
	 * @param value T
	 */
	public void insert(final T value) {
		if (value == null) {
			return;
		}
		final T rs = this.newReproduce.copy(this.creator.construct(), value); // 确保同一主键值的map与list中的对象必须共用。
		final T old = this.map.put(this.primary.getFieldValue(rs), rs);
		if (old == null) {
			this.list.add(rs);
		}
	}

	/**
	 * 从缓存中删除.
	 *
	 * @param id Serializable
	 */
	public void delete(final Serializable id) {
		if (id == null) {
			return;
		}
		final T rs = this.map.remove(id);
		if (rs != null) {
			this.list.remove(rs);
		}
	}

	/**
	 * 根据FilterBuild从缓存中删除.
	 *
	 * @param filterBuild FilterBuild
	 * @return Serializable[]
	 */
	public Serializable[] delete(final FilterBuild filterBuild) {
		if ((filterBuild == null) || this.list.isEmpty()) {
			return new Serializable[0];
		}
		final Object[] rms = this.list.stream().filter(filterBuild.createPredicate(this)).toArray();
		final Serializable[] ids = new Serializable[rms.length];
		int i = -1;
		for (final Object o : rms) {
			final T t = (T) o;
			ids[++i] = this.primary.getFieldValue(t);
			this.map.remove(ids[i]);
			this.list.remove(t);
		}
		return ids;
	}

	/**
	 * 更新缓存.
	 *
	 * @param value T
	 */
	public void update(final T value) {
		if (value == null) {
			return;
		}
		final T rs = this.map.get(this.primary.getFieldValue(value));
		if (rs == null) {
			return;
		}
		this.chgReproduce.copy(rs, value);
	}

	/**
	 * 更新缓存.
	 *
	 * @param value T
	 * @param attrs Collection
	 * @return T
	 */
	public T update(final T value, final Collection<FieldAttribute<T, Serializable>> attrs) {
		if (value == null) {
			return value;
		}
		final T rs = this.map.get(this.primary.getFieldValue(value));
		if (rs == null) {
			return rs;
		}
		for (final FieldAttribute attr : attrs) {
			attr.setFieldValue(rs, attr.getFieldValue(value));
		}
		return rs;
	}

	/**
	 * 更新缓存.
	 *
	 * @param value T
	 * @param attrs Collection
	 * @param filterBuild FilterBuild
	 * @return T[]
	 */
	public T[] update(final T value, final Collection<FieldAttribute<T, Serializable>> attrs, final FilterBuild filterBuild) {
		if ((value == null) || (filterBuild == null)) {
			return (T[]) Array.newInstance(this.type, 0);
		}
		final T[] rms = this.list.stream().filter(filterBuild.createPredicate(this)).toArray(len -> (T[]) Array.newInstance(this.type, len));
		for (final T rs : rms) {
			for (final FieldAttribute attr : attrs) {
				attr.setFieldValue(rs, attr.getFieldValue(value));
			}
		}
		return rms;
	}

	/**
	 * 更新缓存.
	 *
	 * @param <V> value类型
	 * @param id Serializable
	 * @param attr FieldAttribute
	 * @param fieldValue V
	 * @return T
	 */
	public <V> T update(final Serializable id, final FieldAttribute<T, V> attr, final V fieldValue) {
		if (id == null) {
			return null;
		}
		final T rs = this.map.get(id);
		if (rs != null) {
			attr.setFieldValue(rs, fieldValue);
		}
		return rs;
	}

	/**
	 * 更新缓存.
	 *
	 * @param <V> value类型
	 * @param attr FieldAttribute
	 * @param fieldValue V
	 * @param filterBuild FilterBuild
	 * @return T[]
	 */
	public <V> T[] update(final FieldAttribute<T, V> attr, final V fieldValue, final FilterBuild filterBuild) {
		if ((attr == null) || (filterBuild == null)) {
			return (T[]) Array.newInstance(this.type, 0);
		}
		final T[] rms = this.list.stream().filter(filterBuild.createPredicate(this)).toArray(len -> (T[]) Array.newInstance(this.type, len));
		for (final T rs : rms) {
			attr.setFieldValue(rs, fieldValue);
		}
		return rms;
	}

	/**
	 * 根据字段获取FieldAttribute.
	 *
	 * @param fieldname String
	 * @return FieldAttribute
	 */
	public FieldAttribute<T, Serializable> getAttribute(final String fieldname) {
		return this.info.getAttribute(fieldname);
	}

	/**
	 * 根据PageTurn生成Comparator.
	 *
	 * @param pageTurn PageTurn
	 * @return Comparator
	 */
	protected Comparator<T> createComparator(final PageTurn pageTurn) {
		if ((pageTurn == null) || (pageTurn.getSort() == null) || pageTurn.getSort().isEmpty() || (pageTurn.getSort().indexOf(';') >= 0) || (pageTurn.getSort().indexOf('\n') >= 0)) {
			return null;
		}
		final String sort = pageTurn.getSort();
		Comparator<T> comparator = this.sortComparators.get(sort);
		if (comparator != null) {
			return comparator;
		}
		for (final String item : sort.split(",")) {
			if (item.trim().isEmpty()) {
				continue;
			}
			final String[] sub = item.trim().split("\\s+");
			final int pos = sub[0].indexOf('(');
			FieldAttribute<T, Serializable> attr;
			if (pos <= 0) {
				attr = this.getAttribute(sub[0]);
			} else { // 含SQL函数
				final int pos2 = sub[0].lastIndexOf(')');
				final FieldAttribute<T, Serializable> pattr = this.getAttribute(sub[0].substring(pos + 1, pos2));
				final String func = sub[0].substring(0, pos);
				if ("ABS".equalsIgnoreCase(func)) {
					Function getter = null;
					if ((pattr.getFieldType() == int.class) || (pattr.getFieldType() == Integer.class)) {
						getter = x -> Math.abs(((Number) pattr.getFieldValue((T) x)).intValue());
					} else if ((pattr.getFieldType() == long.class) || (pattr.getFieldType() == Long.class)) {
						getter = x -> Math.abs(((Number) pattr.getFieldValue((T) x)).longValue());
					} else if ((pattr.getFieldType() == float.class) || (pattr.getFieldType() == Float.class)) {
						getter = x -> Math.abs(((Number) pattr.getFieldValue((T) x)).floatValue());
					} else if ((pattr.getFieldType() == double.class) || (pattr.getFieldType() == Double.class)) {
						getter = x -> Math.abs(((Number) pattr.getFieldValue((T) x)).doubleValue());
					} else {
						throw new RuntimeException("Flipper not supported sort illegal type by ABS (" + pageTurn.getSort() + ")");
					}
					attr = (FieldAttribute<T, Serializable>) FieldAttribute.create(pattr.getDeclaringClass(), pattr.getFieldDefaultName(), pattr.getFieldType(), getter, (o, v) -> pattr.setFieldValue(o, v));
				} else if (func.isEmpty()) {
					attr = pattr;
				} else {
					throw new RuntimeException("PageTurn not supported sort illegal function (" + pageTurn.getSort() + ")");
				}
			}
			final Comparator<T> c = ((sub.length > 1) && sub[1].equalsIgnoreCase("DESC")) ? (final T o1, final T o2) -> {
				final Comparable c1 = (Comparable) attr.getFieldValue(o1);
				final Comparable c2 = (Comparable) attr.getFieldValue(o2);
				return c2 == null ? -1 : c2.compareTo(c1);
			} : (final T o1, final T o2) -> {
				final Comparable c1 = (Comparable) attr.getFieldValue(o1);
				final Comparable c2 = (Comparable) attr.getFieldValue(o2);
				return c1 == null ? -1 : c1.compareTo(c2);
			};
			if (comparator == null) {
				comparator = c;
			} else {
				comparator = comparator.thenComparing(c);
			}
		}
		this.sortComparators.put(sort, comparator);
		return comparator;
	}

}
