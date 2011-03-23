/**
 * 
 */
package metdemo.DataBase;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import metdemo.Tools.Utils;

/**
 * @author Shlomo Hershkop
 * 
 * file based cache to store persistant information related to a specific database version and data status<P>
 * 
 * two types of cache object, files which the user will admin, but we will store
 * and n level cache for per process small info persistant and non persistant
 * 
 * 
 * We will be using an underlying file system to store seperated files as caches
 *
 *
 *we will flush the cache on db changes (deletes)
 */
public class EMTCacheFileBased extends CacheManagerInterface {

	/* will see if the cache is valid */
	private boolean validState;
	private String p_locationCache;
	private String p_name;
	private Hashtable<String, Object> inmemoryHashCache;
	private Hashtable<String, Boolean> persistHashTable;
	
	EMTCacheFileBased(String locationCache, String Name){
		validState = false;
		p_name = Name;
		p_locationCache = locationCache;
		initializeCache(locationCache, Name);
		
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see metdemo.DataBase.CacheManagerInterface#clearCache()
	 */
	@Override
	public boolean clearAllFromCache() {
		File locationFile = new File(p_locationCache,p_name);
		
		if(!locationFile.exists())
		{
			validState = false;
			return true;
		}
		//need to delete cache;
		System.out.println("Want to delete " + locationFile.getAbsolutePath());
		Utils.SystemIndependantRecursiveDeleteMethod(locationFile.getAbsolutePath(), null, null, true, true);
		
		validState = false;
		return true;
	}

	/* (non-Javadoc)
	 * @see metdemo.DataBase.CacheManagerInterface#getCacheObject(java.lang.String, int)
	 */
	@Override
	Object getCacheObject(String ID, int clientID) {
		return inmemoryHashCache.get(ID);
	}

	/* (non-Javadoc)
	 * @see metdemo.DataBase.CacheManagerInterface#getName()
	 */
	@Override
	public String getName() {
		
		return p_name;
	}

	/* (non-Javadoc)
	 * @see metdemo.DataBase.CacheManagerInterface#initializeCache(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean initializeCache(String Location, String name) {
		//will initialize the inmemoryhash
		inmemoryHashCache = new Hashtable<String, Object>();
		persistHashTable = new Hashtable<String, Boolean>();
		//need to make sure location exists
		File locationFile = new File(Location,name);
		
		if(!locationFile.exists())
		{
			return locationFile.mkdirs();
		}
		//created initial location in memory
		validState = true;
		return true;
	}

	/* (non-Javadoc)
	 * @see metdemo.DataBase.CacheManagerInterface#setCacheObject(java.lang.String, int, java.lang.Object)
	 */
	@Override
	boolean setCacheObject(String ID, int clientID, Object somethingtoSave,boolean persist) {
		inmemoryHashCache.put(ID, somethingtoSave);
		persistHashTable.put(ID, persist);
		return true;
	}

	@Override
	public boolean clearCacheObject(String ID, boolean internalObject) {
		
		//need to check if internal to hashtable or external on file
		if(internalObject){
			inmemoryHashCache.remove(ID);
			persistHashTable.remove(ID);
			
		}
		else{
			File delfile = new File(p_locationCache+File.separator+p_name+File.separator+ID);
			
			if(delfile.delete()){
				System.out.println("Deleted: " + ID);
			}else{
				System.out.println("Problem deleting "+ ID);
				return false;
			}
			
			
		}
		return true;
	}

	
	/**
	 * short test of setting up a sample data cache and test of each of the functions.
	 * @param args
	 */
	
	public static void main(String[] args) {
		System.out.println("Welcome to the test of the cache system");
		
		EMTCacheFileBased emtc = new EMTCacheFileBased("cache/","test123");
		
		
		
		
		emtc.clearAllFromCache();
	}




	@Override
	public String getLocation() {
		return p_locationCache;
	}




	@Override
	ObjectInputStream getInputStream(String ID, int clientID) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	ObjectOutputStream getOutputStream(String ID, int clientID) {
		// TODO Auto-generated method stub
		return null;
	}
}
