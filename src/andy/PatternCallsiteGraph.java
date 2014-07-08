package andy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PatternCallsiteGraph extends CallsiteGraph {

    public PatternCallsiteGraph(int vertex_key, Connection conn, Pattern pattern, boolean isCallsite) throws SQLException  {
        super(vertex_key,conn,isCallsite);
        super.assignCsgNodes(conn,pattern.getPattern_vertices_hash());
    }

    private int computeMissedNode(IdsCallsiteGraph idsCsg) {
        int missed_node = 0;
        Hashtable idsCsg_node_hash = idsCsg.getCsgNodesHash();
        Enumeration enum1 = this.csg_nodes_list.elements();
        while (enum1.hasMoreElements()) {
            Vertex node = (Vertex) enum1.nextElement();
            Object obj = idsCsg_node_hash.get(new Integer(node.getVertex_label()));
            if (obj == null) missed_node++; 
        }
        return missed_node;
    }


    private void computeMissedRequiredEdge(Vector required_edges, Pattern pattern, IdsCallsiteGraph idsCsg, int[] mapping, 
                                          IDS ids, int[][][] rids_edges, MissedInformation missed_info) {

        Enumeration enum1 = required_edges.elements();
        while (enum1.hasMoreElements()) {
            RequiredEdge edge = (RequiredEdge) enum1.nextElement();
            int src_node_idx = edge.getSrc_node_index();
            int tar_node_idx = edge.getTar_node_index();
            Vertex src_node = pattern.getVertexByIndex(src_node_idx);
            Vertex tar_node = pattern.getVertexByIndex(tar_node_idx);
            int edge_type = edge.getEdge_type();

            Vertex mapped_src_node;
            Vertex mapped_tar_node;
            if (edge.getSrc_is_csg_node()) {  // mapped_src_node should be in IDS_CALLSITEGRAPH
                mapped_src_node = (Vertex) idsCsg.getCsgNodesHash().get(new Integer(src_node.getVertex_label()));
                mapped_tar_node = ids.getVertexByKeyFromRids(mapping[tar_node_idx]);
            } else {
                mapped_src_node = ids.getVertexByKeyFromRids(mapping[src_node_idx]);
                mapped_tar_node = (Vertex) idsCsg.getCsgNodesHash().get(new Integer(tar_node.getVertex_label()));
            }
            
            boolean missed_edge = false;
            
            if (mapped_src_node != null && mapped_tar_node != null) {
                if (rids_edges[mapped_src_node.getRids_node_index()][mapped_tar_node.getRids_node_index()][edge_type]<=0) {
                    missed_edge = true;
                } else {
                    if (edge_type == 2) {
                        String mapped_sdd_info = this.getSddInfo(mapped_src_node,mapped_tar_node);
                        /**
                         * Bug fix: Null pointer
                         */
                        if (mapped_sdd_info!=null &&!mapped_sdd_info.equals(edge.getSDD_info())) {
                            missed_edge = true;
                        }
                    }
                }
            } else {
                missed_edge = true;
            }
            
            if (missed_edge) {
                if (mapped_src_node == null) {
                    switch (edge_type) {
                        case 0: missed_info.addMissedDataEdge();  break;
                        case 1: missed_info.addMissedControlEdge();  break;
                        case 2: missed_info.addMissedSddEdge();; break;
                    }
                }
            }    
        }    
    
    }

    public void findMissedInfo(IDS ids, int[][][] rids_edges, int[][][] rids_closures, IdsCallsiteGraph idsCsg, int[] mapping, 
                               Hashtable has_used, Pattern pattern, MissedInformation missed_info) {
        missed_info.assignMissedNode(computeMissedNode(idsCsg));
        Vector required_edges = this.computeRequiredEdges(mapping,pattern);     
        this.computeMissedRequiredEdge(required_edges,pattern,idsCsg,mapping,ids,rids_edges,missed_info); 
        this.computeMissedClosures(pattern,idsCsg,mapping,ids,has_used,missed_info,rids_closures);
    }


    private void computeMissedClosures(Pattern pattern, IdsCallsiteGraph idsCsg, int[] mapping, IDS ids, Hashtable has_used, MissedInformation missed_info, int[][][] rids_closures) {
        int[][][] pattern_closures = pattern.getPatternClosures();
        Enumeration enum1 = this.csg_nodes_list.elements();
        while (enum1.hasMoreElements()) {
            Vertex src_node = (Vertex) enum1.nextElement(); // in pattern           
            Vertex mapped_src_node = (Vertex) idsCsg.csg_nodes_hash.get(new Integer(src_node.getVertex_label())); //in IDS
            if (mapped_src_node != null) {
                Hashtable src_node_edges = this.findEdgeIncidentWithNode(src_node,pattern,mapping,pattern_closures);
                Hashtable mapped_src_node_edges = idsCsg.findEdgeIncidentWithNode(ids,rids_closures,has_used,mapped_src_node);
                Enumeration enum2 = src_node_edges.elements();
                while (enum2.hasMoreElements()) {
                    MissedEdge pattern_edge = (MissedEdge) enum2.nextElement();
                    String key = pattern_edge.getKey();
                    int pattern_edge_num = pattern_edge.getEdgeNumber();
                    MissedEdge ids_edge = (MissedEdge) mapped_src_node_edges.get(key);
                    int edge_type = pattern_edge.getEdgetype();
                    int missed_edge_num = 0;
                    if (ids_edge != null) {
                        int rids_edge_num = ids_edge.getEdgeNumber();
                        missed_edge_num = pattern_edge_num - rids_edge_num;
                    } else {
                        missed_edge_num = pattern_edge_num;
                    }
                    if (missed_edge_num > 0) {
                        switch (edge_type) {
                            case 0: missed_info.addMissedDataEdge(missed_edge_num); break;
                            case 1: missed_info.addMissedControlEdge(missed_edge_num); break;
                            case 2: missed_info.addMissedSddEdge(missed_edge_num); break;
                        }
                    }                    
                }
            }
        }
    }

    private Hashtable findEdgeIncidentWithNode(Vertex src_node, Pattern pattern, int[] mapping, int[][][] closures) {
        int pattern_vertices_num = pattern.getPattern_vertices_num();
        Hashtable src_node_edges = new Hashtable();
        for (int i=0; i<pattern_vertices_num; i++) {
            if (mapping[i] <= 0 && src_node.getNode_index() != i) { // Node with index i has not been mapped
                Vertex tar_node = pattern.getVertexByIndex(i);
                for (int k=0; k<=2; k++) {
                    if (closures[src_node.getNode_index()][i][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(tar_node.getVertex_label(),k,true,this.getSddInfo(src_node,tar_node));
                        else        edge = new MissedEdge(tar_node.getVertex_label(),k,true,this.getSddInfo(src_node,tar_node));
                        countEdgeNumber(src_node_edges,edge);
                    }
                    if (closures[i][src_node.getNode_index()][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(tar_node.getVertex_label(),k,false,this.getSddInfo(src_node,tar_node));
                        else        edge = new MissedEdge(tar_node.getVertex_label(),k,false,this.getSddInfo(src_node,tar_node));
                        countEdgeNumber(src_node_edges,edge);
                    }                    
                }
            }
        }
        return src_node_edges;
    }

    
    public Vector computeRequiredEdges(int[] mapping, Pattern pattern) {
        Vector required_edges = new Vector();
        int pattern_vertices_num = pattern.getPattern_vertices_num();
        int [][][] pattern_edges = pattern.getPattern_vertices_edges();
        
        Enumeration csg_node_enum  = csg_nodes_list.elements();
        while (csg_node_enum.hasMoreElements()) {
            Vertex csg_node = (Vertex) csg_node_enum.nextElement();
            int csg_node_index = csg_node.getNode_index();
            for (int adj_idx=0; adj_idx<pattern_vertices_num; adj_idx++) {
                 if (csg_node_index != adj_idx) {
                     Vertex adj_node = pattern.getVertexByIndex(adj_idx);
                     if (mapping[adj_idx] > 0) {
                         this.addRequiredEdge(pattern_edges,csg_node,adj_node,required_edges);
                     } 
                 }
            }
        }        
        return required_edges;
    }

    private void addRequiredEdge(int[][][] pattern_edges, Vertex csg_node, Vertex adj_node, Vector required_edges) {
        for (int k=0; k<=2; k++) {
            if (pattern_edges[csg_node.getNode_index()][adj_node.getNode_index()][k] > 0) {
                RequiredEdge edge = new RequiredEdge(csg_node.getNode_index(),adj_node.getNode_index(),k,true);
                if (k == 2) edge.setSDD_info(getSddInfo(csg_node,adj_node));
                required_edges.add(edge);
            }
            if (pattern_edges[adj_node.getNode_index()][csg_node.getNode_index()][k] > 0) {
                RequiredEdge edge = new RequiredEdge(adj_node.getNode_index(),csg_node.getNode_index(),k,false);
                if (k == 2) edge.setSDD_info(getSddInfo(adj_node,csg_node));
                required_edges.add(edge);
            }
        }
    }
    
}
