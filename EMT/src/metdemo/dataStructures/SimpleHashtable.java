
/**
 * Implementation of a quadratic probing
 * hashtable. 
 * For calculate Hellinger Distance
 * @Yen-Jong Tu
 */



package metdemo.dataStructures;

/*
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.lang.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.SQLException;
import chapman.graphics.JPlot2D;
import java.text.NumberFormat;
*/
import java.util.Vector;


public class SimpleHashtable
{
	
	private static final int DEFAULT_TABLE_SIZE = 6;

	/**
	 * The array of elements. 
	 */
	private HashEntry [] entry;
	
	/**
	 * Current number of entries.
	 */
	private int currentSize;

	/**
	 * Empty constructor.
	 */
	public SimpleHashtable()
	{
		this(DEFAULT_TABLE_SIZE);
	}

	/**
	 * Constructor with size parameter.
	 * @param size the approximate initial size.
	 */
	public SimpleHashtable(int size)
	{
		allocateArray(size);
		makeEmpty();
	}

	/**
	 * Returns number of entries in table.
	 */
	public int getSize()
	{
		return currentSize;
	}
	
	/**
	 * Returns a vector of the keys in the table.
	 * Akin to java.util.Hashtable.keys()
	 *
	 */
	public Vector getKeys()
	{
		// create a vector contain what we'll return
		Vector ret = new Vector();
		
		for(int i=0; i<entry.length; i++)
		{
			// make sure there's a live element at
			// this position in the array
			if(isPositionActive(i))
				ret.addElement(entry[i].getKey());
		}
		return ret;
	}// end of getKeys()

	/**
	 * Insert into the hash table. 
	 * @param x the item to insert.
	 */
	public void insert(String theKey, String direct, double value, double value2)
	{
		// find position for this key
		int currentPos = findPos(theKey);
		
		// if this key is already in the table		
		if (entry[currentPos] != null)
		{
			if(! isPositionActive(currentPos))
			{
				entry[currentPos].setActive(true);
				entry[currentPos].setTrainFreq(0.0);
				entry[currentPos].setTestFreq(0.0);
			}
			if (direct.equals("train"))
			{
				double freq = entry[currentPos].getTrainFreq()+value;
				entry[currentPos].setTrainFreq(freq);
			}else{ 
				if(direct.equals("test"))
				{
					double freq = entry[currentPos].getTestFreq()+value;
					entry[currentPos].setTestFreq(freq);
				}
			}
		}else{
		// otherwise, either key isn't present - so we 
		// insert a new HashEntry

		// create a new, active HashEntry
			if(direct != null)
			{
				if (direct.equals("train"))
					entry[currentPos] = new HashEntry(theKey, value, 0.0, true);
				else
					entry[currentPos] = new HashEntry(theKey, 0.0, value, true);
			}else{
				entry[currentPos] = new HashEntry(theKey, value,value2,true);
			}
		}

		// if load factor is greater than 0.5,
		// rehash into a bigger table
		if( ++currentSize > entry.length / 2 )
			rehash();
	}




	/**
	 * Expand the hash table.
	 */
	private void rehash(){
		
		// copy the current storage array
		HashEntry [] oldArray = entry;

		// Create a new double-sized, empty table
		allocateArray( nextPrime( 2 * oldArray.length ) );
		currentSize = 0;

		// Copy old array over, leaving behind any logically
		// deleted entries
		for( int i = 0; i < oldArray.length; i++ ){
			
			// can't use isPositionActive() b/c it only looks
			// at the current storage array
			if( oldArray[i] != null && oldArray[i].isActive() )
				insert(oldArray[i].getKey(),null, oldArray[i].getTrainFreq(), oldArray[i].getTestFreq());
		}
		return;
	}


	/**
	 * Method that performs quadratic probing resolution.
	 * @param x the item to search for.
	 * @return the position where the search terminates.
	 */
	private int findPos(String theKey){
		
		int collisionNum = 0;
		
		int currentPos = hash(theKey, entry.length);

		while(entry[currentPos] != null && !entry[currentPos].getKey().equals(theKey) ){
		
			currentPos += 2 * ++collisionNum - 1;  // Compute ith probe
		
			// Implement the mod
			if( currentPos >= entry.length ){       
					currentPos -= entry.length;
			}
		}
		return currentPos;
	}



	/**
	 * Remove from the hash table.
	 * @param x the item to remove.
	 */
	public void remove(String theKey, String direct, double value)
	{
		int currentPos = findPos(theKey);
		
		if(isPositionActive(currentPos))
		{
			if(direct.equals("train"))
			{
				double freq = entry[currentPos].getTrainFreq()-value;
				if (freq<=0)
				{
					entry[currentPos].setTrainFreq(0.0);
				}else{
					entry[currentPos].setTrainFreq(freq);
				}	
			}else{
				double freq = entry[currentPos].getTestFreq()-value;
				if (freq<=0)
				{
					entry[currentPos].setTestFreq(0.0);
				}else{
					entry[currentPos].setTestFreq(freq);
				}	
			}
			if(entry[currentPos].getTrainFreq() <= 0.0 && entry[currentPos].getTestFreq() <=0.0)
				entry[currentPos].setActive(false);
		}else{
			
		}
	}


	/**
	 * Find an item in the hash table.
	 * @param x the item to search for.
	 * @return the matching item.
	 */
	public double getTrain(String theKey)
	{
		int currentPos = findPos(theKey);
		
		if(entry[currentPos] != null)
		{
			//if(isPositionActive(currentPos))
			//{
				return entry[currentPos].getTrainFreq();
			//}else{
			//	return -1;
			//}
		}else{
			return -1;
		}
	}

	public double getTest(String theKey)
	{
		int currentPos = findPos(theKey);
		
		if ( entry[currentPos] != null)
			return entry[currentPos].getTestFreq();
		else
			return -1;
	}

	/**
	 * Return true if currentPos exists and is active.
	 * @param currentPos the result of a call to findPos.
	 * @return true if currentPos is active.
	 */
	private boolean isPositionActive(int currentPos)
	{
		return entry[currentPos] != null && entry[currentPos].isActive();
	}

	/**
	 * Make the hash table logically empty.
	 */
	public void makeEmpty()
	{
		currentSize = 0;
		for( int i = 0; i < entry.length; i++ )
			entry[ i ] = null;
	}

	/**
	 * A hash routine for String objects.
	 * @param key the String to hash.
	 * @param tableSize the size of the hash table.
	 * @return the hash value.
	 */
	public static int hash( String theKey, int tableSize )
	{
		int hashVal = 0;

		for( int i = 0; i < theKey.length( ); i++ )
		{
			hashVal = 37 * hashVal + theKey.charAt( i );
		}

		hashVal %= tableSize;
		if( hashVal < 0 )
			hashVal += tableSize;

		return hashVal;
	}

	/**
	 * Internal method to allocate array.
	 * @param arraySize the size of the array.
	 */
	private void allocateArray(int arraySize)
	{
		entry = new HashEntry[ arraySize ];
	}

	/**
	 * Internal method to find a prime number at least as large as n.
	 * @param n the starting number (must be positive).
	 * @return a prime number larger than or equal to n.
	 */
	private static int nextPrime(int n)
	{
	
		if( n % 2 == 0 )
		{
			n++;
		}

		while(!isPrime(n))
		{
			n +=2;
		}

		return n;
	}

	/**
	 * Internal method to test if a number is prime.
	 * Not an efficient algorithm.
	 * @param n the number to test.
	 * @return the result of the test.
	 */
	private static boolean isPrime(int n)
	{
		
		if( n == 2 || n == 3 )
			return true;

		if( n == 1 || n % 2 == 0 )
			return false;

		for( int i = 3; i * i <= n; i += 2 )
		{
			if( n % i == 0 )
				return false;
		}

		return true;
	}

		/**
		 * Private inner class for entries stored in 
		 * the hashtable.
		 */
		class HashEntry
		{

			// the key
			String key;

			//the frequency
			double trainFreq;
			double testFreq;
			
			// flag for logical deletes
			boolean active;

			/**
			 * Simple constructor that sets the 
			 * instance variables.
			 */
			public HashEntry(String key, double trainFreq, double testFreq, boolean active)
			{
				this.key = key;
				this.trainFreq = trainFreq;
				this.testFreq = testFreq;
				this.active = active;
			}

			/**
			 * Key accessor.
			 */
			public String getKey()
			{
				return key;
			}

			/**
			 * get training data frequency
			 */
			public double getTrainFreq()
			{
				return trainFreq;
			}
			
			public double getTestFreq()
			{
				return testFreq;
			}
			
			/**
			 * Active accessor.
			 */
			public boolean isActive()
			{
				return active;
			}

			public void setTrainFreq(double value)
			{
				this.trainFreq = value;
			}
			
			public void setTestFreq(double value)
			{
				this.testFreq = value;
			}			
			
			public void setActive(boolean active)
			{
				this.active = active;
			}
		}
/*
        // Simple main
        public static void main( String [ ] args )
        {
            SimpleHashtable H = new SimpleHashtable( );
            Vector distance = new Vector();
			Vector address = new Vector();
			address.addElement("aoa5@hotmail.com");
			address.addElement("sal@columbia.edu");
			address.addElement("ssh553@columbia.edu");
			address.addElement("aoa5@hotmail.com");				
			address.addElement("eeskin@columbia.edu");
			address.addElement("wl318@columbia.edu");
			address.addElement("yt333@columbia.edu");
			address.addElement("eeskin@columbia.edu");
			address.addElement("aoa5@hotmail.com");
			address.addElement("aoa5@hotmail.com");
			address.addElement("uuu@columbia.edu");
			address.addElement("hhbhf@hotmail.com");//------------12
			address.addElement("hhbhf@hotmail.com");
			address.addElement("hhbhf@hotmail.com");
			address.addElement("wl318@columbia.edu");
			address.addElement("aoa5@hotmail.com");
			address.addElement("sal@columbia.edu");
			address.addElement("ssh553@columbia.edu");
			address.addElement("sal@columbia.edu");
			address.addElement("eeskin@columbia.edu");
            address.addElement("yt333@hotmail.com");
            address.addElement("jc111@columbia.edu");
            address.addElement("vision@hotmail.com");
            address.addElement("yu2004@columbia.edu");
            address.addElement("haha@columb.edu");
            address.addElement("gg@columb.edu");
            
			int defaultSize = 3;
			double trainFreq = 1.0/((double)defaultSize*4);
			double testFreq = 1.0/(double)defaultSize;
			double sum = 0;
			int start = 0;
			
			System.out.println("trainFreq : "+trainFreq);
			System.out.println("testFreq : "+testFreq);
			for(int i=0; i<defaultSize*5; i++)
			{
			    String icp = (String)address.get(i);
			    if (i<defaultSize*4)
			    {
			        H.insert(icp,"train",trainFreq,0);
			    }else{
			        H.insert(icp,"test",testFreq, 0);
			    }
			}
            Vector theKey = H.getKeys();
            for (int i=0; i<theKey.size(); i++)
            {
                String name = (String)theKey.get(i); 
                //System.out.println("Term 0 Key : "+name);
                double train = H.getTrain(name);
                double test = H.getTest(name);
                sum +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
            }
            distance.addElement(new Double(sum));//-----defaultSize                    
                       
            				
			for (int i=defaultSize*5; i< address.size();i++)//total runing times
			{
			    double summ =0;
			    //cut first for training
			    String name =(String ) address.get(start);
			    H.remove(name, "train", trainFreq);
		    	System.out.println("remove train name : "+name+"  subnum : "+H.getTrain(name));
			    
			    //add last for training
			    name = (String)  address.get(start+defaultSize*4);
			    H.insert(name, "train", trainFreq,0);
		    	System.out.println("add train name : "+name+"  subnum : "+H.getTrain(name));			    
			    
			    //cut first for test
			    name = (String )address.get(start+defaultSize*4);
			    H.remove(name, "test", testFreq);
		    	System.out.println("remove test name : "+name+"  subnum : "+H.getTest(name));			    
			    //add last for test
                name = (String )address.get(start+defaultSize*5);
                H.insert(name, "test", testFreq,0 );
		    	System.out.println("add test name : "+name+"  subnum : "+H.getTest(name));                
                //if(i==defaultSize*5				    
			    //compute distance
			    theKey.clear();
			    theKey = H.getKeys();
                for (int j=0; j<theKey.size(); j++)
                {
                    String tag =(String ) theKey.get(j);
                    System.out.println("Term "+(i-defaultSize*5+1)+" Key : "+tag);
                    double train = H.getTrain(tag);
                    System.out.println("Term "+(i-defaultSize*5+1)+" train Num : "+train);
                    double test = H.getTest(tag);
                    System.out.println("Term "+(i-defaultSize*5+1)+" test Num : "+test);
                    
                    summ +=Math.pow(Math.sqrt(train)-Math.sqrt(test),2);
               		System.out.println("Number 3 summ : "+summ);
                }
                if (i==defaultSize*5+1)
                	System.out.println("Number total summ : "+summ);            	
            		
                distance.addElement(new Double(summ));//-----defaultSize 
                start++;
			}
			
			for(int i=0; i<distance.size(); i++)
			{
			    System.out.println("Number "+i+" : "+((Double)distance.get(i)).doubleValue());
			}
        }
 */
}// end of class SimpleHashtable
