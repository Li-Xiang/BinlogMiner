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
import java.util.ArrayList;
import java.util.Arrays;

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * The Post-Header for this event type is empty. The Body has two components:
 * Body for Previous_gtids_event
 * 
 * Name 	     Format 	        Description
 * ----------+---------------------+-----------------------------------------------------------
 * buf 	    unsigned char array   It contains the Gtids executed in the last binary log file.
 * buf_size  4 byte integer        Size of the above buffer
 * 
 * --gtid_mode=OFF : 不产生GTID，全0x00；
 * # 419e6234-4f78-11e9-ac39-3cd92b6701e3:1-322
 *  
 * The syntax for a GTID set:
 *   gtid_set: uuid_set [, uuid_set] ... | ''
 *     uuid_set: uuid:interval[:interval] ...
 *       uuid: hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh
 */

public class PreviousGtidsLogEventBody extends EventBodyDefaultImpl {
	public static final int CHAR_ARRAY_SIZE_LENGTH = 4;
	
	private ArrayList<String> previousGtidSet = new ArrayList<String>();
	private int pos = 0;
	
	public PreviousGtidsLogEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		byte[] rawGtidCount = Arrays.copyOfRange(bodyData, pos, pos += 8);
		long gtidCount = ParserHelper.getUnsignedLong(rawGtidCount, ByteOrder.BIG_ENDIAN);
		StringBuilder intervalsSet = new StringBuilder();
		for (long i = 0; i < gtidCount; i++) {
			byte[] rawUuid = getBytes(bodyData, pos, pos += 16);
			byte[] rawIntervalsCount = getBytes(bodyData, pos, pos += 8);
			long intervalsCount = ParserHelper.getUnsignedLong(rawIntervalsCount, ByteOrder.BIG_ENDIAN);
			for (int j = 0; j < intervalsCount; j++) {
				//intervals format: startInterval - endInterval
				byte[] rawStartInterval = getBytes(bodyData, pos, pos += 8);
				long startInterval = ParserHelper.getUnsignedLong(rawStartInterval, ByteOrder.BIG_ENDIAN);
				byte[] rawEndInterval = Arrays.copyOfRange(bodyData, pos, pos += 8);
				long endInterval = ParserHelper.getUnsignedLong(rawEndInterval, ByteOrder.BIG_ENDIAN) - 1L;
				intervalsSet.append(startInterval).append("-").append(endInterval);
				if (j < gtidCount - 1)
					intervalsSet.append(":");
			}
			previousGtidSet.add(ParserHelper.getFormattedUuid(rawUuid)+":"+intervalsSet.toString());			
		}
	}
	
	public ArrayList<String> getPreviousGtidSet(){
		return previousGtidSet;
	}
	
	@Override
	public String toString() {
		StringBuilder retValue = new StringBuilder("Previous-GTIDs: \n");
		if (previousGtidSet.size() == 0) {
			retValue.append("# [EMPTY]\n");
			return retValue.toString();
		}
		for (String gtid : previousGtidSet) {
			retValue.append("# ").append(gtid).append("\n");
		}
		return retValue.toString();
	}
}
