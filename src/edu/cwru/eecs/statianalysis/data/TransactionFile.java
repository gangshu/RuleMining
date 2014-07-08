package edu.cwru.eecs.statianalysis.data;

import java.util.List;
import java.util.Map;

/**
 * A file which is involved in a bug fix transaction. This file also contains
 * all the procedures in this file that is affected in the bug fix transaction
 * 
 * @author Boya
 * 
 */
public class TransactionFile {
	
	/**
	 * Name of the file
	 */
	private String filename;
	/**
	 * The list of procedures in the buggy code that is affected by a bug fix
	 * transaction in this file. "Affected" means that the PDG has added,
	 * inserted or deleted lines
	 */
	private List<String> bugPdgIds;
	/**
	 * The list of procedures in the fix code that is affected by a bug fix
	 * transaction in this file. "Affected" means that the PDG has added,
	 * inserted or deleted lines
	 */
	private List<String> fixPdgIds;
	/**
	 * Key: bug pdg_id Value: fix pdg_id
	 */
	private Map<String, String> mapBugPdgIdToFixPdgId;
	/**
	 * Key: fix pdg_id Value: bug pdg_id
	 */
	private Map<String, String> mapFixPdgIdToBugPdgId;

	/**
	 * A basic FileT constructor with just the transaction ID and the filename.
	 * This can be used to construct a very basic FileT. The rest of the more
	 * components can be built with the AbstractFileBuilder based on the basic
	 * information.
	 * 
	 * @param tid
	 * @param name
	 */
	public TransactionFile(String filename) {
		this.filename = filename;
	}
	
	public TransactionFile() {
	}

	public void setMapBugPdgIdToFixPdgId(
			Map<String, String> mapBugPdgIdToFixPdgId) {
		this.mapBugPdgIdToFixPdgId = mapBugPdgIdToFixPdgId;
	}

	public void setMapFixPdgIdToBugPdgId(
			Map<String, String> mapFixPdgIdToBugPdgId) {
		this.mapFixPdgIdToBugPdgId = mapFixPdgIdToBugPdgId;
	}

	public List<String> getBugPdgIds() {
		return bugPdgIds;
	}

	public void setBugPdgIds(List<String> bugPdgIds) {
		this.bugPdgIds = bugPdgIds;
	}

	public List<String> getFixPdgIds() {
		return fixPdgIds;
	}

	public void setFixPdgIds(List<String> fixPdgIds) {
		this.fixPdgIds = fixPdgIds;
	}

	/**
	 * Get the corresponding procedure in the buggy code from the procedure in
	 * the fix code
	 * 
	 * @param fixPdgId
	 *            the procedure in the fix code
	 * @return procedure in the buggy code
	 */
	public String getBugPdgIdFromFixPdgId(String fixPdgId) {
		return mapFixPdgIdToBugPdgId.get(fixPdgId);
	}

	/**
	 * Get the corresponding procedure in the fix code from the procedure in the
	 * buggy code
	 * 
	 * @param bugPdgId
	 *            the procedure in the buggy code
	 * @return procedure in the fix code
	 */
	public String getFixPdgIdFromBugPdgId(String bugPdgID) {
		return mapBugPdgIdToFixPdgId.get(bugPdgID);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
