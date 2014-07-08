package andy;

import java.sql.*;

import java.io.*;

import oracle.jdbc.OracleDriver;

public class MainProgramMining {
    public static void main(String[] args) throws SQLException, IOException {
        
        
        int gds_id_start; // out of memory: 688,689
        int gds_id_end;  // Error: 755,756,836,
        float freq = 0.8f;

        /*if (args.length == 0) {
            gds_id_start = 1164;
            gds_id_end =  1164;
            Parameter.setParameter(1,2);
        } else {
            gds_id_start = Integer.parseInt(args[0]);
            gds_id_end = Integer.parseInt(args[1]);
            freq = Float.parseFloat(args[2]);
            if (args[3].equals("R") || args[3].equals("r")) Parameter.setJdbc(1);            
            if (args[3].equals("B") || args[3].equals("b")) Parameter.setJdbc(2); 
            Parameter.setMachine(3);           
            Parameter.setUsername(args[4]);
        }*/
        if (args.length == 0) {
            //gds_id_start = 155;
            //gds_id_end = 155;
            //Parameter.setParameter(1,2);
        	System.out.println("Usage: gds_id_start gds_id_end freq username password machine service_name");
        	return;
        } else {
            gds_id_start = Integer.parseInt(args[0]);
            gds_id_end = Integer.parseInt(args[1]);
            freq = Float.parseFloat(args[2]);
            /**
            if (args[3].equals("R") || args[3].equals("r")) Parameter.setJdbc(1);
            if (args[3].equals("B") || args[3].equals("b")) Parameter.setJdbc(2); 
            Parameter.setMachine(3);
            Parameter.setUsername(args[4]);
            **/
            Parameter.setUsername(args[3]);
            Parameter.setPassword(args[4]);
            Parameter.setJdbc(args[5], args[6]);
        }
        
        
        
        DriverManager.registerDriver(new OracleDriver());
        Connection conn = getConnection();
        GraphDataset gds = null;

        for (int gds_id = gds_id_start; gds_id <= gds_id_end; gds_id++) {
            System.out.println(gds_id+":"+freq);
            if (isIntrestedGraphDataset(gds_id, conn)) {
                gds = new GraphDataset(gds_id, conn, freq);
            }
        }
        conn.close();
    }

    private static boolean isIntrestedGraphDataset(int gds_id, 
                                                   Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rids_nodes WHERE gds_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, gds_id);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int count = rset.getInt(1);
        rset.close();
        pstmt.close();
        if (count > 0)
            return true;
        else
            return false;
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
}
