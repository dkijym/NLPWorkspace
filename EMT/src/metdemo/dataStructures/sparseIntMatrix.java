/**
 * Created Nov 2006
 * Shlomo Hershkop
 * 
 * idea: efficient representation of sparse matrix
 */
package metdemo.dataStructures;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * @author shlomo
 *
 */
public class sparseIntMatrix implements Serializable{

	//we will either use a int array or hashtable
	int dataints[][];
	Hashtable<Integer , Integer> dataHashtable;
	boolean smallTable;
	int m_size;
	
	public sparseIntMatrix(int size) {
		
		m_size =size;
		if(size < 5000){
			dataints = new int[size][size];
			smallTable = true;
		}else{
			smallTable = false;
			dataHashtable = new Hashtable<Integer, Integer>();
		}
			
	}
	
	
	public int get(int x, int y){
		if(smallTable){
			return dataints[x][y];
		}
		int key = x*m_size+y;
		if(dataHashtable.containsKey(key)){
			return dataHashtable.get(key);
		}
		
		return 0;
	}
	
	public void put(int x,int y, int val){
		if(smallTable){
			dataints[x][y] = val;
			return;
		}
		int key = x*m_size+y;
		/*if(dataHashtable.containsKey(key)){
			dataHashtable.put(key,dataHashtable.get(key)+val);
		}*/
		dataHashtable.put(key, val);
	}
	
	public int getSize(){
		return m_size;
	}
	
}
