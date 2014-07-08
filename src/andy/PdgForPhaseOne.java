package andy;

public class PdgForPhaseOne {
    int p_pdg_id;
    int pdg_id;
    int level = 0;
    boolean involved_in_ids = false;

    public PdgForPhaseOne(int pdg_id, int p_pdg_id, int level, boolean involved_in_ids) {
        this.pdg_id = pdg_id;
        this.level = level;
        this.involved_in_ids = involved_in_ids;
        this.p_pdg_id = p_pdg_id;
    }
    
    public void setP_pdg_id(int p_pdg_id) {
        this.p_pdg_id = p_pdg_id;
    }
    
    public int getP_pdg_id() {
        return this.p_pdg_id;
    }
    
    public Integer getPdg_id_Key() {
        return new Integer(this.pdg_id);
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setInvolved_in_ids() {
        this.involved_in_ids = true;
    }
    
    public boolean isInvolved_in_ids() {
        return this.involved_in_ids;
    }
    
}
