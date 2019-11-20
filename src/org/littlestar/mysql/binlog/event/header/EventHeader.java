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

package org.littlestar.mysql.binlog.event.header;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.littlestar.mysql.binlog.event.EventType;

/*
 * https://dev.mysql.com/doc/internals/en/binlog-event-header.html
 *  Binlog header Payload： 
 *    4              timestamp
 *    1              event type
 *    4              server-id
 *    4              event-size
 *    if binlog-version > 1:
 *        4              log pos
 *        2              flags
 *        
 * https://dev.mysql.com/doc/internals/en/binlog-version.html
*/

public class EventHeader {
	public static final int COMMON_HEADER_LENGTH_V1 = 13; 
	public static final int COMMON_HEADER_LENGTH_V2 = 19;
	public static final int COMMON_HEADER_LENGTH_V3 = 19;
	public static final int COMMON_HEADER_LENGTH_V4 = 19;
	public static final int TIMESTAMP_LENGTH        = 4;
    public static final int EVENT_TYPE_LENGTH       = 1;
    public static final int SERVER_ID_LENGTH        = 4;
    public static final int EVENT_SIZE_LENGTH       = 4;
    public static final int NEXT_POS_LENGTH         = 4;
    public static final int FLAGS_LENGTH            = 2;
    
    protected final int commonHeaderLength;
    protected Date timestamp;
    protected EventType eventType;
    protected long serverId;
    protected int eventSize;
    protected long nextPosition;
    protected int flags;
    
    public EventHeader(int length) {
    	commonHeaderLength = length;
    }
    
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = new Date(timestamp);
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public int getEventSize() {
		return eventSize;
	}

	public void setEventSize(int eventSize) {
		this.eventSize = eventSize;
	}

	public long getStartPosition() {
		return nextPosition - eventSize;
	}

	public long getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(long nextPosition) {
		this.nextPosition = nextPosition;
	}

	public int getEventFlags() {
		return flags;
	}
	
	/*
	 * https://dev.mysql.com/doc/internals/en/binlog-event-flag.html
	 *   Hex       Flag 
	 * -----------+----------------------------------------
	 *   0x0001    LOG_EVENT_BINLOG_IN_USE_F
	 *   0x0002    LOG_EVENT_FORCED_ROTATE_F
	 *   0x0004    LOG_EVENT_THREAD_SPECIFIC_F
	 *   0x0008    LOG_EVENT_SUPPRESS_USE_F
	 *   0x0010    LOG_EVENT_UPDATE_TABLE_MAP_VERSION_F
	 *   0x0020    LOG_EVENT_ARTIFICIAL_F
	 *   0x0040    LOG_EVENT_RELAY_LOG_F
	 *   0x0080    LOG_EVENT_IGNORABLE_F
	 *   0x0100    LOG_EVENT_NO_FILTER_F 
	 *   0x0200    LOG_EVENT_MTS_ISOLATE_F
	 */
	public void setEventFlags(int flags) {
		this.flags = flags;
	}
	
	public int getEventHeaderLength() {
		return commonHeaderLength;
	}

	/*
	 * Return specific binary log event's data part full length, including checksum part, 
	 * if checksum is enabled.
	 */
	public int getEventBodyLength() {
		return getEventSize() - getEventHeaderLength();
	}
	
	public String getText() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		String ts = String.format("%-21s", sf.format(getTimestamp()));
		String et = String.format("%-28s", getEventType().toString());
		String si = String.format("%-12s", getServerId());
		String sp = String.format("%-12s", getStartPosition());
		String ep = String.format("%-12s", getNextPosition());
		return ts + et + si + sp + ep;
	}
	
	@Override
	public String toString() {
		return getText();
	}
}
