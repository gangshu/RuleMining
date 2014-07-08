package andy;
import java.sql.*;
import oracle.jdbc.*;
import oracle.sql.*;
public class Violations {
    static String qry_violation_by_violation_key = "SELECT * FROM violations WHERE violation_key = ?";
    int pattern_key;
    int violation_key;
    int vertex_key;
    int lost_nodes;
    int lost_edges;
    int rank;
    String comments;
    String comments_id;

    public Violations(Connection conn, int p_violation_key) throws SQLException {
        this.violation_key = p_violation_key;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            pstmt = conn.prepareStatement(qry_violation_by_violation_key);
            pstmt.setInt(1,violation_key);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                loadData(rset);
            } else {
                throw new SQLException("Violation Key Not Found");
            }
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (rset != null) rset.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public Violations(ResultSet rset) throws SQLException {
        loadData(rset);
    }
  
    public void setComments(String c) {
        this.comments = c;
    }
  
    public String getComments() {
        return this.comments;
    }
  
    public void setCommentsID(String c) {
        this.comments_id = c;
    }
  
    public String getCommentsID() {
        return this.comments_id;
    }
  
    private void loadData(ResultSet rset) throws SQLException {
       pattern_key = rset.getInt(1);
       violation_key = rset.getInt(2);
       vertex_key = rset.getInt(3);
       lost_nodes = rset.getInt(4);
       lost_edges = rset.getInt(5);
       rank = rset.getInt(6);  
       this.comments = rset.getString(8);
       this.comments_id = rset.getString(9);
    }
  
    public int getPattern_key() {
        return pattern_key;
    }

    public void setPattern_key(int newPattern_key){
        pattern_key = newPattern_key;
    }

    public int getViolation_key(){
        return violation_key;
    }

    public void setViolation_key(int newViolation_key){
        violation_key = newViolation_key;
    }

    public int getVertex_key() {
        return vertex_key;
    }

    public void setVertex_key(int newVertex_key) {
        vertex_key = newVertex_key;
    }

    public int getLost_nodes() {
        return lost_nodes;
    }

    public void setLost_nodes(int newLost_nodes) {
        lost_nodes = newLost_nodes;
    }
    
    public int getLost_edges() {
        return lost_edges;
    }

    public void setLost_edges(int newLost_edges) {
        lost_edges = newLost_edges;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int newRank) {
        rank = newRank;
    }
}
