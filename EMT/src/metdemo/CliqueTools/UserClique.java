package metdemo.CliqueTools;

//import java.lang.*;
import java.util.*;

public class UserClique{
    
    Vector recpSet = null; // set of recipient lists
    Vector orderedSet = null; // ordered sets 
    Vector minSet = null;  // min sets
    int minSize = 1000; // min size of recp list, set high to ensure overwrite
    int maxSize = 0;    // max size of recp list, set low to ensure overwrite

    // Detects user clique violation 

    public UserClique(String[][] rawData){
	recpSet = getAllSets(rawData);

    }


    ///////////////    
    // find minimum sets
    //
    public void findMinSets(){
	orderedSet = orderAllSets(recpSet);
	minSet = getMinSets(orderedSet);
    }


    ///////////////
    // Process raw records into set of recp lists
    // Each recp list contains addresses belonging to the same mailref
    // Assumption: records are in increasing order of time;
    //             thus only need to search consecutive records

    public Vector getAllSets(String[][] data){
	Vector sets = new Vector();  // set of recipient lists

	if (data.length == 0)
	    return sets;         // in case data is empty

	String mailref = data[0][1];    // initially set to first record
	Vector recp = new Vector();     // a recp list; with common mailref

	recp.add(data[0][0]);
	for (int i=1;i<data.length;i++){
	    if (data[i][1].equals(mailref)){
		recp.add(data[i][0]);
	    }
	    else {
		sets.add(recp);
		int setSize = recp.size();
		if (setSize > maxSize)
		    maxSize = setSize;
		else if (setSize < minSize)
		    minSize = setSize;
		recp = new Vector();
		mailref = data[i][1];
		recp.add(data[i][0]);
	    }
	}
	sets.add(recp);

	// show set of recp lists

	return sets;
    }


    ///////////////
    // order recp lists in increasing list size
    public Vector orderAllSets(Vector sets){
	Vector orderedSet = new Vector();
	for (int i=minSize;i<=maxSize;i++){   // order in increasing size
	    for (int j=0;j<sets.size();j++){
		Vector tempVec = (Vector) sets.elementAt(j);
		if (tempVec.size() == i){
		    orderedSet.add(sets.elementAt(j));
		}
	    }
	}

	// show set of recp lists

	return orderedSet;
    }


    ///////////////
    // Get minimum sets
    // A min set is one that cannot be subsumed by other sets
    // Assumption: Sets are pre-sorted in increasing size
    public Vector getMinSets(Vector sets){
	Vector minSet = new Vector();    // min sets
	for (int i=0;i<sets.size();i++){  // check if a set is a min set
	    Vector currentSet = (Vector) sets.elementAt(i);
	    boolean found = false;

	    // check to see if it's subsumed by a known min set
	    Vector tempSet;  // to see if this set subsums currentSet
	    int j;  // index to move around a check list
	    j = 0;
	    while ((!found) && (j<minSet.size())){
		tempSet = (Vector) minSet.elementAt(j);
		found = tempSet.containsAll(currentSet);
		if (found)
		    break;
		j++;
	    }

	    // check to see if set is subsumed by another down the list
	    j=i+1;
	    while ((!found) && (j<sets.size())){
		tempSet = (Vector) sets.elementAt(j);
		found = tempSet.containsAll(currentSet);
		if (found)
		    break;
		j++;
	    }
	    if (!found)  // set is a min set
		minSet.add(currentSet.clone());
	}

	return minSet;
    }


    ///////////////
    // return emails that's deduced from records
    //
    public Vector getSets(){
	return recpSet;
    }


    ///////////////
    // return min set that's already found
    //
    public Vector getMinSets(){
	return minSet;
    }


    ///////////////
    // print content of sets
    //
   /* public void printSets(Vector sets){
	Vector tempSet;
	
	for (int i=0;i<sets.size();i++){
	    tempSet = (Vector) sets.elementAt(i);
	}
    }
*/

    // main()
    //public static void main(String[] args){
    //}

}
