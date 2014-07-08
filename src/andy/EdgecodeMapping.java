package andy;

import java.util.*;


public class EdgecodeMapping {

  int max_item = 0;
  Hashtable mapping_hash = new Hashtable();
  Vector    mapping_list = new Vector();
  public EdgecodeMapping() {}
  
  public int getMax_item() {
    return this.max_item;
  }
  
  // Input key(node_id + edgelabel + nodelabel + edge direction) ---> item (integer)

  public EdgeCode getEdgeCodeByItem(int item) {
    EdgeCode edgecode = (EdgeCode) this.mapping_list.get(item);
    return edgecode;
  }
  
  public int addEdgecodeMapping(EdgeCode edgecode) {
    EdgeCode obj = (EdgeCode) this.mapping_hash.get(edgecode.getCode());
    if (obj == null) {
      edgecode.setItem(this.max_item);
      this.mapping_hash.put(edgecode.getCode(),edgecode);
      this.mapping_list.add(this.max_item,edgecode);
      this.max_item++;
      return edgecode.getItem();
    } else {
      edgecode.setItem(obj.getItem());
      return obj.getItem();
    }
  }
}