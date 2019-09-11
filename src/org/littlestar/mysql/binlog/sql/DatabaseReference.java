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

package org.littlestar.mysql.binlog.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.littlestar.mysql.binlog.event.body.helper.ColumnDefine;
import org.littlestar.mysql.binlog.event.body.helper.TableDefine;

public class DatabaseReference {
	private static final Logger logger = Logger.getLogger(DatabaseReference.class.getName());
	private HashMap<Long, TableDefine> tableDefineCache;
	private Connection connection;
	private String refTableName;
	public DatabaseReference(final Connection connection, String refTableName) {
		tableDefineCache = new HashMap<Long, TableDefine>();
		this.connection = connection;
		this.refTableName = refTableName;
	}

	public TableDefine getTableDefine(long tableId, String schema, String tableName) {
		TableDefine dbTableDefine = tableDefineCache.get(tableId);
		//if not in cache, get from database;
		if (dbTableDefine == null) {
			dbTableDefine = new TableDefine(tableId);
			try {
				PreparedStatement stmt = connection
						.prepareStatement("select column_name, ordinal_position, character_set_name" + " from "
								+ refTableName + " where table_schema=? and table_name=?");
				stmt.setString(1, schema);
				stmt.setString(2, tableName);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					int ordinalPosition = rs.getInt("ordinal_position");
					String columnName = rs.getString("column_name");
					String charsetName = rs.getString("character_set_name");
					ColumnDefine columnDef = new ColumnDefine();
					columnDef.setColumnId(ordinalPosition - 1);
					columnDef.setColumnName(columnName);
					columnDef.setCharsetName(charsetName);
					dbTableDefine.setColumnDefine(columnDef);
				}
				rs.close();
				stmt.close();
				tableDefineCache.put(tableId, dbTableDefine);
			} catch (Throwable e) {
				logger.log(Level.WARNING, "", e);
				return null;
			}
		}
		return dbTableDefine;
	}
	
	public TableDefine getTableDefine(TableDefine binlogTableDefine) {
		long tableId = binlogTableDefine.getTableId();
		String schema = binlogTableDefine.getSchemaName();
		String tableName = binlogTableDefine.getTableName();
		return getTableDefine(tableId, schema, tableName);
	}
	
	public String getColumnReferenceName(TableDefine binlogTableDefine, int columnId) {
		String columnReferenceName = null;
		TableDefine dbTableDefine = getTableDefine(binlogTableDefine);
		if (dbTableDefine != null) {
			ColumnDefine dbColDef = dbTableDefine.getColumnDefine(columnId);
			if (dbColDef != null) {
				columnReferenceName = dbColDef.getColumnName();
			}
		}
		return columnReferenceName;
	}
	
	public String getColumnReferenceCharset(TableDefine binlogTableDefine, int columnId) {
		String columnReferenceCharset = null;
		TableDefine dbTableDefine = getTableDefine(binlogTableDefine);
		if (dbTableDefine != null) {
			ColumnDefine dbColDef = dbTableDefine.getColumnDefine(columnId);
			if (dbColDef != null) {
				columnReferenceCharset = dbColDef.getCharsetName();
			}
		}
		return columnReferenceCharset;
	}
	
}