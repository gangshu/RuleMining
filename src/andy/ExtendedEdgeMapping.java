package andy;
import java.util.*;
public class ExtendedEdgeMapping {
  int max_item = 0;
  Hashtable mapping_hash = new Hashtable();
  Vector  mapping_list = new Vector();
  public ExtendedEdgeMapping() {
  }
  
  public ExtendedEdge getExtendedEdgeByItem(int item) {
    ExtendedEdge ee = (ExtendedEdge) this.mapping_list.get(item);
    return ee;
  }
  
  public int addEdgecodeMapping(ExtendedEdge ee) {
    ExtendedEdge obj = (ExtendedEdge) this.mapping_hash.get(ee.getKey());
    if (obj == null) {
      ee.setItem(this.max_item);;
      this.mapping_hash.put(ee.getKey(),ee);
      this.mapping_list.add(this.max_item,ee);
      this.max_item++;
      return ee.getItem();
    } else {
      ee.setItem(obj.getItem());
      return obj.getItem();
    }
  }  
  
  
}
