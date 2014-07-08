package andy;
import java.util.*;
import java.sql.*;
public class IDSSetPhaseOne {

    static int MAX_CALLER_LEVEL = 3;
    static int MAX_GRAPH_NUM = 10;
    static int MAX_CALLSITE_NUM = 100;
    static int MAX_NODE_NUM = 1500;
    static String forward_sql = "select e.src_vertex_key current_node_vertex_key, e.src_pdg_id current_node_pdg_id,  " + 
                                "       e.tar_vertex_key new_node_vertex_key, e.tar_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type  " + 
                                "from   vertex v, edges e  " + 
                                "where  e.src_vertex_key = ?  " + 
                                "and    e.tar_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) "  +
                                "and    (not v.vertex_kind_id in (3,13,14,15,16))";
    static String backward_sql= "select e.tar_vertex_key current_node_vertex_key, e.tar_pdg_id current_node_pdg_id,  " + 
                                "       e.src_vertex_key new_node_vertex_key, e.src_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type  " + 
                                "from   vertex v, edges e  " + 
                                "where  e.tar_vertex_key = ?  " + 
                                "and    e.src_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) " +
                                "and    (not v.vertex_kind_id in (3,13,14,15,16))";
    int candidate_label;
    static String qry_candidate_node = "SELECT v.*,p.pdg_kind FROM vertex v, pdg p WHERE vertex_label = ? and p.pdg_id = v.pdg_id";
    static String dir = "D:/project/phd/graph/";
    Connection conn;
    int num_ids = 0;
    Hashtable[] ids_callsites = new Hashtable[10];
    Hashtable pdg_in_idsset = new Hashtable();
    Hashtable user_defined_pdg = new Hashtable();
    int gds_id = 0;

    public IDSSetPhaseOne(int candidate_label, Connection conn) throws SQLException {
        this.candidate_label = candidate_label;
        this.conn = conn;
        this.getGds_id();
        int graph_num = this.buildIDSGraphDataset();
        this.outputGraphDataSet(graph_num);
    }
    
    private void getGds_id() throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("select GRAPH_DATASET_SEQ.nextval from dual");
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        this.gds_id = rset.getInt(1);
        rset.close();
        pstmt.close(); 
    }

    private int buildIDSGraphDataset() throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(qry_candidate_node);
        pstmt.setInt(1,this.candidate_label);
        ResultSet rset = pstmt.executeQuery();
        Hashtable candidate_node_pdg_hash = new Hashtable();  // Used for checking that at most one IDS is created for each PDG
        int gid = 0;
        while (rset.next() && this.num_ids <MAX_GRAPH_NUM) {
           Integer pdg_id = new Integer(rset.getInt("PDG_ID")); 
           String pdg_kind = rset.getString("PDG_KIND");
           Object obj = candidate_node_pdg_hash.get(pdg_id);  // The candidate node must come from different PDGs
           if (obj == null && pdg_kind.indexOf("user") != -1) {
             candidate_node_pdg_hash.put(pdg_id,pdg_id);
             int candidate_node_vertex_key = rset.getInt("VERTEX_KEY");
             ids_callsites[this.num_ids++] = findIDS_callsites(candidate_node_vertex_key, pdg_id.intValue(), gid++);             
           } 
        }
        rset.close();
        pstmt.close();
        return gid;
    }
    
    private void outputGraphDataSet(int graph_num) throws SQLException {
        String sql = "INSERT INTO graphdataset values (?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        pstmt.setInt(2,this.candidate_label);
        pstmt.setInt(3,graph_num);
        pstmt.execute();
        pstmt.close();
    }
    
    private boolean isNewPdgInIdsSet(int pdg_id) {
        Object obj = this.pdg_in_idsset.get(new Integer(pdg_id));
        if (obj == null) return true;
        else             return false;
    }
    
    private int findBestCallerPdg(int pdg_id) throws SQLException  {
        String sql = "select p.* " +
                     " from  (select distinct pdg_id from vertex " +
                     "        where vertex_label = (select vertex_key from vertex where pdg_id = ? and pdg_vertex_kind = 'entry')) t, " +
                     "        pdg p " +
                     "where p.pdg_id = t.pdg_id " +
                     "order by child_call_site_num + call_site_num desc ";
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        int first_pdg_id = 0;      
        try {
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1,pdg_id);
          rset = pstmt.executeQuery();
          while (rset.next()) {
            String pdg_type = rset.getString("PDG_KIND");
            if (pdg_type.substring(0,4).equals("user")) {
              int best_pdg_id = rset.getInt("PDG_ID");
              if (first_pdg_id == 0 && best_pdg_id != pdg_id) first_pdg_id = best_pdg_id;
              if (this.isNewPdgInIdsSet(best_pdg_id)) return best_pdg_id;
            }
          }
        } finally {
          rset.close();
          pstmt.close();
        }
        return first_pdg_id;
    } 
    
    
    // Find the PDGs which call the function containing the candidate node directly or indirectly
    private void buildCallerInfo(int pdg_id, Hashtable ids_pdg_hash) throws SQLException {
        int callee_pdg_id = pdg_id;
        int level = -1;
        for (int i=0; i<MAX_CALLER_LEVEL; i++) {
          int caller_pdg_id = findBestCallerPdg(callee_pdg_id);
          if (caller_pdg_id == 0) return;
          PdgForPhaseOne caller_pdg = new PdgForPhaseOne(caller_pdg_id,0,level--,false);
          ids_pdg_hash.put(new Integer(caller_pdg_id), caller_pdg ); // Insert into the PDG list from which the IDS is derived
          PdgForPhaseOne callee_pdg = (PdgForPhaseOne) ids_pdg_hash.get(new Integer(callee_pdg_id));
          callee_pdg.setP_pdg_id(caller_pdg_id); // set up the parent pdg_id of the callee pdg
          
          callee_pdg_id = caller_pdg_id;
        }
    }
    
    
    // Record that the PDG has been used to build a IDS in the IDSSet
    private void insertPdgIntoIdsSet(int pdg_id) {
        Object obj = this.pdg_in_idsset.get(new Integer(pdg_id));
        if (obj == null) {
            this.pdg_in_idsset.put(new Integer(pdg_id),new Integer(pdg_id));
        }
    }
    
    private Integer getNumCallsite(Hashtable callsites) {
        return ((Integer)callsites.get("num_callsite"));
    }
    
    private void increaseNumCallsite(Hashtable callsites) {
        Integer i = (Integer) callsites.get("num_callsite");
        callsites.remove("num_callsite");
        callsites.put("num_callsite", new Integer(i.intValue()+1));
    }
    
    private int loadCallsitGraph(Hashtable callsites, Hashtable vertices_hash, int[] vertices_list, int vertex_key, 
                                 int node_type, int num_vertices, int[] vertices_distance, int distance) throws SQLException{
        int callsite_node_vertex_key = 0;
        switch (node_type) {
            case 1:
            case 2: callsite_node_vertex_key = this.findCallsiteNodeVertexKey(vertex_key); break;
            case 5: callsite_node_vertex_key = vertex_key; break;
        }
        Object obj = vertices_hash.get(new Integer(callsite_node_vertex_key));
        if (obj != null) return num_vertices;  // Callsite graph has been included in the IDS
        this.increaseNumCallsite(callsites);
        ////////////////// Add call-site node
        
        num_vertices = addNode(vertices_hash,vertices_list,callsite_node_vertex_key,num_vertices,vertices_distance,distance);        
        this.recordCallsiteLabelinIDS(callsites,this.getLabelOfNode(callsite_node_vertex_key));
        ////////////////// Add parameter nodes
        String sql = "select v.vertex_key, v.vertex_label " + 
                     "from   edges e, vertex v " + 
                     "where  e.src_vertex_key = ? " + 
                     "and    e.tar_vertex_key = v.vertex_key " + 
                     "and    e.edge_type = 2  " + 
                     "and    v.vertex_kind_id in (1,2)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,callsite_node_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int parameter_node_vertex_key = rset.getInt("VERTEX_KEY");
            int parameter_node_vertex_label = rset.getInt("VERTEX_LABEL");         
            num_vertices = this.addNode(vertices_hash,vertices_list,parameter_node_vertex_key,num_vertices,vertices_distance,distance);
            this.recordCallsiteLabelinIDS(callsites,parameter_node_vertex_label);
        }
        rset.close();
        pstmt.close();
        return num_vertices;
    }
    
    private void recordCallsiteLabelinIDS(Hashtable callsites, int vertex_label) {
       Object obj = callsites.get(new Integer(vertex_label));
       if (obj == null) callsites.put(new Integer(vertex_label),new Integer(vertex_label));
    }
    
   
    private int getLabelOfNode(int vertex_key) throws SQLException {
        String sql = "SELECT * FROM vertex WHERE vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int vertex_label = rset.getInt("VERTEX_LABEL");
        rset.close();
        pstmt.close();
        return vertex_label;
    }
    
    
    // Find the callsite of a parameter node
    private int findCallsiteNodeVertexKey(int vertex_key) throws SQLException {
        String sql = "SELECT * FROM EDGES WHERE TAR_VERTEX_KEY = ? and edge_type = 2";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int callsite_node_vertex_key = rset.getInt("SRC_VERTEX_KEY");
        rset.close();
        pstmt.close();
        return callsite_node_vertex_key;
    }
    
    // The PDGs in "ids_pdg" are used for building the IDS 
    private void insertPdgIntoIDS(Hashtable ids_pdg_hash, PdgForPhaseOne pdg) {
        PdgForPhaseOne new_pdg = (PdgForPhaseOne) ids_pdg_hash.get(pdg.getPdg_id_Key());
        if (new_pdg == null) ids_pdg_hash.put(pdg.getPdg_id_Key(),pdg);
        else {
            if (!new_pdg.involved_in_ids) {  // Set up that the PDG is used for building the IDS
                new_pdg.setInvolved_in_ids();
                new_pdg.setLevel(pdg.getLevel());
            }
        }
    }
    
    private Hashtable findIDS_callsites(int candidate_node_vertex_key, int candidate_node_pdg_id, int gid) throws SQLException {
    
        int candidate_function_pdg_id = findCandidateFunctionPdgId(candidate_node_vertex_key); // The PDG of the candidate function won't be included in the IDS
        
        Hashtable ids_pdg_hash = new Hashtable();  // The PDGs from which the IDS is derived
        this.insertPdgIntoIDS(ids_pdg_hash,new PdgForPhaseOne(candidate_node_pdg_id,0,0,true));  // The pdg used for building IDS
        this.insertPdgIntoIdsSet(candidate_node_pdg_id);  // the pdg has been used by IDS Set. 
        
        buildCallerInfo(candidate_node_pdg_id, ids_pdg_hash);  // find the precedence PDGs of the candidate function
        
        Hashtable callsites = new Hashtable(); // The labels of call-site nodes in the IDS
        
        Hashtable vertices_hash = new Hashtable();
        int[] vertices_list = new int[2500];
        int[] vertices_distance = new int[2500];
        int num_vertices = 0;
        int distance = 0;
        callsites.put("num_callsite",new Integer(0));
        // Load the callsite graph of the candidate node
        num_vertices = this.loadCallsitGraph(callsites,vertices_hash,vertices_list,candidate_node_vertex_key,5,num_vertices,vertices_distance,distance);
        
        int index = 0;
        while (index < num_vertices && this.getNumCallsite(callsites).intValue() < MAX_CALLSITE_NUM && num_vertices < MAX_NODE_NUM) {
            int current_node_vertex_key = vertices_list[index];
            boolean[] forward = {true,false};
            for (int i=0; i<2; i++) {
                PreparedStatement pstmt = null;
                if (forward[i]) {
                    pstmt = conn.prepareStatement(forward_sql);
                } else {
                    pstmt = conn.prepareStatement(backward_sql);
                }
                pstmt.setInt(1,current_node_vertex_key);
                ResultSet rset = pstmt.executeQuery();
                while (rset.next() && this.getNumCallsite(callsites).intValue() < MAX_CALLSITE_NUM && num_vertices < MAX_NODE_NUM) {
                    int current_node_pdg_id = rset.getInt("CURRENT_NODE_PDG_ID");
                    int new_node_vertex_key = rset.getInt("NEW_NODE_VERTEX_KEY");
                    int new_node_pdg_id = rset.getInt("NEW_NODE_PDG_ID");
                    int new_node_vertex_kind_id = rset.getInt("NEW_NODE_VERTEX_KIND_ID");
                    int edge_type = rset.getInt("EDGE_TYPE");
                    switch (new_node_vertex_kind_id) {
                      case 1:
                      case 2: 
                      case 5: if (edge_type == 1) { 
                                  num_vertices = this.loadCallsitGraph(callsites,vertices_hash,vertices_list,new_node_vertex_key,new_node_vertex_kind_id,
                                                                       num_vertices,vertices_distance,vertices_distance[index]+1);
                              } else {  // 
                                  if (new_node_pdg_id != candidate_function_pdg_id) { 
                                      if (this.isIntrestedPdg(ids_pdg_hash,current_node_pdg_id,new_node_pdg_id,forward[i],current_node_vertex_key, new_node_vertex_key)) {
                                          num_vertices = this.loadCallsitGraph(callsites,vertices_hash,vertices_list,new_node_vertex_key,new_node_vertex_kind_id,
                                                                               num_vertices,vertices_distance,vertices_distance[index]+1);
                                      }
                                  }   
                              }
                              break;
                      default: if (edge_type == 1) {
                                   num_vertices = addNode(vertices_hash,vertices_list,new_node_vertex_key,num_vertices,vertices_distance,vertices_distance[index]+1);
                               } else {
                                   if (new_node_pdg_id != candidate_function_pdg_id) { // The PDG of the candidate function won't be included in the IDS
                                       if (this.isIntrestedPdg(ids_pdg_hash,current_node_pdg_id,new_node_pdg_id,forward[i],current_node_vertex_key, new_node_vertex_key)) {
                                           num_vertices = addNode(vertices_hash,vertices_list,new_node_vertex_key,num_vertices,vertices_distance,vertices_distance[index]+1); 
                                       }
                                   }
                               }
                               break;
                      
                    }                    
                }
                rset.close();
                pstmt.close();
            }
            index++;
        } 
        
        outputIds(gid,candidate_node_vertex_key,vertices_list,num_vertices,ids_pdg_hash,vertices_distance);        
        return callsites;
    }
    
    private void outputIds(int gid, int candidate_node_vertex_key, int[] vertices_list, int num_vertices, 
                           Hashtable ids_pdg_hash, int[] vertices_distance) throws SQLException {
        String sql = "INSERT INTO Graphs Values(?,?,?,?)"; // gds_id, gid, candidate_node_vertex_key
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        pstmt.setInt(2,gid);
        pstmt.setInt(3,candidate_node_vertex_key);
        pstmt.setInt(4,num_vertices);
        pstmt.execute();
        pstmt.close();
        
        /// Output Nodes
        sql = "INSERT INTO ids_nodes VALUES(?,?,?,?,?)";
        pstmt = conn.prepareStatement(sql);
        for (int i=0; i<num_vertices; i++) {
            pstmt.setInt(1,gds_id);
            pstmt.setInt(2,gid);
            pstmt.setInt(3,i); //node index
            pstmt.setInt(4,vertices_list[i]);
            pstmt.setInt(5,vertices_distance[i]);
            pstmt.execute();
        }
        pstmt.close();
           
        ///////////Output PDGs of the IDS
        sql = "INSERT INTO ids_pdg VALUES(?,?,?,?,?)";
        pstmt = conn.prepareStatement(sql);
        Enumeration pdg_enum = ids_pdg_hash.elements();
        while (pdg_enum.hasMoreElements()) {
            PdgForPhaseOne pdg = (PdgForPhaseOne) pdg_enum.nextElement();
            if (pdg.isInvolved_in_ids()) {
                pstmt.setInt(1,this.gds_id);
                pstmt.setInt(2,gid);
                pstmt.setInt(3,pdg.getPdg_id_Key().intValue());
                pstmt.setInt(4,pdg.getLevel());
                pstmt.setInt(5,pdg.getP_pdg_id());
                pstmt.execute();
            }
        }
        pstmt.close();
        
    }
    
    
    // Check whether the PDG is a user-defined PDG
    private boolean isUserDefinedPdg(int pdg_id) throws SQLException {
        String flag = (String) user_defined_pdg.get(new Integer(pdg_id));
        if (flag == null) {
            String sql = "SELECT * FROM pdg WHERE pdg_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,pdg_id);
            ResultSet rset = pstmt.executeQuery();
            rset.next();
            String pdg_kind = rset.getString("PDG_KIND");
            if (pdg_kind.indexOf("user")!=-1) flag = "Y";
            else                              flag = "N";
            user_defined_pdg.put(new Integer(pdg_id),flag);
            rset.close();
            pstmt.close();
        } 
        if (flag.equals("Y")) return true;
        else                  return false;
    }
    
    
    private boolean isIntrestedPdg(Hashtable ids_pdg_hash, int current_node_pdg_id, int new_node_pdg_id, 
                                   boolean direction, int current_node_vertex_key, int new_node_vertex_key) throws SQLException {
       
        int current_level = ((PdgForPhaseOne)ids_pdg_hash.get(new Integer(current_node_pdg_id))).getLevel();
        if (direction) {
            if (current_level >= MAX_CALLER_LEVEL || current_level <= -MAX_CALLER_LEVEL) {
               return false; 
            } else {
                if (this.isUserDefinedPdg(new_node_pdg_id)) {
                    if (current_level>=0) {
                        this.insertPdgIntoIDS(ids_pdg_hash,new PdgForPhaseOne(new_node_pdg_id,current_node_pdg_id,++current_level,true));
                    } else {
                        this.insertPdgIntoIDS(ids_pdg_hash, new PdgForPhaseOne(new_node_pdg_id,current_node_pdg_id,--current_level,true));
                    }
                    this.insertPdgIntoIdsSet(new_node_pdg_id);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            PdgForPhaseOne new_pdg = (PdgForPhaseOne) ids_pdg_hash.get(new Integer(new_node_pdg_id));
            if (new_pdg == null) {
                return false;
            } else {
                new_pdg.setInvolved_in_ids();
                this.insertPdgIntoIdsSet(new_node_pdg_id);
                return true;
            }
        }
    }


    private int addNode(Hashtable vertices_hash, int[] vertices_list, int new_node_vertex_key, int num_vertices, 
                        int[] vertices_distance,int distance) {
    
        Object obj = vertices_hash.get(new Integer(new_node_vertex_key));
        if (obj == null) {
            vertices_hash.put(new Integer(new_node_vertex_key), new Integer(new_node_vertex_key));
            vertices_list[num_vertices] = new_node_vertex_key;
            vertices_distance[num_vertices] = distance;
            num_vertices++;
        }
        return num_vertices;
    }


    // The nodes of the PDG of the candidate node won't be included in the IDS.
    private int findCandidateFunctionPdgId(int candidate_node_vertex_key) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM edges WHERE src_vertex_key = ? and edge_type = 4");
        pstmt.setInt(1,candidate_node_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        /**
         * Bug fix: Some call sites, for example APIs, does not have a corresponding PDG.
         */
        if(!rset.next())
            return Integer.MAX_VALUE; //Since all PDG-id are negative, we could return this indicating that no candidate function exists
        int candidate_function_pdg_id = rset.getInt("TAR_PDG_ID");
        rset.close();
        pstmt.close();
        return candidate_function_pdg_id;
    }
}
