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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.littlestar.mysql.binlog.parser.ParserHelper;

public class TableMapEventOptionalMetaData {

	// rows_event.h -> enum Optional_metadata_field_type
	public static final int SIGNEDNESS = 1;
	public static final int DEFAULT_CHARSET = 2;
	public static final int COLUMN_CHARSET = 3;
	public static final int COLUMN_NAME = 4;
	public static final int SET_STR_VALUE = 5;
	public static final int ENUM_STR_VALUE = 6;
	public static final int GEOMETRY_TYPE = 7;
	public static final int SIMPLE_PRIMARY_KEY = 8;
	public static final int PRIMARY_KEY_WITH_PREFIX = 9;
	public static final int ENUM_AND_SET_DEFAULT_CHARSET = 10;
	public static final int ENUM_AND_SET_COLUMN_CHARSET = 11;

	private final HashMap<Integer, byte[]> optionalMetaData;
	private final ByteOrder order;

	public TableMapEventOptionalMetaData(final byte[] bodyData, final int startPos, final int endPos,
			final ByteOrder order) {
		optionalMetaData = new HashMap<Integer, byte[]>();
		this.order = order;
		int pos = startPos;
		while ((endPos - pos) > 1) {
			/*
			 * Optional metadata fields are stored in TLV fields. Type, Length, Value(TLV) format：
			 *  +-----------------------------------------------+
			 *  |         Optional metadata fields              |
			 *  +------------+--------------------+-------------+
			 *  | field-type | field-value-length | field-value |  
			 *  +------------+--------------------+-------------+
			 *  |                  ......                       |
			 *  
			 * Filed elements bytes : 
			 *  field-type         = 1 byte; 
			 *  field-value-length = length of packed integer; 
			 *  field-value        = value of field-value-length;
			 * */
			byte[] rawFieldType = Arrays.copyOfRange(bodyData, pos, pos += 1);
			int fieldType = ParserHelper.getInteger(rawFieldType, order);
			byte[] rawFieldLength = ParserHelper.getPackedInteger(bodyData, pos);
			pos += rawFieldLength.length;
			int fieldLength = ParserHelper.getUnsignedInteger(rawFieldLength, order);
			if (fieldLength > 0) {
				byte[] fieldValue = Arrays.copyOfRange(bodyData, pos, pos += fieldLength);
				optionalMetaData.put(fieldType, fieldValue);
				System.out.println("Type: 0x"+ParserHelper.getHexString(rawFieldType)
					+"Value: 0x" +ParserHelper.getHexString(fieldValue));
			}
		}
		getSimplePrimaryKey();
	}
	
	public byte[] getFieldValue(int fieldType) {
		return optionalMetaData.get(fieldType);
	}

	public byte[] getSignedness() {
		return optionalMetaData.get(SIGNEDNESS);
	}

	// CharacterSet Reference: SELECT id, collation_name FROM
	// information_schema.collations ORDER BY id;
	public byte[] getDefaultCharset() {
		return optionalMetaData.get(DEFAULT_CHARSET);
	}

	public byte[] getColumnCharset() {
		return optionalMetaData.get(COLUMN_CHARSET);
	}

	public ArrayList<String> getColumnNames() {
		ArrayList<String> columnNames = new ArrayList<String>();
		byte[] rawColumnNames = optionalMetaData.get(COLUMN_NAME);
		if (rawColumnNames != null) {
			int pos = 0;
			String columnName;
			while ((rawColumnNames.length - pos) > 1) {
				Object[] pkgColumnName = ParserHelper.getPackedString(rawColumnNames, pos, order);
				int columnNameBytes = (int) pkgColumnName[0];
				pos += columnNameBytes;
				byte[] rawColumnName = (byte[]) pkgColumnName[1];
				columnName = ParserHelper.getString(rawColumnName);
				columnNames.add(columnName);
			}
		}
		return columnNames;
	}

	public byte[] getSetStrValue() {
		return optionalMetaData.get(SET_STR_VALUE);
	}

	public byte[] getEnumStrValue() {
		return optionalMetaData.get(ENUM_STR_VALUE);
	}

	public byte[] getGeometryType() {
		return optionalMetaData.get(GEOMETRY_TYPE);
	}

	public ArrayList<Integer> getSimplePrimaryKey() {
		ArrayList<Integer> simplePKCols = new ArrayList<Integer>();
		byte[] rawsimplePKCols = optionalMetaData.get(SIMPLE_PRIMARY_KEY);
		if (rawsimplePKCols != null) {
			int pos = 0;
			while ((rawsimplePKCols.length - pos) > 0) {
				byte[] rawColNum = ParserHelper.getPackedInteger(rawsimplePKCols, pos);
				pos += rawColNum.length;
				int colNum = ParserHelper.getUnsignedInteger(rawColNum, order);
				simplePKCols.add(colNum);
			}
		}
		return simplePKCols;
	}

	public byte[] getPrimaryKeyWithPrefix() {
		return optionalMetaData.get(PRIMARY_KEY_WITH_PREFIX);
	}

	public byte[] getEnumAndSetDefaultCharset() {
		return optionalMetaData.get(ENUM_AND_SET_DEFAULT_CHARSET);
	}

	public byte[] getEnumAndSetColumnCharset() {
		return optionalMetaData.get(ENUM_AND_SET_COLUMN_CHARSET);
	}

	public static String getOptionalMetaDataFieldName(int type) {
		switch (type) {
		case SIGNEDNESS:      return "SIGNEDNESS";
		case DEFAULT_CHARSET: return "DEFAULT_CHARSET";
		case COLUMN_CHARSET:  return "COLUMN_CHARSET";
		case COLUMN_NAME:     return "COLUMN_NAME";
		case SET_STR_VALUE:   return "SET_STR_VALUE";
		case ENUM_STR_VALUE:  return "ENUM_STR_VALUE";
		case GEOMETRY_TYPE:   return "GEOMETRY_TYPE";
		case SIMPLE_PRIMARY_KEY:      return "SIMPLE_PRIMARY_KEY";
		case PRIMARY_KEY_WITH_PREFIX: return "PRIMARY_KEY_WITH_PREFIX";
		case ENUM_AND_SET_DEFAULT_CHARSET: return "ENUM_AND_SET_DEFAULT_CHARSET";
		case ENUM_AND_SET_COLUMN_CHARSET:  return "ENUM_AND_SET_COLUMN_CHARSET";
		default: return "Type{" + type + "}";
		}
	}
}
