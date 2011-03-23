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

/**
 * Major revision June 2006, Shlomo Hershkop
 * 1 changes in global variables
 * 2 changes in data structures
 * 3 changes in efficiency
 */

package metdemo.CliqueTools;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.Aliaser;
import metdemo.dataStructures.CliqueEdge;

/**
 * Implementation of clique finding algorithm using the branch 
 * and bound method  described in "Finding All Cliques 
 * of an Undirected Graph" by Bron & Kerbosch (1971)
 */

public class CliqueFinder implements Serializable
{
//private HashMap<String,HashMap> m_graph;
  //private HashMap<String,Integer> m_allWords;
  //private ArrayList m_cliques;
  //private HashMap<String,HashSet> m_users; // index of users->cliques
 
  
  private transient EMTDatabaseConnection m_jdbc;
  private transient Aliaser m_aliaser;
 
  //private boolean m_printAsFound;
  //private Stack<AccountPoint> m_compsub;
  
  private HashMap<String,String> stoplistMap = new HashMap<String,String>();

  public CliqueFinder(EMTDatabaseConnection jdbc, String aliasFilename) throws Exception
  {
    this(jdbc,aliasFilename,null);
  }

    public CliqueFinder(EMTDatabaseConnection jdbc, String aliasFilename,String stoplistFilename) throws Exception
  {
    m_jdbc = jdbc;
    m_aliaser = new Aliaser(aliasFilename);
   // m_cliques = new ArrayList();
   // m_users = new HashMap<String,HashSet>();
   // m_allWords = new HashMap<String,Integer>();
    //m_printAsFound = false;
    
    if(stoplistFilename!=null)
    {
    	setupStoplist(stoplistFilename);
    }
  }

    private final void setupStoplist(String filename) {
		stoplistMap = new HashMap<String,String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			System.out.println(filename);
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				stoplistMap.put(line, "");
			}
		} catch (Exception e) {
			System.out.println("read stoplist error\n" + e);
		}
	}

  public final HashMap<String,HashMap> buildGraph(final int minEdgeWeight, int minSubjectWordLength) throws Exception
  {
	  return buildGraph(minEdgeWeight,minSubjectWordLength,"select sender, rcpt, subject from email");
    /*
     m_graph = new HashMap();
    String [][]data  = m_jdbc.getSQLData("select sender, rcpt, subject from email");

    if ((data.length) > 0 && (data[0].length != 3))
    {
      throw new Exception("database returned wrong number of columns\n");
    }

    for (int i=0; i<data.length; i++)
    {
      String sender = m_aliaser.resolve(data[i][0].toLowerCase());
      String rcpt = m_aliaser.resolve(data[i][1].toLowerCase());
      String subject = data[i][2].toLowerCase();

      if (minSubjectWordLength < 1)
      {
	minSubjectWordLength = 1;
      }

      if (!sender.equals(rcpt))
      {
	updateEdge(sender, rcpt, subject, minSubjectWordLength);
	// only record subject lines for senders to avoid duplication
	updateEdge(rcpt, sender, "", minSubjectWordLength);
      }
    }

    // prune edges that don't meet the minimum threshold
    Set pointSet = m_graph.entrySet();
    Map.Entry[] aPoints = (Map.Entry[]) pointSet.toArray(new Map.Entry[0]);
    for (int i=0; i<aPoints.length; i++)
    {
      Map.Entry point = aPoints[i];
      HashMap edges = (HashMap) point.getValue();
      Set edgeSet = edges.entrySet();
      Map.Entry[] aEdges = (Map.Entry[]) edgeSet.toArray(new Map.Entry[0]);
      for (int j=0; j<aEdges.length; j++)
      {
	Map.Entry edge = aEdges[j];
	int weight = ((Edge) edge.getValue()).getWeight();
	if (weight < minEdgeWeight)
	{
	  edgeSet.remove(edge);
	}
      }
      if (edgeSet.isEmpty())
      {
	// if no edges, this point can't be in a clique
	pointSet.remove(point);
      }
    }
     */
  }

    public final HashMap<String,HashMap> buildGraph(final int minEdgeWeight, int minSubjectWordLength, final String query) throws Exception
    {

    	HashMap<String,HashMap> m_graph = new HashMap<String,HashMap>();
    	HashMap<String,Integer> m_allWords = new HashMap<String,Integer>();
	
	
	String [][]data = m_jdbc.getSQLData(query);
	if ((data.length) > 0 && (data[0].length != 3))
	{
		throw new Exception("database returned wrong number of columns in build graph\n");
	}
	    
	for (int i=0; i<data.length; i++)
	    {
		String sender = m_aliaser.resolve(data[i][0].toLowerCase());
		String rcpt = m_aliaser.resolve(data[i][1].toLowerCase());
		String subject = data[i][2].toLowerCase();

		if (minSubjectWordLength < 1)
		    {
			minSubjectWordLength = 1;
		    }

		if (!sender.equals(rcpt))
		    {
			updateEdge(m_graph,m_allWords,sender, rcpt, subject, minSubjectWordLength);
			// only record subject lines for senders to avoid duplication
			updateEdge(m_graph,m_allWords,rcpt, sender, "", minSubjectWordLength);
		    }
	    }

	// prune edges that don't meet the minimum threshold
	Set pointSet = m_graph.entrySet();
	Map.Entry[] aPoints = (Map.Entry[]) pointSet.toArray(new Map.Entry[0]);
	for (int i=0; i<aPoints.length; i++)
	    {
		Map.Entry point = aPoints[i];
		HashMap edges = (HashMap) point.getValue();
		Set edgeSet = edges.entrySet();
		Map.Entry[] aEdges = (Map.Entry[]) edgeSet.toArray(new Map.Entry[0]);
		for (int j=0; j<aEdges.length; j++)
		    {
			Map.Entry edge = aEdges[j];
			int weight = ((CliqueEdge) edge.getValue()).getWeight();
			if (weight < minEdgeWeight)
			    {
				edgeSet.remove(edge);
			    }
		    }
		if (edgeSet.isEmpty())
		    {
			// if no edges, this point can't be in a clique
			pointSet.remove(point);
		    }
	    }
	
	
	
	
	return m_graph;
	
    }

  private void updateEdge(HashMap<String,HashMap> m_graph,HashMap<String,Integer> m_allWords,final String point1, final String point2, final String subject, final int minSubjectWordLength)
  {
    // add this point to the graph is it isn't already there
	  HashMap edges;
    if ((edges =  m_graph.get(point1)) == null)
    {
      edges = new HashMap();
      m_graph.put(point1, edges);
    }

    /** TODO: working here now. */
    // add/update this edge
    
    CliqueEdge edge;
    if ((edge = (CliqueEdge) edges.get(point2)) == null)
    {
      edge = new CliqueEdge();
      edges.put(point2, edge);
    }
    edge.update(m_allWords,subject, minSubjectWordLength);
  }


/**
 * 
 * @param printAsFound
 * @return
 * @throws Exception
 */
  public final ArrayList<oneClique> findCliques(HashMap<String,HashMap> m_graph, final boolean printAsFound) //throws Exception
  {
    //m_printAsFound = printAsFound;
	ArrayList<oneClique> m_cliques = new ArrayList<oneClique>();
	  //m_cliques.clear();
    HashMap<String,HashSet<oneClique>> m_users = new HashMap<String,HashSet<oneClique>>();
    Stack<AccountPoint> m_compsub = new Stack<AccountPoint>();

    HashSet candidates = new HashSet();
    Iterator it = m_graph.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry point = (Map.Entry) it.next();
      candidates.add(new AccountPoint((String) point.getKey(), (HashMap) point.getValue()));
    }

    extendClique(new HashSet(), candidates,printAsFound,m_users,m_compsub,m_cliques);

    return  m_cliques;//getCliques();
  }



  /**
   * @param ne The set "not" of explicitly excluded points
   * @param ce The set "candidates" for addition to the clique
   */
  private void extendClique(HashSet not, HashSet candidates, final boolean m_printAsFound,HashMap<String,HashSet<oneClique>> m_users, Stack<AccountPoint> m_compsub ,ArrayList<oneClique> m_cliques) //throws Exception
  {
    
	  
	//  HashMap<String,HashSet> m_users = new HashMap<String,HashSet>();  
	int nod = 0;				// number of disconnections
    int minNod = candidates.size();
    AccountPoint fixedPoint = null;
    AccountPoint candidate = null;
    Iterator itPosFixedPoints;

    if (candidates.isEmpty())
    {
      if (not.isEmpty())
      {
	// clique found
	//addClique();
    	    oneClique clique = new oneClique(m_compsub);
    	    m_cliques.add(clique);

    	    if (m_printAsFound)
    	    {
    	      // print clique
    	      System.out.println(clique.toString(true));
    	    }

    	    //updateIndex(clique);
    	    //private void updateIndex(oneClique clique)
    	    {
    	      Iterator enumWalker = clique.getPoints().iterator();
    	      while (enumWalker.hasNext())
    	      {
    	        AccountPoint point = (AccountPoint) enumWalker.next();
    	        String user = point.getAccount();

    	        HashSet<oneClique> cliques = m_users.get(user);
    	        
    	        if (cliques == null)
    	        {
    	          cliques = new HashSet<oneClique>();
    	          m_users.put(user, cliques);
    	        }
    	        
    	        cliques.add(clique);
    	      }
    	    }
    	  
    	  
      }
      return;
    }

    // select fixed point with fewest disconnections
    itPosFixedPoints = not.iterator();
    while (itPosFixedPoints.hasNext())
    {
      int count = 0;
      AccountPoint possibleCandidate = null;

      AccountPoint point = (AccountPoint) itPosFixedPoints.next();
      
      Iterator itCandidates = candidates.iterator();
      while (itCandidates.hasNext())
      {
	AccountPoint point2 = (AccountPoint) itCandidates.next();
	if (!point.isConnected(point2))
	{
	  count++;
	  possibleCandidate = point2;
	}
      }

      if (count <= minNod)
      {
	fixedPoint = point;
	minNod = count;
	candidate = possibleCandidate;
      }
    }

    itPosFixedPoints = candidates.iterator();
    while (itPosFixedPoints.hasNext())
    {
      int count = 0;

      AccountPoint point = (AccountPoint) itPosFixedPoints.next();
      
      Iterator itCandidates = candidates.iterator();
      while (itCandidates.hasNext())
      {
	AccountPoint point2 = (AccountPoint) itCandidates.next();
	if (!point.isConnected(point2))
	{
	  count++;
	}
      }

      if (count <= minNod)
      {
	fixedPoint = point;
	minNod = count;
	candidate = point;
      }
    }

    nod = minNod;
    
    while ((nod > 0) && (!candidates.isEmpty()))
    {
      // select candidate (if not preselected)
      if (candidate == null)
      {
	candidate = selectCandidate(candidates, fixedPoint);
      }

      // add selected candidate to compsub
      m_compsub.push(candidate);
      
      // create new sets "candidates" and "not" from the old sets by 
      // removing all points not connected to the selected candidate, 
      // keeping old sets intact.
      HashSet newNot = new HashSet();
      Iterator it = not.iterator();
      while (it.hasNext())
      {
	AccountPoint p = (AccountPoint) it.next();
	if (p.isConnected(candidate))
	{
	  newNot.add(p);
	}
      }
      
      HashSet<AccountPoint> newCandidates = new HashSet<AccountPoint>();
      it = candidates.iterator();
      while (it.hasNext())
      {
	AccountPoint p = (AccountPoint) it.next();
	if (p.isConnected(candidate))
	{
	  newCandidates.add(p);
	}
      }

      // recursively call extend to operate on sets just formed
      extendClique(newNot, newCandidates,m_printAsFound,m_users,m_compsub,m_cliques);

      // remove candidate from compsub and move it from "candidates" to "not"
      m_compsub.pop();
      candidates.remove(candidate);
      not.add(candidate);

      // decrement counter of disconnections
      nod--;

      // no pre-selected candidate now
      candidate = null;
    }
    
    
    return;
  }



  private AccountPoint selectCandidate(HashSet candidates, AccountPoint refPoint) //throws Exception
  {
    Iterator it = candidates.iterator();
    while (it.hasNext())
    {
      AccountPoint possibleCandidate = (AccountPoint) it.next();
      if (!refPoint.isConnected(possibleCandidate))
      {
    	  return possibleCandidate;
      }
    }
    
    // we should never get here because the number of disconnects > 0
    return refPoint;
    //throw new Exception("Unexpectedly, no candidates found in select candidates!");
  }



  public final void printGraph(HashMap<String,HashMap> m_graph)
  {
    Iterator itPoints = m_graph.entrySet().iterator();
    while (itPoints.hasNext())
    {
      Map.Entry point = (Map.Entry) itPoints.next();
      System.out.println("\n" + point.getKey() + ":");
      HashMap edges = (HashMap) point.getValue();
      Iterator itEdges = edges.entrySet().iterator();
      while (itEdges.hasNext())
      {
	Map.Entry edge = (Map.Entry) itEdges.next();
	System.out.println("  "  + edge.getKey() + "\t\t" + ((CliqueEdge) edge.getValue()).getWeight());
      }
    }
  }



  /*public final ArrayList getCliques()
  {
    return m_cliques;
  }*/

    /*
      For the method findCliques. 
      Since everyone outside this class cannot use Point and Edge.
      findCliques returns a ArrayList that everyone cannot use it...

      Class oneClique uses a private global variable from it's parent,
      which is CliqueFinder.

      Example:
      Common Subject Words: fwd, com, drmath, you, coming
      sh553@columbia.edu
      yhershko@ymail.yu.edu
    */
    /*
      return ArrayList type: CliqueWithCommonWords
    */
    public final ArrayList<CliqueWithCommonWords> getReadableCliques(ArrayList<oneClique> m_cliques){
	ArrayList<CliqueWithCommonWords> toReturn = new ArrayList<CliqueWithCommonWords>();

	if(m_cliques==null)
			return null;
	for(int i=0;i<m_cliques.size();i++){
	    CliqueWithCommonWords oneclique = new CliqueWithCommonWords();

	    String str = m_cliques.get(i).toString();
	    String[] seg = str.split("\n");

	    //common words
	    String[] seg2 = seg[0].split(":");
	    String str2 = seg2[1];
	    String[] commonwords = str2.split(",");
	    for(int j=0;j<commonwords.length;j++){
		oneclique.addCommonWord(commonwords[j].trim());
	    }
	    
	    //users
	    for(int j=1;j<seg.length;j++){
		oneclique.addUser(seg[j]);
	    }

	    toReturn.add(oneclique);
	}

	return toReturn;
    }


  public final ArrayList<String> findCliqueViolations(final HashMap<String,HashSet> m_users) throws Exception
  {
    ArrayList<String> violations = new ArrayList<String>();
    String [][]data = m_jdbc.getSQLData("select sender, rcpt, subject, inserttime from email");
   
    if ((data.length > 0) && (data[0].length != 4))
    {
      throw new Exception("Database returned wrong number of columns");
    }

    for (int i=0; i<data.length; i++)
    {
      String sender = m_aliaser.resolve(data[i][0].toLowerCase());
      String rcpt = m_aliaser.resolve(data[i][1].toLowerCase());
      if (isCliqueViolation(m_users,sender, rcpt))
      {
	violations.add(data[i][3] + " " + sender + "==>" + rcpt + " (\"" + data[i][2] + "\")");
      }
    }

    return violations;
  }



  /**
   * Naive implementation of clique violation. (Good enough for gov't work...)
   *
   * @param from message sender
   * @param to message recipient
   * @return <i>true</i> if message constitutes a clique violation
   * @return <i>false</i> otherwise
   */
  public final boolean isCliqueViolation(final HashMap<String,HashSet> m_users,String from, String to)
  {
    Set cliques = (Set) m_users.get(from);

    if (cliques == null)
    {
      // sender has no cliques
      return true;
    }

    Iterator it = cliques.iterator();
    while (it.hasNext())
    {
      oneClique clique = (oneClique) it.next();
	
      if (clique.contains(to))
      {
	return false;
      }
    }

    return true;
  }



  /**
   * add the set in compub to cliques
   */  
  /*private void addClique()
  {
    oneClique clique = new oneClique(m_compsub);
    m_cliques.add(clique);

    if (m_printAsFound)
    {
      // print clique
      System.out.println(clique.toString(true));
    }

    updateIndex(clique);
  }*/



  /**
   * update the index with new clique
   */
  /*private void updateIndex(oneClique clique)
  {
    Iterator enumWalker = clique.getPoints().iterator();
    while (enumWalker.hasNext())
    {
      Point point = (Point) enumWalker.next();
      String user = point.getAccount();

      Set cliques = (Set) m_users.get(user);
      
      if (cliques == null)
      {
        cliques = new HashSet();
	m_users.put(user, cliques);
      }
      
      cliques.add(clique);
    }
  }*/



  public class AccountPoint implements Serializable
  {
    private String m_account;
    private HashMap m_edges;

    public AccountPoint(String account, HashMap edges)
    {
      m_account = account;
      m_edges = edges;
    }
    
    public final String getAccount() { return m_account; }
    public final HashMap getEdges() { return m_edges; }
    public final boolean equals(String account) { return getAccount().equals(account); }
    
    public final void setAccount(String account) { m_account = account; }
    public final void setEdges(HashMap edges) { m_edges = edges; }

    public final boolean isConnected(AccountPoint otherAccount)
    {
      return m_edges.containsKey(otherAccount.getAccount());
    }
  }


  public class oneClique implements Serializable
  {
    private ArrayList m_points;			// accounts in this clique
    private ArrayList m_words;			// most common words in subject lines for this clique
    private int m_rcs;					// raw clique score for this clique (added by Ryan, 7/5/2006)
    private int m_wcs;					// weighted clique score for this clique (added by Ryan, 7/5/2006)

    public oneClique(Stack points)
    {
    	
      m_points = new ArrayList();
      
      Iterator cloner = points.iterator();
    	while(cloner.hasNext()){
    		m_points.add(cloner.next());
    	}
    	
//      m_points = (Stack) points.clone();

      // calculate most common words in subject lines
    	HashMap allWords = new HashMap();
      Iterator enumWalker = m_points.iterator();//elements();
      while (enumWalker.hasNext())
      {
	AccountPoint point = (AccountPoint) enumWalker.next();
	HashMap edges = point.getEdges();
	Iterator itEdges = edges.entrySet().iterator();
	while (itEdges.hasNext())
	{
	  Map.Entry entry = (Map.Entry) itEdges.next();
	  String otherPointName = (String) entry.getKey();
	  CliqueEdge edge = (CliqueEdge) entry.getValue();

	  // add the words from this edge only if other point is in the clique
	  int i = 0;
	  while (i<points.size() && !((AccountPoint) points.get(i)).getAccount().equals(otherPointName))
	  {
	    i++;
	  }
	  
	  if (i != points.size())
	  {
	    Iterator itWords = edge.getSubjectWords().entrySet().iterator();
	    while (itWords.hasNext())
	    {
	      Map.Entry word = (Map.Entry) itWords.next();
	      Integer frequency;
	      if ((frequency = (Integer) allWords.get(word.getKey())) == null)
	      {
		frequency = (Integer) word.getValue();
	      }
	      else
	      {
		frequency = new Integer(frequency.intValue() + ((Integer) word.getValue()).intValue());
	      }
	      allWords.put(word.getKey(), frequency);
	    }
	  }
	}
      }

      // we have all the words and their frequencies for this clique. now sort.
      Map.Entry[] aAllWords = (Map.Entry[]) allWords.entrySet().toArray(new Map.Entry[0]);
      Arrays.sort(aAllWords, new WordEntryComparator());
      m_words = new ArrayList();
      for (int i=0; i<aAllWords.length; i++)
      {
    	  	m_words.add(aAllWords[i].getKey());
      }
    }



    public final String toString(final boolean printSubjectWords)
    {
      String str = "";

      if (printSubjectWords)
      {
	str += "Common Subject Words: ";
	for (int i=0; i<5 && i<getSubjectWords().size(); i++)
	{
	  if (i != 0)
	  {
	    str += ", ";
	  }
	  
	  str += getSubjectWords().get(i);
	}
	str += "\n";
      }

      Iterator enumWalker = getPoints().iterator();
      while (enumWalker.hasNext())
      {
	AccountPoint p = (AccountPoint) enumWalker.next();
	str += p.getAccount() + "\n";
      }

      return str;
    }
    
    



    public String toString()
    {
      return toString(true);
    }


    public final ArrayList getPoints() { return m_points; }
    public final ArrayList getSubjectWords() { return m_words; }
    public final int getRCS(){ return m_rcs; }		//
    public final int getWCS(){ return m_wcs; }    	// get and set functions for rcs 
    public final void setRCS(int n){ m_rcs = n; }	// and wcs member variables
    public final void setWCS(int n){ m_wcs = n; }	//

    public final boolean contains(String s)
    {
      Iterator enumWalker = getPoints().iterator();
      while (enumWalker.hasNext())
      {
	if (((AccountPoint) enumWalker.next()).equals(s))
	{
	  return true;
	}
      }

      return false;
    }



    // sorts in reverse order
    private class WordEntryComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	int freq1 = ((Integer) ((Map.Entry)o1).getValue()).intValue();
	int freq2 = ((Integer) ((Map.Entry)o2).getValue()).intValue();
	//???int glblFreq1 = ((Integer) m_allWords.get(((Map.Entry)o1).getKey())).intValue();
	//???int glblFreq2 = ((Integer) m_allWords.get(((Map.Entry)o2).getKey())).intValue();
	
	//???double score1 = (freq1/(double)glblFreq1);
	//???double score2 = (freq2/(double)glblFreq2);

	if (freq2 < freq1)
	{
	  return -1;
	}
	else if (freq2 > freq1)
	{
	  return 1;
	}
	else
	{
	  return 0;
	}
      }
    }
  }
  



  /*public static void main(String args[])
  {
    try
    {
      if (args.length != 2)
      {
	System.out.println("usage: java CliqueFinder aliasFilename minMessages");
	System.exit(1);
      }
      
      int minMessages = Integer.parseInt(args[1]);

      String host = "win";
      String database = "met1";
      String driver = "org.gjt.mm.mysql.Driver";
      String user = "outsider";
      String password = "password";

      EMTDatabaseConnection jdbc = DatabaseManager.LoadDB(host , database, user, password,DatabaseManager.sDERBY);
      if (jdbc.connect() == false)
      {
	throw new Exception("connect failed");
      }

      CliqueFinder cliqueFinder = new CliqueFinder(jdbc, args[0]);
      cliqueFinder.buildGraph(minMessages, 3);
      //cliqueFinder.printGraph();
      cliqueFinder.findCliques(true);

      System.out.println("\n=====Violations=====");
      ArrayList violations = cliqueFinder.findCliqueViolations();
      Enumeration enumWalker = violations.elements();
      while (enumWalker.hasMoreElements())
      {
	System.out.println((String) enumWalker.nextElement());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }
  }*/
}
