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

package org.littlestar.mysql.binlog.parser;

import java.io.Closeable;

import org.littlestar.mysql.binlog.event.BinlogEvent;

public interface BinlogParser extends Closeable {
	public BinlogEvent nextEvent() throws Throwable;
	public boolean hasEvent();
	public BinlogFileMeta getBinlogFileMeta();
	public long getPosition();
	//public TableMapEventBody getTableMapEvent(long tableId);
	
}
