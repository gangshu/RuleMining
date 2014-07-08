package andy;
import java.util.*;
import java.sql.*;
public class CandidatePatternInstance {

    RIDS rids; // The CPI is derived from the RIDS
    int[] cpi_vertices_list;
    Hashtable cpi_vertices_hash = new Hashtable();
    int cpi_vertices_num = 0;
    Connection conn;
    int[][][] cpi_edges;
    Hashtable itemset;
    Hashtable ee_itemset;
    CandidatePattern cp;
    Hashtable identical_keys;
    int cpi_edge_num = 0;
    
    public void clearCandidatePatternInstance() {
        rids = null;
        cpi_vertices_list = null;
        cpi_vertices_hash = null;
        conn = null;
        cpi_edges = null;
        itemset = null;
        ee_itemset = null;
        cp = null;
        identical_keys = null;
    }
    
    // Used by ViolationInstance
    public CandidatePatternInstance() {}
    
    public CandidatePatternInstance(CandidatePattern cp, Connection conn, int instance_id) throws SQLException {
        this.cp = cp;
        this.cpi_vertices_num = cp.getCp_vertices_num();
        this.cpi_vertices_list = new int[this.cpi_vertices_num];
        this.cpi_edges = new int[cpi_vertices_num][cpi_vertices_num][3];    
        this.loadVerticesInfoFromDB(conn,instance_id);
        this.loadEdgesInfoFromDB(conn,instance_id);
    }  
    
    private void loadEdgesInfoFromDB(Connection conn, int instance_id) throws SQLException {
        String sql = "select * from pattern_instance where pattern_key = ? and graph_id = ?";
        for (int i=0; i<this.cpi_vertices_num; i++) 
            for (int j=0; j<this.cpi_vertices_num; j++) 
                for (int k=0; k<3; k++) 
                    this.cpi_edges[i][j][k]=0;
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,cp.getPattern_key());
        pstmt.setInt(2,instance_id);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int src_vertex_key = rset.getInt("SRC_VERTEX_KEY");
            int tar_vertex_key = rset.getInt("TAR_VERTEX_KEY");
            Vertex src_vertex = (Vertex) this.cpi_vertices_hash.get(new Integer(src_vertex_key));
            Vertex tar_vertex = (Vertex) this.cpi_vertices_hash.get(new Integer(tar_vertex_key));
            int src_index = src_vertex.getNode_index();
            int tar_index = tar_vertex.getNode_index();
            int edgetype = rset.getInt("EDGE_TYPE");
            if (edgetype>=2) {
                this.cpi_edges[src_index][tar_index][2]= edgetype;   
            } else {
                this.cpi_edges[src_index][tar_index][edgetype]= 1;
            }
            this.cpi_edge_num++;
        }
        rset.close();
        pstmt.close();  
    }       
    
    
    private void loadVerticesInfoFromDB(Connection conn,int instance_id) throws SQLException {
        String sql = "select  n.node_index, v.* " +
                     "from   pattern_node_info n, vertex v " +
                     "where  n.pattern_key = ? " +
                     "and    n.pattern_instance = ? " +
                     "and    n.vertex_key = v.vertex_key order by node_index";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.cp.getPattern_key());
        pstmt.setInt(2,instance_id);
        ResultSet rset = pstmt.executeQuery();
        int i=0;
        while (rset.next()) {
            int node_index = rset.getInt("NODE_INDEX");
            int vertex_key = rset.getInt("VERTEX_KEY");
            Vertex v = new Vertex(node_index,vertex_key,
                                  rset.getInt("VERTEX_LABEL"),rset.getInt("VERTEX_KIND_ID"),
                                  rset.getInt("STARTLINE"),0,"","",rset.getInt("PDG_ID"));
                                
            v.setVertex_char(rset.getString("VERTEX_CHARACTERS")); 
            this.cpi_vertices_list[node_index] = vertex_key;
            this.cpi_vertices_hash.put(new Integer(vertex_key),v);
        }
        rset.close();
        pstmt.close();    
    }    
    
    public int getEdge_num() {
        return this.cpi_edge_num;
    }    
    
    public Vertex getVertexByNodeIndex(int idx) {
        int vertex_key = this.cpi_vertices_list[idx];
        Vertex v = (Vertex) this.cpi_vertices_hash.get(new Integer(vertex_key));
        return v;
    }
    
    public void removeEdgesBetweenTwoNodes(int i, int j, int k) {
        this.cpi_edges[i][j][k]=-1;
    }
    
    public boolean checkNodeLink(int i) {
        boolean hasLink = false;
        for (int j=0; j<this.cpi_vertices_num; j++) {
            for (int k=0;k<3; k++) {
                if (this.cpi_edges[i][j][k] > 0) return true;
                if (this.cpi_edges[j][i][k] > 0) return true;
            }
        }
        return hasLink;
    }
    
    public void removeToEdges(int i) {
        for (int j=0; j<this.cpi_vertices_num; j++) {
            for (int k=0; k<3; k++) {
                this.cpi_edges[i][j][k]=-1;
            }
        }
    }    

    public boolean[] getRemainingNode_and_setNewIdx() {
        int[] list = new int[this.cpi_vertices_num];
        boolean[] valid_node = new boolean[this.cpi_vertices_num];
        for (int i=0; i<this.cpi_vertices_num; i++) valid_node[i]=false;
        valid_node[0]=true;
        int src_idx = 0;
        list[0]=0;
        int last_node_idx = 1;
        while (src_idx < last_node_idx) {
            int i = list[src_idx];    
            for (int j=0; j<this.cpi_vertices_num; j++) {
                for (int k=0; k<3; k++) {
                    if (this.cpi_edges[i][j][k]>0 || this.cpi_edges[j][i][k]>0){
                        if (!valid_node[j]) {
                            valid_node[j]=true;
                            list[last_node_idx] = j;
                            last_node_idx++;
                        }             
                    }
                }
            }
            src_idx++;
        }
        this.setIndexAfterRemove(valid_node);
        return valid_node;
    }
    
    private void setIndexAfterRemove(boolean[] remaining_node) {
        int new_idx = 0;
        for (int i=0; i<this.cpi_vertices_num; i++) {
            int vertex_key = cpi_vertices_list[i];
            Vertex v = (Vertex) this.cpi_vertices_hash.get(new Integer(vertex_key));
            if (remaining_node[i]) {
                v.setNode_index(new_idx);
                new_idx++;
            } else {
                v.setNotExtendable(); 
                v.setNode_index(-1);
            }
        }
    }    
    
    public void removeFromEdges(int j) {
        for (int i=0; i<this.cpi_vertices_num; i++) {
            for (int k=0; k<3; k++) {
                this.cpi_edges[i][j][k]=-1;
            }
         }
    }        
    
    // For initialization use
    public CandidatePatternInstance(CandidatePattern cp, RIDS rids, Connection conn) throws SQLException {
        this.rids = rids;
        this.conn = conn;
        this.cp = cp;
        int rids_vertices_num = this.rids.getVertices_Number();
        cpi_vertices_list = new int[rids_vertices_num];
        cpi_edges = new int[rids_vertices_num][rids_vertices_num][3];
        itemset = new Hashtable();
        identical_keys = new Hashtable();
    }
    
    
    // Clone the CPI of "scpi"
    public CandidatePatternInstance(CandidatePattern cp, CandidatePatternInstance scpi, boolean boundary_extension, Connection conn) throws SQLException {
        this.cp = cp;
        this.conn = conn;
        this.rids = scpi.getRIDS();
        this.cpi_vertices_num= scpi.getCpiVertices_num();
        int rids_vertices_num = this.rids.getVertices_Number();
        cpi_vertices_list = new int[rids_vertices_num];
        cpi_edges = new int[rids_vertices_num][rids_vertices_num][3];
        this.cloneDataFromSuperCPI(scpi,boundary_extension);
    }
    
    public int[] getCpiVerticesList() {
        return this.cpi_vertices_list;
    }
    
    public Hashtable getCpiVerticesHash() {
        return this.cpi_vertices_hash;
    }
    
    public Hashtable getIdenticalKeys() {
        return this.identical_keys;
    }
    
    // Copy Cpi Nodes and Edges from super pattern instance. 
    private void cloneDataFromSuperCPI(CandidatePatternInstance scpi, boolean boundary_extension) {
    
        // Clone CPI vertices   
        int rids_vertices_num = rids.getVertices_Number();
        this.cpi_vertices_num = scpi.getCpiVertices_num();     
        int[] scpi_vertices_list = scpi.getCpiVerticesList();
        Hashtable scpi_vertices_hash = scpi.getCpiVerticesHash();
        for (int i=0; i<rids_vertices_num; i++) {
            this.cpi_vertices_list[i] = scpi_vertices_list[i];
            Vertex v = (Vertex) scpi_vertices_hash.get(new Integer(this.cpi_vertices_list[i]));
            if (v!= null) cpi_vertices_hash.put(new Integer(this.cpi_vertices_list[i]),v);
        }

        // Clone CPI edges
        int[][][] scpi_edges = scpi.getCpiEdges();
        for (int i=0; i<rids_vertices_num; i++) {
             for (int j=0; j<rids_vertices_num; j++) {
                 for (int k=0; k<3; k++) {
                     this.cpi_edges[i][j][k] = scpi_edges[i][j][k];
                 }
             }
        }
      
        // Clone frequent boundary edges
        if (boundary_extension) {
            Hashtable super_itemset = scpi.getItemset();
            Enumeration key_list = super_itemset.keys();
            this.itemset = new Hashtable();
            while (key_list.hasMoreElements()) {
                Integer key = (Integer) key_list.nextElement();
                Vector item_member = (Vector) super_itemset.get(key);
                /// copy item_member
                Vector copy_item_member = new Vector();
                for (int i=0; i<item_member.size(); i++) {
                    Integer item = (Integer) item_member.get(i);
                    copy_item_member.add(i,item);
                }
                itemset.put(key,copy_item_member);
            }
            
            key_list = scpi.getIdenticalKeys().keys();
            this.identical_keys = new Hashtable();
            while (key_list.hasMoreElements()) {
                Integer key =(Integer) key_list.nextElement();
                identical_keys.put(key,key);
            }
        }     
    }    
    
    public Hashtable getItemset() {
        return this.itemset;
    }
    
    public RIDS getRIDS() {
        return this.rids;
    }
    
    public int[][][] getCpiEdges() {
        return this.cpi_edges;
    }

    public int getCpiVertices_num() {
        return this.cpi_vertices_num;
    }
    
    public void addExtendedEdge(int i, int j, int k) {
        if (k>=2) {
            this.cpi_edges[i][j][2] = k;
        } else {
            this.cpi_edges[i][j][k] = 1;
        }
    }

    private boolean isIntrestedNewNode(Vertex new_node, int type) {
        Object obj = this.cpi_vertices_hash.get(new_node.getVertex_key_Integer());
        if (obj != null) return false; // The new node has been included in the CPI 
        obj = this.identical_keys.get(new_node.getVertex_key_Integer()); // The new node is duplicated in CPI(s)
        if (obj != null) return false;
        int new_node_id = new_node.getVertex_kind_id();
        switch (type) {
            case 0: if (new_node_id == 1 || new_node_id == 2) return true; // consider actual-in/out node
                    else return false;
            case 1: if (new_node_id == 6 || new_node_id == 21) return true;  // Consider only control point & switch node
                    else return false;
            // When control dependence dependences are used for expanding CP, the new nodes can't be control point nodes
            case 2: if (new_node_id == 6 || new_node_id == 21) return false;  
                    else                                       return true;
        }
        return true;
    }

    public Hashtable findEdgesIncidentFromBN(EdgecodeMapping mapping, int type) {
        this.itemset = new Hashtable();
        Hashtable edgecode_itemset = new Hashtable();
        int[][][] rids_edges = rids.getRids_edge();
        int rids_vertices_num = rids.getVertices_Number();
        int[] rids_vertices_list = rids.getRids_vertices_list();
        Hashtable rids_vertices_hash = rids.getRids_vertices_hash();
        int item;
        for (int cpi_idx=0; cpi_idx<this.cpi_vertices_num; cpi_idx++) {
            Vertex bn = (Vertex) rids_vertices_hash.get(new Integer(this.cpi_vertices_list[cpi_idx])); // boundary node
            
            if (bn.getVertex_key() == 4004068 && type == 1) {
                System.out.println("WAIT");
            }
            
            int bn_node_idx = bn.getNode_index();
            for (int new_node_idx=0; new_node_idx<rids_vertices_num; new_node_idx++) {
                if (bn_node_idx != new_node_idx) {
                    Vertex new_node = (Vertex) rids_vertices_hash.get(new Integer(rids_vertices_list[new_node_idx]));                
                    
                    if (new_node.getVertex_key() == 4004071) {
                        System.out.println("WAIT");
                    }
                    
                    if (this.isIntrestedNewNode(new_node,type)) {
                        int new_node_vertex_label;
                        if (new_node.getVertex_kind_id() == 6 || new_node.getVertex_kind_id() == 21) {
                            new_node_vertex_label = 0;
                        } else {
                            new_node_vertex_label = new_node.getVertex_label();
                        }
                        switch (type) {
                            case 0:
                            case 1: if (rids_edges[bn_node_idx][new_node_idx][3]>0) { //DATA
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,0,0,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);
                                        
                                        if (item == 33) {
                                            System.out.println("WAIT");
                                        }
                                        
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }
                                    
                                  /*  2008-01-05  
                                    if (rids_edges[bn_node_idx][new_node_idx][4] == 1) {
                                        if (bn.getVertex_kind_id()==5 && new_node.getVertex_kind_id()==2) {
                                            EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,1,0,bn.getVertex_kind_id(),new_node.getVertex_kind_id());
                                            item = mapping.addEdgecodeMapping(edgecode);
                                            this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                        }
                                    } */
                                    
                                    if (rids_edges[bn_node_idx][new_node_idx][5]>0) { //SDD
                                        int sdd_edge_label = rids_edges[bn_node_idx][new_node_idx][5];
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,sdd_edge_label,0,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);      
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }
                                    if (rids_edges[new_node_idx][bn_node_idx][3]>0) {
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,0,1,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }
                                    if (rids_edges[new_node_idx][bn_node_idx][5]>0) {
                                        int sdd_edge_label = rids_edges[new_node_idx][bn_node_idx][5];
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,sdd_edge_label,1,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);               
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }                    
                                    break;
                            case 2: if (rids_edges[bn_node_idx][new_node_idx][4]>0) {
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,1,0,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }  
                                    if (rids_edges[new_node_idx][bn_node_idx][4]>0) {
                                        EdgeCode edgecode = new EdgeCode(cpi_idx,new_node_vertex_label,1,1,bn.getVertex_kind_id(),new_node.getVertex_kind_id());                                       
                                        item = mapping.addEdgecodeMapping(edgecode);
                                        this.addEdgecode_Itemset(edgecode_itemset,edgecode,item,new_node_idx);
                                    }
                                    break;
                        }
                    }            
                }
            }
        }
       return edgecode_itemset;
    }
    
    public void setIdenticalKey(Integer value) {
        Object obj = this.identical_keys.get(value);
        if (obj == null) this.identical_keys.put(value,value);
    }

    private void addEdgecode_Itemset(Hashtable edgecode_itemset, EdgeCode edgecode, int item, int new_node_idx) {
    
        Object obj = edgecode_itemset.get(edgecode.getCode());
        if (obj == null) {
            edgecode_itemset.put(edgecode.getCode(),edgecode);
        }
        
        Vector item_member = (Vector) itemset.get(new Integer(item));
        if (item_member == null) {
            item_member = new Vector();
            item_member.add(0,new Integer(this.rids.getRIDS_vertex_key(new_node_idx)));
            itemset.put(new Integer(item),item_member);   
        } else { 
            item_member.add(item_member.size(),new Integer(this.rids.getRIDS_vertex_key(new_node_idx)));
        }
    }
    
    public boolean checkEdgeBetweenTwoNodesInCPI(int src_idx_in_cpi, int tar_idx_in_cpi, int type) {
        int src_vertex_key = this.cpi_vertices_list[src_idx_in_cpi];
        int tar_vertex_key = this.cpi_vertices_list[tar_idx_in_cpi];
        Vertex src_node_in_rids = (Vertex) rids.getRids_vertices_hash().get(new Integer(src_vertex_key));
        Vertex tar_node_in_rids = (Vertex) rids.getRids_vertices_hash().get(new Integer(tar_vertex_key));
        int[][][] rids_edges = rids.getRids_edge();
        int len = rids_edges[src_node_in_rids.getNode_index()][tar_node_in_rids.getNode_index()][type];
        if (len != 0) return true;
        else          return false;
    }  
    
    public int getSddLabelBetweenTwoNodes(int src_idx_in_cpi, int tar_idx_in_cpi, int type) {
        int src_vertex_key = this.cpi_vertices_list[src_idx_in_cpi];
        int tar_vertex_key = this.cpi_vertices_list[tar_idx_in_cpi];
        Vertex src_node_in_rids = (Vertex) rids.getRids_vertices_hash().get(new Integer(src_vertex_key));
        Vertex tar_node_in_rids = (Vertex) rids.getRids_vertices_hash().get(new Integer(tar_vertex_key));
        int[][][] rids_edges = rids.getRids_edge();
        return rids_edges[src_node_in_rids.getNode_index()][tar_node_in_rids.getNode_index()][type];      
    }
    

    
    public void addNewEdgeToCPI(int i, int j, int k, int label) {
        this.cpi_edges[i][j][k] = label;
    }    
    
    public void addNewNode(int bn_node_kind_id, Vertex new_node) throws SQLException {
        int callsite_vertex_key = 0;
        switch (new_node.getVertex_kind_id()) {
            case 1: 
            case 2: callsite_vertex_key = findCallsiteVertexKey(new_node.getVertex_key());
                    break;
            case 5: callsite_vertex_key = new_node.getVertex_key();
                    break;
        }
        
        switch (new_node.getVertex_kind_id()) {
            /* 2008-01-05
            case 1: this.addCallsiteGraph(callsite_vertex_key,false);
                    break;
            case 2: if (bn_node_kind_id==5) {
                        cpi_vertices_list[cpi_vertices_num] = new_node.getVertex_key();
                        cpi_vertices_hash.put(new Integer(new_node.getVertex_key()),new_node);
                        cpi_vertices_num++;
                    } else {
                        Object obj = cpi_vertices_hash.get(new Integer(callsite_vertex_key));
                        if (obj == null) {
                            this.addCallsiteGraph(callsite_vertex_key,true);
                        } else {
                            cpi_vertices_list[cpi_vertices_num] = new_node.getVertex_key();
                            cpi_vertices_hash.put(new Integer(new_node.getVertex_key()),new_node);
                            cpi_vertices_num++;    
                        }
                    }
                    break;
            case 5: this.addCallsiteGraph(callsite_vertex_key,false);
                    break; */
            
            case 1:
            case 2:
            case 5: this.addCallsiteGraph(callsite_vertex_key,true);
                    break;
            default: cpi_vertices_list[cpi_vertices_num] = new_node.getVertex_key();
                     cpi_vertices_hash.put(new Integer(new_node.getVertex_key()),new_node);
                     cpi_vertices_num++;
        }
    }
    
    private int findCallsiteVertexKey(int param_vertex_key) throws SQLException {
        String sql = "SELECT * FROM edges WHERE tar_vertex_key = ? and edge_type = 2";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,param_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        int callsite_vertex_key = 0;
        if (rset.next()) callsite_vertex_key = rset.getInt("SRC_VERTEX_KEY");
        rset.close();
        pstmt.close();       
        return callsite_vertex_key;
    }
    
    
    public void  addCallsiteGraph(int callsite_vertex_key, boolean includeAONode) throws SQLException {
        // Callsite node        
        int callsite_node_index = cpi_vertices_num;  // set the index of the callsite node
        cpi_vertices_list[this.cpi_vertices_num++] = callsite_vertex_key; 
        Vertex callsite_node = (Vertex) rids.getRids_vertices_hash().get(new Integer(callsite_vertex_key));
        cpi_vertices_hash.put(new Integer(callsite_vertex_key),callsite_node);
       
        // Parameter nodes
        String sql;
        if (includeAONode) {
            sql = "SELECT * FROM edges e, vertex v WHERE e.src_vertex_key = ? AND e.tar_vertex_key = v.vertex_key AND " +
                  "e.edge_type = 2 AND v.vertex_kind_id in (1,2) ORDER BY v.vertex_label";
        } else {
            sql = "SELECT * FROM edges e, vertex v WHERE e.src_vertex_key = ? AND e.tar_vertex_key = v.vertex_key AND " +
                  "e.edge_type = 2 AND v.vertex_kind_id in (1,2) ORDER BY v.vertex_label";
        }
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,callsite_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int param_vertex_key = rset.getInt("VERTEX_KEY");
            Vertex param_node = (Vertex) rids.getRids_vertices_hash().get(new Integer(param_vertex_key));
            if (param_node != null) {
                int param_node_index = cpi_vertices_num;                
                cpi_vertices_list[this.cpi_vertices_num++] = param_vertex_key;
                cpi_vertices_hash.put(new Integer(param_vertex_key),param_node);
                this.cpi_edges[callsite_node_index][param_node_index][1] = 1; // Control dependence from callsite to param node
            } else {
                System.out.println("Error message: callsite vertex key" + callsite_vertex_key +","+param_node);
            }
        }
        rset.close();
        pstmt.close();
    }
 
    public boolean includeAllFrequentExtendedEdgeCode(Hashtable frq_edgecode_set) {
        Enumeration enum1 = frq_edgecode_set.elements();
        while (enum1.hasMoreElements()) { 
            Integer item = (Integer) enum1.nextElement();   
            Object obj = this.ee_itemset.get(item);
            if (obj == null) return false;
        }
        return true;   
    }
    
    // Check whether this pattern instance contains all frequent edgecode set. 
    public boolean includeAllFrequentEdgeCode(Hashtable frq_edgecode_set) {
        Enumeration enum1 = frq_edgecode_set.elements();
        while (enum1.hasMoreElements()) { 
            Integer item = (Integer) enum1.nextElement();   
            Object obj = itemset.get(item);
            if (obj == null) return false;
        }
        return true;   
    }
    
    public Vector findCandidateVertexLabel(EdgeCode edgecode) {
        Vector list_candidate  = (Vector) itemset.get(new Integer(edgecode.getItem()));
        Vector candidate_label = new Vector();
        int idx = 0;
        for (int i=0; i<list_candidate.size(); i++) {
            Integer candidate_node_vertex_key = (Integer) list_candidate.get(i);
            Vertex candidate_node = (Vertex) this.cpi_vertices_hash.get(candidate_node_vertex_key);
            if (candidate_node == null) { // The candidate node has not included in the CPI yest
                candidate_node = (Vertex) rids.getRids_vertices_hash().get(candidate_node_vertex_key);
                candidate_label.add(idx++,new Integer(candidate_node.getVertex_label()));               
            }
        }
        return candidate_label;
    }
    
    
    public Vertex findBestNewNodeCandidate(EdgeCode edgecode, int suggested_cp_label) {
        Vertex new_node = null;
        Vertex src_node = (Vertex) this.cpi_vertices_hash.get(new Integer(this.cpi_vertices_list[edgecode.getNode_index()]));  
        
        Vector list_candidate =  (Vector) itemset.get(new Integer(edgecode.getItem()));
        for (int i=0; i<list_candidate.size(); i++) {        
            Integer tar_node_vertex_key = (Integer) list_candidate.get(i);
            Object obj = this.cpi_vertices_hash.get(tar_node_vertex_key);
            Vertex tar_node = (Vertex) rids.getRids_vertices_hash().get(tar_node_vertex_key);
            if (obj == null) { // Target Node has not included in the pattern instance
                obj = this.identical_keys.get(tar_node_vertex_key);
                if (obj == null) {
                    if (new_node == null) {    
                        new_node = tar_node;
                    } else { // There are more than candidate nodes, select that is close to the source node
                        new_node = choose_suitable_node(src_node, new_node, tar_node, edgecode, suggested_cp_label);      
                    }
                }
            }
        }
        return new_node;
    }   
    
    
    // The number of edges between new_node and nodes in the CPI
    private int getEdgeNumberWithCPI(Vertex new_node) {
        Hashtable rids_vertices_hash = rids.getRids_vertices_hash();
        int rids_edges[][][] = rids.getRids_edge();
        int new_node_index = new_node.getNode_index();
        int count = 0;
        for (int i=0; i<this.cpi_vertices_num; i++) {
            // Nodes in the CPI
            int tar_vertex_key_in_cpi = this.cpi_vertices_list[i];
            // Get the node_index in the RIDS
            Vertex tar_node_in_cpi = (Vertex) rids_vertices_hash.get(new Integer(tar_vertex_key_in_cpi));
            int tar_node_index = tar_node_in_cpi.getNode_index();
            for (int k=3; k<6; k++) {
                if (rids_edges[new_node_index][tar_node_index][k]!=0) count++;
                if (rids_edges[tar_node_index][new_node_index][k]!=0) count++;
            }            
        }
        return count;
    }
    
    private Vertex choose_suitable_node(Vertex src_node, Vertex new_node, Vertex tar_node, EdgeCode edgecode, int suggested_label) {
        int src_node_pdg_id = src_node.getPdg_id();
        int tar_node_pdg_id = tar_node.getPdg_id();
        int new_node_pdg_id = new_node.getPdg_id();
        if (src_node_pdg_id == tar_node_pdg_id && src_node_pdg_id != new_node_pdg_id) return tar_node;
        if (src_node_pdg_id != tar_node_pdg_id && src_node_pdg_id == new_node_pdg_id) return new_node;
        int new_node_physical_distance = Math.abs(src_node.getStartline()-new_node.getStartline());
        int tar_node_physical_distance = Math.abs(src_node.getStartline()-tar_node.getStartline());
        
        int src_idx = src_node.getNode_index();
        int tar_idx = tar_node.getNode_index();
        int new_idx = new_node.getNode_index();
        
        int edgetype = edgecode.getEdge_label();
        if (edgetype >=2 ) edgetype = 2;
        
        int tar_transitive_distance = rids.getRids_edge()[src_idx][tar_idx][edgetype];
        int new_transitive_distance = rids.getRids_edge()[new_idx][tar_idx][edgetype];
       
        int tar_node_edge_info = this.getEdgeNumberWithCPI(tar_node);
        int new_node_edge_info = this.getEdgeNumberWithCPI(new_node);
        int new_node_distance = new_node.getDistance_to_candidate();
        int tar_node_distance = tar_node.getDistance_to_candidate();
                
        if (new_node_pdg_id == src_node_pdg_id && src_node_pdg_id == tar_node_pdg_id) {
        
            if (tar_node.getVertex_label() != suggested_label && new_node.getVertex_label() == suggested_label) {                
                if (Math.abs(new_node_physical_distance-tar_node_physical_distance)<5)
                    if (new_node_physical_distance < 10) return new_node;
            }         
        
            if (tar_node.getVertex_kind_id()==6) {  // control point
                switch (edgetype) {
                    case 0: 
                            if (tar_node_physical_distance < 1 && new_node_physical_distance > 3) return tar_node;
                            if (tar_node_physical_distance > 3 && new_node_physical_distance < 1) return new_node;                     
                  
                            if (tar_node_physical_distance <= 2 && new_node_physical_distance > 5) return tar_node;
                            if (tar_node_physical_distance > 5 && new_node_physical_distance <= 2) return new_node;
                            if (tar_node_physical_distance > new_node_physical_distance + 10) return new_node;
                            if (new_node_physical_distance > tar_node_physical_distance + 10) return tar_node;
                            if (tar_transitive_distance > new_transitive_distance) return new_node;
                            if (new_transitive_distance < tar_transitive_distance) return tar_node;   
                            if (tar_node_edge_info>new_node_edge_info) return tar_node;
                            if (tar_node_edge_info<new_node_edge_info) return new_node;
                            if (tar_node_distance>new_node_distance) return new_node;
                            if (tar_node_distance<new_node_distance) return tar_node;
                            if (tar_node_physical_distance>new_node_physical_distance) return new_node;
                            else return tar_node;
                   case 1:  if (tar_node_edge_info>new_node_edge_info) return tar_node;
                            if (tar_node_edge_info<new_node_edge_info) return new_node;
                            if (tar_transitive_distance > new_transitive_distance) return new_node;
                            if (new_transitive_distance < tar_transitive_distance) return tar_node;                      
                            if (tar_node_distance>new_node_distance) return new_node;
                            if (tar_node_distance<new_node_distance) return tar_node;
                            if (tar_node_physical_distance>new_node_physical_distance + 3) return new_node;
                            if (new_node_physical_distance>tar_node_physical_distance + 3) return tar_node;
                            if (tar_node_edge_info > new_node_edge_info) return tar_node;
                            else return new_node;
                   case 2:  
                            if (tar_node_physical_distance < 1 && new_node_physical_distance > 3) return tar_node;
                            if (tar_node_physical_distance > 3 && new_node_physical_distance < 1) return new_node; 
                            if (tar_node_physical_distance < 2 && new_node_physical_distance > 5) return tar_node;
                            if (tar_node_physical_distance > 5 && new_node_physical_distance < 2) return new_node;
                            if (tar_node_physical_distance > new_node_physical_distance + 10) return new_node;
                            if (new_node_physical_distance > tar_node_physical_distance + 10) return tar_node;
                            if (tar_node_physical_distance>new_node_physical_distance) return new_node;
                            if (tar_node_physical_distance<new_node_physical_distance) return tar_node;
                            if (tar_node_edge_info>new_node_edge_info) return tar_node;
                            if (tar_node_edge_info<new_node_edge_info) return new_node;
                            if (tar_transitive_distance > new_transitive_distance) return new_node;
                            if (new_transitive_distance < tar_transitive_distance) return tar_node;                      
                            if (tar_node_distance>new_node_distance+3) return new_node;
                            if (tar_node_distance+3<new_node_distance) return tar_node;                                                
                            if (tar_node_edge_info > new_node_edge_info) return tar_node;
                            else return new_node;
                   default: return tar_node;
                }
            } else {
                if (tar_node_distance>new_node_distance) return new_node;
                if (tar_node_distance<new_node_distance) return tar_node;
                if (tar_node_physical_distance<new_node_physical_distance + 3) return tar_node;
                if (tar_node_physical_distance>new_node_physical_distance + 3) return new_node;   
                if (tar_node_edge_info>new_node_edge_info) return tar_node;
                else return new_node;
            }
        } else {
            if (new_node_pdg_id == tar_node_pdg_id && new_node_pdg_id != src_node_pdg_id) {
                if (new_node.getStartline()<src_node.getStartline()) return new_node;
                if (tar_node_distance>new_node_distance) return new_node;
                if (tar_node_distance<new_node_distance) return tar_node;  
                if (tar_node_edge_info>new_node_edge_info) return tar_node;
                else return new_node;
            } else {
                if (tar_node_distance>new_node_distance) return new_node;
                if (tar_node_distance<new_node_distance) return tar_node;  
                if (tar_node_edge_info>new_node_edge_info) return tar_node;
                else return new_node;
            }
        }   
    }
 
    public Hashtable findExtendedEdges(ExtendedEdgeMapping mapping) {
        int[][][] rids_edges = rids.getRids_edge();
        this.ee_itemset = new Hashtable();
        for (int i=0; i < this.cpi_vertices_num; i++) {
            Vertex vi = (Vertex) this.cpi_vertices_hash.get(new Integer(this.cpi_vertices_list[i]));
            for (int j=i+1; j < this.cpi_vertices_num; j++) {
                Vertex vj = (Vertex) this.cpi_vertices_hash.get(new Integer(this.cpi_vertices_list[j]));
                for (int k=0; k<3; k++) {
                    int src_idx = vi.getNode_index();
                    int tar_idx = vj.getNode_index();        
                    if (this.cpi_edges[i][j][k]==0) {
                        if (rids_edges[src_idx][tar_idx][k]!=0) {
                            ExtendedEdge ee;
                            if (k == 2) { // SDD edges
                                ee = new ExtendedEdge(i,j,rids_edges[src_idx][tar_idx][k]);
                            } else {
                                ee = new ExtendedEdge(i,j,k);
                            }
                            int item = mapping.addEdgecodeMapping(ee);
                            ee_itemset.put(new Integer(item),ee);
                        }
                    }
                    if (this.cpi_edges[j][i][k]==0) {
                        if (rids_edges[tar_idx][src_idx][k]!=0) {  
                            ExtendedEdge ee; 
                            if (k == 2) {
                                ee = new ExtendedEdge(j,i,rids_edges[src_idx][tar_idx][k]);
                            } else {
                                ee = new ExtendedEdge(j,i,k);
                            }
                            int item = mapping.addEdgecodeMapping(ee);
                            ee_itemset.put(new Integer(item),ee);
                        }
                    }
                }
            }
        }
        return ee_itemset;
    }    
    
    
}
