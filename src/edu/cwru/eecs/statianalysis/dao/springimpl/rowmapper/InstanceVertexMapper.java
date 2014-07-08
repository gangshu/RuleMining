package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;

/**
 * 
 * @author Boya Sun
 * 
 * Might reduce generality using Map<Integer, Vertex>
 *
 */
public class InstanceVertexMapper<V extends Vertex, M extends Map<Integer, V>> implements ParameterizedRowMapper<M> {

	@Override
	public M mapRow(ResultSet rs, int rowNum) throws SQLException {
		HashMap<Integer, Vertex> map = new HashMap<Integer, Vertex>();
		Vertex vertex = new Vertex();
		vertex.setVertexKey(rs.getInt(2));
		vertex.setVertexLabel(rs.getString(3));
		vertex.setVertexKindId(rs.getString(4));
		vertex.setStartline(rs.getInt(5));
		vertex.setEndline(rs.getInt(6));
		vertex.setVertexCharacters(rs.getString(7));
		vertex.setPdgId(rs.getString(8));
		vertex.setVertexAst(rs.getString(9));
		map.put(rs.getInt(1), vertex);
		return (M)map;
	}

}
