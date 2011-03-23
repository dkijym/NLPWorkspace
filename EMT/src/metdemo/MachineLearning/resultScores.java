/*
 * Created on Nov 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.MachineLearning;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Shlomo Hershkop
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class resultScores implements Serializable {

	public static final int MIDSCORE = 50;
	public static final int SMALLSCORE = 0;
	public static final int LARGESCORE = 100;
	private String classLabel;
	private int classScore;
	private String realLabel; //when known
	private String m_mailref;//so know which example are labeling
	private boolean isTesting = true;
	
	/**
	 *  
	 */
	public resultScores(String label, int scored,String mailref) {
		classScore = scored;
		classLabel = label;
		m_mailref = new String(mailref);
	}

	public resultScores(int label, int scored,String mailref) {
		classScore = scored;
		classLabel = MLearner.getClassString(label);
		m_mailref = new String(mailref);
	}

	
	public boolean isTesting(){
		return isTesting;
	}
	
	/**
	 * Keeps track if we are running a test or training example
	 * @param arewetesting is we are in testing set to true(default)
	 */
	public void setTesting(boolean arewetesting){
		isTesting = arewetesting;
	}
	
	public int getScore() {
		return classScore;
	}

	public String getLabel() {
		return classLabel;
	}

	public final void setMailref(String mailf){
		m_mailref = mailf;
	}
	
	public String getMailref(){
		return m_mailref;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(classScore);
		out.writeObject(classLabel);
		out.writeObject(realLabel);
		out.writeObject(m_mailref);
		if(isTesting){
			out.writeInt(1);
		}
		else
		{
			out.writeInt(0);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		classScore = in.readInt();
		classLabel = (String) in.readObject();
		realLabel = (String) in.readObject();
		m_mailref = (String) in.readObject();
		int n = in.readInt();
		if(n==0){
			isTesting = false;
		}
		else
		{
			isTesting = true;
		}
	}

	public void setRealLabel(String rl){
		realLabel = rl;
	}
	
	public String getRealLabel(){
		return realLabel;
	}
}