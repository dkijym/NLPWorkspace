package metdemo.dataStructures;

public class WordCount implements Comparable{
	
	private int count;
	private String word;
	
	/**
	 * 
	 */
	public WordCount(String wrd,int cnt) {
		word = wrd;
		count = cnt;
	}

	
	public String getWord()
	{
		return word;
	}
	
	public int getCount(){
		return count;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		//most of the time count 1, so optimized for that
		if(count == ((WordCount)arg0).getCount())
			return 0;
		else if(count < ((WordCount)arg0).getCount())
			return 1;
		//else if(count > ((WordCount)arg0).getCount())
		return -1;
	}
	
	
	
	
}