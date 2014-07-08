package andy;

public class Vertex {

    int node_index;
    int vertex_key;
    int vertex_label;
    int vertex_kind_id;
    int startline;
    int distance_to_candidate;
    String fieldname1;
    String fieldname2;
    int pdg_id;
    String vertex_char = null;
    boolean extendable = true;
    int rids_node_index;
    
    
    public Vertex(int node_index, int vertex_key, int vertex_label, int vertex_kind_id, int startline, int distance, 
                  String fieldname1, String fieldname2, int pdg_id) {
        this.node_index = node_index;
        this.vertex_key = vertex_key;
        this.vertex_label = vertex_label;
        this.vertex_kind_id  = vertex_kind_id;
        this.startline = startline;
        this.distance_to_candidate = distance;
        if (fieldname1 == null) this.fieldname1 = "";
        else                    this.fieldname1 = fieldname1;
        if (fieldname2 == null) this.fieldname2 = "";
        else                    this.fieldname2 = fieldname2;
        this.pdg_id = pdg_id;
    }
    
    public Vertex(int vertex_key, int vertex_label) {
        this.vertex_key = vertex_key;
        this.vertex_label = vertex_label;
    }
    
    public void setVertex_char(String data) {
        this.vertex_char = data;
    }
    
    public Integer getPdg_id_Integer() {
        return new Integer(pdg_id);
    }
    
    public void setRids_node_index(int index) {
        this.rids_node_index = index;
    }
    
    public int getRids_node_index() {
        return this.rids_node_index;
    }
    
    public void setDistance_to_candidate(int distance) {
        this.distance_to_candidate = distance;
    }
    
    public String getVertex_char() {
        return this.vertex_char;
    }    
    
    public int getVertex_label() {
        return this.vertex_label;
    }
    
    public int getVertex_kind_id() {
        return this.vertex_kind_id;
    }
    
    public Integer getVertex_key_Integer() {
        return new Integer(vertex_key);
    }
    
    public int getStartline() {
        return this.startline;
    }
    
    public int getDistance_to_candidate() {
        return this.distance_to_candidate;
    }
    
    public int getPdg_id() {
        return this.pdg_id;
    }
    
    public String getFieldname1() {
        return this.fieldname1;
    }
    
    public String getFieldname2()  {
        return this.fieldname2;
    }
    
    public int getNode_index() {
        return node_index;
    }
    
    public int getVertex_key() {
        return vertex_key;
    }

    public boolean isExtendable() {
        return this.extendable;
    }
    
    public void setNotExtendable() {
        this.extendable = false;
    }  
    
    public void setNode_index(int idx) {
        this.node_index = idx;
    }    
    
    public void setPdg_id(int pdg_id) {
        this.pdg_id = pdg_id;
    }
    
    public void setStartline(int line) {
        this.startline = line;
    }
    
    public void setVertex_kind_id(int kind) {
        this.vertex_kind_id = kind;
    }
    
}
