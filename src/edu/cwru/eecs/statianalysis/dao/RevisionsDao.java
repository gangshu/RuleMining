package edu.cwru.eecs.statianalysis.dao;

import java.util.Map;

import edu.cwru.eecs.statianalysis.to.RevisionsPo;

public interface RevisionsDao {
	public Map<Integer, RevisionsPo> getAllRevisions();
}
