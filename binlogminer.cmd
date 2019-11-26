
set PRG_HOME=%~dp0
set PRG_LIB="%PRG_HOME%*;.;%PRG_HOME%lib\*"

java -Xms256m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -cp %PRG_LIB%  org.littlestar.mysql.binlog.BinlogMinerApp %*