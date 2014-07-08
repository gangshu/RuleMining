package andy;

public class Parameter {
	//Directory of Mafia
    static String dir="./Mafia-1.4/src/";
    static String user;
    static String passwd = "chang";
    static String jdbc;
    static int NUM_FROM_AO_TO_CP = 2;
    static public String getUserName() {
       return user;
    }
    
    static public int getNUM_FROM_AO_TO_CP() {
        return NUM_FROM_AO_TO_CP;
    }
    
    static public String getPasswd() {
        return passwd;
    }
    
    static public String getJdbcURL() {
        return jdbc;
    }
    
    static public void setJdbc(int choice) {
        switch (choice) {
            case 1: jdbc = "jdbc:oracle:thin:@prajna.cwru.edu:1521:andy"; break;
            case 2: jdbc = "jdbc:oracle:thin:@sila.cwru.edu:1521:openssle"; break;
            case 3: jdbc = "jdbc:oracle:thin:@127.0.0.1:1521:annchang"; break;
        }
    }
    
    static public void setMachine(int choice) {
        switch (choice) {  
            case 1: dir = "d:/project/phd/graph/";break;
            case 2: dir = "c:/ray/project/program/pdg/graph/"; break;
            case 3: dir = "/home/rxc92/program/graph/"; break;
        }        
    }
    /**
     * @author Boya
     */
    static public void setJdbc(String machine, String service_name)
    {
    	jdbc = "jdbc:oracle:thin:@"+machine+".cwru.edu:1521:"+service_name;
    }
    
    /**
     * @author Boya
     */    
    static public void setPassword(String password)
    {
    	passwd = password;
    }
    static public void setUsername(String username) {
       user = username;
    }
    
    static public void setParameter(int machine, int database) {
        passwd = "chang";
        switch (machine) {  
            case 1: dir = "d:/project/phd/graph/";break;
            case 2: dir = "c:/ray/project/program/pdg/graph/"; break;
            case 3: dir = "/home/rxc92/program/graph/"; break;
        }
        switch (database) {
            case 1: jdbc = "jdbc:oracle:thin:@prajan.cwru.edu:1521:andy";
                    user = "og";
                    break;
            case 2: jdbc = "jdbc:oracle:thin:@prajna.cwru.edu:1521:andy";
                    user = "og";
                    break;
        }           
    }
}


