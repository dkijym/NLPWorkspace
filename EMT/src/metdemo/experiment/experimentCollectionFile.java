/**
 * 
 */
package metdemo.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import metdemo.Parser.EMTEmailMessage;

/**
 * @author Shlomo
 *
 */
public class experimentCollectionFile implements experimentalCollectionInterface {

	
	final static String VERSIONstring = "**VERISON**";
	final static String VERSIONnumber = "1";
	private ArrayList emailCollection;
	private String filename;
	private File rawFile;
	private boolean inMemory;
	private String m_comment;
	private boolean toAppendToFile = true;
	public experimentCollectionFile(String fileToName){
		
		filename = fileToName;
		
		rawFile = null;
		emailCollection = new ArrayList();
		inMemory = false;
		m_comment = new String();
		
	}
	
	
	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#getCount()
	 */
	public int getCount() {
		return emailCollection.size();
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#clear()
	 */
	public boolean clear() {
		emailCollection.clear();
		return true;
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#setName(java.lang.String)
	 */
	public void setName(String name) {
	filename = name;
	if(emailCollection.size()>0){
		inMemory = true;
	}
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#save()
	 */
	public boolean save() throws experimentalCollectionFileException {
		
		try{
		 BufferedWriter bufw = new BufferedWriter(new FileWriter(filename, toAppendToFile));

		 //lets write out a version string
		 bufw.write("#"+VERSIONstring);
		 bufw.write("#"+VERSIONnumber);
		 //lets write out the version number
		 bufw.write(m_comment);
		 //lets write out the comment string
		 
		 
         // if((experimentDistribution.getSelectedIndex()==NOCHANGE)){
         for (int i = 0; i < emailCollection.size(); i++) {
             EMTEmailMessage emailM = (EMTEmailMessage)emailCollection.get(i);
        	 bufw.write(emailM.getMailref() + "\t" + emailM.getUtime() + "\t" + emailM.getLabel() + "\n");
         }
         bufw.flush();
         bufw.close();
         
		}
		catch(IOException ioe){
			throw new experimentalCollectionFileException(ioe.toString());
		}
		return true;
		
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#setComment(java.lang.String)
	 */
	public void setComment(String comment) {
		m_comment = comment.replaceAll("\n","\t");
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#getComment()
	 */
	public String getComment() {
		return m_comment.replaceAll("\t","\n");
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#getIterator()
	 */
	public Iterator getIterator() {
		return new Iterator(){
			int item =0;
			
			public boolean hasNext() {
				return item < emailCollection.size();
			}

			public Object next() {
				return emailCollection.get(item++);
			}

			public void remove() {
				if(item == 0){
						return;
						}
				
				emailCollection.remove(--item);
				
			}
			
			
			
		};
	}

	/* (non-Javadoc)
	 * @see metdemo.experiment.experimentalFileInterface#addEmail(metdemo.Parser.EMTEmailMessage)
	 */
	public void addEmail(EMTEmailMessage newone) {
		emailCollection.add(newone);
		inMemory = true;

	}


	public void addEmailSet(EMTEmailMessage[] newones) {

		
		
		for(int i=0;i<newones.length;i++){
			emailCollection.add(newones[i]);
		}
		inMemory = true;
	}


	public void loadDataFromFile()throws experimentalCollectionFileException {
		try{
			 BufferedReader bufr = new BufferedReader(new FileReader(filename));

			 //lets write out a version string
			 String line1 = bufr.readLine();
			 String line2 = bufr.readLine();
			if(!line1.equals("#"+VERSIONstring)){
				throw new experimentalCollectionFileException("Version string wrong in "+filename);
			}
			if(!line1.equals("#"+VERSIONnumber)){
				throw new experimentalCollectionFileException("Version number string wrong in "+filename);
			}
			emailCollection = new ArrayList();
			
			while((line1 = bufr.readLine())!=null){
				
				//need to cut up line1
				//need to create new emt email object
				//add it to the arraylist of emails

		        //	 bufw.write(emailM.getMailref() + "\t" + emailM.getUtime() + "\t" + emailM.getLabel() + "\n");
		        int a = line1.indexOf('\t');
		        if(a<0){
		        	throw new experimentalCollectionFileException("Bad file data "+ line1);
		        }
		        int b = line1.indexOf('\t',a+1);
		        if(b<0){
		        	throw new experimentalCollectionFileException("2Bad file data "+ line1);
		        }
		        EMTEmailMessage emtnow = new EMTEmailMessage(line1.substring(0,a));
		        long utime  = Long.parseLong(line1.substring(a+1,b));
		        String label = line1.substring(b+1);
		        emailCollection.add (emtnow);
				
			}
			 bufr.close();
	         
			}
			catch(IOException ioe){
				throw new experimentalCollectionFileException(ioe.toString());
			}
	
	
	}

}
