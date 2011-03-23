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


package metdemo.DataBase;

//import java.net.*;
//import java.io.*;
//import java.util.Vector;
//import java.util.StringTokenizer;
import java.sql.SQLException;


/**
 * <code>DBConnect</code> class outlines method for connecting to DB.
 */
public abstract class DBConnect {

    /** Error message. */
    protected String message;

    /** 
     * Returns the error message. 
     * @return  error message
     */
    public String getMsg() {
	return message;
    }

    /**
     * Connects to DW.
     * @return  <code>true</code> if it connects to the DB succesfully
     *          <code>false</code> otherwise
     */
    public abstract boolean connect();

    /**
     * Used for <code>DwClient</code>.  Sleeps for specified time.
     * @param time   the amount of time to sleep.
     */
    protected void sleep(int time) {
	try{Thread.sleep(time);}
	catch(InterruptedException e) {}
    }

    /**
     * Closes connection to DB
     */
    public abstract boolean shutdown(); // shutdown
    
    /**
     * Queries number of rows in <code>table</code>.
     * @param table   table name
     * @return   number of rows in <code>table</code>
     */
    public abstract int getCount(String table);	
    
    /**
     * Queries result for specified SQL statement.
     * @param sqlStmt   SQL statement
     * @return  <code>true</code> if it gets the result successfully
     *          <code>false</code> otherwise
     */
    public abstract boolean getSqlData(String sqlStmt) throws SQLException;

    /**
     * Returns column names.
     * @return  an array of column names
     */
    public abstract String[] getColHeader();

    /** Returns row data.
     * @return  an array of row data
     */
    public abstract String[][] getRowData();

}
    






