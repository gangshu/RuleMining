package edu.cwru.eecs.statianalysis.pattern.display;

import javax.sql.DataSource;
import java.util.*;
import edu.cwru.eecs.statianalysis.data.*;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;

public class DisplayVioInstance <V extends Vertex, E extends Edge<V>, R extends Rule<V,E>> extends DisplayPatternInstance<V, E, R> {
	Violation<V, E> vio;
	R r;
	String src;
	DataSource ds;
	String textToDisplay;
	public DisplayVioInstance(R r, String src, DataSource ds)
	{
		super(r, src, ds);		
	}
	
	public void setViolatoin(Violation<V, E> vio)
	{
		this.vio = vio;
	}
	
	@Override
	public void getPdgVertices()
	{
		Set<V> vSet = vio.getPatternGraph().getAllvertices();
		Iterator<V> itV = vSet.iterator();
		
		/**
		 * 1. Group vertices into groups of PDGs
		 */
		while(itV.hasNext())
		{
			V v = itV.next();
			String pdgId = v.getPdgId();
			Set<V> pdgVSet = this.pdgVertices.get(pdgId);
			if(pdgVSet == null)
			{
				pdgVSet = new HashSet<V>();
				pdgVertices.put(pdgId, pdgVSet);
			}
			pdgVSet.add(v);
		}
		
		/**
		 * 2. Sort vertices in each PDG according to their starting line
		 */
		Set<String> pdgIdSet = this.pdgVertices.keySet();
		Iterator<String> pdgIdIt = pdgIdSet.iterator();
		while(pdgIdIt.hasNext())
		{
			String pdgId = pdgIdIt.next();
			Set<V> pdgVSet = this.pdgVertices.get(pdgId);
			List<V> sortedPdgVList = this.getSortedVertex(pdgVSet);
			this.pdgVerticesSorted.put(pdgId, sortedPdgVList);
		}
		
		
	}

}
