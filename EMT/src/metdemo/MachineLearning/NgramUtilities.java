/*
 * Created on Jan 27, 2005
 * 
 * The idea is to consolidate the tools needed by ngram and others, but move it out of the learner
 *
 */
package metdemo.MachineLearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import metdemo.dataStructures.contentDis;

/**
 * @author Shlomo Hershkop
 *
 * Utilities needed by ngram analysis
 * 
 */

public abstract class NgramUtilities {

	
	/**
	 * Convert multiple spaces into one.
	 * @param s
	 * @return
	 */
/*	private static String deleteDoubleSpace(String s) {
		//String temp = s;//in case no string, will return itself.
		//s = s.trim();
		//int pos = -1;
		//while ((pos = s.indexOf("  ")) > 0) {
		//	temp = s.substring(0, pos) + s.substring(pos + 1);
		//	s = temp;
		//}

		return s.trim().replaceAll("  ","");
	}*/
	
	public static double hashmapLength(final HashMap ht1) {
		double val;
		double sum = 0;
		Iterator keys = ht1.keySet().iterator();
		while (keys.hasNext()) {
			//t = (Double)ht2.get((String)keys.next());//sub);
			//val = t.doubleValue();
			val = ((Double) ht1.get(keys.next())).doubleValue();
			sum += val * val;
			// val*val;
		}
		return sum;
	}
	
//	sort according to distance, increasingly
	//bubble sort
	public static final ArrayList sort(ArrayList<contentDis> V) {

		//Vector vec = new Vector();

		int size = V.size();
		if (size <= 1)
			return V;

		//vec.addElement(V.elementAt(0));
		//bubble sort
		//int j=0;
		double d, d2;
		for (int i = 1; i < size; i++) {
			contentDis u = (contentDis) V.get(i);
			d = u.dis;
			for (int j = i + 1; j < size; j++) {
				d2 = ((contentDis) V.get(j)).dis;
				if (d > d2) {
					contentDis temp = V.get(i);
					V.set(i, V.get(j));
					V.set(j, temp);
					d = d2;
				}

				//vec.insertElementAt(u, j);
				//break;
			}

		}// for i

		// if greater than any one esle

		return V;
	}

	
	/**
	 * get the n-gram of the string s
	 * 
	 * @param s
	 *            the string to split up
	 * @param n
	 *            the size of the ngram
	 */
	static final HashMap<String,Double> getNGram(final String s,final int n) {
		HashMap<String,Double> ht = new HashMap<String,Double>();
		String sub ;

		int i = 0, len = s.length();
		//total length of s is smaller than n, should give more space
		if (len < n) {
			//System.out.print("triggered");
			sub = s;
			for (int j = 0; j < n - len; j++)
				sub = sub + " ";
				ht.put(new String(sub),new Double(1));
				return ht;
				//n = len;//cause we are only getting one anyway
		}

		int number_ngrams = s.length() - n + 1; //number of ngrams
		Double dvalue;
		for (i = 0; i < number_ngrams; i++) {
			sub = s.substring(i, i + n);
			if ( (dvalue = ht.get(sub)) != null) {
				
			//	Double t = (Double)ht.get(sub).d;
				//double val = t.doubleValue();
				//dvalue = ((Double)ht.get(sub)).doubleValue();
				ht.put(new String(sub), dvalue.doubleValue() + 1);
			} else {
				ht.put(new String(sub), new Double(1));
			}

		}

		//count the percentage
		Iterator keys = ht.keySet().iterator();
		while (keys.hasNext()) {

			sub = new String((String) keys.next());
			//	    Double t = (Double)ht.get(sub);
			// double val = t.doubleValue()/num;
			ht.put(sub, ht.get(sub) / number_ngrams);
		}

		return ht;
	}

	static final HashMap<String,Double> getNGramLimited(final String s,final int n, final int limit) {
		HashMap<String,Double> ht = new HashMap<String,Double>();
		String sub;

		int i = 0, len = s.length();
		//total length of s is smaller than n, should give more space
		if (len < n) {
			sub = s;
			for (int j = 0; j < n - len; j++)
				sub = sub + " ";
				ht.put(new String(sub),new Double(1));
				return ht;
				//n = len;//cause we are only getting one anyway
		}

		int number_ngrams = s.length() - n + 1; //number of ngrams
		int actual_ngrams =0;
		Double dvalue;
		for (i = 0; i < number_ngrams; i++) {
			sub = s.substring(i, i + n);
			if(sub.hashCode()%limit!=0)
				continue;
			
			
			actual_ngrams++;
			if ( (dvalue = ht.get(sub))!=null) {
				
			//	Double t = (Double)ht.get(sub).d;
				//double val = t.doubleValue();
				
				ht.put(new String(sub), dvalue.doubleValue()+1);
			} else {
				ht.put(new String(sub), new Double(1));
			}

		}
		//System.out.print(number_ngrams+"="+actual_ngrams+" ");
		//count the percentage
		Iterator keys = ht.keySet().iterator();
		while (keys.hasNext()) {

			sub = new String((String) keys.next());
			//	    Double t = (Double)ht.get(sub);
			// double val = t.doubleValue()/num;
			ht.put(sub,  ht.get(sub) / actual_ngrams);
		}

		return ht;
	}

	
	
}
