// *Shlomo Hershkop, columbia university fall 2002.


package metdemo.MachineLearning;


import java.io.IOException;
import java.lang.String;

import metdemo.Parser.EMTEmailMessage;


/**
 * Abstract Class to impliment Machine Learning models in EMT. Some global
 * defintions are set here, and some flags can be set to signal to models.
 * 
 * @author Shlomo Hershkop
 */

public abstract class MLearner {
	/** String of features */
	String m_Feature = new String();
	boolean []m_featureBooleanArray = new boolean[0];
	public final static short REAL = 1;
	public final static short DISCRETE = 2;
	public final static short CLASS = 3;
	public final static short TEXT = 4;
	public final static String tokenizer = new String(" .,()\\/<>#{}[]:\t\n\r\f\"=;|'+-?+;*&^%@~_");
	public static final int ML_NBAYES_NGRAM = 0;
	public static final int ML_NBAYES_TXTCLASS = 1;
	public static final int ML_NGRAM = 2;
	public static final int ML_TEXTCLASSIFIER = 3;
	public static final int ML_HARDCODED = 4;
	public static final int ML_EWCOMBINATION = 5;
	public static final int ML_OUTLOOK = 6;
	public static final int ML_PGRAM = 7;
	public static final int ML_TFIDF = 8;
	public static final int ML_LINKS = 9;
	public static final int ML_LIMITEDNGRAM = 10;
	public static final int ML_SPAMASSASSIN = 11;
	public static final int ML_SVM = 21;
	public static final int ML_ORACLE = 12;
	public static final int ML_EW = 13;
	public static final int ML_NBW = 14;
	public static final int ML_NB2d = 15;
	public static final int ML_WEIGHTEDMAJORITY = 16;
	public static final int ML_VIPLearner = 17;
	public static final int ML_UsageLearner = 18;
	public static final int ML_STATS = 20;
	
	
	
	//classes
	public static final String SPAM_LABEL = "Spam";
	public static final String UNKNOWN_LABEL = "Unknown";
	public static final String INTERESTING_LABEL = "Interesting";
	public static final String VIRUS_LABEL = "Virus";
	public static final String NOTINTERESTING_LABEL = "Not Interesting";
//classes offsets
	public static final int UNKNOWN_CLASS = 0;
	public static final int INTERESTING_CLASS = 1;
	public static final int SPAM_CLASS = 2;
	public static final int VIRUS_CLASS = 3;
	public static final int NOT_INTERESTING_CLASS = 4;
	public static final int NUMBER_CLASSES =5;
		
	
	private String targetLabel = SPAM_LABEL;
	private boolean isStop = false; //default no stop words
	private boolean isLower = false; //default no lowercase
	private boolean isOneClass = false;
	private double m_threshold = 0.0;
	/** model id */
	private String m_ID = new String("");
	/** some informatin about the model */
	private String m_info = new String("");
	/*
	 * flag if we want to shwo progress or not. clients are responsible on
	 * checking this out
	 */
	private boolean m_show_progress = true;


	/** set use stopwords */
	public void setStop(final boolean b) {
		isStop = b;
	}

	public abstract void flushModel();
	
	
	/** check if to use stop words */
	public final boolean isStop() {
		return isStop;
	}
	/** are we in one class mode */
	public final boolean isOneClass() {
		return isOneClass;
	}

	/** set the one class flag */
	public final void isOneClass(boolean b) {
		isOneClass = b;
	}

	/** get current threshold */
	public final double getThreshold() {
		return m_threshold;
	}

	/** sets the current threshold */
	public final void setThreshold(double d) {

		m_threshold = d;
	}

	/** set to use lowercase */
	public void setLowerCase(final boolean b) {
		isLower = b;
	}

	/** check if to set to lower case */
	public final boolean isLowerCase() {
		return isLower;
	}

	public void setTargetLabel(String target){
		targetLabel = target;
	}

	public String getTargetLabel(){
		return targetLabel;
	}

	
	
	
	public final void setFeatureBoolean(boolean []vals){
		m_featureBooleanArray = new boolean[vals.length];
		
		for(int i=0;i<vals.length;i++){
			m_featureBooleanArray[i] = vals[i];
		}
	}
	
	public final boolean[] getFeatureBoolean(){
		return m_featureBooleanArray;
	}
	
	/**
	 * This will set the feature string
	 * 
	 * @param s
	 *            the string to save as the tag to be included with the model,
	 *            each one can use it as they want
	 */

	public void setFeature(final String s) {
		m_Feature = new String(s);
	}

	/** sets the information string */
	public final void setInfo(final String s) {
		m_info = new String(s);
	}

	/** returns the info string */
	public final String getInfo() {
		return m_info;
	}

	/**
	 * This will set the m_show_progress flag, will remember weather we want to
	 * show a progress bar
	 */
	public final void setProgress(final boolean s) {
		m_show_progress = s;
	}


	/** reads back if we should show a progress bar */
	public final boolean showProgress() {
		return m_show_progress;
	}

	public void setID(final String s) {
		m_ID = new String(s);
	}

	public final String getID() {
		return m_ID;
	}


	/**
	 * @return will return the current feature string
	 */
	public final String getFeature() {
		return m_Feature;
	}


	/**
	 * General Saving routine
	 * <P>
	 * <b>NOTE: </b> id is saved first (string), followed by feature string,
	 * then info string
	 * <P>
	 * 
	 * @param filename
	 *            this is the filename which is used to save to disk
	 */

	public abstract void save(final String filename) throws IOException;

	/*
	 * General Loadingin program <P> <b>NOTE: </b> Must follow same order of
	 * save routine <P> @param filename this is the filename to load up
	 */

	public abstract void load(final String filename) throws Exception;


	/**
	 * General idea of training
	 * <P>
	 * each of the parameters can be used idfferently by each model. please see
	 * {@link NGram}for specific example
	 * 
	 * @param header
	 *            can be the size of each type column
	 * @param classes
	 *            can be more info on column types as strings
	 * @param data
	 *            the actual data
	 * @param n
	 *            can be used for flag
	 * @param TARGET_CLASS
	 *            is the class which we want to optimize classification for.
	 * @exception is
	 *                thrown if there is an anconsistency in the data
	 */
	//public abstract void train(final short[] header, final String[] classes, final String[][] data, final int n,
	//		String TARGET_CLASS) throws Exception;

	/**
	 * General idea of classification
	 * 
	 * @param records
	 *            this will be examples given to the modeler to evaluate.
	 * 
	 * @return this will return an array of classificatoin and score for each
	 *         example given in the arguements
	 */

	//public abstract String[][] classify(final String records[][]) throws Exception;

	/**
	 * Return the id number of the particular model type
	 * @return the model number
	 */
	public abstract int getIDNumber();
	
	/**
	 * Train the classifier on the specific Example using a String as a target to learn
	 * @param msgData
	 * @param Label
	 */
	
	public abstract void doTraining(final EMTEmailMessage msgData,final String Label);
	
	public abstract resultScores doLabeling(final EMTEmailMessage msgData) throws machineLearningException;
	
	/** 
	 * translates between string representation and int
	 * @param classLabel
	 * @return
	 */
	public static int getClassNumber(String classLabel){
		
		if(classLabel.equals(SPAM_LABEL))
		{
			return SPAM_CLASS;
		}
		else if(classLabel.equals(INTERESTING_LABEL))
		{
			return INTERESTING_CLASS;
		}
		else if(classLabel.equals(NOTINTERESTING_LABEL))
		{
			return NOT_INTERESTING_CLASS;
		}
		else if(classLabel.equals(VIRUS_LABEL))
		{
			return VIRUS_CLASS;
		}
		else if(SPAM_LABEL.startsWith(classLabel)){
		    return SPAM_CLASS;
		}
		else if(INTERESTING_LABEL.startsWith(classLabel))
		{
			return INTERESTING_CLASS;
		}
		else if(NOTINTERESTING_LABEL.startsWith(classLabel))
		{
			return NOT_INTERESTING_CLASS;
		}
		else if(VIRUS_LABEL.startsWith(classLabel))
		{
			return VIRUS_CLASS;
		}else
			return UNKNOWN_CLASS;
	}
	
	public static String getClassString(int classnum)
	{
		switch(classnum){
			case(SPAM_CLASS):
				return SPAM_LABEL;
			case(INTERESTING_CLASS):
				return INTERESTING_LABEL;
			case(NOT_INTERESTING_CLASS):
				return NOTINTERESTING_LABEL;
			case(VIRUS_CLASS):
				return VIRUS_LABEL;
			default:
				return UNKNOWN_LABEL;
		}
	}
	
	
	public static String getTypeString(int type){
		switch(type){
			case(ML_NBAYES_NGRAM):
				return "NBayes (ng)";
			case(ML_NBAYES_TXTCLASS):
				return "NBayes (tx)";
			case(ML_NGRAM):
				return "NGram";
			case(ML_TEXTCLASSIFIER):
				return "TxtClassifier";
			case(ML_HARDCODED):
				return "Hardcoded";
			case(ML_EWCOMBINATION):
				return "EW";
			case(ML_OUTLOOK):
				return "Outlook rules";
			case(ML_PGRAM):
				return "PGram";
			case(ML_TFIDF):
				return "TFIDF";
			case(ML_SVM):
				return "SVM";
			case(ML_LINKS):
				return "URL";
			case(ML_LIMITEDNGRAM):
				return "Limited NGRAM";
			case(ML_ORACLE):
				return "Judge";
			case(ML_EW):
				return "EWeights";
			case(ML_NBW):
				return "NBWeights 1 Dim.";
			case(ML_NB2d):
				return "NB 2 Dimensional";
			case(ML_WEIGHTEDMAJORITY):
			    return "Weighted Majority";
			case(ML_VIPLearner):
			    return "VIP Learner";
			case(ML_UsageLearner):
			    return "Usage Histogram";
			case(ML_SPAMASSASSIN):
			    return "SPAMASSASSIN";
			default:
				return "Unknown";
		}
	}
	
	
	

}






