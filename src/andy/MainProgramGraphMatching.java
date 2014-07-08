package andy;

import java.io.IOException;

import java.sql.*;
import oracle.jdbc.OracleDriver;

public class MainProgramGraphMatching {
    public static void main(String[] args) throws SQLException {
        int pattern_key_start; // out of memory: 688,689
        int pattern_key_end;  // Error: 755,756,836,

        /*if (args.length == 0) {
            pattern_key_start = 130;
            pattern_key_end =  130;
            Parameter.setParameter(1,2);
            Parameter.setUsername("apache_boya");
            Parameter.setJdbc(2);
        } else {
            pattern_key_start = Integer.parseInt(args[0]);
            pattern_key_end = Integer.parseInt(args[1]);
            if (args[2].equals("R") || args[2].equals("r")) Parameter.setJdbc(1);
            if (args[2].equals("B") || args[2].equals("b")) Parameter.setJdbc(2); 
            Parameter.setMachine(3);
            Parameter.setUsername(args[3]);
        }*/
        if (args.length == 0) {
            //gds_id_start = 155;
            //gds_id_end = 155;
            //Parameter.setParameter(1,2);
        	System.out.println("Usage: pattern_key_start pattern_key_end username password machine service_name");
        	return;
        } else {
        	pattern_key_start = Integer.parseInt(args[0]);
        	pattern_key_end = Integer.parseInt(args[1]);
            /**
            if (args[3].equals("R") || args[3].equals("r")) Parameter.setJdbc(1);
            if (args[3].equals("B") || args[3].equals("b")) Parameter.setJdbc(2); 
            Parameter.setMachine(3);
            Parameter.setUsername(args[4]);
            **/
            Parameter.setUsername(args[2]);
            Parameter.setPassword(args[3]);
            Parameter.setJdbc(args[4], args[5]);
        }

        new OracleDriver();     
        Connection conn = getConnection();
        
        /**
         * TODO: change back: N~Y
         */
        //String sql = "select pattern_key, candidate_node_label from pattern where confirm in ('Y', 'N') and pattern_key not in (select pattern_key from violations) order by pattern_key";
        String sql = "select pattern_key, candidate_node_label from pattern where pattern_key in \n" + 
                     "(select pattern_key from pattern_info where reviewer = 'boya' and pattern_key not in (select pattern_key from violations))\n" +
                     "order by pattern_key";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        /**
         *TODO: uncomment
         */
        //pstmt.setInt(1,pattern_key_start);
        //pstmt.setInt(2,pattern_key_end);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int pattern_key = rset.getInt("PATTERN_KEY");
            System.out.println("Pattern_key:"+pattern_key+"--------");
            int candidate_node_label = rset.getInt("CANDIDATE_NODE_LABEL");
            Connection detector_conn = getConnection();
     //       del_existing_pattern_violation(conn,pattern_key);     
            PatternViolationDetection detector = null;
            detector = new PatternViolationDetection(pattern_key,candidate_node_label,conn);
            detector.findPatternInstances();
            detector_conn.close();
        }
        rset.close();
        pstmt.close();
        conn.close();
    }
   
    static private void del_existing_pattern_violation(Connection conn, int pattern_key) throws SQLException {
        CallableStatement cstmt = null;
        cstmt = conn.prepareCall("{call del_pattern_violation(?)}");
        cstmt.setInt(1,pattern_key); //graph group id
        cstmt.execute();
        cstmt.close();
    }    
    
    static private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Parameter.getJdbcURL(),Parameter.getUserName(),Parameter.getPasswd());
            //   conn = DriverManager.getConnection("jdbc:oracle:thin:@prajna.cwru.edu:1521:andy","MAKE","chang");     
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }      
    
    
}
