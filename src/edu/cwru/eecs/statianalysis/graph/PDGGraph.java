package edu.cwru.eecs.statianalysis.graph;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;

public interface PDGGraph<V extends Vertex, E extends Edge<V>> extends Serializable{

	public V getVertexByKey(int vertexKey);

	public Set<V> getVerticesBySource(int line);

	public Set<V> getAllvertices();
	public Set<E> getAllEdges();
	public Set<E> getEdge(V src, V tar);
	public Set<E> getEdgeByType(V src, V tar, String typeDep);

	public Set<E> getEdgeByType(V src, V tar, String typeDep,
			boolean isTransitive);

	public Set<E> getIncomingEdgeByType(V vertex, String typeDep);

	public Set<E> getIncomingEdgeByType(V vertex, String typeDep,
			boolean isTransitive);

	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep);

	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep,
			boolean isTransitive);

	public Set<E> getIncomingEdges(V vertex);

	public Set<E> getOutgoingEdges(V vertex);

	public boolean containsEdge(E e);

	public boolean containsEdge(V sourceVertex, V targetVertex);

	public boolean containsVertex(V v);
	
	/**
	 * BFS implementation
	 * 
	 * @param startNode
	 * @param asUndirected DO BFS as undirected graph or not
	 * @return The shortest distance from each traversed node to the start node
	 */
	public Hashtable<Integer, Integer> BFS(V startNode, boolean asUndirected);
}
