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

package org.littlestar.mysql.binlog;

import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.littlestar.mysql.binlog.event.BinlogEvent;
import org.littlestar.mysql.binlog.event.EventType;
import org.littlestar.mysql.binlog.event.body.EventBody;
import org.littlestar.mysql.binlog.event.body.impl.GtidLogEventBody;
import org.littlestar.mysql.binlog.event.header.EventHeader;
import org.littlestar.mysql.binlog.parser.BinlogParser;
import org.littlestar.mysql.binlog.parser.BinlogParserBuilder;
import org.littlestar.mysql.binlog.parser.Gtid;

public class BinlogParserApp {
	private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
	private static final String HELP_OPTION_NAME = "help";
	private static final String BYTE_ORDER_OPTION_NAME = "byte-order";
	private static final String CHARACTER_SET_OPTION_NAME = "character-set";
	private static final String DISABLE_STRING_DECODE_OPTION_NAME = "disable-string-decode";
	private static final String START_POSITION_OPTION_NAME = "start-position";
	private static final String STOP_POSITION_OPTION_NAME = "stop-position";
	private static final String START_DATETIME_OPTION_NAME = "start-datetime";
	private static final String STOP_DATETIME_OPTION_NAME = "stop-datetime";
	private static final String START_GTID_OPTION_NAME = "start-gtid";
	private static final String STOP_GTID_OPTION_NAME = "stop-gtid";
	private static final String EVENTS_OPTION_NAME = "events";
	private static final String EVENT_HEADER_ONLY = "event-header-only";
	
	protected static Options options = getOptions();
	public static void main(String[] args) throws Throwable {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cli = parser.parse(options, args);
			start(cli);
		} catch (ParseException e) {
			System.err.println("Binlog parsing failed: " + e.getMessage()+"\n");
			showHelp(options);
		}
	}
	
	public static void start(CommandLine cli) throws Throwable {
		if(cli.hasOption(HELP_OPTION_NAME)) {
			showHelp(options);
			System.exit(0);
		}
		List<String> logfiles = cli.getArgList();
		if (logfiles.size() < 1)
			throw new MissingOptionException("Missing required arguments: log-files.");
		
		ByteOrder order = ByteOrder.nativeOrder();
		if (cli.hasOption(BYTE_ORDER_OPTION_NAME)) {
			String strByteOrder = cli.getOptionValue(BYTE_ORDER_OPTION_NAME).trim().toUpperCase();
			if (strByteOrder.equals("LITTLE_ENDIAN")) {
				order = ByteOrder.LITTLE_ENDIAN;
			} else if (strByteOrder.equals("BIG_ENDIAN")) {
				order = ByteOrder.BIG_ENDIAN;
			}
		}
		
		String charset = "utf8";
		if (cli.hasOption(CHARACTER_SET_OPTION_NAME)) {
			charset = cli.getOptionValue(CHARACTER_SET_OPTION_NAME);
		}

		boolean encodeString = true;
		if (cli.hasOption(DISABLE_STRING_DECODE_OPTION_NAME)) {
			encodeString = false;
		}

		boolean offsetRange = false;
		int startPos = 0;
		if (cli.hasOption(START_POSITION_OPTION_NAME)) {
			offsetRange = true;
			String strStartPos = cli.getOptionValue(START_POSITION_OPTION_NAME);
			try {
				startPos = Integer.parseInt(strStartPos);
			} catch (Throwable e) {
			}
		}

		int stopPos = -1;
		if (cli.hasOption(STOP_POSITION_OPTION_NAME)) {
			offsetRange = true;
			String strStopPos = cli.getOptionValue(STOP_POSITION_OPTION_NAME);
			try {
				stopPos = Integer.parseInt(strStopPos);
			} catch (Throwable e) {
			}
		}
		
		boolean datetimeRange = false;
		long startDatetime = 0;
		if (cli.hasOption(START_DATETIME_OPTION_NAME)) {
			datetimeRange = true;
			String value = cli.getOptionValue(START_DATETIME_OPTION_NAME);
			try {
				startDatetime = sf.parse(value).getTime();
			} catch (Throwable e) {
				throw new Throwable("Invalid --start-datetime option: " + value);
			}
		}
		
		long stopDatetime = 0;
		if (cli.hasOption(STOP_DATETIME_OPTION_NAME)) {
			datetimeRange = true;
			String value = cli.getOptionValue(STOP_DATETIME_OPTION_NAME);
			try {
				stopDatetime = sf.parse(value).getTime();
			} catch (Throwable e) {
				throw new Throwable("Invalid --stop-datetime option: "+value);
			}
		}
		
		boolean gtidRange = false;
		Gtid startGTID = null;
		if (cli.hasOption(START_GTID_OPTION_NAME)) {
			gtidRange = true;
			String value = cli.getOptionValue(START_GTID_OPTION_NAME);
			startGTID = new Gtid(value);
		}
		
		Gtid stopGTID = null;
		if (cli.hasOption(STOP_GTID_OPTION_NAME)) {
			gtidRange = true;
			String value = cli.getOptionValue(STOP_GTID_OPTION_NAME);
			stopGTID = new Gtid(value);
		}
		
		HashSet<EventType> eventFilter = null;
		if (cli.hasOption(EVENTS_OPTION_NAME)) {
			String events = cli.getOptionValue(EVENTS_OPTION_NAME);
			eventFilter = getEventFilter(events);
		}
		
		boolean headerOnly = false;
		if (cli.hasOption(EVENT_HEADER_ONLY)) {
			headerOnly = true;
		}
		
		int rangeFilterCount = trueCount(offsetRange, datetimeRange, gtidRange);
		if (rangeFilterCount > 1) {
			throw new Throwable("The command cant have either --{start|stop}-position or --{start|stop}-datetime or --{start|stop}-gtid at one time");
		}
		
		final String logFile = logfiles.get(0);
		BinlogParserBuilder builder = BinlogParserBuilder.newBuilder(logFile).withByteOrder(order)
				.decodeString(encodeString).withCharSet(charset);
		if (gtidRange) {
			if (eventFilter != null) {
				if (eventFilter.size() > 0) {
					eventFilter.add(EventType.GTID_LOG_EVENT);
					builder.withEventFilter(eventFilter);
				}
			}
			BinlogParser parser = builder.build();
			outputEvents(parser, startGTID, stopGTID, eventFilter, headerOnly);
		} else if (datetimeRange) {
			if (eventFilter != null) {
				if (eventFilter.size() > 0) {
					builder.withEventFilter(eventFilter);
				}
			}
			BinlogParser parser = builder.build();
			outputEvents(parser, startDatetime, stopDatetime, eventFilter, headerOnly);
			
		} else {
			if (eventFilter != null) {
				if (eventFilter.size() > 0) {
					builder.withEventFilter(eventFilter);
				}
			}
			BinlogParser parser = builder.build();
			outputEvents(parser, startPos, stopPos, eventFilter, headerOnly);
		}
	}
	
	private static void outputEvents(BinlogParser parser, Gtid startGTID, Gtid stopGTID,
			HashSet<EventType> eventFilter, boolean headerOnly) throws Throwable {
		long startTid = 0L, stopTid = 0L;

		if (startGTID != null) {
			startTid = startGTID.getTransactionId();
		}

		if (stopGTID != null) {
			stopTid = stopGTID.getTransactionId();
		}

		if (startTid > 0 & stopTid > 0) {
			if (startTid >= stopTid)
				throw new MissingOptionException("start-gtid's transaction-id(" + startTid
						+ ") must less than stop-gtid's transaction-id(" + stopTid + ").");
		}
		boolean startFlag = false;
		while (parser.hasEvent()) {
			BinlogEvent event = parser.nextEvent();
			EventHeader header = event.getHeader();
			EventType et = header.getEventType();
			
			if (startTid > 0 & !startFlag) {
				if (et.equals(EventType.GTID_LOG_EVENT)) {
					GtidLogEventBody gtidLogBody = (GtidLogEventBody) event.getBody();
					long currTid = new Gtid(gtidLogBody.getGtid()).getTransactionId();

					if (currTid >= startTid) {
						startFlag = true;
					}
				}
			}
			if (startFlag) {
				output(event, eventFilter,  headerOnly);
			}
			
			if (stopTid > 0) {
				if (et.equals(EventType.GTID_LOG_EVENT)) {
					GtidLogEventBody gtidLogBody = (GtidLogEventBody) event.getBody();
					long currTid = new Gtid(gtidLogBody.getGtid()).getTransactionId();
					if (currTid >= stopTid) {
						break;
					}
				}
			}
		}
		parser.close();
	}
	
	private static void outputEvents(BinlogParser parser, long startDatetime, long stopDatetime,
			HashSet<EventType> eventFilter, boolean headerOnly) throws Throwable {
		if (startDatetime > 0 & stopDatetime > 0) {
			if (startDatetime >= stopDatetime)
				throw new MissingOptionException("--start-datetime(" + sf.format(new Date(startDatetime))
						+ ") must less than --stop-datetime(" + sf.format(new Date(stopDatetime)) + ").");
		}
		while (parser.hasEvent()) {
			BinlogEvent event = parser.nextEvent();
			EventHeader header = event.getHeader();
			long eventDatetime = header.getTimestamp().getTime();
			if (eventDatetime >= startDatetime) {
				if (stopDatetime > 0 & eventDatetime >= stopDatetime) {
					break;
				}
				output(event, eventFilter, headerOnly);
			}
		}
	}
	
	private static void outputEvents(BinlogParser parser, int startPos, int stopPos,
			HashSet<EventType> eventFilter, boolean headerOnly) throws Throwable {
		
		if (startPos > 0 & stopPos > 0) {
			if (startPos >= stopPos)
				throw new MissingOptionException(
						"--start-position(" + startPos + ") must less than --stop-position(" + stopPos + ").");
		}
		
		while (parser.hasEvent()) {
			BinlogEvent event = parser.nextEvent();
			EventHeader header = event.getHeader();
			long sPos = header.getStartPosition();
			long ePos = header.getNextPosition();
			if (sPos >= startPos) { //当事件的开始偏移大于--start-position, 开始输出；
				if (stopPos > 0 & ePos >= stopPos) { //当事件结束偏移大于--stop-position, 结束输出；
					break;
				}
				output(event, eventFilter, headerOnly);
			}
		}
		parser.close();
	}
	
	private static void output(BinlogEvent event, HashSet<EventType> eventFilter, boolean headerOnly) {
		EventHeader header = event.getHeader();
		if (eventFilter != null) { // 指定了--events，只有在指定列表才输出；
			EventType et = header.getEventType();
			if (eventFilter.contains(et))
				System.out.println(header.getText());
		} else {
			System.out.println(header.getText());
		}
		if (!headerOnly) { //如果指定了值输出事件通用头，将不继续输出；
			EventBody body = event.getBody();
			// 指定了withEventFilter(), 如果事件不再指定--events列表, 将不解析EventBody, BinlogEvent.getBody()返回为null.
			if (body != null) {  
				System.out.println("-------------------------------------------------------------------------------------");
				System.out.println(body.toString());
			}
		}
	}
	
	private static Options getOptions() {
	    Options options = new Options();
	    options.addOption(Option.builder("?").longOpt(HELP_OPTION_NAME).desc("Display this help and exit.").build());
	    options.addOption(Option.builder("b").longOpt(BYTE_ORDER_OPTION_NAME).hasArg().argName("big_endian | little_endian").desc("Specify the byte order of the binlog file, the default is the native byte order of this Java virtual machine is running.").build());
	    options.addOption(Option.builder("c").longOpt(CHARACTER_SET_OPTION_NAME).hasArg().argName("name").desc("Set the default character set, the default is 'utf8'.").build());
	    options.addOption(Option.builder("D").longOpt(DISABLE_STRING_DECODE_OPTION_NAME).desc("Disable decode the string value, instead of output string's hex value. ").build());
	    options.addOption(Option.builder("s").longOpt(START_POSITION_OPTION_NAME).hasArg().argName("#").desc("Start reading the binlog at first event having a offset equal or posterior to the argument.").build());
	    options.addOption(Option.builder("e").longOpt(STOP_POSITION_OPTION_NAME).hasArg().argName("#").desc("Stop reading the binlog at first event having a offset equal or posterior to the argument.").build());
	    options.addOption(Option.builder().longOpt(START_GTID_OPTION_NAME).hasArg().argName("#").desc("Start reading the binlog at first event having a gtid equal or posterior to the argument.").build());
	    options.addOption(Option.builder().longOpt(STOP_GTID_OPTION_NAME).hasArg().argName("#").desc("Stop reading the binlog at first event having a gtid equal or posterior to the argument.").build());
	    options.addOption(Option.builder().longOpt(START_DATETIME_OPTION_NAME).hasArg().argName("#").desc("Start reading the binlog at first event having a datetime equal or posterior to the argument, datetime format accepted is 'YYYY-MM-DD'T'hh:mm:ss', for example: '2004-12-25T11:25:56' (you should probably use quotes for your shell to set it properly).").build());
	    options.addOption(Option.builder().longOpt(STOP_DATETIME_OPTION_NAME).hasArg().argName("#").desc("Stop reading the binlog at first event having a datetime equal or posterior to the argument, datetime format accepted is 'YYYY-MM-DD'T'hh:mm:ss', for example: '2004-12-25T11:25:56' (you should probably use quotes for your shell to set it properly).").build());
	    //options.addOption(Option.builder("d").longOpt(DATABASE_OPTION_NAME).hasArg().argName("name").desc("List entries for just this database only.").build());
	    options.addOption(Option.builder("t").longOpt(EVENTS_OPTION_NAME).hasArg().argName("events").desc("Output only this comma-sparated list of binlog events.").build());
	    options.addOption(Option.builder().longOpt(EVENT_HEADER_ONLY).desc("Output common event header only.").build());
	    return options;
	}
	
	private static HashSet<EventType> getEventFilter(String events) throws MissingOptionException {
		HashSet<EventType> eventFilter = new HashSet<EventType>();
		String[] strEvents = events.split(",");
		for (String strEvent : strEvents) {
			EventType et = EventType.getEventType(strEvent.trim());
			if (et.equals(EventType.UNKNOWN_EVENT)) {
				throw new MissingOptionException("Invalid --events option:" + strEvent + ".");
			}
			eventFilter.add(et);
		}
		return eventFilter;
	}
	
    private static void showHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp("binlogparser.sh [options] log-files", "options: ", options, "");
    }
    
	private static int trueCount(boolean... values) {
		int count = 0;
		for (boolean value : values) {
			if (value)
				count++;
		}
		return count;
	}
}