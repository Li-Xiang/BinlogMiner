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
 * BEGIN_LOAD_QUERY_EVENT: truncate a file and set block-data.
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/begin-load-query-event.html
 *   https://dev.mysql.com/doc/dev/mysql-server/8.0.11/classbinary__log_1_1Begin__load__query__event.html
 *   
 * Payload
 *   4              file_id
 *   string.EOF     block-data
 */
public class BeginLoadQueryEvent extends EventBodyDefaultImpl {
	public static final int FILE_ID_LENGTH = 4;
	private long fileId;
	private byte[] blockData;
	private int pos = 0;

	public BeginLoadQueryEvent(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		fileId = getUnsignedLong(bodyData, pos, pos += FILE_ID_LENGTH);
		blockData = getBytes(bodyData, pos, eventHeader.getEventBodyLength());
	}
	
	public long getFileId() {
		return fileId;
	}
	
	public byte[] getBlockData() {
		return blockData;
	}
	
	public String getBlockDataString() {
		return ParserHelper.getString(blockData);
	}

	@Override
	public String toString() {
		return "file_id: " + fileId +" block_len: "+ getBlockData().length;
	}
}
