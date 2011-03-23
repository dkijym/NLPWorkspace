/*
 * Created on Jan 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

/**
 * @author Shlomo 
 * 
 * this class is to count freq of characters in a string.
 *         used because we want fast way to sort things.
 */
public class CharFreq implements Comparable {

	private char c;

	private int count;

	/** constructor using the character */
	public CharFreq(char ch) {
		count = 0;
		c = ch;
	}

	public CharFreq(char ch, int n) {
		count = n;
		c = ch;
	}

	public String toString() {
		return "" + c;
	}

	public boolean equals(Object o) {

		if (!(o instanceof CharFreq))
			return false;
		CharFreq n = (CharFreq) o;

		return count == n.count;
	}

	public int compareTo(Object o) {
		if (!(o instanceof CharFreq))
			return -1;

		CharFreq n = (CharFreq) o;
		if (n.count < count)
			return -1;
		else if (n.count > count)
			return 1;
		else
			return 0;
	}

}