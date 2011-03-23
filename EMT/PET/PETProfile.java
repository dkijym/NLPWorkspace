/**
 * 
 */
package PET;

import java.util.Date;

import metdemo.Parser.EMTEmailMessage;

/**
 * @author Shlomo Hershkop
 *
 */
public interface PETProfile {

    /**
     * This adds a new email in to the profile <P>
     * Programs are responsible for keeping track of unique emails
     * @param newmessage
     * @return true if incorporated into the profile, false otherwise
     */
    public abstract boolean addEmail(EMTEmailMessage newmessage);
    
    /**
     * use the underlying email profile to score this email message
     * @param message
     * @return
     */
    public abstract int scoreEmail(EMTEmailMessage message);
    
    
    /**
     * Clears the data form the particular email profile
     *
     */
    public abstract void clearProfile();
    
    /**
     * gets the number of emails seen by the profile
     * @return the number of emails included in the profile
     */
    public abstract int getCount();
    
    /**
     * Gets the earliest aviable email date from our system
     * @return The Date or null if empty
     */
    public abstract Date getEarliestDate();
    
    /**
     * Gets the latest (most recent) email date
     * @return the Date or null if empty
     */
    public abstract Date getHighestDate();
    
    
}
