package andy;

public class AdjacentEdge {

    int   edge_type;
    int[] forward_neighbors = null;
    int[] backward_neighbors = null;
    int   forward_neighbor_num;
    int   backward_neighbor_num;
    int   forward_index = 0;
    int   backward_index = 0;

    public AdjacentEdge(int edge_type, int forward_neighbor_num, int backward_neighbor_num) {
        this.edge_type = edge_type;
        this.forward_neighbor_num = forward_neighbor_num;
        this.backward_neighbor_num = backward_neighbor_num;
        if (forward_neighbor_num != 0) forward_neighbors = new int[forward_neighbor_num];
        if (backward_neighbor_num != 0) backward_neighbors = new int[backward_neighbor_num];
    }
    
    public int getEdgeType() {
        return this.edge_type;
    }
    
    public int getForward_neighbor_number() {
        return this.forward_neighbor_num;
    }
    
    public int getBackward_neighbor_number() {
        return this.backward_neighbor_num;
    }
    
    public int[] getForward_neighbors() {
        return this.forward_neighbors;
    }
    
    public int[] getBackward_neighbors() {
        return this.backward_neighbors;
    }
    
    public void addForwardNeighbor(int neighbor) {
        this.forward_neighbors[this.forward_index++] = neighbor;
    }
    
    public void addBackwardNeighbor(int neighbor) {
        this.backward_neighbors[this.backward_index++] = neighbor;
    }
    
}
