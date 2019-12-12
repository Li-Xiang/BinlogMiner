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

/**
 * EXECUTE_LOAD_QUERY_EVENT: Event responsible for LOAD DATA execution, it similar to Query_event but before 
 * executing the query it substitutes original filename in LOAD DATA query with name of temporary file. 
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/execute-load-query-event.html
 *   https://dev.mysql.com/doc/dev/mysql-server/8.0.13/classbinary__log_1_1Execute__load__query__event.html
 *   
 * Post-header
 *     4              slave_proxy_id
 *     4              execution time
 *     1              schema length
 *     2              error-code
 *     2              status-vars length
 * Payload
 *     4              file_id
 *     4              start_pos
 *     4              end_pos
 *     1              dup_handling_flags
 *     (+ QUERY_EVENT's Payload +)
 */

public class ExecuteLoadQueryEvent extends EventBodyDefaultImpl {
	public static final int SLAVE_PROXY_ID_LENGTH     = 4;
	public static final int EXECUTION_TIME_LENGTH     = 4;
	public static final int SCHEMA_LENGTH_LENGTH      = 1;
	public static final int ERROR_CODE_LENGTH         = 2;
	public static final int STATUS_VARS_LENGTH_LENGTH = 2;
	public static final int FILE_ID_LENGTH            = 4;
	public static final int START_POS_LENGTH          = 4;
	public static final int END_POS_LENGTH            = 4;
	public static final int DUP_HANDLING_FLAGS_LENGTH = 1;
	
	private long slaveProxyId ;
	private long executionTime;
	private int schemaLength;
	private int errorCode;
	private int statusVarsLength ;
	private long fileId ;
	private long startPos; //The start position within the statement for filename substitution
	private long stopPos; //The end position within the statement for filename substitution
	private int dupFlags ;
	private int pos = 0;
	private byte[] statusVars; 
	private String schema;
	private String query;
	
	public ExecuteLoadQueryEvent(byte[] bodyData, EventHeader eventHeader, BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		slaveProxyId = getUnsignedLong(bodyData, pos, pos += SLAVE_PROXY_ID_LENGTH);
		executionTime = getUnsignedLong(bodyData, pos, pos += EXECUTION_TIME_LENGTH);
		schemaLength = getUnsignedInteger(bodyData, pos, pos += SCHEMA_LENGTH_LENGTH);
		errorCode = getUnsignedInteger(bodyData, pos, pos += ERROR_CODE_LENGTH);
		statusVarsLength = getInteger(bodyData, pos, pos += STATUS_VARS_LENGTH_LENGTH);
		fileId = getUnsignedLong(bodyData, pos, pos += FILE_ID_LENGTH);
		startPos = getUnsignedLong(bodyData, pos, pos += START_POS_LENGTH);
		stopPos = getUnsignedLong(bodyData, pos, pos += END_POS_LENGTH);
		dupFlags = getUnsignedInteger(bodyData, pos, pos += DUP_HANDLING_FLAGS_LENGTH);
		
		if (statusVarsLength > 0) {
			statusVars = getBytes(bodyData, pos, pos += statusVarsLength);
		}
		schema = getString(bodyData, pos, pos += schemaLength).trim();
		pos++; // skip null -- [00];
		query = getNulTerminatedString(bodyData, pos, getBodyDataLength());
	}
	
	public long getThreadId() {
		return slaveProxyId;
	}
	
	public long getExecuteTime() {
		return executionTime;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public byte[] getStatusVars() {
		return statusVars;
	}
	
	public long getFileId() {
		return fileId;
	}
	
	public long getStartPos() {
		return startPos;
	}
	
	public long getStopPos() {
		return stopPos;
	}
	
	public int getDuplicateHandleFlag() {
		return dupFlags;
	}
	
	public String getDuplicateHandleFlagName() {
		return getDuplicateHandleFlagString(dupFlags);
	}
	
	@Override
	public String toString() {
		return 	query+"\n\n(thread_id="+slaveProxyId
				+", exec_time="+executionTime
				+", schema="+schema
				+", error_code="+errorCode
				+", file_id="+fileId
				+", start_pos="+startPos 
				+", stop_pos="+stopPos
				+", dup_flags="+getDuplicateHandleFlagName()+")\n";
	}

	public static String getDuplicateHandleFlagString(int flag) {
		switch(flag) {
		case 0: return "LOAD_DUP_ERROR";
		case 1: return "LOAD_DUP_IGNORE";
		case 2: return "LOAD_DUP_REPLACE";
		default: return "";
		}
	}
}
