/*
  This Class is used to compute the average communication time between different users. This can be used to help identify the VIP, rank the importance of different accounts.

  Ke Wang, 06/17/03
*/


package metdemo.Tools;

//import metdemo.*;
import java.util.*;
//import java.io.*;
import java.sql.SQLException;

import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.dataStructures.singleMessage;
import metdemo.dataStructures.userComm;


public class CommunicateTime{

   
    private EMTDatabaseConnection m_jdbcView; //for view only
    
   // private HistogramCalculator HC;

    //constructor, connect to the db
    public CommunicateTime(EMTDatabaseConnection jdbcView){
	
		if(jdbcView==null)
		{
			throw new NullPointerException();
		}
			
		
		m_jdbcView = jdbcView;
		
		//HC = new HistogramCalculator(jdbcView);
    }

/*
	public CommunicateTime(){
		String dbHost = "localhost";
		String dbName = "met03";
		String user = "outsider";
		String password = "pleaze9enter";
			
		// connect to the database 
		try{
			m_jdbcUpdate = DatabaseManager.LoadDB(dbHost , dbName, user, password,DatabaseManager.sMYSQL);
			
			if (m_jdbcUpdate.connect() == false){
				throw(new Exception());
			}
	
			m_jdbcView = DatabaseManager.LoadDB(dbHost,dbName, user, password,DatabaseManager.sMYSQL);
				
			if (m_jdbcView.connect() == false){			
			    throw(new Exception());
			}
			
			HC = new HistogramCalculator(m_jdbcUpdate, m_jdbcView);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't connect to database");
			System.exit(1);
		}
		
		
	}
*/

	


    /*
    	allUserTime
    	@param String user: the base one
    	@param int level: for computeVIPRank
    	@return Vector: each other user and the avg comm time to him
    */
    public userComm[] allUserTime(String user) throws SQLException{
    	return allUserTime(user,"", 5);
    }
    
    public userComm[] allUserTime(String user, String condition, int level) throws SQLException{
        	
    	//first, get out all the emails related to this user, and order them by time
    	String[][] data;
    	String query;	
    	synchronized(m_jdbcView){
			query="select sender, rcpt, utime from email where (sender='"+user+
				"' or rcpt='"+user+"') and ("+condition+") order by utime";
			data = m_jdbcView.getSQLData(query);
	    	
	    	
    	}
    	
    	//for test
    	/*
    	System.out.println("before data:");
    	for(int i=0;i<20;i++){
    		System.out.println((String)data[i][0]+", "+(String)data[i][1]+", "+(String)data[i][2]);	
    	}
    	*/
    	
    	data=processAllTime(data, user);
    	
    	//for test
    	/*
    	System.out.println("after data:");
    	for(int i=0;i<20;i++){
    		System.out.println((String)data[i][0]+", "+(String)data[i][1]+", "+(String)data[i][2]);	
    	}
    	*/
    	
    	//secondly, organize the emails according to individual account
    	HashMap accounts= new HashMap(); //acct number as key, vector of emails as value
    	int rows = data.length;
    	for(int i=0;i<rows; i++){
    		
    		String sender=data[i][0];
    		String rcpt=data[i][1];
    		String time=data[i][2];// + " "+ (String)data[i][3];	
    		
    		if(sender.equalsIgnoreCase(rcpt)) //user to himself
    			continue;
    		
    		String keyUser;
    		singleMessage email;
    		if(sender.equals(user)){ //from user to other
    			keyUser=rcpt;
    			email = new singleMessage(time, singleMessage.SEND);
    		}else{ //from others to user
    			keyUser=sender;
    			email = new singleMessage(time, singleMessage.RECEIVE);
    		}
    		
    		if(accounts.containsKey(keyUser)){
    			Vector vec = (Vector)accounts.get(keyUser);
    			vec.addElement(email);
	    		accounts.put(keyUser, vec);
    		}else{
    			Vector vec = new Vector();
    			vec.addElement(email);
    			accounts.put(keyUser, vec);	
    		}    		
    	}// for i
    	
    	
    	//get this user's usage histogram to adjust the avg comm time
    	//Vector histogram = HC.getHistogram(user);
    	
    	//for test
    	/*
    	for(int k=0;k<histogram.size();k++){
    		System.out.print((Double)histogram.elementAt(k)+", ");	
    	}
    	System.out.println();
    	*/
    	
    	//compute avg time for each account
    	//Vector ret = new Vector();
    	//Set keys = accounts.keySet();
    	
    	
    	userComm[] ret = new userComm[accounts.size()];
								    	
    	Iterator iter = accounts.keySet().iterator();//keys.iterator();
    	int total=0;
    	int place=0;
    	while(iter.hasNext()){
    		String acct = (String)iter.next();
    		Vector vec = (Vector)accounts.get(acct);		
    		total+=vec.size();
    		
    		double gap = avgtime(vec);
    		
    		/*
    		//OR adjust the time individually according to each's usage histogram
    		Vector rounded_vec = adjustTime(vec, histogram);
    		double gap = avgtime(rounded_vec);
    		*/
    		ret[place++]  =    	new userComm(acct, vec.size(), gap);	
//    		ret.addElement(new userComm(acct, vec.size(), gap));
    		
    		//for test
    		//if(gap>0)
    			//System.out.println(acct+", "+vec.size()+",  "+gap);
    	}    	
    	
    	ret = computeVIPRank(ret, level);
    	
    	return ret;
    }
  	
  	
  	
  	/*
  		Adjust the insertTime of every user according to the nearest next send email
  	*/
  	private String[][] processAllTime(String[][] array, String user){
  		
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
  	}
  	
  	
  	/*
  		Adjust the time of emails according to this user's usage histogram
  	*/
  /*	private Vector adjustTime(Vector vec, Vector histogram){
  		
  		int size=vec.size();
  		Calendar c=Calendar.getInstance();
  		
  		for(int i=0;i<size;i++){
  			oneEmail email = (oneEmail)vec.elementAt(i);
  			c.setTimeInMillis(email.datestamp);
  			int hour = c.get(Calendar.HOUR_OF_DAY);
  			double v = ((Double)histogram.elementAt(hour)).doubleValue();
  			
  			if(v>0.02) //this email is sent in usual work time
  				continue;
  			
  			int pos = hour;
  			int iter=0;
  			//System.out.println("i="+i+", start: "+hour);
  			while(v<=0.02 && iter<23){  				
  				pos++; pos=pos%24;
  				iter++;
  				v = ((Double)histogram.elementAt(pos)).doubleValue();
  				//System.out.print("pos:"+pos);
  			}
  			
  			int gap = pos-hour;
  			c.roll(Calendar.HOUR_OF_DAY,gap);
  			email.datestamp=c.getTimeInMillis();
  			vec.setElementAt(email, i);
  			
  			//for test
  			//System.out.println("rolling: "+gap);
  		}
  		
  		return vec;
  	}
  	*/
  	/*
  		given vector of emails, compute the average communication time
  		@param Vector vec: the vector of emails  		
  	*/
  	private double avgtime(Vector vec){
  		
  		if(vec.size()<=1) //no real communication
		     //retun 1000*1000;
		    return Double.MAX_VALUE;
		    
  		
  		long totalTime=0;
  		int totalNum=0;
  		int size = vec.size();
  		int i=0;
  		while(i<size){
  		  singleMessage email = (singleMessage)vec.elementAt(i);
  			if(email.type == singleMessage.SEND){
  				i++;
  				continue;
  			}
  			
  			int start = i; //start of RECEIVE email
  			int end=-1;
  			i++;
  			boolean notfound = true;
  			while(i<size && notfound){
  				email = (singleMessage)vec.elementAt(i);
  				if(email.type == singleMessage.RECEIVE){
  					i++;
  					continue;
  				}
  				end=i; //first reply(SEND) email to previous received emails
  				notfound =false;
  			}
  			
  			//compute the time between start and end(the first response)
  			if(!notfound){
  				totalNum += end-start;
  				long endTime = ((singleMessage)vec.elementAt(end)).datestamp;
  				for(int j=start;j<end;j++){
  					long tt = ((singleMessage)vec.elementAt(j)).datestamp;
  					totalTime += endTime-tt;
  				}
  				
  			}
  		}
  		
  		if(totalNum==0)
		    //return 1000*1000;
		    return Double.MAX_VALUE;
  		
  		double avg = totalTime/(double)(totalNum*1000*60); //how many minutes
  		
  		return avg;
  	}
  	
	
	/*
		given the userComm with gap value, compute their VIP rank using formula:
			VIP =(#email/avg #email) + (avg_time/(time+avg_time))
		@param Vector vec: the vector of computed userComm object
		@return Vector
	*/
	public static userComm[] computeVIPRank(userComm[] vec, int level){
		int size = vec.length;
		
		//get average
		int total=0;
		double totalNum = 0;
		double totalTime =0;
		for(int i=0;i<size;i++){
			userComm user = vec[i];//(userComm)vec.elementAt(i);
			totalNum += user.number;
			if(user.time<Double.MAX_VALUE){ //only count those valid time
				totalTime += user.time;
				total++;
			}
		}
		
		double avgNum = totalNum/size;
		double avgTime = totalTime/total;
		
		//System.out.println("avgNum:"+avgNum+", avgTime="+avgTime);
		
		for(int i=0;i<size;i++){
			userComm user = vec[i];//(userComm)vec.elementAt(i);
			if(user.time<Double.MAX_VALUE){
				double factor1 = (11-level)/(double)12;
				double factor2 = (1+level)/(double)12;
				double tmp = factor1*(user.number)/avgNum + factor2*avgTime/(user.time+avgTime/10);
				user.vipValue = tmp;
				vec[i] = user;//vec.setElementAt(user,i);
			}
			
			//for test
			//System.out.println(user.acct+", "+user.vipValue);
		}
		
		return vec;
	}
	
	/*
	//main for testing	
	public static void main(String[] args){
		CommunicateTime CT = new CommunicateTime();
		String user=args[0];
		try{
			CT.allUserTime(user);
		}catch(Exception e){
			System.err.println("exp:"+e);	
		}
		
	}
	*/  
}






