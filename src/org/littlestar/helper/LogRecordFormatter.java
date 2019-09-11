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

package org.littlestar.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogRecordFormatter extends Formatter {
	
	@Override
	public String format(LogRecord record) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		StringBuilder formatted = new StringBuilder();
		formatted
			.append("[").append(format.format(new Date(record.getMillis()))).append("] ")
			.append("[").append(String.format("%-7s", record.getLevel())).append("]")
			//.append(record.getSourceClassName()).append(".").append(record.getSourceMethodName())
			.append(": ").append(record.getMessage()).append("\n");

		if (record.getThrown() != null) {
			StringWriter writer = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(writer));
			formatted.append(writer);
		}
		return formatted.toString();
	}
}
