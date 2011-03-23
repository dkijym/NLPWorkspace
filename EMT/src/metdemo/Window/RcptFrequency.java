/**
for the tab "Recipient Frequency" in metdemo client
Analyzes Recipient Frequency Patterns per User (fin)
Olivier Nimeskern, 07/15/2002
Wei-Jen Li, Fall 2002
Yen-Jong Tu, Fall 2002

There are five 'pages' in this class, Profile, Chi Square,
, CS+cliques, Hellinger Distance, and test.
Press the buttons to switch the pages.
The start up page is Profile
Hellinger Distance : to compute hellinger distance
in-bound: 'sender' is the user, analyze the recipients
out-bound: 'recipient' is the user, analyze the senders
out-bound with attachment: compute the emails with attachment

The button 'Test' is for virus simulation.

Comments are added by Wei-Jen.
 */

/**
data structures stuff oct 2002
for memory and time speed. 
 */

/** memory org for speedup oct 2003 sh
-moved scope
-no need for blockall/unblokc all wingui enabled does this already
-more static strings for recurring strings....save tons of room and time
-more collapsing of gloabl variables
*/

/** completly redawn layout - shlomo hershkop dec 2003. */
package metdemo.Window;


import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;

import metdemo.winGui;
import metdemo.AlertTools.AlertPoint;
import metdemo.CliqueTools.CliqueFinder;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.MLStatistics;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.HellingerConf;
import metdemo.Tools.HellingerResult;
import metdemo.Tools.HellingerTest;
import metdemo.Tools.Utils;
import metdemo.dataStructures.SimpleHashtable;
import metdemo.dataStructures.userShortProfile;

import java.lang.String;
import java.sql.SQLException;
import chapman.graphics.JPlot2D;
//import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Vector;
// TODO: Auto-generated Javadoc
//import java.util.ArrayList;


/** this class displays the frequency of either incoming/outgogn emails.<P>
 * Original author: Oliver
 * weijen fixed and cleaned things up added virus testing etc
 * shlomo enhanced by optimization and cleanup
 */


public class RcptFrequency extends JScrollPane implements MouseListener{
   
   
    /** The Constant table. */
    private final static String table="email";
    
    /** The Constant MSG_TESRDIFF. */
    private final static String MSG_TESRDIFF = "Testing Range Sender List        Diff Threshold:  ";
    
    /** The Constant MSG_TERRDIFF. */
    private final static String MSG_TERRDIFF = "Testing Range Rcpt List        Diff Threshold:  ";
    
    /** The Constant MSG_TRRSL. */
    private final static String MSG_TRRSL = "Training Range Sender List";
    
    /** The Constant MSG_SADD. */
    private final static String MSG_SADD = "sender address";
    
    /** The Constant RL. */
    private final static String RL = "Recipient list";
    
    /** The Constant MSG_TERRL. */
    private final static String MSG_TERRL = "Training Range Recipient List";
    
    /** The Constant MSG_RA. */
    private final static String MSG_RA = "recipient address";
    
    /** The Constant MSG_FREQ. */
    private final static String MSG_FREQ = "frequency";
    
    /** The Constant MSG_DIFF. */
    private final static String MSG_DIFF = "diff";
    
    /** The Constant MSG_SNDR. */
    private final static String MSG_SNDR = "SENDER";
    
    /** The Constant MSG_SFH. */
    private final static String MSG_SFH = "Sender Frequency Histogram";
    
    /** The Constant MSG_SFIP. */
    private final static String MSG_SFIP = "sender frequency in percent";
    
    /** The Constant MSG_EPR. */
    private final static String MSG_EPR = "each point is a recipient";
    
    /** The Constant MSG_RFIP. */
    private final static String MSG_RFIP = "recipient frequency in percent";
    
    /** The Constant MSG_RFH. */
    private final static String MSG_RFH = "Recipient Frequency Histogram";
    
    /** The Constant MSG_EPS. */
    private final static String MSG_EPS = "each point is a sender";
    
    /** The bound arg. */
    private String boundArg="";
    
    /** The m_alias filename. */
    private String m_aliasFilename;
    
    /** The m_jdbc. */
    private EMTDatabaseConnection m_jdbc;
    
    /** The m_panel test plot. */
    private JPanel m_panel,m_panel1,m_panel2, m_panelTestPlot;

    //private JComboBox m_comboUsers; //the combo box of users
    /** The m_hs users. */
    private HashSet m_hsUsers= new HashSet();
    
    /** The m_win gui. */
    winGui m_winGui; //reference a copy of the wingui so can pass args.
	
    /** m_plot11, m_plot12, and m_plot13 are for page 'Profile' m_plot21 and m_plot22 are for page 'ChiSquare' and 'CS+cliques'  m_plot11: 'Total Recipient/Sender Address List size over time' m_plot12: '# distinct rcpt/sender attach per email blocks' m_plot13: 'Recipient/Sender Frequency Histogram' m_plot21: 'Training Recipient/Sender Frequency Histogram' m_plot22: 'Testing Recipient/Sender Frequency Histogram'. */
    private JPlot2D m_plot11,m_plot12,m_plot13,m_plot21,m_plot22;
    
    /** The m_ al sequence5. */
    private double m_ALSequence[], m_ALSequence2[], m_ALSequence3[], m_ALSequence5[];//some arrays for the results
    
    /** The dsize. */
    private int dsize =0;//current data size for fixed arrays
    
    /** The hellingersize. */
    private int hellingersize = 0;//for hellinger distance
    //Vectors for calculating Frequency
    /** The m_ al dist3. */
    private Vector m_ALDist1,m_ALDist2,m_ALDist3;
    
    /** The m_ al dist. */
    private Rcpt m_ALDist[];  //save space
    
    /** The m_enum clique list. */
    private Vector m_enumCliqueList = new Vector();
    
    /** The m_user clique member. */
    private Vector  m_userCliqueMember = new Vector();
    //tables for different 'pages'
    /** The m_rcpt model3. */
    private DefaultTableModel m_rcptModel,m_rcptModel1,m_rcptModel2, m_rcptModel3;
    
    /** The m_rcpt freq2. */
    private JTable m_rcptFreq,m_rcptFreq1,m_rcptFreq2;
    
    /** The m_pan threshold1. */
    private JPanel m_panThreshold1;
    
    /** The m_diff column renderer2. */
    private DefaultTableCellRenderer m_diffColumnRenderer, m_diffColumnRenderer2;
    
    /** The m_gridbag3. */
    private GridBagLayout m_gridbag,m_gridbag1,m_gridbag2, m_gridbag3;
    
    /** The m_constraints3. */
    private GridBagConstraints m_constraints,m_constraints1,m_constraints2, m_constraints3;
    
    /** The hellinger size text. */
    private JTextField m_min1, m_max1, m_min2,m_max2,m_threshold,m_threshold2, hellingerSizeText;
	
   
    /** The m_lab ks. */
    private JLabel m_labRecords,m_labChisquare,m_labDegree,m_labRcpt1a, m_labKS; 
    
    /** The Constant m_labRcpt1b. */
    static final JLabel	m_labRcpt1b = new JLabel("Training Range Rcpt List       Clique Threshold: ");
    /*
      m_Button is for tracking the current page
      m_Button=0, profile
      m_Button=1, chi square
      m_Button=2, chi+clique
      m_Button=3, hellinger
      m_Button=4, Test
    */
    /** The PROFILE. */
    private final int PROFILE = 0;
    
    /** The CH i_ square. */
    private final int CHI_SQUARE = 1;
    
    /** The CH i_ clique. */
    private final int CHI_CLIQUE = 2;
    
    /** The HELLINGER. */
    private final int HELLINGER = 3;
    
    /** The DUMM y_ test. */
    private final int DUMMY_TEST = 4;
    
    /** The m_thres2. */
    private int m_Button,m_thres2;
    
    /** The m_thres. */
    private double m_thres;
    //remember the number of records of selected user
    /** The m_nb records. */
    private Integer m_nbRecords = new Integer(0);
    //private long before,after;//test the runing time
   // private BusyWindow bw;
    /** The working. */
    private BusyWindow working = new BusyWindow("RCPTFreq","calculating",true);
	
    //for distinct user, 20 or 50
    /** The SPA n2. */
    private final int SPAN2=20;
    
    /** The SPA n3. */
    private final int SPAN3=50;

    /** The SPA n_ av g2. */
    private final int SPAN_AVG2=50;
    
    /** The SPA n_ av g3. */
    private final int SPAN_AVG3=100;
    
    /** The SPA n_ ma x4. */
    private final int SPAN_MAX4=5000;
    
    /** The CH i_ min. */
    private final int CHI_MIN=100;
    
    /** The CH i_ default. */
    private final int CHI_DEFAULT=200;
    
    /** The CH i_ defaul t2. */
    private final int CHI_DEFAULT2=1000;
    
    /** The CLIQU e_ share. */
    private final double CLIQUE_SHARE=0.6;
    //private final int DEFAULT_SIZE = 100;//for hellinger distance
	
       //for hellinger distance
    /** The hellinger distance. */
    double[] hellingerDistance;
    
    /** The dummy h distance. */
    double[] dummyHDistance;
    
    /** The real dummy. */
    int[] realDummy;//dummy test
    //the threshold block size of dummy test 
    /** The S d_ thr e_ size. */
    final private int SD_THRE_SIZE = 50;
	
    //the boolean for inbound/outbound
    /** The isoutbound. */
    private boolean isoutbound = true;
    
    /** The withatt. */
    private boolean withatt = false;//with attachment or not
	
    
    //sometimes mysql is not fast enough, it may return empty data
    /** The empty set. */
    private boolean emptySet = false;
	
    /*
      test whether the user press "Chi Square" (true)
      or "CS + cliques" (false)
    */
    /** The Chi or csc. */
    private boolean ChiOrCSC = true;
    
    /** The lab rcpt2. */
    private JLabel labRcpt, labRcpt2;
    //if outbound, it's MSG_RA,
    //if inbound, it's MSG_SADD
    /** The Ror saddress. */
    private String RorSaddress = MSG_RA;
    //for virus simulation
    /** The original hell. */
    private Vector originalHell;
    
    /** The AL d_ mode l_1. */
    private final int ALD_MODEL_1 = 1;
    
    /** The AL d_ mode l_2. */
    private final int ALD_MODEL_2 = 2;
    
    /** The tmp hellinger list. */
    private Vector tmpHellingerList;
    //private md5CellRenderer md5Renderer;
    /** The clipboard. */
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    /** The attachment. */
    private JRadioButton outbound, inbound, attachment;
    
    /** The bg. */
    private ButtonGroup bg;
    
    /** The haveclique. */
    private boolean haveclique = false;
    
    /** The startup. */
    private boolean startup = true;
    
    /** The test button. */
    private JButton m_buttonProfile, m_buttonChisquare,m_buttonChisquare2
	,hellingerButton, m_buttonRun, testButton;

    /**
     * Constructor to set up initial rcpt freq window.
     *
     * @param jdbcView is the handle to the underlying database
     * @param aliasFilename is used by the clique finding algorithm
     * @param theWinGui the the win gui
     * @throws Exception the exception
     */


    public RcptFrequency(final EMTDatabaseConnection jdbcView, final String aliasFilename, final winGui theWinGui ) throws Exception
    {


	if( jdbcView==null)
	    {  
		throw new NullPointerException("Problem in addresslist Null db reference");  
	    }

	m_winGui = theWinGui;
	m_jdbc = jdbcView;
	m_aliasFilename = aliasFilename;
	//m_winGui.setUserSelectedIndex(4890);
	//m_comboUsers = new JComboBox();
	//m_comboUsers.setPreferredSize(new Dimension(225,25));//not sure baout this.
	
	
	m_panel = new JPanel();//the main panel
	m_gridbag = new GridBagLayout();
	m_constraints = new GridBagConstraints();
	m_panel.setLayout(m_gridbag);
		
	//moved variables from global to local scope!!!!
	Box boundP;//, testBox, winBox;//this is the first row in the GUI
	 /*
      buttons for switching the pages
      m_buttonProfile ==> Profile
      m_buttonChisquare ==> Chi Square
      m_buttonChisquare2 ==> CS + cliques
      hellingerButton ==> Hellinger Distance
      m_buttonRun ==> recalculate current data
      testButton ==> virus simulation test
    */
	//JButton m_buttonProfile, m_buttonChisquare, m_buttonChisquare2, m_buttonRun,hellingerButton, testButton, refreshButton, copyButton;
	//end move variables
	
	//the bound panel, includes the combo box and radio buttons
	boundP = Box.createHorizontalBox();
	//winBox = Box.createHorizontalBox();
	//testBox = Box.createHorizontalBox();

	//mainP = new JPanel();
	//mainP.setLayout(new BorderLayout());
		
	//users combo box
	//boundP.add(new JLabel("Select Account:"));
	//boundP.add(Box.createHorizontalStrut(15));//new JLabel("    "));
	//boundP.add(m_comboUsers);
	//boundP.add(Box.createHorizontalStrut(15));//new JLabel("    "));
	
		
	//inbound/outbound and attachment
	inbound = new JRadioButton("in-bound");
	inbound.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    boundArg = "in-bound";
	    		
		    emptySet=false;
		    //String user = (String)m_comboUsers.getSelectedItem();
		    String user = (String)m_winGui.getSelectedUser();
		    
		    isoutbound = false;
		    withatt=false;
		    //reset the labels
		    labRcpt.setText("Sender list");

		    //chi aquare
		    if(ChiOrCSC){
			labRcpt2.setText(MSG_TESRDIFF);
		    }
		    //with clique
		    else{
			labRcpt2.setText(MSG_TERRDIFF);
		    }
		    m_labRcpt1a.setText(MSG_TRRSL);
		    RorSaddress = MSG_SADD;
		    countRecords();
		    LoadPage();
		    //doClick();
		}});


	//inbound.setActionCommand("in-bound");
	//inbound.addActionListener(new boundListener());
	outbound = new JRadioButton("out-bound");
	outbound.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //String user = (String)m_comboUsers.getSelectedItem();
		    String user = (String)m_winGui.getSelectedUser();
		    emptySet=false;
		    boundArg = "out-bound";
	    
		    isoutbound = true;
		    withatt=false;
		    //reset the labels
		    labRcpt.setText(RL);
		    labRcpt2.setText(MSG_TERRDIFF);
		    m_labRcpt1a.setText(MSG_TERRL);
		    RorSaddress = MSG_RA;
		    countRecords();
		    LoadPage();
		    //doClick();
		}
	    }   );
	

	outbound.setSelected(true);
	attachment = new JRadioButton("out-bound with attachment");
	attachment.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //String user = (String)m_comboUsers.getSelectedItem();  
		    String user = (String)m_winGui.getSelectedUser();
		    boundArg = "attachment";
	    
		    emptySet=false;
		    isoutbound = true;
		    withatt=true;
		    //reset the labels
		    labRcpt.setText(RL);
		    labRcpt2.setText(MSG_TERRDIFF);
		    m_labRcpt1a.setText(MSG_TERRL);
		    RorSaddress = MSG_RA;
	
		    countRecords();
		    LoadPage();
		    //doClick();
		}
	    }   );
	bg = new ButtonGroup();
	bg.add(inbound);
	bg.add(outbound);
	bg.add(attachment);
	boundP.add(inbound);
	boundP.add(outbound);
	boundP.add(attachment);

	m_labRecords = new JLabel(" records  ");
	m_labRecords.setForeground(Color.blue);
	boundP.add(m_labRecords);

	//this panel is for the 'second row'
	//for the different statistical algorithm
	//m_panselect = new JPanel();
	//m_panselect.setLayout(new GridLayout(2,1));

	m_buttonProfile = new JButton("Profile");
	m_buttonProfile.setBackground(Color.pink);
	m_buttonProfile.setToolTipText("Runs a general recipient frequency profile per user");
	//m_buttonProfile.setEnabled(false);
	m_buttonChisquare = new JButton("Chi Square");
	m_buttonChisquare.setBackground(Color.pink);
	m_buttonChisquare.setToolTipText("Runs a chi square test and histograms for comparing the recipient frequency patterns over two time spans for the same user");
	m_buttonChisquare2 = new JButton("CS + cliques");
	m_buttonChisquare2.setToolTipText("runs a chi square test and histograms including clique frequency");
	m_buttonChisquare2.setBackground(Color.pink);
	//for Hellinger distance button
	hellingerButton = new JButton("Hellinger Distance");
	hellingerButton.setToolTipText("calculate Hellinger Distance");
	hellingerButton.setBackground(Color.pink);
	//hellingerSizeLabel = new JLabel("Hellinger Distance Size");
	hellingerSizeText = new JTextField("50",5);

	m_buttonRun = new JButton("Run");
	m_buttonRun.setToolTipText("runs Chi Square and Chi Square 2 tests");
	//m_buttonRun.setEnabled(false);

	//for test button
	testButton = new JButton("Test");
	testButton.setToolTipText("compare Hellinger Distance using dummy data and real clear data");	
	/*	
	JButton refreshButton = new JButton("Refresh Users");
	refreshButton.setToolTipText("<html>Reloads the user list from the db<p>Should be done if imported new data</html>");
	refreshButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try{
			refreshCombo(false);  
			//Refresh();
			
		    }
		    catch(Exception ex){
			System.out.println("refresh error in freq:"+ex);
		    }
		}
	    }   );
	*/

	JButton copyButton = new JButton ("Copy Frequency Table");
	copyButton.setToolTipText("Copy the frequency table contents to system clipboard");
	copyButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setCopy();

		}
	    }   );

	/*
	JButton sortButton = new JButton("Sort Users Desc");
	sortButton.setToolTipText("Reloads the user list with most freq first");
	sortButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    refreshCombo(true);
		    profileListener();
		}
	    }   );
	*/
	//EMTHelp emthelp = new EMTHelp(1);
	//testBox.add(emthelp);
	//m_panselect.add(winBox);
	//m_panselect.add(testBox);

	//adding things intot he panel here
	m_constraints.gridwidth = 7;
	m_constraints.gridx = 0;
	m_constraints.gridy = 0;
	m_constraints.insets = new Insets (3,3,3,3);     
	m_gridbag.setConstraints(boundP,m_constraints);
	m_panel.add(boundP);
		
	//add the bound panel
	//mainP.add(m_panselect, BorderLayout.SOUTH);
	//mainP.add(boundP, BorderLayout.NORTH);
	//m_gridbag.setConstraints(mainP,m_constraints);
	//m_panel.add(mainP);
	
	//winBox.add(m_buttonProfile);
	//winBox.add(m_buttonChisquare);
	//winBox.add(m_buttonChisquare2);
	//winBox.add(hellingerButton);		
	//winBox.add(new JLabel("Hellinger Distance Size"));
	//winBox.add(hellingerSizeText);
	//winBox.add(m_buttonRun);
	//m_gridbag.setConstraints(winBox,m_constraints);
	//m_panel.add(winBox);

	m_constraints.gridwidth=1;
	m_constraints.gridx = 0;
	m_constraints.gridy = 1;
	       
	m_gridbag.setConstraints(m_buttonProfile,m_constraints);
	m_panel.add(m_buttonProfile);

	m_constraints.gridx++;
	m_gridbag.setConstraints(m_buttonChisquare,m_constraints);
	m_panel.add(m_buttonChisquare);

	m_constraints.gridx++;
	m_gridbag.setConstraints(m_buttonChisquare2,m_constraints);
	m_panel.add(m_buttonChisquare2);
	m_constraints.gridx++;
	m_gridbag.setConstraints(hellingerButton,m_constraints);
	m_panel.add(hellingerButton);

	JLabel hds = new JLabel("Hellinger Distance Size");
	m_constraints.gridx++;
	m_gridbag.setConstraints(hds,m_constraints);
	m_panel.add(hds);
	m_constraints.gridx++;
	m_gridbag.setConstraints(hellingerSizeText,m_constraints);
	m_panel.add(hellingerSizeText);
	m_constraints.gridx++;
	m_gridbag.setConstraints(m_buttonRun,m_constraints);
	m_panel.add(m_buttonRun);

	//testBox.add(testButton);
	//testBox.add(new EMTHelp(1));
	//testBox.add(copyButton);
	//testBox.add(refreshButton);
	//testBox.add(sortButton);
	
	m_constraints.gridx = 0;
	m_constraints.gridy = 2;
	
	//m_gridbag.setConstraints(testBox,m_constraints);
	//m_panel.add(testBox);

	m_gridbag.setConstraints(testButton,m_constraints);
	m_panel.add(testButton);
	EMTHelp emth = new EMTHelp(EMTHelp.ADDRESSLIST);
	m_constraints.gridx++;
	m_gridbag.setConstraints(emth,m_constraints);
	m_panel.add(emth);
	m_constraints.gridx++;
	m_gridbag.setConstraints(copyButton,m_constraints);
	m_panel.add(copyButton);
	//m_constraints.gridx++;
	//m_gridbag.setConstraints(refreshButton,m_constraints);
	//m_panel.add(refreshButton);
	//m_constraints.gridx++;
	//m_gridbag.setConstraints(sortButton,m_constraints);
	//m_panel.add(sortButton);
			



	//temp fix because we dont remove top so need last thing to remove
	//m_panel.add(new JLabel("dummy"));

	
	m_Button = PROFILE;
	//refreshCombo(false);
	m_userCliqueMember = new Vector();
	m_thres2=50;//was 50 set to zero so it triggers a recalc
	
	//createCliqueList(m_thres2,5);   
	//countRecords();
	//Profile panel
	//the panel with 3 diagrams and 1 table
	m_panel1 = new JPanel();
	m_panel1.setPreferredSize(new Dimension(730,650));
	m_gridbag1 = new GridBagLayout();
	m_constraints1 = new GridBagConstraints();
	m_panel1.setLayout(m_gridbag1);
	//sh	m_ALSequence = new Vector();	//sh    m_ALSequence2 = new Vector();
	//sh    m_ALSequence3 = new Vector();	//sh    m_ALSequence5 = new Vector();
	//m_ALList = new Vector();
	m_ALDist = null;//new ArrayList();
	m_ALDist1 = new Vector();
	m_ALDist2 = new Vector();
	m_ALDist3 = new Vector();
		
	//this is the diagram of recipient frequency histogram
	m_plot13 = new JPlot2D();
	m_plot13.setPreferredSize(new Dimension(350,290));    
	m_constraints1.gridx = 0;
	m_constraints1.gridy = 0;
	//m_constraints1.insets = new Insets (3,3,3,3);     
	m_gridbag1.setConstraints(m_plot13,m_constraints1);
	m_panel1.add(m_plot13);
	
	//the total recipient address list
	m_plot11 = new JPlot2D();
	m_plot11.setPreferredSize(new Dimension(350,290));
	m_constraints1.gridx = 0;
	m_constraints1.gridy = 1;
	//m_constraints1.insets = new Insets (3,3,3,3);
	m_gridbag1.setConstraints(m_plot11,m_constraints1);
	m_panel1.add(m_plot11);  
		
	//# distinct rcpt and # attach per email blocks
	m_plot12 = new JPlot2D();
	m_plot12.setPreferredSize(new Dimension(350,290));
	m_constraints1.gridx = 1;
	m_constraints1.gridy = 1;
	//m_constraints1.insets = new Insets (3,3,3,3);     
	m_gridbag1.setConstraints(m_plot12,m_constraints1);
	m_panel1.add(m_plot12);  
		
	//the table
	GridBagLayout gridbag11 = new GridBagLayout();
	GridBagConstraints constraints11 = new GridBagConstraints();
	JPanel panRcpt = new JPanel();
	panRcpt.setLayout(gridbag11);
	labRcpt = new JLabel(RL);
	constraints11.gridx = 0;
	constraints11.gridy = 0;
	gridbag11.setConstraints(labRcpt, constraints11);
	panRcpt.add(labRcpt);
	m_rcptModel = new DefaultTableModel();
	m_rcptModel.addColumn(RorSaddress); 
	m_rcptModel.addColumn(MSG_FREQ);
	m_rcptFreq = new JTable(m_rcptModel);
	m_rcptFreq.setRowSelectionAllowed(true);
	m_rcptFreq.setColumnSelectionAllowed(false);
	m_rcptFreq.setEnabled(false);
	JScrollPane rcptFreqPane = new JScrollPane(m_rcptFreq);
	rcptFreqPane.setPreferredSize(new Dimension(344,268));
	constraints11.gridx = 0;
	constraints11.gridy = 1;
	constraints11.gridwidth=1;
	//constraints11.insets = new Insets(3,3,3,3);
	gridbag11.setConstraints(rcptFreqPane, constraints11);
	panRcpt.add(rcptFreqPane);//add the JTable
	panRcpt.setPreferredSize(new Dimension(350,290));
	m_constraints1.gridx = 1;
	m_constraints1.gridy = 0;
	//m_constraints1.insets = new Insets(3,3,3,3);
	m_gridbag1.setConstraints(panRcpt, m_constraints1);
	m_panel1.add(panRcpt);    
		
	//add panel1 to the biggest panel
	m_constraints.gridx = 0;
	m_constraints.gridy = 3;
		m_constraints.gridwidth = 7;
	//m_constraints.insets = new Insets(3,3,3,3);
	m_gridbag.setConstraints(m_panel1, m_constraints);
	m_panel.add(m_panel1);
	//m_panel.setVisible(true);
	//m_panel1.setVisible(true);
		
	//Chisquare panel;
	//when the user press clique
	//2 tables and 2 diagrams
	m_panel2 = new JPanel();
	
	m_panel2.setPreferredSize(new Dimension(730,650));
	m_gridbag2 = new GridBagLayout();
	m_constraints2 = new GridBagConstraints();  
	m_panel2.setLayout(m_gridbag2);
		
	//the training diagram
	JPanel panBound1 = new JPanel();
	JLabel labBound1 = new JLabel("Training Range");
	panBound1.add(labBound1);
	m_min1 = new JTextField("0",5);
	panBound1.add(m_min1);   
	m_max1 = new JTextField("250",5); 
	panBound1.add(m_max1);
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 0;
	m_constraints2.insets = new Insets (3,3,3,3);//was 1,1,1,1     
	m_gridbag2.setConstraints(panBound1,m_constraints2);
	m_panel2.add(panBound1);
	m_plot21 = new JPlot2D();
	m_plot21.setPreferredSize(new Dimension(350,245));  
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 1;  
	//	m_constraints2.insets = new Insets (3,3,3,3);
	m_gridbag2.setConstraints(m_plot21,m_constraints2);
	m_panel2.add(m_plot21);         
		
	//the testing diagram
	JPanel panBound2 = new JPanel();
	JLabel labBound2 = new JLabel("Testing Range");
	panBound2.add(labBound2);
	m_min2 = new JTextField("250",5);
	panBound2.add(m_min2); 
	m_max2 = new JTextField("300",5); 
	panBound2.add(m_max2);
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 2;
	//	m_constraints2.insets = new Insets (1,1,1,1);     
	m_gridbag2.setConstraints(panBound2,m_constraints2);
	m_panel2.add(panBound2);
	m_plot22 = new JPlot2D();
	m_plot22.setPreferredSize(new Dimension(350,245));
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 3;
	//m_constraints2.insets = new Insets (3,3,3,3);     
	m_gridbag2.setConstraints(m_plot22,m_constraints2);
	m_panel2.add(m_plot22);
		
	//the training table
	m_panThreshold1 = new JPanel();

	m_labRcpt1a = new JLabel("Training Range Rcpt List"); 
	m_panThreshold1.add(m_labRcpt1a);
	m_thres2=50;
	m_threshold2 = new JTextField("50",3);
	m_panThreshold1.add(m_threshold2);
	m_constraints2.gridx = 1;
	m_constraints2.gridy = 0;
	m_gridbag2.setConstraints(m_labRcpt1a, m_constraints2);
	m_panel2.add(m_panThreshold1);
		
	m_rcptModel1 = new DefaultTableModel();
	m_rcptModel1.addColumn(RorSaddress);
	m_rcptModel1.addColumn(MSG_FREQ);
	m_rcptFreq1 = new JTable(m_rcptModel1);
	m_rcptFreq1.addMouseListener(this);
	m_rcptFreq1.setEnabled(false);
	JScrollPane rcptFreqPane1 = new JScrollPane(m_rcptFreq1);
	rcptFreqPane1.setPreferredSize(new Dimension(350,245));
	m_constraints2.gridx = 1;
	m_constraints2.gridy = 1;
	//m_constraints2.insets = new Insets(3,3,3,3);
	m_gridbag2.setConstraints(rcptFreqPane1, m_constraints2);
	m_panel2.add(rcptFreqPane1);
	JPanel panThreshold2 = new JPanel();
	labRcpt2 = new JLabel(MSG_TERRDIFF);
		
	//the testing table
	panThreshold2.add(labRcpt2);
	m_thres=5;
	m_threshold = new JTextField("5",3);
	panThreshold2.add(m_threshold);
	m_constraints2.gridx = 1;
	m_constraints2.gridy = 2;
	m_gridbag2.setConstraints(panThreshold2, m_constraints2);
	m_panel2.add(panThreshold2); 
	//
	m_rcptModel2 = new DefaultTableModel();
	m_rcptModel2.addColumn(RorSaddress);
	m_rcptModel2.addColumn(MSG_FREQ);
	m_rcptModel3 = new DefaultTableModel();
	m_rcptModel3.addColumn(RorSaddress);
	m_rcptModel3.addColumn(MSG_FREQ);
	m_rcptModel3.addColumn(MSG_DIFF);
	m_rcptFreq2 = new JTable(m_rcptModel3);
	m_rcptFreq2.addMouseListener(this);
	m_rcptFreq2.setEnabled(false);
	JScrollPane rcptFreqPane2 = new JScrollPane(m_rcptFreq2);
	rcptFreqPane2.setPreferredSize(new Dimension(350,245));
	m_constraints2.gridx = 1;
	m_constraints2.gridy = 3;
	//	m_constraints2.insets = new Insets(3,3,3,3);
	m_gridbag2.setConstraints(rcptFreqPane2, m_constraints2);
	m_panel2.add(rcptFreqPane2);    
	
	//show colors on abnormal data
	m_diffColumnRenderer = new DefaultTableCellRenderer()
	    {
		public void setValue(Object value) 
		{
		    //sh - oct 03 dont think this does anything
		    //if(Double.parseDouble((String)value)>=0.0 || Double.parseDouble((String)value)<=0.0)	
		    //if((String.valueOf(value) instanceof Double))
		    {  
			if ( (Double.parseDouble((String)value) >	m_thres)||(Double.parseDouble((String)value) < -m_thres))
			    setBackground(Color.red);  
			else
			    setBackground(Color.white); 	
			setText((value==null) ? "" : String.valueOf(value));
		    }
		}
	    };
		
	m_diffColumnRenderer2 = new DefaultTableCellRenderer()
	    {
		public void setValue(Object value) 
		{
		    if((value instanceof String))
			{  
			    if ( ((String)value).startsWith("clique"))
				setBackground(Color.green);  
			    else
				setBackground(Color.white); 	
			    setText((value == null) ? "" : value.toString());
			}
		}
	    };
		
		
	//the two blue labels in the bottom
	m_labChisquare = new JLabel("Chi Square test");
	m_labChisquare.setForeground(Color.blue);
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 4;
	//	m_constraints2.insets = new Insets(7,7,7,7);
	m_gridbag2.setConstraints(m_labChisquare, m_constraints2);
	m_panel2.add(m_labChisquare);
		
	m_labDegree = new JLabel("Degrees of freedom");
	m_labDegree.setForeground(Color.blue);
	m_constraints2.gridx = 1;
	m_constraints2.gridy = 4;
	//m_constraints2.insets = new Insets(1,1,1,1);
	m_gridbag2.setConstraints(m_labDegree, m_constraints2);
	m_panel2.add(m_labDegree);
		
	//KS-value label 
	m_labKS = new JLabel("KS test: ");
	m_labKS.setForeground(Color.blue);
	m_constraints2.gridx = 0;
	m_constraints2.gridy = 5;
	//m_constraints2.insets = new Insets(7,7,7,7);
	m_gridbag2.setConstraints(m_labKS, m_constraints2);
	m_panel2.add(m_labKS);		

	//the actions
	m_buttonChisquare.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    chisquareListener();
		}
	    }   );
	///addActionListener(new chisquareListener());
	

	m_buttonChisquare2.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    chisquareListener2();
		}
	    }   );

	//.addActionListener(new chisquareListener2());
	


	m_buttonProfile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		 
		    profileListener();
		}
	    }   );

	//.addActionListener(new profileListener());
	//m_buttonProfile.setEnabled(false);
	

	m_buttonRun.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    runListener();

		}
	    }   );


	//.addActionListener(new runListener());
		
	//Hellinger actions
	hellingerButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    hellingerListener();

		}
	    }   );


	//.addActionListener(new hellingerListener());
		
	//Test action
	testButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ChiOrCSC = true;
		    emptySet=false;
			
		    //before=System.currentTimeMillis();
		    //  System.out.println("working...");
		    (new Thread(new Runnable(){
			    public void run(){
			
				m_winGui.setEnabled(false);//block the window
				/*
				  Object[] options = {"Virus Simulation",
				  "ML Training"};
				  int type 
				  = JOptionPane.showOptionDialog(AddressList.this, "Select Testing Type:", "WOO~LA~LA~~", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				  if(type==0){
				  runTest();
				  }
				  else if (type==1){
				  runML();
				  }
				*/
				runTest();
				m_winGui.setEnabled(true);
				System.gc();
				//after=System.currentTimeMillis();
				//System.out.print("completed in ");
				//System.out.print((after-before)/1000.0);
				//System.out.println(" secs");
				}
			   })).start();
		    /*
		      Do the last action again
		      I need to do it again. Otherwise some message
		      will be lost.
		    */
		    LoadPage();
		}
	    }  );

	//addActionListener(new testListener());
	//show help

	//addActionListener(new helpListener());
		
	//refresh action




	//.addActionListener(new copyListener());
	//bw = new BusyWindow("statistical models","working");
//	working = new BusyWindow(this,"Statistical Models"
//				 ,"      System is working,"
//				 +" please wait...         ");
//	//the combo box
	//why use 'setViewportView'?
	//It may be a problem when switch windows.
	/*
	m_comboUsers.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //comboUserListener();
		        
		    // System.out.println("going to count");
		    (new Thread(new Runnable(){
			    public void run(){
	 
				countRecords();
				
			    }
			})).start();
		
		}
	    }   );
	*/

	//.addActionListener(new comboUserListener());
	setViewportView(m_panel1); 
	setViewportView(m_panel); 
	startupCombouser();
	countRecords();
	System.gc();
	//do once not everywhere since it doesnt cahnge

	   



    }//end constructor 
	
    /**
       For refresh button, refresh the database
    
       class refreshListener implements ActionListener{
       public void actionPerformed(ActionEvent e){
       try{
       before=System.currentTimeMillis();
       System.out.println("working...");
       Refresh();
       after=System.currentTimeMillis();
       System.out.print("completed in ");
       System.out.print((after-before)/1000.0);
       System.out.println(" secs");
       }
       catch(Exception ex){
       System.out.println("refresh error:"+ex);
       }
       }
       }
    */
    /*
      class helpListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
      showhelp();
      }
      }
    */


    /**
       class copyListener implements ActionListener{
       public void actionPerformed(ActionEvent e){
       setCopy();
       }
       }
    */
    private void LoadPage()
    {
	//System.out.println("loadpage");
	if(m_Button==PROFILE)
	    profileListener();
	//	    m_buttonProfile.doClick();
	else if(m_Button==HELLINGER)
	    hellingerListener();
	//  hellingerButton.doClick();
	else if(m_Button==CHI_SQUARE)
	    chisquareListener();
	//m_buttonChisquare.doClick();
	else if(m_Button==CHI_CLIQUE)
	    chisquareListener2();
	//m_buttonChisquare2.doClick();
    }


    /**
     * Sets the copy.
     */
    private void setCopy(){
	
	//private DefaultTableModel m_rcptModel,m_rcptModel1,m_rcptModel2, m_rcptModel3;
	//private JTable m_rcptFreq,m_rcptFreq1,m_rcptFreq2;
	System.out.println("!!!!!!!1s copy here");
	Vector select = new Vector();
	String neat = new String();
	int n2,n = m_rcptFreq.getColumnCount();
	if(m_Button==PROFILE || m_Button==HELLINGER){
	    select = m_rcptModel.getDataVector();
	    for(int i=0;i<n-1;i++)
	    	neat = neat.concat( m_rcptFreq.getColumnName(i) + ",");
	    //shlom fixed the tight loop problem here
	    //he was trying to only give a ocmma everywhere but last.
	    neat += m_rcptFreq.getColumnName(n-1)+ "\n";
	}
	else if(m_Button==CHI_SQUARE || m_Button==CHI_CLIQUE){
	    Object[] options = {"Training Table","Testing Table"};
	    int type 
		= JOptionPane.showOptionDialog(RcptFrequency.this, "Which Table?", "Select a Table", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

	    if(type==0){
		select = m_rcptModel1.getDataVector();
		n2 =m_rcptFreq1.getColumnCount();
		for(int i=0;i<n2-1;i++)
		    neat = neat.concat( m_rcptFreq1.getColumnName(i)+",");
		//if(i!=m_rcptFreq1.getColumnCount()-1)neat+=",";
		//same as above
		neat +=m_rcptFreq1.getColumnName(n2-1)+ "\n";
	    }
	    else if(type==1){
		select = m_rcptModel3.getDataVector();
		n2= m_rcptFreq2.getColumnCount();
		for(int i=0;i<n2-1;i++)
		    neat = neat.concat( m_rcptFreq2.getColumnName(i)+",");
		//    if(i!=m_rcptFreq2.getColumnCount()-1)neat+=",";
		
		neat +=  m_rcptFreq2.getColumnName(n2-1) + "\n";
	    }
	}

	int j=0;
	for(int i=0;i<select.size();i++){
	    Vector v = (Vector)select.elementAt(i);
	    for(j=0;j<v.size()-1;j++)
		neat  = neat.concat( (String)v.elementAt(j)+",") ;
	    neat += v.elementAt(j) + "\n";
	}
	StringSelection data = new StringSelection(neat);
	clipboard.setContents (data, data);

    }


    /**
     * Run ml.
     */
    private void runML(){
	try{
	    long before=System.currentTimeMillis();
		
	    /*************** start here ******************/
	    //the header and classes
	    String query = "select sender,count(sender) from email "
		+ "group by sender having count(*)>1000 order by sender";
	    
	    String[][] hc=null;
	    if (m_jdbc!=null){  
		try{
		    synchronized (m_jdbc){
			hc = m_jdbc.getSQLData(query);
			//hc = m_jdbc.getRowData();
		    }

		}
		catch(SQLException ex){   
		    System.out.println("query1 error:"+ex);
		}finally{}
	    }

	    int length = 0;
	    if(hc!=null)
		length = hc.length;
	    String[] classes = new String[length];
	    short[] header = new short[length];
	    String users = "";
	    for(int i=0;i<length;i++){
		classes[i] = hc[i][0];
		header[i] = Short.parseShort(hc[i][1]);
		
		if(i==0)
		    users += " sender='"+classes[i]+"'";
		else
		    users += " or sender='"+classes[i]+"'";
	    }

	    //get the data
	    query = "select sender,rcpt,DaTES,numattach,UID from"
		+" email where"+users
		+" order by sender,DaTES";
	  
	    String [][]emailData = null;

	    if (!(m_jdbc==null)){  
		try{

		    synchronized (m_jdbc){
		    	emailData = m_jdbc.getSQLData(query);
			//emailData = m_jdbc.getRowData();

		    }
		}
		catch(SQLException ex){   
		    System.out.println("query2 error:"+ex);
		}
	    }
	    
	  
	  //  int numOfData = emailData.length;

	    MLStatistics mls = new MLStatistics();

	    mls.train(header,classes,emailData,length,m_winGui.getTarget());
		
	    /*************** end here ******************/
		
	    long after=System.currentTimeMillis();
	    System.out.print("completed in ");
	    System.out.print((after-before)/1000.0);
	    System.out.println(" secs");
	    mls.save("test.txt");
	    JOptionPane.showMessageDialog(RcptFrequency.this, "ML training Done...");

	}
	catch(Exception e){
	    System.out.println("runML error:"+e);
	}
    }
	
    /**
       Implement the action of 'Test' button
       Generate dummy data, and find out the difference
       with the original data.
    */
    public final void runTest(){

	try{
	    int numDummy = 0;//number of dummies
	    int location = 0;//location
	    long timeInt = 0;//time intval
	    int hdistance = 0;//hellinger distance
	    int stSize = 0;//size of moving average xstandard deviation
	    double alpha = 2;//alpha
	    boolean isDistinct = true;//distinct rcpt
	    boolean withAttach = false;//with attachment
	    //the original hellinger distance
	    originalHell = new Vector();
			
	    //get the selected user
	    //String select = (String)m_comboUsers.getSelectedItem();
	    String select = (String)m_winGui.getSelectedUser();
	    
	    /*
	    String queryUsers = "select count(*) from email where sender='"+select+"'";
	    String count[][] = null;
	    if (m_jdbc!=null)
		{  
		    try{
			synchronized (m_jdbc){
			    m_jdbc.getSqlData(queryUsers);
			    count = m_jdbc.getRowData();
			}
		    }
		    catch(SQLException ex){   
			System.out.println("HD testing error.");
			System.out.println(ex);
		    }finally{}
		}
	    */
	   userShortProfile qu = m_winGui.getUserInfo(select);
	    String count[][] = new String[1][1];
	    count[0][0] = ""+qu.getTotalCount();//qu[winGui.User_count];
	    
		
	    //the configuration
	    //	    System.out.println("enter configuration");
	    HellingerConf dialog 
		= new HellingerConf(this, count[0][0]);   
	    
	    dialog.setVisible(true);
			
	    //if someone just closes the dialog
	    boolean tobreak = false;
			
	    //read configuration
	    while(true){
		//after press the 'set' button
		if(dialog.isBreak()==true)
		    {
		    tobreak = true;
		    break;
		   }
		//when press the 'set' button
		//read the information
		else if(dialog.isSet()==true){
		    numDummy = dialog.numOfDummy();
		    location = dialog.getLoc();
		    timeInt = dialog.getT();
		    hdistance = dialog.getHD();
		    isDistinct = dialog.isDist();
		    withAttach = dialog.isAttach();
		    stSize = dialog.getSDsize();
		    alpha = dialog.getAlpha();
		    dialog.close();
		    break;
		}
		
		//pause .5 seconds
		try{
		    Thread.sleep(500);
		}catch(Exception eee){}



	    }//while

	    if(m_nbRecords.intValue()<location || m_nbRecords.intValue()<numDummy*4){
		String message = "Not enough data. Try other users.\n";
		message += "Or try a smaller start location.";
		JOptionPane.showMessageDialog(RcptFrequency.this,message);
	    }
	    else if(!tobreak){
		
		//generate dummy data
		String[][] dummy = 
		    createDummy(Integer.parseInt(count[0][0])
				,numDummy,location,timeInt
				,isDistinct,select,hdistance,withAttach);
		AlertPoint alertpoint = new AlertPoint(realDummy);		
		/*** show the figures ***/

		//original hellinger distance
		double[] originalHDistance = new double[originalHell.size()];
		//double[] tmpValue = new double[50];
		for (int i=0; i<originalHell.size(); i++){
		    originalHDistance[i] 
			= ((Double)originalHell.get(i)).doubleValue();
		}
				
		//hellinger distance of dummy data
		Vector tmp = calculateHell(hdistance, dummy);
		double[] dummyHDistance = new double[tmp.size()];
		for (int i=0; i<tmp.size(); i++){
		    dummyHDistance[i] = ((Double)tmp.get(i)).doubleValue();
		}
				
		//show some message
		String msg1 = "Number of Dummy: "+String.valueOf(numDummy)
		    +",  Dummy Starts Location: "+String.valueOf(location)
		    +",  Time Interval: "+String.valueOf(timeInt);

		String msg2 = "Hellinger Distance: "+hdistance
		    +",  Size of Moving Average: "+String.valueOf(stSize) 
		    + ",  Alpha: "+String.valueOf(alpha);
				
		String msg3 = "";
		if(isDistinct)
		    msg3 += "   Use Distinct RCPT";
		if(withAttach)
		    msg3 += "   With Attachment";
				
		System.out.println("show diagrams");
		
		//the diagram panel
		HellingerTest displayTest 
		    = new HellingerTest(msg1, msg2, msg3
					,dummyHDistance);
				
		//Hellinger Distance Diagram
		//left
		displayTest.setFigure("Hellinger Distance"
				      ,"Reciptent"
				      ,"Hellinger Distance"
				      ,displayTest.hellinger1
				      ,Color.orange
				      ,originalHDistance);

	        displayTest.addCurve(displayTest.hellinger1
				     ,dummyHDistance
				     ,Color.red);
				
		//right
		displayTest.setFigure("Hellinger Distance"
				      ,"Reciptent"
				      ,"Hellinger Distance"
				      ,displayTest.hellinger2
				      ,Color.red
				      ,dummyHDistance);
		
		//threshold by moving average
		double[] hdThreshold 
		    = alertpoint.movingAverage(stSize, dummyHDistance);

		//label the range of real dummy
		double[] testThr 
		    = new double[originalHDistance.length+numDummy];
		double[] testEnd
		    = new double[originalHDistance.length+numDummy];
		for(int i=0;i<testThr.length;i++){
		    if(i<location) testThr[i]=0;
		    else testThr[i]=1;

		    if(i<realDummy[realDummy.length-1])testEnd[i]=0;
		    else testEnd[i]=1;
		}
		/*
		  displayTest.addCurve(displayTest.hellinger2
		  ,testThr
		  ,Color.black);
		  displayTest.addCurve(displayTest.hellinger2
		  ,testEnd
		  ,Color.black);
		*/
		//by Standard Deviation
		double[] thr2 
		    = alertpoint.calculateThreshold(dummyHDistance
						    , SD_THRE_SIZE, alpha);
		displayTest.addCurve(displayTest.hellinger2
				     ,thr2
				     ,Color.blue);

		/***** calculate the information of dummy data *****/
		double[][] sequence = calculateDummy(dummy);
				
		//yep, I have to do this stupid thing again
		double[] sequence1 = new double[sequence.length];//Number of Distince RCPT / 20
		double[] sequence2 = new double[sequence.length];//Number of Distince RCPT / 50
		double[] sequence3 = new double[sequence.length];//Number of MSGS with Attachment / 50

		for(int i=0; i<sequence.length; i++){
		    sequence1[i] = sequence[i][0];
		    sequence2[i] = sequence[i][1];
		    sequence3[i] = sequence[i][2];
		}

		//calculate the original information
		calculateProfile();
				
		//Number of Distince RCPT Diagram
		//left, the real one
		displayTest.setFigure("Number of Distince RCPT / 50"
				      ,"all emails"
				      ,"Number of Distince RCPT per 50"
				      ,displayTest.distRcpt50_1
				      ,Color.orange
				      ,m_ALSequence3);

		displayTest.addCurve(displayTest.distRcpt50_1
				     ,sequence2
				     ,Color.red);
		
		
		//right, the dummy one
		displayTest.setFigure("Number of Distince RCPT / 50"
				      ,"all emails"
				      ,"Number of Distince RCPT per 50"
				      ,displayTest.distRcpt50_2
				      ,Color.red
				      ,sequence2);

		//threshold of moving average
		double[] rcptThreshold 
		    = alertpoint.movingAverage(stSize, sequence2);
		//label the range of real dummy
		double[] testThr2
		    = new double[originalHDistance.length+numDummy];
		double[] testEnd2
		    = new double[originalHDistance.length+numDummy];
		for(int i=0;i<testThr2.length;i++){
		    if(i<location) testThr2[i]=0;
		    else testThr2[i]=SD_THRE_SIZE;

		    if(i<realDummy[realDummy.length-1])testEnd2[i]=0;
		    else testEnd2[i]=SD_THRE_SIZE;
		}
		/*
		  displayTest.addCurve(displayTest.distRcpt50_2
		  ,testThr2
		  ,Color.black);
		  displayTest.addCurve(displayTest.distRcpt50_2
		  ,testEnd2
		  ,Color.black);
		*/
		//by Standard Deviation
		double[] thr3 
		    = alertpoint.calculateThreshold(sequence2
						    , SD_THRE_SIZE, alpha);
		displayTest.addCurve(displayTest.distRcpt50_2
				     ,thr3
				     ,Color.blue);

		//Number of MSGS with Attachment Diagram
		//left, the real one
		displayTest.setFigure("MSGS with Attachment / 50"
				      ,"all emails"
				      ,"Number of Attachment per 50"
				      ,displayTest.attach1
				      ,Color.orange
				      ,m_ALSequence5);
				
		displayTest.addCurve(displayTest.attach1
				     ,sequence3
				     ,Color.red);

		//right, the dummy one
		displayTest.setFigure("MSGS with Attachment / 50"
				      ,"all emails"
				      ,"Number of Attachment per 50"
				      ,displayTest.attach2
				      ,Color.red
				      ,sequence3);
		//moving average
		double[] attachThreshold 
		    = alertpoint.movingAverage(stSize, sequence3);
		/*		
				displayTest.addCurve(displayTest.attach2
				,testThr2
				,Color.black);
				displayTest.addCurve(displayTest.attach2
				,testEnd2
				,Color.black);
		*/
		//by Standard Deviation
		double[] thr4 
		    = alertpoint.calculateThreshold(sequence3
						    , SD_THRE_SIZE, alpha);
		displayTest.addCurve(displayTest.attach2
				     ,thr4
				     ,Color.blue);

		displayTest.redraw();
		displayTest.setVisible(true);
		//calculateProfile();
		//calculate some old value again
		//, just make the last GUI better
		m_ALDist= null;//.clear();// = new Vector();
		calculateFreq();
		//m_rcptFreq.getColumnModel().getColumn(1).setMaxWidth(60);

		//the moving average
		boolean[] hdalert 
		    = alertpoint.maAlert(hdThreshold, dummyHDistance);
		boolean[] rcptalert 
		    = alertpoint.maAlert(rcptThreshold, sequence2);
		boolean[] attachalert 
		    = alertpoint.maAlert(attachThreshold, sequence3);
		AlertPoint[] mAvg 
		    = alertpoint.collectAlert(hdalert, rcptalert, attachalert);

		//the slope-shape
		AlertPoint[] alerts 
		    = alertpoint.findMountain(dummyHDistance
					      ,sequence2,sequence3);
		
		//Standard Deviation
		boolean[] test1 
		    = alertpoint.maAlert(thr2, dummyHDistance);
		boolean[] test2 
		    = alertpoint.maAlert(thr3, sequence2);
		boolean[] test3 
		    = alertpoint.maAlert(thr4, sequence3);
		AlertPoint[] STD 
		    = alertpoint.collectAlert(test1, test2, test3);

		/*
		  for(int i=0;i<STD.length;i++){
		  System.out.print("S:"+STD[i].start+"-"+STD[i].end+" ");
		  }
		*/
		int testingSize = originalHDistance.length;

		//Standard Dev. union Shape-Slope
		AlertPoint[] refine1 
		    = alertpoint.union(testingSize+numDummy
				       ,STD,alerts);
		/*
		  Show another panel, for the suspicious ranges.
		  Suspicious ranges will be marked as "Alert".
		*/
		String alertMsg[] 
		    = alertpoint.getAlertMsg(testingSize+numDummy,refine1);
		//System.out.println("show result...");
		
		HellingerResult result 
		    = new HellingerResult(this, alertMsg,msg1,msg2,msg3);
		result.setVisible(true);

		System.out.println(msg1);
		System.out.println(msg2);
		System.out.println(msg3);
		
	    }
	}catch(Exception e){}
    }

    
	
    /** 
	Generate the dummy data.
	@param size number of normal data
	@param dsize number of dummy data
	@param loc location of dummy data
	@param time time interval of each dummy data
	@param isDist distinct rcpt
	@param user current testing user
	@param hdistance the Hellinger Distance
	@param withAttach with Attachment or not
	
	@return A 2D array of the dummy data and attachment
    */
    public final String[][] createDummy(final int size, final int dsize, final int loc
					, final long time, final boolean isDist
					, final String user, final int hdistance, final boolean withAttach){ 
	System.out.println("create dummy...");
	originalHell = new Vector();
//	Vector dummyv = new Vector();
	String[] dummya;
	int dummyNum = dsize;
//	double[] dummyValue;
	String[][] old = null;//the original data
	String[][] fake;//original + dummy data
	realDummy = new int[dsize];	
		
	//compute the old real data
	String[] date = m_winGui.getDateRange_toptoolbar();
	String tmpquery = "select rcpt,utime,numattach from"
	    +" email where sender='"+user+"' and DaTES>'"+date[0]
	    +"' and DaTES<'"+date[1]+"' order by utime";
		
		
	if (m_jdbc!=null){  
	    try{
			    
		synchronized (m_jdbc)
		    {
			old = m_jdbc.getSQLData(tmpquery);
			//old = m_jdbc.getRowData();
		    }
	

	    }
	    catch(SQLException ex){   
		System.out.println("HD testing error.");
		System.out.println(ex);
	    }finally{}
	}
		

//cLOUDSCAPE
	//change DaTES to Long value
	String[][] tmpOld = new String[old.length][2];
	for(int i=0; i<old.length; i++){
	    tmpOld[i][0] = old[i][0];
	  //  long tmpt = getMill(old[i][1]);
	    tmpOld[i][1] = old[i][1];//String.valueOf(tmpt);
	}
	
	//distinct rcpt or not
	if(isDist){	    
	    tmpquery = "select distinct rcpt,numattach from"
		+" email where sender='"+user+"' and DaTES>'"+date[0]
	    +"' and DaTES<'"+date[1]+"'";
	}
	else{
	    tmpquery = "select rcpt,numattach from"
		+" email where sender='"+user+"' and DaTES>'"+date[0]
	    +"' and DaTES<'"+date[1]+"'";
	}
	String rcpts[][] = null;


	//generate dummy rcpt
	if (!(m_jdbc==null)){  
	    try{
		synchronized (m_jdbc)
		    {
			rcpts = m_jdbc.getSQLData(tmpquery);
			//rcpts = m_jdbc.getRowData();

		    }
	    }
	    catch(SQLException ex){   
		System.out.println("HD testing error.");
		System.out.println(ex);
	    }finally{}
	}
	int rlength = rcpts.length;
	if(rlength < dummyNum)dummyNum = rlength;
	Vector vrcpt = new Vector(rlength);
	for(int i=0; i<rlength; i++){
	    vrcpt.addElement(rcpts[i][0]);
	}

	dummya = new String[dummyNum];
	int tmpsize,seed;
	for(int i=0; i<dummyNum; i++){
	    tmpsize = vrcpt.size();
	    // double ran = Math.random();
	    seed = (int)Math.floor(tmpsize * Math.random());
	    dummya[i] = (String)vrcpt.remove(seed);
	}
	//**************************

	//combine original and dummy daya
	int currold = 0;
	int currdummy = 0;
	int dIndex=0;
	long tmpl,startLoc = Long.parseLong(old[loc][1])+1;
	fake = new String[dummyNum+old.length][2];
	System.out.println("dummy:");
	for(int i=0; i<dummyNum+old.length; i++){
	    tmpl = 0;
	    if(currold<old.length){
		tmpl = Long.parseLong(old[currold][1]);
	    }
	    
	    //here is the dummy
	    if(startLoc < tmpl && currdummy<dummyNum){
		//System.out.print(i+" ");//show the dummy position
		realDummy[dIndex]=i;
		dIndex++;
		fake[i][0] = dummya[currdummy++];
		if(withAttach){
		    fake[i][1] = "1";
		}
		else{
		    fake[i][1] = "0";
		}
				
		startLoc += time*1000*60;//by minutes
	    }
	    else{
		fake[i][0] = old[currold][0];
		fake[i][1] = old[currold++][2];
	    }
	}
	System.out.println("");
	System.out.println("test3");
	originalHell = calculateHell(hdistance, tmpOld);
		
	//the dummy data
	Vector tmp2 = calculateHell(hdistance, fake);
	dummyHDistance=new double[tmp2.size()];
	for (int i=0; i<tmp2.size(); i++){
	    dummyHDistance[i]=((Double)tmp2.get(i)).doubleValue();
	}
	return fake;
    }
	
    /**
     * Change the DaTES to millisecond.
     *
     * @param dummy the dummy
     * @return Insert Time in Long
     */
  /*  public final long getMill(String in){
	String[] dt=in.split(" ");
	String[] date=dt[0].split("-");
	String[] time=dt[1].split(":");
	if(time[2].indexOf(".") != -1){
	    time[2] = time[2].substring(0,time[2].indexOf("."));
	}
	Calendar c=Calendar.getInstance();
	c.set(Integer.parseInt(date[0])
	      ,Integer.parseInt(date[1])-1
	      ,Integer.parseInt(date[2])
	      ,Integer.parseInt(time[0])
	      ,Integer.parseInt(time[1])
	      ,Integer.parseInt(time[2]));
		
	long ms = c.getTimeInMillis();
	return ms;
    }
	*/
    /** 
	To calculate some information for dummy test
	number of distinct RCPT and emails with attachment.
	@param The dummy array
	@return The arrays of Distince RCPT and Attachment
    */
    public final double[][] calculateDummy(String[][] dummy){
	double[][] sequence = new double[dummy.length][3];
	Vector ALList = new Vector(dummy.length);
	Vector ALList2 = new Vector(dummy.length);
	Vector ALList3 = new Vector(dummy.length);
	Vector ALList_collect = new Vector(dummy.length);
	Hashtable ALList2_collect = new Hashtable();
	Hashtable m_ALList = new Hashtable();//.clear();
	m_ALDist = null;//.clear();
	int cnt = 0, cnt2 = 0, cnt3 = 0;
	String last = new String();
	int firstSpan = 30;//analysis size, 30 is a good choice

	//the second analysis size, maybe it should be smaller.
	int secondSpan = 50;
	for (int i=0; i<dummy.length; i++){ 
			
	    ///plot distinct per 'firstSpan'
	    if (i<firstSpan){  
		ALList.add(0,dummy[i][0]);     
		if (!(ALList_collect.contains(dummy[i][0]))){  
		    ALList_collect.addElement(dummy[i][0]);  
		}
	    }
	    else{  
		last = (String) ALList.elementAt(firstSpan-1);
		ALList.remove(firstSpan-1);
		ALList.add(0,dummy[i][0]);                           
		if (!ALList.contains(last)){  
		    ALList_collect.remove(last);
		}
		if (!(ALList_collect.contains(dummy[i][0]))){  
		    ALList_collect.addElement(dummy[i][0]); 
		}
	    }
	    cnt = ALList_collect.size();
	    sequence[i][0]= cnt;
			
	    ///plot  distince per 100, and with attachment per 100
	    //use secondSpan
	    if (i<secondSpan){  
		ALList2.add(0,dummy[i][0]);
		if ((new Integer(dummy[i][1])).intValue()>0){ 
		    cnt3++;
		}
		if (!(ALList2_collect.containsKey(dummy[i][0]))){  
		    ALList2_collect.put(dummy[i][0],"");  
		}
	    }
	    else{  
		last = (String) ALList2.elementAt(secondSpan-1);
		ALList2.remove(secondSpan-1);
		ALList2.add(0,dummy[i][0]);          
		if (!ALList2.contains(last)){  
		    ALList2_collect.remove(last);
		}
		if (!(ALList2_collect.containsKey(dummy[i][0]))){  
		    ALList2_collect.put(dummy[i][0],"");
		}
		if (Integer.parseInt(dummy[i][1]) > 0 ){  
		    cnt3++;
		}
		if (Integer.parseInt(dummy[i-SPAN3][1]) > 0 ){  
		    cnt3--;
		}
	    }
			
	    cnt2 = ALList2_collect.size();
	    sequence[i][1] = cnt2;
	    sequence[i][2] = cnt3;
	}
	return sequence;
    }
	
    /**
     * this method is the same to calculateHellinger, just with
     * different parameter.
     *
     * @param hDLength the h d length
     * @param Data the data
     * @return the vector
     */
    public final Vector calculateHell(int hDLength,String[][] Data){
	Vector distance = new Vector();
	SimpleHashtable simpleHash = new SimpleHashtable( );
		
	int allDataSize=Data.length;
	double trainFreq = 1.0/((double)hDLength*4);
	double testFreq = 1.0/(double)hDLength;
	int start = 0;			   	
	double sum = 0;
		
	if (hDLength*5 > allDataSize)
	    return null;
		
	//build hash table for data 0 to defaultSize*5
	for(int i=0; i<hDLength*5; i++){
	    String name = (String)Data[i][0];
	    if (i<hDLength*4){
		simpleHash.insert(name,"train",trainFreq,0);
	    }
	    else{
		simpleHash.insert(name,"test",testFreq, 0);
	    }
	    if (i <hDLength*5-1)
		distance.addElement(new Double(0.0));
	}
		
	//compute distance in the first term
	Vector theKey = simpleHash.getKeys();
	for (int i=0; i<theKey.size(); i++){
	    String name = (String)theKey.get(i); 
	    double train = simpleHash.getTrain(name);
	    double test = simpleHash.getTest(name);
	    sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	}
	distance.addElement(new Double(sum));// first term's distance
		
		
	//build hash table for all of terms and compute distances
	//total runing times
	for (int i=hDLength*5; i<Data.length; i++){
	    double summ =0;
	    //cut first for training
	    String name =Data[start][0];
	    simpleHash.remove(name, "train", trainFreq);
			
	    //add last for training
	    name = Data[start+hDLength*4][0];
	    simpleHash.insert(name, "train", trainFreq,0);
			
	    //cut first for test
	    name = Data[start+hDLength*4][0];
	    simpleHash.remove(name, "test", testFreq);
	    //add last for test
	    name = Data[start+hDLength*5][0];
	    simpleHash.insert(name, "test", testFreq,0 );
			
	    //compute distance
	    theKey.clear();
	    theKey = simpleHash.getKeys();
	    for (int j=0; j<theKey.size(); j++){
		String tag =(String ) theKey.get(j);
		double train = simpleHash.getTrain(tag);
		double test = simpleHash.getTest(tag);
				
		summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	    }
			
	    distance.addElement(new Double(summ)); 
	    start++;
	}
		
	return distance;
    }
	
    /**
     * for hellinger distance button.
     */
    //  class hellingerListener implements ActionListener{
    //	public void actionPerformed(ActionEvent e){
    public final void hellingerListener()
    {
	ChiOrCSC = true;
	emptySet=false;
	m_Button =HELLINGER;
	//before=System.currentTimeMillis();
	System.out.println("working...");
	int defaultSize = Integer.parseInt(hellingerSizeText.getText());
	//tmpHellingerList = new Vector();
	tmpHellingerList = calculateHellinger(defaultSize);
	try{
	    //test the size of data first
	    if (tmpHellingerList != null){
		(new Thread(new Runnable(){
			public void run(){
			    working.progress(0,3);
			    working.setVisible(true);
			    m_winGui.setEnabled(false);//block the window
			    runHellinger();
			    working.progress(1,3);
			    m_panel.remove(m_panel.getComponentCount()-1);
			    m_constraints.gridx = 0;
			    m_constraints.gridy = 3;
			    m_constraints.insets = new Insets(3,3,3,3);
			    m_gridbag.setConstraints(m_panel1, m_constraints);
			    m_panel.add(m_panel1);
			    working.progress(2,3);
			    setViewportView(m_panel);
			    m_winGui.setEnabled(true);
			    working.setVisible(false);
			    //System.gc();
			}
		    })).start();
					
	    }else {
		m_Button = PROFILE;
		String msg = "Data are not enough for Hellinger Distance";
		msg += "\nGo to profile";
		System.out.println(msg);
		JOptionPane.showMessageDialog(RcptFrequency.this,msg);
		profileListener();
		//m_buttonProfile.doClick();
	    }
	}
	catch(Exception ee){
	    System.out.println("test warn2:"+ee);
	}
    }
    //   }
	
    /**
     * implement the action.
     */
    public final void runHellinger(){
	try{  
	    System.out.println("hellinger OK"+tmpHellingerList.size());
	    hellingerDistance=new double[tmpHellingerList.size()];
	    for (int i=0; i<tmpHellingerList.size(); i++){
		hellingerDistance[i]=((Double)tmpHellingerList.get(i)).doubleValue();
	    }
	    if (m_Button==PROFILE){	
		//m_Button =3;
		displayProfile();
	    }else{ 
		//m_Button=3;
		calculateProfile();
		m_ALDist = null;//.clear();// = new Vector();
		calculateFreq();
		//m_rcptFreq.getColumnModel().getColumn(1).setMaxWidth(60);
		displayProfile();
	    }
	}
	catch(Exception e){
	    System.out.println("warning 1:"+e);
	}
	finally{}
    }
	
    /**
     * when selecting the Chi Square page.
     */
    //class chisquareListener implements ActionListener{
    //	public void actionPerformed(ActionEvent e){
    public final void chisquareListener(){

	ChiOrCSC = true;
	emptySet=false;
	m_Button=CHI_SQUARE;
	if(isoutbound)labRcpt2.setText(MSG_TERRDIFF);
	else labRcpt2.setText(MSG_TESRDIFF);
	//before=System.currentTimeMillis();
	//System.out.println("working...");
			
	try{
	    //test the size of data first
	    if (m_nbRecords.intValue()>CHI_MIN){
		m_panThreshold1.remove(m_labRcpt1b);
		m_panThreshold1.remove(m_threshold2);
		m_panThreshold1.add(m_labRcpt1a); 

		(new Thread(new Runnable(){
			public void run(){
			    working.progress(0,3);
			    working.setVisible(true);
			    m_winGui.setEnabled(false);//block the window
			    runChisquare();
			    working.progress(1,3);
			    m_panel.remove(m_panel.getComponentCount()-1);
			    m_constraints.gridx = 0;
			    m_constraints.gridy = 3;
			    m_constraints.insets = new Insets(3,3,3,3);
			    m_gridbag.setConstraints(m_panel2, m_constraints);
			    m_panel.add(m_panel2);
			    working.progress(2,3);
			    setViewportView(m_panel);
			    m_winGui.setEnabled(true);
			    working.setVisible(false);
			//    System.gc();

			}
		    })).start();
	    }
	    else{//not enough data
		String msg = "Not enough records for Chi Square Test";
		msg += "\nGo to profile";
		JOptionPane.showMessageDialog(RcptFrequency.this,msg);
		profileListener();
		//m_buttonProfile.doClick();
		//m_Button = PROFILE;
	    }
	}
	catch(Exception ee){
	    System.out.println("test warn1:"+ee);
	}
			
    }
	
    /**
     * Chi Square
     * implement the action.
     */
    public final void runChisquare(){
	try{  
	    //setup the range first, move part from runCombouser
	    /******************************/
	    countRecords();
			
	    //something about minimum/maximum
	    int min10 = m_nbRecords.intValue()-CHI_DEFAULT2;
	    int max10 = m_nbRecords.intValue()-CHI_DEFAULT-1;
	    int min20 = m_nbRecords.intValue()-CHI_DEFAULT;
	    int max20 = m_nbRecords.intValue()-1;
	    if (min10<0) {  min10=0; }
	    if ((max10<0)||(max10<CHI_DEFAULT2/2)){
		max10=m_nbRecords.intValue()/2;
		min20=max10+1;
	    }
	    if (min20<0) {  min20=0; }
	    if (max20<0) {  max20=10; }
	    m_min1.setText(""+min10);//(new Integer(min10)).toString());
	    m_max1.setText(""+max10);//(new Integer(max10)).toString());
	    m_min2.setText(""+min20);//(new Integer(min20)).toString());
	    m_max2.setText(""+max20);//(new Integer(max20)).toString());
	    /******************************/


	    //training and testing range
	    int min1 = min10;//Integer.parseInt(m_min1.getText());
	    int max1 = max10;//Integer.parseInt(m_max1.getText());
	    int min2 = min20;//Integer.parseInt(m_min2.getText());
	    int max2 = max20;//Integer.parseInt(m_max2.getText());
	    //difference threshold
	    m_thres = Double.parseDouble(m_threshold.getText());
	    

	    calculateProfile();
	    m_ALDist1.clear();// = new Vector();
	    calculateFreqVar(ALD_MODEL_1,min1,max1-1);
	    m_ALDist2.clear();// = new Vector();
	    calculateFreqVar(ALD_MODEL_2,min2,max2-1);  
	    insertFreq();
	    calculateChisquare();
	    
	    m_rcptFreq1.getColumnModel().getColumn(1).setMaxWidth(60);
	    m_rcptFreq2.getColumnModel().getColumn(1).setMaxWidth(60);
	    m_rcptFreq2.getColumnModel().getColumn(2).setMaxWidth(60); 
	    (m_rcptFreq2.getColumn(MSG_DIFF)).setCellRenderer(m_diffColumnRenderer);

	    displayChisquare();
	}
	catch(Exception e){
	    System.out.println("warning 2:"+e);
	}
	finally {}
		
    }
	
    /**
     * when selecting the CS + cliques page.
     */
    //    class chisquareListener2 implements ActionListener{
    //	public void actionPerformed(ActionEvent e){
    public final void chisquareListener2()
    {

	try{
	labRcpt2.setText(MSG_TERRDIFF);
	m_Button=CHI_CLIQUE;
	//before=System.currentTimeMillis();
	//System.out.println("working...");
			
	//test the size of data first
	if (m_nbRecords.intValue()>CHI_MIN){
	    ChiOrCSC = false;
	    if(!haveclique)
		{
		   // BusyWindow bw = new BusyWindow(RcptFrequency.this,"Patience","Setting up the clique list....only done once");
		    //bw.show();
		    System.out.println("updating cliques..might take a few");
		    haveclique = true;
		    createCliqueList(50,5);
		    //bw.hide();
		}
		
	    boundArg = "out-bound";
	    setOutBound();
	    emptySet=false;
	    m_panThreshold1.remove(m_labRcpt1a);
	    m_panThreshold1.add(m_labRcpt1b);
	    m_panThreshold1.add(m_threshold2); 
				
	    (new Thread(new Runnable(){
		    public void run(){
			working.progress(0,3);
			working.setVisible(true);
			m_winGui.setEnabled(false);//block the window
			runChisquare2();
			working.progress(1,3);
			m_panel.remove(m_panel.getComponentCount()-1);  
			m_constraints.gridx = 0;
			m_constraints.gridy = 3;
			m_constraints.insets = new Insets(3,3,3,3);
			m_gridbag.setConstraints(m_panel2, m_constraints);
			m_panel.add(m_panel2);
			working.progress(2,3);
			setViewportView(m_panel);
			m_winGui.setEnabled(true);
			//?????
			//inbound.setEnabled(false);
			//outbound.setEnabled(false);
			//attachment.setEnabled(false);
			    
			working.setVisible(false);
			//System.gc();
		    }
		})).start();
				
	}
	else{//not enough data
	    String msg = "Not enough records for Chi Square Test\nGo to profile";
	    JOptionPane.showMessageDialog(RcptFrequency.this,msg);
	    profileListener();
	    //m_buttonProfile.doClick();
	}
	}
	catch(Exception e){
	    System.out.println("warning chi2: "+e);
	}
    }
 
	
    /**
     * Chi Square + cliques
     * implement the action.
     */
    public final void runChisquare2(){
	try{   

	    //setup the range first, move part from runCombouser
	    /******************************/
	    countRecords();
			
	    //something about minimum/maximum
	    int min10 = m_nbRecords.intValue()-CHI_DEFAULT2;
	    int max10 = m_nbRecords.intValue()-CHI_DEFAULT-1;
	    int min20 = m_nbRecords.intValue()-CHI_DEFAULT;
	    int max20 = m_nbRecords.intValue()-1;
	    if (min10<0) 
		{  min10=0; }
	    if ((max10<0)||(max10<CHI_DEFAULT2/2)){
		max10=m_nbRecords.intValue()/2;
		min20=max10+1;
	    }
	    if (min20<0) {  min20=0; }
	    if (max20<0) {  max20=10; }
	    m_min1.setText(""+min10);
	    m_max1.setText(""+max10);//(new Integer(max10)).toString());
	    m_min2.setText(""+min20);//(new Integer(min20)).toString());
	    m_max2.setText(""+max20);//(new Integer(max20)).toString());
	    /******************************/

	    //training and testing range
	    int min1 = min10;//Integer.parseInt(m_min1.getText());
	    int max1 = max10;//Integer.parseInt(m_max1.getText());
	    int min2 = min20;//Integer.parseInt(m_min2.getText());
	    int max2 = max20;//Integer.parseInt(m_max2.getText());
			
	    //clique and difference threshold
	    m_thres = Double.parseDouble(m_threshold.getText());
	    m_thres2 = Integer.parseInt(m_threshold2.getText());
	    calculateProfile();
			
	    //for both training and testing table
	    m_ALDist1.clear();// = new Vector();
	    calculateFreqVarClique(ALD_MODEL_1,min1,max1-1);
	    m_ALDist2.clear();// = new Vector();
	    calculateFreqVarClique(ALD_MODEL_2,min2,max2-1);  
	    insertFreq();
	    calculateChisquare();
	    m_rcptFreq1.getColumnModel().getColumn(1).setMaxWidth(60);
	    m_rcptFreq2.getColumnModel().getColumn(1).setMaxWidth(60);
	    m_rcptFreq2.getColumnModel().getColumn(2).setMaxWidth(60); 
	    ((TableColumn)(m_rcptFreq1.getColumn(MSG_RA))).setCellRenderer(m_diffColumnRenderer2);
	    ((TableColumn)(m_rcptFreq2.getColumn(MSG_RA))).setCellRenderer(m_diffColumnRenderer2);
	    ((TableColumn)(m_rcptFreq2.getColumn(MSG_DIFF))).setCellRenderer(m_diffColumnRenderer);
	    displayChisquare();
	}
	catch(Exception e){
	    System.out.println("warning 3:"+e);
	}
	finally {}
    }
	
    /*
     * recalculate current algorithm again
     * it's for Chi Square and Chi Square + Cliques only
     */
    //    class runListener implements ActionListener{
    //	public void actionPerformed(ActionEvent e){
    /**
     * Run listener.
     */
    public final void runListener()
    {	
	(new Thread(new Runnable(){
		public void run(){
		    	
		    if(m_Button==CHI_SQUARE || m_Button==CHI_CLIQUE){
		    	working.progress(0,3);
			working.setVisible(true);
			m_winGui.setEnabled(false);
			runRun();
			working.progress(1,3);
			  m_panel.remove(m_panel.getComponentCount()-1);  
			//m_panel.removeAll();
			//m_panel.repaint();
			//m_panel.add(mainP);
			m_constraints.gridx = 0;
			m_constraints.gridy = 3;
			m_constraints.insets = new Insets(3,3,3,3);
			m_gridbag.setConstraints(m_panel2, m_constraints);
			m_panel.add(m_panel2);
			working.progress(2,3);
			setViewportView(m_panel);
			m_winGui.setEnabled(true);
			if(m_Button==CHI_SQUARE){
				//m_buttonChisquare.setEnabled(false);
			}
			else if(m_Button==CHI_CLIQUE){
				//inbound.setEnabled(false);
				//outbound.setEnabled(false);
				//attachment.setEnabled(false);
				//m_buttonChisquare2.setEnabled(false);
			}
			working.setVisible(false);
//			System.gc();
			//after=System.currentTimeMillis();
			//System.out.print("completed in ");
			//System.out.print((after-before)/1000.0);
			//System.out.println(" secs");
		    }
		    
		}//if(m_Button==CHI_SQUARE || m_Button==CHI_CLIQUE){
	    })).start();
    }
    //   }
	
    /**
     * implement the action.
     */
    public final void runRun(){
	try{  
	    //before=System.currentTimeMillis();
	    //System.out.println("working...");
	    if(!haveclique)
		{
		    System.out.println("updating cliques..might take a few");
		    haveclique = true;
		    createCliqueList(50,5);
		}
			
	    //test the size of data first
	    if (m_nbRecords.intValue()>CHI_MIN){
		//the range and threshold
		int min1 = Integer.parseInt(m_min1.getText());
		int max1 = Integer.parseInt(m_max1.getText());
		int min2 = Integer.parseInt(m_min2.getText());
		int max2 = Integer.parseInt(m_max2.getText());
		m_thres = Double.parseDouble(m_threshold.getText());
		int thres_old = m_thres2;
		m_thres2 = Integer.parseInt(m_threshold2.getText());
		if (!(m_thres2==thres_old))
		    {  
			createCliqueList(m_thres2,5);//fixed hardcoded 50 here 
		    }
		if (min1> m_nbRecords.intValue()-1) 
		    {  min1 = m_nbRecords.intValue()-CHI_DEFAULT2;  }
		if (max1> m_nbRecords.intValue()-1) 
		    {  max1 = m_nbRecords.intValue()-CHI_DEFAULT-1; }
		if (min2> m_nbRecords.intValue()-1) 
		    {  min2 = m_nbRecords.intValue()-CHI_DEFAULT;  }
		if (max2> m_nbRecords.intValue()-1) 
		    {  max2 = m_nbRecords.intValue()-1;  }	
		if (min1>max1) {  min1=max1-1;   }
		if (min2>max2) {  min2=max2-1;   }
		if (min1<0) {  min1=0; }
		if (min2<0) {  min2=0; }
		if (max1<0) {  max1=10; }
		if (max2<0) {  max2=10; }
		m_min1.setText(""+min1);//(new Integer(min1)).toString());
		m_max1.setText(""+max1);//(new Integer(max1)).toString());
		m_min2.setText(""+min2);//(new Integer(min2)).toString());
		m_max2.setText(""+max2);//(new Integer(max2)).toString());
		calculateProfile();
				
		if (m_Button==CHI_SQUARE){//chi Square
		    m_ALDist1.clear();// = new Vector();
		    calculateFreqVar(ALD_MODEL_1,min1,max1-1);
		    m_ALDist2.clear();// = new Vector();
		    calculateFreqVar(ALD_MODEL_2,min2,max2-1);
		}
		if (m_Button==CHI_CLIQUE){//plus Clique
		    m_ALDist1.clear();// = new Vector();
		    calculateFreqVarClique(ALD_MODEL_1,min1,max1-1);
		    m_ALDist2.clear();// = new Vector();
		    calculateFreqVarClique(ALD_MODEL_2,min2,max2-1);
		}
		insertFreq();
		calculateChisquare();
		m_rcptFreq1.getColumnModel().getColumn(1).setMaxWidth(60);
		m_rcptFreq2.getColumnModel().getColumn(1).setMaxWidth(60);
		m_rcptFreq2.getColumnModel().getColumn(2).setMaxWidth(60);
				
		//change some labels
		if(!ChiOrCSC){
		    ((TableColumn)(m_rcptFreq1.getColumn(MSG_RA))).setCellRenderer(m_diffColumnRenderer2);
		    ((TableColumn)(m_rcptFreq2.getColumn(MSG_RA))).setCellRenderer(m_diffColumnRenderer2);
		}
		else{
		    ((TableColumn)(m_rcptFreq1.getColumn(RorSaddress))).setCellRenderer(m_diffColumnRenderer2);
		    ((TableColumn)(m_rcptFreq2.getColumn(RorSaddress))).setCellRenderer(m_diffColumnRenderer2);
		}
		((TableColumn)(m_rcptFreq2.getColumn(MSG_DIFF))).setCellRenderer(m_diffColumnRenderer);
				
		displayChisquare();
	    }
	    else{//not enough data
		String msg = "Not enough records for Chi Square Test\nGo to profile";
		JOptionPane.showMessageDialog(RcptFrequency.this,msg);
		//m_buttonProfile.doClick();
		m_Button = PROFILE;
	    }
	    //after=System.currentTimeMillis();
	    //System.out.print("completed in ");
	    //System.out.print((after-before)/1000.0);
	    //System.out.println(" secs");
	    //bw.hide();
	}
	catch(Exception e){
	    System.out.println("warning 4:"+e);
	}
	finally {}
    }
	
    /**
     * when selecting Profile page.
     */
    //   class profileListener implements ActionListener{
    //	public void actionPerformed(ActionEvent e){
    public final void profileListener()
    {
	ChiOrCSC = true;
	emptySet=false;
			
	m_Button=PROFILE;
	
	(new Thread(new Runnable(){
		public void run(){
		    working.progress(0,3);
		    working.setVisible(true);
		    m_winGui.setEnabled(false);
		    runProfile();
		    working.progress(1,3);
		    m_panel.remove(m_panel.getComponentCount()-1);
		    m_constraints.gridx = 0;
		    m_constraints.gridy = 3;
		    m_gridbag.setConstraints(m_panel1, m_constraints);
		    m_panel.add(m_panel1);
		    m_constraints.insets = new Insets(3,3,3,3);
		    
		    working.progress(2,3);
		    setViewportView(m_panel);
		    
		    m_winGui.setEnabled(true);
		    System.gc();
		    working.setVisible(false);
		}
	    })).start();
    }
	
    /**
     * profile
     * implement the action.
     */
    public final void runProfile(){
	//System.out.println("in run profile");
	try{ 
	    //if(!startup){
	    m_ALDist = null;
	    ChiOrCSC = true;
	    calculateProfile();
	    //m_ALDist = null;//.clear();// = new Vector();
	    calculateFreq();
	    countRecords();
	    //m_rcptFreq.getColumnModel().getColumn(1).setMaxWidth(60);
	    displayProfile();
	    //}
	//else startup = false;
	}
	catch(Exception e){
	    System.out.println("warning Addreslist - 5:"+e);
	}

	//finally {
	//    System.out.println("done profile");
	//}
    }
	
    /**
     * the action for comboUser.
     */
    //    class comboUserListener implements ActionListener{
    //	public void actionPerformed(ActionEvent event){ 
    /*    
	  public final void comboUserListener()
	  {
	  emptySet=false;
	  //before=System.currentTimeMillis();
	  //System.out.println("working...");
	  (new Thread(new Runnable(){
	  public void run(){
	  countRecords();
	  //working.show();
	  /*
	    m_winGui.setEnabled(false);
	    runCombouser();
	    m_winGui.setEnabled(true);
	    m_panel.removeAll();
	    m_panel.add(mainP);
	    m_constraints.gridx = 0;
	    m_constraints.gridy = 3;
	    m_constraints.insets = new Insets(3,3,3,3);
	    if(m_Button==PROFILE || m_Button==HELLINGER){
	    m_gridbag.setConstraints(m_panel1, m_constraints);
	    m_panel.add(m_panel1);
	    }
	    else{
	    m_gridbag.setConstraints(m_panel2, m_constraints);
	    m_panel.add(m_panel2);
	    }
	    //setViewportView(m_panel);
	    repaint();
	    //working.hide();
	    after=System.currentTimeMillis();	 
	    System.out.print("completed in ");
	    System.out.print((after-before)/1000.0);
	    System.out.println(" secs");
	    /
	    }
	      })).start();
			
	    }
	    //    }
    */
   

    public final void startupCombouser(){
	if(!startup){
	runCombouser();
	m_panel.remove(m_panel.getComponentCount()-1);
	}else startup = false;
	//m_panel.removeAll();
	//m_panel.add(mainP);
	m_constraints.gridx = 0;
	m_constraints.gridy = 3;
	//m_constraints.insets = new Insets(3,3,3,3);
	if(m_Button==PROFILE || m_Button==HELLINGER){
	    m_gridbag.setConstraints(m_panel1, m_constraints);
	    m_panel.add(m_panel1);
	}
	else{
	    m_gridbag.setConstraints(m_panel2, m_constraints);
	    m_panel.add(m_panel2);
	}
	repaint();
    }
    
    /**
     * combo box
     * implement the action.
     */
    public final void runCombouser(){
	try {
	    countRecords();

	    int min10 = 0;
	    int max10 = 0;
	    int min20 = 0;
	    int max20 = 0;

	    //something about minimum/maximum
	    if(m_nbRecords!=null){
		min10 = m_nbRecords.intValue()-CHI_DEFAULT2;
		max10 = m_nbRecords.intValue()-CHI_DEFAULT-1;
		min20 = m_nbRecords.intValue()-CHI_DEFAULT;
		max20 = m_nbRecords.intValue()-1;
	    
		if (min10<0) {  min10=0; }
		if ((max10<0)||(max10<CHI_DEFAULT2/2)){
		    max10=m_nbRecords.intValue()/2;
		    min20=max10+1;
		}
		if (min20<0) {  min20=0; }
		if (max20<0) {  max20=10; }
	    }
		
	    m_min1.setText(""+min10);//(new Integer(min10)).toString());
	    m_max1.setText(""+max10);//(new Integer(max10)).toString());
	    m_min2.setText(""+min20);//(new Integer(min20)).toString());
	    m_max2.setText(""+max20);//(new Integer(max20)).toString());

	    /** 
		different pages
		m_Button=0, profile
		m_Button=3, hellinger
		m_Button=1, chi square
		m_Button=2, chi+clique
		m_Button=4, Test
	    */
		 
	    if (m_Button==DUMMY_TEST){
		m_Button=PROFILE;
		runProfile();
		return;
	    }
			
			
	    if (m_Button==PROFILE || m_Button == HELLINGER){ 
		//if in Hellinger Distance situation
		if (m_Button ==HELLINGER){
		    int defaultSize=Integer.parseInt(hellingerSizeText.getText());
		    Vector tmp = calculateHellinger(defaultSize);		  
		    //Vector tmp = calculateHellinger(DEFAULT_SIZE);
		    //test the size of data first
		    if (tmp != null){
			System.out.println("hellinger OK"+tmp.size());
						
			hellingerDistance = new double[tmp.size()];
			for (int i=0; i<tmp.size(); i++){
			    hellingerDistance[i]=((Double)tmp.get(i)).doubleValue();
			}
			//hellingerButton.setEnabled(true);
			//m_buttonProfile.setEnabled(true);
		    }else{
			m_Button=PROFILE;
			String msg = "Data are not enough for Hellinger Distance";
			System.out.println(msg);
			JOptionPane.showMessageDialog(RcptFrequency.this,msg);
			//m_buttonProfile.doClick();
		    }
					
		}else{//m_Button==0		    
		    //m_buttonProfile.setEnabled(false);
		    //hellingerButton.setEnabled(true);
		}
		//m_buttonChisquare.setEnabled(true);
		//m_buttonChisquare2.setEnabled(true);
		calculateProfile();
		m_ALDist = null;//.clear();// = new Vector();
		calculateFreq();
		//m_rcptFreq.getColumnModel().getColumn(1).setMaxWidth(60);
		displayProfile();
	    }
	    else{//chi square or chi+clique
		if (m_nbRecords.intValue()>CHI_MIN){
		    calculateProfile();
		    int min1 = Integer.parseInt(m_min1.getText());
		    int max1 = Integer.parseInt(m_max1.getText());
		    int min2 = Integer.parseInt(m_min2.getText());
		    int max2 = Integer.parseInt(m_max2.getText());
		    m_thres = Double.parseDouble(m_threshold.getText());  
		    m_thres2 = Integer.parseInt(m_threshold2.getText()); 
		    if (m_Button==CHI_SQUARE){
			m_ALDist1.clear();// = new Vector();
			calculateFreqVar(ALD_MODEL_1,min1,max1-1);
			m_ALDist2.clear();// = new Vector();
			calculateFreqVar(ALD_MODEL_2,min2,max2-1);
		    }
		    if (m_Button==CHI_CLIQUE){
			//outbound.doClick();
			m_ALDist1.clear();// = new Vector();
			calculateFreqVarClique(ALD_MODEL_1,min1,max1-1);
			m_ALDist2.clear();// = new Vector();
			calculateFreqVarClique(ALD_MODEL_2,min2,max2-1);
		    }
		    insertFreq();
		    calculateChisquare();
		    m_rcptFreq1.getColumnModel().getColumn(1).setMaxWidth(60);
		    m_rcptFreq2.getColumnModel().getColumn(1).setMaxWidth(60);
		    m_rcptFreq2.getColumnModel().getColumn(2).setMaxWidth(60);
		    ((TableColumn)(m_rcptFreq1.getColumn(RorSaddress))).setCellRenderer(m_diffColumnRenderer2);
		    ((TableColumn)(m_rcptFreq2.getColumn(RorSaddress))).setCellRenderer(m_diffColumnRenderer2);
		    ((TableColumn)(m_rcptFreq2.getColumn(MSG_DIFF))).setCellRenderer(m_diffColumnRenderer);
		    displayChisquare();
		}
		else{  
		    String msg = "Not enough records for Chi Square Test";
		    System.out.println(msg);
		    JOptionPane.showMessageDialog(RcptFrequency.this,msg);
		    //m_buttonProfile.setEnabled(false);
		    //m_buttonChisquare.setEnabled(true);
		    //m_buttonChisquare2.setEnabled(true);
		    //hellingerButton.setEnabled(true);
		    //m_buttonRun.setEnabled(false);
		    calculateProfile();
		    m_ALDist = null;//.clear();// = new Vector();
		    calculateFreq();
		    displayProfile();
		    m_Button=PROFILE;
		}
	    }//else m_Button != 0
			
	}//try
	catch(Exception e){
	    System.out.println("warning 6:"+e);
	}
	finally {}
    }
	
    /**
       the action for the inbound/outbound radio buttons
    
    
       class boundListener implements ActionListener{
       public void actionPerformed(ActionEvent e){
       boundArg = e.getActionCommand();
       runBound();
       //unblockall();
       if(m_Button==PROFILE)
       m_buttonProfile.doClick();
       else if(m_Button==HELLINGER)
       hellingerButton.doClick();
       else if(m_Button==CHI_SQUARE)
       m_buttonChisquare.doClick();
       else if(m_Button==CHI_CLIQUE)
       m_buttonChisquare2.doClick();
       }
       }
    */

    /**
       in/out-bound, attachment
       implement the action
    */
    public void runBound(){
	//String arg=boundArg;
	//String user = (String)m_comboUsers.getSelectedItem();
	//String user = (String)m_winGui.getSelectedUser();
	emptySet=false;
	if(boundArg.equals("in-bound")){//in-bound
	    isoutbound = false;
	    withatt=false;
	    //reset the labels
	    labRcpt.setText("Sender list");

	    //chi aquare
	    if(ChiOrCSC){
		labRcpt2.setText(MSG_TESRDIFF);
	    }
	    //with clique
	    else{
		labRcpt2.setText(MSG_TERRDIFF);
	    }
	    m_labRcpt1a.setText(MSG_TRRSL);
	    RorSaddress = MSG_SADD;
	}
	else if(boundArg.equals("out-bound")){//out-bound
	    isoutbound = true;
	    withatt=false;
	    //reset the labels
	    labRcpt.setText(RL);
	    labRcpt2.setText(MSG_TERRDIFF);
	    m_labRcpt1a.setText(MSG_TERRL);
	    RorSaddress = MSG_RA;
	}
	else if(boundArg.equals("attachment")){//with attachment
	    isoutbound = true;
	    withatt=true;
	    //reset the labels
	    labRcpt.setText(RL);
	    labRcpt2.setText(MSG_TERRDIFF);
	    m_labRcpt1a.setText(MSG_TERRL);
	    RorSaddress = MSG_RA;
	}
	countRecords();
    }
	
    /**
     * set the radio button at out-bound.
     */
    public final void setOutBound(){
	outbound.setSelected(true);
	boundArg = "out-bound";
	emptySet=false;
	isoutbound = true;
	withatt=false;
	//reset the labels
	labRcpt.setText(RL);
	labRcpt2.setText(MSG_TERRDIFF);
	m_labRcpt1a.setText(MSG_TERRL);
	RorSaddress = MSG_RA;
    }
	
    /**
     * calculate hellinger distance for test button.
     * It is same method but different parameter.
     *
     * @param defaultSize the default size
     * @param account the account
     * @return the vector
     */
    public final Vector calculateHellinger(int defaultSize, String account)
    {
	SimpleHashtable simpleHash = new SimpleHashtable( );
	String user = account;
	if (user == null)
	    return null;
	String query;
	String allData[][];
	Vector distance = new Vector();
		
	int allDataSize=0;
	double trainFreq = 1.0/((double)defaultSize*4);
	double testFreq = 1.0/(double)defaultSize;
	int start = 0;			   	
	double sum = 0;
		
	if (!(m_jdbc == null))     
	    {  
		try{ 
		    String[] date = m_winGui.getDateRange_toptoolbar();
		    if(isoutbound || (!ChiOrCSC))
			{
			    query= "select rcpt from " + table + " "
				+ "where sender='" + user 
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+ "' order by dates, times";
			    if(withatt){
				query="select rcpt from "+table
				    +" where sender='"+user
				    + "' and DaTES>'"+date[0]
				    +"' and DaTES<'"+date[1]
				    +"' and numattach<>0 "
				    +"order by dates, times";
			    }
			}else{
			    query= "select sender from " + table + " "
				+ "where rcpt='" + user
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+"' order by dates, times";
			}			
				
		    
	    
		    synchronized (m_jdbc){
		    	allData = m_jdbc.getSQLData(query);
	//		allData=m_jdbc.getRowData();//get all data	       
		    }
		    allDataSize = allData.length;//get all data size	
		    emptySet=false;
				
		    if(allDataSize!=0){
			if (defaultSize*5 > allDataSize)
			    return null;
					
			//build hash table for data 0 to defaultSize*5
			for(int i=0; i<defaultSize*5; i++)
			    {
				String name = allData[i][0];
				if (i<defaultSize*4)
				    {
					simpleHash.insert(name,"train",trainFreq,0);
				    }else{
					simpleHash.insert(name,"test",testFreq, 0);
				    }
				if (i < defaultSize*5-1)
				    distance.addElement(new Double(0.0));
						
			    }
			//compute distance in the first term
			Vector theKey = simpleHash.getKeys();
			for (int i=0; i<theKey.size(); i++)
			    {
				String name = (String)theKey.get(i); 
				double train = simpleHash.getTrain(name);
				double test = simpleHash.getTest(name);
				sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
			    }
			distance.addElement(new Double(sum));// first term's distance
					
			//build hash table for all of terms and compute distances			
			for (int i=defaultSize*5; i< allData.length; i++)//total runing times
			    {
				double summ =0;
				//cut first for training
				String name =(String ) allData[start][0];
				simpleHash.remove(name, "train", trainFreq);
						
				//add last for training
				name = (String)  allData[start+defaultSize*4][0];
				simpleHash.insert(name, "train", trainFreq,0);
						
				//cut first for test
				name = (String )allData[start+defaultSize*4][0];
				simpleHash.remove(name, "test", testFreq);
				//add last for test
				name = (String )allData[start+defaultSize*5][0];
				simpleHash.insert(name, "test", testFreq,0 );
						
				//compute distance
				theKey.clear();
				theKey = simpleHash.getKeys();
				for (int j=0; j<theKey.size(); j++)
				    {
					String tag =(String ) theKey.get(j);
					double train = simpleHash.getTrain(tag);
					double test = simpleHash.getTest(tag);
							
					summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
				    }
						
				distance.addElement(new Double(summ)); 
				start++;
			    }
		    }else{
			System.out.println("calculate Hellinger: empty data set");
			emptySet=true;
		    }
		}catch (SQLException ex){
		    System.out.println("warning 7: "+ex);
		}			
	    }
	return distance;
    }   
	
	
    /**
     * calculate hellinger distance.
     *
     * @param defaultSize the default size
     * @return the vector
     */
    public final Vector calculateHellinger(int defaultSize)
    {
	SimpleHashtable simpleHash = new SimpleHashtable( );
	//Object selected = m_comboUsers.getSelectedItem();
	//if (selected == null)
	//    return null;
		
	//String user = (String)m_comboUsers.getSelectedItem();
	String user = (String)m_winGui.getSelectedUser();
	if (user == null)
	    return null;
		
	String query;
	String allData[][];
	Vector distance = new Vector();
		
	int allDataSize=0;
	double trainFreq = 1.0/((double)defaultSize*4);
	double testFreq = 1.0/(double)defaultSize;
	int start = 0;			   	
	double sum = 0;
	//ChiOrCSC = true;
		
	if (!(m_jdbc == null))     
	    {  
		try{ 
		    String[] date = m_winGui.getDateRange_toptoolbar();
		    if(isoutbound || (!ChiOrCSC))
			{
			    query= "select rcpt from " + table + " "
				+ "where sender='" + user
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+"' order by dates, times";
			    if(withatt){
				query="select rcpt from "+table
				    +" where sender='"+user
				    + "' and DaTES>'"+date[0]
				    +"' and DaTES<'"+date[1]
				    +"' and numattach<>0 "
				    +"order by dates, times";
			    }
			}else{
			    query= "select sender from " + table + " "
				+ "where rcpt='" + user
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+"' order by dates, times";
			}			
		    	    
		    synchronized (m_jdbc)
			{
	
		    	allData = m_jdbc.getSQLData(query);
			    //allData=m_jdbc.getRowData();//get all data	       
			}
		    allDataSize = allData.length;//get all data size	
		    emptySet=false;
				
		    if(allDataSize!=0){
			if (defaultSize*5 > allDataSize)
			    return null;
					
			//build hash table for data 0 to defaultSize*5
			for(int i=0; i<defaultSize*5; i++)
			    {
				String name = allData[i][0];
				if (i<defaultSize*4)
				    {
					simpleHash.insert(name,"train",trainFreq,0);
				    }else{
					simpleHash.insert(name,"test",testFreq, 0);
				    }
				if (i < defaultSize*5-1)
				    distance.addElement(new Double(0.0));
						
			    }
			//compute distance in the first term
			Vector theKey = simpleHash.getKeys();
			for (int i=0; i<theKey.size(); i++)
			    {
				String name = (String)theKey.get(i); 
				double train = simpleHash.getTrain(name);
				double test = simpleHash.getTest(name);
				sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
			    }
			distance.addElement(new Double(sum));// first term's distance
					
			//build hash table for all of terms and compute distances			
			for (int i=defaultSize*5; i< allData.length; i++)//total runing times
			    {
				double summ =0;
				//cut first for training
				String name =allData[start][0];
				simpleHash.remove(name, "train", trainFreq);
						
				//add last for training
				name = allData[start+defaultSize*4][0];
				simpleHash.insert(name, "train", trainFreq,0);
						
				//cut first for test
				name = allData[start+defaultSize*4][0];
				simpleHash.remove(name, "test", testFreq);
				//add last for test
				name = allData[start+defaultSize*5][0];
				simpleHash.insert(name, "test", testFreq,0 );
						
				//compute distance
				theKey.clear();
				theKey = simpleHash.getKeys();
				for (int j=0; j<theKey.size(); j++)
				    {
					String tag =(String ) theKey.get(j);
					double train = simpleHash.getTrain(tag);
					double test = simpleHash.getTest(tag);
							
					summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
				    }
						
				distance.addElement(new Double(summ)); 
				start++;
			    }
		    }
		    else{
			System.out.println("calculate Hellinger: empty data set");
			emptySet=true;
		    }
		}catch (SQLException ex){
		    System.out.println("warning 8: "+ex);
		}			
	    }
	return distance;
    }   
	
	
    /**
       calculate when using profile page,
       also calculate some relevant arrays.
       the arrays are:
       m_ALSequence,m_ALSequence2,m_ALSequence3, and m_ALSequence5
    */
    public final void calculateProfile()
    { 
	//	System.out.println("calc profile");
	//	Object selected =m_comboUsers.getSelectedItem();
	
	String user =(String)m_winGui.getSelectedUser();  

	//String user =(String) m_comboUsers.getSelectedItem();;    
	
	//	if (user == null) {  
	//  return; 
	//}
	//System.out.println(user);
	Vector ALList2 = new Vector();
	Vector ALList3 = new Vector();
	Vector ALList5 = new Vector();
	Hashtable ALList2_collect = new Hashtable();
	Hashtable ALList3_collect = new Hashtable();  
	String query;      
	String data[][];    
	//changed the follwing variables to regular ints from Integer classes
	int cnt ,cnt2, cnt3,cnt5;
	cnt=cnt2=cnt3=cnt5=0;
	String last = new String();	
	//sh m_ALSequence.clear();
	//sh m_ALSequence2.clear();
	//sh m_ALSequence3.clear();
	//sh m_ALSequence5.clear();
	Hashtable m_ALList = new Hashtable();//.clear();
	m_ALDist = null;//.clear();
	if (!(m_jdbc == null))     
	    {  try
		{
		    //test inbound/outbound
		    //weijen 2-3
		    String[] date = m_winGui.getDateRange_toptoolbar();
		    if(isoutbound || (!ChiOrCSC)){
			//if(isoutbound){
			query= "select rcpt,numattach from " + table + " "
			    + "where sender='" + user 
			    + "' and DaTES>'"+date[0]
			    +"' and DaTES<'"+date[1]
			    + "' order by "
			    +"DaTES"; 
			if(withatt){
			    query="select rcpt,numattach from "+table
				+" where sender='"+user
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+"' and numattach <> 0 order by "
				+"DaTES";
			}
		    }
		    else{
			query= "select sender,numattach from " + table + " "
			    +"where rcpt='" + user 
			    + "' and DaTES>'"+date[0]
			    +"' and DaTES<'"+date[1]
			    + "' order by "
			    +"DaTES";
		    }
		    
		    synchronized (m_jdbc){
			data = m_jdbc.getSQLData(query);
			//data=m_jdbc.getRowData();
		    }
		    dsize = data.length;
		    emptySet=false;
				
		    if(dsize!=0){//data is not empty
			m_ALSequence = new double[dsize];
			m_ALSequence2 = new double[dsize];
			m_ALSequence3 = new double[dsize];
			m_ALSequence5 = new double[dsize];
					
			//sh - here is good place to clean up our work:
			//???????????System.gc();
			//need to test it without it.
					
					
			for (int i=0; i<data.length; i++)
			    { 
				///plot1
				if (!(m_ALList.containsKey(data[i][0])))
				    { 
					m_ALList.put(data[i][0],"");  
					cnt++;
				    }
				m_ALSequence[i] = cnt;
				///plot2
				if (i<SPAN2)	    
				    {  
					ALList2.add(0,data[i][0]);     
					if (!(ALList2_collect.containsKey(data[i][0])))
					    {  ALList2_collect.put(data[i][0],"");  }
				    }
				else
				    {  
					last = (String) ALList2.elementAt(SPAN2-1);
					ALList2.remove(SPAN2-1);
					ALList2.add(0,data[i][0]);
					if (!ALList2.contains(last))  
					    {  
						ALList2_collect.remove(last);  
					    }
					if (!(ALList2_collect.containsKey(data[i][0])))
					    {  
						ALList2_collect.put(data[i][0],"");   
					    }
				    }
				cnt2 = ALList2_collect.size();
				m_ALSequence2[i]= cnt2;
				///plot2b
				if (i<SPAN3)	
				    {  
					ALList3.add(0,data[i][0]);
					if ((new Integer(data[i][1])).intValue()>0)
					    { 
						cnt5++; 
					    }
					if (!(ALList3_collect.containsKey(data[i][0])))
					    {  
						ALList3_collect.put(data[i][0],"");  
					    }
				    }
				else
				    {  
					last = (String) ALList3.elementAt(SPAN3-1);
					ALList3.remove(SPAN3-1);
					ALList3.add(0,data[i][0]);          
					if (!ALList3.contains(last))  
					    {  
						ALList3_collect.remove(last);  
					    }
					if (!(ALList3_collect.containsKey(data[i][0])))
					    {  
						ALList3_collect.put(data[i][0],"");   
					    }
					if (Integer.parseInt(data[i][1]) > 0 )
					    {  
						cnt5++;   
					    }
					if (Integer.parseInt(data[i-SPAN3][1]) > 0 )
					    {  
						cnt5--;    
					    }
				    }
				cnt3 = ALList3_collect.size();
				m_ALSequence3[i] = cnt3;
				m_ALSequence5[i] = cnt5;
						
			    }
		    }
		    else{
			    System.out.println("empty data set for this config");
			    JOptionPane.showMessageDialog(RcptFrequency.this,"Empty Data Set. This users has no data for this configuration. try again.");
			    emptySet=true;
		    }
		}
	    catch (SQLException ex){
		System.out.println("Addreslist: whynot; list of messages"+ex);
		calculateProfile();
	    }
	    catch(Exception e){
		System.out.println("warning:"+e);
	    }
	    }
    }
	
    /**
     * calculate the frequency and show them in the table.
     */
    public final void calculateFreq()
    {  
	String user =(String)m_winGui.getSelectedUser();
	String query;      
	String data[][]; 
	//    Integer cnt = new Integer(0);
	int cnt=0;
	double cnt4=0; //by shlomo to help do things fast
	//System.out.println(user);
	//vect.clear(); 
	if (m_jdbc != null)     
	    {  
		try
		{  
		    String[] date = m_winGui.getDateRange_toptoolbar();
		    //test inbound/outbound
		    if(isoutbound){
			query= "select rcpt,count(*) as cnt from " + table
			    + " where sender='" + user
			    + "' and DaTES>'"+date[0]
			    +"' and DaTES<'"+date[1]
			    +"' group by rcpt order by cnt desc";// limit 130";
			if(withatt){
			    query="select rcpt,count(*) as cnt from "+table
				+" where sender='"+user
				+ "' and DaTES>'"+date[0]
				+"' and DaTES<'"+date[1]
				+"' and numattach<>0 "
				+"group by rcpt order by cnt desc ";//limt 130
			}
		    }
		    else{
			query= "select sender,count(*) as cnt from " + table
			    + " where rcpt='" + user
			    + "' and DaTES>'"+date[0]
			    +"' and DaTES<'"+date[1]
			    +"' group by sender order by cnt desc ";//lmiit 130
		    }
		    
		    synchronized (m_jdbc)
			{
			    data = m_jdbc.getSQLDataLimited(query,130);
			    //data=m_jdbc.getRowData();
			}
		    emptySet=false;
			
		    if(data.length!=0){//data is not empty
			//count the total number	
			for (int i=0; i<data.length; i++)
			    {  
				cnt += Integer.parseInt(data[i][1]);
			    }	       
			//m_ALDist = new Rcpt[data.length];
			//calculate the frequency
		
			Vector rcptColumns = new Vector(2);
			//String rcptColumns[] = new String[2];
			//String col1 = new String(RorSaddress);
			//String col2 = new String(MSG_FREQ);
			//rcptColumns[0] = (new String(RorSaddress));
			//rcptColumns[1] = MSG_FREQ;
			rcptColumns.addElement(new String(RorSaddress));
			rcptColumns.addElement(MSG_FREQ);
			//Work HERE!!!!! some way to change the vetor underlying represention
			//m_rcptModel.setColumnIdentifiers(rcptColumns);
			
			//System.out.println("rows: " + m_rcptModel.getRowCount());
			//remove  all rows
			
			//for(int i=m_rcptModel.getRowCount();i>0;i--)
			//   m_rcptModel.removeRow(i-1);

			
			Vector temp = new Vector(data.length);
			m_ALDist = new Rcpt[data.length];
			for (int i=0; i<data.length; i++)
			    {
						
				cnt4 = ((Double.parseDouble(data[i][1]))/cnt)*100;
				//cnt4 = ((Double.parseDouble(data[i][1]))/(cnt*100));
				//shlomo compressed 2 lines into this. why create a seperate rcpt then insert?
				//
				//m_ALDist.add( new Rcpt(data[i][0],cnt4));
				m_ALDist[i] =  new Rcpt(data[i][0],cnt4);
				temp.addElement(m_ALDist[i]);
				//m_rcptModel.addRow(m_ALDist[i]);
			    }
			m_rcptModel.setDataVector(temp,rcptColumns);                
			m_rcptFreq.getColumnModel().getColumn(1).setMaxWidth(60);
		    }
		    else{
			System.out.println("calculate Freq: empty data set");
			emptySet=true;
		    }
		    
		}
	    catch (SQLException ex)   
		{
		    System.out.println("whynot; calculateFreq"+ex);
		}
	    }
    }
	
    /**
     * calculate the frequency with varience. basically, it's for chi-square
     * 'start' and 'end' are the data range
     *
     * @param model the model
     * @param start the start
     * @param end the end
     */
    public final void calculateFreqVar(int model, int start, int end){  
	String user =(String)m_winGui.getSelectedUser();  
	String query;      
	String data0[][];
	String data[][]; 
	int cnt=0;//Integer cnt = new Integer(0);
	if (start<0) {  start=0;  }
	if (start>m_nbRecords.intValue()) {  start=m_nbRecords.intValue();  }
	if (end<start) {  end=start+1;  }
	if (end>m_nbRecords.intValue()) {  end=m_nbRecords.intValue();  }
	//vect.clear(); 
	if (!(m_jdbc == null)){  
	    try{  
		//for the inbound/outbound
		String ssender;
		String srcpt;
		String[] date = m_winGui.getDateRange_toptoolbar();
		if(isoutbound || (!ChiOrCSC)){
		    ssender = MSG_SNDR;
		    srcpt = "rcpt";
		}
		else{
		    ssender = "rcpt";
		    srcpt = MSG_SNDR;
		}
		if(withatt){
		    query="select DaTES from "+table
			+" where "+ssender+"='"+user
			+ "' and DaTES>'"+date[0]
			+"' and DaTES<'"+date[1]
			+"' and numattach<>0 "
			+"order by dates, times,"+srcpt+"";
		}
		else{
		    query="select DaTES from " + table
			+ " where "+ssender+"='" + user
			+ "' and DaTES>'"+date[0]
			+"' and DaTES<'"+date[1]
			+ "' order by dates,times,"+srcpt;
		}
		
		synchronized (m_jdbc)
		    {
			data0 = m_jdbc.getSQLData(query);
	//		data0=m_jdbc.getRowData();
		    }
		emptySet=false;
		if(data0.length!=0){
		    String DaTES_start =new String(data0[start][0]);
		    String DaTES_end=new String(data0[end][0]);
		    String dates_start =new String(data0[start][0]);
		    String dates_end=new String(data0[end][0]);
		    double cnt4 =0;//shlomo
		
		    query= "select "+srcpt+",count(*) as cnt from " + table
			+ " where "+ssender+"='" + user + "' "
			+ " and DaTES>='" + DaTES_start + "' "
			+ " and DaTES<='" + DaTES_end + "' ";
		    if(withatt){
			query+="and numattach<>0";
		    }
		    query += " group by "+srcpt+" order by cnt desc";
		    
		    synchronized (m_jdbc)
			{
			   data =  m_jdbc.getSQLData(query);
			    //data=m_jdbc.getRowData();
			}
		    emptySet=false;
		    if(data.length!=0){//data is not empty
			//count the total number
			for (int i=0; i<data.length; i++){  
			    cnt += Integer.parseInt(data[i][1]);
			}	       
			//calculate the frequency
			for (int i=0; i<data.length; i++){  
			    cnt4 = (Double.parseDouble(data[i][1])/cnt)*100;
			    //cnt4 = ((Double.parseDouble(data[i][1]))/(cnt)*100);
			    if(model==1)
				m_ALDist1.addElement( new Rcpt(data[i][0],cnt4));
			    else
				m_ALDist2.addElement( new Rcpt(data[i][0],cnt4));
			}
			Vector rcptColumns = new Vector(3);
			//String col1 = new String(RorSaddress);
			//String col2 = new String(MSG_FREQ);
			rcptColumns.addElement(new String(RorSaddress));
			rcptColumns.addElement(MSG_FREQ);
			if(model==1)
			    m_rcptModel1.setDataVector(m_ALDist1,rcptColumns);
			else
			    {
				//System.out.println("weijen fix me...");
				m_rcptModel2.setDataVector(m_ALDist2,rcptColumns);
			    }
		    }
		    else{
			System.out.println("calculate FreqVar: empty data set");
			emptySet=true;
		    }
		}
		else{
		    System.out.println("calculate FreqVar: empty data set");
		    emptySet=true;
		}
				
	    }
	    catch (SQLException ex){
		System.out.println("whynot; calculateFreqVar"+ex);
	    }
	}
    }
	
    /**
     * calculate the frequency with clique.
     *
     * @param model the model
     * @param start the start
     * @param end the end
     */
    public final void calculateFreqVarClique(int model, int start,int end){  
	String user =(String)m_winGui.getSelectedUser(); 
	String query;      
	String data0[][];
	String data[][]; 
	String sender="";
	//Integer cnt = new Integer(0);
	int cnt=0;
	int nbcl=0;int nbrcpt=0;int nbcnt=0;int nbmsg=0;
	double minmsg=0; //changed to double by sh
	Vector userCliqueList = new Vector();
	Vector userCliqueRcptList = new Vector();
	m_userCliqueMember.clear();
	if (start<0) {  start=0;  }
	if (start>m_nbRecords.intValue()) {  start=m_nbRecords.intValue();  }
	if (end<start) {  end=start+1;  }
	if (end>m_nbRecords.intValue()) {  end=m_nbRecords.intValue();  }
	//vect.clear(); 
	if (!(m_jdbc == null)){  
	    try{  
		String[] date = m_winGui.getDateRange_toptoolbar();
		query="select DaTES from " + table
		    + " where sender='" + user
		    + "' and DaTES>'"+date[0]
		    +"' and DaTES<'"+date[1]
		    + "' order by dates,times,rcpt";
		
		synchronized (m_jdbc)
		    {
			data0 = m_jdbc.getSQLData(query);
			//data0=m_jdbc.getRowData();	
		 
		String DaTES_start =new String(data0[start][0]);
		String DaTES_end=new String(data0[end][0]);
		String dates_start =new String(data0[start][0]);
		String dates_end=new String(data0[end][0]);
		query= "select rcpt,count(*) as cnt from " + table
		    + " where sender='" + user + "' "
		    + " and DaTES>='" + DaTES_start + "' "
		    + " and DaTES<='" + DaTES_end + "' "
		    + " group by rcpt order by cnt desc";
		
		
			data = m_jdbc.getSQLData(query);
			//data=m_jdbc.getRowData();
		    }
		Vector w = new Vector();
		double cnt4=0;
				
		for(int i=0; i<m_enumCliqueList.size();i++){
		    Vector temp1 = (Vector)m_enumCliqueList.get(i);
		    Vector temp2 = new Vector();
		    String tmpS;
		    for(int j=0; j<temp1.size();j++){
			tmpS = (String)temp1.get(j);
			temp2.addElement(tmpS);
		    }
		    w.addElement(temp2);
		}
				
		for (int i=0;i<w.size();i++){  
		    Vector v = new Vector();
		    v =  (Vector)w.elementAt(i);
		    if (v.contains(user))
			{  userCliqueList.addElement(v);  } 
		}
		nbcl=userCliqueList.size();
				
		//calculate the clique
		for (int i=0;i<nbcl;i++){  
		    for (int j=0;j<((Vector)(userCliqueList.elementAt(i))).size();j++){  
			if (!(userCliqueRcptList.contains(((Vector)(userCliqueList.elementAt(i))).elementAt(j)))){  
			    userCliqueRcptList.addElement(((Vector)(userCliqueList.elementAt(i))).elementAt(j)); }
		    }
		}
		if (userCliqueRcptList.contains(user)){  userCliqueRcptList.removeElement(user);  }
		for (int i=0;i<nbcl;i++){  
		    if (((Vector)userCliqueList.elementAt(i)).contains(user)){  
			((Vector)userCliqueList.elementAt(i)).removeElement(user);
		    }
		}
		nbrcpt=userCliqueRcptList.size();  
		//count the total number
		for (int i=0; i<data.length; i++){  
		    cnt += Integer.parseInt(data[i][1]);
		}
		//calculate the frequency
		for (int i=0; i<data.length; i++){ 
		    cnt4 = (Double.parseDouble(data[i][1])/cnt)*100;
		    if (userCliqueRcptList.contains(data[i][0])){ 
			Rcpt rcpt2 = new Rcpt(data[i][0],Double.parseDouble(data[i][1]),0);
			userCliqueRcptList.removeElement(data[i][0]);
			userCliqueRcptList.addElement(rcpt2);
		    }
		    if(model==1)
			m_ALDist1.addElement(new Rcpt(data[i][0],cnt4));
		    else
			m_ALDist2.addElement(new Rcpt(data[i][0],cnt4));
		}
		for (int i=0;i<nbrcpt;i++){  
		    String stcl = new String();
		    String strcpt = new String();
		    stcl=userCliqueRcptList.elementAt(i).getClass().toString();
		    if (stcl.trim().equals("class java.lang.String")){  
			strcpt=(String)userCliqueRcptList.elementAt(i);
			userCliqueRcptList.removeElementAt(i);
			userCliqueRcptList.insertElementAt( new Rcpt(strcpt,2.0,0),i);
		    }
		}
		for (int i=0; i<nbrcpt; i++){  
		    nbcnt=0;
		    for (int j=0;j<nbcl;j++){   
			if (((Vector)userCliqueList.elementAt(j)).contains((((Rcpt)userCliqueRcptList.elementAt(i)).getName()))) {  nbcnt++;  }
		    }
					
		    Rcpt rcpt = new Rcpt((Rcpt)userCliqueRcptList.elementAt(i),nbcnt);
		    userCliqueRcptList.removeElementAt(i);
		    userCliqueRcptList.insertElementAt(rcpt,i);
					
		}
		for (int i=0; i<nbrcpt; i++){
		    nbmsg=(int)(CLIQUE_SHARE*((Rcpt)userCliqueRcptList.elementAt(i)).getFrequency()/((Rcpt)userCliqueRcptList.elementAt(i)).getDifference());
					
		    for (int j=0;j<nbcl;j++){   
			if (((Vector)userCliqueList.elementAt(j)).contains((((Rcpt)userCliqueRcptList.elementAt(i)).getName()))){
			    Rcpt rcpt = new Rcpt(((Rcpt)userCliqueRcptList.elementAt(i)).getName(),nbmsg);
			    ((Vector)userCliqueList.elementAt(j)).removeElement((((Rcpt)userCliqueRcptList.elementAt(i)).getName()));
			    ((Vector)userCliqueList.elementAt(j)).addElement(rcpt);
			}
		    }		  		
		}
		for (int i=0;i<nbcl;i++){  
		    minmsg=10000;
		    Vector v= (Vector)(userCliqueList.elementAt(i));
		    for (int j=0;j<v.size();j++){  
			double n = ((Rcpt)(v.elementAt(j))).getFrequency();
						
			if (n<minmsg){  minmsg=n;    }
		    }    
		}
				
		if (nbcl>0){ 
		    for (int i=0;i<nbcl;i++){  
			Vector v= (Vector)(userCliqueList.elementAt(i));
			Vector cl = new Vector();
			nbmsg=0; 
			for (int j=0;j<v.size();j++){  
			    //int n = (int)( ((Rcpt)(v.elementAt(j))).getFrequency());
			    cl.addElement(((Rcpt)(v.elementAt(j))).getName());
			    nbmsg+=(int)( ((Rcpt)(v.elementAt(j))).getFrequency());
				//n; //that means add n to current value
			}
			if (nbmsg==0){nbmsg=1;}
			String str = "clique" + Integer.toString(i+1,10);
			cl.insertElementAt(str,0);
			m_userCliqueMember.addElement(cl);
			Rcpt rcl = new Rcpt(str,((double)(100*nbmsg)/(double)(cnt)));
			int k=0;
			int tmpsize = 0;
			Rcpt tmprcpt;
			if(model==1){
			    tmpsize = m_ALDist1.size();
			    tmprcpt = (Rcpt)(m_ALDist1.elementAt(0));
			}
			else{
			    tmpsize = m_ALDist2.size();
			    tmprcpt = (Rcpt)(m_ALDist2.elementAt(0));
			}
			while((k<tmpsize)
			      &&(rcl.getFrequency()<tmprcpt.getFrequency())){
			    if(++k<tmpsize){
				if(model==1)
				    tmprcpt = (Rcpt)(m_ALDist1.elementAt(k));
				else
				    tmprcpt = (Rcpt)(m_ALDist2.elementAt(k));
			    }
			}
			if(model==1)
			    m_ALDist1.insertElementAt(rcl,k);
			else
			    m_ALDist2.insertElementAt(rcl,k);
						
		    }
		}
		if (nbcl>0){  
		    for (int i=0;i<nbrcpt;i++){ 
			nbmsg=(int)((Rcpt)userCliqueRcptList.elementAt(i)).getFrequency();
			sender=(((Rcpt)userCliqueRcptList.elementAt(i)).getName());
			for (int j=0;j<nbcl;j++){  
			    Vector v= (Vector)(userCliqueList.elementAt(j));
			    for (int k=0;k<v.size();k++){  
				if ((((Rcpt)(v.elementAt(k))).getName()).equals(sender)){ 
				    nbmsg-=(int)(((Rcpt)(v.elementAt(k))).getFrequency());
				}
			    }
			}
			Rcpt r =new Rcpt(sender,(double)(100*nbmsg)/(double)(cnt));
			int tmpsize=0;
			if(model==1)
			    tmpsize = m_ALDist1.size();
			else
			    tmpsize = m_ALDist2.size();
			for (int k=0;k<tmpsize;k++){
			    Rcpt tmprcpt;
			    if(model==1)
				tmprcpt = (Rcpt)m_ALDist1.elementAt(k);
			    else
				tmprcpt = (Rcpt)m_ALDist2.elementAt(k);
							
			    if (tmprcpt.getName().equals(sender)){
				if(model==1){
				    m_ALDist1.removeElementAt(k);
				    tmpsize = m_ALDist1.size();
				}
				else{
				    m_ALDist2.removeElementAt(k);
				    tmpsize = m_ALDist2.size();
				}
				k--;
			    }
			}
			int kk=0;
			Rcpt tmprcpt2;
			if(model==1)
			    tmprcpt2 = (Rcpt)(m_ALDist1.elementAt(0));
			else
			    tmprcpt2 = (Rcpt)(m_ALDist2.elementAt(0));
			while ((kk<tmpsize) && (r.getFrequency() < tmprcpt2.getFrequency())){
			    if(++kk<tmpsize){
				if(model==1)
				    tmprcpt2 = (Rcpt)(m_ALDist1.elementAt(kk));
				else
				    tmprcpt2 = (Rcpt)(m_ALDist2.elementAt(kk));
			    }
			}
			if(model==1)
			    m_ALDist1.insertElementAt(r,kk);
			else
			    m_ALDist2.insertElementAt(r,kk);
		    }
		}
		Vector rcptColumns = new Vector(3);
		//String col1 = new String(MSG_RA);
		//String col2 = new String(MSG_FREQ);
		rcptColumns.addElement(MSG_RA);
		rcptColumns.addElement(MSG_FREQ);
		if(model == 1)
		    m_rcptModel1.setDataVector(m_ALDist1,rcptColumns);
		else
		    {
			//			System.out.println("weijen fix me....");
			m_rcptModel2.setDataVector(m_ALDist2,rcptColumns);
		    }
	    }
	    catch (SQLException ex){
		System.out.println("whynot; calculateFreqVarClique1 "+ex);
	    }
	}
    }
	
    /**
     * Execute.
     */
    public final void Execute()
    {
    	countRecords();
    }
    
    
    /**
     * get the number of records of currently selected user.
     */
    public final void countRecords()
    { 
	
	String user =(String)m_winGui.getSelectedUser(); 
	String query;      
	String data[][] = null; 
    
	if (m_jdbc != null)     
	    {  
		String[] date = m_winGui.getDateRange_toptoolbar();
		    if(isoutbound || !ChiOrCSC){
			query = "select count(*) from " + table
			    + " where sender='" + user
			    + "' and dates>'"+date[0]    //was insertime on both lines
			    +"' and dates<'"+date[1]+"'";
			if(withatt)
			    {
			    query+="and numattach<>0 ";
			}
			
		    }
		    else{
		        query= "select count(*) from " + table
			    + " where rcpt='" + user
			    + "' and dates>'"+date[0]
			    +"' and dates<'"+date[1]+"'"; ///was inserttime on both lines
		    }
		    
		 try
		     {
		     synchronized (m_jdbc)
			{
			    data = m_jdbc.getSQLData(query);
			    //data=m_jdbc.getRowData();
			}
				}
		 catch (SQLException ex){
		           System.out.println("whynot; counting records" + ex);
		}

		    emptySet=false;
				
		    if(data.length!=0){
			m_nbRecords=new Integer(data[0][0]);
			m_labRecords.setText(user+" has "+m_nbRecords.toString() + " records");   
		    }
		    else{
			System.out.println("count Records: empty data set");
			emptySet=true;
		    }

	    }
    }
	
    /**
       calculate the frequency and difference. and set them in the table
    */
    public final void insertFreq(){
	//	System.out.println("in insert freq");  
	//m_ALDist1 = new Vector();
	//m_ALDist2 = new Vector();
	//m_ALDist3 = new Vector();
	Rcpt rcpt = new Rcpt();
	m_ALDist3.removeAllElements();
	//    Vector tmpVector = new Vector();
	Hashtable tmpHash = new Hashtable();  //should replace by hashtable since mostly lookup
	double freq=0;
	double di=0;
	String tmpString = new String();
	for (int i=0; i<m_ALDist2.size(); i++)
	    { 
		tmpString = (String)((Rcpt)m_ALDist2.elementAt(i)).getName(); 
		//    tmpString.trim();
		tmpHash.put(tmpString,""+i);
	    }
	int index = 0; 
	for (int i=0; i<m_ALDist1.size(); i++)
	    {  

		rcpt = (Rcpt)m_ALDist1.elementAt(i);
		tmpString = (String)rcpt.elementAt(0);
		if (tmpHash.containsKey(tmpString))
		    { 
			index = Integer.parseInt((String)tmpHash.get(tmpString));
				
			freq=((Rcpt)m_ALDist2.elementAt(index)).getFrequency();         //elementAt(1); 
			di =   ((Rcpt)m_ALDist2.elementAt(index)).getFrequency();      //elementAt(1)).doubleValue();
			di -= ((Rcpt)m_ALDist1.elementAt(i)).getFrequency();
				
			m_ALDist3.insertElementAt(new Rcpt(tmpString,freq,di),i);    //rcpt2,i);  
		    }
		else
		    {  
				
			freq = 0;//new Double(0);
			di =  - ((Rcpt)m_ALDist1.elementAt(i)).getFrequency(); //.doubleValue();
			m_ALDist3.insertElementAt( new Rcpt(tmpString,freq,di),i);  
				
		    }
	    }     
	tmpHash.clear();
	for (int i=0; i<m_ALDist1.size(); i++)
	    { 
		tmpString = (String)((Rcpt)m_ALDist1.elementAt(i)).getName(); 
		//tmpString.trim();
		tmpHash.put(tmpString,"");
	    }
	for (int i=0; i<m_ALDist2.size(); i++)
	    { 
		rcpt = (Rcpt)m_ALDist2.elementAt(i);
		tmpString = (String)rcpt.getName();
		if (!(tmpHash.containsKey(tmpString)))
		    { 
			freq = ((Rcpt)m_ALDist2.elementAt(i)).getFrequency();
			m_ALDist3.addElement(new Rcpt(tmpString,freq,freq));     
		    }
	    }
	Vector rcptColumns = new Vector(3);
	String col1;
	if(ChiOrCSC){
	    col1 = new String(RorSaddress);
	}
	else{  
	    col1 = new String(MSG_RA);
	}
	//String col2 = new String(MSG_FREQ);
	//String col3 = new String(MSG_DIFF);
	rcptColumns.addElement(col1);
	rcptColumns.addElement(MSG_FREQ);
	rcptColumns.addElement(MSG_DIFF);
	m_rcptModel3.setDataVector(m_ALDist3,rcptColumns);     
    }
	
    /**
     * calculate the chi-square of data in two tables.
     */
    public final void calculateChisquare()
    {  
	int sz2=m_ALDist1.size();
	int sz3=m_ALDist3.size();
	double extraFreq=0;
	double x=0;//chi square test
	double num=0;
	double den=0;
	Rcpt rcpt = new Rcpt();
		
	//for KS test
	double ks = KS();
	if (sz3>sz2)
	    for (int i=sz2;i<sz3; i++)
		{  rcpt = (Rcpt)m_ALDist3.elementAt(i);
		extraFreq += rcpt.getFrequency();//(rcpt.elementAt(1))).doubleValue();
		}
	//System.out.print("extra frequency: ");System.out.println(extraFreq);
	for (int i=0; i<sz3; i++)
	    { 
		rcpt = (Rcpt)m_ALDist3.elementAt(i);
		num =  rcpt.getDifference();    //((Double)(rcpt.elementAt(2))).doubleValue();
		num*=num;
		den = rcpt.getFrequency();//((Double)(rcpt.elementAt(1))).doubleValue();
		den-= rcpt.getDifference();//  ((Double)(rcpt.elementAt(2))).doubleValue();  
		if (den>0.00001)   
		    {   x+= num/den;  }
	    }
	//	Integer x1=new Integer((int)x);
	//Integer xs=new Integer(sz2);
	//compute p-value
	double p = chiSquareP(x,sz2);
	String sp = ""+Utils.round(p,3);//decimal(p);//convert double to decimal for p value
        String dks = ""+Utils.round(ks,3);//decimal(ks);//convert double to decimal for KS value
		
	//show 3 digit if 0.000... then show 0.000+
	if (!sp.startsWith("1"))
	    {
		if ( sp.endsWith("00"))
		    sp = sp+'+';
	    }
	m_labChisquare.setText("Chi Square Test:  " + x);//x1.toString());  
	m_labDegree.setText("Degrees of freedom:  " + sz2+"  P value: "+sp); 
	m_labKS.setText("KS test: "+dks); //show ks test
		
    }    
	
    /**
     * calculate p value for chi square.
     *
     * @param test Chi square test
     * @param df ...a kind of size
     * 
     * don't know what are these 2 parameters
     * @return the double
     */
    public final double chiSquareP(double test, int df)
    {
	double p = 0.0;
	double h = test/100000.0; 
	double a = chiSquare(0.0, df);
	double b = chiSquare(test,df);
	double dj =0.0;
	double xj = 0.0;
		
	for (int i = 1; i<100000.0; i++ )
	    dj += chiSquare( i*h, df);
		
	xj = 2.0*dj;
	p =1-((h/2.0 )* (a + b + xj));
		
	return p;
    }
	
    /**
     * chisquare function
     * 
     * don't know what are these 2 parameters, either.
     *
     * @param x the x
     * @param df the df
     * @return the double
     */
    private double chiSquare(double x, int df)
    {
	if (x ==0)
	    return 0.0;
	else 
	    {
		double exp1 = (-x/2.0);
		double exp2 = (((double)df/2.0)-1) * Math.log(x);
		double exp3 = ((double)df/2.0) * Math.log(2);
		double exp4 = logGamma((double)df/2.0);
		double f = exp1 + exp2 - exp3 - exp4;
		//double answer = Math.exp(f);
		//return answer;
		return Math.exp(f);
	    }		
    }//end of chisquare
	
    /** log Gamma function--use df and chi square test number are too big. */
    static final double sqrt2Pi = Math.sqrt( 2 * Math.PI);
    
    /**
     * Log gamma.
     *
     * @param x the x
     * @return the double
     */
    private static double logGamma(double x)
    {
	

	return x>1 
	    ? leadingFactor(x) + Math.log ( series(x) * sqrt2Pi /x )
	    : (x >0 ? logGamma(x + 1) - Math.log(x)
	       : Double.NaN);
    }

    /**
     * for log gamma function ---??doesnt seem to be used.
     *
     * @param n the n
     * @return the long
     */
    private static long factorial (long n)
    {
	return n<2 ? 1: n *  factorial( n-1);
    }

    /**
     * for log gamma function.
     *
     * @param x the x
     * @return the double
     */
    private static double leadingFactor (double x)
    {
	double temp = x + 5.5;
	return Math.log (temp) * (x + 0.5) - temp;
    }
	
    /** for log gamma function. */
    static final double[] coefficients = { 76.18009172947146,
				     -86.50532032941677,
				     24.01409824083091,
				     -1.231739572450155,
				     0.1208650973866179e-2,
				     -0.5395239384953e-5};
	


    /**
     * Series.
     *
     * @param x the x
     * @return the double
     */
    private static double series( double x)
    {
	double answer = 1.000000000190015;
	double term = x;
	for (int i =0; i<6; i++)
	    {
		term +=1;
		answer += coefficients[i] / term;
	    }
	return answer;
    }
	
    /**
     * for Kolmogorov-Smirnov Test.
     *
     * @return the double
     */
    public final double KS()
    {
	Rcpt tmp =  new Rcpt();
	double freqTrain = 0.0;
	double freqTest = 0.0;
	double diff = 0.0;
	double maxFreq = 0.0;
		
	for (int i=0; i<m_ALDist1.size(); i++)
	    {
		freqTrain = freqTrain + (((Rcpt)m_ALDist1.get(i)).getFrequency());			
		freqTest = freqTest+(((Rcpt)m_ALDist3.get(i)).getFrequency()); 
		diff = Math.abs(freqTrain-freqTest);
		if ( diff > maxFreq)
		    maxFreq = diff;
	    }
	for (int i=m_ALDist1.size(); i<m_ALDist3.size(); i++)
	    {
		freqTest = freqTest+(((Rcpt)m_ALDist3.get(i)).getFrequency());
		diff = Math.abs(freqTrain-freqTest);
		if ( diff > maxFreq)
		    maxFreq = diff;
	    }
	return maxFreq;
    }
	
    /**
       conver double value to decimal--3 digits
    
       public final static String decimal(double n)
       {
       NumberFormat nf = NumberFormat.getNumberInstance();
       nf.setGroupingUsed(false) ;     // don't group by threes
       nf.setMaximumFractionDigits(3) ;
       nf.setMinimumFractionDigits(3) ;
       Double D1 = new Double(n);
       double d1 = D1.doubleValue();
       return nf.format(d1);
       }	
    */
    /**
       display the page of profile
       one table and diagrams
    */
    public final void displayProfile()
    {
	//	System.out.println("displayprofile");
	if(!emptySet){
	    try{
		//Object selected =m_winGui.getSelectedUser();
				
		int sz = dsize;//m_ALSequence.size();
		int sz4 = m_ALDist.length;
		if (sz4>SPAN_MAX4) 
		    {  sz4=SPAN_MAX4; 
		    }
		double[] x = new double[sz];
		double[] y = new double[sz];     
		//double[] y2 = new double[sz];
		double[] y2_avg = new double[sz];
		//double[] y3 = new double[sz];
		double[] y3_avg = new double[sz];
		//double[] y5 = new double[sz];
		double[] x4 = new double[sz4];
		double[] y4 = new double[sz4];
		//double[] z4 = new double[sz4];
		double[] sclX = new double[2];
		double[] sclY = new double[2];
		//shlomo -loop unrolling
		if (sz>0)
		    { 
			x[0]=0;
			y2_avg[0]=m_ALSequence2[0];//y2[i];
			y3_avg[0]=m_ALSequence3[0];//y3[i];	 
		    }


		for (int i=1; i<sz;i++)
		    {
			x[i]=i;
			//	y[i]=((Integer)m_ALSequence.get(i)).intValue();
			//y2[i]=((Integer)m_ALSequence2.get(i)).intValue(); 
			//y3[i]=((Integer)m_ALSequence3.get(i)).intValue();
			//y5[i]=((Integer)m_ALSequence5.get(i)).intValue();
			//if (i==0)
			//{ 
			//    y2_avg[i]=m_ALSequence2[i];//y2[i];
			//    y3_avg[i]=m_ALSequence3[i];//y3[i];	 
			//	}
			//else
			//{  
			if (i<SPAN_AVG2) 
			    { y2_avg[i]=((y2_avg[i-1]*(i-1))+m_ALSequence2[i])/i;}
			else
			    {   y2_avg[i]=((y2_avg[i-1]*(SPAN_AVG2))+m_ALSequence2[i]-m_ALSequence2[i-SPAN_AVG2])/SPAN_AVG2;  }
			if (i<SPAN_AVG3) 
			    { y3_avg[i]=((y3_avg[i-1]*(i-1))+m_ALSequence3[i])/i;}
			else
			    {  y3_avg[i]=((y3_avg[i-1]*(SPAN_AVG3))+m_ALSequence3[i]-m_ALSequence3[i-SPAN_AVG3])/SPAN_AVG3;  }
			//}
		    }
		
		for (int i=0; i<sz4;i++)
		    { 
			x4[i]=i;//Math.log((double)(i+1));
			y4[i]=m_ALDist[i].getFrequency(); 
			//((Rcpt)m_ALDist.get(i)).getFrequency(); 
			//
			//y4[i]=Math.log(y4[i]+1);
			//
		    }
		m_plot11.removeAll();
		
		//if in the Hellinger Distance
		if (m_Button == HELLINGER){
		    m_plot11.addCurve(hellingerDistance);
		}
		/*
		  else if(m_Button ==4){//weijen
		  m_plot11.addCurve(dummyHDistance);
		  m_plot11.setLineColor(Color.red);
		  m_plot11.addCurve(hellingerDistance);
		  }	
		*/
		else {	
		    m_plot11.addCurve(x,m_ALSequence);
		}
		m_plot11.setLineColor(Color.blue);
		m_plot11.setGridState(JPlot2D.GRID_ON);
		if(isoutbound || !ChiOrCSC){
		
		    //if in the Hellinger Distance
		    if(m_Button ==HELLINGER){
			m_plot11.setTitle("Recipients' Hellinger Distance ");
			m_plot11.setXLabel("all emails sent");
			m_plot11.setYLabel("the Hellinger Distance");
		    }else{
			m_plot11.setTitle("Total Recipient Address List size over time");
			m_plot11.setXLabel("all emails sent");
			m_plot11.setYLabel("address list size");
		    }	
		}
		else{
		    //if in the Hellinger Distance
		    if(m_Button ==HELLINGER){
			m_plot11.setTitle("Senders' Hellinger Distance ");
			m_plot11.setXLabel("all emails received");
			m_plot11.setYLabel("the Hellinger Distance");
		    }else{    
			m_plot11.setTitle("Total Sender Address List size over time");
			m_plot11.setXLabel("all emails received");
			m_plot11.setYLabel("address list size");
		    }
		}
		//		m_plot11.repaint();
		
		m_panel1.remove(m_plot12);
		m_plot12 = new JPlot2D();
		m_plot12.setPreferredSize(new Dimension(350,290));
		m_constraints1.gridx = 1;
		m_constraints1.gridy = 1;
		m_constraints1.insets = new Insets (3,3,3,3);     
		m_gridbag1.setConstraints(m_plot12,m_constraints1);
		m_panel1.add(m_plot12);
		
		//sh tried to shorten previous lines into:
	
		//m_plot12.removeAll();
		//System.out.println("total: " + m_plot12.getTotalAnnotations() );
		m_plot12.addCurve(x,m_ALSequence2);
		m_plot12.setLineColor(Color.orange);
		m_plot12.setLineStyle(JPlot2D.LINESTYLE_LONGDASH);
		m_plot12.addCurve(x,m_ALSequence3);
		m_plot12.setLineColor(Color.green);
		m_plot12.setLineStyle(JPlot2D.LINESTYLE_LONGDASH);
		m_plot12.addCurve(x,y2_avg);
		m_plot12.setLineColor(Color.blue);
		m_plot12.addCurve(x,y3_avg);
		m_plot12.setLineColor(Color.blue);
		m_plot12.addCurve(x,m_ALSequence5);
		m_plot12.setLineColor(Color.red);
		m_plot12.setGridState(JPlot2D.GRID_ON);
		if(isoutbound || !ChiOrCSC){
		    m_plot12.setTitle("# distinct rcpt & # attach per email blocks");
		    m_plot12.setXLabel("all emails sent");
		    m_plot12.setYLabel("# distinct rcpt & # attachements per block");
		}
		else{
		    m_plot12.setTitle("# distinct sender & # attach per email blocks");
		    m_plot12.setXLabel("all emails received");
		    m_plot12.setYLabel("# distinct sender & # attachements per block");
		}
		sclY[0]=0;sclY[1]=(double)SPAN3;
		m_plot12.setYScale(sclY);
		if (sz>50){  
		    String RorS = "RCPT";
		    if(!isoutbound && ChiOrCSC) RorS = MSG_SNDR;
		    m_plot12.addAnnotation("NUMBER of DISTINCT "+RorS+" per BLOCK of 50",8,47);
		    m_plot12.setAnnotationColor(Color.green);
		    m_plot12.addAnnotation("NUMBER of DISTINCT "+RorS+" per BLOCK of 20",8,44);
		    m_plot12.setAnnotationColor(Color.orange);
		    m_plot12.addAnnotation("NUMBER of MSGS with ATTACH per BLOCK of 50",8,41);
		    m_plot12.setAnnotationColor(Color.red);
		    m_plot12.addAnnotation("ROLLING AVERAGES (per 100 records)",8,38);
		    m_plot12.setAnnotationColor(Color.blue);
		}
		//sh???sh    m_plot12.repaint();
		//m_plot12.repaint();
		m_plot13.removeAll();
		sclX[0]=0;sclX[1]=(double)sz4;
		m_plot13.setXScale(sclX);
		if(isoutbound || !ChiOrCSC){
		    m_plot13.setTitle(MSG_RFH);
		    m_plot13.setXLabel("each bar is a recipient");
		    m_plot13.setYLabel(MSG_RFIP);
		}
		else{
		    m_plot13.setTitle(MSG_SFH);
		    m_plot13.setXLabel("each bar is a sender");
		    m_plot13.setYLabel(MSG_SFIP);
		}
		m_plot13.addCurve(x4,y4); 
		m_plot13.setGridState(JPlot2D.GRID_ON);
		m_plot13.setPlotType(JPlot2D.BAR); 
		//sh ???     m_plot13.repaint();
		//m_plot13.repaint();
		repaint();
		//setViewportView(m_panel);
		


	    }
	    catch(Exception e){
		System.out.println(" Exception on plot drawing in diplsay profile: " +e);
	    }
	    System.gc();
	}
		
    }//end display profile
	
    /**
     * display the page of chi-square.
     */
    public final void displayChisquare(){
	try{
	    //Object selected = m_winGui.getSelectedUser();

	    int sz1 = m_ALDist1.size();
	    if (sz1>SPAN_MAX4) {  sz1=SPAN_MAX4; }
	    int sz2 = m_ALDist3.size();
	    if (sz2>SPAN_MAX4) {  sz2=SPAN_MAX4; }
	    double[] x1 = new double[sz2];
	    double[] y1 = new double[sz2];  
	    double[] x2 = new double[sz2];
	    double[] y2 = new double[sz2]; 
	    double[] sclX = new double[2];
	    double[] sclY = new double[2];
	    for (int i=0; i<sz1;i++)
		{ x1[i]=i;
		if (m_Button==CHI_CLIQUE)
		    { }// x1[i]=Math.log((double)(i+1));  }
		y1[i]=((Rcpt)m_ALDist1.elementAt(i)).getFrequency(); 
		if (m_Button==CHI_CLIQUE)
		    {  }//y1[i]=Math.log(y1[i]+1);   }
		}
	    if (sz1<sz2)
		for (int i=sz1; i<sz2;i++)
		    {  x1[i]=i;
		    y1[i]=0;
		    }
	    sclX[0]=0;
	    m_plot21.removeAll();
	    sclX[1]=(double)sz2;
	    m_plot21.setXScale(sclX);
	    if(isoutbound || !ChiOrCSC){
		m_plot21.setTitle(MSG_RFH);
		m_plot21.setXLabel(MSG_EPR);
		m_plot21.setYLabel(MSG_RFIP);
				
	    }
	    else{
		m_plot21.setTitle(MSG_SFH);
		m_plot21.setXLabel(MSG_EPS);
		m_plot21.setYLabel(MSG_SFIP);
	    }
	    m_plot21.addCurve(x1,y1); 
	    m_plot21.setGridState(JPlot2D.GRID_ON);
	    for (int i=0; i<sz2;i++)
		{ x2[i]=i;
		//if (m_Button==2)
		//   {  x2[i]=Math.log((double)(i+1));  }
		y2[i]=((Rcpt)m_ALDist3.elementAt(i)).getFrequency();
		//if (m_Button==2)
		//  {  y2[i]=Math.log(y2[i]+1);   }
		}
	    sclX[0]=0;
	    m_plot22.removeAll();
	    sclX[1]=(double)sz2; 
	    m_plot22.setXScale(sclX);
	    if(isoutbound || !ChiOrCSC){
		m_plot22.setTitle(MSG_RFH);
		m_plot22.setXLabel(MSG_EPR);
		m_plot22.setYLabel(MSG_RFIP);
	    }
	    else{
		m_plot22.setTitle(MSG_SFH);
		m_plot22.setXLabel(MSG_EPS);
		m_plot22.setYLabel(MSG_SFIP);
	    }
	    m_plot22.addCurve(x2,y2); 
	    m_plot22.setGridState(JPlot2D.GRID_ON);
	    m_plot21.setPlotType(JPlot2D.BAR); 
	    m_plot22.setPlotType(JPlot2D.BAR); 
	    //sh ??  m_plot21.repaint();
	    //sh??  m_plot22.repaint();
	    //m_plot21.repaint();
	    //m_plot22.repaint();
	    //m_plot21.repaint();
	    //m_plot22.repaint();
	    //m_plot21.repaint();
	    //m_plot22.repaint();
	    //repaint();
	    setViewportView(m_panel);
	}
	catch(Exception e){
	    System.out.println("repaint null plot2");
	}
    }

    /**
     * Something with the Clique calculation......
     *
     * @param minMessages the min messages
     * @param minSubjectWordLength the min subject word length
     */	
    public final void createCliqueList(int minMessages, int minSubjectWordLength){  
	//System.out.println("clique cal create list");
	try{
	    CliqueFinder m_cliqueFinder = new CliqueFinder(m_jdbc, m_aliasFilename);
	    HashMap<String,HashMap> mapped =  m_cliqueFinder.buildGraph(minMessages, minSubjectWordLength);
	    ArrayList vCliques = m_cliqueFinder.findCliques(mapped,false);
	    String str = ""; 
	    StringTokenizer st;
	    m_enumCliqueList = new Vector(); 
	    String str2 =new String();
	    for (int i=0; i<vCliques.size(); i++)
		{  
		    Vector clique = new Vector(); 
		    str = vCliques.get(i) + "\n";
		    if ( ! (vCliques.get(i) == null ) )
			{  
			    int nb = str.indexOf(10);
			    str=str.substring(nb+1);
			    if(str.length() > 2)
				{
				    str=str.substring(0,str.length()-2);
				}
		    
			    st = new StringTokenizer(str,"\n");
			    if (st.countTokens()>2){  
				while (st.hasMoreTokens()){  
				   
				    str2=(st.nextToken()).trim();
				    clique.addElement(str2);
				}
				m_enumCliqueList.addElement(clique);
			    }
			}
		}
	    //  	  System.out.print("CLIQUE LIST (with threshold = ");
	    //  	  System.out.print(m_thres2);
	    //  	  System.out.println("):");
	    //  	  for (int k=0; k<m_enumCliqueList.size(); k++)
	    //  	    {  System.out.println(m_enumCliqueList.get(k)); }
	    //  	  System.out.println(" ");
	    System.gc();
	}
	catch (Exception ex){  
	    System.out.println("error in clique construction");
	    JOptionPane.showMessageDialog(this, "Could not get cliques: " + ex);
	}

    }
	
    /**
     * refresh info.
     *
     * @throws SQLException the sQL exception
     */
    
    public final void Refresh() throws SQLException
    {   
	//refreshCombo(false);  
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {}
	
    /**
     * Yep, mouse is clicked.
     *
     * @param e the e
     */
    public final void mouseClicked(MouseEvent e)
    {  //System.out.println(e.getClickCount());
    //if (e.getClickCount()==2){  
	Component c = this;
	while ((c != null) && !(c instanceof Frame))
	    {	c = c.getParent();   }
	Point p = e.getPoint();
	int row = m_rcptFreq1.rowAtPoint(p);
	String st = (String)m_rcptFreq1.getValueAt(row,0);
	String subst=st.substring(0,6);
	subst=subst.trim();
	if (subst.equals("clique"))
	    {  String comp = new String();
	    Vector srv = new Vector();
	    boolean which = true;
	    int i = 0 ;
	    while ( which )
		{  Vector v = new Vector();
		v=(Vector)m_userCliqueMember.elementAt(i);
		String wst = new String();
		wst = (String)v.elementAt(0);
		if (wst.equals(st))
		    {  which = false;
		    comp+=st;
		    comp+=" members:\n\n"; 

		    String user =(String)m_winGui.getSelectedUser(); 
		    comp+=user; comp+="\n";
		    for (int j=1;j<v.size();j++)
			{  comp = comp.concat(v.elementAt(j)+"\n");
			}
		    }
		i++;
		}
	    CliqueBox dd;
	    if (c == null)
		{  dd = new CliqueBox(null, false, comp);   }
	    else
		{  dd = new CliqueBox((Frame) c, false, comp);   }
	    dd.setLocationRelativeTo(this);
	    dd.setVisible(true); 
	    }
	//}
    } 
	
    
    //Block all the buttons.
    
       /**
     * Blockall.
     */
    public void blockall(){
       System.out.println("blocking");

	
       m_buttonProfile.setEnabled(false);
       m_buttonChisquare.setEnabled(false);
       m_buttonChisquare2.setEnabled(false);
       m_buttonRun.setEnabled(false);
       hellingerButton.setEnabled(false);
       testButton.setEnabled(false);
       inbound.setEnabled(false);
       outbound.setEnabled(false);
       attachment.setEnabled(false);
	
       }
    
    
    //Unblock all the buttons.
    
       /**
     * Unblockall.
     */
    public void unblockall(){
       System.out.println("unblocking");
	
	
       m_buttonProfile.setEnabled(true);
       m_buttonChisquare.setEnabled(true);
       m_buttonChisquare2.setEnabled(true);
       m_buttonRun.setEnabled(true);
       hellingerButton.setEnabled(true);
       testButton.setEnabled(true);
       inbound.setEnabled(true);
       outbound.setEnabled(true);
       attachment.setEnabled(true);
	
       }
    
	
	
    /**
     * The Class CliqueBox.
     */
    public class CliqueBox extends JDialog{
	
	/** The Constant TITLE. */
	private static final String TITLE = "CLIQUE MEMBERS";
	
	/** The m_text. */
	private JTextArea m_text;
	
	/**
	 * Instantiates a new clique box.
	 *
	 * @param owner the owner
	 * @param modal the modal
	 * @param detail the detail
	 */
	public CliqueBox(Frame owner, boolean modal, String detail)
	{
	    super(owner, TITLE, modal);  
	    m_text = new JTextArea(detail);
	    m_text.setLineWrap(false);
	    JScrollPane scrollPane = new JScrollPane(m_text);
	    getContentPane().add(scrollPane);
	    setSize(new Dimension(250,250));
	} 
    }
	
}

class Rcpt extends Vector{  
    private String name;
    private double frequency;
    private double difference;
	
	
    public Rcpt()
    {
	super(3);//sets initial cpacity of this vector to three not 10
	name = new String();
	frequency = 0;
	difference =0;
	update();
    }
	
    public Rcpt(String n, double f)
    {  
	super(3);//sets initial capacity to 3
	name = new String(n);
	frequency = f;
	difference=0;
	update();
		
    }
    public Rcpt(String n, double f, double d)
    { 
	super(3);
	name = new String(n);
	frequency = f;
	difference = d;
	update();
		
    }
    public Rcpt(Rcpt r,double d)
    { 
	super(3);
	name = r.getName();
	frequency = r.getFrequency();
	this.difference= d;
	this.removeAllElements();
	update();
		
    }
    private void update()
    {
		
	this.add(0,name);
	this.add(1,""+this.frequency);
	this.add(2,""+this.difference);
		
    }
	
    public final String getName()
    {  return name; }
    public final double getFrequency()
    {  return frequency;  }
    public final double getDifference()
    {  return  difference; }
    public final void setFrequency(double f)
    {  
	frequency = f;
	this.removeElementAt(1);
	this.add(1,""+f);	 
    }
	
}


