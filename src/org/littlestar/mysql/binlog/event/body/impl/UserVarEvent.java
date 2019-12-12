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

import org.littlestar.mysql.binlog.event.body.helper.Collation;
import org.littlestar.mysql.binlog.event.body.helper.Collations;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;

/**
 * USER_VAR_EVENT: Written every time a statement uses a user variable; precedes other events for the statement.
 *  
 * References: 
 *   https://dev.mysql.com/doc/internals/en/user-var-event.html
 *   https://dev.mysql.com/doc/dev/mysql-server/8.0.13/classbinary__log_1_1User__var__event.html
 *   
 * Payload
 *   4                    name_length
 *   string[$len]         name
 *   1                    is_null
 *   if not is_null {
 *       1                type
 *       4                charset
 *       4                value_length
 *       string[$len]     value
 *       if more data {
 *           1            flags
 *       }
 *    }
 */
public class UserVarEvent extends EventBodyDefaultImpl {
	public static final int NAME_LENGTH_LENGTH   = 4;
	public static final int IS_NULL_LENGTH       = 1;
	public static final int TYPE_LENGTH          = 1;
	public static final int CHARSET_LENGTH       = 4;
	public static final int VALUE_LENGTH_LENGTH  = 4;
	public static final int FLAGS                = 1;
	private String name ; 
	private int typeId;
	private int charsetId;
	private String value;
	private int pos = 0;
	private int isNull;
	private int flags = 0x00;
	
	public UserVarEvent(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		super(bodyData, eventHeader, meta);
		long nameLength = getUnsignedLong(bodyData, pos, pos += NAME_LENGTH_LENGTH);
		name = getString(bodyData, pos, pos += nameLength);
		isNull = getInteger(bodyData, pos, pos += IS_NULL_LENGTH);
		if (isNull == 0) {
			typeId = getUnsignedInteger(bodyData, pos, pos += TYPE_LENGTH);
			charsetId = getUnsignedInteger(bodyData, pos, pos += CHARSET_LENGTH);
			long valueLength = getUnsignedLong(bodyData, pos, pos += VALUE_LENGTH_LENGTH);
			value = getString(bodyData, pos, pos += valueLength);
			int remains = eventHeader.getEventBodyLength() - meta.getChecksumLength() - pos;
			if (remains > 0) {
				flags = 0x01;
			}
		}
	}
	
	public String getVariableName() {
		return name;
	}
	
	public String getVariableValue() {
		return value;
	}
	
	public int isNull() {
		return isNull;
	}
	
	public int getVariableTypeNumber() {
		return typeId;
	}

	public String getVariableTypeName() {
		return getValueType(typeId);
	}
	
	public int getVariableCharsetNumber () {
		return charsetId;
	}
	
	public Collation getVariableCharset() {
		return Collations.getCollation(charsetId);
	}
	
	public int getFlages() {
		return flags;
	}
	
	public static String getValueType(int type) {
		switch (type) {
		case 0:  return "STRING_TYPE";
		case 1:  return "REAL_TYPE";
		case 2:  return "INT_TYPE";
		case 3:  return "ROW_TYPE";
		case 4:  return "DECIMAL_TYPE";
		case 5:  return "VALUE_TYPE_COUNT";
		default: return "UNKNOWN_TYPE";
		}
	}
	
	public String toString() {
		Collation coll = getVariableCharset();
		return "set @`"+name+"`:=" + value 
				+"\n\n(is-null="+isNull+", type="+getVariableTypeName()+", charset="+coll.getCharacterSetName()
				+", collate="+coll.getCollationName()+", more-data="+flags+")\n";
	}
}
