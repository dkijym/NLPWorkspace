/**
 * interface for general access of email collection files
 */
package metdemo.Parser;

import java.util.Iterator;

/**
 * 
 * Interface to access some set of email messages encoded in a file in some prespecified format<P>
 * Specific format decoders will be written and used in an iterator framework
 * @see metdemo.Parser.EmailMessageIterator
 * 
 * @author Shlomo Hershkop
 * Aug 2005
 *
 */

public interface EmailFileInterface {


	public static final int MBOX = 0;
	public static final int DBX = 1;
	public static final int PST2002 = 2;
	public static final int PST2003 = 3;
	public static final int TEXTEXPORT = 4;
	public static final int UNKNOWN = 5;
	
	
	public abstract String getNameandPath();
	
	public abstract String getName();
	
	public abstract int getMessageCount() throws EmailFileOpenException;
	
	public abstract long getHash();
	
	public abstract boolean open() throws EmailFileOpenException;
	
	public abstract boolean close() throws EmailFileClosedException;
	
	/**
	 * Return an Iterator over the file list, the file MUST call open first.
	 * @return a Iterator for processing the mail messages
	 */
	public abstract Iterator getMessageIterator() throws EmailFileOpenException;
	
	/*
	 * Return the predefined message format
	 */
	public abstract int getMessageStoreageFormat();
	
	/**
	 *  Sets the last error for the processing file.
	 *
	 */
	public abstract void setErrorMessage(String error);
	
	/**
	 * Extract the last error message.
	 * @return
	 */
	public abstract String getErrorMessage();
	
}
