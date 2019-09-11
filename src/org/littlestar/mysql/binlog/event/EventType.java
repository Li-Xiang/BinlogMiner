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

package org.littlestar.mysql.binlog.event;

public enum EventType {
	//https://dev.mysql.com/doc/dev/mysql-server/8.0.11/binlog__event_8h_source.html
	UNKNOWN_EVENT(0), 
	START_EVENT_V3(1), 
	QUERY_EVENT(2), 
	STOP_EVENT(3), 
	ROTATE_EVENT(4), 
	INTVAR_EVENT(5), 
	SLAVE_EVENT(7), 
	APPEND_BLOCK_EVENT(9), 
	DELETE_FILE_EVENT(11), 
	RAND_EVENT(13), 
	USER_VAR_EVENT(14), 
	FORMAT_DESCRIPTION_EVENT(15), 
	XID_EVENT(16), 
	BEGIN_LOAD_QUERY_EVENT(17), 
	EXECUTE_LOAD_QUERY_EVENT(18), 
	TABLE_MAP_EVENT(19), 
	WRITE_ROWS_EVENT_V1(23), 
	UPDATE_ROWS_EVENT_V1(24), 
	DELETE_ROWS_EVENT_V1(25), 
	INCIDENT_EVENT(26), 
	HEARTBEAT_LOG_EVENT(27), 
	IGNORABLE_LOG_EVENT(28), 
	ROWS_QUERY_LOG_EVENT(29), 
	WRITE_ROWS_EVENT(30), 
	UPDATE_ROWS_EVENT(31), 
	DELETE_ROWS_EVENT(32), 
	GTID_LOG_EVENT(33), 
	ANONYMOUS_GTID_LOG_EVENT(34), 
	PREVIOUS_GTIDS_LOG_EVENT(35), 
	TRANSACTION_CONTEXT_EVENT(36), 
	VIEW_CHANGE_EVENT(37), 
	XA_PREPARE_LOG_EVENT(38), 
	PARTIAL_UPDATE_ROWS_EVENT(39);
	
	final int code;
	
	EventType(int code) {
		this.code = code;
	}
	
	public int getTypeCode() {
		return code;
	}
	
	public static EventType getEventType(final int code) {
		switch(code) {
		case 0: return UNKNOWN_EVENT;
		case 1: return START_EVENT_V3;
		case 2: return QUERY_EVENT;
		case 3: return STOP_EVENT;
		case 4: return ROTATE_EVENT;
		case 5: return INTVAR_EVENT;
		case 7: return SLAVE_EVENT;
		case 8: return APPEND_BLOCK_EVENT;
		case 11: return DELETE_FILE_EVENT;
		case 13: return RAND_EVENT;
		case 14: return USER_VAR_EVENT;
		case 15: return FORMAT_DESCRIPTION_EVENT;
		case 16: return XID_EVENT;
		case 17: return BEGIN_LOAD_QUERY_EVENT;
		case 18: return EXECUTE_LOAD_QUERY_EVENT;
		case 19: return TABLE_MAP_EVENT;
		case 23: return WRITE_ROWS_EVENT_V1;
		case 24: return UPDATE_ROWS_EVENT_V1;
		case 25: return DELETE_ROWS_EVENT_V1;
		case 26: return INCIDENT_EVENT;
		case 27: return HEARTBEAT_LOG_EVENT;
		case 28: return IGNORABLE_LOG_EVENT;
		case 29: return ROWS_QUERY_LOG_EVENT;
		case 30: return WRITE_ROWS_EVENT;
		case 31: return UPDATE_ROWS_EVENT;
		case 32: return DELETE_ROWS_EVENT;
		case 33: return GTID_LOG_EVENT;
		case 34: return ANONYMOUS_GTID_LOG_EVENT;
		case 35: return PREVIOUS_GTIDS_LOG_EVENT;
		case 36: return TRANSACTION_CONTEXT_EVENT;
		case 37: return VIEW_CHANGE_EVENT;
		case 38: return XA_PREPARE_LOG_EVENT;
		case 39: return PARTIAL_UPDATE_ROWS_EVENT;
		default: return UNKNOWN_EVENT;
		}
	}
	
	public static EventType getEventType(final String type) {
		if(UNKNOWN_EVENT.isEqual(type)) return UNKNOWN_EVENT;
		if(START_EVENT_V3.isEqual(type)) return START_EVENT_V3;
		if(QUERY_EVENT.isEqual(type)) return QUERY_EVENT;
		if(STOP_EVENT.isEqual(type)) return STOP_EVENT;
		if(ROTATE_EVENT.isEqual(type)) return ROTATE_EVENT;
		if(INTVAR_EVENT.isEqual(type)) return INTVAR_EVENT;
		if(SLAVE_EVENT.isEqual(type)) return SLAVE_EVENT;
		if(APPEND_BLOCK_EVENT.isEqual(type)) return APPEND_BLOCK_EVENT;
		if(DELETE_FILE_EVENT.isEqual(type)) return DELETE_FILE_EVENT;
		if(RAND_EVENT.isEqual(type)) return RAND_EVENT;
		if(USER_VAR_EVENT.isEqual(type)) return USER_VAR_EVENT;
		if(FORMAT_DESCRIPTION_EVENT.isEqual(type)) return FORMAT_DESCRIPTION_EVENT;
		if(XID_EVENT.isEqual(type)) return XID_EVENT;
		if(BEGIN_LOAD_QUERY_EVENT.isEqual(type)) return BEGIN_LOAD_QUERY_EVENT;
		if(EXECUTE_LOAD_QUERY_EVENT.isEqual(type)) return EXECUTE_LOAD_QUERY_EVENT;
		if(TABLE_MAP_EVENT.isEqual(type)) return TABLE_MAP_EVENT;
		if(WRITE_ROWS_EVENT_V1.isEqual(type)) return WRITE_ROWS_EVENT_V1;
		if(UPDATE_ROWS_EVENT_V1.isEqual(type)) return UPDATE_ROWS_EVENT_V1;
		if(DELETE_ROWS_EVENT_V1.isEqual(type)) return DELETE_ROWS_EVENT_V1;
		if(INCIDENT_EVENT.isEqual(type)) return INCIDENT_EVENT;
		if(HEARTBEAT_LOG_EVENT.isEqual(type)) return HEARTBEAT_LOG_EVENT;
		if(IGNORABLE_LOG_EVENT.isEqual(type)) return IGNORABLE_LOG_EVENT;
		if(ROWS_QUERY_LOG_EVENT.isEqual(type)) return ROWS_QUERY_LOG_EVENT;
		if(WRITE_ROWS_EVENT.isEqual(type)) return WRITE_ROWS_EVENT;
		if(UPDATE_ROWS_EVENT.isEqual(type)) return UPDATE_ROWS_EVENT;
		if(DELETE_ROWS_EVENT.isEqual(type)) return DELETE_ROWS_EVENT;
		if(GTID_LOG_EVENT.isEqual(type)) return GTID_LOG_EVENT;
		if(ANONYMOUS_GTID_LOG_EVENT.isEqual(type)) return ANONYMOUS_GTID_LOG_EVENT;
		if(PREVIOUS_GTIDS_LOG_EVENT.isEqual(type)) return PREVIOUS_GTIDS_LOG_EVENT;
		if(TRANSACTION_CONTEXT_EVENT.isEqual(type)) return TRANSACTION_CONTEXT_EVENT;
		if(VIEW_CHANGE_EVENT.isEqual(type)) return VIEW_CHANGE_EVENT;
		if(XA_PREPARE_LOG_EVENT.isEqual(type)) return XA_PREPARE_LOG_EVENT;
		if(PARTIAL_UPDATE_ROWS_EVENT.isEqual(type)) return PARTIAL_UPDATE_ROWS_EVENT;
		return UNKNOWN_EVENT;
	}
	
	public boolean isEqual(final String type) {
		return name().equalsIgnoreCase(type);
	}
}
