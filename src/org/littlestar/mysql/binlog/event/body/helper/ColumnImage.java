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

package org.littlestar.mysql.binlog.event.body.helper;

public class ColumnImage {
	private int columnId;
	private Object value;
	
	public ColumnImage(int id) {
		columnId = id;
	}
	
	public int getColumnId() {
		return columnId;
	}
	
	public void setColumnValue(Object value) {
		this.value = value;
	}
	
	public boolean isNullValue() {
		if (value == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public Object getColumnValue() {
		return value;
	}
	
}
