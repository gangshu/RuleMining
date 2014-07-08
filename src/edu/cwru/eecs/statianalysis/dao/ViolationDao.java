package edu.cwru.eecs.statianalysis.dao;

import edu.cwru.eecs.statianalysis.data.PatternViolationKeyPair;
import java.util.List;
import java.util.Map;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public interface ViolationDao <V, E, Vio, M> {
	public Vio getViolation(int violationKey);
	public List<V> getVertices(int violationKey);
	public List<EdgesPo> getEdges(int violationKey);
	public List<V> getDeltaVertices(int patternKey, int violationKey);
	public List<EdgesPo> getDeltaEdges(int patternKey, int violationKey);	
	public List<V> getVerticesForDeltaGraph(int patternKey, int violationKey);
	public List<EdgesPo> getEdgesForDeltaGraph(int patternKey, int violationKey);	   
	public List<M> getVertexIndexMaps(int violationKey);
	public int getNumFunctionsCrossed(int violationKey);
	public List<PatternViolationKeyPair> getTruePatternViolationKeyPairs();
	public List<PatternViolationKeyPair> getFalsePatternViolationKeyPairs();
	public List<PatternViolationKeyPair> getAllPatternViolationKeyPairs();
	public List<Map<String, Object>> getViolationsForPattern(int patternKey, String confirm);
	
}
