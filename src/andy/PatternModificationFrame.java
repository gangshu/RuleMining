package andy;

import java.awt.TextField;

import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import oracle.jdeveloper.layout.XYLayout;
import javax.swing.JButton;
import oracle.jdeveloper.layout.XYConstraints;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.JTextField;

import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.service.GraphService;
import edu.cwru.eecs.statianalysis.service.RulePreprocessService;
import edu.cwru.eecs.statianalysis.service.RuleService;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.*;
import oracle.sql.*;
import oracle.jdbc.*;
import y.view.Graph2D;

import java.util.*;
import java.io.*;

public class PatternModificationFrame extends JFrame {
    static String update_pattern_status = "Update pattern set confirm = ?, modification = ?, remark = ? where pattern_key = ?";
    static String update_pattern_status2 = "Update pattern_info set REVIEWER = ?, RANKING_CODE  = ? where pattern_key = ?";
    static String update_pattern_review_time = "Update pattern_info set REVIEW_TIME = ? where pattern_key = ? and review_time is null";
    static String spacechar = "       ";
    private JPanel jPanel1 = new JPanel();
    private XYLayout xYLayout1 = new XYLayout();
    private JButton btnExecution = new JButton();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JLabel jLabel_pattern_key = new JLabel();
    private JEditorPane displayArea = new JEditorPane();
    private JTextField field_pattern_key = new JTextField();
    private String data = null;
    private JScrollPane edgepane = new JScrollPane();
    private JTable edgedata;
    private JButton btn_del_src_node = new JButton();
    CandidatePattern cp;
    int pattern_key;
    private JButton btn_del_single_link = new JButton();
    private JButton btn_save = new JButton();
    private JButton btn_del_tar_node = new JButton();
    private JButton btn_confirm_pattern = new JButton();
    private JButton btn_visualize = new JButton();
    private JTextField delete_from = new JTextField();
    private JTextField delete_to = new JTextField();
    private JButton btn_delete_range = new JButton();
    private JButton btn_auto_trim = new JButton();
    private JTextField field_confirm = new JTextField();
    private JLabel jLabel_confirm = new JLabel();
    private JLabel jLabel_mod = new JLabel();
    private JLabel jLabel_suggestedLabel = new JLabel();
    private JLabel jLabel_remarks = new JLabel();
    private JLabel jLabel_RankCode = new JLabel();
    private JLabel jLabel_reviewer = new JLabel();
    private JTextField field_reviewer = new JTextField();
    private JTextField field_modification = new JTextField();
    private JTextField field_suggestedLabel = new JTextField();
    private JTextField field_rankingCode = new JTextField();
    private JTextPane pane_remarks = new JTextPane();
    private JTextField field_user = new JTextField();
    private JTextField field_passwd = new JTextField();
    private JTextField field_jdbc = new JTextField();
    private JButton jButton_load_db = new JButton();
    String user;
    String passwd;
    String jdbc;
    private TextField mode = new TextField();
    private TextField line_num = new TextField();
    
    //Used to store the review time
    private java.util.Date startDate, endDate;
    Hashtable<String, String> htFileNames = new Hashtable<String, String>();

    public PatternModificationFrame() {
        try {
            DriverManager.registerDriver(new OracleDriver());   
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
        htFileNames.put("og","openssl-0.9.8g");
        htFileNames.put("apache_boya","httpd-2.2.8");
        htFileNames.put("python_2_5_2","Python-2.5.2");
        htFileNames.put("abb_cir_cirld_5_10","rel5_10.0146.release");
        htFileNames.put("snmp","snmp-5.3.2");
        htFileNames.put("ac800m","AC800M\\source");
    }
  
    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(this.jdbc,this.user,this.passwd);     
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }  

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(1035, 860));
        jPanel1.setBounds(new Rectangle(5, 5, 1030, 860));
        jPanel1.setBackground(new Color(142, 232, 236));
        jPanel1.setLayout(xYLayout1);
        btnExecution.setText("Execution");
        btnExecution.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnExecution_actionPerformed(e);
            }
        });
        jLabel_pattern_key.setText("Pattern ID: ");
        jLabel_pattern_key.setFont(new Font("·s²Ó©úÅé", 0, 16));
        btn_del_src_node.setText("Delete Source Node");
        btn_del_src_node.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_del_src_node_actionPerformed(e);
            }
        });
        btn_del_single_link.setText("Delete Single Link");
        btn_del_single_link.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                btn_del_single_link_actionPerformed(e);
            }
        });
        btn_save.setText("Save to Database");
        btn_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_save_actionPerformed(e);
            }
        });
        btn_del_tar_node.setText("Delete Target Node");
        btn_del_tar_node.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_del_tar_node_actionPerformed(e);
            }
        });
        btn_confirm_pattern.setText("Confirm Pattern");
        btn_confirm_pattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_confirm_pattern_actionPerformed(e);
            }
        });
        delete_from.setText("0");
        delete_to.setText("0");
        btn_delete_range.setText("Delete");
        btn_delete_range.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_delete_range_actionPerformed(e);
            }
        });
        btn_auto_trim.setText("Auto Trim");
        btn_auto_trim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_auto_trim_actionPerformed(e);
            }
        });
        btn_visualize.setText("Visusalize");
        btn_visualize.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e)
        		{
        			visualizeRulePattern();
        		}
        	}
        );
        jLabel_confirm.setText("Confirm");
        jLabel_mod.setText("Modification");
        jLabel_remarks.setText("Remarks");
        jLabel_suggestedLabel.setText("Suggested Label");
        jLabel_RankCode.setText("Rank Code");
        jLabel_reviewer.setText("Reviewer");
        field_user.setText("ac800m");
        field_passwd.setText("andypodgurski");
        field_jdbc.setText("jdbc:oracle:thin:@selserver.cwru.edu:1521:orcl");
        jButton_load_db.setText("load db");
        jButton_load_db.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        mode.setText("1");
        line_num.setText("1");
        jPanel1.add(jLabel_pattern_key, new XYConstraints(885, 15, 151, 20));
        jPanel1.add(field_pattern_key, new XYConstraints(955, 10, 60, 30));
        jPanel1.add(btnExecution, new XYConstraints(880, 45, 135, 25));
        jPanel1.add(btn_del_src_node, new XYConstraints(880, 75, 135, 25));
        jPanel1.add(btn_del_tar_node, new XYConstraints(880, 105, 135, 25));
        jPanel1.add(btn_del_single_link, new XYConstraints(880, 135, 135, 25));
        jPanel1.add(btn_save, new XYConstraints(880, 165, 135, 25));
        
        jPanel1.add(jLabel_suggestedLabel, new XYConstraints(880, 195, 95, 20));
        jPanel1.add(field_suggestedLabel, new XYConstraints(975, 195, 30, 25));
        field_suggestedLabel.setEditable(false);
        jPanel1.add(jLabel_confirm, new XYConstraints(880, 220, 90, 20));
        jPanel1.add(field_confirm, new XYConstraints(975, 220, 30, 25));
        jPanel1.add(jLabel_mod, new XYConstraints(880, 245, 90, 20));
        jPanel1.add(field_modification, new XYConstraints(975, 245, 30, 25));
        jPanel1.add(jLabel_RankCode, new XYConstraints(880, 270, 90, 20));
        jPanel1.add(field_rankingCode, new XYConstraints(975, 270, 30, 25));
        jPanel1.add(jLabel_remarks, new XYConstraints(880, 295, 90, 20));
        JScrollPane scrollPane_remarks = new JScrollPane(pane_remarks);
        jPanel1.add(scrollPane_remarks, new XYConstraints(880, 315, 135, 75));
        jPanel1.add(jLabel_reviewer, new XYConstraints(880, 395, 50, 20));
        jPanel1.add(field_reviewer, new XYConstraints(935, 395, 70, 25));
        /*jPanel1.add(btn_delete_range, new XYConstraints(880, 395, 65, 25));
        jPanel1.add(delete_to, new XYConstraints(990, 395, 35, 25));
        jPanel1.add(delete_from, new XYConstraints(950, 395, 35, 25));
        jPanel1.add(btn_auto_trim, new XYConstraints(880, 425, 135, 25));*/
        
        jPanel1.add(btn_confirm_pattern, new XYConstraints(880, 425, 135, 25));
        jPanel1.add(btn_visualize, new XYConstraints(880, 455, 135, 25));
        jPanel1.add(field_passwd, new XYConstraints(950, 485, 65, 30));
        jPanel1.add(field_user, new XYConstraints(880, 485, 60, 30));
        jPanel1.add(field_jdbc, new XYConstraints(880, 520, 140, 25));
        jPanel1.add(jButton_load_db, new XYConstraints(880, 550, 75, 20));
        jPanel1.add(mode, new XYConstraints(965, 550, 20, 20));
        jPanel1.add(line_num, new XYConstraints(990, 550, 20, 20));
        jPanel1.add(edgepane, new XYConstraints(0, 575, 1025, 255));
        
        jScrollPane1.getViewport().add(displayArea, null);
        jPanel1.add(jScrollPane1, new XYConstraints(5, 0, 870, 575));  
        this.getContentPane().add(jPanel1, null);
    }

    private void btnExecution_actionPerformed(ActionEvent e) {
        Connection conn = null;
        try { 
            conn = getConnection();
            pattern_key = Integer.parseInt(field_pattern_key.getText());
            cp = new CandidatePattern(conn,pattern_key);
            getPattern_Info(conn,pattern_key);
            showSourceCode(conn);    
            showEdgeInfo();  
            startDate = new java.util.Date();
            //visualizeRulePattern();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e2) {}
        }
    }
    
    private void visualizeRulePattern()
    {
    	DBUtil.createDatasource(this.user);
    	RuleService ruleService = new RuleService(DBUtil.getDataSource(), pattern_key);
    	Rule rule = ruleService.getRule();
        RulePreprocessService rulePreprocessService = new RulePreprocessService(rule);
        rulePreprocessService.getAdjMatrixByType("1");
        rulePreprocessService.removeUselessEdgesByType("1");
        rulePreprocessService.removeUselessVertices();
    	System.out.println("rule: "+rule.getPatternKey());
    	GraphService graphService = new GraphService();
    	Graph2D graph2d = graphService.transformToYGraph(rule.getPatternGraph());
    	JFrame j = new JFrame();
    	j.setName("Visualization of the rule pattern");
    	graphService.visualizeGraph(graph2d, j);
        j.pack();
        j.setVisible(true); 
    }
  
    private void getPattern_Info(Connection conn, int pattern_key) throws SQLException {
        String sql = "SELECT confirm, nvl(modification,''), nvl(remark,'') FROM pattern WHERE pattern_key = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pattern_key);
        ResultSet rset = pstmt.executeQuery();
        if (rset.next()) {
            field_confirm.setText(rset.getString(1));
            field_modification.setText(rset.getString(2));
            pane_remarks.setText(rset.getString(3));
        } else {
            field_confirm.setText("");
            field_modification.setText("");
            pane_remarks.setText("");    
        }
        
        sql = "SELECT REVIEWER, SUGGESTED_LABEL, RANKING_CODE from pattern_info where pattern_key = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1,pattern_key);
        rset = pstmt.executeQuery();
        if (rset.next()) {
            field_reviewer.setText(rset.getString(1));
            field_suggestedLabel.setText(rset.getString(2));
            field_rankingCode.setText(rset.getString(3));
        } else {
            field_confirm.setText("");
            field_modification.setText("");
            pane_remarks.setText("");    
        }
        rset.close();
        pstmt.close();
    }
    
    
    private void showSourceCode(Connection conn) throws SQLException, IOException {
        data = "<html><html><head><title>Pattern Instance in a Program</title></head><body>";
        CandidatePatternInstance[] cpi_list = cp.getCpi_list();
        for (int i=0; i<cp.num_cpi; i++) {
            CandidatePatternInstance cpi = cpi_list[i];
            getPatternInstance(conn, cpi);
            data = data + "<p>=============================================================================" +
                          "=======================</p>";
        }
        data = data + "</body></html>";
        displayArea.setEditorKit(new HTMLEditorKit());
        displayArea.setEditable(true);
        displayArea.setText(data);     
    }
  
    private void showEdgeInfo() {
        int num_vertex = cp.getCp_vertices_num();
        int num_cpi = cp.getNum_cpi();
        int rownum = cp.getCpi_list()[0].getEdge_num() * cp.getNum_cpi();
        int[][][] edges = cp.getCpi_list()[0].getCpiEdges();
        this.edgedata = new JTable(rownum,11);
        edgedata.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.edgepane.getViewport().add(edgedata);
        int row=0;
        CandidatePatternInstance[] cpi_list = cp.getCpi_list();
        
        String display_mode = mode.getText();
    
        if (display_mode.equals("1")) {
            for (int i=0; i<num_vertex; i++) {
                for (int j=0; j<num_vertex; j++) {
                    for (int k=0; k<3; k++) {
                        if (edges[i][j][k] > 0) {
                            for (int m=0; m<num_cpi; m++) {
                                Vertex src = cpi_list[m].getVertexByNodeIndex(i);
                                Vertex tar = cpi_list[m].getVertexByNodeIndex(j);
                                displayOneRowInfo(row,src,tar,k);
                                row++;
                            }
                        }
                    }
                }
            }
        } else {
            for (int m=0; m<num_cpi; m++) {
                for (int i=0; i<num_vertex; i++) {
                    for (int j=0; j<num_vertex; j++) {
                        for (int k=0; k<3; k++) {
                            if (edges[i][j][k] > 0) {                    
                                Vertex src = cpi_list[m].getVertexByNodeIndex(i);
                                Vertex tar = cpi_list[m].getVertexByNodeIndex(j);
                                displayOneRowInfo(row,src,tar,k);
                                row++;
                            }
                        }
                    }
                }
            }           
        }
    }
  
    private void displayOneRowInfo(int row, Vertex src, Vertex tar, int k) {
        edgedata.setValueAt(new Integer(src.getNode_index()),row,0);
        edgedata.setValueAt(new Integer(src.getStartline()),row,1);
        edgedata.setValueAt(src.getVertex_char(),row,2);
        edgedata.setValueAt(VertexKindMapping.getVertex_kind(src.getVertex_kind_id()),row,3);
        edgedata.setValueAt(new Integer(tar.getNode_index()),row,4);
        edgedata.setValueAt(new Integer(tar.getStartline()),row,5);
        edgedata.setValueAt(tar.getVertex_char(),row,6);
        edgedata.setValueAt(VertexKindMapping.getVertex_kind(tar.getVertex_kind_id()),row,7);                 
        edgedata.setValueAt(new Integer(k),row,8);
        edgedata.setValueAt(new Integer(src.getPdg_id()),row,9);
        edgedata.setValueAt(new Integer(tar.getPdg_id()),row,10); 
    }
  
    private String showLine(int line) {
        String data = Integer.toString(line);
        if (data.length() <=5) {
            data = spacechar.substring(0,6-data.length())+data;
        }
        return data+": ";
    }  
  
    private void getPatternInstance(Connection conn, CandidatePatternInstance cpi) throws IOException, SQLException {

        int pdg_id = 0;
        int[] cpi_vertices_list = cpi.getCpiVerticesList();
        
        Hashtable all_pdgs = new Hashtable();
        
        int e_line = 3;
        if (this.line_num.getText().equals("2")) e_line = 20;
    
        for (int i=0; i<cpi.getCpiVertices_num(); i++) {
            int vertex_key = cpi_vertices_list[i];
            Vertex v = (Vertex) cpi.getCpiVerticesHash().get(new Integer(vertex_key));
            if (v != null && v.isExtendable()) {
                int line1 = v.getStartline(); 
                pdg_id = v.getPdg_id();
                VertexPosition position = new VertexPosition(v.getVertex_key(),pdg_id,line1,line1);
                PdgVertices pdg_nodes = (PdgVertices) all_pdgs.get(new Integer(pdg_id));
                if (pdg_nodes == null) {
                    pdg_nodes = new PdgVertices(pdg_id);
                    all_pdgs.put(new Integer(pdg_nodes.getPdg_id()),pdg_nodes);
                }
                pdg_nodes.addPdgVertexPosition(position);                
                pdg_nodes.addHighlight(new Integer(line1));   
                pdg_nodes.setBoundary(line1);
            }
        }
        
        Enumeration enum2 = all_pdgs.elements();
        while (enum2.hasMoreElements()) {
            PdgVertices pdg = (PdgVertices) enum2.nextElement();
            SourceFile sourcefile = new SourceFile(conn,pdg.getPdg_id());
            Hashtable highlight = pdg.getHighlight();
            String filename = sourcefile.getFilename();
            String pdg_name = sourcefile.getPdg_name();
            data = data +"<p align='center' color='blue'>"+filename+"("+pdg_name+")("+pdg.getPdg_id_string()+")</p>";
            data = data + "<pre>";
            filename = filename.replace('\\','/');
            filename = "C:/project_src/"+htFileNames.get(user)+"/"+filename;
            BufferedReader reader = new BufferedReader(new FileReader(filename));     
            for (int i=1; i <= pdg.getEnd_line() + e_line; i++) {
                String line = reader.readLine();         
                if (line != null) line = line.replaceAll("<"," le ");
                if (i >= pdg.getStart_line() - e_line) {
                    Object obj = highlight.get(new Integer(i));
                    if (obj != null) data = data + "<br>" + this.showLine(i) + "<font color='red'>" + line + "</font>";
                    else             data = data  + "<br>" + this.showLine(i) + line;
                }
            }
            data = data + "</pre>";
            reader.close();    
        }    
    }

    private void btn_del_src_node_actionPerformed(ActionEvent e) {
        this.removeSingleNode(0); //0: source node index in JTable
        this.showModifiedResult();
    }

    private void btn_del_single_link_actionPerformed(ActionEvent e) {
        int[] rows = this.edgedata.getSelectedRows();
        CandidatePatternInstance[] cpi_list = cp.getCpi_list();
        
        for (int i=0;i<rows.length; i++) {
            int src_idx = ((Integer)this.edgedata.getValueAt(rows[i],0)).intValue();
            int tar_idx = ((Integer)this.edgedata.getValueAt(rows[i],4)).intValue();
            int edge_type = ((Integer)this.edgedata.getValueAt(rows[i],8)).intValue();     
            
            for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
                CandidatePatternInstance cpi = cpi_list[cpi_idx];
                Hashtable cpi_vertices_hash = cpi.getCpiVerticesHash();
                int[] cpi_vertices_list = cpi.getCpiVerticesList();                
                //Vertex v = (Vertex) cpi.cpi_node_list.get(node_index);         
                cpi.removeEdgesBetweenTwoNodes(src_idx,tar_idx,edge_type);
                boolean src_has_link = true;
                boolean tar_has_link = true;
                if (cpi_idx == 0) {
                    src_has_link = cpi.checkNodeLink(src_idx);
                    tar_has_link = cpi.checkNodeLink(tar_idx);
                }
                
                Vertex src_node = (Vertex) cpi.getVertexByNodeIndex(src_idx);
                Vertex tar_node = (Vertex) cpi.getVertexByNodeIndex(tar_idx);
                if (!src_has_link) src_node.setNotExtendable();
                if (!tar_has_link) tar_node.setNotExtendable();
            }
        }    
        this.showModifiedResult();  
    }

  private void btn_save_actionPerformed(ActionEvent e) {
      CandidatePatternInstance[] cpi_list = cp.getCpi_list();
      CandidatePatternInstance cpi = cpi_list[0];
      boolean[] remaining_nodes = cpi.getRemainingNode_and_setNewIdx();
      int[][][] new_edges = this.generateNewEdges(cpi,remaining_nodes);
      this.saveToDatabase(new_edges,this.getRemainingNodeNum(remaining_nodes));
  }
 
  private void saveToDatabase(int[][][] new_edges, int num_nodes) {
      Connection conn = null;
      try {
          conn = getConnection();
          CallableStatement cstmt = conn.prepareCall("{call del_pattern_instance(?)}");
          cstmt.setInt(1,pattern_key); //graph group id
          cstmt.execute();
          cstmt.close();       
          
          // Build the mapping between old_node_index and new_node_index     
          int[] mapping = new int[num_nodes];
          CandidatePatternInstance cpi = cp.getCpi_list()[0];
          int[] cpi_vertices_list = cpi.getCpiVerticesList();
          Hashtable cpi_vertices_hash = cpi.getCpiVerticesHash();
        
          for (int i=0; i<cpi.getCpiVertices_num(); i++) {       
              int vertex_key = cpi_vertices_list[i];
              Vertex v = (Vertex) cpi_vertices_hash.get(new Integer(vertex_key));
              if (v.getNode_index()>=0 ) mapping[v.getNode_index()] = i;
          }

       
       // Pattern Instance1(patter_key, instance_id, src_vertex_key, tar_vertex_key, edge_type)
          String ins_sql = "INSERT INTO pattern_instance VALUES(?,?,?,?,?,?,?)";
          PreparedStatement pstmt = conn.prepareStatement(ins_sql) ;
          CandidatePatternInstance[] cpi_list = cp.getCpi_list();
          for (int cpi_idx=0; cpi_idx<cp.num_cpi; cpi_idx++) {    
              cpi = cpi_list[cpi_idx]; 
              cpi_vertices_list = cpi.getCpiVerticesList();
              cpi_vertices_hash = cpi.getCpiVerticesHash();
              // Output a pattern instance
              for (int i=0; i<num_nodes; i++) {
                  int src_key = cpi_vertices_list[mapping[i]];
                  Vertex src = (Vertex) cpi_vertices_hash.get(new Integer(src_key));
                  for (int j=0; j<num_nodes; j++) {
                      int tar_key = cpi_vertices_list[mapping[j]];
                      Vertex tar = (Vertex) cpi_vertices_hash.get(new Integer(tar_key));
                      for (int k=0; k<3; k++) {
                          if (new_edges[i][j][k] !=0) {
                              pstmt.setInt(1,pattern_key);
                              pstmt.setInt(2,0);    
                              pstmt.setInt(3,99);
                              pstmt.setInt(4,cpi_idx);
                              pstmt.setInt(5,src.getVertex_key());
                              pstmt.setInt(6,tar.getVertex_key());
                              pstmt.setInt(7,k);
                              pstmt.execute();
                          }
                      }
                  }        
              }
          }
          pstmt.close();       
       
          ins_sql = "INSERT INTO pattern_node_info VALUES(?,?,?,?)";
          pstmt = conn.prepareStatement(ins_sql) ;
   
          for (int cpi_idx=0; cpi_idx<cp.getNum_cpi(); cpi_idx++) {     
              cpi = cp.getCpi_list()[cpi_idx];
              cpi_vertices_list = cpi.getCpiVerticesList();
              cpi_vertices_hash = cpi.getCpiVerticesHash();

              // Output a pattern instance
              for (int i=0; i<num_nodes; i++) {
                  int src_key = cpi_vertices_list[mapping[i]];
                  Vertex src = (Vertex) cpi_vertices_hash.get(new Integer(src_key));
                  pstmt.setInt(1,pattern_key);
                  pstmt.setInt(2,cpi_idx);
                  pstmt.setInt(3,i);
                  pstmt.setInt(4,src.getVertex_key());
                  pstmt.execute();  
              }
          }
          pstmt.close();        
       
       
          cp = new CandidatePattern(conn,pattern_key);
          getPattern_Info(conn,pattern_key);
          this.showSourceCode(conn);
          this.showEdgeInfo();

      } catch (Exception e) {
          e.printStackTrace();
      } finally {
          try {
              if (conn!= null) conn.close();
          } catch (SQLException sqle) {}
      }
  }
  
  private int[][][] generateNewEdges(CandidatePatternInstance cpi, boolean[] remaining_nodes) {
      int[][][] original_edges = cpi.cpi_edges;
      int[] cpi_node_list = cpi.getCpiVerticesList();
      Hashtable cpi_vertices_hash = cpi.getCpiVerticesHash();
      int num_remaining_node = this.getRemainingNodeNum(remaining_nodes);
      int[][][] new_edges = new int[num_remaining_node][num_remaining_node][3];
      for (int i=0; i<cpi.getCpiVertices_num(); i++) {
          if (remaining_nodes[i]) {
              int vi_key = cpi_node_list[i];
              Vertex vi = (Vertex)cpi_vertices_hash.get(new Integer(vi_key));
              for (int j=0; j<cpi.getCpiVertices_num(); j++) {
                  if (remaining_nodes[j]) {     
                      int vj_key = cpi_node_list[j];
                      Vertex vj = (Vertex) cpi_vertices_hash.get(new Integer(vj_key));
                      for (int k=0; k<3; k++) {
                          if (original_edges[i][j][k]>0) {
                              new_edges[vi.getNode_index()][vj.getNode_index()][k] = 1;
                          } else {
                              new_edges[vi.getNode_index()][vj.getNode_index()][k] = 0;
                          }
                      }
                  }
              }
          }
      }    
      return new_edges;
  }
  
  private int getRemainingNodeNum(boolean[] remaining_nodes) {
      int count = 0;
      for (int i=0; i<remaining_nodes.length; i++) {
          if (remaining_nodes[i]) count++;
      }
      return count;
  }

  private void removeSingleNode(int del_node_idx) {
      int[] rows = this.edgedata.getSelectedRows();
      for (int i=0;i<rows.length; i++) {
          int node_index = ((Integer)this.edgedata.getValueAt(rows[i],del_node_idx)).intValue();
          for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
              CandidatePatternInstance cpi = cp.getCpi_list()[cpi_idx];
              int vertex_key = cpi.getCpiVerticesList()[node_index];
              Vertex v = (Vertex) cpi.getCpiVerticesHash().get(new Integer(vertex_key));
              v.setNotExtendable(); // Node v is removed from the pattern
              cpi.removeFromEdges(node_index);
              cpi.removeToEdges(node_index);
          }
      }  
  }

  private void showModifiedResult() {
      Connection conn = null;
      try {
          conn = getConnection();
          this.showSourceCode(conn);
          this.showEdgeInfo();
      } catch (Exception ee) {
          ee.printStackTrace();
      } finally {
          try {
              if (conn != null) conn.close();
          } catch (SQLException e1) {}
      }  
  }
  
  private void btn_del_tar_node_actionPerformed(ActionEvent e) {
      this.removeSingleNode(4); // 4: Target Node Index
      this.showModifiedResult();
  }




  private void btn_confirm_pattern_actionPerformed(ActionEvent e){
      this.updatePatternStatus();
  }

  private void updatePatternStatus() {
      Connection conn = null;
      try {
    	  endDate = new java.util.Date();
    	  
          conn = this.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(update_pattern_status);
          pstmt.setString(1,field_confirm.getText());
          pstmt.setString(2,field_modification.getText());
          pstmt.setString(3,pane_remarks.getText());
          pstmt.setInt(4,this.pattern_key);
          pstmt.executeUpdate();
          
          pstmt = conn.prepareStatement(update_pattern_status2);
          pstmt.setString(1,field_reviewer.getText());
          pstmt.setString(2,field_rankingCode.getText());
          pstmt.setInt(3,this.pattern_key);
          pstmt.executeUpdate();
          
          pstmt = conn.prepareStatement(update_pattern_review_time);
          pstmt.setLong(1, (endDate.getTime() - startDate.getTime())/1000);
          pstmt.setInt(2, pattern_key);
          pstmt.executeUpdate();
          
          pstmt.close();    
      } catch (SQLException e) {
          e.printStackTrace();
      } finally {
          try {
              if (conn != null) conn.close();
          } catch (SQLException ie) {}
      }  
  }



  private void btn_delete_range_actionPerformed(ActionEvent e)  {
      try {
          int from = Integer.parseInt(delete_from.getText());
          int to = Integer.parseInt(delete_to.getText());
          CandidatePatternInstance first_cpi = cp.getCpi_list()[0];
          int[] cpi_vertices_list = first_cpi.getCpiVerticesList();
          Hashtable cpi_vertices_hash = first_cpi.getCpiVerticesHash();
          for (int i=0; i<first_cpi.getCpiVertices_num(); i++) { 
              int v_key_in_first_cpi = cpi_vertices_list[i];
              Vertex v_in_first_cpi = (Vertex) cpi_vertices_hash.get(new Integer(v_key_in_first_cpi));
              int line = v_in_first_cpi.getStartline();
              int node_index = v_in_first_cpi.getNode_index();
              if (line>=from && line<=to) {    
                  for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
                      CandidatePatternInstance cpi = cp.getCpi_list()[cpi_idx];
                      int vertex_key = cpi.getCpiVerticesList()[node_index];
                      Vertex v = (Vertex) cpi.getCpiVerticesHash().get(new Integer(vertex_key));
                      v.setNotExtendable(); // Node v is removed from the pattern
                      cpi.removeFromEdges(node_index);
                      cpi.removeToEdges(node_index);
                  }
              }
          }          
          this.showModifiedResult();
      } catch (Exception ie) {
          ie.printStackTrace();
      }
  }



  private void btn_auto_trim_actionPerformed(ActionEvent e) {
/*  
    // Remove control points without data or variable links
    CandidatePatternInstance cpi0 = (CandidatePatternInstance) cp.list_cpi.get(0);
    int node_num = cpi0.getNum_vertices_in_cpi();
    int[][][] cpi_edges = cpi0.getCpi_edges();
    Vector cpi_nodes = cpi0.getCpi_node_list();
    for (int i=0; i<node_num; i++) {
       Vertex vi = (Vertex) cpi_nodes.get(i);
       if (vi.getVertex_kind_id()==6 && this.qualified_for_remove_from_pattern(node_num,i,cpi_edges,cpi_nodes)) {
          cp.removeToEdges(i);
          cp.removeFromEdges(i);      
          for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
             CandidatePatternInstance cpi = (CandidatePatternInstance) cp.list_cpi.get(cpi_idx);
             Vertex v = (Vertex) cpi.cpi_node_list.get(i);
             v.setNotExtendable(); // Node v is removed from the pattern
             cpi.removeFromEdges(i);
             cpi.removeToEdges(i);
          }
       }
       
       // Remove x-node(except call-site) ---> actual-out
       // Remove actual-in/out --> call-site node
       for (int j=0; j<node_num; j++) {
          Vertex vj = (Vertex) cpi_nodes.get(j);
          int remove_type = isQualfiied_for_delete(vi,vj);
          if (remove_type != -1) {
             cp.removeDependenceEdges(i,j,remove_type);
             for (int cpi_idx=0; cpi_idx<cp.num_cpi; cpi_idx++) {
                CandidatePatternInstance cpi = (CandidatePatternInstance) cp.list_cpi.get(cpi_idx);
                cpi.removeDependenceEdges(i,j,remove_type);
             }
          }
       }
    }
    
    // Remove variable link where a node is a statement node
    for (int i=0;i<node_num; i++) {
       for (int j=0; j<node_num; j++) {
          if (cpi_edges[i][j][2]!=0) { 
            Vertex vi = (Vertex) cpi_nodes.get(i);
            Vertex vj = (Vertex) cpi_nodes.get(j);
            if (vi.getVertex_kind_id() == 10 || vj.getVertex_kind_id() == 10) {
              cp.removeEdgesBetweenTwoNodes(i,j,2);      
              boolean src_has_link = cp.checkNodeLink(i);
              boolean tar_has_link = cp.checkNodeLink(j);
              /// cp 
              for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
                 CandidatePatternInstance cpi = (CandidatePatternInstance) cp.list_cpi.get(cpi_idx);
                 //Vertex v = (Vertex) cpi.cpi_node_list.get(node_index);         
                 cpi.removeEdgesBetweenTwoNodes(i,j,2);
                 Vertex src_node = (Vertex) cpi.cpi_node_list.get(i);
                 Vertex tar_node = (Vertex) cpi.cpi_node_list.get(j);
                 if (!src_has_link) src_node.setNotExtendable();
                 if (!tar_has_link) tar_node.setNotExtendable();
              }
            }    
          }
       }
    }  
    this.showModifiedResult();  
    this.deleteLinkWithLongDistance();
    this.showModifiedResult();  
    
    // Remove statement node which only connect to formal nodes
    cpi0 = (CandidatePatternInstance) cp.list_cpi.get(0);
    node_num = cpi0.getNum_vertices_in_cpi();
    cpi_edges = cpi0.getCpi_edges();
    cpi_nodes = cpi0.getCpi_node_list();    
    for (int i=0; i<node_num; i++) {
       Vertex vi = (Vertex) cpi_nodes.get(i);
       int t = vi.getVertex_kind_id();
       if ((t==1 || t==2 || t==10) && this.qualified_for_remove_from_pattern(node_num,i,cpi_edges,cpi_nodes)) {
          cp.removeToEdges(i);
          cp.removeFromEdges(i);      
          for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
             CandidatePatternInstance cpi = (CandidatePatternInstance) cp.list_cpi.get(cpi_idx);
             Vertex v = (Vertex) cpi.cpi_node_list.get(i);
             v.setNotExtendable(); // Node v is removed from the pattern
             cpi.removeFromEdges(i);
             cpi.removeToEdges(i);
          }
       }
    }    
    this.showModifiedResult(); */
    
  }

/*


  private int isQualfiied_for_delete(Vertex vi, Vertex vj) {
    int src_type = vi.getVertex_kind_id();
    int tar_type = vj.getVertex_kind_id();
    if (tar_type == 2 && src_type != 5) return 10;    
    if (tar_type == 5 && (src_type == 1 || src_type == 2)) return 10;
    return -1;      
  }  
  
  private void deleteLinkWithLongDistance() {
    int rownum = this.edgedata.getRowCount();
    for (int i=0; i<rownum; i++) {
      boolean del_flag = false;
      try {
        int src_line = ((Integer)this.edgedata.getValueAt(i,1)).intValue();
        int tar_line = ((Integer)this.edgedata.getValueAt(i,5)).intValue();
        int distance = Math.abs(src_line-tar_line);
        if (distance > 100) {
          String id1 = (String)this.edgedata.getValueAt(i,3);
          String id2 = (String)this.edgedata.getValueAt(i,7);
          if (id1.equals("SW") || id2.equals("SW")) {
            del_flag = false;
          } else {
            del_flag = true;
          }
        }
        if (del_flag) {
          int src_idx = ((Integer)this.edgedata.getValueAt(i,0)).intValue();
          int tar_idx = ((Integer)this.edgedata.getValueAt(i,4)).intValue();
          int edge_type = ((Integer)this.edgedata.getValueAt(i,8)).intValue();     
          cp.removeEdgesBetweenTwoNodes(src_idx,tar_idx,edge_type);      
          boolean src_has_link = cp.checkNodeLink(src_idx);
          boolean tar_has_link = cp.checkNodeLink(tar_idx);
          /// cp 
          for (int cpi_idx =0; cpi_idx<cp.num_cpi; cpi_idx++) {
             CandidatePatternInstance cpi = (CandidatePatternInstance) cp.list_cpi.get(cpi_idx);
             //Vertex v = (Vertex) cpi.cpi_node_list.get(node_index);         
             cpi.removeEdgesBetweenTwoNodes(src_idx,tar_idx,edge_type);
             Vertex src_node = (Vertex) cpi.cpi_node_list.get(src_idx);
             Vertex tar_node = (Vertex) cpi.cpi_node_list.get(tar_idx);
             if (!src_has_link) src_node.setNotExtendable();
             if (!tar_has_link) tar_node.setNotExtendable();
          }
        }
      } catch (Exception e) {}
    }    
  }
  
  
  private boolean qualified_for_remove_from_pattern(int node_num, int i, int[][][] cpi_edges, Vector cpi_nodes) {
    Vertex vi = (Vertex) cpi_nodes.get(i);
    int vertex_kind_id = vi.getVertex_kind_id();
    switch (vertex_kind_id) {
      case 6: for (int j=0; j<node_num; j++) { // no variable link or data link to control point
                 if (cpi_edges[i][j][0] !=0 || cpi_edges[i][j][2]!=0 || cpi_edges[j][i][0]!=0 || cpi_edges[j][i][2]!=0) {
                     Vertex vj = (Vertex) cpi_nodes.get(j);
                     if (vj.getVertex_kind_id()!=11) return false; //formal point
                 }
              }
              return true;
      case 1:
      case 2: for (int j=0; j<node_num; j++) { //actual-in/out without its call-site node
                 Vertex vj = (Vertex) cpi_nodes.get(j);
                 if (vj.getVertex_kind_id()==5 && cpi_edges[j][i][1]!=0) return false;
              }
              return true;
      case 10:for (int j=0; j<node_num; j++) { // statement without any link except formal-in
                 Vertex vj = (Vertex) cpi_nodes.get(j);
                 if (vj.getVertex_kind_id()!=11) {
                   for (int k=0; k<4; k++) {
                      if (cpi_edges[i][j][k]!=0 || cpi_edges[j][i][k]!=0) return false;
                   }
                 }
              }
              return true;           
      default: return false;
    }
  }*/

    private void jButton1_actionPerformed(ActionEvent e) {
        this.user = this.field_user.getText();
        this.passwd = this.field_passwd.getText();
        this.jdbc = this.field_jdbc.getText();  
    }
}
