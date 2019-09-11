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
 * A STOP_EVENT has no payload or post-header.
 * 
 * https://dev.mysql.com/doc/internals/en/stop-event.html
 */
public class StopEventBody extends EventBodyDefaultImpl {

	public StopEventBody(byte[] bodyData, EventHeader eventHeader, BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
	}

	public String assumeNextFile() {
		String current = meta.getFile().toString();
		int idx = current.lastIndexOf('.');
		if(idx < 1) {
			return null;
		}
		String strSeq = current.substring(idx + 1, current.length());
		String Prefix = current.substring(0, idx + 1);
		
		long currSeq = Long.parseLong(strSeq);
		String nextFile = Prefix + getFixedLenghtString(currSeq + 1, strSeq.length());
		return nextFile;
	}

	private String getFixedLenghtString(long l, int length) {
		String strLong = Long.toString(l);
		if (strLong.length() < length) {
			strLong = ParserHelper.lpad(strLong, length, '0');
		} else {
			strLong = ParserHelper.lpad("1", length, '0');
		}
		return strLong;
	}
}
