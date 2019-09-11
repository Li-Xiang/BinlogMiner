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
package org.littlestar.mysql.binlog.demo;

import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import org.littlestar.helper.ConnectionFactory;
import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.impl.RowsEventBody;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.BinlogParser;
import org.littlestar.mysql.binlog.parser.BinlogParserBuilder;
import org.littlestar.mysql.binlog.sql.DatabaseReference;
import org.littlestar.mysql.binlog.sql.RowsEvent2SQL;
import org.littlestar.mysql.binlog.sql.StatmentPair;


/*
 * Replicate MySQL tpcc.emloyees to Oracle tpcc.employees. 
 */

public class Demo3 {
	public static void main(String[] args) throws Throwable {
		HashSet<EventType> eventFilter = new HashSet<EventType>();
		eventFilter.add(EventType.TABLE_MAP_EVENT);
		eventFilter.add(EventType.QUERY_EVENT);
		eventFilter.add(EventType.WRITE_ROWS_EVENT_V1);
		eventFilter.add(EventType.UPDATE_ROWS_EVENT_V1);
		eventFilter.add(EventType.DELETE_ROWS_EVENT_V1);
		eventFilter.add(EventType.WRITE_ROWS_EVENT);
		eventFilter.add(EventType.UPDATE_ROWS_EVENT);
		eventFilter.add(EventType.DELETE_ROWS_EVENT);
		BinlogParser parser = BinlogParserBuilder.newBuilder("/data/mysql/5.7.18/binlog/blog.000016")
				.withByteOrder(ByteOrder.LITTLE_ENDIAN)
				.withEventFilter(eventFilter)
				.decodeString(false)
				.withCharSet("utf8")
				.build();
		BinlogFileMeta binlogMeta = parser.getBinlogFileMeta();
		DatabaseReference dbRef = null;
		Connection refConnect = ConnectionFactory.newFactory()
				.setDriver(ConnectionFactory.MYSQL_DRIVER)
				.setUrl("jdbc:mysql://127.0.0.1/mysql")
				.setUser("root")
				.setPassword("Passw0rd")
				.withMySqlProperties()
				.build();
		dbRef = new DatabaseReference(refConnect, "information_schema.columns");
		
		Connection repConnect = ConnectionFactory.newFactory()
				.setDriver(ConnectionFactory.ORACLE_DRIVER)
				.setUrl("jdbc:oracle:thin:@//192.168.6.11:1521/db112")
				.setUser("tpcc")
				.setPassword("Passw0rd")
				.build();
		
		while (parser.hasEvent()) {
			BinlogEvent event = parser.nextEvent();
			EventBody body = event.getBody();
			if (body instanceof RowsEventBody) {
				RowsEventBody rowsEventBody = (RowsEventBody) body;
				long tableId = rowsEventBody.getTableId();
				TableMapEventBody mapedTable = binlogMeta.getTableMapEventBody(tableId);
				ArrayList<StatmentPair> pairArray = RowsEvent2SQL.getRowsEventStatements(mapedTable, dbRef,
						rowsEventBody, binlogMeta.getDefaultCharsetName());
				if (mapedTable.getTableName().equalsIgnoreCase("employees")) {
					for (StatmentPair stmt : pairArray) {
						String redoSQL = stmt.getRedoStatement();
						// not support date type, so you need to change to string type:
						// ORACLE> alter table tpcc.employees modify ( hire_date varchar2(30));
						// In real-world, you need to coding to change to target RDBMS's syntax.
						Statement repStmt = repConnect.createStatement();
						repStmt.execute(redoSQL);
						repStmt.close();
					}
				}
			}
		}
		parser.close();
		refConnect.close();
		repConnect.close();
	}
}
