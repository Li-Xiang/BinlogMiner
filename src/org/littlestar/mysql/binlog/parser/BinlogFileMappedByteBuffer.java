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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinlogFileMappedByteBuffer implements Closeable {
	private final static Logger logger = Logger.getLogger(BinlogFileMappedByteBuffer.class.getName());
	private static final MapMode READ_ONLY_MODE = MapMode.READ_ONLY;
	private final Object lock = new Object();
	private final String binlogFileName;
	private final RandomAccessFile binlogFile;
	private final FileChannel binlogFileChannel;
	private MappedByteBuffer blogFileBuffer;
	private final ByteOrder order;
	
	/*
	 * max_binlog_size option's maximum value is 1GB.
	 */
	public BinlogFileMappedByteBuffer(final String binlogFileName, final ByteOrder order) throws IOException {
		this.binlogFileName = binlogFileName;
		this.order = order;
		binlogFile = new RandomAccessFile(binlogFileName, "r");
		binlogFileChannel = binlogFile.getChannel();
		blogFileBuffer = binlogFileChannel.map(READ_ONLY_MODE, 0, binlogFile.length());
	}
	
	@Override
	public void close() throws IOException {
		binlogFileChannel.close();
		binlogFile.close();
	}
	
	public String getFileName() {
		return binlogFileName;
	}
	
	public ByteOrder getByteOrder() {
		return order;
	}
	
	public void remap(long position, long size) throws IOException {
		synchronized(lock) {
			int pos = blogFileBuffer.position(); 
			blogFileBuffer = binlogFileChannel.map(READ_ONLY_MODE, position, size);
			blogFileBuffer.position(pos);
		}
	}
	
	public int getPosition() {
		synchronized(lock) {
			return blogFileBuffer.position();
		}
	}
	
	public void setPosition(int newPosition) {
		synchronized(lock) {
			blogFileBuffer.position(newPosition);
		}
	}
	/*
	public int remaining() {
		synchronized (lock) {
			return blogFileBuffer.remaining();
		}
	}
	*/
	public int remaining(boolean withCheck) {
		synchronized (lock) {
			try {
				// For current writing binlog file;
				long fileSize = binlogFileChannel.size();
				if (fileSize > blogFileBuffer.limit()) {
					remap(0, fileSize);
				}
			} catch (Throwable e) {
				logger.log(Level.WARNING, "", e);
			}
			return blogFileBuffer.remaining();
		}
	}
	
	public void skip(int length) {
		synchronized (lock) {
			int pos = blogFileBuffer.position();
			blogFileBuffer.position(pos + length);
		}
	}
	
	public byte[] getBytes(int length) {
		byte[] raw = new byte[length];
		synchronized (lock) {
			blogFileBuffer.get(raw);
		}
		return raw;
	}
	
	public byte getByte() {
		synchronized (lock) {
			return blogFileBuffer.get();
		}
	}
	
	public int getInteger(int length) {
		byte[] raw = getBytes(length);
		int retVal = ParserHelper.getInteger(raw, order);
		return retVal;
	}
	
	public int getUnsignedInteger(int length) {
		byte[] raw = getBytes(length);
		int retVal = ParserHelper.getUnsignedInteger(raw, order);
		return retVal;
	}
	
	public long getLong(int length) {
		byte[] raw = getBytes(length);
		long retVal = ParserHelper.getLong(raw, order);
		return retVal;
	}
	
	public long getUnsignedLong(int length) {
		byte[] raw = getBytes(length);
		long retVal = ParserHelper.getUnsignedLong(raw, order);
		return retVal;
	}
	
	public String getString(int length) {
		byte[] raw = getBytes(length);
		String retVal = ParserHelper.getString(raw);
		return retVal;
	}
}
