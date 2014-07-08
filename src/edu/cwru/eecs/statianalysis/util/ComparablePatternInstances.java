package edu.cwru.eecs.statianalysis.util;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.pattern.RuleInstance;

public class ComparablePatternInstances<V extends Vertex, E extends Edge<V>, RI extends RuleInstance<V, E>> implements Comparable{

	RI ri;
	public ComparablePatternInstances(RI ruleInstance)
	{
		this.ri = ruleInstance;
	}
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Object o) {
		ComparablePatternInstances<V, E, RI> cri= (ComparablePatternInstances<V, E, RI>)o;
		RI ri2 = cri.getRuleInstance();
		if(ri.getNumFunctionsCrossed()<ri2.getNumFunctionsCrossed())
			return -1;
		else if(ri.getNumFunctionsCrossed()==ri2.getNumFunctionsCrossed())
		{
			if(ri.getInstanceGraph().getAllvertices().size()<ri2.getInstanceGraph().getAllvertices().size())
				return -1;
			else if(ri.getInstanceGraph().getAllvertices().size()==ri2.getInstanceGraph().getAllvertices().size())
				return 0;
			else 
				return 1;
		}
		else return 1;
	}
	
	public RI getRuleInstance()
	{
		return this.ri;
	}

}
