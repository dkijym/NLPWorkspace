/*
 * Created on Dec 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates 
 */
package metdemo.experiment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

// import chapman.graphics.JPlot2D;

import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.CodedModel_1;
import metdemo.MachineLearning.CorModel;
import metdemo.MachineLearning.LinkAnalysisLearner;
import metdemo.MachineLearning.MLearner;
import metdemo.MachineLearning.NBayesClassifier;
import metdemo.MachineLearning.NGram;
import metdemo.MachineLearning.NGramLimited;
import metdemo.MachineLearning.OutlookModel;
import metdemo.MachineLearning.PGram;
import metdemo.MachineLearning.SVMLearner;
import metdemo.MachineLearning.SpamAssassinLearner;
import metdemo.MachineLearning.TextClassifier;
import metdemo.MachineLearning.Tfidf;
import metdemo.MachineLearning.machineLearningException;
import metdemo.MachineLearning.resultScores;
import metdemo.Parser.EMTEmailMessage;
import metdemo.Tables.AlternateColorTableRowsRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.KeywordPanel;
import metdemo.Tools.Utils;
import metdemo.Window.EmailInternalConfigurationWindow;

/**
 * @author Shlomo
 * 
 * 
 */
public class ExperimentalEMTWindow extends JScrollPane {

    EMTConfiguration m_emailConfiguration;

    EMTDatabaseConnection DBConnect;

    JLabel runningLabel;

    JTextArea commentField;

    EmailInternalConfigurationWindow features;

    JTextField sql_fld, name_setup_fld, name_run_fld, name_model_fld, name_result_fld;

    File setupFileLocation = null;

    File runFileLocation = null;

    JCheckBox uniqueMailref, addToCurrent, useEmail, useMessage, useFileinOrder, newModel, forceLower, useStop;

    JFileChooser chooser;

    JSlider percentageSlider;

    private JComboBox chosen_model_type, body_length_use, experimentDistribution;

    // "No Changes","Even Spread","10% Spam","25% Spam","30% Spam ","50%
    // Spam","60% Spam","75% Spam","90% Spam"
    static final int NOCHANGE = 0;

    static final int EVENSPREAD = 1;

    static final int TEN = 2;

    static final int TWENTYFIVE = 3;

    static final int THIRTY = 4;

    static final int FIFTY = 5;

    static final int SIXTY = 6;

    static final int SEVENTYFIVE = 7;

    static final int NINTY = 8;

    public static final int TRAINALL = 0;

    public static final int ONLYSPAM = 1;

    public static final int ONLYLEGIT = 2;

    Hashtable stopHash;

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

    private JTable resultsTable;

    private DefaultTableModel dtm;

    private JPanel mainjp;

    private JButton viewResults;

    private JComboBox trainingCriteria;

    public ExperimentalEMTWindow(EMTDatabaseConnection emtconnect, EMTConfiguration emtc) {

        mainjp = new JPanel();
        DBConnect = emtconnect;
        m_emailConfiguration = emtc;
        // setup background
        // this = new JPanel();
        features = new EmailInternalConfigurationWindow(emtc, emtconnect, null);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        mainjp.setLayout(gridbag);
        constraints.insets = new Insets(2, 2, 2, 2);

        JLabel Toplabel = new JLabel("Setup the experimental files on DB: " + DBConnect.getDatabaseName());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        gridbag.setConstraints(Toplabel, constraints);
        mainjp.add(Toplabel);

        JLabel label_filename = new JLabel("Filename:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        gridbag.setConstraints(label_filename, constraints);
        mainjp.add(label_filename);

        name_setup_fld = new JTextField(25);
        constraints.gridx = 1;
        constraints.gridy = 1;
        gridbag.setConstraints(name_setup_fld, constraints);
        mainjp.add(name_setup_fld);

        JButton browseButton = new JButton("Browse");
        constraints.gridx = 2;
        constraints.gridy = 1;
        gridbag.setConstraints(browseButton, constraints);
        mainjp.add(browseButton);

        chooser = new JFileChooser();
        // ExampleFileFilter filter = new ExampleFileFilter();
        // filter.addExtension("txt");
        // filter.setDescription("plain text format");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // chooser.setCurrentDirectory(new File("/"));
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
                JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Problem making exprmt subdirectory");
            }
        } else {
            setupFileLocation = subfolder;
        }

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (setupFileLocation != null) {
                    chooser.setCurrentDirectory(setupFileLocation);
                }
                int returnVal = chooser.showOpenDialog(ExperimentalEMTWindow.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // File file = fc.getSelectedFile();
                    // legalfile = true;
                    name_setup_fld.setText(chooser.getSelectedFile().getPath());
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
                String input = new String();
                String filename = name_setup_fld.getText().trim();

                if (filename.length() < 1) {
                    JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "no filename specified");
                    return;
                }

                StringBuffer buf = new StringBuffer();
                try {
                    BufferedReader bufread = new BufferedReader(new FileReader(filename));
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
                Object[] option_array = { "Save Me", "Save to new File", "Cancel/Close" };
                int c = JOptionPane.showOptionDialog(null, m_areaScrollPane, "Experiment" + filename,
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option_array,
                        option_array[2]);

                if (c == JOptionPane.YES_OPTION) {

                } else if (c == JOptionPane.NO_OPTION) {

                } else {

                }

            }
        });

        constraints.gridx = 3;
        constraints.gridy = 1;
        gridbag.setConstraints(viewFile, constraints);
        mainjp.add(viewFile);

        JButton fileinfo = new JButton("File Info");
        fileinfo.setBackground(Color.yellow);
        fileinfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                getFileInfo(name_setup_fld.getText().trim(), SETUP_STATE);
            }
        });
        constraints.gridx = 4;
        constraints.gridy = 1;
        gridbag.setConstraints(fileinfo, constraints);
        mainjp.add(fileinfo);

        // target condition
        JLabel label_sql = new JLabel("SQL where condition:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        gridbag.setConstraints(label_sql, constraints);
        mainjp.add(label_sql);

        sql_fld = new JTextField(25);
        sql_fld.setText("where class = 's' or class='n'");
        constraints.gridx = 1;
        constraints.gridy = 2;
        gridbag.setConstraints(sql_fld, constraints);
        mainjp.add(sql_fld);

        JButton testsql = new JButton("Test SQL");
        constraints.gridx = 2;
        constraints.gridy = 2;
        gridbag.setConstraints(testsql, constraints);
        mainjp.add(testsql);

        testsql.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String sql = new String("Select count(distinct email.mailref) from ");
                if (useEmail.isSelected() && !useMessage.isSelected()) {
                    sql += " email ";
                } else if (useEmail.isSelected() && useMessage.isSelected()) {
                    sql += " email left join message on email.mailref=message.mailref ";
                } else if (!useEmail.isSelected() && useMessage.isSelected()) {
                    sql += " message ";
                } else {
                    return;
                }

                sql += sql_fld.getText().trim();

                try {
                    String data[][] = DBConnect.getSQLData(sql);
                    JOptionPane
                            .showMessageDialog(ExperimentalEMTWindow.this, "Number of lines returned: " + data[0][0]);
                } catch (SQLException s) {
                    System.out.println("3 " + s);
                }

            }
        });

        JButton runsql = new JButton("Execute SQL/Create File");
        runsql.setBackground(Color.red);
        constraints.gridx = 3;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        gridbag.setConstraints(runsql, constraints);
        mainjp.add(runsql);

        runsql.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupExperimentalFile();
            }
        });

        addToCurrent = new JCheckBox("Add to current file", false);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        gridbag.setConstraints(addToCurrent, constraints);
        mainjp.add(addToCurrent);

        useEmail = new JCheckBox("Use Email Table", true);
        String[] distributions = new String[] { "No Changes", "Even Spread", "10% Spam", "25% Spam", "30% Spam",
                "50% Spam", "60% Spam", "75% Spam", "90% Spam" };
        experimentDistribution = new JComboBox(distributions);
        experimentDistribution
                .setToolTipText("Distribute the classes in the output file but keep in time order, for example if time wise all spam come first, checking this will space them evenly over time with other classess");

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

        uniqueMailref = new JCheckBox("Unique Mailref in File", true);
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

        runningLabel = new JLabel("Running the experiment: ");
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(runningLabel, constraints);
        mainjp.add(runningLabel);

        JLabel runningFileLabel = new JLabel("Train File:");
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        gridbag.setConstraints(runningFileLabel, constraints);
        mainjp.add(runningFileLabel);

        name_run_fld = new JTextField(25);
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        gridbag.setConstraints(name_run_fld, constraints);
        mainjp.add(name_run_fld);

        JButton run_browseButton = new JButton("Browse");
        constraints.gridx = 2;
        constraints.gridy = 6;
        gridbag.setConstraints(run_browseButton, constraints);
        mainjp.add(run_browseButton);

        runFileLocation = setupFileLocation.getAbsoluteFile();

        run_browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (runFileLocation != null) {
                    chooser.setCurrentDirectory(runFileLocation);
                }
                int returnVal = chooser.showOpenDialog(ExperimentalEMTWindow.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // File file = fc.getSelectedFile();
                    // legalfile = true;
                    name_run_fld.setText(chooser.getSelectedFile().getPath());
                    runFileLocation = chooser.getCurrentDirectory();

                    // This is where a real application would open the file.
                    // log.append("Opening: " + file.getName() + "." + newline);
                } else {
                    // log.append("Open command cancelled by user." + newline);
                }

            }
        });

        JButton run_info = new JButton("More Info");
        constraints.gridx = 3;
        constraints.gridy = 6;
        gridbag.setConstraints(run_info, constraints);
        mainjp.add(run_info);

        run_info.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getFileInfo(name_run_fld.getText().trim(), RUN_STATE);
            }
        });

        JButton viewFeatures = new JButton("features");
        constraints.gridx = 4;
        constraints.gridy = 6;
        gridbag.setConstraints(viewFeatures, constraints);
        mainjp.add(viewFeatures);

        viewFeatures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame showfeatures = new JFrame("Select Features");
                showfeatures.setSize(new Dimension(800, 500));
                showfeatures.getContentPane().add(features);
                showfeatures.setVisible(true);
            }
        });
        JLabel run_percentage = new JLabel("Percentage Train:");
        constraints.gridx = 0;
        constraints.gridy = 7;
        gridbag.setConstraints(run_percentage, constraints);
        mainjp.add(run_percentage);

        percentageSlider = new JSlider(0, 100, 80);

        percentageSlider.setMajorTickSpacing(5);
        percentageSlider.setSnapToTicks(true);
        percentageSlider.setPaintTicks(true);

        // Hashtable labelTable = new Hashtable();
        // labelTable.put(new Integer(200), new JLabel("More"));
        // labelTable.put(new Integer(0), new JLabel("Less"));
        // m_groupThreshold.setLabelTable(labelTable);
        percentageSlider.setPaintLabels(true);
        percentageSlider.setToolTipText("Percentage of file to use as training");

        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(percentageSlider, constraints);
        mainjp.add(percentageSlider);

        useFileinOrder = new JCheckBox("Keep File Order", true);
        useFileinOrder
                .setToolTipText("if we should leave the order of the mailrefs the same as viewed in file or reorder");
        constraints.gridx = 3;
        constraints.gridy = 7;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(useFileinOrder, constraints);
        mainjp.add(useFileinOrder);

        JLabel modelLabel = new JLabel("Model Name:");
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.gridwidth = 1;
        gridbag.setConstraints(modelLabel, constraints);
        mainjp.add(modelLabel);

        name_model_fld = new JTextField(25);
        constraints.gridx = 1;
        constraints.gridy = 8;

        gridbag.setConstraints(name_model_fld, constraints);
        mainjp.add(name_model_fld);

        name_model_fld.setText("model.cla");

        newModel = new JCheckBox("New Model", true);
        newModel.setToolTipText("if to create a new model or add to old");
        constraints.gridx = 2;
        constraints.gridy = 8;

        gridbag.setConstraints(newModel, constraints);
        mainjp.add(newModel);

        chosen_model_type = winGui.buildJComboModels();
        constraints.gridx = 3;
        constraints.gridy = 8;
        constraints.gridwidth = 2;
        gridbag.setConstraints(chosen_model_type, constraints);
        mainjp.add(chosen_model_type);

        forceLower = new JCheckBox("Force Lower Case", true);
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.gridwidth = 1;
        gridbag.setConstraints(forceLower, constraints);
        mainjp.add(forceLower);

        JPanel stoppanel = new JPanel();
        useStop = new JCheckBox("Use Stop List", false);
        stoppanel.add(useStop);
        JButton sedit = new JButton("Edit Stop List");
        sedit.setToolTipText("Edit current stop word list");
        sedit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                KeywordPanel kp = new KeywordPanel(m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST));
                String msgs = new String("Please confirm Stop list for message bodies");
                Object stuff[] = { msgs, kp };
                int c = JOptionPane.showConfirmDialog(ExperimentalEMTWindow.this, stuff, "Setup Stopwords",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (c == JOptionPane.CANCEL_OPTION) {
                    if (stopHash == null || stopHash.isEmpty())
                        stopHash = kp.getHash(forceLower.isSelected());
                    return;// no grouping to do.
                }
                kp.saveInput();
                stopHash = kp.getHash(forceLower.isSelected());

                m_emailConfiguration.putProperty(EMTConfiguration.STOPLIST, kp.getFileName());

            }
        });

        stoppanel.add(sedit);

        constraints.gridx = 1;
        constraints.gridy = 9;
        gridbag.setConstraints(stoppanel, constraints);
        mainjp.add(stoppanel);

        String[] choices = { "Full Body", "First 400", "First 800", "First 1600" };

        body_length_use = new JComboBox(choices);

        constraints.gridx = 2;
        constraints.gridy = 9;
        gridbag.setConstraints(body_length_use, constraints);
        mainjp.add(body_length_use);

        JButton runxprmt = new JButton("Run");
        runxprmt.setToolTipText("Run the experiment");
        runxprmt.setBackground(Color.red);
        runxprmt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //(new Thread(new Runnable() {
                 //   public void run() {
                      
                        // get handle to file of examples to include in the experiment.
                        String filename = name_run_fld.getText().trim();
                        String modelname = name_model_fld.getText().trim();
                        if (filename.length() < 1) {
                            JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "no filename specified");
                            return;
                        }
                       
                        //set up the model name
                        if (modelname.length() < 1) {
                            modelname = "model.cla";// default
                        }
                        //clean up file name
                        if (filename.indexOf(File.separatorChar) < 0) {
                            filename = runFileLocation.getAbsolutePath() + File.separatorChar + filename;
                        }
                        // stuff need later
                       
                        
                        ArrayList<String> mailrefs = new ArrayList<String>();
                        ArrayList<String> classnames = new ArrayList<String>();
                        
                        
                       
                        String input = new String();
                        try {
                            BufferedReader bufread = new BufferedReader(new FileReader(filename));
                            // buffer mailrefs,class
                            
                            //lets read through the file and store all the mailrefs and classes
                            while ((input = bufread.readLine()) != null) {
                                // need to find mailref and class
                                int a = input.indexOf("\t");
                                if (a < 0) {
                                    continue;
                                }
                                // mailref = input.substring(0, a);
                                mailrefs.add(input.substring(0, a));
                                int b = input.indexOf("\t", a + 1);
                                // classname = input.substring(a + 1, b);
                                classnames.add(input.substring(a + 1, b));

                            }
                            int cutoff = (int) ((double) mailrefs.size() * (double) percentageSlider.getValue() / 100.);
                          ArrayList<resultScores> results = runExperiments(mailrefs,classnames,chosen_model_type.getSelectedIndex(),cutoff,features.getselectedFeaturesBoolean(),MLearner.SPAM_LABEL, features.getFeatures(),trainingCriteria.getSelectedIndex(),false, null);  
                          saveResultsToFile(results, chosen_model_type.getSelectedIndex());
                        } catch (IOException eie) {
                            System.out.println("33:" + eie);
                        }
                    }
             //   }//)).start();

            //}
        });

        constraints.gridx = 3;
        constraints.gridy = 9;
        gridbag.setConstraints(runxprmt, constraints);
        mainjp.add(runxprmt);

        String[] criteria = { "ALL", "ONLY SPAM", "ONLY Legit" };
        trainingCriteria = new JComboBox(criteria);
        trainingCriteria.setToolTipText("Which Examples to feed the TRAINING, (testing unchanged)");
        constraints.gridx = 4;
        constraints.gridy = 9;
        gridbag.setConstraints(trainingCriteria, constraints);
        mainjp.add(trainingCriteria);

        // add comments to experiment
        JLabel commentLabel = new JLabel("Add Comments:");
        constraints.gridx = 0;
        constraints.gridy = 10;
        gridbag.setConstraints(commentLabel, constraints);
        mainjp.add(commentLabel);

        commentField = new JTextArea(25, 4);

        JScrollPane commentholder = new JScrollPane(commentField);
        commentField.setLineWrap(true);
        commentField.setWrapStyleWord(true);
        commentholder.setPreferredSize(new Dimension(100, 40));
        constraints.gridx = 1;
        constraints.gridy = 10;
        constraints.gridwidth = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(commentholder, constraints);
        mainjp.add(commentholder);

        JTextPane separator2 = new JTextPane();
        separator2.setEditable(false);
        separator2.setBackground(Color.orange);
        constraints.gridx = 0;
        constraints.gridy = 11;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(separator2, constraints);
        mainjp.add(separator2);

        JLabel resulttop = new JLabel("Result Section");
        constraints.gridx = 1;
        constraints.gridy = 12;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(resulttop, constraints);
        mainjp.add(resulttop);
        // refresh the reuslts file names
        JButton refreshResults = new JButton("Refresh View");
        refreshResults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshResultsInfo(true);
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 13;
        gridbag.setConstraints(refreshResults, constraints);
        mainjp.add(refreshResults);
        // select and deslsect all
        JPanel selectorHelper = new JPanel();
        JButton selectAll = new JButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectAllinTable(true);
            }
        });
        JButton deselectAll = new JButton("Deselect All");
        deselectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectAllinTable(false);
            }
        });
        JButton delete = new JButton("Delete Selected");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteSelected();
            }
        });
        JButton moreResultFileInfo = new JButton("More info");
        selectorHelper.add(selectAll);
        selectorHelper.add(deselectAll);
        selectorHelper.add(delete);
        selectorHelper.add(moreResultFileInfo);
        constraints.gridx = 1;
        constraints.gridy = 13;
        constraints.gridwidth = 2;
        gridbag.setConstraints(selectorHelper, constraints);
        mainjp.add(selectorHelper);
        JButton graphcombo = new JButton("Graph w Combo");
        constraints.gridx = 3;
        constraints.gridy = 13;
        constraints.gridwidth = 1;
        gridbag.setConstraints(graphcombo, constraints);
        mainjp.add(graphcombo);
        moreResultFileInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // TODO: pop up info here on stats of result file
                resultFileInfo();
            }
        });
        // show the reuslts as a graph
        viewResults = new JButton("Graph");
        viewResults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewResults.setEnabled(false);
                mainjp.setEnabled(false);
                // see which one we want to view:
                Vector chooseNames = new Vector();
                if (dtm.getRowCount() == 0) {
                    return;
                }
                // read backwards since if we remove it will make the table
                // shorter.
                for (int j = 0; j < dtm.getRowCount(); j++) {
                    Boolean checkoff = (Boolean) dtm.getValueAt(j, useColumn);
                    if (checkoff.booleanValue()) {
                        chooseNames.add(dtm.getValueAt(j, exampleNameColumn));
                    }
                }
                if (chooseNames.size() <= 0) {
                    System.out.println("No Results chosen to display");
                    return;
                }
                viewResultsGraph(chooseNames, false, false);
                mainjp.setEnabled(true);
                viewResults.setEnabled(true);
            }
        });

        graphcombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewResults.setEnabled(false);
                mainjp.setEnabled(false);
                // see which one we want to view:
                Vector chooseNames = new Vector();
                if (dtm.getRowCount() == 0) {
                    return;
                }
                // read backwards since if we remove it will make the table
                // shorter.
                for (int j = 0; j < dtm.getRowCount(); j++) {
                    Boolean checkoff = (Boolean) dtm.getValueAt(j, useColumn);
                    if (checkoff.booleanValue()) {
                        chooseNames.add(dtm.getValueAt(j, exampleNameColumn));
                    }
                }
                if (chooseNames.size() <= 0) {
                    System.out.println("No Results chosen to display");
                    return;
                }
                viewResultsGraph(chooseNames, true, false);
                mainjp.setEnabled(true);
                viewResults.setEnabled(true);
            }
        });

        constraints.gridx = 4;
        constraints.gridy = 13;

        constraints.gridwidth = 1;
        gridbag.setConstraints(viewResults, constraints);
        mainjp.add(viewResults);

        JButton graphOnlycombo = new JButton("Graph only Combo");
        constraints.gridx = 3;
        constraints.gridy = 14;
        constraints.gridwidth = 1;
        gridbag.setConstraints(graphOnlycombo, constraints);
        mainjp.add(graphOnlycombo);
        graphOnlycombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewResults.setEnabled(false);
                mainjp.setEnabled(false);
                // see which one we want to view:
                Vector chooseNames = new Vector();
                if (dtm.getRowCount() == 0) {
                    return;
                }
                // read backwards since if we remove it will make the table
                // shorter.
                for (int j = 0; j < dtm.getRowCount(); j++) {
                    Boolean checkoff = (Boolean) dtm.getValueAt(j, useColumn);
                    if (checkoff.booleanValue()) {
                        chooseNames.add(dtm.getValueAt(j, exampleNameColumn));
                    }
                }
                if (chooseNames.size() <= 0) {
                    System.out.println("No Results chosen to display");
                    return;
                }
                viewResultsGraph(chooseNames, true, true);
                mainjp.setEnabled(true);
                viewResults.setEnabled(true);
            }
        });
        // show the reuslts as a graph

        // show alternate grays
        DefaultTableCellRenderer m_TableCellRenderer = new AlternateColorTableRowsRenderer();

        // results file table
        String[] columnsTable = { "Use", "Name", "Time", "Type", "Ratio", "Part", "File", "Comment" };
        dtm = new DefaultTableModel(columnsTable, 0) {
            public Class getColumnClass(final int column) {
                if (column == useColumn) {
                    return Boolean.class;
                } else if (column == ratioColumn) {
                    return Integer.class;
                }
                return String.class;
            }

            public boolean isCellEditable(int row, int col) {
                if (col == useColumn)
                    return true;
                return false;
            }
        };
        resultsTable = new JTable(dtm);
        TableColumn getaColumn = resultsTable.getColumn("Use");
        getaColumn = resultsTable.getColumn("Time");
        getaColumn.setPreferredWidth(100);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("Type");
        getaColumn.setMaxWidth(60);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("Name");
        getaColumn.setPreferredWidth(80);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("Ratio");
        getaColumn.setPreferredWidth(40);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("Part");
        getaColumn.setPreferredWidth(60);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("File");
        getaColumn.setPreferredWidth(60);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        getaColumn = resultsTable.getColumn("Comment");
        getaColumn.setPreferredWidth(340);
        getaColumn.setCellRenderer(m_TableCellRenderer);
        JScrollPane rollingTable = new JScrollPane();
        resultsTable.setPreferredScrollableViewportSize(new Dimension(900, 200));
        rollingTable.setViewportView(resultsTable);

        constraints.gridx = 0;
        constraints.gridy = 15;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(rollingTable, constraints);
        mainjp.add(rollingTable);
        setViewportView(mainjp);
        refreshResultsInfo(false);
    }

   
     /**
     * General framework for running an experiment of type n learner over a set of mailrefs and class labels.
     * @param mailrefs of emails to use
     * @param classnames label list cooresponding to the emails
     * @param type of machine learning to use
     * @param cutoff percentage of emails to use for training
     * @param features_chosen list of used features
     * @param targetString target class label
     * @param featureString feature as a string
     * @param traingCriteria speciies which training to use (only spam/both/non spam)
     * @return list of results scores for the emails trained on
     * @param saveModel if to save the model to a file
     * @param Modelname the name of the file to save it to
     * @return
     */
    public ArrayList<resultScores> runExperiments(ArrayList<String> mailrefs,ArrayList<String> classnames,int type,int cutoff,boolean [] features_chosen,String targetString, String featureString,int traingCriteria,boolean saveModel,String Modelname){
    	
    	
    	int newcounter = 0;
        
        int done = 0;
        
        
    	long timestart, now, left;
        BusyWindow BW = new BusyWindow(ExperimentalEMTWindow.this, "Progress", "Running Experiment",true);
        BW.setVisible(true);
        LinkedList<Object> examplesCache = new LinkedList<Object>();
         
         ArrayList<resultScores> results = new ArrayList<resultScores>();
        
         // create appropriate learner
         MLearner m_mlearner;
         // need to reset the features if we are dealing with
         // spamassassin.
         if (type == MLearner.ML_SPAMASSASSIN) {
             features.setDefaultSpamassassinSelect();
             features.showFigures();
         }

        

         // using a static class mlearner which generalizes
         // the
         // learning process.
         if (type == MLearner.ML_NBAYES_NGRAM)
             m_mlearner = new NBayesClassifier(NBayesClassifier.SUBTYPE_NGRAM, features_chosen);
         else if (type == MLearner.ML_NBAYES_TXTCLASS) // from
             // drop
             // down menu
             m_mlearner = new NBayesClassifier(NBayesClassifier.SUBTYPE_TXTCLASSIFIER,
                     features_chosen);
         else if (type == MLearner.ML_NGRAM)
             m_mlearner = new NGram();
         else if (type == MLearner.ML_LIMITEDNGRAM)
             m_mlearner = new NGramLimited();
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
         else if (type == MLearner.ML_SVM)
             m_mlearner = new SVMLearner();
         else if (type == MLearner.ML_LINKS) {
             m_mlearner = new LinkAnalysisLearner();
         } else if (type == MLearner.ML_SPAMASSASSIN) {
             m_mlearner = new SpamAssassinLearner();

         } else {
             JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Unknown learner choice",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return null;
         }
         m_mlearner.setTargetLabel(targetString);
         BW.setMax(mailrefs.size());
         BW.setVisible(true);
         boolean haveBody = true;

        
         // idea remove the body from the feature string
         // since we
         // have a quicker way to get it out.
         if (type == MLearner.ML_NBAYES_NGRAM || type == MLearner.ML_NBAYES_TXTCLASS
                 || type == MLearner.ML_SPAMASSASSIN) {
             int s = -1;

             if (features_chosen[EmailInternalConfigurationWindow.MAILREF]) {
                 s = featureString.indexOf("e.mailref");
                 if (s == 0) {
                     featureString = featureString.substring(9);
                 } else {
                     featureString = featureString.substring(0, s - 1)
                             + featureString.substring(s + 9);
                 }
             }

             s = featureString.indexOf("m.body");
             haveBody = false;// assume no body
             if (s > 0) { // check for body in feature
                             // string
                 haveBody = true;
                 featureString = featureString.substring(0, s - 1) + featureString.substring(s + 6);
             }
         }

         if (type == MLearner.ML_SPAMASSASSIN) {
             haveBody = true;
         }

         timestart = System.currentTimeMillis();
         for (; done < cutoff; done++) {
             // for training
             // for each mailref, update progress timer
             // #done/n
             // BW.progress(done);

             if (done % 1000 == 1) {
                 now = System.currentTimeMillis() - timestart;
                 left = (((mailrefs.size() * now / done) - now) / 1000);

                 System.out.print(done + " so far: " + (now) / 1000 + " seconds, time left:");

                 System.out.println((left / 60) + " minutes and " + left % 60 + " seconds");

             }

             // build email
             // System.out.print(" "+(String)
             // classnames.get(done));
             String targetl = ((String) classnames.get(done)).toLowerCase();
             if (targetl.startsWith("s")) {
                 targetl = MLearner.SPAM_LABEL;
             } else if (targetl.startsWith("n")) {
                 targetl = MLearner.NOTINTERESTING_LABEL;

             } else if (targetl.startsWith("v")) {
                 targetl = MLearner.VIRUS_LABEL;

             } else if (targetl.startsWith("i")) {
                 targetl = MLearner.INTERESTING_LABEL;

             } else {
                 targetl = MLearner.UNKNOWN_LABEL;
             }
             // check if want to use this specific example in
             // training.
             // example if only legit/spam chosen then can
             // ignore
             // others.
             if (traingCriteria == ONLYSPAM && targetl != MLearner.SPAM_LABEL) {
                 continue;
             } else if (traingCriteria == ONLYLEGIT
                     && targetl == MLearner.SPAM_LABEL) {
                 continue;
             }

             EMTEmailMessage current_message = setupEmailMessage((String) mailrefs.get(done), type,
                     haveBody, features_chosen, featureString);
             if (current_message == null) {
                 System.out.print("empty email ");
                 continue;
             }
             if (examplesCache.size() < 1000) {
                 newcounter++;
                 examplesCache.add(current_message);
                 examplesCache.add(new String(targetl));
             } else {

                 // train on cache
                 for (int a = 0; a < examplesCache.size(); a += 2) {
                     EMTEmailMessage email = (EMTEmailMessage) examplesCache.get(a);
                     String targ = (String) examplesCache.get(a + 1);

                     // System.out.println(newcounter + " " +
                     // email.getMailref());
                     newcounter++;
                     m_mlearner.doTraining(email, targ);
                 }
                 // feed to learne
                 // test on cache
                 for (int a = 0; a < examplesCache.size(); a += 2) {
                     try {

                         EMTEmailMessage email = (EMTEmailMessage) examplesCache.get(a);
                         String targ = (String) examplesCache.get(a + 1);

                         resultScores rsc = m_mlearner.doLabeling(email);
                         rsc.setTesting(false);// training
                         // example
                         rsc.setRealLabel(targ);
                         results.add(rsc);

                     } catch (machineLearningException mle) {
                         System.out.println(mle);
                     }

                 }
                 examplesCache.clear();
             }

         }

         // need to flush cache
         for (int a = 0; a < examplesCache.size(); a += 2) {
             EMTEmailMessage email = (EMTEmailMessage) examplesCache.get(a);
             String targ = (String) examplesCache.get(a + 1);
             m_mlearner.doTraining(email, targ);
         }
         // feed to learne

         for (int a = 0; a < examplesCache.size(); a += 2) {
             try {

                 EMTEmailMessage email = (EMTEmailMessage) examplesCache.get(a);
                 String targ = (String) examplesCache.get(a + 1);

                 resultScores rsc = m_mlearner.doLabeling(email);
                 rsc.setTesting(false);// training
                 // example
                 rsc.setRealLabel(targ);
                 results.add(rsc);

             } catch (machineLearningException mle) {
                 System.out.println(mle);
             }

         }
         examplesCache.clear();
         System.out.println("***********************\nNow for testing");
         // for each milaref feed for result and store
         for (; done < mailrefs.size(); done++) {
             BW.progress(done);
             if (done % 1000 == 1) {
                 now = System.currentTimeMillis() - timestart;
                 left = (((mailrefs.size() * now / done) - now) / 1000);

                 System.out.print(done + " so far: " + (now) / 1000 + " seconds, time left:");

                 System.out.println((left / 60) + " minutes and " + left % 60 + " seconds");

             }

             EMTEmailMessage current_message = setupEmailMessage((String) mailrefs.get(done), type,
                     haveBody, features_chosen, featureString);

             if (current_message == null)
                 continue;

             try {
                 resultScores rsc = m_mlearner.doLabeling(current_message);

                 String targetl = ((String) classnames.get(done)).toLowerCase();
                 if (targetl.startsWith("s")) {
                     targetl = MLearner.SPAM_LABEL;
                 } else if (targetl.startsWith("n")) {
                     targetl = MLearner.NOTINTERESTING_LABEL;

                 } else if (targetl.startsWith("v")) {
                     targetl = MLearner.VIRUS_LABEL;

                 } else if (targetl.startsWith("i")) {
                     targetl = MLearner.INTERESTING_LABEL;

                 } else {
                     targetl = MLearner.UNKNOWN_LABEL;
                 }
                 rsc.setRealLabel(targetl);
                 results.add(rsc);
             } catch (machineLearningException mle) {
                 System.out.println(done + " - " + mle);
             }
         }

         BW.setVisible(false);
         
         if(saveModel){
        	 System.out.println("going to save model");
        	 try{
        	 m_mlearner.save(Modelname);
        	 
        	 }catch(IOException eo){
        		 eo.printStackTrace();
        	 }
        	 
         }
         
         
         System.out.println("done in:");
         System.out.println((System.currentTimeMillis() - timestart) / 1000 + " seconds");

         JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Done training");
         return results;

    }
    
    
    
    
    
    /**
     * Delete those files which are selected in the result table
     * 
     */
    private void deleteSelected() {
        // read through the table
        // if useColumn == true
        // read the nameColumn and move to delete folder
        Vector deletenames = new Vector();
        // read backwards since if we remove it will make the table shorter.
        for (int j = dtm.getRowCount(); j > 0; j--) {
            Boolean checkoff = (Boolean) dtm.getValueAt(j - 1, useColumn);

            if (checkoff.booleanValue()) {
                deletenames.add(dtm.getValueAt(j - 1, exampleNameColumn));

                dtm.removeRow(j - 1);
            }
        }

        File results = new File(System.getProperty("user.dir"), "results");
        System.out.println("going to delete : " + deletenames.size());
        for (int i = 0; i < deletenames.size(); i++) {
            File target = new File(results.getAbsolutePath(), (String) deletenames.elementAt(i));
            target.delete();
        }
        selectAllinTable(false);
    }

    private void selectAllinTable(boolean toSelect) {
        for (int j = 0; j < dtm.getRowCount(); j++) {
            dtm.setValueAt(new Boolean(toSelect), j, useColumn);
        }
    }

    private void viewResultsGraph(Vector chooseNames, boolean docombo, boolean onlyCombo) {

        // get all the results
        resultScores[][] results = new resultScores[chooseNames.size()][];
        for (int i = 0; i < chooseNames.size(); i++) {
            results[i] = loadResultsFromFile((String) chooseNames.elementAt(i));
        }
        new ROCResultWindow(DBConnect, chooseNames, results, docombo, onlyCombo);

    }

    private void refreshResultsInfo(boolean showwarning) {

        // results table with any new info.
        File results = new File(System.getProperty("user.dir"), "results");
        //
        if (!results.exists()) {
            if (showwarning) {
                JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Error", "No Result files Available",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        String names[] = results.list(new FilenameFilter() {
            public boolean accept(File arg0, String arg1) {
                if (arg1.endsWith(".bin.zip")) {
                    return true;
                }
                return false;
            }
        });

        if (names == null || names.length == 0) {
            if (showwarning) {
                JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Error", "No Result filess Available",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        long[] namesV = new long[names.length];
        // to help sort first we convert the filenames to a number
        for (int i = 0; i < names.length; i++) {
            int a = names[i].indexOf("_");
            if (a < 0) {
                namesV[i] = 0;
            } else {
                namesV[i] = Long.parseLong(names[i].substring(0, a));
            }

        }
        // bubble sort sorry but I was rushed :)
        // need to sort the names according to the first part of the names;
        for (int i = 0; i < namesV.length; i++) {
            for (int j = i + 1; j < namesV.length; j++) {
                // we need to see if i and ja re in correct order
                if (namesV[j] > namesV[i]) {
                    long temp = namesV[i];
                    namesV[i] = namesV[j];
                    namesV[j] = temp;
                    String temps = names[i];
                    names[i] = names[j];
                    names[j] = temps;
                }
            }
        }

        // reset the table size
        this.dtm.setRowCount(0);
        // {"Use", "Name" , "Time", "Type", "Comment"};
        for (int i = 0; i < names.length; i++) {
            // create object array to hold table values
            Object values[] = new Object[numberColumns];
            values[useColumn] = new Boolean(false);
            values[exampleNameColumn] = new String(names[i]);
            // open the file
            try {
                FileInputStream in = new FileInputStream(new File(results, names[i]));
                GZIPInputStream gin = new GZIPInputStream(in);
                ObjectInputStream s = new ObjectInputStream(gin);

                String version = (String) s.readObject();
                if (!version.equals("VERSION 1 EMT RESULT FILE")) {
                    continue;
                }
                Date d = (Date) s.readObject();
                values[timeColumn] = d.toString();
                values[typeColumn] = MLearner.getTypeString(s.readInt());// type
                s.readInt();
                // oout.writeInt(resultsarray.size());
                values[fileColumn] = (String) s.readObject();// s.readObject();
                // oout.writeObject(name_run_fld.getText());
                // percentage
                values[ratioColumn] = new Integer(s.readInt());
                // oout.writeInt(percentageSlider.getValue());
                // keep file order
                s.readBoolean();
                // oout.writeBoolean(useFileinOrder.isSelected());
                // model name
                s.readObject();
                // oout.writeObject(name_model_fld.getText());
                // if new model
                s.readBoolean();
                // oout.writeBoolean(newModel.isSelected());
                // force lower case
                s.readBoolean();
                // oout.writeBoolean(forceLower.isSelected());
                // stop words + stopword listname
                s.readObject();
                // oout.writeObject(new String(useStop.isSelected() + "_"
                // +
                // m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST)));
                // full/partial body
                values[bodysizeColumn] = bodyLength(s.readInt());
                // oout.writeInt(body_length_use.getSelectedIndex());
                // comment
                values[commentColumn] = (String) s.readObject();
                // oout.writeObject(commentField.getText());
                // separe line

                // bufw.println(seperatorString);
                // oout.writeObject(seperatorString);//for campitability
                // purposes we
                in.close();
                // get the timestamp
                dtm.addRow(values);
                //
            } catch (FileNotFoundException fnf) {
                System.out.println(fnf);
            } catch (IOException ioe) {
                System.out.println(ioe);
            } catch (ClassNotFoundException cnf) {
                System.out.println(cnf);
            }
        }

    }

    private String bodyLength(int n) {

        switch (n) {
        case (0):
            return "Full Body";
        case (1):
            return "First 400";
        case (2):
            return "First 800";
        case (3):
            return "First 1600";
        default:
            return "nnn";
        }

    }

    public void setupExperimentalFile() {
        String filename = name_setup_fld.getText().trim();
        if (filename.length() < 1) {
            JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "no filename specified");
            return;
        }

        if (filename.indexOf(File.separatorChar) < 0) {
            filename = setupFileLocation.getAbsolutePath() + File.separatorChar + filename;
        }

        String sql = new String();
        if (uniqueMailref.isSelected()) {
            sql = "Select distinct email.mailref,email.class,email.utime from ";
        } else {
            sql = "Select email.mailref,email.class,email.utime from ";

        }
        if (useEmail.isSelected() && !useMessage.isSelected()) {
            sql += " email ";
        } else if (useEmail.isSelected() && useMessage.isSelected()) {
            sql += " email left join message on email.mailref=message.mailref ";
        } else if (!useEmail.isSelected() && useMessage.isSelected()) {
            sql += " message ";
        } else {
            return;
        }

        sql += sql_fld.getText().trim();

        try {
            System.out.println("sql:" + sql);
            String data[][] = DBConnect.getSQLData(sql);
            int[] spamlist = null;
            int spamOffset = 0;
            int[] nonspamlist = null;
            int nonspamOffset = 0;
            // might need to redistribute the data according to the distribution
            // drop list
            if (!(experimentDistribution.getSelectedIndex() == NOCHANGE)) {
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
                bufw = new BufferedWriter(new FileWriter(filename, addToCurrent.isSelected()));

                // if((experimentDistribution.getSelectedIndex()==NOCHANGE)){
                for (int i = 0; i < data.length; i++) {
                    bufw.write(data[i][0] + "\t" + data[i][1] + "\t" + data[i][2] + "\n");
                }
                // }
                // else{
                // TODO: do this for addition to file
                // }
            } else // we want unique mailrefs
            {
                HashMap uniqueMailrefs = new HashMap();
                if (addToCurrent.isSelected()) {
                    try {
                        BufferedReader bufr = new BufferedReader(new FileReader(filename));
                        String line = new String();
                        while ((line = bufr.readLine()) != null) {
                            int n = line.indexOf("\t");
                            uniqueMailrefs.put(line.substring(0, n - 1), "");
                        }
                    } catch (IOException ii1) {

                    }
                }
                bufw = new BufferedWriter(new FileWriter(filename, addToCurrent.isSelected()));

                if ((experimentDistribution.getSelectedIndex() == NOCHANGE)) {

                    for (int i = 0; i < data.length; i++) {
                        if (!uniqueMailrefs.containsKey(data[0])) {
                            bufw.write(data[i][0] + "\t" + data[i][1] + "\t" + data[i][2] + "\n");
                        }
                    }
                } else {
                    // first to calculate chunks to grab from spam and non spam
                    // lists
                    int spamchunk = 0, nonspamchunk = 0;
                    spamOffset = 0;
                    nonspamOffset = 0;
                    if (experimentDistribution.getSelectedIndex() == TEN) {
                        spamchunk = 1;
                        nonspamchunk = 9;
                    } else if (experimentDistribution.getSelectedIndex() == TWENTYFIVE) {
                        spamchunk = 5;
                        nonspamchunk = 15;

                    } else if (experimentDistribution.getSelectedIndex() == THIRTY) {
                        spamchunk = 3;
                        nonspamchunk = 7;

                    } else if (experimentDistribution.getSelectedIndex() == FIFTY) {
                        spamchunk = 5;
                        nonspamchunk = 5;

                    } else if (experimentDistribution.getSelectedIndex() == SIXTY) {
                        spamchunk = 6;
                        nonspamchunk = 4;

                    } else if (experimentDistribution.getSelectedIndex() == SEVENTYFIVE) {
                        spamchunk = 15;
                        nonspamchunk = 5;

                    } else if (experimentDistribution.getSelectedIndex() == NINTY) {
                        spamchunk = 9;
                        nonspamchunk = 1;
                    }

                    for (; (spamOffset < (spamlist.length - spamchunk))
                            && nonspamOffset < (nonspamlist.length - nonspamchunk);) {

                        // for we write out spam examples
                        for (int j = 0; j < spamchunk; j++) {
                            int target = spamlist[spamOffset++];
                            if (!uniqueMailrefs.containsKey(data[target])) {
                                bufw.write(data[target][0] + "\t" + data[target][1] + "\t" + data[target][2] + "\n");
                            }
                        }

                        // then write out non spam examples
                        for (int j = 0; j < nonspamchunk; j++) {
                            int target = nonspamlist[nonspamOffset++];
                            if (!uniqueMailrefs.containsKey(data[target])) {
                                bufw.write(data[target][0] + "\t" + data[target][1] + "\t" + data[target][2] + "\n");
                            }
                        }
                    }

                }

            }
            bufw.close();
            JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "Done!");

        } catch (SQLException s) {
            System.out.println(s);
        } catch (IOException io) {
            System.out.println(io);
        }

    }

    /**
     * Saves the results array of results into a file named:
     * timestamp_type.bin.zip since its a binary zipped file before the results
     * it includes setup information
     * 
     * @param resultsarray
     *            the Result array to save
     * @param type
     *            the number of model.
     */
    private void saveResultsToFile(ArrayList<resultScores> resultsarray, int type) {

        // first establish nameof file

    	if(resultsarray==null){
    		return;
    	}
    	
    	
        File outfile = calculateNextResultFile(type);

        try {
            // fill in the following per file
            // PrintWriter bufw = new PrintWriter(new
            // FileOutputStream(outfile));
            FileOutputStream out = new FileOutputStream(outfile);
            GZIPOutputStream gout = new GZIPOutputStream(out);
            ObjectOutputStream oout = new ObjectOutputStream(gout);
            // version
            oout.writeObject(new String("VERSION 1 EMT RESULT FILE"));
            // date/time
            oout.writeObject(new Date(System.currentTimeMillis()));
            // type of classifier
            oout.writeInt(type);
            // number of examples used
            oout.writeInt(resultsarray.size());
            System.out.println("results size:" + resultsarray.size());
            // TODO: number spam vs number non spam in training and testing
            // training file name
            oout.writeObject(name_run_fld.getText());
            // percentage
            oout.writeInt(percentageSlider.getValue());
            // keep file order
            oout.writeBoolean(useFileinOrder.isSelected());
            // model name
            oout.writeObject(name_model_fld.getText());
            // if new model
            oout.writeBoolean(newModel.isSelected());
            // force lower case
            oout.writeBoolean(forceLower.isSelected());
            // stop words + stopword listname
            oout.writeObject(new String(useStop.isSelected() + "_"
                    + m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST)));
            // full/partial body
            oout.writeInt(body_length_use.getSelectedIndex());
            // comment
            oout.writeObject(commentField.getText());
            // separe line

            // bufw.println(seperatorString);
            oout.writeObject(seperatorString);// for campitability purposes we
            // would like a deliminator where
            // the data starts
            // results
            for (int i = 0; i < resultsarray.size(); i++) {
                oout.writeObject(resultsarray.get(i));
                // bufw.println(resultsarray.get(i));
            }

            oout.flush();
            oout.close();

        } catch (IOException iof) {
            System.out.println(iof);
        }

    }

    private void resultFileInfo() {

        String report = new String();
        Vector chooseNames = new Vector();
        if (dtm.getRowCount() == 0) {
            return;
        }
        // read backwards since if we remove it will make the table shorter.
        for (int j = 0; j < dtm.getRowCount(); j++) {
            Boolean checkoff = (Boolean) dtm.getValueAt(j, useColumn);
            if (checkoff.booleanValue()) {
                chooseNames.add(dtm.getValueAt(j, exampleNameColumn));
            }
        }
        if (chooseNames.size() <= 0) {
            System.out.println("No Results chosen to display");
            return;
        }
        File results = new File(System.getProperty("user.dir"), "results");

        // for each file
        for (int namei = 0; namei < chooseNames.size(); namei++) {

            report += chooseNames.elementAt(namei) + "\n";
            File infile = new File(results.getPath(), (String) chooseNames.elementAt(namei));
            resultScores rscores[] = null;
            try {
                // fill in the following per file
                // PrintWriter bufw = new PrintWriter(new
                // FileOutputStream(outfile));
                FileInputStream in = new FileInputStream(infile);
                GZIPInputStream gin = new GZIPInputStream(in);
                ObjectInputStream oin = new ObjectInputStream(gin);
                // version
                if (!((String) oin.readObject()).startsWith("VERSION 1 EMT RESULT FILE")) {
                    System.out.println("bad version string in file");
                    return;
                }
                // date/time
                oin.readObject();
                // type of classifier
                oin.readInt();
                // number of examples used
                int totalsize = oin.readInt();// oout.writeInt(resultsarray.size());
                // TODO: number spam vs number non spam in training and testing
                // training file name
                oin.readObject();// oout.writeObject(name_run_fld.getText());
                // percentage
                oin.readInt();// oout.writeInt(percentageSlider.getValue());
                // keep file order
                oin.readBoolean();// //oout.writeBoolean(useFileinOrder.isSelected());
                // model name
                oin.readObject();// //oout.writeObject(name_model_fld.getText());
                // if new model
                oin.readBoolean();// oout.writeBoolean(newModel.isSelected());
                // force lower case
                report += "forced lowercase: " + oin.readBoolean() + "\n";// oout.writeBoolean(forceLower.isSelected());
                // stop words + stopword listname
                oin.readObject();// oout.writeObject(new
                // String(useStop.isSelected()
                // + "_"
                // +
                // m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST)));
                // full/partial body
                oin.readInt();// oout.writeInt(body_length_use.getSelectedIndex());
                // comment
                oin.readObject();// oout.writeObject(commentField.getText());
                // separe line

                // bufw.println(seperatorString);
                oin.readObject();// oout.writeObject(seperatorString);//for
                // campitability purposes we
                // would like a deliminator where
                // the data starts
                report += "number examples " + totalsize + "\n";

                rscores = new resultScores[totalsize];
                // results
                int really_spam = 0;
                int labeled_spam = 0;
                int labeled_nonspam = 0;
                int really_nonspam = 0;
                for (int i = 0; i < totalsize; i++) {
                    rscores[i] = (resultScores) oin.readObject();
                    if (rscores[i].getLabel().startsWith(MLearner.SPAM_LABEL)) {
                        labeled_spam++;
                    } else {
                        labeled_nonspam++;
                    }
                    if (rscores[i].getRealLabel().startsWith(MLearner.SPAM_LABEL)) {
                        really_spam++;
                    } else {
                        really_nonspam++;
                    }
                }
                oin.close();
                report += "known labels spam/nonspam : " + really_spam + "/" + really_nonspam + "\n";
                report += "labels       spam/nonspam : " + labeled_spam + "/" + labeled_nonspam + "\n";
            } catch (IOException iof) {
                System.out.println(iof);
            } catch (ClassNotFoundException cnf) {
                System.out.println(cnf);
            }
            report += "\n";
        }// for each file selected
        // show report string
        JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, report);
        return;

    }

    // start
    private resultScores[] loadResultsFromFile(String filename) {

        // first establish nameof file
        File results = new File(System.getProperty("user.dir"), "results");

        File infile = new File(results.getPath(), filename);
        resultScores rscores[] = null;
        try {
            // fill in the following per file
            // PrintWriter bufw = new PrintWriter(new
            // FileOutputStream(outfile));
            FileInputStream in = new FileInputStream(infile);
            GZIPInputStream gin = new GZIPInputStream(in);
            ObjectInputStream oin = new ObjectInputStream(gin);
            // version
            if (!((String) oin.readObject()).startsWith("VERSION 1 EMT RESULT FILE")) {
                System.out.println("bad version string in file");
                return null;
            }
            // date/time
            oin.readObject();
            // type of classifier
            oin.readInt();
            // number of examples used
            int totalsize = oin.readInt();// oout.writeInt(resultsarray.size());
            System.out.println("total size is " + totalsize);
            // TODO: number spam vs number non spam in training and testing
            // training file name
            oin.readObject();// oout.writeObject(name_run_fld.getText());
            // percentage
            oin.readInt();// oout.writeInt(percentageSlider.getValue());
            // keep file order
            oin.readBoolean();// //oout.writeBoolean(useFileinOrder.isSelected());
            // model name
            oin.readObject();// //oout.writeObject(name_model_fld.getText());
            // if new model
            oin.readBoolean();// oout.writeBoolean(newModel.isSelected());
            // force lower case
            oin.readBoolean();// oout.writeBoolean(forceLower.isSelected());
            // stop words + stopword listname
            oin.readObject();// oout.writeObject(new
                                // String(useStop.isSelected()
            // + "_"
            // + m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST)));
            // full/partial body
            oin.readInt();// oout.writeInt(body_length_use.getSelectedIndex());
            // comment
            oin.readObject();// oout.writeObject(commentField.getText());
            // separe line

            // bufw.println(seperatorString);
            oin.readObject();// oout.writeObject(seperatorString);//for
            // campitability purposes we
            // would like a deliminator where
            // the data starts
            rscores = new resultScores[totalsize];
            // results
            for (int i = 0; i < totalsize; i++) {
                rscores[i] = (resultScores) oin.readObject();
            }
            oin.close();

        } catch (IOException iof) {
            System.out.println(iof);
        } catch (ClassNotFoundException cnf) {
            System.out.println(cnf);
        }

        return rscores;
    }

    // end

    private File calculateNextResultFile(int type) {
        // first we need to establish is results subdirectory exists
        File results = new File(System.getProperty("user.dir"), "results");
        //
        if (!results.exists()) {
            results.mkdir();
        }

        // lets juts use the current time stamp at the results filename
        // time_results.bin
        String name = new String(System.currentTimeMillis() + "_" + type + "_results.bin.zip");
        return new File(results.getAbsolutePath(), name);
    }

    private EMTEmailMessage setupEmailMessage(String mailref, int type, boolean haveBody,
            boolean SelectedFeaturesBoolean[], String featuresString) {
        EMTEmailMessage emtm = new EMTEmailMessage(mailref);

        if (type == MLearner.ML_NBAYES_NGRAM || type == MLearner.ML_NBAYES_TXTCLASS || type == MLearner.ML_SPAMASSASSIN) {
            // TODO: nbayes work
            String sqlfetch = new String("select " + featuresString + " from email as e where e.mailref = ?");

            try {
                if (featuresString.length() > 1)
                    synchronized (DBConnect) {
                        PreparedStatement ps = DBConnect.prepareStatementHelper(sqlfetch);

                        ps.setString(1, mailref);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            int position = 1;

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDER]) {
                                emtm.setFromEmail(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SNAME]) {
                                emtm.setFromName(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SDOM]) {
                                emtm.setFromDomain(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SZN]) {
                                emtm.setFromZN(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPT]) {
                                emtm.addRCPT(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RNAME]) {
                                emtm.setRecName(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RDOM]) {
                                emtm.setRecDomain(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RZN]) {
                                emtm.setRecZN(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMRCPT]) {
                                emtm.setNumRcpts(rs.getShort(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.NUMATTACH]) {
                                emtm.setNumberAttached(rs.getShort(position++));
                            }
                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SIZE]) {
                                emtm.setSize(rs.getInt(position++));
                            }

                            // milaref remved

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.XMAILER]) {
                                emtm.setXMailer(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.DATES]) {
                                emtm.setDate(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMES]) {
                                emtm.setTime(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FLAGS]) {
                                emtm.setFlags(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MSGHASH]) {
                                emtm.setMsgHash(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RPLYTO]) {
                                emtm.setRPLYTO(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FORWARD]) {
                                emtm.setForward(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TYPE]) {
                                emtm.setType(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RECNT]) {
                                emtm.setReCOUNT(rs.getShort(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SUBJECT]) {
                                emtm.setSubject(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.TIMEADJ]) {
                                emtm.setTimeADJ(rs.getLong(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.FOLDER]) {
                                emtm.setFolder(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.UTIME]) {
                                emtm.setUtime(rs.getLong(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.SENDERLOC]) {
                                emtm.setSenderLOC(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.RCPTLOC]) {
                                emtm.setRCPTLOC(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.INSERTTIME]) {
                                emtm.setInsertTime(rs.getString(position++));
                            }

                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.HOURTIME]) {
                                emtm.setGHour(rs.getShort(position++));
                            }
                            if (SelectedFeaturesBoolean[EmailInternalConfigurationWindow.MONTHTIME]) {
                                emtm.setGMonth(rs.getShort(position++));
                            }

                        }
                    }
            } catch (SQLException s12) {
                System.out.println(s12);
            }

        }// end work specific to naive bayes modelers.

        if (haveBody) {
            String body[][] = DBConnect.getBodyAndTypeByMailref(mailref);
            String Subject = DBConnect.getSubjectByMailref(mailref);

            if (Subject == null) {
                System.out.println(mailref);
                Subject = "";
            }

            if (body == null) {
                return null;
            }

            // Full Body", "First 400", "First 800", "First 1600
            if (body_length_use.getSelectedIndex() != 0) {
                for (int i = 0; i < body.length; i++) {
                    switch (body_length_use.getSelectedIndex()) {
                    case (1):
                        if (body[i][0].length() > 400)
                            body[i][0] = body[i][0].substring(0, 400);
                        break;
                    case (2):
                        if (body[i][0].length() > 800)
                            body[i][0] = body[i][0].substring(0, 800);
                        break;
                    default:
                        if (body[i][0].length() > 1600)
                            body[i][0] = body[i][0].substring(0, 1600);
                        break;

                    }

                }
            }

            if (forceLower.isSelected() && !useStop.isSelected()) {

                Subject = Subject.toLowerCase();
            } else if (forceLower.isSelected() && useStop.isSelected()) {

                Subject = Utils.replaceStop(Subject.toLowerCase(), stopHash);
            } else if (!forceLower.isSelected() && useStop.isSelected()) {

                Subject = Utils.replaceStop(Subject, stopHash);
            }

            for (int i = 0; i < body.length; i++) {
                if (body[i][1].toLowerCase().startsWith("text")) {
                    if (forceLower.isSelected() && !useStop.isSelected()) {
                        body[i][0] = body[i][0].toLowerCase();
                    } else if (forceLower.isSelected() && useStop.isSelected()) {
                        body[i][0] = Utils.replaceStop(body[i][0].toLowerCase(), stopHash);
                    } else if (!forceLower.isSelected() && useStop.isSelected()) {
                        body[i][0] = Utils.replaceStop(body[i][0], stopHash);
                    }
                } else {
                    body[i][0] = "";
                }
            }

            emtm.setBody(body);
            emtm.setSubject(Subject);
        }

        return emtm;
    }

    /**
     * Display file info, will depend on if we are showing setup or experimental
     * state
     * 
     * @param filename
     *            or exeriment file to process
     * @param state
     *            if we are in Setup state or run state
     */
    public String getFileInfo(String filename, int state) {
        String input = new String();
        // TODO: something with the state
        int train_total = 0;
        int test_total = 0;
        int spam_train = 0;
        int spam_test = 0;

        int total_count = 0;
        int cutoff = 0;
        HashMap countings_train = new HashMap();
        HashMap countings_test = new HashMap();
        int current = 0;
        if (filename == null || filename.length() < 1) {
            JOptionPane.showMessageDialog(ExperimentalEMTWindow.this, "no filename specified");
            return null;
        }
        String report = new String("Report on " + filename + "\n");
        String report2 = new String();
        // StringBuffer buf = new StringBuffer();
        try {
            BufferedReader bufread = new BufferedReader(new FileReader(filename));
            while ((input = bufread.readLine()) != null) {
                if (!input.startsWith("#")) {
                    total_count++;
                }
            }
            // calculate training cutoff
            cutoff = (int) ((double) total_count * (double) percentageSlider.getValue() / 100.);

            // now to re-read it
            bufread = new BufferedReader(new FileReader(filename));
            while ((input = bufread.readLine()) != null) {
                // check for comment
                if (input.startsWith("#")) {
                    continue;
                }
                // need to find class

                int a = input.indexOf("\t");

                int b = input.indexOf("\t", a + 1);

                String classname = input.substring(a + 1, b);
                if (current < cutoff) {
                    if (!countings_train.containsKey(classname)) {
                        countings_train.put(classname, new Integer(1));
                    } else {
                        countings_train.put(classname, new Integer(1 + ((Integer) countings_train.get(classname))
                                .intValue()));
                    }
                } else {
                    if (!countings_test.containsKey(classname)) {
                        countings_test.put(classname, new Integer(1));
                    } else {
                        countings_test.put(classname, new Integer(1 + ((Integer) countings_test.get(classname))
                                .intValue()));
                    }
                }
                current++;
            }
            report2 += "Training\n";
            for (Iterator itera = countings_train.keySet().iterator(); itera.hasNext();) {
                String key1 = (String) itera.next();
                Integer countclass = (Integer) countings_train.get(key1);
                train_total += countclass.intValue();
                if (key1.startsWith("s")) {
                    spam_train = countclass.intValue();
                }
                report2 += key1 + "\t " + countclass + "\n";
            }
            report2 += "Training examples: " + train_total + "\n";
            report2 += "Spam percentage: " + Utils.round((100. * spam_train / train_total), 2) + "%\n";

            report2 += "\nTesting\n";
            for (Iterator itera = countings_test.keySet().iterator(); itera.hasNext();) {
                String key1 = (String) itera.next();
                Integer countclass = (Integer) countings_test.get(key1);
                test_total += countclass.intValue();
                if (key1.startsWith("s")) {
                    spam_test = countclass.intValue();
                }
                report2 += key1 + "\t " + countclass + "\n";

            }
            report2 += "Testing examples:" + test_total + "\n";
            report2 += "Spam percentage: " + Utils.round((100. * spam_test / test_total), 2) + "%\n";
            report2 += "Total Examples in File:" + (train_total + test_total) + "\n";
        } catch (IOException fnf) {
            System.out.println("2 " + fnf);
        }

        JTextArea textArea = new JTextArea(report + report2);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane m_areaScrollPane = new JScrollPane(textArea);
        m_areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        m_areaScrollPane.setPreferredSize(new Dimension(550, 380));
        // ok we've setup the body text
        JOptionPane
                .showMessageDialog(null, m_areaScrollPane, "Experiment " + filename, JOptionPane.INFORMATION_MESSAGE);
        // , null, option_array,
        // option_array[2]);
        return report2;
    }
/*
    public static void main(String s[]) {
        // EMTDatabaseConnection emtdata = new
        // EMTMysqlConnection("blackberry.cs.columbia.edu",
        // "emtfall04", "root", "ddw641qq");
        EMTDatabaseConnection emtdata = new EMTMysqlConnection("localhost", "emt05", "root", "ddw641q");
        try {
            emtdata.connect(true);
        } catch (Exception ee) {
            System.out.println(ee);
            System.exit(-1);
        }

        EMTConfiguration emtc = new EMTConfiguration();
        emtc.loadFileFromDisk("Config\\EMTConfig2.txt");

        ExperimentalEMTWindow panel = new ExperimentalEMTWindow(emtdata, emtc); // creates
        // a
        // place
        // on
        // the
        // screen
        JFrame frame = new JFrame("Experimental");// title the frame
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.getContentPane().add("Center", panel);
        frame.pack();
        frame.setBounds(100, 100, 900, 600); // moves it x,y,width,height

        frame.setVisible(true);
    }
*/}