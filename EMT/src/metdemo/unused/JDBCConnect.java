package metdemo.unused;

//import java.net.*;
import java.io.*;

import java.util.Vector;
//import java.util.StringTokenizer;
import java.sql.*;

import metdemo.DataBase.DBConnect;



/**
 * <code>JDBConnect</code> class handles communication with the Data Warehouse
 * using mysql driver connection.
 */
public class JDBCConnect extends DBConnect {

	final String default_db_url = "jdbc:mysql:127.0.0.1";

	/** user name. */
	String user = "user";
	/** password. */
	String passwd = "password";

	/** driver url. */
	String url = default_db_url;
	/** driver name. */
	//String driverName = "org.gjt.mm.mysql.Driver";
	String driverName = "com.mysql.jdbc.Driver";

	Connection connection;
	Statement statement;
	ResultSet resultSet;
	ResultSetMetaData metaData;
	String sql;
	String table = "ps";

	String[] columnNames;
	Vector result;
	String[][] rowdata;
	int max_rows, numberOfColumns;

	//precompiled statements for speedups
	//PreparedStatement ps_getcount, ps_getcount_date, ps_body_by_mailref;

	/**
	 * Constructor
	 * 
	 * @param url
	 *            driver url
	 * @param driverName
	 *            driver name
	 * @param user
	 *            user name
	 * @param passwd
	 *            passowrd
	 */
	public JDBCConnect(String url, String driverName, String user, String passwd) {
		this.url = url;
		this.driverName = driverName;
		this.user = user;
		this.passwd = passwd;
	}

	/**
	 * Constructor
	 * 
	 * @param url
	 *            driver url
	 */
	public JDBCConnect(String url) {
		this.url = url;
	}

	/**
	 * Constructor
	 */
	public JDBCConnect() {
	}

	public boolean connect() {
		try {
			Class.forName(driverName);
							
			connection = DriverManager.getConnection(url, user, passwd);

			statement = connection.createStatement();

			statement.setEscapeProcessing(false);

			//TODO: add stuff here
			//do prepared statements
			return true;
		} catch (ClassNotFoundException ex) {
			System.err.println("Cannot find the database driver classes.");
			System.err.println(ex);
			return false;
		} catch (SQLException ex) {
			System.err.println("Cannot connect to this database.");
			System.err.println(ex);
			return false;
		}

	}

	public boolean shutdown() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	//TODO: add fast stuff here
	public synchronized int getCountDate(String date) {

		try {
			PreparedStatement ps_getcount_date = connection.prepareStatement("select count(*) from email where dates >= ?");

			//	resultSet = statement.executeQuery(query);
			//if (ps_getcount_date != null) {
			ps_getcount_date.setString(1, date);
			resultSet = ps_getcount_date.executeQuery();
			//	} else
			//	resultSet = statement.executeQuery("select count(*) from email
			// where dates>='" + date + "'");

			if (resultSet.next()) {
				max_rows = resultSet.getInt(1);
			}
			resultSet.close();
		} catch (SQLException ex) {
			//ex.printStackTrace();
			max_rows = -1;
		}

		return max_rows;
	}

	public synchronized int getCount(String table) {

		//TODO
		//System.out.println("TESTMSG:getcountdb");
		//		String query = "SELECT count(*) FROM " + table;
		try {
			//	resultSet = statement.executeQuery(query);
			PreparedStatement ps_getcount = connection.prepareStatement("SELECT count(*) FROM ?");

			//if (ps_getcount != null) {
			ps_getcount.setString(1, table);
			resultSet = ps_getcount.executeQuery();
			//			} else
			//			resultSet = statement.executeQuery("SELECT count(*) FROM " +
			// table);

			if (resultSet.next()) {
				max_rows = resultSet.getInt(1);
			}
			resultSet.close();
		} catch (SQLException ex) {
			//ex.printStackTrace();
			max_rows = -1;
		}

		return max_rows;

	}

	public Connection getConnection() {
		return connection;
	}

	/*
	public void executeBinaryQuery(String query, int bincolumn) throws SQLException {
		try {
			if (statement.execute(query)) {
				resultSet = statement.getResultSet();
			} else
				return;
			//      resultSet = statement.executeQuery(query);
			if (resultSet == null) {
				throw new SQLException("Could not execute query");
			}
			resultSet.close();
		} catch (SQLException e) {
			if (resultSet != null) {
				resultSet.close();
			}
			throw e;
		}
	}
*/
	public void executeQuery(String query) throws SQLException {
	//	System.out.println("q:" + query);

		try {
			if (statement.execute(query)) {
				resultSet = statement.getResultSet();
			} else
				return;
			//      resultSet = statement.executeQuery(query);
			if (resultSet == null) {
				throw new SQLException("Could not execute query");
			}
			resultSet.close();
		} catch (SQLException e) {
			if (resultSet != null) {
				resultSet.close();
			}
			throw e;
		}
	}
	public boolean getSqlData(String query) throws SQLException {
		//System.out.println("q2:" + query);
		boolean flag = false;
		try {
			resultSet = statement.executeQuery(query);
			metaData = resultSet.getMetaData();

			numberOfColumns = metaData.getColumnCount();

			columnNames = new String[numberOfColumns];
			// Get the column names and cache them.
			for (int column = 0; column < numberOfColumns; column++) {
				columnNames[column] = metaData.getColumnLabel(column + 1);
			}

			// Get all rows.
			result = new Vector();
			while (resultSet.next()) {
				String[] temp = new String[numberOfColumns];
				for (int i = 1; i <= numberOfColumns; i++) {
					Object obj = resultSet.getObject(i);
					if (obj != null) {
						//System.out.print(" "+obj.getClass().getName());
						if (obj.getClass().getName().startsWith("[B"))//   ||obj
						// instanceof
						// java.sql.Blob)
						{
							//			System.out.println("got blob");
							try {
								java.sql.Blob blob = resultSet.getBlob(i);
								InputStream is = blob.getBinaryStream();
								int c;
								StringBuffer small2 = new StringBuffer(256);
								byte buf[] = new byte[256];
								while ((c = is.read(buf)) != -1)
									small2.append(new String(buf, 0, c));

								temp[i - 1] = small2.toString();
							} catch (IOException e) {
								temp[i - 1] = "null";
							}

						} else
							temp[i - 1] = (resultSet.getObject(i)).toString();
					} else {
						temp[i - 1] = "null";
					}
				}
				result.addElement(temp);
			}
			resultSet.close();

			flag = true;
		} catch (SQLException ex) {
			throw ex;
			//String message = ex.toString();
			//System.err.println(message);
			//ex.printStackTrace();
		}

		return flag;
	}

	public synchronized String[] getBodyByMailref(String mailref) throws SQLException {
		//System.out.println("getting");
		PreparedStatement ps_body_by_mailref = connection.prepareStatement("select body from message where mailref = ?");
		
		//if (ps_body_by_mailref != null) {
			ps_body_by_mailref.setString(1, mailref);
			resultSet = ps_body_by_mailref.executeQuery();
		//} else
			//resultSet = statement.executeQuery("select body from message where mailref ='" + mailref + "'");

		metaData = resultSet.getMetaData();

		numberOfColumns = metaData.getColumnCount();

		columnNames = new String[numberOfColumns];
		// Get the column names and cache them.
		for (int column = 0; column < numberOfColumns; column++) {
			columnNames[column] = metaData.getColumnLabel(column + 1);
		}

		// Get all rows.
		result = new Vector();
		InputStream is;
		int c;
		byte buf[] = new byte[256];

		while (resultSet.next()) {
			//String[] temp = new String[numberOfColumns];
			String temp = new String();
			//for (int i = 1; i <= numberOfColumns; i++)

			try {
				java.sql.Blob blob = resultSet.getBlob(1);
				is = blob.getBinaryStream();
				StringBuffer small2 = new StringBuffer(256);
				while ((c = is.read(buf)) != -1)
					small2.append(new String(buf, 0, c));

				temp = small2.toString();
			} catch (IOException e) {
				temp = "null";
			}

			result.addElement(temp);
		}
		resultSet.close();

		return (String[]) result.toArray(new String[result.size()]);
	}

	public byte[] getBinSqlData(String query) throws SQLException {
		byte[] results = null;
		try {
			resultSet = statement.executeQuery(query);
			metaData = resultSet.getMetaData();
			numberOfColumns = metaData.getColumnCount();
			columnNames = new String[numberOfColumns];
			// Get the column names and cache them.
			for (int column = 0; column < numberOfColumns; column++) {
				columnNames[column] = metaData.getColumnLabel(column + 1);
			}

			// Get all rows.
			//result = new Vector();
			while (resultSet.next()) {
				results = resultSet.getBytes(1);
			}

			/*
			 * byte[] temp = new String[numberOfColumns]; for (int i = 1; i <=
			 * numberOfColumns; i++) { Object obj = resultSet.getObject(i);
			 * if(obj != null) { //System.out.print("
			 * "+obj.getClass().getName());
			 * if(obj.getClass().getName().startsWith("[B"))// ||obj instanceof
			 * java.sql.Blob) { // System.out.println("got blob"); try{
			 * java.sql.Blob blob = resultSet.getBlob(i); InputStream is =
			 * blob.getBinaryStream(); int c; StringBuffer small2 = new
			 * StringBuffer(256); byte buf[] = new byte[256]; while ((c =
			 * is.read(buf)) != -1) small2.append(new String(buf,0,c));
			 * 
			 * 
			 * temp[i-1] = small2.toString(); }catch(IOException
			 * e){temp[i-1]="null";} } else temp[i-1] =
			 * (resultSet.getObject(i)).toString(); } else { temp[i-1] = "null"; } }
			 * result.addElement(temp); } resultSet.close();
			 * 
			 * flag = true;
			 */
		} catch (SQLException ex) {
			throw ex;
			//String message = ex.toString();
			//System.err.println(message);
			//ex.printStackTrace();
		}
		return results;
		/*
		 * return flag;
		 */
	}

	public String[] getColHeader() {
		return columnNames;
	}

	//shlomo s02
	public int getResultSize() {
		return result.size();
	}

	public String[][] getRowData() {
		rowdata = new String[result.size()][numberOfColumns];
		for (int i = 0; i < result.size(); i++) {
			String[] temp = (String[]) result.elementAt(i);
			for (int j = 0; j < temp.length; j++) {
				rowdata[i][j] = temp[j];
			}
		}

		return rowdata;
	}

	public final void clean() {
		result.clear();
		rowdata = null;
	}

	public String[] getColumnData(int n) {
		String rowdata[] = new String[result.size()];

		//instead of throwing exception assume some columns exist
		if (n > numberOfColumns)
			n = 0;

		for (int i = 0; i < result.size(); i++) {
			String[] temp = (String[]) result.elementAt(i);
			rowdata[i] = temp[n];
		}

		return rowdata;
	}

	/**
	 * For testing.
	 */
	public static void main(String args[]) {
		JDBCConnect jc = new JDBCConnect();
		jc.connect();
		try {
			jc.getSqlData("select * from abc");
		} catch (SQLException e) {
			System.err.println(e);
		}
		//System.err.println("number of columns in ps " + jc.getCount("ps"));
		//String[][] temp = jc.getRowData();
		jc.shutdown();
	}

}

