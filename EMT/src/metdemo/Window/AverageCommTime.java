package metdemo.Window;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
//import javax.swing.table.*;
import javax.swing.event.*;

import metdemo.winGui;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.SortTableModel;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.CommunicateTime;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;
import metdemo.dataStructures.userComm;
import metdemo.dataStructures.userShortProfile;

//import java.io.*;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import chapman.graphics.*;



// TODO: Auto-generated Javadoc
/**
 * For the tab of average communication time between different users.
 *
 * @author Ke Wang 06/22/03
 */


public class AverageCommTime extends JScrollPane implements ListSelectionListener {

	//private ReportForensicWindow m_alerts;
	//    private static BusyWindow BW;
	/** The m_win gui. */
	private winGui m_winGui;
	//private EMTDatabaseConnection m_jdbcView;
	/** The CT. */
	private CommunicateTime CT;

	/** The cur result. */
	private userComm[] curResult;

	//Gui component
	//   private JRadioButton b1;
	//private JRadioButton b2, b3;
	//private ButtonGroup bg;
	//private JTextField userFreq;


	//    private JComboBox m_comboUsers;
	//private HashSet m_hsUsers; //store the users for comboUser
	/** The cur user. */
	private String curUser;

	/** The panel. */
	private JPanel panel;

	/** The period label. */
	private JLabel periodLabel;
	
	/** The min available date. */
	private String minAvailableDate; //from the db
	
	/** The max available date. */
	private String maxAvailableDate; //from the db
	
	/** The user_count. */
	private int user_count;
	
	/** The m_spinner model recent start. */
	private SpinnerDateModel m_spinnerModelRecentStart;
	
	/** The m_spinner model recent end. */
	private SpinnerDateModel m_spinnerModelRecentEnd;
	
	/** The run button. */
	private JButton runButton;//, helpButton1;

	/** The vip threshold. */
	private JTextField emailNum, vipThreshold;
	
	/** The m_slider. */
	private JSlider m_slider;
	
	/** The refresh button. */
	private JButton refreshButton;

	/** The m_histogram. */
	private JPlot2D m_legend, m_histogram;
	
	/** The h. */
	private JPanel h;
	
	/** The label1. */
	private JLabel label1;

	//final private String []COLUMN_NAMES = new String[]{"index","account","#
	// email","avg time (min)","VIP rank"};
	//private MessageTableModel model;

	/** The m_time table. */
	private JTable m_timeTable;
	
	/** The s pane. */
	private JScrollPane sPane;


	//for do copy stuff
	/** The clipboard. */
	final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	/**
	 * constructor.
	 *
	 * @param jdbcView the jdbc view
	 * @param alert the alert
	 * @param md5Renderer the md5 renderer
	 * @param winGui the win gui
	 */
	public AverageCommTime(final EMTDatabaseConnection jdbcView, final ReportForensicWindow alert,
			final md5CellRenderer md5Renderer, final winGui winGui) {

		if (jdbcView == null) {
			throw new NullPointerException();
		}

		//init the variables
		//m_jdbcView = jdbcView;
		//m_alerts = alert;
		m_winGui = winGui;

		CT = new CommunicateTime(jdbcView);
		//m_hsUsers = new HashSet();

		//arrange the layout
		panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		panel.setLayout(gridbag);
		/*
		 * //radio buttons to select accout JPanel panUser = new JPanel();
		 * panUser.setLayout(new FlowLayout());
		 * 
		 * b1 = new JRadioButton("all users", false); b2 = new
		 * JRadioButton("only senders", true); b3 = new JRadioButton("frequent
		 * users", false); bg = new ButtonGroup(); bg.add(b1); bg.add(b2);
		 * bg.add(b3); panUser.add(b1); panUser.add(b2); panUser.add(b3);
		 * 
		 * JLabel freqUserLabel = new JLabel("( #email threshold:");
		 * panUser.add(freqUserLabel); userFreq = new JTextField("8");
		 * userFreq.setColumns(5); userFreq.setText("20");
		 * panUser.add(userFreq); JLabel back = new JLabel(")");
		 * panUser.add(back);
		 * 
		 * constraints.gridx =0; constraints.gridy =0; constraints.gridwidth =2;
		 * constraints.insets = new Insets(0, 5, 0, 5);
		 * gridbag.setConstraints(panUser, constraints); panel.add(panUser);
		 */
		/*
		 * //comboBox for select user m_comboUsers = new JComboBox();
		 * m_comboUsers.setPreferredSize(new Dimension(250,25));
		 * m_comboUsers.setRenderer(md5Renderer);
		 * 
		 * JPanel panSelect = new JPanel(); panSelect.setLayout(new
		 * FlowLayout()); panSelect.add(new JLabel("Select Account:"));
		 * panSelect.add(m_comboUsers);
		 * 
		 * initCombo("sender");
		 * 
		 * constraints.gridx =0; constraints.gridy =1; constraints.gridwidth =1;
		 * constraints.insets = new Insets(0, 5, 0, 5);
		 * gridbag.setConstraints(panSelect, constraints); panel.add(panSelect);
		 */
		//label to show available data period
		periodLabel = new JLabel("");
		JPanel pp = new JPanel();
		//pp.setLayout(new FlowLayout());
		pp.add(periodLabel);

		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 0, 5);
		gridbag.setConstraints(pp, constraints);
		panel.add(pp);


		JPanel panRecent = new JPanel();
		panRecent.setLayout(new FlowLayout());
		panRecent.add(new JLabel("Select Compute Period:  "));
		panRecent.add(new JLabel("Start Date"));

		m_spinnerModelRecentStart = new SpinnerDateModel();
		Calendar recentcal = new GregorianCalendar();
		recentcal.set(2002, 7, 15);
		recentcal.add(Calendar.DATE, -7);
		m_spinnerModelRecentStart.setValue(recentcal.getTime());
		JSpinner spinnerRecentStart = new JSpinner(m_spinnerModelRecentStart);
		JSpinner.DateEditor editorRecentStart = new JSpinner.DateEditor(spinnerRecentStart, "MMM dd, yyyy   ");
		spinnerRecentStart.setEditor(editorRecentStart);

		panRecent.add(spinnerRecentStart);
		panRecent.add(new JLabel("   End Date"));

		m_spinnerModelRecentEnd = new SpinnerDateModel();
		Calendar recentcalend = new GregorianCalendar();
		recentcalend.set(2002, 7, 15);
		m_spinnerModelRecentEnd.setValue(recentcalend.getTime());
		JSpinner spinnerRecentEnd = new JSpinner(m_spinnerModelRecentEnd);
		JSpinner.DateEditor editorRecentEnd = new JSpinner.DateEditor(spinnerRecentEnd, "MMM dd, yyyy   ");
		spinnerRecentEnd.setEditor(editorRecentEnd);

		panRecent.add(spinnerRecentEnd);

		runButton = new JButton("Run");
		runButton.setBackground(Color.red);
		runButton.setToolTipText("press this button to show relative average comm time to the selected user");
		panRecent.add(runButton);

		//helpButton1 = new JButton("HELP");
		//helpButton1.setBackground(Color.orange);
		//helpButton1.setToolTipText("press this button to give you help
		// information about this tab");
		EMTHelp j = new EMTHelp(EMTHelp.AVERAGECOMMTIME);
		panRecent.add(j);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(0, 5, 0, 5);
		gridbag.setConstraints(panRecent, constraints);
		panel.add(panRecent);



		//histogram to show avg comm time
		label1 = new JLabel("relative average communication time for each account");
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(10, 5, 0, 5);
		gridbag.setConstraints(label1, constraints);
		panel.add(label1);


		//draw out the legend plot, which only shows the legend
		m_legend = new JPlot2D();
		m_legend.setBackgroundColor(Color.white);
		m_legend.setPreferredSize(new Dimension(340, 80));

		JPanel L = new JPanel();
		L.add(m_legend);
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 2;
		constraints.insets = new Insets(5, 2, 0, 2);
		gridbag.setConstraints(L, constraints);
		panel.add(L);

		drawLegend();


		JPanel threshP = new JPanel();
		threshP.setLayout(new FlowLayout());
		threshP.add(new JLabel("#email threshold: "));

		emailNum = new JTextField();
		emailNum.setColumns(5);
		emailNum.setText("1"); //default all accounts
		threshP.add(emailNum);

		threshP.add(new JLabel("  VIP rank threshold: "));
		vipThreshold = new JTextField();
		vipThreshold.setColumns(7);
		vipThreshold.setText("0.0"); //default all accounts
		threshP.add(vipThreshold);

		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 2, 0, 2);
		gridbag.setConstraints(threshP, constraints);
		panel.add(threshP);


		JPanel slideP = new JPanel();
		slideP.setLayout(new FlowLayout());
		slideP.add(new JLabel("emphasize on: "));

		m_slider = new JSlider(SwingConstants.HORIZONTAL, 0, 10, 5);
		m_slider.setPreferredSize(new Dimension(160, 40));
		m_slider.setMajorTickSpacing(5);
		m_slider.setMinorTickSpacing(1);
		m_slider.setPaintTicks(true);

		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("#email"));
		labelTable.put(new Integer(10), new JLabel("time"));
		m_slider.setLabelTable(labelTable);
		m_slider.setPaintLabels(true);

		slideP.add(m_slider);

		refreshButton = new JButton("Refresh Table");
		refreshButton.setToolTipText("Press to refresh the table and plot according to the thresholds");
		slideP.add(refreshButton);
		refreshButton.setEnabled(false);
		constraints.gridx = 1;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 2, 0, 2);
		gridbag.setConstraints(slideP, constraints);
		panel.add(slideP);

		//draw the plots and table
		m_histogram = new JPlot2D();
		m_histogram.setPlotType(JPlot2D.LINEAR);
		m_histogram.setBackgroundColor(Color.white);
		m_histogram.setFillColor(Color.blue);
		m_histogram.setPreferredSize(new Dimension(340, 320));

		h = new JPanel();
		h.add(m_histogram);
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(0, 2, 5, 2);
		gridbag.setConstraints(h, constraints);
		panel.add(h);


		SortTableModel model = new SortTableModel();
		model.addColumn("index");
		model.addColumn("account");
		model.addColumn("# email");
		model.addColumn("avg time (min)");
		model.addColumn("VIP rank");
		m_timeTable = new JTable(model);
		model.addMouseListenerToHeaderInTable(m_timeTable);



		m_timeTable.setRowSelectionAllowed(true);
		m_timeTable.setColumnSelectionAllowed(false);
		m_timeTable.getTableHeader().setReorderingAllowed(false);
		m_timeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_timeTable.setDefaultRenderer(model.getColumnClass(1), md5Renderer);
		m_timeTable.getColumnModel().getColumn(0).setMaxWidth(40);
		m_timeTable.getColumnModel().getColumn(2).setMaxWidth(50);


		sPane = new JScrollPane(m_timeTable);
		sPane.setPreferredSize(new Dimension(340, 320));

		constraints.gridx = 1;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 0, 5);
		gridbag.setConstraints(sPane, constraints);
		panel.add(sPane);


		//add the copy button
		JButton copy = new JButton("Copy Clipboard");
		copy.setToolTipText("To copy the table contents to system clipboard");
		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//String selection = jt.getSelectedText();
				int j = 0;
				SortTableModel smodel = (SortTableModel) m_timeTable.getModel();
				Vector select = smodel.getDataVector();
				String neat = new String();
				for (int i = 0; i < select.size(); i++) {
					Vector v = (Vector) select.elementAt(i);
					for (j = 0; j < v.size() - 1; j++)
						neat += v.elementAt(j).toString() + ",";
					neat += v.elementAt(j) + "\n";
				}
				StringSelection data = new StringSelection(neat);
				clipboard.setContents(data, data);
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 7;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		gridbag.setConstraints(copy, constraints);
		panel.add(copy);

		/*
		 * //add listerner b1.addActionListener(new ActionListener() { public
		 * void actionPerformed(ActionEvent evt) { try{
		 * 
		 * initCombo("all"); } catch (SQLException ex) {
		 * JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get
		 * data for account: " + ex); } } });
		 */

		/*
		 * //.addActionListener(new chooseUsers()); b2.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent evt) {
		 * try{
		 * 
		 * initCombo("sender"); } catch (SQLException ex) {
		 * JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get
		 * data for account: " + ex); } } });
		 * 
		 * //.addActionListener(new chooseUsers()); b3.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent evt) {
		 * try{
		 * 
		 * initCombo(userFreq.getText().trim()); //pass in threshold } catch
		 * (SQLException ex) {
		 * JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get
		 * data for account: " + ex); } } });
		 *  
		 */
		/*
		 * //.addActionListener(new chooseUsers());
		 * userFreq.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent evt) { try{
		 * initCombo(userFreq.getText().trim()); //pass in threshold } catch
		 * (SQLException ex) {
		 * JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get
		 * data for account: " + ex); } } });
		 * 
		 * 
		 * //addActionListener(new chooseUsers());
		 * 
		 * m_comboUsers.addActionListener(new showPeriod());
		 * if(m_comboUsers.getItemCount() >0) m_comboUsers.setSelectedIndex(0);
		 */
		runButton.addActionListener(new histogramListener());
		//helpButton1.addActionListener(new helpListener());
		refreshButton.addActionListener(new refreshListener());

		//add row click listener
		ListSelectionModel rowSM = m_timeTable.getSelectionModel();
		rowSM.addListSelectionListener(this);

		//show it!
		setViewportView(panel);

	}//end of constructor

	/*
	 * //for the radio button. switch between all user and sender and threshold
	 * class chooseUsers implements ActionListener{ public void
	 * actionPerformed(ActionEvent e){ try{ if(b1.isSelected())
	 * initCombo("all"); else if(b2.isSelected()) initCombo("sender"); else
	 * if(b3.isSelected()) initCombo(userFreq.getText().trim()); //pass in
	 * threshold } catch (SQLException ex) {
	 * JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get data
	 * for account: " + ex); } } }
	 */


	//listener for the m_comboUser
	//class showPeriod implements ActionListener{
	//	public void actionPerformed(ActionEvent e){
	/**
	 * Execute.
	 */
	public final void Execute() {
		//try{
		Object selected = m_winGui.getSelectedUser();
		//m_comboUsers.getSelectedItem();
		if (selected == null)
			return;
		curUser = (String) selected;
		getAvailablePeriod();
		adjustSelectionDate();

		// }
		//catch (SQLException ex)
		//	{
		//    JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to get
		// data for this account: " + ex);
		//	}
	}
	//   }


	//for run botton
	/**
	 * The listener interface for receiving histogram events.
	 * The class that is interested in processing a histogram
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addhistogramListener<code> method. When
	 * the histogram event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see histogramEvent
	 */
	class histogramListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			(new Thread(new Runnable() {
				public void run() {
					refreshButton.setEnabled(false);
					try {//TODO: blank out the table button
						BusyWindow bw = new BusyWindow("Working", "getting data",false);
						bw.setVisible(true);
						//compute and show the histograms
						m_histogram.removeAll();
						m_histogram.repaint();

						// clear table
						SortTableModel model = (SortTableModel) m_timeTable.getModel();
						//int length = model.getRowCount();
						//for(int i=length-1;i>=0;i--)
						//   model.removeRow(i);
						model.setRowCount(0);

						String startDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
						String endDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());

						getCommTime(curUser, startDate, endDate,m_slider.getValue());
						//m_winGui.setEnabled(true);

						panel.repaint();
						bw.setVisible(false);
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(AverageCommTime.this, "Failed to compute for one user: " + ex);
						m_winGui.setEnabled(true);
					}
					refreshButton.setEnabled(true);
				}


			})).start();

		}
	}

	/*
	 * //for helpButton1 class helpListener implements ActionListener{ public
	 * void actionPerformed(ActionEvent e){
	 * 
	 * String message = EMTHelp.getAverageCommTime(); String title = "Help";
	 * DetailDialog dialog = new DetailDialog(null, false, title, message);
	 * dialog.setLocationRelativeTo(AverageCommTime.this); dialog.show(); } }
	 */

	//for refreshButton
	/**
	 * The listener interface for receiving refresh events.
	 * The class that is interested in processing a refresh
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addrefreshListener<code> method. When
	 * the refresh event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see refreshEvent
	 */
	class refreshListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			(new Thread(new Runnable() {
				public void run() {
					try {
						//compute and show the histograms
						m_histogram.removeAll();
						m_histogram.repaint();


						// clear table
						SortTableModel model = (SortTableModel) m_timeTable.getModel();
						//int length = model.getRowCount();
						//for(int i=length-1;i>=0;i--)
						//   model.removeRow(i);
						model.setRowCount(0);

						int level = m_slider.getValue();
						//m_winGui.setEnabled(false);
						runButton.setEnabled(false);
						//b1.setEnabled(false);
						//b2.setEnabled(false);
						//b3.setEnabled(false);

						if (curResult == null)
							throw new SQLException("current Result is null");
						curResult = CommunicateTime.computeVIPRank(curResult, level);

						SortTableModel srt = (SortTableModel) m_timeTable.getModel();
						refreshTable(srt);
						refreshPlot(srt);

						//m_winGui.setEnabled(true);
						runButton.setEnabled(true);
						//b1.setEnabled(true);
						//b2.setEnabled(true);
						//b3.setEnabled(true);

						panel.repaint();
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(AverageCommTime.this, "Hit run first...table error: " + ex);
						m_winGui.setEnabled(true);
					}

				}
			})).start();

		}
	}


	//for the row select of m_timeTable
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public final void valueChanged(ListSelectionEvent event) {
		highlightPlot((SortTableModel) m_timeTable.getModel()); //highlight the position of that user in the curve
		// plot
	}


	/*
	 * select out all the users out and put into comboBox String arg should be
	 * "all" or others, which means only sender
	 */
	/*
	 * private void initCombo(final String arg) throws SQLException{
	 * 
	 * //populate String data[];
	 * 
	 * m_comboUsers.removeAllItems(); /* synchronized(m_jdbcUpdate){
	 * if(arg.equalsIgnoreCase("all")) m_jdbcUpdate.getSqlData("select distinct
	 * sender from email order by sender"); else m_jdbcUpdate.getSqlData("select
	 * distinct sender from email where folder like '%sent%' order by sender");
	 * 
	 * data = m_jdbcUpdate.getRowData();
	 * 
	 * }// end of synchronized /
	 * 
	 * synchronized(m_jdbcUpdate){ if(arg.equalsIgnoreCase("all")){
	 * //m_jdbcUpdate.getSqlData("select distinct sender from email group by
	 * sender"); //data = m_jdbcUpdate.getRowData(); data =
	 * (String[])m_winGui.getUserList(); }
	 * 
	 * else if(arg.equalsIgnoreCase("sender")){ m_jdbcUpdate.getSqlData("select
	 * distinct sender from email group by sender having count(*) > 200"); data =
	 * m_jdbcUpdate.getColumnData(0); } else{ //frequent threshold
	 * //System.out.println("arg is '" + arg+"'");
	 * m_jdbcUpdate.getSqlData("select distinct sender from email group by
	 * sender having count(*) > "+arg); data = m_jdbcUpdate.getColumnData(0); /*
	 * String query="create temporary table CountTemp (sender varchar(122),
	 * count int)"; m_jdbcUpdate.executeQuery(query);
	 * 
	 * try{ query ="insert into CountTemp "; query+="select "; query+="sender,
	 * count(*) "; query+="from email "; query+="group by sender";
	 * 
	 * m_jdbcUpdate.executeQuery(query);
	 * 
	 * query="select sender "; query+="from CountTemp where count>="+arg;
	 * 
	 * m_jdbcUpdate.getSqlData(query); data = m_jdbcUpdate.getRowData(); }
	 * finally{ m_jdbcUpdate.executeQuery("drop table CountTemp"); } / }
	 * 
	 * }// end of synchronized
	 * 
	 * 
	 * 
	 * ComboBoxModel model = new DefaultComboBoxModel(data);
	 * m_comboUsers.setModel(model);
	 * 
	 * 
	 * 
	 * //for(int i=0;i <data.length;i++){
	 * 
	 * //if(data[i].length!=1){ // throw new SQLException("Database returned
	 * wrong number of columns"); // } // if(data[i][0] !=null)//.length() >0){
	 * //non-null sender name //{ // m_comboUsers.addItem((String)data[i][0]);
	 * //m_hsUsers.add((String)data[i][0]); // } //}
	 * if(m_comboUsers.getItemCount() >1) m_comboUsers.setSelectedIndex(0);
	 * 
	 *  }
	 */

	/*
	 * public final void refreshCombo() throws SQLException{ // remember current
	 * selection String strCurrent = new String(""); Object objCurrent =
	 * m_comboUsers.getSelectedItem(); if (objCurrent != null) { strCurrent =
	 * (String) objCurrent; } // repopulate String data[][];
	 * synchronized(m_jdbcUpdate) { m_jdbcUpdate.getSqlData("select distinct
	 * sender from email order by sender"); data = m_jdbcUpdate.getRowData(); }
	 * 
	 * for (int i=0; i <data.length; i++) { if (data[i].length != 1) { throw new
	 * SQLException("Database returned wrong number of columns"); } if
	 * (data[i][0].length() > 0) { if (m_hsUsers.add(data[i][0])) { int j=0;
	 * while ((j < m_comboUsers.getItemCount()) &&
	 * (data[i][0].compareToIgnoreCase((String) m_comboUsers.getItemAt(j)) > 0)) {
	 * j++; } m_comboUsers.insertItemAt((String) data[i][0], j); if
	 * (m_comboUsers.getItemCount() == 1) { m_comboUsers.setSelectedIndex(0); } } } //
	 * replace selection if (strCurrent.equals((String)data[i][0])) {
	 * m_comboUsers.setSelectedItem((String)data[i][0]); } }//end of for }
	 */

	/**
	 * Refresh.
	 *
	 * @throws SQLException the sQL exception
	 */
	public final void Refresh() throws SQLException {
	//refreshCombo();
	}






	//for the selected user, find the min and max date for availabe data
	/**
	 * Gets the available period.
	 *
	 * @return the available period
	 */
	private void getAvailablePeriod() {


		/*
		 * Object selected = m_comboUsers.getSelectedItem(); if (selected ==
		 * null) return;
		 * 
		 * curUser = (String)selected;
		 */
		/*
		 * String data[][]; String query;
		 * 
		 * minAvailableDate=""; maxAvailableDate="";
		 * 
		 * synchronized(m_jdbcView){
		 * 
		 * query = "select max(dates), min(dates) from email where
		 * sender='"+curUser+"' or rcpt='"+curUser+"'";
		 * m_jdbcView.getSqlData(query); data = m_jdbcView.getRowData();
		 * 
		 * if(data.length!=1 && data[0].length!=2){ System.err.println("select
		 * dates error"); throw new SQLException("Wrong number of cells"); }
		 * 
		 * 
		 * maxAvailableDate = (String)data[0][0]; minAvailableDate =
		 * (String)data[0][1]; }
		 */
		userShortProfile udata = m_winGui.getUserInfo(curUser);
		if (udata != null) {

			maxAvailableDate = udata.getEndDate();//data[winGui.User_max];
			minAvailableDate = udata.getStartDate();//data[winGui.User_min];
			user_count = udata.getTotalCount();//data[2];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			user_count = 0;
		}


	} //end of getAvailabeData



	//display available date and adjust the period selection
	/**
	 * Adjust selection date.
	 */
	void adjustSelectionDate() {

		periodLabel.setText(user_count + " email(s) from " + minAvailableDate + " to " + maxAvailableDate);
		periodLabel.validate();

		// set the time selection according to the min and max date
		Date d = Utils.deSqlizeDateYEAR(maxAvailableDate);
		Date mind = Utils.deSqlizeDateYEAR(minAvailableDate);
		if (d != null && mind != null) {
			m_spinnerModelRecentEnd.setValue(d);
			m_spinnerModelRecentStart.setValue(mind);
		}
	}

	
	/**
	 * Gets the comm time work.
	 *
	 * @param user the user
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param level the level
	 * @return the comm time work
	 * @throws SQLException the sQL exception
	 */
	public userComm[] getCommTimeWork(final String user, String startDate, String endDate,int level)throws SQLException
	{
		String condition = " dates BETWEEN '" + startDate + "' AND '" + endDate + "'";
		
		userComm[] hm = CT.allUserTime(user, condition, level);

		return sortUC(hm);//?vec;
	}
	

	/*
	 * compute the avg comm time for this user, and draw out
	 */
	/**
	 * Gets the comm time.
	 *
	 * @param user the user
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param level the level
	 * @return the comm time
	 * @throws SQLException the sQL exception
	 */
	private void getCommTime(final String user, String startDate, String endDate,int level) throws SQLException {


		if ((startDate.compareTo(endDate)) > 0) {
			throw (new SQLException("start date is larger than end date"));
		}

		/*if (startDate.compareTo(minAvailableDate) < 0) {
			//throw (new SQLException("recent start date is too early"));
			startDate = minAvailableDate;

		}

		if (endDate.compareTo(maxAvailableDate) > 0) {
			//throw (new SQLException("recent end date is too late"));
			//lose the requirement
			endDate = maxAvailableDate;
		}*/

		//String condition = " dates >= '" + startDate + "' and dates <='" + endDate + "'";
		//userComm[] hm = CT.allUserTime(user, condition, level);
		//?Vector vec = sortVector(hm);
		//set the global variable
		curResult = getCommTimeWork(user,  startDate,  endDate, level);//sortUC(hm);//?vec;
		int count=0;
		//Vector toDraw = new Vector();
		//Vector numberV = new Vector();
		//Vector vipV = new Vector();
		for (int i = 0; i < curResult.length; i++) {
			userComm u = curResult[i];//(userComm)vec.elementAt(i);

			if (u.time >= 0 && u.time < Double.MAX_VALUE) {
				count++;
				//	toDraw.addElement(new Double(u.time));
			//	numberV.addElement(new Double(u.number));
			//	vipV.addElement(new Double(u.vipValue));
			}
		}

		if (count>0){//toDraw.size() > 0) {
			//draw_histogram(m_histogram, toDraw, numberV, vipV);
			//fillTable(m_timeTable, vec);
			SortTableModel srt = (SortTableModel) m_timeTable.getModel();
			refreshTable(srt);
			refreshPlot(srt);
		}


	}

	/*
	 * add legend to the legend plot, which gives explanation to the curves
	 */
	/**
	 * Draw legend.
	 */
	private void drawLegend() {

		double[] x = new double[2];
		x[0] = 0;
		x[1] = 30;
		double[] y = new double[2];
		y[0] = 0;
		y[1] = 12;
		m_legend.setXScale(x);
		m_legend.setYScale(y);

		x[0] = 1;
		x[1] = 5;
		double[] y1 = new double[2];
		y1[0] = 10;
		y1[1] = 10;
		m_legend.addCurve(x, y1);
		m_legend.setLineColor(Color.green);
		double[] y2 = new double[2];
		y2[0] = 7;
		y2[1] = 7;
		m_legend.addCurve(x, y2);
		m_legend.setLineColor(Color.blue);
		double[] y3 = new double[2];
		y3[0] = 3;
		y3[1] = 3;
		m_legend.addCurve(x, y3);
		m_legend.setLineColor(Color.red);

		/*
		 * double[] xtic = m_legend.getXTics(); double[] ytic =
		 * m_legend.getYTics();
		 * 
		 * System.out.println("xtic: "+xtic.length); System.out.println("ytic:
		 * "+ytic.length);
		 */

		m_legend.addAnnotation("Log of relative avg comm time (min)", 8, 9);
		m_legend.setAnnotationColor(Color.green);
		m_legend.addAnnotation("Log of # email", 8, 6);
		m_legend.setAnnotationColor(Color.blue);
		m_legend.addAnnotation("VIP rank", 8, 2);
		m_legend.setAnnotationColor(Color.red);

		m_legend.repaint();
	}


	/*
	 * draw out the histogram inside the plot
	 */
	/**
	 * Draw_histogram.
	 *
	 * @param plot the plot
	 * @param Histogram the histogram
	 * @param nV the n v
	 * @param vV the v v
	 */
	private void draw_histogram(final JPlot2D plot, final Vector Histogram, final Vector nV, final Vector vV) {

		plot.removeAll();

		int size = Histogram.size();
		double[] s = new double[2];
		s[0] = 0;
		s[1] = size;
		double[] x = new double[size + 1];
		double[] y = new double[size + 1];
		double[] y2 = new double[size + 1];
		double[] y3 = new double[size + 1];

		for (int i = 0; i < size; i++) {
			x[i + 1] = i + 1;
			y[i + 1] = Math.log(((Double) Histogram.elementAt(i)).doubleValue() + 1); //+1
			// to
			// avoid
			// negative
			// number
			y2[i + 1] = Math.log(((Double) nV.elementAt(i)).doubleValue());
			y3[i + 1] = ((Double) vV.elementAt(i)).doubleValue();
		}

		plot.setXLabel("Index of accounts");
		//plot.setYLabel("Log of relative avg comm time (min)");
		plot.setXScale(s);
		plot.addCurve(x, y);
		plot.setLineColor(Color.green);
		plot.addCurve(x, y2);
		plot.setLineColor(Color.blue);
		plot.addCurve(x, y3);
		plot.setLineColor(Color.red);



		plot.repaint();
	}


	/*
	 * fill the table m_timeTable by vector
	 */
	/**
	 * Fill table.
	 *
	 * @param table the table
	 * @param V the v
	 */
	private void fillTable(final JTable table, final Vector V) {

		//DefaultTableModel model = (DefaultTableModel)m_timeTable.getModel();

		SortTableModel model = (SortTableModel) m_timeTable.getModel();

		// clear table
		int length = model.getRowCount();
		for (int i = length - 1; i >= 0; i--)
			model.removeRow(i);

		/*
		 * MessageTableModel model = (MessageTableModel)m_timeTable.getModel();
		 * model.clear();
		 */

		Object[] tt = new Object[5];
		int index = 0;
		for (int i = 0; i < V.size(); i++) {

			userComm u = (userComm) V.elementAt(i);
			tt[1] = u.acct;
			tt[2] = (new Integer(u.number));
			/*
			 * if(u.time==Double.MAX_VALUE) tt[3]="NaN"; //print out NaN when
			 * there is no real communication else tt[3]=(new Double(u.time));
			 * if(u.vipValue==-1) tt[4]="NaN"; else tt[4]=(new
			 * Double(u.vipValue));
			 */

			if (u.time == Double.MAX_VALUE) {
				continue;
			}
			tt[3] = (new Double(u.time));
			if (u.vipValue == -1) {
				continue;
			}
			tt[4] = (new Double(u.vipValue));

			tt[0] = (new Integer(++index));
			model.addRow(tt);
		}
	}


	/*
	 * Refresh the table using new conditions
	 */
	/**
	 * Refresh table.
	 *
	 * @param model the model
	 * @throws SQLException the sQL exception
	 */
	private void refreshTable(SortTableModel model) throws SQLException {

		if (curResult == null) {
			throw new SQLException("current result is null");
		}

		int num = 1;
		double vip = 0;
		try {
			num = Integer.parseInt(emailNum.getText().trim());
			vip = Double.parseDouble(vipThreshold.getText().trim());
		} catch (NumberFormatException e) {
			System.err.println("parse number error: " + e);
		}

		//SortTableModel model = (SortTableModel) m_timeTable.getModel();

		// clear table
		//int length = model.getRowCount();
		//for (int i = length - 1; i >= 0; i--)
		//	model.removeRow(i);
		model.setRowCount(0);
		
		Object[] tt = new Object[5];
		int index = 0;
		for (int i = 0; i < curResult.length; i++) {

			userComm u = curResult[i];//(userComm)curResult.elementAt(i);
			
			if (u.time == Double.MAX_VALUE)
				continue;
			
			if (u.vipValue == -1 || u.vipValue < vip)
				continue;

			if (u.number >= num)
				tt[2] = (new Integer(u.number));
			else
				continue;

			

			tt[3] = (new Double(u.time));
			
			tt[4] = (new Double(u.vipValue));

			tt[0] = (new Integer(++index));
			tt[1] = u.acct;

			model.addRow(tt);
		}
	}


	/*
	 * refresh the plot m_histogram according to current table content
	 */
	/**
	 * Refresh plot.
	 *
	 * @param model the model
	 */
	private void refreshPlot(SortTableModel model) {

		m_histogram.removeAll();
		try {
//			SortTableModel model = (SortTableModel) m_timeTable.getModel();

			int size = model.getRowCount();

			double[] s = new double[2];
			s[0] = 0;
			s[1] = size;
			double[] x = new double[size + 1];
			double[] y = new double[size + 1]; //avg time
			double[] y2 = new double[size + 1]; //#email
			double[] y3 = new double[size + 1]; //vip

			for (int i = 0; i < size; i++) {
				x[i + 1] = i + 1;
				int index = ((Integer) model.getValueAt(i, 0)).intValue();
				y[index] = Math.log(((Double) model.getValueAt(i, 3)).doubleValue() + 1); //+1
				// to
				// avoid
				// negative
				// number
				y2[index] = Math.log(((Integer) model.getValueAt(i, 2)).intValue());
				y3[index] = ((Double) model.getValueAt(i, 4)).doubleValue();
			}

			m_histogram.setXLabel("Index of accounts");
			//plot.setYLabel("Log of relative avg comm time (min)");
			m_histogram.setXScale(s);
			m_histogram.addCurve(x, y);
			m_histogram.setLineColor(Color.green);
			m_histogram.addCurve(x, y2);
			m_histogram.setLineColor(Color.blue);
			m_histogram.addCurve(x, y3);
			m_histogram.setLineColor(Color.red);

			m_histogram.repaint();
		} catch (Exception e) {
		}



	}


	/*
	 * after select a row in the table, highlight the position in the curve plot
	 * using a black bar
	 */
	/**
	 * Highlight plot.
	 *
	 * @param model the model
	 */
	private void highlightPlot(SortTableModel model) {

		int row = m_timeTable.getSelectedRow();
		if (row == -1)
			return;

		refreshPlot(model);

		//SortTableModel model = (SortTableModel) m_timeTable.getModel();

		double index = ((Integer) model.getValueAt(row, 0)).intValue();

		double[] yScale = m_histogram.getYScale();
		double[] t1 = new double[7];
		double[] t2 = new double[7];
		double top = yScale[1];
		double gap = top / (30 * 100);
		//double pos = index-0.07;
		double pos = index - 7 * gap;


		for (int i = 0; i < 7; i++) {
			t1[i] = pos;
			pos += gap;
			t2[i] = top;
		}
		t2[0] = 0;
		t2[6] = 0;
		m_histogram.addCurve(t1, t2);
		m_histogram.setLineStyle(JPlot2D.LINESTYLE_SOLID);
		m_histogram.setLineColor(Color.black);

		m_histogram.repaint();
	}



	/*
	 * sort Vector, increasingly using bubble sort
	 *//*
	    * private Vector sortVector(final Vector V){
	    */
	/**
	 * Sort uc.
	 *
	 * @param list the list
	 * @return the user comm[]
	 */
	private userComm[] sortUC(userComm[] list) {


		userComm tempu;
		if (list.length <= 0)
			return list;


		for (int i = 0; i < list.length; i++) {
			// userComm u = list[i];//(userComm)V.elementAt(i);
			//double d = u.time;
			double d = list[i].vipValue;
			for (int j = i + 1; j < list.length; j++) {

				//double d2 = ((userComm)vec.elementAt(j)).time;
				double d2 = list[j].vipValue;//((userComm)vec.elementAt(j)).vipValue;
				if (d < d2) {
					tempu = list[j];
					list[j] = list[i];
					list[i] = tempu;
					d = tempu.vipValue;
				}

			}// for j
		}//for i

		return list;
	}

	/*
	 * private final Date deSqlizeDate(final String s) { SimpleDateFormat format =
	 * new SimpleDateFormat("yyyy-MM-dd"); Date d; try{ d = format.parse(s); }
	 * catch(ParseException pe){ System.err.println("parse error for sql style
	 * string"+pe); return null; }
	 * 
	 * return d; }
	 */
}

///////////////////////////////////

