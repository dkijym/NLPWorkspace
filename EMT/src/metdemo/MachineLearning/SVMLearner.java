package metdemo.MachineLearning;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.BusyWindow;

/** Class which call SVM_light machine learning model
 *@author Ke Wang,Shlomo Hershkop (modifications)
 */


public class SVMLearner extends MLearner{

  //  private EMTDatabaseConnection m_jdbcUpdate, m_jdbcView;
  //  private EMTDatabaseConnection m_jdbc;

    // private String seperator = " \t\n\r\f"+".,*#@!?&;:/<>(){}[]=+-\"";

    private Hashtable allWords; //done when do training
    private Vector orderWords;  


    private String class1_name, class2_name;
    private int class1_len, class2_len;

    private String modelfile = "c:\\met\\metdemo\\gui\\temp_svm_model_file";

    //constructor
    public SVMLearner(){
	super.setID("SVMLearner");
    }
    

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_SVM;
    }
    //constructor with passin parameter
  //  public SVMLearner(EMTDatabaseConnection jdbc, EMTDatabaseConnection jdbcView){
//	super.setID("SVMLearner");
//	m_jdbc = jdbc;
//	m_jdbcView = jdbcView;
 //   }



    //get the total value of the hashtable
    private int getTotal(Hashtable ht){

	int total =0;
	if(ht==null)
	    return 0;

	Enumeration enu = ht.keys();
	while(enu.hasMoreElements()){
		String word = (String)enu.nextElement();
		int times = ((Integer)ht.get(word)).intValue();
		total+=times;
	}
	return total;
	
    }

    //get the sqrt of sum of square  of the hashtable
    private double getSqrtTotal(Hashtable ht){

	int total =0;
	if(ht==null)
	    return 0;

	Enumeration enu = ht.keys();
	while(enu.hasMoreElements()){
		String word = (String)enu.nextElement();
		int times = ((Integer)ht.get(word)).intValue();
		total+=times*times;
	}
	return Math.sqrt(total);
	
    }



    /*
      train():generate the training file for svm, and call svm_light to build model
    */
    public final void train(short header[], String classes[], String data[][], int dummy,String TARGET_CLASS) throws Exception{
		
	if(header==null || header.length<=0)
	    throw (new Exception("header for training is empty"));

	if(header[0]!=2)
	    throw (new Exception("svm must have exact 2 classes for train"));

	int work=0;
	final int max = data.length*2;
	BusyWindow bw=null;
	if(showProgress())
	    {bw = new BusyWindow("Training TextClassifier","Progress Report",true);
	    bw.setVisible(true);
	    }
		


	//for svm classificatin, can only have 2 class here
	class1_name = classes[0];
	class2_name = classes[1];

	class1_len = header[1];
	class2_len = header[2];

	//get out all words from training set
	allWords = new Hashtable(); //all words in all document

	for(int i=0;i<data.length;i++){
    	    if(showProgress())	    
		bw.progress(work++,max);

	    String s = data[i][0];
	    StringTokenizer st = new StringTokenizer(s, MLearner.tokenizer ,true);
	    while(st.hasMoreTokens()){
				
		String sub = st.nextToken();
		if(allWords.containsKey(sub)){
			Integer t = (Integer)allWords.get(sub);
			int val = t.intValue();
			allWords.put(sub, (new Integer(val+1)));
		}
		else{
			allWords.put(sub, (new Integer(1)));
		}

	    }//while
	}//for


	//delete those words that appears less than 3 times in all documents
	//this is optional, but I think should better
	Enumeration enu = allWords.keys();
	while(enu.hasMoreElements()){
	        

		String word = (String)enu.nextElement();
		int times = ((Integer)allWords.get(word)).intValue();
		if(times<3)
		    allWords.remove(word);
	}

	//order this hashtable, actually only put them in a random order
	orderWords = new Vector();
	enu = allWords.keys();
	while(enu.hasMoreElements()){
		String word = (String)enu.nextElement();
		orderWords.addElement(word);		
	}

	//the value of the allWords hashtable to the ordered index
	for(int i=0;i<orderWords.size();i++){
	    String word = (String)orderWords.elementAt(i);
	    if(allWords.containsKey(word))
		allWords.put(word, (new Integer(i+1)));
	}
	

	//write out to training file and words file
	String trainfile="temp_svm_train_file.dat";
	String wordfile="temp_svm_words.dat";
	try{
	    PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(trainfile)));
	    PrintWriter out2=new PrintWriter(new BufferedWriter(new FileWriter(wordfile)));
	    
	    for(int i=0;i<orderWords.size();i++){
		out2.println((String)orderWords.elementAt(i));
	    }
	    out2.flush();
	    out2.close();
	    
	    for(int i=0;i<data.length;i++){
	    if(showProgress())	    
		bw.progress(work++,max);

		Hashtable doc = new Hashtable();
		String s = data[i][0];
		StringTokenizer st = new StringTokenizer(s, MLearner.tokenizer, true);
		while(st.hasMoreTokens()){
				
		    String sub = st.nextToken();
		    if(doc.containsKey(sub)){
			Integer t = (Integer)doc.get(sub);
			int val = t.intValue();
			doc.put(sub, (new Integer(val+1)));
		    }
		    else if(allWords.containsKey(sub)){
			doc.put(sub, (new Integer(1)));
		    }
		}//while
	    
		String toWrite="";
		if(i<class1_len) //treat the first class as positive
		    toWrite+="1";
		else
		    toWrite+="-1"; //treat second class as negative
	    

		//get the total of the doc words
		int total = getTotal(doc);
		double total2 = getSqrtTotal(doc);

		for(int j=0;j<orderWords.size();j++){
		    String w = (String)orderWords.elementAt(j);
		    if(doc.containsKey(w)){
			double freq = ((Integer)doc.get(w)).doubleValue()/(double)total;
			double freq2 = ((Integer)doc.get(w)).doubleValue()/total2;
			//toWrite+=" "+(j+1)+":"+freq;
			toWrite+=" "+(j+1)+":"+freq2; //normalized feature value
			//toWrite+=" "+(j+1)+":"+"1"; //for binary feature
		    }
		}
		out.println(toWrite);

	    }//for
	    out.flush();
	    out.close();

	}catch(Exception e){
	    System.out.println("exception:"+e);
	}


	//call svm_light to exec
	modelfile = "temp_svm_model_file";
	try{
	    String toExe = "svm_learn "+trainfile+" "+modelfile;
	    Process proc=Runtime.getRuntime().exec(toExe);
	    
	}catch(Exception e){
            System.out.println("call svm train failed with error: "+e);
	    return;
        }



    } //train



    /*
      classify: use the model from training and do svm classify, 
      read back result file
     */
    public final String[][] classify(String data[][]) throws Exception{ 
		
	if(allWords == null || allWords.size()<=0){
	    throw (new Exception("allWords is empty"));
	}
	
	if(orderWords == null || orderWords.size()<=0){
	    throw (new Exception("orderWords is empty"));
	}
	        
	if(data==null || data.length==0){
	    throw (new Exception("the test data is empty"));
	}

	String ret[][] = new String[data.length][2];
	String testfile="c:\\met\\metdemo\\gui\\temp_svm_test_file.dat";
	try{
	    PrintWriter out3=new PrintWriter(new BufferedWriter(new FileWriter(testfile)));
	    for(int i=0;i<data.length;i++){

		Hashtable doc = new Hashtable();
		String s = data[i][0];
		if(s==null) 
		    System.out.println("i="+i);

		StringTokenizer st = new StringTokenizer(s, MLearner.tokenizer, true);
		while(st.hasMoreTokens()){
				
		    String sub = st.nextToken();
		    if(doc.containsKey(sub)){
			Integer t = (Integer)doc.get(sub);
			int val = t.intValue();
			doc.put(sub, (new Integer(val+1)));
		    }
		    else if(allWords.containsKey(sub)){
			doc.put(sub, (new Integer(1)));
		    }
		}//while
	    
		String toWrite="";
		toWrite+="1"; //just give 1 here, won't care
	    
		//might should do normalization here??
		int total = getTotal(doc);
		double total2 = getSqrtTotal(doc);

		for(int j=0;j<orderWords.size();j++){
		    String w = (String)orderWords.elementAt(j);
		    if(doc.containsKey(w)){
			double freq = ((Integer)doc.get(w)).doubleValue()/(double)total;
			double freq2 = ((Integer)doc.get(w)).doubleValue()/total2; //this has sum of square to 1
			//toWrite+=" "+(j+1)+":"+freq;
			toWrite+=" "+(j+1)+":"+freq2;
			//toWrite+=" "+(j+1)+":"+"1"; //for binary
		    }
		}
		out3.println(toWrite);

	    }//for
	    
	    out3.flush();
	    out3.close();

	}catch(IOException e){
	    System.out.println(".....exception:"+e);
	}

	//call svm_light classify
	String predictfile="c:\\met\\metdemo\\gui\\temp_svm_predict_file";
	try{
	    String toExe = "c:\\met\\metdemo\\gui\\svm_classify "+testfile+" "+modelfile+" "+predictfile;
	    Process proc=Runtime.getRuntime().exec(toExe);
	    
	}catch(Exception e){
            System.out.println("call svm test error: "+e);
        }

	Thread.sleep(10*100); //sleep 10 sec
	//read back the predict file to fill in the result
	 try{
            BufferedReader in = new BufferedReader(new FileReader(predictfile));
	    for(int i =0;i<data.length;i++){
		String s = in.readLine();
		double dis = Double.parseDouble(s);
		ret[i][1] = new String(""+dis);
		if(dis>=0)
		    ret[i][0] = class1_name;
		else
		    ret[i][0] = class2_name;
		    
	    }
	 }catch(Exception e){
            System.out.println("read in predict file error: "+e);
        }
	

	return ret;
    }//classify
    




    //save the computed tfidf_vector to file	
    public final void save(String filename){

	try{
	    System.out.println("svm - save : " + filename);
	    FileOutputStream out = new FileOutputStream(filename);
	    GZIPOutputStream gout = new GZIPOutputStream(out);
	    ObjectOutputStream s = new ObjectOutputStream(gout);
	    s.writeObject(getID());
	    s.writeObject(getFeature());
	  
	    s.writeObject(allWords);
	    s.writeObject(orderWords);
	    s.writeDouble(getThreshold());
	    s.flush();
	    gout.close();
	    s.close();
	    System.out.println("done saving..going to return");
	}catch(Exception e){
	    System.out.println("Exception in file write out: "+e);
	}

	   
    }



    //load back the centroid vector from the file
    public final void load(String filename){
	try{
	    FileInputStream in = new FileInputStream(filename);
	    GZIPInputStream gin = new GZIPInputStream(in);
	    ObjectInputStream s = new ObjectInputStream(gin);
	    s.readObject(); //this is the id string
	    setFeature((String)s.readObject());
	    allWords = (Hashtable)s.readObject();
	    orderWords = (Vector)s.readObject();
	    setThreshold(s.readDouble());
	}catch(Exception e){
	    System.out.println("Exception in file read in: "+e);
	}
    }


	/* (non-Javadoc)
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[], java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		// TODO Auto-generated method stub
		
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



}//class
