package edu.cwru.eecs.statianalysis.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class DefaultPDGGraph<V extends Vertex, E extends Edge<V>> implements
		PDGGraph<V, E> {

	private static final long serialVersionUID = 1L;

	protected DirectedMultigraph<V, E> jgraph;
	protected Map<Integer, V> vertexKeymap;
	protected Map<Integer, Set<V>> vertexLinemap;
	/**
	 * The key is in the form of "src_key tar_key edge_type isInter".
	 * For example "1 3 0 F"
	 */
	protected Map<String, E> edgeMap;

	public DefaultPDGGraph(Class edgeClass, List<V> vertices,
			List<EdgesPo> edges) {
		this.jgraph = new DirectedMultigraph<V, E>(
				new ClassBasedEdgeFactory<V, E>(edgeClass));
		this.vertexKeymap = new HashMap<Integer, V>();
		this.vertexLinemap = new HashMap<Integer, Set<V>>();
		this.edgeMap = new HashMap<String, E>();
		createGraph(vertices, edges);
	}

	@Override
	public Set<E> getAllEdges() {
		return this.jgraph.edgeSet();
	}

	@Override
	public Set<V> getAllvertices() {
		return this.jgraph.vertexSet();
	}

	@Override
	public Set<E> getEdgeByType(V src, V tar, String typeDep) {
		return this.getEdgeByType(src, tar, typeDep, false);
	}

	@Override
	public Set<E> getEdgeByType(V src, V tar, String typeDep,
			boolean isTransitive) {
		Set<E> edgeSet = this.jgraph.getAllEdges(src, tar);
		return this.getEdgeByType(edgeSet, typeDep, isTransitive);
	}

	@Override
	public Set<E> getIncomingEdgeByType(V vertex, String typeDep) {
		return this.getIncomingEdgeByType(vertex, typeDep, false);
	}

	@Override
	public Set<E> getIncomingEdgeByType(V vertex, String typeDep,
			boolean isTransitive) {
		Set<E> edgeSet = this.getIncomingEdges(vertex);
		return this.getEdgeByType(edgeSet, typeDep, isTransitive);
	}

	@Override
	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep) {
		return this.getOutgoingEdgeByType(vertex, typeDep, false);
	}

	@Override
	public Set<E> getOutgoingEdgeByType(V vertex, String typeDep,
			boolean isTransitive) {
		Set<E> edgeSet = this.getOutgoingEdges(vertex);
		return this.getEdgeByType(edgeSet, typeDep, isTransitive);
	}

	@Override
	public V getVertexByKey(int vertexKey) {
		return this.vertexKeymap.get(vertexKey);
	}

	@Override
	public Set<V> getVerticesBySource(int line) {
		return this.vertexLinemap.get(line);
	}

	@Override
	public Set<E> getIncomingEdges(V vertex) {
		return this.jgraph.incomingEdgesOf(vertex);
	}

	@Override
	public Set<E> getOutgoingEdges(V vertex) {
		return this.jgraph.outgoingEdgesOf(vertex);
	}

	@Override
	public boolean containsEdge(E e) {
		return this.jgraph.containsEdge(e);
	}

	@Override
	public boolean containsEdge(V sourceVertex, V targetVertex) {
		return this.jgraph.containsEdge(sourceVertex, targetVertex);
	}

	@Override
	public boolean containsVertex(V v) {
		return this.jgraph.containsVertex(v);
	}

	private void createGraph(List<V> vertices, List<EdgesPo> edges) {
		for (int i = 0; i < vertices.size(); i++) {
			V vertex = vertices.get(i);
			this.addVertex(vertex);
		}
		for (int i = 0; i < edges.size(); i++) {
			EdgesPo po = edges.get(i);
			int srVertexKey = po.getSrcVertexKey();
			int tarVertexKey = po.getTarVertexKey();
			Edge<V> edge = new Edge<V>();
			edge.setSrc(this.getVertexByKey(srVertexKey));
			edge.setTar(this.getVertexByKey(tarVertexKey));
			edge.setEdgeType(po.getEdgeType());
			edge.setTransitive(false);
			this.addEdge((E) edge);
		}
	}

	private Set<E> getEdgeByType(Set<E> edgeSet, String typeDep,
			boolean isTransitive) {
		Set<E> result = new HashSet<E>();
		/**
		 * BUGFIX: null poiter exception
		 */
		if(edgeSet == null)
			return result;
		Iterator<E> it = edgeSet.iterator();
		while (it.hasNext()) {
			E edge = it.next();
			if (edge.getEdgeType().equals(typeDep)
					&& edge.isTransitive() == isTransitive) {
				result.add(edge);
			}
		}

		return result;
	}

	/*************** might have some bugs in these two add methods ********************************/
	protected boolean addVertex(V v) {

		if (this.containsVertex(v))
			return false;
		this.vertexKeymap.put(v.getVertexKey(), v);
		for (int j = v.getStartline(); j <= v.getEndline(); j++) {
			Set<V> vertexSet = this.vertexLinemap.get(j);
			if (vertexSet == null) {
				vertexSet = new HashSet<V>();
				this.vertexLinemap.put(j, vertexSet);
			}
			vertexSet.add(v);
		}
		return this.jgraph.addVertex(v);
	}

	protected boolean addEdge(E e) {
		V src = e.getSrc();
		V tar = e.getTar();

		if (!this.addVertex(src)) {
			src = this.vertexKeymap.get(src.getVertexKey());
		}
		if (!this.addVertex(tar)) {
			tar = this.vertexKeymap.get(tar.getVertexKey());
		}
		String isTrans;
		if(e.isTransitive())
			isTrans = "T";
		else
			isTrans = "F";
        String str = e.getSrc().getVertexKey()+" "+e.getTar().getVertexKey()+" "+e.getEdgeType()+" "+isTrans;
        this.edgeMap.put(str, e);
		return jgraph.addEdge((V) e.getSrc(), (V) e.getTar(), e);
	}

	@Override
	public Set<E> getEdge(V src, V tar) {
		return jgraph.getAllEdges(src, tar);
	}

	@Override
	public Hashtable<Integer, Integer> BFS(V startNode, boolean asUndirected) {
		Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>(); //The returned nodes with distances to the start node
		Hashtable<Integer, Integer> workingV = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> visitedV = new Hashtable<Integer, Integer>();
		LinkedList<V> workingQueue = new LinkedList<V>();
		
		workingQueue.add(startNode);
		workingV.put(startNode.getVertexKey(), startNode.getVertexKey());
		ht.put(startNode.getVertexKey(), 0);
		while(!workingQueue.isEmpty())
		{
			V currentV = workingQueue.removeFirst();
			int distToStart = ht.get(currentV.getVertexKey());
			Set<E> adjOutEdges = this.getOutgoingEdges(currentV);
			Iterator<E> adjOutEdgesIt = adjOutEdges.iterator();
			while(adjOutEdgesIt.hasNext())
			{
				E e = adjOutEdgesIt.next();
				V v = e.getTar();
				if(workingV.get(v.getVertexKey())==null && visitedV.get(v.getVertexKey()) == null)
				{
					workingQueue.add(v);
					ht.put(v.getVertexKey(), distToStart+1);
					workingV.put(v.getVertexKey(), v.getVertexKey());
				}
			}
			/**
			 * If traverse as undirected graph, then also consider incoming edges
			 */
			if(asUndirected)
			{
				Set<E> adjInEdges = this.getIncomingEdges(currentV);
				Iterator<E> adjInEdgesIt = adjInEdges.iterator();
				while(adjInEdgesIt.hasNext())
				{
					E e = adjInEdgesIt.next();
					V v = e.getSrc();
					if(workingV.get(v.getVertexKey())==null && visitedV.get(v.getVertexKey()) == null)
					{
						workingQueue.add(v);
						ht.put(v.getVertexKey(), distToStart+1);
						workingV.put(v.getVertexKey(), v.getVertexKey());
					}
				}
			}
			visitedV.put(currentV.getVertexKey(), currentV.getVertexKey());
			
			
		}
		
		return ht;
	}
}
