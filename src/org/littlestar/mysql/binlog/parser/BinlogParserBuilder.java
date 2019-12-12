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

package org.littlestar.mysql.binlog.parser;

import java.io.IOException;
import java.nio.ByteOrder;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.impl.FormatDescriptionEventBody;
import org.littlestar.mysql.binlog.event.body.impl.StartEventV3Body;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.impl.BinlogParser4;

/**
 * https://dev.mysql.com/doc/internals/en/binlog-version.html
 * Binlog version     MySQL Version
 * --------------------------------------
 * 1                  MySQL 3.23 - < 4.0.0
 * 2                  MySQL 4.0.0 - 4.0.1
 * 3                  MySQL 4.0.2 - < 5.0.0
 * 4                  MySQL 5.0.0+
 * 
 * Version 1: supported statement based replication events.
 * Version 2: can be ignored as it was only used in early alpha versions of MySQL 4.1.x 
 *            and won't be documented here.
 * Version 3: added the relay logs and changed the meaning of the log position.
 * Version 4: added the FORMAT_DESCRIPTION_EVENT and made the protocol extensible.
 * 
 * Note: 
 * 1. A start event (START_EVENT_V3) is the first event of a binlog for binlog-version 1 to 3. 
 * 2. A format description event (FORMAT_DESCRIPTION_EVENT) is the first event 
 *    of a binlog for binlog-version 4. 
 * 
 * https://dev.mysql.com/doc/internals/en/binlog-event.html#statement-based-replication-events
 */

public class BinlogParserBuilder {
	// https://dev.mysql.com/doc/internals/en/binlog-event-header.html
	// The binlog event header is either 13 (v1) or 19(v1+)
	private BinlogFileMappedByteBuffer fileMappedBuffer;
	private final String binlogFileName;
	private ByteOrder order ;
	private HashSet<EventType> eventFilter = null;
	private Boolean decodeString = true;
	private Boolean decodeEventBody = true;
	private String defaultCharsetName = null;
	
	private BinlogParserBuilder(String binlogFileName) throws Throwable {
		order = ByteOrder.nativeOrder();
		this.binlogFileName = binlogFileName;
		// Checking input binlog file.
		if (binlogFileName == null) {
			throw new IOException("File name is null.");
		}
	}
	
	private void close() throws IOException {
		if (fileMappedBuffer != null) {
			fileMappedBuffer.close();
		}
	}
	
	public static BinlogParserBuilder newBuilder(String binlogFileName) throws Throwable {
		return new BinlogParserBuilder(binlogFileName);
	}
	
	public BinlogParserBuilder withByteOrder(ByteOrder order) {
		this.order = order;
		return this;
	}
	
	public BinlogParserBuilder withEventFilter(HashSet<EventType> eventFilter) {
		this.eventFilter = eventFilter;
		return this;
	}
	
	public BinlogParserBuilder decodeString(boolean decode) {
		decodeString = decode;
		return this;
	}
	
	public BinlogParserBuilder decodeEventBody(boolean decode) {
		decodeEventBody = decode;
		return this;
	}
	
	public BinlogParserBuilder withCharSet(String defaultMysqlCharsetName) {
		defaultCharsetName = defaultMysqlCharsetName;
		return this;
	}
	
	public BinlogParser build() throws Throwable {
		fileMappedBuffer = new BinlogFileMappedByteBuffer(binlogFileName, order);
		BinlogFileMeta fileMeta = getBinlogFileMeta();
		fileMappedBuffer.close();
		if (decodeString != null) {
			fileMeta.decodeString(decodeString);
		}
		if (defaultCharsetName != null) {
			fileMeta.setDefaultCharsetName(defaultCharsetName);
		}
		return BinlogParser4.newParser(fileMeta, eventFilter, decodeEventBody);
	}
	
	private BinlogFileMeta getBinlogFileMeta() throws Throwable {
		byte[] magicNumber = fileMappedBuffer.getBytes(BinlogFileMeta.MAGIC_NUMBER_LENGTH);
		if (!Arrays.equals(magicNumber, BinlogFileMeta.MAGIC_NUMBER)) {
			close();
			throw new IOException(
					"Bad magic number: " + ParserHelper.getHexString(magicNumber) + ", it's not a binary log file.");
		}
		// Parsing the first event of binlog file.
		int eventPos = fileMappedBuffer.getPosition();
		// 4 bytes : event timestamp --> not need skip.
		fileMappedBuffer.skip(EventHeader.TIMESTAMP_LENGTH);
		// 1 byte: event type
		int typeCode = fileMappedBuffer.getUnsignedInteger(EventHeader.EVENT_TYPE_LENGTH); 
		EventType eventType = EventType.getEventType(typeCode);
		// 4 bytes : server-id --> not need skip.
		fileMappedBuffer.skip(EventHeader.SERVER_ID_LENGTH);
		// 4 bytes: event-size
		int eventSize = fileMappedBuffer.getUnsignedInteger(EventHeader.EVENT_SIZE_LENGTH);
		int eventNextPos = eventPos + eventSize;
		
		BinlogFileMeta binlogFileMeta = new BinlogFileMeta(fileMappedBuffer.getFileName(), fileMappedBuffer.getByteOrder());
		
		int commonHeaderLength = EventHeader.COMMON_HEADER_LENGTH_V1;
		int skipBytes = 6; // if binlog-version > 1: skip log pos + flags;
		// version 1 to 3;
		if (eventType.equals(EventType.START_EVENT_V3)) {
			// version 1 START_EVENT_V3 size: Header + Body: 13 + (2 + 50 + 4) = 69
			// version 2,3 START_EVENT_V3 size: Header + Body: 19 + (2 + 50 + 4) = 75
			if (eventSize > 69) {
				commonHeaderLength = EventHeader.COMMON_HEADER_LENGTH_V3;
				fileMappedBuffer.skip(skipBytes);
			}
			binlogFileMeta.setCommonHeaderLength(commonHeaderLength);
			//
			int binlogVersion = fileMappedBuffer.getUnsignedInteger(StartEventV3Body.BINLOG_VERSION_LENGTH);
			binlogFileMeta.setBinlogVersion(binlogVersion);
			
			String serverVersion = fileMappedBuffer.getString(StartEventV3Body.MYSQL_SERVER_VERSION_LENGTH).trim();
			binlogFileMeta.setServerVersion(serverVersion);
			
			long createTimestampValue = fileMappedBuffer.getUnsignedInteger(StartEventV3Body.CREATE_TIMESTAMP_LENGTH) * 1000L;
			binlogFileMeta.setCreateTimestamp(new Date(createTimestampValue));
			
		} else if (eventType.equals(EventType.FORMAT_DESCRIPTION_EVENT)) {
			// version 4;
			commonHeaderLength = EventHeader.COMMON_HEADER_LENGTH_V4;
			fileMappedBuffer.skip(skipBytes);
			
			// Parsing FORMAT_DESCRIPTION_EVENT evnet body ...
			int binlogVersion = fileMappedBuffer.getUnsignedInteger(FormatDescriptionEventBody.BINLOG_VERSION_LENGTH);
			binlogFileMeta.setBinlogVersion(binlogVersion);
			
			String serverVersion = fileMappedBuffer.getString(FormatDescriptionEventBody.MYSQL_SERVER_VERSION_LENGTH).trim();
			binlogFileMeta.setServerVersion(serverVersion);
			
			long createTimestampValue = fileMappedBuffer.getUnsignedInteger(FormatDescriptionEventBody.CREATE_TIMESTAMP_LENGTH) * 1000L;
			binlogFileMeta.setCreateTimestamp(new Date(createTimestampValue));

			// version 4 header's length, it's always be 19, skip
			fileMappedBuffer.skip(FormatDescriptionEventBody.EVENT_HEADER_LENGTH_LENGTH);
			binlogFileMeta.setCommonHeaderLength(19);

			int remainLength = eventNextPos - fileMappedBuffer.getPosition();
			// EventBody = EventData(Post-header + Playload) + CheckSum, Please Reference BinlogEvent.java
			//   - version 4 FD event's playload is 0, so EventData = Post-header;
		    //   - binlog_checksum variable was added in MySQL 5.6.2: 
			//     int FD event, 1 byte checksum algorithm + checksum value;
			// So "checksum size" = "event size" - "common header size" - "post header size" - "playload size"
			
			byte[] remainBytes = fileMappedBuffer.getBytes(remainLength);
			binlogFileMeta.setPostHeaderLengths(remainBytes);
			
			int postHeaderLength = binlogFileMeta.getPostHeaderLength(EventType.FORMAT_DESCRIPTION_EVENT);
			int checksumWithAlgLength = eventSize - 19 - postHeaderLength;
			int postHeaderLengthsLength = remainLength - checksumWithAlgLength;
			byte[] eventPostHeaderLengths = Arrays.copyOfRange(remainBytes, 0, postHeaderLengthsLength);
			if (checksumWithAlgLength >= 1) {
				byte rawChecksumAlg = Arrays.copyOfRange(remainBytes, postHeaderLengthsLength, postHeaderLengthsLength + 1)[0];
				int checksumAlg = ParserHelper.getUnsignedInteger(rawChecksumAlg);
				int checksumLength = checksumWithAlgLength - 1;
				binlogFileMeta.setChecksumLength(checksumLength);
				binlogFileMeta.setChecksumAlg(checksumAlg);
			}
			binlogFileMeta.setPostHeaderLengths(eventPostHeaderLengths);
		} else {
			close();
			throw new IOException("unknown binlog version, with the first event of '" + eventType.toString() + "'.");
		}
		return binlogFileMeta;
	}

}
