package edu.cwru.eecs.statianalysis.to;

import java.util.List;

public class RevisionsPo {

	private int gid;
	private List<Integer> bugNumber;
	private String log;

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public List<Integer> getBugNumber() {
		return bugNumber;
	}

	public void setBugNumber(List<Integer> bugNumber) {
		this.bugNumber = bugNumber;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

}
