package edu.cwru.eecs.statianalysis.pattern;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.cwru.eecs.statianalysis.dao.EdgesDao;
import edu.cwru.eecs.statianalysis.dao.PDGDao;
import edu.cwru.eecs.statianalysis.dao.PatternEdgeDao;
import edu.cwru.eecs.statianalysis.dao.PatternVertexDao;
import edu.cwru.eecs.statianalysis.dao.VertexDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.EdgesDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.PDGDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.PatternEdgeDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.PatternVertexDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.VertexDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.graph.DefaultPDGGraph;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class BugFixHelper<V extends Vertex, E extends Edge<V>> {

	public PDG<V, E> createPDG(String pdgId, DataSource dataSource) {
		VertexDao<V> vertexDao = new VertexDaoSpringImpl<V>(
				dataSource);
		EdgesDao edgeDao = new EdgesDaoSpringImpl(dataSource);
		PDGDao pdgDao = new PDGDaoSpringImpl(dataSource);
		List<V> vertexList = vertexDao.getVerticesInPdg(pdgId);
		List<EdgesPo> edgeList = edgeDao.getEdgesInPdg(pdgId);
		DefaultPDGGraph<V, E> pdgGraph = new DefaultPDGGraph<V, E>(
				Edge.class, vertexList, edgeList);
		PDG<V, E> pdg = new PDG<V, E>(pdgId);
		pdg.setPdgName(pdgDao.getPdgName(pdgId));
		pdg.setFileName(pdgDao.getPdgFile(pdgId));
		pdg.setStartLine(pdgDao.getPdgStartline(pdgId));
		pdg.setEndLine(pdgDao.getPdgEndline(pdgId));
		pdg.setPdgGraph(pdgGraph);
		return pdg;
	}

	// TODO
	/*
	 * public static List<BugFixPattern> createBugFixList(TransactionTo
	 * transaction, RevisionInfoDao infoDao){ List<int[]> bugfixKey =
	 * infoDao.getBugFixPatternKeys(transaction);
	 * 
	 * return null; }
	 */

	/*
	 * public static List<PatternGraph> createPatternGraph(int patternKey,
	 * PatternVertexDao vertexDao, PatternEdgeDao edgeDao){ List<Vertex>
	 * vertices = vertexDao.get return null; }
	 */

	public List<BugFixPattern<V, E>> createBugFixPattern(List<int[]> patternKeyList) {
		PatternVertexDao<V> vertexDao = new PatternVertexDaoSpringImpl<V>(
				DBUtil.getDataSource());
		PatternEdgeDao edgeDao = new PatternEdgeDaoSpringImpl(DBUtil
				.getDataSource());
		List<BugFixPattern<V, E>> patternList = new ArrayList<BugFixPattern<V, E>>();
		for (int[] keys : patternKeyList) {
			int bugPatternKey = keys[0];
			int fixPatternKey = keys[1];

			PatternGraph<V, E> bugPatternGraph = new PatternGraph<V, E>(
					Edge.class, vertexDao.getBugPatternVertices(bugPatternKey),
					edgeDao.getBugPatternEdges(bugPatternKey));
			PatternGraph<V, E> fixPatternGraph = new PatternGraph<V, E>(
					Edge.class, vertexDao.getBugPatternVertices(fixPatternKey),
					edgeDao.getBugPatternEdges(fixPatternKey));
			
			Pattern<V, E> bugPattern = new Pattern<V, E>(bugPatternKey,bugPatternGraph);
			Pattern<V, E> fixPattern = new Pattern<V, E>(fixPatternKey,fixPatternGraph);
			BugFixPattern<V, E> bugfixPattern = new BugFixPattern<V, E>(bugPattern, fixPattern);
			patternList.add(bugfixPattern);
		}
		
		return patternList;
	}
}
