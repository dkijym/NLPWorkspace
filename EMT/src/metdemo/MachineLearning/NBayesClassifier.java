package metdemo.MachineLearning;


import java.util.*;
import java.io.*;
//import drasys.or.prob.*;
import java.lang.Math;
//import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.Utils;
import metdemo.Window.EmailInternalConfigurationWindow;


/**
 * Class to do Naive Bayes Classification
 * <P>
 * Extends the abstract {@link MLearner}Class
 * 
 * @author Shlomo Hershkop frm ana's code
 */

public class NBayesClassifier extends MLearner {

	public final static int SUBTYPE_NGRAM = 0;
	public final static int SUBTYPE_TXTCLASSIFIER = 1;

	/*
	 * if you add new feature type, you should check error handling in train and
	 * classify! methods
	 */
	public final static double ZERO = 0.001;
	/* I don't know... for calculating probability for reals */
	public final static double ERROR = 1;
	public final static double SSIZE = 10;
	public final static double m_Precision = 0.01;//(1e-6);//??
	/* save time */
	private final static String _MEAN = "_m";
	private final static String _VAR = "_v";
	private final static String _TOTAL = "_n";
//	private final static String _DISC = "_d";
//	private final static String _REAL = "_r";

	//final static int _DISC =0;
	//final static int _REAL =0;
	//kernel estimation
	private int m_NumValues[];// =0;
	private double[][] m_Weights;
	private double[] m_SumOfWeights;
	private double m_StandardDev;

	//private boolean hasModel;
	//private String Features;
	//private int[] header;
	//private ArrayList classnames;
	private int featureTotals[];
	private double[] classCounts;
	private double totalExamplesSeen = 0;
	//private int classnum;
	/*
	 * probs [num of classes] [number of features] - map: value_name -> count
	 * (prob)
	 */
	private HashMap[][] probs;
	private HashMap[][] Realprobs;
	private double m_seenNumbers[][];//for each number feature, keep a list of
	// all seen numbers.
	/* will be used to classify body parts .. hee hee */
	private MLearner txtclass = null;
	private int txt_type = 0; //type of text classifier, either ngram or
	// textclassifier(word tokens)
	private boolean[] featureArray;
	private boolean inTraining = true;

	/**
	 * Constructor just sets the id to naive bayes
	 *  
	 */
	public NBayesClassifier(int type, boolean[] farray) {
		
		if(type == SUBTYPE_NGRAM)
			super.setID("Naive Bayes + NGram");
		else
			super.setID("Naive Bayes + TextClassifier");
		//hasModel = false;
		txt_type = type;
		featureArray = farray;
		if (farray != null)
			setupStuff();

		if (txtclass == null) {
			System.out.println("building sub model class:"+txt_type);
			if (txt_type == NBayesClassifier.SUBTYPE_NGRAM)
				txtclass = new NGram();
			else
				txtclass = new TextClassifier();

			txtclass.setTargetLabel(getTargetLabel());
			txtclass.setProgress(false);//we dont want to show subclass
			// progresses.
		}
		
	}

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       if (txt_type == NBayesClassifier.SUBTYPE_NGRAM)
           return MLearner.ML_NBAYES_NGRAM;
       return MLearner.ML_NBAYES_TXTCLASS;
    }
    
    
	public void setLowerCase(boolean l){
		txtclass.setLowerCase(l);
		super.setLowerCase(l);
	}

	public void setFeature(String s){
		txtclass.setFeature(s);
		super.setFeature(s);
	}
	
	private void setupStuff() {
		/** will hold seen numbers for kernel part */
		m_seenNumbers = new double[featureArray.length][];
		m_Weights = new double[featureArray.length][];
		m_NumValues = new int[featureArray.length];
		m_SumOfWeights = new double[featureArray.length];
		featureTotals = new int[featureArray.length];

		for (int i = 0; i < featureArray.length; i++) {
			m_NumValues[i] = 0;
			m_seenNumbers[i] = new double[100];
			m_Weights[i] = new double[100];
		}

		/*
		 * initialize structures for probabilities of each class and of each
		 * feature
		 */
		classCounts = new double[MLearner.NUMBER_CLASSES];
		probs = new HashMap[MLearner.NUMBER_CLASSES][featureArray.length];
		Realprobs = new HashMap[MLearner.NUMBER_CLASSES][featureArray.length];
		for (int i = 0; i < MLearner.NUMBER_CLASSES; i++) {
			for (int j = 0; j < featureArray.length; j++) {
				if (featureArray[j]) {//only create if we are using that
					// feature
					probs[i][j] = new HashMap();
					if (EmailInternalConfigurationWindow.isFeatureDiscrete(j)) {
						probs[i][j].put("_default", new Double(0));

					}
				}

			}
			classCounts[i] = 0;
		}

	}


	/** converts integer constant to string description...utility finction */
	/*private final String stringType(final int i) {
		if (i == MLearner.REAL)
			return "real";
		if (i == MLearner.DISCRETE)
			return "discrete";
		if (i == MLearner.CLASS)
			return "class name";
		return "unknown type";
	}
*/
	/**
	 * checks if class is utility class - not to be shown
	 */
	//private final boolean util(final int i) {
	//		if (((String) classnames.get(i)).equals(_DISC) || ((String)
	// classnames.get(i)).equals(_REAL))
	//			return true;
	//		return false;
	//	}
	/**
	 * Sums up the members of the map. I assume they are doubles.
	 */

	/*private final int sumFreq(final HashMap map) {
		Set s = map.keySet();
		Iterator r = s.iterator();
		int sum = 0;
		while (r.hasNext())
			sum += ((Double) map.get(r.next())).intValue();
		return sum;
	}*/

	/**
	 * utility: divides each member of the map by denominator I assume they are
	 * doubles, and since this is a private procedure, I don't do error
	 * checking, because they ARE doubles
	 */
	//does laplace smoothing for now
	private final HashMap divideEach(final HashMap inmap, final double tot, final double m, final double p) {
		HashMap outmap = new HashMap();
		//	System.out.println("div each: " + p + " " + m);

		Set s = inmap.keySet();
		Iterator r = s.iterator();
		while (r.hasNext()) {
			String key = (String) r.next();
			double d = (((Double) inmap.get(key)).doubleValue());
			//d = d / denominator;
			d = Math.log(d + m) - Math.log(tot + m * p);
			outmap.put(key, new Double(Math.exp(d)));
		}
		return outmap;
	}


	/** BEGIN WEKa CODE >>>>>>>>>>>>>>>> */
	/**
	 * Round a data value using the defined precision for this estimator
	 * 
	 * @param data
	 *            the value to round
	 * @return the rounded data value
	 */
	private double round(double data) {

		return Math.rint(data / m_Precision) * m_Precision;
	}


	/**
	 * Execute a binary search to locate the nearest data value
	 * 
	 * @param the
	 *            data value to locate
	 * @return the index of the nearest data value
	 */
	private int findNearestValue(final int i, final double key) {

		int low = 0;
		int high = m_NumValues[i];
		int middle = 0;
		while (low < high) {
			middle = (low + high) / 2;
			double current = m_seenNumbers[i][middle];
			if (current == key) {
				return middle;
			}
			if (current > key) {
				high = middle;
			} else if (current < key) {
				low = middle + 1;
			}
		}
		return low;
	}

	/**
	 * Add a new data value to the current estimator.
	 * 
	 * @param data
	 *            the new data value
	 * @param weight
	 *            the weight assigned to the data value
	 */
	public void addValue(int i, double data) {

		final double weight = 1;

		data = round(data);
		int insertIndex = findNearestValue(i, data);

		if ((m_NumValues[i] <= insertIndex) || (m_seenNumbers[i][insertIndex] != data)) {
			if (m_NumValues[i] < m_seenNumbers[i].length) {
				int left = m_NumValues[i] - insertIndex;
				System.arraycopy(m_seenNumbers[i], insertIndex, m_seenNumbers[i], insertIndex + 1, left);
				System.arraycopy(m_Weights[i], insertIndex, m_Weights[i], insertIndex + 1, left);

				m_seenNumbers[i][insertIndex] = data;
				m_Weights[i][insertIndex] = weight;
				m_NumValues[i]++;
			} else {
				double[] newValues = new double[m_seenNumbers[i].length * 2];
				double[] newWeights = new double[m_seenNumbers[i].length * 2];
				int left = m_NumValues[i] - insertIndex;
				System.arraycopy(m_seenNumbers[i], 0, newValues, 0, insertIndex);
				System.arraycopy(m_Weights[i], 0, newWeights, 0, insertIndex);
				newValues[insertIndex] = data;
				newWeights[insertIndex] = weight;
				System.arraycopy(m_seenNumbers[i], insertIndex, newValues, insertIndex + 1, left);
				System.arraycopy(m_Weights[i], insertIndex, newWeights, insertIndex + 1, left);
				m_NumValues[i]++;
				m_seenNumbers[i] = newValues;
				m_Weights[i] = newWeights;
			}
			//      if (weight != 1) {
			//	  m_AllWeightsOne = false;
			//}
		} else {
			m_Weights[i][insertIndex] += weight;
			//m_AllWeightsOne = false;
		}
		m_SumOfWeights[i] += weight;
		double range = m_seenNumbers[i][m_NumValues[i] - 1] - m_seenNumbers[i][0];
		if (range > 0) {
			m_StandardDev = Math.max(range / Math.sqrt(m_SumOfWeights[i]),
			// allow at most 3 sds within one interval
					m_Precision / (2 * 3));
		}
	}





	/** END WEKA CODE>>>>>>>>>>>>>> */
	/** helper function to query if we have a model loaded */
	//public final boolean hasModel() {
	//	return hasModel;
	//}///
	/**
	 * method to save model to disk
	 * 
	 * @param model_file
	 *            the name of the name to save to.
	 */

	public void save(final String model_file) throws IOException {
		
			FileOutputStream out = new FileOutputStream(model_file);
			GZIPOutputStream out2 = new GZIPOutputStream(out);
			ObjectOutputStream s = new ObjectOutputStream(out2);

			s.writeObject(getID());
			s.writeObject(getFeature());
			s.writeObject(getInfo());
			//probs is [][]hashmap
		//	s.writeInt(probs.length);
			//s.writeInt(probs[0].length)
			
			s.writeObject(probs);
			//s.writeInt(classnum);
			//s.writeObject(classnames);
			s.writeInt(classCounts.length);
			for (int i = 0; i < classCounts.length; i++)
				s.writeDouble(classCounts[i]);
			s.writeDouble(totalExamplesSeen);
			//s.writeInt(header.length);
			//for (int i = 0; i < header.length; i++)
			//	s.writeInt(header[i]);
			//need to know which name of the ngram/textclass is being saved
			// under
			//write out the doubles for weights and nums
			s.writeInt(m_seenNumbers.length);

			for (int i = 0; i < m_seenNumbers.length; i++) {

				s.writeInt(m_seenNumbers[i].length);
				//now to sort the numbers
				//if(m_seenNumbers[i].length>1)
				//Arrays.sort(m_seenNumbers[i]);
				//done
				for (int j = 0; j < m_seenNumbers[i].length; j++)
					s.writeDouble(m_seenNumbers[i][j]);
			}
			s.writeInt(m_Weights.length);

			for (int i = 0; i < m_Weights.length; i++) {

				s.writeInt(m_Weights[i].length);
				//now to sort the numbers

				//done
				for (int j = 0; j < m_Weights[i].length; j++)
					s.writeDouble(m_Weights[i][j]);
			}
			s.writeInt(m_NumValues.length);

			for (int i = 0; i < m_NumValues.length; i++)
				s.writeInt(m_NumValues[i]);

			s.writeInt(m_SumOfWeights.length);
			for (int i = 0; i < m_SumOfWeights.length; i++)
				s.writeDouble(m_SumOfWeights[i]);


			s.writeDouble(m_StandardDev);
			s.writeDouble(getThreshold());
			s.writeBoolean(isOneClass());
			s.writeInt(featureArray.length);

			for (int i = 0; i < featureArray.length; i++) {
				s.writeBoolean(featureArray[i]);
			}
			s.writeInt(featureTotals.length);
			for (int i = 0; i < featureArray.length; i++) {
				s.writeInt(featureTotals[i]);
			}
			s.writeBoolean(inTraining);
			txtclass.save(model_file + "nbayessubmodel");
			s.writeObject(model_file + "nbayessubmodel");


			s.flush();
			out2.close();
			out.close();
		
		

	}

	/**
	 * specific implimentatin for saving the nbayes model. for text parts,
	 * seperate model is saved there
	 */
	public void load(final String model_file) throws Exception {
		try {
			FileInputStream in = new FileInputStream(model_file);
			GZIPInputStream gin = new GZIPInputStream(in);

			ObjectInputStream s = new ObjectInputStream(gin);

			s.readObject(); //this is the id

			setFeature((String) s.readObject());
			setInfo((String) s.readObject());

			probs = (HashMap[][]) s.readObject();
			//classnum = s.readInt();
			//classnames = (ArrayList) s.readObject();
			int b, a = s.readInt();
			classCounts = new double[a];

			for (int i = 0; i < a; i++)
				classCounts[i] = s.readDouble();
			totalExamplesSeen = s.readDouble();
			//a = s.readInt();
			//header = new int[a];
			//for (int i = 0; i < header.length; i++)
			//	header[i] = s.readInt();
			//read in saved numbers
			a = s.readInt();
			m_seenNumbers = new double[a][];
			for (int i = 0; i < a; i++) {
				b = s.readInt();
				m_seenNumbers[i] = new double[b];
				for (int j = 0; j < b; j++)
					m_seenNumbers[i][j] = s.readDouble();
			}
			//now for weights
			a = s.readInt();
			m_Weights = new double[a][];
			for (int i = 0; i < a; i++) {
				b = s.readInt();
				m_Weights[i] = new double[b];
				for (int j = 0; j < b; j++)
					m_Weights[i][j] = s.readDouble();
			}

			a = s.readInt();
			m_NumValues = new int[a];
			for (int i = 0; i < a; i++)
				m_NumValues[i] = s.readInt();

			a = s.readInt();
			m_SumOfWeights = new double[a];
			for (int i = 0; i < a; i++)
				m_SumOfWeights[i] = s.readDouble();


			m_StandardDev = s.readDouble();
			setThreshold(s.readDouble());
			isOneClass(s.readBoolean());
			int len = s.readInt();
			featureArray = new boolean[len];
			for (int i = 0; i < featureArray.length; i++) {
				featureArray[i] = s.readBoolean();
			}
			len = s.readInt();
			featureTotals = new int[len];
			for (int i = 0; i < featureTotals.length; i++) {
				featureTotals[i] = s.readInt();
			}

			inTraining = s.readBoolean();


			//read in ngram model
			String name = (String) s.readObject();
			//need to see which type of txt class it is.
			try {
				//need to fix
				FileInputStream in2 = new FileInputStream(name);
				ObjectInputStream s2 = new ObjectInputStream(in2);
				String modeltype = new String((String) s2.readObject());
				s2.close();
				if (modeltype.startsWith("NGram"))
					txtclass = new NGram();
				else
					txtclass = new TextClassifier();

				txtclass.setProgress(false);//we dont want to show subclass
				// progresses.
			} catch (Exception e2) {
				txtclass = new NGram();
				txtclass.setProgress(false);//we dont want to show subclass
				// progresses.
			}

			txtclass.load(name);



			in.close();
		} catch (Exception e) {
			System.out.println("Error in nbayes read file:" + e);
		}
		//now that we've loaded, its time to set the flag to true.
		//hasModel = true;
		/*for(int i=0;i<MLearner.NUMBER_CLASSES;i++)
		{	
			if(classCounts[i]<3)
				continue;
			for(int j=0;j<featureArray.length;j++)
			{
				if(featureArray[j])
					System.out.println(i+" "+j+" "+probs[i][j]);
			}
		}*/
		Realprobs = new HashMap[MLearner.NUMBER_CLASSES][featureArray.length];
		setFeatureBoolean(featureArray);
		inTraining = true;//easier than saving the work (although should check size fisrt
	}//end loading process



	/**
	 * Method to train a naive bayes classifier
	 * 
	 * @param header
	 *            lets us know what each column in the data part is...uses
	 *            mlearner class types
	 * @param classes
	 *            different class labels.
	 * @param data
	 *            the data to train on
	 * @param type_txtclass
	 *            choose either a ngram or textclassfier to use on sub parts.
	 *            RETHINK PLEASE
	 */
	/*
	 * public void train(final short[] header, final String[] classes, final
	 * String[][] data, final int type_txtclass, final String TARGET_CLASS)
	 * throws Exception { /* Checking for problems in advance, if we dont have
	 * labels for each of the data columns, its error time / if (header.length !=
	 * data[0].length) throw new Exception("header and data lengths don't match
	 * header - " + header.length + ", data - " + data.length + ")"); /* just to
	 * be safe, this variable is used to tell us which column has data label, ie
	 * class label column / classnum = -1; /* ie if we havent chosen a text
	 * classifier * / if (txtclass == null) { txt_type = type_txtclass; if
	 * (type_txtclass == 0) txtclass = new NGram(); else txtclass = new
	 * TextClassifier();
	 * 
	 * txtclass.setProgress(false);//we dont want to show subclass //
	 * progresses. } /** will hold seen numbers for kenel part * / m_seenNumbers =
	 * new double[header.length][]; m_Weights = new double[header.length][];
	 * m_NumValues = new int[header.length]; m_SumOfWeights = new
	 * double[header.length];
	 * 
	 * 
	 * /** this will hold the results from body + class if needed * / String
	 * subdata[][] = new String[data.length][2]; /* for each column ..some
	 * preprocessing * / for (int i = 0; i < header.length; i++) { //shlomo
	 * removed after running this stuff for a while /* //non-parseable reals: if
	 * (header[i] == MLearner.REAL) for (int j = 0; j <data.length; j++) { try {
	 * //prob take this out. Double.parseDouble(data[j][i]); } catch (Exception
	 * e) { throw new Exception ("error parsing data in nbayes ["+
	 * Integer.toString(j)+"]["+ Integer.toString(i)+"]"+", which
	 * is"+data[j][i]); } }
	 */
	/*
	 * several "CLASS" features in the data: * / // else //removed because above
	 * removed. m_NumValues[i] = 0; m_seenNumbers[i] = new double[0];
	 * m_Weights[i] = new double[0]; //assume only one column has a class label.
	 * if (header[i] == MLearner.CLASS) { //this is the one with the // labels
	 * 
	 * 
	 * if (classnum == -1) classnum = i; else throw new Exception("More than one
	 * class type in header!"); } /* assume only called once...will need to
	 * extend to multiple body parts / else if (header[i] == MLearner.TEXT) {
	 * 
	 * 
	 * //ok we will feed the body part to an underlying text // classifier.
	 * //but we need to collect it and feed to textclassifier: int classloc = 0;
	 * //which column has class info for (int ct = 0; ct < header.length; ct++) {
	 * if (header[ct] == MLearner.CLASS) classloc = ct; } Vector classes1 = new
	 * Vector(); //will store label and size in // recs of each class String
	 * lastclass = new String(data[0][classloc]); //last label
	 * classes1.add(lastclass); int len = 0; /* copy out the body parts * / for
	 * (int g = 0; g < data.length; g++) { subdata[g][0] = data[g][i];
	 * subdata[g][1] = data[g][classloc]; if
	 * (data[g][classloc].equals(lastclass)) { len++; } else { classes1.add("" +
	 * len); lastclass = new String(data[g][classloc]); classes1.add(lastclass);
	 * len = 1; } } classes1.add("" + len); /* now to figure out how many
	 * classes we had * / short hdear[] = new short[classes1.size() / 2 + 1];
	 * //need an // extra one // to store // # of // classes String classlist[] =
	 * new String[classes1.size() / 2]; hdear[0] = (short) (classes1.size() /
	 * 2); len = 0; while (!classes1.isEmpty()) { classlist[len] = (String)
	 * classes1.remove(0); hdear[++len] = (short) Integer.parseInt((String)
	 * classes1.remove(0)); } txtclass.train(hdear, classlist, subdata, 8,
	 * TARGET_CLASS); txtclass.setFeature("submodel only"); //subdata =
	 * txtclass.classify(subdata);//recycle //tada! } /* unknown feature type
	 * (make sure you take care of these, if adding new feature types!) / else
	 * if (header[i] == MLearner.REAL) { m_seenNumbers[i] = new double[10 +
	 * data.length / 4];//start at // some // small // size
	 * 
	 * m_Weights[i] = new double[10 + data.length / 4]; } else if (header[i] !=
	 * MLearner.DISCRETE && header[i] != MLearner.REAL) throw new
	 * Exception("Header contains unknown feature type: " + header[i] + ", check
	 * nbayes code..also the column number is " + i); } //end going through
	 * preprocessing header data
	 *  /* check if we have data labels....else cant train..its supervised
	 * training..remember? / if (classnum == -1) throw new Exception("No class
	 * label in header of nbayes training data!"); /* initializing private
	 * variables to save, this.header is the saved version / this.header = new
	 * int[header.length]; for (int i = 0; i < header.length; i++)
	 * this.header[i] = header[i]; /* names of the classes in the same order as
	 * in probabilities array * / classnames = new ArrayList(); /* _d : total
	 * class FOR DISCRETE FEATURES!
	 */
	/*
	 * _r : total class for REAL FEATURES! * / classnames.add(_DISC);
	 * classnames.add(_REAL); /* yes, I know it's 0 and 1 - but don't want to
	 * hardcode * / int discrete_column = classnames.indexOf(_DISC); int
	 * realnum_column = classnames.indexOf(_REAL); /* add other classnames into
	 * the array * /
	 * 
	 * for (int i = 0; i < classes.length; i++) if
	 * (classnames.contains(classes[i])) throw new Exception("duplicate
	 * classnames"); else classnames.add(classes[i]); /* initialize structures
	 * for probabilities of each class and of each feature / classCounts = new
	 * double[classnames.size()]; probs = new
	 * HashMap[classnames.size()][header.length]; for (int i = 0; i <
	 * classnames.size(); i++) { for (int j = 0; j < header.length; j++) {
	 * probs[i][j] = new HashMap(); } classCounts[i] = 0; } /* progress window * /
	 * BusyWindow bw = null; if (showProgress()) { bw = new BusyWindow("Training
	 * NBayes", "Progress Report"); bw.show(); }
	 * 
	 * int maxl = data.length; String cname; int cnum; double m, d, v, n;
	 * System.out.println("maxl: " + maxl + "classnum: " + classnum + "header
	 * length: " + header.length); /* summing up data, also for total classes * / /*
	 * for each data item * / for (int i = 0; i < maxl; i++) {
	 * //System.out.print(" "+i); if (showProgress()) bw.progress(i, maxl);
	 * //will show progress
	 * 
	 * cname = data[i][classnum]; //REMOVED FOR SPEEDUP.....not sure if smart??
	 * if (!classnames.contains(cname)) throw new Exception("unknown class name: " +
	 * cname);
	 *  /* cnum is class number, i.e. first index in probs array * / cnum =
	 * classnames.indexOf(cname); /* j is feature name, i.e. second index in
	 * probs * / /* j is also second index in data; if j == classnum, it is of
	 * CLASS type / /* first index in data is always i (record number) * / for
	 * (int j = 0; j < header.length; j++) { //System.out.print(" " + j); if
	 * (header[j] == MLearner.DISCRETE)// || header[j] == MLearner) { /*
	 * increment or increase frequency in the map for feature j * / d = 0; if
	 * (probs[cnum][j].containsKey(data[i][j])) //probs[cnum][j].get(data[i][j]) !=
	 * null) { d = ((Double) probs[cnum][j].get(data[i][j])).doubleValue(); }
	 * d++; probs[cnum][j].put(data[i][j], new Double(d)); /* this is for total: * /
	 * d = 0; if (probs[discrete_column][j].containsKey(data[i][j]))// != //
	 * null) d = ((Double)
	 * probs[discrete_column][j].get(data[i][j])).doubleValue(); d++;
	 * probs[discrete_column][j].put(data[i][j], new Double(d)); } else if
	 * (header[j] == MLearner.REAL) { //System.out.print("r"); /* update mean
	 * and variance for this class and for total * / if (probs[cnum][j].size() ==
	 * 0) { d = Double.parseDouble(data[i][j]);
	 * 
	 * probs[cnum][j].put(_MEAN, new Double(d)); probs[cnum][j].put(_VAR, new
	 * Double(d * d)); probs[cnum][j].put(_TOTAL, new Double(1)); } else { d =
	 * Double.parseDouble(data[i][j]); m = ((Double)
	 * probs[cnum][j].get(_MEAN)).doubleValue(); v = ((Double)
	 * probs[cnum][j].get(_VAR)).doubleValue(); n = ((Double)
	 * probs[cnum][j].get(_TOTAL)).doubleValue(); n++; v = ((n - 1) / n) * v +
	 * (1.0 / n) * d * d; //not a // bessel's // correction m = ((n - 1) / n) *
	 * m + (1.0 / n) * d; probs[cnum][j].put(_MEAN, new Double(m));
	 * probs[cnum][j].put(_VAR, new Double(v)); probs[cnum][j].put(_TOTAL, new
	 * Double(n)); } if (probs[realnum_column][j].size() == 0) { d =
	 * Double.parseDouble(data[i][j]); probs[realnum_column][j].put(_MEAN, new
	 * Double(d)); probs[realnum_column][j].put(_VAR, new Double(d * d));
	 * probs[realnum_column][j].put(_TOTAL, new Double(1)); } else { d =
	 * Double.parseDouble(data[i][j]); m = ((Double)
	 * probs[realnum_column][j].get(_MEAN)).doubleValue(); v = ((Double)
	 * probs[realnum_column][j].get(_VAR)).doubleValue(); n = ((Double)
	 * probs[realnum_column][j].get(_TOTAL)).doubleValue(); n++; v = ((n - 1) /
	 * n) * v + (1.0 / n) * d * d; m = ((n - 1) / n) * m + (1.0 / n) * d;
	 * probs[realnum_column][j].put(_MEAN, new Double(m));
	 * probs[realnum_column][j].put(_VAR, new Double(v));
	 * probs[realnum_column][j].put(_TOTAL, new Double(n)); }
	 * //m_seenNumbers[j][i] = d;//save the number just seen;
	 * 
	 * addValue(j, d); } } // loop over features } //loop over data i variable /*
	 * done building the model probabilites in the probs array. * / /* we deal
	 * with probs[i][j] in these loops * / //System.out.println("here"); /*
	 * number of records in the sample * /
	 * 
	 * classCounts[discrete_column] = sumFreq(probs[discrete_column][0]); double
	 * total = classCounts[discrete_column]; //System.out.println("Total seems
	 * to be " + total + " can we replace // with ? " + data.length); // total =
	 * data.length; /* loop through the features to convert raw data into
	 * probabilities or whatever / //System.out.println("classnames:\n" +
	 * classnames); for (int i = 0; i < classnames.size(); i++) { /* same stuff
	 * for discrete and real - probability of class * / classCounts[i] =
	 * sumFreq(probs[i][0]);
	 * 
	 * 
	 * //?????????????? if (classCounts[i] != 0) { for (int j = 0; j <
	 * header.length; j++) if (header[j] == MLearner.DISCRETE && i !=
	 * realnum_column) { //???????? double possibles =
	 * (probs[discrete_column][j].size()); //System.out.println("j is " + j + "
	 * and pos: " + // possibles + "the hash: " + probs[0][0].size() );
	 * //System.out.println("clasprob: " + classCounts[i]); //???? //????
	 * possibles*=SSIZE;
	 * 
	 * probs[i][j].put("_default", new Double(0)); //?????? //probs[i][j]=
	 * divideEach (probs[i][j], // ((double)classCounts[i]+SSIZE) );
	 * 
	 * //estimate using the m estimate // #token j + mp // ------------------- // /
	 * #total token + m //actually turned into laplace because m = alpha = .01 //
	 * and p = 1/k = 1/p
	 * 
	 * //where: m = constant weight called the quivalent // sample size //p =
	 * assume unifrom disribution how heavy to count // sample size, 1/total
	 * here. //divideeach ( hash , $total , m ,p) //System.out.println("total is " +
	 * total); probs[i][j] = divideEach(probs[i][j], classCounts[i], .01,
	 * possibles); } else if (header[j] == MLearner.REAL && i !=
	 * discrete_column) { v = ((Double) probs[i][j].get(_VAR)).doubleValue(); n =
	 * ((Double) probs[i][j].get(_TOTAL)).doubleValue(); m = ((Double)
	 * probs[i][j].get(_MEAN)).doubleValue(); v = v - m * m; v *= (n / (n - 1)); /*
	 * Bessel's correction * / if (v < 0) throw new Exception("something wrong:
	 * variance negative in nbayes");
	 * 
	 * v = Math.sqrt(v); //get the std deviation //System.out.println("v is " +
	 * v); probs[i][j].put(_VAR, new Double(v)); } else if (header[j] ==
	 * MLearner.TEXT) //shlomo poutting in // the scroe for text // parts. {
	 * //nothing todo since we've done it already.
	 * 
	 * //System.out.println("seeing text feature and i is " + // i ); }
	 * 
	 * classCounts[i] /= total; }//end if else { classCounts[i] = ZERO; }
	 * System.out.println(i + " class prob: " + classCounts[i]); }
	 *  /* if (true) { System.out.println("CLASSES");
	 * System.out.println(classnames);
	 * 
	 * System.out.println("DATA"); for (int i = 0; i <probs.length; i++) {
	 * System.out.println("SIZE: "+classCounts[i]); for (int j = 0; j
	 * <probs[i].length; j++) { System.out.println(i+","+j+" -- "+probs[i][j]); } } } /
	 * //scucessfully computed model. hasModel = true; if (showProgress()) {
	 * bw.hide(); bw = null; }
	 *  }
	 *  
	 */
	/**
	 * Method to classify data based on naive bayes assumption of independant
	 * prob
	 * 
	 * @param records
	 *            is the data to classify, only requirement is that the header
	 *            order be the same as in training.
	 * 
	 * @return double array of classifications.
	 */
	/*
	 * public final String[][] classify(final String records[][]) throws
	 * Exception {
	 * 
	 * if (!hasModel) { System.out.println("No model has been computer for naive
	 * bayes.!!"); throw new Exception("The classifier doesn't have a model,
	 * please train NB first."); } if (records[0].length != header.length) {
	 * 
	 * //its ok to be off by one needed for distinct emailref if
	 * (records[0].length == header.length + 1) { } else throw new
	 * Exception("classify: wrong number of features in data"); }
	 * 
	 * String subdata[][] = new String[records.length][1]; //will hold results //
	 * of body // classification String[][] result = new
	 * String[records.length][2]; //hold the resulting // classifications. //
	 * class label // followed by // probability.
	 * 
	 * int discrete_column = classnames.indexOf(_DISC); int realnum_column =
	 * classnames.indexOf(_REAL); //RULEZ!! int[] possibles = new
	 * int[header.length];
	 * 
	 * for (int i = 0; i < header.length; i++) { if (header[i] == MLearner.REAL)
	 * possibles[i] = 0; else if (header[i] == MLearner.DISCRETE) possibles[i] =
	 * probs[discrete_column][i].size(); else if (header[i] == MLearner.TEXT) {
	 * possibles[i] = 0; //now to replace all stuff in here with ngram values
	 * 
	 * for (int g = 0; g < records.length; g++) subdata[g][0] = records[g][i];
	 * //run the sub classifier. subdata = txtclass.classify(subdata); } }
	 * 
	 * BusyWindow msg = null; if (showProgress()) { msg = new
	 * BusyWindow("Classify NBayes", "Progress Report"); msg.show(); } boolean
	 * singleClass = false; if (classnames.size() == 3) {
	 * System.out.println("only one class in model"); singleClass = true; } //
	 * double tempdub=0; int j, k;
	 * 
	 * 
	 * int best_class = -1; double certainty = 0; double certainty2 = 0; /* to
	 * smooth out the result to make it between 0-1 * / double denominator = 0;
	 * /** will hold the probabilty
	 * /
	double prob = 0;//new double[records.length];
	String value = new String();
	double other = 0;//for single class
	/*
	 * for each record * / for (int i = 0; i < records.length; i++) { if
	 * (showProgress()) { msg.progress(i, records.length); }
	 * 
	 * best_class = -1; certainty = 0; certainty2 = 0; denominator = 0; /*
	 * traverse all classes except summary classes * / /* do we really need
	 * them? * / for (j = 0; /* certainty!=-1 && * /j < classnames.size(); j++) {
	 * //System.out.println("j is " + j + " and prob is " + prob); if (j !=
	 * discrete_column && j != realnum_column && classCounts[j] != 0) { other =
	 * 0;//reset other class prob prob = 0;//reset the probability //prob = 1;
	 * for (k = 0; k < header.length; k++) { // System.out.print("p" +prob +
	 * "p.."); if (header[k] == MLearner.DISCRETE) {
	 * 
	 * value = records[i][k]; /* value of given feature in data / /* this value
	 * of feature is not seen in this class * / if
	 * (!probs[j][k].containsKey(value))//==null) { /* if
	 * (probs[discrete_column][k].get(value)==null ||
	 * probs[j][k].get("_default")==null) { prob=0; break; }
	 * 
	 * else / prob += Math.log(((Double)
	 * probs[j][k].get("_default")).doubleValue()); other += Math.log(((Double)
	 * probs[j][k].get("_default")).doubleValue()); //value="_default";
	 * //System.out.println((Double)probs[j][k].get(value) // + " is default"); } /*
	 * probability P(Xi = xi | C = given_class) * / //double d = //
	 * ((Double)probs[j][k].get(value)).doubleValue(); else { prob +=
	 * Math.log(((Double) probs[j][k].get(value)).doubleValue()); other +=
	 * Math.log(((Double) probs[j][k].get("_default")).doubleValue()); } //prob +=
	 * Math.log(d); } else if (header[k] == MLearner.REAL) { double x = 0, v =
	 * 0, n = 0, m = 0, zero = 0; try { x = Double.parseDouble(records[i][k]); v =
	 * ((Double) probs[j][k].get(_VAR)).doubleValue(); n = ((Double)
	 * probs[j][k].get(_TOTAL)).doubleValue(); m = ((Double)
	 * probs[j][k].get(_MEAN)).doubleValue(); zero = (SSIZE / (SSIZE + n)); if
	 * (v == 0) { //System.out.println("v is zero"); v = zero; } /** create a
	 * normal distribution using mean and std dviation
	 */
	/*
	 * / /* compute the prob that x is between x1 and x2 * / double d = 0; /*
	 * double x1 = x-v;//ERROR*v;//Utils.round(x-(v/2.0),3); double x2 =
	 * x+v;//ERROR*v;//Utils.round(x+(v/2.0),3); NormalDistribution nd = new
	 * NormalDistribution(m,v); d = nd.probability(x1, x2);//was v * Error
	 * //NEED TO CONVERT TO LOG IF NECCESSARY!!!!
	 * 
	 * System.out.println("x1 is " + x1 + " x2 is " + x2); System.out.println("m
	 * "+m+" v: " + v +" 0: " + zero + " d is: " + d); System.out.println("x:" +
	 * x +" pdf:" + nd.pdf(x) +"\nand cdf:" + nd.cdf(x)); System.out.println("my
	 * own pdf: " + Utils.pdf(m,v,x)); System.out.println("with log:" +
	 * Utils.pdf(Math.log(m),Math.log(v),Math.log(x)));
	 * System.out.println("multiple gaussian: " +
	 * Utils.kernelDensityEstimator(x,seenNumbers[k].length,seenNumbers[k],m)); /
	 * //if(seenNumbers[k].length) //System.out.print(" check"+k); d =
	 * Utils.kernelDensityEstimator(x, m_NumValues[k], m_seenNumbers[k],
	 * m_Weights[k], m_StandardDev, findNearestValue(k, x), m_SumOfWeights[k]);
	 * //else //d=-1; // System.out.println("d is " + d);
	 * 
	 * //if (d <zero) // { //d=zero; // } //else//added by shlomo, since small
	 * will not // include in calculation //System.out.println(d); // { prob +=
	 * Math.log(d); other += Math.log(d); //}//??} /*
	 * catch(drasys.or.prob.ProbError p) { System.out.println("k is " + k + "
	 * malformatted double inside drasys, x=" +x+" v=" + v + " m = " + m + "
	 * zero=" + zero ); //prob=0; } / } catch (Exception e) { /* oops! But I am
	 * nice, I'll ignore this record
	 */
	/*
	 * instead of simply dying :-) * / /* should not be triggered * /
	 * System.out.println("malformatted double" + e); prob = 0; // break; } }
	 * else if (header[k] == MLearner.TEXT) {
	 * 
	 * //subdata has text results. //i is the data line //j is class 2/3 n/s
	 * //System.out.print(k + "," + j + ":"); try { //oneclass if
	 * (isOneClass())//need to undo log prob +=
	 * Math.exp(Double.parseDouble(subdata[i][1])); else prob +=
	 * Math.log(Double.parseDouble(subdata[i][1])); /* if(j==2)
	 * if(subdata[i][0].equals("n")) {
	 * tempdub=Double.parseDouble(subdata[i][1]); prob*=(.99);//+tempdub); }
	 * else {}//prob/=2;}//prob-=Double.parseDouble(subdata[i][1]);//prob/=2;
	 * else if(j==3) if(subdata[i][0].equals("s")) {
	 * 
	 * //prob*=Double.parseDouble(subdata[i][1]); } else {prob/=10;}//prob/=4; / }
	 * catch (Exception rt) { System.out.println(rt); } } } if (!singleClass)
	 * prob += Math.log(classCounts[j]); // System.out.println("prob is " +
	 * Math.exp(prob)); if (Math.exp(prob) > certainty || certainty == 0) { //
	 * System.out.println("setting best class" + // Math.exp(prob)); certainty =
	 * Math.exp(prob); best_class = j; certainty2 = Math.exp(other); } /* this
	 * is to normalize, if needed * / if (!singleClass) denominator +=
	 * Math.exp(prob); }//traversal of classes }
	 * 
	 * if (best_class < 0)//should never be needed best_class = 2;
	 * 
	 * 
	 * result[i][0] = (String) classnames.get(best_class); //else {
	 * //result[i][0]="?"; // } //LOOK INTO NORMALIZING THE SCORES WHEN MORE
	 * THAN ONE CLASS!! // document length problem /* normalized probability * /
	 * if (singleClass) { result[i][1] = Double.toString(Math.log(certainty));
	 * //Math.exp( // Math.log(certainty)-Math.log(certainty+certainty2)));
	 * //System.out.println("other: " + certainty2 + " " + "self: " + //
	 * certainty); } else { result[i][1] = Double.toString(certainty /
	 * denominator); //System.out.println("other: " + denominator + " " + "self: " + //
	 * certainty); } }//data traversal from i..n
	 * 
	 * 
	 * if (showProgress()) { msg.hide(); msg = null; }
	 * 
	 * 
	 * return result; }
	 * /
	/ **
	 * outputs the model in a "human-readable" format
	 *  
	 * /
	/*
	 * private void formatModel(final PrintWriter f) throws Exception { /* if no
	 * model - nothing to talk about * / if (!hasModel) { f.println("No model
	 * available"); return; } /* enumerate classes * / for (int i = 0; i <
	 * classnames.size(); i++) { /* if those are utility classes - totals etc. * /
	 * if (util(i)) continue;
	 * 
	 * f.println("CLASS: " + classnames.get(i)); f.println("Probability of this
	 * class: " + classCounts[i]); /* enumerage features * / for (int j = 0; j <
	 * header.length; j++) { f.println(""); f.println("FEATURE:" + j + ":");
	 * f.println("Type:" + stringType(header[j]));
	 * 
	 * if (header[j] == DISCRETE)// || header[j]==TEXT) { f.println("Values and
	 * probabilities: "); f.println("-------------------------"); Set ks =
	 * probs[i][j].keySet(); Iterator itr = ks.iterator(); while (itr.hasNext()) {
	 * String ky = (String) itr.next(); Double nm = (Double)
	 * probs[i][j].get(ky); if (!ky.equals("_default")) f.println(ky + " : " +
	 * nm); } } if (header[j] == MLearner.REAL) { if (probs[i][j].size() == 0)
	 * f.println("No data for this class "); else { f.println("Normal
	 * Distribution: "); String ky = _MEAN; Double nm = (Double)
	 * probs[i][j].get(ky); if (nm != null) { double d = nm.doubleValue(); d =
	 * (Math.round(d * 1000)) / 1000; f.println("Average: " + d); } else
	 * f.println("Average: N/A"); ky = _VAR; nm = (Double) probs[i][j].get(ky);
	 * if (nm != null) { double d = nm.doubleValue(); d = (Math.round(d * 1000)) /
	 * 1000; f.println("Variance: " + d); } else f.println("Variance: N/A"); } }
	 * //real feature
	 * 
	 * }//over features
	 * 
	 * f.println(""); f.println(""); } //over classes }
	 *  
	 * /

	/ **
	 * @return human readable string format of model
	 *  
	 * /
	/*
	 * private final String formatModel() { /* if no model - nothing to talk
	 * about * / if (!hasModel) {
	 * 
	 * return ""; } String report = new String(); /* enumerate classes * / for
	 * (int i = 0; i < classnames.size(); i++) { /* if those are utility classes -
	 * totals etc. * / if (util(i)) continue;
	 * 
	 * report += "CLASS: " + classnames.get(i) + "\n"; report += "Probability of
	 * this class: " + classCounts[i] + "\n"; /* enumerage features * / for (int
	 * j = 0; j < header.length; j++) { report += "\nFEATURE:" + j + ":"; report +=
	 * "Type:" + stringType(header[j]) + "\n";
	 * 
	 * if (header[j] == MLearner.DISCRETE)// || header[j]==TEXT) { report +=
	 * "Values and probabilities: \n"; report += "-------------------------\n";
	 * Set ks = probs[i][j].keySet(); Iterator itr = ks.iterator(); while
	 * (itr.hasNext()) { String ky = (String) itr.next(); Double nm = (Double)
	 * probs[i][j].get(ky); if (!ky.equals("_default")) report += ky + " : " +
	 * nm + "\n"; } } if (header[j] == MLearner.REAL) { if (probs[i][j].size() ==
	 * 0) report += "No data for this class \n"; else { report += "Normal
	 * Distribution: \n"; String ky = _MEAN; Double nm = (Double)
	 * probs[i][j].get(ky); if (nm != null) { double d = nm.doubleValue(); d =
	 * (Math.round(d * 1000)) / 1000; report += "Average: " + d + "\n"; } else
	 * report += "Average: N/A\n"; ky = _VAR; nm = (Double) probs[i][j].get(ky);
	 * if (nm != null) { double d = nm.doubleValue(); d = (Math.round(d * 1000)) /
	 * 1000; report += "Variance: " + d + "\n"; } else report += "Variance:
	 * N/A\n"; } } //real feature
	 * 
	 * }//over features
	 * 
	 * report += "\n"; } //over classes
	 * 
	 * return report; }
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doTraining(java.lang.String[],
	 *      java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		/*
		 * Checking for problems in advance, if we dont have labels for each of
		 * the data columns, its error time
		 */
		totalExamplesSeen++;

		/* for each column ..some preprocessing */
		/*
		 * for (int i = 0; i < featureArray.length; i++) { m_NumValues[i] = 0;
		 * m_seenNumbers[i] = new double[0]; m_Weights[i] = new double[0]; /*
		 * assume only called once...will need to extend to multiple body parts
		 */
		/*
		 * unknown feature type (make sure you take care of these, if adding new
		 * feature types!)
		 */
		/*
		 * else if (header[i] == MLearner.REAL) { m_seenNumbers[i] = new
		 * double[10 + data.length / 4];//start at // some // small // size
		 * 
		 * m_Weights[i] = new double[10 + data.length / 4]; } else if (header[i] !=
		 * MLearner.DISCRETE && header[i] != MLearner.REAL) throw new
		 * Exception("Header contains unknown feature type: " + header[i] + ",
		 * check nbayes code..also the column number is " + i); } //end going
		 * through preprocessing header data
		 */


		/* names of the classes in the same order as in probabilities array */
		//	classnames = new ArrayList();
		/* _d : total class FOR DISCRETE FEATURES! */
		/* _r : total class for REAL FEATURES! */
		//classnames.add(_DISC);
		//classnames.add(_REAL);
		/* yes, I know it's 0 and 1 - but don't want to hardcode */
		//int discrete_column = classnames.indexOf(_DISC);
		//int realnum_column = classnames.indexOf(_REAL);
		/* add other classnames into the array */

		//for (int i = 0; i < classes.length; i++)
		//	if (classnames.contains(classes[i]))
		//		throw new Exception("duplicate classnames");
		//	else
		//		classnames.add(classes[i]);


		int classNumber = MLearner.getClassNumber(Label);
	//incrmeent the specific class's count ie number of spam seen
		classCounts[classNumber]++;
		double m, d, v, n;
		/* summing up data, also for total classes */
		/* for each data item */
		//for (int i = 0; i < maxl; i++)
		{

				/* cnum is class number, i.e. first index in probs array */
			//cnum = classnames.indexOf(cname);
			/* j is feature name, i.e. second index in probs */
			/*
			 * j is also second index in data; if j == classnum, it is of CLASS
			 * type
			 */
			/* first index in data is always i (record number) */
			String featureValue = null;

			for (int j = 0; j < featureArray.length; j++) {

				if (!featureArray[j]) {//ie not using this feature.
					continue;
				}

				if (j == EmailInternalConfigurationWindow.MESSAGEBODY) {
					//					check if we have to do body
					txtclass.doTraining(msgData, Label);

				} else if (EmailInternalConfigurationWindow.isFeatureDiscrete(j))//featureArray[j]
				{

					switch (j) {
						case (EmailInternalConfigurationWindow.SENDER) :
							featureValue = msgData.getFromEmail();
							break;
						case (EmailInternalConfigurationWindow.SNAME) :
							featureValue = msgData.getFromName();
							break;
						case (EmailInternalConfigurationWindow.SDOM) :
							featureValue = msgData.getFromDomain();
							break;
						case (EmailInternalConfigurationWindow.SZN) :
							featureValue = msgData.getFromZN();
							break;
						case (EmailInternalConfigurationWindow.RCPT) :
							featureValue = msgData.getRCPT();
							break;
						case (EmailInternalConfigurationWindow.RNAME) :
							featureValue = msgData.getRecName();
							break;
						case (EmailInternalConfigurationWindow.RDOM) :
							featureValue = msgData.getRecDomain();
							break;
						case (EmailInternalConfigurationWindow.RZN) :
							featureValue = msgData.getRecZN();
							break;
						case (EmailInternalConfigurationWindow.FOLDER) :
							featureValue = msgData.getFolder();
							break;
						case (EmailInternalConfigurationWindow.DATES) :
							featureValue = msgData.getDate();
							break;
						case (EmailInternalConfigurationWindow.FLAGS) :
							featureValue = msgData.getFlags();
							break;
						case (EmailInternalConfigurationWindow.FORWARD) :
							featureValue = msgData.getForward();
							break;
						case (EmailInternalConfigurationWindow.RCPTLOC) :
							featureValue = new String("" + msgData.getRCPTLoc());
							break;
						case (EmailInternalConfigurationWindow.SENDERLOC) :
							featureValue = new String("" + msgData.getSenderLoc());
							break;
						case (EmailInternalConfigurationWindow.RPLYTO) :
							featureValue = msgData.getRPLYTO();
							break;
						default :
							featureValue = "";
					}
					/* increment or increase frequency in the map for feature j */
					d = 0;

					if (probs[classNumber][j].containsKey(featureValue))
					//probs[cnum][j].get(data[i][j]) != null)
					{
						d = ((Double) probs[classNumber][j].get(featureValue)).doubleValue();
					}
					d++;
					probs[classNumber][j].put(featureValue, new Double(d));
					featureTotals[j]++;
					/* this is for total: */
					//??d = 0;
					//?if (probs[discrete_column][j].containsKey(data[i][j]))//
					// !=
					// null)
					//? d = ((Double)
					// probs[discrete_column][j].get(data[i][j])).doubleValue();
					//??d++;
					//??probs[discrete_column][j].put(data[i][j], new
					// Double(d));
				} else {
					//System.out.print("r");
					/* update mean and variance for this class and for total */
					switch (j) {
						case (EmailInternalConfigurationWindow.SIZE) :
							d = msgData.getSize();
							break;
						case (EmailInternalConfigurationWindow.HOURTIME) :
							d = msgData.getGHour();
							break;
						case (EmailInternalConfigurationWindow.MONTHTIME) :
							d = msgData.getGMonth();
							break;
						case (EmailInternalConfigurationWindow.NUMATTACH) :
							d = msgData.getNumberAttach();
							break;
						case (EmailInternalConfigurationWindow.NUMRCPT) :
							d = msgData.getRcptsNumber();
							break;
						case (EmailInternalConfigurationWindow.TIMEADJ) :
							d = msgData.getTimeADJ();
							break;
						default :
							d = 0;
					}

					if (probs[classNumber][j].size() == 0) {

						probs[classNumber][j].put(_MEAN, new Double(d));
						probs[classNumber][j].put(_VAR, new Double(d * d));
						probs[classNumber][j].put(_TOTAL, new Double(1));
					} else {

						m = ((Double) probs[classNumber][j].get(_MEAN)).doubleValue();
						v = ((Double) probs[classNumber][j].get(_VAR)).doubleValue();
						n = ((Double) probs[classNumber][j].get(_TOTAL)).doubleValue();
						n++;
						v = ((n - 1) / n) * v + (1.0 / n) * d * d; //not a
						// bessel's
						// correction
						m = ((n - 1) / n) * m + (1.0 / n) * d;
						probs[classNumber][j].put(_MEAN, new Double(m));
						probs[classNumber][j].put(_VAR, new Double(v));
						probs[classNumber][j].put(_TOTAL, new Double(n));
					}/*
					  * ??????? if (probs[realnum_column][j].size() == 0) { d =
					  * Double.parseDouble(data[i][j]);
					  * probs[realnum_column][j].put(_MEAN, new Double(d));
					  * probs[realnum_column][j].put(_VAR, new Double(d * d));
					  * probs[realnum_column][j].put(_TOTAL, new Double(1)); }
					  * else { d = Double.parseDouble(data[i][j]); m = ((Double)
					  * probs[realnum_column][j].get(_MEAN)).doubleValue(); v =
					  * ((Double)
					  * probs[realnum_column][j].get(_VAR)).doubleValue(); n =
					  * ((Double)
					  * probs[realnum_column][j].get(_TOTAL)).doubleValue();
					  * n++; v = ((n - 1) / n) * v + (1.0 / n) * d * d; m = ((n -
					  * 1) / n) * m + (1.0 / n) * d;
					  * probs[realnum_column][j].put(_MEAN, new Double(m));
					  * probs[realnum_column][j].put(_VAR, new Double(v));
					  * probs[realnum_column][j].put(_TOTAL, new Double(n)); }
					  */
					//m_seenNumbers[j][i] = d;//save the number just seen;

					addValue(j, d);

				}

			} // loop over features
		} //loop over data i variable


		inTraining = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException 
{
	double d=0;
	int best_class = -1;
	double certainty = 0;
	/* to smooth out the result to make it between 0-1 */
	double denominator = 0;
	/** will hold the probabilty */
	double probCollection[] = new double[classCounts.length];//new
	String featureValue = new String();
	//double other = 0;//for single class
	boolean isFirst = true;

	if(inTraining)
		{
			for (int i = 0; i < classCounts.length; i++) 
			{
				if(classCounts[i]<3)
				{
					continue;
				}

				for (int j = 0; j < featureArray.length; j++)
					{
						
						if(!featureArray[j])
							continue;
					
						if(EmailInternalConfigurationWindow.MESSAGEBODY ==j){
						
						}
						else if (EmailInternalConfigurationWindow.isFeatureDiscrete(j)) {
							
							//double possibles =
							// (probs[discrete_column][j].size());
		
							
							//estimate using the m estimate
							//    #token j + mp
							//   -------------------
							//    / #total token + m
							//actually turned into laplace because m = alpha =
							// .01
							// and p = 1/k = 1/p

							//where: m = constant weight called the quivalent
							// sample size
							//p = assume unifrom disribution how heavy to count
							// sample size, 1/total here.
							//divideeach ( hash , $total , m ,p)
							//System.out.println("total is " + total);
							Realprobs[i][j] = divideEach(probs[i][j], classCounts[i]/totalExamplesSeen, .01, probs[i][j].size());//featureTotals[j]);

						} else  {
							Realprobs[i][j] = new HashMap();
							double v = ((Double) probs[i][j].get(_VAR)).doubleValue();
							double n = ((Double) probs[i][j].get(_TOTAL)).doubleValue();
							double m = ((Double) probs[i][j].get(_MEAN)).doubleValue();
							v = v - m * m;
							v *= (n / (n - 1)); /* Bessel's correction */
							if (v < 0)
								throw new machineLearningException("something wrong: variance negative in nbayes");

							v = Math.sqrt(v); //get the std deviation
							//System.out.println("v is " + v);
							Realprobs[i][j].put(_VAR, new Double(v));
							Realprobs[i][j].put(_TOTAL, new Double(n));
							Realprobs[i][j].put(_MEAN, new Double(m));
						} 
				}//end if
			}//end for each class

			inTraining = false;
		}//endin training
	
	//here is were regular evaluation gets to work.
	
	//int discrete_column = classnames.indexOf(_DISC);
	//int realnum_column = classnames.indexOf(_REAL);
		
		for(int i=0;i<probCollection.length;i++){
			probCollection[i] = 0;
		}
		
		best_class = -1;
		certainty = 0;
		denominator = 0;

			/* traverse all classes */
			
			for (int i = 0; i < Realprobs.length; i++) 
			{
				if(classCounts[i] < 3){
					continue;
				}
				
				//if (j != discrete_column && j != realnum_column &&
				// classCounts[j] != 0) {
					//other = 0;//reset other class prob
					//prob = 0;//reset the probability
					//prob = 1;
					for (int k = 0; k < featureArray.length; k++) 
					{
						
						if(!featureArray[k])
						{
							continue;
						}
						
						if(k == EmailInternalConfigurationWindow.MESSAGEBODY)
						{
							if(featureArray[k]){
								//oneclass
								try{
								resultScores rs = txtclass.doLabeling(msgData);
								probCollection[i] += Math.log((double)rs.getScore()/100.);
								
								}catch(machineLearningException ml){
								System.out.println("ml"+ml);
								}
								//PROCESS BODY HERE TODO:
							
							}
						
						}
						else if (EmailInternalConfigurationWindow.isFeatureDiscrete(k)) 
						{

							switch(k){
								case(EmailInternalConfigurationWindow.SENDER):
										featureValue = msgData.getFromEmail();
										break;
								case(EmailInternalConfigurationWindow.SNAME):
									featureValue = msgData.getFromName();
									break;
								case(EmailInternalConfigurationWindow.SDOM):
									featureValue = msgData.getFromDomain();
									break;
								case(EmailInternalConfigurationWindow.SZN):
									featureValue = msgData.getFromZN();
									break;
								case(EmailInternalConfigurationWindow.RCPT):
									featureValue = msgData.getRCPT();
									break;
								case(EmailInternalConfigurationWindow.RNAME):
									featureValue = msgData.getRecName();
									break;
								case(EmailInternalConfigurationWindow.RDOM):
									featureValue = msgData.getRecDomain();
									break;
								case(EmailInternalConfigurationWindow.RZN):
									featureValue = msgData.getRecZN();
									break;
								case(EmailInternalConfigurationWindow.FOLDER):
									featureValue = msgData.getFolder();
									break;	
								case(EmailInternalConfigurationWindow.DATES):
									featureValue = msgData.getDate();
									break;	
								case(EmailInternalConfigurationWindow.FLAGS):
									featureValue = msgData.getFlags();
									break;	
								case(EmailInternalConfigurationWindow.FORWARD):
									featureValue = msgData.getForward();
									break;	
								case(EmailInternalConfigurationWindow.RCPTLOC):
									featureValue = new String(""+msgData.getRCPTLoc());
									break;	
								case(EmailInternalConfigurationWindow.SENDERLOC):
									featureValue = new String(""+msgData.getSenderLoc());
									break;
								case(EmailInternalConfigurationWindow.RPLYTO):
									featureValue = msgData.getRPLYTO();
									break;
									default:
										featureValue = "";
							}
							
							/* this value of feature is not seen in this class */
							if (!Realprobs[i][k].containsKey(featureValue))//==null)
							{
								probCollection[i] += Math.log(((Double) Realprobs[i][k].get("_default")).doubleValue());
								//other += Math.log(((Double)
														/*
														 * probability P(Xi = xi |
														 * C = given_class)
														 */
							//double d =
							// ((Double)probs[j][k].get(value)).doubleValue();
							}else {
								probCollection[i] += Math.log(((Double) Realprobs[i][k].get(featureValue)).doubleValue());
								//other += Math.log(((Double)
								// probs[i][k].get("_default")).doubleValue());
							}
							//prob += Math.log(d);
						} else  {
							
							switch(k){
								case(EmailInternalConfigurationWindow.SIZE):
									d = msgData.getSize();
									break;	
								case(EmailInternalConfigurationWindow.HOURTIME):
									d = msgData.getGHour();
									break;	
								case(EmailInternalConfigurationWindow.MONTHTIME):
									d = msgData.getGMonth();
									break;	
								case(EmailInternalConfigurationWindow.NUMATTACH):
									d = msgData.getNumberAttach();
									break;	
								case(EmailInternalConfigurationWindow.NUMRCPT):
									d = msgData.getRcptsNumber();
									break;	
								case(EmailInternalConfigurationWindow.TIMEADJ):
									d = msgData.getTimeADJ();
									break;	
								default: 
									d =0;
							}
							
							double v = 0, n = 0, m = 0, zero = 0;
							try 
							{
								
								v = ((Double) Realprobs[i][k].get(_VAR)).doubleValue();
								n = ((Double) Realprobs[i][k].get(_TOTAL)).doubleValue();
								m = ((Double) Realprobs[i][k].get(_MEAN)).doubleValue();
								zero = (SSIZE / (SSIZE + n));
								if (v == 0) 
								{
									//System.out.println("v is zero");
									v = zero;
								}
								/**
								 * create a normal distribution using mean and
								 * std dviation
								 */
								/*
								 */
								/* compute the prob that x is between x1 and x2 */
								double d1 = 0;
								/*
								 * double x1 =
								 * x-v;//ERROR*v;//Utils.round(x-(v/2.0),3);
								 * double x2 =
								 * x+v;//ERROR*v;//Utils.round(x+(v/2.0),3);
								 * NormalDistribution nd = new
								 * NormalDistribution(m,v); d =
								 * nd.probability(x1, x2);//was v * Error //NEED
								 * TO CONVERT TO LOG IF NECCESSARY!!!!
								 * 
								 * System.out.println("x1 is " + x1 + " x2 is " +
								 * x2); System.out.println("m "+m+" v: " + v +"
								 * 0: " + zero + " d is: " + d);
								 * System.out.println("x:" + x +" pdf:" +
								 * nd.pdf(x) +"\nand cdf:" + nd.cdf(x));
								 * System.out.println("my own pdf: " +
								 * Utils.pdf(m,v,x)); System.out.println("with
								 * log:" +
								 * Utils.pdf(Math.log(m),Math.log(v),Math.log(x)));
								 * System.out.println("multiple gaussian: " +
								 * Utils.kernelDensityEstimator(x,seenNumbers[k].length,seenNumbers[k],m));
								 */
								//if(seenNumbers[k].length)
								//System.out.print(" check"+k);
								d1 = Utils.kernelDensityEstimator(d, m_NumValues[k], m_seenNumbers[k], m_Weights[k],
										m_StandardDev, findNearestValue(k, d), m_SumOfWeights[k]);
								//else
								//d=-1;
								//	System.out.println("d is " + d);

								//if (d<zero)
								//	    {
								//d=zero;
								//	    }
								//else//added by shlomo, since small will not
								// include in calculation
								//System.out.println(d);
								//	{
							probCollection[i] += Math.log(d1);
								//other += Math.log(d1);
							} catch (NumberFormatException e) {
								/* oops! But I am nice, I'll ignore this record */
								/* instead of simply dying :-) */
								/* should not be triggered */
								System.out.println("malformatted double" + e);
								//prob = 0;
								// break;
							}
						}
						
					}//end for each feature
					probCollection[i] += Math.log(classCounts[i]/totalExamplesSeen);

			}//end running through all classes

//no to see which class has highest score
				for(int i=0;i<classCounts.length;i++){
					if(classCounts[i]<3){
						continue;
					}
										
					if(isFirst ){
						certainty = probCollection[i];
						best_class = i;
						isFirst = false;
					}
					else if(probCollection[i] > certainty){
						certainty = probCollection[i];
						best_class = i;
						
					}
					denominator += probCollection[i];
				}
										
	//System.out.println(best_class+" "+Math.exp(certainty) +" "+Math.exp(denominator) +" "+((int)(100. * certainty/denominator)));
		return new resultScores(best_class,(int)(100. * certainty/denominator),msgData.getMailref());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
	// TODO Auto-generated method stub

	}


	/**
	 * train and write results into the file same as "train" without fileName
	 * parameter
	 */
	/*
	 * public void train(final short[] header, final String[] classes,final
	 * String[][] data, final String fileName,final int ignore) throws Exception {
	 * 
	 * train(header,classes,data,ignore); if (fileName == null) return;
	 * PrintWriter f; try { f = new PrintWriter(new FileWriter(fileName)); }
	 * catch (Exception e) { throw new Exception ("cannot find file "+fileName); }
	 * 
	 * f.println("\n\n========== cross-validation =============\n"); String[][]
	 * result = classify(data); int TRAIN_SIZE = result.length; int right=0; int
	 * wrong=0; int[][] confusion = new
	 * int[classnames.size()][classnames.size()]; for (int i=0;i
	 * <classnames.size();i++) for (int k=0;k <classnames.size();k++)
	 * confusion[i][k]=0; for (int i=0;i <result.length;i++) { if
	 * (data[i][classnum].equals(result[i][0])) right++; else wrong++; int
	 * real_class = classnames.indexOf(data[i][classnum]); int assumed_class =
	 * classnames.indexOf(result[i][0]); if (real_class>=0 && assumed_class>=0)
	 * confusion[real_class][assumed_class]++; } f.println("Total Number of
	 * Instances: "+result.length); double x = 100*((double)right)/TRAIN_SIZE; x =
	 * Math.round(x*10000)/10000; f.println("Correctly classified instances:
	 * "+right+" "+x+"%"); x = 100*((double)wrong)/TRAIN_SIZE; x =
	 * Math.round(x*10000)/10000; f.println("Incorrectly classified instances:
	 * "+wrong+" "+x+"%");
	 * 
	 * f.println("\n\n========== Confusion Matrix ============="); for (int i =
	 * 0;i <classnames.size();i++) if (!util(i)) { String tmps =
	 * (String)classnames.get(i); while (tmps.length() <7) tmps=tmps+" ";
	 * f.print(tmps); }
	 * 
	 * f.println(" <--- classified as "); // i - real class, j - assumed class
	 * for (int i = 0;i <classnames.size();i++) if (!util(i)) { for (int j=0;j
	 * <classnames.size();j++) if (!util(j)) { String tmps =
	 * Integer.toString(confusion[i][j]); while (tmps.length() <7) tmps=tmps+" ";
	 * f.print(tmps); } f.println("| "+classnames.get(i)); }
	 * 
	 * f.println("\n\n============= Details of the model ===========");
	 * formatModel(f); f.close(); }
	 */

}//end class



/*******************************************************************************
 * public static void main (String[] args) { NBayesClassifier nbc = new
 * NBayesClassifier(); int HEADER_SIZE=6; int TRAIN_SIZE=5000; int
 * START_RECORD=0; int TEST_SIZE=15000;
 * 
 * int class_place=2; short[] header = new short [HEADER_SIZE];
 * 
 * header[0]=NBayesClassifier.DISCRETE; header[1]=NBayesClassifier.DISCRETE;
 * 
 * header[2]=NBayesClassifier.CLASS;
 * 
 * header[3]=NBayesClassifier.DISCRETE; header[4]=NBayesClassifier.DISCRETE;
 * header[5]=NBayesClassifier.REAL;
 * 
 * String[] classes = new String[3]; classes[0]="i"; classes[1]="n";
 * classes[2]="?"; BufferedReader f;
 * 
 * try { f = new BufferedReader(new FileReader("my_data.rtf")); int k=0; String
 * s = f.readLine(); String[][] data = new String[TRAIN_SIZE][HEADER_SIZE];
 * String[][] test_data = new String[TEST_SIZE][HEADER_SIZE]; while (s!=null &&
 * k <START_RECORD) { s=f.readLine(); k++; } while (s!=null && k
 * <(START_RECORD+TRAIN_SIZE+TEST_SIZE)) { StringTokenizer tok = new
 * StringTokenizer(s,", "); String receiver = tok.nextToken(); tok.nextToken();
 * String sender = tok.nextToken(); tok.nextToken(); String xx=tok.nextToken();
 * tok.nextToken(); String size = tok.nextToken(); String format =
 * tok.nextToken(); tok.nextToken(); String cls = tok.nextToken(); if (k
 * <(TRAIN_SIZE+START_RECORD)) { int l = k - START_RECORD; data[l][0]=sender;
 * data[l][1]=format; data[l][2]=cls; data[l][3]=receiver; data[l][4]=xx;
 * data[l][5]=size; } else { int l = k - START_RECORD - TRAIN_SIZE;
 * test_data[l][0]=sender; test_data[l][1]=format; test_data[l][2]=cls;
 * test_data[l][3]=receiver; test_data[l][4]=xx; test_data[l][5]=size; }
 * 
 * k++; s = f.readLine(); } nbc.train(header,classes,data,"BigModel.xxx",0);
 * 
 * k=0;
 * 
 * String[][] result = nbc.classify(data); int right=0; int wrong=0; int
 * undefined = 0; for (int i=0;i <result.length;i++) { if
 * (data[i][class_place].equals(result[i][0])) right++; else if
 * (result[i][0].equals("")) undefined++; else wrong++; } double x =
 * ((double)right)/TRAIN_SIZE; System.out.println("correctly for training set:
 * "+x); x = ((double)wrong)/TRAIN_SIZE; System.out.println("incorrectly for
 * training set: "+x); x = ((double)undefined)/TRAIN_SIZE;
 * System.out.println("illegal for training set: "+x);
 * 
 * result = nbc.classify(test_data); right=0; undefined = 0; wrong = 0; for (int
 * i=0;i <result.length;i++) { if
 * (test_data[i][class_place].equals(result[i][0])) right++; else if
 * (result[i][0].equals("")) undefined++; else wrong++; } x =
 * ((double)right)/TEST_SIZE; System.out.println("correctly for test set: "+x);
 * x = ((double)wrong)/TEST_SIZE; System.out.println("incorrectly for test set:
 * "+x); x = ((double)undefined)/TEST_SIZE; System.out.println("illegal for test
 * set: "+x); } catch (Exception e) { System.out.println(e); return;} }
 ******************************************************************************/











