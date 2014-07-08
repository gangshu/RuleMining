package andy;

public class RuleInstanceLostEdge {

    int end_point_vertex_key;
    boolean forward;
    int edgetype;

    public RuleInstanceLostEdge(int vertex_key, boolean forward, int edgetype) {
        this.end_point_vertex_key = vertex_key;
        this.forward = forward;
        this.edgetype = edgetype;
    }
    
    public int getEnd_point_vertex_key() {
        return this.end_point_vertex_key;
    }
    
    public boolean isForward() {
        return this.forward;
    }
    
    public int getEdgetype() {
        return this.edgetype;
    }
      
}
