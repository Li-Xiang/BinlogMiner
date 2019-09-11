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
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.littlestar.helper.ConnectionFactory;
import org.littlestar.helper.XmlHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BinlogMinerConfigBuilder {
	private File minerConfig;
	private final static Logger logger = Logger.getLogger(BinlogMinerConfigBuilder.class.getName());
	private static final String ELEMENT_TAG_BYTEORDER = "ByteOrder";
	private static final String ELEMENT_TAG_DEFAULTCHARSET = "DefaultCharset";
	private static final String ELEMENT_TAG_BINLOG = "Binlogs";
	private static final String ELEMENT_TAG_FILE = "File";
	private static final String ELEMENT_TAG_OUTPUTTABLE = "OutputTable";
	private static final String ELEMENT_ATTR_TRUNCATE = "truncate";

	private static final String ELEMENT_TAG_REFTABLE = "ReferenceTable";
	private static final String ELEMENT_ATTR_DSID = "dsid";

	private LinkedHashSet<File> binlogFileSet = new LinkedHashSet<File>();
	private String defaultCharset = null;
	private ByteOrder byteOrder = null;
	private Connection outpuConnection = null;
	private String outputTableName = null;
	private String outputTableDsid = null;
	private boolean outputTruncateFlag = false;

	private Connection referenceConnection = null;
	private String referenceTableName = null;
	private String referenceDsid = null;
	
	private void parseByteOrder(NodeList byteOrderNodeList) {
		String strByteOrder = null;
		if (byteOrderNodeList.getLength() > 0) {
			Node byteOrderNode = byteOrderNodeList.item(0);
			strByteOrder = byteOrderNode.getTextContent().trim();
		}
		byteOrder = toByteOrder(strByteOrder);
	}

	private static ByteOrder toByteOrder(String str) {
		ByteOrder order = ByteOrder.nativeOrder();
		if (str != null) {
			str = str.toUpperCase().trim();
			if (str.equals("BIG_ENDIAN")) {
				order = ByteOrder.BIG_ENDIAN;
			} else if (str.equals("LITTLE_ENDIAN")) {
				order = ByteOrder.LITTLE_ENDIAN;
			} else {
				logger.warning("Unknown byte order '"+str+", using native '"+order.toString()+"'");
			}
		}
		return order;
	}
	
	private void parseDefaultCharset(NodeList defaultCharsetNodeList) {
		if(defaultCharsetNodeList.getLength() > 0) {
			Node charsetNode = defaultCharsetNodeList.item(0);
			defaultCharset = charsetNode.getTextContent().trim();
		}
		
	}
	private void parseBinlogFiles(String str) {
		if (str != null) {
			String[] files = str.split(";");
			for (String f : files) {
				File file = new File(f.trim());
				if (file.canRead()) {
					binlogFileSet.add(file);
				} else {
					logger.warning(f + "' file not accessible, ignore it.");
				}
			}
		}
	}

	private void parseBinlogFiles(NodeList binlogsNodeList) {
		if (binlogsNodeList.getLength() > 0) {
			Node binlogNode = binlogsNodeList.item(0);
			NodeList children = binlogNode.getChildNodes();
			for (int c = 0; c < children.getLength(); c++) {
				Node child = children.item(c);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String childName = child.getNodeName();
					String childValue = child.getTextContent();
					if (childName.equalsIgnoreCase(ELEMENT_TAG_FILE)) {
						File file = new File(childValue.trim());
						if (file.exists()) {
							binlogFileSet.add(file);
						} else {
							logger.warning(childValue + "' not exists, ignore it.");
						}
					}
				}
			}
		}
	}

	private void parseOutputTable(NodeList outputNodeList) {
		if (outputNodeList.getLength() > 0) {
			Node refTableNode = outputNodeList.item(0);
			outputTableName = refTableNode.getTextContent().trim();
			if (refTableNode.hasAttributes()) {
				NamedNodeMap nm = refTableNode.getAttributes();
				Node dsidNode = nm.getNamedItem(ELEMENT_ATTR_DSID);
				if (dsidNode != null)
					outputTableDsid = dsidNode.getNodeValue().trim();
				Node truncateNode = nm.getNamedItem(ELEMENT_ATTR_TRUNCATE);
				if (truncateNode != null)
					outputTruncateFlag = Boolean.parseBoolean(truncateNode.getNodeValue().trim());
			}
			if (outputTableDsid != null) {
				try {
					outpuConnection = ConnectionFactory.newFactory(minerConfig, outputTableDsid).build();
				} catch (Throwable e) {
					logger.log(Level.WARNING,
							"Get miner output table 's datasource failed, id='" + outputTableDsid + "'.", e);
				}
			}
		}
	}

	private void parseReferenceTable(NodeList referenceNodeList) {
		if (referenceNodeList.getLength() > 0) {
			Node refTableNode = referenceNodeList.item(0);
			referenceTableName = refTableNode.getTextContent().trim();
			if (refTableNode.hasAttributes()) {
				NamedNodeMap nm = refTableNode.getAttributes();
				Node dsidNode = nm.getNamedItem(ELEMENT_ATTR_DSID);
				if (dsidNode != null)
					referenceDsid = dsidNode.getNodeValue().trim();
			}
			if (referenceDsid != null) {
				try {
					referenceConnection = ConnectionFactory.newFactory(minerConfig, referenceDsid).build();
				} catch (Throwable e) {
					logger.log(Level.WARNING, "Get miner referece datasource failed, id='" + referenceDsid + "'.", e);
				}
			}
		}
	}



	public BinlogMinerConfigBuilder(String[] args) {
		int len = args.length;
		if (len > 0) {
			parseBinlogFiles(args[0]);
			if(len > 1) {
				byteOrder = toByteOrder(args[1]);
			}
		}
	}

	public BinlogMinerConfigBuilder(File cfgFile) throws Throwable {
		minerConfig = cfgFile;
		if (minerConfig.exists()) {
			Document doc = XmlHelper.getDocument(minerConfig);
			Element root = doc.getDocumentElement();

			NodeList binlogsNodeList = root.getElementsByTagName(ELEMENT_TAG_BINLOG);
			parseBinlogFiles(binlogsNodeList);
			
			NodeList defaultCharsetNodeList = root.getElementsByTagName(ELEMENT_TAG_DEFAULTCHARSET);
			parseDefaultCharset(defaultCharsetNodeList);
			
			NodeList byteOrderNodeList = root.getElementsByTagName(ELEMENT_TAG_BYTEORDER);
			parseByteOrder(byteOrderNodeList);

			NodeList outputTableNodeList = root.getElementsByTagName(ELEMENT_TAG_OUTPUTTABLE);
			parseOutputTable(outputTableNodeList);

			NodeList referenceTableNodeList = root.getElementsByTagName(ELEMENT_TAG_REFTABLE);
			parseReferenceTable(referenceTableNodeList);
		}
	}

	public LinkedHashSet<File> getBinlogFiles() {
		return binlogFileSet;
	}

	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}
	
	public Connection getOutputConnection() {
		return outpuConnection;
	}

	public String getOutputTableName() {
		return outputTableName;
	}

	public boolean isTruncateOutputTable() {
		return outputTruncateFlag;
	}

	public Connection getReferenceConnection() {
		return referenceConnection;
	}

	public String getReferenceTableName() {
		return referenceTableName;
	}

	private static String getString(Object object) {
		String val = "null";
		if (object != null)
			val = object.toString();
		return val;
	}

	public String toFormattedString() throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append("\n--------------  Configurations --------------\n")
			.append("  Binlogs:\n");
		for (File f : getBinlogFiles()) {
			builder.append("  " + f.toString()).append("\n");
		}
		builder.append("  Byte Order: " + getByteOrder()).append("\n")
				.append("  Default Charset: " + getDefaultCharset()).append("\n")
				.append("  Output Table: ").append(getString(getOutputTableName())).append("\n")
				.append("  Output Connection: ").append(getString(getOutputConnection())).append("\n")
				.append("  Truncate Output table: ").append(getString(isTruncateOutputTable())).append("\n")
				.append("  Reference Table: ").append(getString(getReferenceTableName())).append("\n")
				.append("  Reference Connection: ").append(getString(getReferenceConnection())).append("\n");
		return builder.toString();
	}
}
