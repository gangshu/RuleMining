package andy;


public class EdgeCode implements Comparable{
    int node_index;
    int vertex_label;
    int edge_label;
    int edge_direction;
    int item;
    int start_vertex_kind_id;
    int end_vertex_kind_id;

    public EdgeCode(int node_index, int vertex_label, int edge_label, int edge_direction, int start_vertex_kind_id, int end_vertex_kind_id) {
        this.node_index = node_index;
        this.vertex_label = vertex_label;
        this.edge_label = edge_label;
        this.edge_direction = edge_direction;
        this.start_vertex_kind_id = start_vertex_kind_id;
        this.end_vertex_kind_id = end_vertex_kind_id;
    }
    
    public boolean isAo_to_Cp_Data_edge() {
        if (start_vertex_kind_id == 2 && end_vertex_kind_id == 6 && edge_direction == 0) return true;
        if (start_vertex_kind_id == 6 && end_vertex_kind_id == 2 && edge_direction == 1) return true;
        return false;
    }
    
    public int compareTo(Object o) {
       EdgeCode obj = (EdgeCode) o;
       if (node_index < obj.getNode_index()) return -1;
       if (node_index == obj.getNode_index()) return 0;
       if (node_index > obj.getNode_index()) return 1;
       return 0;
    }
    
    public int getEdgeDirection() {
        return this.edge_direction;
    }
  
    public int getEnd_Vertex_kind_id() {
        return this.end_vertex_kind_id;
    }
    
    public int getStart_Vertex_Kind_Id() {
        return this.start_vertex_kind_id;
    }
  
    public int getEdge_label() {
        return this.edge_label;
    }
  
    public String getCode() {
        String code = (new Integer(node_index)).toString()+"-"+(new Integer(vertex_label)).toString()+
                      "-" + (new Integer(edge_label)).toString()+"-"+(new Integer(edge_direction)).toString();
        return code;
    }
  
    public int getVertex_label() {
        return this.vertex_label;
    }
  
    public int getNode_index() {
        return this.node_index;
    }
  
    public void setItem(int item) {
       this.item = item;
    }
  
    public int getItem() {
        return item;
    }
 
}

