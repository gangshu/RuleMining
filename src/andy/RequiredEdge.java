package andy;

public class RequiredEdge {
    int src_node_index;
    int tar_node_index;
    int edge_type;
    String sdd_info = "";
    String key;
    boolean src_is_csg_node;
    
    public RequiredEdge(int src_node_index, int tar_node_index, int edge_type, boolean src_is_csg_node) {
        this.src_node_index = src_node_index;
        this.tar_node_index = tar_node_index;
        this.edge_type = edge_type;
        key = Integer.toString(src_node_index) + "-" + Integer.toString(tar_node_index) + "-" +
              Integer.toString(edge_type);
        this.src_is_csg_node = src_is_csg_node;
    }
    
    public boolean getSrc_is_csg_node() {
        return this.src_is_csg_node;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public void setSDD_info(String data) {
        this.sdd_info = data;
    }
    
    public String getSDD_info() {
        return this.sdd_info;
    }
    
    public int getSrc_node_index() {
        return this.src_node_index;
    }
    
    public int getTar_node_index() {
        return this.tar_node_index;
    }
    
    public int getEdge_type() {
        return this.edge_type;
    }
    
    
}
