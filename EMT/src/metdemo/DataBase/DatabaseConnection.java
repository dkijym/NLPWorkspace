/*
 * Created on Sep 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @author Shlomo Hershkop
 * 
 * The idea is to create an interface of what the database connection should look like to the outside world
 * 
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


public interface DatabaseConnection {
	/**
	 * Open a connection to some underlying database
	 * @param createTables if we should create a table in the database (when it doesnt exist)
	 * @return true on success
	 * @throws DatabaseConnectionException for any problems
	 */
	
	public boolean connect(boolean createTables) throws DatabaseConnectionException;

	/**
	 * Will switch to the target database and create it if it isnt there
	 * @param targetDB is the target database we want to switch to
	 * @return true on success
	 */
	public boolean switchCreate(String targetDB);
	
	
	/**
	 * returns a connection handle
	 * @return
	 */
	public Connection getConnection();
	
	
	/**
	 * Attempts to clean shutdown the database
	 * @return true on success
	 * @throws DatabaseConnectionException
	 */
	public boolean shutdown() throws DatabaseConnectionException;

	/**
	 * Return the result of executing the SQL statement
	 * @param query the actual sql statement
	 * @return the result as string
	 * @throws SQLException on any problems
	 */
	public String[][] getSQLData(String query)throws SQLException;
	
	/**
	 * Gets a result from 0 to limit rows
	 * @param Query The sql to get
	 * @param Limit a limit set on the number of results
	 * @return results
	 * @throws SQLException
	 */
	public String[][] getSQLDataLimited(String Query,int Limit) throws SQLException;
	/**
	 * Optional to return a specific column of data so that we can use it instead of having to copy it out of a double array
	 * starts counting from 1
	 * 
	 * @param query
	 * @param Column
	 * @return
	 * @throws SQLException
	 */
	public String[] getSQLDataByColumn(String query, int Column)throws SQLException;
	
	/**
	 * Will return a count of the size of the result set by the current query
	 * @param query
	 * @return the number of rows returned by the query
	 * @throws SQLException
	 */
	public int getCountOnSQLData(String query)throws SQLException;
	
	
	/**
	 * Run an update on the database
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public boolean updateSQLData(String query) throws SQLException;
	
	
	/**
	 * Returns the name of the current Database
	 * @return
	 */
	public String getDatabaseName();
	
	/**
	 * Return some type of ID to know what the underlying db is
	 * @return
	 */
	public String getTypeDB();
	
	/**
	 * Fetch binary sql data
	 * @param query
	 * @return byte array of the results
	 * @throws SQLException
	 */
	public byte[][] getBinarySQLData(String query) throws SQLException;

	/**
	 * Returns a unique hash of the database and contents. <P>
	 * This can be used to save certain stats over the db, and be assured nothing changed.
	 * @return
	 */
	public String getHash();
	
	public PreparedStatement prepareStatementHelper(String p)throws SQLException;	
	
}
