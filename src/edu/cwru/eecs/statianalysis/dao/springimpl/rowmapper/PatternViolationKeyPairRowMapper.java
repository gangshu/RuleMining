package edu.cwru.eecs.statianalysis.dao.springimpl.rowmapper;

import edu.cwru.eecs.statianalysis.data.PatternViolationKeyPair;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class PatternViolationKeyPairRowMapper implements ParameterizedRowMapper<PatternViolationKeyPair> {
	@Override
	public PatternViolationKeyPair mapRow(ResultSet rs, int rowNum) throws SQLException {
		PatternViolationKeyPair p = new PatternViolationKeyPair();
		p.setPatternKey(rs.getInt(1));
		p.setViolationKey(rs.getInt(2));
		return p;
	}
}
