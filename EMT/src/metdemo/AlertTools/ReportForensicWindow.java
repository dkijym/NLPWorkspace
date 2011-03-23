/**
 * Alert and Report Window
 * Alert will be dumped here, along with capacity to generate forensic and exploratory reports.
 */

package metdemo.AlertTools;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import javax.swing.JFileChooser;

import chapman.graphics.JPlot2D;

import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.AlertTableModel;
import metdemo.Tables.AlternateColorTableRowsRenderer;
import metdemo.Tables.SortTableModel;
import metdemo.Tables.TableSorter;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.DistanceCompute;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.NonModalMessage;
import metdemo.Tools.Utils;
import metdemo.dataStructures.UserDistance;
import metdemo.dataStructures.userComm;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** class to display the alerts in a table fashion */

public class ReportForensicWindow extends JScrollPane implements MouseListener {

	private JTable m_alertTable, m_emailTable, m_VIPTable;

	private DefaultTableModel dtm;

	private AlertTableModel m_alertTableModel;
private SortTableModel VIPSortermodel;
	private boolean m_silent;

	private JFileChooser chooser;

	private File gd = null;

	private JTextField current_file;
private JButton VIP_Diff_msgText;
	private JPanel panel;

	private JScrollPane jspMessages;

	private GridBagConstraints constraints;

	private GridBagLayout gridbag;

	private TableSorter sorter, emailTableSorter;

	private EMTDatabaseConnection m_emtdb;

	private winGui m_winGui;
double [] profileHistogram;
	static final int COLUMN_EMAIL = 0;
static final int COLUMN_EMAILCOUNT = 1;
	static final int COLUMN_SENDER = 1;

	static final int COLUMN_RCPT = 2;
static final int COLUMN_AVGTIME = 2;
	static final int COLUMN_RELATIONSHIP = 3;
	static final int COLUMN_VIP = 3;
	private JCheckBox displayUsageView, displayVIP, displayVIPUsageDiff;

	//private int displayUsageNumber;
	private int lastViewRow = -1;

	//static BusyWindow BWMAIN = new BusyWindow("Busy", "Working....",false);

	private JPlot2D userPlot, vipPlot;


 private String currentUser,currentVIP;
	/**
	 * Constructor which sets up the report window
	 *  
	 */
	public ReportForensicWindow(EMTDatabaseConnection emtd, winGui wg) {

		m_winGui = wg;
		m_emtdb = emtd;
		m_alertTableModel = new AlertTableModel();
		sorter = new TableSorter(m_alertTableModel);
		m_alertTableModel.addTableModelListener(sorter);
		m_alertTable = new JTable(sorter);
		sorter.addMouseListenerToHeaderInTable(m_alertTable);
		//added by shlomo
		current_file = new JTextField(25);
		panel = new JPanel();
		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel.setLayout(gridbag);
		constraints.gridx = 0;
		constraints.gridy = 0;
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1, 1, 1, 1);
		JLabel filename = new JLabel("File:");
		gridbag.setConstraints(filename, constraints);
		panel.add(filename);

		constraints.gridx = 1;
		constraints.gridwidth = 3;
		gridbag.setConstraints(current_file, constraints);
		panel.add(current_file);

		//constraints.gridx = 4;
		//EMTHelp helpbutton = new EMTHelp(EMTHelp.REPORTS);
		//gridbag.setConstraints(helpbutton, constraints);
		//panel.add(helpbutton);

		//to choose file

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileHidingEnabled(false);
		chooser.setMultiSelectionEnabled(false);

		JButton browse = new JButton("Choose file");
		browse.setToolTipText("Select file");
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (gd != null)
					chooser.setCurrentDirectory(gd);

				int returnVal = chooser
						.showOpenDialog(ReportForensicWindow.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					//legalfile=true;
					current_file.setText(chooser.getSelectedFile().getPath());
					gd = chooser.getCurrentDirectory();

					//This is where a real application would open the file.

				} else {
					//user cancelled
				}

			}
		});

		constraints.gridy = 0;
		constraints.gridx = 4;

		constraints.gridwidth = 1;
		gridbag.setConstraints(browse, constraints);
		panel.add(browse);

		JButton loadbutton = new JButton("Load");
		loadbutton.setToolTipText("load old report file");
		loadbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				LoadReport();
			}

		});

		constraints.gridy = 1;
		constraints.gridx = 1;
		gridbag.setConstraints(loadbutton, constraints);
		panel.add(loadbutton);

		JButton savebutton = new JButton("Save");
		savebutton.setToolTipText("save curent report window");
		savebutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				SaveReport();
			}

		});

		constraints.gridx = 2;
		gridbag.setConstraints(savebutton, constraints);
		panel.add(savebutton);

		JButton create = new JButton("Create Alert/Report");
		create.setToolTipText("add alert or note into table");
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAlert("", "");
			}
		});

		constraints.gridx = 3;
		gridbag.setConstraints(create, constraints);
		panel.add(create);

		//next row
		constraints.gridy = 1;
		constraints.gridx = 0;

		JButton erase = new JButton("Clear Table");
		erase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}

		});
		erase.setBackground(Color.yellow);
		gridbag.setConstraints(erase, constraints);
		panel.add(erase);
		constraints.gridy = 1;
		constraints.gridx = 4;
		JButton export = new JButton("Export Table");
		export.setToolTipText("Convert into text file (comma deliminated)");
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String name = (String) JOptionPane.showInputDialog(
						ReportForensicWindow.this,
						"Enter Name of text file to copy table to.",
						"Filename", JOptionPane.PLAIN_MESSAGE, null, null, "");
				if (name != null) {
					try {
						PrintWriter pw = new PrintWriter(new FileOutputStream(
								name));
						int n = m_alertTable.getRowCount();
						for (int i = 0; i < n; i++) {
							Alert A = m_alertTableModel.getAlert(i);
							pw.println(A.getLevel() + "," + A.getBrief() + ","
									+ A.getDetailed());
						}

						pw.flush();
						pw.close();

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								ReportForensicWindow.this,
								"Error in exporting: " + name + "\nmsg: " + e1,
								"Problem saving File",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		});
		;
		gridbag.setConstraints(export, constraints);
		panel.add(export);

		//  gridbag.setConstraints(m_alertTable, constraints);
		m_alertTable.getColumnModel().getColumn(0).setMaxWidth(50);
		m_alertTable.getColumnModel().getColumn(0).setCellRenderer(
				new AlertLevelCellRenderer());
		m_alertTable.addMouseListener(this);

		//m_alertTable.setPreferredScrollableViewportSize(new Dimension(500,
		// 70));

		jspMessages = new JScrollPane();
		m_alertTable
				.setPreferredScrollableViewportSize(new Dimension(400, 100));
		jspMessages.setViewportView(m_alertTable);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 5;
		//constraints.weightx = 1;
		gridbag.setConstraints(jspMessages, constraints);
		panel.add(jspMessages);
		//seperator
		JTextPane separator = new JTextPane();
		separator.setEditable(false);
		separator.setBackground(Color.orange);
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(separator, constraints);
		panel.add(separator);

		EMTHelp emth = new EMTHelp(EMTHelp.REPORTS);
		constraints.gridx = 4;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(emth, constraints);
		panel.add(emth);
		
		
		//TODO:

		JButton refreshEmailInfo = new JButton("Refresh Email Info");
		refreshEmailInfo
				.setToolTipText("Double click on email name to set report");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(refreshEmailInfo, constraints);
		panel.add(refreshEmailInfo);
		refreshEmailInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshEmailInfo();
			}
		});

		//create a jtable with email name, #inbound email , #outbound email
		String tablecolumn[] = new String[] { "Email", "Outbound", "Inbound",
				"Relationship" };
		DefaultTableCellRenderer dtcr = new AlternateColorTableRowsRenderer();

		dtm = new DefaultTableModel(tablecolumn, 0) {

			public Class getColumnClass(final int column) {
				if (column == COLUMN_EMAIL) {
					return String.class;
				} else if (column == COLUMN_RELATIONSHIP) {
					return Double.class;
				}
				return Integer.class;
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}

		};

		emailTableSorter = new TableSorter(dtm);
		dtm.addTableModelListener(emailTableSorter);
		m_emailTable = new JTable(emailTableSorter) {
			protected void processKeyEvent(KeyEvent evt) {
				//int column = table.getSelectedColumn();
				//int row = table.getSelectedRow();
				if (evt.getKeyCode() == KeyEvent.VK_UP
						|| evt.getKeyCode() == KeyEvent.VK_DOWN)
					updateUserView(m_emailTable.getSelectedRow());

				super.processKeyEvent(evt);
			}
		};
		emailTableSorter.addMouseListenerToHeaderInTable(m_emailTable);
		m_emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_emailTable.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public final void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int row = m_emailTable.rowAtPoint(point);
				if (m_emailTable.isRowSelected(row))//&& m_showKnobs)
				{
					updateUserView(row);
				}

				if (e.getClickCount() == 2) {

					String name = (String) emailTableSorter.getValueAt(row,
							COLUMN_EMAIL);
					Integer senderCount = (Integer) emailTableSorter
							.getValueAt(row, COLUMN_SENDER);
					Integer rcptCount = (Integer) emailTableSorter.getValueAt(
							row, COLUMN_RCPT);

					//offer to create alert based on this email:
					createAlert("Interesting Email", "Email Account: " + name
							+ "\n" + "Sender Count: " + senderCount
							+ "\nRcpt Count: " + rcptCount + "\n");

				}
			}
		});

		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 5;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		TableColumn getaColumn = m_emailTable.getColumn("Inbound");
		getaColumn.setPreferredWidth(80);
		getaColumn.setCellRenderer(dtcr);
		getaColumn = m_emailTable.getColumn("Outbound");
		getaColumn.setPreferredWidth(80);
		getaColumn.setCellRenderer(dtcr);
		getaColumn = m_emailTable.getColumn("Email");
		getaColumn.setCellRenderer(dtcr);
		getaColumn = m_emailTable.getColumn("Relationship");
		getaColumn.setCellRenderer(dtcr);
		getaColumn.setPreferredWidth(100);

		JScrollPane rollingTable = new JScrollPane();
		m_emailTable
				.setPreferredScrollableViewportSize(new Dimension(300, 100));
		rollingTable.setViewportView(m_emailTable);

		gridbag.setConstraints(rollingTable, constraints);
		panel.add(rollingTable);

		JButton buttonSeeAllEmails = new JButton("View All Msgs");
		buttonSeeAllEmails
				.setToolTipText("Show all of selected users messages in message window, if VIP user is selected both are shown (ie communication)");
		buttonSeeAllEmails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: switch to message and view all user emails.

				if (currentUser.trim().length() < 1){//m_emailTable.getSelectedRowCount() != 1) {

					JOptionPane
							.showMessageDialog(ReportForensicWindow.this,
									"Please select a single user, click refresh to fill list");
					return;
				}
				//int row = m_emailTable.getSelectedRow();
				//String emailname = (String) emailTableSorter.getValueAt(row,
				//		COLUMN_EMAIL);
				if(currentVIP.trim().length()<1)
				m_winGui.setMessageView(currentUser);//emailname);
				else{
					String []groups = new String[2];
					groups[0] = currentUser;
					groups[1] = currentVIP;
					m_winGui.setMessageViewGroup(groups);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(buttonSeeAllEmails, constraints);
		panel.add(buttonSeeAllEmails);

		JButton button_at_user_top = new JButton("Add to top and Select");
		button_at_user_top
				.setToolTipText("Add selected user to top level drop down list");
		button_at_user_top.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (m_emailTable.getSelectedRowCount() != 1) {

					JOptionPane.showMessageDialog(ReportForensicWindow.this,
							"Please select one account");
					return;
				}
				int row = m_emailTable.getSelectedRow();
				String emailname = (String) emailTableSorter.getValueAt(row,
						COLUMN_EMAIL);
				m_winGui.setUserName(emailname);
			}
		});
		constraints.gridx = 2;
		constraints.gridy = 7;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(button_at_user_top, constraints);
		panel.add(button_at_user_top);

		//String emailname =
		// (String)emailTableSorter.getValueAt(row,COLUMN_EMAIL);
		JButton button_calculate_similarity = new JButton(
				"Show Usage similarity");
		button_calculate_similarity
				.setToolTipText("Relate all users to selected");
		button_calculate_similarity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (m_emailTable.getSelectedRowCount() != 1) {

					JOptionPane.showMessageDialog(ReportForensicWindow.this,
							"Please select one account");
					return;
				}
				//fetch the similair users list given the other users
				String emailname = (String) emailTableSorter.getValueAt(
						m_emailTable.getSelectedRow(), COLUMN_EMAIL);

				//check size of table first
				int size = emailTableSorter.getRowCount();
				System.out.println("number rows: " + size);
				//grab all user names
				String names[] = new String[size];
				for (int i = 0; i < size; i++) {
					names[i] = (String) emailTableSorter.getValueAt(i,
							COLUMN_EMAIL);
				}
				try {
					UserDistance[] simScore = m_winGui.getSimilarityScores(
							emailname, names);
					System.out.println("back from sim");

					HashMap<String,Double> scoremap = new HashMap<String,Double>();
					for (int i = 0; i < simScore.length; i++) {//Iterator itera
															   // =
															   // simScore.iterator();itera.hasNext();){
						UserDistance udist = simScore[i];// (UserDistance)itera.next();
						scoremap.put(udist.getName(), new Double(udist
								.getDistNum()));
					}

					for (int i = 0; i < size; i++) {
						if (scoremap.containsKey(emailTableSorter.getValueAt(i,
								COLUMN_EMAIL))) {
							emailTableSorter.setValueAt(scoremap
									.get(emailTableSorter.getValueAt(i,
											COLUMN_EMAIL)), i,
									COLUMN_RELATIONSHIP);
						} else {
							emailTableSorter.setValueAt(new Double(-1), i,
									COLUMN_RELATIONSHIP);
						}
					}

				} catch (SQLException se) {
					System.out.println(se);
				}

			}
		});
		constraints.gridx = 4;
		constraints.gridy = 7;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(button_calculate_similarity, constraints);
		panel.add(button_calculate_similarity);

		displayVIP = new JCheckBox("Get VIP List for user", true);
		displayVIP.setToolTipText("Bring up the VIP list to selected user");
		constraints.gridx = 1;
		constraints.gridy = 8;
		constraints.gridwidth = 1;
		gridbag.setConstraints(displayVIP, constraints);
		panel.add(displayVIP);

		displayVIPUsageDiff = new JCheckBox("Calculate VIP's difference",true);
		displayVIPUsageDiff.setToolTipText("If the VIP is chosen calculate usage difference between normal user communcation and specific vip user");
		constraints.gridx = 2;
		constraints.gridy = 8;
		constraints.gridwidth = 1;
		gridbag.setConstraints(displayVIPUsageDiff, constraints);
		panel.add(displayVIPUsageDiff);
		
		
		VIP_Diff_msgText = new JButton("                ");
		
		constraints.gridx = 3;
		constraints.gridy = 8;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(VIP_Diff_msgText, constraints);
		panel.add(VIP_Diff_msgText);
		VIP_Diff_msgText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(VIP_Diff_msgText.getText().trim().length()<2)
				{
					return;
				}
				
				String msg = "VIP distance between \nUser: " + currentUser +"\nVIP: " +currentVIP+"\n";
				msg+=VIP_Diff_msgText.getText();
				createAlert("Vip Difference Alert", msg);
			}
		});
		VIP_Diff_msgText.setToolTipText("Click to create alert");
		
		displayUsageView = new JCheckBox("Display Usage Profile", true);

		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 8;
		constraints.gridwidth = 1;
		gridbag.setConstraints(displayUsageView, constraints);
		panel.add(displayUsageView);

		//empty usage one
		constraints.gridx = 0;
		constraints.gridy = 10;
		constraints.gridwidth = 2;
		userPlot = new JPlot2D();
		userPlot.setPreferredSize(new Dimension(250, 200));
		gridbag.setConstraints(userPlot, constraints);
		panel.add(userPlot);

		vipPlot = new JPlot2D();
		constraints.gridx = 2;
		constraints.gridy = 10;
		constraints.gridwidth = 2;
		vipPlot.setPreferredSize(new Dimension(250, 200));
		gridbag.setConstraints(vipPlot, constraints);
		panel.add(vipPlot);

		//VIP TABLE
		VIPSortermodel = new SortTableModel();

		VIPSortermodel.addColumn("account");
		VIPSortermodel.addColumn("# email");
		VIPSortermodel.addColumn("avg time (min)");
		VIPSortermodel.addColumn("VIP rank");
		m_VIPTable = new JTable(VIPSortermodel){
			protected void processKeyEvent(KeyEvent evt) {
				//int column = table.getSelectedColumn();
				//int row = table.getSelectedRow();
				if (evt.getKeyCode() == KeyEvent.VK_UP
						|| evt.getKeyCode() == KeyEvent.VK_DOWN)
					updateVIPDIFFView(m_VIPTable.getSelectedRow());

				super.processKeyEvent(evt);
			}
		};
		VIPSortermodel.addMouseListenerToHeaderInTable(m_VIPTable);

		m_VIPTable.setRowSelectionAllowed(true);
		m_VIPTable.setColumnSelectionAllowed(false);
		m_VIPTable.getTableHeader().setReorderingAllowed(false);
		m_VIPTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_VIPTable.getColumnModel().getColumn(1).setMaxWidth(50);
m_VIPTable.addMouseListener(new MouseListener() {
	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public final void mouseClicked(MouseEvent e) {
		Point point = e.getPoint();
		int row = m_VIPTable.rowAtPoint(point);
		if (m_VIPTable.isRowSelected(row))//&& m_showKnobs)
		{
			updateVIPDIFFView(row);
		}

		if (e.getClickCount() == 2) {
//TODO:
			/*String name = (String) emailTableSorter.getValueAt(row,
					COLUMN_EMAIL);
			Integer senderCount = (Integer) emailTableSorter
					.getValueAt(row, COLUMN_SENDER);
			Integer rcptCount = (Integer) emailTableSorter.getValueAt(
					row, COLUMN_RCPT);

			//offer to create alert based on this email:
			createAlert("Interesting Email", "Email Account: " + name
					+ "\n" + "Sender Count: " + senderCount
					+ "\nRcpt Count: " + rcptCount + "\n");
*/
		}
	}
});
		JScrollPane sPane = new JScrollPane(m_VIPTable);
		sPane.setPreferredSize(new Dimension(300, 260));
		constraints.gridx = 4;
		constraints.gridy = 10;
		constraints.gridwidth = 1;
		gridbag.setConstraints(sPane, constraints);
		panel.add(sPane);

		/*
		 * buttonUsageProfile.addActionListener(new ActionListener() { public
		 * void actionPerformed(ActionEvent e) {
		 * if(m_emailTable.getSelectedRowCount()!=1){
		 * 
		 * JOptionPane.showMessageDialog(ReportForensicWindow.this,"Please
		 * select one account"); return; } int row =
		 * m_emailTable.getSelectedRow(); String emailname =
		 * (String)emailTableSorter.getValueAt(row,COLUMN_EMAIL);
		 * JOptionPane.showMessageDialog(ReportForensicWindow.this,m_winGui.getUsageProfile(emailname));
		 * }});
		 */
		//panel.setPreferredSize(new Dimension(530, 70));
		setViewportView(panel);

		m_silent = false;

	}

	
	
	public synchronized void updateVIPDIFFView(int row){
		
		currentVIP = (String)VIPSortermodel.getValueAt(row,
				COLUMN_EMAIL);
		BusyWindow BWMAIN = new BusyWindow("Busy", "Working....",false);

		BWMAIN.setVisible(true);
		if(displayVIPUsageDiff.isSelected()){
		
			//System.out.println("getting for " + currentUser +" to " +currentVIP);
			vipPlot.removeAll();
			double[] userpro = m_winGui.getUsageProfile(currentUser,currentVIP);
			vipPlot.setPlotType(JPlot2D.BAR);
			vipPlot.setBackgroundColor(Color.white);
			vipPlot.setFillColor(Color.blue);
			vipPlot.setXLabel("time");
			vipPlot.setYLabel("avg email sent");
			vipPlot.setTitle("User histogram");
			vipPlot.setXScale(Utils.s1by24);
			vipPlot.addCurve(Utils.x_24, userpro);
			
			vipPlot.repaint();
			
			//now to calculate difference
			
			
			VIP_Diff_msgText.setText("Distance = "+DistanceCompute.getDistance(profileHistogram, userpro, null, ""));
			
			
			
			
			
		}
		BWMAIN.setVisible(false);
		
	}
	
	
	/**
	 * updates the view of the user histogram and vip table
	 * if either is selected.
	 * @param row
	 */
	public synchronized void updateUserView(int row) {

		if (row == lastViewRow) {
			return;
		}
		BusyWindow BWMAIN = new BusyWindow("Busy", "Working....",false);

		BWMAIN.setVisible(true);
		lastViewRow = row;
		//remove user view
		currentUser = (String) emailTableSorter.getValueAt(row,
				COLUMN_EMAIL);
		currentVIP="";
		profileHistogram = m_winGui.getUsageProfile(currentUser);
		if (displayUsageView.isSelected()) {
			userPlot.removeAll();
			
			userPlot.setPlotType(JPlot2D.BAR);
			userPlot.setBackgroundColor(Color.white);
			userPlot.setFillColor(Color.blue);
			userPlot.setXLabel("time");
			userPlot.setYLabel("avg email sent");
			userPlot.setTitle("User histogram");
			userPlot.setXScale(Utils.s1by24);
			if(profileHistogram!=null){
			userPlot.addCurve(Utils.x_24, profileHistogram);
			}
			userPlot.repaint();
		}
		//now to see if we need to do communciation list
		if (displayVIP.isSelected()) {
			try {
				userComm[] list = m_winGui.getCommunicationList(currentUser, 6);
				
				SortTableModel srt = (SortTableModel) m_VIPTable.getModel();
				srt.setRowCount(0);
				if(list!=null)
				for (int i = 0; i < list.length; i++) {

					Object[] tt = new Object[4];
					userComm u = list[i];//(userComm)curResult.elementAt(i);

					if (u.time == Double.MAX_VALUE)
						continue;

					if (u.vipValue == -1)
						continue;

					tt[COLUMN_EMAILCOUNT] = (new Integer(u.number));
					tt[COLUMN_AVGTIME] = (new Double(u.time));

					tt[COLUMN_VIP] = (new Double(u.vipValue));

					tt[COLUMN_EMAIL] = u.acct;

					srt.addRow(tt);
				}

			} catch (SQLException s) {
				s.printStackTrace();
			}
		}

		setViewportView(panel);

		BWMAIN.setVisible(false);
	}

	public void createAlert(String titlem, String msg) {

		String now = new Date(System.currentTimeMillis()).toString();

		String title = (String) JOptionPane
				.showInputDialog(ReportForensicWindow.this,
						"Please choose a short title for your alert:\n",
						"Title of Alert", JOptionPane.PLAIN_MESSAGE, null,
						null, titlem);

		JTextPane jp = new JTextPane();
		jp.setText(msg);
		JScrollPane areaScrollPane = new JScrollPane(jp);
		areaScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(300, 300));

		jp.requestFocus();
		String body = new String();
		int choice = JOptionPane.showConfirmDialog(ReportForensicWindow.this,
				areaScrollPane, "Edit body of message here",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (choice == JOptionPane.NO_OPTION) {
			return;
		}
		body = jp.getText();

		m_alertTableModel.addAlert(new Alert(2, title + " (" + now + ")",
				"Custom alert added at " + now + "\n" + body));
	}

	/**
	 * Refresh the email table with email names and counts of sending and
	 * rcpting
	 *  
	 */
	private void refreshEmailInfo() {
		//		will map mailrefs to flowcounts
		currentUser = "";
		currentVIP = "";
		
		HashMap<String,flowCount> emailFlowCounts = new HashMap<String,flowCount>();
		//TODO:
		BusyWindow BW = new BusyWindow("Fetching Email", "Processing",true);
		//ask the user if to limit the number returned
		String choices[] = new String[] { "All Accounts",
				"At Least X for sender", "At Least X for rcpt",
				"At Least X both ways" };
		boolean limitSender = false;
		boolean limitRcpt = false;
		int havingAtLeast = 0;

		int choo = JOptionPane.showOptionDialog(ReportForensicWindow.this,
				"Choose which accounts are displayed", "Update Email Accounts",
				JOptionPane.CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, choices, choices[0]);
		dtm.setRowCount(0);

		if (choo == 0) {
		} else if (choo == 1) {
			limitSender = true;
		} else if (choo == 2) {
			limitRcpt = true;
		} else {
			limitRcpt = true;
			limitSender = true;
		}

		try {
			if (limitRcpt || limitSender) {
				String backvalue = ((String) JOptionPane.showInputDialog(this,
						"Type in min number of messages for each account:\n",
						"Give limit", JOptionPane.PLAIN_MESSAGE, null, null,
						" 50"));
				
				if(backvalue==null){
					return;
				}
				
				havingAtLeast = Integer.parseInt(backvalue.trim());
			}
		} catch (NumberFormatException nf) {
			System.out.println("caught nf...will set default limits (50)");
			havingAtLeast = 50;
		}
		//sql: select sender,count(mailref) frome email group by sender having
		// count(mailref)> XXXX
		BW.setVisible(true);
		try {
			String[][] ps_sender;
			String[][] ps_rcpt;
			if (limitSender) {
				ps_sender = m_emtdb
						.getSQLData("select sender,count(mailref) from email group by sender having count(mailref) > "
								+ havingAtLeast);
				//ps_sender.setInt(1, havingAtLeast);
			} else {
				ps_sender = m_emtdb
						.getSQLData("select sender,count(mailref) from email group by sender");

			}

			if (limitRcpt) {
				ps_rcpt = m_emtdb
						.getSQLData("select rcpt,count(mailref) from email group by rcpt having count(mailref) > "
								+ havingAtLeast);
				//ps_rcpt.setInt(1, havingAtLeast);
			} else {
				ps_rcpt = m_emtdb
						.getSQLData("select rcpt,count(mailref) from email group by rcpt");

			}

			//now to cycle through each list
			//sender first
			int count;

			for (int i = 0; i < ps_sender.length; i++) {
				count = Integer.parseInt(ps_sender[i][1]);
				emailFlowCounts.put(ps_sender[i][0], new flowCount(count, 0));
			}
			//now for rcpt

			for (int i = 0; i < ps_rcpt.length; i++) {

				count = Integer.parseInt(ps_rcpt[i][1]);
				if (emailFlowCounts.containsKey(ps_rcpt[i][0])) {
					flowCount fc = (flowCount) emailFlowCounts
							.get(ps_rcpt[i][0]);
					fc.setDest(count);
					emailFlowCounts.put(ps_rcpt[i][0], fc);
				} else {
					emailFlowCounts.put(ps_rcpt[i][0], new flowCount(0, count));
				}
			}
			int num = 0;
			System.out.println("step 1");
			BW.progress(0, emailFlowCounts.size());
			//now to cycle through and update the table
			for (Iterator it = emailFlowCounts.keySet().iterator(); it
					.hasNext();) {
				String key = (String) it.next();
				flowCount fc = (flowCount) emailFlowCounts.get(key);
				BW.progress(num++);
				//lets see if we have enough to include in the table
				if (limitSender && fc.getSourceCount() < havingAtLeast) {
					continue;
				}
				if (limitRcpt && fc.getDestCount() < havingAtLeast) {
					continue;
				}
				Object line[] = new Object[4];
				line[COLUMN_EMAIL] = key;
				line[COLUMN_SENDER] = new Integer(fc.getSourceCount());
				line[COLUMN_RCPT] = new Integer(fc.getDestCount());
				line[COLUMN_RELATIONSHIP] = new Double(-1);

				//I guess we can add to it
				dtm.addRow(line);
			}

		} catch (SQLException sle) {
			sle.printStackTrace();
		}
		BW.setVisible(false);

	}

	public void alert(int level, String brief, String detailed) {
		m_alertTableModel.addAlert(new Alert(level, brief, detailed));
		if (!m_silent) {
			NonModalMessage.showMessageDialog(this, "Alert", brief);
		}
	}

	/** save the table */
	public void SaveReport() {
		if (current_file.getText().length() < 1) {
			JOptionPane
					.showMessageDialog(
							this,
							"Please choose a valid file using the browse button or manually type it in.",
							"Empty File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(current_file.getText()
					.trim());
			GZIPOutputStream gout = new GZIPOutputStream(out);
			ObjectOutputStream s = new ObjectOutputStream(gout);

			int n = m_alertTableModel.getRowCount();

			s.writeInt(n);

			for (int i = 0; i < n; i++)
				s.writeObject(m_alertTableModel.getAlert(i));

			s.close();
			JOptionPane.showMessageDialog(this, "Done saving: "
					+ current_file.getText());

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving: "
					+ current_file.getText() + "\nmsg: " + e,
					"Problem saving File", JOptionPane.ERROR_MESSAGE);
		}

	}

	/** Load information into the table */
	public void LoadReport() {
		if (current_file.getText().length() < 1) {
			JOptionPane
					.showMessageDialog(
							this,
							"Please choose a valid file using the browse button or manually type it in.",
							"Missing File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			FileInputStream in = new FileInputStream(current_file.getText()
					.trim());
			GZIPInputStream gin = new GZIPInputStream(in);
			ObjectInputStream s = new ObjectInputStream(gin);
			int n = s.readInt();
			for (int i = 0; i < n; i++)
				m_alertTableModel.addAlert((Alert) s.readObject());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Following error occured while accessing : "
							+ current_file.getText() + "\nError:" + e,
					"File Problem", JOptionPane.ERROR_MESSAGE);
		}

	}//end load reports

	/**
	 * clear the alert table
	 */
	public void clear() {
		/*
		 * panel.remove(jspMessages);
		 * 
		 * 
		 * constraints.gridx = 0; constraints.gridy = 3; constraints.gridwidth =
		 * 5; constraints.weightx = 1; // actually just replace it with a new
		 * one
		 * 
		 * m_tableModel = new AlertTableModel(); sorter = new
		 * TableSorter(m_tableModel);
		 * m_tableModel.addTableModelListener(sorter); m_alertTable = new
		 * JTable(sorter); sorter.addMouseListenerToHeaderInTable(m_alertTable);
		 * 
		 * m_alertTable.getColumnModel().getColumn(0).setMaxWidth(50);
		 * m_alertTable.getColumnModel().getColumn(0).setCellRenderer(new
		 * AlertLevelCellRenderer()); m_alertTable.addMouseListener(this);
		 * 
		 * jspMessages = new JScrollPane();
		 * jspMessages.setViewportView(m_alertTable);
		 * 
		 * gridbag.setConstraints(jspMessages, constraints);
		 * panel.add(jspMessages); panel.setPreferredSize(new Dimension(530,
		 * 70)); setViewportView(panel);
		 * 
		 * panel.repaint();
		 */
		m_alertTableModel.clearTable();
		m_alertTable.repaint();
		jspMessages.validate();
	}

	public void setSilent(boolean silent) {
		m_silent = silent;
	}

	// required for MouseListener interface
	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

		Point p = e.getPoint();
		int row = m_alertTable.rowAtPoint(p);

		if (e.getClickCount() == 2) {

			Alert alert = new Alert(((Integer) sorter.getValueAt(row, 0))
					.intValue(), (String) sorter.getValueAt(row, 1),
					(String) sorter.getValueAt(row, 2));
			//m_tableModel.getAlert(row);
			// get frame ancestor
			Component c = this;
			while ((c != null) && !(c instanceof Frame)) {
				c = c.getParent();
			}

			DetailDialog detail;
			if (c == null) {
				detail = new DetailDialog(null, false, alert.getDetailed());
			} else {
				detail = new DetailDialog((Frame) c, false, alert.getDetailed());
			}

			detail.setLocationRelativeTo(this);
			detail.setVisible(true);
		}
	}

	private class AlertLevelCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component comp = super.getTableCellRendererComponent(table, "",
					isSelected, hasFocus, row, column);

			int level = ((Integer) value).intValue();

			if (level == Alert.ALERTLEVEL_YELLOW) {
				comp.setBackground(Color.YELLOW);
			} else if (level == Alert.ALERTLEVEL_RED) {
				comp.setBackground(Color.RED);
			} else if (level == Alert.ALERTLEVEL_GREEN) {
				comp.setBackground(Color.GREEN);
			}

			return comp;
		}
	}

	private class flowCount {

		private int source_count;

		private int dest_count;

		public flowCount() {
			source_count = 0;
			dest_count = 0;
		}

		public flowCount(int n_in, int n_out) {
			source_count = n_in;
			dest_count = n_out;
		}

		public int getSourceCount() {
			return source_count;
		}

		public int getDestCount() {
			return dest_count;
		}

		public void setSource(int n) {
			source_count = n;
		}

		public void setDest(int o) {
			dest_count = o;
		}

	}
}

