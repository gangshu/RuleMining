package edu.cwru.eecs.statianalysis.data;

import java.io.Serializable;

/*
 * @author Boya Sun
 */

public class Edge <V extends Vertex> implements Serializable{
	/**
	 * Source vertex of the edge. Which is the "starting point" of the edge
	 */
	protected V src;
	/**
	 * Target vertex of the edge. Which is the "ending point" of the edge
	 */
	protected V tar;
	/**
	 * The type of the edge, which indicates whether it is
	 * inter-/intra-procedural and whether it is data/control dependence.
	 * <p>
	 * The edge_type can be one of the followings:
	 * <p>
	 * 1: intraprocedural data dependence
	 * <p>
	 * 2: intraprocedural control dependence
	 * <p>
	 * 3: interprocedural data dependence
	 * <p>
	 * 4: interprocedural control dependence
	 */
	protected String edgeType;
	/**
	 * <p>
	 * true: the edge is a transitive path which is contracted into an edge
	 * <p>
	 * false: the edge is just an original edge in the grpah
	 * 
	 */
	protected boolean isTransitive;

	public V getSrc() {
		return src;
	}

	public void setSrc(V src) {
		this.src = src;
	}

	public V getTar() {
		return tar;
	}

	public void setTar(V tar) {
		this.tar = tar;
	}

	public String getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
	}

	public boolean isTransitive() {
		return isTransitive;
	}

	public void setTransitive(boolean isTransitive) {
		this.isTransitive = isTransitive;
	}

	

}
