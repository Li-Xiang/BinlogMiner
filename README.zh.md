## BinlogMiner
BinlogMiner是一个开源的, 基于Java的MySQL二进制日志分析库和实用工具(挖掘工具和分析工具)。通过BinlogMiner, 你可以轻松的解析MySQL二进制日志中的事件, 还可以根据解析出的可执行的UNDO/REDO语句, 实现数据库或者数据表的重做和回滚(闪回); 通过BinlogParser可以解析出MySQL二进制日志的内容。

### 关键特性
- 支持解析当前所有的二进制格式, 包括{STATEMENT|ROW|MIXED}; (但只有row based的binlog能生成回滚语句.)
- 支持MySQL 3.9到8.0服务器版本, 支持binlog v1, v3,v4版本;
- 支持离线分析和挖掘, 可以将二进制文件拷贝到其他设备进行分析;
- 支持将挖掘出的UNDO/REDO语句输出到数据库表中, 可以使用标准的SQL语句查询和过滤需要的数据;
- 具有Java语言开发, 不需要安装, 支持大部分操作系统(Linux, Windows, Unix...), 并提供简单易用的Java AIPs供二次开发;

### 安装/编译
可以直接下载使用已经编译好的二进制JAR包，也可以克隆最新的源代码，通过ANT自己编译，或者通过Eclipse等工具编译。

1#. 下载二进制版本（https://sourceforge.net/projects/binlongminer/），解压缩后即可使用。
```shell
$ tar -xzvf xxx.tar.gz
```
或者

2#. 通过Ant编译（需要安装JDK和Apache Ant） 
```shell
$ git clone https://github.com/Li-Xiang/BinlogMiner.git
$ cd BinlogMiner
$ ant clean
$ ant
```
<pre>
注意
1. 如果要将挖掘结果输出到表中，或者需要通过数据库连接获取列信息，需要下载对应的JDBC驱动, 并将*.jar放到lib目录下。
MySQL : https://dev.mysql.com/downloads/connector/j/
Derby/Java DB's Embedded: $JDK_HOME/db/lib/derby.jar 
SQLite: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/

2. 需要创建和插入权限, 以输出数据到表中。 需要对参考表(通常为information_schema.columns)查询的权限，以构建可执行的SQL语句。
</pre>

### 用法
#### (1). 二进制日志分析工具 - binlogparser
与自带的mysqlbinlog不同点：
  可以通过指定偏移（--start-position|--stop-position）来查找或者输出指定范围的事件，偏移值不需要必须为一个事件的开始位置；
  如果开启了gtid模式（gtid_mode=on）,可以指定gtid范围（--start-gtid|--stop-gtid）；
<pre>
$ ./binlogparser --help
usage: binlogparser [options] log-files
options:
 -?,--help                                      Display this help and exit.
 -b,--byte-order <big_endian | little_endian>   Specify the byte order of the binlog file, the default is the native
                                                byte order of this Java virtual machine is running.
 -c,--character-set <name>                      Set the default character set, the default is 'utf8'.
 -D,--disable-string-decode                     Disable decode the string value, instead of output string's hex value.
 -e,--stop-position <#>                         Stop reading the binlog at first event having a offset equal or
                                                posterior to the argument.
    --event-header-only                         Output common event header only.
 -s,--start-position <#>                        Start reading the binlog at first event having a offset equal or
                                                posterior to the argument.
    --start-datetime <#>                        Start reading the binlog at first event having a datetime equal or
                                                posterior to the argument, datetime format accepted is
                                                'YYYY-MM-DD'T'hh:mm:ss', for example: '2004-12-25T11:25:56' (you should
                                                probably use quotes for your shell to set it properly).
    --start-gtid <#>                            Start reading the binlog at first event having a gtid equal or posterior
                                                to the argument.
    --stop-datetime <#>                         Stop reading the binlog at first event having a datetime equal or
                                                posterior to the argument, datetime format accepted is
                                                'YYYY-MM-DD'T'hh:mm:ss', for example: '2004-12-25T11:25:56' (you should
                                                probably use quotes for your shell to set it properly).
    --stop-gtid <#>                             Stop reading the binlog at first event having a gtid equal or posterior
                                                to the argument.
 -t,--events <events>                           Output only this comma-sparated list of binlog events.
</pre>
examples:
<pre>
$ ./binlogparser --events='GTID_LOG_EVENT,TABLE_MAP_EVENT,QUERY_EVENT' /data/mysql/8.0.15/binlog/blog.000005
$ ./binlogparser --start-position=6490000 --event-header-only /data/mysql/8.0.15/binlog/blog.000005 
$ ./binlogparser --start-gtid='29e14539-0466-11ea-8cb2-080027a92a27:14757' --stop-gtid='29e14539-0466-11ea-8cb2-080027a92a27:14758' /data/mysql/8.0.15/binlog/blog.000005 
$ ./binlogparser --stop-datetime='2019-11-13T09:52:16' /data/mysql/8.0.15/binlog/blog.000005

</pre>
#### (2). 二进制日志挖掘工具 - binlogminer
可以通过命令行模式或者配置文件方式来配置BinlogMiner来进行二进制数据挖掘。
- 命令行模式
<pre>
$ binlogminer [binlog-file1; binlog-file2; ...] [little_endian | big_endian] 
  参数1# 指定要分析的二进制文件路径，支持指定多个文件, 每个文件使用';'进行分隔.
  参数2# (可选) 指定二进制文件的字节序，默认使用本地的字节序号。

注意: 命令行不支持输出到数据表，也不支持通过数据库连接获取列信息。
</pre>
- 通过使用配置文件(miner.xml)
<pre>
BinlogMiner的配置文件在BinlogMiner的运行目录下，文件名为miner.xml。 
使用方法参考'miner.xml.demo'文件.
</pre>
### 限制和常见问题
- MySQL产生的二进制文件默认的访问权限为640, 如果BinlogMiner的执行用户不在对应的组将无法法访问， 需要添加对应权限。
```shell
# chmod +r /path_to_binlog_files
```
- 只有行模式(binlog_format=ROW)包含撤销数据(也就是修改数据的前值), 所以只有行模式才能够生成undo语句.

- MySQL二进制文件包含的信息有限，通常不足以构建完整的可执行的SQL语句，如，通常不包含列名，也不包含字符类型列的编码类型，所以需要连接到源库去获取缺少的信息。如果没有指定数据库连接，将只能获取伪SQL语句，无法直接执行，所有字符串将使用指定的默认方式编码，如果存在不兼容的编码，将出现乱码。

MySQL8.0.1+版本开始引入了binlog_row_metadata选项 指定为FULL(非默认)时候， TABLE_MAP事件OptionalMetaData中将包含列名，可以不需要再连接到源数据库。
 
- MySQL通过二进制的QUERY_EVENT事件记录DDL语句, 目前还不支持通过QUERY_EVENT构建UNDO DDL语句，需要手动构建，这在发生表结构修改的时候非常重要. 你不能忽视这些语句，因为很可能导致构建的SQL语句不对。

- 默认使用Java的UTF-8进行字符集的解码，目前支持的非UTF-8字符集如下：
<pre>
    MySQL    |   Java
    ---------+------------
    "gbk"    | "gbk"
    "latin1" | "ISO_8859_1"
    "gb2312" | "gb2312"
    "ucs2"   | "UTF_16"
支持的字符集是可以扩展的，但需要修改: org.littlestar.mysql.binlog.parser.ParserHelper.getCharset()方法, 并重新编译.
</pre>

- 平台与字节序对照表
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

### 使用案例
准备一个测试表tpcc.employees，用于后续的测试。该表包含了常见的一些数据类型，并且不同列使用了不同的字符集，有一定的代表性。
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

#### 1. 通过配置必要的参数，闪回被修改的数据（包括删除，更新，插入操作）

模拟delete, update, insert操作, 并通过binnlogminer构建闪回的语句 ...
```sql
delete from tpcc.employees where job_id='AD_VP'; 
update `tpcc`.`employees` set commission_pct=0.30 where employee_id=104;
insert into `tpcc`.`employees` values (156,'Janette','King','JKING','011.44.1345.429268','1996-01-30 00:00:00','SA_REP',10000.00,0.35,146,80);
```
步骤1# 配置miner.xml.
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
		<!--1. 在这里指定需要分析的二进制文件 -->
		<File>/data/mysql/5.7.18/binlog/blog.000016</File> 
		<File>/data/mysql/5.7.18/binlog/blog.000017</File>
	</Binlogs>
	<!--2. 指定要输出的表，这里是tpcc.binlogminer， 使用的是source-database数据源 -->
	<OutputTable dsid="source-database" truncate="true">tpcc.binlogminer</OutputTable>
	<ReferenceTable dsid="source-database">information_schema.columns</ReferenceTable>
	<!--3. 指定列的参考表，这里是information_schema.columns， 使用的是source-database数据源 -->
	<DataSource id="source-database">
		<Driver>com.mysql.cj.jdbc.Driver</Driver>
		<Url>jdbc:mysql://127.0.0.1/information_schema</Url>
		<User>root</User>
		<Password>Passw0rd</Password>
		<Properties>useSSL=true;useUnicode=true;characterEncoding=UTF-8;rewriteBatchedStatements=true;sessionVariables=sql_log_bin=0</Properties>
	</DataSource>
</Configuration>
```
步骤2# 执行数据挖掘
```shell
$ ./binlogminer
```
步骤3# 通过查询输出表，获取需要的语句。可以参考输出表的表结构，构建需要的过滤条件，如事件，开始位置等，这里只是个例子。
```sql
--> 对应delete操作的闪回语句.
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'DELETE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;

insert into tpcc.employees (EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, HIRE_DATE, JOB_ID, SALARY, COMMISSION_PCT, MANAGER_ID, DEPARTMENT_ID) values (101, 'Neena', 'Kochhar', 'NKOCHHAR', '515.123.4568', '1989-09-21 00:00:00', 'AD_VP', 17000.00, null, 100, 90);
insert into tpcc.employees (EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, HIRE_DATE, JOB_ID, SALARY, COMMISSION_PCT, MANAGER_ID, DEPARTMENT_ID) values (102, '李', '四', 'LDEHAAN', '515.123.4569', '1993-01-13 00:00:00', 'AD_VP', 17000.00, null, 100, 90);

--> 对应update操作的闪回语句.
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'UPDATE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;
...

--> 对应insert操作的闪回语句.
select concat(undo_stmt,';') undo_stmt
from tpcc.binlogminer 
where table_schema='tpcc'
	  and table_name='employees'
      and event_type like 'WRITE_ROWS_EVENT%'
order by event_timestamp asc, start_pos asc;
...
```


步骤4# 过一遍得到的语句，如果正常即可在数据库工具或者输出到文件中执行。
<pre>
example:
mysql -uxxx -pxxx < flashback.sql
</pre>

#### 2. 使用命令行模式获取对应语句
<pre>

$ ./binlogminer /data/mysql/5.7.18/binlog/blog.000016 little_endian
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

注意: 命令行功能简单，只能输出到屏幕，因为且无法通过数据库连接获取列的参考信息，可以看到列名使用'@数字'替代，一些列的值因为字符集不对，出现乱码。
</pre>

#### 3. 离线挖掘
为避免对源库的性能造成影响，可以将二进制文件拷贝到其他的机器进行分析。 
案例中, 输出和参考的表都放在sqlite3数据库中，输出表为binlogminer，参考表为ref_columns； 
1. 需要构建参考表：
```sql
CREATE TABLE ref_columns (
    table_schema  VARCHAR (64)  NOT NULL DEFAULT '',
    table_name VARCHAR (64)  NOT NULL DEFAULT '',
    column_name  VARCHAR (64)  NOT NULL DEFAULT '',
    ordinal_position   [UNSIGNED BIG INT] (21) NOT NULL DEFAULT '0',
    character_set_name VARCHAR (32)  DEFAULT NULL
);
```
2. 将源库的information_schema.columns的数据导出， 以‘MySQL Workbench'为例：
执行查询：
```sql
select table_schema, table_name, column_name, ordinal_position, character_set_name from information_schema.columns where table_schema='tpcc';
```
<pre>点击‘Export' -> Format选择'SQL INSERT statements' 将结果集导出成插入语句，并导入到SQLite中。</pre>

3. 修改miner.xml配置：
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
		<Properties>journal_mode=WAL;wal_autocheckpoint=6000;synchronous=NORMAL;cache_size=8000</Properties>
	</DataSource>
</Configuration>
```
4. 执行binlogminer.cmd，并从SQLite的输出表binlogminer中获取语句。

### Development 
将binlogminer.jar或者源代码导入到你的项目中，即可。 

1. 解析Binlog(/data/mysql/5.7.18/binlog/blog.000020)的事件， 使用LITTLE_ENDIAN字节序，默认使用GBK进行字符串解码。
```java
BinlogParser parser = BinlogParserBuilder.newBuilder(file)
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

2. 获取binlog（/data/mysql/5.7.18/binlog/blog.000020）的UNDO/REDO SQL语句。
```java
//设置事件过滤，提升解析效率。
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
//参考表的数据库连接。
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


