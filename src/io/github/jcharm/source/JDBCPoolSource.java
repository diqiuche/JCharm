/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * JDBC连接池.
 */
public class JDBCPoolSource {

	private static final Map<String, AbstractMap.SimpleEntry<WatchService, List<WeakReference<JDBCPoolSource>>>> maps = new HashMap<>();

	private final AtomicLong usingCounter = new AtomicLong();

	private final AtomicLong creatCounter = new AtomicLong();

	private final AtomicLong cycleCounter = new AtomicLong();

	private final AtomicLong saveCounter = new AtomicLong();

	private final ConnectionPoolDataSource source;

	private final ArrayBlockingQueue<PooledConnection> queue;

	private final ConnectionEventListener listener;

	private final DataDefaultSource dataSource;

	private final String stype; // "" 或 "read" 或 "write"

	private final int max;

	private String url;

	private String user;

	private String password;

	/** Properties. */
	final Properties props;

	/**
	 * Instantiates a new JDBC pool source.
	 *
	 * @param source the source
	 * @param stype the stype
	 * @param prop the prop
	 */
	public JDBCPoolSource(final DataDefaultSource source, final String stype, final Properties prop) {
		this.dataSource = source;
		this.stype = stype;
		this.props = prop;
		this.source = DataDefaultSource.createDataSource(prop);
		this.url = prop.getProperty(DataDefaultSource.JDBC_URL);
		this.user = prop.getProperty(DataDefaultSource.JDBC_USER);
		this.password = prop.getProperty(DataDefaultSource.JDBC_PWD);
		this.max = Integer.decode(prop.getProperty(DataDefaultSource.JDBC_CONNECTIONSMAX, "" + (Runtime.getRuntime().availableProcessors() * 16)));
		this.queue = new ArrayBlockingQueue<>(this.max);
		this.listener = new ConnectionEventListener() {

			@Override
			public void connectionClosed(final ConnectionEvent event) {
				final PooledConnection pc = (PooledConnection) event.getSource();
				if (JDBCPoolSource.this.queue.offer(pc)) {
					JDBCPoolSource.this.saveCounter.incrementAndGet();
				}
			}

			@Override
			public void connectionErrorOccurred(final ConnectionEvent event) {
				JDBCPoolSource.this.usingCounter.decrementAndGet();
				if ("08S01".equals(event.getSQLException().getSQLState()))
				 {
					return; // MySQL特性， 长时间连接没使用会抛出com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
				}
				JDBCPoolSource.this.dataSource.logger.log(Level.WARNING, "connectionErronOccurred  [" + event.getSQLException().getSQLState() + "]", event.getSQLException());
			}
		};
		if (this.isOracle()) {
			this.props.setProperty(DataDefaultSource.JDBC_CONTAIN_SQLTEMPLATE, "INSTR(${keystr}, ${column}) > 0");
			this.props.setProperty(DataDefaultSource.JDBC_NOTCONTAIN_SQLTEMPLATE, "INSTR(${keystr}, ${column}) = 0");
		} else if (this.isSqlserver()) {
			this.props.setProperty(DataDefaultSource.JDBC_CONTAIN_SQLTEMPLATE, "CHARINDEX(${column}, ${keystr}) > 0");
			this.props.setProperty(DataDefaultSource.JDBC_NOTCONTAIN_SQLTEMPLATE, "CHARINDEX(${column}, ${keystr}) = 0");
		}
		try {
			this.watch();
		} catch (final Exception e) {
			this.dataSource.logger.log(Level.WARNING, DataSource.class.getSimpleName() + " watch " + this.dataSource.conf + " error", e);
		}
	}

	/**
	 * Checks if is mysql.
	 *
	 * @return true, if is mysql
	 */
	final boolean isMysql() {
		return (this.source != null) && this.source.getClass().getName().contains(".mysql.");
	}

	/**
	 * Checks if is oracle.
	 *
	 * @return true, if is oracle
	 */
	final boolean isOracle() {
		return (this.source != null) && this.source.getClass().getName().contains("oracle.");
	}

	/**
	 * Checks if is sqlserver.
	 *
	 * @return true, if is sqlserver
	 */
	final boolean isSqlserver() {
		return (this.source != null) && this.source.getClass().getName().contains(".sqlserver.");
	}

	private void watch() throws IOException {
		if ((this.dataSource.conf == null) || (this.dataSource.name == null)) {
			return;
		}
		final String file = this.dataSource.conf.getFile();
		final File f = new File(file);
		if (!f.isFile() || !f.canRead()) {
			return;
		}
		synchronized (JDBCPoolSource.maps) {
			final AbstractMap.SimpleEntry<WatchService, List<WeakReference<JDBCPoolSource>>> entry = JDBCPoolSource.maps.get(file);
			if (entry != null) {
				entry.getValue().add(new WeakReference<>(this));
				return;
			}
			final WatchService watcher = f.toPath().getFileSystem().newWatchService();
			final List<WeakReference<JDBCPoolSource>> list = new CopyOnWriteArrayList<>();
			final Thread watchThread = new Thread() {

				@Override
				public void run() {
					try {
						while (!this.isInterrupted()) {
							final WatchKey key = watcher.take();
							Thread.sleep(3000); // 防止文件正在更新过程中去读取
							final Map<String, Properties> m = DataDefaultSource.loadProperties(new FileInputStream(file));
							key.pollEvents().stream().forEach((event) -> {
								if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
									return;
								}
								if (!((Path) event.context()).toFile().getName().equals(f.getName())) {
									return;
								}
								for (final WeakReference<JDBCPoolSource> ref : list) {
									final JDBCPoolSource pool = ref.get();
									if (pool == null) {
										continue;
									}
									try {
										Properties property = m.get(pool.dataSource.name);
										if (property == null) {
											property = m.get(pool.dataSource.name + "." + pool.stype);
										}
										if (property != null) {
											pool.change(property);
										}
									} catch (final Exception ex) {
										JDBCPoolSource.this.dataSource.logger.log(Level.INFO, event.context() + " occur error", ex);
									}
								}
							});
							key.reset();
						}
					} catch (final Exception e) {
						JDBCPoolSource.this.dataSource.logger.log(Level.WARNING, "DataSource watch " + file + " occur error", e);
					}
				}
			};
			f.getParentFile().toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			watchThread.setName("DataSource-Watch-" + JDBCPoolSource.maps.size() + "-Thread");
			watchThread.setDaemon(true);
			watchThread.start();
			this.dataSource.logger.log(Level.INFO, watchThread.getName() + " start watching " + file);
			list.add(new WeakReference<>(this));
			JDBCPoolSource.maps.put(file, new AbstractMap.SimpleEntry<>(watcher, list));
		}
	}

	/**
	 * Change.
	 *
	 * @param property the property
	 */
	public void change(final Properties property) {
		Method seturlm;
		final Class clazz = this.source.getClass();
		final String newurl = property.getProperty(DataDefaultSource.JDBC_URL);
		final String newuser = property.getProperty(DataDefaultSource.JDBC_USER);
		final String newpassword = property.getProperty(DataDefaultSource.JDBC_PWD);
		if (this.url.equals(newurl) && this.user.equals(newuser) && this.password.equals(newpassword)) {
			return;
		}
		try {
			try {
				seturlm = clazz.getMethod("setUrl", String.class);
			} catch (final Exception e) {
				seturlm = clazz.getMethod("setURL", String.class);
			}
			seturlm.invoke(this.source, newurl);
			clazz.getMethod("setUser", String.class).invoke(this.source, newuser);
			clazz.getMethod("setPassword", String.class).invoke(this.source, newpassword);
			this.url = newurl;
			this.user = newuser;
			this.password = newpassword;
			this.dataSource.logger.log(Level.INFO, DataSource.class.getSimpleName() + "(" + this.dataSource.name + "." + this.stype + ") change  (" + property + ")");
		} catch (final Exception e) {
			this.dataSource.logger.log(Level.SEVERE, DataSource.class.getSimpleName() + " dynamic change JDBC (url userName password) error", e);
		}
	}

	/**
	 * Poll.
	 *
	 * @return the connection
	 */
	public Connection poll() {
		return this.poll(0, null);
	}

	private Connection poll(final int count, final SQLException e) {
		if (count >= 3) {
			this.dataSource.logger.log(Level.WARNING, "create pooled connection error", e);
			throw new RuntimeException(e);
		}
		PooledConnection result = this.queue.poll();
		if (result == null) {
			if (this.usingCounter.get() >= this.max) {
				try {
					result = this.queue.poll(6, TimeUnit.SECONDS);
				} catch (final Exception t) {
					this.dataSource.logger.log(Level.WARNING, "take pooled connection error", t);
				}
			}
			if (result == null) {
				try {
					result = this.source.getPooledConnection();
					result.addConnectionEventListener(this.listener);
					this.usingCounter.incrementAndGet();
				} catch (final SQLException ex) {
					return this.poll(count + 1, ex);
				}
				this.creatCounter.incrementAndGet();
			}
		} else {
			this.cycleCounter.incrementAndGet();
		}
		Connection conn;
		try {
			conn = result.getConnection();
			if (!conn.isValid(1)) {
				this.dataSource.logger.info("sql connection is not vaild");
				this.usingCounter.decrementAndGet();
				return this.poll(0, null);
			}
		} catch (final SQLException ex) {
			if (!"08S01".equals(ex.getSQLState())) {// MySQL特性， 长时间连接没使用会抛出com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
				this.dataSource.logger.log(Level.FINER, "result.getConnection from pooled connection abort [" + ex.getSQLState() + "]", ex);
			}
			return this.poll(0, null);
		}
		return conn;
	}

	/**
	 * Gets the creat count.
	 *
	 * @return the creat count
	 */
	public long getCreatCount() {
		return this.creatCounter.longValue();
	}

	/**
	 * Gets the cycle count.
	 *
	 * @return the cycle count
	 */
	public long getCycleCount() {
		return this.cycleCounter.longValue();
	}

	/**
	 * Gets the save count.
	 *
	 * @return the save count
	 */
	public long getSaveCount() {
		return this.saveCounter.longValue();
	}

	/**
	 * Close.
	 */
	public void close() {
		this.queue.stream().forEach(x -> {
			try {
				x.close();
			} catch (final Exception e) {
			}
		});
	}

}
