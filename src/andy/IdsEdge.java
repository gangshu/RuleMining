package andy;

public class IdsEdge {
    int src_node_index;
    int tar_node_index;
    int edgetype;
    int length = 1;
    String key;

    public IdsEdge(int src_node_index, int tar_node_index, int edgetype) {
        this.src_node_index = src_node_index;
        this.tar_node_index = tar_node_index;
        this.edgetype = edgetype;
        this.key = Integer.toString(src_node_index)+"-"+Integer.toString(tar_node_index)+"-"+Integer.toString(edgetype);
    }
    
    public IdsEdge(int src_node_index, int tar_node_index, int edgetype, int length) {
        this.src_node_index = src_node_index;
        this.tar_node_index = tar_node_index;
        this.edgetype = edgetype;
        this.key = Integer.toString(src_node_index)+"-"+Integer.toString(tar_node_index)+"-"+Integer.toString(edgetype);
        this.length = length;       
    }
    
    public int getLength() {
        return this.length;
    }
    
    public void change_index(int change) {
        this.src_node_index = this.src_node_index + change;
        this.tar_node_index = this.tar_node_index + change;
        this.key = Integer.toString(src_node_index)+"-"+Integer.toString(tar_node_index)+"-"+Integer.toString(edgetype);
    }
    
    public int getSrc_node_index() {
        return this.src_node_index;
    }
    
    public int getTar_node_index() {
        return this.tar_node_index;
    }
    
    public int getEdgetype() {
        return this.edgetype;
    }
    
    public String getKey() {
        return this.key;
    }
    
}
