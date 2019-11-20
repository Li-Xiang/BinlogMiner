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
import java.util.Arrays;
import java.util.Date;

import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * GTID_LOG_EVENT
 * 
 * Reference:
 *   https://mysqlhighavailability.com/taking-advantage-of-new-transaction-length-metadata/
 * 
 * The layout of the buffer is as follows:
 * +------------+
 * |     1 byte | Flags
 * +------------+
 * |    16 bytes| Encoded SID, Server UUID
 * +------------+
 * |     8 bytes| Encoded GNO, Transaction ID
 * +------------+
 * |     1 byte | lt_type
 * +------------+
 * |     8 bytes| last_committed
 * +------------+
 * |     8 bytes| sequence_number
 * +------------+
 * |  7/14 bytes| timestamps*
 * +------------+
 * |1 to 9 bytes| transaction_length (see net_length_size())
 * +------------+
 * |   4/8 bytes| original/immediate_server_version (see timestamps*)
 * +------------+
 * 
 * The 'Flags' field contains gtid flags.
 * 
 * lt_type (for logical timestamp typecode) is always equal to the
 * constant LOGICAL_TIMESTAMP_TYPECODE.
 * 
 * 5.6 did not have TS_TYPE and the following fields. 5.7.4 and
 * earlier had a different value for TS_TYPE and a shorter length for
 * the following TS fields. Both these cases are accepted and ignored. 
 * The section titled "timestamps" contains commit timestamps on originating
 * server and commit timestamp on the immediate master.
 *  
 * This is how we write the timestamps section serialized to a memory buffer.
 * 
 * if original_commit_timestamp != immediate_commit_timestamp:
 * +-7 bytes, high bit (1<<55) set-----+-7 bytes----------+
 * | immediate_commit_timestamp        |original_timestamp|
 * +-----------------------------------+------------------+
 * 
 * else:
 * 
 * +-7 bytes, high bit (1<<55) cleared-+
 * | immediate_commit_timestamp        |
 * +-----------------------------------+

*/
public class GtidLogEventBody extends EventBodyDefaultImpl {
	public static final int ENCODED_FLAG_LENGTH = 1;
	public static final int ENCODED_SID_LENGTH = 16; 
	public static final int ENCODED_GNO_LENGTH = 8;
	public static final int LT_TYPE_LENGTH = 1;
	public static final int LAST_COMMITTED_LENGTH = 8;
	public static final int SEQUENCE_NUMBER_LENGTH = 8;
	public static final int IMMEDIATE_COMMIT_TIMESTAMP_LENGTH = 7;
	public static final int ORIGINAL_COMMIT_TIMESTAMP_LENGTH = 7;
	public static final int IMMEDIATE_SERVER_LENGTH = 4;
	public static final int ORIGINAL_SERVER_LENGTH = 4;
	
	private int flags;
	private byte[] encodedSID;
	private byte[] encodedGNO;
	private byte ltType;
	private long lastCommit;
	private long sequenceNumber;
	private byte[] originalCommittedTimestamp;
	private byte[] immediateCommittedTimestamp;
	private long transactionLength;
	
	private int pos = 0;
	
	public GtidLogEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		
		flags = getInteger(bodyData, pos, pos += ENCODED_FLAG_LENGTH);
		encodedSID = getBytes(bodyData, pos, pos += ENCODED_SID_LENGTH);
		encodedGNO = getBytes(bodyData, pos, pos += ENCODED_GNO_LENGTH);
		ltType = getBytes(bodyData, pos, pos += LT_TYPE_LENGTH)[0];
		byte[] rawLastCommit = getBytes(bodyData, pos, pos += LAST_COMMITTED_LENGTH);
		lastCommit = ParserHelper.getUnsignedLong(rawLastCommit, ByteOrder.BIG_ENDIAN);
		byte[] rawSequenceNumber = getBytes(bodyData, pos, pos += SEQUENCE_NUMBER_LENGTH); //
		sequenceNumber = ParserHelper.getUnsignedLong(rawSequenceNumber, ByteOrder.BIG_ENDIAN);
		long remainBytes = getBodyDataLength() - pos;
		if (remainBytes >= 9 && remainBytes <= 16) {
			immediateCommittedTimestamp = Arrays.copyOfRange(bodyData, pos, pos += 7);
			originalCommittedTimestamp = immediateCommittedTimestamp;
			pos ++;
			byte[] rawTransactionLength = Arrays.copyOfRange(bodyData, pos, getBodyDataLength());
			transactionLength = getUnsignedLong(rawTransactionLength);
		}

	}
	
	public long[] getCommittedTimestamp(byte[] timestamp) {
		long[] retValue = new long[2];
		byte[] padedTs = rpad(timestamp, new byte[] { 0x00 });
		long tsValue = ParserHelper.getUnsignedLong(padedTs, ByteOrder.BIG_ENDIAN);
		long fractional = tsValue % 1000000L;
		long epochSinceSecond = tsValue / 1000000L;
		retValue[0] = epochSinceSecond;
		retValue[1] = fractional;
		return retValue;
	}
	
	public int getFlag() {
		return flags;
	}
	
	public byte[] getSID() {
		return encodedSID;
	}
	
	public String getUUID() {
		StringBuilder uuid = new StringBuilder(ParserHelper.getHexString(getSID()).toLowerCase());
		//to uuid (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx )format;
		uuid.insert(8, '-');
		uuid.insert(13, '-');
		uuid.insert(18, '-');
		uuid.insert(23, '-');
		return uuid.toString();
	}
	
	public byte[] getGNO() {
		return encodedGNO;
	}
	
	public long getTransactionId() {
		return getLong(getGNO());
	}
	
	public String getGtid() {
		return getUUID()+":"+getTransactionId();
	}
	
	public byte getLogicalTimestampType() {
		return ltType;
	}
	
	public long getLastCommitted() {
		return lastCommit;
	}
	
	public long getSequenceNumber() {
		return sequenceNumber;
	}
	
	public byte[] getOriginalCommittedTimestamp() {
		return originalCommittedTimestamp;
	}
	
	public byte[] getImmediateCommittedTimestamp() {
		return immediateCommittedTimestamp;
	}
	
	public long getTransactionLength() {
		return transactionLength;
	}
	
	private static byte[] rpad(byte[] s, byte[] p) {
		byte[] retVal = new byte[s.length + p.length];
		System.arraycopy(s, 0, retVal, 0, s.length);
		System.arraycopy(p, 0, retVal, s.length, p.length);
		return retVal;
	}
	
	@Override
	public String toString() {
		String output = 
				    "last_committed               = " + getLastCommitted() 
				+ "\nsequence_number              = " + getSequenceNumber()
				+ "\ntransaction_length           = " + transactionLength ;
		if (getOriginalCommittedTimestamp() != null) {
			long[] ocTs = getCommittedTimestamp(getOriginalCommittedTimestamp());
			String strOcTs = ParserHelper.getString(new Date(ocTs[0] * 1000L)) + "." + ocTs[1];
			output = output + "\noriginal_committed_timestamp = " + strOcTs ;
		}

		if (getImmediateCommittedTimestamp() != null) {
			long[] icTs = getCommittedTimestamp(getImmediateCommittedTimestamp());
			String strIcTs = ParserHelper.getString(new Date(icTs[0] * 1000L)) + "." + icTs[1];
			output = output + "\nimmediate_commit_timestamp   = " + strIcTs;
		}
		output += "\n\n";
		output = output + "SET @@SESSION.GTID_NEXT= '" +getGtid()+"'\n";
		return output;   

	}
}
