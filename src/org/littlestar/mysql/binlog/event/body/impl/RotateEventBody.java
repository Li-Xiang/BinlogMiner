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

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * ROTATE_EVENT: The rotate event is added to the binlog as last event to tell the reader 
 * what binlog to request next.
 * 
 * 
 * Reference: 
 *   https://dev.mysql.com/doc/internals/en/rotate-event.html
 *   https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html
 *   
 * Post-header:
 *   if binlog-version > 1 {
 *     8              position
 *   }
 *      
 * Payload:
 *   string[p]      name of the next binlog
 * 
 */
public class RotateEventBody extends EventBodyDefaultImpl {
	public static final int POSITION_LENGTH = 8;
	private long position = -1;
	private String nextBinlogName;
	private int pos = 0;
	
	public RotateEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		if (meta.getBinlogVersion() > 1) {
			position = getUnsignedLong(bodyData, pos, pos += POSITION_LENGTH);
		}
		byte[] rawNexBinlogName = getBytes(bodyData, pos, getBodyDataLength());
		nextBinlogName = ParserHelper.getString(rawNexBinlogName); 
	}
	
	public long getPosition() {
		return position;
	}
	
	public String getNextBinlogName() {
		return nextBinlogName;
	}
	
	@Override
	public String toString() {
		return "position=" + getPosition() + ", next-binlog="+getNextBinlogName();
	}

}
