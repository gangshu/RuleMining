package andy;

import java.io.IOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PatternViolationDetection {

    Connection conn;
    int pattern_key;
    int canidate_node_label;
    Pattern pattern;
  
    public PatternViolationDetection(int pattern_key,int candidate_node_label, Connection conn, int vertex_key) throws SQLException {
        this.conn = conn;
        this.pattern_key = pattern_key;
        this.canidate_node_label = candidate_node_label;
        this.pattern = new Pattern(pattern_key,conn);      
        
        String sql = "SELECT * FROM vertex WHERE vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
            int pdg_id = rset.getInt("PDG_ID");
            System.out.println(pattern.getPattern_key() +":"+vertex_key);
            this.findInstance(vertex_key,pdg_id);
        }
        rset.close();
        pstmt.close();        
    }  
    
    public PatternViolationDetection(int pattern_key,int candidate_node_label, Connection conn) throws SQLException {
        this.conn = conn;
        this.pattern_key = pattern_key;
        this.canidate_node_label = candidate_node_label;
        this.pattern = new Pattern(pattern_key,conn);
    }
    
     
 /*    public void findPatternInstances() throws SQLException {
         String sql = "select v.* from pattern_node_info p, vertex v " +
                      "where p.pattern_key = ? and p.node_index = 0 and v.vertex_key = p.vertex_key";
         PreparedStatement pstmt = conn.prepareStatement(sql);
         pstmt.setInt(1,pattern.getPattern_key());
         ResultSet rset = pstmt.executeQuery();
         int count = 0;
        
         while (rset.next() && count++<10) {
             int vertex_key = rset.getInt("VERTEX_KEY");
             int pdg_id = rset.getInt("PDG_ID");
             System.out.println(pattern.getPattern_key() +":" + vertex_key);
             this.findInstance(vertex_key,pdg_id);
         }
         rset.close();
         pstmt.close();         
     } */
     
    
    public void findPatternInstances() throws SQLException {
        String sql = "SELECT * FROM vertex WHERE vertex_label = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.canidate_node_label);
        ResultSet rset = pstmt.executeQuery();
        int count = 0;
        while (rset.next() && count++<100) {
            int vertex_key = rset.getInt("VERTEX_KEY");
            int pdg_id = rset.getInt("PDG_ID");
//            if (vertex_key ==  2213459 || vertex_key == 2248540  || vertex_key == 2325520  ||
//                vertex_key == 2380627  || vertex_key == 2356168) {
            //if (vertex_key == 64803 ) {
//              if (!checkViolation(vertex_key)) {
                  System.out.println(pattern.getPattern_key() +":"+vertex_key);
                  this.findInstance(vertex_key,pdg_id);
                  System.out.println("==============================================================================\n");
//              }
            //}
        }
        rset.close();
        pstmt.close();
    }   
    
    private boolean checkViolation(int vertex_key) throws SQLException  {
        String sql = "SELECT count(*) FROM violations WHERE pattern_key = ? AND vertex_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pattern_key);
        pstmt.setInt(2,vertex_key);
        ResultSet rset = pstmt.executeQuery();
        rset.next();
        int has_detect = rset.getInt(1);
        rset.close();
        pstmt.close();
        if (has_detect>0) return true;
        else              return false;
    
    }
    
    
    private void outputComputationTime(long cr_interval, long matching_interval) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("insert into violation_interval values(matching_seq.nextval,?,?)");
        pstmt.setLong(1,cr_interval);
        pstmt.setLong(2,matching_interval);
        pstmt.execute();
        pstmt.close();
    }
    
    private void findInstance(int vertex_key, int pdg_id) throws SQLException {
        int index = 0;
        int ids_num = 1;
        IDS[] ids_list = new IDS[1000];
        ids_list[0] = null;    
        java.util.Date start_time_ids;
        java.util.Date end_time_ids;
        java.util.Date start_time_matching;
        java.util.Date end_time_matching;
        
        while (index < ids_num && ids_num < 1000) {
            IDS ids;
            if (ids_list[index] == null && index == 0)  {
                start_time_ids = new java.util.Date();
                ids = new IDS(vertex_key,pdg_id,conn,pattern);
                end_time_ids = new java.util.Date();
                start_time_matching = new java.util.Date();
                comparePattern(ids,conn);
                end_time_matching = new java.util.Date();
                this.outputComputationTime(end_time_ids.getTime()-start_time_ids.getTime(),
                                           end_time_matching.getTime()-start_time_matching.getTime());
                if (ids.isMatchPattern()) {
                    ids.setWhetherOutputResult(true);
                    System.out.println("match\n");
                    this.saveMatchingInfo(this.pattern_key,vertex_key,-9,"S","I");
                    ids.setWhetherCheckSuperIDS(false);
                } else {
                    System.out.println("fail:" + ids.getLostNodes() +"-" + ids.getLostEdges()+"\n");
                    this.saveMatchingInfo(this.pattern_key,vertex_key,-9,"F","I");
                    ids.setWhetherCheckSuperIDS(true);
                }
                ids_list[ids_num] = ids;
                ids_num++;
            } else {
                IDS subIDS = ids_list[index];
                boolean willcheck = true;
                if (!subIDS.isMatchPattern() && subIDS.getLostNodes() > 0 && subIDS.getIteration_number()<=2 && (willcheck = subIDS.willCheckSuperIDS())) {  // 
                    if (subIDS.evaluateForExtension(conn)) {
                        Vector ext_ids_info = this.getCallerVertexKey(subIDS.getToppest_pdg_id(),conn);
                        Enumeration ext_ids_enum = ext_ids_info.elements();
                        int fail_count = 0;
                        int sub_ids_num = 0;
                        int ids_num_before_iteration = ids_num;
                        while (ext_ids_enum.hasMoreElements() && fail_count < 3 && sub_ids_num <= 10) {
                            ExtendedIDSInformation ext_ids = (ExtendedIDSInformation) ext_ids_enum.nextElement();
                            System.out.println(ext_ids.getP_Pdg_id());   
                            start_time_ids = new java.util.Date();
                            ids = new IDS(ext_ids.getP_Pdg_id(),conn,subIDS);
                            end_time_ids = new java.util.Date();
                            if (ids.getIds_vertices_number() > subIDS.getIds_vertices_number()) { // The IDS has been expanded
                                start_time_matching = new java.util.Date();
                                comparePattern(ids,conn);
                                end_time_matching = new java.util.Date();
                                this.outputComputationTime(end_time_ids.getTime()-start_time_ids.getTime(),
                                                           end_time_matching.getTime()-start_time_matching.getTime());                                
                                ids_list[ids_num] = ids;                        
                                ids_num++;
                                if (!ids.isMatchPattern()) {
                                    System.out.println("superIDS failed:" + ids.getLostNodes() +"-" +ids.getLostEdges()  +":"+ext_ids.getP_Pdg_id() +":"+ ext_ids.getVertex_key()+"\n");
                                    fail_count++;
                                    this.saveMatchingInfo(this.pattern_key,vertex_key,ext_ids.getVertex_key(),"I","F");
                                    System.out.println("Failure Times:" + fail_count + "subIDS level:" +subIDS.getIteration_number());
                                } else {
                                    this.saveMatchingInfo(this.pattern_key,vertex_key,ext_ids.getVertex_key(),"I","S");
                                    System.out.println("superIDS matched\n");
                                }
                                sub_ids_num++;
                            }
                        }
                        
                        
                        // if subIDS is outputted, then the super IDS won't be outputted because that
                        // the violation occurs in the subIDS
                        boolean outputSubIds = this.evaluateWhetherOutputSubIds(ids_num_before_iteration,ids_num,ids_list);
                        subIDS.setWhetherOutputResult(outputSubIds);
                        
                        for (int i=ids_num_before_iteration; i<ids_num; i++) {
                            // if the subIDS is outputted, then the super IDS won't be outputted
                            // On the contrary, the superIDS is outputted if the subIDS won't be outputted
                            ids_list[i].setWhetherOutputResult(!outputSubIds);    
                            if (outputSubIds) { 
                                ids_list[i].setWhetherCheckSuperIDS(false);
                            }
                        }
                        
                    } else {
                        int subIDS_index = subIDS.getSubIDSIndex();
                        if (subIDS_index != -1 && !ids_list[subIDS_index].getOutputResult()) {
                            subIDS.setWhetherOutputResult(true);
                        }
                        System.out.println("Don't extend \n");
                    }
                } else {
                    if (!subIDS.isMatchPattern() && (subIDS.getLostNodes() == 0 || subIDS.getIteration_number() >= 3 || !willcheck || (subIDS.getLostNodes() == 0 && subIDS.getLostEdges() >0))) {
                        int subIDS_index = subIDS.getSubIDSIndex();
                        if (subIDS_index != -1 && !ids_list[subIDS_index].getOutputResult()) {
                            subIDS.setWhetherOutputResult(true);
                        }
                    }
                }
            }
            index++;
        }
        
        for (int i=1; i<ids_num; i++) {
            if (ids_list[i].getOutputResult()) {          
                ids_list[i].outputMatchingResult(conn, this.computeRidsClosures(ids_list[i],getRidsEdges(ids_list[i])));
            }
        }
    
    }
    
    
    private void saveMatchingInfo(int pattern_key, int vertex_key, int super_vertex_key, String matching, String super_matching)  throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO rule_instance VALUES(?,?,?,?,?)");
        pstmt.setInt(1,pattern_key);
        pstmt.setInt(2,vertex_key);
        pstmt.setInt(3,super_vertex_key);
        pstmt.setString(4,matching);
        pstmt.setString(5,super_matching);
        pstmt.execute();
        pstmt.close();
    }
    
    
    
    private boolean evaluateWhetherOutputSubIds(int ids_num_before_iteration, int current_ids_num, IDS[] ids_list) {
        int succ_num = 0;
        int fail_num = 0;
        int total_num = 0;
        for (int i = ids_num_before_iteration; i < current_ids_num; i++) {
            if (ids_list[i].isMatchPattern()) {
                succ_num++;
            } else {
                fail_num++;
            }
            total_num++;
        }
        if (total_num == 0) return true;
        if (fail_num >= 3) return true;
        float fail_rate = fail_num / total_num;
        if (fail_rate >= 0.4) return true;
        else return false;
    }
    
    
    private Vector getCallerVertexKey(int pdg_id, Connection conn) throws SQLException {
        Vector ext_ids_info = new Vector();
        String sql = "SELECT v.* FROM vertex v, pdg p  " +
                     "WHERE  v.pdg_id = ? AND v.vertex_kind_id = 8 " +
                     "AND    p.pdg_id = v.pdg_id " +
                     "AND    p.pdg_kind = 'user-defined'";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pdg_id);
        ResultSet rset = pstmt.executeQuery();
        int entry_node_vertex_key;
        if (rset.next()) {
            entry_node_vertex_key = rset.getInt("VERTEX_KEY");
            System.out.println(rset.getInt("PDG_ID"));
            rset.close();
            pstmt.close();
        } else {
            rset.close();
            pstmt.close();
            return ext_ids_info;
        }
        sql = "SELECT * FROM vertex WHERE vertex_label = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,entry_node_vertex_key);
        rset = pstmt.executeQuery();
        while (rset.next()) {
           int vertex_key = rset.getInt("VERTEX_KEY");
           int p_pdg_id = rset.getInt("PDG_ID");
           ExtendedIDSInformation ext_ids = new ExtendedIDSInformation(vertex_key,p_pdg_id);
           ext_ids_info.add(ext_ids);
        }
        rset.close();
        pstmt.close();       
        return ext_ids_info;        
    }
    
    private int[][][] getRidsEdges(IDS ids) {
        Hashtable ids_vertices_hash = ids.getIds_vertices_hash();
        int[] ids_vertices_list = ids.getIds_vertices_list();
        Hashtable rids_edge_hash = ids.getRids_edges();
        int rids_vertices_num = ids.getRids_vertices_num();
        int[][][] rids_edges = new int[rids_vertices_num][rids_vertices_num][3];
        
        for (int i=0; i<rids_vertices_num; i++) {
            for (int j=0; j<rids_vertices_num; j++) {
                for (int k=0; k<=2; k++) {
                    rids_edges[i][j][k] = 0;
                }
            }
        }
        
        Enumeration enum_rids_edges = rids_edge_hash.elements();
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
            rids_edges[rids_src_node_index][rids_tar_node_index][edge_type] = length;
        }
        return rids_edges;
    }

    private int getCallsiteVertexKeyUsingParameterNode(Connection conn, int param_vertex_key) throws SQLException {
        int callsite_vertex_key = 0;
        String sql = "SELECT * FROM edges WHERE tar_vertex_key = ? AND edge_type = 2 ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,param_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) callsite_vertex_key = rset.getInt("SRC_VERTEX_KEY");
        rset.close();
        pstmt.close();
        return callsite_vertex_key;
    }

    private int matchCallsiteGraphByParameterNode(IDS ids, int src_param_vertex_key, int mapped_param_vertex_key, Hashtable has_used,
                                                   int[] mapping, boolean[] has_mapped, Connection conn, int[] matched_queue, int matched_num) throws SQLException {
        int src_callsite_vertex_key = this.getCallsiteVertexKeyUsingParameterNode(conn,src_param_vertex_key);
        int mapped_callsite_vertex_key = this.getCallsiteVertexKeyUsingParameterNode(conn,mapped_param_vertex_key);
        return this.matchCallsiteGraph(ids,src_callsite_vertex_key,mapped_callsite_vertex_key,has_used,mapping,has_mapped,conn,matched_queue,matched_num);
    }

    
    private int matchCallsiteGraph(IDS ids, int src_callsite_vertex_key, int mapped_callsite_vertex_key, Hashtable has_used,
                                    int[] mapping, boolean[] has_mapped, Connection conn, int[] matched_queue, int matched_num) throws SQLException {
                            
        String src_sql = "SELECT v.* FROM edges e, vertex v WHERE e.src_vertex_key = ? " +
                         " AND   e.tar_vertex_key = v.vertex_key AND v.vertex_kind_id in (1,2) ";
        String mapped_sql = "SELECT v.* FROM edges e, vertex v WHERE e.src_vertex_key = ? " +
                            " AND   e.tar_vertex_key = v.vertex_key AND v.vertex_label = ? ";
        Vertex src_callsite_node = pattern.getVertexByKey(src_callsite_vertex_key);
        Vertex mapped_callsite_node = ids.getVertexByKeyFromIDS(mapped_callsite_vertex_key);
        
        if (mapped_callsite_node == null) return matched_num;
        if (src_callsite_node == null) return matched_num;
        
        int src_callsite_node_rids_index = src_callsite_node.getRids_node_index();
        
        System.out.println("call-site:"+src_callsite_vertex_key+"-"+mapped_callsite_vertex_key);
        
        mapping[src_callsite_node_rids_index] = mapped_callsite_node.getVertex_key();
        
        has_mapped[src_callsite_node_rids_index] = true;
        has_used.put(mapped_callsite_node.getVertex_key_Integer(),mapped_callsite_node);
        /**
         * Quick and dirty fix for bug 18
         */
        if(matched_num>=matched_queue.length)
        	return matched_num;
        matched_queue[matched_num] = src_callsite_node_rids_index;
        
        matched_num++;
        
        PreparedStatement src_pstmt = conn.prepareStatement(src_sql);
        src_pstmt.setInt(1,src_callsite_vertex_key);
        ResultSet src_rset = src_pstmt.executeQuery();
        while (src_rset.next()) {
            int src_param_vertex_key = src_rset.getInt("VERTEX_KEY");
            int src_param_vertex_label = src_rset.getInt("VERTEX_LABEL");
            Vertex src_param_node = pattern.getVertexByKey(src_param_vertex_key);
            if (src_param_node != null) { // Parameter node is included in the pattern
                PreparedStatement mapped_pstmt = conn.prepareStatement(mapped_sql);
                mapped_pstmt.setInt(1,mapped_callsite_node.getVertex_key());
                mapped_pstmt.setInt(2,src_param_vertex_label);
                ResultSet mapped_rset = mapped_pstmt.executeQuery();
                if (mapped_rset.next()) {
                    int mapped_param_vertex_key = mapped_rset.getInt("VERTEX_KEY");
                    Vertex mapped_param_node = ids.getVertexByKeyFromRids(mapped_param_vertex_key);
                    if (mapped_param_node != null) {
                        int src_param_node_index = src_param_node.getRids_node_index();
                        mapping[src_param_node_index] = mapped_param_node.getVertex_key();
                        has_mapped[src_param_node_index] = true;
                        has_used.put(mapped_param_node.getVertex_key_Integer(),mapped_param_node); 
                        /**
                         * Quick and dirty fix for bug 18
                         */
                        if(matched_num>=matched_queue.length)
                        	break;
                        matched_queue[matched_num] = src_param_node_index;
                        System.out.println("param_node:"+src_param_node.getVertex_key()+"-"+mapped_param_node.getVertex_key());
                        matched_num++;                       
                    } else {
                        System.out.println("param_node:"+src_param_node.getVertex_key()+"-");
                    }
                    
                }
                mapped_rset.close();
                mapped_pstmt.close();
            }
        }    
        src_rset.close();
        src_pstmt.close();
        return matched_num;
    }
    
    private boolean checkIntegerItemInHashtable(Hashtable ht, Integer item) {
        Object obj = ht.get(item);
        if (obj == null) return false;
        else             return true;
    }
    
    
    
    
    private boolean hasSameSDD(Vertex src_node, Vertex tar_node, Vertex mapped_src_node, Vertex mapped_tar_node) {
        String of1 = src_node.getFieldname1();
        String of2 = tar_node.getFieldname1();
        String mf1 = mapped_src_node.getFieldname1();
        String mf2 = mapped_tar_node.getFieldname1();
        if ( (of1.equals("") && of2.equals("")) || (of1.equals(of2)) ) {
            if (mf1.equals("") && mf2.equals("")) return true;
            if (mf1.equals(mf2)) return true;
            return false;
        }
        
        if ( of1.equals("") && !of2.equals("") ) { 
            if (mf1.equals("") && mf2.equals(of2)) return true;
            return false;
        }
        
        if ( !of1.equals("") && of2.equals("") ) {
            if (mf1.equals(of1) && mf2.equals(of2)) return true;
            return false;
        }
        return false;
    }
    
    private boolean qualifiedMappedNode(int type, Vertex src_node, Vertex tar_node, Vertex mapped_src_node,
                                        Vertex candidate_mapped_tar_node, int[][][] rids_closures) {                                                                 
                                        
        int midx1 = mapped_src_node.getRids_node_index();
        int midx2 = candidate_mapped_tar_node.getRids_node_index();
        int oidx1 = src_node.getRids_node_index();
        int oidx2 = tar_node.getRids_node_index();  
        
        switch (type) {
            case 0:  // sdd, --> nodes except control points
            case 1: if (rids_closures[midx1][midx2][0] > 0 || rids_closures[midx2][midx1][0] > 0) return true;
                    if (pattern.getPattern_vertices_edges()[oidx1][oidx2][2] > 0 && rids_closures[midx1][midx2][2] > 0 &&
                        hasSameSDD(src_node,tar_node,mapped_src_node,candidate_mapped_tar_node)) return true;
                    if (pattern.getPattern_vertices_edges()[oidx2][oidx1][2] > 0 && rids_closures[midx2][midx1][2] > 0 &&
                        hasSameSDD(tar_node,src_node,candidate_mapped_tar_node,mapped_src_node)) return true; 
                    return false;
            case 2: if (rids_closures[midx1][midx2][1] > 0 || rids_closures[midx2][midx1][1] > 0) return true;
                    return false;
        }
        return false;
    }
    
    private Vector findQualifiedMappedNodes(Vertex src_node, Vertex tar_node, Vertex mapped_src_node, Hashtable has_used,
                                            IDS ids,  int[][][] rids_closures, int type) {
        Vector qualified_mapped_nodes = new Vector();
        int rids_vertices_num = ids.getRids_vertices_num();
        for (int i=0; i<rids_vertices_num; i++) {
            Vertex candidate_tar_node = ids.getVertexByRidsIndex(i);  
 
            
            if (tar_node.getVertex_kind_id() == candidate_tar_node.getVertex_kind_id()) { // 2008-05-23
            
                if (!checkIntegerItemInHashtable(has_used,candidate_tar_node.getVertex_key_Integer())) {
                    if (this.qualifiedMappedNode(type,src_node,tar_node,mapped_src_node,candidate_tar_node,rids_closures)) {
                        switch (tar_node.getVertex_kind_id()) {
                            case  6: qualified_mapped_nodes.add(candidate_tar_node);
                                     break;
                            default: if (tar_node.getVertex_label() == candidate_tar_node.getVertex_label()) {
                                         qualified_mapped_nodes.add(candidate_tar_node);     
                                     }
                                     break;
                        }
                    }
                }
            }
            
        }
        return qualified_mapped_nodes;
    }


    private Vertex findMappingNode(Vertex src_node,Vertex tar_node, Vertex mapped_src_node, Hashtable has_used, Connection conn,
                                IDS ids, int[][][] rids_edges, int[][][] rids_closures, int type, int[] mapping) throws SQLException {
        Vector qualifiedMappedNodes = this.findQualifiedMappedNodes(src_node,tar_node,mapped_src_node,has_used,ids,rids_closures,type);
        Enumeration enum_qnodes = qualifiedMappedNodes.elements();
        Vertex candidate_mapped_tar_node = null;
        MissedInformation current_miss = new MissedInformation();
        
        int direction = 0;
        if (src_node.getStartline()<tar_node.getStartline()) direction = 1;
        if (src_node.getStartline()>tar_node.getStartline()) direction = -1;
        
        int current_direction = 0;        
        
        while (enum_qnodes.hasMoreElements()) {
                       
            Vertex qualifiedMappedNode = (Vertex) enum_qnodes.nextElement();
        
        
            int mapped_direction = 0;
            if (mapped_src_node.getStartline()<qualifiedMappedNode.getStartline()) mapped_direction = 1;
            if (mapped_src_node.getStartline()>qualifiedMappedNode.getStartline()) mapped_direction = -1;            
            int same_direction = mapped_direction * direction;        
        
            
            
            MissedInformation new_miss = new MissedInformation();
            switch (tar_node.getVertex_kind_id()) {
                case 1: 
                case 2: PatternCallsiteGraph pcsg = new PatternCallsiteGraph(tar_node.getVertex_key(),conn,pattern,false);
                        IdsCallsiteGraph icsg = new IdsCallsiteGraph(qualifiedMappedNode.getVertex_key(),conn,false,ids);
                        pcsg.findMissedInfo(ids,rids_edges,rids_closures,icsg,mapping,has_used,pattern,new_miss);
                        break;
                case 5: pcsg = new PatternCallsiteGraph(tar_node.getVertex_key(),conn,pattern,true);
                        icsg = new IdsCallsiteGraph(qualifiedMappedNode.getVertex_key(),conn,true,ids);
                        pcsg.findMissedInfo(ids,rids_edges,rids_closures,icsg,mapping,has_used,pattern,new_miss);
                        break;
               default: this.getMissedInfoByCP(tar_node,qualifiedMappedNode,mapping, pattern, rids_edges, rids_closures, has_used, ids,new_miss);
                        break;
            }
            if (candidate_mapped_tar_node == null) {
                candidate_mapped_tar_node = qualifiedMappedNode;
                current_miss = new_miss;
                current_direction = same_direction;
            } else {
                boolean change = selectBestTarMappedNode(src_node,tar_node,mapped_src_node,qualifiedMappedNode,candidate_mapped_tar_node,new_miss,
                                                         current_miss,rids_edges,rids_closures,ids,same_direction,current_direction);
                if (change) {
                    candidate_mapped_tar_node = qualifiedMappedNode;
                    current_miss = new_miss;
                    current_direction = same_direction;
                }
            }
        }
        return candidate_mapped_tar_node;
    }
      
    
    private boolean selectBestTarMappedNode(Vertex src_node, Vertex tar_node, Vertex mapped_src_node, Vertex qualified_tar_node, Vertex candidate_tar_node, 
                                            MissedInformation qualified_miss, MissedInformation candidate_miss, int[][][] rids_edges, int[][][] rids_closures, IDS ids,
                                            int same_direction, int current_direction) {
        if (qualified_miss.getMissedNode() > candidate_miss.getMissedNode()) return false;
        if (qualified_miss.getMissedNode() < candidate_miss.getMissedNode()) return true;
        if (qualified_miss.getMissedDataEdge() > candidate_miss.getMissedDataEdge()) return false;
        if (qualified_miss.getMissedDataEdge() < candidate_miss.getMissedDataEdge()) return true;
        if (qualified_miss.getMissedSddEdge() > candidate_miss.getMissedSddEdge()) return false;
        if (qualified_miss.getMissedSddEdge() < candidate_miss.getMissedSddEdge()) return true;
        if (qualified_miss.getMissedControlEdge() > candidate_miss.getMissedControlEdge()) return false;
        if (qualified_miss.getMissedControlEdge() < candidate_miss.getMissedControlEdge()) return true;
        
        if (mapped_src_node.getPdg_id() == candidate_tar_node.getPdg_id() && mapped_src_node.getPdg_id() != qualified_tar_node.getPdg_id()) return false;
        if (mapped_src_node.getPdg_id() != candidate_tar_node.getPdg_id() && mapped_src_node.getPdg_id() == qualified_tar_node.getPdg_id()) return true;
        
        int ms_idx = mapped_src_node.getRids_node_index();
        int qt_idx = qualified_tar_node.getRids_node_index();
        int ct_idx = candidate_tar_node.getRids_node_index();
        
        int qualified_tar_node_edges = 0;
        int qualified_tar_node_closures = 0;
        int candidate_tar_node_edges = 0;
        int candidate_tar_node_closures = 0;
  
        for (int i=0; i<ids.getRids_vertices_num(); i++) {
            for (int k=0; k<=2; k++) {
                if (rids_edges[i][qt_idx][k] > 0) qualified_tar_node_edges++;
                if (rids_edges[qt_idx][i][k] > 0) qualified_tar_node_edges++;
                if (rids_edges[i][ct_idx][k] > 0) candidate_tar_node_edges++;
                if (rids_edges[ct_idx][i][k] > 0) candidate_tar_node_edges++;
                if (rids_closures[i][qt_idx][k] > 0) qualified_tar_node_edges++;
                if (rids_closures[qt_idx][i][k] > 0) qualified_tar_node_edges++;
                if (rids_closures[i][ct_idx][k] > 0) candidate_tar_node_edges++;
                if (rids_closures[ct_idx][i][k] > 0) candidate_tar_node_edges++;     
            }
        }
        
        
        if (mapped_src_node.getPdg_id() == candidate_tar_node.getPdg_id() && mapped_src_node.getPdg_id() == qualified_tar_node.getPdg_id()) {
        
            if (src_node.getVertex_kind_id() == 6 || tar_node.getVertex_kind_id() ==6) { 
               if (same_direction > 0 && current_direction < 0) return true;
               if (current_direction > 0 && same_direction < 0) return false;          
            }
        
            int candidate_physical_distance = Math.abs(candidate_tar_node.getStartline()-mapped_src_node.getStartline());
            int qualified_physical_distance = Math.abs(qualified_tar_node.getStartline()-mapped_src_node.getStartline());
            if (candidate_physical_distance > (qualified_physical_distance + 20)) return true;
            if (qualified_physical_distance > (candidate_physical_distance + 20)) return false;
            int candidate_tranisitive_distance = rids_edges[ms_idx][ct_idx][0] + rids_edges[ms_idx][ct_idx][1] + rids_edges[ct_idx][ms_idx][0] + rids_edges[ct_idx][ms_idx][0];
            int qualified_transitive_distance = rids_edges[ms_idx][qt_idx][0] + rids_edges[ms_idx][qt_idx][1] + rids_edges[qt_idx][ms_idx][0] + rids_edges[qt_idx][ms_idx][0];
            if (candidate_tranisitive_distance > qualified_transitive_distance + 2) return true;
            if (candidate_tranisitive_distance < qualified_transitive_distance + 2 ) return false;
            if (candidate_tar_node.getDistance_to_candidate() > qualified_tar_node.getDistance_to_candidate() + 2) return true;
            if (qualified_tar_node.getDistance_to_candidate() < candidate_tar_node.getDistance_to_candidate() + 2) return false;
            if (candidate_tar_node_edges > qualified_tar_node_edges + 2) return false;
            if (qualified_tar_node_edges > candidate_tar_node_edges + 2) return true;
            if (candidate_tar_node_closures > qualified_tar_node_closures + 2) return false;
            if (qualified_tar_node_closures > candidate_tar_node_closures + 2) return true;
            
            if (candidate_tar_node.getVertex_kind_id() == 6) { //
                if (candidate_tar_node.getVertex_label() == tar_node.getVertex_label() && qualified_tar_node.getVertex_label() != tar_node.getVertex_label()) return true;
                if (candidate_tar_node.getVertex_label() != tar_node.getVertex_label() && qualified_tar_node.getVertex_label() == tar_node.getVertex_label()) return false;
            }
        } else {
            int candidate_tranisitive_distance = rids_edges[ms_idx][ct_idx][0] + rids_edges[ms_idx][ct_idx][1] + rids_edges[ct_idx][ms_idx][0] + rids_edges[ct_idx][ms_idx][0];
            int qualified_transitive_distance = rids_edges[ms_idx][qt_idx][0] + rids_edges[ms_idx][qt_idx][1] + rids_edges[qt_idx][ms_idx][0] + rids_edges[qt_idx][ms_idx][0];
            if (candidate_tranisitive_distance > qualified_transitive_distance + 2) return true;
            if (candidate_tranisitive_distance < qualified_transitive_distance + 2 ) return false;
            if (candidate_tar_node.getDistance_to_candidate() > qualified_tar_node.getDistance_to_candidate() + 2) return true;
            if (qualified_tar_node.getDistance_to_candidate() < candidate_tar_node.getDistance_to_candidate() + 2) return false;
            if (candidate_tar_node_edges > qualified_tar_node_edges + 2) return false;
            if (qualified_tar_node_edges > candidate_tar_node_edges + 2) return true;
            if (candidate_tar_node_closures > qualified_tar_node_closures + 2) return false;
            if (qualified_tar_node_closures > candidate_tar_node_closures + 2) return true;            
        }
        return false;       
    }
    
    
    private void getMissedInfoByCP(Vertex tar_node, Vertex mapped_tar_node, int[] mapping, Pattern pattern, int[][][] rids_edges,
                                   int[][][] rids_closures, Hashtable has_used, IDS ids, MissedInformation missed_info)  {
        this.getMissedEdgesByCP(tar_node,mapped_tar_node,mapping,pattern,rids_edges,ids,missed_info);
        this.getMissedClosuredByCP(tar_node,mapped_tar_node,mapping,has_used,pattern,rids_closures,ids,missed_info);
    }
    private void getMissedClosuredByCP(Vertex tar_node, Vertex mapped_tar_node, int[] mapping, Hashtable has_used,
                                  Pattern pattern, int[][][] rids_closures, IDS ids, MissedInformation missed_info) {
        int[][][] pattern_closures = pattern.getPatternClosures();
        int tar_node_index = tar_node.getNode_index();
        Hashtable tar_node_edges = new Hashtable();
        for (int i=0; i < pattern.getPattern_vertices_num(); i++) {
            Vertex adj_node = pattern.getVertexByIndex(i);
            if (adj_node.getVertex_key()!=adj_node.getVertex_key() && mapping[i] <=0) {
                for (int k=0; k<=2; k++) {
                    if (pattern_closures[tar_node_index][i][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(tar_node.getVertex_label(),k,true,this.getSddInfo(tar_node,adj_node));
                        else        edge = new MissedEdge(tar_node.getVertex_label(),k,true);
                        countEdgeNumber(tar_node_edges,edge);
                    }
                    if (pattern_closures[i][tar_node_index][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(tar_node.getVertex_label(),k,false,this.getSddInfo(tar_node,adj_node));
                        else        edge = new MissedEdge(tar_node.getVertex_label(),k,false);
                        countEdgeNumber(tar_node_edges,edge);                        
                    }                   
                }
            }
        }
        
        Hashtable mapped_tar_node_edges = new Hashtable();
        for (int i=0; i<ids.getRids_vertices_num(); i++) {
            int mapped_adj_key = ids.getRids_vertices_list()[i];
            Vertex mapped_adj_node = ids.getVertexByKeyFromRids(mapped_adj_key);
            Object obj = has_used.get(mapped_adj_node.getVertex_key_Integer());
            if (obj == null && mapped_adj_node.getVertex_key() != mapped_tar_node.getVertex_key()) {
                for (int k=0; k<=2; k++) {
                    if (rids_closures[mapped_tar_node.getRids_node_index()][i][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,true,this.getSddInfo(mapped_tar_node,mapped_adj_node));
                        else        edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,true);
                        countEdgeNumber(mapped_tar_node_edges,edge);
                    }
                    if (rids_closures[mapped_tar_node.getRids_node_index()][i][k] > 0) {
                        MissedEdge edge;
                        if (k == 2) edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,false,this.getSddInfo(mapped_tar_node,mapped_adj_node));
                        else        edge = new MissedEdge(mapped_tar_node.getVertex_label(),k,false);
                        countEdgeNumber(mapped_tar_node_edges,edge);
                    }
                }
            }
        }
        
        Enumeration ee = tar_node_edges.elements();
        while (ee.hasMoreElements()) {
            MissedEdge edge = (MissedEdge) ee.nextElement();
            MissedEdge mapped_edge = (MissedEdge) mapped_tar_node_edges.get(edge.getKey());
            int missed_num = 0;
            if (mapped_edge == null) {
                missed_num = edge.getEdgeNumber();
            } else {
                if (edge.getEdgeNumber() > mapped_edge.getEdgeNumber()) {
                    missed_num = edge.getEdgeNumber() - mapped_edge.getEdgeNumber();
                }
            }
            if (missed_num > 0) {
                switch (edge.getEdgetype()) {
                    case 0: missed_info.addMissedDataEdge(missed_num); break;
                    case 1: missed_info.addMissedControlEdge(missed_num); break;
                    case 2: missed_info.addMissedSddEdge(missed_num); break;
                }
            }
        }
        
        
        
    }
    
    public void countEdgeNumber(Hashtable ht, MissedEdge edge) {
        String key = edge.getKey();
        MissedEdge me = (MissedEdge) ht.get(key);
        if (me == null) {
            ht.put(key,edge);
        } else {
            me.increaseEdgeNumber();
        }
    }
    
    private void getMissedEdgesByCP(Vertex tar_node, Vertex mapped_tar_node, int[] mapping, 
                                    Pattern pattern, int[][][] rids_edges, IDS ids, MissedInformation missed_info) {
        int[][][] pattern_edges = pattern.getPattern_vertices_edges();
        int tar_node_index = tar_node.getNode_index();
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            Vertex adj_node = pattern.getVertexByIndex(i);
            if (adj_node.getVertex_key() != tar_node.getVertex_key()) {
                if (mapping[i] > 0) { // has been mapped
                    Vertex mapped_adj_node = ids.getVertexByKeyFromRids(mapping[i]);
                    for (int k=0; k<=2; k++) {
                        if (pattern_edges[tar_node_index][i][k] > 0) {
                            boolean match = true;
                            if (rids_edges[mapped_tar_node.getRids_node_index()][mapped_adj_node.getRids_node_index()][k] == 0) match = false;
                            if (k == 2) {
                                String sdd_info = this.getSddInfo(tar_node,adj_node);
                                String mapped_sdd_info = this.getSddInfo(mapped_tar_node,mapped_adj_node);
                                if (!sdd_info.equals(mapped_sdd_info)) match = false;
                            }
                            if (!match) {
                                switch (k) {
                                    case 0: missed_info.addMissedDataEdge(); break;
                                    case 1: missed_info.addMissedControlEdge(); break;
                                    case 2: missed_info.addMissedControlEdge(); break;
                                }
                            }
                        }
                        if (pattern_edges[i][tar_node_index][k] > 0) {
                            boolean match = true;
                            if (rids_edges[mapped_adj_node.getRids_node_index()][mapped_tar_node.getRids_node_index()][k] == 0) match = false;
                            if (k == 2) {
                                String sdd_info = this.getSddInfo(tar_node,adj_node);
                                String mapped_sdd_info = this.getSddInfo(mapped_tar_node,mapped_adj_node);
                                if (!sdd_info.equals(mapped_sdd_info)) match = false;
                            }
                            if (!match) {
                                switch (k) {
                                    case 0: missed_info.addMissedDataEdge(); break;
                                    case 1: missed_info.addMissedControlEdge(); break;
                                    case 2: missed_info.addMissedControlEdge(); break;
                                }
                            }
                        }                        
                        
                        
                    }
                }
            }
            
        }
    }
    
    public String getSddInfo(Vertex src_node, Vertex tar_node) {
        String f1 = src_node.getFieldname1();
        String f2 = tar_node.getFieldname1();
        if (f1.equals("") && f2.equals("")) return "";
        if (f1.equals(f2)) return "";
        if (f1.equals("") && !f2.equals("")) return "TAR" + f2;
        if (!f1.equals("") && f2.equals("")) return "SRC" + f1;
        return null;
    }        
    
    
    //////////////////////////////////////////////// 
    //////////////////////NOT FINISH YET
    ////////////////////////////////////////////////
    private int[][][] computeRidsClosures(IDS ids, int[][][] rids_edges) {
        int rids_node_num = ids.getRids_vertices_num();
        int[][][] rids_closures = new int[rids_node_num][rids_node_num][3];
        for (int i=0; i<rids_node_num; i++) {
            for (int j=0; j<rids_node_num; j++) {
                for (int k=0; k<=2; k++) {
                    rids_closures[i][j][k] = rids_edges[i][j][k];
                }
            }
        }
        
        for (int m=0; m<rids_node_num; m++) {
            for (int i=0; i<rids_node_num; i++) {
                for (int j=0; j<rids_node_num; j++) {
                    for (int b=0; b<rids_node_num; b++) {
                        if (i!=j && rids_closures[i][b][0] > 0 && rids_closures[b][j][0] > 0 && 
                            (rids_closures[i][j][0]==0 || (rids_closures[i][j][0] > (rids_closures[i][b][0] + rids_closures[b][j][0] + 1)))) {
                            rids_closures[i][j][0] = rids_closures[i][b][0] + rids_closures[b][j][0] + 1;
                        }
                        if (i!=j && rids_closures[i][b][1] > 0 && rids_closures[b][j][1] > 0 && 
                            (rids_closures[i][j][1]==0 || (rids_closures[i][j][1] > (rids_closures[i][b][1] + rids_closures[b][j][1] + 1)))) {
                            rids_closures[i][j][1] = rids_closures[i][b][1] + rids_closures[b][j][1] + 1;
                        }                       
                    }
                }                
            }
            
        }
        return rids_closures;
    }
    
    
    // Matching reulst is stored in IDS
    private void comparePattern(IDS ids, Connection conn) throws SQLException {
        int[][][] rids_edges = this.getRidsEdges(ids);
        int[][][] rids_closures = this.computeRidsClosures(ids,rids_edges);
        Hashtable has_used = new Hashtable();
        int[] mapping = new int[pattern.getPattern_vertices_num()]; 
        boolean[] has_mapped = new boolean[pattern.getPattern_vertices_num()];// nodes in the pattern has mapped to a mapped node
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
          mapping[i]=-1;
          has_mapped[i]=false;
        }    

        int[] match_queue = new int[pattern.getPattern_vertices_num()];
        int matched_num = 0;
        
        // Initialization
        int src_callsite_vertex_key = pattern.getPattern_vertices_list()[0];
        int mapped_callsite_vertex_key = ids.getRids_vertices_list()[0];
        matched_num = this.matchCallsiteGraph(ids,src_callsite_vertex_key,mapped_callsite_vertex_key,has_used,mapping,has_mapped,conn,match_queue,matched_num);
        int start_match_index = 0;
  //      int start_match_num = 0;
        int next_match_index = 0;
        do {
        //    start_match_num = matched_num;
            matched_num = this.matchPatternByType(start_match_index,0,matched_num,mapping,has_mapped,has_used,
                                                  ids,match_queue,rids_edges,rids_closures);
            next_match_index = matched_num;
            matched_num = this.matchPatternByType(start_match_index,1,matched_num,mapping,has_mapped,has_used,
                                                  ids,match_queue,rids_edges,rids_closures);
            matched_num = this.matchPatternByType(start_match_index,2,matched_num,mapping,has_mapped,has_used,
                                                  ids,match_queue,rids_edges,rids_closures);
            start_match_index = next_match_index;
        } while (start_match_index < matched_num);
        

        int lost_nodes = this.outputViolationNodes(mapping,pattern);
        int lost_edges = this.outputViolationEdges(mapping,pattern,ids,rids_closures);
        ids.setMatchingResult(mapping,lost_edges,lost_nodes);
    }
    

    private int outputViolationEdges(int[] mapping, Pattern pattern, IDS ids, int[][][] re) { 
        int lost_edges = 0;
        int[][][] pe = pattern.getPattern_vertices_edges();
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            for (int j=0; j<pattern.getPattern_vertices_num(); j++) {
                for (int k=0; k<3; k++) {     
                    if (pe[i][j][k]>0) {
                        int m_s_key = mapping[i]; // mapped source node key
                        int m_t_key = mapping[j]; // mapped source node key
                        if (m_s_key!=-1 && m_t_key!=-1) {
                            Vertex vs = ids.getVertexByKeyFromRids(m_s_key);
                            Vertex vt = ids.getVertexByKeyFromRids(m_t_key);
                            if (re[vs.getRids_node_index() ][vt.getRids_node_index()][k]<=0) {
                                lost_edges++;                    
                            }
                        } else {
                            lost_edges++;
                        }
                    }
                }
            }    
        }
        return lost_edges;
    }


    private int outputViolationNodes( int[] mapping, Pattern pattern) {
        int lost_nodes = 0;
        for (int i=0; i<pattern.getPattern_vertices_num(); i++) {
            if (mapping[i] == -1) {
                lost_nodes++;
            }
        }
        return lost_nodes;
    }

    private int matchPatternByType (int index, int type, int matched_num, int[] mapping, boolean[] has_mapped, Hashtable has_used, 
                                    IDS ids, int[] match_queue, int[][][] rids_edges, int[][][] rids_closures) throws SQLException  {
        while (index < matched_num) {
            int src_node_index = match_queue[index];
            Vertex src_node = pattern.getVertexByIndex(src_node_index);
            Vertex mapped_src_node = ids.getVertexByKeyFromRids(mapping[src_node_index]);     

            int[] neighbors = findNeighbors(src_node_index,type, mapping);
            for (int i=0; i<neighbors.length; i++) {
                Vertex tar_node = pattern.getVertexByIndex(neighbors[i]);

                if (mapping[neighbors[i]] <= 0) { // This node has not been mapped yet
                    Vertex mapped_tar_node = this.findMappingNode(src_node,tar_node,mapped_src_node,has_used,conn,ids,rids_edges,rids_closures,type,mapping);
                    if (mapped_tar_node != null) {
                        int mapped_node_kind_id = mapped_tar_node.getVertex_kind_id();
                        switch (mapped_node_kind_id) {
                            case 1 :
                            case 2 : matched_num = this.matchCallsiteGraphByParameterNode(ids,tar_node.getVertex_key(),mapped_tar_node.getVertex_key(),
                                                                                          has_used,mapping,has_mapped,conn,match_queue,matched_num);
                                     break;
                            case 5 : matched_num = this.matchCallsiteGraph(ids,tar_node.getVertex_key(),mapped_tar_node.getVertex_key(),has_used,mapping,
                                                                           has_mapped,conn,match_queue,matched_num);
                                     break;
                            default: has_used.put(mapped_tar_node.getVertex_key_Integer(),mapped_tar_node);
                                     mapping[tar_node.getRids_node_index()] = mapped_tar_node.getVertex_key();
                                     has_mapped[tar_node.getRids_node_index()] = true;
                                     /**
                                      * Quick and dirty fix for Bug 18
                                      */
                                     if(matched_num>=match_queue.length)
                                    	 break;
                                     match_queue[matched_num] = tar_node.getRids_node_index();
                                     matched_num++;
                                     System.out.println(src_node.getVertex_key()+":"+tar_node.getVertex_key()+"-"+
                                                        mapped_src_node.getVertex_key()+":"+mapped_tar_node.getVertex_key());
                                     break;
                        }
                    } else {
                        System.out.println(src_node.getVertex_key()+":"+tar_node.getVertex_key()+"-"+
                                           mapped_src_node.getVertex_key()+":");
                    }
                }
            }
            index++;
        }
        return matched_num;
    }

    private boolean isIntrestedNeighbor(Vertex neighbor_node, int type) {
        int neighbor_node_kind_id = neighbor_node.getVertex_kind_id();        
        switch (type) {
            case 0: if (neighbor_node_kind_id == 1 || neighbor_node_kind_id == 2) return true; // consider actual-in/out node
                    else return false;
            case 1: if (neighbor_node_kind_id == 6 || neighbor_node_kind_id == 21) return true;  // Consider only control point & switch node
                    else return false;
            // When control dependence dependences are used for expanding CP, the new nodes can't be control point nodes
            case 2: if (neighbor_node_kind_id == 6 || neighbor_node_kind_id == 21) return false;  
                    else return true;
        }
        return true;
    }
    
    private int[] findNeighbors(int src_node_index, int type, int[] mapping) {
        Vector neighbors = new Vector();
        int[][][] pattern_edges = pattern.getPattern_vertices_edges();
        int pattern_vertices_num = pattern.getPattern_vertices_num();
        int neighbor_num = 0;
        for (int i=0; i<pattern_vertices_num; i++) {
            Vertex vi = pattern.getVertexByIndex(i);
            if (mapping[i] <= 0 && i != src_node_index && this.isIntrestedNeighbor(vi,type)) { // The ith node in the pattern has not been mapped to a node in the IDS
                switch (type) {
                    case 0: 
                    case 1: if (pattern_edges[i][src_node_index][0] > 0 || pattern_edges[i][src_node_index][2] > 0 || 
                                pattern_edges[src_node_index][i][0] > 0 || pattern_edges[src_node_index][i][2] > 0) {
                                neighbors.add(new Integer(i)); 
                                neighbor_num++;
                            }
                            break;
                    case 2: if (pattern_edges[i][src_node_index][1] > 0 || pattern_edges[src_node_index][i][1] > 0) {
                                neighbors.add(new Integer(i));
                                neighbor_num++;
                            }
                            break;
                }
            }
        }
        
        int[] neighbor_list = new int[neighbor_num];
        Enumeration neighbor_enum = neighbors.elements();
        int i=0;
        while (neighbor_enum.hasMoreElements()) {
            Integer neighbor = (Integer) neighbor_enum.nextElement();
            neighbor_list[i++] = neighbor.intValue();
        }
        return neighbor_list;
        
    }
    
}
