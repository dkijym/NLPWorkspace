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


package metdemo.Attach;


//import java.io.*;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import java.lang.Boolean;

import metdemo.AlertTools.Alert;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.DataBase.EMTDatabaseConnection;

/** 
 *Class to do individual attachment
 *
 */

public class Attachment 
{
    public static final int MALICIOUS_RED = 0;
    public static final int MALICIOUS_YELLOW = 1;
    public static final int BENIGN = 100;

    
    String m_signature;
    String m_filename;
    String m_mimeType;
    long m_firstSeen;
    long m_lastSeen;
    double m_overallBirthratePerMin, m_overallBirthratePerHour, m_overallBirthratePerDay;
    double m_internalBirthratePerMin, m_internalBirthratePerHour, m_internalBirthratePerDay;
    double m_lifespan;
    double m_internalSpeed;
    double m_overallSpeed;
    double m_internalSaturation;
    double m_overallSaturation;
    double m_size;
    boolean m_hasExternalOrigin;
    int m_classification;
    int m_group;
    //TickListener m_tickListener;

    ReportForensicWindow m_alerts;
    AttachmentAlertOptions m_alertOptions;

    
    public Attachment(ReportForensicWindow alerts, AttachmentAlertOptions alertOptions)//, TickListener tickListener)
    {

	m_alerts = alerts;
	m_alertOptions = alertOptions;
	//m_tickListener = tickListener;

    }




    public Attachment(String sig, String filename, String mimeType, ReportForensicWindow alerts, AttachmentAlertOptions alertOptions)//, TickListener tickListener)
    {
	m_signature = sig;
	if ((mimeType != null) && (mimeType.length()>0))
	    {
		m_mimeType = mimeType;
	    }
	else
	    {
		m_mimeType = "unknown";
	    }
	if ((filename != null) && (filename.length()>0))
	    {
		m_filename = filename;
	    }
	else
	    {
		m_filename = "no filename";
	    }
	m_overallBirthratePerMin = 0;
	m_overallBirthratePerHour = 0;
	m_overallBirthratePerDay = 0;
	m_internalBirthratePerMin = 0;
	m_internalBirthratePerDay = 0;
	m_internalBirthratePerHour = 0;
	m_lifespan = 0;
	m_internalSpeed = 0;
	m_overallSpeed = 0;
	m_internalSaturation = 0;
	m_overallSaturation = 0;
	m_size = 0;
	m_hasExternalOrigin = false;
	m_classification = BENIGN;

	m_alerts = alerts;
	m_alertOptions = alertOptions;

	//m_tickListener = tickListener;
	m_group = 0;
    }

    public final String[] Save()
    {
	int i=0;
	String data[] = new String[20];
	data[i++] = new String(m_signature);
	data[i++] = new String(m_filename);
	data[i++] = new String(m_mimeType);
	data[i++] = new String(""+m_firstSeen);
	data[i++] = new String(""+m_lastSeen);
	data[i++] = new String(""+m_overallBirthratePerMin);
	data[i++] = new String(""+m_overallBirthratePerHour);
	data[i++] = new String(""+m_overallBirthratePerDay);
	data[i++] = new String(""+m_internalBirthratePerMin);
	data[i++] = new String(""+m_internalBirthratePerHour);
	data[i++] = new String(""+m_internalBirthratePerDay);
	data[i++] = new String(""+m_lifespan);
	data[i++] = new String(""+m_internalSpeed);
	data[i++] = new String(""+m_overallSpeed);
	data[i++] = new String(""+m_internalSaturation);
	data[i++] = new String(""+m_overallSaturation);
	data[i++] = new String(""+m_size);
	data[i++] = new String(""+m_hasExternalOrigin);
	data[i++] = new String(""+m_classification);
	data[i++] = new String(""+m_group);
	
	//  AttachmentAlertOptions m_alertOptions;
	
	return data;

    }  

    public final void Load(String data[])
    {
	int i=0;
	try{

	    m_signature = new String(data[i++]);
	    m_filename = new String(data[i++]);
	    m_mimeType = new String(data[i++]);
	    m_firstSeen = new Double(data[i++]).longValue();
	    m_lastSeen =  new Double(data[i++]).longValue();
	    m_overallBirthratePerMin = Double.parseDouble(data[i++]);
	    m_overallBirthratePerHour = Double.parseDouble(data[i++]);
	    m_overallBirthratePerDay = Double.parseDouble(data[i++]);
	    m_internalBirthratePerMin = Double.parseDouble(data[i++]);
	    m_internalBirthratePerHour = Double.parseDouble(data[i++]);
	    m_internalBirthratePerDay = Double.parseDouble(data[i++]);
	    m_lifespan = Double.parseDouble(data[i++]);;
	    m_internalSpeed = Double.parseDouble(data[i++]);
	    m_overallSpeed = Double.parseDouble(data[i++]);
	    m_internalSaturation = Double.parseDouble(data[i++]);;
	    m_overallSaturation = Double.parseDouble(data[i++]);
	    m_size = Double.parseDouble(data[i++]);
	    boolean m_hasExternalOrigin = new Boolean(data[i++]).booleanValue() ;
	    m_classification = Integer.parseInt(data[i++]);
	    m_group = Integer.parseInt(data[i++]);

	}catch(Exception e){System.out.println(i+"problems setting up attachment load.."+e);}
	//  TickListener m_tickListener;
	//Alerts m_alerts;
	//AttachmentAlertOptions m_alertOptions;
    }


    // calculate anything that will be AND'd (or might later be AND'd) first
    public final void calculateEasyMetrics(EMTDatabaseConnection jdbc) throws SQLException
    {
	//String query;
	//String data[][];
        if(getSize() ==0)
        {
            calcSize(jdbc);
        }
        calcOriginAndLifespan(jdbc);
    }


    public final void setSignature(String s)
    {
	m_signature = s;
    }

    public final String getSignature()
    {
	return m_signature;
    }

  
    public final void setGroup(int g)
    {
	m_group = g;
    }

    public final int getGroup()
    {
	return m_group;
    }

    public final void setFilename(String s)
    {
	m_filename = s;
    }

    public final String getFilename()
    {
	return m_filename;
    }

    public final void setMimeType(String s)
    {
	m_mimeType = s;
    }

    public final String getMimeType()
    {
	return m_mimeType;
    }

    public final void setClassification(int i)
    {
	m_classification = i;
    }

    public final int getClassification()
    {
	return m_classification;
    }

    public final String getClassificationAsString()
    {
	switch (m_classification)
	    {
	    case BENIGN:
		return "Benign";
	    case MALICIOUS_RED:
		return "Malicious (Red)";
	    case MALICIOUS_YELLOW:
		return "Mailicious (Yellow)";
	    default:
		return "Unknown";
	    }
    }

    public final void setOverallBirthratePerMin(double d)
    {
	m_overallBirthratePerMin = d;
    }

    public final double getOverallBirthratePerMin()
    {
	return m_overallBirthratePerMin;
    }

    public final void setOverallBirthratePerHour(double d)
    {
	m_overallBirthratePerHour = d;
    }

    public final double getOverallBirthratePerHour()
    {
	return m_overallBirthratePerHour;
    }

    public final void setOverallBirthratePerDay(double d)
    {
	m_overallBirthratePerDay = d;
    }

    public final double getOverallBirthratePerDay()
    {
	return m_overallBirthratePerDay;
    }

    public final void setInternalBirthratePerMin(double d)
    {
	m_internalBirthratePerMin = d;
    }

    public final double getInternalBirthratePerMin()
    {
	return m_internalBirthratePerMin;
    }

    public final void setInternalBirthratePerHour(double d)
    {
	m_internalBirthratePerHour = d;
    }

    public final double getInternalBirthratePerHour()
    {
	return m_internalBirthratePerHour;
    }

    public final void setInternalBirthratePerDay(double d)
    {
	m_internalBirthratePerDay = d;
    }

    public final double getInternalBirthratePerDay()
    {
	return m_internalBirthratePerDay;
    }

    public final void setLifespan(double d)
    {
	m_lifespan = d;
    }

    public final double getLifespan()
    {
	return m_lifespan;
    }

    public final void setInternalSpeed(double d)
    {
	m_internalSpeed = d;
    }

    public final double getInternalSpeed()
    {
	return m_internalSpeed;
    }

    public final void setOverallSpeed(double d)
    {
	m_overallSpeed = d;
    }

    public final double getOverallSpeed()
    {
	return m_overallSpeed;
    }

    public final void setInternalSaturation(double d)
    {
	m_internalSaturation = d;
    }

    public final double getInternalSaturation()
    {
	return m_internalSaturation;
    }

    public final void setOverallSaturation(double d)
    {
	m_overallSaturation = d;
    }

    public final double getOverallSaturation()
    {
	return m_overallSaturation;
    }

    public final void setSize(double d)
    {
	m_size = d;
    }
  


    public final double getSize()
    {
	return m_size;
    }

    public final void setOrigin(boolean isExternal)
    {
	m_hasExternalOrigin = isExternal;
    }

    public final boolean hasExternalOrigin()
    {
	return m_hasExternalOrigin;
    }

  
    public final String toString()
    {
	return (getFilename() + " (" + getSignature() + ")");
    }


    private void calcSize(EMTDatabaseConnection jdbc) throws SQLException
    {
	String data[][];

	synchronized(jdbc)
	    {
		//jdbc.getSqlData("select distinct size from message where hash='" + m_signature + "'");
		//data = jdbc.getRowData();
	    data = jdbc.getSQLData("select size from message where hash='" + m_signature + "'");
		
	    }

	if ((data.length < 1) || (data[0].length < 1))
	    {
		throw new SQLException("Could not get size of attachment: database returned no data");
	    }

	if (data[0][0].equals("null"))
	    {
		setSize(0);
	    }
	else
	    {
		try
		    {
			setSize(Long.parseLong(data[0][0]));
		    }
		catch (NumberFormatException ex)
		    {
			throw new SQLException("Could not get size of attachment: " + ex);      
		    }
	    }
    }
  
    private void calcOriginAndLifespan(EMTDatabaseConnection jdbc) throws SQLException
    {
	String data[][];

	// get origin, lifespan
//	query = "select utime, senderLoc, email.mailref, sender, hash ";
    String query = "select utime, senderLoc ";
    query += "from message left join email ";
	query += "on email.mailref=message.mailref ";
	query += "where hash='" + m_signature + "' ";
	query += "order by utime asc";
	synchronized (jdbc)
	    {//System.out.println(query);
		//jdbc.getSqlData(query);
		//data = jdbc.getRowData();
		data = jdbc.getSQLData(query);
	    }

	if ((data.length > 0) && (data[0].length > 0))
	    {
		setOrigin(data[0][1].equals("E"));
		try
		    {//System.out.println (data[0][0] + " and " +  data[(data.length)-1][0]);
			//this is really only once, but just n case on weird emails
			if(data[0][0].equals("null"))
			    data[0][0] = "0";
			m_firstSeen = Long.parseLong(data[0][0]);
			m_lastSeen = Long.parseLong(data[(data.length)-1][0]);
			setLifespan((m_lastSeen - m_firstSeen) / 60);
		    }
		catch (NumberFormatException ex)
		    {
			throw new SQLException("Couldn't parse message timestamp: ");
		    }
	    }

    }

    private boolean allAndConditionsMet(int level)
    {
	int index;

	if (Alert.ALERTLEVEL_YELLOW == level)
	    {
		index = 0;
	    }
	else
	    {
		index =1;
	    }

	if ((getLifespan() <= m_alertOptions.getLifespan()[index]) &&
	    (getSize() >= m_alertOptions.getAttSize()[index]))
	    {
		if ((!m_alertOptions.getMustBeExternal()[index] && !m_alertOptions.getMustBeInternal()[index]) ||
		    (m_alertOptions.getMustBeExternal()[index] && hasExternalOrigin()) || 
		    (m_alertOptions.getMustBeInternal()[index] && !hasExternalOrigin()))
		    {
			return true;
		    }
	    }

	return false;

    }


    public final void calcOverallSpeed(long incidentCount, boolean quellAlerts)
    {
	setOverallSpeed(incidentCount / (getLifespan() + 1));
	if (!quellAlerts)
	    {
		checkSpeed(getOverallSpeed(), m_alertOptions.getOvSpeed(), "overall", "Overall");
	    }
    }

    public final void calcInternalSpeed(long incidentCount, boolean quellAlerts)
    {
	setInternalSpeed(incidentCount / (getLifespan() + 1));
	if (!quellAlerts)
	    {
		checkSpeed(getInternalSpeed(), m_alertOptions.getInSpeed(), "internal", "Internal");
	    }
    }

    public final void checkSpeed(double speed, double[] speedThresh, String qualifierL, String qualifierU)
    {
	double threshYellow = speedThresh[0];
	double threshRed = speedThresh[1];

	if (speed >= Math.min(threshYellow, threshRed))
	    {
		int levelToExceed = -1;
		String strLevel = "";
		int level = -1;
		double threshold = -1;

		if ((speed >= threshRed) && allAndConditionsMet(Alert.ALERTLEVEL_RED))
		    {
			levelToExceed = MALICIOUS_RED;
			level = Alert.ALERTLEVEL_RED;
			strLevel = "Red";
			threshold = threshRed;
		    }
		else if (allAndConditionsMet(Alert.ALERTLEVEL_YELLOW))
		    {
			levelToExceed = MALICIOUS_YELLOW;
			level = Alert.ALERTLEVEL_YELLOW;
			strLevel = "Yellow";
			threshold = threshYellow;
		    }

		if ((levelToExceed > -1) && (getClassification() > levelToExceed))
		    {
			String brief = (new Date(1000*m_lastSeen)) + "\n";
			brief += "attachment " + getSignature() + " exceeded " + qualifierL + " speed limit";
			String detailed = brief + "\n\n" + qualifierU + " Speed: " + speed + "\n";
			detailed += qualifierU + " Speed Limit (" + strLevel + "): " + threshold + "\n";
			m_alerts.alert(level, brief, detailed);
			setClassification(levelToExceed);
		    }
	    }
    }

    public final void calcSaturation(EMTDatabaseConnection jdbc, long[] userCount, boolean quellAlerts) throws SQLException
    {
        if(m_signature==null){return;}
//	String query;
//	String data[][];
    HashMap<String,String> hashmapUsers = new HashMap<String,String>();
    HashMap<String,String> hashmapInternalUsers = new HashMap<String,String>();

	try
	    {
		//--query = "create temporary table AttachmentMetricsTemp (account varchar(255), location char)";
		//--synchronized (jdbc)
		    //--{
			//--			jdbc.updateSQLData(query);
		   //-- }
		synchronized (jdbc){
		PreparedStatement ps = jdbc.prepareStatementHelper("select distinct sender,senderLoc,rcpt,rcptLoc from message left join email on email.mailref=message.mailref where hash =?");
		ps.setString(1,m_signature);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String sender = rs.getString(1);
			String senderLoc = rs.getString(2);//char??
			
					
			String rcpt = rs.getString(3);
			String rcptLoc = rs.getString(4);
			hashmapUsers.put(sender,"");
			hashmapUsers.put(rcpt,"");
			
			if(senderLoc.equals("I"))
			{
				hashmapInternalUsers.put(sender,"");
			}
			if(rcptLoc.equals("I"))
			{
				hashmapInternalUsers.put(rcpt,"");
			}
			
			
		}
		rs.close();
		}
//--		query = "insert into AttachmentMetricsTemp ";
//--		query += "select distinct sender, senderLoc ";
//--		query += "from message left join email ";
//--		query += "on email.mailref=message.mailref ";
//--		query += "where hash=\"" + m_signature + "\"";
//--		synchronized (jdbc)
//--		    {
//--			jdbc.updateSQLData(query);
//--		    }
//--		query = "insert into AttachmentMetricsTemp ";
//--		query += "select distinct rcpt, rcptLoc ";
//--		query += "from message left join email ";
//--		query += "on email.mailref=message.mailref ";
//--		query += "where hash=\"" + m_signature + "\"";
//--		synchronized (jdbc)
//--		    {
//--			jdbc.updateSQLData(query);
//--		    }

		//--query = "select count(distinct account) from AttachmentMetricsTemp";
		//--synchronized (jdbc)
		    //--{
			//--data = jdbc.getSQLData(query);
			//jdbc.getSqlData(query);
			//data = jdbc.getRowData();
		    //--}

		long overallTouched = hashmapUsers.size();//--Long.parseLong(data[0][0]);
		setOverallSaturation(overallTouched / (double) userCount[0]);

//--		query = "select count(distinct account) from AttachmentMetricsTemp ";
//--		query += "where location=\"I\"";
	//--	synchronized (jdbc)
		//--    {
		///--	data = jdbc.getSQLData(query);
			//jdbc.getSqlData(query);
			//data = jdbc.getRowData();
		   //-- }
		long internalTouched = hashmapInternalUsers.size();//--Long.parseLong(data[0][0]);
		setInternalSaturation(internalTouched / (double) userCount[1]);

		if (!quellAlerts)
		    {
			checkSaturation(getInternalSaturation(), m_alertOptions.getInSaturation(), "internal", "Internal");
			checkSaturation(getOverallSaturation(), m_alertOptions.getOvSaturation(), "overall", "Overall");
		    }
	    }
	catch (NumberFormatException ex)
	    {
		throw new SQLException("Could not caluclate saturation: " + ex);
	    }
	//--finally
	 //--   {
	//--	synchronized (jdbc)
	//--	    {
	//--		jdbc.updateSQLData("drop table AttachmentMetricsTemp");
	//--	    }
	 //--   }
    }

    public final void checkSaturation(double saturation, double[] saturationThresh, String qualifierL, String qualifierU)
    {
	double threshYellow = saturationThresh[0];
	double threshRed = saturationThresh[1];

	if (saturation >= Math.min(threshYellow, threshRed))
	    {
		int levelToExceed = -1;
		String strLevel = "";
		int level = -1;
		double threshold = -1;

		if ((saturation >= threshRed) && allAndConditionsMet(Alert.ALERTLEVEL_RED))
		    {
			levelToExceed = MALICIOUS_RED;
			level = Alert.ALERTLEVEL_RED;
			strLevel = "Red";
			threshold = threshRed;
		    }
		else if (allAndConditionsMet(Alert.ALERTLEVEL_YELLOW))
		    {
			levelToExceed = MALICIOUS_YELLOW;
			level = Alert.ALERTLEVEL_YELLOW;
			strLevel = "Yellow";
			threshold = threshYellow;
		    }

		if ((levelToExceed > -1) && (getClassification() > levelToExceed))
		    {
			String brief = (new Date(1000*m_lastSeen)) + "\n";
			brief += "attachment " + getSignature() + " exceeded " + qualifierL + " saturation limit";
			String detailed = brief + "\n\n" + qualifierU + " Saturation: " + saturation + "\n";
			detailed += qualifierU + " Saturation Limit (" + strLevel + "): " + threshold + "\n";
			m_alerts.alert(level, brief, detailed);
			setClassification(levelToExceed);
		    }
	    }
    }

    public final void calcBirthrates(EMTDatabaseConnection jdbc, long nextUpdateUtime, long currentUtime, 
				     long ovMaxBirthrateMin, long ovMaxBirthrateHour, long ovMaxBirthrateDay,
				     long inMaxBirthrateMin, long inMaxBirthrateHour, long inMaxBirthrateDay,
				     boolean quellAlerts) throws SQLException
    {
	// for each second since the last update, ask how many births in the last min/hour/day
	setOverallBirthratePerMin(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						60, ovMaxBirthrateMin, 
						m_alertOptions.getOvBirthrateMin(), false, quellAlerts));
	setOverallBirthratePerHour(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						 60*60, ovMaxBirthrateHour,
						 m_alertOptions.getOvBirthrateHour(), false, quellAlerts));
	setOverallBirthratePerDay(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						60*60*24, ovMaxBirthrateDay,
						m_alertOptions.getOvBirthrateDay(), false, quellAlerts));
	setInternalBirthratePerMin(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						 60, inMaxBirthrateMin,
						 m_alertOptions.getInBirthrateMin(), true, quellAlerts));
	setInternalBirthratePerHour(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						  60*60, inMaxBirthrateHour,
						  m_alertOptions.getInBirthrateHour(), true, quellAlerts));
	setInternalBirthratePerDay(calcBirthrate(jdbc, nextUpdateUtime, currentUtime, 
						 60*60*24, inMaxBirthrateDay,
						 m_alertOptions.getInBirthrateDay(), true, quellAlerts));
    }

    private long calcBirthrate(EMTDatabaseConnection jdbc, long nextUpdateUtime, long currentUtime, 
			       int period, long maxBirthrate, long[] thresholds, 
			       boolean bInternalOnly, boolean quellAlerts) 
	throws SQLException
    {
	String query;
	String data[][] = null;
	int granularity = 1;

	long threshYellow;
	long threshRed;
	String strMetric;
	switch (period)
	    {
	    case 60:
		if (bInternalOnly)
		    {
			threshYellow = m_alertOptions.getInBirthrateMin()[0];
			threshRed = m_alertOptions.getInBirthrateMin()[1];
		    }
		else
		    {
			threshYellow = m_alertOptions.getOvBirthrateMin()[0];
			threshRed = m_alertOptions.getOvBirthrateMin()[1];
		    }
		strMetric = "Birthrate/Min";
		granularity = 60;
		break;
	    case 60*60:
		if (bInternalOnly)
		    {
			threshYellow = m_alertOptions.getInBirthrateHour()[0];
			threshRed = m_alertOptions.getInBirthrateHour()[1];
		    }
		else
		    {
			threshYellow = m_alertOptions.getOvBirthrateHour()[0];
			threshRed = m_alertOptions.getOvBirthrateHour()[1];
		    }
		strMetric = "Birthrate/Hour";
		granularity = 60*60;
		break;
	    case 60*60*24:
		if (bInternalOnly)
		    {
			threshYellow = m_alertOptions.getInBirthrateDay()[0];
			threshRed = m_alertOptions.getInBirthrateDay()[1];
		    }
		else
		    {
			threshYellow = m_alertOptions.getOvBirthrateDay()[0];
			threshRed = m_alertOptions.getOvBirthrateDay()[1];
		    }
		strMetric = "Birthrate/Day";
		granularity = 60*60*24;
		break;
	    default:
		throw new SQLException("Invalid period in calcBirthrate");
	    }

	if (maxBirthrate < threshYellow)
	    {
		// just update values for the most recent period for display purposes
		//m_tickListener.tick((currentUtime - nextUpdateUtime) / granularity);
		nextUpdateUtime = currentUtime;
	    }

	// only loop through lifetime of *this* attachment
	long loopStart = java.lang.Math.max(nextUpdateUtime, m_firstSeen);
	long loopEnd = java.lang.Math.min(currentUtime, m_lastSeen);

	// keep tickListener updated
	//m_tickListener.tick(((loopStart - nextUpdateUtime) + (m_lastSeen - loopEnd))/granularity);

	for (long i=loopStart; i<=loopEnd; i+=granularity)
	    {
		query = "select count(*) from message left join email on email.mailref=message.mailref ";
		query += "where hash='" + m_signature + "' ";
		query += "and (utime >= " + (i - period) + ") ";
		query += "and (utime < " + i + ") ";
		if (bInternalOnly)
		    {
			query += "and (rcptLoc='I') ";
		    }
      
		synchronized(jdbc)
		    {
			data = jdbc.getSQLData(query);
		    }
      
		if ((data.length != 1) || (data[0].length != 1))
		    {
			throw new SQLException("Database did not correctly return birthrate");
		    }

		try
		    {
			long birthrate = Long.parseLong(data[0][0]);

			if (!quellAlerts && (birthrate >= Math.min(threshYellow, threshRed)))
			    {
				long threshold = -1;
				String strLevel = "";
				int level = -1;
				int levelToExceed = -1;
				if ((birthrate >= threshRed) && allAndConditionsMet(Alert.ALERTLEVEL_RED))
				    {
					threshold = threshRed;
					strLevel = "Red";
					level = Alert.ALERTLEVEL_RED;
					levelToExceed = MALICIOUS_RED;
				    }
				else if (allAndConditionsMet(Alert.ALERTLEVEL_YELLOW))
				    {
					threshold = threshYellow;
					strLevel = "Yellow";
					level = Alert.ALERTLEVEL_YELLOW;
					levelToExceed = MALICIOUS_YELLOW;
				    }

				if ((levelToExceed > -1) && (getClassification() > levelToExceed))
				    {
					String brief = (new Date(i*1000)) + "\n";
					brief += "attachment " + getSignature() + " exceeded " + strMetric  + " limit";
					String detailed = brief + "\n\n" + strMetric + ": " + birthrate + "\n";
					detailed += strMetric + " Limit (" + strLevel + "): " + threshold + "\n";
					m_alerts.alert(level, brief, detailed);
					setClassification(levelToExceed);
				    }
			    }
		    }
		catch (NumberFormatException ex)
		    {
			throw new SQLException("Database returned malformed birthrate");
		    }

		//m_tickListener.tick();
	    }

	// return the count from the most recent period to be displayed
	try
	    {
		if ((data != null) && (data[0] != null) && (data[0][0] != null))
		    {
			return(Long.parseLong(data[0][0]));
		    }
		
			return(0);
		
	    }
	catch (NumberFormatException ex)
	    {
		throw new SQLException("Database returned malformed birthrate");
	    }
    }
  
    public final static String[][] calcPeriodBirthrateForAll(EMTDatabaseConnection jdbc, long startUtime, long endUtime,
							     boolean bInternalOnly) throws SQLException
    {
	String query;

	query = "select hash, count(email.mailref) from message left join email on email.mailref=message.mailref ";
	query += "and (utime >= " + startUtime + ") ";
	query += "and (utime < " + endUtime + ") ";
	if (bInternalOnly)
	    {
		query += "and (rcptLoc='I') ";
	    }
	query += "where hash!='\"\"' group by hash ";

	synchronized(jdbc)
	    {
		//jdbc.getSqlData(query);
		return(jdbc.getSQLData(query));//jdbc.getRowData());
	    }
    }

    public final static String[][] calcOverallIncidencesForAll(EMTDatabaseConnection jdbc) throws SQLException
    {
	String query;
//	String data[][];
    
	query = "select hash, count(*) ";
	query += "from message left join email on email.mailref=message.mailref ";
	query += "where hash!='\"\"' group by hash";
    
    synchronized(jdbc)
	    {
		//jdbc.getSqlData(query);
		return jdbc.getSQLData(query);//jdbc.getRowData();
	    }
    }

    public final static String[][] calcInternalIncidencesForAll(EMTDatabaseConnection jdbc) throws SQLException
    {
	String query;
//	String data[][];
    
	query = "select hash, count(*) ";
	query += "from message left join email on email.mailref=message.mailref ";
	query += "where rcptLoc='I' ";
	query += "and hash!='\"\"' group by hash";
	synchronized(jdbc)
	    {
	//	jdbc.getSqlData(query);
		return jdbc.getSQLData(query);//jdbc.getRowData();
	    }
    }


}

