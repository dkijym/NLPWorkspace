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




package metdemo.AlertTools;


import java.io.*;

/** wrapper class for alert event */

public class Alert implements Serializable
{
  public final static int ALERTLEVEL_RED = 0;
  public final static int ALERTLEVEL_YELLOW = 1;
  public final static int ALERTLEVEL_GREEN = 2;

  private int m_level;
  private String m_brief;                       // brief description of alert
  private String m_detailed;                    // detailed description of alert


  public Alert()
  {
    m_level = ALERTLEVEL_YELLOW;
    m_brief = "";
    m_detailed = "";
  }

  public Alert(int level, String brief, String detailed)
  {
    m_level = level;
    m_brief = brief;
    m_detailed = detailed;
  }

  public void setLevel(int level)
  {
    m_level = level;
  }

  public int getLevel()
  {
    return m_level;
  }

  public void setBrief(String brief)
  {
    m_brief = brief;
  }

  public String getBrief()
  {
    return m_brief;
  }

  public void setDetailed(String detailed)
  {
    m_detailed = detailed;
  }

  public String getDetailed()
  {
    return m_detailed;
  }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {

	    out.writeInt(getLevel());
	    out.writeObject(getBrief());
	    out.writeObject(getDetailed());
    }
 
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {

	setLevel(in.readInt());
	setBrief((String)in.readObject());
	setDetailed((String)in.readObject());

    }




}















