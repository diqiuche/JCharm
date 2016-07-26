/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.nio.channels.CompletionHandler;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数据库数据源.
 */
public interface DataSource {

	// ===================================================INSERT====================================================================

	/**
	 * 插入数据.
	 *
	 * @param <T> 泛型
	 * @param values Entity对象
	 */
	public <T> void insert(final T... values);

	/**
	 * 插入数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param values Entity对象
	 */
	public <T> void insert(final CompletionHandler<Void, T[]> handler, final T... values);

	// ===================================================DELETE====================================================================

	/**
	 * 删除数据.
	 *
	 * @param <T> 泛型
	 * @param values Entity对象
	 */
	public <T> void delete(final T... values);

	/**
	 * 根据主键值删除数据.
	 *
	 * @param <T> Entity类的泛型
	 * @param clazz Entity类
	 * @param ids 主键值
	 */
	public <T> void delete(final Class<T> clazz, final Serializable... ids);

	/**
	 * 根据FilterBuild删除数据.
	 *
	 * @param <T> Entity类的泛型
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T> void delete(final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 删除数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param values Entity对象
	 */
	public <T> void delete(final CompletionHandler<Void, T[]> handler, final T... values);

	/**
	 * 根据主键值删除数据(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param ids 主键值
	 */
	public <T> void delete(final CompletionHandler<Void, Serializable[]> handler, final Class<T> clazz, final Serializable... ids);

	/**
	 * 根据FilterBuild删除数据(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T> void delete(final CompletionHandler<Void, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild);

	// ===================================================UPDATE====================================================================

	/**
	 * 更新数据.
	 *
	 * @param <T> 泛型
	 * @param values Entity对象
	 */
	public <T> void update(final T... values);

	/**
	 * 根据主键值更新指定列的数据.
	 *
	 * @param <T> Entity类的泛型
	 * @param clazz Entity类
	 * @param id 主键值
	 * @param column Entity类字段名
	 * @param value Entity类字段值
	 */
	public <T> void updateColumn(final Class<T> clazz, final Serializable id, final String column, final Serializable value);

	/**
	 * 根据FilterBuild更新指定列的数据.
	 *
	 * @param <T> Entity类的泛型
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @param column Entity类字段名
	 * @param value Entity类字段值
	 */
	public <T> void updateColumn(final Class<T> clazz, final FilterBuild filterBuild, final String column, final Serializable value);

	/**
	 * 更新Entity对象指定的列.
	 *
	 * @param <T> Entity类泛型
	 * @param bean Entity对象
	 * @param columns Entity类的字段
	 */
	public <T> void updateColumns(final T bean, final String... columns);

	/**
	 * 根据FilterBuild更新Entity对象指定的列.
	 *
	 * @param <T> Entity类泛型
	 * @param bean Entity对象
	 * @param filterBuild FilterBuild
	 * @param columns Entity类的字段
	 */
	public <T> void updateColumns(final T bean, final FilterBuild filterBuild, final String... columns);

	/**
	 * 更新数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param values Entity对象
	 */
	public <T> void update(final CompletionHandler<Void, T[]> handler, final T... values);

	/**
	 * 根据主键值更新指定列的数据(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param id 主键值
	 * @param column Entity类字段名
	 * @param value Entity类字段值
	 */
	public <T> void updateColumn(final CompletionHandler<Void, Serializable> handler, final Class<T> clazz, final Serializable id, final String column, final Serializable value);

	/**
	 * 根据FilterBuild更新指定列的数据(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @param column Entity类字段名
	 * @param value Entity类字段值
	 */
	public <T> void updateColumn(final CompletionHandler<Void, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild, final String column, final Serializable value);

	/**
	 * 更新Entity对象指定的列(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param bean Entity对象
	 * @param columns Entity类的字段
	 */
	public <T> void updateColumns(final CompletionHandler<Void, T> handler, final T bean, final String... columns);

	/**
	 * 根据FilterBuild更新Entity对象指定的列(异步).
	 *
	 * @param <T> Entity类的泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param bean Entity对象
	 * @param filterBuild FilterBuild
	 * @param columns Entity类的字段
	 */
	public <T> void updateColumns(final CompletionHandler<Void, FilterBuild> handler, final T bean, final FilterBuild filterBuild, final String... columns);

	// ===================================================统计查询====================================================================

	/**
	 * 根据FilterFunction对指定列进行统计查询.
	 *
	 * @param entityClass Entity类
	 * @param filterFunction FilterFunction
	 * @param column Entity类字段名
	 * @return Number
	 */
	public Number getNumberResult(final Class entityClass, final FilterFunction filterFunction, final String column);

	/**
	 * 根据FilterFunction和FilterBuild对指定列进行统计查询.
	 *
	 * @param entityClass Entity类
	 * @param filterFunction FilterFunction
	 * @param column Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Number
	 */
	public Number getNumberResult(final Class entityClass, final FilterFunction filterFunction, final String column, final FilterBuild filterBuild);

	/**
	 * 根据FilterFunction对指定列根据keyColumn进行分组统计查询.
	 *
	 * @param <T> Entity类泛型
	 * @param <K> key类型
	 * @param <N> value类型
	 * @param entityClass Entity类
	 * @param keyColumn Entity类字段名
	 * @param filterFunction FilterFunction
	 * @param funcColumn Entity类字段名
	 * @return Map
	 */
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn);

	/**
	 * 根据FilterFunction和FilterBuild对指定列根据keyColumn进行分组统计查询.
	 *
	 * @param <T> Entity类泛型
	 * @param <K> key类型
	 * @param <N> value类型
	 * @param entityClass Entity类
	 * @param keyColumn Entity类字段名
	 * @param filterFunction FilterFunction
	 * @param funcColumn Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Map
	 */
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn, final FilterBuild filterBuild);

	/**
	 * 根据FilterFunction对指定列进行统计查询(异步).
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param entityClass Entity类
	 * @param filterFunction FilterFunction
	 * @param column Entity类字段名
	 * @return Number
	 */
	public void getNumberResult(final CompletionHandler<Number, String> handler, final Class entityClass, final FilterFunction filterFunction, final String column);

	/**
	 * 根据FilterFunction和FilterBuild对指定列进行统计查询(异步).
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param entityClass Entity类
	 * @param filterFunction FilterFunction
	 * @param column Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Number
	 */
	public void getNumberResult(final CompletionHandler<Number, FilterBuild> handler, final Class entityClass, final FilterFunction filterFunction, final String column, final FilterBuild filterBuild);

	/**
	 * 根据FilterFunction对指定列根据keyColumn进行分组统计查询(异步).
	 *
	 * @param <T> Entity类泛型
	 * @param <K> key类型
	 * @param <N> value类型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param entityClass Entity类
	 * @param keyColumn Entity类字段名
	 * @param filterFunction FilterFunction
	 * @param funcColumn Entity类字段名
	 * @return Map
	 */
	public <T, K extends Serializable, N extends Number> void getMapResult(final CompletionHandler<Map<K, N>, String> handler, final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn);

	/**
	 * 根据FilterFunction和FilterBuild对指定列根据keyColumn进行分组统计查询(异步).
	 *
	 * @param <T> Entity类泛型
	 * @param <K> key类型
	 * @param <N> value类型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param entityClass Entity类
	 * @param keyColumn Entity类字段名
	 * @param filterFunction FilterFunction
	 * @param funcColumn Entity类字段名
	 * @param filterBuild FilterBuild
	 * @return Map
	 */
	public <T, K extends Serializable, N extends Number> void getMapResult(final CompletionHandler<Map<K, N>, FilterBuild> handler, final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn, final FilterBuild filterBuild);

	// ===================================================FIND====================================================================

	/**
	 * 根据主键值获取数据.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param pk 主键值
	 * @return Entity对象
	 */
	public <T> T find(final Class<T> clazz, final Serializable pk);

	/**
	 * 根据主键值结合SelectColumn获取数据.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param pk 主键值
	 * @return Entity对象
	 */
	public <T> T find(final Class<T> clazz, final SelectColumn selects, final Serializable pk);

	/**
	 * 根据列名及列值获取数据.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param column Entity类字段名
	 * @param key Entity类字段值
	 * @return Entity对象
	 */
	public <T> T find(final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取数据.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @return Entity对象
	 */
	public <T> T find(final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param filterBuild FilterBuild
	 * @return Entity对象
	 */
	public <T> T find(final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild);

	/**
	 * 指定主键值的数据是否存在.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param pk 主键值
	 * @return boolean
	 */
	public <T> boolean exists(final Class<T> clazz, final Serializable pk);

	/**
	 * 根据FilterBuild判断数据是否存在.
	 *
	 * @param <T> 泛型
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @return boolean
	 */
	public <T> boolean exists(final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据主键值获取数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param pk 主键值
	 */
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final Serializable pk);

	/**
	 * 根据主键值结合SelectColumn获取数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param pk 主键值
	 */
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final SelectColumn selects, final Serializable pk);

	/**
	 * 根据列名及列值获取数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param column Entity类字段名
	 * @param key Entity类字段值
	 */
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T> void find(final CompletionHandler<T, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param filterBuild FilterBuild
	 */
	public <T> void find(final CompletionHandler<T, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild);

	/**
	 * 指定主键值的数据是否存在(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param pk 主键值
	 */
	public <T> void exists(final CompletionHandler<Boolean, Serializable> handler, final Class<T> clazz, final Serializable pk);

	/**
	 * 根据FilterBuild判断数据是否存在(异步).
	 *
	 * @param <T> 泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T> void exists(final CompletionHandler<Boolean, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild);

	// ===================================================List Column====================================================================

	/**
	 * 根据指定列和值获取某个列的集合.
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段类型
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param column Entity类的字段名
	 * @param key Entity类的字段值
	 * @return List
	 */
	public <T, V extends Serializable> List<V> queryColumnList(final String selectedColumn, final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取某个列的集合.
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段的数据类型
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @return List
	 */
	public <T, V extends Serializable> List<V> queryColumnList(final String selectedColumn, final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据指定列和值获取某个列的集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段数据类型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param column Entity类的字段名
	 * @param key Entity类的字段值
	 */
	public <T, V extends Serializable> void queryColumnList(final CompletionHandler<List<V>, Serializable> handler, final String selectedColumn, final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取某个列的集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段数据类型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T, V extends Serializable> void queryColumnList(final CompletionHandler<List<V>, FilterBuild> handler, final String selectedColumn, final Class<T> clazz, final FilterBuild filterBuild);

	// ===================================================Page Column====================================================================

	/**
	 * 根据FilterBuild获取某个列的页集合.
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段类型
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 * @return PageData
	 */
	public <T, V extends Serializable> PageData<V> queryColumnPage(final String selectedColumn, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild获取某个列的页集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param <V> 字段类型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param selectedColumn Entity类的字段名
	 * @param clazz Entity类
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 */
	public <T, V extends Serializable> void queryColumnPage(final CompletionHandler<PageData<V>, FilterBuild> handler, final String selectedColumn, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild);

	// ===================================================List====================================================================

	/**
	 * 根据指定列和值获取数据集合.
	 *
	 * @param <T> Entity泛型
	 * @param clazz Entity类
	 * @param column Entity类字段名
	 * @param key Entity类字段值
	 * @return List
	 */
	public <T> List<T> queryList(final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取数据集合.
	 *
	 * @param <T> Entity泛型
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 * @return List
	 */
	public <T> List<T> queryList(final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据集合.
	 *
	 * @param <T> Entity泛型
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param filterBuild FilterBuild
	 * @return List
	 */
	public <T> List<T> queryList(final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild);

	/**
	 * 根据指定列和值获取数据集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param column Entity类字段名
	 * @param key Entity类字段值
	 */
	public <T> void queryList(final CompletionHandler<List<T>, Serializable> handler, final Class<T> clazz, final String column, final Serializable key);

	/**
	 * 根据FilterBuild获取数据集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param filterBuild FilterBuild
	 */
	public <T> void queryList(final CompletionHandler<List<T>, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param filterBuild FilterBuild
	 */
	public <T> void queryList(final CompletionHandler<List<T>, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild);

	// ===================================================Page====================================================================

	/**
	 * 根据FilterBuild获取数据页集合.
	 *
	 * @param <T> Entity泛型
	 * @param clazz Entity类
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 * @return PageData
	 */
	public <T> PageData<T> queryPage(final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据页集合.
	 *
	 * @param <T> Entity泛型
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 * @return PageData
	 */
	public <T> PageData<T> queryPage(final Class<T> clazz, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild获取数据页集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 */
	public <T> void queryPage(final CompletionHandler<PageData<T>, FilterBuild> handler, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild);

	/**
	 * 根据FilterBuild和SelectColumn获取数据页集合(异步).
	 *
	 * @param <T> Entity泛型
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param clazz Entity类
	 * @param selects SelectColumn
	 * @param pageTurn PageTurn
	 * @param filterBuild FilterBuild
	 */
	public <T> void queryPage(final CompletionHandler<PageData<T>, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild);

	// ===================================================DIRECT====================================================================

	/**
	 * 直接本地执行SQL语句进行查询.
	 *
	 * @param sql SQL语句
	 * @param consumer 回调函数
	 */
	public void directQuery(String sql, final Consumer<ResultSet> consumer);

	/**
	 * 直接本地执行SQL语句进行增删改操作.
	 *
	 * @param sqls SQL语句
	 * @return 结果数组
	 */
	public int[] directExecute(String... sqls);

}
