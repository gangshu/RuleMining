package andy;

public class MissedInformation {
    int missed_node = 0;
    int missed_data_edge = 0;
    int missed_control_edge = 0;
    int missed_sdd_edge = 0;
    public MissedInformation() {
    }
    
    public void assignMissedNode(int missed_node) {
        this.missed_node = missed_node;
    }
    
    public void addMissedDataEdge(int num) {
        missed_data_edge = missed_data_edge + num;
    }
    
    public void addMissedControlEdge(int num) {
        missed_control_edge = missed_control_edge + num;
    }    
    
    public void addMissedSddEdge(int num) {
        missed_sdd_edge = missed_sdd_edge + num;
    }    
    
    public int getMissedNode() {
        return this.missed_node;
    }
    
    public void addMissedDataEdge() {
        this.missed_data_edge++;
    }
    
    public int getMissedDataEdge() {
        return this.missed_data_edge;
    }
    
    public void addMissedControlEdge() {
        this.missed_control_edge++;
    }
    
    public int getMissedControlEdge() {
        return this.missed_control_edge;
    }
    
    public void addMissedSddEdge() {
        this.missed_sdd_edge++;
    }
    
    public int getMissedSddEdge() {
        return this.missed_sdd_edge;
    }
    
}
