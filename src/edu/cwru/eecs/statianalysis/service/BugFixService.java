package edu.cwru.eecs.statianalysis.service;

import java.util.List;

import edu.cwru.eecs.statianalysis.dao.RevisionChangedLinesDao;
import edu.cwru.eecs.statianalysis.dao.RevisionInfoDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.RevisionChangedLinesDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.RevisionInfoDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.pattern.BugFix;
import edu.cwru.eecs.statianalysis.pattern.BugFixHelper;
import edu.cwru.eecs.statianalysis.pattern.BugFixPattern;
import edu.cwru.eecs.statianalysis.pattern.PDG;
import edu.cwru.eecs.statianalysis.to.TransactionTo;

public class BugFixService {

	public BugFix<Vertex, Edge<Vertex>> getBugFix(TransactionTo transaction) {
		BugFix<Vertex, Edge<Vertex>> bugfix = new BugFix<Vertex, Edge<Vertex>>();
		bugfix.setTid(transaction.getTid());
		bugfix.setFile(transaction.getFilename());
		BugFixHelper<Vertex, Edge<Vertex>> helper = new BugFixHelper<Vertex, Edge<Vertex>>();
		PDG<Vertex, Edge<Vertex>> bugPDG = helper.createPDG(transaction.getBugPdgId(), DBUtil
				.getDataSource());
		PDG<Vertex, Edge<Vertex>> fixPDG = helper.createPDG(transaction.getFixPdgId(), DBUtil
				.getDataSource());
		bugfix.setBugPdg(bugPDG);
		bugfix.setFixPdg(fixPDG);

		RevisionChangedLinesDao linesDao = new RevisionChangedLinesDaoSpringImpl(
				DBUtil.getDataSource());
		bugfix.setBugChangedHunk(linesDao
				.getBugFixChangedHunksInBug(transaction));
		bugfix.setFixChangedHunk(linesDao
				.getBugFixChangedHunksInFix(transaction));

		RevisionInfoDao infoDao = new RevisionInfoDaoSpringImpl(DBUtil
				.getDataSource());
		List<int[]> patternKeyList = infoDao.getBugFixPatternKeys(transaction);

		// TODO throws patternKeyList null exception here

		List<BugFixPattern<Vertex, Edge<Vertex>>> patternList = helper
				.createBugFixPattern(patternKeyList);
		bugfix.setPatternList(patternList);
		return bugfix;
	}

	public void insertBugFix() {
		// TODO
	}

	public void updateBugFix() {
		// TODO
	}
}
