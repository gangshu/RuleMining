package andy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import oracle.jdbc.OracleDriver;

public class MainProgramPhaseOne {
    public static void main(String[] args) throws SQLException {
        ArrayList<Integer> a = new ArrayList<Integer>(); 
        if (args.length == 0) {
            //Parameter.setParameter(1,2);  // 1: Notebook, 2: Server, 3: Linux,
        	System.out.println("Usage: username password machine service_name");
        	return;
        } else {
            //Parameter.setParameter(3,2);  // second parameter: 1: local db, 2: server db
            Parameter.setUsername(args[0]);
            Parameter.setPassword(args[1]);
            Parameter.setJdbc(args[2], args[3]);
        }
        DriverManager.registerDriver(new OracleDriver());
        Connection conn = getConnection();        
        IDSSetPhaseOne idss = null;
        
        /**
         * Get the candidate nodes
         */
        String sql = "select callee_vertex_key from candidate_fun_for_analysis where number_of_caller >= 5 order by callee_vertex_key";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
        	a.add(rs.getInt(1));
        }
        
        for (int i=0; i<a.size(); i++) {
          System.out.println(a.get(i));        
          idss = new IDSSetPhaseOne(a.get(i),conn);          
        }
        conn.close();          
    }
    
    static private Connection getConnection() {
        Connection conn = null;
        try {     
           conn = DriverManager.getConnection(Parameter.getJdbcURL(),Parameter.getUserName(),Parameter.getPasswd());   
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }    
}

