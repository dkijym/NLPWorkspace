/**
 *Abstarct class to impliment machine learning in emt framework
 *
 *Shlomo Hershkop, columbia university fall 2002.
 */

package metdemo.MachineLearning;


import java.lang.String;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.Utils;


/*
 * Basic idea: hard code some ideas to detect spam, to compare to machine lerning methods
 *
 *
 **/

public class CodedModel_1 extends MLearner {
    

    String m_filename;
    String spamLabel;
    String otherLabel;
    boolean m_haveModel =false;

    /*
     *
     *by creating new ngram, textclass we guarantee to be not null
     */

    public CodedModel_1()
    {
	super.setID("CodedModel_1");
	spamLabel = SPAM_LABEL;
	otherLabel = NOTINTERESTING_LABEL;

    }
    

    
    /*
     * @param filename this is the filename which is used to save to disk
     */

    public final void save(String filename) throws IOException
    {

	
	    System.out.println("coded Model 1 - save : " + filename);
	    FileOutputStream out = new FileOutputStream(filename);
	    GZIPOutputStream gout = new GZIPOutputStream(out);

	    ObjectOutputStream s = new ObjectOutputStream( gout);
	    s.writeObject(getID());
	    s.writeObject(spamLabel);
	    s.writeObject(otherLabel);
	    s.writeDouble(getThreshold());
  
	    s.flush();
	    gout.close();
	    s.close();
	    System.out.println("done saving..going to return");
	


    }




    /*
     * @param filename this is the filename to load up
     */
    
    public void load(String filename) throws Exception
    {

	
	try{
	    FileInputStream in = new FileInputStream(filename);
	    GZIPInputStream gin = new GZIPInputStream(in);
	    ObjectInputStream s = new ObjectInputStream(gin);
	    
	    setFeature((String)s.readObject());
	    spamLabel = new String((String)s.readObject());
	    otherLabel = new String((String)s.readObject());
	    setThreshold(s.readDouble());
	    System.out.println("haveloaded: spam: " + spamLabel + " and other: " + otherLabel);


	    in.close();
	}catch(Exception e){
	    System.out.println("error in read file:"+e);
	}
	
	m_haveModel=true;




    }

    /*
     *  
     */
    public final void train(short[] header, String[] classes, String[][] data,int n,String TARGET_CLASS) throws Exception
    {
	int i=0;

	for(i=0;i<data.length;i++)
	    {
		if(data[i][1].equals(SPAM_LABEL))
		    {}
		else
		    {
			otherLabel = new String(data[i][1]);
			break;

		    }
	    }
	m_haveModel = true;

    }


    /*
     *@param data this will be axamples given to the modeler to evaluate.
     *
     *@param progress a window to show current progress using the BusyWindow class
     *@return this will return an array of classificatoin and score
     *        for each example given in the arguements
     */

    public final String[][] classify(final String data[][])throws Exception
    {

	//	if(!m_haveModel)
	//  throw new Exception ("No model loaded in hard coded Model 1");

	int cap_count,ex_count,nchar_count,total_c,q_count;

	String result[][] = new String[data.length][2];
	
	for(int i=0;i<data.length;i++)
	    {
		//get current body
		char[] temp = (data[i][0]).toCharArray();
		q_count=0;      //count question marks
		cap_count=0;   //count capital letter
		ex_count=0;    //count exlamation
		nchar_count=0;
		
		total_c =0;

		for(int j=0;j<temp.length;j++)
		    {
			if(temp[j] == '!')
			    ex_count++;
			else if (temp[j] == '?')
			    q_count++;
			else if (temp[j] >= 'A' && temp[j] <='Z')
			    {
				cap_count++;
				total_c++;
			    }
			else if (temp[j] >= 'a' && temp[j] <= 'z')
			    total_c++;
                        else if ((temp[j] <=' ' || temp[j] >= '~')&& temp[j]!='\n' && temp[j] != '\t' && temp[j] != '\\' && temp[j] != '/' && temp[j]!='.') //ascii range of normal stuff
			    nchar_count++;
			
		    }
		//finsihed running through message
		if(ex_count > 4)    //rule 1 - more than 4 exlamation signs
		    {  
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (""+ex_count);
		    }
		else if(q_count > 4)    //rule 2 - more than 4 question marks
		    {  
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (""+ex_count);
		    }
		
		else if ((double)cap_count/(double)total_c > .3) 
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (""+Utils.round((double)cap_count/(double)total_c,3));
		    }
		else if ((double)nchar_count/(double)total_c > .3) 
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (""+Utils.round((double)nchar_count/(double)total_c,3));
		    }
		else     //maybe normal
		    {
			if(data[i][0].indexOf("To be removed")>0)
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".989");
			    }
			else if(data[i][0].indexOf("MORGAGE")>0)
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9899");
			    }
			 else if(data[i][0].indexOf("LOSE")>0)
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9799");
			    }
			else if(data[i][0].indexOf("TABLE")>0)
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9699");
			    }
			else if(data[i][0].indexOf("Reduce Debt")>0)
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9599");
			    }
			else if(data[i][0].indexOf("ick here")>0) //part of click here to remove stuff
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9499");
			    }
			else if(data[i][0].indexOf("fffff")>0) //color red html
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9399");
			    }
			else if(data[i][0].indexOf("FFFF")>0) //color red html
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9399");
			    }
			else if(data[i][0].indexOf("next of kin")>0)  //africa spam
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9399");
			    }
			else if(data[i][0].indexOf("icking here")>0)  //africa spam
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9299");
			    }
			else if(data[i][0].indexOf("cgi-bin")>0)  //africa spam
			    {
				result[i][0] = new String (spamLabel);
				result[i][1] = new String (".9299");
			    }

			else{



			result[i][0] = new String (otherLabel);
			result[i][1] = new String ("0.5");//round((double)nchar_count/(double)total_c,3) + " " + round((double)cap_count/(double)total_c,3) + " " + ex_count + " " +q_count+" "+ total_c );
			}
		    }

	    }//end all bodies
	  
	return result;
    }



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[], java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		if(Label!=spamLabel)
		{
			otherLabel = Label;
		}
	}



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {

		int cap_count,ex_count,nchar_count,total_c,q_count;

		//String result[][] = new String[data.length][2];
		int score =0;
		String label = spamLabel;
		//for(int i=0;i<msgData.length;i++)
		    {
			//get current body
		    	String msg = msgData.getBody();
			char[] temp = (msg).toCharArray();
			q_count=0;      //count question marks
			cap_count=0;   //count capital letter
			ex_count=0;    //count exlamation
			nchar_count=0;
			
			total_c =0;

			for(int j=0;j<temp.length;j++)
			    {
				if(temp[j] == '!')
				    ex_count++;
				else if (temp[j] == '?')
				    q_count++;
				else if (temp[j] >= 'A' && temp[j] <='Z')
				    {
					cap_count++;
					total_c++;
				    }
				else if (temp[j] >= 'a' && temp[j] <= 'z')
				    total_c++;
	                        else if ((temp[j] <=' ' || temp[j] >= '~')&& temp[j]!='\n' && temp[j] != '\t' && temp[j] != '\\' && temp[j] != '/' && temp[j]!='.') //ascii range of normal stuff
				    nchar_count++;
				
			    }
			//finsihed running through message
			if(ex_count > 4)    //rule 1 - more than 4 exlamation signs
			    {  
				score = ex_count;
			    }
			else if(q_count > 4)    //rule 2 - more than 4 question marks
			    {  
				score = q_count;
			    }
			
			else if ((double)cap_count/(double)total_c > .3) 
			    {
				score =(int)( 100*(Utils.round((double)cap_count/(double)total_c,3)));
			    }
			else if ((double)nchar_count/(double)total_c > .3) 
			    {
				score = (int)(100*Utils.round((double)nchar_count/(double)total_c,3));
			    }
			else     //maybe normal
			    {
				if(msg.indexOf("To be removed")>0)
				    {
					score = 90;
				    }
				else if(msg.indexOf("MORGAGE")>0)
				    {
					score = 91;
				    }
				 else if(msg.indexOf("LOSE")>0)
				    {
					score = 92;
				    }
				else if(msg.indexOf("TABLE")>0)
				    {
					score = 93;
				    }
				else if(msg.indexOf("Reduce Debt")>0)
				    {
					score = 93;
				    }
				else if(msg.indexOf("ick here")>0) //part of click here to remove stuff
				    {
					score = 92;
				    }
				else if(msg.indexOf("fffff")>0) //color red html
				    {
					score = 99;
				    }
				else if(msg.indexOf("FFFF")>0) //color red html
				    {
					score = 99;
				    }
				else if(msg.indexOf("next of kin")>0)  //africa spam
				    {
					score = 93;
				    }
				else if(msg.indexOf("icking here")>0)  //africa spam
				    {
					score = 99;
				    }
				else if(msg.indexOf("cgi-bin")>0)  //africa spam
				    {
					score = 93;
				    }

				else{



				label = (otherLabel);
				score = 50;;//round((double)nchar_count/(double)total_c,3) + " " + round((double)cap_count/(double)total_c,3) + " " + ex_count + " " +q_count+" "+ total_c );
				}
			    }

		    }//end all bodies
		  
		return new resultScores(label,score,msgData.getMailref());
	}



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub
		
	}



    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_HARDCODED;
    }

	    

}






