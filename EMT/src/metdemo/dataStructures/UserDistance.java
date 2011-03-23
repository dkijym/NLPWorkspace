/*
 * Created on Jan 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

/**
 * @author Shlomo 
 * 
 * moved out of similiar users
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UserDistance {
	 private String name;
	    private String distance;
	    private double distNum;

	    public final String getName(){
		return name;
	    }

	    public final String getDistance(){
		return distance;
	    }

	    public final double getDistNum(){
		return distNum;
	    }

	    public UserDistance(String n, double d){
		name = n;
		distNum = d;
		distance = (new Double(d)).toString();
	    }
}
