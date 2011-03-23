package nlp.Old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * This is only used if I need to parse through a mbox. Use shlomo's code instead
 * @deprecated 
 * @author reuben
 *
 */
public class MailBoxImporter {
	
	//static String directoryPath = "C:/Users/reuben/NLPResearch/maildir";
	//These are the properties for parsing
	static Properties dummyProperties; 
	//This is the dummy mail session
	static Session sess;
	
	/**
	 * Initialize the email parser
	 */
	private static void initEmailParser()
	{
		dummyProperties = new Properties();
		dummyProperties.setProperty("mail.host",
		  "smtp.xsadkajshdkaasdasdascd.com");
		dummyProperties.setProperty("mail.transport.protocol", "smtp");
		 sess = Session.getDefaultInstance(dummyProperties);
	}
	
	public static MimeMessage parseEmail(String emailPath)
	{
		 FileInputStream file = null; 
		 MimeMessage message = null; 
		 
		 //Grab email
		  try {
			  file = new FileInputStream(emailPath); 
		  }
		  catch (FileNotFoundException e) { // TODO Auto-generated catch block
			  e.printStackTrace(); 
		  } 
		  
		  //Parse message
		  try { 
			  message = new MimeMessage(sess, file); }
		  catch (MessagingException e) { // TODO Auto-generated catch block
		  e.printStackTrace(); 
		  } 
		  
		  //Print subject
		  try {
		  System.out.println(message.getSubject()); 
		  } catch (MessagingException e) { // TODO Auto-generated catch block 
			  e.printStackTrace(); }
		  
	    return message;
	}
	
	
	public ArrayList<Person> parseMailbox(String directoryPath)
	{
		ArrayList<Person> us = new ArrayList<Person>();
		
		//Assume that the directory is in the maildir format
		File dir = new File(directoryPath);

		File[] users = dir.listFiles();
		int i = 0;
		//int peopleToGoThrough = users.length;
		int peopleToGoThrough = 5;
		    for (int j =0; j< peopleToGoThrough; j++) {
		    	
		        Person p = new Person(users[j].getName());
		        System.out.println(users[j].getAbsolutePath() + "/all_documents");
		        File allDoc = new File(users[j].getAbsolutePath() + "/all_documents");
		        if(allDoc.exists()){
		        	System.out.println(allDoc.list().length);
		        	i+= allDoc.list().length;
		        
		        //	for(File emlFile: allDoc.listFiles())
		        //		p.emails.add(parseEmail(emlFile.getAbsolutePath()));
		        }
		        
		        us.add(p);
		        
		    }
		    
		  return us;
	}
	
}
