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

import java.util.HashMap;

public class TableDefine {
	private final HashMap<Integer, ColumnDefine> columnMap;
	private final long tableId;
	private String schemaName;
	private String tableName;
	public TableDefine(long tableId) {
		columnMap = new HashMap<Integer, ColumnDefine>();
		this.tableId = tableId;
	}
	
	public long getTableId() {
		return tableId;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getSchemaName() {
		return this.schemaName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public void setColumnName(int index, String columnName) {
		ColumnDefine colDef = columnMap.get(index);
		if (colDef == null) {
			colDef = new ColumnDefine();
			setColumnDefine(colDef);
		}
		colDef.setColumnName(columnName);
	}
	
	public String getCoumnName(int index) {
		ColumnDefine colDef = columnMap.get(index);
		String columnName = colDef.getColumnName();
		return columnName;
	}
	
	public void setColumnDefine(ColumnDefine columnDefine) {
		columnMap.put(columnDefine.getColumnId(), columnDefine);
	}
	
	public ColumnDefine getColumnDefine(int columnId) {
		return columnMap.get(columnId);
	}
	
	public int getColumnCount() {
		return columnMap.size();
	}
}
