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


package metdemo.Tools;


import java.io.*;
import java.util.*;


public class Aliaser
{
  private String m_defaultDomain;
  private Vector m_ignoreMachineNames;
  private HashSet m_neverAliasUsers;
  private HashSet m_neverAliasDomains;
  private Hashtable m_domainMap;
  private Hashtable m_userMap;

  boolean setup = false;

  /**
   * @param filename file containing aliasing directives
   */
  public Aliaser(String filename) throws Exception
  {
    m_defaultDomain = "";
    m_ignoreMachineNames = new Vector();
    m_neverAliasUsers = new HashSet();
    m_neverAliasDomains = new HashSet();
    m_domainMap = new Hashtable();
    m_userMap = new Hashtable();

    BufferedReader in = null;
    try{
    in = new BufferedReader(new FileReader(filename));
    }catch(IOException each)
	{
    	System.out.println(each);
    	return;
	}
    
    String line;
    while ((line = in.readLine()) != null)
    {
      if (line.startsWith("#"))
      {
	// comment
	continue;
      }

      StringTokenizer tok = new StringTokenizer(line, " ");
      while (tok.hasMoreTokens())
      {
	String cur = tok.nextToken();
	if (cur.equalsIgnoreCase("default-domain"))
	{
	  if (tok.hasMoreTokens())
	  {
	    m_defaultDomain = tok.nextToken().toLowerCase();
	  }
	}
	else if (cur.equalsIgnoreCase("ignore-machine-name"))
	{
	  if (tok.hasMoreTokens())
	  {
	    m_ignoreMachineNames.add(tok.nextToken().toLowerCase());
	  }
	}
	else if (cur.equalsIgnoreCase("never-alias"))
	{
	  if (tok.hasMoreTokens())
	  {
	    cur = tok.nextToken();
	    if (cur.equalsIgnoreCase("user"))
	    {
	      if (tok.hasMoreTokens())
	      {
		m_neverAliasUsers.add(tok.nextToken().toLowerCase());
	      }
	    }
	    else if (cur.equalsIgnoreCase("domain"))
	    {
	      if (tok.hasMoreTokens())
	      {
		m_neverAliasDomains.add(tok.nextToken().toLowerCase());
	      }
	    }
	  }
	}
	else if (cur.equalsIgnoreCase("domain"))
	{
	  if (tok.hasMoreTokens())
	  {
	    String key = tok.nextToken().toLowerCase();
	    if (tok.hasMoreTokens())
	    {
	      m_domainMap.put(key, tok.nextToken().toLowerCase());
	    }
	  }
	}
	else if (cur.equalsIgnoreCase("user"))
	{
	  if (tok.hasMoreTokens())
	  {
	    String key = tok.nextToken().toLowerCase();
	    if (tok.hasMoreTokens())
	    {
	      m_userMap.put(key, tok.nextToken().toLowerCase());
	    }
	  }
	}
      }
    }
    setup = true;
  }


/**
 * If failed to open resolve file will return the same thing
 * @param email
 * @return
 */
  public String resolve(String email)
  {
  	
  	if(!this.setup)
  		return email;
  	
  	
    String user = "";
    String domain = "";
    String newEmail = email;

    // parse into user and domain
    StringTokenizer tok = new StringTokenizer(email, "@");
    if (tok.hasMoreTokens())
    {
      user = tok.nextToken();
      if (tok.hasMoreTokens())
      {
	domain = tok.nextToken();
      }
    }
    
    Enumeration enumWalker = m_ignoreMachineNames.elements();
    while (enumWalker.hasMoreElements())
    {
      String tld = (String)enumWalker.nextElement();
      if (domain.endsWith(tld))
      {
	domain = tld;
	newEmail = user + "@" + domain;
      }
    }

    if (domain.equals(""))
    {
      domain = m_defaultDomain;
      newEmail = user + "@" + domain;
    }

    if (m_neverAliasUsers.contains(user) || m_neverAliasDomains.contains(domain))
    {
      return newEmail;
    }

    String newDomain;
    while ((newDomain = (String) m_domainMap.get(domain)) != null)
    {
      domain = newDomain;
      newEmail = user + "@" + domain;
    }

    String tmpEmail;
    while ((tmpEmail = (String) m_userMap.get(newEmail)) != null)
    {
      newEmail = tmpEmail;
    }

    // note: if you add anything here, be sure to update user & domain 
    // in the tmpEmail business above
    return newEmail;
  }



  public String toString()
  {
    Iterator it;

    String str = "";

    str += "Default Domain: " + m_defaultDomain + "\n\n";

    str += "Ignore Machine Names For These Domains:\n";
    it = m_ignoreMachineNames.iterator();
    while (it.hasNext())
    {
      str += "  " + it.next() + "\n";
    }
    str += "\n";

    str += "Never Alias These Domains:\n";
    it = m_neverAliasDomains.iterator();
    while (it.hasNext())
    {
      str += "  " + it.next() + "\n";
    }
    str += "\n";

    str += "Never Alias These Users:\n";
    it = m_neverAliasUsers.iterator();
    while (it.hasNext())
    {
      str += "  " + it.next() + "\n";
    }
    str += "\n";

    str += "Domain Mappings:\n";
    it = m_domainMap.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry mapping = (Map.Entry) it.next();
      str += "  " + (String) mapping.getKey() + "=>" + (String) mapping.getValue() + "\n";
    }
    str += "\n";

    str += "User Mappings:\n";
    it = m_userMap.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry mapping = (Map.Entry) it.next();
      str += "  " + (String) mapping.getKey() + "=>" + (String) mapping.getValue() + "\n";
    }
    
    return str;
  }
}
