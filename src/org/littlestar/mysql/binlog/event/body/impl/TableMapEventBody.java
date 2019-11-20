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

import java.util.ArrayList;
import java.util.BitSet;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.helper.ColumnDefine;
import org.littlestar.mysql.binlog.event.body.helper.TableDefine;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * TABLE_MAP_EVENT: The TABLE_MAP_EVENT defines the structure if the tables that are about to be changed. 
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/table-map-event.html
 *   https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html
 * 
 * post-header:
 *   if post_header_len == 6 {
 *     4              table id
 *   } else {
 *     6              table id
 *   }
 *   2              flags
 * payload:
 *   1              schema name length
 *   string         schema name
 *   1              [00]
 *   1              table name length
 *   string         table name
 *   1              [00]
 *   lenenc-int     column-count
 *   string.var_len [length=$column-count] column-def
 *   lenenc-str     column-meta-def
 *   n              NULL-bitmask, length: (column-count + 8) / 7
 *
 */


public class TableMapEventBody extends EventBodyDefaultImpl {
	public static final int FLAGS_LENGTH = 2;
	public static final int SCHEMA_NAME_LENGTH_LENGTH = 1;
	public static final int TABLE_NAME_LENGTH_LENGTH = 1;
	
	private final TableDefine tableDefine;
	private TableMapEventOptionalMetaData optionalMetaData;
	private int pos = 0;
	
	/**
	 * @param bodyData
	 * @param eventHeader
	 * @param parser
	 */
	public TableMapEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		// table-id
		int postHeaderLength = meta.getPostHeaderLength(EventType.TABLE_MAP_EVENT);
		long tableId;
		if(postHeaderLength == 6) {
			tableId = getUnsignedLong(bodyData, pos, pos += 4);
		} else {
			tableId = getUnsignedLong(bodyData, pos, pos += 6);
		}
		tableDefine = new TableDefine(tableId);
		
		//2 bytes. Reserved for future use.  skip;
		pos += 2;
		
		// schema name
		int schemaNameLength = getUnsignedInteger(bodyData, pos, pos += SCHEMA_NAME_LENGTH_LENGTH);
		String schemaName = getString(bodyData, pos, pos += schemaNameLength).trim();
		pos ++; // skip database name null-terminated
		tableDefine.setSchemaName(schemaName);
		
		// table name
		int tableNameLength = getUnsignedInteger(bodyData, pos, pos += TABLE_NAME_LENGTH_LENGTH);
		String tableName = getString(bodyData, pos, pos += tableNameLength).trim();
		pos ++; // skip table name null-terminated[00].   
		tableDefine.setTableName(tableName);
		
		//column-count
		byte[] rawColumnCount = getPackedInteger(bodyData, pos);
		pos += rawColumnCount.length;
		int columnCount = getUnsignedInteger(rawColumnCount);
		
		//column_type_def
		byte[] rawColumnType = getBytes(bodyData, pos, pos += columnCount);
		for (int i = 0; i < columnCount; i++) {
			ColumnDefine columnDef = tableDefine.getColumnDefine(i);
			if (columnDef == null) {
				columnDef = new ColumnDefine();
				columnDef.setColumnId(i);
				tableDefine.setColumnDefine(columnDef);
			}
			columnDef.setColumnType(rawColumnType[i]);
		}
		
		// The length of the metadata block.
		byte[] rawColumnMetaLength = getPackedInteger(bodyData, pos);
		pos += rawColumnMetaLength.length;
		long columnMetaLength = getUnsignedLong(rawColumnMetaLength);
		
		//column_meta_def:
		//https://dev.mysql.com/doc/dev/mysql-server/8.0.11/classbinary__log_1_1Table__map__event.html#Table_table_map_event_column_types
		//说明： 只有一部分的字段类型有meta-def，不同的字段类型的meta-def的长度不同。
		int columnMetaPos = pos; // columnMetaPos use to parsing meta data;
		pos += columnMetaLength; // body parsing position skip to end of meta data block;
		for (int i = 0; i < columnCount; i++) {
			ColumnDefine columnDef = tableDefine.getColumnDefine(i);
			int columnType = ParserHelper.getUnsignedInteger(columnDef.getColumnType());
			byte[] columnMeta;
			switch (columnType) {
			/* meta data = 1 byte(以下类型有1个字节的meta data)。
			    MYSQL_TYPE_FLOAT	    0x04 
				MYSQL_TYPE_DOUBLE	    0x05 
				MYSQL_TYPE_BLOB	        0xfc 
				MYSQL_TYPE_GEOMETRY	    0xff 
				IMESTAMP2               0x11 
			 	DATETIME2               0x12 
				TIME2                   0x13
			*/
			case 0x04: case 0x05: case 0xfc: case 0xff: case 0x11: 
			case 0x12: case 0x13: 
				columnMeta = getBytes(bodyData, columnMetaPos, columnMetaPos += 1);
				columnDef.setColumnMeta(columnMeta);
				break;
			/* meta data = 2 bytes(以下类型有2个字节的meta data。)
				MYSQL_TYPE_VARCHAR	    0x0f 
				MYSQL_TYPE_BIT	        0x10 
				MYSQL_TYPE_NEWDECIMAL	0xf6 
				MYSQL_TYPE_VAR_STRING	0xfd 
				MYSQL_TYPE_STRING	    0xfe 
			*/
			case 0x0f: case 0x10: case 0xf6: case 0xfd: case 0xfe: 
				columnMeta = getBytes(bodyData, columnMetaPos, columnMetaPos += 2);
				columnDef.setColumnMeta(columnMeta);
				break;
			}
		}
		// NULL-bitmask
		int nullableBitMapLength = ((int) columnCount + 8) / 7;
		BitSet nullableBitSet = getBitSet(bodyData, pos, pos += nullableBitMapLength);
		for (int i = 0; i < tableDefine.getColumnCount(); i++) {
			ColumnDefine columnDef = tableDefine.getColumnDefine(i);
			columnDef.setIsNullable(nullableBitSet.get(i));
		}
		// Option Mea Data
		// https://dev.mysql.com/doc/dev/mysql-server/8.0.11/classbinary__log_1_1Table__map__event.html#Table_table_map_event_optional_metadata
		optionalMetaData = new TableMapEventOptionalMetaData(bodyData, pos, getBodyDataLength(), meta.getByteOrder());
		
		/* 
		 * https://github.com/alibaba/canal/wiki/BinlogChange%28MySQL8%29
		 * binlog_row_metadata=FULL
		 * MySQL8.0.1版本之后新增在binlog里记录更多的column metadata信息，比如列名、主键、编码、SET/ENUM/GEO类型等信息，
		 * 默认是MINIMAL信息 (只记录基本的type/meta/unsigned等信息)
		 */
		//如果'Option Mea Data'包含列名，更新到TableDefine中。
		ArrayList<String> colunmNames = optionalMetaData.getColumnNames();
		if (colunmNames.size() > 0) {
			for (int i = 0; i < colunmNames.size(); i++) {
				tableDefine.setColumnName(i, colunmNames.get(i));
			}
		}
	}
	
	public long getTableId() {
		return tableDefine.getTableId();
	}

	public String getSchemaName() {
		return tableDefine.getSchemaName();
	}
	
	public String getTableName() {
		return tableDefine.getTableName();
	}
	
	public TableDefine getTableDefine(){
		return tableDefine;
	}
	
	public TableMapEventOptionalMetaData getTableMapEventOptionalMetaData() {
		return optionalMetaData;
	}
	
	@Override
	public String toString() {
		return "table-id = " + getTableId() +", " + getSchemaName()+"."+getTableName() +" {\n"
				+ getColumnsString()
				+"}\n";
	}
	
	protected String getOptionalMetaString() {
		ArrayList<Integer> simplePKCols = optionalMetaData.getSimplePrimaryKey();
		String simplePK = "";
		for (int col : simplePKCols) {
			simplePK = simplePK + col + ", ";
		}
		
		String defaultCharset = "";
		byte[] rawDefaultCharset = optionalMetaData.getDefaultCharset();
		if (rawDefaultCharset != null) {
			defaultCharset += Integer.toString(getUnsignedInteger(rawDefaultCharset));
		}
		return " SIGNEDNESS: "+ ParserHelper.getHexString(optionalMetaData.getSignedness())
			+ "; DEFAULT_CHARSET: " + defaultCharset
			+ "; COLUMN_CHARSET: " + ParserHelper.getHexString(optionalMetaData.getColumnCharset())
			//+ "; SET_STR_VALUE: " + ParserHelper.getHexString(optionalMetaData.getSetStrValue())
			//+ "; ENUM_STR_VALUE: " + ParserHelper.getHexString(optionalMetaData.getEnumStrValue())
			//+ "; GEOMETRY_TYPE: " + ParserHelper.getHexString(optionalMetaData.getGeometryType())
			+ "; SIMPLE_PRIMARY_KEY: " + simplePK
			+ "; PRIMARY_KEY_WITH_PREFIX: " + ParserHelper.getHexString(optionalMetaData.getPrimaryKeyWithPrefix())
			//+ "; ENUM_AND_SET_DEFAULT_CHARSET: " + ParserHelper.getHexString(optionalMetaData.getEnumAndSetDefaultCharset())
			//+ "; ENUM_AND_SET_COLUMN_CHARSET: " + ParserHelper.getHexString(optionalMetaData.getEnumAndSetColumnCharset())
			+"";	
	}
	
	private String getColumnsString() {
		StringBuilder build = new StringBuilder();
		for (int i = 0; i < tableDefine.getColumnCount(); i++) {
			ColumnDefine def = tableDefine.getColumnDefine(i);
			if (def != null) {
				String meta = "";
				if (def.getColumnMeta() != null) {
					meta = ParserHelper.getHexString(def.getColumnMeta());
				}
				String columnName = def.getColumnName();
				if (columnName == null) {
					columnName = "";
				}
				build.append("  @").append(i).append(" "+columnName+" = ").append(ColumnDefine.getColumnTypeString(def.getColumnType()))
						.append(", meta[" + meta + "],").append(" nullable=").append(def.getIsNullable()).append("\n");
			}
		}
		return build.toString();
	}
}
