

package metdemo.CliqueTools;



//import javax.swing.JSpinner.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

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
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;
import metdemo.dataStructures.userShortProfile;

import chapman.graphics.JPlot2D;

/** 
 * This window allows a graphical look at clique building 
 *@author Shlomo Hershkop
 *fall 202
*/



public class SocialCliques extends JScrollPane//JPanel
{

    private EMTDatabaseConnection m_jdbc;
    private JTextArea m_textArea;
    private JTextField m_threshold;
    private SpinnerDateModel m_spinnerModelStart;
    private SpinnerDateModel m_spinnerModelEnd;
    private SpinnerDateModel m_spinnerModelTStart; //test part
    private SpinnerDateModel m_spinnerModelTEnd;
    private long before,after;//runing time testing
    private JSpinner spinnerStart,spinnerEnd, spinnerTStart,spinnerTEnd;
    private JTable m_table;
    private DefaultTableModel dtmodel;
    //private JPlot2D userplot;
   
    private TimeSeries jfplot_series = new TimeSeries("Emails", "Month-Year", "Count", Month.class);
    private JRadioButton b_autoCompute,b_manualCompute;
    
    
    JTextField UInfo;
    private winGui m_wg;

    public SocialCliques(final EMTDatabaseConnection jdbc, final String aliasFilename, final winGui wingui) throws Exception {
        
        
        
        GridBagLayout gridbag;
        GridBagConstraints constraints;
        
        m_wg = wingui;
        m_jdbc = jdbc;
        JPanel panel = new JPanel();
        gridbag = new GridBagLayout();
        constraints = new GridBagConstraints();
        panel.setLayout(gridbag);
        UInfo = new JTextField("");

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
        
        
        
        
        //need to initilaize the jplot2d
        /*userplot = new JPlot2D();
        userplot.setPlotType(JPlot2D.BAR);
        userplot.setBackgroundColor(Color.white);
        userplot.setFillColor(Color.blue);
        userplot.setGridState(JPlot2D.GRID_ON);
        userplot.setPreferredSize(new Dimension(400, 200));
*/
        //int elements=0;
        //m_userCombo = new JComboBox();
        /*
         * m_userCombo.addActionListener(new ActionListener() { public void
         * actionPerformed(ActionEvent evt) { updateInfo();
         *  } });
         */
        //String data[][];
        //updateUserList();
        //now to do for fisrt
        /*
         * try{ synchronized (m_jdbc) {
         * 
         * m_jdbc.getSqlData("select distinct sender from email order by
         * sender"); data = m_jdbc.getRowData(); } m_userCombo.removeAllItems();
         * for(int i=0;i <data.length;i++) {
         * m_userCombo.addItem(data[i][0].trim()); } }catch(Exception
         * e){System.out.println("problem in social cliques: " + e);}
         */
        //Dimension D = m_userCombo.getPreferredSize();
        //D.width = D.width * 2;
        //m_userCombo.setPreferredSize(D);//new Dimension(30,25));

        //setEditable(true);
        //JButton Refresh = new JButton("Refresh Users");
        //Refresh.setToolTipText("Refresh User List");
        //Refresh.addActionListener(new ActionListener()
        //   {
        //	public void actionPerformed(ActionEvent evt)
        //	{
        //	    updateUserList();
        //	
        //	}
        //   });

        m_spinnerModelStart = new SpinnerDateModel();
        m_spinnerModelEnd = new SpinnerDateModel();
        m_spinnerModelTStart = new SpinnerDateModel();
        m_spinnerModelTEnd = new SpinnerDateModel();

        JLabel labStart = new JLabel("Start Training");
        labStart
                .setToolTipText("This part chooses the dates to create our cliques from");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(3, 3, 3, 3);
        gridbag.setConstraints(labStart, constraints);
        panel.add(labStart);

        GregorianCalendar cal = new GregorianCalendar(1999, 1, 1);
        //cal.add(Calendar.MONTH, -1);
        m_spinnerModelStart.setValue(cal.getTime());
        spinnerStart = new JSpinner(m_spinnerModelStart);
        JSpinner.DateEditor editorStart = new JSpinner.DateEditor(spinnerStart,
                "MMM dd, yyyy   ");
        spinnerStart.setEditor(editorStart);

        constraints.gridx = 1;
        constraints.gridy = 0;
        gridbag.setConstraints(spinnerStart, constraints);
        panel.add(spinnerStart);
        cal = new GregorianCalendar(2002, 1, 1);
        //cal.add(Calendar.MONTH, -1);
        m_spinnerModelEnd.setValue(cal.getTime());

        JLabel labEnd = new JLabel("End Training");
        //labEnd.setHorizontalAlignment(SwingConstants.RIGHT);
        //???constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 0;
        //constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(labEnd, constraints);
        panel.add(labEnd);

        spinnerEnd = new JSpinner(m_spinnerModelEnd);
        JSpinner.DateEditor editorEnd = new JSpinner.DateEditor(spinnerEnd,
                "MMM dd, yyyy   ");
        spinnerEnd.setEditor(editorEnd);
        constraints.gridx = 3;
        constraints.gridy = 0;
        //constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(spinnerEnd, constraints);
        panel.add(spinnerEnd);

        //JLabel su = new JLabel("Select User:");
        //su.setToolTipText("choose the user to examine their social email
        // cliques");
        //constraints.gridx = 4;
        //constraints.gridy = 0;
        //	constraints.fill = GridBagConstraints.NONE;
        //gridbag.setConstraints(su, constraints);
        //panel.add(su);

        //constraints.gridx = 6;
        //constraints.gridy = 0;
        //constraints.gridwidth=3;
        //	constraints.fill = GridBagConstraints.BOTH;

        //     	gridbag.setConstraints(m_userCombo, constraints);
        //panel.add(m_userCombo);
        //constraints.fill = GridBagConstraints.NONE;

        //now for testing lablels
        JLabel labTStart = new JLabel("Start Test");
        labTStart.setToolTipText("Data to look at using our learned cliques");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        gridbag.setConstraints(labTStart, constraints);
        panel.add(labTStart);

        GregorianCalendar calt = new GregorianCalendar(2002, 1, 1);
        //	calt.add(Calendar.MONTH, -1);
        m_spinnerModelTStart.setValue(calt.getTime());
        spinnerTStart = new JSpinner(m_spinnerModelTStart);
        JSpinner.DateEditor editorTStart = new JSpinner.DateEditor(
                spinnerTStart, "MMM dd, yyyy   ");
        spinnerTStart.setEditor(editorTStart);

        constraints.gridx = 1;
        constraints.gridy = 1;
        gridbag.setConstraints(spinnerTStart, constraints);
        panel.add(spinnerTStart);

        JLabel labTEnd = new JLabel("End Test");

        constraints.gridx = 2;
        constraints.gridy = 1;

        gridbag.setConstraints(labTEnd, constraints);
        panel.add(labTEnd);
        cal = new GregorianCalendar(2003, 1, 1);
        //cal.add(Calendar.MONTH, -1);
        m_spinnerModelTEnd.setValue(cal.getTime());

        spinnerTEnd = new JSpinner(m_spinnerModelTEnd);
        JSpinner.DateEditor editorTEnd = new JSpinner.DateEditor(spinnerTEnd,
                "MMM dd, yyyy   ");
        spinnerTEnd.setEditor(editorTEnd);
        constraints.gridx = 3;
        constraints.gridy = 1;
        //	constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(spinnerTEnd, constraints);
        panel.add(spinnerTEnd);

        //JPanel butPanel = new JPanel();
        //	butPanel.setLayout(new FlowLayout());

        
        //buttons to allow computation to automatic or manual
        b_autoCompute = new JRadioButton("Automatic Computation",true);
        b_autoCompute.setToolTipText("Automatic refresh view on new user");
        b_manualCompute = new JRadioButton("Manual",false);
        
        
        ButtonGroup autos = new ButtonGroup();
        autos.add(b_autoCompute);
        autos.add(b_manualCompute);
        
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        
        gridbag.setConstraints(b_autoCompute, constraints);
        panel.add(b_autoCompute);
        
        
        constraints.gridx = 5;
        constraints.gridy = 0;
        
        gridbag.setConstraints(b_manualCompute, constraints);
        panel.add(b_manualCompute);
        
        JButton updatev = new JButton("Update View");
        updatev.setToolTipText("Manually update the view");
        updatev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                
                BusyWindow bw = new BusyWindow("Computing view","one moment",false);
                bw.setVisible(true);
                
                updateInfo(true);
                bw.setVisible(false);
            }});
        
        constraints.gridx = 6;
        constraints.gridy = 0;
        
        gridbag.setConstraints(updatev, constraints);
        panel.add(updatev);
        
        JPanel threshPanel = new JPanel(new GridLayout(1,2));
        
        JLabel minThresholdLabel = new JLabel("Threshold:");
        minThresholdLabel
                .setToolTipText("The Amount of Times a Clique needs to be seen to be included in the good group");
        //	Panel t = new Panel();
        //t.add(m_threshold);
        m_threshold = new JTextField(" 5");
   //     m_threshold.setMinimumSize(new Dimension(15, 20)); //this is because
                                                           // its killing it on
        constraints.gridx = 4;
        constraints.gridy = 1;
        threshPanel.add(minThresholdLabel);
        threshPanel.add(m_threshold);
        gridbag.setConstraints(threshPanel, constraints);
        panel.add(threshPanel);
                                                           // minimize

        JButton butRefresh = new JButton("Clique Violations");
        butRefresh
                .setToolTipText("Find groups of emails violating the clique set");

        butRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    before = System.currentTimeMillis();
                    // System.out.println("working...");
                    //  int minMessages =
                    // Integer.parseInt(m_textMinMsgs.getText());
                    showCliques(false);//show violations
                    after = System.currentTimeMillis();
                    System.out.print("sc completed in ");
                    System.out.print((after - before) / 1000.0);
                    System.out.println(" secs");

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SocialCliques.this, nfe);
                }
            }
        });
        constraints.gridx = 5;
        constraints.gridy = 1;
        gridbag.setConstraints(butRefresh, constraints);
        panel.add(butRefresh);

        JButton butCliques = new JButton("Cliques sets");
        butCliques.setToolTipText("Show users cliques for training period");
        butCliques.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    //  int minMessages =
                    // Integer.parseInt(m_textMinMsgs.getText());
                    showCliques(true);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SocialCliques.this, nfe);
                }
            }
        });
        constraints.gridx = 6;
        constraints.gridy = 1;
        gridbag.setConstraints(butCliques, constraints);
        panel.add(butCliques);

        EMTHelp et = new EMTHelp(EMTHelp.USERCLIQUES);
        constraints.gridx = 7;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        gridbag.setConstraints(et, constraints);
        panel.add(et);

        //constraints.gridx = 1;
        //constraints.gridy = 2;
        //
        //gridbag.setConstraints(Refresh, constraints);
        //panel.add(Refresh);

        /*
         * JButton UserInfo = new JButton("Update User Info");
         * UserInfo.setToolTipText("Show Information on current user");
         * 
         * UserInfo.addActionListener(new ActionListener() { public void
         * actionPerformed(ActionEvent evt) { updateInfo(); } });
         * constraints.gridx = 9; constraints.gridy = 1;
         * gridbag.setConstraints(UserInfo, constraints); panel.add(UserInfo);
         */

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 9;
        constraints.gridheight = 1;

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        //constraints.weightx = 1;
        //constraints.weighty = 1;
        gridbag.setConstraints(UInfo, constraints);
        panel.add(UInfo);

       // updateInfo(b_autoCompute.isSelected());

        //plotwin = new JPanel();
        //plotwin.add(userplot);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 9;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;

        gridbag.setConstraints(jfplotholder, constraints);
        panel.add(jfplotholder);

        //if (m_wg.getUserCount() > 0)
            //m_userCombo.getItemCount() > 0)
         //   setHistory((String) m_wg.getSelectedUser(),b_autoCompute.isSelected());

        m_textArea = new JTextArea(
                "Press Show to Generate User Cliques\nNOTE: it is bounded by date above");
        m_textArea.setEditable(false);
        //m_textArea.setPreferredSize(new Dimension(200,200));
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 9;

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;

        //trying a table of values
        dtmodel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        dtmodel.addColumn("Seen");
        dtmodel.addColumn("Weight");
        dtmodel.addColumn("Clique");
        m_table = new JTable(dtmodel);

        //m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_table.getColumnModel().getColumn(0).setMaxWidth(60);
        m_table.getColumnModel().getColumn(1).setMaxWidth(60);

        JScrollPane textScroll = new JScrollPane(m_table);//m_textArea);
       // textScroll.setBorder(new BevelBorder(BevelBorder.LOWERED));

        gridbag.setConstraints(textScroll, constraints);
        panel.add(textScroll);
        panel.setPreferredSize(new Dimension(400, 70));
        setViewportView(panel);

    }

    /*
     * public final void updateUserList(){ //try{
     * 
     * //String data2[] = m_wg.getUserList();
     * 
     * 
     * //synchronized(m_jdbc) //{ // m_jdbc.getSqlData("select sender,count(*)
     * from email group by sender having count(*) > 100"); //
     * m_jdbc.getSqlData("select distinct sender from email order by sender"); //
     * data2 = m_jdbc.getRowData();
     * 
     * //}
     * 
     * if(m_userCombo.getItemCount() >0) m_userCombo.removeAllItems();
     * 
     * ComboBoxModel model = new DefaultComboBoxModel(m_wg.getUserList());
     * m_userCombo.setModel(model);
     * 
     * 
     * 
     * //for(int i=0;i <data2.length;i++) //{
     *  // m_userCombo.addItem(data2[i][0].trim()); // } //}catch(Exception
     * e){System.out.println("problem in social cliques update: " + e);} }
     *  
     */
    
    
    
    
    
    public final void updateInfo(boolean compute) {

       
        //will be updating the UInfo textarea with relevant information on
        // current user:

        if (m_wg.getUserCount() == 0)
            //m_userCombo.getItemCount() ==0 )
            return;

        String user  = (String) m_wg.getSelectedUser();
            //(String)m_userCombo.getSelectedItem();
       if(user == null)
       {
    	   return;
       }
       
        String info = new String("User '" + user + "'");

        //String query;
        /*
         * try{ synchronized(m_jdbc){
         * 
         * query = "select count(*), min(dates), max(dates) from email where
         * sender='"+user+"' "; m_jdbc.getSqlData(query); data =
         * m_jdbc.getRowData();
         *  } if(data.length!=1 && data[0].length!=3){
         * System.out.println("Problem fetching user datas in scliques update
         * info"); UInfo.setText(info); return;
         *  }
         */
        userShortProfile udata = m_wg.getUserInfo(user);
        if (udata != null) {
            info += " has " + udata.getTotalCount() + " Emails available from " + udata.getStartDate()
                    + " to " + udata.getEndDate();
        }
        UInfo.setText(info);
        //check if we should update the view
        if(!compute){
            //TODO: clean up old stuff first.
            
            
            setHistory(" ",compute,jfplot_series,m_wg,m_jdbc);
            return;
        }
        setHistory(user,compute,jfplot_series,m_wg,m_jdbc);
        //}catch(SQLException e){System.out.println("prpblem: " + e);}
        //error check

        
        //setHistory(user);
    }

    public final void Execute() {

        updateInfo(b_autoCompute.isSelected());

    }

    /**
     * 
     * Graphical email histogram of past activity
     * 
     *  
     */

    public static final void setHistory(String user,boolean toDraw, TimeSeries tseries/*JPlot2D userplotCanvas*/, winGui m_wg,EMTDatabaseConnection m_jdbc ) {
        
        if (user == null)
        {
            return;
        }
        //	mysql> select count(*), MONTH(dates),year(dates) from email where
        // sender like 'sh553@cs.columbia.edu' group by year(dates),month(dates)
        // order by YEAR(dates)asc,MONTH(dates) asc;
        //	String user = ((String)m_userCombo.getSelectedItem()).toLowerCase();
        
        //clear the plot
       tseries.clear();
        
        
        
           // userplotCanvas.removeAll();// = new JPlot2D();

            if(!toDraw)
            {
            	//TODO:?????
                //userplotCanvas.repaint();
                //ie just clear
                return;
            }
            
            
            
           
            //explanation.removeAll();
            //e = explanation.getGraphics();
            // explanation.add(new JtextBox(
            //e.setColor(Color.black);
            ///e.drawString("Bar At Each Year",75,65);
            //g.drawString("Threshold (Moving Average)",75,80);
            //g.drawString("Malicious Emails",75,95);
            //e.setColor(Color.black);
            //e.fillRect(10,60,50,3);
            //g.setColor(Color.blue);
            //g.fillRect(10,75,50,3);
            //  e.setColor(Color.black);
            //e.fillRect(10,90,50,3);

            String data[][] = null;
            //String query;
            int numberbins = 0;
            int minyr = 0;
            int maxyr =0;
            int total = 0;
            
            userShortProfile usrdata = m_wg.getUserInfo(user);
            if (usrdata != null) {
                total =usrdata.getTotalCount(); //Integer.parseInt(usrdata[winGui.User_count]);
                
                
                minyr =  Utils.getSQLyear(usrdata.getStartDate());//usrdata[winGui.User_min]);
                maxyr =  Utils.getSQLyear(usrdata.getEndDate());//usrdata[winGui.User_max]);
            }
            
          
                numberbins = maxyr - minyr;
     
            numberbins++;//for zero offset
            numberbins = numberbins * 12;
            
            double x[] = new double[numberbins];
            double y[] = new double[numberbins];

           // userplotCanvas.setTitle("Email History");
          //  userplotCanvas.setXLabel("Per month useage starting jan " + minyr);
          //  userplotCanvas.setYLabel("Total Emails = " + total);

            //now to fill in the info:
            //synchronized (m_jdbc) 
            //{
              /*  query = "select count(sender), gmonth, "
                        + "gyear from email where sender = '" + user + "' "
                        + "GROUP BY gyear,gmonth "
                        + "order by gyear ASC ,gmonth ASC";
*/
                
                try{
                	synchronized (m_jdbc) {
                data = m_jdbc.getSQLData("select count(sender), gmonth, "
                        + "gyear from email where sender = '" + user + "' "
                        + "GROUP BY gyear,gmonth "
                        + "order by gyear ASC ,gmonth ASC");

					}

                	} catch (SQLException e) {
                    System.out.println("sql sethistory problem2: " + e);
                }
            //}
                
            int walkmonth = 0; //this will walk thorugh the months to match
                               // bins
            int currentmonth;
            int pos = 0;

            for (int i = 0; i < numberbins; i++) {
                y[i] = 0;//initialize
                x[i] = i + 1;
            }
            
           // System.out.println("data len:"+data.length);
            
            //this is fort he free chart monhth
            
            try{
            
            //step through the data and write to the y axis values
            for (int i = 0; i < data.length; i++) {
              //NOTE: NEED TO ADD ONE TO MONTH!!
            	tseries.add(new Month(Integer.parseInt(data[i][1])+1,Integer.parseInt(data[i][2])), new Integer(Integer.parseInt(data[i][0])));
            	
            	
            	
            	
            	
            	
            	/*currentmonth = Integer.parseInt(data[i][1]);
                //if not equal means no data, so advance till equal
                while (walkmonth != currentmonth && pos < numberbins) {
                    //x[pos] = walkmonth;
                    //    y[pos++] =0.0;
                    walkmonth++;
                    if (walkmonth > 11)
                        walkmonth = 0;
                }

                //		    if(walkmonth == currentmonth)
                //x[pos] = walkmonth;
                y[pos++] = Integer.parseInt(data[i][0]); /// total ;

                //next month
                walkmonth++;
                if (walkmonth > 11)
                    walkmonth = 0;
                    */
            }
            
            }catch(Exception ec){
            	ec.printStackTrace();
            }
            
            
            //ready to draw information
/*
            double[] s = new double[2];
            s[0] = 0;
            s[1] = 1;
           // userplotCanvas.setYScale(s);
            double[] s2 = new double[2];
            s2[0] = 1;
            s2[1] = numberbins + 1;
           // userplotCanvas.setXScale(s2);

           // userplotCanvas.addCurve(x, y);
           
            //need to draw line every 12 months
            //size if max y
            double max = 0;
            double yrlines[] = new double[numberbins];
            for (int i = 0; i < numberbins; i++) {
                if (y[i] > max)
                    max = y[i];
            }

            for (int i = 0; i < numberbins; i++) {
                if (x[i] % 12 == 1)
                    yrlines[i] = max;
                else
                    yrlines[i] = 0;

            }
            yrlines[0] = max;
            */
           //?? userplotCanvas.addCurve(yrlines);
          //??  userplotCanvas.setLineColor(Color.black);
        

          //??  userplotCanvas.setAutoXScale();
          //??  userplotCanvas.setAutoYScale();
            //userplot.setAnnotationColor(Color.black);
            //would like to put it on top of first x.
            //need to find the top
            //s = userplot.getXScale();
            ///s2 = userplot.getYScale();
            //userplot.addAnnotation("YR lines, starting from "+minyr,8,12);
            //??userplotCanvas.repaint();
            //  for(int i=0;i<numberbins;i++)
            //	System.out.print(y[i] + " ");
            //System.out.println("\nusing number bins: " + numberbins);
            /*
             * .setAnnotationColor(Color.green); m_plot12.addAnnotation("NUMBER
             * of DISTINCT "+RorS+" per BLOCK of 20",8,44);
             * m_plot12.setAnnotationColor(Color.orange);
             * m_plot12.addAnnotation("NUMBER of MSGS with ATTACH per BLOCK of
             * 50",8,41); m_plot12.setAnnotationColor(Color.red);
             * m_plot12.addAnnotation("ROLLING AVERAGES (per 100
             * records)",8,38); m_plot12.setAnnotationColor(Color.blue);
             */
        
        //error check
    }//end set history

    /**
     * show = false then show violations, if true show sets
     *  
     */

    public final void showCliques(boolean show) {
        //will calculate the users social cliques
        //using bloom filters
        int elements = 0;
        String data[][];//hold the sql data
        String result = new String();
        try {
            //adding table part:
            // clear table
            int length = dtmodel.getRowCount();
            for (int i = length - 1; i >= 0; i--)
                dtmodel.removeRow(i);

            String startDate = Utils.mySqlizeDate2YEAR(m_spinnerModelStart
                    .getDate());
            Calendar cal = new GregorianCalendar();
            cal.setTime(m_spinnerModelEnd.getDate());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String endDate = Utils.mySqlizeDate2YEAR(cal.getTime());
            String user = (String) m_wg.getSelectedUser();//((String)m_userCombo.getSelectedItem()).toLowerCase();
            String query;
            query = new String(
                    "select distinct sender,rcpt,mailref from email where dates>='"
                            + startDate + "' and dates<'" + endDate
                            + "' and sender = '" + user
                            + "' and numrcpt>1 group by mailref,rcpt,sender ");

            int total_emails;
            String cur_ref = new String();
            Hashtable userhash = new Hashtable();
            Hashtable rcptCount = new Hashtable();
            TreeSet sortedlist = new TreeSet(new UClique());
            //new stuff:
            //give weights to each so first see how many emails this user has
            // in general, then count freq for each user
            synchronized (m_jdbc) {
                data = m_jdbc
                        .getSQLData("select count(sender) from email where sender = '"
                                + user + "'");
                //data = m_jdbc.getRowData();
                //elements = m_jdbc.getResultSize();

            
            if (data.length > 0)
                total_emails = Integer.parseInt(data[0][0]);

            //no to get for each user
                data = m_jdbc
                        .getSQLData("select rcpt,count(*) from email where sender = '"
                                + user + "' and numrcpt>0 group by rcpt");
                //elements = m_jdbc.getResultSize();
            }
            
            
            
            
            for (int i = 0; i < data.length; i++) {
                //RGIHT HERE
                rcptCount.put(data[i][0].toLowerCase().trim(), data[i][1]);
            }

            System.out.println("done new stuff with rcpt cnt");

            int count = 0;
            synchronized (m_jdbc) {
                data = m_jdbc.getSQLData(query);
                //elements = m_jdbc.getResultSize();
            }
            System.out.println("here now");
            
            result = new String();
            if(data!=null)
            	elements = data.length;
            
            int group = 0;
            
            String lastSeen = new String();
            
            if (elements > 0)
                cur_ref = data[0][2]; //ie the current ref
            //idea: while ref is the same, collect the rcpt list
            for (int i = 0; i < elements; i++) {
                //build current group
                //note one user is not a clique
                if (cur_ref.equals(data[i][2])) {
                    //we need to add this email to ourlist provided it isnt our
                    // own
                    if (!user.equals(data[i][1].trim().toLowerCase())
                            && !lastSeen
                                    .equals(data[i][1].trim().toLowerCase())) {
                        group++;
                        lastSeen = data[i][1].trim().toLowerCase();
                        result = result + lastSeen + " , ";
                    }
                } else //this means that we are seeing a new email starting, so
                       // we need to save what we have so far
                {
                    if (group > 1) {
                        if (!userhash.containsKey(result)) {
                            userhash.put(result, "1");
                        } else {
                            count = Integer.parseInt((String) userhash
                                    .get(result));
                            count++;
                            // lastSeen="";
                            userhash.put(result, "" + count);
                        }
                    }
                    if (i < elements - 1) {
                        cur_ref = data[i][2];
                        //check if not current user
                        if (user.equals(data[i][1].trim().toLowerCase())) {
                            result = new String();
                            group = 0;
                            lastSeen = "";//data[i][0].trim();
                        } else {
                            lastSeen = data[i][1].trim().toLowerCase();
                            result = new String(lastSeen + " , ");
                            group = 1; //because we are starting with current
                                       // name
                        }
                    }
                }
            }
            //now for last one
            if (group > 1) {
                if (!userhash.containsKey(result)) {

                    userhash.put(result, "1");
                } else {
                    //  System.out.println(result);
                    count = Integer.parseInt((String) userhash.get(result));
                    count++;
                    userhash.put(result, "" + count);
                }
            }
            int sum, stop;
            String t = new String();
            result = new String();
            String tempkey = new String();
            //we set up the clique groups, if show = true then only show this
            // set
            if (show == true) {

                String w[] = new String[3];
                w[0] = new String();
                w[1] = new String();
                w[2] = new String(
                        "Clique set section, these are groups seen in training, we have "
                                + elements + " data elements\n");

                dtmodel.addRow(w);

                String temp = new String();
                int tempint, thresh = Integer.parseInt(m_threshold.getText()
                        .trim());
                for (Enumeration v = userhash.elements(), k = userhash.keys(); k
                        .hasMoreElements();) {

                    temp = new String((String) v.nextElement());
                    tempint = Integer.parseInt(temp);
                    tempkey = (String) k.nextElement();
                    sum = 0;
                    //here were we can compute a relevent score for the
                    StringTokenizer tok = new StringTokenizer(tempkey, ",");
                    stop = tok.countTokens();
                    for (; stop > 1; stop--) {
                        t = tok.nextToken().toLowerCase().trim();
                        sum += Integer.parseInt((String) rcptCount.get(t));
                    }
                    if (tempint >= thresh) {

                        sortedlist.add(new UClique(tempkey, tempint, sum));
                    }
                }
                //System.out.println("sorted list size: " + sortedlist.size());

                int k = 0;
                for (Iterator v = sortedlist.iterator(); v.hasNext();) {
                    //result +=k +")"+v.next()+"\n";
                    dtmodel.addRow(((UClique) v.next()).toStringArray());
                    k++;
                }

                //end test violations

                //m_textArea.setText(result);
                //m_textArea.setCaretPosition(0);
                return;
            }

            //ok we have a hastable and we want to sort it:
            //List l = Arrays.asList(n);
            //Collections.sort(l);
            String temp = new String();
            //System.out.println("threshold is now: " +
            // m_threshold.getText().trim());
            //String report = new String();// "number of elements: " + elements
            // + "\n" + result+"\n");
            //now to detect violation on clique behaviour.
            //we will get the data build a new hash, and then check for each
            // group if it isn ot in first hash
            startDate = Utils.mySqlizeDate2YEAR(m_spinnerModelTStart.getDate());

            cal = new GregorianCalendar();
            cal.setTime(m_spinnerModelTEnd.getDate());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            endDate = Utils.mySqlizeDate2YEAR(cal.getTime());
            System.out.println("here123");

            query = new String(
                    "select distinct sender,rcpt,mailref from email where dates>='"
                            + startDate + "' and dates<'" + endDate
                            + "' and sender = '" + user
                            + "' and numrcpt > 1 group by mailref,rcpt,sender ");

            Hashtable checkhash = new Hashtable();

            synchronized (m_jdbc) {
                data = m_jdbc.getSQLData(query);
                //data = m_jdbc.getRowData();
                elements = data.length;//m_jdbc.getResultSize();
            }
            System.out.println("here222");

            result = new String();
            group = 0;
            lastSeen = "";
            if (elements > 0)
                cur_ref = data[0][2]; //ie the current ref
            //idea: while ref is the same, collect the rcpt list
            for (int i = 0; i < elements; i++) {
                //build current group
                //note one user is not a clique
                if (cur_ref.equals(data[i][2])) {
                    //we need to add this email to ourlist provided it isnt our own or duplicate
                    if (!user.equals(data[i][1].trim().toLowerCase())
                            && !lastSeen
                                    .equals(data[i][0].trim().toLowerCase())) {
                        lastSeen = data[i][1].trim().toLowerCase();
                        group++;
                        result = result + lastSeen + " , ";
                    }
                } else //this means that we are seeing a new email starting, so we need to save what we have so far
                {
                    if (group > 1) {
                        if (!checkhash.containsKey(result)) {
                            checkhash.put(result, "1");
                        } else {
                            count = Integer.parseInt((String) checkhash
                                    .get(result));
                            count++;
                            checkhash.put(result, "" + count);
                        }
                    }
                    if (i < elements - 1) {
                        cur_ref = data[i][2];
                        //check if not current user
                        if (user.equals(data[i][1].trim().toLowerCase())) {
                            lastSeen = "";
                            result = new String();
                            group = 0;
                        } else {
                            lastSeen = data[i][1].trim().toLowerCase();
                            result = new String(lastSeen + " , ");
                            group = 1; //because we are starting with current name
                        }
                    }
                }
            }
            //now for last one
            if (group > 1) {
                if (!checkhash.containsKey(result)) {
                    checkhash.put(result, "1");
                } else {
                    //  System.out.println(result);
                    count = Integer.parseInt((String) checkhash.get(result));
                    count++;
                    checkhash.put(result, "" + count);
                }
            }
            result = new String();
            //report += " clique violation section, these are groups not seen before, looked at " + elements + " data elements\n";
            String w[] = new String[3];
            w[0] = new String();
            w[1] = new String();
            w[2] = new String(
                    "clique violation section, these are groups not seen before, looked at "
                            + elements + " data elements\n");

            dtmodel.addRow(w);

            //ok we have a hastable and we want to sort it:
            //List l = Arrays.asList(n);
            //Collections.sort(l);
            //		temp = new String();
            //System.out.println("threshold is now: " + m_threshold.getText().trim());
            String tempval = new String();
            //int sum;
            sortedlist = new TreeSet(new UClique());
            for (Enumeration v = checkhash.elements(), k = checkhash.keys(); k
                    .hasMoreElements()
                    && v.hasMoreElements();) {
                sum = 0;
                temp = (String) k.nextElement();
                if (!userhash.containsKey(temp)) {
                    //here were we can compute a relevent score for the 
                    StringTokenizer tok = new StringTokenizer(temp, ",");
                    stop = tok.countTokens();
                    for (; stop > 1; stop--) {
                        sum += Integer.parseInt((String) rcptCount.get(tok
                                .nextToken().toLowerCase().trim()));
                    }

                    tempval = (String) v.nextElement();
                    //report+=tempval+"\t"+temp+"\n";
                    sortedlist.add(new UClique(temp, Integer.parseInt(tempval),
                            sum));
                } else
                    v.nextElement();
                //tempint  = Integer.parseInt(temp);
                //if(tempint >= thresh)
                //  result += k.nextElement() + "=" +temp+"\n";
                //else
                //  k.nextElement();//since looked at next value need to look at next key
            }
            //		System.out.println("sorted list size: " + sortedlist.size());
            int k = 0;
            for (Iterator v = sortedlist.iterator(); v.hasNext();) {
                //report +=k +")"+v.next()+"\n";
                dtmodel.addRow(((UClique) v.next()).toStringArray());

                k++;
            }

            //end test violations
            //m_textArea.setText(report);
            //m_textArea.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not get cliques: " + ex);
        }

    }

    //from messgae class
    /* private String mySqlizeDate(Date date)
     {
     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
     return format.format(date);
     }*/

    private class UClique implements Comparator {
        private String name;

        private int value;

        private int weight;

        public UClique() {
            name = new String();
            value = -1;
            weight = 0;
        }

        public UClique(String s, int n, int w) {
            name = new String(s);
            value = n;
            weight = w;
        }

        public final String[] toStringArray() {
            String s[] = new String[3];
            s[2] = name;
            s[0] = new String("" + value);
            s[1] = new String("" + weight);
            return s;
        }

        public final void setName(String s) {
            name = s;
        }

        public final void setValue(int n) {
            value = n;
        }

        public final String toString() {
            return value + "\t" + weight + "\t" + name;
        }

        public final int getWeight() {
            return weight;
        }

        public final int getValue() {
            return value;
        }

        public final String getName() {
            return name;
        }

        public final int compare(Object o1, Object o2) {

            if ((o1 instanceof UClique) && (o2 instanceof UClique)) {
                if (((UClique) o1).getValue() < ((UClique) o2).getValue())
                    return -1;
                if (((UClique) o1).getValue() > ((UClique) o2).getValue())
                    return 1;
                //so equal values
                if (((UClique) o1).getWeight() < ((UClique) o2).getWeight())
                    return -1;
                if (((UClique) o1).getWeight() > ((UClique) o2).getWeight())
                    return 1;
                return (((UClique) o1).toString()).compareTo(((UClique) o2)
                        .toString());
            }
            System.out.println("oop: " + o1 + "and " + o2);
            return 0;

        }

        public final boolean equals(Object obj) {
            if (obj instanceof UClique) {
                return this.toString().equals((((UClique) obj).toString()));
            }

            System.out.println("obj: " + obj);
            return false;
        }
    }

}









