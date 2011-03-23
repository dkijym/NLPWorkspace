package metdemo.MachineLearning;

//import metdemo.contentDis;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.BusyWindow;
import metdemo.dataStructures.contentDis;

/**
* Class which impliments NGram machine learning model
* 
* @author Ke Wang,Shlomo Hershkop (modifications)
*/

public class NGramLimited extends MLearner {

	//private EMTDatabaseConnection m_jdbcView;
	//private EMTDatabaseConnection m_jdbc;
	//private Vector centroid_Vector = new Vector();
	//private Vector classes_Vector = new Vector();
	private int N = 10; //by default use 5-gram although can be set by the user
	private int limit = 125;
	// before model time
	private Hashtable stopwords = new Hashtable(); //store stopwords

	private Hashtable keywords = new Hashtable(); //store keywords

	//for faster implimentation
	private HashMap[] classCentroids = new HashMap[NUMBER_CLASSES]; //centroid

	// and count
	// per class
	private int[] classCounts = new int[NUMBER_CLASSES]; //number examples per

	// class
	private int totalTraining = 0;

	private boolean inTrainingMode = true;

	double sumj[];

	/** constructor */
	public NGramLimited() {
		super.setID("LimitedNGram");
		for (int i = 0; i < NUMBER_CLASSES; i++) {
			classCentroids[i] = new HashMap();
			classCounts[i] = 0;
		}
	}

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_LIMITEDNGRAM;
    }
	/** sets the size of the NGram */
	public final void setN(final int num) {
		N = num;
	}

	/** returns the current ngram size */
	public final int getN() {
		return N;
	}

	/** ******************************* */
	/* the part for using stop words */
	/** ******************************* */

	/**
	 * This method reads in the stop words
	 * 
	 * @param filename
	 *            to read in stopwords
	 */
	public final void read_stopwords(final String filename) {

		String s;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			s = in.readLine();
			while (s != null) {
				if (s.length() > 0)
					//words.addElement(s.trim());
					stopwords.put(s.trim(), new Integer(1));
				s = in.readLine();
			}

		} catch (Exception e) {
			System.out.println("read stopword file: " + e);
		}

	}

	/**
	 * given filename, read in keywords
	 * 
	 * @param filaname
	 *            the filename to read in the keywords
	 */
	public final void read_keywords(final String filename) {

		String s;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			s = in.readLine();
			while (s != null) {
				if (s.length() > 0)
					//words.addElement(s.trim());
					keywords.put(s.trim(), new Integer(1));
				s = in.readLine();
			}

		} catch (Exception e) {
			System.out.println("read keyword file: " + e);
		}

	}
	/**
	 * pass in array of string as content, group them by similarity
	 * 
	 * @return vector of groups, each group is a vector also, using the index of
	 *         array as id
	 */
	public static final ArrayList<ArrayList> groupContent(String[] data,
			final double threshhold, int nsize, boolean show, boolean sort) {

		if (data == null) {
			System.out.println("no data to group by content");
			return null;
		}

		ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
		int size = data.length;

		if (size == 0) {
			System.out.println("data size is 0. no data to group by content");
			return null;
		}
		BusyWindow bw = null;
		if (show) //will check if to show progress. set in mlearner
		{
			bw = new BusyWindow("Progress Report", "NGRAM Grouping",true);
			bw.setVisible(true);
		}

		//		delete the double spaces
		//????
		HashMap precalculatedHashmaps[] = new HashMap[data.length];
		double precalculatedLengths[] = new double[data.length];
		System.out.println("precalculating");
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i].trim().replaceAll("  ","");
			precalculatedHashmaps[i] = NgramUtilities.getNGramLimited(data[i], nsize,125);
			precalculatedLengths[i] = NgramUtilities.hashmapLength(precalculatedHashmaps[i]);
		}
		System.out.println("done precalc");

		int[] flags = new int[size]; //to indicate whether this string has been
		// grouped
		for (int i = 0; i < size; i++)
			flags[i] = 0; //set to 1 if it's grouped

		int base = 0; // the base one for one group

		int maxl = size;
		int done = 0;

		//repeatly find the groups, until all in group
		//core of this function
		while (true) {
			if (show) {
				bw.progress(done, maxl);
			}
			//find the base for each group
			int i = 0;
			while (i < size && flags[i] == 1) {
				i++;
			}
			if (i >= size) //reach the end
			{
				break;
			}
			//since we arent breaking
			base = i;

			ArrayList<contentDis> oneGroup = new ArrayList<contentDis>();
			oneGroup.add(new contentDis(base, 1.0));
			flags[base] = 1;

			//HashMap baseHash = NgramUtilities.getNGram(data[base], nsize);

			//traverse all string to find similar one
			for (int j = i + 1; j < size; j++) { //j=base to speed things up
				if (flags[j] == 1) {
					continue;
				}
				//HashMap ht = NgramUtilities.getNGram(data[j], nsize);
				//System.out.println(ht);
				double dis = precalc_cosDist(precalculatedHashmaps[base],
						precalculatedLengths[base], precalculatedHashmaps[j],
						precalculatedLengths[j]);//baseHash, ht);
				//System.out.println(" " + dis);
				if (dis >= threshhold) {
					oneGroup.add(new contentDis(j, dis));
					flags[j] = 1;
					precalculatedHashmaps[j] = null;
				}
			}
			precalculatedHashmaps[base] = null;

			done += oneGroup.size();
			//	        	System.out.println("done:"+done+", max:"+maxl);
			if (sort) {
				groups.add(NgramUtilities.sort(oneGroup));
			} else {
				groups.add(oneGroup);
			}
		}//while true

		//test output
		/*
		 * for(int i=0;i <groups.size();i++){ Vector vec =
		 * (Vector)groups.elementAt(i); System.out.println("\n Group "+i+": ");
		 * for(int j=0;j <vec.size();j++){ contentDis cd =
		 * (contentDis)vec.elementAt(j); System.out.println("index: "+cd.id+",
		 * dis:"+cd.dis); System.out.println("String is: "+data[cd.id]); } }
		 */

		if (show) {
			bw.setVisible(false);
			bw = null;
		}
		return groups;
	}

	/**
	 * Replace the stopwords in old by r. r can be * or nothing then later can
	 * ingore r when do accounting about words
	 * 
	 * @param old
	 *            the old string which we are modifying
	 * @param r
	 *            the replacement part
	 *  
	 */
	public final String replace(final String old, final String r) {

		StringTokenizer st = new StringTokenizer(old, MLearner.tokenizer, true);
		//" \t\n\r\f"+".,;?!/", true);
		String s = new String();
		while (st.hasMoreTokens()) {
			String t = st.nextToken();
			if (!stopwords.containsKey(t))
				s += t;
			else
				s += r;
		}
		return s;
	}

	

	/**
	 * compute the cosine distance of these two hashtable given the length of
	 * each
	 */
	private static double precalc_cosDist(final HashMap ht1, final double sum1,
			final HashMap ht2, final double sum2) {

		String sub;
		double total = 0;

		Iterator keys = ht1.keySet().iterator();
		while (keys.hasNext()) {
			sub = (String) keys.next();
			if (ht2.containsKey(sub)) {
				total += (((Double) ht1.get(sub)).doubleValue() * ((Double) ht2
						.get(sub)).doubleValue());
			}
		}
		//System.out.println("common words number: "+same);
		//double ret = total/(Math.sqrt(sum1*sum2));
		return (total / (Math.sqrt(sum1 * sum2)));//ret;
	}

	/**
	 * compute the cosine distance of these two hashtable
	 */
	private static double cosDist(final HashMap ht1, final HashMap ht2) {

		double sum1 = 0;
		double sum2 = 0;
		String sub;
		double total = 0;
		double val = 0;

		Iterator keys = ht2.keySet().iterator();
		while (keys.hasNext()) {
			//t = (Double)ht2.get((String)keys.next());//sub);
			//val = t.doubleValue();
			val = ((Double) ht2.get(keys.next())).doubleValue();
			sum2 += val * val;
			// val*val;
		}

		keys = ht1.keySet().iterator();
		while (keys.hasNext()) {

			sub = (String) keys.next();
			val = ((Double) ht1.get(sub)).doubleValue();//frm first loop moved
			// here
			sum1 += val * val;
			if (ht2.containsKey(sub)) {
				//t = (Double)ht1.get(sub);
				//val = t.doubleValue();
				//	t2 = (Double)ht2.get(sub);
				//val2 = t2.doubleValue();
				//total += val*val2;
				total += ((Double) ht1.get(sub)).doubleValue()
						* ((Double) ht2.get(sub)).doubleValue();
				//same++;
			}
		}
		//System.out.println("common words number: "+same);
		//double ret = total/(Math.sqrt(sum1*sum2));
		return (total / (Math.sqrt(sum1 * sum2)));//ret;
	}

	/**
	 * compute the cosine distance of these two hashtable, fast version since
	 * rmeebers old ht2
	 */
	private static double cosDist1(final HashMap ht1, final HashMap ht2,
			final double sum2) {

		double sum1 = 0;
		String sub;
		double total = 0;
		//int same=0;
		//Double t, t2;
		double val;//, val2;
		//int total1=0;
		Iterator keys = ht1.keySet().iterator();
		while (keys.hasNext()) {
			//	    total1++;
			sub = (String) keys.next();
			val = ((Double) ht1.get(sub)).doubleValue();
			sum1 += val * val;
			// ((Double)ht1.get(sub)).doubleValue();//frm first loop moved here
			if (ht2.containsKey(sub)) {

				total += ((Double) ht1.get(sub)).doubleValue()
						* ((Double) ht2.get(sub)).doubleValue();
				//same++;
			}
		}

		return (total / (Math.sqrt(sum1 * sum2)));//ret;
	}

	/**
	 * save the computed centroid to file
	 * 
	 * @param filename
	 *            is the file to save to
	 */
	public final void save(final String filename) {
		try {

			FileOutputStream out = new FileOutputStream(filename);
			GZIPOutputStream gout = new GZIPOutputStream(out);

			ObjectOutputStream s = new ObjectOutputStream(gout);

			s.writeObject(getID());
			s.writeObject(getFeature());
			s.writeObject(getInfo());

			s.writeInt(N);
			//System.out.println("11 " + centroid_Vector.size());
			//s.writeInt(centroid_Vector.size());
			//for (int i = 0; i < centroid_Vector.size(); i++) {
			//  System.out.println("**" + i);
			//	s.writeObject(centroid_Vector.elementAt(i));
			//}
			//		s.writeObject(centroid_Vector);
			//System.out.println("21");

			//s.writeObject(classes_Vector);
			s.writeDouble(getThreshold());
			s.writeBoolean(isOneClass());
			s.writeInt(classCentroids.length);
			for (int i = 0; i < classCentroids.length; i++) {
				s.writeObject(classCentroids[i]);
			}
			s.writeInt(totalTraining);
			s.writeInt(classCounts.length);
			for (int i = 0; i < classCounts.length; i++) {
				s.writeInt(classCounts[i]);
			}
			s.flush();
			gout.close();
			s.close();
			//System.out.println("done saving..going to return");
		} catch (Exception e) {
			System.out.println("Exception in file write out: " + e);
		}

	}

	/**
	 * load back the centroid vector from the file
	 * 
	 * @param filename
	 *            the name to load up.
	 */
	public final void load(final String filename) {
		try {
			FileInputStream in = new FileInputStream(filename);
			GZIPInputStream gin = new GZIPInputStream(in);
			ObjectInputStream s = new ObjectInputStream(gin);
			s.readObject();//this is the id..used elsewhere
			setFeature((String) s.readObject());
			setInfo((String) s.readObject());
			N = s.readInt();
			//centroid_Vector = (Vector)s.readObject();
			//int c = s.readInt();
			//for (int i = 0; i < c; i++)
			//	centroid_Vector.addElement(s.readObject());
			//classes_Vector = (Vector) s.readObject();
			setThreshold(s.readDouble());
			isOneClass(s.readBoolean());
			int cn = s.readInt();
			classCentroids = new HashMap[cn];
			for (int i = 0; i < cn; i++) {
				classCentroids[i] = (HashMap) s.readObject();
			}
			totalTraining = s.readInt();
			cn = s.readInt();
			classCounts = new int[cn];
			for (int i = 0; i < cn; i++) {
				classCounts[i] = s.readInt();
			}

		} catch (Exception e) {
			System.out.println("Exception in file read in: " + e);
		}

	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[],
	 *      java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {

		if (msgData == null || Label == null) {
			return; //we could in theory throw an exception
		}

		inTrainingMode = true;

		String sub;
		double v, val;
		int classnumber = getClassNumber(Label);
		//find centroid for every class's training data
		HashMap tht = NgramUtilities.getNGramLimited(msgData.getBody(), N,limit);
		Iterator keys = tht.keySet().iterator();
		while (keys.hasNext()) {
			sub = new String((String) keys.next());
			if (classCentroids[classnumber].containsKey(sub)) {
				val = ((Double) tht.get(sub)).doubleValue(); //t.doubleValue();
				v = ((Double) classCentroids[classnumber].get(sub))
						.doubleValue();//d.doubleValue();
				//v += val;
				classCentroids[classnumber].put(sub, (new Double(v + val)));
			} else {
				classCentroids[classnumber].put(sub, tht.get(sub));//was t
			}
		}//while

		totalTraining++;
		classCounts[classnumber]++;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData)
			throws machineLearningException {

		//need to check how many examples seen so far.
		if (totalTraining == 0) {
			throw (new machineLearningException("Model centroid is empty"));

		}

		if (msgData == null || msgData.isEmpty()) {
			throw (new machineLearningException("the test data is empty"));
		}

		//	String ret[] = new String[2+NUMBER_CLASSES];
		//this will speed things up later for doing co-dist
		if (inTrainingMode) {
			sumj = new double[NUMBER_CLASSES];

			for (int j = 0; j < NUMBER_CLASSES; j++) {
				sumj[j] = 0;
				if (this.classCounts[j] > 0) {
					Iterator keys = classCentroids[j].keySet().iterator();
					while (keys.hasNext()) {
						double d3 = ((Double) (classCentroids[j]).get(keys
								.next())).doubleValue()
								/ this.classCounts[j];
						sumj[j] += d3 * d3;
					}
				}

			}
			inTrainingMode = false;
		}
		/** for each element go and calc cos dist */
		int classes;
		double dis, total;
		//for(int i=0;i<data.length;i++){
		total = 0;

		dis = 0;
		classes = 0;
		HashMap ht = NgramUtilities.getNGramLimited(msgData.getBody(), N,limit);

		boolean first = true;
		//the one with the max distance is the most matched one
		for (int j = 0; j < NUMBER_CLASSES; j++) {
			if (classCounts[j] < 1) {
				continue;
			}

			double dis_temp = cosDist1_new(ht, this.classCentroids[j], sumj[j],
					classCounts[j]);
			//ret[2+j] = ""+dis_temp; //write down the calc for each class.
			total += dis_temp;
			if (first) {
				dis = dis_temp;
				classes = j;
				first = false;
			} else if (dis_temp > dis) {
				dis = dis_temp;
				classes = j;
			}

		}

		//normalize the score. by dividing
		//ret[1]=new String(""+(dis/total));

		//ret[0]=((String)classes_Vector.elementAt(classes));
		//     System.out.print(" " + i);
		//}
		int finalScore = (int) (100 * dis / total);//100 * dis / total

		return new resultScores(classes, (int) (finalScore), msgData
				.getMailref());//ret;
	}

	/**
	 * compute the cosine distance of these two hashtable, fast version since
	 * rmeebers old ht2
	 */
	private static double cosDist1_new(final HashMap ht1, final HashMap ht2,
			final double sum2, final int norms) {

		double sum1 = 0;
		String sub;
		double total = 0;
		//int same=0;
		//Double t,t2;
		double val;
		//int total1=0;
		Iterator keys = ht1.keySet().iterator();
		while (keys.hasNext()) {
			//	    total1++;
			sub = (String) keys.next();
			val = ((Double) ht1.get(sub)).doubleValue();
			sum1 += val * val;
			// ((Double)ht1.get(sub)).doubleValue();//frm first loop moved here
			if (ht2.containsKey(sub)) {

				total += ((Double) ht1.get(sub)).doubleValue()
						* (((Double) ht2.get(sub)).doubleValue() / norms);
				//same++;
			}
		}

		return (total / (Math.sqrt(sum1 * sum2)));//ret;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub
		totalTraining = 0;
		for (int i = 0; i < classCounts.length; i++) {
			classCounts[i] = 0;
		}
		for (int i = 0; i < classCentroids.length; i++) {
			classCentroids[i].clear();
		}

	}

}

