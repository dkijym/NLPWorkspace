package metdemo.MachineLearning;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;

/**
 * Class which impliments Paul Graham's Plan for spam -
 * <P>
 * baseline
 * <P>
 * Rah Rah
 * <P>
 * We currently have ngram..so paul graham becomes...! pgram :)
 * 
 * @author Shlomo Hershkop
 */

public class PGram extends MLearner {
	/**
	 * hashtbale of joint prob from training ie the chance that each token here
	 * is spam :)
	 */
	Hashtable m_hash;

	String otherclass = new String("");

	String TARGET_CLASS = MLearner.SPAM_LABEL;

	boolean inTraining = true;

	Hashtable hash_nc;

	/** hash of spam count */
	Hashtable hash_sc;

	/** Constructor just sets the id to PGram */
	int s_count, n_count;

	public PGram() {
		super.setID("PGram");
		m_hash = new Hashtable();
		hash_sc = new Hashtable();
		hash_nc = new Hashtable();
		s_count = 0;
		n_count = 0;
	}

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_PGRAM;
    }
	/**
	 * training as described in plan for spam
	 * <P>
	 * basic algorithm:
	 * <P>
	 * <P>
	 * create 2 hashtables , one normal tokens, one spam tokens.
	 * <P>
	 * Tokens = a-Z,0-9,-,',",$ and not only numbers In each do count. also
	 * ngodd = normal emails nspam = spam email count
	 * <P>
	 * <P>
	 * For Each token in each table (distict) place into m_hash and score
	 * accoriding to formula
	 * <P>
	 * <P>
	 */
	/*
	 * public final void train(short header[], String classes[], String
	 * data[][],int n,final String TARGET) throws Exception{ /** the good hash
	 * of normal tokens count * / TARGET_CLASS = TARGET;
	 * 
	 * BusyWindow bw = null; if(showProgress()) { bw= new BusyWindow("Training
	 * PGram","Progress Report"); bw.show(); } /** will hold tokens from each
	 * message * / //Vector Tokens = new Vector(); int len = data.length;
	 * Integer count = new Integer(0); Enumeration keys; StringTokenizer stk;
	 * 
	 * 
	 * /** for each message * / for(int i=0;i <len;i++){ if(showProgress())
	 * bw.progress(i,len); /** make sure vector of tokens is empty * /
	 * 
	 * /** break the message into tokens * /
	 *  // need to see which hashtable to use..not sure if its already lower cas
	 * if(data[i][1].equals(TARGET_CLASS)) // data[i][1].equals("S") {
	 * 
	 * s_count++; stk = new StringTokenizer(data[i][2].toLowerCase() + " " +
	 * data[i][0].toLowerCase(),MLearner.tokenizer);//can be refined here String
	 * t = new String(); while(stk.hasMoreTokens()) { t= stk.nextToken();
	 * 
	 * if(hash_sc.containsKey(t)) {//need to increment count count =
	 * (Integer)hash_sc.get(t); hash_sc.put(t,new Integer(count.intValue() +1)); }
	 * else //first time so make 1 as count hash_sc.put(t,new Integer(1));
	 *  } } else //normal emails { otherclass = data[i][1]; n_count++; stk = new
	 * StringTokenizer(data[i][2].toLowerCase() + " " +
	 * data[i][0].toLowerCase(),MLearner.tokenizer);
	 * 
	 * String t = new String(); while(stk.hasMoreTokens()) { t =
	 * stk.nextToken();
	 * 
	 * if(hash_nc.containsKey(t)) {//need to increment count count =
	 * (Integer)hash_nc.get(t); hash_nc.put(t,new Integer(count.intValue() +1)); }
	 * else //first time so make 1 as count hash_nc.put(t,new Integer(1)); }
	 * 
	 *  }
	 * 
	 * 
	 * 
	 * }//end loop through training data
	 *  // System.out.println("Reality check shash: " + hash_sc.size() + " and
	 * normal : " + hash_nc.size());
	 * 
	 * 
	 * 
	 * //now to calc joint probabilites //will start with spam counting String
	 * m_tok;//local tokens when flipping through tables double g=0,b=0;
	 * 
	 * double x,y,t; for(int i=0;i <2;i++) { if(i==0) keys = hash_sc.keys();
	 * else keys = hash_nc.keys();
	 * 
	 * 
	 * while(keys.hasMoreElements()){
	 * 
	 * m_tok = (String)keys.nextElement();
	 * 
	 * 
	 * if(hash_sc.containsKey(m_tok)) { b =
	 * ((Integer)hash_sc.get(m_tok)).doubleValue(); hash_sc.remove(m_tok); }
	 * else b = .99;
	 * 
	 * if(hash_nc.containsKey(m_tok)) { g =
	 * ((Integer)hash_nc.get(m_tok)).doubleValue(); g = g*2;
	 * hash_nc.remove(m_tok); } else g = .01;
	 * 
	 * if(g+b > 5) { x =Math.min(1.0, b/s_count); y =Math.min(1.0, g/n_count);
	 * 
	 * t = Math.min(.99 , x/(x+y)); t = Math.max(.01, t);
	 * 
	 * //lets set tok to t in m_hash
	 * 
	 * m_hash.put(m_tok,new Double(t)); } }//end while keys }//end for 2 loop
	 * 
	 * 
	 * System.out.println("done the training part in pgram,..." +
	 * m_hash.size()); if(showProgress()){ bw.hide(); bw = null;} }
	 *  
	 */

	/** classification of body of emails */
	/*
	 * public String[][] classify(String data[][]) throws Exception{
	 * //System.out.println("classify in pgram.."+ m_hash.size());
	 * 
	 * StringTokenizer stk; BusyWindow msg=null; if(showProgress()) { msg = new
	 * BusyWindow("Classify PGram","Progress Report"); msg.show(); } int maxl =
	 * data.length;
	 * 
	 * 
	 * String ret[][] = new String[data.length][2]; for(int ii=0;ii <maxl;ii++) {
	 * if(showProgress()) msg.progress(ii,maxl); //1 need to tokenize the body
	 * //Vector tok = getPGram(data[ii][0]); //now to score each of the toks.
	 * 
	 * int j=0; stk = new StringTokenizer(data[ii][2].toLowerCase() + " " +
	 * data[ii][0].toLowerCase(),MLearner.tokenizer);
	 * 
	 * String t = new String(); double Scores[] = new double[stk.countTokens()];
	 * while(stk.hasMoreTokens()) { t = stk.nextToken();
	 * 
	 * if(m_hash.containsKey(t)) { Scores[j] =
	 * (((Double)m_hash.get(t)).doubleValue());
	 *  } else { Scores[j] = (0.4); //unknown } j++; }
	 * 
	 * //now to call top 15 scores. //by sorting the scores.
	 * //System.out.println("going to sort\n"); Arrays.sort(Scores);
	 * //System.out.println("sorted"); //now to collect top 15 //need to take
	 * from both ends (pos and neg) double top[] = new double[15]; int s=0,f =
	 * j-1;//maybe minus 2? int loop = 15; if(loop> Scores.length) loop =
	 * Scores.length;
	 * 
	 * for(int i=0;i <loop;i++) { if(Math.abs(.5 - Scores[s]) > Math.abs(.5 -
	 * Scores[f])) { top[i] = Scores[s]; s++; } else { top[i] = Scores[f]; f--; } }
	 * //so now have top 15 double x=1,y=1;
	 * 
	 * for(int i=0;i <loop;i++) {
	 * 
	 * x = x*top[i]; y = y*(1-top[i]); }
	 * 
	 * //now to score double fscore = x/(x+y); //System.out.println(fscore);
	 * ret[ii][1]=new String(""+fscore); if(fscore >= .9)
	 * ret[ii][0]=TARGET_CLASS; else ret[ii][0]=otherclass; }
	 * 
	 * if(showProgress()){ msg.hide(); msg = null; } return ret; }
	 *  
	 */

	//save the computed centroid to file
	public void save(String filename) {
		try {
			System.out.println("\nPGRAM - save : " + filename);
			FileOutputStream out = new FileOutputStream(filename);
			GZIPOutputStream gout = new GZIPOutputStream(out);
			ObjectOutputStream s = new ObjectOutputStream(gout);

			s.writeObject(getID());
			s.writeObject(getFeature());
			s.writeObject(getInfo());

			s.writeObject(m_hash);
			s.writeObject(otherclass.toLowerCase());
			s.writeObject(TARGET_CLASS);
			s.writeDouble(getThreshold());
			s.writeBoolean(isOneClass());
			s.writeBoolean(inTraining);
			s.writeObject(hash_nc);
			s.writeInt(n_count);
			s.writeObject(hash_sc);
			s.writeInt(s_count);
			s.flush();
			gout.close();
			s.close();
			System.out.println("done saving..going to return");
		} catch (Exception e) {
			System.out.println("Exception in file write out: " + e);
		}

	}

	//load back the centroid vector from the file
	public void load(String filename) {
		try {
			FileInputStream in = new FileInputStream(filename);
			GZIPInputStream gin = new GZIPInputStream(in);
			ObjectInputStream s = new ObjectInputStream(gin);
			s.readObject();//this is the id..used elsewhere

			setFeature((String) s.readObject());
			setInfo((String) s.readObject());
			m_hash = new Hashtable();
			m_hash = (Hashtable) s.readObject();
			otherclass = (String) s.readObject();
			TARGET_CLASS = (String) s.readObject();
			setThreshold(s.readDouble());
			isOneClass(s.readBoolean());
			inTraining = s.readBoolean();
			hash_nc = new Hashtable();
			hash_nc = (Hashtable) s.readObject();
			n_count = s.readInt();
			hash_sc = new Hashtable();
			hash_sc = (Hashtable) s.readObject();
			s_count = s.readInt();

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
		inTraining = true;
		/** will hold tokens from each message */
		//int s_count,n_count
		//,len = msgData.getSize();
		Integer count;
		//Enumeration keys;
		StringTokenizer stk;
		String t = new String();
		//s_count = 0;
		//n_count = 0;

		/** for each message */
		//for(int i=0;i<len;i++)
		{
			/** make sure vector of tokens is empty */

			/** break the message into tokens */
			if (isLowerCase()) {
				stk = new StringTokenizer(msgData.getSubject().toLowerCase()
						+ " " + msgData.getBody().toLowerCase(),
						MLearner.tokenizer);//can be refined here
			} else {
				stk = new StringTokenizer(msgData.getSubject() + " "
						+ msgData.getBody(), MLearner.tokenizer);//can be
																 // refined here
			}

			// need to see which hashtable to use..not sure if its already lower
			// cas
			if (Label.equals(TARGET_CLASS)) //  data[i][1].equals("S")
			{

				s_count++;
				//    stk = new StringTokenizer(msgData.getSubject().toLowerCase()
				// + " " +
				// msgData.getBody().toLowerCase(),MLearner.tokenizer);//can be
				// refined here

				while (stk.hasMoreTokens()) {
					t = stk.nextToken();

					if (hash_sc.containsKey(t)) {//need to increment count
						count = (Integer) hash_sc.get(t);
						hash_sc.put(t, new Integer(count.intValue() + 1));
					} else //first time so make 1 as count
					{
						hash_sc.put(t, new Integer(1));
					}
				}
			} else //normal emails
			{
				otherclass = Label;
				n_count++;
				//    stk = new StringTokenizer(msgData.getSubject().toLowerCase()
				// + " " + msgData.getBody().toLowerCase(),MLearner.tokenizer);

				while (stk.hasMoreTokens()) {
					t = stk.nextToken();

					if (hash_nc.containsKey(t)) {//need to increment count
						count = (Integer) hash_nc.get(t);
						hash_nc.put(t, new Integer(count.intValue() + 1));
					} else //first time so make 1 as count
					{
						hash_nc.put(t, new Integer(1));
					}
				}

			}

		}//end loop through training data

		//System.out.println("done the training part in pgram,..." +
		// m_hash.size());

	}

	private void preLabelingWork() {
		//		now to calc joint probabilites
		m_hash.clear();
		Hashtable hash_nc2 = (Hashtable)hash_nc.clone();
		Hashtable hash_sc2 = (Hashtable)hash_sc.clone();
		
		//will start with spam counting
		String m_tok;//local tokens when flipping through tables
		double g = 0, b = 0;
		Enumeration keysEnum;
		double x, y, t;
		for (int i = 0; i < 2; i++) {
			if (i == 0) {
				keysEnum = hash_sc2.keys();
			} else {
				keysEnum = hash_nc2.keys();
			}

			while (keysEnum.hasMoreElements()) {

				m_tok = (String) keysEnum.nextElement();

				if (hash_sc2.containsKey(m_tok)) {
					b = ((Integer) hash_sc2.get(m_tok)).doubleValue();
					hash_sc2.remove(m_tok);
				} else {
					b = .99;
				}

				if (hash_nc2.containsKey(m_tok)) {
					g = 2 * ((Integer) hash_nc2.get(m_tok)).doubleValue();
					//g = g*2;
					hash_nc2.remove(m_tok);
				} else {
					g = .01;
				}

				if (g + b > 5) {
					x = Math.min(1.0, b / s_count);
					y = Math.min(1.0, g / n_count);

					t = Math.min(.99, x / (x + y));
					t = Math.max(.01, t);

					//lets set tok to t in m_hash

					m_hash.put(m_tok, new Double(t));
				}
			}//end while keys
		}//end for 2 loop

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(java.lang.String[])
	 */
	public resultScores doLabeling(EMTEmailMessage msgData)
			throws machineLearningException {
		if (inTraining) {
			//this will calcualte the probabilities, if any training was done,
			// we need to recalculcate
			this.preLabelingWork();
			inTraining = false;
		}

		StringTokenizer stk;

		//1 need to tokenize the body
		//Vector tok = getPGram(data[ii][0]);
		//now to score each of the toks.

		int j = 0;
		if (isLowerCase()) {
			stk = new StringTokenizer(msgData.getSubject().toLowerCase() + " "
					+ msgData.getBody().toLowerCase(), MLearner.tokenizer);

		} else {
			stk = new StringTokenizer(msgData.getSubject() + " "
					+ msgData.getBody(), MLearner.tokenizer);
		}
		
		String t = new String();
		double Scores[] = new double[stk.countTokens()];
		while (stk.hasMoreTokens()) {
			t = stk.nextToken();

			if (m_hash.containsKey(t)) {
				Scores[j] = (((Double) m_hash.get(t)).doubleValue());

			} else {
				Scores[j] = (0.4); //unknown
			}
			j++;
		}

		//now to call top 15 scores.
		//by sorting the scores.
		//System.out.println("going to sort\n");
		Arrays.sort(Scores);
		//System.out.println("sorted");
		//now to collect top 15 //need to take from both ends (pos and neg)
		double top[] = new double[15];
		int s = 0, f = j - 1;//maybe minus 2?
		int loop = 15;
		if (loop > Scores.length)
			loop = Scores.length;

		for (int i = 0; i < loop; i++) {
			if (Math.abs(.5 - Scores[s]) > Math.abs(.5 - Scores[f])) {
				top[i] = Scores[s];
				s++;
			} else {
				top[i] = Scores[f];
				f--;
			}
		}
		//so now have top 15
		double x = 1, y = 1;

		for (int i = 0; i < loop; i++) {

			x = x * top[i];
			y = y * (1 - top[i]);
		}

		//now to score
		double fscore = x / (x + y);
		String lab;
		//System.out.println(fscore);
		//ret[ii][1]=new String(""+fscore);
		if (fscore >= .9) {
			lab = TARGET_CLASS;
		} else {
			lab = otherclass;
		}

		int score = (int) (90 * fscore);
		if (score > 100) {
			System.out.println("too high score" + score);
			score = 100;
		}
		
		if(fscore<.9){
			score = 100-score;
		}
		
		return new resultScores(lab, score, msgData.getMailref());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		// TODO Auto-generated method stub

	}

}//end pgram class

