package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.to.PatternVertexPo;

public interface PatternVertexDao<V extends Vertex> {

	public List<V> getBugPatternVertices(int patternKey);

	public List<V> getFixPatternVertices(int patternKey);
	
	public void insertBugPatternVertices(List<PatternVertexPo> vertexList);
	
	public void insertFixPatternVertices(List<PatternVertexPo> vertexList);
	
	public void deleteBugPatternVertex(PatternVertexPo vertex);
	
	public void deleteFixPatternVertex(PatternVertexPo vertex);
	
	public void updateBugPatternVertex(PatternVertexPo vertex);
	
	public void updateFixPatternVertex(PatternVertexPo vertex);
	
}
