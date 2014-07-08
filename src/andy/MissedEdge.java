package andy;

public class MissedEdge {
    
    int tar_node_label;
    int edgetype;
    int count = 0;
    String key;
    public MissedEdge(int tar_node_label, int edgetype, boolean forward) {
        this.tar_node_label = tar_node_label;
        this.edgetype = edgetype;
        if (forward) this.key = "F" + Integer.toString(tar_node_label) + "-" + Integer.toString(edgetype);
        else         this.key = "B" + Integer.toString(tar_node_label) + "-" + Integer.toString(edgetype);
    }
    
    public MissedEdge(int tar_node_label, int edgetype, boolean forward, String sdd_info) {
        this.tar_node_label = tar_node_label;
        this.edgetype = edgetype;
        if (forward) this.key = "F" + Integer.toString(tar_node_label) + "-" + Integer.toString(edgetype) + "." + sdd_info;
        else         this.key = "B" + Integer.toString(tar_node_label) + "-" + Integer.toString(edgetype) + "." + sdd_info;
    }
    
    public String getKey() {
        return key; 
    }
    
    public int getEdgetype() {
        return this.edgetype;
    }
    
    public void increaseEdgeNumber() {
        this.count++;
    }
    
    public int getEdgeNumber() {
        return this.count;
    }
    
    
}
