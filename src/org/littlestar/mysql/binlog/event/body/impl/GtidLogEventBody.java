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
import java.util.BitSet;
import java.util.Date;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

/**
 * GTID_LOG_EVENT
 * 
 * Reference: libbinlogevents/src/control_events.cpp
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
 * 
 * Fetch the timestamps used to monitor replication lags with respect to
 * the immediate master and the server that originated this transaction.
 * Check that the timestamps exist before reading. Note that a master
 * older than MySQL-5.8 will NOT send these timestamps. We should be
 * able to ignore these fields in this case.
 * 
 * 说明1: GTID_LOG_EVENT 8.0开始引入"*_commit_timestamp"用于延时复制(master_delay)
 *         immediate_commit_timestamp: 事务在master端提交的时间戳;
 *         original_commit_timestamp : 事务在slave端提交的时间戳;
 *      master端的GTID_LOG_EVENT事件中, immediate_commit_timestamp一定是等于original_commit_timestamp;
 *      slave端的GTID_LOG_EVENT事件中(启用binlog), immediate_commit_timestamp可能不original_commit_timestamp;
 *      
 * 说明2: ANONYMOUS_GTID_LOG_EVENT是GTID_LOG_EVENT的一个特例, 区别在于ANONYMOUS_GTID_LOG_EVENT的Server UUID为空。
*/

public class GtidLogEventBody extends EventBodyDefaultImpl {
	public static final int ENCODED_FLAG_LENGTH = 1;
	public static final int ENCODED_SID_LENGTH = 16; 
	public static final int ENCODED_GNO_LENGTH = 8;
	public static final int LT_TYPE_LENGTH = 1;
	public static final int LAST_COMMITTED_LENGTH = 8;
	public static final int SEQUENCE_NUMBER_LENGTH = 8;
	public static final int ENCODED_COMMIT_TIMESTAMP_LENGTH = 55;
	public static final int IMMEDIATE_COMMIT_TIMESTAMP_LENGTH = 7;
	public static final int ORIGINAL_COMMIT_TIMESTAMP_LENGTH = 7;
	public static final int ENCODED_SERVER_VERSION_LENGTH = 31;
	public static final int IMMEDIATE_SERVER_VERSION_LENGTH = 4;
	public static final int ORIGINAL_SERVER_VERSION_LENGTH = 4;
	private int flags;
	private byte[] encodedSID;
	private byte[] encodedGNO;
	private byte ltType;
	private long lastCommit;
	private long sequenceNumber;
	private byte[] originalCommittedTimestamp;
	private byte[] immediateCommittedTimestamp;
	private long transactionLength;
	
	private long originalServerVersion = -1;
	private long immediateServerVersion = -1;
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
		if (remainBytes >= 7) {
			immediateCommittedTimestamp = getBytes(bodyData, pos, pos += IMMEDIATE_COMMIT_TIMESTAMP_LENGTH);
			BitSet bitSet = BitSet.valueOf(immediateCommittedTimestamp);
			if (bitSet.get(ENCODED_COMMIT_TIMESTAMP_LENGTH)) {
				// if high bit (55) set, original_commit_timestamp != immediate_commit_timestamp
				bitSet.clear(ENCODED_COMMIT_TIMESTAMP_LENGTH); // -> clean immediate_commit_timestamp's high bit
				immediateCommittedTimestamp = bitSet.toByteArray();
				// read original_timestamp;
				originalCommittedTimestamp = getBytes(bodyData, pos, pos += ORIGINAL_COMMIT_TIMESTAMP_LENGTH);
			} else {
				originalCommittedTimestamp = immediateCommittedTimestamp;
			}
		}
		remainBytes = getBodyDataLength() - pos;
		if (remainBytes >= 1) {
			//mysys/pack.cc -> uint64_t net_field_length_ll(uchar **packet) { ... }
			Byte byte1 = getBytes(bodyData, pos, pos += 1)[0];
			int value1 = getUnsignedInteger(byte1);
			if (value1 < 251) {
				transactionLength = value1;
			} else if (value1 == 251) {
				// NULL_LENGTH???
			} else if (value1 == 252) {
				if (remainBytes >= 3)
					transactionLength = this.getUnsignedLong(bodyData, pos, pos += 2);
			} else if (value1 == 253) {
				if (remainBytes >= 4)
					transactionLength = this.getUnsignedLong(bodyData, pos, pos += 3);
			} else if (value1 == 254) {
				if (remainBytes >= 9)
					transactionLength = this.getUnsignedLong(bodyData, pos, pos += 8);
			}
		}
		
		remainBytes = getBodyDataLength() - pos;
		if (remainBytes >= 4) {
			byte[] rawOriginalServerVersion = null, rawImmediateServerVersion = null;
			rawImmediateServerVersion = getBytes(bodyData, pos, pos += IMMEDIATE_SERVER_VERSION_LENGTH);
			BitSet bitSet = BitSet.valueOf(rawImmediateServerVersion);
			if (bitSet.get(ENCODED_SERVER_VERSION_LENGTH)) {
				// if high bit (31) set, originalServerVersion != immediateServerVersion
				bitSet.clear(ENCODED_COMMIT_TIMESTAMP_LENGTH); // -> clean immediateServerVersion high bit
				rawImmediateServerVersion = bitSet.toByteArray();
				// read originalServerVersion;
				if (remainBytes >= 8)
					rawOriginalServerVersion = getBytes(bodyData, pos, pos += ORIGINAL_SERVER_VERSION_LENGTH);
			} else {
				rawOriginalServerVersion = rawImmediateServerVersion;
			}
			if (rawOriginalServerVersion != null) {
				originalServerVersion = getUnsignedLong(rawOriginalServerVersion);
			}
			if (rawImmediateServerVersion != null) {
				immediateServerVersion = getUnsignedLong(rawImmediateServerVersion);
			}
		}
	}
	
	public long[] getCommittedTimestamp(byte[] timestamp) {
		long[] retValue = new long[2];
		byte[] padedTs = rpad(timestamp, new byte[] { 0x00 });
		long tsValue = ParserHelper.getUnsignedLong(padedTs, ByteOrder.BIG_ENDIAN);
		//System.out.println(tsValue);
		//long tsValue = 1576117446907846L;
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
	
	public String getOriginalCommittedTimestamp() {
		String retValue = null;
		if (originalCommittedTimestamp != null) {
			long[] timestamp = getCommittedTimestamp(originalCommittedTimestamp);
			retValue = ParserHelper.getString(new Date(timestamp[0] * 1000L)) + "." + timestamp[1];
		}
		return retValue;
	}
	
	public String getImmediateCommittedTimestamp() {
		String retValue = null;
		if (immediateCommittedTimestamp != null) {
			long[] timestamp = getCommittedTimestamp(immediateCommittedTimestamp);
			retValue = ParserHelper.getString(new Date(timestamp[0] * 1000L)) + "." + timestamp[1];
		}
		return retValue;
	}
	
	public long getTransactionLength() {
		return transactionLength;
	}
	
	public long getOriginalServerVersion() {
		return originalServerVersion;
	}
	
	public long getImmediateServerVersion() {
		return immediateServerVersion;
	}
	
	private static byte[] rpad(byte[] s, byte[] p) {
		byte[] retVal = new byte[s.length + p.length];
		System.arraycopy(s, 0, retVal, 0, s.length);
		System.arraycopy(p, 0, retVal, s.length, p.length);
		return retVal;
	}
	
	private String getGtidNext() {
		EventType et = getEventHeader().getEventType();
		if (et.equals(EventType.ANONYMOUS_GTID_LOG_EVENT)) {
			return "ANONYMOUS";
		} else if (et.equals(EventType.GTID_LOG_EVENT)) {
			return getGtid();
		} else {
			return "";
		}
	}
	
	@Override
	public String toString() {
		String output = 
				    "last_committed               = " + getLastCommitted() 
				+ "\nsequence_number              = " + getSequenceNumber()
				+ "\ntransaction_length           = " + transactionLength ;
		if (originalCommittedTimestamp != null) {
			output = output + "\noriginal_commit_timestamp    = " + getOriginalCommittedTimestamp();
		}

		if (getImmediateCommittedTimestamp() != null) {
			output = output + "\nimmediate_commit_timestamp   = " + getImmediateCommittedTimestamp();
		}
		if(originalServerVersion > 0) {
			output = output + "\noriginal_server_version      = " + originalServerVersion;
		}
		
		if(immediateServerVersion > 0) {
			output = output + "\nimmediate_server_version     = " + immediateServerVersion;
		}
		
		output += "\n\n";
		output = output + "SET @@SESSION.GTID_NEXT= '" +getGtidNext()+"'\n";
		return output;   
	}
}
