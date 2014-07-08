package andy;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;

public class RIDSSetPhaseTwo {

    static int MAX_NODE_IN_PHASE2 = 3500;
    
    static String forward_sql = "select e.src_vertex_key current_node_vertex_key, e.src_pdg_id current_node_pdg_id,  " + 
                                "       e.tar_vertex_key new_node_vertex_key, e.tar_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type,  " + 
                                "       v.vertex_label new_node_vertex_label, v.startline new_node_startline " + 
                                "from   vertex v, edges e  " + 
                                "where  e.src_vertex_key = ?  " + 
                                "and    e.tar_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) ";
    static String backward_sql= "select e.tar_vertex_key current_node_vertex_key, e.tar_pdg_id current_node_pdg_id,  " + 
                                "       e.src_vertex_key new_node_vertex_key, e.src_pdg_id new_node_pdg_id,  " + 
                                "       v.vertex_kind_id new_node_vertex_kind_id, e.edge_type edge_type,  " + 
                                "       v.vertex_label new_node_vertex_label, v.startline new_node_startline " + 
                                "from   vertex v, edges e  " + 
                                "where  e.tar_vertex_key = ?  " + 
                                "and    e.src_vertex_key = v.vertex_key  " + 
                                "and    e.edge_type in (1,3) " ; 
                                
                                
    // Ignore interprocedural dependences                            
    static String control_sql   = "SELECT tar_vertex_key , tar_pdg_id, vertex_kind_id " +
                                     "FROM   edges e, vertex v " +
                                     "WHERE  e.src_vertex_key = ? " +
                                     "AND    e.tar_vertex_key = v.vertex_key " +
                                     "AND    e.edge_type in (2,4) ";
    static String mixed_sql   = "SELECT tar_vertex_key , tar_pdg_id, vertex_kind_id " +
                                     "FROM   edges e, vertex v " +
                                     "WHERE  e.src_vertex_key = ? " +
                                     "AND    e.tar_vertex_key = v.vertex_key ";                                   
                                     
    Connection conn;
    int gds_id;
    float frequency;
    Hashtable frq_label_hash = new Hashtable();
    int num_graph = 0;
    Hashtable sdd_labels = new Hashtable();
    int sdd_label_index = 3;
    boolean forward[] = {true,false};
    Hashtable cfg_vertices = new Hashtable();
    Hashtable ignore_frq_ai_node = new Hashtable();    
    
    /*****************Check time*************************/
	Stack<Long> time_stack = new Stack<Long>();
	int tab_count = 0;
	File log;
	BufferedWriter writer;
	boolean writeToFile = false;
	/*****************Check time ends********************/

    public RIDSSetPhaseTwo(int gds_id, Connection conn, float frq) throws SQLException {
    	/*****************check time: init writer****************************/
		if(this.writeToFile){
		try {
			log = new File("RIDSSetPhaseTwo_"+gds_id+".txt");
			writer = new BufferedWriter(new FileWriter(log));
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
		/*****************check time: init writer ends****************************/  
		
		
      this.conn = conn;
      this.gds_id = gds_id;
      this.frequency =frq;
      this.getGraphNumber();  // Get the number of graphs in the graph dataset
      if (this.num_graph < 4) return;
      this.loadFrq_label();  // Load Frequent Label
      this.loadIgnore_frq_ai_node();
      this.buildRidsSet();   // Create the set of Reduced Dependence Sphere
      
      
      
      /*****************check time: close writer****************************/
      if(this.writeToFile){
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
      }
	 /*****************check time: close writer ends****************************/
      
    }
        
    
    // The actual-in node' labels of some functions are not considered to be frequent lablel
    private void loadIgnore_frq_ai_node() throws SQLException {
      /* 20008-01-05
  /*      String sql = "SELECT * FROM ignore_frq_ai_node";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            Integer callsite_label = new Integer(rset.getInt(1));
            this.ignore_frq_ai_node.put(callsite_label, callsite_label);
        }
        rset.close();
        pstmt.close(); */
    
    
    }
    
    private void buildRidsSet() throws SQLException {
        /*********Print start**********/
    	print_start("buildRidsSet total:"+this.num_graph);
        /******************************/
       for (int gid=0; gid<this.num_graph; gid++) {
           buildRids(gid);
       }
       /*********Print end**********/
   	   print_end("buildRidsSet");
       /******************************/
    }
    
    
    // Build the control dependence between call-site node and parameter nodes
    private void buildControlDependenceForCallsiteGraph(Hashtable rids_vertices_hash, int[] rids_vertices_list, int rids_vertices_num) throws SQLException {
    	
        /*********Print start**********/
    	//print_start("buildControlDependenceForCallsiteGraph");
        /******************************/
    	
        String sql = "select v.*  " + 
                     "from   edges e, vertex v  " + 
                     "where  e.src_vertex_key = ?  " + 
                     "and    v.vertex_key = e.tar_vertex_key  " + 
                     "and    v.vertex_kind_id in (1,2)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i=0; i<rids_vertices_num; i++) {
            int src_vertex_key = rids_vertices_list[i];
            RidsVertex src_node = (RidsVertex) rids_vertices_hash.get(new Integer(src_vertex_key));
            /**
             * Bug-Fix of Bug-34
             */
            if(src_node == null)
                continue;
            if (src_node.getVertex_kind_id() == 5) {  // Call-site node
                pstmt.setInt(1,src_vertex_key);
                ResultSet rset = pstmt.executeQuery();
                while (rset.next()) {
                    int tar_vertex_key = rset.getInt("VERTEX_KEY");
                    RidsVertex tar_node = (RidsVertex) rids_vertices_hash.get(new Integer(tar_vertex_key));
                    if (tar_node != null) {
                        this.setEdges(src_node,tar_node,true,1,1);
                    } 
                    
                    /////////////////////////////// Solve actual-out parameter problem
                    
                    
                }
                rset.close();            
            }
        }
        pstmt.close();
        
        /*********Print end**********/
    	//print_end("buildControlDependenceForCallsiteGraph");
        /******************************/
    }
    
    private void buildRids(int gid) throws SQLException {
    	
        /*********Print start**********/
    	print_start("buildRids "+gid);
        /******************************/
    	
        Hashtable ids_vertices_hash = new Hashtable();
        int[] ids_vertices_list = new int[5000];
        
        Hashtable ids_pdg = this.getIdsPdg(gid); // The PDGs from which the IDS is derived
        int ids_vertices_num = this.loadIds(gid,ids_vertices_hash,ids_vertices_list);
        //2008-04-29
        ids_vertices_num = this.addGlobalNodes(ids_vertices_num,ids_vertices_hash,ids_vertices_list,ids_pdg);
    
        /* 2007-11-28 */    
    //    this.buildControlDependence(ids_vertices_hash,ids_vertices_list,ids_vertices_num,ids_pdg);
           
        this.buildSDDEdge(ids_vertices_num,ids_vertices_list,ids_vertices_hash);
        this.findRidsNodesAndEdges(gid,ids_vertices_num,ids_vertices_hash,ids_vertices_list,ids_pdg);
        
        /*********Print end**********/
    	print_end("buildRids");
        /******************************/
    }
    
    // Check whether the edge is used for building edge in RIDS
    private boolean hasQualifiedLinkEdge(int type, Edge link_edge) {
        /*********Print start**********/
    	//print_start("hasQualifiedLinkEdge");
        /******************************/    	
    	
      switch (type) {
          case 0: if (link_edge.getEdgeInfo(0)!=0 || link_edge.getEdgeInfo(2)!= 0) return true; break;
          case 1: if (link_edge.getEdgeInfo(1)!=0) return true; break;
          case 4: if (link_edge.getEdgeInfo(0)!=0 || link_edge.getEdgeInfo(1)!=0 || link_edge.getEdgeInfo(2)!=0) return true; break;
      }
      /*********Print end**********/
  	  //print_end("hasQualifiedLinkEdge");
      /******************************/ 
      return false;
    }
    
    private boolean isLoopPath(IdsVertex src_node_in_ids, IdsVertex current_node_in_ids, IdsVertex next_node_in_ids, 
                               PdgForPhaseOne src_node_pdg) {
       if (current_node_in_ids.getPdg_id() != next_node_in_ids.getPdg_id()) {
           if (src_node_in_ids.getPdg_id() == next_node_in_ids.getPdg_id()) {
               if (src_node_pdg.getP_pdg_id() == current_node_in_ids.getPdg_id()) {
                   return true;
               }
           }
       }
       return false;
    }
    
    private int getTransitiveEdgeDistance(int type, Edge link_edge) {
        int d_len = link_edge.getEdgeInfo(0) + link_edge.getEdgeInfo(2);
        int control_len = link_edge.getEdgeInfo(1);
        switch (type) {
            case 0: return d_len;
            case 1: return control_len;
            case 4: if (d_len == 0 && control_len !=0) return control_len;
                    if (d_len != 0 && control_len == 0) return d_len;
                    if (d_len > control_len) return control_len;
                    else                     return d_len;
        }
        return 0;
    }    
    
        
    private void buildRidsEdges(Hashtable ids_vertices_hash, Hashtable rids_vertices_hash, RidsVertex src_node_in_rids,
                                       int type, PdgForPhaseOne src_node_pdg) {
        /*********Print start**********/
    	//print_start("buildRidsEdges");
        /******************************/         

        int[][] cd_node_list = new int[10000][2];
        boolean[] inter_path = new boolean[10000];
        for (int i=0; i<inter_path.length; i++) inter_path[i]=false;
        int cd_node_num = 0;
        Hashtable cd_node_hash = new Hashtable();
        Hashtable cd_node_hash_temp = new Hashtable();
              
        /**
         * Bug-Fix of Bug-34
         */
        if(src_node_in_rids == null)
            return;
      // Get the node in the sphere which contain data dependence information 
        IdsVertex src_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(src_node_in_rids.getVertex_key()));
        /**
         * Bug Fix for Bug-34
         */
        if(src_node_in_ids == null)
            return;
        cd_node_list[cd_node_num][0] = src_node_in_ids.getVertex_key();
        cd_node_list[cd_node_num][1] = 0;
        cd_node_hash.put(new Integer(src_node_in_rids.getVertex_key()),src_node_in_ids);
        cd_node_hash_temp.put(new Integer(src_node_in_rids.getVertex_key()),new Integer(cd_node_num));
        cd_node_num++;
        int index = 0;
      
        while (index < cd_node_num) {    
            int current_node_vertex_key = cd_node_list[index][0];
            IdsVertex current_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(current_node_vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(current_node_in_ids == null){
                index++;
                continue;
            }
            Enumeration ee = current_node_in_ids.getTargetNodes().elements(); // The edges incident from current node in IDS
        
            while (ee.hasMoreElements()) {
                Edge link_edge = (Edge) ee.nextElement();
                if (this.hasQualifiedLinkEdge(type,link_edge)) { // determine whether link_edge is a qualified edge
                    int next_node_vertex_key = link_edge.getTar_vertex_key();
                   
                    IdsVertex next_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(next_node_vertex_key));
                    /**
                     * Bug Fix for Bug-34
                     */
                    if(next_node_in_ids == null)
                        continue;
                    if (!this.isLoopPath(src_node_in_ids,current_node_in_ids,next_node_in_ids,src_node_pdg)) {
                        if (!next_node_in_ids.isIds_frq_label()) { // next_node_in_sphere is not a node with frequent label
                            IdsVertex next_node = (IdsVertex) cd_node_hash.get(new Integer(next_node_vertex_key));
                            if (next_node == null) {             
                                cd_node_hash.put(new Integer(next_node_in_ids.getVertex_key()),next_node_in_ids);
                                cd_node_hash_temp.put(new Integer(next_node_in_ids.getVertex_key()), new Integer(cd_node_num));
                                cd_node_list[cd_node_num][0] = next_node_in_ids.getVertex_key();
                                cd_node_list[cd_node_num][1] = cd_node_list[index][1] + this.getTransitiveEdgeDistance(type,link_edge);
                                if (inter_path[index] || current_node_in_ids.getPdg_id() != next_node_in_ids.getPdg_id()) inter_path[cd_node_num] =true;
                                cd_node_num++;
                            } else{
                                int newlength = cd_node_list[index][1] + this.getTransitiveEdgeDistance(type,link_edge);
                                int cd_node_index = ((Integer) cd_node_hash_temp.get(new Integer(next_node.getVertex_key()))).intValue();
                                int original_len = cd_node_list[cd_node_index][1];
                                if (newlength<original_len) {
                                    cd_node_list[cd_node_index][1] = newlength;   
                                    if (inter_path[index] || current_node_in_ids.getPdg_id() != next_node_in_ids.getPdg_id()) {
                                        inter_path[cd_node_index] = true;
                                    } else {
                                        inter_path[cd_node_index] = false;
                                    }
                                }
                            }
                        } else {  
                            if (isIntrestedEdge(src_node_in_ids,next_node_in_ids,inter_path[index],type)) {
                        
                                Hashtable neighbors = src_node_in_rids.getTargetNodes();               
                                Edge new_edge = (Edge)neighbors.get(new Integer(next_node_in_ids.getVertex_key()));
                            
                                int new_len = cd_node_list[index][1] + getTransitiveEdgeDistance(type,link_edge);
                                if (this.isQualifiedTransitiveEdge(new_edge,type,new_len)) {
                                    RidsVertex next_node_in_rids = (RidsVertex) rids_vertices_hash.get(new Integer(next_node_in_ids.getVertex_key()));   
                                    /**
                                     * Bug-Fix for Bug-34
                                     */
                                    if(next_node_in_ids != null)
                                        this.setEdges(src_node_in_rids,next_node_in_rids,true,type,new_len);  
                                }
                            }
                        }
                    }
                }
            }        
            index++;
        }
        
        /*********Print end*********/
    	//print_end("buildRidsEdges");
        /******************************/ 
    }        
    
    private boolean isIntrestedEdge(IdsVertex src_node_in_ids, IdsVertex next_node_in_ids, boolean path_type, int type) {
        boolean same_pdg = (src_node_in_ids.getPdg_id() == next_node_in_ids.getPdg_id());
        boolean interpath = (path_type || (!same_pdg));
        int sn_id = src_node_in_ids.getVertex_kind_id();
        int nn_id = next_node_in_ids.getVertex_kind_id(); 
        if (type == 0) {
            ///////////////////// Actual-in/////////////////////////////        
            if (sn_id == 1 && (nn_id == 1 || nn_id == 6 || nn_id == 21)) {  // AI --> AI
                if (!same_pdg) return true; // In different PDGs
                if (!interpath) return false; // in same pdg but intradependence
                // in the same pdg with interprocedure dependence
                if (src_node_in_ids.getStartline()>=next_node_in_ids.getStartline()) return false;
                if (hasSharedDataDependence(src_node_in_ids,next_node_in_ids)) return true;
                else                                                           return false;
            }
            if (sn_id == 1 && (nn_id == 2 || nn_id == 5)) return false; // AI --> AO || CS
            
            /////////////////////Actual-out///////////////////////////
            if (sn_id == 2 && (nn_id == 2 || nn_id == 5)) return false;
        }
        return true;
        
        
  /*      if (type == 0 && path_type && src_node_in_ids.getPdg_id() == next_node_in_ids.getPdg_id()) {  // Data dependence + inter-path
            if (src_node_in_ids.getStartline()>next_node_in_ids.getStartline()) {
                return false;
            }
        }
        
    --    if (type == 0 && src_node_in_ids.getVertex_kind_id() == 1 && next_node_in_ids.getVertex_kind_id() == 5) return false;
        
        // Ignore the path from actual-in --> actual-out
    --    if (type == 0 && src_node_in_ids.getVertex_kind_id() == 1 && next_node_in_ids.getVertex_kind_id() == 2) return false;
        
        if (type == 0 && src_node_in_ids.getVertex_kind_id() == 1 && next_node_in_ids.getVertex_kind_id() == 6 && // from actual-in --> control -point
            src_node_in_ids.getPdg_id() == next_node_in_ids.getPdg_id() && path_type) {
            if (!hasSharedDataDependence(src_node_in_ids,next_node_in_ids)) {
                return false;
            }
        }
        return true; */
        
    }
    
    private boolean hasSharedDataDependence(IdsVertex src_node_in_ids, IdsVertex next_node_in_ids) {
    	
        /*********Print start**********/
 	    //print_start("hasSharedDataDependence");
        /******************************/ 
    	
        String sql = "select v.* from  edges e, vertex v  " + 
                     "where e.tar_vertex_key = ? " + 
                     "and   e.src_vertex_key = v.vertex_key " + 
                     "and   edge_type = 1  " + 
                     "intersect  " + 
                     "select v.*  " + 
                     "from  edges e, vertex v  " + 
                     "where e.tar_vertex_key = ? " + 
                     "and   e.src_vertex_key = v.vertex_key  " + 
                     "and   edge_type = 1";
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,src_node_in_ids.getVertex_key());
            pstmt.setInt(2,next_node_in_ids.getVertex_key());
            rset = pstmt.executeQuery();
            if (rset.next()) return true;
            else return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
                rset.close();
            } catch (SQLException ie) {ie.printStackTrace();}
        }
        
        /*********Print end**********/
 	    //print_end("hasSharedDataDependence");
        /******************************/ 
        
        return true;
    }
    
    private boolean isQualifiedTransitiveEdge(Edge new_edge, int type, int new_len) {
        if (new_edge == null) return true;
        switch (type) {
            case 0: if (new_edge.getEdgeInfo(0) == 0) return true;
                    if (new_edge.getEdgeInfo(0)>new_len) return true;
                    break;
            case 1: if (new_edge.getEdgeInfo(1) == 0) return true;
                    if (new_edge.getEdgeInfo(1)>new_len) return true;
                    break;
            case 4: if (new_edge.getEdgeInfo(0) == 0 && new_edge.getEdgeInfo(1) == 0 && new_edge.getEdgeInfo(4) == 0) return true;
                    if (new_edge.getEdgeInfo(0) == 0 && new_edge.getEdgeInfo(1) == 0 && new_edge.getEdgeInfo(4) > new_len) return true;
                    break;
        }
        return false;
    }    
    
    private void buildRidsData_SDD_Edges(Hashtable ids_vertices_hash, int rids_vertices_num, 
                                               Hashtable rids_vertices_hash, int[] rids_vertices_list, Hashtable ids_pdg) {
        /*********Print start**********/
    	//print_start("buildRidsData_SDD_Edges");
        /******************************/ 
    	
        // transitive data dependences
        for (int i=0; i<rids_vertices_num; i++) {  
             RidsVertex src_node = (RidsVertex) rids_vertices_hash.get(new Integer(rids_vertices_list[i]));
             /**
              * Bug Fix for Bug-34
              */
             if(src_node == null)
                 continue;
             PdgForPhaseOne src_node_pdg = (PdgForPhaseOne) ids_pdg.get(src_node.getPdg_id_key());
             this.buildRidsEdges(ids_vertices_hash,rids_vertices_hash,src_node,0,src_node_pdg);
            
        }
  
        
        //preserve shared data dependence edge
        for (int i=0; i<rids_vertices_num; i++) {
           RidsVertex src_node = (RidsVertex) rids_vertices_hash.get(new Integer(rids_vertices_list[i]));
           buildRidsSDDEdges(rids_vertices_hash, src_node, ids_vertices_hash);
        }          
        
        /*********Print end**********/
    	//print_end("buildRidsData_SDD_Edges");
        /******************************/ 
    }
    
    private void buildRidsSDDEdges(Hashtable rids_vertices_hash, RidsVertex src_node_in_rids, Hashtable ids_vertices_hash) {
    	
        /*********Print start**********/
    	//print_start("buildRidsSDDEdges");
        /******************************/ 
        /**
         * Bug Fix for Bug-34
         */
        if(src_node_in_rids == null)
            return;
        IdsVertex src_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(src_node_in_rids.getVertex_key())); 
        /**
         * Bug Fix for Bug-34
         */
        if(src_node_in_ids == null)
            return;
        Enumeration edge_ee = src_node_in_ids.getTargetNodes().elements(); 
        while (edge_ee.hasMoreElements()) {
            Edge edge = (Edge) edge_ee.nextElement();
            if (edge.getEdgeInfo(5)!=0) {  //SDD
                int tar_vertex_key = edge.getTar_vertex_key();
                RidsVertex tar_node_in_rids = (RidsVertex) rids_vertices_hash.get(new Integer(tar_vertex_key));
                if (tar_node_in_rids != null) {
                    this.setEdges(src_node_in_rids,tar_node_in_rids,true,5,edge.getEdgeInfo(5));                 
                }
            }
        }
        
        /*********Print end**********/
    	//print_end("buildRidsSDDEdges");
        /******************************/         
    }
         
    
    private int findRidsNodes(int ids_vertices_num, int[] ids_vertices_list, Hashtable ids_vertices_hash,
                              int[] rids_vertices_list, Hashtable rids_vertices_hash) {
    	
        /*********Print start**********/
    	//print_start("findRidsNodes");
        /******************************/ 
    	
        int rids_vertices_num = 0;
        for (int i=0; i<ids_vertices_num; i++) {
            int vertex_key = ids_vertices_list[i];
            IdsVertex sv = (IdsVertex) ids_vertices_hash.get(new Integer(vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(sv == null)
                continue;
            if (sv.isIds_frq_label()) {
                rids_vertices_list[rids_vertices_num] = vertex_key;
                rids_vertices_hash.put(new Integer(vertex_key),new RidsVertex(sv,rids_vertices_num));
                rids_vertices_num++;
            }
        }       
        
        /*********Print end**********/
    	//print_end("findRidsNodes");
        /******************************/ 
        
        return rids_vertices_num;
    }
    
    private void findRidsNodesAndEdges(int gid, int ids_vertices_num, Hashtable ids_vertices_hash, int[] ids_vertices_list, Hashtable ids_pdg) throws SQLException {
        ///////////////////////////////////////////////////// Compute Data dependences and SDD
        
        /*********Print start**********/
    	print_start("findRidsNodesAndEdges");
        /******************************/ 
    	
    	// The nodes in the RIDS are stored in rids_vertices_hash_temp0 and rids_vertices_list_temp0
        Hashtable rids_vertices_hash_temp0 = new Hashtable();
        int[] rids_vertices_list_temp0 = new int[2000];
        int rids_vertices_num_temp0 = this.findRidsNodes(ids_vertices_num,ids_vertices_list,ids_vertices_hash,rids_vertices_list_temp0,rids_vertices_hash_temp0);    
        this.buildRidsData_SDD_Edges(ids_vertices_hash,rids_vertices_num_temp0,rids_vertices_hash_temp0,
                                     rids_vertices_list_temp0,ids_pdg);
        this.buildControlDependenceForCallsiteGraph(rids_vertices_hash_temp0,rids_vertices_list_temp0,rids_vertices_num_temp0);
       
        // find the maximal connected graph containing the candidate node
        Hashtable rids_vertices_hash = new Hashtable();
        int[] rids_vertices_list = new int[2000];
        int rids_vertices_num = 0;
        RidsVertex rsv = (RidsVertex) rids_vertices_hash_temp0.get(new Integer(rids_vertices_list_temp0[rids_vertices_num]));
        
        
        /**
         * Bug Fix for Bug-34
         * The program will mysteriously end and quit!       
         */
        if(rsv == null)
            return;
        rids_vertices_hash.put(new Integer(rids_vertices_list_temp0[rids_vertices_num]),rsv);
        rsv.setRids_index(rids_vertices_num);
        
        rids_vertices_list[rids_vertices_num] = rids_vertices_list_temp0[0];
        rids_vertices_num++;
        
        int index = 0;
        while (index < rids_vertices_num) {
            int current_vertex_key = rids_vertices_list[index];
            RidsVertex current_node = (RidsVertex) rids_vertices_hash.get(new Integer(current_vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(current_node == null){
                index++;
                continue;
            }
            for (int i=0; i<2; i++) {  //0: forward, 1: backward
                Enumeration ee;
                if (i == 0) {
                    ee = current_node.getTargetNodes().elements();
                } else {
                    ee = current_node.getSourceNodes().elements(); 
                }
                while (ee.hasMoreElements()) {
                    Edge link_edge = (Edge) ee.nextElement();
                    int next_node_vertex_key;
                    if (i == 0) {
                        next_node_vertex_key = link_edge.getTar_vertex_key();
                    } else {
                        next_node_vertex_key = link_edge.getSrc_veretx_key();
                    }
                    RidsVertex next_node = (RidsVertex) rids_vertices_hash.get(new Integer(next_node_vertex_key));
                    if (next_node == null) {
                        next_node = (RidsVertex) rids_vertices_hash_temp0.get(new Integer(next_node_vertex_key));
                        if (next_node != null) { // ensure the next node is a node with frequent label or a control point
                            rids_vertices_list[rids_vertices_num] = next_node_vertex_key;
                            next_node.setRids_index(rids_vertices_num);
                            rids_vertices_hash.put(new Integer(next_node_vertex_key),next_node);
                            rids_vertices_num++;
                        }
                    }                
                }
            }
            index++;        
        }
        ///////// build control or mixed dependence 
        buildControlDependenceForRids(rids_vertices_hash, rids_vertices_list, rids_vertices_num, ids_pdg,ids_vertices_hash);
        removeRidsUselessEdge(rids_vertices_hash, rids_vertices_list, rids_vertices_num, gid);
        /*********Print end**********/
    	print_end("findRidsNodesAndEdges");
        /******************************/ 
    }
    
    private void removeRidsUselessEdge(Hashtable rids_vertices_hash, int[] rids_vertices_list, int rids_vertices_num, int gid) throws SQLException{
    	
        /*********Print start**********/
    	//print_start("removeRidsUselessEdge");
        /******************************/ 
    	
        int[][][] rids_edges = new int[rids_vertices_num][rids_vertices_num][3];
        for (int i=0; i<rids_vertices_num; i++) {
            for (int j=0; j<rids_vertices_num; j++) {
                for (int k=0; k<3;k++) {
                    rids_edges[i][j][k]=0;
                }
            }
        }
        
        for (int i=0; i<rids_vertices_num; i++) {
            int src_node_vertex_key = rids_vertices_list[i];
            RidsVertex src_node = (RidsVertex) rids_vertices_hash.get(new Integer(src_node_vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(src_node == null)
                continue;
            Enumeration neighbors = src_node.getTargetNodes().elements();
            while (neighbors.hasMoreElements()) {
                Edge edge = (Edge) neighbors.nextElement();
                int next_node_vertex_key = edge.getTar_vertex_key();
                RidsVertex next_node = (RidsVertex) rids_vertices_hash.get(new Integer(next_node_vertex_key));
                /**
                 * Bug Fix for Bug-34
                 */
                if(next_node == null)
                    continue;
                int src_node_idx = src_node.getRids_index();
                int next_node_idx = next_node.getRids_index();
                rids_edges[src_node_idx][next_node_idx][0] = edge.getEdgeInfo(0);
                rids_edges[src_node_idx][next_node_idx][1] = edge.getEdgeInfo(1);
                rids_edges[src_node_idx][next_node_idx][2] = edge.getEdgeInfo(5);
            }
        }
        
        
        boolean[] removed_nodes = new boolean[rids_vertices_num];
        for (int i=0; i<rids_vertices_num; i++) removed_nodes[i] = false;
        
        
        boolean keep = true;
        for (int kk=0; kk<rids_vertices_num && keep; kk++) {
        
            keep = false;
            
            for (int i=0; i<rids_vertices_num; i++) {
            
                int src_node_vertex_key = rids_vertices_list[i];
                RidsVertex src_node = (RidsVertex) rids_vertices_hash.get(new Integer(src_node_vertex_key));
                /**
                 * Bug Fix for Bug-34
                 */
                if(src_node == null)
                    continue;
                int src_node_idx = src_node.getRids_index();
                
                if (src_node.getVertex_kind_id()==6 && !removed_nodes[src_node_idx]) { // Control point && not removed yet
                
                    // Check whether there is at least one edge incident from the control point
                    boolean flag = true;
                    for (int j=0; j<rids_vertices_num && flag; j++) {
                        int total_edge = rids_edges[i][j][0]+rids_edges[i][j][1]+rids_edges[i][j][2];
                        if (total_edge>0) flag = false;                        
                        if (rids_edges[j][i][0] !=0) {
                            RidsVertex tar_node_rids = (RidsVertex) rids_vertices_hash.get(new Integer(rids_vertices_list[j]));
                            /**
                             * Bug Fix for Bug-34
                             */
                            if(tar_node_rids == null)
                                continue;
                            if (tar_node_rids.getVertex_kind_id()==2) flag = false; //Actual out 
                        }
                    }
                    
                    
                    if (flag) { // no edges are incident from the control point
                        for (int m=0; m<rids_vertices_num; m++) {
                            rids_edges[m][src_node_idx][0] = 0;
                            rids_edges[m][src_node_idx][1] = 0;
                            rids_edges[m][src_node_idx][2] = 0;
                        }
                        keep = true;
                        removed_nodes[src_node_idx] = true;
                    }
                }
            }
        }
        
        int[] rids_node_new_indices = new int[rids_vertices_num];
        int index = 0;
        for (int i=0; i<rids_vertices_num; i++) {
            if (removed_nodes[i]) {
                rids_node_new_indices[i] = -1;
            } else {
                rids_node_new_indices[i] = index++;
            }
        }
        
        String sql = "INSERT INTO RIDS_NODES VALUES(?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i=0; i<rids_vertices_num; i++) {
            if (!removed_nodes[i]) {
                RidsVertex rv = (RidsVertex)rids_vertices_hash.get(new Integer(rids_vertices_list[i]));
                /**
                 * Bug Fix for Bug-34
                 */
                if(rv == null)
                    continue;
                pstmt.setInt(1,this.gds_id);
                pstmt.setInt(2,gid);
                pstmt.setInt(3,rids_node_new_indices[i]);
                pstmt.setInt(4,rids_vertices_list[i]);
                pstmt.setInt(5,rv.getDistance_to_candidate());
                pstmt.execute();
            }
        }
        pstmt.close();        
        
        sql = "INSERT INTO RIDS_EDGES VALUES(?,?,?,?,?,?,?,?,?,?)";
        pstmt = conn.prepareStatement(sql);
        for (int i=0; i<rids_vertices_num; i++) {
            if (!removed_nodes[i]) {  
                for (int j=0; j<rids_vertices_num; j++) {
                    if (!removed_nodes[j]) {
                        int total_edges = rids_edges[i][j][0] + rids_edges[i][j][1] + rids_edges[i][j][2];
                        if (total_edges > 0) {     
                            pstmt.setInt(1,gds_id);
                            pstmt.setInt(2,gid);
                            pstmt.setInt(3,rids_vertices_list[i]);
                            pstmt.setInt(4,rids_node_new_indices[i]);
                            pstmt.setInt(5,rids_vertices_list[j]);                    
                            pstmt.setInt(6,rids_node_new_indices[j]);
                            pstmt.setInt(7,rids_edges[i][j][0]);
                            pstmt.setInt(8,rids_edges[i][j][1]);
                            pstmt.setInt(9,0);
                            pstmt.setInt(10,rids_edges[i][j][2]);
                            pstmt.execute();
                        }
                    }
                }
            }   
        }      
        
        /*********Print end**********/
    	//print_end("removeRidsUselessEdge");
        /******************************/ 
        
    }
    
    
    private void buildMixedDependenceForRids(Hashtable rids_vertices_hash, int[] rids_vertices_list, int rids_vertices_num, Hashtable ids_pdg,
                                             Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
    	//print_start("buildMixedDependenceForRids");
        /******************************/ 
    	
        for (int i=0; i<rids_vertices_num; i++) {
            int src_node_vertex_key = rids_vertices_list[i];
            RidsVertex src_node_in_rids = (RidsVertex) rids_vertices_hash.get(new Integer(src_node_vertex_key));
            buildRidsControl_Mixed_ForNode(src_node_in_rids,rids_vertices_hash,ids_pdg,false,ids_vertices_hash);  
        }
        
        /*********Print end**********/
    	//print_end("buildMixedDependenceForRids");
        /******************************/ 
    }
    
  
    
    private void buildControlDependenceForRids(Hashtable rids_vertices_hash, int[] rids_vertices_list, int rids_vertices_num, 
                                               Hashtable ids_pdg, Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
    	//print_start("buildControlDependenceForRids");
        /******************************/ 
    	
        for (int i=0; i<rids_vertices_num; i++) {
            int src_node_vertex_key = rids_vertices_list[i];
            RidsVertex src_node_in_rids = (RidsVertex) rids_vertices_hash.get(new Integer(src_node_vertex_key));
            buildRidsControl_Mixed_ForNode(src_node_in_rids,rids_vertices_hash, ids_pdg,true,ids_vertices_hash);
        }
        
        /*********Print end**********/
    	//print_end("buildControlDependenceForRids");
        /******************************/ 
    }
     
    private void buildRidsControl_Mixed_ForNode(RidsVertex src_node_in_rids, Hashtable rids_vertices_hash, Hashtable ids_pdg, 
                                                boolean control, Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
    	//print_start("buildRidsControl_Mixed_ForNode");
        /******************************/ 

        String sql = "";
        if (control) sql = this.control_sql;
        else         sql = this.mixed_sql;

        int[][] cd_node_list = new int[10000][2];
        int cd_node_num = 0;
        Hashtable cd_node_hash = new Hashtable();
        
        /**
         * Bug Fix for Bug-34
         */
        if(src_node_in_rids == null)
            return;
        cd_node_list[cd_node_num][0] = src_node_in_rids.getVertex_key();
        cd_node_list[cd_node_num][1] = 0; //distance
        cd_node_hash.put(new Integer(src_node_in_rids.getVertex_key()),new Integer(cd_node_num));
        cd_node_num++;
        
        int index = 0;
        
        while (index < cd_node_num && index < 5000) {    
        
            int current_node_vertex_key = cd_node_list[index][0];
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            
            while (rset.next() && index < 5000) { 
            
                int next_node_vertex_key = rset.getInt("TAR_VERTEX_KEY");
                int next_node_pdg_id = rset.getInt("TAR_PDG_ID");
                int id = rset.getInt("VERTEX_KIND_ID");
                RidsVertex next_node_in_rids = (RidsVertex) rids_vertices_hash.get(new Integer(next_node_vertex_key));

                if (next_node_in_rids == null) {  // It is not a node in the IDS
                    if (isInvolvedPdg(ids_pdg,next_node_pdg_id)) { // check whether the pdg of the next node is included in the ids_pdg
                        Object obj = cd_node_hash.get(new Integer(next_node_vertex_key));
                        if (obj == null) {
                            boolean flag = false;
                          
                            if ((id >17 || id < 13) && id !=3) { // not global(formal)-actual-in/out & auxiliary node 
                                if (!control && id == 5) flag = false;
                                else                     flag = true;
                            } else {
                                if (!control) { // mixed dependence
                                    Object obj2 = ids_vertices_hash.get(new Integer(next_node_vertex_key));
                                    if (obj2 != null) flag = true;
                                }
                            }
                            
                            if (flag) {
                                cd_node_list[cd_node_num][0] = next_node_vertex_key;
                                cd_node_list[cd_node_num][1] = cd_node_list[index][1]+1;
                                cd_node_hash.put(new Integer(next_node_vertex_key),new Integer(cd_node_num));
                                cd_node_num++;
                            }
                        } 
                    }
                } else { // Add a control dependence from src_node to next_node
                    if (src_node_in_rids.getVertex_key()!=next_node_in_rids.getVertex_key()) {
                        Edge edge = (Edge) src_node_in_rids.getTargetNodes().get(new Integer(next_node_in_rids.getVertex_key()));
                        if (control) {
                            if (edge == null || edge.getEdgeInfo(1) == 0 || edge.getEdgeInfo(1) > (cd_node_list[index][1]+1)) {
                                this.setEdges(src_node_in_rids,next_node_in_rids,true,1,cd_node_list[index][1]+1);
                            }
                        } else {
                            if (edge == null || edge.suitableForAddingMixedEdge(cd_node_list[index][1]+1)) {
                                this.setEdges(src_node_in_rids,next_node_in_rids,true,4,cd_node_list[index][1]+1);
                            }
                        }
                    }
                }
            }            
            rset.close();
            pstmt.close();
            index++;
        }
        
        /*********Print end**********/
    	//print_end("buildRidsControl_Mixed_ForNode");
        /******************************/ 
    }
    
   
     
    // Find the actual-in nodes and control nodes that are data dependent on sdd_node
    private void findNodesForSDDEdge(IdsVertex sdd_node, Vector actual_in_neighbors, 
                                     Vector control_neighbors, Vector expression_neighbors, Hashtable ids_vertices_hash) {
    	
        /*********Print start**********/
    	//print_start("findNodesForSDDEdge");
        /******************************/ 
        /**
         * Bug Fix for Bug-34
         */
        if(sdd_node == null)
            return;
        Enumeration target_enum = sdd_node.getTargetNodes().elements();
        while (target_enum.hasMoreElements())  {
            Edge edge = (Edge) target_enum.nextElement();
            if (edge.getEdgeInfo(0)!=0) {
                int next_node_vertex_key = edge.getTar_vertex_key();
                IdsVertex next_node = (IdsVertex) ids_vertices_hash.get(new Integer(next_node_vertex_key));
                /**
                 * Bug Fix for Bug-34
                 */
                if(next_node == null)
                    continue;
                if (next_node.getVertex_kind_id() == 1) { // actual-in 
                    actual_in_neighbors.add(new Integer(next_node_vertex_key));
                }
                if (next_node.getVertex_kind_id() == 6) { //control-point
                    control_neighbors.add(new Integer(next_node_vertex_key));
                }          
                if (next_node.getVertex_kind_id() == 10) {
                    expression_neighbors.add(new Integer(next_node_vertex_key));
                }
            }
        }
        
        /*********Print end**********/
    	//print_end("findNodesForSDDEdge");
        /******************************/ 
    }    
    
    
    // build SDD edges from control-node to actual-in node or among actual-in nodes
    private void buildSDDEdge(int ids_num_vertices, int[] ids_vertices_list, Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
    	print_start("buildSDDEdge");
        /******************************/ 
    	
        for (int i=0; i<ids_num_vertices; i++) {
            int sdd_vertex_key = ids_vertices_list[i];
            IdsVertex sdd_node = (IdsVertex) ids_vertices_hash.get(new Integer(sdd_vertex_key));
            Vector ai_neighbors = new Vector();
            Vector control_neighbors = new Vector();
            Vector expression_neighbors = new Vector();
        
            // Find the control point nodes and actual-in nodes that are data dependent on the sdd node
            this.findNodesForSDDEdge(sdd_node,ai_neighbors,control_neighbors,expression_neighbors,ids_vertices_hash);
            buildSDDFromControlToActualIn(control_neighbors,ai_neighbors, ids_vertices_hash);
            buildSDDBetweenActualIn(ai_neighbors, ids_vertices_hash);
        //    }
            //2007-12-07
           // markSDDBetweenControlAndExpression(control_neighbors,expression_neighbors, ids_vertices_hash);            
            
        }
        
    	
        /*********Print end**********/
    	print_end("buildSDDEdge");
        /******************************/ 
    }
    
/*    private void markSDDBetweenControlAndExpression(Vector control_neighbors, Vector expression_neighbors, Hashtable ids_vertices_hash) {
        Enumeration control_enum = control_neighbors.elements();
        while (control_enum.hasMoreElements()) {
            Integer control_node_vertex_key = (Integer) control_enum.nextElement();
            IdsVertex control_node = (IdsVertex) ids_vertices_hash.get(control_node_vertex_key);
        
            Enumeration expression_enum = expression_neighbors.elements();
            boolean continue_flag = true;
            while (expression_enum.hasMoreElements() && continue_flag) {
                Integer expression_node_vertex_key = (Integer) expression_enum.nextElement();
                IdsVertex expression_node = (IdsVertex) ids_vertices_hash.get(expression_node_vertex_key);
                if (control_node.getStartline()<expression_node.getStartline() && control_node.intrestedSDDFromControlToExpression(expression_node) ) {
                    control_node.setSddEdgeWithExpressionNode();
                    continue_flag = false;
                }
            }
        }
    }*/
    
 
 // build the sdd edges between actual-in nodes
 private void buildSDDBetweenActualIn(Vector ai_neighbors, Hashtable ids_vertices_hash) throws SQLException{
	 
 	
     /*********Print start**********/
 	 //print_start("buildSDDBetweenActualIn");
     /******************************/ 
	 
     Enumeration src_ai_ee = ai_neighbors.elements();
     while (src_ai_ee.hasMoreElements()) {
         Integer src_ai_vertex_key = (Integer) src_ai_ee.nextElement();
         IdsVertex src_ai_node = (IdsVertex) ids_vertices_hash.get(src_ai_vertex_key);
         /**
          * Bug Fix for Bug-34
          */
         if(src_ai_node == null)
             continue;
         Enumeration tar_ai_ee = ai_neighbors.elements();
         while (tar_ai_ee.hasMoreElements()) {
             Integer tar_ai_vertex_key = (Integer) tar_ai_ee.nextElement();
             if (src_ai_vertex_key.intValue()!=tar_ai_vertex_key.intValue()) {
                 IdsVertex tar_ai_node = (IdsVertex) ids_vertices_hash.get(tar_ai_vertex_key);
                 /**
                  * Bug Fix for Bug-34
                  */
                 if(tar_ai_node == null)
                     continue;
                 int sdd_type = src_ai_node.intrestedSDDFromAItoAI(tar_ai_node,this);  
                 if (src_ai_node.getStartline() < tar_ai_node.getStartline() && sdd_type > 0 &&
                     this.checkCfgPath(src_ai_node,tar_ai_node)) {
                     
                     if (sdd_type > 0 ) {
                     
                         Vector src_eqv_nodes = this.findAllEqvAINodes(src_ai_node,ids_vertices_hash);
                         src_eqv_nodes.add(src_ai_node);

                         Vector tar_eqv_nodes = this.findAllEqvAINodes(tar_ai_node,ids_vertices_hash);
                         tar_eqv_nodes.add(tar_ai_node);
                        
                         Enumeration src_eqv_nodes_enum = src_eqv_nodes.elements();
                         while (src_eqv_nodes_enum.hasMoreElements()) {
                             IdsVertex src_eqv_node = (IdsVertex) src_eqv_nodes_enum.nextElement();
                             /**
                              * Bug Fix for Bug-34
                              */
                             if(src_eqv_node == null)
                                 continue;
                             Enumeration tar_eqv_nodes_enum = tar_eqv_nodes.elements();
                             while(tar_eqv_nodes_enum.hasMoreElements()) {
                                 IdsVertex tar_eqv_node = (IdsVertex) tar_eqv_nodes_enum.nextElement();
                                 /**
                                  * Bug Fix for Bug-34
                                  */
                                 if(tar_eqv_node == null)
                                     continue;
                                 this.setEdges(src_eqv_node,tar_eqv_node,true,5,sdd_type);
                             }
                         }
                     }
                 }
             }
         }
     }    
     
     /*********Print end**********/
 	 //print_end("buildSDDBetweenActualIn");
     /******************************/ 
 } 
    
    
/*    // build the sdd edges between actual-in nodes
    private void buildSDDBetweenActualIn(Vector ai_neighbors, Hashtable ids_vertices_hash) throws SQLException{
        Enumeration src_ai_ee = ai_neighbors.elements();
        while (src_ai_ee.hasMoreElements()) {
            Integer src_ai_vertex_key = (Integer) src_ai_ee.nextElement();
            IdsVertex src_ai_node = (IdsVertex) ids_vertices_hash.get(src_ai_vertex_key);
            Enumeration tar_ai_ee = ai_neighbors.elements();
            while (tar_ai_ee.hasMoreElements()) {
                Integer tar_ai_vertex_key = (Integer) tar_ai_ee.nextElement();
                if (src_ai_vertex_key.intValue()!=tar_ai_vertex_key.intValue()) {
                    IdsVertex tar_ai_node = (IdsVertex) ids_vertices_hash.get(tar_ai_vertex_key);
                    int sdd_type = src_ai_node.intrestedSDDFromAItoAI(tar_ai_node,this);  
                    if (src_ai_node.getStartline() < tar_ai_node.getStartline() && sdd_type > 0 &&
                        this.checkCfgPath(src_ai_node,tar_ai_node)) {
                        
                        if (sdd_type > 0 ) {
                            this.setEdges(src_ai_node,tar_ai_node,true,5,sdd_type);
                            Enumeration eqv_ai_node_enum = this.findAllEqvAINodes(tar_ai_node,ids_vertices_hash).elements();
                            while (eqv_ai_node_enum.hasMoreElements()) {
                                IdsVertex eqv_ai_node = (IdsVertex) eqv_ai_node_enum.nextElement();
                                this.setEdges(src_ai_node,eqv_ai_node,true,5,sdd_type);                
                            }
                        }
                    }
                }
            }
        }    
    }     */
    
      // Build the shared data dependence edges from control node to actual-in parameter node
    private void buildSDDFromControlToActualIn(Vector control_neighbors, Vector ai_neighbors, Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
    	 //print_start("buildSDDFromControlToActualIn");
        /******************************/ 
    	
        Enumeration control_enum = control_neighbors.elements();
        
        while (control_enum.hasMoreElements()) {
    
            int control_node_vertex_key = ((Integer)control_enum.nextElement()).intValue();
            IdsVertex control_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(control_node_vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(control_node_in_ids == null)
                continue;
            Enumeration ai_node_ee = ai_neighbors.elements();
            while (ai_node_ee.hasMoreElements()) {
            
                int ai_node_vertex_key = ((Integer)ai_node_ee.nextElement()).intValue();
                IdsVertex ai_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(ai_node_vertex_key));          
                /**
                 * Bug Fix for Bug-34
                 */
                if(ai_node_in_ids == null)
                    continue;
                // check whether there is a cfg path from control-point to actual-in parameter
                int sdd_type = control_node_in_ids.intrestedSDDFromControlToAI(ai_node_in_ids,this);
                if (control_node_in_ids.getStartline()<=ai_node_in_ids.getStartline() && sdd_type > 0 &&
                    this.checkCfgPath(control_node_in_ids,ai_node_in_ids)) {

              //  if (control_node_in_ids.getStartline()<=ai_node_in_ids.getStartline() && control_node_in_ids.intrestedSDDFromControlToAI(ai_node_in_ids)) {
                    this.setEdges(control_node_in_ids,ai_node_in_ids,true,5,sdd_type);         
                   /////////// Find the equivalent AI nodes and build SDD from eqv AI nodes to control point node
                    Vector eqv_ai_nodes = this.findAllEqvAINodes(ai_node_in_ids,ids_vertices_hash);
                    Enumeration eqv_enum = eqv_ai_nodes.elements();
                    while (eqv_enum.hasMoreElements()) {
                        IdsVertex eqv_ai_node_in_ids = (IdsVertex) eqv_enum.nextElement();
                        /**
                         * Bug Fix for Bug-34
                         */
                        if(eqv_ai_node_in_ids == null)
                            continue;
                        this.setEdges(control_node_in_ids,eqv_ai_node_in_ids,true,5,sdd_type);
                    }
                }
            }
        }
        
        /*********Print end**********/
   	    //print_end("buildSDDFromControlToActualIn");
       /******************************/ 
    }    
    
    public Hashtable getSdd_labels_hash() {
        return this.sdd_labels;
    }
    
    public int getSdd_label_index() {
        return this.sdd_label_index;
    }
    
    public void increase_Sdd_label_index() {
        this.sdd_label_index++;
    }
    
    
    private IdsVertex findFormalInNode(int ai_node_vertex_key, Hashtable ids_vertices_hash) throws SQLException {
    	
    	
        String sql = "SELECT * FROM edges WHERE src_vertex_key = ? and edge_type = 3";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,ai_node_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        int fi_node_vertex_key = 0;
        if (rset.next()) {
            fi_node_vertex_key = rset.getInt("TAR_VERTEX_KEY");
            rset.close();
            pstmt.close();
        } else {
            rset.close();
            pstmt.close();
            return null;
        }
        IdsVertex fi_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(fi_node_vertex_key));
        if (fi_node_in_ids == null) return null;
        else                        return fi_node_in_ids;
        
        
        

    }
    
    private Vector findAllEqvAINodes(IdsVertex src_node, Hashtable ids_vertices_hash) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("findAllEqvAINodes");
        /******************************/ 
    	
        Vector eqv_ai_nodes = new Vector(); // Store the actual-in node that share data dependence with src_node
        int[] ai_node_vertex_key_list = new int [2000];  
        int num_ai_node = 0;
        /**
         * Bug Fix for Bug-34
         */
        if(src_node == null)
            return eqv_ai_nodes;
        ai_node_vertex_key_list[num_ai_node] = src_node.getVertex_key();
        num_ai_node++;
        int index = 0;
        while (index < num_ai_node && num_ai_node<200) {
            int current_ai_node_vertex_key = ai_node_vertex_key_list[index++];
            
            // Find the formal in node that is data dependent on current_ai_node 
            IdsVertex current_fi_node = this.findFormalInNode(current_ai_node_vertex_key,ids_vertices_hash);
            if (current_fi_node != null) {  // The formal-in parameter is interstet
                // Find those nodes that are data dependent on the formal-in node
                Enumeration ee_formal = current_fi_node.getTargetNodes().elements(); 
                while (ee_formal.hasMoreElements()) {
                    Edge edge = (Edge) ee_formal.nextElement();
                    int next_node_vertex_key = edge.getTar_vertex_key();
                    IdsVertex next_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(next_node_vertex_key));
                    if(next_node_in_ids == null)
                        continue;
                    //Find those actual-in nodes that are data dependent on the current formal-in node
                    if (edge.getEdgeInfo(0)!=0 && next_node_in_ids.getVertex_kind_id()==1) {
                        eqv_ai_nodes.add(next_node_in_ids);
                        ai_node_vertex_key_list[num_ai_node++] = next_node_vertex_key;
                    }
                }
            }
        }
        
        /*********Print end**********/
 	    //print_end("findAllEqvAINodes");
        /******************************/ 
        
        return eqv_ai_nodes;
    }    
    
    
    private boolean isInvolvedPdg(Hashtable ids_pdg, int pdg_id) {
        Object obj = ids_pdg.get(new Integer(pdg_id));
        if (obj == null) return false;
        else             return true;
    }
    
    
    // Including global-formal-in/out global-actual-in/out into the IDS by using data dependences
    private int addGlobalNodes(int ids_vertices_num, Hashtable ids_vertices_hash, int[] ids_vertices_list, Hashtable ids_pdg) throws SQLException {
    	
        /*********Print start**********/
 	    print_start("addGlobalNodes");
        /******************************/ 
    	
        int index = 0;
        while (index < ids_vertices_num && ids_vertices_num < MAX_NODE_IN_PHASE2) {
            int current_node_vertex_key = ids_vertices_list[index++];
            IdsVertex current_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(current_node_vertex_key));
            /**
             * Bug Fix for Bug-34
             */
            if(current_node_in_ids == null)
                continue;
            PreparedStatement  pstmt = conn.prepareStatement(forward_sql);
            pstmt.setInt(1,current_node_vertex_key);
            ResultSet rset = pstmt.executeQuery();
            
            while (rset.next() && ids_vertices_num < MAX_NODE_IN_PHASE2) {
                int new_node_vertex_key = rset.getInt("NEW_NODE_VERTEX_KEY");
                int new_node_vertex_label = rset.getInt("NEW_NODE_VERTEX_LABEL");
                int new_node_vertex_kind_id = rset.getInt("NEW_NODE_VERTEX_KIND_ID");
                int new_node_pdg_id = rset.getInt("NEW_NODE_PDG_ID");
                int new_node_startline = rset.getInt("NEW_NODE_STARTLINE");
                int edge_type = rset.getInt("EDGE_TYPE")-1;
                IdsVertex new_node_in_ids = new IdsVertex(ids_vertices_num,new_node_vertex_key,new_node_vertex_label,
                                                          new_node_vertex_kind_id,new_node_pdg_id,new_node_startline);
                /**
                 * Bug Fix for Bug-34
                 */
                if(new_node_in_ids == null)
                    continue;
                if (isIntrestedNode(ids_pdg,current_node_in_ids,new_node_in_ids,true)) {  // 
                    // The edge is added into the IDS
                    ids_vertices_num = this.addNodeOrEdge(ids_vertices_hash,ids_vertices_list,ids_vertices_num,
                                                          current_node_in_ids,new_node_in_ids,true,edge_type);
                }    
            }
            rset.close();
            pstmt.close();  
        }        
        
        /*********Print end**********/
 	    print_end("addGlobalNodes");
        /******************************/ 
        
        return ids_vertices_num;
    }
    
    private int addNodeOrEdge(Hashtable ids_vertices_hash, int[] ids_vertices_list, int ids_vertices_num, 
                              IdsVertex current_node_in_ids, IdsVertex new_node_in_ids, boolean direction, int edge_type) {
    	
        /*********Print start**********/
 	    //print_start("addNodeOrEdge");
        /******************************/ 
    	
        IdsVertex next_node_in_ids = (IdsVertex) ids_vertices_hash.get(new Integer(new_node_in_ids.getVertex_key()));
        if (next_node_in_ids == null) {
            ids_vertices_list[ids_vertices_num] = new_node_in_ids.getVertex_key();
            ids_vertices_hash.put(new Integer(new_node_in_ids.getVertex_key()),new_node_in_ids);
            ids_vertices_num++;
            this.setEdges(current_node_in_ids,new_node_in_ids,direction,edge_type,1);
        } else {
            this.setEdges(current_node_in_ids,next_node_in_ids,direction,edge_type,1);
        }
        
        /*********Print end**********/
 	    //print_end("addNodeOrEdge");
        /******************************/ 
        
        return ids_vertices_num;
    }
    
    private void setEdges(IdsVertex current_node, IdsVertex new_node, boolean direction,int edge_type, int len) {
    	
        /*********Print start**********/
 	    //print_start("setEdges");
        /******************************/ 
    	
        if(direction) {
            current_node.addSingleTargetNeighbor(new_node.getVertex_key(),new_node.getPdg_id(),edge_type,len);
            new_node.addSingleSourceNeighbor(current_node.getVertex_key(),current_node.getPdg_id(),edge_type,len);
        } else {
            new_node.addSingleTargetNeighbor(current_node.getVertex_key(),current_node.getPdg_id(),edge_type,len);
            current_node.addSingleSourceNeighbor(new_node.getVertex_key(),new_node.getPdg_id(),edge_type,len);
        }
        
        /*********Print end**********/
 	    //print_end("setEdges");
        /******************************/ 
        
    }
    
       
    // Check whether the PDS has been used for building the IDS
    // Check (1) the pdg of the target node is involved in the ids_pdg
    // the target node is not an gloabl-actual-out and auxiliary node
    private boolean isIntrestedNode(Hashtable ids_pdg, IdsVertex current_node_in_ids, IdsVertex new_node_in_ids, boolean direction) {    	
    	
        Object obj = ids_pdg.get(new_node_in_ids.getPdg_id_key());
        if (obj == null) return false;
        int cn_id = current_node_in_ids.getVertex_kind_id();
        int nn_id = new_node_in_ids.getVertex_kind_id();
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
    
    // Get the PDGs from which the IDS is derived from 
    private Hashtable getIdsPdg(int gid) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("getIdsPdg");
        /******************************/ 
    	
        Hashtable ids_pdg = new Hashtable();
        String sql = "SELECT * FROM ids_pdg WHERE gds_id = ? AND graph_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        pstmt.setInt(2,gid);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int pdg_id = rset.getInt("PDG_ID");
            int level = rset.getInt("PDG_LEVEL");
            int parent_pdg_id = rset.getInt("PARENT_PDG_ID");
            PdgForPhaseOne pdg = new PdgForPhaseOne(pdg_id,parent_pdg_id,level,true);
            ids_pdg.put(pdg.getPdg_id_Key(),pdg);
        }
        rset.close();
        pstmt.close();
        
        /*********Print end**********/
 	    //print_end("getIdsPdg");
        /******************************/ 
        
        return ids_pdg;
    }
    
    
    // Load the vertices included in the IDS (The nodes were found in the phase 1)
    // return the number of nodes in the IDS
    private int loadIds(int gid, Hashtable ids_vertices_hash, int[] ids_vertices_list) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("loadIds");
        /******************************/ 
    	
        String sql = "SELECT I.NODE_INDEX, I.DISTANCE_TO_CANDIDATE, V.*  " + 
                     "FROM   IDS_NODES I, VERTEX V  " + 
                     "WHERE  I.GDS_ID = ?  " + 
                     "AND    I.GRAPH_ID = ?  " + 
                     "AND    I.VERTEX_KEY = V.VERTEX_KEY  " + 
                     "ORDER BY NODE_INDEX";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        pstmt.setInt(2,gid);
        ResultSet rset = pstmt.executeQuery();
        int num_vertices = 0;
        while (rset.next()) {
            int ids_index = rset.getInt("NODE_INDEX");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int startline = rset.getInt("STARTLINE");
            int pdg_id = rset.getInt("PDG_ID");
            int distance_to_candidate = rset.getInt("DISTANCE_TO_CANDIDATE");
            String fieldname1 = rset.getString("FIELDNAME1");
            String fieldname2 = rset.getString("FIELDNAME2");
            IdsVertex v = new IdsVertex(ids_index,vertex_key,vertex_label,vertex_kind_id,pdg_id,startline);
            v.setDistance_to_candidate(distance_to_candidate);
            v.setFieldName1(fieldname1);
            v.setFieldName2(fieldname2);
            switch (vertex_kind_id) {
                case 1: 
                case 2:
                case 5:Object obj = this.frq_label_hash.get(new Integer(vertex_label));
                       if (obj != null) v.setIds_frq_label();
                       break;
                case 6:v.setIds_frq_label(); break;
                case 21:v.setIds_frq_label(); break;
            }

            
            ids_vertices_hash.put(new Integer(vertex_key),v);
            ids_vertices_list[ids_index] = vertex_key;
            num_vertices++;
        }
        rset.close();        
        pstmt.close();
        
        /*********Print end**********/
 	    //print_end("loadIds");
        /******************************/ 
        
        return num_vertices;
    }
    
    private void getGraphNumber() throws SQLException  {
    	
        /*********Print start**********/
 	    //print_start("getGraphNumber");
        /******************************/ 
    	
        String sql = "SELECT * FROM graphdataset WHERE gds_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
          num_graph = rset.getInt("GRAPH_NUM");
        } else {
          num_graph = 0;
        }
        rset.close();
        pstmt.close();
        
        /*********Print end**********/
 	    //print_end("getGraphNumber");
        /******************************/ 
    }
    
    private void insertFreqParameterNodelabel(Hashtable frq_label_hash,int call_site_vertex_key, int callsite_label) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("insertFreqParameterNodelabel");
        /******************************/ 
    	
        Object obj1 = this.ignore_frq_ai_node.get(new Integer(callsite_label));
        if (obj1 != null) return;        
        
        String sql = "SELECT vertex_label FROM edges e, vertex v " +
                     "WHERE  e.src_vertex_key = ? " +
                     "AND    e.tar_vertex_key = v.vertex_key " +
                     "AND    v.vertex_kind_id in (1,2)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,call_site_vertex_key);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            Integer vertex_label = new Integer(rset.getInt("VERTEX_LABEL"));
            Object obj = frq_label_hash.get(vertex_label);
            if (obj == null) frq_label_hash.put(vertex_label,vertex_label);
        }
        rset.close();
        pstmt.close();
        
    	
        /*********Print end**********/
 	    //print_end("insertFreqParameterNodelabel");
        /******************************/ 
    }
    
    private void loadFrq_Callsite(Hashtable frq_label_hash, int frq_label) throws SQLException {
    	
    	
        /*********Print start**********/
 	    //print_start("loadFrq_Callsite");
        /******************************/ 
    	
        String sql = "SELECT * FROM vertex WHERE vertex_label = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,frq_label);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
            int vertex_kind_id  = rset.getInt("VERTEX_KIND_ID");
            if (vertex_kind_id != 17) { //indirect function call
                Object obj = frq_label_hash.get(new Integer(frq_label));
                if (obj == null) {
                   frq_label_hash.put(new Integer(frq_label),new Integer(frq_label));
                   int call_site_vertex_key = rset.getInt("VERTEX_KEY");
                   this.insertFreqParameterNodelabel(frq_label_hash,call_site_vertex_key,frq_label);
                }
            }
        }
        rset.close();
        pstmt.close();
        
        /*********Print end**********/
 	    //print_end("loadFrq_Callsite");
        /******************************/ 
    }
    
    
    
    
    private void loadFrq_label() throws SQLException {
    
        /*********Print start**********/
 	    //print_start("loadFrq_label");
        /******************************/ 
    
        String sql = "select vertex_label, count(*) " + 
                             "from (select distinct graph_id, vertex_label " + 
                             "      from   ids_nodes i, vertex v  " + 
                             "      where  i.gds_id = ?  " + 
                             "      and    i.vertex_key = v.vertex_key  " + 
                             "      and    v.vertex_kind_id in (5))  " + 
                             "group by vertex_label  " + 
                             "having count(*) >= round("+ (new Float(this.frequency * this.num_graph)).toString()+")";
          System.out.println(sql);
          PreparedStatement pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1,this.gds_id);
          ResultSet rset = pstmt.executeQuery();
          while (rset.next()) {
               int frq_label = rset.getInt("VERTEX_LABEL");
               loadFrq_Callsite(frq_label_hash,frq_label);   
  //                  this.frq_label_hash.put(frq_label,frq_label);
          }
          rset.close();
          pstmt.close();                       
/*        String sql = "select vertex_label, count(*) " + 
                     "from (select distinct graph_id, vertex_label\n" + 
                     "      from   ids_nodes i, vertex v  " + 
                     "      where  i.gds_id = ?  " + 
                     "      and    i.vertex_key = v.vertex_key  " + 
                     "      and    v.vertex_kind_id in (1,2,5))  " + 
                     "group by vertex_label  " + 
                     "having count(*) >= round("+ (new Float(this.frequency * this.num_graph)).toString()+")";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds_id);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            Integer frq_label = new Integer(rset.getInt("VERTEX_LABEL"));
            this.frq_label_hash.put(frq_label,frq_label);
        }
        rset.close();
        pstmt.close();  */
          
          /*********Print end**********/
   	      //print_end("loadFrq_label");
          /******************************/ 
    }
    
   
    private CfgVertex getCfgVertex(int vertex_key, int pdg_id) throws SQLException {
        /*********Print start**********/
 	    //print_start("getCfgVertex");
        /******************************/ 
    	
          CfgVertex node = (CfgVertex) cfg_vertices.get(new Integer(vertex_key));
          if (node == null) {
              String sql = "select src_vertex_key, count(*) " + 
                           "from   cfg_edges c, vertex v " + 
                           "where  c.src_pdg_id = ? " + 
                           "and    c.src_pdg_id = c.tar_pdg_id " + 
                           "and    c.tar_vertex_key = v.vertex_key " + 
                           "and    (not v.vertex_kind_id in (1,2,23,24)) " + 
                           "group by src_vertex_key "; 
              PreparedStatement pstmt = conn.prepareStatement(sql);
              pstmt.setInt(1, pdg_id);
              ResultSet rset = pstmt.executeQuery();
              while (rset.next()) {
                  int key = rset.getInt(1);
                  int neighbor_num = rset.getInt(2);
                  CfgVertex v = new CfgVertex(key,neighbor_num);
                  cfg_vertices.put(new Integer(key),v);
              }
              rset.close();
              pstmt.close();
          
              sql = "select src_vertex_key, tar_vertex_key, v.startline " + 
                    "from   cfg_edges c, vertex v                " + 
                    "where  c.src_pdg_id = ?                 " + 
                    "and    c.src_pdg_id = c.tar_pdg_id          " + 
                    "and    c.tar_vertex_key = v.vertex_key      " + 
                    "and    (not v.vertex_kind_id in (1,2,23,24))"; 
              pstmt = conn.prepareStatement(sql);
              pstmt.setInt(1,pdg_id);
              rset = pstmt.executeQuery();
              while (rset.next()) {
                  Integer key = new Integer(rset.getInt(1));
                  int neighbor_key = rset.getInt(2);
                  int startline = rset.getInt(3);
                  CfgVertex v = (CfgVertex) cfg_vertices.get(key);
                  v.setNeighbor(neighbor_key,startline);                
              }
              rset.close();
              pstmt.close();
              node = (CfgVertex) cfg_vertices.get(new Integer(vertex_key));
          }
          
          /*********Print end**********/
   	      ///print_end("getCfgVertex");
          /******************************/ 
          
          return node;
    }   
    
    private int checkExistingCfgPath(IdsVertex src_node, IdsVertex tar_node) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("checkExistingCfgPath");
        /******************************/ 
    	
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
        
        /*********Print end**********/
 	    //print_end("checkExistingCfgPath");
        /******************************/ 
        
        return check_result;
    }


    private boolean checkCfgPath(IdsVertex src_node, IdsVertex tar_node) throws SQLException {
    	
        /*********Print start**********/
 	    //print_start("checkCfgPath");
        /******************************/ 
    
         
          IdsVertex source_node;
          IdsVertex target_node;
          if (src_node.getVertex_kind_id()==1) source_node = this.getCallsiteNode(src_node);
          else                                 source_node = src_node;
          if (tar_node.getVertex_kind_id()==1) target_node = this.getCallsiteNode(tar_node);
          else                                 target_node = tar_node;
          if (source_node == null || target_node == null) return false;
     
          int check_result = this.checkExistingCfgPath(src_node,tar_node);
          if (check_result == 0) return false;
          if (check_result == 1) return true;      
          
          boolean found_path = false;
     
          int pdg_id = source_node.getPdg_id();
          int target_node_startline = target_node.getStartline();
          int source_node_startline = src_node.getStartline();
          int[][] cfg_path = new int[20000][3];
          cfg_path[0][0] = source_node.getVertex_key();
          cfg_path[0][1] = source_node.getStartline();
          cfg_path[0][2] = source_node.getStartline();
          int path_vertices_num = 1;
          int index = 0;
          Hashtable path_nodes = new Hashtable();
          path_nodes.put(source_node.getVertex_key_integer(),source_node.getVertex_key_integer());
          while (index < path_vertices_num && !found_path) {
              int current_node_vertex_key = cfg_path[index][0];
              int current_node_startline = cfg_path[index][1];
              int previous_node_startline =cfg_path[index][2];
              
              CfgVertex current_node = getCfgVertex(current_node_vertex_key,pdg_id);
              if (current_node != null) {
                  int[][] neighbors = current_node.getNeighbors();
                  int neighbor_num = current_node.getNeighbor_num();
                  for (int i=0; i<neighbor_num; i++) {
                      Integer next_node_vertex_key = new Integer(neighbors[i][0]);
                      int next_node_startline = neighbors[i][1];
                      // Find a path to target node
                      if (next_node_vertex_key == target_node.getVertex_key()) found_path = true; 
                   
                      boolean valid_next_node;
                      if (current_node_startline >= previous_node_startline) {
                          if (next_node_startline <= target_node_startline) valid_next_node = true;
                          else                                              valid_next_node = false;
                      } else {
                          if (next_node_startline >= previous_node_startline && next_node_startline <= target_node_startline) valid_next_node = true;
                          else                                                                                                valid_next_node = false;   
                      }
                      
               //      
               //     2007-12-28
               // if (next_node_startline>=current_node_startline && next_node_startline <= target_node_startline) {
               
                      if (valid_next_node) {
                          Object obj = path_nodes.get(new Integer(next_node_vertex_key));
                          if (obj == null) {
                              cfg_path[path_vertices_num][0] = next_node_vertex_key.intValue();
                              cfg_path[path_vertices_num][1] = next_node_startline;
                              cfg_path[path_vertices_num][2] = current_node_startline;
                              path_vertices_num++;
                              path_nodes.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                          }    
                      }  
                  }
              }
              index++;
          }
          
          this.insertExistCfgPath(src_node,tar_node,found_path);
          
      	
          /*********Print end**********/
   	       //print_end("checkCfgPath");
          /******************************/ 
          
          return found_path;
          
    }

    private void insertExistCfgPath(IdsVertex src_node, IdsVertex tar_node, boolean foundPath) throws SQLException {
        /**
         * Bug Fix for Bug-34
         */
        if(src_node == null || tar_node == null)
            return;
        String sql = "INSERT INTO cfg_path VALUES(?,?,?)";
        int edgeType = 0;
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,src_node.getVertex_key());
        pstmt.setInt(2,tar_node.getVertex_key());
        if (foundPath) {pstmt.setInt(3,1);edgeType=1;}
        else           {pstmt.setInt(3,0);edgeType=0;}
        pstmt.execute();
        pstmt.close();
        //System.out.println("RIDSSetPhaseTwo.insertExistCfgPath: insertetd into cfg_path: "+src_node.getVertex_key()+" "+tar_node.getVertex_key()+" "+edgeType);
    }  


/*    private boolean checkCfgPath(IdsVertex src_node, IdsVertex tar_node) throws SQLException {
        IdsVertex source_node;
        IdsVertex target_node;
        if (src_node.getVertex_kind_id()==1) source_node = this.getCallsiteNode(src_node);
        else                                 source_node = src_node;
        if (tar_node.getVertex_kind_id()==1) target_node = this.getCallsiteNode(tar_node);
        else                                 target_node = tar_node;
        if (source_node == null || target_node == null) return false;
        
        loadCfgEdges(src_node.getPdg_id());
        
        
        int[][] cfg_path = new int[20000][2];
        cfg_path[0][0] = source_node.getVertex_key();
        cfg_path[0][1] = source_node.getStartline();
        int path_vertices_num = 1;
        int index = 0;
        Hashtable path_nodes = new Hashtable();
        path_nodes.put(source_node.getVertex_key_integer(),source_node.getVertex_key_integer());
        while (index < path_vertices_num) {
            int current_node_vertex_t_key = cfg_path[index][0];
            int current_node_startline = cfg_path[index][1];
            String sql = "select v.vertex_key, v.vertex_kind_id, v.startline  " + 
                         "from cfg_edges c, vertex v  " + 
                         "where c.src_vertex_key = ?  " + 
                         "and   c.tar_vertex_key = v.vertex_key  " + 
                         "and   c.src_pdg_id = c.tar_pdg_id " +
                         "and   (not v.vertex_kind_id in (1,2)) " +
                         "and   v.startline <= ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,current_node_vertex_t_key);
            pstmt.setInt(2,target_node.getStartline());
            ResultSet rset = pstmt.executeQuery();
            boolean flag = false;
            while (rset.next()) {
                Integer next_node_vertex_key = new Integer(rset.getInt("VERTEX_KEY"));
                int next_node_startline = rset.getInt("STARTLINE");
                // Find a path to target node
                if (next_node_vertex_key == target_node.getVertex_key()) flag = true; 
                if (next_node_startline>=current_node_startline) {
                    Object obj = path_nodes.get(new Integer(next_node_vertex_key));
                    if (obj == null) {
                        cfg_path[path_vertices_num][0] = next_node_vertex_key.intValue();
                        cfg_path[path_vertices_num][1] = next_node_startline;
                        path_vertices_num++;
                        path_nodes.put(new Integer(next_node_vertex_key), new Integer(next_node_vertex_key));
                    }    
                }  
            }
            rset.close();
            pstmt.close();
            if (flag) return true;
            index++;
        }
        return false;
    } */
    
    private IdsVertex getCallsiteNode(IdsVertex param_node) throws SQLException {
    	
    	
        /*********Print start**********/
 	    //print_start("getCallsiteNode");
        /******************************/ 
    	
        String sql = "select v.* from edges e, vertex v " +
                     "where e.tar_vertex_key = ? and e.edge_type = 2 " +
                     "and   e.src_vertex_key = v.vertex_key ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,param_node.getVertex_key());
        ResultSet rset = pstmt.executeQuery();
        IdsVertex callsite_node = null;
        if (rset.next())  {
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int pdg_id = rset.getInt("PDG_ID");
            int startline = rset.getInt("STARTLINE");
            callsite_node = new IdsVertex(0,vertex_key,vertex_label,vertex_kind_id,pdg_id,startline);
        }
        rset.close();
        pstmt.close();
        rset = null;
        pstmt = null; 
        //System.gc();
        
        /*********Print end**********/
 	    //print_end("getCallsiteNode");
        /******************************/ 
        
        return callsite_node;
    }
    
    
    /***************Check time functions *******************/
    private void print_start(String s)
    {
   	 java.util.Date  start_date = new java.util.Date();
   	 time_stack.push(start_date.getTime());
   	 String out = printtab(tab_count);
   	 out = out+s+" started ";
   	 System.out.println(out);
   	 if(this.writeToFile){
   	     try {
   	         writer.write(out+"\n");
   	     } catch (Exception e) {
   	         e.printStackTrace();
   	     }
   	 }
   	 tab_count++;
   	 
    }
    
    private void print_end(String s)
    {
   	 java.util.Date end_date = new java.util.Date();
   	 long start_time = time_stack.pop();
   	 long total = end_date.getTime()-start_time;
   	 tab_count--;
   	 String out = printtab(tab_count);
   	 out = out + s+" ended: "+String.valueOf(total);
   	 System.out.println(out);
   	 if(this.writeToFile){
   	     try {
   	         writer.write(out+"\n");
   	     } catch (Exception e) {
   	         e.printStackTrace();
   	     } 
   	 }
    }
   	 
    
    private String printtab(int n)
    {
   	 String s = "";
   	 for(int i=0; i<n;i++)
   	 {
   		 s=s+"\t";
   	 }
   	 return s;
    }
    /***************Check time functions end*******************/
    
}
