package edu.cwru.eecs.statianalysis.dao;

import java.util.List;

public interface PDGDao {

	public String getPdgFile(String pdgId);

	public String getPdgName(String pdgId);

	public int getPdgStartline(String pdgId);

	public int getPdgEndline(String pdgId);
	
	public String getTarPdg(String projectSrc, String projectTar, String pdgSrc);
	
	public List<String> getParentPdgs(int vertexKey);
}
