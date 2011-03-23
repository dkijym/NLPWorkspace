/**
 *adopted from clique code (gui part) from sysd (copyright) yada yada
 *by shlomo hershkop, fall 2002
 *code is my own
 **/

package metdemo.Finance;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.ButtonGroup;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.EMTHelp;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.MultiPickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.VertexStringer;

/** this window is a variation of Experiments
 *@author German Creamer, October 2005
 *@author shlomo Jan 2007 - fixed up the window to integrate into emt easier
 */

public class Finance extends JScrollPane {

	private EMTDatabaseConnection DBConnect;
	private JLabel runningLabel;
	private JTextField sql_condition_fld;
	private JTextField raw_sql_datafile;
	private JTextField raw_name_network_fld;
	private JTextField txtField_processedGraphFile;
	//private JTextField corporate_name_run_fld;
	private JRadioButton edgeBetwButton;
	private File setupFileLocation = null;
	private File runFileLocation = null;
	private JCheckBox uniqueMailref, addToCurrent, useEmail, useMessage;
//	private JCheckBox newModel;//, forceLower, useStop;
	private static JCheckBox maxWeakComponent;
	private JFileChooser chooser;
//	private JSlider percentageSlider;
	private JComboBox  experimentDistribution;//body_length_use,

	// "No Changes","Even Spread","10% Spam","25% Spam","30% Spam ","50%
	// Spam","60% Spam","75% Spam","90% Spam"
	private static final int NOCHANGE = 0;
//	private static final int EVENSPREAD = 1;
	private static final int TEN = 2;
	private static final int TWENTYFIVE = 3;
	static final int THIRTY = 4;
	static final int FIFTY = 5;
	static final int SIXTY = 6;
	static final int SEVENTYFIVE = 7;
	static final int NINTY = 8;
	static final int TRAINALL = 0;
	static final int ONLYSPAM = 1;
	static final int ONLYLEGIT = 2;
	static public final int RUN_STATE = 1;
	static public final int SETUP_STATE = 0;
	static final int useColumn = 0;
	static final int exampleNameColumn = 1;
	static final int timeColumn = 2;
	static final int typeColumn = 3;
	static final int ratioColumn = 4;
	static final int bodysizeColumn = 5;
	static final int fileColumn = 6;
	static final int commentColumn = 7;
	static final int numberColumns = 8;
	static final String seperatorString = new String("####RESULTFILE####");
	static final String labelPAJEK = "PAJEK";
	static final String labelBIPART = "Bipartite";
	static final String labelBIPART2UNI = "Bipartite2unimodal";//maybe ?? "BIPARTITE2UNIMODAL"
	
	private String kpartiteType = labelPAJEK;
	private Graph mainGraph;
	private String resultsFile = "datasets/socNetIndic.txt";
	private String dataDir = "datasets";
	private StringLabeller lblr;//, lblr1;
	//static 
	//private HashMap<Integer, String> vertexLabel;
//	private boolean gotStats = false;
	private boolean maxWeakComponentBool = false;
	//private String wekaFile = "datasets/weka.txt";
//	private String mergeFile = "datasets/merge.txt";
//	private String accountingFile;
//	private static HashMap<String, CompanyData> companyStats;
	/*private boolean pajekGraph = false, bipartiteGraph = false,
			bipartiteUni = false, bipartiteWeak = false;
	*//*static JButton[] graphLoaded = {new JButton("No Graph Loaded"),
			new JButton("Pajek Graph Loaded"),
			new JButton("Bipartite Graph Loaded"),
			new JButton("Bipartite Unimodal Loaded"),
			new JButton("Bipartite Small World Loaded")};
*/
	private winGui m_wingui;
	/**
	 * Main constructor
	 * @param emtconnect main handle to the database
	 */
	public Finance(EMTDatabaseConnection emtconnect, winGui mguiHandle) {
		
		
		m_wingui = mguiHandle;
		JPanel mainjp = new JPanel();
		DBConnect = emtconnect;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		mainjp.setLayout(gridbag);
		constraints.insets = new Insets(2, 2, 2, 2);

		EMTHelp helpButton = new EMTHelp(EMTHelp.FINANCEHELP);
		//place the help button
		constraints.gridx = 0;
		constraints.gridy = 0;
		gridbag.setConstraints(helpButton, constraints);
		//mainPanel.
		mainjp.add(helpButton);

		JLabel Toplabel = new JLabel("Setup the network files on DB: "
				+ DBConnect.getDatabaseName());
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		gridbag.setConstraints(Toplabel, constraints);
		mainjp.add(Toplabel);

		JLabel label_filename = new JLabel("Raw SQL Filename:"); //HERE
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(label_filename, constraints);
		mainjp.add(label_filename);

		raw_sql_datafile = new JTextField(25);
		constraints.gridx = 1;
		constraints.gridy = 1;
		gridbag.setConstraints(raw_sql_datafile, constraints);
		mainjp.add(raw_sql_datafile);

		JButton browseButton = new JButton("Browse");
		constraints.gridx = 2;
		constraints.gridy = 1;
		gridbag.setConstraints(browseButton, constraints);
		mainjp.add(browseButton);

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileHidingEnabled(false);
		chooser.setMultiSelectionEnabled(false);
		setupFileLocation = new File(System.getProperty("user.dir"));

		// we are in top directory
		// lets try to create experimental folder for later usage

		File subfolder = new File(System.getProperty("user.dir"), "exprmnt");
		if (!subfolder.exists()) {
			if (subfolder.mkdir()) {
				setupFileLocation = subfolder;
			} else {
				JOptionPane.showMessageDialog(Finance.this,
						"Problem making exprmt subdirectory");
			}
		} else {
			setupFileLocation = subfolder;
		}

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (setupFileLocation != null) {
					chooser.setCurrentDirectory(setupFileLocation);
				}
				int returnVal = chooser.showOpenDialog(Finance.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// File file = fc.getSelectedFile();
					// legalfile = true;
					raw_sql_datafile.setText(chooser.getSelectedFile()
							.getPath());
					setupFileLocation = chooser.getCurrentDirectory();

					// This is where a real application would open the file.
					// log.append("Opening: " + file.getName() + "." + newline);
				} else {
					// log.append("Open command cancelled by user." + newline);
				}

			}
		});

		JButton viewFile = new JButton("View File");

		viewFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//String input = new String();
				String filename = raw_sql_datafile.getText().trim();

				visualizeStatsResults(filename);
			}
		});

		constraints.gridx = 3;
		constraints.gridy = 1;
		gridbag.setConstraints(viewFile, constraints);
		mainjp.add(viewFile);

		JButton grabDataButton = new JButton("Execute: Use info from above");
		grabDataButton
				.setToolTipText("Grab all users from above data and dump to sql raw file");
		grabDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//String input = new String();
				String filename = raw_sql_datafile.getText().trim();

				processUserDataSQL(filename,(String)m_wingui.getSelectedUser());
			}
		});
		constraints.gridwidth = 2;
		constraints.gridx = 3;
		constraints.gridy = 0;
		gridbag.setConstraints(grabDataButton, constraints);
		mainjp.add(grabDataButton);
		constraints.gridwidth = 1;
		// target condition
		JLabel label_sql = new JLabel("SQL where condition:");
		constraints.gridx = 0;
		constraints.gridy = 2;
		gridbag.setConstraints(label_sql, constraints);
		mainjp.add(label_sql);

		sql_condition_fld = new JTextField(25);
		//        sql_fld.setText("where class = 's' or class='n'");
		constraints.gridx = 1;
		constraints.gridy = 2;
		gridbag.setConstraints(sql_condition_fld, constraints);
		mainjp.add(sql_condition_fld);

		JButton testsql = new JButton("Test SQL");
		constraints.gridx = 2;
		constraints.gridy = 2;
		gridbag.setConstraints(testsql, constraints);
		mainjp.add(testsql);

		testsql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String sql = new String(
						"Select count(distinct email.mailref) from ");
				if (useEmail.isSelected() && !useMessage.isSelected()) {
					sql += " email ";
				} else if (useEmail.isSelected() && useMessage.isSelected()) {
					sql += " email left join message on email.mailref=message.mailref ";
				} else if (!useEmail.isSelected() && useMessage.isSelected()) {
					sql += " message ";
				} else {
					return;
				}

				sql += sql_condition_fld.getText().trim();

				try {
					String data[][] = DBConnect.getSQLData(sql);
					JOptionPane.showMessageDialog(Finance.this,
							"Number of lines returned: " + data[0][0]);
				} catch (SQLException s) {
					JOptionPane.showMessageDialog(Finance.this,
							"Problem with: " + sql);
					System.out.println(sql + "\n problem error 3: " + s);
				}

			}
		});

		JButton runsql = new JButton("Execute SQL/Create File");
		runsql.setBackground(Color.red);
		runsql
				.setToolTipText("If Filename is not defined, a temporal bipartite file is generated.");
		constraints.gridx = 3;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		gridbag.setConstraints(runsql, constraints);
		mainjp.add(runsql);

		runsql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// fileNameNetwork=

				//basically get sql data and pass it to a file, and then write the filename to next field box
				raw_name_network_fld
						.setText(setupRawSQLTXTFile(raw_sql_datafile.getText()
								.trim(), experimentDistribution
								.getSelectedIndex(), getSQLdata())); //String is passed with name of Pajek file in case that user does not want to use intermediate files.
			}
		});

		addToCurrent = new JCheckBox("Add to current file", false);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		gridbag.setConstraints(addToCurrent, constraints);
		mainjp.add(addToCurrent);

		useEmail = new JCheckBox("Use Email Table", true);
		String[] distributions = new String[]{"No Changes", "Even Spread",
				"10% Spam", "25% Spam", "30% Spam", "50% Spam", "60% Spam",
				"75% Spam", "90% Spam"};
		experimentDistribution = new JComboBox(distributions);
		experimentDistribution
				.setToolTipText("<html>Distribute the classes in the output file but keep in time order.<P> For example if time wise all spam come first,<P> checking this will space them evenly over time with other classess</html>");

		JPanel twochoices = new JPanel();
		twochoices.add(useEmail);
		twochoices.add(experimentDistribution);
		constraints.gridx = 1;
		constraints.gridy = 3;
		gridbag.setConstraints(twochoices, constraints);
		mainjp.add(twochoices);

		useMessage = new JCheckBox("Use Message Table", false);
		constraints.gridx = 2;
		constraints.gridy = 3;
		gridbag.setConstraints(useMessage, constraints);
		mainjp.add(useMessage);

		uniqueMailref = new JCheckBox("Unique Mailref in File", false);
		constraints.gridx = 3;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		gridbag.setConstraints(uniqueMailref, constraints);
		mainjp.add(uniqueMailref);

		JTextPane separator = new JTextPane();
		separator.setEditable(false);
		separator.setBackground(Color.orange);
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(separator, constraints);
		mainjp.add(separator);

		setViewportView(mainjp);
		JTextPane separator3 = new JTextPane();
		separator3.setEditable(false);
		separator3.setBackground(Color.orange);
		constraints.gridx = 0;
		constraints.gridy = 21;
		constraints.gridwidth = 16;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(separator3, constraints);
		mainjp.add(separator3);

		/*************************************/
		// Social network generation:
		int initialRef = 5;
		runningLabel = new JLabel("Social networks: ");
		constraints.gridx = 0;
		constraints.gridy = initialRef;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(runningLabel, constraints);
		mainjp.add(runningLabel);

		//Convert to Pajek format:
		JLabel convertFileNetwork = new JLabel(
				"File (.txt) to convert to Pajek format:");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(convertFileNetwork, constraints);
		mainjp.add(convertFileNetwork);
		//shlomo
		raw_name_network_fld = new JTextField(25);
		constraints.gridx = 1;
		constraints.gridy = initialRef + 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(raw_name_network_fld, constraints);
		mainjp.add(raw_name_network_fld);

		JButton raw_browseButton3 = new JButton("Browse");
		constraints.gridx = 2;
		constraints.gridy = initialRef + 1;
		gridbag.setConstraints(raw_browseButton3, constraints);
		mainjp.add(raw_browseButton3);

		JButton convertTxt2pajek = new JButton("Txt2Pajek");
		constraints.gridx = 3;
		constraints.gridy = initialRef + 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(convertTxt2pajek, constraints);
		mainjp.add(convertTxt2pajek);

		JButton convertTxt2bipartite = new JButton("Txt2Bipartite");
		constraints.gridx = 4;
		constraints.gridy = initialRef + 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(convertTxt2bipartite, constraints);
		mainjp.add(convertTxt2bipartite);

		//JButton pluggableNet = new JButton("RandomMixed");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 2;
		constraints.gridwidth = 1;
		//gridbag.setConstraints(pluggableNet, constraints);
		//mainjp.add(pluggableNet);

		//      Select network file:
		JLabel runningFileNetwork = new JLabel(
				"Network file (net: Pajek, bp: bipart.):");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 3;
		constraints.gridwidth = 1;
		gridbag.setConstraints(runningFileNetwork, constraints);
		mainjp.add(runningFileNetwork);
		//shlomo
		txtField_processedGraphFile = new JTextField(25);
		constraints.gridx = 1;
		constraints.gridy = initialRef + 3;
		constraints.gridwidth = 1;
		gridbag.setConstraints(txtField_processedGraphFile, constraints);
		mainjp.add(txtField_processedGraphFile);

		JButton run_browseButton2 = new JButton("Browse");
		constraints.gridx = 2;
		constraints.gridy = initialRef + 3;
		gridbag.setConstraints(run_browseButton2, constraints);
		mainjp.add(run_browseButton2);
		File subfolder2 = new File(System.getProperty("user.dir"), dataDir);
		if (!subfolder2.exists()) {
			if (subfolder2.mkdir()) {
				setupFileLocation = subfolder2;
			} else {
				JOptionPane.showMessageDialog(Finance.this,
						"Problem making datasets subdirectory");
			}
		} else {
			setupFileLocation = subfolder2;
		}
		runFileLocation = setupFileLocation.getAbsoluteFile();

		// 		To convert to Pajek or bipartite format:
		raw_browseButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (runFileLocation != null) {
					chooser.setCurrentDirectory(runFileLocation);
				}
				int returnVal = chooser.showOpenDialog(Finance.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					raw_name_network_fld.setText(chooser.getSelectedFile()
							.getPath());
					runFileLocation = chooser.getCurrentDirectory();
					//shlomo - fileConvertNameNetwork = name_convert_network_fld.getText().trim();
				} else {
					//log.append("Open command cancelled by user." + newline);
				}

			}
		});

		run_browseButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (runFileLocation != null) {
					chooser.setCurrentDirectory(runFileLocation);
				}
				int returnVal = chooser.showOpenDialog(Finance.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// File file = fc.getSelectedFile();
					// legalfile = true;
					txtField_processedGraphFile.setText(chooser.getSelectedFile()
							.getPath());
					runFileLocation = chooser.getCurrentDirectory();
					//shlomo - fileNameNetwork = name_network_fld.getText().trim();

					// This is where a real application would open the file.
					//log.append("Opening: " + file.getName() + "." + newline);
				} else {
					// log.append("Open command cancelled by user." + newline);
				}

			}
		});

		JLabel formatGraphLabel = new JLabel("Select graph format:");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 4;
		constraints.gridwidth = 1;
		gridbag.setConstraints(formatGraphLabel, constraints);
		mainjp.add(formatGraphLabel);

		String[] graphTypeString = new String[]{labelPAJEK, labelBIPART,
				labelBIPART2UNI};
		JComboBox graphType = new JComboBox(graphTypeString);
		graphType.setSelectedIndex(0);
		graphType
				.setToolTipText("<html>Pajek is generic format.<P> Bipartite is format with edges only for bipartite graphs.<P> Bipartite2unimodal converts to unimodal.</html>");
		constraints.gridx = 1;
		constraints.gridy = initialRef + 4;
		constraints.gridwidth = 1;
		gridbag.setConstraints(graphType, constraints);
		mainjp.add(graphType);

		maxWeakComponent = new JCheckBox("Max Weak Component", false);
		maxWeakComponent
				.setToolTipText("<html>Get graph with only max weak component<P>Use only for Pajek format or unimodal graphs</html>");
		constraints.gridx = 2;
		constraints.gridy = initialRef + 4;
		constraints.gridwidth = 1;
		maxWeakComponent.setSelected(true);
		gridbag.setConstraints(maxWeakComponent, constraints);
		mainjp.add(maxWeakComponent);

		JButton graphStatisticsLoader = new JButton("Load Graph & Statistics");
		graphStatisticsLoader.setToolTipText("<html>Get graph and calculate statistics according to type of graph. If statistics are not consistent, select max weak component using Pajek format or unimodal graphs</html>");
		constraints.gridx = 3;
		constraints.gridy = initialRef + 4;
		constraints.gridwidth = 1;
		gridbag.setConstraints(graphStatisticsLoader, constraints);
		mainjp.add(graphStatisticsLoader);

		JButton simpleNetButton = new JButton("Simple network");
		simpleNetButton
				.setToolTipText("Need to select 'Get graph & statistics' first.");
		constraints.gridx = 4;
		constraints.gridy = initialRef + 4;
		constraints.gridwidth = 1;
		gridbag.setConstraints(simpleNetButton, constraints);
		mainjp.add(simpleNetButton);

		JButton clusterNet = new JButton("Cluster network");
		clusterNet
				.setToolTipText("<html>Need to select 'Get graph & statistics' first.<P> Run Edge Betweenneess first and then voltage cluster</html>");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 5;
		constraints.gridwidth = 5;
		gridbag.setConstraints(clusterNet, constraints);
		mainjp.add(clusterNet);

		JLabel typeGraphLabel = new JLabel("Select type of graph:");
		constraints.gridx = 0;
		constraints.gridy = initialRef + 5;
		constraints.gridwidth = 1;
		gridbag.setConstraints(typeGraphLabel, constraints);
		mainjp.add(typeGraphLabel);


		String edgeBetwGraph = "EDGEBETWEEN";
		String VoltageClusterGraph = "VOLTAGECLUSTER";

		edgeBetwButton = new JRadioButton(edgeBetwGraph);
		edgeBetwButton.setMnemonic(KeyEvent.VK_S);
		edgeBetwButton.setActionCommand(edgeBetwGraph);

		JRadioButton voltageClusterButton = new JRadioButton(
				VoltageClusterGraph);
		voltageClusterButton.setMnemonic(KeyEvent.VK_R);
		voltageClusterButton.setActionCommand(VoltageClusterGraph);
		voltageClusterButton.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(edgeBetwButton);
		group.add(voltageClusterButton);

		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.add(edgeBetwButton);
		radioPanel.add(voltageClusterButton);
		constraints.gridx = 0;
		constraints.gridy = initialRef + 5;
		constraints.gridwidth = 2;
		gridbag.setConstraints(radioPanel, constraints);
		mainjp.add(radioPanel);


		graphType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JComboBox cb = (JComboBox) evt.getSource();
				String graphSelection = (String) cb.getSelectedItem();

				if (graphSelection.equals(labelPAJEK)) {
					kpartiteType = labelPAJEK;
				} else if (graphSelection.equals(labelBIPART)) {
					kpartiteType = labelBIPART;
				} else if (graphSelection.equals(labelBIPART2UNI)) {
					kpartiteType = labelBIPART2UNI;
				}
			}
		});

		convertTxt2pajek.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					//shlomo - was if fileConvertNameNetwork
					if (txtField_processedGraphFile.getText().trim() == null) {
						JOptionPane.showMessageDialog(Finance.this,
								"filename not setup");
						return;
					}

					boolean PAJEK = true;
					pajekUtilities pj = new pajekUtilities();

					HashMap<Integer, String> vertexLabel = pj.convertTextToPreGraph(raw_name_network_fld
							.getText().trim(), PAJEK, txtField_processedGraphFile
							.getText().trim(), txtField_processedGraphFile);
					//vertexLabel = pj.getLabels();
					if (vertexLabel != null) {
						JOptionPane
								.showMessageDialog(Finance.this,
										"Conversion is done. Select network file and/or graph format in panel below");
					} else {
						JOptionPane
								.showMessageDialog(Finance.this,
										"You may have problems in the conversion. Please check the files.");
					}
				} catch (FileNotFoundException e) {
					System.out.println("Problem opening files.");
					JOptionPane.showMessageDialog(Finance.this,
							"Problem opening files.");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(Finance.this,
							"Error reading from data file to convert to Pajek");
				}
			}
		});

		convertTxt2bipartite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (raw_name_network_fld.getText().trim().length() < 1) {
						JOptionPane.showMessageDialog(Finance.this,
								"filename not setup");
						return;
					}

					boolean PAJEK = false;
					pajekUtilities pj = new pajekUtilities();

					HashMap<Integer, String> vertexLabel = pj.convertTextToPreGraph(raw_name_network_fld
							.getText().trim(), PAJEK, txtField_processedGraphFile
							.getText().trim(), txtField_processedGraphFile);
					//vertexLabel = pj.getLabels();
					if (vertexLabel != null) {
						JOptionPane
								.showMessageDialog(Finance.this,
										"Conversion is done. Select network file and/or graph format in panel below");
					} else {
						JOptionPane
								.showMessageDialog(Finance.this,
										"You may have problems in the conversion. Please check the files.");
					}

				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(Finance.this,
							"Problem opening files.");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(Finance.this,
							"Error reading from data file to convert");
				}
			}
		});

		graphStatisticsLoader.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {

					//shlomo added for bad processing if not done correctly
					if (txtField_processedGraphFile.getText().trim().length() < 1) {
						JOptionPane.showMessageDialog(Finance.this,
								"filename to load not setup, please correct");
						return;
					}

					if (maxWeakComponent.isSelected()) {
						maxWeakComponentBool = true;
					} else {
						maxWeakComponentBool = false;
					}

					System.out.println("test4 by load grphastats" + kpartiteType);
					/*if (!kpartite.equals(labelPAJEK)) {
						try {
							boolean PAJEK = false;
							txt2pajek pj = new txt2pajek();
							String[] prefix = name_convert_network_fld
									.getText().trim().split(".bp");
							String txtFile = prefix[0] + ".txt";
							vertexLabel = pj.convert(txtFile, PAJEK,
									name_convert_network_fld.getText().trim());
							//vertexLabel = pj.getLabels();
							if (vertexLabel == null) {
								JOptionPane
										.showMessageDialog(Finance.this,
												"Error converting txt file to bp file, please try again");
							}
						} catch (FileNotFoundException e) {
							JOptionPane
									.showMessageDialog(
											Finance.this,
											"Problem opening txt file for bp conversion, NOTE: output stats will not contain vertex labels.");
						} catch (IOException e) {
							JOptionPane
									.showMessageDialog(
											Finance.this,
											"Error reading from data file to convert, NOTE: output stats will not contain vertex labels.");
						}
					}*/
					mainGraph = calculateStatistics(txtField_processedGraphFile
							.getText().trim(), kpartiteType/*, vertexLabel*/);
					System.out.println("Complete graph statistics\n");

					//JOptionPane.showMessageDialog(Finance.this, "Load graph & create file with statistics in datasets/socNetIndic.txt");
				} catch (IOException e) {
					add(new JLabel(e.toString()));
					e.printStackTrace();
				}
			}
		});

		simpleNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					DisplaySimpleversionGraphDraw(mainGraph);
				} catch (IOException e) {
					//shlomo no idea what this is supposed to be doing: add(new JLabel(e.toString()));
					e.printStackTrace();
				}
			}
		});

		clusterNet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				//shlomo for null graph type
				if (mainGraph == null) {

					JOptionPane.showMessageDialog(Finance.this,
							"No graph object loaded, click get graph");
					return;
				}

				try {
					Clustering cl = new Clustering();

					if (edgeBetwButton.isSelected()) {
						cl.setUpView(mainGraph, "EDGEBETWEEN", resultsFile,
								kpartiteType, lblr/*, vertexLabel*/);
					} else {
						cl.setUpView(mainGraph, "VOLTAGECLUSTER", resultsFile,
								kpartiteType, lblr/*, vertexLabel*/);
					}
					//shlomo - cl.setUpView(graph,typeGraph,resultsFile, kpartite,lblr, vertexLabel);

				} catch (IOException e) {
					add(new JLabel(e.toString()));
					e.printStackTrace();
				}
			}
		});

		/*pluggableNet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Pluggable pl = new Pluggable();
					String typeGraph = "RANDOMMIXED";
					Graph g = pl.getGraph(name_convert_network_fld.getText()
							.trim(), typeGraph);
					pl.startFunction(g);
				} catch (IOException e) {
					add(new JLabel(e.toString()));
					e.printStackTrace();
				}
			}
		});*/

	}

	//Functions definitions:
	/**
	 * Delete those files which are selected in the result table
	 *
	 */

	//If using email - SQL:
	//	String sql1="select sender,rcpt from email";
	//	String data1[][] = DBConnect.getSQLData(sql1);
	//	Reader br = new InputStreamReader(data1);

	/*Merge the file and social network stats together*/
	/*public void merge(String fileNameNetwork, char type,HashMap<String, CompanyData> companyStats) throws IOException {
		System.out.println("Merging " + type + " with " + fileNameNetwork);
		InputStream is = new FileInputStream(fileNameNetwork);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Collection companyInfo = companyStats.values();
		iterate through the file and merge the specific data together into an output file when the correct
		 * company name is found by checking the collection and comparing permnos
		 
		String line = br.readLine();
		line = br.readLine();
		String permno, date, car1, car2, size, frev, ltg, sue, sg, ta, capex, bp, ep, anfor, anforlag, felag, consensus, label;
		StringTokenizer tok;
		CompanyData data;
		parse through the data to store the economic indicators and store in the company data file
		while (line != null) {
			tok = new StringTokenizer(line, ",");

			permno = tok.nextToken();
			if (companyStats.containsKey(permno)) {
				for (int i = 0; i < 3; i++)
					tok.nextToken();
				date = tok.nextToken();
				if (date.equals("2003")) {
					data = companyStats.get(permno);
					for (int i = 0; i < 3; i++)
						tok.nextToken();
					data.car1(tok.nextToken());
					data.car2(tok.nextToken());
					data.size(tok.nextToken());
					data.frev(tok.nextToken());
					data.ltg(tok.nextToken());
					data.sue(tok.nextToken());
					data.sg(tok.nextToken());
					data.ta(tok.nextToken());
					data.capex(tok.nextToken());
					data.bp(tok.nextToken());
					data.anfor(tok.nextToken());
					data.anforlag(tok.nextToken());
					tok.nextToken();
					data.felag(tok.nextToken());
					tok.nextToken();
					data.consensus(tok.nextToken());
					tok.nextToken();
					data.label(tok.nextToken());
				}
			}
			line = br.readLine();
		}
		PrintWriter outputStreamIndic = null;
		String mergeFile = "datasets/merge.txt";
		printout data in weka or normal format
		try {
			if (type == 'n') {
				outputStreamIndic = new PrintWriter(new FileOutputStream(
						mergeFile));
				outputStreamIndic
						.println("Permno, degree, score, betweenness, HITS, avg dist, cluster coef, CAR1, CAR2, SIZE, FREV, LTG, SUE, SG, TA, CAPEX, BP, EP, ANFOR, ANFORLAG, FELAG, CONSENSUS, LABEL");
				Collection dataValues = companyStats.values();
				Iterator<CompanyData> itr = dataValues.iterator();
				while (itr.hasNext()) {
					data = itr.next();
					outputStreamIndic.println(data);
				}
				JOptionPane.showMessageDialog(Finance.this,
						"Output of regular file successful");
			} else {
				outputStreamIndic = new PrintWriter(new FileOutputStream(
						wekaFile));
				outputStreamIndic
						.println("% Weka file for company economic and social network data");
				outputStreamIndic.println("@RELATION company");
				outputStreamIndic.println();
				outputStreamIndic.println("@ATTRIBUTE degree NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE score NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE betweeness NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE HITS NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE avgDist NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE clustercoeff NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE car1 NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE car2 NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE size NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE frev NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE ltg NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE sue NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE sg NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE ta NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE capex NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE bp NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE ep NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE anfor NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE anforlag NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE felag NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE consensus NUMERIC");
				outputStreamIndic.println("@ATTRIBUTE label {-1,1}");
				outputStreamIndic.println();
				outputStreamIndic.println("@DATA");
				Collection dataValues = companyStats.values();
				Iterator itr = dataValues.iterator();
				while (itr.hasNext()) {
					data = (CompanyData) itr.next();
					outputStreamIndic.println(data.toWeka());
				}
				JOptionPane.showMessageDialog(Finance.this,
						"Output of Weka file successful");
			}
			outputStreamIndic.close();
		} catch (FileNotFoundException e) {
			String message1;
			if (type == 'n')
				message1 = "Error opening file(s) for indicators: " + mergeFile;
			else
				message1 = "Error opening file(s) for indicators: " + wekaFile;
			System.out.println(message1);
			System.exit(0);
		}
	}*/

	public void processUserDataSQL(String filename, String user) {

		if (filename.length() < 1) {
			JOptionPane.showMessageDialog(null,
					"no filename for saving raw data specified");
			return;

		}
		
		sql_condition_fld.setText("where sender = '"+user+"' or rcpt = '"+user+"'");
		//get above list
		//get huge sql statement
		//fetch data

		//send it to the right place to be saved

	}

	/**
	 * 
	 * @param filename
	 */
	static void visualizeStatsResults(String filename) {
		String input = new String();

		if (filename.length() < 1) {
			JOptionPane.showMessageDialog(null, "no filename specified");
			//            JOptionPane.showMessageDialog(Finance.this, "no filename specified");
			return;
		}

		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader bufread = new BufferedReader(
					new FileReader(filename));
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
		m_areaScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_areaScrollPane.setPreferredSize(new Dimension(550, 380));
		// ok we've setup the body text
		Object[] option_array = {"Save Me", "Save to new File", "Cancel/Close"};
		int c = JOptionPane.showOptionDialog(null, m_areaScrollPane,
				"Experiment" + filename, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, option_array,
				option_array[2]);

		if (c == JOptionPane.YES_OPTION) {
		} else if (c == JOptionPane.NO_OPTION) {
		} else {
		}
	}

	

	/**
	 * 
	 * @param fileNameNetwork
	 * @param kpartite
	 * @return
	 * @throws IOException
	 */
	public Graph calculateStatistics(String fileNameNetwork, String kpartite/*,
			HashMap<Integer, String> vertexLabel*/) throws IOException {
	
		System.out.println("in calc stats, looking at: " + fileNameNetwork + " and kpart: "
				+ kpartite);

		
		//Graph g = pajekUtilities.getGraph(fileNameNetwork, kpartite,maxWeakComponentBool);
		Object caththing[] =  pajekUtilities.getAndBuildGraph(fileNameNetwork, kpartite,maxWeakComponentBool);
		Graph g = (Graph)caththing[0];
		lblr = (StringLabeller)caththing[1];
		if (pajekUtilities.socNetworkStatistics(g, /*vertexLabel,*/ lblr, resultsFile, kpartite)) {
			System.out.println("Complete statistics calculation");
			visualizeStatsResults(resultsFile);
		} else {
			System.out.println("Something wrong with soc net calcs");
		}
		return g;
	}

	/**
	 * 
	 * @param g
	 * @throws IOException
	 */
	public void DisplaySimpleversionGraphDraw(final Graph gtoDraw) throws IOException {
		//shlomo
		//need to check for nullness on g
		if (gtoDraw == null) {

			JOptionPane.showMessageDialog(Finance.this,
					"No graph object loaded");
			return;
		}

		PluggableRenderer pr = new PluggableRenderer();
		//pr.setVertexStringer(lblr);
		
		
		
		
		final VisualizationViewer vv = new VisualizationViewer(new SpringLayout(gtoDraw),
				pr);
		//setup by shlomo replacing the labeler
		pr.setVertexStringer(new VertexStringer() {

            public String getLabel(ArchetypeVertex v) {
               
            	//want to only draw the string if we are in pick state
            	if(vv.getPickedState().isPicked(v)){
            		return v.toString();
				}
            	
            	
            	/*if(v.getClass() == UserClique.class)
            		return v.toString();
            	else*/
            	return "";
            }

			});

	    vv.setPickedState(new MultiPickedState());
	    
		
		
		JFrame jf = new JFrame();
		jf.getContentPane().add(vv);
		//jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
		System.out.println("Complete simple graph draw");

	}

	

	/**
	 * generate data for the graph algorithms the sql data processed expects sender, rcpt, count(mailref)
	 * @return
	 */
	public String[][] getSQLdata() {
		String sql = new String();
		if (uniqueMailref.isSelected()) {
			sql = "Select distinct sender,rcpt,count(mailref),mailref";//email.mailref,email.class,email.utime from ";
		} else {
			sql = "Select sender,rcpt,count(mailref) from ";
		}
		if (useEmail.isSelected() && !useMessage.isSelected()) {
			sql += " email ";
		} else if (useEmail.isSelected() && useMessage.isSelected()) {
			sql += " email left join message on email.mailref=message.mailref ";
		} else if (!useEmail.isSelected() && useMessage.isSelected()) {
			sql += " message ";
		} else {
			return null;//shlomo - fileConvertNameNetwork;
		}

		//SHLOMO: fixed was reversed so on condition wouldnt work
		sql += sql_condition_fld.getText().trim();
		sql += " group by sender,rcpt ";

		String data[][] = null;
		try {
			System.out.println("sql:" + sql);
			data = DBConnect.getSQLData(sql);

		} catch (SQLException s) {
			System.out.println(s);
		}
		return data;

	}

	/**
	 * The method will use a standard distribution over classes to get relevent sql data
	 * @param filename to save the sql information into
	 * @param experimentDistributionSelectedIndex
	 * @return the name of the file it used, in case of naming issues
	 */
	private String setupRawSQLTXTFile(String filename,
			int experimentDistributionSelectedIndex, String data[][]) {

		if (data == null) {
			return "";
		}

		boolean NOFILE = false;

		//shlomo - check for valid filename
		if (filename.length() < 1) {
			filename = setupFileLocation.getAbsolutePath() + File.separatorChar
					+ "temp1.txt";
			NOFILE = true;
		}

		if (filename.indexOf(File.separatorChar) < 0) {
			filename = setupFileLocation.getAbsolutePath() + File.separatorChar
					+ filename;
		}

		try {
			int[] spamlist = null;
			int spamOffset = 0;
			int[] nonspamlist = null;
			int nonspamOffset = 0;
			// might need to redistribute the data according to the distribution
			// drop list
			if (!(experimentDistributionSelectedIndex == NOCHANGE)) {
				// read in two list
				// all spam
				// all normal
				// for each distribution D (example D = .3)
				// while have spam && have non spam
				// fetch 40*D from spam and 40-40*D from normal

				for (int i = 0; i < data.length; i++) {
					if (data[i][1].startsWith("s")) {
						spamOffset++;
					} else {
						nonspamOffset++;
					}
				}
				// counted spam and non spam in test
				spamlist = new int[spamOffset];
				nonspamlist = new int[nonspamOffset];
				spamOffset = 0;
				nonspamOffset = 0;
				for (int i = 0; i < data.length; i++) {
					if (data[i][1].startsWith("s")) {
						spamlist[spamOffset++] = i;
					} else {
						nonspamlist[nonspamOffset++] = i;
					}
				}
				nonspamOffset = 0;
				spamOffset = 0;
			}
			BufferedWriter bufw = null;

			if (!uniqueMailref.isSelected()) {
				bufw = new BufferedWriter(new FileWriter(filename, addToCurrent
						.isSelected()));

				if ((experimentDistributionSelectedIndex == NOCHANGE)) {
					for (int i = 0; i < data.length; i++) {
						//bufw.write(data[i][0] + "\t" + data[i][1] + "\t" + data[i][2] + "\n");
						if (data[i][0].length() > 0 && data[i][1].length() > 0) {
							bufw.write(data[i][0] + "\t" + data[i][1] + "\n");
						}
					}
					// }
					// else{
					// TODO: do this for addition to file
				}
			} else // we want unique mailrefs
			{
				HashMap uniqueMailrefs = new HashMap();
				if (addToCurrent.isSelected()) {
					try {
						BufferedReader bufr = new BufferedReader(
								new FileReader(filename));
						String line = new String();
						while ((line = bufr.readLine()) != null) {
							int n = line.indexOf("\t");
							uniqueMailrefs.put(line.substring(0, n - 1), "");
						}
					} catch (IOException ii1) {

					}
				}
				bufw = new BufferedWriter(new FileWriter(filename, addToCurrent
						.isSelected()));

				if ((experimentDistributionSelectedIndex == NOCHANGE)) {

					for (int i = 0; i < data.length; i++) {
						if (!uniqueMailrefs.containsKey(data[0])) {
							//                            bufw.write(data[i][0] + "\t" + data[i][1] + "\t" + data[i][2] + "\n");
							if (data[i][0].length() > 0
									&& data[i][1].length() > 0)
								bufw.write(data[i][0] + "\t" + data[i][1]
										+ "\n");
						}
					}
				} else {
					// first to calculate chunks to grab from spam and non spam
					// lists
					int spamchunk = 0, nonspamchunk = 0;
					spamOffset = 0;
					nonspamOffset = 0;
					if (experimentDistributionSelectedIndex == TEN) {
						spamchunk = 1;
						nonspamchunk = 9;
					} else if (experimentDistributionSelectedIndex == TWENTYFIVE) {
						spamchunk = 5;
						nonspamchunk = 15;

					} else if (experimentDistributionSelectedIndex == THIRTY) {
						spamchunk = 3;
						nonspamchunk = 7;

					} else if (experimentDistributionSelectedIndex == FIFTY) {
						spamchunk = 5;
						nonspamchunk = 5;

					} else if (experimentDistributionSelectedIndex == SIXTY) {
						spamchunk = 6;
						nonspamchunk = 4;

					} else if (experimentDistributionSelectedIndex == SEVENTYFIVE) {
						spamchunk = 15;
						nonspamchunk = 5;

					} else if (experimentDistributionSelectedIndex == NINTY) {
						spamchunk = 9;
						nonspamchunk = 1;
					}

					for (; (spamOffset < (spamlist.length - spamchunk))
							&& nonspamOffset < (nonspamlist.length - nonspamchunk);) {

						// for we write out spam examples
						for (int j = 0; j < spamchunk; j++) {
							int target = spamlist[spamOffset++];
							if (!uniqueMailrefs.containsKey(data[target])) {
								if (data[target][0].length() > 0
										&& data[target][1].length() > 0)
									bufw.write(data[target][0] + "\t"
											+ data[target][1] + "\n");
								//                         bufw.write(data[target][0] + "\t" + data[target][1] + "\t" + data[target][2] + "\n");
							}
						}

						// then write out non spam examples
						for (int j = 0; j < nonspamchunk; j++) {
							int target = nonspamlist[nonspamOffset++];
							if (!uniqueMailrefs.containsKey(data[target])) {
								if (data[target][0].length() > 0
										&& data[target][1].length() > 0)
									bufw.write(data[target][0] + "\t"
											+ data[target][1] + "\n");
								//                         bufw.write(data[target][0] + "\t" + data[target][1] + "\t" + data[target][2] + "\n");
							}
						}
					}

				}

			}

			bufw.close();
			JOptionPane.showMessageDialog(Finance.this, "Done!");
		} catch (IOException io) {
			System.out.println(io);
		}

		/*German: do not convert automatically to bipartite. let user do it.
		 if(NOFILE){
		 try
		 {
		 boolean PAJEK=false;
		 txt2pajek pj = new txt2pajek();

		 //			String outFile;
		 String[] prefix = filename.split(".txt");//shlomo - was fileConvertNameNetwork
		 String outfile;
		 if(PAJEK){
		 outfile= prefix[0] + ".net";
		 }else{
		 outfile= prefix[0] + ".bp";
		 }

		 Integer resultConvert = pj.convert(filename,PAJEK,outfile);//fileConvertNameNetwork,PAJEK,fileNameNetwork);

		 // German: commented to avoid error during conversion of emails.
		 //if(resultConvert==1){
		 //	graph=calculateStatistics(fileNameNetwork,"BIPARTITE2UNIMODALMAXWEAKCOMP"); // automatically calculate statistics and load graph. It selects Pajek format.
		 //	JOptionPane.showMessageDialog(Finance.this, "Statistics calculated only for Unimodal-max. component version. For other versions, give a filename, create files, and select graph format in next panel.");
		 //} else{
		 //	JOptionPane.showMessageDialog(Finance.this, "You may have problems in the conversion. Please check the files.");
		 //}
		 }

		 catch(FileNotFoundException e)
		 {
		 System.out.println("Problem opening files.");
		 }
		 catch(IOException e)
		 {
		 System.out.println("Error reading from temp1.txt.");
		 }
		 }*/
		return filename;
	}

	// start
	// end

}
