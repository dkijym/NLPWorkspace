/*
 * Created on Oct 13, 2004
 *
 */
package metdemo.Tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import chapman.graphics.JPlot2D;

import metdemo.MachineLearning.MLearner;
import metdemo.dataStructures.WordCount;


/**
 * @author Shlomo Hershkop
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class KeywordWizard extends JPanel {

	private JTextArea m_thresholdNumber;
	private JCheckBox m_ascendingOrder;
	
	private JPlot2D m_histogramGraph;
	private int m_totalwords;
	private JTable m_wordTable;
	static final int i_wordColum = 0;
	static final int i_countColumn = 1;
	static final int i_useColumn = 2;
	private DefaultTableModel dtm;
	//private TableSorter sorter;
	/* for last column if to use the specific word or not. */
	static final Boolean USE = new Boolean(true);
	static final Boolean NOTUSE = new Boolean(false);
	private String keywordFilename;
	private int size;
	
	/**
	 *  
	 */
	public KeywordWizard(StringBuffer input,String kwFilename,boolean toLowerCase) {
		keywordFilename = kwFilename;
		Hashtable<String,Integer> m_keymaps = new Hashtable<String,Integer>();
		StringTokenizer tok = new StringTokenizer(input.toString(), MLearner.tokenizer);
		m_totalwords = 0;
		
		
		
		while (tok.hasMoreTokens()) {
			String key = tok.nextToken();
			if(toLowerCase)
			{
				key = key.toLowerCase();
			}
			m_totalwords++;
			if (m_keymaps.containsKey(key)) {
				Integer count =  m_keymaps.get(key);
				m_keymaps.put(key, count.intValue()+1);
			} else {
				m_keymaps.put(key, 1);
			}
		}
		size = m_keymaps.size();
		String[] columnsa = {"KeyWord", "Count", "Use"};
		dtm = new DefaultTableModel(columnsa, size) {
			public Class getColumnClass(final int column) {
				if (column == i_countColumn) {
					return java.lang.Integer.class;
				} else if (column == i_useColumn) {
					return Boolean.class;
				} else
					return String.class;
			}
			
			
		};
		//sorter = new TableSorter(dtm);

		m_wordTable = new JTable(dtm);//sorter);
		//sorter.setSortColumn(i_countColumn);
		//int step = 0;
		ArrayList wordList = new ArrayList(m_keymaps.size());
		for (Enumeration enumWalker = m_keymaps.keys(); enumWalker.hasMoreElements();) {
			String key = (String) enumWalker.nextElement();
			
			wordList.add(new WordCount(key,((Integer)m_keymaps.get(key)).intValue()));
			//dtm.setValueAt(m_keymaps.get(key), step, i_countColumn);
			//step++;
		}
	    Collections.sort(wordList);
	    dtm.setRowCount(0);
		for(Iterator it = wordList.iterator();it.hasNext();)
		{
			WordCount wc = (WordCount)it.next();
			Object [] rowdata = new Object[3];
			rowdata[0] = wc.getWord();
			rowdata[1] = new Integer(wc.getCount());
			rowdata[2] = USE;
			
			dtm.addRow(rowdata);
		}
		
		
		/*for (Enumeration it = m_keymaps.keys(); it.hasMoreElements();) {
			String key = (String) it.nextElement();
			dtm.setValueAt(key, step, i_wordColum);
			dtm.setValueAt(m_keymaps.get(key), step, i_countColumn);
			dtm.setValueAt(USE, step, i_useColumn);
			step++;
		}*/
		//sorter.sortByColumn(i_countColumn, false);//sort desc
		//sorter.setSortingOn(true);
		JScrollPane rollingTable = new JScrollPane();
		m_wordTable.setPreferredScrollableViewportSize(new Dimension(380, 200));
		rollingTable.setViewportView(m_wordTable);

		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout GridBag = new GridBagLayout();
		setLayout(GridBag);
		constraints.insets = new Insets(2, 2, 2, 2);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		//constraints.weightx = 1;
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;


		GridBag.setConstraints(rollingTable, constraints);
		add(rollingTable);
	
		//totalwords;
		m_histogramGraph = new JPlot2D();
		m_histogramGraph.setPlotType(JPlot2D.BAR);
		m_histogramGraph.setBackgroundColor(Color.white);
		m_histogramGraph.setFillColor(Color.blue);
		m_histogramGraph.setPreferredSize(new Dimension(450, 320));
		m_histogramGraph.setXLabel("Relative Frequencys desc order");
		//plot.setYLabel("Log of relative avg comm time (min)");
		//m_histogram.setXScale(s);

		m_histogramGraph.setFillColor(Color.blue);

		constraints.gridx = 0;
		constraints.gridy = 1;

		GridBag.setConstraints(m_histogramGraph, constraints);
		add(m_histogramGraph);

		JLabel thresholdlabel = new JLabel("Threshold:");
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		GridBag.setConstraints(thresholdlabel, constraints);
		add(thresholdlabel);


		//add a threshld number thing
		m_thresholdNumber = new JTextArea(" 2   ");
		m_thresholdNumber.setPreferredSize(new Dimension(50, 25));
		constraints.gridwidth = 1;
		constraints.gridx = 1;
		constraints.gridy = 2;
		GridBag.setConstraints(m_thresholdNumber, constraints);
		add(m_thresholdNumber);

		//the ascending checkbox
		m_ascendingOrder = new JCheckBox("Checkoff Above Threshold", true);
		m_ascendingOrder.setToolTipText("Will choose those items which are above the threshold");
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		constraints.gridy = 2;
		GridBag.setConstraints(m_ascendingOrder, constraints);
		add(m_ascendingOrder);

		JButton refresh = new JButton("Refresh Histogram Picture");
		refresh.setToolTipText("Refresh the histogram/table based on threshold and other settings");
		refresh.addActionListener(new ActionListener() { /*
														  * (non-Javadoc)
														  * 
														  * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
														  */
			public void actionPerformed(ActionEvent arg0) {
				int threshold = 1;
				try {
					threshold = Integer.parseInt(m_thresholdNumber.getText().trim());
				} catch (NumberFormatException p) {
					System.out.println(p);
					threshold = 1;
				}


				reDrawTables(threshold, m_ascendingOrder.isSelected());

			}
		});

		constraints.gridwidth = 1;
		constraints.gridx = 2;
		constraints.gridy = 3;
		GridBag.setConstraints(refresh, constraints);
		add(refresh);

		JButton addToKeywords = new JButton("Add to Current Keyword List");
		addToKeywords.addActionListener(new ActionListener() { /*
															    * (non-Javadoc)
															    * 
															    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
															    */
			public void actionPerformed(ActionEvent arg0) {
				KeywordPanel kp = new KeywordPanel(keywordFilename);

				int rows = dtm.getRowCount();
				for (int i = 0; i < rows; i++) {

					if (((Boolean) dtm.getValueAt(i, i_useColumn)).booleanValue()) {
						kp.addNewWord((String) dtm.getValueAt(i, i_wordColum));
					}
				}
				kp.saveInput();

			}
		});
		
		
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		constraints.gridy = 4;
		GridBag.setConstraints(addToKeywords, constraints);
		add(addToKeywords);
		
		
		
		JButton newKeywordList = new JButton("Create a new Keyword List");
		newKeywordList.setToolTipText("This will create a new keylist, to use it you need to go to the configuration emt window");
		newKeywordList.addActionListener(new ActionListener() { /*
															    * (non-Javadoc)
															    * 
															    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
															    */
			public void actionPerformed(ActionEvent arg0) {
				
				String filename = new String(keywordFilename);
				
				filename = JOptionPane.showInputDialog(KeywordWizard.this,"Choose a target keyword filename",filename);
				
				if(filename.length()<1)
				{
					return;
				}
				
				KeywordPanel kp = new KeywordPanel(filename,true);
				kp.clear();
				int rows = dtm.getRowCount();
				for (int i = 0; i < rows; i++) {

					if (((Boolean) dtm.getValueAt(i, i_useColumn)).booleanValue()) {
						kp.addNewWord((String) dtm.getValueAt(i, i_wordColum));
					}
				}
				kp.saveInput();

			}
		});
		
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		constraints.gridy = 5;
		GridBag.setConstraints(newKeywordList, constraints);
		add(newKeywordList);
		
		
		
		
		
		//refresh with 2 as threhsold
		reDrawTables(2, true);
		
	}

	
	/*
	 * Redraws the table choices, and histogram table for keywords
	 */
	public final void reDrawTables(int threshold, boolean ascending) {
		
		int longest = size;
		if(longest>400)
		{
			longest = 400;
		}
		
		double[] x = new double[longest];
		double[] y = new double[longest];
		int tempd;
		int i = 0;
		
		m_histogramGraph.removeAll();
		if (ascending) {//ie start with top numbers
		
			for (i = 0; i < x.length; i++) {
				tempd = ((Integer) dtm.getValueAt(i, i_countColumn)).intValue();
				
				if (tempd < threshold) {
					break;
				}
				dtm.setValueAt(USE, i, i_useColumn);
				x[i] = i;
				y[i] = tempd;
				//y[i] = y[i] / m_totalwords;
			}
			
			
			
			if(i< size){
				for (int j=i; j < size; j++) {
					dtm.setValueAt(NOTUSE, j, i_useColumn);
				}
				
			}
			
			
			if (i < (x.length - 1)) {
								
				double tempx[] = new double[i];
				double tempy[] = new double[i];
				
				
				System.arraycopy(x, 0, tempx, 0, i);

				System.arraycopy(y, 0, tempy, 0, i);

				m_histogramGraph.addCurve(tempx, tempy);

			} else {
				m_histogramGraph.addCurve(x, y);
			}
		} else {
			//asc order
			for(i = size-1;i>x.length;i--){
				dtm.setValueAt(USE, i, i_useColumn);
			}
			
			
			for (i = x.length - 1; i > 0; i--) {
				tempd = ((Integer) dtm.getValueAt(i, i_countColumn)).intValue();
				if (tempd > threshold) {
					break;
				}
				dtm.setValueAt(USE, i, i_useColumn);
				x[i] = i;
				y[i] = tempd;
				//y[i] = y[i] / m_totalwords;
			}

			for (int j=i; j > -1; j--) {
				dtm.setValueAt(NOTUSE, j, i_useColumn);
			}
			
			
			
			if (i > 0) {
				
				double tempx[] = new double[x.length - i];
				double tempy[] = new double[x.length - i];
				
				System.arraycopy(x, 0, tempx, 0, x.length - i - 1);

				System.arraycopy(y, 0, tempy, 0, x.length - i - 1);
				
				m_histogramGraph.addCurve(tempx, tempy);
				
			} else {
				
				m_histogramGraph.addCurve(x, y);
			}
		}
		//double[] s = new double[2];
		//s[0] = 0;
		//s[1] = 1 + ((Integer) sorter.getValueAt(0,
		// i_countColumn)).intValue();
		m_histogramGraph.repaint();
	}




}