
package metdemo.CliqueTools;



import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.CliqueTools.CliqueFinder.oneClique;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;

import java.text.SimpleDateFormat;

/** 
 * Graphic representation of clique relationships
 * Allows it to be calculated on both a user lever and enclave level relationship.
 * 
 * 
 * @author Wei-Jen Li
 * @author Shlomo Hershkop
 *
 *2007 - updated using jung libraries - shlomo
 */


public class VisualClique extends JPanel implements ActionListener{
   
	/* handle for passing and recieving information between windows */
	private winGui parentWingui = null;	
  //  ShowUser suser;
 	int currenttype = 0;
    JLabel average, std;

    private JTextField threshold; //use the sql here
    private JTextField userlimit;
    private JCheckBox senderonly;
    private JComboBox attach_type;
    JRadioButton chooseclique1, chooseclique2, usertype1, usertype2;
    
    private final int SYSD_CLIQUE = 1;
    private final int CHARLIE_CLIQUE =2;
    private final int YSPACING = 20;
    //JRadioButton slarge, smedium, ssmall;
   
   
   /* static final int SMALL = 1;
    static final int MEDIUM = 2;
    static final int LARGE = 3;*/
  //  int selectedSize = 1;
   /*  private final int smallPANE = 400;
     private final int mediumPANE = 800;
     private final int largePANE = 1600;*/
   // private int PANE_SIZE = smallPANE;
    //private int PaneDuringDraw = smallPANE;
  
	
    //ArrayList<VCNode> cliqueNodes;//Node
   
   // ArrayList lineNodes;//Node
  //  VCNode lastCliqueNode = null;
  //  ArrayList lastUserNode = null;//String
  //  VCNode lastLineNode = null;
    
   
    //CliqueDetail cliquedetail = null;//for a popup window
/*    static final int smallPoint = 10;
    static final int bigPoint = 14;
*/
    //remember last used clique, if change algorithm, read it back fast
/*    ArrayList lastUsedClique = null;
    ArrayList currentUsingClique = null;
*/   
    
    //for the user part...
    //int biggestuser = 0;
    private String m_aliasFilename;
   int lastUsedType = 1;
   EMTConfiguration emtConfigHandle;
   ArrayList userNodes;//Node
   private EMTDatabaseConnection jdbcView = null;
   
   
   private cliqueHierarchyVisualiazerJung CHVJ = new cliqueHierarchyVisualiazerJung();
   private JungCliquesVisualizer JCV = new JungCliquesVisualizer(CHVJ);
   
    /**
     * Main constructor to setup the initial window
     * 
     * @param jdbcU database handle
     * @param filename alias host filename
     * @param wg handle to wingui
     * @param stpl stop list
     */
    public VisualClique(final EMTDatabaseConnection jdbcU, final EMTConfiguration emtc, final String filename, final winGui wg) {
	
    	emtConfigHandle = emtc;
    	parentWingui = wg;
    	jdbcView = jdbcU;
    	m_aliasFilename = filename;
	//TODO: automacticall resize when choose small/large
	JButton Load, Load2;
	JPanel mainPanel, topPanel, bottomDataResult;//, down;
	
	Box keywordSizeBox, minConSenderBox, cliqueHelpTypeBox;
	//the main panel
	mainPanel = this;
	mainPanel.setLayout(new BorderLayout());
		
	topPanel = new JPanel();
	GridBagConstraints gbc2 = new GridBagConstraints();
	GridBagLayout gridbag2 = new GridBagLayout();
	
	topPanel.setLayout(gridbag2);	/************************/
	keywordSizeBox = Box.createHorizontalBox();
	
	chooseclique1 = new JRadioButton("With Keyword");
	chooseclique1.setActionCommand("clique1");
	chooseclique1.addActionListener(this);
	chooseclique1.setSelected(true);
	chooseclique2 =  new JRadioButton("Without");
	chooseclique2.setActionCommand("clique2");
	chooseclique2.addActionListener(this);
	chooseclique1.setToolTipText("Select a clique-finding algorithm, with Common Subject Words (charlie)");
	chooseclique2.setToolTipText("Select a clique-finding algorithm, without Common Subject Words (branch bound)");
	
	ButtonGroup chooseCliqueButtonGroup = new ButtonGroup();
	chooseCliqueButtonGroup.add(chooseclique1);
	chooseCliqueButtonGroup.add(chooseclique2);

	//firstBox.add(new JLabel("Algorithm:  "));
	keywordSizeBox.add(chooseclique1);
	keywordSizeBox.add(chooseclique2);
	keywordSizeBox.add(Box.createRigidArea(new Dimension(10, 10)));

	/*slarge = new JRadioButton("Large");
	slarge.setActionCommand("sl");
	slarge.addActionListener(this);
	smedium = new JRadioButton("Medium");
	smedium.setActionCommand("sm");
	smedium.addActionListener(this);
	ssmall = new JRadioButton("Small");
	ssmall.setSelected(true);
	ssmall.setActionCommand("ss");
	ssmall.addActionListener(this);
	ButtonGroup sizeButtonGroup = new ButtonGroup();
	sizeButtonGroup.add(ssmall);
	sizeButtonGroup.add(smedium);
	sizeButtonGroup.add(slarge);*/
	/*slarge.setToolTipText("Define the size of the display panel");
	smedium.setToolTipText("Define the size of the display panel");
	ssmall.setToolTipText("Define the size of the display panel");*/

/*	keywordSizeBox.add(new JLabel("Zoom Size: "));
	keywordSizeBox.add(ssmall);
	keywordSizeBox.add(smedium);
	keywordSizeBox.add(slarge);*/

	//the radio buttons
	minConSenderBox = Box.createHorizontalBox();
	threshold = new JTextField(3);
	//threshold.setPreferredSize(new Dimension(100,10));
	threshold.setMaximumSize(new Dimension(100,25));
	threshold.setMinimumSize(new Dimension(70,25));
	threshold.setText("10");
	threshold.setToolTipText("The limit of each \"Edge\"");

	minConSenderBox.add(new JLabel("  Min Connection: "));
	minConSenderBox.add(threshold);
	
	//senderonly = new JCheckBox("Use Frequent Users");
	senderonly = new JCheckBox("Use accounts with more than ");
	senderonly.setToolTipText("Only calculate accounts that have lots of emails");
	userlimit = new JTextField(3);
	userlimit.setText("200");
	userlimit.setMaximumSize(new Dimension(100,25));
	userlimit.setMinimumSize(new Dimension(70,25));
	//userlimit.setPreferredSize(new Dimension(100,10));

	minConSenderBox.add(Box.createRigidArea(new Dimension(60, 10)));
	minConSenderBox.add(senderonly);
	//sndBox.add(Box.createRigidArea(new Dimension(10, 10)));
	//sndBox.add(new JLabel("Limit: "));
	minConSenderBox.add(userlimit);
	minConSenderBox.add(new JLabel("  records"));

	//no use now
	usertype1 = new JRadioButton("Outbound");
	usertype1.setActionCommand("Outbound");
	usertype1.addActionListener(this);
	usertype1.setSelected(true);
	usertype2 =  new JRadioButton("Inbound");
	usertype2.setActionCommand("Inbound");
	usertype2.addActionListener(this);
	
	usertype1.setEnabled(false);
	usertype2.setEnabled(false);
	ButtonGroup ubg = new ButtonGroup();
	ubg.add(usertype1);
	ubg.add(usertype2);

	/*************************************/
	cliqueHelpTypeBox = Box.createHorizontalBox();
	Load = new JButton("Enclave Clique");
	Load.setBackground(Color.green);
	Load.setMnemonic(KeyEvent.VK_S);
	Load.addActionListener(this);

	Load2 = new JButton("User Clique");
	Load2.setBackground(Color.cyan);
	Load2.setMnemonic(KeyEvent.VK_U);
	Load2.addActionListener(this);
	
	

	JButton statisticsb = new JButton("Statistical Info.");
	statisticsb.setBackground(new Color(204,0,153));
	statisticsb.setToolTipText("click to get the statistical information of sending email");
	statisticsb.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    showStatistics();
		}
	    });

	EMTHelp emthelp = new EMTHelp(EMTHelp.VISUALCLIQUE);

	cliqueHelpTypeBox.add(Load);
	cliqueHelpTypeBox.add(Box.createRigidArea(new Dimension(10, 10)));
	cliqueHelpTypeBox.add(Load2);
	cliqueHelpTypeBox.add(Box.createRigidArea(new Dimension(10, 10)));
	cliqueHelpTypeBox.add(statisticsb);
	cliqueHelpTypeBox.add(Box.createRigidArea(new Dimension(10, 10)));
	cliqueHelpTypeBox.add(emthelp);

	//RULEZ
	ComboBoxModel model2 = new DefaultComboBoxModel(parentWingui.getAttachments());
	attach_type = new JComboBox(model2);
	attach_type.insertItemAt(new String("All Attachment types"),0);
	attach_type.insertItemAt(new String("All text types"),1);
	attach_type.setToolTipText("Choose type of email attachmant to analyze in window");
	attach_type.setSelectedIndex(0);
	//sndBox.add(Box.createRigidArea(new Dimension(40, 10)));
	cliqueHelpTypeBox.add(new JLabel("Attachment Type: "));
	cliqueHelpTypeBox.add(attach_type);

	
	/************************/
		
	
	//the upper panel
	gbc2.gridwidth = 1;
	gbc2.gridx = 0;
	gbc2.gridy = 0;
	
	topPanel.add(keywordSizeBox,gbc2);
	gbc2.gridwidth = 1;
	gbc2.gridx = 1;
	gbc2.gridy = 0;
	
	topPanel.add(minConSenderBox,gbc2);
	
	gbc2.gridwidth = 2;
	gbc2.gridx = 0;
	gbc2.gridy = 1;
	
	topPanel.add(cliqueHelpTypeBox,gbc2);
	//upper.add(fourthBox);
		
	//the center
	bottomDataResult = new JPanel();
		
	//sclique = new ShowClique();
	JScrollPane showCliqueScrollPane = new JScrollPane(JCV);//sclique);
	Border bd1 =
	    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
	TitledBorder ttl1 = new TitledBorder(bd1,"Clique");
	showCliqueScrollPane.setBorder(ttl1);
	showCliqueScrollPane.setPreferredSize(new Dimension(470,470));
		
	//suser = new ShowUser();
	JScrollPane showUserScrollPane = new JScrollPane(CHVJ);//suser);
	Border bd2 =
	    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
	TitledBorder ttl2 = new TitledBorder(bd2,"User");
	showUserScrollPane.setBorder(ttl2);
	showUserScrollPane.setPreferredSize(new Dimension(470,470));

	//for fun
	//demo = new CliqueInformationScreen();
	JScrollPane informationBoardPanel = new JScrollPane(JCV.getInformationPanel());
	Border bd3 =
	    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
	TitledBorder ttl3 = new TitledBorder(bd3,"Information Board");
	informationBoardPanel.setBorder(ttl3);//changed from scrd
	//informationBoardPanel.setPreferredSize(new Dimension(920,120));//changed from scrd

	//not safe, but works
	/*suser.showclique = sclique;
	suser.demo = demo;
	sclique.showuser = suser;
	sclique.demo = demo;
	demo.showclique = sclique;
	demo.showuser = suser;*/
		
	//middle.setLayout(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout gridbag = new GridBagLayout();
	bottomDataResult.setLayout(gridbag);
	
	gbc.gridwidth = 2;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weighty =1;
	gbc.fill = GridBagConstraints.BOTH;
	bottomDataResult.add(informationBoardPanel, gbc);
	
	gbc.gridwidth = 1;
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbc.weighty =1;
	gbc.fill = GridBagConstraints.BOTH;
	bottomDataResult.add(JCV.getControlPanel(), gbc);
	//shlomo
	gbc.gridwidth = 1;
	gbc.gridx = 1;
	gbc.gridy = 3;
	gbc.weighty =1;
	gbc.fill = GridBagConstraints.BOTH;
	bottomDataResult.add(CHVJ.getControlPanel(), gbc);
	//*****************

	gbc.gridwidth = 1;
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weighty = 5;
	gbc.weightx = 2;
	bottomDataResult.add(showCliqueScrollPane, gbc);
	gbc.gridx = 1;
	gbc.gridy = 1;
	bottomDataResult.add(showUserScrollPane, gbc);
//	JScrollPane dataScroll = new JScrollPane(bottomDataResult);
	
	/*****************************/
	
	mainPanel.add(topPanel, BorderLayout.NORTH);
	mainPanel.add(bottomDataResult, BorderLayout.CENTER);
		
    }

    public void showStatistics()
    {
	
    	double[] distri = countConnect();

    	if(distri!=null && distri.length>0){
    		int above = 0;
		int thr = Integer.parseInt(threshold.getText());
	    int max = (int)Math.round(distri[0]);
	    for(int i=0;i<distri.length;i++){
		int amount = (int)Math.round(distri[i]);
		if(max<amount)max = amount;
		if(amount>thr)above++;
	    }

	    double[] refined = refineCurve(distri, max);
	    double[] avg_std = getAvgStd(distri);
	    String s1 = String.valueOf(avg_std[0]);
	    String s2 = String.valueOf(avg_std[1]);
	    if(s1.indexOf(".")!=-1){
		if(s1.length()>s1.indexOf(".")+4)
		    s1 = s1.substring(0,s1.indexOf(".")+4);
	    }
	    if(s2.indexOf(".")!=-1){
		if(s2.length()>s2.indexOf(".")+4)
		    s2 = s2.substring(0,s2.indexOf(".")+4);
	    }
	    
	    String msg1 = "Average Connection: " + s1;
	    String msg2 = "Standard Deviation: " + s2;
	    String msg3 = "# of the Maximum connection of a single sender: "+ String.valueOf(max);
	    String msg4 = "# of senders that have more connections than the current threshold: "+above;
		

	    VCliqueInfo info = new VCliqueInfo(msg1, msg2, msg3, msg4, this);
	    info.setFigure("Email Sending Distribution"
			   ,"Sender Distribution"
			   ,"# of Emails"
			   ,info.distribution
			   ,Color.blue
			   ,refined);

	    info.setVisible(true);
	}
	else{
	    JOptionPane.showMessageDialog(this,
					  "no email data here.\n",
					  "Message",
					  JOptionPane.INFORMATION_MESSAGE);
	}
    }

    //change the style of curve to a better form
    public double[] refineCurve(double[] array, int max){
	int length = array.length;
	

	double[] result = new double[max+1];
	for(int i=0;i<length;i++){
	    int amount = (int)Math.round(array[i]);
	    result[amount]++;
	}

	return result;
    }

    public double[] countConnect(){
	
    String[] date = getDate();
	String query = "select sender,dates,count(mailref) from email where DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"' group by sender,dates order by sender";

	String[][] data = runSql(query);

	//get the average of each single sender
	String currSender = "";
	double currCnt = 0.0;
	double singleCnt = 1.0;
	ArrayList cntArray = new ArrayList();
	for(int i=0;i<data.length;i++){
	    if(i==0){
		currSender = data[i][0];
		currCnt = Double.parseDouble(data[i][2]);
	    }
	    else if(currSender.equals(data[i][0])){
		currCnt += Double.parseDouble(data[i][2]);
		singleCnt += 1;
	    }
	    else{
		cntArray.add(String.valueOf(currCnt/singleCnt));
		currCnt = Double.parseDouble(data[i][2]);
		currSender = data[i][0];
		singleCnt = 1.0;
	    }

	    if(i==data.length-1)
		    cntArray.add(String.valueOf(currCnt));
	}
	
	//get the average of all the senders
	int length = cntArray.size();
	double[] element = new double[length];
	for(int i=0;i<length;i++){
	    element[i] = Double.parseDouble((String)cntArray.get(i));
	}

	return element;
	
    }

    public double[] getAvgStd(double[] element){

	int length = element.length;
	double total = 0.0;
	for(int i=0;i<length;i++){
	    total += element[i];
	}
	double avg = total / (double)length;

	double var = 0.0;
	for(int i=0;i<length;i++){
	    var += (element[i]-avg) * (element[i]-avg);
	}
	
	double std = Math.sqrt(var/(double)length);
	
	double[] avg_std = {avg, std};

	return avg_std;
    }

    public String[][] runSql(String sql) {
        String[][] data = null;
        try {
            synchronized (jdbcView) {
               data =  jdbcView.getSQLData(sql);
            }
        } catch (SQLException ex) {
            if(!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
                System.out.println("ex: "+ex);
                System.out.println("sql: " +sql);
	    }
        }
        return data;
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
	
    //execute the sql command
    /*public final void run(final String sql) {
	try{
	    synchronized (jdbcView){
		data = jdbcView.getSQLData(sql);
		}
	}
	catch (SQLException ex){
	    if(!ex.toString().equals("java.sql.SQLException: ResultSet is from UPDATE. No Data") ){
		System.out.println("ex: "+ex);
		System.out.println("sql:"+sql);
	    }
	}

		
    }*/

    //generate the cliques and show them, by Charlie's Algorithm
    //enclave clique
    public final ArrayList loadclique(){
	
	int thr = Integer.parseInt(threshold.getText());
	String[] date = getDate();
	String[][] tmpsenders;
	String[][] temp = null;
	
	String query = "";
	if(!senderonly.isSelected()){
	    try {
		query = "select mailref,sender,rcpt from email where DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"' ORDER BY mailref";
		synchronized (jdbcView){
		    temp = jdbcView.getSQLData(query);
		}
	    } catch (SQLException ex1) {System.out.println(ex1);}
	}
	else{
	    try{
		//get the senders first
		query = "SELECT sender,count(*) from email where DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"' group by sender having count(*) > "+userlimit.getText();
		
		synchronized (jdbcView){
			tmpsenders = jdbcView.getSQLData(query);

		query = "select mailref,sender,rcpt from email where DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"' and (";
	
		//NOTE FROM SHLOMO TO WIEJEN (JAN 2004) see sender in ('..','...',etc)
		for(int i=0;i<tmpsenders.length;i++){
		    query += " sender='"+tmpsenders[i][0]+"'";
		    if(i!=tmpsenders.length-1)query += " or";
		}
		query += ") ORDER BY mailref";
		
		temp = jdbcView.getSQLData(query);
		}
	    } catch (SQLException ex1) {System.out.println(ex1);}
	}
	
	
	
	System.out.println("sql ok!");
	System.out.println("user ok!");
	System.out.println("clique generating..."+temp.length);
	long before = System.currentTimeMillis();
	EnclaveCliqueAlgo2 eclique = new EnclaveCliqueAlgo2(temp, thr, m_aliasFilename);
	eclique.findMaxCliques();
	ArrayList v = eclique.getMaxCliques();
	//Vector v = sc.generateSocialCliques(date[0], date[1]);
	long after = System.currentTimeMillis();
	System.out.println("clique ok!"+" uses "+((after-before)/1000)+" seconds.");

	return v;
    }

    //SYSD's algorithm
    public final ArrayList loadclique2(){

	//fgure out attachment type
	ArrayList cliques = new ArrayList();
	try{
	    String types = new String();
	    //which types to use:
	    int mi = attach_type.getSelectedIndex();
	    if( mi ==0)
		types  = "";
	    else if(mi ==1)
		types = "text/%";
	    else
		types = (String)attach_type.getSelectedItem();




	    int thr = Integer.parseInt(threshold.getText());
	    String[] date = getDate();

	    String query = "";
	    //"select sender, rcpt, subject from email"
	    if(!senderonly.isSelected()){
		query = "select sender,rcpt,subject from  email left join message on email.mailref = message.mailref where email.DATEs>'"+date[0]+"' and email.DATEs<'"+date[1]+"' ";
		if(mi!=0)
		    query += "and message.type like '"+types+"'";
	    }
	    else{
		//get the senders first
		query = "SELECT sender,count(*) from email where DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"' group by sender having count(*) > "+userlimit.getText();
		String [][]tmpsenders;
		synchronized (jdbcView){
			tmpsenders = jdbcView.getSQLData(query);
		   
		}
		query = "select sender,rcpt,subject from email left join message on email.mailref = message.mailref where email.DATEs>'"+date[0]+"' and email.DATEs<'"+date[1]+"' ";

		if(mi!=0)
		    query += " and message.type like '"+types+"'";

		query+= " and (";
		for(int i=0;i<tmpsenders.length;i++){
		    query += " sender='"+tmpsenders[i][0]+"'";
		    if(i!=tmpsenders.length-1)query += " or";
		}
		query += ")";
	    }

	    System.out.println("start clique...");
	    CliqueFinder cliqueFinder = new CliqueFinder(jdbcView, m_aliasFilename,emtConfigHandle.getProperty(EMTConfiguration.STOPLIST));
	    
	    System.out.println("clique generating...");
	    long before = System.currentTimeMillis();
	    HashMap<String,HashMap> mapped =  cliqueFinder.buildGraph(thr, 3, query);//minEdgeWeight, minSubjectWordLength
	    ArrayList<oneClique> cliqlist = cliqueFinder.findCliques(mapped,false);//not print, false
	    cliques = cliqueFinder.getReadableCliques(cliqlist);
	    long after = System.currentTimeMillis();
	    System.out.println("clique ok!"+" uses "+((after-before)/1000)+" seconds.");

	}
	catch(Exception e){
	    System.out.println("clique error:"+e);
	}
	return cliques;
    }

    public final ArrayList<ArrayList<String>> callUserClique(final String user,final String startDate,final String endDate){
    	ArrayList<ArrayList<String>> clique = null;
	try{
	    System.out.println("starting clique calculations...");
	   
	    String query = "select mailref,rcpt from email "+
		"where sender ='" + user + "'" +
		" and dates>= '"+startDate+"'"  +
		" and dates<='"+endDate+"'" +
		" order by mailref";
	  
	    String[][] datarecords = null;
	   /* try {
		synchronized (jdbcView){*/
			datarecords = jdbcView.getSQLData(query);
		    //records = jdbcView.getRowData();
		/*}
	    } catch (SQLException ex1) {System.out.println(ex1);}
	   */
	    System.out.println("sql ok!");

	    System.out.println("clique generating...");
	    long before = System.currentTimeMillis();
	    UserCliqueAlgo2 uc = new UserCliqueAlgo2(datarecords,user,m_aliasFilename);
	    uc.findMaxSets();
	    clique = uc.getMaxSets();

	   
	    long after = System.currentTimeMillis();
	    System.out.println("clique ok!"+" uses "+((after-before)/1000.0)+" seconds.");
	    //get an array of cliques
	    /*
	      for(int i=0;i<emails.length;i++){
	      CliqueWithCommonWords oneclique = new CliqueWithCommonWords();
	      for(int j=0;j<emails[i].length;j++){
	      if(emails[i][j]!=null)oneclique.users.add(emails[i][j]);
	      }
	      clique.add(oneclique);
	      }
	    */
	}catch(SQLException ee){
		ee.printStackTrace();
		return null;
	}
	catch(Exception e){
	    e.printStackTrace();
	    return null;
	}
	return clique;
    }

    public final String[][] getMailref(final String user){
	String[][] mailref = null;
	String[] date = getDate();
	String query = "";
	if(usertype1.isSelected()){
	    query = "SELECT DISTINCT mailref FROM email where sender='"+user+"' and DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"'";
	}
	else{
	    query = "SELECT DISTINCT mailref FROM email where rcpt='"+user+"' and DATEs>'"+date[0]+"' and DATEs<'"+date[1]+"'";
	}

	try {
	    synchronized (jdbcView){
		mailref = jdbcView.getSQLData(query);
	//	mailref = jdbcView.getRowData();

	    }
	} catch (Exception ex1) {System.out.println(ex1);}

	
	return mailref;
    }
    
    public final int getColSize(final String user){
	int column = 0;
	try {
	    synchronized (jdbcView){
		column = jdbcView.getCountOnSQLData("SELECT * from email where sender = '"+user+"'");
		//column = jdbcView.getResultSize();
	    }
	}
	catch (Exception ex2) {
	    System.out.println(ex2);
	}
	return column;
    }

    
   /**
    * Display the clique graph
    * @param v of clique lists
    * @param type
    */
    public final void showTheClique(final ArrayList v, final int type) {

		if (v == null) {
			return;
		}
		
		
		/*cliqueNodes = new ArrayList<VCNode>();
		lineNodes = new ArrayList();*/
		int arraylistSize = v.size(); //number of vertices
		
		
		
		  //this is for the jung stuff
		  JCV.restart();
		  CHVJ.restart();
		  /*
		   * We are going to map all edges using an adjacency list of vertices
		   * mapping user x to a list of vertices they are in so we can make it
		   *  easy to generate edges correctly
		   */
		  HashMap<String, ArrayList<Integer>> edgeMapOne = new HashMap<String, ArrayList<Integer>>();
		  
		  
		  

		
		// int size = 73;//number of points
		// TODO: actual settinghere
		//int radius = PANE_SIZE / 2;
		//int base = PANE_SIZE / 40;// ...

		// int scalingFactor = PANE_SIZE/SMALL_PANE;
		/*int center = base + radius;
		System.out.println("radius: " + radius + " center: " + center);
	*/	// double interval = 360.0/(double)size;
		double interval = (2.0 * Math.PI) / arraylistSize;

		double index = 0;

		// for the right part
		//int interval2 = PANE_SIZE / (arraylistSize + 1);
		//int y2 = base + interval2;

		if (type == SYSD_CLIQUE) {// SYSD's
			for (int i = 0; i < arraylistSize; i++) {
				//double x = center + (radius * Math.cos(index));
				//double y = center + (radius * Math.sin(index));

				CliqueWithCommonWords oneclique = (CliqueWithCommonWords) v
						.get(i);
				//VCNode n = new VCNode(Color.blue, (int) x, (int) y, smallPoint,
				//		i,oneclique);

				/*for (int j = 0; j < oneclique.users.size(); j++) {
					n.users.add(oneclique.getUser(j));
					// System.out.println("user:"+oneclique.users.get(j));
				}

				for (int j = 0; j < oneclique.commonWords.size(); j++) {
					n.commonwords.add(oneclique.commonWords.get(j));
					// System.out.println("words:"+oneclique.commonWords.get(j));
				}*/

				//cliqueNodes.add(n);
				index += interval;

				// for the right part
			//	n.xPos2 = base + 20;
			//	n.yPos2 = y2;
			//	y2 += interval2;
				
//				test of jung stuff
				/**
				 * linear algorithm to add edges as we go, since we are adding a vertice now, its a simple matter of checking for past edges seen already
				 */
				ArrayList<String> alltheusrs = oneclique.getAllUsers();
				cliqueVertex vertexHandle = new cliqueVertex(alltheusrs,oneclique.getAllCommon());
				JCV.addVertex(vertexHandle);
				for (int j = 0; j < alltheusrs.size(); j++) {
					String username = alltheusrs.get(j);
					//lets add tot eh edge map
					if(edgeMapOne.containsKey(username)){
					
						//this means we;ve seen this user before, so lets add the appropriate edges
						ArrayList<Integer> listedges  = edgeMapOne.get(username);
						for(Iterator<Integer> vertwalk = listedges.iterator();vertwalk.hasNext();){
						Integer targetvert = vertwalk.next();
							JCV.addEdge(new cliqueEdgeJung(vertexHandle,JCV.getVertex(targetvert.intValue())));
						}
						
						
						
						
					}
					else{
					ArrayList<Integer> listedges = new ArrayList<Integer>();
					 listedges.add(i);
						
					 edgeMapOne.put(username, listedges);
					}
				}
				
				
				
				
			}
		} else {// Charlie's
			for (int i = 0; i < arraylistSize; i++) {
				//double x = center + (radius * Math.cos(index));
				//double y = center + (radius * Math.sin(index));

				// Object[] obj = (Object[])v.get(i);
				ArrayList<String> oneclique = (ArrayList<String>) v.get(i);
				//VCNode n = new VCNode(Color.blue, (int) x, (int) y, smallPoint,
				//		i,oneclique);
				// for(int j=0;j<obj.length;j++){
				/*for (int j = 0; j < oneclique.size(); j++) {
					// String oneaccount = (String)oneclique.get(j);
					n.users.add(oneclique.get(j));// oneaccount);
				}*/
				//cliqueNodes.add(n);
				index += interval;

				//for the right part
			//	n.xPos2 = base + 20;
			//	n.yPos2 = y2;
			//	y2 += interval2;
				
				//test of jung stuff
				/**
				 * linear algorithm to add edges as we go, since we are adding a vertice now, its a simple matter of checking for past edges seen already
				 */
				cliqueVertex vertexHandle = new cliqueVertex(oneclique);
				JCV.addVertex(vertexHandle);
				for (int j = 0; j < oneclique.size(); j++) {
					String username = (String)oneclique.get(j);
					//lets add tot eh edge map
					if(edgeMapOne.containsKey(username)){
					
						//this means we;ve seen this user before, so lets add the appropriate edges
						ArrayList<Integer> listedges  = edgeMapOne.get(username);
						for(Iterator<Integer> vertwalk = listedges.iterator();vertwalk.hasNext();){
						Integer targetvert = vertwalk.next();
							JCV.addEdge(new cliqueEdgeJung(vertexHandle,JCV.getVertex(targetvert.intValue())));
						}
						
						
						
						
					}
					else{
					ArrayList<Integer> listedges = new ArrayList<Integer>();
					 listedges.add(i);
						
					 edgeMapOne.put(username, listedges);
					}
				}
			
				//end jung stuff
				
				
				
			}
			//lets display the cliques
			
		}
		 JCV.showSomething(false,false);
	}

   /**
    * Calculate the User nodes on the left side panel
    * @param cliq_dataArray array list of clique data
    * @param type which clique algorithm to use
    */
    public final void showUserNodes(final ArrayList cliq_dataArray, final int type){
	
    if (cliq_dataArray == null) {
			return;
	}
    CHVJ.restart();//?????
    
    //need some helpers for jung stuff
    HashMap<String, userVertex> userVerts = new HashMap<String, userVertex>();
    HashMap<String, Integer> userCounter = new HashMap<String, Integer>();
    HashMap<String, String> cliqueEdgeList = new HashMap<String, String>();
    HashMap<Integer, cliqueVertex> cliqueVerts = new HashMap<Integer, cliqueVertex>();
    
    	
	int cliqArraysize = cliq_dataArray.size();		
	//HashMap<String, VCNode> userCounts = new HashMap<String, VCNode>();
	
	//biggestuser = 0;

	//compute the involved cliques of each user
	if (type == SYSD_CLIQUE) {
			for (int count = 0; count < cliqArraysize; count++) {
				CliqueWithCommonWords oneclique = (CliqueWithCommonWords) cliq_dataArray.get(count);
				
				//jung lets create a vertex for this clique
				cliqueVerts.put(count, new cliqueVertex(oneclique.getAllUsers()));
				
				
				for (int j = 0; j < oneclique.getNumUsers(); j++) {
					
					String nameUsr = (String) oneclique.getUser(j);
				
					/*if (userCounts.containsKey(nameUsr)) {
						VCNode n = (VCNode) userCounts.get(nameUsr);
						n.clique.add(new Integer(count));
						n.count++;
						if (biggestuser < n.count)
							biggestuser = n.count;
						userCounts.put(nameUsr, n);
					} else {
						VCNode n = new VCNode(nameUsr);
						n.clique.add(new Integer(count));
						userCounts.put(nameUsr, n);
					}*/
					
					//jung stuff
					//we want to make sure we have a vertex for each user (for edge lists
					if(!userVerts.containsKey(nameUsr)){
						userVerts.put(nameUsr, new userVertex(nameUsr));
					}
					
//					jung count user
//					user2
					if(userCounter.containsKey(nameUsr)){
						userCounter.put(nameUsr, userCounter.get(nameUsr)+1);
					}else{
						userCounter.put(nameUsr,1);
					}
					
					//jung counter 
//					now to count number of time seen each
					if(userCounter.containsKey(nameUsr)){
						userCounter.put(nameUsr, userCounter.get(nameUsr)+1);
					}else{
						userCounter.put(nameUsr,1);
					}
				
				
					cliqueEdgeList.put(count+"###"+j,nameUsr);
				}//foreach user
				
				//jung want to generate all edges for current clique
				//so if clique is A,B,D want a-b, a-d, b-d
//				for (int j = 0; j < oneclique.getNumUsers(); j++) {
//					for(int k=1;k<oneclique.getNumUsers(); k++){
//						String user1 = (String) oneclique.getUser(j);
//						String user2 = (String) oneclique.getUser(k);
//						
//						//lets order them
//						if(user1.hashCode() > user2.hashCode()){
//							String temp = user2;
//							user2 = user1;
//							user1 = temp;
//						}
//						
//						//if(userVerts.containsKey(user1) && userVerts.containsKey(user2)){
////						System.out.print("2");
////							UndirectedSparseEdge usedge = new UndirectedSparseEdge(userVerts.get(user1),userVerts.get(user2));
////						//edgeList.put(usedge, true);
//						//}
//						edgeList.put(user1+"###"+user2, true);
//						
//						
//						
//					}
//				}//end jung edge listing
				
			}
		}
	else {
			for (int count = 0; count < cliqArraysize; count++) {
				// Object[] obj = (Object[])v.get(i);
				ArrayList oneclique = (ArrayList) cliq_dataArray.get(count);
				
//				jung lets create a vertex for this clique
				cliqueVerts.put(count, new cliqueVertex(oneclique));
				
				for (int j = 0; j < oneclique.size(); j++) {
					String oneaccount = (String) oneclique.get(j);
					/*if (userCounts.containsKey(oneaccount)) {
						VCNode n = (VCNode) userCounts.get(oneaccount);
						n.clique.add(new Integer(count));
						n.count++;
						if (biggestuser < n.count)
							biggestuser = n.count;
						userCounts.put(oneaccount, n);
					} else {
						VCNode n = new VCNode(oneaccount);
						n.clique.add(new Integer(count));
						userCounts.put(oneaccount, n);
					}*/
					
//					jung stuff
					//we want to make sure we have a vertex for each user (for edge lists
					if(!userVerts.containsKey(oneaccount)){
						userVerts.put(oneaccount, new userVertex(oneaccount));
					}
					//now to count number of time seen each
					if(userCounter.containsKey(oneaccount)){
						userCounter.put(oneaccount, userCounter.get(oneaccount)+1);
					}else{
						userCounter.put(oneaccount,1);
					}
				
				
					cliqueEdgeList.put(count+"###"+j,oneaccount);
					
				}
//				jung want to generate all edges for current clique
				//so if clique is A,B,D want a-b, a-d, b-d
//				for (int j = 0; j < oneclique.size(); j++) {
//					for(int k=1;k<oneclique.size(); k++){
//						String user1 = (String) oneclique.get(j);
//						String user2 = (String) oneclique.get(k);
//						
////						lets order them
//						if(user1.hashCode() > user2.hashCode()){
//							String temp = user2;
//							user2 = user1;
//							user1 = temp;
//						}
//						
//							//UndirectedSparseEdge usedge = new UndirectedSparseEdge(userVerts.get(user1),userVerts.get(user2));
//						edgeList.put(user1+"###"+user2, true);
//						
////						now to count number of time seen each
//						if(userCounter.containsKey(user1)){
//							userCounter.put(user1, userCounter.get(user1)+1);
//						}else{
//							userCounter.put(user1,1);
//						}
//						//user2
//						if(userCounter.containsKey(user2)){
//							userCounter.put(user2, userCounter.get(user2)+1);
//						}else{
//							userCounter.put(user2,1);
//						}
//						
//					}
//				}//end jung edge listing
				
				
				
			}
		}

	//jung want to sort the counts hashmap
	

   

    // --- Put entries into map here ---

    // Get a list of the entries in the map
   List<Map.Entry<String, Integer>> listofCountedUsers = new ArrayList<Map.Entry<String, Integer>>(userCounter.entrySet());

    // Sort the list using an annonymous inner class implementing Comparator for the compare method
    java.util.Collections.sort(listofCountedUsers, new Comparator<Map.Entry<String, Integer>>(){
        public int compare(Map.Entry<String, Integer> entry, Map.Entry<String, Integer> entry1)
        {
            // Return 0 for a match, -1 for less than and +1 for more then
            return (entry.getValue().equals(entry1.getValue()) ? 0 : (entry.getValue() > entry1.getValue() ? 1 : -1));
        }
    });
	
    //we should have a sorted list now
	
	//end jung stuff
	
	
	//read them out
	/*VCNode[] nodes = new VCNode[userCounts.size()];

	//for the "out of picture" problem
	if (biggestuser < 1)
			biggestuser = 1;
	
	int[] biggest = new int[biggestuser];

	int index = 0;
	//read the data in the hashtable
	Set entries = userCounts.entrySet();
	Iterator iter = entries.iterator();
	while(iter.hasNext()){
	    Map.Entry entry = (Map.Entry)iter.next();
	   // Object key = (Object)entry.getKey();
	    nodes[index] = (VCNode)entry.getValue();
	    nodes[index].index = index;
	    biggest[nodes[index].count-1]++;
	    index++;
	}

	Arrays.sort(biggest);
//TODO: changed from 10 to 0
	//assign the position
	
	int PANE_SIZE =800;//tr
	int base = PANE_SIZE/40;//...
	//double cinterval = (double)PANE_SIZE/(double)(size+1);
	double uinterval = (double)PANE_SIZE/(double)(biggestuser);
	double yinterval = (double)PANE_SIZE/(double)(biggest[biggest.length-1]);
	int[] posIndex = new int[biggestuser];

	for(int i=0;i<biggestuser;i++){
	    posIndex[i]+= (yinterval/biggestuser)*i + base;
	}

	userNodes = new ArrayList();
	for(int i=0;i<nodes.length;i++){
	    nodes[i].xPos = nodes[i].count*(int)uinterval + base + 20;
	    posIndex[nodes[i].count-1]+=(int)yinterval;
	    nodes[i].yPos = posIndex[nodes[i].count-1];
	    nodes[i].color = Color.black;
	    nodes[i].size = 10;//smallPoint;
	    userNodes.add(nodes[i]);
	    
	    
	}*/
	//jung stuff
	
	//lets place the cliques down the left side
	int cliqueY=0;
	for(Iterator<Integer> allverts = cliqueVerts.keySet().iterator();allverts.hasNext();){
		Integer newone = allverts.next();
		CHVJ.addVertex(cliqueVerts.get(newone), 20, cliqueY+=15);
	}
	//now for the users
	int minValue = 0;
	int maxValue = 0;
	if(listofCountedUsers.size()>0){
		minValue = listofCountedUsers.get(0).getValue().intValue();
	
		maxValue=listofCountedUsers.get(listofCountedUsers.size()-1).getValue().intValue();
	}
	
	//jung lets run through the sorted list
	/*for (Map.Entry<String, Integer> entry: listofCountedUsers)
    {
        System.out.println(entry.getKey()+" "+ entry.getValue());
    } 
	System.out.println("MAx:"+maxValue + " min: " + minValue);
	*/
	
	int []locationYusers = new int[100];
	
	
	for(Iterator<String> allUsersverts = userVerts.keySet().iterator();allUsersverts.hasNext();){
		
		String username = allUsersverts.next();
		//lets see which x it should go in
		int counts = userCounter.get(username);
		int locationX=30;
		int locationY=30;
		if(counts == 1){
			locationX = 60;
			locationY = locationYusers[0] +=YSPACING;
		}else if (counts == 2){
			locationX = 80;
			locationY = locationYusers[1] +=YSPACING;
		}else if (counts == 3){
			locationX = 100;
			locationY = locationYusers[2] +=YSPACING;
		}else if (counts == 4){
			locationX = 120;
			locationY = locationYusers[3] +=YSPACING;
		}else if (counts == 5){
			locationX = 140;
			locationY = locationYusers[4] +=YSPACING;
		}else{
			int temp = 5 + (counts/maxValue * 200);
			/*if(temp<5){
				temp =5;
			}else if (temp > 19){
				temp =19;
			}*/
			locationX = 150 + (counts/maxValue * 200); //150 + (10 * temp);
			locationY = locationYusers[7+ temp/10 + temp%20] +=YSPACING;
			
		}
		
		
		userVertex vert = userVerts.get(username);
	    if(vert!=null){
	    	CHVJ.addVertex(vert, locationX, locationY);
	    }
		
		
		
		
		
		
	}
	
	
	/* run through the users
	 * String currentNode = nodes[i].name;
	    
	 */
	
	
	
	//
	
	//now to run through all edges on the map
	//cliqueEdgeList.put(count+"###"+j,username);
	//that is count is in CliqueVerts and need a edge to username
	
	for(Iterator<String> edgerun = cliqueEdgeList.keySet().iterator();edgerun.hasNext();){
		//CHVJ.addEdge(edgerun.next());
		String edgetoadd = edgerun.next();
		int split = edgetoadd.indexOf("###");//edgeList.put(user1+"###"+user2, true);
		int indexClique = Integer.parseInt(edgetoadd.substring(0,split));
		String usern = cliqueEdgeList.get(edgetoadd);
		
		CHVJ.addEdge(new cliqueEdgeJung(cliqueVerts.get(indexClique),userVerts.get(usern)));
	}
	//jung update
	CHVJ.showSomething(false, false);
    }//end of placing user nodes on left
	
    /**
     * Get the date as yy-mm-dd
     * 
     * @return wingui's top bar range
     */
    public final String[] getDate(){

    	return parentWingui.getDateRange_toptoolbar();
    }
	
    /**
     * get current date
     */
    public final Timestamp getDateNow(){
    	return new Timestamp(new Date().getTime());
    }    
	
	
    public final void runEnclaveClique(){
	(new Thread(new Runnable(){
		public void run(){
			BusyWindow bw = new BusyWindow("Progress","Running...",true);	
		    bw.progress(0,5);
		    bw.setVisible(true);
			
		    /******************************/
		  //  demo.start = true;
		  //  cliqueNodes = new ArrayList();
		    userNodes = new ArrayList();
		  //  lineNodes = new ArrayList();
		  //  lastCliqueNode = null;
		  //  lastUserNode = null;
		  //  lastLineNode = null;
		  //  sclique.shareLine = null;
		    ArrayList cliques = new ArrayList();
		    int type = SYSD_CLIQUE;
		    
		    bw.progress(2,5);
		    if(chooseclique2.isSelected()){
			cliques = loadclique();
			type = CHARLIE_CLIQUE;
			
		    }
		    else {
		    	cliques = loadclique2();
		    }
		    //lets remeber what the pane size was during drawing so that we can get to the correct x,y locations
		  //  PaneDuringDraw = PANE_SIZE;
		    
		    
		    showTheClique(cliques, type);
		    bw.progress(3,5);
		    showUserNodes(cliques, type);
		    bw.progress(4,5);
		    repaint();
		    currenttype = type;
	/*	    if(currentUsingClique!=null && lastUsedType != type){
		    	
		    	ArrayList tmp1 = (ArrayList)currentUsingClique.clone();
			lastUsedClique = new ArrayList();
			lastUsedClique = tmp1;
		    }
		    ArrayList tmp2 = (ArrayList)cliques.clone();
		    currentUsingClique = new ArrayList();
		    currentUsingClique = tmp2;
	*/
		    lastUsedType = type;
		    bw.progress(5,5);
		    bw.setVisible(false);
		    /******************************/
			
		}
	    })).start();
    }
	
    public final void runUserClique(final String user){
	(new Thread(new Runnable(){
		public void run(){
			BusyWindow bw = new BusyWindow("Progress","Running...",true);
		    bw.progress(0,5);
		    bw.setVisible(true);
			
		    /******************************/
		  /*  demo.start = true;
		    cliqueNodes = new ArrayList<VCNode>();*/
		    userNodes = new ArrayList();
		    /*lineNodes = new ArrayList();
		    lastCliqueNode = null;
		    lastUserNode = null;
		    lastLineNode = null;
		    sclique.shareLine = null;*/
		    //String user = (String)users.getSelectedItem();
		    
		    bw.progress(2,5);
		    String[] date = getDate();
		  
		    ArrayList<ArrayList<String>> cliques = callUserClique(user,date[0],date[1]);
		    bw.progress(3,5);
		    showTheClique(cliques,CHARLIE_CLIQUE);
		   
		    bw.progress(4,5);
		    showUserNodes(cliques, CHARLIE_CLIQUE);
		    repaint();//here is the actual redrawing done
		    bw.progress(5,5);
		    bw.setVisible(false);
		    /******************************/
			
		}
	    })).start();
    }
	
    public final void actionPerformed(ActionEvent evt){
	String arg = evt.getActionCommand();	
	boolean repaintcliques = false;
	//System.out.println("action:"+arg);
		
	if(arg.equals("Enclave Clique")){
	    runEnclaveClique();
	}
	else if(arg.equals("User Clique")){
	    runUserClique((String)parentWingui.getSelectedUser());
	}/*
	else if(arg.equals("sl")){
	    //selectedSize = 1;
	    PANE_SIZE = largePANE;
	    repaintcliques= true;
	}
	else if(arg.equals("sm")){
	    //selectedSize = 2;
	    PANE_SIZE = mediumPANE;
	    repaintcliques = true;
	}
	else if(arg.equals("ss")){
	    //selectedSize = 4;
	    PANE_SIZE = smallPANE;
	    
	   repaintcliques = true;
	}
	else if(arg.equals("clique1")){
	    int type = 1;
	    if(lastUsedClique!=null && lastUsedType!=type){
		cliqueNodes = new ArrayList();
		userNodes = new ArrayList();
		lineNodes = new ArrayList();
		lastCliqueNode = null;
		lastUserNode = null;
		lastLineNode = null;
		sclique.shareLine = null;

		showClique(lastUsedClique, type);
		showUser(lastUsedClique, type);
		repaint();

		ArrayList tmp1 = (ArrayList)lastUsedClique.clone();
		ArrayList tmp2 = (ArrayList)currentUsingClique.clone();
		currentUsingClique = new ArrayList();
		lastUsedClique = new ArrayList();
		currentUsingClique = (ArrayList)tmp1.clone();
		lastUsedClique = (ArrayList)tmp2.clone();
		lastUsedType = type;
	    }
	}
	else if(arg.equals("clique2")){
	    int type = 2;
	    if(lastUsedClique!=null && lastUsedType!=type){
		cliqueNodes = new ArrayList();
		userNodes = new ArrayList();
		lineNodes = new ArrayList();
		lastCliqueNode = null;
		lastUserNode = null;
		lastLineNode = null;
		sclique.shareLine = null;
		
		showClique(lastUsedClique, type);
		showUser(lastUsedClique, type);
		repaint();

		ArrayList tmp1 = (ArrayList)lastUsedClique.clone();
		ArrayList tmp2 = (ArrayList)currentUsingClique.clone();
		currentUsingClique = new ArrayList();
		lastUsedClique = new ArrayList();
		currentUsingClique = (ArrayList)tmp1.clone();
		lastUsedClique = (ArrayList)tmp2.clone();
		lastUsedType = type;
	    }
	}
	
	
	if(repaintcliques){
		if(currentUsingClique!=null){
		//TODO:
			cliqueNodes = new ArrayList<VCNode>();
			userNodes = new ArrayList();
			lineNodes = new ArrayList();
			lastCliqueNode = null;
			lastUserNode = null;
			lastLineNode = null;
			sclique.shareLine = null;
		 showClique(currentUsingClique, currenttype);
		 showUser(currentUsingClique, currenttype);
		 repaint();
		}
	}*/
	
	/*else if(arg.equals(HELP)){
	    System.out.println("shouldnt be here...fix help ruitine");
		
		//
		//showHelp();
	}*/
    }

    /*public void showHelp(){
	String message = EMTHelp.getVisualClique();
	
	DetailDialog dialog = new DetailDialog(null, false, HELP, message);
	
	dialog.setLocationRelativeTo(this);
	dialog.setVisible(true);

    }*/

/*    class ShowClique extends JPanel implements MouseMotionListener{
	private int shift = 60;
	private ShowUser showuser;
	private CliqueInformationScreen demo;
    private VCNode shareLine = null;

	public ShowClique(){
	    addMouseListener(new MouseAdapter(){  
		    public void mousePressed(MouseEvent evt){  	}
		    public void mouseClicked(MouseEvent evt){
			int x = evt.getX();
			int y = evt.getY();
			//TODO: show clique
			//System.out.println("Pane:" + PANE_SIZE + " drawsize " + PaneDuringDraw);
			//System.out.println("x:"+x+" y:"+y);
			
			//will translate the x,y to correct coordinates.
			if(PANE_SIZE != PaneDuringDraw){
				
				if(PANE_SIZE < PaneDuringDraw){
					int factor = PaneDuringDraw/PANE_SIZE;
					//System.out.println("1  "+factor + " " + PANE_SIZE + " " + PaneDuringDraw);
					x = (int)(x * factor);
					y = (int)(y * factor);
				}
				else{
					int factor = PANE_SIZE/PaneDuringDraw;
					//System.out.println("2  "+factor + " " + PANE_SIZE + " " + PaneDuringDraw);
					x  =(int) (x /factor);
					y = (int)(y / factor);//y * (PaneDuringDraw/PANE_SIZE);
						
				}
				
			}
			System.out.println("x:"+x+" y:"+y);
			
			
			
			if(cliqueNodes!=null){
			    int node = checkNode(x,y);
//			    System.out.println("found node: "+ node);
			    int line = online(x,y);
			    if(node>=0){//check the point first
				//shareLine = null;
			    	updateNode(node);
			    	draw();
			    	demo.displayClique(lastCliqueNode);
			    }
			    else if(line>=0){
				
			    	shareLine = (VCNode)lineNodes.get(line);
			    	draw();
			    	demo.lineInfo(shareLine);
			    }
			}
		    }
		});
	    addMouseMotionListener(this);
	    int size = PANE_SIZE * selectedSize *4;
	    setPreferredSize(new Dimension(size+shift,size+shift));
	}

	public final void share(final Graphics g,final int x1, final int y1, final int x2, final int y2){
	    g.setColor(Color.black);
	    g.drawLine(x1,y1,x2,y2);
	    g.fillOval(x1-4,y1-4,VisualClique.bigPoint,VisualClique.bigPoint);
	    g.fillOval(x2-4,y2-4,VisualClique.bigPoint,VisualClique.bigPoint);

	    int x = VisualClique.bigPoint;
	    int y = VisualClique.bigPoint;
	    
	      if(shareLine.shareuser!=null){
	      for(int i=0;i<shareLine.shareuser.size();i++){
	      g.drawString((String)shareLine.shareuser.get(i),x,y);
	      y+=15;
	      }
	      }
	    
	}

	public void updateNode(final int toUpdate){	    

	    //update the clique nodes
	    VCNode n = (VCNode)cliqueNodes.get(toUpdate);
	    cliqueNodes.remove(toUpdate);
	    n.size = VisualClique.bigPoint;
	    n.color = Color.red;
	    n.isSelected = true;
	    //cliqueNodes.add(n);
	    //if(toUpdate>=cliqueNodes.size())
	    //System.out.println(toUpdate+" "+cliqueNodes.size());
	    cliqueNodes.add(toUpdate,n);

	    //update the last selected clique
	    if(lastCliqueNode!=null){
		int ind = cliqueNodes.indexOf(lastCliqueNode);
		cliqueNodes.remove(lastCliqueNode);
		lastCliqueNode.color = Color.blue;
		lastCliqueNode.size = VisualClique.smallPoint;
		lastCliqueNode.isSelected = false;
		//cliqueNodes.add(lastCliqueNode);
		cliqueNodes.add(ind,lastCliqueNode);
	    }
	    lastCliqueNode = n;

	    //clean up previous use
	    for(int i=0;i<userNodes.size();i++){
		VCNode usern = (VCNode)userNodes.get(i);
		userNodes.remove(i);
		usern.isSelected = false;
		usern.color = Color.black;
		usern.size = VisualClique.smallPoint;
		userNodes.add(i,usern);
	    }

	    //update the user nodes
	    lastUserNode = new ArrayList();
	    for(int i=0;i<n.users.size();i++){
		String user = (String)n.users.get(i);
		for(int j=0;j<userNodes.size();j++){
		    VCNode usern = (VCNode)userNodes.get(j);
		    if(usern.name.equals(user)){//if selected
			lastUserNode.add(user);
			userNodes.remove(j);
			usern.isSelected = true;
			usern.color = Color.red;
			usern.size = VisualClique.bigPoint;
			userNodes.add(j,usern);
			//break;
		    }
		    else if(usern.isSelected && !lastUserNode.contains(usern.name)){//not selected
			userNodes.remove(j);
			usern.isSelected = false;
			usern.color = Color.black;
			usern.size = VisualClique.smallPoint;
			userNodes.add(j,usern);
		    }
		}//for(int j=0;j<userNodes.size();j++)
	    }// for(int i=0;i<n.users.size();i++)

	}
		
	public void paintComponent(Graphics g){
	    int size = PANE_SIZE * selectedSize *4;
	    //setPreferredSize(new Dimension(size+shift,size+shift));
	    //System.out.println("repaint s");
	    g.setColor(Color.white);
	    // int size = PANE_SIZE * selectedSize;
	    g.fillRect(0,0,size+shift,size+shift);
	    if(cliqueNodes!=null && userNodes!=null)
	    	drawCliques(g);
	}
		
	public void draw(){
	    super.repaint();
	    showuser.callrepaint();
	}
	
	public void callrepaint(){
	    super.repaint();
	}
		
	public final void drawCliques(final Graphics g){
	    lines(g);
	    //points(g);
	    if(shareLine!=null)share(g,shareLine.xPos,shareLine.yPos,shareLine.xPos2,shareLine.yPos2);
	    points(g);
	}

	public final void lines(Graphics g){
	    lineNodes.clear();
	    for(int i=0;i<cliqueNodes.size();i++){
		for(int j=i+1;j<cliqueNodes.size();j++){
		    VCNode n1 = (VCNode)cliqueNodes.get(i);
		    VCNode n2 = (VCNode)cliqueNodes.get(j);
		    VCNode line = VCNodeOperator.computeWeight(n1,n2);
		    //white means they don't share users
		    if(line!=null){
			g.setColor(line.color);
			g.drawLine(n1.xPos+4,n1.yPos+4,n2.xPos+4,n2.yPos+4);
			lineNodes.add(line);
		    }
		}
	    }
	}

	public final void points(Graphics g){
	    for(int i=0;i<cliqueNodes.size();i++){
		VCNode n = (VCNode)cliqueNodes.get(i);
		g.setColor(n.color);
		if(shareLine!=null){
		    if( !((shareLine.xPos==n.xPos) || (shareLine.xPos2==n.xPos)
			  || (shareLine.yPos==n.yPos) || (shareLine.yPos2==n.yPos))){
			//g.fillOval(n.xPos,n.yPos,n.size,n.size);
			if(n.users.size()>2)
			    drawpolygon(g, n.size, n.users.size(), n.color, n.xPos, n.yPos);
			else g.fillOval(n.xPos,n.yPos,n.size,n.size);
		    }
		}
		else{
		    //g.fillOval(n.xPos,n.yPos,n.size,n.size);
		    if(n.users.size()>2)
			drawpolygon(g, n.size, n.users.size(), n.color, n.xPos, n.yPos);
		    else g.fillOval(n.xPos,n.yPos,n.size,n.size);
		}
		g.setColor(Color.black);
		g.drawString(String.valueOf(i+1),n.xPos+10,n.yPos+5);
	    }
	}

	public final void drawpolygon(Graphics g, int size, int points, Color color, int x, int y){
	    int[] xarray = new int[points];
	    int[] yarray = new int[points];
	    double radius = ((double)size/2);
	    double xcenter = (double)x + radius;
	    double ycenter = (double)y + radius;

	    double index = 0;
	    double interval = (2.0*Math.PI)/(double)points;
	    for(int i=0;i<points;i++){
		xarray[i] = (int)(xcenter + ((double)radius * Math.cos(index)));
		yarray[i] = (int)(ycenter + ((double)radius * Math.sin(index)));
		index+=interval;
	    }

	    g.setColor(color);
	    g.fillPolygon(xarray, yarray, points);
	}

	public final int checkNode(final int x, final int y){
	    int selected = -1;
	    
	    for(int i=0;i<cliqueNodes.size();i++){
		VCNode n = (VCNode)cliqueNodes.get(i);
		//System.out.println(n.xPos +" "+n.yPos);
		if(x>=n.xPos && x<=n.xPos+n.size
		   && y>=n.yPos && y<=n.yPos+n.size){
			
		    selected = i;
		    break;
		}
	    }
	    return selected;
	}

	public final int online(final int x, final int y){
	    //int index = -1;
	    if(lineNodes!=null){
		for(int i=0;i<lineNodes.size();i++){
		    VCNode n = (VCNode)lineNodes.get(i);
		    if(isOnline((double)n.xPos,(double)n.yPos,(double)n.xPos2,(double)n.yPos2,(double)x,(double)y, 3))return i;
		}
	    }
	    return -1;
	}

	*//**
	 * Simple check if point 3 (x3,y3) one the line
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param accuracy
	 * @return
	 *//*
	public final boolean isOnline(final double x1, final double y1, final double x2, final double y2, final double x3,final  double y3,final int accuracy){
	    //test is x3,y3 is between x1,y1 x2,y2
	    
	    for(int i=0;i<accuracy;i++){

		double testx1 = x1+(double)i;
		double testx2 = x2+(double)i;
		double testy1 = y1;
		double testy2 = y2;

		if(testx1==testx2 && testx2==x3){
		    if(testy1>y3 && y3>testy2)return true;
		    else if(testy1<y3 && y3<testy2)return true;
		}
		else if(testx1!=x3 && testx2!=x3){
		    double slope1 = (testy1-y3)/(testx1-x3);
		    double slope2 = (y3-testy2)/(x3-testx2);
		    if(Math.abs(slope1-slope2)<0.1)return true;
		}

		testx1 = x1;
		testx2 = x2;
		testy1 = y1+(double)i;
		testy2 = y2+(double)i;
		if(testy1==testy2 && testy2==y3){
		    
		    if(testx1>x3 && x3>testx2)return true;
		    else if(testx1<x3 && x3<testx2)return true;
		}
		else if(testx1!=x3 && testx2!=x3){
		    double slope1 = (testy1-y3)/(testx1-x3);
		    double slope2 = (y3-testy2)/(x3-testx2);
		    if(Math.abs(slope1-slope2)<0.1)return true;
		}
	    }//for

	    for(int i=0;i<accuracy;i++){

		double testx1 = x1-(double)i;
		double testx2 = x2-(double)i;
		double testy1 = y1;
		double testy2 = y2;

		if(testx1==testx2 && testx2==x3){
		    if(testy1>y3 && y3>testy2)return true;
		    else if(testy1<y3 && y3<testy2)return true;
		}
		else if(testx1!=x3 && testx2!=x3){
		    double slope1 = (testy1-y3)/(testx1-x3);
		    double slope2 = (y3-testy2)/(x3-testx2);
		    if(Math.abs(slope1-slope2)<0.1)return true;
		}

		testx1 = x1;
		testx2 = x2;
		testy1 = y1-(double)i;
		testy2 = y2-(double)i;
		if(testy1==testy2 && testy2==y3){
		    
		    if(testx1>x3 && x3>testx2)return true;
		    else if(testx1<x3 && x3<testx2)return true;
		}
		else if(testx1!=x3 && testx2!=x3){
		    double slope1 = (testy1-y3)/(testx1-x3);
		    double slope2 = (y3-testy2)/(x3-testx2);
		    if(Math.abs(slope1-slope2)<0.1)return true;
		}
	    }//for
	    
	    return false;
	}
	
	  public final void popupUsers(){
	  if(cliquedetail!=null)cliquedetail.setVisible(false);
	  cliquedetail = new CliqueDetail(lastCliqueNode);
	  cliquedetail.setTitle("Users");
	  cliquedetail.pack();
	  cliquedetail.setSize(200,150);
	  //cd.setLocationRelativeTo(null);
	  cliquedetail.setLocation(30,30);
	  cliquedetail.setVisible(true);
	  }
	
	public void mouseMoved(MouseEvent evt){ }
	public void mouseDragged(MouseEvent evt){ }
		
    }*/
	
/*    class ShowUser extends JPanel implements MouseMotionListener{
	//private ShowClique showclique;
	//private CliqueInformationScreen demo;
	private int shift = 60;
		
	public ShowUser(){
	    addMouseListener(new MouseAdapter(){  
		    public void mousePressed(MouseEvent evt){  	}
		    public void mouseClicked(MouseEvent evt){
			int x = evt.getX();
			int y = evt.getY();

		    }
		});
	    addMouseMotionListener(this);
	    int size = PANE_SIZE * selectedSize *4;
	    setPreferredSize(new Dimension(size+shift,size+shift));
	    
	}
		
	public final void paintComponent(final Graphics g){
	    int size = 3200;//PANE_SIZE * selectedSize*4;
	    //setPreferredSize(new Dimension(size+shift,size+shift));
	    //System.out.println("repaint u");
	    g.setColor(Color.white);
	    //int size = PANE_SIZE * selectedSize;
	    g.fillRect(0,0,size+shift,size+shift);
	    if(cliqueNodes!=null && userNodes!=null)drawUsers(g);
	}

	public final void drawUsers(Graphics g){
	 //   lines(g);
	    userpoints(g);
	   // cliquepoints(g);
	}

	public final void draw(){
	    super.repaint();
	    //showclique.callrepaint();
	}
	
	public final void callrepaint(){
	    super.repaint();
	}

	public final void lines(final Graphics g){
	    for(int i=0;i<userNodes.size();i++){
		VCNode n = (VCNode)userNodes.get(i);
		for(int j=0;j<n.clique.size();j++){
		    Integer c = (Integer)n.clique.get(j);
		    int index = c.intValue();
		    VCNode clique = (VCNode)cliqueNodes.get(index);
		    g.setColor(VCNodeOperator.getColor(n.count));
		    g.drawLine(n.xPos+4,n.yPos+4,clique.xPos2+4,clique.yPos2+4);
		}
	    }
	}

	public final void userpoints(final Graphics g){
	    for(int i=0;i<userNodes.size();i++){
		VCNode n = (VCNode)userNodes.get(i);
		g.setColor(n.color);
		g.fillOval(n.xPos,n.yPos,n.size,n.size);
		g.setColor(Color.blue);
		if(n.isSelected){
		    int xpos = n.xPos-10;
		    //if(xpos+100 > PANE_SIZE)xpos-=100;
		    StringTokenizer st = new StringTokenizer(n.name, ".");
		    String tmpname = n.name;
		    if(st.hasMoreTokens()) {
			tmpname = st.nextToken();
		    }

		    g.drawString(tmpname,xpos,n.yPos-10);
		}
	    }
	}

	public final void cliquepoints(final Graphics g){
	    for(int i=0;i<cliqueNodes.size();i++){
		VCNode n = (VCNode)cliqueNodes.get(i);
		g.setColor(n.color);
		if(n.users.size()>2){
		    drawpolygon(g, n.size, n.users.size(), n.color, n.xPos2, n.yPos2);
		}
		else{
		    g.fillOval(n.xPos2,n.yPos2,n.size,n.size);
		}
		g.setColor(Color.black);
		g.drawString(String.valueOf(i+1),n.xPos2-15,n.yPos2+5);
	    }
	}

	public final void drawpolygon(final Graphics g, final int size, final int points, final Color color, final int x, final int y){
	    int[] xarray = new int[points];
	    int[] yarray = new int[points];
	    double radius = ((double)size/2);
	    double xcenter = (double)x + radius;
	    double ycenter = (double)y + radius;

	    double index = 0;
	    double interval = (2.0*Math.PI)/(double)points;
	    for(int i=0;i<points;i++){
		xarray[i] = (int)(xcenter + ((double)radius * Math.cos(index)));
		yarray[i] = (int)(ycenter + ((double)radius * Math.sin(index)));
		index+=interval;
	    }

	    g.setColor(color);
	    g.fillPolygon(xarray, yarray, points);
	}

	public void mouseMoved(MouseEvent evt){ }
	public void mouseDragged(MouseEvent evt){ }
    }*/

//    class CliqueInformationScreen extends JPanel{
//    	ShowClique showclique;
//    	ShowUser showuser;
//    	
//	String[] userMsg;
//	String[] commonWordMsg;
//	ArrayList demoFigure;
//	String cliqueMsg;
//	String username;
//	ArrayList shareLines;
//
//	int displayRange = 4;
//
//	CliqueDetail cliquedetail1;
//	CliqueDetail cliquedetail2;
//	CliqueDetail cliquedetail3;
//	
//	boolean start = true;
//
//	public CliqueInformationScreen(){
//	    //int size = PANE_SIZE * selectedSize;
//	    setPreferredSize(new Dimension(900,400));
//	}
//
//	public final void paintComponent(Graphics g){
//	    g.setColor(Color.white);
//	  
//	    Rectangle rec = this.getBounds();
//	    
//	    g.fillRect(0,0,rec.width,rec.height);
//	    
//
//	    if(start){
//	    	ArrayList[] nodes = getFirstDemoPoints();
//	    	for(int i=0;i<nodes.length;i++){
//		    points(g, nodes[i]);
//		    lines(g, nodes[i]);
//	    	}
//	    	g.setColor(Color.blue);
//	    	g.drawString("2 Clique",80,130);
//	    	g.drawString("3 Clique",200,130);
//	    	g.drawString("4 Clique",320,130);
//	    	g.drawString("5 Clique",440,130);
//	    	g.drawString("6 Clique",560,130);
//	    	g.drawString("7 Clique",680,130);
//		demoFigure = new ArrayList();
//		shareLines = new ArrayList();
//	    }
//	    else{
//		g.setColor(Color.black);
//		g.drawLine(685,0,685,400);
//		g.drawLine(690,0,690,400);
//
//		if(demoFigure!=null && demoFigure.size()>0){
//		    points(g, demoFigure);
//		    lines(g, demoFigure);
//			
//		    g.setColor(Color.blue);
//		    g.drawString(cliqueMsg,80,130);
//		    int xPos = 170;
//		    int yPos = 10;
//		    int yInterval = 15;
//		    if(username!=null){
//			String[] str1 = username.split("@");
//			String str2 = str1[0];
//			g.drawString(str2+"'s freinds:", xPos, yPos);
//		    }
//		    else{
//			g.drawString("Clique Users:", xPos, yPos);
//		    }
//		    yPos += yInterval;
//		    for(int i=0;i<userMsg.length;i++){
//				//System.out.println("i:"+i);
//				/*
//				  if(i==displayRange){
//				  g.drawString("More......", xPos, yPos);
//				  break;
//				  }
//				*/
//			g.drawString(userMsg[i], xPos, yPos);
//			yPos += yInterval;
//		    }
//		    g.setColor(Color.white);
//		    g.fillRect(420,0,260,150);
//		    g.setColor(Color.red);
//		    xPos = 420;
//		    yPos = 10;
//		    g.drawString("Common Subject Words:", xPos, yPos);
//		    yPos += yInterval;
//		    for(int i=0;i<commonWordMsg.length;i++){
//		    	/*
//			  if(i==displayRange){
//			  g.drawString("More......", xPos, yPos);
//			  break;
//			  }
//			*/
//			g.drawString(commonWordMsg[i], xPos, yPos);
//			yPos += yInterval;
//		    }
//		    
//		}//if(demoFigure!=null && demoFigure.size()>0)
//
//		if(shareLines!=null && shareLines.size()>0){
//		    int yInterval = 15;
//		    g.setColor(Color.black);
//		    int xPos = 700;
//		    int yPos = 10;
//		    g.drawString("Sharing Users:", xPos, yPos);
//		    yPos += yInterval;
//		    for(int i=0;i<shareLines.size();i++){
//		    	/*
//			  if(i==displayRange){
//			  g.drawString("More......", xPos, yPos);
//			  break;
//			  }
//			*/
//			g.drawString((String)shareLines.get(i), xPos, yPos);
//			yPos += yInterval;
//		    }
//		}
//	    }
//	}
//	
//	public final void displayClique(final VCNode node){
//	    start = false;
//	    int size = node.users.size();
//	    demoFigure = getDemoClique(size);
//	    userMsg = new String[size];
//	    commonWordMsg = new String[node.commonwords.size()];
//	    cliqueMsg = size + " Clique";
//	    username = node.name;
//		
//	    for(int i=0;i<size;i++){
//		userMsg[i] = (String)node.users.get(i);
//	    }
//
//	    for(int i=0;i<node.commonwords.size();i++){
//		commonWordMsg[i] = (String)node.commonwords.get(i);
//		//System.out.println("words:"+commonWordMsg[i]);
//	    }
//
//	    super.repaint();
//	    /*
//	      if(userMsg.length>displayRange){
//	      popupUsers("User:",200,100,200,50,lastCliqueNode.users,1);
//	      }
//	      else if(cliquedetail1!=null && cliquedetail1.isVisible()){
//	      cliquedetail1.setVisible(false);
//	      }
//
//	      if(commonWordMsg.length>displayRange){
//	      popupUsers("Common Subject Words:",250,100,500,50,lastCliqueNode.commonwords,2);
//	      }
//	      else if(cliquedetail2!=null && cliquedetail2.isVisible()){
//	      cliquedetail2.setVisible(false);
//	      }
//	    */
//	}
//
//	public final ArrayList getDemoClique(final int type){
//		ArrayList nodes = new ArrayList();
//	    int xbase = 50;
//	    int ybase = 10;
//	    int radius = 30;
//	    int centerx = xbase+radius;
//	    int centery = ybase+radius;
//
//	    double index = 0;
//	    double interval = (2.0*Math.PI)/(double)type;
//	    for(int i=0;i<type;i++){
//		double x = centerx + ((double)radius * Math.cos(index));
//		double y = centery + ((double)radius * Math.sin(index));
//		VCNode n = new VCNode(Color.black,(int)x,(int)y,7,i);
//		nodes.add(n);
//		index+=interval;
//	    }
//
//	    return nodes;
//	}
//
//	public final ArrayList[] getFirstDemoPoints(){
//		ArrayList[] nodes = new ArrayList[6];
//	    for(int i=0;i<nodes.length;i++)
//		nodes[i] = new ArrayList();
//
//	    int xbase = 50;
//	    int ybase = 10;
//	    int radius = 30;
//	    int interval = 120;
//
//	    int centerx = xbase+radius;
//	    int centery = ybase+radius;
//	    for(int i=2;i<8;i++){
//		double index = 0;
//		double interval2 = (2.0*Math.PI)/(double)i;
//		for(int j=0;j<i;j++){
//		    double x = centerx + ((double)radius * Math.cos(index));
//		    double y = centery + ((double)radius * Math.sin(index));
//		    VCNode n = new VCNode(Color.black,(int)x,(int)y,7,i);
//		    nodes[i-2].add(n);
//		    index+=interval2;
//		}
//		centerx+=interval;
//	    }
//	    return nodes;
//	}
//
//	public final void lineInfo(VCNode line){
//	    start = false;
//	    shareLines = new ArrayList();
//	    for(int i=0;i<line.shareuser.size();i++){
//		shareLines.add(line.shareuser.get(i));
//	    }
//	    super.repaint();
//	    /*
//	      if(shareLines.size()>displayRange){
//	      popupUsers("Share Users:",200,100,600,50,shareLines,3);
//	      }
//	      else if(cliquedetail3!=null && cliquedetail3.isVisible()){
//	      cliquedetail3.setVisible(false);
//	      }
//	    */
//	}
//
//	public final void lines(final Graphics g, final ArrayList node){
//	    g.setColor(Color.black);
//	    for(int i=0;i<node.size();i++){
//		for(int j=i+1;j<node.size();j++){
//		    VCNode n1 = (VCNode)node.get(i);
//		    VCNode n2 = (VCNode)node.get(j);
//		    g.drawLine(n1.xPos+4,n1.yPos+4,n2.xPos+4,n2.yPos+4);
//		}
//	    }
//	}
//
//	public final void points(Graphics g, ArrayList node){
//	    g.setColor(Color.magenta);
//	    for(int i=0;i<node.size();i++){
//		VCNode n = (VCNode)node.get(i);
//		g.fillOval(n.xPos,n.yPos,n.size,n.size);
//	    }
//	}
//	
//	public final  void popupUsers(final String msg, final int xsize, final int ysize,
//				      final int xpos, final int ypos, final ArrayList value, final int type){
//	    
//	    if(type == 1){
//		if(cliquedetail1!=null)cliquedetail1.setVisible(false);
//		cliquedetail1 = new CliqueDetail(value);
//		cliquedetail1.setTitle(msg);
//		cliquedetail1.pack();
//		cliquedetail1.setSize(xsize,ysize);
//		cliquedetail1.setLocation(xpos, ypos);
//		cliquedetail1.setVisible(true);
//	    }
//	    else if(type == 2){
//		if(cliquedetail2!=null)cliquedetail2.setVisible(false);
//		cliquedetail2 = new CliqueDetail(value);
//		cliquedetail2.setTitle(msg);
//		cliquedetail2.pack();
//		cliquedetail2.setSize(xsize,ysize);
//		cliquedetail2.setLocation(xpos, ypos);
//		cliquedetail2.setVisible(true);
//	    }
//	    else if(type == 3){
//		if(cliquedetail3!=null)cliquedetail3.setVisible(false);
//		cliquedetail3 = new CliqueDetail(value);
//		cliquedetail3.setTitle(msg);
//		cliquedetail3.pack();
//		cliquedetail3.setSize(xsize,ysize);
//		cliquedetail3.setLocation(xpos, ypos);
//		cliquedetail3.setVisible(true);
//	    }
//	}
//    }
//}

/*class CliqueDetail extends JFrame{
    public CliqueDetail(VCNode n){
	CliqueDetailPane cd = new CliqueDetailPane(n);
	Container con = getContentPane();
	con.add(cd, "Center");
    }
    
    public CliqueDetail(ArrayList msg){
	CliqueDetailPane cd = new CliqueDetailPane(msg);
	Container con = getContentPane();
	con.add(cd, "Center");
    }
}*/

/*class CliqueDetailPane extends JPanel{  

	public CliqueDetailPane(VCNode node){
	    JPanel pan = new JPanel();
	    int size = node.users.size();
	    pan.setLayout(new GridLayout(size,1));

	    for(int i=0;i<size;i++){
		pan.add(new JLabel((String)node.users.get(i)));
	    }
	    
	    JScrollPane sp = new JScrollPane(pan);
	    setLayout(new BorderLayout());
	    add(sp,BorderLayout.CENTER);
	}

	public CliqueDetailPane(ArrayList msg){
	    JPanel pan = new JPanel();
	    int size = msg.size();
	    pan.setLayout(new GridLayout(size,1));

	    for(int i=0;i<size;i++){
		pan.add(new JLabel((String)msg.get(i)));
	    }
	    
	    JScrollPane sp = new JScrollPane(pan);
	    setLayout(new BorderLayout());
	    add(sp,BorderLayout.CENTER);
	}
   
}*/
}








