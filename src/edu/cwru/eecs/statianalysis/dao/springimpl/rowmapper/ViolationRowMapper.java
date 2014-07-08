package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;

public class ViolationRowMapper <Vio extends Violation> implements ParameterizedRowMapper<Vio> {

	@Override
	public Vio mapRow(ResultSet rs, int rowNum) throws SQLException {
		Violation violation = new Violation();
		violation.setPatternKey(rs.getInt(1));
		violation.setViolatonKey(rs.getInt(2));
		violation.setCenterNodeKey(rs.getInt(3));
		violation.setLost_nodes(rs.getInt(4));
		violation.setLost_edges(rs.getInt(5));
		violation.setConfirm(rs.getString(6));
		violation.setComments(rs.getString(7));
		
		return (Vio)violation;
	}

}
