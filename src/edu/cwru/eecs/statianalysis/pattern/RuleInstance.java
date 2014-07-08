package edu.cwru.eecs.statianalysis.pattern;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.InstanceGraph;

public class RuleInstance <V extends Vertex, E extends Edge<V>> {
	private int patternKey;
	private int instanceKey;
	private int numFunctionsCrossed;
	private InstanceGraph<V, E> instanceGraph;
	

	public int getPatternKey() {
		return patternKey;
	}
	public void setPatternKey(int patternKey) {
		this.patternKey = patternKey;
	}
	public int getInstanceKey() {
		return instanceKey;
	}
	public void setInstanceKey(int instanceKey) {
		this.instanceKey = instanceKey;
	}
	public InstanceGraph<V, E> getInstanceGraph() {
		return instanceGraph;
	}
	public void setInstanceGraph(InstanceGraph<V, E> instanceGraph) {
		this.instanceGraph = instanceGraph;
	}
	public int getNumFunctionsCrossed() {
		return numFunctionsCrossed;
	}
	public void setNumFunctionsCrossed(int numFunctionsCrossed) {
		this.numFunctionsCrossed = numFunctionsCrossed;
	}	
}
