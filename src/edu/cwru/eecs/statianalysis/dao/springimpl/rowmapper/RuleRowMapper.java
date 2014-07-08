package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;

public class RuleRowMapper<R extends Rule> implements ParameterizedRowMapper<R>{
	
	@Override
	public R mapRow(ResultSet rs, int rowNum) throws SQLException {
		Rule rule = new Rule();
		rule.setPatternKey(rs.getInt(1));
		rule.setCandidate_node_label(rs.getInt(2));
		rule.setFrequency(rs.getInt(3));
		rule.setConfirm(rs.getString(4));
		rule.setComments(rs.getString(5));
		return (R)rule;
	}

}
