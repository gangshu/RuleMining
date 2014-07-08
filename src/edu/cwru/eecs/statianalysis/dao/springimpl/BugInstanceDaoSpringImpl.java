package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.BugInstanceDao;
import edu.cwru.eecs.statianalysis.to.BugInstancePo;

public class BugInstanceDaoSpringImpl implements BugInstanceDao {

	private SimpleJdbcTemplate template;

	public BugInstanceDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public BugInstancePo getBugInstance(int bugPatternInstanceKey) {
		String sql = "select bug_pattern_instance_key, bug_pattern_key, confirm, comments from bug_instance where bug_pattern_instance_key = ?";
		return this.template.queryForObject(sql, ParameterizedBeanPropertyRowMapper.newInstance(BugInstancePo.class), bugPatternInstanceKey);
	}

	@Override
	public List<BugInstancePo> getBugInstancesByPatternKey(int bugPatternKey) {
		String sql = "select bug_pattern_instance_key, bug_pattern_key, confirm, comments from bug_instance where bug_pattern_key = ? and bug_pattern_instance_key >=0 and (confirm is null or confirm!='F') order by bug_pattern_instance_key";
		return this.template.query(sql, ParameterizedBeanPropertyRowMapper.newInstance(BugInstancePo.class), bugPatternKey);
	}

	@Override
	public void updateBugInstanceInfo(BugInstancePo bugInstance) {
		String sql = "update bug_instance set confirm = ?, comments = ? where bug_pattern_instance_key = ?";
		this.template.update(sql, bugInstance.getConfirm(), bugInstance.getComments(), bugInstance.getBugPatternInstanceKey());
	}

}