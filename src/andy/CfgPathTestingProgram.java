package andy;
import java.sql.*;
import java.io.*;

import java.util.Hashtable;

import oracle.jdbc.OracleDriver;

public class CfgPathTestingProgram {

   static Connection conn;
   static Hashtable cfg_vertices = new Hashtable();

    public static void main(String[] args) throws SQLException {
            for (int i=0; i<args.length; i++) {
                System.out.println(args[i]);
            }
            System.out.println(args.length);
            DriverManager.registerDriver(new OracleDriver());
            conn = getConnection();
            IdsVertex src_node = getVertexInfo(2437678);
            IdsVertex tar_node = getVertexInfo(2437703);
            if (checkCfgPath(src_node,tar_node)) {
                System.out.println("has path");
            } else {
                System.out.println("failure");
            }
            
            
            
            conn.close();
    }
 
    private static IdsVertex getVertexInfo(int vertex_key) throws SQLException  {
        String sql = "SELECT * FROM vertex where vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int vertex_label = rset.getInt("VERTEX_LABEL");
        int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
        int pdg_id = rset.getInt("PDG_ID");
        int startline = rset.getInt("STARTLINE");
        IdsVertex v = new IdsVertex(0,vertex_key,vertex_label,vertex_kind_id,pdg_id,startline);
        rset.close();
        pstmt.close();
        return v;
    }
 
    private static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Parameter.getJdbcURL(), Parameter.getUserName(),
                   Parameter.getPasswd());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    } 
 
    static private IdsVertex getCallsiteNode(IdsVertex param_node) throws SQLException {
        String sql = "select v.* from edges e, vertex v " +
                     "where e.tar_vertex_key = ? and e.edge_type = 2 " +
                     "and   e.src_vertex_key = v.vertex_key ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,param_node.getVertex_key());
        ResultSet rset = pstmt.executeQuery();
        IdsVertex callsite_node = null;
        if (rset.next())  {
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int pdg_id = rset.getInt("PDG_ID");
            int startline = rset.getInt("STARTLINE");
            callsite_node = new IdsVertex(0,vertex_key,vertex_label,vertex_kind_id,pdg_id,startline);
        }
        rset.close();
        pstmt.close();
        return callsite_node;
    } 
    
    static private CfgVertex getCfgVertex(int vertex_key, int pdg_id) throws SQLException {
        CfgVertex node = (CfgVertex) cfg_vertices.get(new Integer(vertex_key));
        if (node == null) {
            String sql = "select src_vertex_key, count(*) " + 
                         "from   cfg_edges c, vertex v " + 
                         "where  c.src_pdg_id = ? " + 
                         "and    c.src_pdg_id = c.tar_pdg_id " + 
                         "and    c.tar_vertex_key = v.vertex_key " + 
                         "and    (not v.vertex_kind_id in (1,2,23,24)) " + 
                         "group by src_vertex_key "; 
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pdg_id);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
                int key = rset.getInt(1);
                int neighbor_num = rset.getInt(2);
                CfgVertex v = new CfgVertex(key,neighbor_num);
                cfg_vertices.put(new Integer(key),v);
            }
            rset.close();
            pstmt.close();
        
            sql = "select src_vertex_key, tar_vertex_key, v.startline " + 
                  "from   cfg_edges c, vertex v                " + 
                  "where  c.src_pdg_id = ?                 " + 
                  "and    c.src_pdg_id = c.tar_pdg_id          " + 
                  "and    c.tar_vertex_key = v.vertex_key      " + 
                  "and    (not v.vertex_kind_id in (1,2,23,24))"; 
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,pdg_id);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                Integer key = new Integer(rset.getInt(1));
                int neighbor_key = rset.getInt(2);
                int startline = rset.getInt(3);
                CfgVertex v = (CfgVertex) cfg_vertices.get(key);
                v.setNeighbor(neighbor_key,startline);                
            }
            rset.close();
            pstmt.close();
            node = (CfgVertex) cfg_vertices.get(new Integer(vertex_key));
        }
        return node;
    }
    

    static private boolean checkCfgPath(IdsVertex src_node, IdsVertex tar_node) throws SQLException {
          IdsVertex source_node;
          IdsVertex target_node;
          if (src_node.getVertex_kind_id()==1) source_node = getCallsiteNode(src_node);
          else                                 source_node = src_node;
          if (tar_node.getVertex_kind_id()==1) target_node = getCallsiteNode(tar_node);
          else                                 target_node = tar_node;
          if (source_node == null || target_node == null) return false;
          int pdg_id = tar_node.getPdg_id();
          int target_node_startline = target_node.getStartline();
          
          int[][] cfg_path = new int[20000][2];
          cfg_path[0][0] = source_node.getVertex_key();
          cfg_path[0][1] = source_node.getStartline();
          int path_vertices_num = 1;
          int index = 0;
          Hashtable path_nodes = new Hashtable();
          path_nodes.put(source_node.getVertex_key_integer(),source_node.getVertex_key_integer());
          while (index < path_vertices_num) {
              int current_node_vertex_key = cfg_path[index][0];
              int current_node_startline = cfg_path[index][1];
              
              CfgVertex current_node = getCfgVertex(current_node_vertex_key,pdg_id);
              int[][] neighbors = current_node.getNeighbors();
              int neighbor_num = current_node.getNeighbor_num();
              for (int i=0; i<neighbor_num; i++) {
                  Integer next_node_vertex_key = new Integer(neighbors[i][0]);
                  int next_node_startline = neighbors[i][1];
                  // Find a path to target node
                  if (next_node_vertex_key == target_node.getVertex_key()) return true; 
                  if (next_node_startline>=current_node_startline && next_node_startline <= target_node_startline) {
                      Object obj = path_nodes.get(new Integer(next_node_vertex_key));
                      if (obj == null) {
                          cfg_path[path_vertices_num][0] = next_node_vertex_key.intValue();
                          cfg_path[path_vertices_num][1] = next_node_startline;
                          path_vertices_num++;
                          path_nodes.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                      }    
                  }  
              }
              index++;
          }
          return false;
    }
}
