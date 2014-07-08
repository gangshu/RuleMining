package andy;
import java.sql.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.activation.DataSource;
public class IDS {

	public int getTopped_pdg_id() {
		return topped_pdg_id;
	}

	protected static int MAX_VERTICES_NUM_1 = 800;
	protected static int MAX_VERTICES_NUM_2 = 1200;
	protected static int MAX_CALLSITE_NUM = 100;
    static float[] rate = {0.8f, 0.75f, 0.7f, 0.65f, 0.6f};
    static String qry_violation_id = "SELECT  violation_id.nextval FROM dual";
    static String ins_violation_nodes = "INSERT INTO violation_node_info VALUES(?,?,?)";
    static String ins_violation_info = "INSERT INTO violations(pattern_key, violation_key, vertex_key,lost_nodes, lost_edges) "+
                                       "VALUES(?,?,?,?,?)";
    static String ins_violation_edges = "INSERT INTO pattern_violation_edges VALUES(?,?,?,?)";
    static String ins_instance_missed_node = "INSERT INTO instance_missed_node VALUES(?,?)" ;
    static String ins_instance_missed_edge = "INSERT INTO instance_missed_edge VALUES(?,?,?,?)";
    
    
    protected static String forward_sql = "select e.src_vertex_key current_node_vertex_key, e.src_pdg_id current_node_pdg_id,  " + 
                                "       e.tar_vertex_key new_node_vertex_key, e.tar_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type,  " +
                                "       v.vertex_label new_node_vertex_label, v.startline new_node_startline, " +
                                "       v.fieldname1 new_node_fieldname1,   v.fieldname2 new_node_fieldname2 " +
                                "from   vertex v, edges e  " + 
                                "where  e.src_vertex_key = ?  " + 
                                "and    e.tar_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) "  +
                                "and    (not v.vertex_kind_id in (3,13,14,15,16))";
    protected static String backward_sql= "select e.tar_vertex_key current_node_vertex_key, e.tar_pdg_id current_node_pdg_id,  " + 
                                "       e.src_vertex_key new_node_vertex_key, e.src_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type,  " + 
                                "       v.vertex_label new_node_vertex_label, v.startline new_node_startline, " +
                                "       v.fieldname1 new_node_fieldname1,   v.fieldname2 new_node_fieldname2 " +
                                "from   vertex v, edges e  " + 
                                "where  e.tar_vertex_key = ?  " + 
                                "and    e.src_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) " +
                                "and    (not v.vertex_kind_id in (3,13,14,15,16))";
    protected static String find_global_node = "select e.src_vertex_key current_node_vertex_key, e.src_pdg_id current_node_pdg_id,  " + 
                                     "       e.tar_vertex_key new_node_vertex_key, e.tar_pdg_id new_node_pdg_id,  " + 
                                     "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type,  " + 
                                     "       v.vertex_label new_node_vertex_label, v.startline new_node_startline, " + 
                                     "       v.fieldname1 new_node_fieldname1,   v.fieldname2 new_node_fieldname2 " +
                                     "from   vertex v, edges e  " + 
                                     "where  e.src_vertex_key = ?  " + 
                                     "and    e.tar_vertex_key = v.vertex_key  " + 
                                     "and    e.edge_type in (1,3) ";

    static String control_sql   = "SELECT tar_vertex_key , tar_pdg_id, vertex_kind_id " +
                                  "FROM   edges e, vertex v " +
                                  "WHERE  e.src_vertex_key = ? " +
                                  "AND    e.tar_vertex_key = v.vertex_key " +
                                  "AND    e.edge_type in (2,4) ";   

    int key_node_target_pdg_id; // This PDG won't be used for building a dependence sphere
    int iteration_num;  
    int callsite_num;
    Pattern pattern;
    Hashtable<Integer, Vertex> ids_vertices_hash = new Hashtable<Integer, Vertex>() ;
    int ids_vertices_num = 0;
    int[] ids_vertices_list;
    Hashtable<String, IdsEdge> ids_edges = new Hashtable<String, IdsEdge>();
    protected Hashtable<Integer, PdgForPhaseOne> ids_pdg_set = new Hashtable<Integer, PdgForPhaseOne>();
    
    Hashtable<Integer, Vertex> rids_vertices_hash = new Hashtable<Integer, Vertex>() ;
    int rids_vertices_num = 0;
    int[] rids_vertices_list;
    Hashtable<String, IdsEdge> rids_edges = new Hashtable<String, IdsEdge>();    
    
    int[] mapping;
    int lost_nodes;
    int lost_edges;
    boolean matchPattern;
    protected int topped_pdg_id;
    int subIDS_max_vertices_num = 0;
    int ids_id = 0;
    
    boolean outputResult = false;
    boolean checkSuperIDG = true;

    int subIDSIndex = -1;
    Vector lost_edges_info = new Vector();  // Used for evaluating whether it is required to include super PDG to find pattern instance    
    
    /**
     * The following are added in order to get the new Constructor that only does initialization but perform any expantions
     */
    Connection conn;
    Vertex key_node;
    
    public IDS(int key_node_vertex_key, int key_node_pdg_id, Connection conn, Pattern pattern) throws SQLException {
        this.topped_pdg_id = key_node_pdg_id;
        
        this.iteration_num = 1;
        this.pattern = pattern;
        // this pdg won't be used for building IDS
        this.key_node_target_pdg_id = getCallsiteTargetPdg(key_node_vertex_key,conn);
        this.callsite_num = 0;
        // ids_vertices_num, ids_vertices_hash, ids_edges, ids_pdg_set have beeen initialized    
        int[] ids_temp_vertices_list = new int[MAX_VERTICES_NUM_1*2];
        
        /// The node in the pdg can be used for building the IDS
        PdgForPhaseOne pdg = new PdgForPhaseOne(key_node_pdg_id,-1,0,true);
        this.ids_pdg_set.put(pdg.getPdg_id_Key(),pdg);
        Vertex key_node = this.getVertexFromDB(key_node_vertex_key,conn); // with node_index 0 (set in getVertexFromDB())
        key_node.setDistance_to_candidate(0);        
        this.loadCallsitGraph(conn,key_node,ids_temp_vertices_list);        
        this.expandIds(conn,ids_temp_vertices_list);
        this.buildIdsSDDEdge(conn);
            
        /// Build Reduced Dependence Sphere
        buildRids(conn,null);             
    }
    
    /**
     * 
     * This is used to only initialize the variables needed but not starting to build IDS or RIDS
     * 
     * @author Boya
     * 
     * @param key_node_vertex_key
     * @param key_node_pdg_id
     * @param conn
     * @param pattern
     * @throws SQLException
     */
    public IDS(int key_node_vertex_key, int key_node_pdg_id, javax.sql.DataSource dataSource, Pattern pattern) throws SQLException {
    	
    	conn = dataSource.getConnection();
        this.topped_pdg_id = key_node_pdg_id;        
        this.iteration_num = 1;
        this.pattern = pattern;
        this.key_node_target_pdg_id = getCallsiteTargetPdg(key_node_vertex_key,conn);
        this.callsite_num = 0;
        /**
         * Get the PDG where the key note locates
         */
        PdgForPhaseOne pdg = new PdgForPhaseOne(key_node_pdg_id,-1,0,true);
        this.ids_pdg_set.put(pdg.getPdg_id_Key(),pdg);
        
        /**
         * Get key node, which is the center node
         */
        key_node = this.getVertexFromDB(key_node_vertex_key,conn); // with node_index 0 (set in getVertexFromDB())
        key_node.setDistance_to_candidate(0);        
    
    }
    
    public void buildIDS() throws SQLException
    {
        int[] ids_temp_vertices_list = new int[MAX_VERTICES_NUM_1*2];
        this.loadCallsitGraph(conn,key_node,ids_temp_vertices_list);        
        this.expandIds(conn,ids_temp_vertices_list);
        this.buildIdsSDDEdge(conn);
    }
    public void buildIDS(List<Vertex> vList, List<IdsEdge> eList) throws SQLException
    {
    	int[] ids_temp_vertices_list = new int[MAX_VERTICES_NUM_1*2];
    	this.loadInitialGraph(vList, eList, ids_temp_vertices_list);
        this.expandIds(conn,ids_temp_vertices_list);
        this.buildIdsSDDEdge(conn);
    }
    /**
     * If there is an initial graph, load the initial graph
     * 
     * @param vList vertex list sorted by their distance to the center node
     * @param eList edge list in the initial graph
     * @param ids_tmp_vertices_list
     */
    private void loadInitialGraph(List<Vertex> vList, List<IdsEdge> eList, int[] ids_tmp_vertices_list)
    {
    	/**
    	 * ids_vertices_hash
    	 * ids_pdg_set
    	 * ids_tmp_vertices_list
    	 */
    	for(int i=0; i<vList.size();i++)
    	{
    		Vertex v = vList.get(i);
    		int pdgId = v.getPdg_id();
    		ids_vertices_hash.put(v.getVertex_key(), v);
    		/**
    		 * Get the PDG of the vertex
    		 * TODO: The level and parent value is incorrect; but it does not affect expanding IDS
    		 */
    		PdgForPhaseOne pdg = new PdgForPhaseOne(pdgId,-1,0,true);
    		ids_pdg_set.put(pdgId, pdg);
    		ids_tmp_vertices_list[i]=v.getVertex_key();
    	}
    	/**
    	 * ids_edges
    	 */
    	for(int i=0; i<eList.size();i++)
    	{
    		IdsEdge e = eList.get(i);
    		ids_edges.put(e.getKey(), e);
    	}
    	/**
    	 * ids_vertices_num
    	 */
    	this.ids_vertices_num+=vList.size();    	
    }
    
    public int getSubIDSIndex() {
        return this.subIDSIndex;
    }
    
    public void setSUbIDSIndex(int index) {
        this.subIDSIndex = index;
    }
    
    public void setWhetherCheckSuperIDS(boolean decision) {
        this.checkSuperIDG = decision;
    }
    
    public boolean willCheckSuperIDS() {
        return this.checkSuperIDG;
    }
    
    /** type :11 formal-in , type :12 formal-out */
    private Hashtable getNodesFromDBByType(Connection conn, int pdg_id, int vertex_kind_id) throws SQLException {
        Hashtable nodes = new Hashtable();
        String sql = "SELECT * FROM vertex WHERE pdg_id = ? and vertex_kind_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pdg_id);
        pstmt.setInt(2,vertex_kind_id);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            Integer vertex_key = new Integer(rset.getInt("VERTEX_KEY"));
            nodes.put(vertex_key,vertex_key);            
        }
        rset.close();
        pstmt.close();      
        return nodes;
    }
    
    public void setWhetherOutputResult(boolean decision) {
        this.outputResult = decision;
    }
    
    public boolean getOutputResult() {
        return this.outputResult;
    }
    
    public boolean evaluateForExtension(Connection conn) throws SQLException {
        // Get the formal-in/out of the toppest level pdg, which are interfaces with the extended Pdg. 
        Hashtable fi_nodes =  this.getNodesFromDBByType(conn,this.topped_pdg_id,11);
        Hashtable fo_nodes =  this.getNodesFromDBByType(conn,this.topped_pdg_id,12);
      
        // check whether the interface nodes (fi/fo_nodes) have been included in the IDS
        boolean has_all_fi_nodes = this.checkContainFIFONodes(fi_nodes);
        boolean has_all_fo_nodes = this.checkContainFIFONodes(fo_nodes);

        // It means that nodes in the toppest PDG has not been included in the IDS yet
        if (!has_all_fi_nodes  && !has_all_fo_nodes) return true;

        Enumeration enum1 = this.lost_edges_info.elements();
        while (enum1.hasMoreElements()) {
            RuleInstanceLostEdge le = (RuleInstanceLostEdge) enum1.nextElement();
            switch (le.getEdgetype()) {
                case 0: if (le.isForward()) {
                            if (has_all_fo_nodes) {
                                if (!checkExtensionByDataDependence(le,conn,fo_nodes)) {
                                    return false;
                                }
                            } 
                        } else {
                            if (has_all_fi_nodes) {
                                if (!checkExtensionByDataDependence(le,conn,fi_nodes)) {
                                    return false;
                                }
                            }
                        }
                        break;
                case 2: if (has_all_fi_nodes) {
                            if (!checkExtensionBySDD(le,conn,fi_nodes)) {
                               return false;
                            }
                        }
                        break;
            }
        }
        return true;
    }
    
    private boolean checkExtensionByDataDependence(RuleInstanceLostEdge le,Connection conn, Hashtable topped_nodes) throws SQLException {

        boolean forward = le.isForward();
        if (forward) {
            return checkExtensionByForwardDataDependence(le,conn,topped_nodes);
        } else {
            return checkExtensionByBackwardDataDependence(le,conn,topped_nodes);
        }
    }
    
    private Hashtable findPathToFINodes(Hashtable ck_fi_nodes, Connection conn, int src_node_vertex_key) throws SQLException {
        String sql = "SELECT v.* " +
                     "FROM   edges e, vertex v " +
                     "WHERE  e.tar_vertex_key = ? AND edge_type = 1 " +
                     "AND    (NOT (v.vertex_kind_id in (7,13,14,15,16))) " +
                     "AND    e.src_vertex_key = v.vertex_key ";
        Hashtable fi_nodes = new Hashtable();
        Hashtable path_nodes = new Hashtable();
        int index = 0;
        int[] path = new int[2000];
        path[0] = src_node_vertex_key;
        path_nodes.put(new Integer(src_node_vertex_key), new Integer(src_node_vertex_key));
        int node_num = 1;
        while (index < node_num && node_num < 2000) {
            int current_node_vertex_key = path[index++];
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next() && node_num < 2000) {
                Integer vertex_key = new Integer(rset.getInt("VERTEX_KEY"));
                int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
                if (vertex_kind_id == 11) { /// Find the formal-in node
                    Object obj = ck_fi_nodes.get(vertex_key);
                    if (obj == null) {
                        ck_fi_nodes.put(vertex_key,vertex_key);
                        fi_nodes.put(vertex_key,vertex_key);
                    }
                }
                Object obj = path_nodes.get(vertex_key);
                if (obj == null) {
                    path[node_num++] = vertex_key.intValue();
                    path_nodes.put(vertex_key,vertex_key);
                }                
            }
            rset.close();
            pstmt.close();
        }
        return fi_nodes;
    }
    
    
    private boolean checkExtensionByBackwardDataDependence(RuleInstanceLostEdge le,Connection conn, Hashtable topped_nodes) throws SQLException {
        int mapped_node_vertex_key = le.getEnd_point_vertex_key();
        Hashtable ck_fi_nodes = new Hashtable();
        Hashtable ck_ai_nodes = new Hashtable();
        Hashtable fi_nodes = findPathToFINodes(ck_fi_nodes,conn,mapped_node_vertex_key);
        Hashtable ai_nodes;
        int count = 0;
        do {
        
            ai_nodes = new Hashtable();
            Enumeration fi_nodes_enum = fi_nodes.elements();
            while (fi_nodes_enum.hasMoreElements()) {
                Integer fi_node_vertex_key = (Integer) fi_nodes_enum.nextElement();
                Object obj = topped_nodes.get(fi_node_vertex_key);
                if (obj != null) return true;
            
                Enumeration temp_ai_nodes_enum = this.getNodeFromDBByDependence(ck_ai_nodes,conn,fi_node_vertex_key.intValue(),3,1,false).elements();
                while (temp_ai_nodes_enum.hasMoreElements()) {
                    Integer temp_ai_node_vertex_key = (Integer) temp_ai_nodes_enum.nextElement(); 
                    int pdg_id = this.getNodePdgId(conn,temp_ai_node_vertex_key.intValue());
                    obj = this.ids_pdg_set.get(new Integer(pdg_id));
                    if (obj != null) {
                        ai_nodes.put(temp_ai_node_vertex_key,temp_ai_node_vertex_key);
                    }
                }
            }
            count = 0;
            fi_nodes = new Hashtable();
            Enumeration ai_nodes_enum = ai_nodes.elements();
            while (ai_nodes_enum.hasMoreElements()) {
                Integer ai_nodes_vertex_key = (Integer) ai_nodes_enum.nextElement();
                Enumeration temp_fi_nodes_enum = this.findPathToFINodes(ck_fi_nodes,conn,ai_nodes_vertex_key.intValue()).elements();
                while (temp_fi_nodes_enum.hasMoreElements()) {
                    Integer fi_node_vertex_key = (Integer) temp_fi_nodes_enum.nextElement();
                    fi_nodes.put(fi_node_vertex_key,fi_node_vertex_key);
                    count++;
                }
            }
            
        } while (count > 0);
    
        return false;
    }
    

    private boolean checkExtensionByForwardDataDependence(RuleInstanceLostEdge le,Connection conn, Hashtable topped_nodes) throws SQLException {
        int mapped_node_vertex_key = le.getEnd_point_vertex_key();
        Hashtable ck_fo_nodes = new Hashtable();
        Hashtable ck_ao_nodes = new Hashtable();
        Hashtable fo_nodes = new Hashtable();
        Hashtable ao_nodes;
        Integer fo_node_vertex_key = new Integer(findDataDependencePathToFO(conn,mapped_node_vertex_key));
        if (fo_node_vertex_key.intValue() == -1) return false; /// no path to formal-out node
        ck_fo_nodes.put(fo_node_vertex_key,fo_node_vertex_key);
        fo_nodes.put(fo_node_vertex_key,fo_node_vertex_key);
        int count = 0;
        do {
            ao_nodes = new Hashtable();
            Enumeration fo_nodes_enum = fo_nodes.elements();
            while (fo_nodes_enum.hasMoreElements()) {
                fo_node_vertex_key = (Integer) fo_nodes_enum.nextElement();
                Object obj = topped_nodes.get(fo_node_vertex_key);
                if (obj != null) return true;
                
                // Find AO nodes on which FO nodes are data dependent
                Hashtable temp_ao_nodes = this.getNodeFromDBByDependence(ck_ao_nodes,conn,fo_node_vertex_key.intValue(),3,2,true);
                Enumeration temp_ao_node_enum = temp_ao_nodes.elements();
                while (temp_ao_node_enum.hasMoreElements()) {
                    Integer ao_node_vertex_key = (Integer) temp_ao_node_enum.nextElement();
                    int ao_node_pdg_id = this.getNodePdgId(conn,ao_node_vertex_key.intValue());
                    obj = this.ids_pdg_set.get(new Integer(ao_node_pdg_id));
                    if (obj != null) {
                        ao_nodes.put(ao_node_vertex_key,ao_node_vertex_key);
                    }
                }
            }
                
            fo_nodes = new Hashtable();    
            Enumeration ao_nodes_enum = ao_nodes.elements();
            count = 0;
            while (ao_nodes_enum.hasMoreElements()) {
                Integer ao_node_vertex_key = (Integer) ao_nodes_enum.nextElement();
                fo_node_vertex_key = new Integer(this.findDataDependencePathToFO(conn,ao_node_vertex_key.intValue()));
                if (fo_node_vertex_key.intValue() != -1) {
                    Object obj = ck_fo_nodes.get(fo_node_vertex_key);
                    if (obj == null) {
                        fo_nodes.put(fo_node_vertex_key,fo_node_vertex_key);
                        ck_fo_nodes.put(fo_node_vertex_key,fo_node_vertex_key);
                        count++;
                    }
                }
            }
              
        } while (count > 0);
        return false;
        
    }
    
    private int findDataDependencePathToFO(Connection conn, int src_vertex_key) throws SQLException {
        String sql = "SELECT v.* " +
                     "FROM   edges e, vertex v " +
                     "WHERE  e.src_vertex_key = ? AND edge_type = 1 " +
                     "AND    (NOT (v.vertex_kind_id in (6,7,13,14,15,16))) " +
                     "AND    e.tar_vertex_key = v.vertex_key ";
        Hashtable path_nodes = new Hashtable();
        int[] path = new int[2000];
        int index = 0;
        path[0] = src_vertex_key;
        int node_num = 1;
        boolean cont = true;
        int fi_node_vertex_key = -1;
        
        // used for checking whether the node has been included in the path
        path_nodes.put(new Integer(src_vertex_key), new Integer(src_vertex_key));
        
        while (index < node_num && cont && node_num < 2000) {
            int current_node_vertex_key = path[index++];
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next() && cont && node_num < 2000) {
                int vertex_key = rset.getInt("VERTEX_KEY");
                int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
                if (vertex_kind_id == 12) { /// Find the formal-out node
                    fi_node_vertex_key = vertex_key;
                    cont = false;
                }
                
                Object obj = path_nodes.get(new Integer(vertex_key));
                if (obj == null) {
                    path[node_num++] = vertex_key;
                    path_nodes.put(new Integer(vertex_key), new Integer(vertex_key));
                }
            }
            rset.close();
            pstmt.close();   
        }
        return fi_node_vertex_key;
        
    }
    
    private Hashtable getNodeFromDBByDependence(Hashtable ck_nodes, Connection conn, int src_vertex_key, int edge_type, int tar_vertex_kind_id,boolean forward) throws SQLException {
       Hashtable nodes = new Hashtable();
       String sql;
       if (forward)
            sql = "SELECT v.vertex_key " +
                  " FROM   edges e, vertex v" +
                  " WHERE  e.src_veretx_key = ? AND e.edge_type = ? " +
                  " AND    e.tar_vertex_key = v.vertex_key AND v.vertex_kind_id = ? ";
       else sql = "SELECT v.vertex_key " +
                  " FROM   edges e, vertex v" +
                  " WHERE  e.tar_veretx_key = ? AND e.edge_type = ? " +
                  " AND    e.src_vertex_key = v.vertex_key AND v.vertex_kind_id = ? ";
       PreparedStatement pstmt = conn.prepareStatement(sql);
       pstmt.setInt(1,src_vertex_key);
       pstmt.setInt(2,edge_type);
       pstmt.setInt(3,tar_vertex_kind_id);
       ResultSet rset = pstmt.executeQuery();
       while (rset.next()) {
           Integer vertex_key = new Integer(rset.getInt("VERTEX_KEY"));
           Object obj = ck_nodes.get(vertex_key);
           if (obj == null) {
               nodes.put(vertex_key,vertex_key);
               ck_nodes.put(vertex_key,vertex_key);
           }
       }
       rset.close();
       pstmt.close();
       return nodes;
    }
    
    private int getNodePdgId(Connection conn, int vertex_key) throws SQLException {
        int pdg_id = 0;
        String sql = "SELECT * FROM vertex WHERE vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) pdg_id = rset.getInt("PDG_ID");
        rset.close();
        pstmt.close();
        return pdg_id;
    }

    private boolean checkExtensionBySDD(RuleInstanceLostEdge le,Connection conn, Hashtable topped_fi_nodes) throws SQLException {
        Vertex mapped_node = (Vertex) this.rids_vertices_hash.get((new Integer(le.getEnd_point_vertex_key())));
        // the fi nodes that have direct data dependence with the mapped_node
        Hashtable fi_ck_nodes = new Hashtable();
        Hashtable ai_ck_nodes = new Hashtable();
        
        int count = 0;

        // get the fi nodes that mapped_node is directly data dependent on
        Hashtable fi_nodes = this.getNodeFromDBByDependence(fi_ck_nodes,conn,mapped_node.getVertex_key(),1,11,false);
        do {
           Hashtable ai_nodes = new Hashtable(); // the AI nodes are used in the next iteration
           Enumeration fi_enum = fi_nodes.elements();
           while (fi_enum.hasMoreElements()) {
               // the fi-node that mapped_node is directly data dependent on
               Integer fi_node_vertex_key = (Integer) fi_enum.nextElement(); 
               Object obj = topped_fi_nodes.get(fi_node_vertex_key);
               if (obj != null) return true;
               Hashtable temp_ai_nodes = this.getNodeFromDBByDependence(ai_ck_nodes,conn,fi_node_vertex_key.intValue(),3,1,false);
               Enumeration temp_ai_enum = temp_ai_nodes.elements();
               while (temp_ai_enum.hasMoreElements()) {
                   Integer ai_node_vertex_key = (Integer) temp_ai_enum.nextElement();
                   Integer ai_node_pdg_id = this.getNodePdgId(conn,ai_node_vertex_key.intValue());
                   obj = this.ids_pdg_set.get(ai_node_pdg_id);
                   if (obj != null) {
                       ai_nodes.put(ai_node_vertex_key,ai_node_vertex_key);
                   } 
               }
           }
           
           fi_nodes = new Hashtable();
           Enumeration ai_nodes_enum = ai_nodes.elements();
           
           count = 0;
           
           while (ai_nodes_enum.hasMoreElements()) {
               Integer ai_node_vertex_key = (Integer) ai_nodes_enum.nextElement();
               Hashtable temp_fi_nodes = this.getNodeFromDBByDependence(fi_ck_nodes,conn,ai_node_vertex_key.intValue(),1,11,false);
               Enumeration temp_fi_nodes_enum = temp_fi_nodes.elements();
               while (temp_fi_nodes_enum.hasMoreElements()) {
                   Integer fi_node_vertex_key = (Integer) temp_fi_nodes_enum.nextElement();
                   count++;
                   fi_nodes.put(fi_node_vertex_key,fi_node_vertex_key);
               }
           }
        } while (count > 0); 
        return false;
    }
    
    
    // Check whether the fi-fo nodes have been included in the IDS
    private boolean checkContainFIFONodes(Hashtable nodes) {
        Enumeration e = nodes.elements();
        while (e.hasMoreElements()) {
            Integer fi_key = (Integer) e.nextElement();
            Object obj = this.ids_vertices_hash.get(fi_key);
            if (obj == null) return false;
        }        
        return true;
    }


    public int getLostNodes() {
        return this.lost_nodes;
    }
    
    public void setMatched() {
        this.matchPattern = true;
    }
    
    public int getLostEdges() {
        return this.lost_edges;
    }
    
    public int getIDS_id() {
        return ids_id;
    }
     
    public void setMatchingResult(int[] mapping_result, int lost_edges, int lost_nodes) {
        this.mapping = new int[pattern.getPattern_vertices_num()];
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            this.mapping[i] = mapping_result[i];
        }
        this.lost_edges = lost_edges;
        this.lost_nodes = lost_nodes;
        if (lost_edges == 0 && lost_nodes == 0) this.matchPattern = true;
        else                                   this.matchPattern = false;
    }
  
    public void outputMatchingResult(Connection conn, int[][][] rc) throws SQLException { 
       // int[][][] rids_edges_in_matrix = getMatrixRidsEdges();        
        int violation_id = this.getViolation_Id(conn);
        this.outputViolationNodes(violation_id,conn);
        this.outputViolationEdges(violation_id,conn,rc);
        this.outputViolationInfo(violation_id,conn);
    }
    
    private void outputViolationInfo(int violation_id, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(ins_violation_info);
        pstmt.setInt(1,pattern.getPattern_key());
        pstmt.setInt(2,violation_id);
        pstmt.setInt(3,rids_vertices_list[0]);
        pstmt.setInt(4,lost_nodes);
        pstmt.setInt(5,lost_edges);
        pstmt.execute();
        pstmt.close();
    }       

    private void outputViolationNodes(int violation_id, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(ins_violation_nodes);
        PreparedStatement missed_pstmt = conn.prepareStatement(ins_instance_missed_node);
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            if (mapping[i] != -1) {
                pstmt.setInt(1,violation_id);
                pstmt.setInt(2,i);
                pstmt.setInt(3,mapping[i]);
                pstmt.execute();
            } else {
                missed_pstmt.setInt(1,violation_id);
                missed_pstmt.setInt(2,i);
            }
        }
        missed_pstmt.close();
        pstmt.close();
    }    


    private int getViolation_Id(Connection conn) throws SQLException {
        int violation_id = 0;
        PreparedStatement pstmt = conn.prepareStatement(qry_violation_id);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        violation_id = rset.getInt(1);
        rset.close();
        pstmt.close();   
        return violation_id;
    }    
    
    private int[][][] getMatrixRidsEdges() {

        int[][][] rids_edges_matrix = new int[rids_vertices_num][rids_vertices_num][3];
        Enumeration enum_rids_edges = this.rids_edges.elements();
        while (enum_rids_edges.hasMoreElements()) {
            IdsEdge edge = (IdsEdge) enum_rids_edges.nextElement();
            int ids_src_node_index = edge.getSrc_node_index();
            int src_node_vertex_key = ids_vertices_list[ids_src_node_index];
            Vertex src_node = (Vertex) ids_vertices_hash.get(new Integer(src_node_vertex_key));
            int ids_tar_node_index = edge.getTar_node_index();
            int tar_node_vertex_key = ids_vertices_list[ids_tar_node_index];
            Vertex tar_node = (Vertex) ids_vertices_hash.get(new Integer(tar_node_vertex_key));
            int edge_type = edge.getEdgetype();
            int length = edge.getLength();
            int rids_src_node_index = src_node.getRids_node_index();
            int rids_tar_node_index = tar_node.getRids_node_index();
            rids_edges_matrix[rids_src_node_index][rids_tar_node_index][edge_type] = length;
        }
        return rids_edges_matrix;
    }    
    
    private void outputViolationEdges(int violation_id, Connection conn, int[][][] re) throws SQLException { 
        PreparedStatement pstmt = conn.prepareStatement(ins_violation_edges);
        PreparedStatement pstmt_miss = conn.prepareStatement(ins_instance_missed_edge);
        int[][][] pe = pattern.getPattern_vertices_edges();
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            for (int j=0; j<pattern.getPattern_vertices_num(); j++) {
                for (int k=0; k<3; k++) {     
                    if (pe[i][j][k]>0) {
                        int m_s_key = mapping[i]; // mapped source node key
                        int m_t_key = mapping[j]; // mapped source node key
                        if (m_s_key!=-1 && m_t_key!=-1) {
                            Vertex vs = getVertexByKeyFromRids(m_s_key);
                            Vertex vt = getVertexByKeyFromRids(m_t_key);
                            if (re[vs.getRids_node_index()][vt.getRids_node_index()][k]>0) {
                                pstmt.setInt(1,violation_id);
                                pstmt.setInt(2,m_s_key);
                                pstmt.setInt(3,m_t_key);
                                pstmt.setString(4,Integer.toString(k));
                                pstmt.execute();
                            } else {
                                pstmt_miss.setInt(1,violation_id);
                                pstmt_miss.setInt(2,i);
                                pstmt_miss.setInt(3,j);
                                pstmt_miss.setInt(4,k);
                                pstmt_miss.execute();                                
                            }
                        } else {
                           pstmt_miss.setInt(1,violation_id);
                           pstmt_miss.setInt(2,i);
                           pstmt_miss.setInt(3,j);
                           pstmt_miss.setInt(4,k);
                           pstmt_miss.execute(); 
                        }
                        if (k == 0 || k == 2) {
                            if (m_s_key != -1 && m_t_key == -1) { //forward
                                Vertex vs = getVertexByKeyFromRids(m_s_key);
                                this.lost_edges_info.add(new RuleInstanceLostEdge(vs.getVertex_key(),true,k));
                            }
                            
                            if (m_s_key == -1 && m_t_key != -1) { //backward
                                Vertex vt = getVertexByKeyFromRids(m_t_key);
                                this.lost_edges_info.add(new RuleInstanceLostEdge(vt.getVertex_key(),false,k));
                            }
                            
                        }
                    }
                }
            }    
        }
        pstmt.close();
        pstmt_miss.close();
    }    
    
    
    public boolean isMatchPattern() {
        return this.matchPattern;
    }
    
    public int getToppest_pdg_id() {
        return this.topped_pdg_id;
    }
    
    public IDS(int ext_pdg_id, Connection conn, IDS subIDS) throws SQLException {
        this.topped_pdg_id = ext_pdg_id;
        this.iteration_num = subIDS.getIteration_number() + 1; /// 1,2,3,4
        this.pattern = subIDS.getPattern();
        this.key_node_target_pdg_id = subIDS.getKey_node_target_pdg_id();
        this.callsite_num = subIDS.getCallsiteNumber();
        int[] ids_temp_vertices_list = new int[15000];
        
        //// 2007-12-29
        this.subIDS_max_vertices_num = subIDS.getIds_vertices_number();
        
        // Duplicate the following information: ids_vertices_num, ids_vertices_hash, ids_temp_vertices_list,
        //                                      ids_edges, ids_pdg_set
        
        
        this.copySubIds(subIDS,ids_temp_vertices_list);        
        PdgForPhaseOne pdg = new PdgForPhaseOne(ext_pdg_id,-1,this.iteration_num-1,true); // 0,1,2,3
        this.ids_pdg_set.put(pdg.getPdg_id_Key(),pdg);
        this.expandIds(conn,ids_temp_vertices_list);
            
        if (this.ids_vertices_num > this.subIDS_max_vertices_num) { // The IDS has been expanded
            this.buildIdsSDDEdge(conn);
            buildRids(conn,subIDS);            
        }
    }

    public Set<Integer> getIdsPdgIds()
    {
    	return this.ids_pdg_set.keySet();
    }
    
    private AdjacentEdge[] buildAdjacentEdgesOfIDS(int edge_type) {
         
        int[] forward_edge_num = new int[ids_vertices_num];
        int[] backward_edge_num = new int[ids_vertices_num];
        for (int i=0; i<ids_vertices_num; i++) {
            forward_edge_num[i] = 0;
            backward_edge_num[i] = 0;
        }
        
        Enumeration ee_enum = ids_edges.elements();
        while (ee_enum.hasMoreElements()) {
            IdsEdge edge = (IdsEdge) ee_enum.nextElement();
            int src_node_index = edge.getSrc_node_index();
            int tar_node_index = edge.getTar_node_index();
            if (edge.getEdgetype()==edge_type) {
                forward_edge_num[src_node_index]++;
                backward_edge_num[tar_node_index]++;
            }
        }
        
        // Build Edge Information
        AdjacentEdge[] adj_edges = new AdjacentEdge[ids_vertices_num];
        for (int i=0; i<ids_vertices_num; i++) {
        	/**
        	 * Boya's Note: For each vertex, forward_edge_num[i]: in degree
        	 * 								 backward_edge_num[i]: out degree
        	 */
            adj_edges[i] = new AdjacentEdge(edge_type,forward_edge_num[i],backward_edge_num[i]); 
        }

        ee_enum = ids_edges.elements();
        while (ee_enum.hasMoreElements()) {
            IdsEdge edge = (IdsEdge) ee_enum.nextElement();
            int src_node_index = edge.getSrc_node_index();
            int tar_node_index = edge.getTar_node_index();
            if (edge.getEdgetype()==edge_type) {
                adj_edges[src_node_index].addForwardNeighbor(tar_node_index); /**Boya's Note: The detailed outgoing edges*/
                adj_edges[tar_node_index].addBackwardNeighbor(src_node_index);/**Boya's Note: The detailed incoming edges*/
            }
        }    
        return adj_edges;
    }
    /**
     * Boya's note: Find in the forwarding direction the **intraprocedural** ai and cp neighbors for the current sdd_node
     * 
     * @param adj_edges
     * @param sdd_node
     * @param ai_neighbors
     * @param cp_neighbors
     */
    private void findNodesForSDDEdge(AdjacentEdge[] adj_edges, Vertex sdd_node, Vector ai_neighbors, Vector cp_neighbors) {
        int sdd_node_index = sdd_node.getNode_index();
        /**
         * Boya's Note: Only consider forward direction:
         * Consider all nodes that uses the definition of sdd_node;
         * Then build SDDE edges among these neighboring nodes
         */
        int[] target_nodes = adj_edges[sdd_node_index].getForward_neighbors(); 
        int   target_node_number = adj_edges[sdd_node_index].getForward_neighbor_number();
        for (int i=0; target_nodes!= null && i<target_node_number; i++) {
            int target_node_index = target_nodes[i]; 
            int vertex_key = ids_vertices_list[target_node_index];
            Vertex v = (Vertex) ids_vertices_hash.get(new Integer(vertex_key));
            if (sdd_node.getPdg_id() == v.getPdg_id()) { //Intraprocedural
                if (v.getVertex_kind_id() == 1) ai_neighbors.add(v);
                if (v.getVertex_kind_id() == 6) cp_neighbors.add(v);
            }
        }
    }

    private Vertex findFormalInNode(int ai_node_vertex_key, AdjacentEdge[] adj_edges) throws SQLException {   
        Vertex ai_node = (Vertex) ids_vertices_hash.get(new Integer(ai_node_vertex_key));
        AdjacentEdge edge = adj_edges[ai_node.getNode_index()];
        int[] forward_neighbors = edge.getForward_neighbors();
        int   forward_neighbor_num = edge.getForward_neighbor_number();
        for (int i=0; forward_neighbors!=null && i<forward_neighbor_num; i++) {
            int neighbor_node_index = forward_neighbors[i];
            int neighbor_vertex_key = ids_vertices_list[neighbor_node_index];
            Vertex v = (Vertex) ids_vertices_hash.get(new Integer(neighbor_vertex_key));
            if (v.getVertex_kind_id() == 11) return v;
        }
        return null;
    }    


    // Find all AI nodes that are data dependence on src_node
    private Vector findAllEqvAINodes( AdjacentEdge[] adj_edges, Vertex src_node) throws SQLException {
        
        Vector eqv_ai_nodes = new Vector(); // Store the actual-in node that share data dependence with src_node
        Hashtable eqv_ai_node_hash = new Hashtable();
        int[] ai_node_vertex_key_list = new int [2000];
        
        int num_ai_node = 0;
        ai_node_vertex_key_list[num_ai_node++] = src_node.getVertex_key();
        eqv_ai_node_hash.put(src_node.getVertex_key_Integer(),src_node.getVertex_key_Integer());
        
        int index = 0;
        while (index < num_ai_node && num_ai_node<200) {
            
            int current_ai_node_vertex_key = ai_node_vertex_key_list[index++];            
            Vertex current_fi_node =this.findFormalInNode(current_ai_node_vertex_key,adj_edges);
             
            // Find the formal in node that is data dependent on current_ai_node 
            if (current_fi_node != null) {  // The formal-in parameter is in IDS
                
                // Find those nodes that are data dependent on the formal-in node
                int current_fi_node_index = current_fi_node.getNode_index();
                int[] current_fi_node_neighbors = adj_edges[current_fi_node_index].getForward_neighbors();
                int   current_fi_node_neighbor_num = adj_edges[current_fi_node_index].getForward_neighbor_number();
                
                for (int i=0; current_fi_node_neighbors != null && i < current_fi_node_neighbor_num; i++) {
                    int next_ai_node_index = current_fi_node_neighbors[i];
                    int next_ai_node_vertex_key = ids_vertices_list[next_ai_node_index];
                    Vertex next_ai_node = (Vertex) ids_vertices_hash.get(new Integer(next_ai_node_vertex_key));
                   
                    if (next_ai_node.getVertex_kind_id() == 1) { // Ensure the next_ai_node is an actual-in                         
                        Object obj = eqv_ai_node_hash.get(next_ai_node.getVertex_key_Integer());
                        if (obj == null) {
                            eqv_ai_nodes.add(next_ai_node);
                            ai_node_vertex_key_list[num_ai_node++] = next_ai_node_vertex_key;
                            eqv_ai_node_hash.put(next_ai_node.getVertex_key_Integer(),next_ai_node.getVertex_key_Integer());
                        }
                    }
                }
            }
        }
        return eqv_ai_nodes;
    }  


    private boolean involvePattern_Cp_AI_SDD(Vector eqv_ai_nodes) {
        Hashtable cp_to_ai_labels = pattern.getCp_to_ai_labels();
        Enumeration eqv_ai_enum = eqv_ai_nodes.elements();
        while (eqv_ai_enum.hasMoreElements()) {
            Vertex v = (Vertex) eqv_ai_enum.nextElement();
            Object obj = cp_to_ai_labels.get(Integer.toString(v.getVertex_label()));
            if (obj != null) return true;
        }
        return false;
    }
    /**
     * Boya's note: Checking the field names of src_node and tar_node; the following 4 cases are desirable:
     * (1) both field names are empty	
     * 		Ex: if(a) f(a)
     * (2) They have the same field names
     * 		Ex: if(a->x) f(a->x)
     * (3) cp field name is not empty but ai field name is empty; 
     * 		Ex: if(a->x) f(a)
     * (4) cp field name is empty bug ai field name is not empyt
     * 		Ex: if(a) f(a->x)
     * 
     * @param src_node
     * @param tar_node
     * @return
     */
    private boolean intrestedSDDFromControlToAI(Vertex src_node, Vertex tar_node) {
        String src_fieldname = src_node.getFieldname1();
        String tar_fieldname = tar_node.getFieldname1();
        
        if (src_fieldname.equals("") && tar_fieldname.equals("")) return true;
        if (!tar_fieldname.equals("") && !src_fieldname.equals("") && tar_fieldname.equals(src_fieldname)) return true;
        if (tar_fieldname.equals("")  && !src_fieldname.equals("")) {
            return true;   
        }
        if (!tar_fieldname.equals("") &&  src_fieldname.equals("")) { 
           return true;       
        }        
        return false;
    }    
    
    private Vertex getCallsiteNode(Connection conn, Vertex src_node) throws SQLException {
        String sql = "SELECT v.* FROM edges e, vertex v " +
                     "WHERE  e.tar_vertex_key = ? and e.src_vertex_key = v.vertex_key " +
                     "AND    e.edge_type = 2";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,src_node.getVertex_key());
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int vertex_key = rset.getInt("VERTEX_KEY");
        int vertex_label = rset.getInt("VERTEX_LABEL");
        int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
        int startline = rset.getInt("STARTLINE");
        String fieldname1 = rset.getString("FIELDNAME1");
        String fieldname2 = rset.getString("FIELDNAME2");
        int pdg_id = rset.getInt("PDG_ID");
        Vertex v = new Vertex(0,vertex_key,vertex_label,vertex_kind_id,startline,0,fieldname1,fieldname2,pdg_id);
        rset.close();
        pstmt.close();
        return v;
    }
    
    public Vertex getVertexByRidsIndex(int index) {
        int vertex_key = this.rids_vertices_list[index];
        Vertex v = (Vertex) rids_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
    
   public Vertex getVertexByKeyFromIDS(int vertex_key) {
        Vertex v = (Vertex) ids_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
    
    public boolean checkNodeInRids(int vertex_key) {
        Object obj = rids_vertices_hash.get(new Integer(vertex_key));
        if (obj == null) return false;
        else             return true;
    }
    
    public Vertex getVertexByKeyFromRids(int vertex_key) {
        Vertex v = (Vertex) rids_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
    
    
    // 0: without path,  1: with path, 2: without data in cfg_path
    private int checkExistingCfgPath(Connection conn, Vertex src_node, Vertex tar_node) throws SQLException {
        int check_result = 2;
        String sql = "SELECT * FROM cfg_path WHERE src_vertex_key = ? AND tar_vertex_key = ? ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,src_node.getVertex_key());
        pstmt.setInt(2,tar_node.getVertex_key());
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
            check_result = rset.getInt("PATH");
        }
        rset.close();
        pstmt.close();
        return check_result;
    }
    
    private boolean checkCfgPath(Connection conn, Vertex src_node, Vertex tar_node) throws SQLException {
          
        String sql = "select v.* " + 
                     "from   cfg_edges c, vertex v " + 
                     "where  c.src_vertex_key = ? " + 
                     "and    c.src_pdg_id = c.tar_pdg_id " + 
                     "and    c.tar_vertex_key = v.vertex_key " + 
                     "and    (not v.vertex_kind_id in (1,2,23,24))"; 
        Vertex source_node;
        Vertex target_node;
        if (src_node.getVertex_kind_id()==1) source_node = this.getCallsiteNode(conn,src_node);
        else                                 source_node = src_node;
        if (tar_node.getVertex_kind_id()==1) target_node = this.getCallsiteNode(conn,tar_node);
        else                                 target_node = tar_node;
        if (source_node == null || target_node == null) return false;

        //2008-05-28  original: check_result = this.checkExistingCfgPath(conn,src_node,tar_node);
        /**Boya's Note: If returned 0 and 1, meaning that there exists entries from source_node to target_node in CFG_PATH;
         * Otherwise, check existance all over again
         */
        int check_result = this.checkExistingCfgPath(conn,source_node,target_node);
        if (check_result == 0) return false;
        if (check_result == 1) return true;

        if (source_node.getVertex_key() == 280811 && target_node.getVertex_key() == 292547) {
            System.out.println("Wait");
        }


        int target_node_startline = target_node.getStartline();    
        int[][] cfg_path = new int[20000][3];
        cfg_path[0][0] = source_node.getVertex_key();
        cfg_path[0][1] = source_node.getStartline();
        cfg_path[0][2] = source_node.getStartline();
        int path_vertices_num = 1;
        int index = 0;
        Hashtable path_nodes = new Hashtable();    
        path_nodes.put(source_node.getVertex_key_Integer(),source_node.getVertex_key_Integer());
        boolean foundPath = false; 
        
        while (index < path_vertices_num && !foundPath) {
            int current_node_vertex_key = cfg_path[index][0];
            int current_node_startline = cfg_path[index][1];
            int previous_node_startline = cfg_path[index][2];
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next() && !foundPath) {
                int next_node_vertex_key = rset.getInt("VERTEX_KEY");
                int next_node_startline = rset.getInt("STARTLINE");
                if (next_node_vertex_key == target_node.getVertex_key()) foundPath = true;
                                
              //  if (next_node_startline>=current_node_startline && next_node_startline <= target_node_startline) {
                 boolean valid_next_node;
                 
                 //TODO: Why are so many heuristics on lines? Why not just directly check on the CFG?
   ///////////////////////////////////////////////////////////////////////////////////
                 // 2008-05-28
                 if (target_node_startline >= next_node_startline) {
                    valid_next_node = true;
                 } else {
  //////////////////////////////////////////////////////////////////////////////////
                     if (current_node_startline >= previous_node_startline) {
                         if (next_node_startline <= target_node_startline) valid_next_node = true;
                         else                                              valid_next_node = false;
                     } else {
                         if (next_node_startline >= previous_node_startline && next_node_startline <= target_node_startline) valid_next_node = true;
                         else {
                        //2008-05-28
                            if (next_node_startline+2>=previous_node_startline) valid_next_node = true; 
                            else valid_next_node = false;   
                         }
                     }
                 }

     //           valid_next_node = true;

                if (valid_next_node) {
                    Object obj = path_nodes.get(new Integer(next_node_vertex_key));
                    if (obj == null) {
                        cfg_path[path_vertices_num][0] = next_node_vertex_key;
                        cfg_path[path_vertices_num][1] = next_node_startline;
                        cfg_path[path_vertices_num][2] = current_node_startline;
                        path_vertices_num++;
                        path_nodes.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                    }
                }
            } 
            rset.close();
            pstmt.close();

            index++;
        }
        insertExistCfgPath(conn,src_node,tar_node,foundPath);        
        return foundPath;
    }   
    
    private void insertExistCfgPath(Connection conn, Vertex src_node, Vertex tar_node, boolean foundPath) throws SQLException {
        String sql = "INSERT INTO cfg_path VALUES(?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,src_node.getVertex_key());
        pstmt.setInt(2,tar_node.getVertex_key());
        if (foundPath) pstmt.setInt(3,1);
        else           pstmt.setInt(3,0);
        pstmt.execute();
        pstmt.close();
    }
    
    
    /**
     * Boya's Note:
     * 
     * For each pair of CP and AI:
     * 	Find all AI' that are interprocedually data dependent on AI
     * 	IF there is already an SDDE from CP to AI, then add CP->AI'
     * 	Otherwise, if 
     * 				(1) CP and AI are in the same function
     * 				(2) There is cfg path between CP to AI
     * 				(3) CP occurs before AI in the function
     * 				(4) Field names of CP and AI satisfies some properties
     * 				(5) AI has an incoming SDDE from CP to AI in the pattern
     * Then add SDDE between CP->AI and also CP->AI'
     * 
     * @param adj_edges
     * @param cp_neighbors cp_neighbors of an implicit sdd node
     * @param ai_neighbors ai_neighbors of an implicit sdd node
     * @param conn
     * @throws SQLException
     */
    private void buildSDDFromControlToActualIn(AdjacentEdge[] adj_edges, Vector cp_neighbors, Vector ai_neighbors, Connection conn) throws SQLException {
        
        Hashtable cp_to_ai_labels = pattern.getCp_to_ai_labels(); 
        Enumeration cp_enum = cp_neighbors.elements();
        while (cp_enum.hasMoreElements()) {    
            Vertex cp_node = (Vertex) cp_enum.nextElement();    
 
            Enumeration ai_enum = ai_neighbors.elements();
            while (ai_enum.hasMoreElements()) {
            
                          
                Vertex ai_node = (Vertex) ai_enum.nextElement();
                                        
                // Find those actual-in nodes that are interprocedural data dependent on src_node. 
                Vector eqv_ai_nodes = this.findAllEqvAINodes(adj_edges,ai_node);
                eqv_ai_nodes.add(ai_node);
                
                boolean involve_p_cp_ai_sdd = this.involvePattern_Cp_AI_SDD(eqv_ai_nodes);
                                
                if (involve_p_cp_ai_sdd) {

                    IdsEdge edge = new IdsEdge(cp_node.getNode_index(),ai_node.getNode_index(),2);
                    Object obj = ids_edges.get(edge.getKey()); // if not null, then cp-->ai_node is a useful SDD edges 
                    
                    /**
                     * Boya's Note: If not null, it indicates that the cp->ai edge already exists
                     * in the ids_edge set. In this case, we can directly add all edges from cp to every other ai in eqv_ai_nodes into the graph,
                     * without having to make the additional checkings in the 2nd, 3rd line of the following predicate
                     */                                    
                    if (obj != null || 
                       (cp_node.getPdg_id() == ai_node.getPdg_id() && cp_node.getStartline()<=ai_node.getStartline() && 
                        this.intrestedSDDFromControlToAI(cp_node,ai_node) && this.checkCfgPath(conn,cp_node,ai_node))) {
                
                        Enumeration eqv_enum = eqv_ai_nodes.elements();
                        while (eqv_enum.hasMoreElements()) {
                            Vertex eqv_ai_node = (Vertex) eqv_enum.nextElement();
                            obj = cp_to_ai_labels.get(Integer.toString(eqv_ai_node.getVertex_label()));
                            if (obj != null) { // if obj == null, the eqv_ai node is a node without sdd edge in the pattern
                                edge = new IdsEdge(cp_node.getNode_index(),eqv_ai_node.getNode_index(),2);
                                obj = this.ids_edges.get(edge.getKey());
                                if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                            }
                        } 
                    } 
                } 
            } 
        }   
    }


    private boolean involvedInAItoAISDDTargetNode(Vector eqv_ai_nodes, Hashtable labels) {
       // Hashtable ai_to_ai_tar_labels = pattern.getAi_to_ai_tar_labels();
        Enumeration enum_eqv_ai_nodes = eqv_ai_nodes.elements();
        while (enum_eqv_ai_nodes.hasMoreElements()) {
            Vertex eqv_ai_node = (Vertex) enum_eqv_ai_nodes.nextElement();
            Object obj = labels.get(Integer.toString(eqv_ai_node.getVertex_label()));
            if (obj != null) return true;
        }
        return false;
    }

    private boolean isIntrestedSDDFromAItoAI(Vertex src_node, Vertex tar_node) {
        String src_fieldname = src_node.getFieldname1();
        String tar_fieldname = tar_node.getFieldname1();
        if (tar_fieldname.equals("") && src_fieldname.equals("")) return true;
        if (!tar_fieldname.equals("") && !src_fieldname.equals("") && tar_fieldname.equals(src_fieldname)) return true;
        if (tar_fieldname.equals("")  && !src_fieldname.equals("")) {
            return true;                           
        }
        if (!tar_fieldname.equals("") &&  src_fieldname.equals("")) { 
            return true;        
        }            
        return false;        
        
    }

    /**
     * 
     * Boya's Notes:
     * 
     * @param adj_edges
     * @param ai_neighbors
     * @param conn
     * @throws SQLException
     */
    private void buildSDDBetweenActualIn(AdjacentEdge[] adj_edges, Vector ai_neighbors, Connection conn) throws SQLException{

        Hashtable ai_to_ai_labels = pattern.getAi_to_ai_labels();
        Enumeration ai_enum = ai_neighbors.elements();
        Hashtable ai_eqv_node_hash = new Hashtable();
        while (ai_enum.hasMoreElements()) {
            Vertex ai_node = (Vertex) ai_enum.nextElement();
            Vector eqv_ai_nodes = this.findAllEqvAINodes(adj_edges,ai_node);
            eqv_ai_nodes.add(ai_node);
            ai_eqv_node_hash.put(ai_node.getVertex_key_Integer(),eqv_ai_nodes);
        }
        

        Enumeration src_ai_ee = ai_neighbors.elements();       
        while (src_ai_ee.hasMoreElements()) {

            Vertex src_ai_node = (Vertex) src_ai_ee.nextElement();
            Vector src_eqv_ai_nodes = (Vector) ai_eqv_node_hash.get(src_ai_node.getVertex_key_Integer()); 
            //Object obj = ai_to_ai_src_labels.get(Integer.toString(src_ai_node.getVertex_label()));
            //if (obj != null) {  // No SDD edges are incident from such node. 
            /**
             * Boya's note: As long as one of the equivalent nodes have been involved in the pattern, building SDDE is allowed
             * But before adding an SDDE edge, still check to make sure that both end nodes are in the pattern
             */
            if (involvedInAItoAISDDTargetNode(src_eqv_ai_nodes,pattern.getAi_to_ai_src_labels())) {
                Enumeration tar_ai_ee = ai_neighbors.elements();
                
                while (tar_ai_ee.hasMoreElements()) {
                
                    Vertex tar_ai_node = (Vertex) tar_ai_ee.nextElement();
                    
                    if (src_ai_node.getVertex_key() != tar_ai_node.getVertex_key()) {
                
//                        Vector eqv_ai_nodes = this.findAllEqvAINodes(adj_edges,src_ai_node);
//                        eqv_ai_nodes.add(tar_ai_node);
                        Vector tar_eqv_ai_nodes = (Vector) ai_eqv_node_hash.get(tar_ai_node.getVertex_key_Integer());
                        
                        if (this.involvedInAItoAISDDTargetNode(tar_eqv_ai_nodes,pattern.getAi_to_ai_tar_labels())) {
                           
                           IdsEdge edge = new IdsEdge(src_ai_node.getNode_index(),tar_ai_node.getNode_index(),2);
                           Object obj = ids_edges.get(edge.getKey());
                           
                           if (obj != null ||
                               (src_ai_node.getPdg_id() == tar_ai_node.getPdg_id() && src_ai_node.getStartline() < tar_ai_node.getStartline() &&
                                this.isIntrestedSDDFromAItoAI(src_ai_node,tar_ai_node) && 
                                this.checkCfgPath(conn,src_ai_node,tar_ai_node))) {
                               
                               Enumeration src_eqv_ai_node_enum = src_eqv_ai_nodes.elements();
                               
                               while (src_eqv_ai_node_enum.hasMoreElements()) {
                                   Vertex src_eqv_ai_node = (Vertex) src_eqv_ai_node_enum.nextElement();
                                   
                                   Enumeration tar_eqv_ai_node_enum = tar_eqv_ai_nodes.elements();
                                   while (tar_eqv_ai_node_enum.hasMoreElements()) {

                                       Vertex tar_eqv_ai_node = (Vertex) tar_eqv_ai_node_enum.nextElement();
                                       String key = Integer.toString(src_eqv_ai_node.getVertex_label()) + "-" +Integer.toString(tar_eqv_ai_node.getVertex_label());
                                       obj = ai_to_ai_labels.get(key);
                                       if (obj != null) {
                                           edge = new IdsEdge(src_eqv_ai_node.getNode_index(),tar_eqv_ai_node.getNode_index(),2);
                                           obj = this.ids_edges.get(edge.getKey());
                                           if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                                       } 
                                   }
                               }
                           }
                        }
                    }
                }
            }
        }        
        
/*        
        Enumeration src_ai_ee = ai_neighbors.elements();       
        while (src_ai_ee.hasMoreElements()) {

            Vertex src_ai_node = (Vertex) src_ai_ee.nextElement();
            
            Object obj = ai_to_ai_src_labels.get(Integer.toString(src_ai_node.getVertex_label()));
            if (obj != null) {  // No SDD edges are incident from such node. 
            
                Enumeration tar_ai_ee = ai_neighbors.elements();
                
                while (tar_ai_ee.hasMoreElements()) {
                
                    Vertex tar_ai_node = (Vertex) tar_ai_ee.nextElement();
                    
                    if (src_ai_node.getVertex_key() != tar_ai_node.getVertex_key()) {
                
                        Vector eqv_ai_nodes = this.findAllEqvAINodes(adj_edges,src_ai_node);
                        eqv_ai_nodes.add(tar_ai_node);
                        
                        if (this.involvedInAItoAISDDTargetNode(eqv_ai_nodes)) {
                           
                           IdsEdge edge = new IdsEdge(src_ai_node.getNode_index(),tar_ai_node.getNode_index(),2);
                           obj = ids_edges.get(edge.getKey());
                           
                           if (obj != null ||
                               (src_ai_node.getPdg_id() == tar_ai_node.getPdg_id() && src_ai_node.getStartline() < tar_ai_node.getStartline() &&
                                this.isIntrestedSDDFromAItoAI(src_ai_node,tar_ai_node) && this.checkCfgPath(conn,src_ai_node,tar_ai_node))) {
                        
                               Enumeration eqv_ai_node_enum = eqv_ai_nodes.elements();     
                               while (eqv_ai_node_enum.hasMoreElements()) {
                                   Vertex eqv_ai_node = (Vertex) eqv_ai_node_enum.nextElement();
                                   String key = Integer.toString(src_ai_node.getVertex_label()) + "-" +Integer.toString(eqv_ai_node.getVertex_label());
                                   obj = ai_to_ai_labels.get(key);
                                   if (obj != null) {
                                       edge = new IdsEdge(src_ai_node.getNode_index(),eqv_ai_node.getNode_index(),2);
                                       obj = this.ids_edges.get(edge.getKey());
                                       if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                                   } 
                               }
                           }
                        }
                    }
                }
            }
        } */    
    }     

    /**
     * Boya's Note: 
     * (1)Build **data** adjacent edges (forward and backward incident edges) for each node
     * (2)For each vertices in IDS:
     * 		Get current node, and its ai and cp neighbors;
     * 		Build CP-AI SDDE
     * 		Build AI-AI SDDE
     * @param conn
     * @throws SQLException
     */
    protected void buildIdsSDDEdge(Connection conn) throws SQLException {
        
        // Build the adjacent data edges of IDS
        AdjacentEdge[] data_adj_edges = this.buildAdjacentEdgesOfIDS(0);       
        
        for (int i=0; i<ids_vertices_num; i++) {
            int sdd_vertex_key = ids_vertices_list[i];
            Vertex sdd_node = (Vertex) ids_vertices_hash.get(new Integer(sdd_vertex_key));
            

            
            Vector ai_neighbors = new Vector();
            Vector cp_neighbors = new Vector();             
            // Find the control point nodes and actual-in nodes that are data dependent on the sdd node
            this.findNodesForSDDEdge(data_adj_edges,sdd_node,ai_neighbors,cp_neighbors);          
            buildSDDFromControlToActualIn(data_adj_edges,cp_neighbors,ai_neighbors,conn);
            buildSDDBetweenActualIn(data_adj_edges,ai_neighbors,conn);                  
        }  
    }
    
    /**
     * Boya's Note: 
     * Starting from the src_node, traverse the incident data edges in a BFS way untill a node in the RIDS is encountered; if a RIDS node is 
     * encountered, then backtrack.
     * 
     * Note that this is **NOT** really building the TRANSITIVE CLOSURE; it is in fact just building RIDS edges, which could be paths in the 
     * oritinal PDG.
     * 
     * @param adj_edges
     * @param src_node
     */
    private void buildTransitiveDataDependence(AdjacentEdge[] adj_edges, Vertex src_node) {
        int index = 0;
        int[][] path = new int[15000][2]; /**Boya's Note: path[i][1] stores the length of the path from src_node to the node path[i][0]*/ 
        Hashtable path_hash = new Hashtable();
        path_hash.put(src_node.getVertex_key_Integer(),src_node.getVertex_key_Integer());
        path[0][0] = src_node.getVertex_key();
        path[0][1] = 0;
        int path_num = 1;
        while (index < path_num) {
            int current_node_vertex_key = path[index][0];
            Vertex current_node = (Vertex) ids_vertices_hash.get(new Integer(current_node_vertex_key));
            int current_node_index = current_node.getNode_index();
            int[] neighbors = adj_edges[current_node_index].getForward_neighbors();
            for (int i=0; neighbors != null && i<adj_edges[current_node_index].getForward_neighbor_number(); i++) {
                int next_node_index = neighbors[i];
                int next_node_vertex_key = ids_vertices_list[next_node_index];
                Vertex next_node = (Vertex) ids_vertices_hash.get(new Integer(next_node_vertex_key));
                if (isRidsVertex(next_node)) {  // next_node is a node in the RIDS
                	/**
                	 * BUG FIX
                	 */
                    addEdgeIntoRids(new IdsEdge(src_node.getRids_node_index(),next_node.getRids_node_index(),0,path[index][1]+1));
                } else {
                    if (!this.findItemInHash(path_hash,next_node.getVertex_key_Integer())) {
                        path_hash.put(next_node.getVertex_key_Integer(),next_node.getVertex_key_Integer());
                        path[path_num][0] = next_node.getVertex_key();
                        path[path_num][1] = path[index][1] + 1; 
                        path_num++;
                    }
                }
            }
            index++;
        }
    }
    
    private boolean findItemInHash(Hashtable h, Integer key) {
        Object obj = h.get(key);
        if (obj == null) return false;
        return true;
    }
    
    private void addEdgeIntoRids(IdsEdge edge) {
        Object obj = rids_edges.get(edge.getKey());
        if (obj == null) rids_edges.put(edge.getKey(),edge);
    }
    
    private boolean isRidsVertex(Vertex v) {
        Object obj = rids_vertices_hash.get(v.getVertex_key_Integer());
        if (obj == null) return false;
        else             return true;
    }
    
    private boolean isRidsVertex(int vertex_key) {
        Object obj = rids_vertices_hash.get(new Integer(vertex_key));
        if (obj == null) return false;
        else             return true;
    }
    
    private void buildTransitiveControlDependence(Connection conn, Vertex src_node) throws SQLException {
        int index = 0;
        int path_num = 0;
        Hashtable path_hash = new Hashtable();
        int[][] path = new int[15000][2];
        path_hash.put(src_node.getVertex_key_Integer(),src_node.getVertex_key_Integer());
        path[path_num][0] = src_node.getVertex_key();
        path[path_num][1] = 0;
        path_num++;
        while (index < path_num && path_num < 2000) {
            int current_node_vertex_key = path[index][0];
            PreparedStatement pstmt = conn.prepareStatement(control_sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
                int next_node_vertex_key = rset.getInt("TAR_VERTEX_KEY");
                int next_node_pdg_id = rset.getInt("TAR_PDG_ID");
                int id = rset.getInt("VERTEX_KIND_ID");
                if (this.isRidsVertex(next_node_vertex_key)) {
                    Vertex next_node = (Vertex) rids_vertices_hash.get(new Integer(next_node_vertex_key));
                    /**
                     * BUG FIX
                     */
                    this.addEdgeIntoRids(new IdsEdge(src_node.getRids_node_index(),next_node.getRids_node_index(),1,path[index][1]+1));
                } else {
                    if ((id >17 || id < 13) && id !=3 && isValidPdg(next_node_pdg_id)) {
                        if (!this.findItemInHash(path_hash,new Integer(next_node_vertex_key))) {
                            path[path_num][0] = next_node_vertex_key;
                            path[path_num][1] = path[index][1] + 1;
                            path_num++;
                            path_hash.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                        }
                    }
                }
            }
            rset.close();
            pstmt.close();
            index++;
        }
    }
    
    private boolean isValidPdg(int pdg_id) {
        Object obj = ids_pdg_set.get(new Integer(pdg_id));
        if (obj == null) return false;
        else             return true;
    }
    
    
    
    public void buildRids(Connection conn, IDS subIDS) throws SQLException {
        buildRidsNodes();
        if (subIDS != null) copySubRidsEdges(subIDS);
        
        AdjacentEdge[] adj_edges = this.buildAdjacentEdgesOfIDS(0);
        
        ////////Build Rids Data dependence        
        for (int i=0; i<this.rids_vertices_num; i++) {
            int vertex_key = rids_vertices_list[i];
            Vertex v = (Vertex) rids_vertices_hash.get(new Integer(vertex_key));
            buildTransitiveDataDependence(adj_edges,v);    
        }
        
        /**
         * Boya's Note: Build RIDS control dependence
         */
        for (int i=0; i<this.rids_vertices_num; i++) {
            int vertex_key = rids_vertices_list[i];
            Vertex v = (Vertex) rids_vertices_hash.get(new Integer(vertex_key));
            buildTransitiveControlDependence(conn,v);
        }

        /**
         * Boya's Note: Build RIDS SDDE
         */
        for (int i=0; i<this.rids_vertices_num; i++) {
            Vertex vi = this.getVertexByRidsIndex(i);
            for (int j=0; j<this.rids_vertices_num; j++) {
                Vertex vj = this.getVertexByRidsIndex(j);
                if (i != j) {   
                    /////////////////////////////////////////
                	/**
                	 * BUG FIX
                	 */
                	/**
                	 * FIX-ON-FIX
                	 */
                    IdsEdge edgeIDS = new IdsEdge(vi.getNode_index(),vj.getNode_index(),2); 
                    Object obj = this.ids_edges.get(edgeIDS.getKey());      
                    if (obj != null) {                 
                        this.addEdgeIntoRids(new IdsEdge(vi.getRids_node_index(),vj.getRids_node_index(),2));
                    }            
                }
                
            }
        }
        
    
    }
    
    
    // Copy sub-RIDS edges
    private void copySubRidsEdges(IDS subIDS) {
        Hashtable sub_rids_edges = subIDS.getRids_edges();
        Enumeration enum_edges = sub_rids_edges.elements();
        while (enum_edges.hasMoreElements()) {
            IdsEdge edge = (IdsEdge) enum_edges.nextElement();
            this.rids_edges.put(edge.getKey(),edge);
        }
    }
    
    // build nodes for the RIDS
    private void buildRidsNodes() {
        int[] rids_temp_vertices_list = new int[MAX_VERTICES_NUM_1*2];
        Hashtable frq_label_hash = pattern.getPattern_vertices_label();
        for (int i=0; i<ids_vertices_num; i++) {
            int vertex_key = ids_vertices_list[i];
            Vertex v = (Vertex) ids_vertices_hash.get(new Integer(vertex_key));
            boolean frq_label = false;
            
            /// Only control points and those nodes whose labels are in the pattern are included in the RIDS
            if (v.getVertex_kind_id()==6) frq_label = true;
            Object obj = frq_label_hash.get(new Integer(v.getVertex_label()));
            if (obj != null) frq_label = true;
            if (frq_label) {
               rids_temp_vertices_list[rids_vertices_num] = v.getVertex_key();
               rids_vertices_hash.put(v.getVertex_key_Integer(),v);
               v.setRids_node_index(rids_vertices_num);
               rids_vertices_num++;
            }
        }
        
        rids_vertices_list = new int[rids_vertices_num];
        for (int i=0; i<rids_vertices_num; i++) {
            rids_vertices_list[i] = rids_temp_vertices_list[i];
        }
        
    }    
    

    protected boolean isIntrestingPdg(Vertex new_node, Vertex current_node, boolean direction) throws SQLException {
        Object obj = ids_pdg_set.get(new_node.getPdg_id_Integer()); // The pdg has been included in the subIDS
        if (obj != null) return true;
        
        PdgForPhaseOne current_node_pdg = (PdgForPhaseOne) ids_pdg_set.get(current_node.getPdg_id_Integer());  
        if (current_node_pdg.getLevel()>=3) return false;
        int cid = current_node.getVertex_kind_id();
        int nid = new_node.getVertex_kind_id();
        
        if (direction) { // forward 
        	/**
        	 * Boya's Note: Only include children functions but not parent functions;
        	 * Since different parent functions indicate different calling contexts
        	 */
        	
            if (nid > 16 || nid < 13) { 
            	/**
            	 * Boya's note: Do not consider edge fo->ao, since this will lead to parent function
            	 */
                if (!(cid == 12 && nid == 2)) {// can't be formal out parameter
                    PdgForPhaseOne new_node_pdg = new PdgForPhaseOne(new_node.getPdg_id(),current_node.getPdg_id(),current_node_pdg.getLevel()+1,true);
                    ids_pdg_set.put(new_node_pdg.getPdg_id_Key(),new_node_pdg);
                    return true;
                }
            }
        }    
        return false;
    } 

    public Hashtable getIds_edges() {
		return ids_edges;
	}

	public void setIds_edges(Hashtable ids_edges) {
		this.ids_edges = ids_edges;
	}

	public Hashtable getRids_vertices_hash() {
		return rids_vertices_hash;
	}

	public void setRids_vertices_hash(Hashtable rids_vertices_hash) {
		this.rids_vertices_hash = rids_vertices_hash;
	}

	public void setIds_vertices_hash(Hashtable ids_vertices_hash) {
		this.ids_vertices_hash = ids_vertices_hash;
	}

	public void setIds_pdg_set(Hashtable ids_pdg_set) {
		this.ids_pdg_set = ids_pdg_set;
	}

	public void setRids_edges(Hashtable rids_edges) {
		this.rids_edges = rids_edges;
	}

	private void expandIds(Connection conn, int[] ids_temp_vertices_list) throws SQLException { 

        int index = 0;                       
        int callsite_limit = IDS.MAX_CALLSITE_NUM * this.iteration_num;
        int node_limit = IDS.MAX_VERTICES_NUM_1 * this.iteration_num;
        
        while ( index < ids_vertices_num && ids_vertices_num < node_limit && this.callsite_num < callsite_limit )  {
            int current_node_vertex_key = ids_temp_vertices_list[index];
            Vertex current_node = (Vertex) ids_vertices_hash.get(new Integer(current_node_vertex_key));
            /**
             * TODO: delete
             */
            if(current_node.getVertex_key() == 263041)
            	System.out.println("Current vertex: 263041");
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
                boolean flag = true;

                while (rset.next() && ids_vertices_num < (IDS.MAX_VERTICES_NUM_1 * this.iteration_num) &&
                       (this.callsite_num<IDS.MAX_CALLSITE_NUM * this.iteration_num) && flag) {
                    int new_node_vertex_key = rset.getInt("NEW_NODE_VERTEX_KEY");
                    //TODO: delete
                    if(new_node_vertex_key == 84459)
                    	System.out.println("Node 84459 reached");
                    Vertex new_node = (Vertex) ids_vertices_hash.get(new Integer(new_node_vertex_key));
                                                                            
                    if (new_node == null) {                
                        int new_node_pdg_id = rset.getInt("NEW_NODE_PDG_ID");
                        int new_node_vertex_kind_id = rset.getInt("NEW_NODE_VERTEX_KIND_ID");
                        int edge_type = rset.getInt("EDGE_TYPE");
                        int new_node_startline = rset.getInt("NEW_NODE_STARTLINE");
                        int new_node_vertex_label = rset.getInt("NEW_NODE_VERTEX_LABEL");
                        String new_node_fieldname1 = rset.getString("NEW_NODE_FIELDNAME1");
                        String new_node_fieldname2 = rset.getString("NEW_NODE_FIELDNAME2");

                        boolean valid_new_node = false;
                        if (index >= this.subIDS_max_vertices_num) valid_new_node = true;//TODO: ??
                        else {
                            if (new_node_pdg_id == this.topped_pdg_id) valid_new_node = true;
                        }
                        
                        if (valid_new_node) {
                        
                            new_node = new Vertex(ids_vertices_num,new_node_vertex_key,new_node_vertex_label,new_node_vertex_kind_id,new_node_startline,
                                                  current_node.getDistance_to_candidate()+1,new_node_fieldname1,new_node_fieldname2,new_node_pdg_id);
                            //TODO: delete
                            if(new_node.getVertex_key() == 84459)
                            	System.out.println("New node: "+new_node.getVertex_key());
                            switch (new_node_vertex_kind_id) {
                                case 1:
                                case 2: 
                                case 5: boolean add_edge = false;
                                        if (edge_type == 1) { 
                                            this.loadCallsitGraph(conn,new_node,ids_temp_vertices_list); 
                                            this.callsite_num++;                                                        
                                            add_edge = true;                      
                                        } else {  // 
                                            if (new_node_pdg_id != this.key_node_target_pdg_id) {     
                                                if (this.isIntrestingPdg(new_node,current_node,forward[i])) {
                                                    this.loadCallsitGraph(conn,new_node,ids_temp_vertices_list);
                                                    this.callsite_num++;
                                                    add_edge = true;
                                                }
                                            }   
                                        }
                                        if (add_edge) {
                                            new_node = (Vertex) ids_vertices_hash.get(new Integer(new_node_vertex_key));
                                            IdsEdge edge;
                                            if (forward[i]) edge = new IdsEdge(current_node.getNode_index(),new_node.getNode_index(),0);
                                            else            edge = new IdsEdge(new_node.getNode_index(),current_node.getNode_index(),0);
                                            ids_edges.put(edge.getKey(),edge);
                                        }
                                        break;
                              default:  if ( edge_type == 1 || 
                                            (edge_type == 3 && new_node_pdg_id != this.key_node_target_pdg_id && this.isIntrestingPdg(new_node,current_node,forward[i]))) {
                                            
                                            
                                            ids_temp_vertices_list[ids_vertices_num] = new_node.getVertex_key();
                                            ids_vertices_hash.put(new_node.getVertex_key_Integer(),new_node);                                    
                                            ids_vertices_num++;
                                            IdsEdge edge;
                                            if (forward[i]) {
                                                edge = new IdsEdge(current_node.getNode_index(),new_node.getNode_index(),0);
                                            } else {
                                                edge = new IdsEdge(new_node.getNode_index(),current_node.getNode_index(),0);
                                            }
                                            ids_edges.put(edge.getKey(),edge);                                                         
                                        } 
                                        break;
                            }
                        }
                        
                    } else { // The new node has been included in the IDS. Therefore, the only thing we need to do is to add the edge between new_node and current_node                    
                        IdsEdge edge;
                        if (forward[i]) edge = new IdsEdge(current_node.getNode_index(),new_node.getNode_index(),0);
                        else            edge = new IdsEdge(new_node.getNode_index(),current_node.getNode_index(),0);  
                        Object obj = ids_edges.get(edge.getKey());
                        if (obj == null) ids_edges.put(edge.getKey(),edge);
                    }                   

                }
                rset.close();
                pstmt.close();
            }
            index++;    
        }
        
        if (this.ids_vertices_num > this.subIDS_max_vertices_num) { // The IDS has been expanded
            this.addGlobalNodes(conn,ids_temp_vertices_list);
            this.ids_vertices_list = new int[this.ids_vertices_num];
            for (int i=0; i<this.ids_vertices_num; i++) {
                this.ids_vertices_list[i] = ids_temp_vertices_list[i];
            }
        }
        
    }

    private boolean isIntrestedNodeByInterDependence(Vertex current_node, Vertex new_node) {
        Object obj = ids_pdg_set.get(new_node.getPdg_id_Integer());
        if (obj == null) return false;
        int cn_id = current_node.getVertex_kind_id();
        int nn_id = new_node.getVertex_kind_id();
        switch (cn_id) {
            case  1: switch (nn_id) {  // Ignore the edge AI --> AO, AUX, GAI,GAO
                         case 11: return true; // AI --> FI
                         default: return false;
                     }
            case 13: switch (nn_id) {  //GAI 
                         case 15: return true;
                         default: return false;
                     }
            case 14: switch (nn_id) {//GAO
                         case 11:                //GAO --> FI
                         case 15: return false;  //GAO --> GFI
                         default: return true;
                     }
        }
        return true;
    }

    
    private void addGlobalNodes(Connection conn, int[] ids_temp_vertices_list) throws SQLException {
        int index = 0;
        while (index < ids_vertices_num && ids_vertices_num < (IDS.MAX_VERTICES_NUM_2 * this.iteration_num)) {
            int current_node_vertex_key = ids_temp_vertices_list[index++];
            Vertex current_node = (Vertex) ids_vertices_hash.get(new Integer(current_node_vertex_key));
            PreparedStatement  pstmt = conn.prepareStatement(find_global_node);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            
            while (rset.next() && ids_vertices_num < (IDS.MAX_VERTICES_NUM_2 * this.iteration_num)) {
                int new_node_vertex_key = rset.getInt("NEW_NODE_VERTEX_KEY");
                Vertex new_node = (Vertex) ids_vertices_hash.get(new Integer(new_node_vertex_key));
                if (new_node != null) {
                    IdsEdge edge = new IdsEdge(current_node.getNode_index(),new_node.getNode_index(),0); // Data
                    Object obj =  ids_edges.get(edge.getKey());
                    if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                } else {                
                    int new_node_vertex_label = rset.getInt("NEW_NODE_VERTEX_LABEL");
                    int new_node_vertex_kind_id = rset.getInt("NEW_NODE_VERTEX_KIND_ID");
                    int new_node_pdg_id = rset.getInt("NEW_NODE_PDG_ID");
                    int new_node_startline = rset.getInt("NEW_NODE_STARTLINE");
                    String new_node_fieldname1 = rset.getString("NEW_NODE_FIELDNAME1");
                    String new_node_fieldname2 = rset.getString("NEW_NODE_FIELDNAME2");               
                    new_node = new Vertex(ids_vertices_num,new_node_vertex_key,new_node_vertex_label,new_node_vertex_kind_id,
                                          new_node_startline,current_node.getDistance_to_candidate()+1,new_node_fieldname1,new_node_fieldname2,new_node_pdg_id);
                    if (this.isIntrestedNodeByInterDependence(current_node,new_node)) {  // 
                        ids_temp_vertices_list[this.ids_vertices_num] = new_node.getVertex_key();
                        this.ids_vertices_hash.put(new_node.getVertex_key_Integer(),new_node);
                        IdsEdge edge = new IdsEdge(current_node.getNode_index(),new_node.getNode_index(),0);
                        this.ids_edges.put(edge.getKey(),edge);
                        this.ids_vertices_num++;
                    }
                }
            }
            rset.close();
            pstmt.close();  
        }        
    }    
    


    private Vertex getVertexFromDB(int vertex_key, Connection conn) throws SQLException {
        String sql = "SELECT * FROM vertex WHERE vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        Vertex v = null;
        if (rset.next()) {
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int startline = rset.getInt("STARTLINE");
            String fieldname1 = rset.getString("FIELDNAME1");
            String fieldname2 = rset.getString("FIELDNAME2");
            int pdg_id = rset.getInt("PDG_ID");
            v = new Vertex(ids_vertices_num,vertex_key,vertex_label,vertex_kind_id,startline,0,fieldname1,fieldname2,pdg_id);
        }
        rset.close();
        pstmt.close();
        return v;
    }

    private int findCallsiteNodeVertexKey(int vertex_key, Connection conn) throws SQLException {
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


    private void loadCallsitGraph(Connection conn, Vertex new_node, int[] ids_temp_vertices_list) throws SQLException {
        
        int callsite_node_vertex_key = 0;        
        switch (new_node.getVertex_kind_id()) {
            case 1:
            case 2: callsite_node_vertex_key = this.findCallsiteNodeVertexKey(new_node.getVertex_key(),conn); break;
            case 5: callsite_node_vertex_key = new_node.getVertex_key(); break;
        }

        ////////////////// Add call-site no ids_temp_vertices_num
        Vertex callsite_node = this.getVertexFromDB(callsite_node_vertex_key,conn);
        callsite_node.setDistance_to_candidate(new_node.getDistance_to_candidate());
             
            
        ids_temp_vertices_list[ids_vertices_num] = callsite_node.getVertex_key();
        ids_vertices_hash.put(callsite_node.getVertex_key_Integer(),callsite_node);
        ids_vertices_num++;
            
        ////////////////// Add parameter nodes
        String sql = "select v.* " + 
                     "from   edges e, vertex v " + 
                     "where  e.src_vertex_key = ? " + 
                     "and    e.tar_vertex_key = v.vertex_key " + 
                     "and    e.edge_type = 2  " + 
                     "and    v.vertex_kind_id in (1,2)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,callsite_node_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int startline = rset.getInt("STARTLINE");
            String fieldname1 = rset.getString("FIELDNAME1");
            String fieldname2 = rset.getString("FIELDNAME2");
            int pdg_id = rset.getInt("PDG_ID");
            Vertex param_node = new Vertex(ids_vertices_num,vertex_key,vertex_label,vertex_kind_id,startline,
                                           callsite_node.getDistance_to_candidate(),fieldname1,fieldname2,pdg_id);
                                                                        
                                           
            ids_temp_vertices_list[ids_vertices_num]  = vertex_key;
            ids_vertices_hash.put(param_node.getVertex_key_Integer(),param_node);
            ids_vertices_num++;
                
            IdsEdge edge = new IdsEdge(callsite_node.getNode_index(),param_node.getNode_index(),1); //control dependence
            ids_edges.put(edge.getKey(),edge);
        }
        rset.close();
        pstmt.close();
    }  


    private void copySubIds(IDS subIDS, int[] ids_temp_vertices_list) {

        // Duplicate ids_temp_vertices_list, ids_vertices_hash 
        int[] sub_ids_vertices_list = subIDS.getIds_vertices_list();
        Hashtable sub_ids_vertices_hash = subIDS.getIds_vertices_hash();
        for (int i=0; i<subIDS.getIds_vertices_number(); i++) {
            ids_temp_vertices_list[i] = sub_ids_vertices_list[i];
            Vertex v = (Vertex) sub_ids_vertices_hash.get(new Integer(ids_temp_vertices_list[i]));
            this.ids_vertices_hash.put(v.getVertex_key_Integer(),v);
            this.ids_vertices_num++;
        }        
            
        // duplicate ids_edges
        Hashtable sub_ids_edges = subIDS.getIdsEdges();
        Enumeration ee = sub_ids_edges.elements();
        while (ee.hasMoreElements()) {
            IdsEdge edge = (IdsEdge) ee.nextElement();
            this.ids_edges.put(edge.getKey(),edge);
        }
        
        // duplciate ids_pdg_set
        Hashtable sub_ids_pdg_set = subIDS.getIds_pdg_set();
        Enumeration enum_pdg_set = sub_ids_pdg_set.elements();
        while (enum_pdg_set.hasMoreElements()) {
            PdgForPhaseOne pdg = (PdgForPhaseOne) enum_pdg_set.nextElement();
            ids_pdg_set.put(pdg.getPdg_id_Key(),pdg);
        }

    }

        
    public Hashtable getIds_pdg_set() {
        return this.ids_pdg_set;
    }
        

    public int[] getIds_vertices_list() {
        return this.ids_vertices_list;
    }

    private int getCallsiteTargetPdg(int vertex_key, Connection conn) throws SQLException {
        String sql = "SELECT * FROM edges WHERE src_vertex_key = ? and edge_type = 4";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        int target_pdg_id = 0;
        if (rset.next()) target_pdg_id = rset.getInt("TAR_PDG_ID");
        rset.close();
        pstmt.close();
        return target_pdg_id;
    }

    public Hashtable getIdsEdges() {
        return this.ids_edges;
    }

    public Pattern getPattern() {
        return this.pattern;
    }
    
    public int getIds_vertices_number() {
        return this.ids_vertices_num;
    }
    
    
    public int getIteration_number() {
        return this.iteration_num;
    }

    public int getCallsiteNumber() {
        return this.callsite_num;
    }
    
    public int getKey_node_target_pdg_id() {
        return this.key_node_target_pdg_id;
    }
    
    public Hashtable getIds_vertices_hash() {
        return this.ids_vertices_hash;
    }
    
    public Hashtable getRIds_vertices_hash() {
        return this.rids_vertices_hash;
    }
    
    public int getRids_vertices_num() {
        return this.rids_vertices_num;
    }
    
    public int[] getRids_vertices_list() {
        return this.rids_vertices_list;
    }
    
    public Hashtable getRids_edges() {
        return this.rids_edges;
    }
    
/*     

    
    

    private boolean isIntrestedSDDFromAItoAI(Vertex src_node, Vertex tar_node) {
        String src_fieldname = src_node.getFieldname1();
        String tar_fieldname = tar_node.getFieldname1();
        if (tar_fieldname.equals("") && src_fieldname.equals("")) return true;
        if (!tar_fieldname.equals("") && !src_fieldname.equals("") && tar_fieldname.equals(src_fieldname)) return true;
        if (tar_fieldname.equals("")  && !src_fieldname.equals("")) {
            return true;                           
        }
        if (!tar_fieldname.equals("") &&  src_fieldname.equals("")) { 
            return true;        
        }            
        return false;        
        
    }

    private void buildSDDBetweenActualIn(AdjacentEdge[] adj_edges, Vector ai_neighbors, Pattern pattern, Connection conn) throws SQLException{
    
        Hashtable ai_to_ai_label = pattern.getAi_to_ai_labels();
        Enumeration src_ai_ee = ai_neighbors.elements();
        while (src_ai_ee.hasMoreElements()) {

            Vertex src_ai_node = (Vertex) src_ai_ee.nextElement();
            Enumeration tar_ai_ee = ai_neighbors.elements();
            
            while (tar_ai_ee.hasMoreElements()) {
                Vertex tar_ai_node = (Vertex) tar_ai_ee.nextElement();
                if (src_ai_node.getVertex_key() != tar_ai_node.getVertex_key()) {
                
                    String key = Integer.toString(src_ai_node.getVertex_label()) + "-" + Integer.toString(tar_ai_node.getVertex_label());                    
                    Object obj1 = ai_to_ai_label.get(key);

                    if (obj1!= null && src_ai_node.getStartline() < tar_ai_node.getStartline() && 
                        this.isIntrestedSDDFromAItoAI(src_ai_node,tar_ai_node) && this.checkCfgPath(conn,src_ai_node,tar_ai_node)) {
                        
                        IdsEdge edge = new IdsEdge(src_ai_node.getNode_index(),tar_ai_node.getNode_index(),2);
                        Object obj = this.ids_edges.get(edge.getKey());
                        if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                        Enumeration eqv_ai_node_enum = this.findAllEqvAINodes(conn,adj_edges,src_ai_node).elements();
                        while (eqv_ai_node_enum.hasMoreElements()) {
                            Vertex eqv_ai_node = (Vertex) eqv_ai_node_enum.nextElement();
                            edge = new IdsEdge(src_ai_node.getNode_index(),tar_ai_node.getNode_index(),2);
                            obj = this.ids_edges.get(edge.getKey());
                            if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                        }
                    }
                }
            }
        }    
    }    

    private void buildSDDFromControlToActualIn(AdjacentEdge[] adj_edges, Vector cp_neighbors, Vector ai_neighbors, Pattern pattern, Connection conn) throws SQLException {
        
        Hashtable cp_to_ai_labels = pattern.getCp_to_ai_labels();
        Enumeration cp_enum = cp_neighbors.elements();
        while (cp_enum.hasMoreElements()) {    
            Vertex cp_node = (Vertex) cp_enum.nextElement();
         
            Enumeration ai_enum = ai_neighbors.elements();
            while (ai_enum.hasMoreElements()) {
                Vertex ai_node = (Vertex) ai_enum.nextElement();
                Object obj = cp_to_ai_labels.get(Integer.toString(ai_node.getVertex_label()));
                if (obj != null && this.intrestedSDDFromControlToAI(cp_node,ai_node) && cp_node.getStartline()<=ai_node.getStartline() &&
                    this.checkCfgPath(conn,cp_node,ai_node)) {

                    IdsEdge edge = new IdsEdge(cp_node.getNode_index(),ai_node.getNode_index(),2);
                    obj = this.ids_edges.get(edge.getKey());
                    if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                    
                    Vector eqv_ai_nodes = this.findAllEqvAINodes(conn,adj_edges,ai_node);
                    Enumeration eqv_enum = eqv_ai_nodes.elements();
                    while (eqv_enum.hasMoreElements()) {
                        Vertex eqv_ai_node = (Vertex) eqv_enum.nextElement();
                        edge = new IdsEdge(cp_node.getNode_index(),eqv_ai_node.getNode_index(),2);
                        obj = this.ids_edges.get(edge.getKey());
                        if (obj == null) this.ids_edges.put(edge.getKey(),edge);
                    } 
                } 
            } 
        } 
    }   
    
    
    
   
    
    
    
    private boolean checkCfgPath(Connection conn, Vertex src_node, Vertex tar_node) throws SQLException {
    
        String sql = "select v.* " + 
                     "from   cfg_edges c, vertex v " + 
                     "where  c.src_vertex_key = ? " + 
                     "and    c.src_pdg_id = c.tar_pdg_id " + 
                     "and    c.tar_vertex_key = v.vertex_key " + 
                     "and    (not v.vertex_kind_id in (1,2,23,24))"; 
        Vertex source_node;
        Vertex target_node;
        if (src_node.getVertex_kind_id()==1) source_node = this.getCallsiteNode(conn,src_node);
        else                                 source_node = src_node;
        if (tar_node.getVertex_kind_id()==1) target_node = this.getCallsiteNode(conn,tar_node);
        else                                 target_node = tar_node;
        if (source_node == null || target_node == null) return false;
          
        int pdg_id = source_node.getPdg_id();
        int target_node_startline = target_node.getStartline();
    
        int[][] cfg_path = new int[20000][2];
        cfg_path[0][0] = source_node.getVertex_key();
        cfg_path[0][1] = source_node.getStartline();
        int path_vertices_num = 1;
        int index = 0;
        Hashtable path_nodes = new Hashtable();
        
        boolean foundPath = false;
        
        path_nodes.put(source_node.getVertex_key_Integer(),source_node.getVertex_key_Integer());
        while (index < path_vertices_num && !foundPath) {
            int current_node_vertex_key = cfg_path[index][0];
            int current_node_startline = cfg_path[index][1];
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next() && !foundPath) {
                int next_node_vertex_key = rset.getInt("VERTEX_KEY");
                int next_node_startline = rset.getInt("STARTLINE");
                if (next_node_vertex_key == target_node.getVertex_key()) foundPath = true;            
                if (next_node_startline>=current_node_startline && next_node_startline <= target_node_startline) {
                    Object obj = path_nodes.get(new Integer(next_node_vertex_key));
                    if (obj == null) {
                        cfg_path[path_vertices_num][0] = next_node_vertex_key;
                        cfg_path[path_vertices_num][1] = next_node_startline;
                        path_vertices_num++;
                        path_nodes.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                    }
                }
            } 

            index++;
        }  
        return foundPath;
    }    
    

    

    
    
    
    private boolean intrestedSDDFromControlToAI(Vertex src_node, Vertex tar_node) {
        String src_fieldname = src_node.getFieldname1();
        String tar_fieldname = tar_node.getFieldname1();
        
        if (src_fieldname.equals("") && tar_fieldname.equals("")) return true;
        if (!tar_fieldname.equals("") && !src_fieldname.equals("") && tar_fieldname.equals(src_fieldname)) return true;
        if (tar_fieldname.equals("")  && !src_fieldname.equals("")) {
            return true;   
        }
        if (!tar_fieldname.equals("") &&  src_fieldname.equals("")) { 
           return true;       
        }        
        return false;
    }    
    
    private void findNodesForSDDEdge(AdjacentEdge[] adj_edges, Vertex sdd_node, Vector ai_neighbors, Vector cp_neighbors) {
        int sdd_node_index = sdd_node.getNode_index();
        int[] target_nodes = adj_edges[sdd_node_index].getData_Target_nodes();
        for (int i=0; i<adj_edges[sdd_node_index].getData_Target_node_num(); i++) {
            int vertex_key = ids_vertices_list[target_nodes[i]];
            Vertex v = (Vertex) ids_vertices_hash.get(new Integer(vertex_key));
            if (sdd_node.getPdg_id() == v.getPdg_id()) {
                if (v.getVertex_kind_id() == 1) ai_neighbors.add(v);
                if (v.getVertex_kind_id() == 6) cp_neighbors.add(v);
            }
        }
    }
    
    */   
    
    
    
    
}
