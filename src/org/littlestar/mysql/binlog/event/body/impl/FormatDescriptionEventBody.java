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

import java.util.Date;

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;

/*
 * FORMAT_DESCRIPTION_EVENT 
 * A format description event is the first event of a binlog for binlog-version 4. It describes how the other events are layed out.
 * Payload:
 *   2                binlog-version
 *   string[50]       mysql-server version
 *   4                create timestamp
 *   1                event header length
 *   string[p]        event type header lengths
 */

public class FormatDescriptionEventBody extends EventBodyDefaultImpl {
	public static final int BINLOG_VERSION_LENGTH = 2;
	public static final int MYSQL_SERVER_VERSION_LENGTH = 50;
	public static final int CREATE_TIMESTAMP_LENGTH = 4;
	public static final int EVENT_HEADER_LENGTH_LENGTH = 1;
	public static final int CHECKSUM_ALGORITHM_LENGTH = 1;
	
	private final BinlogFileMeta binlogFileMeta; 
	
	public FormatDescriptionEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		binlogFileMeta = meta;
	}

	public String getServerVersion() {
		return binlogFileMeta.getServerVersion();
	}

	public int getBinlogVersion() {
		return binlogFileMeta.getBinlogVersion();
	}

	public Date getCreateTimestamp() {
		return binlogFileMeta.getCreateTimestamp();
	}
	
	public int getEventCommonHeaderLength() {
		return binlogFileMeta.getCommonHeaderLength();
	}
	
	@Override
	public String toString() {
		return "Server Version: " + getServerVersion() 
				+ "; Binlog Version: " + getBinlogVersion() 
				+ "; Binlog Created: " + getCreateTimestamp()
				+ "; Common Header: " + getEventCommonHeaderLength()
				+ " " + getChecksumString()
				+ "\n";
	}
}
