package edu.cwru.eecs.statianalysis.data;


/**
 * Contains information on the project which this tool applies to. The project
 * contains the username and password which is used to connect to DB, and the
 * local path to the source code of the project. These information can be
 * provided by the programmer by an UI or hard coded for experiments.
 * 
 * @author Boya Sun
 * 
 */
public class Project {
	/**
	 * Username to DB of the buggy project
	 */
	private String bugUser;
	/**
	 * Password to DB of the buggy project
	 */
	private String bugPassword;
	/**
	 * Path to source code of the buggy project
	 */
	private String bugPath;
	/**
	 * Username to DB of the fix project
	 */
	private String fixUser;
	/**
	 * Password to DB of the fix project
	 */
	private String fixPassword;
	/**
	 * Path to source code of the fix project
	 */
	private String fixPath;
	

	public String getBugUser() {
		return bugUser;
	}

	public void setBugUser(String bugUser) {
		this.bugUser = bugUser;
	}

	public String getBugPassword() {
		return bugPassword;
	}

	public void setBugPassword(String bugPassword) {
		this.bugPassword = bugPassword;
	}

	public String getBugPath() {
		return bugPath;
	}

	public void setBugPath(String bugPath) {
		this.bugPath = bugPath;
	}

	public String getFixUser() {
		return fixUser;
	}

	public void setFixUser(String fixUser) {
		this.fixUser = fixUser;
	}

	public String getFixPassword() {
		return fixPassword;
	}

	public void setFixPassword(String fixPassword) {
		this.fixPassword = fixPassword;
	}

	public String getFixPath() {
		return fixPath;
	}

	public void setFixPath(String fixPath) {
		this.fixPath = fixPath;
	}

}
