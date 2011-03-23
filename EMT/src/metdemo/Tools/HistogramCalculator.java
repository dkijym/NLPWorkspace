
package metdemo.Tools;

//import metdemo.*;

import java.text.SimpleDateFormat;
import java.util.*;
//import java.text.SimpleDateFormat;
//import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import metdemo.winGui;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.dataStructures.userShortProfile;

/*
 * This class is used to caculate the histogram of email usage of some user
 * 
 * @author Ke Wang, 07/08/02
 */

public class HistogramCalculator {

	//private EMTDatabaseConnection m_jdbcUpdate; //database connect
	private EMTDatabaseConnection m_jdbcView; //for view only

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Vector Histogram = new Vector();

	private double[] stdDev; //store the standard deviation

	private int periodNum = 24; //24 hours/period per day

	private int totalDays = 53; //for met1

	static boolean temp1b = false;

	//private winGui m_wg;

	//constructor, connect to the db
	public HistogramCalculator(final EMTDatabaseConnection jdbcView) {

		if (jdbcView == null) {
			throw new NullPointerException();
		}

		//m_jdbcUpdate = jdbcUpdate;
		m_jdbcView = jdbcView;
		//m_wg = winguicopy;
	}//end of constructor

	// for those methods need standard dev with specified period, like past
	// week, month etc.
	public final Vector getHistogramWithDev(final String user,
			String[] userData, String period) throws SQLException {

		//	System.out.println("gethstdev1");

		String data[][] = null;
		String days[];//--[];
		String query;
		String query1 = "";
		Vector Histogram = new Vector();
		String maxDay = "";
		stdDev = new double[periodNum];
		//stdDev.clear();
		period = period.toLowerCase();
		synchronized (m_jdbcView) {
			//query = "select max(dates), to_days(max(dates)) -
			// to_days(min(dates)) from email where sender='"+user+"' ";

			//--query = "select max(dates), min(dates) from email where
			// sender='"+user+"' ";

			//days = m_wg.getUserInfo(user);//max,min,count
			//--m_jdbcView.getSQLData(query);
			// days = m_jdbcView.getRowData();
			//-- System.out.println("days1:" + days[0][1]);

			//-- if(days.length!=1 && days[0].length!=2){
			//-- System.err.println("select dates error");
			//-- throw new SQLException("Wrong number of cells");
			//-- }

			try {
				maxDay = userData[0];
				java.util.Date a = sdf.parse(userData[0]);
				java.util.Date b = sdf.parse(userData[1]);
				totalDays = 1 + (int) Utils.daysBetween(b, a);
				//totalDays = 1+(new Integer(days[0][1])).intValue();
			} catch (Exception e) {
				totalDays = 1;
			}
		}

		// give proper totalDays
		if (period.equals("last week")) {
			totalDays = 7;
			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 7)
					+ "'";
			//--maxDay+"' - interval 7 day)) ";
		} else if (period.equals("last half month")) {
			totalDays = 15;
			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 15)
					+ "'";
			//--('"+maxDay +"'- interval 15 day)) ";
		} else if (period.equals("last month")) {
			totalDays = 30;
			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 30)
					+ "'";
			//--('"+maxDay+"' - interval 30 day)) ";
		} else //period.equalsIgnoreCase("all time")
		{
			query1 = "";
		}

		int stddevTable[][] = new int[periodNum][2];
		synchronized (m_jdbcView) {

			//--if(m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY))
			//-- {
			//-- query="DECLARE GLOBAL TEMPORARY TABLE HistogramsTemp (period
			// int not null, dates date not null, msg_count int"+/*, primary key
			// (period, dates)*/") not logged";
			//-- }
			//-- else{
			//-- query="create temporary table HistogramsTemp (period int not
			// null, dates date not null, msg_count int, primary key (period,
			// dates))";
			//-- }
			//-- m_jdbcView.updateSQLData(query);

			try {
				//System.out.println("attempting");
				// 24 hours per day, hour(times) starts from 0
				//-- query ="insert into HistogramsTemp ";
				query = "select " + "ghour ,dates, "//--as period, dates, ";
						+ "count(*) as msg_count " + "from email "
						+ "where sender='" + user + "' " + query1
						+ "group by ghour, dates order by ghour";

				data = m_jdbcView.getSQLData(query);

				for (int i = 0; i < 24; i++) {
					stddevTable[i][0] = 0;
					stddevTable[i][1] = 0;
				}

				for (int i = 0; i < data.length; i++) {
					int n = Integer.parseInt(data[i][0]);
					int x = Integer.parseInt(data[i][2]);

					stddevTable[n][0] += (x * x);
					stddevTable[n][1] += x;

				}

			} catch (SQLException se) {
				System.out.println(se);
			}
		}

		// fill in the empty entries with 0
		//--for(int i=0;i<totalDays;i++)
		/*
		 * for(int j=0;j <24;j++){
		 * 
		 * 
		 * try{ //fill in the blanks String stmt = "insert into HistogramsTemp
		 * values "; stmt +="("+j+",
		 * '"+Utils.backIntervalFunction(maxDay,i)+"',0)"; //--maxDay+"' -
		 * interval "+i+" day, 0)"; //System.out.println("stmt: "+stmt);
		 * m_jdbcView.updateSQLData(stmt); } catch(Exception e){
		 * //System.out.println("insert error: "+e); }
		 *  }
		 */
		//--query="select period, stddev(msg_count), ";
		//--query+="sum(msg_count)/"+totalDays+" ";
		//--q/uery+="from HistogramsTemp ";
		//--query+="group by period";
		//-- data = m_jdbcView.getSQLData(query);
		// data = m_jdbcView.getRowData();
		//--}
		//--finally{
		/*
		 * for(int i=0;i <data.length;i++) { for(int j=0;j <data[i].length;j++)
		 * System.out.print(data[i][j] + " "); System.out.println(""); }
		 */
		//--if(!m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY))
		//--{
		//-- m_jdbcView.updateSQLData("drop table HistogramsTemp");
		//--}
		//--}
		//--}

		int j = 0;

		for (int i = 0; i < periodNum; i++) {
			Histogram.insertElementAt((new Double(0)), i);
			//stdDev.insertElementAt((new Double(0)),i);
		}

		//System.out.println("total: " + totalDays);
		for (int i = 0; i < periodNum; i++) {

			//if(data[i].length!=3){
			//	System.err.println("wrong number of columns returned");
			//throw new SQLException("Wrong number of columns returned");
			// }
			// Integer Int = new Integer(i);//--data[i][0]);
			if (stddevTable[i][1] > 0) {
				double d_stddev = Math
						.sqrt((double) (totalDays * stddevTable[i][0] - (stddevTable[i][1] * stddevTable[i][1]))
								/ (double) (totalDays * totalDays));
				stdDev[i] = d_stddev;
				//stdDev.set(i,(new Double(d_stddev)));//(String)data[i][1]));
				Histogram.set(i, (new Double((double) stddevTable[i][1]
						/ (double) totalDays)));
			}
		}

		return Histogram;

	}

	// Another one for histogram with Stand Dev, with specified start date and
	// end date
	public final double[] getHistogramWithDev(final String user,
			userShortProfile userData, String startDate, final String endDate)
			throws SQLException {
		//System.out.println("gethsdev2" + startDate +" " + endDate);
		String data[][] = null;//--[];
		String days[];
		String query = null;
		//Vector Histogram = new Vector();
		double[] Histogram = new double[periodNum];
		stdDev = new double[periodNum];
		//stdDev.clear();

		//String minDate = "";
		//String maxDate = "";

		//-- query = "select max(dates), min(dates) from email where
		// sender='"+user+"' ";
		//days = m_wg.getUserInfo(user);
		//--m_jdbcView.getSQLData(query);

		//-- if(data.length!=1 && data[0].length!=2){
		//-- System.err.println("select dates error");
		//-- throw new SQLException("Wrong number of cells");
		//-- }

		//maxDate = userData[winGui.User_max];
		//minDate = userData[winGui.User_min];

		if ((userData.getStartDate().compareTo(startDate)) > 0)
			throw (new SQLException(
					"the start date of getting profile histogram is too small, no data of so early date"));

		startDate = maxDate(userData.getStartDate(), startDate);
		//endDate = minDate(maxDate, endDate);

		//System.out.println("getting stddev histogram for user: "+user+",
		// start day:"+startDate+", end day:"+endDate);
		try {
			java.util.Date early = sdf.parse(startDate);
			java.util.Date later = sdf.parse(endDate);
			totalDays = 1 + (int) Utils.daysBetween(early, later);
		} catch (Exception e) {
			totalDays = 1;
		}

		//-- totalDays = periodLength(endDate, startDate);

		int stddevTable[][] = new int[periodNum][2];

		synchronized (m_jdbcView) {
			/*
			 * if(m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY)) {
			 * if(!temp1b) { query="create TABLE HistogramsTemp1 (period int not
			 * null, dates date not null, msg_count int,primary key (period,
			 * dates)) "; temp1b=true;
			 *  } } else { query="create temporary table HistogramsTemp1 (period
			 * int not null, dates date not null, msg_count int, primary key
			 * (period, dates))"; }
			 *  
			 */
			//System.out.println("create temp: " +
			// m_jdbcView.updateSQLData(query));
			try {

				// 24 hours per day, hour(times) starts from 0
				//-- query ="insert into HistogramsTemp1 ";
				query = "select ";
				query += "ghour, dates, ";
				query += "count(*) as msg_count ";
				query += "from email ";
				query += "where sender='" + user + "' ";
				query += "and dates>='" + startDate + "' and dates<='"
						+ endDate + "' "; //specify time
				query += "group by ghour, dates order by ghour";
				//  System.out.println(query);

				//System.out.println("sqlwork: "+query);

				data = m_jdbcView.getSQLData(query);

				for (int i = 0; i < periodNum; i++) {
					stddevTable[i][0] = 0;
					stddevTable[i][1] = 0;
				}

				for (int i = 0; i < data.length; i++) {
					int n = Integer.parseInt(data[i][0]);
					int x = Integer.parseInt(data[i][2]);

					stddevTable[n][0] += (x * x);
					stddevTable[n][1] += x;

				}

			} catch (SQLException se) {
				System.out.println(se);
			}
		}

		/*
		 *  // fill in the empty entries with 0 for(int i=0;i <totalDays;i++)
		 * for(int j=0;j <24;j++){
		 * 
		 * try{ String stmt = "insert into HistogramsTemp1 values "; stmt
		 * +="("+j+", '"+Utils.backIntervalFunction(endDate,i)+"',0)";
		 * //--endDate+"' - interval "+i+" day, 0)"; System.out.println("stmt:
		 * "+stmt); m_jdbcView.updateSQLData(stmt); } catch(Exception e){
		 * System.out.println("insert error: "+e); } }
		 * 
		 * query="select period, stddev(msg_count), ";
		 * query+="sum(msg_count)/"+totalDays+" "; query+="from HistogramsTemp1 ";
		 * query+="group by period"; System.out.println("query"+query); data =
		 * m_jdbcView.getSQLData(query);
		 * 
		 *  }
		 * 
		 * finally{
		 *  /* for(int i=0;i <data.length;i++) { for(int j=0;j
		 * <data[i].length;j++) System.out.print(data[i][j] + " ");
		 * System.out.println(""); } /
		 * //if(!m_jdbcView.getTypeDB().equals(DatabaseManager.sDERBY)) {
		 * m_jdbcView.updateSQLData("drop table HistogramsTemp1");
		 *  } } }
		 */
		//int j=0;
		//for(int i=0;i<periodNum;i++){
		//   Histogram[i] =0;
		//Histogram.insertElementAt((new Double(0)),i);
		// stdDev[i] =0;
		//stdDev.insertElementAt((new Double(0)),i);
		//}
		//System.out.println("total: " + totalDays);

		for (int i = 0; i < periodNum; i++) {
			//if(data[i].length!=3){
			//	System.err.println("wrong number of columns returned");
			//throw new SQLException("Wrong number of columns returned");
			// }
			// Integer Int = new Integer(i);//--data[i][0]);
			if (stddevTable[i][1] > 0) {
				//System.out.println(stddevTable[i][0] +" " +
				// stddevTable[i][1]);

				stdDev[i] = Math
						.sqrt((double) (totalDays * stddevTable[i][0] - (stddevTable[i][1] * stddevTable[i][1]))
								/ (double) (totalDays * totalDays));
				// stdDev.set(i,(new Double(d_stddev)));//(String)data[i][1]));
				//   Histogram.set(i,(new
				// Double((double)stddevTable[i][1]/(double)totalDays)));
				Histogram[i] = ((double) stddevTable[i][1] / (double) totalDays);
			} else {
				Histogram[i] = 0;
				stdDev[i] = 0;
			}
		}

		return Histogram;

	}

	/*
	 * int j=0;
	 * 
	 * for(int i=0;i <periodNum;i++){ Histogram.insertElementAt((new
	 * Double(0)),i); stdDev.insertElementAt((new Double(0)),i); }
	 * 
	 * 
	 * for(int i=0;i <data.length;i++){
	 * 
	 * //if(data[i].length!=3){ // System.err.println("wrong number of columns
	 * returned"); //throw new SQLException("Wrong number of columns returned");
	 * //} Integer Int = new Integer(data[i][0]); stdDev.set(Int.intValue(),(new
	 * Double(data[i][1]))); Histogram.set(Int.intValue(),(new
	 * Double(data[i][2]))); }
	 * 
	 * return Histogram;
	 *  }
	 *  
	 */

	public final double[] get_stdDev() {
		return stdDev;
	}

	/***************************************************************************
	 * functions to construct the histograms
	 */

	//calculate and return the usage histogram for user
	//in 24 hour style, return UsageHistogram kind of object
	public final double[] getHistogram(final String user,
			final userShortProfile userData) throws SQLException {

		String data[][] = null;
		String days[];
		//String query;
		double[] Histogram = new double[periodNum];

	
		if (userData == null) {
			//1 && days[0].length!=1){
			System.err.println("select dates error");
			throw new SQLException("select dates error");
		}

		try {

			java.util.Date later = sdf.parse(userData.getEndDate());
			java.util.Date early = sdf.parse(userData.getStartDate());
			totalDays = 1 + (int) Utils.daysBetween(early, later);
			// System.out.println(totalDays+ " ==="+ user);
			//totalDays = 1+(new Integer(days[0][0])).intValue();
		} catch (Exception e) {
			System.out.println("e1" + e);
			totalDays = 1;
		}
		//--}
		try {
			synchronized (m_jdbcView) {

				data = m_jdbcView
						.getSQLData("select ghour,count(*) from email where sender ='"
								+ user + "' group by ghour");
			}
		} catch (SQLException se1) {
			System.out.println("se1:" + se1);
		}

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			//Histogram.insertElementAt((new Double(0)),i);
		}
		double dval;
		if (data != null) {
			for (int i = 0; i < data.length; i++) {

				Integer Int = new Integer(data[i][0]);
				try {
					dval = Double.parseDouble(data[i][1]) / totalDays;
				} catch (NumberFormatException def) {
					System.out.println(def);
					dval = 0;
				}

				Histogram[Int.intValue()] = dval;
				//Histogram.set(Int.intValue(),new Double(dval));
			}
		}
		return Histogram;

	}
/*
	public final double[] getHistogram(final String user, final String desUser,
			final String[] userData) throws SQLException {

		String data[][] = null;
		String days[];
		//String query;
		double[] Histogram = new double[periodNum];

		if (userData == null) {
			System.err.println("select dates error");
			throw new SQLException("select dates error");
		}

		try {

			java.util.Date a = sdf.parse(userData[0]);
			java.util.Date b = sdf.parse(userData[1]);
			totalDays = 1 + (int) Utils.daysBetween(b, a);
		} catch (Exception e) {
			System.out.println("e1" + e);
			totalDays = 1;
		}
		try {
			synchronized (m_jdbcView) {

				data = m_jdbcView
						.getSQLData("select ghour,count(*) from email where sender ='"
								+ user
								+ "' and rcpt = '"
								+ desUser
								+ "' group by ghour");
			}
		} catch (SQLException se1) {
			System.out.println("se1:" + se1);
		}

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			//Histogram.insertElementAt((new Double(0)),i);
		}
		double dval;
		if (data != null) {
			for (int i = 0; i < data.length; i++) {

				Integer Int = new Integer(data[i][0]);
				try {
					dval = Double.parseDouble(data[i][1]) / totalDays;
				} catch (NumberFormatException def) {
					System.out.println(def);
					dval = 0;
				}

				Histogram[Int.intValue()] = dval;
				//Histogram.set(Int.intValue(),new Double(dval));
			}
		}
		return Histogram;

	}
*/
	// similar to perious one, has one more period argument
	public final double[] getHistogram(final String user, userShortProfile userData,
			String period) throws SQLException {

		period = period.toLowerCase();

		if (period.equals("all time"))
			return getHistogram(user, userData);

		double[] Histogram = new double[periodNum];
		String data[][];
		//String query;
		String query1 = "";

		String maxDay = "";
		synchronized (m_jdbcView) {
			maxDay = m_jdbcView.getSQLDataByColumn(
					"select max dates from email where user = '" + user + "'",
					1)[0];
			// maxDay =((String[]) m_jdbcView.getColumnData(0))[0];
		}
		//	maxDay="2002-08-14"; //give a time limit, for demo, not for real time

		// different time period for the histogram
		if (period.equals("past 1 day")) {

			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 1)
					+ "'";
			//-- maxDay+"' - interval 1 day)) ";
			totalDays = 1;

		} else if (period.equals("past 3 days")) {

			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 3)
					+ "'";
			//--maxDay+"' - interval 3 day)) ";
			totalDays = 3;

		} else if (period.equals("past week")) {

			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 7)
					+ "'";
			//--maxDay+"' - interval 7 day)) ";
			totalDays = 7;

		} else if (period.equals("past half month")) {

			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 15)
					+ "'";
			//--maxDay+"' - interval 15 day)) ";
			totalDays = 15;

		} else if (period.equals("past month")) {

			query1 = " and dates >= '" + Utils.backIntervalFunction(maxDay, 30)
					+ "'";
			//--maxDay+"' - interval 30 day)) ";
			totalDays = 30;

		}

		synchronized (m_jdbcView) {
			data = m_jdbcView
					.getSQLData("select ghour,count(*)  from email where sender ='"
							+ user + "' " + query1 + " group by ghour");
		}

		//int j=0;

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			//Histogram.insertElementAt((new Double(0)),i);
		}

		double dval;
		for (int i = 0; i < data.length; i++) {

			Integer Int = new Integer(data[i][0]);
			try {
				dval = Double.parseDouble(data[i][1]) / totalDays;
			} catch (NumberFormatException num) {
				System.out.println(num);
				dval = 0;
			}

			Histogram[Int.intValue()] = dval;
			//Histogram.set(Int.intValue(),new Double(dval));
		}

		return Histogram;

	}

	// compute the histogram with specified startDate and endDate
	// need to adjust when the startDate is too small, or endDate is too large
	// temporary sln: get the min(minDate, startDate), max(maxDate, endDate)
	// or min(minDate, startDate), and just endDate?
	public final double[] getHistogram(final String user,
			final userShortProfile userData, String startDate, final String endDate)
			throws SQLException {

		String data[][];
		String days[];
		
		//String minDate = "";
		//String maxDate = "";
//		maxDate = userData[0];
//		minDate = userData[1];

		/*if ((userData.getStartDate().compareTo(startDate)) > 0)
			throw (new SQLException(
					"the start date of getting recent histogram is too small, no data of so early date"));
*/
		startDate = maxDate(userData.getStartDate(), startDate);
		//endDate = minDate(maxDate, endDate);

		//	System.out.println("getting histogram for user: "+user+", start
		// day:"+startDate+", end day:"+endDate);
		try {
			java.util.Date a = sdf.parse(startDate);
			java.util.Date b = sdf.parse(endDate);
			totalDays = 1 + (int) Utils.daysBetween(a, b);
		} catch (Exception e) {
			totalDays = 1;
		}

		//System.out.println("totaldyas:"+totalDays +" start"+startDate+" " +
		// endDate);

		//totalDays = periodLength(endDate, startDate);

		synchronized (m_jdbcView) {

			data = m_jdbcView
					.getSQLData("select ghour,count(*) from email where sender ='"
							+ user
							+ "' and dates>='"
							+ startDate
							+ "' and dates<='" + endDate + "' group by ghour");

		}
		/*
		 * 
		 * 
		 * query="create temporary table HistogramsTemp (period int, dates date,
		 * msg_count int)"; m_jdbcView.executeQuery(query);
		 * 
		 * try{
		 *  // 24 hours per day, hour(times) starts from 0 query ="insert into
		 * HistogramsTemp "; query+="select "; query+="hour(times) as period,
		 * dates, "; query+="count(*) as msg_count "; query+="from email ";
		 * query+="where (sender='"+user+"') "; query+="and
		 * dates>='"+startDate+"' and dates <='"+endDate+"' "; //specify time
		 * query+=" group by period, dates";
		 * 
		 * m_jdbcView.executeQuery(query);
		 * 
		 * query="select period, "; query+="sum(msg_count)/"+totalDays+" ";
		 * query+="from HistogramsTemp "; query+="group by period";
		 * 
		 * m_jdbcView.getSqlData(query); data = m_jdbcView.getRowData();
		 *  }
		 * 
		 * finally{
		 * 
		 * 
		 * for(int i=0;i <data.length;i++) { for(int j=0;j <data[i].length;j++)
		 * System.out.print(data[i][j] + " "); System.out.println(""); }
		 * 
		 * m_jdbcView.executeQuery("drop table HistogramsTemp"); } }
		 */

		int j = 0;
		double[] Histogram = new double[periodNum];

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			// Histogram.insertElementAt((new Double(0)),i);
		}
		double dval;
		for (int i = 0; i < data.length; i++) {

			//--if(data[i].length!=2){
			//-- System.err.println("wrong number of columns returned");
			//--throw new SQLException("Wrong number of columns returned");
			//--}
			Integer Int = new Integer(data[i][0]);
			try {
				dval = Double.parseDouble(data[i][1]) / totalDays;
			} catch (NumberFormatException num) {
				System.out.println(num);
				dval = 0;
			}

			///"+totalDays+"
			// Histogram.set(Int.intValue(),new Double(dval));
			Histogram[Int.intValue()] = dval;
		}

		return Histogram;

	}//end get histogram
	public final double[] getHistogram(final String user,final String desUser,
			final userShortProfile userData, String startDate, final String endDate)
			throws SQLException {

		String data[][];
		String days[];
		
		String minDate = "";
		String maxDate = "";

				maxDate = userData.getEndDate();
		minDate = userData.getStartDate();

		if ((minDate.compareTo(startDate)) > 0)
			throw (new SQLException(
					"the start date of getting recent histogram is too small, no data of so early date"));

		startDate = maxDate(minDate, startDate);
		try {
			java.util.Date a = sdf.parse(startDate);
			java.util.Date b = sdf.parse(endDate);
			totalDays = 1 + (int) Utils.daysBetween(a, b);
		} catch (Exception e) {
			totalDays = 1;
		}

		synchronized (m_jdbcView) {

			data = m_jdbcView
					.getSQLData("select ghour,count(*) from email where sender ='"
							+ user
							+ "' and rcpt = '"+desUser+"' and dates>='"
							+ startDate
							+ "' and dates<='" + endDate + "' group by ghour");

		}
	
		int j = 0;
		double[] Histogram = new double[periodNum];

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			// Histogram.insertElementAt((new Double(0)),i);
		}
		double dval;
		for (int i = 0; i < data.length; i++) {

			Integer Int = new Integer(data[i][0]);
			try {
				dval = Double.parseDouble(data[i][1]) / totalDays;
			} catch (NumberFormatException num) {
				System.out.println(num);
				dval = 0;
			}

			Histogram[Int.intValue()] = dval;
		}

		return Histogram;

	}//end get histogram
	//compare between 2 dates, return the smaller/earlier one;
	private final String minDate(final String d1, final String d2) {

		if ((d1.compareTo(d2)) > 0) //date d1 is later than d2
			return d2;

		return d1;
	}

	//compare between 2 dates, return the larger/later one;
	private final String maxDate(final String d1, final String d2) {

		if ((d1.compareTo(d2)) > 0) //date d1 is later than d2
			return d1;

		return d2;
	}

	// how many days are there between these two date, d1 is later than d2
	/*
	 * private int periodLength(final String d1, final String d2) throws
	 * SQLException{
	 * 
	 * String query; String data[][]; String days; int dayNum=1;
	 * 
	 * synchronized(m_jdbcView){ // query = "select
	 * (to_days('"+d1+"')-to_days('"+d2+"'))"; // data =
	 * m_jdbcView.getSQLData(query); // System.out.println("days3:" +
	 * data[0][0]);
	 * 
	 * //if(data.length!=1 && data[0].length!=1){ //System.err.println("select
	 * dates error"); //throw new SQLException("Wrong number of cells"); // }
	 * try{ java.util.Date a = sdf.parse(d1); java.util.Date b = sdf.parse(d2);
	 * dayNum =1+(int)Utils.daysBetween(a,b); }catch(ParseException ps){
	 * System.out.println("ps"+ps); dayNum =1; } //days = (String)data[0][0];
	 *  }
	 * 
	 * //try{ // dayNum = Integer.parseInt(days); // }
	 * //catch(NumberFormatException e){ // System.out.println("period length
	 * number error:"+e); // dayNum =0; // }
	 * 
	 * 
	 * //System.out.println("there are "+(dayNum+1)+" days between "+d1+",
	 * "+d2); return dayNum;//+1; }
	 */
	//calculate and return the usage histogram for user
	//based on the avg size of email in Kbytes sent out per hour
	public final double[] getHistogramOfEmailSize(final String user)
			throws SQLException {

		//--String data[][];
		//--String query;
		double[] Histogram = new double[periodNum];
		double sizes[] = new double[24];
		int sizecnt[] = new int[24];

		for (int i = 0; i < 24; i++) {
			sizes[i] = 0;
			sizecnt[i] = 0;
		}

		int c;
		synchronized (m_jdbcView) {

			// 24 hours per day, hour(times) starts from 0
			//--query ="select ";
			//--query+="ghour, avg(size)/1024 ";
			//--query+="from email ";
			//--query+="where sender='"+user+"' ";
			//--query+="group by ghour";

			//--data = m_jdbcView.getSQLData(query);
			PreparedStatement ps = m_jdbcView
					.prepareStatementHelper("select ghour,size from email where sender =?");
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				c = rs.getInt(1);
				sizecnt[c]++;
				sizes[c] += rs.getInt(2);
			}
			rs.close();

		}

		for (int i = 0; i < periodNum; i++) {

			Histogram[i] = 0;
			// Histogram.insertElementAt(new Double(0),i);
		}

		for (int i = 0; i < periodNum; i++) {//--data.length;i++){

			//--if(data[i].length!=2){
			//-- System.err.println("wrong number of columns returned");
			//-- throw new SQLException("Wrong number of columns returned");
			//-- }
			//-- Integer Int = new Integer(data[i][0]);
			if (sizecnt[i] > 0) {
				double d = sizes[i] / sizecnt[i];
				d = d / 1024;
				Histogram[i] = d;
				//Histogram.set(i,new Double(d));
			}
		}

		return Histogram;

	}

	//calculate and return the usage histogram for user
	//based on the avg number of attachment sent out per hour
	public final double[] getHistogramOfAttachment(final String user)
			throws SQLException {

		//--String data[][];
		//--String query;
		double[] Histogram = new double[periodNum];//new Vector();
		double numattach[] = new double[24];
		int attcnt[] = new int[24];
		int c;
		for (int i = 0; i < 24; i++) {
			attcnt[i] = 0;
			numattach[i] = 0;
		}
		synchronized (m_jdbcView) {

			// 24 hours per day, hour(times) starts from 0
			//--query ="select ";
			//--query+="ghour, avg(numattach) ";
			//--query+="from email ";
			//--query+="where sender='"+user+"' ";
			//--query+="group by ghour";
			//-- data = m_jdbcView.getSQLData(query);
			PreparedStatement ps = m_jdbcView
					.prepareStatementHelper("select ghour,numattach from email where sender =?");
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				c = rs.getInt(1);
				numattach[c] += rs.getInt(2);
				attcnt[c]++;
			}
			rs.close();

		}

		//--}

		//int j=0;

		for (int i = 0; i < periodNum; i++) {
			Histogram[i] = 0;
			//Histogram.insertElementAt((new Double(0)),i);
		}

		for (int i = 0; i < periodNum; i++) {//data.length;i++){

			//-- if(data[i].length!=2){
			//-- System.err.println("wrong number of columns returned");
			//-- throw new SQLException("Wrong number of columns returned");
			//-- }
			//-- Integer Int = new Integer(data[i][0]);
			if (attcnt[i] > 0)
				Histogram[i] = (numattach[i] / attcnt[i]);
			//Histogram.set(i,new Double(numattach[i]/attcnt[i]));
		}

		return Histogram;

	}

	//calculate and return the usage histogram for user
	//based on the avg number of recipient for emails sent out per hour
	public final double[] getHistogramOfRecipient(final String user,
			final userShortProfile userData) throws SQLException {

		String data[][];
		//--String query;
		double[] Histogram = new double[periodNum];//Vector();
		String days[];
		int numrcpts[] = new int[24];
		//--synchronized(m_jdbcView){
		//--query = "select min(dates), max(dates) from email where
		// sender='"+user+"' ";
		//query = "select min(dates), to_days(max(dates)) - to_days(min(dates))
		// from email where sender='"+user+"' ";

		//days =m_wg.getUserInfo(user);//-- m_jdbcView.getSQLData(query);
		//days = m_jdbcView.getRowData();

		//--if(days.length!=1 && days[0].length!=2){
		//--System.err.println("select dates error");
		//--throw new SQLException("Wrong number of cells");
		//-- }

		try {
			java.util.Date early = sdf.parse(userData.getStartDate());
			java.util.Date later = sdf.parse(userData.getEndDate());
			totalDays = 1 + (int) Utils.daysBetween(early, later);

			//totalDays = 1+(new Integer(days[0][1])).intValue();
		} catch (Exception e) {
			totalDays = 1;
		}
		//--}

		synchronized (m_jdbcView) {
			PreparedStatement ps = m_jdbcView
					.prepareStatementHelper("select ghour,numrcpt from email where sender=? order by ghour");
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int c = rs.getInt(1);
				numrcpts[c] += rs.getInt(2);
			}

			rs.close();

			//data = m_jdbcView.getSQLData("select ghour,numrcpts from email
			// where sender ='"+user+"' group by ghour");
			/*
			 * 
			 * query="create temporary table HistogramsTemp (period int,
			 * rcpt_count int)"; m_jdbcView.executeQuery(query);
			 * 
			 * try{
			 *  // 24 hours per day, hour(times) starts from 0 query ="insert
			 * into HistogramsTemp "; query+="select "; query+="hour(times) as
			 * period, numrcpt "; query+="from email "; query+="where
			 * sender='"+user+"' "; query+="group by mailref ";
			 * 
			 * m_jdbcView.executeQuery(query);
			 * 
			 * 
			 * query="select period, "; query+="sum(rcpt_count)/"+totalDays+" ";
			 * query+="from HistogramsTemp "; query+="group by period";
			 * 
			 * m_jdbcView.getSqlData(query); data = m_jdbcView.getRowData();
			 *  }
			 * 
			 * finally{ m_jdbcView.executeQuery("drop table HistogramsTemp"); }
			 */
		}

		int j = 0;

		//for(int i=0;i<periodNum;i++){
		//   Histogram[i]=0;
		// Histogram.insertElementAt((new Double(0)),i);
		//}

		//double dval;
		for (int i = 0; i < periodNum; i++) {//data.length;i++){

			//-- if(data[i].length!=2){
			//-- System.err.println("wrong number of columns returned");
			//--throw new SQLException("Wrong number of columns returned");
			//-- }
			//--Integer Int = new Integer(data[i][0]);

			//try{
			// 	dval = Double.parseDouble(data[i][1])/totalDays ;
			//}catch(NumberFormatException dd){
			//	System.out.println(dd);
			//	dval =0;
			//}

			Histogram[i] = (numrcpts[i] / totalDays);
			//  Histogram.set(i,new
			// Double(numrcpts[i]/totalDays));//--((String)data[i][1])));
		}

		return Histogram;

	}
}


