package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.PatternEdgeDao;
import edu.cwru.eecs.statianalysis.to.EdgesPo;
import edu.cwru.eecs.statianalysis.to.PatternEdgesPo;

public class PatternEdgeDaoSpringImpl implements PatternEdgeDao {

	private SimpleJdbcTemplate template;

	public PatternEdgeDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public void deleteBugPatternEdge(PatternEdgesPo edge) {
		String sql = "delete from bug_pattern_edge where bug_pattern_key = ?";
		this.template.update(sql, edge.getPatternKey());
	}

	@Override
	public void deleteFixPatternEdge(PatternEdgesPo edge) {
		String sql = "delete from rule_pattern_edge where rule_pattern_key = ?";
		this.template.update(sql, edge.getPatternKey());
	}

	@Override
	public List<EdgesPo> getBugPatternEdges(int patternKey) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from bug_pattern_edge where bug_pattern_key = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), patternKey);
	}

	@Override
	public List<EdgesPo> getFixPatternEdges(int patternKey) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from rule_pattern_edge where rule_pattern_key = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), patternKey);
	}

	@Override
	public void insertBugPatternEdges(List<PatternEdgesPo> edgeList) {
		String sql = "insert into bug_pattern_edge (BUG_PATTERN_KEY,SRC_VERTEX_KEY,SRC_VERTEX_IDX,TAR_VERTEX_KEY,TAR_VERTEX_IDX,EDGE_TYPE) values (?,?,?,?,?,?)";
		for(int i = 0; i < edgeList.size(); i++){
			PatternEdgesPo edge = edgeList.get(i);
			SqlParameterSource ps = new BeanPropertySqlParameterSource(edge);
			this.template.update(sql, ps);
		}	
	}

	@Override
	public void insertFixPatternEdge(List<PatternEdgesPo> edgeList) {
		String sql = "insert into rule_pattern_edge (RULE_PATTERN_KEY,SRC_VERTEX_KEY,SRC_VERTEX_IDX,TAR_VERTEX_KEY,TAR_VERTEX_IDX,EDGE_TYPE) values (?,?,?,?,?,?)";
		for(int i = 0; i < edgeList.size(); i++){
			PatternEdgesPo edge = edgeList.get(i);
			SqlParameterSource ps = new BeanPropertySqlParameterSource(edge);
			this.template.update(sql, ps);
		}	

	}

	@Override
	public void updateBugPatternEdge(PatternEdgesPo edge) {
		String sql = "update bug_pattern_edge set SRC_VERTEX_KEY = ?, SRC_VERTEX_IDX = ?, TAR_VERTEX_KEY = ?, TAR_VERTEX_IDX = ?, EDGE_TYPE = ?) where BUG_PATTERN_Key = ?";
		SqlParameterSource ps = new BeanPropertySqlParameterSource(edge);
		this.template.update(sql, ps);
	}

	@Override
	public void updateFixPatternEdge(PatternEdgesPo edge) {
		String sql = "update rule_pattern_edge set SRC_VERTEX_KEY = ?, SRC_VERTEX_IDX = ?, TAR_VERTEX_KEY = ?, TAR_VERTEX_IDX = ?, EDGE_TYPE = ?) where RULE_PATTERN_Key = ?";
		SqlParameterSource ps = new BeanPropertySqlParameterSource(edge);
		this.template.update(sql, ps);
	}

}
