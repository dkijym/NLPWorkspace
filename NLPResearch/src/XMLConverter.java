import metdemo.DataBase.*;
import nlp.Mail.*;
import nlp.Mail.Thread;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.internet.MailDateFormat;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.*;
import javax.xml.transform.sax.*;
import javax.xml.xpath.*;
import javax.xml.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import AceJet.Ace;
import AceJet.AceDocument;
import AceJet.EDTtype;
import AceJet.EventTagger;
import AceJet.FindAceValues;
import AceJet.LearnRelations;
import AceJet.PerfectAce;
import Jet.Control;
import Jet.JetTest;
import Jet.Lex.Tokenizer;
import Jet.Pat.Pat;
import Jet.Refres.Resolve;
import Jet.Time.TimeAnnotator;
import Jet.Time.TimeMain;
import Jet.Tipster.*;

import javax.xml.bind.*;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLConverter.
 */
public class XMLConverter {

	/** Mail defaults */
	private static int DEFAULT_MAIL_LENGTH = 0;
	private static int DEFAULT_S_TYPE = 0;
	private static int DEFAULT_S_STAT = 0;
	private static int DEFAULT_MAILREF = 0;
	private static int DEFAULT_FILENAME = 0;
	private static int DEFAULT_NUM_ATTACH = 0;

	/** DB Variables */
	private final static String DB_LOCATION = "C:/Users/reuben/workspace/EMT";
	private final static String DB_NAME = "emt01";
	private final static String DB_USER = "user";
	private final static String DB_PASSWORD = "password";
	
	/** The dbh. */
	private EMTDatabaseConnection dbh;
	
	private final static String IMPORT_DIRECTORYPATH = "C:\\Users\\reuben\\NLPResearch\\ThreadedData\\raw-updated-2009";

	/**
	 * This initializes and connects the database.
	 */
	private void initDB() {
		dbh = null;
		try {

			dbh = DatabaseManager.LoadDB(DB_LOCATION, DB_NAME, DB_USER,
					DB_PASSWORD, "DERBY");
			if (dbh.connect(true) == false) {
				throw (new Exception());
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());

			System.err.println("Couldn't connect to database");
			System.out.println(e.getNextException().getMessage());
			System.exit(-3);
		} catch (DatabaseConnectionException e) {
			System.err.println(e.getMessage());
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-3);
		} catch (Exception a) {
			System.err.println(a.getMessage());
		}
	}

	/**
	 * This imports everything in the directory path into the EMT instance.
	 *
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public void importThreads(int threadsToGoThrough) throws JAXBException, FileNotFoundException {
		
		initDB();

		for (Thread t : new ThreadCollection(XMLConverter.IMPORT_DIRECTORYPATH, threadsToGoThrough)) {
			if (t != null && t.getMessage() != null)
				insertEmailsDatabase(t.getMessage(), t.getID());
			else
				System.out.println(" not found.");
		}
	}

	/**
	 * Insert email database.
	 *
	 * @param msgs the msgs
	 */
	public void insertEmailsDatabase(List<Message> msgs, String id) {
		for (int i = 0; i< msgs.size() ; i++) {
			for (To t : msgs.get(i).getTo()) {
				try {
					msgs.get(i).setId(id + i);
					insertEmailDatabase(msgs.get(i), t);
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Insert the message into the database.
	 *
	 * @param m the message to put in EMT database
	 * @param receiver the receiver
	 * @throws SQLException the sQL exception
	 */
	public void insertEmailDatabase(Message m, To receiver) throws SQLException {

		
		System.out.println(m.getSubject() + " to " + receiver.getAddress());

		// System.out.println("Hey");

		PreparedStatement ps_emailinsert;
		ps_emailinsert = dbh
				.prepareStatementHelper("INSERT INTO email (size,msghash,sender,sname,sdom,szn,dates,times,timeadj,subject,recnt,forward,type,flags,mailref,folder,numrcpt,numattach,rcpt,rname,rdom,rzn,insertTime,xmailer,utime,gyear,gmonth,ghour)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		ps_emailinsert.setEscapeProcessing(false);

		ps_emailinsert.setInt(1, DEFAULT_MAIL_LENGTH);
		
		ps_emailinsert.setString(2, m.getMD5Sum(receiver) );

		InternetAddress from = new InternetAddress(m.getFrom());

		ps_emailinsert.setString(3, from.address);

		ps_emailinsert.setString(4, from.name);
		ps_emailinsert.setString(5, from.domain);
		ps_emailinsert.setString(6, from.zone);

		// try{
		long utime_dateheadertimestamp = m.getDate();
		ps_emailinsert.setDate(7, new java.sql.Date(utime_dateheadertimestamp));

		Time timeobj = new Time(utime_dateheadertimestamp);
		ps_emailinsert.setTime(8, timeobj);

		ps_emailinsert.setLong(9, 0);

		String s_subject = m.getSubject();
		if (s_subject.length() > 254) {
			ps_emailinsert.setString(10, s_subject.substring(0, 250) + "...");
		} else {
			ps_emailinsert.setString(10, s_subject);
		}
		
		//TODO add all of these in
		int recount = 0;
		String fcount = "";
		String s_type = "";
		String s_stat = "";
		String s_mid = "";
		String filename = "";
		short numcc = 0;
		short attnum = 0;

		ps_emailinsert.setInt(11, recount);
		ps_emailinsert.setString(12, "" + fcount);
		ps_emailinsert.setString(13, new String(s_type));

		ps_emailinsert.setString(14, s_stat);
		ps_emailinsert.setString(15, s_mid);
		ps_emailinsert.setString(16, "");
		ps_emailinsert.setShort(17, (short) numcc);
		ps_emailinsert.setShort(18, (short) attnum);

		
		InternetAddress receiverAddr = new InternetAddress(
				receiver.getAddress());
		ps_emailinsert.setString(19, receiverAddr.address);
		if (receiverAddr.name.length() > 63) {
			ps_emailinsert.setString(20, receiverAddr.name.substring(0, 62)
					+ "..");
		} else {
			ps_emailinsert.setString(20, receiverAddr.name);
		}
		ps_emailinsert.setString(21, receiverAddr.domain);
		ps_emailinsert.setString(22, receiverAddr.zone);

		try {
			ps_emailinsert.setTimestamp(23, new Timestamp(
					utime_dateheadertimestamp));
		} catch (IllegalArgumentException il) {
		}

		String s_xmail = "None";
		ps_emailinsert.setString(24, s_xmail);
		// need to divide by 1000 since unix keeps number of seconds not milli
		ps_emailinsert.setLong(25, (utime_dateheadertimestamp));
		try {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(new Date(utime_dateheadertimestamp));// sqld.parse(s_date));
			ps_emailinsert.setInt(26, gc.get(Calendar.YEAR));// 1900+sqld.parse(s_date).getYear());//dateobj.getYear());
			ps_emailinsert.setInt(27, gc.get(Calendar.MONTH));// 1+sqld.parse(s_date).getMonth());//dateobj.getMonth());
			ps_emailinsert.setInt(28, gc.get(Calendar.HOUR_OF_DAY));// 1+sqld.parse(s_date).getHours());//dateobj.getHours());
		} catch (Exception ps) {
			System.out.println("EXCEPTION SETTING DATA\n" + from.address);
			// backup
			java.sql.Date dateobj = new java.sql.Date(utime_dateheadertimestamp);
			String datestring = dateobj.toString();
			try {
				ps_emailinsert.setInt(26,
						Integer.parseInt(datestring.substring(0, 4)));
				ps_emailinsert.setInt(27,
						Integer.parseInt(datestring.substring(5, 7)));
				ps_emailinsert.setInt(28,
						Integer.parseInt(datestring.substring(8)));

			} catch (Exception s) {
				System.out.println(s);
			}
		}
		dbh.doPrepared(ps_emailinsert);

	}

	/**
	 * Adds the message.
	 * TODO Add the message
	 * @param m the m
	 */
	public void addMessage(Message m) {
		// TODO Add in the add message code
		/*
		 * private PreparedStatement ps_message; ps_message =
		 * dbh.prepareStatementHelper(
		 * "INSERT INTO message (filename,mailref,hash,received,body,size,type) VALUES(?,?,?,?,?,?,?)"
		 * ); ps_message.setEscapeProcessing(false);
		 * 
		 * ps_message.setString(1, filename2); ps_message.setString(2, s_mid);
		 * ps_message.setString(3, md5sum(small.getBytes()));
		 * ps_message.setString(4, rec_path); ps_message.setBinaryStream(5, new
		 * ByteArrayInputStream(small.getBytes()), small.length());
		 * ps_message.setInt(6, truelength); ps_message.setString(7,
		 * temp.toLowerCase()); //ps_message.executeUpdate();
		 * dbh.doPrepared(ps_message);
		 */
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			new XMLConverter().importThreads(35000);
			// new XMLConverter().insertEmailDatabase((Message)null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	
}
