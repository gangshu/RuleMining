package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

import edu.cwru.eecs.statianalysis.to.EdgesPo;
import edu.cwru.eecs.statianalysis.to.PatternEdgesPo;

public interface PatternEdgeDao {

	public List<EdgesPo> getBugPatternEdges(int patternKey);

	public List<EdgesPo> getFixPatternEdges(int patternKey);

	public void insertBugPatternEdges(List<PatternEdgesPo> edgeList);

	public void insertFixPatternEdge(List<PatternEdgesPo> edge);

	public void deleteBugPatternEdge(PatternEdgesPo edge);

	public void deleteFixPatternEdge(PatternEdgesPo edge);

	public void updateBugPatternEdge(PatternEdgesPo edge);

	public void updateFixPatternEdge(PatternEdgesPo edge);

}
