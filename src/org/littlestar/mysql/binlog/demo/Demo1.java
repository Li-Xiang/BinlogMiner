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
import java.util.HashSet;

import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogParser;
import org.littlestar.mysql.binlog.parser.BinlogParserBuilder;

public class Demo1 {
	public static void main(String[] args) throws Throwable {
		HashSet<EventType> eventFilter = new HashSet<EventType>();
		eventFilter.add(EventType.TABLE_MAP_EVENT);
		eventFilter.add(EventType.WRITE_ROWS_EVENT);

		String file = "/data/mysql/5.7.18/binlog/blog.000020";
		BinlogParser parser = BinlogParserBuilder.newBuilder(file)
				.withByteOrder(ByteOrder.LITTLE_ENDIAN)
				.withEventFilter(eventFilter)
				.decodeString(true).withCharSet("utf8").build();
		while (parser.hasEvent()) {
			BinlogEvent event = parser.nextEvent();
			EventHeader header = event.getHeader();
			EventBody body = event.getBody();
			if (body != null) {
				System.out.println(header.toString());
				System.out.println(body.toString());
			}
			long pos = parser.getPosition();
			if (pos > 8000) {
				break;
			}
		}
		parser.close();
	}
}
