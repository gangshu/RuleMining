package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import java.util.List;
import edu.cwru.eecs.statianalysis.dao.RuleDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.InstanceVertexMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.PatternKeyRowMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.RuleRowMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.VertexRowMapper;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class RuleDaoSpringImpl<V extends Vertex, E extends Edge<V>, R extends Rule<V, E>, M extends Map<Integer, V>> implements RuleDao<V, E, R, M>{
	
	private SimpleJdbcTemplate template;

	public RuleDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public R getRule(int patternKey) {
		String sql = "select pattern_key, candidate_node_label, frequency, confirm, comments from pattern where pattern_key = ?";
		return this.template.queryForObject(sql, new RuleRowMapper<R>(), patternKey);
	}
	@Override
	public int getNumMatches(int patternKey) {
		String sql = "select count(*) from violations where lost_edges=0 and lost_nodes=0 and pattern_key =?";
		return this.template.queryForInt(sql, patternKey);
	}

	@Override
	public int getNumMisMatches(int patternKey) {
		String sql = "select count(*) from violations where not(lost_edges=0 and lost_nodes=0) and pattern_key =?";
		return this.template.queryForInt(sql, patternKey);
	}
	
	@Override
	public int getNumFunctionsCrossed(int patternKey, int instanceKey) {
		String sql = "select count(distinct v.pdg_id)\n"+
                     "from pattern_node_info pni, vertex v\n"+
                     "where pni.PATTERN_KEY= ? and pni.PATTERN_INSTANCE=?\n"+
                     "and pni.vertex_key = v.vertex_key\n" +
                     "order by pni.node_index";
		return this.template.queryForInt(sql, patternKey, instanceKey);
	}
	
	@Override
	public List<V> getVertices(int patternKey) {
		String sql = "select distinct pni.node_index, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast\n"+
                     "from pattern_node_info pni, vertex v\n"+
                     "where pni.PATTERN_KEY= ? and pni.PATTERN_INSTANCE=0\n"+
                     "and pni.vertex_key = v.vertex_key\n" +
                     "order by pni.node_index";
		return this.template.query(sql, new VertexRowMapper<V>(),patternKey);
	}
	@Override
	public List<EdgesPo> getEdges(int patternKey) {
		String sql = "select distinct pni_src.node_index as src_vertex_key, pni_tar.node_index as tar_vertex_key, pi.edge_type from pattern_instance pi, pattern_node_info pni_src, pattern_node_info pni_tar\n"+
                     "where pi.pattern_key = ? and pni_src.pattern_key = ? and pni_tar.pattern_key = ?\n"+
                     "and pi.graph_id=0 and pni_src.pattern_instance=0 and pni_tar.pattern_instance=0\n"+
                     "and pi.src_vertex_key = pni_src.vertex_key\n"+
                     "and pi.tar_vertex_key=pni_tar.vertex_key\n" +
                     "order by pni_src.node_index, pni_tar.node_index";   
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), patternKey, patternKey, patternKey);
	}
	@Override
	public List<M> getInstanceVertexIndexMaps(int patternKey, int instanceKey) {
		String sql = "select distinct pni.node_index,v.vertex_key, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast\n"+
                     "from pattern_node_info pni, vertex v\n"+
                     "where pni.pattern_key = ? and pni.pattern_instance= ?\n"+
                     "and pni.vertex_key = v.vertex_key\n"+
                     "order by pni.node_index";
		return template.query(sql,new InstanceVertexMapper<V,M>(),patternKey, instanceKey);
	}
	@Override
	public List<Integer> getTrueRuleKeys() {
		String sql = "select pattern_key from pattern where confirm in ('Y','E') and pattern_key >0 order by pattern_key";
		return this.template.query(sql, new PatternKeyRowMapper());
	}
	@Override
	public List<Integer> getFalseRuleKeys() {
		String sql = "select pattern_key from pattern where confirm = 'N' and pattern_key >0 order by pattern_key";
		return this.template.query(sql, new PatternKeyRowMapper());
	}

	@Override
	public int getNumEdges(int patternKey) {
		String sql = "select count(*) from pattern_instance pi where graph_id=0 and pattern_key = ?";
		return this.template.queryForInt(sql, patternKey);
	}

	@Override
	public int getNumEdgesByType(int patternKey, String type) {
		String sql = "select count(*) from pattern_instance pi where graph_id=0 and pattern_key = ? and edge_type = ?";
		return this.template.queryForInt(sql, patternKey, type);
	}

	@Override
	public int getNumNodes(int patternKey) {
		String sql = "select count(*) from pattern_node_info where pattern_instance=0 and pattern_key = ?";
		return this.template.queryForInt(sql, patternKey);
	}

	@Override
	public List<Integer> getFunctionInvolved(int patternKey) {
		String sql = "select distinct vertex_label from pattern_node_info pni, vertex v\n"+ 
                     "where pni.pattern_key =? and pni.PATTERN_INSTANCE=0 and pni.vertex_key = v.vertex_key and v.vertex_kind_id=5\n"+
                     "order by vertex_label\n";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper.newInstance(Integer.class), patternKey);
	}

	@Override
	public List<Integer> getFunctionOccur(int patternKey) {
		String sql = "select count(*) from pattern_node_info pni, vertex v where pni.pattern_key = ? and pni.PATTERN_INSTANCE=0 and pni.vertex_key = v.vertex_key and v.vertex_kind_id=5 group by v.vertex_label";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper.newInstance(Integer.class), patternKey);
	}

	@Override
	public int getNumGraphDataset(int patternKey) {
		String sql = "select g.GRAPH_NUM from pattern p, graphdataset g\n"+
					 "where p.pattern_key = ?\n"+
					 "and p.graph_group_id = g.gds_id";
		return this.template.queryForInt(sql, patternKey);
	}

	@Override
	public int getCandidateNodeLabel(int patternKey) {
		String sql = "select candidate_node_label from pattern where pattern_key = ?";		
		return this.template.queryForInt(sql, patternKey);
	}

	@Override
	public List<Integer> getAllRuleKeys() {
		String sql = "select pattern_key from pattern where pattern_key >0 and confirm in ('Y','N','U') order by pattern_key";
		return this.template.query(sql, new PatternKeyRowMapper());
	}

	@Override
	public List<Integer> getAllUnobservedRuleKeys() {
		String sql = "select pattern_key from pattern where pattern_key >0 and confirm in ('U') order by pattern_key";
		return this.template.query(sql, new PatternKeyRowMapper());
	}



}
