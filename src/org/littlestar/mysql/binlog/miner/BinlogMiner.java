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

package org.littlestar.mysql.binlog.miner;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.impl.QueryEventBody;
import org.littlestar.mysql.binlog.event.body.impl.RowsEventBody;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.BinlogParser;
import org.littlestar.mysql.binlog.parser.BinlogParserBuilder;
import org.littlestar.mysql.binlog.parser.ParserHelper;
import org.littlestar.mysql.binlog.sql.DatabaseReference;
import org.littlestar.mysql.binlog.sql.RowsEvent2SQL;
import org.littlestar.mysql.binlog.sql.StatmentPair;

public class BinlogMiner extends Thread {
	private static final int batchSize = 600;
	private static final Logger logger = Logger.getLogger(BinlogMiner.class.getName());
	
	private final BinlogParser parser;
	private final BinlogFileMeta binlogMeta;
	private final String blogFile ;
	
	private Connection outputConnection = null;
	private String outputTable = null;
	
	private Connection referenceConnection = null;
	private String referenceTableName = "information_schema.columns";
	private DatabaseReference dbRef = null;
	
	private PrintStream outputStream = null;
	
	private BinlogMiner(String blogFile, ByteOrder byteOrder, String charset) throws Throwable {
		this.blogFile = blogFile;
		HashSet<EventType> eventFilter = new HashSet<EventType>();
		eventFilter.add(EventType.TABLE_MAP_EVENT);
		eventFilter.add(EventType.QUERY_EVENT);
		eventFilter.add(EventType.WRITE_ROWS_EVENT_V1);
		eventFilter.add(EventType.UPDATE_ROWS_EVENT_V1);
		eventFilter.add(EventType.DELETE_ROWS_EVENT_V1);
		eventFilter.add(EventType.WRITE_ROWS_EVENT);
		eventFilter.add(EventType.UPDATE_ROWS_EVENT);
		eventFilter.add(EventType.DELETE_ROWS_EVENT);
		parser = BinlogParserBuilder.newBuilder(blogFile)
				.withByteOrder(byteOrder)
				.withEventFilter(eventFilter)
				.decodeString(false)
				.withCharSet(charset)
				.build();
		binlogMeta = parser.getBinlogFileMeta();
	}
	
	public static BinlogMiner newMiner(String blogFile, ByteOrder byteOrder) throws Throwable {
		return new BinlogMiner(blogFile, byteOrder, null);
	}
	
	public static BinlogMiner newMiner(String blogFile, ByteOrder byteOrder, String charset) throws Throwable {
		return new BinlogMiner(blogFile, byteOrder, charset);
	}
	
	public BinlogMiner withOutputConnection(final Connection connection) {
		this.outputConnection = connection;
		return this;
	}
	
	public BinlogMiner withOutputTable(String outputTable) {
		if (outputTable != null & outputTable.trim().length() > 0) {
			this.outputTable = outputTable;
		}
		return this;
	}
	
	public BinlogMiner withReferenceConnection(Connection connection ) {
		referenceConnection = connection;
		return this;
	}
	
	public BinlogMiner withReferenceTable(String refTableName) {
		if (refTableName != null & refTableName.trim().length() > 0) {
			this.referenceTableName = refTableName;
		}
		return this;
	}
	
	public BinlogMiner withOutputStream(PrintStream outputStream) {
		this.outputStream = outputStream;
		return this;
	}
	
	public String getBinlogFileName() {
		return blogFile;
	}
	
	public long getBinlogFileSize() throws IOException {
		return binlogMeta.getFileSize();
	}
	
	public long getProcessedBytes() {
		return parser.getPosition();
	}
	
	@Override
	public void run() {
		if (referenceConnection != null) {
			dbRef = new DatabaseReference(referenceConnection, referenceTableName);
		}
		PreparedStatement stmt = null;
		int num = 0;
		while (parser.hasEvent()) {
			try {
				BinlogEvent event = parser.nextEvent();
				EventHeader header = event.getHeader();
				EventBody body = event.getBody();
				EventType eventType = header.getEventType();
				Timestamp eventTimestamp = new Timestamp(header.getTimestamp().getTime());
				String schemaName = "";
				String tableName = "";
				long startPos = header.getStartPosition();
				long endPos = header.getNextPosition();
				ArrayList<StatmentPair> stmtPairArray = new ArrayList<StatmentPair>();
				if (body instanceof RowsEventBody) {
					RowsEventBody rowsEventBody = (RowsEventBody) body;
					long tableId = rowsEventBody.getTableId();
					TableMapEventBody tableMapEventBody = binlogMeta.getTableMapEventBody(tableId);
					schemaName = tableMapEventBody.getSchemaName();
					tableName = tableMapEventBody.getTableName();
					ArrayList<StatmentPair> pairArray = RowsEvent2SQL.getRowsEventStatements(tableMapEventBody, dbRef,
							rowsEventBody, binlogMeta.getDefaultCharsetName());
					stmtPairArray.addAll(pairArray);
				} else if (body instanceof QueryEventBody) {
					QueryEventBody queryEventBody = (QueryEventBody) body;
					schemaName = queryEventBody.getSchema();
					tableName = "";
					String redoStmt = queryEventBody.getQuery();
					String undoStmt = "";
					StatmentPair pair = new StatmentPair();
					pair.setRedoStatement(redoStmt);
					pair.setUndoStatement(undoStmt);
					stmtPairArray.add(pair);
				}
				
				if (outputTable != null & outputConnection != null) {
					if(stmt == null) {
						final String outputStmt = "insert into "+outputTable+" (event_timestamp,"
								+ "event_type, table_schema, table_name, start_pos, "
								+ "end_pos, undo_stmt, redo_stmt) values(?, ?, ?, ?, ?, ?, ?, ?)";
						stmt = outputConnection.prepareStatement(outputStmt);
					}
					for (StatmentPair stmtPair : stmtPairArray) {
						stmt.setTimestamp(1, eventTimestamp);
						stmt.setString(2, eventType.toString());
						stmt.setString(3, schemaName);
						stmt.setString(4, tableName);
						stmt.setLong(5, startPos);
						stmt.setLong(6, endPos);
						stmt.setString(7, stmtPair.getUndoStatement());
						stmt.setString(8, stmtPair.getRedoStatement());
						stmt.addBatch();
						num++;
						if (num % batchSize == 0) {
							stmt.executeBatch();
						}
					}
				} else {
					if (outputStream == null) {
						outputStream = System.out;
					}
					if ((body instanceof RowsEventBody) | (body instanceof QueryEventBody)) {
						outputStream.println(ParserHelper.getString(eventTimestamp) + " " + eventType.toString() + " "
								+ schemaName + "." + tableName + " start-pos: " + startPos + " end-pos: " + endPos);
						for (StatmentPair stmtPair : stmtPairArray) {
							outputStream.println("REDO# " + stmtPair.getRedoStatement());
							outputStream.println("UNDO# " + stmtPair.getUndoStatement());
						}
					}
				}
			} catch (Throwable e) {
				logger.log(Level.WARNING, "", e);
			}
		}
		try {
			if (stmt != null) {
				stmt.executeBatch();
				stmt.close();
			}
		} catch (Throwable e) {
		}
	}
}
