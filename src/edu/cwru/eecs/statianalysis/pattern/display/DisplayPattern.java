package edu.cwru.eecs.statianalysis.pattern.display;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import edu.cwru.eecs.statianalysis.util.ComparablePatternInstances;
import edu.cwru.eecs.statianalysis.dao.VertexDao;
import edu.cwru.eecs.statianalysis.dao.ViolationDao;
import edu.cwru.eecs.statianalysis.dao.springimpl.VertexDaoSpringImpl;
import edu.cwru.eecs.statianalysis.dao.springimpl.ViolationDaoSpringImpl;
import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.RuleInstance;
import edu.cwru.eecs.statianalysis.pattern.Violation;
import edu.cwru.eecs.statianalysis.service.RuleService;
import edu.cwru.eecs.statianalysis.service.ViolationService;

public class DisplayPattern <V extends Vertex, E extends Edge<V>>{
	
	int patternKey;
	String src;
	DataSource ds;
	RuleService<V, E> rs;
	String textToDisplay;
	Rule<V, E> r;
	VertexDao<V> vertexDao;
	ViolationDao violationDao;
	public DisplayPattern(int patternKey, String src, DataSource ds)
	{
		this.patternKey = patternKey;
		this.src = src;
		this.ds = ds;
		rs = new RuleService<V, E>(ds,patternKey); 
		r = rs.getRule();
		vertexDao = new VertexDaoSpringImpl<V>(ds);
		violationDao = new ViolationDaoSpringImpl(ds);
		
	}
	
	public void computeDisplayText()
	{
		textToDisplay = "("+r.getPatternKey()+")\n";
		textToDisplay = textToDisplay+"Description: "+r.getComments()+"\n";
		textToDisplay = textToDisplay+"Key Functions: "+this.getFuncInvolved()+"\n";
		textToDisplay = textToDisplay + "Occurrences: "+r.getFrequency()+"\n";
		float ratioVio = -1;
		if((r.getNum_matches()+r.getNum_mismtaches())!=0)
		{	
			ratioVio = (float)r.getNum_mismtaches()/(float)(r.getNum_matches()+r.getNum_mismtaches());
			textToDisplay = textToDisplay + "Ratio of Deviants:" +ratioVio+"\n";
		}
		textToDisplay = textToDisplay+"Pattern instances: \n";
		
		/**
		 * Display pattern instances
		 */
		DisplayPatternInstance<V,E,Rule<V, E>> dpi = new DisplayPatternInstance<V,E,Rule<V, E>>(r, src, ds);
		List<RuleInstance<V, E>> riList = this.getRuleInstancesToDisplay();
		for(int i=0; i<riList.size();i++)
		{
			//System.out.println(i);
			String dpiText;
			RuleInstance<V, E> ri = riList.get(i);
			dpi.setRuleInstance(ri);
			dpi.computeDisplayText();
			dpiText = dpi.getText();
			//System.out.println(dpiText);
			//Preprocesss
			dpiText = "\t"+dpiText;
			dpiText =  dpiText.replaceAll("\n", "\n\t");
			dpiText = dpiText.substring(0, dpiText.length()-1);
			//Assemble
			textToDisplay = textToDisplay + "\t ---- "+i+" ----\n"+ dpiText;
		}
		/**
		 * Display violation instances
		 */
		//System.out.println(r.getNum_mismtaches());textToDisplay = textToDisplay + "Deviants:\n";		
		List<Map<String, Object>> vioKeys = violationDao.getViolationsForPattern(r.getPatternKey(), "Y");
		System.out.println(vioKeys.size());
		ViolationService vioService;
		if(vioKeys.size()!=0)
		{
			textToDisplay = textToDisplay + "Deviants:\n";
            for(int i=0; i<vioKeys.size();i++)
            {
                Map<String, Object> map = vioKeys.get(i);
                String dpiText = "";
                /**
                * Very weird exception:
                * Cannot convert from java.math.BigDecimal to int
                */
                int vioKey = ((BigDecimal)map.get("VIOLATION_KEY")).intValue();
                vioService = new ViolationService(DBUtil.getDataSource(), vioKey);
                Violation vio = vioService.getViolatoin();
                DisplayVioInstance dvi = new DisplayVioInstance(r, src, DBUtil.getDataSource());
                dvi.setViolatoin(vio);
                dvi.computeDisplayText();
                dpiText = dvi.getText();
                dpiText = "\t"+dpiText;
                dpiText =  dpiText.replaceAll("\n", "\n\t");
                dpiText = dpiText.substring(0, dpiText.length()-1);
                //Assemble
                textToDisplay = textToDisplay + "\t ---- "+i+" ----\n"+ dpiText + "\n";
                textToDisplay = textToDisplay+"\t"+"Comments: "+vio.getComments()+"\n";
            }
		}	
		//System.out.println(textToDisplay);
	}
	
	@SuppressWarnings("unchecked")
	private List<RuleInstance<V, E>> getRuleInstancesToDisplay()
	{
		List<RuleInstance<V, E>> riList = r.getRuleInstanceList();
		List<ComparablePatternInstances<V, E, RuleInstance<V, E>>> criList = new ArrayList<ComparablePatternInstances<V, E, RuleInstance<V, E>>>();
		if(riList.size()<=3)
			return riList;
		for(int i=0; i<riList.size();i++)
		{
			RuleInstance<V, E> ri = riList.get(i);
			ComparablePatternInstances<V, E, RuleInstance<V, E>> cri = new ComparablePatternInstances<V, E, RuleInstance<V, E>>(ri);
			criList.add(cri);
		}
		Collections.sort(criList);
		riList.clear();
		for(int i=0; i<3; i++)
		{
			riList.add(criList.get(i).getRuleInstance());
		}
		return riList;
	}
	
	public String getFuncInvolved()
	{
		Set<V> vSet = r.getPatternGraph().getAllvertices();
		Set<Integer> vLabels = new HashSet<Integer>();
		Iterator<V> vIt = vSet.iterator();
		String ret = "";
		while(vIt.hasNext())
		{
			V v = vIt.next();
			if(v.getVertexKindId().equals("5"))
				vLabels.add(Integer.valueOf(v.getVertexLabel()));
		}
		
		Iterator<Integer> vLabelIt = vLabels.iterator();
		while(vLabelIt.hasNext())
		{
			int vLabel = vLabelIt.next();
			String pdgName = "";
			if(vertexDao.getEntVertices(r.getCandidate_node_label()).size() != 0)
			{
				Map<String, Object> pdgAndFileName = this.vertexDao.getPdgAndFileNameFromCallsiteLabel(vLabel);
				pdgName = (String)pdgAndFileName.get("PDG_NAME");
			}
			ret = ret+pdgName + ", ";
		}
		return ret.substring(0, ret.length());
	}
	
	public String textToDisplay()
	{
		return this.textToDisplay;
	}

}
