package edu.cwru.eecs.statianalysis.to;

import java.io.Serializable;

public class BugInstancePo  implements Serializable {

	private int bugPatternKey;

	private int bugPatternInstanceKey;

	private String confirm;

	private String comments;

	public int getBugPatternKey() {
		return bugPatternKey;
	}

	public void setBugPatternKey(int bugPatternKey) {
		this.bugPatternKey = bugPatternKey;
	}

	public int getBugPatternInstanceKey() {
		return bugPatternInstanceKey;
	}

	public void setBugPatternInstanceKey(int bugPatternInstanceKey) {
		this.bugPatternInstanceKey = bugPatternInstanceKey;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
