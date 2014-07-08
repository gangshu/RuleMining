package andy;

public class ExtendedEdge {
  int src_idx;
  int tar_idx;
  int edge_label;
  int item = 0;

  public ExtendedEdge(int p_src_idx, int p_tar_idx, int p_edge_label){
    this.src_idx = p_src_idx;
    this.tar_idx = p_tar_idx;
    this.edge_label = p_edge_label;
  }
  
  public void setItem(int p_item) {
     this.item = p_item;
  }
  
  public int getItem() {
     return this.item;
  }
  public String getKey() {
    return Integer.toString(this.src_idx)+"-"+Integer.toString(this.tar_idx)+"-"+
                            Integer.toString(this.edge_label);
  }
  
  public int getSrc_idx()
  {
    return src_idx;
  }

  public void setSrc_idx(int newSrc_idx)
  {
    src_idx = newSrc_idx;
  }

  public int getTar_idx()
  {
    return tar_idx;
  }

  public void setTar_idx(int newTar_idx)
  {
    tar_idx = newTar_idx;
  }

  public int getEdge_label()
  {
    return edge_label;
  }

  public void setEdge_label(int newEdge_label)
  {
    edge_label = newEdge_label;
  }
}
