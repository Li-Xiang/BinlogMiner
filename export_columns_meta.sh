
NEW_TABLE_NAME=tpcc.ref_columns
USERNAME=root
PASSWORD=Passw0rd
DATABASE=information_schema
SQL_TEXT="select concat('insert into ${NEW_TABLE_NAME}(table_schema, table_name, column_name,ordinal_position, character_set_name)values(\'', table_schema, '\',\'',table_name,'\',\'', column_name,'\',', ordinal_position, ',', ifnull(concat('\'',character_set_name,'\''), 'null'),');')  stmt from information_schema.columns where table_schema not in('mysql','information_schema', 'performance_schema','sys')"

mysql -u${USERNAME} -p${PASSWORD} -e "${SQL_TEXT}" ${DATABASE}
