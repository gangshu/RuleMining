package andy;
import java.sql.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CallsiteGraph {

    static String load_param_sql = "SELECT * FROM edges e, vertex v WHERE e.edge_type = 2 " +
                                   "AND    e.src_vertex_key = ? AND v.vertex_key = e.tar_vertex_key " +
                                   "AND    v.vertex_kind_id IN (1,2) ";
    static String find_callsite_sql = "SELECT * FROM edges WHERE tar_vertex_key = ? and edge_type = 2 ";
    int callsite_node_vertex_key;
    Vector    csg_nodes_list = new Vector();
    Hashtable csg_nodes_hash = new Hashtable(); // Key is the vertex label of the node
    
    public CallsiteGraph(int vertex_key, Connection conn, boolean isCallsite) throws SQLException  {
        assignCallsite_Vertex_key(vertex_key,conn,isCallsite);    
    }
    
    public Hashtable getCsgNodesHash() {
        return this.csg_nodes_hash;
    }
    
    public Vector getCsgNodes_list() {
        return this.csg_nodes_list;
    }
    
    private void assignCallsite_Vertex_key(int vertex_key, Connection conn, boolean isCallsite) throws SQLException {
        if (isCallsite) {
            this.callsite_node_vertex_key = vertex_key;
        } else {
            PreparedStatement pstmt = conn.prepareStatement(find_callsite_sql);
            pstmt.setInt(1,vertex_key);
            ResultSet rset = pstmt.executeQuery();
            rset.next();
            this.callsite_node_vertex_key = rset.getInt("SRC_VERTEX_KEY");
            rset.close();
            pstmt.close();
        }
    }
    
    
    // valid_node_hash : rids_vertices_hash or pattern_vertices_hash
    public void assignCsgNodes(Connection conn, Hashtable valid_node_hash) throws SQLException {
        Vertex callsite_node = (Vertex) valid_node_hash.get(new Integer(this.callsite_node_vertex_key));
        if (callsite_node != null) { 
            this.csg_nodes_list.add(callsite_node);
            this.csg_nodes_hash.put(new Integer(callsite_node.getVertex_label()),callsite_node);
        }
        PreparedStatement pstmt = conn.prepareStatement(load_param_sql);
        pstmt.setInt(1,this.callsite_node_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            Integer param_key = new Integer(rset.getInt("VERTEX_KEY"));
            Vertex param_node = (Vertex) valid_node_hash.get(new Integer(param_key));
            if (param_node != null) {
                csg_nodes_hash.put(new Integer(param_node.getVertex_label()),param_node);
                csg_nodes_list.add(param_node);
            }
        }
        rset.close();
        pstmt.close();
    }    
    
    public void countEdgeNumber(Hashtable ht, MissedEdge edge) {
        String key = edge.getKey();
        MissedEdge me = (MissedEdge) ht.get(key);
        if (me == null) {
            ht.put(key,edge);
        } else {
            me.increaseEdgeNumber();
        }
    }    
    
    public String getSddInfo(Vertex src_node, Vertex tar_node) {
        String f1 = src_node.getFieldname1();
        String f2 = tar_node.getFieldname1();
        if (f1.equals("") && f2.equals("")) return "";
        if (f1.equals(f2)) return "";
        if (f1.equals("") && !f2.equals("")) return "TAR" + f2;
        if (!f1.equals("") && f2.equals("")) return "SRC" + f1;
        return null;
    }    
    
}
