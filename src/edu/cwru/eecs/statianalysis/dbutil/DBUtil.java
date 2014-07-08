package edu.cwru.eecs.statianalysis.dbutil;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import edu.cwru.eecs.statianalysis.util.ProjectInfo;
import edu.cwru.eecs.statianalysis.util.ProjectInfoReader;

public class DBUtil {

	private static DataSource DataSource = null;
	private static ProjectInfo info;
	
	public static void createDatasource(String username){
		
		info = ProjectInfoReader.getProject(username);
		try {
			Class.forName(info.getDriver());
			DataSource = new DriverManagerDataSource(info.getUrl(), info.getUser(), info.getPassword());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static ProjectInfo getProject(){
		return info;
	}
	
	public static DataSource getDataSource(){
		return DataSource;
	}
	
	/*public static void main(String[] args){
		DBUtil.createDatasource("apache_boya");
		System.out.println(DBUtil.getDataSource());
		System.out.println(DBUtil.getProject().getUser());
		System.out.println(DBUtil.getProject().getPassword());
	}*/
}
