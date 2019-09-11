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

package org.littlestar.mysql.binlog.event.body.helper;

import org.littlestar.mysql.binlog.parser.ParserHelper;

public class ColumnDefine {
	private int columnId;
	private String columnName;
	private byte columnType;
	private byte[] columnMeta;
	private boolean isNullableColumn;
	private String charsetName ;
	
	public void setColumnId(int columnId) {
		this.columnId = columnId;
	}
	
	public int getColumnId() {
		return columnId;
	}
	
	public void setColumnName(String colunmName) {
		this.columnName = colunmName;
	}

	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnType(byte columnType) {
		this.columnType = columnType;
	}

	public byte getColumnType() {
		return columnType;
	}
	
	public void setColumnMeta(byte[] metaData) {
		this.columnMeta = metaData;
	}

	public byte[] getColumnMeta() {
		return columnMeta;
	}

	public void setIsNullable(boolean isNullColumn) {
		this.isNullableColumn = isNullColumn;
	}

	public boolean getIsNullable() {
		return isNullableColumn;
	}
	
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
	
	public String getCharsetName() {
		return charsetName;
	}
	
	public boolean isNumberType() {
		int type = ParserHelper.getUnsignedInteger(getColumnType());
		switch (type) {
		case 0x00: // DECIMAL
		case 0x01: // TINY
		case 0x02: // SHORT
		case 0x03: // LONG
		case 0x04: // FLOAT
		case 0x05: // DOUBLE
		case 0x08: // LONGLONG
		case 0x09: // INT24
		case 0xf6: // NEWDECIMAL
			return true;
		default:
			return false;
		}
	}
	
	public boolean isStringType() {
		int type = ParserHelper.getUnsignedInteger(getColumnType());
		switch (type) {
		case 0x0f: // VARCHAR
		case 0xfd: // VAR_STRING
		case 0xfe: // STRING
			return true;
		default:
			return false;
		}
	}
	
	public boolean isDatetimeType() {
		int type = ParserHelper.getUnsignedInteger(getColumnType());
		switch (type) {
		case 0x07: // TIMESTAMP
		case 0x0a: // DATE
		case 0x0b: // TIME
		case 0x0c: // DATETIME
		case 0x0d: // YEAR
		case 0x0e: // NEWDATE
		case 0x11: // TIMESTAMP2
		case 0x12: // DATETIME2
		case 0x13: // TIME2
			return true;
		default:
			return false;
		}
	}
	
	//https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnType
	public static String getColumnTypeString(byte b) {
		int type = ParserHelper.getUnsignedInteger(b);
		switch (type) {
		case 0x00: return "DECIMAL";
		case 0x01: return "TINY";
		case 0x02: return "SHORT";
		case 0x03: return "LONG";
		case 0x04: return "FLOAT";
		case 0x05: return "DOUBLE";
		case 0x06: return "NULL";
		case 0x07: return "TIMESTAMP";
		case 0x08: return "LONGLONG";
		case 0x09: return "INT24";
		case 0x0a: return "DATE";
		case 0x0b: return "TIME";
		case 0x0c: return "DATETIME";
		case 0x0d: return "YEAR";
		case 0x0e: return "NEWDATE";
		case 0x0f: return "VARCHAR";
		case 0x10: return "BIT";
		case 0x11: return "TIMESTAMP2";
		case 0x12: return "DATETIME2";
		case 0x13: return "TIME2";
		case 0xf5: return "JSON";
		case 0xf6: return "NEWDECIMAL";
		case 0xf7: return "ENUM";
		case 0xf8: return "SET";
		case 0xf9: return "TINY_BLOB";
		case 0xfa: return "MEDIUM_BLOB";
		case 0xfb: return "LONG_BLOB";
		case 0xfc: return "BLOB";
		case 0xfd: return "VAR_STRING";
		case 0xfe: return "STRING";
		case 0xff: return "GEOMETRY";
		default:
			return "0x" + ParserHelper.getHexString(b) + "";
		}
	}
}
