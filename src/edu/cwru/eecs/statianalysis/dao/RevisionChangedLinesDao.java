package edu.cwru.eecs.statianalysis.dao;

import java.util.List;
import java.util.Map;

import edu.cwru.eecs.statianalysis.data.Transaction;
import edu.cwru.eecs.statianalysis.to.TransactionTo;

public interface RevisionChangedLinesDao {

	public Map<Integer, Transaction> getAllRevisionChangedLines();

	public Map<Integer, List<Integer>> getBugFixChangedHunksInBug(TransactionTo transaction);
	
	public Map<Integer, List<Integer>> getBugFixChangedHunksInFix(TransactionTo transaction);
	
	public List<Integer> getTransactionIds();

	public List<String> getTransactionFileNames(int tid);

	public List<String> getTransactionPdgsFromBug(int tid, String filename);

	public List<String> getTransactionPdgsFromFix(int tid, String filename);
}
