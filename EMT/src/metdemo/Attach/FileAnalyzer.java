/*
 *
 */
package metdemo.Attach;


import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;

import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import javax.swing.JPanel;

import java.sql.SQLException;
import java.util.*;
import java.util.Date;

import java.text.SimpleDateFormat;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;




public class FileAnalyzer extends JScrollPane implements ActionListener, MouseListener {
    winGui parent;
	
    //DefaultTableCellRenderer alarmRenderer; 
    JPanel panel, inPanel, outPanel;
    JComboBox bound, gram;
    JButton refresh, testunknown, loadmodel, savemodel, displaymodel, browse, help, merge;
    JTextField trunc;
    JLabel modelname;
    //JCheckBox detail;

    JPopupMenu classMenu = null;
    JFileChooser filechooser;
    File currentDir = null;
    String currentFName = null;
    FAModel currentModel = null;

    JScrollPane tableScrollPane;
    DefaultTableModel tablemodel;
   // HCSortFilterModelVsimulate sorter;
    String[] columnNames = {"Sender","Recipient","Subject","Time","Attachment",
			    "Folder","Label","ref"};
    GridBagConstraints gbc;
    
    JTable table = null;
    String[] mailref = null;


    private EMTDatabaseConnection jdbc = null;
    private EMTDatabaseConnection jdbcUpdate=null;
    md5CellRenderer m_md5Renderer;
    //BusyWindow bw = new BusyWindow("Progress","Running...");

    
    //constructor
    public FileAnalyzer(EMTDatabaseConnection jdbcU, EMTDatabaseConnection jdbcV, md5CellRenderer m, winGui w){
        parent = w;
        m_md5Renderer = m;
        jdbc = jdbcV;
        jdbcUpdate = jdbcU;
	//connect to the database	
	/*	
	  try{
	      jdbc = new JDBCConnect("jdbc:mysql://blackberry/sal03","org.gjt.mm.mysql.Driver","outsider", "fizzy99");
	      //jdbc = new JDBCConnect("jdbc:mysql://blackberry/sal03","com.mysql.jdbc.Driver","outsider", "fizzy99");
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

	//the upper panel, with buttons
	inPanel = new JPanel();
	inPanel.setLayout(gridbag);
	inPanel.setPreferredSize(new Dimension(800, 150));
	inPanel.setBorder(BorderFactory.createTitledBorder(""));

	//the table panel
	outPanel = new JPanel();
	outPanel.setLayout(gridbag);
	outPanel.setBorder(BorderFactory.createTitledBorder("Email Table"));
	
	String[] tmp1 = {"Inbound", "Outbound", "Both"};
	String[] tmp2 = {"1-Gram", "2-Gram"};
	bound = new JComboBox(tmp1);
	gram = new JComboBox(tmp2);
	gram.setToolTipText("Read data in 1-byte or 2-bytes form");

    	refresh = new JButton("Refresh Table");
        refresh.setActionCommand("Refresh");
    	refresh.addActionListener(this);
	refresh.setBackground(Color.yellow);
	refresh.setToolTipText("Refresh the email table");

	testunknown = new JButton("Test Unknown");
        testunknown.setActionCommand("Test Unknown");
    	testunknown.addActionListener(this);
	testunknown.setBackground(Color.magenta);
	testunknown.setToolTipText("Test the marked emails against current model");

	loadmodel = new JButton("Load Model");
        loadmodel.setActionCommand("Load Model");
    	loadmodel.addActionListener(this);
	//loadmodel.setBackground(Color.green);
	loadmodel.setToolTipText("Load a saved model");

	savemodel = new JButton("Save Model");
        savemodel.setActionCommand("Save Model");
    	savemodel.addActionListener(this);
	//savemodel.setBackground(Color.green);
	savemodel.setToolTipText("Save the marked emails to a model");

	merge = new JButton("Merge Models");
        merge.setActionCommand("Merge Model");
        merge.addActionListener(this);
        //merge.setBackground(Color.green);
        merge.setToolTipText("Merge some saved models");

	displaymodel = new JButton("Display Model");
        displaymodel.setActionCommand("Display Model");
    	displaymodel.addActionListener(this);
	displaymodel.setBackground(Color.cyan);
	displaymodel.setToolTipText("Show the byte distribution of current model");

	help = new JButton("Help");
        help.setActionCommand("Help");
    	help.addActionListener(this);
	help.setBackground(Color.orange);

	trunc = new JTextField("20",8);
	trunc.setToolTipText("The ratio to truncate, from 1 to 100");

	modelname = new JLabel("Null                               ");
	modelname.setForeground(Color.blue);

	//detail = new JCheckBox("Detailed Info.");
	//detail.setSelected(true);

    	tablemodel = new DefaultTableModel(null, columnNames){
    		public boolean isCellEditable(int row, int col) {return false;}
	    };
    
    	//sorter = new HCSortFilterModelVsimulate(tablemodel);
	
    	table = new JTable(tablemodel);
    	//sorter.addMouseListener(table);
    	//table.addMouseListener(this);
	setTableRender();
    	tableScrollPane = new JScrollPane(table);
    	table.setPreferredScrollableViewportSize(new Dimension(790, 500));

	filechooser = new JFileChooser();
	filechooser.addChoosableFileFilter(new FAFilter());
	filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	filechooser.setFileHidingEnabled(false);
	filechooser.setMultiSelectionEnabled(false);

	/*
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
	*/
      
	EMTHelp emthelp = new EMTHelp(EMTHelp.FANALYZER);
	/*
	JButton reporter = new JButton("Generate Reports");
	reporter.setToolTipText("Generate reports for each of the users using the range specified.");
	reporter.addActionListener(this);
	reporter.setBackground(Color.pink);
	*/
    	gbc.insets = new Insets(3,3,3,3);

	gbc.gridwidth = 1;
	gbc.gridx = 0;
    	gbc.gridy = 0;
    	inPanel.add(new JLabel("Email Type:"), gbc);

	gbc.gridx = 1;
    	gbc.gridy = 0;
    	inPanel.add(bound, gbc);

	gbc.gridx = 2;
    	gbc.gridy = 0;
    	inPanel.add(new JLabel("Truncate Ratio:"), gbc);

	gbc.gridx = 3;
    	gbc.gridy = 0;
    	inPanel.add(trunc, gbc);
	
	gbc.gridx = 4;
    	gbc.gridy = 0;
    	inPanel.add(new JLabel("Analyze:"), gbc);

	gbc.gridx = 5;
    	gbc.gridy = 0;
    	inPanel.add(gram, gbc);

	gbc.gridx = 0;
    	gbc.gridy = 2;
    	inPanel.add(refresh, gbc);

	gbc.gridx = 1;
    	gbc.gridy = 2;
    	inPanel.add(displaymodel, gbc);

	gbc.gridx = 2;
    	gbc.gridy = 2;
    	inPanel.add(testunknown, gbc);
	
	
	gbc.gridx = 3;
    	gbc.gridy = 2;
    	inPanel.add(emthelp, gbc);
	
	gbc.gridx = 0;
    	gbc.gridy = 1;
    	inPanel.add(new JLabel("Current Model:"), gbc);

	gbc.gridwidth = 2;
	gbc.gridx = 1;
    	gbc.gridy = 1;
    	inPanel.add(modelname, gbc);
	
	gbc.gridwidth = 1;
	gbc.gridx = 3;
    	gbc.gridy = 1;
    	inPanel.add(loadmodel, gbc);

	gbc.gridx = 4;
    	gbc.gridy = 1;
    	inPanel.add(savemodel, gbc);

	gbc.gridx = 5;
        gbc.gridy = 1;
        inPanel.add(merge, gbc);

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
	    SortMsgObj[] obj = currentModel.computeInfo();
	    if(obj != null && obj.length>0){
		String[] msg = new String[2];
		msg[0] = "current model is: "+currentFName;
		msg[1] = "Type of analysis: "+obj[0].gram+"-Gram";

		int length = obj.length;
		int binaryLength = 0;
		if(obj[0].gram == 1)binaryLength = 256;
		else if(obj[0].gram == 2)binaryLength = 256*256;
		
		//Arrays.sort(obj);
		double[][] array = new double[length][binaryLength];
		String[] names = new String[length];
		int[] sizes = new int[length];
		for(int i=0;i<length;i++){
		    names[i] = obj[i].name;
		    sizes[i] = obj[i].size;
		    for(int j=0;j<binaryLength;j++){
			array[i][j] = obj[i].value[j];
		    }
		}
		
		FAInfo info = new FAInfo(msg, this, array
					 , names, sizes, length);
		info.setVisible(true);
	    }
	}
	catch(Exception e){
	    System.out.println("Report error. "+ e);	
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
		    int index = bound.getSelectedIndex();

		    if(index == 0){
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e,message"
			    +" m where (e.mailref=m.mailref) and (e.rcpt='"
			    +user+"') and (e.DATEs>'"+start+"' and "
			    +" e.DATEs<'"+end+"')";
		    }
		    else if(index == 1){
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e,message"
			    +" m where (e.mailref=m.mailref) and (e.sender='"
			    +user+"') and (e.DATEs>'"+start+"' and "
			    +" e.DATEs<'"+end+"')";
		    }
		    else{
			query = "select e.sender,e.rcpt,e.subject,"
			    +"e.insertTime,m.filename,e.folder,e.mailref from email e,message"
			    +" m where (e.mailref=m.mailref) and (e.rcpt='"
			    +user+"' or e.sender='"+user+"') and "
			    +"(e.DATEs>'"+start+"' and "
			    +"e.DATEs<'"+end+"')";
		    }

		    query += " and e.numattach>0 and m.filename!=''";
		    query += " order by e.utime";

		    //stem.out.println(query);
		    bw.progress(1,4);
		    String[][] data = runSql(query);
		    //System.out.println("data:"+data+"\ndata size:"+data.length);
		    bw.setMSG("Updating Table...");
		    bw.progress(2,4);
		    String[][] tabledata= null;

		    if(data.length>0){
		    	tabledata = new String[data.length][data[0].length+4];
			for(int i=0;i<data.length;i++){
			    for(int j=0;j<data[i].length-1;j++){
				tabledata[i][j] = data[i][j];
			    }
			    
			    tabledata[i][6] = "NA";
			    tabledata[i][7] = data[i][data[i].length-1];
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
		    //if(col==5)
		    //return true;
		    //else return false;
		    return false;
		}
	    };
	    
	//sorter = new HCSortFilterModelVsimulate(tablemodel);
	//table = new JTable(sorter);
	    table = new JTable(tablemodel);
	//}
	//else{
	//table = new JTable();
	//}

	table.addMouseListener(this);
	//sorter.addMouseListener(table);
	
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

	tmpColumn = table.getColumn("ref");
	tmpColumn.setCellRenderer(m_md5Renderer);
	tmpColumn.setPreferredWidth(10);
	
	//setup the label
	JComboBox tableCombo = new JComboBox();
	tableCombo.addItem("NA");
	tableCombo.addItem("Mark");
	
	tableCombo.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt){
		    JComboBox source = (JComboBox)evt.getSource();

		    int c = ((JComboBox)source).getSelectedIndex();

		    if(c==0){
			((JComboBox)source).setBackground(Color.green);
		    }
		    else if(c==1){   
			((JComboBox)source).setBackground(Color.red); 
		    }
		    else{
			((JComboBox)source).setBackground(Color.yellow);
		    }
		}
	    });
	
	TableColumn interestColumn = table.getColumn("Label");
	interestColumn.setCellEditor(new DefaultCellEditor(tableCombo));
	interestColumn.setMaxWidth(65);
	
	
	DefaultTableCellRenderer interestColumnRenderer = new DefaultTableCellRenderer(){
		public void setValue(Object value){
		    if(value instanceof String){  
			if(((String)value).equals("NA")){
			    setBackground(Color.green);
			    setText("NA");
			}
			else if(((String)value).equals("Mark")){
			    setBackground(Color.red);
			    setText("Mark");
			}
		    }
		    else
			setText((value == null) ? "" : value.toString());
		}
	    };
	interestColumnRenderer.setToolTipText("Click for Label box");
	interestColumn.setCellRenderer(interestColumnRenderer);
	
	classMenu = new JPopupMenu("Set Label");
        classMenu.add("NA").addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    setTableValue("NA");
		    panel.repaint();
		}
	    });
	classMenu.add("Mark").addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    setTableValue("Mark");
		    panel.repaint();
		}
	    });
    }

    public void setTableValue(String value){
	int[] rows = table.getSelectedRows();

	//have to change related same mail.... the join table bug
	//don't need this function here
	//Vector<String> ref = new Vector<String>();

	for(int i=0; i<rows.length;i++){
	    table.setValueAt(value, rows[i], 6);
	    //String tmpref = (String)table.getValueAt(rows[i], 7);
	    //if(!ref.contains(tmpref))ref.add(tmpref);
	    //System.out.println(tmpref+" "+rows[i]);
	}

	/*
	for(int i=0;i<table.getRowCount();i++){
	    String tmpref = (String)table.getValueAt(i, 7);
	    if(ref.contains(tmpref)){
		table.setValueAt(value, i, 6);
	    }
	}
	*/
    }

    public final byte[] getAttachment(String filesql, String bodysql){
	byte[] att = null;
	//String[][] data;
	try{
	    att = (byte[])jdbc.getBinarySQLData(bodysql)[0];
	}
	catch(Exception e){
	    System.out.println("Attachment error:"+e);
	    return null;
	}

	return att;
    }

    public FAModel getMarkedData(){
	int rows = table.getRowCount();
	if(rows<1)return null;
	//Vector models = new Vector();
	FAModel model;

	String tmpg = (String)gram.getSelectedItem();
	if(tmpg.equals("1-Gram"))
	    model = new FAModel(1);
	else
	    model = new FAModel(2);

	for(int i=0;i<rows;i++){
	    String label = table.getValueAt(i,6).toString();
	    if(label.equals("Mark")){
		String tmpfname = table.getValueAt(i,4).toString();
		tmpfname = tmpfname.replaceAll("'", "\\\\'");
		String sql = " from message where filename='"+tmpfname
		    +"' and mailref='"+table.getValueAt(i,7).toString()+"'";
		String filesql = "select filename,size"+sql;
		String bodysql = "select body"+sql;
		byte[] data = getAttachment(filesql, bodysql);
		FAObj tmpobj = getFeature(data, tmpfname);
		model.addobj(tmpobj);
	    }
	}
	return model;
    }

    public FAObj getFeature(byte[] data, String filename){
	FAObj obj = null;
	String tmpt = trunc.getText();
	int truncate = 0;
	try{
	    //get the truncated ratio
	    int tr = Integer.parseInt(tmpt);
	    if(tr>=1 || tr<=100){
		truncate = tr;
	    }
	    else{
	    	showMsg("Truncate Ratio must be greater than 0 and less than 100");
		return null;
	    }
	    int totalFeature = data.length*truncate/100;

	    //1-gram or 2-gram
	    String tmpg = (String)gram.getSelectedItem();
	    int gramflag = 0;

	    //get the features
	    double[] array = null;
	    if(tmpg.equals("1-Gram")){//1-gram
		gramflag = 1;
		array = new double[256];
		for(int i=0;i<totalFeature;i++){
		    int index = (int)data[i];
		    if(index<0)index+=256;//avoid negitive value
		    array[index]++;
		}
	    }
	    else{//2-gram
		gramflag = 2;
		array = new double[256*256];
		for(int i=0;i<totalFeature-1;i++){
		    int index1 = (int)data[i];
		    int index2 = (int)data[i+1];
		    if(index1<=0)index1+=256;//avoid negitive value
		    if(index2<=0)index2+=256;//avoid negitive value
		    array[index1*index2-1]++;
		}
	    }

	    //normalize
	    for(int i=0;i<array.length;i++){
		array[i] /= totalFeature;
	    }

	    if(filename.indexOf(".")==-1){//no extension
		obj = new FAObj(gramflag, array, filename, " no-extension");
	    }
	    else{
		String tmpf = filename.substring(filename.lastIndexOf(".")+1
						 ,filename.length());
		obj = new FAObj(gramflag, array, filename, tmpf);
	    }
	}
	catch(Exception e){
	    return null;
	}

	return obj;
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
    public FAModel appendModel(FAModel oldmodel, FAModel newmodel){
	FAModel appended = oldmodel;
	try{
	    if(oldmodel.gram != newmodel.gram){
		showMsg("Append model error.\n"
			+"These models use different"
			+" size of gram");
		return null;
	    }
	    for(int i=0;i<newmodel.files.size();i++){		
		appended.addobj((FAObj)newmodel.files.get(i));
	    }
	}
	catch(Exception e){
	    return null;
	}
	return appended;
    }

    public void showMsg(String msg){
	JOptionPane.showMessageDialog(this,msg);
    }

    //get the 10% highest peaks
    public String peakTest(FAModel model, FAModel test){
	String result = "Peak Signature Match\n\n";
	
	for(int i=0;i<test.files.size();i++){
	    //get the peaks of testing data
	    FAObj tmpo = (FAObj)test.files.get(i);
	    result = result.concat((i+1)+". Testing file name: "+tmpo.filename+"\n");

	    int length = tmpo.feature.length;
	    SimpleSortObj[] sortobj = new SimpleSortObj[length];
	    for(int j=0;j<length;j++){
		sortobj[j] = new SimpleSortObj(tmpo.feature[j], String.valueOf(j));
	    }
	    Arrays.sort(sortobj);
	    //for(int x=0;x<sortobj.length;x++)
	    //System.out.println(sortobj[x].var_a+"   "+sortobj[x].var_b);

	    int peaksize = (int)((float)(length)*0.1);
	    Vector<String> testpeaks = new Vector<String>();
	    for(int j=0;j<peaksize;j++){
		testpeaks.add(sortobj[j].var_b);
		//System.out.println("test peak:"+sortobj[j].var_a+" "+sortobj[j].var_b);
	    }

	    int matchcount = 0;
	    int matchindex = 0;
	    //test the peaks, test against each model
	    SortMsgObj[] classified = model.computeInfo();
	    for(int j=0;j<classified.length;j++){
		SimpleSortObj[] sortobj2 = new SimpleSortObj[length];
		for(int k=0;k<length;k++){
		    sortobj2[k] = new SimpleSortObj(classified[j].value[k]
						    , String.valueOf(k));
		}
		Arrays.sort(sortobj2);
		int tmpmatch = 0;
		for(int k=0;k<peaksize;k++){
		    if(testpeaks.contains(sortobj2[k].var_b))tmpmatch++;
		    //System.out.println("model peak:"+sortobj2[k].var_a+" "+sortobj2[k].var_b);
		}

		if(tmpmatch>matchcount){
		    matchcount = tmpmatch;
		    matchindex = j;
		}
	    }

	    result = result.concat("Closest file type: "
				   +classified[matchindex].name+"\n"
				   +"The ratio of matching peak: "
				   +matchcount+"/"+peaksize
				   +" = "+(float)matchcount/(float)peaksize
				   +"\n\n");
	}

	return result;
    }

    private class SimpleSortObj implements Comparable{
	double var_a;
	String var_b;

	public SimpleSortObj(double a, String b){
	    var_a = a;
	    var_b = b;
	}

	public int compareTo(Object other) throws ClassCastException {
	    SimpleSortObj s = (SimpleSortObj)other;
	    //return (new Double(this.var_a)).compareTo(new Double(s.var_a));
	    //from the biggest
	    return (new Double(s.var_a)).compareTo(new Double(this.var_a));
	}

    }

    public String mahalanobisTest(FAModel model, FAModel test){
	String result = "Mahalanobis Distance Test\n\n";
	
	for(int i=0;i<test.files.size();i++){
	    //get the peaks of testing data
	    FAObj tmpo = (FAObj)test.files.get(i);
	    result = result.concat((i+1)+". Testing file name: "+tmpo.filename+"\n");

	    SortMsgObj[] classified = model.computeInfo();
	    double lowestdis = Double.MAX_VALUE;
	    int lowestindex = 0;
	    for(int j=0;j<classified.length;j++){
		double maha = computeMaha(classified[j].value, tmpo.feature
					  ,classified[j].std);
		if(lowestdis>maha){
		    lowestdis = maha;
		    lowestindex = j;
		}
	    }

	    result = result.concat("Closest model: "
				   +classified[lowestindex].name+"\n"
				   +"The Mahalanobis Distance: "
				   +lowestdis
				   +"\n\n");
	}

	return result;
    }

    /**
       Compute Mahalanobis Distance
    */
    public double computeMaha(final double[] train, final double[] test
			      , final double[] std){
	double alpha = 0.001;
	double sum = 0;
	for(int i=0;i<train.length;i++){
	    sum += Math.abs(train[i]-test[i])/(std[i]+alpha);
	}
	
	return sum;
    }
    
    //compute the standard deviation of all bytes of a single file
    public String stdTest(FAModel model, FAModel test){
	String result = "Standard Deviation Test\n\n";
	
	for(int i=0;i<test.files.size();i++){
	    //get the peaks of testing data
	    FAObj tmpo = (FAObj)test.files.get(i);
	    result = result.concat((i+1)+". Testing file name: "+tmpo.filename+"\n");

	    SortMsgObj[] classified = model.computeInfo();
	    double closeststd = Double.MAX_VALUE;
	    int closestindex = 0;
	    for(int j=0;j<classified.length;j++){
		double std = Math.abs(computeStd(classified[j].value) - computeStd(tmpo.feature));

		if(closeststd>std){
		    closeststd = std;
		    closestindex = j;
		}
	    }

	    result = result.concat("Closest model: "
				   +classified[closestindex].name+"\n"
				   +"The Standard Deviation Difference: "
				   +closeststd
				   +"\n\n");
	}

	return result;
    }

    public double computeStd(final double[] array){

	double sum = 0;
	int length = array.length;
	for(int i=0;i<length;i++)
	    sum += array[i];
	
	double mean = sum/(double)length;

	double var = 0;
	for(int i=0;i<length;i++){
	    double v = array[i] - mean;
	    var += v*v;
	}

	return Math.sqrt(var/(double)length);
    }
    

    public void actionPerformed(ActionEvent evt) {
        String arg = evt.getActionCommand();	
		
    	if(arg.equals("Refresh")) {
	    getmail();
        }
	else if(arg.equals("Test Unknown")){
	    FAModel model = getMarkedData();
	    if(currentModel == null)showMsg("Please load a model first.");
	    else if(model == null)showMsg("Please mark some emails first.");
	    else if(currentModel.gram != model.gram)showMsg("Gram size don't match.");
	    else{
		String result1 = peakTest(currentModel, model);
		String line = "\n\n-----------------------------------------------------\n\n";
		String result2 = mahalanobisTest(currentModel, model);
		String result3 = stdTest(currentModel, model);
		
		FAInfo info = new FAInfo(result1+line+result2+line+result3, this);
		info.setVisible(true);
		
	    }
	}
	else if(arg.equals("Load Model")){
	    FAModel prem = new FAModel(currentModel);
	    File pred = currentDir;
	    String pren = currentFName;
	    if(useFileChooser("Load", "Open a file") == true){
		currentModel = (FAModel)loadModel(currentFName);
		if(currentModel == null){
		    showMsg("Load model error.");
		    resetModel(prem, pren, pred);
		}
		else{
		    //also update the combobox
		    if(currentModel.gram == 1)
			gram.setSelectedIndex(0);
		    else if(currentModel.gram == 2)
			gram.setSelectedIndex(1);
		}
	    }

	}
	else if(arg.equals("Save Model")){
	    FAModel prem = new FAModel(currentModel);
	    File pred = currentDir;
	    String pren = currentFName;
	    FAModel model = getMarkedData();
	    if(model == null)showMsg("Please mark some emails first.");
	    else{
		String[] opt = {"New Model", "Append an Old Model"};
		int n = 
		    JOptionPane.showOptionDialog(this,
						 "New model or append a model?\n",
						 "Confirmation",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.QUESTION_MESSAGE,
						 null,
						 opt,
						 opt[0]);
		if(n == 0){
		    if(useFileChooser("Save", "Save a file") == true){
			if(saveModel(model, currentFName)!=true){
			    showMsg("Save model error.");
			    resetModel(prem, pren, pred);
			}
			currentModel = model;
		    }
		}
		else if(n == 1){
		    try{
			if(useFileChooser("Append", "Select a file to be appended") == true){
			    FAModel tmpm1 = (FAModel)loadModel(currentFName);
			    //append a model
			    FAModel tmpm = appendModel(tmpm1, model);
			    if(tmpm != null){
				if(saveModel(tmpm, currentFName)!=true){
				    showMsg("Save model error.");
				    resetModel(prem, pren, pred);
				}
				currentModel = tmpm;
			    }
			    else resetModel(prem, pren, pred);
			}
			else resetModel(prem, pren, pred);
		    }
		    catch(Exception e){
			showMsg("Append model error.");
		    }
		}
	    }
	}
	else if(arg.equals("Merge Model")){
	    FAModel prem = new FAModel(currentModel);
	    File pred = currentDir;
	    String pren = currentFName;
	    String[] filenames = useFileChooser2("Merge", "Select some files and merge them");
	    if(filenames != null){
		FAModel[] models = new FAModel[filenames.length];
		for(int i=0;i<models.length;i++)
		    models[i] = (FAModel)loadModel(filenames[i]);

		FAModel all = mergeModels(models);
		if(all != null){
		    showMsg("Merge done!\nNow save it.");
		    System.out.println(currentDir+"  "+currentFName);
		    if(useFileChooser("Save", "Save a file") == true){
			if(saveModel(all, currentFName)!=true){
			    showMsg("Save model error.");
			    resetModel(prem, pren, pred);
			}
			currentModel = all;
		    }
		}
		else resetModel(prem, pren, pred);
	    }
	    else resetModel(prem, pren, pred);
	}
	else if(arg.equals("Display Model")){
	    if(currentModel == null){
		showMsg("Please load a model first.");
	    }
	    else{
		makeReport();
	    }
	}
	else if(arg.equals("Browse")){

	}
    }

    public FAModel mergeModels(FAModel[] models){
	if(models.length == 0)return null;
	if(models.length == 1)return models[0];

	FAModel all = models[0];
	for(int i=1;i<models.length;i++){
	    if(all.gram != models[i].gram){
		showMsg("Append model error.\n"
                        +"These models use different"
			+" size of gram");
		return null;
	    }
	    for(int j=0;j<models[i].files.size();j++){
		all.addobj((FAObj)models[i].files.get(j));
	    }
	}
	return all;
    }

    public void testmodel(FAModel model){
	System.out.println(model.files.size());
	System.out.println(model.exts.size());
	for(int i=0;i<model.files.size();i++){
	    
	}
    }

    //this is for....when used filechooser, but get exceptions...
    public void resetModel(FAModel m, String name, File dir){
	currentModel = m;
	currentFName = name;
	currentDir = dir;
	modelname.setText(currentFName);
    }

    public boolean useFileChooser(String approve, String title){
	filechooser.setApproveButtonText(approve);
	filechooser.setDialogTitle(title);

	if(currentDir!=null)
	    filechooser.setCurrentDirectory(currentDir);
	
	int returnVal = filechooser.showOpenDialog(this);
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    currentFName = filechooser.getSelectedFile().getPath();
	    if(!currentFName.endsWith(".fa"))
		currentFName = currentFName.concat(".fa");
	    modelname.setText(currentFName);
	    currentDir = filechooser.getCurrentDirectory();
	    return true;
	}
	return false;
    }

    //for multiple files
    public String[] useFileChooser2(String approve, String title){
	filechooser.setMultiSelectionEnabled(true);
        filechooser.setApproveButtonText(approve);
	filechooser.setDialogTitle(title);
	String[] filenames = null;
        if(currentDir!=null)
            filechooser.setCurrentDirectory(currentDir);

        int returnVal = filechooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            File[] files = filechooser.getSelectedFiles();
	    filenames = new String[files.length];
	    for(int i=0;i<files.length;i++)
		filenames[i] = files[i].getPath();

            currentDir = filechooser.getCurrentDirectory();
	    filechooser.setMultiSelectionEnabled(false);
            return filenames;
        }
	filechooser.setMultiSelectionEnabled(false);
        return null;
    }

    public Object loadModel(String filename) {
	try {
	    FileInputStream in = new FileInputStream(filename);
	    GZIPInputStream in2 = new GZIPInputStream(in);
	    ObjectInputStream s = new ObjectInputStream(in2);
	    Object temp = s.readObject();
	    s.close();
	    in.close();
	    if (temp instanceof FAModel) {
		return temp;
	    } else {
		System.out.println("File format error.");
		return null;
	    }
	} catch (Exception e) {
	    System.out.println("Exception in file read in: " + e);
	    return null;
	}
    }

    public static boolean saveModel(Object toSave, String filename){
	try {
	    
	    FileOutputStream out = new FileOutputStream(filename);
	    GZIPOutputStream out2 = new GZIPOutputStream(out);
	    ObjectOutputStream s = new ObjectOutputStream(out2);

	    s.writeObject(toSave);
	    s.flush();
	    s.close();
	    return true;
	} catch (Exception e) {
	    System.out.println("Exception in file write out: " + e);
	    return false;
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
	    //String mailref = (String)sorter.getValueAt(row, 9);
	    //table.setSelectionBackground(Color.blue);
	    //showEmailBody(mailref);
	}
	else if(button>1)
	    classMenu.show(table, point.x, point.y);
    }  

    public String[][] runSql(String sql) {
        String[][] data = null;
        try {
            synchronized (jdbc) {
                data = jdbc.getSQLData(sql);
               // data = jdbc.getRowData();
            }
        } catch (SQLException ex) {
            if(!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
                System.out.println("ex: "+ex);
                System.out.println("sql: " +sql);
	    }
        }
        return data;
    }   
    

}
    
/*
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
	
    public Object getValueAt(int r, int c)
    {  return model.getValueAt(rows[r].index, c);
    }
	
    public boolean isCellEditable(int r, int c)
    {  return model.isCellEditable(rows[r].index, c);
    }
	
    public void setValueAt(Object aValue, int r, int c)
    {  model.setValueAt(aValue, rows[r].index, c);
    }
	
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
*/

//an object for file analyzer
class FAObj implements Serializable{
    int gram = 0;
    double[] feature = null;
    String filename;
    String ext;

    public FAObj(int g, double[] array, String f, String e){
	gram = g;
	feature = new double[array.length];
	for(int i=0;i<array.length;i++)
	    feature[i] = array[i];
	
	filename = f;
	ext = e;
	//System.out.println(e);
    }

    public FAObj(FAObj o){
	gram = o.gram;
	filename = o.filename;
	ext = o.ext;
	feature = new double[o.feature.length];
	for(int i=0;i<feature.length;i++)
	    feature[i] = o.feature[i];
    }
}

class FAModel implements Serializable{
    int gram = 0;
    Vector files;
    Vector exts;

    public FAModel(int g){
	gram = g;
	files = new Vector();
	exts = new Vector();
    }

    //copy and object
    public FAModel(FAModel m){
	if(m != null){
	    gram = m.gram;
	    files = new Vector();
	    exts = new Vector();
	    for(int i=0;i<m.files.size();i++)
		files.add((FAObj)m.files.get(i));
	    for(int i=0;i<m.exts.size();i++)
		exts.add((String)m.exts.get(i));
	}
    }
    
    public void addobj(FAObj o){
	files.add(o);
	if(!exts.contains(o.ext))
	    exts.add(o.ext);
    }

    //get the arrays
    public SortMsgObj[] computeInfo(){
	int extsize = exts.size();
	HashMap hashtable = new HashMap();

	//use hash table to collect data, classfied by the extension
	for(int i=0;i<files.size();i++){
	    FAObj obj = (FAObj)files.get(i);
	    String key = obj.ext;
	    if(hashtable.containsKey(key)){
		FAFeatureInfo info = (FAFeatureInfo)hashtable.remove(key);
		info.update(obj.feature);
		hashtable.put(key, info);
	    }
	    else{
		FAFeatureInfo info = new FAFeatureInfo(gram, obj.feature, key);
		hashtable.put(key, info);
	    }
	}

	//retrieve the hash table and arrays
	FAFeatureInfo[] tmpinfo = getMap(hashtable);
	int size = 0;
	if(gram == 1)size = 256;
	else size = 256*256;
	
	//the length is the number of distinct extensions
	SortMsgObj[] msg = new SortMsgObj[tmpinfo.length];
	for(int i=0;i<tmpinfo.length;i++){
	    msg[i] = new SortMsgObj(size, gram, tmpinfo[i].name
				    , (int)Math.round(tmpinfo[i].size));
	    for(int j=0;j<size;j++){
		//have to normalize...
		msg[i].value[j] = tmpinfo[i].array[j] / tmpinfo[i].size;
		msg[i].std[j] = Math.abs(tmpinfo[i].var[j] - tmpinfo[i].array[j]);
	    }
	}

	Arrays.sort(msg);
	return msg;
    }

    public FAFeatureInfo[] getMap(HashMap map){
	FAFeatureInfo[] features = new FAFeatureInfo[map.size()];
	
	int index = 0;
	//read the data in the hashtable
	Set entries = map.entrySet();
	Iterator iter = entries.iterator();
	while(iter.hasNext()){
	    Map.Entry entry = (Map.Entry)iter.next();
	    features[index] = (FAFeatureInfo)entry.getValue();
	    index++;
	}

	return features;
    }

    private class FAFeatureInfo{
	double[] array;//the feature
	double[] var;
	double size;//number of files...
	String name;
	
	public FAFeatureInfo(int gram, double[] a, String n){
	    if(gram==1){
		array = new double[256];
		var = new double[256];
	    }
	    else{
		var = new double[256*256];
		array = new double[256*256];
	    }
	    for(int i=0;i<a.length;i++){
		array[i] = a[i];
		var[i] = a[i]*a[i];
	    }
	    size = 1;
	    name = n;
	}

	public void update(double[] a){
	    size++;
	    for(int i=0;i<a.length;i++){
		array[i] += a[i];
		var[i] += a[i]*a[i];
	    }
	}
    }

}

/*
class MsgObj{
    double[][] array;
    String[] names;
    int[] sizes;
    int gram = 0;

    public MsgObj(int x, int y, int g){
	array = new double[x][y];
	names = new String[x];
	sizes = new int[x];
	gram = g;
    }
}
*/

class SortMsgObj implements Comparable{
    double[] value;
    double[] std;//standard deviation of each byte
    String name;
    int gram;
    int size;

    public SortMsgObj(int length, int g, String n, int s){
	value = new double[length];
	std = new double[length];
	name = n;
	gram = g;
	size = s;
    }

    public int compareTo(Object other) throws ClassCastException {
	SortMsgObj s = (SortMsgObj)other;
	//return (int)(this.count - s.count);
	return this.name.compareTo(s.name);
    }
}
