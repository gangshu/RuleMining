package andy;
import java.sql.*;
import java.util.*;
public class IdsCallsiteGraph extends CallsiteGraph {
    public IdsCallsiteGraph(int vertex_key, Connection conn, boolean isCallsite, IDS ids) throws SQLException{
        super(vertex_key,conn,isCallsite);
        super.assignCsgNodes(conn,ids.getRIds_vertices_hash());
    }
    
    public Hashtable findEdgeIncidentWithNode(IDS ids, int[][][] closures, Hashtable has_used, Vertex mapped_src_node) {
    
        int rids_vertices_num = ids.getRids_vertices_num();
        int[] rids_vertices_list = ids.getRids_vertices_list();
        Hashtable mapped_src_node_edges = new Hashtable();
        for (int i=0; i<rids_vertices_num; i++) {
            int mapped_tar_key = rids_vertices_list[i];
            Vertex mapped_tar_node = ids.getVertexByKeyFromRids(mapped_tar_key);
            Object obj = has_used.get(new Integer(mapped_tar_key));
            if (obj == null && mapped_src_node.getVertex_key() != mapped_tar_node.getVertex_key() ) { // Node with index i has not been mapped
                for (int k=0; k<=2; k++) {
                    if (closures[mapped_src_node.getRids_node_index()][i][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,true,super.getSddInfo(mapped_src_node,mapped_tar_node));
                        else        edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,true);
                        countEdgeNumber(mapped_src_node_edges,edge);
                    }
                    if (closures[i][mapped_src_node.getRids_node_index()][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,false,super.getSddInfo(mapped_src_node,mapped_tar_node));
                        else        edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,false);
                        countEdgeNumber(mapped_src_node_edges,edge);
                    }                    
                }
            }
        }
        return mapped_src_node_edges;               
    }
}
