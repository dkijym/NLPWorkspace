/**
 * 
 */
package PET;

/**
 * @author crash
 *
 */
public interface PETEmailConnection {

    /**
     * some status codes set by the system
     */
    public final int loadFailure = 0;
    public final int saveFailure = 1;
    public final int nameConflict = 2;
    public final int alreadyOpen = 3;
    public final int notOpen = 4;
    public final int everythingOK = 5;
    
    
    
    /**
     * Default constructor sets the database name
     * @param name the name of the email database to access
     */
    public abstract void setupName(String name);
    
    /**
     * startup an email data base refered to by name
     * @return true on startup success
     */
    public abstract boolean startPETDB();
    
    /**
     * shuts down the database
     * @return true on success
     */
    public abstract boolean closePETDB();
    
    /**
     * committs the database to disk
     * @param petdbname of the database
     * @return true on success
     */
    public abstract boolean savePETDB(String petdbname);
    
    /**
     * Loads the database off the disk
     * @param petdbname of the database
     * @return true on success
     */
    public abstract boolean loadPETDB(String petdbname);
    
    /**
     * Return the current status of the database, will be set on success/failure of any methods 
     * @return the int code of the status
     */
    public abstract int getStatus();
    
        
}
