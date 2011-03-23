/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.DataBase;


import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Shlomo Hershkop
 * 
 * This package is meant to be used to instantiate an instance of the db
 * 
 * It will know whether to call mysql or java based (derby/cloudscape) etc
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DatabaseManager {

	public static final int MYSQL = 0;
	public static final int DERBY = 1;
	public static final int CMYSQL =2;
	public static final int POSTGRES = 3;
	public static final int MICROSOFT = 4;
	public static final String sMYSQL = "MYSQL";
	public static final String sDERBY = "DERBY";
	public static final String sCMYSQL = "CACHEMYSQL";
	public static final String sPOSTGRES = "POSTGRES";
	public static final String sMICROSOFTSQL = "MSSQL";
	private static int m_id = MYSQL;

	public static EMTDatabaseConnection LoadDB(String host, String dbname, String user, String passwd, String type) {
		//File mysql = new File("c:/mysql");
		//if(mysql.exists()){
		if(type.equals(sMYSQL)){
			m_id = MYSQL;
			return new EMTMysqlConnection(host, dbname, user, passwd);
		}
		else if (type.equals(sDERBY))
		{
			m_id = DERBY;
			return new EMTCloudscapeConnection(host, dbname, user, passwd);
		}
		else if(type.equals(sPOSTGRES)){
			m_id = POSTGRES;
			return new EMTPostgreSQLConnection(host, dbname, user, passwd);
		}else if(type.equals(sMICROSOFTSQL)){
			m_id = CMYSQL;
			return new EMTMicrosoftSQLConnection(host, dbname, user, passwd);
		}
		else if(type.equals(sCMYSQL)){
			m_id = CMYSQL;
			return new EMTMysqlCachedDatabaseConnection(host, dbname, user, passwd);
		}
		//default we have a problem	
		System.out.println("WARNING: unknown DB requested");
		return null;
	}


	public static boolean createNewEMTDatabase(String name, Statement statement)
	{
		return createNewEMTDatabase(name, statement, m_id);
	}
	
	public static boolean createNewEMTDatabase(String name, Statement statement, int type) {
		boolean flag = true;
		m_id = type;
		System.out.println("type is = " +type);
		/*
		 * String query = new String(); String line = null; while ((line =
		 * reader.readLine()) != null) { line = line.trim(); if
		 * (!line.startsWith("--") && line.length() > 1) if (line.endsWith(";")) {
		 * m_jdbc.updateSQLData(query + line); query = ""; } else query += line; }
		 */
		
		//Email Table
		if(m_id==MYSQL || m_id == CMYSQL || m_id == POSTGRES )
		try {
			statement.executeUpdate("DROP TABLE IF EXISTS email");

			statement.executeUpdate("DROP TABLE IF EXISTS message");

			statement.executeUpdate("DROP TABLE IF EXISTS kwords");
		} catch (SQLException se1) {
			System.out.println(se1);
			flag = false;
		}
		else if(m_id == MICROSOFT){
			try {
				statement.executeUpdate("DROP TABLE email");

				statement.executeUpdate("DROP TABLE message");

				statement.executeUpdate("DROP TABLE kwords");
			} catch (SQLException se1) {
				System.out.println(se1);
				flag = false;
			}
		}else{
			try {
				statement.execute("drop table email");
			} catch (SQLException s) {
				if (!s.toString().endsWith("exist") && !s.toString().endsWith("exist."))
					System.out.println(s);
			}
			try {
				statement.execute("drop table message");
			} catch (SQLException s) {
				if (!s.toString().endsWith("exist")&&!s.toString().endsWith("exist."))
					System.out.println(s);
			}
			try {
				statement.execute("drop table kwords");
			} catch (SQLException s) {
				if (!s.toString().endsWith("exist")&&!s.toString().endsWith("exist."))
					System.out.println(s);
			}
			
			
			
			
		}
		String BLOB = new String("");
		String TIMEstamp = new String("");
		String TEXTTYPE = new String("");
		if(m_id== MYSQL || m_id == CMYSQL)
		{
			BLOB = "mediumblob";
			TIMEstamp = "datetime";
			TEXTTYPE = "text";
		}else if (m_id == POSTGRES || m_id == MICROSOFT){
			BLOB = "bytea"; //not sure about this
			TIMEstamp = "timestamp";
			TEXTTYPE = "text";
		}
		else
		{
			BLOB = "blob(8 M) ";//"LONG VARBINARY";//"blob(16M)";//LONG VARBINARY";
			TIMEstamp = "timestamp";
			TEXTTYPE = "LONG VARCHAR";//(65536)";
		}
		System.out.println("attempting creation of email table");
		try {
			if(type==MYSQL || m_id == CMYSQL )
			{
				statement.executeUpdate("CREATE TABLE email (" +
						"UID int unsigned NOT NULL auto_increment,"
						+"sender varchar(127) NOT NULL default '',"
						+ "sname varchar(64) NOT NULL default '',"  
						+ "sdom varchar(64) NOT NULL default '',"
						+ "szn varchar(16) NOT NULL default ''," + 
						"rcpt varchar(127) NOT NULL default '',"
						+ "rname varchar(64) NOT NULL default ''," + "rdom varchar(64) NOT NULL default '',"
						+ "rzn varchar(16) NOT NULL default ''," + "numrcpt smallint NOT NULL default '0',"
						+ "numattach smallint default '0'," + "size int unsigned default '0',"
						+ "mailref varchar(127) NOT NULL ," + "xmailer varchar(100) default '',"
						+ "dates date NOT NULL default '0000-00-00'," + "times time NOT NULL default '00:00:00',"
						+ "flags varchar(255) default NULL," + "msghash varchar(128) NOT NULL default '',"
						+ "rplyto varchar(127) default ''," + "forward varchar(32) default '',"
						+ "type varchar(64) NOT NULL default ''," + "recnt smallint unsigned default '0',"
						+ "subject varchar(255) NOT NULL default ''," + "timeadj bigint default '0',"
						+ "folder varchar(127) default '',utime bigint default '0',"
						+ "senderLoc char(1) default '',rcptLoc char(1) default '',"
						+ "insertTime "+TIMEstamp+" default NULL,recpath varchar(255) default ''," + "class char(1) default '?',"
						+ "score smallint default '0', groups smallint unsigned default '0', ghour smallint, gmonth smallint, gyear smallint,"
						+ "PRIMARY KEY  (msghash,sender,rcpt,dates,times)," + "KEY insertTime (insertTime),"
						+ "KEY utime (utime), KEY UID (UID)," + "KEY msghash (msghash)," + "KEY mailref (mailref),"
						+ "KEY sender (sender), KEY rcpt (rcpt), KEY type (type), KEY folder(folder), KEY SIZE(size), KEY gyear (gyear),  KEY gmonth (gmonth) , KEY ghour (ghour) , KEY dates (dates) )" +
						" ENGINE = MYISAM");
			
				statement.executeUpdate("optimize table email");
			
			}
			else if(type == DERBY){
				
				statement.executeUpdate("CREATE TABLE email (" +
						"UID INT NOT NULL GENERATED ALWAYS AS IDENTITY ," +
						"sender varchar(127) NOT NULL default '',"
						+ "sname varchar(64) NOT NULL default ''," 
						+ "sdom varchar(64) NOT NULL default '',"
						+ "szn varchar(16) NOT NULL default ''," + 
						"rcpt varchar(127) NOT NULL default '',"
						+ "rname varchar(64) NOT NULL default ''," + "rdom varchar(64) NOT NULL default '',"
						+ "rzn varchar(16) NOT NULL default ''," + "numrcpt smallint NOT NULL default 0,"
						+ "numattach smallint default 0," + "size int default 0,"
						+ "mailref varchar(127) NOT NULL ," + "xmailer varchar(100) default '',"
						+ "dates date NOT NULL default '0000-00-00'," + "times time NOT NULL default '00:00:00',"
						+ "flags varchar(255) default NULL," + "msghash varchar(128) NOT NULL default '',"
						+ "rplyto varchar(127) default ''," + "forward varchar(32) default '',"
						+ "type varchar(64) NOT NULL default ''," + "recnt smallint default 0,"
						+ "subject varchar(255) NOT NULL default ''," + "timeadj bigint default 0,"
						+ "folder varchar(127) default '',utime bigint default 0,"
						+ "senderLoc char(1) default '',rcptLoc char(1) default '',"
						+ "insertTime "+TIMEstamp+" default NULL,recpath varchar(255) default '',"
						 + "class char(1) default '?',"
						+ "score smallint default 0, groups smallint default 0, ghour smallint, gmonth smallint, gyear smallint,"
						+ "PRIMARY KEY  (msghash,sender,rcpt,dates,times))");
				//change to btree index
					statement.executeUpdate("create index insertTime on email(insertTime)");
					statement.executeUpdate("create index utime on email(utime)");
					statement.executeUpdate("create index UID on email(UID)");
					statement.executeUpdate("create index msghash on email(msghash)");
					statement.executeUpdate("create index mailref on email(mailref)");
					statement.executeUpdate("create index sender on email(sender)");
					statement.executeUpdate("create index rcpt on email(rcpt)");
					statement.executeUpdate("create index type on email(type)");
                    statement.executeUpdate("create index folder on email(folder)");
                    statement.executeUpdate("create index size on email(size)");
					statement.executeUpdate("create index gyear on email(gyear)");
					statement.executeUpdate("create index gmonth on email(gmonth)");
					statement.executeUpdate("create index ghour on email(ghour)");
					statement.executeUpdate("create index dates on email(dates)");
					
					
			}else if(type == POSTGRES || type == MICROSOFT){
				statement.executeUpdate("CREATE TABLE email (" +
						"UID serial NOT NULL,"
						+"sender varchar(127) NOT NULL default '',"
						+ "sname varchar(64) NOT NULL default '',"  
						+ "sdom varchar(64) NOT NULL default '',"
						+ "szn varchar(16) NOT NULL default ''," + 
						"rcpt varchar(127) NOT NULL default '',"
						+ "rname varchar(64) NOT NULL default ''," + "rdom varchar(64) NOT NULL default '',"
						+ "rzn varchar(16) NOT NULL default ''," + "numrcpt smallint NOT NULL default '0',"
						+ "numattach smallint default '0'," + "size integer default '0',"
						+ "mailref varchar(127) NOT NULL ," + "xmailer varchar(100) default '',"
						+ "dates date NOT NULL," + "times time NOT NULL,"
						+ "flags varchar(255) default NULL," + "msghash varchar(128) NOT NULL default '',"
						+ "rplyto varchar(127) default ''," + "forward varchar(32) default '',"
						+ "type varchar(64) NOT NULL default ''," + "recnt smallint default '0',"
						+ "subject varchar(255) NOT NULL default ''," + "timeadj bigint default '0',"
						+ "folder varchar(127) default '',utime bigint default '0',"
						+ "senderLoc char(1) default '',rcptLoc char(1) default '',"
						+ "insertTime "+TIMEstamp+" default NULL,recpath varchar(255) default ''," + "class char(1) default '?',"
						+ "score smallint default '0', groups smallint default '0', ghour smallint, gmonth smallint, gyear smallint,"
						/*+ "INDEX insertTime (insertTime),"
						+ "INDEX utime (utime), INDEX UID (UID)," + "INDEX msghash (msghash)," + "INDEX mailref (mailref),"
						+ "INDEX sender (sender), INDEX rcpt (rcpt), INDEX type (type), INDEX folder(folder), INDEX SIZE(size), INDEX gyear (gyear),  " +
						"INDEX gmonth (gmonth) , INDEX ghour (ghour) , INDEX dates (dates), " +
						*/
						+"PRIMARY KEY  (msghash,sender,rcpt,dates,times) )");
				
				statement.executeUpdate("create index insertTime on email(insertTime)");
				statement.executeUpdate("create index utime on email(utime)");
				statement.executeUpdate("create index UID on email(UID)");
				statement.executeUpdate("create index msghash on email(msghash)");
				statement.executeUpdate("create index mailref on email(mailref)");
				statement.executeUpdate("create index sender on email(sender)");
				statement.executeUpdate("create index rcpt on email(rcpt)");
				statement.executeUpdate("create index type on email(type)");
                statement.executeUpdate("create index folder on email(folder)");
                statement.executeUpdate("create index size on email(size)");
				statement.executeUpdate("create index gyear on email(gyear)");
				statement.executeUpdate("create index gmonth on email(gmonth)");
				statement.executeUpdate("create index ghour on email(ghour)");
				statement.executeUpdate("create index dates on email(dates)");
			}else{
				System.out.println("WARNING: UNKNOWN DB PASSED>>>>WILL NOT CREATE!!");
				return false;
			}
		} catch (SQLException se2) {
			System.out.println(se2);
			flag = false;
		}
		System.out.println("will try to make message");
		//message table
		try {
			if(type==MYSQL || m_id == CMYSQL ){
			statement.executeUpdate("CREATE TABLE message (mailref varchar(127) NOT NULL default '',"
					+ "type varchar(64) default NULL," + "hash varchar(127) NOT NULL default '',"
					+ "received "+TEXTTYPE+" NOT NULL," + "size int unsigned default '0',"
					/*+ "msghash varchar(127) NOT NULL default ''," */
					+ "body "+BLOB+","
					+ "filename varchar(255) default ''," + "PRIMARY KEY  (mailref,hash) ,"
					+ "KEY hash1 (hash), KEY type1 (type), KEY filename1(filename)   ) ENGINE = MYISAM");

			statement.executeUpdate("optimize table message");
			}		
			else if(type == DERBY)
			{
				statement.executeUpdate("CREATE TABLE message (mailref varchar(127) NOT NULL default '',"
						+ "type varchar(64) default NULL," + "hash varchar(127) NOT NULL default '',"
						+ "received "+TEXTTYPE+" NOT NULL," + "size int default 0,"
						+  "body "+BLOB+","
						+ "filename varchar(255) default ''," + "PRIMARY KEY  (mailref,hash))");
				statement.executeUpdate("create index mailref1 on message(mailref)"); //mysql has built from primary
				statement.executeUpdate("create index hash1 on message(hash)");
				statement.executeUpdate("create index type1 on message(type)");
				statement.executeUpdate("create index filename1 on message(filename)");
			}else if (type == POSTGRES || type == MICROSOFT){
				statement.executeUpdate("CREATE TABLE message (mailref varchar(127) NOT NULL,"
						+ "type varchar(64) default NULL," + "hash varchar(127) NOT NULL,"
						+ "received "+TEXTTYPE+" NOT NULL," + "size integer default '0',"
						+ "body "+BLOB+","
						+ "filename varchar(255) default ''," + 
				/*
						 "INDEX hash1 (hash), INDEX type1 (type), INDEX filename1(filename), " +*/
						 "PRIMARY KEY  (mailref,hash) )");
				statement.executeUpdate("create index mailref1 on message(mailref)"); //mysql has built from primary
				statement.executeUpdate("create index hash1 on message(hash)");
				statement.executeUpdate("create index type1 on message(type)");
				statement.executeUpdate("create index filename1 on message(filename)");
			}else{
				System.out.println("WARNING: UNKNOWN DB PASSED>>>>WILL NOT CREATE!!");
				return false;
			}
            
            //TODO: move the messages themselves to a new table (hash,message) using only hash as key
            
            
            
		} catch (SQLException se3) {
			System.out.println(se3);
			flag = false;
		}
		System.out.println("will try to make kwords");
		//keyword table
		try {
			statement.executeUpdate("CREATE TABLE kwords (" + " mailref varchar(127) NOT NULL default '',"
					+ "hotwords varchar(255) default NULL," + "PRIMARY KEY  (mailref))");
		} catch (SQLException se4) {
			System.out.println(se4);
			flag = false;
		}

		return flag;
	}

}