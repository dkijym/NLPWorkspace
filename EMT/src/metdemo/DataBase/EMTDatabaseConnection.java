/*
 * Created on Sep 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.DataBase;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Shlomo Hershkop
 * 
 * This defines the requirements of the EMT type database connection<P>
 * 
 * We extend the simple database connection and provide specific EMT extensions<P>
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface EMTDatabaseConnection extends DatabaseConnection {

	
	/** 
	 * For speed, allow the engines to set/unset autocommit, but responsible is on engines inside sync block
	 * @param flag on true commit
	 */
	public void setAutocommit(boolean flag);
	
	/**
	 * use date to get count
	 * @param date
	 * @return
	 */
	public int getCountByDate(String date);
	
	/**
	 * given a table give me a count
	 * @param table
	 * @return
	 */
	public int getCountByTable(String table);
	
	/**
	 * quickly look up the body
	 * @param EmailRef
	 * @return
	 */
	public String[] getBodyByMailref(String EmailRef);
	
	/**
	 * Quick way to get the body and type from mailref
	 * @param EmailRef
	 * @return
	 */
	public String[][] getBodyAndTypeByMailref(String EmailRef);
	
	/**
	 * Cache method to get the subject when given the mailref
	 * @param EmailRef
	 * @return
	 */
	public String getSubjectByMailref(String EmailRef);
	
	/**
	 * Allow us to create a new EMT database
	 * @param name of the database
	 * @return true on success
	 */
	public boolean createNewEMTDatabase(String name);
	
	/**
	 * will be used for speed up in caching scheme
	 * @param p
	 */
	public void doPrepared(PreparedStatement p) throws SQLException;

	
/**
 * 
 * @return a list of available databases
 * @throws DatabaseConnectionException is throw if not opened
 * @throws SQLException throws if something is wrong
 */
	public String[][] getAllDatabases() throws DatabaseConnectionException,SQLException;
	
	}



