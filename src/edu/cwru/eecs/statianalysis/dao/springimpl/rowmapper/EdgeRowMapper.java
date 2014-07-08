package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.VertexC;

public class EdgeRowMapper implements ParameterizedRowMapper<Edge> {
	@Override
	public Edge mapRow(ResultSet rs, int rowNum) throws SQLException {
		Edge edge = new Edge();
		VertexC src = new VertexC();
		src.setVertexKey(rs.getInt(1));
		VertexC tar = new VertexC();
		tar.setVertexKey(rs.getInt(2));
		edge.setSrc(src);
		edge.setTar(tar);
		edge.setEdgeType(rs.getString(3));
		
		return edge;
	}
}
