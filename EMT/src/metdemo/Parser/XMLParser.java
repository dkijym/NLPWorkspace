package metdemo.Parser;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.BusyWindow;

public class XMLParser {

	/*
	enum DummyValues
	{
		DEFAULT_MAIL_LENGTH = 10,
		DEFAULT_S_TYPE,
		DEFAULT_S_STAT,
		DEFAULT_MAILREF,
		DEFAULT_FILENAME,
		DEFAULT_NUM_ATTACH
	}


	if (emailAddress.indexOf('@') > 0) {
	Matcher m = p_address1.matcher(emailAddress.replaceAll(".*<", ""));//p_address1.matcher(from);
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

	ps_emailinsert.setInt(1, DEFAULT_MAIL_LENGTH);
	ps_emailinsert.setString(2, s_msghash);
	ps_emailinsert.setString(5, s_fdom);
	ps_emailinsert.setString(6, s_fzn);
						//try{
	ps_emailinsert.setDate(7, new java.sql.Date(utime_dateheadertimestamp));//dateobj);//TODO
	ps_emailinsert.setInt(11, recount);
	ps_emailinsert.setString(12, "" + fcount);
	ps_emailinsert.setString(13, new String(s_type));

	ps_emailinsert.setString(14, s_stat);
	ps_emailinsert.setString(15, s_mid);
	ps_emailinsert.setString(16, filename);
	ps_emailinsert.setShort(17, (short) numcc);
	ps_emailinsert.setShort(18, (short) attnum);
	ps_emailinsert.setString(19, rcpt);
*/
}
