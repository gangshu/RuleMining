package edu.cwru.eecs.statianalysis.util;

import edu.cwru.eecs.statianalysis.data.Project;

public class ProjectInfo {
	private String driver;
	private String url;

	/**
	 * Username to DB
	 */
	private String user;
	/**
	 * Password to DB
	 */
	private String password;
	/**
	 * Project folder
	 */
	private String curentVersionFolder;
	
	public String getCurentVersionFolder() {
		return curentVersionFolder;
	}

	public void setCurentVersionFolder(String curentVersionFolder) {
		this.curentVersionFolder = curentVersionFolder;
	}

	public String getThreeMonthVersionFolder() {
		return threeMonthVersionFolder;
	}

	public void setThreeMonthVersionFolder(String threeMonthVersionFolder) {
		this.threeMonthVersionFolder = threeMonthVersionFolder;
	}

	private String threeMonthVersionFolder;


	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String bugUser) {
		this.user = bugUser;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String bugPassword) {
		this.password = bugPassword;
	}


}
