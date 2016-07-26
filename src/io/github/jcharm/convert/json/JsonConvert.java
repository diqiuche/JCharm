/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.Convert;

/**
 * JSON双向序列化类.
 */
public final class JsonConvert extends Convert<JsonDeserializeReader, JsonSerializeWriter> {

	private static final ObjectPool<JsonDeserializeReader> READERPOOL = JsonDeserializeReader.createPool(16);

	private static final ObjectPool<JsonSerializeWriter> WRITERPOOL = JsonSerializeWriter.createPool(16);

	/**
	 * 构造函数.
	 *
	 * @param jsonConvertFactory JsonConvertFactory
	 */
	protected JsonConvert(final JsonConvertFactory jsonConvertFactory) {
		super(jsonConvertFactory);
	}

	@Override
	public JsonConvertFactory getConvertFactory() {
		return (JsonConvertFactory) super.getConvertFactory();
	}

	/**
	 * 获取双向序列化类实例.
	 *
	 * @return JsonConvert
	 */
	public static JsonConvert instance() {
		return JsonConvertFactory.instance().getConvert();
	}

	// -----------------------------------------JsonDeserializeReader------------------------------------------------

	/**
	 * 获取JsonByteBufferDeserializeReader反序列化输入流.
	 *
	 * @param buffers ByteBuffer
	 * @return JsonDeserializeReader
	 */
	public JsonDeserializeReader pollJsonDeserializeReader(final ByteBuffer... buffers) {
		return new JsonByteBufferDeserializeReader(buffers);
	}

	/**
	 * 获取JsonStreamDeserializeReader反序列化输入流.
	 *
	 * @param in InputStream
	 * @return the JsonDeserializeReader
	 */
	public JsonDeserializeReader pollJsonDeserializeReader(final InputStream in) {
		return new JsonStreamDeserializeReader(in);
	}

	/**
	 * 获取当前JsonConvert的JsonDeserializeReader反序列化输入流.
	 *
	 * @return JsonDeserializeReader
	 */
	public JsonDeserializeReader pollJsonDeserializeReader() {
		return JsonConvert.READERPOOL.get();
	}

	/**
	 * 将JsonDeserializeReader反序列化输入流存放到对象池中.
	 *
	 * @param in JsonDeserializeReader
	 */
	public void offerJsonReader(final JsonDeserializeReader in) {
		if (in != null) {
			JsonConvert.READERPOOL.offer(in);
		}
	}

	// -------------------------------------------JsonSerializeWriter----------------------------------------------

	/**
	 * 获取JsonByteBufferSerializeWriter序列化输出流.
	 *
	 * @param supplier Supplier
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter pollJsonWriter(final Supplier<ByteBuffer> supplier) {
		return new JsonByteBufferSerializeWriter(supplier);
	}

	/**
	 * 获取JsonStreamByteBufferSerializeWriter序列化输出流.
	 *
	 * @param out OutputStream
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter pollJsonWriter(final OutputStream out) {
		return new JsonStreamSerializeWriter(out);
	}

	/**
	 * 获取JsonStreamByteBufferSerializeWriter序列化输出流.
	 *
	 * @param charset Charset
	 * @param out OutputStream
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter pollJsonWriter(final Charset charset, final OutputStream out) {
		return new JsonStreamSerializeWriter(charset, out);
	}

	/**
	 * 获取当前JsonConvert的JsonSerializeWriter序列化输出流.
	 *
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter pollJsonWriter() {
		return JsonConvert.WRITERPOOL.get();
	}

	/**
	 * 将JsonSerializeWriter序列化输出流存放到对象池中.
	 *
	 * @param out JsonSerializeWriter
	 */
	public void offerJsonSerializeWriter(final JsonSerializeWriter out) {
		if (out != null) {
			JsonConvert.WRITERPOOL.offer(out);
		}
	}

	// ------------------------------------------convertFrom-----------------------------------------------

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param text String
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final String text) {
		if (text == null) {
			return null;
		}
		return this.convertFrom(type, text.toCharArray());
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param text char[]
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final char[] text) {
		if (text == null) {
			return null;
		}
		return this.convertFrom(type, text, 0, text.length);
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param text char[]
	 * @param start int
	 * @param len int
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final char[] text, final int start, final int len) {
		if ((text == null) || (type == null)) {
			return null;
		}
		final JsonDeserializeReader in = JsonConvert.READERPOOL.get();
		in.setText(text, start, len);
		final T rs = (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(in);
		JsonConvert.READERPOOL.offer(in);
		return rs;
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
		return (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(new JsonStreamDeserializeReader(in));
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
		if ((type == null) || (buffers == null) || (buffers.length == 0)) {
			return null;
		}
		return (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(new JsonByteBufferDeserializeReader(buffers));
	}

	/**
	 * 反序列化操作.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type Type
	 * @param reader JsonDeserializeReader
	 * @return T
	 */
	public <T> T convertFrom(final Type type, final JsonDeserializeReader reader) {
		if (type == null) {
			return null;
		}
		final T rs = (T) this.convertFactory.loadDeSerializeParser(type).convertFrom(reader);
		return rs;
	}

	// -------------------------------------------convertTo----------------------------------------------

	/**
	 * 序列化操作.
	 *
	 * @param value Object
	 * @return String
	 */
	public String convertTo(final Object value) {
		if (value == null) {
			return "null";
		}
		return this.convertTo(value.getClass(), value);
	}

	/**
	 * 序列化操作.
	 *
	 * @param type Type
	 * @param value Object
	 * @return String
	 */
	public String convertTo(final Type type, final Object value) {
		if (type == null) {
			return null;
		}
		if (value == null) {
			return "null";
		}
		final JsonSerializeWriter out = JsonConvert.WRITERPOOL.get();
		this.convertFactory.loadSerializeParser(type).convertTo(out, value);
		final String result = out.toString();
		JsonConvert.WRITERPOOL.offer(out);
		return result;
	}

	/**
	 * 序列化操作.
	 *
	 * @param out OutputStream
	 * @param value Object
	 */
	public void convertTo(final OutputStream out, final Object value) {
		if (value == null) {
			new JsonStreamSerializeWriter(out).writeNull();
		} else {
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(new JsonStreamSerializeWriter(out), value);
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
			new JsonStreamSerializeWriter(out).writeNull();
		} else {
			this.convertFactory.loadSerializeParser(type).convertTo(new JsonStreamSerializeWriter(out), value);
		}
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
		final JsonByteBufferSerializeWriter out = new JsonByteBufferSerializeWriter(null, supplier);
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
	 * @param supplier Supplier
	 * @param type Type
	 * @param value Object
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] convertTo(final Supplier<ByteBuffer> supplier, final Type type, final Object value) {
		if ((supplier == null) || (type == null)) {
			return null;
		}
		final JsonByteBufferSerializeWriter out = new JsonByteBufferSerializeWriter(null, supplier);
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
	 * @param writer JsonSerializeWriter
	 * @param value Object
	 */
	public void convertTo(final JsonSerializeWriter writer, final Object value) {
		if (value == null) {
			writer.writeNull();
		} else {
			this.convertFactory.loadSerializeParser(value.getClass()).convertTo(writer, value);
		}
	}

	/**
	 * 序列化操作.
	 *
	 * @param writer JsonSerializeWriter
	 * @param type Type
	 * @param value Object
	 */
	public void convertTo(final JsonSerializeWriter writer, final Type type, final Object value) {
		if (type == null) {
			return;
		}
		if (value == null) {
			writer.writeNull();
		} else {
			this.convertFactory.loadSerializeParser(type).convertTo(writer, value);
		}
	}

	/**
	 * 获取JsonSerializeWriter序列化输出流.
	 *
	 * @param value Object
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter convertToWriter(final Object value) {
		if (value == null) {
			return null;
		}
		return this.convertToWriter(value.getClass(), value);
	}

	/**
	 * 获取JsonSerializeWriter序列化输出流.
	 *
	 * @param type Type
	 * @param value Object
	 * @return JsonSerializeWriter
	 */
	public JsonSerializeWriter convertToWriter(final Type type, final Object value) {
		if (type == null) {
			return null;
		}
		final JsonSerializeWriter out = JsonConvert.WRITERPOOL.get();
		this.convertFactory.loadSerializeParser(type).convertTo(out, value);
		return out;
	}

}
