package metdemo.MachineLearning;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import metdemo.Parser.EMTEmailMessage;

/**
 * This class implement the general NB text classifier from book "Machine
 * Learning", pp180-182
 * <P>
 * addition of one class handling by shlomo
 *  
 */

public class TextClassifier extends MLearner {

	//  private JDBCConnect m_jdbcUpdate, m_jdbcView;
	//  private JDBCConnect m_jdbc;
	//private Vector allWords_Vector = new Vector();
	//private Vector nPosition_Vector = new Vector();
	//private Vector ratio_Vector = new Vector();
	//private Vector classes_Vector = new Vector();
	//private int total = 0;

	//new version
	private HashMap[] ClasswordCounts = new HashMap[NUMBER_CLASSES];
	private double totalExamples = 0;
	private double[] classCounts = new double[NUMBER_CLASSES];
	private int[] tokensSeenPerClass = new int[NUMBER_CLASSES];

	/** Constructor */
	public TextClassifier() {
		super.setID("TextClassifier");
		for (int i = 0; i < NUMBER_CLASSES; i++) {
			ClasswordCounts[i] = new HashMap();

		}

	}

	/**
	 * Constructor
	 *  
	 */
	/*
	 * public TextClassifier(){
	 * 
	 * super.setID("TextClassifier"); //m_jdbc = jdbc; //m_jdbcView = jdbcView; }
	 */
	/**
	 * Build the hashtable for all strings in the set, including all words and
	 * tokens
	 *  
	 */
	/*private HashMap buildTokenCounts(final String data[][]) {

		HashMap ht = new HashMap();
		total = 0;

		for (int i = 0; i < data.length; i++) {

			StringTokenizer st = new StringTokenizer(data[i][0], MLearner.tokenizer);//"
																					 // \t\n\r\f"+"/<>");
			while (st.hasMoreTokens()) {

				String sub = st.nextToken();
				if (ht.containsKey(sub)) {
					Double t = (Double) ht.get(sub);
					//double val = t.doubleValue();
					ht.put(sub, (new Double(t.doubleValue() + 1)));
				} else {
					ht.put(sub, (new Double(1.0)));
				}

				total++;
			}
		}

		return ht;
	}
*/
	private int buildTokenCountsPerClass(final String data, int classnum) {

		int total = 0;

		//for(int i=0;i<data.length;i++){

		StringTokenizer st = new StringTokenizer(data, MLearner.tokenizer);//"
																		   // \t\n\r\f"+"/<>");
		while (st.hasMoreTokens()) {

			String sub = st.nextToken();
			if (ClasswordCounts[classnum].containsKey(sub)) {
				Double t = (Double) ClasswordCounts[classnum].get(sub);
				//double val = t.doubleValue();
				ClasswordCounts[classnum].put(sub, (new Double(t.doubleValue() + 1)));
			} else {
				ClasswordCounts[classnum].put(sub, (new Double(1.0)));
			}

			total++;
		}
		//}
		return total;
	}
/*
	public final void train(final short header[], final String classes[], final String data[][], final int noth,
			final String TARGET_CLASS) throws Exception {

		if (header == null || header.length <= 0)
			throw (new Exception("header for training is empty"));

		//int num =header[0] ;
		double total_length = data.length;
		int start = 0;
		int length = 0;
		BusyWindow bw = null;
		if (showProgress()) {
			bw = new BusyWindow("Training TextClassifier", "Progress Report");
			bw.show();
		}

		int maxl = data.length;

		for (int j = 0; j < header[0]; j++) {

			if (showProgress())
				bw.progress(j, header[0]);

			start += length;
			length = header[j + 1];

			HashMap allWords = new HashMap();
			int nPosition = 0;
			double ratio = 0;

			String temp[][] = new String[length][1];
			for (int k = 0; k < length; k++) {
				temp[k][0] = data[start + k][0];
			}

			allWords = buildTokenCounts(temp);
			nPosition = total;

			ratio = (double) length / (double) total_length;

			allWords_Vector.addElement(allWords);
			nPosition_Vector.addElement(new Integer(nPosition));
			ratio_Vector.addElement(new Double(ratio));

			classes_Vector.addElement(classes[j]);

		}//for
		if (showProgress()) {
			bw.hide();
			bw = null;
		}
	}
*/
	//return the probability of s belongs to model
	private final double prob(final HashMap model, final int total_pos, final double ratio, final String data) {
		
		double prob = 0;// * ratio;

		StringTokenizer st = new StringTokenizer(data, MLearner.tokenizer);
		//" .,()\\/<>#{}[]:\t\n\r\f\"=;|?+;*&^%@~_");//" \t\n\r\f"+"/<>");

		int stat = 0;

		while (st.hasMoreTokens()) {

			String sub = st.nextToken();
			if (model.containsKey(sub)) {
				Double t = (Double) model.get(sub);
				//double val = t.doubleValue();
				//double temp =
				// t.doubleValue();//10000*(t.doubleValue()+1)/(total_pos +
				// model.size());
				//prob *= temp;
				//prob *= /*1000 * */((t.doubleValue() + 1.) / (total_pos + model.size()));
				prob += Math.log((t.doubleValue() + 1.) / (double)(total_pos + model.size()));
				
				stat++;
			} else {
				//		double temp = (10000*1)/(double)(total_pos + model.size());
				//prob *= (/*1000 **/ 1.) / (total_pos + model.size());
				
				prob += Math.log((/*1000 **/ 1.) / (double)(total_pos + model.size()));
			}

		}
		
		//		System.out.print("hit times: "+stat + " and prob: " + prob);
		prob += Math.log(ratio);
		return (prob);

	}

	/**
	 * classify the data given some previous training data
	 */
	/*public final String[][] classify(final String data[][]) throws Exception {

		double holders[] = new double[classes_Vector.size() + 1];
		if (allWords_Vector == null || allWords_Vector.size() <= 0) {
			throw (new Exception("train model is emtpy"));
		}

		if (data == null || data.length == 0) {
			throw (new Exception("the test data is empty"));
		}

		BusyWindow msg = null;
		if (showProgress()) {
			msg = new BusyWindow("Classify TextClassifier", "Progress Report");
			msg.show();
		}

		String ret[][] = new String[data.length][2 + classes_Vector.size()];
		int maxl = data.length;//save data length lookup
		boolean singleClass = false;//if one class so no need for normalization
		if (classes_Vector.size() == 1)
			singleClass = true;

		for (int i = 0; i < maxl; i++) {
			if (showProgress())
				msg.progress(i, maxl);
			/*
			 * double d1 = prob(allWords_1, nPositions_1, ratio1, data[i][0]);
			 * double d2 = prob(allWords_2, nPositions_2, ratio2, data[i][0]);
			 * 
			 * System.out.println("prob 1="+d1+", prob2="+d2);
			 * 
			 * if(d1>=d2) ret[i][0]="class 1"; else ret[i][0]="class 2";
			 * /

			double dis = -10000;
			double d_temp = 0;
			int classes = 0; //label offset
			double totald = 0.0001;
			for (int j = 0; j < allWords_Vector.size(); j++) {
				HashMap allWords = (HashMap) allWords_Vector.elementAt(j);
				int pos = ((Integer) nPosition_Vector.elementAt(j)).intValue();
				double ratio = ((Double) ratio_Vector.elementAt(j)).doubleValue();
				d_temp = prob(allWords, pos, ratio, data[i][0]);
				totald += d_temp;
				holders[j] = d_temp;

				if (d_temp > dis) {
					dis = d_temp;
					classes = j;
				}

			}

			ret[i][0] = ((String) classes_Vector.elementAt(classes));

			if (singleClass) {
				ret[i][1] = new String("" + Math.log(d_temp));
				//System.out.print(" " + d_temp);
			} else
				ret[i][1] = new String("" + (dis / totald));

			for (int j = 0; j < classes_Vector.size(); j++) {
				ret[i][2 + j] = new String("" + (holders[j] / totald));
			}

		}
		if (showProgress()) {
			msg.hide();
			msg = null;
		}
		return ret;
	}
*/
	/**
	 * @param filename
	 *            to save it to
	 */
	public final void save(final String filename) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			GZIPOutputStream gout = new GZIPOutputStream(out);
			ObjectOutputStream s = new ObjectOutputStream(gout);
			s.writeObject(getID());
			s.writeObject(getFeature());
			s.writeObject(getInfo());
			//s.writeObject(allWords_Vector);
			//s.writeObject(nPosition_Vector);
			//s.writeObject(ratio_Vector);
			//s.writeObject(classes_Vector);
			//s.writeInt(total);
			s.writeDouble(getThreshold());
			s.writeBoolean(isOneClass());
			s.writeInt(ClasswordCounts.length);
			for (int i = 0; i < ClasswordCounts.length; i++) {
				s.writeObject(ClasswordCounts[i]);
			}
			s.writeDouble(totalExamples);
			s.writeInt(classCounts.length);
			for (int i = 0; i < classCounts.length; i++) {
				s.writeDouble(classCounts[i]);
				s.writeInt(tokensSeenPerClass[i]);
			}
			s.flush();
			gout.close();
			out.close();
		} catch (Exception e) {
			System.out.println("error in save file:" + e);
		}

	}

	//load back the centroid vector from the file
	public final void load(final String filename) {
		try {
			FileInputStream in = new FileInputStream(filename);
			GZIPInputStream gin = new GZIPInputStream(in);
			ObjectInputStream s = new ObjectInputStream(gin);
			s.readObject(); //this is the id string
			setFeature((String) s.readObject());
			setInfo((String) s.readObject());

			//allWords_Vector = (Vector) s.readObject();
			//nPosition_Vector = (Vector) s.readObject();
			//ratio_Vector = (Vector) s.readObject();
			//classes_Vector = (Vector) s.readObject();
			//total = s.readInt();
			setThreshold(s.readDouble());
			isOneClass(s.readBoolean());
			int n = s.readInt();
			ClasswordCounts = new HashMap[n];
			for (int i = 0; i < ClasswordCounts.length; i++) {
				ClasswordCounts[i] = (HashMap) s.readObject();
			}
			totalExamples = s.readDouble();
			n = s.readInt();
			classCounts = new double[n];
			tokensSeenPerClass = new int[n];
			for (int i = 0; i < classCounts.length; i++) {
				classCounts[i] = s.readDouble();
				tokensSeenPerClass[i] = s.readInt();
			}

			in.close();
		} catch (Exception e) {
			System.out.println("error in read file:" + e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[],
	 *      java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		if (msgData == null || msgData.isEmpty())
			return;

		int classnumber = getClassNumber(Label);

		int total = buildTokenCountsPerClass(msgData.getBody(), classnumber);
		totalExamples++;
		classCounts[classnumber]++;
		
		tokensSeenPerClass[classnumber] += total;
		//  ratio = (double)length/(double)total_length;

		// allWords_Vector.addElement(allWords);
		// nPosition_Vector.addElement(new Integer(nPosition));
		// ratio_Vector.addElement(new Double(ratio));

		//classes_Vector.addElement(classes[j]);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {
		double holders[] = new double[NUMBER_CLASSES];
		double dis = 0;
		double d_temp = 0;
		int classes = 0; //label offset

		double totald = 0;

		//if(allWords_Vector == null || allWords_Vector.size()<=0){
		//    throw (new Exception("train model is emtpy"));
		//}

		if (msgData == null || msgData.isEmpty()) {
			throw (new machineLearningException("the test data is empty"));
		}

		//String ret[][] = new String[data.length][2+classes_Vector.size()];
		//	int maxl = msgData.getSize();//save data length lookup
		//boolean singleClass = false;//if one class so no need for
		// normalization
		//if(classes_Vector.size()==1)
		//  	    singleClass = true;

		//for(int i=0;i<maxl;i++)
		{
			/*
			 * double d1 = prob(allWords_1, nPositions_1, ratio1, data[i][0]);
			 * double d2 = prob(allWords_2, nPositions_2, ratio2, data[i][0]);
			 * 
			 * System.out.println("prob 1="+d1+", prob2="+d2);
			 * 
			 * if(d1>=d2) ret[i][0]="class 1"; else ret[i][0]="class 2";
			 */
			boolean first = true; //ie save first score and then compare to later ones
			for (int j = 0; j < NUMBER_CLASSES; j++) {
				//HashMap allWords = (HashMap)allWords_Vector.elementAt(j);
				//int pos =
				// ((Integer)nPosition_Vector.elementAt(j)).intValue();
				if (classCounts[j] > 0) {
					double ratio = classCounts[j] / totalExamples;//((Double)ratio_Vector.elementAt(j)).doubleValue();
					
					d_temp = prob(ClasswordCounts[j], tokensSeenPerClass[j], ratio, msgData.getBody());
					//System.out.print(d_temp + " ");
					totald += d_temp;
					
					holders[j] = d_temp;
					if(first){
					first = false;
						dis = d_temp;
						classes = j;
				
					}
					else if (d_temp > dis) {
						dis = d_temp;
						classes = j;
					}

				}
			}

			//ret[i][0] = ((String)classes_Vector.elementAt(classes));

			//  if(singleClass){
			//	ret[i][1] = new String(""+Math.log(d_temp));
			//System.out.print(" " + d_temp);
			//   }
			//  else
			//  ret[i][1] = new String(""+(dis/totald));

			//for(int j=0;j<classes_Vector.size();j++)
			//{
			//   ret[i][2+j] = new String(""+(holders[j]/totald));
			//}

		}
		//System.out.println("classes " + classes+"rawscore: " + dis/totald );
		dis = 150 * (dis / totald);  //will give 1-75 range

		if (dis > resultScores.LARGESCORE) {
			System.out.println("too large score");
			dis = resultScores.LARGESCORE;
		} else if (dis < resultScores.SMALLSCORE) {
			System.out.println("score too small");
			dis = resultScores.SMALLSCORE;
		}
		return new resultScores(MLearner.getClassString(classes), (int) dis,msgData.getMailref());
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
       return MLearner.ML_TEXTCLASSIFIER;
    }
}

/**
 * 
 * //get bodys for yy <=uid <=xx, includingly public String[][] getBlock(int
 * start_uid, int end_uid){ String query; String data[][];
 * 
 * synchronized (m_jdbcView){ try{
 * 
 * query = "select M.body from message M, email E where M.mailref=E.mailref and
 * E.uid>="+start_uid+" and E.uid <="+end_uid; m_jdbcView.getSqlData(query);
 * data = m_jdbcView.getRowData(); return data;
 * 
 * }catch(Exception e){ System.out.println("get body exception: "+e); }
 *  } return null; }
 */

