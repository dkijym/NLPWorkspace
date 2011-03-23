/*
 * Created on Feb 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.experiment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.MLearner;
import metdemo.MachineLearning.resultScores;
import metdemo.Tables.AlternateColorTableRowsRenderer;
import metdemo.Tables.TableSorter;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.Utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Shlomo Hershkop
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ROCResultWindow {

    static final int c_mailref = 0;

    static final int c_real = 1;

    static final int c_classifiers = 2;

    static final int NUMBERCOMBO = 5;

    private DefaultTableModel resultTablemodel;

    TableSorter viewMSGsorter;

    JTable viewExampleTable;

    EMTDatabaseConnection DBConnect;

    JFreeChart ROCchart;

    private boolean zoom = false;

    /**
     * Utility for displaying the results of the spam classification
     *  
     */
    public ROCResultWindow(EMTDatabaseConnection dbc, Vector chooseNames,
            resultScores[][] resultsSet, boolean doCombinations,
            boolean OnlyCombo) {

        DBConnect = dbc;
        //setup the table to show results
        String[] columnsTable = null;
        /*
         * if(OnlyCombo){ columnsTable = new String[2 + 3]; } else
         */if (doCombinations) {
            columnsTable = new String[2 + chooseNames.size() + NUMBERCOMBO];//+2
            // for
            // equal
            // and
            // nb
            // weighted
            // and
            // oracle
        } else {
            columnsTable = new String[2 + chooseNames.size()];
        }
        columnsTable[0] = "Mailref";
        columnsTable[1] = "Real";
        //if(!OnlyCombo)
        for (int i = 0; i < chooseNames.size(); i++) {
            columnsTable[c_classifiers + i] = "Score" + i;
        }
        /*
         * if(OnlyCombo){ columnsTable[c_classifiers ] = "Equal";
         * columnsTable[c_classifiers +2] = "Oracle"; columnsTable[c_classifiers +
         * 1] = "NBayes"; } else
         */
        if (doCombinations) {
            columnsTable[c_classifiers + chooseNames.size()] = "Equal";
            columnsTable[c_classifiers + chooseNames.size() + 1] = "NBayes";
            columnsTable[c_classifiers + chooseNames.size() + 2] = "Oracle";
            columnsTable[c_classifiers + chooseNames.size() + 3] = "NDim";
            columnsTable[c_classifiers + chooseNames.size() + 4] = "WMajority";
            

        }

        resultTablemodel = new DefaultTableModel(columnsTable, 0) {
            public Class getColumnClass(final int column) {
                if (column > c_real) {
                    return Point.class;
                }
                return String.class;
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

        };

        viewMSGsorter = new TableSorter(resultTablemodel);
        viewExampleTable = new JTable(viewMSGsorter);
        viewMSGsorter.addMouseListenerToHeaderInTable(viewExampleTable);
        viewExampleTable.addMouseListener(new MouseListener() {
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
                int row = viewExampleTable.rowAtPoint(point);

                if (e.getClickCount() == 2) {
                    String mailref = (String) viewMSGsorter.getValueAt(row,
                            c_mailref);
                    System.out.println("m" + mailref);
                    //try to get sender info
                    String premessage = "";
                    try {

                        String data[][] = DBConnect
                                .getSQLData("select sender from email where mailref = '"
                                        + mailref + "'");

                        if (data.length > 0 && data[0][0] != null)
                            premessage = "Sender: " + data[0][0];

                    } catch (SQLException se) {

                    }

                    Utils.popBody(null, premessage, mailref, DBConnect, true);

                }
            }
        });

        //TableColumn getaColumn = viewExampleTable.getColumn("Score");
        //getaColumn.setMaxWidth(45);
        DefaultTableCellRenderer m_TableCellRenderer = new AlternateColorTableRowsRenderer();

        TableColumn getaColumn = viewExampleTable.getColumn("Mailref");
        getaColumn.setCellRenderer(m_TableCellRenderer);
        DefaultTableCellRenderer realRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {

                String cellValue = (value instanceof String) ? (String) value
                        : "";

                setBackground((cellValue.startsWith("S")) ? Color.RED
                        : Color.yellow);
                setText((value == null) ? "" : value.toString());
            }
        };
        getaColumn = viewExampleTable.getColumn("Real");
        getaColumn.setCellRenderer(realRenderer);
        getaColumn.setMaxWidth(45);
        DefaultTableCellRenderer classifierRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {

                if (value instanceof Point) {
                    setBackground((((Point) value).y == MLearner.SPAM_CLASS) ? Color.RED
                            : Color.YELLOW);
                    setText("" + ((Point) value).x);
                } //else {
                //if(value!=null)
                //		System.out.print(value.getClass());
                //setValue(value);
                //}
            }
        };
        //setup the scores columns
        //if(!OnlyCombo){
        for (int i = 0; i < chooseNames.size(); i++) {
            getaColumn = viewExampleTable.getColumn("Score" + i);
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
        }
        //System.out.println("reslen: "+ resultsSet.length);
        if (doCombinations) {
            getaColumn = viewExampleTable.getColumn("Equal");
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
            getaColumn = viewExampleTable.getColumn("Oracle");
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
            getaColumn = viewExampleTable.getColumn("NBayes");
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
            getaColumn = viewExampleTable.getColumn("NDim");
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
            getaColumn = viewExampleTable.getColumn("WMajority");
            getaColumn.setCellRenderer(classifierRenderer);
            getaColumn.setMaxWidth(45);
            resultsSet = addCombinations(resultsSet);
            chooseNames.add("_" + MLearner.ML_EW + "_");
            chooseNames.add("_" + MLearner.ML_NBW + "_");
            chooseNames.add("_" + MLearner.ML_ORACLE + "_");
            chooseNames.add("_" + MLearner.ML_NB2d + "_");
            chooseNames.add("_" + MLearner.ML_WEIGHTEDMAJORITY + "_");
            
        }
        //calculate combinations:

        //System.out.println("reslen: "+ resultsSet.length);
        //end setup tables
int runningtotal=0;
        XYSeriesCollection ROCCollection = new XYSeriesCollection();
        XYSeriesCollection scoreDistribution = new XYSeriesCollection();
        XYSeriesCollection scoreProbabilityDistribution = new XYSeriesCollection();

        //for each file
        HashMap mailref_location = new HashMap();

        for (int namei = 0; namei < resultsSet.length; namei++) {

            //double fp_axis[] = new double[200];
            //double detection_axis[] = new double[200];
            double distributionSPAM[] = new double[202];
            double distributionLEGIT[] = new double[202];
            XYSeries histScoreGood = new XYSeries(namei + " good", false, true);
            XYSeries histScoreSpam = new XYSeries(namei + " spam", false, true);
            //will see how the scores are distributed
            XYSeries rocpoints = new XYSeries(namei + " "
                    + getTypeString((String) chooseNames.elementAt(namei))
                    + " classifier", false, true); //dont sort and allow dupli
            int totalspam = 0;
            for (int j = 0; j < resultsSet[namei].length; j++) {
                //ignore training examples
                if (!resultsSet[namei][j].isTesting()) {
                    continue;
                }

                int score;

                if (resultsSet[namei][j].getLabel().startsWith(
                        MLearner.SPAM_LABEL)) {
                    score = 101 + resultsSet[namei][j].getScore();
                } else {
                    score = 100 - resultsSet[namei][j].getScore();
                }

                if (score > 201) {
                    score = 201;
                } else if (score < 0) {
                    score = 0;
                }
                //we really want to color it on the real color but only if we
                // have it marked
                String t123 = resultsSet[namei][j].getRealLabel();
                if (t123 == null)
                    t123 = resultsSet[namei][j].getLabel();

                int targetRow = resultTablemodel.getRowCount();
                if (mailref_location.containsKey(resultsSet[namei][j]
                        .getMailref())) {
                    targetRow = ((Integer) mailref_location
                            .get(resultsSet[namei][j].getMailref())).intValue();
                } else {
                    mailref_location.put(resultsSet[namei][j].getMailref(),
                            new Integer(resultTablemodel.getRowCount()));

                    //first time we see the mailref
                    Object[] newval = new Object[2 + chooseNames.size()];

                    newval[0] = resultsSet[namei][j].getMailref();
                    newval[1] = t123;
                    for (int aa = c_classifiers; aa < (namei + c_classifiers); aa++) {
                        newval[aa] = new Point(-1, -1);
                    }
                    resultTablemodel.addRow(newval);
                    //resultTablemodel.setValueAt(results[j].getMailref(),j,c_mailref);
                    //resultTablemodel.setValueAt(t123,j,c_real);
                }
                int coloring = MLearner.NOT_INTERESTING_CLASS;
                //eq instead of starts wi
                if (resultsSet[namei][j].getLabel().equals(
                        MLearner.SPAM_LABEL)) {
                    coloring = MLearner.SPAM_CLASS;
                }
                //dta[3] = getTypeString((String)
                // chooseNames.elementAt(namei));
                //eq instead startswit
                if (t123.equals(MLearner.SPAM_LABEL)) {
                    distributionSPAM[score]++;
                    totalspam++;
                    //	dta[1] = new Point(score, 1);
                    resultTablemodel.setValueAt(new Point(score, coloring),
                            targetRow, c_classifiers + namei);
                    histScoreSpam.add(targetRow,score);
                } else {
                    distributionLEGIT[score]++;
                    resultTablemodel.setValueAt(new Point(score, coloring),
                            targetRow, c_classifiers + namei);

                    histScoreGood.add(targetRow,score);
                }
                
               // scoreDistribution.addSeries(histScoreSpam);
               
            }//end through all results
            /* XYSeries histScoreGood = new XYSeries(namei + " Good", false, true);
            for (int i = 0; i < distributionLEGIT.length; i++) {
                histScoreGood.add(i, distributionLEGIT[i]);
            }
            XYSeries histScoreSpam = new XYSeries(namei + " Spam", false, true);
            for (int i = 0; i < distributionSPAM.length; i++) {
                histScoreSpam.add(i, distributionSPAM[i]);
            }
            */
            runningtotal = 0;
            XYSeries histScoreProbabilitySpam = new XYSeries(namei + " Spam",
                    false, true);
            for (int i = 0; i < distributionSPAM.length; i++) {
                runningtotal += distributionSPAM[i];
                histScoreProbabilitySpam.add(i,
                        (100 * runningtotal / totalspam));
            }
            if (OnlyCombo) {
                if (namei >= resultsSet.length - NUMBERCOMBO) {
                    scoreDistribution.addSeries(histScoreGood);
                    scoreDistribution.addSeries(histScoreSpam);
                }
            } else {
                scoreDistribution.addSeries(histScoreGood);
                scoreDistribution.addSeries(histScoreSpam);
            }
           
            scoreProbabilityDistribution.addSeries(histScoreProbabilitySpam);
            //calculate roc
            //			det detection rate ie is the stuff the system go right, sometimes
            // called
            // precision
            //    tp / tp + fp
            //    spam which is really spam / ????total we said is spam
            //fp = fp/fp+tn
            //     normal we said is spam / ????all we said is normal.

            double old_fp = 0;
            double old_det = 0;
            double gain_axis[] = new double[1000+1];
            for(int i=0;i<gain_axis.length;i++){
                gain_axis[i] = -1;
            }
            
            
            for (int threshold = 0; threshold < 200; threshold++) {

                int totalSpam = 0;
                int totalNonSpam = 0;
                int legit_as_spam = 0;
                int spam_as_spam = 0;
                int score = 0;
                String label = null, actual = null;
                for (int j = 0; j < resultsSet[namei].length; j++) {

                    if (!resultsSet[namei][j].isTesting()) {
                        continue;
                    }

                    label = resultsSet[namei][j].getLabel();
                    actual = resultsSet[namei][j].getRealLabel();
                    if (label.startsWith(MLearner.SPAM_LABEL)) {
                        score = 101 + resultsSet[namei][j].getScore();
                    } else {
                        score = resultsSet[namei][j].getScore();
                    }

                    if (actual.startsWith(MLearner.SPAM_LABEL)) {
                        totalSpam++;
                    } else {
                        totalNonSpam++;
                    }

                    if (score >= threshold) {

                        if (actual.startsWith(MLearner.SPAM_LABEL)) {
                            spam_as_spam++;
                        } else {
                            legit_as_spam++;
                        }
                    }

                }//for each score
                double d_det = Utils.round(100 * (double) (spam_as_spam)
                        / (totalSpam), 2);
                double d_fp = Utils.round(100 * (double) (legit_as_spam)
                        / (totalNonSpam), 2);

                if (old_det == d_det && old_fp == d_fp) {
                } else {
                    rocpoints.add(d_fp, d_det);
                    //System.out.println(threshold + " = " + d_det + " " +
                    // d_fp);
                    old_det = d_det;
                    old_fp = d_fp;
                    if(gain_axis[(int)(d_fp*10)]==-1){
                    gain_axis[(int)(d_fp*10)] = d_det;
                    }
                    
                    
                }//fp_axis[threshold] = f;
                //System.out.print(" d"+d_det+" f"+d_fp);
               
                
                
                
                
                
                //detection_axis[threshold] = d;
                //if(d_det >=8){
                //System.out.println(actual +" " +label + " " + score);

                // + " " + spam_as_spam + " " + totalSpam + " "
                //+ legit_as_spam + " " + totalNonSpam);
                //}
            }//for each threshold

            //gain:
            //need to fill in the outside
            if(gain_axis[0] ==-1){
                gain_axis[0] = 0;
            }
            if(gain_axis[1000] == -1){
                gain_axis[1000] = 100;
            }
            int left,right,gap;
            double valuegap;
            for(int step =1;step < 1000;step++){
                if(gain_axis[step]!=-1){
                    continue;
                }
                left = step-1;
                //so step-1 has left value, now to find rightmost
                while(gain_axis[step]==-1){
                    step++;
                    if(step>999)
                        break;
                }
                right = step;
                
                //now to fill in the parts between left/right
                gap = right-left;
                valuegap = gain_axis[right] - gain_axis[left];
                
                for(int h=1;h<gap;h++){
                    gain_axis[left+h] = gain_axis[left]+h*valuegap/gap;
                }
                
                step = right;
            }
            double total_gain = 0;
            //calculate the gain
            int glen = gain_axis.length;
            for(int j=0;j<glen/10;j++){
             //   System.out.print(gain_axis[j]+" ");
                total_gain += ((glen-j)*gain_axis[j])/glen;
            }
            
            System.out.println("gain is: "+total_gain);
            
            
            
            
            
            
            //if we want to only draw combinations
            if (OnlyCombo) {
                if (namei >= resultsSet.length - NUMBERCOMBO) {
                    ROCCollection.addSeries(rocpoints);
                }
            } else {
                ROCCollection.addSeries(rocpoints);
            }

            //add to graph
        }//end for loop for each name

        /*
         * public static JFreeChart createXYLineChart(java.lang.String title,
         * java.lang.String xAxisLabel, java.lang.String yAxisLabel, XYDataset
         * dataset, PlotOrientation orientation, boolean legend, boolean
         * tooltips, boolean urls)
         */

        ROCchart = ChartFactory.createXYLineChart("ROC Curve",
                "Percent FP Rate", "Precent Detection Rate", ROCCollection,
                PlotOrientation.VERTICAL, true, true, false);
        JFreeChart ScoreProbChart = ChartFactory.createXYLineChart(
                "Score Probability", "Raw Scores", "Count",
                scoreProbabilityDistribution, PlotOrientation.VERTICAL, true,
                false, false);
        ScoreProbChart.getXYPlot().getDomainAxis().setRange(-1, 202);
        ScoreProbChart.getXYPlot().getRangeAxis().setRange(0, 100);
        //set the rnederer
        XYItemRenderer perItemRender = ROCchart.getXYPlot().getRenderer();
        XYItemRenderer perItemRender2 = ScoreProbChart.getXYPlot()
                .getRenderer();

        for (int i = 0; i < chooseNames.size(); i++) {
            
            perItemRender.setSeriesStroke(i, strokeList[i % strokeList.length]);
            perItemRender.setSeriesPaint(i, getColor(i));
            perItemRender2.setSeriesStroke(i, strokeList[i % strokeList.length]);
            perItemRender2.setSeriesPaint(i, getColor(i));
            
            //perItemRender.setSeriesShape(i,shapeList[i % strokeList.length]);
            
            //perItemRender.setBaseItemLabelsVisible(true);
            //perItemRender.setItemLabelsVisible(true);
        }
       
        
        //TODO: figure out how to replace in 1.0.1 from earlier version of the code
        // ((LegendTitle) ROCchart.getLegend()).setDisplaySeriesLines(true);
    
    //    ((LegendTitle) ScoreProbChart.getLegend())
     //           .setDisplaySeriesLines(true);

        //set the axis
        //we would like the x to be 0-5
        //and y 70-100
        zoomROC(zoom);
        //was createhistogram
        JFreeChart ScoreChart = ChartFactory.createScatterPlot("Score Spread",
                "Raw Scores", "Count", scoreDistribution,
                PlotOrientation.VERTICAL, true, false, false);
        ScoreChart.getXYPlot().getRangeAxis().setRange(-2, 203);
        
       //show 700-1400
        ScoreChart.getXYPlot().getDomainAxis().setRange((runningtotal>1800)?700:0,(runningtotal>1800)?1800:runningtotal);
        ScoreChart.getXYPlot().getRenderer().setShape(new Rectangle(new Dimension(3,3)));
        JPanel resultpane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        resultpane.setLayout(gridbag);
        constraints.insets = new Insets(2, 2, 2, 2);

        ChartPanel r1 = new ChartPanel(ROCchart);
        ChartPanel r2 = new ChartPanel(ScoreChart);
        ChartPanel r3 = new ChartPanel(ScoreProbChart);

        r1.setPreferredSize(new Dimension(480, 350));
        r2.setPreferredSize(new Dimension(650, 350));
        r3.setPreferredSize(new Dimension(300, 300));

        //viewExampleTable.setPreferredSize(new Dimension(480,200));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(r1, constraints);

        resultpane.add(r1);

        
        constraints.gridwidth =1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        gridbag.setConstraints(r3, constraints);
        resultpane.add(r3);

        JScrollPane rollingTable = new JScrollPane();
        //		viewExampleTable.setPreferredScrollableViewportSize(new
        // Dimension(470, 350));
        rollingTable.setViewportView(viewExampleTable);
        rollingTable.setPreferredSize(new Dimension(480, 200));
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(rollingTable, constraints);
        resultpane.add(rollingTable);

        //add buttons above table
        JButton removeObv = new JButton("Remove easy examples");
        removeObv
                .setToolTipText("Removes examples which agree with real results");
        removeObv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BusyWindow BW = new BusyWindow("Working", "really working",false);
                BW.setVisible(true);
                int count = 0;
                int columns = resultTablemodel.getColumnCount();
                for (int i = resultTablemodel.getRowCount() - 1; i >= 0; i--) {
                    boolean isSpam = false;
                    boolean othersAgree = true;

                    //get the real label
                    if (((String) resultTablemodel.getValueAt(i, c_real))
                            .startsWith(MLearner.SPAM_LABEL)) {
                        isSpam = true;

                    }

                    //compare to all others
                    for (int j = c_classifiers; j < columns; j++) {
                        Point p = (Point) resultTablemodel.getValueAt(i, j);

                        if (isSpam) {
                            if (p.y != MLearner.SPAM_CLASS) {
                                othersAgree = false;
                                break;
                            }
                        } else {
                            if (p.y == MLearner.SPAM_CLASS) {
                                othersAgree = false;
                                break;
                            }
                        }

                    }

                    //drop if the same
                    if (othersAgree) {
                        resultTablemodel.removeRow(i);
                        count++;
                    }

                    //count how many
                    //need to see if real column and rest columns look the same
                    //	if(resultTablemodel.get)

                    //TODO:
                }
                System.out.println("removed " + count);
                BW.setVisible(false);

            }
        });
        JButton zoombutton = new JButton("Zoom ROC");
        zoombutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (zoom) {
                    zoom = false;
                } else {
                    zoom = true;
                }

                zoomROC(zoom);

            }
        });

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;

        JPanel buttons = new JPanel();

        buttons.add(removeObv);
        buttons.add(zoombutton);
        gridbag.setConstraints(buttons, constraints);
        resultpane.add(buttons);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        //		add buttons above table
        /*
         * JButton doCombo = new JButton("do combinations");
         * doCombo.setToolTipText("adds combination results to the table");
         * doCombo.addActionListener(new ActionListener() { public void
         * actionPerformed(ActionEvent e) { BusyWindow BW = new
         * BusyWindow("Working", "Adding Combinations"); BW.setVisible(true);
         * 
         * resultTablemodel.addColumn("Equal");
         * 
         * BW.setVisible(false); } }); constraints.gridx = 1; constraints.gridy =
         * 1; constraints.gridwidth = 1; gridbag.setConstraints(doCombo,
         * constraints); resultpane.add(doCombo);
         */
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        gridbag.setConstraints(r2, constraints);
        resultpane.add(r2);
        
        JScrollPane jroller = new JScrollPane();
        jroller.setViewportView(resultpane);

        JFrame jf = new JFrame("Result view");
        JScrollPane jc = new JScrollPane(resultpane);
        jc.setSize(650,500);
        jf.getContentPane().add(jc);
        jf.getContentPane().setSize(new Dimension(500, 650));
        jf.pack();
        jf.setVisible(true);
    }

    private void zoomROC(boolean tozoom) {
        if (tozoom) {
            ROCchart.getXYPlot().getDomainAxis().setRange(0, 1);
            ROCchart.getXYPlot().getRangeAxis().setRange(85, 100);
        } else {
            ROCchart.getXYPlot().getDomainAxis().setRange(0, 10);
            ROCchart.getXYPlot().getRangeAxis().setRange(50, 100);

        }

    }

    private static Stroke[] strokeList = new Stroke[7];
    static {
        float[] fltTwo = { 2, 5,2,5};
        float[] fltFive = { 5,3, 5,3 };
        float[] fltSeven = { 7, 9,7,9 };
        float[] fltNine = { 9, 9 };
//BasicStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase) 
        strokeList[0] = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 10.0f, fltNine, 3);
        strokeList[1] = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f,
                new float[] {10, 5, 5, 5}, 2); 
        		//BasicStroke.CAP_BUTT,
                //BasicStroke.JOIN_MITER, 10.0f, fltTwo, 0f);
        strokeList[2] = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 1.0f, fltTwo, 0);
        strokeList[3] = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 2.0f, fltFive, 2);
        strokeList[4] = new BasicStroke(3.0f);
        strokeList[5] = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 2.0f, fltSeven, 2);
        strokeList[6] = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0f, fltTwo, 2);
        

    }
  //TODO:  
    /*private static Shape[] shapeList = new Shape[6];
    static {
      
    	int diamondX[] = {8,16,4,0};
    	int diamondY[] = {0,4,16,4};
    	
    	
        shapeList[0] =  new Ellipse2D.Double(-3, -3, 6, 6);
        	
        shapeList[1] = new Rectangle(new Dimension(4,4));	
        shapeList[2] = new Rectangle(new Dimension(2,2));
        shapeList[3] = 	new Polygon(diamondX,diamondY,4);
//new Rectangle(new Dimension(1,3));
        shapeList[4] = new Rectangle(new Dimension(3,5));
        shapeList[5] = new Rectangle(new Dimension(3,3));

    }*/

    private Color getColor(int choose) {
        switch (choose) {
        case (0):
            return Color.RED;
        case (1):
            return Color.BLUE;
        case (2):
            return Color.ORANGE;
        case (3):
            return Color.BLACK;
        case (4):
            return Color.GREEN;
        case (5):
            return Color.MAGENTA;
        case (6):
            return Color.DARK_GRAY;
        default:
            return Color.PINK;
        }
    }

    private String getTypeString(String s) {
        int a = s.indexOf("_");
        int b = s.indexOf("_", a + 1);
        int type = Integer.parseInt(s.substring(a + 1, b));

        return MLearner.getTypeString(type);
    }
/*
    private int getTypeInt(String s) {
        int a = s.indexOf("_");
        int b = s.indexOf("_", a + 1);
        int type = Integer.parseInt(s.substring(a + 1, b));
        return type;
    }
*/
    static final int NUMBERBINS = 6;

    static final int BINWIDTH = 1 + (201 / NUMBERBINS); //+1 because of
    static private double beta = .004; //we will subtract but can multiply i guess.
	static private double reward = .001;
    // rounding

    //combinations
    /**
     * adds the combination scores to the current mix of regular scores.
     *  
     */

    private resultScores[][] addCombinations(resultScores[][] pre_results) {

        //to hold the new one.
        resultScores[][] newResults = new resultScores[pre_results.length
                + NUMBERCOMBO][];

        HashMap TestExamplesSeen = new HashMap();
        HashMap TrainExamplesSeen = new HashMap();
        int equalColumn = pre_results.length;
        int nbayesColumn = equalColumn + 1;
        int oracleColumn = equalColumn + 2;
        int ndimensionalColumn = equalColumn + 3;
        int weightedMajorityColum = equalColumn+4;
        /*
         * for(int i=0;i <lengths.length;i++){ lengths[i] =
         * pre_results[i].length; if(lengths[i]>max){ max = lengths[i]; } }
         */
        //setup for the naive bayes
        System.out.println("pre len: " + pre_results.length);
        double[] prob_classifyspam_given_spam = new double[2*pre_results.length];
        double[] prob_classifyspam_given_legit = new double[2*pre_results.length];
        double[] prob_classifylegit_given_spam = new double[2*pre_results.length];
        double[] prob_classifylegit_given_legit = new double[2*pre_results.length];
        //for 2 dimensional
        //use of single array for n squared one
        HashMap ND_spam_scores = new HashMap();//[] = new int[NUMBERBINS * NUMBERBINS];
        HashMap ND_good_scores = new HashMap();//[] = new int[NUMBERBINS * NUMBERBINS];
        int number_spam = 0;
        int number_legit = 0;
        //for weighted majority
        double[] weightsClassifier = new double[2*pre_results.length];
        boolean[] votesSpam = new boolean[2*pre_results.length];
        
        for(int j=0;j<weightsClassifier.length;j++){
            weightsClassifier[j] = 9000000;
            votesSpam[j] = false;
        }
        
        
        int score;
        boolean useMax = true;
        //do setup
        for (int jth = 0; jth < pre_results.length; jth++) {
            newResults[jth] = new resultScores[pre_results[jth].length];
            for (int i = 0; i < pre_results[jth].length; i++) {

                newResults[jth][i] = pre_results[jth][i];
                String mailref = pre_results[jth][i].getMailref();
               
                if (pre_results[jth][i].isTesting()) {
                    if (TestExamplesSeen.containsKey(mailref)) {
                        ArrayList alist = (ArrayList) TestExamplesSeen.get(mailref);
                        alist.add(newResults[jth][i]);
                        TestExamplesSeen.put(mailref, alist);
                    } else {
                        ArrayList alist = new ArrayList();
                        alist.add(newResults[jth][i]);
                        TestExamplesSeen.put(mailref, alist);
                    }
                } else {
                    //its training example
                    if (TrainExamplesSeen.containsKey(mailref)) {
                        ArrayList alist = (ArrayList) TrainExamplesSeen
                                .get(mailref);
                        alist.add(newResults[jth][i]);
                        TrainExamplesSeen.put(mailref, alist);
                    } else {
                        ArrayList alist = new ArrayList();
                        alist.add(newResults[jth][i]);
                        TrainExamplesSeen.put(mailref, alist);
                    }
                }
            }
        }
        /***********************************
         * TRAINING
         */
        //so now the training and test sets are batched into 2 groups.
        //and we've copied the original into the new array.
        int jth = 0;
        double w_spam,w_good;
        boolean isSpam =false;
        String ndimlocation;
        for (Iterator examplesIter = TrainExamplesSeen.keySet().iterator(); examplesIter
                .hasNext(); jth++) {

            w_spam = 0;
            w_good = 0;
            ArrayList alist = (ArrayList) TrainExamplesSeen.get(examplesIter
                    .next());
            ndimlocation = "";
            isSpam = false;
            for (int a = 0; a < alist.size(); a++) {

                resultScores currentOne = ((resultScores) alist.get(a));

                if (currentOne.getLabel().startsWith(MLearner.SPAM_LABEL)) {
                    score = 101 + currentOne.getScore();
                } else {
                    score = 100 - currentOne.getScore();
                }

                //						get the score being generated

                //spam example
                if (a == 0) {
                    //check only first, since the rest are the same
                    if (currentOne.getRealLabel().startsWith(
                            MLearner.SPAM_LABEL)) {

                        number_spam++;
                    } else {
                        number_legit++;
                    }
                }

                ndimlocation += score/BINWIDTH;
                
                if (currentOne.getRealLabel().startsWith(MLearner.SPAM_LABEL)) {
                    isSpam = true;
                    if (score < 101) {
                        prob_classifylegit_given_spam[a]++;
                    } else {
                        prob_classifyspam_given_spam[a]++;
                    }
                } else {
                    isSpam = false;
                    if (score < 101) {
                        prob_classifylegit_given_legit[a]++;
                    } else {
                        prob_classifyspam_given_legit[a]++;
                    }
                }
                
                //weighted majority stuff
                if(score < 101){
                    //good vote
                    w_good += weightsClassifier[a];
                    votesSpam[a] = false;
                }else{
                    w_spam += weightsClassifier[a];
                    votesSpam[a] = true;
                }
                
                
                
            }//end all scores
            //no to do combo for training
            if(isSpam){
                if(ND_spam_scores.containsKey(ndimlocation)){
                    Integer val = (Integer)ND_spam_scores.get(ndimlocation);
                    ND_spam_scores.put(ndimlocation,new Integer(val.intValue()+1));
                }
                else{
                    ND_spam_scores.put(ndimlocation,new Integer(+1));
                }
            }else{
                if(ND_good_scores.containsKey(ndimlocation)){
                    Integer val = (Integer)ND_good_scores.get(ndimlocation);
                    ND_good_scores.put(ndimlocation,new Integer(val.intValue()+1));
                }
                else{
                    ND_good_scores.put(ndimlocation,new Integer(+1));
                }
            }
            //do the weighted majority here
            if( (isSpam && w_spam >= w_good) || (!isSpam && w_good >=w_spam)){
                //nothgin to do 
            }else{
                //need to adjust
                if(isSpam){
                    
                    for (int a = 0; a < alist.size(); a++) {
                        if(votesSpam[a])
                            weightsClassifier[a]+=reward;
                        else
                            weightsClassifier[a]-=beta;
                    }
                    
                }
                else{
                    for (int a = 0; a < alist.size(); a++) {
                        if(votesSpam[a])
                            weightsClassifier[a]-=beta;
                        else
                            weightsClassifier[a]+=reward;
                    }
                }
                
                
            }
            
            
            

        }//end training
        newResults[equalColumn] = new resultScores[TestExamplesSeen.size()];
        newResults[oracleColumn] = new resultScores[TestExamplesSeen.size()];
        newResults[nbayesColumn] = new resultScores[TestExamplesSeen.size()];
        newResults[ndimensionalColumn] = new resultScores[TestExamplesSeen
                .size()];
        newResults[weightedMajorityColum] = new resultScores[TestExamplesSeen
                                                          .size()];
                                                  
        //now for test list
        jth = 0;
        int ew_sum, oracle, nb_min, nb_max;
        double prob_spam;
        double prob_legit;
        resultScores rs = null;
        String mailref = "", reallabel = "";
        /****************
         * TEST ING
         */
        Iterator examplesIter = TestExamplesSeen.keySet().iterator();
        for (; examplesIter.hasNext(); jth++) {
           ndimlocation="";
            ew_sum = 0;
            oracle = 0;
            nb_min = 205;
            nb_max = -1;
            prob_spam = 0;
            prob_legit = 0;
            w_spam = 0;
            w_good = 0;
           
            ArrayList alist = (ArrayList) TestExamplesSeen.get(examplesIter
                    .next());
            for (int a = 0; a < alist.size(); a++) {

                resultScores currentOne = ((resultScores) alist.get(a));
                if (currentOne.getLabel().startsWith(MLearner.SPAM_LABEL)) {
                    score = 101 + currentOne.getScore();
                } else {
                    score = 100 - currentOne.getScore();
                }
                ndimlocation += score/BINWIDTH;
                if (a == 0) {

                    mailref = currentOne.getMailref();
                    reallabel = currentOne.getRealLabel();
                    oracle = score;
                    if (currentOne.getRealLabel().startsWith(
                            MLearner.SPAM_LABEL)) {
                        useMax = true;
                    } else {
                        useMax = false;
                    }
                }
                //do equal weights
                ew_sum += score;
                //oracle judge
                if (useMax) {
                    if (oracle < score) {
                        oracle = score;
                    }
                } else {
                    if (oracle > score) {//sd2.sum>100 && sd2.sum>score){
                        oracle = score;
                    }
                }
                //do one dim nb
                if (nb_max < score) {
                    nb_max = score;
                }
                if (nb_min > score) {
                    nb_min = score;
                }
                if (score < 101) {
                    prob_legit += Math.log(prob_classifylegit_given_legit[a]
                            / number_legit);
                    prob_spam += Math.log(prob_classifylegit_given_spam[a]
                            / number_spam);
                } else {
                    prob_legit += Math.log(prob_classifyspam_given_legit[a]
                            / number_legit);
                    prob_spam += Math.log(prob_classifyspam_given_spam[a]
                            / number_spam);
                }
                //do weighted majority
                if(score < 101){
                    //good vote
                    w_good += weightsClassifier[a];
                   
                }else{
                    w_spam += weightsClassifier[a];
                   
                }

            }//end for each example in combination individual
            //set equal
            ew_sum = ew_sum / alist.size();
            if (ew_sum < 101) {
                rs = new resultScores(MLearner.NOT_INTERESTING_CLASS,
                        100 - ew_sum, mailref);

            } else {
                rs = new resultScores(MLearner.SPAM_CLASS, ew_sum - 101,
                        mailref);
            }
            rs.setRealLabel(reallabel);
            newResults[equalColumn][jth] = rs;
            //set oracle
            if (oracle < 101) {
                rs = new resultScores(MLearner.NOT_INTERESTING_CLASS,
                        100 - oracle, mailref);
            } else {
                rs = new resultScores(MLearner.SPAM_CLASS, oracle - 101,
                        mailref);
            }
            rs.setRealLabel(reallabel);
            newResults[oracleColumn][jth] = rs;
            //set nb
            //			score = 201* prob_spam[offset] / (prob_spam[offset]+
            // prob_legit[offset]);
            if (prob_spam < prob_legit) {
                rs = new resultScores(MLearner.NOT_INTERESTING_CLASS,
                        100 - nb_min, mailref);
            } else {
                rs = new resultScores(MLearner.SPAM_CLASS, nb_max - 101,
                        mailref);
            }
            rs.setRealLabel(reallabel);
            newResults[nbayesColumn][jth] = rs;
            //set 2 dimentional nb
            prob_spam = prob_legit =0;
            if(ND_good_scores.containsKey(ndimlocation)){
                prob_legit = ((Integer)ND_good_scores.get(ndimlocation)).intValue();
            }
            if(ND_spam_scores.containsKey(ndimlocation)){
                prob_spam = ((Integer)ND_spam_scores.get(ndimlocation)).intValue();
            }
           // int sc = (int)(201 * (1. + prob_spam) / (prob_spam + prob_legit + NUMBERBINS));
            
            if (prob_spam < prob_legit) {
                rs = new resultScores(MLearner.NOT_INTERESTING_CLASS,
                        100 - nb_min, mailref);
            } else {
                rs = new resultScores(MLearner.SPAM_CLASS, nb_max - 101,
                        mailref);
            }
            rs.setRealLabel(reallabel);
            newResults[ndimensionalColumn][jth] = rs;
            //weighted majority
            if(w_spam < w_good){
                rs = new resultScores(MLearner.NOT_INTERESTING_CLASS,
                        100 - nb_min, mailref);
            } else {
                rs = new resultScores(MLearner.SPAM_CLASS, nb_max - 101,
                        mailref);
            }
            rs.setRealLabel(reallabel);
            newResults[weightedMajorityColum][jth] = rs;
            
            
        }

        return newResults;

    }

    /*private class sumdiv {

        public int sum;

        public int div;

        String real;

        public sumdiv() {

        }

        public sumdiv(int sc) {
            sum = sc;
        }

        public sumdiv(int s, int d, String r) {
            sum = s;
            div = d;
            real = r;
        }

    }*/

}