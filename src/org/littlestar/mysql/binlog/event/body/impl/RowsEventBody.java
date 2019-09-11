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

package org.littlestar.mysql.binlog.event.body.impl;

import java.math.BigDecimal;
import java.nio.ByteOrder;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;
import org.littlestar.mysql.binlog.event.body.helper.RowImage;
import org.littlestar.mysql.binlog.event.body.helper.TableDefine;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.helper.ColumnDefine;
import org.littlestar.mysql.binlog.event.body.helper.ColumnImage;

/**
 * ROWS_EVENT: three basic kinds of ROWS_EVENT exist: 
 * 
 * Event               SQL Command  rows Contents 
 * --------------------+------------+---------------------------------------------------------
 * WRITE_ROWS_EVENT    INSERT       the row data to insert 
 * DELETE_ROWS_EVENT   DELETE       as much data as needed to identify a row 
 * UPDATE_ROWS_EVENT   UPDATE       as much data as needed to identify a row + the data to change  
 * 
 * References:
 * https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html
 * https://dev.mysql.com/doc/internals/en/rows-event.html
 * 
 * Post-header:
 *   if post_header_len == 6 {
 *     4                    table id
 *   } else {
 *     6                    table id
 *   }
 *   2                    flags
 *   if version == 2 {
 *     2                    extra-data-length
 *     string.var_len       extra-dat
 *   }
 *   
 * Payload:  :
 *   lenenc_int           number of columns
 *   string.var_len       columns-present-bitmap1, length: (num of columns+7)/8
 *   if UPDATE_ROWS_EVENTv1 or v2 {
 *     string.var_len       columns-present-bitmap2, length: (num of columns+7)/8
 *   }
 *  
 *   rows:
 *   string.var_len       nul-bitmap, length (bits set in 'columns-present-bitmap1'+7)/
 *   string.var_len       value of each field as defined in table-map
 *   if UPDATE_ROWS_EVENTv1 or v2 {
 *     string.var_len       nul-bitmap, length (bits set in 'columns-present-bitmap2'+7)/
 *     string.var_len       value of each field as defined in table-map
 *   }
 *  ... repeat rows until event-end
 */

public class RowsEventBody extends EventBodyDefaultImpl {
	private Logger logger = Logger.getLogger(RowsEventBody.class.getName());
	public static final int FLAGS_LENGTH = 2;
	public static final int EXTRA_DATA_LENGTH = 2;
	public static final int NUM_COLUMNS_LENGTH = 2;
	
	private int rowEventVersion ;
	private long tableId ;
	private int flags;
	private byte[] extraData;
	private int columnCount;
	private byte[] columnsBitmap;
	private BitSet presentBitSet;
	private ArrayList<RowImage> rowImages = new ArrayList<RowImage>();
	
	private int pos = 0;
	
	public RowsEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		// table-id
		int postHeaderLength = meta.getPostHeaderLength(EventType.TABLE_MAP_EVENT);
		if (postHeaderLength == 6) {
			tableId = getUnsignedLong(bodyData, pos, pos += 4);
		} else {
			tableId = getUnsignedLong(bodyData, pos, pos += 6);
		}
		// flags
		flags = getUnsignedInteger(bodyData, pos, pos += FLAGS_LENGTH);
		//extra-dat
		rowEventVersion = getRowEventVersion(meta.getServerVersion());
		if (rowEventVersion == 2) {
			extraData = this.getBytes(bodyData, pos, pos += EXTRA_DATA_LENGTH);
		}
		
		//number of columns
		byte[] rawColumnCount = getPackedInteger(bodyData, pos);
		columnCount = getUnsignedInteger(rawColumnCount);
		pos += rawColumnCount.length;
		
		// present-bitmap
		int persentBitMapLength = (columnCount + 7) / 8;
		presentBitSet = getBitSet(bodyData, pos, pos += persentBitMapLength).get(0, columnCount);
		int presentColumns = presentBitSet.cardinality();
		//The row-images
		int nullBitMapLength = (presentColumns + 7) / 8; 
		while (true) {
			long remainBytes = getEventBodyLength() - pos;
			// a row-image should be bigger than checksum-length + null-bitmap-length;
			if (remainBytes < (getChecksumLength() + nullBitMapLength))
				break;
			RowImage rowImage = getRowImage(bodyData, pos, tableId, presentColumns);
			if (!rowImage.isNullImage()) //Skip row-image which all columns is null.
				rowImages.add(rowImage);
			pos = rowImage.getEndPosition();
		}
	}
	
	public long getTableId() {
		return tableId;
	}
	
	public byte[] getExtraData() {
		return extraData;
	}
	
	public byte[] getColumnsBitMap() {
		return columnsBitmap;
	}
	
	public int getRowEventVersion() {
		return rowEventVersion;
	}
	
	public ArrayList<RowImage> getRowImages() {
		return rowImages;
	}
	
	//rows_event.h -> enum_flag:
	public static String getRowsEventFlagsName(int value) {
		if (value == 1) { 
			return " STMT_END_F";
		} else if (value == 2) { 
			return "NO_FOREIGN_KEY_CHECKS_F";
		} else if (value == 4) { 
			return "RELAXED_UNIQUE_CHECKS_F";
		} else if (value == 8) { 
			return "COMPLETE_ROWS_F";
		} else {
			return Integer.toString(value);
		}
	}
	
	/*
	https://dev.mysql.com/doc/internals/en/rows-event.html
	  Version 0 written from MySQL 5.1.0 to 5.1.15 ;
	  Version 1 written from MySQL 5.1.15 to 5.6.x ;
	  Version 2 written from MySQL 5.6.x ;
	*/ 
	public int getRowEventVersion(String serverVersion) {
		if (ParserHelper.compareVersion(serverVersion, "5.6") < 0) {
			if (ParserHelper.compareVersion(serverVersion, "5.1.15") < 0) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 2;
		}
	}
	
	/*
	public int getRowEventVersion(EventType eventType) {
		String eventName = eventType.toString();
		String lastChar = eventName.substring(eventName.length() - 1);
		int version = -1;
		try {
			Integer.parseInt(lastChar);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Parsing last char of '" + eventName + " failed. ");
		}
		return version;
	}
	*/
	
	// Get value of each field base on table define;
	// 如何解析每种数据类型的值，需要参考log_event.cc中的log_event_print_value（...)函数；
	private RowImage getRowImage(final byte[] bodyData, final int startPos, final long tableId, final int presentColumns) {
		int pos = startPos;
		RowImage rowImage = new RowImage(tableId);
		rowImage.setStartPosition(startPos);
		int nullBitMapLength = (presentColumns + 7) / 8; // null-bitmap
		BitSet nullBitSet = getBitSet(bodyData, pos, pos += nullBitMapLength);
		
		TableMapEventBody tableMapEventData = meta.getTableMapEventBody(tableId);
		TableDefine tableDefine = tableMapEventData.getTableDefine();
		
		final int columnCount = tableDefine.getColumnCount();
		// For UPDATE_ROWS_EVENT, Check if all columns is null;
		boolean isAllNull = true;
		for (int i = 0; i < columnCount; i++) {
			if (!nullBitSet.get(i)) {
				isAllNull = false;
				break;
			}
		}
		rowImage.isNullImage(isAllNull);
		for (int col = 0; col < columnCount; col++) {
			ColumnDefine colDef = tableDefine.getColumnDefine(col);
			String charsetName = null;
			if (meta.decodeString()) {
				charsetName = meta.getDefaultCharsetName();
				if (colDef.getCharsetName() != null)
					charsetName = colDef.getCharsetName();
			}
			
			ColumnImage columnImage = new ColumnImage(col);
			if (nullBitSet.get(col)) { // skip null column
				columnImage.setColumnValue(null);
			} else {
				byte cTypeRaw = colDef.getColumnType();
				int cType = ParserHelper.getUnsignedInteger(cTypeRaw);
				switch(cType) {
				//---- Fixed Length Columns -----
				case 0x01:   // TINY  --> 1 byte
					columnImage.setColumnValue(getInteger(bodyData, pos, pos += 1));
					break;
				case 0x02:   // SHORT --> 2 bytes
					columnImage.setColumnValue(getInteger(bodyData, pos, pos += 2));
					break;
				case 0x03:   // LONG  --> 4 bytes
					columnImage.setColumnValue(getInteger(bodyData, pos, pos += 4));
					break;
				case 0x04:   // FLOAT:  4 bytes, IEEE 754 single precision format 
					columnImage.setColumnValue(getFloat(bodyData, pos, pos += 4));
					break;
				case 0x05:   // DOUBLE: 8 bytes, IEEE 754 double precision format 
					columnImage.setColumnValue(getDouble(bodyData, pos, pos += 8));
					break;
				case 0x06:   // NULL
				case 0x08:   // LONGLONG: 8 bytes
					columnImage.setColumnValue(getLong(bodyData, pos, pos += 8));
					break;
				case 0x09:   // INT24   : 4 bytes
					columnImage.setColumnValue(getInteger(bodyData, pos, pos += 4));
					break;
				case 0xfe: // ProtocolBinary::MYSQL_TYPE_STRING;  
					Object[] lenEncString = getPackedString(bodyData, pos);
					int strLen = (int)lenEncString[0];
					byte[] rawStringValue = (byte[])lenEncString[1];
					pos += strLen;
					if (charsetName != null) {
						columnImage.setColumnValue(ParserHelper.getString(rawStringValue, charsetName));
					} else {
						columnImage.setColumnValue(rawStringValue);
					}
					break;
				case 0x0f: // ProtocolBinary::MYSQL_TYPE_VARCHAR; 
				case 0xfd: // ProtocolBinary::MYSQL_TYPE_VAR_STRING; 
					byte[] rawVarcharMeta = colDef.getColumnMeta();
					int varcharMetaValue = getUnsignedInteger(rawVarcharMeta);
					int varcharLength ;
					if (varcharMetaValue < 256) {
						varcharLength = getUnsignedInteger(bodyData, pos, pos += 1);
					} else {
						varcharLength = getUnsignedInteger(bodyData, pos, pos += 2);
					}
					byte[] rawVarcharValue = getBytes(bodyData, pos, pos += varcharLength);
					if (charsetName != null) {
						columnImage.setColumnValue(ParserHelper.getString(rawVarcharValue, charsetName));
					} else {
						columnImage.setColumnValue(rawVarcharValue);
					}
					break;
				case 0x00: // ProtocolBinary::MYSQL_TYPE_DECIMAL; 
				case 0xf6: // ProtocolBinary::MYSQL_TYPE_NEWDECIMAL;
				/*
				 * DECIMAL基本概念： DECIMAL(precision, scale)
				 *   precision (field   length) : 精度，整数位数 + 小数的位数； 
				 *   scale     (decimal places) : 范围，小数的位数；
				 * 
				 * MySQL的Decimal存储需求：
				 * https://dev.mysql.com/doc/refman/8.0/en/precision-math-decimal-characteristics.html
				 * 
				 * 简单的来说就是用4个字节存储9位数，并根据剩余位数(余数)决定剩下的存储空间(详见链接文档).
				 * 
				 * 文档案例： 
				 * DECIMAL(18,9)：整数部分=9位数, 小数部分=9位数； 整数部分需要4 bytes, 小数部分需要4 bytes，总共需要8 bytes存储字段值; 
				 * DECIMAL(20,6)：整数部分=14位数, 小数部分=6位数； 整数部分(14/9)=1(商), 也就是（1*4)=4 bytes, 余数5, 
				 *                余数根据为文档需要3 bytes来存储剩余5位数，合起来就是7 bytes;
				 *                小数部分6，不到9位数， 需要4bytes； 总共需要7+4=11bytes存储字段值.
				 * 
				 * 	对于decimal的二进制格式, 参考"decimal.c"的"decimal2bin"函数说明部分;
				 *  1. 一个重点就是如果存储的是负数, 那么每个字节都是取反的, 也就是需要对每个字节做0xFF的异或(^)操作;
				 *     "If the number is negative - every byte is inversed."
				 *  2. 二进制存储的字节序是固定的"big-endian"与平台无关。 
				 *  
				 */
					byte[] rawDecimalMeta = colDef.getColumnMeta();
					int decimalMetaValue = getUnsignedInteger(rawDecimalMeta);
					
					int precision = decimalMetaValue & 0xFF;
					int scale = decimalMetaValue >> 8;
					
					final int digits_pre_4bytes = 9; // 4 bytes store 9 digits;
					int integerDigits = precision - scale;
					int integerQuotient = integerDigits / digits_pre_4bytes;
					int integerLeftover = integerDigits % digits_pre_4bytes;
					int integerLeftoverBytes = getLeftoverBytes(integerLeftover);
					int integerBytes = 4 * integerQuotient + integerLeftoverBytes;
					
					int fractionQuotient = scale / digits_pre_4bytes;
					int fractionLeftover = scale % digits_pre_4bytes;
					int fractionLeftoverBytes =  getLeftoverBytes(fractionLeftover);
					int fractionBytes = 4 * fractionQuotient + fractionLeftoverBytes;

					byte[] rawDecimalValue = getBytes(bodyData, pos, pos += (integerBytes + fractionBytes));
					
			        boolean negative = (rawDecimalValue[0] & 0x80) == 0x80;
			        
			        rawDecimalValue[0] ^= 0x80;
			        negative = !negative;
			        if (negative) {
			            for (int i = 0; i < rawDecimalValue.length; i++) {
			            	rawDecimalValue[i] ^= 0xFF;
			            }
			        }

			        int offset = 0; //digital offset.
					BigDecimal integerValue = BigDecimal.ZERO;
					if (integerLeftoverBytes > 0) {
						byte[] rawLeftoverValue = getBytes(rawDecimalValue, offset, offset += integerLeftoverBytes);
						long leftoverValue = ParserHelper.getUnsignedInteger(rawLeftoverValue, ByteOrder.BIG_ENDIAN);
						integerValue = BigDecimal.valueOf(leftoverValue);
					}
					
					for (int i = 0; i < integerQuotient; i++) {
						byte[] rawEach4Byte = getBytes(rawDecimalValue, offset, offset += (i + 1) * 4);
						long value = ParserHelper.getUnsignedInteger(rawEach4Byte, ByteOrder.BIG_ENDIAN);
						integerValue = integerValue.movePointRight((i + 1) * digits_pre_4bytes)
								.add(BigDecimal.valueOf(value));
					}
					
					BigDecimal fractionValue = BigDecimal.ZERO;
					for (int i = 0; i < fractionQuotient; i++) {
						byte[] rawEach4Byte = getBytes(rawDecimalValue, offset, offset += (i + 1) * 4);
						long value = ParserHelper.getUnsignedInteger(rawEach4Byte, ByteOrder.BIG_ENDIAN);
						fractionValue = fractionValue
								.add(BigDecimal.valueOf(value).movePointLeft((i + 1) * digits_pre_4bytes));
					}
					
					if (fractionBytes > 0) {
						byte[] rawLeftoverValue = getBytes(rawDecimalValue, offset, offset += fractionLeftoverBytes);
						long leftoverValue = ParserHelper.getUnsignedInteger(rawLeftoverValue, ByteOrder.BIG_ENDIAN);
						fractionValue = fractionValue.add(BigDecimal.valueOf(leftoverValue).movePointLeft(scale));
					}

					BigDecimal decimalValue = integerValue.add(fractionValue);
					if (negative) {
						decimalValue = decimalValue.negate();
					}
					columnImage.setColumnValue(decimalValue);
					break;
				/*
				日期类型：
				https://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html	
				简单来说MySQL 5.6.4引入了新的日期时间类型， TIME2, TIMESTAMP2, and DATETIME2, 在新类型中
				引入了"fractional seconds part". "fractional seconds part"长度是根据这些类型的定义决定的
				比如col1 timestamp(3); 所以这些类型是可变长的，比秒更小的部分的存储长度根据这些列的meta-def来决定。
				*/
				case 0x0d:  // YEAR: 1 byte, little endian 
					byte[] rawYear = getBytes(bodyData, pos, pos += 1);
					int year = ParserHelper.getInteger(rawYear, ByteOrder.LITTLE_ENDIAN);
					columnImage.setColumnValue(year);
					break;	
				case 0x0e:  // NEWDATE ??
				case 0x0a:  // DATE: 3 byte
					byte[] rawDate = getBytes(bodyData, pos, pos += 3);
					int[] dateArray = ParserHelper.getDateV1(rawDate);
					Date date;
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						date = dateFormat.parse(dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2]);
					} catch (Throwable e) {
						logger.log(Level.WARNING, "", e);
						date = null;
					}
					columnImage.setColumnValue(date);
					break;
				case 0x0b:  // TIME: 4 bytes
					byte[] rawTime = getBytes(bodyData, pos, pos += 4);
					int[] timeArray = ParserHelper.getTimeV1(rawTime);
					String timeValue = timeArray[0]+":"+timeArray[1]+":"+timeArray[2];
					columnImage.setColumnValue(timeValue);
					break;	
				case 0x0c:  // DATETIME 8 bytes
					byte[] rawDatePacked = getBytes(bodyData, pos, pos += 4);
					byte[] rawTimePacked = getBytes(bodyData, pos, pos += 4);
					int[] datetimeArray = ParserHelper.getDateTimeV1(rawDatePacked, rawTimePacked);
					String strValue = datetimeArray[0]+"-"+datetimeArray[1]+"-"+datetimeArray[2]
							+" "+datetimeArray[3]+":"+datetimeArray[4]+":"+datetimeArray[5];	
					/*Date dateTime = null;
					try {
						SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						dateTime =dtFormat.parse(strValue);
					} catch (ParseException e) {
					}*/
					columnImage.setColumnValue(strValue);
					break;
				case 0x07: // TIMESTAMP
					byte[] rawTimestamp = getBytes(bodyData, pos, pos += 4);
					int tsValue = ParserHelper.getTimestampV1(rawTimestamp);
					Date ts = new Date(tsValue * 1000L);
					columnImage.setColumnValue(ts);
					break;
				case 0x11:  // TIMESTAMP2 encoding for nonfractional part: Same as before 5.6.4, 
					        // except big endian rather than little endian
					byte[] rawTimestamp2 = getBytes(bodyData, pos, pos += 4);
					int dateTimeVAlue = ParserHelper.getTimestampV2(rawTimestamp2);
					Date tsDateTime = new Date(dateTimeVAlue * 1000L);
					String timestamp2 = "";
					try {
						SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						timestamp2 = dtFormat.format(tsDateTime);
					} catch (Throwable e) {
						logger.log(Level.WARNING, "", e);
					}
					byte[] rawTimestamp2Meta = colDef.getColumnMeta();
					int ts2Fsp = getUnsignedInteger(rawTimestamp2Meta);
					int ts2FractionLenght = ParserHelper.getTimeFractionalLength(ts2Fsp);
					if (ts2FractionLenght > 0) {
						byte[] rawTs2Fraction = getBytes(bodyData, pos, pos += ts2FractionLenght);
						int ts2Faction = ParserHelper.getUnsignedInteger(rawTs2Fraction, ByteOrder.BIG_ENDIAN);
						timestamp2 += ("." + ts2Faction);
					}
					columnImage.setColumnValue(timestamp2);
					break;
				case 0x12:  // DATETIME2 5 bytes + fractional-seconds storage, big endian 
					byte[] rawDateTime2 = getBytes(bodyData, pos , pos += 5);
					int[] dateTime2Array = ParserHelper.getDateTimeV2(rawDateTime2);
					String dateTime2 = dateTime2Array[0] + "-" 
							+ ParserHelper.getFixedLengthString(dateTime2Array[1], 2) + "-"
							+ ParserHelper.getFixedLengthString(dateTime2Array[2], 2) + " "
							+ ParserHelper.getFixedLengthString(dateTime2Array[3], 2) + ":"
							+ ParserHelper.getFixedLengthString(dateTime2Array[4], 2) + ":"
							+ ParserHelper.getFixedLengthString(dateTime2Array[5], 2);
					byte[] rawDt2Meta = colDef.getColumnMeta();
					int dt2Fsp = getUnsignedInteger(rawDt2Meta);
					int dt2FractionLenght = ParserHelper.getTimeFractionalLength(dt2Fsp);
					if(dt2FractionLenght > 0) {
						byte[] rawDt2Fraction = getBytes(bodyData, pos , pos += dt2FractionLenght);
						int dt2Faction = ParserHelper.getInteger(rawDt2Fraction, ByteOrder.BIG_ENDIAN);
						dateTime2 += dt2Faction;
					}
					columnImage.setColumnValue(dateTime2);
					break;
				case 0x13:  // TIME2: 3 bytes + fractional-seconds storage, big endian
					byte[] rawTime2 = getBytes(bodyData, pos , pos += 3);
					int[] time2Array = ParserHelper.getTimeV2(rawTime2);
					String time2 = time2Array[0] * time2Array[1] + ":" + time2Array[2] + ":" + time2Array[3];
					byte[] rawTime2Meta = colDef.getColumnMeta();
					int tFsp = getUnsignedInteger(rawTime2Meta);
					int tFractionLenght = ParserHelper.getTimeFractionalLength(tFsp);
					if(tFractionLenght > 0) {
						byte[] rawFraction = getBytes(bodyData, pos , pos += tFractionLenght);
						int faction = ParserHelper.getInteger(rawFraction, ByteOrder.BIG_ENDIAN);
						time2 += faction;
					}
					columnImage.setColumnValue(time2);
					break;
				case 0xfc: // ProtocolBinary::MYSQL_TYPE_BLOB; 
					byte[] rawBlobMeta = colDef.getColumnMeta();
					int blobMetaValue = getUnsignedInteger(rawBlobMeta);
					/*
					switch (blobMetaValue) {
					case 1: // "TINYBLOB/TINYTEXT"
					case 2: // "BLOB/TEXT"
					case 3: // "MEDIUMBLOB/MEDIUMTEXT"
					case 4: // "LONGBLOB/LONGTEXT"
					default: // Unknown BLOB.
					}*/
					byte[] rawBlobLength = getBytes(bodyData, pos, pos += blobMetaValue);
					int blobLength = getUnsignedInteger(rawBlobLength);
					byte[] rawBlobValue = getBytes(bodyData, pos, pos += blobLength);
					if (charsetName != null) {
						columnImage.setColumnValue(ParserHelper.getString(rawBlobValue, charsetName));
					} else {
						columnImage.setColumnValue(rawBlobValue);
					}
					break;
				/* This enumeration value is only used internally and cannot exist in a binlog.
				case 0xf7: // ProtocolBinary::MYSQL_TYPE_ENUM; 
				case 0xf8: // ProtocolBinary::MYSQL_TYPE_SET;
				case 0xf9: // ProtocolBinary::MYSQL_TYPE_TINY_BLOB; 
				case 0xfa: // ProtocolBinary::MYSQL_TYPE_MEDIUM_BLOB; 
				case 0xfb: // ProtocolBinary::MYSQL_TYPE_LONG_BLOB; 
				*/
				case 0x10: // ProtocolBinary::MYSQL_TYPE_BIT;
					/* Meta-data: bit_len, bytes_in_rec, 2 bytes */
					byte[] rawBitMeta = colDef.getColumnMeta();
					int bitMeta = getUnsignedInteger(rawBitMeta);
					int nbits = ((bitMeta >> 8) * 8) + (bitMeta & 0xFF);
					int bitLen = (nbits + 7) / 8;
					byte[] rawBitValue = getBytes(bodyData, pos, pos += bitLen);
					columnImage.setColumnValue(rawBitValue);
					break;
				case 0xff: // ProtocolBinary::MYSQL_TYPE_GEOMETRY;
					byte[] rawGeometryMeta = colDef.getColumnMeta();
					int geometryMetaValue = getUnsignedInteger(rawGeometryMeta);
					int geometryLength =  getUnsignedInteger(bodyData, pos, pos += geometryMetaValue);
					byte[] rawGeometryValue = getBytes(bodyData, pos, pos += geometryLength);
					columnImage.setColumnValue(rawGeometryValue);
					break;
				}// end of switch statement 
			}// end of if...else statement 
			rowImage.setColumnImage(columnImage);
		} // end of for statement
		rowImage.setEndPosition(pos);
		return rowImage;
	}
	
	private static int getLeftoverBytes(int leftover) {
		int bytes = 0;
		if (leftover >= 1 && leftover <= 2) {
			bytes = 1;
		} else if (leftover >= 3 && leftover <= 4) {
			bytes = 2;
		} else if (leftover >= 5 && leftover <= 6) {
			bytes = 3;
		} else if (leftover >= 7 && leftover <= 9) {
			bytes = 4;
		}
		return bytes;
	}
	
	@Override
	public String toString() {
		String msg = "table-id="+tableId 
				+", flags="+getRowsEventFlagsName(flags)
				+", columns="+columnCount
				+", present-columns="+presentBitSet;
		int i = 0;
		StringBuilder builder = new StringBuilder();
		builder.append(msg).append("\nrow-images {\n");
		for(RowImage rowImage: rowImages) {
			builder.append("  ROW ").append(i++).append("#: ");
			for (int j = 0; j < rowImage.getColumnCount(); j++) {
				Object colImageValue = rowImage.getColumnImage(j).getColumnValue();
				String strValue;
				if (colImageValue == null) {
					strValue = "null";
				} else {
					strValue = colImageValue.toString();
				}
				builder.append("@").append(j).append("=").append(strValue).append(", ");
			}
			builder.append("\n");
		}
		builder.append("}\n");
		return builder.toString();
	}
}
