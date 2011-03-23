/*
 * ++Copyright SYSDETECT++
 * 
 * Copyright (c) 2002 System Detection.  All rights reserved.
 * 
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SYSTEM DETECTION.
 * The copyright notice above does not evidence any actual
 * or intended publication of such source code.
 * 
 * Only properly authorized employees and contractors of System Detection
 * are authorized to view, posses, to otherwise use this file.
 * 
 * System Detection
 * 5 West 19th Floor 2 Suite K
 * New York, NY 10011-4240
 * 
 * +1 212 242 2970
 * <sysdetect@sysdetect.org>
 * 
 * --Copyright SYSDETECT--
 */


package metdemo.Tools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.RowHeaderRenderer;
import metdemo.Tables.md5CellRenderer;
import metdemo.dataStructures.userShortProfile;


import java.util.*;
import java.text.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Histograms extends JScrollPane
{
  private final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
  private final String[] TIME_PERIODS = {"Daytime", "Evening", "Night"};
  //private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  
  private JComboBox m_comboUsers;
  private JTable m_tableCount;
  private JTable m_tableSize;
  private JTable m_tableRcpts;

  private HashSet m_hsUsers;                  // to keep track of who we already have in combo
  private winGui m_wingui;
  private EMTDatabaseConnection m_jdbcUpdate, m_jdbcView;

  public Histograms(winGui wg,EMTDatabaseConnection jdbcUpdate, EMTDatabaseConnection jdbcView, md5CellRenderer md5Renderer)
  {
    if ((jdbcUpdate == null) || (jdbcView == null))
    {
      throw new NullPointerException();
    }
    m_wingui = wg;
    m_jdbcUpdate = jdbcUpdate;
    m_jdbcView = jdbcView;

    JPanel panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    panel.setLayout(gridbag);

    m_hsUsers = new HashSet();

    m_comboUsers = new JComboBox();
    m_comboUsers.setPreferredSize(new Dimension(350,25));
    m_comboUsers.setRenderer(md5Renderer);
    
    JPanel panSelect = new JPanel();
    panSelect.setLayout(new FlowLayout());
    panSelect.add(new JLabel("Select Account:"));
    panSelect.add(m_comboUsers);
    
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.insets = new Insets(20, 5, 20, 5);
    gridbag.setConstraints(panSelect, constraints);
    panel.add(panSelect);

    JLabel label;
    label = new JLabel("Average Number of Messages Sent");
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.insets = new Insets(5, 5, 5, 5);
    gridbag.setConstraints(label, constraints);
    panel.add(label);

    JScrollPane scrollPane;
    m_tableCount = new JTable(DAYS.length, TIME_PERIODS.length);
    scrollPane = initTable(m_tableCount);
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.insets = new Insets(5, 5, 20, 5);
    gridbag.setConstraints(scrollPane, constraints);
    panel.add(scrollPane);

    label = new JLabel("Average Size of Messages Sent (bytes)");
    constraints.gridx = 0;
    constraints.gridy = 3;
    constraints.insets = new Insets(5, 5, 5, 5);
    gridbag.setConstraints(label, constraints);
    panel.add(label);

    m_tableSize = new JTable(DAYS.length, TIME_PERIODS.length);
    scrollPane = initTable(m_tableSize);
    constraints.gridx = 0;
    constraints.gridy = 4;
    constraints.insets = new Insets(5, 5, 20, 5);
    gridbag.setConstraints(scrollPane, constraints);
    panel.add(scrollPane);

    label = new JLabel("Average Number of Recipients");
    constraints.gridx = 0;
    constraints.gridy = 5;
    constraints.insets = new Insets(5, 5, 5, 5);
    gridbag.setConstraints(label, constraints);
    panel.add(label);

    m_tableRcpts = new JTable(DAYS.length, TIME_PERIODS.length);
    scrollPane = initTable(m_tableRcpts);
    constraints.gridx = 0;
    constraints.gridy = 6;
    constraints.insets = new Insets(5, 5, 20, 5);
    gridbag.setConstraints(scrollPane, constraints);
    panel.add(scrollPane);

    m_comboUsers.addActionListener(new AbstractAction()
      {
	public void actionPerformed(ActionEvent event)
	{
	  try
	  {
	    refreshTables();
	  }
	  catch (SQLException ex)
	  {
	    JOptionPane.showMessageDialog(Histograms.this, "Failed to get data for account: " + ex);
	  }
	}
      });

    setViewportView(panel);
  }

  public JScrollPane initTable(JTable table)
  {
    DefaultTableModel headerData = new DefaultTableModel(0, 1);
    for (int i=0; i<DAYS.length; i++)
    {
      headerData.addRow(new Object[] {DAYS[i]});
    }

    for (int i=0; i<TIME_PERIODS.length; i++)
    {
      table.getColumnModel().getColumn(i).setHeaderValue(TIME_PERIODS[i]);
    }
    JTable rowHeader = new JTable(headerData);

    LookAndFeel.installColorsAndFont(rowHeader, "TableHeader.background", "TableHeader.foreground", "TableHeader.font");

    rowHeader.setIntercellSpacing(new Dimension(0, 0));
    Dimension d = rowHeader.getPreferredScrollableViewportSize();
    d.width = rowHeader.getPreferredSize().width;
    rowHeader.setPreferredScrollableViewportSize(d);
    rowHeader.setRowHeight(table.getRowHeight());
    rowHeader.setDefaultRenderer(Object.class, new RowHeaderRenderer());

    table.setPreferredScrollableViewportSize(table.getPreferredSize());
    
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setRowHeaderView(rowHeader);

    JTableHeader corner = rowHeader.getTableHeader();
    corner.getColumnModel().getColumn(0).setHeaderValue("");
    corner.setReorderingAllowed(false);
    corner.setResizingAllowed(false);
    
    scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);

    return scrollPane;
  }


  public void refreshCombo() throws SQLException
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
    synchronized(m_jdbcUpdate)
    {
      data = m_jdbcUpdate.getSQLData("select distinct sender from email order by sender");
      //data = m_jdbcUpdate.getRowData();
    }
    for (int i=0; i<data.length; i++)
    {
      if (data[i].length != 1)
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
	  m_comboUsers.insertItemAt(data[i][0], j);
	  if (m_comboUsers.getItemCount() == 1)
	  {
	    m_comboUsers.setSelectedIndex(0);
	  }
	}
      }

      // replace selection
      if (strCurrent.equals(data[i][0]))
      {
	m_comboUsers.setSelectedItem(data[i][0]);
      }
    }
  }

  

  public void refreshTables() throws SQLException
  {
   // String data[][];
   // String query;
    userShortProfile days;
	double results[][] = new double[TIME_PERIODS.length][DAYS.length];
	double numrcpts[][] = new double[TIME_PERIODS.length][DAYS.length];
	double sizeinfo[][] = new double[TIME_PERIODS.length][DAYS.length];
	// get the user
    Object selected = m_comboUsers.getSelectedItem();
    if (selected == null)
    {
      return;
    }

    String user = (String) selected;

    // average number of messages
    synchronized (m_jdbcView)
    {
     //-- m_jdbcView.updateSQLData("create temporary table HistogramsRefreshTemp (period int, msg_date date, msg_count int)");
//TODO:
      try
      {
	// get lifespan of account
	//--query = "select min(dates), max(dates) from email where sender='" + user + "'";
	//--data = m_jdbcView.getSQLData(query);
	days = m_wingui.getUserInfo(user);
      	
      	//data = m_jdbcView.getRowData();
	//--if (data.length != 1)
	//--{
	 //-- throw new SQLException("No data returned from query " + query);
	//--}
	//--if (data[0].length != 2)
	//--{
	 //--- throw new SQLException("Wrong number of columns returned from query " + query);
	//--}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Calendar calStart = new GregorianCalendar();
	calStart.setTime(dateFormat.parse(days.getStartDate()));//[winGui.User_min]));
	calStart.add(Calendar.DATE, -1);
	Calendar calEnd = new GregorianCalendar();
	calEnd.setTime(dateFormat.parse(days.getEndDate()));//[winGui.User_max]));
	double weekLifespan = Math.ceil((calEnd.getTimeInMillis() - calStart.getTimeInMillis()) / (double) (1000*60*60*24*7));

	// get count for each period
	//--query = "insert into HistogramsRefreshTemp ";
	//--query += "select ";
	//--query += "if ((ghour >= 9) and (ghour < 17), 0,";
	//--query += "if ((ghour >= 17) or (ghour = 0), 1,";
	//--query += " 2)) as period,";
	//--query += "if (ghour >= 9, dates, dates - interval 1 day) as msg_date,";
	//--query += "count(*) as msg_count ";
	//--query += "from email ";
	//--query += "where sender=\"" + user + "\" ";
	//--query += "group by msg_date,";
	//--query += "period ";
	//--m_jdbcView.updateSQLData(query);
	GregorianCalendar gcal = new GregorianCalendar();
	for (int i = 0; i < TIME_PERIODS.length; i++) {
		for (int j = 0; j < DAYS.length; j++) {
			results[i][j] = 0;
			numrcpts[i][j] = 0;
			sizeinfo[i][j] =0;
		}
	}
	int ghour, size, day, period,numcc;

	
	PreparedStatement infoget = m_jdbcView.prepareStatementHelper("select ghour,dates,numrcpt,size from email where sender = ? order by dates,ghour,mailref");
	infoget.setString(1,user);
	
	ResultSet rs = infoget.executeQuery();
	while(rs.next())
	//for(int i=0;i<rs..length;i++)
	{
		ghour = rs.getInt(1);//Integer.parseInt(data[i][0]);
		//count = Integer.parseInt(data[i][2]);
		gcal.setTime(rs.getDate(2));//sdf.parse(data[i][1]));
		
		day = gcal.get(Calendar.DAY_OF_WEEK);
		
		numcc = rs.getInt(3);
		size = rs.getInt(4);
		if(ghour>=9 && ghour <=17)
		{
			period = 0;
		}
		else if(ghour >=17 || ghour ==0)
		{
			period =1;			
		}
		else
		{
			period =2;
		}
		
		if(ghour<9)
		{
			day--;
			if(day<0)
			{
				day =6;
			}
		}
			
		results[period][day] ++;
		numrcpts[period][day] +=numcc;
		sizeinfo[period][day] +=size;
	}	
	rs.close();	
	
	for(int i=0;i<TIME_PERIODS.length;i++)
	{
		for(int j=0;j<DAYS.length;j++){
			
			numrcpts[i][j] = numrcpts[i][j] / results[i][j];
			sizeinfo[i][j] = sizeinfo[i][j] / results[i][j];
			
			results[i][j] /= weekLifespan;
		}
	}
	
		
	//--query = "select ";
	//--query += "period,";
	//--query += "weekday(msg_date) as day_of_week,";
	//--query += "sum(msg_count)/" + weekLifespan + " as count_avg ";
	//--query += "from HistogramsRefreshTemp ";
	//--query += "group by period,";
	//--query += "day_of_week ";
	//--data = m_jdbcView.getSQLData(query);
//	data = m_jdbcView.getRowData();
      }
      catch (ParseException pe)
      {
	throw new SQLException("Database returned invalid date format: " + pe);
      }
      //--finally
     //-- {
	// drop temp table even on error
	//--m_jdbcView.updateSQLData("drop table HistogramsRefreshTemp;");
      //--}
    }//jdbclock

    // do refresh in table model to prevent flicker
    // clear the table
    DefaultTableModel model = (DefaultTableModel) m_tableCount.getModel();
    Vector vRows = model.getDataVector();
    for (int i=0; i<vRows.size(); i++)
    {
      Vector vColumns = (Vector) vRows.get(i);

      for (int j=0; j<vColumns.size(); j++)
      {
      	vColumns.set(j, ""+results[i][j]);//--"0.0");
      }
     
    }

    //--for (int i=0; i<data.length; i++)
  //  {
//--      if (data[i].length != 3)
   //--   {
	//--throw new SQLException("Wrong number of columns returned");
      //--}
    //--  ((Vector) vRows.get(Integer.parseInt(data[i][1]))).set(Integer.parseInt(data[i][0]), data[i][2]);
     
    //--}

    model.newDataAvailable(new TableModelEvent(model));


    // average number of recipients
   //-- synchronized (m_jdbcView)
//  --   {
    	//TODO:
      //--query = "create temporary table HistogramsRefreshTemp ";

   //--   try
    //--  {
	//--query += "(sender varchar(40), mailref varchar(255), dates date, times time, numrcpt smallint(6))";
//---	m_jdbcView.updateSQLData(query);
    
	//--query = "insert into HistogramsRefreshTemp select sender, mailref, dates, times, count(*) as numrcpt ";
	//--query += "from email ";
	//--query += "where ";
	//--query += "(sender = \"" + user + "\") ";
	//--query += "group by sender, mailref";
	//--m_jdbcView.updateSQLData(query);
	//--query = "select ";
//    	--query += "if ((ghour >= 9) and (ghour < 17), 0,";
//    	--query += "if ((ghour >= 17) or (ghour = 0), 1,";
//    	--query += "2)) as period,";
//    	--query += "if (ghour >= 9, weekday(dates), (weekday(dates)+6)%7) as day_of_week, ";
//    	--query += "avg(numrcpt) as rcpt_avg ";
//    	--query += "from HistogramsRefreshTemp ";
//    	--query += "group by day_of_week, ";
//    	--query += "period ";
//    	--data = m_jdbcView.getSQLData(query);

//	data = m_jdbcView.getRowData();
//    	--  }
//    	--   finally
//    	--    {
	// drop temp table even on error
//    	--	m_jdbcView.updateSQLData("drop table HistogramsRefreshTemp");
//    	--      }
//    	--    }

    // clear the table
    model = (DefaultTableModel) m_tableRcpts.getModel();
    vRows = model.getDataVector();
    for (int i=0; i<vRows.size(); i++)
    {
      Vector vColumns = (Vector) vRows.get(i);

      for (int j=0; j<vColumns.size(); j++)
      {
      	vColumns.set(j, ""+numrcpts[i][j]);
      }
    }

   //-- for (int i=0; i<data.length; i++)
   //-- {
     //-- if (data[i].length != 3)
     //-- {
	//--throw new SQLException("Wrong number of columns returned");
     //-- }
    
   //--   ((Vector) vRows.get(Integer.parseInt(data[i][1]))).set(Integer.parseInt(data[i][0]), data[i][2]);
    //--}

    model.newDataAvailable(new TableModelEvent(model));


    // average message size
   //-- synchronized (m_jdbcView)
  //--  {
  //--    query = "select ";
//  -- query += "if ((ghour >= 9) and (ghour < 17), 0,";
//  --  query += "if ((ghour >= 17) or (ghour = 0), 1,";
//  --   query += "2)) as period,";
//  --   query += "if (ghour >= 9, weekday(dates), (weekday(dates)+6)%7) as day_of_week,";
//  --   query += "avg(size) as size_avg ";
//  --   query += "from email ";
//  --   query += "where ";
//  --   query += "(sender = \"" + user + "\") ";
//  --   query += "group by day_of_week,";
//  --    query += "period ";
      
//  --     data =  m_jdbcView.getSQLData(query);
//      data = m_jdbcView.getRowData();
//  --}
    // clear the table
    model = (DefaultTableModel) m_tableSize.getModel();
    vRows = model.getDataVector();
    for (int i=0; i<vRows.size(); i++)
    {
      Vector vColumns = (Vector) vRows.get(i);

      for (int j=0; j<vColumns.size(); j++)
      {
	vColumns.set(j, ""+sizeinfo[i][j]);
      }
    }

//  --  for (int i=0; i<data.length; i++)
//  --    {
//  --     if (data[i].length != 3)
//  --     {
//  --	throw new SQLException("Wrong number of columns returned");
//  --      }
    
//  --     ((Vector) vRows.get(Integer.parseInt(data[i][1]))).set(Integer.parseInt(data[i][0]), data[i][2]);
//  --   }

    model.newDataAvailable(new TableModelEvent(model));
  }

  public void Refresh() throws SQLException
  {
    refreshCombo();
  }
}
