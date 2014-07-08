package edu.cwru.eecs.statianalysis.pattern;

import java.io.Serializable;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;

/**
 * The bug fix patterns are the patterns created by programmers. This class contains a bug pattern and a fix pattern.
 * 
 * @author Boya Sun
 *
 */
public class BugFixPattern<V extends Vertex, E extends Edge<V>> implements Serializable{
	private Pattern<V, E> bugPattern;
	private Pattern<V, E> fixPattern;
	
	public BugFixPattern(Pattern<V, E> bugPattern, Pattern<V, E> fixPattern) {
		this.bugPattern = bugPattern;
		this.fixPattern = fixPattern;
	}

	public Pattern<V, E> getBugPattern() {
		return bugPattern;
	}

	public void setBugPattern(Pattern<V, E> bugPattern) {
		this.bugPattern = bugPattern;
	}

	public Pattern<V, E> getFixPattern() {
		return fixPattern;
	}

	public void setFixPattern(Pattern<V, E> fixPattern) {
		this.fixPattern = fixPattern;
	}
	
	

}
