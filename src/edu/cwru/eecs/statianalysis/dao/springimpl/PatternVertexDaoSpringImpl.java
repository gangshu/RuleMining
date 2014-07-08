package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.PatternVertexDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.VertexRowMapper;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.to.PatternVertexPo;

public class PatternVertexDaoSpringImpl<V extends Vertex> implements PatternVertexDao<V> {

	private SimpleJdbcTemplate template;

	public PatternVertexDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public void deleteBugPatternVertex(PatternVertexPo vertex) {
		String sql = "delete from bug_pattern_vertex where bug_pattern_key = ?";
		this.template.update(sql, vertex.getPatternKey());
	}

	@Override
	public void deleteFixPatternVertex(PatternVertexPo vertex) {
		String sql = "delete from rule_pattern_vertex where rule_pattern_key = ?";
		this.template.update(sql, vertex.getPatternKey());
	}

	@Override
	public List<V> getBugPatternVertices(int patternKey) {
		StringBuffer sql = new StringBuffer();
		sql.append("select v.vertex_key, v.vertex_label2 as vertexLabel,v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id\n")
			.append("from ").append(DBUtil.getProject().getUser())
			.append(".vertex v, bug_pattern_vertex bv\n")
			.append("where v.VERTEX_KEY = bv.VERTEX_KEY\n")
			.append("and bv.BUG_PATTERN_KEY=?");
		return this.template.query(sql.toString(), new VertexRowMapper<V>(),
				patternKey);
	}

	@Override
	public List<V> getFixPatternVertices(int patternKey) {
		StringBuffer sql = new StringBuffer();
		sql.append("select v.vertex_key, v.vertex_label2 as vertexLabel,v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id\n")
			.append("from vertex v, rule_pattern_vertex bv\n ")
			.append("where v.VERTEX_KEY = bv.VERTEX_KEY\n")
			.append("and bv.RULE_PATTERN_KEY=?");
		return this.template.query(sql.toString(), new VertexRowMapper<V>(),
				patternKey);
	}

	@Override
	public void insertBugPatternVertices(List<PatternVertexPo> vertexList) {
		String sql = "insert into bug_pattern_vertex (BUG_PATTERN_KEY, VERTEX_KEY,VERTEX_LABEL,VERTEX_IDX) values (?,?,?,?)";
		for (int i = 0; i < vertexList.size(); i++) {
			PatternVertexPo vertex = vertexList.get(i);
			SqlParameterSource ps = new BeanPropertySqlParameterSource(vertex);
			this.template.update(sql, ps);
		}
	}

	@Override
	public void insertFixPatternVertices(List<PatternVertexPo> vertexList) {
		String sql = "insert into rule_pattern_vertex (rule_pattern_KEY, VERTEX_KEY,VERTEX_LABEL,VERTEX_IDX) values (?,?,?,?)";
		for (int i = 0; i < vertexList.size(); i++) {
			PatternVertexPo vertex = vertexList.get(i);
			SqlParameterSource ps = new BeanPropertySqlParameterSource(vertex);
			this.template.update(sql, ps);
		}
	}

	@Override
	public void updateBugPatternVertex(PatternVertexPo vertex) {
		String sql = "update bug_pattern_vertex set set vertex_key = ?, vertex_label = ?, vertex_idx = ? where rule_pattern_key = ?";
		SqlParameterSource ps = new BeanPropertySqlParameterSource(vertex);
		this.template.update(sql, ps);

	}

	@Override
	public void updateFixPatternVertex(PatternVertexPo vertex) {
		String sql = "update rule_pattern_vertex set set vertex_key = ?, vertex_label = ?, vertex_idx = ? where bug_pattern_key = ?";
		SqlParameterSource ps = new BeanPropertySqlParameterSource(vertex);
		this.template.update(sql, ps);
	}

}
