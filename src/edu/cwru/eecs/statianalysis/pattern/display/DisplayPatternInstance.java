package edu.cwru.eecs.statianalysis.pattern.display;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import edu.cwru.eecs.statianalysis.util.ComparableVertex;
import edu.cwru.eecs.statianalysis.dao.PDGDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.PDGDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.RuleInstance;

public class DisplayPatternInstance <V extends Vertex, E extends Edge<V>, R extends Rule<V,E>>{
	RuleInstance<V, E> ri;
	R r;
	String src;
	String textToDisplay = "";
	PDGDao pdgDao;
	Hashtable<String, Set<V>> pdgVertices = new Hashtable<String, Set<V>>();
	Hashtable<String, List<V>> pdgVerticesSorted = new Hashtable<String, List<V>>();
	public DisplayPatternInstance(R r, String src, DataSource ds)
	{
		this.r = r;
		this.src = src;
		pdgDao = new PDGDaoSpringImpl(ds);	
	}
	
	public void setRuleInstance(RuleInstance<V, E> ri)
	{
		this.ri = ri;
	}
	
	public void computeDisplayText()
	{
		textToDisplay = "";
		this.pdgVertices.clear();
		this.pdgVerticesSorted.clear();
		this.getPdgVertices();
		Set<String> pdgIdSet = this.pdgVerticesSorted.keySet();
		Iterator<String> pdgIdIt = pdgIdSet.iterator();
		while(pdgIdIt.hasNext())
		{
			/**
			 * Get header
			 */
			String pdgId = pdgIdIt.next();
			String filename = pdgDao.getPdgFile(pdgId);
			String pdgName = pdgDao.getPdgName(pdgId);
			textToDisplay = textToDisplay + filename+"("+pdgName+")\n";
			
			/**
			 * get list of vertex lines to be displayed
			 */
			List<V> vList = this.pdgVerticesSorted.get(pdgId);
			List<VLines> vLines = new ArrayList<VLines>();
			//First vertex location
			V vFirst = vList.get(0);
			int startF = vFirst.getStartline();
			int endF = vFirst.getEndline();
			if(startF<=2)
				startF =1;
			else
				startF = startF-2;
			endF = endF+2;
			VLines cur = new VLines();
			cur.setStartline(startF);
			cur.setEndline(endF);
			vLines.add(cur);
			//All the rest vertex locations
			for(int i=1; i<vList.size(); i++)
			{
				V v = vList.get(i);
				int start = v.getStartline();
				int end = v.getEndline();
				if(start<=2)
					start =1;
				else
					start = start-2;
				end = end+2;
				if(start<=cur.getEndline() && end>cur.getEndline())
				{
					cur.setEndline(end);
				}
				if(start>cur.getEndline())
				{
					VLines nLine = new VLines();
					nLine.setStartline(start);
					nLine.setEndline(end);
					cur = nLine;
					vLines.add(nLine);
				}
			}
			
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(this.src+"/"+filename));
				int lineNum = 0;
				String line = null;
				VLines vLineCur= vLines.get(0);
				int vLineIdxCur = 0;
				while((line = br.readLine())!=null)
				{
					lineNum++;
					if(lineNum<=vLineCur.getEndline() && lineNum>=vLineCur.getSartline())
						textToDisplay = textToDisplay+"\t"+lineNum+line+"\n";
					if(lineNum == vLineCur.getEndline())
					{
						if(vLineIdxCur < vLines.size()-1)
						{
							vLineIdxCur++;
							vLineCur = vLines.get(vLineIdxCur);
							textToDisplay = textToDisplay+"\t\t ... ... ... ...\n";
						}
						else
							break;
					}
				}
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		
		}
	}
	
	public String getText()
	{
		return this.textToDisplay;
	}
	
	public void getPdgVertices()
	{
		Set<V> vSet = ri.getInstanceGraph().getAllvertices();
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
	
	@SuppressWarnings("unchecked")
	public List<V> getSortedVertex(Set<V> vSet)
	{
		Iterator<V> vIt = vSet.iterator();
		List<ComparableVertex<V>> vCompList = new ArrayList<ComparableVertex<V>>();
		List<V> vRetList = new ArrayList<V>();
		while(vIt.hasNext())
		{
			V v = vIt.next();
			ComparableVertex<V> vCmp = new ComparableVertex<V>(v);
			vCompList.add(vCmp);
		}
		Collections.sort(vCompList);
		for(int i=0; i<vCompList.size();i++)
		{
			vRetList.add(vCompList.get(i).getVertex());
		}
		return vRetList;
	}
	
	public Hashtable<String, List<V>> getSortedPDGVList()
	{
		return this.pdgVerticesSorted;
	}

}

class VLines
{
	private int startLine;
	private int endLine;
	
	public int getSartline()
	{
		return startLine;
	}
	
	public void setStartline(int startLine)
	{
		this.startLine = startLine;
	}
	
	public int getEndline()
	{
		return endLine;
	}
	
	public void setEndline(int endLine)
	{
		this.endLine = endLine;
	}
}
