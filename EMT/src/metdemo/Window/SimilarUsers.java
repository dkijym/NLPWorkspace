/*
For the tab to find users with similar behavior
Ke Wang, 07/22/02

Added new functionality to find out all groups of similar users, comment by 11/11
Added new concept: aligned histogram, which will shift to the first non-0 bin, using 5am as the time to mean the beginning of a new day.

shlomo - integrated with other windows and top bar - dec-jan 2004

*/

package metdemo.Window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.DistanceCompute;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.HistogramCalculator;
import metdemo.dataStructures.UserDistance;
import metdemo.dataStructures.UserHistogram;

import java.util.*;
//import java.io.*;
import java.sql.SQLException;
import java.awt.datatransfer.*;
import chapman.graphics.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SimilarUsers.
 */
public class SimilarUsers extends JScrollPane implements ListSelectionListener
{
    //private JComboBox m_comboUsers;
    /** The m_aspect. */
    private JComboBox m_aspect;
    
    /** The m_comparison method. */
    private JComboBox m_comparisonMethod;
    //private JComboBox m_confidenceLevel;
    /** The m_confidence level. */
    private JSlider m_confidenceLevel;
    
    /** The run button. */
    private JButton runButton; 
    
    /** The run all button. */
    private JButton runAllButton; //for find all the similar user group, 11/11
    
    /** The help button. */
    private JButton helpButton;

    //    private JRadioButton b1, b2;
    /** The b4. */
    private JRadioButton b3, b4;
    
    /** The bg2. */
    private ButtonGroup bg2;

    /** The aspects. */
    private String[] aspects = {"avg number of emails sent per hour", "avg email size(KB)", "avg number of attachment", "avg number of recipients"};
    
    /** The methods. */
    private String[] methods = {"L1-form","L2-form","Quadratic","KS-test"};
    
    /** The levels. */
    private String[] levels = {"high","middle","low"};
    
    /** The m_histogram. */
    private JPlot2D m_histogram;
    
    /** The m_thumbnail. */
    private JPlot2D m_thumbnail;
    // private HashSet m_hsUsers;
    /** The m_query. */
    private JPlot2D m_query;
    
    /** The default color. */
    private Color defaultColor;

    /** The m_list users. */
    private JTable m_listUsers; //for similar users
    
    /** The m. */
    private DefaultTableModel m;


    //to calculate and store the user's usage histogram
    /** The hc. */
    private HistogramCalculator hc;
    
    /** The user h. */
    private UserHistogram userH;
    
    /** The m_ht histograms. */
    private Hashtable m_htHistograms; // store the calculated histogram to improve effeciency
    
    /** The m_ht histograms of email size. */
    private Hashtable m_htHistogramsOfEmailSize;
    
    /** The m_ht histograms of attachment. */
    private Hashtable m_htHistogramsOfAttachment;
    
    /** The m_ht histograms of recipient. */
    private Hashtable m_htHistogramsOfRecipient;

    //private String curUser = new String(); //current user got selected;
    /** The cur aspect. */
    private String curAspect = new String(); //current aspect comparision based on
    
    /** The m_similar users. */
    private UserDistance[] m_similarUsers; //store the similar users to curUser

    /** The align point. */
    private int alignPoint; //the start point after align. set in align() method

    /** The m_jdbc view. */
    private EMTDatabaseConnection m_jdbcView;

    /** The BW. */
    private BusyWindow BW;
    
    /** The m_win gui. */
    private winGui m_winGui;

    //for do copy stuff
    /** The clipboard. */
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    /** The s1by24. */
    double []s1by24 = {1,24};




    //constructor
    /**
     * Instantiates a new similar users.
     *
     * @param jdbcView the jdbc view
     * @param md5Renderer the md5 renderer
     * @param winGui the win gui
     * @throws SQLException the sQL exception
     */
    public SimilarUsers(EMTDatabaseConnection jdbcView, md5CellRenderer md5Renderer, winGui winGui) throws SQLException
    {
	if(jdbcView==null)
	    {
		throw new NullPointerException("similiar users recieed empty database handle");
	    }
		
	//init the variables
	//	m_jdbcUpdate = jdbcUpdate;
	m_jdbcView = jdbcView;
	m_winGui = winGui;

	//m_hsUsers = new HashSet();	
	m_htHistograms = new Hashtable();
	m_similarUsers = new UserDistance[0];
	m_htHistogramsOfEmailSize = new Hashtable();
	m_htHistogramsOfAttachment = new Hashtable();
	m_htHistogramsOfRecipient = new Hashtable();

	hc = new HistogramCalculator(jdbcView);

	
	//arrange the layout
	JPanel panel = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	panel.setLayout(gridbag);
	
	int gridy = 0;
	
	m_aspect = new JComboBox(aspects);
	//m_aspect.setPreferredSize(new Dimension(220,25));
	//JLabel title = new JLabel("Find similar users based on:");
	JPanel panTitle = new JPanel();
	//panTitle.setLayout(new FlowLayout());
	/*
	b1 = new JRadioButton("all users", false);
	b1.setBackground(Color.orange);	
	b2 = new JRadioButton("only senders", true);
	b2.setBackground(Color.orange);
	bg = new ButtonGroup();
	bg.add(b1);
	bg.add(b2);
	b1.setToolTipText("Click to choose which set of users to use");
	b2.setToolTipText("Click to choose which set of users to use");
	panTitle.add(new JLabel("Accout Style: "));
	panTitle.add(b1);
	panTitle.add(b2);
	*/
	b3 = new JRadioButton("Normal Histogram", true);
	b4 = new JRadioButton("Aligned Histogram", false);
	bg2 = new ButtonGroup();
	bg2.add(b3);
	bg2.add(b4);

	panTitle.add(new JLabel("Similar Acct Style:"));
	panTitle.add(b3);
	panTitle.add(b4);


	constraints.gridx = 0;
	constraints.gridy = gridy; gridy++;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(0,0,0,0);
	gridbag.setConstraints(panTitle, constraints);
	panel.add(panTitle);

	
	JPanel panSelect = new JPanel();
	//panSelect.setLayout(new FlowLayout());
	/*	
	m_comboUsers = new JComboBox();
	m_comboUsers.setPreferredSize(new Dimension(250,25));
	//m_comboUsers.setRenderer(md5Renderer);

	//JPanel panSelect = new JPanel();
	//panSelect.setLayout(new FlowLayout());
	panSelect.add(new JLabel("Select Account:"));
	panSelect.add(m_comboUsers);
	*/
	panSelect.add(new JLabel("Aspect: "));
	panSelect.add(m_aspect);

	//	initCombo();

	constraints.gridx =1;
	//constraints.gridy =gridy; gridy++; 
	//constraints.gridwidth =2;
	//constraints.insets = new Insets(5, 5, 5, 5);
	gridbag.setConstraints(panSelect, constraints);
	panel.add(panSelect);
	
	

	
	m_comparisonMethod = new JComboBox(methods);
	m_comparisonMethod.setPreferredSize(new Dimension(100,25));
	JPanel panMethod = new JPanel();
	panMethod.setLayout(new FlowLayout());
	panMethod.add(new JLabel("Compare Method:"));
	panMethod.add(m_comparisonMethod);


	//	m_confidenceLevel = new JComboBox(levels);
	//m_confidenceLevel.setPreferredSize(new Dimension(80,25));
	m_confidenceLevel = new JSlider(JSlider.HORIZONTAL,0,10,5);
	m_confidenceLevel.setPreferredSize(new Dimension(120,40));
	m_confidenceLevel.setMajorTickSpacing(5);
	m_confidenceLevel.setMinorTickSpacing(1);
	m_confidenceLevel.setPaintTicks(true);

	//Create the label table
        Hashtable labelTable = new Hashtable();
	labelTable.put( new Integer( 0 ), new JLabel("low") );
        labelTable.put( new Integer( 10 ), new JLabel("high") );
        m_confidenceLevel.setLabelTable( labelTable );
	m_confidenceLevel.setPaintLabels(true);

	//JPanel panLevel = new JPanel();
	//panLevel.setLayout(new FlowLayout());
	panMethod.add(new JLabel("Similar Level:"));
	panMethod.add(m_confidenceLevel);
	runButton = new JButton("Find Similar Accts");
	runButton.setToolTipText("press this button to show the histogram and find similar users");
	panMethod.add(runButton);

	runAllButton = new JButton("Find All User Groups");
	runAllButton.setToolTipText("press this button to find all the groups of similar users");
	panMethod.add(runAllButton);

	//helpButton = new JButton("HELP");
	//helpButton.setBackground(Color.orange);
	EMTHelp j = new EMTHelp(EMTHelp.SIMILARUSERS);
	panMethod.add(j);
	
	constraints.gridx =0;
	constraints.gridy =gridy; gridy++;
	constraints.gridwidth =2;
	constraints.insets = new Insets(0, 5, 0, 5);
	gridbag.setConstraints(panMethod, constraints);
	panel.add(panMethod);
  
	JLabel label;

	
	label = new JLabel("Usage Histogram for Selected Account");
	constraints.gridx = 0;
	constraints.gridy = gridy; gridy++;
	constraints.gridwidth =2;
	constraints.insets = new Insets(5,5,0,5);
	gridbag.setConstraints(label, constraints);
	panel.add(label);

	m_histogram = new JPlot2D();
	m_histogram.setPlotType(JPlot2D.BAR);
	m_histogram.setBackgroundColor(Color.white);
	m_histogram.setFillColor(Color.blue);
	m_histogram.setPreferredSize(new Dimension(350,220));


	JPanel h = new JPanel();
	h.add(m_histogram);
	constraints.gridx = 0;
	constraints.gridy = gridy;gridy++;
	constraints.gridwidth =2;
	constraints.insets = new Insets(10,5,5,5);
	gridbag.setConstraints(h, constraints);
	panel.add(h);

	label = new JLabel("Similar Users");
	constraints.gridx = 0;
	constraints.gridy = gridy; 
	constraints.gridwidth =1;
	constraints.insets = new Insets(5,5,5,5);
	gridbag.setConstraints(label, constraints);
	panel.add(label);


	label = new JLabel("Selected one from similar users");
	constraints.gridx=1;
	constraints.gridy=gridy; gridy++;
	constraints.gridwidth =1;
	constraints.insets = new Insets(5,5,5,5);
	gridbag.setConstraints(label,constraints);
	panel.add(label);

	
	m = new DefaultTableModel(){
		public boolean isCellEditable(int row, int col) {return 
								     false;}};

	m.addColumn("account");
	m.addColumn("difference");
	m_listUsers = new JTable(m);
	m_listUsers.setRowSelectionAllowed(true);
	m_listUsers.setColumnSelectionAllowed(false);
	m_listUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	m_listUsers.setDefaultRenderer(m.getColumnClass(0),md5Renderer);
	m_listUsers.getColumnModel().getColumn(1).setMaxWidth(70);
	
	JScrollPane sPane = new JScrollPane(m_listUsers);
	sPane.setPreferredSize(new Dimension(300,180));
	constraints.gridx = 0;
	constraints.gridy = gridy;
	constraints.gridwidth =1;
	constraints.insets = new Insets(5,5,5,5);
	gridbag.setConstraints(sPane, constraints);
	panel.add(sPane);



	m_query = new JPlot2D();
	m_query.setPlotType(JPlot2D.BAR);
	m_query.setBackgroundColor(Color.white);
	m_query.setFillColor(Color.blue);
	m_query.setPreferredSize(new Dimension(250,180));


	JPanel hh = new JPanel();
	hh.add(m_query);
	constraints.gridx = 1;
	constraints.gridy = gridy;gridy++;
	constraints.gridwidth =1;
	constraints.insets = new Insets(5,5,5,5);
	gridbag.setConstraints(hh, constraints);
	panel.add(hh);


	//add the copy button
	JButton copy = new JButton ("Copy Clipboard");
	copy.setToolTipText("To copy test table contents to system clipboard");
	copy.addActionListener (new ActionListener() {
		public void actionPerformed (ActionEvent e) {
		    //String selection = jt.getSelectedText();
		    int j=0;
		    Vector select = m.getDataVector();
		    String neat = new String();
		    for(int i=0;i<select.size();i++)
		      	{Vector v = (Vector)select.elementAt(i);
			for(j=0;j<v.size()-1;j++)
			    neat += (String)v.elementAt(j)+"," ;
			neat += v.elementAt(j) + "\n";
			}
		    StringSelection data = new StringSelection(   neat);
		    clipboard.setContents (data, data);
		}
	    });
    	constraints.gridx = 0;
	constraints.gridy = gridy++;
	constraints.gridwidth =1;
	constraints.insets = new Insets(5,5,5,5);
	gridbag.setConstraints(copy, constraints);
	panel.add(copy);

	//set renderer for table
	TableColumn interestColumn = m_listUsers.getColumn("account");
 

	// Set a pink background and tooltip for the interest column renderer.
	DefaultTableCellRenderer interestColumnRenderer = new DefaultTableCellRenderer(){
		public void setValue(Object value) {
	      
	     
		    if(value instanceof String)
			{  
	
			    if(((String)value).startsWith("GROUP")){
				setBackground(Color.yellow);
			    }
			    else
				setBackground(Color.white);
			}

		    setText((value == null) ? "" : value.toString());
		
		}
	    };
 
	interestColumn.setCellRenderer(interestColumnRenderer);
	/*
	b1.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try{
		
			initCombo("all");
			
		    }
		    catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for account: " + ex);   
			}
		}
	    });



	//addActionListener(new chooseUsers());
	b2.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try{
		
		 
			initCombo("sender");
		    }
		    catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for account: " + ex);   
			}
		}
	    });

	*/
	//addActionListener(new chooseUsers());
	/*
	m_comboUsers.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try{
			//		showSmallHistogram();
			RefreshHistogram();
		    }
		    catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for this account: " + ex);   
			}
		}
	    });
	*/

	//addActionListener(new showThumbNail());
	m_aspect.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try{
			//		showSmallHistogram();//get current user
		    String selected = (String)m_winGui.getSelectedUser(); 
			    //m_comboUsers.getSelectedItem();
			if (selected == null)
			    return;
			
			RefreshHistogram(selected);
		    }
		    catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for this account: " + ex);   
			}
		}
	    });





	    //addActionListener(new showThumbNail());
	runButton.addActionListener(new compareListener());
	runAllButton.addActionListener(new allGroupListener());
	//helpButton.addActionListener(new helpListener());
	ListSelectionModel rowSM = m_listUsers.getSelectionModel();
	rowSM.addListSelectionListener(this);
	//initCombo("sender"); //trigger update
//	get current user
	String selected = (String)m_winGui.getSelectedUser(); 
	    //m_comboUsers.getSelectedItem();
	if (selected == null)
	{}
	else{
	RefreshHistogram(selected);
	}
	setViewportView(panel);
    }

    /*
    //for the m_comboUser
    class showThumbNail implements ActionListener{
	public void actionPerformed(ActionEvent e){
	    try{
		//		showSmallHistogram();
		RefreshHistogram();
	    }
	    catch (SQLException ex)
		{
		    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for this account: " + ex);   
		}
	}
	
    }
    */
    

    /**
     * Execute.
     */
    public final void Execute()
    {
	
		    try{
			//		showSmallHistogram();
//		    	get current user
		    	String selected = (String)m_winGui.getSelectedUser(); 
		    	    //m_comboUsers.getSelectedItem();
		    	if (selected == null)
		    	    return;
		    	

		    	RefreshHistogram(selected);
			
		    }
		    catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for this account: " + ex);   
			}
    }




    /**
     * The listener interface for receiving compare events.
     * The class that is interested in processing a compare
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addcompareListener<code> method. When
     * the compare event occurs, that object's appropriate
     * method is invoked.
     *
     * @see compareEvent
     */
    class compareListener implements ActionListener{
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e){
	    (new Thread(new Runnable()
		{
		    public void run(){
			try
			    {
//				get current user
				String selected = (String)m_winGui.getSelectedUser(); 
				    //m_comboUsers.getSelectedItem();
				if (selected == null)
				    return;
			

				m_winGui.setEnabled(false);
				RefreshTable(selected,m_winGui.getUserList());
				m_winGui.setEnabled(true);
			    }
			catch (SQLException ex)
			    {
				JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to compute user group: " + ex);   
				m_winGui.setEnabled(true);
			    }
			
		    }
		})).start();

	}
    }

    //for all button
    /**
     * The listener interface for receiving allGroup events.
     * The class that is interested in processing a allGroup
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addallGroupListener<code> method. When
     * the allGroup event occurs, that object's appropriate
     * method is invoked.
     *
     * @see allGroupEvent
     */
    class allGroupListener implements ActionListener{
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e){
	    (new Thread(new Runnable()
		{
		    public void run(){
			try
			    {

				m_winGui.setEnabled(false);
				FindAllUserGroups();
				m_winGui.setEnabled(true);
			    }
			catch (SQLException ex)
			    {
				JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to compute all user group: " + ex); 
				m_winGui.setEnabled(true);
			    }
			
		    }
		})).start();

	}
    }


 
    /*
    class chooseUsers implements ActionListener{
	public void actionPerformed(ActionEvent e){
	    try{
		if(b1.isSelected())
		    initCombo("all");
		else if(b2.isSelected())
		    initCombo("sender");
	    }
	    catch (SQLException ex)
		{
		    JOptionPane.showMessageDialog(SimilarUsers.this, "Failed to get data for account: " + ex);   
		}
	}
    }
    */
    /*
      //for helpButton
      class helpListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
      String message = EMTHelp.getSimilarUsers();
      String title = "Help";
      DetailDialog dialog = new DetailDialog(null, false, title, message);
      dialog.setLocationRelativeTo(SimilarUsers.this);
      dialog.show();

      }	
      }
    */


    //for the list selection
    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public final void valueChanged(ListSelectionEvent event)
    {
	drawPlot();
    }

   
    // redraw the usage histogram
    /**
     * Refresh histogram.
     *
     * @param curUser the cur user
     * @throws SQLException the sQL exception
     */
    private final void RefreshHistogram(String curUser) throws SQLException
    {
	
	
	String xLabel="24 hour per day";
	String yLabel="";

	//get current aspect
	Object selected = m_aspect.getSelectedItem();
	if (selected == null)
	    curAspect = aspects[0]; //by default compare avg # of emails
	else
	    curAspect = (String)selected;
	
	
	if(curAspect.equals(aspects[0])){ //# of email
	    yLabel="Avg number of emails sent";
	}
	else if(curAspect.equals(aspects[1])){ //email size
	    yLabel="Avg size of emails sent";
	}
	else if(curAspect.equals(aspects[2])){ //# of attachement
	    yLabel="Avg number of attachment sent";
	}
	else{ //# of recipient
	    yLabel="Avg number of recipients";
	}
	
	

	double[] histogramVector = retrieveHistogram(curUser, curAspect);
	if(histogramVector == null)
	    return;

	// clear table
	DefaultTableModel model = (DefaultTableModel)m_listUsers.getModel();
	
	model.setRowCount(0);//fast way of emptying the rows.

	//int length = model.getRowCount();
	//for(int i=length-1;i>=0;i--)
	//   model.removeRow(i);

	// show the plot
	m_histogram.removeAll();
	m_query.removeAll();
	m_query.repaint();
	m_histogram.setXLabel(xLabel);
	m_histogram.setYLabel(yLabel);
	
	/*
	  double[] s = new double[2];
	  s[0]=1; s[1]=24;
	  double[] x = new double[24];
	  double[] y = new double[24];
	
	  for(int i=0;i<24;i++){
	  x[i]=i+1;
	  y[i]=((Double)histogramVector.elementAt(i)).doubleValue();
	  }
	
	  m_histogram.setXScale(s);
	  m_histogram.addCurve(x,y);	
	
	*/
	paintHistogram(histogramVector, m_histogram);
	m_histogram.repaint();
    }
    

    // return the usage histogram according to the aspect and account, or null if the selection is null; Get directly from hashTable if it's already existing
    /**
     * Retrieve histogram.
     *
     * @param curUser the cur user
     * @param curAspect the cur aspect
     * @return the double[]
     * @throws SQLException the sQL exception
     */
    private final double[] retrieveHistogram(String curUser, String curAspect) throws SQLException
    {

	double[] histogramVector;

	// get from hashtable if there is one, otherwise get from database
	// ATTN: not for real time work

	if(curAspect.equals(aspects[0]))//# of email
	    {
		if(m_htHistograms.containsKey(curUser))
		    {
			userH = (UserHistogram)m_htHistograms.get(curUser);
			histogramVector  = userH.getHistogram();
		    }
		else
		    {

			histogramVector = hc.getHistogram(curUser,m_winGui.getUserInfo(curUser));
			userH = new UserHistogram(histogramVector, curUser);
			m_htHistograms.put(curUser, userH);
		    }
	    }
	else if(curAspect.equals(aspects[1])) //size of email
	    {
		if(m_htHistogramsOfEmailSize.containsKey(curUser))
		    {
			userH = (UserHistogram)m_htHistogramsOfEmailSize.get(curUser);
			histogramVector  = userH.getHistogram();
		    }
		else
		    {
			histogramVector = hc.getHistogramOfEmailSize(curUser);
			userH = new UserHistogram(histogramVector, curUser);
			m_htHistogramsOfEmailSize.put(curUser, userH);
		    }
	    }
	else if(curAspect.equals(aspects[2])) // for num of attachment
	    {
		if(m_htHistogramsOfAttachment.containsKey(curUser))
		    {
			userH = (UserHistogram)m_htHistogramsOfAttachment.get(curUser);
			histogramVector  = userH.getHistogram();
		    }
		else
		    {
			histogramVector = hc.getHistogramOfAttachment(curUser);
			userH = new UserHistogram(histogramVector, curUser);
			m_htHistogramsOfAttachment.put(curUser, userH);
	    
		    }
	    }
	else // number of recipient
	    {
		if(m_htHistogramsOfRecipient.containsKey(curUser))
		    {
			userH = (UserHistogram)m_htHistogramsOfRecipient.get(curUser);
			histogramVector  = userH.getHistogram();
		    }
		else
		    {
			histogramVector = hc.getHistogramOfRecipient(curUser,m_winGui.getUserInfo(curUser));
			userH = new UserHistogram(histogramVector, curUser);
			m_htHistogramsOfRecipient.put(curUser, userH);
	    
		    }
	    }   

	return histogramVector;
    }
    

    /**
     * re-find the similar users.
     *
     * @param curUser the cur user
     * @param userList the user list
     * @throws SQLException the sQL exception
     */
    private final void RefreshTable(String curUser,String userList[]) throws SQLException
    {
	
	DefaultTableModel model = (DefaultTableModel)m_listUsers.getModel();
	model.setRowCount(0);
	// clear table
	//int length = model.getRowCount();
	//for(int i=length-1;i>=0;i--)
	//   model.removeRow(i);
	
	
	
	//model.clear();
	m_similarUsers = findSimilarUsers(curUser,userList); //result is in m_similarUsers;
	
	// no similar user;
	if(m_similarUsers.length<=0)
	{
		return;
	}

	UserDistance[] vec = sort(m_similarUsers);

	int size = vec.length;

	for(int i=0;i<size;i++)
	    {
		UserDistance s = vec[i];//(UserDistance)vec.elementAt(i);
		// model.addElement(s);
		String[] tt = new String[2];
		tt[0]=s.getName();
		tt[1]=s.getDistance();
		model.addRow(tt);
	    }
	
	
    }



    /**
     * return the vector of similar user's name.
     *
     * @param curUser the cur user
     * @param userList the user list
     * @return the user distance[]
     * @throws SQLException the sQL exception
     */
    public final UserDistance[] findSimilarUsers(String curUser,String userList[]) throws SQLException{

    	if(m_htHistograms.containsKey(curUser))
	    {
		
	    }
    	else
	    {
			m_htHistograms.put(curUser, new UserHistogram(hc.getHistogram(curUser,m_winGui.getUserInfo(curUser)), curUser));
	    }
    	
    	
    	
    	
    	
    	
    ArrayList m_similarUsers2 = new ArrayList();	
	//DistanceCompute DC = new DistanceCompute();

	double threshhold = 0.5; // default threshhold for comparison
	
	int len = userList.length; 
	    // m_comboUsers.getItemCount();
	
	if(len==0)
	    return new UserDistance[0];
	
	//int curr = m_winGui.getSelectedUserIndex();//get ith number of user of interest
	    //m_comboUsers.getSelectedIndex();

	String method = (String)m_comparisonMethod.getSelectedItem();
	int level = m_confidenceLevel.getValue();
	
	threshhold = getThreshhold(method, level);
	
	
	//compare the following two vector/histogram
	double[] baseV;
	
	if(curAspect.equals(aspects[0]))//# of email
	    baseV = ((UserHistogram)m_htHistograms.get(curUser)).getHistogram();
	else if(curAspect.equals(aspects[1]))//email size
	    baseV = ((UserHistogram)m_htHistogramsOfEmailSize.get(curUser)).getHistogram();
	else if(curAspect.equals(aspects[2]))//attachment
	    baseV = ((UserHistogram)m_htHistogramsOfAttachment.get(curUser)).getHistogram();
	else //recipient
	    baseV = ((UserHistogram)m_htHistogramsOfRecipient.get(curUser)).getHistogram();
	
	

	double[] toCompare;// = new Vector();

	

	
	System.out.println("--------------------------------------------");
	System.out.println("The base user is: "+curUser);
	BW = new BusyWindow("Similiar User","Progress",true);
	BW.progress(0,len);
	BW.setVisible(true);
	for(int i=0;i<len;i++)
	    {
		BW.progress(i,len);
		//if(i == curr) //don't need to calculate the selected user
		  //  continue;
	
		//user = m_winGui.getUserAt(i);
		

		if(curAspect.equals(aspects[0])) //# of email
		    {
			if(m_htHistograms.containsKey(userList[i]))
			    {
				userH = (UserHistogram)m_htHistograms.get(userList[i]);
				toCompare  = userH.getHistogram();
			    }
			else
			    {

				toCompare = hc.getHistogram(userList[i],m_winGui.getUserInfo(userList[i]));
				userH = new UserHistogram(toCompare, userList[i]);
				m_htHistograms.put(userList[i], userH);
			    }
		    }
		else if(curAspect.equals(aspects[1])) //email size
		    {
			if(m_htHistogramsOfEmailSize.containsKey(userList[i]))
			    {
				userH = (UserHistogram)m_htHistogramsOfEmailSize.get(userList[i]);
				toCompare  = userH.getHistogram();
			    }
			else
			    {

				toCompare = hc.getHistogramOfEmailSize(userList[i]);
				userH = new UserHistogram(toCompare, userList[i]);
				m_htHistogramsOfEmailSize.put(userList[i], userH);
			    }
		    }
		else if(curAspect.equals(aspects[2])) // for num of Attachment
		    {
			if(m_htHistogramsOfAttachment.containsKey(userList[i]))
			    {
				userH = (UserHistogram)m_htHistogramsOfAttachment.get(userList[i]);
				toCompare  = userH.getHistogram();
			    }
			else
			    {

				toCompare = hc.getHistogramOfAttachment(userList[i]);
				userH = new UserHistogram(toCompare, userList[i]);
				m_htHistogramsOfAttachment.put(userList[i], userH);
			    }

		    }
		else // # of recipient
		    {
			if(m_htHistogramsOfRecipient.containsKey(userList[i]))
			    {
				userH = (UserHistogram)m_htHistogramsOfRecipient.get(userList[i]);
				toCompare  = userH.getHistogram();
			    }
			else
			    {

				toCompare = hc.getHistogramOfRecipient(userList[i],m_winGui.getUserInfo(userList[i]));
				userH = new UserHistogram(toCompare, userList[i]);
				m_htHistogramsOfRecipient.put(userList[i], userH);
			    }

		    }
	  

		// histogram comparison, look for similar ones
		double distance;
		if(b3.isSelected()) //normal histogram
		    distance = DistanceCompute.getDistance(baseV, toCompare, method);
		else{ //do aligned histogram comparison
		    double[] V1 = align(baseV);
		    double[] V2 = align(toCompare);
		    distance = DistanceCompute.getDistance(V1, V2, method);
		}
	    
		//System.out.println("User is:"+user+", with dis:"+distance);

		// add to m_similarUsrs vector, global var
		if(distance<threshhold && withinRegion(baseV, toCompare, level))
		    {
			//UserDistance temp = new UserDistance(userList[i], distance);

			m_similarUsers2.add(new UserDistance(userList[i], distance));
		    }
	
	    }//for
	BW.setVisible(false);
	return (UserDistance[])m_similarUsers2.toArray(new UserDistance[m_similarUsers2.size()]);
    }

    
    //align the histogram to its first non-zero bin
    /**
     * Align.
     *
     * @param vec the vec
     * @return the double[]
     */
    private final double[] align(double[] vec){
	double[] ret = new double[vec.length];//new Vector();
	int start=0;
	int size = vec.length;//vec.size();
	int i=0;
	
	//fist need to find the new start for the alignment
	double r1 = vec[0];//((Double)vec.elementAt(0)).doubleValue();
	if(r1<=0){ //find the first non-zero one
	    double d = r1;
	    i=1;
	    while(d<=0 && i<size){
		d = vec[i];//((Double)vec.elementAt(i)).doubleValue();
		i++;
	    }
	    if(i==size) //all bins are 0
		start = 0;
	    else
		start = i-1;
	}
	// the first bin is non-zero, need to find the first non-zero one after 0 gap
	else{ 
	    double d = r1;
	    i=1;
	    while(d>0 && i<size){//try to find the first 0
		d = vec[i];//((Double)vec.elementAt(i)).doubleValue();
		i++;
	    }

	    if(i==size) //all non-0
		start = 0;

	    else{ //start of the 0 period

		int gap_begin = i;
		while(d<=0 && i<size){
		    d = vec[i];//((Double)vec.elementAt(i)).doubleValue();
		    i++;
		}
		if(i==size)
		    start = 0;
		else{
		    int gap = i-gap_begin; //the width of the 0 gap
		    //think it's a valid gap only if it's wider than 5, other just chaos
		    if(gap>=5)
			start = i-1;
		    else
			start = 0;
		}
	    }
	    

	}

	alignPoint = start;

	//start point not changed, the same vector
	if(start==0)
	    return vec;

	//do the alignment
	for(i=0;i<24;i++){
	    double dd = vec[(start+i)%24];//(Double)vec.elementAt( (start+i)%24);
	    ret[i] = dd;//ret.addElement(dd);
	}

	return ret;
    }

    //to find all the user groups
    /**
     * Find all user groups.
     *
     * @throws SQLException the sQL exception
     */
    private final void FindAllUserGroups() throws SQLException
    {

	Vector leftUsers = new Vector(); //keep the users to compute groups on
	Vector groups = new Vector(); //it's a vector of vector
	Vector groupMember; //one group with members


	//get current aspect
	Object selected = m_aspect.getSelectedItem();
	if (selected == null)
	    curAspect = aspects[0]; //by default compare avg # of emails
	else
	    curAspect = (String)selected;


	int len =m_winGui.getUserCount(); 
	    //m_comboUsers.getItemCount();
	String method = (String)m_comparisonMethod.getSelectedItem();
	int level = m_confidenceLevel.getValue();

	String user;
	System.out.println("starting get histogram for all users....");
	for(int i=0;i<len;i++){
	    user = m_winGui.getUserAt(i);
	    retrieveHistogram(user, curAspect);
	    leftUsers.add(user);
	}//for
	System.out.println("finished getting histogram, start calculate groups...");

	Hashtable ht = new Hashtable();
	if(curAspect.equals(aspects[0])) //# of email
	    {
		ht=m_htHistograms;
	    }
	else if(curAspect.equals(aspects[1])){
	    ht=m_htHistogramsOfEmailSize;
	} 
	else if(curAspect.equals(aspects[2])){
	    ht=m_htHistogramsOfAttachment;
	}
	else{
	    ht=m_htHistogramsOfRecipient;
	}

	while(leftUsers.size()>0){

	    user= (String)leftUsers.elementAt(0);
	    groupMember = findUserGroup(user, leftUsers, ht, method, level);
	    groups.add(groupMember);
	}

	//for testing result
	for(int i=0;i<groups.size();i++){
	    Vector V = (Vector)groups.elementAt(i);
	    //System.out.println("\nGroup "+i);
	    for(int j=0; j<V.size();j++){
		String s = ((UserDistance)V.elementAt(j)).getName();
		//System.out.println("User: "+s);
	    }
	}

	//clear out
	m_histogram.removeAll();
	m_histogram.repaint();

	//add to table
	DefaultTableModel model = (DefaultTableModel)m_listUsers.getModel();
	
	// clear table
	int length = model.getRowCount();
	for(int i=length-1;i>=0;i--)
	    model.removeRow(i);
	

	String[] tt = new String[2];
	UserDistance s;

	int rows =0;
	for(int i=0;i<groups.size();i++){
	 
	    
	    tt[0]="GROUP "+i;
	    tt[1]="";
	    model.addRow(tt);

	    	    
	    Vector V = (Vector)groups.elementAt(i);
	    if(V.size()<=0)
		continue;
	    
	    UserDistance first=(UserDistance)V.remove(0);
	    tt[0]=first.getName();
	    tt[1]=first.getDistance();
	    model.addRow(tt);

	    if(V.size() <=0){
		V.insertElementAt(first, 0);
		continue;
	    }

	    UserDistance[] vec = sort((UserDistance[])V.toArray(new UserDistance[V.size()]));
	    for(int j=0;j<vec.length;j++)
		{
		    s = vec[j];//(UserDistance)vec.elementAt(j);
		    tt[0]=s.getName();
		    tt[1]=s.getDistance();
		    model.addRow(tt);

		}
	    V.insertElementAt(first, 0);
	}
	
	//pass the result to winGui
	Vector vec = new Vector();
	for(int i=0;i<groups.size();i++){

	    Vector V = (Vector)groups.elementAt(i);
	    if(V.size()<=0)
		continue;

	    StringBuffer sb = new StringBuffer();
	    sb.append("(");
	    for(int j=0;j<V.size();j++)
		{
	
		    s = (UserDistance)V.elementAt(j);
		    String acct =s.getName();
		    sb.append("'").append(acct).append("'");
		    if(j<V.size()-1)
			sb.append(",");
		}
	    sb.append(")");
	     
	    vec.addElement(sb.toString());
	}

	m_winGui.setGroups(vec);

	//for test
	//	for(int i=0;i<vec.size();i++){
	//   System.out.println((String)vec.elementAt(i));
	//}
    }

    //find out the user group similar to baseUser, using Histograms in ht
    /**
     * Find user group.
     *
     * @param baseUser the base user
     * @param users the users
     * @param ht the ht
     * @param method the method
     * @param level the level
     * @return the vector
     */
    private final Vector findUserGroup(String baseUser, Vector users, Hashtable ht, String method, int level){

	//DistanceCompute DC = new DistanceCompute();
	Vector result = new Vector();
	double threshhold = 0.5; // default threshhold for comparison
	threshhold = getThreshhold(method, level);
	
	result.addElement(new UserDistance(baseUser, 0));
	users.remove(baseUser);

	double[] baseV = ((UserHistogram)ht.get(baseUser)).getHistogram();
	double[] toCompare;
	
	for(int i=0;i<users.size();i++){
	    
	    String user = (String)users.elementAt(i);
	    toCompare = ((UserHistogram)ht.get(user)).getHistogram();
	    
	    // histogram comparison, look for similar ones
	    double distance;
	    if(b3.isSelected())
		distance = DistanceCompute.getDistance(baseV, toCompare, method);
	    else{
		double[] V1 = align(baseV);
		double[] V2 = align(toCompare);
		distance = DistanceCompute.getDistance(V1, V2, method);
	    }
	    

	    // add to m_similarUsrs vector, global var
	    if(distance<threshhold && withinRegion(baseV, toCompare, level))
		{
		    //User temp = new User(user, distance);
		    UserDistance temp = new UserDistance(user, distance);
		    result.addElement(temp);
		    users.remove(user);
		    i--;

		}
	}


	return result;
    }

    
    // compare the total number of V1, V2, if the difference is within some percentage of V1, return true. V1 is base, V2 is toCompare
    /**
     * Within region.
     *
     * @param V1 the v1
     * @param V2 the v2
     * @param level the level
     * @return true, if successful
     */
    private final boolean withinRegion(double[] V1, double[] V2, int level){

	double sum1 = getSum(V1);
	double sum2 = getSum(V2);

	if(sum1 ==0)
	    return false;

	double high = 0.2; // for the highest similar level, within 20%
	double low = 0.8; //for the lowest level

	double region = (Math.abs(sum1 - sum2)) / sum1;

	double sh = high+(low-high)*((double)(10-level)/(double)10); //the percentage should be

	//System.out.println("sum1="+sum1+",sum2 ="+sum2+",region:"+region+", sh:"+sh);

	if(region<=sh) return true;

	return false;
	
    }

    // get the sum of the element
    /**
     * Gets the sum.
     *
     * @param V the v
     * @return the sum
     */
    private final double getSum(double[] V){
	
	double sum =0;

	for(int i=0;i<V.length;i++){
	    sum += V[i];//((Double)V.elementAt(i)).doubleValue();
	}
	
	return sum;
    }
    
    // need to add sth. Consider method when return threshhold
    /**
     * Gets the threshhold.
     *
     * @param method the method
     * @param level the level
     * @return the threshhold
     */
    private final double getThreshhold(String method, int level){

	double t = 0.5; //default
	
	double high, low;


	if(method.equals("L1-form")){
	    high = 0.4;
	    low = 1;
	}
	else if(method.equals("Quadratic")){
	    high = 0.5;
	    low = 1.2;
	}
	else if(method.equals("L2-form")){
	    high = 0.15;
	    low = 0.6;

	}
	else //if(method.equalsIgnoreCase("KS-test"))
	    {
		high = 0.3;
		low = 0.6;
	    }

	t = high+(low-high)*((double)(10-level)/(double)10);
	
	return t;
    }

    //sort according to distance, increasingly
    //bubblesort
    /**
     * Sort.
     *
     * @param V the v
     * @return the user distance[]
     */
    private final UserDistance[] sort(UserDistance[] V){
	
    	int size =V.length;
    //	UserDistance[] vec = new UserDistance[size];
	//Vector vec = new Vector();
   
	if(size<=0)
	    return V;
 	
	
	UserDistance temp;
	//vec.addElement(V.elementAt(0));

	for(int i=0;i<size;i++){
	    //UserDistance u = V[i];//(UserDistance)V.elementAt(i);
	    double d = V[i].getDistNum();

	   //int j=0; int len = vec.size();
	    for(int j=i+1;j<size;j++){

		double d2 = V[j].getDistNum();//((UserDistance)vec.elementAt(j)).getDistNum();
		if(d>d2){
		   // vec.insertElementAt(u,j);
		   temp =V[j];
		   V[j] = V[i];
		   V[i] = temp;
		   d =  temp.getDistNum();
			
			
		}
		
	    }// for j

	    // if greater  than any one esle
	   //if(j==len) 
	//	vec.addElement(u);
	    
	}//for i
	
	return V;
    }

    
    //for the list selection
    /**
     * Draw plot.
     */
    private final void drawPlot()
    {

	int row = m_listUsers.getSelectedRow();
	if(row == -1)
	    return;

	String name = (String)m_listUsers.getValueAt(row,0);
	String value = (String)m_listUsers.getValueAt(row,1);

	if((name.startsWith("GROUP") || name.equals("")) && value.equals("")) //as the  title for group
	    return;

	double[] vec;

	if(curAspect.equalsIgnoreCase(aspects[0])) //# of email
	    vec = ((UserHistogram)m_htHistograms.get(name)).getHistogram();
	else if(curAspect.equalsIgnoreCase(aspects[1])) //email size 
	    vec = ((UserHistogram)m_htHistogramsOfEmailSize.get(name)).getHistogram();
	else if(curAspect.equalsIgnoreCase(aspects[2])) //attachment
	    vec = ((UserHistogram)m_htHistogramsOfAttachment.get(name)).getHistogram();
	else
	    vec = ((UserHistogram)m_htHistogramsOfRecipient.get(name)).getHistogram();

	// show the plot
	m_query.removeAll();

	paintHistogram(vec, m_query);
	m_query.repaint();

    }
    

    /**
     * Paint histogram.
     *
     * @param vec the vec
     * @param plot the plot
     */
    private final void paintHistogram(double[] vec, JPlot2D plot){

	//	double[] s = new double[2];
	//s[0]=1; s[1]=24;
	double[] x = new double[24];
	//double[] y = new double[24];
	
	for(int i=0;i<24;i++){
	    x[i]=i+1;
	    //y[i]=((Double)vec.elementAt(i)).doubleValue();
	}

	plot.setPlotType(JPlot2D.BAR);
	plot.setXScale(s1by24);
	plot.addCurve(x,vec);

	//	plot.setPlotType(JPlot2D.BAR);
	plot.setBackgroundColor(Color.white);
	plot.setFillColor(Color.green);

	//	plot.setXScale(s);
	//plot.addCurve(x,y);

	//if the histogram is aligned, draw out the anchor
	if(b4.isSelected()){
	    double[] x1 = new double[24];
	    double[] y1 = new double[24];
	    for(int i=0;i<24;i++){
		x1[i]=i+1;
		y1[i]=0;
	    }
	    align(vec);
	    y1[alignPoint]= vec[alignPoint];//((Double)vec.elementAt(alignPoint)).doubleValue();
	    System.out.println("align point is: "+alignPoint);
	    //y1[alignPoint]=0.5;
	    plot.addCurve(x1, y1);
	    //plot.setPlotType(JPlot2D.LINEAR);
	    plot.setFillColor(Color.red);
	   
	}
    }


    /*
    private void initCombo(String arg) throws SQLException
    {
	//populate
	String data[];

	m_comboUsers.removeAllItems();

	
	//	System.out.println("inside init/combo");
	synchronized(m_jdbcView){
	    
	    if(arg.equalsIgnoreCase("all")){
		//m_jdbcView.getSqlData("select distinct sender from email order by sender");
		//data=m_jdbcView.getColumnData(0);
		data = (String[])m_winGui.getUserList();

		
	    }
	    else {
		//m_jdbcView.getSqlData("select distinct sender,count(*) from email group by sender having count(*)>100");
		m_jdbcView.getSqlData("select distinct sender from email group by sender having count(*) > 200");
		data = m_jdbcView.getColumnData(0);
		//where folder like '%sent%' order by sender");
		/*
		  String q="create temporary table tt(sender varchar(125), count int)";
		  m_jdbcView.executeQuery(q);

		  try{
		  String qq = "insert into tt select sender, count(*) as count from email group by sender";
		  m_jdbcView.executeQuery(qq);

		  qq="select sender from tt where count>50";
		  m_jdbcView.getSqlData(qq);
		  data = m_jdbcView.getRowData();
		  }
		  finally{
		  m_jdbcView.executeQuery("drop table tt");
		  }
		/
	    }
	   
	    
	}// end of synchronized

	
	ComboBoxModel model = new DefaultComboBoxModel(data);
	m_comboUsers.setModel(model);
		if(m_comboUsers.getItemCount() >1)
		    m_comboUsers.setSelectedIndex(0);
		/*
	//System.out.println("da len: "+data.length);
	for(int i=0;i<data.length;i++){

	    //    if(data[i].length>2){
	    //throw new SQLException("Database returned wrong number of columns");
	    //}
	    
	    if(data[i][0].length() >0){ //non-null sender name
		//m_comboUsers.insertItemAt((String)data[i][0],i);
		m_comboUsers.addItem((String)data[i][0]);
		m_hsUsers.add((String)data[i][0]);
		
		if(m_comboUsers.getItemCount() ==1)
		    m_comboUsers.setSelectedIndex(0);
	    }
	} //for
		/

    }
    */

    /*
    public final void refreshCombo() throws SQLException
    {
	// remember current selection
	String strCurrent = new String("");
	Object objCurrent = m_comboUsers.getSelectedItem();
	if (objCurrent != null)
	    {
		strCurrent = (String) objCurrent;
	    }

	// repopulate
	String data[][];
	synchronized(m_jdbcView)
	    {
		if(b1.isSelected()){
		    m_jdbcView.getSqlData("select distinct sender from email order by sender");

		}
		else {
		    //m_jdbcView.getSqlData("select distinct sender,count(*) from email group by sender having count(*)>'100'");
		    m_jdbcView.getSqlData("select distinct sender from email where folder like '%sent%' order by sender");
		    // where folder like '%sent%' order by sender");
		}

		data = m_jdbcView.getRowData();
	    }

	for (int i=0; i<data.length; i++)
	    {
		if (data[i].length > 2)
		    {
			throw new SQLException("Database returned wrong number of columns");
		    }
		if (data[i][0].length() > 0)
		    {
			if (m_hsUsers.add(data[i][0]))
			    {
				int j=0;
				while ((j < m_comboUsers.getItemCount()) &&
				       (data[i][0].compareToIgnoreCase((String) m_comboUsers.getItemAt(j)) > 0))
				    {
					j++;
				    }
				m_comboUsers.insertItemAt((String) data[i][0], j);
				if (m_comboUsers.getItemCount() == 1)
				    {
					m_comboUsers.setSelectedIndex(0);
				    }
			    }
		    }

		// replace selection
		if (strCurrent.equals((String)data[i][0]))
		    {
			m_comboUsers.setSelectedItem((String)data[i][0]);
		    }
	    }//end of for
    }


    */
 

    /*
    public final void Refresh() throws SQLException
    {
	refreshCombo();
    }
    */

}



