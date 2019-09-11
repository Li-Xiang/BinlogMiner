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

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.littlestar.helper.LogRecordFormatter;
import org.littlestar.mysql.binlog.miner.BinlogMiner;

public class BinlogMinerApp {
	public static final String CFG_FILE = System.getProperty("user.dir") + File.separator + "miner.xml";
	public static final String LOG_FILE = System.getProperty("user.dir") + File.separator + "miner.log";
	private final static Logger logger = Logger.getLogger(BinlogMinerApp.class.getName());
	
	private static void initLogger() throws SecurityException, IOException {
		LogManager.getLogManager().reset();
		Logger rootLogger = Logger.getLogger("");
		rootLogger.setUseParentHandlers(false);

		FileHandler fileHandler = new FileHandler(LOG_FILE, 2097152, 4, true); //Append, Max 2 MB, 4 files.
		Formatter logFormatter = new LogRecordFormatter();
		fileHandler.setFormatter(logFormatter);
		fileHandler.setLevel(Level.ALL);
		fileHandler.setEncoding("UTF-8");
		rootLogger.addHandler(fileHandler);

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(logFormatter);
		consoleHandler.setLevel(Level.INFO);
		rootLogger.addHandler(consoleHandler);
		
		rootLogger.setLevel(Level.INFO);
	}
	
	private static String usage() {
		StringBuilder usage = new StringBuilder();
		usage.append("MySQL Binlog Miner \n\n")
			.append("SYNOPSIS \n")
			.append("binlogminer [binlog-file1;binlog-file2;...] [little_endian|big_endian]\n\n")
			.append("DESCRIPTION \n")
			.append("You can use comand line arguments or confiugration file to setup binlogminer to analyse MySQL binlogs:\n\n")
			.append("1. Command line mode: \n")
			.append("  Argument 1# Specific the paths of binlog files to be analysis, files split by ';'.\n")
			.append("  Argument 2# (optional) Spfcific the byte order of platform which generate those binlog files, if not specific,\n")
			.append("              using miner native byte order.\n")
			.append("Note: Command line mode not support output to table, and couldn't get column name from database. \n\n")
			.append("2. Configuration file(miner.xml): \n")
			.append("  Configuration file is fixed name with miner.xml in runing directory. Usage reference the 'miner.xml.sample' file.\n");
		return usage.toString();
	}
	
	private static boolean createOutputTable(final Connection connection, String tableName) {
		final String createTableStmt = 
				"create table if not exists "+tableName+" ("+
				"    event_timestamp timestamp,"+
				"    event_type varchar(40),"+
				"    table_schema varchar(64),"+
				"    table_name varchar(64),"+
				"    start_pos int,"+
				"    end_pos int,"+
				"    undo_stmt text(10000),"+
				"    redo_stmt text(10000)"+
				") ";
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(createTableStmt);
			stmt.close();
			return true;
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Create Output table '" + tableName + "' falied!", e);
			return false;
		}
	}
	
	private static boolean truncateOutputTable(final Connection connection, String tableName) {
		//final String truncateTableStmt = "truncate table "+tableName;
		final String deleteTableStmt = "delete from "+tableName;
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(deleteTableStmt);
			stmt.close();
			return true;
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Truncate Output table '" + tableName + "' falied!", e);
			return false;
		}
	}
	
	private static boolean checkReferenceTable(final Connection connection, String tableName) {
		final String checkStmt = "select ordinal_position, column_name from " + tableName + " where 1=2";
		try {
			Statement stmt = connection.createStatement();
			stmt.executeQuery(checkStmt);
			stmt.close();
			return true;
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Check reference table '" + tableName + "' falied!", e);
			return false;
		}
	}
	
	private static double round(double value, int scale) {
		if (scale < 0)
			return value;
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public static void main(String[] args) throws SecurityException, IOException  {
		LinkedHashSet<File> binlogFiles;
		initLogger();
		BinlogMinerConfigBuilder config = null;
		File minerConfig = new File(CFG_FILE);
		// first check arguments, if not, check configuration file.
		if (args.length > 0) {
			logger.info("Runing on arguments mode ...");
			config = new BinlogMinerConfigBuilder(args);
		} else if (minerConfig.exists() & minerConfig.canRead()) {
			logger.info("Runing on configuration file mode ...");
			try {
				config = new BinlogMinerConfigBuilder(minerConfig);
				logger.info(config.toFormattedString());
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Parsing configuration file '" + CFG_FILE + "' failed! ", e);
			}
		} else {
			System.out.println(usage());
			System.exit(-1);
		}
		
		binlogFiles = config.getBinlogFiles();
		if(binlogFiles.size() <1) {
			logger.log(Level.SEVERE, "Binlog File is empty, no thing to be analyse, exit!");
			System.exit(-1);
		}
		
		ByteOrder byteOrder = config.getByteOrder();
		String charset = config.getDefaultCharset();
		boolean withOutputTable = false;
		String outputTableName = config.getOutputTableName();
		Connection outputConnection = config.getOutputConnection();
		boolean truncateOutputTable = config.isTruncateOutputTable();
		
		if(outputConnection != null & outputTableName != null) {
			logger.info("Preparing Binlog Miner output table ...");
			withOutputTable = createOutputTable(outputConnection, outputTableName);
			if(truncateOutputTable) {
				logger.info("Truncate output table ...");
				truncateOutputTable(outputConnection, outputTableName);
			}
		} 
		
		boolean withReferenceTable = false;
		String referenceTableName = config.getReferenceTableName();
		Connection referenceConnection = config.getReferenceConnection();
		if(referenceConnection != null & referenceTableName != null) {
			withReferenceTable = checkReferenceTable(referenceConnection, referenceTableName);
		}
		
		for(File file : binlogFiles) {
			logger.info("Mining Binlog '"+file.toString()+" ... ");
			try {
				BinlogMiner miner = BinlogMiner.newMiner(file.toString(), byteOrder, charset);
				if (withOutputTable) {
					miner.withOutputConnection(outputConnection).withOutputTable(outputTableName);
				}
				if (withReferenceTable) {
					miner.withReferenceConnection(referenceConnection).withReferenceTable(referenceTableName);
				}
				miner.start();
				//
				while (withOutputTable) {
					if (miner != null) {
						long fileSize = miner.getBinlogFileSize();
						long proccessed = miner.getProcessedBytes();
						double s = fileSize;
						double p = proccessed;
						double pct = (p / s) * 100;

						logger.info("Thread: " + miner.getState() + ", " + proccessed + " of " + fileSize + " bytes, "
								+ round(pct, 2) + "% done.");
						if (miner.getState() == State.TERMINATED) {
							logger.info("done.");
							break;
						}
						try {
							Thread.sleep(2000L);
						} catch (Throwable e) {
						}
					} else {
						break;
					}
				}
				//
				
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Miner thread throw exception.", e);
			}
		}
		logger.info("All binlogs has bend analysed, ending ...");
		try {
			outputConnection.close();
		} catch (Throwable e) {
		}
		try {
			referenceConnection.close();
		} catch (Throwable e) {
		}

	}
}
