
/**
 * Mail loaded class which displays emt window components<P>
 * 
 * Minimum amount of windows are created, so that only when a window is invoked for the first time is it instantiated...makes things faster<P>
 * 
 * In addition, it also has methods to allow the different windows to share/notify/send event information among themselves
 */

package metdemo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.SubstanceLightAquaTheme;

import metdemo.AlertTools.ReportForensicWindow;
import metdemo.Attach.AttachmentWindow;
import metdemo.Attach.FileAnalyzer;
import metdemo.Attach.VirusDetectModels;
import metdemo.CliqueTools.EnclaveCliques;
import metdemo.CliqueTools.SocialCliques;
import metdemo.CliqueTools.VCliqueInfo;
import metdemo.CliqueTools.VisualClique;
import metdemo.DataBase.DatabaseConnectionException;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.DataBase.dbSQL;
import metdemo.Finance.Finance;
import metdemo.MachineLearning.MLearner;
import metdemo.Tables.MessageWindow;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.EMTLimitedComboSpaceRenderer;
import metdemo.Tools.Options;
import metdemo.Tools.Preferences;
import metdemo.Tools.Utils;
import metdemo.Window.AverageCommTime;
import metdemo.Docflow.DocumentFlowWindow;
import metdemo.Window.EmailInternalConfigurationWindow;
import metdemo.Window.SHDetector;
import metdemo.Window.EmailFlow;
import metdemo.Window.LoadDB;
import metdemo.Window.RcptFrequency;
import metdemo.Window.SimilarUsers;
import metdemo.Window.SplashScreen;
import metdemo.Window.UsageHistogram;
import metdemo.Window.WelcomeEMTWindow;
import metdemo.dataStructures.UserDistance;
import metdemo.dataStructures.userComm;
import metdemo.dataStructures.userShortProfile;
import metdemo.experiment.ExperimentalEMTWindow;
import metdemo.search.SearchEMT;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
//import java.util.TimerTask;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Class to setup the main interface and the tab panes.
 * will manage start and end, and also main GUI elements (global stuff) 
 */

public class winGui extends JFrame implements ActionListener, ItemListener {
	
    private JMenuItem m_miExit, m_miOptions,  m_miClearAlerts, m_miPreferences, m_helpabout,
			m_helpwalk, m_showsplash, m_windowshelp;
	private md5CellRenderer m_md5Renderer;
	private JPanel m_EMTWindow_tabbedPane;//used to be tabbed switch to drop down list switcher sys
	private JLabel m_statusBar;
	private WelcomeEMTWindow m_welcome;
	private MessageWindow m_messages;
	private AttachmentWindow m_attachments;
	private ReportForensicWindow m_alerts;
	private EnclaveCliques m_cliques;
	private SocialCliques m_social_cliques;
	private Options m_options;
	private Preferences m_preferences;
	private boolean m_verbose;
	private boolean m_forensicMode = true;
	private String m_dbHost;
	private String m_dbName;
	private String m_user;
	private String m_password;
	private Hashtable<Component,Boolean> m_cachedEnabledState;
	private RcptFrequency m_addresslist; //on
	private EmailInternalConfigurationWindow m_figures;//s02
	private LoadDB m_loader;//sh
	private dbSQL m_sqlview;
	private ExperimentalEMTWindow m_experiment;
	private Finance m_finance;
    private SearchEMT m_search;
	private SimilarUsers m_similarusers; // by Ke Wang
	private UsageHistogram m_histogramcomp; //ke wang
	private AverageCommTime m_averageComm; //Ke Wang
	private SHDetector m_shDetector; //Ryan Rowe
	private DocumentFlowWindow m_DocFlow;
	private EmailFlow m_emailflow;
	private VisualClique m_visualclique;
	//private VirusSimulationWin m_virusSim;
	private VirusDetectModels m_virusSim;
	private FileAnalyzer m_fanalyzer;
	private EMTDatabaseConnection jdbcView = null;
	private String g_maxdate, g_mindate;
	private int g_totalCount;//need to make it easier to reset TODO
	private String[] g_allAttachments;
	private Hashtable<String,userShortProfile> g_userHash;//this will keep a fast lookup for each user's
	// max,min,count by name
	private JToolBar comboBoxPane;
	private SpinnerDateModel m_spinnerModelStart = new SpinnerDateModel();
	private SpinnerDateModel m_spinnerModelEnd = new SpinnerDateModel();
	private GregorianCalendar cal = new GregorianCalendar();
	private GregorianCalendar cal2 = new GregorianCalendar();
	private JSpinner spinnerStart, spinnerEnd;
	private JComboBox mainChoiceBox;
	private JComboBox m_view_user;
	//some helper variables...
	static final int typeSENDER = 0;
	static final int typeRCPT = 1;
	static final int typeBOTH = 2;
	static final int typeMIN_SENDER = 3;
	static final int typeX_SENDER = 4;
	//if we want to look at specifc subset of users...
	static final boolean sql_AllUsers = false;
	private int user_type = typeSENDER; //0= sender, 1 = rcpts, 2 = both 4= sender with
	private String sql_emailDomain = null; //for setting up to see specific emails from specific domains
    private int minNumber = 0;
   
    
	//window id's for signaling
    static final int i_welcomeWindow = 0;
	static final int i_loadWindow = 1;
	static final int i_messageWindow = 2;
	static final int i_featuresWindow = 3;
	static final int i_usageWindow = 4;
	static final int i_enclaveWindow = 5;
	static final int i_usercliquesWindow = 6;
	static final int i_similiarWindow = 7;
	static final int i_communicationWindow = 8;
	static final int i_rcptWindow = 9;
	static final int i_attachmentWindow = 10;
	static final int i_emailflowWindow = 11;
	static final int i_graphiccliquesWindow = 12;
	static final int i_virusWindow = 13;
	static final int i_analyzerWindow = 14;
	static final int i_reportWindow = 15;
	static final int i_sqlviewWindow = 16;
	static final int i_experimentalWindow = 17;
    static final int i_searchWindow = 18;
    static final int i_financeWindow = 19;
    static final int i_shdetectorWindow = 20;
    static final int i_doclflowWindow = 21;
    //static to save on this
    static public final String st_WelcomeWindow = "Welcome Screen";
    static public final String st_LoadWindow =  "Load Data";
    static public final String st_MessageWindow = "Messages Window";
    static public final String st_ConfigWindow = "Configuration";
    static public final String st_UsageWindow = "Usage Histogram";
    static public final String st_EnclaveWindow = "Enclave Cliques";
    static public final String st_UserCliqWindow = "User Cliques";
    static public final String st_SimilarWindow = "Similar Users";
    static public final String st_VIPWindow = "Average Comm Time";
    static public final String st_RCPTWindow =  "Recipient Frequency";
    static public final String st_AttachWindow =  "Attachment Statistics";
    static public final String st_FlowWindow = "Email Flow";
    static public final String st_GraphicCliqWindow = "Graphic Cliques";
    static public final String st_VirusWindow = "Virus Scanning";
    static public final String st_AttachVirAnalyzerWindow = "Attachment Analyzer";
    static public final String st_ReportWindow = "Reports & Forensic";
    static public final String st_SQLWindow = "SQL view";
    static public final String st_ExperimentWindow = "Experiments";
    static public final String st_SearchWindow = "Search";
    static public final String st_SocNetWindow = "SocNetworks";
    static public final String st_SocHeirWindow = "Social Hierarchies";
    static public final String st_docFlowWindow = "Document Flow Analysis";
    
    static final int numberWindows = 22;
	
    private boolean setupEMTWindowList[] = new boolean[numberWindows];

	//not currently used, met had it to map server names....
	private String m_aliasFilename;
	//private String m_stoplist;
	
    //link to the config file to get install specific information
	private EMTConfiguration m_emtconfig;

    
 /**
  * Main EMT constructor to help setup everything
  * @param dbHost the name of our database host machine
  * @param dbName the name of the database to use
  * @param user user to log into db with
  * @param password
  * @param aliasFilename if there is a name of machine aliases
  * @param wordlist list of words to use location
  * @param stoplist stoplist location
  * @param startAt time we started
  * @param refreshRate rate to reload (not used)
  * @param verbose if we should print out stuff ??
  * @param ec the configuration handle
  * @param emtdbc database handle
  */
	public winGui(final String dbHost, final String dbName, final String user, final String password,
			/*final String mysql,*/ final String aliasFilename, final String wordlist, final String stoplist,
			final long startAt, final int refreshRate, /*final long bootstrapSeconds,*/ final boolean verbose,
			/*final boolean debug, final boolean dormant,*/ EMTConfiguration ec, EMTDatabaseConnection emtdbc) {
		//final String dosbatch, final String bashbatch) {
		//set title
		super("EMT - Email Mining Toolkit");
       
		//assignments
		m_emtconfig = ec;
		m_dbHost = dbHost;
		m_dbName = dbName;
		m_user = user;
		m_password = password;
		m_cachedEnabledState = null;
		//m_debug = debug;
		m_verbose = verbose;
		m_aliasFilename = aliasFilename;
	
		// open handle to database connection
		jdbcView = emtdbc;
	
		/***********************************************************************
		 * Test for normal shutdown on last use by creating a checksum file
		 * which will be deleted on normal exit
		 **********************************************************************/
		//first add the current db name to frontto make unique
		//will make it manageable to use only first 10 characters
		String CHECKFILE;
		
		if (dbName.length() > 10)
			CHECKFILE = ec.getProperty(EMTConfiguration.DBTYPE) + "_" + dbName.substring(0, 9) + "_" + "checkfile.del";
		else
			CHECKFILE = ec.getProperty(EMTConfiguration.DBTYPE) + "_" + dbName + "_" + "checkfile.del";

		System.setProperty("CHECKFILE", CHECKFILE);//for deleting

		File checksum = new File(CHECKFILE);

		try {
			if (checksum.createNewFile() == false) {
				Object[] options = {"Attempt Cleanup", "DB Info", "Delete All Records", "Ignore and Continue", "Help"};
				//			String data[][];
				int type = 4;
				do {
					type = JOptionPane.showOptionDialog(winGui.this, "Abnormal Shutdown of EMT Detected:", "Warning",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[3]);

					if (type == 1) {
						//get some basic info
						String report = new String("For database named: " + dbName + "\n");
						
                        report += "Number of records in email: " + jdbcView.getCountByTable("email") + "\n";

						report += "Number of records in message: " + jdbcView.getCountByTable("Message") + "\n";

						//display it
						JOptionPane.showMessageDialog(winGui.this, report, "Info on DB",
								JOptionPane.INFORMATION_MESSAGE);
						type = 4;//so will loop
					} else if (type == 4) {
						JOptionPane
								.showMessageDialog(
										winGui.this,
										"For some reason EMT was not shutdown correctly\n1) Attempt cleanup = will try to delete bad records from db and continue normal operation\n2) DB info will give back some info on current db\n3) Delete = will delete all records and fix problem\n4) Ignore = will try to run emt normally\n5) Help - this screen\n",
										"Help", JOptionPane.INFORMATION_MESSAGE);
					}

				} while (type == 4);

				if (type == 0) {//attemp cleanup
					dbSQL.cleanmailref(true, jdbcView, new JPanel());
					dbSQL.optimize(jdbcView);

				} else if (type == 2) {//delete all records

					dbSQL.flushDatabase(jdbcView,true);
				}
			}
		} catch (IOException ifile) {
			System.out.println("problem making checkfile, will not be able to detect crashes");
		}
		//end testcheck file
		comboBoxPane = new JToolBar("EMT Global Menu System"); 
		//show cool emt logo
		SplashScreen splashScreen = new SplashScreen();
		splashScreen.setVisible(true);
		final String comboBoxItems[] = {st_WelcomeWindow , st_LoadWindow , st_MessageWindow ,
			   st_ConfigWindow, st_UsageWindow , st_EnclaveWindow , st_UserCliqWindow,
			   st_SimilarWindow , st_VIPWindow , st_RCPTWindow , st_AttachWindow ,
			   st_FlowWindow , st_GraphicCliqWindow, st_VirusWindow, st_AttachVirAnalyzerWindow ,
			     st_ReportWindow , st_SQLWindow , st_ExperimentWindow , st_SearchWindow ,
			     st_SocNetWindow , st_SocHeirWindow, st_docFlowWindow	};
				//"Welcome Screen","Load Data", "Messages", "Configuration", "Usage Histogram", "Enclave Cliques",
				//"User Cliques", "Similar Users", "Average Comm Time", "Recipient Frequency", "Attachment Statistics",
				//"Email Flow", "Graphic Cliques", "Virus Scanning", "Attachment Analyzer", "Reports & Forensic", "SQL view", 
				//"Experiments","Search","SocNetworks", "Social Hierarchies"};
		//fast create a combo from above
		mainChoiceBox = new JComboBox(comboBoxItems);
		mainChoiceBox.setEditable(false);
		mainChoiceBox.setMaximumSize(new Dimension(140, 25));
		mainChoiceBox.addItemListener(this); //so will listen to events

		JButton jb_emailselect = new JButton("Email:");
		jb_emailselect.setToolTipText("Click to choose types of users to see");
		jb_emailselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				Object[] options = {"Sender's Only", "Rcpt's Only", "Sender's and Rcpt's", "Sender > 200 emails", "Sender > X emails" };
				int type = JOptionPane.showOptionDialog(winGui.this, "Select Email Users:", "Emails",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				//let us choose what users we want on the top gui component, used in other windows to compare user space...so can limit it
				if (type >= 0) {
					user_type = type;
				
					if(type == typeX_SENDER){
						String backvalue = ((String) JOptionPane.showInputDialog(winGui.this,
								"Type in min number of messages for each account:\n",
								"Give limit", JOptionPane.PLAIN_MESSAGE, null, null,
								" 50"));
						
						minNumber = 200;
						try{
						if(backvalue!=null){
							minNumber = Integer.parseInt(backvalue.trim());
						}
						}catch(NumberFormatException m){
							//if any issue default to at least 200
							minNumber = 200;
						}
					}else{
						minNumber =0;//reset it to zero
					}
					
					
					//lets find out if to constrain domain of emails
					//say we only want columbia emails
					String inputusers = JOptionPane.showInputDialog(winGui.this,"Please choose user constraints\n (Example %@columbia.edu for all emails with columbia.edu domain)\n Leave default (%@%) if want all users", "%@%");
						
					 
					//System.out.println("you entered: " + inputusers);
					
					//TODO: maybe save history so no need to retype each time
					
					
					
					setupGlobals(false, inputusers);
				}
				
								
				Execute();
			}
		});
//some stats - weijen work
		comboBoxPane.add(jb_emailselect);
		comboBoxPane.add(new JLabel("test"));//will be replaced by list of email users
		comboBoxPane.add(new JLabel(" Range: "));
		comboBoxPane.add(new JLabel("test"));//will be replaced by start dates
		comboBoxPane.add(new JLabel("test"));//will be replaced by end dates
		JButton jb_info = new JButton(" Info ");
		jb_info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				getInfo();
			}
		});
		jb_info.setToolTipText("Click for more info on this user");
		comboBoxPane.add(jb_info);
		JButton jb_p = new JButton(" P ");
		jb_p.setToolTipText("Click to set period range");
		comboBoxPane.add(jb_p);
		JButton em = new JButton(" Help ");
		em.setToolTipText("Click for general help");
		em.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				EMTHelp e = new EMTHelp(EMTHelp.EMTHELP);
				e.showME();
			}
		});

		//EMTHelp em = new EMTHelp(22);
		comboBoxPane.add(em);
		comboBoxPane.setBackground(Color.gray);
		comboBoxPane.setForeground(Color.white);
		splashScreen.setMSG("setup user info");
		setupGlobals(false, "%@%");
		//this.itemStateChanged("Message");
		/**
		 * s02 shlomo set next lines to false
		 * since we aren't using that model anymore...
		 */
		m_md5Renderer = new md5CellRenderer(false);
		//s02 kewang set the tooltip dismiss time
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setDismissDelay(600000);
		// set up menu bar

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		JMenu m_menuview = new JMenu("View");//, KeyEvent.VK_H);
		m_menuview.setMnemonic(KeyEvent.VK_V);
		menuBar.add(m_menuview);

		menuBar.add(new JSeparator(SwingConstants.VERTICAL));

		JMenu m_windowsdrop = new JMenu("Switch Windows");//, KeyEvent.VK_H);
		m_windowsdrop.setMnemonic(KeyEvent.VK_W);
		menuBar.add(m_windowsdrop);

		menuBar.add(new JSeparator(SwingConstants.VERTICAL));

		JMenu menuTools = new JMenu("Tools");
		menuTools.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menuTools);

		JMenu m_help = new JMenu("Help");//, KeyEvent.VK_H);
		m_help.setMnemonic(KeyEvent.VK_H);
		menuBar.add(m_help);
splashScreen.setMSG("Setup components.");
		m_windowshelp = new JMenuItem("Specific Window Help");//,
		// KeyEvent.VK_H);
		m_windowshelp.setMnemonic(KeyEvent.VK_W);
		m_windowshelp.addActionListener(this);
		m_help.add(m_windowshelp);

		m_helpabout = new JMenuItem("Help About", KeyEvent.VK_A);
		m_helpabout.getAccessibleContext().setAccessibleDescription("About EMT");
		m_helpabout.addActionListener(this);
		m_help.add(m_helpabout);
		m_helpwalk = new JMenuItem("Help Walkthrough", KeyEvent.VK_W);
		m_helpwalk.getAccessibleContext().setAccessibleDescription("Walkthrough of EMT");
		m_helpwalk.addActionListener(this);
		m_help.add(m_helpwalk);

		m_showsplash = new JMenuItem("Show Intro Screen", KeyEvent.VK_S);
		m_showsplash.getAccessibleContext().setAccessibleDescription("Show EMT intro");
		m_showsplash.addActionListener(this);
		m_help.add(m_showsplash);

		m_miExit = new JMenuItem("Exit", KeyEvent.VK_X);
		m_miExit.getAccessibleContext().setAccessibleDescription("Exit the program");
		m_miExit.addActionListener(this);
		menuFile.add(m_miExit);

		m_miOptions = new JMenuItem("Alert Options...", KeyEvent.VK_O);
		m_miOptions.getAccessibleContext().setAccessibleDescription("Set alert options");
		m_miOptions.addActionListener(this);
		menuTools.add(m_miOptions);
		

		m_miClearAlerts = new JMenuItem("Clear Alerts", KeyEvent.VK_C);
		m_miClearAlerts.getAccessibleContext().setAccessibleDescription("Clear alert table");
		m_miClearAlerts.addActionListener(this);
		menuTools.add(m_miClearAlerts);

		m_miPreferences = new JMenuItem("Preferences...", KeyEvent.VK_P);
		m_miPreferences.getAccessibleContext().setAccessibleDescription("Preferences");
		m_miPreferences.addActionListener(this);
		menuTools.add(m_miPreferences);

		//.getAccessibleContext().setAccessibleDescription("Show EMT intro");

		for (int i = 0; i < comboBoxItems.length; i++) {
			JMenuItem temp = new JMenuItem(comboBoxItems[i]);
			temp.getAccessibleContext().setAccessibleDescription("" + i);
			temp.addActionListener(this);
			m_windowsdrop.add(temp);
		}

		m_EMTWindow_tabbedPane = new JPanel(new CardLayout());//JTabbedPane();
        //m_EMTWindow_tabbedPane.setPreferredSize(new Dimension(930,770));
		//m_tabbedPane.addChangeListener(this);
		for (int i = 0; i < setupEMTWindowList.length; i++) {
			setupEMTWindowList[i] = false;
		}

		// options dialog
		m_options = new Options(wordlist, stoplist);
		
		// preferences dialog
		m_preferences = new Preferences(ec,true);//dosbatch, bashbatch);
		setupEMTWindowList[i_featuresWindow] = true;
		// alerts panel
		m_alerts = new ReportForensicWindow(jdbcView,this);
		setupEMTWindowList[i_reportWindow] = true;
		
		m_alerts.setSilent(m_forensicMode);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(comboBoxPane, BorderLayout.NORTH);//PAGE_START);
		getContentPane().add(m_EMTWindow_tabbedPane, BorderLayout.CENTER);
		createStatusBar();
		long totalBefore = System.currentTimeMillis();
		//setup the emt windows
		//set all windows to false;
		
		try {
			// attachments panel
			m_attachments = new AttachmentWindow(jdbcView, m_alerts, m_options.getAttachmentAlertOptions(),
					m_md5Renderer, m_statusBar, this, m_verbose);

			setupEMTWindowList[i_attachmentWindow] = true;
			//BusyWindow bw = new BusyWindow("Loading Progress", "Progress");
			//bw.setVisible(true);
			int i = 1;
			int max = 15;
			// cliques panel
			m_cliques = null;//new Cliques(this, jdbcView, aliasFilename,
							 // getAttachments());
			setupEMTWindowList[i_enclaveWindow] = false;
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Enclave Cliques");
			m_social_cliques = null;//new SocialCliques(jdbcView,
									// aliasFilename, this);
			setupEMTWindowList[i_usercliquesWindow] = false;
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Similiar Users");
			//s02 kewang find similar users panel
			m_similarusers = null;// new SimilarUsers(jdbcView, jdbcView,
								  // m_md5Renderer, this); //by
			setupEMTWindowList[i_similiarWindow] = false;

			// Ke
			// Wang
			//s02 kewang
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Usage Historgrams");
			m_histogramcomp = null;//new UsageHistogram(jdbcView, jdbcView,
								   // m_alerts, m_md5Renderer, this);
			setupEMTWindowList[i_usageWindow] = false;

			//s03 kewang
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Average Communication");
			m_averageComm = null;//new AverageCommTime(jdbcView, jdbcView,
								 // m_alerts, m_md5Renderer, this);
			setupEMTWindowList[i_communicationWindow] = false;
						
			//summer06 ryanrowe
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Social Hierarchies");
			m_shDetector = null;
			setupEMTWindowList[i_shdetectorWindow] = false;
			setupEMTWindowList[i_doclflowWindow] = false;
			m_DocFlow = null;
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Email Flow");
			m_emailflow = null;//new EmailFlow(jdbcView, jdbcView,
							   // m_md5Renderer, this);
			setupEMTWindowList[i_emailflowWindow] = false;

			splashScreen.progress(i++, max);
			splashScreen.setMSG("Visual Cliques");
			m_visualclique = null;// new VisualClique(jdbcView, jdbcView,
								  // aliasFilename, this, stoplist);
			setupEMTWindowList[i_graphiccliquesWindow] = false;

			splashScreen.progress(i++, max);
			splashScreen.setMSG("Virus Scanning");
			m_virusSim = null;//new VirusDetectModels(jdbcView, jdbcView,
							  // m_md5Renderer, this, m_alerts);
			setupEMTWindowList[i_virusWindow] = false;

			//m_virusSim = new
			// VirusSimulationWin(m_jdbc,jdbcView,m_md5Renderer,this);
			splashScreen.progress(i++, max);
			//	m_hellingerclique = new
			// HellingerCliqueTrainer(m_jdbc,jdbcView,aliasFilename,this,
			// m_md5Renderer);
			splashScreen.setMSG("Attachment Analyzer");
			m_fanalyzer = null;//new FileAnalyzer(jdbcView, jdbcView,
							   // m_md5Renderer, this);
			setupEMTWindowList[i_analyzerWindow] = false;

			splashScreen.progress(i++, max);
			//s02

			//bw.progress(i++,max);
			//on
			// addresslist panel
			splashScreen.setMSG("Rcpt Frequency");
			m_addresslist = null;//new AddressList(jdbcView, aliasFilename,
								 // this);//on
			setupEMTWindowList[i_rcptWindow] = false;

			splashScreen.progress(i++, max);
			//email figures panel
			splashScreen.setMSG("Configuration");
			m_figures = new EmailInternalConfigurationWindow(ec, jdbcView, this); //s02
			setupEMTWindowList[i_featuresWindow] = true;

			splashScreen.progress(i++, max);
			// messages panel
			splashScreen.setMSG(st_MessageWindow);

			//lets show the welcome screen
			m_welcome = new WelcomeEMTWindow(this,ec);
			setupEMTWindowList[i_welcomeWindow] = true;
			
			m_messages = new MessageWindow(ec, jdbcView, m_md5Renderer, this);
			setupEMTWindowList[i_messageWindow] = true;

			//m_messages.set_win_gui(this);//added for model buidling from
			// messgae windows
			m_messages.setup();
			splashScreen.progress(i++, max);
			splashScreen.setMSG("Load Window");
			m_loader = new LoadDB(m_dbHost, m_dbName, m_user, m_password, jdbcView, this, m_alerts, wordlist,m_statusBar, m_emtconfig); //s02
			setupEMTWindowList[i_loadWindow] = true;

			splashScreen.progress(i++, max);
			m_loader.addActionListener(this);
						splashScreen.progress(i++, max);
			splashScreen.setMSG("SQL View");
			m_sqlview = null;//new dbSQL(jdbcView);
			splashScreen.progress(i++, max);
			// do the initial population & bootstrapping
			if (m_verbose) {
				long before, after;
				//System.out.println("242");

				System.out.print("Intializing messages... ");
				before = System.currentTimeMillis();
				m_messages.Refresh(true);
				after = System.currentTimeMillis();
				System.out.println(((after - before) / 1000.0) + " sec");

				if (!m_forensicMode) {
					System.out.println("Initializing attachments... ");
					before = System.currentTimeMillis();
					m_attachments.Refresh(true);
					after = System.currentTimeMillis();
					System.out.println("    Total... " + ((after - before) / 1000.0) + " sec");
				}

				if (!m_forensicMode) {
					System.out.print("Initializing account statistics... ");
					before = System.currentTimeMillis();
					//s02
					//m_bursts.Refresh("Training on user behavior...",
					// Bursts.INIT_GRANULARITY, true, false);
					after = System.currentTimeMillis();
					System.out.println(((after - before) / 1000.0) + " sec");
				}

				/**
				 * S02
				 */

				
				long totalAfter = System.currentTimeMillis();
				System.out.println("Intialization completed in " + ((totalAfter - totalBefore) / 1000.0) + " sec");
			} else {
				//System.out.println("2422");

				m_messages.Refresh(true);

				if (!m_forensicMode) {
					m_attachments.Refresh(true);
					//m_bursts.Refresh("Training on user behavior...",
					// Bursts.INIT_GRANULARITY, true, false);
				}
				
				//on
				if (m_addresslist != null)
					m_addresslist.Refresh();
				//on

			}
			splashScreen.setVisible(false);
			splashScreen.dispose();
			splashScreen = null;

			// add all tabs

            
            //changed to add minimum elements to jump start emt, will add in every item as it is chosen by user.....
			m_EMTWindow_tabbedPane.add(st_WelcomeWindow,m_welcome);
			m_EMTWindow_tabbedPane.add(st_LoadWindow, m_loader);
			m_EMTWindow_tabbedPane.add(st_MessageWindow, m_messages);
			m_EMTWindow_tabbedPane.add(st_ConfigWindow, m_figures);
			m_EMTWindow_tabbedPane.add(st_AttachWindow, m_attachments);
			m_EMTWindow_tabbedPane.add(st_ReportWindow, m_alerts);
			
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Could not intialize1: " );
			ex.printStackTrace();
			System.exit(1);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Could not intialize2: ");
			ex.printStackTrace();
			System.exit(1);
		}

		//defaults to message view unless empty
		/*if (g_totalCount > 0)
			cb.setSelectedItem("Messages");
		else
			cb.setSelectedItem("Load Data");
		*/
		//will start on welcome screen unless user has chosen to start elsewhere....
		mainChoiceBox.setSelectedIndex(m_welcome.getChooseWindow());     //st_WelcomeWindow);
		

	}

	/**
     * get current user information, when you click on the Info button
     * 
     * @author weijen
	 */
    private final void getInfo() {
		BusyWindow bw = new BusyWindow("Computing data...", "Progress",true);
		bw.setVisible(true);
		int max = 6;
		int imax = 0;
		bw.progress(imax++, max);
		bw.setMSG("Reading database...");
		String[] date = getDateRange_toptoolbar();
		String user = (String) getSelectedUser();
		String query = "select max(dates),min(dates) from email where (sender='" + user + "' or rcpt='" + user
				+ "') and (dates>='" + date[0] + "' and dates<='" + date[1] + "')";
		//System.out.println(query);

		//get min, max date...
		String[][] ddd = null;
		try {
			synchronized (jdbcView) {
				ddd = jdbcView.getSQLData(query);
				//ddd = jdbcView.getRowData();
			}
		} catch (SQLException ex) {
			if (!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
				System.out.println("ex: " + ex);
				System.out.println("sql: " + query);
			}
		}

		query = "select dates,count(mailref) from email where sender='" + user + "' and dates>='" + date[0]
				+ "' and dates<='" + date[1] + "' group by dates order by dates";

		bw.progress(imax++, max);
		//get all...
		String[][] data = null;
		try {
			//need to synchoronize db access for different parts of emt...
			synchronized (jdbcView) {
				data = jdbcView.getSQLData(query);
				//data = jdbcView.getRowData();
			}
		} catch (SQLException ex) {
			if (!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
				System.out.println("ex: " + ex);
				System.out.println("sql: " + query);
			}
		}

		query = "select dates,count(mailref) from email where rcpt='" + user + "' and dates>='" + date[0] + "' and dates<='"
				+ date[1] + "' group by dates order by dates";

		bw.progress(imax++, max);
		//get all...
		String[][] data2 = null;
		try {
			synchronized (jdbcView) {
				data2 = jdbcView.getSQLData(query);
				//data2 = jdbcView.getRowData();
			}
		} catch (SQLException ex) {
			if (!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
				System.out.println("ex: " + ex);
				System.out.println("sql: " + query);
			}
		}

		String maxDay = ddd[0][0];
		String minDay = ddd[0][1];
		System.out.println("maxday: " + maxDay + " " + minDay);
		//the list of information...for the list
		ArrayList<String> ylist = new ArrayList<String>();
		ArrayList<String> mlist = new ArrayList<String>();
		ArrayList<String> dlist = new ArrayList<String>();
		ArrayList<String> ylist2 = new ArrayList<String>();
		ArrayList<String> mlist2 = new ArrayList<String>();
		ArrayList<String> dlist2 = new ArrayList<String>();
		ylist.add("year  (# of emails)");
		ylist2.add("year  (# of emails)");
		mlist.add("month  (# of emails)");
		mlist2.add("month  (# of emails)");
		dlist.add("day  (# of emails)");
		dlist2.add("day  (# of emails)");

		bw.progress(imax++, max);
		bw.setMSG("Computing statistics...");
		//outbound
		double[] year = new double[1];
		double[] month = new double[1];
		double[] day = new double[1];
		int totalEmails = 0;
		//because the sun saving time...
		long oneday = 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000;
		long time = 0;

		if (data != null && data.length > 0) {
			//keep the count, for the plots
			Vector<String> yearly = new Vector<String>();
			Vector<String> monthly = new Vector<String>();
			Vector<String> daily = new Vector<String>();

			//get the first day
			//String today = data[0][0];
			String today = minDay;
			String[] tmp1 = today.split("-");
			int tmp2 = Integer.parseInt(data[0][1]);
			//			String currDay = tmp1[2];

			//why I use this way?
			//to fix a small but hard problem
			String currMonth = tmp1[0] + "-" + tmp1[1];

			String currYear = tmp1[0];
			int ycount = tmp2;
			int mcount = tmp2;
			int dcount = tmp2;

			//if the user has only one data
			if (data.length == 1) {
				totalEmails += tmp2;
				//ylist.add(tmp1[0]+" ("+totalEmails+")");
				//mlist.add(tmp1[0]+"-"+tmp1[1]+" ("+totalEmails+")");
				//dlist.add(tmp1[0]+"-"+tmp1[1]+"-"+tmp1[2]+"
				// ("+totalEmails+")");
			}

			String lastyear = tmp1[0];
			String lastmonth = tmp1[1];
			String yesterday = today;
			String toCompare;
			int count;
			for (int i = 1; i < data.length;) {

				toCompare = data[i][0];
				//System.out.println(today+" "+toCompare);
				if (today.equals(toCompare)) {
					//long time = Utils.getMill2(today);
					count = Integer.parseInt(data[i][1]);
					totalEmails += count;
					//System.out.println(i+" "+data[i][0]+" "+data.length);
					String[] tmpd = toCompare.split("-");
					if (currYear.equals(tmpd[0])) {
						ycount += count;
					} else {
						//compute if there is an empty year
						//this thing is important
						int yearDiff = Integer.parseInt(tmpd[0]) - Integer.parseInt(currYear);

						int numOfEmpty = yearDiff - 1;
						for (int j = 0; j < numOfEmpty; j++) {
							yearly.add(String.valueOf(0));
						}

						lastyear = currYear;
						currYear = tmpd[0];
						yearly.add(String.valueOf(ycount));

						//String[] tmplastyear = lastyear.split("-");
						ylist.add(lastyear + "  (" + ycount + ")");

						ycount = count;
					}

					if (currMonth.equals(tmpd[0] + "-" + tmpd[1])) {
						mcount += count;
					} else {
						//compute if there is an empty month
						//this thing is important
						int yearDiff = Integer.parseInt(tmpd[0]) - Integer.parseInt(currYear);

						String[] tmp3 = currMonth.split("-");
						int monthDiff = Integer.parseInt(tmpd[1]) - Integer.parseInt(tmp3[1]);

						int numOfEmpty = (yearDiff - 1) * 12 + monthDiff - 1;
						for (int j = 0; j < numOfEmpty; j++) {
							monthly.add(String.valueOf(0));
						}

						lastmonth = currMonth;
						currMonth = tmpd[0] + "-" + tmpd[1];
						monthly.add(String.valueOf(mcount));

						//String tmplastmonth =
						// lastmonth.substring(0,lastmonth.lastIndexOf("-"));
						mlist.add(lastmonth + "  (" + mcount + ")");

						mcount = count;
					}

					daily.add(String.valueOf(count));
					dlist.add(yesterday + "  (" + count + ")");

					i++;
				}//if(today.equals(toCompare)){
				else {
					daily.add(String.valueOf(0));
				}

				yesterday = today;
				//oneday = 24*60*60*1000;
				time = Utils.getMill2(today);
				//System.out.println(time+" "+today);
				time = time + oneday;

				today = Utils.millisToTimeString2(time);
				//System.out.println(time+" "+today+" "+maxDay+" \n");

				if (Utils.getMill2(today) > Utils.getMill2(maxDay))
					break;

			}

			yearly.add(String.valueOf(ycount));
			monthly.add(String.valueOf(mcount));
			daily.add(String.valueOf(dcount));
			if (ycount > 0)
				ylist.add(currYear + "  (" + ycount + ")");
			if (mcount > 0)
				mlist.add(currMonth + "  (" + mcount + ")");
			if (dcount > 0)
				dlist.add(today + "  (" + dcount + ")");

			bw.progress(imax++, max);

			year = new double[yearly.size()];
			for (int i = 0; i < yearly.size(); i++) {
				year[i] = Double.parseDouble(yearly.get(i));
			}

			month = new double[monthly.size()];
			for (int i = 0; i < monthly.size(); i++) {
				month[i] = Double.parseDouble(monthly.get(i));
			}

			day = new double[daily.size()];
			for (int i = 0; i < daily.size(); i++) {
				day[i] = Double.parseDouble(daily.get(i));
			}
		}

		//inbound
		double[] year2 = new double[1];
		double[] month2 = new double[1];
		double[] day2 = new double[1];
		bw.progress(imax++, max);
		int totalEmails2 = 0;
		if (data2 != null && data2.length > 0) {
			Vector<String> yearly = new Vector<String>();
			Vector<String> monthly = new Vector<String>();
			Vector<String> daily = new Vector<String>();

			//get the first day
			//String today = data2[0][0];
			String today = minDay;
			String[] tmp1 = today.split("-");
			int tmp2 = Integer.parseInt(data2[0][1]);
			//		String currDay = tmp1[2];
			String currMonth = tmp1[0] + "-" + tmp1[1];
			String currYear = tmp1[0];
			int ycount = tmp2;
			int mcount = tmp2;
			int dcount = tmp2;

			//if the user has only one data
			if (data2.length == 1) {
				totalEmails2 += tmp2;
			}

			String lastyear = tmp1[0];
			String lastmonth = tmp1[1];
			String yesterday = today;
			int count = 0;
			String toCompare = "";
			for (int i = 1; i < data2.length;) {

				toCompare = data2[i][0];
				if (today.equals(toCompare)) {
					count = Integer.parseInt(data2[i][1]);
					totalEmails2 += count;

					String[] tmpd = toCompare.split("-");
					if (currYear.equals(tmpd[0])) {
						ycount += count;
					} else {
						//compute if there is an empty year
						//this thing is important
						int yearDiff = Integer.parseInt(tmpd[0]) - Integer.parseInt(currYear);

						int numOfEmpty = yearDiff - 1;
						for (int j = 0; j < numOfEmpty; j++) {
							yearly.add(String.valueOf(0));
						}

						lastyear = currYear;
						currYear = tmpd[0];
						yearly.add(String.valueOf(ycount));

						ylist2.add(lastyear + "  (" + ycount + ")");

						ycount = count;
					}

					if (currMonth.equals(tmpd[0] + "-" + tmpd[1])) {
						mcount += count;
					} else {
						//compute if there is an empty month
						//this thing is important
						int yearDiff = Integer.parseInt(tmpd[0]) - Integer.parseInt(currYear);

						String[] tmp3 = currMonth.split("-");
						int monthDiff = Integer.parseInt(tmpd[1]) - Integer.parseInt(tmp3[1]);

						int numOfEmpty = (yearDiff - 1) * 12 + monthDiff - 1;
						for (int j = 0; j < numOfEmpty; j++) {
							monthly.add(String.valueOf(0));
						}

						lastmonth = currMonth;
						currMonth = tmpd[0] + "-" + tmpd[1];
						monthly.add(String.valueOf(mcount));

						mlist2.add(lastmonth + "  (" + mcount + ")");

						mcount = count;
					}

					daily.add(String.valueOf(count));
					dlist2.add(yesterday + "  (" + count + ")");

					i++;
				}//if(today.equals(toCompare)){
				else {
					daily.add(String.valueOf(0));
				}

				yesterday = today;
				//System.out.println(time+" "+today+" ");
				//oneday = 24*60*60*1000;
				time = Utils.getMill2(today);
				time = time + oneday;

				today = Utils.millisToTimeString2(time);
				//System.out.println(time+" "+today+" \n");
				if (Utils.getMill2(today) > Utils.getMill2(maxDay))
					break;

			}

			yearly.add(String.valueOf(ycount));
			monthly.add(String.valueOf(mcount));
			daily.add(String.valueOf(dcount));
			if (ycount > 0)
				ylist2.add(currYear + "  (" + ycount + ")");
			if (mcount > 0)
				mlist2.add(currMonth + "  (" + mcount + ")");
			if (dcount > 0)
				dlist2.add(today + "  (" + dcount + ")");

			bw.progress(imax++, max);

			year2 = new double[yearly.size()];
			for (int i = 0; i < yearly.size(); i++) {
				year2[i] = Double.parseDouble(yearly.get(i));
			}

			month2 = new double[monthly.size()];
			for (int i = 0; i < monthly.size(); i++) {
				month2[i] = Double.parseDouble(monthly.get(i));
			}

			day2 = new double[daily.size()];
			for (int i = 0; i < daily.size(); i++) {
				day2[i] = Double.parseDouble(daily.get(i));
			}
		}

		bw.progress(imax++, max);
		bw.setMSG("Setup figure...");
		String[] message = new String[3];
		message[0] = "Selected user (email): " + user;
		message[1] = "Minimum date of selected user: " + ddd[0][1] + "Maximum date of selected user: " + ddd[0][0];
		message[2] = "Count as sender: " + totalEmails + "Count as recipient: " + totalEmails2;
		VCliqueInfo info = new VCliqueInfo(message, this, ylist, ylist2, mlist, mlist2, dlist, dlist2);

		info.setFigure("Outbound Email Distribution (yearly)", "Year", "# of Emails", info.plots[0], Color.blue, year);

		info.setFigure("Outbound Email Distribution (monthly)", "Month", "# of Emails", info.plots[1], Color.blue,
				month);

		info.setFigure("Outbound Email Distribution (daily)", "Day", "# of Emails", info.plots[2], Color.blue, day);

		info.setFigure("Inbound Email Distribution (yearly)", "Year", "# of Emails", info.plots[3], Color.red, year2);

		info
				.setFigure("Inbound Email Distribution (monthly)", "Month", "# of Emails", info.plots[4], Color.red,
						month2);

		info.setFigure("Inbound Email Distribution (daily)", "Day", "# of Emails", info.plots[5], Color.red, day2);

		bw.setVisible(false);
		info.setVisible(true);
	}

    /**
     * Turn on the user drop box of users
     *
     */
    public void enableEmailList(){
        Component c = comboBoxPane.getComponentAtIndex(1);
        c.setEnabled(true);
    }
    
    /**
     * turn off the user email list
     *
     */
    public void disableEmailList(){
        Component c = comboBoxPane.getComponentAtIndex(1);
        c.setEnabled(false);
    }
    
	/**
	 * This will change the windows when the selection box is changed
	 */
	public void itemStateChanged(ItemEvent evt) {

	    
	    if(evt.getStateChange() != ItemEvent.SELECTED)
	    {
	        return;
	    }
	    
		CardLayout cl = (CardLayout) (m_EMTWindow_tabbedPane.getLayout());
		int n = mainChoiceBox.getSelectedIndex();
		//setupEMTComponent
		if (!setupEMTWindowList[n]) {
			setupEMTComponent(n);
		}
		cl.show(m_EMTWindow_tabbedPane, (String) evt.getItem());
		
		

		Component c;

		if (n == i_loadWindow || n == i_welcomeWindow || n == i_messageWindow || n == i_enclaveWindow || n == i_reportWindow
				|| n == i_sqlviewWindow ||n == i_experimentalWindow|| n== i_featuresWindow ) {
			for (int i = 1; i < 7; i++) {
				c = comboBoxPane.getComponentAtIndex(i);
				c.setEnabled(false);
			}

		} else {

			for (int i = 1; i < 7; i++) {
				c = comboBoxPane.getComponentAtIndex(i);
				c.setEnabled(true);
			}
		}

		switch (n) {
			case (i_loadWindow) :
				m_statusBar.setText("Loading the data window");
				break;
			case (i_welcomeWindow):
				m_statusBar.setText("Welcome Screen for EMT, choose where to go from here");
				break;
			case (i_messageWindow) :
				m_statusBar.setText("View messages and use machine learning models");
				break;
			case (i_featuresWindow) :
				m_statusBar.setText("Choose Email Features Configuration");
				break;
			case (i_usageWindow) :
				m_statusBar.setText("See Usage history");
				break;
			case (i_enclaveWindow) :
				m_statusBar.setText("Explore Enclave Groups");
				break;
			case (i_usercliquesWindow) :
				m_statusBar.setText("Individual Groups");
				break;
			case (i_similiarWindow) :
				m_statusBar.setText("Explore Users who are similar");
				break;
			case (i_communicationWindow) :
				m_statusBar.setText("Importance of email response");
				break;
			case (i_rcptWindow) :
				m_statusBar.setText("Rcpt behaviour");
				break;
			case (i_attachmentWindow) :
				m_statusBar.setText("Study attachments");
				break;
			case (i_emailflowWindow) :
				m_statusBar.setText("Flow of email around the system");
				break;
			case (i_graphiccliquesWindow) :
				m_statusBar.setText("Explore the groups graphically");
				break;
			case (i_virusWindow) :
				m_statusBar.setText("Find likly virus");
				break;
			case (i_analyzerWindow) :
				m_statusBar.setText("Analyze attachments");
				break;
			case (i_reportWindow) :
				m_statusBar.setText("Create Reports and Step through Forensic Email Investigations");
				break;
			case (i_sqlviewWindow) :
				m_statusBar.setText("Use/test Raw SQL on database");
				break;
            case(i_searchWindow):
                m_statusBar.setText("Search the database");
            	break;
            case(i_financeWindow):
            	m_statusBar.setText("Social Networks applied email or finance");
            	break;
            case(i_shdetectorWindow):
            	m_statusBar.setText("Detection of Social Hierarchy");
            case(i_doclflowWindow):
            	m_statusBar.setText("Detect similiar documents as flows");
		}

		//need to update the indiv window settings to reflect changes made
		// while it wasnt up
		Execute();

	}

	

	public void actionPerformed(java.awt.event.ActionEvent e) {
	
		if (e.getSource() == m_miExit) {
			try {
				if (JOptionPane.showConfirmDialog(this, "Exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
			} catch (Exception ex) {
			}
			System.exit(0);//need to replace with save call
		} else if (e.getSource() == m_miOptions) {
			JDialog dialog = new JDialog(this, "Alert Options", false);
			dialog.setContentPane(m_options);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}else if (e.getSource() == m_miClearAlerts) {
			m_alerts.clear();
		} else if (e.getSource() == m_miPreferences) {
			JDialog dialog = new JDialog(this, "Preferences", false);
			m_preferences.readSettingsFromFile();
			dialog.setContentPane(m_preferences);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			
		} else if (e.getSource() == m_helpabout) {
			EMTHelp m_emtHelp = new EMTHelp(15);
			m_emtHelp.showME();
		} else if (e.getSource() == m_helpwalk) {
			EMTHelp m_emtHelp = new EMTHelp(21);
			m_emtHelp.showME();

		} else if (e.getSource() == m_showsplash) {
			//JDialog jw = new JDialog();
			JFrame showwindow = new JFrame();
			final SplashScreen splashScreen = new SplashScreen();
			showwindow.setContentPane(splashScreen.getContentPane());
			showwindow.addWindowListener(splashScreen);
			showwindow.pack();
			final Dimension screenDim = showwindow.getToolkit().getScreenSize();

			/* Center the window */
			final Rectangle winDim = showwindow.getBounds();
			System.out.println("\nwindim" + winDim);
			showwindow.setLocation((screenDim.width - winDim.width) / 2,
					  (screenDim.height - winDim.height-36) / 2);
			showwindow.setVisible(true);
		}

		else if (e.getSource() == m_loader) {
			// data in database changed
			try {
				// reload messages
				m_messages.Refresh(true, true, false, null);
				// reload features tab
				m_figures.Refresh();
				// histogram comparison
				if (m_histogramcomp != null)
					m_histogramcomp.Refresh();
				//average comm time
				if (m_averageComm != null)
					m_averageComm.Refresh();
				//Social Hierarchy Detection
				if (m_shDetector!= null)
					m_shDetector.Refresh();
				if(m_DocFlow!=null){
					m_DocFlow.Refresh();
				}
				// address list
				if (m_addresslist != null)
					m_addresslist.Refresh();
			} catch (java.sql.SQLException ex) {
				JOptionPane.showMessageDialog(this, "After succesful database load, automatic refresh failed: " + ex);
			}
		} else if (e.getSource().getClass().equals(JMenuItem.class)) {

			if (e.getActionCommand().startsWith("Specific Window Help")) {
				//need to pop up specific window help

			} else {
				JMenuItem temp = (JMenuItem) e.getSource();

				try {
					int n = Integer.parseInt(temp.getAccessibleContext().getAccessibleDescription());
					mainChoiceBox.setSelectedIndex(n);

				} catch (NumberFormatException nfe) {
					return;
				}
			}

		}

	}

	private void createStatusBar() {
		JPanel panel = new JPanel();
		Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray);
		panel.setBorder(border);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		m_statusBar = new JLabel(" ");
		m_statusBar
				.setFont(m_statusBar.getFont().deriveFont(Font.PLAIN, m_statusBar.getFont().getSize() * (float) .80));
		m_statusBar.setForeground(Color.black);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(m_statusBar);
		getContentPane().add(panel, BorderLayout.SOUTH); // JFrame uses BorderLayout
	}

	/*private JLabel getStatusBar() {
		return m_statusBar;
	}*/

	/*
	private void updateStatusBar() {
		if (m_forensicMode) {
			// don't show refresh in forensic mode
			return;
		}
		if (m_statusBar != null) {
			m_statusBar.setText("Last Refresh at " + (new Date()));
		}
	}
	*/
	
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			m_cachedEnabledState = new Hashtable<Component,Boolean>();
		}

		setEnabled(this, enabled);

		if (enabled) {
			m_cachedEnabledState = null;
		}
	}

	/**
	 * Disables and enables components recursively. When disabling, it caches
	 * the original state of each component to restore it on enable
	 */
	public void setEnabled(Container c, boolean enabled) {
		Component[] components = c.getComponents();

		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof Container) {
				setEnabled((Container) components[i], enabled);
			}

			// never disable these
			if (components[i] instanceof JTabbedPane)
				continue;
			if (components[i] instanceof JScrollBar)
				continue;
			if (components[i] instanceof JScrollPane)
				continue;
			if (components[i] instanceof JLabel)
				continue;

			boolean realEnabled = enabled;

			if (!enabled) {
				m_cachedEnabledState.put(components[i], new Boolean(components[i].isEnabled()));
			} else if (m_cachedEnabledState != null) {
				Boolean b = m_cachedEnabledState.get(components[i]);
				if (b != null) {
					realEnabled = b.booleanValue();
				}
			}

			components[i].setEnabled(realEnabled);
		}
	}

	/**
	 * Main method for EMT, will setup variables and start the ball rolling.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		//this will be used to make sure EMT has been started using the config files...else we will not allow to continue until it has been created
		boolean wehaveconfigfile = false;
		for(int i=0;i<args.length;i++){

			if(args[i].equalsIgnoreCase("-c")){
				wehaveconfigfile = true;
			}
		}
		
		
		
		String wordlist = null; //will see if we have a wordlist defined
		String stoplist = null; //stop list for stop words
		String show = null;     //if to show the config window
		String dosbatch = null;
		String dbHost = null;
		String dbName = null;
		String dbUsername = null;
		String dbPassword = null;
		String mysql = null;
		String aliasFilename = null;
		int schemeChoice =2;
		int refreshRate = 30;
		//long bootstrapSeconds = 0;
		long startAt = -1;
		boolean verbose = false;
		//boolean debug = false;
		//boolean dormant = false;
		EMTConfiguration emtconfig = new EMTConfiguration();
		
		
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-c")) {
					if (!emtconfig.loadFileFromDisk((args[++i]))) {
						System.out.println("problem loading the config file");
					}
					emtconfig.putProperty(EMTConfiguration.CONFIGNAME, args[i]);
				} else if (args[i].equals("-w")) {
					wordlist = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.WORDLIST)) {
						emtconfig.putProperty(EMTConfiguration.WORDLIST, wordlist);
					}
				} else if (args[i].equals("-s")) {
					stoplist = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.STOPLIST)) {
						emtconfig.putProperty(EMTConfiguration.STOPLIST, stoplist);
					}
				} else if (args[i].equals("-dosb")) {
					dosbatch = args[++i];
					emtconfig.putProperty(EMTConfiguration.DOS_BATCH_FILE, dosbatch);

				} else if (args[i].equals("-sh")) {
					show = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.SHOW)) {
						emtconfig.putProperty(EMTConfiguration.SHOW, show);
					}
				} //else if (args[i].equals("-bb")) {
				//	bashbatch = args[++i];
				//}

				else if (args[i].equals("-h")) {
					dbHost = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.DBHOST)) {
						emtconfig.putProperty(EMTConfiguration.DBHOST, dbHost);
					}
				} else if (args[i].equals("-db")) {
					dbName = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.DBNAME)) {
						emtconfig.putProperty(EMTConfiguration.DBNAME, dbName);
					}
				} else if (args[i].equals("-u")) {
					dbUsername = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.DBUSERNAME)) {
						emtconfig.putProperty(EMTConfiguration.DBUSERNAME, dbUsername);
					}
				} else if (args[i].equals("-p")) {
					if (!args[i + 1].startsWith("-"))
						dbPassword = args[++i];
					else
						dbPassword = "";
					if (!emtconfig.hasProperty(EMTConfiguration.DBPASSWORD)) {
						emtconfig.putProperty(EMTConfiguration.DBPASSWORD, dbPassword);
					}
				} else if (args[i].equals("-mysql")) {
					mysql = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.DBPATH)) {
						emtconfig.putProperty(EMTConfiguration.DBPATH, mysql);
					}
				} else if (args[i].equals("-alias")) {
					aliasFilename = args[++i];
					if (!emtconfig.hasProperty(EMTConfiguration.ALIASFILENAME)) {
						emtconfig.putProperty(EMTConfiguration.ALIASFILENAME, aliasFilename);
					}
				} else if (args[i].equals("-ref")) {
					refreshRate = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-boot")) {
					//bootstrapSeconds = 
					    Long.parseLong(args[++i]);
				} else if (args[i].equals("-start")) {
					startAt = Long.parseLong(args[++i]);
				} else if (args[i].equals("-v")) {
					verbose = true;
				} else if (args[i].equals("-d")) {
					//debug = true;
				} else if (args[i].equals("-z")) {
					//dormant = true;
				} else {
					throw (new Exception());
				}
			}
		} catch (Exception e) {
			System.out.println("unknown arg");
            final String usage = "usage: java -Xmx512M -jar emt.jar [-c configfile]\n\njava metdemo/winGui [-d] [-v] [-z] [-bb bash-batch-file-name] [-dosb dos-starting-file-name] [-sh [true/flase]if-show-config-on-startup] [-ref refreshRate] [-boot bootstrapSeconds] -h dbHost -db dbName -u dbUsername -p dbPassword -mysql mysqlPath -alias aliasFilename -w wordlist -s stoplist";
			System.err.println(usage);
			System.exit(1);
		}
//now to re-read the stuff from configuration file
		if (emtconfig.hasProperty(EMTConfiguration.WORDLIST)) {
			wordlist = emtconfig.getProperty(EMTConfiguration.WORDLIST);
		}
		if (emtconfig.hasProperty(EMTConfiguration.STOPLIST)) {
			stoplist = emtconfig.getProperty(EMTConfiguration.STOPLIST);
		}
		if (emtconfig.hasProperty(EMTConfiguration.SHOW)) {
			show = emtconfig.getProperty(EMTConfiguration.SHOW);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DBHOST)) {
			dbHost = emtconfig.getProperty(EMTConfiguration.DBHOST);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DBNAME)) {
			dbName = emtconfig.getProperty(EMTConfiguration.DBNAME);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DBUSERNAME)) {
			dbUsername = emtconfig.getProperty(EMTConfiguration.DBUSERNAME);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DBPASSWORD)) {
			dbPassword = emtconfig.getProperty(EMTConfiguration.DBPASSWORD);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DBPATH)) {
			//dbPath = emtconfig.getProperty(EMTConfiguration.DBPATH);
		}
		if (emtconfig.hasProperty(EMTConfiguration.ALIASFILENAME)) {
			aliasFilename = emtconfig.getProperty(EMTConfiguration.ALIASFILENAME);
		}
		if (emtconfig.hasProperty(EMTConfiguration.DOS_BATCH_FILE)) {
			dosbatch = emtconfig.getProperty(EMTConfiguration.DOS_BATCH_FILE);
		}

		if (emtconfig.hasProperty(EMTConfiguration.DBTYPE)) {
			//dbType = emtconfig.getProperty(EMTConfiguration.DBTYPE);
		}
		if(emtconfig.hasProperty(EMTConfiguration.SCHEME_COLOR)){
			schemeChoice = Integer.parseInt(emtconfig.getProperty(EMTConfiguration.SCHEME_COLOR));
		}
		
		
		//now to use that choice
		try{
			System.out.println("Evaluating scheme....");
			if(schemeChoice ==1){
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}else if(schemeChoice == 2){
				UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
				SubstanceLookAndFeel.setCurrentTheme(new SubstanceLightAquaTheme());
			}else if(schemeChoice == 3){
				UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel");
			}
			
			//UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");
			//
			//UIManager.setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel");
		
			//SubstanceLookAndFeel.setCurrentTheme(new SubstanceOliveTheme());
           // javax.swing.UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
        } catch(Exception e) {
            System.out.println(e);
        }



		if ((dbHost == null) || (dbName == null) || (dbUsername == null) || (aliasFilename == null)
				|| (wordlist == null) || (stoplist == null) || (show == null) || show.toLowerCase().startsWith("true"))//||
		{
			Preferences preferences = new Preferences(emtconfig,wehaveconfigfile);
			JDialog dialog = new JDialog();
			dialog.setTitle("Preferences");
			dialog.setModal(true);
			preferences.readSettingsFromFile();
			dialog.setContentPane(preferences);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			preferences.grabFocusButton();
			dialog.setVisible(true);
			
			//chek if to continue onto emt
			if (!preferences.getStatus().equals("continue"))
				System.exit(1);
		}
		final EMTDatabaseConnection  m_jdbcView = DatabaseManager.LoadDB(dbHost, dbName, dbUsername, dbPassword, emtconfig.getProperty(EMTConfiguration.DBTYPE));
        
		try{
			//check for failure
			if (m_jdbcView.connect(true) == false) {
				System.err.println("Couldn't connect to database with these paramaters, check the batch file, will exit now");
				System.exit(-1);
			}
		}catch(DatabaseConnectionException de){
			de.printStackTrace();
			System.exit(-2);
		}
	
		winGui frame = new winGui(dbHost, dbName, dbUsername, dbPassword,/* mysql,*/ aliasFilename, wordlist, stoplist,
				startAt, refreshRate, /*bootstrapSeconds,*/ verbose, /*debug, dormant,*/ emtconfig, m_jdbcView);
		//dosbatch, bashbatch);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//need to clean up by delete all TEMP_ATT* files
				//only done windows:
				try {
					//we would like to remove all files beginning with the name TEMP_ATT...
					/*
					 * File currentdir = new File(System.getProperty("user.dir"));
					if(currentdir.isDirectory()){}
					else{
						currentdir = currentdir.getParentFile();
					}
					//we know we are at a direct
					
					File []filesindir = currentdir.listFiles();
					for(int i=0;i<filesindir.length;i++){
						
						if(filesindir[i].getName().toLowerCase().startsWith("temp_att")){
							if(filesindir[i].delete()){
								System.out.println("removed " + filesindir[i]);
							}
						}
						
					}*/
					
				Utils.SystemIndependantRecursiveDeleteMethod(System.getProperty("user.dir"),"temp_att",null,false,false);
					
					
					/*
					String osName = System.getProperty("os.name");
					//System.out.println("running: " + osName);
					String cmds[] = null;

					if (osName.indexOf("Win") != -1) {
						cmds = new String[3];
						cmds[0] = "cmd";
						cmds[1] = "/c";
						cmds[2] = "del TEMP_ATT*";
					} else {
						cmds = new String[1];
						cmds[0] = "rm TEMP_ATT*";
					}
					Runtime rt = Runtime.getRuntime();
					Process proc = rt.exec(cmds);
					int exitVal = proc.waitFor();

					System.out.println("Cleanup exitValue: " + exitVal);
					*/
					System.out.println("Attemp to shutdown the db connection");
					m_jdbcView.shutdown();
				} /*catch (IOException eeee) {
					System.out.println("msg: " + eeee);
				}*/catch(DatabaseConnectionException sd){ 
				  sd.printStackTrace();
				}
				/*catch (InterruptedException e23) {
				}*/

				File checksum = new File(System.getProperty("CHECKFILE"));
				checksum.delete();

				System.out.println("Ending EMT now. Thanks and have a great day!");
				System.exit(0);
			}
		});
		frame.pack();
		frame.setSize(930, 770); //better view on windows
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	//dummy one for Ke's test
	public void setGroups(Vector<String> v) {

		if (m_messages != null)//shlomo stuff
			m_messages.setGroups(v);

	}
	
    
	/** helper finction to return user in msg window */
	public final String getMsgTabUser() {

		return m_messages.getMsgTabUser();

	}

	/**
	 * Helper function to set and switch to message view
	 * @param name
	 */
	public final void setMessageView(String name){
		//need to switch to message window
		mainChoiceBox.setSelectedIndex(i_messageWindow);
		ItemEvent t = new ItemEvent(mainChoiceBox,0,new String(st_MessageWindow),0);
		this.itemStateChanged(t);
		m_messages.setMessageView(name);
		
	}
	
	
	
	public final boolean setWindow(String windowString){
		
		int n = getEventID(windowString);
		if(n<0){
			System.out.println("n is neg");
			return false;
		}
		mainChoiceBox.setSelectedIndex(n);
		ItemEvent t = new ItemEvent(mainChoiceBox,0,windowString,0);
		this.itemStateChanged(t);
		
		return true;
	}
	
	
	private final int getEventID(String idString){
		
		for(int i=0;i< mainChoiceBox.getItemCount();i++){
			
			String itemStr = (String)mainChoiceBox.getItemAt(i);
			
			if(itemStr.equals(idString)){
				return i;
			}
			
		}
		
		return -1;
	}
	
	
	/**
	 * Helper function to set and switch to message view
	 * @param name
	 */
	public final void setMessageViewGroup(String[] name){
		//need to switch to message window
		mainChoiceBox.setSelectedIndex(i_messageWindow);
		ItemEvent t = new ItemEvent(mainChoiceBox,0,new String(st_MessageWindow),0);
		this.itemStateChanged(t);
		m_messages.setMessageViewGroup(name);
		
	}
	
	/**
	 * Method to add a user name to the list and make it the chosen one.
	 * @param name
	 * @return
	 */
	public final boolean setUserName(String name) {
		String data[][] = null;
		//		first check if we know about this user
		if (g_userHash.containsKey(name)) {
			int i = 0;
			int num = m_view_user.getItemCount();
			for (i = 0; i < num; i++) {

				if (m_view_user.getItemAt(i).equals(name))
					break;

			}
			m_view_user.setSelectedIndex(i);



		} else {
			try {
				data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),sender from email where sender = '"
						+ name + "' group by sender");

				if (data.length < 0 || data == null) {
					data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),rcpt from email where rcpt = '"
							+ name + "' group by rcpt");
				}
				if (data.length < 0 || data == null) {
					return false;
				}
                
                int c = Integer.parseInt(data[0][2]);
                userShortProfile usp = new userShortProfile(name,data[0][1],data[0][0],c,0);
                g_userHash.put(usp.getName(), usp);
                
                m_view_user.addItem(usp.getName());
				m_view_user.setSelectedIndex(m_view_user.getItemCount());
			} catch (SQLException see) {
				see.printStackTrace();
				return false;
			}
		}
		return true;

	}
	
	
	public final int getNGRAM() {
		return m_figures.getNGram();
	}

	public void setTargetClass(final String s) {
		m_messages.setTargetClass(s);
	}

	public final String getTarget() {
		return m_figures.getTarget();
	}

	public final Hashtable getHash() {
		return m_figures.getHash();
	}

	public final boolean isStop() {
		return m_figures.isStopwords();
	}

	public final boolean isLower() {
		return m_figures.isLower();
	}

	public final String getFeatures() {
		return m_figures.getFeatures();
	}

	public final boolean[] getBooleanFeatures() {
		return m_figures.getselectedFeaturesBoolean();
	}

	public final void setBooleans(final boolean s, final boolean l) {
		m_messages.setBooleans(s, l);
	}

	public final void updateRecords(boolean flushinfofiles, String users) {
		
		setupGlobals(flushinfofiles,users);//reload the user lists.
		m_messages.updateRecords(); //refresh user list and folder list in
		// message window
		//clean up db
		if (m_sqlview == null) {
			setupEMTComponent(i_sqlviewWindow);
		}
		m_sqlview.cleanmailref(false);
		
	}

	/** method to see which type of email attachment to use */
	public final String getAttachType() {
		return m_figures.getAttachType().trim();
	}

	/** return the max data in the db..cached here */
	public final String getMaxDate() {

		return g_maxdate;
	}
	/** return the min data in the db..cached here */
	public final String getMinDate() {

		return g_mindate;
	}

	/** return number of user in drop list */
	public final int getUserCount() {
		return m_view_user.getItemCount();//g_allUsers.length;
	}
	
	/** return the ith user name */
	public final String getUserAt(int i) {
		return (String)m_view_user.getItemAt(i);//g_allUsers[i];
	}

	/** sets the user to ith one */
	public final void setUserSelectedIndex(int i) {
		m_view_user.setSelectedIndex(i);
	}

	/** reutrn selected index */
	public final int getSelectedUserIndex() {
		return m_view_user.getSelectedIndex();
	}

	/** return current selected user */
	public final Object getSelectedUser() {
		return m_view_user.getSelectedItem();
	}

	public final int[] getToday() {
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

	public static final int startDate =0;
	public static final int endDate = 1;
	
/**
 * Get the date as yy-mm-dd
 * @return double array, 0 start and 1 last
 */
	public final String[] getDateRange_toptoolbar() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String[] startAndEnd = new String[2];
		startAndEnd[0] = format.format(m_spinnerModelStart.getDate());
		startAndEnd[1] = format.format(m_spinnerModelEnd.getDate());

		return startAndEnd;
	}

	/** return current db count. cached */
	public final int getCount() {
		return g_totalCount;
	}

	/**
	 * return profile of a user info in the form of a datastructure which contains max(dates), min(dates), count(mailref) (for both in and out)
	 */
	public final userShortProfile getUserInfo(String user) {

		return g_userHash.get(user);
	}

	/** method to update the status bar */
	public final void StatusBar(final String msg) {
		m_statusBar.setText(msg);
	}

	/** method to return cache copy of user list from db */
	public final String[] getUserList() {

        String []userlist = new String[m_view_user.getItemCount()];
        for(int i=0;i<userlist.length;i++){
            userlist[i] = (String)m_view_user.getItemAt(i);
         }
        //TODO: optimize this whole thing
		//return m_view_user.//g_allUsers;
        return userlist;
	}

	/** return the list of attachment types */
	public final String[] getAttachments() {
		return g_allAttachments;
	}


	/**
	 * Method to setup the global db stats, this will create a single reference
	 * point to fetch the data once so that it doesnt need to be refretched by
	 * every component.
	 * @param flushinfofiles if to flush the cache info files associated with the current db
	 * @param sqlusers
	 */
	private final void setupGlobals(boolean flushinfofiles, String sqlusersConstraints) {
		
		System.out.println("In Setup globals"); //lets print out something to show we are alive
		
		boolean notAllusers = false;
		if(!sqlusersConstraints.equals("%@%")){
			notAllusers = true;
			//that is we are constraining in some way, so we should not cache the results.....
		}
		TreeSet<String> spp = null;
        try {
			//synchronized (jdbcView)
			//{
                //we reset the global user count
				g_totalCount = 0;//just in case
		        //will see if the data has changed.....assuming it has not, we can use the max/min dates
				//and max/min uid to profile the data to see if we have been here already....if yes lets load up the data from disk.
				
				String data[][] = jdbcView.getSQLData("select max(dates),min(dates),count(mailref) from email");
				String data2[][] = jdbcView.getSQLData("select max(UID),min(UID) from email");
		        
				if (data.length > 0 && data[0].length > 2) {
					g_maxdate = data[0][0];
					g_mindate = data[0][1];

					try {
						g_totalCount = Integer.parseInt(data[0][2]);

						//now to set max min dates in the roll windows.
						int yr, mnth, dy;
						yr = Integer.parseInt(g_mindate.substring(0, 4));
						mnth = Integer.parseInt(g_mindate.substring(5, 7));
						dy = Integer.parseInt(g_mindate.substring(8, 10));
						cal.set(yr, mnth - 1, dy);
						yr = Integer.parseInt(g_maxdate.substring(0, 4));
						mnth = Integer.parseInt(g_maxdate.substring(5, 7));
						dy = Integer.parseInt(g_maxdate.substring(8, 10));
						cal2.set(yr, mnth - 1, dy);

					} catch (Exception num) {
						System.out.println("0 user count during setup");
						g_totalCount = 0;
						int[] today = getToday();
						cal.set(today[0] - 4, today[1] - 1, today[2]);
						cal2.set(today[0], today[1] - 1, today[2]);
					}
				} else//empty them then
				{
					int[] today = getToday();
					cal.set(today[0] - 3, today[1] - 1, today[2]);
					cal2.set(today[0], today[1] - 1, today[2]);
					g_maxdate = new String();
					g_mindate = new String();
					g_totalCount = 0;
				}

				if (g_userHash == null)
					g_userHash = new Hashtable<String,userShortProfile>();
				else
					g_userHash.clear();
                
//				String temp[] = new String[User_Fields];

				System.out.print("...Initializing stats ");//, starting with

				
		        if(flushinfofiles){
		        	deleteInfoFiles();
		        }

		        File userinfo;
		        //if(user_type == typeX_SENDER){
		        	userinfo = new File(user_type+"_"+minNumber + "_"+(data2[0][0]+data2[0][1] + data[0][0]+data[0][1]).hashCode() + "_" + m_emtconfig.getProperty(EMTConfiguration.DBTYPE)+"_" + m_emtconfig.getProperty(EMTConfiguration.DBNAME)+"_userset.info");
		        /*}else if( user_type == ty)
		        
		        {
		        	userinfo = new File(user_type + "_"+(data2[0][0]+data2[0][1] + data[0][0]+data[0][1]).hashCode() + "_" + m_emtconfig.getProperty(EMTConfiguration.DBTYPE)+"_" + m_emtconfig.getProperty(EMTConfiguration.DBNAME)+"_userset.info");
			        
		        }
*/
		        
		        
		        
		        //if (user_type == typeX_SENDER) {
		        
		        //check if name exists, else will have to create the info
		        if(userinfo.exists() && !notAllusers){
		        	try{
		        		System.out.println("Going to load hashset");
		        	ObjectInputStream oin = new ObjectInputStream(new GZIPInputStream(new FileInputStream(userinfo)));
		        	
		        	int n = oin.readInt();
		        	
		        	for(int i=0;i<n;i++){
		        		userShortProfile usp = (userShortProfile)oin.readObject();
		        		g_userHash.put(usp.getName(),usp);
		        	}
		        	oin.close();
		        	
		        	}catch(ClassNotFoundException dno){
		        	
		        	dno.printStackTrace();
		        	}
		        	catch(IOException ioe){
		        		ioe.printStackTrace();
		        		userinfo.delete();//at least lets make sure it gets created next time around
		        	}
		        }else{
		        
				
				
				
                //user type : only sender, rcpt plus sender etc.

				if (user_type != typeRCPT) {
					if (user_type == typeX_SENDER) {
						//got to find out the X
						if(notAllusers){
							data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),sender from email where sender like '"+sqlusersConstraints+"' group by sender having count(mailref) > " + minNumber);
						}else{
						data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),sender from email group by sender having count(mailref) > " + minNumber);
						}
					
					} else if (user_type == typeMIN_SENDER) {
						if(notAllusers){
							data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),sender from email where sender like '"+sqlusersConstraints+"' group by sender having count(mailref) > 200 ");
							}else{
						data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),sender from email group by sender having count(mailref) > 200 ");
						}
						//	jdbcView.getSqlData("select
						// max(dates),min(dates),count(*),sender from email
						// group by sender having count(*) > 200 ");
					} else {
						
						if(notAllusers){
							data = jdbcView
							.getSQLData("select max(dates),min(dates),count(mailref),sender from email where sender like '"+sqlusersConstraints+"'  group by sender");
					
						}else{
						data = jdbcView
								.getSQLData("select max(dates),min(dates),count(mailref),sender from email group by sender");
						}
						//jdbcView.getSqlData("select
						// max(dates),min(dates),count(*),sender from email
						// group by sender");
					}

					System.out.println("data fetched: "+data.length);
					for (int i = 0; i < data.length; i++) {

                        int sendc = Integer.parseInt(data[i][2]);
						userShortProfile usp = new userShortProfile(data[i][3],data[i][1],data[i][0],sendc,0);
                        //g_allUsers2.add(data[i][3]);
						g_userHash.put(usp.getName(), usp);

					}
				}//end sender's
                
                
                //now to see if seperate rctpt collection to add
				if (user_type != typeSENDER && user_type != typeMIN_SENDER) {
					//now for rcpts.
					if(notAllusers){
						data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),rcpt from email where rcpt like '"+sqlusersConstraints+"' group by rcpt");
						}else{
					data = jdbcView.getSQLData("select max(dates),min(dates),count(mailref),rcpt from email group by rcpt");
					}
					System.out.println("data fetched: " + data.length);
					for (int i = 0; i < data.length; i++) {
						if (!g_userHash.containsKey(data[i][3])) {
							
                            int rcpt = Integer.parseInt(data[i][2]);
                            userShortProfile usp = new userShortProfile(data[i][3],data[i][1],data[i][0],0,rcpt);
                            //g_allUsers2.add(data[i][3]);
                            g_userHash.put(usp.getName(), usp);
                                               
						}else{
                            
                            userShortProfile usp = g_userHash.get(data[i][3]);
                            int rcpt = Integer.parseInt(data[i][2]);
                            usp.setRcptCount(rcpt);
                            g_userHash.put(usp.getName(), usp);
                        }
					}
				}
				
				try{
				
					if(!notAllusers){
					System.out.println("Going to save hashset");
						// now to write it out so we dont need to calcualte it
						// next time around
						ObjectOutputStream oin = new ObjectOutputStream(
								new GZIPOutputStream(new FileOutputStream(
										userinfo)));

						oin.writeInt(g_userHash.size());

						for (Iterator<String> hashiter = g_userHash.keySet()
								.iterator(); hashiter.hasNext();) {
							oin.writeObject(g_userHash.get(hashiter.next()));
						}
						oin.close();
					}
				}catch(IOException ioe){
					ioe.printStackTrace();
				}
				
		        }//end guser hash setting up manually
				
				//now to sort all users if both sender,rcpts
               System.out.println("going to sort list");
                spp = new TreeSet<String>(g_userHash.keySet());
           	//Collections.sort(spp, String.CASE_INSENSITIVE_ORDER);
				//System.out.println("done sort");
              //  System.out.print(g_allUsers2.size() + " users ");

			//	g_allUsers = (String[]) g_allUsers2.toArray(new String[g_allUsers2.size()]);
			//	g_allUsers = (String[]) spp.toArray(new String[spp.size()]);
                
                g_allAttachments = jdbcView.getSQLDataByColumn("select distinct type from message", 1);
				//data = null;
                
			//}
		} catch (SQLException sqle) {
			System.err.println("Problem updating global stats");
		}

		//need to update toolbar:
        
		ComboBoxModel model;
        if(spp == null){
         model = new DefaultComboBoxModel();    
        }else{
            model = new DefaultComboBoxModel(spp.toArray(new String[spp.size()]));

        }
        m_view_user = new JComboBox(model);
		m_view_user.setEditable(false);
		//m_view_user.setMaximumSize(new Dimension(180, 25));
		//m_view_user.setPreferredSize(new Dimension(180, 25));
		m_view_user.setRenderer(new EMTLimitedComboSpaceRenderer(40));
		m_view_user.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			  Execute();
			}
		});

		//weijen
		//m_view_user.setSelectedItem("sal@cs.columbia.edu");

		if (
		//TODO rethik this
		comboBoxPane.getComponentCount() >= 2)
			comboBoxPane.remove(1);
		
		
		comboBoxPane.add(m_view_user, 1);
		//comboBoxPane.add(,2);
		//set the new dates
		m_spinnerModelStart.setValue(cal.getTime());
		spinnerStart = new JSpinner(m_spinnerModelStart);
		JSpinner.DateEditor editorStart = new JSpinner.DateEditor(spinnerStart, "MMM dd, yyyy");
		spinnerStart.setEditor(editorStart);
		m_spinnerModelEnd.setValue(cal2.getTime());
		spinnerEnd = new JSpinner(m_spinnerModelEnd);
		JSpinner.DateEditor editorEnd = new JSpinner.DateEditor(spinnerEnd, "MMM dd, yyyy ");
		spinnerEnd.setEditor(editorEnd);
		spinnerStart.setMaximumSize(new Dimension(100, 25));
		spinnerEnd.setMaximumSize(new Dimension(100, 25));

		if (comboBoxPane.getComponentCount() >= 4) {
			//remove backwards cause things move down
			comboBoxPane.remove(4);
			comboBoxPane.remove(3);
		}
		comboBoxPane.add(spinnerStart, 3);
		comboBoxPane.add(spinnerEnd, 4);

		comboBoxPane.validate();
		System.out.println("...done");
        System.gc();//cleanup

	}

	static public final JComboBox buildJComboModels() {
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Naive Bayes + TXTclass");
		comboBox.addItem("Naive Bayes + NGram"); //hard coded later to
		comboBox.addItem("NGram - Content");
		comboBox.addItem("Text Classifier");
		comboBox.addItem("1-Coded Model");
		comboBox.addItem("Model Correlation");
		comboBox.addItem("Outlook Model");
		comboBox.addItem("Plan For Spam Model");
		comboBox.addItem("Tfidf - Text Freq / Doc Freq");
		comboBox.addItem("Link Analysis");
		comboBox.addItem("Limited NGram");
		comboBox.addItem(MLearner.getTypeString(MLearner.ML_SPAMASSASSIN));
		
		
		return comboBox;
	}
	//TODO:
	/**
	 * for each window call the updater for it if needed
	 */
	private final void Execute() {
		//need to fetch current component and call execute
		//Component c =
		// (Component)comboBoxPane.getComponentAtIndex(cb.getSelectedIndex());
		//c.execute((String)m_view_user.getSelectedItem());//will call execute
		// on the specific class.
		//String comboBoxItems[] = {"Load Data","Messages", "Email Features",
		// "Usage Histogram", "Enclave Cliques", "User Cliques", "Similar
		// Users", "Average Comm Time", "Recipient Frequency", "Attachment
		// Statistics", "Email Flow", "Graphic Cliques", "Virus
		// Scanning","Reports", "SQL view" };

		switch (mainChoiceBox.getSelectedIndex()) {
			case i_usageWindow :
				m_histogramcomp.Execute((String) m_view_user.getSelectedItem());
				break;
			case i_similiarWindow :
				m_similarusers.Execute();
				break;
			case i_communicationWindow :
				m_averageComm.Execute();
				break;
			case i_shdetectorWindow :
				m_shDetector.Execute();
				break;
			case i_doclflowWindow:
				m_DocFlow.Execute();
				break;
			case i_usercliquesWindow :
				m_social_cliques.Execute();
				break;
			case i_rcptWindow :
				m_addresslist.Execute();
				break;
			case i_attachmentWindow :
				m_attachments.Execute();
				break;

		}
	}

	/**
	 * The idea is to push off setup of some of the components until they are
	 * actually needed, this minimizing the memory overhead. tested to make sure
	 * they take less than second to setup under normal conditions.
	 * 
	 * @param component
	 */
	private void setupEMTComponent(int component) {
		
		switch (component) {
			case (i_sqlviewWindow) :
				if (m_sqlview == null) {
					m_sqlview = new dbSQL(jdbcView);
					setupEMTWindowList[i_sqlviewWindow] = true;
					m_EMTWindow_tabbedPane.add(st_SQLWindow, m_sqlview);
				}
				break;
			case (i_usageWindow) :
				if (m_histogramcomp == null) {
					try {
						m_histogramcomp = new UsageHistogram(jdbcView, m_alerts, m_md5Renderer, this);

						m_histogramcomp.Refresh();

						setupEMTWindowList[i_usageWindow] = true;
						m_EMTWindow_tabbedPane.add(st_UsageWindow, m_histogramcomp);
					} catch (SQLException sqe) {
						sqe.printStackTrace();
					}
				}
				break;
			case (i_usercliquesWindow) :
				if (m_social_cliques == null) {
					try {
						m_social_cliques = new SocialCliques(jdbcView, m_aliasFilename, this);
						setupEMTWindowList[i_usercliquesWindow] = true;
						m_EMTWindow_tabbedPane.add(st_UserCliqWindow, m_social_cliques);
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
				break;
			case (i_communicationWindow) :
				if (m_averageComm == null) {
					
						m_averageComm = new AverageCommTime(jdbcView, m_alerts, m_md5Renderer, this);
						setupEMTWindowList[i_communicationWindow] = true;
						m_EMTWindow_tabbedPane.add(st_VIPWindow, m_averageComm);
				}
				break;
			case (i_shdetectorWindow) :
				if (m_shDetector == null) {
					try {
						m_shDetector = new SHDetector(jdbcView, m_alerts, m_md5Renderer, this, getAttachments(), m_aliasFilename);
						setupEMTWindowList[i_shdetectorWindow] = true;
						m_EMTWindow_tabbedPane.add(st_SocHeirWindow, m_shDetector);
					} catch(Exception ee) {
						ee.printStackTrace();
					}
				}
				break;
				case (i_doclflowWindow) :
					if (m_DocFlow == null) {
						try {
							m_DocFlow = new DocumentFlowWindow(jdbcView, this);
							setupEMTWindowList[i_doclflowWindow] = true;
							m_EMTWindow_tabbedPane.add(st_docFlowWindow, m_DocFlow);
						} catch(Exception ee) {
							ee.printStackTrace();
						}
					}
					break;
			case (i_virusWindow) :
				if (m_virusSim == null) {

					m_virusSim = new VirusDetectModels(jdbcView, jdbcView, m_md5Renderer, this, m_alerts);
					setupEMTWindowList[i_virusWindow] = true;
					m_EMTWindow_tabbedPane.add(st_VirusWindow, m_virusSim);

				}
				break;
			case (i_emailflowWindow) :
				if (m_emailflow == null) {
					m_emailflow = new EmailFlow(jdbcView, m_md5Renderer, this);
					m_EMTWindow_tabbedPane.add(st_FlowWindow, m_emailflow);
					setupEMTWindowList[i_emailflowWindow] = true;
				}
				break;
			case (i_graphiccliquesWindow) :
				if (m_visualclique == null) {
					m_visualclique = new VisualClique(jdbcView, m_emtconfig,m_aliasFilename, this);
					m_EMTWindow_tabbedPane.add(st_GraphicCliqWindow, m_visualclique);
					setupEMTWindowList[i_graphiccliquesWindow] = true;
				}
				break;
			case (i_enclaveWindow) :
				if (m_cliques == null) {
					try {
						m_cliques = new EnclaveCliques(this, jdbcView, m_aliasFilename, getAttachments());
						setupEMTWindowList[i_enclaveWindow] = true;
						m_EMTWindow_tabbedPane.add(st_EnclaveWindow, m_cliques);
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
				break;
			case (i_similiarWindow) :
				if (m_similarusers == null) {
					try {
						m_similarusers = new SimilarUsers(jdbcView, m_md5Renderer, this); //by
						setupEMTWindowList[i_similiarWindow] = true;
						m_EMTWindow_tabbedPane.add(st_SimilarWindow, m_similarusers);
					} catch (SQLException ee) {
						ee.printStackTrace();
					}
				}
				break;
			case (i_rcptWindow) :
				if (m_addresslist == null) {
					try {
						m_addresslist = new RcptFrequency(jdbcView, m_aliasFilename, this);//on
						setupEMTWindowList[i_rcptWindow] = true;
						m_EMTWindow_tabbedPane.add(st_RCPTWindow, m_addresslist);
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
				break;
			case (i_analyzerWindow) :
				if (m_fanalyzer == null) {
					m_fanalyzer = new FileAnalyzer(jdbcView, jdbcView, m_md5Renderer, this);
					setupEMTWindowList[i_analyzerWindow] = true;
					m_EMTWindow_tabbedPane.add(st_AttachVirAnalyzerWindow, m_fanalyzer);
				}
				break;
			case (i_experimentalWindow) :
				if (m_experiment == null) {
					m_experiment = new ExperimentalEMTWindow(jdbcView, m_emtconfig);
					setupEMTWindowList[i_experimentalWindow] = true;
					m_EMTWindow_tabbedPane.add("Experiments", m_experiment);
				}
				break;
            case (i_searchWindow) :
                if (m_search == null) {
                    m_search = new SearchEMT(jdbcView, m_emtconfig, this);
                    setupEMTWindowList[i_searchWindow] = true;
                    m_EMTWindow_tabbedPane.add(st_SearchWindow, m_search);
                }
                break;
            case (i_financeWindow) :
				if (m_finance == null) {
					m_finance = new Finance(jdbcView,this);
					setupEMTWindowList[i_financeWindow] = true;
					m_EMTWindow_tabbedPane.add(st_SocNetWindow, m_finance);
				}
				break;
            
            default :
				System.out.println("dont know how to setup :" + component);
		}




	}
	
	
	public UserDistance[] getSimilarityScores(String target,String list[])throws SQLException{
		//make sure its up
		setupEMTComponent(i_similiarWindow);
		
		return m_similarusers.findSimilarUsers(target,list);
	}
	
	public final double[] getUsageProfile(String user){
		setupEMTComponent(i_usageWindow);
		
		return m_histogramcomp.getUsageProfileChart(user);
	}

	public final double[] getUsageProfile(String user1,String user2){
		setupEMTComponent(i_usageWindow);
		
		return m_histogramcomp.getUsageProfileChart(user1,user2);
	}
		
	/**
	 * this will delete the cache info files
	 *
	 */
	public void deleteInfoFiles(){
    	System.out.println("Attempting to delete the info file, probably due to data changes");
    	
    	String name = "_" + m_emtconfig.getProperty(EMTConfiguration.DBTYPE)+"_" + m_emtconfig.getProperty(EMTConfiguration.DBNAME)+"_userset.info";
    	Utils.SystemIndependantRecursiveDeleteMethod(System.getProperty("user.dir"),null,name.toLowerCase(),false,false);
    }
			
	public userComm[] getCommunicationList(String user,int level)throws SQLException{
		setupEMTComponent(i_communicationWindow);
		userShortProfile info = getUserInfo(user);
		if(info==null)
		    return null;
		return m_averageComm.getCommTimeWork(user,info.getStartDate(),info.getEndDate(),level);
	}
	
}

