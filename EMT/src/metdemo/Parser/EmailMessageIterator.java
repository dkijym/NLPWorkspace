/**
 * 
 */
package metdemo.Parser;

import java.util.Iterator;

/**
 * @author Shlomo Hershkop
 * @date Oct 2005
 * 
 * The idea is to encode an iterator for each type of email message format.
 * 
 *
 */
public interface EmailMessageIterator extends Iterator {

	/**
	 * Keep track of how many messages processed
	 * @return a number which says how many messages have we seen so far
	 */
	public abstract int seenSoFar();
	
	
}
