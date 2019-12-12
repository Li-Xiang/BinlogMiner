/*
 * Copyright 2019 Li Xiang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.littlestar.mysql.binlog.parser;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;

import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.impl.RotateEventBody;
import org.littlestar.mysql.binlog.event.body.impl.StopEventBody;

public class ParserHelper {
	private ParserHelper() {
	}

	public static byte[] getReverse(final byte[] bytes) {
		byte[] newBytes = bytes.clone();
		for (int i = 0, length = newBytes.length >> 1; i < length; i++) {
			int j = newBytes.length - 1 - i;
			byte t = newBytes[i];
			newBytes[i] = newBytes[j];
			newBytes[j] = t;
		}
		return newBytes;
	}

	private static int getInt(final byte b) {
		int value = (int) b;
		return value;
	}

	private static int getUInt(final byte b) {
		int value = b & 0xFF;
		return value;
	}

	private static int getInt16(final byte[] bytes, final ByteOrder order) {
		// if (bytes.length < 2) throw new NumberFormatException("Int16 at least 2 bytes.");
		byte[] newBytes = bytes;
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		//little-endian, most of mysql server run on little-endian platform.
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1];
		return (b1 << 8) | b0;
	}

	private static int getUInt16(final byte[] bytes, final ByteOrder order) {
		int si = getInt16(bytes, order);
		return si & 0xFFFF;
	}

	private static int getInt24(final byte[] bytes, final ByteOrder order) throws NumberFormatException {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2];
		return (b2 << 16) | (b1 << 8) | b0;
	}

	private static int getUInt24(final byte[] bytes, final ByteOrder order) {
		int si = getInt24(bytes, order);
		return si & 0xFFFFFF;
	}
	
	private static long getInt32(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		int b3 = newBytes[3];
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}
	
	private static long getUInt32(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		long b3 = newBytes[3];
		return (long) ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0) & 0xFFFFFFFFL;
	}

	private static long getInt40(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		long b3 = newBytes[3] & 0xFFL;
		long b4 = newBytes[4];
		return (b4 << 32) | (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	private static long getUInt40(final byte[] bytes, final ByteOrder order) {
		return getInt40(bytes, order) & 0xFFFFFFFFFFL;
	}

	private static long getInt48(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		long b3 = newBytes[3] & 0xFFL;
		long b4 = newBytes[4] & 0xFFL;
		long b5 = newBytes[5];
		return (b5 << 40) | (b4 << 32) | (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}
	
	private static long getUInt48(final byte[] bytes, final ByteOrder order) {
		return getInt48(bytes, order) & 0xFFFFFFFFFFFFL;
	}
	
	private static long getInt56(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		long b3 = newBytes[3] & 0xFFL;
		long b4 = newBytes[4] & 0xFFL;
		long b5 = newBytes[5] & 0xFFL;
		long b6 = newBytes[6];
		return (b6 << 48) | (b5 << 40) | (b4 << 32) | (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}
	
	private static long getUInt56(final byte[] bytes, final ByteOrder order) {
		return getInt56(bytes, order) & 0xFFFFFFFFFFFFFFL;
	}
	
	private static long getInt64(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		int b0 = newBytes[0] & 0xFF;
		int b1 = newBytes[1] & 0xFF;
		int b2 = newBytes[2] & 0xFF;
		long b3 = newBytes[3] & 0xFFL;
		long b4 = newBytes[4] & 0xFFL;
		long b5 = newBytes[5] & 0xFFL;
		long b6 = newBytes[6] & 0xFFL;
		long b7 = newBytes[7];
		return (b7 << 56) | (b6 << 48) | (b5 << 40) | (b4 << 32) | (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	private static BigInteger getUInt64(final byte[] bytes, final ByteOrder order) {
		byte[] newBytes = bytes.clone();
		if (order.equals(ByteOrder.BIG_ENDIAN))
			newBytes = getReverse(bytes);
		return new BigInteger(1, newBytes);
	}

	private static Number getSignedNumber(byte[] bytes, ByteOrder order) throws NumberFormatException {
		int length = bytes.length;
		if (length == 0) {
			throw new NumberFormatException("null");
		} else if (length == 1) {
			return getInt(bytes[0]);
		} else if (length == 2) {
			return getInt16(bytes, order);
		} else if (length == 3) {
			return getInt24(bytes, order);
		} else if (length == 4) {
			return getInt32(bytes, order);
		} else if (length == 5) {
			return getInt40(bytes, order);
		} else if (length == 6) {
			return getInt48(bytes, order);
		} else if (length == 7) {
			return getInt56(bytes, order);
		} else if (length == 8) {
			return getInt64(bytes, order);
		} else {
			throw new NumberFormatException("unsupport int" + length * 8 + "type");
		}
	}
	
	private static Number getUnsignedNumber(byte[] bytes, ByteOrder order) throws NumberFormatException {
		int length = bytes.length;
		if (length == 0) {
			throw new NumberFormatException("null");
		} else if (length == 1) {
			return getUInt(bytes[0]);
		} else if (length == 2) {
			return getUInt16(bytes, order);
		} else if (length == 3) {
			return getUInt24(bytes, order);
		} else if (length == 4) {
			return getUInt32(bytes, order);
		} else if (length == 5) {
			return getUInt40(bytes, order);
		} else if (length == 6) {
			return getUInt48(bytes, order);
		} else if (length == 7) {
			return getUInt56(bytes, order);
		} else if (length == 8) {
			return getUInt64(bytes, order);
		} else {
			throw new NumberFormatException("unsupport int" + (length * 8) + " type");
		}
	}

	public static int getInteger(byte b) {
		return getInt(b);
	}

	public static int getUnsignedInteger(byte b) {
		return getUInt(b);
	}

	public static int getInteger(byte[] bytes, ByteOrder order) {
		return getSignedNumber(bytes, order).intValue();
	}

	public static int getUnsignedInteger(byte[] bytes, ByteOrder order) {
		return ParserHelper.getUnsignedNumber(bytes, order).intValue();
	}

	public static long getLong(byte[] bytes, ByteOrder order) {
		return getSignedNumber(bytes, order).longValue();
	}

	public static long getUnsignedLong(byte[] bytes, ByteOrder order) {
		return getUnsignedNumber(bytes, order).longValue();
	}

	public static double getDouble(byte[] bytes, ByteOrder order) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(order);
		return buffer.getDouble();
	}

	public static float getFloat(byte[] bytes, ByteOrder order) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(order);
		return buffer.getFloat();
	}
	
	public static String getString(byte[] bytes, String mysqlCharsetName) {
		Charset charset = getCharset(mysqlCharsetName);
		return getString(bytes, charset);
	}

	public static String getString(byte[] bytes, Charset charset) {
		return new String(bytes, charset);
	}
	
	public static Charset getCharset(String mysqlCharsetName) {
		if (mysqlCharsetName == null) {
			return StandardCharsets.UTF_8;
		}
		switch (mysqlCharsetName.trim().toLowerCase()) {
		case "gbk":
			return Charset.forName("gbk");
		case "latin1":
			return StandardCharsets.ISO_8859_1;
		case "gb2312":
			return Charset.forName("gb2312");
		case "ucs2":
			return StandardCharsets.UTF_16;
		default:
			return StandardCharsets.UTF_8;
		}
	}
	
	public static String getString(byte[] bytes) {
		return getString(bytes, StandardCharsets.UTF_8);
	}

	public static String getHexString(byte b) {
		return getHexString(b, true);
	}

	public static String getHexString(byte b, boolean lpad) {
		int v = getUnsignedInteger(b);
		String s = Integer.toHexString(v).toUpperCase();
		if (v < 16 && lpad)
			s = "0" + s;
		return s;
	}

	public static String getHexString(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (bytes == null || bytes.length <= 0) {
			return null;
		}
		for (int i = 0; i < bytes.length; i++) {
			int v = getUnsignedInteger(bytes[i]);
			String hv = Integer.toHexString(v).toUpperCase();
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String getFormattedHexString(byte[] bytes, int rowlen) {
		StringBuilder strValue = new StringBuilder();
		int pos = 0;
		int rows = bytes.length / rowlen;
		for (int i = 0; i < rows; i++) {
			strValue.append(lpad(Integer.toHexString((i) * rowlen), 8, '0') + "h: ");
			for (int j = 0; j < rowlen; j++) {
				byte byteValue = bytes[pos++];
				String hexValue = ParserHelper.getHexString(byteValue);
				strValue.append(hexValue).append(" ");
			}
			strValue.append("\n");
		}
		// less bytes.
		if (pos < bytes.length) {
			strValue.append(lpad(Integer.toHexString((rows) * rowlen), 8, '0') + "h: ");
			for (int k = pos; k < bytes.length; k++) {
				byte byteValue = bytes[pos++];
				String hexValue = ParserHelper.getHexString(byteValue);
				strValue.append(hexValue).append(" ");
			}
			strValue.append("\n");
		}
		return strValue.toString();
	}

	public static String getString(Date d) {
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dtFormat.format(d);
	}

	/**
	 * 用于对比源(source)与目标(target)的软件版本号: 当source > target, 源版本比目标版本高, 返回1; 当source =
	 * target, 源版本与目标版本相等, 返回0; 当source < target, 源版本比目标版本低, 返回-1; 如:
	 * compareVersion("10.2.0.5.0","11.1"); --> -1
	 * compareVersion("10.2.0.5.0","10.2.0.5.3"); --> -1
	 * compareVersion("10.2.0.5.0","10.2.0"); --> 1
	 * compareVersion("10.2.0.5.0","10.2.0.5.0.0"); --> 0
	 * compareVersion("10.2.0.5.0","10.2.0.5.0"); --> 0
	 */
	public static int compareVersion(String source, String target) {
		String[] arrSrc = source.replaceAll("-", ".").split("\\.");
		String[] arrTgt = target.replaceAll("-", ".").split("\\.");
		int maxLength = Math.max(arrSrc.length, arrTgt.length);
		for (int i = 0; i < maxLength; i++) {
			int numSrc = 0;
			if (i < arrSrc.length)
				numSrc = parseInt(arrSrc[i], 0);
			int numTgt = 0;
			if (i < arrTgt.length)
				numTgt = parseInt(arrTgt[i], 0);
			if (numTgt > numSrc) {
				return -1;
			} else if (numTgt < numSrc) {
				return 1;
			}
		}
		return 0;
	}

	private static int parseInt(String v, int d) {
		try {
			return Integer.parseInt(v);
		} catch (Throwable e) {
		}
		return d;
	}
	
	/**
	 * 根据第一个字节的数值来确定该数值的存储字节数。 如果第一个字节数值小于0xfb，则该数值通过1个字节存储；
	 * 如果第一个字节数值等于0xfc，则该数值通过2个字节存储； 如果第一个字节数值等于0xfd，则该数值通过3个字节存储；
	 * 如果第一个字节数值等于0xfe，则该数值通过8个字节存储；
	 **/
	public static int getPackedIntegerLength(final byte firstByte) {
		int b = getUnsignedInteger(firstByte);
		if (b < 0xfb) {
			return 1;
		} else if (b == 0xfc) {
			return 2;
		} else if (b == 0xfd) {
			return 3;
		} else if (b == 0xfe) {
			return 8;
		} else {
			return -1;
		}
	}

	/**
	 * https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::LengthEncodedInteger
	 * 
	 * @param buffer,
	 *            packed-integer-byte-array, 如果还无法确定字节长度，那么送最大值，或者送byte[9];
	 */
	public static byte[] getPackedInteger(final byte[] buffer, int from) {
		long pkgLen = getPackedIntegerLength(buffer[from]);
		return Arrays.copyOfRange(buffer, from, (int) pkgLen + from);
	}

	public static String lpad(String s, int len, char pad) {
		StringBuilder padded = new StringBuilder();
		while ((s.length() + padded.length()) < len) {
			padded.append(pad);
		}
		padded.append(s);
		return padded.toString();
	}

	private static String getBinaryString(byte b) {
		int v = getUnsignedInteger(b);
		String strBin = Integer.toBinaryString(v);
		return lpad(strBin, 8, '0');
	}

	private static String getBinaryString(byte[] bytes, ByteOrder order) {
		StringBuilder builder = new StringBuilder();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(order);
		while (buffer.hasRemaining()) {
			byte b = buffer.get();
			builder.append(getBinaryString(b));
		}
		return builder.toString();
	}

	// LengthEncodedString
	// return array, array[0]=length-encode-string length (bytes);
	// array[1]=length-encode-string data;
	public static Object[] getPackedString(final byte[] data, final int from, ByteOrder order) {
		Object[] retVal = new Object[2];
		int pos = from;
		byte[] rawStringLength = getPackedInteger(data, pos);
		pos += rawStringLength.length;
		int stringLength = getUnsignedInteger(rawStringLength, order);
		byte[] rawStringData = Arrays.copyOfRange(data, pos, pos += stringLength);
		retVal[0] = (rawStringLength.length + rawStringData.length);
		retVal[1] = rawStringData;
		return retVal;
	}

	/**
	 * Read variable-length string. End is indicated by 0x00 byte.
	 */
	public static byte[] getNulTerminatedString(final byte[] bytes, final int from, final int to) {
		if (from >= to)
			return null;
		int max = to - from;
		int eofPos = from;
		byte eofFlag = (byte) 0x00;
		for (int i = 0; i < max; i++) {
			byte b = bytes[eofPos];
			eofPos++;
			if (b == eofFlag)
				break;
		}
		return Arrays.copyOfRange(bytes, from, eofPos);
	}

	public static byte[] getNulTerminatedString(final byte[] bytes, final int from) {
		return getNulTerminatedString(bytes, from, bytes.length);
	}

	/**
	 * https://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html
	 * 
	 * Type Storage before MySQL 5.6.4 Storage as of MySQL 5.6.4
	 * --------------+--------------------------------+------------------------------------------------------------
	 * YEAR 1 byte, little endian Unchanged DATE 3 bytes, little endian Unchanged
	 * TIME 3 bytes, little endian 3 bytes + fractional-seconds storage, big endian
	 * TIMESTAMP 4 bytes, little endian 4 bytes + fractional-seconds storage, big
	 * endian DATETIME 8 bytes, little endian 5 bytes + fractional-seconds storage,
	 * big endian
	 */

	/**
	 * Get the value of date in rows-event: 3 bytes, little endian, integer packed
	 * as YYYY×16×32 + MM×32 + DD
	 * 
	 * @param bytes
	 *            the byte[] of value.
	 * @return the value of integer array: array[0]=year, array[1]=month,
	 *         array[2]=day.
	 */
	public static int[] getDateV1(byte[] bytes) {
		int dateValue = ParserHelper.getInteger(bytes, ByteOrder.LITTLE_ENDIAN);
		int[] result = new int[3];
		result[2] = dateValue % 32;
		dateValue >>>= 5;
		result[1] = dateValue % 16;
		result[0] = dateValue >> 4;
		return result;
	}

    public static String getFixedLengthString(int value, int len) {
        return lpad(Integer.toString(value), len, '0');
    }

	/**
	 * Get the value of time v1 in rows-event: 3 bytes, little endian, integer
	 * packed as DD×24×3600 + HH×3600 + MM×60 + SS
	 * 
	 * @param bytes
	 *            the byte[] of value.
	 * @return the value of integer array: array[1]=hours, array[2]=minutes,
	 *         array[3]=seconds
	 */
	public static int[] getTimeV1(byte[] bytes) {
		int[] result = new int[3];
		int timeValue = ParserHelper.getInteger(bytes, ByteOrder.LITTLE_ENDIAN);

		int seconds = Math.abs(timeValue % 60);
		timeValue -= seconds;

		int msInSeconds = Math.abs(timeValue % 3600);
		int minutes = msInSeconds / 60;
		timeValue -= msInSeconds;

		int hsInSeconds = Math.abs(timeValue % 86400);
		int hours = Math.abs(hsInSeconds / 3600);
		int dsInHours = timeValue / 86400;
		if (dsInHours < 0) {
			hours = -hours;
			hours += dsInHours;
		} else {
			hours += dsInHours;
		}
		result[0] = hours;
		result[1] = minutes;
		result[2] = seconds;

		return result;
	}

	/**
	 * Get the (non-fractional part of) value of time v2 in rows-event: 1 bit sign
	 * (1= non-negative, 0= negative) 1 bit unused (reserved for future extensions)
	 * 10 bits hour (0-838) 6 bits minute (0-59) 6 bits second (0-59)
	 * --------------------- 24 bits = 3 bytes
	 * 
	 * @param bytes
	 *            the byte[] of value.
	 * @return the value of integer array: array[0] = sign, 1=non-negative,
	 *         -1=negative; array[1] = hour; array[2] = minute; array[3] = second;
	 */

	public static int[] getTimeV2(byte[] bytes) {
		int[] result = new int[4];
		String binaryString = getBinaryString(bytes, ByteOrder.BIG_ENDIAN);
		int pos = 0;
		String binSign = binaryString.substring(pos, pos += 1);
		pos++; // skip unused bit;
		String binHour = binaryString.substring(pos, pos += 10);
		String binMinute = binaryString.substring(pos, pos += 6);
		String binSecond = binaryString.substring(pos, pos += 6);
		int sign = 1;
		if (binSign == "0") {
			sign = -1;
		}
		int hour = Integer.parseInt(binHour, 2);
		int minute = Integer.parseInt(binMinute, 2);
		int second = Integer.parseInt(binSecond, 2);

		result[0] = sign;
		result[1] = hour;
		result[2] = minute;
		result[3] = second;
		return result;
	}

	/*
	 * 解析Binlog的datetime类型的值，返回整数数组： int[0]=year, int[1]=m Note: A four-byte integer
	 * for date packed as YYYY×10000 + MM×100 + DD and a four-byte integer for time
	 * packed as HH×10000 + MM×100 + SS
	 */

	public static int[] getDateTimeV1(byte[] rawDate, byte[] rawTime) {
		int[] result = new int[6];
		int dateValue = ParserHelper.getInteger(rawDate, ByteOrder.LITTLE_ENDIAN);
		int day = dateValue % 100;
		dateValue -= day;
		int month = dateValue % 10000;
		dateValue -= month;
		int year = dateValue / 10000;
		result[0] = year;
		result[1] = month;
		result[2] = day;

		int timeValue = ParserHelper.getInteger(rawTime, ByteOrder.LITTLE_ENDIAN);
		int second = timeValue % 100;
		timeValue -= second;
		int minute = timeValue % 10000;
		timeValue -= minute;
		int hour = timeValue / 10000;

		result[3] = hour;
		result[4] = minute;
		result[5] = second;
		return result;
	}

	/*
	 * DATETIME encoding for nonfractional part: 1 bit sign (1= non-negative, 0=
	 * negative) 17 bits year*13+month (year 0-9999, month 0-12) 5 bits day (0-31) 5
	 * bits hour (0-23) 6 bits minute (0-59) 6 bits second (0-59)
	 * --------------------------- 40 bits = 5 bytes
	 */
	public static int[] getDateTimeV2(byte[] bytes) {
		int[] result = new int[6];
		String binaryString = getBinaryString(bytes, ByteOrder.BIG_ENDIAN);
		int pos = 0;

		// The sign bit is always 1. A value of 0 (negative) is reserved.
		pos++;
		String binYearMonth = binaryString.substring(pos, pos += 17);
		String binDay = binaryString.substring(pos, pos += 5);
		String binHour = binaryString.substring(pos, pos += 5);
		String binMinute = binaryString.substring(pos, pos += 6);
		String binSecond = binaryString.substring(pos, pos += 6);

		int yearMonth = Integer.parseInt(binYearMonth, 2);
		int year = yearMonth / 13;
		int month = yearMonth % 13;
		int day = Integer.parseInt(binDay, 2);
		int hour = Integer.parseInt(binHour, 2);
		int minute = Integer.parseInt(binMinute, 2);
		int second = Integer.parseInt(binSecond, 2);

		result[0] = year;
		result[1] = month;
		result[2] = day;
		result[3] = hour;
		result[4] = minute;
		result[5] = second;
		return result;
	}

	public static int getTimestampV1(byte[] bytes) {
		int timestampValue = ParserHelper.getInteger(bytes, ByteOrder.LITTLE_ENDIAN);
		return timestampValue;
	}

	public static int getTimestampV2(byte[] bytes) {
		int timestampValue = ParserHelper.getInteger(bytes, ByteOrder.BIG_ENDIAN);
		return timestampValue;
	}

	public static BitSet getBitSet(byte[] bytes, ByteOrder order) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(order);
		BitSet bitSet = BitSet.valueOf(buffer);
		return bitSet;
	}
	
	public static String isBinlogFileEof(BinlogEvent event) {
		String next = null;
		EventType type = event.getHeader().getEventType();
		EventBody body = event.getBody();
		if (type.equals(EventType.ROTATE_EVENT)) {
			RotateEventBody rotateEventBody = (RotateEventBody) body;
			next = rotateEventBody.getNextBinlogName();
		} else if (type.equals(EventType.STOP_EVENT)) {
			StopEventBody stopEventBody = (StopEventBody) body;
			next = stopEventBody.assumeNextFile();
		}
		return next;
	}

	/*
	 * Fractional-part encoding depends on the fractional seconds precision (FSP).
	 * FSP     Storage 
	 * -------+----------- 
	 * 0       0 bytes 
	 * 1,2     1 byte 
	 * 3,4     2 bytes 
	 * 5,6     3 bytes
	 */
	public static int getTimeFractionalLength(int fsp) {
		int bytes = (fsp + 1) / 2;
		return bytes;
	}

	public static String getFormattedUuid(byte[] rawUuid) {
		String hexUuid = getHexString(rawUuid);
		String formmattedUuid = hexUuid.substring(0, 8) + "-" + hexUuid.substring(8, 12) + "-"
				+ hexUuid.substring(12, 16) + "-" + hexUuid.substring(16, 20) + "-" + hexUuid.substring(20, 32);
		return formmattedUuid.toLowerCase();
	}		
}
