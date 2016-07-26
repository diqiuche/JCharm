/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.CompletionHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.sql.ConnectionPoolDataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import io.github.jcharm.common.FieldAttribute;

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

	/** The Constant PAGETURN_ONE. */
	static final PageTurn PAGETURN_ONE = new PageTurn(1);

	/** The logger. */
	final Logger logger = Logger.getLogger(DataDefaultSource.class.getSimpleName());

	/** The debug. */
	final AtomicBoolean debug = new AtomicBoolean(this.logger.isLoggable(Level.FINE));

	/** The name. */
	final String name;

	/** The conf. */
	final URL conf;

	/** The cache forbidden. */
	final boolean cacheForbidden;

	private final JDBCPoolSource readPool;

	private final JDBCPoolSource writePool;

	@Resource(name = "property.datasource.nodeid")
	private int nodeid;

	@Resource(name = "$")
	private DataSQLListener writeListener;

	@Resource(name = "$")
	private DataCacheListener cacheListener;

	private final Function<Class, List> fullloader = (t) -> this.queryPage(false, false, t, null, null, (FilterBuild) null).list(true);

	/**
	 * Instantiates a new data default source.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DataDefaultSource() throws IOException {
		this("");
	}

	/**
	 * Instantiates a new data default source.
	 *
	 * @param unitName the unit name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DataDefaultSource(final String unitName) throws IOException {
		this(unitName, System.getProperty(DataDefaultSource.DATASOURCE_CONFPATH) == null ? DataDefaultSource.class.getResource("/META-INF/persistence.xml") : new File(System.getProperty(DataDefaultSource.DATASOURCE_CONFPATH)).toURI().toURL());
	}

	/**
	 * Instantiates a new data default source.
	 *
	 * @param unitName the unit name
	 * @param url the url
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DataDefaultSource(final String unitName, URL url) throws IOException {
		if (url == null) {
			url = this.getClass().getResource("/persistence.xml");
		}
		final InputStream in = url.openStream();
		final Map<String, Properties> map = DataDefaultSource.loadProperties(in);
		Properties readprop = null;
		Properties writeprop = null;
		if (unitName != null) {
			readprop = map.get(unitName);
			writeprop = readprop;
			if (readprop == null) {
				readprop = map.get(unitName + ".read");
				writeprop = map.get(unitName + ".write");
			}
		}
		if (((unitName == null) || unitName.isEmpty()) || (readprop == null)) {
			String key = null;
			for (final Map.Entry<String, Properties> en : map.entrySet()) {
				key = en.getKey();
				readprop = en.getValue();
				writeprop = readprop;
				break;
			}
			if ((key != null) && (key.endsWith(".read") || key.endsWith(".write"))) {
				if (key.endsWith(".read")) {
					writeprop = map.get(key.substring(0, key.lastIndexOf('.')) + ".write");
				} else {
					readprop = map.get(key.substring(0, key.lastIndexOf('.')) + ".read");
				}
			}
		}
		if (readprop == null) {
			throw new RuntimeException("not found persistence properties (unit:" + unitName + ")");
		}
		this.name = unitName;
		this.conf = url;
		this.readPool = new JDBCPoolSource(this, "read", readprop);
		this.writePool = new JDBCPoolSource(this, "write", writeprop);
		this.cacheForbidden = "NONE".equalsIgnoreCase(readprop.getProperty("shared-cache-mode"));
	}

	/**
	 * Instantiates a new data default source.
	 *
	 * @param unitName the unit name
	 * @param readprop the readprop
	 * @param writeprop the writeprop
	 */
	public DataDefaultSource(final String unitName, final Properties readprop, final Properties writeprop) {
		this.name = unitName;
		this.conf = null;
		this.readPool = new JDBCPoolSource(this, "read", readprop);
		this.writePool = new JDBCPoolSource(this, "write", writeprop);
		this.cacheForbidden = "NONE".equalsIgnoreCase(readprop.getProperty("shared-cache-mode"));
	}

	/**
	 * Creates the.
	 *
	 * @param in the in
	 * @return the map
	 */
	public static Map<String, DataDefaultSource> create(final InputStream in) {
		final Map<String, Properties> map = DataDefaultSource.loadProperties(in);
		final Map<String, Properties[]> maps = new HashMap<>();
		map.entrySet().stream().forEach((en) -> {
			if (en.getKey().endsWith(".read") || en.getKey().endsWith(".write")) {
				final String key = en.getKey().substring(0, en.getKey().lastIndexOf('.'));
				if (maps.containsKey(key)) {
					return;
				}
				final boolean read = en.getKey().endsWith(".read");
				final Properties rp = read ? en.getValue() : map.get(key + ".read");
				final Properties wp = read ? map.get(key + ".write") : en.getValue();
				maps.put(key, new Properties[] { rp, wp });
			} else {
				maps.put(en.getKey(), new Properties[] { en.getValue(), en.getValue() });
			}
		});
		final Map<String, DataDefaultSource> result = new HashMap<>();
		maps.entrySet().stream().forEach((en) -> {
			result.put(en.getKey(), new DataDefaultSource(en.getKey(), en.getValue()[0], en.getValue()[1]));
		});
		return result;
	}

	/**
	 * Load properties.
	 *
	 * @param in0 the in0
	 * @return the map
	 */
	static Map<String, Properties> loadProperties(final InputStream in0) {
		final Map<String, Properties> map = new LinkedHashMap();
		Properties result = new Properties();
		boolean flag = false;
		try (final InputStream in = in0) {
			final XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(in);
			while (reader.hasNext()) {
				final int event = reader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					if ("persistence-unit".equalsIgnoreCase(reader.getLocalName())) {
						if (!result.isEmpty()) {
							result = new Properties();
						}
						map.put(reader.getAttributeValue(null, "name"), result);
						flag = true;
					} else if (flag && "property".equalsIgnoreCase(reader.getLocalName())) {
						final String name = reader.getAttributeValue(null, "name");
						final String value = reader.getAttributeValue(null, "value");
						if (name == null) {
							continue;
						}
						result.put(name, value);
					} else if (flag && "shared-cache-mode".equalsIgnoreCase(reader.getLocalName())) {
						result.put(reader.getLocalName(), reader.getElementText());
					}
				}
			}
			in.close();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
		return map;
	}

	/**
	 * Creates the data source.
	 *
	 * @param property the property
	 * @return the connection pool data source
	 */
	static ConnectionPoolDataSource createDataSource(final Properties property) {
		try {
			return DataDefaultSource.createDataSource(property.getProperty(DataDefaultSource.JDBC_SOURCE, property.getProperty(DataDefaultSource.JDBC_DRIVER)), property.getProperty(DataDefaultSource.JDBC_URL), property.getProperty(DataDefaultSource.JDBC_USER),
					property.getProperty(DataDefaultSource.JDBC_PWD));
		} catch (final Exception ex) {
			throw new RuntimeException("(" + property + ") have no jdbc parameters", ex);
		}
	}

	/**
	 * Creates the data source.
	 *
	 * @param source0 the source0
	 * @param url the url
	 * @param user the user
	 * @param password the password
	 * @return the connection pool data source
	 * @throws Exception the exception
	 */
	static ConnectionPoolDataSource createDataSource(final String source0, final String url, final String user, final String password) throws Exception {
		String source = source0;
		if (source0.contains("Driver")) { // 为了兼容JPA的配置文件
			switch (source0) {
			case "org.mariadb.jdbc.Driver":
				source = "org.mariadb.jdbc.MySQLDataSource";
				break;
			case "com.mysql.jdbc.Driver":
				source = "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource";
				break;
			case "oracle.jdbc.driver.OracleDriver":
				source = "oracle.jdbc.pool.OracleConnectionPoolDataSource";
				break;
			case "com.microsoft.sqlserver.jdbc.SQLServerDriver":
				source = "com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource";
				break;
			}
		}
		final Class clazz = Class.forName(source);
		final Object pdsource = clazz.newInstance();
		Method seturlm;
		try {
			seturlm = clazz.getMethod("setUrl", String.class);
		} catch (final Exception e) {
			seturlm = clazz.getMethod("setURL", String.class);
		}
		seturlm.invoke(pdsource, url);
		clazz.getMethod("setUser", String.class).invoke(pdsource, user);
		clazz.getMethod("setPassword", String.class).invoke(pdsource, password);
		return (ConnectionPoolDataSource) pdsource;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	private Connection createReadSQLConnection() {
		return this.readPool.poll();
	}

	private <T> Connection createWriteSQLConnection() {
		return this.writePool.poll();
	}

	private void closeSQLConnection(final Connection sqlconn) {
		if (sqlconn == null) {
			return;
		}
		try {
			sqlconn.close();
		} catch (final Exception e) {
			this.logger.log(Level.WARNING, "closeSQLConnection abort", e);
		}
	}

	@Override
	public void close() throws Exception {
		this.readPool.close();
		this.writePool.close();
	}

	/**
	 * 将entity的对象全部加载到Cache中去.
	 *
	 * @param <T> Entity类泛型
	 * @param clazz Entity类
	 */
	public <T> void refreshCache(final Class<T> clazz) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache == null) {
			return;
		}
		cache.fullLoad();
	}

	private <T> EntityInfo<T> loadEntityInfo(final Class<T> clazz) {
		return EntityInfo.load(clazz, this.nodeid, this.cacheForbidden, this.readPool.props, this.fullloader);
	}

	@Override
	public EntityInfo apply(final Class t) {
		return this.loadEntityInfo(t);
	}

	@Override
	public <T> void insert(final T... values) {
		if (values.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo((Class<T>) values[0].getClass());
		if (info.isVirtualEntity()) {
			this.insert(null, info, values);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.insert(conn, info, values);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void insert(final CompletionHandler<Void, T[]> handler, final T... values) {
		this.insert(values);
		if (handler != null) {
			handler.completed(null, values);
		}
	}

	private <T> void insert(final Connection conn, final EntityInfo<T> info, final T... values) {
		if (values.length == 0) {
			return;
		}
		try {
			if (!info.isVirtualEntity()) {
				final String sql = info.insertSQL;
				final PreparedStatement prestmt = info.autoGenerated ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql);
				final Class primaryType = info.getPrimary().getFieldType();
				final FieldAttribute primary = info.getPrimary();
				final boolean distributed = info.distributed;
				final FieldAttribute<T, Serializable>[] attrs = info.insertAttributes;
				String[] sqls = null;
				if (distributed && !info.initedPrimaryValue && primaryType.isPrimitive()) { // 由DataSource生成主键
					synchronized (info) {
						if (!info.initedPrimaryValue) { // 初始化最大主键值
							Statement stmt = conn.createStatement();
							ResultSet rs = stmt.executeQuery("SELECT MAX(" + info.getPrimarySQLColumn() + ") FROM " + info.getTable());
							if (rs.next()) {
								if (primaryType == int.class) {
									final int v = rs.getInt(1) / info.allocationSize;
									if (v > info.primaryValue.get()) {
										info.primaryValue.set(v);
									}
								} else {
									final long v = rs.getLong(1) / info.allocationSize;
									if (v > info.primaryValue.get()) {
										info.primaryValue.set(v);
									}
								}
							}
							rs.close();
							stmt.close();
							if (info.distributeTables != null) { // 是否还有其他表
								for (final Class t : info.distributeTables) {
									final EntityInfo<T> infox = this.loadEntityInfo(t);
									stmt = conn.createStatement();
									rs = stmt.executeQuery("SELECT MAX(" + info.getPrimarySQLColumn() + ") FROM " + infox.getTable()); // 必须是同一字段名
									if (rs.next()) {
										if (primaryType == int.class) {
											final int v = rs.getInt(1) / info.allocationSize;
											if (v > info.primaryValue.get()) {
												info.primaryValue.set(v);
											}
										} else {
											final long v = rs.getLong(1) / info.allocationSize;
											if (v > info.primaryValue.get()) {
												info.primaryValue.set(v);
											}
										}
									}
									rs.close();
									stmt.close();
								}
							}
							info.initedPrimaryValue = true;
						}
					}
				}
				if (this.writeListener == null) {
					for (final T value : values) {
						int i = 0;
						if (distributed) {
							info.createPrimaryValue(value);
						}
						for (final FieldAttribute<T, Serializable> attr : attrs) {
							prestmt.setObject(++i, attr.getFieldValue(value));
						}
						prestmt.addBatch();
					}
				} else { // 调用writeListener回调接口
					final char[] sqlchars = sql.toCharArray();
					sqls = new String[values.length];
					final CharSequence[] ps = new CharSequence[attrs.length];
					int index = 0;
					for (final T value : values) {
						int i = 0;
						if (distributed) {
							info.createPrimaryValue(value);
						}
						for (final FieldAttribute<T, Serializable> attr : attrs) {
							final Object a = attr.getFieldValue(value);
							ps[i] = FilterBuild.formatToString(a);
							prestmt.setObject(++i, a);
						}
						prestmt.addBatch();
						final StringBuilder sb = new StringBuilder(128);
						i = 0;
						for (final char ch : sqlchars) {
							if (ch == '?') {
								sb.append(ps[i++]);
							} else {
								sb.append(ch);
							}
						}
						sqls[index++] = sb.toString();
					}
				}
				prestmt.executeBatch();
				if (this.writeListener != null) {
					this.writeListener.insert(sqls);
				}
				if (info.autoGenerated) { // 由数据库自动生成主键值
					final ResultSet set = prestmt.getGeneratedKeys();
					int i = -1;
					while (set.next()) {
						if (primaryType == int.class) {
							primary.setFieldValue(values[++i], set.getInt(1));
						} else if (primaryType == long.class) {
							primary.setFieldValue(values[++i], set.getLong(1));
						} else {
							primary.setFieldValue(values[++i], set.getObject(1));
						}
					}
					set.close();
				}
				prestmt.close();
				if (this.debug.get()) { // 打印调试信息
					final char[] sqlchars = sql.toCharArray();
					for (final T value : values) {
						final StringBuilder sb = new StringBuilder(128);
						int i = 0;
						for (final char ch : sqlchars) {
							if (ch == '?') {
								final Object obj = attrs[i++].getFieldValue(value);
								if ((obj != null) && obj.getClass().isArray()) {
									sb.append("'[length=").append(java.lang.reflect.Array.getLength(obj)).append("]'");
								} else {
									sb.append(FilterBuild.formatToString(obj));
								}
							} else {
								sb.append(ch);
							}
						}
						this.logger.fine(info.getType().getSimpleName() + " insert sql=" + sb.toString().replaceAll("(\r|\n)", "\\n"));
					}
				} // 打印结束
			}
			final EntityCache<T> cache = info.getCache();
			if (cache != null) { // 更新缓存
				for (final T value : values) {
					cache.insert(value);
				}
				if (this.cacheListener != null) {
					this.cacheListener.insertCache(info.getType(), values);
				}
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Insert cache.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param values the values
	 */
	public <T> void insertCache(final Class<T> clazz, final T... values) {
		if (values.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache == null) {
			return;
		}
		for (final T value : values) {
			cache.insert(value);
		}
	}

	@Override
	public <T> void delete(final T... values) {
		if (values.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo((Class<T>) values[0].getClass());
		if (info.isVirtualEntity()) { // 虚拟表只更新缓存Cache
			this.delete(null, info, values);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.delete(conn, info, values);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void delete(final Class<T> clazz, final Serializable... ids) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		if (info.isVirtualEntity()) { // 虚拟表只更新缓存Cache
			this.delete(null, info, ids);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.delete(conn, info, ids);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void delete(final Class<T> clazz, final FilterBuild filterBuild) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		if (info.isVirtualEntity()) {
			this.delete(null, info, filterBuild);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.delete(conn, info, filterBuild);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void delete(final CompletionHandler<Void, T[]> handler, final T... values) {
		this.delete(values);
		if (handler != null) {
			handler.completed(null, values);
		}
	}

	@Override
	public <T> void delete(final CompletionHandler<Void, Serializable[]> handler, final Class<T> clazz, final Serializable... ids) {
		this.delete(clazz, ids);
		if (handler != null) {
			handler.completed(null, ids);
		}
	}

	@Override
	public <T> void delete(final CompletionHandler<Void, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild) {
		this.delete(clazz, filterBuild);
		if (handler != null) {
			handler.completed(null, filterBuild);
		}
	}

	private <T> void delete(final Connection conn, final EntityInfo<T> info, final T... values) {
		if (values.length == 0) {
			return;
		}
		final FieldAttribute primary = info.getPrimary();
		final Serializable[] ids = new Serializable[values.length];
		int i = 0;
		for (final T value : values) {
			ids[i++] = (Serializable) primary.getFieldValue(value);
		}
		this.delete(conn, info, ids);
	}

	private <T> void delete(final Connection conn, final EntityInfo<T> info, final Serializable... keys) {
		if (keys.length == 0) {
			return;
		}
		try {
			if (!info.isVirtualEntity()) {
				final String sql = "DELETE FROM " + info.getTable() + " WHERE " + info.getPrimarySQLColumn() + " IN " + FilterBuild.formatToString(keys);
				if (this.debug.get()) {
					this.logger.fine(info.getType().getSimpleName() + " delete sql=" + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.delete(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			for (final Serializable key : keys) {
				cache.delete(key);
			}
			if (this.cacheListener != null) {
				this.cacheListener.deleteCache(info.getType(), keys);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void delete(final Connection conn, final EntityInfo<T> info, final FilterBuild filterBuild) {
		try {
			if (!info.isVirtualEntity()) {
				final Map<Class, String> joinTabalis = filterBuild.getJoinTabalis();
				final CharSequence join = filterBuild.createSQLJoin(this, joinTabalis, info);
				final CharSequence where = filterBuild.createSQLExpress(info, joinTabalis);
				final String sql = "DELETE " + (this.readPool.isMysql() ? "a" : "") + " FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
				if (this.debug.get()) {
					this.logger.fine(info.getType().getSimpleName() + " delete sql=" + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.delete(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			final Serializable[] ids = cache.delete(filterBuild);
			if (this.cacheListener != null) {
				this.cacheListener.deleteCache(info.getType(), ids);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete cache.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param ids the ids
	 */
	public <T> void deleteCache(final Class<T> clazz, final Serializable... ids) {
		if (ids.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache == null) {
			return;
		}
		for (final Serializable id : ids) {
			cache.delete(id);
		}
	}

	@Override
	public <T> void update(final T... values) {
		if (values.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo((Class<T>) values[0].getClass());
		if (info.isVirtualEntity()) {
			this.update(null, info, values);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.update(conn, info, values);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void updateColumn(final Class<T> clazz, final Serializable id, final String column, final Serializable value) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		if (info.isVirtualEntity()) {
			this.updateColumn(null, info, id, column, value);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.updateColumn(conn, info, id, column, value);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void updateColumn(final Class<T> clazz, final FilterBuild filterBuild, final String column, final Serializable value) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		if (info.isVirtualEntity()) {
			this.updateColumn(null, info, column, value, filterBuild);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.updateColumn(conn, info, column, value, filterBuild);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void updateColumns(final T bean, final String... columns) {
		final EntityInfo<T> info = this.loadEntityInfo((Class<T>) bean.getClass());
		if (info.isVirtualEntity()) {
			this.updateColumns(null, info, bean, columns);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.updateColumns(conn, info, bean, columns);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void updateColumns(final T bean, final FilterBuild filterBuild, final String... columns) {
		final EntityInfo<T> info = this.loadEntityInfo((Class<T>) bean.getClass());
		if (info.isVirtualEntity()) {
			this.updateColumns(null, info, bean, filterBuild, columns);
			return;
		}
		final Connection conn = this.createWriteSQLConnection();
		try {
			this.updateColumns(conn, info, bean, filterBuild, columns);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void update(final CompletionHandler<Void, T[]> handler, final T... values) {
		this.update(values);
		if (handler != null) {
			handler.completed(null, values);
		}
	}

	@Override
	public <T> void updateColumn(final CompletionHandler<Void, Serializable> handler, final Class<T> clazz, final Serializable id, final String column, final Serializable value) {
		this.updateColumn(clazz, id, column, value);
		if (handler != null) {
			handler.completed(null, id);
		}
	}

	@Override
	public <T> void updateColumn(final CompletionHandler<Void, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild, final String column, final Serializable value) {
		this.updateColumn(clazz, filterBuild, column, value);
		if (handler != null) {
			handler.completed(null, filterBuild);
		}
	}

	private <T> void updateColumn(final Connection conn, final EntityInfo<T> info, final String column, final Serializable value, final FilterBuild filterBuild) {
		try {
			if (!info.isVirtualEntity()) {
				final Map<Class, String> joinTabalis = filterBuild.getJoinTabalis();
				final CharSequence join = filterBuild.createSQLJoin(this, joinTabalis, info);
				final CharSequence where = filterBuild.createSQLExpress(info, joinTabalis);
				final String sql = "UPDATE " + info.getTable() + " a SET " + info.getSQLColumn("a", column) + " = " + FilterBuild.formatToString(value) + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
				if (this.debug.get()) {
					this.logger.finest(info.getType().getSimpleName() + " update sql=" + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.update(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			final T[] rs = cache.update(info.getAttribute(column), value, filterBuild);
			if (this.cacheListener != null) {
				this.cacheListener.updateCache(info.getType(), rs);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				this.closeSQLConnection(conn);
			}
		}
	}

	@Override
	public <T> void updateColumns(final CompletionHandler<Void, T> handler, final T bean, final String... columns) {
		this.updateColumns(bean, columns);
		if (handler != null) {
			handler.completed(null, bean);
		}
	}

	@Override
	public <T> void updateColumns(final CompletionHandler<Void, FilterBuild> handler, final T bean, final FilterBuild filterBuild, final String... columns) {
		this.updateColumns(bean, filterBuild, columns);
		if (handler != null) {
			handler.completed(null, filterBuild);
		}
	}

	private <T> void update(final Connection conn, final EntityInfo<T> info, final T... values) {
		try {
			final Class clazz = info.getType();
			if (!info.isVirtualEntity()) {
				if (this.debug.get()) {
					this.logger.fine(clazz.getSimpleName() + " update sql=" + info.updateSQL);
				}
				final FieldAttribute<T, Serializable> primary = info.getPrimary();
				final PreparedStatement prestmt = conn.prepareStatement(info.updateSQL);
				final FieldAttribute<T, Serializable>[] attrs = info.updateAttributes;
				String[] sqls = null;
				if (this.writeListener == null) {
					for (final T value : values) {
						int i = 0;
						for (final FieldAttribute<T, Serializable> attr : attrs) {
							prestmt.setObject(++i, attr.getFieldValue(value));
						}
						prestmt.setObject(++i, primary.getFieldValue(value));
						prestmt.addBatch();
					}
				} else {
					final char[] sqlchars = info.updateSQL.toCharArray();
					sqls = new String[values.length];
					final CharSequence[] ps = new CharSequence[attrs.length];
					int index = 0;
					for (final T value : values) {
						int i = 0;
						for (final FieldAttribute<T, Serializable> attr : attrs) {
							final Object a = attr.getFieldValue(value);
							ps[i] = FilterBuild.formatToString(a);
							prestmt.setObject(++i, a);
						}
						prestmt.setObject(++i, primary.getFieldValue(value));
						prestmt.addBatch();
						final StringBuilder sb = new StringBuilder(128);
						i = 0;
						for (final char ch : sqlchars) {
							if (ch == '?') {
								sb.append(ps[i++]);
							} else {
								sb.append(ch);
							}
						}
						sqls[index++] = sb.toString();
					}
				}
				prestmt.executeBatch();
				prestmt.close();
				if (this.writeListener != null) {
					this.writeListener.update(sqls);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			for (final T value : values) {
				cache.update(value);
			}
			if (this.cacheListener != null) {
				this.cacheListener.updateCache(clazz, values);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void updateColumn(final Connection conn, final EntityInfo<T> info, final Serializable id, final String column, final Serializable value) {
		try {
			if (!info.isVirtualEntity()) {
				final String sql = "UPDATE " + info.getTable() + " SET " + info.getSQLColumn(null, column) + " = " + FilterBuild.formatToString(value) + " WHERE " + info.getPrimarySQLColumn() + " = " + FilterBuild.formatToString(id);
				if (this.debug.get()) {
					this.logger.finest(info.getType().getSimpleName() + " update sql=" + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.update(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			final T rs = cache.update(id, info.getAttribute(column), value);
			if (this.cacheListener != null) {
				this.cacheListener.updateCache(info.getType(), rs);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				this.closeSQLConnection(conn);
			}
		}
	}

	private <T> void updateColumns(final Connection conn, final EntityInfo<T> info, final T bean, final String... columns) {
		if ((bean == null) || (columns.length < 1)) {
			return;
		}
		try {
			final Class<T> clazz = (Class<T>) bean.getClass();
			final StringBuilder setsql = new StringBuilder();
			final Serializable id = info.getPrimary().getFieldValue(bean);
			final List<FieldAttribute<T, Serializable>> attrs = new ArrayList<>();
			final boolean virtual = info.isVirtualEntity();
			for (final String col : columns) {
				final FieldAttribute<T, Serializable> attr = info.getUpdateAttribute(col);
				if (attr == null) {
					continue;
				}
				attrs.add(attr);
				if (!virtual) {
					if (setsql.length() > 0) {
						setsql.append(", ");
					}
					setsql.append(info.getSQLColumn(null, col)).append(" = ").append(FilterBuild.formatToString(attr.getFieldValue(bean)));
				}
			}
			if (!virtual) {
				final String sql = "UPDATE " + info.getTable() + " SET " + setsql + " WHERE " + info.getPrimarySQLColumn() + " = " + FilterBuild.formatToString(id);
				if (this.debug.get()) {
					this.logger.finest(bean.getClass().getSimpleName() + ": " + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.update(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			final T rs = cache.update(bean, attrs);
			if (this.cacheListener != null) {
				this.cacheListener.updateCache(clazz, rs);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void updateColumns(final Connection conn, final EntityInfo<T> info, final T bean, final FilterBuild filterBuild, final String... columns) {
		if ((bean == null) || (filterBuild == null) || (columns.length < 1)) {
			return;
		}
		try {
			final Class<T> clazz = (Class<T>) bean.getClass();
			final StringBuilder setsql = new StringBuilder();
			info.getPrimary().getFieldValue(bean);
			final List<FieldAttribute<T, Serializable>> attrs = new ArrayList<>();
			final boolean virtual = info.isVirtualEntity();
			for (final String col : columns) {
				final FieldAttribute<T, Serializable> attr = info.getUpdateAttribute(col);
				if (attr == null) {
					continue;
				}
				attrs.add(attr);
				if (!virtual) {
					if (setsql.length() > 0) {
						setsql.append(", ");
					}
					setsql.append(info.getSQLColumn("a", col)).append(" = ").append(FilterBuild.formatToString(attr.getFieldValue(bean)));
				}
			}
			if (!virtual) {
				final Map<Class, String> joinTabalis = filterBuild.getJoinTabalis();
				final CharSequence join = filterBuild.createSQLJoin(this, joinTabalis, info);
				final CharSequence where = filterBuild.createSQLExpress(info, joinTabalis);

				final String sql = "UPDATE " + info.getTable() + " a SET " + setsql + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
				if (this.debug.get()) {
					this.logger.finest(info.getType().getSimpleName() + " update sql=" + sql);
				}
				final Statement stmt = conn.createStatement();
				stmt.execute(sql);
				stmt.close();
				if (this.writeListener != null) {
					this.writeListener.update(sql);
				}
			}
			final EntityCache<T> cache = info.getCache();
			if (cache == null) {
				return;
			}
			final T[] rs = cache.update(bean, attrs, filterBuild);
			if (this.cacheListener != null) {
				this.cacheListener.updateCache(clazz, rs);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Update cache.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param values the values
	 */
	public <T> void updateCache(final Class<T> clazz, final T... values) {
		if (values.length == 0) {
			return;
		}
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache == null) {
			return;
		}
		for (final T value : values) {
			cache.update(value);
		}
	}

	/**
	 * Reload cache.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param ids the ids
	 */
	public <T> void reloadCache(final Class<T> clazz, final Serializable... ids) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache == null) {
			return;
		}
		final String column = info.getPrimary().getFieldDefaultName();
		for (final Serializable id : ids) {
			final PageData<T> pageData = this.queryPage(false, true, clazz, null, DataDefaultSource.PAGETURN_ONE, FilterBuild.create(column, id));
			final T value = pageData.isEmpty() ? null : pageData.list().get(0);
			if (value != null) {
				cache.update(value);
			}
		}
	}

	@Override
	public Number getNumberResult(final Class entityClass, final FilterFunction filterFunction, final String column) {
		return this.getNumberResult(entityClass, filterFunction, column, (FilterBuild) null);
	}

	@Override
	public Number getNumberResult(final Class entityClass, final FilterFunction filterFunction, final String column, final FilterBuild filterBuild) {
		final Connection conn = this.createReadSQLConnection();
		try {
			final EntityInfo info = this.loadEntityInfo(entityClass);
			final EntityCache cache = info.getCache();
			if ((cache != null) && (info.isVirtualEntity() || cache.isFullLoaded())) {
				if ((filterBuild == null) || filterBuild.isCacheUseable(this)) {
					return cache.getNumberResult(filterFunction, column, filterBuild);
				}
			}
			final Map<Class, String> joinTabalis = filterBuild == null ? null : filterBuild.getJoinTabalis();
			final CharSequence join = filterBuild == null ? null : filterBuild.createSQLJoin(this, joinTabalis, info);
			final CharSequence where = filterBuild == null ? null : filterBuild.createSQLExpress(info, joinTabalis);
			final String sql = "SELECT " + filterFunction.getColumn(((column == null) || column.isEmpty() ? "*" : ("a." + column))) + " FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
			if (this.debug.get()) {
				this.logger.fine(entityClass.getSimpleName() + " single sql=" + sql);
			}
			final PreparedStatement prestmt = conn.prepareStatement(sql);
			Number rs = null;
			final ResultSet set = prestmt.executeQuery();
			if (set.next()) {
				rs = (Number) set.getObject(1);
			}
			set.close();
			prestmt.close();
			return rs;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				this.closeSQLConnection(conn);
			}
		}
	}

	@Override
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn) {
		return this.getMapResult(entityClass, keyColumn, filterFunction, funcColumn, (FilterBuild) null);
	}

	@Override
	public <T, K extends Serializable, N extends Number> Map<K, N> getMapResult(final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn, final FilterBuild filterBuild) {
		final Connection conn = this.createReadSQLConnection();
		try {
			final EntityInfo info = this.loadEntityInfo(entityClass);
			final EntityCache cache = info.getCache();
			if ((cache != null) && (info.isVirtualEntity() || cache.isFullLoaded())) {
				if ((filterBuild == null) || filterBuild.isCacheUseable(this)) {
					return cache.getMapResult(keyColumn, filterFunction, funcColumn, filterBuild);
				}
			}
			final String sqlkey = info.getSQLColumn(null, keyColumn);
			final Map<Class, String> joinTabalis = filterBuild == null ? null : filterBuild.getJoinTabalis();
			final CharSequence join = filterBuild == null ? null : filterBuild.createSQLJoin(this, joinTabalis, info);
			final CharSequence where = filterBuild == null ? null : filterBuild.createSQLExpress(info, joinTabalis);
			final String sql = "SELECT a." + sqlkey + ", " + filterFunction.getColumn(((funcColumn == null) || funcColumn.isEmpty() ? "*" : ("a." + funcColumn))) + " FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where))
					+ " GROUP BY a." + sqlkey;
			if (this.debug.get()) {
				this.logger.fine(entityClass.getSimpleName() + " single sql=" + sql);
			}
			final PreparedStatement prestmt = conn.prepareStatement(sql);
			final Map<K, N> rs = new LinkedHashMap<>();
			final ResultSet set = prestmt.executeQuery();
			final ResultSetMetaData rsd = set.getMetaData();
			final boolean smallint = rsd.getColumnType(1) == Types.SMALLINT;
			while (set.next()) {
				rs.put((K) (smallint ? set.getShort(1) : set.getObject(1)), (N) set.getObject(2));
			}
			set.close();
			prestmt.close();
			return rs;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				this.closeSQLConnection(conn);
			}
		}
	}

	@Override
	public void getNumberResult(final CompletionHandler<Number, String> handler, final Class entityClass, final FilterFunction filterFunction, final String column) {
		final Number rs = this.getNumberResult(entityClass, filterFunction, column);
		if (handler != null) {
			handler.completed(rs, column);
		}
	}

	@Override
	public void getNumberResult(final CompletionHandler<Number, FilterBuild> handler, final Class entityClass, final FilterFunction filterFunction, final String column, final FilterBuild filterBuild) {
		final Number rs = this.getNumberResult(entityClass, filterFunction, column, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}

	}

	@Override
	public <T, K extends Serializable, N extends Number> void getMapResult(final CompletionHandler<Map<K, N>, String> handler, final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn) {
		final Map<K, N> map = this.getMapResult(entityClass, keyColumn, filterFunction, funcColumn);
		if (handler != null) {
			handler.completed(map, funcColumn);
		}
	}

	@Override
	public <T, K extends Serializable, N extends Number> void getMapResult(final CompletionHandler<Map<K, N>, FilterBuild> handler, final Class<T> entityClass, final String keyColumn, final FilterFunction filterFunction, final String funcColumn, final FilterBuild filterBuild) {
		final Map<K, N> map = this.getMapResult(entityClass, keyColumn, filterFunction, funcColumn, filterBuild);
		if (handler != null) {
			handler.completed(map, filterBuild);
		}
	}

	@Override
	public <T> T find(final Class<T> clazz, final Serializable pk) {
		return this.find(clazz, (SelectColumn) null, pk);
	}

	@Override
	public <T> T find(final Class<T> clazz, final SelectColumn selects, final Serializable pk) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache != null) {
			final T rs = cache.find(selects, pk);
			if (cache.isFullLoaded() || (rs != null)) {
				return rs;
			}
		}
		final Connection conn = this.createReadSQLConnection();
		try {
			final SelectColumn sels = selects;
			final String sql = "SELECT * FROM " + info.getTable() + " WHERE " + info.getPrimarySQLColumn() + " = " + FilterBuild.formatToString(pk);
			if (this.debug.get()) {
				this.logger.fine(clazz.getSimpleName() + " find sql=" + sql);
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			final T rs = set.next() ? info.getValue(sels, set) : null;
			set.close();
			ps.close();
			return rs;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> T find(final Class<T> clazz, final String column, final Serializable key) {
		return this.find(clazz, null, FilterBuild.create(column, key));
	}

	@Override
	public <T> T find(final Class<T> clazz, final FilterBuild filterBuild) {
		return this.find(clazz, null, filterBuild);
	}

	@Override
	public <T> T find(final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if ((cache != null) && cache.isFullLoaded() && ((filterBuild == null) || filterBuild.isCacheUseable(this))) {
			return cache.find(selects, filterBuild);
		}
		final Connection conn = this.createReadSQLConnection();
		try {
			final SelectColumn sels = selects;
			final Map<Class, String> joinTabalis = filterBuild == null ? null : filterBuild.getJoinTabalis();
			final CharSequence join = filterBuild == null ? null : filterBuild.createSQLJoin(this, joinTabalis, info);
			final CharSequence where = filterBuild == null ? null : filterBuild.createSQLExpress(info, joinTabalis);
			final String sql = "SELECT a.* FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
			if (this.debug.get()) {
				this.logger.fine(clazz.getSimpleName() + " find sql=" + sql);
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			final T rs = set.next() ? info.getValue(sels, set) : null;
			set.close();
			ps.close();
			return rs;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> boolean exists(final Class<T> clazz, final Serializable pk) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (cache != null) {
			final boolean rs = cache.exists(pk);
			if (rs || cache.isFullLoaded()) {
				return rs;
			}
		}
		final Connection conn = this.createReadSQLConnection();
		try {
			final String sql = "SELECT COUNT(*) FROM " + info.getTable() + " WHERE " + info.getPrimarySQLColumn() + " = " + FilterBuild.formatToString(pk);
			if (this.debug.get()) {
				this.logger.finest(clazz.getSimpleName() + " exists sql=" + sql);
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			final boolean rs = set.next() ? (set.getInt(1) > 0) : false;
			set.close();
			ps.close();
			return rs;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> boolean exists(final Class<T> clazz, final FilterBuild filterBuild) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if ((cache != null) && cache.isFullLoaded() && ((filterBuild == null) || filterBuild.isCacheUseable(this))) {
			return cache.exists(filterBuild);
		}
		final Connection conn = this.createReadSQLConnection();
		try {
			final Map<Class, String> joinTabalis = filterBuild == null ? null : filterBuild.getJoinTabalis();
			final CharSequence join = filterBuild == null ? null : filterBuild.createSQLJoin(this, joinTabalis, info);
			final CharSequence where = filterBuild == null ? null : filterBuild.createSQLExpress(info, joinTabalis);
			final String sql = "SELECT COUNT(" + info.getPrimarySQLColumn("a") + ") FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where));
			if (this.debug.get()) {
				this.logger.finest(clazz.getSimpleName() + " exists sql=" + sql);
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			final boolean rs = set.next() ? (set.getInt(1) > 0) : false;
			set.close();
			ps.close();
			return rs;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final Serializable pk) {
		final T rs = this.find(clazz, pk);
		if (handler != null) {
			handler.completed(rs, pk);
		}
	}

	@Override
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final SelectColumn selects, final Serializable pk) {
		final T rs = this.find(clazz, selects, pk);
		if (handler != null) {
			handler.completed(rs, pk);
		}
	}

	@Override
	public <T> void find(final CompletionHandler<T, Serializable> handler, final Class<T> clazz, final String column, final Serializable key) {
		final T rs = this.find(clazz, column, key);
		if (handler != null) {
			handler.completed(rs, key);
		}
	}

	@Override
	public <T> void find(final CompletionHandler<T, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild) {
		final T rs = this.find(clazz, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> void find(final CompletionHandler<T, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild) {
		final T rs = this.find(clazz, selects, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> void exists(final CompletionHandler<Boolean, Serializable> handler, final Class<T> clazz, final Serializable pk) {
		final boolean rs = this.exists(clazz, pk);
		if (handler != null) {
			handler.completed(rs, pk);
		}
	}

	@Override
	public <T> void exists(final CompletionHandler<Boolean, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild) {
		final boolean rs = this.exists(clazz, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T, V extends Serializable> List<V> queryColumnList(final String selectedColumn, final Class<T> clazz, final String column, final Serializable key) {
		return this.queryColumnList(selectedColumn, clazz, FilterBuild.create(column, key));
	}

	@Override
	public <T, V extends Serializable> List<V> queryColumnList(final String selectedColumn, final Class<T> clazz, final FilterBuild filterBuild) {
		return (List<V>) this.queryColumnPage(selectedColumn, clazz, null, filterBuild).list(true);
	}

	@Override
	public <T, V extends Serializable> void queryColumnList(final CompletionHandler<List<V>, Serializable> handler, final String selectedColumn, final Class<T> clazz, final String column, final Serializable key) {
		final List<V> rs = this.queryColumnList(selectedColumn, clazz, column, key);
		if (handler != null) {
			handler.completed(rs, key);
		}
	}

	@Override
	public <T, V extends Serializable> void queryColumnList(final CompletionHandler<List<V>, FilterBuild> handler, final String selectedColumn, final Class<T> clazz, final FilterBuild filterBuild) {
		final List<V> rs = this.queryColumnList(selectedColumn, clazz, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T, V extends Serializable> PageData<V> queryColumnPage(final String selectedColumn, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final PageData<T> pageData = this.queryPage(true, true, clazz, SelectColumn.createIncludes(selectedColumn), pageTurn, filterBuild);
		final PageData<V> rs = new PageData<>();
		if (pageData.isEmpty()) {
			return rs;
		}
		rs.setTotal(pageData.getTotal());
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final FieldAttribute<T, V> selected = (FieldAttribute<T, V>) info.getAttribute(selectedColumn);
		final List<V> list = new ArrayList<>();
		for (final T t : pageData.getRows()) {
			list.add(selected.getFieldValue(t));
		}
		rs.setRows(list);
		return rs;
	}

	@Override
	public <T, V extends Serializable> void queryColumnPage(final CompletionHandler<PageData<V>, FilterBuild> handler, final String selectedColumn, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final PageData<V> rs = this.queryColumnPage(selectedColumn, clazz, pageTurn, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> List<T> queryList(final Class<T> clazz, final String column, final Serializable key) {
		return this.queryList(clazz, FilterBuild.create(column, key));
	}

	@Override
	public <T> List<T> queryList(final Class<T> clazz, final FilterBuild filterBuild) {
		return this.queryList(clazz, (SelectColumn) null, filterBuild);
	}

	@Override
	public <T> List<T> queryList(final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild) {
		return this.queryPage(true, false, clazz, selects, null, filterBuild).list(true);
	}

	@Override
	public <T> void queryList(final CompletionHandler<List<T>, Serializable> handler, final Class<T> clazz, final String column, final Serializable key) {
		final List<T> rs = this.queryList(clazz, column, key);
		if (handler != null) {
			handler.completed(rs, key);
		}
	}

	@Override
	public <T> void queryList(final CompletionHandler<List<T>, FilterBuild> handler, final Class<T> clazz, final FilterBuild filterBuild) {
		final List<T> rs = this.queryList(clazz, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> void queryList(final CompletionHandler<List<T>, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final FilterBuild filterBuild) {
		final List<T> rs = this.queryList(clazz, selects, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> PageData<T> queryPage(final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild) {
		return this.queryPage(clazz, null, pageTurn, filterBuild);
	}

	@Override
	public <T> PageData<T> queryPage(final Class<T> clazz, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild) {
		return this.queryPage(true, true, clazz, selects, pageTurn, filterBuild);
	}

	@Override
	public <T> void queryPage(final CompletionHandler<PageData<T>, FilterBuild> handler, final Class<T> clazz, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final PageData<T> rs = this.queryPage(clazz, pageTurn, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	@Override
	public <T> void queryPage(final CompletionHandler<PageData<T>, FilterBuild> handler, final Class<T> clazz, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final PageData<T> rs = this.queryPage(clazz, selects, pageTurn, filterBuild);
		if (handler != null) {
			handler.completed(rs, filterBuild);
		}
	}

	private <T> PageData<T> queryPage(final boolean readcache, final boolean needtotal, final Class<T> clazz, final SelectColumn selects, final PageTurn pageTurn, final FilterBuild filterBuild) {
		final EntityInfo<T> info = this.loadEntityInfo(clazz);
		final EntityCache<T> cache = info.getCache();
		if (readcache && (cache != null) && cache.isFullLoaded()) {
			if ((filterBuild == null) || filterBuild.isCacheUseable(this)) {
				if (this.debug.get()) {
					this.logger.fine(clazz.getSimpleName() + " cache query predicate = " + (filterBuild == null ? null : filterBuild.createPredicate(cache)));
				}
				return cache.queryPage(needtotal, selects, pageTurn, filterBuild);
			}
		}
		final Connection conn = this.createReadSQLConnection();
		try {
			final SelectColumn sels = selects;
			final List<T> list = new ArrayList();
			final Map<Class, String> joinTabalis = filterBuild == null ? null : filterBuild.getJoinTabalis();
			final CharSequence join = filterBuild == null ? null : filterBuild.createSQLJoin(this, joinTabalis, info);
			final CharSequence where = filterBuild == null ? null : filterBuild.createSQLExpress(info, joinTabalis);
			final String sql = "SELECT a.* FROM " + info.getTable() + " a" + (join == null ? "" : join) + (((where == null) || (where.length() == 0)) ? "" : (" WHERE " + where)) + info.createSQLOrderby(pageTurn);
			if (this.debug.get()) {
				this.logger.fine(clazz.getSimpleName() + " query sql=" + sql + (pageTurn == null ? "" : (" LIMIT " + pageTurn.getOffset() + "," + pageTurn.getLimit())));
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			if ((pageTurn != null) && (pageTurn.getOffset() > 0)) {
				set.absolute(pageTurn.getOffset());
			}
			final int limit = pageTurn == null ? Integer.MAX_VALUE : pageTurn.getLimit();
			int i = 0;
			while (set.next()) {
				i++;
				list.add(info.getValue(sels, set));
				if (limit <= i) {
					break;
				}
			}
			long total = list.size();
			if (needtotal && (pageTurn != null)) {
				set.last();
				total = set.getRow();
			}
			set.close();
			ps.close();
			return new PageData<>(total, list);
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	@Override
	public void directQuery(final String sql, final Consumer<ResultSet> consumer) {
		final Connection conn = this.createReadSQLConnection();
		try {
			if (this.debug.get()) {
				this.logger.fine("direct query sql=" + sql);
			}
			final PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet set = ps.executeQuery();
			consumer.accept(set);
			set.close();
			ps.close();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

	private int[] directExecute(final Connection conn, final String... sqls) {
		if (sqls.length == 0) {
			return new int[0];
		}
		try {
			final Statement stmt = conn.createStatement();
			final int[] rs = new int[sqls.length];
			int i = -1;
			for (final String sql : sqls) {
				rs[++i] = stmt.execute(sql) ? 1 : 0;

			}
			stmt.close();
			return rs;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] directExecute(final String... sqls) {
		final Connection conn = this.createWriteSQLConnection();
		try {
			return this.directExecute(conn, sqls);
		} finally {
			this.closeSQLConnection(conn);
		}
	}

}
