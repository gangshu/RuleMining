package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.EdgesDao;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class EdgesDaoSpringImpl implements EdgesDao {

	private SimpleJdbcTemplate template;

	public EdgesDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public int getEdgeCountInPdg(String pdgId) {
		String sql = "select count(*) from edges where src_pdg_id = ? and tar_pdg_id = ?";
		return template.queryForInt(sql, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getEdgesInPdg(String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getIncomingEdges(int vertexKey, String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where tar_vertex_key = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getIncomingEdgesByType(int vertexKey, String type,
			String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where tar_vertex_key = ? and edge_type = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, type, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getOutgoingEdges(int vertexKey, String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where src_vertex_key = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getOutgoingEdgesByType(int vertexKey, String type,
			String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where src_vertex_key = ? and edge_type = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, type, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getEdgesByDepAndTrans(int srcVertexKey, int tarVertexKey,
			String edgeType) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from edges where src_vertex_key = ? and tar_vertex_key = ? and edge_type = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), srcVertexKey, tarVertexKey, edgeType);
	}

	@Override
	public List<EdgesPo> getInterEdgesByDepAndTrans(int srcVertexKey,
			int tarVertexKey, String edgeType) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from inter_edges where src_vertex_key = ? and tar_vertex_key = ? and edge_type = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), srcVertexKey, tarVertexKey, edgeType);
	}

	@Override
	public List<EdgesPo> getInterIncomingEdgesByType(int vertexKey,
			String type, String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from inter_edges where tar_vertex_key = ? and edge_type = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, type, pdgId, pdgId);
	}

	@Override
	public List<EdgesPo> getInterOutgoingEdgesByType(int vertexKey,
			String type, String pdgId) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from inter_edges where src_vertex_key = ? and edge_type = ? and src_pdg_id = ? and tar_pdg_id = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), vertexKey, type, pdgId, pdgId);
	}

}
