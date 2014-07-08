package andy;

import java.sql.*;
public class SourceFile {
    static String qry_fileinfo = "select p.pdg_name, f.filename " +
                                 "from pdg p, source_file f, vertex v " +
                                 "where p.pdg_id = v.pdg_id "+
                                 "and p.compiler_id = f.compiler_id "+
                                 "and v.vertex_key = ? ";
    String pdg_name;
    String filename;

   public SourceFile(Connection conn, int pdg_id) throws SQLException {
       String sql = "SELECT p.pdg_name,s.filename FROM pdg p, source_file s " +
                    "WHERE  p.pdg_id = ? " +
                    "AND    p.compiler_id = s.compiler_id";
       PreparedStatement pstmt = conn.prepareStatement(sql);
       pstmt.setInt(1,pdg_id);
       ResultSet rset = pstmt.executeQuery();
       rset.next();
       pdg_name = rset.getString(1);
       filename = rset.getString(2);
       rset.close();
       pstmt.close();
   }
 
    public String getPdg_name() {
        return this.pdg_name;
    }
  
    public String getFilename() {
        return this.filename;  
    }
}
