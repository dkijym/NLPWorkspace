/*
 *
 */
package metdemo.Attach;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
//import javax.swing.event.*;
///import javax.swing.border.*;
import javax.swing.JPanel;

import metdemo.winGui;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.CliqueTools.UserClique;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.dataStructures.SimpleHashtable;

import java.sql.SQLException;
import java.util.*;
//import java.sql.*;
//import javax.swing.JPanel;
//import javax.swing.JFrame;
import java.text.SimpleDateFormat;
//import java.text.DateFormat;
import java.io.*;
//import chapman.graphics.JPlot2D;

public class VirusDetectModels extends JScrollPane implements ActionListener, MouseListener {
    winGui parent;
	
    DefaultTableCellRenderer alarmRenderer; 
    JPanel panel, inPanel, outPanel;

    JButton detect, getMail,useModel, testRange, updateMEF;
    JCheckBox checkMef, checkHellinger, checkClique;
    JCheckBox attachOnly;
    JTextField cliqueHellingerThreshold;
    JScrollPane tableScrollPane;
    DefaultTableModel tablemodel;
    HCSortFilterModelVsimulate sorter;
    String sender;
    String[] columnNames = {"Sender","Recipient","Subject","Time","Attachment",
			    "Folder","Label","Hellinger", "Clique", "MEF","ref"};
    GridBagConstraints gbc;
    JRadioButton inbound, outbound, bothbound;
    String inboundQuery = "select distinct rcpt from email";
    String outboundQuery = "select distinct sender from email";

    JTable table = null;
    String[] mailref = null;
    JPopupMenu classMenu = null;

    int MEFwindow = 6;
    double MEFthreshold = 0.2;

    private EMTDatabaseConnection jdbc = null;
    private EMTDatabaseConnection jdbcUpdate=null;
    md5CellRenderer m_md5Renderer;
    //BusyWindow bw = new BusyWindow("Progress","Running...");

    ReportForensicWindow m_alerts;
    
    //constructor
    public VirusDetectModels(EMTDatabaseConnection jdbcU, EMTDatabaseConnection jdbcV, md5CellRenderer m, winGui w, ReportForensicWindow a){
    //public VirusDetectModels(JDBCConnect jdbcU, JDBCConnect jdbcV){
        parent = w;
        m_md5Renderer = m;
	m_alerts = a;
        jdbc = jdbcV;
        jdbcUpdate = jdbcU;
	//parent.setUserSelectedIndex(4890);
	//connect to the database	
	/*
	  try{
	  //jdbc = new JDBCConnect("jdbc:mysql://blackberry/met03","org.gjt.mm.mysql.Driver","outsider", "fizzy99");
	  jdbc = new JDBCConnect("jdbc:mysql://blackberry/sal03","com.mysql.jdbc.Driver","outsider", "fizzy99");
	  if(jdbc.connect() == false){
	  throw(new Exception());
	  }
	  }
	  catch(Exception e){
	  System.err.println("Couldn't connect to database 1");
	  System.exit(1);
	  }
	*/
	//bw.setLocationRelativeTo(this);
	
	//the main panel
    	gbc = new GridBagConstraints();
    	GridBagLayout gridbag = new GridBagLayout();

	panel = new JPanel();
	panel.setLayout(gridbag);

	inPanel = new JPanel();
	inPanel.setLayout(gridbag);
	inPanel.setPreferredSize(new Dimension(800, 150));
	inPanel.setBorder(BorderFactory.createTitledBorder(""));

	outPanel = new JPanel();
	outPanel.setLayout(gridbag);
	outPanel.setBorder(BorderFactory.createTitledBorder("Results"));
	
    	outbound = new JRadioButton("outbound");
    	outbound.setActionCommand("outbound");
    	outbound.addActionListener(this);
    	inbound = new JRadioButton("inbound");
    	inbound.setActionCommand("inbound");
    	inbound.addActionListener(this);
	bothbound = new JRadioButton("both");
    	bothbound.setActionCommand("both");
    	bothbound.addActionListener(this);
    	inbound.setSelected(true);

    	ButtonGroup boundgroup = new ButtonGroup();
    	boundgroup.add(inbound);
    	boundgroup.add(outbound);
	boundgroup.add(bothbound);

    	detect = new JButton("Detect");
        detect.setActionCommand("Detect");
    	detect.addActionListener(this);
	detect.setBackground(Color.green);
	detect.setToolTipText("Start to test unknown emails");
	
	getMail = new JButton("Get Email");
        getMail.setActionCommand("getmail");
    	getMail.addActionListener(this);
	getMail.setBackground(Color.green);
	getMail.setToolTipText("Refresh the emails in the Result table");

	useModel = new JButton("Use Model");
        useModel.setActionCommand("usemodel");
    	useModel.addActionListener(this);
	useModel.setBackground(Color.cyan);
	useModel.setToolTipText("Use a pre-built model");

	testRange = new JButton("Test User Data Range");
        testRange.setActionCommand("testRange");
    	testRange.addActionListener(this);
	testRange.setBackground(Color.pink);
	//testRange.setToolTipText("");

	updateMEF = new JButton("Update MEF Model");
        updateMEF.setActionCommand("mef");
    	updateMEF.addActionListener(this);
	updateMEF.setBackground(Color.cyan);
	updateMEF.setToolTipText("Update the malicious emails to the model");

	checkMef = new JCheckBox("MEF");
	checkHellinger = new JCheckBox("Hellinger");
	checkClique = new JCheckBox("Clique");
	checkClique.setSelected(true);
	checkMef.setSelected(true);
	attachOnly = new JCheckBox("Attachment Only");
	attachOnly.setSelected(true);
	//attachOnly.setSelected(true);

    	tablemodel = new DefaultTableModel(null, columnNames){
    		public boolean isCellEditable(int row, int col) {return false;}
	    };
    
    	sorter = new HCSortFilterModelVsimulate(tablemodel);
	
    	table = new JTable(sorter);
    	//sorter.addMouseListener(table);
    	//table.addMouseListener(this);
	setTableRender();
    	tableScrollPane = new JScrollPane(table);
    	table.setPreferredScrollableViewportSize(new Dimension(790, 500));

        alarmRenderer = new DefaultTableCellRenderer() {
		public void setValue(Object value) {
		    if((value instanceof String)) {  
			if(((String)value).equals("alarm"))
			    setBackground(Color.red);
			else if(((String)value).equals("normal"))
			    setBackground(Color.green);
			else 
			    setBackground(Color.yellow);
		        setText((value == null) ? "" : value.toString());
		    }
		}
	    };
                
	EMTHelp emthelp = new EMTHelp(EMTHelp.VIRUSSCAN);
	
	JButton reporter = new JButton("Generate Reports");
	reporter.setToolTipText("Generate reports for each of the users using the range specified.");
	reporter.addActionListener(this);
	reporter.setBackground(Color.pink);

    	gbc.insets = new Insets(3,3,3,3);

	gbc.gridwidth = 1;
	gbc.gridx = 0;
    	gbc.gridy = 0;
    	inPanel.add(new JLabel("Email Type:"), gbc);

	gbc.gridx = 1;
    	gbc.gridy = 0;
    	inPanel.add(inbound, gbc);

	gbc.gridx = 2;
    	gbc.gridy = 0;
    	inPanel.add(outbound, gbc);

	gbc.gridx = 3;
    	gbc.gridy = 0;
    	inPanel.add(bothbound, gbc);
	
	gbc.gridx = 4;
    	gbc.gridy = 0;
    	inPanel.add(attachOnly, gbc);

	gbc.gridx = 0;
    	gbc.gridy = 1;
    	inPanel.add(new JLabel("Method Using:"), gbc);
	
	gbc.gridx = 1;
    	gbc.gridy = 1;
    	inPanel.add(checkClique, gbc);

	gbc.gridx = 2;
    	gbc.gridy = 1;
    	inPanel.add(checkHellinger, gbc);

	gbc.gridx = 3;
    	gbc.gridy = 1;
    	inPanel.add(checkMef, gbc);

	gbc.gridx = 0;
    	gbc.gridy = 2;
    	inPanel.add(getMail, gbc);

	gbc.gridx = 1;
    	gbc.gridy = 2;
    	inPanel.add(detect, gbc);
	/*
	gbc.gridx = 4;
    	gbc.gridy = 2;
    	inPanel.add(useModel, gbc);
	*/
	gbc.gridx = 2;
    	gbc.gridy = 2;
	//inPanel.add(new JLabel("HELP"), gbc);
	inPanel.add(emthelp, gbc);

	gbc.gridx = 3;
    	gbc.gridy = 2;
    	inPanel.add(reporter, gbc);

	//gbc.gridwidth = 1;
	gbc.gridx = 4;
    	gbc.gridy = 2;
    	inPanel.add(updateMEF, gbc);

	gbc.weightx = .16;
        gbc.gridwidth = 1;
	gbc.weighty = 0.75;
	gbc.gridwidth = 6;
        gbc.gridx = 0;
        gbc.gridy = 1;
        outPanel.add(tableScrollPane, gbc);

        gbc.weighty = 0.25;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(inPanel, gbc);

        gbc.weighty = 0.75;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(outPanel, gbc);

        setViewportView(panel);
    }

    public int[] getToday(){
	//get the date of today, the end of testing
	Calendar rightNow = Calendar.getInstance();
	Date d = rightNow.getTime();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	String today = format.format(d);

	String[] seg = today.split("-");
	int[] date = new int[3];
	date[0] = Integer.parseInt(seg[0]);
	date[1] = Integer.parseInt(seg[1]);
	date[2] = Integer.parseInt(seg[2]);
	return date;
    }

    //get the date as yy-mm-dd
    public String getSpinnerDate(SpinnerDateModel spinner){
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	return format.format(spinner.getDate());
    }

    public String[] getDate(){
	return parent.getDateRange_toptoolbar();
    }

	public void makeReport(){
		try{
		TableColumn tmpColumn = null;
		tmpColumn = table.getColumn("Clique");
		int clique = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Hellinger");
		int hellinger = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("MEF");
		int mef = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("ref");
		int ref = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Subject");
		int subject = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Attachment");
		int attachment = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Sender");
		int sender = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Recipient");
		int rcpt = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Time");
		int time = tmpColumn.getModelIndex();
		tmpColumn = table.getColumn("Label");
		int label = tmpColumn.getModelIndex();
		
		
		int size = table.getRowCount();
		int alertCnt = 0;
		String brief = "";
		String detail = "";
		String cvalue = "";
		String hvalue = "";
		String mvalue = "";
		String emailclass = "";

		for(int i=0;i<size;i++){
			alertCnt = 0;
			brief = "";
			detail = "";
			cvalue = (String)table.getValueAt(i,clique);
			hvalue = (String)table.getValueAt(i,hellinger);
			mvalue = (String)table.getValueAt(i,mef);
			emailclass = table.getValueAt(i,label).toString();
			
			if(cvalue.equals("alarm")){
				alertCnt++;
				brief += "clique   ";
				detail += "Clique Violation: This email violates all the cliques"
					+" of current selected account.\n\n";
			}
			if(hvalue.equals("alarm")){
				alertCnt++;
				brief += "hellinger   ";
				detail += "High Hellinger Distance: This email violates the user"
				+" frequency behavior.\n\n";
			}
			if(mvalue.equals("alarm")){
				alertCnt++;
				brief += "MEF   ";
				detail += "Malicious Attachment: The attachment of this email might"
				+" be malicious.\n\n";
			}
			
			if(alertCnt>=2 && !emailclass.equals("Malicious")){
				detail += "Subject: "+(String)table.getValueAt(i,subject);
				detail += "\nSender: "+(String)table.getValueAt(i,sender);
				detail += "\nRecipient: "+(String)table.getValueAt(i,rcpt);
				detail += "\nTime: "+(String)table.getValueAt(i,time);
				detail += "\nAttachment: "+(String)table.getValueAt(i,attachment);
				detail += "\nReference Number: "+(String)table.getValueAt(i,ref);
				m_alerts.alert(0, brief, detail);
			}
			
			else if(alertCnt>=1 && !emailclass.equals("Malicious")){
				detail += "Subject: "+(String)table.getValueAt(i,subject);
				detail += "\nSender: "+(String)table.getValueAt(i,sender);
				detail += "\nRecipient: "+(String)table.getValueAt(i,rcpt);
				detail += "\nTime: "+(String)table.getValueAt(i,time);
				detail += "\nAttachment: "+(String)table.getValueAt(i,attachment);
				detail += "\nReference Number: "+(String)table.getValueAt(i,ref);
				m_alerts.alert(1, brief, detail);
			}
		}
		}
		catch(Exception e){
			System.out.println("Abort reprot");	
		}
		
	}

    public void getmail(){
	(new Thread(new Runnable(){
		public void run(){
			BusyWindow bw = new BusyWindow("Progress","Running...",true);
		    bw.progress(0,4);
		    bw.setVisible(true);
		    bw.setMSG("Reading Database...");
		    String[] tmpd = getDate();
		    String start = tmpd[0];
		    String end = tmpd[1];
		    String user = (String)parent.getSelectedUser();
		    String query = "";
		    if(inbound.isSelected()){
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e left join message"
			    +" m on e.mailref=m.mailref where e.rcpt='"
			    +user+"' and e.DATEs>'"+start+"' and "
			    +" e.DATEs<'"+end+"'";
		    }
		    else if(outbound.isSelected()){
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e left join message"
			    +" m on e.mailref=m.mailref where e.sender='"
			    +user+"' and e.DATEs>'"+start+"' and "
			    +" e.DATEs<'"+end+"'";
		    }
		    else{
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e left join message"
			    +" m on e.mailref=m.mailref where (e.rcpt='"
			    +user+"' or e.sender='"+user+"') and "
			    +"e.DATEs>'"+start+"' and "
			    +"e.DATEs<'"+end+"'";
		    }

		    if(attachOnly.isSelected()){
			query += " and e.numattach > 0 and m.filename!=''";
		    }
		    query += " order by e.utime";
		    //System.out.println(query);
		    bw.progress(1,4);
		    String[][] data = runSql(query);
		    
		    bw.setMSG("Updating Table...");
		    bw.progress(2,4);
		    String[][] tabledata= null;

		    if(data.length>0){
		    	tabledata = new String[data.length][data[0].length+4];
			for(int i=0;i<data.length;i++){
			    for(int j=0;j<data[i].length-1;j++){
				tabledata[i][j] = data[i][j];
			    }
			    
			    tabledata[i][6] = "Normal";
			    tabledata[i][7] = "NA";
			    tabledata[i][8] = "NA";
			    tabledata[i][9] = "NA";
			    
			    tabledata[i][10] = data[i][data[i].length-1];
			}
		    }
		    
		    bw.progress(3,4);
		    updateTable(tabledata);

		    bw.progress(4,4);
		    bw.setVisible(false);
		}
	    })).start();
    }

    public void updateTable(String[][] tabledata){

	this.outPanel.remove(tableScrollPane);

	//if(tabledata!=null){
	    //set up the table again
	tablemodel = new DefaultTableModel(tabledata, this.columnNames){
		public boolean isCellEditable(int row, int col) {
		    if(col==5)
			return true;
		    else return false;
		}
	    };
	    
	sorter = new HCSortFilterModelVsimulate(tablemodel);
	table = new JTable(sorter);
	    //}
	    //else{
	    //table = new JTable();
	    //}


	table.addMouseListener(this);
	sorter.addMouseListener(table);
	
	setTableRender();
	
	tableScrollPane = new JScrollPane(table);
	table.setPreferredScrollableViewportSize(new Dimension(790, 500));

	// set up the gbc again
	this.gbc.weighty = 0.75;
	this.gbc.gridwidth = 6;
	this.gbc.gridx = 0;
	this.gbc.gridy = 3;
	this.outPanel.add(tableScrollPane, gbc);
	
	setViewportView(panel);
    }

    public void setTableRender(){
	TableColumn tmpColumn = null;

	(table.getColumn("Sender")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Recipient")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Subject")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Time")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Attachment")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Label")).setCellRenderer(m_md5Renderer);
	(table.getColumn("Folder")).setCellRenderer(m_md5Renderer);

	tmpColumn = table.getColumn("Hellinger");
	tmpColumn.setCellRenderer(alarmRenderer);
	tmpColumn.setMaxWidth(60);

	tmpColumn = table.getColumn("Clique");
	tmpColumn.setCellRenderer(alarmRenderer);
	tmpColumn.setMaxWidth(60);

	tmpColumn = table.getColumn("MEF");
	tmpColumn.setCellRenderer(alarmRenderer);
	tmpColumn.setMaxWidth(60);
	
	tmpColumn = table.getColumn("ref");
	tmpColumn.setCellRenderer(m_md5Renderer);
	tmpColumn.setPreferredWidth(10);
	
	//setup the label
	JComboBox tableCombo = new JComboBox();
	tableCombo.addItem("Unknown");
	tableCombo.addItem("Normal");
	tableCombo.addItem("Malicious");
	
	tableCombo.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt){
		    JComboBox source = (JComboBox)evt.getSource();

		    int c = ((JComboBox)source).getSelectedIndex();

		    if(c==0){
			((JComboBox)source).setBackground(Color.gray);
		    }
		    else if(c==1){   
			((JComboBox)source).setBackground(Color.green); 
		    }
		    else{
			((JComboBox)source).setBackground(Color.red);
		    }
		}
	    });
	
	TableColumn interestColumn = table.getColumn("Label");
	interestColumn.setCellEditor(new DefaultCellEditor(tableCombo));
	interestColumn.setMaxWidth(65);
	
	
	DefaultTableCellRenderer interestColumnRenderer = new DefaultTableCellRenderer(){
		public void setValue(Object value){
		    if(value instanceof String){  
			if(((String)value).equals("Normal")){
			    setBackground(Color.green);
			    setText("Normal");
			}
			else if(((String)value).equals("Malicious")){
			    setBackground(Color.red);
			    setText("Malicious");
			}
			else if(((String)value).equals("Unknown")){
			    setBackground(Color.gray);
			    setText("Unknown");
			}
		    }
		    else
			setText((value == null) ? "" : value.toString());
		}
	    };
	interestColumnRenderer.setToolTipText("Click for Class box");
	interestColumn.setCellRenderer(interestColumnRenderer);
	
	classMenu = new JPopupMenu("Set Class");
        classMenu.add("Unknown").addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    setTableValue("Unknown");
		    panel.repaint();
		}
	    });
	classMenu.add("Normal").addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    setTableValue("Normal");
		    panel.repaint();
		}
	    });
	classMenu.add("Malicious").addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    setTableValue("Malicious");
		    panel.repaint();
		}
	    });
    }

    public void setTableValue(String value){
	int[] rows = table.getSelectedRows();

	//have to change related same mail.... the join table bug
	Vector ref = new Vector();

	for(int i=0; i<rows.length;i++){
	    table.setValueAt(value, rows[i], 6);
	    String tmpref = (String)table.getValueAt(rows[i],10);
	    if(!ref.contains(tmpref))ref.add(tmpref);
	    //System.out.println(tmpref+" "+rows[i]);
	}


	for(int i=0;i<table.getRowCount();i++){
	    String tmpref = (String)table.getValueAt(i,10);
	    if(ref.contains(tmpref)){
		table.setValueAt(value, i, 6);
	    }
	}
    }

    public void testRealVirus(){
    	(new Thread(new Runnable(){
		public void run(){
			BusyWindow bw = new BusyWindow("Progress","Running...",true);
		    int rows = table.getRowCount();
		    Vector tmpNormal = new Vector();//mailref
		    Vector tmpUnknown = new Vector();
		    Vector tmpMalicious = new Vector();
		    Vector fileNormal = new Vector();//filename
		    Vector fileUnknown = new Vector();
		    Vector fileMalicious = new Vector();

		    Vector normalIndex = new Vector();
		    Vector unknownIndex = new Vector();
		    Vector maliciousIndex = new Vector();

		    for(int i=0;i<rows;i++){
			String tmpClass = table.getValueAt(i,6).toString();
			String tmps = table.getValueAt(i,10).toString();
			String file = table.getValueAt(i,4).toString();
			if(tmpClass.equals("Normal")){
			    tmpNormal.add(tmps);
			    fileNormal.add(file);
			    normalIndex.add(String.valueOf(i));
			}
			if(tmpClass.equals("Unknown")){
			    tmpUnknown.add(tmps);
			    fileUnknown.add(file);
			    unknownIndex.add(String.valueOf(i));
			}
			if(tmpClass.equals("Malicious")){
			    tmpMalicious.add(tmps);
			    fileMalicious.add(file);
			    maliciousIndex.add(String.valueOf(i));
			}
		    }
		    int maxp = 10+tmpUnknown.size();
		    int current = 0;
		    bw.progress(current++,maxp);
		    bw.setVisible(true);
		    bw.setMSG("Setup Data...");


		    String query = "";
		    String user = (String)parent.getSelectedUser();

		    bw.progress(current++,maxp);
		    bw.setMSG("Setup Normal Data...");
		    //get the normal emails
		    String[][] normalEmail = null;
		    String[][] unknownEmail = null;
		    
		    if(tmpNormal.size()>0){
			query = "select e.sender,e.mailref,e.insertTime,e.numattach"
			    +" from email e,message m where (";
			
			String allref = new String();
			for(int i=0;i<tmpNormal.size();i++){
			    allref = allref.concat("(m.mailref='");
			    allref = allref.concat((String)(tmpNormal.get(i)));
			    allref = allref.concat("' and m.filename='");
			    String ref = (String)(fileNormal.get(i));
			    ref = ref.replaceAll("'", "\\\\'");
			    allref = allref.concat(ref);
			    allref = allref.concat("')");
			    if(i<tmpNormal.size()-1)allref = allref.concat(" or ");
			    else allref = allref.concat(")");
			    
			}
			query = query.concat(allref);
			
			query = query.concat(" and (e.mailref=m.mailref)");

			if(inbound.isSelected()){
			    query += " and e.rcpt='"+user+"'";
			}
			else if(outbound.isSelected()){
			    query += " and e.sender='"+user+"'";
			}
			else{
			    query += " and (e.rcpt='"+user+"' or e.sender='"+user+"')";
			}
			query += " order by e.utime";
			normalEmail = runSql(query);
		    }
		    
	
		    bw.progress(current++,maxp);
		    bw.setMSG("Setup Unknown Data...");
		    //get the unknown emails
		    
		    if(tmpUnknown.size()>0){
			
			query = "select e.sender,e.mailref,e.insertTime,e.numattach"
			    +" from email e,message m where (";

			String allref = new String();
			for(int i=0;i<tmpUnknown.size();i++){
			    allref = allref.concat("(m.mailref='");
			    allref = allref.concat((String)(tmpUnknown.get(i)));
			    allref = allref.concat("' and m.filename='");
			    String ref = (String)(fileUnknown.get(i));
			    ref = ref.replaceAll("'", "\\\\'");
			    allref = allref.concat(ref);
			    allref = allref.concat("')");
			    if(i<tmpUnknown.size()-1)allref = allref.concat(" or ");
			    else allref = allref.concat(")");
			    
			}
			query = query.concat(allref);
			query = query.concat(" and (e.mailref=m.mailref)");

			if(inbound.isSelected()){
			    query += " and e.rcpt='"+user+"'";
			}
			else if(outbound.isSelected()){
			    query += " and e.sender='"+user+"'";
			}
			else{
			    query += " and (e.rcpt='"+user+"' or e.sender='"+user+"')";
			}
			query += " order by e.utime";
			unknownEmail = runSql(query);
			bw.setMSG("Start Detecting...");
			//System.out.println(query);
			//for(int i=0;i<unknownEmail.length;i++){
			//    System.out.println(unknownEmail[i][0]+" "+unknownEmail[i][1]);
			//}
			//System.out.println(unknownEmail.length+" "+unknownIndex.size());

			/******** clique **********/
			if(checkClique.isSelected()){
			    bw.setMSG("Clique Violation...");
			    bw.progress(current++,maxp);
			    System.out.println("clique...");
			    UserClique uc = new UserClique(normalEmail);
			    uc.findMinSets();
			    Vector clique = uc.getMinSets();
			    Vector today = grouping(unknownEmail);
			    //String user = (String)userCombo.getSelectedItem();
			    boolean[] groupAlarm = cliqueViolation(today, clique, user);
			    boolean[] individualAlarm = new boolean[unknownEmail.length];
			    
			    for(int i=0;i<today.size();i++){
				Vector groupi = (Vector)today.get(i);
				for(int j=0;j<groupi.size();j++){
				    GroupType node = (GroupType)groupi.get(j);
				    int index = node.index;
				    individualAlarm[index] = groupAlarm[i];
				    
				    //with attachment only
				    if(Integer.parseInt(unknownEmail[index][3])==0)
					individualAlarm[index] = false;
				}
			    }
			    //System.out.println(normalEmail.length+" "+normalIndex.size());
			    //System.out.println(individualAlarm.length+" "+unknownIndex.size());

			    bw.progress(current++,maxp);

			    for(int i=0;i<individualAlarm.length;i++){
				int ind = Integer.parseInt((String)unknownIndex.get(i));
				if(individualAlarm[i])table.setValueAt("alarm",ind,8);
				else table.setValueAt("normal",ind,8);
			    }
			    
			    for(int i=0;i<normalIndex.size();i++){
				int ind = Integer.parseInt((String)normalIndex.get(i));
				table.setValueAt("normal",ind,8);
			    }
			    
			    for(int i=0;i<maliciousIndex.size();i++){
				int ind = Integer.parseInt((String)maliciousIndex.get(i));
				table.setValueAt("alarm",ind,8);
			    }
			}

			/******** Hellinger **********/
			if(checkHellinger.isSelected()){
			    bw.setMSG("Hellinger Distince...");
			    bw.progress(current++,maxp);
			    System.out.println("hellinger...");

			    if(normalEmail.length > 5*unknownEmail.length){
				HellingerTester he = new HellingerTester();
				boolean[] hellingerAlarm = he.filter(normalEmail, unknownEmail);
				
				bw.progress(current++,maxp);
				for(int i=0;i<hellingerAlarm.length;i++){
				    int ind = Integer.parseInt((String)unknownIndex.get(i));
				    if(hellingerAlarm[i])table.setValueAt("alarm",ind,7);
				    else table.setValueAt("normal",ind,7);
				}
			    }
			    else{
				JOptionPane.showMessageDialog(null,
							      "To test Hellinger Distance, the number"
							      +" of Normal Emails must be >= 5 *"
							      +" number of Unknown Emails\n",
							      "Message",
							      JOptionPane.INFORMATION_MESSAGE);
			    }

			    for(int i=0;i<normalIndex.size();i++){
				int ind = Integer.parseInt((String)normalIndex.get(i));
				table.setValueAt("normal",ind,7);
			    }
			    
			    for(int i=0;i<maliciousIndex.size();i++){
				int ind = Integer.parseInt((String)maliciousIndex.get(i));
				table.setValueAt("alarm",ind,7);
			    }
			}

			/******** MEF **********/
			if(checkMef.isSelected()){
			    bw.setMSG("MEF...slow, please wait...");
			    bw.progress(current++,maxp);

			    boolean[] mefresult = new boolean[tmpUnknown.size()];
			    for(int i=0;i<mefresult.length;i++){
				String fref = (String)(fileUnknown.get(i));
				fref = fref.replaceAll("'", "\\\\'");
				System.out.println(fref);
				if(fref.equals("")){
				    mefresult[i] = false;
				}
				else{
				    String ref = (String)tmpUnknown.get(i);
				    mefresult[i] = callMEF(ref, fref, user);
				}
				bw.progress(current++,maxp);
			    }

			    for(int i=0;i<mefresult.length;i++){
				    int ind = Integer.parseInt((String)unknownIndex.get(i));
				    if(mefresult[i])table.setValueAt("alarm",ind,9);
				    else table.setValueAt("normal",ind,9);
			    }
			    
			    for(int i=0;i<normalIndex.size();i++){
				int ind = Integer.parseInt((String)normalIndex.get(i));
				table.setValueAt("normal",ind,9);
			    }
			    
			    for(int i=0;i<maliciousIndex.size();i++){
				int ind = Integer.parseInt((String)maliciousIndex.get(i));
				table.setValueAt("alarm",ind,9);
			    }
			}
		    }// unknown > 0
		    else{
			bw.progress(current++,maxp);
			for(int i=0;i<normalIndex.size();i++){
			    int ind = Integer.parseInt((String)normalIndex.get(i));
			    if(checkHellinger.isSelected()) table.setValueAt("normal",ind,7);
			    if(checkClique.isSelected()) table.setValueAt("normal",ind,8);
			    if(checkMef.isSelected()) table.setValueAt("normal",ind,9);
			    
			}
			bw.progress(current++,maxp);
			for(int i=0;i<maliciousIndex.size();i++){
			    int ind = Integer.parseInt((String)maliciousIndex.get(i));
			    if(checkHellinger.isSelected()) table.setValueAt("alarm",ind,7);
			    if(checkClique.isSelected()) table.setValueAt("alarm",ind,8);
			    if(checkMef.isSelected()) table.setValueAt("alarm",ind,9);
			}
			
		    }

		    bw.progress(current++,maxp);
		    panel.repaint();
		    bw.progress(current++,maxp);
		    bw.setVisible(false);

		}
	    })).start();
    }

    public boolean callMEF(String ref, String fref, String user){

	MEFDetect mef = new MEFDetect();
	//int window = 10;
	//double threshold = 0.2;

	String from = " from email e,message m where ((m.mailref='"
	    +ref+"' and m.filename='"+fref
	    +"')) and (e.mailref=m.mailref)";
	if(inbound.isSelected()){
	    from += " and e.rcpt='"+user+"'";
	}
	else if(outbound.isSelected()){
	    from += " and e.sender='"+user+"'";
	}
	else{
	    from += " and (e.rcpt='"+user+"' or e.sender='"+user+"')";
	}
	from += " order by e.utime";		    
	
	String selectFilename = "select m.filename, m.size"+from;
	String selectBody = "select m.body"+from;
	byte[] att = getAttachment(selectFilename, selectBody);
	if(att == null || att.length<MEFwindow){return false;}
	    return mef.detect2(fref, MEFwindow, MEFthreshold, att);
    }

    public final byte[] getAttachment(String filesql, String bodysql){
	byte[] att = null;
	String[][] data;
	try{
	    data = jdbc.getSQLData(filesql);
	    //data = jdbc.getRowData();

	    //if size is too big, not a virus
	    int size = Integer.parseInt(data[0][1]);
	    if(size>500000)return null;

	    att = jdbc.getBinarySQLData(bodysql)[0];
	}
	catch(Exception e){
	    System.out.println("Attachment error:"+e);
	}

	return att;
    }

    public final String getAttachment(String filesql, String bodysql, String ref, String filename){

	String tempfile = "TEMP_ATT_";
	try{
	    //get the filename,body off this hash
	    String data[][];
	    File fi;

	    data = jdbc.getSQLData(filesql);
	    //data = jdbc.getRowData();
	    //System.out.println("here");
	    //if size is too big, not a virus
	    int size = Integer.parseInt(data[0][1]);
	    if(size>500000)return null;
    
	    if(ref.length()>5)
		tempfile += ref.substring(0,5);
	    else
		tempfile += ref;

	    data[0][0] = data[0][0].replaceAll(" ","");
	    tempfile += "_"+data[0][0].trim();
	    //tempfile += "_"+filename;
	    //System.out.println("will check: " + tempfile);

	    fi = new File(tempfile);
	    //if (fi.exists())
	    //return tempfile;

	    FileOutputStream fout = new FileOutputStream(fi);
	    byte[] bt = jdbc.getBinarySQLData(bodysql)[0];
	    fout.write(bt);
	    System.out.println(bt.length);
	    fout.flush();
	    fout.close();
	    //System.out.println("done");

	}catch(SQLException s){
	    System.out.println("s is : " + s);
	    return null;
	}
	catch(IOException e){
	    System.out.println("e is " + e);
	    return null;
	}
	return tempfile;
    }

    public void updateMEFmodel(){
	
	(new Thread(new Runnable(){
		public void run(){
	
		    int rows = table.getRowCount();
		    Vector files = new Vector();
		    Vector ref = new Vector();
		    //int window = 10;
		    //double threshold = 0.2;
		    String message = "Update this viral file(s) to MEF model?\n";

		    for(int i=0;i<rows;i++){
			String tmpClass = table.getValueAt(i,6).toString();
			String tmps = table.getValueAt(i,10).toString();
			String file = table.getValueAt(i,4).toString();
			if(tmpClass.equals("Malicious")){
			    ref.add(tmps);
			    files.add(file);
			    message += file+"\n";
			}
		    }

		    int yes = 1;
		    if(files.size()>0)
			yes = JOptionPane.showConfirmDialog(null,message,"MEF",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
		    if(yes==0){
			int length = files.size();
			BusyWindow bw = new BusyWindow("Progress","Running...",true);
			bw.progress(0,length);
			bw.setVisible(true);
			bw.setMSG("Updating...");
			MEFDetect mef = new MEFDetect();
			
			for(int i=0;i<files.size();i++){
			    String filename = (String)files.get(i);
			    filename = filename.replaceAll("'", "\\\\'");
			    String reff = (String)ref.get(i);
			    String sql = " from message where filename='"+filename
				+"' and mailref='"+reff+"'";
			    String filesql = "select filename,size"+sql;
			    String bodysql = "select body"+sql;

			    byte[] att = getAttachment(filesql, bodysql);
			    if(att!=null && att.length>MEFwindow)
				mef.updateModel2(att, filename, MEFwindow, MEFthreshold);
			    bw.progress(i,length);
			}
			bw.setVisible(false);
		    }
		}
	    })).start();
    }

    //group the one-day emails by mailref
    public Vector grouping(String[][] data){
	Vector group = new Vector();
	HashMap table = new HashMap();

	//group by type
	for(int i=0;i<data.length;i++){
	    String key = data[i][1];
	    if(table.containsKey(key)){
		Vector v = (Vector)table.remove(key);
		GroupType gt = new GroupType(key, data[i][0]);
		gt.index = i;
		gt.time = data[i][2];
		v.add(gt);
		table.put(key, v);
	    }
	    else{
		Vector v = new Vector();
		GroupType gt = new GroupType(key, data[i][0]);
		gt.index = i;
		gt.time = data[i][2];
		v.add(gt);
		table.put(key, v);
	    }
	}

	//read the groups
	int index = 0;
	Set entries = table.entrySet();
	Iterator iter = entries.iterator();
	while(iter.hasNext()){
	    Map.Entry entry = (Map.Entry)iter.next();
	    //Object key = (Object)entry.getKey();
	    Vector v = (Vector)entry.getValue();
	    group.add(v);
	    index++;
	}

	return group;
    }

    //check clique violation for groups
    public boolean[] cliqueViolation(Vector group, Vector cliques, String user){
	boolean[] violation = new boolean[group.size()];

	for(int i=0;i<group.size();i++){
	    Vector groupi = (Vector)group.get(i);
	    //GroupType node = (GroupType)group.get(i);

	    for(int j=0;j<cliques.size();j++){
		Vector oneclique = (Vector)cliques.get(j);
		if(check(groupi, oneclique, user)){//is in the clique
		    violation[i] = false;
		    break;
		}
		else violation[i] = true;
	    }
	}

	return violation;
    }

    //check clique violation for a single clique
    public boolean check(Vector sub, Vector clique, String user){
	
	for(int i=0;i<sub.size();i++){
	    GroupType node = (GroupType)sub.get(i);
	    String test = node.value;
	    if(!test.equals(user))
		if(!clique.contains(test))return false;
	}
	return true;
    }
/*
    public static long getMill(String in){
        if(in==null)return 0;
        String[] dt=in.split(" ");
        String[] date=dt[0].split("-");
                                                                                                                                       
        Calendar c=Calendar.getInstance();
        c.set(Integer.parseInt(date[0])
              ,Integer.parseInt(date[1])-1
              ,Integer.parseInt(date[2]));                                                                                                        

        long ms = c.getTimeInMillis();
        return ms;
    }
*/

    public void setAlert(){
	int row = table.getRowCount();
	for(int i=0;i<row;i++){
	    String label = (String)table.getValueAt(i,6);
	    String h = (String)table.getValueAt(i,7);
	    String c = (String)table.getValueAt(i,8);
	    String m = (String)table.getValueAt(i,9);

	    String brief = "brief";
	    String detailed = "detailed";
	    int alertCount = 0;

	    if(h.equals("alarm")){
		alertCount++;
	    }
	    else{
		
	    }

	    if(c.equals("alarm")){
		alertCount++;
	    }
	    else{

	    }

	    if(m.equals("alarm")){
		alertCount++;
	    }
	    else{

	    }
	    if(alertCount>0)m_alerts.alert(1, brief, detailed);
	}
    }

    public void actionPerformed(ActionEvent evt) {
        String arg = evt.getActionCommand();	
	//System.out.println("action:"+arg);
		
    	if(arg.equals("Detect")) {
	    //MEFDetect mef = new MEFDetect();
	    //mef.buildAllNewModel("train", 10, 0.2);
	    testRealVirus();
	    //setAlert();
            //MEFdetect m = new MEFdetect("win","MEFModel");
        }
	else if(arg.equals("getmail")){
	    getmail();
	}
	else if(arg.equals("usemodel")){
	    JOptionPane.showMessageDialog(this,
					  "To Be Continue.....\n",
					  "Message",
					  JOptionPane.INFORMATION_MESSAGE);
	}
	else if(arg.equals("testRange")){
	    //testRange();
	}
	else if(arg.equals("Generate Reports")){
		makeReport();
	}
	else if(arg.equals("mef")){
	    updateMEFmodel();
	}
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {
	Point point = e.getPoint();
        int row = table.rowAtPoint(point);
	int button = e.getButton();

	if(button==1 && e.getClickCount()==2){
	    String mailref = (String)sorter.getValueAt(row, 9);
	    //table.setSelectionBackground(Color.blue);
	    //showEmailBody(mailref);
	}
	else if(button>1)
	    classMenu.show(table, point.x, point.y);
    }  

    public String showEmailBody(String mailref) {

	//	if(showWindow){
	    /*	    
	    String title = "Email Detail";
	    DetailDialog dialog = new DetailDialog(null, false, title, message);
	    dialog.setLocationRelativeTo(this);
	    dialog.show();
	    */
	    //emailmessage.setText(message);
	    //emailmessage.setCaretPosition(0);
	    //}

	/*
        String title = "Email Message Body";
    	String message = "";
        String query = "SELECT email.sender,email.rcpt,message.type,message.filename,message.body from email left join message on email.mailref = message.mailref where email.mailref = '"+mailref+"'";
        //System.out.println(query);
        String[][] queryResult = runSql(query);
        if(queryResult != null && queryResult.length > 0) {
            message += "Sender: "+queryResult[0][0]+"\n";
            message += "Recipients: ";
            for(int i=0;i<queryResult.length;i++) {
                message += queryResult[i][1];
                if(i != queryResult.length-1) {
                    message += ",\n";
                }
            }  
            message += "\nType: "+queryResult[0][2]+"\n";
            message += "Filename: "+queryResult[0][3]+"\n";
            message += "Content:\n";
            message += queryResult[0][4]+"\n"; 
        } else { 
            message += "This was a dummy email\n";
        }

        JOptionPane.showMessageDialog(this,message,title,JOptionPane.INFORMATION_MESSAGE); 
	*/
        return "";
    }

    public String[][] runSql(String sql) {
        String[][] data = null;
        try {
            synchronized (jdbc) {
                data = jdbc.getSQLData(sql);
                //data = jdbc.getRowData();
            }
        } catch (SQLException ex) {
            if(!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
                System.out.println("ex: "+ex);
                System.out.println("sql: " +sql);
	    }
        }
        return data;
    }   


    private class GroupType{
    int index;
    String ref;//type or mailref
    String value;
    String time;

    public GroupType(String r, String v){
	ref = r;
	value = v;
    }
}

}
    

class HCSortFilterModelVsimulate extends AbstractTableModel {  
    public HCSortFilterModelVsimulate(TableModel m) {  
	model = m;
	rows = new Row[model.getRowCount()];
	for (int i = 0; i < rows.length; i++) {  
	    rows[i] = new Row();
	    rows[i].index = i;
	}
    }
	
    public void sort(int c) {  
	sortColumn = c;
	Arrays.sort(rows);
	fireTableDataChanged();
    }
	
    public void addMouseListener(final JTable table) {  
	table.getTableHeader().addMouseListener(new MouseAdapter() {  
                public void mouseClicked(MouseEvent event) {  
		    // check for double click
		    if (event.getClickCount() < 2) return;
					
		    // find column of click and
		    int tableColumn
			= table.columnAtPoint(event.getPoint());
					
		    // translate to table model index and sort
		    int modelColumn
			= table.convertColumnIndexToModel(tableColumn);
		    sort(modelColumn);
		}
	    });
    }
	
    /* compute the moved row for the three methods that access
       model elements
    */
	
    public Object getValueAt(int r, int c)
    {  return model.getValueAt(rows[r].index, c);
    }
	
    public boolean isCellEditable(int r, int c)
    {  return model.isCellEditable(rows[r].index, c);
    }
	
    public void setValueAt(Object aValue, int r, int c)
    {  model.setValueAt(aValue, rows[r].index, c);
    }
	
    /* delegate all remaining methods to the model
     */
	
    public int getRowCount()
    {  return model.getRowCount();
    }
	
    public int getColumnCount()
    {  return model.getColumnCount();
    }
	
    public String getColumnName(int c)
    {  return model.getColumnName(c);
    }
	
    public Class getColumnClass(int c)
    {  return model.getColumnClass(c);
    }
	
    /* this inner class holds the index of the model row
       Rows are compared by looking at the model row entries
       in the sort column
    */
	
    private class Row implements Comparable
    {  public int index;
	public int compareTo(Object other)
	{  Row otherRow = (Row)other;
	Object a = model.getValueAt(index, sortColumn);
	Object b = model.getValueAt(otherRow.index, sortColumn);
	if (a instanceof Comparable)
	    return ((Comparable)a).compareTo(b);
	else
	    return index - otherRow.index;
	}
    }
	
    private TableModel model;
    private int sortColumn;
    private Row[] rows;
}

class HellingerTester{
	
    private int numUserWin=50;//other two testing window size
    private int HDistance = 50;//Hellinger window size

    //when data is not enough, we need to test more users
    //this is a bound to limit the testing range
    private int databound = 60;
	
    //some constants for testing
    private final int thrSize = 20;//size of Standard Deviation
  //  private final int stSize = 100;//size of Moving Average xStandard Deviation
    private final double alpha = 0.1;//alpha
    private final double accuThreshold = 1.2;//the accumulative stuff threshold
	
    double[] userWin = null;//number of distinct sender/rcpt
    double[] attachWin = null;//number of emails with attachment
    double[] hellinger = null;//the hellinger curve
    double[] testThreshold = null;//for test, if need
    Vector accuAttach =new Vector();//accumulative attachment

    boolean[] dailyResult;
	
    public HellingerTester(){
		
    }
	
    int trainEnd = 0;//the end location of current training
    int dayCount = 0;//the number of emails in current day
    boolean[] flags = null;//return a list of boolean
    int flagLog = 0;//an index of current flag
    String[][] traindata=null;

    boolean hasbadday=false;
    /**
       start to train and test
       @param data the entire testing emails
    */
    public boolean[] filter(String[][] data, int trainLength){

	flags = new boolean[data.length];
	flagLog = trainLength-1;
	for(int i=0; i<trainLength-1;i++)flags[i]=false;
		
	//compute the first time, add the testing data of the first
	//day to training data
	traindata = trainFirst(data, trainLength);

	HDistance=dayCount;
	//dynamic hellinger window size
	if(HDistance>databound)HDistance=databound;
	else if(HDistance<20)HDistance=20;
	else HDistance = dayCount;

	//put dayCount in trainFirst
	calculateProfile(traindata);
	hellinger = calculateHellinger(traindata);
	compute(hellinger);
	
	//combine the accumulative stuff
	computeAccuTrain(traindata);
	boolean accu = testAccu();
	if(accu)hasbadday=true;

	countResult2(dailyResult,accu);
	flagLog = traindata.length;
		
	//then, day by day
	while(trainEnd+1 < data.length){
	    traindata = trainData(traindata, data);

	    HDistance=dayCount;
	    if(HDistance>databound)HDistance=databound;
	    else if(HDistance<20)HDistance=20;
	    else HDistance = dayCount;

	    calculateProfile(traindata);//numOfUser and numOfAttachment
	    hellinger = calculateHellinger(traindata);//hellinger
	    
	    
	    if(hellinger!=null){
		compute(hellinger);

		//combine the accumulative stuff
		computeAccuTrain(traindata);
		accu = testAccu();
		if(accu)hasbadday=true;
		countResult2(dailyResult, accu);
	    }
	    
	}
	//System.out.println("ok");
	return flags;
    }
	
    /**
       count the resutl in one train.(one day)
    */
    public void countResult(boolean[] checklist, boolean accu){
	int length = checklist.length;
	int index = length - dayCount;
	
	boolean bad_today = false;//today is bad
	//new, combine with accumulative stuff
	for(int i=0;i<dayCount;i++){
	    if(checklist[index] && accu){
		bad_today=true;
		break;
	    }
	    index++;
	}

	if(bad_today){
	    for(int i=0;i<dayCount;i++){
		flags[flagLog] = true;
		flagLog++;
	    }
	}
	else{
	    for(int i=0;i<dayCount;i++){
		flags[flagLog] = false;
		flagLog++;
	    }
	}

	/*
	//old algo
	for(int i=0;i<dayCount;i++){
	    flags[flagLog] = checklist[index];
	    index++;
	    flagLog++;
	}
	*/
    }

    /**
       count the resutl in one train.(one day)
       second way to use "bad_day"
    */
    public void countResult2(boolean[] checklist, boolean accu){
	int length = checklist.length;
	int index = length - dayCount;
	
	boolean bad_today = false;//today is bad
	//new, combine with accumulative stuff
	for(int i=0;i<dayCount;i++){
	    if(checklist[index] && !accu){
		bad_today=true;
		break;
	    }
	    index++;
	}

	if(bad_today){
	    for(int i=0;i<dayCount;i++){
		flags[flagLog] = false;
		flagLog++;
	    }
	}
	else{
	    index = length - dayCount;
	    for(int i=0;i<dayCount;i++){
		flags[flagLog] = checklist[index];
		index++;
		flagLog++;
	    }
	}

    }
	
    /**
       calculate the number of distince recipients and
       attachments in current window
       @param data the input email data
    */
    public void calculateProfile(String[][] data){ 		
	//distinct rcpt
	Vector userlist = new Vector();
	Vector attlist = new Vector();

	//I use "trainEnd" and dayCount to save time
	//don't count from the first everytime
	
	int attcount = 0;//attachment counter
	int testlength = databound*3;//evaluating range
	int totallength = data.length;//total datalength
	
	//number of distinct rcpt until now, in the window
	userWin = new double[testlength];
	attachWin = new double[testlength];
	HashMap table = new HashMap();//use hash table to speed up
	int shift = totallength-testlength;//a shift to speed up

	for(int i=shift; i<totallength; i++){ 
	    //count the number of distinct rcpt
	    //if(!table.containsKey(data[i][0])){ 
	    table.put(data[i][0],"1");  
	    //}
			
	    //numUserWin is the window size
	    if ( (i-shift) < numUserWin ){//before the window is full
	        userlist.add(data[i][0]);
		
		//if has attachment
		if(Integer.parseInt(data[i][3])>0){ 
		    attcount++;
		}
	    }
	    else{  
		String last = (String)userlist.remove(0);
		userlist.add(data[i][0]);          
				
		if (!userlist.contains(last)){  
		    table.remove(last);
		}
				
		//for attachment
		if(Integer.parseInt(data[i][3])>0){  
		    attcount++;
		}
				
		if(Integer.parseInt(data[i-numUserWin][3])>0){
		    attcount--;
		}
	    }
	    userWin[i-shift] = table.size();
	    attachWin[i-shift] = attcount;
	}//for (int i=0; i<data.length; i++)
    }
	
	
    /**
       @param data the input email data
    */
    public double[] calculateHellinger(String[][] data){
		
	SimpleHashtable simpleHash = new SimpleHashtable();
		
	double[] distance = new double[data.length];
	int disIndex = 0;

	//shift to speed up
	int shift = data.length - (HDistance*5 + databound*3);
	if(shift<0) shift=0;
	shift=0;
	//System.out.println(shift);
		
	double trainFreq = 1.0/((double)HDistance*4);
	double testFreq = 1.0/HDistance;
		
	if (HDistance*5 > data.length)
	    return null;
		
	int count = 0;
	//build hash table for data 0 to HDistance*5
	for(int i=shift; count<HDistance*5; i++){
	    String name = data[i][0];
	    if( count < HDistance*4 ){
		simpleHash.insert(name,"train",trainFreq,0);
	    }else{
		simpleHash.insert(name,"test",testFreq, 0);
	    }
			
	    if( count < HDistance*5-1 ){
		//distance.addElement(new Double(0.0));
		distance[disIndex] = 0.0;
		disIndex++;
	    }

	    count++;
	}
		
	//compute distance in the first term
	Vector theKey = simpleHash.getKeys();
	double sum = 0;
	for(int i=0; i<theKey.size(); i++){
	    String name = (String)theKey.get(i); 
	    double train = simpleHash.getTrain(name);
	    double test = simpleHash.getTest(name);
	    sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	}
		
	//distance.addElement(new Double(sum));// first term's distance
	distance[disIndex] = sum;
	disIndex++;

	//build hash table for all of terms and compute distances
	for (int i=shift; i< data.length-(HDistance*5); i++){//total runing times

	    double summ =0;
	    //cut first for training
	    String name = data[i][0];
	    simpleHash.remove(name, "train", trainFreq);
			
	    //add last for training
	    name = data[i+HDistance*4][0];
	    simpleHash.insert(name, "train", trainFreq,0);
			
	    //cut first for test
	    name = data[i+HDistance*4][0];
	    simpleHash.remove(name, "test", testFreq);
	    //add last for test
	    name = data[i+HDistance*5][0];
	    simpleHash.insert(name, "test", testFreq,0 );
			
	    //compute distance
	    theKey.clear();
	    theKey = simpleHash.getKeys();
	    for (int j=0; j<theKey.size(); j++){
		String tag =(String) theKey.get(j);
		double train = simpleHash.getTrain(tag);
		double test = simpleHash.getTest(tag);
				
		summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	    }
			
	    distance[disIndex] = summ;
	    disIndex++;
			
	}
	
	double[] toReturn = new double[databound*3];
	System.arraycopy(distance, distance.length-(databound*3)
			 , toReturn, 0, toReturn.length);
	return toReturn;
    }
	
    /**
       compute the algorithms, catch virus
    */
    public void compute(double[] hellinger){


	ComputeAlert cp = new ComputeAlert();
	double[] sdThreshold_h = cp.calculateThreshold(hellinger, thrSize, alpha);
	//double[] sdThreshold_u = cp.calculateThreshold(userWin, thrSize, alpha);
	double[] sdThreshold_a = cp.calculateThreshold(attachWin, thrSize, alpha);

	//boolean[] hellingerResult = cp.makeAlarm(sdThreshold_h, sdThreshold_u, sdThreshold_a
	//		 , hellinger, userWin, attachWin);
	
	boolean[] hellingerResult = cp.makeAlarm(sdThreshold_h, sdThreshold_a, hellinger, attachWin);
	
	dailyResult = hellingerResult;
	testThreshold = sdThreshold_h;
    }
	
    /**
       get first training and testing data
	
    */
    public String[][] trainFirst(String[][] data, int length){
	try{
	    trainEnd = length-1;
	    if(data.length<=trainEnd+1)return null;
			
	    String[] endtime = data[trainEnd][2].split(" ");
	    int extra = 0;//more emails in the same day,last day
	    String[] testtime = data[trainEnd+extra][2].split(" ");
	    while(endtime[0].equals(testtime[0]) && extra<databound){
		if(data.length>trainEnd+extra+1){
		    extra++;
		    testtime = data[trainEnd+extra][2].split(" ");
		}
		else break;
	    }
	    extra--;
			
	    String[][] train = new String[length+extra][5];
	    System.arraycopy(data, 0, train, 0, length+extra);
			
	    dayCount = extra;//don't count before trainstart
	    trainEnd = length+extra-1;//remember the end of this training area
	    return train;
	}
	catch(Exception e){
	    System.out.println("data end, no more data.(first)");
	    return null;
	}
    }
	
    /**
       get next training and testing data
	
    */
    public String[][] trainData(String[][] trainning, String[][] alldata){
	try{
	    String[][] clean = cleanDummy(trainning);
			
	    trainEnd++;
	    String[] endtime = alldata[trainEnd][2].split(" ");
	    String[] testtime = alldata[trainEnd][2].split(" ");
	    int extra = 0;
	    Vector buffer = new Vector();

	    while(endtime[0].equals(testtime[0]) && extra<databound){
		if(alldata.length>trainEnd+extra+1){
		    int i = trainEnd+extra;
		    buffer.add(alldata[i][0]+","+alldata[i][1]+","+alldata[i][2]+","+alldata[i][3]+","+alldata[i][4]);
		    extra++;
		    testtime = alldata[trainEnd+extra][2].split(" ");
		}
		else if(alldata.length==trainEnd+extra+1){
		    int i = trainEnd+extra;
		    buffer.add(alldata[i][0]+","+alldata[i][1]+","+alldata[i][2]+","+alldata[i][3]+","+alldata[i][4]);
		    extra++;
		}
		else break;
	    }
		

	    //copy the old clean data first
	    String[][] tmpBuffer = new String[buffer.size()][5];
	    for(int i=0;i<buffer.size();i++){
		String str = (String)buffer.get(i);
		String[] seg = str.split(",");
		tmpBuffer[i][0] = seg[0];
		tmpBuffer[i][1] = seg[1];
		tmpBuffer[i][2] = seg[2];
		tmpBuffer[i][3] = seg[3];
		tmpBuffer[i][4] = seg[4];
	    }
	    String[][] toReturn = new String[clean.length+extra][5];
	    System.arraycopy(clean, 0, toReturn, 0, clean.length);
			
	    //add extra day
	    System.arraycopy(tmpBuffer, 0, toReturn, clean.length, extra);
			
	    dayCount = extra;
	    trainEnd += extra-1;//move forward

	    return toReturn;
	    }
	    catch(Exception e){
	    System.out.println("data end, no more data.(train)");
	    return null;
	    }
    }
	
    /**
       clean the dummy emails in the train data
    */
    public String[][] cleanDummy(String[][] train){		
	Vector buffer = new Vector();
		
	for(int i=0;i<train.length;i++){
	    if(train[i][4].equals("T")){
		buffer.add(train[i][0]+","+train[i][1]+","+train[i][2]+","+train[i][3]+","+train[i][4]);
	    }
	}
		
	String[][] clean = new String[buffer.size()][5];
	for(int i=0;i<buffer.size();i++){
	    String str = (String)buffer.get(i);
	    String[] seg = str.split(",");
	    clean[i][0] = seg[0];
	    clean[i][1] = seg[1];
	    clean[i][2] = seg[2];
	    clean[i][3] = seg[3];
	    clean[i][4] = seg[4];
	}
		
	return clean;
    }

    /**
       Compute the accumulative attachment counter, day by day
       for training data
    */
    public void computeAccuTrain(String[][] data){

	//get the first day
	String time = data[0][2];
	String[] seg = time.split(" ");
	String date = seg[0];
	String testDate = date;

	int numAtt = 0;
	for(int i=0;i<data.length;i++){
	 
	    //parse the date
	    time = data[i][2];
	    seg = time.split(" ");
	    date = seg[0];
	    if(!testDate.equals(date) || i==data.length-1){
		testDate=date;
		accuAttach.add(String.valueOf(numAtt));
	    }
	    
	    if(Integer.parseInt(data[i][3]) > 0){
		numAtt++;
	    }
	}
    }

    /**
       Compute the accumulative attachment counter, day by day
       for testing data
    */
    public void computeAccuTest(String[][] data){
	int start = data.length - dayCount;
	String lastnum = (String)accuAttach.get(accuAttach.size()-1);
	int numAtt = Integer.parseInt(lastnum);
	for(int i=start;i<data.length;i++){
	    if(Integer.parseInt(data[i][3]) > 0){
		numAtt++;
	    }
	}

	accuAttach.add(String.valueOf(numAtt));
    }

    /**
       test the accumulative stuff
       old_slope = (y1)/size-1
       new_slope = (y3-y2)/magic ,   the magic number
       y1 = value of yesterday
       y2 = value of one week ago
       y3 = value of today
    */
    public boolean testAccu(){
	int magic = 5;
	int size = accuAttach.size();

	String s1 = (String)accuAttach.get(size-2);
	String s2 = "";
	if(size<magic)s2="0";
	else s2 = (String)accuAttach.get(size-1-magic);
	String s3 = (String)accuAttach.get(size-1);

	int y1 = Integer.parseInt(s1);
	int y2 = Integer.parseInt(s2);
	int y3 = Integer.parseInt(s3);
	double oldSlope = (double)y1/(double)size;
	double newSlope =  ((double)(y3-y2)) / (double)magic;

	if(newSlope> oldSlope*accuThreshold) {return true;}//burst
	return false;
    }


    /**************************************************/
    /*
      This part is for testing the real one day virus
    */
    public boolean[] filter(String[][] trainData, String[][] testData){

	flags = new boolean[testData.length];

	int log = 0;
	while(log<testData.length){
	    String[][] total = appendData(trainData, testData, log);
	    HDistance = total.length-trainData.length;
	    if(HDistance>databound)HDistance=databound;
	    else if(HDistance<20)HDistance=20;

	    double[] attachment = blockAttachment(total);
	    double[] singlehellinger = singleHellinger(total);//single day hellinger

	    boolean[] tmpFlag = compute(singlehellinger, attachment);
	    singleTestResult(tmpFlag, trainData.length);

	    //accuAttach = new Vector();
	    //computeAccuTrain(traindata);
	    //hasbadday = accuTest(testData);
	    
	    
	    log += databound;
	}

	/*	
	//combine the accumulative stuff
	computeAccuTrain(traindata);
	boolean accu = testAccu();
	if(accu)hasbadday=true;

	countResult2(dailyResult,accu);
	flagLog = traindata.length;
	*/		
	return flags;
    }

    public void singleTestResult(boolean[] tmpFlag, int start){
	
	for(int i=start;i<tmpFlag.length;i++){
	    flags[flagLog] = tmpFlag[i];
	    flagLog++;
	}
    }

    public String[][] appendData(String[][] train, String[][] test, int log){
	String[][] total;

	if(test.length-log>databound)total = new String[train.length+databound][4];
	else total = new String[train.length+test.length-log][4];
	
	int i=0;
	for(;i<train.length;i++)
	    for(int j=0;j<train[i].length;j++)
		total[i][j] = train[i][j];

	for(int k=log;k<test.length && k-log<databound;k++){
	    for(int j=0;j<test[k].length;j++){
		total[i][j] = test[k][j];
	    }
	    i++;
	}

	return total;
    }

    public double[] blockAttachment(String[][] data){ 		
	//distinct rcpt
	Vector attlist = new Vector();

	int attcount = 0;//attachment counter
	int totallength = data.length;//total datalength
	
	//number of emails with attachment until now, in the window
	double[] attach = new double[totallength];
	HashMap table = new HashMap();//use hash table to speed up

	for(int i=0; i<totallength; i++){ 
	    //count the number of distinct rcpt
	    //if(!table.containsKey(data[i][0])){ 
	    table.put(data[i][0],"1");  
	    //}
			
	    //numUserWin is the window size
	    if (i<numUserWin){//before the window is full
		//if has attachment
		if(Integer.parseInt(data[i][3])>0){ 
		    attcount++;
		}
	    }
	    else{  
		//for attachment
		if(Integer.parseInt(data[i][3])>0){  
		    attcount++;
		}
				
		if(Integer.parseInt(data[i-numUserWin][3])>0){
		    attcount--;
		}
	    }
	    attach[i] = attcount;
	}

	return attach;
    }

    public double[] singleHellinger(String[][] data){
		
	SimpleHashtable simpleHash = new SimpleHashtable();
		
	double[] distance = new double[data.length];
	int disIndex = 0;

	double trainFreq = 1.0/((double)HDistance*4);
	double testFreq = 1.0/(double)HDistance;
		
	if (HDistance*5 > data.length)
	    return null;
		
	int count = 0;
	//build hash table for data 0 to HDistance*5
	for(int i=0; count<HDistance*5; i++){
	    String name = data[i][0];
	    if( count < HDistance*4 ){
		simpleHash.insert(name,"train",trainFreq,0);
	    }else{
		simpleHash.insert(name,"test",testFreq, 0);
	    }
			
	    if( count < HDistance*5-1 ){
		//distance.addElement(new Double(0.0));
		distance[disIndex] = 0.0;
		disIndex++;
	    }

	    count++;
	}
		
	//compute distance in the first term
	Vector theKey = simpleHash.getKeys();
	double sum = 0;
	for(int i=0; i<theKey.size(); i++){
	    String name = (String)theKey.get(i);
	    double train = simpleHash.getTrain(name);
	    double test = simpleHash.getTest(name);
	    sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	}
		
	//distance.addElement(new Double(sum));// first term's distance
	distance[disIndex] = sum;
	disIndex++;

	//build hash table for all of terms and compute distances
	for (int i=0; i< data.length-(HDistance*5); i++){//total runing times

	    double summ =0;
	    //cut first for training
	    String name = data[i][0];
	    simpleHash.remove(name, "train", trainFreq);
			
	    //add last for training
	    name = data[i+HDistance*4][0];
	    simpleHash.insert(name, "train", trainFreq,0);
			
	    //cut first for test
	    name = data[i+HDistance*4][0];
	    simpleHash.remove(name, "test", testFreq);
	    //add last for test
	    name = data[i+HDistance*5][0];
	    simpleHash.insert(name, "test", testFreq,0 );
			
	    //compute distance
	    theKey.clear();
	    theKey = simpleHash.getKeys();
	    for (int j=0; j<theKey.size(); j++){
		String tag =(String) theKey.get(j);
		double train = simpleHash.getTrain(tag);
		double test = simpleHash.getTest(tag);
				
		summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	    }
			
	    distance[disIndex] = summ;
	    disIndex++;
			
	}
	return distance;
    }

    public boolean[] compute(double[] hel, double[] att){

	ComputeAlert cp = new ComputeAlert();
	double[] sdThreshold_h = cp.calculateThreshold(hel, thrSize, alpha);
	double[] sdThreshold_a = cp.calculateThreshold(att, thrSize, alpha);

	return cp.makeAlarm(sdThreshold_h, sdThreshold_a, hel, att);
    }

    public boolean accuTest(String[][] testData){
	
	int dailyAtt = 0;
	for(int i=0;i<testData.length;i++){
	    if(Integer.parseInt(testData[i][3])>0)
	       dailyAtt++;
	}

	int magic = 5;
	int size = accuAttach.size();

	String s1 = (String)accuAttach.get(size-1);
	String s2 = "";
	if(size<magic)s2="0";
	else s2 = (String)accuAttach.get(size-1-magic);

	int y1 = Integer.parseInt(s1);
	int y2 = Integer.parseInt(s2);
	double oldSlope = (double)y1/(double)size;
	double newSlope =  ((double)(dailyAtt-y2)) / (double)magic;

	if(newSlope> oldSlope*accuThreshold) {return true;}//burst
	 return false;
    }
}

/**
	This class is for Hellinger to compute alert.
*/
class ComputeAlert{


    /**
       Calculate the Threshold for the dummy test.
       Because of statistics, we don't compute the first N-1 data.
	
       Algorithm:
       Use the last N data, compute their Standard Deviation(SD).
       The Threshold is (old_value + SD)*alpha.
	
       @param input the list of data(curve) to be used
       @param N the size of a block to compute SD.
       @param alpha A canstant.
       @return threshold the data(curve) of threshold
    */
    public double[] calculateThreshold(double[] input, int N, double alpha){
    	try{
	double[] threshold = new double[input.length];
	double[] block = new double[N];
	int oldestIndex = N-1;//the oldest value of this block
//	int shift = 20;

	//read the first N data
	for(int i=0; i<N-1; i++){
	    threshold[i] = input[i];
	    block[i] = input[i];
	}

	//calculate the threshold from N-1th data
	for(int i=N-1; i<input.length; i++){
	    block[oldestIndex] = input[i];
	    oldestIndex++;
	    if(oldestIndex >= N) oldestIndex = 0;
			
	    threshold[i] = (input[i])
		+ (alpha * getSD(block, getAverage(block)) );
	}

	//shift the value to last 20
	double[] t2 = new double[input.length];
	for(int i=0; i<input.length; i++){
	    if( i < (N-1 + 20) ){
		t2[i] = input[i];
	    }
	    else{
		t2[i] = threshold[i-20];
	    }
	}

	//return threshold;
	return t2;
	}
	catch(Exception e){
		System.out.println("message:"+e);
		System.out.println(input);
		//System.out.println(input.length);
		double[] d = new double[0];
		return d;
	}
    }

     /**
       Get the average of a set of numbers.
       @param input an array with numbers.
       @return the average.
    */
    public double getAverage(double[] input){
	double length = (double)input.length;
	double total = 0.0;
	for(int i=0; i< input.length; i++){
	    total += input[i];
	}
		
	double avg = total/length;
	return avg;
    }
	
    /**
       Compute the Standard Deviation.
       @param input The numbers.
       @param avg The average of these numbers
       @return The Standard Deviation.
    */
    public double getSD(double[] input, double avg){
	double tmpAdder = 0.0;
	for(int i=0; i<input.length; i++){
	    tmpAdder += Math.pow(avg-input[i], 2);
	}
		
	double tmp = tmpAdder/input.length;
	double standardDev = Math.sqrt(tmp);
	return standardDev;
    }

    /**
       the length of these arrays must be the same
       when data > threshold, it's an alarm
       take "ALL" of the three curves
    */
    public boolean[] makeAlarm(double[] thr1, double[] thr2, double[] thr3
			       , double[] data1, double[] data2, double[] data3){
	int length = thr1.length;
		
	boolean[] alert = new boolean[length];
	for(int i=0; i<length; i++){

	    //the value over threshold
	    if(data1[i]>thr1[i] 
	       && data2[i]>thr2[i] 
	       && data3[i]>thr3[i]){
		
		alert[i]=true;
		
		//check more missed alarm backward
		int j=i;
		while(j>0 && alert[j]==false
		      //this is an increasing slope...
		      && data1[j]<data1[j+1]
		      && data2[j]<data2[j+1]
		      && data3[j]<data3[j+1]){
		    alert[j]=true;
		    j--;
		}
	    }
	    else{
		alert[i]=false;
	    }
	}
	return alert;
    }
    
    
    /**
       the length of these arrays must be the same
       when data > threshold, it's an alarm
       take "ALL" of the three curves
       two models only
    */
    public boolean[] makeAlarm(double[] thr1, double[] thr2
			       , double[] data1, double[] data2){
	int length = thr1.length;
		
	boolean[] alert = new boolean[length];
	for(int i=0; i<length; i++){

	    //the value over threshold
	    if(data1[i]>thr1[i]  
	       && data2[i]>thr2[i]){
		
		alert[i]=true;
		
		//check more missed alarm backward
		int j=i;
		while(j>0 && alert[j]==false
		      //this is an increasing slope...
		      && data1[j]<data1[j+1]
		      && data2[j]<data2[j+1]){
		    alert[j]=true;
		    j--;
		}
	    }
	    else{
		alert[i]=false;
	    }
	}
	return alert;
    }
    
}
