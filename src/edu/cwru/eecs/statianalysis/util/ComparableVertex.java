package edu.cwru.eecs.statianalysis.util;

import edu.cwru.eecs.statianalysis.data.Vertex;

public class ComparableVertex <V extends Vertex> implements Comparable{

	V v;
	public ComparableVertex (V vertex)
	{
		v = vertex;
	}
	
	@SuppressWarnings("unchecked")
	public ComparableVertex(Object o)
	{
		v = (V)o;
	}
	
	public V getVertex()
	{
		return v;
	}
	@Override
	public int compareTo(Object o) {
		ComparableVertex v2 = (ComparableVertex)o;
		if(v.getStartline()<v2.getVertex().getStartline())
			return -1;
		else if(v.getStartline()==v2.getVertex().getStartline())
			return 0;
		else
			return 1;
	}
	
}
