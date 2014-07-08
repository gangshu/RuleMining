package edu.cwru.eecs.statianalysis.graph;

import java.util.List;


import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class PatternGraph<V extends Vertex, E extends Edge<V>> extends DefaultPDGGraph<V, E> {

	public PatternGraph(Class edgeClass, List<V> vertices, List<EdgesPo> edges) {
		super(edgeClass, vertices, edges);
	}
	
	public boolean addEdge(E edge){
		return super.addEdge(edge);
	}
	
	public boolean addVertex(V v){
		return super.addVertex(v);
	}

	public boolean removeVertex(V v){
		V vertex = this.vertexKeymap.get(v.getVertexKey());
		
		try{
			this.vertexKeymap.remove(vertex);
			return this.jgraph.removeVertex(vertex);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public boolean removeEdge(E e){
		String isTrans;
		if(e.isTransitive())
			isTrans = "T";
		else
			isTrans = "F";
        String str = e.getSrc().getVertexKey()+" "+e.getTar().getVertexKey()+" "+e.getEdgeType()+" "+isTrans;
        E edge = this.edgeMap.get(str);
        this.edgeMap.remove(edge);
        return this.jgraph.removeEdge(edge);
		
	}
	
}
