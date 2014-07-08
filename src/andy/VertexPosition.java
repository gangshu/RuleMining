package andy;


public class VertexPosition {
    int vertex_key;
    int pdg_id;
    int start_line;
    int end_line;

    public VertexPosition(int vertex_key, int pdg_id, int start_line, int end_line)  {
        this.vertex_key = vertex_key;
        this.pdg_id = pdg_id;
        this.start_line = start_line;
        this.end_line = end_line;
    }

    public int getVertex_key() {
        return vertex_key;
    }

    public void setVertex_key(int newVertex_key) {
        vertex_key = newVertex_key;
    }

    public int getPdg_id() {
        return pdg_id;
    }

    public void setPdg_id(int newPdg_id) {
        pdg_id = newPdg_id;
    }

    public int getStart_line() {
        return start_line;
    }

    public void setStart_line(int newStart_line) {
        start_line = newStart_line;
    }

    public int getEnd_line() {
        return end_line;
    }

    public void setEnd_line(int newEnd_line) {
        end_line = newEnd_line;
    }
}
