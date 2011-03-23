/**
 * 
 */
package metdemo.DataBase;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Shlomo Hershkop
 * Fall 2006
 * 
 * Main idea is to create a cache for recently used data over a static database of emails.
 * 
 * each application in EMT can access either a dynamic LRU n level cache of short information
 * or static name based caches (expected to be replaced by sting id) using streams of data.
 * 
 * on data change, expected to call cache flush (clearallfromcache
 *
 */
public abstract class CacheManagerInterface {

	
	
	/**
	 * This will Clear the cache contents
	 * @return
	 */
	abstract public boolean clearAllFromCache();
	
	/**
	 * Deletes specific cached object 
	 * @param ID
	 * @param internalObject if this is an internal cache object or not
	 * @return
	 */
	abstract public boolean clearCacheObject(String ID,boolean internalObject);
	
	/**
	 * Initliaze the content of the cache and set it up, if successful should keep track internally
	 * @param Location
	 * @param name
	 * @return
	 */
	abstract public boolean initializeCache(String Location,String name);
	
	/**
	 * Will be used to get the cache name
	 * @return the cache name as a String
	 */	
	abstract public String getName();
	
	
	/**
	 * return id of location
	 * @return
	 */
	abstract public String getLocation();
	
	
	/**
	 * Returns a unique cached object
	 * @param ID 
	 * @param clientID
	 * @return the object found or null if not present
	 */
	abstract Object getCacheObject(String ID, int clientID);
	
	/**
	 * Set an object into the cache
	 * @param ID
	 * @param clientID
	 * @param somethingtoSave
	 * @param persist if the object should persist across sessions
	 * @return
	 */
	abstract boolean setCacheObject(String ID, int clientID,Object somethingtoSave, boolean persist);
	
	/**
	 * fetch file for getting information
	 * @param ID
	 * @param clientID
	 * @return
	 */
	abstract ObjectOutputStream getOutputStream(String ID, int clientID);
	
	/**
	 * store a cached file
	 * @param ID
	 * @param clientID
	 * @return
	 */
	abstract ObjectInputStream getInputStream(String ID, int clientID);
	
}
