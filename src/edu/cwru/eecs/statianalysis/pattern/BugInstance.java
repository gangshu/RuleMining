package edu.cwru.eecs.statianalysis.pattern;

import java.io.Serializable;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;


/**
 * Bug instance is the subgraph that is the same as the input bug pattern which
 * is obtained by a subgraph matching algorithm agains a fairly large base
 * graph.
 * 
 * @author Boya Sun
 * 
 */
public class BugInstance<V extends Vertex, E extends Edge<V>> implements Serializable{
	/**
	 * The pattern of the bug pattern from which the bug instance is obtained
	 */
	int bugPatternKey;
	/**
	 * The key of the bug instance, which is global.
	 */
	int bugInstanceKey;
	/**
	 * The graph details of this pattern
	 */
	Pattern<V, E> pattern;
	/**
	 * <p>
	 * Y: programmer confirmed that this instance is a bug
	 * <p>
	 * N: programmer confirmed that this instance is false alarm
	 * 
	 */
	String confirm;
	/**
	 * Comments that programmers give on this bug instance, such as why it is a
	 * false alarm.
	 */
	String comments;

	/**
	 * A basic bug instance constructor with just the bug pattern key and bug
	 * instance key. This can be used to construct a very basic bug instance.
	 * The rest of the components can be built with the
	 * AbstractBugInstanceBuilder based on the basic information.
	 * 
	 * @param bugPatternKey
	 * @param bugInstanceKey
	 */
	public BugInstance(int bugPatternKey, int bugInstanceKey) {
		this.bugPatternKey = bugPatternKey;
		this.bugInstanceKey = bugInstanceKey;
	}

	/**
	 * A full constructor if you have everyting available
	 * 
	 * @param bugPatternKey
	 * @param bugInstanceKey
	 * @param pattern
	 * @param confirm
	 * @param comments
	 */
	public BugInstance(int bugPatternKey, int bugInstanceKey, Pattern<V, E> pattern,
			String confirm, String comments) {
		this.bugPatternKey = bugPatternKey;
		this.bugInstanceKey = bugInstanceKey;
		this.pattern = pattern;
		this.confirm = confirm;
		this.comments = comments;
	}

	public int getBugPatternKey() {
		return bugPatternKey;
	}

	public void setBugPatternKey(int bugPatternKey) {
		this.bugPatternKey = bugPatternKey;
	}

	public int getBugInstanceKey() {
		return bugInstanceKey;
	}

	public void setBugInstanceKey(int bugInstanceKey) {
		this.bugInstanceKey = bugInstanceKey;
	}

	public Pattern<V, E> getPattern() {
		return pattern;
	}

	public void setPattern(Pattern<V, E> pattern) {
		this.pattern = pattern;
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

}
