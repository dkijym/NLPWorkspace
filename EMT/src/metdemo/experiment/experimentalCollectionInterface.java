/**
 * 
 */
package metdemo.experiment;

import java.util.Iterator;

import metdemo.Parser.EMTEmailMessage;

/**
 * @author Shlomo
 * @date Feb 2006
 * 
 * This interface defines an EMT experiment file which will contain emails to train or test upon 
 *
 */
public interface experimentalCollectionInterface {

	/**
	 * Fet the number of emails associated with the experiment
	 * @return the number of emails in the file
	 */
	public int getCount();
	
	/**
	 * Will clear the contents of a specific file
	 * @return true on success
	 */
	public boolean clear();
	
	
	/**
	 * will set the filename
	 * @param name is the filename to be used from this point on
	 */
	
	public void setName(String name);
	
	/**
	 * will save the contents of the file to some representation
	 * @return true on success
	 * @throws experimentalCollectionFileException if anything goes wrong
	 */
	public boolean save() throws experimentalCollectionFileException;
	
	/**
	 * will attempt to load the data from the file
	 * @throws experimentalCollectionFileException if the data isnt correct this can include version and data integrity
	 */
	public void loadDataFromFile() throws experimentalCollectionFileException;
	
	
	/**
	 * sets a comment to be associated with the file
	 * @param comment
	 */
	
	public void setComment(String comment);
	
	/**
	 * returns the comment
	 * @return
	 */
	public String getComment();
	
	/**
	 * Will return an iterator to run over the email file list
	 * @return the iterator
	 */
	public Iterator getIterator();
	
	/**
	 * Add an email to the list
	 * @param newone an email to be added to the file
	 */
	public void addEmail(EMTEmailMessage newone);
	
	/**
	 * Add an email to the list
	 * @param newones an array of  emails to be added to the file
	 */
	public void addEmailSet(EMTEmailMessage []newones);
	
	
	
}
