package andy;

import java.sql.*;
import java.util.*;

public class CandidatePattern {
    
    Connection conn;
    CandidatePatternInstance[] cpi_list;
    GraphDataset gds;
    EdgecodeMapping mapping = new EdgecodeMapping(); //mapping between edge info <---> item   
    ExtendedEdgeMapping ee_mapping = new ExtendedEdgeMapping();
    boolean max_cp;
    int num_cpi;
    int cp_vertices_num;
    CandidatePattern scp;
    boolean insertNewNode = false;
    int pattern_key;
    
    public void clearCandidatePattern()  {
        for (int i=0; i<gds.getGDSGraphNumber();i++) {
            CandidatePatternInstance cpi = cpi_list[i];
            if (cpi != null) cpi.clearCandidatePatternInstance();
        }
        conn = null;
        cpi_list = null;
        gds = null;
        mapping = null;
        ee_mapping = null;
        scp = null;        
    }

 
    // For initialization
    public CandidatePattern(GraphDataset gds, Connection conn) throws SQLException {
        this.gds = gds;
        this.conn = conn;
        this.num_cpi = 0;
        cpi_list = new CandidatePatternInstance[gds.getGDSGraphNumber()];
        for (int i=0; i<gds.getGDSGraphNumber();i++) {
            cpi_list[i] = new CandidatePatternInstance(this,gds.getRIDS(i),conn);
            int candidate_node_vertex_key = gds.getRIDS(i).getRIDS_vertex_key(0);
            cpi_list[i].addCallsiteGraph(candidate_node_vertex_key,true);
            num_cpi++;
        }         
    }

    // Useful for displaying pattern
    public CandidatePattern(Connection conn, int pattern_key) throws SQLException {
        this.pattern_key = pattern_key;
        this.readVertexNumFromDB(conn,pattern_key);
        for (int i=0;i<this.num_cpi; i++) {
            this.cpi_list[i] = new CandidatePatternInstance(this,conn,i);
        }      
    }  

    public int getCp_vertices_num() {
        return this.cp_vertices_num;
    }
    
    public int getPattern_key() {
        return this.pattern_key;
    }
    
    private void readVertexNumFromDB(Connection conn, int pattern_key) throws SQLException {
        String sql =  "select nvl(max(pattern_instance),-1)+1,nvl(max(node_index),-1)+1 from pattern_node_info"+
                      " where pattern_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pattern_key);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        this.num_cpi = rset.getInt(1);
        this.cp_vertices_num = rset.getInt(2);
        this.cpi_list = new CandidatePatternInstance[this.num_cpi];
        rset.close();
        pstmt.close();
    }      
    
    public CandidatePattern(CandidatePattern scp, Hashtable frq_ee_set, String indicate, Connection conn) throws SQLException{
        this.conn = conn;
        this.scp = scp;
        this.gds = scp.getGds();       
        
        ExtendedEdgeMapping scpi_ee_mapping = scp.getExtendedEdgeMapping();

        CandidatePatternInstance[] scpi_list = scp.getCpi_list();
        this.cpi_list = new CandidatePatternInstance[gds.getGDSGraphNumber()];
        // Determine the pattern instances of the candidate pattern
        for (int i=0; i<gds.getGDSGraphNumber(); i++) { 
            CandidatePatternInstance scpi = scpi_list[i];
            if (scpi != null && scpi.includeAllFrequentExtendedEdgeCode(frq_ee_set)) { 
                this.cpi_list[i] = new CandidatePatternInstance(this,scpi,false,conn); // COPY
                this.num_cpi++;
            } else {
                this.cpi_list[i] = null;
            }
        } 
      
      /// Adding extended edges
        Enumeration enum3 = frq_ee_set.elements();
        while (enum3.hasMoreElements()) {
            Integer item = (Integer) enum3.nextElement();
            ExtendedEdge new_ee = (ExtendedEdge) scpi_ee_mapping.getExtendedEdgeByItem(item.intValue());
            for (int i=0; i<this.gds.getGDSGraphNumber(); i++) { 
                CandidatePatternInstance ccpi = this.cpi_list[i];
                if (ccpi != null) { 
                    ccpi.addExtendedEdge(new_ee.getSrc_idx(),new_ee.getTar_idx(),new_ee.getEdge_label());
                }
            }
        }    
        if (scp.getNum_cpi() == this.getNum_cpi()) scp.unsetMax_cp();
        
    }     
    
    public ExtendedEdgeMapping getExtendedEdgeMapping() {
        return this.ee_mapping;
    }
    
    public EdgecodeMapping getEdgecodeMapping() {
        return this.mapping;
    }
    
    private int findFirstCPIIndex() {
        for (int i=0; i<gds.getGDSGraphNumber();i++) {
            CandidatePatternInstance cpi = this.cpi_list[i];
            if (cpi !=null) return i;
        }
        return 0;
    }
    
    
    public boolean isInterestPattern() {
        int[] vertex_kind = new int[23];
        for (int i=0; i<23; i++) {
            vertex_kind[i] = 0;
        }
        CandidatePatternInstance cpi = this.cpi_list[this.findFirstCPIIndex()]; 
        Hashtable cpi_vertices_hash = cpi.getCpiVerticesHash();
        int[] cpi_vertices_list = cpi.getCpiVerticesList();
        for (int i=0; i<cpi.getCpiVertices_num(); i++) {
            Vertex v = (Vertex) cpi_vertices_hash.get(new Integer(cpi_vertices_list[i]));
            vertex_kind[v.getVertex_kind_id()]  = vertex_kind[v.getVertex_kind_id()] + 1;
        }  
        if (vertex_kind[5]>1 || vertex_kind[6]>0 || vertex_kind[21]>0 || vertex_kind[10]>1) return true;
        else return false;
    }    
    
    // CandidatePattern for extension by Boundary edges
    public CandidatePattern(CandidatePattern scp, Hashtable frq_edgecode_set, Connection conn) throws SQLException {
        this.conn = conn;
        this.scp = scp;
        this.gds = scp.getGds();
        CandidatePatternInstance[] scpi_list = scp.getCpi_list();
        this.cpi_list = new CandidatePatternInstance[gds.getGDSGraphNumber()];
        // Determine the pattern instances of the candidate pattern
        for (int i=0; i<gds.getGDSGraphNumber(); i++) { 
            CandidatePatternInstance scpi = scpi_list[i];
            if (scpi != null && scpi.includeAllFrequentEdgeCode(frq_edgecode_set)) { 
                this.cpi_list[i] = new CandidatePatternInstance(this,scpi,true,conn);  // copy 
                this.num_cpi++;
            } else {
                this.cpi_list[i] = null;
            }
        }    
        addCPNewNodes(scp,frq_edgecode_set);       
    }    
    
    private int getSuggestVertexLabel(EdgeCode edgecode) {
       Vector[] label_list = new Vector[gds.getGDSGraphNumber()];
       int support = 0;
       for (int i=0; i<gds.getGDSGraphNumber(); i++) {
           CandidatePatternInstance cpi = this.cpi_list[i];
           if (cpi != null) {
               // label_list contain the qualified vertex labels 
               label_list[i] = cpi.findCandidateVertexLabel(edgecode); 
               support++; 
           } else {
               label_list[i] = null;
           }
       }

       Hashtable label_table = new Hashtable();
       for (int cpi_idx=0; cpi_idx<gds.getGDSGraphNumber(); cpi_idx++) {
           if (label_list[cpi_idx] != null) {
               for (int i=0; i<label_list[cpi_idx].size(); i++) {
                   Integer vertex_label = (Integer) label_list[cpi_idx].get(i);
                   boolean[] cpi_label_mapping = (boolean[]) label_table.get(vertex_label);
                   if (cpi_label_mapping == null) {
                       cpi_label_mapping = new boolean[gds.getGDSGraphNumber()];
                       for (int j=0; j<gds.getGDSGraphNumber(); j++) cpi_label_mapping[j] = false;
                       cpi_label_mapping[cpi_idx] = true;
                       label_table.put(vertex_label,cpi_label_mapping);
                   } else {
                       cpi_label_mapping[cpi_idx] = true;
                   }
               }
           }
       }
       
       int current_support= 0;
       int suggested_label = 0;
       Enumeration labels = label_table.keys();
       while (labels.hasMoreElements()) {
           Integer label = (Integer) labels.nextElement();
           boolean[] cpi_label_mapping = (boolean[]) label_table.get(label);
           int temp_support = 0;
           for (int i=0; i<cpi_label_mapping.length; i++) {
               if (cpi_label_mapping[i]) temp_support++;
           }
           if (temp_support > current_support) {
               current_support = temp_support;
               suggested_label = label.intValue();
           }
       }
       
       return suggested_label;       
    }
    
    public boolean getInsertNewNode() {
        return this.insertNewNode;
    }
    
    private boolean isConsistentCPnodes(EdgeCode edgecode, Vertex[] new_candidate_nodes) {
        if (edgecode.getEnd_Vertex_kind_id()!=6) return true;
    
        if (edgecode.getEdge_label() <= 2 && 
            (!(edgecode.getEdge_label()==0 && edgecode.getStart_Vertex_Kind_Id()==2 && edgecode.getEnd_Vertex_kind_id()==6))) return true;       

        Hashtable ht = new Hashtable();
        for (int i=0; i<gds.getGDSGraphNumber(); i++) {
            if (new_candidate_nodes[i] != null) {
                String key = new_candidate_nodes[i].getFieldname1();
                if (key == null || key.equals("")) key = "NOFIELD";
                Object obj = ht.get(key);
                if (obj == null) ht.put(key,key);
            }
        }
        if (ht.size()>3) return false;
        return true;   
    }
    
    private EdgeCode[] getFrqBE(CandidatePattern scp, Hashtable frq_edgecode_set) {
        EdgeCode[] fbes = new EdgeCode[frq_edgecode_set.size()];
        Enumeration items_enum = frq_edgecode_set.elements();
        EdgecodeMapping scp_mapping = scp.getEdgecodeMapping();
        int i = 0;
        while (items_enum.hasMoreElements()) {
            Integer item = (Integer) items_enum.nextElement();
            EdgeCode edgecode = scp_mapping.getEdgeCodeByItem(item.intValue());
            fbes[i++] = edgecode;
        }
        Arrays.sort(fbes);
        return fbes;
    }
    

    
    ///2008-01-18
    private void addCPNewNodes(CandidatePattern scp, Hashtable frq_edgecode_set) throws SQLException {

            int suggested_cp_label = 0;

            EdgeCode[] fbes = this.getFrqBE(scp,frq_edgecode_set);
            for (int idx=0; idx<fbes.length; idx++) {
                EdgeCode edgecode = fbes[idx];
                Vertex[] new_candidate_nodes = new Vertex[this.gds.getGDSGraphNumber()];
                
                // If the new node is a control point, we choose a suggested label for the control point            
                if (edgecode.getEnd_Vertex_kind_id() == 6) {
                    suggested_cp_label = getSuggestVertexLabel(edgecode);
                } 
                
                boolean is_ao_to_cp_edge = edgecode.isAo_to_Cp_Data_edge();
                int ao_to_cp_edge_num = 0;
                boolean ao_to_cp_flag = true;
                while (isQualifiedForAdding(edgecode,new_candidate_nodes,suggested_cp_label)) {
                    
                    if (ao_to_cp_edge_num > Parameter.getNUM_FROM_AO_TO_CP()) ao_to_cp_flag = false;
                    
                    if (notIdenticalNodes(new_candidate_nodes,edgecode) && isConsistentCPnodes(edgecode,new_candidate_nodes) && ao_to_cp_flag) {
                        for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
                            CandidatePatternInstance cpi = this.cpi_list[i];
                            if (cpi!= null) {
                                cpi.addNewNode(edgecode.getStart_Vertex_Kind_Id() ,new_candidate_nodes[i]);
                                this.cp_vertices_num = cpi.getCpiVertices_num();
                            }
                        }
                        this.addCPIedges();
                        this.insertNewNode = true;
                        
                        if (is_ao_to_cp_edge) ao_to_cp_edge_num++;
                        
                    } else { //identical key won't be used for expanding CP
                        for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
                            CandidatePatternInstance cpi = this.cpi_list[i];
                            if (cpi != null) {
                                cpi.setIdenticalKey(new_candidate_nodes[i].getVertex_key_Integer());
                            }
                        }          
                    }
                }
            }
        }     
    
    
    
/*    private void addCPNewNodes(CandidatePattern scp, Hashtable frq_edgecode_set) throws SQLException {
        Enumeration items_enum = frq_edgecode_set.elements();
        EdgecodeMapping scp_mapping = scp.getEdgecodeMapping();
        int suggested_cp_label = 0;
        
            
        
        
        while (items_enum.hasMoreElements()) {
            Integer item = (Integer) items_enum.nextElement();
            EdgeCode edgecode = scp_mapping.getEdgeCodeByItem(item.intValue());

            Vertex[] new_candidate_nodes = new Vertex[this.gds.getGDSGraphNumber()];
            
            // If the new node is a control point, we choose a suggested label for the control point            
            if (edgecode.getEnd_Vertex_kind_id() == 6) {
                suggested_cp_label = getSuggestVertexLabel(edgecode);
            } 
            
            while (isQualifiedForAdding(edgecode,new_candidate_nodes,suggested_cp_label)) {
                if (notIdenticalNodes(new_candidate_nodes,edgecode) && isConsistentCPnodes(edgecode,new_candidate_nodes)) {
                    for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
                        CandidatePatternInstance cpi = this.cpi_list[i];
                        if (cpi!= null) {
                            cpi.addNewNode(edgecode.getStart_Vertex_Kind_Id() ,new_candidate_nodes[i]);
                            this.cp_vertices_num = cpi.getCpiVertices_num();
                        }
                    }
                    this.addCPIedges();
                    this.insertNewNode = true;
                } else { //identical key won't be used for expanding CP
                    for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
                        CandidatePatternInstance cpi = this.cpi_list[i];
                        if (cpi != null) {
                            cpi.setIdenticalKey(new_candidate_nodes[i].getVertex_key_Integer());
                        }
                    }          
                }
            }
        }
    }   */      
    
    private boolean notIdenticalNodes(Vertex[] new_candidate_nodes, EdgeCode edgecode) {
       Hashtable identical_hash = new Hashtable();
       Hashtable identical_pdg = new Hashtable();
       for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
           Vertex v = new_candidate_nodes[i];
           if (v != null) {
               Object obj = identical_hash.get(v.getVertex_key_Integer());
               if (obj == null) {
                   identical_hash.put(v.getVertex_key_Integer(),v.getVertex_key_Integer());
               }   
               obj = identical_pdg.get(new Integer(v.getPdg_id()));
               if (obj == null) {
                   identical_pdg.put(new Integer(v.getPdg_id()),new Integer(v.getPdg_id()));
               }
           }
       }
       if (identical_hash.size() <= 2) return false;
       if (identical_pdg.size() <=2  && edgecode.getEdgeDirection() == 0) return false;  
       return true;
    }
    
    private void addCPIedges() {
        for (int i=0; i<this.cp_vertices_num; i++) {
            for (int j=i+1; j<this.cp_vertices_num; j++) {  // j: existing node
                for (int k=3; k<6; k++) {                   // k: edge type
                    boolean forward_edge = true;
                    boolean backward_edge = true;
                    int[] sdd_forward_labels = new int[gds.getGDSGraphNumber()];
                    int[] sdd_backward_labels = new int[gds.getGDSGraphNumber()];
                    for (int cpi_idx=0; cpi_idx<gds.getGDSGraphNumber(); cpi_idx++) {
                        CandidatePatternInstance cpi = this.cpi_list[cpi_idx];
                        if (cpi != null) {
                            if (!cpi.checkEdgeBetweenTwoNodesInCPI(i,j,k)) {
                                forward_edge = false;
                            } else {
                                if (k == 5) sdd_forward_labels[cpi_idx] = cpi.getSddLabelBetweenTwoNodes(i,j,k); // SDD edges 
                            }
                            if (!cpi.checkEdgeBetweenTwoNodesInCPI(j,i,k)) {
                                backward_edge = false;
                            } else {
                                if (k == 5) sdd_backward_labels[cpi_idx]  = cpi.getSddLabelBetweenTwoNodes(j,i,k); // SDD edges
                            }
                        } else {
                            sdd_forward_labels[cpi_idx] = 0;
                            sdd_backward_labels[cpi_idx] = 0;
                        }
                    }    
                    if (forward_edge || backward_edge) {
                        //        this.cp_edges[i][j][k] = 1;
                        int forward_sdd_label = 0;
                        int backward_sdd_label = 0;
                        if (k == 5) {
                            if (forward_edge) forward_sdd_label = this.hasIdenticalSDDLabel(sdd_forward_labels);
                            if (backward_edge) backward_sdd_label = hasIdenticalSDDLabel(sdd_backward_labels);
                        }
                        for (int cpi_idx=0; cpi_idx<this.gds.getGDSGraphNumber(); cpi_idx++) {
                            CandidatePatternInstance cpi = this.cpi_list[cpi_idx];
                            if (cpi != null) {
                                if (k == 5) {
                                    if (forward_sdd_label != 0) cpi.addNewEdgeToCPI(i,j,k-3,forward_sdd_label);
                                    if (backward_sdd_label != 0) cpi.addNewEdgeToCPI(j,i,k-3,backward_sdd_label);
                                
                                } else { 
                                    if (forward_edge) cpi.addNewEdgeToCPI(i,j,k-3,1);
                                    if (backward_edge) cpi.addNewEdgeToCPI(j,i,k-3,1);
                                }
                            }
                        }
                    }
                }
            }
        }    
    }
    
    private int hasIdenticalSDDLabel(int[] sdd_labels) {
        int key = 0;
        for (int i=0; i<gds.getGDSGraphNumber(); i++) {
            if (sdd_labels[i] != 0) {
                if (key == 0) {
                    key = sdd_labels[i];
                }
                if (key != sdd_labels[i]) return 0;
            }
        }
        return key;
    }
    

    private boolean isQualifiedForAdding(EdgeCode edgecode, Vertex[] new_candidate_nodes, int suggested_cp_label) {
        for (int i=0; i<gds.getGDSGraphNumber(); i++) {
            CandidatePatternInstance cpi =  this.cpi_list[i];
            if (cpi!= null) {
                Vertex new_node = cpi.findBestNewNodeCandidate(edgecode,suggested_cp_label);
                if (new_node != null) {
                    new_candidate_nodes[i] = new_node;
                } else {
                    return false;
                }
            } 
        }   
        return true;
    }    
      
    public int getNum_cpi() {
        return this.num_cpi;
    }
   
   
    public boolean getMax_cp() {
        return this.max_cp;
    }

    public void setMax_cp() {
        this.max_cp = true;
    }
    
    
    
    public void unsetMax_cp() {
        this.max_cp = false;
    }
   
    // type: 0: data + sdd --> the target node can't be control point
    //       1: data + sdd --> the target node are control point
    //       2: control dependence
    public Hashtable[] buildEdgeInfoForMining(int type) {
        Hashtable[] edgeinfo = new Hashtable[gds.getGDSGraphNumber()];
        for (int i=0; i<gds.getGDSGraphNumber(); i++) {
            edgeinfo[i] = null;
            CandidatePatternInstance cpiworking = cpi_list[i];
            if (cpiworking != null) {
                Hashtable edgecodeitemset = cpiworking.findEdgesIncidentFromBN(mapping,type);  // Return the boundary edge of the CPI
                if (edgecodeitemset.size() >0 ) edgeinfo[i] = edgecodeitemset;  // The set of boundary edgeset of the CP
            }
        }
        return edgeinfo;
    }
    
    public Hashtable[] buildExtendedEdgeInfoForMining() {
        Hashtable[] edgeinfo = new Hashtable[gds.getGDSGraphNumber()];
        for (int i=0; i<this.gds.getGDSGraphNumber(); i++) {
            CandidatePatternInstance cpiworking = this.cpi_list[i];
            edgeinfo[i] = null;
            if (cpiworking != null) {
                Hashtable eeitemset = cpiworking.findExtendedEdges(this.ee_mapping);
                if (eeitemset.size() >0 ) 
                    edgeinfo[i] = eeitemset; // 2007-02-26 modification
            }
        }
        return edgeinfo;
    }     
    
    public CandidatePatternInstance getCpi(int index) {
        return this.cpi_list[index];
    }
    
    public CandidatePatternInstance[] getCpi_list() {
        return this.cpi_list;
    }
    
    public GraphDataset getGds() {
        return this.gds;
    }     
    
}
