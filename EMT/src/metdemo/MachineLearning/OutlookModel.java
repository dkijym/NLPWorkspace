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
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import metdemo.Parser.EMTEmailMessage;


/*
 * Basic idea: hard code some ideas to detect spam, to compare to machine lerning methods
 *
 *
 **/

public class OutlookModel extends MLearner {
    

//    private String m_filename;
    private String spamLabel;
    private String otherLabel;
 //   private boolean m_haveModel =false;

    /*
     *
     *by creating new ngram, textclass we guarantee to be not null
     */

    public OutlookModel()
    {
	super.setID("OutlookModel");
	spamLabel = new String("s");
	otherLabel = new String("n");
	
    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_OUTLOOK;
    }

    
    /*
     * @param filename this is the filename which is used to save to disk
     */

    public void save(final String filename) throws IOException
    {

	
	    System.out.println("coded Model 1 - save : " + filename);

	    FileOutputStream out = new FileOutputStream(filename);
	    GZIPOutputStream gout = new GZIPOutputStream(out);
	    ObjectOutputStream s = new ObjectOutputStream( gout);
	    s.writeObject(getID());
	    s.writeObject(spamLabel);
	    s.writeObject(otherLabel);
	    s.writeBoolean(isOneClass());
  
	    s.flush();
	    gout.close();
	    s.close();
	    System.out.println("done saving..going to return");
	


    }




    /*
     * @param filename this is the filename to load up
     */
    
    public void load(final String filename) throws Exception
    {

	
	try{
	    FileInputStream in = new FileInputStream(filename);
	    GZIPInputStream gin = new GZIPInputStream(in);;
	    ObjectInputStream s = new ObjectInputStream(gin);
	    
	    setFeature((String)s.readObject());
	    spamLabel = new String((String)s.readObject());
	    otherLabel = new String((String)s.readObject());
	    isOneClass(s.readBoolean());

	    System.out.println("haveloaded: spam: " + spamLabel + " and other: " + otherLabel);


	    in.close();
	}catch(Exception e){
	    System.out.println("error in read file:"+e);
	}
	
	//m_haveModel=true;




    }

    /*
     *  
     *//*
    public void train(final short[] header, final String[] classes, final String[][] data,final int n,final String TARGET_CLASS) throws Exception
    {
	int i=0;

	for(i=0;i<data.length;i++)
	    {
		if(data[i][1].equals("s"))
		    {}
		else
		    {
			otherLabel = new String(data[i][1]);
			break;

		    }
	    }
	//m_haveModel = true;

    }
*/

    /*
     *
     *
     *
     *@param data this will be axamples given to the modeler to evaluate.
     *       progress a window to show current progress using the BusyWindow class
     *@return this will return an array of classificatoin and score
     *        for each example given in the arguements
     */
/*
    public final String[][] classify(final String data[][])throws Exception
    {
	//dont need since are hard coded.
	//	if(!m_haveModel)
	//    throw new Exception ("No model loaded in hard coded Model 1");

	//number refer to number in FILTERS.txt file from outlook 2000

	String result[][] = new String[data.length][2];
	
	for(int i=0;i<data.length;i++)
	    {
		//rules for body 20/36
		/*
		  0 >From is blank
		  1 Subject contains "advertisement"
		  2 Body contains "money back "
		  3 Body contains "cards accepted"
		  4 Body contains "removal instructions"
		  5 Body contains "extra income"
		  6 Subject contains "!" AND Subject contains "$"
		  7 Subject contains "!" AND Subject contains "free"
		  8 Body contains ",000" AND Body contains "!!" AND Body contains "$"a
		  9 Body contains "for free?"
		  10 Body contains "for free!"
		  11 Body contains "Guarantee" AND (Body contains "satisfaction" OR Body contains "absolute")
		  12 Body contains "more info " AND Body contains "visit " AND Body contains "$"
		  13 Body contains "SPECIAL PROMOTION"
		  14 Body contains "one-time mail"
		  15 Subject contains "$$"
		  16 Body contains "$$$"
		  17 Body contains "order today"
		  18 Body contains "order now!"
		  19 Body contains "money-back guarantee"
		  20 Body contains "100% satisfied"
		  21 To contains "friend@"
		  22 To contains "public@"
		  23 To contains "success@"
		  24 >From contains "sales@"
		  25 >From contains "success."
		  26 >From contains "success@"
		  27 >From contains "mail@"
		  28 >From contains "@public"
		  29 >From contains "@savvy"
		  30 >From contains "profits@"
		  31 >From contains "hello@"
		  32 Body contains " mlm"
		  33 Body contains "@mlm"
		  34 Body contains "///////////////"
		  35 Body contains "check or money order"
		* /
		//note: data[i][0] = body
		//data[i][2] = subject
		
		if(data[i][1].indexOf("advertisement")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".91");
		    }
		else if(data[i][0].indexOf("money back")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".92");
		    }
		else if(data[i][0].indexOf("cards accepted")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".903");
		    }
		else if(data[i][0].indexOf("removal instructions")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".904");
		    }
		else if(data[i][0].indexOf("extra income")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".905");
		    }
		
		else if(data[i][2].indexOf("!")>0 && data[i][2].indexOf("$")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".906");
		    }
		else if(data[i][2].indexOf("!")>0 && data[i][2].indexOf("free")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".907");
		    }
		else if(data[i][0].indexOf(",000")>0 && data[i][0].indexOf(",!!")>0 && data[i][0].indexOf("$")>0 )
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".908");
		    }
		else if(data[i][0].indexOf("for free?")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".909");
		    }
		else if(data[i][0].indexOf("for free!")>0)
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9010");
		    }
		else if(data[i][0].indexOf("Guarantee")>0 && (data[i][0].indexOf("absolute")>0||data[i][0].indexOf("satisfaction")>0)) 
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9011");
		    }
		else if(data[i][0].indexOf("more info ")>0 && data[i][0].indexOf("visit")>0 && data[i][0].indexOf("$")>0) 
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9012");
		    }
		else if(data[i][0].indexOf("SPECIAL PROMOTION")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9013");
		    }
		else if(data[i][0].indexOf("one-time mail")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9014");
		    }
		else if(data[i][2].indexOf("$$")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9015");
		    }
		else if(data[i][0].indexOf("$$$")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9016");
		    }
		else if(data[i][0].indexOf("order today")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9017");
		    }
		else if(data[i][0].indexOf("order now!")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9018");
		    }
		else if(data[i][0].indexOf("money-back guarantee")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9019");
		    }
		else if(data[i][0].indexOf("100% satisfied")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9020");
		    }
		else if(data[i][0].indexOf(" mlm")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9032");
		    }
		else if(data[i][0].indexOf("@mlm")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9033");
		    }

		else if(data[i][0].indexOf("///////////////")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9034");
		    }

		else if(data[i][0].indexOf("check or money order")>0)  
		    {
			result[i][0] = new String (spamLabel);
			result[i][1] = new String (".9035");
		    }
		else{



		    result[i][0] = new String (otherLabel);
		    result[i][1] = new String ("0.5");//round((double)nchar_count/(double)total_c,3) + " " + round((double)cap_count/(double)total_c,3) + " " + ex_count + " " +q_count+" "+ total_c );

		}
	    

	    }//end all bodies
	  
	return result;
    }
*/


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
		
//		String result[][] = new String[data.length][2];
		int score = 100;
		String classification = otherLabel;
		String msg = msgData.getBody();
		String subject = msgData.getSubject();
		//for(int i=0;i<msgData.length;i++)
		    {
		if(subject.indexOf("advertisement")>0)
			    {
				classification = SPAM_LABEL;
				score = 85;
				
			    }
			else if(msg.indexOf("money back")>0)
			    {
				classification = SPAM_LABEL;
				score = 86;
			    }
			else if(msg.indexOf("cards accepted")>0)
			    {
				classification = SPAM_LABEL;
				score = 87;
			    }
			else if(msg.indexOf("removal instructions")>0)
			    {
				classification = SPAM_LABEL;
				score = 88;
			    }
			else if(msg.indexOf("extra income")>0)
			    {
				classification = SPAM_LABEL;
				score = 89;
			    }
			
			else if(subject.indexOf("!")>0 && subject.indexOf("$")>0)
			    {
				classification = SPAM_LABEL;
				score = 90;
			    }
			else if(subject.indexOf("!")>0 && subject.indexOf("free")>0)
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf(",000")>0 && msg.indexOf(",!!")>0 && msg.indexOf("$")>0 )
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("for free?")>0)
			    {
				classification = SPAM_LABEL;
				score = 92;
			    }
			else if(msg.indexOf("for free!")>0)
			    {
				classification = SPAM_LABEL;
				score = 93;
			    }
			else if(msg.indexOf("Guarantee")>0 && (msg.indexOf("absolute")>0||msg.indexOf("satisfaction")>0)) 
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("more info ")>0 && msg.indexOf("visit")>0 && msg.indexOf("$")>0) 
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("SPECIAL PROMOTION")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("one-time mail")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			//else if(msgData.getFromEmail().indexOf("$$")>0)  
			 //   {
			//	classification = SPAM_LABEL;
			//	score = 91;
			 //   }
			else if(msg.indexOf("$$$")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("order today")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("order now!")>0)  
			    {
				classification = SPAM_LABEL;
				score = 94;
			    }
			else if(msg.indexOf("money-back guarantee")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("100% satisfied")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf(" mlm")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			else if(msg.indexOf("@mlm")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }

			else if(msg.indexOf("///////////////")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }

			else if(msg.indexOf("check or money order")>0)  
			    {
				classification = SPAM_LABEL;
				score = 91;
			    }
			
		    

		    }//end all bodies
		  
		return new resultScores(classification,score,msgData.getMailref());
	}



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub
		
	}


	    

}






