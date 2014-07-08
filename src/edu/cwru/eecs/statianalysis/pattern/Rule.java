package edu.cwru.eecs.statianalysis.pattern;



import java.util.List;
import java.util.Set;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;

public class Rule<V extends Vertex, E extends Edge<V>>{
	
	private int patternKey;
	/**
	 * center node of the sphere
	 */
	private int candidate_node_label;
	/**
	 * num of occurrences
	 */
	private int frequency;
	/**
	 * Y/N	
	 */
	private String confirm;
    /**
     * combination of SEQ, PRE and POST
     */
	private String comments;
	/**
	 * Num matches
	 */
	private int num_matches;
	/**
	 * Num mismatches;
	 */
	private int num_mismtaches;
	/**
	 * The PatternGraph
	 */
	private PDGGraph<V, E> patternGraph;
	/**
	 * A list of instances; 
	 * each instance is a mapping from 
	 * vertex index in the patternGraph to the vertex in the instance
	 */	
	private List<RuleInstance<V,E>> ruleInstanceList;
	
	/**
	 * Is the rule preprocessed for removing useless edges and vertices?
	 */
	private boolean isPreprocessed = false;
	public boolean isPreprocessed() {
		return isPreprocessed;
	}


	public void setPreprocessed(boolean isPreprocessed) {
		this.isPreprocessed = isPreprocessed;
	}


	public Set<E> getRemovedEdges() {
		return removedEdges;
	}


	public void setRemovedEdges(Set<E> removedEdges) {
		this.removedEdges = removedEdges;
	}


	public Set<V> getRemovedVertices() {
		return removedVertices;
	}


	public void setRemovedVertices(Set<V> removedVertices) {
		this.removedVertices = removedVertices;
	}


	/**
	 * The edges being removed
	 */
	private Set<E> removedEdges = null;
	/**
	 * The vertices being removed
	 */
	private Set<V> removedVertices = null;

	public List<RuleInstance<V,E>> getRuleInstanceList() {
		return ruleInstanceList;
	}


	public void setRuleInstanceList(List<RuleInstance<V, E>> ruleInstanceList) {
		this.ruleInstanceList = ruleInstanceList;
	}


	public int getCandidate_node_label() {
		return candidate_node_label;
	}


	public void setCandidate_node_label(int candidateNodeLabel) {
		candidate_node_label = candidateNodeLabel;
	}


	public int getFrequency() {
		return frequency;
	}


	public void setFrequency(int frequency) {
		this.frequency = frequency;
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
	public int getNum_matches() {
		return num_matches;
	}


	public void setNum_matches(int numMatches) {
		num_matches = numMatches;
	}


	public int getNum_mismtaches() {
		return num_mismtaches;
	}


	public void setNum_mismtaches(int numMismtaches) {
		num_mismtaches = numMismtaches;
	}
	
	public int getPatternKey() {
		return patternKey;
	}


	public void setPatternKey(int patternKey) {
		this.patternKey = patternKey;
	}


	public PDGGraph<V, E> getPatternGraph() {
		return patternGraph;
	}


	public void setPatternGraph(PDGGraph<V, E> patternGraph) {
		this.patternGraph = patternGraph;
	}
}
