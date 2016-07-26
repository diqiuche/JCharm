/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert.bson;

import java.util.function.Predicate;

import io.github.jcharm.common.CommonUtils;
import io.github.jcharm.common.ConstructCreator;
import io.github.jcharm.common.ObjectPool;
import io.github.jcharm.convert.ConvertException;
import io.github.jcharm.convert.DeSerializeMember;
import io.github.jcharm.convert.DeserializeReader;
import io.github.jcharm.convert.parser.BooleanArraySimpleParser;
import io.github.jcharm.convert.parser.ByteArraySimpleParser;
import io.github.jcharm.convert.parser.CharArraySimpleParser;
import io.github.jcharm.convert.parser.DoubleArraySimpleParser;
import io.github.jcharm.convert.parser.FloatArraySimpleParser;
import io.github.jcharm.convert.parser.IntegerArraySimpleParser;
import io.github.jcharm.convert.parser.LongArraySimpleParser;
import io.github.jcharm.convert.parser.ShortArraySimpleParser;
import io.github.jcharm.convert.parser.StringArraySimpleParser;

/**
 * BSON反序列化输入流.
 */
public class BsonDeserializeReader extends DeserializeReader {

	private static final short SIGN_OBJECTB = (short) 0xBB;

	private static final short SIGN_OBJECTE = (short) 0xEE;

	private static final byte SIGN_HASNEXT = 1;

	private static final byte SIGN_NONEXT = 0;

	/** 字段的类型值, 对应 BsonSerializeWriter.writeField. */
	protected byte typeval;

	/** 位置下标. */
	protected int position = -1;

	private byte[] content;

	/**
	 * 构造函数.
	 */
	public BsonDeserializeReader() {
	}

	/**
	 * 构造函数.
	 *
	 * @param bytes byte[]
	 */
	public BsonDeserializeReader(final byte[] bytes) {
		this.setBytes(bytes, 0, bytes.length);
	}

	/**
	 * 构造函数.
	 *
	 * @param bytes byte[]
	 * @param start int
	 * @param len int
	 */
	public BsonDeserializeReader(final byte[] bytes, final int start, final int len) {
		this.setBytes(bytes, start, len);
	}

	/**
	 * 创建一个存放BsonDeserializeReader的对象池.
	 *
	 * @param max 对象池存放对象的最大值
	 * @return ObjectPool
	 */
	public static ObjectPool<BsonDeserializeReader> createPool(final int max) {
		return new ObjectPool<BsonDeserializeReader>(max, new ConstructCreator<BsonDeserializeReader>() {

			@Override
			public BsonDeserializeReader construct(final Object... params) {
				return new BsonDeserializeReader();
			}
		}, null, new Predicate<BsonDeserializeReader>() {

			@Override
			public boolean test(final BsonDeserializeReader t) {
				return t.recycle();
			}
		});
	}

	/**
	 * 将指定范围的字节数组赋值给字节内容数组.
	 *
	 * @param bytes byte[]
	 * @param start int
	 * @param len int
	 */
	public final void setBytes(final byte[] bytes, final int start, final int len) {
		if (bytes == null) {
			this.position = 0;
		} else {
			this.content = bytes;
			this.position = start - 1;
		}
	}

	/**
	 * 将字节数组赋值给字节内容数组.
	 *
	 * @param bytes byte[]
	 */
	public final void setBytes(final byte[] bytes) {
		if (bytes == null) {
			this.position = 0;
		} else {
			this.setBytes(bytes, 0, bytes.length);
		}
	}

	/**
	 * 是否允重复利用对象池中存储的对象.
	 *
	 * @return boolean
	 */
	protected boolean recycle() {
		this.position = -1;
		this.typeval = 0;
		this.content = null;
		return true;
	}

	protected byte currentByte() {
		return this.content[this.position];
	}

	@Override
	public boolean readBoolean() {
		return this.content[++this.position] == 1;
	}

	@Override
	public byte readByte() {
		return this.content[++this.position];
	}

	@Override
	public char readChar() {
		return (char) ((0xff00 & (this.content[++this.position] << 8)) | (0xff & this.content[++this.position]));
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(this.readLong());
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(this.readInt());
	}

	@Override
	public int readInt() {
		return ((this.content[++this.position] & 0xff) << 24) | ((this.content[++this.position] & 0xff) << 16) | ((this.content[++this.position] & 0xff) << 8) | (this.content[++this.position] & 0xff);
	}

	@Override
	public long readLong() {
		return ((((long) this.content[++this.position] & 0xff) << 56) | (((long) this.content[++this.position] & 0xff) << 48) | (((long) this.content[++this.position] & 0xff) << 40) | (((long) this.content[++this.position] & 0xff) << 32) | (((long) this.content[++this.position] & 0xff) << 24)
				| (((long) this.content[++this.position] & 0xff) << 16) | (((long) this.content[++this.position] & 0xff) << 8) | (((long) this.content[++this.position] & 0xff)));
	}

	@Override
	public short readShort() {
		return (short) ((0xff00 & (this.content[++this.position] << 8)) | (0xff & this.content[++this.position]));
	}

	@Override
	public String readSmallString() {
		final int len = 0xff & this.readByte();
		if (len == 0) {
			return "";
		}
		final String value = new String(this.content, ++this.position, len);
		this.position += len - 1; // 上一行已经++this.position，所以此处要-1
		return value;
	}

	@Override
	public String readString() {
		final int len = this.readInt();
		if (len == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (len == 0) {
			return "";
		}
		final String value = new String(CommonUtils.decodeUTF8ToChars(this.content, ++this.position, len));
		this.position += len - 1;// 上一行已经++this.position，所以此处要-1
		return value;
	}

	@Override
	public int readArrayBegin() {
		final short bt = this.readShort();
		if (bt == DeserializeReader.SIGN_NULL) {
			return bt;
		}
		return ((bt & 0xffff) << 16) | ((this.content[++this.position] & 0xff) << 8) | (this.content[++this.position] & 0xff);
	}

	@Override
	public void readArrayEnd() {
	}

	@Override
	public int readMapBegin() {
		return this.readArrayBegin();
	}

	@Override
	public void readMapEnd() {
	}

	@Override
	public String readObjectBegin(final Class clazz) {
		this.fieldIndex = 0; // 必须要重置为0
		final String newcls = this.readClassName();
		if ((newcls != null) && !newcls.isEmpty()) {
			return newcls;
		}
		final short bt = this.readShort();
		if (bt == DeserializeReader.SIGN_NULL) {
			return null;
		}
		if (bt != BsonDeserializeReader.SIGN_OBJECTB) {
			throw new ConvertException("a bson object must begin with " + (BsonDeserializeReader.SIGN_OBJECTB) + " (position = " + this.position + ") but '" + this.currentByte() + "'");
		}
		return "";
	}

	@Override
	public void readObjectEnd(final Class clazz) {
		if (this.readShort() != BsonDeserializeReader.SIGN_OBJECTE) {
			throw new ConvertException("a bson object must end with " + (BsonDeserializeReader.SIGN_OBJECTE) + " (position = " + this.position + ") but '" + this.currentByte() + "'");
		}
	}

	@Override
	public boolean hasNext() {
		final byte b = this.readByte();
		if (b == BsonDeserializeReader.SIGN_HASNEXT) {
			return true;
		}
		if (b != BsonDeserializeReader.SIGN_NONEXT) {
			throw new ConvertException("hasNext option must be (" + (BsonDeserializeReader.SIGN_HASNEXT) + " or " + (BsonDeserializeReader.SIGN_NONEXT) + ") but '" + b + "' at position(" + this.position + ")");
		}
		return false;
	}

	@Override
	public void skipValue() {
		if (this.typeval == 0) {
			return;
		}
		final byte val = this.typeval;
		this.typeval = 0;
		switch (val) {
		case 1:
			this.readBoolean();
			break;
		case 2:
			this.readByte();
			break;
		case 3:
			this.readShort();
			break;
		case 4:
			this.readChar();
			break;
		case 5:
			this.readInt();
			break;
		case 6:
			this.readLong();
			break;
		case 7:
			this.readFloat();
			break;
		case 8:
			this.readDouble();
			break;
		case 9:
			this.readString();
			break;
		case 101:
			BooleanArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 102:
			ByteArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 103:
			ShortArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 104:
			CharArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 105:
			IntegerArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 106:
			LongArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 107:
			FloatArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 108:
			DoubleArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 109:
			StringArraySimpleParser.INSTANCE.convertFrom(this);
			break;
		case 127:
			BsonConvertFactory.objectDeSerializeParser.convertFrom(this);
			break;
		}
	}

	@Override
	public void readBlank() {
	}

	@Override
	public String readClassName() {
		return this.readSmallString();
	}

	@Override
	public DeSerializeMember readFieldName(final DeSerializeMember[] deSerializeMembers) {
		final String exceptedfield = this.readSmallString();
		this.typeval = this.readByte();
		final int len = deSerializeMembers.length;
		if (this.fieldIndex >= len) {
			this.fieldIndex = 0;
		}
		for (int k = this.fieldIndex; k < len; k++) {
			if (exceptedfield.equals(deSerializeMembers[k].getFieldAttribute().getFieldAliasName())) {
				this.fieldIndex = k;
				return deSerializeMembers[k];
			}
		}
		for (int k = 0; k < this.fieldIndex; k++) {
			if (exceptedfield.equals(deSerializeMembers[k].getFieldAttribute().getFieldAliasName())) {
				this.fieldIndex = k;
				return deSerializeMembers[k];
			}
		}
		return null;
	}

}
