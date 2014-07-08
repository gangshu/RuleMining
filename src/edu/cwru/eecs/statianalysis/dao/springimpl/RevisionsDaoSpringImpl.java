package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import edu.cwru.eecs.statianalysis.dao.RevisionsDao;
import edu.cwru.eecs.statianalysis.to.RevisionsPo;

public class RevisionsDaoSpringImpl implements RevisionsDao {

	/*private SimpleJdbcTemplate template;*/
	private DataSource dataSource;

	public RevisionsDaoSpringImpl(DataSource dataSource) {
		this.dataSource = dataSource;
		/*this.template = new SimpleJdbcTemplate(dataSource);*/
	}

	@Override
	public Map<Integer, RevisionsPo> getAllRevisions() {
		String sql = "select gid, bug_number, log from revisions order by gid";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, new Object[] {});

		Map<Integer, RevisionsPo> revisionMap = new LinkedHashMap<Integer, RevisionsPo>();
		while (rs.next()) {
			RevisionsPo revision = revisionMap.get(rs.getInt(1));
			if (revision == null) {
				revision = new RevisionsPo();
				revision.setGid(rs.getInt(1));
				revision.setLog(rs.getString(3));
				revisionMap.put(rs.getInt(1), revision);
			}
			List<Integer> bugNums = revision.getBugNumber();
			if(bugNums == null){
				bugNums = new ArrayList<Integer>();
				revision.setBugNumber(bugNums);
			}
			if(!bugNums.contains(rs.getInt(2)))
				bugNums.add(rs.getInt(2));
		}
		return revisionMap;
	}

}
