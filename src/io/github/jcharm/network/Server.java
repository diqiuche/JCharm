/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.network;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.Format;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import io.github.jcharm.common.ConfigValue;

/**
 * Server抽象类.
 *
 * @param <K> 请求ID的数据类型, 例如HTTP协议请求标识为url, 请求ID的数据类型就是String
 * @param <C> Context的子类型
 * @param <R> Request的子类型
 * @param <N> Response的子类型
 * @param <S> Servlet的子类型
 */
public abstract class Server<K extends Serializable, C extends Context, R extends Request<C>, N extends Response<C, R>, S extends Servlet<C, R, N>> {

	/** 服务的资源根位置. */
	public static final String RESNAME_SERVER_ROOT = "SERVER_ROOT";

	/** 服务的日志对象. */
	protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	/** 服务的启动时间. */
	protected LocalDateTime serverStartTime;

	/** 服务的名称. */
	protected String name;

	/** 应用层协议名. */
	protected String protocol;

	/** 服务内置的Servlet. */
	protected PrepareServlet<K, C, R, N, S> prepareServlet;

	/** 服务的上下文对象. */
	protected C context;

	/** 服务的配置信息. */
	protected ConfigValue configValue;

	/** 服务数据的编解码, null视为UTF-8. */
	protected Charset charset;

	/** 服务的监听端口. */
	protected InetSocketAddress inetSocketAddress;

	/** 连接队列大小. */
	protected int backlog;

	/** 传输层协议的服务. */
	protected ProtocolServer protocolServer;

	/** ByteBuffer的容量大小. */
	protected int bufferCapacity;

	/** 线程数. */
	protected int threads;

	/** 线程池. */
	protected ExecutorService executorService;

	/** ByteBuffer对象池大小. */
	protected int bufferPoolSize;

	/** Response对象池大小. */
	protected int responsePoolSize;

	/** 请求包大小的上限. */
	protected int maxbody;

	/** IO读取的超时秒数. */
	protected int readTimeoutSecond;

	/** IO写入的超时秒数. */
	protected int writeTimeoutSecond;

	/**
	 * 构造函数.
	 *
	 * @param serverStartTime LocalDateTime
	 * @param protocol String
	 * @param prepareServlet PrepareServlet
	 */
	protected Server(final LocalDateTime serverStartTime, final String protocol, final PrepareServlet<K, C, R, N, S> prepareServlet) {
		this.serverStartTime = serverStartTime;
		this.protocol = protocol;
		this.prepareServlet = prepareServlet;
	}

	/**
	 * 初始化服务.
	 *
	 * @param configValue ConfigValue
	 * @throws Exception the exception
	 */
	public void init(final ConfigValue configValue) throws Exception {
		this.configValue = configValue;
		this.inetSocketAddress = new InetSocketAddress(configValue.getValue("host", "0.0.0.0"), configValue.getIntValue("port", 80));
		this.charset = Charset.forName(configValue.getValue("charset", "UTF-8"));
		this.backlog = configValue.getIntValue("backlog", 8 * 1024);
		this.readTimeoutSecond = configValue.getIntValue("readTimeoutSecond", 0);
		this.writeTimeoutSecond = configValue.getIntValue("writeTimeoutSecond", 0);
		this.maxbody = configValue.getIntValue("maxbody", 64 * 1024);
		this.bufferCapacity = configValue.getIntValue("bufferCapacity", 8 * 1024);
		this.threads = configValue.getIntValue("threads", Runtime.getRuntime().availableProcessors() * 16);
		this.bufferPoolSize = configValue.getIntValue("bufferPoolSize", Runtime.getRuntime().availableProcessors() * 512);
		this.responsePoolSize = configValue.getIntValue("responsePoolSize", Runtime.getRuntime().availableProcessors() * 256);
		this.name = configValue.getValue("name", "Server-" + this.protocol + "-" + this.inetSocketAddress.getPort());
		if (!this.name.matches("^[a-zA-Z][\\w_-]{1,64}$")) {
			throw new RuntimeException("server.name (" + this.name + ") is illegal");
		}
		final AtomicInteger counter = new AtomicInteger();
		final Format f = this.createFormat();
		final String n = this.name;
		this.executorService = Executors.newFixedThreadPool(this.threads, (final Runnable r) -> {// 创建一个可重用固定线程数的线程池, 以共享的无界队列方式来运行这些线程, 在需要时使用提供的ThreadFactory创建新线程
			final Thread t = new WorkThread(this.executorService, r);
			t.setName(n + "-ServletThread-" + f.format(counter.incrementAndGet()));
			return t;
		});
	}

	/**
	 * 销毁服务.
	 *
	 * @param configValue ConfigValue
	 * @throws Exception the exception
	 */
	public void destroy(final ConfigValue configValue) throws Exception {
		this.prepareServlet.destroy(this.context, configValue);
	}

	/**
	 * 获取服务的InetSocketAddress.
	 *
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getSocketAddress() {
		return this.inetSocketAddress;
	}

	/**
	 * 获取服务的名称.
	 *
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 获取应用层协议名.
	 *
	 * @return String
	 */
	public String getProtocol() {
		return this.protocol;
	}

	/**
	 * 获取服务的日志对象.
	 *
	 * @return Logger
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * 添加Servlet.
	 *
	 * @param servlet Servlet的子类型
	 * @param attachment IO操作
	 * @param configValue ConfigValue
	 * @param mappings 请求ID的数据类型
	 */
	public void addServlet(final S servlet, final Object attachment, final ConfigValue configValue, final K... mappings) {
		this.prepareServlet.addServlet(servlet, attachment, configValue, mappings);
	}

	/**
	 * 开启服务.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void start() throws IOException {
		this.context = this.createContext();
		this.prepareServlet.init(this.context, this.configValue);
		this.protocolServer = ProtocolServer.create(this.protocol, this.context);
		this.protocolServer.open();
		if (this.protocolServer.supportedOptions().contains(StandardSocketOptions.TCP_NODELAY)) {
			this.protocolServer.setOption(StandardSocketOptions.TCP_NODELAY, true);
		}
		this.protocolServer.bind(this.inetSocketAddress, this.backlog);
		this.protocolServer.accept();
		final String threadName = "[" + Thread.currentThread().getName() + "] ";
		this.logger.info(threadName + this.getClass().getSimpleName() + ("TCP".equalsIgnoreCase(this.protocol) ? "" : ("." + this.protocol)) + " listen: " + this.inetSocketAddress + ", threads: " + this.threads + ", bufferCapacity: " + this.bufferCapacity + ", bufferPoolSize: " + this.bufferPoolSize + ", responsePoolSize: "
				+ this.responsePoolSize + ", started in " + (System.currentTimeMillis() - this.context.getServerStartTime().toEpochSecond((ZoneOffset) ZoneId.systemDefault())) + " ms");
	}

	/**
	 * 创建Context上下文.
	 *
	 * @return Context的子类型
	 */
	protected abstract C createContext();

	/**
	 * 关闭服务.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void shutdown() throws IOException {
		final long s = System.currentTimeMillis();
		this.logger.info(this.getClass().getSimpleName() + "-" + this.protocol + " shutdowning");
		try {
			this.protocolServer.close();
		} catch (final Exception e) {
		}
		this.logger.info(this.getClass().getSimpleName() + "-" + this.protocol + " shutdow prepare servlet");
		this.prepareServlet.destroy(this.context, this.configValue);
		final long e = System.currentTimeMillis() - s;
		this.logger.info(this.getClass().getSimpleName() + " shutdown in " + e + " ms");
	}

	/**
	 * 创建Format对象.
	 *
	 * @return Format
	 */
	protected Format createFormat() {
		String sf = "0";
		if (this.threads > 10) {
			sf = "00";
		}
		if (this.threads > 100) {
			sf = "000";
		}
		if (this.threads > 1000) {
			sf = "0000";
		}
		return new DecimalFormat(sf);
	}

	/**
	 * 加载Lib.
	 *
	 * @param logger Logger
	 * @param lib String
	 * @return URL[]
	 * @throws Exception the exception
	 */
	public static URL[] loadLib(final Logger logger, final String lib) throws Exception {
		if ((lib == null) || lib.isEmpty()) {
			return new URL[0];
		}
		final Set<URL> set = new HashSet<>();
		for (final String s : lib.split(";")) {
			if (s.isEmpty()) {
				continue;
			}
			if (s.endsWith("*")) {
				final File root = new File(s.substring(0, s.length() - 1));
				if (root.isDirectory()) {
					for (final File f : root.listFiles()) {
						set.add(f.toURI().toURL());
					}
				}
			} else {
				final File f = new File(s);
				if (f.canRead()) {
					set.add(f.toURI().toURL());
				}
			}
		}
		if (set.isEmpty()) {
			return new URL[0];
		}
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl instanceof URLClassLoader) {
			final URLClassLoader loader = (URLClassLoader) cl;
			final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			for (final URL url : set) {
				method.invoke(loader, url);
			}
		} else {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(set.toArray(new URL[set.size()]), cl));
		}
		final List<URL> list = new ArrayList(set);
		Collections.sort(list, (final URL o1, final URL o2) -> o1.getFile().compareTo(o2.getFile()));
		return list.toArray(new URL[list.size()]);
	}

}
