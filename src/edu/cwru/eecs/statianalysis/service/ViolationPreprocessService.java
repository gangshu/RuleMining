package edu.cwru.eecs.statianalysis.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;

public class ViolationPreprocessService<V extends Vertex, E extends Edge<V>> {
	
	Rule<V, E> rule; 
	Violation<V, E> violation;
	public ViolationPreprocessService (Rule<V, E> rule, Violation<V, E> violation)
	{
		this.rule = rule;
		this.violation = violation;
	}
	
	public Set<E> removeUselessEdges()
	{
		Set<E> removedEdges = new HashSet<E>();
		if(!rule.isPreprocessed())
		{
			RulePreprocessService<V, E> rulePreprocessService = new RulePreprocessService<V, E>(rule);
			rulePreprocessService.getAdjMatrixByType("1");
			rulePreprocessService.removeUselessEdgesByType("1");
			rulePreprocessService.removeUselessVertices();
		}
		
		Set<E> rmEdgesInRule = rule.getRemovedEdges();
		Iterator<E> it = rmEdgesInRule.iterator();
		while(it.hasNext())
		{
			E e = it.next();
			/**
			 * Remove edge in the pattern graph
			 */
			removedEdges.addAll(this.removeEdgeInPatternGraph(e));
			/**
			 * Remove edge in the delta graph
			 */
			removedEdges.addAll(this.removeEdgeInDeltaGraph(e));
		}
		/**
		 * Remove singleton in the delta graph
		 */
		//this.removeSingletons((PatternGraph<V, E>)violation.getDeltaGraph());
		return removedEdges;
	}
	
	public Set<V> removeUselessVertices()
	{
		Set<V> removedVertices = new HashSet<V>();
		if(!rule.isPreprocessed())
		{
			RulePreprocessService<V, E> rulePreprocessService = new RulePreprocessService<V, E>(rule);
			rulePreprocessService.getAdjMatrixByType("1");
			rulePreprocessService.removeUselessEdgesByType("1");
			rulePreprocessService.removeUselessVertices();
		}
		Set<V> rmVerticesInRule = rule.getRemovedVertices();
		Iterator<V> it = rmVerticesInRule.iterator();
		while(it.hasNext())
		{
			V v = it.next();
			V removedVertexInPattern = removeVertexInPatternGraph(v);
			V removedVertexInDeltaG = removeVertexInDeltaGraph(v);
			
			if(removedVertexInPattern!=null)
				removedVertices.add(removedVertexInPattern);
			if(removedVertexInDeltaG!=null)
				removedVertices.add(removedVertexInDeltaG);
		}
		/**
		 * Remove singleton in the delta graph
		 */
		//this.removeSingletons((PatternGraph<V, E>)violation.getDeltaGraph());
		return removedVertices;
	}
	
	private void removeSingletons(PatternGraph<V, E> patternGraph)
	{
		/**
		 * TODO: bug fix
		 */
		Set<V> vertexSet = patternGraph.getAllvertices();
		Iterator<V> iterator = vertexSet.iterator();
		while(iterator.hasNext())
		{
			V vertex = iterator.next();
			int inDegree = patternGraph.getOutgoingEdges(vertex).size();
			int outDegree = patternGraph.getIncomingEdges(vertex).size();
			if(inDegree == 0 && outDegree == 0)
				 patternGraph.removeVertex(vertex);
		}
		
		return;
	}
	
	private Set<E> removeEdgeInPatternGraph(E e)
	{
		Set<E> ret = new HashSet<E>();
		V src = e.getSrc();
		V tar = e.getTar();
		PatternGraph<V, E> patternGraph = (PatternGraph<V, E>)this.violation.getPatternGraph();
		V srcRM = violation.getVertexByIndex(src.getVertexKey());
		V tarRM = violation.getVertexByIndex(tar.getVertexKey());
		if(srcRM==null || tarRM==null)
			return ret;
		Set<E> removeEdge = patternGraph.getEdgeByType(srcRM, tarRM, e.getEdgeType());
		if(removeEdge.isEmpty())
			return ret;
		Iterator<E> itEdge = removeEdge.iterator();
		while(itEdge.hasNext())
		{
			E edge = itEdge.next();
			patternGraph.removeEdge(edge);
			ret.add(edge);
		}
		return ret;
	}
	
	private Set<E> removeEdgeInDeltaGraph(E e)
	{
		Set<E> ret = new HashSet<E>();
		V src = e.getSrc();
		V tar = e.getTar();
		PatternGraph<V, E> deltaGraph = (PatternGraph<V, E>) this.violation.getDeltaGraph();
		V srcRM = deltaGraph.getVertexByKey(src.getVertexKey());
		V tarRM = deltaGraph.getVertexByKey(tar.getVertexKey());
		if(srcRM==null || tarRM==null)
			return ret;
		Set<E> removeEdge = deltaGraph.getEdgeByType(srcRM, tarRM, e.getEdgeType());
		if(removeEdge.isEmpty())
			return ret;
		Iterator<E> itEdge = removeEdge.iterator();
		while(itEdge.hasNext())
		{
			E edge = itEdge.next();
			deltaGraph.removeEdge(edge);
			ret.add(edge);
		}
		return ret;
	}
	private V removeVertexInPatternGraph(V v)
	{
		V removedVertex = violation.getVertexByIndex(v.getVertexKey());
		if(removedVertex == null)
			return null;
		PatternGraph<V, E> patternGraph = (PatternGraph<V, E>)this.violation.getPatternGraph();
		patternGraph.removeVertex(removedVertex);
		return removedVertex;
		
	}
	private V removeVertexInDeltaGraph(V v)
	{
		PatternGraph<V, E> deltaGraph = (PatternGraph<V, E>)this.violation.getDeltaGraph();
		V removedVertex = deltaGraph.getVertexByKey(v.getVertexKey());
		if(removedVertex == null)
			return null;
		deltaGraph.removeVertex(removedVertex);
		return removedVertex;
	}
 
}
