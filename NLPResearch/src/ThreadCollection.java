import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nlp.Mail.Thread;

// TODO: Auto-generated Javadoc
//TODO Add iterator limit
/**
 * This is a thread collection. 
 */
/**
 * @author reuben
 *
 */
public class ThreadCollection implements Iterator<Thread>, Iterable<Thread> {
	
	/** The i. */
	private int i;
	
	/** The threads from the directory */
	private File[] threads;
	
	/** The jaxb Context */
	private JAXBContext jc;
	
	/** The unmarshaller */
	private Unmarshaller u;
	
	/** The file path. */
	private String filePath;
	
	/** The limit. */
	private int limit;
	
	/** The allowed files. */
	private HashSet<String> allowedFiles;
	
	/**
	 * Instantiates a new thread collection.
	 *
	 * @param filePath the file path
	 * @throws JAXBException the jAXB exception
	 */
	public ThreadCollection(String filePath) throws JAXBException
	{
		this.filePath = filePath;
		jc = JAXBContext.newInstance( "nlp.Mail" );
		u = jc.createUnmarshaller();
		
		i=0;
	
		File dir = new File(filePath);
		System.out.println(dir.isDirectory());	
		threads = dir.listFiles();			
	}
	
	/**
	 * Instantiates a new thread collection.
	 *
	 * @param filePath the file path
	 * @param limit the limit
	 * @throws JAXBException the jAXB exception
	 */
	public ThreadCollection(String filePath, int limit) throws JAXBException
	{
		this(filePath);
		this.limit = limit;
	}
	

	
	/**
	 * Instantiates a new thread collection.
	 *
	 * @param directory the directory
	 * @param fileNames the file names
	 */
	public ThreadCollection(String directory, String[] fileNames)
	{
		//TODO add only allowed files	
		allowedFiles = new HashSet<String>();
		//Arrays.
	}
	
	/**
	 * Checks to see if the file is valid.
	 *
	 * @param f The file to check
	 * @return true, if is valid
	 */
	private boolean isValid(File f)
	{
		String absPath = f.getAbsolutePath();
		return 	!absPath.contains(".bak") && (absPath.contains("train") || absPath.contains("test"));
	}
	
	/**
	 * This returns if the length is not less than total number of threads.
	 * This could be wrong if n-1, n-2 are all invalid.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasNext()
	{
		System.out.println("Has next " + (i < threads.length && i < limit));
		return i < threads.length && i < limit;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Thread next() 
	{
		
		while(i < threads.length && i < limit && !isValid(threads[i]))
		{
			i++;
		}
		i++;
		Thread rtnThread = null;
		try {
			//System.out.println(threads[i-1].getAbsolutePath());
			
			if(i != threads.length || i != limit)
			{
				rtnThread = (Thread)u.unmarshal( new FileInputStream( threads[i-1].getAbsolutePath()) );
				rtnThread.setID(threads[i-1].getName());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			System.out.println("Issues with "+ threads[i-1].getAbsolutePath());
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return rtnThread;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Thread> iterator() {
		Iterator<Thread> rtn = null;
		try {
			rtn = new ThreadCollection(filePath,limit);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return rtn;
	}
	
}
