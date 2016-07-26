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
import java.util.function.Function;

/**
 * 默认的数据库数据源实现类.
 */
public final class DataDefaultSource implements DataSource, Function<Class, EntityInfo>, AutoCloseable {

	/** DATASOURCE_CONFPATH. */
	public static final String DATASOURCE_CONFPATH = "DATASOURCE_CONFPATH";

	/** JDBC_CONNECTIONSMAX. */
	static final String JDBC_CONNECTIONSMAX = "javax.persistence.connections.limit";

	/** The JDBC_CONTAIN_SQLTEMPLATE. */
	static final String JDBC_CONTAIN_SQLTEMPLATE = "javax.persistence.contain.sqltemplate";

	/** The JDBC_NOTCONTAIN_SQLTEMPLATE. */
	static final String JDBC_NOTCONTAIN_SQLTEMPLATE = "javax.persistence.notcontain.sqltemplate";

	/** JDBC_URL. */
	static final String JDBC_URL = "javax.persistence.jdbc.url";

	/** JDBC_USER. */
	static final String JDBC_USER = "javax.persistence.jdbc.user";

	/** JDBC_PWD. */
	static final String JDBC_PWD = "javax.persistence.jdbc.password";

	/** JDBC_DRIVER. */
	static final String JDBC_DRIVER = "javax.persistence.jdbc.driver";

	/** JDBC_SOURCE. */
	static final String JDBC_SOURCE = "javax.persistence.jdbc.source";

	@Override
	public void close() throws Exception {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public EntityInfo apply(Class t) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> void insert(T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void insert(CompletionHandler<Void, T[]> handler, T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(Class<T> clazz, Serializable... ids) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(CompletionHandler<Void, T[]> handler, T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(CompletionHandler<Void, Serializable[]> handler, Class<T> clazz, Serializable... ids) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void delete(CompletionHandler<Void, FilterBuild> handler, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void update(T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumn(Class<T> clazz, Serializable id, String column, Serializable value) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumn(Class<T> clazz, FilterBuild filterBuild, String column, Serializable value) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumns(T bean, String... columns) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumns(T bean, FilterBuild filterBuild, String... columns) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void update(CompletionHandler<Void, T[]> handler, T... values) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumn(CompletionHandler<Void, Serializable> handler, Class<T> clazz, Serializable id, String column, Serializable value) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumn(CompletionHandler<Void, FilterBuild> handler, Class<T> clazz, FilterBuild filterBuild, String column, Serializable value) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumns(CompletionHandler<Void, T> handler, T bean, String... columns) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void updateColumns(CompletionHandler<Void, FilterBuild> handler, T bean, FilterBuild filterBuild, String... columns) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public Number getNumberResult(Class entityClass, FilterFunction filterFunction, String column) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public Number getNumberResult(Class entityClass, FilterFunction filterFunction, String column, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(Class<T> entityClass, String keyColumn, FilterFunction filterFunction, String funcColumn) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(Class<T> entityClass, String keyColumn, FilterFunction filterFunction, String funcColumn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public void getNumberResult(CompletionHandler<Number, String> handler, Class entityClass, FilterFunction filterFunction, String column) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public void getNumberResult(CompletionHandler<Number, FilterBuild> handler, Class entityClass, FilterFunction filterFunction, String column, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T, K extends Serializable, N extends Number> void getMapResult(CompletionHandler<Map<K, N>, String> handler, Class<T> entityClass, String keyColumn, FilterFunction filterFunction, String funcColumn) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T, K extends Serializable, N extends Number> void getMapResult(CompletionHandler<Map<K, N>, FilterBuild> handler, Class<T> entityClass, String keyColumn, FilterFunction FilterFunction, String funcColumn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> T find(Class<T> clazz, Serializable pk) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> T find(Class<T> clazz, SelectColumn selects, Serializable pk) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> T find(Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> T find(Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> T find(Class<T> clazz, SelectColumn selects, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> boolean exists(Class<T> clazz, Serializable pk) {
		// XXX 自动生成的方法存根
		return false;
	}

	@Override
	public <T> boolean exists(Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return false;
	}

	@Override
	public <T> void find(CompletionHandler<T, Serializable> handler, Class<T> clazz, Serializable pk) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void find(CompletionHandler<T, Serializable> handler, Class<T> clazz, SelectColumn selects, Serializable pk) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void find(CompletionHandler<T, Serializable> handler, Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void find(CompletionHandler<T, FilterBuild> handler, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void find(CompletionHandler<T, FilterBuild> handler, Class<T> clazz, SelectColumn selects, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void exists(CompletionHandler<Boolean, Serializable> handler, Class<T> clazz, Serializable pk) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void exists(CompletionHandler<Boolean, FilterBuild> handler, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T, V extends Serializable> List<V> queryColumnList(String selectedColumn, Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T, V extends Serializable> List<V> queryColumnList(String selectedColumn, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T, V extends Serializable> void queryColumnList(CompletionHandler<List<V>, Serializable> handler, String selectedColumn, Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T, V extends Serializable> void queryColumnList(CompletionHandler<List<V>, FilterBuild> handler, String selectedColumn, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T, V extends Serializable> PageData<V> queryColumnPage(String selectedColumn, Class<T> clazz, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T, V extends Serializable> void queryColumnPage(CompletionHandler<PageData<V>, FilterBuild> handler, String selectedColumn, Class<T> clazz, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> List<T> queryList(Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> List<T> queryList(Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> List<T> queryList(Class<T> clazz, SelectColumn selects, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> void queryList(CompletionHandler<List<T>, Serializable> handler, Class<T> clazz, String column, Serializable key) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void queryList(CompletionHandler<List<T>, FilterBuild> handler, Class<T> clazz, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void queryList(CompletionHandler<List<T>, FilterBuild> handler, Class<T> clazz, SelectColumn selects, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> PageData<T> queryPage(Class<T> clazz, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> PageData<T> queryPage(Class<T> clazz, SelectColumn selects, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		return null;
	}

	@Override
	public <T> void queryPage(CompletionHandler<PageData<T>, FilterBuild> handler, Class<T> clazz, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public <T> void queryPage(CompletionHandler<PageData<T>, FilterBuild> handler, Class<T> clazz, SelectColumn selects, PageTurn pageTurn, FilterBuild filterBuild) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public void directQuery(String sql, Consumer<ResultSet> consumer) {
		// XXX 自动生成的方法存根
		
	}

	@Override
	public int[] directExecute(String... sqls) {
		// XXX 自动生成的方法存根
		return null;
	}

}
