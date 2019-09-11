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

/**
 * START_EVENT_V3: A start event is the first event of a binlog for binlog-version 1 to 3.
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/start-event-v3.html
 *   https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html
 *   
 * Payload
 *   2             binlog-version
 *   string[50]    mysql-server version
 *   4             create timestamp
 *
 */
public class StartEventV3Body extends EventBodyDefaultImpl{
	public static final int BINLOG_VERSION_LENGTH = 2;
	public static final int MYSQL_SERVER_VERSION_LENGTH = 50;
	public static final int CREATE_TIMESTAMP_LENGTH = 4;
	private final BinlogFileMeta binlogFileMeta; 
	public StartEventV3Body(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		binlogFileMeta = meta;
	}
	
	public int getBinlogVersion() {
		return binlogFileMeta.getBinlogVersion();
	}
	
	public String getServerVersion() {
		return binlogFileMeta.getServerVersion();
	}
	
	public Date getCreateTimestamp() {
		return binlogFileMeta.getCreateTimestamp();
	}
	
	@Override
	public String toString() {
		return "Server Version: " + getServerVersion() 
				+ "; Binlog Version: " + getBinlogVersion() 
				+ "; Binlog Created: " + getCreateTimestamp()
				+ "\n";
	}
}
