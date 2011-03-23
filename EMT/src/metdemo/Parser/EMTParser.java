package metdemo.Parser;

/**
 * @author shlomo hershkop shlomo@cs.columbia.edu
 * Short History:
 * This started out as a convertion from out emt-parse.pl perl script (met)
 * this has been adopted and tested and refined until it should pretty much work on anything thrown to it.
 * 
 * main tests: our own email mbox files, assorted mbox from 91, some outlook express file, and the enron data sets
 * when testing main comparisons to outlook express and pine email readers
 * 
 * Note: that our implimentation actually works on some emails that pine can not process correctly (very weird instances)
 */


import java.lang.Process;
import java.lang.Runtime;
import java.lang.InterruptedException;
import java.lang.String;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Enumeration;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.mail.*;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;

import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.BusyWindow;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;



/**
 * Columbia Univerty - Email Mining Toolkit's (EMT) parser for converting mbox
 * emails to EMT database records
 * <P>
 * 
 * The basic idea: run through a file or set of files (depends on flags) <BR>
 * and parse out the contents as mbox files emails.
 * <P>
 * 
 * 
 * @author Shlomo Hershkop 2001 - 2006
 */




public class EMTParser {
	
	/* needed for gui display */
	public static final String GENERALFROM = "^From ([^,])*:[0-9]+:(.*)$";
	private static final String SEPERATORDIR = "/";//File.separatorChar
	private static final int MAX_BODY = 8360000;//little less than 8meg
	private static final int DB_DUP_KEY = 1022;
	private static final int ER_DUP_ENTRY = 1062;
	private static final int DERBY_ERROR = 20000;
    private static final int DERBY_ERROR2 = 30000;
   
    private static final int BUFREADBUFFERNUMBER =  65536;
    private static final int bytebuf = 8096;
   
    //be aware that testing shows global variables are faster in this program
	//byte[] md5rslt;
	/** Global Variables */
	private String s_frompattern;
	private boolean b_hashContents = true;
	/** to handle mbox format with each file is its own */
	private boolean b_single = false;
	private boolean toread = true;
	//private boolean b_recurse_single = false;
	/** if we want to flush the db conetents */
	private boolean b_flush_db = false;
	private boolean b_chompname = false;
	private boolean b_alignDates = false;
	/** #for logging */
	private boolean b_tolog = false;
	/** for db connection - data base handle */
//	private EMTDatabaseConnection dbh;
	/** for log file if neccessary */
	private PrintWriter m_outlog;
    //save time later
	
	/** for date handling */
	/* this is for really messed up dates in spam messages, so just use last messages date*/
	private long last_good_utime;
	/** global variable */
	//private long timediff =0; //differnece between the Date: header and mesage wrapper
//	private boolean do_delete = false;

	//Patterns used in the class
	private PreparedStatement ps_emailinsert;
	private PreparedStatement ps_message;

	/**
	 * Speed up to have precompiled expressions which we will be reusing later
	 */
	private Pattern p_topfrom = Pattern.compile(GENERALFROM, Pattern.CASE_INSENSITIVE);
	//these Patterns are for parts of the body
	final Pattern body_div = Pattern.compile("^\\s+([^\\s].*)*$");
	//    final Pattern body_div = Pattern.compile("^\\s+([^\\s].*)*$");
	final Pattern body_end = Pattern.compile("^\\s*$");//added extra 010
	final Pattern p_return_path = Pattern.compile("^Return-Path: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern rec_line = Pattern.compile("^Received: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_newsgrp = Pattern.compile("^Newsgroups: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_date = Pattern.compile("^Date: (.*):(.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_content = Pattern.compile("^Content-Type:(.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_midmsgid = Pattern.compile(".*Message-ID: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_midstat = Pattern.compile("^.*Status: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_subject = Pattern.compile("^Subject:(.*)", Pattern.CASE_INSENSITIVE);
	//final Pattern p_to = Pattern.compile("^To: (.*)"// ,Pattern.CASE_INSENSITIVE);
	//final Pattern p_cc = Pattern.compile("^Cc: (.*)"	// ,Pattern.CASE_INSENSITIVE);
	final Pattern p_rcpt = Pattern.compile("^(To|B?cc):(.*)", Pattern.CASE_INSENSITIVE);
	//final Pattern p_bcc = Pattern.compile("^Bcc: (.*)" 	// ,Pattern.CASE_INSENSITIVE);
	final Pattern p_delivered = Pattern.compile("Delivered-To: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_fromline = Pattern.compile("^From:(.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_status = Pattern.compile("^Status: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_msgid = Pattern.compile("^Message-ID: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_mimever = Pattern.compile("^MIME-Version: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_delto = Pattern.compile("^Delivered-To: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_inrply = Pattern.compile("^In-Reply-To: (.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_xmailer = Pattern.compile("^X-Mail.*: (.*)", Pattern.CASE_INSENSITIVE);
	//final Pattern p_xstuff = Pattern.compile("^X-.*$", Pattern.CASE_INSENSITIVE);
	
	final Pattern p_misc = Pattern.compile("^[\\w|-|\\t]+:?.*");// ,Pattern.CASE_INSENSITIVE);
	//final Pattern p_boundry = Pattern.compile(".*boundary=\\\"([^\\\"]*)\\\".*");
	final Pattern p_address1 = Pattern.compile("^.*?([^\"'\\s<]*)@([^>\\s'\"\\);]*).*$");//"^.*?([^\\s<]*)@([^\\s>]*).*$");//Pattern.compile("^.*?([^\\s<]*)@([^\\s>]*).*$");//("^.*[<]?(\\S+)\\@(\\S+)[>]?.*$");//"^\\s*([^\\s]*)\\@([^\\s]*)\\s*$"//  ,Pattern.CASE_INSENSITIVE);
	//final Pattern p_emailname= Pattern.compile("^.*?([^\\s<\\'\\(]*)@([^\\s'\"\\)>;]*).*$");//".*[<|](\\S*@\\S*)[>|].*");

	//final Pattern p_isprint = Pattern.compile("^([[:print:]].*)?$"); //is it
																	 // printable
																	 // characters....from
																	 // perl,
																	 // hope it
																	 // works
																	 // here.
																	 // else
																	 // will
																	 // need to
																	 // do
																	 // something
																	 // else
	final Pattern number = Pattern.compile("^\\d+$");
	final Pattern p_recount = Pattern.compile("Re:", Pattern.CASE_INSENSITIVE);
	final Pattern p_fwdcount = Pattern.compile("fwd*:", Pattern.CASE_INSENSITIVE);
	final Pattern p_quotes = Pattern.compile("[\\\"']");
	final Pattern p_x400emailname = Pattern.compile("^.*/CN=(.*)>.*$");
	final Pattern sqldate = Pattern.compile("^(.*) .* at (.*) .*$");
	final Pattern p_cleanfile = Pattern.compile("^[0-9]{2}\\.[0-9]{2}\\/.*");
	final Pattern p_dots = Pattern.compile("(.*)\\.(.*)\\/");
	final Pattern p_base64 = Pattern.compile("Encoding: base64", Pattern.CASE_INSENSITIVE);//|Pattern.MULTILINE
																						   // );
	
	
	final Pattern p_zeroFifteen = Pattern.compile("\\015"); //replaceAll("\\015", "");
	final Pattern p_zeroTwelve = Pattern.compile("\\012"); //replaceAll("\\012", "");
	final Pattern p_singlecomma = Pattern.compile(",");
	
	
	private boolean b_mbox_only = false;
	private boolean b_outlookExpress_readdbx = false;
	private boolean b_outlookExpress_readoe = false;
	private boolean b_outlook_readpst = false;
	private boolean b_im_only = false;
	//TODO: add mac support
	private boolean isWindowsOS = true;
	private boolean useDBX = false;
	
	
	// Get a Properties object
	//private Properties props = System.getProperties();
	// Get a Session object
	//private Session session = Session.getDefaultInstance(props, null);
	//  private PreparedStatement pstmt;

	
	/**
	 * Main constuctor which takes arguements to use and a busy window to update on the screen
	 */
	public EMTParser(String args[], EMTDatabaseConnection dbh1, BusyWindow BW2) {
		
		//parse out some stuff for us
		HashMap<String,String> bunchOvals = initialize(args);
		//lets get them out
		String s_filein = bunchOvals.get("s_filein");
		String s_dbase = bunchOvals.get("s_dbase");
		String s_dbmachine = bunchOvals.get("s_dbmachine");
		String s_dbuser = bunchOvals.get("s_dbuser");
		String s_dbpass = bunchOvals.get("s_dbpass");
		String s_typeDB = bunchOvals.get("s_typeDB");
		
		//hasndle to talk to db
		EMTDatabaseConnection dbh = null;

		
		//setup some of the global variables
		HashMap<String, Boolean> m_parsed_files = new HashMap<String, Boolean>();

		//lets see if we are in windows
		String osName = System.getProperty("os.name");
		
		isWindowsOS = osName.indexOf("Win") != -1 ? true : false;
		
		System.out.println("Windows check: "+isWindowsOS);
		
		//2 ways
		//if dbh1 is null need to make connection
		if (dbh1 == null) {

			//if empty database full stop
			if (s_dbase.length() < 1) {
				showUsage();
				System.exit(-2);
			}
			
			try {
				//try to connect to the database
				dbh = null;

				dbh = DatabaseManager.LoadDB(s_dbmachine, s_dbase, s_dbuser, s_dbpass, s_typeDB);
				if (dbh.connect(true) == false) {
					throw (new Exception());
				}
			} catch (Exception e) {
				System.err.println("Couldn't connect to database");
				System.exit(-3);
			}
		} else {
			dbh = dbh1;
		}

		//ok we have a legal connection.!!
		try {
			//check if have to flush old data out
			if (b_flush_db) {
				//try to get faster deletes
				//dbh.setAutocommit(false);
			//	dbSQL.flushDatabase(dbh,false);
				//safer to rebuild db
				dbh1.createNewEMTDatabase(dbh1.getDatabaseName());
			
				//	dbh.setAutocommit(true);
				System.out.println("Finished flushing db");
			}
			
			//check if have a file limitation problem
			if(dbh.getTypeDB().equals(DatabaseManager.sMYSQL)){
				
				try{
					
					String data[][] = dbh.getSQLData("show variables like 'max_allowed_packe%'");
					if(data!=null){
						System.out.println("need to extend the mysql limit on packet size");
						//check if the limit is less than 8 meg
						int limit = Integer.parseInt(data[0][1]);
						if(limit < 12777216 )
						{
							//??dbh.setAutocommit(true);
							dbh.updateSQLData("set max_allowed_packet=12000000");
						}
						System.out.println("EMT Done packet size extension");
						
						data = dbh.getSQLData("show variables like 'max_allowed_packe%'");
						if(data!=null){
							System.out.println("Test of max setting:\n"+ data[0][0] + " "+ data[0][1]);
						}else{
							System.out.println("problem setting max packet...time to panic");
						}
					}
					
					
				}catch(SQLException se){
					System.out.println("Problem setting the max allowed section");
					se.printStackTrace();
				}
				
			}
			
			
			/**
			 * Check if we need to and setup log system
			 */
			if (b_tolog) {
				m_outlog = new PrintWriter(new BufferedWriter(new FileWriter("EMT_logfile")));

			} else
				m_outlog = null; //so can test this later.

			//setup the preparedstatements
			try {
				ps_emailinsert = dbh
						.prepareStatementHelper("INSERT INTO email (size,msghash,sender,sname,sdom,szn,dates,times,timeadj,subject,recnt,forward,type,flags,mailref,folder,numrcpt,numattach,rcpt,rname,rdom,rzn,insertTime,xmailer,utime,gyear,gmonth,ghour)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				ps_emailinsert.setEscapeProcessing(false);

				ps_message = dbh
						.prepareStatementHelper("INSERT INTO message (filename,mailref,hash,received,body,size,type) VALUES(?,?,?,?,?,?,?)");
				ps_message.setEscapeProcessing(false);

			} catch (SQLException s) {
				System.out.println(s);
				System.exit(1);
			}
			//if the file given was directory, then we should be able to switch between file types
		
				File f = new File(s_filein);
				if(f.isDirectory()){
					
				}
				else{
					b_single = true;
				}
				//now to actually parse the file
			//will recursivly crawl to parse see the method.
//				turning off autocommit will speed things up.
				//??dbh.setAutocommit(false);
				Dimension d = BW2.getSize();
				BW2.setSize(new Dimension(3*d.width,d.height));
				BW2.validate();
				BW2.repaint();
			long startingtime = System.currentTimeMillis();
            
			parseEmails(dbh,s_filein,BW2,s_typeDB,m_parsed_files);
            
			System.out.println("total processing time:" + ((System.currentTimeMillis() - startingtime) / 1000.0) +" sec" );    
			//clean up time
			if (m_outlog != null)
				m_outlog.close();


		} catch (PatternSyntaxException reg) {

			System.out.println("d:" + reg.getDescription());
			System.out.println("index: " + reg.getIndex());
			System.out.println("Mes: " + reg.getMessage());
		} catch (Exception e) {
			System.out.println(" caught: " + e);

		}/*finally{
//			turning off autocommit will speed things up.
			//??if(dbh!=null)
			//??dbh.setAutocommit(true);
			
		}*/
		
		try {
			ps_emailinsert.close();
			ps_message.close();
		} catch (SQLException s2) {
			System.out.println(s2);
		}

	} //end class constructor




	/** initilizes the golobal variable setting based on arguments passed */

	
	private final HashMap<String, String> initialize(String args[]) {
		HashMap<String, String> returnVals = new HashMap<String, String>();
		String s_dbase = null;
		String s_dbmachine=null,s_dbuser=null, s_dbpass=null,s_typeDB=null;
		String s_filein = new String();
		String temp = new String();
		//will count till args.length
		int max = args.length;
		for (int c = 0; c < max; c++) {
			temp = args[c].trim();
			//choose fileout
			if (temp.equals("-o")) {
				//s_fileout = args[++c].replaceAll("\\\\","\\/");

			}
			//choose input file
			else if (temp.equals("-i")) {
				//anbd replace all back slashes with forward slashes
				s_filein = args[++c].replaceAll("\\\\", "\\/");
				//need to clean up slashes :)
			}
			//get db info
			else if (temp.equals("-db")) {
				if (c + 3 > max) {
					System.out.println("Missing args for database");
					showUsage();
					System.exit(-4);
				}
				s_dbase = args[++c];
				s_dbmachine = args[++c];
				s_dbuser = args[++c];
				s_dbpass = args[++c];
			} else if (temp.equals("-dbnp")) {
				if (c + 2 > max) {
					System.out.println("Missing args for database");
					showUsage();
					System.exit(-5);
				}
				s_dbase = args[++c];
				s_dbmachine = args[++c];
				s_dbuser = args[++c];

			}
			//no hashing
			else if (temp.equals("-n")) //$t =~ /^-n/)
			{
				b_hashContents = false;
			} else if (temp.equals("-typedb")) //$t =~ /^-n/)
			{
				if (args[++c].equals(DatabaseManager.sMYSQL)) {
					s_typeDB = DatabaseManager.sMYSQL;
				} else {
					s_typeDB = DatabaseManager.sDERBY;
				}
				System.out.println("DB type set to : " + s_typeDB);

			} else if (temp.equals("-count")) {
				System.out.println("Actually count flag does nothing");
				//b_onlycount = true;
			} else if (temp.equals("-from")) {
				s_frompattern = new String(args[++c]);

				while (c < max - 1) {
					if (args[c + 1].startsWith("-"))
					{	break;}
					//else
						s_frompattern = s_frompattern.concat(" " + args[++c]);
				}

				boolean worked = true;
				s_frompattern = s_frompattern.trim();
				System.out.println("from pattern: '" + s_frompattern + "'");
				try {
					p_topfrom = Pattern.compile(s_frompattern, Pattern.CASE_INSENSITIVE);
				} catch (Exception fromex) {
					worked = false;
				}

				//check if worked
				if (worked) {
					System.out.println("Changed pattern matcher for from line");
				} else {
					System.out.println("pattern didnt take going to default");
					p_topfrom = Pattern.compile(GENERALFROM, Pattern.CASE_INSENSITIVE);
				}
			} else if (temp.equals("-mm")) {
				boolean worked = true;
				try {
					p_topfrom = Pattern.compile("^[ ]?[0-9]+-[a-zA-Z]+-[0-9]+ (.*):(.*);.*$", Pattern.CASE_INSENSITIVE);
				} catch (Exception fromex) {
					worked = false;
				}

				//check if worked
				if (worked) {
					System.out.println("Changed pattern matcher for from line to mm");
				} else {
					System.out.println("pattern didnt take going to default");
					p_topfrom = Pattern.compile(GENERALFROM, Pattern.CASE_INSENSITIVE);
				}
			} else if (temp.equals("-flush"))//$t =~ /^-flush/)
			{
				b_flush_db = true;
			}else if(temp.equals("-chompname")){
				b_chompname = true;
			}else if(temp.equals("-aligndates")){
				b_alignDates = true;
			}
			else if (temp.equals("-log"))//$t =~ /^-log/)
			{
				b_tolog = true;
			} else if (temp.equals("-w"))//$t =~ /^-w/)
			{
				++c;
				//s_wordlist = args[++c].trim();

			} else if (temp.equals("-a"))//$t =~ /^-a/)
			{
				//	b_doaddress=true;
			} else if (temp.equals("-s"))//$t =~ /^-s/)
			{
				b_single = true;
			} else if (temp.equals("-rs"))//$t =~ /^-rs/)
			{
				//b_recurse_single = true;
				b_single = true;
			} else if (temp.equals("-forcembox")) {
				System.out.println("forcing mbox");
				b_mbox_only = true;
			} else if (temp.equals("-forceoutlook")) {
				System.out.println("forcing outlook");
				b_outlookExpress_readdbx = true;
			} else if (temp.equals("-forcepst")) {
				b_outlook_readpst = true;
			} else if (temp.equals("-forceoutlook2")) {
				System.out.println("forcing readoe");
				b_outlookExpress_readoe = true;
			} else if (temp.equals("-forceim")) {
				b_im_only = true;
			}

			else if (temp.equals("-folder"))//$t =~ /^-folder/)
			{
				//b_folderhash=true;
			} else if (temp.equals("")) {
			} else {
				showUsage();
				System.exit(-1);
			}


		}//end for loop

		System.out.println("Every 50 email will show - ...ignore all other output");
		//return s_filein;
		//lets put it in the hashmap
		
		/*String s_dbase,s_dbmachine,s_dbuser, s_dbpass,s_typeDB;
		String s_filein = new String();
		*/
		returnVals.put("s_filein", s_filein);

		returnVals.put("s_dbase", s_dbase);

		returnVals.put("s_dbmachine", s_dbmachine);

		returnVals.put("s_dbuser", s_dbuser);

		returnVals.put("s_dbpass", s_dbpass);

		returnVals.put("s_typeDB", s_typeDB);
		return returnVals;
	}//end initilize


	/** prints to std out the usage string of argument flags */
	private final void showUsage() {

		System.out.print("-h this Help:\n");
		System.out.print("Default Usage: java EMTParse [options]\n");
		//    System.out.print( "Default input for linux systems: mail/* and
		// mailbox(mbox or /var/spool/mail/name\n");
		//System.out.print( "Default output to CleansedMail.txt\n");
		System.out.print("-i filename \tto specify input file/directory\n");
		//System.out.print( "-o filename \tto change outputfile\n");
		System.out.print("-n          \tto run without hashing output:\n");
		System.out.print("-a          \tto add your address book (unix only)\n");
		System.out.print("-db dbase dbmachine dbuser dbpassword  :to access the db\n");
		System.out.print("-dbnp dbase dbmachine dbuser  :to access the db with blank password\n");
		System.out.print("-flush   \tto empty db contents before insertion\n");
		System.out.print("-chompname   \tshorten file name of the email source if numbers only\n");
		System.out.println("-aligndates   \tto believe the rcvd line dates more than the header dates\n");
		System.out.print("-log     \tto create plogfile and log stuff for debugging\n");
		System.out.print("-s       \tfor mail files in a single file per message\n");
		System.out.print("-rs      \tfor directory containing messages each in own file\n");
		System.out
				.print("-from 'java pattern expression' \tReplace std seperator between messages with another use single quotes.\n");
		System.out.print("-count \tTo only count and not actually insert emails\n");
		System.out.print("-typedb DERBY or MYSQL    this will be the type of db\n");
		System.out.print("-forcembox \t will treat data as mbox\n");
		System.out.print("-forceoutlook \t will treat data as outlook\n");
		System.out.print("-forceim \t will treat data as instant messager data\n");

	}//end usage


 
	/**
	 * Method to recursivly call the parser to parse emails will ignore file
	 * beginning with . , and .zip and .tar and .gz files.
	 *
	 * @param filein is the data to process in a file
	 * @param BW busy window to use to show progress
	 */
	private final void parseEmails(EMTDatabaseConnection dbh,String filein, BusyWindow BW, String s_typeDB,HashMap<String, Boolean> m_parsed_files) {

		if (!legalFile(filein))
			return;
		int totalcount =0;
		//int total=0;
		long before, after;
		File fopen = new File(filein);
		//s_filein is current file, will need to process it and then recursivly
		// run down if direcoty.
		if (fopen.isDirectory()) {

			String fileslist[] = fopen.list();

			for (int i = 0; i < fileslist.length; i++)
				if (legalFile(fileslist[i])) {

					parseEmails(dbh,filein + SEPERATORDIR + fileslist[i],BW,s_typeDB,m_parsed_files);

				}
		} else {
			//allows us to batch the work for each file and then integrate it
			// into the system.
			
			try{
				
			//grab the time
			before = System.currentTimeMillis();
			//if we have a window handle, lets show something
			if(BW != null){
				BW.setMSG("Working on "+filein);
			}
			//grab the count		
			int cn = parseEmails(dbh,filein.trim(),BW,false,s_typeDB,m_parsed_files);
			//update total count
			totalcount += cn;
			//grab the time after we are done
			after = System.currentTimeMillis();
			//lets not show millions of things when we are processing single file messages
			if(cn>2)
				System.out.println("processed in: " + ((after - before) / 1000.0) + " sec");
			}catch(Exception ee){
				ee.printStackTrace();
			}
			
		}


	}//end parsemails



	/**
	 *  Method to parse every email
	 * @param s_filein file to read
	 * @param BW busyWindow to update
	 * @param testforDir if we need to check for directory recursivly
	 * @return number of emails seen
	 */

	private final int parseEmails(EMTDatabaseConnection dbh, String s_filein,BusyWindow BW,boolean testforDir,String s_typeDB, HashMap<String, Boolean> m_parsed_files) {
		Properties props = System.getProperties();
		// Get a Session object
		Session session = Session.getDefaultInstance(props, null);
		
		boolean do_delete = false;
		//check if we have already parsed this file
		//ok since we are looking at full path
		if (m_parsed_files.containsKey(s_filein)) {
			return 0;
		}
		//lets assume we can process it
		m_parsed_files.put(s_filein, true);
		
		//check if a directory only if needed, save us a test if we've done it already
		if(testforDir)
		{
			if (isDirectory(s_filein)) {
				//this is in the case a directory gets passed to us, we can mark as seen without trying to reread it
				return 0;
			}	
		}
		
		/**
		 * we need multiple matchers for some of the patterns to find everything we need
		 */
		Matcher matcherGuy1, matcherPerson2, matcherGuy3;
		
		int num = 0;
		int count = 0;
		String realdate = new String();
		
		//if we are directory traveling and file ends with .dbx and .pst we need to reset stuff
		if(!b_single){
			if(s_filein.toLowerCase().endsWith(".dbx")){
				useDBX = true;
			}			
		}

		//variables need to operate

		//if not mbox to something else;
		//if we know we are only dealing with mbox lets skip the test

		if (b_mbox_only) {
			//nothing to do
		} else if (b_im_only) {
			System.out.println("need to deal with im data here");
			System.out.println("Reminder to self!");
		}
		//here either we know its outlook or we need to test (since not set)
		else if (useDBX || b_outlook_readpst || b_outlookExpress_readoe|| b_outlookExpress_readdbx || !is_mbox(s_filein)) 
		{
			System.out.println("Will try to convert to mbox format.\n" +
					"Creating a temp file, attemp to delete it afterwards.");
			//do readmbx here and assign to mailfile
			do_delete = true;
			//need to execute the convertion:
			String cmds[] = new String[6];
			
			if(b_outlookExpress_readdbx || useDBX){
			
			if(isWindowsOS){
				cmds[0] = "readdbx";
				}
				else{
					cmds[0] = "./readdbx";
				}
			
			cmds[1] = "-q";//no extra info
			cmds[2] = "-f";//-f"; //filein
			cmds[3] = s_filein;
			cmds[4] = "-o";//out file
			cmds[5] = s_filein + ".temp";
			
			useDBX = false; //for single file in directory crawl
			}
			else if (b_outlookExpress_readoe){
				if(isWindowsOS){
				cmds[0] = "readoe";
				}
				else
				{
					cmds[0] = "./readoe";
				}
				cmds[1] = "-q";//no extra info
				cmds[2] = "-i";//-f"; //filein
				cmds[3] = s_filein;
				cmds[4] = "-o";//out file
				cmds[5] = s_filein + ".temp";
			}
			else if(b_outlook_readpst){
				//need to first create temp directory
				File tempd = new File(s_filein+".temp");
				
				tempd.mkdirs();
				tempd.deleteOnExit();
				if(isWindowsOS){
					cmds[0] = "readpst";
				}
				else{
					cmds[0] = "./readpst";
				}
				cmds[1] = "-r";//recursive
				cmds[2] = "-o";//output
				cmds[3] = s_filein+".temp";
				cmds[4] = "-w";//overwrite
				cmds[5] = s_filein;
			}
			
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(cmds);
				//int exitVal = 
					
				//next line does something....be carefull about commenting out
				System.out.println("Processing exitValue: " + proc.waitFor());

				s_filein = s_filein + ".temp";
				
				if(b_outlook_readpst){
					//because we generate a directory of mbox formated stuff
					b_outlook_readpst = false;
					b_mbox_only = true;
					parseEmails(dbh,s_filein,BW,s_typeDB,m_parsed_files);
					//TODO: clean up
					
					return 0;
				}
			} catch (IOException eeee) {
				System.out.println("Make sure that readdbx.exe is present\n" + eeee);
				return 0;
			} catch (InterruptedException e23) {
				System.out.println("e23;" + e23);
				return 0;
			}
		}
		
		
		String s_subject = new String();
		String datel = new String();
		//String return_path = new String();
		String s_xmail = new String();
		Hashtable<String,String> cclist = new Hashtable<String,String>();
		Hashtable<String,String> dellist = new Hashtable<String,String>();
		String s_type = new String();
		String s_mid = new String();
		String s_fdom = new String();
		String s_fzn = new String();
		String s_fname = new String();
		String s_femail = new String();
		String rec_path = new String();
		String line = new String();
		String line2 = new String();
		String temp = new String();
		String temp2 = new String();
		String CURRENTDOMAIN = new String("unknowndomain.com");
		//String s_forward =new String();
		String s_stat = new String();
		String fromline = new String();
		int recount =0, fcount=0;
		//end regexp stuff
		//open handle to the mail file
		try {
			//deafult buffer size is fine, since too big actually slowed it
			// down in inforal tests
			BufferedReader in = new BufferedReader(new FileReader(s_filein),BUFREADBUFFERNUMBER);
			StringBuffer header2 = new StringBuffer(BUFREADBUFFERNUMBER); //hold email header
			String filename = new String(s_filein); //filename will hold the cleaned up filename for later
			//System.out.println("do now: "+filename);
			//will clean up enron data here
			if(b_chompname){
				
				//need to chech only end of name not all path
				int locate = filename.lastIndexOf(SEPERATORDIR);//File.separatorChar);
				String check = new String();
				if(locate>0){
					
					check = filename.substring(locate+1);
					
				}else{
					check = filename;
				}
				//test if it is a number
				if(number.matcher(check).matches()){
					if(locate<2){
						filename = ""+SEPERATORDIR;//File.separatorChar;
					}else{
						filename = filename.substring(0,locate);
					}
				}
			}//end enron cleanup
			
			//bring down the name if its too long
			if (filename.length() > 120) {
				filename = ".." + filename.substring(filename.length() - 120);
			}
			//Finally we are reading in the first line
			line = in.readLine();
			
			if (line != null) {
				line2 = new String(line);
				line = p_zeroFifteen.matcher(line).replaceAll("");//line.replaceAll("\\015", ""); //    tr/\015//d;
				line = p_zeroTwelve.matcher(line).replaceAll("");//line.replaceAll("\\012", "");
			}
			
			while (line != null) {
				//if($_ =~ /^From (.*):(.*):(.*)/ || $once eq "true")
				//are we at the beginning of a new message
				matcherGuy1 = p_topfrom.matcher(line);
				if (count==0 || matcherGuy1.matches()) {
					//System.out.println("matched begin new email:" + line);

					//we print out old info since we are beginning nect email
					/*******
					 * ****************************************************
					 * // DO INSERT STUFF HERE CAUSE WE ARE GOING TO START
					 * ANOTHER MESSAGE
					 **********************************************************/
					if (count > 0 || (count == 1 && !line.matches(".*MAILER-DAEMON.*"))) {

						importIntoDB(dbh,filename,count,header2,s_subject,datel,s_xmail,cclist,dellist, s_type,s_mid,s_femail,s_fname,s_fdom,s_fzn,rec_path,s_stat,fromline,recount,fcount,CURRENTDOMAIN,s_typeDB,session);
						
					}
					//if( ($fname) !~ /MAILER\-DAEMON/i)
					//output here.
					count++;
					if(matcherGuy1.matches())
					if (matcherGuy1.groupCount() > 1) {
						realdate = matcherGuy1.group(1).trim() + ":" + matcherGuy1.group(2).trim();
					}
					//need to clean up the date here

					fromline = new String(realdate);//incase we need to see the
													// fromemail here
					realdate = realdate.replaceFirst("^.*@", "@");//repalce all
																  // till at
					realdate = realdate.replaceAll("^[\\S\\@]+ ", "");//clean
																	  // out
																	  // emails
					realdate = realdate.replaceAll("\\s+", " ");//clean out
																// double space
					realdate = realdate.replaceFirst("^\\s-\\s", "");//clean
																	 // out
																	 // slash
																	 // when no
																	 // email

					
					//new email start

					/**
					 * clean up variable here since need reset
					 */

					cclist.clear();
					dellist.clear();
					rec_path = "";//new String();
					header2 = new StringBuffer(4096);
					header2.append(line2 + "\n");
					datel = "";//new String();
					s_xmail = "";//new String();
					s_mid = "";//new String();
					s_stat = "";//new String();
					s_fdom = "";//new String();
					s_fzn = "";//new String();
					s_fname = "";//new String();
					s_femail = "";//new String();
					fromline = "";//new String();
					temp = "";//new String();
					
					s_subject = "";//new String();
					s_type = "";//new String();
					
					//lets start reading the emails....
					//
					//REMOVED DUE TO NO CLIENT FROM MESSAGE HERE ie header ofemail starts on first line
					//line = in.readLine();
					header2.append(line + "\n");
					line = p_zeroFifteen.matcher(line).replaceAll("");//line.replaceAll("\\015", ""); 
					line = p_zeroTwelve.matcher(line).replaceAll("");//line.replaceAll("\\012", "");//assign current to
														// temp
					

					while (true) {
						//idea is to consolidate lines whcih begin with XXX:
						// something
						// and wrap to next line space space seomthing2
						//save to temp space

						temp = new String(line);
						while (true) {
							// # Begin slurping body here.
							line = in.readLine();
							//2006
							if(line == null)
								break;
							//append to the header save part
							header2.append(line + "\n");
							line = p_zeroFifteen.matcher(line).replaceAll("");//line.replaceAll("\\015", ""); //clean out cR
							line = p_zeroTwelve.matcher(line).replaceAll("");//line.replaceAll("\\012", ""); //clean out lf

							matcherGuy1 = body_div.matcher(line); //check if should end
							if (!matcherGuy1.matches()) {
								//msg = line;
								//msg = line2; //? +\n //might be beginning of
								// the body
								break;
							}
							temp = temp.concat(line);
							
						}//end inner while(true)
						//2006
						if(line == null)
							break;
						
						matcherPerson2 = p_topfrom.matcher(line);
						if (matcherPerson2.matches()) {//???2004
							//    if(start == 0 )
							//start = header2.length() - line.length();
							break;
						}
						
						//2007 to make it more efficient, want to be able to look at the first character and then 
						//case switch it quickly....
						
						char firstHeaderCharacter;
						if(temp.length()>1)
							firstHeaderCharacter = temp.substring(0,1).toLowerCase().charAt(0);
						else
							firstHeaderCharacter = ' ';
						boolean docontinue = false;
						
						//have m f c d s
						switch(firstHeaderCharacter){
							case 'r':
//								recieved line, and it can span a few
								matcherGuy1 = rec_line.matcher(temp);
								if (matcherGuy1.matches()) {
									rec_path += matcherGuy1.group(1) + "\t";
									docontinue = true;//continue;
								}
								break;
							case 'd':
								//date tag
								matcherGuy1 = p_date.matcher(temp);
								if (matcherGuy1.matches()) {
									//				    	System.out.println("in date processing");
									//!!header +=temp+"\n";
									//reconstruct everything after date: 
									datel = matcherGuy1.group(1).trim() + ":" + matcherGuy1.group(2).trim();
									//logic: if no comma
									if (datel.indexOf(',') < 0) {
										//datel = datel.trim();
										//?num = datel.indexOf(' ');
										//?if (num > 0)
										//?	datel = datel.substring(0, num) + "," + datel.substring(num);//want
																										 // space
																										 // after
																										 // comma
									}else{
										datel = p_singlecomma.matcher(datel).replaceAll("");//datel.replaceAll(",","");
									}
//									System.out.println("dateline:"+datel);
									docontinue = true;//continue;
								}
								break;//for d
							case 'c':
//								content type
								matcherGuy1 = p_content.matcher(temp);
								if (matcherGuy1.matches()) {
									s_type = matcherGuy1.group(1).trim();
									matcherPerson2 = p_midstat.matcher(temp);
									if (matcherPerson2.matches()) {
										s_stat = matcherPerson2.group(1);
										//docontinue=true;//continue;
									}
									matcherPerson2 = p_midmsgid.matcher(temp);
									if (matcherPerson2.matches()) {
										s_mid = matcherPerson2.group(1);
										//docontinue = true;//continue;
									}
									/*matcherPerson2 = p_boundry.matcher(temp);
									if (matcherPerson2.matches()) {

										//s_boundry = n.group(1);
										//    System.out.println("boundry:" +s_boundry);

									}*/
									docontinue = true;//continue;
								}

								
								break;//for c
							case 's':
//								subject line
								matcherGuy1 = p_subject.matcher(temp);
								if (matcherGuy1.matches()) {
									s_subject = matcherGuy1.group(1); // save the subject line
									//want to count re's
									recount = 0;
									matcherPerson2 = p_recount.matcher(s_subject); //look for re's
									if (matcherPerson2.find()) {
										recount++;
										while (matcherPerson2.find())
											recount++;
										s_subject = matcherPerson2.replaceAll("");//will replace all
																	 // re's so clean
																	 // subjet

									}
									fcount = 0;
									matcherGuy3 = p_fwdcount.matcher(s_subject); //look for re's
									if (matcherGuy3.find()) {
										fcount++;
										while (matcherGuy3.find())
											fcount++;

										s_subject = matcherGuy3.replaceAll("");//will replace all
																	 // re's so clean
																	 // subjet
										// System.out.println(fcount + " " +s_subject);
									}
									s_subject = s_subject.trim();
									if (b_tolog) {
										//System.out.println("LOGGING1: " + temp);
										m_outlog.println("LOGGING: " + s_subject);
									}

									docontinue = true;//continue;
								}
								break;//for s
							case 'f':
								//from: line
								matcherGuy1 = p_fromline.matcher(temp);
								if (matcherGuy1.matches()) {
								
									
									if (matcherGuy1.groupCount() > 0)
									{
										
										
										temp2 = matcherGuy1.group(1).trim();//stuff after the
									}
									//new logic: 2007
									matcherGuy3 = p_address1.matcher(temp2.replaceAll(".*<", ""));//p_emailname.matcher(temp2);
									if(matcherGuy3.matches()){
										//System.out.println("cc:"+matcherGuy3.group(1));
										temp2=matcherGuy3.group(1)+"@"+matcherGuy3.group(2);
									}
									else {
										matcherGuy3 = p_x400emailname.matcher(temp2);
										if(matcherGuy3.matches()){
											temp2=matcherGuy3.group(1)+"@"+CURRENTDOMAIN;
										}else{
											//System.out.println("@@@@@@@@@@@"+temp2);
										}
									}
									//now to get emails
									if (temp2.indexOf('@') > 0) {
										matcherPerson2 = p_address1.matcher(temp2.replaceAll(".*<", ""));
										if (matcherPerson2.matches()) //NEED THIS OR WILL NOT
														 // WORK>
										{
											s_fname = matcherPerson2.group(1).trim().toLowerCase();
											s_fdom = matcherPerson2.group(2).trim().toLowerCase();
											s_femail = s_fname + "@" + s_fdom;//can
																			  // prob
																			  // leave
																			  // this
																			  // till
																			  // end.
											//now to get zone
											s_fzn = s_fdom.substring((s_fdom.lastIndexOf(".")) + 1);
										}
									}
									
									

									//make sure we had captured something
									if (s_femail.length() < 2 || temp2.indexOf('@') == -1) {

										//paste
										temp2 = new String(fromline);//stuff after the
																	 // from: ...
										//SHOULD CHECK (NEW) IF HAS @ SIGN
										if (temp2.indexOf('@') > 0) {
											//cleaning up
											//no need 2007
											
											//check if we have an email address here
											//and not delete by mistake
											if (temp2.indexOf('@') > 0) {
												matcherPerson2 = p_address1.matcher(temp2.replaceAll(".*<", ""));
												if (matcherPerson2.matches()) //NEED THIS OR WILL
																 // NOT WORK>
												{
													s_fname = matcherPerson2.group(1).trim().toLowerCase();
													s_fdom = matcherPerson2.group(2).trim().toLowerCase();
													s_femail = s_fname + "@" + s_fdom;//can
																					  // prob
																					  // leave
																					  // this
																					  // till
																					  // end.

													//now to get zone
													s_fzn = s_fdom.substring((s_fdom.lastIndexOf(".")) + 1);
												}
											} else//its unknown
											{
												//    System.out.println("temp2:" + temp2);
											}

											//end paste

										} else {

											temp = temp.replaceAll(" ", "");

											s_fname = temp.replaceFirst("From:", "").toLowerCase();
											s_fdom = "unknown1.com";
											s_femail = temp + "@" + s_fdom;//can prob
																		   // leave this
																		   // till end.

											}
									}

									docontinue = true;//continue;
								}
									break;
							case 'm': //mime version
								matcherGuy1 = p_mimever.matcher(temp);
								if (matcherGuy1.matches()) {
									//!!header+=temp+"\n";
									docontinue= true;//continue;
								}
									break;
							case 'i':
								matcherGuy1 = p_inrply.matcher(temp);
								if (matcherGuy1.matches()) {

									//do we even use this?
									//s_irpto = m.group(1).replaceAll("[<()>,]", "");
									docontinue=true;//continue;
								}
								break;//for i
							case 'x':
//								xmail tag
								matcherGuy1 = p_xmailer.matcher(temp);
								if (matcherGuy1.matches()) {
									//!!header +=temp+"\n";
									s_xmail = matcherGuy1.group(1);
									//-- s_xmail = m.group(1).replaceAll(","," ");
									// //cant have commas, or wont insert.
									//-- s_xmail = s_xmail.replaceAll("'","");
									//-- s_xmail = s_xmail.replaceAll("\"","");
									docontinue = true;//continue;
								}
								break;//for x
							//default:
								//default goes here
						}
						if(docontinue){
							continue;
						}


						//DONE: have combined the patterns to make it go
						// faster.
						//FIX ME!! no idea on how to combine/fix and make it
						// work :)
						//m = p_to.matcher(temp);
						//n = p_cc.matcher(temp);
						matcherGuy1 = p_rcpt.matcher(temp);
						if (matcherGuy1.matches())//m.matches() || n.matches() ||
										// o.matches())
						{
							
							//updated for .eml files messed out from outlook
							//logic:
							//check if have commas, if we do split around it....
							//extract email part only
							String temp23[] = matcherGuy1.group(2).trim().split(",");
							for (int i = 0; i < temp23.length; i++) {
								
								//now we should have a legal email here....
								//System.out.println(temp23[i]);
								matcherGuy3 = p_address1.matcher(temp23[i].replaceAll(".*<", ""));//p_emailname.matcher(temp23[i]);
								if(matcherGuy3.matches()){
									//System.out.println("cc:"+matcherGuy3.group(1));
									cclist.put(matcherGuy3.group(1)+"@"+matcherGuy3.group(2),"");//matcherGuy3.group(3),"");
								}
								else {
									matcherGuy3 = p_x400emailname.matcher(temp23[i]);
									if(matcherGuy3.matches()){
										//System.out.println("cc:"+matcherGuy3.group(1));
										if(s_fdom.length()>0){
											cclist.put(matcherGuy3.group(1)+"@"+s_fdom,"");
										}else
											cclist.put(matcherGuy3.group(1)+"@"+CURRENTDOMAIN,"");
									}else{
										
										//either suppresed lists or otherwise...fine since to is legal
										//System.out.println("!!!!!!!!!"+temp2 +"\n"+temp);
									}
								}
								
							}
							
							
							//System.out.println(m.group(1)+" '" +
							// m.group(2)+"'");
							//@temp23 = split /\<|,| |\>/ ,$ccline; #$1 ;
							//foreach $n (@temp23)
							//{
							//if ($n !~ /\'|\"/ && $n =~ /@/)
							//{
							//# Squash out duplicates by only accounting each
							// recipient once.
							//$cclist{$n}=1;
							//}
							//}
							//System.out.println("****"+m.group(2));

							//logic that works for long long time
							//split around [<, >]
							/*String temp23[] = matcherGuy1.group(2).trim().split("[<, >]");
							for (int i = 0; i < temp23.length; i++) {
								matcherGuy1 = p_quotes.matcher(temp23[i]);
								if (temp23[i].indexOf('@') > 0 && !matcherGuy1.matches()) {
									String rcpt1 = temp23[i].replaceAll("\\[|\\]|\\(|\\)|<|>", "");


									cclist.put(rcpt1, "1");
								}
							}*/


							continue;
						}

						//delivered-to line
						matcherGuy1 = p_delto.matcher(temp);
						if (matcherGuy1.matches()) {
							
//							logic:
							//check if have commas, if we do split around it....
							//extract email part only
							String temp23[] = matcherGuy1.group(1).trim().split(",");
							for (int i = 0; i < temp23.length; i++) {
								
								//now we should have a legal email here....
								matcherGuy3 = p_address1.matcher(temp23[i].replace(".*<", ""));//p_emailname.matcher(temp23[i]);
								if(matcherGuy3.matches()){
									//System.out.println("cc:"+matcherGuy3.group(1));
									dellist.put(matcherGuy3.group(),"");
								}
								else {
									matcherGuy3 = p_x400emailname.matcher(temp23[i]);
									if(matcherGuy3.matches()){
										//System.out.println("cc:"+matcherGuy3.group(1));
										if(s_fdom.length()>0){
											dellist.put(matcherGuy3.group()+"@"+s_fdom,"");
										}else
										dellist.put(matcherGuy3.group()+"@"+CURRENTDOMAIN,"");
									}
								}
								
							}
							
							continue;
						}

					
						//System.out.print("2223");
						//status:
						matcherGuy1 = p_status.matcher(temp);
						if (matcherGuy1.matches()) {
							//!!header +=temp+"\n";
							s_stat = matcherGuy1.group(1);
							continue;
						}

						//message-id:
						matcherGuy1 = p_msgid.matcher(temp);
						if (matcherGuy1.matches()) {
							//!!header += temp + "\n";
							s_mid = matcherGuy1.group(1);


							continue;
						}

						//clean out x-garbage
						/*matcherGuy1 = p_xstuff.matcher(temp);
						if (matcherGuy1.matches()) {

							continue;
						}*/
						//rest of stuff...if want to add features add CODE
						// here.
						matcherGuy1 = p_misc.matcher(temp);
						if (matcherGuy1.matches()) {
							//!!header +=temp+"\n";
							continue;
						}

						break;//this should fix missing emails in outlook.
						//print "$linenum &&$_\n";
						//System.out.println(count + " " + temp.length());
					}//end outer while(true)

				}//end match from line
				//didnt match the from line so in middle of message
				else {
					//System.out.println("@@@'"+line2+"'");
					header2.append(line2 + "\n");
					//header2 += line2 + "\n";
					//SPEEDUP: use concat takes from 6-> down to one sec !!! on
					// 100K test file.
					//tight loop no idea why concat is better than +=
					//on header part above concat slows it down actually :)

					//faster still: string buffer append!
					while (line.length() > 1) {

						if ((line = in.readLine()) == null)
							break;
						//header2= header2.concat(line +"\n");
						header2.append(line + "\n");
						//msg = msg.concat(line + "\n");
						//m = p_isprint.matcher(line);
						// found: a bit slower if next line is split up
						//check for next email:
						if (p_topfrom.matcher(line).matches())//!m.matches())
						{
							toread = false;
							line2 = new String(line);
							break;
						}
						//shlomo: march 2004 replaced below with above because
						// of no newlines on some formats.
						//will stop when see the from of next message
						//if(! ((Matcher)p_isprint.matcher(line)).matches()
						// )//!m.matches())
						//	continue;


						//line = line.replaceFirst("\\015","");

					}//end while
					//msg += "\n";
					//header2+="\n";
					//msg = header2.substring(start);
					//header2 = header2.concat(msg);
				}//end body part
				if (toread) {
					line = in.readLine();
					if (line != null) {
						line2 = new String(line);
						line = p_zeroFifteen.matcher(line).replaceAll("");//line.replaceAll("\\015", ""); //    tr/\015//d;
						line = p_zeroTwelve.matcher(line).replaceAll("");//line.replaceAll("\\012", "");
					}
				}
				toread = true;
			}//end while can read something from file.

			importIntoDB(dbh,filename,count,header2,s_subject,datel,s_xmail,cclist,dellist, s_type,s_mid,s_femail,s_fname,s_fdom,s_fzn,rec_path,s_stat,fromline,recount,fcount,CURRENTDOMAIN,s_typeDB,session);
			count++;
			in.close();
		} catch (IOException ioe) {
			System.out.println("Problems opening: " + s_filein + " " + ioe);
			return 0;
		} finally {
			if (do_delete) {
				// System.out.println("will have to delete temp file" +
				// s_filein);
				//do readmbx here and assign to mailfile
				do_delete = false;
				try {

					File fd = new File(s_filein);
					System.out.println("Have deleted temp file: " + s_filein + " = " + fd.delete());
				} catch (Exception e34) {
					System.out.println("problem erasing : " + e34);
				}

				/*
				 * try{ //need to execute the convertion: String cmds[] = new
				 * String[3]; cmds[0] = "del"; cmds[1] = "/y";//no extra info
				 * cmds[2] = s_filein; //filein
				 * 
				 * Runtime rt = Runtime.getRuntime(); Process proc =
				 * rt.exec(cmds); int exitVal = proc.waitFor();
				 * System.out.println("Finished delete with : " + exitVal);
				 * }catch(IOException e2){ System.out.println("problem
				 * deleteing: " + e2);} catch(InterruptedException
				 * ie3){System.out.println("interrupt: "+ ie3);}
				 */
			}

		}
		if(count >1)
		{
			count--;
		}

		if(count>10)
		   System.out.println("\n '" + s_filein + "' with " + count + " emails");
		return count;
	}

/**
 * Method to convert mail date frome Date: and header lines. leveraging
 * maildateformat of java std lib.
 * @param dateString the string date representation to parse
 * @param count which nth email we are up to (debugging)
 * @param more flag to show where this method was called in the code logic
 * @return
 */

	private final long parseDate(final String dateString,final int count, final int more) {
		
		MailDateFormat mdf = new MailDateFormat();//SimpleDateFormat();
		//static final SimpleDateFormat sqld = new SimpleDateFormat("yyyy-MM-dd");
		//static final SimpleDateFormat timed = new SimpleDateFormat("HH:mm:ss");
		
		long longtime = 0;
		//try to parse out the mail date
		Date g_date = mdf.parse(dateString, new ParsePosition(0));

		if (g_date != null)
		{	
			longtime = g_date.getTime();
		
		}else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            
			g_date = sdf.parse(dateString, new ParsePosition(0));
			if (g_date != null)
				longtime = g_date.getTime();
			else {
                SimpleDateFormat sdf2 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
                
                g_date = sdf2.parse(dateString, new ParsePosition(0));

				if (g_date != null) {
					longtime = g_date.getTime();
				} else {
                    SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");//was dd, 
                    
                    g_date = sdf3.parse(dateString, new ParsePosition(0));

					if (g_date != null) {
						longtime = g_date.getTime();
					}else {
						//System.out.print("4");
	                    SimpleDateFormat sdf4 = new SimpleDateFormat("EEEEEEEE dd MMMMMMMMMM yyyy HH:mm");//was dd, 
	                    
	                    g_date = sdf4.parse(dateString, new ParsePosition(0));

						if (g_date != null) {
							longtime = g_date.getTime();
						}else {
							//System.out.print("5");
		                    SimpleDateFormat sdf5 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZZZ");//was dd, 
		                    
		                    g_date = sdf5.parse(dateString.replaceAll("[PpaA][Mm]", ""), new ParsePosition(0));

							if (g_date != null) {
								longtime = g_date.getTime();
							}else{
//								System.out.print("6");
			                    SimpleDateFormat sdf6 = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss ZZZZ");//was dd, 
			                    
			                    g_date = sdf6.parse(dateString.replaceAll("[PpaA][Mm]", ""), new ParsePosition(0));

								if (g_date != null) {
									longtime = g_date.getTime();
								}else{
									//System.out.print("7");
				                    SimpleDateFormat sdf7 = new SimpleDateFormat("MMM dd yyyy HH:mm:ss ZZZZ");//was dd, 
				                    
				                    g_date = sdf7.parse(dateString.replaceAll("[PpaA][Mm]", ""), new ParsePosition(0));

									if (g_date != null) {
										longtime = g_date.getTime();
									}else{
										System.out.println("problem with date");
									}
								}
							}
						}

					}


				}
			}
		}

		if (longtime == 0) {
			System.out.println(more + "problem parsing date str, cnt: "+count + " '" + dateString+"'");
			
		}
		return longtime;
	}


	/**
	 * Method which interacts with the underlying db system
	 * @param s_filein
	 * @param count
	 * @param header2
	 * @param s_subject
	 * @param datel
	 * @param s_xmail
	 * @param cclist
	 * @param dellist
	 * @param s_type
	 * @param s_mid
	 * @param s_femail
	 * @param s_fname
	 * @param s_fdom
	 * @param s_fzn
	 * @param rec_path
	 * @param s_stat
	 * @param fromline
	 * @param recount
	 * @param fcount
	 */


	private final void importIntoDB( EMTDatabaseConnection dbh,final String filename,int count,StringBuffer header2,String s_subject,String datel,
			String s_xmail,Hashtable<String,String>cclist,Hashtable<String,String> dellist, String s_type,String s_mid,String s_femail,String s_fname,
			String s_fdom,String s_fzn,String rec_path,String s_stat,String fromline,int recount,int fcount,String CURRENTDOMAIN,String s_typeDB,Session session) {
		//    	System.out.println("\n"+count + ") " + s_subject +" " +
		// (header2.length()-start));
		//int len, numattach;
		//String temp = new String();
	//	System.out.println("in output: "+count);
		if (count < 2) {

			if (s_femail.toLowerCase().startsWith("mailer-daemon")) {
				count--;
				return;

			}
		}

		//keep track of diff in rcpt and header dates
		long timediff =0;
		//show something on screen for each 100th email in a specific file
		if (count % 100 == 99)
			System.out.print("-");

		//datel = p_singlecomma.matcher(datel).replaceAll("");//datel.replaceAll(",","");
		if(datel.startsWith("Date:")){
			datel = datel.substring(5).trim();
		}
		//start by saying we dont know our date field time
		long utime_dateheadertimestamp = 0;
//date from recieved line
		long utime2_rectimestamp =0;
		
		if(datel.length()>1){
			//attempt to grab the date from the date header
			utime_dateheadertimestamp = parseDate(datel,count,1);
		}
		
		//we will try to parse it out of the rec line
		int jump = rec_path.lastIndexOf(";");

		if (jump > 0) {
			String recdate = rec_path.substring(jump + 1).trim();
			recdate = p_singlecomma.matcher(recdate).replaceAll("");//recdate.replaceAll(",","");
			if(recdate.length() > 1)
			utime2_rectimestamp = this.parseDate(recdate,count,2);
			
		
		}

		//now assuming date header was parsed and maybe rec line was parsed we have some choices:
		
		
		if (utime2_rectimestamp > 0) {// #ie we have the timestamp by mail client

			if (utime_dateheadertimestamp <= 0) {
				timediff = -1;
				utime_dateheadertimestamp = utime2_rectimestamp;
			} else {
				timediff = utime2_rectimestamp - utime_dateheadertimestamp;
				//use rec if diff is more than 6 hours...
				//added in 2007
				if(b_alignDates)
					if(timediff > 25000000 || timediff < -25000000){
						utime_dateheadertimestamp = utime2_rectimestamp;
				}
			
			}
			
		} else {
			timediff = 0;//?maybe -2? probably a sent email..

			//SHLOMO TRIED TO fix timeproblem stamp
//??			utime2_rectimestamp = utime_dateheadertimestamp;
			
		}
		//System.out.println("timediff"+timediff);
		//now to setup the date
		if (utime_dateheadertimestamp > 0) {
			//s_date = new java.sql.Date(utime).toString();
			//s_time = new java.sql.Time(utime).toString();
			last_good_utime = utime_dateheadertimestamp;
			//System.out.print(s_subject);
		} else {
			System.out.println("subject:'"+s_subject+"' needs to replace date (from prev) in email, cnt:"+count +" will use:"+last_good_utime);
			utime_dateheadertimestamp = last_good_utime;
		}
		//copy of message part of the email
		//now to get hash of body
		String s_msghash = md5sum(header2.toString().getBytes());//.substring(start).getBytes());//msg);
		//make sure the mid is fine
		if (s_mid.length() < 8){
			int number = filename.indexOf(File.separator);
			if(number != -1){
				s_mid += s_msghash + filename.substring(number+1).hashCode() + count;
			}
			else
			{
				s_mid += s_msghash + filename.hashCode() + count;
			}
		}
		//set mid to have the date time infrom
		
		s_mid = utime_dateheadertimestamp/1000 + s_mid;
		//clean up message id
		//s_mid = s_mid.replaceAll("\\","");
		s_mid = p_singlecomma.matcher(s_mid).replaceAll("");//s_mid.replaceAll(",", "");
		s_mid = s_mid.replaceAll("\\\\", "");
		s_mid = s_mid.replaceAll("'", "");
		s_mid = s_mid.replaceAll("`", "");
		s_mid = s_mid.replaceAll("^", "");
		s_mid = s_mid.replaceAll("_", "");
		s_mid = s_mid.replaceAll("%", "");
		s_mid = s_mid.replaceAll("\"", "");
		s_mid = s_mid.trim();
		if(s_mid.length()>125){
			s_mid = s_mid.substring(0,120);
		}
		//clean up rec path
		//--rec_path = rec_path.replaceAll("\\\\","");
		//--rec_path = rec_path.replaceAll("`","");
		//--rec_path = rec_path.replaceAll(",","\\\\,");
		//--rec_path = rec_path.replaceAll("'","");
		//--rec_path = rec_path.replaceAll("\"","");

		//TODO: pull out real date from last timestamp

		int len = header2.length();// - start; //total email body size.

		if (b_tolog) {
			System.out.println(count + " size = " + len);
			m_outlog.println(count + " size = " + len);
			//print LOG "$count: size = $len\n";
		}
		//was numhash
		//int numattach = 0;
		int level = 0; //how deep into the message parts
		//String temp = new String();
		MimeMessage mm;
		int attnum = 0; //number of attachments
		try {
			//problem with certian spam emails, most of the headers get sent to
			// the body part. so....we need to process it here
			//size is start of body part
			//    System.out.print("h "+header2.length() + " " +start+"
			// "+s_boundry.length()+" ");
			//String s = header2.substring(start, start+3+ s_boundry.length()
			// );
			//if (!s_boundry.equals("") &&
			// (s).trim().startsWith("--"+s_boundry))
			//	    {
			//					System.out.println("need fodder"+s_subject);
			//	$mime_fodder = "This is text fodder, which MIME forces us to
			// insert before the first MIME boundary. Apparently the sending MUA
			// is not fully compliant\n";

			//	    }
			header2.append("\n");

			if (s_femail.length() < 2 && fromline.indexOf('@') > 0) {
				System.out.print(".!!..(" + count + ")");
				mm = new MimeMessage(session, new ByteArrayInputStream(header2.toString().trim().replaceAll("\n\n",
						"\n").getBytes()));//quick fix
				//System.out.println("d"+start);
			} else
				mm = new MimeMessage(session, new ByteArrayInputStream(header2.toString().trim().getBytes()));
			//(header+"\n"+mime_fodder+"\n"+msg+"\n").getBytes()));

			
			//temp = ((Part) mm).getContentType();
			attnum = parseMime(dbh,mm, level, 0,header2,s_mid,rec_path,attnum,session);
			//System.gc();
			//	    System.out.print("num attach: " + attnum + " ");

		} catch (MessagingException pt) {
			//TODO FIX THOSE EMAILS WHICH SHOW PROBLEMS WITH ATTACHAMENT
			// BOUNDARY!!!

			//System.out.println("type:" + header2+"\nbound: "+s_boundry+"
			// \n"+level + " - subject: " + s_subject+"\nreport this please,
			// pt:"+pt);
			return;

		}

		//done inserting into the message db now to do regular.
		//try to recover the subject line

		//if we havent collected the rcpt from the header will do it now.
		if (cclist.size() <= 0) {

			try {
				Address[] data = mm.getRecipients(Message.RecipientType.TO);// mm.getHeader("To");
				if (data != null)
					for (int i = 0; i < data.length; i++) {
						String name = data[i].toString().replaceAll(".*<","");
						name = name.replaceAll(">.*","");
						if (name.indexOf('@') > 0)
							cclist.put(name, "1");
						else
							cclist.put(name + "@unknown.com", "1");

					}
			} catch (Exception cc) {
				cc.printStackTrace();
			}
		}

		int numcc = cclist.size();
		if (numcc <= 0)//cclist has no names
		{
			numcc = dellist.size(); //check delivered to list
			cclist = dellist;
			if (numcc <= 0) //still no emails, must make one up
			{
				numcc = 1;
				if (filename.length() < 25) {
					cclist.put(filename + "@spammail.com", "1");
				} else {
					cclist.put(filename.substring(0, 25) + "@spammail.com", "1");
				}

			}

		}//end numcc

		//check if have COLLECTED valid date
		
		//SHOULD NOT BE TRIGGERED
		if (utime_dateheadertimestamp <= 0) {
			
			try {
				String[] data = mm.getHeader("Date");
				String ty;
				if (data != null) {
					ty = p_singlecomma.matcher(data[0]).replaceAll("");//data[0].replaceAll(",","");
					
					if(ty.length()>1)
					utime_dateheadertimestamp = parseDate(ty,count,3);
					//-- if(utime>0){
					//-- s_date = new java.sql.Date(utime).toString();
					//-- s_time = new java.sql.Time(utime).toString();//was
					// timestamp
					//-- }
				} else {
					if (count == 0)
						return;

					System.out.println(count + "could not catch date:" + s_subject + "..will reuse last");
				}
			} catch (Exception cc) {
			}
			
			if(utime_dateheadertimestamp<=0){
				utime_dateheadertimestamp = last_good_utime;
			}
			else{
				last_good_utime = utime_dateheadertimestamp;
			}
			
			
		}



		if (s_subject.length() < 1) {
			//		System.out.println(header2);
			try {
				String[] data = mm.getHeader("Subject");

				if (data != null)
					s_subject = data[0];
				//s_subject = mm.group(1).trim(); // save the subject line
				//want to count re's
				recount = 0;
				Matcher n = p_recount.matcher(s_subject); //look for re's
				if (n.find()) {
					recount++;
					while (n.find())
						recount++;
					s_subject = n.replaceAll("");//will replace all re's so
												 // clean subjet
				}
				fcount = 0;
				Matcher o = p_fwdcount.matcher(s_subject); //look for re's
				if (o.find()) {
					fcount++;
					while (o.find())
						fcount++;

					s_subject = o.replaceAll("");//will replace all re's so
												 // clean subjet
				}

				s_subject = s_subject.trim();

				if (b_tolog) {
					System.out.println("LOGGING2: " + s_subject);
					m_outlog.println("LOGGING2: " + s_subject);
				}



			} catch (Exception cc) {
			}

		}

		String temp2 = new String();
		if (s_femail.length() < 2) {
			//need to deal with the issue of blank emails....
			try {
	
				String[] data = mm.getHeader("From");

				if (data != null) {
					temp2 = data[0];
					
					//new lgoic for 2007
					Matcher matcherGuy3 = p_address1.matcher(temp2.replaceAll(".*<", ""));//p_emailname.matcher(temp2);
					if(matcherGuy3.matches()){
						temp2=matcherGuy3.group(1)+"@"+matcherGuy3.group(2);//june 2007//matcherGuy3.group();
					}
					else {
						matcherGuy3 = p_x400emailname.matcher(temp2);
						if(matcherGuy3.matches()){
							//System.out.println("cc:"+matcherGuy3.group(1));
							temp2=matcherGuy3.group()+"@"+CURRENTDOMAIN;
						}else{
							System.out.println("444444444"+temp2);
						}
					}

					//check if we have an email address here
					//and not delete by mistake
					if (temp2.indexOf('@') > 0) {
						Matcher m = p_address1.matcher(temp2.replaceAll(".*<", ""));//p_address1.matcher(temp2);
						if (m.matches()) //NEED THIS OR WILL NOT WORK>
						{
							s_fname = m.group(1).trim().toLowerCase();
							s_fdom = m.group(2).trim().toLowerCase();
							s_femail = s_fname + "@" + s_fdom;//can prob leave
															  // this till end.

							//now to get zone
							s_fzn = s_fdom.substring((s_fdom.lastIndexOf(".")) + 1);
						} else {
							s_femail = "XX@XX.X";
							s_fdom = "XX";
							s_fzn = "X";
							s_fname = "X";


						}
					}

				} else {

					s_femail = "XYX2@XX2.X";
					s_fdom = "XX2.X";
					s_fzn = "X";
					s_fname = "XYX2";
				}

			} catch (Exception cc) {
			}

		}

		if (b_hashContents) {
			StringTokenizer stok = new StringTokenizer(s_subject);
			s_subject = new String();
			while (stok.hasMoreTokens()) {
				s_subject = s_subject.concat(md5sum(stok.nextToken().getBytes()) + ' ');
			}
			s_subject = s_subject.trim();

		} else {
			//need to clean out the sbuject line
			//--s_subject = s_subject.replaceAll("\\\\","\\\\\\\\");

			//--s_subject = s_subject.replaceAll("'","\\\\'");
			//--s_subject = s_subject.replaceAll(",","\\\\,");
			//--s_subject = s_subject.replaceAll("_","\\\\_");

		}


String s_rname = new String();
String s_rdom = new String();
String rcpt = new String();
String s_rzn = new String();
		//--return_path = return_path.replaceAll(",","\\\\,");
		//--return_path = return_path.replaceAll("_","\\\\_");

		//??return_path = return_path.replaceAll("'","\\'");
		//--s_forward = s_forward.replaceAll(",","\\\\,");
		//--s_irpto = s_irpto.replaceAll("'","");
		//--s_irpto = s_irpto.replaceAll(",","");
		//--s_irpto = s_irpto.replaceAll("_","");

		int mid = s_type.indexOf(';');
		if (mid > 0)
			s_type = s_type.substring(0, mid);

		//iterator so that for ech recpient email we insert another
		// sender->rcpt int he email table
		for (Enumeration<String> itEnuma = cclist.keys(); itEnuma.hasMoreElements();) {
			rcpt = itEnuma.nextElement().toLowerCase();
			rcpt = rcpt.replaceAll(".*<", "");
			rcpt = rcpt.replaceAll("'", "");
			rcpt = rcpt.replaceAll("\"", "");
			rcpt = p_singlecomma.matcher(rcpt).replaceAll("");//rcpt.replaceAll(",", "");

			mid = rcpt.indexOf('@');
			if (mid > 0) {
				s_rname = rcpt.substring(0, mid);
				s_rdom = rcpt.substring(mid + 1).trim();
			} else {
				s_rname = "unknown-rcpt";
				s_rdom = "uknown";
			}
			mid = rcpt.lastIndexOf('.');
			if (mid > 0)
				s_rzn = rcpt.substring(mid + 1);
			else
				s_rzn = "";


			//	if($count<3)
			//{
			//#?? print "$count: $rcpt\n";
			//}
			//($rname, $rdom) = split(/\@/, $rcpt);
			//@labels = split(/\./, $rdom);
			//$rzn = $labels[$#labels];

			//temp = BEGININSERT; //start of the insert line
			if (b_hashContents) {
				System.out.println("nothing in hashcontent body");
				/*
				 * ##once have the subject need to parse it and ##divide by
				 * word, if hash then hash each one $len','$email_hash', $temps .=
				 * "'" . md5_hex($femail) . "','" . md5_hex($fname) . "','" .
				 * md5_hex($fdom) . "','" . md5_hex($fedom) .
				 * "','$date','$time','$timediff','$subj','$re','$type','$stat','".
				 * md5_hex($mid) . "','".
				 * md5_hex($filename)."','$numcc','$numhash','". md5_hex($rcpt) .
				 * "','". md5_hex($rname) . "','". md5_hex($rdom) . "','".
				 * md5_hex($rzn) . "','$date $time','" . md5_hex($xmail) .
				 * "','$utime')"; $ret = $dbh->do($temps); # print "$temps\n";
				 * print STDERR "Insert failed 4: " . $dbh->errstr . "\n" if
				 * (!defined $ret);
				 */
			} else {
				//temp += "'" +len + CM + s_msghash + CM +
				//s_femail + CM + s_fname + CM + s_fdom + CM +
				//s_fzn + CM + s_date + CM + s_time + CM + timediff +
				//CM + s_subject + CM + recount + CM + fcount +
				//CM + s_type + CM + s_stat + CM + s_mid + CM + filename +
				//CM + numcc + CM + attnum + CM + rcpt + CM + s_rname +
				//CM + s_rdom + CM + s_rzn + CM + s_date + ' ' +
				//s_time + CM + s_xmail + CM + utime + "')";
				try {
					/*
					 * ("INSERT INTO email (size,msghash,sender,sname,sdom,szn," +
					 * "dates,times,timeadj,subject,recnt,forward,type," +
					 * "flags,mailref,folder,numrcpt,numattach," +
					 * "rcpt,rname,rdom,rzn,insertTime,xmailer,utime) " +
					 */
					//	ps_emailinsert.clearParameters();
					//size,msghash,sender,sname,sdom,
					//szn,dates,times,timeadj,subject,
					//recnt,forward,type,flags,mailref,
					//folder,numrcpt,numattach,rcpt,
					//rname,rdom,rzn,insertTime,xmailer,utime)
					//java.sql.Date dateobj = new java.sql.Date(utime);
					if(utime_dateheadertimestamp < 1){
						System.out.println("!!!!!!!!!!!!!!HELP");
					}
					
					Time timeobj = new Time(utime_dateheadertimestamp);

					ps_emailinsert.setInt(1, len);
					ps_emailinsert.setString(2, s_msghash);
					
					//hotmail has quotes in email sometimes
					if(s_femail.contains("\"")){
						s_femail = s_femail.replaceAll("\"", "");
					s_fname = s_fname.replaceAll("\"", "");
					s_fdom = s_fdom.replaceAll("\"", "");
					s_fzn = s_fzn.replaceAll("\"", "");
					}
					
					
					ps_emailinsert.setString(3, s_femail);

					if (s_fname.length() > 63) {
						ps_emailinsert.setString(4, s_fname.substring(0, 62) + "..");
					} else {
						
						ps_emailinsert.setString(4, s_fname);
					}

					ps_emailinsert.setString(5, s_fdom);
					ps_emailinsert.setString(6, s_fzn);
					//try{
					ps_emailinsert.setDate(7, new java.sql.Date(utime_dateheadertimestamp));//dateobj);//TODO
																		// check
																		// this
																		// out

					ps_emailinsert.setTime(8, timeobj);
					//}catch(ParseException ps1){
					//	System.out.println(ps1);
					//	}

//					System.out.print(" timediff"+timediff);
					ps_emailinsert.setLong(9,  timediff);
					
					if (s_subject.length() > 254) {
						ps_emailinsert.setString(10, s_subject.substring(0, 250) + "...");
					} else {
						ps_emailinsert.setString(10, s_subject);
					}
					ps_emailinsert.setInt(11, recount);
					ps_emailinsert.setString(12, "" + fcount);
					ps_emailinsert.setString(13, new String(s_type));

					ps_emailinsert.setString(14, s_stat);
					ps_emailinsert.setString(15, s_mid);
					ps_emailinsert.setString(16, filename);
					ps_emailinsert.setShort(17, (short) numcc);
					ps_emailinsert.setShort(18, (short) attnum);
					ps_emailinsert.setString(19, rcpt);
					if (s_rname.length() > 63) {
						ps_emailinsert.setString(20, s_rname.substring(0, 62) + "..");
					} else {
						ps_emailinsert.setString(20, s_rname);
					}
					ps_emailinsert.setString(21, s_rdom);
					ps_emailinsert.setString(22, s_rzn);
					if (s_typeDB.equals(DatabaseManager.sDERBY)) {
						try {
							ps_emailinsert.setTimestamp(23, new Timestamp(utime_dateheadertimestamp));
						} catch (IllegalArgumentException il) {
						}
					} else {
						ps_emailinsert.setString(23, new java.sql.Date(utime_dateheadertimestamp).toString());
					}
					if(s_xmail.length() > 100){
						s_xmail = s_xmail.substring(0,96) +"..";
					}
					
					ps_emailinsert.setString(24, s_xmail);
					//need to divide by 1000 since unix keeps number of seconds not milli
					ps_emailinsert.setLong(25, (utime_dateheadertimestamp));
					try {
						GregorianCalendar gc = new GregorianCalendar();
						gc.setTime(new Date(utime_dateheadertimestamp));//sqld.parse(s_date));
						ps_emailinsert.setInt(26, gc.get(Calendar.YEAR));//1900+sqld.parse(s_date).getYear());//dateobj.getYear());
						ps_emailinsert.setInt(27, gc.get(Calendar.MONTH));//1+sqld.parse(s_date).getMonth());//dateobj.getMonth());
						ps_emailinsert.setInt(28, gc.get(Calendar.HOUR_OF_DAY));//1+sqld.parse(s_date).getHours());//dateobj.getHours());
					} catch (Exception ps) {
						System.out.println("EXCEPTION SETTING DATA\n" + s_femail + "\n" + count + " --" + ps);
						//backup
						java.sql.Date dateobj = new java.sql.Date(utime_dateheadertimestamp);
						String datestring = dateobj.toString();
						try {
							ps_emailinsert.setInt(26, Integer.parseInt(datestring.substring(0, 4)));
							ps_emailinsert.setInt(27, Integer.parseInt(datestring.substring(5, 7)));
							ps_emailinsert.setInt(28, Integer.parseInt(datestring.substring(8)));

						} catch (NumberFormatException s) {
							System.out.println(s);
						}

					}
					dbh.doPrepared(ps_emailinsert);
					// dbh.updateSQLData(temp);
				} catch (SQLException sql1) {
					//TODO: testing only
					if(sql1.getErrorCode() != DB_DUP_KEY && sql1.getErrorCode() != ER_DUP_ENTRY && sql1.getErrorCode() != DERBY_ERROR && sql1.getErrorCode() != DERBY_ERROR2){
						
						if(!sql1.toString().contains("duplicate key"))
						{	sql1.printStackTrace();
						System.out.println("v1" + sql1.getErrorCode());
						}
					}
					
					
					
				
				} 

			}


		}//end iterator loop for each rcpt.
		/*
		 * if(@foundwords) { if (defined($dbh)) { $temps = "INSERT INTO kwords
		 * (mailref,hotwords) VALUES('$mid','". (join "|", @foundwords) ."')";
		 * $ret = $dbh->do($temps); #print "$temps\n";
		 * 
		 * print STDERR "Insert failed: " . $dbh->errstr . "\n" if (!defined
		 * $ret); } print STDERR "Found hot words: " . join("|", @foundwords).
		 * "\n" if ($debug); }
		 */







	} //end output

	/**
	 * 
	 * Will parse the email message and return the number of attachments seen, recursive defined so can prcocess recursive attachments
	 * @param p
	 * @param level
	 * @param stop
	 * @param header2
	 * @param s_mid
	 * @param rec_path
	 * @param numattach
	 * @return the number of seen attachments
	 * @throws MessagingException
	 */
	private final int parseMime(EMTDatabaseConnection dbh,Part p, int level, int stop,StringBuffer header2,String s_mid,String rec_path, int numattach,Session session) throws MessagingException {

		String small = null;
		//start paste
		String filename2 = p.getFileName();
		
		if (filename2 == null)
			filename2 = "";//p.getContentType();
		else {
			filename2 = filename2.replaceAll("'", "\\\\'");
			filename2 = p_singlecomma.matcher(filename2).replaceAll("\\\\,");//filename2.replaceAll(",", "\\\\,");
			filename2 = filename2.replaceAll("_", "\\\\_");
			filename2 = filename2.replaceAll("\\\\", ""); //fixed for windows
			filename2 = filename2.replaceAll("/", ""); //fixed for windows
			//filename2 = filename2.replaceAll("\\.", ""); //fixed for windows
		}
		//"[\\s\\']","");//celan up so it doesnt messs sql


		/*
		 * Using isMimeType to determine the content type avoids fetching the
		 * actual content data until we need it.
		 */
		//	if (p.isMimeType("text/plain")) {
		//	        System.out.println("This is: plain text");
		//return;
		//((String)p.getContent());
		//}
		//	else
		if (p.isMimeType("text/plain") || p.isMimeType("text/html")) {
			//String q = new String();
			try {//start

				try {
					small = (String) p.getContent();
				} catch (IOException ie) {

					//System.out.println("in jump 1");
					//some email have messed up by saying
					// UnsupportedEncodingException
					//trick treat as input stream
					if (ie.toString().indexOf("UnsupportedEncodingException") > 0
							|| ie.toString().indexOf("Error in encoded stream, got") < 0) {
						try {
							InputStream is = p.getInputStream();
							int c;
							StringBuffer small2 = new StringBuffer(2048);
							byte buf[] = new byte[bytebuf];
							while ((c = is.read(buf)) != -1)
								{
									small2.append(new String(buf, 0, c));
								}
							
							if (c > 0)
							{
								small2.append(new String(buf, 0, c));
							}

							small = new String();
							if (small2.length() > MAX_BODY) {
								small = small2.substring(0, MAX_BODY - 1);
							} else {
								small = small2.toString();
							}
							small2 = null;
						} catch (Exception sub2) {
							throw new MessagingException();
						}
					}//throw new MessagingException();



					//problem with the datahandler object. can be fixed by just
					// deleting the last character.
					//ie:
					if (small == null)// || small.length() < 10)//if we have
									  // something its fine.
					{
						//System.out.print(" cleaning up header");
						header2 = header2.delete(header2.length() - (stop + 1), header2.length());
						//try again
						if (stop < 2) {
							//	System.out.print (stop+" " + s_subject+" ");
							//System.exit(1);
							numattach = parseMime(dbh,new MimeMessage(session, new ByteArrayInputStream(header2.toString().trim()
									.getBytes())), level, stop++,header2,s_mid,rec_path,numattach,session);
							//System.out.println("ie: " + ie);
						} else
							throw new MessagingException();
						return numattach;
					}
					//		    if(small==null)
					//small = "";


				}//end catch
				//System.out.print(".--."+small.length());

				int nlen = small.length();

				if (nlen > MAX_BODY)
					{small = small.substring(0, MAX_BODY - 1);
					}
				//else
				//    small = small.substring(0);

				//FILEZ
				//no need since blob type
				//small = Cleanup(small);

				//no need since fixed by turning off escape sequence in the
				// jdbcconnect class
				//see :
				// http://www.geocrawler.com/archives/3/193/1999/9/50/2597144/
				//old stuff:
				//small = small.replaceAll("[\000-\032]","");
				//small = small.replaceAll("[\340-\355]","");
				//actually for non text this is bad. fixed by using a prepared
				// statment in non text body

				//small = small.replaceAll("_","\\\\_");
				//	small = small.replaceAll("%","\\\\%");


				//TODO KWORDS HERE
				//try{

				String temp = p.getContentType();
				//null check??
				if (temp != null) {
					int mid = temp.indexOf(';');
					if (mid > 0)
						temp = temp.substring(0, mid);

					temp = temp.replaceAll("`", " ");
					temp = p_singlecomma.matcher(temp).replaceAll(" ");//temp.replaceAll(",", " ");
					temp = temp.replaceAll("'", "");
					temp = temp.replaceAll("\"", "");
				} else
				{
					temp = " ";
				}

				if (nlen == 0) {
					small = new String(" ");
				}

				if(temp.length() ==0){
					temp = " ";
				}
				
//				trying to grab filename:
				/*int endn,startn = small.indexOf("filename=\"");
				if(startn != -1){
					endn = small.indexOf("\"",(startn+11));
					if(endn != -1){
						System.out.println("FILENAME:" + small.substring(start+11,endn));
					}
				}
				*/
				
				//q = "INSERT INTO message
				// (filename,mailref,hash,received,body,size,type
				//) VALUES ('"+filename2+"','" + s_mid + "','"+md5sum(small)
				// +"','" + rec_path + "','" + small + "','" +nlen +"','"+ temp
				// +"')";
				//ps_message = dbh.prepareStatementHelper("INSERT INTO message
				// (filename,mailref,hash,received,body,size,type)
				// VALUES(?,?,?,?,?,?,?)");
				
				//ps_message.clearParameters();
				ps_message.setString(1, filename2);
				ps_message.setString(2, s_mid);
				ps_message.setString(3, md5sum(small.getBytes()));
				ps_message.setString(4, rec_path);
				ps_message.setBinaryStream(5, new ByteArrayInputStream(small.getBytes()), small.length());
				ps_message.setInt(6, nlen);
				ps_message.setString(7, temp.toLowerCase());
				//ps_message.executeUpdate();
				dbh.doPrepared(ps_message);
				//dbh.updateSQLData(q);

			} catch (MessagingException s) {
				throw new MessagingException();
			} catch (SQLException sql1) { //changed from exception
				if(sql1.getErrorCode() != DB_DUP_KEY && sql1.getErrorCode() != ER_DUP_ENTRY && sql1.getErrorCode() != DERBY_ERROR && sql1.getErrorCode() != DERBY_ERROR2){
					
					if(!sql1.toString().contains("duplicate key")){
						System.out.println("v2" + sql1.getErrorCode());
						sql1.printStackTrace();
					}
				}
				
			}
		
		} else if (p.isMimeType("multipart/*")) {
			//System.out.println("This is a Multipart" + header2.length());

			//pr("---------------------------");
			level++;
			try {
				Multipart mp = (Multipart) p.getContent();

				
				for (int i = 0; i < mp.getCount(); i++) {
					numattach = parseMime(dbh,mp.getBodyPart(i), level, ++stop,header2,s_mid,rec_path,numattach,session);
				}

			} catch (IOException e) {
				System.out.println("problem running through parts" + e);
			}
			level--;
		} else if (p.isMimeType("message/rfc822")) {
			//	    System.out.println("This is a Nested Message");
			//pr("---------------------------");
			level++;
			try {
				numattach = parseMime(dbh,(Part) p.getContent(), level, 0,header2,s_mid,rec_path,numattach,session);

			} catch (IOException e2) {
				System.out.println("e2" + e2);
			}
			level--;
		} else {

			/*
			 * If we actually want to see the data, and it's not a MIME type we
			 * know, fetch it and check its Java type.
			 */
			try {

				Object o = p.getContent();
				//System.out.print(")");
				if (o instanceof String) {
					//if(showmore)
					//System.out.print("STRNG");
					//we can now process it :)
                    int truelength = ((String) o).length();
					if (truelength > MAX_BODY)
						small = ((String) o).substring(0, MAX_BODY - 1);
					else
						small = (String) o;

					//trying to grab filename:
					/*int endn,startn = small.indexOf("filename=\"");
					if(startn != -1){
						endn = small.indexOf("\"",(startn+11));
						if(endn != -1){
							System.out.println("FILENAME:" + small.substring(start+11,endn));
						}
					}*/
					
					
					
					/*
					 * n = p_base64.matcher(header2); if(n.matches()) {
					 * System.out.println("2!!!!!!!!!!!!!!!!!!GOt base64"); }
					 */
					//FILEZ
					//no need since blob type
					//  small = Cleanup(small);
					//small = small.replaceAll("\\\\","\\\\\\\\"); //???
					//small = small.replaceAll("'","\\\\'");
					//small = small.replaceAll("`","\\\\`");
					//small = small.replaceAll(",","\\\\,");
					//TODO KWORDS HERE
					try { //?

						String  temp = p.getContentType();
						if (temp != null) {
							int mid = temp.indexOf(';');
							if (mid > 0)
								temp = temp.substring(0, mid).trim();
							temp = temp.replaceAll("\n", "");
							temp = temp.replaceAll("\t", "");
							temp = p_singlecomma.matcher(temp).replaceAll(" ");//temp.replaceAll(",", " ");
							temp = temp.replaceAll("`", " ");
							temp = temp.replaceAll("'", "");
							temp = temp.replaceAll("\"", "");
						}
						if (small.length() == 0) {
							small = new String(" ");
						}

                        ps_message.setString(1, filename2);
						ps_message.setString(2, s_mid);
						ps_message.setString(3, md5sum(small.getBytes()));
						ps_message.setString(4, rec_path);
						ps_message.setBinaryStream(5, new ByteArrayInputStream(small.getBytes()), small.length());
						ps_message.setInt(6, truelength);
						ps_message.setString(7, temp.toLowerCase());
						//ps_message.executeUpdate();
						dbh.doPrepared(ps_message);



					} catch (SQLException sql1) {//changed frm exception
						if(sql1.getErrorCode() != DB_DUP_KEY && sql1.getErrorCode() != ER_DUP_ENTRY && sql1.getErrorCode() != DERBY_ERROR && sql1.getErrorCode() != DERBY_ERROR2){
							
							if(!sql1.toString().contains("duplicate key"))
							{	sql1.printStackTrace();
							    System.out.println("v3" + sql1.getErrorCode());
							}
						}
						//TODO: testing only
						/*if (sql1.toString().indexOf("Duplicate entry") == -1
								&& sql1.toString().indexOf("duplicate key") == -1)
							System.err.println("Insert failed 2b: " + sql1);*/
					}
				} else {
					 //   System.out.print("3");
					//this block includes input streams and unknowns
					//will use as input stream
					InputStream is;
                    int totallength = 0;
					if (o instanceof InputStream)
                    {
                        is = (InputStream) o;
                    }else{
                        is = p.getInputStream();
                    }
					int c;
					//StringBuffer small2 = new StringBuffer(2048);
					byte buf[] = new byte[bytebuf];
                    ArrayList<byte[]> bytelist = new ArrayList<byte[]>();
                    c = is.read(buf); 
                    while (c != -1)
                    {
                        totallength += c;
                        bytelist.add(buf);
                       // small2.append(new String(buf, 0, c));
                        buf = new byte[bytebuf];
                        c= is.read(buf); 
                    }
                    //small = small.concat(new String(buf));
			//		if (c > 0)
              //      {
                //        small2.append(new String(buf, 0, c));
                  //  }


					// int nlen;
					//small = new String();
				/*	if (totallength > MAX_BODY) {
						System.out.println("max");
                        //nlen = small.length();
						small = small2.substring(0, MAX_BODY - 1);
					} else {
						// nlen = small2.length();
						small = small2.toString();
					}
					*///small2 = null;//to clean up
					//for binary processing
					//TODO KWORDS HERE
					try {

						String temp = p.getContentType();
						int mid = temp.indexOf(';');
						if (mid > 0)
						{
							temp = temp.substring(0, mid).trim();
						}
						temp = temp.replaceAll("\n", "");
						temp = temp.replaceAll("\t", "");
						temp = p_singlecomma.matcher(temp).replaceAll(" ");//temp.replaceAll(",", " ");
						temp = temp.replaceAll("`", " ");
						//temp = temp.replaceAll(","," ");
						temp = temp.replaceAll("'", "");
						temp = temp.replaceAll("\"", "");


						//if (o instanceof InputStream)
						//		is = (InputStream)o;
						//else
						//	is = p.getInputStream();
						//Connection con = dbh.getConnection();
						//String sqlpre = "INSERT INTO message
						// (filename,mailref,hash,received,body,size,type)
						// VALUES(?,?,?,?,?,?,?)";
						//PreparedStatement pstmt =
						// con.prepareStatement(sqlpre);
						//pstmt.setEscapeProcessing(false);
						//new stuff
						// Create some binary data
						//pstmt.setBytes(5,small.getBytes() );

						if (totallength == 0) {
							byte[]t = new byte[1];
                            t[0] =' ';
                            bytelist.add(t);
                            totallength=1;
                            //small = new String(" ");
						}
//						trying to grab filename:
						/*int endn,startn = small.indexOf("filename=\"");
						if(startn != -1){
							endn = small.indexOf("\"",(startn+11));
							if(endn != -1){
								System.out.println("FILENAME:" + small.substring(start+11,endn));
							}
						}
                                             
                        
                        
*/
                        
                        //build the byte array of the body
                        //but cant pass the max length thing
                        int cnt = totallength;
                        
                        if(totallength > MAX_BODY){
                            cnt = MAX_BODY;
                        }
                        //System.out.println("total length is "+totallength);
                        
                        byte[]totalbody = new byte[cnt];
                        
                       cnt =0;
                        for(int i=0;i<bytelist.size();i++){
                            
                            byte[]pc = bytelist.get(i);
                            
                            for(int j=0;j<pc.length && cnt<totalbody.length; j++){
                                totalbody[cnt++]= pc[j];
                            
                                
                            }
                            
                        }
                    	ps_message.setString(1, filename2);
						ps_message.setString(2, s_mid);
						ps_message.setString(3, md5sum(totalbody));///small.getBytes()));
						ps_message.setString(4, rec_path);
						ps_message.setBinaryStream(5, new ByteArrayInputStream(totalbody), cnt);
						//ps_message.setBinaryStream(5, is, small.length());
						ps_message.setInt(6, totallength);
						ps_message.setString(7, temp.toLowerCase());
						//ps_message.executeUpdate();
						dbh.doPrepared(ps_message);
						// Insert the data
						//TODO CLEAN UP CONTENTTUYPE SO EVERYTHING AFTER ; is
						// erased
						//dbh.executeQuery("INSERT INTO message
						// (filename,mailref,hash,received,body,size,type)
						// VALUES ('"+filename2+"','" + s_mid +
						// "','"+md5sum(small)+"','" + rec_path + "','" + small
						// + "','" + small.length() +"','"+temp+"')");
					} catch (SQLException sql1) {//changed from exception
						if(sql1.getErrorCode() != DB_DUP_KEY && sql1.getErrorCode() != ER_DUP_ENTRY && sql1.getErrorCode() != DERBY_ERROR && sql1.getErrorCode() != DERBY_ERROR2){
							if(!sql1.toString().contains("duplicate key"))
							{	sql1.printStackTrace();
							    System.out.println("v5" + sql1.getErrorCode());
							}/*System.out.println("v5" + sql1.getErrorCode());
							sql1.printStackTrace();*/
						}
						//TODO: testing only
/*						if (sql1.toString().indexOf("Duplicate entry") == -1
								&& sql1.toString().indexOf("duplicate key") == -1){
							System.err.println("Insert failed 2d: " + sql1);
							sql1.printStackTrace();
							}*/
					}

				}

			} catch (IOException e3) {
				System.out.println("er3" + e3 + "gg");
			}


		}

		try {
			if (level != 0 && !p.isMimeType("multipart/*")) {

				String disp = p.getDisposition();
				//   System.out.println("@@@@@@@@" + disp);
				// many mailers don't include a Content-Disposition

				if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
					numattach++;
				}
			}
		} catch (Exception t) {
			System.out.println("tt:" + t);
		}
		return numattach;
		//end paste
	}//end parse mime



	/** method to return md5 */
	/*private final String md5sum(String inputtext) {
		try {
			// get Instance from Java Security Classes
			final MessageDigest md5 = MessageDigest.getInstance("MD5");

			StringBuffer sb = new StringBuffer();

			md5rslt = md5.digest(inputtext.getBytes());

			for (int i = 0; i < md5rslt.length; i++) {
				if ((0xff & md5rslt[i]) < 16) {
					// add leading zero if value is a single digit
					sb.append("0");
				}
				sb.append(Integer.toHexString((0xff & md5rslt[i])));
			}
            System.out.println(" " + sb.toString());
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			System.err.println(ex);
			return null;
		}
	}
*/
    
    
   /**
    * method to return the md5sum
    */ 
    private final String md5sum(byte[] inputtext) {
        try {
            // get Instance from Java Security Classes
            final MessageDigest md5 = MessageDigest.getInstance("MD5");

            StringBuffer sb = new StringBuffer();

            byte [] md5rslt = md5.digest(inputtext);

            for (int i = 0; i < md5rslt.length; i++) {
                if ((0xff & md5rslt[i]) < 16) {
                    // add leading zero if value is a single digit
                    sb.append("0");
                }
                sb.append(Integer.toHexString((0xff & md5rslt[i])));
            }
           
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
            return null;
        }
    }


	/**
     * this checks if this file is a mbox file basically look if it starts with
     * from...
     *
     * @param filename
     * @return
	 */

	private final boolean is_mbox(String filename) {

		//most simple check if ends with correct extension :)
		if (filename.toLowerCase().endsWith(".dbx") || filename.toLowerCase().endsWith(".mbx"))
			return false;


		int nbytes = 1;
		int totalbytes = 0;
		final String mbox_seperator = new String("from ");
		int len = mbox_seperator.length();
		boolean ret = true;
		String opening = new String();
		//return true;
		//my($filename)=@_;
		//my($nbytes)=1;
		//my($totalbytes) = 0;
		//    my($mbox_separator) = "From ";
		//my($len) = length($mbox_separator);
		//my($buf);
		char[] buf = new char[80];

		//my($opening) = "";
		//my($ret) = 1;
		System.out.println("Mbox Check: " + filename);

		//open(FILE, "< $filename") || die "Could not open $filename for
		// reading: $!\n";
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename),BUFREADBUFFERNUMBER);
			while (totalbytes < len) {
				nbytes = in.read(buf, 0, 80);
				if (nbytes == -1 && in != null) {
					ret = false;
					break;
				}
				totalbytes += nbytes;
				opening += new String(buf).toLowerCase();
			}
			in.close();

			if (ret && !opening.trim().startsWith(mbox_seperator)) {
				if (opening.indexOf('\n') > 0)
					opening = opening.substring(0, opening.indexOf('\n') - 1);
				System.out.println("opening: '" + opening + "'");
				//System.out.println("topfrom: " + p_topfrom);
				if (p_topfrom.matcher(opening).matches())
					ret = true;
				else
					ret = false;
			}

		} catch (IOException i) {
			return true;
		}
		//	System.out.println("done here" + ret);
		return (ret);
	}

/**
 * 	Checks if file is a directory and not a regular file 
 * 
 * @param file to check
 * @returntrue if it is a directory
 */
	private final boolean isDirectory(String file) {
		return new File(file).isDirectory();
	}
	
	
	/**
	 * Check if legal, that is not temp emacs file or zipped
	 * 	
	 * @param filein
	 * @return
	 */
	private final boolean legalFile(String filein) {
		String tempf = filein.trim().toLowerCase();
		//check if bad file like .zip .gz or ends with ~
		if (tempf.endsWith(".tar") || tempf.endsWith(".zip") || tempf.endsWith(".gz") || tempf.endsWith(".bz2")|| tempf.endsWith(".bz")|| tempf.endsWith("~")
				|| tempf.startsWith(".")) {
			return false;

		}
		//chop down to see if it starts with .
		int n = tempf.lastIndexOf(SEPERATORDIR);
		if (n > 0 && n + 1 < tempf.length()) {
			if (tempf.charAt(n + 1) == '.') {
				return false;
			}
		}
		//i guess its ok
		return true;
	}

	/*
	 * private final String Cleanup(String small) { small =
	 * small.replaceAll("\\\\","\\\\\\\\"); small =
	 * small.replaceAll("'","\\\\'"); small = small.replaceAll("\"","\\\\\"");
	 * small = small.replaceAll(",","\\\\,"); return small; }
	 *  
	 */
}//end class


//thrown out code for referecne


/*
 * else if (o instanceof InputStream) { //if(showmore)
 * //System.out.println("This is just an input stream");
 * //System.out.println("---------------------------"); InputStream is =
 * (InputStream)o; int c; StringBuffer small2 = new StringBuffer(256); byte
 * buf[] = new byte[256]; while ((c = is.read(buf)) != -1) small2.append(new
 * String(buf,0,c));
 * 
 * if(c>0) small2.append(new String(buf,0,c));
 * 
 * int nlen;
 *  /* n = p_base64.matcher(small); if(n.matches()) {
 * System.out.println("3!!!!!!!!!!!!!!!!!!GOt base64"); } /* small = new
 * String(); if(small2.length() >MAX_BODY){ nlen = small.length(); small =
 * small2.substring(0,MAX_BODY-1); } else { nlen = small2.length(); small =
 * small2.toString(); } small2 = null; //FILEZ small = Cleanup(small); //small =
 * small.replaceAll("\\\\",""); //small = small.replaceAll("\\\\","\\\\\\\\");
 * //??? //small = small.replaceAll("'","\\\\'"); //small =
 * small.replaceAll("`","\\\\`"); //small = small.replaceAll(",","\\\\,");
 * 
 * //TODO KWORDS HERE try{//2
 * 
 * temp = p.getContentType(); mid = temp.indexOf(';'); if(mid>0) temp =
 * temp.substring(0,mid).trim(); temp = temp.replaceAll("\n",""); temp =
 * temp.replaceAll("\t",""); temp = temp.replaceAll(","," "); temp =
 * temp.replaceAll("`"," "); //temp = temp.replaceAll(","," "); temp =
 * temp.replaceAll("'",""); temp = temp.replaceAll("\"","");
 * pstmt.setBinaryStream(5,p.getInputStream(),small.length());
 * pstmt.setString(1,filename2); pstmt.setString(2,s_mid);
 * pstmt.setString(3,md5sum(small)); pstmt.setString(4,rec_path);
 * pstmt.setInt(6,small.length()); pstmt.setString(7,temp);
 *  // Insert the data pstmt.executeUpdate();
 * 
 * //TODO CLEAN UP CONTENTTUYPE SO EVERYTHING AFTER ; is erased
 * //dbh.executeQuery("INSERT INTO message
 * (filename,mailref,hash,received,body,size,type) VALUES ('"+filename2+"','" +
 * s_mid + "','"+md5sum(small)+"','" + rec_path + "','" + small + "','" +
 * small.length() +"','"+temp+"')"); }catch(Exception sql1){
 * if(sql1.toString().indexOf("Duplicate entry")== -1)
 * System.err.println("Insert failed 2c: " + sql1);}
 * 
 *  }
 */

