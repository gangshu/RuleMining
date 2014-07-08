package andy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStreamReader;

import java.sql.*;
import java.util.*;
public class GraphDataset {
    
    RIDS[] rids_list;
    int gds_id;
    int graph_num = 0;
    Connection conn;
    int num_cp = 0; 
    Vector cp_list = new Vector();
    int support;
    float frequency;
    java.util.Date start_time;
    EdgecodeMapping mapping = new EdgecodeMapping(); //mapping between edge info <---> item
    int candidate_label;
    
    
    public GraphDataset(int gds_id, Connection conn, float frq) throws SQLException, IOException {
        this.start_time = new java.util.Date();
        this.gds_id = gds_id;
        this.frequency = frq;
        this.conn = conn;
        loadGraphDataset();    
        this.support = Math.round(frq*graph_num);
        CandidatePattern init_cp = new CandidatePattern(this,conn);
        cp_list.add(num_cp++,init_cp);
        findCandidatePatternByBoundaryEdge(); //extend candidate pattern by boundary edges
        findMaxFrequentSubgraphByExtendedEdge();  // extend candidate pattern by extended edges
        outputPatternsToDB();
    }
    

    
    private void findMaxFrequentSubgraphByExtendedEdge() throws SQLException, IOException {
        int num_frq_subgraph = this.num_cp;
        for (int i=0; i < num_frq_subgraph; i++) { 
            CandidatePattern cpw = (CandidatePattern) this.cp_list.get(i);
            if (cpw!=null && cpw.isInterestPattern() && cpw.getMax_cp()) {
                 Vector frq_ee_itemsets = this.buildFrequentExtendedEdges(cpw);
                 if (frq_ee_itemsets != null && frq_ee_itemsets.size()!=0) {
             
                 /// 2007-10-21
                     cpw.unsetMax_cp();             
                     Enumeration enum2 = frq_ee_itemsets.elements();
                     while (enum2.hasMoreElements()) {
                         Hashtable frq_ee_itemset = (Hashtable) enum2.nextElement();
                         CandidatePattern expandedCP = new CandidatePattern(cpw,frq_ee_itemset,"EE",conn);
                         expandedCP.setMax_cp();
                         if (cpw.getNum_cpi()==expandedCP.getNum_cpi()) {
                             cpw.unsetMax_cp();
                             this.cp_list.add(this.num_cp, expandedCP);
                             this.num_cp++;
                         } else {
                             if (!this.isSubsetCP(expandedCP,-1)) {
                                 this.cp_list.add(this.num_cp,expandedCP);
                                 this.num_cp++;
                             }
                         } 
                     }
                 }
            }
            if (cpw!=null && (!cpw.getMax_cp() || !cpw.isInterestPattern())) {
                cp_list.set(i,null);
                cpw.clearCandidatePattern();
            }

        }       
    }
    
    private Vector buildFrequentExtendedEdges(CandidatePattern cpw) throws IOException {
        String filename = Parameter.dir + "GDS_E_" + (new Integer(this.gds_id)).toString();
        int rec_num = this.outputExtendedEdgesForMining(cpw,filename);
        if (rec_num >= support) {
            this.executeFrequentEdgeCodeMining(filename,rec_num);
            Vector list_frq_edge_code_set = this.readFrequentEdgeCode(filename);
            return list_frq_edge_code_set;  
        } else{
            return null;
        }
    }    
    
 
    
    
    private void outputPatternsToDB() throws SQLException {

        for (int i=0; i<this.cp_list.size(); i++) {
            CandidatePattern cp = (CandidatePattern) cp_list.get(i);
            if (cp!=null && cp.getMax_cp()) {
                outputSingleCP(cp,i);
            }
        }
    }   
    
    private void outputSingleCP(CandidatePattern cp, int cp_id) throws SQLException {
    
        // Output Parameter: 1: Pattern Link Key,  
        // Input Parameter 2:ggi, 3: candidate_node_label 4: frequent_item_set_id 5: pattern_id 6: frequency
        java.util.Date end_time = new java.util.Date();
        long interval = end_time.getTime()-start_time.getTime();
        CallableStatement cstmt = conn.prepareCall("{? = call ins_pattern(?,?,?,?,?,?)}");
        cstmt.registerOutParameter(1,Types.INTEGER);
        cstmt.setInt(2,this.gds_id); //graph group id
        cstmt.setInt(3,this.candidate_label); // candidate node label
        cstmt.setInt(4,cp_id);  // frequent item set id
        cstmt.setInt(5,cp_id);  //  pattern_id
        cstmt.setInt(6,cp.getNum_cpi());      //  frequency
        cstmt.setLong(7,interval);
        cstmt.execute();
        int pattern_key = cstmt.getInt(1);
        cstmt.close();
       
        // Pattern Instance1(patter_key, instance_id, src_vertex_key, tar_vertex_key, edge_type)
        String ins_sql = "INSERT INTO pattern_instance VALUES(?,?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(ins_sql) ;
       
        CandidatePatternInstance[] list_cpi = cp.getCpi_list();
        int pattern_instance = 0;
        for (int cpi_idx=0; cpi_idx<this.graph_num; cpi_idx++) {
            CandidatePatternInstance cpi = list_cpi[cpi_idx];
            if (cpi != null) {
                int[] cpi_vertices_list = cpi.getCpiVerticesList();
                int[][][] cpi_edges = cpi.getCpiEdges();
                int num_cpi_vertices = cpi.getCpiVertices_num();
                // Output a pattern instance
                for (int i=0; i<num_cpi_vertices; i++) {
                    for (int j=0; j<num_cpi_vertices; j++) {
                        for (int k=0; k<3; k++) {
                            if (cpi_edges[i][j][k] !=0) {
                                pstmt.setInt(1,pattern_key);
                                pstmt.setInt(2,0);    
                                pstmt.setInt(3,gds_id);
                                pstmt.setInt(4,pattern_instance);
                                pstmt.setInt(5,cpi_vertices_list[i]);
                                pstmt.setInt(6,cpi_vertices_list[j]);
                                if (k<=1) {
                                    pstmt.setInt(7,k);
                                } else {
                                    pstmt.setInt(7,cpi_edges[i][j][k]);
                                }
                                pstmt.execute();
                            }
                        }
                    }        
                }
                pattern_instance++;
            }
        }
        pstmt.close(); 
       

       /************ 2007-03-15*/
        ins_sql = "INSERT INTO pattern_node_info VALUES(?,?,?,?)";
        pstmt = conn.prepareStatement(ins_sql) ;
        list_cpi = cp.getCpi_list();
        pattern_instance = 0;
        for (int cpi_idx=0; cpi_idx<graph_num; cpi_idx++) {
            CandidatePatternInstance cpi = list_cpi[cpi_idx];
            if (cpi != null) {
                int[] cpi_vertices_list = cpi.getCpiVerticesList();
                int num_cpi_vertices = cpi.getCpiVertices_num();
                // Output a pattern instance
                for (int i=0; i<num_cpi_vertices; i++) {
                    pstmt.setInt(1,pattern_key);
                    pstmt.setInt(2,pattern_instance);
                    pstmt.setInt(3,i);
                    pstmt.setInt(4,cpi_vertices_list[i]);
                    pstmt.execute();  
                }
                pattern_instance++;
            }
        }
        pstmt.close();   
    }




    private void findCandidatePatternByBoundaryEdge() throws SQLException, IOException {
       
        int start_cp_index = 0;
        int start_cp_num = 0;
        int next_cp_index = 0;
        do {
            start_cp_num = this.num_cp;
            expandCpByType(start_cp_index,0); // Using Transitive Data dependence and SDD edges (not including control-point)
            next_cp_index = this.num_cp;
            expandCpByType(start_cp_index,1); // Using Data dependence and SDD edges (only control points)
            expandCpByType(start_cp_index,2); // Uisng control dependences
            start_cp_index = next_cp_index;
        } while (start_cp_num < this.num_cp);
    
    }
    
    //expnad Candidate Pattern by using Data dependence and SDD edges (Not including control point
    private void expandCpByType(int start_index, int type) throws IOException, SQLException {

        Vector list_frq_edgecode_set;
        while (start_index < this.num_cp) {
            CandidatePattern cpw = (CandidatePattern) cp_list.get(start_index);
            if (cpw != null) {
                cpw.setMax_cp();
                list_frq_edgecode_set = buildFrequentEdges(cpw,type); // build frequent edges            
                if (list_frq_edgecode_set != null && list_frq_edgecode_set.size()>0) {
                    Enumeration enum1 = list_frq_edgecode_set.elements();  
                    while (enum1.hasMoreElements()) {
                        Hashtable frq_edgecode_set = (Hashtable) enum1.nextElement();
                        // Expand the existing pattern by adding frequent edges 
                        CandidatePattern expandedCP = new CandidatePattern(cpw,frq_edgecode_set,conn);
                        // No new nodes are inserted into expanded CP (frq edges are derived from the same nodes
                        if (expandedCP.getInsertNewNode()) {                
                            if (cpw.getNum_cpi() == expandedCP.getNum_cpi()) {
                                cpw.unsetMax_cp();
                                this.cp_list.add(this.num_cp,expandedCP);
                                this.num_cp++;
                            } else {
                                if (!isSubsetCP(expandedCP,0)) {
                                    this.cp_list.add(this.num_cp,expandedCP);
                                    this.num_cp++;
                                } else {
                                    expandedCP.clearCandidatePattern();
                                }        
                            }
                        } else {
                            expandedCP.clearCandidatePattern();
                        }
                    }
                }
            }
            if (cpw != null && !cpw.getMax_cp()) {
                this.cp_list.set(start_index,null);
                cpw.clearCandidatePattern();
            }
            start_index++;            
        }
    }
    
    
    
    private boolean isSubsetCP(CandidatePattern expandedCP, int index) {
        for (int i=index+1; i<this.num_cp; i++) {
            CandidatePattern cp = (CandidatePattern) this.cp_list.get(i);
            if (cp != null) {
                boolean flag = true;
                for (int j=0; j<this.graph_num; j++) {
                    CandidatePatternInstance cpi = cp.getCpi(j);
                    CandidatePatternInstance cpi_e = expandedCP.getCpi(j);
                    if ((cpi == null && cpi_e != null) || (cpi != null && cpi_e == null)) flag = false;
                }
                if (flag) return true;
            }
        }  
        return false;
    }

    
    
    // build frequent edges
    private Vector buildFrequentEdges(CandidatePattern cpw, int type) throws IOException {
        String filename = Parameter.dir + "GDS_" + (new Integer(this.gds_id)).toString();

        int rec_num = this.outputEdgeInfoForMining(cpw,filename,type); // The filename contain the boundary edge info of each CPI
        if (rec_num >= support) {
            this.executeFrequentEdgeCodeMining(filename,rec_num);  // Mining the frequent boundary edges
            Vector list_frq_edge_code_set = this.readFrequentEdgeCode(filename); // The vector contain the frequent boundary edges
            return list_frq_edge_code_set;  
        } else{
            return null;
        }
    }    
    
    private Vector readFrequentEdgeCode(String filename) throws IOException {
        Vector list_frq_edge_code_set = new Vector();
        BufferedReader reader = new BufferedReader(new FileReader(filename+".out"));    
        String line = null;
        while ((line = reader.readLine()) != null) {
            Hashtable ht = parseFrqEdgeCode(line);
            if (ht.size() > 0) {
               list_frq_edge_code_set.add(ht);
            }
        }
        reader.close();
        return list_frq_edge_code_set;    
    } 

    // Parse the frequent code(edges)
    private Hashtable parseFrqEdgeCode(String line) {
       Hashtable fec = new Hashtable();
       StringTokenizer st = new StringTokenizer(line);
       while (st.hasMoreTokens()) {
         String token = st.nextToken();
         if (!token.substring(0,1).equals("(")) {
           Integer frq_edge_code  = new Integer(token);
           fec.put(frq_edge_code,frq_edge_code);
         }
       }     
       return fec;  
    } 
    
    private void executeFrequentEdgeCodeMining(String filename, int rec_num) throws IOException {
        float mafia_frq = this.graph_num * this.frequency / rec_num;
        if (mafia_frq >=1.0) mafia_frq = 1.0f;
        String command = Parameter.dir +"mafia -mfi " + (new Float(mafia_frq)).toString() + " -ascii " + filename + ".dat "+ filename+".out";      
        System.out.println(command);
        Process proc = Runtime.getRuntime().exec(command);
        // To ensure that frequent item set mining algorithm has completed its task
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        do {
            line = reader.readLine();
        } while (line != null && (!line.equals("END")));
        reader.close();    
    }    
    
    
    
    // Output the boundary edge information to an external file 
    private int outputEdgeInfoForMining(CandidatePattern cpw, String filename, int type) throws IOException {
        // A hash table is created for each candidate pattern instance. 
        // The hash table contains the code of edges (EdgeCode) incident from boundary nodes
        int rec = 0;
        Hashtable[] edge_records = cpw.buildEdgeInfoForMining(type);// Each Element is a Hashtable
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename+".dat")); 
        for (int i=0; i<this.graph_num; i++ ) {
            if (edge_records[i] != null) {
                // The hashtable contain a list of edge code generated by a candidate pattern instance
                Enumeration enum_edge_detail = edge_records[i].elements();
                String line ="";
                while (enum_edge_detail.hasMoreElements()) {
                    EdgeCode e = (EdgeCode) enum_edge_detail.nextElement(); // Each element is an edge code
                    line = line + (new Integer(e.getItem())).toString() +" ";
                }
                rec++;
                writer.write(line+"\n");
            }
        }
        writer.close();
        return rec;
    }    
 
    private int outputExtendedEdgesForMining(CandidatePattern cpw, String filename) throws IOException {
        int rec = 0;
        Hashtable[] edge_records =  cpw.buildExtendedEdgeInfoForMining(); // Each Element is a Hashtable
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename+".dat")); 
        for (int i=0; i<this.graph_num; i++) { 
            if (edge_records[i] != null) {     
                Enumeration enum_edge_detail = edge_records[i].elements();
                String line ="";
                while (enum_edge_detail.hasMoreElements()) {
                    ExtendedEdge e = (ExtendedEdge) enum_edge_detail.nextElement(); // Each element is an edge code
                    line = line + (new Integer(e.getItem())).toString() +" ";
                }
                rec++;
                writer.write(line+"\n");
            }
        }
        writer.close();
        return rec;  
    }     
    
    public int getGDSGraphNumber() {
        return this.graph_num;
    }
    
    public RIDS getRIDS(int i) {
        return rids_list[i];
    }
    
    public RIDS[] getAllRIDS() {
        return rids_list;
    }
    
    private void loadGraphDataset() throws SQLException {
       loadGraphNumber();
        rids_list = new RIDS[graph_num];
        for (int i=0; i<graph_num; i++) {
            rids_list[i] = new RIDS(this,i,conn);
        }       
    }
    
    
    public int getGds_id() {
        return this.gds_id;
    }
    
    private void loadGraphNumber() throws SQLException {
        /**
         * BUG FIX:
         * Exception happens for gds_id 103, 153, 171 in abb_cir_cirld_5_10, when
         * the graph number in graphdatset is not the same as the number of distinct
         * graph_ids in rids_nodes
         */
        String sql = "select gds.candidate_label, count(distinct graph_id) as cnt from graphdataset gds, rids_nodes rn\n"+
                     "where gds.gds_id = rn.gds_id\n"+
                     "and rn.gds_id = ?\n"+
                     "group by gds.candidate_label" ;
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,gds_id);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
           this.graph_num = rset.getInt("cnt");
           this.candidate_label = rset.getInt("CANDIDATE_LABEL");
        }
        rset.close();
        pstmt.close();        
    }
    
}
