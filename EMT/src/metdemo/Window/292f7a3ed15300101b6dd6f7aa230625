package metdemo.Window;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import metdemo.winGui;
import metdemo.AlertTools.Alert;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.CliqueTools.SocialCliques;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.DistanceCompute;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.HistogramCalculator;
import metdemo.Tools.Utils;
import metdemo.Tools.overviewHistoryPanel;
import metdemo.dataStructures.userShortProfile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.io.*;
import java.sql.SQLException;
import chapman.graphics.*;

// TODO: Auto-generated Javadoc
/**
 * For the tab to find users with similar behavior.
 *
 * @author Ke Wang, 11/02/02, modify keyword: version 2
 */

public class UsageHistogram extends JScrollPane implements ListSelectionListener {
	
	/** The m_alerts. */
	private ReportForensicWindow m_alerts;

	//version 2
	/** The h1. */
	private JPanel panel, panResult, h, h1;

	/** The label1. */
	private JLabel label, label1;

	/** The m_ all user diff. */
	private JTable m_AllUserDiff;

	/** The s pane. */
	private JScrollPane sPane;

	/** The m_diff column renderer. */
	private DefaultTableCellRenderer m_diffColumnRenderer;

	/** The m_safe. */
	private double m_highalert, m_middlealert, m_safe; //threshhold

	/** The recentp. */
	private JComboBox recentp = new JComboBox();

	/** The Constant RECENTWEEK. */
	private static final int RECENTWEEK = 0;

	/** The Constant RECENT2WEEK. */
	private static final int RECENT2WEEK = 1;

	/** The Constant RECENTMONTH. */
	private static final int RECENTMONTH = 2;

	/** The Constant RECENT2MONTH. */
	private static final int RECENT2MONTH = 3;

	/** The profileh. */
	private JComboBox profileh = new JComboBox();

	/** The Constant PROFILEMONTH. */
	private static final int PROFILEMONTH = 0;

	/** The Constant PROFILE2MONTH. */
	private static final int PROFILE2MONTH = 1;

	/** The Constant PROFILE3MONTH. */
	private static final int PROFILE3MONTH = 2;

	/** The Constant PROFILEFULL. */
	private static final int PROFILEFULL = 3;

	//    private JComboBox m_comboUsers;

	/** The m_histogram base. */
	private JPlot2D m_histogram, m_histogramBase;

	/** The m_difference. */
	private JTextField m_difference;

	/** The alert label. */
	private JLabel alertLabel;

	/** The period label. */
	private JLabel periodLabel;

	//JPlot2D userplot;
	/** The jfplot_series. */
	private TimeSeries jfplot_series = new TimeSeries("Emails", "Month-Year", "Count", Month.class);
	
	/** The m_spinner model recent start. */
	private SpinnerDateModel m_spinnerModelRecentStart;

	/** The m_spinner model recent end. */
	private SpinnerDateModel m_spinnerModelRecentEnd;

	/** The m_spinner model start. */
	private SpinnerDateModel m_spinnerModelStart;

	/** The m_spinner model end. */
	private SpinnerDateModel m_spinnerModelEnd;

	/** The m_alert level. */
	private JSlider m_alertLevel;

	//private HashSet m_hsUsers;
	/** The hc. */
	private HistogramCalculator hc;

	//	private String curUser; //current selected user
	/** The recent histogram. */
	private double[] recentHistogram;

	/** The profile histogram. */
	private double[] profileHistogram;

	/** The std dev. */
	private double[] stdDev = null; //store the standard dev

	/** The profiles. */
	private Hashtable profiles; // as a cache to speed up

	/** The m_jdbc view. */
	private EMTDatabaseConnection m_jdbcView;

	/** The m_win gui. */
	private winGui m_winGui; //handle

	//constructor
	/**
	 * Instantiates a new usage histogram.
	 *
	 * @param jdbcView the jdbc view
	 * @param alert the alert
	 * @param md5Renderer the md5 renderer
	 * @param winGui the win gui
	 */
	public UsageHistogram(EMTDatabaseConnection jdbcView, ReportForensicWindow alert, md5CellRenderer md5Renderer,
			winGui winGui) {

		if ((jdbcView == null)) {
			throw new NullPointerException("problem db in usage");
		}

		//init the variables
		m_jdbcView = jdbcView;
		m_alerts = alert;
		m_winGui = winGui;

		//m_hsUsers = new HashSet();
		hc = new HistogramCalculator(jdbcView);
		profiles = new Hashtable();

		//button to make reports
		JButton reporter = new JButton("Generate Reports");
		reporter.setToolTipText("Generate reports for each of the users using the range specified.");
		reporter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//thread it out so it can run gui wise
				(new Thread(new Runnable() {
					public void run() {
						MakeReport();
					}
				})).start();
			}
		});

		//arrange the layout
		panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		panel.setLayout(gridbag);

		//m_comboUsers = new JComboBox();
		//m_comboUsers.setPreferredSize(new Dimension(250,25));
		//m_comboUsers.setRenderer(md5Renderer);

		//JPanel panUser = new JPanel();
		//panUser.setLayout(new FlowLayout());
		//panUser.add(reporter);
		/*
		 * b1 = new JRadioButton("all users", false); b2 = new
		 * JRadioButton("only senders", true); b3 = new JRadioButton("frequent
		 * users", false); bg = new ButtonGroup(); bg.add(b1); bg.add(b2);
		 * bg.add(b3);
		 */

		constraints.anchor = GridBagConstraints.EAST;
		//constraints.fill = GridBagConstraints.HORIZONTAL;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(0, 5, 0, 5);
		gridbag.setConstraints(reporter, constraints);
		panel.add(reporter);
		/*
		 * constraints.gridx++; constraints.anchor = GridBagConstraints.EAST;
		 * gridbag.setConstraints(b1, constraints); panel.add(b1);
		 * constraints.anchor = GridBagConstraints.WEST; constraints.gridx++;
		 * gridbag.setConstraints(b2, constraints); panel.add(b2);
		 * constraints.anchor = GridBagConstraints.EAST; constraints.gridx++;
		 * gridbag.setConstraints(b3, constraints); panel.add(b3);
		 * 
		 * 
		 * JLabel freqUserLabel = new JLabel("(#email threshold:");
		 * 
		 * constraints.gridx++; gridbag.setConstraints(freqUserLabel,
		 * constraints); panel.add(freqUserLabel); constraints.anchor =
		 * GridBagConstraints.WEST; JPanel kp = new JPanel();
		 * //panUser.add(freqUserLabel); userFreq = new JTextField("8");
		 * userFreq.setText(" 20 "); //panUser.add(userFreq); kp.add(userFreq);
		 * kp.add(new JLabel(")")); constraints.gridx++;
		 * gridbag.setConstraints(kp, constraints); panel.add(kp);
		 */

		//panUser.add(back);
		//constraints.gridx++;
		//gridbag.setConstraints(back, constraints);
		//panel.add(back);
		//JPanel panSelect = new JPanel();
		//panSelect.setLayout(new FlowLayout());
		//	panSelect.add(new JLabel("Select Account:"));
		/*
		 * JLabel select = new JLabel("Select Account:"); constraints.gridy=1;
		 * constraints.gridx=0; gridbag.setConstraints(select, constraints);
		 * panel.add(select);
		 * 
		 * constraints.anchor = GridBagConstraints.WEST;
		 * constraints.gridwidth=2;//wide format constraints.gridx++;
		 * gridbag.setConstraints(m_comboUsers, constraints);
		 * panel.add(m_comboUsers); constraints.anchor =
		 * GridBagConstraints.EAST; //panSelect.add(m_comboUsers);
		 * initCombo("sender"); //constraints.gridx =0; //constraints.gridy =1;
		 * //constraints.gridwidth =1; //constraints.insets = new Insets(0, 5,
		 * 0, 5); //gridbag.setConstraints(panSelect, constraints);
		 * //panel.add(panSelect);
		 */
		periodLabel = new JLabel("User data");
		periodLabel.setForeground(Color.blue);

		//JPanel pp = new JPanel();
		//pp.setLayout(new FlowLayout());
		//pp.add(periodLabel);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx += 2;
		constraints.gridwidth = 3;
		gridbag.setConstraints(periodLabel, constraints);
		panel.add(periodLabel);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = 1;
		//constraints.gridx =1;
		//constraints.gridy =1;
		//constraints.gridwidth = 1;
		//constraints.insets = new Insets(5, 5, 0, 5);
		//gridbag.setConstraints(pp, constraints);
		//panel.add(pp);

		//	JLabel recentp = new JLabel("Recent Period:");
		JLabel startdate = new JLabel("Start Date");

		recentp.addItem("Recent Period (Week)");
		recentp.addItem("Recent Period (2 Week)");
		recentp.addItem("Recent Period (Month)");
		recentp.addItem("Recent Period (2 Month)");
		recentp.addItemListener(new ItemListener() {
		
			public void itemStateChanged(ItemEvent evt) {
				
				 if(evt.getStateChange() != ItemEvent.SELECTED)
				    {
				        return;
				    }
				 
				 String selected = (String)m_winGui.getSelectedUser();
					//m_comboUsers.getSelectedItem();
					if (selected == null)
						return;
				 adjustSelectionDate(selected);
			}
		});
		
		
		
		//JPanel panRecent = new JPanel();
		//panRecent.setLayout(new FlowLayout());
		constraints.gridwidth = 1;
		constraints.gridy = 2;
		constraints.gridx = 0;
		gridbag.setConstraints(recentp, constraints);
		panel.add(recentp);

		constraints.gridy = 2;
		constraints.gridx++;
		gridbag.setConstraints(startdate, constraints);
		panel.add(startdate);

		//panRecent.add(new JLabel("Recent Period: "));
		//panRecent.add(new JLabel("Start Date"));

		m_spinnerModelRecentStart = new SpinnerDateModel();
		Calendar recentcal = new GregorianCalendar();
		recentcal.set(2002, 7, 15);
		recentcal.add(Calendar.DATE, -7);
		m_spinnerModelRecentStart.setValue(recentcal.getTime());
		JSpinner spinnerRecentStart = new JSpinner(m_spinnerModelRecentStart);
		JSpinner.DateEditor editorRecentStart = new JSpinner.DateEditor(spinnerRecentStart, "MMM dd, yyyy   ");
		spinnerRecentStart.setEditor(editorRecentStart);

		//panRecent.add(spinnerRecentStart);
		//panRecent.add(new JLabel("End Date"));
		JLabel enddate = new JLabel("End Date");
		JLabel enddate2 = new JLabel("End Date");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridy = 2;
		constraints.gridx++;
		gridbag.setConstraints(spinnerRecentStart, constraints);
		panel.add(spinnerRecentStart);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx++;
		gridbag.setConstraints(enddate, constraints);
		panel.add(enddate);

		m_spinnerModelRecentEnd = new SpinnerDateModel();
		Calendar recentcalend = new GregorianCalendar();
		recentcalend.set(2002, 7, 15);
		m_spinnerModelRecentEnd.setValue(recentcalend.getTime());
		JSpinner spinnerRecentEnd = new JSpinner(m_spinnerModelRecentEnd);
		JSpinner.DateEditor editorRecentEnd = new JSpinner.DateEditor(spinnerRecentEnd, "MMM dd, yyyy   ");
		spinnerRecentEnd.setEditor(editorRecentEnd);
		constraints.anchor = GridBagConstraints.WEST;
		//panRecent.add(spinnerRecentEnd);
		constraints.gridx++;
		gridbag.setConstraints(spinnerRecentEnd, constraints);
		panel.add(spinnerRecentEnd);
		constraints.anchor = GridBagConstraints.EAST;

		JButton studyUserbutton = new JButton("Study User Profile");

		constraints.gridx++;
		gridbag.setConstraints(studyUserbutton, constraints);
		panel.add(studyUserbutton);

		studyUserbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				studyCurrentUser((String) m_winGui.getSelectedUser());
			}
		});

		//constraints.gridx =0;
		//constraints.gridy =2;
		//constraints.gridwidth =2;
		//constraints.insets = new Insets(0, 5, 0, 5);
		//gridbag.setConstraints(panRecent, constraints);
		//panel.add(panRecent);

		/////
		//JPanel panProfile = new JPanel();
		//panProfile.setLayout(new FlowLayout());
		//panProfile.add(new JLabel("Profile Histogram Period:"));
		//panProfile.add(new JLabel("Start Date"));
		//JLabel profileh = new JLabel("Profile Histogram Period:");

		profileh.addItem("Profile Histogram Period: (Month)");
		profileh.addItem("Recent Period (2 Month)");
		profileh.addItem("Recent Period (3 Month)");
		profileh.addItem("All Data");
		profileh.addItemListener(new ItemListener() {
		
			public void itemStateChanged(ItemEvent evt) {
				
				 if(evt.getStateChange() != ItemEvent.SELECTED)
				    {
				        return;
				    }
				 
				 String selected = (String)m_winGui.getSelectedUser();
					//m_comboUsers.getSelectedItem();
					if (selected == null)
						return;
				 adjustSelectionDate(selected);
			}
		});
		constraints.gridwidth = 1;
		constraints.gridy = 3;
		constraints.gridx = 0;
		gridbag.setConstraints(profileh, constraints);
		panel.add(profileh);
		JLabel startdate2 = new JLabel("Start Date");
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx++;
		gridbag.setConstraints(startdate2, constraints);
		panel.add(startdate2);
		constraints.anchor = GridBagConstraints.WEST;

		m_spinnerModelStart = new SpinnerDateModel();
		Calendar cal = new GregorianCalendar();
		cal.set(2002, 7, 15); //aug 15
		cal.add(Calendar.MONTH, -1);
		cal.add(Calendar.DATE, -7);
		m_spinnerModelStart.setValue(cal.getTime());
		JSpinner spinnerStart = new JSpinner(m_spinnerModelStart);
		JSpinner.DateEditor editorStart = new JSpinner.DateEditor(spinnerStart, "MMM dd, yyyy   ");
		spinnerStart.setEditor(editorStart);

		constraints.gridx++;
		gridbag.setConstraints(spinnerStart, constraints);
		panel.add(spinnerStart);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx++;
		gridbag.setConstraints(enddate2, constraints);
		panel.add(enddate2);

		//panProfile.add(spinnerStart);
		//panProfile.add(new JLabel(" End Date"));

		m_spinnerModelEnd = new SpinnerDateModel();
		Calendar endcal = new GregorianCalendar();
		endcal.set(2002, 7, 15);
		endcal.add(Calendar.DATE, -7);
		m_spinnerModelEnd.setValue(endcal.getTime());
		JSpinner spinnerEnd = new JSpinner(m_spinnerModelEnd);
		JSpinner.DateEditor editorEnd = new JSpinner.DateEditor(spinnerEnd, "MMM dd, yyyy   ");
		spinnerEnd.setEditor(editorEnd);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx++;
		gridbag.setConstraints(spinnerEnd, constraints);
		panel.add(spinnerEnd);
		constraints.anchor = GridBagConstraints.EAST;
		//panProfile.add(spinnerEnd);

		//constraints.gridx =0;
		//constraints.gridy =3;
		//constraints.gridwidth =2;
		//constraints.insets = new Insets(0, 5, 0, 5);
		//gridbag.setConstraints(panProfile, constraints);
		//panel.add(panProfile);

		//JPanel panAlert = new JPanel();
		//panAlert.setLayout(new FlowLayout());
		//panAlert.add(new JLabel("Alert Level: "));
		JLabel alertlevel = new JLabel("Alert Level:");
		constraints.gridwidth = 1;
		constraints.gridy = 4;
		constraints.gridx = 0;
		gridbag.setConstraints(alertlevel, constraints);
		panel.add(alertlevel);
		constraints.anchor = GridBagConstraints.CENTER;
		// for the slider of alert level
		m_alertLevel = new JSlider(SwingConstants.HORIZONTAL, 0, 10, 5);
		m_alertLevel.setPreferredSize(new Dimension(180, 40));
		m_alertLevel.setMajorTickSpacing(5);
		m_alertLevel.setMinorTickSpacing(1);
		m_alertLevel.setPaintTicks(true);

		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("low"));
		labelTable.put(new Integer(10), new JLabel("high"));
		m_alertLevel.setLabelTable(labelTable);
		m_alertLevel.setPaintLabels(true);

		//panAlert.add(m_alertLevel);
		constraints.gridwidth = 2;
		constraints.gridx++;
		gridbag.setConstraints(m_alertLevel, constraints);
		panel.add(m_alertLevel);

		JButton runButton = new JButton("Update Histograms");
		runButton.setBackground(Color.red);
		runButton
				.setToolTipText("<html>Press this button to see<BR> Recent and common histograms<BR> and the difference between them</html>");
		//panAlert.add(runButton);
		constraints.gridwidth = 1;
		constraints.gridx += 2;
		gridbag.setConstraints(runButton, constraints);
		panel.add(runButton);

		//version 2 , add one more button compute for all users
		JButton runAllButton = new JButton("Run Against All");
		runAllButton
				.setToolTipText("press this button to calculate difference for all users using above selected periods");
		//panAlert.add(runAllButton);

		constraints.gridx++;
		gridbag.setConstraints(runAllButton, constraints);
		panel.add(runAllButton);

		//helpButton = new JButton("HELP");
		//helpButton.setBackground(Color.orange);
		EMTHelp j = new EMTHelp(EMTHelp.USAGEHISTORGRAM);
		//panAlert.add(j);

		constraints.gridx++;
		constraints.gridwidth = 1;
		gridbag.setConstraints(j, constraints);
		panel.add(j);

		//constraints.gridx = 0;
		//constraints.gridy = 4;
		//constraints.gridwidth =2;
		//constraints.insets = new Insets(10,5,0,5);
		//gridbag.setConstraints(panAlert, constraints);
		//panel.add(panAlert);

		//version 2, add table for all users' diffence
		DefaultTableModel m = new DefaultTableModel() {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		m.addColumn("account");
		m.addColumn("difference");
		m_AllUserDiff = new JTable(m);
		m_AllUserDiff.setRowSelectionAllowed(true);
		m_AllUserDiff.setColumnSelectionAllowed(false);
		m_AllUserDiff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_AllUserDiff.setDefaultRenderer(m.getColumnClass(0), md5Renderer);
		m_AllUserDiff.getColumnModel().getColumn(1).setMaxWidth(70);

		//for column difference
		m_diffColumnRenderer = new DefaultTableCellRenderer() {
			public void setValue(Object value) {

				if ((value instanceof String)) {
					if (((String) value).equals("N/A"))
						setBackground(Color.green);

					else {
						double d = Double.parseDouble((String) value);
						if (d > m_highalert)
							setBackground(Color.red);
						else if (d > m_safe)
							setBackground(Color.pink);
						else
							setBackground(Color.white);
					}
				}

				setText((value == null) ? "" : value.toString());
			}
		};
		//m_AllUserDiff.setDefaultRenderer(m.getColumnClass(1),m_diffColumnRenderer);

		constraints.anchor = GridBagConstraints.CENTER;
		sPane = new JScrollPane(m_AllUserDiff);
		sPane.setPreferredSize(new Dimension(400, 140));

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 6;
		constraints.insets = new Insets(5, 5, 0, 5);
		gridbag.setConstraints(sPane, constraints);
		panel.add(sPane);
		sPane.setVisible(false);

		label = new JLabel("Usage histogram for recent period");
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 3;
		constraints.insets = new Insets(10, 5, 0, 5);
		gridbag.setConstraints(label, constraints);
		panel.add(label);

		m_histogram = new JPlot2D();
		m_histogram.setPlotType(JPlot2D.BAR);
		m_histogram.setBackgroundColor(Color.white);
		m_histogram.setFillColor(Color.blue);
		m_histogram.setPreferredSize(new Dimension(340, 260));

		h = new JPanel();
		h.add(m_histogram);
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = 3;
		constraints.insets = new Insets(5, 2, 5, 2);
		gridbag.setConstraints(h, constraints);
		panel.add(h);

		label1 = new JLabel("Usage histogram for profile period");
		constraints.gridx = 3;
		constraints.gridy = 6;
		constraints.gridwidth = 3;
		constraints.insets = new Insets(10, 5, 0, 5);
		gridbag.setConstraints(label1, constraints);
		panel.add(label1);

		m_histogramBase = new JPlot2D();
		m_histogramBase.setPlotType(JPlot2D.BAR);
		m_histogramBase.setBackgroundColor(Color.white);
		m_histogramBase.setFillColor(Color.yellow);
		m_histogramBase.setPreferredSize(new Dimension(340, 260));

		h1 = new JPanel();
		h1.add(m_histogramBase);
		constraints.gridx = 3;
		constraints.gridy = 7;
		constraints.gridwidth = 3;
		constraints.insets = new Insets(5, 2, 5, 2);
		gridbag.setConstraints(h1, constraints);
		panel.add(h1);

		m_difference = new JTextField(15);
		m_difference.setPreferredSize(new Dimension(120, 25));

		panResult = new JPanel();
		panResult.setLayout(new FlowLayout());
		panResult.add(new JLabel("Difference from usual behavior:"));
		panResult.add(m_difference);

		constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.gridwidth = 6;
		//constraints.insets = new Insets(5, 5, 0, 5);
		gridbag.setConstraints(panResult, constraints);
		panel.add(panResult);

		alertLabel = new JLabel("Behaviour");
		//JPanel jp = new JPanel();
		//jp.setLayout(new FlowLayout());
		//jp.add(alertLabel);

		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.gridwidth = 6;

		gridbag.setConstraints(alertLabel, constraints);
		panel.add(alertLabel);

		constraints.gridx = 0;
		constraints.gridy = 10;
		constraints.gridwidth = 6;

		/*userplot = new JPlot2D();
		userplot.setPlotType(JPlot2D.BAR);
		userplot.setBackgroundColor(Color.white);
		userplot.setFillColor(Color.blue);
		userplot.setGridState(JPlot2D.GRID_ON);
		userplot.setPreferredSize(new Dimension(700, 200));*/
		 //need to setup jfreechart
        JFreeChart jfplot = ChartFactory.createXYBarChart(
                "Email History",
                "Monthly",
                true,
                "Number of Email",
                new TimeSeriesCollection(jfplot_series),
                PlotOrientation.VERTICAL,
                true,
                false,
                false
            );
       //customize it
        jfplot.setBackgroundPaint(Color.white);
        
        XYPlot plot = jfplot.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator generator = new StandardXYToolTipGenerator(
            "{1} = {2}", new SimpleDateFormat("MM-yyyy"), new DecimalFormat("0"));
        renderer.setToolTipGenerator(generator);
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        axis.setLowerMargin(0.01);
        axis.setUpperMargin(0.01);
        //end customize
        ChartPanel jfplotholder = new ChartPanel(jfplot);
        jfplotholder.setPreferredSize(new Dimension(400, 200));
		gridbag.setConstraints(jfplotholder, constraints);
		panel.add(jfplotholder);

		/*
		 * b1.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent evt) { initCombo("all");
		 * 
		 * }}); b2.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent evt) { initCombo("sender");
		 * //chooseUser(); }}); //.addActionListener(chooseUsers());
		 * b3.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent evt) {
		 * initCombo(userFreq.getText().trim()); //pass in threshold //
		 * chooseUser(); }}); //.addActionListener(chooseUsers());
		 * b2.setSelected(true);//trigger
		 */
		/*
		 * userFreq.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent evt) { chooseUser(); }});
		 */
		//addActionListener(chooseUsers());
		//replace inline new showPeriod());
		/*
		 * m_comboUsers.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e){ // try{ Object selected =
		 * m_comboUsers.getSelectedItem(); if (selected == null) return; curUser =
		 * (String)selected; //get global max min dates getAvailablePeriod();
		 * //reset the display adjustSelectionDate(); m_histogram.removeAll();
		 * m_histogram.repaint(); m_histogramBase.removeAll();
		 * m_histogramBase.repaint(); // } //catch (SQLException ex)//new
		 * showPeriod()); //{ //
		 * JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to get
		 * data for this account: " + ex); //} }});
		 * 
		 * if(m_comboUsers.getItemCount() >0) m_comboUsers.setSelectedIndex(0);
		 */
		//runButton.addActionListener(new histogramListener());
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected = (String)m_winGui.getSelectedUser();
				//m_comboUsers.getSelectedItem();
				if (selected == null)
					return;
				//curUser = (String) selected;
				//now to setup histogram
				showHistogram( selected, true);
				SocialCliques.setHistory( selected, true, jfplot_series, m_winGui, m_jdbcView);

			}
		});

		runAllButton.addActionListener(new runAllListener()); //version 2
		//helpButton.addActionListener(new helpListener());

		ListSelectionModel rowSM = m_AllUserDiff.getSelectionModel();
		rowSM.addListSelectionListener(this);

		setViewportView(panel);

	}

	/**
	 * Execute.
	 *
	 * @param curUSer the cur u ser
	 */
	public final void Execute(String curUSer) {
		if (curUSer == null)
			return;
		//curUser = selected;
		//get global max min dates
		//getAvailablePeriod(selected);
		//reset the display
		adjustSelectionDate(curUSer);
		m_histogram.removeAll();
		/*userplot.removeAll();
		userplot.repaint();*/
		jfplot_series.clear();
		m_histogram.repaint();
		m_histogramBase.removeAll();
		m_histogramBase.repaint();
		stdDev = null;

	}
	 
	
	/**
	 * Full number.
	 *
	 * @param number the number
	 * @return the string
	 */
	static String fullNumber(int number){
		switch (number) {
		case 9:
			return "09";
		case 8:
			return "08";
		case 7:
			return "07";
		case 6:
			return "06";
		case 5:
			return "05";
		case 4:
			return "04";
		case 3:
			return "03";
		case 2:
			return "02";
		case 1:
			return "01";
		case 0:
			return "00";
		default:
			return ""+number;
		}
	}
	
	
	
	
	
	/**
	 * SEt in motion the ability to profile a user over time.
	 *
	 * @param userName the user name
	 */
	public void studyCurrentUser(String userName) {

		//TODO: code here 

		//will fetch start and end dates from the wingui cache
		userShortProfile userStats = m_winGui.getUserInfo(userName);
		
		
		
		//Date startDate = new Date(userStats,getMinimumSize()'')
		Date startDate = Utils.deSqlizeDateYEAR(userStats.getStartDate());
		Date endDate = Utils.deSqlizeDateYEAR(userStats.getEndDate());
		GregorianCalendar startingPoint = new GregorianCalendar();
		startingPoint.setTime(startDate);
		startingPoint.add(Calendar.DAY_OF_MONTH,-1); //want to have the day before for the loop later
		GregorianCalendar endPoint = new GregorianCalendar();
		endPoint.setTime(endDate);
		

		System.out.println(startingPoint.getTime().toString());
		
		endPoint.add(Calendar.DAY_OF_MONTH,1);//move up a day

		System.out.println(endPoint.getTime().toString());
		try{
		getProfileAndDisplay(userName, userStats.getStartDate(), userStats.getEndDate(),false);
		}catch(SQLException se){
			se.printStackTrace();
		}
		//System.out.println(" the number of days between the start and end:" + userStats.daysBetween(startDate,endDate));
		//TODO: allow overlapping profiles
		
		//divide the profile into pieces
		
		//simple loop back up the first of the month on a calendar, and 
		//ArrayList<String> datesToCover = new ArrayList<String>();
		
		ArrayList<double[]> datalist = new ArrayList<double[]>();
		BusyWindow BW = new BusyWindow("Study User Profile","Running",false);
		BW.setVisible(true);
		String startDatePeriod = userStats.getStartDate();
		int monthcount =0;
		//lets loop and grab each month section, backing up...
		while(startingPoint.before(endPoint)){
			String starttime = startingPoint.get(Calendar.YEAR)+"-"+fullNumber(1+startingPoint.get(Calendar.MONTH))+"-"+fullNumber(startingPoint.get(Calendar.DAY_OF_MONTH));
			
			startingPoint.add(Calendar.MONTH,1);
			
			String endtime =startingPoint.get(Calendar.YEAR)+"-"+fullNumber(1+startingPoint.get(Calendar.MONTH))+"-"+fullNumber(startingPoint.get(Calendar.DAY_OF_MONTH));
			 try{
				 
				 /*System.out.println(endtime);*/
				//TODO: fix this...too many checks on the same stuff burried in system calls here...
			     datalist.add(getHistogram(userName,starttime,endtime,false));
			     
			     
			     System.out.print (" done: " + monthcount++);
				 
			     
			     
			//TODO: gethistorgram for the perdio
			 }catch(SQLException se){
				 se.printStackTrace();
				// System.out.println("oops:"+starttime + "to " + endtime + " end loop:" + se );
			 }
			
			
			
		}//end while
		
		BW.setVisible(false);
		
		System.out.println("Its a wrap");
		//create a window
		JFrame testwindow = new JFrame();//UsageHistogram.this);
		
		
		testwindow.getContentPane().add(new overviewHistoryPanel(datalist,monthcount,startDatePeriod,stdDev));
		
		testwindow.pack();
		
		testwindow.setVisible(true);
		
		//calculat the first period
		
		//for loop comparing n to n+1

	}

	/**
	 * does the historgram for the selected user.
	 *
	 * @param curUser the cur user
	 * @param show the show
	 */
	public void showHistogram(final String curUser, final boolean show) {
		(new Thread(new Runnable() {
			BusyWindow bw = null;

			public void run() {
				try {

					if (show) {
						bw = new BusyWindow("Working", "progress",false);
						bw.setVisible(true);
					}
					label.setVisible(true);
					label1.setVisible(true);
					panResult.setVisible(true);
					h.setVisible(true);
					h1.setVisible(true);
					alertLabel.setVisible(true);
					sPane.setVisible(false);

					//moved to higher up the get current user.
					//getAvailablePeriod(curUser);

					//compute and show the histograms
					if (show)
						m_winGui.setEnabled(false);
					String profileStartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelStart.getDate());
					String profileEndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelEnd.getDate());
					String StartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
					String EndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());

					recentHistogram = getHistogram(curUser, StartDate, EndDate,true);
					draw_histogram(m_histogram, recentHistogram);
					getProfileAndDisplay(curUser, profileStartDate, profileEndDate,true);
					if (show)
						m_winGui.setEnabled(true);
					showDifference(curUser, StartDate, EndDate, profileStartDate, profileEndDate);
					if (show) {
						if (bw != null)
							bw.setVisible(false);
					}
					//panel.repaint();
				} catch (SQLException ex) {
					if (show) {
						JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to compute for one user: " + ex);
						if (bw != null) {
							bw.setVisible(false);
						}
						m_winGui.setEnabled(true);
					}
				}

			}
		})).start();
	}

	//this will gernerate a report
	/**
	 * Make report.
	 */
	public void MakeReport() {
		//first refresh user list of fre user

		Object selected = m_winGui.getSelectedUser();
		if (selected != null)
			showHistogram((String) selected, true);

		//
		int n = m_winGui.getUserCount();

		if (n == 0)
			return;
		BusyWindow bw = new BusyWindow("useagehist report", "Progress",true);
		bw.progress(0, n);
		m_winGui.setEnabled(false);

		try {
			//initCombo(userFreq.getText().trim());

			bw.setVisible(true);
			for (int i = 0; i < n; i++) {
				bw.progress(i, n);

				m_winGui.setUserSelectedIndex(i);
				String curUser = m_winGui.getUserAt(i);
				//showHistogram(false);
				//getAvailablePeriod(curUser);
				adjustSelectionDate(curUser);
				String profileStartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelStart.getDate());
				String profileEndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelEnd.getDate());
				String StartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
				String EndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());

				getHistogram(curUser, StartDate, EndDate,true);
				getProfileAndDisplay(curUser, profileStartDate, profileEndDate,true);
				showDifference(curUser, StartDate, EndDate, profileStartDate, profileEndDate);
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(UsageHistogram.this, "oops" + e, "Error", JOptionPane.ERROR_MESSAGE);

		}
		if (bw != null) {
			bw.setVisible(false);
		}
		m_winGui.setEnabled(true);
		//now we want to run through each user and call the analysis part.
		//if unusual we will be making reports.

	}

	//for table selection
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent event) {
		//switch the display
		label.setVisible(true);
		label1.setVisible(true);
		panResult.setVisible(true);
		h.setVisible(true);
		h1.setVisible(true);
		alertLabel.setVisible(true);

		int row = m_AllUserDiff.getSelectedRow();
		if (row == -1)
			return;

		String value = (String) m_AllUserDiff.getValueAt(row, 1);
		if (value.equals("N/A")) { //period doesn't fit, return
			System.out.println("N/A");
			m_histogram.removeAll();
			m_histogram.repaint();
			m_histogramBase.removeAll();
			m_histogramBase.repaint();
			return;
		}

		String curUser = (String) m_AllUserDiff.getValueAt(row, 0);

		try {
			//getAvailablePeriod(curUser);
			String profileStartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelStart.getDate());
			String profileEndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelEnd.getDate());
			String StartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
			String EndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());

			getHistogram(curUser, StartDate, EndDate,true);
			getProfileAndDisplay(curUser, profileStartDate, profileEndDate,true);
			showDifference(curUser, StartDate, EndDate, profileStartDate, profileEndDate);
			//System.out.println("working...");
		} catch (SQLException e) {
			System.out.println("Exception when click on table" + e);
		}

		panel.repaint();

	}

	/*
	 * //for the radio button. switch between all user and sender public final
	 * void chooseUser() { //class chooseUsers implements ActionListener{
	 * //public void actionPerformed(ActionEvent e){ try{ if(b1.isSelected())
	 * 
	 * else if(b2.isSelected())
	 * 
	 * else if(b3.isSelected())
	 *  } catch (SQLException ex) {
	 * JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to get data
	 * for account: " + ex); } }
	 */

	/*
	 * //for the m_comboUser class showPeriod implements ActionListener{ public
	 * void actionPerformed(ActionEvent e){ try{ Object selected =
	 * m_comboUsers.getSelectedItem(); if (selected == null) return; curUser =
	 * (String)selected; //get global max min dates getAvailablePeriod();
	 * //reset the display adjustSelectionDate(); m_histogram.removeAll();
	 * m_histogram.repaint(); m_histogramBase.removeAll();
	 * m_histogramBase.repaint(); } catch (SQLException ex) {
	 * JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to get data
	 * for this account: " + ex); } }
	 *  }
	 */

	/*
	 * //for run botton class histogramListener implements ActionListener{
	 * public void actionPerformed(ActionEvent e){ (new Thread(new Runnable() {
	 * public void run(){ try { label.setVisible(true); label1.setVisible(true);
	 * panResult.setVisible(true); h.setVisible(true); h1.setVisible(true);
	 * alertLabel.setVisible(true); sPane.setVisible(false);
	 * 
	 * Object selected = m_comboUsers.getSelectedItem(); if (selected == null)
	 * return; curUser = (String)selected;
	 * 
	 * getAvailablePeriod();
	 * 
	 * //compute and show the histograms
	 * 
	 * m_winGui.setEnabled(false); getHistogram(); getProfile();
	 * m_winGui.setEnabled(true); showDifference();
	 * 
	 * panel.repaint(); } catch (SQLException ex) {
	 * JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to compute for
	 * one user: " + ex); m_winGui.setEnabled(true); }
	 *  } })).start();
	 *  } }
	 */
	//for run all button
	/**
	 * The listener interface for receiving runAll events.
	 * The class that is interested in processing a runAll
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addrunAllListener<code> method. When
	 * the runAll event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see runAllEvent
	 */
	class runAllListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			(new Thread(new Runnable() {
				public void run() {
					try {
						m_histogram.removeAll();
						m_histogram.repaint();
						m_histogramBase.removeAll();
						m_histogramBase.repaint();

						label.setVisible(false);
						label1.setVisible(false);
						panResult.setVisible(false);
						h.setVisible(false);
						h1.setVisible(false);
						alertLabel.setVisible(false);
						sPane.setVisible(true);

						m_winGui.setEnabled(false);
						computeAllUserDiff();
						m_winGui.setEnabled(true);
						panel.repaint();
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to compute for all user: " + ex);
						m_winGui.setEnabled(true);
					}

				}
			})).start();

		}
	}

	/*
	 * //for helpButton class helpListener implements ActionListener{ public
	 * void actionPerformed(ActionEvent e){
	 * 
	 * String message = EMTHelp.getUsageHistogram(); String title = "Help";
	 * DetailDialog dialog = new DetailDialog(null, false, title, message);
	 * dialog.setLocationRelativeTo(UsageHistogram.this); dialog.show(); } }
	 */

	/**
	 * for the selected user, find the min and max date for availabe data.
	 *
	 * @param curUser the cur user
	 */
	/*private void getAvailablePeriod()// throws SQLException
	 {
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
	 * query = "select max(dates), min(dates),count(*) from email where
	 * sender='"+curUser+"' "; m_jdbcView.getSqlData(query); data =
	 * m_jdbcView.getRowData();
	 * 
	 * if(data.length!=1 && data[0].length!=2){ System.err.println("select
	 * dates error"); throw new SQLException("Wrong number of cells"); }
	 * 
	 * maxAvailableDate = (String)data[0][0]; minAvailableDate =
	 * (String)data[0][1]; countAvailable = data[0][2]; }
	 */
	/*		String data[] = m_winGui.getUserInfo(curUser);
	 if (data != null) {

	 maxAvailableDate = data[0];
	 minAvailableDate = data[1];
	 countAvailable = data[2];
	 } else {
	 maxAvailableDate = "";
	 minAvailableDate = "";
	 countAvailable = "";
	 }

	 } //end of getAvailabeData

	 */
	//display available date and adjust the period selection
	void adjustSelectionDate(String curUser) {
		userShortProfile data = m_winGui.getUserInfo(curUser);
		String maxAvailableDate, minAvailableDate;
		int countAvailable;
		if (data != null) {

			maxAvailableDate = data.getEndDate();//[winGui.User_max];
			minAvailableDate = data.getStartDate();//[winGui.User_min];
			countAvailable = data.getTotalCount();//[winGui.User_count];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			countAvailable = 0;
		}

		periodLabel.setText(countAvailable + " data available from " + minAvailableDate + " to " + maxAvailableDate);

		//periodLabel.repaint();

		// set the time selection according to the min and max date
		Date maxdt = Utils.deSqlizeDateYEAR(maxAvailableDate);
		Date mind = Utils.deSqlizeDateYEAR(minAvailableDate);
		if (maxdt != null) {
			String start, end;

			m_spinnerModelRecentEnd.setValue(maxdt);

			Calendar cal = new GregorianCalendar();
			cal.setTime(maxdt);

			int recentchoice = this.recentp.getSelectedIndex();

			if (recentchoice == RECENTWEEK) {
				cal.add(Calendar.DATE, -7);
			} else if (recentchoice == RECENT2WEEK) {
				cal.add(Calendar.DATE, -14);
			} else if (recentchoice == RECENTMONTH) {
				cal.add(Calendar.MONTH, -1);
			} else {
				cal.add(Calendar.MONTH, -2);

			}

			m_spinnerModelRecentStart.setValue(cal.getTime());
			start = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
			if (start.compareTo(minAvailableDate) < 0) //earlier than min
				m_spinnerModelRecentStart.setValue(mind);

			cal.add(Calendar.DATE, -1);
			//m_spinnerModelEnd.setValue(cal.getTime());

			//			cal.add(Calendar.MONTH, 1);
			m_spinnerModelEnd.setValue(cal.getTime());
			end = Utils.mySqlizeDate2YEAR(m_spinnerModelEnd.getDate());
			if (end.compareTo(maxAvailableDate) > 0)
				m_spinnerModelEnd.setValue(maxdt);

			int modelch = this.profileh.getSelectedIndex();

			if (modelch == PROFILEMONTH) {
				cal.add(Calendar.MONTH, -1);
			} else if (modelch == PROFILE2MONTH) {
				cal.add(Calendar.MONTH, -2);
			} else if (modelch == PROFILE3MONTH) {
				cal.add(Calendar.MONTH, -3);
			}

			//TODO: set for fill if modelch == full

			//cal.add(Calendar.MONTH, -1);
			m_spinnerModelStart.setValue(cal.getTime());
			start = Utils.mySqlizeDate2YEAR(m_spinnerModelStart.getDate());
			if (start.compareTo(minAvailableDate) < 0) { //earlier than min
				m_spinnerModelStart.setValue(mind);
				cal.setTime(mind);
			}

		}

	}

	/**
	 * Fetch the recently usage histogram according to user's selection.
	 *
	 * @param curUser name of the user
	 * @param startDate beginning period
	 * @param endDate end period
	 * @param shouldThrow if we should really throw an exception
	 * @return the histogram
	 * @throws SQLException the sQL exception
	 */
	private double[] getHistogram(String curUser, String startDate, String endDate,boolean shouldThrow) throws SQLException {
		
		//get the users info
		userShortProfile Userdata = m_winGui.getUserInfo(curUser);
		String maxAvailableDate, minAvailableDate;//,countAvailable;
		if (Userdata != null) {

			maxAvailableDate = Userdata.getEndDate();//[winGui.User_max];
			minAvailableDate = Userdata.getStartDate();//[winGui.User_min];
			//countAvailable = data[2];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			//countAvailable = "";
		}/*
		 * Object selected = m_comboUsers.getSelectedItem(); if (selected ==
		 * null) return; curUser = (String)selected;
		 */

		/*System.out.println("startdate:"+startDate);//+", min
		// date:"+minAvailableDate);
		System.out.println("enddate:"+endDate);//+", max
		*/// date:"+maxAvailableDate);
		if ((startDate.compareTo(endDate)) > 0) {
			throw (new SQLException("start date is larger than end date"));
		}
		if (shouldThrow && startDate.compareTo(minAvailableDate) < 0 ) {
			throw (new SQLException("recent start date is too early"));
		}
		if (shouldThrow && endDate.compareTo(maxAvailableDate) > 0 ) {
			//System.out.println("endDate is: "+endDate);
			//System.out.println("max date: "+maxAvailableDate);

			throw (new SQLException("recent end date is too late"));
			//endDate=maxAvailableDate; //loose the requirement
		}

		//get from db
		//recentHistogram = hc.getHistogram(curUser,period);
		return hc.getHistogram(curUser, m_winGui.getUserInfo((curUser)), startDate, endDate);
	}

	/**
	 * Gets the histogram.
	 *
	 * @param curUser the cur user
	 * @param desUser the des user
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return the histogram
	 * @throws SQLException the sQL exception
	 */
	private double[] getHistogram(String curUser, String desUser, String startDate, String endDate) throws SQLException {
		userShortProfile data = m_winGui.getUserInfo(curUser);
		String maxAvailableDate, minAvailableDate;//,countAvailable;
		if (data != null) {

			maxAvailableDate = data.getEndDate();//[winGui.User_max];
			minAvailableDate = data.getStartDate();//[winGui.User_min];
			//countAvailable = data[2];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			//countAvailable = "";
		}/*
		 * Object selected = m_comboUsers.getSelectedItem(); if (selected ==
		 * null) return; curUser = (String)selected;
		 */

		//System.out.println("startdate:"+startDate+", min
		// date:"+minAvailableDate);
		//System.out.println("enddate:"+endDate+", max
		// date:"+maxAvailableDate);
		if ((startDate.compareTo(endDate)) > 0) {
			throw (new SQLException("start date is larger than end date"));
		}
		if (startDate.compareTo(minAvailableDate) < 0) {
			throw (new SQLException("recent start date is too early"));
		}
		if (endDate.compareTo(maxAvailableDate) > 0) {
			//System.out.println("endDate is: "+endDate);
			//System.out.println("max date: "+maxAvailableDate);

			throw (new SQLException("recent end date is too late"));
			//endDate=maxAvailableDate; //loose the requirement
		}

		//get from db
		//recentHistogram = hc.getHistogram(curUser,period);
		return hc.getHistogram(curUser, desUser, m_winGui.getUserInfo((curUser)), startDate, endDate);
	}

	// show the plot using Histogram
	/**
	 * Draw_histogram.
	 *
	 * @param plot the plot
	 * @param Histogram the histogram
	 */
	void draw_histogram(JPlot2D plot, double[] Histogram) {

		plot.removeAll();

		//double[] s = new double[2];
		//s[0]=1; s[1]=24;

		//double[] y = new double[Histogram.length];

		//for (int i = 0; i < Histogram.length; i++) {
		////x[i]=i+1;
		//y[i] = ((Double) Histogram.elementAt(i)).doubleValue();
		//}

		plot.setXLabel("time");
		plot.setYLabel("avg email sent");
		plot.setXScale(Utils.s1by24);
		plot.addCurve(Utils.x_24, Histogram);
		plot.repaint();
	}

	// show the histogram from profile, ie, normal usage
	/**
	 * Gets the profile and display.
	 *
	 * @param curUser the cur user
	 * @param profileStartDate the profile start date
	 * @param profileEndDate the profile end date
	 * @param display the display
	 * @return the profile and display
	 * @throws SQLException the sQL exception
	 */
	private void getProfileAndDisplay(String curUser, String profileStartDate, String profileEndDate, boolean display)
			throws SQLException {

		if (curUser == null || curUser.equals(""))
			return;

		userShortProfile data = m_winGui.getUserInfo(curUser);
		String maxAvailableDate, minAvailableDate;//,countAvailable;
		if (data != null) {

			maxAvailableDate = data.getEndDate();//[winGui.User_max];
			minAvailableDate = data.getStartDate();//[winGui.User_min];
			//countAvailable = data[2];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			//countAvailable = "";
		}

		//System.out.println("profile start: "+profileStartDate+", profile end:
		// "+profileEndDate);
		if ((profileStartDate.compareTo(profileEndDate)) > 0) {
			throw (new SQLException("start date is larger than end date"));
		}

		if (profileStartDate.compareTo(minAvailableDate) < 0) {
			throw (new SQLException("profile start date is too early"));
		}
		if (profileEndDate.compareTo(maxAvailableDate) > 0) {
			//throw (new SQLException("profile end date is too late"));
			profileEndDate = maxAvailableDate;
		}

		if (!profiles.containsKey(curUser)) {
			profileHistogram = hc.getHistogramWithDev(curUser, m_winGui.getUserInfo((curUser)), profileStartDate,
					profileEndDate);
			stdDev = hc.get_stdDev();
			UserProfile up = new UserProfile(curUser, profileStartDate, profileEndDate, profileHistogram, stdDev);
			profiles.put(curUser, up);
		} else {
			UserProfile up = (UserProfile) profiles.get(curUser);
			String start = up.getStart();
			String end = up.getEnd();

			//same user and same period
			if (start.equalsIgnoreCase(profileStartDate) && end.equalsIgnoreCase(profileEndDate)) {
				profileHistogram = up.getHistogram();
				stdDev = up.getstdDev();
				//System.out.println("******get from hash table***:
				// "+curUser+", "+stdDev);
			} else {
				profileHistogram = hc.getHistogramWithDev(curUser, m_winGui.getUserInfo((curUser)), profileStartDate,
						profileEndDate);
				stdDev = hc.get_stdDev();
				up = new UserProfile(curUser, profileStartDate, profileEndDate, profileHistogram, stdDev);
				profiles.put(curUser, up);
			}
		}
if(display)
		draw_histogram(m_histogramBase, profileHistogram);
		/*
		 * // show the plot m_histogramBase.removeAll();
		 * 
		 * 
		 * double[] s = new double[2]; s[0]=1; s[1]=24; double[] x = new
		 * double[24]; double[] y = new double[24];
		 * 
		 * for(int i=0;i <24;i++){ x[i]=i+1;
		 * y[i]=((Double)profileHistogram.elementAt(i)).doubleValue(); }
		 * 
		 * m_histogramBase.setXLabel("time"); m_histogramBase.setYLabel("avg
		 * emails sent"); m_histogramBase.setXScale(s);
		 * m_histogramBase.addCurve(x,y);
		 * 
		 * m_histogramBase.repaint();
		 */
	}

	// show the difference between normal one and recent one
	/**
	 * Show difference.
	 *
	 * @param curUser the cur user
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param profileStartDate the profile start date
	 * @param profileEndDate the profile end date
	 */
	private void showDifference(String curUser, String startDate, String endDate, String profileStartDate,
			String profileEndDate) {
		userShortProfile data = m_winGui.getUserInfo(curUser);
		String maxAvailableDate, minAvailableDate;
		int countAvailable;
		if (data != null) {

			maxAvailableDate = data.getEndDate();//[winGui.User_max];
			minAvailableDate = data.getStartDate();//[winGui.User_min];
			countAvailable = data.getTotalCount();//[winGui.User_count];
		} else {
			maxAvailableDate = "";
			minAvailableDate = "";
			countAvailable = 0;
		}
		//DistanceCompute DC = new DistanceCompute();
//WHAT IF STD DEV IS NULL??
		final String method = "mahalanobis";
		double distance = DistanceCompute.getDistance(profileHistogram, recentHistogram, stdDev, method);
		//test
		/*
		 * System.out.println("......, for user: "+curUser);
		 * System.out.println("profile"+profileHistogram.toString());
		 * System.out.println("recnt:"+recentHistogram);
		 * System.out.println("stddev"+stdDev);
		 * System.out.println("distance:"+distance);
		 */
		m_difference.setText("" + distance);

		int level = m_alertLevel.getValue();
		getAlertThreshhold(level);
		String alerts = getAlertString(distance);

		int setAlertLevel;// put into the whole alert log
		String brief = ""; // the brief description for the alert log
		String detailed = "";// the detailed description

		if (alerts.equalsIgnoreCase("high")) {
			alertLabel.setForeground(Color.red);
			alertLabel.setText("Alert: Recent Behavior Is Abnormal!!");

			setAlertLevel = Alert.ALERTLEVEL_RED;
			brief = "Alert: Recent behavior of \"" + curUser + "\" is abnormal!";
			detailed = "Behavior abnormal!! \n" + "User: " + curUser + "\n";
			detailed += "recent period is: " + startDate + " -- " + endDate + "\n";
			detailed += "profile period is: " + profileStartDate + " -- " + profileEndDate + "\n";
			detailed += "distance is: " + distance;
			m_alerts.alert(setAlertLevel, brief, detailed);
		} else if (alerts.equalsIgnoreCase("middle")) {
			alertLabel.setForeground(Color.yellow);
			alertLabel.setText("Recent Behavior might be abnormal!!");

			setAlertLevel = Alert.ALERTLEVEL_YELLOW;
			brief = "Alert: Recent behavior of \"" + curUser + "\" might be abnormal!";
			detailed = "Behavior might be abnormal. \n" + "User: " + curUser + "\n";
			detailed += "recent period is: " + startDate + " -- " + endDate + "\n";
			detailed += "profile period is: " + profileStartDate + " -- " + profileEndDate + "\n";
			detailed += "distance is: " + distance;
			m_alerts.alert(setAlertLevel, brief, detailed);
		} else {
			alertLabel.setForeground(Color.blue);
			alertLabel.setText("Behavior is Normal");
		}

		m_difference.repaint();
		//alertLabel.repaint();

	}

	//calculate the threshhold for high, middle, safe
	/**
	 * Gets the alert threshhold.
	 *
	 * @param level the level
	 * @return the alert threshhold
	 */
	private void getAlertThreshhold(int level) {

		double danger_high = 2.0; // for the highest alert level, if
		// distance>=6, it's dangerous, return "high"
		double safe_high = 1.0; // for the highest alert level, if distance <=2,
		// it's normal, return "low". "middle" just be
		// median of high and low

		double danger_low = 6.0;
		double safe_low = 3.0;

		double danger = danger_low - ((danger_low - danger_high) / 10) * level; //the
		// danger
		// number
		// for
		// this
		// level
		double safe = safe_low - ((safe_low - safe_high) / 10) * level; //the
		// danger
		// number
		// for
		// this
		// level

		//System.out.println("For usage histogram, level is:"+level+", danger
		// is:"+danger+", safe is:"+safe);

		m_highalert = danger;
		//	m_middlealert = (danger+safe)/2;
		m_safe = safe;
	}

	// decide the alert striing should be high, middle or low
	/**
	 * Gets the alert string.
	 *
	 * @param distance the distance
	 * @return the alert string
	 */
	private String getAlertString(double distance) {

		if (distance >= m_highalert)
			return "high";
		if (distance <= m_safe)
			return "low";

		//	if(distance>=m_middlealert) return "middle";

		return "middle";
	}

	/*
	 * private void initCombo(String arg) //throws SQLException { //populate
	 * String data[]; try{ m_comboUsers.removeAllItems();
	 * 
	 * 
	 * synchronized(m_jdbcView){ if(arg.equalsIgnoreCase("all")){ //will fetch
	 * cached copy from wingui. //m_jdbcView.getSqlData("select distinct sender
	 * from email order by sender"); //data = m_jdbcView.getColumnData(0); data =
	 * (String[])m_winGui.getUserList(); }
	 * 
	 * else if(arg.equalsIgnoreCase("sender")){ m_jdbcView.getSqlData("select
	 * distinct sender from email group by sender having count(*) > 200");
	 * //m_jdbcUpdate.getSqlData("select distinct sender from email where folder
	 * like '%sent%' order by sender"); data = m_jdbcView.getColumnData(0); }
	 * 
	 * else{ //frequent threshold m_jdbcView.getSqlData("select distinct sender
	 * from email group by sender having count(*) > '"+arg+"'"); data =
	 * m_jdbcView.getColumnData(0); /* String query="create temporary table
	 * CountTemp (sender varchar(122), count int)";
	 * m_jdbcUpdate.executeQuery(query);
	 * 
	 * System.out.println("create OK");
	 * 
	 * try{ query ="insert into CountTemp "; query+="select "; query+="sender,
	 * count(*) "; query+="from email "; query+="group by sender";
	 * 
	 * System.out.println("insert: "+query); m_jdbcUpdate.executeQuery(query);
	 * System.out.println("....done");
	 * 
	 * query="select sender "; query+="from CountTemp where count>="+arg;
	 * 
	 * System.out.println("insert: "+query); m_jdbcUpdate.getSqlData(query);
	 * System.out.println("****"); data = m_jdbcUpdate.getRowData();
	 * System.out.println("====="); } finally{ m_jdbcUpdate.executeQuery("drop
	 * table CountTemp"); } / } } } catch (SQLException ex) {
	 * JOptionPane.showMessageDialog(UsageHistogram.this, "Failed to get data
	 * for account: " + ex); return; }
	 * 
	 * 
	 * 
	 * ComboBoxModel model = new DefaultComboBoxModel(data);
	 * m_comboUsers.setModel(model);
	 * 
	 * if(m_comboUsers.getItemCount() >=1) m_comboUsers.setSelectedIndex(0);
	 *  /* for(int i=0;i <data.length;i++){
	 * 
	 * if(data[i].length!=1){ throw new SQLException("Database returned wrong
	 * number of columns"); }
	 * 
	 * if(data[i][0].length() >0){ //non-null sender name
	 * //m_comboUsers.insertItemAt((String)data[i][0],i);
	 * m_comboUsers.addItem((String)data[i][0]);
	 * m_hsUsers.add((String)data[i][0]);
	 * 
	 * if(m_comboUsers.getItemCount() ==1) m_comboUsers.setSelectedIndex(0); } }
	 * //for /
	 *  }
	 *  
	 */
	// compute and show the diff for all user using the selected periods
	// diff is N/A is the user doesn't have data for that period
	/**
	 * Compute all user diff.
	 *
	 * @throws SQLException the sQL exception
	 */
	private void computeAllUserDiff() throws SQLException {

		int len = m_winGui.getUserCount();
		//m_comboUsers.getItemCount();
		//int curr = m_comboUsers.getSelectedIndex();

		//DistanceCompute DC = new DistanceCompute();
		String method = "mahalanobis";
		double distance;
		DefaultTableModel model = (DefaultTableModel) m_AllUserDiff.getModel();
		String[] tt = new String[2];
		(m_AllUserDiff.getColumn("difference")).setCellRenderer(m_diffColumnRenderer);

		int level = m_alertLevel.getValue();
		getAlertThreshhold(level);

		// clear table
		model.setRowCount(0);

		//int length = model.getRowCount();
		//for(int i=length-1;i>=0;i--)
		//   model.removeRow(i);
		String profileStartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelStart.getDate());
		String profileEndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelEnd.getDate());
		String StartDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
		String EndDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());

		String userList[] = m_winGui.getUserList();
		BusyWindow bw = new BusyWindow(this, "Calculating histogram difference", "Progress report",true);
		bw.progress(0, len);
		bw.setVisible(true);
		for (int i = 0; i < len; i++) {
			bw.progress(i, len);
			String curUser = userList[i];
			tt[0] = curUser;
			try {
				//getAvailablePeriod(curUser);
				recentHistogram = getHistogram(curUser, StartDate, EndDate,true);
				draw_histogram(m_histogram, recentHistogram);
				getProfileAndDisplay(curUser, profileStartDate, profileEndDate,true);
				/*
				 * System.out.println("In table, for user: "+curUser);
				 * System.out.println("profile"+profileHistogram.toString());
				 * System.out.println("recnt:"+recentHistogram);
				 * System.out.println("stddev"+stdDev);
				 */distance = DistanceCompute.getDistance(profileHistogram, recentHistogram, stdDev, method);
				tt[1] = (new Double(distance)).toString();

			} catch (Exception se) {
				tt[1] = "N/A";
			}
			model.addRow(tt);

		}//for
		bw.setVisible(false);
	}

	/*
	 * public void refreshCombo() throws SQLException { // remember current
	 * selection String strCurrent = new String(""); Object objCurrent =
	 * m_winGui.getSelectedUser(); // m_comboUsers.getSelectedItem(); if
	 * (objCurrent != null) { strCurrent = (String) objCurrent; }
	 *  // repopulate String data[][]; synchronized(m_jdbcUpdate) {
	 * m_jdbcUpdate.getSqlData("select distinct sender from email order by
	 * sender"); data = m_jdbcUpdate.getRowData(); }
	 * 
	 * for (int i=0; i <data.length; i++) { if (data[i].length != 1) { throw new
	 * SQLException("Database returned wrong number of columns"); } if
	 * (data[i][0].length() > 0) { if (m_hsUsers.add(data[i][0])) { int j=0;
	 * while ((j < m_comboUsers.getItemCount()) &&
	 * (data[i][0].compareToIgnoreCase((String) m_comboUsers.getItemAt(j)) > 0)) {
	 * j++; } m_comboUsers.insertItemAt((String) data[i][0], j); if
	 * (m_comboUsers.getItemCount() == 1) { m_comboUsers.setSelectedIndex(0); } } }
	 *  // replace selection if (strCurrent.equals((String)data[i][0])) {
	 * m_comboUsers.setSelectedItem((String)data[i][0]); } }//end of for }
	 */

	/**
	 * Gets the usage profile chart.
	 *
	 * @param user the user
	 * @return the usage profile chart
	 */
	public final double[] getUsageProfileChart(String user) {
		try {
			userShortProfile data = m_winGui.getUserInfo(user);
			if (data == null) {
				return null;
			}
			//String maxAvailableDate = data.getEndDate();//[winGui.User_max];
			//String minAvailableDate = data.getStartDate();//[winGui.User_min];

			return getHistogram(user, data.getStartDate(), data.getEndDate(),true);

		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}

	}

	/**
	 * Gets the usage profile chart.
	 *
	 * @param user1 the user1
	 * @param user2 the user2
	 * @return the usage profile chart
	 */
	public final double[] getUsageProfileChart(String user1, String user2) {
		try {
			userShortProfile data = m_winGui.getUserInfo(user1);
			//String maxAvailableDate = data[winGui.User_max];
			//String minAvailableDate = data[winGui.User_min];

			return getHistogram(user1, user2, data.getStartDate(), data.getEndDate());

		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}

	}

	/**
	 * Refresh.
	 *
	 * @throws SQLException the sQL exception
	 */
	public void Refresh() throws SQLException {
		//refreshCombo();
	}

}

class UserProfile {

	String user;

	String startDate;

	String endDate;

	double[] histogram;

	double[] stdDev;

	public UserProfile(String s, String start, String end, double[] h, double[] std) {

		user = s;
		startDate = start;
		endDate = end;
		histogram = new double[h.length];
		stdDev = new double[std.length];
		for (int i = 0; i < h.length; i++) {
			histogram[i] = h[i];
		}
		for (int i = 0; i < std.length; i++) {
			stdDev[i] = std[i];
		}

		//new Vector();//(Vector)h.clone();
		//copyVector(h, histogram);

	}

	/*
	 private final void copyVector(Vector from, Vector to) {
	 Object []fromcopy = from.toArray();
	 
	 int size = from.size();
	 for (int i = 0; i < size; i++) {
	 to.addElement(fromcopy[i]);//from.elementAt(i));
	 }
	 }
	 */
	public final String getStart() {
		return startDate;
	}

	public final String getEnd() {
		return endDate;
	}

	public final double[] getHistogram() {
		return histogram;
	}

	public final double[] getstdDev() {
		return stdDev;
	}
}