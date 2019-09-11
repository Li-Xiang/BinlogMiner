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

import java.util.ArrayList;
import java.util.Arrays;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.helper.ColumnDefine;
import org.littlestar.mysql.binlog.event.body.helper.ColumnImage;
import org.littlestar.mysql.binlog.event.body.helper.RowImage;
import org.littlestar.mysql.binlog.event.body.helper.TableDefine;
import org.littlestar.mysql.binlog.event.body.impl.RowsEventBody;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * RowsEvent2SQL是个工具类，提供将Rowimage转换成对应的SQL重做(redo)/撤销(undo)语句
 * 的方法。
 * 
 * 通常Binlog里是不包含表的列名的，如果仅靠binlog只能生成伪代码，列名只能通过@i 表示，
 * 其中i为对应列在表中的位置(ordinal_position -1 )，因为程序中是通过数组存储的，从0
 * 开始编号，而不是从1开始。
 * 
 * 从MySQL server 8.0.1开始引入了--binlog-row-metadata系统变量，如果设置为‘FULL’
 * 则在TABLE_MAP_EVENT中将包含对应表的列名，这样可以不依赖源库，直接解析出可以执行的
 * SQL语句，但处于性能考虑，该系统变量的默认值为'MINIMAL',不包含列信息。
 * 
 * 这里设计是通过DatabaseReference类获，连接数据库，通过查询information_schema.columns
 * 来获取列信息，需要注意的是，因为数据库是某个特定的状态（原始/最终），如果表期间发生了DDL，如
 * 表删除，重命名，添加列,... 这都将导致获取的列名不对，这里需要通过QUERY_EVENT获取DDL语句，并
 * 手动修改语句。
 * 
 * 简单来说，如果8.0.1+，且--binlog-row-metadata=FULL，那么可以不依赖数据库，直接构建出SQL
 * 语句，且不受DDL语句的影响，如果不是，则需要通过数据库来辅助构建出SQL语句，如果期间发生了DDL，
 * 那么很可能构建出来的SQL语句是不对的；如果无法连接数据库，那么生成的是伪SQL代码，不能直接执行
 * 只能手工替换成真实的SQL语句才能执行。
 * 
 */

public class RowsEvent2SQL {
	private RowsEvent2SQL() {}

	public static ArrayList<StatmentPair> getRowsEventStatements(TableMapEventBody tableMapEventBody,
			DatabaseReference dbRef, RowsEventBody rowsEventBody, String defaultCharset) {
		ArrayList<RowImage> rowImages = rowsEventBody.getRowImages();
		EventType eventType = rowsEventBody.getEventType();
		ArrayList<StatmentPair> stmtPairArray;
		if (eventType.equals(EventType.WRITE_ROWS_EVENT) | eventType.equals(EventType.WRITE_ROWS_EVENT_V1)) {
			stmtPairArray = RowsEvent2SQL.getWriteRowsEventStatements(tableMapEventBody, dbRef, rowImages,
					defaultCharset);
		} else if (eventType.equals(EventType.UPDATE_ROWS_EVENT) | eventType.equals(EventType.UPDATE_ROWS_EVENT_V1)) {
			stmtPairArray = RowsEvent2SQL.getUpdateRowsEventStatments(tableMapEventBody, dbRef, rowImages,
					defaultCharset);
		} else if (eventType.equals(EventType.DELETE_ROWS_EVENT) | eventType.equals(EventType.DELETE_ROWS_EVENT_V1)) {
			stmtPairArray = RowsEvent2SQL.getDeleteRowsEventStatments(tableMapEventBody, dbRef, rowImages,
					defaultCharset);
		} else {
			stmtPairArray = new ArrayList<StatmentPair>();
		}
		return stmtPairArray;
	}
	
	public static ArrayList<StatmentPair> getWriteRowsEventStatements(TableMapEventBody tableMapEventBody,
			DatabaseReference dbRef, ArrayList<RowImage> rowImages, String defaultCharset) {
		final ArrayList<StatmentPair> statementPairArray = new ArrayList<StatmentPair>();
		final TableDefine tableDefine = tableMapEventBody.getTableDefine();
		final String tableName = tableMapEventBody.getSchemaName() + "." + tableMapEventBody.getTableName();
		for (int i = 0; i < rowImages.size(); i++) {
			RowImage image = rowImages.get(i);
			StatmentPair stmtPair = new StatmentPair();
			String redoStmt = "insert into " + tableName + " (" + getInsertColumnList(tableDefine, dbRef) + ") values ("
					+ getInsertValueList(tableDefine, dbRef, image, defaultCharset) + ")";
			String undoStmt = "delete from " + tableName + " where "
					+ getDeleteWhereCondition(tableDefine, dbRef, image, defaultCharset);
			stmtPair.setRedoStatement(redoStmt);
			stmtPair.setUndoStatement(undoStmt);
			statementPairArray.add(stmtPair);
		}
		return statementPairArray;
	}
	
	public static ArrayList<StatmentPair> getUpdateRowsEventStatments(TableMapEventBody tableMapEventBody,
			DatabaseReference dbRef, ArrayList<RowImage> rowImages, String defaultCharset) {
		ArrayList<StatmentPair> statementPairArray = new ArrayList<StatmentPair>();
		TableDefine tableDefine = tableMapEventBody.getTableDefine();
		RowImage beforeImage = null, afterImage = null;
		for (int i = 0; i < rowImages.size(); i++) {
			if ((i % 2) == 0) { // before-image
				beforeImage = rowImages.get(i);
			} else { // after-image
				afterImage = rowImages.get(i);
				//判断那些列发生了数值变化。
				ArrayList<Integer> changed = getChangedColumns(beforeImage, afterImage);
				StatmentPair stmtPair = new StatmentPair();
				//update为原值，没有任何改变；
				if (changed.size() == 0) {
					stmtPair.setUndoStatement("/* not-changed */");
					stmtPair.setRedoStatement("/* not-changed */");
				} else {
					String undoStmt = "update " + tableMapEventBody.getSchemaName() + "." + tableMapEventBody.getTableName() + " set ";
					String redoStmt = undoStmt;
					undoStmt += getUpdateAssignmentList(tableDefine, dbRef, beforeImage, changed, defaultCharset);
					redoStmt += getUpdateAssignmentList(tableDefine, dbRef, afterImage, changed, defaultCharset);
					undoStmt += " where ";
					redoStmt += " where ";
					undoStmt += getUpdateWhereCondition(tableDefine, dbRef, afterImage, defaultCharset);
					redoStmt += getUpdateWhereCondition(tableDefine, dbRef, beforeImage, defaultCharset);
					stmtPair.setUndoStatement(undoStmt);
					stmtPair.setRedoStatement(redoStmt);
				}
				statementPairArray.add(stmtPair);
			}
		}
		return statementPairArray;
	}
	
	public static ArrayList<StatmentPair> getDeleteRowsEventStatments(TableMapEventBody tableMapEventBody,
			DatabaseReference dbRef, ArrayList<RowImage> rowImages, String defaultCharset) {
		final ArrayList<StatmentPair> statementPairArray = new ArrayList<StatmentPair>();
		final TableDefine tableDefine = tableMapEventBody.getTableDefine();
		final String tableName = tableMapEventBody.getSchemaName() + "." + tableMapEventBody.getTableName();
		for (int i = 0; i < rowImages.size(); i++) {
			RowImage image = rowImages.get(i);
			StatmentPair stmtPair = new StatmentPair();
			String redoStmt = "delete from " + tableName + " where " + getDeleteWhereCondition(tableDefine, dbRef, image, defaultCharset);
			String undoStmt = "insert into " + tableName + " (" + getInsertColumnList(tableDefine, dbRef) + ") values ("
					+ getInsertValueList(tableDefine, dbRef, image, defaultCharset) + ")";
			stmtPair.setRedoStatement(redoStmt);
			stmtPair.setUndoStatement(undoStmt);	
			statementPairArray.add(stmtPair);
		}
		return statementPairArray;
	}
	
	private static String getUpdateAssignmentList(TableDefine tableDefine, DatabaseReference dbRef,
			RowImage rowImage, ArrayList<Integer> changed, String defaultCharset) {
		final StringBuilder assignmentList = new StringBuilder();
		final String spliter = ", ";
		for (int col : changed) {
			String columnName = tableDefine.getCoumnName(col);
			if (columnName == null) {
				columnName = "@" + col;
				if (dbRef != null) {
					String columnReferenceName = dbRef.getColumnReferenceName(tableDefine, col);
					if (columnReferenceName != null)
						columnName = columnReferenceName;
				}
			}
			
			ColumnDefine columnDefine = tableDefine.getColumnDefine(col);
			ColumnImage columnImage = rowImage.getColumnImage(col);
			String columnCharset = defaultCharset;
			if (dbRef != null) {
				String columnReferenceCharset = dbRef.getColumnReferenceCharset(tableDefine, col);
				if (columnReferenceCharset != null)
					columnCharset = columnReferenceCharset;
			}
			String columnValue = getSqlValue(columnDefine, columnImage, columnCharset);
			
			assignmentList.append(columnName).append("=").append(columnValue).append(spliter);
		}
		if (assignmentList.length() > spliter.length())
			assignmentList.delete(assignmentList.length() - spliter.length(), assignmentList.length());
		return assignmentList.toString();
	}
	
	private static String getUpdateWhereCondition(TableDefine tableDefine, DatabaseReference dbRef, RowImage image, String defaultCharset) {
		return getWhereCondition(tableDefine, dbRef, image, defaultCharset);
	}
	
	private static String getDeleteWhereCondition(TableDefine tableDefine, DatabaseReference dbRef, RowImage image, String defaultCharset) {
		return getWhereCondition(tableDefine, dbRef, image, defaultCharset);
	}
	
	private static String getWhereCondition(TableDefine blongTableDefine, DatabaseReference dbRef, RowImage image, String defaultCharset) {
		StringBuilder whereClause = new StringBuilder();
		String spliter = " and ";
		for (int i = 0; i < blongTableDefine.getColumnCount(); i++) {
			String columnName = blongTableDefine.getCoumnName(i);
			ColumnDefine columnDefine = blongTableDefine.getColumnDefine(i);
			if (columnName == null) {
				columnName = "@" + i;
				if (dbRef != null) {
					String columnReferenceName = dbRef.getColumnReferenceName(blongTableDefine, i);
					if (columnReferenceName != null)
						columnName = columnReferenceName;
				}
			}
			String columnCharset = defaultCharset;
			if (dbRef != null) {
				String columnReferenceCharset = dbRef.getColumnReferenceCharset(blongTableDefine, i);
				if (columnReferenceCharset != null)
					columnCharset = columnReferenceCharset;
			}
			
			whereClause.append(columnName);
			ColumnImage columnImage = image.getColumnImage(i);
			whereClause.append("=").append(getSqlValue(columnDefine, columnImage, columnCharset));
			whereClause.append(spliter);
		}
		whereClause.delete(whereClause.length() - spliter.length(), whereClause.length());
		return whereClause.toString();
	}
	
	private static String getInsertColumnList(TableDefine blogTableDefine, DatabaseReference dbRef) {
		StringBuilder columnList = new StringBuilder();
		String spliter = ", ";
		for (int i = 0; i < blogTableDefine.getColumnCount(); i++) {
			String columnName = blogTableDefine.getCoumnName(i);
			if (columnName == null) {
				// 如果TableMapEvent不包含column-name, 尝试通过数据库的information_schema.columns中获取；
				columnName = "@" + i;
				if (dbRef != null) {
					String columnReferenceName = dbRef.getColumnReferenceName(blogTableDefine, i);
					if (columnReferenceName != null)
						columnName = columnReferenceName;
				}
			}
			columnList.append(columnName).append(spliter);
		}
		columnList.delete(columnList.length() - spliter.length(), columnList.length());
		return columnList.toString();
	}
	
	private static String getInsertValueList(TableDefine tableDefine, DatabaseReference dbRef,
			RowImage image, String defaultCharset) {
		StringBuilder valueList = new StringBuilder();
		String spliter = ", ";
		for (int i = 0; i < tableDefine.getColumnCount(); i++) {
			ColumnDefine columnDefine = tableDefine.getColumnDefine(i);
			ColumnImage columnImage = image.getColumnImage(i);
			
			String columnCharset = defaultCharset;
			if (dbRef != null) {
				String columnReferenceCharset = dbRef.getColumnReferenceCharset(tableDefine, i);
				if (columnReferenceCharset != null)
					columnCharset = columnReferenceCharset;
			}
			String columnValue = getSqlValue(columnDefine, columnImage, columnCharset);
			valueList.append(columnValue).append(spliter);
		}
		valueList.delete(valueList.length() - spliter.length(), valueList.length());
		return valueList.toString();
	}
	
	private static String getSqlValue(ColumnDefine columnDefine, ColumnImage columnImage, String defaultCharset) {
		Object rawValue = columnImage.getColumnValue();
		String value;
		if (rawValue == null) {
			value = "null";
		} else if (columnDefine.isNumberType()) {
			value = rawValue.toString();
		} else {
			if (rawValue instanceof byte[]) {
				String charset = columnDefine.getCharsetName();
				if(charset == null)
					charset = defaultCharset;
				value = "'" + ParserHelper.getString((byte[]) rawValue, charset) + "'";
			} else {
				value = "'" + rawValue.toString() + "'";
			}
		}
		return value;
	}
	
	private static ArrayList<Integer> getChangedColumns(RowImage beforeImage, RowImage afterImage) {
		final ArrayList<Integer> changedColumns = new ArrayList<Integer>();
		for (int i = 0; i < afterImage.getColumnCount(); i++) {
			Object colAfterValue = afterImage.getColumnImage(i).getColumnValue();
			Object colBeforeValue = beforeImage.getColumnImage(i).getColumnValue();
			if (colAfterValue != null) {
				if (!colAfterValue.equals(colBeforeValue)) {
					//!! byte[] could not compare using equals.
					if ((colAfterValue instanceof byte[]) & (colBeforeValue instanceof byte[])) {
						if (!Arrays.equals((byte[]) colAfterValue, (byte[]) colBeforeValue)) {
							changedColumns.add(i);
						}
					} else {
						changedColumns.add(i);
					}

				}
			} else {
				if (colBeforeValue != null) {
					changedColumns.add(i);
				}
			}
		}
		return changedColumns;
	}
}
