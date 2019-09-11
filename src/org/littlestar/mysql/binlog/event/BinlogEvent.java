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

import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;


/*
  Binlog structure definition in my BlogParser :
  ++------------++ ---------------------------------++--------------------------------------------------++ 
  ||		    ||<--         Event Header       -->||<--                    Event Body              -->||  
  || Definition ||                                  ||<--     Event Data      -->|<-- Event Checksum -->||
  ||------------++----------------------------------++--------------+------------+----------------------++
  || Binary Log || Common Header (fixed 19 bytes)   || Post-Header  |  PlayLoad  |   Checksum*          ||
  ++------------++----------------------------------++--------------+ -----------+----------------------++   
 * https://dev.mysql.com/worklog/task/?id=2540#tabs-2540-4
   A =  checksum algorithm (fixed 1 byte), FORMAT_DESCRIPTION_EVENT only;
*/

public class BinlogEvent {
	private EventHeader header;
	private EventBody body;

	public BinlogEvent(EventHeader header, EventBody body) {
		this.header = header;
		this.body = body;
	}
	
	public EventHeader getHeader() {
		return header;
	}

	public void getBody(EventBody body) {
		this.body = body;
	}

	public EventBody getBody() {
		return body;
	}
}
