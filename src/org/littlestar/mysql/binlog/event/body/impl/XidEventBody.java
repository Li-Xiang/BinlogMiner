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

import java.nio.ByteOrder;

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * XID_EVENT: Transaction ID for 2PC, written whenever a COMMIT is expected.
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/xid-event.html
 *   
 * Payload:
 *   8              xid
*/

public class XidEventBody extends EventBodyDefaultImpl {
	public final static int XID_LENGTH = 8;

	private long xid;
	private int pos = 0;

	public XidEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		byte[] rawXid = getBytes( bodyData, pos, pos += XID_LENGTH);
		
		xid = ParserHelper.getUnsignedLong(rawXid, ByteOrder.BIG_ENDIAN);
	}

	public long getXid() {
		return xid;
	}

	@Override
	public String toString() {
		return "Xid = " + xid + "\n";
	}
}
