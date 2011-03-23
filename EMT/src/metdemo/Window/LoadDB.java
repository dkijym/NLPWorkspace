/**
 *Shlomo Hershkop
 *Columbia University
 *worked on summer 2002 from 
 *Fall 2001 - spring 2002
 *shlomo@cs.columbia.edu
 * 
 * 
 */


package metdemo.Window;
import javax.swing.border.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.*; //for reg exp

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.jdesktop.layout.GroupLayout;

import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Parser.EMTParser;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Process;
import java.lang.Runtime;
import java.sql.SQLException;
// TODO: Auto-generated Javadoc

/**
 * main class for the Loading the DB engine
 * 
 * will allow main gui and load of data sets.
 */


public class LoadDB extends JScrollPane implements ActionListener {


	/** The m_jdbc view. */
	private EMTDatabaseConnection m_jdbcView;
	// private Alerts m_Alerts;
	/** The m_win gui. */
	private winGui m_winGui;
	
	/** The m_status. */
	private JLabel m_status;
	
	/** The m_emtconfig. */
	private EMTConfiguration m_emtconfig;
	
	/** The m_v action listeners. */
	private Vector<ActionListener> m_vActionListeners;
	
	/** The frame. */
	static JFrame frame;
	
	/** The help_sort_checkbox. */
	private JCheckBox m_logger_chkbox, help_sort_checkbox;
	
	/** The m_realign dates_chkbx. */
	private JCheckBox m_chompname_chkbx, m_emptyDB_chkbx, m_useJavaParser_chkbx, m_from_seperator_chkbox , m_realignDates_chkbx;

	/** The frompattern_textfld. */
	private JTextField realDomain, perl = new JTextField(10), frompattern_textfld = new JTextField(10);

	/** The domain list. */
	private JTextArea domainList;

	/** The legalfile. */
	boolean helpwin = false, loadwin = false, legalfile = false;
	
	/** The Load. */
	Frame Help, Load;
	
	/** The location. */
	JTextField location;
	
	/** The pointer_up. */
	JTextField pointer_up = new JTextField(50);
	
	/** The msg. */
	TextArea msg = new TextArea();
	
	/** The Filename. */
	String Filename = new String("");
	
	/** The chooser. */
	JFileChooser chooser;
	
	/** The database. */
	String database = new String();
	
	/** The host. */
	String host = new String();
	
	/** The user. */
	String user = new String();
	
	/** The pass. */
	String pass = new String();
	
	/** The wordlist. */
	String wordlist = new String();
	
	/** The get stats table button. */
	JButton getDomainName, goConvertDomain_button, getStatsTableButton;
	//  JTextArea Stats =new JTextArea(18,15);
	/** The m_stats. */
	JLabel m_stats;
	
	/** The m_domains. */
	JComboBox m_domains;// will hold listo f unuiqe domain names in db
	
	/** The gd. */
	File gd = null;
	
	/** The clipboard. */
	final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	/** The loca. */
	private JLabel loca;
	
	/** The button_outlookexpress. */
	JRadioButton button_mbox, button_outlook, button_pst, button_outlookexpress;//, button_im;

	/**
	 * Help load the database from disk to emt database.
	 *
	 * @param h host name
	 * @param db db name
	 * @param u user name
	 * @param p pass?
	 * @param jdbcUpdate handle to the db
	 * @param gui hook back t ogui window
	 * @param alerts hook to generatin alert
	 * @param wl word list file (not implimented
	 * @param StatusBar the status bar
	 * @param emtc the emtc
	 * @throws NullPointerException the null pointer exception
	 */
	public LoadDB(String h, String db, String u, String p, EMTDatabaseConnection jdbcUpdate,
			 winGui gui, ReportForensicWindow alerts, String wl, JLabel StatusBar, EMTConfiguration emtc) throws NullPointerException {


		JTabbedPane realmain = new JTabbedPane();

		if ((jdbcUpdate == null)  || (alerts == null)) {
			throw new NullPointerException();
		}
		m_jdbcView = jdbcUpdate;
		//m_jdbcUpdate = jdbcUpdate;
		//m_Alerts = alerts;
		m_winGui = gui;
		m_status = StatusBar;
		m_emtconfig = emtc;
		m_vActionListeners = new Vector<ActionListener>();

		database = db;
		host = h;
		user = u;
		pass = p;
		wordlist = wl;


		gd = new File(System.getProperty("user.dir"));
		chooser = new JFileChooser();
		//ExampleFileFilter filter = new ExampleFileFilter();
		//filter.addExtension("txt");
		//filter.setDescription("plain text format");

		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//chooser.setCurrentDirectory(new File("/"));

		chooser.setFileHidingEnabled(false);
		chooser.setMultiSelectionEnabled(false);
		//chooser.setFileFilter(filter);

		// Create the buttons.
		JButton browseButton = new JButton("Browse");
		browseButton.setToolTipText("Select file");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (gd != null)
					chooser.setCurrentDirectory(gd);

				int returnVal = chooser.showOpenDialog(LoadDB.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					//File file = fc.getSelectedFile();
					legalfile = true;
					pointer_up.setText(chooser.getSelectedFile().getPath());
					gd = chooser.getCurrentDirectory();

					//This is where a real application would open the file.
					//log.append("Opening: " + file.getName() + "." + newline);
				} else {
					//log.append("Open command cancelled by user." + newline);
				}

			}
		});
		//test button
		JButton testButton = new JButton("Test");
		testButton.setToolTipText("Test file with how many emails we can get");
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//will test file how many time the current from pattern matches
				try {
					String s = pointer_up.getText();
					File f = new File(s);
					if (!f.exists())
						legalfile = false;
					else
						legalfile = true;

					if (!legalfile) {
						JOptionPane.showMessageDialog(LoadDB.this,
								"Please Choose valid file from load window to proceed");
						return;
					}

					BufferedReader bf = new BufferedReader(new FileReader(s));
					int count = 0, lines = 0;
					System.out.println("from patter: " + frompattern_textfld.getText().trim());
					Pattern p_topfrom = Pattern.compile(frompattern_textfld.getText().trim(), Pattern.CASE_INSENSITIVE);
					Matcher m;
					s = bf.readLine();
					while (s != null) {
						lines++;
						m = p_topfrom.matcher(s);
						if (m.matches()) {
							count++;
							System.out.println(s);
						}
						s = bf.readLine();

					}

					bf.close();
					System.out.println("number of lines in file " + lines);
					System.out.println("count is now " + count);


				} catch (Exception f) {
					System.out.println(f);
				}



			}
		});


		JButton importButton = new JButton("Import");
		importButton.setBackground(Color.pink);
		importButton.setToolTipText("Import from file into database");
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//check if we have legal filei intput
				String files = null;
				try {
					files = pointer_up.getText().trim();
					File f = new File(files);
					if (!f.exists())
						legalfile = false;
					else
						legalfile = true;
				} catch (Exception f) {
				}

				int continueanyway = JOptionPane.YES_OPTION;
				if (files.toLowerCase().endsWith(".dbx")) {
					if (!button_outlook.isSelected() && !button_outlookexpress.isSelected())

					{
						continueanyway = JOptionPane
								.showConfirmDialog(
										LoadDB.this,
										"Detected .dbx file type, but file specified as other\nAre you sure you dont want to specify outlook express format?\nClick Yes to continue anyway, No to rechoose format",
										"check on email type", JOptionPane.YES_NO_OPTION);
					}
				}
				if (files.toLowerCase().endsWith(".pst") && !button_pst.isSelected()) {
					continueanyway = JOptionPane
							.showConfirmDialog(
									LoadDB.this,
									"Detected .pst file type, but file specified as other\nAre you sure you dont want to specify outlook express format?\nClick Yes to continue anyway, No to rechoose format",
									"check on email type", JOptionPane.YES_NO_OPTION);
				}
				if (!legalfile) {
					JOptionPane.showMessageDialog(LoadDB.this, "Please Choose valid file from load window to proceed");
				} else if (continueanyway != JOptionPane.YES_OPTION) {
					System.out.println("chance to redo choice");
				} else {
					(new Thread(new Runnable() {
						public void run() {

							m_winGui.setEnabled(false);
							try {
								
								String loglocation = getLogName();
								goImportFile(!(m_useJavaParser_chkbx.isSelected()),pointer_up.getText(),m_emptyDB_chkbx.isSelected(),loglocation);//should be
														   // isSelectPerl
							} catch (Exception q) {
								System.out.println("@@@@problem: " + q);
							}
							getStatistics();
							m_winGui.setEnabled(true);
							if (m_useJavaParser_chkbx.isSelected()) {
								perl.setEnabled(false);
								loca.setEnabled(false);
							}



						}
					})).start();
				}
			}
		});

		getStatsTableButton = new JButton("Refresh DB Statistics on table below");
		getStatsTableButton.setToolTipText("Update the stats from the database");
		getStatsTableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				(new Thread(new Runnable() {
					public void run() {
						m_winGui.setEnabled(false);
						getStatistics();
						m_winGui.setEnabled(true);
					}
				})).start();
			}
		});

		m_logger_chkbox = new JCheckBox("Create Log File");
		m_logger_chkbox.setToolTipText("This will create a log file called plogfile showing subject and email count");

		m_emptyDB_chkbx = new JCheckBox("Empty DB before importing data");
		m_emptyDB_chkbx.setToolTipText("This will Empty The Current DB, please restart after importing new data");

		m_chompname_chkbx = new JCheckBox("Shorten path if name is only numbers",true);
		m_chompname_chkbx.setToolTipText("This will help the enron data set, where for example all single emails in number files");
		
		m_realignDates_chkbx = new JCheckBox("Align rcvd dates",true);
		m_realignDates_chkbx.setToolTipText("This will attempt to fix date alignment by trusting the rcvd time-stamp over the time-stamp in the email header (note: good for spam mail)");
		
		m_useJavaParser_chkbx = new JCheckBox("Use Java Parser", true);
		m_useJavaParser_chkbx.setToolTipText("Choose the java parser");
		m_useJavaParser_chkbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (m_useJavaParser_chkbx.isSelected()) {
					perl.setEnabled(false);
					loca.setEnabled(false);
				} else {
					perl.setEnabled(true);
					loca.setEnabled(true);
				}
			}
		});



		//final setup before gui
		JPanel getDataPanel = new JPanel();
		
		JPanel cleanDataPanel = new JPanel();
		JPanel dataInfoPanel = new JPanel();

		/*Box box = Box.createHorizontalBox();
		box.add(browseButton);
		box.add(Box.createHorizontalStrut(10));
		box.add(testButton);
		box.add(importButton);
		//box.add(getStats);
		box.add(m_logger);
		box.add(m_emptyDB_chkbx);
		box.add(m_chompname_chkbx);
		box.add(m_realignDates_chkbx);
		box.add(m_useJavaParser_chkbx);
		
		getDataPanel.add(box);*/

		button_mbox = new JRadioButton("Mbox/EML", true);
		button_outlook = new JRadioButton("Microsoft Outlook Expres(dbx)", false);
		button_outlookexpress = new JRadioButton("Outlook Express(dbx)", false);
		button_pst = new JRadioButton("Outlook pst File", false);
		//button_im = new JRadioButton("Instant Message", false);
		//button_im.setEnabled(false);
		ButtonGroup dataType = new ButtonGroup();
		dataType.add(button_mbox);
		dataType.add(button_outlook);
		//	dataType.add(button_outlookexpress);
		dataType.add(button_pst);
		//dataType.add(button_im);
		/*JPanel dataTypePanel = new JPanel();
		dataTypePanel.add(new JLabel("Format of Data:"));
		dataTypePanel.add(button_mbox);
		dataTypePanel.add(button_outlook);
		dataTypePanel.add(button_pst);*/
		
		
		JLabel processLabel = new JLabel("File/Directory to parse/process:");
		
		JTextField file_textbox = new JTextField(50);
		pointer_up = file_textbox;
		
		loca = new JLabel("Location of EMT parser:");

		loca.setHorizontalAlignment(SwingConstants.RIGHT);
		perl.setText("lib" + File.separator + "emt-parse.pl");
		
		loca.setEnabled(false);
		perl.setEnabled(false);//defaultjava parser

		frompattern_textfld.setText(EMTParser.GENERALFROM);
		//"^From (.*):(.*)$");
		frompattern_textfld.setEnabled(false);
		m_from_seperator_chkbox = new JCheckBox("std from", true);
		m_from_seperator_chkbox.setToolTipText("Use std from seperator between emails -- has to be java regular expression");
		m_from_seperator_chkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (m_from_seperator_chkbox.isSelected()) {
					frompattern_textfld.setEnabled(false);

				} else {
					frompattern_textfld.setEnabled(true);

				}
			}
		});
		
		//mm pattern ^[ ]?[0-9]+-[a-zA-Z]+-[0-9]+ (.*):(.*);(.*)$
		//regular ^From ([^,])*:(.*)

		JButton mmButton = new JButton("MM Pattern");
		mmButton.setToolTipText("set reg ex pattern to mm style");
		mmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_from_seperator_chkbox.setSelected(false);
				frompattern_textfld.setEnabled(true);

				frompattern_textfld.setText("^[ ]?[0-9]+-[a-zA-Z]+-[0-9]+ (.*):(.*);.*$");
			}
		});
		
		EMTHelp e1 = new EMTHelp(EMTHelp.EMTPARSER);
		JButton defaultmbox_button = new JButton("Default MBOX pattern");
		defaultmbox_button.setToolTipText("set reg ex pattern to mm style");
		defaultmbox_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frompattern_textfld.setText(EMTParser.GENERALFROM);
				m_from_seperator_chkbox.setSelected(true);
				frompattern_textfld.setEnabled(false);

				//"^From ([^,])*:(.*)$");

			}
		});
		
		//view import history button
		JButton historyButton = new JButton("View Import History");
		historyButton.setToolTipText("view imports into db");
		historyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				String input;
				 StringBuffer buf = new StringBuffer();
	                try {
	                    BufferedReader bufread = new BufferedReader(new FileReader(getLogName()));
	                    while ((input = bufread.readLine()) != null) {
	                        buf.append(input + "\n");
	                    }
	                } catch (IOException fnf) {
	                    System.out.println("fnf  " + fnf);
	                }

	                JTextArea textArea = new JTextArea(buf.toString());
	                textArea.setLineWrap(true);
	                textArea.setWrapStyleWord(true);
	                JScrollPane m_areaScrollPane = new JScrollPane(textArea);
	                m_areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	                m_areaScrollPane.setPreferredSize(new Dimension(550, 380));
	                // ok we've setup the body text
	                JOptionPane.showMessageDialog(LoadDB.this, m_areaScrollPane, "import logfile view: " + getLogName(),
	                        JOptionPane.INFORMATION_MESSAGE);


			}
		});
		
		/*JLabel cleanup = new JLabel("Domain CleanUp Tools:");
		cleanup.setForeground(Color.blue);
		cleanup.setBackground(Color.white);
		*/
		
		/* button to label folder contents. */
		JButton labelFolder_button = new JButton("Label Folder Tool");
		labelFolder_button.setForeground(Color.red);
		labelFolder_button.setToolTipText("Quickly label folder with specific class");
		labelFolder_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//idea: grab all folders, show user, choose one, show labels,
				// choose then execute.
				String data[][] = null;
				//get names of current folders
				try {
					synchronized (m_jdbcView) {
						data = m_jdbcView.getSQLData("select distinct folder from email");
						//data = m_jdbcView.getRowData();
					}

					String data2[] = new String[data.length];
					for (int i = 0; i < data.length; i++)
						data2[i] = data[i][0];

					//	Object[] possibilities = data2;
					String choosefolder = (String) JOptionPane.showInputDialog(LoadDB.this,
							"Please choose a Target Folder to Label:\n", "Label Folder Messages",
							JOptionPane.PLAIN_MESSAGE, null, data2, data2[0]);

					if (choosefolder == null)
						return;//do nothing
					//now to get which is the label
					String classes[] = {"s", "v", "i", "n", "u"};
					String chooselabel = (String) JOptionPane.showInputDialog(LoadDB.this,
							"Please choose a Target Label for :" + choosefolder + "\n", "Label Folder Messages",
							JOptionPane.PLAIN_MESSAGE, null, classes, classes[0]);

					if (classes == null)
						return;//do nothing


//					m_jdbcUpdate
					m_jdbcView.updateSQLData("update email set class = '" + chooselabel + "' where  folder='"
							+ choosefolder + "'");



				} catch (SQLException s) {

					JOptionPane.showMessageDialog(LoadDB.this, "Error getting folder details from database: " + s,
							"Problem folders", JOptionPane.ERROR_MESSAGE);

					return;
				}



			}
		});
		
		/* button to setup domain cleanup */
		JButton setupDomain = new JButton("Refresh Domain Names");
		setupDomain.setForeground(Color.blue);
		setupDomain.setToolTipText("Refresh the combo list of domains");
		setupDomain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new Thread(new Runnable() {
					public void run() {
						m_winGui.setEnabled(false);
						setupDomains();
						m_winGui.setEnabled(true);
					}
				})).start();
			}
		});
		
		help_sort_checkbox = new JCheckBox("Help Sort");
		help_sort_checkbox.setToolTipText("Sort the domains backwards easier to find similiar ones");
		help_sort_checkbox.setSelected(true);
		m_domains = new JComboBox();

		getDomainName = new JButton("Copy to List");
		getDomainName.setForeground(Color.blue);
		getDomainName.setToolTipText("Copy current domain into list");
		getDomainName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (m_domains.getItemCount() < 1)
					return;//sop at least one thing
				// m_winGui.setEnabled(false);
				String temp = new String(domainList.getText());
				temp += (String) m_domains.getSelectedItem() + "\n";
				domainList.setText(temp);
				//m_winGui.setEnabled(true);
			}
		});
		JLabel targetdomain_label = new JLabel("Target Domain:");
		targetdomain_label.setForeground(Color.blue);
		JLabel convertLabel = new JLabel(
				"<html><center>List of domains<br> to replace:<br>Note: use % for wildcards</center></html>");

		convertLabel.setToolTipText("<html>The idea here is to replace for example:<br>user1@flame.cs.columbia.edu<br>user1@cyber.cs.columbia.edu<br>user1@bear.cs.columbia.edu<br>with: user1@cs.columbia.edu</html>");
		convertLabel.setForeground(Color.blue);
		
		goConvertDomain_button = new JButton("Click to Convert to:");
		goConvertDomain_button.setForeground(Color.blue);
		goConvertDomain_button.setToolTipText("Replace domains on list with real one");
		goConvertDomain_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new Thread(new Runnable() {
					public void run() {
						
						if(domainList.getText().trim().equals("%")){
							domainList.setText("%.%");
						}
						
						
						if (domainList.getText().trim().length() < 1) {
							JOptionPane.showMessageDialog(LoadDB.this, "Input domain no good");
							return;

						}
						if (realDomain.getText().trim().length() < 1) {
							JOptionPane.showMessageDialog(LoadDB.this, "Target domain not good.");
							return;

						}

						m_winGui.setEnabled(false);
						convertDomains(domainList.getText().trim(), realDomain.getText().trim());
						domainList.setText("");
						m_winGui.deleteInfoFiles();
						
						JOptionPane.showMessageDialog(LoadDB.this,"Deleted info files....Restart EMT for changes to propogate or click on EMAIL button on top near drop down list");
						
						m_winGui.setEnabled(true);
					}
				})).start();
			}
		});
		
		domainList = new JTextArea(5, 20);//(20);
		//domainList.setText(" ");
		JScrollPane domainListArea_spane = new JScrollPane(domainList);
		domainListArea_spane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		realDomain = new JTextField(20);
		realDomain.setToolTipText("Replace domains with this one");
		JButton copytoClipButton = new JButton("Copy all from below to report on the clipboard");
		copytoClipButton
				.setToolTipText("<html>This will copy the contents of the datbase report below<P>to the system clipboard..<P>You can then paste it into any application you may wish to use</html>");
		copytoClipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//String selection = jt.getSelectedText();
				//		    String select = Stats.getText();
				StringSelection data = new StringSelection(m_stats.getText());
				//	Stats.getText());
				clipboard.setContents(data, data);
			}
		});
		
		EMTHelp help2button = new EMTHelp(EMTHelp.CLEANDATA);
		m_stats = new JLabel();
		//getStatistics();

		JScrollPane dbstats_scrollpane = new JScrollPane(m_stats);//Stats);
		dbstats_scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		dbstats_scrollpane.setPreferredSize(new Dimension(500, 300));
		//stats2.setBorder(new BevelBorder(BevelBorder.LOWERED));
		//Border bd =
		//   BorderFactory.createEtchedBorder(Color.white,
		//				     new Color(134, 134, 134));
		//TitledBorder ttl = new TitledBorder(bd,"Database Report");
		//stats2.setBorder(ttl);
		//stats2.setTitle("Database Stats and Report");
		//constraints.gridx=0;
		//constraints.gridy = 7;
		JLabel emtlogoLabel = new JLabel();
		
		emtlogoLabel.setFont(new java.awt.Font("Tahoma", 1, 24));
		emtlogoLabel.setForeground(new java.awt.Color(51, 51, 255));
		emtlogoLabel.setText("EMT Data Loading");
		
		JLabel optionLabel = new JLabel();
		optionLabel.setText("Options:");
		
		JLabel formatlabel= new JLabel("Format:");
		
		
		//adding gui elemens

		GroupLayout layout = new GroupLayout(getDataPanel);//getDataPanelgetContentPane());
        getDataPanel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .addContainerGap()
                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                                        .add(optionLabel)
                                        .add(formatlabel))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                            .add(button_mbox)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(button_outlook))
                                        .add(m_chompname_chkbx)
                                        .add(m_logger_chkbox)
                                        .add(m_from_seperator_chkbox))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                                        .add(m_emptyDB_chkbx)
                                        .add(layout.createSequentialGroup()
                                            .add(m_realignDates_chkbx)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(m_useJavaParser_chkbx))
                                        .add(frompattern_textfld, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)
                                        .add(layout.createSequentialGroup()
                                            .add(button_pst)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 85, Short.MAX_VALUE)
                                            .add(historyButton))
                                        .add(layout.createSequentialGroup()
                                            .add(defaultmbox_button)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(mmButton)
                                            .add(46, 46, 46))))
                                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                    .add(processLabel)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(file_textbox, GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(browseButton))
                                .add(layout.createSequentialGroup()
                                    .add(e1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 222, Short.MAX_VALUE)
                                    .add(testButton)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(importButton)))
                            .add(98, 98, 98))
                        .add(layout.createSequentialGroup()
                            .add(325, 325, 325)
                            .add(emtlogoLabel)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(emtlogoLabel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                    .add(55, 55, 55)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(processLabel)
                        .add(file_textbox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(browseButton))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(formatlabel)
                        .add(button_mbox)
                        .add(button_outlook)
                        .add(button_pst)
                        .add(historyButton))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(optionLabel)
                        .add(m_logger_chkbox)
                        .add(m_emptyDB_chkbx))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(m_chompname_chkbx)
                        .add(m_realignDates_chkbx)
                        .add(m_useJavaParser_chkbx))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(m_from_seperator_chkbox)
                        .add(frompattern_textfld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(mmButton)
                        .add(defaultmbox_button))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.TRAILING)
                        .add(e1, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(GroupLayout.BASELINE)
                            .add(importButton)
                            .add(testButton)))
                    .add(48, 48, 48))
            );
        
        
        
        
        
        
        
        
	                                           
            		
            		
                               //                .add(48, 48, 48))//.addContainerGap(176, Short.MAX_VALUE))
        

        //pack();		
        //getDataPanel.setPreferredSize(new Dimension(600,400));
            
            
            
            //now for the clean tool
           JLabel one_label = new JLabel();
           JLabel two_label = new JLabel();
           JLabel three_label = new JLabel();
            one_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            one_label.setText("1");

            two_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            two_label.setText("2");

            three_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            three_label.setText("3");
            JLabel optional_label = new JLabel();
            optional_label.setText("Optional");
            JLabel domain_cleanup_label = new JLabel();
            domain_cleanup_label.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
            domain_cleanup_label.setForeground(new java.awt.Color(255, 0, 0));
            domain_cleanup_label.setText("Domain Cleanup Tools");
            
            //layout for cleaning tool
           layout = new GroupLayout(cleanDataPanel);//getDataPanelgetContentPane());
           cleanDataPanel.setLayout(layout);
            
         //cleanup tool
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(GroupLayout.LEADING)
                                    .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                                        .add(layout.createSequentialGroup()
                                            .add(18, 18, 18)
                                            .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                                                .add(m_domains, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(setupDomain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                                .add(help_sort_checkbox)
                                                .add(getDomainName)))
                                        .add(layout.createSequentialGroup()
                                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                                .add(layout.createSequentialGroup()
                                                    .add(10, 10, 10)
                                                    .add(convertLabel))//, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                                .add(three_label))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(domainListArea_spane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .add(layout.createSequentialGroup()
                                            .add(10, 10, 10)
                                            .add(targetdomain_label)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                                .add(goConvertDomain_button)
                                                .add(realDomain))))
                                    .add(one_label))
                                .add(layout.createParallelGroup(GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 36, Short.MAX_VALUE)
                                        .add(help2button, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(28, 28, 28)
                                        .add(optional_label))))
                            .add(labelFolder_button)
                            .add(two_label)
                            .add(domain_cleanup_label))
                        .addContainerGap())
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(domain_cleanup_label)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(one_label)
                            .add(help2button))
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                                    .add(setupDomain)
                                    .add(help_sort_checkbox))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                                    .add(m_domains, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .add(getDomainName))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(two_label)
                                .add(7, 7, 7)
                                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                                        .add(convertLabel)//, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(domainListArea_spane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .add(three_label))
                                .add(8, 8, 8)
                                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                                    .add(targetdomain_label)
                                    .add(realDomain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(goConvertDomain_button)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(optional_label)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(labelFolder_button)
                        .addContainerGap())
                );
           
                                       
            
            
            
         //db stats panel
         JLabel dbstatsLabel = new JLabel();
         dbstatsLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         dbstatsLabel.setText("DB Statistics");
         EMTHelp help3button = new EMTHelp(EMTHelp.EMTPARSER);   
         layout = new GroupLayout(dataInfoPanel);//getDataPanelgetContentPane());
         dataInfoPanel.setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(GroupLayout.LEADING)
                 .add(layout.createSequentialGroup()
                     .addContainerGap()
                     .add(layout.createParallelGroup(GroupLayout.LEADING)
                         .add(dbstats_scrollpane, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                         .add(layout.createSequentialGroup()
                             .add(dbstatsLabel)
                             .add(82, 82, 82)
                             .add(help3button))
                         .add(layout.createSequentialGroup()
                             .add(getStatsTableButton)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(copytoClipButton)))
                     .addContainerGap())
             );
             layout.setVerticalGroup(
                 layout.createParallelGroup(GroupLayout.LEADING)
                 .add(layout.createSequentialGroup()
                     .addContainerGap()
                     .add(layout.createParallelGroup(GroupLayout.BASELINE)
                         .add(dbstatsLabel)
                         .add(help3button))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(layout.createParallelGroup(GroupLayout.BASELINE)
                         .add(getStatsTableButton)
                         .add(copytoClipButton))
                     .add(18, 18, 18)
                     .add(dbstats_scrollpane, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                     .addContainerGap())
             ); 
         
         
         
         
		realmain.addTab("Get Data", getDataPanel);
		realmain.addTab("Clean Data", cleanDataPanel);
		realmain.addTab("Information on Data", dataInfoPanel);
		
		

		//	setViewportView(panel);
		setViewportView(realmain);

		//end set up gui
	}




	/**
	 * An ActionListener that listens to the radio buttons. //class
	 * RadioListener implements ActionListener {
	 * 
	 * 
	 * public String getLocation(){ return Filename; }
	 * 
	 * public void setupdate(JTextField s) { pointer_up = s; }
	 *
	 * @param list the list
	 * @param target the target
	 */



	private final void convertDomains(final String list, final String target) {

		//will have a list of domains to convert
		//first need to check target domain
		if ((target.trim()).length() < 1) {
			System.out.println("target length too short");
			return;
		}
		
		if(target.contains("%")){
			System.out.println("bad character wildcard in target domain...");
			return;
		}
		
		
		try {
			//ok so now we have a target
			//for each domain replacement, we need to
			String data[][]; //sender domains
			String fromDomain = new String();
			String toZN;
			//mysql> update email set
			// rzn='edu',rdom='cs.columbia.edu',rcpt=CONCAT(rname,'@',rdom)
			//where rdom like '.cs.columbia.edu' and rname like 'evs';

			//mysql> update email set rzn='edu',rdom='cs.columbia.edu',
			//rcpt=CONCAT(rname,'@',rdom) where rdom like '.cs.columbia.edu';

			//lets setup the targte zn
			//assume first its in the db
			synchronized (m_jdbcView) {
				data = m_jdbcView.getSQLData("select distinct sdom,szn from email where sdom like '" + target + "'");
				//data = m_jdbcView.getRowData();
			
			if (data.length < 1) {
				//synchronized (m_jdbcView) {
					data = m_jdbcView
							.getSQLData("select distinct rdom,rzn from email where rdom like '" + target + "'");
					//data = m_jdbcView.getRowData();
				//}
				if (data.length < 1) //need to figure out the target end domain
					data = new String[1][2];
				int end = target.lastIndexOf('.');
				if (end < 0)
					data[0][1] = new String(target); //just copy it out
				else
					data[0][1] = new String(target.substring(end + 1));

			}
			//}//end synch
			//at this point data[0][1] has our target zn

			toZN = new String(data[0][1].trim());

			//no to replace all in our list

			StringTokenizer stok = new StringTokenizer(list);
			boolean derby = m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY);
			//synchronized (m_jdbcView) {

				while (stok.hasMoreTokens()) {
					fromDomain = stok.nextToken();
					//mysql> update email set
					// rzn='edu',rdom='cs.columbia.edu',rcpt=CONCAT(rname,'@',rdom)
					//where rdom like '.cs.columbia.edu' and rname like 'evs';
					//mysql> update email set rzn='edu',rdom='cs.columbia.edu',
					//rcpt=CONCAT(rname,'@',rdom) where rdom like
					// '.cs.columbia.edu';
					//ignore just ignore duplicates in the table when they
					// happen
					//System.out.println("fromdom: " + fromDomain + " tozn: " +
					// toZN);
					try {
						if (derby) {

							try {
								m_jdbcView.updateSQLData("update email set rzn='" + toZN + "' , rdom='" + target
										+ "',rcpt= rname || '@' || '" + target + "' where rdom like '" + fromDomain
										+ "'");

							} catch (SQLException s1) {
								//this means that we've created a conflict with
								System.out.println("switching to one at a time: " + target);
								// on of our changes...so lets it one at a time.
								String mailrefs[] = m_jdbcView.getSQLDataByColumn(
										"select distinct mailref from email where rdom like '" + fromDomain + "'", 1);



								m_jdbcView.setAutocommit(false);
								for (int i = 0; i < mailrefs.length; i++) {
									try {
										m_jdbcView.updateSQLData("update email set rzn='" + toZN + "' , rdom='"
												+ target + "',rcpt= rname || '@' || '" + target + "' where mailref = '"
												+ mailrefs[i] + "'");

									} catch (SQLException s2) {
									}
								}
								m_jdbcView.setAutocommit(true);
							}
							try {
								m_jdbcView.updateSQLData("update email set szn='" + toZN + "' , sdom='" + target
										+ "',sender= sname || '@' || '" + target + "' where sdom like '" + fromDomain
										+ "'");
							} catch (SQLException s1) {
								//this means that we've created a conflict with
								// on of our changes...so lets it one at a time.
								String mailrefs[] = m_jdbcView.getSQLDataByColumn(
										"select distinct mailref from email where sdom like '" + fromDomain + "'", 1);
								m_jdbcView.setAutocommit(false);
								for (int i = 0; i < mailrefs.length; i++) {
									try {
										m_jdbcView.updateSQLData("update email set szn='" + toZN + "' , sdom='"
												+ target + "',sender= sname || '@' || '" + target
												+ "'  where mailref = '" + mailrefs[i] + "'");

									} catch (SQLException s2) {
									}
								}
								m_jdbcView.setAutocommit(true);
							}
							//some cant update so need to delete
							if(!fromDomain.startsWith("%") && !fromDomain.startsWith(target))
							 {
								m_jdbcView.updateSQLData("delete from email where rdom like '" + fromDomain + "'");
							 	m_jdbcView.updateSQLData("delete from email where sdom like '" + fromDomain + "'");
							 }else if(fromDomain.equals("%.%")){
									//manual cleanup of non targets....
									
									m_jdbcView.updateSQLData("delete from email where rdom not like '" + target + "'");
									m_jdbcView.updateSQLData("delete from email where sdom not like '" + target + "'");
							
									
									
									
								}
						} else {
						//not in derby......
							
							m_jdbcView.updateSQLData("update IGNORE email set rzn='" + toZN + "' , rdom='" + target
									+ "',rcpt=CONCAT(rname,'@','" + target + "') where rdom like '" + fromDomain + "'");

							m_jdbcView.updateSQLData("update IGNORE email set szn='" + toZN + "' , sdom='" + target
									+ "',sender=CONCAT(sname,'@','" + target + "') where sdom like '" + fromDomain
									+ "'");

							//							some cant update so need to delete

							if(!fromDomain.startsWith("%") && !fromDomain.startsWith(target)){
									m_jdbcView.updateSQLData("delete from email where rdom like '" + fromDomain + "'");
									m_jdbcView.updateSQLData("delete from email where sdom like '" + fromDomain + "'");
							}else if(fromDomain.equals("%.%")){
								//manual cleanup of non targets....
								
								m_jdbcView.updateSQLData("delete from email where rdom not like '" + target + "'");
								m_jdbcView.updateSQLData("delete from email where sdom not like '" + target + "'");
						
								
								
								
							}
						}
					} catch (SQLException see) {
						System.out.println(see);
					}
				}

			}//end synch
			//update user list and refresh tables
			m_winGui.updateRecords(true,"%@%");
			//need to reset the dbcount thing
			m_emtconfig.deleteProperty(EMTConfiguration.DBDATEINFO);
			m_emtconfig.saveFileToDisk(m_emtconfig.getProperty(EMTConfiguration.CONFIGNAME));


		} catch (Exception sqle) {
			System.out.println("problems in setup domains : " + sqle);
		}
	}


	/**
	 * generate the domain list, if reverse is checked we reverse sort, and then
	 * we can help find likly matched.
	 */

	public final void setupDomains() {

		m_domains.removeAllItems();
		//	String detail = new String(); // get all recipients
		String datas[]; //sender domains
		String datar[]; //rec domains
		boolean reverse = help_sort_checkbox.isSelected();


		try {
			synchronized (m_jdbcView) {
				datas = m_jdbcView.getSQLDataByColumn("select distinct sdom from email order by sdom asc", 1);
				//datas = m_jdbcView.getColumnData(0);
				datar = m_jdbcView.getSQLDataByColumn("select distinct rdom from email order by rdom asc", 1);
				//datar = m_jdbcView.getColumnData(0);

			}
			//we will do an O(n) merge from the two lists.
			HashMap<String, String> listname = new HashMap<String, String>();

			if (reverse) {
				for (int i = 0; i < datas.length; i++) {
					listname.put(Utils.reverseString(datas[i]), "");
				}
				for (int i = 0; i < datar.length; i++) {
					String key = Utils.reverseString(datar[i]);
					if (!listname.containsKey(key)) {
						listname.put(key, "");
					}
				}
			} else {
				//non reverse
				for (int i = 0; i < datas.length; i++) {
					listname.put(datas[i], "");
				}
				for (int i = 0; i < datar.length; i++) {
					if (!listname.containsKey(datar[i])) {
						listname.put(datar[i], "");
					}
				}
			}
			ArrayList<String> names = new ArrayList<String>(listname.size());

			for (Iterator<String> it = listname.keySet().iterator(); it.hasNext();) {
				names.add(it.next());
			}

			Collections.sort(names);

			String namel[] = (String[]) names.toArray(new String[names.size()]);

			if (reverse) {
				for (int i = 0; i < namel.length; i++) {
					namel[i] = Utils.reverseString(namel[i]);
				}
			}


			/*
			 * int s=0,r=0; //sender and rec int check=0; int count=0; Vector
			 * m_domains2 = new Vector();
			 * 
			 * while(s <datas.length && r <datar.length) {
			 * 
			 * //since both lists are in sorted order, we would like to go
			 * thorugh them //and collect only unque items check =
			 * datas[s].trim().compareToIgnoreCase(datar[r].trim()); //if equal
			 * then only need one if(check==0) { //since its the same need only
			 * one copy m_domains2.add(datas[s]); s++;r++; } else if(check <0)
			 * //rcpt is less need to add it { /* if(reverse) {
			 * m_domains2.add(datar[r]); r++; } /
			 * 
			 * m_domains2.add(datas[s]); s++; } else { /* if(reverse) {
			 * m_domains2.add(datas[s]); s++; } / m_domains2.add(datar[r]); r++; }
			 * count++; } //seems to fix problem of missing last elements. works
			 * odnt know why if(check!=0 && r <datar.length)
			 * m_domains2.add(datar[r]); else if (check==0 && s <datas.length)
			 * m_domains2.add(datas[s]);
			 * 
			 * if(reverse) { for(int i=0;i <m_domains2.size();i++) {
			 * StringBuffer sb = new StringBuffer(
			 * (String)m_domains2.elementAt(i));
			 * m_domains2.setElementAt(sb.reverse().toString(),i); }
			 * 
			 *  }
			 */



			ComboBoxModel model = new DefaultComboBoxModel(namel);//m_domains2);
			m_domains.setModel(model);


			//if one is less than the other need to add rest
			/*
			 * while(s <datas.length) {count++;
			 * m_domains.addItem(datas[s++][0]); } while(r <datar.length)
			 * {count++; m_domains.addItem(datar[r++][0]); }
			 */
			//          System.out.println("in setup domina have setup " + count);
		} catch (Exception sqle) {
			System.out.println("problems in setup domains : " + sqle);
		}
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	public final void getStatistics() {
		
		if(m_jdbcView == null){
			JOptionPane.showMessageDialog(LoadDB.this,"Problem with database handle");
			return;
		}
		
		
		//Stats = new JTextArea(30,45);
		//	select count(*) from email;
		//select distinct numattach, count(*) from email group by numattach;
		//select avg(size),numattach from email group by numattach;
		// select count(*),type from message group by type;

		String detail = new String("<html>"); // get all recipients
		String data[][];
		//count unmber of records
		synchronized (m_jdbcView) {
		try {
			// synchronized (m_jdbcView)
			//{
			//   m_jdbcView.getSqlData("select count(*) from email");
			//   data = m_jdbcView.getRowData();
			//	}
			//  if(data!=null)
			
			detail += "Number of Email Records: " + m_jdbcView.getCountByTable("email") + "<P>";
			System.out.print("1/6");
				data = m_jdbcView.getSQLData("select count(distinct mailref) from email");
				//data = m_jdbcView.getRowData();
				System.out.print(" 2/6");

			if (data != null)
				detail += "Number of Unique Emailrefs in Email Records (ie emails): " + data[0][0] + "<P>";

			
				data = m_jdbcView.getSQLData("select count(mailref),folder from email group by folder");
				//data = m_jdbcView.getRowData();
				System.out.print(" 3/6");

			detail += "<hr><P>Email files/folders (data sources) in Database:<P><table>";
			for (int i = 0; i < data.length; i++) {
				detail += "<tr><td>" + data[i][0] + "</td><td>" + data[i][1] + "</td></tr>";
			}
		
				data = m_jdbcView.getSQLData("select distinct numattach, count(*) from email group by numattach");
				//data = m_jdbcView.getRowData();
				System.out.print(" 4/6");
			detail += "</table><P><HR><P>Number of emails per number of attachments stats:<P><table>";
			for (int i = 0; i < data.length; i++) {
				detail += "<tr><td>" + data[i][1] + "</td><td>" + data[i][0] + "</td></tr>";
			}

			
				data = m_jdbcView.getSQLData("select avg(size),numattach,count(*) from email group by numattach");
				//data = m_jdbcView.getRowData();
				System.out.print(" 5/6");
			detail += "</table><P>Average size msg per number of attachment stats and how many in each catagory:<P><table>";
			for (int i = 0; i < data.length; i++) {
				detail += "<tr><td>" + data[i][1] + "</td><td>" + data[i][0] + "</td></tr>";
			}

			
				data = m_jdbcView.getSQLData("select count(*),type from message group by type");
				//data = m_jdbcView.getRowData();
				System.out.println("6/6");
			detail += "</table><hr><P>Types of Attachments, with count of each:<P><table>";
			for (int i = 0; i < data.length; i++) {
				detail += "<TR><TD>" + data[i][1] + "</td><TD>" + data[i][0] + "</td></tr>";
			}
			detail += "</table><P>";
		} catch (SQLException e) {
			detail += "<P>Error in loaddb report generator: " + e;
		}
		}//end synchronized
		m_stats.setText(detail + "</html>");
		//Stats.setText(detail);
		//Stats.setCaretPosition(0);

		data = null;//attempto get gc
	}//end getstatistics





	/**
	 * This part takes care of all our clickable actions :) on the GUI.
	 *
	 * @param e the e
	 */
	public void actionPerformed(ActionEvent e) {

		String lnfName = e.getActionCommand();
		if (lnfName.startsWith("ApproveSelection")) {
			gd = chooser.getCurrentDirectory();
			Filename = new String(chooser.getSelectedFile().getPath());
			// + chooser.getSelectedFile().getName();
			legalfile = true;//assumne that choose good file
			loadwin = false;
			Load.remove(chooser);
			Load.setVisible(false);
			Load.dispose();
			pointer_up.setText(Filename);
		} else if (lnfName.startsWith("CancelSelection")) {
			loadwin = false;
			Load.remove(chooser);
			Load.setVisible(false);
			Load.dispose();

		}
		//    else if (lnfName.indexOf("swing") > 0 )
		//{
		//	try {
		//UIManager.setLookAndFeel(lnfName);
		//SwingUtilities.updateComponentTreeUI(frame);
		//frame.pack();
		//frame.setBounds(100,100,480,200); //moves it x,y,width,height
		//
		//}
		//catch (Exception exc) {
		//JRadioButton button = (JRadioButton)e.getSource();
		//button.setEnabled(false);
		//updateState();
		/// System.err.println("Could not load LookAndFeel: " + lnfName);
		//}
		//}///end else if

	}//end actionperformed


	/**
	 * Adds the action listener.
	 *
	 * @param listener the listener
	 */
	public void addActionListener(ActionListener listener) {
		m_vActionListeners.add(listener);
	}

	/**
	 * Removes the action listener.
	 *
	 * @param listener the listener
	 */
	public void removeActionListener(ActionListener listener) {
		m_vActionListeners.remove(listener);
	}

	/**
	 * Fire data loaded.
	 */
	private void fireDataLoaded() {
		ActionEvent evt = new ActionEvent(this, 0, "Data Loaded");
		Enumeration<ActionListener> enumWalker = m_vActionListeners.elements();
		while (enumWalker.hasMoreElements()) {
			ActionListener listener = enumWalker.nextElement();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * Method to draw the help instructions To make it generic it takes a string
	 * and puts it on the screen.
	 *
	 * @param S the s
	 */
	public final void DrawHelp(final String S) {
		//use a boolean helpwin which is set if window in existance, then only
		// need to refocus
		if (helpwin) {

			msg.setText(S);
			//"This is the help window\nsecond line!\nI Love you Chana!!!
			Help.repaint();
			Help.requestFocus();
			return;
		}

		//so need to make the window
		Help = new JFrame("Help");

		Help.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				helpwin = false;
				Help.dispose();
			}
		});
		msg.setText(S);

		Rectangle bounds = Help.getBounds();
		Help.setLocation(300 + bounds.x, 50 + bounds.y);
		Help.add("Center", msg);
		Help.pack();
		Help.setVisible(true);
		helpwin = true; //set the boolean to true
	}//end DrawHelp


	/**
	 * method to load file SEE FILECHOOSER DEMO WHICH COMES WITH JDK TO DO FILE
	 * CHOOSING.
	 */
	/*
	 * public void Load() { //use a boolean helpwin which is set if window in
	 * existance, then only need to refocus if(loadwin) { Load.requestFocus();
	 * return; } //so need to make the window Load = new Frame("Load");
	 * 
	 * Load.addWindowListener(new WindowAdapter() { public void
	 * windowClosing(WindowEvent e) {loadwin=false;Load.dispose(); } });
	 * 
	 * Rectangle bounds2 =Load.getBounds(); Load.setLocation(200 +bounds2.x,45
	 * +bounds2.y);
	 * 
	 * location = new JTextField("Current File:"+Filename,40);
	 * 
	 * location.setEditable(false); chooser = new JFileChooser();//,new
	 * FileSystemView()); //next class from jdk demo provided by SUN
	 * ExampleFileFilter filter = new ExampleFileFilter();
	 * filter.addExtension("txt"); filter.setDescription("plain text format");
	 * 
	 * chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	 * //chooser.setCurrentDirectory(new File("/"));
	 * 
	 * chooser.setFileHidingEnabled(false);
	 * chooser.setMultiSelectionEnabled(false); chooser.setFileFilter(filter);
	 * if(gd!=null) chooser.setCurrentDirectory(gd);
	 * 
	 * 
	 * chooser.updateUI(); chooser.addActionListener(this);
	 * 
	 * Load.add("North",location); Load.add("Center",chooser); Load.pack();
	 * Load.setVisible(true); loadwin=true; //set the boolean to true }//end
	 * Load
	 */
	/**
	 * tests the format of the string by running the pattern analyzer
	 */
	public final void testFormat() {
		//file to open will be in pointer_up.getText()
		//Class1 C = new Class1(pointer_up.getText());
		System.out.println("Asked to open : " + pointer_up.getText());
		//       DrawHelp(C.Report());

	}//end test format

/**
 * import the data into EMT.
 *
 * @param usePerl the use perl
 * @param processingFile the processing file
 * @param flushDB the flush db
 * @param logFile the log file
 */
	public final void goImportFile(boolean usePerl, String processingFile,boolean flushDB,String logFile) {

		m_winGui.setEnabled(false);


		try {

			
			m_status.setText("Parsing file" + processingFile);
			File fcheker = new File(processingFile);
			if (!fcheker.exists()) {
				legalfile = false;
				JOptionPane.showMessageDialog(this, "File does not exist: " + processingFile);
				return;
			} else if (!fcheker.canRead()) {

				JOptionPane.showMessageDialog(this, "Can't read selected file: " + processingFile);
				return;
			}
            
			//lets create a log file and add to it
			File logf = new File(logFile);
			//if it doesn't exist, lets start it with the number currently in the db
			try{
			PrintWriter foutlog = new PrintWriter( new FileOutputStream(logFile,true));
			Date now = new Date();
			if(flushDB){
				foutlog.println(now + "\twill flush DB");
			}
			else{
				foutlog.println(now + "\tDB email count: " + this.m_jdbcView.getCountByTable("email"));
			}
			
			foutlog.println(now +"\t"+processingFile);
			
			foutlog.close();
			
			}catch(FileNotFoundException fno){
				System.out.println("problem with log file: "+ logFile);
			}
			
			//done logging
			
			//		System.out.println("in go..going to call perl script on "+ s);
			//try {

				if (usePerl) {
					if (!(new File(perl.getText().trim())).exists()) {
						JOptionPane.showMessageDialog(this, "Perl parser not found at given location");
						return;
					}
				}

				String cmds[] = new String[20];
				for (int i = 0; i < 20; i++)
				{	cmds[i] = new String();
				}
				if (usePerl) {
					cmds[0] = "perl";
					cmds[1] = perl.getText().trim();
					cmds[2] = "-i";
					cmds[3] = processingFile;//"c:\\met\\parse\\spam";
					cmds[4] = "-n";
					cmds[5] = "-db";
					cmds[6] = database;
					cmds[7] = host;
					cmds[8] = user;
					cmds[9] = pass;
					cmds[10] = "-w";
					cmds[11] = wordlist;


					if (m_logger_chkbox.isSelected())
						cmds[12] = "-log";

					if (flushDB)
						cmds[13] = "-flush";

					if (m_chompname_chkbx.isSelected())
						cmds[14] = "-chompname";

					
					if(m_realignDates_chkbx.isSelected()){
						cmds[15] = "-aligndates";
					}
					
					if (!m_from_seperator_chkbox.isSelected()) {
						cmds[16] = "-from";
						cmds[17] = frompattern_textfld.getText().trim();
					}
					
					
					
					
					Runtime rt = Runtime.getRuntime();
					Process proc = null;
					try{
					 proc = rt.exec(cmds);
					}catch(IOException  ee){
						ee.printStackTrace();
					}
					final InputStream stdout = proc.getInputStream();
					final InputStream stderr = proc.getErrorStream();

					System.out.println("Processing:");

					(new Thread() {
						public void run() {
							try {
								int c;
								InputStreamReader isr = new InputStreamReader(stdout);

								while ((c = isr.read()) > -1)
									System.out.print((char) c);
							} catch (IOException ioex) {
								// ignore
							}
						}
					}).start();

					(new Thread() {
						public void run() {
							try {
								String line = null;
								InputStreamReader isr = new InputStreamReader(stderr);
								BufferedReader br = new BufferedReader(isr);

								File f = new File("parser.err");
								f.createNewFile();

								if (f.exists() && f.canWrite()) {
									PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));

									pw.println(new Date());

									while ((line = br.readLine()) != null) {
										pw.println(line);
										line = null;
									}

									pw.close();
								}
							} catch (IOException ioex) {
								// ignore
							}
						}
					}).start();
					int exitVal = -1;
try{
					exitVal = proc.waitFor();
}catch(InterruptedException ie){
	ie.printStackTrace();
}
					
					System.out.println("Process exitValue: " + exitVal);

					if (exitVal == 0) {
						m_status.setText("Refreshing with newly loaded data");
						fireDataLoaded();
					} else {
						JOptionPane.showMessageDialog(this, "Parser failed to load data into database");
					}
				} else//not perl java version
				{

					cmds[0] = "-i";
					cmds[1] = processingFile;//"c:\\met\\parse\\spam";
					cmds[2] = "-n";
					cmds[3] = "-db";
					cmds[4] = database;
					cmds[5] = host;
					cmds[6] = user;
					cmds[7] = pass;
					cmds[8] = "-w";
					cmds[9] = wordlist;
					cmds[10] = "-typedb";
					cmds[11] = this.m_jdbcView.getTypeDB();

					//force data type
					if (button_mbox.isSelected()) {
						cmds[12] = "-forcembox";
					} else if (button_outlook.isSelected()) {
						System.out.println("outlook1");
						cmds[12] = "-forceoutlook";

					} else if (button_outlookexpress.isSelected()) {
						System.out.println("outlook2");
						cmds[12] = "-forceoutlook2";

					} else if (button_pst.isSelected()) {
						System.out.println("pst");
						cmds[12] = "-forcepst";

					} //else if (button_im.isSelected()) {
					//	cmds[12] = "-forceim";

//					}




					if (m_logger_chkbox.isSelected())
						cmds[13] = "-log";

					if (flushDB)
						cmds[14] = "-flush";

					if (m_chompname_chkbx.isSelected())
						cmds[15] = "-chompname";

					
					if(m_realignDates_chkbx.isSelected()){
						cmds[16] = "-aligndates";
					}
					
					
					if (!m_from_seperator_chkbox.isSelected()) {
						cmds[17] = "-from";
						cmds[18] = frompattern_textfld.getText().trim();
					}

					BusyWindow BBW = new BusyWindow("Parser Window","Going to process emails",false);
					BBW.setVisible(true);
					EMTParser emtp = new EMTParser(cmds, m_jdbcView, BBW);//done
					BBW.setVisible(false);												   // :)
					
					try{
						PrintWriter foutlog = new PrintWriter( new FileOutputStream(logFile,true));
						Date now = new Date();
						foutlog.println(now + "\tDB email count: " + this.m_jdbcView.getCountByTable("email"));
						
						foutlog.close();
						
						}catch(FileNotFoundException fno){
							System.out.println("problem with log file: "+ logFile);
						}
					m_winGui.updateRecords(true,"%@%");
				}

			//} catch (Throwable t) {
		//		t.printStackTrace();
		//	}

		} finally {
			m_status.setText(" ");
			m_winGui.setEnabled(true);


		}

	}//end Go

	
	
	/**
	 * Gets the log name.
	 *
	 * @return the log name
	 */
	private String getLogName(){
		return m_emtconfig.getProperty(EMTConfiguration.DBNAME)+"import.log";
	}
	

	/**
	 * for formatting.
	 */
	public final void updateState() {

	}


}

/**
 * main to launch the program
 */
//    public static void main(String s[]) {
//	LoadDB panel = new LoadDB(); //creates a place on the screen
//	frame = new JFrame("scam1");//title the frame
//	frame.addWindowListener(new WindowAdapter() {
//	    public void windowClosing(WindowEvent e) {System.exit(0);}
//	});
//	
//	frame.getContentPane().add("Center", panel);
//	frame.pack();
//	frame.setBounds(100,100,480,200); //moves it x,y,width,height
//	
//	frame.setVisible(true);
//	panel.updateState();
//  }
//}








