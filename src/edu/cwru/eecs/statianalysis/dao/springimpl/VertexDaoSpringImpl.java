package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.VertexDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.VertexRowMapper;
import edu.cwru.eecs.statianalysis.data.Vertex;

public class VertexDaoSpringImpl<V extends Vertex> implements VertexDao<V> {

	private SimpleJdbcTemplate template;

	public VertexDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public List<V> getSrcVertices(int tarVertexKey, String pdgId){
		StringBuffer sql = new StringBuffer();
		sql.append("select v.vertex_key, v.vertex_label2 as vertexLabel,v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id,v.vertex_ast\n")
			.append("from vertex v, edges e\n")
			.append("where v.vertex_key = e.tar_vertex_key\n")
			.append("and e.src_vertex_key = ?\n")
			.append("and e.tar_pdg_id = ?\n")
			.append("order by vertex_key");
		
		return this.template.query(sql.toString(), new VertexRowMapper<V>(), 
				tarVertexKey, pdgId);
	}

	@Override
	public List<V> getTarVertices(int srcVertexKey, String pdgId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select v.vertex_key, v.vertex_label2 as vertexLabel,v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast\n")
			.append("from vertex v, edges e\n")
			.append("where v.vertex_key = e.src_vertex_key\n")
			.append("and e.tar_vertex_key = ?\n")
			.append("and e.src_pdg_id = ?\n")
			.append("order by vertex_key");
		
		return this.template.query(sql.toString(), new VertexRowMapper<V>(),
				srcVertexKey, pdgId);
	}

	@Override
	public V getVertexByVertexKey(int vertexKey) {

		String sql = "select vertex_key, vertex_label2 as vertexLabel, vertex_kind_id, startline, endline, vertex_characters, pdg_id, v.vertex_ast from vertex where vertex_key = ?";
		return this.template.queryForObject(sql, new VertexRowMapper<V>(),	vertexKey);
	}

	@Override
	public List<V> getVerticesBySource(String pdgId, int line) {
		String sql = "select vertex_key, vertex_label2 as vertexLabel, vertex_kind_id, startline, endline, vertex_characters, pdg_id, vertex_ast from vertex where startline <=? and endline>=? and pdg_id = ?";

		return this.template.query(sql, new VertexRowMapper<V>(), line, line, pdgId);
	}

	@Override
	public List<V> getVerticesInPdg(String pdgId) {
		String sql = "select vertex_key, vertex_label2 as vertexLabel, vertex_kind_id, startline, endline, vertex_characters, pdg_id, vertex_ast from vertex where pdg_id = ?";

		return this.template.query(sql, new VertexRowMapper<V>(), pdgId);
	}

	@Override
	public int getVertexOccurrence(int vertexLabel) {
		String sql = "select count(*) from vertex where vertex_label = ?";
		return this.template.queryForInt(sql, vertexLabel);
	}

	@Override
	public int getVertexKey(String pdgId, String pdgVertexId) {
		String sql = "select vertex_key from vertex where pdg_id=? and pdg_vertex_id = ?";
		return this.template.queryForInt(sql, pdgId, pdgVertexId);
	}

	@Override
	public String getVertexKindId(String pdgId, String pdgVertexId) {
		String sql = "select vertex_kind_id from vertex where pdg_id = ? and pdg_vertex_id = ?";
		return this.template.queryForObject(sql, ParameterizedSingleColumnRowMapper.newInstance(String.class),pdgId, pdgVertexId);
	}

	@Override
	public int updateVertexASTAndVertexString(String pdgId, String pdgVertexId, String astString, String labelString) {
		String sql = "update vertex set vertex_ast = ?, string = ? where pdg_id = ? and pdg_vertex_id = ? ";
		return this.template.update(sql, astString, labelString, pdgId, pdgVertexId);
		
	}

	@Override
	public Map<String, Object> getPdgAndFileNameFromCallsiteLabel(
			int vertexLabel) {
		String sql = "select p.pdg_name, sf.filename\n" +
				"from (select * from vertex where vertex_label = ? and rownum<2) cs,\n" +
				"edges e, vertex ent, pdg p, source_file sf\n" +
				"where e.src_vertex_key = cs.vertex_key and\n" +
				"e.edge_type = 4\n" +
				"and e.tar_vertex_key = ent.vertex_key\n" +
				"and ent.pdg_id = p.pdg_id\n" +
				"and sf.compiler_id = p.compiler_id\n";
		return this.template.queryForMap(sql, vertexLabel);
	}

	@Override
	public List<V> getEntVertices(int vertexLabel) {
		String sql = "select cs.vertex_key, cs.vertex_label2 as vertexLabel,\n" +
				"cs.vertex_kind_id, cs.startline, cs.endline, cs.vertex_characters, cs.pdg_id,cs.vertex_ast\n" +
				"from (select * from vertex where vertex_label = ? and rownum<2) cs,\n" +
				"edges e\n" +
				"where e.edge_type = 4 and e.src_vertex_key = cs.vertex_key";
		return this.template.query(sql.toString(), new VertexRowMapper<V>(), 
				vertexLabel);
	}
	@Override
	public String getFileName(int vertexKey) {
		String sql = "select s.filename from source_file s, vertex v, pdg p\n" +
				"where v.pdg_id = p.pdg_id and p.compiler_id = s.compiler_id and v.vertex_key=?";
		return this.template.queryForObject(sql, ParameterizedSingleColumnRowMapper.newInstance(String.class), vertexKey);
	}
}
