package andy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.*;
import java.sql.*;

import oracle.jdbc.OracleDriver;

public class Pattern {
    
    Hashtable pattern_vertices_hash = new Hashtable();
    int[] pattern_vertices_list;
    int pattern_vertices_num = 0;
    int pattern_key;
    int[][][] pattern_edges;
    int[][][] pattern_closures;
    Hashtable pattern_label_hash = new Hashtable();
    Hashtable ai_to_ai_labels = new Hashtable();
    Hashtable cp_to_ai_labels = new Hashtable();
    Hashtable ai_to_ai_src_labels = new Hashtable();
    Hashtable ai_to_ai_tar_labels = new Hashtable();
    
    public Pattern(int pattern_key, Connection conn) throws SQLException {
        this.pattern_key = pattern_key;
        this.getPatternVerticesNumber(conn);
        this.pattern_vertices_list = new int[this.pattern_vertices_num];
        this.pattern_edges = new int[pattern_vertices_num][pattern_vertices_num][6];
        this.loadPatternVertices(conn);
        this.loadPatternEdges(conn);
        this.computeClosures();
    }
    
    
    //////////// Compute Patter Closures
    
    private void computeClosures() {
        this.pattern_closures = new int[this.pattern_vertices_num][this.pattern_vertices_num][3];
        for (int i=0; i<pattern_vertices_num; i++) {
            for (int j=0; j<pattern_vertices_num; j++) {
                for (int k=0; k<=2; k++) {
                    this.pattern_closures[i][j][k] = this.pattern_edges[i][j][k];
                }
            }
        }
        
        for (int m=0; m<pattern_vertices_num; m++) {
            for (int i=0; i<pattern_vertices_num; i++) {
                for (int j=0; j<pattern_vertices_num; j++) {
                    for (int b=0; b<pattern_vertices_num; b++) {
                        if (i!=j && pattern_closures[i][b][0] > 0 && pattern_closures[b][j][0] > 0 && 
                            (pattern_closures[i][j][0]==0 || (pattern_closures[i][j][0] > (pattern_closures[i][b][0] + pattern_closures[b][j][0] + 1)))) {
                            pattern_closures[i][j][0] = pattern_closures[i][b][0] + pattern_closures[b][j][0] + 1;
                        }
                        if (i!=j && pattern_closures[i][b][1] > 0 && pattern_closures[b][j][1] > 0 && 
                            (pattern_closures[i][j][1]==0 || (pattern_closures[i][j][1] > (pattern_closures[i][b][1] + pattern_closures[b][j][1] + 1)))) {
                            pattern_closures[i][j][1] = pattern_closures[i][b][1] + pattern_closures[b][j][1] + 1;
                        }                       
                    }
                }                
            }
            
        }        
        
        
        
    }
    
    public int getPattern_vertices_num() {
        return this.pattern_vertices_num;
    }
    
    public Hashtable getPattern_vertices_hash() {
        return this.pattern_vertices_hash;
    }
    
    public int getPattern_key() {
        return this.pattern_key;
    }
    
    public int[][][] getPatternClosures() {
        return this.pattern_closures;
    }
    
    public int[][][] getPattern_vertices_edges() {
        return this.pattern_edges;
    }
    
    public int[] getPattern_vertices_list() {
        return this.pattern_vertices_list;
    }

    public Vertex getVertexByKey(int vertex_key) {
        Vertex v = (Vertex) pattern_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
    
    public boolean chekcVertexInPattern(int vertex_key) {
        Object obj = pattern_vertices_hash.get(new Integer(vertex_key));
        if (obj == null) return false;
        else             return true;
    }

    public Vertex getVertexByIndex(int idx) {
        int vertex_key = this.pattern_vertices_list[idx];
        Vertex v = (Vertex) pattern_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
     
    public Hashtable getAi_to_ai_labels() {
        return this.ai_to_ai_labels;
    }
              
    public Hashtable getCp_to_ai_labels() {
        return this.cp_to_ai_labels;
    }
    
    public Hashtable getAi_to_ai_src_labels() {
        return this.ai_to_ai_src_labels;
    }
    
    public Hashtable getAi_to_ai_tar_labels() {
        return this.ai_to_ai_tar_labels;
    }
    
    private void loadPatternEdges(Connection conn) throws SQLException {
        String sql = "SELECT * FROM pattern_instance WHERE pattern_key = ? and graph_id = 0 and instance_id = 0";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.pattern_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int src_vertex_key = rset.getInt("SRC_VERTEX_KEY");
            int tar_vertex_key = rset.getInt("TAR_VERTEX_KEY");
            int edgetype = rset.getInt("EDGE_TYPE");
            Vertex src_node = (Vertex) pattern_vertices_hash.get(new Integer(src_vertex_key));
            Vertex tar_node = (Vertex) pattern_vertices_hash.get(new Integer(tar_vertex_key));
            int src_index = src_node.getNode_index();
            int tar_index = tar_node.getNode_index();
            if (edgetype >= 2) edgetype = 2;
            this.pattern_edges[src_index][tar_index][edgetype] = 1;
            if (edgetype == 2) {
                if (src_node.getVertex_kind_id()==1) {
                    String key = Integer.toString(src_node.getVertex_label())+"-"+Integer.toString(tar_node.getVertex_label());
                    Object obj = this.ai_to_ai_labels.get(key);
                    if (obj == null) this.ai_to_ai_labels.put(key,key);
                    obj = this.ai_to_ai_src_labels.get(Integer.toString(src_node.getVertex_label()));
                    if (obj == null) this.ai_to_ai_src_labels.put(Integer.toString(src_node.getVertex_label()),Integer.toString(src_node.getVertex_label()));
                    obj = this.ai_to_ai_tar_labels.get(Integer.toString(tar_node.getVertex_label()));
                    if (obj == null) this.ai_to_ai_tar_labels.put(Integer.toString(tar_node.getVertex_label()),Integer.toString(tar_node.getVertex_label()));
                } else {
                    String key = Integer.toString(tar_node.getVertex_label());
                    Object obj = this.cp_to_ai_labels.get(key);
                    if (obj == null) this.cp_to_ai_labels.put(key,key);
                }
            }
        }
        rset.close();
        pstmt.close();
    }
    
    private void loadPatternVertices(Connection conn) throws SQLException {
        String sql = "select p.node_index, v.* " + 
                     "from   pattern_node_info p, vertex v " + 
                     "where  p.pattern_key = ? " + 
                     "and    p.pattern_instance = 0 " + 
                     "and    v.vertex_key = p.vertex_key " + 
                     "order by node_index ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.pattern_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int node_index = rset.getInt("NODE_INDEX");
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int startline = rset.getInt("STARTLINE");
            String fieldname1 = rset.getString("FIELDNAME1");
            String fieldname2 = rset.getString("FIELDNAME2");
            int pdg_id = rset.getInt("PDG_ID");
            Vertex v = new Vertex(node_index,vertex_key,vertex_label,vertex_kind_id,startline,0,fieldname1,fieldname2,pdg_id);
            v.setRids_node_index(node_index);
            this.pattern_vertices_list[node_index] = vertex_key;
            this.pattern_vertices_hash.put(v.getVertex_key_Integer(),v);
            if (vertex_kind_id != 6) {  // control point
                Object obj = this.pattern_label_hash.get(new Integer(vertex_label));
                if (obj == null) this.pattern_label_hash.put(new Integer(vertex_label),new Integer(vertex_label));
            }
            
            
        }
        rset.close();
        pstmt.close();
    }
    
    public Hashtable getPattern_vertices_label() {
        return this.pattern_label_hash;
    }
    
    
    private void getPatternVerticesNumber(Connection conn) throws SQLException {
        String sql = "SELECT count(*) FROM pattern_node_info WHERE pattern_key = ? and pattern_instance = 0";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pattern_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) this.pattern_vertices_num = rset.getInt(1);
        rset.close();
        pstmt.close();
    }
    
    
}
