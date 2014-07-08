package andy;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
// Reduced Interprocedural Dependence Sphere
public class RIDS {

    GraphDataset gds;
    int gid;
    int[] rids_vertices_list;
    Hashtable rids_vertices_hash = new Hashtable();
    Connection conn;
    int vertices_num = 0;
    int[][][] edges;
    
    /*****************Check time*************************/
	Stack<Long> time_stack = new Stack<Long>();
	int tab_count = 0;
	File log;
	BufferedWriter writer;
	/*****************Check time ends********************/
	
    public RIDS(GraphDataset gds, int gid, Connection conn) throws SQLException {
    	/*****************check time: init writer****************************/
		try {
			log = new File("RIDS_"+gds+"_"+gid+".txt");
			writer = new BufferedWriter(new FileWriter(log));
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*****************check time: init writer ends****************************/
		
		
        this.gds = gds;
        this.gid = gid;
        this.conn = conn;
        this.loadRIDSVerticesNumber();
        loadRIDS();
        computeTransitiveClosure();
        
        /*****************check time: close writer****************************/
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*****************check time: close writer ends****************************/
    }
    
    public int getVertices_Number() {
        return this.vertices_num;
    }
    
    
    public int[][][] getRids_edge() {
        return this.edges;
    }
    
    public int[] getRids_vertices_list() {
        return this.rids_vertices_list;
    }
    
    private void setupEdges() {
        for (int i=0; i<vertices_num; i++) {
            for (int j=0; j<vertices_num; j++) {
                for (int k=0; k<6; k++) {
                    edges[i][j][k] = 0;
                }
            }
        }
    }
    
    public int getRIDS_vertex_key(int index) {
        return this.rids_vertices_list[index];
    }
    
    public Hashtable getRids_vertices_hash() {
        return this.rids_vertices_hash;
    }
    
    private void loadRIDS() throws SQLException {
        /*********Print start**********/
    	//print_start("loadRIDS");
        /******************************/
        rids_vertices_list = new int[this.vertices_num];
        //0:data, 1:control, 2:sdd, 3:transitive data, 4:transitive control
        edges = new int[this.vertices_num][this.vertices_num][6];
        this.setupEdges();
        /// Load RIDS Nodes
        String sql = "SELECT v.*, r.NODE_INDEX, r.DISTANCE_TO_CANDIDATE FROM vertex v, rids_nodes r " +
                     "WHERE  r.gds_id = ? " +
                     "AND    r.graph_id = ? " +
                     "AND    r.vertex_key = v.vertex_key " +
                     "ORDER BY NODE_INDEX "; 
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,gds.getGds_id());
        pstmt.setInt(2,gid);
        ResultSet rset = pstmt.executeQuery();
        while (rset.next()) {
            int node_index = rset.getInt("NODE_INDEX");
            int vertex_label = rset.getInt("VERTEX_LABEL");
            int vertex_key = rset.getInt("VERTEX_KEY");
            int vertex_kind_id = rset.getInt("VERTEX_KIND_ID");
            int startline = rset.getInt("STARTLINE");
            int pdg_id = rset.getInt("PDG_ID");
            int distance_to_candidate = rset.getInt("DISTANCE_TO_CANDIDATE");
            String fieldname1 = rset.getString("FIELDNAME1");
            String fieldname2 = rset.getString("FIELDNAME2");
            Vertex v = new Vertex(node_index,vertex_key,vertex_label,vertex_kind_id,startline,distance_to_candidate,fieldname1,fieldname2,pdg_id);
            this.rids_vertices_hash.put(new Integer(vertex_key),v);
            this.rids_vertices_list[node_index] = vertex_key;
        }
        rset.close();
        pstmt.close();
        
        
        /// Load RIDS edges
        sql = "SELECT * FROM rids_edges " +
              "WHERE gds_id = ? " +
              "AND   graph_id = ? ";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,this.gds.getGds_id());
        pstmt.setInt(2,this.gid);
        rset = pstmt.executeQuery();
        while (rset.next()) {
            int src_node_index = rset.getInt("SRC_NODE_INDEX");
            int tar_node_index = rset.getInt("TAR_NODE_INDEX");
            int data_edge = rset.getInt("DATA_EDGE");
            int control_edge = rset.getInt("CONTROL_EDGE");
            int sdd_edge = rset.getInt("SDD_EDGE");
            this.edges[src_node_index][tar_node_index][0] = data_edge;
            this.edges[src_node_index][tar_node_index][1] = control_edge;
            this.edges[src_node_index][tar_node_index][2] = sdd_edge;
            this.edges[src_node_index][tar_node_index][3] = data_edge;
            this.edges[src_node_index][tar_node_index][4] = control_edge;      
            this.edges[src_node_index][tar_node_index][5] = sdd_edge;
        }
        rset.close();
        pstmt.close(); 
        
        /*********Print ends**********/
    	//print_end("loadRIDS");
    	/******************************/
    }
    
    private void loadRIDSVerticesNumber() throws SQLException {
        /*********Print start**********/
    	//print_start("loadRIDSVerticesNumber");
        /******************************/
        
    	String sql = "SELECT count(*) FROM rids_nodes WHERE gds_id = ? AND graph_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,gds.getGds_id());
        pstmt.setInt(2,this.gid);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
            this.vertices_num = rset.getInt(1);
        }
        rset.close();
        pstmt.close();
        
        /*********Print end**********/
    	//print_end("loadRIDSVerticesNumber");
        /******************************/
    }
    
    private void computeTransitiveClosure() {
        /*********Print start**********/
    	//print_start("computeTransitiveClosure");
        /******************************/
        
    	computeClosureByType(0);  // Data transitive closure
        computeClosureByType(1);  // Control transitive closure
        
        /*********Print end**********/
    	//print_end("computeTransitiveClosure");
        /******************************/
    }

    private void computeClosureByType(int type) {
        /*********Print start**********/
    	//print_start("computeClosureByType");
        /******************************/
        
    	for (int m=0; m<3; m++) {
            for (int i=0; i<vertices_num; i++) {
                for (int j=0; j<vertices_num; j++) {
                    for (int k=0; k<vertices_num; k++) {   
                        if (edges[i][j][type]>0 && edges[j][k][type]>0 && edges[i][k][type] ==0) {
                            int length = edges[i][j][type] + edges[j][k][type];
                            if (length < 10) {
                                edges[i][k][type+3] = length;
                            }
                        }
                    }
                }
            }
        }
        
        /*********Print end**********/
    	//print_end("loadRIDS");
        /******************************/
    }
    
    /***************Check time functions *******************/
    private void print_start(String s)
    {
   	 java.util.Date  start_date = new java.util.Date();
   	 time_stack.push(start_date.getTime());
   	 String out = printtab(tab_count);
   	 out = out+s+" started ";
   	 System.out.println(out);
   	 try {
       	 writer.write(out+"\n");
		} catch (Exception e) {
			e.printStackTrace();
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
   	 try {
       	 writer.write(out+"\n");
		} catch (Exception e) {
			e.printStackTrace();
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
