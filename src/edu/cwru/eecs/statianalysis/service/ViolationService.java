package edu.cwru.eecs.statianalysis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import edu.cwru.eecs.statianalysis.dao.RuleDao;
import edu.cwru.eecs.statianalysis.dao.ViolationDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.ViolationDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.DefaultPDGGraph;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class ViolationService <V extends Vertex, E extends Edge<V>>{
	private int violationKey;
	private int patternKey;
	private Violation<V, E> violation;
	private ViolationDao<V, E, Violation<V, E>, Map<Integer, V>> violationDao;
	public ViolationService(DataSource dataSource, int violationKey)
	{
		this.violationKey = violationKey;
		this.violationDao = new ViolationDaoSpringImpl<V, E, Violation<V,E>, Map<Integer,V>>(dataSource);
		/**
		 * 1. Get general info
		 */
		violation = violationDao.getViolation(violationKey);	
		this.patternKey=violation.getPatternKey();
		/**
		 * 2. Get num of functions
		 */
		violation.setNumFunctionsInvolved(violationDao.getNumFunctionsCrossed(violationKey));
		/**
		 * 3. Get graph
		 */
		this.getViolationGraph();
		/**
		 * 4. Get vertex index maps
		 */
		this.getMap();
		/**
		 * 5. Get delta graph
		 */
		this.getDeltaGraph();
		/**
		 * 6. Get the intersecting vertices between the pattern graph and the delta graph
		 */
		//this.getJointVSet();
		
	}
	
	private void getViolationGraph()
	{
		List<V> vertexList = violationDao.getVertices(violationKey);
		List<EdgesPo> edgeList = violationDao.getEdges(violationKey);
		PDGGraph<V, E> patternGraph = new PatternGraph<V, E>(Edge.class, vertexList, edgeList);
		violation.setPatternGraph(patternGraph);		
	}
	
	private void getDeltaGraph()
	{
		List<V> vertexList = violationDao.getVerticesForDeltaGraph(this.patternKey, this.violationKey);
		List<EdgesPo> edgeList = violationDao.getEdgesForDeltaGraph(this.patternKey, this.violationKey);
		
		/**
		 * BUGFIX: missing condition
		 */
		if(vertexList == null || vertexList.isEmpty())
		{
			violation.setDeltaGraph(null);
			return;
		}
		
		PDGGraph<V, E> deltaGraph = new PatternGraph<V, E>(Edge.class, vertexList, edgeList);
		violation.setDeltaGraph(deltaGraph);
	}

	private void getMap()
	{
		List<Map<Integer, V>> vertexMaps= violationDao.getVertexIndexMaps(this.violationKey);
		Map<Integer, V> vertexMap = new HashMap<Integer, V>();		
		for(int j=0; j<vertexMaps.size();j++)
		{
			Map<Integer,V> map = vertexMaps.get(j);
			Set<Integer> keySet = map.keySet();
			Iterator<Integer> iterator = keySet.iterator();
			while(iterator.hasNext())
			{
				int key = iterator.next();
				V vertex = map.get(key);
				vertexMap.put(key, vertex);
			}
		}
		violation.setVertexIndexMaps(vertexMap);
	}
	public Violation<V, E> getViolatoin()
	{
		return this.violation;
	}

}
