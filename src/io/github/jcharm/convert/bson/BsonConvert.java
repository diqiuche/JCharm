/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.Convert;
import io.github.jcharm.convert.ConvertFactory;

/**
 * BSON双向序列化类.
 */
public final class BsonConvert extends Convert<BsonDeserializeReader, BsonSerializeWriter> {

	private static final ObjectPool<BsonDeserializeReader> readerPool = BsonDeserializeReader.createPool(16);

	private static final ObjectPool<BsonSerializeWriter> writerPool = BsonSerializeWriter.createPool(16);

	/**
	 * 构造函数.
	 *
	 * @param convertFactory ConvertFactory
	 */
	protected BsonConvert(final ConvertFactory<BsonDeserializeReader, BsonSerializeWriter> convertFactory) {
		super(convertFactory);
	}

	@Override
	public BsonConvertFactory getConvertFactory() {
		return (BsonConvertFactory) super.getConvertFactory();
	}

	/**
	 * 获取双向序列化类实例.
	 *
	 * @return BsonConvert
	 */
	public static BsonConvert instance() {
		return BsonConvertFactory.instance().getConvert();
	}

	// ---------------------------------------BsonDeserializeReader--------------------------------------------------

	/**
	 * 获取BsonByteBufferDeserializeReader反序列化输入流.
	 *
	 * @param buffers ByteBuffer
	 * @return BsonDeserializeReader
	 */
	public BsonDeserializeReader pollBsonDeserializeReader(final ByteBuffer... buffers) {
		return new BsonByteBufferDeserializeReader(buffers);
	}

	/**
	 * 获取BsonStreamDeserializeReader反序列化输入流.
	 *
	 * @param in InputStream
	 * @return BsonDeserializeReader
	 */
	public BsonDeserializeReader pollBsonDeserializeReader(final InputStream in) {
		return new BsonStreamDeserializeReader(in);
	}

	/**
	 * 获取当前BsonConvert的BsonDeserializeReader反序列化输入流.
	 *
	 * @return BsonDeserializeReader
	 */
	public BsonDeserializeReader pollBsonDeserializeReader() {
		return BsonConvert.readerPool.get();
	}

	/**
	 * 将BsonDeserializeReader反序列化输入流存放到对象池中.
	 *
	 * @param in BsonDeserializeReader
	 */
	public void offerBsonDeserializeReader(final BsonDeserializeReader in) {
		if (in != null) {
			BsonConvert.readerPool.offer(in);
		}
	}

	// ------------------------------------------BsonSerializeWriter-----------------------------------------------

	/**
	 * 获取BsonByteBufferSerializeWriter序列化输出流.
	 *
	 * @param supplier Supplier
	 * @return BsonByteBufferSerializeWriter
	 */
	public BsonByteBufferSerializeWriter pollBsonSerializeWriter(final Supplier<ByteBuffer> supplier) {
		return new BsonByteBufferSerializeWriter(supplier);
	}

	/**
	 * 获取BsonStreamByteBufferSerializeWriter序列化输出流.
	 *
	 * @param out OutputStream
	 * @return BsonSerializeWriter
	 */
	public BsonSerializeWriter pollBsonSerializeWriter(final OutputStream out) {
		return new BsonStreamSerializeWriter(out);
	}

	/**
	 * 获取当前BsonConvert的BsonSerializeWriter序列化输出流.
	 *
	 * @return BsonSerializeWriter
	 */
	public BsonSerializeWriter pollBsonSerializeWriter() {
		return BsonConvert.writerPool.get();
	}

	/**
	 * 将BsonSerializeWriter序列化输出流存放到对象池中.
	 *
	 * @param out BsonSerializeWriter
	 */
	public void offerBsonSerializeWriter(final BsonSerializeWriter out) {
		if (out != null) {
			BsonConvert.writerPool.offer(out);
		}
	}

	// ------------------------------------------------convertFrom------------------------------------------------

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param bytes byte[]
	 * @param start int
	 * @param len int
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final byte[] bytes, final int start, final int len) {
		if (type == null) {
			return null;
		}
		final BsonDeserializeReader in = BsonConvert.readerPool.get();
		in.setBytes(bytes, start, len);
		final T rs = (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(in);
		BsonConvert.readerPool.offer(in);
		return rs;
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param bytes byte[]
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return this.convertFrom(type, bytes, 0, bytes.length);
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param in InputStream
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final InputStream in) {
		if ((type == null) || (in == null)) {
			return null;
		}
		return (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(new BsonStreamDeserializeReader(in));
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param buffers ByteBuffer
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final ByteBuffer... buffers) {
		if ((type == null) || (buffers.length < 1)) {
			return null;
		}
		return (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(new BsonByteBufferDeserializeReader(buffers));
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param reader BsonDeserializeReader
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final BsonDeserializeReader reader) {
		if (type == null) {
			return null;
		}
		final T rs = (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(reader);
		return rs;
	}

	// ----------------------------------------------------convertTo--------------------------------------------------------

	/**
	 * 序列化操作.
	 *
	 * @param type Type
	 * @param value Object
	 * @return byte[]
	 */
	public byte[] convertTo(final Type type, final Object value) {
		if (type == null) {
			return null;
		}
		final BsonSerializeWriter out = BsonConvert.writerPool.get();
		this.convertFactory.loadSerializeParser(type).convertTo(out, value);
		final byte[] result = out.toArray();
		BsonConvert.writerPool.offer(out);
		return result;
	}

	/**
	 * 序列化操作.
	 *
	 * @param value Object
	 * @return byte[]
	 */
	public byte[] convertTo(final Object value) {
		if (value == null) {
			final BsonSerializeWriter out = BsonConvert.writerPool.get();
			out.writeNull();
			final byte[] result = out.toArray();
			BsonConvert.writerPool.offer(out);
			return result;
		}
		return this.convertTo(value.getClass(), value);
	}

	/**
	 * 序列化操作.
	 *
	 * @param out OutputStream
	 * @param value the value
	 */
	public void convertTo(final OutputStream out, final Object value) {
		if (value == null) {
			new BsonStreamSerializeWriter(out).writeNull();
		} else {
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(new BsonStreamSerializeWriter(out), value);
		}
	}

	/**
	 * 序列化操作.
	 *
	 * @param out OutputStream
	 * @param type Type
	 * @param value Object
	 */
	public void convertTo(final OutputStream out, final Type type, final Object value) {
		if (type == null) {
			return;
		}
		if (value == null) {
			new BsonStreamSerializeWriter(out).writeNull();
		} else {
			this.convertFactory.loadSerializeParser(type).convertTo(new BsonStreamSerializeWriter(out), value);
		}
	}

	/**
	 * 序列化操作.
	 *
	 * @param supplier Supplier
	 * @param type Type
	 * @param value Object
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] convertTo(final Supplier<ByteBuffer> supplier, final Type type, final Object value) {
		if ((supplier == null) || (type == null)) {
			return null;
		}
		final BsonByteBufferSerializeWriter out = new BsonByteBufferSerializeWriter(supplier);
		if (value == null) {
			out.writeNull();
		} else {
			this.convertFactory.loadSerializeParser(type).convertTo(out, value);
		}
		return out.toBuffers();
	}

	/**
	 * 序列化操作.
	 *
	 * @param supplier Supplier
	 * @param value Object
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] convertTo(final Supplier<ByteBuffer> supplier, final Object value) {
		if (supplier == null) {
			return null;
		}
		final BsonByteBufferSerializeWriter out = new BsonByteBufferSerializeWriter(supplier);
		if (value == null) {
			out.writeNull();
		} else {
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(out, value);
		}
		return out.toBuffers();
	}

	/**
	 * 序列化操作.
	 *
	 * @param writer BsonSerializeWriter
	 * @param value Object
	 */
	public void convertTo(final BsonSerializeWriter writer, final Object value) {
		if (value == null) {
			writer.writeNull();
		} else {
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(writer, value);
		}
	}

	/**
	 * 序列化操作.
	 *
	 * @param writer BsonSerializeWriter
	 * @param type Type
	 * @param value Object
	 */
	public void convertTo(final BsonSerializeWriter writer, final Type type, final Object value) {
		if (type == null) {
			return;
		}
		this.convertFactory.loadSerializeParser(type).convertTo(writer, value);
	}

	/**
	 * 获取BsonSerializeWriter序列化输出流.
	 *
	 * @param value Object
	 * @return BsonSerializeWriter
	 */
	public BsonSerializeWriter convertToWriter(final Object value) {
		if (value == null) {
			return null;
		}
		return this.convertToWriter(value.getClass(), value);
	}

	/**
	 * 获取BsonSerializeWriter序列化输出流.
	 *
	 * @param type Type
	 * @param value Object
	 * @return BsonSerializeWriter
	 */
	public BsonSerializeWriter convertToWriter(final Type type, final Object value) {
		if (type == null) {
			return null;
		}
		final BsonSerializeWriter out = BsonConvert.writerPool.get();
		this.convertFactory.loadSerializeParser(type).convertTo(out, value);
		return out;
	}

}
