/**
 * 
 */
package metdemo.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Implimentation of MBOx file format for email processing
 * 
 * inclludes standard mbox and netscape cousin.
 * 
 * @author Shlomo Hershkop August 2005
 * 
 */
public class MBOXFileParser implements EmailFileInterface {

	String filename; // the file to open and process

	String pathString; // the path to the file

	int estimatedMailCount = -1;

	String errorString;
	
	boolean isOpen = false;

	BufferedReader filehandle;
	
	/**
	 * Sets up a parser on a specific file and path
	 * 
	 * @param path
	 *            the full system path name for the file
	 * @param fname
	 *            the actual folder name
	 */
	public MBOXFileParser(String path, String fname) {

		pathString = new String(path);
		filename = new String(fname);
		filehandle = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getNameandPath()
	 */
	public String getNameandPath() {
		return this.pathString + File.pathSeparator + this.filename;
	}

	/**
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getName()
	 */
	public String getName() {
		return this.filename;
	}

	/**
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getMessageCount() Will make the assumption that the file hasnt changed in
	 *      size.....can fix this by taing file hash and caching the results
	 */
	public int getMessageCount() throws EmailFileOpenException {
		if (this.estimatedMailCount != -1) {
			return this.estimatedMailCount;
		}
		// now we need to count it.
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(this.pathString, this.filename)));
			int count = 0, lines = 0;
			System.out.println("from patter: " + EMTParser.GENERALFROM);
			Pattern p_topfrom = Pattern.compile(EMTParser.GENERALFROM, Pattern.CASE_INSENSITIVE);
			Matcher m;
			String sreadline = bf.readLine();
			while (sreadline != null) {
				lines++;
				m = p_topfrom.matcher(sreadline);
				if (m.matches()) {
					count++;
					System.out.println(sreadline);
				}
				sreadline = bf.readLine();
			}
			estimatedMailCount = lines;
			bf.close();
			System.out.println("number of lines in file " + lines);
			System.out.println("count is now " + count);
		} catch (FileNotFoundException fnf) {
			throw new EmailFileOpenException(fnf.getMessage());
		} catch (IOException ioe) {
			System.out.println(ioe);
		}

		return estimatedMailCount;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getHash()
	 */
	public long getHash() {
		// TODO Auto-generated method stub
		return filename.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#open()
	 */
	public boolean open() throws EmailFileOpenException {

		try{
		this.filehandle = new BufferedReader(new FileReader(new File(this.pathString, this.filename)));
		}catch(FileNotFoundException fnf){
			isOpen = false;
			throw new EmailFileOpenException(fnf.getMessage());
		}
		isOpen = true;
		
		return isOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#clost()
	 */
	public boolean close() throws EmailFileClosedException {
		if(isOpen){
			try{
			filehandle.close();
			filehandle = null;
			}catch(IOException ie){
				throw new EmailFileClosedException(ie.getMessage());
			}
			return true;
		}
		setErrorMessage("File was not opened to be closed");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getMessageIterator()
	 */
	public Iterator getMessageIterator() throws EmailFileOpenException{
		
		if (filehandle == null){
			throw new EmailFileOpenException("File handle to mailbox null, need to open first");
		}
		
		return new MBOXIterator(filehandle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.Parser.EmailFileInterface#getMessageStoreageFormat()
	 */
	public int getMessageStoreageFormat() {
		return EmailFileInterface.MBOX;
	}

	
	public void setErrorMessage(String error) {
		this.errorString = new String(error);

	}

	public String getErrorMessage() {
		return errorString;
	}

}
