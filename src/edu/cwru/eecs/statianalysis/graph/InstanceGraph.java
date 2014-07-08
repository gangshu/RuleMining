package edu.cwru.eecs.statianalysis.graph;

import java.util.*;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;

public class InstanceGraph <V extends Vertex, E extends Edge<V>> implements PDGGraph<V, E> {
	
	private Map<Integer, V> map;
	private PDGGraph<V, E> patternGraph;
	public InstanceGraph(Map<Integer, V> map, PDGGraph<V, E> patternGraph)
	{
		this.map = map;
		this.patternGraph = patternGraph;
	}

	@Override
	public boolean containsEdge(E e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsEdge(V sourceVertex, V targetVertex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsVertex(V v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<V> getAllvertices() {
		Set<V> vSet = patternGraph.getAllvertices();
		Set<V> ret = new HashSet<V>();
		Iterator<V> itV = vSet.iterator();
		while(itV.hasNext())
		{
			V v = itV.next();
			ret.add(this.getVertexByIndex(v.getVertexKey()));
		}
		
		return ret;
	}

	@Override
	public Set<E> getEdgeByType(V src, V tar, String typeDep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getEdgeByType(V src, V tar, String typeDep,
			boolean isTransitive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getIncomingEdgeByType(V vertex, String typeDep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getIncomingEdgeByType(V vertex, String typeDep,
			boolean isTransitive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getIncomingEdges(V vertex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep,
			boolean isTransitive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getOutgoingEdges(V vertex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V getVertexByKey(int vertexKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<V> getVerticesBySource(int line) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public V getVertexByIndex(int index)
	{
		return this.map.get(index);
	}

	@Override
	public Set<E> getAllEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<E> getEdge(V src, V tar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<Integer, Integer> BFS(V startNode, boolean asUndirected) {
		// TODO Auto-generated method stub
		return null;
	}

}
