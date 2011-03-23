/**
   This is the alert range, for dummy simulation.
   It also includes many useful methods for the simulation
*/
package metdemo.AlertTools;

//import java.lang.*;
//import java.math.*;
//import java.net.*;
import java.util.*;
//import java.io.*;
//import java.sql.*;
//import java.sql.SQLException;
import java.text.*;
//import java.text.NumberFormat;

public class AlertPoint{
    public int start = 0;
    public int end = 0;
    private int[] realDummy;

    public AlertPoint(){
    }

    public AlertPoint(int[] dummy){
	realDummy = new int[dummy.length];
	System.arraycopy(dummy,0,realDummy,0,dummy.length);
    }

    /**
       Add the start position.
       @param s Start position.
    */
    public void addStart(int s){
	start = s;
    }

    /**
       Add the end position.
       @param e End position.
    */
    public void addEnd(int e){
	end = e;
    }


    /**
       ...a useless method...
    */
    public AlertPoint[] refineThreshold(AlertPoint[] thr, double[] seq){
	AlertPoint[] refined = new AlertPoint[thr.length];
	//int thrIndex=0;
	for(int i=0;i<thr.length;i++){
	    refined[i]=new AlertPoint();
	    int s=thr[i].start;
	    refined[i].addStart(s);
	    int e=thr[i].end;
	    double last=0;
	    for(int j=s;j<e;j++){
		System.out.print(" "+j+":"+seq[j]+" ");
	    }
	    System.out.println("");
	    for(int j=s;j<e;j++){
		if(seq[j]<last){
		    refined[i].addEnd(j);
		    break;
		}
		else if(j==e-1){
		    refined[i].addEnd(j);
		}
		else last=seq[j];
	    }
	}
	return refined;
    }

    //yes, take the intersection of two ranges
    public AlertPoint[] intersection(AlertPoint[] alert1, AlertPoint[] alert2){
//	int log=0;
	try{
	    int index2=0;
	    int length = alert1.length;
	    if(alert1.length<alert2.length)length=alert2.length;

	//    log=1;
	    AlertPoint[] alert = new AlertPoint[length+1000];
	    int alertIndex=0;
	    AlertPoint tmp=new AlertPoint();
	    for(int i=0; i<alert1.length; i++){
		int s1=alert1[i].start;
		int e1=alert1[i].end;
		//log=2;
		if(index2>0)index2--;//skip the checked part
		for(int j=index2;j<alert2.length;j++){
		    tmp=new AlertPoint();
		    int s2=alert2[j].start;
		    int e2=alert2[j].end;
		  //  log=3;
		    if(e1<s2){
			break;//go to next alert1
		    }
		    else if(e2<s1){//go to next alert2
			continue;
		    }
		    //log=4;
		    //start
		    if(s1<s2){
			//log=5;
			tmp.addStart(s2);
		    }
		    else{
			//log=6;
			tmp.addStart(s1);
		    }

		    //end
		    if(e1<e2){
			//log=7;
			tmp.addEnd(e1);
		    }
		    else{
			//log=8;
			tmp.addEnd(e2);
		    }

		    alert[alertIndex]=tmp;
		    alertIndex++;
		    //log=9;
		
		}//for j
	    }//for i

	    AlertPoint[] toReturn = new AlertPoint[alertIndex];
	    System.arraycopy(alert,0,toReturn,0,alertIndex);
	    return toReturn;
	}
	catch(Exception e){
	    System.out.println("intersection error:"+e);
	    return null;
	}
    }

    //take the intersection of two ranges
    public static boolean[] intersection(boolean[] alert1, boolean[] alert2){
		
	int length = alert1.length;
	if(length>alert2.length)length=alert2.length;
		
	boolean[] toReturn = new boolean[length];
	for(int i=0; i<length; i++){
	    if(alert1[i]==true && alert2[i]==true)
		toReturn[i] = true;
	    else toReturn[i] = false;
	}
		
	return toReturn;
    }

     //take the intersection of two ranges
    public static boolean[] union(boolean[] alert1, boolean[] alert2){
		
	int length = alert1.length;
	if(length>alert2.length)length=alert2.length;
		
	boolean[] toReturn = new boolean[length];
	for(int i=0; i<length; i++){
	    if(alert1[i]==true || alert2[i]==true)
		toReturn[i] = true;
	    else toReturn[i] = false;
	}
		
	return toReturn;
    }

    /**
       This way is a little slow. Because I'm too tired to think more.
    */
    public AlertPoint[] union(int size
			      , AlertPoint[] alert1, AlertPoint[] alert2){
//	int log=0;
	try{
	    boolean[] range = new boolean[size];
	    for(int i=0; i<alert1.length; i++){
		for(int j=alert1[i].start;j<=alert1[i].end;j++){
		    range[j]=true;
		}
	    }
	    for(int i=0; i<alert2.length; i++){
		for(int j=alert2[i].start;j<=alert2[i].end;j++){
		    range[j]=true;
		}
	    }

	    AlertPoint[] alert = new AlertPoint[size];
	    int alertIndex=0;
	    AlertPoint tmp=new AlertPoint();
	    boolean enter=false;
	    for(int i=0;i<range.length;i++){
		if(!enter){
		    if(range[i]){
			enter=true;
			tmp.addStart(i);
		    }
		}

		else{
		    if(!range[i] || i==range.length-1){
			tmp.addEnd(i);
			alert[alertIndex]=tmp;
			alertIndex++;
			tmp=new AlertPoint();
			enter=false;
		    }
		}
	    }

	    AlertPoint[] toReturn = new AlertPoint[alertIndex];
	    System.arraycopy(alert,0,toReturn,0,alertIndex);
	    return toReturn;
	}
	catch(Exception e){
	    System.out.println("union error:"+e);
	    return null;
	}
    }

    	
    /**
       Calculate the Threshold for the dummy test.
       Because of statistics, we don't compute the first N-1 data.
	
       Algorithm:
       Use the last N data, compute their Standard Deviation(SD).
       The Threshold is (old_value + SD)*alpha.
	
       @param input the list of data(curve) to be used
       @param N the size of a block to compute SD.
       @param alpha A canstant.
       @return threshold the data(curve) of threshold
    */
    public double[] calculateThreshold(double[] input, int N, double alpha){
	double[] threshold = new double[input.length];
	double[] block = new double[N];
	int oldestIndex = N-1;//the oldest value of this block
	//int shift = 20;

	//read the first N data
	for(int i=0; i<N-1; i++){
	    threshold[i] = input[i];
	    block[i] = input[i];
	}

	//calculate the threshold from N-1th data
	for(int i=N-1; i<input.length; i++){
	    block[oldestIndex] = input[i];
	    oldestIndex++;
	    if(oldestIndex >= N) oldestIndex = 0;
			
	    threshold[i] = (input[i])
		+ (alpha * getSD(block, getAverage(block)) );
	}

	//shift the value to last 20
	double[] t2 = new double[input.length];
	for(int i=0; i<input.length; i++){
	    if( i < (N-1 + 20) ){
		t2[i] = input[i];
	    }
	    else{
		t2[i] = threshold[i-20];
	    }
	}

	//return threshold;
	return t2;
    }
	
    /**
       Get the average of a set of numbers.
       @param input an array with numbers.
       @return the average.
    */
    public double getAverage(double[] input){
	double length = (double)input.length;
	double total = 0.0;
	for(int i=0; i< input.length; i++){
	    total += input[i];
	}
		
	double avg = total/length;
	return avg;
    }
	
    /**
       Compute the Standard Deviation.
       @param input The numbers.
       @param avg The average of these numbers
       @return The Standard Deviation.
    */
    public double getSD(double[] input, double avg){
	double tmpAdder = 0.0;
	for(int i=0; i<input.length; i++){
	    tmpAdder += Math.pow(avg-input[i], 2);
	}
		
	double tmp = tmpAdder/(double)input.length;
	double standardDev = Math.sqrt(tmp);
	return standardDev;
    }

    /**
       Analyze the curves for the dummy simulation.
       When rcpt and attach are increasing, and the slope of
       hd is changing, this range may be a suspicious range.
       @param hd Data of The Hellinger Distance.
       @param rcpt Data of distince recepients.
       @return Several suspicious ranges.
    */
    public AlertPoint[] findMountain(double[] hd
				     , double[] rcpt
				     , double[] attach){
	AlertPoint[] alert = new AlertPoint[hd.length];
	int alertCount = 0;
//	boolean beginChange = false;
	AlertPoint tmpAlert = new AlertPoint();
	boolean mnt1 = false;
	boolean mnt2 = false;
	int suspectRange = 10;
	int slopeRange = 5;
	double hdLastSlope = 1;
	boolean suspect = false;
	boolean alertArea = false;

	/*
	  Skip the first 50 data, because they are not enough
	  for statistical analysis.
	*/
	for(int i=50; 
	    i<hd.length-1 && i<rcpt.length-1 && i<attach.length-1;
	    i++){

	    /*
	      find the slope
	      1: rcpt
	      2: attach
	    */
	    double[] tmp1 = new double[suspectRange];
	    double[] tmp2 = new double[suspectRange];
	    int tmpIndex = suspectRange-1;
	    for(int j=0; j<suspectRange; j++){
		tmp1[j] = rcpt[i-tmpIndex];
		tmp2[j] = attach[i-tmpIndex];
		tmpIndex--;
	    }
	    double slope1 = getSlope(tmp1);
	    double slope2 = getSlope(tmp2);
		
	    //find the hd slope
	    double[] tmp3 = new double[slopeRange];
	    tmpIndex = slopeRange-1;
	    for(int j=0; j<slopeRange; j++){
		tmp3[j] = hd[i-tmpIndex];
		tmpIndex--;
	    }
	    double hdSlope = getSlope(tmp3);

	    if(slope1 > 0) mnt1 = true;
	    else mnt1 = false;
	    if(slope2 > 0) mnt2 = true;
	    else mnt2 = false;

	    //if both of them are increasing...
	    if(mnt1 && mnt2){
		if(!suspect){
		    suspect = true;
		}
	    }
	    else{
		//this point is just after the top of the "mountain"
		if(alertArea){
		    tmpAlert.addEnd(i);
		    alertArea = false;
		    //alert[alertCount] = tmpAlert;
		    alert[alertCount]=new AlertPoint();
		    alert[alertCount].addStart(tmpAlert.start);
		    alert[alertCount].addEnd(tmpAlert.end);
		    alertCount++;
		}
		suspect = false;
		tmpAlert = new AlertPoint();
	    }
	    
	    if(suspect){
		double tmp4 = hdSlope-hdLastSlope;
		//if the slope of HD increases
		if(tmp4 > 0 && !alertArea){
		    tmpAlert.addStart(i);
		    alertArea = true;
		}
	    }
	    hdLastSlope = hdSlope;
	}//for

	AlertPoint[] toReturn = new AlertPoint[alertCount];
	System.arraycopy(alert,0,toReturn,0,alertCount);
	return toReturn;
    }

    public AlertPoint[] findMountain(double[] hd
				     , double[] rcpt){
	AlertPoint[] alert = new AlertPoint[hd.length];
	int alertCount = 0;
	//boolean beginChange = false;
	AlertPoint tmpAlert = new AlertPoint();
	boolean mnt1 = false;
	int suspectRange = 10;
	int slopeRange = 5;
	double hdLastSlope = 1;
	boolean suspect = false;
	boolean alertArea = false;

	/*
	  Skip the first 50 data, because they are not enough
	  for statistical analysis.
	*/
	for(int i=50; i<hd.length-1 && i<rcpt.length-1; i++){

	    /*
	      find the slope
	      1: rcpt
	      2: attach
	    */
	    double[] tmp1 = new double[suspectRange];
	    int tmpIndex = suspectRange-1;
	    for(int j=0; j<suspectRange; j++){
		tmp1[j] = rcpt[i-tmpIndex];
		tmpIndex--;
	    }
	    double slope1 = getSlope(tmp1);
		
	    //find the hd slope
	    double[] tmp3 = new double[slopeRange];
	    tmpIndex = slopeRange-1;
	    for(int j=0; j<slopeRange; j++){
		tmp3[j] = hd[i-tmpIndex];
		tmpIndex--;
	    }
	    double hdSlope = getSlope(tmp3);

	    if(slope1 > 0) mnt1 = true;
	    else mnt1 = false;

	    //if both of them are increasing...
	    if(mnt1){
		if(!suspect){
		    suspect = true;
		}
	    }
	    else{
		//this point is just after the top of the "mountain"
		if(alertArea){
		    tmpAlert.addEnd(i);
		    alertArea = false;
		    //alert[alertCount] = tmpAlert;
		    alert[alertCount]=new AlertPoint();
		    alert[alertCount].addStart(tmpAlert.start);
		    alert[alertCount].addEnd(tmpAlert.end);
		    alertCount++;
		}
		suspect = false;
		tmpAlert = new AlertPoint();
	    }
	    
	    if(suspect){
		double tmp4 = hdSlope-hdLastSlope;
		//if the slope of HD increases
		if(tmp4 > 0 && !alertArea){
		    tmpAlert.addStart(i);
		    alertArea = true;
		}
	    }
	    hdLastSlope = hdSlope;
	}//for

	AlertPoint[] toReturn = new AlertPoint[alertCount];
	System.arraycopy(alert,0,toReturn,0,alertCount);
	return toReturn;
    }

    /**
       Compute whether the slope of the last 2 points is higher than
       the slope of the first 2 points.
       @param first The first point.
       @param second The second point.
       @param third The third point.
       @return Increasing or not.
    */
    public boolean slopeUp(double first, double second, double third){
	double slope1 = second-first;
	double slope2 = third-second;
	if(slope2>slope1)return true;
	else return false;
    }

    /**
       Compute the slope of several points.
       @param points The points.
       @return The slope.
    */
    public double getSlope(double[] points){
	//this algorithm is not the best, but close enough
	//I hope you can understand. :)
	double avg = 0;
	double total = Math.round(points.length-1);
	//skip the first one
	for(int i=1; i<points.length; i++){
	    avg += points[i];
	}
	avg /= total;

	double slope = (avg-points[0])/total;
	return slope;
    }

    /**
       Calculate another Threshold for the dummy test.
       Because of statistics, we don't compute the first N-1 data.
	
       @param range the size of a block for moving average.
       @param  the list of data(curve) to be used
       @return threshold, the data(curve) of threshold
    */
    public double[] movingAverage(int range, double[] input){
	double[] ma = new double[input.length];
	double[] block = new double[range];
	int oldestIndex = range-1;//the oldest value of this block
		
	//read the first N data
	for(int i=0; i<range-1; i++){
	    ma[i] = input[i];
	    block[i] = input[i];
	}

	//calculate the threshold from N-1th data
	for(int i=range-1; i<input.length; i++){
	    block[oldestIndex] = input[i];
	    oldestIndex++;
	    if(oldestIndex >= range) oldestIndex = 0;
			
	    ma[i] = getAverage(block);
	}
		
	return ma;
    }

    /**
       Alert area for moving average.
       @param threshold the array of threshold
       @param data the array of data
       @return which position is that threshold > data
    */
    public boolean[] maAlert(double[] threshold, double[] data){
	int length;

	if(threshold.length>data.length)
	    length = data.length;
	else
	    length = threshold.length;

	boolean[] alert = new boolean[length];
	for(int i=0; i<length; i++){
	    if(threshold[i]<data[i])alert[i]=true;
	    else alert[i]=false;
	}
	return alert;
    }

    /**
       get the alert ranges from the three plots.
    */
    public AlertPoint[] collectAlert(boolean[] alert1
				     , boolean[] alert2
				     , boolean[] alert3){
	int length = alert1.length;
	if(length>alert2.length)length=alert2.length;
	if(length>alert3.length)length=alert3.length;

	AlertPoint[] alert = new AlertPoint[length];
	int alertCount = 0;
        AlertPoint tmpalert = new AlertPoint();
	boolean suspect = false;
	for(int i=0; i<length; i++){
	    if(suspect){
		if(!alert1[i] || !alert2[i] || !alert3[i]){
		    suspect=false;
		    tmpalert.addEnd(i);
		    alert[alertCount]=new AlertPoint();
		    alert[alertCount].addStart(tmpalert.start);
		    alert[alertCount].addEnd(tmpalert.end);
		    alertCount++;
		    tmpalert=new AlertPoint();
		}
		else if(i==length-1){
		    suspect=false;
		    tmpalert.addEnd(i);
		    alert[alertCount]=new AlertPoint();
		    alert[alertCount].addStart(tmpalert.start);
		    alert[alertCount].addEnd(tmpalert.end);
		    alertCount++;
		    tmpalert=new AlertPoint();
		}
	    }
	    else{
		if(alert1[i] && alert2[i] && alert3[i]){
		    suspect=true;
		    tmpalert.addStart(i);
		}
	    }
	}


	AlertPoint[] toReturn = new AlertPoint[alertCount];
	System.arraycopy(alert,0,toReturn,0,alertCount);
	return toReturn;
    }
	
    /**
       get the alert ranges from the three plots, the second form
    */
    public boolean[] collectAlert2(boolean[] alert1
				   , boolean[] alert2
				   , boolean[] alert3){
		
	int length = alert1.length;
	if(length>alert2.length)length=alert2.length;
	if(length>alert3.length)length=alert3.length;
		
	boolean[] toReturn = new boolean[length];
	for(int i=0; i<length; i++){
	    if(alert1[i]==true && alert2[i]==true && alert3[i]==true)
		toReturn[i] = true;
	    else toReturn[i] = false;
	}
		
	return toReturn;
    }

    /**
       get the alert ranges from the three plots, the second form
    */
    public boolean[] collectAlert2(boolean[] alert1
				   , boolean[] alert2){
		
	int length = alert1.length;
	if(length>alert2.length)length=alert2.length;
		
	boolean[] toReturn = new boolean[length];
	for(int i=0; i<length; i++){
	    if(alert1[i]==true && alert2[i]==true)
		toReturn[i] = true;
	    else toReturn[i] = false;
	}
		
	return toReturn;
    }
	
    /**
       get the alert ranges from the three plots, the second form
    */
    public boolean[] collectAlert2(AlertPoint[] alert, int length){
		
	boolean[] toReturn = new boolean[length];
	for(int i=0; i<length; i++)toReturn[i] = false;
		
	for(int i=0; i<alert.length; i++){
	    for(int j=alert[i].start;j<=alert[i].end;j++){
		toReturn[j] = true;
	    }
	}
		
	return toReturn;
    }
		
    /**
       get the alert message for the alert window.
    */
    public String[] getAlertMsg(int size, AlertPoint[] a, AlertPoint[] b){
	try{
	    String[] msg = new String[size];
	    int indexa= 0, indexb = 0;
	    int realDummyIndex=0;

	    for(int i=0; i<size; i++){
		//no range left
		if(indexa>=a.length || indexb>=b.length){
		    while(i<size){
			msg[i]=i+", Normal.";
			if(i==realDummy[realDummyIndex]){
			    msg[i]+="  Virus is here. (miss)    ";
			    realDummyIndex++;
			}
			i++;
		    }
		    break;
		}

		int starta=a[indexa].start;
		int enda=a[indexa].end;
		int startb=b[indexb].start;
		int endb=b[indexb].end;
	    
		if(i>=starta && i<=enda
		   && i>=startb && i<=endb){
		    msg[i]=i+", Alert";
		    if(i==realDummy[realDummyIndex]){
			msg[i]+="  Virus is here. (catch)";
			realDummyIndex++;
		    }
		}
		else{
		    msg[i]=i+", Normal.";
		    if(i==realDummy[realDummyIndex]){
			msg[i]+="  Virus is here. (miss)    ";
			realDummyIndex++;
		    }
		}

		if(i>enda)indexa++;
		if(i>endb)indexb++;

		//just make sure it won't out of array length
		if(realDummyIndex>=realDummy.length)
		    realDummyIndex=realDummy.length-1;

	    }

	    return msg;
	}
	catch(Exception e){
	    System.out.println("getAlertMsg error:"+e);
	    return null;
	}
    }

    /**
       get the alert message for the alert window.
    */
    public String[] getAlertMsg(int size, AlertPoint[] alert){
	try{
	    String[] msg = new String[size];
	    int alertIndex=0;
	    int realDummyIndex=0;

	    for(int i=0; i<size; i++){
		if(alertIndex<alert.length){
		    int s=alert[alertIndex].start;
		    int e=alert[alertIndex].end;
		    if(i<=e && i>=s){
			msg[i]=i+", Alert";
			if(i==e){
			    alertIndex++;
			}
		    }
		    else{
			msg[i]=i+", Normal.";
		    }
		}
		else{
		    msg[i]=i+", Normal";
		}
		if(realDummyIndex<realDummy.length){
		    if(i==realDummy[realDummyIndex]){
			msg[i]+="  Virus is here.";
			if(msg[i].indexOf("Alert")!=-1){
			    msg[i] += " (catch)";
			}
			else{
			    msg[i] += " (miss)    ";
			}
			realDummyIndex++;
		    }
		}

		if(msg[i].indexOf("Virus")==-1
		   &&msg[i].indexOf("Alert")!=-1){
		    msg[i] += " (false positive)";
		}
	    }

	    return msg;
	}
	catch(Exception e){
	    System.out.println("getAlertMsg2 error:"+e);
	    return null;
	}
    }


    //for get time from a string "2xxx-xx-xx xx:xx:xx"
   /* public long getMill(String in){
	String[] dt=in.split(" ");
	String[] date=dt[0].split("-");
	String[] time=dt[1].split(":");
	if(time[2].indexOf(".") != -1){
	    time[2] = time[2].substring(0,time[2].indexOf("."));
	}
	Calendar c=Calendar.getInstance();
	c.set(Integer.parseInt(date[0])
	      ,Integer.parseInt(date[1])-1
	      ,Integer.parseInt(date[2])
	      ,Integer.parseInt(time[0])
	      ,Integer.parseInt(time[1])
	      ,Integer.parseInt(time[2]));
		
	long ms = c.getTimeInMillis();
	return ms;
    }
*/
    //translate time fomr millisecond
    public String reMill(long in){
	Calendar c=Calendar.getInstance();
	c.setTimeInMillis(in);
	Date d = c.getTime();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String time = format.format(d);

	return time;
    }
}

