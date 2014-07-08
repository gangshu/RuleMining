package edu.cwru.eecs.statianalysis.dao.springimpl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import edu.cwru.eecs.statianalysis.dao.PDGDao;

public class PDGDaoSpringImpl implements PDGDao {

	private SimpleJdbcTemplate template;

	public PDGDaoSpringImpl(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public int getPdgEndline(String pdgId) {
		String sqlMax = "select min(startline) from pdg p, vertex v where p.pdg_id = ? and v.pdg_id = p.pdg_id";
		StringBuffer sql = new StringBuffer();
		sql.append("select decode(endline, null, (")
			.append(sqlMax).append("), endline) as endline ")
			.append("from vertex where vertex_kind_id = 9 and pdg_id = ?");
		/*System.out.println(sql.toString());*/
		return this.template.queryForInt(sql.toString(), pdgId, pdgId);
	}

	@Override
	public String getPdgFile(String pdgId) {
		String sql = "select trim(f.filename) from pdg p, source_file f where p.compiler_id = f.compiler_id and p.pdg_id = ?";
		return this.template.queryForObject(sql, ParameterizedSingleColumnRowMapper.newInstance(String.class), pdgId);
	}

	@Override
	public String getPdgName(String pdgId) {
		String sql = "select pdg_name from pdg where pdg_id = ?";
		return this.template.queryForObject(sql, ParameterizedSingleColumnRowMapper.newInstance(String.class), pdgId);
	}

	@Override
	public int getPdgStartline(String pdgId) {
		String sqlMin = "select min(startline) from pdg p, vertex v where p.pdg_id = ? and v.pdg_id = p.pdg_id";
		StringBuffer sql = new StringBuffer();
		sql.append("select decode(startline, null, (")
			.append(sqlMin).append("), startline) as startline ")
			.append("from vertex where vertex_kind_id = 8 and pdg_id = ?");
		return this.template.queryForInt(sql.toString(), pdgId, pdgId);
	}
	
	@Override
	public String getTarPdg(String projectSrc, String projectTar, String pdgSrc) {
		/*concatenates the nasty sql statement...*/
		
		/*Sub query to retrieve filename from pdg id*/
		String tmpSql = "select trim(f.filename) from :project.pdg p, :project.source_file f where p.compiler_id = f.compiler_id and p.pdg_id = ?";
		tmpSql = tmpSql.replaceAll(":project", projectSrc);
		
		/*Sub query, get compiler id from the result of the last query*/
		StringBuffer compileridSql = new StringBuffer("select compiler_id from :project.source_file ");
		compileridSql.append("where filename like '%' || (")
			.append(tmpSql).append(") || '%'");
		tmpSql = compileridSql.toString().replaceAll(":project", projectTar);
		
		
		StringBuffer sql = new StringBuffer();
		sql.append("select tar.pdg_id from :tarproject.pdg tar, :srcproject.pdg src ")
			.append("where src.pdg_id = ? and src.pdg_name = tar.pdg_name and tar.compiler_id in (")
			.append(tmpSql).append(")");
		tmpSql = sql.toString().replace(":tarproject", projectTar);
		tmpSql = tmpSql.replace(":srcproject", projectSrc);
		
		return this.template.queryForObject(tmpSql, 
				ParameterizedSingleColumnRowMapper.newInstance(String.class), 
				pdgSrc, pdgSrc);
	}
	
	public List<Integer> getPdgStartline() {
		String sql = "select startline from vertex where vertex_kind_id = 8";
		return this.template.query(sql, ParameterizedSingleColumnRowMapper.newInstance(Integer.class));
	}

	@Override
	public List<String> getParentPdgs(int vertexKey) {
		String sql = "select distinct cs.pdg_id from vertex cs, vertex ent, vertex v, edges e\n"+
					 "where e.src_vertex_key = cs.vertex_key and\n"+
					 "e.tar_vertex_key = ent.vertex_key and\n"+
					 "e.edge_type = 4 and\n"+
					 "v.pdg_id = ent.pdg_id and\n"+
					 "ent.vertex_kind_id = 8 and\n"+
					 "v.vertex_key = ?";
		return this.template.query(sql, 
				ParameterizedSingleColumnRowMapper.newInstance(String.class), 
				vertexKey);
	}

}
