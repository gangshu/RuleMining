package andy;

public class Edge {
    int[] edge_type = new int[6]; //0: data, 1:control, 2: inter data 3: inter control 4: mixed 5: shared data dependence 
    int src_vertex_key;
    int src_pdg_id;
    int tar_vertex_key;
    int tar_pdg_id;
    public Edge(int p_src_vertex_key, int p_src_pdg_id, int p_tar_vertex_key,  int p_tar_pdg_id) {
       this.src_vertex_key = p_src_vertex_key;
       this.tar_vertex_key = p_tar_vertex_key;
       this.src_pdg_id = p_src_pdg_id;
       this.tar_pdg_id = p_tar_pdg_id;
       for (int i=0; i < edge_type.length; i++) edge_type[i] = 0;
    }
    
    public int getSrc_veretx_key() {
      return this.src_vertex_key;
    }
    
    public boolean suitableForAddingMixedEdge(int len) {
        if (edge_type[0]!=0 || edge_type[1]!=0) return false;
        if (edge_type[4]==0)  return true;    
        if (len < edge_type[4]) return true;    
        return false;
    }
    
    
    public int getSrc_pdg_id() {
      return this.src_pdg_id;
    }
    
    public int getTar_vertex_key() {
      return this.tar_vertex_key;
    }
    
    public int getTar_pdg_id() {
      return this.tar_pdg_id;
    }
    public int getEdgeInfo(int type) {
      return this.edge_type[type];
    }    
    
    public void setEdgeInfo(int type, int len) {
      this.edge_type[type] = len;
    }

    public int[] getAlledges() {
      return this.edge_type;
    }

}

