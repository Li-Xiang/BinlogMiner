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

import java.util.HashMap;

public class RowImage {
	private int startPos = -1;
	private int endPos = -1;
	private boolean isAllNull = false;
	private final long tableId ;
	private final HashMap<Integer, ColumnImage> columnImages;
	
	public RowImage(long tableId) {
		columnImages = new HashMap<Integer, ColumnImage>();
		this.tableId = tableId;
	}
	
	public long getTableId() {
		return tableId;
	}
	
	public void setStartPosition(int pos) {
		startPos = pos;
	}
	
	public void setEndPosition(int pos) {
		endPos = pos;
	}
	
	public int getStartPosition() {
		return startPos;
	}
	
	public int getEndPosition() {
		return endPos;
	}
	
	public void setColumnImage(ColumnImage columnData) {
		columnImages.put(columnData.getColumnId(), columnData);
	}
	
	public void setColumnImage(int columnId, ColumnImage columnData) {
		columnImages.put(columnId, columnData);
	}
	
	public ColumnImage getColumnImage(int columnId) {
		return columnImages.get(columnId);
	}
	
	public int getColumnCount() {
		return columnImages.size();
	}
	
	public boolean isNullImage() {
		return isAllNull;
	}
	
	public void isNullImage(boolean isAllNull) {
		this.isAllNull = isAllNull;
	}
}
