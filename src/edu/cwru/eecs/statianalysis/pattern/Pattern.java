package edu.cwru.eecs.statianalysis.pattern;

import java.io.Serializable;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;

public class Pattern<V extends Vertex, E extends Edge<V>> implements Serializable{
	/**
	 * The key that uniquely identifies the pattern. The pattern can be a bug
	 * pattern, a fix pattern, a bug instance pattern etc. Patterns are usually
	 * very small PDG graphs which contains less than 200 nodes.
	 */
	private int patternKey;
	/**
	 * The PDG graph of the pattern
	 */
	private PatternGraph<V, E> patternGraph;

	public Pattern(int patternKey,
			PatternGraph<V, E> patternGraph) {
		this.patternKey = patternKey;
		this.patternGraph = patternGraph;
	}

	public int getPatternKey() {
		return patternKey;
	}

	public void setPatternKey(int patternKey) {
		this.patternKey = patternKey;
	}

	public PatternGraph<V, E> getPatternGraph() {
		return patternGraph;
	}

	public void setPatternGraph(PatternGraph<V, E> patternGraph) {
		this.patternGraph = patternGraph;
	}

}
