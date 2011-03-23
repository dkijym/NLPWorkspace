/*
 * This class is used to compare the histograms, return the distance 
 *
 * Ke Wang, 07/08/02
 */


package metdemo.Tools;


//import java.io.*;
//import java.math.*;


// All the passed in Vectors have length 24, for 24 hours, 
// might need to reconstruct them according to the comparison period
public abstract class DistanceCompute{
	
    private static int periodNum = 24; //by default is 12, start from 7am, can be changed later

    
    
    // re-group the vector according to periodNum
    public static final double[] reGroup(double[] V1)
    {

	if(periodNum==24) return V1; //don't need to regroup

//	Vector vec = new Vector(periodNum);
double[]vec = new double[periodNum];
	
	//for(int i=0;i<periodNum;i++)
	//   vec.insertElementAt((new Double(0)),i);
	
	int n = 24/periodNum;
	int index =6; //start from 7am

	for(int i=0;i<periodNum;i++)
	{
	    double d =0;
	    for(int j=0;j<n;j++)
	    {
		int temp = (index%24);
		d+= V1[temp];
			//((Double)V1.elementAt(temp)).doubleValue();
		index++;
	    }
	    vec[i] = d;
	    //vec.addElement(new Double(d));
	}

	return vec;
    }


    // return a normalized vector, ie. the sum of element is 1
    public static final double[] normalize(final double[] V)
    {
    double []vec = new double[periodNum];	
	//Vector vec = new Vector(periodNum);
	//Double zero = new Double(0);
	//for(int i=0;i<periodNum;i++)
	//   vec.insertElementAt(zero,i);

	double sum =0;
	
	for(int i=0;i<periodNum;i++)
	{
		sum += V[i];
		//sum += ((Double)V.elementAt(i)).doubleValue();
	}
	// if sum is 0, can't divide using it
	if(sum ==0)
	    return V;


	for(int i=0;i<periodNum;i++)
	{
	    double d = V[i];//((Double)V.elementAt(i)).doubleValue();
	    double dd = d/sum;

	    vec[i] = dd;
	    //vec.addElement(new Double(dd));
	}
	    
	return vec;
    }


    // return a cumulative distribution
    public static final double[] cumulate(double[] V){
	
    	double[]vec = new double[periodNum];	
	//Vector vec = new Vector(periodNum);
	//	for(int i=0;i<periodNum;i++)
	//  vec.insertElementAt((new Double(0)),i);
	
	double sum =0;
	for(int i=0;i<periodNum;i++)
	{
	    sum += V[i];//((Double)V.elementAt(i)).doubleValue();
	    vec[i] = sum;
	    //vec.addElement(new Double(sum));
	}
	
	return vec;
    }



    public static final double getL1(double[] V1, double[] V2)
    {
	double distance =0;
	double[] Vec1 = normalize(reGroup(V1));
	double[] Vec2 = normalize(reGroup(V2));

	for(int i=0;i<periodNum;i++)
	{
	    double d1 = Vec1[i];//((Double)Vec1.elementAt(i)).doubleValue();
	    double d2 = Vec2[i];//((Double)Vec2.elementAt(i)).doubleValue();
	    
	    distance +=  Math.abs(d1-d2);
	}
	
	return distance;
    }
    
    //use both periodNum= 4, 24 to get the avg
    public static final double L1_form(double[] V1, double[] V2){

	int temp  = periodNum;
	periodNum = 24;
	double d1 = getL1(V1,V2);

	periodNum = 4;
	double d2 = getL1(V1,V2);

	periodNum = temp;
	
	return (d1+d2)/2;
    }
    


    public static final double getL2(double[] V1, double[] V2)
    {
	double distance =0;
	double[] Vec1 = normalize(reGroup(V1));
	double[] Vec2 = normalize(reGroup(V2));

	for(int i=0;i<periodNum;i++)
	{
	    double d1 = Vec1[i];//((Double)Vec1.elementAt(i)).doubleValue();
	    double d2 = Vec2[i];//((Double)Vec2.elementAt(i)).doubleValue();
	    
	    distance +=(d1-d2)*(d1-d2);
	}
	
	return distance;
    }
    
    public static final double L2_form(double[] V1, double[] V2){

	int temp = periodNum;

	periodNum = 24;
	double d1 = getL2(V1,V2);

	periodNum = 4;
	double d2 = getL2(V1,V2);

	periodNum = temp;

	return (d1+d2)/2;
    }


    public static final double KS_test(double[] V1, double[] V2){

	double distance =0;
	double[] Vec1 = cumulate(normalize(reGroup(V1)));
	double[] Vec2 = cumulate(normalize(reGroup(V2)));

	for(int i=0;i<periodNum;i++){

	    double d1 = Vec1[i];//((Double)Vec1.elementAt(i)).doubleValue();
	    double d2 = Vec2[i];//((Double)Vec2.elementAt(i)).doubleValue();

	    double d = Math.abs(d1-d2);
	    if(d>distance)
		distance = d;
	}

	return distance;
    }

    
    // histogram quadratic distance
    public static final double getQuadratic(double[] V1, double[] V2){

	double distance =0;
	double[] Vec1 = normalize(reGroup(V1));
	double[] Vec2 = normalize(reGroup(V2));
	
	
	for(int i=0;i<periodNum;i++){

	    double d1 = Vec1[i];//((Double)Vec1.elementAt(i)).doubleValue();
	    double d2 = Vec2[i];//((Double)Vec2.elementAt(i)).doubleValue();

	    for(int j=0;j<periodNum;j++){

		double r1 = Vec1[j];//((Double)Vec1.elementAt(j)).doubleValue();
		double r2 = Vec2[j];//((Double)Vec2.elementAt(j)).doubleValue();

		double aij = Math.abs(i-j)+1;
		
		distance +=(d1-d2)*aij*(r1-r2);
	    }
	}
	
	return Math.abs(distance);
    }

public static final double quadratic(double[] V1, double[] V2){

	int temp = periodNum;

	periodNum = 24;
	double d1 = getQuadratic(V1, V2);
	
	periodNum = 4;
	double d2 = getQuadratic(V1, V2);
	periodNum = temp;

	return (d1+d2)/2;
    }



    // histogram mahalanobis distance
    // V1 for profile, V2 for recent histogram
    public static final double mahalanobis(double[] V1, double[] V2, double[] stdDev){

	double distance =0;
	//Vector Vec1 = normalize(reGroup(V1));


	double sum =0;
	for(int i=0;i<24;i++)
	    sum += V2[i];//((Double)V2.elementAt(i)).doubleValue();
	
	double[] Vec2;

	if(sum==0){

	    Vec2 = new double[24];//new Vector(24);
	    for(int i=0;i<24;i++)
	    {
	    	Vec2[i] = ((double)1/24);//Vec2.addElement(new Double((double)1/24));
	    }
	}
	else    
	    Vec2 = normalize(reGroup(V2)); //used as weight for final result
	
	for(int i=0;i<24;i++){
	    
	    double r1 = V1[i];//((Double)V1.elementAt(i)).doubleValue();
	    double r2 = V2[i];//((Double)V2.elementAt(i)).doubleValue();
	    double t = stdDev[i];//((Double)stdDev.elementAt(i)).doubleValue();
	    double weight = Vec2[i];//((Double)Vec2.elementAt(i)).doubleValue();
	  
	    // System.out.println("i="+i+", avg="+r1+", stddev = "+t+", weight="+weight);
	    if(t<0.1) t=0.1; // if t is 0, treat is as a very small number
	    distance += Math.abs(r1-r2)*weight/t;
	}
	
	return distance;
    }

   

    public static final double getDistance(double[] V1,  double[]V2, String method){

	if(method.equals("L1-form"))
	    return L1_form(V1,V2);
	else if (method.equals("L2-form"))
	    return L2_form(V1,V2);
	else if (method.equals("KS-test"))
	    return KS_test(V1,V2);
	else if (method.equals("quadratic"))
	    return quadratic(V1,V2);
	else 
	    return L1_form(V1,V2);
    }
    

    public static final double getDistance(double[] V1, double[] V2, double[] dev, String method){

	if (method.equals("mahalanobis"))
	    return mahalanobis(V1,V2,dev);
	 //by default, give dev as 1
	    
	    double[] V = new double[24];
	    for(int i=0;i<24;i++)
	    	V[i] = 1;//	V.addElement(new Double(1));

	    return mahalanobis(V1,V2,V);
	
    }
}
