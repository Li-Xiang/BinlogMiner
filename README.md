## BinlogMiner
BinlogMiner is a Open Source parser Library and Tools for MySQL binlog, Which can easy to decode MySQL binlog event's 
contents and flashback your table/database by generated executable UNDO/REDO statements. 

*README.zh.md FOR Chinses*

### Key features

- Support all binlog format {STATEMENT|ROW|MIXED}; (but only the row based format can be generate undo statements.)<br/>
- Support and Tested from MySQL 3.9 to 8.0;
- Support binlog v1, v3, v4;
- Support extract UNDO/REDO data into table, can query the data you interested by using standard SQL.
- Based on Java development, without installation, multi-OS support, provides easy-to-use Java AIPs.

### Build/Install
There are 2 ways to Install BinlogMiner.

1#. dowload  from url (https://github.com/Li-Xiang/BinlogMiner/tree/master/release) and extract it.
```shell
$ tar -xzvf xxx.tar.gz
```
or

2#. get the source code and build yourself by apache ant(https://ant.apache.org/). 
```shell
$ git clone https://github.com/Li-Xiang/BinlogMiner.git
$ cd BinlogMiner
$ ant clean
$ ant
```
<pre>
Note
----
1. If you need to output data to table, your need to download JDBC driver jar and put it into ./lib directory.
MySQL : https://dev.mysql.com/downloads/connector/j/
SQLite: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/
Derby/Java DB's Embedded: $JDK_HOME/db/lib/derby.jar 
2. Need create and insert privileges to output table database user, need select privilege to reference table (like information_schema.columns) to construct executable SQL statements. 
</pre>

### Usage
You can use command line arguments or configuration file to setup BinlogMiner to analyze MySQL's binlogs:
- Command line mode
<pre>
$ binlogminer.sh [binlog-file1; binlog-file2; ...] [little_endian | big_endian] 
  Argument 1#  the paths of binlog files to be analysis, files split by ';'.
  Argument 2# (optional) the byte order of binlog files, if not specific, using native byte order.

Note: Command line mode not support output to table, and couldn't get column name from database.
</pre>
- Using configuration file(miner.xml)
<pre>
Configuration file is fixed name with miner.xml in BinlogMiner running directory. 
Usage reference the 'miner.xml.demo' file.
</pre>
### Limit & Frequently Questions
- MySQL binlog default permissions mode is 640, if run within mysql server owner or owner group, need to check you has read files privileges.
```shell
# chmod +r /path_to_binlog_files
```
- Only row-base format (binlog_format=ROW) contains undo data(before-row-image), so only row-base format binlog has undo statements.

- MySQL binlog not contain column's name and character set name until MySQL8.0.1+ and binlog_row_metadata=FULL(TABLE_MAP event 's OptionalMetaData). So, BinlogMiner need to get them from source database 's 'information_schema.columns' table to construct executable SQL statements. if MySQL8.0.1- and without 'information_schema.columns' reference, BinlogMiner construct pseudo-SQL, column like '@1', and all string columns decode with default character set name, if not specific using utf8.

- All DDL logging in binlog's QUERY_EVENT, QUERY_EVENT couldn't support construct undo statements yet, you need to undo it yourself.

- Support MySQL character set is below:
<pre>
    MySQL    |   Java
    ---------+------------
    "gbk"    | "gbk"
    "latin1" | "ISO_8859_1"
    "gb2312" | "gb2312"
    "ucs2"   | "UTF_16"
You can extend character set by edit: org.littlestar.mysql.binlog.parser.ParserHelper.getCharset() method, and rebuild.
</pre>

- The platform and bye order table
<pre>
PLATFORM_NAME                       ENDIAN_FORMAT
----------------------------------- --------------
HP Open VMS                         Little
Solaris Operating System (x86-64)   Little
HP IA Open VMS                      Little
Solaris Operating System (x86)      Little
Microsoft Windows x86 64-bit        Little
Linux x86 64-bit                    Little
Microsoft Windows IA (64-bit)       Little
Apple Mac OS (x86-64)               Little
Microsoft Windows IA (32-bit)       Little
Linux IA (32-bit)                   Little
HP Tru64 UNIX                       Little
Linux IA (64-bit)                   Little
HP-UX IA (64-bit)                   Big
Apple Mac OS                        Big
HP-UX (64-bit)                      Big
AIX-Based Systems (64-bit)          Big
IBM Power Based Linux               Big
IBM zSeries Based Linux             Big
Solaris[tm] OE (64-bit)             Big
Solaris[tm] OE (32-bit)             Big
</pre>


### Use Case
Prepared Table for test.
```sql
CREATE TABLE `tpcc`.`employees` (
  `EMPLOYEE_ID` decimal(6,0) NOT NULL,
  `FIRST_NAME` varchar(20) CHARACTER SET utf8,
  `LAST_NAME` varchar(25) CHARACTER SET gbk,
  `EMAIL` varchar(25) DEFAULT '', 
  `PHONE_NUMBER` varchar(20),
  `HIRE_DATE` datetime,
  `JOB_ID` varchar(10) ,
  `SALARY` decimal(8,2) ,
  `COMMISSION_PCT` decimal(2,2),
  `MANAGER_ID` decimal(6,0) ,
  `DEPARTMENT_ID` integer,
  PRIMARY KEY (`EMPLOYEE_ID`)
)  DEFAULT CHARSET=latin1;

INSERT INTO `tpcc`.`employees` VALUES (100,'张','三','SKING','515.123.4567','1987-06-17 00:00:00','AD_PRES',24000.00,NULL,NULL,90);
INSERT INTO `tpcc`.`employees` VALUES (101,'Neena','Kochhar','NKOCHHAR','515.123.4568','1989-09-21 00:00:00','AD_VP',17000.00,NULL,100,90);
INSERT INTO `tpcc`.`employees` VALUES (102,'李','四','LDEHAAN','515.123.4569','1993-01-13 00:00:00','AD_VP',17000.00,NULL,100,90);
INSERT INTO `tpcc`.`employees` VALUES (103,'Alexander','Hunold','AHUNOLD','590.423.4567','1990-01-03 00:00:00','IT_PROG',9000.00,NULL,102,60);
INSERT INTO `tpcc`.`employees` VALUES (104,'王','五','BERNST','590.423.4568','1991-05-21 00:00:00','IT_PROG',6000.00,NULL,103,60);
INSERT INTO `tpcc`.`employees` VALUES (105,'David','Austin','DAUSTIN','590.423.4569','1997-06-25 00:00:00','IT_PROG',4800.00,NULL,103,60);
```

#### 1. Flashback table using configuration file.

Execute delete, update, insert, undo them ...
```sql
delete from tpcc.employees where job_id='AD_VP'; 
update `tpcc`.`employees` set commission_pct=0.30 where employee_id=104;
insert into `tpcc`.`employees` values (156,'Janette','King','JKING','011.44.1345.429268','1996-01-30 00:00:00','SA_REP',10000.00,0.35,146,80);
```
step 1# setup miner env.
```shell
$ cd /data/mysql/5.7.18/binlog/
$ chmod +r binlog.*

$ cd {binlogminer's directory}
$ vi miner.xml
```
```xml
<Configuration>
	<ByteOrder>little_endian</ByteOrder>
	<DefaultCharset>utf8</DefaultCharset>
	<Binlogs>
		<!--1. Add binlog files here -->
		<File>/data/mysql/5.7.18/binlog/blog.000016</File> 
		<File>/data/mysql/5.7.18/binlog/blog.000017</File>
	</Binlogs>
	<!--2. output to table tpcc.binlogminer -->
	<OutputTable dsid="source-database" truncate="true">tpcc.binlogminer</OutputTable>
	<ReferenceTable dsid="source-database">information_schema.columns</ReferenceTable>
	<!--3. setup source datasource to get column name and charset -->
	<DataSource id="source-database">
		<Driver>com.mysql.cj.jdbc.Driver</Driver>
		<Url>jdbc:mysql://127.0.0.1/information_schema</Url>
		<User>root</User>
		<Password>Passw0rd</Password>
		<Properties>useSSL=true;useUnicode=true;characterEncoding=UTF-8;rewriteBatchedStatements=true</Properties>
	</DataSource>
</Configuration>
```
step 2# run binlogminer.sh
```shell
$ ./binlogminer.sh
```
step 3# query output and get undo statements.
```sql
--> undo delete.
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'DELETE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;


insert into tpcc.employees (EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, HIRE_DATE, JOB_ID, SALARY, COMMISSION_PCT, MANAGER_ID, DEPARTMENT_ID) values (101, 'Neena', 'Kochhar', 'NKOCHHAR', '515.123.4568', '1989-09-21 00:00:00', 'AD_VP', 17000.00, null, 100, 90);

insert into tpcc.employees (EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, HIRE_DATE, JOB_ID, SALARY, COMMISSION_PCT, MANAGER_ID, DEPARTMENT_ID) values (102, '李', '四', 'LDEHAAN', '515.123.4569', '1993-01-13 00:00:00', 'AD_VP', 17000.00, null, 100, 90);

--> undo update
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'UPDATE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;
...

--> undo insert ...
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'WRITE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;
...
```

step 4# review the undo statements and execute them to flashback as you need;
<pre>
example:
mysql -uxxx -pxxx < flashback.sql
</pre>

#### 2. command line mode
<pre>

$ ./binlogminer.sh /data/mysql/5.7.18/binlog/blog.000016 little_endian
...
2019-09-05 10:52:30 QUERY_EVENT . start-pos: 259 end-pos: 327
REDO# BEGIN
UNDO# 
2019-09-05 10:52:30 DELETE_ROWS_EVENT tpcc.employees start-pos: 409 end-pos: 563
REDO# delete from tpcc.employees where @0=101 and @1='Neena' and @2='Kochhar' and @3='NKOCHHAR' and @4='515.123.4568' and @5='1989-09-21 00:00:00' and @6='AD_VP' and @7=17000.00 and @8=null and @9=100 and @10=90
UNDO# insert into tpcc.employees (@0, @1, @2, @3, @4, @5, @6, @7, @8, @9, @10) values (101, 'Neena', 'Kochhar', 'NKOCHHAR', '515.123.4568', '1989-09-21 00:00:00', 'AD_VP', 17000.00, null, 100, 90)
REDO# delete from tpcc.employees where @0=102 and @1='李' and @2='��' and @3='LDEHAAN' and @4='515.123.4569' and @5='1993-01-13 00:00:00' and @6='AD_VP' and @7=17000.00 and @8=null and @9=100 and @10=90
UNDO# insert into tpcc.employees (@0, @1, @2, @3, @4, @5, @6, @7, @8, @9, @10) values (102, '李', '��', 'LDEHAAN', '515.123.4569', '1993-01-13 00:00:00', 'AD_VP', 17000.00, null, 100, 90)
2019-09-05 10:52:42 QUERY_EVENT . start-pos: 659 end-pos: 727
REDO# BEGIN
UNDO# 
2019-09-05 10:52:42 UPDATE_ROWS_EVENT tpcc.employees start-pos: 809 end-pos: 960
REDO# update tpcc.employees set @8=0.30 where @0=104 and @1='王' and @2='��' and @3='BERNST' and @4='590.423.4568' and @5='1991-05-21 00:00:00' and @6='IT_PROG' and @7=6000.00 and @8=null and @9=103 and @10=60
UNDO# update tpcc.employees set @8=null where @0=104 and @1='王' and @2='��' and @3='BERNST' and @4='590.423.4568' and @5='1991-05-21 00:00:00' and @6='IT_PROG' and @7=6000.00 and @8=0.30 and @9=103 and @10=60
2019-09-05 10:53:09 QUERY_EVENT . start-pos: 1056 end-pos: 1124
REDO# BEGIN
UNDO# 
2019-09-05 10:53:09 WRITE_ROWS_EVENT tpcc.employees start-pos: 1206 end-pos: 1309
REDO# insert into tpcc.employees (@0, @1, @2, @3, @4, @5, @6, @7, @8, @9, @10) values (156, 'Janette', 'King', 'JKING', '011.44.1345.429268', '1996-01-30 00:00:00', 'SA_REP', 10000.00, 0.35, 146, 80)
UNDO# delete from tpcc.employees where @0=156 and @1='Janette' and @2='King' and @3='JKING' and @4='011.44.1345.429268' and @5='1996-01-30 00:00:00' and @6='SA_REP' and @7=10000.00 and @8=0.35 and @9=146 and @10=80
...

Note: Command line mode not support get column name from database, so output statements not column name, and couldn't get column charset from database, so, using default('utf-8') to decode string.
</pre>

#### 3. offline mode
You can copy binlogs to other machine to analyze them.
example, MySQL server run on Linux, analyze on Windows, and output to SQLite database.

1. export source MySQL server's information_schema.columns to SQLite, example:

create reference table 'ref_columns' in SQLite database.

```sql
CREATE TABLE ref_columns (
    table_schema  VARCHAR (64)  NOT NULL DEFAULT '',
    table_name VARCHAR (64)  NOT NULL DEFAULT '',
    column_name  VARCHAR (64)  NOT NULL DEFAULT '',
    ordinal_position   [UNSIGNED BIG INT] (21) NOT NULL DEFAULT '0',
    character_set_name VARCHAR (32)  DEFAULT NULL
);
```
export information_schema.columns by 'MySQL WorkBench', export to sql file.
```sql
select table_schema, table_name, column_name, ordinal_position, character_set_name from information_schema.columns where table_schema='tpcc';

```
<pre>
In Result Grid window -> 'Export' -> 'FORMAT:' select 'SQL INSERT statements'. 

load ouput data to SQLite database.
</pre>
2. edit miner.xml
```xml
<Configuration>
	<!-- little_endian or big_endian -->
	<ByteOrder>little_endian</ByteOrder>
	<DefaultCharset>utf8</DefaultCharset>
	<Binlogs>
		<File>D:\build\binlogminer\binlogs\5.7.18\blog.000016</File> 
	</Binlogs>
	<OutputTable dsid="sqlite-database" truncate="true">binlogminer</OutputTable>
	<ReferenceTable dsid="sqlite-database">ref_columns</ReferenceTable>
	<DataSource id="sqlite-database">
		<Driver>org.sqlite.JDBC</Driver>
		<Url>jdbc:sqlite:D:\build\binlogminer\miner.db</Url>
		<Properties>journal_mode=WAL;wal_autocheckpoint=6000;synchronous=NORMAL;cache_ssize=8000</Properties>
	</DataSource>
</Configuration>
``` 
3. run binlogminer.cmd, and get undo/redo statements from sqlite's binlogminer table.

### Development 
Import binlogminer.jar into your project. 

1. Parser binlog file "/data/mysql/5.7.18/binlog/blog.000020".
```java
BinlogParser parser = BinlogParserBuilder.newBuilder("/data/mysql/5.7.18/binlog/blog.000020")
	.withByteOrder(ByteOrder.LITTLE_ENDIAN)
	.decodeString(true)
	.withCharSet("gbk")
	.build();
while (parser.hasEvent()) {
	BinlogEvent event = parser.nextEvent();
	EventHeader header = event.getHeader();
	EventBody body = event.getBody();
	// do something ... 
}
parser.close();
```

2. Get statements from "/data/mysql/5.7.18/binlog/blog.000020".
```java
HashSet<EventType> eventFilter = new HashSet<EventType>();
eventFilter.add(EventType.TABLE_MAP_EVENT);
eventFilter.add(EventType.QUERY_EVENT);
eventFilter.add(EventType.WRITE_ROWS_EVENT_V1);
eventFilter.add(EventType.UPDATE_ROWS_EVENT_V1);
eventFilter.add(EventType.DELETE_ROWS_EVENT_V1);
eventFilter.add(EventType.WRITE_ROWS_EVENT);
eventFilter.add(EventType.UPDATE_ROWS_EVENT);
eventFilter.add(EventType.DELETE_ROWS_EVENT);

BinlogParser parser = BinlogParserBuilder.newBuilder("/data/mysql/5.7.18/binlog/blog.000020")
	.withByteOrder(ByteOrder.LITTLE_ENDIAN)
	.withEventFilter(eventFilter)
	.decodeString(false)
	.withCharSet("utf8")
	.build();
BinlogFileMeta binlogMeta = parser.getBinlogFileMeta();

Connection connection = ConnectionFactory.newFactory()
	.setDriver(ConnectionFactory.MYSQL_DRIVER)
	.setUrl("jdbc:mysql://127.0.0.1/mysql")
	.setUser("root")
	.setPassword("Passw0rd")
	.withMySqlProperties()
	.build();

DatabaseReference dbRef = new DatabaseReference(connection, "information_schema.columns");
while (parser.hasEvent()) {
	BinlogEvent event = parser.nextEvent();
	EventHeader header = event.getHeader();
	EventBody body = event.getBody();
	if (body instanceof RowsEventBody) {
		RowsEventBody rowsEventBody = (RowsEventBody) body;
		long tableId = rowsEventBody.getTableId();
		TableMapEventBody tableMapEventBody = binlogMeta.getTableMapEventBody(tableId);
		ArrayList<StatmentPair> pairArray = RowsEvent2SQL.getRowsEventStatements(tableMapEventBody, dbRef,
			rowsEventBody, binlogMeta.getDefaultCharsetName());
		System.out.println(header.getEventType()+", at "+header.getStartPosition()+": ");
		for(StatmentPair stmt: pairArray) {
			System.out.println("  "+stmt.getRedoStatement());
			System.out.println("  "+stmt.getRedoStatement());
		}
	}
}
parser.close();
connection.close();
```


### Contact Me
lee.xiang@hotmail.com
