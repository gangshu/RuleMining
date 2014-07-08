package edu.cwru.eecs.statianalysis.pattern;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;

/**
 * The bug fix defined here is an intraprocedural bug fix, which means that the
 * bug fix only affects one procedure.
 * 
 * @author Boya Sun
 */
public class BugFix<V extends Vertex, E extends Edge<V>> implements Serializable{
	/**
	 * The transaction where this bug fix belongs to
	 */
	private int tid;
	/**
	 * The file where this bug fix occured
	 */
	private String file;
	/**
	 * The procedure dependence graph of the buggy project where this bug fix
	 * belongs to.
	 */
	private PDG<V, E> bugPdg;
	/**
	 * The procedure dependence graph of the fix project where this bug fix
	 * belongs to.
	 */
	private PDG<V, E> fixPdg;
	/**
	 * Each changed hunk is a hunk in the diff of the bug fix. For example:
	 * <p>
	 * 
	 * @@ -1933,14 +1933,18 @@
	 *    <p>
	 *    posix_lchown(PyObject *self, PyObject *args)
	 *    <p>
	 *    + {
	 *    <p>
	 *    - char *path = NULL;
	 * 
	 *    <p>
	 *    would be a chunk. For changed_hunk_bug, each hunk contains all lines
	 *    affected in the buggy code, which are the lines with "-"; For
	 *    changed_hunk_fix, each hunk contains all lines affected in the fix
	 *    code, which are the lines with "+";
	 */
	private Map<Integer, List<Integer>> bugChangedHunk, fixChangedHunk;
	/**
	 * Bug/fix pattern pairs created by programmers from this bug fix.
	 * <p>
	 * Programmers are likely to create several patterns from one bug fix. For
	 * example, the bug fix added return value for two different function calls
	 * within one function
	 */
	private List<BugFixPattern<V, E>> patternList;
	private int i = 0;

	public BugFix() {

	}

	/**
	 * A basic constructor with the transaction, filename and pdg_id_fix. These
	 * three fields are actually the input from the GUI. The basic constructor
	 * can be used to build a basic BugFix, and the basic BugFix can be fully
	 * constructed with the AbstractBugFixBuilder
	 * 
	 * @param transaction
	 * @param file
	 * @param pdg_id_fix
	 */
	public BugFix(int tid, String file, String fixPdgId) {
		this.tid = tid;
		this.file = file;
		// bugPdg = new PDG(pdg_id_fix);
	}

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public PDG<V, E> getBugPdg() {
		return bugPdg;
	}

	public void setBugPdg(PDG<V, E> bugPdg) {
		this.bugPdg = bugPdg;
	}

	public PDG<V, E> getFixPdg() {
		return fixPdg;
	}

	public void setFixPdg(PDG<V, E> fixPdg) {
		this.fixPdg = fixPdg;
	}

	public Map<Integer, List<Integer>> getBugChangedHunk() {
		return bugChangedHunk;
	}

	public void setBugChangedHunk(Map<Integer, List<Integer>> bugChangedHunk) {
		this.bugChangedHunk = bugChangedHunk;
	}

	public Map<Integer, List<Integer>> getFixChangedHunk() {
		return fixChangedHunk;
	}

	public void setFixChangedHunk(Map<Integer, List<Integer>> fixChangedHunk) {
		this.fixChangedHunk = fixChangedHunk;
	}

	public List<BugFixPattern<V, E>> getPatternList() {
		return patternList;
	}

	public void setPatternList(List<BugFixPattern<V, E>> patternList) {
		this.patternList = patternList;
	}

	public BugFixPattern<V, E> getPreviousBugFixPattern() {
		if (i == 0)
			return null;
		i--;
		return this.patternList.get(i);
	}

	public BugFixPattern<V, E> getNextBugFixPattern() {
		if (i == this.patternList.size())
			return null;
		i++;
		return this.patternList.get(i);
	}

	public void updateBugFixPattern() {
		// TODO add an observer to synchronize with DB operation
		/*
		 * BugFixPattern curBugFixPattern = this.patternList.get(i);
		 * curBugFixPattern.getBugPattern().getPatternGraph().recreate(pdgBug);
		 * curBugFixPattern.getFixPattern().getPatternGraph().recreate(pdgFix);
		 */
	}

	public void insertBugFixPattern(BugFixPattern<V, E> bugFixPattern) {
		// TODO add an observer to synchronize with DB operation
		this.patternList.add(i, bugFixPattern);

	}

	public void deleteCurrentBugFixPattern() {
		// TODO add an observer to synchronize with DB operation
		this.patternList.remove(i);
	}

}
