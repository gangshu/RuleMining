package edu.cwru.eecs.statianalysis.service;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;
import javax.swing.JFrame;
import javax.swing.ListModel;

import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.graph.DefaultPDGGraph;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.pattern.BugFixHelper;
import y.view.*;
import y.algo.GraphConnectivity;
import y.view.LineType;

 
import y.base.*;
import y.layout.hierarchic.HierarchicLayouter;
import  y.layout.Layouter;
import edu.cwru.eecs.statianalysis.pattern.PDG;
public class GraphService {
		
	public Graph2D transformToYGraph(PDGGraph<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> pdgGraph)
	{
		if(pdgGraph==null)
			return null;
        Graph2D pattern_y;
        pattern_y = new Graph2D();
        Hashtable<Integer,Node> ht = new Hashtable<Integer,Node>();
        NodeLabel nl = new NodeLabel();
        java.awt.geom.Rectangle2D.Double rd;
        ShapeNodeRealizer nr = new ShapeNodeRealizer();
        DefaultLabelConfiguration dlc = new DefaultLabelConfiguration();  
        //Create nodes
        Set<Vertex>vs = pdgGraph.getAllvertices();
        Iterator<Vertex> v_it = vs.iterator();
        while(v_it.hasNext())
        {
            Vertex v = v_it.next();
            String text = v.getVertexKey()+" "+v.getVertexCharacters()+"("+v.getVertexKindId()+")";
            Node node = pattern_y.createNode();
            ht.put(v.getVertexKey(), node);
            nr = (ShapeNodeRealizer)pattern_y.getRealizer(node);
            nl = nr.getLabel();
            nl.setText(text);           
            rd = nr.getBoundingBox(); 
            dlc.calcUnionRect(nl, rd);
            nr.setFrame(rd);            
        }
        
        //Create edges
        //Create edges
        Set<edu.cwru.eecs.statianalysis.data.Edge<Vertex>> es=pdgGraph.getAllEdges();
        Iterator<edu.cwru.eecs.statianalysis.data.Edge<Vertex>> e_it = es.iterator();
        while(e_it.hasNext())
        {
        	edu.cwru.eecs.statianalysis.data.Edge<Vertex> edge = e_it.next();
            Node src = ht.get(edge.getSrc().getVertexKey());
            Node tar = ht.get(edge.getTar().getVertexKey());
            y.base.Edge edge_y = pattern_y.createEdge(src, tar);
            /*
            if(edge.getType().equals("1"))
                graph_y.getRealizer(edge_y).setArrow(Arrow.DELTA);
            else
                graph_y.getRealizer(edge_y).setArrow(Arrow.WHITE_DELTA);
             */
            pattern_y.getRealizer(edge_y).setArrow(Arrow.DELTA);
            
            if(edge.getEdgeType().equals("0"))
                pattern_y.getRealizer(edge_y).setLineColor(Color.green);
            if(edge.getEdgeType().equals("1"))
                pattern_y.getRealizer(edge_y).setLineColor(Color.blue);
            if(edge.getEdgeType().equals("2"))
            	pattern_y.getRealizer(edge_y).setLineColor(Color.orange);
        }
        return pattern_y;
	}
	
	public void visualize(List<Graph2D> yGraphs,String text)
	{
		if(yGraphs == null)
			return;
        JFrame f = new JFrame(text);
        f.setLayout(new FlowLayout());
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();  
        int x = (screenSize.width - f.getWidth()) / 2;  
        int y = (screenSize.height - f.getHeight()) / 2; 
        f.setLocation(x, y);  
		for(int i=0; i<yGraphs.size(); i++)
		{
			Graph2D yGraph = yGraphs.get(i);
            visualizeGraph(yGraph, f);

			/**
			if(i==0)
	        f.add(g2dv, BorderLayout.WEST);
			if(i==1)
				f.add(g2dv, BorderLayout.CENTER);
			if(i==2)
				f.add(g2dv, BorderLayout.EAST);
				*/
		}          
        f.pack();
        f.setVisible(true);       
	}
	
	public void visualizeGraph(Graph2D yGraph, JFrame f)
	{
		if(yGraph == null)
			return;
		Graph2DView g2dv;
		//Visuaslize
		Layouter hl = new HierarchicLayouter();
		hl.doLayout(yGraph);            
		g2dv = new Graph2DView(yGraph); 
		EditMode em = new EditMode();
		em.allowNodeCreation(false);
		g2dv.addViewMode(em);        
		g2dv.setSize(1000, 700);
		//TODO: make this more elegant
		f.add(g2dv);
	}
	
	public void visualizeFromPdgGraph(List<PDGGraph<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>>> pdgGraphs, String text)
	{
		List<Graph2D> yGraphs = new ArrayList<Graph2D>();
		for(int i=0; i<pdgGraphs.size(); i++)
		{
			PDGGraph<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> pdgGraph = pdgGraphs.get(i);
			Graph2D yGraph = this.transformToYGraph(pdgGraph);
			yGraphs.add(yGraph);
 		}
		this.visualize(yGraphs, text);
	}
	
	public void visualizeListOfPdgGraphs(List<String> pdgs,DataSource dataSource)
	{
		BugFixHelper<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> bfHelper = new BugFixHelper<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>>();
		List<PDGGraph<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>>> pdgGraphs = new ArrayList<PDGGraph<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>>>();
		for(int i=0; i<pdgs.size();i++)
		{
			String pdgId = pdgs.get(i);
			PDG<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> pdg = bfHelper.createPDG(pdgId, dataSource);
			pdgGraphs.add(pdg.getPdgGraph());
		}
		this.visualizeFromPdgGraph(pdgGraphs, "Visualize PDG grahps");
	}
	
	public void visualizeSinglePDGGraph(String pdgId,DataSource dataSource, String text)
	{
		BugFixHelper<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> bfHelper = new BugFixHelper<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>>();
		PDG<Vertex, edu.cwru.eecs.statianalysis.data.Edge<Vertex>> pdg = bfHelper.createPDG(pdgId, dataSource);
		Graph2D yGraph = this.transformToYGraph(pdg.getPdgGraph());
		
        JFrame f = new JFrame(text);
        //f.setLayout(new FlowLayout());
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();  
        int x = (screenSize.width - f.getWidth()) / 2;  
        int y = (screenSize.height - f.getHeight()) / 2; 
        f.setLocation(x, y);  
        
        visualizeGraph(yGraph, f);          
        f.pack();
        f.setVisible(true); 
	}
	

	public void demoPdgList()
	{
		//Create data source using DB username
		DBUtil.createDatasource("og");
		//Get a list of PDGs to be displayed
		List<String> pdgs = new ArrayList<String>();
		pdgs.add("-1017");
		//Visualize PDGs
		this.visualizeListOfPdgGraphs(pdgs, DBUtil.getDataSource());
	}
	
	public void demoPdgSingle()
	{
		DBUtil.createDatasource("og");
		String pdgId = "-1017";
		this.visualizeSinglePDGGraph(pdgId, DBUtil.getDataSource(), "visualize "+pdgId);
	}
}
