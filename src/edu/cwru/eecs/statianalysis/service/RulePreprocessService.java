package edu.cwru.eecs.statianalysis.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenu;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PatternGraph;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import Jama.Matrix;
public class RulePreprocessService <V extends Vertex, E extends Edge<V>> {
	
	Rule<V, E> rule;
	static int maxIter = 5;
	
	public RulePreprocessService(Rule<V, E> r) {
		this.rule=r;
	}
	
	/**
	 * 
	 * Remove useless Edges; useless edges are transitive edges for which corresponding path exists in the rule pattern.
	 * 
	 * @param type
	 * @return removed edges
	 */
	public Set<E> removeUselessEdgesByType(String type)
	{
		PatternGraph<V, E> patternGraph = (PatternGraph<V, E>)rule.getPatternGraph();
		Set<E> ret = new HashSet<E>();
		double[][] adjMatrix = this.getAdjMatrixByType(type);
		Matrix matrix = new Matrix(adjMatrix);
		for(int k=0; k<maxIter; k++)
		{			
			matrix = matrix.times(matrix); //matrix[i][j] = 1 if there is a path from i to j with length k
			for(int i=0; i<matrix.getRowDimension(); i++)
				for(int j=0; j<matrix.getColumnDimension();j++)
				{
					if(matrix.get(i, j)==1.0 && adjMatrix[i][j]==1)
					{
						V src = patternGraph.getVertexByKey(i);
						V tar = patternGraph.getVertexByKey(j);
						Set<E> edgeSet = patternGraph.getEdgeByType(src, tar, type);
						if(edgeSet.isEmpty())
						{
							System.out.println("RulePreprocessService.removeUselessEdgesByType: cannot find edge between "
									           +src.getVertexKey()+" "+tar.getVertexAst()+" of type "+type+" in the rule "+rule.getPatternKey());
							return null;
						}
						/**
						 * There actually ould not be multiple transitive edges.
						 */
						Iterator<E> it = edgeSet.iterator();						
						while(it.hasNext())
						{
							E e = it.next();
							patternGraph.removeEdge(e);
							ret.add(e);
						}
						/**
						 * TODO: remove
						 */
						if(edgeSet.size()>1)
							System.out.println(rule.getPatternKey()+": multi edges!");
					}
				}
		}
		
		/**
		 * Set certain fields of the rule
		 */
		rule.setPreprocessed(true);
		rule.setRemovedEdges(ret);
		return ret;
		
	}
	
	public Set<V> removeUselessVertices()
	{
		PatternGraph<V, E> patternGraph = (PatternGraph<V, E>)rule.getPatternGraph();
		/**
		 * Bug fix: should not make changes to Collection during iteration; so created a new set for it
		 */
		Set<V> vertexSet = new HashSet<V>(); 
		vertexSet.addAll(patternGraph.getAllvertices());
		Set<V> retSet = new HashSet<V>();
		Iterator<V> it = vertexSet.iterator();
		while(it.hasNext())
		{
			V v=it.next();
			int inDegree=patternGraph.getIncomingEdges(v).size();
			int outDegree=patternGraph.getOutgoingEdges(v).size();
			if((v.getVertexKindId().equals("1")||v.getVertexKindId().equals("2"))&&inDegree == 1 && outDegree == 0)
			{
				int vertexKey = v.getVertexKey();
				V vRm = patternGraph.getVertexByKey(vertexKey);
				patternGraph.removeVertex(vRm);
				retSet.add(vRm);
			}
		}		
		/**
		 * Set certain fields of the rule
		 */
		rule.setPreprocessed(true);
		rule.setRemovedVertices(retSet);
		return retSet;
	}
	
	public double[][] getAdjMatrixByType(String type)
	{
		PatternGraph<V, E> patternGraph = (PatternGraph<V, E>)rule.getPatternGraph();
		Set<V> vertexSet = patternGraph.getAllvertices();
		double[][] ret = new double[vertexSet.size()][vertexSet.size()];
		for(int i=0; i<vertexSet.size(); i++)
			for(int j=0; j<vertexSet.size(); j++)
			{
				V src = patternGraph.getVertexByKey(i);
				V tar = patternGraph.getVertexByKey(j);
				Set<E> edgeSet = patternGraph.getEdgeByType(src, tar, type);
				if(!edgeSet.isEmpty())
					ret[i][j]=1;
			}
		
		return ret;
		
	}
	
}
