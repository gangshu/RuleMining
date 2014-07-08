package edu.cwru.eecs.statianalysis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import y.base.EdgeMap;

import edu.cwru.eecs.statianalysis.dao.RuleDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.RuleDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.graph.DefaultPDGGraph;
import edu.cwru.eecs.statianalysis.graph.InstanceGraph;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.RuleInstance;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class RuleService <V extends Vertex, E extends Edge<V>> {
	private int patternKey;
	private Rule<V, E> rule;
	RuleDao<V, E, Rule<V, E>, Map<Integer, V>> ruleDao;
	public RuleService(DataSource dataSource, int patternKey)
	{
		this.patternKey = patternKey;
		this.ruleDao = new RuleDaoSpringImpl<V, E, Rule<V, E>, Map<Integer, V>>(dataSource);		
		/**
		 * 1. Get general info
		 */
		rule = ruleDao.getRule(patternKey);
		/**
		 * 2. Get num matches, num mismatches
		 */
		rule.setNum_matches(ruleDao.getNumMatches(patternKey));
		rule.setNum_mismtaches(ruleDao.getNumMisMatches(patternKey));
		/**
		 * 3. Get graph for Rule
		 */
		this.getRuleGraph();
		/**
		 * 4. Get rule instance list
		 */
		this.getInstanceList();
		
    }
	
	private void getRuleGraph()
	{
		List<V> vertexList = ruleDao.getVertices(this.patternKey);
		List<EdgesPo> edgeList = ruleDao.getEdges(this.patternKey);
		PDGGraph<V, E> pdgGraph = new PatternGraph<V, E>(
				Edge.class, vertexList, edgeList);
		rule.setPatternGraph(pdgGraph);

	}
	
	private void getInstanceList()
	{
		List<RuleInstance<V, E>> instanceList = new ArrayList<RuleInstance<V, E>>();
		for(int i=0; i<rule.getFrequency();i++)
		{
			RuleInstance<V, E> instance = new RuleInstance<V, E>();
			instance.setPatternKey(rule.getPatternKey());
			instance.setInstanceKey(i);
			instance.setNumFunctionsCrossed(ruleDao.getNumFunctionsCrossed(this.patternKey,i));
			
			Map<Integer, V> map = this.getMap(this.patternKey, i);
			PDGGraph<V, E> pdgGraph = rule.getPatternGraph();
			InstanceGraph<V, E> instanceGraph = new InstanceGraph<V, E>(map, pdgGraph);
		    
			instance.setInstanceGraph(instanceGraph);
			instanceList.add(instance);
		}
		rule.setRuleInstanceList(instanceList);
	}
	
	private Map<Integer, V> getMap(int patternKey, int instanceKey)
	{
		List<Map<Integer, V>> vertexMaps= ruleDao.getInstanceVertexIndexMaps(patternKey, instanceKey);
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
		return vertexMap;
	}
	
	public Rule<V, E> getRule()
	{
		return this.rule;
	}

}
