/*
 * Created on Feb 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

/**
 * @author Shlomo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
account with calculated avg comm time
*/
public class userComm{
	public String acct; //email account
	public int number; //how many emails exchanged
	public double time; //avg time
	public double vipValue = -1; //vip value
	
	public userComm(String s, int n, double d){
		acct = s;
		number = n;
		time = d;	
	}
}