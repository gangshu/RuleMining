package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.RevisionInfoDao;
import edu.cwru.eecs.statianalysis.to.RevisionInfoPo;
import edu.cwru.eecs.statianalysis.to.TransactionTo;

public class RevisionInfoDaoSpringImpl implements RevisionInfoDao {
	
	private SimpleJdbcTemplate template;

	public RevisionInfoDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public List<RevisionInfoPo> getRevisionInfo(edu.cwru.eecs.statianalysis.to.TransactionTo transaction) {
		StringBuffer sql = new StringBuffer();
		sql.append("select gid, filename, pdg_id_bug, pdg_id_fix, bug_pattern_key, ")
			.append("rule_pattern_key, status, comments, isbugsubgraph from revision_info ")
			.append("where gid = ? and filename = ? and pdg_id_bug = ? and pdg_id_fix = ?");
		return this.template.query(sql.toString(), 
				ParameterizedBeanPropertyRowMapper.newInstance(RevisionInfoPo.class), 
				transaction.getTid(), transaction.getFilename(), 
				transaction.getBugPdgId(), transaction.getFixPdgId());
	}

	@Override
	public void insertRevisionInfo(RevisionInfoPo info) {
		StringBuffer sql = new StringBuffer();
		sql.append("insert into revision_info (gid, filename, pdg_id_bug, pdg_id_fix, bug_pattern_key, rule_pattern_key, status, comments, isbugsubgraph) ")
			.append("values (?,?,?,?,?,?,?,?,?)");
		SqlParameterSource ps = new BeanPropertySqlParameterSource(info);
		this.template.update(sql.toString(), ps);
	}

	@Override
	public void updateRevisionInfo(RevisionInfoPo info) {
		StringBuffer sql = new StringBuffer();
		sql.append("update revision_info set ")
			.append("bug_pattern_key = ?, rule_pattern_key = ?, status = ?, comments = ?, ")
			.append("isbugsubgraph = ?) ")
			.append("where gid = ? and filename = ? and pdg_id_bug = ? and pdg_id_fix = ?");
		SqlParameterSource ps = new BeanPropertySqlParameterSource(info);
		this.template.update(sql.toString(), ps);
	}

	@Override
	public boolean exist(RevisionInfoPo info) {
		String sql = "select gid from revision_info where gid = ? and filename = ? and pdg_id_bug =? and pdg_id_fix = ?";
		//int gid = template.queryForInt(sql, info.getGid(), info.getFilename(), info.getPdgIdBug(), info.getPdgIdFix());
		
		if(template.queryForList(sql, info.getGid(), info.getFilename(), info.getPdgIdBug(), info.getPdgIdFix()).size() > 0)
			return false;
		else
			return true;
	}

	@Override
	public List<int[]> getBugFixPatternKeys(TransactionTo transaction) {
		StringBuffer sql = new StringBuffer();
		sql.append("select bug_pattern_key, rule_pattern_key from revision_info")
			.append(" where gid = ? and filename = ? and pdg_id_bug = ? and pdg_id_fix = ?");
		return this.template.query(sql.toString(), 
				new ParameterizedRowMapper<int[]>(){

					@Override
					public int[] mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						int bugPatternKey = rs.getInt(1);
						int fixPatternKey = rs.getInt(1);
						return new int[]{bugPatternKey, fixPatternKey};
					}
			
		}, 
				transaction.getTid(), transaction.getFilename(), 
				transaction.getBugPdgId(), transaction.getFixPdgId());
	}

}
/*	String sql_check = "select gid from revision_info where gid = ? and filename = ? and pdg_id_bug =? and pdg_id_fix = ?";*/