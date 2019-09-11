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

package org.littlestar.mysql.binlog.sql;

public class StatmentPair {
	private String undoStmt;
	private String redoStmt;
	
	public String getUndoStatement() {
		return undoStmt;
	}
	
	public String getRedoStatement() {
		return redoStmt;
	}
	
	public void setUndoStatement(String stmt) {
		undoStmt = stmt;
	}
	
	public void setRedoStatement(String stmt) {
		redoStmt = stmt;
	}
}
