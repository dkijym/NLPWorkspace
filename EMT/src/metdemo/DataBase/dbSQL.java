//modified by shlomo hershkop
//summer 2002

/**
 * A a UI around the JDBCAdaptor, allowing database data to be interactively
 * fetched, sorted and displayed.
 * 
 * @author shlomo Hershkop
 */

package metdemo.DataBase;

//import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.*;
//import javax.swing.table.*;
//import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JPanel;

import metdemo.Tables.TableSorter;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;
//import java.sql.SQLException;

public class dbSQL extends JPanel {
	//    static String[] ConnectOptionNames = { "Connect" };
	//static String ConnectTitle = "Connection Information";
	//    Dimension origin = new Dimension(0, 0);
	JButton fetchButton, helpButton, dateButton, speedButton;
	//JButton showConnectionInfoButton;
	//JPanel connectionPanel;
	JFrame frame; // The query/results window.
	// JLabel queryLabel;
	JTextArea queryTextArea;
	JComponent queryAggregate;
	//JPanel mainPanel;
	TableSorter sorter;
	//JDBCAdapter dataBase;
	JScrollPane tableAggregate;
	boolean am_connected = false;
	EMTDatabaseConnection jdbcUpdate;
	GridBagConstraints constraints = new GridBagConstraints();
	GridBagLayout GridBag = new GridBagLayout();

	

	public dbSQL(EMTDatabaseConnection dbb) {
		//jdbcView = new JDBCConnect("jdbc:mysql://" + dbHost + "/" + dbName,
		// "org.gjt.mm.mysql.Driver", user, password);
		//else
		//passwordField.setText(" " + p);
		
		jdbcUpdate = dbb;
		//JPanel mainPanel = this;
	
		//mainPanel.
		setLayout(GridBag);
		constraints.gridwidth = 1;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.anchor = GridBagConstraints.WEST;

		// Create the buttons.
		/*
		 * showConnectionInfoButton = new JButton("Configuration");
		 * showConnectionInfoButton.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) {
		 * activateConnectionDialog(); } } );
		 */

		fetchButton = new JButton("Fetch/Execute");
		fetchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fetch();
			}
		});
		//place the fetch button
		constraints.gridx = 3;
		constraints.gridy = 0;
		GridBag.setConstraints(fetchButton, constraints);
		//mainPanel.
		add(fetchButton);

		JButton CButton = new JButton("Clean Invalid Data");
		CButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanmailref(true);
			}
		});
		constraints.gridx = 4;
		constraints.gridy = 0;
		GridBag.setConstraints(CButton, constraints);
		//mainPanel.
		add(CButton);

		EMTHelp helpButton = new EMTHelp(EMTHelp.SQLVIEW);
		//place the help button
		constraints.gridx = 0;
		constraints.gridy = 0;
		GridBag.setConstraints(helpButton, constraints);
		//mainPanel.
		add(helpButton);

		speedButton = new JButton("Optimize Tables");
		speedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optimize(jdbcUpdate);
			}
		});

		//place the fetch button
		constraints.gridx = 2;
		constraints.gridy = 0;
		GridBag.setConstraints(speedButton, constraints);
		//mainPanel.
		add(speedButton);

		dateButton = new JButton("Clean Invalid Dates Problem");
		dateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanCalendarDates();
			}
		});
		//place the fetch button
		constraints.gridx = 1;
		constraints.gridy = 0;
		GridBag.setConstraints(dateButton, constraints);
		//mainPanel.
		add(dateButton);

		// Create the query text area and label.
		queryTextArea = new JTextArea("SELECT * FROM email where UID < 50", 25,
				25);
		queryAggregate = new JScrollPane(queryTextArea);
		queryAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// Create the table.
		tableAggregate = createTable();
		tableAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));

			//place the sql window
		constraints.gridx = 0;
		constraints.gridy = 1;

		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridwidth = 7;

		GridBag.setConstraints(queryAggregate, constraints);
		//mainPanel.
		add(queryAggregate);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weighty = 4;
		GridBag.setConstraints(tableAggregate, constraints);
		//mainPanel.
		add(tableAggregate);

		//        mainPanel.add(tableAggregate);

		// Create a Frame and put the main panel in it.
		// frame = new JFrame("EMT sql view");
		//frame.addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {//System.exit(0);
		//	}});
		//frame.setBackground(Color.lightGray);
		//frame.getContentPane().add(mainPanel);
		//frame.pack();
		//frame.setVisible(false);
		//frame.setBounds(200, 200, 640, 480);

		//mainPanel.
		setVisible(true);
		
	}
/*
	public void connect() {
		am_connected = true;
		dataBase = new JDBCAdapter(serverField.getText(),
				driverField.getText(), userNameField.getText(), passwordField
						.getText());
		sorter.setModel(dataBase);
	}
*/
	public void fetch() {
		
		fetchAndDisplaySQL(queryTextArea.getText());
	}

	public void fetchAndDisplaySQL(String sql)
	{
		String data[][]= null;
		try{
			data = jdbcUpdate.getSQLData(sql);
		}catch(SQLException ee)
		{
			System.out.println(ee);
			data = new String[1][1];
			data[0][0] = ee.toString();
		}
		
		//need to create table columns
		//TODO
		//and add to table
		//m_tableModel = new MessageTableModel(COLUMN_NAMES, 0);
//		m_messageTable = new JTable(m_tableModel
		
		if (data.length == 0 ) {
			try{
			if (data[0].length > 0) {

				data = new String[1][data[0].length];

				for (int j = 0; j < data[0].length; j++) {
					data[0][j] = "NULL";
				}
			} else {
				data = new String[1][1];
				data[0][0] = "NULL";
			}
			}catch(ArrayIndexOutOfBoundsException e){
				data = new String[1][1];
				data[0][0] = "NULL";
			}
		}

		remove(tableAggregate);

		DefaultTableModel dftm = new DefaultTableModel(0, data[0].length) {
			public final boolean isCellEditable(final int rowIndex, final int columnIndex) {
				return false;
			}
		};
		sorter = new TableSorter(dftm);
		JTable table = new JTable(sorter);
		// Use a scrollbar, in case there are many columns.
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSize(600,300);
	
		//set the column widths
		for(int i=0;i<data[0].length;i++)
		{
			table.getColumn(table.getColumnName(i)).setPreferredWidth(100);
			
		}
		//System.out.println("data len" + data.length);
		
		for(int i=0;i<data.length;i++)
		{
			
		     dftm.insertRow(i,data[i]);
		}
		// Install a mouse listener in the TableHeader as the sorter UI.
		sorter.addMouseListenerToHeaderInTable(table);
		JScrollPane scrollpane = new JScrollPane(table);
		
		
		//now to re-add to table
		//GridBagConstraints constraints = new GridBagConstraints();
		
		//mainPanel.
		//if(tableAggregate!=null)
		this.tableAggregate = scrollpane;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weighty = 4;
		//GridBagLayout GridBag = (GridBagLayout)mainPanel.getLayout();
		
		GridBag.setConstraints(tableAggregate, constraints);
		//mainPanel.
		add(tableAggregate);
		repaint();
		validate();	
	}
	
	
	
	public static void flushDatabase(EMTDatabaseConnection jdbcView, boolean toOptimizeAfter){
		synchronized (jdbcView) {

			try {
				jdbcView.updateSQLData("delete from email");
			} catch (SQLException s1) {
				System.out.println(s1);
			}
			try {
				jdbcView.updateSQLData("delete from message");
			} catch (SQLException s1) {
				System.out.println(s1);
			}

			try {
				jdbcView.updateSQLData("delete from kwords");
			} catch (SQLException s1) {
				System.out.println(s1);
			}
			System.out.println("Finished flushing db");
			
			
			dbSQL.optimize(jdbcView);

		}
	}
	
	
	static public final void optimize(EMTDatabaseConnection jdbcUpdate) {
		
		if(jdbcUpdate.getTypeDB().equals(DatabaseManager.sMYSQL) || jdbcUpdate.getTypeDB().equals(DatabaseManager.sCMYSQL)){
			
			synchronized(jdbcUpdate){
				try{
					jdbcUpdate.updateSQLData("optimize table email,message,kwords");
				}catch(SQLException s){
					System.out.println(s);
				}
			}
			}else if(jdbcUpdate.getTypeDB().equals(DatabaseManager.sPOSTGRES)){
				
				synchronized(jdbcUpdate){
					try{
						jdbcUpdate.updateSQLData("VACUUM email");
						jdbcUpdate.updateSQLData("VACUUM message");
						jdbcUpdate.updateSQLData("VACUUM kwords");
					}catch(SQLException s){
						System.out.println(s);
					}
				}
				}
		else
		{
			System.out.println("2:optimize not implimented in cloudscape/derby");
		}
	}
	
	
	public final void cleanmailref(boolean show) {
		cleanmailref(show, this.jdbcUpdate, this);
	}

	/** clean out bad mailref ...usually if data is manually messed with or a problem with the parser
	 * 
	 * The idea is that every mailref in the email table needs a corresponding mailref in the message table
	 * if the message table cant match, problems occur elsewhere.
	 * 
	 * 
	 * */
	static public final void cleanmailref(boolean show, EMTDatabaseConnection jdbcUpdate,
			JPanel th) {
		
		HashMap emailTableMailref = new HashMap();
	//	HashMap messageTableMailref = new HashMap();
		
		
		int c = 0;
		try {

			ArrayList deleteList = new ArrayList();
			String data[];
			synchronized (jdbcUpdate) {
//					data = jdbcUpdate.getSQLData("select distinct m.mailref from message m left join email e on e.mailref=m.mailref where e.mailref IS NULL");
	
				data = jdbcUpdate.getSQLDataByColumn("select distinct mailref from email",1);
				for(int i=0;i<data.length;i++){
					emailTableMailref.put(data[i],"");
				}
				data = jdbcUpdate.getSQLDataByColumn("select distinct mailref from message",1);
				for(int i=0;i<data.length;i++){
					if(!emailTableMailref.containsKey(data[i])){
						deleteList.add(data[i]);
						
					}
				}
				
				c = deleteList.size();
				try{
				for(Iterator it = deleteList.iterator();it.hasNext();){
					String target = (String)it.next();
					jdbcUpdate.updateSQLData("delete from email where mailref = '" + target +"'");
					jdbcUpdate.updateSQLData("delete from message where mailref = '" + target +"'");
					
				}
				}catch(SQLException sin){
					System.out.println(sin);
				}
				
				
				
				//data = jdbcUpdate.getRowData();
			}
			
			
			
			
/*
			if ((data.length < 1) || (data[0].length != 1)) {
				if (show)
					JOptionPane
							.showMessageDialog(th,
									"Database cleanup report: no change done, seems to be fine");
				return;
				//throw new Exception("select failed");
			}
			c = data.length;
			for (int i = 0; i < data.length; i++) {
				jdbcUpdate.updateSQLData("delete from kwords where mailref='"
						+ data[i][0] + "'");
				jdbcUpdate.updateSQLData("delete from message where mailref='"
						+ data[i][0] + "'");
			}
	
		*/
			
		} catch (SQLException e) {
			System.out.println(e);
		}
		if (show)
			JOptionPane.showMessageDialog(th,
					"Number of records fixed by cleanup utility: " + c);

	}

	public void cleanCalendarDates() {
		int sum = 0;
        boolean errorsFlag = false;
		try {

			String data[][];
			String sub = new String();
			synchronized (jdbcUpdate) {
				data = jdbcUpdate
						.getSQLData("select dates,times,utime,uid from email order by uid");
				//data = jdbcUpdate.getRowData();
			}
			if ((data.length < 1)) {
				JOptionPane.showMessageDialog(this, "Select Failed in clean calendar method ", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String d, t, it;
			d = new String("2005-01-01");
			t = new String("12:12:12");
			it = new String("1104581532");
			int year;
			Calendar nowyear = new GregorianCalendar();
			nowyear.setTime(new Date(System.currentTimeMillis()));
			int maxyear = 2+ nowyear.get(Calendar.YEAR);
			
			//hpoe first isnt empty
			synchronized (jdbcUpdate) {
			for (int i = 0; i < data.length; i++) {
				sub = data[i][0].substring(0, 4);
				try {
					year = Integer.parseInt(sub);
				} catch (Exception t3) {
					System.out.println("problem with year" + sub);
					year = maxyear -2;
				}

				
				//	  System.out.print(data[i][0] + " ");
				if (data[i][0].startsWith("00") || year < 1980 || year > maxyear) {
					sum++;
					//TODO: prepared statement here
            			 // System.out.println(i + ") d wil be "+d+" uid for "+data[i][3]);
					
						jdbcUpdate.updateSQLData("update email set dates = '"
								+ d + "', times = '" + t + "', utime= " + it
								+ " where uid = " + data[i][3]);
					
				}else if(data[i][2].equals("0")){
					//need to fix utime if date is ok
                    //System.out.println(i+") fix");
                    long u_utime = Utils.getMill(data[i][0]+" "+data[i][1]);
                    System.out.println(data[i][0]+" "+data[i][1] +" will be update email set utime = "+u_utime
							+ " where uid = " + data[i][3]);
                    //u_utime /=1000; //need t0 adjust to unix seconds
					jdbcUpdate.updateSQLData("update email set utime = "+u_utime
							+ " where uid = " + data[i][3]);
				
					
				}else{
					d = data[i][0];
					t = data[i][1];
					it = data[i][2];
				}
			}}
		}
	catch(SQLException se){	
se.printStackTrace();
		JOptionPane.showMessageDialog(this, se, "ERROR in clean date method",
				JOptionPane.ERROR_MESSAGE);
        errorsFlag = true;
		
	}catch (Exception e) {
e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "ERROR in clean date method",
					JOptionPane.ERROR_MESSAGE);
            errorsFlag = true;

		}
        
        if(!errorsFlag){
		JOptionPane.showMessageDialog(this,
				"Done cleaning up Dates, number of records fixed: " + sum);
        }
	}

	/*
	 * public void fetch() { //dataBase.executeQuery(queryTextArea.getText());
	 * System.out.println("Will execute: " + queryTextArea.getText() );
	 * 
	 * try{ dataBase.getSqlData(queryTextArea.getText()); //data =
	 * dataBase.getRowData();
	 * 
	 * }catch (SQLException sqlex) { JOptionPane.showMessageDialog(this, "sql
	 * problem: " + sqlex); }
	 *  }
	 */

	public JScrollPane createTable() {
		sorter = new TableSorter();

		//connect();
		//fetch();

		// Create the table
		JTable table = new JTable(sorter);
		// Use a scrollbar, in case there are many columns.
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Install a mouse listener in the TableHeader as the sorter UI.
		sorter.addMouseListenerToHeaderInTable(table);

		JScrollPane scrollpane = new JScrollPane(table);

		return scrollpane;
	}

}

