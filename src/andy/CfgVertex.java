package andy;

public class CfgVertex {
    int vertex_key;
    int neighbor_num = 0;
    int[][] neighbors;
    int idx = 0;

    public CfgVertex(int vertex_key, int neighbor_num) {
        this.neighbor_num = neighbor_num;
        this.vertex_key = vertex_key;
        this.neighbors = new int[neighbor_num][2];
    }
    
    public int[][] getNeighbors() {
        return this.neighbors;
    }
    
    public int getNeighbor_num() {
        return this.neighbor_num;
    }
    
    public void setNeighbor( int neighbor_key, int startline) {
        neighbors[idx][0] = neighbor_key;
        neighbors[idx][1] = startline;
        idx++;
    }
    
    
}
