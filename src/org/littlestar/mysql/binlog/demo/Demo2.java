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

import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogParserBuilder;
import org.littlestar.mysql.binlog.parser.ParserHelper;
import org.littlestar.mysql.binlog.parser.impl.BinlogParser4;

public class Demo2 {
	public static void main(String[] args) throws Throwable {
		String nextName = "/data/mysql/5.7.18/binlog/blog.000015";
		BinlogParser4 parser = (BinlogParser4) BinlogParserBuilder.newBuilder(nextName)
				.withByteOrder(ByteOrder.LITTLE_ENDIAN)
				.decodeString(true).build();
		while (true) { //
			while (parser.hasEvent(true)) {
				BinlogEvent event = parser.nextEvent();
				nextName = ParserHelper.isBinlogFileEof(event);
				if (nextName != null) {
					parser.close();
					parser = (BinlogParser4) BinlogParserBuilder.newBuilder(nextName)
							.withByteOrder(ByteOrder.LITTLE_ENDIAN)
							.decodeString(true).build();
				}
				EventHeader header = event.getHeader();
				EventBody body = event.getBody();
				if (body != null) {
					System.out.println(body.toString());
					System.out.println(header.toString());
				}
			}
			Thread.sleep(2000L);
		}
	}
}
