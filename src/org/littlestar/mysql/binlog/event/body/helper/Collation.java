package org.littlestar.mysql.binlog.event.body.helper;

public class Collation {
	private int id;
	private String collationName;
	private String characterSetName;
	private int sortLen;

	public Collation(int id, String collationName, String characterSetName, int sortLen) {
		this.id = id;
		this.collationName = collationName;
		this.characterSetName = characterSetName;
		this.sortLen = sortLen;
	}

	public int getId() {
		return id;
	}

	public String getCollationName() {
		return collationName;
	}

	public String getCharacterSetName() {
		return characterSetName;
	}

	public int getSortLen() {
		return sortLen;
	}
}
