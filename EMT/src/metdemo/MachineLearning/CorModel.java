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

import metdemo.Parser.EMTEmailMessage;





/*
 * Basic idea is to combine the work of two models.
 *
 *
 **/

public class CorModel extends MLearner {
    

    String m_filename;
    NGram m_ngram;
    TextClassifier m_text;
    PGram m_coded1;
    boolean m_haveModel =false;
    double m_correlation[][];
    int totalExamplesSeen =0;
    final int MINEXAMMPLENEEDED = 400;
    /*
     *
     *by creating new ngram, textclass we guarantee to be not null
     */

    public CorModel()
    {
	super.setID("CorModel");
	m_ngram = new NGram();
	m_text = new TextClassifier();
	m_coded1 = new PGram();
	m_correlation = new double[8][2];
    }
    

    
    /*
     * @param filename this is the filename which is used to save to disk
     */

    public void save(String filename) throws IOException
    {

	
	    System.out.println("\nCorModel - save : " + filename);
	    ObjectOutputStream s = new ObjectOutputStream( new FileOutputStream(filename));
	    s.writeObject(getID());
	    m_ngram.save(filename+ "-ngram-subCor");
	    m_text.save(filename+ "-textc-subCor");
	    m_coded1.save(filename+"-pgram-subCor");
	    s.writeInt(m_correlation.length);
	    for(int i=0;i<m_correlation.length;i++)
		{
		 
		    s.writeDouble(m_correlation[i][0]);
		    s.writeDouble(m_correlation[i][1]);
		}

	    s.writeInt(totalExamplesSeen);
	    s.flush();
	    s.close();
	    System.out.println("done saving..going to return");
	

    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
      return ML_EWCOMBINATION;
    }


    /*
     * @param filename this is the filename to load up
     */
    
    public void load(String filename) throws Exception
    {

	
	try{
	    FileInputStream in = new FileInputStream(filename);
	    ObjectInputStream s = new ObjectInputStream(in);
	    //s.readObject();
	    setFeature((String) s.readObject());

	    m_ngram.load(filename+ "-ngram-subCor");
	    m_text.load(filename+ "-textc-subCor");
	    m_coded1.load(filename+"-pgram-subCor");
	    int i=s.readInt();//m_correlation[][].length);
	    m_correlation = new double[i][2]; 
	    for(i=0;i<m_correlation.length;i++)
		{
		 
		    m_correlation[i][0] =  s.readDouble();
		    m_correlation[i][1] =  s.readDouble();
		}

	    totalExamplesSeen = s.readInt();



	    in.close();
	}catch(Exception e){
	    System.out.println("error in read file of correlation:"+e);
	}
	
	m_haveModel=true;




    }

    /*
     *  
     *//*
    public void train(short[] header, String[] classes, String[][] data,int n,String TARGET_CLASS) throws Exception
    {
	//need to train each of the submodels
	if(data.length == 0)
	    throw new Exception("No Training data");


	//run each trainer seperatly
	m_ngram.train(header,classes,data,n,TARGET_CLASS);
	m_text.train(header,classes,data,n,TARGET_CLASS);
	m_coded1.train(header,classes,data,n,TARGET_CLASS);
	//now need to see where they are correct and where they arent. 
	//basically create a prob table of their predictions.
	int num_class = header[0];
	int side=0;    
	String result1[][] = m_ngram.classify(data);
	String result2[][] = m_text.classify(data);
	String result3[][] = m_coded1.classify(data);
	String current_class;// = data[0][1];
	//System.out.println("what is this = ? " + (int)java.lang.Math.pow(2,num_class));
	m_correlation = new double[8][2];
	//need to initialize it
	System.out.println("corlength: "+ m_correlation.length);
	System.out.println("r1: " + result1.length + " r2: " + result2.length + " r3: " + result3.length + " d: " + data.length);

	for(int i=0;i< m_correlation.length;i++)
	    for(int j=0;j<2;j++)
		m_correlation[i][j]=0;
	
	BusyWindow msg = new BusyWindow("Train Model Correlation","Progress Report");
	int maxl = data.length;
	msg.show();
	int jump=0;
	int one =1;
	//this table will be as follows:
	// using s =0, n =1 we can figure out an osseft row
	// example: when model=s,model2=s,model3=n   ssn = 001 = spot 1
	//two columns, one when it is spam, one when it is not
	//will track when models are correct this way
	for(int i=0;i<data.length;i++)
	    {
		msg.progress(i,maxl);
		current_class = data[i][1].trim();
		//in data second spot is target class
		//need to calc jump into cor table
		jump =0;
		one =1;
		if(!result1[i][0].equals(TARGET_CLASS))
		    {    jump |= one;
		    }
		jump = jump <<1;
		if(!result2[i][0].equals(TARGET_CLASS))
		    {    jump |= one;
		    }
		jump = jump <<1;
		if(!result3[i][0].equals(TARGET_CLASS))
		    {    jump |= one;
		    }

		if(current_class.equals(TARGET_CLASS))
		    m_correlation[jump][0]++;
		else
		    m_correlation[jump][1]++;
		 
		  

		  
	    }

	System.out.println("correlation output:");
	for(int i=0;i<m_correlation.length;i++)
	    {
		System.out.println(i+" \t"+m_correlation[i][0] + " \t" + m_correlation[i][1]);
	    }

	msg.hide();
	msg = null;
	m_haveModel=true;

    


    }
    */

    /*
     *@param records this will be axamples given to the modeler to evaluate.
     *
     *@param progress a window to show current progress using the BusyWindow class
     *@return this will return an array of classificatoin and score
     *        for each example given in the arguements
     */
/*
    public String[][] classify(final String records[][])
	throws Exception
    {

	if(!m_haveModel)
	    throw new Exception ("No model loaded in Model Correlation");

	
	return null;



    }
*/


	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[], java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
//		need to train each of the submodels
		if(msgData == null)
		{
			return;
		}
		resultScores rs1,rs2,rs3;
		//run each trainer seperatly
		m_ngram.doTraining( msgData,  Label);
		m_text.doTraining( msgData,  Label);
		m_coded1.doTraining( msgData,  Label);
		//now need to see where they are correct and where they arent. 
		//basically create a prob table of their predictions.
		totalExamplesSeen++;
		try{
		 rs1 = m_ngram.doLabeling(msgData);
		 rs2 = m_text.doLabeling(msgData);
		 rs3 = m_coded1.doLabeling(msgData);
		}catch(machineLearningException mm){
			System.out.println(mm);
			return;
		}
		 //System.out.println("what is this = ? " + (int)java.lang.Math.pow(2,num_class));
		
		//need to initialize it
		//System.out.println("corlength: "+ m_correlation.length);
		//System.out.println("r1: " + result1.length + " r2: " + result2.length + " r3: " + result3.length + " d: " + data.length);

		for(int i=0;i< m_correlation.length;i++)
		    for(int j=0;j<2;j++)
			m_correlation[i][j]=0;
		
		int jump=0;
		int one =1;
		//this table will be as follows:
		// using s =0, n =1 we can figure out an osseft row
		// example: when model=s,model2=s,model3=n   ssn = 001 = spot 1
		//two columns, one when it is spam, one when it is not
		//will track when models are correct this way
		    {
			String TARGET_CLASS = getTargetLabel();
			//in data second spot is target class
			//need to calc jump into cor table
			jump =0;
			one =1;
			if(!rs1.getLabel().equals(TARGET_CLASS))
			    {    jump |= one;
			    }
			jump = jump <<1;
			if(!rs2.getLabel().equals(TARGET_CLASS))
			    {    jump |= one;
			    }
			jump = jump <<1;
			if(!rs3.getLabel().equals(TARGET_CLASS))
			    {    jump |= one;
			    }

			if(Label.equals(TARGET_CLASS))
			    m_correlation[jump][0]++;
			else
			    m_correlation[jump][1]++;
			 
			  

			  
		    }

		//System.out.println("correlation output:");
		//for(int i=0;i<m_correlation.length;i++)
		 //   {
			//System.out.println(i+" \t"+m_correlation[i][0] + " \t" + m_correlation[i][1]);
		//    }

		
		m_haveModel=true;

	    
		
	}



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {
		
			
		// TODO Auto-generated method stub
		return null;
	}



	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub
		
	}

}






