/**
  This Class is used to compute the average communication time between a given
  user and all other users for the SHDetector Class

  @author: Ryan Rowe, 07/06/06
*/

package metdemo.Tools;

import java.util.*;

import java.sql.SQLException;

import metdemo.dataStructures.CliqueEdge;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.dataStructures.singleMessage;
import metdemo.dataStructures.sparseIntMatrix;
import metdemo.dataStructures.vipUser;
import metdemo.Tools.HistogramCalculator;
import metdemo.winGui;


public class SHAvgCommTime{
	
    transient private EMTDatabaseConnection m_jdbcView; //for view only
    transient private HistogramCalculator hc;
    transient private winGui m_winGui;
	private HashMap<String,HashMap> graph;
	private sparseIntMatrix dgraph; //trying to cut memory in half by using 2 bytes in stead of 4, would like unsigned short, so need to use char
	private ArrayList<String> emailList;
	private double timeMean, orderMean, timeStDev, orderStDev;
	private int curSent, curRcvd;
	private HashMap<String,vipUser> vipList;
	
    //constructor, connect to the db
    public SHAvgCommTime(EMTDatabaseConnection jdbcView, winGui winGui){
    	//initialize various components
    	if(jdbcView==null){
			throw new NullPointerException();
		}
		m_jdbcView = jdbcView;
		m_winGui = winGui;
		
		//create the graph for clique finding purposes
		graph = new HashMap<String,HashMap>();
		
		//create index for the dgraph (directed graph) for PageRank purposes
		//String[] allUsers = m_winGui.getUserList();

		//initialize the dgraph (directed graph) for PageRank purposes
		dgraph = null;
    }
    
    
    /*
	 *	Method for initializing the important member variables at just the right time	
	 */
    public void init(String[] allUsers){
		//int size = allUsers.length;
		emailList = convertToArrayList(allUsers);
    	vipList = new HashMap<String,vipUser>();
    	//shlomo should logically be part of init
    	if(dgraph == null){
			dgraph = new sparseIntMatrix(emailList.size());//new char[emailList.size()][emailList.size()];
		}
    }

    
    /**
     *	Method for computing the average communication time	between one user and all the rest	
	 *
     * @param user
     * @throws SQLException
     */
    public void avgUserTime(final String user) throws SQLException{
    	
    	//-- FIRST, RETRIEVE ALL EMAILS RELATED TO THIS USER, AND ORDER THEM BY TIME ------
    	String[][] data1, data2, data;
    	String query1, query2;
    	int totalNumSent, totalNumRcvd, totalNumEmails;
		if(dgraph == null){
			dgraph = new sparseIntMatrix(emailList.size());//new char[emailList.size()][emailList.size()];
		}
		int userIndex = emailList.indexOf(user);
    	synchronized(m_jdbcView){
    		
			query1 = "SELECT sender, rcpt, utime, uid FROM email WHERE sender = '" + user + "'";
			query2 = "SELECT sender, rcpt, utime, uid FROM email WHERE rcpt = '" + user  + "'";
			
			data1 = m_jdbcView.getSQLData(query1); //get all emails where user = sender
			data2 = m_jdbcView.getSQLData(query2); //get all emails where user = rcpt
			
			totalNumSent = data1.length; //save the total number of emails the user has sent
			totalNumRcvd = data2.length; //save the total number of emails the user has received
    	}

    	data = removeDuplicates(data1,data2); //remove all duplicate entries from the union of the two results
    	totalNumEmails = data.length; //save the total number of emails altogether  
    	
    	
    	// SECOND, ORGANIZE THE EMAILS BY INDIVIDUAL ACCOUNT ------------------------------
    	HashMap<String, Vector> accounts = new HashMap<String,Vector>();
    	HashMap<String,Integer> allWords = new HashMap<String,Integer>();
    	HashMap<String,Integer> sendCount = new HashMap<String,Integer>();
    	Vector<String> emailIndex = new Vector<String>();
    	boolean send;
    	
    	for(int i=0;i<totalNumEmails;i++){
    		String sender = data[i][0];
    		String rcpt = data[i][1];
    		String time = data[i][2];
    		String uid = data[i][3];

    		if(sender.equalsIgnoreCase(rcpt)){ //ignore if the email is from the account to itself
    			continue;
    		}
    		
    		String keyUser;
    		singleMessage email;
    		if(sender.equalsIgnoreCase(user)){ //if current keyUser account is recipient, message sent by "user"
    			keyUser = rcpt;
    			email = new singleMessage(time, singleMessage.SEND);
    			send = true;
    		} else { //if current keyUser account is sender, message received by "user"
    			keyUser = sender;
    			email = new singleMessage(time, singleMessage.RECEIVE);
    			send = false;
    		}    		
    		if(accounts.containsKey(keyUser)){ // if the account is already in the HashMap, add the email
    			Vector vec = (Vector)accounts.get(keyUser);
    			vec.addElement(email);
	    		accounts.put(keyUser, vec);
	    		if(send){
	    			sendCount.put(keyUser,sendCount.get(keyUser)+1); //add one to the current number sent
	    		}
    		} else { // if the account is not yet in the HashMap, add it and the email
    			Vector vec = new Vector();
    			vec.addElement(email);
    			accounts.put(keyUser, vec);
    			if(send){
    				sendCount.put(keyUser,1); //initiate the number sent with 1
    			} else {
    				sendCount.put(keyUser,0); //initiate the number sent with 0
    			}
    		}
      	}
    	
    	
    	//-- THIRD, COMPUTE THE AVERAGE RESPONSE TIME CALCULATE RESPONSE ORDER VALUES ------
    	int totalNum = 0;
    	long totalTime = 0; //initiate average response time value for this user
    	double avgResponseTime = -1;
    	
    	Iterator iter = accounts.keySet().iterator();
    	while(iter.hasNext()){
    		//declare the account name and the vector of emails with its properties
    		String acct = (String)iter.next();
    		Vector vec = (Vector)accounts.get(acct);
    		int size = vec.size();
			int numSent = sendCount.get(acct);
			int numRcvd = size - numSent;
			
    		//accumulate the total number of responses and the related response time
    		HashMap<Integer,Long> pair = calcResponseTimes(vec,numSent,numRcvd); //tabulate the responses (number and time)
    		Iterator pairIter = pair.keySet().iterator(); //extract the first hash object
    		int curTotNum = (Integer)pairIter.next(); //get the total number of responses
    		long curTotTime = pair.get(curTotNum); //get the total response time for all those responses
    		totalNum += curTotNum; //add this total number to the overall total number for the user
    		totalTime += curTotTime; //add this total time tot he overall total time for the user
    		
    		//update the undirected graph and the directed graph for clique and pagerank analysis
    		updateEdge(graph,allWords,user, acct, "", 1, size); //update the main graph
    		if(emailList.contains(acct)){
    			int acctIndex = emailList.indexOf(acct);
    			//dgraph[userIndex][acctIndex] = (char)numSent; //update the dgraph (sent)
    			//dgraph[acctIndex][userIndex] = (char)numRcvd; //update the dgraph (rcvd)
    			dgraph.put(userIndex,acctIndex,numSent); //update the dgraph (sent)
    			dgraph.put(acctIndex,userIndex,numRcvd); //update the dgraph (rcvd)
    		} else {
    			updateEdge(graph,allWords,acct, user, "", 1, size);
    		}
    	}
    	
    	if(totalNum != 0){
    		avgResponseTime = totalTime/(totalNum*60*1000); //compute the avg response time (in minutes)
    	}
    	
    	vipUser newVIP = new vipUser(user,totalNumEmails,totalNum,avgResponseTime);
    	vipList.put(user,newVIP);
    }
    
    
    /*
	 *	Method for calcualating the response times to the current user 	
	 */
    public HashMap<Integer,Long> calcResponseTimes(Vector<singleMessage> vec, int numSent, int numRcvd){
    	int totNum = 0;
    	long totTime = 0;
    	if(numSent != 0 && numRcvd != 0){ //vector does not contain both sends and receives (i.e. no responses)
	    	int size = vec.size();
	    	int i = 0;
	    	while(i<size){
				singleMessage email = (singleMessage)vec.elementAt(i);
				if(email.type == singleMessage.RECEIVE){ //if user is the RCPT, continue
					i++;
					continue;
				} 
				int start = i; //start of SEND email
				int end = i; //end of SEND email (first RECEIVE email)
				i++;
				boolean foundIt = false;
				while(i < size && !foundIt){
					email = (singleMessage)vec.elementAt(i);
					if(email.type == singleMessage.SEND){
						i++;
						continue;
					}
					end = i; //first RECEIVE email since start of this SEND email set
					foundIt = true;
				}
				
				//compute the time between each SEND email and the first RECEIVE email
				if(foundIt){
					long timeReceived = ((singleMessage)vec.elementAt(end)).datestamp;
					for(int j=start;j<end;j++){
						long  timeSent = ((singleMessage)vec.elementAt(j)).datestamp;
						long responseTime = timeReceived - timeSent; //compute the response time
						long replyWindow = getReplyWindow(timeSent); //get reply window for the day sent
						if(responseTime < replyWindow){ //if the response time was within the reply window
							totTime += responseTime; //add the response time to the total time
							totNum += 1; //add one response to the total number of responses
						}
					}
				}
	    	}
		}
    	
    	HashMap<Integer,Long> pair = new HashMap<Integer,Long>(); //create the HashMap to return
    	pair.put(totNum,totTime); //create the object containing the current total num and total time
    	return pair; //return the HashMap
    }
    
    
    /**
	 *	Method for calcualating the response time and response order for each user
	 *	FUTURE WORK HERE
	 */
    /*public double calcResponseOrder(Vector vec){
    	if(vec.size() <= 1){ //only one email (so no real communication)
			double orderValue = 0;
			return orderValue;
		}
    	
    	System.out.println("Vector Size: "+vec.size());
    	
    	//get the index of the first SEND email
    	int i=0;
    	singleMessage msg = (singleMessage)vec.elementAt(i);
    	while(i<vec.size() && msg.type == singleMessage.RECEIVE){
    		msg = (singleMessage)vec.elementAt(i++);
    	}
    	int lastSendIndex = msg.index;
    	//System.out.println("Last Send Index: "+lastSendIndex);
        	
    	//compute response times, one message at a time
		int size = vec.size();	
		Vector responseTimes = new Vector(); //create a vector of response times with this user
		Vector responseOrders = new Vector(); //create a vector of response orders with this user
		for(i=0;i<size;i++){
			singleMessage email = (singleMessage)vec.elementAt(i);
			if(email.type == singleMessage.SEND){ //if user is the SENDER, set the send index and continue to next
				lastSendIndex = email.index;
				//System.out.println(i+": SENDMAIL with send index: "+lastSendIndex);
				continue;
			} else { //if user is the RCPT, look through following emails one by one for next SEND mail
				int j=i;
				singleMessage curEmail = (singleMessage)vec.elementAt(j);
				long msgTime = curEmail.datestamp;
				long replyWindow = getReplyWindow(msgTime);
				System.out.println("Reply Window: "+replyWindow);
				while(j<size-1 && curEmail.type == singleMessage.RECEIVE){
					System.out.println(j+": RECVMAIL");
					curEmail = (singleMessage)vec.elementAt(++j);
				}
				System.out.println(j+": SENDMAIL with send index: "+lastSendIndex);
				long timeDiff = curEmail.datestamp - msgTime; //when found, get the time difference between them
				System.out.println("Time Diff: "+timeDiff);
				if(j<size && timeDiff<replyWindow){ //check if the "response" occured within the "reply window" (72hrs)
					responseTimes.add(timeDiff); //if so, add that response time to the time vector
					responseOrders.add(curEmail.index - lastSendIndex); //and add the index diff to the order vector
					System.out.println("Good Data: index diff = "+(curEmail.index - lastSendIndex));
				} else {
					//never replied/next send was too far away
				}
			}
	    	//==== CODE TO PAUSE EXECUTION ====//
	    	try {
	    		System.in.read();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    	//==== ======================= ====//
		}
		
		//now compute the average values with both the time and the order vectors
		long totalTime = 0;
		double totalOrder = 0;
		for(i=0;i<responseTimes.size();i++){ //sum the total of each
			totalTime += (Long)responseTimes.get(i);
			totalOrder += (Integer)responseOrders.get(i)*1.0;
		}
		double avgTime, avgOrder;
		if(i != 0){
			avgTime = totalTime/i; //compute the average response time (mean)
			avgOrder = totalOrder/i; //compute the average response order (mean)
		} else {
			avgTime = 0;
			avgOrder = 0;
		}
		double[] both = {avgTime,avgOrder};
		return both; //return the pair of averages
    }*/
    
    
    /**
	 *	Method for calcualating the response time and response order for each user
	 *	OUTDATED: ABANDONED THIS APPROACH, TOO DELICATE AND ERROR PRONE
	 */
    /*public double[] calcTimeAndOrder(Vector vec){
    	if(vec.size() <= 1){ //only one email (so no real communication)
			double[] both = {0,0};
			return both;
		}
    	
    	System.out.println("Vector Size: "+vec.size());
    	
    	//get the index of the first SEND email
    	int i=0;
    	singleMessage msg = (singleMessage)vec.elementAt(i);
    	while(i<vec.size() && msg.type == singleMessage.RECEIVE){
    		msg = (singleMessage)vec.elementAt(i++);
    	}
    	int lastSendIndex = 1;
    	//System.out.println("Last Send Index: "+lastSendIndex);
        	
    	//compute response times, one message at a time
		int size = vec.size();	
		Vector responseTimes = new Vector(); //create a vector of response times with this user
		Vector responseOrders = new Vector(); //create a vector of response orders with this user
		for(i=0;i<size;i++){
			singleMessage email = (singleMessage)vec.elementAt(i);
			if(email.type == singleMessage.SEND){ //if user is the SENDER, set the send index and continue to next
				lastSendIndex = 1;
				//System.out.println(i+": SENDMAIL with send index: "+lastSendIndex);
				continue;
			} else { //if user is the RCPT, look through following emails one by one for next SEND mail
				int j=i;
				singleMessage curEmail = (singleMessage)vec.elementAt(j);
				long msgTime = curEmail.datestamp;
				long replyWindow = getReplyWindow(msgTime);
				System.out.println("Reply Window: "+replyWindow);
				while(j<size-1 && curEmail.type == singleMessage.RECEIVE){
					System.out.println(j+": RECVMAIL");
					curEmail = (singleMessage)vec.elementAt(++j);
				}
				System.out.println(j+": SENDMAIL with send index: "+lastSendIndex);
				long timeDiff = curEmail.datestamp - msgTime; //when found, get the time difference between them
				System.out.println("Time Diff: "+timeDiff);
				if(j<size && timeDiff<replyWindow){ //check if the "response" occured within the "reply window" (72hrs)
					responseTimes.add(timeDiff); //if so, add that response time to the time vector
					responseOrders.add(2); //and add the index diff to the order vector
					System.out.println("Good Data: index diff = "+(2));
				} else {
					//never replied/next send was too far away
				}
			}
	    	//==== CODE TO PAUSE EXECUTION ====//
	    	try {
	    		System.in.read();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    	//==== ======================= ====//
		}
		
		//now compute the average values with both the time and the order vectors
		long totalTime = 0;
		double totalOrder = 0;
		for(i=0;i<responseTimes.size();i++){ //sum the total of each
			totalTime += (Long)responseTimes.get(i);
			totalOrder += (Integer)responseOrders.get(i)*1.0;
		}
		double avgTime, avgOrder;
		if(i != 0){
			avgTime = totalTime/i; //compute the average response time (mean)
			avgOrder = totalOrder/i; //compute the average response order (mean)
		} else {
			avgTime = 0;
			avgOrder = 0;
		}
		double[] both = {avgTime,avgOrder};
		return both; //return the pair of averages
    }*/
    
    
    /*
	 *	Method for computing the size of the reply window depending on the day
	 */
    public long getReplyWindow(long utime){
		Calendar dateObj = Calendar.getInstance();
		dateObj.setTimeInMillis(utime);
		int day = dateObj.get(Calendar.DAY_OF_WEEK);
		int modifier = 3;
		if(day > 3 && day < 7){
			modifier = 5;	
		} else if (day == 7){
			modifier = 4;
		}
		long replyWindow = modifier*24*60*60*1000; //number of millis in the reply window
		return replyWindow;
    }
        
    
    /*
	 *	Method for computing the number of standard deviations away from the mean for the average response values
	 */
    public double[] calcResponseValues(double[] bothIn){
    	double timeValue = (timeMean - bothIn[0])/timeStDev; //the # of standard deviations away from the mean
    	double orderValue = (bothIn[1] - orderMean)/orderStDev; //the # of standard deviations away from the mean
    	double[] bothOut = {timeValue, orderValue};
    	return bothOut;
    }
    
    
    /*	
     *	Method for returning the vipList of all accounts with #emails and avg response time
     */
    public HashMap<String,vipUser> getVipList(){
    	return vipList;
    }
    
    public vipUser[] getVipListArray(){
    	int size = vipList.keySet().size();
		vipUser[] arr = new vipUser[size];
		Iterator iter = vipList.values().iterator();
		int i = 0;
		while(iter.hasNext()){
			arr[i] = (vipUser)iter.next();
			i++;
		}
		return arr;		
    }
    
    
	/** TODO: Probably want to do this in this class: Need to create a HashMap vipList 
	 * of all email accounts containting the number of emails, response time AND response 
	 * order. There are matrices for each, including the DGraph for numbers of emails... 
	 * using these three matrices try and compute each of the three above mentioned 'columns' 
	 * for each vipUser in the vipList. Return a viphm for use in SHDetector. THIS WILL REQUIRE 
	 * MODIFICATION OF MIN AND MAX FUNCTOIN!
	 */
    
    
    /** OUTDATED **
	 *	Method for averaging the time contained in a vector of emails
	 */
	private double avgTime(Vector vec){
		
		if(vec.size() <= 1){ //only one email (so no real communication)
			return Double.MAX_VALUE;
		}
		
		long totalTime = 0;
		int totalNum = 0;
		int size = vec.size();
		int i = 0;
		
		while(i < size){
			singleMessage email = (singleMessage)vec.elementAt(i);
			if(email.type == singleMessage.SEND){ //ignore if user is sender
				i++;
				continue;
			}			
			int start = i; //start of RECEIVE email
			int end = -1;
			i++;
			boolean foundIt = false;
			while(i < size && !foundIt){
				email = (singleMessage)vec.elementAt(i);
				if(email.type == singleMessage.RECEIVE){
					i++;
					continue;
				}
				end = i; //first SEND email since start of this RECEIVE email set
				foundIt = true;
			}
			
			//compute the time between each RECIEVE email and the first SEND email
			if(foundIt){
				totalNum += end - start;
				long endTime = ((singleMessage)vec.elementAt(end)).datestamp;
				for(int j=start;j<end;j++){
					long  tt = ((singleMessage)vec.elementAt(j)).datestamp;
					totalTime += endTime - tt;
				}
			}
		}
		
		if(totalNum == 0){
			return Double.MAX_VALUE;
		}
		
		double avg = totalTime/(double)(totalNum*1000*60); //how many minutes
		return avg;
	}
    
 	
 	/*
 	 * 	Method for removing duplicate entries after querying the database twice (for self cc emails)
 	 */
 	public String[][] removeDuplicates(String[][] data1, String[][] data2){
 		HashMap<String,String[]> datahm = new HashMap<String, String[]>();
 		
 		//add emails from data1 to the HashMap
 		for(String[] row : data1){
 			datahm.put(row[3],row);
 		}
 		
 		//add emails from data2 to the HashMap (if they aren't duplicates)
 		for(String[] row: data2){
 			if(!datahm.containsKey(row[3])){
 				datahm.put(row[3],row);
 			}
 		}
 		
 		//create the unique String[][] array to output
 		int numUnique = datahm.size();
 		String[][] output = new String[numUnique][4];
 		
 		//convert the HashMap back to a String[][] array
 		Iterator iter = datahm.keySet().iterator();
 		int i = 0;
 		while(iter.hasNext()){
 			String uid = (String)iter.next();
 			String[] temp = datahm.get(uid);
 			output[i][0] = temp[0];
 			output[i][1] = temp[1];
 			output[i][2] = temp[2];
 			output[i][3] = temp[3];
 			i++;
 		}
 		
 		//sort the unique String[][] array by utime
 		Comparator<String[]> comp = new SHComparator();
 		Arrays.sort(output,comp);
 		
 		//return the unique String[][] array
 		return output;
 	}
 	
 
 	/*
 	 * 	Class for comparing result set entries (for sorting purposes)
 	 */
 	class SHComparator implements Comparator<String[]> {

 	    // Comparator interface requires defining compare method.
 	    public int compare(String[] a, String[] b) {
 	    	Long aVal = Long.parseLong(a[2]);
 	    	Long bVal = Long.parseLong(b[2]);
 	    	if(aVal < bVal){
 	    		return -1;
 	    	} else if(aVal > bVal){
 	    		return 1;
 	    	} else {
 	    		return 0;
 	    	}
 	    }
 	}
 	

 	/*
 	 * 	Method for returning the graph object to SHDetector class
 	 */
 	public HashMap<String,HashMap> getGraph(){
 		return graph;
 	}
 	
 	
 	/*
 	 * 	Method for returning the dgraph object to SHDetector class
 	 */
 	public sparseIntMatrix getDGraph(){
 		return dgraph;
 	} 	
 	 	
 	/*
 	 * 	Method for setting the dgraph object to SHDetector class
 	 */
 	public void setDGraph(sparseIntMatrix d){
 		dgraph = d;
 	} 	
 	/*
 	 * 	Method for constructing a graph object for finding cliques (taken from cliqueFinder Class)
 	 */
	private void updateEdge(HashMap<String,HashMap> graph, HashMap<String,Integer> allWords,final String point1, final String point2, final String subject, final int minSubjectWordLength, final int weight){

		// add this point to the graph is it isn't already there
		HashMap edges;
		if((edges =  graph.get(point1)) == null){
			edges = new HashMap();
			graph.put(point1, edges);
 	    }

 	    // add/update this edge 	    
 	    CliqueEdge edge;
 	    if((edge = (CliqueEdge)edges.get(point2)) == null){
 	    	edge = new CliqueEdge();
 	    	edges.put(point2, edge);
 	    }
 	    edge.updateWeight(weight);
	}
	
	
 	/*
 	 * 	Method for converting a String[] array to an ArrayList<String>
 	 */
	private ArrayList<String> convertToArrayList(String[] arr){
		ArrayList<String> al = new ArrayList();
		int size = arr.length;
		for(int i=0;i<size;i++){
			al.add(i,arr[i]);
		}
		return al;
	}
 	
	
	/*
  	 *	Method for adjusting the time of emails according to this user's usage histogram
  	 */
 	/*private Vector adjustTime(Vector vec, Vector histogram){
  		
  		int size=vec.size();
  		Calendar c = Calendar.getInstance();
  		
  		for(int i=0;i<size;i++){
  			singleMessage email = (singleMessage)vec.elementAt(i);
  			c.setTimeInMillis(email.datestamp);
  			int hour = c.get(Calendar.HOUR_OF_DAY);
  			double v = ((Double)histogram.elementAt(hour)).doubleValue();
  			
  			if(v > 0.02){ //ignore if this email is sent in usual work time
  				continue;
  			}
  			
  			int pos = hour;
  			int iter = 0;

  			while(v <= 0.02 && iter < 23){  				
  				pos++; 
  				pos = pos%24;
  				iter++;
  				v = ((Double)histogram.elementAt(pos)).doubleValue();
  			}
  			
  			int gap = pos - hour;
  			c.roll(Calendar.HOUR_OF_DAY, gap);
  			email.datestamp = c.getTimeInMillis();
  			vec.setElementAt(email, i);
  		}
  		
  		return vec;
  	}*/
	
  	
  	/*
  	 *	Method for adjusting the insertTime of every user according to the nearest next send email
  	 */
  	/*private String[][] processAllTime(String[][] array, String user){
  		
  		int row = array.length;
  		if(row<=0)
  			return array;  		
  		
  		int i=0;
  		while(i<row){
  			
  			String sender = array[i][0];
  			String rcpt = array[i][1];  			
  			
  			if(sender.equalsIgnoreCase(rcpt)){
  				i++;
  				continue;	
  			}
  			
  			if(sender.equals(user)){
  				i++;
  				continue;
  			}
  			
  			int start = i; //start of RECEIVE email
  			int end=-1;
  			//System.out.print("start: "+start+";");  			
  			i++;		
  			boolean notfound = true;
  			while(i<row && notfound){
  				sender = array[i][0];
  				rcpt = array[i][1];
  				if(rcpt.equals(user)){
  					i++;
  					continue;
  				}
  				end=i; //first reply(SEND) email to previous received emails
  				notfound =false;
  			}
  			
  			//compute the time between start and end(the first response)
  			if(!notfound){
  				
  				String adjustTime = array[end][2];
  				for(int j=start;j<end;j++){
  					array[j][2]= adjustTime; //update the insertTime of received email to next Send email
  				}
  				
  				//for test
  				//System.out.println("adjust from "+start+" to "+end+" with time:"+adjustTime);
  				
  			}
  		}
  		
  		return array;
  	}*/
 	
	//goes up in AllUserTime
	// -- OTHER POTENTIAL ALGORITHMS
	/*//TESTING FOR HISTOGRAM STUFF
	double[] histogramVector;
	UserHistogram userH;
	userShortProfile usp = m_winGui.getUserInfo(acct);
	//System.out.println(acct+": "+usp.getName());
		histogramVector = hc.getHistogram(acct,m_winGui.getUserInfo(acct));
		userH = new UserHistogram(histogramVector, acct);
		System.out.println(acct);
		for(int i=0;i<histogramVector.length;i++){
			System.out.println(histogramVector[i]);
		}
		System.out.print("\n");*/
  	
  	
  	
}






