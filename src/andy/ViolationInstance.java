package andy;

import java.sql.*;

public class ViolationInstance extends CandidatePatternInstance{
    static String qry_violation_instance = "select  n.node_index, v.* " +
                                           "from   violation_node_info n, vertex v " +
                                           "where  violation_key = ? " +
                                           "and    n.vertex_key = v.vertex_key order by node_index";
    int violation_key;
    public ViolationInstance(int p_violation_key, Connection db_conn)  {
        conn = db_conn;
        this.violation_key = p_violation_key;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        cpi_vertices_num = 0;
        try {
            // Load Rule Instance Node Number
            this.getRuleInstanceNodeNumber();
            pstmt = conn.prepareStatement(qry_violation_instance);
            pstmt.setInt(1,violation_key);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                int node_index = rset.getInt("NODE_INDEX");
                int vertex_key = rset.getInt("VERTEX_KEY");
                int vertex_label = rset.getInt("VERTEX_LABEL");
                int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
                int startline = rset.getInt("STARTLINE");
                int pdg_id = rset.getInt("PDG_ID");
                Vertex v = new Vertex(node_index,vertex_key,vertex_label,vertex_kind_id,startline,0,"","",pdg_id);
               // v.setCharacter(rset.getString("VERTEX_CHARACTERS"));
               // v.setEndline(rset.getInt("ENDLINE"));
                cpi_vertices_list[cpi_vertices_num++] = vertex_key;             
                cpi_vertices_hash.put(v.getVertex_key_Integer(),v);
            }
        } catch (SQLException e) {
            try {
                if (pstmt != null) pstmt.close();
                if (rset != null) rset.close();     
            } catch (SQLException e1) {}
        }
    }
    
    private void getRuleInstanceNodeNumber() throws SQLException {
        String sql = "SELECT count(*) FROM violation_node_info  " +
                     "WHERE  violation_key = ? "; 
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.violation_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) cpi_vertices_list = new int[rset.getInt(1)];
        rset.close();
        pstmt.close();                     
    }
    
}