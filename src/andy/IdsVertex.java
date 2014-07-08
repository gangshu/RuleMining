package andy;

import java.util.Hashtable;

public class IdsVertex {

    int ids_index;
    int vertex_key;
    int vertex_label;
    int vertex_kind_id;
    int pdg_id;
    int startline;
    boolean ids_frq_label = false;
    boolean sddWithExpressionNode = false;
    int distance_to_candidate;
    String fieldname1 = "";
    String fieldname2 = "";
    
    Hashtable sources = new Hashtable();
    Hashtable targets = new Hashtable();
    
    public IdsVertex(int ids_index, int vertex_key, int vertex_label, int vertex_kind_id, int pdg_id, int startline) {
        this.ids_index = ids_index;
        this.vertex_key = vertex_key;
        this.vertex_label = vertex_label;
        this.vertex_kind_id = vertex_kind_id;
        this.pdg_id = pdg_id;    
        this.startline = startline;
    }
    
    public void setFieldName1(String data) {
        if (data == null) data = "";
        this.fieldname1 = data;
    }
    
    public void setSddEdgeWithExpressionNode() {
        this.sddWithExpressionNode = true;
    }
    
    public boolean hasSddEdgeWithExpressionNode() {
        return this.sddWithExpressionNode;
    }
    
    public void setFieldName2(String data) {
        if (data == null) data = "";
        this.fieldname2 = data;
    }    
    
    public String getFieldname1() {
        return this.fieldname1;
    }
    
    public Integer getVertex_key_integer() {
        return new Integer(vertex_key);
    }
    
    public String getFieldname2() {
        return this.fieldname2;
    }
    
    public int intrestedSDDFromAItoAI(IdsVertex ai_node, RIDSSetPhaseTwo ridsSet) {
        String ai_fieldname = ai_node.getFieldname1();
        if (ai_fieldname.equals("") && fieldname1.equals("")) return 2;
        if (!ai_fieldname.equals("") && !fieldname1.equals("") && ai_fieldname.equals(fieldname1)) return 2;
        if (ai_fieldname.equals("")  && !fieldname1.equals("")) {
            String key = "A-NA"+fieldname1;
            Hashtable sdd_labels_hash = ridsSet.getSdd_labels_hash();
            Integer sdd_label = (Integer) sdd_labels_hash.get(key);
            if (sdd_label == null) {
                sdd_label = new Integer(ridsSet.getSdd_label_index());
                ridsSet.increase_Sdd_label_index();
                sdd_labels_hash.put(key,sdd_label);
            }
            return sdd_label.intValue();
                           
        }
        if (!ai_fieldname.equals("") &&  fieldname1.equals("")) { 
            String key = "NA-A"+fieldname1;
            Hashtable sdd_labels_hash = ridsSet.getSdd_labels_hash();
            Integer sdd_label = (Integer) sdd_labels_hash.get(key);
            if (sdd_label == null) {
                sdd_label = new Integer(ridsSet.getSdd_label_index());
                ridsSet.increase_Sdd_label_index();
                sdd_labels_hash.put(key,sdd_label);
            }
            return sdd_label.intValue();           
        }            
        return 0;
    }
    
    public int intrestedSDDFromControlToAI(IdsVertex ai_node, RIDSSetPhaseTwo ridsSet) {
        String ai_fieldname = ai_node.getFieldname1();
        if (ai_fieldname.equals("") && fieldname1.equals("")) return 2;
        if (!ai_fieldname.equals("") && !fieldname1.equals("") && ai_fieldname.equals(fieldname1)) return 2;
        if (ai_fieldname.equals("")  && !fieldname1.equals("")) {
            String key = "C-NA"+fieldname1;
            Hashtable sdd_labels_hash = ridsSet.getSdd_labels_hash();
            Integer sdd_label = (Integer) sdd_labels_hash.get(key);
            if (sdd_label == null) {
                sdd_label = new Integer(ridsSet.getSdd_label_index());
                ridsSet.increase_Sdd_label_index();
                sdd_labels_hash.put(key,sdd_label);
            }
            return sdd_label.intValue();
                           
        }
        if (!ai_fieldname.equals("") &&  fieldname1.equals("")) { 
            String key = "NC-A"+fieldname1;
            Hashtable sdd_labels_hash = ridsSet.getSdd_labels_hash();
            Integer sdd_label = (Integer) sdd_labels_hash.get(key);
            if (sdd_label == null) {
                sdd_label = new Integer(ridsSet.getSdd_label_index());
                ridsSet.increase_Sdd_label_index();
                sdd_labels_hash.put(key,sdd_label);
            }
            return sdd_label.intValue();           
        }        
        return 0;
    }
    
    public int intrestedSDDFromControlToExpression(IdsVertex expr_node, RIDSSetPhaseTwo ridsSet) {
        String expr_node_fieldname = expr_node.getFieldname1();
        if (expr_node_fieldname.equals("") && fieldname1.equals("")) return 2;
        if (!expr_node_fieldname.equals("") && !fieldname1.equals("") && expr_node_fieldname.equals(fieldname1)) return 2;
        return 0;
    }    
    
    public Hashtable getTargetNodes() {
        return this.targets;
    }
    
    public void setDistance_to_candidate(int distance) {
        if (this.distance_to_candidate == 0) {
            this.distance_to_candidate = distance;
            return;
        }
        if (this.distance_to_candidate > distance) this.distance_to_candidate = distance;
    }
    
    public int getDistance_to_candidate() {
        return this.distance_to_candidate;
    }
    
    public void unsetIds_frq_label() {
        this.ids_frq_label = false;
    }
    
    public Hashtable getSourceNodes() {
        return this.sources;
    }
    
    
    public Integer getPdg_id_key() {
        return new Integer(pdg_id);
    }
    
    public int getStartline() {
        return this.startline;
    }
    
    public void setIds_frq_label() {
        this.ids_frq_label = true;
    }
    
    public boolean isIds_frq_label() {
        return this.ids_frq_label;
    }
    
    public int getIds_index() {
        return this.ids_index;
    }
    
    public int getVertex_key() {
        return this.vertex_key;
    }
    
    public int getVertex_label() {
        return this.vertex_label;
    }
    
    public int getPdg_id() {
        return this.pdg_id;
    }
    
    public int getVertex_kind_id() {
        return this.vertex_kind_id;
    }

    public void addSingleSourceNeighbor(int src_vertex_key, int src_pdg_id, int edge_type,int len) {
        Edge edge = (Edge) sources.get(new Integer(src_vertex_key));
        if (edge == null) {
            edge = new Edge(src_vertex_key,src_pdg_id,this.vertex_key,this.pdg_id);
            edge.setEdgeInfo(edge_type,len);
            sources.put(new Integer(src_vertex_key),edge);
        } else {
            edge.setEdgeInfo(edge_type,len);
        }
    }
    
    public void setIndex(int index) {
        this.ids_index = index;
    }
    
    public void addSingleTargetNeighbor(int tar_vertex_key, int tar_pdg_id, int edge_type, int len) {
        Edge edge = (Edge) targets.get(new Integer(tar_vertex_key));
        if (edge == null) {
            edge = new Edge(this.vertex_key,this.pdg_id,tar_vertex_key,tar_pdg_id);
            edge.setEdgeInfo(edge_type,len);
            targets.put(new Integer(tar_vertex_key),edge);
        } else {
            edge.setEdgeInfo(edge_type,len);
        }
    }   

    
}
