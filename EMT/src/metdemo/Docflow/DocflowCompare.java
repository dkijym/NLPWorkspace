/**
 * @(#)DocflowCompare.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/28
 */

package metdemo.Docflow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.ArrayList;

import metdemo.Docflow.Sparse.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class DocflowCompare extends JFrame{

	JPanel chart1;
	JPanel chart2;
	JPanel textpane;
	JScrollPane schart1;
	JScrollPane schart2;
	JScrollPane stextpane;
	JTextPane textInfo;

    public DocflowCompare() {
    }
    
    /**
     *	compare two Word documents
     */
    public void compareDoc(){
    	//.setPreferredSize(new java.awt.Dimension(300, 100));
    }
    
    /**
     *	compare two attachments, any type
     */
    public void compareAtt(byte[] b1, byte[] b2, String name1, String name2, String textborder){
    	schart1 = new JScrollPane();
        schart2 = new JScrollPane();
        
        //byte value
        plotByte(b1, schart1, name1, "Byte Value", b2, name2, true);

		//entropy
		int entropyGram = 50;
	 	plotDouble(computeEntropy(b1, entropyGram), schart2
	 			, name1, "Entropy"
                , computeEntropy(b2, entropyGram), name2, true);
                                
     	//change to byte arrays
     	int gramSize = 5;//default
     	boolean[] foreign = findForeign(b1, b2, gramSize, "");
     	
     	//set the text styles
     	ArrayList<String> textlist = new ArrayList<String>();
     	ArrayList<String> stylelist = new ArrayList<String>();
     	textlist.add(getHeader().toString());
        stylelist.add("large");
        for(int i=0; i<b2.length; i+=16){//show 16 bytes on each line
        	getDisplayText(b2, i, foreign, textlist, stylelist);
        }
     	String[] text = (String[])textlist.toArray(new String[textlist.size()]);
        String[] styles = (String[])stylelist.toArray(new String[stylelist.size()]);
        textInfo = new JTextPane();
		StyledDocument doc = textInfo.getStyledDocument();
        addStylesToDocument(doc);
        textInfo.setBackground(new Color(153, 255, 255));
        try {
            for (int i=0; i < text.length; i++) {
                doc.insertString(doc.getLength(), text[i],
                                 doc.getStyle(styles[i]));
            }
            textInfo.select(0,0);
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert text into text pane.");
        }
        
     	displayPanel(textborder);
    }
    
    /**
     *	compare the text of two emails
     */
     public void compareText(char[] t1, char[] t2, String name1, String name2, String textborder){
     	
     	//setup the scrollpane for the charts
     	byte[] b1 = new byte[t1.length];
     	for(int i=0;i<t1.length;++i)b1[i] = (byte)t1[i];
     	byte[] b2 = new byte[t2.length];
     	for(int i=0;i<t2.length;++i)b2[i] = (byte)t2[i];
     	schart1 = new JScrollPane();
        schart2 = new JScrollPane();
        
        //byte value
        plotByte(b1, schart1, name1, "Byte Value", b2, name2, true);

		//entropy
		int entropyGram = 50;
	 	plotDouble(computeEntropy(b1, entropyGram), schart2
	 			, name1, "Entropy"
                , computeEntropy(b2, entropyGram), name2, true);
                                
     	//change to byte arrays
     	int gramSize = 5;//default
     	boolean[] foreign = findForeign(b1, b2, gramSize, "");
     	
     	//set the text styles
     	ArrayList<String> textlist = new ArrayList<String>();
     	ArrayList<String> stylelist = new ArrayList<String>();
     	getDisplayText(t2, foreign, textlist, stylelist);
     	String[] text = (String[])textlist.toArray(new String[textlist.size()]);
        String[] styles = (String[])stylelist.toArray(new String[stylelist.size()]);
        textInfo = new JTextPane();
		StyledDocument doc = textInfo.getStyledDocument();
        addStylesToDocument(doc);
        textInfo.setBackground(new Color(153, 255, 255));
        try {
            for (int i=0; i < text.length; i++) {
                doc.insertString(doc.getLength(), text[i],
                                 doc.getStyle(styles[i]));
            }
            textInfo.select(0,0);
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert text into text pane.");
        }
        
     	displayPanel(textborder);
     }
     
    
    
    
    /**
	 *	display it
	 */
	public void displayPanel(String textborder){
		
		JPanel panelToSetup = new JPanel();
		GridBagConstraints GBconstraints = new GridBagConstraints();
		GridBagLayout GBLayout = new GridBagLayout();
		panelToSetup.setLayout(GBLayout);
		
		//the charts
		chart1 = new JPanel();
		chart1.add(schart1);
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 0;
		GBconstraints.gridwidth = 1;
		GBconstraints.insets = new Insets(5,5,5,5);
		GBLayout.setConstraints(schart1, GBconstraints);
		panelToSetup.add(schart1);
        
        chart2 = new JPanel();
		chart2.add(schart2);
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 1;
		GBconstraints.gridwidth = 1;
		GBconstraints.insets = new Insets(5,5,5,5);
		GBLayout.setConstraints(schart2, GBconstraints);
		panelToSetup.add(schart2);
		
		//the textpane
        textpane = new JPanel();
        stextpane = new JScrollPane();
		textpane.setBorder(javax.swing.BorderFactory.createTitledBorder(textborder));
        textpane.setPreferredSize(new Dimension(680, 300));
        stextpane.setPreferredSize(new Dimension(660, 280));
        stextpane.setViewportView(textInfo);
        textpane.add(stextpane);
        GBconstraints.gridx = 0;
		GBconstraints.gridy = 2;
		GBconstraints.gridwidth = 1;
		GBconstraints.insets = new Insets(5,5,5,5);
		GBLayout.setConstraints(textpane, GBconstraints);
		panelToSetup.add(textpane);
		
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.getContentPane().add(panelToSetup,BorderLayout.CENTER);
		
		this.pack();
		this.setVisible(true);

	}
	
	/**
     *  To setup the style of the TextPane
     *
     */
    private void addStylesToDocument(StyledDocument doc){
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(regular, "Courier");
        Style s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 12);
        s = doc.addStyle("largeb", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setBold(s, true);
        

    }
	
	public void resetPlots(){
        //double[] data = {1.0};
        double[] data = new double[1000];
        for(double i=0;i<1000;++i){
        	data[(int)i] = (double)(i%70 + 19);
        }
        schart1 = new JScrollPane();
        schart2 = new JScrollPane();
        plotDouble(data, schart1, "Byte Value", "Byte Value", null, null, false);
        plotDouble(data, schart2, "Entropy", "Entropy Distribution", null, null, false);
        
    }
    
    public void plotDouble(final double[] data, JScrollPane pane
            , final String name, final String title
            , final double[] data2, final String name2
            , final boolean legend){
        try{
            final XYSeries series = new XYSeries(name);
            for(int i=0;i<data.length;++i)
                series.add(i, data[i]);
            final XYSeriesCollection xydata = new XYSeriesCollection();
            xydata.addSeries(series);
            if(data2 != null){
                final XYSeries series2 = new XYSeries(name2);
                for(int i=0;i<data2.length;++i)
                series2.add(i, data2[i]);
                xydata.addSeries(series2);
            }
            
            final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X", 
                "Y", 
                xydata,
                PlotOrientation.VERTICAL,
                legend,
                false,
                false
            );

            //set the plot...
            final XYPlot plot = (XYPlot)chart.getPlot();
            final NumberAxis axis = (NumberAxis)plot.getRangeAxis();
            axis.setAutoRangeIncludesZero(false);
            axis.setAutoRangeMinimumSize(1.0);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            //add to the chart
            final ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(500, 150));
            pane.setViewportView(cp);

        }
        catch(Exception e){
            System.out.println("Plot Entropy:"+e);
        }
    }
    
    public void plotByte(final byte[] data, final JScrollPane scrolpane
            ,String name, String title
            , final byte[] data2, final String name2
            , final boolean legend){
        try{
            final XYSeries series = new XYSeries(name);
            for(int i=0;i<data.length;++i)
                series.add(i, data[i]);
            final XYSeriesCollection xydata = new XYSeriesCollection();
            xydata.addSeries(series);
            if(data2 != null){
                final XYSeries series2 = new XYSeries(name2);
                for(int i=0;i<data2.length;++i)
                series2.add(i, data2[i]);
                xydata.addSeries(series2);
            }
            
            final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X", 
                "Y", 
                xydata,
                PlotOrientation.VERTICAL,
                legend,
                false,
                false
            );

            final XYPlot plot = (XYPlot)chart.getPlot();
            final NumberAxis axis = (NumberAxis)plot.getRangeAxis();
            axis.setAutoRangeIncludesZero(false);
            axis.setAutoRangeMinimumSize(1.0);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            final ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new Dimension(500, 150));
            scrolpane.setViewportView(cp);
        }
        catch(Exception e){
            System.out.println("Plot Entropy:"+e);
        }
    }
    
     /**
     *  find foreign gram in b from a
     */
    public boolean[] findForeign(byte[] a, byte[] b, int gram, String name){
        boolean [] foreign = new boolean[b.length];
        ExperimentObj train = new ExperimentObj(gram, name, false);
        train.update(a, null);
        byte[] test = new byte[gram];
        for(int i=0;i<b.length-gram;++i){
        	System.arraycopy(b, i, test, 0, gram);
        	if(!train.checkSingle(test))foreign[i] = true;
        }
        
        //the last window
        for(int i=b.length-gram;i<b.length;++i)
        	if(!train.checkSingle(test))foreign[i] = true;
        
        return foreign;
    }
    
    /**
     *  get the text with some bold string, for the foreign gram thing
     */
    public void getDisplayText(final char[] data, final boolean[] foreign
            , ArrayList<String> textlist, ArrayList<String> stylelist){

        boolean isBold = true;//true = good, false = bad
        StringBuffer text = new StringBuffer("");
		StringBuffer buffer = new StringBuffer("");
		
		for(int i=0;i<data.length;++i){		
        	//setup bold or not
        	if(!isBold){//last not bold
                if(!foreign[i]){//current not bold
                    //append string buffer
                    buffer.append(data[i]);
                }
                else{//current bold
                    //add last to list
                    textlist.add(buffer.toString());
                    stylelist.add("large");
                    buffer = new StringBuffer("");//reset;
                    buffer.append(data[i]);
                    isBold = true;
                }
            }
            else{//last bold
                if(foreign[i]){//current bold
                    //append string buffer
                    buffer.append(data[i]);
                }
                else{//current not bold
                    //add last to list
                    textlist.add(buffer.toString());
                    stylelist.add("largeb");
                    buffer = new StringBuffer("");//reset;
                    buffer.append(data[i]);
                    isBold = false;
                }
            }
        }//for(int i=0;i<data.length;++i)
        
        //add the last run
        if(isBold){//last bold
            textlist.add(buffer.toString());
            stylelist.add("largeb");
        }
        else{//last not bold
            textlist.add(buffer.toString());
            stylelist.add("large");
        }
        
    }
    
    public void getDisplayText(final byte[] value, final int index, final boolean[] foreign
            , ArrayList<String> textlist, ArrayList<String> stylelist){

        boolean isBold = true;//true = good, false = bad
                
        //the offset
        StringBuffer header = new StringBuffer("");
        StringBuffer indexs = getHexNumber(index);
        header.append(indexs);
        header.append(" || ");
        textlist.add(header.toString());
        stylelist.add("large");
                    
        //the ASCII value
        StringBuffer asciivalue = new StringBuffer("");
        
        //a temporary buffer
        StringBuffer buffer = new StringBuffer("");
        
        //the numericalvalue
        for(int i=0;i<16;++i){
            
            int tmpvalue = value[index+i];
            if(tmpvalue<0)tmpvalue+=256;

            //setup bold or not
            /********************************/
            if(!isBold){//last not bold
                if(!foreign[index+i]){//current not bold
                    //append string buffer
                    if(tmpvalue<16)buffer.append("0");
                    buffer.append(Integer.toHexString(tmpvalue).toUpperCase());
                    buffer.append(" ");
                }
                else{//current bold
                    //add last to list
                    textlist.add(buffer.toString());
                    stylelist.add("large");
                    buffer = new StringBuffer("");//reset
                    if(tmpvalue<16)buffer.append("0");
                    buffer.append(Integer.toHexString(tmpvalue).toUpperCase());
                    buffer.append(" ");
                    isBold = true;
                }
            }
            else{//last bold
                if(foreign[index+i]){//current bold
                    //append string buffer
                    if(tmpvalue<16)buffer.append("0");
                    buffer.append(Integer.toHexString(tmpvalue).toUpperCase());
                    buffer.append(" ");
                }
                else{//current not bold
                    //add last to list
                    textlist.add(buffer.toString());
                    stylelist.add("largeb");
                    buffer = new StringBuffer("");//reset
                    if(tmpvalue<16)buffer.append("0");
                    buffer.append(Integer.toHexString(tmpvalue).toUpperCase());
                    buffer.append(" ");
                    isBold = false;
                }
            }
            /********************************/
            
            
            //setup the ascii code
            if(tmpvalue>=32 && tmpvalue<=126){
                asciivalue.append((char)tmpvalue);
            }
            else{
                asciivalue.append(".");
            }
        }
        
        //add the last run
        if(isBold){//last bold
            textlist.add(buffer.toString());
            stylelist.add("largeb");
        }
        else{//last not bold
            textlist.add(buffer.toString());
            stylelist.add("large");
        }
        
        StringBuffer tail = new StringBuffer("");
        tail.append(" || ");
        tail.append(asciivalue);
        tail.append("\n");
        textlist.add(tail.toString());
        stylelist.add("large");
    }
    
    public StringBuffer getHeader(){
        StringBuffer info = new StringBuffer("OFFSET   || BYTE VALUE" +
            "                                       ||   ASCII VALUE\n");
        info.append("================================================================================\n");
        return info;
    }
    
    private StringBuffer getHexNumber(final long num){
        StringBuffer hexnumber = new StringBuffer("00000000");
        String hexindex = Long.toHexString(num).toUpperCase();
        int end = 8;
        int start = end - hexindex.length();
        hexnumber.replace(start, end, hexindex);
        return hexnumber;
    }
    
    public double[] computeEntropy(byte[] value, int gram){
        double[] entropy = new double[value.length];
        int index = 0;
        double entropy_size = 0;
        /** a bit map (also a counter) to check whether the 
         * byte value exists in current array
         */
        int[] exist_list = new int[256];
        //pre build
        for(int i=0;i<gram;++i){
            int curr_value = (int)value[i];
            if(curr_value<0)curr_value += 256;
            if(++exist_list[curr_value] == 1)
                ++entropy_size;
            
            entropy[index++] = entropy_size/(double)gram;
        }

        for(int i=gram;i<value.length;++i){
            int curr_value = (int)value[i];//the current value to append
            if(curr_value<0)curr_value += 256;
            int last_value = (int)value[i-gram];//the first value to remove
            if(last_value<0)last_value += 256;

            //remove the last one
            //if it is the only one at thie value
            if(--exist_list[last_value] == 0)
                --entropy_size;

            //add the current one
            //this value doesn't exist at all
            if(++exist_list[curr_value] == 1){
                ++entropy_size;
            }
            
            entropy[index++] = entropy_size/(double)gram;
        }
        return entropy;
    }
    
}