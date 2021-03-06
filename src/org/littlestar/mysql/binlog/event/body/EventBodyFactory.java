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

package org.littlestar.mysql.binlog.event.body;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.impl.BeginLoadQueryEvent;
import org.littlestar.mysql.binlog.event.body.impl.EventBodyDefaultImpl;
import org.littlestar.mysql.binlog.event.body.impl.ExecuteLoadQueryEvent;
import org.littlestar.mysql.binlog.event.body.impl.FormatDescriptionEventBody;
import org.littlestar.mysql.binlog.event.body.impl.GtidLogEventBody;
import org.littlestar.mysql.binlog.event.body.impl.PreviousGtidsLogEventBody;
import org.littlestar.mysql.binlog.event.body.impl.QueryEventBody;
import org.littlestar.mysql.binlog.event.body.impl.RotateEventBody;
import org.littlestar.mysql.binlog.event.body.impl.RowsEventBody;
import org.littlestar.mysql.binlog.event.body.impl.StartEventV3Body;
import org.littlestar.mysql.binlog.event.body.impl.StopEventBody;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;
import org.littlestar.mysql.binlog.event.body.impl.UserVarEvent;
import org.littlestar.mysql.binlog.event.body.impl.XidEventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;

public class EventBodyFactory {
	public static EventBody createEventBody(final byte[] bodyData, final EventHeader eventHeader,
			final BinlogFileMeta binlogFileMeta, boolean isDecode) {
		if (!isDecode) {
			return new EventBodyDefaultImpl(bodyData, eventHeader, binlogFileMeta);
		}
		EventBody eventBody = null;	
		EventType type = eventHeader.getEventType();
		switch (type) {
		case FORMAT_DESCRIPTION_EVENT:
			eventBody = new FormatDescriptionEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case START_EVENT_V3:
			eventBody = new StartEventV3Body(bodyData, eventHeader, binlogFileMeta);
			break;
		case QUERY_EVENT:
			eventBody = new QueryEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case XID_EVENT:
			eventBody = new XidEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case TABLE_MAP_EVENT:
			eventBody = new TableMapEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case WRITE_ROWS_EVENT_V1:
		case UPDATE_ROWS_EVENT_V1:
		case DELETE_ROWS_EVENT_V1:
		case WRITE_ROWS_EVENT:
		case UPDATE_ROWS_EVENT:
		case DELETE_ROWS_EVENT:
			eventBody = new RowsEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case ANONYMOUS_GTID_LOG_EVENT:
		case GTID_LOG_EVENT:
			eventBody = new GtidLogEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case ROTATE_EVENT:
			eventBody = new RotateEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case PREVIOUS_GTIDS_LOG_EVENT:
			eventBody = new PreviousGtidsLogEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case STOP_EVENT:
			eventBody = new StopEventBody(bodyData, eventHeader, binlogFileMeta);
			break;
		case BEGIN_LOAD_QUERY_EVENT:
			eventBody = new BeginLoadQueryEvent(bodyData, eventHeader, binlogFileMeta);
			break;
		case USER_VAR_EVENT:
			eventBody = new UserVarEvent(bodyData, eventHeader, binlogFileMeta);
			break;
		case EXECUTE_LOAD_QUERY_EVENT:
			eventBody = new ExecuteLoadQueryEvent(bodyData, eventHeader, binlogFileMeta);
			break;
		default:
			eventBody = new EventBodyDefaultImpl(bodyData, eventHeader, binlogFileMeta);
			break;
		}
		return eventBody;
	}
}
