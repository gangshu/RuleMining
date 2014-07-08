package edu.cwru.eecs.statianalysis.data;

import java.util.List;
import java.util.Map;

/**
 * A bug fix transaction. One transaction may affect many files and many
 * procedures. So in our work, a transaction is consists of the files involved
 * in the transaction; a file is consists of the PDGs involved in the file; and
 * a PDG contains intra procedural bug fixes. Each bug fix may contain many bug
 * / fix patterns that programmers built.
 * <p>
 * In conclution, the hierarchy of the bug fix transaction looks like this:
 * transaction -> file -> PDG -> bug fixes -> bug/fix patterns
 * 
 * @author Boya
 * 
 */
public class Transaction {
	/**
	 * Unique ID of this transaction
	 */
	private int tid;
	/**
	 * The list of bug numbers associated with the transaction. The bug numbers
	 * are the numbers in the project's bug database. This is because that one
	 * bug fix transaction may fix many bugs simualtaneously
	 * 
	 */
	private List<Integer> bugNumbers;
	/**
	 * The log in the source version control system which corresponds to this
	 * transaction.
	 */
	private String log;
	/**
	 * The list of files affected by the bug fix transaction
	 */
	private Map<String, TransactionFile> files;

	/**
	 * A basic Transaction builder with just the tid. This can be used to
	 * construct a very basic Transaction. The rest of the components can be
	 * built with the DefaultTransactionBuilder based on the basic information.
	 * 
	 * @param tid
	 */
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public List<Integer> getBugNumbers() {
		return bugNumbers;
	}

	public void setBugNumbers(List<Integer> bugNumbers) {
		this.bugNumbers = bugNumbers;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public Map<String, TransactionFile> getFiles() {
		return files;
	}

	public void setFiles(Map<String, TransactionFile> files) {
		this.files = files;
	}

}
