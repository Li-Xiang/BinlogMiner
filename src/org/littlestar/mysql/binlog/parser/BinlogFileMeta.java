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

import java.io.File;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.impl.TableMapEventBody;

public class BinlogFileMeta {
	// Each log file contains a 4-byte magic number, the magic number bytes are 0xfe 0x62 0x69 0x6e ;
	public static final int MAGIC_NUMBER_LENGTH = 4;
	public static final byte[] MAGIC_NUMBER = new byte[] 
			{ (byte) 0xfe, (byte) 0x62, (byte) 0x69, (byte) 0x6e };
	private final File blogFile;
	private final ByteOrder order;
	private Date createTimestamp;
	private int binlogVersion;
	private String serverVersion;
	private int commonHeaderLength;
	private byte[] eventPostHeaderLengths;
	private int checksumLength = 0;
	private int checksumAlg = 0;
	private boolean decodeString = false;
	private String defaultCharsetName = "utf8";
	
	private final HashMap<Long, TableMapEventBody> tableMapEventBodys;
	
	public BinlogFileMeta(String blogFileName, ByteOrder order) {
		blogFile = new File(blogFileName);
		this.order = order;
		tableMapEventBodys = new HashMap<Long, TableMapEventBody>();
	}
	
	public ByteOrder getByteOrder() {
		return order;
	}
	
	public void setCreateTimestamp(Date timestamp) {
		createTimestamp = timestamp;
	}
	
	public Date getCreateTimestamp() {
		return createTimestamp;
	}
	
	public void setServerVersion(String version) {
		serverVersion = version;
	}
	
	public String getServerVersion() {
		return serverVersion;
	}
	
	public void setBinlogVersion(int version) {
		binlogVersion = version;
	}
	
	public int getBinlogVersion() {
		return binlogVersion;
	}
	
	public void putTableMapEventBody(TableMapEventBody body) {
		tableMapEventBodys.put(body.getTableId(), body);
	}
	
	public TableMapEventBody getTableMapEventBody(long tableId) {
		return tableMapEventBodys.get(tableId);
	}
	
	public void setCommonHeaderLength(int length) {
		commonHeaderLength = length;
	}
	
	
	public int getCommonHeaderLength() {
		return commonHeaderLength;
	}
	
	public void setPostHeaderLengths(byte[] array) {
		eventPostHeaderLengths = array;
	}
	
	public byte[] getPostHeaderLengths() {
		return eventPostHeaderLengths;
	}
	
	// A array indexed by (Binlog Event Type - 1) to extract the length of the event
	// specific (post) header.
	public int getPostHeaderLength(EventType eventType) {
		int evnetIndex = eventType.getTypeCode() - 1;
		byte rawPostHeaderLength = eventPostHeaderLengths[evnetIndex];
		int postHeaderLength = ParserHelper.getUnsignedInteger(rawPostHeaderLength);
		return postHeaderLength;
	}
	
	public void setChecksumLength(int length) {
		checksumLength = length;
	}
	
	public int getChecksumLength() {
		return checksumLength;
	}
	
	public void setChecksumAlg(int alg) {
		checksumAlg = alg;
	}
	
	public int getChecksumAlg() {
		return checksumAlg;
	}
	
	public String getChecksumAlgName() {
		switch (checksumAlg) {
		case 0: return "NONE";
		case 1: return "CRC32";
		default: return "UNKNOWN{"+checksumAlg+"}";
		}
	}
	
	public void setDefaultCharsetName(String mysqlCharsetName) {
		if (mysqlCharsetName != null)
			defaultCharsetName = mysqlCharsetName;
	}
	
	public void decodeString(boolean decode) {
		decodeString = decode;
	}
	
	public boolean decodeString() {
		return decodeString;
	}
	
	public String getDefaultCharsetName() {
		return defaultCharsetName;
	}
	
	public long getFileSize() {
		return blogFile.length();
	}
	
	public File getFile() {
		return blogFile;
	}
	
	@Override
	public String toString() {
		return "server version: " + serverVersion 
				+ ", binlog version: "+binlogVersion 
				+ ", binlog created: "+getCreateTimestamp()
				+ ", common-header length: "+getCommonHeaderLength()
				+ ", checksum algorithm: "+ getChecksumAlgName()
				+ ", checksum length: " + getChecksumLength()
				;
	}
	
}
