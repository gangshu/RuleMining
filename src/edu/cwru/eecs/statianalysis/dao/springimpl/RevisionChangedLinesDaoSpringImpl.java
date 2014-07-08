package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import edu.cwru.eecs.statianalysis.dao.RevisionChangedLinesDao;
import edu.cwru.eecs.statianalysis.data.Transaction;
import edu.cwru.eecs.statianalysis.data.TransactionFile;
import edu.cwru.eecs.statianalysis.to.TransactionTo;

public class RevisionChangedLinesDaoSpringImpl implements
		RevisionChangedLinesDao {

	private SimpleJdbcTemplate template;
	private DataSource dataSource;

	public RevisionChangedLinesDaoSpringImpl(DataSource dataSource) {
		this.dataSource = dataSource;
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public List<String> getTransactionFileNames(int tid) {
		String sql = "select distinct filename from revisions_changed_lines where gid = ? and pdg_id!=0 order by filename";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper
				.newInstance(String.class), tid);
	}

	@Override
	public List<Integer> getTransactionIds() {
		String sql = "select distinct gid from revisions_changed_lines where pdg_id != 0 order by gid";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper
				.newInstance(Integer.class));
	}

	@Override
	public List<String> getTransactionPdgsFromBug(int tid, String filename) {
		String sql = "select distinct pdg_id from revisions_changed_lines where gid = ? and filename = ? and pdg_id!=0 and old_new = 'O' order by pdg_id";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper
				.newInstance(String.class), tid, filename);
	}

	@Override
	public List<String> getTransactionPdgsFromFix(int tid, String filename) {
		String sql = "select distinct pdg_id from revisions_changed_lines where gid = ? and filename = ? and pdg_id!=0 and old_new = 'N' order by pdg_id";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper
				.newInstance(String.class), tid, filename);
	}

	@Override
	public Map<Integer, Transaction> getAllRevisionChangedLines() {
		String sql = "select distinct gid, filename, pdg_id, old_new from revisions_changed_lines where pdg_id != 0 order by gid";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, new Object[] {});

		Map<Integer, Transaction> transactionMap = new LinkedHashMap<Integer, Transaction>();
		
		while (rs.next()) {
			Transaction tempTrans = transactionMap.get(rs.getInt(1));
			if(tempTrans == null){
				tempTrans  = new Transaction();
				tempTrans.setTid(rs.getInt(1));
				transactionMap.put(rs.getInt(1), tempTrans);
			}
			
			Map<String, TransactionFile> files = tempTrans.getFiles();
			if(files == null){
				files = new HashMap<String, TransactionFile>();
				tempTrans.setFiles(files);
			}				
			
			TransactionFile tempFile = files.get(rs.getString(2));
			if(tempFile == null){
				tempFile = new TransactionFile(rs.getString(2));
				tempTrans.getFiles().put(tempFile.getFilename(), tempFile);
			}
			String pdgId = rs.getString(3);
			String oldNew = rs.getString(4);
			List<String> pdgIds;
			if("N".equals(oldNew)){
				pdgIds = tempFile.getFixPdgIds();
				if(pdgIds == null){
					pdgIds = new ArrayList<String>();
					tempFile.setFixPdgIds(pdgIds);
				}
			} else{
				pdgIds = tempFile.getBugPdgIds();
				if(pdgIds == null){
					pdgIds = new ArrayList<String>();
					tempFile.setBugPdgIds(pdgIds);
				}
			}
			if(!pdgIds.contains(pdgId))
				pdgIds.add(pdgId);
		}

		return transactionMap;
	}

	
	@Override
	public Map<Integer, List<Integer>> getBugFixChangedHunksInBug(TransactionTo transaction) {
		
		/*StringBuffer sql = new StringBuffer();
		sql.append("select change_hunk_id, changed_line from revisions_changed_lines ")
		 	.append("where old_new = 'O' and gid = ? and filename = ? and pdg_id = ? and change_hunk_id in (")
		 	.append("select distinct change_hunk_id from revisions_changed_lines ")
		 	.append("where pdg_id !=0 and gid = ? and filename = ? and ")
		 	.append("(old_new = 'N' and pdg_id = ?) or (old_new = 'O' and pdg_id = ?)) ")
		 	.append("order by change_hunk_id");
		
		int tid = transaction.getTid();
		String filename = transaction.getFilename();
		String bugPdgId = transaction.getBugPdgId();
		String fixPdgId = transaction.getFixPdgId();
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql.toString(), new Object[] {tid, filename, bugPdgId, tid, filename, fixPdgId, bugPdgId});
		
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		while(rs.next()){
			List<Integer> lineList = map.get(rs.getInt(1));
			if(lineList == null){
				lineList = new ArrayList<Integer>();
				map.put(rs.getInt(1), lineList);
			}
			if(!lineList.contains(rs.getInt(2))){
				lineList.add(rs.getInt(2));
			}
		}
		
		return map;*/
		int tid = transaction.getTid();
		String filename = transaction.getFilename();
		String bugPdgId = transaction.getBugPdgId();
		String fixPdgId = transaction.getFixPdgId();
		
		String sql_hunk = "select distinct change_hunk_id from revisions_changed_lines where pdg_id !=0 and gid = ? and filename = ? and "
			+ "(old_new = 'N' and pdg_id = ?) or (old_new = 'O' and pdg_id = ?) order by change_hunk_id";
	String sql_lines_bug = "select changed_line from revisions_changed_lines where old_new = 'O' and gid = ? and filename = ? and pdg_id = ? and change_hunk_id = ? order by changed_line";
	PreparedStatement ps_hunk = null, ps_lines_bug = null;
	ResultSet rs_hunk = null, rs_lines_bug = null;
		Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
		try {
			try {
				Connection conn = dataSource.getConnection();
				ps_hunk = conn.prepareStatement(sql_hunk);
				ps_lines_bug = conn.prepareStatement(sql_lines_bug);

				ps_hunk.setInt(1, tid);
				ps_hunk.setString(2, filename);
				ps_hunk.setString(3, fixPdgId);
				ps_hunk.setString(4, bugPdgId);
				rs_hunk = ps_hunk.executeQuery();
				while (rs_hunk.next()) {
					int change_hunk_id = rs_hunk.getInt(1);
					ps_lines_bug.setInt(1, tid);
					ps_lines_bug.setString(2, filename);
					ps_lines_bug.setString(3, bugPdgId);
					ps_lines_bug.setInt(4, change_hunk_id);
					rs_lines_bug = ps_lines_bug.executeQuery();

					ArrayList<Integer> changed_lines = new ArrayList<Integer>();
					while (rs_lines_bug.next()) {
						int changed_line = rs_lines_bug.getInt(1);
						changed_lines.add(changed_line);
					}
					result.put(change_hunk_id, changed_lines);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps_hunk != null)
					ps_hunk.close();
				if (ps_lines_bug != null)
					ps_lines_bug.close();
				if (rs_hunk != null)
					rs_hunk.close();
				if (rs_lines_bug != null)
					rs_lines_bug.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (result.isEmpty())
			return null;
		else
			return result;
		
	}

	@Override
	public Map<Integer, List<Integer>> getBugFixChangedHunksInFix(TransactionTo transaction) {
		/*StringBuffer sql = new StringBuffer();
		sql.append("select change_hunk_id, changed_line from revisions_changed_lines ")
		 	.append("where old_new = 'N' and gid = ? and filename = ? and pdg_id = ? and change_hunk_id in (")
		 	.append("select distinct change_hunk_id from revisions_changed_lines ")
		 	.append("where pdg_id !=0 and gid = ? and filename = ? and ")
		 	.append("(old_new = 'N' and pdg_id = ?) or (old_new = 'O' and pdg_id = ?)) ")
		 	.append("order by change_hunk_id");

		
		
		int tid = transaction.getTid();
		String filename = transaction.getFilename();
		String bugPdgId = transaction.getBugPdgId();
		String fixPdgId = transaction.getFixPdgId();
		System.out.println(tid + "\t" + filename + "\t" + bugPdgId + "\t" + fixPdgId);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql.toString(), new Object[] {tid, filename, bugPdgId, tid, filename, fixPdgId, bugPdgId});
		
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		while(rs.next()){
			List<Integer> lineList = map.get(rs.getInt(1));
			if(lineList == null){
				lineList = new ArrayList<Integer>();
				map.put(rs.getInt(1), lineList);
			}
			if(!lineList.contains(rs.getInt(2))){
				lineList.add(rs.getInt(2));
			}
		}
		
		return map;*/
		int tid = transaction.getTid();
		String filename = transaction.getFilename();
		String bugPdgId = transaction.getBugPdgId();
		String fixPdgId = transaction.getFixPdgId();
		String sql_hunk = "select distinct change_hunk_id from revisions_changed_lines where pdg_id !=0 and gid = ? and filename = ? and "
			+ "(old_new = 'N' and pdg_id = ?) or (old_new = 'O' and pdg_id = ?) order by change_hunk_id";
	String sql_lines_fix = "select changed_line from revisions_changed_lines where old_new = 'N' and gid = ? and filename = ? and pdg_id = ? and change_hunk_id = ? order by changed_line";
	PreparedStatement ps_hunk = null, ps_lines_bug = null;
	ResultSet rs_hunk = null, rs_lines_bug = null;
	
	Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
	try {
		try {
			Connection conn = dataSource.getConnection();
			ps_hunk = conn.prepareStatement(sql_hunk);
			ps_lines_bug = conn.prepareStatement(sql_lines_fix);

			ps_hunk.setInt(1, tid);
			ps_hunk.setString(2, filename);
			ps_hunk.setString(3, fixPdgId);
			ps_hunk.setString(4, bugPdgId);
			rs_hunk = ps_hunk.executeQuery();
			while (rs_hunk.next()) {
				int change_hunk_id = rs_hunk.getInt(1);
				ps_lines_bug.setInt(1, tid);
				ps_lines_bug.setString(2, filename);
				ps_lines_bug.setString(3, fixPdgId);
				ps_lines_bug.setInt(4, change_hunk_id);
				rs_lines_bug = ps_lines_bug.executeQuery();

				ArrayList<Integer> changed_lines = new ArrayList<Integer>();
				while (rs_lines_bug.next()) {
					int changed_line = rs_lines_bug.getInt(1);
					changed_lines.add(changed_line);
				}
				result.put(change_hunk_id, changed_lines);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps_hunk != null)
				ps_hunk.close();
			if (ps_lines_bug != null)
				ps_lines_bug.close();
			if (rs_hunk != null)
				rs_hunk.close();
			if (rs_lines_bug != null)
				rs_lines_bug.close();
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	if (result.isEmpty())
		return null;
	else
		return result;
		
		
		
		
	}

	

}
