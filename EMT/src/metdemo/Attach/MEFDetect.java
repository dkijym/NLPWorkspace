package metdemo.Attach;
import java.io.*;
import java.util.*;
//import java.lang.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MEFDetect{
    private Hashtable hashModel = null;
    private String benignFile = "benign";
    private String maliciousFile = "malicious";
    private String outputModel = "mefModel";
    private String preBuiltModel = "useModel";
    private int windowsize = 6;

    int fileCounter = 0;
    String save = "MEFModel/Model";
    String modelDir = "MEFModel";

    /**
       constructor, read the training data
       for convience, I do everything here
    */
    public MEFDetect(){

    }

    /**********************************************/
    //these are new during spring break
    //don't read file, only from database
    public boolean detect2(String filename, int win, double threshold, byte[] attach){
	System.out.println("Detect... "+filename);
	Vector malicious = getFiles(modelDir);
	try{
	    SFeature[] features = getMap(getTestFeature(attach, win, filename).table);
	    String malFile = "";
	    //read multiple files
	    for(int i=0;i<malicious.size();i++){
		malFile = (String)malicious.get(i);
		SModel train = readModels(malFile);
		double prob = computeProb(features, train);
		if(prob > -0.00000001 && prob < 0.00000001)return false;
		if(prob>=train.min-threshold && prob<=train.max+threshold)
		    return true;
	    }
	    return false;
	}
	catch(Exception e){
	    System.out.println(e);
	}
	return false;
    }

    public SModel getTestFeature(byte[] attach, int win, String filename){

	String zeroString = "";
	for(int j=0;j<win;j++){
	    zeroString += "0;";
	}
		
	String byteString = "";
	HashMap table = new HashMap();
	long total = 0;
		
	int index = 0;
	while(index<win){
	    byteString = byteString.concat(String.valueOf(attach[index++])+";");
	}
	
	//add the first one
	String key = byteString;
	if(!key.equals(zeroString)){
	    total++;
	    SFeature tmpf = new SFeature(key);
	    table.put(key, tmpf);
	}
		
	
	int length = attach.length;
	while(index<length){
	    byteString = byteString.substring(byteString.indexOf(";")+1,byteString.length());
	    byteString = byteString.concat(String.valueOf(attach[index++])+";");
			
	    key = byteString;
	    if(key.equals(zeroString)){
		continue;
	    }
			
	    if(table.containsKey(key)){
		SFeature f = (SFeature)table.remove(key);
		f.add();
		table.put(key, f);
	    }
	    else{
		SFeature tmpf = new SFeature(key);
		table.put(key, tmpf);
	    }
	}
		
	SModel model = new SModel(table, total);
	model.addFile(filename);
	return model;

    }

    //do the self-training thing
    public void updateModel2(byte[] attach, String filename, int win, double threshold){

	String saveName = "MEFModel/Model";
	String dir = "MEFModel";
	//use saved model
	Vector trainfiles = getFiles(dir);//all the training files
	//13
	//System.out.println("*********************** "+filename);
	int counter = trainfiles.size();
	//add a model if there is none
	if(counter==0){
	    //System.out.print("Train      ");
	    SModel testmodel = getTestFeature(attach, win, filename);//get the test model
	    String savefile = save+String.valueOf(++counter);
	    System.out.println(filename+" new model "+savefile);
	    double[] mm = updateMinMax2(testmodel, testmodel, win, threshold, filename);	    
	    testmodel.min = mm[0];
	    testmodel.max = mm[1];
	    saveModels(testmodel, savefile);
	}
	else{
	    //System.out.print("Train      ");
	    SModel testmodel = getTestFeature(attach, win, filename);//get the test model 
	
	    //test if this file belongs some model
	    //System.out.print("Test      ");
	    int belone = -1;
	    SModel tmpTrainModel = null;
	    for(int i=0;i<trainfiles.size();i++){
		tmpTrainModel = readModels((String)trainfiles.get(i));
		boolean isMal = belongs(getMap(testmodel.table), threshold
					, tmpTrainModel.min,tmpTrainModel.min, tmpTrainModel);
		if(isMal){
		    belone = i;
		    break;
		}
	    }
	    
	    //System.out.print("Update ");
	    if(belone < 0){//doesn't belone anyone
		String savefile = save+String.valueOf(++counter);
		System.out.println(filename+" new model:"+savefile);
		double[] mm = updateMinMax2(testmodel, testmodel, win, threshold, filename);
		testmodel.min = mm[0];
		testmodel.max = mm[1];
		saveModels(testmodel, savefile);
	    }
	    else{//append it, update the saved file
		
		String tmpModel = (String)trainfiles.get(belone);
		if(!tmpTrainModel.files.contains(filename)){
		    
		    SModel toSave = appendModel(testmodel, tmpTrainModel, filename);
		    System.out.println(filename+" append model:"+tmpModel);
		    double[] mm = updateMinMax2(testmodel, toSave, win, threshold, filename);
		    toSave.min = mm[0];
		    toSave.max = mm[1];
		    saveModels(toSave, tmpModel);
		}
		else{
		    System.out.println(filename+" file exist");
		}
	    }
	}
    }

    //public double[] updateMinMax2(byte[] attach, SModel train
    public double[] updateMinMax2(SModel test, SModel train
				  , int win, double threshold, String filename){
	double[] minmax = new double[2];//min and max
	minmax[0] = train.min;
	minmax[1] = train.max;
	//SModel testm = getTestFeature(attach, win, filename);
	SFeature[] features = getMap(test.table);
	double prob = computeProb(features, train);
	if((minmax[1]-minmax[0])<threshold*3){
	    if(prob<minmax[0])minmax[0] = prob;
	    if(prob>minmax[1])minmax[1] = prob;
	}

	return minmax;
    }

    /**********************************************/


    public boolean detect(String filename, int win, double threshold){

	System.out.println("Detect... "+filename);
	Vector malicious = getFiles(modelDir);
	try{
	    SFeature[] features = getMap(getTestFeatures(filename, win).table);
	    String malFile = "";
	    //read multiple files
	    for(int i=0;i<malicious.size();i++){
		malFile = (String)malicious.get(i);
		SModel train = readModels(malFile);
		double prob = computeProb(features, train);
		//System.out.println(train.min+" "+train.max+" "+prob);
		
		if(prob>=train.min-threshold && prob<=train.max+threshold)
		    return true;
		
	    }
	    return false;
	}
	catch(Exception e){
	    System.out.println(e);
	}
	return false;
    }

    public void buildAllNewModel(String train, int win, double threshold){
	Vector models = makeModel(train, win, threshold);
	updateThreshold(models, win);
    }
    
    /**
       update the minimum and maximum of the maliciouse models
    */
    public void updateThreshold(Vector models, int win){
	System.out.println("Update Threshold...");
	String file = "";
	for(int i=0;i<models.size();i++){
	    file = (String)models.get(i);
	    SModel train = readModels(file);
	    double[] minmax = updateMinMax(win, train);
	    train.min = minmax[0];
	    train.max = minmax[1];
	    System.out.println(train.min+" "+train.max);
	    saveModels(train, file);
	}
    }

    /**
       update and report the minimum and maximum range of malicious models
     */
    public double[] updateMinMax(int win, SModel train){
	double[] minmax = new double[2];//min and max
	minmax[0] = 100;
	minmax[1] = -100;
	try{
	    Vector trainfiles = train.files;
	    //read training files
	    //System.out.println("filesize:"+trainfiles.size());
	    for(int i=0;i<trainfiles.size();i++){
		String filename = (String)trainfiles.get(i);
		SModel testm = getTestFeatures(filename, win);
		SFeature[] features = getMap(testm.table);
		double prob = computeProb(features, train);
		//System.out.println("prob:"+prob);
		if(prob<minmax[0])minmax[0] = prob;
		if(prob>minmax[1])minmax[1] = prob;
		//System.out.println(minmax[0]+" "+minmax[1]);
	    }
	}
	catch(Exception e){
	    System.out.println(e);
	    return null;
	}
	return minmax;
    }

    public double[] updateMinMax(SModel train, int win, String filename, double threshold){
	double[] minmax = new double[2];//min and max
	minmax[0] = train.min;
	minmax[1] = train.max;
	SModel testm = getTestFeatures(filename, win);
	SFeature[] features = getMap(testm.table);
	double prob = computeProb(features, train);
	if((prob<minmax[0]) && ((minmax[1]-minmax[0])<threshold*3) )minmax[0] = prob;
	if(prob>minmax[1])minmax[1] = prob;

	return minmax;
    }

    public boolean belongs(SFeature[] features, double threshold, double min, double max, SModel train){
	double prob2 = computeProb(features, train);
	//System.out.println(prob2+" "+min+" "+max);
	if(prob2>=min-threshold && prob2<=max+threshold)
	    return true;
	else 
	    return false;
    }

    //do the self-training thing
    public Vector makeModel(String train, int win, double threshold){

	//use saved model
	SModel trainModel = null;//temporary useing model
	Vector trainfiles = getFiles(train);//all the training files
	Vector malModels = new Vector();//remember the filename of malicious models
	double preMin = 0;//minimum threshold of last model

	double total = trainfiles.size()-1;//for fun
	System.out.println("Running... 0%");//for fun

	for(int i=0;i<trainfiles.size()-1;i++){
	    
	    //train the first run. if there is no model, build the first one
	    if(trainModel == null){
		System.out.println("Train      ");
		String savefile = save+String.valueOf(++fileCounter);
		trainModel = trainModels(trainfiles, win, i);
		saveModels(trainModel, savefile);
		malModels.add(savefile);
		continue;
	    }
	    System.out.print("Train      ");
	    
	    //trainModel = trainModels(trainfiles, win, i);//get trainmodels
	    String testfile = (String)trainfiles.get(i+1);//test next file to test
	    SModel testmodel = getTestFeatures(testfile, win);//get the test model 

	    //test if this file belongs some model
	    System.out.print("Test      ");
	    int belone = -1;
	    SModel tmpTrainModel = null;
	    for(int j=0;j<malModels.size();j++){
		tmpTrainModel = readModels((String)malModels.get(j));
		double[] minmax = updateMinMax(win, tmpTrainModel);
		boolean isMal = belongs(getMap(testmodel.table), threshold, minmax[0], minmax[1], tmpTrainModel);
		//boolean isMal = testModel(testmodel, win, tmpTrainModel, threshold);
		if(isMal){
		    belone = j;
		    break;
		}
	    }

	    System.out.print("Update \n");
	    if(belone < 0){//doesn't belone anyone
		System.out.println("new model");
		String savefile = save+String.valueOf(++fileCounter);
		saveModels(testmodel, savefile);
		malModels.add(savefile);
	    }
	    else{//append it, update the saved file
		System.out.println("append model");
		String tmpModel = (String)malModels.get(belone);
		SModel toSave = appendModel(testmodel, tmpTrainModel, testfile);
		//String savefile = save+String.valueOf(++fileCounter);
		saveModels(toSave, tmpModel);		
	    }

	    double running = 100*(double)i/total;//for fun
	    System.out.println("Running... "+(int)running+"%");//for fun
	}

	return malModels;
    }

    public SModel appendModel(SModel test, SModel train, String testfile){
	SFeature[] testFeature = getMap(test.table);
	HashMap trainTable = train.table;
	long total = train.totalCount;
	Vector preFiles = train.files;

	for(int i=0;i<testFeature.length;i++){
	    String key = testFeature[i].feature;
	    long count = testFeature[i].count;
	    total += count;
	    if(trainTable.containsKey(key)){
		SFeature f = (SFeature)trainTable.remove(key);
		f.add(count);
		trainTable.put(key, f);
	    }
	    else{
		SFeature tmpf = new SFeature(key, count);
		trainTable.put(key, tmpf);
	    }
	}

	SModel model = new SModel(trainTable, total);
	model.files = (Vector)preFiles.clone();
	model.addFile(testfile);
	return model;
    }

    //do the self-training thing
    public void updateModel(String filename, int win, double threshold){

	String saveName = "MEFModel/Model";
	String dir = "MEFModel";
	//use saved model
	Vector trainfiles = getFiles(dir);//all the training files
	//13

	int counter = trainfiles.size();
	//add a model if there is none
	if(counter==0){
	    System.out.print("Train      ");
	    SModel testmodel = getTestFeatures(filename, win);//get the test model 	
	    String savefile = save+String.valueOf(++counter);
	    System.out.println("new model");
	    double[] mm = updateMinMax(testmodel, win, filename, threshold);
	    testmodel.min = mm[0];
	    testmodel.max = mm[1];
	    saveModels(testmodel, savefile);
	}
	else{
	    System.out.print("Train      ");
	    SModel testmodel = getTestFeatures(filename, win);//get the test model 
	
	    //test if this file belongs some model
	    System.out.print("Test      ");
	    int belone = -1;
	    SModel tmpTrainModel = null;
	    for(int i=0;i<trainfiles.size();i++){
		tmpTrainModel = readModels((String)trainfiles.get(i));
		boolean isMal = belongs(getMap(testmodel.table), threshold
					, tmpTrainModel.min,tmpTrainModel.min, tmpTrainModel);
		if(isMal){
		    belone = i;
		    break;
		}
	    }
	    
	    System.out.print("Update \n");
	    if(belone < 0){//doesn't belone anyone
		System.out.println("new model");
		String savefile = save+String.valueOf(++counter);
		double[] mm = updateMinMax(testmodel, win, filename, threshold);
		testmodel.min = mm[0];
		testmodel.max = mm[1];
		saveModels(testmodel, savefile);
	    }
	    else{//append it, update the saved file
		
		String tmpModel = (String)trainfiles.get(belone);
		if(!tmpTrainModel.files.contains(filename)){
		    System.out.println("append model");
		    SModel toSave = appendModel(testmodel, tmpTrainModel, filename);
		    double[] mm = updateMinMax(toSave, win, filename, threshold);
		    toSave.min = mm[0];
		    toSave.max = mm[1];
		    saveModels(toSave, tmpModel);
		}
		else{
		    System.out.println("file exist");
		}
	    }
	}
    }

    public SModel trainModels(Vector files, int win, int length){
	HashMap table = new HashMap();
	long total = 0;

	try{
	     String zeroString = "";
	    for(int j=0;j<win;j++){
		zeroString += "0;";
	    }

	    //read multiple files
	    for(int i=0;i<=length;i++){
		String filename = (String)files.get(i);
		DataInputStream dis =
		new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));

		String key = "";
		byte[] singleByte = new byte[1];
		int compareWin = 0;
		
		//solve the hashcode problem
		String byteString = "";

		while(true){
		    if(dis.read(singleByte)<0)break;

		    if(compareWin >= win){
			byteString = byteString.substring(byteString.indexOf(";")+1,byteString.length());
			byteString = byteString.concat(String.valueOf(singleByte[0])+";");
		    }
		    else{
			byteString = byteString.concat(String.valueOf(singleByte[0])+";");
			compareWin++;
			continue;
		    }
		    
		    //skip the zero bytes sequence
		    if(byteString.equals(zeroString)){
			continue;
		    }

		    total++;
		    key = byteString;
		    if(table.containsKey(key)){
			SFeature f = (SFeature)table.remove(key);
			f.add();
			table.put(key, f);
		    }
		    else{
			SFeature tmpf = new SFeature(key);
			table.put(key, tmpf);
		    }
		}

	    }//for files
	}
	catch(Exception e){
	    System.out.println(e);
	}

	SModel model = new SModel(table, total);
	for(int i=0;i<=length;i++){
	    model.addFile((String)files.get(i));
	}
	return model;
    }

    /**
       compute the probability here
     */
    public double computeProb(SFeature[] testTable, SModel train){
	
	//SFeature[] trainTable = getMap(train.table);
	HashMap trainMap = train.table;
	double totalCount = (double)train.totalCount;
	double totalProb = 0;
	double normalize = normalize(testTable);

	//System.out.println("train distinct size:"+trainMap.size()+" test size:"+testTable.length);
	//SUM
	for(int i=0;i<testTable.length;i++){
	    String key = testTable[i].feature;
	    //System.out.println(key);
	    double count = (double)testTable[i].count / normalize;

	    //if this pattern is in the training data
	    if(trainMap.containsKey(key)){
		SFeature trainf = (SFeature)trainMap.get(key);
		totalProb += count * Math.log((double)trainf.count/totalCount);

		//System.out.println("total:"+totalProb+" exp:"+count
		//		   +" P:"+((double)trainf.count/totalCount));
	    }

	    //it is an unseen pattern
	    //use smoothing function
	    //may try different parameters or functions
	    else{
		double distinctCount = 2.0 * (double)trainMap.size();
		totalProb += (double)count 
		    * Math.log(1.0/distinctCount);

		//please also try this one
		//totalProb += (double)count 
		//    * Math.log(1.0/(double)totalCount);
	    }
	}

	return totalProb;
    }

    /**
       compute teh denominator for the normalized number
     */
    public double normalize(SFeature[] list){
	double deno = 0;
	for(int i=0; i<list.length;i++){
	    //deno += (double)(list[i].count*list[i].count);
	    deno += list[i].count;
	}
	//return Math.sqrt(deno);
	return deno;
    }

    /**
       read the byte sequences from a testing file
    */
    public SModel getTestFeatures(String filename, int win){
	HashMap table = new HashMap();
	long total = 0;
	try{
	    String zeroString = "";
	    for(int j=0;j<win;j++){
		zeroString += "0;";
	    }

	    //System.out.println(filename);
	    DataInputStream dis =
		new DataInputStream(
				    new BufferedInputStream(new FileInputStream(filename)));

	    Vector buffer = new Vector();
	    String key = "";
	    int compareWin = 0;
	    byte[] singleByte = new byte[1];
	    String byteString = "";
	    
	    while(true){
		if(dis.read(singleByte)<0)break;
		
		if(compareWin >= win){
		    byteString = byteString.substring(byteString.indexOf(";")+1,byteString.length());
		    byteString = byteString.concat(String.valueOf(singleByte[0])+";");

		    //System.out.print(".");
		}
		else{
		    byteString = byteString.concat(String.valueOf(singleByte[0])+";");
		    compareWin++;
		    continue;
		}
		
		//System.out.println(byteString);
		//skip the zero bytes sequence
		if(byteString.equals(zeroString)){
		    continue;
		}

		total++;
		key = byteString;
		if(table.containsKey(key)){
		    SFeature f = (SFeature)table.remove(key);
		    f.add();
		    //if(!f.source.contains(filename)){
		    //f.source.add(filename);
		    //}
		    table.put(key, f);
		}
		else{
		    SFeature tmpf = new SFeature(key);
		    //tmpf.addSource(filename);
		    table.put(key, tmpf);
		}
	    }
	}
	catch(Exception e){
	    System.out.println(e);
	}

	SModel model = new SModel(table, total);
	model.addFile(filename);
	return model;
    }

    /**
       read the hash table and put the data to an array
     */
    public SFeature[] getMap(HashMap map){
	SFeature[] features = new SFeature[map.size()];
	
	int index = 0;
	//read the data in the hashtable
	Set entries = map.entrySet();
	Iterator iter = entries.iterator();
	while(iter.hasNext()){
	    Map.Entry entry = (Map.Entry)iter.next();
	    features[index] = (SFeature)entry.getValue();
	    index++;
	}

	return features;
    }

    

    /**
       put files into an vector if it's a directory
    */
    public Vector getFiles(String dirName){
	File f = new File(dirName);
	Vector v = new Vector();
	
	if(f.isDirectory()){
	    String s[] = f.list();
	    for(int i = 0; i < s.length; i++){
		File f1 = new File(dirName + "/" + s[i]);
		if(f1.isDirectory()) {
		    Vector v2 = getFiles(f1.toString());
		    for(int j=0; j<v2.size();j++){
			v.add((String)v2.get(j));
		    }
		} 
		else{
		    v.add(f1.toString());
		}
	    }
	}
	else{
	    v.add(dirName);
	}
	return v;
    }

    /**
       use pre-built models
    */
    public SModel readModels(String filename) {
	try {
	    //System.out.println("Loading Model: "+"\""+filename+"\""+"...");
	    FileInputStream in = new FileInputStream(filename);
	    GZIPInputStream in2 = new GZIPInputStream(in);
	    ObjectInputStream s = new ObjectInputStream(in2);
	    Object temp = s.readObject();
	    s.close();
	    in.close();
	    if (temp instanceof SModel) {
		//System.out.println("Load successful...");
			return (SModel) temp;
	    } else {
			System.out.println("File format error.");
			return null;
	    }
	} catch (Exception e) {
	    System.out.println("Exception in file read in: " + e);
	    return null;
	}
    }

    public static boolean saveModels(SModel toSave, String filename){
	try {
	    //System.out.println("\nSaving Model as: " + filename);
	    //System.out.println(toSave.totalCount+" "+toSave.table.size());
	    //ObjectOutputStream s =
		//new ObjectOutputStream(new FileOutputStream(filename));
		
		FileOutputStream out = new FileOutputStream(filename);
		GZIPOutputStream out2 = new GZIPOutputStream(out);
		ObjectOutputStream s = new ObjectOutputStream(out2);
		
	    s.writeObject(toSave);
	    s.flush();
	    s.close();
	    //System.out.println("Done saving Model...\n");
	    return true;
	} catch (Exception e) {
	    System.out.println("Exception in file write out: " + e);
	    return false;
	}
    }

    public static void main(String[] args) {
	String train = "";
	String test = "";
	int window = 6;
	double threshold = 0;
	try{
	    train = args[0];
	    window = Integer.parseInt(args[1]);
	    threshold = Double.parseDouble(args[2]);
	}
	catch(Exception e){
	    System.out.println("Usage: java MEFDetect"
			       +" trainFile testFile windowSize");
	    System.exit(1);
	}
	
	if(window<2)System.out.println("Please assign window size >= 2");
	else{
	    MEFDetect mef = new MEFDetect();
	    mef.buildAllNewModel(train, window, threshold);
	    //mef.detect(train, window, threshold);
	}	
    }

}				

class SFeature implements Serializable{
    
    long count;//total times in all the viral files
    String feature;//the hash code

    public SFeature(String f){
	feature = f;
	count = 1;
    }

    public SFeature(String f, long c){
	feature = f;
	count = c;
    }

    public void add(){
	count ++;
    }

    public void add(long c){
	count += c;
    }
}

/**
   the model
*/
class SModel implements Serializable{
    double max = -100;
    double min = 100;
    HashMap table = null;//the hash table includes all the features
    long totalCount = 0;//total count of all the instances
    Vector<String> files = new Vector<String>();

    public SModel(HashMap h, long c){
	table = h;
	totalCount = c;
    }

    public void addFile(String f){
	files.add(f);
    }

}

class ByteSequence{
    byte[] sequence;

    public ByteSequence(byte[] bt){
	sequence = bt;
    }
}
