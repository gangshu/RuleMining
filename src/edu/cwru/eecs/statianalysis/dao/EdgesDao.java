package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

import edu.cwru.eecs.statianalysis.to.EdgesPo;

public interface EdgesDao {

	public List<EdgesPo> getEdgesInPdg(String pdgId);

	public int getEdgeCountInPdg(String pdgId);

	public List<EdgesPo> getIncomingEdgesByType(int vertexKey, String type,
			String pdgId);

	public List<EdgesPo> getOutgoingEdgesByType(int vertexKey, String type,
			String pdgId);

	public List<EdgesPo> getOutgoingEdges(int vertexKey, String pdgId);

	public List<EdgesPo> getIncomingEdges(int vertexKey, String pdgId);
	
	public List<EdgesPo> getEdgesByDepAndTrans(int srcVertexKey, int tarVertexKey,
			String edgeType);
	
	public List<EdgesPo> getInterIncomingEdgesByType(int vertexKey, String type,
			String pdgId);

	public List<EdgesPo> getInterOutgoingEdgesByType(int vertexKey, String type,
			String pdgId);
	
	public List<EdgesPo> getInterEdgesByDepAndTrans(int srcVertexKey, int tarVertexKey,
			String edgeType);

}
