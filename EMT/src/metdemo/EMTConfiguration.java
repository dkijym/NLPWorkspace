/*
 * Created on Sep 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import metdemo.DataBase.DatabaseManager;

/**
 * @author Shlomo Hershkop
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EMTConfiguration {

	/**
	 *  
	 */
	public static final String CONFIGNAME = "CONFIG";
	public static final String WORDLIST = "HOTWORDS";
	public static final String STOPLIST = "STOPWORDS";
	public static final String CACHE = "CACHE";
	public static final String SHOW = "SHOW";
	public static final String DBHOST = "DBHOST";
	public static final String DBNAME = "DBNAME";
	public static final String DBUSERNAME = "DBUSERNAME";
	public static final String DBPASSWORD = "DBPASSWORD";
	public static final String DBPATH = "DBPATH";
	public static final String ALIASFILENAME = "ALIASFILENAME";
	public static final String DEFAULT_CONF_FILE = "EMTconfig.txt";
	public static final String DOS_BATCH_FILE = "DOS_BATCH_FILE";
	public static final String DBTYPE = "DBTYPE";
	public static final String SCHEME_COLOR = "EMT_SCHEME_COLOR";
	public static final String STARTWINDOW = "STARTINGWINDOW";
	public static final String DBDATEINFO = "DATEINFO";
	private Hashtable<String,String> proproperties;
	//private String variables[] ={""
	private File filename;

	public EMTConfiguration() {
		proproperties = new Hashtable<String,String>();
		setDefault();
	}

	//	public EMTConfiguration(Properties prop) {
	//		proproperties = new Hashtable();
	//	}

	public String getProperty(String key) {
		return (String) proproperties.get(key);
	}
	
	public boolean hasProperty(String key)
	{
		return proproperties.containsKey(key);
	}

	public void putProperty(String key, String value) {

//		System.out.println("put key: " + key);	
		proproperties.put(key, value);
	}
	
	public void deleteProperty(String key){
		proproperties.remove(key);
	}

	public String getFileName() {
		if (filename == null) {
			return null;
		}
		return filename.toString();
	}

	/**
	 * Save a config file on the disk
	 * 
	 * @param name
	 *            the file to be saved
	 * @return on success true
	 */
	public boolean saveFileToDisk(String fname) {
		
		
		
		//TODO: do we need to prompt for overwrite??
		try {
			File name = new File("config",fname);
			
			File test = name.getParentFile();
			if(!test.exists())
			{
				test.mkdirs();
			}
			
			filename = name;
			
			PrintWriter bwrite = new PrintWriter(new FileWriter(filename));

			for(Enumeration e = proproperties.keys();e.hasMoreElements();)
			{
				String key = (String)e.nextElement();
				bwrite.println(key + " = "+(String)proproperties.get(key));
			}
			
			bwrite.flush();
			bwrite.close();
			
		} catch (IOException ioe) {
			System.out.println(ioe);
			return false;
		}
		
	
		
		
		return true;
	}

	/**
	 * Load a configuration file off the disk
	 * 
	 * @param name
	 *            the file to be saved
	 * @return on success true
	 */
	public boolean loadFileFromDisk(String fname) {
		//check if its there
		File name = new File("config",fname);
		if (!name.exists()) {
			return false;
		}

		try {
			BufferedReader bread = new BufferedReader(new FileReader((name)));
			String input = new String();
			int offset = 0;
			int linecount = 0;
			while ((input = bread.readLine()) != null) {
				//ignore commented lines
				if (input.startsWith("#") || input.startsWith("REM")) {
					continue;
				}
				offset = input.indexOf("=");
				//bad format errors
				if (offset < 0) {
					System.out.println("no equals found on line " + linecount);
					return false;
				}
				String key = input.substring(0, offset).trim();
				String value = input.substring(offset + 1).trim();
				//test out
				//System.out.println(key + " = " + value);
				putProperty(key, value);
				linecount++;
			}//end while
			bread.close();
		} catch (IOException ioe) {
			System.out.println("22"+ ioe);
			return false;
		}

		return true;
	}
	
	
	public void setDefault(){
		proproperties.put(DOS_BATCH_FILE,"emtclient.bat");
		proproperties.put(ALIASFILENAME,"etc/aliases");
		proproperties.put(WORDLIST,"etc/hotwords.txt");
		proproperties.put(STOPLIST,"etc/stoplist.txt");
		proproperties.put(CACHE, "cache/");
		proproperties.put(DBHOST,"127.0.0.1");	
		proproperties.put(DBTYPE,DatabaseManager.sDERBY);	
		proproperties.put(SCHEME_COLOR,"2");	
	}
	
	/**
	 * Will return a list of all avialable config files
	 * @return
	 */
	public static String[] getAllConfigFiles()
	{
		File dir1 = new File("config");
		
		if(!dir1.exists())
		{
			String are[] = new String[1];
			are[0] = DEFAULT_CONF_FILE;
			return are;
		}
		
		String []listing = dir1.list();
		if (listing == null || listing.length==0)
		{
			listing = new String[1];
			listing[0] = DEFAULT_CONF_FILE;
		}
		
		
		
		return listing;
		
	}
	
	
}