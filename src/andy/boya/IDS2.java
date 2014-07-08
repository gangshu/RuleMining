package andy.boya;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import andy.*;

public class IDS2 extends IDS {
	static int levelP = 6;
	static int levelC = -1;
	Hashtable<Integer, Integer> excludedPdgs;
	public IDS2(int key_node_vertex_key, int key_node_pdg_id, javax.sql.DataSource dataSource, Pattern pattern) throws SQLException 
	{
		super(key_node_vertex_key, key_node_pdg_id, dataSource, pattern);
		excludedPdgs = new Hashtable<Integer, Integer>();
	}
	
	@Override
    protected boolean isIntrestingPdg(Vertex new_node, Vertex current_node, boolean direction) throws SQLException {
		/**
		 * 1. If the PDG is already an interesting PDG, no need to do further checkings
		 */
        Object obj = getIds_pdg_set().get(new_node.getPdg_id_Integer()); 
        if (obj != null) return true;
        /**
         * 2. From the current node, get the PDG of the new node and the level of the PDG
         */
        PdgForPhaseOne current_node_pdg = (PdgForPhaseOne) getIds_pdg_set().get(current_node.getPdg_id_Integer());
        PdgForPhaseOne new_node_pdg = null;
        /**
         * 2.1 The first case: new node is in the children function, then the level is one less then the current node
         */
        if(
        	(current_node.getVertex_kind_id() == 1 && new_node.getVertex_kind_id()==11) ||
        	(current_node.getVertex_kind_id() == 2 && new_node.getVertex_kind_id()==12)
           )
        {
        	new_node_pdg = new PdgForPhaseOne(new_node.getPdg_id(),current_node.getPdg_id(),current_node_pdg.getLevel()-1,true);
        }
        /**
         * 2.2 The second case: new node is in the parent function, then the level is one more then the current node
         * 
         * NOTE: Only nodes in the base function and parent functions of the base function can backtrace;
         * Nodes in children functions are not allowed to backtrace, since this may lead to parent functions of the
         * base function, which makes the analysis imprecise
         * 
         * For example, consider the call graph: 
         * a->b
         * b->c
         * a->c
         * 
         * b is the base function; c is the children; if backtrace allows, c could backtrace to a. However, we need to consider
         * strictly the children functions, not the calling context 
         */
        else if(
            	((current_node.getVertex_kind_id() == 11 && new_node.getVertex_kind_id()==1) ||
            	(current_node.getVertex_kind_id() == 12 && new_node.getVertex_kind_id()==2)) &&
            	current_node_pdg.getLevel()>=0 //Only nodes in the base function and its parent functions are allowed to backtrace
               )
            {
            	new_node_pdg = new PdgForPhaseOne(new_node.getPdg_id(),current_node.getPdg_id(),current_node_pdg.getLevel()+1,true);
            }
        else
        	return false;
        
        /**
         * 3. Make sure the level is within the range
         */
        if (new_node_pdg.getLevel()>levelP || new_node_pdg.getLevel()< levelC) 
        {
        	return false;
        }
              
        /**
         * 4. Exclude PDGs should not be considered in this round
         * 
         * Note: Currently the excluded PDG(s) are a set of direct parent of the base function;
         * so if the current node is in the base function AND the new node is in one of the excluded parent functions,
         * then do not consider it in this round
         */
        if(excludedPdgs!=null)
        {
        	Object pdg = excludedPdgs.get(new_node_pdg.getPdg_id_Key());
        	if(pdg!=null && current_node_pdg.getPdg_id_Key() == getTopped_pdg_id())
        		return false;
        }
        
        /**
         * 5. Check if the node is a global variable 
         */
        int nid = new_node.getVertex_kind_id(); 
        if (nid > 16 || nid < 13) { 
        	getIds_pdg_set().put(new_node_pdg.getPdg_id_Key(),new_node_pdg);
        	/**
        	 * TODO: delete
        	 */
        	//if(new_node_pdg.getLevel() == 1)
        		//System.out.println("PDG considered: "+new_node_pdg.getPdg_id_Key()+" "+new_node_pdg.getP_pdg_id()+" "+new_node_pdg.getLevel());
            return true;
           }
        return false;
    }
	
	public void setExcludedPdg(Hashtable<Integer, Integer> pdgIDs)
	{
		this.excludedPdgs = pdgIDs;
	}
	
	public static void setMAX_VERTICES_NUM_1(int num)
	{
		MAX_VERTICES_NUM_1 = num;
	}
	
	public static void setMAX_CALLSITE_NUM(int num)
	{
		MAX_CALLSITE_NUM=num;
	}
	public static void setForwardSQl(String sql)
	{
		forward_sql=sql;
	}
	public static void setBackwardSQl(String sql)
	{
		backward_sql=sql;
	}
	public static String getForwardSQL()
	{
		return forward_sql;
	}
	
	public static String getBackwardSQL()
	{
		return backward_sql;
	}
	
	public static void setParentLevel(int pLevel)
	{
		levelP = pLevel;
	}
	public static void setChildLevel(int cLevel)
	{
		levelC = cLevel;
	}
}
