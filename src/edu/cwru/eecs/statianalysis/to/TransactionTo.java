package edu.cwru.eecs.statianalysis.to;

public class TransactionTo {

	private int tid;
	private String filename;
	private String fixPdgId;
	private String bugPdgId;

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFixPdgId() {
		return fixPdgId;
	}

	public void setFixPdgId(String fixPdgId) {
		this.fixPdgId = fixPdgId;
	}

	public String getBugPdgId() {
		return bugPdgId;
	}

	public void setBugPdgId(String bugPdgId) {
		this.bugPdgId = bugPdgId;
	}

}
