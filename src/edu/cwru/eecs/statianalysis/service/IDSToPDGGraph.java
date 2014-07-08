package edu.cwru.eecs.statianalysis.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import andy.IDS;
import andy.IdsEdge;
import edu.cwru.eecs.statianalysis.dao.EdgesDao;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.DefaultPDGGraph;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class IDSToPDGGraph<V extends Vertex, E extends Edge<V>> {
	
	IDS ids;
	PDGGraph<V, E> pdgGraphRIDS;
	PDGGraph<V, E> pdgGraphIDS;
	
	public IDSToPDGGraph(IDS ids)
	{
		this.ids = ids;
	}
	
	@SuppressWarnings("unchecked")
	public void getIDSPdgGraph()
	{
		Hashtable<Integer, andy.Vertex> idsVertices = (Hashtable<Integer, andy.Vertex>)ids.getIds_vertices_hash();
		int[] idsVertexKeys = ids.getIds_vertices_list();
		Hashtable<String, IdsEdge> idsEdges = (Hashtable<String, IdsEdge>)ids.getIds_edges();
		this.pdgGraphIDS = this.getPDGGraph(idsVertices, idsVertexKeys, idsEdges);
		
	}
	
	@SuppressWarnings("unchecked")
	public void getRIDSPdgGraph()
	{
		Hashtable<Integer, andy.Vertex> ridsVertices = (Hashtable<Integer, andy.Vertex>)ids.getRids_vertices_hash();
		int[] ridsVertexKeys = ids.getRids_vertices_list();
		Hashtable<String, IdsEdge> ridsEdges = (Hashtable<String, IdsEdge>)ids.getRids_edges();
		this.pdgGraphRIDS = this.getPDGGraph(ridsVertices, ridsVertexKeys, ridsEdges);
	}
	
	public IDS getIds() {
		return ids;
	}

	public void setIds(IDS ids) {
		this.ids = ids;
	}

	public PDGGraph<V, E> getPdgGraphRIDS() {
		return pdgGraphRIDS;
	}

	public void setPdgGraphRIDS(PDGGraph<V, E> pdgGraphRIDS) {
		this.pdgGraphRIDS = pdgGraphRIDS;
	}

	public PDGGraph<V, E> getPdgGraphIDS() {
		return pdgGraphIDS;
	}

	public void setPdgGraphIDS(PDGGraph<V, E> pdgGraphIDS) {
		this.pdgGraphIDS = pdgGraphIDS;
	}

	@SuppressWarnings("unchecked")
	private PDGGraph<V, E> getPDGGraph(Hashtable<Integer, andy.Vertex> vHt, int[] vKeyList, Hashtable<String, IdsEdge> eHt)
	{
		List<V> vList = new ArrayList<V>();
		List<EdgesPo> ePoList = new ArrayList<EdgesPo>();
		
		/**
		 * Transform vertices
		 */
		Enumeration<andy.Vertex> vertexEnu = vHt.elements();
		while(vertexEnu.hasMoreElements())
		{
			andy.Vertex vertex = vertexEnu.nextElement();
			Vertex vNew = new Vertex();
			vNew.setVertexKey(vertex.getVertex_key());
			vNew.setVertexKindId(String.valueOf(vertex.getVertex_kind_id()));
			vNew.setVertexCharacters(vertex.getVertex_char());
			vNew.setVertexLabel(String.valueOf(vertex.getVertex_label()));
			vNew.setStartline(vertex.getStartline());
			vNew.setEndline(vertex.getStartline());
			vNew.setVertexAst(null);
			vNew.setPdgId(String.valueOf(vertex.getPdg_id()));
			vList.add((V)vNew);
		}
		
		/**
		 * Transform edges
		 */
		/**
		 * TODO: delete
		 */
		
		Enumeration<IdsEdge> edgeEnu = eHt.elements();
		while(edgeEnu.hasMoreElements())
		{
			IdsEdge edge = edgeEnu.nextElement();			
			String edgeType = String.valueOf(edge.getEdgetype());
			int srcKey = vKeyList[edge.getSrc_node_index()];
			int tarKey = vKeyList[edge.getTar_node_index()];
			EdgesPo newEdgePo = new EdgesPo();
			newEdgePo.setSrcVertexKey(srcKey);
			newEdgePo.setTarVertexKey(tarKey);
			newEdgePo.setEdgeType(edgeType);
			ePoList.add(newEdgePo);
			/**
			 * TODO: delete
			 */
			if(srcKey == 61701)
				System.out.println("Out edges for 61701 "+newEdgePo.getSrcVertexKey()+" "+newEdgePo.getTarVertexKey()+" "+newEdgePo.getEdgeType());
			if(tarKey == 66329)
				System.out.println("In edges for 66329 "+newEdgePo.getSrcVertexKey()+" "+newEdgePo.getTarVertexKey()+" "+newEdgePo.getEdgeType());

		}
		
		return new DefaultPDGGraph<V, E>(Edge.class,vList, ePoList);
		
	}

}
