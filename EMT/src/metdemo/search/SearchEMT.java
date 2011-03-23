package metdemo.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.NGram;
import metdemo.Tables.SortTableModel;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;
import metdemo.dataStructures.GraphicCalendar;
import metdemo.dataStructures.WordCount;
import metdemo.dataStructures.contentDis;

public class SearchEMT extends JScrollPane {

	private EMTDatabaseConnection DBConnect;

	private winGui winG;

	//    private EMTConfiguration m_emailConfiguration;
	//private JPanel mainjp;
	/* will hold the results of a search query */
	private JTable m_resultsTable;

	private SortTableModel resultTableModel;

	private JTextField indexName, queryText, searchtext;

	private JTextArea m_msgtext;

	private JRadioButton useLucene;

	private JComboBox datachoose;

	static final int data_ABOVE = 0;

	static final int data_ALL = 1;

	static final int data_SQL = 2;

	static final int i_score = 0;

	static final int i_date = 1;

	static final int i_subject = 2;

	static final int i_reference = 3;

	static final int i_groups = 4;

	static final int i_count = 5;

	private Hits resultHits = null;

	private JLabel m_status;

	private GraphicCalendar datecal;

	private EMTConfiguration m_emailConfiguration;

	private Analyzer analyzer = new StopAnalyzer();

	public SearchEMT(EMTDatabaseConnection emtconnect, EMTConfiguration emtc, winGui wg) {

		DBConnect = emtconnect;
		m_emailConfiguration = emtc;
		winG = wg;
		JPanel highestlevelPanel = new JPanel();
		highestlevelPanel.setLayout(new GridLayout(1, 2));

		highestlevelPanel.setPreferredSize(new Dimension(700, 600));

		JTabbedPane tabbedPanel = new JTabbedPane();
		tabbedPanel.setPreferredSize(new Dimension(350, 600));

		JPanel mainsetupPanel = new JPanel();
		JPanel mainSearchPanel = new JPanel();

		// lets setup the gui
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout gridbag2 = new GridBagLayout();
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints2.insets = new Insets(2, 2, 2, 2);

		mainsetupPanel.setLayout(gridbag);
		mainSearchPanel.setLayout(gridbag2);

		//adding in top level label:
		JTextPane separator = new JTextPane();
		separator.setText("Create Index file");
		separator.setEditable(false);
		separator.setBackground(Color.orange);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(separator, constraints);
		mainsetupPanel.add(separator);
		constraints.fill = GridBagConstraints.NONE;

		//index name
		JLabel searchnamelabel = new JLabel("Index name:");
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(searchnamelabel, constraints);
		mainsetupPanel.add(searchnamelabel);

		//text box for name:
		// index name field
		indexName = new JTextField("index1");
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(indexName, constraints);
		mainsetupPanel.add(indexName);
		constraints.fill = GridBagConstraints.NONE;

		//browse button
		JButton browsebutton = new JButton("Browse");
		browsebutton.setEnabled(false); //will turn this on late
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		gridbag.setConstraints(browsebutton, constraints);
		mainsetupPanel.add(browsebutton);

		JButton infoButton = new JButton("More Info");
		//infoButton.setEnabled(false); //will turn this on late
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		gridbag.setConstraints(infoButton, constraints);
		mainsetupPanel.add(infoButton);

		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				final File filename = new File(getIndexFileName(indexName.getText()));

				String msg = new String();

				if (filename.exists()) {

					msg = "Index file: " + filename.getName() + "\n" + "Last Modified: "
							+ new Date(filename.lastModified());

				} else {
					msg = "The index file: " + filename.getName()
							+ "\nHas not been created yet\nNo more info to offer.";
				}

				JOptionPane.showMessageDialog(SearchEMT.this, msg, "More info on index file",
						JOptionPane.INFORMATION_MESSAGE);

			}
		});

		//delete button
		//        JPanel deletehelp = new JPanel();

		JButton deleteIndex = new JButton("Delete");
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.gridwidth = 1;

		//        deletehelp.add(deleteIndex);
		//       deletehelp.add(j);

		gridbag.setConstraints(deleteIndex, constraints);
		mainsetupPanel.add(deleteIndex);

		deleteIndex.setEnabled(false);

		//next to choose type of engine
		useLucene = new JRadioButton("Use Lucene Engine", true);
		useLucene.setToolTipText("Use lucene mapping engine");
		constraints.gridx = 0;
		constraints.gridy = 3;
		gridbag.setConstraints(useLucene, constraints);
		mainsetupPanel.add(useLucene);

		JRadioButton useOwn = new JRadioButton("Use EMT indexing");
		useLucene.setToolTipText("Use EMT in memory mapping engine");
		constraints.gridx = 1;
		constraints.gridy = 3;
		gridbag.setConstraints(useOwn, constraints);
		mainsetupPanel.add(useOwn);
		useOwn.setEnabled(false);

		ButtonGroup bg = new ButtonGroup();
		bg.add(useLucene);
		bg.add(useOwn);

		/*  JButton browseIndex = new JButton("Browse");
		 constraints.gridx = 4;
		 constraints.gridy = 0;
		 constraints.gridwidth = 1;
		 gridbag.setConstraints(browseIndex, constraints);
		 mainjp.add(browseIndex);
		 browseIndex.setEnabled(false);
		 */
		/* JButton loadIndex = new JButton("Load");
		 constraints.gridx = 3;
		 constraints.gridy = 1;
		 constraints.gridwidth = 1;
		 gridbag.setConstraints(loadIndex, constraints);
		 mainjp.add(loadIndex);
		 loadIndex.setEnabled(false);
		 */
		// seperator
		// data choose section
		JPanel choosedatapanel = new JPanel();

		JLabel chooseIndexDataLabel = new JLabel("Choose Data:");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		gridbag.setConstraints(chooseIndexDataLabel, constraints);
		mainsetupPanel.add(chooseIndexDataLabel);

		datachoose = new JComboBox();
		datachoose.addItem("Data based on above user/dates");
		datachoose.addItem("Everything");
		datachoose.addItem("Will Specify SQL");

		datachoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				final JComboBox source = (JComboBox) evt.getSource();
				
				if (source.getSelectedIndex() != data_SQL) {
					queryText.setEnabled(false);
				} else {
					queryText.setEnabled(true);
				}
			}
		});

		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 2;
		gridbag.setConstraints(datachoose, constraints);
		//mainjp.add(datachoose);
		mainsetupPanel.add(datachoose);

		JButton testDataCount = new JButton("Test");

		//constraints.gridx = 3;
		//constraints.gridy = 3;
		//constraints.gridwidth = 1;
		//gridbag.setConstraints(testDataCount, constraints);
		//mainjp.add(testDataCount);

		choosedatapanel.add(testDataCount);

		testDataCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actione) {

				createSearch(false);
			}
		});

		JButton executeDataCount = new JButton("Execute");

		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 2;
		//gridbag.setConstraints(executeDataCount, constraints);
		//mainjp.add(executeDataCount);
		choosedatapanel.add(executeDataCount);
		gridbag.setConstraints(choosedatapanel, constraints);
		mainsetupPanel.add(choosedatapanel);
		constraints.gridwidth = 1;

		//TODO: decide where it goes....
		EMTHelp helpbutton = new EMTHelp(EMTHelp.EMTSEARCH);

		constraints.gridx = 2;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		gridbag.setConstraints(helpbutton, constraints);
		mainsetupPanel.add(helpbutton);

		/***********************************************************************
		 * EXECUTE indexing here
		 */
		executeDataCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				createSearch(true);
			}
		});

		JLabel queryLabel = new JLabel("Query:");
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 1;

		gridbag.setConstraints(queryLabel, constraints);
		mainsetupPanel.add(queryLabel);

		queryText = new JTextField("where email.type like 'text%'");
		constraints.gridx = 1;
		constraints.gridy = 5;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		queryText.setEnabled(false);

		gridbag.setConstraints(queryText, constraints);
		mainsetupPanel.add(queryText);
		constraints.fill = GridBagConstraints.NONE;

		//now to setup the search window
		//TODO: reconfigure stuff here

		separator = new JTextPane();
		separator.setText("Search Term:");
		separator.setEditable(false);
		separator.setBackground(Color.orange);
		constraints2.gridx = 0;
		constraints2.gridy = 0;
		constraints2.gridwidth = 3;
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		gridbag2.setConstraints(separator, constraints2);
		mainSearchPanel.add(separator);
		constraints2.fill = GridBagConstraints.NONE;

		/*
		 * JButton runQuery = new JButton("Run Query"); constraints.gridx = 4;
		 * constraints.gridy = 5; constraints.gridwidth = 1;
		 * gridbag.setConstraints(runQuery, constraints); mainjp.add(runQuery);
		 */

		//  constraints.gridwidth = 1;
		//    constraints.gridx = 0;
		//      constraints.gridy = 6;
		//        JLabel searchLabel = new JLabel("Search for:");
		//       gridbag.setConstraints(searchLabel, constraints);
		//      mainjp.add(searchLabel);
		//
		searchtext = new JTextField(25);
		constraints2.gridwidth = 2;
		constraints2.gridx = 0;
		constraints2.gridy = 1;
		constraints2.fill = GridBagConstraints.BOTH;
		gridbag2.setConstraints(searchtext, constraints2);
		mainSearchPanel.add(searchtext);

		JButton executeSearch = new JButton("Search me!");
		constraints2.fill = GridBagConstraints.NONE;
		constraints2.gridwidth = 1;
		constraints2.gridx = 2;
		constraints2.gridy = 1;

		gridbag2.setConstraints(executeSearch, constraints2);
		mainSearchPanel.add(executeSearch);

		executeSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final long start = System.currentTimeMillis();
				executeDisplaySearch(getIndexFileName(indexName.getText()), searchtext.getText().trim());
				final long end = System.currentTimeMillis();

				System.out.print((end - start) / 1000. + " total seconds for search");
			}
		});

		// setup the results table will be bases of displayed results

		resultTableModel = new SortTableModel();

		//{
		//    public boolean isCellEditable(int row, int col) {
		//       return false;
		//   }
		//};
		resultTableModel.addColumn("Score");
		resultTableModel.addColumn("Date");
		resultTableModel.addColumn("Subject");
		resultTableModel.addColumn("Reference");
		resultTableModel.addColumn("Groups");
		resultTableModel.addColumn("Count");

		//        model.addColumn("VIP rank");
		//		m_timeTable = new JTable(model);

		m_resultsTable = new JTable(resultTableModel);
		m_resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		resultTableModel.addMouseListenerToHeaderInTable(m_resultsTable);

		m_resultsTable.addMouseListener(new MouseListener() {
			public void mousePressed(final MouseEvent e) {
			}

			public void mouseReleased(final MouseEvent e) {
			}

			public void mouseEntered(final MouseEvent e) {
			}

			public void mouseExited(final MouseEvent e) {
			}

			public final void mouseClicked(MouseEvent e) {
				final Point point = e.getPoint();
				int row = m_resultsTable.rowAtPoint(point);
				//since the rows can be sroted might be different...so we already mapped ith row to fifth column
				row = (Integer) resultTableModel.getValueAt(row, i_count);

				if (e.getClickCount() == 1 && resultHits != null) {
					// System.out.println(""+row);
					
						try {
							Document doc = resultHits.doc(row);
							m_msgtext.setText(doc.get("contents"));
							m_msgtext.setCaretPosition(0);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					

				}
			}
		});
		DefaultTableCellRenderer altcr = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				//check if this row has the Highlight column is selected

				if (0 == row % 2)
					this.setBackground(Color.WHITE);
				else
					this.setBackground(Color.lightGray);

				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			}
		};
		TableColumn getaColumn = m_resultsTable.getColumn("Score");

		getaColumn.setPreferredWidth(150);
		getaColumn.setCellRenderer(altcr);

		getaColumn = m_resultsTable.getColumn("Date");

		getaColumn.setPreferredWidth(150);
		getaColumn.setCellRenderer(altcr);

		getaColumn = m_resultsTable.getColumn("Reference");
		getaColumn.setMaxWidth(0);
		getaColumn.setMinWidth(0);
		getaColumn.setPreferredWidth(0);
		getaColumn.setCellRenderer(altcr);
		getaColumn = m_resultsTable.getColumn("Subject");
		getaColumn.setPreferredWidth(450);
		getaColumn.setCellRenderer(altcr);
		getaColumn = m_resultsTable.getColumn("Groups");
		getaColumn.setPreferredWidth(70);

		DefaultTableCellRenderer numberColumnRenderer = new DefaultTableCellRenderer() {
			public void setValue(Object value) {

				int cellValue = (value instanceof Integer) ? ((Integer) value).intValue() : 0;

				setBackground((cellValue % 2 == 1) ? Color.cyan : Color.yellow);
				setText((value == null) ? "" : value.toString());
			}
		};
		numberColumnRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		getaColumn.setCellRenderer(numberColumnRenderer);

		getaColumn = m_resultsTable.getColumn("Count");
		getaColumn.setMaxWidth(0);
		getaColumn.setMinWidth(0);
		getaColumn.setPreferredWidth(0);
		//add status messages
		m_status = new JLabel("Search stats.....");
		constraints2.gridx = 0;
		constraints2.gridy = 7;
		constraints2.gridwidth = 3; // was 7
		constraints2.fill = GridBagConstraints.BOTH;

		gridbag2.setConstraints(m_status, constraints2);
		mainSearchPanel.add(m_status);

		//groupcontent of results
		JButton groupResults = new JButton("Group results");
		constraints2.gridx = 0;
		constraints2.gridy = 8;
		constraints2.gridwidth = 1; // was 7
		constraints2.fill = GridBagConstraints.BOTH;

		gridbag2.setConstraints(groupResults, constraints2);
		mainSearchPanel.add(groupResults);

		groupResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				final int rows = m_resultsTable.getRowCount();

				if (rows < 2) {
					JOptionPane.showMessageDialog(SearchEMT.this, "No results to group");
					return;
				}
				Hashtable<Integer, Integer> lookupTable = new Hashtable<Integer, Integer>(rows);
				//make sure nothing shifts during running
				//int row = m_resultsTable.rowAtPoint(point);
				String data[] = new String[rows];

				for (int i = 0; i < rows; i++) {
					if (resultHits != null)

					{
						try {

							//need to map ith
							int row = (Integer) resultTableModel.getValueAt(i, i_count);
							lookupTable.put(i, row);
							Document doc = resultHits.doc(row);

							data[i] = doc.get("contents");
						} catch (IOException e) {

						}

					}

				}
				
				//todo generalize the ngram stuff to read off the confiration file 
				//TODO:
				ArrayList<ArrayList> ngramresults = NGram.groupContent(data, 0.5, 5, true, false);

				if (ngramresults != null) {
					// int m_max_groups = ngramresults.size();
					for (int i = 0; i < ngramresults.size(); i++) {

						ArrayList sub = (ArrayList) ngramresults.get(i);
						for (int j = 0; j < sub.size(); j++) {
							contentDis ttt = ((contentDis) sub.get(j));
							resultTableModel.setValueAt(lookupTable.get(i), ttt.id, i_groups);

						}
					}
				}

			}
		});

		// now to add it to the window
		constraints2.gridx = 0;
		constraints2.gridy = 3;
		constraints2.gridwidth = 3; // was 7
		constraints2.fill = GridBagConstraints.BOTH;

		JScrollPane resultpane = new JScrollPane();
		resultpane.setViewportView(m_resultsTable);
		resultpane.setPreferredSize(new Dimension(350, 300));
		gridbag2.setConstraints(resultpane, constraints2);
		mainSearchPanel.add(resultpane);

		m_msgtext = new JTextArea("Message body will go here.......");
		m_msgtext.setEditable(false);
		JScrollPane areaScrollPane = new JScrollPane(m_msgtext);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(350, 100));
		m_msgtext.setLineWrap(true);
		m_msgtext.setWrapStyleWord(true);
		constraints2.gridx = 0;
		constraints2.gridy = 4;
		constraints2.gridwidth = 3; // was 7

		constraints2.fill = GridBagConstraints.BOTH;

		gridbag2.setConstraints(areaScrollPane, constraints2);
		mainSearchPanel.add(areaScrollPane);

		datecal = new GraphicCalendar(this);

		/* constraints.gridx = 3;
		 constraints.gridy = 0;
		 constraints.gridwidth = 2;
		 constraints.gridheight = 10;
		 */
		//JScrollPane areacScrollPane = new JScrollPane(datecal);
		// areacScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// areacScrollPane.setPreferredSize(new Dimension(410, 500));

		//gridbag.setConstraints(datecal, constraints);
		//mainjp.add(datecal);

		//setup everything here
		tabbedPanel.addTab("Technical Setup", mainsetupPanel);
		tabbedPanel.addTab("Search", mainSearchPanel);
		highestlevelPanel.add(tabbedPanel);
		highestlevelPanel.add(datecal);

		setViewportView(highestlevelPanel);

	}

	private String getIndexFileName(String name) {

		return m_emailConfiguration.getProperty(EMTConfiguration.DBNAME)
				+ m_emailConfiguration.getProperty(EMTConfiguration.DBTYPE) + "_ind_" + name.trim();

	}

	/**
	 * Will attemp to connect to an index structure, execute a query and display
	 * the results.
	 * 
	 * @param indexfile
	 * @param queryString
	 */

	public void executeDisplaySearch(String indexfile, String queryString) {
		if (queryString.trim().length() < 1) {
			JOptionPane.showMessageDialog(this, "No text given to seach!", "Search issues", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//System.out.println("going to open: " + indexfile);

		
		
		//lets see if it exists at all:
		File test = new File(indexfile);
		if (!test.exists()) {
			JOptionPane.showMessageDialog(this,
					"No such index file present\nAre you sure you have created an index file?", "Missing Index",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String minDate = new String("2020-11-11");
		String MaxDate = new String("0000-00-00");
		BusyWindow bw = new BusyWindow("Setup", "Adding results",true);
		try {
			Searcher searcher = new IndexSearcher(indexfile);
			//standardanalyser
			QueryParser qp = new QueryParser("contents", analyzer);
			Query query = qp.parse(queryString);
			System.out.println("Searching for: " + query.toString("contents"));
			datecal.clear();
			resultHits = searcher.search(query);
			System.out.println(resultHits.length() + " total matching documents");

			bw.setMax(resultHits.length());
			bw.setVisible(true);
			// reset the table length

			resultTableModel.setRowCount(0);

			for (int i = 0; i < resultHits.length(); i++) {
				bw.progress(i);
				Vector rowvalue = new Vector();
				Document doc = resultHits.doc(i);
				float score = resultHits.score(i);
				String path = doc.get("path");
				String curDate = doc.get("Date");
				datecal.addDate(curDate);
				if (path != null) {
					rowvalue.add("" + score);

					// we need to get the date and subject from the db
					// based on the mailref encode in the path with a space
					// afterwards
					String mailref = doc.get("Mailref");
					rowvalue.add(curDate);
					if (compareDatesString(MaxDate, curDate) < 0) {
						MaxDate = curDate;
					} else if (compareDatesString(minDate, curDate) > 0) {
						minDate = curDate;
					}

					try {

						String data[][] = DBConnect.getSQLData("select subject from email where mailref = '" + mailref
								+ "'");

						if (data.length > 0) {
							rowvalue.add(data[0][0]);

						} else {

							rowvalue.add("subject");
						}

					} catch (SQLException see) {
						see.printStackTrace();

						rowvalue.add("subject");
					}

					rowvalue.add(mailref);
					// System.out.println(i + ". ="+score+"= " +
					// doc.get("contents").substring(0,20));//path );
				} else {
					String url = doc.get("url");
					if (url != null) {
						System.out.println(i + ". " + url);
						System.out.println("   - " + doc.get("title"));
					} else {
						System.out.println(i + ". " + "No path nor URL for this document");
					}
				}

				rowvalue.add(-1);
				rowvalue.add(i);
				resultTableModel.addRow(rowvalue);
			}

		} catch (ParseException pe) {
			JOptionPane.showMessageDialog(SearchEMT.this, "Problem with query " + queryString);
			pe.printStackTrace();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(SearchEMT.this, "Problem opening " + indexfile);
			ioe.printStackTrace();

		}
		bw.setVisible(false);
		//        System.out.println("Max:" + MaxDate + " Min:"+ minDate);

		m_status.setText("Number of results: " + resultHits.length() + " From " + minDate + " to " + MaxDate);

		datecal.setDateRange(minDate, MaxDate);
		datecal.paintComponent(datecal.getGraphics());

	}

	
	
	
	public ArrayList<String> getStats(GregorianCalendar selectedDate, int seenAtLeast){
		
		BusyWindow bw = new BusyWindow("Please wait while building keylist","Working....",false);
		bw.setVisible(true);
		ArrayList<String> info = new ArrayList<String>();
		
		//need to create a string of the date
		final String currentDate = Utils.mySqlizeDate2YEAR(selectedDate.getTime()); 
			//selectedDate.get(GregorianCalendar.YEAR)+"-"+selectedDate.get(GregorianCalendar.MONTH)+"-"+selectedDate.get(GregorianCalendar.DAY_OF_MONTH);
		System.out.println("will be matching: "+ currentDate);
		//run through the table
		int rows = m_resultsTable.getRowCount();
		HashMap<String,Integer> wordCounts = new HashMap<String, Integer>();
		int matches =0;
		for(int i=0;i<rows;i++){
			//(Integer) resultTableModel.getValueAt(row, i_count);
			String date = (String) resultTableModel.getValueAt(i,i_date);
			try{
			if(date.equals(currentDate)){
				//need to fetch contents
				matches++;
				
				//make sure no repeats
				Document doc = resultHits.doc(i);
				StringTokenizer stok = new StringTokenizer(doc.get("contents"));
				Integer Intg;
				while(stok.hasMoreTokens())
				{
					String word = stok.nextToken();
					//if present
					if( (Intg = wordCounts.get(word))!=null){
							//wordCounts.containsKey(word)){
						wordCounts.put(word,Intg.intValue()+1);
					}else{
						wordCounts.put(word,1);
					}
					
				}
			}
			}catch(IOException eio){
				eio.printStackTrace();
			}
			
			
		}//end running through the table
//		need to sort the list
		
		
		//pick out top X
		
		//TODO: send it back
		System.out.println("number of matching docs " + matches);
		
		//for each matching message get the contents
		
		//grab word counts
		
		//return ordered list of words
		ArrayList wordList = new ArrayList(wordCounts.size());
		for (Iterator enumWalker = wordCounts.keySet().iterator(); enumWalker.hasNext();) {
			String key = (String) enumWalker.next();
			
			wordList.add(new WordCount(key,((Integer)wordCounts.get(key)).intValue()));
			//dtm.setValueAt(m_keymaps.get(key), step, i_countColumn);
			//step++;
		}
	    Collections.sort(wordList);
		for(Iterator it = wordList.iterator();it.hasNext();)
		{
		    WordCount wc = (WordCount)it.next();
			info.add(wc.getWord() + " " +(wc.getCount()));
			
			
		}
		
		
		
		bw.setVisible(false);
		
		
		
		return info;
	}
	
	
	
	
	/**
	 * Creates an indexed search crawl of a selection of the data.
	 * 
	 * @param realSearch
	 *            if to really create or just run a test to see how many records
	 *            would have been included.
	 */
	private void createSearch(boolean realSearch) {

		String sqlQuery = new String();

		if (useLucene.isSelected()) {

			if (datachoose.getSelectedIndex() == data_ABOVE) {
				// need to reference the above data.
				String usp = (String) winG.getSelectedUser();
				String dates[] = winG.getDateRange_toptoolbar();
				sqlQuery = "select distinct mailref,dates from email where sender = '" + usp + "' or rcpt = '" + usp
						+ "' and dates BETWEEN '" + dates[winGui.startDate] + "' AND '" + dates[winGui.endDate]
						+ "' ";

			} else if (datachoose.getSelectedIndex() == data_ALL) {
				sqlQuery = "select distinct mailref,dates from email where type like 'text%'";
			} else {
				// need to pop up sql
				sqlQuery = "select distinct mailref,dates from email " + queryText.getText().trim();
			}

			String results[][];
			// actual fetch of data
			try {
				synchronized (DBConnect) {
					results = DBConnect.getSQLData(sqlQuery);
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				return;
			}

			if (!realSearch) {

				JOptionPane.showMessageDialog(SearchEMT.this, "The number of returned mailrefs are: " + results.length);
				return;
			}
			BusyWindow bw = new BusyWindow("Search Engine Setup", "Running",true);
			bw.setMax(results.length);
			bw.setVisible(true);
			// need to setup prefetch cache
			try {
				PreparedStatement ps = DBConnect
						.prepareStatementHelper("select hash,filename,type,body from message where mailref=?");

				// real search need to build index here
				// TODO: see about adding path between folder and name
				IndexWriter writer = null;
				// stopanalyzer
				//writer = new IndexWriter(indexfolder + indexName.getText().trim(), new StandardAnalyzer(), true);
				writer = new IndexWriter(getIndexFileName(indexName.getText()), analyzer, true);

				Date start = new Date();
				for (int i = 0; i < results.length; i++) {
					bw.progress(i);

					ps.setString(1, results[i][0]);

					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						String type = rs.getString(3).toLowerCase();

						if (type.startsWith("image") || type.startsWith("video") || type.startsWith("audio")) {
							break;
						}

						String hash = rs.getString(1);
						String name = rs.getString(2);

						// make a new, empty document
						Document doc = new Document();

						// Add the path of the file as a field named "path". Use
						// a Text field, so
						// that the index stores the path, and so that the path
						// is searchable
						doc.add(new Field("path", hash,Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("Date", results[i][1],Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("Type", type,Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("Name", name,Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("Mailref", new String(results[i][0]),Field.Store.YES, Field.Index.UN_TOKENIZED));
						// Add the last modified date of the file a field named
						// "modified". Use a
						// Keyword field, so that it's searchable, but so that
						// no attempt is made
						// to tokenize the field into words.
						// doc.add(Field.Keyword("modified",
						// DateField.timeToString(f.lastModified())));

						// Add the contents of the file a field named
						// "contents". Use a Text
						// field, specifying a Reader, so that the text of the
						// file is tokenized.
						// ?? why doesn't FileReader work here ??
						try {
							java.sql.Blob blob = rs.getBlob(4);
							int c;
							byte buf[] = new byte[512];//

							InputStream is = blob.getBinaryStream();
							StringBuffer small2 = new StringBuffer(256);
							while ((c = is.read(buf)) != -1) {
								small2.append(new String(buf, 0, c));
							}
							String bodytext = small2.toString();

							doc.add(new Field("contents", bodytext,Field.Store.YES, Field.Index.TOKENIZED));

							// now to insert this doc.
							writer.addDocument(doc);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}
				bw.setTitle("optimizing");
				writer.optimize();
				writer.close();

				Date end = new Date();

				System.out.print(end.getTime() - start.getTime());
				System.out.println(" total milliseconds");

				bw.setVisible(false);

			} catch (SQLException se) {
				se.printStackTrace();
				return;
			} catch (IOException eio) {
				eio.printStackTrace();
				return;
			}

		} else {
			// we use emt indexing
			// TODO: create code here

		}
	}

	/**
	 * Will compare 2 dates as string and return greater than zero if the second is larger, 0 if equal , less than zero if less
	 * @param dateFirst
	 * @param dateSecond
	 * @return
	 */
	public static int compareDatesString(String dateFirst, String dateSecond) {

		if (dateFirst.equals(dateSecond)) {
			return 0;
		}
		///lets check the years from format yyyy-mm-dd
		int year1 = Integer.parseInt(dateFirst.substring(0, 4));
		int year2 = Integer.parseInt(dateSecond.substring(0, 4));

		if (year1 < year2) {
			return -1;
		}
		if (year1 > year2) {
			return 1;
		}

		//if equal lets do months
		year1 = Integer.parseInt(dateFirst.substring(5, 7));
		year2 = Integer.parseInt(dateSecond.substring(5, 7));

		if (year1 < year2) {
			return -1;
		}
		if (year1 > year2) {
			return 1;
		}

		//     if equal lets do days
		year1 = Integer.parseInt(dateFirst.substring(8));
		year2 = Integer.parseInt(dateSecond.substring(8));

		if (year1 < year2) {
			return -1;
		}
		//if(year1 > year2){
		return 1;
		// }

	}

}
