package metdemo.MachineLearning;


import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
//import java.util.Enumeration;
//import java.sql.*;
import java.sql.SQLException;

import metdemo.AlertTools.AlertPoint;
import metdemo.DataBase.EMTCloudscapeConnection;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Parser.EMTEmailMessage;
import metdemo.dataStructures.SimpleHashtable;


/** Statistical modeling for machine learning
 *
 *@author weijen
 */


public class MLStatistics extends MLearner{
	
    final private int SENDER_INDEX = 0;
    final private int RECIPIENT_INDEX = 1;
    final private int ATTACHMENT_INDEX = 3;
    final private int MV_SIZE = 100;
    final private int HELLINGER_DISTANCE = 50;
    final private int SPAN3=50;//the window size...
    final private int SD_THRE_SIZE = 50;
    final private double ALPHA = 0.2;
    final private String LINE = "******* next user *******";
    private String[][] trainingData;
    private Vector results;
    private Vector dataIn;
    private Vector linesIn;
    private String TARGET_CLASS = "s";
    public MLStatistics(){
	super.setID("MLStatistics");		
    }
	
    public void train(short[] header, String[] classes
		      , String[][] data,int n,String TARGET) throws Exception{
	results = new Vector();
	dataIn = new Vector();
	linesIn = new Vector();
	TARGET_CLASS = TARGET;
	System.out.println("start trainning...");
	try{
	    int currIndex = 0;
	    for(int i=0;i<n;i++){
		trainingData = new String[header[i]][5];
				
		for(int j=0;j<header[i];j++){
		    trainingData[j][0] = data[currIndex+j][0];
		    trainingData[j][1] = data[currIndex+j][1];
		    trainingData[j][2] = data[currIndex+j][2];
		    trainingData[j][3] = data[currIndex+j][3];
		    trainingData[j][4] = data[currIndex+j][4];
		}
		currIndex += header[i];
				
		MLStAlert tmpAlert = alert(classes[i]);
		results.add(tmpAlert);
				
	    }
			
	}
	catch(Exception e){
	    System.out.println("train error:"+e);
	}
		
    }
	
    public MLStAlert alert(String user){
	MLStAlert result = new MLStAlert(user);
	try{
	    AlertPoint alertpoint = new AlertPoint();
	    double[] hellinger = getHArray();
	    double[][] sequence = getRcptAttach();
			
	    //yep, I have to do this stupid thing again
	    double[] rcpts = new double[sequence.length];
	    double[] attachs = new double[sequence.length];
	    for(int i=0; i<sequence.length; i++){
		rcpts[i] = sequence[i][1];//rcpt with window size 50
		attachs[i] = sequence[i][2];//attachment with window size 50
	    }
			
	    //Standard Deviation algorithm
	    double[] sdHell = alertpoint.calculateThreshold(hellinger , SD_THRE_SIZE, ALPHA);
	    double[] sdRcpt = alertpoint.calculateThreshold(rcpts , SD_THRE_SIZE, ALPHA);
	    double[] sdAttach = alertpoint.calculateThreshold(attachs , SD_THRE_SIZE, ALPHA);
			
	    boolean[] test1 
		= alertpoint.maAlert(sdHell, hellinger);
	    boolean[] test2 
		= alertpoint.maAlert(sdRcpt, rcpts);
	    boolean[] test3 
		= alertpoint.maAlert(sdAttach, attachs);
	    AlertPoint[] STD
		= alertpoint.collectAlert(test1, test2, test3);
	    /*********************************/
			
	    //moving average algorithm, no use now
	    /*
	      double[] mvHell
	      = alertpoint.movingAverage(MV_SIZE, hellinger);
	      double[] mvRcpt
	      = alertpoint.movingAverage(MV_SIZE, rcpts);
	      double[] mvAttach
	      = alertpoint.movingAverage(MV_SIZE, attachs);
			
	      boolean[] test4
	      = alertpoint.maAlert(mvHell, hellinger);
	      boolean[] test5 
	      = alertpoint.maAlert(mvRcpt, rcpts);
	      boolean[] test6 
	      = alertpoint.maAlert(mvAttach, attachs);
	      AlertPoint[] MA 
	      = alertpoint.collectAlert(test4, test5, test6);
	    */
	    /**********************************/
			
	    //lalala
	    AlertPoint[] alerts 
		= alertpoint.findMountain(hellinger,rcpts,attachs);
	    /**********************************/
			
	    AlertPoint[] refined
		= alertpoint.union(trainingData.length,STD,alerts);
			
	    //add the result
	    int lasts = 0;
	    int laste = 0;
	    int s = 0;
	    int e = 0;
	    for(int i=0;i<refined.length;i++){
		s = refined[i].start;
		e = refined[i].end;
		int j=0;
		for(j=laste;j<s;j++){
		    result.normal(trainingData[j][4]+","
				  +trainingData[j][0]+","
				  +trainingData[j][1]+","
				  +trainingData[j][2]+","
				  +trainingData[j][3]);
		}
		for(;j<e;j++){
		    result.bad(trainingData[j][4]+","
			       +trainingData[j][0]+","
			       +trainingData[j][1]+","
			       +trainingData[j][2]+","
			       +trainingData[j][3]);
		}
				
		lasts = s;
		laste = e;
	    }
			
	}
	catch(Exception e){
	    System.out.println("alert error:"+e);
	}
		
	return result;
    }
	
    public double[] getHArray(){
		
	try{
	    Vector hellinger = calculateHellinger();
	    double[] h = new double[hellinger.size()];
	    for (int i=0; i<hellinger.size(); i++){
		h[i] = ((Double)hellinger.get(i)).doubleValue();
	    }
	    return h;
	}
	catch(Exception e){
	    System.out.println("getHArray error:"+e);
	    return null;
	}
    }
	
    public Vector calculateHellinger(){
	Vector distance = new Vector();
	SimpleHashtable simpleHash = new SimpleHashtable( );
		
	int allDataSize=trainingData.length;
	double trainFreq = 1.0/((double)HELLINGER_DISTANCE*4);
	double testFreq = 1.0/(double)HELLINGER_DISTANCE;
	int start = 0;			   	
	double sum = 0;
		
	if (HELLINGER_DISTANCE*5 > allDataSize)
	    return null;
		
	//build hash table for data 0 to defaultSize*5
	for(int i=0; i<HELLINGER_DISTANCE*5; i++){
	    String name = (String)trainingData[i][RECIPIENT_INDEX];
	    if (i<HELLINGER_DISTANCE*4){
		simpleHash.insert(name,"train",trainFreq,0);
	    }
	    else{
		simpleHash.insert(name,"test",testFreq, 0);
	    }
	    if (i <HELLINGER_DISTANCE*5-1)
		distance.addElement(new Double(0.0));
	}
		
	//compute distance in the first term
	Vector theKey = simpleHash.getKeys();
	for (int i=0; i<theKey.size(); i++){
	    String name = (String)theKey.get(i); 
	    double train = simpleHash.getTrain(name);
	    double test = simpleHash.getTest(name);
	    sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	}
	distance.addElement(new Double(sum));// first term's distance
		
		
	//build hash table for all of terms and compute distances
	//total runing times
	for (int i=HELLINGER_DISTANCE*5; i<trainingData.length; i++){
	    double summ =0;
	    //cut first for training
	    String name =(String)trainingData[start][RECIPIENT_INDEX];
	    simpleHash.remove(name, "train", trainFreq);
			
	    //add last for training
	    name = (String)trainingData[start+HELLINGER_DISTANCE*4][RECIPIENT_INDEX];
	    simpleHash.insert(name, "train", trainFreq,0);
			
	    //cut first for test
	    name = (String)trainingData[start+HELLINGER_DISTANCE*4][RECIPIENT_INDEX];
	    simpleHash.remove(name, "test", testFreq);
	    //add last for test
	    name = (String)trainingData[start+HELLINGER_DISTANCE*5][RECIPIENT_INDEX];
	    simpleHash.insert(name, "test", testFreq,0 );
			
	    //compute distance
	    theKey.clear();
	    theKey = simpleHash.getKeys();
	    for (int j=0; j<theKey.size(); j++){
		String tag =(String ) theKey.get(j);
		double train = simpleHash.getTrain(tag);
		double test = simpleHash.getTest(tag);
				
		summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
	    }
			
	    distance.addElement(new Double(summ)); 
	    start++;
	}
		
	return distance;
    }
	
	
    public double[][] getRcptAttach(){
	double[][] sequence = new double[trainingData.length][3];
	Vector ALList = new Vector();
	Vector ALList2 = new Vector();
	Vector ALList3 = new Vector();
	Vector ALList_collect = new Vector();
	Vector ALList2_collect = new Vector();
	Hashtable m_ALList = new Hashtable();//.clear();
	int cnt = 0, cnt2 = 0, cnt3 = 0;
	String last = new String();
	int firstSpan = 30;//analysis size, 30 is a good choice
		
	//the second analysis size, maybe it should be smaller.
	int secondSpan = 50;
	for (int i=0; i<trainingData.length; i++){ 
			
	    ///plot distinct per 'firstSpan'
	    if (i<firstSpan){  
		ALList.add(0,trainingData[i][RECIPIENT_INDEX]);     
		if (!(ALList_collect.contains(trainingData[i][RECIPIENT_INDEX]))){  
		    ALList_collect.add(trainingData[i][RECIPIENT_INDEX]);  
		}
	    }
	    else{  
		last = (String) ALList.elementAt(firstSpan-1);
		ALList.remove(firstSpan-1);
		ALList.add(0,trainingData[i][RECIPIENT_INDEX]);
		if (!ALList.contains(last)){  
		    ALList_collect.remove(last);
		}
		if (!(ALList_collect.contains(trainingData[i][RECIPIENT_INDEX]))){  
		    ALList_collect.add(trainingData[i][RECIPIENT_INDEX]); 
		}
	    }
	    cnt = ALList_collect.size();
	    sequence[i][0]= cnt;
			
	    ///plot  distince per 100, and with attachment per 100
	    //use secondSpan
	    if (i<secondSpan){  
		ALList2.add(0,trainingData[i][RECIPIENT_INDEX]);
		if ((new Integer(trainingData[i][ATTACHMENT_INDEX])).intValue()>0){ 
		    cnt3++;
		}
		if (!(ALList2_collect.contains(trainingData[i][RECIPIENT_INDEX]))){  
		    ALList2_collect.add(trainingData[i][RECIPIENT_INDEX]);
		}
	    }
	    else{  
		last = (String) ALList2.elementAt(secondSpan-1);
		ALList2.remove(secondSpan-1);
		ALList2.add(0,trainingData[i][RECIPIENT_INDEX]);
		if (!ALList2.contains(last)){  
		    ALList2_collect.remove(last);
		}
		if (!(ALList2_collect.contains(trainingData[i][RECIPIENT_INDEX]))){  
		    ALList2_collect.add(trainingData[i][RECIPIENT_INDEX]);
		}
		if (Integer.parseInt(trainingData[i][ATTACHMENT_INDEX]) > 0 ){  
		    cnt3++;
		}
		if (Integer.parseInt(trainingData[i-SPAN3][ATTACHMENT_INDEX]) > 0 ){
		    cnt3--;
		}
	    }
			
	    cnt2 = ALList2_collect.size();
	    sequence[i][1] = cnt2;
	    sequence[i][2] = cnt3;
	}
	return sequence;
    }
	
    /** save part */
    public final void save(String filename){
	try{
	    // System.out.println("\nStatistics - save : " + filename);
	    FileOutputStream out = new FileOutputStream(filename);
	    ObjectOutputStream s = new ObjectOutputStream(out);
	

	    //	    PrintWriter out= new PrintWriter(new FileWriter(filename));
	    s.writeObject(getID());
	    s.writeObject(getFeature());
	    s.writeInt(results.size());
	    for(int i=0;i<results.size();i++){
		MLStAlert ml = (MLStAlert)results.get(i);
		//out.println(ml.user);
		//out.print(ml.data);
		//out.println(LINE);
		s.writeObject(ml.user);
		s.writeObject(ml.data);
		s.writeObject(LINE);
		

	    }
	    s.writeObject(TARGET_CLASS);
	    s.flush();
	    s.close();
	    //	    System.out.println("Save done!");
	}catch(Exception e){
	    System.out.println("Exception in file write out: "+e);
	}
    }
	
    public final void load(String filename){
	try{
	
	    FileInputStream in = new FileInputStream(filename);
	    ObjectInputStream s = new ObjectInputStream(in);
	

	    //    BufferedReader in = 
	    //	new BufferedReader(new FileReader(filename));
	    s.readObject(); //this is the id string
	    setFeature((String)s.readObject());
	    int n = s.readInt();
	    linesIn = new Vector();
	    
	    for(int a=0;a<n;a++)
		{
		    //MLStAlert ml =new  MLStAlert((String)s.readObject(),(String)s.readObject());  
		    String user = (String)s.readObject();
		    s.readObject();
		    s.readObject();
		    //results.add(ml);
		   linesIn.add(user); //NOT SURE IF THIS????
		}

	    /*boolean next = true;
	    while(in.ready()){
		String readin = in.readLine();
		
		if(next){
		    next = false;
		}
		else if(readin.equals(LINE)){
		    next = true;
		}
		else{
		    //System.out.println("..."+readin);
		    linesIn.add(readin);
		}
		}*/
	    TARGET_CLASS = (String)s.readObject();
	    s.close();
			
	}catch(Exception e){
	    System.out.println("Exception in file read in: "+e);
	    System.out.println("Check the correct file content.");
	}
	System.out.println("Load done!");
    }
	
    public final void load2(String filename){
	try{
	    BufferedReader in = 
		new BufferedReader(new FileReader(filename));
	    dataIn = new Vector();
	    MLStAlert datain = new MLStAlert();
	    boolean next = true;
	    while(in.ready()){
		String readin = in.readLine();
		if(next){
		    datain = new MLStAlert(readin);
		    next = false;
		}
		else if(readin.equals(LINE)){
		    dataIn.add(datain);
		    next = true;
		}
		else{
		    datain.add(readin);
		}
	    }
			
	    in.close();
	}catch(Exception e){
	    System.out.println("Exception in file read in: "+e);
	    System.out.println("Check the correct file content.");
	}
		
    }
	
    public String[][] classify(String records[][]) throws Exception{
	try{
	    System.out.println("clasifying..."+linesIn.size());
	    if(linesIn.size()==0)return null;
			
	    String [][] output = new String[linesIn.size()][6];
			
	    for(int i=0;i<linesIn.size();i++){
		String tmps = (String)linesIn.get(i);
		String[] tmpline = tmps.split(",");
		for(int j=0; j<tmpline.length; j++){
		    output[i][j] = tmpline[j];
		}
	    }
	    System.out.println("classify done!");
	    return output;
	}
	catch(Exception e){
	    System.out.println("classify error: "+e);
	    System.out.println("Try to load a correct file first.");
	    return null;
	}
    }
/*
    public static void main(String[] args){
	EMTDatabaseConnection m_jdbcView;
	try{

	    m_jdbcView = new EMTCloudscapeConnection("jdbc:mysql://" 
					 + "blackberry" + "/" 
					 + ""
					 ,"org.gjt.mm.mysql.Driver"
					 ,"outsider", "");
			
	    if(m_jdbcView.connect() == false){
		throw(new Exception());
	    }

	    
	    /*************** start here ******************
	    //the header and classes
	    String query = "select sender,count(sender) from email "
		+ "group by sender having count(*)>1000 order by sender";
	    
	    String[][] hc  = null;
	    if (!(m_jdbcView==null)){  
		try{
			
			hc = m_jdbcView.getSQLData(query);}
		catch(SQLException ex){   
		    System.out.println("query1 error:"+ex);
		}finally{}
	    }
	   
	    int length = hc.length;
	    String[] classes = new String[length];
	    short[] header = new short[length];
	    String users = "";
	    for(int i=0;i<length;i++){
		classes[i] = hc[i][0];
		header[i] = Short.parseShort(hc[i][1]);
		
		if(i==0)
		    users += " sender='"+classes[i]+"'";
		else
		    users += " or sender='"+classes[i]+"'";
	    }

	    //get the data
	    query = "select sender,rcpt,insertTime,numattach,UID from"
		+" email where"+users
		+" order by sender,insertTime";
	    
	    String[][] emailData = null;
	    if (!(m_jdbcView==null)){  
		try{
			
			emailData = m_jdbcView.getSQLData(query);}
		catch(SQLException ex){   
		    System.out.println("query2 error:"+ex);
		}finally{}
	    }
	    
	   
	    int numOfData = emailData.length;
	    
	    MLStatistics mls = new MLStatistics();
	    mls.train(header,classes,emailData,length,"s");
	    mls.save("test.txt");
	     
	    MLStatistics mlsLoad = new MLStatistics();
	    mlsLoad.load("test.txt");
	    String[][] loadedData = mlsLoad.classify(null);

	    /*
	      String[i]="class, UID, sender, rcpt, insertTime"
	      class: N=normal, B=bad(spam)
	    *
	    for(int i=0;i<loadedData.length;i++){
		for(int j=0;j<loadedData[i].length;j++){
		    System.out.print(loadedData[i][j]+"  ");
		}
		System.out.println();
	    }
	    /*************** end here ******************

	}
	catch(Exception e){
	    System.err.println("Couldn't connect to database");
	    System.exit(1);
	}
    }
*/
	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[], java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
        return MLearner.ML_STATS;
    }
	
}

class MLStAlert{
    String user;
    String data;
    int lineCount;
	
    public MLStAlert(){
	this("none");
    }
	
    public MLStAlert(String u){
	user = u;
	data = "";
	lineCount = 0;
    }

    public MLStAlert(String a,String b, int n){
	user = a;
	data = b;
	lineCount = n;
    }


	
    public void normal(String s){
	data += "N," + s + "\n";
	lineCount++;
    }
	
    public void bad(String s){
	data += "B," + s + "\n";
	lineCount++;
    }
	
    public void add(String s){
	data += s + "\n";
	lineCount++;
    }
	
}
