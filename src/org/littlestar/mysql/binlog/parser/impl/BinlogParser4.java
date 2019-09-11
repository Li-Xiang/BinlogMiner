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

package org.littlestar.mysql.binlog.parser.impl;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashSet;

import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.EventBodyFactory;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMappedByteBuffer;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.BinlogParser;

public class BinlogParser4 implements BinlogParser {
	private final BinlogFileMappedByteBuffer fileMappedBuffer;
	private final BinlogFileMeta binlogFileMeta;
	private ByteOrder order ;
	private HashSet<EventType> eventFilter = null;

	private BinlogParser4(final BinlogFileMeta binlogFileMeta) throws Throwable {
		if (order != null) {
			this.order = ByteOrder.nativeOrder();
		}
		this.binlogFileMeta = binlogFileMeta;
		fileMappedBuffer = new BinlogFileMappedByteBuffer(binlogFileMeta.getFile().toString(), binlogFileMeta.getByteOrder());
		fileMappedBuffer.skip(BinlogFileMeta.MAGIC_NUMBER_LENGTH);
	}
	
	public static BinlogParser4 newParser(final BinlogFileMeta binlogFileMeta, HashSet<EventType> eventFilter)
			throws Throwable {
		BinlogParser4 parser = new BinlogParser4(binlogFileMeta);
		if (eventFilter != null)
			parser.setEventFilter(eventFilter);
		return parser;
	}
	
	public static BinlogParser newParser(final BinlogFileMeta binlogFileMeta) throws Throwable {
		return new BinlogParser4(binlogFileMeta);
	}

	private void setEventFilter(HashSet<EventType> eventFilter) {
		this.eventFilter = eventFilter;
	}

	public HashSet<EventType> getEventFilter() {
		return eventFilter;
	}

	@Override
	public void close() throws IOException {
		if (fileMappedBuffer != null) {
			fileMappedBuffer.close();
		}
	}
	
	@Override
	public BinlogEvent nextEvent() throws Throwable {
		EventHeader eventHeader = getEventHeader();
		int nextPos = (int) eventHeader.getNextPosition();
		EventBody eventBody = null;
		try {
			if (eventFilter != null) {
				if (eventFilter.contains(eventHeader.getEventType())) {
					eventBody = getEventBody(eventHeader);
				}
			} else {
				eventBody = getEventBody(eventHeader);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			fileMappedBuffer.setPosition(nextPos);
		}
		return new BinlogEvent(eventHeader, eventBody);
	}

	@Override
	public boolean hasEvent() {
		return hasEvent(false);
	}
	
	/* 
	 * To support online binlong file parsing. when withCheck = true, 
	 * will remap binlog file when binlog file size was changed.
	 * 
	 */
	public boolean hasEvent(boolean withCheck) {
		if (fileMappedBuffer.remaining(withCheck) < binlogFileMeta.getCommonHeaderLength())
			return false;
		return true;
	}
	
	@Override
	public BinlogFileMeta getBinlogFileMeta() {
		return binlogFileMeta;
	}
	
	@Override
	public long getPosition() {
		return fileMappedBuffer.getPosition();
	}
	
	/** 
	 * Binlog header:
	 * Payload 
	 *   4              timestamp
	 *   1              event type
	 *   4              server-id
	 *   4              event-size
	 *     if binlog-version > 1:
	 *   4              log pos
	 *   2              flags
	 */
	private EventHeader getEventHeader() {
		int startPos = fileMappedBuffer.getPosition();
		long eventTimetamp = fileMappedBuffer.getUnsignedLong(EventHeader.TIMESTAMP_LENGTH);
		int typeCode = fileMappedBuffer.getUnsignedInteger(EventHeader.EVENT_TYPE_LENGTH);
		EventType eventType = EventType.getEventType(typeCode);
		long serverId = fileMappedBuffer.getUnsignedLong(EventHeader.SERVER_ID_LENGTH);
		int eventSize = fileMappedBuffer.getUnsignedInteger(EventHeader.EVENT_SIZE_LENGTH);
		int commonHeaderLength = binlogFileMeta.getCommonHeaderLength();
		long eventNextPos = -1L;
		int flags = -1;
		if (commonHeaderLength == EventHeader.COMMON_HEADER_LENGTH_V4) { // v1+
			eventNextPos = fileMappedBuffer.getUnsignedLong(EventHeader.NEXT_POS_LENGTH);
			flags = fileMappedBuffer.getUnsignedInteger(EventHeader.FLAGS_LENGTH);
		} else if (commonHeaderLength == EventHeader.COMMON_HEADER_LENGTH_V1) {
			eventNextPos = startPos + eventSize;
		}
		EventHeader eventHeader = new EventHeader(commonHeaderLength);
		eventHeader.setTimestamp(eventTimetamp * 1000L);
		eventHeader.setEventType(eventType);
		eventHeader.setServerId(serverId);
		eventHeader.setEventSize(eventSize);
		eventHeader.setNextPosition(eventNextPos);
		eventHeader.setEventFlags(flags);
		return eventHeader;
	}
	
	private EventBody getEventBody(final EventHeader eventHeader) {
		try {
			byte[] rawEventBody = fileMappedBuffer.getBytes(eventHeader.getEventBodyLength());
			EventBody eventBody = EventBodyFactory.createEventBody(rawEventBody, eventHeader, binlogFileMeta);
			if (eventHeader.getEventType().equals(EventType.TABLE_MAP_EVENT)) {
				TableMapEventBody tableMapEventBody = (TableMapEventBody) eventBody;
				binlogFileMeta.putTableMapEventBody(tableMapEventBody);
			}
			return eventBody;
		} catch (Throwable e) {
			throw e;
		}
	}
}
