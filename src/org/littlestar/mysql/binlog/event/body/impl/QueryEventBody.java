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
 * QUERY_EVENT: The query event is used to send text querys right the binlog. 
 * 
 * References: 
 *   https://dev.mysql.com/doc/internals/en/query-event.html
 *   https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html
 * 
 * Post-header :
 *   4              slave_proxy_id
 *   4              execution time
 *   1              schema length
 *   2              error-code
 * if binlog-version ≥ 4:
 *   2              status-vars length
 *   
 * Payload:     
 *   string[$len]   status-vars 
 *   string[$len]   schema
 *   1              [00]
 *   string[EOF]    query
 *   
 */

public class QueryEventBody extends EventBodyDefaultImpl {
	public static final int SLAVE_PROXY_ID_LENGTH = 4;
	public static final int EXECUTION_TIME_LENGTH = 4;
	public static final int SCHEMA_LENGTH_LENGTH = 1;
	public static final int ERROR_CODE_LENGTH = 2;
	public static final int STATUS_VARS_LENGTH_LENGTH = 2;

	private long slaveProxyId;
	private long executionTime;
	private int schemaLength;
	private int errorCode;
	private int statusVarsLength = 0;
	private byte[] statusVars;
	private String schema;
	private String query;
	private int pos = 0;
	
	public QueryEventBody(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		
		slaveProxyId = getUnsignedLong(bodyData, pos, pos += SLAVE_PROXY_ID_LENGTH);
		executionTime = getUnsignedLong(bodyData, pos, pos += EXECUTION_TIME_LENGTH);
		schemaLength = getUnsignedInteger(bodyData, pos, pos += SCHEMA_LENGTH_LENGTH);
		errorCode = getUnsignedInteger(bodyData, pos, pos += ERROR_CODE_LENGTH);

		if (meta.getBinlogVersion() >= 4) {
			statusVarsLength = getInteger(bodyData, pos, pos += STATUS_VARS_LENGTH_LENGTH);
		}
		
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

	public long getExecutionTime() {
		return executionTime;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public byte[] getStatusVars() {
		return statusVars;
	}

	public String getSchema() {
		return schema;
	}

	public String getQuery() {
		return query;
	}
	
	@Override
	public String toString() {
		return "thread_id: "+getThreadId()
			+", exec_time: "+getExecutionTime()
			+", schema: "+getSchema()
			+", error_code: "+getErrorCode() + "\n"
			+"query: "+getQuery()
			+"\n";
	}
}
