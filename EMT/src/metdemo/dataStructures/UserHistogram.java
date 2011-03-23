/*
 * Created on May 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

import java.text.SimpleDateFormat;

/**
 * @author shlomo
 *
 **/

//class to store the histogram information about a user

public class UserHistogram {

	private String user;

	private double[] Histogram;//size if 24, for 24 hours

	private int periodNum = 24; //default is 24 period for a histogram

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	//constructor
	public UserHistogram(double[] H, String s) {
		Histogram = new double[H.length];
		for (int i = 0; i < H.length; i++) {
			Histogram[i] = H[i];
		}
		user = s;
	}

	public UserHistogram() {
		Histogram = new double[0];
	}

	public final void setHistogram(double[] H) {
		Histogram = new double[H.length];
		for (int i = 0; i < H.length; i++) {
			Histogram[i] = H[i];
		}
	}

	public final double[] getHistogram() {
		return Histogram;
	}

	public final void setUser(String s) {
		user = s;
	}

	public final void setPeriodNum(final int n) {
		periodNum = n;
	}

	//print out the histogram, for test
	public final void showHistogram() {

		System.out.println("the histogram has " + Histogram.length + " bins:");
		for (int i = 0; i < Histogram.length; i++) {
			System.out.print(Histogram[i] + ", ");
		}
		System.out.println("");
	}

}