package edu.cwru.eecs.statianalysis.dao;

import java.util.List;
import java.util.Map;

import edu.cwru.eecs.statianalysis.data.Vertex;

public interface VertexDao<V extends Vertex> {

	public V getVertexByVertexKey(int vertexKey);
	
	public List<V> getVerticesInPdg(String pdgId);
	
	public List<V> getSrcVertices(int tarVertexKey, String pdgId);
	
	public List<V> getTarVertices(int srcVertexKey, String pdgId);
	
	public List<V> getVerticesBySource(String pdgId, int line);
	
	public int getVertexOccurrence(int vertexLabel);
	
	public int getVertexKey(String pdgId, String pdgVertexId);
	
	public String getVertexKindId(String pdgId, String pdgVertexId);
	
	public int updateVertexASTAndVertexString(String pdgId, String pdgVertexId, String astString, String labelString);
	public Map<String, Object> getPdgAndFileNameFromCallsiteLabel(int vertex_label);
	
	public List<V> getEntVertices(int vertexLabel);
	
	public String getFileName(int vertexKey);
}
