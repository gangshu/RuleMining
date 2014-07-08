package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.data.VertexC;

public class VertexRowMapper<V extends Vertex> implements ParameterizedRowMapper<V> {
	@Override
	public V mapRow(ResultSet rs, int rowNum) throws SQLException {
		Vertex vertex = new Vertex();
		
		vertex.setVertexKey(rs.getInt(1));
		vertex.setVertexLabel(rs.getString(2));
		vertex.setVertexKindId(rs.getString(3));
		vertex.setStartline(rs.getInt(4));
		vertex.setEndline(rs.getInt(5));
		vertex.setVertexCharacters(rs.getString(6));
		vertex.setPdgId(rs.getString(7));
		vertex.setVertexAst(rs.getString(8));
		return (V)vertex;
	}

}
