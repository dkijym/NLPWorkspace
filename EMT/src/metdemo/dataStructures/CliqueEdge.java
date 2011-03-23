package metdemo.dataStructures;

import java.io.Serializable;
import java.util.HashMap;

public class CliqueEdge implements Serializable{
    private int m_weight;
    private HashMap<String,Integer> m_subjectWords;
    private HashMap<String,String> stoplistMap = new HashMap<String,String>();

    public CliqueEdge(){
    	m_weight = 0;
    	m_subjectWords = new HashMap<String,Integer>();
    }

    public final int getWeight(){
    	return m_weight;
    }

    public final HashMap getSubjectWords(){
    	return m_subjectWords;
    }

    
    /**
     * @param weight
     * @param subject
     * @param minSubjectWordLength
     */
    public final void update(HashMap<String,Integer> m_allWords,final int weight, final String subject, final int minSubjectWordLength) {
		m_weight++;

		String[] words = subject.split("[^\\w-]");
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() < minSubjectWordLength) {
				continue;
			} else if (stoplistMap.containsKey(words[i])) {
				continue;
			}

			// update word set for this edge
			Integer frequency;
			if ((frequency = (Integer) m_subjectWords.get(words[i])) == null) {
				//frequency = new Integer(1);
				m_subjectWords.put(words[i], 1);
			} else {
				//frequency = new Integer(frequency.intValue() + 1);
				m_subjectWords.put(words[i], frequency+1);
			}
			

			// update global word set
			if ((frequency = m_allWords.get(words[i])) == null) {
				//frequency = new Integer(1);
				m_allWords.put(words[i], 1);
			} else {
				//frequency = new Integer(frequency.intValue() + 1);
				m_allWords.put(words[i], frequency+1);
			}
			//m_allWords.put(words[i], frequency);
		}
	}
    
    /**
     * @param subject
     * @param minSubjectWordLength
     */
    
    public void update(HashMap<String,Integer> m_allWords,String subject, int minSubjectWordLength)
    {
      update(m_allWords,1, subject, minSubjectWordLength);
    }
    
    
    //Methods for SHDetector use
    public final void updateWeight(){
    	updateWeight(1);
    }
    
    public final void updateWeight(int n){
    	m_weight += n;
    }
}