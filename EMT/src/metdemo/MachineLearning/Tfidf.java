package metdemo.MachineLearning;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;

//import metdemo.unused.JDBCConnect;

/**
 * Class which impliments TFIDF machine learning model, based on MailCat paper
 * Addition of cohen paper on rule learning for email catagorization for
 * thresholding
 * 
 * @author Ke Wang,Shlomo Hershkop (modifications)
 */

public class Tfidf extends MLearner {

	//public Vector tfidf_Vector = new Vector();
	public int train_spam_length = 0;
	public int train_normal_length = 0;
	int test_spam_length = 0;
	int test_normal_length = 0;
	int sumTotalWordCounts =0;
	private HashMap[] classWordCount;
	private int[] wordTotals = new int[NUMBER_CLASSES];
	private boolean inTraining = true;
	//constructor
	public Tfidf() {
		super.setID("tfidf");
		classWordCount = new HashMap[NUMBER_CLASSES];
		for (int i = 0; i < NUMBER_CLASSES; i++) {
			classWordCount[i] = new HashMap();
			wordTotals[i] = 0;
		}
	}

	/** ********************************************************* */
	/* TF-IDF style classifier. Implemented in MailCat paper */
	/** ********************************************************* */

	/**
	 * Build the hashtable for all messages in one folder, including all words
	 * and tokens return the appearance times of one word in a folder
	 */
	private HashMap tfidf_build(String data) {

		HashMap ht = new HashMap();

		//for (int i = 0; i < data.length; i++) {
		{
			String s = data;
			StringTokenizer st = new StringTokenizer(s, MLearner.tokenizer, true);
			while (st.hasMoreTokens()) {

				String sub = st.nextToken();
				if (ht.containsKey(sub)) {
					Integer t = (Integer) ht.get(sub);
					int val = t.intValue();
					ht.put(sub, (new Integer(val + 1)));
				} else {
					ht.put(sub, (new Integer(1)));
				}
			}
		}
		return ht;
	}

	/** Trian the classifier using examples */
	/*
	 * public final void train(short header[], String classes[], String
	 * data[][], int dummy,final String TARGET_CLASS) throws Exception{
	 * 
	 * if(header==null || header.length <=0) throw (new Exception("header for
	 * training is empty"));
	 * 
	 * 
	 * //System.out.println("header:"+header[0]+","+header[1]+","+header[2]);
	 * //System.out.println("classes:"+classes[0]+","+classes[1]); int num =
	 * header[0]; double total_length = data.length; int start = 0; int
	 * length=0;
	 * 
	 * //if we want to show a gui. BusyWindow bw=null; if(showProgress()) { bw =
	 * new BusyWindow("Training TextClassifier","Progress Report"); bw.show(); }
	 * 
	 * for(int j=0;j <num;j++) {
	 * 
	 * if(showProgress()) bw.progress(j,header[0]);
	 * 
	 * start += length; length = header[j+1];
	 * //System.out.println("start="+start+", length="+length); HashMap words =
	 * new HashMap();
	 * 
	 * String temp[][] = new String[length][1]; for(int k =0;k <length;k++){
	 * temp[k][0]=data[start+k][0]; }
	 * 
	 * words = tfidf_build(temp); tfidf_onefolder TF = new
	 * tfidf_onefolder(words, classes[j]); tfidf_Vector.addElement(TF);
	 * 
	 * }//for
	 * 
	 * if(showProgress()){ bw.hide(); bw = null; }
	 * 
	 * 
	 * //for test //System.out.println("done the training ....");
	 *  /* for(int j=0;j <num;j++){ tfidf_onefolder tt =
	 * (tfidf_onefolder)tfidf_Vector.elementAt(j); System.out.println(tt); } / }
	 */

	/** will classify the data based on earlier training. */
	/*
	 * public final String[][] classify(String data[][]) throws Exception {
	 * 
	 * if(tfidf_Vector == null || tfidf_Vector.size() <=0){ throw (new
	 * Exception("Model centroid is empty")); }
	 * 
	 * if(data==null || data.length==0){ throw (new Exception("the test data is
	 * empty")); }
	 * 
	 * BusyWindow msg =null; if(showProgress()){ msg =new BusyWindow("Classify
	 * TextClassifier","Progress Report"); msg.show(); }
	 * 
	 * 
	 * 
	 * 
	 * String ret[][] = new String[data.length][2]; int maxl =
	 * data.length;//save data length lookup double total=0; boolean singleClass =
	 * false;//if we only have one class this has to do with normalization
	 * if(tfidf_Vector.size() ==1) singleClass = true; for(int i=0;i <maxl;i++){
	 * total=0; if(showProgress()) msg.progress(i,maxl);
	 * 
	 * double dis = 10000; double d_temp =0; int classes =0;
	 * 
	 * 
	 * String[][] toClassify = new String[1][1]; toClassify[0][0] = data[i][0];
	 * HashMap ht = tfidf_build(toClassify); //get the word count and total for
	 * the to be classified msg int htTotal =0; Iterator enu =
	 * ht.values().iterator();//elements(); while(enu.hasNext()){ Integer t =
	 * (Integer)enu.next(); htTotal += t.intValue(); }
	 * 
	 * for(int j = 0; j < tfidf_Vector.size(); j++){
	 * 
	 * d_temp = sim4(ht, htTotal, j); total+=d_temp; if(d_temp <dis){ dis =
	 * d_temp; classes = j; }
	 *  } if(singleClass) ret[i][1] = new String(""+(d_temp)); else ret[i][1] =
	 * new String(""+(d_temp/total)); ret[i][0] =
	 * (((tfidf_onefolder)tfidf_Vector.elementAt(classes)).name);
	 *  }
	 * 
	 * if(showProgress()){ msg.hide(); msg = null; }
	 * 
	 * 
	 * 
	 * return ret; }
	 */
	//sim4-- variance of cosince distance
	//todo is the for the tobe classified msg, total is total words number of
	// this todo msg
	//index is for tfidf_Vector, which folder is being compared

	/*
	private double sim4(HashMap todo, int total, int index) {

		int total_folders = tfidf_Vector.size();

		int totalAll = 0; //all the words number of all folders
		for (int i = 0; i < total_folders; i++) {
			totalAll += ((tfidf_onefolder) tfidf_Vector.elementAt(i)).total_words;
		}

		double upper = 0; //upper part of sim4
		double totalW = 0;

		HashMap index_ht = ((tfidf_onefolder) tfidf_Vector.elementAt(index)).word_vec;
		int index_total = ((tfidf_onefolder) tfidf_Vector.elementAt(index)).total_words;

		Iterator enu = todo.entrySet().iterator();//keys();
		while (enu.hasNext()) {
			String word = (String) enu.next();
			int todo_times = ((Integer) todo.get(word)).intValue();

			//for each word, compute W(F, w). F is the index folder
			int times = 0;
			double FF = 0;
			if (index_ht.containsKey(word)) {
				times = ((Integer) index_ht.get(word)).intValue();
				FF = (double) times / index_total;
				//System.out.println("for "+word+", FF for folder "+index+" is:
				// "+FF);
			}
			//System.out.println("for "+word+", FF for folder "+index+" is:
			// "+FF);

			int timesAll = 0;
			int appearTimes = 0;
			for (int i = 0; i < total_folders; i++) {

				HashMap ht = ((tfidf_onefolder) tfidf_Vector.elementAt(i)).word_vec;
				if (ht.containsKey(word)) {
					appearTimes++;
					timesAll += ((Integer) ht.get(word)).intValue();
				}
			}

			double FFAll = 0;
			if (totalAll > 0)
				FFAll = (double) timesAll / totalAll;
			double TF = 0;
			if (FFAll != 0)
				TF = FF / FFAll;
			double IDF = 0;
			if (appearTimes > 0)
				IDF = (double) (total_folders * total_folders) / (appearTimes * appearTimes);
			else
				IDF = 100000; //a very large number
			double W = TF * IDF;
			//System.out.println("for "+word+", W for folder "+index+" is:
			// "+W);
			totalW += W;
			upper += W * todo_times;
		}

		double ret = 0;
		ret = upper / Math.min(total, totalW);
		//System.out.println("uppper is :"+upper+", ret is: "+ret);

		return ret;
	}
*/
	//  sim4-- variance of cosince distance
	//todo is the for the tobe classified msg, total is total words number of
	// this todo msg
	//index is for tfidf_Vector, which folder is being compared
	private double sim4_new(final HashMap todo, final int total, final int index) {

		

		double upper = 0; //upper part of sim4
		double totalW = 0;

		//HashMap index_ht = classWordCount[index];
		//int index_total = wordTotals[index];

		Iterator enu = todo.keySet().iterator();//keys();
		while (enu.hasNext()) {
			String word = (String) enu.next();
			int todo_times = ((Integer) todo.get(word)).intValue();

			//for each word, compute W(F, w). F is the index folder
			int times = 0;
			double FF = 0;
			if (classWordCount[index].containsKey(word)) {
				times = ((Integer) classWordCount[index].get(word)).intValue();
				FF = (double) times / wordTotals[index];
				//System.out.println("for "+word+", FF for folder "+index+" is:
				// "+FF);
			}
			//System.out.println("for "+word+", FF for folder "+index+" is:
			// "+FF);

			int timesAll = 0;
			int appearTimes = 0;
			for (int i = 0; i < NUMBER_CLASSES; i++) {
				if(wordTotals[i]<1)
					continue;
				
				HashMap ht = classWordCount[i];//((tfidf_onefolder)tfidf_Vector.elementAt(i)).word_vec;
				if (ht.containsKey(word)) {
					appearTimes++;
					timesAll += ((Integer) ht.get(word)).intValue();
				}
			}

			double FFAll = 0;
			if (sumTotalWordCounts > 0)
				FFAll = (double) timesAll / sumTotalWordCounts;
			double TF = 0;
			if (FFAll != 0)
				TF = FF / FFAll;
			double IDF = 0;
			if (appearTimes > 0)
			{	IDF = (double) (NUMBER_CLASSES * NUMBER_CLASSES) / (appearTimes * appearTimes);//TODO:
																							   // get
			}																					   // numbers
			else
			{	
				IDF = 100000; //a very large number
			}
			double W = TF * IDF;
			//System.out.println("for "+word+", W for folder "+index+" is:
			// "+W);
			totalW += W;
			upper += W * todo_times;
		}

		double ret = 0;
		ret = upper / Math.min(total, totalW);
		//System.out.println("uppper is :"+upper+", ret is: "+ret);

		return ret;
	}
	/** ** END of TF-IDF classifier **** */

	//save the computed tfidf_vector to file
	public final void save(String filename) {

		try {
			System.out.println("tfidf - save : " + filename);
			FileOutputStream out = new FileOutputStream(filename);
			GZIPOutputStream gout = new GZIPOutputStream(out);
			ObjectOutputStream s = new ObjectOutputStream(gout);
			s.writeObject(getID());
			s.writeObject(getFeature());

			//s.writeObject(tfidf_Vector);
			s.writeDouble(getThreshold());
			s.writeBoolean(isOneClass());
			s.writeBoolean(inTraining);
			s.writeInt(wordTotals.length);
			for(int i=0;i<wordTotals.length;i++){
				s.writeInt(wordTotals[i]);
			}
			s.writeInt(classWordCount.length);
			for(int i=0;i<classWordCount.length;i++){
				s.writeObject(classWordCount[i]);
			}
			
			s.writeInt(sumTotalWordCounts);
			
			s.flush();
			gout.close();
			s.close();
			System.out.println("done saving..going to return");
		} catch (Exception e) {
			System.out.println("Exception in file write out: " + e);
		}

	}

	//load back the centroid vector from the file
	public final void load(String filename) {
		try {
			
			FileInputStream in = new FileInputStream(filename);
			GZIPInputStream gin = new GZIPInputStream(in);
			ObjectInputStream s = new ObjectInputStream(gin);
			s.readObject(); //this is the id string
			setFeature((String) s.readObject());
			//tfidf_Vector = (Vector) s.readObject();
			setThreshold(s.readDouble());
			isOneClass(s.readBoolean());
			inTraining = s.readBoolean();
			int len = s.readInt();
			wordTotals = new int[len];
			
			for(int i=0;i<wordTotals.length;i++){
				wordTotals[i] = s.readInt();
			}
			len = s.readInt();
			classWordCount = new HashMap[len];
			for(int i=0;i<classWordCount.length;i++){
				classWordCount[i] = (HashMap)s.readObject();
			}
			sumTotalWordCounts = s.readInt();
		} catch (Exception e) {
			System.out.println("Exception in file read in: " + e);
		}
	}

	private void tfidf_build_per_class(String data, int classn) {

		//String s = data[i][0];
		StringTokenizer st = new StringTokenizer(data, MLearner.tokenizer, true);
		while (st.hasMoreTokens()) {
			wordTotals[classn]++;
			sumTotalWordCounts++;
			String sub = st.nextToken();
			if (classWordCount[classn].containsKey(sub)) {
				Integer t = (Integer) classWordCount[classn].get(sub);
				int val = t.intValue();
				classWordCount[classn].put(sub, (new Integer(val + 1)));
			} else {
				classWordCount[classn].put(sub, (new Integer(1)));
			}
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[],
	 *      java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		if (msgData == null || msgData.isEmpty()) {
			return;// throw (new Exception("header for training is empty"));
		}

		//int num = header[0];
		//double total_length = data.length;
		//int start = 0;
		//int length=0;

		//if we want to show a gui.
		int classnumber = getClassNumber(Label);

		tfidf_build_per_class(msgData.getBody(), classnumber);

		inTraining = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {

		if (inTraining) {
			//tfidf_Vector.removeAllElements();
			/*for (int j = 0; j < NUMBER_CLASSES; j++) {

				if (wordTotals[j] < 1) {
					continue;
				}

				tfidf_onefolder TF = new tfidf_onefolder(classWordCount[j], getClassString(j), wordTotals[j]);
				//tfidf_Vector.addElement(TF);
			}*/
			
			inTraining = false;
		}

		if (msgData == null || msgData.isEmpty()) {
			throw (new machineLearningException("the test data is empty"));
		}

		//		int maxl = data.length;//save data length lookup
		//double total=0;
		//boolean singleClass = false;//if we only have one class this has to
		// do with normalization
		//if(tfidf_Vector.size() ==1)
		//    singleClass = true;

		double dis = 0;
		double d_temp = 0;
		int classes = 0;
		int total = 0;

		
		HashMap ht1 = tfidf_build(msgData.getBody());

		//get the word count and total for the to be classified msg
		int htTotal = 0;
		Iterator enu = ht1.keySet().iterator();
		while (enu.hasNext()) {
			
			Integer t = (Integer)ht1.get(enu.next());
			htTotal += t.intValue();
		}

		boolean first = true;
		for (int j = 0; j < NUMBER_CLASSES; j++) {
			if (classWordCount[j].size() < 1) {
				continue;
			}

			d_temp = sim4_new(ht1, htTotal, j);
			total += d_temp;
			if (first) {
				first = false;
				dis = d_temp;
				classes = j;
			} else if (d_temp > dis) {
				dis = d_temp;
				classes = j;
			}

		}
		//if(singleClass)
		//ret[i][1] = new String(""+(d_temp));
		//else
		// ret[i][1] = new String(""+(d_temp/total));
		// ret[i][0] =
		// (((tfidf_onefolder)tfidf_Vector.elementAt(classes)).name);

		//}
//System.out.print("dis"+dis+" "+total);
		return new resultScores(classes, (int) (2.5*dis),msgData.getMailref());//ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub

	}

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_TFIDF;
    }
}

/** ********** sub class ************ */
// to store the information about one folder
class tfidf_onefolder implements Serializable {

	public String name = ""; //name of this folder
	public HashMap word_vec;
	public int total_words = 0;

	public tfidf_onefolder() {
		word_vec = new HashMap();
		total_words = 0;
	}

	public tfidf_onefolder(HashMap ht, String n) {

		name = n;
		word_vec = ht;
		//count the total words number
		//Enumeration enu = ht.elements();
		Iterator enu = ht.values().iterator();
		while (enu.hasNext()) {
			Integer t = (Integer) enu.next();
			total_words += t.intValue();
		}

	}

	public tfidf_onefolder(HashMap ht, String n, int totalw) {

		name = n;
		word_vec = ht;
		//count the total words number
		total_words = totalw;
	}

	public final String toString() {

		String s = "Folder name: " + name + "\n";
		s += "distinct words number :" + word_vec.size() + "\n";
		s += "total word number: " + total_words + "\n";
		s += "first ten words:\n";
		Iterator enu = word_vec.entrySet().iterator();
		for (int i = 0; i < 10 && enu.hasNext(); i++) {
			String word = (String) enu.next();
			s += word;
			s += ": " + ((Integer) word_vec.get(word)).intValue();
		}
		return s;
	}

}//class

