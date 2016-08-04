/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.net;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import io.github.jcharm.common.ConfigValue;

/**
 * The Class Server.
 *
 * @param <K> the key type
 * @param <C> the generic type
 * @param <R> the generic type
 * @param
 * 			<P>
 *            the generic type
 * @param <S> the generic type
 */
public abstract class Server<K extends Serializable, C extends Context, R extends Request<C>, P extends Response<C, R>, S extends Servlet<C, R, P>> {

	public static final String RESNAME_SERVER_ROOT = "SERVER_ROOT";

	protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	// 服务的启动时间
	protected final long serverStartTime;

	// 服务的名称
	protected String name;

	// 应用层协议名
	protected final String protocol;

	// 服务的根Servlet
	protected final PrepareServlet<K, C, R, P, S> prepare;

	// 服务的上下文对象
	protected C context;

	// 服务的配置信息
	protected ConfigValue config;

	// 服务数据的编解码，null视为UTF-8
	protected Charset charset;

	// 服务的监听端口
	protected InetSocketAddress address;

	// 连接队列大小
	protected int backlog;

	// 传输层协议的服务
	protected ProtocolServer serverChannel;

	// ByteBuffer的容量大小
	protected int bufferCapacity;

	// 线程数
	protected int threads;

	// 线程池
	protected ExecutorService executor;

	// ByteBuffer池大小
	protected int bufferPoolSize;

	// Response池大小
	protected int responsePoolSize;

	// 请求包大小的上限，单位:字节
	protected int maxbody;

	// IO读取的超时秒数，小于1视为不设置
	protected int readTimeoutSecond;

	// IO写入 的超时秒数，小于1视为不设置
	protected int writeTimeoutSecond;

	protected Server(final long serverStartTime, final String protocol, final PrepareServlet<K, C, R, P, S> servlet) {
		this.serverStartTime = serverStartTime;
		this.protocol = protocol;
		this.prepare = servlet;
	}

	public void init(final ConfigValue config) throws Exception {
		Objects.requireNonNull(config);
		this.config = config;
		this.address = new InetSocketAddress(config.getValue("host", "0.0.0.0"), config.getIntValue("port", 80));
		this.charset = Charset.forName(config.getValue("charset", "UTF-8"));
		this.backlog = config.getIntValue("backlog", 8 * 1024);
		this.readTimeoutSecond = config.getIntValue("readTimeoutSecond", 0);
		this.writeTimeoutSecond = config.getIntValue("writeTimeoutSecond", 0);
		this.maxbody = config.getIntValue("maxbody", 64 * 1024);
		this.bufferCapacity = config.getIntValue("bufferCapacity", 8 * 1024);
		this.threads = config.getIntValue("threads", Runtime.getRuntime().availableProcessors() * 16);
		this.bufferPoolSize = config.getIntValue("bufferPoolSize", Runtime.getRuntime().availableProcessors() * 512);
		this.responsePoolSize = config.getIntValue("responsePoolSize", Runtime.getRuntime().availableProcessors() * 256);
		this.name = config.getValue("name", "Server-" + this.protocol + "-" + this.address.getPort());
		if (!this.name.matches("^[a-zA-Z][\\w_-]{1,64}$")) {
			throw new RuntimeException("server.name (" + this.name + ") is illegal");
		}
		final AtomicInteger counter = new AtomicInteger();
		final Format f = this.createFormat();
		final String n = this.name;
		this.executor = Executors.newFixedThreadPool(this.threads, (final Runnable r) -> {
			final Thread t = new WorkThread(this.executor, r);
			t.setName(n + "-ServletThread-" + f.format(counter.incrementAndGet()));
			return t;
		});
	}

	public void destroy(final ConfigValue config) throws Exception {
		this.prepare.destroy(this.context, config);
	}

	public InetSocketAddress getSocketAddress() {
		return this.address;
	}

	public String getName() {
		return this.name;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void addServlet(final S servlet, final Object attachment, final ConfigValue conf, final K... mappings) {
		this.prepare.addServlet(servlet, attachment, conf, mappings);
	}

	public void start() throws IOException {
		this.context = this.createContext();
		this.prepare.init(this.context, this.config);
		this.serverChannel = ProtocolServer.create(this.protocol, this.context);
		this.serverChannel.open();
		if (this.serverChannel.supportedOptions().contains(StandardSocketOptions.TCP_NODELAY)) {
			this.serverChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		}
		this.serverChannel.bind(this.address, this.backlog);
		this.serverChannel.accept();
		final String threadName = "[" + Thread.currentThread().getName() + "] ";
		this.logger.info(threadName + this.getClass().getSimpleName() + ("TCP".equalsIgnoreCase(this.protocol) ? "" : ("." + this.protocol)) + " listen: " + this.address + ", threads: " + this.threads + ", bufferCapacity: " + this.bufferCapacity + ", bufferPoolSize: " + this.bufferPoolSize
				+ ", responsePoolSize: " + this.responsePoolSize + ", started in " + (System.currentTimeMillis() - this.context.getServerStartTime()) + " ms");
	}

	protected abstract C createContext();

	public void shutdown() throws IOException {
		final long s = System.currentTimeMillis();
		this.logger.info(this.getClass().getSimpleName() + "-" + this.protocol + " shutdowning");
		try {
			this.serverChannel.close();
		} catch (final Exception e) {
		}
		this.logger.info(this.getClass().getSimpleName() + "-" + this.protocol + " shutdow prepare servlet");
		this.prepare.destroy(this.context, this.config);
		final long e = System.currentTimeMillis() - s;
		this.logger.info(this.getClass().getSimpleName() + " shutdown in " + e + " ms");
	}

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
		final List<URL> list = new ArrayList<>(set);
		Collections.sort(list, (final URL o1, final URL o2) -> o1.getFile().compareTo(o2.getFile()));
		return list.toArray(new URL[list.size()]);
	}

}
