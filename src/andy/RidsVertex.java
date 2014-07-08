package andy;

public class RidsVertex extends IdsVertex{
    int rids_index = 0;
    int transitive_distance = 0;
    boolean withDataOrSddEdge = false;
    public RidsVertex(IdsVertex v, int rids_index) {
        super(v.getIds_index(),v.vertex_key,v.getVertex_label(),v.getVertex_kind_id(),v.getPdg_id(),v.getStartline());
        if (v.isIds_frq_label()) this.setIds_frq_label();
        super.setDistance_to_candidate(v.getDistance_to_candidate());
        this.rids_index = rids_index;
    }
    
    public int getRids_index() {
        return this.rids_index;
    }
    
    public void setRids_index(int index) {
        this.rids_index = index;
    }
    
    public void setTransitive_distance(int distance) {
        this.transitive_distance = distance;
    }
    
    public int getTransitive_distance() {
        return this.transitive_distance;
    }
    
    public boolean hasDataOrSddEdge() {
        return this.withDataOrSddEdge;
    }
    
    public void setDataOrSddEdge(boolean withDataOrSddEdge) {
        this.withDataOrSddEdge = withDataOrSddEdge;
    }
    
}
