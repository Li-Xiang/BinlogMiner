package org.littlestar.mysql.binlog.parser;

public class Gtid {
	private static final String regex = "[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}:\\d+";
	String uuid = null;
	long transactionId = -1;

	public Gtid(String gtid) throws Throwable {
		if (gtid.trim().toLowerCase().matches(regex)) {
			String[] tmp = gtid.split(":");
			uuid = tmp[0];
			transactionId = Long.parseLong(tmp[1]);
		} else {
			throw new Throwable("'" + gtid + "' is not gtid format, eg: '030f51c3-cde9-11e9-80f5-3cd92b6701e3:1733'");
		}
	}

	public String getUUID() {
		return uuid;
	}

	public long getTransactionId() {
		return transactionId;
	}

	/**
	 * Compare 2 GTID by transaction id:
	 * if the transaction id is equal to the arguments then return 0;
	 * if the transaction id is less than the arguments then return -1;
	 * if the transaction id is greater than the arguments then return -1;
	 */
	public int compareTo(Gtid gtid) throws Throwable {
		if (getUUID().equals(gtid.getUUID())) {
			if (getTransactionId() == gtid.getTransactionId()) {
				return 0;
			} else if (getTransactionId() > gtid.getTransactionId()) {
				return 1;
			} else {
				return -1;
			}
		} else {
			throw new Throwable("UUID is difference, not compareable.");
		}
	}
}
