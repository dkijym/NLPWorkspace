package metdemo.Window;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.lang.Double;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import metdemo.winGui;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.DataBase.DatabaseConnectionException;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.SortTableModel;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.SHAvgCommTime;
import metdemo.dataStructures.CliqueEdge;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;
import metdemo.dataStructures.singleMessage;
import metdemo.dataStructures.sparseIntMatrix;
import metdemo.dataStructures.vipUser;
import metdemo.CliqueTools.CliqueFinder;
import metdemo.CliqueTools.CliqueFinder.AccountPoint;
import metdemo.CliqueTools.CliqueFinder.oneClique;
import metdemo.Finance.SHNetworks;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


// TODO: Auto-generated Javadoc
/**
 * For the detection of the Social Hierarchy in an organization.
 *
 * @author Ryan Rowe 06/12/06
 */

public class SHDetector extends JScrollPane implements MouseListener {

	/** The m_win gui. */
	private winGui m_winGui;
	
	/** The SAV e_ file. */
	private final String SAVE_FILE = "cached_SHDETECTORdata";
	//private SHAvgCommTime shact;
	/** The calculatedcur result. */
	private vipUser[] calculatedcurResult;
	
	/** The seen user list. */
	private String[] seenUserList; //needed in case we load up some results, will need a specific user list to look at
	
	/** The m_alias filename. */
	private String m_aliasFilename;
	
	/** The m_dgraph. */
	private sparseIntMatrix m_dgraph;
	
	/** The m_hashgraph. */
	private HashMap<String,HashMap> m_hashgraph;
	
	/** The panel. */
	private JPanel panel;
	
	/** The m_spinner model recent start. */
	private SpinnerDateModel m_spinnerModelRecentStart;
	
	/** The m_spinner model recent end. */
	private SpinnerDateModel m_spinnerModelRecentEnd;
	
	/** The ss threshold. */
	private JTextField emailNum, cliquesNum, tsThreshold, ssThreshold;
	
	/** The run button. */
	private JButton runButton;
	
	/** The refresh button. */
	private JButton refreshButton; 
	
	/** The visualize. */
	private JButton visualize;
	
	/** The save data. */
	private JButton saveData;
	
	/** The m_text min msgs. */
	private JTextField m_textMinMsgs;
	
	/** The tree scroll pane. */
	private JScrollPane treeScrollPane;
	
	/** The g_tree. */
	private JTree g_tree;//global tree
    
    /** The m_text area. */
    private JTextArea m_textArea;
	
	/** The m_count. */
	private JLabel m_count;
    
    /** The m_messages_model. */
    public DefaultTableModel m_messages_model;
    
    /** The jdbc view. */
    private EMTDatabaseConnection jdbcView = null;
    
    /** The help object. */
    private EMTHelp helpObject;
    
    /** The constraints. */
    private GridBagConstraints constraints;
    
    /** The gridbag. */
    private GridBagLayout gridbag;
    
    /** The wcs weight. */
    private JSpinner emailWeight, artWeight, nrWeight, numCliquesWeight, rcsWeight, wcsWeight;
    
    /** The sm6. */
    private SpinnerNumberModel sm1, sm2, sm3, sm4, sm5, sm6;
     

	/** The m_time table. */
	private JTable m_timeTable;
	
	/** The s pane. */
	private JScrollPane sPane;
	
	/** The m_md5 renderer. */
	private md5CellRenderer m_md5Renderer;

	/** The clipboard. */
	final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	
	/*
	 * Constructor method
	 */
	/**
	 * Instantiates a new sH detector.
	 *
	 * @param jdbcView the jdbc view
	 * @param alert the alert
	 * @param md5Renderer the md5 renderer
	 * @param wg the wg
	 * @param attachments the attachments
	 * @param aliasFilename the alias filename
	 * @throws Exception the exception
	 */
	public SHDetector(final EMTDatabaseConnection jdbcView, final ReportForensicWindow alert,
			final md5CellRenderer md5Renderer, final winGui wg, String[]attachments, 
			String aliasFilename) throws Exception {

		if (jdbcView == null) {
			throw new NullPointerException();
		}
m_aliasFilename = aliasFilename;
		//initiate the cell renderer
		m_md5Renderer = md5Renderer;
		
		//initiate database object
		this.jdbcView = jdbcView;
		
		//new winGui window (JFrame)
		m_winGui = wg;
		//m_winGui.changeUserList(winGui.typeBOTH); //BOTH IS ERROR PRONE
		
		//create the communicateTime object
		//shact = new SHAvgCommTime(jdbcView, m_winGui);
		
		//create the cliqueFinder object		
		//m_cliqueFinder = new CliqueFinder(jdbcView, aliasFilename);
		//cliqueGraph = new HashMap<String,HashMap>();
		//emailList = new ArrayList<String>();
		
		
		
		//overall jpanel for everything (GridBagLayout)
		panel = new JPanel();
		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel.setLayout(gridbag);

		//average comm time options panel
		JPanel options1 = new JPanel();
		options1.setLayout(new FlowLayout(FlowLayout.CENTER,10,0));
		options1.add(new JLabel("Start Date:"));
		m_spinnerModelRecentStart = new SpinnerDateModel();
		Calendar recentcal = new GregorianCalendar();
		recentcal.set(2002, 7, 15);
		recentcal.add(Calendar.DATE, -7);
		String[] dateRange = m_winGui.getDateRange_toptoolbar();
		//m_spinnerModelRecentStart.setValue(recentcal.getTime());
		Date min = Utils.deSqlizeDateYEAR(dateRange[0]);
		m_spinnerModelRecentStart.setValue(min);
		JSpinner spinnerRecentStart = new JSpinner(m_spinnerModelRecentStart);
		JSpinner.DateEditor editorRecentStart = new JSpinner.DateEditor(spinnerRecentStart, "MMM dd, yyyy   ");
		spinnerRecentStart.setEditor(editorRecentStart);
		options1.add(spinnerRecentStart);
		options1.add(new JLabel("   End Date:"));
		m_spinnerModelRecentEnd = new SpinnerDateModel();
		Calendar recentcalend = new GregorianCalendar();
		recentcalend.set(2002, 7, 15);
		//m_spinnerModelRecentEnd.setValue(recentcalend.getTime());
		Date max = Utils.deSqlizeDateYEAR(dateRange[1]);
		m_spinnerModelRecentEnd.setValue(max);
		JSpinner spinnerRecentEnd = new JSpinner(m_spinnerModelRecentEnd);
		JSpinner.DateEditor editorRecentEnd = new JSpinner.DateEditor(spinnerRecentEnd, "MMM dd, yyyy   ");
		spinnerRecentEnd.setEditor(editorRecentEnd);
		m_textMinMsgs = new JTextField("30", 3);
	    m_textMinMsgs.setToolTipText("Min # of messages exchanged pairwise");
		runButton = new JButton("Run");
		runButton.setToolTipText("press this button to show relative average comm time to the selected user");
		helpObject = new EMTHelp(EMTHelp.SHDETECTOR);
		options1.add(spinnerRecentEnd);
		options1.add(new JLabel("   Min # Messages:"));
		options1.add(m_textMinMsgs);
		options1.add(runButton);
		options1.add(helpObject);

		
		//clique options panel
		/*JPanel options2 = new JPanel();
		options2.setLayout(new FlowLayout(FlowLayout.CENTER,10,0));		
		options2.add(new JLabel("Type of Attachment:"));
		ComboBoxModel comboModel = new DefaultComboBoxModel(attachments);
		attachType = new JComboBox(comboModel);
		attachType.insertItemAt(new String("All types"),0);
		attachType.insertItemAt(new String("All text types"),1);
		attachType.setToolTipText("Choose type of email attachmant to analyze in message window");
		String[] algorithms = {"Raw Time","Raw Inverse","Response Order"};
		ComboBoxModel comboModel2 = new DefaultComboBoxModel(algorithms);
		algoType = new JComboBox(comboModel2);
		algoType.setToolTipText("Choose which algorithm you'd like to use (see help for details)");
		runButton = new JButton("Run");
		runButton.setToolTipText("press this button to show relative average comm time to the selected user");
		options2.add(attachType);
		options2.add(new JLabel("Algorithm:"));
		options2.add(algoType);		
		enron151_checkBox = new JCheckBox();
		enron151_checkBox.setToolTipText("Check this box if you are working with the 'Enron North America' dataset only");
		//options2.add(enron151_checkBox); 
		options2.add(runButton);
		attachType.setSelectedIndex(0);
			    
		//help and run Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		helpObject = new EMTHelp(EMTHelp.SHDETECTOR);
		buttonPanel.add(helpObject);*/
		
		
		//first row of refreshable options
		JPanel threshP = new JPanel();
		JPanel t1p = new JPanel();
		JPanel t2p = new JPanel();
		JPanel t3p = new JPanel();
		t1p.setLayout(new FlowLayout());
		t2p.setLayout(new FlowLayout());
		t3p.setLayout(new FlowLayout());
		threshP.setLayout(new FlowLayout(FlowLayout.CENTER,30,0));
		t1p.add(new JLabel("Email Threshold: "));
		emailNum = new JTextField();
		emailNum.setColumns(3);
		emailNum.setText("1"); //default all accounts
		t1p.add(emailNum);
		t2p.add(new JLabel("Clique Threshold: "));
		cliquesNum = new JTextField();
		cliquesNum.setColumns(3);
		cliquesNum.setText("1"); //default all accounts
		t2p.add(cliquesNum);
		t3p.add(new JLabel("SocialScore Threshold: "));
		ssThreshold = new JTextField();
		ssThreshold.setColumns(4);
		ssThreshold.setText("0.0"); //default all accounts
		t3p.add(ssThreshold);
		threshP.add(t1p);
		threshP.add(t2p);
		threshP.add(t3p);
		
		
		//second row of refreshable options
		JPanel weightP = new JPanel();
		JPanel w1p = new JPanel();
		JPanel w2p = new JPanel();
		JPanel w3p = new JPanel();
		JPanel w4p = new JPanel();
		JPanel w5p = new JPanel();
		JPanel w6p = new JPanel();
		w1p.setLayout(new FlowLayout());
		w2p.setLayout(new FlowLayout());
		w3p.setLayout(new FlowLayout());
		w4p.setLayout(new FlowLayout());
		w5p.setLayout(new FlowLayout());
		w6p.setLayout(new FlowLayout());
		sm1 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		sm2 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		sm3 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		sm4 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		sm5 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		sm6 = new SpinnerNumberModel(1.0,0.0,1.0,0.1);
		weightP.setLayout(new FlowLayout(FlowLayout.CENTER,10,0));
		weightP.add(new JLabel("Weights:"));
		w1p.add(new JLabel("#Email"));
		emailWeight = new JSpinner(sm1);
		emailWeight.setPreferredSize(new Dimension(45,25));
		emailWeight.setFocusable(false);
		w1p.add(emailWeight);
		w2p.add(new JLabel("Avg Time"));
		artWeight = new JSpinner(sm2);
		artWeight.setPreferredSize(new Dimension(45,25));
		artWeight.setFocusable(false);
		w2p.add(artWeight);
		w3p.add(new JLabel("#Responses"));
		nrWeight = new JSpinner(sm3);
		nrWeight.setPreferredSize(new Dimension(45,25));
		nrWeight.setFocusable(false);
		w3p.add(nrWeight);
		w4p.add(new JLabel("#Cliques"));
		numCliquesWeight = new JSpinner(sm4);
		numCliquesWeight.setPreferredSize(new Dimension(45,25));
		numCliquesWeight.setFocusable(false);
		w4p.add(numCliquesWeight);
		w5p.add(new JLabel("RCS"));
		rcsWeight = new JSpinner(sm5);
		rcsWeight.setPreferredSize(new Dimension(45,25));
		rcsWeight.setFocusable(false);
		w5p.add(rcsWeight);
		w6p.add(new JLabel("WCS"));
		wcsWeight = new JSpinner(sm6);
		wcsWeight.setPreferredSize(new Dimension(45,25));
		wcsWeight.setFocusable(false);
		w6p.add(wcsWeight);
		weightP.add(w1p);
		weightP.add(w2p);
		weightP.add(w3p);
		weightP.add(w4p);
		weightP.add(w5p);
		weightP.add(w6p);
		
		
		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Press to refresh the table and plot according to the thresholds.");
		refreshButton.setEnabled(false);
		weightP.add(refreshButton);
	
	
		//averageCommTime table panel
		SortTableModel model = new SortTableModel();
		model.addColumn("Account");
		model.addColumn("# Email");
		model.addColumn("Avg Time");
		model.addColumn("# Responses");
		model.addColumn("ResponseScore");
		model.addColumn("# Cliques");
		model.addColumn("RCS");
		model.addColumn("WCS");	
		model.addColumn("SocialScore");
		m_timeTable = new JTable(model);
		model.addMouseListenerToHeaderInTable(m_timeTable);
		m_timeTable.setRowSelectionAllowed(true);
		m_timeTable.setColumnSelectionAllowed(false);
		m_timeTable.getTableHeader().setReorderingAllowed(false);
		m_timeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_timeTable.setDefaultRenderer(model.getColumnClass(1), md5Renderer);
		sPane = new JScrollPane(m_timeTable);
		sPane.setPreferredSize(new Dimension(700, 350));

		
		//cliqueCount panel
		m_count = new JLabel("Clique Tree View");
		JPanel countPanel = new JPanel(new FlowLayout());
		countPanel.add(m_count);
		
		
		//enclaveCliques tree panel
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Email Cliques");
		g_tree = new JTree(top);
		g_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeScrollPane = new JScrollPane(g_tree);
		treeScrollPane.setPreferredSize(new Dimension(700, 200));
		m_textArea = new JTextArea("Press Refresh to Generate Cliques");
		m_textArea.setEditable(false);
		
		
		//copy button panels
		JPanel bottomPanel = new JPanel(new FlowLayout());
		JButton copy = new JButton("Copy to Clipboard");
		copy.setToolTipText("To copy the table contents to system clipboard");
		visualize = new JButton("View Networks");
		visualize.setToolTipText("To visualize the structure of the Social Hierarchy");
		visualize.setEnabled(false);
		JButton mapDatabase = new JButton("Map Database");
		JButton forVanessa = new JButton("For Vanessa");
		mapDatabase.setToolTipText("Click to create the desired subset of the database");
		forVanessa.setToolTipText("Click to create this version of the files for Vanessa");
		//String[] sizes = {"4","9","15","22","40","60","80","117","140"};
		//ComboBoxModel sizesModel = new DefaultComboBoxModel(sizes);
		//sizeChoice = new JComboBox(sizesModel);
		
		//shlomo added for saving data to file
		saveData = new JButton("Save Table Data");
		saveData.setEnabled(false);
		JButton loadData = new JButton("Load Table Data");
		
		
		
		bottomPanel.add(copy);
		bottomPanel.add(visualize);
		bottomPanel.add(saveData);
		bottomPanel.add(loadData);
		
		
		//work for saving table data
		saveData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    FileOutputStream out = new FileOutputStream(SAVE_FILE);
                    GZIPOutputStream out2 = new GZIPOutputStream(out);
                    ObjectOutputStream s = new ObjectOutputStream(out2);
                   

                    //s.writeInt(m_htAttachments.size());
                    s.writeInt(calculatedcurResult.length);
                    for (int i = 0; i < calculatedcurResult.length; i++){
                    	s.writeObject(calculatedcurResult[i]);
                        }
                    s.writeInt(seenUserList.length);
                    for(int i=0;i < seenUserList.length; i++) {
                    	s.writeObject(seenUserList[i]);
                    }
                    s.writeObject(m_aliasFilename);
                    //	private sparseIntMatrix m_dgraph;
                    // 	private HashMap<String,HashMap> m_hashgraph;
                    s.writeObject(m_dgraph);
                    s.writeObject(m_hashgraph);
                    
                    
                    s.flush();
                    s.close();
                    
                    JOptionPane.showMessageDialog(null, "done saving computation");

                } catch (Exception e1) {
                    System.out.println(e1);
                }
            }
        });
		
		
		loadData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BusyWindow bw = new BusyWindow("Loading saved data", "Progress",true);
                   
                    FileInputStream in = new FileInputStream(SAVE_FILE);//"saved_hashinfo.zip");
                    GZIPInputStream gin = new GZIPInputStream(in);
                    ObjectInputStream s = new ObjectInputStream(gin);
                    
                    int size = s.readInt();
                    calculatedcurResult = new vipUser[size];
                    bw.progress(0, size);
                    bw.setVisible(true);
                    for (int i = 0; i < size; i++) {
                        bw.progress(i, size);
                        calculatedcurResult[i] = (vipUser)s.readObject();

                    }
                    size = s.readInt();
                    bw.progress(0, size);
                    seenUserList = new String[size];
                    for (int i=0;i<size;i++)
                    {   		
                    		bw.progress(i, size);
                    		seenUserList[i] = (String)s.readObject();
                    }
                    m_aliasFilename = (String)s.readObject();
                    //	private sparseIntMatrix m_dgraph;
                    // 	private HashMap<String,HashMap> m_hashgraph;
                    m_dgraph = (sparseIntMatrix)s.readObject();
                    m_hashgraph = (HashMap<String,HashMap>)s.readObject();
                    
                     
                    bw.setVisible(false);
                    JOptionPane.showMessageDialog(null, "Finished Loading Table Data");
                  refreshListener ref = new refreshListener();
                  ref.actionPerformed(e);
                //    refreshButton.action(e, null);
                } catch (Exception e2) {
                    System.out.println(e2);
                }
                
                visualize.setEnabled(true);
                saveData.setEnabled(true);
                refreshButton.setEnabled(true);
            }

        });
		
		
		
		
		//bottomPanel.add(mapDatabase);
		//bottomPanel.add(forVanessa);
		//bottomPanel.add(sizeChoice);
		//bottomPanel.add(readIn);
		
		
		//add everything to the GridBagLayout
		/*constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets(0, 0, 30, 0);
		gridbag.setConstraints(headerTitle, constraints);
		constraints.insets = new Insets(0, 0, 0, 0);
		panel.add(headerTitle);*/
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.gridheight = 2;
		gridbag.setConstraints(options1, constraints);
		panel.add(options1);
		
		/*constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		gridbag.setConstraints(options2, constraints);
		panel.add(options2);*/
		
		/*constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonPanel, constraints);
		panel.add(buttonPanel);*/
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets(30, 0, 0, 0);
		gridbag.setConstraints(threshP, constraints);
		panel.add(threshP);
		constraints.insets = new Insets(0, 0, 0, 0);
		
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;  
		gridbag.setConstraints(weightP, constraints);
		panel.add(weightP);
		
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		gridbag.setConstraints(sPane, constraints);
		panel.add(sPane);
		
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		gridbag.setConstraints(bottomPanel, constraints);
		panel.add(bottomPanel);
			
		/*constraints.gridx = 1;
		constraints.gridy = 7;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		gridbag.setConstraints(copy2, constraints);
		panel.add(copy2);	*/
		
		/*constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets(10, 0, 0, 0);
		gridbag.setConstraints(countPanel, constraints);
		panel.add(countPanel);	
		constraints.insets = new Insets(0, 0, 0, 0);*/
		
		/*constraints.gridx = 1;
		constraints.gridy = 8;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		gridbag.setConstraints(treeScrollPane, constraints);
		panel.add(treeScrollPane);*/
			
	
		//add all the button ActionListeners
		runButton.addActionListener(new runbuttonListener());
		refreshButton.addActionListener(new refreshListener());
		copy.addActionListener(new copyListener());
		visualize.addActionListener(new visualizeListener());
		mapDatabase.addActionListener(new mapDatabaseListener());
		forVanessa.addActionListener(new forVanessaListener());
		

		//add MouseListener
		m_timeTable.addMouseListener(this);
		
		
		//show it!
		setViewportView(panel);

	}//end of constructor
	
	
	/**
	 * Execute.
	 */
	public final void Execute() {
		Object selected = m_winGui.getSelectedUser();
		if (selected == null)
			return;
		//curUser = (String) selected;
	}
	
	//for map database button
	/**
	 * The listener interface for receiving mapDatabase events.
	 * The class that is interested in processing a mapDatabase
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addmapDatabaseListener<code> method. When
	 * the mapDatabase event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see mapDatabaseEvent
	 */
	class mapDatabaseListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			mapDatabase();
		}		
	}
	
	//for forVanessa button
	/**
	 * The listener interface for receiving forVanessa events.
	 * The class that is interested in processing a forVanessa
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addforVanessaListener<code> method. When
	 * the forVanessa event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see forVanessaEvent
	 */
	class forVanessaListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			forVanessa();
		}		
	}

	
	/** The dformat. */
	static DecimalFormat dformat = new DecimalFormat("###0.000");
	//for copy button
	/**
	 * The listener interface for receiving copy events.
	 * The class that is interested in processing a copy
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addcopyListener<code> method. When
	 * the copy event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see copyEvent
	 */
	class copyListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			//String selection = jt.getSelectedText();
			int j = 0;
			SortTableModel smodel = (SortTableModel) m_timeTable.getModel();
			Vector select = smodel.getDataVector();
			String neat = new String();
			for (int i = 0; i < select.size(); i++) {
				Vector v = (Vector) select.elementAt(i);
				for (j = 0; j < v.size() - 1; j++){
				
					Object o = v.elementAt(j);
					if(o instanceof Double){
						neat += dformat.format((Double)o) + ",";
					}else
					{
					neat += o.toString() + ",";
					}
					
					
				}
				//neat += v.elementAt(j) + "\n";
				Object o = v.elementAt(j);
				if(o instanceof Double){
					neat += dformat.format((Double)o) + "\n";
				}else
				{
				neat += o.toString() + "\n";
				}
			}
			StringSelection data = new StringSelection(neat);
			clipboard.setContents(data, data);
			String[] temp = m_winGui.getUserList();
			System.out.println(temp.length+" users in list");
		}
	}
	
	/**
	 * The listener interface for receiving visualize events.
	 * The class that is interested in processing a visualize
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addvisualizeListener<code> method. When
	 * the visualize event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see visualizeEvent
	 */
	class visualizeListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent event) {
			try {	
				
				//shlomo - lets grab the data for us from uservip list
				vipUser[] curResult = calculatedcurResult.clone();
				SHAvgCommTime shact = new SHAvgCommTime(jdbcView, m_winGui);
				shact.setDGraph(m_dgraph);
				//shact. init(seenUserList);
				double [] minMax = new double[12];
				 minMax = getMinAndMax(curResult,0,minMax); //get the min and max values for num emails and avgCommTime
				 curResult = calcResponseScore(curResult,minMax); //calculate the responseScore for each user returned above
				 HashMap<String,HashMap> cliqueGraph = getCliques(curResult,m_hashgraph,Integer.parseInt(m_textMinMsgs.getText())); //get the Clique statistics for each user returned above
				 curResult= calcNumCliques(curResult); //calc the number of cliques for each user
				 curResult =  calcRCS(curResult); //calculate the raw clique score for each user
				 curResult= calcWCS(curResult); //calculate the weighted clique score for each user
				minMax = getMinAndMax(curResult,1,minMax); //get the min and max values for each property for each user reutnred above
				curResult = calcSocialScore(curResult,minMax); //get the SocialScore using everything
				curResult = calcPageRanks(curResult,shact,seenUserList); // get the pageRank for each user
				curResult= sortVIPList(curResult);
				 //end recalculating
				
				SHNetworks shn = new SHNetworks(m_md5Renderer);
				String fileNameNetwork = null;
				int minEdgeWeight = Integer.parseInt(m_textMinMsgs.getText());
			
				HashMap<String,vipUser> viphm = topNHashMap( curResult,50);
				//sparseIntMatrix dgraph = shact.getDGraph();
				//shlomo added
				shn.startFunction(fileNameNetwork, cliqueGraph, minEdgeWeight,viphm);
				
			} catch (IOException e) {
				add(new JLabel(e.toString()));
				e.printStackTrace();
			}
		}
	}
		
	//for run button
	/**
	 * The listener interface for receiving runbutton events.
	 * The class that is interested in processing a runbutton
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addrunbuttonListener<code> method. When
	 * the runbutton event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see runbuttonEvent
	 */
	class runbuttonListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			(new Thread(new Runnable() {
				public void run() {
					refreshButton.setEnabled(false);
					try {
						/*boolean chooseALL = true;
						String inputusers = new String();
						//bring up dailog to decide if constrained analysis:
						//will give the user a chance to back out also
						
						int choose = JOptionPane.showConfirmDialog(SHDetector.this,"Do you want to run analysis on all users from above ??\n (else will get constraints next)","Social Anlysis",JOptionPane.YES_NO_CANCEL_OPTION);
						if(choose == JOptionPane.CANCEL_OPTION){
							return; //nothing left to do
						}
						else if(choose == JOptionPane.YES_OPTION){
//							run button action for averageCommTime
							 
							chooseALL = true;
					  
						}else{
							chooseALL = false;
							//want to constrain the user set
							 inputusers = JOptionPane.showInputDialog(SHDetector.this,"Please choose user constraints\n (Example %@columbia.edu for all emails with columbia.edu domain)", "%@%");
							
							 if(inputusers == null){
								 System.out.println("No user sql data entered correctly, quitting.....");
								 return;
							 }
							 
							 
							System.out.println("you entered: " + inputusers);
						}*/
						
						//todo the actual work here
						BusyWindow bw = new BusyWindow("Computing data...", "Progress",true);
						bw.setVisible(true);
						SortTableModel model = (SortTableModel) m_timeTable.getModel();
						model.setRowCount(0);
						String startDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
						String endDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());
					
						/*if(chooseALL){*/
								calculateAll(startDate, endDate,bw, m_winGui.getUserList());   
						/*}else{
							//do other stuff here
							//will need to build user list and pass it down
							//seenUserList = m_winGui.getUserList();
							try{
								String []dataList1 = jdbcView.getSQLDataByColumn("select distinct sender from email where sender like '"+inputusers+"'", 1);
								String []dataList2 = jdbcView.getSQLDataByColumn("select distinct rcpt from email where rcpt like '"+inputusers+"'", 1);
								
								HashMap<String, Integer> quickhash = new HashMap<String, Integer>();
								for(int i=0;i<dataList1.length;i++){
									quickhash.put(dataList1[i], 1);
								}
								for(int i=0;i<dataList2.length;i++){
									quickhash.put(dataList2[i], 1);
								}
								//pull out the keys which is the set of unique constrained users
								String userlists[] = new String[quickhash.size()];
								int i=0;
								for(Iterator<String> keysusers = quickhash.keySet().iterator();keysusers.hasNext();){
									userlists[i++] = keysusers.next();
								}
								
								calculateAll(startDate, endDate,bw, userlists);   
								
							}catch(SQLException sel){
								sel.printStackTrace();
							}
						}*/
						
						  //repaint the panel
						panel.repaint();
						bw.setVisible(false);
						
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(SHDetector.this, "Failed to compute for one user: " + ex);
						m_winGui.setEnabled(true);
					}
					refreshButton.setEnabled(true);
				}

			})).start();

		}
	}
	
	/* 
	 *	Method to read all emails into memory (for testing purposes)
	 */
	/**
	 * Read in all.
	 *
	 * @throws SQLException the sQL exception
	 */
	public void readInAll() throws SQLException{
		String[] allUsers = m_winGui.getUserList(); //get all users in the list
		HashMap[] allEmail = new HashMap[allUsers.length];
		String query;
		String[][] data;
		String startDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentStart.getDate());
		String endDate = Utils.mySqlizeDate2YEAR(m_spinnerModelRecentEnd.getDate());
		String condition = " dates BETWEEN'" + startDate + "' AND '" + endDate + "'";
		
		for(int j=0;j<allUsers.length;j++){
			String user = allUsers[j];
			synchronized(jdbcView){
				query = "SELECT sender, rcpt, utime FROM email WHERE (sender = '"
					+ user 
					+ "' OR rcpt = '" 
					+ user + "') AND (" 
					+ condition 
					+ ") ORDER BY utime";
				data = jdbcView.getSQLData(query);
			}

        	HashMap<String, Vector> accounts = new HashMap<String, Vector>();
        	int rows = data.length;
        	for(int i=0;i<rows; i++){
        		
        		String sender = data[i][0];
        		String rcpt = data[i][1];
        		String time = data[i][2];	
        		        		
        		if(sender.equalsIgnoreCase(rcpt)){ //ignore if the email is from the user to himself
        			continue;
        		}
        		
        		String keyUser;
        		singleMessage email;
        		if(sender.equals(user)){ //if current keyUser account is recipient, message sent by "user"
        			keyUser = rcpt;
        			email = new singleMessage(time, singleMessage.SEND);
        		} else { //if current keyUser account is sender, message received by "user"
        			keyUser = sender;
        			email = new singleMessage(time, singleMessage.RECEIVE);
        		}
        		
        		if(accounts.containsKey(keyUser)){ // if the account is already in the HashMap, add the email
        			Vector vec = (Vector)accounts.get(keyUser);
        			vec.addElement(email);
    	    		accounts.put(keyUser, vec);
        		} else { // if the account is not yet in the HashMap, add it and the email
        			Vector vec = new Vector();
        			vec.addElement(email);
        			accounts.put(keyUser, vec);	
        		}
        	}
        	allEmail[j] = accounts;
        	System.out.println("Read in emails for: " + user);
		}
	}

	/*
	public final void showCliques(String user)
    {
		ArrayList vCliques = new ArrayList();
		for(int j=0;j<cliques.size();j++){
			oneClique curClique = (oneClique)cliques.get(j); //return each clique
			Iterator enumWalker = curClique.getPoints().iterator(); 
			boolean foundIt = false;
			int rcs = 0;
			while (enumWalker.hasNext()){ //walk through all the accounts in the clique
				Point p = (Point) enumWalker.next();
				if(user.equals(p.getAccount())){
					foundIt = true; //found the current user in the clique
					break;
				}
			}
			if(foundIt){ //take action only if the current user is in the clique
				vCliques.add(curClique);
			}
		}
		
		int max = vCliques.size();

		m_count.setText("Number of Cliques: " +max);
		
		//setup the jtree		
		String str2 = new String();
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Email Cliques Groups");
		for(int i=0;i<max;i++){
			//oneClique is defined in cliquefinder class
			//top.add(new DefaultMutableTreeNode(new CliqueNode((oneClique)vCliques.get(i))));
			oneClique v = (oneClique)vCliques.get(i);
			ArrayList vec = v.getSubjectWords();
			str2 = new String("");
			for (int j=0; j<8 && j<vec.size(); j++){
				if (j != 0)
				    {
					str2 += ", ";
				    }
				
				str2 += vec.get(j);
			}
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(str2);
			top.add(group);
		
			Iterator enumWalker = v.getPoints().iterator();
			while (enumWalker.hasNext()){
				Point p = (Point) enumWalker.next();
				group.add(new DefaultMutableTreeNode(p.getAccount()));
			}
		}
	
		g_tree = new JTree(top);
		//end tree
		g_tree.getSelectionModel().setSelectionMode
		    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	
		panel.remove(panel.getComponentCount()-1);
		treeScrollPane = new JScrollPane(g_tree);
		treeScrollPane.setPreferredSize(new Dimension(700, 200));
		constraints.gridx = 1;
		constraints.gridy = 7;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		gridbag.setConstraints(treeScrollPane, constraints);
		panel.add(treeScrollPane);
		panel.repaint();
    } */
	


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
						//clear the table
						SortTableModel model = (SortTableModel) m_timeTable.getModel();
						model.setRowCount(0);

						//int level = Integer.parseInt(emailWeight.getText().trim());
						runButton.setEnabled(false);
					//shlomo - grab the data nd use it
						vipUser[] curResult =  calculatedcurResult.clone();
						if (curResult == null)
							throw new SQLException("Error: The Current Result is empty, can't refresh");
//						initialize the 10 element minMax array
						double [] minMax = new double[12];
						minMax = getMinAndMax(curResult,0,minMax); //get the min and max values for num emails and avgCommTime
						calcResponseScore(curResult,minMax);
						calcWCS(curResult);
						minMax = getMinAndMax(curResult,1,minMax);
						calcSocialScore(curResult,minMax);
						//refresh the table
						SortTableModel srt = (SortTableModel) m_timeTable.getModel();
						refreshTable(curResult,srt);
						runButton.setEnabled(true);
						panel.repaint();
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(SHDetector.this, "Hit run first...table error: " + ex);
						m_winGui.setEnabled(true);
					}

				}
			})).start();

		}
	}


	//required by the MouseListener implementation
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public final void mouseClicked(MouseEvent e) {
    }
    

	/**
	 * Refresh.
	 *
	 * @throws SQLException the sQL exception
	 */
	public final void Refresh() throws SQLException {
	}

	
	/*
	 * Method for getting an array of accounts with averageCommTime and number of emails
	 */
	/**
	 * Gets the avg comm time.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param shact the shact
	 * @param allUsers the all users
	 * @param bw the bw
	 * @return the avg comm time
	 * @throws SQLException the sQL exception
	 */
	public vipUser[] getAvgCommTime(String startDate, String endDate, SHAvgCommTime shact,String allUsers[],BusyWindow bw )throws SQLException
	{
		
		//Vector vec = new Vector();
		//HashMap<String, vipUser> viphm = new HashMap<String, vipUser>();
		//String condition = " dates BETWEEN'" + startDate + "' AND '" + endDate + "'";
		
		
		//vipUser[] total;
		
		
		//not used shlomo
		//int j=0;
		
		int progressCount = 1;		
		for(String user : allUsers){
			bw.progress(progressCount++);
		//shlomo: speedup lets not do time length
//			long timeBefore = System.currentTimeMillis();
			//int algo = 0;
			
			shact.avgUserTime(user); //call main computation method in SHAvgCommTime class
			//long timeDiff = System.currentTimeMillis() - timeBefore;
			bw.setMSG("Completed analysis for " +user);//shlomo: speedup+ " in "+( System.currentTimeMillis() - timeBefore)+" millis");
		}

		return shact.getVipListArray();
		/*viphm = shact.getVipList();
		total = convertToArray(viphm);
		return total;*/
	}

	
	/*
	 * Method for retreiving all of the EnclaveCliques and determining who is in which
	 */
	/**
	 * Gets the cliques.
	 *
	 * @param vipList the vip list
	 * @param shact the shact
	 * @param minEdgeWeight the min edge weight
	 * @return the cliques
	 */
	public HashMap<String, HashMap>   getCliques(vipUser[] vipList,HashMap<String,HashMap> shact, int minEdgeWeight){
		//vipUser[] vipList = curResult;
		ArrayList cliques = null;   
		HashMap<String,HashMap> cliqueGraph = null;
		//compute all the cliques using the graph created during avgCommTime calculations
		try {
			CliqueFinder m_cliqueFinder = new CliqueFinder(jdbcView, m_aliasFilename);
			
			//cliqueGraph = shact.getGraph();
			cliqueGraph=pruneEdges(minEdgeWeight,shact);
			cliques = m_cliqueFinder.findCliques(cliqueGraph,false);
		} catch (Exception ex){
			JOptionPane.showMessageDialog(this, "Could not get cliques: " + ex.toString()); //catch the exception if thrown
		}	
		
		HashMap<String, vipUser> viphm = convertToHashMap(vipList); //convert to a HashMap
		
		for(int j=0;j<cliques.size();j++){
			oneClique curClique = (oneClique)cliques.get(j); //return each clique
			ArrayList<AccountPoint> points = curClique.getPoints();
			Iterator iter = points.iterator(); 
			Double rcs = Math.pow(2,points.size()-1);
			Double wcs = new Double(0);
			while (iter.hasNext()){ //walk through all the accounts in the clique
				AccountPoint p = (AccountPoint) iter.next();
				if(viphm.containsKey(p.getAccount())){ //take action if the vip HashMap contains the current account
					vipUser curUser = viphm.get(p.getAccount()); //save the old vipUser object
					ArrayList<oneClique> curCliqueList = curUser.getCliqueList(); //save the old cliqueList
					curCliqueList.add(curClique); //add the current clique to the current users' cliqueList
					curUser.setCliqueList(curCliqueList); //replace the old cliqueList with the new
					viphm.put(curUser.getAcct(),curUser);  //replace the old vipUser object with the new
					wcs += rcs*curUser.getResponseScore();
				}
			}
			curClique.setRCS(rcs.intValue()); //set the raw clique score for the current clique
			curClique.setWCS(wcs.intValue()); //set the weighted clique score for the current clique
			cliques.remove(j);
			cliques.add(j,curClique);
		}
		
		vipList = convertToArray(viphm); //convert back to an array
		return cliqueGraph;//vipList; //set the array to the master curResult object
	}
	
	
	/*
	 * Method for calculating the number of cliques for each member in the VIPList
	 */
	/**
	 * Calc num cliques.
	 *
	 * @param vipList the vip list
	 * @return the vip user[]
	 */
	public vipUser[] calcNumCliques(vipUser[] vipList){
		//vipUser[] vipList = curResult;
		for(int i=0;i<vipList.length;i++){
			ArrayList<oneClique> curCliqueList = vipList[i].getCliqueList();
			vipList[i].setNumCliques(curCliqueList.size());
		}
		return vipList;
	}
	
	
	/*
	 * Method for calculating the raw clique score for each member in the VIPList
	 */
	/**
	 * Calc rcs.
	 *
	 * @param vipList the vip list
	 * @return the vip user[]
	 */
	public vipUser[]  calcRCS(vipUser[] vipList){
		//vipUser[] vipList = curResult;
		for(int i=0;i<vipList.length;i++){
			int rcs = 0;
			ArrayList<oneClique> curCliqueList = vipList[i].getCliqueList();
			for(int j=0;j<curCliqueList.size();j++){
				oneClique curClique = curCliqueList.get(j);
				rcs += curClique.getRCS();
			}
			vipList[i].setRawCliqueScore(rcs);
		}
		return vipList;
	}
	
	
	/*
	 * Method for calculating the weighted clique score for each member in the VIPList
	 */
	/**
	 * Calc wcs.
	 *
	 * @param vipList the vip list
	 * @return the vip user[]
	 */
	public vipUser[]  calcWCS(vipUser[] vipList){
		//vipUser[] vipList = curResult;
		
		for(int i=0;i<vipList.length;i++){
			int wcs = 0;
			ArrayList<oneClique> curCliqueList = vipList[i].getCliqueList();
			for(int j=0;j<curCliqueList.size();j++){
				oneClique curClique = curCliqueList.get(j);
				wcs += curClique.getWCS();
			}
			vipList[i].setWeightedCliqueScore(wcs);
		}
		return vipList;
	}
	
	
	/*
	 * Method for calculating the responseScore of each member in the VIPList
	 */
	/**
	 * Calc response score.
	 *
	 * @param vipList the vip list
	 * @param minMax the min max
	 * @return the vip user[]
	 */
	public vipUser[]  calcResponseScore(vipUser[] vipList,  double[] minMax){
		//vipUser[] vipList = curResult;
		double ne, art, nr, rsVal;
		Double w1, w2, w3; //weights for each score (should add up to 1)
	
		//w1 = (Double)emailWeight.getValue();
		w2 = 3.0;
		w3 = 1.0;
		
		//calculate the "normalized" scores for each property (normalized to [0,1] scale)
		for(int i=0;i<vipList.length;i++){
			ne = ((vipList[i].getNumEmails() - minMax[0])/(minMax[1] - minMax[0]));
			if(vipList[i].getAvgResponseTime() == -1.0){
				art = 0;
			} else {
				art = 1 - ((vipList[i].getAvgResponseTime() - minMax[2])/(minMax[3] - minMax[2]));
			}
			nr = ((vipList[i].getNumResponses() - minMax[4])/(minMax[5] - minMax[4]));
			if(w2+w3 == 0){
				rsVal = 0;
			} else {
				rsVal = (w2*art + w3*nr)/(w2+w3);
			}
			vipList[i].setResponseScore(rsVal);
		}
		
		return  vipList;
	}
	
	
/**
 * Method for calculating the pageRank for each vipUser.
 *
 * @param vipList the vip list
 * @param shact the shact
 * @param allUsers the all users
 * @return the vip user[]
 */
	public vipUser[]  calcPageRanks(vipUser[] vipList, SHAvgCommTime shact,String[] allUsers){
		//vipUser[] vipList = curResult;
		//SHNetworks shn = new SHNetworks(m_md5Renderer); //initialize an SHNetworks object
		sparseIntMatrix dgraph = new sparseIntMatrix(allUsers.length);//shact.getDGraph(); //get the directed graph matrix
		//String[] allUsers = m_winGui.getUserList(); //get all users in the list
		int minEdgeWeight = Integer.parseInt(m_textMinMsgs.getText()); //get the min edge weight
		HashMap<String,vipUser> viphm = convertToHashMap(vipList);
		HashMap<String,Double> pageRanks = SHNetworks.getPageRanking(dgraph, minEdgeWeight, viphm, allUsers);
		Iterator iter = pageRanks.keySet().iterator();
		while(iter.hasNext()){
			String acct = (String)iter.next();
			double score = pageRanks.get(acct);
			vipUser vip = viphm.get(acct);
			vip.setPageRank(score);
			viphm.put(acct,vip);
		}
		return convertToArray(viphm);
	}
	
	
	/*
	 * Method for calculating the SocialScore of each member in the VIPList
	 */
	/**
	 * Calc social score.
	 *
	 * @param vipList the vip list
	 * @param minMax the min max
	 * @return the vip user[]
	 */
	public vipUser[]  calcSocialScore(vipUser[] vipList,double[] minMax){
		//vipUser[] vipList = curResult;		
		double ne, art, nr, nc, rcs, wcs, ssVal;
		Double w1, w2, w3, w4, w5, w6; //weights for each score (should add up to 1)
	
		w1 = (Double)emailWeight.getValue();
		w2 = (Double)artWeight.getValue();
		w3 = (Double)nrWeight.getValue();
		w4 = (Double)numCliquesWeight.getValue();
		w5 = (Double)rcsWeight.getValue();
		w6 = (Double)wcsWeight.getValue();
		
		//calculate the "normalized" scores for each property (normalized to 0-100 scale)
		for(int i=0;i<vipList.length;i++){
			ne = 100*((vipList[i].getNumEmails() - minMax[0])/(minMax[1] - minMax[0]));
			if(vipList[i].getAvgResponseTime() == -1.0){
				art = 0.0;
			} else {
				art = 100 - 100*((vipList[i].getAvgResponseTime() - minMax[2])/(minMax[3] - minMax[2]));
			}
			nr = 100*((vipList[i].getNumResponses() - minMax[4])/(minMax[5] - minMax[4]));
			nc = 100*((vipList[i].getNumCliques() - minMax[6])/(minMax[7] - minMax[6]));
			rcs = 100*((vipList[i].getRawCliqueScore() - minMax[8])/(minMax[9] - minMax[8]));
			wcs = 100*((vipList[i].getWeightedCliqueScore() - minMax[10])/(minMax[11] - minMax[10]));
			if(w1+w2+w3+w4+w5+w6 == 0){
				ssVal = 0;
			} else {
				ssVal = (w1*ne + w2*art + w3*nr + w4*nc + w5*rcs + w6*wcs)/(w1+w2+w3+w4+w5+w6);
			}
			vipList[i].setSocialScore(ssVal);
		}
		
		return sortVIPList(vipList);
	}
	
	
	/*
	 * Method for calculating the minimum and maximum values for each property of a vipList
	 */
	/**
	 * Gets the min and max.
	 *
	 * @param vipList the vip list
	 * @param type the type
	 * @param minMax the min max
	 * @return the min and max
	 */
	public double[] getMinAndMax(vipUser[] vipList,int type,double [] minMax){
		
		//vipUser[] vipList = curResult;

		    
		double neMin, neMax, artMin, artMax, nrMin, nrMax, ncMin, ncMax, rcsMin, rcsMax, wcsMin, wcsMax;
		
		if(type == 0){
			//initialize the min and max values to be the first account
			neMin = vipList[0].getNumEmails();
			neMax = vipList[0].getNumEmails();
			artMin = vipList[0].getAvgResponseTime();
			artMax = vipList[0].getAvgResponseTime();
			nrMin = vipList[0].getNumResponses();
			nrMax = vipList[0].getNumResponses(); 
			
			for(int i=1;i<vipList.length;i++){ // step through the entire VIPList
				double ne = vipList[i].getNumEmails();
				if(ne < neMin)
					neMin = ne;		// check for min and max
				else if(ne > neMax)	// num emails
					neMax = vipList[i].getNumEmails();
				
				double art = vipList[i].getAvgResponseTime();
				if(art < artMin && art != -1.0)
					artMin = art; 				// check for min and max
				else if(art > artMax && art != -1.0)	// average response time
					artMax = art;
				
				double nr = vipList[i].getNumResponses();
				if(nr < nrMin)
					nrMin = nr;		// check for min and max
				else if(nr > nrMax)	// orderScore
					nrMax = nr;
			}
			
			minMax[0] = neMin;
			minMax[1] = neMax;
			minMax[2] = artMin;
			minMax[3] = artMax;
			minMax[4] = nrMin;
			minMax[5] = nrMax;
			
		} else {
			//initialize the min and max values to be the first account
			ncMin = vipList[0].getNumCliques();
			ncMax = vipList[0].getNumCliques();
			rcsMin = vipList[0].getRawCliqueScore();
			rcsMax = vipList[0].getRawCliqueScore();
			wcsMin = vipList[0].getWeightedCliqueScore();
			wcsMax = vipList[0].getWeightedCliqueScore();
			
			for(int i=1;i<vipList.length;i++){ // step through the entire VIPList			
				double nc = vipList[i].getNumCliques();
				if(nc < ncMin)
					ncMin = nc;		// check for min and max
				else if(nc > ncMax)	// num cliques
					ncMax = nc;
				
				double rcs = vipList[i].getRawCliqueScore();
				if(rcs < rcsMin)
					rcsMin = rcs;		// check for min and max
				else if(rcs > rcsMax)	// raw clique score
					rcsMax = rcs;
				
				double wcs = vipList[i].getWeightedCliqueScore();
				if(wcs < wcsMin)
					wcsMin = wcs;		// check for min and max
				else if(wcs > wcsMax)	// weighted cliquescore
					wcsMax = wcs;
			}
			
			minMax[6] = ncMin;
			minMax[7] = ncMax;
			minMax[8] = rcsMin;
			minMax[9] = rcsMax;
			minMax[10] = wcsMin;
			minMax[11] = wcsMax;
		}
		
		
		
		return minMax;
	}
	
	
	/*
	 *  Method for creating an ArrayList of enron NA employees (ONLY FOR ENRON NA RESEARCH) 
	 */
	/**
	 * Filter enron na.
	 *
	 * @param vipList the vip list
	 * @return the vip user[]
	 */
	public vipUser[] filterEnronNA(vipUser[] vipList){
		String[] enronNA_A = {"tim.belden@enron.com",
								"carla.hoffman@enron.com",
								"cara.semperger@enron.com",
								"sean.crandall@enron.com",
								"jeff.richter@enron.com",
								"robert.badeer@enron.com",
								"bill.williams@enron.com",
								"diana.scholtes@enron.com",
								"greg.wolfe@enron.com",
								"tom.alonso@enron.com",
								"chris.foster@enron.com",
								"debra.davidson@enron.com",
								"mark.guzman@enron.com",
								"mike.swerzbin@enron.com",
								"mark.fischer@enron.com",
								"monika.causholli@enron.com",
								"matt.motley@enron.com",
								"kim.ward@enron.com",
								"chris.mallory@enron.com",
								"heather.dunton@enron.com",
								"holden.salisbury@enron.com",
								"anna.mehrer@enron.com",
								"lisa.gang@enron.com",
								"chris.stokley@enron.com",
								"john.forney@enron.com",
								"john.malowney@enron.com",
								"steve.swain@enron.com",
								"caroline.emmert@enron.com",
								"phillip.platter@enron.com",
								"geir.solberg@enron.com",
								"tim.heizenrader@enron.com",
								"jesse.bryson@enron.com",
								"lester.rawson@enron.com",
								"paul.choi@enron.com",
								"ryan.slinger@enron.com",
								"stanley.cocke@enron.com",
								"mike.purcell@enron.com",
								"donna.johnson@enron.com",
								"holli.krebs@enron.com",
								"kourtney.nelson@enron.com",
								"cooper.richey@enron.com",
								"kathy.axford@enron.com",
								"stewart.rosman@enron.com",
								"maria.van@enron.com",
								"julie.sarnowski@enron.com",
								"stacy.runswick@enron.com",
								"smith.day@enron.com",
								"donald.robinson@enron.com",
								"murray.oneil@enron.com",
								"lani.pennington@enron.com",
								"robert.anderson@enron.com",
								"lisa.mattingly@enron.com",
								"brett.hunsucker@enron.com",
								"michael.driscoll@enron.com",
								"jeremy.morris@enron.com"};
		
		ArrayList<String> enronNA_AL = new ArrayList<String>();
		for(int i=0;i<enronNA_A.length;i++){
			enronNA_AL.add(enronNA_A[i]);
		}
		
		//vipUser[] vipList = curResult;
		ArrayList<vipUser> enronNAVIPs = new ArrayList<vipUser>();
		
		for(int i=0;i<vipList.length;i++){
			String acct = vipList[i].getAcct();
			if(enronNA_AL.contains(acct)){
				enronNAVIPs.add(vipList[i]);
			}
		}
		
		vipUser[] temp = new vipUser[enronNAVIPs.size()];
		for(int i=0;i<temp.length;i++){
			temp[i] = enronNAVIPs.get(i);			
		}
		
		return sortVIPList(temp);
	}
	
	/**
	 * Filter enron151.
	 *
	 * @param vipList the vip list
	 * @return the vip user[]
	 */
	public vipUser[] filterEnron151(vipUser[] vipList){
		String[] enron151_A = new String[0]; //PUT LIST HERE
		
		ArrayList<String> enron151_AL = new ArrayList<String>();
		for(int i=0;i<enron151_A.length;i++){
			enron151_AL.add(enron151_A[i]);
		}
		
		//vipUser[] vipList = curResult;
		ArrayList<vipUser> enron151VIPs = new ArrayList<vipUser>();
		
		for(int i=0;i<vipList.length;i++){
			String acct = vipList[i].getAcct();
			if(enron151_AL.contains(acct)){
				enron151VIPs.add(vipList[i]);
			}
		}
		
		vipUser[] temp = new vipUser[enron151VIPs.size()];
		for(int i=0;i<temp.length;i++){
			temp[i] = enron151VIPs.get(i);			
		}
		
		return sortVIPList(temp);
	}

	/**
	 * Method for calculating all the VIP detection values across/for all users.
	 *
	 * @param startDate to start the analysis
	 * @param endDate end the analysis
	 * @param bw busywindow to show user the progress
	 * @param ulist unique list of users to look at
	 * @throws SQLException on start date being larger than end date
	 */
	private void calculateAll(String startDate, String endDate,BusyWindow bw, String []ulist) throws SQLException {
		if ((startDate.compareTo(endDate)) > 0) {
			throw (new SQLException("Start Date is larger than End Date... Please change the selected period..."));
		}
		
		//shlomo removed next line since being overwritten anyway 3 lines later
		//shlomo moved this local
		SHAvgCommTime shact = new SHAvgCommTime(jdbcView, m_winGui);

		//shlomo for speed lets not calculate time long time1 = System.currentTimeMillis();
		//moved this up
		//seenUserList = m_winGui.getUserList();
		seenUserList = ulist;
		//shlomo: makes more sense to do it here
		//initilaize the object
		shact.init(seenUserList); //initialize the shact member objects
		bw.setMax(seenUserList.length);
		
		vipUser[] curResult = getAvgCommTime(startDate, endDate,shact,seenUserList,bw); //get the Average Communication Times

		calculatedcurResult = curResult.clone(); //save a copy shlomo
		m_hashgraph = shact.getGraph();
		//SHLOMO make everything local work
		//		initialize the 10 element minMax array
		double [] minMax = new double[12];
		 minMax = getMinAndMax(curResult,0,minMax); //get the min and max values for num emails and avgCommTime
		 curResult =calcResponseScore(curResult,minMax); //calculate the responseScore for each user returned above
		 HashMap<String,HashMap> cliqueGraph= getCliques(curResult,m_hashgraph,Integer.parseInt(m_textMinMsgs.getText())); //get the Clique statistics for each user returned above
		 curResult =calcNumCliques(curResult); //calc the number of cliques for each user
		 curResult =calcRCS(curResult); //calculate the raw clique score for each user
		 curResult=calcWCS(curResult); //calculate the weighted clique score for each user
		minMax = getMinAndMax(curResult,1,minMax); //get the min and max values for each property for each user reutnred above
		curResult = calcSocialScore(curResult,minMax); //get the SocialScore using everything
		/*if(enron151_checkBox.isSelected()){
			filterEnron151(); //filter out accounts not from Enron North America (ONLY FOR ENRON NA RESEARCH)
		}*/
		curResult =  calcPageRanks(curResult,shact,seenUserList); // get the pageRank for each user
		//shlomo - speed up lets not calculate time lenght
		//long time2 = System.currentTimeMillis();
		//long diff = time2-time1;
		//System.out.println(diff + " milliseconds");
		
		visualize.setEnabled(true);
		saveData.setEnabled(true);
		
		curResult = sortVIPList(curResult);
		
		int count = 0;
		for (int i = 0; i < curResult.length; i++) {
			vipUser u = curResult[i];
			if (u.getAvgResponseTime() >= 0 && u.getAvgResponseTime() < Double.MAX_VALUE) {
				count++;
			}
		}
		
		if (count > 0){
			SortTableModel srt = (SortTableModel) m_timeTable.getModel();
			refreshTable(curResult,srt);
			
		}
		m_dgraph = shact.getDGraph();
		
		
		
	}
	
	
/**
 * Method for removing edges which are less than the threshold of number of messages.
 *
 * @param minEdgeWeight the min edge weight
 * @param cliqueGraph the clique graph
 * @return the hash map
 */
	private HashMap<String,HashMap> pruneEdges(int minEdgeWeight,HashMap<String,HashMap> cliqueGraph){
		
		Set pointSet = cliqueGraph.entrySet();
		//int minEdgeWeight = Integer.parseInt(m_textMinMsgs.getText());
		Map.Entry[] aPoints = (Map.Entry[]) pointSet.toArray(new Map.Entry[0]);
		
		for (int i=0; i<aPoints.length; i++){
			Map.Entry point = aPoints[i];
			HashMap edges = (HashMap) point.getValue();
			Set edgeSet = edges.entrySet();
			Map.Entry[] aEdges = (Map.Entry[]) edgeSet.toArray(new Map.Entry[0]);
			for (int j=0; j<aEdges.length; j++){
				Map.Entry edge = aEdges[j];
				int weight = ((CliqueEdge) edge.getValue()).getWeight();
				if (weight < minEdgeWeight){
					edgeSet.remove(edge);
				}
			}
			if (edgeSet.isEmpty()){ // if no edges, this point can't be in a clique
				pointSet.remove(point);
			}
		}
		return cliqueGraph;
	}


/**
 * Refresh the table data.
 *
 * @param curResult the cur result
 * @param model the model
 * @throws SQLException the sQL exception
 */
	private void refreshTable(vipUser[] curResult,SortTableModel model) throws SQLException {

		if (curResult == null) {
			throw new SQLException("current result is null");
		}

		int num = 1;
		int cli = 1;
		double vip = 0;
		try {
			num = Integer.parseInt(emailNum.getText().trim());
			cli = Integer.parseInt(cliquesNum.getText().trim());
			vip = Double.parseDouble(ssThreshold.getText().trim());
		} catch (NumberFormatException e) {
			System.err.println("parse number error: " + e);
		}

		//SortTableModel model = (SortTableModel) m_timeTable.getModel();

		// clear table
		//int length = model.getRowCount();
		//for (int i = length - 1; i >= 0; i--)
		//	model.removeRow(i);
		model.setRowCount(0);
		
		Object[] curRow = new Object[9];
		//shlomo: index not being used
		//int index = 0;
		for (int i = 0; i < curResult.length; i++) {
			//copy will slow you down - shlomo
			//vipUser u = curResult[i];//(vipUser)curResult.elementAt(i);
			 
			if (curResult[i].getAvgResponseTime() == -1){
				curRow[2] = "N/A";	
			} else {
				curRow[2] = (new Double(curResult[i].getAvgResponseTime()));
			}
			
			if (curResult[i].getSocialScore() == -1 || curResult[i].getSocialScore() < vip)
				continue;
			
			if (curResult[i].getNumCliques() < cli)
				continue;

			if (curResult[i].getNumEmails() >= num)
				curRow[1] = (new Integer(curResult[i].getNumEmails()));
			else
				continue;
						
			curRow[0] = curResult[i].getAcct();
			curRow[3] = curResult[i].getNumResponses();
			curRow[4] = (new Double(curResult[i].getResponseScore()));
			curRow[5] = curResult[i].getNumCliques();
			curRow[6] = curResult[i].getRawCliqueScore();
			curRow[7] = curResult[i].getWeightedCliqueScore();
			curRow[8] = (new Double(curResult[i].getSocialScore()));

			model.addRow(curRow);
		}
	}

/**
 * Sort the lists of important people using arrays sort (quicksort).
 *
 * @param list the list
 * @return the vip user[]
 */
	private vipUser[] sortVIPList(vipUser[] list) {
		//vipUser tempu;
		if (list.length <= 0)
			return list;

	/*	for (int i = 0; i < list.length; i++) {
			double d = list[i].getSocialScore();
			for (int j = i + 1; j < list.length; j++) {
				double d2 = list[j].getSocialScore();
				if (d < d2) {
					tempu = list[j];
					list[j] = list[i];
					list[i] = tempu;
					d = tempu.getSocialScore();
				}
			}// for j
		}//for i

		return list;*/
		
		Arrays.sort(list);
		return list;	
	}
	
	/**
	 * get the vip list out of the keyset.
	 *
	 * @param hm the hm
	 * @return the vip user[]
	 */
	private vipUser[] convertToArray(HashMap<String, vipUser> hm){
		int size = hm.keySet().size();
		vipUser[] arr = new vipUser[size];
		Iterator iter = hm.values().iterator();
		int i = 0;
		while(iter.hasNext()){
			arr[i] = (vipUser)iter.next();
			i++;
		}
		return arr;		
	}
	
	/**
	 * convert the vip list to hash.
	 *
	 * @param arr the arr
	 * @return the hash map
	 */
	private HashMap<String, vipUser> convertToHashMap(vipUser[] arr){
		HashMap<String, vipUser> hm = new HashMap<String, vipUser>();
		for(vipUser user: arr){
			hm.put(user.getAcct(),user);
		}
		return hm;
	}
	
	/**
	 * pull out n vips and pass to hashmap.
	 *
	 * @param arr the arr
	 * @param n the n
	 * @return the hash map
	 */
	private HashMap<String, vipUser> topNHashMap(vipUser[] arr,int n){
		HashMap<String, vipUser> hm = new HashMap<String, vipUser>();
	
		for(int i=0;i < n && i < arr.length;i++){
			vipUser user = arr[i];
			hm.put(user.getAcct(),user);
		}
		return hm;
	}
	
	/**
	 * for vanessa analysis of enron stuff.
	 *
	 * @return the account list
	 */
	private ArrayList<String> getAccountList(){
		String[] accounts = {"louise.kitchen@enron.com",
				"mike.grigsby@enron.com",
				"liz.taylor@enron.com",
				"jeff.dasovich@enron.com",
				"tana.jones@enron.com",
				"kimberly.watson@enron.com",
				"sally.beck@enron.com",
				"michelle.lokay@enron.com",
				"steven.harris@enron.com",
				"sara.shackleton@enron.com",
				"kenneth.lay@enron.com",
				"lindy.donoho@enron.com",
				"richard.shapiro@enron.com",
				"lynn.blair@enron.com",
				"kevin.hyatt@enron.com",
				"drew.fossum@enron.com",
				"marie.heard@enron.com",
				"scott.neal@enron.com",
				"stephanie.panus@enron.com",
				"shelley.corman@enron.com",
				"carol.clair@enron.com",
				"matthew.lenhart@enron.com",
				"rod.hayslett@enron.com",
				"susan.bailey@enron.com",
				"d..steffes@enron.com",
				"kam.keiser@enron.com",
				"rosalee.fleming@enron.com",
				"teb.lokey@enron.com",
				"john.arnold@enron.com",
				"susan.scott@enron.com",
				"barry.tycholiz@enron.com",
				"greg.whalley@enron.com",
				"mark.mcconnell@enron.com",
				"j..kean@enron.com",
				"k..allen@enron.com",
				"gerald.nemec@enron.com",
				"s..shively@enron.com",
				"monique.sanchez@enron.com",
				"m..presto@enron.com",
				"jason.williams@enron.com",
				"paul.y barbo@enron.com",
				"kim.ward@enron.com",
				"elizabeth.sager@enron.com",
				"m..love@enron.com",
				"bill.rapp@enron.com",
				"tracy.geaccone@enron.com",
				"debra.perlingiere@enron.com",
				"chris.germany@enron.com",
				"errol.mclaughlin@enron.com",
				"mary.hain@enron.com",
				"jay.reitmeyer@enron.com",
				"e..haedicke@enron.com",
				"j..sturm@enron.com",
				"andy.zipper@enron.com",
				"eric.bass@enron.com",
				"taylor@enron.com",
				"mark.whitt@enron.com",
				"lysa.akin@enron.com",
				"a..martin@enron.com",
				"darrell.schoolcraft@enron.com",
				"stanley.horton@enron.com",
				"danny.mccarty@enron.com",
				"jason.wolfe@enron.com",
				"rick.buy@enron.com",
				"l..gay@enron.com",
				"frank.ermis@enron.com",
				"keith.holst@enron.com",
				"a..shankman@enron.com",
				"don.baughman@enron.com",
				"dutch.quigley@enron.com",
				"john.griffith@enron.com",
				"lavorato@enron.com",
				"kay.mann@enron.com",
				"larry.may@enron.com",
				"stacy.dickson@enron.com",
				"bill.williams@enron.com",
				"tori.kuykendall@enron.com",
				"kate.symes@enron.com",
				"mike.maggi@enron.com",
				"diana.scholtes@enron.com",
				"doug.gilbert-smith@enron.com",
				"dan.hyvl@enron.com",
				"matt.smith@enron.com",
				"jeff.skilling@enron.com",
				"cara.semperger@enron.com",
				"robert.badeer@enron.com",
				"jonathan.mckay@enron.com",
				"martin.cuilla@enron.com",
				"chris.dorland@enron.com",
				"t..lucci@enron.com",
				"b..sanders@enron.com",
				"james.derrick@enron.com",
				"dana.davis@enron.com",
				"john.zufferli@enron.com",
				"judy.townsend@enron.com",
				"mike.swerzbin@enron.com",
				"m..forney@enron.com",
				"f..brawner@enron.com",
				"jim.schwieger@enron.com",
				"w..white@enron.com",
				"sean.crandall@enron.com",
				"geoff.storey@enron.com",
				"brad.mckay@enron.com",
				"holden.salisbury@enron.com",
				"c..giron@enron.com",
				"michelle.cash@enron.com",
				"eric.saibi@enron.com",
				"theresa.staab@enron.com",
				"scott.hendrickson@enron.com",
				"geir.solberg@enron.com",
				"ryan.slinger@enron.com",
				"juan.hernandez@enron.com",
				"harry.arora@enron.com",
				"robert.benson@enron.com",
				"cooper.richey@enron.com",
				"vladi.pimenov@enron.com",
				"joe.stepenovitch@enron.com",
				"d..thomas@enron.com",
				"j.kaminski@enron.com",
				"andrea.ring@enron.com",
				"jeff.king@enron.com",
				"mike.carson@enron.com",
				"john.hodge@enron.com",
				"matt.motley@enron.com",
				"l..mims@enron.com",
				"joe.parks@enron.com",
				"benjamin.rogers@enron.com",
				"w..delainey@enron.com",
				"mark.guzman@enron.com",
				"chris.stokley@enron.com",
				"h..lewis@enron.com",
				"kevin.ruscitti@enron.com",
				"j..farmer@enron.com",
				"charles.weldon@enron.com",
				"craig.dean@enron.com",
				"lisa.gang@enron.com",
				"robin.rodrigue@enron.com",
				"peter.keavey@enron.com",
				"jane.tholt@enron.com",
				"richard.ring@enron.com",
				"phillip.platter@enron.com",
				"joe.quenet@enron.com",
				"albert.meyers@enron.com",
				"f..campbell@enron.com",
				"tom.donohoe@enron.com",
				"monika.causholli@enron.com",
				"eric.linder@enron.com",
				"steven.south@enron.com",
				"w..pereira@enron.com"};
		
		ArrayList<String> accounts_AL = new ArrayList<String>();
		for(int i=0;i<accounts.length;i++){
			accounts_AL.add(accounts[i]);
		}

		return accounts_AL;
	}
	
	/**
	 * vanessae stuff.
	 */
	private void mapDatabase(){
		//define the source database connection parameters
		String srcHost = "127.0.0.1";
		String srcName = "enron151";
		String srcUsername = "root";
		String srcPassword = "";
		String srcType = "MYSQL";
		
		//define the target database connection parameters
		String targetHost = "127.0.0.1";
		String targetName = "enronNAonly";
		String targetUsername = "root";
		String targetPassword = "";
		String targetType = "MYSQL";
		
		//create the database connection objects for both databases
		final EMTDatabaseConnection  sourceDB = DatabaseManager.LoadDB(srcHost, srcName, srcUsername, srcPassword,srcType);
		final EMTDatabaseConnection  targetDB = DatabaseManager.LoadDB(targetHost, targetName, targetUsername, targetPassword,targetType);
		
		//shutdown the connection to the main database (jdbcview)
		System.out.println("Shutting down connection to main database...");
		try{
			jdbcView.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the main database was successful...");
		
		//connect to the source database
		System.out.println("Connecting to the source database...");
		try{
			if(sourceDB.connect(true) == false){
				System.err.println("Couldn't connect to the source database, EMT will now exit...");
				System.exit(-1);
			}
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Connection to source database was successful...");
		
		//connect to the target database
		System.out.println("Connecting to the target database...");
		try{
			if(targetDB.connect(true) == false){
				System.err.println("Couldn't connect to the target database, EMT will now exit...");
				System.exit(-1);
			}
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Connection to target database was successful...");
		
		//---- map source data to target data ------------------------------------------
		String qE = "SELECT Email_id FROM employeelist";
		String[][] dataE, data1, data2;
		dataE = new String[0][0];
		ArrayList<String> accts151 = new ArrayList<String>();
		
		
		/** NOT FOR ENRON NA			
		//first query the source database for the employee list
		try{
			dataE = sourceDB.getSQLData(qE);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		//make an ArrayList full of the employee accts
		for(String[] acct : dataE){
			accts151.add(acct[0]);
		}*/	
		
		accts151 = getAccountList();
		
		int uid = 100000; //
		int totalCount = 0; //counter for progress updates
		int ridInc;
		
		//query the source database for all the messages
		//data1 = sourceDB.getSQLData(q1);
					
		//mid goes up to 3242063
		
		
		String lastMessageInput = "";
		ArrayList<String> rcptList = new ArrayList<String>();
					
		for(ridInc=1;ridInc<3250000;ridInc+=10000){
			String q1 = "SELECT m.mid, m.sender, r.rvalue, m.date, m.message_id, " +
					"m.subject, m.body, m.folder FROM message AS m INNER JOIN " +
					"recipientinfo AS r ON m.mid = r.mid WHERE r.rid BETWEEN " + 
					ridInc + " AND " + (ridInc+9999);
			data1 = new String[0][0];
			try{
				data1 = sourceDB.getSQLData(q1);
			} catch(SQLException e) {
				e.printStackTrace();
			}
			for(String[] row: data1){	
									
				//save each of the column inputs
				String mid = row[0];
				String sender = row[1];
				String rcpt = row[2];
				String date = row[3].split("\\.")[0];
				String message_id = row[4];
				String subject = row[5].replaceAll("\"","'");
				String body = row[6].replaceAll("\"","'");
				String folder = row[7].replaceAll("\"","'");
								
				//split and process the sender data (with error checking!)
				String sdom, szn;
				String ss[] = sender.split("@");
				String sname = ss[0];
				if(ss.length==2){
					sdom = ss[1];
					String ss2[] = ss[1].split("\\.");
					if(ss2.length==2){
						szn = ss2[1];
					} else {
						szn = "";
					}						
				} else {
					sdom = "";
					szn = "";
				}
				
				//split and process the rcpt data (with error checking!)
				String rdom, rzn;
				String rs[] = rcpt.split("@");
				String rname = rs[0];
				if(rs.length==2){
					rdom = rs[1];
					String rs2[] = rs[1].split("\\.");
					if(rs2.length==2){
						rzn = rs2[1];
					} else {
						rzn = "";
					}						
				} else {
					rdom = "";
					rzn = "";
				}

				//split and process the date data
				String ds[] = date.split(" ");
				String dates = ds[0];
				String times = ds[1];
				String ds2[] = dates.split("-");
				String ds3[] = times.split(":");
				String gmonth = ds2[1];
				String gyear = ds2[0];
				String ghour = ds3[0];
				String seconds = ds3[0];
				Calendar dateAndTime = Calendar.getInstance();
				int calYear = Integer.parseInt(ds2[0]);
				int calMonth = Integer.parseInt(ds2[1]);
				int calDay = Integer.parseInt(ds2[2]);
				int calHour = Integer.parseInt(ds3[0]);
				int calMinute = Integer.parseInt(ds3[1]);
				int calSecond = Integer.parseInt(seconds);
				dateAndTime.set(calYear,calMonth,calDay,calHour,calMinute,calSecond);
				long utime = dateAndTime.getTimeInMillis();
				
				//insert into email only if one of the two people are on the 151 LIST
				if(accts151.contains(rcpt) && accts151.contains(sender)){
					//insert into email only if its a new sender/rcpt combo or a new email mid
					if(!rcptList.contains(rcpt) || !lastMessageInput.equals(mid)){
						String insertQuery2 = "INSERT INTO email ("
						+ "uid, sender, sname, sdom, szn, rcpt, rname, rdom, "
						+ "rzn, numrcpt, mailref, dates, times, msghash, type, "
						+ "subject, folder, utime, ghour, gmonth, gyear) VALUES ("
						+ uid + ", "
						+ "\"" + sender + "\", "
						+ "\"" + sname + "\", "
						+ "\"" + sdom + "\", "
						+ "\"" + szn + "\", "
						+ "\"" + rcpt + "\", "
						+ "\"" + rname + "\", "
						+ "\"" + rdom + "\", "
						+ "\"" + rzn + "\", "
						+ 1 + ", "
						+ "\"" + message_id + "\", "
						+ "\"" + dates + "\", "
						+ "\"" + times + "\", "
						+ "\"" + mid + "\", "
						+ "\"text/plain\", "
						+ "\"" + subject + "\", "
						+ "\"" + folder + "\", "
						+ utime + ", "
						+ "\"" + ghour + "\", "
						+ "\"" + gmonth + "\", "
						+ "\"" + gyear + "\")";
						//System.out.println("mid: "+mid+", sender: "+sender+", rcpt: "+rcpt);
						try{
							targetDB.updateSQLData(insertQuery2); //actually insert the row into the email table 
						} catch(SQLException e) {
							e.printStackTrace();
						}
						uid++; //increment uid
						rcptList.add(rcpt); //add the rcpt to the list
					}
										
					//Insert into message if its a new mid
					if(!lastMessageInput.equals(mid)){
						String insertQuery1 = "INSERT INTO message "
							+ "(mailref, hash, type, body) VALUES ("
							+ "\"" + message_id + "\", "
							+ "\"" + mid + "\", "
							+ "\"text/plain\", "
							+ "\"" + body + "\")";
						try{
							targetDB.updateSQLData(insertQuery1);//actually insert into the message table
						} catch(SQLException e) {
							e.printStackTrace();
						}
						lastMessageInput = mid;
						rcptList = new ArrayList<String>();
						rcptList.add(rcpt); //re-add the first rcpt to the list
					}
				}							
				totalCount++;
			}//for
			System.out.println("Finished "+((ridInc-1)/10000)+" out of 325 iterations...");
		}//for i (+10000 each time, this should give us a total of about 40 loops)
			
		// -----------------------------------------------------------------------------		
			
		//shutdown the connection to the source database (jdbcview)
		System.out.println("Shutting down connection to source database...");
		try{
			sourceDB.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the source database was successful...");
		
		//shutdown the connection to the target database (jdbcview)
		System.out.println("Shutting down connection to target database...");
		try{
			sourceDB.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the target database was successful...");
	}
	
	
	/**
	 * For vanessa.
	 */
	private void forVanessa(){
		//define the source database connection parameters
		String srcHost = "127.0.0.1";
		String srcName = "enron151";
		String srcUsername = "root";
		String srcPassword = "";
		String srcType = "MYSQL";
		
		//define the target database connection parameters
		String targetHost = "127.0.0.1";
		String targetName = "enronBlank";
		String targetUsername = "root";
		String targetPassword = "";
		String targetType = "MYSQL";
		
		//create the database connection objects for both databases
		final EMTDatabaseConnection  sourceDB = DatabaseManager.LoadDB(srcHost, srcName, srcUsername, srcPassword,srcType);
		final EMTDatabaseConnection  targetDB = DatabaseManager.LoadDB(targetHost, targetName, targetUsername, targetPassword,targetType);
		
		//shutdown the connection to the main database (jdbcview)
		System.out.println("Shutting down connection to main database...");
		try{
			jdbcView.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the main database was successful...");
		
		//connect to the source database
		System.out.println("Connecting to the source database...");
		try{
			if(sourceDB.connect(true) == false){
				System.err.println("Couldn't connect to the source database, EMT will now exit...");
				System.exit(-1);
			}
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Connection to source database was successful...");
		
		//connect to the target database
		System.out.println("Connecting to the target database...");
		try{
			if(targetDB.connect(true) == false){
				System.err.println("Couldn't connect to the target database, EMT will now exit...");
				System.exit(-1);
			}
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Connection to target database was successful...");
		
		//---- map source data to target data ------------------------------------------
		String qE = "SELECT Email_id FROM employeelist";
		String[][] dataE, data1, data2;
		
		int bigSizes[] = {4,9,15,22,40,60,80,117,140};
		
		for(int bigCount=0;bigCount<9;bigCount++){
			int SIZE = bigSizes[bigCount];
			//int SIZE = Integer.parseInt((String)sizeChoice.getSelectedItem());
			String dir = "subset"+SIZE;
			int[] numSent = new int[SIZE];
			int[] numRcvd = new int[SIZE];
			
	        FileOutputStream fileOut; // declare a file output object
	        PrintStream p; // declare a print stream object
			
			ArrayList<String> allAccts = new ArrayList<String>();
			ArrayList<String> accts151 = new ArrayList<String>();
			allAccts = getAccountList();
			System.out.println("allAccts Size: "+allAccts.size());
			System.out.println("size of SIZE: "+SIZE);
			for(int i=0;i<SIZE;i++){
				accts151.add(i,allAccts.get(i));
			}
			
			int ridInc;
					
			/*
	    	//==== CODE TO PAUSE EXECUTION ====//
	    	try {
	    		System.in.read();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    	//==== ======================= ====//
	    	 */
						
			//mid goes up to 3242063
			String lastMessageInput = "";
			ArrayList<String> rcptList = new ArrayList<String>();
						
			for(ridInc=1;ridInc<3250000;ridInc+=10000){
				String q1 = "SELECT m.mid, m.sender, r.rvalue, m.date, m.message_id, " +
						"m.subject, m.body, m.folder FROM message AS m INNER JOIN " +
						"recipientinfo AS r ON m.mid = r.mid WHERE r.rid BETWEEN " + 
						ridInc + " AND " + (ridInc+9999);
				data1 = new String[0][0];
				try{
					data1 = sourceDB.getSQLData(q1);
				} catch(SQLException e) {
					e.printStackTrace();
				}
				
				String mid, sender, rcpt, datetime, subject, body;
				mid = null;
				sender = null;
				rcpt = null;
				datetime = null;
				subject = null;
				body = null;
				
				for(String[] row: data1){									
					//insert into email only if BOTH of the accounts are on the current list
					if(accts151.contains(row[1]) && accts151.contains(row[2])){
						if(row[0].equals(mid)){
							if(!rcptList.contains(row[2])){
								rcptList.add(row[2]);
							}
						} else {
							if(mid!=null){
								String ds[] = datetime.split(" ");
								String date = "Date: " + ds[0];
								String time = "Time: " + ds[1];
								String toLine = "To:";
								int numRcpts = rcptList.size();
								for(int rcptCount=0;rcptCount < (numRcpts - 1);rcptCount++){
 									String username = "user"+accts151.indexOf(rcptList.get(rcptCount))+".enron.com";
									toLine += username + ",";
								}
								String username = "user"+accts151.indexOf(rcptList.get(numRcpts - 1))+".enron.com";
								toLine += username;
								String contentTitle = "Content:";
								String endDelimiter = ":*:end:barter:";
								
								try {
			                        int senderIndex = accts151.indexOf(sender);
			                        fileOut = new FileOutputStream("C:/vanessa/"+dir+"/user"+senderIndex+".txt",true);
			                        p = new PrintStream(fileOut);
			                        p.println(toLine);
			                        p.println(date);
			                        p.println(time);
			                        p.println(contentTitle);
			                        p.println(subject);
			                        p.println(body);
			                        p.println(endDelimiter);		                        
			                        p.close();
				                } catch (Exception e) {
			                        System.err.println ("Error writing to file");
				                }	
				                
								numSent[accts151.indexOf(sender)]++; //increment the number sent by this account
								numRcvd[accts151.indexOf(rcpt)]++; //increment the number rcvd by this account
							}
									
							mid = row[0];
							sender = row[1];
							rcpt = row[2];
							datetime = row[3].split("\\.")[0];						
							rcptList = new ArrayList<String>(); //create a new (empty) rcptList
							rcptList.add(rcpt); //initialize the new rcptList
							subject = row[5].replaceAll("\"","'");
							body = row[6].replaceAll("\"","'");
						}
					}
				}//for
				System.out.println("Finished "+((ridInc-1)/10000)+" out of 325 iterations...");
			}//for i (+10000 each time, this should give us a total of about 40 loops)
			
			//** WRITING TO THE REF FILE **//
	        try {
	            fileOut = new FileOutputStream("C:/vanessa/"+dir+"/ref.txt");
	            p = new PrintStream(fileOut);
	            for(int i=0;i<SIZE;i++){
	                p.println(accts151.get(i) + " - " + "user" + i + ".enron.com" + " - " + numSent[i] + " - " + numRcvd[i]);
	            }
	            p.close();
	        } catch (Exception e) {
	            System.err.println ("Error writing to file");
	        }
		}
		// -----------------------------------------------------------------------------		
			
		//shutdown the connection to the source database (jdbcview)
		System.out.println("Shutting down connection to source database...");
		try{
			sourceDB.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the source database was successful...");
		
		//shutdown the connection to the target database (jdbcview)
		System.out.println("Shutting down connection to target database...");
		try{
			sourceDB.shutdown();
		} catch(DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(-2);
		}
		System.out.println("Shutdown of the connection to the target database was successful...");
	}
	
}
//////////////////////////////////////

