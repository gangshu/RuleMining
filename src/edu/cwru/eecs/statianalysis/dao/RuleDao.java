package edu.cwru.eecs.statianalysis.dao;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

/**
 * Generic paraeters: vertex, edges, rules
 * @author Boya Sun
 *
 * @param <V>
 * @param <E>
 * @param <R>
 */
public interface RuleDao<V, E, R, M> {	
	public R getRule(int patternKey);
	public List<V> getVertices(int patternKey);
	public List<EdgesPo> getEdges(int patternKey);
	public int getNumMatches(int patternKey);
	public int getNumMisMatches(int patternKey);
	public int getNumFunctionsCrossed(int patternKey, int instanceKey);
	public List<Integer> getTrueRuleKeys();
	public List<Integer> getFalseRuleKeys();
	public List<Integer> getAllRuleKeys();
	public List<Integer> getAllUnobservedRuleKeys();
	public List<M> getInstanceVertexIndexMaps(int patternKey, int instanceKey);
	public int getNumNodes(int patternKey);
	public int getNumEdges(int patternKey);
	public int getNumEdgesByType(int patternKey, String type);
	public List<Integer> getFunctionInvolved(int patternKey);
	public List<Integer> getFunctionOccur(int patternKey);
	public int getNumGraphDataset(int patternKey);
	public int getCandidateNodeLabel(int patternKey);
}
