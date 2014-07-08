package edu.cwru.eecs.statianalysis.pattern;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.data.MissedEdge;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;

public class Violation <V extends Vertex, E extends Edge<V>>{

	private int patternKey;
	private int violatonKey;
	private int centerNodeKey;
	private int lost_nodes;
	private int lost_edges;
	/**
	 * Y and F
	 */
	private String confirm;
	private String comments;
	private Map<Integer, V> vertexIndexMaps;
	private PDGGraph<V, E> patternGraph;
	private PDGGraph<V, E> deltaGraph;
	private int numFunctionsInvolved;
	/**
	 * The intersecting vertices for the patternGraph and the deltaGraph
	 */
	private Set<V> jointVset;
	/**
	 * The set of edges missing
	 */
	private Set<MissedEdge<V, E>> missedEdgeSet = null;
	public Set<V> getJointVset() {
		return jointVset;
	}
	public void setJointVset(Set<V> jointVset) {
		this.jointVset = jointVset;
	}
	public int getNumFunctionsInvolved() {
		return numFunctionsInvolved;
	}
	public void setNumFunctionsInvolved(int numFunctionsInvolved) {
		this.numFunctionsInvolved = numFunctionsInvolved;
	}
	public Map<Integer, V> getVertexIndexMaps() {
		return vertexIndexMaps;
	}
	public int getPatternKey() {
		return patternKey;
	}
	public void setPatternKey(int patternKey) {
		this.patternKey = patternKey;
	}
	public int getViolatonKey() {
		return violatonKey;
	}
	public void setViolatonKey(int violatonKey) {
		this.violatonKey = violatonKey;
	}
	public int getCenterNodeKey() {
		return centerNodeKey;
	}
	public void setCenterNodeKey(int centerNodeKey) {
		this.centerNodeKey = centerNodeKey;
	}
	public int getLost_nodes() {
		return lost_nodes;
	}
	public void setLost_nodes(int lostNodes) {
		lost_nodes = lostNodes;
	}
	public int getLost_edges() {
		return lost_edges;
	}
	public void setLost_edges(int lostEdges) {
		lost_edges = lostEdges;
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
	/*public Map<Integer, V> getVertexIndexMaps() {
		return vertexIndexMaps;
	}*/
	public void setVertexIndexMaps(Map<Integer, V> vertexIndexMaps) {
		this.vertexIndexMaps = vertexIndexMaps;
	}
	public PDGGraph<V, E> getPatternGraph() {
		return patternGraph;
	}
	public void setPatternGraph(PDGGraph<V, E> patternGraph) {
		this.patternGraph = patternGraph;
	}
	public PDGGraph<V, E> getDeltaGraph() {
		return deltaGraph;
	}
	public void setDeltaGraph(PDGGraph<V, E> deltaGraph) {
		this.deltaGraph = deltaGraph;
	}
	public V getVertexByIndex(int index)
	{
		return this.vertexIndexMaps.get(index);
	}
	
	/**
	 *Get the set of vertices such that one end is in the violation graph, and the other end is missing
	 * @return
	 */
	public Set<V> getJointVertexSetWithRule()
	{
		Set<V> retSet = new HashSet<V>();
		Set<V> vertexSetDelta = this.deltaGraph.getAllvertices();
		Iterator<V> it = vertexSetDelta.iterator();
		while(it.hasNext())
		{
			V vDelta = it.next();
			V vJoint = this.getVertexByIndex(vDelta.getVertexKey());
			if(vJoint!=null)
				retSet.add(vJoint);
		}
		return retSet;
	}
	
	public Set<MissedEdge<V, E>> getMissedEdges()
	{
		if(missedEdgeSet !=null)
			return missedEdgeSet;
		missedEdgeSet = new HashSet<MissedEdge<V, E>>();
		Set<E> deltaEdgeSet = getDeltaGraph().getAllEdges();
		Iterator<E> itDelta = deltaEdgeSet.iterator();
		while(itDelta.hasNext())
		{
			E deltaE = itDelta.next();
			V deltaSrc = deltaE.getSrc();
			V deltaTar = deltaE.getTar();
			
			MissedEdge<V, E> missedEdge = new MissedEdge<V, E>();
			
			missedEdge.setEdgeType(deltaE.getEdgeType());
			
			V missedSrc = this.getVertexByIndex(deltaSrc.getVertexKey());
			V missedTar = this.getVertexByIndex(deltaTar.getVertexKey());
			if(missedSrc == null)
			{
				missedSrc = deltaSrc;
				missedEdge.setSrcMissed(true);
			}
			
			if(missedTar == null)
			{
				missedTar = deltaTar;
				missedEdge.setTarMissed(true);
			}		
			
			missedEdge.setSrc(missedSrc);
			missedEdge.setTar(missedTar);
			
			missedEdgeSet.add(missedEdge);
		}
		
		return missedEdgeSet; 
	}

}
