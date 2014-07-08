package andy;

import java.sql.*;
import oracle.jdbc.OracleDriver;
import java.util.*;
public class MainProgramPhaseTwo {
    public static void main(String[] args) throws SQLException {

        int gds_id_start;//160:error
        int gds_id_end;
        float freq = 0.8f;

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
        RIDSSetPhaseTwo ridss = null;
        
        
        try {

            for (int gds_id=gds_id_start; gds_id<=gds_id_end; gds_id++) {
            	
            	
                System.out.println(gds_id);       
                java.util.Date start_time = new java.util.Date();
                ridss = new RIDSSetPhaseTwo(gds_id,conn,freq);   
                java.util.Date  end_time = new java.util.Date();

                outputTime(conn,gds_id, end_time.getTime()-start_time.getTime());
                ridss = null;
                System.gc();
                
                
            }
             
		} catch (Exception e) {
		    e.printStackTrace();			
		}
		finally{
			 conn.close();  
			 conn = null;
			 System.gc();
		}
            
    }
    
    
    static private void outputTime(Connection conn, int gds_id, long interval) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO TIME_FOR_RIDS VALUES(?,?)");
        pstmt.setInt(1,gds_id);
        pstmt.setLong(2,interval);
        pstmt.execute();
        pstmt.close();
    }
    
    static private Connection getConnection() {
        Connection conn = null;
        try {
            System.out.println(Parameter.getUserName());
            conn = DriverManager.getConnection(Parameter.getJdbcURL(),Parameter.getUserName(),Parameter.getPasswd());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }        
    
}
