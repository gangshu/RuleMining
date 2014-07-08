package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

import edu.cwru.eecs.statianalysis.to.BugInstancePo;

public interface BugInstanceDao {

	public BugInstancePo getBugInstance(int bugPatternInstanceKey);
	
	public List<BugInstancePo> getBugInstancesByPatternKey(int bugPatternKey);
	
	public void updateBugInstanceInfo(BugInstancePo bugInstance);
	
}
