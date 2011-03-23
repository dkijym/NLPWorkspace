package metdemo.CliqueTools;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import metdemo.DataBase.DatabaseConnectionException;
import metdemo.DataBase.EMTCloudscapeConnection;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.Aliaser;

public class EnclaveCliqueAlgo2 {
    private EMTDatabaseConnection jdbc;
    private String userAddr;
    private String[][] records;  // original records
    private ArrayList allSets;
    private ArrayList maxCliques;
    Hashtable hashPairs;  // key = pairs, value = count
    Hashtable hashPairLevel; // key = pairs, value = Boolean
    int threshold = 0; // default;
    private ArrayList sortedPairs;
    String aliasFilename;

    public EnclaveCliqueAlgo2(String [][] records, int threshold, 
			      String aliasFilename){
	this.records = records;
	this.threshold = threshold;
	this.aliasFilename = aliasFilename;
	allSets = new ArrayList();
	hashPairs = new Hashtable();
	sortedPairs = new ArrayList();
	hashPairLevel = new Hashtable(); // key = pairs, value = Boolean
	maxCliques = new ArrayList();

	// System.out.println(records.length);
	if (this.records.length > 0){
	    preproc();
	    findAllSets();
	    //printAllSets();
	    findPairStats();
	    //showPairStats();
	    applyThreshold(threshold);
	    //showPairStats();
	    sortPairs();
	    //showSortedPairs();
	    findHashPairLevel();
	    // printHash(hashPairLevel);
	}
    }


    // pre-process the data
    private void preproc(){
	for (int i=0;i<records.length;i++){
	    records[i][0] = records[i][0].trim();
	    records[i][1] = records[i][1].trim();
	    records[i][2] = records[i][2].trim();
	    records[i][1] = records[i][1].toLowerCase();
	    records[i][2] = records[i][2].toLowerCase();
	}
	if (!aliasFilename.equals(""))
	    checkAliases();
    }

    // check for aliases to substitute
    private void checkAliases(){
	try {
	    Aliaser a = new Aliaser(aliasFilename);
	    for (int i=0;i<records.length;i++){
		/*
		System.out.println(records[i][1] + " = > " +
				   a.resolve(records[i][1]));
		*/
		records[i][1] = a.resolve(records[i][1]);
		records[i][2] = a.resolve(records[i][2]);
	    }
	}
	catch (Exception e){
	    System.out.println(e);
	}
    }


    // find lists of participants in email
    // This method also removes duplicate entries
    public void findAllSets() {

	// assuming columns are: {mailref, sender, recp}
	String mailref = records[0][0];
	ArrayList vec = new ArrayList();
	vec.add(records[0][1]);  // sender

	for (int i=0;i<records.length;i++){
	    if (!records[i][0].equals(mailref)){
		allSets.add(vec);
		vec = new ArrayList();
		mailref = records[i][0];
		// System.out.println(mailref);
		vec.add(records[i][1]);
	    }
	    // Before adding record, make sure it's not a duplicate
	    // Duplicate could result when database contains folders
	    //   from sender and recipient of an email
	    if (!vec.contains(records[i][2]))
		vec.add(records[i][2]);
	}
	allSets.add(vec);

	// remove ArrayLists that has only 1 element (self-emailing)    
	ArrayList tempSets = new ArrayList();
	for (int i=0;i<allSets.size();i++){
	    vec = (ArrayList) allSets.get(i);
	    if (vec.size() > 1)
		tempSets.add(vec);
	}
	allSets = tempSets;
    }

    // find pairs stats, using ArrayList as key in hashtable
    public void findPairStats() {
	for (int i=0;i<allSets.size();i++){
	    ArrayList vec = (ArrayList) allSets.get(i);
	    String str1 = (String) vec.get(0);
	    for (int j=1;j<vec.size();j++){
		String str2 = (String) vec.get(j);
		ArrayList vecPair = new ArrayList();
		if (str1.compareTo(str2) <= 0){
		    vecPair.add(str1);
		    vecPair.add(str2);
		}
		else {
		    vecPair.add(str2);
		    vecPair.add(str1);
		}
		if (hashPairs.containsKey(vecPair)){
		    Integer n = (Integer)hashPairs.get(vecPair);
		    int count = n.intValue();
		    hashPairs.put(vecPair, new Integer(count+1));
		}
		else{
		    hashPairs.put(vecPair, new Integer(1));
		}
	    }
	}
    }


    // show pair communication stats
    public void showPairStats(){
	for (Enumeration e = hashPairs.keys(); 
	     e.hasMoreElements();) {
	    ArrayList vec = (ArrayList) e.nextElement();
	    System.out.println(vec.get(0));
	    System.out.println(vec.get(1));
	    Integer n = (Integer)hashPairs.get(vec);
	    System.out.println(n.intValue());
	}
    }


    // remove pairs with infrequent communication
    private void applyThreshold(int threshold){
	for (Enumeration e = hashPairs.keys(); 
	     e.hasMoreElements();) {
	    ArrayList vec = (ArrayList) e.nextElement();
	    Integer n = (Integer)hashPairs.get(vec);
	    if (n.intValue() < threshold){
		hashPairs.remove(vec);
	    }
	}
    }


    // sort pairs 
    // Pre: within each pair, it's already sorted
    private void sortPairs(){
	for (Enumeration e = hashPairs.keys(); 
	     e.hasMoreElements();) {
	    ArrayList vec1 = (ArrayList) e.nextElement();

	    if (sortedPairs.size() == 0){
		sortedPairs.add(vec1);
		continue;
	    }
	    boolean added = false;
	    for (int i=0;i<sortedPairs.size();i++){
		ArrayList vec2 = (ArrayList) sortedPairs.get(i);
		if (compareVec(vec1,vec2) < 0){
		    sortedPairs.add(i,vec1);
		    added = true;
		    break;
		}
	    }
	    if (!added)
		sortedPairs.add(vec1);
	}
    }


    // put hashpairs into proper form for algo
    private void findHashPairLevel(){
        for (Enumeration e = hashPairs.keys();
             e.hasMoreElements();) {
            ArrayList vec = (ArrayList) e.nextElement();
            Boolean b = new Boolean(true);
	    hashPairLevel.put(vec, b);
        }
    }


    // print Hash Keys
    public void printHash(Hashtable hTable){
        for (Enumeration e = hTable.keys();
             e.hasMoreElements();) {
            ArrayList vec = (ArrayList) e.nextElement();
	    System.out.println("key: "+ vec);
        }
    }


    // compare lexographic order of 2 ArrayLists
    // Each vec has a pair of strings
    public int compareVec(ArrayList vec1, ArrayList vec2){
	String str1, str2;
	str1 = (String) vec1.get(0);
	str2 = (String) vec2.get(0);
	
	int result = str1.compareTo(str2);
	if (result != 0)
	    return result;
	else{ // first string not equal
	    str1 = (String) vec1.get(1);
	    str2 = (String) vec2.get(1);
	    result = str1.compareTo(str2);
	    return result;
	}
    }


    // print sorted pairs
    public void showSortedPairs(){
	for (int i=0;i<sortedPairs.size();i++){
	    ArrayList vec = (ArrayList) sortedPairs.get(i);
	    System.out.println(vec.get(0));
	    System.out.println(vec.get(1));
	    System.out.println();
	}
    }


    public void findMaxCliques(){
	if (records.length == 0)
	    return;

	ArrayList allLevelSets = new ArrayList(); // sets at all levels
	ArrayList allLevelHash = new ArrayList(); // hashtable at all levels

	boolean moreLevels = true;
	ArrayList currentLevelSet = sortedPairs;  // starting from sorted pairs
	Hashtable currentLevelHash = hashPairLevel; // hashtable for pairs
	while (moreLevels){
	    ArrayList nextLevelSet;
	    ArrayList candSet;
	    Hashtable nextLevelHash = new Hashtable();
	    
	    // Main steps
	    // derive candidate set for next level
	    // check candidate set for next level
	    // update current-level hashtable
	    // construct hashtable for next level

	    candSet = generateCand(currentLevelSet);
	    // printSets(candSet);
	    nextLevelSet = checkCandSet(candSet, currentLevelHash);
 	    // System.out.println("///////////");
	    // System.out.println("next level:::");
	    // printSets(nextLevelSet);
	    updateCurrentLevelHash(nextLevelSet, currentLevelHash);
	    nextLevelHash = makeNextHash(nextLevelSet);

	    allLevelSets.add(currentLevelSet);
	    allLevelHash.add(currentLevelHash);

	    if (nextLevelSet.size() == 0)
		moreLevels = false;

	    currentLevelSet = nextLevelSet;
	    currentLevelHash = nextLevelHash;
	}

	// get max cliques from the derived structure
	// System.out.println("=====> max Level = " + allLevelSets.size());
	for (int i=0;i<allLevelSets.size();i++){
	    currentLevelSet = (ArrayList) allLevelSets.get(i);
	    currentLevelHash = (Hashtable) allLevelHash.get(i);
	    for (int j=0;j<currentLevelSet.size();j++){
		ArrayList currentSet = (ArrayList) currentLevelSet.get(j);
		if (!currentLevelHash.containsKey(currentSet)){
		    System.out.println("=> no key in hash: " + currentSet);
		    printHash(currentLevelHash);
		}
		Boolean b = (Boolean)currentLevelHash.get(currentSet);
		boolean maxCliq = b.booleanValue();
		if (maxCliq)
		    maxCliques.add(currentSet.clone());
	    }
	}
    }


    // generate candidate sets for the next level
    // Idea: Examine all possible pairs of sets
    // Speedup is possible because sets are sorted
    public ArrayList generateCand(ArrayList currentLevelSet){
	ArrayList candSet = new ArrayList();

	ArrayList tempVec = (ArrayList) currentLevelSet.get(0);
	int levelValue = tempVec.size();

	for (int i=0;i<currentLevelSet.size();i++){
	    ArrayList vec1 = (ArrayList) currentLevelSet.get(i);
	    ArrayList subVec1 = new ArrayList();
	    for (int k=0;k<levelValue-1;k++)
		subVec1.add(vec1.get(k));

	    for (int j=i+1;j<currentLevelSet.size();j++){
		ArrayList vec2 = (ArrayList) currentLevelSet.get(j);
		ArrayList subVec2 = new ArrayList();
		for (int k=0;k<levelValue-1;k++)
		    subVec2.add(vec2.get(k));

		// subVec's are the original vecs without the last element
		if (subVec1.equals(subVec2)){
		    String str1 = (String) vec1.get(levelValue-1);
		    String str2 = (String) vec2.get(levelValue-1);
		    ArrayList vecComb = new ArrayList();
		    for (int k=0;k<levelValue-1;k++)
			vecComb.add(vec1.get(k));
		    if (str1.compareTo(str2) <= 0){
			vecComb.add(vec1.get(levelValue-1));
			vecComb.add(vec2.get(levelValue-1));
			candSet.add(vecComb);
		    }
		    else{
			vecComb.add(vec2.get(levelValue-1));
			vecComb.add(vec1.get(levelValue-1));
			candSet.add(vecComb);
		    }
		}
		else{
		    break;
		}
	    }
	}

	return candSet;
    }


    // check candidate set for next level 
    // put sets that qualify in a new set
    public ArrayList checkCandSet(ArrayList candSet, Hashtable currentLevelHash){
	ArrayList nextLevelSet = new ArrayList();

        for (int i=0;i<candSet.size();i++){
            ArrayList vec = (ArrayList) candSet.get(i);
	    boolean add = true;
            for (int j=0;j<vec.size();j++){
                // forming subVec
                ArrayList subVec = new ArrayList();
                for (int k=0;k<vec.size();k++){
                    if (k != j)
                        subVec.add(vec.get(k));
                }

		//System.out.println(":::" + subVec);
                // check if key is in currentHash
                if (!currentLevelHash.containsKey(subVec)){
		    add = false;
		    break;
		}
	    }
	    if (add)
		nextLevelSet.add(vec.clone());
	}
	/*
	System.out.println("----------Inside CheckCandSet::");
	printSets(nextLevelSet);
	System.out.println("----------End CheckCandSet::");
	*/
	return nextLevelSet;
    }


    // update current-level hashtable
    // remove sets that are subsumed by larger sets
    public void updateCurrentLevelHash	(ArrayList nextLevelSet, 
					 Hashtable currentHash){
	for (int i=0;i<nextLevelSet.size();i++){
	    ArrayList vec = (ArrayList) nextLevelSet.get(i);
	    for (int j=0;j<vec.size();j++){
		// forming subVec
		ArrayList subVec = new ArrayList();
		for (int k=0;k<vec.size();k++){
		    if (k != j)
			subVec.add(vec.get(k));
		}
		
		// set value to false if it's there
		if (currentHash.containsKey(subVec)){
		    // currentHash.remove(subVec);
		    currentHash.put(subVec, new Boolean(false));
		}
	    }
	}
    }

    // generate Hashtable for sets from current level
    public Hashtable makeNextHash(ArrayList sets){
	Hashtable hTable = new Hashtable();
	for (int i=0;i<sets.size();i++){
	    ArrayList vec = (ArrayList) sets.get(i);
	    hTable.put(vec.clone(), new Boolean(true));
	}
	return hTable;
    }


    // return maximal cliques
    public ArrayList getMaxCliques() {
	return maxCliques;
    }


    // print all recp sets
    public void printAllSets(){
	printSets(allSets);
    }


    // print max sets
    public void printMaxCliques(){
	printSets(maxCliques);
    }


    // print sets
    public void printSets(ArrayList records) {
	for (int i=0;i<records.size();i++){
	    ArrayList vec = (ArrayList) records.get(i);
	    for (int j=0; j<vec.size(); j++){
		System.out.println(vec.get(j));		
	    }
	    System.out.println("");
	}
    }
 

    public static void main(String[] args){
        //test1();
	test2();
    }

    public static void test1(){
	/*
	String[][] records = new String[5][3]; 
	// {a->a,a->ab,b->cd}
	records[0][0] = "ref 1";
	records[1][0] = "ref 2";
	records[2][0] = "ref 2";
	records[3][0] = "ref 3";
	records[4][0] = "ref 3";

	records[0][1] = "a";
	records[1][1] = "a";
	records[2][1] = "a";
	records[3][1] = "b";
	records[4][1] = "b";

	records[0][2] = "a";
	records[1][2] = "a";
	records[2][2] = "b";
	records[3][2] = "c";
	records[4][2] = "d";
	*/

	//	{ab,ac,ad,bc,bd,cd,ce}
	String[][] records = new String[7][3]; 	
	records[0][0] = "ref 1";
	records[1][0] = "ref 2";
	records[2][0] = "ref 3";
	records[3][0] = "ref 4";
	records[4][0] = "ref 5";
	records[5][0] = "ref 6";
	records[6][0] = "ref 7";

	records[0][1] = "a";
	records[1][1] = "a";
	records[2][1] = "a";
	records[3][1] = "b";
	records[4][1] = "b";
	records[5][1] = "c";
	records[6][1] = "c";

	records[0][2] = "b";
	records[1][2] = "c";
	records[2][2] = "d";
	records[3][2] = "c";
	records[4][2] = "d";
	records[5][2] = "d";
	records[6][2] = "e";

	EnclaveCliqueAlgo2 ec = 
	    new EnclaveCliqueAlgo2(records, 1, "metdemo/aliases.txt");

	// ec.printAllSets();
	ec.findMaxCliques();
	ArrayList result = ec.getMaxCliques();
	ec.printSets(result);
	System.out.println("---------------");
    }

    public static void test2(){
	String dbHost = "blackberry";
        String dbName = "met02";
        String user = "outsider";
        String password = "";
        EMTDatabaseConnection jc;
        jc = new EMTCloudscapeConnection(dbHost, dbName,
			     user, password);
        try{
        jc.connect(true);
        }catch(DatabaseConnectionException d){
        	System.out.println(d);
        	System.exit(-1);
        }
        String query = "select mailref, sender, rcpt from email "+
	    //"where (folder LIKE '%sent%' OR '%Sent')" +
	    //	    "and dates >= \"2001-01-01\" and dates <= \"2001-12-05\"" + 
	    "order by mailref ";

	//	String query = "select distinct sender from email";
        String[][] records = null;
        try {
         records =    jc.getSQLData(query);
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        

	/*
	for (int i=0;i<records.length;i++){

	    System.out.println(records[i][0]);
	    System.out.println(records[i][1]);
	    System.out.println(records[i][2]);
	    System.out.println();
	}
	*/

	EnclaveCliqueAlgo2 ec = 
	    new EnclaveCliqueAlgo2(records, 1, "metdemo/aliases.txt");
	ec.findMaxCliques();	
	// ec.printAllSets();
	System.out.println("max cliques ==>");
	ec.printMaxCliques();
	System.out.println("---------------");
    }
}
