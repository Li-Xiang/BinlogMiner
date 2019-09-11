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

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConnectionFactory {
	public static final String DEFAULT_DATASOURCE_ID = "$default-datasource-id$";
	public static final String USER_DIR = System.getProperty("user.dir");
	/*
	 * MySQL Connector/J:
	 *   
	 */
	public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	/*
	 * Oracle Thin Driver:
	 *   URL Format: 
	 *     jdbc:oracle:thin:@//<host>[:<port>]/<serviceName>
	 *     jdbc:oracle:thin:@<host>[:<port>]:<SID>
	 *     jdbc:oracle:thin:@<tnsname> --> Need Oracle DB Client and $ORACLE_HOME env.
	 */
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	/*
	 * SQLite Driver: https://github.com/xerial/sqlite-jdbc
	 *   URL Format: jdbc:derby:/data/derby/demodb
	 */
	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	/*
	 * Derby Driver:
	 *   EmbeddedDriver need derby.jar;     
	 *     URL Format: jdbc:derby:/data/derby/demodb
	 *   ClientDriver need derbyclient.jar; 
	 *     URL Format: jdbc:derby://127.0.0.1/data/derby/demodb
	 */
	public static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; 
	public static final String DERBY_CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";     
	
	private String driver;
	private String url;
	private final Properties connectionPropertis;

	private ConnectionFactory() {
		connectionPropertis = new Properties();
	}
	
	private static final String ELEMENT_TAG_DATASOURCE    = "DataSource";
	private static final String ELEMENT_ATTR_DATASOURCE_ID = "id";
	private static final String ELEMENT_TAG_DRIVER = "Driver";
	private static final String ELEMENT_TAG_URL = "Url";
	private static final String ELEMENT_TAG_USER = "User";
	private static final String ELEMENT_TAG_PASSWORD = "Password";
	private static final String ELEMENT_TAG_PROPERTIES = "Properties";
	
	public static ConnectionFactory newFactory(File cfgFile, String dataSourceId) throws Throwable {
		HashMap<String, Properties> dsConfig = getDataSourceConfig(cfgFile);
		Properties dsProp = null;
		if (dsConfig.size() > 0) {
			if(dataSourceId == null) {
				//Map.Entry<String, Properties> entry = dsConfig.entrySet().iterator().next();
				//dsProp = entry.getValue();
				dsProp = dsConfig.get(DEFAULT_DATASOURCE_ID);
			} else {
				dsProp = dsConfig.get(dataSourceId);
			}
		}
		
		if (dsProp == null)
			throw new SQLException("Datasource Configuration: id='" + dataSourceId + "' not found!");
		
		String driver = dsProp.getProperty(ELEMENT_TAG_DRIVER);
		String url = dsProp.getProperty(ELEMENT_TAG_URL);
		String user = dsProp.getProperty(ELEMENT_TAG_USER);
		String password = dsProp.getProperty(ELEMENT_TAG_PASSWORD);
		String properties = dsProp.getProperty(ELEMENT_TAG_PROPERTIES);
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setDriver(driver);
		factory.setUrl(url);
		factory.setUser(user);
		factory.setPassword(password);
		factory.setConnectionProperties(properties);
		return factory;
	}
	
	public static ConnectionFactory newFactory(final File cfgFile) throws Throwable {
		return newFactory(cfgFile, null);
	}
	
	public static String getDBMS(String driver) {
		driver = driver.trim();
		if (driver.startsWith("com.mysql")) {
			return "mysql";
		} else if (driver.startsWith("oracle.jdbc")) {
			return "oracle";
		}
		return "";
	}
	
	private static HashMap<String, Properties> getDataSourceConfig(File cfgFile)
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, Properties> dsConfig = new HashMap<String, Properties>();
		Document doc = XmlHelper.getDocument(cfgFile);
		Element root = doc.getDocumentElement();
		NodeList dataSourceList = root.getElementsByTagName(ELEMENT_TAG_DATASOURCE);
		for (int i = 0; i < dataSourceList.getLength(); i++) {
			Node dsNode = dataSourceList.item(i);
			//Fetch datasource id;
			String dsId = DEFAULT_DATASOURCE_ID;
			if (dsNode.hasAttributes()) {
				NamedNodeMap nm = dsNode.getAttributes();
				Node dsIdNode = nm.getNamedItem(ELEMENT_ATTR_DATASOURCE_ID);
				if (dsIdNode != null) {
					dsId = dsIdNode.getNodeValue();
				}
			}
			//Fetch datasource element's children.
			if (dsNode.hasChildNodes()) {
				Properties dsProp = new Properties();
				NodeList dsChildren = dsNode.getChildNodes();
				for (int c = 0; c < dsChildren.getLength(); c++) {
					Node dsChild = dsChildren.item(c);
					if (dsChild.getNodeType() == Node.ELEMENT_NODE) {
						String dsChildName = dsChild.getNodeName();
						String dsChildValue = dsChild.getTextContent().trim();
						dsProp.put(dsChildName, dsChildValue);
					}
				}
				dsConfig.put(dsId, dsProp);
			}
		}
		return dsConfig;
	}
	
	public static ConnectionFactory newFactory() {
		return new ConnectionFactory();
	}

	public ConnectionFactory setDriver(String driver) {
		this.driver = driver;
		return this;
	}
	
	public ConnectionFactory setUrl(String url) {
		this.url = url;
		return this;
	}
	
	public ConnectionFactory setUser(final String userName) {
		if (userName != null)
			connectionPropertis.put("user", userName);
		return this;
	}

	public ConnectionFactory setPassword(final String password) {
		if (password != null)
			connectionPropertis.put("password", password);
		return this;
	}
	
	public ConnectionFactory setConnectionProperty(final String key, final String value) {
		connectionPropertis.put(key, value);
		return this;
	}
	
	public ConnectionFactory setConnectionProperties(final String properties) {
		if (properties != null) {
			final String[] entries = properties.split(";");
			for (final String entry : entries) {
				if (entry.length() > 0) {
					final int index = entry.indexOf('=');
					if (index > 0) {
						final String name = entry.substring(0, index);
						final String value = entry.substring(index + 1);
						setConnectionProperty(name, value);
					} else {
						setConnectionProperty(entry, "");
					}
				}
			}
		}
		return this;
	}
	
	/* Sets the maximum time in seconds that a driver will wait while attempting to connect to a database.
	public ConnectionFactory setLoginTimeout(int seconds) {
		if (seconds >= 0)
			DriverManager.setLoginTimeout(seconds);
		return this;
	}*/
	
	public ConnectionFactory withMySqlProperties() {
		connectionPropertis.put("useSSL", "true");
		connectionPropertis.put("useUnicode", "true");
		connectionPropertis.put("useOldAliasMetadataBehavior", "true");
		connectionPropertis.put("rewriteBatchedStatements", "true");
		connectionPropertis.put("defaultFetchSize", "100");
		connectionPropertis.put("serverTimezone", "UTC");
		return this;
	}
	
	public ConnectionFactory withSQLiteProperties() {
		connectionPropertis.put("journal_mode", "WAL");
		connectionPropertis.put("wal_autocheckpoint", "6000");
		connectionPropertis.put("synchronous", "NORMAL");
		connectionPropertis.put("cache_ssize", "8000");
		return this;
	}
	
	/*
	 * To use Oracle DB client(tnsname.ora), need oracle.net.tns_admin property.
	 * this function set oracle.net.tns_admin by $ORACLE_HOME environment.
	 */
	public ConnectionFactory withOracleProperties() {
		String oracleHome = System.getenv("ORACLE_HOME");
		String tnsAdmin = oracleHome + File.separator + "network" + File.separator + "admin";
		System.setProperty("oracle.net.tns_admin", tnsAdmin);
		return this;
	}
	
	public Connection build()
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (driver == null ) {
			throw new SQLException("JDBC driver is not set");
		}
		if(driver.trim().equals(ORACLE_DRIVER)) {
			withOracleProperties();
		}
		Class.forName(driver);
		if (url == null )
			throw new SQLException("JDBC url is not set.");
		/*
		System.out.println("driver: "+driver);
		System.out.println("url: "+url);
		Set<String> keys = connectionPropertis.stringPropertyNames();
	    for (String key : keys) {
	      System.out.println(key + ": " + connectionPropertis.getProperty(key));
	    }
	    System.out.println();
	   	*/
		return DriverManager.getConnection(url, connectionPropertis);
	}
	
	/*demo
	public static void main(String[] args)
			throws Throwable {
		Connection connection;
		File dsConfig = new File(USER_DIR+File.separator+"ds.xml");
		connection = ConnectionFactory.newFactory(dsConfig, "mysql-demo").build();
		connection = ConnectionFactory.newFactory(dsConfig).build();
		connection = ConnectionFactory.newFactory(dsConfig, "sqlite-demo").build();
		connection = ConnectionFactory.newFactory(dsConfig, "derby-embedded-demo").build();
		connection = ConnectionFactory.newFactory()
				.setDriver(MYSQL_DRIVER)
				.setUrl("jdbc:mysql://127.0.0.1/mysql")
				.setUser("root")
				.setPassword("Passw0rd")
				.withMySqlProperties()
				.build();
		connection = ConnectionFactory.newFactory()
				.setDriver(ORACLE_DRIVER)
				.setUrl("jdbc:oracle:thin:@//127.0.0.1:1521/db112")
				.setUser("sys")
				.setPassword("oracle")
				.setConnectionProperty("internal_logon", "sysdba")
				.build();
		connection = ConnectionFactory.newFactory()
				.setDriver(SQLITE_DRIVER)
				.setUrl("jdbc:sqlite:/home/think/eclipse-workspace/BinlogMiner/data.db")
				.withSQLiteProperties()
				.build();
		connection = ConnectionFactory.newFactory()
				.setDriver(DERBY_EMBEDDED_DRIVER)
				.setUrl("jdbc:derby:/data/derby/demodb")
				.build();
		connection.close();
	}*/
}

