package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

import edu.cwru.eecs.statianalysis.to.RevisionInfoPo;
import edu.cwru.eecs.statianalysis.to.TransactionTo;

public interface RevisionInfoDao {
	
	public void insertRevisionInfo(RevisionInfoPo info);
	
	public void updateRevisionInfo(RevisionInfoPo info);
	
	public List<RevisionInfoPo> getRevisionInfo(TransactionTo transaction);

	public boolean exist(RevisionInfoPo info);
	
	public List<int[]> getBugFixPatternKeys(TransactionTo transaction);
}
