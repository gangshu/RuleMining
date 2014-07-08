package edu.cwru.eecs.statianalysis.to;

public class RevisionInfoPo {
	private int gid;
	private String filename;
	private String pdgIdBug;
	private String pdgIdFix;
	private int bugPatternKey;
	private int rulePatternkey;
	private String status;
	private String comments;
	private String isBugSubgraph;

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPdgIdBug() {
		return pdgIdBug;
	}

	public void setPdgIdBug(String pdgIdBug) {
		this.pdgIdBug = pdgIdBug;
	}

	public String getPdgIdFix() {
		return pdgIdFix;
	}

	public void setPdgIdFix(String pdgIdFix) {
		this.pdgIdFix = pdgIdFix;
	}

	public int getBugPatternKey() {
		return bugPatternKey;
	}

	public void setBugPatternKey(int bugPatternKey) {
		this.bugPatternKey = bugPatternKey;
	}

	public int getRulePatternkey() {
		return rulePatternkey;
	}

	public void setRulePatternkey(int rulePatternkey) {
		this.rulePatternkey = rulePatternkey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getIsBugSubgraph() {
		return isBugSubgraph;
	}

	public void setIsBugSubgraph(String isBugSubgraph) {
		this.isBugSubgraph = isBugSubgraph;
	}

}
