/* 
 * copyright Columbia University 2002-2005
 * 
 * Major overhaul to split data views shlomo fall 02
 * 
 * FEATURE ADDITION SPR 03 - SH
 * 
 * 2.1.1 summer 2003 - drop in everything from trainig and testing window into this. (sh)
 * 
 * FALL 2003 - 3.0 added roc curve and time experiments.
 * 
 * 
 * 
 */

/**
 *KNOWN BUG (2002): messd up version of adobe 4 writer, when printing to pdf doc...will loose stop list unless have full path to stop list in the setup file
 */

package metdemo.Tables;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.Toolkit;


import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.CorModel;
import metdemo.MachineLearning.MLearner;
import metdemo.MachineLearning.NBayesClassifier;
// import metdemo.MachineLearning.NGram;
import metdemo.MachineLearning.CodedModel_1;
import metdemo.MachineLearning.LinkAnalysisLearner;
import metdemo.MachineLearning.ModelBuilder;
import metdemo.MachineLearning.NGramLimited;
import metdemo.MachineLearning.NgramUtilities;
import metdemo.MachineLearning.OutlookModel;
import metdemo.MachineLearning.PGram;
import metdemo.MachineLearning.SVMLearner;
import metdemo.MachineLearning.TextClassifier;
import metdemo.MachineLearning.Tfidf;
import metdemo.MachineLearning.VIPLearner;
import metdemo.MachineLearning.machineLearningException;
import metdemo.MachineLearning.modelHelper;
import metdemo.MachineLearning.resultScores;
import metdemo.MachineLearning.NGram;
import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.EMTLimitedComboSpaceCountingRenderer;
import metdemo.Tools.KeywordPanel;
import metdemo.Tools.KeywordWizard;
import metdemo.Tools.LinkExtractor;
import metdemo.Tools.Utils;
import metdemo.Tools.popupCalendar;
import metdemo.Window.EmailInternalConfigurationWindow;
import metdemo.dataStructures.CharFreq;
import metdemo.dataStructures.LinkInfoDS;
//import metdemo.dataStructures.RoundBorder;
import metdemo.dataStructures.ThinDoubleArrayWrapper;
import metdemo.dataStructures.contentDis;

import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import chapman.graphics.JPlot2D;

/**
 * <P>
 * Main Window to view the EMT Data
 * </P>
 * <P>
 * The Window allows the user among other things to:
 * <ul>
 * <li>To create multiple views of the underlying data by changing drop down
 * lists</li>
 * <LI>To Label the data pulled up in the window</li>
 * <LI>To create and evaluate models without leaving the window</li>
 * <LI>Save changes to the Database</li>
 * <LI>Use the output of the {@link UserHistogram}window and get groups of
 * users</li>
 * <LI>TO DO: Allow user to order lunch from inside window</li>
 * </UL>
 * </P>
 * 
 * @author Shlomo Hershkop(most), Brian Lindaur (old base implimentation with no
 *         view choices)
 */

public class MessageWindow extends JScrollPane implements MouseListener, ItemListener {

    /** Names of column */
    final private String[] COLUMN_NAMES = new String[] { "Mailref", "Sender", "Recipient", "Subject", "CC", "A",
            "Size", "G", "Date", "Time", "Label", "Score", "FP", "utime" };

    /** Connection to underlying Database */
    private EMTDatabaseConnection m_jdbcView;
    private MessageTableModel m_tableModel;
    private JTable m_messageTable;

    /** Allow privatize information by md5-ing the information */
    // private md5CellRenderer m_md5Renderer;
    private JButton m_butSaveChanges; // needs global since will be changing
    private JPopupMenu m_popupMenu;
    private boolean force_refresh = false;

    /** the Vector of the data changed since last db update */
    private Vector<String> SelectedData = new Vector<String>();

    /** changes the goal of the window by specific views of the data */
    private JComboBox m_view;

    /** Select User */
    private JComboBox m_view_user;

    // private ArrayList m_userlist;
    /** Select In/OUT/Both bound data */
    private JComboBox m_view_direction;

    /** Date by bounds or everything */
    private JCheckBox m_view_date;

    // JComboBox m_view_date;
    /** Groups of users */
    private JComboBox m_view_similiar;

    // private Vector m_view_similiar_vector;

    /** By folders */
    // private JComboBox m_view_folder;
    /** Control start and end dates */
    // 04private JSpinner spinnerStart, spinnerEnd;
    /** Needs to do something still */
    // private JSpinner spinnerThreshold;
    // private SpinnerNumberModel m_spinnerNumber;
    private JSlider m_groupThreshold;

    // private String m_sql;
    private String m_sql_exp;

    winGui m_wg;

    /** Window on building the model */
    //ModelBuilder m_mb = null;// new ModelBuilder(this,0); //windows showing
                                // how

    // to build model
    /** Window on eval model */
    //ModelBuilder m_me = null;// new ModelBuilder(this,1);

    private boolean update_all_labels;

    /** whether we are in graphical or table mode */
    private boolean m_graphic_mode;

    private JButton m_tableMode, m_graphicMode;

    /** To do grouping */
    // private NGram m_ng = new NGram();
    /** which value old group by done */
    private int m_groupbyNum;

    private int m_groupType;

    private JComboBox m_groupBy;

    private JScrollPane messageTableScroll;// will allow remving table contents from

    // view only
    // private JPlot2D m_graphicPlot;
    private GraphicPlot m_graphicsPlot;

    
    /** table split plane for viewing the table and preview */
    private JSplitPane messageViewSplitPane;
    
    private JSplitPane mainMessageSplitPane;    
    
    // ned for sizing
    // private int max_x;

    private String selected_user = null;// thisi s the current selected user.

    private int m_max_groups = 0; // for printingout the number of groups

    // quickly
    private Stack<String> m_backStack;

    private Stack<String> m_forwardStack;

    private JButton back_but, for_but;

    // private Icon m_help_icon;
    private boolean isLower, isStop;

    /* know whihc model */
    private int model_num;

    private GregorianCalendar globalCalendarStart = new GregorianCalendar();

    private GregorianCalendar globalCalendarEnd = new GregorianCalendar();

    static popupCalendar popCStart, popCEnd;

    static final int i_mailrefColumn = 0;

    static final int i_senderColumn = 1;

    static final int i_rcptColumn = 2;

    static final int i_subjectColumn = 3;

    static final int i_ccColumn = 4;

    static final int i_attachmentColumn = 5;

    static final int i_sizeColumn = 6;

    static final int i_groupColumn = 7;

    static final int i_dateColumn = 8;

    static final int i_timeColumn = 9;

    static final int i_labelColumn = 10;

    static final int i_scoreColumn = 11;

    static final int i_falsepositiveColumn = 12;

    static final int i_utimeColumn = 13;

    static final int GROUP_NOTHING = 0;

    static final int GROUP_SUBJECT = 1;

    static final int GROUP_CONTENT = 2;
    
    static final int GROUP_CONTENT_LESS = 3;
    

    static final int GROUP_KEYWORD = 4;

    static final int GROUP_ZSTRING = 5;

    static final int GROUP_CONFIG = 6;

    static final int TABLEWIDTH = 5;

    static final int TABLEHEIGHT = 3;

    static final int TABLEWEIGHTX = 1;

    static final int TABLEWIGHTY = 9;

    static final int TABLE_X = 2;

    static final int TABLE_Y = 3;

    private EMTConfiguration emtConfigHandle;
    private JPlot2D userPlot;
    private Hashtable<String,ThinDoubleArrayWrapper> userPlotHash;
    private JRadioButton noPlot, senderPlot, rcptPlot;
    public static final double lambda = 9;

    private final int plotxsize = 310;// 380

    private final int plotysize = 220;// 250
    private double m_threshold = 0;
    private String TARGET_CLASS = new String(MLearner.SPAM_LABEL);
    private JTextArea m_msgPreviewText;
    private JScrollPane msgPreviewAreaScroll;
    private String m_msg_displayed_mailref;
    private String m_msg_displayed_user;
   // private boolean userChanged = false;
    private boolean folderChanged = false;
    private MLearner m_mlearner;
    private boolean m_timespam = false; // to run time experiments
    // to show threhold affect
    private double d_thresholds[][] = new double[100][5];
    private JFrame t_chooser;
    private JSlider js_threshold = new JSlider();
    private long before;
    private JButton labStart, labEnd;
    private JPopupMenu groupPopup;
    private JPanel datePanel;
    private final int MIN_NUMBER_MSGS_TO_START = 500;
    private final int MAX_NUMBER_MSGS_TO_START = 8000;
    private JTree folderListTree; // navigation list of avaiable folders
    private DefaultMutableTreeNode roottree = new DefaultMutableTreeNode("Folders");

    private JRadioButton choose_specificFolders, choose_allFolders;

    private String m_view_current_folder = new String("");
private  JPanel mainPanell = new JPanel();
GridBagLayout gridbag = new GridBagLayout();
GridBagConstraints constraints = new GridBagConstraints();

//searching stuff
private JComboBox searchconstr;
private JButton cancelSearchButton;
private String SearchString = new String();
private Thread searchHandle; //to start and stop the thread of search when retyping
private boolean startingsearch;
private JTextField searchArea;
private Vector DataofTableWhileSearching;




    /**
     * Main constructor for the message window
     * 
     * @param emtc
     *            handle to EMTConfiguration file
     * @param jdbcV
     *            hnadle to database
     * @param md5Renderer
     *            handle to renderer
     * @param wg
     *            handle to winGui object for passing information
     * @throws NullPointerException
     *             on error
     */
    public MessageWindow(final EMTConfiguration emtc, final EMTDatabaseConnection jdbcV,
            final md5CellRenderer md5Renderer, winGui wg) throws NullPointerException {

        if (jdbcV == null) {
            throw new NullPointerException("Problem in message window with null db handle(at setup)");
        }
        
        System.out.println("starting message view");
        // db handle
        m_jdbcView = jdbcV;
        // wingui handle
        m_wg = wg;
        // set configu handle
        emtConfigHandle = emtc;

        // variables section
        String max_available_date;
        int yr, mnth, dy;
        DefaultTableCellRenderer m_emtTableCellRenderer = new AlternateColorTableRowsRenderer();
        long startTime = System.currentTimeMillis();
        int amountMsgs = 0;

        // set pictures
        ImageIcon icon = Utils.createImageIcon("metdemo/images/cal1.gif", "choose date");

        labStart = new JButton("");
        //???labStart.setBorder(new RoundBorder());
        labEnd = new JButton("");
//      ???labEnd.setBorder(new RoundBorder());

        // no md5 render //m_md5Renderer = md5Renderer;

        update_all_labels = false; // flags
        m_graphic_mode = false; // flags
        m_sql_exp = null;
        // setup gui now
       
       
        mainPanell.setLayout(gridbag);

        // will print out time to setup
        // get the max date from the wingui class here

        if (m_wg.getCount() > 0) {
            max_available_date = m_wg.getMaxDate();
        } else {
            System.out.println("Setting current time as top date");
            max_available_date = new java.sql.Date(System.currentTimeMillis()).toString();
        }// need to strip out the date

        yr = Integer.parseInt(max_available_date.substring(0, 4));
        mnth = Integer.parseInt(max_available_date.substring(5, 7));
        dy = Integer.parseInt(max_available_date.substring(8, 10));

        // set the global cal to the max dates
        globalCalendarStart.set(yr, mnth - 1, dy);
        globalCalendarEnd.set(yr, mnth - 1, dy);

        if (m_wg.getCount() < MIN_NUMBER_MSGS_TO_START) {
            System.out.println("Less than " + MIN_NUMBER_MSGS_TO_START + " emails in db");
            // so easily set the start date to min, end date to max and we
            // are done
            // if more than one we can do date else leave the same
            if (m_wg.getCount() > 0) {
                max_available_date = m_wg.getMinDate();
                yr = Integer.parseInt(max_available_date.substring(0, 4));
                mnth = Integer.parseInt(max_available_date.substring(5, 7));
                dy = Integer.parseInt(max_available_date.substring(8, 10));
            }

            globalCalendarStart.set(yr, mnth - 1, dy);

        } else {
            try {
                //
                // set up the end date which is max day +1
                // cal.add(Calendar.DAY_OF_MONTH, -4);//next day
                // cal.add(Calendar.MONTH, -1);//was -2
                // now to see the amount of emails.
                // System.out.println("cal get time: " + cal.getTime());

                // first try to load values from save file to save time
                String tdate = emtc.getProperty(EMTConfiguration.DBDATEINFO);

                if (tdate != null) {
                    amountMsgs = m_jdbcView.getCountByDate(tdate);

                }

                // check if too many msgs
                if (amountMsgs > MAX_NUMBER_MSGS_TO_START) {
                    System.out.println("Too many msgs, will try to grab less");
                    amountMsgs = 0;

                }

                if (amountMsgs > 0 && amountMsgs >= MIN_NUMBER_MSGS_TO_START) {

                    globalCalendarStart.setTime(Utils.deSqlizeDateYEAR(tdate));
                    System.out.println("setting span...saving time");

                } else {

                    // we need to calculate the time span

                    String grabDate = Utils.mySqlizeDate2YEAR(globalCalendarStart.getTime());

                    synchronized (m_jdbcView) {
                        try {
                            System.out.println("Going to find date to span the data");
                            while (amountMsgs < MIN_NUMBER_MSGS_TO_START) {

                                globalCalendarStart.add(Calendar.DAY_OF_MONTH, -4);

                                // translate the date
                                grabDate = Utils.mySqlizeDate2YEAR(globalCalendarStart.getTime());
                                // do a count
                                amountMsgs = m_jdbcView.getCountByDate(grabDate);
                                if (amountMsgs < 0)
                                    break;
                            }
                            // need to remeber
                            emtc.putProperty(EMTConfiguration.DBDATEINFO, grabDate);
                            emtc.saveFileToDisk(emtc.getProperty(EMTConfiguration.CONFIGNAME));

                        } catch (Exception s2) {
                        }

                    }
                }
            } catch (NumberFormatException nfe) {
                // this means no records present
            }

        }
        System.out.println("Done date setup in Message window: " + ((System.currentTimeMillis() - startTime) / 1000.));

        // add pop up claendar here for later use
        popCStart = new popupCalendar(labStart, globalCalendarStart, globalCalendarStart.get(Calendar.DAY_OF_MONTH),
                "Starting Date");

        popCEnd = new popupCalendar(labEnd, globalCalendarEnd, globalCalendarEnd.get(Calendar.DAY_OF_MONTH),
                "Ending Date");
        labStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                popCStart.setVisible(true);
            }
        });
        labEnd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                popCEnd.setVisible(true);
            }
        });

        // "Dates"
        datePanel = new JPanel();
//      ???datePanel.setBorder(new RoundBorder());
        m_view_date = new JCheckBox("", icon, true);
       
        
        
        m_view_date.setToolTipText("This will help choose either all data  or some of it based on constraint range");
        m_view_date.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBox source = (JCheckBox) evt.getSource();
                boolean c = source.isSelected();
                if (c) {
                   // m_view_date.setBackground(Color.GREEN);
                    labStart.setBackground(Color.GREEN);
                    labEnd.setBackground(Color.GREEN);
                  //  datePanel.setBackground(Color.GREEN);
                    labStart.setEnabled(true);
                    labEnd.setEnabled(true);
                } else {
                    //m_view_date.setBackground(Color.lightGray);
                    labStart.setBackground(Color.lightGray);
                    labEnd.setBackground(Color.lightGray);
                   // datePanel.setBackground(Color.lightGray);
                    labStart.setEnabled(false);
                    labEnd.setEnabled(false);

                }
            }
        });
        
        //set certain colors on the gui, easier to read
       // m_view_date.setBackground(Color.GREEN);
        labStart.setBackground(Color.GREEN);
        labEnd.setBackground(Color.GREEN);
        //datePanel.setBackground(Color.GREEN);
        datePanel.add(m_view_date);
        datePanel.add(labStart);
        datePanel.add(labEnd);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        // constraints.weightx = 1;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        gridbag.setConstraints(datePanel, constraints);
        mainPanell.add(datePanel);

        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;// 0
        constraints.gridy = 0;// 0
        constraints.gridwidth = 1;
        EMTHelp j = new EMTHelp(EMTHelp.MESSAGE);
        gridbag.setConstraints(j, constraints);
        mainPanell.add(j);

        JButton countinginfo = new JButton("General", Utils.createImageIcon("metdemo/images/magglass.gif", "More info"));
        countinginfo.setToolTipText("Project how much data will be fetched, and count current window");
//      ???countinginfo.setBorder(new RoundBorder());
        countinginfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    Refresh(false, true, true, null);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MessageWindow.this, "oops" + e, "Error", JOptionPane.ERROR_MESSAGE);
                    // System.out.println("oops " +e);
                }
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 6;
        constraints.gridy = 1;
        gridbag.setConstraints(countinginfo, constraints);
        mainPanell.add(countinginfo);
        // adding modeling
        icon = Utils.createImageIcon("metdemo/images/modelbuild.gif", "Build a Learning Model");

        JButton modeling = new JButton("Create", icon);
//      ???modeling.setBorder(new RoundBorder());
        modeling.setToolTipText("Build model based on current window");
        modeling.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (update_all_labels == true) {

                        int n = JOptionPane.showConfirmDialog(MessageWindow.this,
                                "Would you like to save changes made to the labels?", "Confirm changes",
                                JOptionPane.YES_NO_OPTION);

                        if (n == JOptionPane.YES_OPTION)
                            saveLabelChanges();

                        // need to refresh
                        force_refresh = true;

                        // TODO: see if this is needed
                        Refresh(true, true, false, null);

                    }
                    buildModel();// -m_sql_exp);
                    // Refresh(false, true, true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MessageWindow.this, "Exception caught:" + e);
                    // System.out.println("oops " +e);
                }
            }
        });

        constraints.gridwidth = 1;
        constraints.gridx = 4;
        constraints.gridy = 1;

        gridbag.setConstraints(modeling, constraints);
        mainPanell.add(modeling);
        icon = Utils.createImageIcon("metdemo/images/modelcheck.gif", "Use a Learning Model");
        JButton umodeling = new JButton("Evaluate", icon);
//      ???umodeling.setBorder(new RoundBorder());
        umodeling.setToolTipText("Evaluate model on data in current window");
        umodeling.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (update_all_labels == true) {
                        int n = JOptionPane.showConfirmDialog(MessageWindow.this,
                                "Would you like to save changes made to the labels?", "Confirm changes",
                                JOptionPane.YES_NO_OPTION);
                        if (n == JOptionPane.YES_OPTION)
                            saveLabelChanges();
                        else {
                            // need to refresh
                            force_refresh = true;
                            Refresh(true, true, false, null);

                        }

                    }
                    evalModel();// m_sql_exp);
                    // Refresh(false, true, true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MessageWindow.this, "oops" + e);
                    // System.out.println("oops " +e);
                }
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 5;
        constraints.gridy = 1;

        gridbag.setConstraints(umodeling, constraints);
        mainPanell.add(umodeling);

        // end modeling
        icon = Utils.createImageIcon("metdemo/images/label.gif", "Saved changed labels");
        m_butSaveChanges = new JButton("Save Labels", icon);
        m_butSaveChanges.setEnabled(false);
//      ???m_butSaveChanges.setBorder(new RoundBorder());
        m_butSaveChanges.setToolTipText("Click to Save/Commit class changes to the Database");
        m_butSaveChanges.setMnemonic(KeyEvent.VK_U);
        m_butSaveChanges.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveLabelChanges();
            }
        });

        constraints.gridx = 5;
        constraints.gridy = 0;
        gridbag.setConstraints(m_butSaveChanges, constraints);
        mainPanell.add(m_butSaveChanges);

        // warning this is hard coded later..to lazy now
        m_view = new JComboBox();

        m_view.setBackground(Color.green);
        m_view.addItem("Unique SEND->RCPT");
        m_view.addItem("By Mailref");
        m_view.addItem("Virus");
        m_view.addItem("Spam");
        m_view.addItem("Interesting");
        m_view.addItem("Unknown");
        m_view.addItem("Not Interesting");
        m_view.addItem("By Similiar Grps");
        m_view.setSelectedIndex(1); // set it to mailref
        m_view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JComboBox source = (JComboBox) evt.getSource();
                int c = source.getSelectedIndex();
                if (c == 7) {
                    m_view_similiar.setEnabled(true);
                } else {
                    m_view_similiar.setEnabled(false);
                }
            }
        });
        // set upt he folder tree

        folderListTree = new JTree(roottree);
        // end tree
        folderListTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        folderListTree.setScrollsOnExpand(true);
        folderListTree.setExpandsSelectedPaths(true);
        folderListTree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeExpanded(TreeExpansionEvent e) {
                // System.out.println("path = " + e.getPath());
            }

            // Required by TreeExpansionListener interface.
            public void treeCollapsed(TreeExpansionEvent e) {
                // TODO:System.out.println("col-path = " + e.getPath());
            }
        });
        folderListTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderListTree.getLastSelectedPathComponent();
                folderListTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                // Object nodeInfo = node.getUserObject();
                // check if there are any siblings.
                else if (!node.isLeaf()) {
                    // System.out.println("non leaf");
                    return;
                }
                TreeNode[] pts = node.getPath();
                m_view_current_folder = new String();
                // skip 0 cause of the root label
                for (int h = 1; h < pts.length; h++) {

                    m_view_current_folder += pts[h].toString();

                }

                if (pts.length > 0 && m_view_current_folder.lastIndexOf(" (") > 0) {
                    m_view_current_folder = m_view_current_folder.substring(0, m_view_current_folder.lastIndexOf(" ("));
                }

                // m_view_current_folder = node.getParent().toString()
                // + node.toString();

                // need to up date the file info

                // TODO:
                // System.out.println("setting:" + m_view_current_folder);

            }
        });
        DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer() {
            /** Color to use for the background when selected. */
            protected final Color SelectedBackgroundColor = Color.yellow;

            /** Whether or not the item that was last configured is selected. */
            protected boolean selected;

            /**
             * This is messaged from JTree whenever it needs to get the size of
             * the component or it wants to draw it. This attempts to set the
             * font based on value, which will be a TreeNode.
             */
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                if (hasFocus)
                    setForeground(Color.RED);

                /* Update the selected flag for the next paint. */
                this.selected = selected;

                if (selected && leaf) {
                    // System.out.println("selected:" + value);
                }

                return this;
            }

            /**
             * paint is subclassed to draw the background correctly. JLabel
             * currently does not allow backgrounds other than white, and it
             * will also fill behind the icon. Something that isn't desirable.
             */
            public void paint(Graphics g) {
                Color bColor;
                Icon currentI = getIcon();

                if (selected)
                    bColor = SelectedBackgroundColor;
                else if (getParent() != null)
                    /*
                     * Pick background color up from parent (which will come
                     * from the JTree we're contained in).
                     */
                    bColor = getParent().getBackground();
                else
                    bColor = getBackground();
                g.setColor(bColor);
                if (currentI != null && getText() != null) {
                    int offset = (currentI.getIconWidth() + getIconTextGap());

                    if (getComponentOrientation().isLeftToRight()) {
                        g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
                    } else {
                        g.fillRect(0, 0, getWidth() - 1 - offset, getHeight() - 1);
                    }
                } else
                    g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }

        };

        folderListTree.setCellRenderer(dtcr);

        m_view.setToolTipText("Choose current label of data to fetch. By mailref ignores label");
        // setup the view type of data
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        gridbag.setConstraints(m_view, constraints);
        mainPanell.add(m_view);

        // this will allow to view by specific user;
        m_view_user = new JComboBox();
        m_view_user.addItem("All Users");
        m_view_user.addItem("Choose From Above");
        // m_view_user.setRenderer(new EMTLimitedComboSpaceRenderer(30));
        // m_view_folder = new JComboBox();
        m_view_direction = new JComboBox(new String[] { "In or Out Bound", "OutBound Only", "InBound Only" });
        m_view_direction.setToolTipText("Choose between specific user as rcpt,sender, or both");
        m_view_user.setToolTipText("Choose specific users records from the top toolbox");

        m_view_user.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JComboBox source = (JComboBox) evt.getSource();
                int c = source.getSelectedIndex();
                if (c == 0) {// if all users, then it doesn tyet make sense
                    // to view only inbound
                    m_view_direction.setSelectedIndex(0);
                    m_view_direction.setEnabled(false);
                    m_wg.disableEmailList();
                } else {
                    m_wg.enableEmailList();

                    m_view_direction.setEnabled(true);
                    // m_view_direction.setSelectedIndex(0);
                }
            }
        });
        // need to call a method to update this one.
        // addUsers();
        userPlotHash = new Hashtable<String,ThinDoubleArrayWrapper>();
        // m_view_folder.setToolTipText("Choose the imported mailbox name");
        // m_view_folder.setRenderer(new EMTLimitedComboSpaceRenderer(30));
        // need to call a method to update this one.
        addFolders();
        // setup the view type of data
        constraints.gridx = 0;// 2
        constraints.gridy = 1;// 1
        // constraints.weightx = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(m_view_user, constraints);
        mainPanell.add(m_view_user);
        // constraints.gridx = 0;//4
        // constraints.gridy = 1;//2
        // constraints.gridwidth = 2;
        // gridbag.setConstraints(m_view_folder, constraints);
        // panel.add(m_view_folder);
        // m_view_folder.setEnabled(false);
        // setup the view type of data
        constraints.gridx = 1;// 3;
        constraints.gridy = 1;// 2;
        constraints.gridwidth = 2;
        gridbag.setConstraints(m_view_direction, constraints);
        mainPanell.add(m_view_direction);
        constraints.gridwidth = 1;// constraints.weightx = 1;
        icon = Utils.createImageIcon("metdemo/images/reload.gif", "Reload the page with current settings");
        JButton butRefresh = new JButton("Refresh", icon);
        butRefresh
                .setToolTipText("<html>This will attempt to reload the table<p>Based on the constrints chosen.<p>.<b>Note:</b> If nothing has been changed since last refresh, nothing will happen<P></html>");
        // Border br1 = new RoundBorder() ;
        // Border br2 = BorderFactory.createBevelBorder(BevelBorder.RAISED );
        // Border br3 = BorderFactory.createCompoundBorder ( br2, br1 ) ;

//      ??? butRefresh.setBorder(new RoundBorder());// SoftBevelBorder(BevelBorder.RAISED));
        butRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                	startingsearch = true;
               	   cancelSearchButton.setEnabled(false);
                    searchArea.setText("");
                    selected_user = null;
                    before = System.currentTimeMillis();
                    Refresh(true, true, false, null);
                    long after = System.currentTimeMillis();
                    System.out.println("refresh took ..." + ((after - before) / 1000.0) + " sec");

                } catch (SQLException sqlex) {
                    JOptionPane.showMessageDialog(MessageWindow.this, "Refresh failed: " + sqlex, "Exception",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        constraints.gridx = 6;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(butRefresh, constraints);
        mainPanell.add(butRefresh);

        // adding the group by similar button
        m_groupbyNum = -1;
        m_groupType = 0;
        // grouping button
        m_groupBy = new JComboBox(new String[] { "Group Method", "Group Subject", "Group Content", "Content Quick","Group Keyword",
                "Freq Edit Distance", "CONFIGURE SETTINGS" });
        groupPopup = new JPopupMenu("Group settings");

        m_groupBy.setBackground(Color.red);
        m_groupBy.setSelectedIndex(0);
        // b_groupsim.setFont(new Font("Serif", Font.BOLD, 12));
        m_groupBy.setToolTipText("Group the emails based on some parameter");
        // $$$$$$$$
        m_groupBy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (m_groupBy.getSelectedIndex() == GROUP_CONFIG) {
                    groupPopup.show(m_groupBy, 10, 10);
                    m_groupBy.setSelectedIndex(0);
                    m_groupBy.setBackground(Color.red);
                } else if (m_groupBy.getSelectedIndex() != 0)
                    (new Thread(new Runnable() {
                        public void run() {
                            groupBySimilarity();
                        }
                    })).start();
            }
        });

        constraints.gridwidth = 1;
        constraints.gridx = 6;// 4;
        constraints.gridy = 2;// 0;
        gridbag.setConstraints(m_groupBy, constraints);
        mainPanell.add(m_groupBy);

        m_groupThreshold = new JSlider(0, 200, 100);
        m_groupThreshold.setMajorTickSpacing(20);
        m_groupThreshold.setSnapToTicks(true);
        m_groupThreshold.setPaintTicks(true);

        Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
        labelTable.put(new Integer(200), new JLabel("More"));
        labelTable.put(new Integer(0), new JLabel("Less"));
        // labelTable.put( new Integer( 100 ), new JLabel("Groups") );
        m_groupThreshold.setLabelTable(labelTable);
        m_groupThreshold.setPaintLabels(true);
        m_groupThreshold.setToolTipText("Threshold to use when grouping");

        groupPopup.add(m_groupThreshold);
        groupPopup.add("      Set").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // groupPopup.hide();
            }
        });
        
        JPanel searchTablePanel = new JPanel();
        
        searchTablePanel.setBackground(Color.yellow);//just to see size
        //lets add some stuff to this
        searchArea = new JTextField(10);
//        JPanel subsearchAreaHolder = new JPanel();//will need extra so doesn't resize
   //     subsearchAreaHolder.add(searchArea);
        
        searchTablePanel.setLayout(new BorderLayout());
        //add a drop down list of things
        
        searchconstr = new JComboBox();
        searchconstr.addItem("Users");//Utils.createImageIcon("metdemo/images/mailplain.png", "Mail"));
        searchconstr.addItem("From");//Utils.createImageIcon("metdemo/images/mailfrom.png","Sender"));
        searchconstr.addItem("To");
        searchconstr.addItem("Subject");
        // searchconstr.addItem("Rcpt");
        //did we start a search 
        startingsearch = true;
        
        
        //cancel box
        cancelSearchButton = new JButton("Stop");//Utils.createImageIcon("metdemo/images/cancelredX.png","Cancel"));
        cancelSearchButton.setEnabled(false);
        cancelSearchButton.setToolTipText("Will cancel current serch against table");
        
        cancelSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            //need to stop the search thread
            	if(searchHandle!=null){
            		searchHandle.interrupt();
            		
            	}
            	//need to turn off cancel button
            	cancelSearchButton.setEnabled(false);
            	cancelSearchButton.repaint();
            	//need to clear search area
            	searchArea.setText("");
            	//lets restore the table view
            	clear();
            	m_tableModel.setSortingOn(false);
            	m_tableModel.setData(DataofTableWhileSearching);
            	m_tableModel.setSortingOn(true);
            	startingsearch = true;
            
            }
        });
        
        
        
        //now to set the search action when typing
       searchArea.addCaretListener(
        	    new CaretListener() {
        	       	public void caretUpdate(CaretEvent arg0) {
        	       		if(searchArea.getText().length()>1)
        	       			startSearchInput(searchArea.getText());
					
					}
        	    }
        );
        //this is for entering enter
        searchArea.addActionListener(
        	    new ActionListener() {
        	        public void actionPerformed(ActionEvent e) {
        	            //typed in enter
        	     
        	        	startSearchInput(searchArea.getText());
        	        	
        	        }
        	    });
        
        
        
        
        
        
        
        
        searchTablePanel.add(searchconstr,BorderLayout.WEST);
        searchTablePanel.add(searchArea,BorderLayout.CENTER);
        searchTablePanel.add(cancelSearchButton,BorderLayout.EAST);
       
        //now to add a title
		Border bd = BorderFactory.createEtchedBorder();//Color.white, new Color(134, 134, 134));
		TitledBorder ttl = new TitledBorder(bd, "Quick Search");
		searchTablePanel.setBorder(ttl);

        
        
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;

        gridbag.setConstraints(searchTablePanel, constraints);
        mainPanell.add(searchTablePanel);
        
        

        constraints.gridwidth = 1;
        m_view_similiar = new JComboBox();
        m_view_similiar
                .setToolTipText("To choose which similiar group to choose, needs to be computed in Similar Window");
        m_view_similiar.setRenderer(new EMTLimitedComboSpaceCountingRenderer(80));
        m_view_similiar.addItem("Similiar Groups");
        m_view_similiar.setEnabled(false);
        // constraints.weightx = 0;
        // setup the view type of data
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = 4;

        gridbag.setConstraints(m_view_similiar, constraints);
        mainPanell.add(m_view_similiar);

        // priority reordering
        JButton priorityButton = new JButton("Prioritize");
        priorityButton.setEnabled(false);
        priorityButton.setToolTipText("Reorder the emails based on priority scheme...might take time first time only");
        priorityButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                System.out.println("will do priority now");
                m_wg.setEnabled(false);
                long starttime = System.currentTimeMillis();
                String vipmodel_name = emtc.getProperty(EMTConfiguration.DBNAME) + "_VIPMODEL";

                // lets try to load the model
                MLearner VIPmodel = modelHelper.goLoadModel(vipmodel_name);

                if (VIPmodel == null) {
                    System.out.println("going to train vip model:");
                    String data[] = null;
                    // lets try to get the email data
                    synchronized (m_jdbcView) {
                        try {
                            data = m_jdbcView
                                    .getSQLDataByColumn("Select distinct mailref from email order by utime", 1);
                        } catch (SQLException se) {
                            se.printStackTrace();
                        }

                    }

                    if (data != null) {
                        String currentLabel = new String();
                        VIPmodel = new VIPLearner();
                        try {
                            PreparedStatement ps_subdata = m_jdbcView
                                    .prepareStatementHelper("select sender,rcpt,utime,class from email where mailref = ?");
                            // now to train the model
                            for (int i = 0; i < data.length; i++) {
                                try {
                                    EMTEmailMessage current_msg = new EMTEmailMessage(data[i]);
                                    ps_subdata.setString(1, data[i]);
                                    ResultSet rs = ps_subdata.executeQuery();
                                    while (rs.next()) {
                                        current_msg.setFromEmail(rs.getString(1));
                                        ArrayList<String> rc = new ArrayList<String>();
                                        rc.add(rs.getString(2));
                                        current_msg.setRcptsList(rc);
                                        current_msg.setUtime(rs.getLong(3));
                                        currentLabel = rs.getString(4).toUpperCase();
                                    }

                                    VIPmodel.doTraining(current_msg, currentLabel);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (SQLException se) {
                            se.printStackTrace();
                        }

                        try {
                            System.out.println("going to save now");
                            VIPmodel.save(vipmodel_name);
                        } catch (Exception eed) {
                            eed.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("loaded the model");

                }
                // else we havea valid model
                System.out.println("total time: " + Utils.stringTime(System.currentTimeMillis() - starttime));

                // now to cycle through the table and set the vip score
                for (int i = 0; i < m_messageTable.getRowCount(); i++) {
                    // get a specific email

                    EMTEmailMessage current_msg = new EMTEmailMessage((String) m_messageTable.getValueAt(i,
                            i_mailrefColumn));
                    current_msg.setFromEmail((String) m_messageTable.getValueAt(i, i_senderColumn));
                    ArrayList<String> rl = new ArrayList<String>();
                    rl.add((String)m_messageTable.getValueAt(i, i_rcptColumn));
                    current_msg.setRcptsList(rl);
                    current_msg.setUtime(Long.parseLong((String) m_messageTable.getValueAt(i, i_utimeColumn)));
                    try {
                        resultScores rs = VIPmodel.doLabeling(current_msg);

                        m_messageTable.setValueAt("" + rs.getScore(), i, i_scoreColumn);

                    } catch (machineLearningException ml) {
                        ml.printStackTrace();
                    }

                }

                m_wg.setEnabled(true);
            }
        });
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.gridwidth = 1;

        // gridbag.setConstraints(priorityButton, constraints);
        // panel.add(priorityButton);

        // constraints.weightx = 1;
        m_graphicsPlot = new GraphicPlot();
        m_graphicsPlot.setTitle("Email History");
        m_graphicsPlot.setXLabel("Per month useage");
        m_graphicsPlot.setYLabel("Total Emails");

        // for navigation
        m_backStack = new Stack<String>();
        m_forwardStack = new Stack<String>();

        Box NavBox = Box.createHorizontalBox();
        NavBox.setToolTipText("Navigation Controls");
        // need to add back button
        back_but = new JButton(new ArrowIcon(ArrowIcon.LEFT));
        back_but.setToolTipText("Previous SQL view");
        back_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveBack();
            }
        });
        for_but = new JButton(new ArrowIcon(ArrowIcon.RIGHT));
        for_but.setToolTipText("Next SQL view");
        for_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveForward();
            }
        });
        back_but.setEnabled(false);
        for_but.setEnabled(false);

        JButton sql_but = new JButton("SQL");
        sql_but.setToolTipText("Current view as an sql statement which can be modified.");
        sql_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showSQL();
            }
        });
        NavBox.add(back_but);
        NavBox.add(sql_but);
        NavBox.add(for_but);
        constraints.gridx = 4;// 0
        constraints.gridy = 2;// 0
        constraints.gridwidth = 2;
        // gridbag.setConstraints(NavBox, constraints);
        // panel.add(NavBox);
        icon = Utils.createImageIcon("metdemo/images/Icon0145.gif", "Tables");

        m_tableMode = new JButton("Table", icon);
//      ??? m_tableMode.setBorder(new RoundBorder());
        // m_tableMode.setBackground(Color.white);
        m_tableMode.setToolTipText("View Table of Email Data");
        m_tableMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                m_graphic_mode = false;
              // removeGraphicFromView();
                m_graphicMode.setEnabled(true);
                m_tableMode.setEnabled(false);
                try {
                    Refresh(true, true, false, null);
                } catch (Exception c) {
                }
               
               placeTableandPreviewOnView();
               
               mainPanell.validate();
               mainPanell.repaint();
            }
        });

        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(m_tableMode, constraints);
        mainPanell.add(m_tableMode);
        icon = Utils.createImageIcon("metdemo/images/chart2.gif", "Graphical view of data");

        m_graphicMode = new JButton("Graphical", icon);
       //??? m_graphicMode.setBorder(new RoundBorder());
        // m_graphicMode.setBackground(Color.white);
        m_graphicMode.setToolTipText("Show Data as a visual graphic");
        /* Adding action to the graphic button*/
        m_graphicMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              //  BusyWindow bw = new BusyWindow("working", "work");
              //  bw.setVisible(true);
                m_graphic_mode = true;
                m_graphicMode.setEnabled(false);
                m_tableMode.setEnabled(true);
                // panel.remove(m_graphicsPlot);//so wont be zero size
                try {
                    Refresh(true, true, false, null);
                } catch (Exception c2) {
                    c2.printStackTrace();
                }
           //     removeTableandPreviewFromView();
                placeGraphicalView();
                mainPanell.validate();
               mainPanell.repaint();
            }
        });

        constraints.gridwidth = 1;
        constraints.gridx = 4;
        constraints.gridy = 0;

        gridbag.setConstraints(m_graphicMode, constraints);
        mainPanell.add(m_graphicMode);

        // end show knobs.

        m_tableModel = new MessageTableModel(COLUMN_NAMES, 0);
        m_messageTable = new JTable(m_tableModel) {
            /*
             * public TableCellRenderer getCellRenderer(int row, int column) {
             * 
             * return m_md5Renderer; }
             */
            protected void processKeyEvent(KeyEvent evt) {
                // int column = table.getSelectedColumn();
                // int row = table.getSelectedRow();
                if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {

                    updateMSGtextWindow(m_messageTable.getSelectedRow());
                }
                super.processKeyEvent(evt);
            }
        };

        m_messageTable.getTableHeader().setReorderingAllowed(false);
        m_tableModel.addMouseListenerToHeaderInTable(m_messageTable);
        // TableColumn getaColumn;// = m_messageTable.getColumn("Interest");
        // the geta column will allow us to access the columns without knowing
        // the specific location ie NOT HARDCODED
        // complements of shlomo @ columbia :)

        // set columns that we want to be privatized
        TableColumn getaColumn = m_messageTable.getColumn("Mailref");
        // getaColumn.setCellRenderer(m_md5Renderer);
        getaColumn.setMinWidth(0);
        getaColumn.setMaxWidth(0);
        getaColumn.setPreferredWidth(0);

        getaColumn = m_messageTable.getColumn("utime");
        getaColumn.setMinWidth(0);
        getaColumn.setMaxWidth(0);
        getaColumn.setPreferredWidth(0);

        getaColumn = m_messageTable.getColumn("Sender");
        getaColumn.setMaxWidth(130);
        getaColumn.setCellRenderer(m_emtTableCellRenderer);

        getaColumn = m_messageTable.getColumn("Recipient");
        getaColumn.setMaxWidth(130);
        getaColumn.setCellRenderer(m_emtTableCellRenderer);

        getaColumn = m_messageTable.getColumn("Subject");
        getaColumn.setCellRenderer(m_emtTableCellRenderer);// should be set
        getaColumn.setPreferredWidth(150);
        // different so
        // never renders

        // fix up columsn widths to optimize space
        getaColumn = m_messageTable.getColumn("CC");
        getaColumn.setMaxWidth(23);
        getaColumn.setPreferredWidth(23);
        getaColumn.setCellRenderer(m_emtTableCellRenderer);// should be set
        // different so
        // never renders

        getaColumn = m_messageTable.getColumn("A");
        getaColumn.setMaxWidth(23);
        getaColumn.setPreferredWidth(23);
        getaColumn.setCellRenderer(m_emtTableCellRenderer);// should be set
        // different so
        // never renders

        getaColumn = m_messageTable.getColumn("Size");
        getaColumn.setMaxWidth(50);
        getaColumn.setPreferredWidth(50);
        getaColumn.setCellRenderer(m_emtTableCellRenderer);// should be set
        // different so
        // never renders

        getaColumn = m_messageTable.getColumn("G");
        getaColumn.setMaxWidth(25);
        getaColumn.setPreferredWidth(25);
        // tcr.setToolTipText("Group number when sorting the columns");
        getaColumn = m_messageTable.getColumn("Label");
        getaColumn.setMaxWidth(50);
        getaColumn.setPreferredWidth(50);
        getaColumn = m_messageTable.getColumn("Score");
        getaColumn.setMaxWidth(20);
        getaColumn.setCellRenderer(m_emtTableCellRenderer); // unrender
        getaColumn = m_messageTable.getColumn("Date");
        getaColumn.setMaxWidth(75);
        getaColumn.setPreferredWidth(75);
        getaColumn.setCellRenderer(m_emtTableCellRenderer); // unrender
        getaColumn = m_messageTable.getColumn("Time");
        getaColumn.setMaxWidth(60);
        getaColumn.setPreferredWidth(60);
        getaColumn.setCellRenderer(m_emtTableCellRenderer); // unrender

        getaColumn = m_messageTable.getColumn("FP");
        getaColumn.setMaxWidth(20);

        // setup the group color basedon the number
        // will make it easier to read
        TableColumn numbersColumn = m_messageTable.getColumn("G");
        DefaultTableCellRenderer numberColumnRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {

                int cellValue = (value instanceof Integer) ? ((Integer) value).intValue() : 0;

                setBackground((cellValue % 2 == 1) ? Color.cyan : Color.yellow);
                setText((value == null) ? "" : value.toString());
            }
        };
        numberColumnRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        numbersColumn.setCellRenderer(numberColumnRenderer);

        numbersColumn = m_messageTable.getColumn("FP");
        DefaultTableCellRenderer fpColumnRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {

                int cellValue = (value instanceof Integer) ? ((Integer) value).intValue() : 0;

                if (cellValue == 0)
                    setBackground(Color.white);
                else if (cellValue == 1)
                    setBackground(Color.red);
                else
                    setBackground(Color.pink);

                setText((value == null) ? "" : value.toString());
            }
        };
        numbersColumn.setCellRenderer(fpColumnRenderer);

        JComboBox comboBox = new JComboBox(
                new String[] { "Unknown", "Interesting", "Spam", "Virus", "Not Interesting" });
        /*
         * comboBox.addItem("Unknown"); comboBox.addItem("Interesting");
         * comboBox.addItem("Spam"); comboBox.addItem("Virus");
         * comboBox.addItem("Not Interesting");
         */
        comboBox.addItemListener(this);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JComboBox source = (JComboBox) evt.getSource();
                int c = source.getSelectedIndex();

                if (c == 0) {
                    source.setBackground(Color.yellow);
                } else if (c == 1) {
                    source.setBackground(Color.red);
                } else if (c == 2) {
                    source.setBackground(Color.gray);
                } else if (c == 3) {
                    source.setBackground(Color.pink);
                } else {
                    source.setBackground(Color.green);
                }
            }
        });
        TableColumn interestColumn = m_messageTable.getColumn("Label");
        // Use the combo box as the editor in the "interest" column.
        interestColumn.setCellEditor(new DefaultCellEditor(comboBox));
        // Set a pink background and tooltip for the interest column
        // renderer.
        DefaultTableCellRenderer interestColumnRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {
                // int cellValue = (value instanceof String) ?
                // ((Number)value).intValue() : 0;
                // setForeground((cellValue > 30) ? Color.black :
                // Color.red);
                if (value instanceof String) {
                    if (((String) value).startsWith("Unknow")) {
                        setBackground(Color.yellow);
                        setText("Unknown");
                        // return;
                    } else if (((String) value).startsWith("Interestin")) {
                        setBackground(Color.red);
                        setText("Interesting");
                        // return;
                    } else if (((String) value).startsWith("Not Interestin")) {
                        setBackground(Color.green);
                        setText("Not Interesting");
                        // return;
                    } else if (((String) value).startsWith("Spa")) {
                        setBackground(Color.gray);
                        setText("Spam");
                        // return;
                    } else if (((String) value).startsWith("Viru")) {
                        setBackground(Color.pink);
                        setText("Virus");
                        // return;
                    }
                } else
                    setText((value == null) ? "" : value.toString());

            }
        };
        interestColumnRenderer.setToolTipText("Click for Class box");
        interestColumn.setCellRenderer(interestColumnRenderer);

        // Set a tooltip for the header of the colors column.
        TableCellRenderer headerRenderer = interestColumn.getHeaderRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer)
            ((DefaultTableCellRenderer) headerRenderer).setToolTipText("change me!");

        m_popupMenu = new JPopupMenu("Set Interest");
        JMenu m_popupMenu2 = new JMenu("Label");
        m_popupMenu2.add("Unknown").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                classifySelectedMessages("Unknown");
            }
        });
        m_popupMenu2.add("Interesting").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                classifySelectedMessages("Interesting");
            }
        });
        m_popupMenu2.add("Not Interesting").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                classifySelectedMessages("Not Interesting");
            }
        });
        m_popupMenu2.add("Spam").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                classifySelectedMessages("Spam");
            }
        });
        m_popupMenu2.add("Virus").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                classifySelectedMessages("Virus");
            }
        });
        m_popupMenu.add(m_popupMenu2);
        m_popupMenu.addSeparator();
        m_popupMenu.add("Copy to Clipboard").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printSelectedMessages(false);
                // System.out.println("gonna move its folder");
            }
        });
        m_popupMenu.addSeparator();
        m_popupMenu.add("Print!").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printSelectedMessages(true);
            }
        });
        m_popupMenu.addSeparator();
        m_popupMenu.add("Extract Keywords").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ExtractKeywordsFromMessages();
            }
        });

        m_popupMenu.addSeparator();
        m_popupMenu.add("Display Links").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ExtractLinksFromMessages();
            }
        });
        m_popupMenu.addSeparator();
        m_popupMenu.add("View all Folder Messages").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewAllSelectedMessageFolder();
            }
        });

        m_popupMenu.addSeparator();

        m_popupMenu.add("Move To Folder...").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveSelectedMessages();
            }
        });

        m_messageTable.setPreferredScrollableViewportSize(new Dimension(800, 70));
        m_messageTable.addMouseListener(this);

        m_tableMode.setEnabled(false);// since default is table view
        messageTableScroll = new JScrollPane();
        messageTableScroll.setViewportView(m_messageTable);
        
       
        
        
     
        
        
        
        mainPanell.setPreferredSize(new Dimension(900, 500));
        setViewportView(mainPanell);

       
        // bottom left panel part
        JPanel leftBottomside = new JPanel();
        leftBottomside.setLayout(new BorderLayout());
//      ???leftBottomside.setBorder(new RoundBorder());
        leftBottomside.setBackground(Color.yellow);
        noPlot = new JRadioButton("None", true);
        noPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userPlot.removeAll();
                userPlot.repaint();

            }
        });
        senderPlot = new JRadioButton("Sender", false);
        senderPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateMSGtextWindow(m_messageTable.getSelectedRow());
            }
        });
        rcptPlot = new JRadioButton("Rcpt", false);
        rcptPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateMSGtextWindow(m_messageTable.getSelectedRow());
            }
        });
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(noPlot);
        bg1.add(senderPlot);
        bg1.add(rcptPlot);
        noPlot.setSelected(true);
        JPanel choosebuttons = new JPanel();
        choosebuttons.add(noPlot);
        choosebuttons.add(senderPlot);
        choosebuttons.add(rcptPlot);
        
        //setup sender histo
//        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = TABLE_Y + 1; // was 1
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        constraints.gridheight = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        
        
        userPlot = new JPlot2D();
        leftBottomside.add(userPlot, BorderLayout.CENTER);
        leftBottomside.add(choosebuttons, BorderLayout.NORTH);
        userPlot.setPreferredSize(new Dimension(250, 300));
        //gridbag.setConstraints(leftBottomside, constraints);
        //mainPanell.add(leftBottomside);

    /*    // adding the text view panel
        constraints.gridheight = 1;
        constraints.gridx = TABLE_X;
        constraints.gridy = TABLE_Y + 2; // was 1
        constraints.gridwidth = TABLEWIDTH;

        // constraints.weightx = 1;
        constraints.weighty = 2;*/
        m_msg_displayed_mailref = new String();
        m_msg_displayed_user = new String();
        m_msgPreviewText = new JTextArea("Message body will go here.......");
        m_msgPreviewText.setEditable(false);
        msgPreviewAreaScroll = new JScrollPane(m_msgPreviewText);
        msgPreviewAreaScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        msgPreviewAreaScroll.setPreferredSize(new Dimension(600, 200));
        m_msgPreviewText.setLineWrap(true);
        m_msgPreviewText.setWrapStyleWord(true);
        /*gridbag.setConstraints(msgAreaScrollPane, constraints);
        mainPanell.add(msgAreaScrollPane);
*/
        
        messageViewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageTableScroll, msgPreviewAreaScroll);
        messageViewSplitPane.setDividerLocation(400);
        messageViewSplitPane.setContinuousLayout(true); 
     	messageViewSplitPane.setOneTouchExpandable(true); 
        //placeTableandPreviewOnView();
        
        constraints.weighty = 1;

        JPanel leftside = new JPanel();
        leftside.setLayout(new BorderLayout());
//      ???leftside.setBorder(new RoundBorder());
        leftside.setBackground(Color.yellow);

        choose_allFolders = new JRadioButton("All Folders", true);
        choose_allFolders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                folderListTree.setEnabled(false);

            }
        });
        choose_specificFolders = new JRadioButton("Specific", false);
        choose_specificFolders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                folderListTree.setEnabled(true);

            }
        });
        folderListTree.setEnabled(false);
        ButtonGroup folders = new ButtonGroup();
        folders.add(choose_allFolders);
        folders.add(choose_specificFolders);
        JPanel folderpanel = new JPanel();
        folderpanel.add(choose_allFolders);
        folderpanel.add(choose_specificFolders);
        leftside.add(folderpanel, BorderLayout.NORTH);
        JScrollPane treescroll = new JScrollPane(folderListTree);
        treescroll.setPreferredSize(new Dimension(110, 100));
        leftside.add(treescroll, BorderLayout.CENTER);

        constraints.gridx = 0;
        constraints.gridy = TABLE_Y ;//- 1;
        constraints.gridwidth = 2 + TABLEWIDTH;

        constraints.gridheight = 3;
        constraints.weighty = TABLEWIGHTY;
        
        //setup splitpane between folders and histo
        JSplitPane leftsideSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftside, leftBottomside);
        leftsideSplitPane.setDividerLocation(300);
        leftsideSplitPane.setContinuousLayout(true); 
        leftsideSplitPane.setOneTouchExpandable(true); 
        
        mainMessageSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftsideSplitPane,messageViewSplitPane);
        mainMessageSplitPane.setDividerLocation(.3D);
        mainMessageSplitPane.setContinuousLayout(true); 
        mainMessageSplitPane.setOneTouchExpandable(true); 
        
        
        
        
        
        
        gridbag.setConstraints(mainMessageSplitPane, constraints);//was left side
        mainPanell.add(mainMessageSplitPane);

        constraints.weighty = 1;
    }// end default constructor for message table

    
    private void startSearchInput(String searchWords){
    	
    	//if blank return
    	if(searchWords.length()<1){
    		return;
    	}
    	
    	//check if in middle of same
    	if(SearchString.equals(searchWords)){
    		return;
    	}
   
    	//need to compare old search string with new
    	
    	//if change need to stop current search and restart search
    	if(searchHandle!=null){
    		
    		searchHandle.interrupt();
    		
    	}
    	SearchString = searchWords;
    	
    	
    
    	//ok now lets grab the search string
    	searchHandle = new Thread(new Runnable(){

			public void run() {
				//lets setup what we want to do.
				//lets rember the search string
				String matchString = SearchString;
				//we need to freeze the table
				 m_tableModel.setSortingOn(false);
				//shove all information to side
				//int rowsnumber = m_tableModel.getRowCount();
				//int columnCount = m_tableModel.getColumnCount();
				//WARNING: this should only be done once since it will make our data lost
				 if(startingsearch)
				 {	 DataofTableWhileSearching = (Vector)m_tableModel.getAllData().clone();
				 	startingsearch = false;	
				 }
				 //lets move everything off the table
				clear();
				//now we can restore it by click ing cancel
				cancelSearchButton.setEnabled(true);
				//start to add anything which can match
				//lets loop through the data
				//to make it faster we will check ahead of time which type of matching we will be doing
				if(searchconstr.getSelectedIndex()==0){
					for(int i=0;i<DataofTableWhileSearching.size();i++){
						//current row
						Vector rowdata = (Vector)DataofTableWhileSearching.get(i);
						
						//check if subject matches
						if(((String)rowdata.get(i_senderColumn)).indexOf(matchString)>=0||
								((String)rowdata.get(i_rcptColumn)).indexOf(matchString)>=0){
							m_tableModel.setRowData(rowdata);
						}
					}
				}else if(searchconstr.getSelectedIndex()==1){
					for(int i=0;i<DataofTableWhileSearching.size();i++){
						//current row
						Vector rowdata = (Vector)DataofTableWhileSearching.get(i);
						
						//check if subject matches
						if(((String)rowdata.get(i_senderColumn)).indexOf(matchString)>=0){
							m_tableModel.setRowData(rowdata);
						}
					}
				}else if(searchconstr.getSelectedIndex()==0){
					for(int i=0;i<DataofTableWhileSearching.size();i++){
						//current row
						Vector rowdata = (Vector)DataofTableWhileSearching.get(i);
						
						//check if subject matches
						if(((String)rowdata.get(i_rcptColumn)).indexOf(matchString)>=0){
							m_tableModel.setRowData(rowdata);
						}
					}
				}
				else{
				//for subject lines
				matchString = matchString.toLowerCase();
				for(int i=0;i<DataofTableWhileSearching.size();i++){
					//current row
					Vector rowdata = (Vector)DataofTableWhileSearching.get(i);
					
					//check if subject matches
					if(((String)rowdata.get(i_subjectColumn)).toLowerCase().indexOf(matchString)>=0){
						m_tableModel.setRowData(rowdata);
					}
				}
				}
				
				
				
				//remember to update the stats bar
				
				//unfreeze the table
				 m_tableModel.setSortingOn(true);
				
				//System.out.println("will be searching for: " + SearchString);
				
			}
    		
    	});
    	searchHandle.start();
    	
    	
    	
    	
    	
    }
    
    
    
    
    
    
//    private void removeGraphicFromView(){
//    	return;
//    	//mainPanell.remove(m_graphicsPlot);
//    }
//    
//    private void removeTableandPreviewFromView(){
//    	return;
//  	//mainPanell.remove(messageViewSplitPane); 
//    	//mainPanell.remove(messageTableScroll);
//         //mainPanell.remove(msgPreviewAreaScroll);
//    }
    /**
     * Will setup the table into the current message window
     *
     */
    private void placeTableandPreviewOnView(){
    	
    	
    	
    	mainMessageSplitPane.setRightComponent(messageViewSplitPane);
    	
//    	 constraints.gridheight = 3;
//         constraints.gridx = TABLE_X;
//         constraints.gridy = TABLE_Y; // was 1
//         constraints.gridwidth = TABLEWIDTH;// table should span
//         constraints.anchor = GridBagConstraints.WEST;
//         constraints.fill = GridBagConstraints.BOTH;
//         // constraints.weightx = 1;
//         constraints.weighty = TABLEWIGHTY;
//         // jspMessages.setViewportView(m_messageTable);
         
         
        /* gridbag.setConstraints(messageTableScroll, constraints);
        mainPanell.add(messageTableScroll);*/
      //  gridbag.setConstraints(messageViewSplitPane, constraints);
     //   mainPanell.add(messageViewSplitPane);
        //placing the preivew window up
        
//        constraints.gridheight = 1;
//        constraints.gridx = TABLE_X;
//        constraints.gridy = TABLE_Y + 2; // was 1
//        constraints.gridwidth = TABLEWIDTH;
//        constraints.anchor = GridBagConstraints.WEST;
//        constraints.fill = GridBagConstraints.BOTH;
//        // constraints.weightx = 1;
//        constraints.weighty = 2;
//        
        m_msgPreviewText.setText("Message body will go here.......");
        
        /*gridbag.setConstraints(msgPreviewAreaScroll, constraints);
        mainPanell.add(msgPreviewAreaScroll);*/
   }
    
    private void placeGraphicalView(){
//    	constraints.gridheight = 3;//was 2
//        constraints.gridx = TABLE_X;
//        constraints.gridy = TABLE_Y; // was 1
//        constraints.gridwidth = TABLEWIDTH;// table should span
//        constraints.anchor = GridBagConstraints.WEST;
//        constraints.fill = GridBagConstraints.BOTH;
//        // constraints.weightx = 1;
//        constraints.weighty = TABLEWIGHTY + 2; //wasnt +2
//        gridbag.setConstraints(m_graphicsPlot, constraints);
//       mainPanell.add(m_graphicsPlot);
       mainMessageSplitPane.setRightComponent(m_graphicsPlot);
    }
    
 
    
    
    /** Initial setup of the buttons, sometime java doest do it correctly */
    public final void setup() {

        isLower = m_wg.isLower();
        isStop = m_wg.isStop();
        m_butSaveChanges.setEnabled(false);
        back_but.setEnabled(false);
        for_but.setEnabled(false);
        m_tableMode.setEnabled(false);
        m_view_direction.setEnabled(false);
        m_view_similiar.setEnabled(false);
        repaint();
    }

    /**
     * turn on off the wingui enabled feature
     */

    public final void Enabled(boolean b) {
        m_wg.setEnabled(b);
    }

    /**
     * Builds a machine learned model from the current data displayed called when you click on create model
     * button
     * 
     */
    private void buildModel() {// final String exp) {
        // System.out.println("in buld model*****************");

    	
    	//first a check to make sure we have data to work with.
    	int rows = m_messageTable.getRowCount();
        if (rows < 1) {
            JOptionPane.showMessageDialog(this, "No data on table to build model");
            return;
        }

        //grab all emails from current table, feed to modelbuilder
        
        
       //?????? EMTEmailMessage emails[] = getEmailsFromTable();
        
        //if (m_mb == null) {
        ModelBuilder  m_mb = new ModelBuilder(this, ModelBuilder.BUILD, m_wg.getTarget());
        //}
        m_mb.setVisible(true);

    }

    /*
     * private void evalModel(final String exp) {
     * 
     * //System.out.println("startig eval model"); int rows =
     * m_messageTable.getRowCount(); if (rows < 1) {
     * JOptionPane.showMessageDialog(this, "No Data on table to build model");
     * return; } int a = 0, b = 0; // String sql; a =
     * m_sql_exp.indexOf("where"); b = m_sql_exp.indexOf("group by"); //
     * System.out.println("a is " + a + " b is " + b); if (a > 0 && b > 0) m_sql =
     * new String(m_sql_exp.substring(a + 5, b)); else if (a > 0 && b < 0) m_sql =
     * new String(m_sql_exp.substring(a + 5)); else {//smoetime no where
     * statment //might need to clean out the group // by statements a =
     * m_sql_exp.lastIndexOf("e.utime"); if (a > 0 && b > 0) m_sql = new
     * String(m_sql_exp.substring(a + 9, b)); else if (a > 0) m_sql = new
     * String(m_sql_exp.substring(a + 9)); else m_sql = new String("1=1"); }
     * 
     * //ModelBuilder bd = //??????System.out.println(" mysql_exp: " + m_sql_exp + "
     * mysql: "+ // m_sql);
     * 
     * if (m_me == null) m_me = new ModelBuilder(this, 1);
     * m_me.setVisible(true); }
     */
    private void evalModel() {

        // System.out.println("startig eval model");
        int rows = m_messageTable.getRowCount();
        if (rows < 1) {
            JOptionPane.showMessageDialog(this, "No Data on table to build model");
            return;
        }

        //if (m_me == null)
          ModelBuilder  m_me = new ModelBuilder(this, ModelBuilder.EVALUATE, m_wg.getTarget());
        m_me.setVisible(true);
    }

    public int getViewIndex() {
        return m_view.getSelectedIndex();
    }

    /**
     * method to do evaluation of model over the data
     * 
     * @param name
     *            is the name of the model to load
     * @param model
     *            type of model
     * @param flag
     *            is distinct emails
     */

    public final void askEval(final String name,/* final int model, */int flag) {
        if (m_view.getSelectedIndex() == 1)
            flag = 1;

        // String report = new String();
        // -m_sql = m_sql.trim();
        // -if (m_sql.length() < 2)
        // - m_sql = new String("1=1");
        //if (m_me != null)
        //    m_me.setVisible(false);
        // will load m_threshold in here
        EvalClassifier2(name.trim()/* , m_sql , flag */);
    }

    /**
     * run a classifier over some data and return the result
     * 
     * @param name
     *            model to load
     * @param cond
     *            sql condition of training data
     * @param flag
     *            if to select distinct emails
     * @return result of the data
     */

    private final void EvalClassifier2(final String name/*
                                                         * final String cond,
                                                         * final int flag
                                                         */)// ,
    {
        // fetch the model
        m_mlearner = modelHelper.goLoadModel(name);
        // m_mlearner = (MLearner) lresu.elementAt(modelHelper.MODEL);
        model_num = m_mlearner.getIDNumber();// ((Integer)
                                                // lresu.elementAt(modelHelper.MOD_NUM)).intValue();

        if (m_mlearner == null) {
            JOptionPane.showMessageDialog(this, "Error: no model loaded");
            return;
        }
        // HashMap classifiedClasses = new HashMap();
        HashMap<String,String> initialClasses = new HashMap<String,String>();
        boolean haveBody = true;// assume have the body will reset this later as
        // Vector tvec = new Vector();//temp vec
        // boolean oneclass = m_mlearner.isOneClass();

        // update current classification threshold
        m_threshold = m_mlearner.getThreshold(); // since not null can call
                                                    // the
        // threshold off the model
        System.out.println("classify with : " + m_threshold + " as threshold");
        // update if needed
        isLower = m_mlearner.isLowerCase();
        isStop = m_mlearner.isStop();

        // build list of email to classify
        EMTEmailMessage emails[] = getEmailsFromTable();

        BusyWindow BW = new BusyWindow("progress bar", "Classifying Emails",true);
        BW.progress(1, emails.length);
        BW.setVisible(true);

        for (int i = 0; i < emails.length; i++) {
            initialClasses.put(emails[i].getMailref() + emails[i].getRCPT(), emails[i].getLabel());
        }
        // this is different for every modeler
        String feats = m_mlearner.getFeature();
        boolean[] SelectedFeaturesBoolean = m_mlearner.getFeatureBoolean();
        // StringTokenizer tok = new StringTokenizer(feats, ",");
        // if (feats.indexOf('\n') > 0)
        // feats = feats.substring(0, feats.indexOf("\n"));

        if (model_num == MLearner.ML_NBAYES_NGRAM || model_num == MLearner.ML_NBAYES_TXTCLASS) {
            haveBody = false;// will reset if have body

            int s = feats.indexOf("m.body");
            if (s > 0) {
                haveBody = true;
                feats = feats.substring(0, s - 1) + feats.substring(s + 6);
            }
            // remove features which have been collected already
            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDER]) {
                feats = feats.substring(8);
            }

            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPT]) {
                s = feats.indexOf("e.rcpt");
                if (s == 0) {
                    feats = feats.substring(6);
                } else {
                    feats = feats.substring(0, s - 1) + feats.substring(s + 6);
                }
            }

            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SIZE]) {
                s = feats.indexOf("e.size");
                if (s == 0) {
                    feats = feats.substring(6);
                } else {
                    feats = feats.substring(0, s - 1) + feats.substring(s + 6);
                }
            }

            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MAILREF]) {
                s = feats.indexOf("e.mailref");
                if (s == 0) {
                    feats = feats.substring(9);
                } else {
                    feats = feats.substring(0, s - 1) + feats.substring(s + 9);
                }
            }

            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SUBJECT]) {
                s = feats.indexOf("e.subject");
                if (s == 0) {
                    feats = feats.substring(9);
                } else {
                    feats = feats.substring(0, s - 1) + feats.substring(s + 9);
                }
            }
            String sqlfetch = new String("select " + feats + " from email as e where e.mailref = ? and rcpt = ?");
            // System.out.println(sqlfetch);

            try {
                if (feats.length() > 1)
                    synchronized (m_jdbcView) {
                        PreparedStatement ps = m_jdbcView.prepareStatementHelper(sqlfetch);
                        for (int i = 0; i < emails.length; i++) {
                            ps.setString(1, emails[i].getMailref());
                            ps.setString(2, emails[i].getRCPT());
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                int position = 1;
                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SNAME]) {
                                    emails[i].setFromName(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SDOM]) {
                                    emails[i].setFromDomain(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SZN]) {
                                    emails[i].setFromZN(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RNAME]) {
                                    emails[i].setRecName(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RDOM]) {
                                    emails[i].setRecDomain(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RZN]) {
                                    emails[i].setRecZN(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMRCPT]) {
                                    emails[i].setNumRcpts(rs.getShort(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMATTACH]) {
                                    emails[i].setNumberAttached(rs.getShort(position++));
                                }

                                // milaref remved

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.XMAILER]) {
                                    emails[i].setXMailer(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.DATES]) {
                                    emails[i].setDate(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMES]) {
                                    emails[i].setTime(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FLAGS]) {
                                    emails[i].setFlags(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MSGHASH]) {
                                    emails[i].setMsgHash(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RPLYTO]) {
                                    emails[i].setRPLYTO(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FORWARD]) {
                                    emails[i].setForward(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TYPE]) {
                                    emails[i].setType(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RECNT]) {
                                    emails[i].setReCOUNT(rs.getShort(position++));
                                }

                                // if(SelectedFeaturesBoolean[EmailFigures.SUBJECT]){
                                // emails[i].setSubject(rs.getString(position++));
                                // }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMEADJ]) {
                                    emails[i].setTimeADJ(rs.getLong(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FOLDER]) {
                                    emails[i].setFolder(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.UTIME]) {
                                    emails[i].setUtime(rs.getLong(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDERLOC]) {
                                    emails[i].setSenderLOC(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPTLOC]) {
                                    emails[i].setRCPTLOC(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.INSERTTIME]) {
                                    emails[i].setInsertTime(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.HOURTIME]) {
                                    emails[i].setGHour(rs.getShort(position++));
                                }
                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MONTHTIME]) {
                                    emails[i].setGMonth(rs.getShort(position++));
                                }

                            }
                        }
                    }
            } catch (SQLException s12) {
                System.out.println(s12);
            }

        } else {
            haveBody = true;

            // elements=0;
            /*
             * synchronized (m_jdbcView) { // System.out.println("eq2: " +
             * query); data = getPreBodyInfoFromTable(); //--data =
             * m_jdbcView.getSQLData(query); //data = m_jdbcView.getRowData();
             * 
             * for (int i = 0; i < data.length; i++) { //System.out.print(i);
             * 
             * mailref = data[i][3];//from above // get out body if
             * (!oldmailr.equals(mailref)) { data[i][numclass] =
             * data[i][numclass].toLowerCase(); oldmailr = mailref; data2 =
             * m_jdbcView.getSQLData("select m.body from message m where
             * m.mailref='" + mailref + "' and m.type like '" +
             * m_wg.getAttachType() + "'"); //data2 = m_jdbcView.getRowData();
             * 
             * if (data2.length > 0) {
             * 
             * for (int y = 0; y < data2.length; y++) { if (isLower && !isStop)
             * data[i][0] += (data2[y][0].toLowerCase());//-- + // " " // + //
             * data2[y][1].toLowerCase() // + ' '); else if (!isLower && isStop)
             * data[i][0] += Utils.replaceStop(data2[y][0]);//-- + // ' ' // + //
             * data2[y][1] // + ' // '); else if (isLower && isStop) data[i][0] +=
             * Utils.replaceStop(data2[y][0].toLowerCase());//-- + // ' ' //--+
             * data2[y][1].toLowerCase() + ' '); else data[i][0] +=
             * (data2[y][0]);//-- + ' ' // + // data2[y][1] // + ' '); } } else {
             * data[i][0] = " "; } } else { data[i][numclass] =
             * data[i][numclass].toLowerCase(); data[i][0] = data[i -
             * 1][0];//repeat since mailref // same } } }//end synchrnoized
             * //paste
             */
        }// end else for not nb

        // we will fetch the mailref,subject(which we have already) and then
        // calculate the proportion if any
        try {
            for (int i = 0; i < emails.length; i++) {
                // TODO: fix to make this obvious to user
                if (haveBody) {
                    String[] bodyinfo = (m_jdbcView.getBodyByMailref(emails[i].getMailref()));
                    for (int j = 0; j < bodyinfo.length; j++) {
                        if (isLower && !isStop)
                            bodyinfo[j] = bodyinfo[j].toLowerCase();
                        else if (isLower && isStop)
                            bodyinfo[j] = Utils.replaceStop(bodyinfo[j].toLowerCase(), m_wg.getHash());
                        else if (!isLower && isStop)
                            bodyinfo[j] = Utils.replaceStop(bodyinfo[j], m_wg.getHash());
                    }
                    emails[i].setBody(bodyinfo);
                }
                // ready to classify

                resultScores rs = m_mlearner.doLabeling(emails[i]);
                // classifiedClasses.put(emails[i].getMailref() +
                // emails[i].getRCPT(), rs);

                m_messageTable.setValueAt(rs.getLabel(), i, i_labelColumn);
                m_messageTable.setValueAt("" + rs.getScore(), i, i_scoreColumn);

                BW.progress(i);
                // TODO: clear body for space saving??
            }// end for data loop
        } catch (machineLearningException m2) {
            System.out.println(m2);
        }
        // real work done here
        /*
         * result = m_mlearner.classify(data); //actual classifications.
         * 
         * //need to fix up the results if (oneclass) {
         * 
         * for (int i = 0; i < result.length; i++) { if
         * (Double.parseDouble(result[i][1]) < m_threshold) result[i][0] =
         * flipClass(result[i][0]); } } else {//RULEZ
         * 
         * for (int i = 0; i < result.length; i++) { if
         * (result[i][0].equals(TARGET_CLASS)) if
         * (Double.parseDouble(result[i][1]) < m_threshold) result[i][0] =
         * flipClass(result[i][0]); } }
         */
        // } catch (Exception e1) {
        // JOptionPane.showMessageDialog(Messages.this, "oops" + e1,
        // "Exception", JOptionPane.ERROR_MESSAGE);
        // }
        // m_status.setText(" ");
        // return result;
        // System.out.println("back from classify");
        // m_wg.askEval(name,m_sql,model,flag);
        // ----int rows = 0;
        // ----rows = m_messageTable.getRowCount();
        // int target_column1 =
        // m_messageTable.getColumn("Label").getModelIndex();
        // int target_column2 =
        // m_messageTable.getColumn("Score").getModelIndex();
        // if (rows < 1 || result == null) {
        // JOptionPane.showMessageDialog(Messages.this, "No Data On Table to
        // Group", "Problem",
        // JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        // --try {
        // took out hard coding to get speed
        // int c = 0;
        // m_messageTable.getColumn("Mailref").getModelIndex();//sort data
        // on mailref column
        // ---m_tableModel.setSortingOn(false);
        // ---rows = m_messageTable.getRowCount();
        // if (rows < 1 || result == null) {
        // JOptionPane.showMessageDialog(this, "No data on table when
        // calling refresh to group");
        // return;
        // }
        // make sure they match
        // if (rows != result.length)
        // throw new Exception("result size not equal to table size in eval
        // model message");
        // end paste
        // assume some labels will change:
        /*
         * int fp = 0, tp = 0, tn = 0, fn = 0; int right = 0, wrong = 0; String
         * cur; for (int i = 0; i < rows; i++) { cur = ((String)
         * m_tableModel.getValueAt(i, i_labelColumn)).substring(0,
         * 1).toLowerCase();
         * 
         * if (cur.equals(result[i][0])) { right++;
         * 
         * if (cur.equals(TARGET_CLASS)) tp++; else tn++; } else { wrong++; if
         * (cur.equals(TARGET_CLASS)) { fn++;
         * 
         * m_messageTable.setValueAt("" + -1, i, 11); } else { fp++;
         * 
         * m_messageTable.setValueAt("" + 1, i, 11); } }
         * 
         * m_tableModel.setValueAt(result[i][0], i,
         * i_labelColumn);//target_column1);
         * m_tableModel.setValueAt(result[i][1], i,
         * i_scoreColumn);//target_column2); } System.out.println("result
         * length: " + result.length); System.out.println("rt: " + right + " ng: " +
         * wrong + " rt per " + (double) right / (double) result.length);
         * System.out.println("tp: " + tp + " tn: " + tn + " tp per " + (double)
         * tp / (double) result.length); System.out.println("fp: " + fp + " fn: " +
         * fn); System.out.println("det% " + Utils.round(((double) tp / (double)
         * (tp + fp)), 3) + " fp%: " + Utils.round(((double) fp / (double) (fp +
         * tn)), 3));
         */// --} catch (Exception e) {
        // JOptionPane.showMessageDialog(this, "Error getting details from
        // database: " + e);
        // ---JOptionPane.showMessageDialog(Messages.this, "oops22:" + e,
        // "Exception", JOptionPane.ERROR_MESSAGE);
        // --} finally {
        BW.setVisible(false);
        update_all_labels = true;
        m_butSaveChanges.setEnabled(true);
        m_tableModel.setSortingOn(true);
        // --}

    }// end get data

    /*
     * static final String flipClass(String s) { if (s.startsWith("n") ||
     * s.startsWith("u")) return "s"; //else return "n"; }
     */

    private final void viewAllSelectedMessageFolder() {

        // first need to get the list of selected messages

        // for simplicity we will only work if one message selected.

        m_tableModel.setSortingOn(false);
        int[] rows = m_messageTable.getSelectedRows();
        int len = rows.length;
        if (len == 0 || len > 1) {
            JOptionPane.showMessageDialog(MessageWindow.this, "Please choose only one");
        }

        String mailref = (String) m_messageTable.getValueAt(rows[0], i_mailrefColumn);
        try {
            String[] data = m_jdbcView.getSQLDataByColumn("select folder from email where mailref = '" + mailref + "'",
                    1);

            // int offset1 =0;
            if (data == null) {
                System.out.println("!!!!null data in retrn folder name");
                return;
            }
            System.out.println("looking for : " + data[0]);
            m_view_current_folder = data[0];
            choose_specificFolders.setSelected(true);
            choose_allFolders.setSelected(false);
            folderListTree.setEnabled(true);
            // TODO: see how to set tree
            StringTokenizer stok = new StringTokenizer(data[0], "/\\", true);
            // while(stok.hasMoreTokens()){
            // here
            DefaultMutableTreeNode child = roottree;
            String current_name = new String();
            // idea is to take care of 2 types of cases:
            // 1 - c:\shlomo which is c:,\,shlomo
            // 2 - shlomo which is shlomo
            // we will recursivly build 2 tokens (so that we know how the
            // seperator looks like (\ or /) and use it
            // as node names

            while (stok.hasMoreTokens()) {
                current_name = stok.nextToken();
                // check for second
                if (stok.hasMoreTokens()) {
                    current_name += stok.nextToken();
                }
                // now to find it in the tree
                for (Enumeration enumer1 = child.children(); enumer1.hasMoreElements();) {
                    DefaultMutableTreeNode dftn = (DefaultMutableTreeNode) enumer1.nextElement();
                    if (dftn.toString().startsWith(current_name)) {

                        child = dftn;
                        break;
                    }
                }

            }
            TreePath currentpath = new TreePath(child.getPath());
            folderListTree.expandPath(currentpath);
            folderListTree.getSelectionModel().setSelectionPath(currentpath);
            folderListTree.scrollPathToVisible(currentpath);

            // here

            // }

            /*
             * for (Enumeration iter1 = roottree.preorderEnumeration(); iter1
             * .hasMoreElements();) {
             * 
             * DefaultMutableTreeNode current = (DefaultMutableTreeNode) iter1
             * .nextElement(); if (!current.isLeaf()) continue;
             * 
             * String now = current.getParent().toString() + current.toString();
             * 
             * 
             * if (now.equals(data[0])) { System.out.println("at :" + now);
             * 
             * //TreeNode tp[] =
             * current.getPath();//folderListTree.getSelectionPath(); TreePath
             * currentpath = new TreePath(current);
             * folderListTree.getSelectionModel().setSelectionPath(
             * currentpath);
             * 
             * //Object node = folderListTree.getLastSelectedPathComponent();
             * 
             * folderListTree.expandPath(currentpath);
             * folderListTree.scrollPathToVisible(currentpath); /*for (int j =
             * 0; j < tp.length; j++) { if (tp[j].isLeaf()) { // int
             * n=folderListTree.getRowForPath(currentpath); //
             * System.out.println("n is "+n); // TreePath(tp[j])); //JTree
             * tree,Object value, boolean selected, boolean expanded, //boolean
             * leaf, int row, boolean hasFocus) //
             * folderListTree.getCellRenderer().getTreeCellRendererComponent(folderListTree,current.toString(),true,true,true,n,true);
             * 
             * folderListTree.scrollPathToVisible(new TreePath(tp[j]));
             * 
             * //folderListTree.setLeadSelectionPath(new TreePath(tp[j])); }
             * else { folderListTree.expandPath(new TreePath(tp[j]));
             *  } }
             * //folderListTree.scrollPathToVisible(currentpath);//currentpath); /
             * Refresh(true, true, false, null); break; }
             *  }
             */
            /*
             * 
             * for (int i = 0; i < m_view_folder.getItemCount(); i++) {
             * 
             * if (m_view_folder.getItemAt(i).equals(data[0])) {
             * m_view_folder.setSelectedIndex(i);
             * 
             * Refresh(true, true, false, null);
             * 
             * break; } }
             */

        } catch (SQLException s) {
            s.printStackTrace();
        }
        m_tableModel.setSortingOn(true);

    }

    public final void setMessageView(String name) {
        m_view_user.setSelectedIndex(1);

    }

    /*
     * public final void setMessageView(String name) {
     * 
     * /*int i, n = m_view_user.getItemCount(); //first need to see if we can
     * find the name for (i = 0; i < n; i++) {
     * 
     * if (m_view_user.getItemAt(i).equals(name)) { break; }
     *  }* / int i; if (i != n) { // TODO check if we need to save anything???
     * if (update_all_labels == true) { saveLabelChanges(); } //need to setup so
     * we see all the emails: //need date select set to no
     * m_view_date.setSelected(false); //need to set to this user
     * m_view_user.setSelectedIndex(i); //need to set to all folders
     * //m_view_folder.setSelectedIndex(0); choose_allFolders.setSelected(true);
     * m_view_date.setSelected(false);//all dates try { Refresh(true, true,
     * false, null); } catch (SQLException se) { se.printStackTrace(); } } }
     */
    public final void setMessageViewGroup(String[] name) {

        if (name.length <= 0) {
            return;
        }
        if (update_all_labels == true) {
            saveLabelChanges();
        }

        m_view_user.setSelectedIndex(0);// all user
        // TODO check if we need to save anything???

        // need to setup so we see all the emails:
        // need date select set to no
        m_view_date.setSelected(false);
        // need to set to this user
        // need to set to all folders
        // m_view_folder.setSelectedIndex(0);
        choose_allFolders.setSelected(true);
        m_view_date.setSelected(false);// all dates

        m_view.setSelectedIndex(7);// choose groups

        String toadd = new String("('");
        for (int i = 0; i < name.length; i++) {
            if (i == 0) {
                toadd += name[i];

            } else {
                toadd += "','" + name[i];
            }

        }
        toadd += "')";
        // TODO:
        // should check if already on the list!!
        m_view_similiar.addItem(toadd);
        m_view_similiar.setSelectedIndex(m_view_similiar.getItemCount() - 1);

        try {
        	
            Refresh(true, true, false, null);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    /** this sets our lowercase and stop list booleans */
    public final void setBooleans(final boolean stop, final boolean lower) {
        isStop = stop;
        isLower = lower;
    }
    
   /**
    * Will train a model based on some settings passed 
    * @param modelName is the taget file to save to
    * @param modelType type fo model we are building
    * @param percentage percetnage of data to use
    * @param classesUse which classes to include in the model
    * @param flush if we reset the model before starting
    */
    public void askTrain(final String modelName, final int modelType, final int percentage, boolean[] classesUse,
            boolean flush) {
       
    	if (m_wg != null) {
            model_num = modelType;
            TrainClassifierPerExample(/* m_sql, */m_wg.getFeatures(), m_wg.getBooleanFeatures(), modelName, 1, 100,
                    percentage, model_num, classesUse, flush);
        }
    }

    /*
     * public final void set_win_gui(final winGui w) { m_wg = w; isLower =
     * m_wg.isLower(); isStop = m_wg.isStop(); }
     */

    /** training window from trainer class */

    public final void setTargetClass(final String s) {
        TARGET_CLASS = s;
    }

    // end train classifier
    // int totalspam,totaln;
    // det detection rate ie is the stuff the system go right, sometimes called
    // precision
    // tp / tp + fp
    // spam which is really spam / total we said is spam
    // fp = fp/fp+tn
    // normal we said is spam / all we said is normal.
    /**
     * 
     * General interface to train clasifier
     * <P>
     * Allows the user to use multiple paramters in the gui to create many
     * combinations for machine learning agents
     * 
     * @param cond
     *            string of condi to choose the sql data
     * 
     * @param feats
     *            the features to choose the (depends on model)
     * 
     * @param modelName
     *            name of file to save model to.
     * 
     * @param folds
     *            number of k-fold for cross validiton if in fold validation
     *            phase (flag set).
     * 
     * @param per
     *            percentage of data to do cross validation on.
     * 
     * @param type
     *            is the type of learner.
     * @param proportion
     *            is the proportion the target class to others. -1 means ignore
     *            and just use all. n is that n % of the stuff should be target
     *            class.
     * @return nothing since the training will be saved as the model
     */

    private void TrainClassifierPerExample(
    /* final String cond, */String feats, boolean[] SelectedFeaturesBoolean, final String modelName, final int folds,
            final int per, final int proportion, final int type, boolean[] UseClasses, boolean flushModel) {

        // System.out.println("feats are: " + feats);
        HashMap<String,String> initialClasses = new HashMap<String,String>();
        HashMap<String,resultScores> classifiedClasses = new HashMap<String,resultScores>();
        String[] positionMailrefs;
        boolean oneClass = false;
        boolean haveBody = false;// assume no body part for naive bayes, will
        // update as needed
        String classes[];
        Vector<String> lookup = new Vector<String>(); // since small n wont matter
        isLower = m_wg.isLower();// recheck
        isStop = m_wg.isStop();

        // using a static class mlearner which generalizes the learning process.
        if (type == MLearner.ML_NBAYES_NGRAM)
            m_mlearner = new NBayesClassifier(NBayesClassifier.SUBTYPE_NGRAM, SelectedFeaturesBoolean);
        else if (type == MLearner.ML_NBAYES_TXTCLASS) // from drop down menu
            m_mlearner = new NBayesClassifier(NBayesClassifier.SUBTYPE_TXTCLASSIFIER, SelectedFeaturesBoolean);
        else if (type == MLearner.ML_NGRAM)
            m_mlearner = new NGram();
        else if (type == MLearner.ML_HARDCODED)
            m_mlearner = new CodedModel_1();
        else if (type == MLearner.ML_EWCOMBINATION)
            m_mlearner = new CorModel();
        else if (type == MLearner.ML_OUTLOOK)
            m_mlearner = new OutlookModel();
        else if (type == MLearner.ML_TEXTCLASSIFIER)
            m_mlearner = new TextClassifier();
        else if (type == MLearner.ML_PGRAM)
            m_mlearner = new PGram();
        else if (type == MLearner.ML_TFIDF)
            m_mlearner = new Tfidf();
        else if (type == MLearner.ML_LINKS)
            m_mlearner = new LinkAnalysisLearner();
        else if (type == MLearner.ML_LIMITEDNGRAM)
            m_mlearner = new NGramLimited();
        else if (type == MLearner.ML_SVM)
            m_mlearner = new SVMLearner();
        else {
            JOptionPane.showMessageDialog(MessageWindow.this, "Unknown Learner choice", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        m_tableModel.setSortingOn(false);

        m_mlearner.setProgress(true);// show progress by default
        if (flushModel) {
            m_mlearner.flushModel();
        }
        m_mlearner.setLowerCase(isLower);
        m_mlearner.setStop(isStop);
        // count number of classes involved in training
        classes = getDistinctClassLabelsFromTable(UseClasses, true);
        if (classes.length == 1) {
            oneClass = true;
        }
        m_mlearner.isOneClass(oneClass); // remember if one class training
        m_mlearner.setFeature(feats); // save feature string
        // setup the emails to learn off
        EMTEmailMessage emails[] = getEmailsFromTable();
        // need position info since we might be throwing out some
        positionMailrefs = new String[emails.length];
        // will save initial classes of emails
        for (int i = 0; i < emails.length; i++) {
            positionMailrefs[i] = new String(emails[i].getMailref() + emails[i].getRCPT());
            initialClasses.put(emails[i].getMailref() + emails[i].getRCPT(), emails[i].getLabel());
        }
        BusyWindow BW = new BusyWindow("Progress Bar", "Training",true);
        BW.progress(1, emails.length);
        BW.setVisible(true);

        // for training if we need a certain proportion
        emails = doProportionByEmails(emails, proportion); // 1 is the label

        try {
            if (type == MLearner.ML_NBAYES_NGRAM || type == MLearner.ML_NBAYES_TXTCLASS) {

                int s = feats.indexOf("m.body");
                if (s > 0) {
                    haveBody = true;
                    feats = feats.substring(0, s - 1) + feats.substring(s + 6);
                }

                // remove features which have been collected already
                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDER]) {
                    feats = feats.substring(9);
                }

                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPT]) {
                    s = feats.indexOf("e.rcpt");
                    if (s == 0) {
                        feats = feats.substring(6);
                    } else {
                        feats = feats.substring(0, s - 1) + feats.substring(s + 6);
                    }
                }

                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SIZE]) {
                    s = feats.indexOf("e.size");
                    if (s == 0) {
                        feats = feats.substring(6);
                    } else {
                        feats = feats.substring(0, s - 1) + feats.substring(s + 6);
                    }
                }

                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MAILREF]) {
                    s = feats.indexOf("e.mailref");
                    if (s == 0) {
                        feats = feats.substring(9);
                    } else {
                        feats = feats.substring(0, s - 1) + feats.substring(s + 9);
                    }
                }

                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SUBJECT]) {
                    s = feats.indexOf("e.subject");
                    if (s == 0) {
                        feats = feats.substring(9);
                    } else {
                        feats = feats.substring(0, s - 1) + feats.substring(s + 9);
                    }
                }
                String sqlfetch = new String("select " + feats + " from email as e where e.mailref = ? and rcpt = ?");
                System.out.println(sqlfetch);

                if (feats.length() > 1)
                    synchronized (m_jdbcView) {
                        PreparedStatement ps = m_jdbcView.prepareStatementHelper(sqlfetch);
                        for (int i = 0; i < emails.length; i++) {
                            // System.out.println(emails[i].getMailref() +" "+
                            // emails[i].getRCPT());
                            ps.setString(1, emails[i].getMailref());
                            ps.setString(2, emails[i].getRCPT());

                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                int position = 1;
                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SNAME]) {
                                    emails[i].setFromName(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SDOM]) {
                                    emails[i].setFromDomain(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SZN]) {
                                    emails[i].setFromZN(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RNAME]) {
                                    emails[i].setRecName(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RDOM]) {
                                    emails[i].setRecDomain(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RZN]) {
                                    emails[i].setRecZN(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMRCPT]) {
                                    emails[i].setNumRcpts(rs.getShort(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMATTACH]) {
                                    emails[i].setNumberAttached(rs.getShort(position++));
                                }

                                // milaref remved

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.XMAILER]) {
                                    emails[i].setXMailer(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.DATES]) {
                                    emails[i].setDate(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMES]) {
                                    emails[i].setTime(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FLAGS]) {
                                    emails[i].setFlags(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MSGHASH]) {
                                    emails[i].setMsgHash(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RPLYTO]) {
                                    emails[i].setRPLYTO(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FORWARD]) {
                                    emails[i].setForward(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TYPE]) {
                                    emails[i].setType(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RECNT]) {
                                    emails[i].setReCOUNT(rs.getShort(position++));
                                }

                                // if(SelectedFeaturesBoolean[EmailFigures.SUBJECT]){
                                // emails[i].setSubject(rs.getString(position++));
                                // }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMEADJ]) {
                                    emails[i].setTimeADJ(rs.getInt(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FOLDER]) {
                                    emails[i].setFolder(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.UTIME]) {
                                    emails[i].setUtime(rs.getInt(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDERLOC]) {
                                    emails[i].setSenderLOC(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPTLOC]) {
                                    emails[i].setRCPTLOC(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.INSERTTIME]) {
                                    emails[i].setInsertTime(rs.getString(position++));
                                }

                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.HOURTIME]) {
                                    emails[i].setGHour(rs.getShort(position++));
                                }
                                if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MONTHTIME]) {
                                    emails[i].setGMonth(rs.getShort(position++));
                                }

                            } else {
                                System.out.print("nors ");
                            }
                        }
                    }

            } else if (type == MLearner.ML_NGRAM || type == MLearner.ML_PGRAM || type == MLearner.ML_TEXTCLASSIFIER
                    || type == MLearner.ML_HARDCODED || type == MLearner.ML_OUTLOOK || type == MLearner.ML_TFIDF
                    || type == MLearner.ML_SVM || type == MLearner.ML_EWCOMBINATION) {

                haveBody = true;

            } else {
                m_tableModel.setSortingOn(true);

                JOptionPane.showMessageDialog(this, "Model not setup correctly");
                return;
            }

            // we will fetch the mailref,subject(which we have already) and then
            // calculate the proportion if any
            for (int i = 0; i < emails.length; i++) {
                // TODO: fix to make this obvious to user
                if (haveBody) {
                    String[] bodyinfo = (m_jdbcView.getBodyByMailref(emails[i].getMailref()));
                    for (int j = 0; j < bodyinfo.length; j++) {
                        if (isLower && !isStop)
                            bodyinfo[j] = bodyinfo[j].toLowerCase();
                        else if (isLower && isStop)
                            bodyinfo[j] = Utils.replaceStop(bodyinfo[j].toLowerCase(), m_wg.getHash());
                        else if (!isLower && isStop)
                            bodyinfo[j] = Utils.replaceStop(bodyinfo[j], m_wg.getHash());
                    }
                    emails[i].setBody(bodyinfo);
                }
                // System.out.println("training: " + emails[i].getLabel() + " "
                // + MLearner.getClassNumber(emails[i].getLabel()));
                m_mlearner.doTraining(emails[i], emails[i].getLabel());
                BW.progress(i);
                // TODO: clear body for space saving??
            }// end for data loop

            System.out.println("done with training");
            // trying to build a report to return for cross validation purposes

            for (int i = 0; i < emails.length; i++) {
                resultScores rs = m_mlearner.doLabeling(emails[i]);
                BW.progress(i);
                classifiedClasses.put(emails[i].getMailref() + emails[i].getRCPT(), rs);
            }
            {
                // will hash each class to spot in our table
                for (int i = 0; i < classes.length; i++) {
                    lookup.addElement(classes[i]);
                }
            }// end else generate return
            String report = makeReport(classes, oneClass, lookup, positionMailrefs, initialClasses, classifiedClasses);
            // build roc curve.
            // basic idea: given the max and min score, divide the interval by
            // 100, and
            // for each number, use it as a threshold to see the detection rate,
            // vs fp
            // find max min
            if (!m_timespam) {
                JPanel m_pop = new JPanel();
                m_pop.setLayout(new BorderLayout());
                m_pop.setSize(plotxsize + 100, 810);
                // ADD ME BACK TODO:
                m_pop.add(calc_roc_curvePerExample(oneClass, initialClasses, classifiedClasses), BorderLayout.CENTER);
                // add the text of result
                JTextArea textArea = new JTextArea("Done building the model " + modelName.trim() + "\n" + report
                        + "\n have saved to disk\n\n");
                // textArea.setFont(new Font("Serif", Font.ITALIC, 16));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                // JScrollPane areaScrollPane = new JScrollPane(textArea);
                // areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                // areaScrollPane.setPreferredSize(new Dimension(plotxsize,
                // plotysize));
                m_pop.add(textArea, BorderLayout.SOUTH);// areaScrollPane,BorderLayout.SOUTH);
                // m_pop.setVisible(true);

                JFrame jf = new JFrame("roc results");
                JScrollPane m_pop2 = new JScrollPane(m_pop);
                m_pop2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                jf.getContentPane().add(m_pop2);
                jf.setSize(plotxsize + 120, 510);// width height
                jf.setVisible(true);
                jf.requestFocus();
            }// end roc only if not timespan
            // end build report
            m_mlearner.setInfo(report);
            // to save
            (new Thread(new Runnable() {
                public void run() {
                    try {

                        // setThreshold
                        t_chooser = new JFrame("Choose Threshold");
                        JButton set_threshold = new JButton("Set and Save");
                        JButton cancel_threshold = new JButton("Cancel");
                        set_threshold.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                // ned to save stuff here
                                t_chooser.setVisible(false);
                            }
                        });

                        cancel_threshold.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                t_chooser.setVisible(false);
                            }
                        });
                        // mulitply by 1000 incase decimal will get up to 4
                        // spots
                        js_threshold = new JSlider(0, d_thresholds.length - 1, 0);
                        // (int)(1000*d_thresholds[0][0]),(int)(1000*d_thresholds[d_thresholds.length][0]),(int)(1000*d_thresholds[0][0]));
                        js_threshold.setMajorTickSpacing(5);
                        js_threshold.setPaintTicks(true);
                        js_threshold.addChangeListener(new ChangeListener() {
                            public void stateChanged(ChangeEvent evt) {
                                t_chooser.getContentPane().remove(2);
                                int i = js_threshold.getValue();
                                JLabel tpanel = new JLabel("<html>Threshold: " + d_thresholds[i][0] + "<P>Accuracy: "
                                        + d_thresholds[i][3] + " <P>Detection: " + d_thresholds[i][1] + " <P>FP: "
                                        + d_thresholds[i][4] + " <P>Error: " + d_thresholds[i][2] + "<html>");
                                t_chooser.getContentPane().add(tpanel, BorderLayout.CENTER);
                                t_chooser.pack();

                            }
                        });

                        JPanel j = new JPanel();
                        j.add(set_threshold);
                        j.add(cancel_threshold);
                        t_chooser.getContentPane().add(js_threshold, BorderLayout.NORTH);
                        JLabel tpanel = new JLabel("<html>Threshold: " + d_thresholds[0][0] + "<P>Accuracy: "
                                + d_thresholds[0][3] + " <P>Detection: " + d_thresholds[0][1] + " <P>FP: "
                                + d_thresholds[0][4] + " <P>Error: " + d_thresholds[0][2] + "<html>");
                        // JLabel tpanel = new JLabel("Accuracy: "
                        // +d_thresholds[0][3]+" Detection: "
                        // +d_thresholds[0][1]+ " Error: " + d_thresholds[0][2]
                        // );
                        t_chooser.getContentPane().add(j, BorderLayout.SOUTH);
                        t_chooser.getContentPane().add(tpanel, BorderLayout.CENTER);
                        t_chooser.pack();
                        t_chooser.setVisible(true);

                        while (t_chooser.isVisible()) {
                            Thread.sleep(500);
                        }
                        int th = js_threshold.getValue();

                        System.out.println("going to save model with the following threshold: " + d_thresholds[th][0]);
                        m_mlearner.setThreshold(d_thresholds[th][0]); // replace
                        // min
                        // with
                        // actual.
                        // need to set threshold here.
                        m_mlearner.save(modelName.trim());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MessageWindow.this, "Exception thrown in saving model " + e);
                    }

                }
            })).start();

            // String kreports = new String();
            // now to do it manually here and see where we get.
            // String modelinfo = m_mlearner.getInfo();
            // end manual reporting.
            // if(!m_timespam)
            // m_mlearner = null;//trying to clean up memory. since we might
            // create a huge file, lets destroy since other part of gui will be
            // loadin git up anyway.

            // if (m_timespam) {
            // m_timespam_report += "=======START=======\n" + modelName.trim() +
            // "\n" + report + "\n";
            // }
            /*
             * else if(!m_cvalidate) JOptionPane.showMessageDialog(this, "Done
             * building the model " + modelName.trim() + "\n have saved to
             * disk\n\n"+modelinfo ); else JOptionPane.showMessageDialog(this,
             * "Done building the model " + modelName.trim() + "\n have saved to
             * disk\n\n"+modelinfo + " \nFor k fold report\n" + kreports );
             */

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception thrown in Trainer = " + e);
            // System.exit(1);
        }
        m_tableModel.setSortingOn(true);
        BW.setVisible(false);
    }// end get data

    // end train classifier

    // from spam filters: bayes vs chi ...o'brian
    // fn = spam as legitamate -
    // tp = spam marked as spam -
    // fp = legit marked as spam -
    // tn = legitamte marked as legit -
    // recal = tp / tp+fn
    // precision = tp / tp + fp
    // error rate = fp + fn / (tp+fp+fn+tn)

    // boolean notnb = false;
    // if (type > 1 && type < 11)
    // notnb = true;
    public String makeReport(String classes[], boolean oneClass, Vector lookup, String[] positionMailrefs,
            HashMap initialClasses, HashMap classifiedClasses) {
        String report = new String("\n========== cross-validation =============\n");

        int totalspam = 0, totaln = 0;

        // for (int i = 0; i < result.length; i++) {
        for (Iterator it = classifiedClasses.values().iterator(); it.hasNext();) {
            resultScores rs = (resultScores) it.next();
            if (rs.getLabel().equals(TARGET_CLASS) && rs.getScore() > resultScores.MIDSCORE) {
                totalspam++;
            } else {
                totaln++;

            }
        }

        int right, wrong, detection, tn, fp, wrong2;
        right = wrong = detection = tn = fp = wrong2 = 0;
        int[][] confusion = new int[classes.length][classes.length];
        for (int i = 0; i < classes.length; i++)
            for (int k = 0; k < classes.length; k++)
                confusion[i][k] = 0;
        for (int i = 0; i < positionMailrefs.length; i++) { // NEED EVAL
            // CLASSIFIER TO CHECK
            // THE MAILREF
            // update the table view

            // cycle through table mailref
            // m_messageTable.setValueAt("" + value, i,
            // i_falsepositiveColumn);
            // initial class will have first
            // initialClasses.put(emails[i].getMailref()+emails[i].getRCPT(),emails[i].getLabel());
            // resultScores rs = m_mlearner.doLabeling(emails[i]);
            // classifiedClasses.put(emails[i].getMailref()+emails[i].getRCPT(),rs);

            String email = positionMailrefs[i];

            if (!classifiedClasses.containsKey(email)) {
                continue;
            }

            String firstClass = (String) initialClasses.get(email);
            resultScores rs = (resultScores) classifiedClasses.get(email);
            m_messageTable.setValueAt(rs.getLabel(), i, i_labelColumn);
            m_messageTable.setValueAt("" + rs.getScore(), i, i_scoreColumn);
            if (firstClass.equals(rs.getLabel())) {
                m_messageTable.setValueAt("0", i, i_falsepositiveColumn);

                right++;
                if (rs.getLabel().equals(TARGET_CLASS))
                    detection++;
                else
                    tn++;
            } else {
                wrong++;
                if (rs.getLabel().equals(TARGET_CLASS)) {
                    fp++;

                    m_messageTable.setValueAt("1", i, i_falsepositiveColumn);
                } else {
                    wrong2++; // false negative
                    m_messageTable.setValueAt("-1", i, i_falsepositiveColumn);

                }

            }

            int real_class = lookup.indexOf(firstClass);
            int assumed_class = lookup.indexOf(rs.getLabel());

            if (!oneClass)// deosnt make sense with one class.
                if (real_class >= 0 && assumed_class >= 0)
                    confusion[real_class][assumed_class]++;
                else
                    System.out.println("ERROR in CLASS LOOKUP ON CROSS VALIDATION COUNTING" + real_class + " "
                            + assumed_class + " which was " + rs.getLabel());
        }

        report += "Total Number of Instances: " + classifiedClasses.size() + "\n";
        double x = 100 * ((double) right) / classifiedClasses.size();
        report += "Correct/Accuracy:             " + right + "     " + Utils.round(x, 2) + "%" + "\n";
        x = 100 * ((double) wrong) / classifiedClasses.size();
        report += "Incorrectly/Error:              " + wrong + "   " + Utils.round(x, 2) + "%" + "\n";
        x = 100 * (double) detection / totalspam;
        report += "For Target Class of :      " + m_wg.getTarget() + "\n";
        report += "Detection rate:              " + Utils.round(x, 2) + "%\n";
        /* now to do false positive */
        x = 100. * fp / totaln;
        report += "False positive:                 " + Utils.round(x, 2) + "%\n";
        // this is from chi square paper
        x = (double) detection / (double) (detection + wrong2);
        report += "Recall:                 " + Utils.round(x, 2) + "\n";

        x = (double) detection / (double) (detection + fp);
        report += "Precision:            " + Utils.round(x, 2) + "\n";

        x = (double) (fp + wrong2) / (double) (detection + right + fp + wrong2);
        report += "Error:                  " + Utils.round(x, 2) + "\n";

        /*
         * tcr from Androutsopoulos et al papers total spam / (lambda * #reg as
         * spam + #spam ar regular) lamba can be 1 = when just tagged 9 for when
         * email reject 999 (using this) when deleted
         */

        x = totalspam / (fp * lambda + wrong2);
        x = Utils.round(x, 2);
        report += "TCR Total Cost Ratio\nLambda 999 (Delete on detect)\nScore: " + x + "\n";
        report += "\n\n========== Confusion Matrix =============\n";
        for (int i = 0; i < classes.length; i++) {
            String tmps = classes[i];
            while (tmps.length() < 10)
                tmps = tmps + " ";
            report += tmps;
        }

        report += " <--- classified as\n";
        /* i - real class, j - assumed class */
        for (int i = 0; i < classes.length; i++) {
            for (int j = 0; j < classes.length; j++)
            // if (!util(j))
            {
                String tmps = "" + (confusion[i][j]);
                while (tmps.length() < 10)
                    tmps = tmps + " ";
                report += tmps + "";
            }
            report += "|  " + classes[i] + "\n";
        }
        report += "\n\n============= END ===========\n";
        return report;
    }

    // int totalspam,totaln;
    // det detection rate ie is the stuff the system go right, sometimes called
    // precision
    // tp / tp + fp
    // spam which is really spam / total we said is spam

    // fp = fp/fp+tn
    // normal we said is spam / all we said is normal.

    /**
     * This cleans up so that the target class data is some sort of proportion
     * to the general data set.
     * 
     * @param data
     *            is the data to process
     * @param proportion
     *            is the proportion we want the target class to occupy, -1 means
     *            ignore
     * @param numclass
     *            which column has the class label
     * @return the cleaned up data
     */

    public final String[][] doProportion(String data[][], final int proportion, final int numclass) {
        // need to fix the data so its the correct proporion

        if (proportion < 0) {
            return data;
        }

        // first need to count the number of target in the data
        int count_target = 0;
        int n_total = data.length;
        for (int i = 0; i < n_total; i++) {
            if (data[i][numclass].equals(TARGET_CLASS)) {
                count_target++;
            }

        }
        // if we have within 5% its ok
        int p = (100 * count_target) / (n_total);
        // System.out.println("p is : " + p);
        // if no target class or within 3% dont do anything
        if (p == 0 || Math.abs(proportion - p) <= 3)
            return data; // nothing to do

        String data2[][] = null;
        // ok now we need to dleete some records.
        if (proportion > p) // we need to delete non target
        {

            // calc amount to dlete:
            int deletes = proportion - p;
            deletes = (deletes * n_total) / 100;
            // System.out.println("1detlete"+deletes);
            if (deletes < 1)
                return data;

            data2 = new String[data.length - deletes][data[0].length];
            // deletes is number of records not target need to delete.
            for (int i = 0; i < data.length; i++) {
                if (deletes > 0 && !data[i][numclass].equals(TARGET_CLASS)) {
                    deletes--;
                    data[i] = null;
                }
                if (deletes == 0)
                    break;// done
            }

        } else // we need to delete target to lower the proportion.
        {

            // calc amount to dlete:
            int deletes = p - proportion;
            deletes = (deletes * n_total) / 100;
            // System.out.println("detlete"+deletes);
            if (deletes < 1)
                return data;

            data2 = new String[data.length - deletes][data[0].length];
            // deletes is number of records not target need to delete.
            for (int i = 0; i < data.length; i++) {
                if (deletes > 0 && data[i][numclass].equals(TARGET_CLASS)) {
                    deletes--;
                    data[i] = null;
                }
                if (deletes == 0)
                    break;// done
            }

        }

        int h = 0;
        // need to copy to data2
        for (int i = 0; i < data.length; i++)
            if (data[i] != null) {
                for (int j = 0; j < data[i].length; j++)
                    data2[h][j] = data[i][j];
                h++;
            }

        return data2;

    }

    /**
     * This cleans up so that the target class data is some sort of proportion
     * to the general data set.
     * 
     * @param data
     *            is the data to process
     * @param proportion
     *            is the proportion we want the target class to occupy, -1 means
     *            ignore
     * @param numclass
     *            which column has the class label
     * @return the cleaned up data
     */

    public final EMTEmailMessage[] doProportionByEmails(EMTEmailMessage[] data, final int proportion) {
        // need to fix the data so its the correct proporion

        if (proportion < 0) {
            return data;
        }

        // first need to count the number of target in the data
        int count_target = 0;
        int n_total = data.length;
        for (int i = 0; i < n_total; i++) {
            if (data[i].getLabel().equals(TARGET_CLASS)) {
                count_target++;
            }

        }
        // if we have within 5% its ok
        int p = (100 * count_target) / (n_total);
        // System.out.println("p is : " + p);
        // if no target class or within 3% dont do anything
        if (p == 0 || Math.abs(proportion - p) <= 3)
            return data; // nothing to do

        EMTEmailMessage data2[] = null;
        // ok now we need to dleete some records.
        if (proportion > p) // we need to delete non target
        {

            // calc amount to dlete:
            int deletes = proportion - p;
            deletes = (deletes * n_total) / 100;
            // System.out.println("1detlete"+deletes);
            if (deletes < 1)
                return data;

            data2 = new EMTEmailMessage[data.length - deletes];
            // deletes is number of records not target need to delete.
            for (int i = 0; i < data.length; i++) {
                if (deletes > 0 && !data[i].getLabel().equals(TARGET_CLASS)) {
                    deletes--;
                    data[i] = null;
                }
                if (deletes == 0)
                    break;// done
            }

        } else // we need to delete target to lower the proportion.
        {

            // calc amount to dlete:
            int deletes = p - proportion;
            deletes = (deletes * n_total) / 100;
            // System.out.println("detlete"+deletes);
            if (deletes < 1)
                return data;

            data2 = new EMTEmailMessage[data.length - deletes];
            // deletes is number of records not target need to delete.
            for (int i = 0; i < data.length; i++) {
                if (deletes > 0 && data[i].getLabel().equals(TARGET_CLASS)) {
                    deletes--;
                    data[i] = null;
                }
                if (deletes == 0)
                    break;// done
            }

        }

        int h = 0;
        // need to copy to data2
        for (int i = 0; i < data.length; i++)
            if (data[i] != null) {
                data2[h] = data[i];
                h++;
            }

        return data2;

    }

    /**
     * Calculate an ROC curve given a set of initial classes and result classes.
     * 
     * @param oneclass
     * @param initialClass
     * @param resultClass
     * @return
     */

    public JPanel calc_roc_curvePerExample(boolean oneclass, HashMap initialClass, HashMap resultClass) {

        JPanel rocs = new JPanel();
        rocs.setPreferredSize(new Dimension(plotxsize, plotysize * 4 + 20));
        int count = 0;
        int totaln = 0;
        int totalspam = 0;
        // boolean notnb = false;

        // if (type > 1 && type < 11)
        // notnb = true;

        // go through and map each mailref to a ith position
        // for each row positon
        // int value = 0;

        // assume some labels will change:
        update_all_labels = true;
        m_butSaveChanges.setEnabled(true);
        // m_tableModel.setSortingOn(true); //turn back on functionality

        int max = 100;
        int min = 0;
        // int scores[] = new int[resultClass.size()];

        // int i = 0;
        /*
         * for (Iterator it = resultClass.keySet().iterator(); it.hasNext();) {
         * 
         * resultScores rs = (resultScores) resultClass.get(it.next());
         * scores[i] = rs.getScore();
         * 
         * if (scores[i] != Integer.MAX_VALUE && scores[i] > max) { max =
         * scores[i]; } else if (scores[i] < min && scores[i] !=
         * Integer.MIN_VALUE) { min = scores[i]; } }
         */
        System.out.println("min: " + min + " max:" + max);
        // set the threshold here.
        m_mlearner.setThreshold(min); // just to be safe, will adjust later
        count = 0;
        int maxnum = 100;
        // if (maxnum > resultClass.size()) {
        // maxnum = resultClass.size();
        // }
        double a[] = new double[maxnum];// result.length];
        double x[] = new double[maxnum];// result.length];
        double y[] = new double[maxnum];// result.length];
        double z[] = new double[maxnum];// result.length];
        double e[] = new double[maxnum];
        double f[] = new double[maxnum];// fp
        d_thresholds = new double[maxnum][5];
        // double inc = Math.abs(max - min) / 100.0;//incremenet
        double detection, right, wrong, wrong2;
        double tp, tn, fp, fn;
        // int set = 0;
        {
            for (int t = 0; t < 100; t++) {
                // double t = min - inc; t < max && count < maxnum; t += inc) {

                tp = fp = tn = fn = 0;

                detection = 0;

                right = 0;
                wrong = 0;
                wrong2 = 0;
                totalspam = 0;
                totaln = 0;

                for (Iterator it = resultClass.keySet().iterator(); it.hasNext();) {

                    // threshold

                    String email = (String) it.next();
                    resultScores rs = (resultScores) resultClass.get(email);
                    String initialLabel = (String) initialClass.get(email);
                    int dScore = rs.getScore();
                    if (initialLabel.equals(rs.getLabel())) // correct
                    // result
                    {

                        if (rs.getLabel().equals(TARGET_CLASS)) {
                            if (dScore >= t) // if not above threshold will
                            // change
                            // our guess
                            {
                                tp++;// spam as spam
                                totalspam++;
                                right++;
                                detection++;
                            } else {
                                fn++;
                                totaln++;
                                wrong++;
                                wrong2++;
                            }
                        } else {
                            tn++;// legit as legit
                            totaln++;
                            right++;
                        }
                    } else // misclassify
                    {
                        wrong++;
                        if (rs.getLabel().equals(TARGET_CLASS) && dScore >= t) {
                            fp++;
                            totalspam++;
                        } else {
                            fn++;
                            totaln++;
                            wrong2++; // false negative
                        }
                    }

                }// endt hrough all examples
                a[count] = Utils.round(right / resultClass.size(), 4);
                z[count] = t;// Utils.round(t, 4);
                x[count] = Utils.round(100 * detection / (totalspam), 4);// detection;
                e[count] = Utils.round(wrong2 / resultClass.size(), 4);
                f[count] = Utils.round(100. * fp / resultClass.size(), 4);
                if (oneclass)
                    y[count] = Utils.round(100 * fn / totalspam, 4);// fp;
                else
                    y[count] = Utils.round(100 * fp / (totaln), 4);// fp;
                count++;
            }// end of for each threhsold

            // for(int i=0;i<x.length;i++)
            // System.out.println(x[i] + " "+y[i] + " "+z[i]);
            System.out.println("plotting");
            // now to show a graphic with the stuff.
            JPlot2D roc_curve = new JPlot2D();
            roc_curve.setPlotType(JPlot2D.LINEAR);
            roc_curve.setPreferredSize(new Dimension(plotxsize, plotysize));
            roc_curve.setBackgroundColor(Color.white);
            roc_curve.addCurve(y, x);
            // System.out.println("adding first curve");
            roc_curve.setLineWidth(4 * roc_curve.getLineWidth());
            roc_curve.setLineColor(Color.blue);
            roc_curve.setGridState(JPlot2D.GRID_ON);
            roc_curve.setLineState(JPlot2D.LINE_ON);
            roc_curve.setLineStyle(JPlot2D.LINESTYLE_DOT);
            roc_curve.setTitle("ROC Threshold from + " + Utils.round(min, 4) + " to " + Utils.round(max, 4));
            if (oneclass)
                roc_curve.setXLabel(TARGET_CLASS + ": Percent error rate");
            else
                roc_curve.setXLabel(TARGET_CLASS + ": Percent FP rate");
            roc_curve.setYLabel(TARGET_CLASS + ": Percent Detection rate");
            // roc_curve.setLineColor(Color.black);
            rocs.add(roc_curve);
            JPlot2D tp_curve = new JPlot2D();
            tp_curve.setPlotType(JPlot2D.LINEAR);
            tp_curve.setPreferredSize(new Dimension(plotxsize, plotysize));
            tp_curve.setBackgroundColor(Color.white);

            tp_curve.addCurve(z, x);
            tp_curve.setLineWidth(4 * tp_curve.getLineWidth());
            tp_curve.setLineColor(Color.blue);
            tp_curve.setGridState(JPlot2D.GRID_ON);
            tp_curve.setLineState(JPlot2D.LINE_ON);
            tp_curve.setLineStyle(JPlot2D.LINESTYLE_DOT);
            tp_curve.setTitle("Detection Threshold from + " + Utils.round(min, 4) + " to " + Utils.round(max, 4));
            // if(oneclass)
            // tp_curve.setXLabel(TARGET_CLASS + ": Percent error rate");
            // else
            tp_curve.setXLabel(TARGET_CLASS + ": for each threshold");
            tp_curve.setYLabel(TARGET_CLASS + ": Percent Detection rate");
            rocs.add(tp_curve);
            // System.out.println("33");
            // fp curve
            JPlot2D fp_curve = new JPlot2D();
            fp_curve.setPlotType(JPlot2D.LINEAR);
            fp_curve.setPreferredSize(new Dimension(plotxsize, plotysize));
            fp_curve.setBackgroundColor(Color.white);
            fp_curve.addCurve(z, y);
            fp_curve.setLineWidth(4 * fp_curve.getLineWidth());
            fp_curve.setLineColor(Color.blue);
            fp_curve.setGridState(JPlot2D.GRID_ON);
            fp_curve.setLineState(JPlot2D.LINE_ON);
            fp_curve.setLineStyle(JPlot2D.LINESTYLE_DOT);
            fp_curve.setTitle("FP rate using threshold from + " + Utils.round(min, 4) + " to " + Utils.round(max, 4));
            // if(oneclass)

            // tp_curve.setXLabel(TARGET_CLASS + ": Percent error rate");
            // else
            fp_curve.setXLabel(TARGET_CLASS + ": Each threshold");
            fp_curve.setYLabel(TARGET_CLASS + ": Percent fp rate");
            rocs.add(fp_curve);
            // accuracy count

            JPlot2D accuracy_curve = new JPlot2D();
            accuracy_curve.setPlotType(JPlot2D.LINEAR);
            accuracy_curve.setPreferredSize(new Dimension(plotxsize, plotysize));
            accuracy_curve.setBackgroundColor(Color.white);
            accuracy_curve.addCurve(z, a);
            accuracy_curve.setLineWidth(4 * accuracy_curve.getLineWidth());
            accuracy_curve.setLineColor(Color.blue);
            accuracy_curve.setGridState(JPlot2D.GRID_ON);
            accuracy_curve.setLineState(JPlot2D.LINE_ON);
            accuracy_curve.setLineStyle(JPlot2D.LINESTYLE_DOT);
            accuracy_curve.setTitle("Accuracy (total correct) using threshold from + " + Utils.round(min, 4) + " to "
                    + Utils.round(max, 4));
            // if(oneclass)
            // tp_curve.setXLabel(TARGET_CLASS + ": Percent error rate");
            // else
            accuracy_curve.setXLabel(TARGET_CLASS + ": Each threshold");
            accuracy_curve.setYLabel(TARGET_CLASS + ": Percent accuracy rate");
            rocs.add(accuracy_curve);

            for (int j = 0; j < maxnum; j++) {
                d_thresholds[j][0] = z[j];
                d_thresholds[j][1] = x[j];
                d_thresholds[j][2] = y[j];
                d_thresholds[j][3] = a[j];
                d_thresholds[j][4] = f[j];
            }
        }
        return rocs;
        // JScrollPane areaScrollPane = new JScrollPane(rocs);
        // areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // areaScrollPane.setPreferredSize(new Dimension(380,550 ));

        // return areaScrollPane;
    }// end roc

    // time spam experiment stuff

    /**
     * 
     * will setup timespam data set models based on current setup, will need to
     * set model name, then condition for training. trainCondition.setText("");
     * ModelNames.setText("");
     * 
     * @param model
     *            the type of model
     */
    /*
     * public final void TimeSpam(final int model) { model_num = model; /** will
     * cycle through yrs * / String current = new String(); /** will accumulate
     * yr list * / String total = new String(""); String mname = new String("");
     * //BusyWindow bw = new BusyWindow(this,"Timespam","Experiment in //
     * progress"); bw2.progress(1, 51); bw2.setVisible(true); int count = 0; int
     * yr = 1999; int month = 0; m_timespam = true; m_timespam_report = new
     * String(); while (bw2.isAlive()) { bw2.progress(count++);//,51);
     * 
     * //this is wehre we stop if (yr == 2003 && month == 2) break;
     * 
     * month++; if (month > 12) { yr++; month = 1; } if (month < 10) current =
     * "0" + month + "." + yr; else current = month + "." + yr;
     * 
     * if (total.equals("")) total = "'" + current + "'"; else total = total +
     * ",'" + current + "'";
     * 
     * //if ngram need to save ngram len if (model_num < 2)//so we are doing
     * ngram..need leng mname = "models" + System.getProperty("file.separator") +
     * "Model-type" + model_num + "-" + m_wg.getNGRAM() + "_" + current.trim() +
     * ".mod"; else mname = "models" + System.getProperty("file.separator") +
     * "Model-type" + model_num + "_" + current.trim() + ".mod";
     * 
     * //ModelNames.setText(mname); //trainCondition.setText("e.folder = '" +
     * current.trim()+"' or // e.folder like '%yu%'"); //TODO: fix this to make
     * this work..should move all this outside //-TrainClassifier("e.folder = '" +
     * current.trim() + "' or e.folder like '%yu%' and class = 's'", m_wg //-
     * .getFeatures() //- + ",e.class", mname, 1, 100, -1, model_num); //now
     * that we have a model lets run it over all the data time(mname, current); }
     * bw2.setVisible(false);//bw2.hide(); //now to file timespam report try {
     * 
     * PrintWriter ps = new PrintWriter((new FileOutputStream("timespam_model_" +
     * model_num + "_report_file.txt"))); ps.print(m_timespam_report);
     * ps.close(); } catch (Exception fout) { System.out.println("problem making
     * report: " + fout); } /** clean up the string * / m_timespam_report = "";
     * /** reset the in timespam flag * / m_timespam = false;
     * 
     * JOptionPane.showMessageDialog(this, "done building the timespam models \n
     * have saved to disk\nAnd created timespam_model_" + model_num +
     * "_report_file.txt"); }
     */
    /**
     * This will allow us to run the time ordered experiments from teh models
     * buil in the previous window
     * <P>
     * will grab models and run over all the months (so we get forward and
     * backwards).
     * 
     * @param modelname
     *            is the name of the model to run our tests against. assuming a
     *            model has already been built and tested
     * 
     * @param currenttime
     *            is the string of the current month we are testing.
     */
    /*
     * public final void time(String modelname, String currenttime) {
     * 
     * //m_mlearner =goLoadModel(modelname); //m_mlearner.setProgress(false);
     * //at this point m_mlearner is not null if (m_mlearner == null) {
     * JOptionPane.showMessageDialog(this, "WARNING: no model loaded"); return; }
     * /** will cycle through yrs * / String current = new String(""); /** will
     * accumulate yr list * / String total = new String(""); //BusyWindow bw =
     * new BusyWindow(this,"Timespam","Full Time Test for // "+modelname+" in
     * progress"); bw3.setMSG("Full Time Test for " + modelname + " in
     * progress"); bw3.setMax(51); bw3.progress(1); bw3.setVisible(true); int
     * count = 0; int yr = 1999; int month = 0; boolean start = false; String
     * result[][]; String sdata[][]; int right = 0, wrong2 = 0, wrong = 0,
     * detection = 0, fp = 0; //int scount,ncount;
     * 
     * /** will fetch the normal data to work with * / String cond = "e.folder
     * like '%hostbased%'"; boolean haveBody = false; int numfields, numclass =
     * 0; String query = new String(); String feats = m_wg.getFeatures() +
     * ",e.class"; StringTokenizer tok = new StringTokenizer(feats, ","); Vector
     * tvec = new Vector();//temp vec int nc, rn, rs; //temp variabels to keep
     * track of normal stuff
     * 
     * try {
     * 
     * if (model_num < 2) { haveBody = false;//will reset if have body while
     * (tok.hasMoreTokens()) {
     * tvec.addElement(tok.nextToken().toLowerCase().trim()); } if
     * (!tvec.contains("e.class")) tvec.addElement("e.class");
     * 
     * for (int i = 0; i < tvec.size(); i++) { if (((String)
     * tvec.elementAt(i)).equals("e.class")) { numclass = i; } }
     * 
     * if (tvec.contains("m.body")) haveBody = true;
     * 
     * numfields = tvec.size(); if (numfields < 1) throw new Exception("Too
     * little fields");
     * 
     * if (haveBody) query = new String("select " + feats + ",e.mailref from
     * email as e left join message as m on m.mailref=e.mailref where " + cond + "
     * group by e.mailref,e.rcpt ");//"select " // +fields+ // " from // email //
     * where UID // in " + // uids); else query = new String("select " + feats +
     * ",e.mailref from email as e where " + cond + " group by
     * e.mailref,e.rcpt"); } else { haveBody = true; // body_column =0;
     * 
     * feats = "m.body,e.class,e.subject"; query = new String("select " + feats +
     * ",e.mailref from email as e left join message as m on m.mailref=e.mailref
     * where (" + cond + ") group by e.mailref,e.rcpt"); numclass = 1; }
     * 
     * synchronized (m_jdbcView) { sdata = m_jdbcView.getSQLData(query); //sdata =
     * m_jdbcView.getRowData(); } //need to clean up class label for (int i = 0;
     * i < sdata.length; i++) sdata[i][numclass] =
     * sdata[i][numclass].toLowerCase().trim(); //check data len if
     * (sdata.length == 0) { JOptionPane.showMessageDialog(this, "No normal data
     * fetched!"); return; }
     * 
     * /** end normal data fetching * / /** will evaualte over ndata * /
     * 
     * nc = rn = rs = 0; result = m_mlearner.classify(sdata); sdata = null; nc =
     * result.length; for (int i = 0; i < result.length; i++) { if
     * (result[i][0].equals("s") && m_threshold <
     * Double.parseDouble(result[i][1])) rs++; else rn++; } } catch (Exception
     * c) { JOptionPane.showMessageDialog(this, "Exception during normal data " +
     * c); return; }
     * 
     * while (bw3.isAlive()) { bw3.progress(count++);//,51);
     * 
     * //this is wehre we stop if (yr == 2003 && month == 2) break;
     * 
     * month++; if (month > 12) { yr++; month = 1; } if (month < 10) current =
     * "0" + month + "." + yr; else current = month + "." + yr;
     * 
     * if (current.equals(currenttime)) { start = true; total = "'" + current +
     * "'";//will seed with the first } else if (start) { //
     * if(total.length()>4) //total = total +",'"+ current+"'"; //this will make
     * the // current -> end list //else total = "'" + current + "'";//will seed
     * with the first }
     * 
     * if (start) { //need condition if total = "" then use current. try { /**
     * will get sdata * / if (model_num < 2) {
     * 
     * if (haveBody) query = new String( "select " + feats + ",e.mailref from
     * email as e left join message as m on m.mailref=e.mailref where e.folder
     * in (" + total + ") group by e.mailref,e.rcpt ");//"select // " //
     * +fields+ // " // from // email // where // UID // in " // + // uids);
     * else query = new String("select " + feats + ",e.mailref from email as e
     * where e.folder in (" + total + ") group by e.mailref,e.rcpt"); } else {
     * 
     * feats = "m.body,e.class,e.subject"; query = new String( "select " + feats +
     * ",e.mailref from email as e left join message as m on m.mailref=e.mailref
     * where e.folder in (" + total + ") group by e.mailref,e.rcpt"); }
     * 
     * //System.out.print(count + " ");
     * 
     * synchronized (m_jdbcView) { sdata = m_jdbcView.getSQLData(query); //sdata =
     * m_jdbcView.getRowData(); } if (sdata.length == 0) {
     * JOptionPane.showMessageDialog(this, "No spam data fetched!"); return; }
     * result = null; result = m_mlearner.classify(sdata); sdata = null;
     * 
     * /** will evaluate over sdata * / right = rn;//from normal data stuff
     * wrong = rs; fp = rs; detection = 0; wrong2 = 0; /** will write down score * /
     * for (int i = 0; i < result.length; i++) {
     * 
     * if (result[i][0].equals("s") && m_threshold <
     * Double.parseDouble(result[i][1])) { right++; detection++; } else {
     * wrong++; wrong2++; } }
     * 
     * //now to compute accuracy m_timespam_report += "\n" + count + "\n";
     * m_timespam_report += (nc + result.length); double x = 100 * ((double)
     * right) / (nc + result.length);
     * 
     * x = Utils.round(x, 2); m_timespam_report += "Correct/Accuracy: " + right + " " +
     * x + "%" + "\n"; x = 100 * ((double) wrong) / (nc + result.length); x =
     * Utils.round(x, 2); m_timespam_report += "Incorret/Err: " + wrong + " " +
     * x + "%" + "\n"; /* now to do false positive * / x = 100 * (double)
     * detection / result.length; x = Utils.round(x, 2); m_timespam_report +=
     * "Detection rate: " + x + "%\n"; x = 100 * (double) fp / nc; x =
     * Utils.round(x, 2); m_timespam_report += "False positive: " + x + "%\n"; /*
     * tcr from Androutsopoulos et al papers total spam / (lambda * #reg as spam +
     * #spam ar regular) lamba can be 1 = when just tagged 9 for when email
     * reject 999 when deleted / x = result.length / (rs * lambda + wrong2); x =
     * Utils.round(x, 2); m_timespam_report += "TCR Score: " + x + "\n"; } catch
     * (Exception c) { JOptionPane.showMessageDialog(this, "Exception during
     * spam data " + c); return; }
     * 
     * /** might need to check if total == current if yes erase * /
     * 
     * if (total.equals(",'" + current + "'")) total = "";
     * 
     * }//end timespam experiments } bw3.setVisible(false);//bw3.hide(); //now
     * to file timespam report // JOptionPane.showMessageDialog(this, "done
     * building the timespam // models \n have saved to disk\nAnd created //
     * timespam_model_"+comboBox.getSelectedIndex()+"_report_file.txt"); }
     */

    /**
     * returns the current user of interest. this is defined by either user list
     * or current clikced email's sender
     */
    public final String getMsgTabUser() {
        if (m_view_user.getSelectedIndex() == 0 && selected_user == null)
            return null;

        if (m_view_user.getSelectedIndex() < 1) // so no ser is specified by
        { // the drop down list.
            // if( m_selected_user != null)
            return selected_user;
        }
        return (String) m_wg.getSelectedUser();
        // ---(String) m_view_user.getSelectedItem(); //the user on
        // the drop down
        // list
        // return (String)m_userlist.get(m_view_user.getSelectedIndex()-1);
        // //the user on the drop down list
        // reason for selecting not from m_view_user is that this list is
        // truncated.

    }

    /** moveBack allows navigation to last screen */
    private void moveBack() {
        String s = null;
        if (!m_backStack.empty())
            s = m_backStack.pop();

        if (m_backStack.empty()) {
            back_but.setEnabled(false);

        }

        if (s != null) {
            try {
                addForward(s);
                startingsearch = true;
                cancelSearchButton.setEnabled(false);
                searchArea.setText("");
                Refresh(true, true, false, s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MessageWindow.this, "oops" + e, "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** will display current sql setup and allow user to tweak it */

    private void showSQL() {

        String top = new String("This is the sql which will be executed:");
        String test = new String(m_sql_exp);

        test = JOptionPane.showInputDialog(MessageWindow.this, top, test);

        // System.out.println("test is now: "+test);
        if (test != null) {
            try {
                // ???addBack(test);//need to check top
                Refresh(true, true, false, test);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MessageWindow.this, "oops" + e, "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /** allows to move forward */
    private void moveForward() {

        String s = null;
        if (!m_forwardStack.empty())
            s = (String) m_forwardStack.pop();

        if (m_forwardStack.empty()) {
            for_but.setEnabled(false);
        }

        if (s != null) {
            try {
                // ?addBack(s);
            	startingsearch = true;
                Refresh(true, true, false, s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MessageWindow.this, "oops" + e, "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** will add the current sql string to the stack */
    private void addBack(String s) {

        String test = null;
        if (!m_backStack.empty()) {
            test = (String) m_backStack.peek();
            if (!test.equals(s)) {
                m_backStack.push(s);
            }
        } else
            m_backStack.push(s);

        back_but.setEnabled(true);
        back_but.repaint();
    }

    /** will add current sql string to forward stack */
    private void addForward(String s) {
        String test = null;

        if (!m_forwardStack.empty()) {
            test = (String) m_forwardStack.peek();
            if (!test.equals(s)) {
                m_forwardStack.push(s);
            }
        } else
            m_forwardStack.push(s);

        for_but.setEnabled(true);
        for_but.repaint();
    }

    /** add users to the drop down box */
    /*
     * private void addUsers() {
     * 
     * userChanged = false;//reset int c = m_view_user.getSelectedIndex(); int d =
     * m_view_direction.getSelectedIndex(); //we can refresh it every time we
     * refresh m_view_user.removeAllItems(); //m_view_user.addItem("All Users");
     * ComboBoxModel model = new DefaultComboBoxModel(m_wg.getUserList());
     * m_view_user.setModel(model);
     * 
     * m_view_user.insertItemAt("All Users", 0);
     * m_view_user.setSelectedIndex(0); if (c != -1 &&
     * m_view_user.getItemCount() >= c) m_view_user.setSelectedIndex(c);
     * 
     * if (d != -1 && m_view_direction.getItemCount() >= d)
     * m_view_direction.setSelectedIndex(d);
     *  }
     */

    /**
     * Ruitine to save the labels in the window which have been modified. for
     * speedup we just do them all.
     * 
     */
    private final void saveLabelChanges() {

        // turn it off while we work on it
        m_butSaveChanges.setEnabled(false);

        int c = 0;

        // first to see if we have just done a model update
        // if yes then that takes precedence over other data
        if (update_all_labels == true) {
            // int la, ma, sc;
            // sc = m_messageTable.getColumn("Score").getModelIndex();
            // la = m_messageTable.getColumn("Label").getModelIndex();
            // ma = m_messageTable.getColumn("Mailref").getModelIndex();
            // r = m_messageTable.getColumn("Recipient").getModelIndex();

            int rows = m_messageTable.getRowCount();
            // ?????
            synchronized (m_jdbcView) {
                PreparedStatement ps;

                try {
                    ps = m_jdbcView.prepareStatementHelper("update email set class=?, score =? where mailref=?");
                } catch (SQLException e) {
                    System.out.println(e);
                    return;
                }
                m_jdbcView.setAutocommit(false);
                for (int i = 0; i < rows; i++) {

                    String sub = (String) m_messageTable.getValueAt(i, i_labelColumn);
                    sub = sub.substring(0, 1).toLowerCase();// sub.indexOf('_'));

                    try {
                        ps.setString(1, sub);
                        ps.setShort(2, ((Integer) m_messageTable.getValueAt(i, i_scoreColumn)).shortValue());
                        ps.setString(3, ((String) m_messageTable.getValueAt(i, i_mailrefColumn)));
                        ps.execute();
                        c++;
                    } catch (Exception e3) {
                        System.out.println(e3);

                    }

                }
                m_jdbcView.setAutocommit(true);
            }
        }

        else
            // will loop through SelectedData for each uid need to refresh table
            // with new class
            while (SelectedData.size() > 0) {
                synchronized (m_jdbcView) {
                    String mailref = (String) SelectedData.elementAt(0);
                    // String rcpt = (String) SelectedData.elementAt(1);
                    String interest = (String) SelectedData.elementAt(2);
                    SelectedData.removeElementAt(0);
                    SelectedData.removeElementAt(0);
                    SelectedData.removeElementAt(0);
                    try {
                        // System.out.println("update email set class='" +
                        // interest + "' where mailref='" + mailref + "' and
                        // rcpt='" + rcpt + "'" );
                        m_jdbcView.updateSQLData("update email set class='" + interest + "' where mailref='" + mailref
                                + "'");// and rcpt = '" + rcpt + "'"
                        // );
                        c++;
                    } catch (Exception e3) {
                        JOptionPane.showMessageDialog(MessageWindow.this, e3, "Exception thrown",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        // now to show something to the user:
        update_all_labels = false;
        // m_sql_exp = "";//this will call refresh and actually do someting if
        // pressed.
        force_refresh = true;
        JOptionPane.showMessageDialog(MessageWindow.this, "Number of records updated to the DB: " + c);
    }

    private void addFolders() {
    	System.out.println("in add folders");
    	long start = System.currentTimeMillis();
        folderChanged = false;// reset
        userPlotHash.clear(); // cleared in case of new data
        // TODO: make same for drop list folder
        // int c = m_view_folder.getSelectedIndex();
        // String oldfolder = (String) m_view_folder.getSelectedItem();
        // we can refresh it every time we refresh

        // m_view_folder.removeAllItems();

        roottree.removeAllChildren();
       

        String data[][] = null;
        try {
           // synchronized (m_jdbcView) {
                data = m_jdbcView.getSQLData("select folder,count(*) from email group by folder");
            //}
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error getting details from database for folder names: " + ex);
        }
        System.out.println("Fetched add folder data, will build tree" );
        System.out.println((System.currentTimeMillis() - start)/1000.0 +" seconds");
        if(data == null){
        	return;
        }
        
        HashMap<String,DefaultMutableTreeNode> treelocate = new HashMap<String,DefaultMutableTreeNode>();
        
        DefaultMutableTreeNode child = null;
        for (int i = 0; i < data.length; i++) {

            StringTokenizer stok = new StringTokenizer(data[i][0], "/\\", true);
            String current_name;
            child = roottree;
            // idea is to take care of 2 types of cases:
            // 1 - c:\shlomo which is c: , \, shlomo
            // 2 - shlomo which is just shlomo
            // we will recursivly build 2 tokens (so that we know how the
            // seperator looks like (\ or /) and use it
            // as node names
            int level  = 0;
            while (stok.hasMoreTokens()) {
            	level++;
                boolean makeNextLevel = true; // if we need to build down the
                                                // tree path

                current_name = stok.nextToken();

                // check for second
                if (stok.hasMoreTokens()) {
                    current_name += stok.nextToken();
                }

                //first we wil try to locate it in our hashmap dictionary
                
                if(treelocate.containsKey(level+current_name)){
                	
                	child = treelocate.get(level+current_name);
                	makeNextLevel = false;
                }
                
                // need to see if the next level has been seen in the tree yet
                if (makeNextLevel) {
                    DefaultMutableTreeNode dftn;
                    if (!stok.hasMoreTokens()) {
                        dftn = new DefaultMutableTreeNode(current_name + " (" + data[i][1] + ")");
                    } else {
                        dftn = new DefaultMutableTreeNode(current_name);
                    }
                    
                    child.add(dftn);
                    child = dftn;
                    
                    treelocate.put(level+current_name,child);
                }

            }

        }
        folderListTree.repaint();
        
        System.out.println("done add folder" + (System.currentTimeMillis() - start)/1000.0 +" seconds");
    }

    // ItemListener interface
    public final void itemStateChanged(ItemEvent evt) {

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            classifySelectedMessages((String) evt.getItem());
        }
    }

    public void ExtractLinksFromMessages() {
        // make sure table doesnt shift
        m_tableModel.setSortingOn(false);
        StringBuffer collectedBodyData = new StringBuffer(4096);
        int[] rows = m_messageTable.getSelectedRows();
        int cnt = 0;
        int len = rows.length;
        // collect unqiue mailrefs
        BusyWindow bw = new BusyWindow("Link wizard", "Progress",true);
        Hashtable<String,String> mailrefs = new Hashtable<String,String>();

        for (int i = 0; i < len; i++) {
            mailrefs.put((String)m_messageTable.getValueAt(rows[i], i_mailrefColumn), "");
        }

        len = mailrefs.size();
        bw.progress(cnt++, len);
        // foreach mailref collec the body
        synchronized (m_jdbcView) {
            for (Enumeration itEnumerat = mailrefs.keys(); itEnumerat.hasMoreElements();) {
                String bd[] = m_jdbcView.getBodyByMailref((String) itEnumerat.nextElement());
                for (int i = 0; i < bd.length; i++) {
                    collectedBodyData.append(" " + bd[i]);
                }
            }
            bw.progress(cnt++);
        }
        if (isStop) {
            collectedBodyData = new StringBuffer(Utils.replaceStop(collectedBodyData.toString(), m_wg.getHash()));
        }

        LinkInfoDS[] links = LinkExtractor.extractAllLinksFromData(collectedBodyData.toString());

        JTextArea textArea = new JTextArea();
        StringBuffer linksText = new StringBuffer();
        for (int i = 0; i < links.length; i++) {
            linksText.append(links[i] + "\n");
        }
        textArea.setText(linksText.toString());

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(550, 380));

        JOptionPane.showMessageDialog(MessageWindow.this, areaScrollPane);
        // KeywordWizard kwiz = new KeywordWizard(wordcollection,
        // emtConfigHandle.getProperty(EMTConfiguration.WORDLIST),
        // isLower);

        // String msgs = new String("Keyword Wizard");
        // Object stuff[] = {msgs, kwiz};

        // int c = JOptionPane.showConfirmDialog(Messages.this, stuff, "Link
        // Extractor", JOptionPane.OK_CANCEL_OPTION,
        // JOptionPane.INFORMATION_MESSAGE);
        // if (c == JOptionPane.CANCEL_OPTION) {
        // System.out.println("you just hit cancel on wizard");
        // }

        m_tableModel.setSortingOn(true);
    }

    /**
     * Wizard to run the keyword extractions to help use decide on keywords
     * 
     */
    public void ExtractKeywordsFromMessages() {
        m_tableModel.setSortingOn(false);
        StringBuffer wordcollection = new StringBuffer(4096);
        int[] rows = m_messageTable.getSelectedRows();
        int cnt = 0;
        int len = rows.length;
        // collect unqiue mailrefs
        BusyWindow bw = new BusyWindow("Keyword wizard", "Progress",true);
        Hashtable<String,String> mailrefs = new Hashtable<String,String>();

        for (int i = 0; i < len; i++) {
            mailrefs.put((String)m_messageTable.getValueAt(rows[i], i_mailrefColumn), "");
        }

        len = mailrefs.size();
        bw.progress(cnt++, len);
        // foreach mailref collec the body
        synchronized (m_jdbcView) {
            for (Enumeration<String> itEnumerat = mailrefs.keys(); itEnumerat.hasMoreElements();) {
                String bd[] = m_jdbcView.getBodyByMailref(itEnumerat.nextElement());
                for (int i = 0; i < bd.length; i++) {
                    wordcollection.append(" " + bd[i]);
                }
            }
            bw.progress(cnt++);
        }
        if (isStop) {
            wordcollection = new StringBuffer(Utils.replaceStop(wordcollection.toString(), m_wg.getHash()));
        }
        KeywordWizard kwiz = new KeywordWizard(wordcollection, emtConfigHandle.getProperty(EMTConfiguration.WORDLIST),
                isLower);

        String msgs = new String("Keyword Wizard");
        Object stuff[] = { msgs, kwiz };

        int c = JOptionPane.showConfirmDialog(MessageWindow.this, stuff, "Setup Keywords",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (c == JOptionPane.CANCEL_OPTION) {
            System.out.println("you just hit cancel on wizard");
        }

        m_tableModel.setSortingOn(true);

    }

    /** move selected messages to some destination folder */
    private final void moveSelectedMessages() {

        m_tableModel.setSortingOn(false);
        int i = 0;
        int[] rows = m_messageTable.getSelectedRows();
        String mailref = new String("");
        String oldmailref = new String("");
        String data[][] = new String[0][0];
        try {
            synchronized (m_jdbcView) {
                data = m_jdbcView.getSQLData("select distinct folder from email");
                // data = m_jdbcView.getRowData();
            }
        } catch (SQLException s) {
            JOptionPane.showMessageDialog(this, "Error getting folder details from database: " + s, "Problem folders",
                    JOptionPane.ERROR_MESSAGE);

        }
        String[] data2 = new String[data.length + 1];
        data2[0] = new String("CREATE NEW FOLDER");

        for (i = 0; i < data.length; i++)
            data2[i + 1] = data[i][0];

        // Object[] possibilities = data2;
        String choose = (String) JOptionPane.showInputDialog(this, "Choose a Target Folder to move " + rows.length
                + " selected items:\n" + "Or choose NEW to create new one...", "Move Messages",
                JOptionPane.PLAIN_MESSAGE, null, data2, data2[0]);

        if (choose != null)
            if (choose.equals("CREATE NEW FOLDER")) {
                folderChanged = true;

                choose = (String) JOptionPane.showInputDialog(this,
                        "Please choose a target Folder name to move selected items:\n", "Move Messages",
                        JOptionPane.PLAIN_MESSAGE, null, null, " type new folder");
            }

        if (choose != null) // when enw folder and just click ok
        {
            synchronized (m_jdbcView) {
                m_jdbcView.setAutocommit(false);
                for (i = 0; i < rows.length; i++) {
                    mailref = (String) m_messageTable.getValueAt(rows[i], i_mailrefColumn);
                    // --m_messageTable.getColumn("Mailref")
                    // --.getModelIndex());
                    // we have the mailref
                    // compare to old
                    if (!mailref.equals(oldmailref)) {
                        try {
                            m_jdbcView.updateSQLData("update email set folder='" + choose + "' where mailref='"
                                    + mailref + "'");
                        } catch (Exception e3) {
                            System.out.println(e3);

                        }
                        // now to move it out of the table

                    }
                    oldmailref = mailref;// so no repeats.

                }
                m_jdbcView.setAutocommit(true);
            }
            // second loop to remove
            for (i = 0; i < rows.length; i++)
                m_tableModel.removeRow(rows[i] - i); // need to SUBTRACT
                                                        // cause
            // they are getting out of
            // the table so will be
            // deleted.

        }

        // if update folder
        if (folderChanged) {
            addFolders();
        }

        m_tableModel.setSortingOn(true);
        m_tableModel.sortByLastSortColumn();

    }// end move messages

    /** basic function to print all selected messages. */
    private void printSelectedMessages(boolean really_print) {
        // ERROR PATH RESET IN PRINTER OPTION.

        // update_all_labels = true;//need to save later
        m_tableModel.setSortingOn(false);
        int[] rows = m_messageTable.getSelectedRows();
        String report = new String();
        for (int i = 0; i < rows.length; i++) {// hard coded to get speed
            // m_messageTable.getColumn("Mailref").getModelIndex()

            report += "Mailref: " + (String) m_messageTable.getValueAt(rows[i], i_mailrefColumn) + "\n";
            report += "Sender: " + ((String) m_messageTable.getValueAt(i, i_senderColumn)) + "\n";
            report += "Date: " + (String) m_messageTable.getValueAt(i, i_dateColumn) + "\n";
            report += "Subject: " + (String) m_messageTable.getValueAt(i, i_subjectColumn) + "\n";

            report += Utils.getBody((String) m_messageTable.getValueAt(rows[i], i_mailrefColumn), false, m_jdbcView);
            // m_messageTable.setValueAt(interest, rows[i], 9);
        }
        // SelectedData.add(m_messageTable.getValueAt(rows[i],
        // m_messageTable.getColumn("Mailref").getModelIndex()));
        // JTextArea jt = new JTextArea("Printing report\n");
        // jt.setLineWrap(true);
        // jt.setWrapStyleWord(true);
        // jt.setText("\n" + report + "\f");

        if (really_print) {
            System.out.println("before: " + System.getProperty("user.home"));
            System.out.println("before: " + System.getProperty("user.dir"));
            Component c2 = this.getParent();
            while (c2 != null && !(c2 instanceof Frame))
                c2 = c2.getParent();

            PrintJob pjob = c2.getToolkit().getPrintJob((Frame) c2, "Message Printing", null);
            // PageAttributes pa = new PageAttributes();
            // pa.setOrientationRequested(4);
            // new JobAttributes(),pa );//landscape 3 is normal);
            // null means cancel
            if (pjob != null) {
                Graphics pg = pjob.getGraphics();

                if (pg != null) {
                    Utils.printLongString(pjob, pg, report);
                    pg.dispose();
                }
                pjob.end();
                System.out.println("after: " + System.getProperty("user.home"));
                System.out.println("after: " + System.getProperty("user.dir"));
            }
        } else {
            StringSelection data2 = new StringSelection(report);
            // for copying to clip
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            clipboard.setContents(data2, data2);
        }
        /*
         * if(pjob!=null) { //will pritnmulitple pages. Graphics
         * pg=pjob.getGraphics(); if( pg != null) { // printing the text
         * component jt.printAll(pg); pg.dispose(); // flush page }
         * pg=pjob.getGraphics(); int i=3; if( pg=pjob.getGraphics(); pg != null &&
         * i <3; i++,pg=pjob.getGraphics()) { // printing the text component
         * m_messageTable.printAll(pg); pg.dispose(); // flush page }
         * 
         * pjob.end(); }
         */
        // RepaintManager currentManager =
        // RepaintManager.currentManager(c);
        // currentManager.setDoubleBufferingEnabled(false);
        m_tableModel.setSortingOn(true);
        m_tableModel.sortByLastSortColumn();
    }

    private void classifySelectedMessages(String interest) {

        update_all_labels = true;// need to save later
        m_tableModel.setSortingOn(false);
        int[] rows = m_messageTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            m_messageTable.setValueAt(interest, rows[i], i_labelColumn);
            // hard coded
            // m_messageTable.getColumn("Mailref").getModelIndex()
            SelectedData.addElement((String)m_messageTable.getValueAt(rows[i], i_mailrefColumn));
            if (m_view.getSelectedIndex() == 0) {// hard coded to get speed
                // m_messageTable.getColumn("Recipient").getModelIndex()
                SelectedData.addElement((String)m_messageTable.getValueAt(rows[i], i_rcptColumn));
            } else {
                SelectedData.add("%");// becuase want to match mutiple rcpts
            }

            if (interest.startsWith("Unknown")) {
                SelectedData.add("u");
            } else if (interest.startsWith("Interesting")) {
                SelectedData.add("i");
            } else if (interest.startsWith("Spam")) {
                SelectedData.add("s");
            } else if (interest.startsWith("Virus")) {
                SelectedData.add("v");
            } else {
                SelectedData.add("n");
            }
        }

        m_tableModel.setSortingOn(true);
        m_tableModel.sortByLastSortColumn();
        m_butSaveChanges.setEnabled(true);
    }

    /**
     * @param row
     *            which row is being highlighed to display
     */
    private void updateMSGtextWindow(final int row) {

    	//need to check if nothing is selected on the table
    	if(m_messageTable.getSelectedRow() < 0){
    		return;
    	}
    	
    	
        if (rcptPlot.isSelected()) {
            selected_user = (String) m_messageTable.getValueAt(row, i_rcptColumn);
        } else {
            selected_user = (String) m_messageTable.getValueAt(row, i_senderColumn);
        }

        String mailr = (String) m_messageTable.getValueAt(row, i_mailrefColumn);

        if (!noPlot.isSelected())
            if (!this.m_msg_displayed_user.equals(selected_user)) {
                m_msg_displayed_user = selected_user;

                userPlot.removeAll();
                userPlot.setPlotType(JPlot2D.BAR);
                userPlot.setBackgroundColor(Color.white);
                userPlot.setFillColor(Color.blue);
                userPlot.setXLabel("Time");
                userPlot.setYLabel("Avg email SENT");
                userPlot.setTitle("User Histogram");
                userPlot.setXScale(Utils.s1by24);
                if (userPlotHash.containsKey(m_msg_displayed_user)) {
                    double[] td = ( userPlotHash.get(m_msg_displayed_user)).get();
                    if (td != null)
                        userPlot.addCurve(Utils.x_24, td);
                } else {
                    double[] td = m_wg.getUsageProfile(m_msg_displayed_user);
                    if (td != null) {
                        userPlot.addCurve(Utils.x_24, td);
                    }
                    userPlotHash.put(m_msg_displayed_user, new ThinDoubleArrayWrapper(td));

                    if (userPlotHash.size() > 200) {
                        userPlotHash.clear();
                    }
                }
                userPlot.repaint();

            }

        if (!this.m_msg_displayed_mailref.equals(mailr)) {
            try {
                m_msg_displayed_mailref = mailr;
                String data[] = m_jdbcView.getBodyByMailref(mailr);

                /*
                 * synchronized (m_jdbcView) { m_jdbcView.getSqlData("select
                 * body from message where mailref = '" + mailr + "'"); data =
                 * m_jdbcView.getRowData(); }
                 */

                if (data.length > 0) {
                    if (isLower && !isStop)
                        m_msgPreviewText.setText(data[0].toLowerCase());
                    else if (!isLower && isStop)
                        m_msgPreviewText.setText(Utils.replaceStop(data[0], m_wg.getHash()));
                    else if (isStop && isLower) {
                        m_msgPreviewText.setText(Utils.replaceStop(data[0].toLowerCase(), m_wg.getHash()));
                    } else
                        m_msgPreviewText.setText(data[0]);
                } else {
                    m_msgPreviewText.setText("ERROR: NO BODY FOR MAILREF: " + mailr);
                }
                m_msgPreviewText.setCaretPosition(0);
            } catch (Exception ex3) {
                JOptionPane.showMessageDialog(this, "1 - Error getting details for " + mailr + "from database: " + ex3,
                        "Problem body part", JOptionPane.ERROR_MESSAGE);

            }
        }
    }

    // required by MouseListener interface
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
        int row = m_messageTable.rowAtPoint(point);
        if (e.getButton() == MouseEvent.BUTTON3) {
            // right-click
            // rightclick
            // if they didn't click on selected area, then no dice

            if (!m_messageTable.isRowSelected(row)) {
                return;
            }

            // show the popup menu for classifying selected emails
            // if (m_showKnobs)
            m_popupMenu.show(m_messageTable, point.x, point.y);
        }

        // the current user

        // update m_msgtext now
        if (m_messageTable.isRowSelected(row))// && m_showKnobs)
        {
            updateMSGtextWindow(row);

        }
        if (e.getClickCount() == 2) {
            // double click
            // try
            // {

            // hard coded to get speed
            // m_messageTable.getColumn("Mailref").getModelIndex()
            String mailref = (String) m_messageTable.getValueAt(row, i_mailrefColumn);
            String detail = "Mailref: " + mailref + "\n";
            detail += "Sender: " + ((String) m_messageTable.getValueAt(row, i_senderColumn)) + "\n";
            detail += "Date: " + (String) m_messageTable.getValueAt(row, i_dateColumn) + "\n";
            detail += "Subject: " + (String) m_messageTable.getValueAt(row, i_subjectColumn) + "\n";

            Utils.popBody(this, detail, mailref, m_jdbcView, true);
            // detailDialog.setLocationRelativeTo(this);
            // detailDialog.show();
            // }
            // catch (Exception ex)
            // {
            // JOptionPane.showMessageDialog(this, "Error getting details from
            // database: " + ex,"Problem",JOptionPane.ERROR_MESSAGE);
            // }
        }
    }// end mouseclick

    /** clear all rows */
    public final void clear() {
        m_tableModel.clear();
        m_msgPreviewText.setText("");
    }

    /**
     * adds message to table
     * 
     * @param data
     *            {mailref, sender, recipient, date/time}
     */
   /* public final void addMessage(final String data[]) {
        m_tableModel.addRow(data);
        m_tableModel.sortByLastSortColumn();
    }*/

    public final void Refresh(final boolean quellAlerts) throws SQLException {
        Refresh(false, quellAlerts, false, null);
    }

    /** displays similiar groups */

    public final void setGroups(final Vector<String> v) {
        // m_view_similiar_vector = v;

        // System.out.println("in setgrous of msg");

        // Dimension D = m_view_similiar.getPreferredSize();
        // System.out.println("d is " + D);

        String temp = new String();

        // if(max_x <10)
        // { max_x = ((Dimension)labStart.getSize()).width +
        // ((Dimension)labEnd.getSize()).width +
        // 2*((Dimension)spinnerStart.getSize()).width;

        // System.out.println("max x is " + max_x);
        // }

        // D.width = max_x;
        m_view_similiar.removeAllItems();

        for (int i = 0; i < v.size(); i++) {
            temp = v.elementAt(i);

            // if (temp.length() > 60)
            // temp = temp.substring(0, 60);

            m_view_similiar.addItem(temp);
        }

        // m_view_similiar.setPreferredSize(D);
        // m_view_similiar.setSize(D);
        // m_view_similiar.setMaximumSize(D);
        // m_view_similiar.repaint();

    }

    /**
     * returns a matrix from the current table consisting of
     * null,label(lowercase),subject,mailref
     */
    /*
     * private String[][] getPreBodyInfoFromTable() {
     * 
     * int rows = m_messageTable.getRowCount();
     * 
     * String data[][] = new String[rows][4]; for (int i = 0; i < rows; i++) {
     * data[i][0] = null; data[i][1] = (String) m_messageTable.getValueAt(i,
     * i_labelColumn); data[i][1] = data[i][1].substring(0, 1).toLowerCase();
     * data[i][2] = (String) m_messageTable.getValueAt(i, i_subjectColumn);
     * data[i][3] = (String) m_messageTable.getValueAt(i, i_mailrefColumn); }
     * return data; }
     */

    /**
     * returns an array of emtemailmessage types of the current email maessages in the view
     */
    private EMTEmailMessage[] getEmailsFromTable() {

        int rows = m_messageTable.getRowCount();

        EMTEmailMessage data[] = new EMTEmailMessage[rows];

        for (int i = 0; i < rows; i++) {
            data[i] = new EMTEmailMessage((String) m_messageTable.getValueAt(i, i_mailrefColumn));
            data[i].setLabel((String) m_messageTable.getValueAt(i, i_labelColumn));
            data[i].setSubject((String) m_messageTable.getValueAt(i, i_subjectColumn));
            data[i].addRCPT((String) m_messageTable.getValueAt(i, i_rcptColumn));
            data[i].setSize(((Integer) m_messageTable.getValueAt(i, i_sizeColumn)).intValue());
        }
        return data;
    }

    /**
     * 
     * @return the class labels in lowercase from the table
     */

    /*
     * private String[] getClassLabelsFromTable() { int rows =
     * m_messageTable.getRowCount();
     * 
     * String data[] = new String[rows];
     * 
     * for (int i = 0; i < rows; i++) { data[i] = (String)
     * m_messageTable.getValueAt(i, i_labelColumn); data[i] =
     * data[i].substring(0, 1).toLowerCase(); } return data; }
     */

    /**
     * Returns an array of distinct labels from the table, excluding those
     * chosen to be left out by the user
     * 
     * @param UseClasses
     * @return
     */
    private String[] getDistinctClassLabelsFromTable(boolean[] UseClasses, boolean fullD) {
        int rows = m_messageTable.getRowCount();
        HashMap<String,String> hm = new HashMap<String,String>();
        String data = new String();
        String fulldata;
        for (int i = 0; i < rows; i++) {
            fulldata = (String) m_messageTable.getValueAt(i, i_labelColumn);
            data = fulldata.substring(0, 1).toLowerCase();
            if (data.equals("u")) {
                if (UseClasses[0]) {
                    if (fullD)
                        hm.put(fulldata, "");
                    else
                        hm.put(data, "");
                }
            } else if (data.equals("i")) {
                if (UseClasses[1]) {
                    if (fullD)
                        hm.put(fulldata, "");
                    else
                        hm.put(data, "");
                }
            } else if (data.equals("s")) {
                if (UseClasses[2]) {
                    if (fullD)
                        hm.put(fulldata, "");
                    else
                        hm.put(data, "");
                }
            } else if (data.equals("v")) {
                if (UseClasses[3]) {
                    if (fullD)
                        hm.put(fulldata, "");
                    else
                        hm.put(data, "");
                }
            } else if (data.equals("n")) {
                if (UseClasses[4]) {
                    if (fullD)
                        hm.put(fulldata, "");
                    else
                        hm.put(data, "");
                }
            }

        }
        return (String[]) (hm.keySet().toArray(new String[hm.size()]));
    }

    private String[] getContentBasedOnTable(int rows) {
        String data[] = new String[rows];

        if (m_groupType == GROUP_SUBJECT) // ie only need subject
        {

            if (isLower && !isStop) {
                for (int i = 0; i < rows; i++) {
                    data[i] = ((String) m_tableModel.getValueAt(i, i_subjectColumn)).toLowerCase();
                }
            } else if (isLower && isStop) // lowercase and stopwords
            {
                for (int i = 0; i < rows; i++) {
                    data[i] = Utils.replaceStop(((String) m_tableModel.getValueAt(i, i_subjectColumn)).toLowerCase(),
                            m_wg.getHash());
                }
            } else if (isStop && !isLower) // stop but not lower
            {
                for (int i = 0; i < rows; i++) {
                    data[i] = Utils.replaceStop((String) m_tableModel.getValueAt(i, i_subjectColumn), m_wg.getHash());
                }
            } else { // no change
                for (int i = 0; i < rows; i++) {
                    data[i] = ((String) m_tableModel.getValueAt(i, i_subjectColumn));
                }
            }
            // data[i] = data[i].trim(); //clean up data??
        } else // for rest of grouping
        {// need to fetch content of each
           BusyWindow BW = new BusyWindow("progress bar", "Setting up body content",true);
            BW.progress(1, rows);
            BW.setVisible(true);
            // added by Ke Wang
            // int col = 0;
            // hard coded to get speed
            // m_messageTable.getColumn("Mailref").getModelIndex();
            String mailref = new String(""), oldmailr = new String("");
            String[][] data2; // will hold temp fetched body info
            synchronized (m_jdbcView) {
                try {
                    for (int i = 0; i < rows; i++) {
                        BW.progress(i, rows);// show work
                        mailref = (String) m_messageTable.getValueAt(i, 0);
                        // fetch body first 800bytes
                        if (!oldmailr.equals(mailref)) {
                            oldmailr = mailref;
                            // if(m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY))
                            // {
                            data2 = m_jdbcView.getSQLData("select m.body,type from message m where m.mailref='"
                                    + mailref + "' and m.type like  '" + m_wg.getAttachType() + "'");
                            // }
                            // else{
                            // data2 = m_jdbcView.getSQLData("select distinct
                            // m.body,type from message m where m.mailref='"
                            // + mailref + "' and m.type like '" +
                            // m_wg.getAttachType() + "'");
                            // data2 = m_jdbcView.getRowData();
                            // }
                            if (data2 == null) {
                                // if(m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY))
                                // {
                                data2 = m_jdbcView.getSQLData("select m.body,type from message m where m.mailref='"
                                        + mailref + "' ");

                                // }
                                // else{

                                // data2 = m_jdbcView
                                // .getSQLData("select distinct
                                // SUBSTRING(m.body,1,800),type from message m
                                // where m.mailref='"
                                // + mailref + "'");
                                // }//data2 = m_jdbcView.getRowData();
                            }
                            if (data2.length > 0) {
                                // if(m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY)){
                                for (int y = 0; y < data2.length; y++) {
                                    if (data2[y][0].length() > 1000)
                                        data2[y][0] = data2[y][0].substring(0, 1000);

                                    // }
                                }

                                for (int y = 0; y < data2.length; y++) {
                                    if (isLower && !isStop)
                                        data[i] += (data2[y][0].toLowerCase() + " " + data2[y][1].toLowerCase() + ' ');
                                    else if (!isLower && isStop)
                                        data[i] += Utils.replaceStop(data2[y][0] + ' ' + data2[y][1] + ' ', m_wg
                                                .getHash());
                                    else if (isLower && isStop)
                                        data[i] += Utils.replaceStop(data2[y][0].toLowerCase() + ' '
                                                + data2[y][1].toLowerCase() + ' ', m_wg.getHash());
                                    else
                                        data[i] += (data2[y][0] + ' ' + data2[y][1] + ' ');
                                }
                            } else {
                                data[i] = " ";
                            }
                        } else
                            data[i] = data[i - 1];// repeat since mailref same

                    }
                } catch (SQLException s) {
                    System.out.println("problem with db" + s);
                }
            }

            data2 = null;
            BW.setVisible(false); // clean up
        }
        return data;
    }

    /**
     * Groups emails in the message table based on the setting chosen in the
     * m_groupBy combo box. we also check for (if lower case) and if to use stop
     * words.
     */

    public final void groupBySimilarity() {
        // refresh the variables from what is chosen in the emaoilfeature window
        // using wingui class to pass args
        isLower = m_wg.isLower();
        isStop = m_wg.isStop();
        // number of current table rows
        int rows = m_messageTable.getRowCount();
        if (rows < 1 || m_groupBy.getSelectedIndex() == GROUP_NOTHING) {
            // either no data, or no group by choice.
            m_groupBy.setSelectedIndex(0);
            m_groupBy.setBackground(Color.red); // default

            return;
        }

        // make everything wait for us
        m_wg.setEnabled(false);
        // refresh threshold from the message bar
        double threshold = (double) m_groupThreshold.getValue() / (double) 200;
        // hard code to get speed
        // m_messageTable.getColumn("Group").getModelIndex();
        // remeber to compare so no need to do same work twice.
        m_groupbyNum = m_groupThreshold.getValue();
        m_groupType = m_groupBy.getSelectedIndex();
        m_groupBy.setBackground(Color.yellow);

        // lets make things easier lets sort on mailref so multiple bodies wont
        // have to be fetched.
        // no need if getting subject since its a straight fetch, and relativly
        // small compared to body
        if (m_groupType != GROUP_SUBJECT) {
            m_tableModel.sortByColumn(i_mailrefColumn, true);
        }

        m_tableModel.setSortingOn(false); // so nothing shifts during grouping
        // contentDis cd; //use to get score
        try {
            String[] data = getContentBasedOnTable(rows);
            // vector to hold group info
            ArrayList<ArrayList> v = new ArrayList<ArrayList>();
            if (m_groupType == GROUP_KEYWORD) // this is by keyword, so need
                                                // to
            // confirm
            // keywords
            {
                // need to show the keywords option.
                KeywordPanel kp = new KeywordPanel(emtConfigHandle.getProperty(EMTConfiguration.WORDLIST));
                String msgs = new String("Please confirm Keyword list for grouping messages");
                Object stuff[] = { msgs, kp };
                int c = JOptionPane.showConfirmDialog(MessageWindow.this, stuff, "Setup Keywords",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                // "Data to be fetched into table will be " + data.length
                // +"\nShould we continue??","Really Big Data
                // Ahead!",JOptionPane.YES_NO_OPTION);
                kp.saveInput();

                if (c == JOptionPane.NO_OPTION) {
                    m_wg.setEnabled(true);
                    return;// no grouping to do.
                }
                // need to fetch keyword list

                double vs[] = this.groupContentWithList(data, threshold, kp.getList());

                for (int i = 0; i < data.length; i++) {
                    // need to multi by 1000 to make double visible as int
                    m_tableModel.setValueAt("" + (int) (1000 * vs[i]), i, i_groupColumn);
                }

                m_wg.setEnabled(true);
                m_wg.StatusBar("Group by Keyword Done");
                return;

            } else if (m_groupType == GROUP_ZSTRING) {
                // do freq stuff.
                v = groupFreq(data, threshold, true);

                // m_wg.setEnabled(true);
            }else if(m_groupType == GROUP_CONTENT_LESS){
            	
                // get ngram size from the email features window
                // changed to static call for easier useage.
                // last false since we dont need to sort others in the group
                v = NGramLimited.groupContent(data, threshold, m_wg.getNGRAM(), true, false);
                // m_wg.setEnabled(true);
            } else {
                // get ngram size from the email features window
                // changed to static call for easier useage.
                // last false since we dont need to sort others in the group
                v = NGram.groupContent(data, threshold, m_wg.getNGRAM(), true, false);
                // m_wg.setEnabled(true);
            }

            if (v != null) {
                m_max_groups = v.size();
                for (int i = 0; i < v.size(); i++) {

                    ArrayList sub = (ArrayList) v.get(i);
                    for (int j = 0; j < sub.size(); j++) {
                        contentDis ttt = ((contentDis) sub.get(j));
                        m_tableModel.setValueAt("" + i, ttt.id, i_groupColumn);

                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "3-Error getting details from database: " + e);
            System.out.println("error" + e);
        }

        m_wg.setEnabled(true);
        m_tableModel.setSortingOn(true);
        m_tableModel.sortByLastSortColumn();
        m_wg.StatusBar("Grouping Emails done with " + m_max_groups + " groups");
    }

    /**
     * pass in data, convert to string freq, take distance and return groups
     * 
     * @return ArrayList of groups, each group is a vector also, using the index
     *         of array as id
     */
    public final ArrayList<ArrayList> groupFreq(String[] data, final double threshhold, boolean show) {

        if (data == null) {
            // System.out.println("no data to group by content");
            return null;
        }

        ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
        int size = data.length;

        if (size == 0) {
            System.out.println("data size is 0. no data to group by content");
            return null;
        }

        int[] flags = new int[size]; // to indicate whether this string has
                                        // been
        // grouped
        for (int i = 0; i < size; i++)
            flags[i] = 0; // set to 1 if it's grouped

        int base = 0; // the base one for one group

        int maxl = size;
        int done = 0;
        BusyWindow bw = null;
        if (show) // will check if to show progress. set in mlearner
        {
            bw = new BusyWindow("Progress Report", "String freq calculation",true);
            bw.progress(1, size);
            bw.setVisible(true);
        }

        // need to convert the data to freq strings.
        String freqs[] = new String[size];
        for (int i = 0; i < size; i++) {
            freqs[i] = String2Freq(data[i].toCharArray());
            // tested works
            // System.out.println(i + ")" + freqs[i]);
            bw.progress(i);
        }
        // start paste

        // repeatly find the groups, until all in group
        // core of this function
        int i;
        while (true) {
            bw.progress(done, maxl);
            // find the base for each group
            i = 0;
            while (i < size && flags[i] == 1)
                i++;
            if (i >= size) // reach the end
            {
                break;
            }
            base = i;

            ArrayList<contentDis> oneGroup = new ArrayList<contentDis>();
            oneGroup.add(new contentDis(base, 1.0));
            flags[base] = 1;

            double len = 2.0 * freqs[base].length();

            // traverse all string to find similar one
            for (int j = i; j < size; j++) { // j=base to speed things up
                if (flags[j] == 1)
                    continue;

                double dis = Utils.LevenshteinDistance(freqs[base], freqs[j]) / len;
                // System.out.println("dis: " + dis + " len: " +
                // freqs[base].length() + " len 2: " + freqs[j].length());

                if (dis >= .8) {
                    oneGroup.add(new contentDis(j, dis));
                    flags[j] = 1;
                }
            }
            done += oneGroup.size();
            // System.out.println("done:"+done+", max:"+maxl);
            ArrayList g = NgramUtilities.sort(oneGroup);
            groups.add(g);
        }// while true
        // end paste

        bw.setVisible(false);

        return groups;
    }

    /** string freq calculation */
    private String String2Freq(char[] data) {
        int freq[] = new int[257];
        for (int i = 0; i < 257; i++)
            freq[i] = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i] > 255)
                freq[256]++;
            else
                freq[data[i]]++;
        }

        // ArrayList countings = new ArrayList();
        // ok we have counted the freq need to sort.

        int next = 0; // offset positional
        for (int i = 0; i < 257; i++) {
            if (freq[i] != 0)
                next++;

        }
        CharFreq[] countings = new CharFreq[next]; // max size
        next = 0;// reset offset
        for (int i = 0; i < 257; i++) {
            if (freq[i] != 0)
                countings[next++] = (new CharFreq((char) i, freq[i]));

        }
        // sort list
        Arrays.sort(countings);
        StringBuffer sb = new StringBuffer(next);
        for (int i = 0; i < next; i++)
            sb = sb.append(countings[i].toString());

        // now to print out the string

        return sb.toString();
    }

    /**
     * Calculate distance between data and keywork/hotwords calc is done by
     * getting a score for each keyword (seeing how often it occurs in the all
     * the data then normalizing hte freq by this score
     */

    private double[] groupContentWithList(final String data[], final double threshold, final String list[]) {
        int j = 0;
        double results[] = new double[data.length];
        Hashtable<String,Double> ht = new Hashtable<String,Double>();// contains the keyword
        for (int i = 0; i < list.length; i++) {

            if (!ht.containsKey(list[i].toLowerCase()))
                ht.put(list[i].toLowerCase(), new Double(0));

        }
        double score = 0;
        int count = 0;
        String temp = new String();
        int scale = 0;
        // now the hastable has fast lookup.
        for (int i = 0; i < data.length; i++) {

            StringTokenizer st = new StringTokenizer(data[i].toLowerCase());
            while (st.hasMoreTokens()) {
                temp = st.nextToken();
                if (ht.containsKey(temp)) {
                    scale++;
                    count = ht.get(temp).intValue();//Integer.parseInt((String) ht.get(temp));
                    count++;
                    ht.put(temp, new Double(count));
                }
            }

        }

        // int //size = ht.size();
        Iterator<String> keys = ht.keySet().iterator();
        while (keys.hasNext()) {
            temp = keys.next();
            count = ht.get(temp).intValue();//Integer.parseInt((String) ht.get(temp));

            ht.put(temp, new Double((double) count / (double) scale));
        }
        // System.out.println("2");

        // now the hastable has fast lookup.
        for (int i = 0; i < data.length; i++) {
            score = 0;
            StringTokenizer st = new StringTokenizer(data[i].toLowerCase());
            while (st.hasMoreTokens()) {
                temp = st.nextToken();
                if (ht.containsKey(temp))
                    score += ((Double) ht.get(temp)).doubleValue();
            }

            results[j++] = score;
        }
        return results;
    }

    /**
     * setup the sql statement for later doing sqlRefresh sqlcntRefresh are
     * setup here
     */

    private String setupSQL(final boolean justCount) {

        //
        /*
         * <KLUDGE>For some reason mysqld will sometimes erroneously return no
         * results. Work around this </KLUDGE> shlomo: have not seen this. that
         * comment put in at version 1.
         */
        StringBuffer query = new StringBuffer();
        String startDate = Utils.mySqlizeDate2YEAR(globalCalendarStart.getTime());
        Calendar cal = new GregorianCalendar();

        cal.setTime(globalCalendarEnd.getTime());
        // cal.add(Calendar.DAY_OF_MONTH, 1);
        String endDate = Utils.mySqlizeDate2YEAR(cal.getTime());
        query.append("where DATEs>='" + startDate + "' and DATEs<='" + endDate + "'");

        // System.out.println("start: " + startDate + " end : "+ endDate);
        // here is where we can customize the query results:
        // based onm what is selected in the drop down lists. if nothing matched
        // then it is the old stuff (every email).
        int view = m_view.getSelectedIndex(); // choose which part (ie class)
                                                // of
        // data
        int user = m_view_user.getSelectedIndex(); // choose which user/all
        int dir = m_view_direction.getSelectedIndex(); // choose
        // inbound/outbound/both
        boolean date = m_view_date.isSelected();// m_view_date.getSelectedIndex();
        // int folder = m_view_folder.getSelectedIndex();

        // variable arguements on choosing the data
        // where class =? date =? and sender = ?
        // also chance of not having either, in which case dont need a where.
        if (justCount) {
            if (view == 1) {
                query = new StringBuffer("select count(distinct e.mailref) from email as e "/*
                                                                                             * left
                                                                                             * join
                                                                                             * kwords
                                                                                             * as k
                                                                                             * on
                                                                                             * e.mailref=k.mailref "
                                                                                             */);
            } else {
                query = new StringBuffer("select count(*) from email  as e "/*
                                                                             * left
                                                                             * join
                                                                             * kwords
                                                                             * as k
                                                                             * on
                                                                             * e.mailref=k.mailref "
                                                                             */);
            }
        }
        // lets start the query, either distinct or not
        else if (m_graphic_mode) // we will show the data spread out
        {
            query = new StringBuffer("select count(mailref), gmonth,gyear from email as e ");
            // } else if (view == 1) {
            // query = new StringBuffer(
            // "select e.mailref
            // ,e.sender,e.rcpt,e.subject,e.numrcpt,e.numattach,e.size,e.groups,e.DATEs,e.class,e.score,0,e.UID,e.utime"+/*+0,k.hotwords*/"
            // from email as e "/*left join kwords as k on e.mailref=k.mailref
            // "*/);
        } else {

            query = new StringBuffer(
                    "select e.mailref,e.sender,e.rcpt,e.subject,e.numrcpt,e.numattach,e.size,-1,e.DATEs,e.times,e.class,e.score,0,e.utime"/* +0",k.hotwords */
                            + " from email as e "/*
                                                     * left join kwords as k on
                                                     * e.mailref=k.mailref "
                                                     */);

        }
        // only time there is no where is if all users, no date, no class,
        // out+in bound
        if (!date && user == 0 && view < 2 && dir == 0 && choose_allFolders.isSelected()) {
            // System.out.println("should not have a where");
        } else {
            query.append("where ");

            if (choose_specificFolders.isSelected()) {
                query.append(" e.folder = '" + m_view_current_folder + "' ");
                if (user != 0 || view > 1 || date)
                    query.append(" and ");

            }
        }

        // now lets see if any date bound
        if (date) {
            query.append("e.DATEs>='" + startDate + "' and e.DATEs<='" + endDate + "' ");
            // do we need to add "and" ?
            if (user != 0 || view > 1)
                query.append(" and ");

        }

        // check direction
        if (view == 7) {

            if (m_view_similiar.getItemCount() == 1) {
                JOptionPane
                        .showMessageDialog(
                                this,
                                "For this option to work, you need to collect and choose settings in the Similiar Users window\nThe similiar groups can be added to the menu, by going to the Similiar User window and clicking on 'All users'.\n");

                return null;
            }
            if (justCount)
                query.append(" (e.sender in " + ((String) m_view_similiar.getSelectedItem()) + " or e.rcpt in "
                        + ((String) m_view_similiar.getSelectedItem()) + ") ");
            // (String)m_view_similiar.getSelectedItem()

        } else if (user != 0 && dir == 1) // not all users and want inbound
        {
            query.append(" e.rcpt = '" + (String) m_wg.getSelectedUser() + "' ");
            // (String)m_userlist.get(user-1) +"' ");
            if (view > 1)
                query.append(" and ");

        } else if (user != 0 && dir == 2) // not all users and want outbound
        {
            query.append(" e.sender = '" + (String) m_wg.getSelectedUser()// (String)
                                                                            // m_view_user.getItemAt(user)
                    + "' ");
            // m_userlist.get(user-1) +"' ");
            if (view > 1)
                query.append(" and ");
        } else if (user != 0) // specific user both inbound and outbound.
        {
            query.append(" (e.sender = '" + (String) m_wg.getSelectedUser() + "' " + " or e.rcpt = '"
                    + (String) m_wg.getSelectedUser() + "')  ");
            // (String)m_userlist.get(user-1) +"' " + " or e.rcpt =
            // '"+(String)m_userlist.get(user-1) +"') ");
            if (view > 1)
                query.append(" and ");
        }

        if (justCount && view <= 1 || m_graphic_mode && view <= 1) {
        } else if (view == 1)// by mailref
        {
            // DERBY
            query.append("order by mailref");

        } else if (view == 2)// virus
        {
            if (justCount || m_graphic_mode)
                query.append("e.class = 'v' ");
            else
                query.append("e.class = 'v' order by mailref");// group (by
            // e.mailref,e.DATEs,e.rcpt)");
        } else if (view == 3 || m_graphic_mode)// spam
        {
            if (justCount || m_graphic_mode)
                query.append("e.class = 's' ");
            else
                query.append("e.class = 's' order by mailref");// group by
            // e.mailref,e.DATEs,e.rcpt");

        } else if (view == 4)// interesting
        {
            if (justCount || m_graphic_mode)
                query.append("e.class = 'i' ");
            else

                query.append("e.class = 'i' order by mailref");// group by
            // e.mailref
            // ,e.DATEs,e.rcpt");
        } else if (view == 5)// unknown
        {
            if (justCount || m_graphic_mode)
                query.append("(e.class = '?' or e.class ='u') ");
            else

                query.append("(e.class = '?' or e.class='u') order by mailref");// group
            // by
            // e.mailref,
            // e.DATEs,e.rpct");
        } else if (view == 6)// not interesting
        {
            if (justCount || m_graphic_mode)
                query.append("e.class = 'n' ");
            else

                query.append("e.class = 'n' order by mailref");// group by
            // e.mailref,
            // e.DATEs,e.rcpt");
        } else if (view == 7)// by similiar groups
        {
            if (m_view_similiar.getItemCount() == 1) {
                JOptionPane
                        .showMessageDialog(
                                this,
                                "For this option to work, you need to collect and choose settings in the Similiar Users window\nThe similiar groups can be added to the menu, by going to the Similiar User window and clicking on 'All users'.\n");

                return null;
            }
            if (!justCount && !m_graphic_mode)// NOT SURE ABOUT THIS SECOND
                                                // PART
                // OF LAGIC
                query.append(" (e.sender in " + ((String) m_view_similiar.getSelectedItem()) + "AND e.rcpt in "
                        + ((String) m_view_similiar.getSelectedItem()) + ") order by mailref");// group
                                                                                                // by
            // e.mailref,e.DATEs,e.rcpt");
        } else if (!m_graphic_mode) // default unqiue sender-rcpt
        {
            query.append(" order by mailref");// group by e.mailref,e.DATEs,
            // e.rcpt"); //was
            // insert,sender,
            // rcpt,
        }
        // turn on in case of sql errors
        // System.out.println("Query will be : " + query );

        return query.toString();

    }// end setupSQL

    /** function to do the count and messageing for it */
    private void doCount(final String query, final boolean clearFirst) {
        String data[][] = new String[0][0];

        if (clearFirst) {
            clear();
        }
        try {
            synchronized (m_jdbcView) {
                data = m_jdbcView.getSQLData(query);
                // data = m_jdbcView.getRowData();
            }
        } catch (SQLException e) {
            System.out.println("problem1: " + e);
            return;
        }
        // to calc the label distributions.
        int c2 = m_messageTable.getColumn("Label").getModelIndex();

        int r = m_messageTable.getRowCount();
        double cnt[] = new double[5];// keep track of each
        for (int i = 0; i < 5; i++)
            cnt[i] = 0;
        // String temp = new String();
        for (int i = 0; i < r; i++) {

            // will need to see the combobx at each row what it is set to.

            switch (((String) (m_messageTable.getValueAt(i, c2))).charAt(0)) {
            case ('U'):
                cnt[0]++;
                break;
            case ('?'):
                cnt[0]++;
                break;
            case ('I'):
                cnt[1]++;
                break;
            case ('S'):
                cnt[2]++;
                break;
            case ('V'):
                cnt[3]++;
                break;
            case ('N'):
                cnt[4]++;
            }
        }
        String reporter = new String();
        reporter = "\nNumber of cells which will be fetched by hitting refresh Button: " + data[0][0]
                + "\nCurrent table size: " + r + "\nNumber of similiar user groups in current table "
                + ((m_view_similiar == null) ? "0" : "" + m_view_similiar.getItemCount())
                + "\nNumber of groups in group by line: " + m_max_groups + "\nLabel type: \n";
        if (r > 0) {
            if (cnt[0] > 0)
                reporter += "Unknown          " + cnt[0] + "::  " + Utils.round(((cnt[0] / r) * 100), 4) + "%\n";
            if (cnt[1] > 0)
                reporter += "Interesting      " + cnt[1] + "::  " + Utils.round(((cnt[1] / r) * 100), 4) + "%\n";
            if (cnt[2] > 0)
                reporter += "Spam                 " + cnt[2] + "::  " + Utils.round(((cnt[2] / r) * 100), 4) + "%\n";
            if (cnt[3] > 0)
                reporter += "Virus                  " + cnt[3] + "::  " + Utils.round(((cnt[3] / r) * 100), 4) + "%\n";
            if (cnt[4] > 0)
                reporter += "Not Interesting " + cnt[4] + "::  " + Utils.round(((cnt[4] / r) * 100), 4) + "%\n";
        }
        JOptionPane.showMessageDialog(this, reporter);
        return;
    }

    /**
     * Refresh the table data view
     * 
     * @param clearFirst
     *            if we should clear the table
     * @param quellAlerts
     *            do not do alerts
     * @param justCount
     *            only count how many would have been generated if we would
     *            refresh.
     */

    public final void Refresh(final boolean clearFirst, final boolean quellAlerts, final boolean justCount, String sql)
            throws SQLException {

        String query = new String();

        if (sql == null || m_graphic_mode) {
            query = setupSQL(justCount);// get current sql for the window
            // configuration
        } else {
            query = sql;
        }
        if (query == null)// error thrown
        {
            System.out.println("problem with refresh in message");
            return;
        }

        if (justCount) {
            final String q = query;
            (new Thread(new Runnable() {
                public void run() {
                    doCount(q, clearFirst);
                }
            })).start();

            return;
        }
        // will update the add users incase new ones have been added
        // if (userChanged || m_view_user.getItemCount() < 10)//set elsewhere
        // once
        // we import data.
        // {
        // addUsers();
        // }
        if (folderChanged) {
            addFolders();
        }
        String data[][] = new String[0][0];

        if (m_graphic_mode) {
            // will draw the grapihcs stuff here
            query += " group by gyear,gmonth";
            if (query.equals(m_sql_exp)) // ie nothing changed
            {
                System.out.println("no change");
                return;
            }

            // System.out.println("query: "+ query);
            m_sql_exp = new String(query);

            // ok will pull out start and end dates and go for prepared
            // statement
            synchronized (m_jdbcView) {
                data = m_jdbcView.getSQLData(query);
                // data = m_jdbcView.getRowData();
            }
            // System.out.println("got data");
            // ok our data is now gottne, need to figure out how many monthes we
            // have.....
            // for now lets just take yrs.
            // count(*),month,yr
            // problem with dates of 0000-00-00
            // quick fix: copy first legal ito
            if (data.length > 0) {
                /*
                 * if(Integer.parseInt(data[0][2]) < 1900) { if(data.length >1)
                 * data[0][2] = data[1][2]; data[0][1] = "1"; }
                 */
                int numberbins = 12 * (1 + Integer.parseInt(data[data.length - 1][2]) - Integer
                        .parseInt(data[0][2]));
                System.out.println("Numbins " + numberbins);
                double x[] = new double[numberbins];
                double y[] = new double[numberbins];
                // m_graphicPlot.removeAll();// = new JPlot2D();

                m_graphicsPlot.setTitle("Email History");
                m_graphicsPlot.setXLabel("Per month useage starting Jan " + data[0][2] + " to Dec "
                        + data[data.length - 1][2]);
                m_graphicsPlot.setYLabel("Total Emails months " + data.length);

                /* added stuf for graphiucs */
                int walkmonth = 0; // this will walk thorugh the months to
                                    // match
                // bins
                int currentmonth;
                int pos = 0;

                for (int i = 0; i < numberbins; i++) {
                    y[i] = 0;// initialize
                    x[i] = i + 1;
                }

                // step through the data and write to the y axis values
                for (int i = 0; i < data.length; i++) {
                    currentmonth = Integer.parseInt(data[i][1]);
                    // if not equal means no data, so advance till equal
                    while (walkmonth != currentmonth && pos < numberbins) {
                        // x[pos] = walkmonth;
                        // y[pos++] =0.0;
                        walkmonth++;
                        if (walkmonth > 11)
                            walkmonth = 0;
                    }

                    // if(walkmonth == currentmonth)
                    // x[pos] = walkmonth;
                    y[pos++] = Double.parseDouble(data[i][0]); // / total ;

                    // next month
                    walkmonth++;
                    if (walkmonth > 11)
                        walkmonth = 0;
                }
                // ready to draw information

                double[] s = new double[2];
                s[0] = 0;
                s[1] = 1;
                // m_graphicPlot.setYScale(s);
                double[] s2 = new double[2];
                s2[0] = 1;
                s2[1] = numberbins + 1;
                // m_graphicPlot.setXScale(s2);
                // m_graphicPlot.addCurve(x,y);
                m_graphicsPlot.setStuff(y, numberbins);
                // need to draw line every 12 months
                // size if max y
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
            }
            /* end stuff for graphics */

        }

        // will savelast sql statement for use in grouping
        else if (!query.equals(m_sql_exp) || force_refresh == true) {
            force_refresh = false;
            // need to save current on stack, only if not using navigation else
            // its already saved
            // System.out.println("before data fetch\n"+query);

            synchronized (m_jdbcView) {
               // System.out.println("q:" + query);
                data = m_jdbcView.getSQLData(query);
                // data = m_jdbcView.getRowData();
            }
            // System.out.println("after data fetch");

            // if too much data warn here.
            if (data.length > 4000) {
                long b = System.currentTimeMillis();

                int c = JOptionPane.showConfirmDialog(MessageWindow.this, "Rows to be fetched into table will be "
                        + data.length + "\nShould we continue??", "Large Data Fetch Ahead!", JOptionPane.YES_NO_OPTION);

                long di = System.currentTimeMillis() - b;
                before += di;// will make up for time of optino pane.

                if (c == JOptionPane.NO_OPTION)
                    return;

            }

            // if(sql==null)
            // addBack(query);
            SelectedData.clear(); // since doing refresh, want to clear old
            // selection??
            update_all_labels = false;
            if (clearFirst) {
                clear();
                m_msg_displayed_mailref = "";
                m_msg_displayed_user = "";
                m_msgPreviewText.setText("Message body will go here.......Current table size " + data.length);
            }

            if (m_sql_exp != null)
                addBack(m_sql_exp);// old one

            m_sql_exp = new String(query);
            m_tableModel.setSortingOn(false);

            for (int i = 0; i < data.length; i++) {
                /*if (data[i].length != COLUMN_NAMES.length) {
                    throw new SQLException("Database returned wrong number of columns" + data[i].length + " and "
                            + COLUMN_NAMES.length);
                }*/

                // here is where need to clean up data to display.
                if (isLower && !isStop)
                    data[i][3] = data[i][3].toLowerCase();
                else if (isLower && isStop)
                    data[i][3] = Utils.replaceStop(data[i][3].toLowerCase(), m_wg.getHash());
                else if (!isLower && isStop)
                    data[i][3] = Utils.replaceStop(data[i][3], m_wg.getHash());

                // check for keyword alert
                /*
                 * if (!quellAlerts && (!data[i][COLUMN_NAMES.length +
                 * 1].equals("null"))) { //System.out.print("."); String brief =
                 * (new Date()) + "\n"; //no render //brief += "Keyword found in
                 * message from " + // m_md5Renderer.privatize(data[i][1]);
                 * brief += "Keyword found in message from " + data[i][1];
                 * String detailed = brief + "\n\n"; detailed += "Message-ID: " +
                 * data[i][0] + "\n"; detailed += "Sender: " + data[i][1] +
                 * "\n"; //no render //detailed += "Message-ID: " + //
                 * m_md5Renderer.privatize(data[i][0]) + "\n"; //detailed +=
                 * "Sender: " + // m_md5Renderer.privatize(data[i][1]) + "\n";
                 * 
                 * String[][] rcpts = null; synchronized (m_jdbcView) { rcpts =
                 * m_jdbcView.getSQLData("select rcpt from email where
                 * email.mailref=\"" + data[i][0] + "\""); //rcpts =
                 * m_jdbcView.getRowData(); } for (int j = 0; j < rcpts.length;
                 * j++) { //no render //detailed += "Recipient: " + //
                 * m_md5Renderer.privatize(rcpts[j][0]) + "\n"; detailed +=
                 * "Recipient: " + rcpts[j][0] + "\n"; } detailed += "\n";
                 * 
                 * detailed += "Words: \n";
                 * 
                 * String words = data[i][COLUMN_NAMES.length + 1];
                 * StringTokenizer strTok = new StringTokenizer(words, "|");
                 * while (strTok.hasMoreTokens()) { String tok =
                 * strTok.nextToken(); String[] parts = tok.split(":"); if
                 * (parts.length != 2) { detailed += " <Malformed!>\n"; } else {
                 * detailed += " " + parts[0] + " (" + parts[1] + ")\n"; } }
                 * m_alerts.alert(Alert.ALERTLEVEL_YELLOW, brief, detailed); }
                 */
            }
            m_tableModel.addData(data, m_view.getSelectedIndex());
            m_tableModel.setSortingOn(true);
            m_tableModel.sortByLastSortColumn();

            // update uid
            // if (data.length > 0) {
            // m_timeLastLogged = (long) Double.parseDouble(data[data.length -
            // 1][COLUMN_NAMES.length]);
            // }

            m_groupBy.setSelectedIndex(0);
            m_groupBy.setBackground(Color.red);

        } else if (m_groupbyNum != m_groupThreshold.getValue() || m_groupType != m_groupBy.getSelectedIndex()) {

            m_groupBy.setSelectedIndex(0);
            m_groupBy.setBackground(Color.red);

        }

    }

    /**
     * this will signal to system to update the folder and user list for speed
     * we only do it if neccessary
     */
    public final void updateRecords() {

        //userChanged = true;
        // addUsers();
        folderChanged = true;
        addFolders();
        m_sql_exp = "";
    }

    public final Vector getSelectedData() {
        return SelectedData;
    }
    /*
     * public static void main(String args[]) { JDBCConnect jdbcUpdate = null;
     * JDBCConnect jdbcView = null; JDialog dialog = new JDialog((Frame) null,
     * true); dialog.setTitle("Messages"); String dbHost = args[0]; String
     * dbName = args[1]; String user = args[2]; String password = args[3]; //
     * open database connection try { jdbcUpdate = new
     * JDBCConnect("jdbc:mysql://" + dbHost + "/" + dbName,
     * "org.gjt.mm.mysql.Driver", user, password); if (jdbcUpdate.connect() ==
     * false) { throw(new Exception()); } jdbcView = new
     * JDBCConnect("jdbc:mysql://" + dbHost + "/" + dbName,
     * "org.gjt.mm.mysql.Driver", user, password); if (jdbcView.connect() ==
     * false) { throw(new Exception()); } } catch (Exception e) {
     * System.err.println("Couldn't connect to database"); System.exit(1); }
     * 
     * Messages messages = new Messages(jdbcUpdate, jdbcView, new
     * md5CellRenderer(false), new Alerts(), 0, true);
     * 
     * dialog.setContentPane(messages); dialog.pack();
     * dialog.setLocationRelativeTo(null); try { messages.Refresh(true); } catch
     * (Exception ex) { ex.printStackTrace(); } dialog.show();
     * 
     * System.exit(0); }
     */
}

// m_spinnerNumber = new SpinnerNumberModel(0.555, 0.000 , 1.000, 0.01);
// spinnerThreshold = new JSpinner(m_spinnerNumber);
// JSpinner.NumberEditor editorNum = new JSpinner.NumberEditor(spinnerThreshold,
// "0.000");
// spinnerThreshold.setEditor(editorNum);
// spinnerThreshold.setFont(new Font("Serif", Font.BOLD, 12));

/*
 * This class is used for making arrows in jbuttons, can be later extended to
 * load images and do other stuff.
 */

class ArrowIcon implements Icon, SwingConstants {
    private int width = 9;

    private int height = 18;

    public static final int RIGHT = 0;

    public static final int LEFT = 1;

    private int[] xPoints = new int[4];

    private int[] yPoints = new int[4];

    public ArrowIcon(int direction) {
        if (direction == LEFT) {
            xPoints[0] = width;
            yPoints[0] = -1;
            xPoints[1] = width;
            yPoints[1] = height;
            xPoints[2] = 0;
            yPoints[2] = height / 2;
            xPoints[3] = 0;
            yPoints[3] = height / 2 - 1;
        } else /* direction == TRAILING */{
            xPoints[0] = 0;
            yPoints[0] = -1;
            xPoints[1] = 0;
            yPoints[1] = height;
            xPoints[2] = width;
            yPoints[2] = height / 2;
            xPoints[3] = width;
            yPoints[3] = height / 2 - 1;
        }
    }

    public int getIconHeight() {
        return height;
    }

    public int getIconWidth() {
        return width;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        int length = xPoints.length;
        int adjustedXPoints[] = new int[length];
        int adjustedYPoints[] = new int[length];

        for (int i = 0; i < length; i++) {
            adjustedXPoints[i] = xPoints[i] + x;
            adjustedYPoints[i] = yPoints[i] + y;
        }

        if (c.isEnabled()) {
            g.setColor(c.getForeground());
        } else {
            g.setColor(Color.gray);
        }

        g.fillPolygon(adjustedXPoints, adjustedYPoints, length);
    }

}
