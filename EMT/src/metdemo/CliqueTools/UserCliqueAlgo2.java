package metdemo.CliqueTools;

import java.util.ArrayList;
import metdemo.Tools.Aliaser;

public class UserCliqueAlgo2 {
    //private EMTDatabaseConnection jdbc;
    private String userAddr;
    private String[][] records;  // original records
    private ArrayList<ArrayList<String>> allSets = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> maxSets = new ArrayList<ArrayList<String>>();
    private int maxSetSize;  // max size of a recipient set
    String aliasFilename;

    public UserCliqueAlgo2(String[][] records, String userAddr,
			String aliasFilename) {
		this.records = records;
		this.userAddr = userAddr;
		this.aliasFilename = aliasFilename;

		maxSetSize = 0;
		// System.out.println(records.length);
		if (this.records.length > 0) {
			preproc();
			findAllSets();
			sortSizeAllSets();
		}
	}


    // pre-process the data                                                          
    private void preproc(){
        for (int i=0;i<records.length;i++){
            records[i][0] = records[i][0].trim();
            records[i][1] = records[i][1].trim();
            records[i][1] = records[i][1].toLowerCase();
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
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    /**
	 * find all recipient lists per mailref
	 */
	public void findAllSets() {
		String mailref = records[0][0];
		ArrayList<String> vec = new ArrayList<String>();

		for (int i = 0; i < records.length; i++) {
			if (!records[i][0].equals(mailref)) {
				allSets.add(vec);
				if (vec.size() > maxSetSize)
					maxSetSize = vec.size();
				vec = new ArrayList<String>();
				mailref = records[i][0];
			}
			vec.add(records[i][1]);
		}

		allSets.add(vec);

		if (vec.size() > maxSetSize)
			maxSetSize = vec.size();
	}


    /**
     * sort recipient lists by size using selection sorting
     *
     */
    public void sortSizeAllSets() {
		ArrayList<ArrayList<String>> tempSet = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < maxSetSize; i++) {
			for (int j = 0; j < allSets.size(); j++) {
				ArrayList<String> vec = allSets.get(j);
				if (vec.size() == i) {
					tempSet.add(vec);
				}
			}
		}
		allSets = tempSet;
	}


    // find maxSets
    public void findMaxSets() {
		if (records.length == 0)
			return;

		boolean subsumed;

		for (int i = 0; i < allSets.size(); i++) {
			ArrayList<String> cand = allSets.get(i);
			subsumed = false;

			// see if set is subset of existing max set
			for (int j = 0; j < maxSets.size(); j++) {
				ArrayList<String> tester =  maxSets.get(j);
				if (tester.containsAll(cand)) {
					subsumed = true;
					break;
				}
			}

			// see if set is subset of a set down the list
			if (!subsumed) {
				for (int j = i + 1; j < allSets.size(); j++) {
					ArrayList<String> tester =  allSets.get(j);
					if (tester.containsAll(cand)) {
						subsumed = true;
						break;
					}
				}
			}
			if (!subsumed) {
				maxSets.add(cand);
			}
		}
	}


    // add user account to user clique
    public void includeSelf(){
	for (int i=0;i<maxSets.size();i++){
	    ArrayList<String> vec =  maxSets.get(i);
	    vec.add(0, userAddr);
	}
    }

    // return maxSets
    public ArrayList<ArrayList<String>> getMaxSets() {
	return maxSets;
    }


    // print all recp sets
    /*public void printAllSets(){
	printSets(allSets);
    }


    // print max sets
    public void printMaxSets(){
	printSets(maxSets);
    }*/


    // print sets
   /* public void printSets(ArrayList records) {
	for (int i=0;i<records.size();i++){
	    ArrayList vec = (ArrayList) records.get(i);
	    for (int j=0; j<vec.size(); j++){
		System.out.println(vec.get(j));		
	    }
	    System.out.println("");
	}
    }*/


/*    public static void test1(){
	String[][] records = new String[10][2]; // {ab,bc,abc,cd,e}
	records[0][0] = "ref 1";
	records[1][0] = "ref 1";
	records[2][0] = "ref 2";
	records[3][0] = "ref 2";
	records[4][0] = "ref 3";
	records[5][0] = "ref 3";
	records[6][0] = "ref 3";
	records[7][0] = "ref 4";
	records[8][0] = "ref 4";
	records[9][0] = "ref 5";
	records[0][1] = "a";
	records[1][1] = "b";
	records[2][1] = "b";
	records[3][1] = "c";
	records[4][1] = "a";
	records[5][1] = "b";
	records[6][1] = "c";
	records[7][1] = "c";
	records[8][1] = "d";
	records[9][1] = "e";

	UserCliqueAlgo2 uc = 
	    new UserCliqueAlgo2(records, "sender", "metdemo/aliases.txt");
	System.out.println("AllSets => ");
	System.out.println("---------------");
	uc.printAllSets();
	System.out.println("---------------");
	System.out.println("User Cliques => ");
	uc.findMaxSets();
	uc.includeSelf();
	uc.printMaxSets();
	System.out.println("---------------");
    }


    public static void test2(){
	String dbHost = "blackberry";
	String dbName = "met02";
	String user = "outsider";
	String password = "pleaze9enter";
	EMTDatabaseConnection jc;
	jc = new EMTCloudscapeConnection(dbHost,
			     dbName,
			     user, password);
	try{
	jc.connect(true);
	}catch(DatabaseConnectionException s){
		System.out.println(s);
		System.exit(-1);
	}
	//String emailAddr = "sal@cs.columbia.edu";
	String emailAddr = "sh553@columbia.edu";
        String query = "select mailref, rcpt from email "+
	    "where sender ='" + emailAddr + "'" +
	    " and dates>= \"2001-01-01\""  +
	    " and dates<=\"2001-12-31\"" +
            " order by mailref";
        String[][] records =null;
	//   String query = "select distinct sender from email";             
	try {
           records =  jc.getSQLData(query);
	}
	catch (Exception e){
	    System.out.println(e);
	    e.printStackTrace();
	}
	

	UserCliqueAlgo2 uc = 
	    new UserCliqueAlgo2(records,  emailAddr, "metdemo/aliases.txt");
        // System.out.println("--------------");
        // System.out.println("All sets: ");
        // System.out.println();
	// uc.printAllSets();
        // System.out.println("--------------");
        uc.findMaxSets();
	uc.includeSelf();
	ArrayList results = uc.getMaxSets();
        uc.printSets(results);
	System.out.println("---------------");
    }*/

	
  /*  public static void main(String[] args){
	//test1();
	test2();
    }*/
}
