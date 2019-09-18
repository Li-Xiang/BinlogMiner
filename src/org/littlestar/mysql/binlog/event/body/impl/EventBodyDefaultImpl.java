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

import java.util.Arrays;
import java.util.BitSet;
import java.nio.ByteOrder;

import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogFileMeta;
import org.littlestar.mysql.binlog.parser.ParserHelper;

public class EventBodyDefaultImpl implements EventBody {
	protected final byte[] bodyData;
	protected final EventHeader eventHeader;
	protected final BinlogFileMeta meta;
	protected final ByteOrder order;
	
	public EventBodyDefaultImpl(final byte[] bodyData, final EventHeader eventHeader, final BinlogFileMeta meta) {
		this.eventHeader = eventHeader;
		this.bodyData = bodyData;
		this.meta = meta;
		order = meta.getByteOrder();
	}
	
	@Override
	public byte[] getBodyData() {
		return bodyData;
	}
	
	public EventHeader getEventHeader() {
		return eventHeader;
	}
	
	public int getEventBodyLength() {
		return eventHeader.getEventBodyLength();
	}
	
	public int getBodyDataLength() {
		return getEventBodyLength() - getChecksumLength();
	}
	
	public int getChecksumLength() {
		return meta.getChecksumLength();
	}
	
	public int getChecksumAlg() {
		return meta.getChecksumAlg();
	}
	
	public String getChecksumAlgorithmName() {
		return meta.getChecksumAlgName();
	}
	
	public byte[] getChecksumValue() {
		int length = getChecksumLength();
		if (length > 0) {
			return Arrays.copyOfRange(bodyData, bodyData.length - length, bodyData.length);
		} else {
			return null;
		}
	}
	
	public String getChecksumString() {
		byte[] rawCrc = getChecksumValue();
		if (rawCrc != null) {
			long crc = getUnsignedLong(rawCrc, 0, rawCrc.length);
			return getChecksumAlgorithmName() + ": 0x" + Long.toHexString(crc);
		} else {
			return "";
		}
	}
	
	@Override
	public String toString() {
		return ParserHelper.getFormattedHexString(bodyData, 16);
	}

	@Override
	public EventType getEventType() {
		return eventHeader.getEventType();
	}
	
	protected int getInteger(byte[] bytes,  int from, int to) {
		byte[] raw = Arrays.copyOfRange(bytes, from, to);
		return ParserHelper.getInteger(raw, order);
	}
	
	protected int getInteger(byte[] bytes) {
		return ParserHelper.getInteger(bytes, order);
	}
	
	protected int getUnsignedInteger(byte b) {
		return ParserHelper.getUnsignedInteger(b);
	}
	
	protected int getUnsignedInteger(byte[] bytes, int from, int to) {
		byte[] raw = Arrays.copyOfRange(bytes, from, to);
		return ParserHelper.getUnsignedInteger(raw, order); 
	}
	
	protected int getUnsignedInteger(byte[] bytes) {
		return ParserHelper.getUnsignedInteger(bytes, order); 
	}
	
	protected long getLong(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getLong(raw, order);
	}
	
	protected long getLong(byte[] bytes) {
		return ParserHelper.getLong(bytes, order);
	}
	
	protected long getUnsignedLong(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getUnsignedLong(raw, order);
	}
	
	protected long getUnsignedLong(byte[] bytes) {
		return ParserHelper.getUnsignedLong(bytes, order);
	}
	
	protected float getFloat(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getFloat(raw, order);
	}
	
	protected float getFloat(byte[] bytes) {
		return ParserHelper.getFloat(bytes, order);
	}
	
	public double getDouble(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getDouble(raw, order);
	}
	
	public double getDouble(byte[] bytes) {
		return ParserHelper.getDouble(bytes, order);
	}
	
	protected BitSet getBitSet(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getBitSet(raw, order);
	}
	
	protected BitSet getBitSet(byte[] bytes) {
		return ParserHelper.getBitSet(bytes, order);
	}
	
	protected byte[] getPackedInteger(byte[] bytes, int from) {
		return ParserHelper.getPackedInteger(bytes, from);
	}
	
	protected Object[] getPackedString(byte[] bytes, int from) {
		return ParserHelper.getPackedString(bytes, from, order);
	}
	
	protected String getString(byte[] bytes, int from, int to) {
		byte[] raw = getBytes(bytes, from, to);
		return ParserHelper.getString(raw);
	}
	
	protected String getNulTerminatedString(byte[] bytes, int from, int to) {
		byte[] raw = ParserHelper.getNulTerminatedString(bytes, from, to);
		return getString(raw, 0, raw.length);
	}
	
	
	protected byte[] getBytes(byte[] bytes, int from, int to) {
		return  Arrays.copyOfRange(bytes, from, to);
	}
}
