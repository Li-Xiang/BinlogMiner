java -Xms256m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -cp "*;./lib/*"  org.littlestar.mysql.binlog.BinlogParserApp %*