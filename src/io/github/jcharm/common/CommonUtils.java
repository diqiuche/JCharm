/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

/**
 * 实用工具处理类.
 */
public final class CommonUtils {

	/**
	 * 将指定范围的字符数组通过UTF8编码为字节数组.
	 *
	 * @param text char[]
	 * @param start int
	 * @param len int
	 * @return byte[]
	 */
	public static byte[] encodeUTF8ToBytes(final char[] text, final int start, final int len) {
		char c;
		int size = 0;
		final char[] chars = text;
		final int limit = start + len;
		for (int i = start; i < limit; i++) {
			c = chars[i];
			if (c < 0x80) {
				size++;
			} else if (c < 0x800) {
				size += 2;
			} else {
				size += 3;
			}
		}
		final byte[] bytes = new byte[size];
		size = 0;
		for (int i = start; i < limit; i++) {
			c = chars[i];
			if (c < 0x80) {
				bytes[size++] = (byte) c;
			} else if (c < 0x800) {
				bytes[size++] = (byte) (0xc0 | (c >> 6));
				bytes[size++] = (byte) (0x80 | (c & 0x3f));
			} else {
				bytes[size++] = (byte) (0xe0 | ((c >> 12)));
				bytes[size++] = (byte) (0x80 | ((c >> 6) & 0x3f));
				bytes[size++] = (byte) (0x80 | (c & 0x3f));
			}
		}
		return bytes;
	}

	/**
	 * 将指定范围的字节数组通过UTF8解码为字符数组.
	 *
	 * @param array byte[]
	 * @param start int
	 * @param len int
	 * @return char[]
	 */
	public static char[] decodeUTF8ToChars(final byte[] array, final int start, final int len) {
		byte b;
		int size = len;
		final byte[] bytes = array;
		final int limit = start + len;
		for (int i = start; i < limit; i++) {
			b = bytes[i];
			if ((b >> 5) == -2) {
				size--;
			} else if ((b >> 4) == -2) {
				size -= 2;
			}
		}
		final char[] text = new char[size];
		size = 0;
		for (int i = start; i < limit;) {
			b = bytes[i++];
			if (b >= 0) {
				text[size++] = (char) b;
			} else if ((b >> 5) == -2) {
				text[size++] = (char) (((b << 6) ^ bytes[i++]) ^ (((byte) 0xC0 << 6) ^ ((byte) 0x80)));
			} else if ((b >> 4) == -2) {
				text[size++] = (char) ((b << 12) ^ (bytes[i++] << 6) ^ (bytes[i++] ^ (((byte) 0xE0 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80))));
			}
		}
		return text;
	}

	/**
	 * 将字节数组通过UTF8解码为字符数组.
	 *
	 * @param array the array
	 * @return the char[]
	 */
	public static char[] decodeUTF8ToChars(final byte[] array) {
		return CommonUtils.decodeUTF8ToChars(array, 0, array.length);
	}

	/**
	 * 指定范围的字符数组UTF8编码后的大小.
	 *
	 * @param text char[]
	 * @param start int
	 * @param len int
	 * @return int
	 */
	public static int encodeUTF8Length(final char[] text, final int start, final int len) {
		char c;
		int size = 0;
		final char[] chars = text;
		final int limit = start + len;
		for (int i = start; i < limit; i++) {
			c = chars[i];
			size += (c < 0x80 ? 1 : (c < 0x800 ? 2 : 3));
		}
		return size;
	}

	/**
	 * 指定范围的字符数组UTF8编码后的大小(转义字符).
	 *
	 * @param text char[]
	 * @param start int
	 * @param len int
	 * @return int
	 */
	public static int encodeEscapeUTF8Length(final char[] text, final int start, final int len) {
		char c;
		int size = 0;
		final char[] chars = text;
		final int limit = start + len;
		for (int i = start; i < limit; i++) {
			c = chars[i];
			switch (c) {
			case '\n':
				size += 2;
				break;
			case '\r':
				size += 2;
				break;
			case '\t':
				size += 2;
				break;
			case '\\':
				size += 2;
				break;
			case '"':
				size += 2;
				break;
			default:
				size += (c < 0x80 ? 1 : (c < 0x800 ? 2 : 3));
				break;
			}
		}
		return size;
	}

}
