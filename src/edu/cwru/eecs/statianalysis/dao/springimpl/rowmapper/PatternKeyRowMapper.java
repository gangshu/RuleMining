package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class PatternKeyRowMapper implements ParameterizedRowMapper<Integer> {
	@Override
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return rs.getInt(1);
	}
}
