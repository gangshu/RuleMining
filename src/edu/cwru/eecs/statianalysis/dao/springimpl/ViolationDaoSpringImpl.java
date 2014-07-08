package edu.cwru.eecs.statianalysis.dao.springimpl;

import edu.cwru.eecs.statianalysis.data.PatternViolationKeyPair;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.ViolationDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.InstanceVertexMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.PatternViolationKeyPairRowMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.VertexRowMapper;
import edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper.ViolationRowMapper;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class ViolationDaoSpringImpl <V extends Vertex, E extends Edge<V>, Vio extends Violation<V, E>, M extends Map<Integer, V>> implements ViolationDao <V, E, Vio, M>{
	
	private SimpleJdbcTemplate template;

	public ViolationDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public  Vio getViolation(int violationKey) {
		String sql = "select pattern_key, violation_key, vertex_key, lost_nodes, lost_edges, confirm, comments from violations where violation_key = ?";
		return this.template.queryForObject(sql, new ViolationRowMapper<Vio>(), violationKey);
	}
	@Override
	public List<V> getVertices(int violationKey) {
		String sql = "select distinct v.VERTEX_KEY, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast from violation_node_info vni, vertex v\n"+
                     "where vni.violation_key = ?\n"+
                     "and vni.vertex_key = v.vertex_key\n";
                     //"order by node_index";
		return template.query(sql, new VertexRowMapper<V>(), violationKey);
	}
	@Override
	public List<EdgesPo> getEdges(int violationKey) {
		String sql = "select src_vertex_key, tar_vertex_key, edge_type from pattern_violation_edges where violation_key = ?";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class), violationKey);
	}
	@Override
	public List<V> getDeltaVertices(int patternKey, int violationKey) {
		String sql = "select pni.node_index, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id\n"+
                     "from vertex v, pattern_node_info pni\n"+
                     "where v.vertex_key = pni.vertex_key\n"+
                     "and pni.pattern_key = ? and pni.pattern_instance=0\n"+
                     "and pni.node_index in\n"+
                     "(\n"+
                     "select node_index from pattern_node_info where pattern_key = ? and pattern_instance=0\n"+
                     "minus\n"+
                     "select node_index from violation_node_info where violation_key = ?\n"+
                     ")";
        return template.query(sql, new VertexRowMapper<V>(), patternKey, patternKey, violationKey);
	}	
	@Override
	public List<EdgesPo> getDeltaEdges(int patternKey, int violationKey) {
		String sql = "(\n"+
                     "select pni_src.node_index as src_vertex_key, pni_tar.node_index as tar_vertex_key,  pi.edge_type\n"+
                     "from pattern_instance pi, pattern_node_info pni_src, pattern_node_info pni_tar\n"+
                     "where pi.PATTERN_KEY=? and pni_src.PATTERN_KEY=? and pni_tar.PATTERN_KEY=?\n"+
                     "and pi.graph_id=0 and pni_src.pattern_instance =0 and pni_tar.PATTERN_INSTANCE = 0\n"+
                     "and pi.src_vertex_key = pni_src.VERTEX_KEY\n"+
                     "and pi.tar_vertex_key = pni_tar.vertex_key\n"+
                     ")\n"+
                     "minus\n"+
                     "(\n"+
                     "select vni_src.node_index, vni_tar.node_index, pve.edge_type\n"+
                     "from pattern_violation_edges pve, violation_node_info vni_src, violation_node_info vni_tar\n"+
                     "where pve.violation_key = ? and vni_src.violation_key = ? and vni_tar.violation_key = ?\n"+
                     "and pve.src_vertex_key = vni_src.vertex_key\n"+
                     "and pve.tar_vertex_key = vni_tar.vertex_key\n"+
                     ")";
		return template.query(sql, ParameterizedBeanPropertyRowMapper
				.newInstance(EdgesPo.class),patternKey, patternKey, patternKey, violationKey, violationKey, violationKey);
	}
	@Override
	public List<V> getVerticesForDeltaGraph(int patternKey, int violationKey) {
		//This function get all the vertices appeared in the delta edges
		List<EdgesPo> edgesPoList = this.getDeltaEdges(patternKey, violationKey);
		Set<Integer> deltaVertexKeySet = new HashSet<Integer>();
		for(int i=0; i<edgesPoList.size();i++)
		{
			EdgesPo edgesPo = edgesPoList.get(i);
			if(!deltaVertexKeySet.contains(edgesPo.getSrcVertexKey()))
				deltaVertexKeySet.add(edgesPo.getSrcVertexKey());
			if(!deltaVertexKeySet.contains(edgesPo.getTarVertexKey()))
				deltaVertexKeySet.add(edgesPo.getTarVertexKey());
		}
		
		String vertexKeys = "";
		Iterator<Integer> iterator = deltaVertexKeySet.iterator();
		while(iterator.hasNext())
		{
		     vertexKeys = vertexKeys+iterator.next()+",";
		}	
		/**
		 * BUGFIX: missing condition
		 */
		if(vertexKeys == "")
			return null;
		String sql = "select distinct pni.node_index, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast\n"+
        "from vertex v, pattern_node_info pni\n"+
        "where v.vertex_key = pni.vertex_key\n"+
        "and pni.pattern_key = ? and pni.pattern_instance=0\n"+
        "and pni.node_index in (\n"+
        vertexKeys.substring(0, vertexKeys.length()-1)+
        ")";		
        return template.query(sql, new VertexRowMapper<V>(), patternKey);
	}	
	@Override
	public List<EdgesPo> getEdgesForDeltaGraph(int patternKey, int violationKey) {
		return this.getDeltaEdges(patternKey, violationKey);
	}	
	@Override
	public List<M> getVertexIndexMaps(int violationKey) {
		String sql = "select distinct vni.NODE_INDEX, v.VERTEX_KEY, v.vertex_label, v.vertex_kind_id, v.startline, v.endline, v.vertex_characters, v.pdg_id, v.vertex_ast\n"+
                     "from violation_node_info vni, vertex v\n"+
                     "where vni.violation_key = ?\n"+
                     "and vni.vertex_key = v.vertex_key\n"+
                     "order by vni.node_index";
		return template.query(sql,new InstanceVertexMapper<V,M>(),violationKey);
	}
	
	@Override
	public int getNumFunctionsCrossed(int violationKey) {
		String sql = "select count(distinct v.pdg_id)\n"+
                     "from violation_node_info vni, vertex v\n"+
                     "where vni.violation_key = ?\n"+
                     "and vni.vertex_key = v.vertex_key";
		return this.template.queryForInt(sql, violationKey);
	}


	@Override
	public List<PatternViolationKeyPair> getTruePatternViolationKeyPairs() {
		/**
		 * TODO: change back
		 */
		String sql = "select pattern_key, violation_key from violations where pattern_key > 0 and not (lost_edges = 0 and lost_edges = 0) and confirm = 'Y'\n" +
				"order by violation_key";
		return this.template.query(sql, new PatternViolationKeyPairRowMapper());
	}
	
	@Override
	public List<PatternViolationKeyPair> getFalsePatternViolationKeyPairs() {
		/**
		 * TODO: change back
		 */
		String sql = "select pattern_key, violation_key from violations where pattern_key > 0 and not (lost_edges = 0 and lost_edges = 0) and confirm = 'F'\n" +
				"order by violation_key";
		return this.template.query(sql, new PatternViolationKeyPairRowMapper());
	}

	@Override
	public List<PatternViolationKeyPair> getAllPatternViolationKeyPairs() {
		/**
		 * TODO: change back
		 */
		String sql = "select pattern_key, violation_key from violations where pattern_key > 0 and not (lost_edges = 0 and lost_edges = 0) and confirm in ('Y','F')\n" +
				"order by violation_key";
		return this.template.query(sql, new PatternViolationKeyPairRowMapper());
	}

	@Override
	public List<Map<String, Object>> getViolationsForPattern(int patternKey,
			String confirm) {
		String sql = "select violation_key from violations where pattern_key = ? and confirm = ? and not(lost_nodes = 0 and lost_edges=0)";
		return this.template.queryForList(sql, patternKey, confirm);
	}
}
