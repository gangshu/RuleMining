package andy;

public class ExtendedIDSInformation {
    int vertex_key;
    int p_pdg_id;
    public ExtendedIDSInformation(int vertex_key, int p_pdg_id) {
        this.vertex_key = vertex_key;
        this.p_pdg_id = p_pdg_id;
    }
    
    public int getP_Pdg_id() {
        return this.p_pdg_id;
    }
    
    public int getVertex_key() {
        return this.vertex_key;
    }
}
