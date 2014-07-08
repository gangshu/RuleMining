package andy;
import java.util.*;

public class PdgVertices {
    int pdg_id;
    Hashtable vertexposition = new Hashtable();
    Hashtable highlight = new Hashtable();
    int start_line = 200000;
    int end_line = 0;
    
    public PdgVertices(int pdg_id) {
        this.pdg_id = pdg_id;
    }
    
    public int getStart_line() {
        return this.start_line;
    }
    
    public int getEnd_line() {
        return this.end_line;
    }
    
    public String getPdg_id_string()  {
        return Integer.toString(pdg_id);
    }
    
    public void addPdgVertexPosition(VertexPosition position) {
        Object obj = vertexposition.get(new Integer(position.getVertex_key()));
        if (obj == null) {
            vertexposition.put(new Integer(position.getVertex_key()),position);
        }
    }
    
    public void setBoundary(int line1) {
        if (line1 < start_line) start_line = line1;
        if (line1 > end_line) end_line = line1;
    }
    
    public int getPdg_id() {
        return this.pdg_id;
    }
    
    public Hashtable getVertexPosition() {
        return this.vertexposition;
    }
    
    public Hashtable getHighlight() {
        return this.highlight;
    }
    
    public void addHighlight(Integer line) {
        Object obj = highlight.get(line);
        if (obj == null) highlight.put(line,line);
    }
    
}
