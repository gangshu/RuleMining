package edu.cwru.eecs.statianalysis.graph;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.to.EdgesPo;

public class ProductGraph<V extends Vertex, E extends Edge<V>> {
	private PDGGraph<V, E> G1, G2;
	private int dim1, dim2;
	private long alpha;
	private ArrayList<V> vertexArrayG1 = new ArrayList<V>(), vertexArrayG2 = new ArrayList<V>();
	private double[][] E;
	/**
	 * This hashtable maps indes in the large matrix with dimension |G1||G2|*|G1||G2|
	 * to the to-be-produced matrix E, where each vertex is the vertex that DID appear in the product graph
	 */
	private Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
	/**
	 * Inverse hashtable of ht
	 */
	private Hashtable<Integer, Integer> ht_i = new Hashtable<Integer, Integer>();	
	private List<EdgesPo> edgeList = new ArrayList<EdgesPo>();
	public ProductGraph(PDGGraph<V, E> G1, PDGGraph<V, E> G2)
	{
		this.G1 = G1;
		this.G2 = G2;
		Set<V> vertexSetG1 = G1.getAllvertices();
		Set<V> vertexSetG2 = G2.getAllvertices();
		//Init vertexArrayG1
		Iterator<V> iteratorG1 = vertexSetG1.iterator();
		while(iteratorG1.hasNext())
		{
			vertexArrayG1.add(iteratorG1.next());
		}
		//Init vertexArrayG2
		Iterator<V> iteratorG2 = vertexSetG2.iterator();
		while(iteratorG2.hasNext())
		{
			vertexArrayG2.add(iteratorG2.next());
		}
		this.dim1 = vertexArrayG1.size();
		this.dim2 = vertexArrayG2.size();
		this.createProductGraph();
		
	}
	
	public double[][] getAdjacencyMatris()
	{
		return this.E;
	}
	
	private void createProductGraph()
	{

		/**
		 * 1. Initialize the matrix with dimension |G1||G2|*|G1||G2|
		 */
		int dim = dim1*dim2;
		//E = new double[dim][dim];
		/**
		 * 2. Computer vertex kernels
		 * kernelVertex(i, j) = kernelVertexType(i, j)*kernelAST(i, j)
		 */
		double[][] vertexKernelValues = new double[this.dim1][this.dim2];
		for(int i=0; i<this.dim1;i++)
			for(int j=0; j<this.dim2;j++)
			{
				V vG1 = this.vertexArrayG1.get(i);
				V vG2 = this.vertexArrayG2.get(j);
				vertexKernelValues[i][j]= this.kernelVertex(vG1, vG2); 
			}
		/**
		 * 3. Construct adjacency matrix for both G1 and G2
		 * Adjacency matrix contains type of edge
		 * Note that G1 and G2 might be multi graph
		 */
		//adjacency matric for G1
		String[][]adjG1 = this.getAdjacencyMatrix(dim1, vertexArrayG1, G1);
		String[][]adjG2 = this.getAdjacencyMatrix(dim2, vertexArrayG2, G2);

		
		/**
		 * 3. For each entry in E, compute kernel:
		 * E[i][j] = kernelVertex(g1_i, g2_i)*kernelVertex(g1_j,g2_j)*kernelEdge((g1_i, g1_j), (g2_i,g2_j))
		 * 3.1 Also get in and out degrees for each vertex
		 */
		int cur_v_idx = 0;
		Hashtable<Integer,Long> outDegree = new Hashtable<Integer, Long>();
		Hashtable<Integer,Long> inDegree = new Hashtable<Integer, Long>();
		for(int i=0; i<dim; i++)
			for(int j=0; j<dim; j++)
			{
				/**
				 * In the product graph, (i, j) --> ((v_g1_i, v_g2_i),(v_g1_j, v_g2_j))
				 */
				int g1_i = this.getVertexIdxG1(i);
				int g2_i = this.getVertexIdxG2(i);
				int g1_j = this.getVertexIdxG1(j);
				int g2_j = this.getVertexIdxG2(j);
				String e1Type = adjG1[g1_i][g1_j];
				String e2Type = adjG2[g2_i][g2_j];
				double tmp = vertexKernelValues[g1_i][g2_i]*vertexKernelValues[g1_j][g2_j]*this.kernelEdgeSimple(e1Type, e2Type);
				if(tmp!=0)
				{
					/**
					 * Get the edge and vertices
					 */
					int src, tar;
					EdgesPo edge = new EdgesPo();
					//get src
					if(ht.containsKey(i))
						src=ht.get(i);
					else
					{
						src = cur_v_idx;
						ht.put(i, src);
						ht_i.put(src, i);
						cur_v_idx++;						
					}
					//get tar
					if(ht.containsKey(j))
						tar=ht.get(j);
					else
					{
						tar = cur_v_idx;
						ht.put(j, tar);
						ht_i.put(tar, j);
						cur_v_idx++;						
					}
					//get edge
					edge.setSrcVertexKey(src);
					edge.setTarVertexKey(tar);
					edge.setEdgeType(String.valueOf(tmp));
					edgeList.add(edge);
					//out degree for i, indegree for j
					Long out = outDegree.get(i);
					
					if(out == null)
						outDegree.put(i, (long)1);
					else 
					{
						/**
						 * TODO: find a more elegant solution
						 */
						if(out<Long.MAX_VALUE)
						{
							out++;
						}
						else
							out = Long.MAX_VALUE;
						outDegree.remove(i);
						outDegree.put(i, out);
						//System.out.println("out: "+out);
					}
					//indegree for j
					Long in = inDegree.get(j);
					if(in==null)
						inDegree.put(j, (long)1);				
					else
					{
						if(in<Long.MAX_VALUE)
						{
							in++;
						}
						else
						{
							in = Long.MAX_VALUE;
							System.out.println("Indegree exceeds Long.MAX_VALUE! Kernel could potentially be wrong!");
						}
						inDegree.remove(j);
						inDegree.put(j, in);
						//System.out.println("in: "+in);
					}
				}
			
			}
		/**
		 * 4. Get Maximum out and in degree for each vertex; then get the minimum of the two
		 */
		long maxIndegree = this.getMax(inDegree);;
		long maxOutdegree = this.getMax(outDegree);
		if(maxIndegree>maxOutdegree)
			this.alpha=maxOutdegree;
		else
			this.alpha=maxIndegree;
		//System.out.println(maxIndegree+" "+maxOutdegree);
		/**
		 * 5. Get matrix E
		 */
		int dimE = ht.size();
		E = new double[dimE][dimE];
		for(int i=0; i<edgeList.size();i++)
		{
			EdgesPo edge = edgeList.get(i);
			double type = Double.valueOf(edge.getEdgeType());
			E[edge.getSrcVertexKey()][edge.getTarVertexKey()] = type;
		}

	}
	private long getMax(Hashtable<Integer,Long> degree)
	{
		/**
		 * Very important input check!
		 */
		if(degree.isEmpty())
			return 0;
		long ret = Long.MIN_VALUE;
		Enumeration<Long> values = degree.elements();
		while(values.hasMoreElements())
		{
			long x = values.nextElement();
			//System.out.println(x);
			if(x>ret)
				ret = x;
		}
		return ret;
	
		
	}
	public Long getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public List<Integer> getKeyFromMatrixIndex(int i)
	{
		int orig_idx = ht_i.get(i);
		int v1_idx = this.getVertexIdxG1(orig_idx);
		int v2_idx = this.getVertexIdxG2(orig_idx);
		Vertex v1 = this.vertexArrayG1.get(v1_idx);
		Vertex v2 =this.vertexArrayG2.get(v2_idx);
		List<Integer> ret= new ArrayList<Integer>();
		ret.add(v1.getVertexKey());
		ret.add(v2.getVertexKey());
		return ret;
	}

	private double kernelVertex(V v1, V v2)
	{
		return this.kernelVertexType(v1, v2)*this.kernelAST(v1, v2);
	}
	private double kernelEdgeSimple(String e1Type, String e2Type)
	{
		if(e1Type == null || e2Type == null)
			return (double)0;
		if(e1Type.equals(e2Type))
			return (double)1;
		return (double)0;
	}
	private int kernelEdge(E e1, E e2)
	{
		return this.kernelEdgeType(e1, e2);
	}
	private double kernelVertexType(V v1, V v2)
	{
		if(v1.getVertexKindId().equals(v2.getVertexKindId()))
			return (double)1;
		return (double)0;
	}
	public int getDim2() {
		return dim2;
	}

	public void setDim2(int dim2) {
		this.dim2 = dim2;
	}

	private int kernelEdgeType(E e1, E e2)
	{
		if(e1.getEdgeType().equals(e2.getEdgeType()))
			return 1;
		return 0;
	}
	private double kernelAST(V v1, V v2)
	{
		//TODO implement this
		return (double)1;
	}
	private int getVertexIdxG1(int idx)
	{
		return idx/dim2;
		
	}
	private int getVertexIdxG2(int idx)
	{
		return idx%dim2;
	}
	
	private String[][] getAdjacencyMatrix(int dim, ArrayList<V> vertexArray, PDGGraph<V, E> graph)
	{
		String[][] adjG = new String[dim][dim];
		for(int i=0; i<dim; i++)
			for(int j=0; j<dim;j++)
			{
				V src = vertexArray.get(i);
				V tar = vertexArray.get(j);
				if(graph.containsEdge(src, tar))
				{
					Set<E> edgeSet = graph.getEdge(src, tar);
					Iterator<E> it = edgeSet.iterator();
					int count = 0;
					E edge =null;
					while(it.hasNext())
					{
						edge = it.next();
						count++;
						if(count>1)
						{
							System.out.println("ProductGraph.createProductGraph: multi edges between "+src.getVertexKey()+" "+tar.getVertexKey());
						}
					}
					adjG[i][j]=edge.getEdgeType();					
				}
				else
					adjG[i][j]=null;
			}
		return adjG;
	}

}
