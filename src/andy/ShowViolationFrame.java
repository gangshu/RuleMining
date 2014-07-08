package andy;
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
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.JTextField;

import edu.cwru.eecs.statianalysis.dbutil.DBUtil;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;
import edu.cwru.eecs.statianalysis.pattern.Rule;
import edu.cwru.eecs.statianalysis.pattern.Violation;
import edu.cwru.eecs.statianalysis.service.GraphService;
import edu.cwru.eecs.statianalysis.service.RuleService;
import edu.cwru.eecs.statianalysis.service.ViolationService;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.*;
import oracle.jdbc.*;
import y.view.Graph2D;

import java.util.*;
import java.util.Date;
import java.io.*;

public class ShowViolationFrame extends JFrame {
    static String qry_violations_by_pattern_key = "SELECT * FROM violations WHERE pattern_key = ?  ORDER BY violation_key";
    static String confirm_violation_sql = "UPDATE violations SET confirm = ?, comments = ?, comments_id = ?where violation_key = ?";
    static String update_review_time = "UPDATE violations SET REVIEW_TIME = ? where violation_key = ? and review_time is null";
    static String spacechar = "       ";
    private JPanel jPanel1 = new JPanel();
    private XYLayout xYLayout1 = new XYLayout();
    private JButton btnExecution = new JButton();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JLabel jLabel1 = new JLabel();
    private JEditorPane displayArea = new JEditorPane();
    private JTextField field_pattern_key = new JTextField();
    private String data = null;
    private String violation_data = null;
    String  username;
    String  password;
    String  jdbc;
    CandidatePattern cp;
    int pattern_key;
    private JScrollPane jScrollPane2 = new JScrollPane();
    private JEditorPane displayViolation = new JEditorPane();
    private Vector list_pattern_violation;
    private Vector list_pattern_instance;
    private ViolationInstance current_violation_instance;
    private JButton btn_next_violation = new JButton();
    private JButton btn_previous_violation = new JButton();
    private JButton btn_next_correct = new JButton();
    private JButton btn_previous_correct = new JButton();
    private int currect_violation_index = 0;
    private int current_correct_index = 0;
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private JLabel label_lost_nodes = new JLabel();
    private JLabel label_lost_edges = new JLabel();
    private JButton btn_first_violation = new JButton();
    private JButton btn_first_correct = new JButton();
    private JButton btn_confirm_violation = new JButton();
    private JButton btn_not_violation = new JButton();
    private JTextField user = new JTextField();
    private JLabel jLabel4 = new JLabel();
    private JLabel jLabel5 = new JLabel();
    private JTextField passwd = new JTextField();
    private JButton jButton1 = new JButton();
    private JTextField field_jdbc = new JTextField();
    private JTextField comments = new JTextField();
    private JTextField comments_id = new JTextField();
    private JButton show_more_source = new JButton();
    private int extended_line_num =5;
    private JButton but_show_less_src = new JButton();
    private Hashtable<String, String> htFileNames = new Hashtable<String, String>();
    
    //Used to record time needed to confirm each bug instance
    private java.util.Date startDate, endDate;

    public ShowViolationFrame()  {
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
        htFileNames.put("ac800m","AC800M\\source");
    }
  
    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(this.jdbc,this.username,this.password);     
        } catch (SQLException e) {}
        return conn;
    }  

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(1030, 740));
        jPanel1.setBounds(new Rectangle(0, 0, 1030, 740));
        jPanel1.setBackground(new Color(142, 232, 236));
        jPanel1.setLayout(xYLayout1);
        btnExecution.setText("Execution");
        btnExecution.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnExecution_actionPerformed(e);
            }
        });
        jLabel1.setText("Pattern ID: ");
        jLabel1.setFont(new Font("·s²Ó©úÅé", 0, 16));
        btn_next_violation.setText("next violation");
        btn_next_violation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_next_violation_actionPerformed(e);
            }
        });
        btn_previous_violation.setText("previous violation");
        btn_previous_violation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_previous_violation_actionPerformed(e);
            }
        });
        btn_next_correct.setText("next correct ");
        btn_next_correct.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_next_correct_actionPerformed(e);
            }
        });
        btn_previous_correct.setText("previous correct");
        btn_previous_correct.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_previous_correct_actionPerformed(e);
            }
        });
        jLabel2.setText("Lost Node: ");
        jLabel3.setText("Lost edges:");
        label_lost_nodes.setText("0");
        label_lost_edges.setText("0");
        btn_first_violation.setText("first violation");
        btn_first_violation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_first_violation_actionPerformed(e);
            }
        });
        btn_first_correct.setText("first correct");
        btn_first_correct.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_first_correct_actionPerformed(e);
            }
        });
        btn_confirm_violation.setText("Confirm Violation");
        btn_confirm_violation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_confirm_violation_actionPerformed(e);
            }
        });
        btn_not_violation.setText("Not violation");
        btn_not_violation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_not_violation_actionPerformed(e);
            }
        });
        user.setText("ac800m");
        jLabel4.setText("user");
        jLabel5.setText("passwd");
        passwd.setText("andypodgurski");
        jButton1.setText("Load Database Info");
        jButton1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        jButton1_actionPerformed(e);
                    }
                });
        field_jdbc.setText("jdbc:oracle:thin:@selserver.cwru.edu:1521:orcl");
        show_more_source.setText("Show More Source Code");
        show_more_source.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        show_more_source_actionPerformed(e);
                    }
                });
        but_show_less_src.setText("Show Less Source Code");
        but_show_less_src.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        but_show_less_src_actionPerformed(e);
                    }
                });
        jScrollPane2.getViewport().add(displayViolation, null);
        jPanel1.add(but_show_less_src, new XYConstraints(880, 530, 135, 25));
        jPanel1.add(show_more_source, new XYConstraints(880, 495, 135, 25));
        jPanel1.add(comments_id, new XYConstraints(880, 465, 20, 20));
        jPanel1.add(comments, new XYConstraints(880, 435, 135, 25));
        jPanel1.add(field_jdbc, new XYConstraints(880, 370, 135, 25));
        jPanel1.add(jButton1, new XYConstraints(880, 285, 135, 25));
        jPanel1.add(passwd, new XYConstraints(925, 345, 90, 20));
        jPanel1.add(jLabel5, new XYConstraints(880, 345, 45, 20));
        jPanel1.add(jLabel4, new XYConstraints(880, 320, 45, 20));
        jPanel1.add(user, new XYConstraints(925, 320, 90, 20));
        jPanel1.add(btn_not_violation, new XYConstraints(880, 400, 135, 25));
        jPanel1.add(btn_confirm_violation,
                    new XYConstraints(880, 255, 135, 25));
        jPanel1.add(btn_first_correct, new XYConstraints(880, 165, 135, 25));
        jPanel1.add(btn_first_violation, new XYConstraints(880, 75, 135, 25));
        jPanel1.add(label_lost_edges, new XYConstraints(940, 635, 20, 25));
        jPanel1.add(label_lost_nodes, new XYConstraints(940, 610, 20, 25));
        jPanel1.add(jLabel3, new XYConstraints(880, 635, 60, 20));
        jPanel1.add(jLabel2, new XYConstraints(880, 610, 55, 25));
        jPanel1.add(btn_previous_correct,
                    new XYConstraints(880, 225, 135, 25));
        jPanel1.add(btn_next_correct, new XYConstraints(880, 195, 135, 25));
        jPanel1.add(btn_previous_violation, new XYConstraints(880, 135, 135, 25));
        jPanel1.add(btn_next_violation, new XYConstraints(880, 105, 135, 25));
        jPanel1.add(jScrollPane2, new XYConstraints(5, 335, 870, 375));
        jPanel1.add(field_pattern_key, new XYConstraints(955, 10, 60, 30));
        jPanel1.add(jLabel1, new XYConstraints(885, 15, 151, 20));
        jScrollPane1.getViewport().add(displayArea, null);
        jPanel1.add(jScrollPane1, new XYConstraints(5, 0, 870, 335));
        jPanel1.add(btnExecution, new XYConstraints(880, 45, 135, 25));
        this.getContentPane().add(jPanel1, null);
    }

    private void btnExecution_actionPerformed(ActionEvent e) {
        Connection conn = null;
        this.currect_violation_index = 0;
        this.current_correct_index = 0;
        try { 
            conn = getConnection();
            pattern_key = Integer.parseInt(field_pattern_key.getText());
            cp = new CandidatePattern(conn,pattern_key);
            showSourceCode(conn);          
            /**
             *@author Boya Sun
             */
            //this.visualizeRulePattern(pattern_key);
      
            this.readListViolations(conn);
            Violations display_violation = null;
            if (list_pattern_violation.size()>0) {
                display_violation = (Violations) list_pattern_violation.get(0);
            } else {
                if (this.list_pattern_instance.size()>0) {
                    display_violation =  (Violations) list_pattern_instance.get(0);
                }
            }
      
            if (display_violation != null) {
                showViolation(conn,display_violation);
            }
      
            showButton();
            if (this.list_pattern_violation.size()>0) {
                this.btn_confirm_violation.setEnabled(true);
                this.btn_not_violation.setEnabled(true);
            } else {
                this.btn_confirm_violation.setEnabled(false);
                this.btn_not_violation.setEnabled(false);
            }      
      
            startDate = new java.util.Date();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e2) {}
        }
    }
  
    private void showButton() {
        if (this.list_pattern_instance.size()>0) {
            this.btn_first_correct.setEnabled(true);
        } else {
            this.btn_first_correct.setEnabled(false);
        }
        if (this.list_pattern_violation.size()>0) {
            this.btn_first_violation.setEnabled(true);
        } else {
            this.btn_first_violation.setEnabled(false);
        }
        if (this.list_pattern_instance.size()==0 || this.list_pattern_instance.size()-1 == this.current_correct_index) {
            this.btn_next_correct.setEnabled(false);
        } else {
            this.btn_next_correct.setEnabled(true);
        }
        if (this.list_pattern_instance.size()==0 || this.current_correct_index == 0) {
            this.btn_previous_correct.setEnabled(false);
        } else {
            this.btn_previous_correct.setEnabled(true);
        }
        if (this.list_pattern_violation.size()==0 || this.currect_violation_index == this.list_pattern_violation.size()-1)   {
            this.btn_next_violation.setEnabled(false); 
        } else {
            this.btn_next_violation.setEnabled(true);
        }
        if (this.list_pattern_violation.size()==0 || this.currect_violation_index == 0) {
            this.btn_previous_violation.setEnabled(false);
        } else {
            this.btn_previous_violation.setEnabled(true);
        }
    }
  
    private void readListViolations(Connection conn) throws SQLException {
        this.list_pattern_violation = new Vector();
        this.list_pattern_instance = new Vector();
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        int correct = 0;
        int incorrect = 0;
        try {
            pstmt = conn.prepareStatement(qry_violations_by_pattern_key);
            pstmt.setInt(1,pattern_key);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                Violations v = new Violations(rset);
                if (v.getLost_edges() == 0 && v.getLost_nodes() == 0) {
                    this.list_pattern_instance.add(correct,v);
                    correct++;
                } else {
                    this.list_pattern_violation.add(incorrect,v);
                    incorrect++;
                }
            }
        } finally {
            if (pstmt != null) pstmt.close();
            if (rset != null) rset.close();
        }
    }
  
    private void showSourceCode(Connection conn) throws SQLException, IOException {
        data = "<html><html><head><title>Pattern Instance in a Program</title></head><body>";
        CandidatePatternInstance[] cpi_list = cp.getCpi_list();
        CandidatePatternInstance cpi = cpi_list[0];
        data = data + getPatternInstance(conn, cpi);
        data = data + "</body></html>";
        displayArea.setEditorKit(new HTMLEditorKit());
        displayArea.setEditable(true);
        displayArea.setText(data);            
    }
  
    private void showViolation(Connection conn, Violations violation) {
        try {
            violation_data = "<html><html><head><title>Pattern Instance in a Program</title></head><body>";
            this.current_violation_instance = new ViolationInstance(violation.getViolation_key(),conn);
            violation_data = violation_data + getPatternInstance(conn,this.current_violation_instance);
            violation_data = violation_data + "</body></html>";
            displayViolation.setEditorKit(new HTMLEditorKit());
            displayViolation.setEditable(true);
            displayViolation.setText(violation_data);   
            this.comments.setText(violation.getComments());
            this.comments_id.setText(violation.getCommentsID());
            this.label_lost_nodes.setText(Integer.toString(violation.getLost_nodes()));
            this.label_lost_edges.setText(Integer.toString(violation.getLost_edges()));
            /**
             *@author Boya Sun
             */
            //this.visualizeVioPattern(violation.getPattern_key(), violation.getViolation_key());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *@author Boya Sun
     */
    private void visualizeRulePattern(int patternKey)
    {
    	DBUtil.createDatasource(this.user.getText());
    	RuleService ruleService = new RuleService(DBUtil.getDataSource(), patternKey);
    	Rule rule = ruleService.getRule();
    	System.out.println("rule: "+rule.getPatternKey());
    	GraphService graphService = new GraphService();
    	Graph2D graph2d = graphService.transformToYGraph(rule.getPatternGraph());
    	JFrame j = new JFrame("Visualization of the rule pattern");
    	graphService.visualizeGraph(graph2d, j);
        j.pack();
        j.setVisible(true); 
    }
    /**
     *@author Boya Sun
     */
    private void visualizeVioPattern(int patternKey, int violationKey)
    {
    	DBUtil.createDatasource(this.user.getText());
    	ViolationService vioService = new ViolationService(DBUtil.getDataSource(), violationKey);
    	Violation vio = vioService.getViolatoin();
    	System.out.println("violation: "+vio.getPatternKey()+" "+vio.getViolatonKey());
    	GraphService graphService = new GraphService();
    	PDGGraph<edu.cwru.eecs.statianalysis.data.Vertex, edu.cwru.eecs.statianalysis.data.Edge<edu.cwru.eecs.statianalysis.data.Vertex>> vioGraph = vio.getPatternGraph();
    	PDGGraph<edu.cwru.eecs.statianalysis.data.Vertex, edu.cwru.eecs.statianalysis.data.Edge<edu.cwru.eecs.statianalysis.data.Vertex>> deltaGraph = vio.getDeltaGraph();
    	List<PDGGraph<edu.cwru.eecs.statianalysis.data.Vertex, edu.cwru.eecs.statianalysis.data.Edge<edu.cwru.eecs.statianalysis.data.Vertex>>> pdgGraphList = new ArrayList<PDGGraph<edu.cwru.eecs.statianalysis.data.Vertex, edu.cwru.eecs.statianalysis.data.Edge<edu.cwru.eecs.statianalysis.data.Vertex>>>();
    	pdgGraphList.add(vioGraph);
    	pdgGraphList.add(deltaGraph);
    	graphService.visualizeFromPdgGraph(pdgGraphList, "Visualize violation graph "+violationKey);
    }
  
    private String showLine(int line) {
        String data = Integer.toString(line);
        if (data.length() <=5) {
            data = spacechar.substring(0,6-data.length())+data;
        }
        return data+": ";
    }  
  
    private String getPatternInstance(Connection conn, CandidatePatternInstance cpi) throws IOException, SQLException {
      
        String showdata = "";
        int pdg_id = 0;
        int[] cpi_vertices_list = cpi.getCpiVerticesList();
      
        Hashtable all_pdgs = new Hashtable();
      
        Hashtable cpi_vertices_hash = cpi.getCpiVerticesHash();
        for (int i=0; i<cpi.getCpiVertices_num(); i++) {
            int vertex_key = cpi_vertices_list[i];
            Vertex v = (Vertex) cpi_vertices_hash.get(new Integer(vertex_key));
            if (v.isExtendable()) {
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
            showdata = showdata +"<p align='center' color='blue'>"+filename+"("+pdg_name+")("+pdg.getPdg_id_string()+")</p>";
            showdata = showdata + "<pre>";
            filename = filename.replace('\\','/');
            filename = "C:/project_src/"+htFileNames.get(user.getText())+"/"+filename;
            BufferedReader reader = new BufferedReader(new FileReader(filename));     
            for (int i=1; i <= pdg.getEnd_line() + this.extended_line_num; i++) {
                String line = reader.readLine();
                if (line != null) line = line.replace("<"," le ");
                if (i >= pdg.getStart_line() - this.extended_line_num) {
                    Object obj = highlight.get(new Integer(i));
                    if (obj != null) showdata = showdata + "<br>" + this.showLine(i) + "<font color='red'>" + line + "</font>";
                    else             showdata = showdata  + "<br>" + this.showLine(i) + line;
                }
            }
            showdata = showdata + "</pre>";
            reader.close();    
        }      
        return showdata;
    }

    private void btn_next_violation_actionPerformed(ActionEvent e){
        this.currect_violation_index++;
        showViolationDetail(this.list_pattern_violation,this.currect_violation_index);
        this.btn_confirm_violation.setEnabled(true);
        this.btn_not_violation.setEnabled(true);
        startDate = new java.util.Date();
    }

    private void showViolationDetail(Vector list_violation, int index) {
        Connection conn = null;
        Violations displayViolation = (Violations) list_violation.get(index);
        try {
            conn = this.getConnection();
            showViolation(conn,displayViolation);
            showButton();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ie2) {}
        }
    }

    private void btn_first_violation_actionPerformed(ActionEvent e)  {
        this.currect_violation_index = 0;
        this.showViolationDetail(this.list_pattern_violation,this.currect_violation_index);
        this.btn_confirm_violation.setEnabled(true);
        this.btn_not_violation.setEnabled(true);
        startDate = new java.util.Date();
    }

    private void btn_previous_violation_actionPerformed(ActionEvent e) {
        this.currect_violation_index--;
        this.showViolationDetail(this.list_pattern_violation,this.currect_violation_index);
        this.btn_confirm_violation.setEnabled(true);
        this.btn_not_violation.setEnabled(true);
        startDate = new java.util.Date();
    }

    private void btn_first_correct_actionPerformed(ActionEvent e){
        this.current_correct_index = 0;
        this.showViolationDetail(this.list_pattern_instance,this.current_correct_index);
        this.btn_confirm_violation.setEnabled(false);
        this.btn_not_violation.setEnabled(false);
    }

    private void btn_next_correct_actionPerformed(ActionEvent e)  {
        this.current_correct_index++;
        this.showViolationDetail(this.list_pattern_instance,this.current_correct_index);
        this.btn_confirm_violation.setEnabled(false);
        this.btn_not_violation.setEnabled(false);
    }

    private void btn_previous_correct_actionPerformed(ActionEvent e) {
        this.current_correct_index--;
        this.showViolationDetail(this.list_pattern_instance,this.current_correct_index);  
        this.btn_confirm_violation.setEnabled(false);
        this.btn_not_violation.setEnabled(false);
    }

    private void btn_confirm_violation_actionPerformed(ActionEvent e)  {
        Violations v = (Violations) this.list_pattern_violation.get(this.currect_violation_index);
        //  this.comments.setText("");
        // this.comments_id.setText("");
        endDate = new java.util.Date();
        v.setComments(this.comments.getText()); 
        v.setCommentsID(this.comments_id.getText());
        this.setConfirmViolation("Y");  
    }
  
    private void setConfirmViolation(String confirm) {
        Connection conn = null;
        Violations confirmViolation = (Violations) this.list_pattern_violation.get(this.currect_violation_index);
        try {
            conn = this.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(confirm_violation_sql);
            pstmt.setString(1,confirm);
            pstmt.setInt(4,confirmViolation.getViolation_key());
            pstmt.setString(2,this.comments.getText());
            pstmt.setString(3,this.comments_id.getText());
            pstmt.executeUpdate();
            
            pstmt = conn.prepareStatement(update_review_time);
            pstmt.setLong(1, (endDate.getTime() - startDate.getTime())/1000);
            pstmt.setInt(2, confirmViolation.getViolation_key());
            pstmt.executeUpdate();
            
            pstmt.close();
        } catch (SQLException ie) {
            ie.printStackTrace();   
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ie2) {}
        }      
    }

    private void btn_not_violation_actionPerformed(ActionEvent e) {
    	endDate = new java.util.Date();
        Violations v = (Violations) this.list_pattern_violation.get(this.currect_violation_index);
        v.setComments(this.comments.getText());
        v.setCommentsID(this.comments_id.getText());
        this.setConfirmViolation("N");
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        this.username = user.getText();
        this.password = passwd.getText();
        this.jdbc = this.field_jdbc.getText();
    }

    private void show_more_source_actionPerformed(ActionEvent e) {
        this.extended_line_num = 30;
    }


    private void but_show_less_src_actionPerformed(ActionEvent e) {
        this.extended_line_num = 5;
    }
}

