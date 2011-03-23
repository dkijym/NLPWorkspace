/*
 * Created on Jan 19, 2005
 *
 * Analyze Links
 */
package metdemo.MachineLearning;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.BagOfLinks;
import metdemo.Tools.LinkExtractor;
import metdemo.dataStructures.LinkInfoDS;

/**
 * @author Shlomo Hershkop Study links and try to group good and bad, and to see
 *         how close an unknown set of links is to good or spam
 */
public class LinkAnalysisLearner extends MLearner {

	//HashMap seenURLS[];
	ArrayList classLinkCollection[];
	int[] classCounts;
	static final int MIN_DISTANCE_TO_INCLUDE = 300;
	HashMap lookupClassLabelHelper = new HashMap();

	public LinkAnalysisLearner() {

		//seenURLS = new HashMap[MLearner.NUMBER_CLASSES];
		classCounts = new int[MLearner.NUMBER_CLASSES];
		classLinkCollection = new ArrayList[MLearner.NUMBER_CLASSES];

		for (int i = 0; i < classLinkCollection.length; i++) {
			classLinkCollection[i] = new ArrayList();
		//	seenURLS[i] = new HashMap();
		}

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#flushModel()
	 */
	public void flushModel() {
		for (int i = 0; i < classLinkCollection.length; i++) {
			classLinkCollection[i].clear();
			classCounts[i] = 0;
		}




	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#save(java.lang.String)
	 */
	public void save(String filename) throws IOException {

		FileOutputStream out = new FileOutputStream(filename);
		GZIPOutputStream gout = new GZIPOutputStream(out);
		ObjectOutputStream s = new ObjectOutputStream(gout);

		s.writeInt(classCounts.length);

		for (int i = 0; i < classCounts.length; i++) {
			s.writeInt(classCounts[i]);
		}

		s.writeInt(classLinkCollection.length);

		for (int i = 0; i < classLinkCollection.length; i++) {
			s.writeObject(classLinkCollection[i]);
		}

		s.flush();
		s.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#load(java.lang.String)
	 */
	public void load(String filename) throws Exception {
		FileInputStream in = new FileInputStream(filename);
		GZIPInputStream gin = new GZIPInputStream(in);
		ObjectInputStream s = new ObjectInputStream(gin);

		int n = s.readInt();
		classCounts = new int[n];
		for (int i = 0; i < n; i++) {
			classCounts[i] = s.readInt();
		}
		n = s.readInt();
		classLinkCollection = new ArrayList[n];
		for (int i = 0; i < n; i++) {
			classLinkCollection[i] = (ArrayList) s.readObject();
		}
		s.close();

	}
	//static int totalseen =0;
	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doTraining(metdemo.Parser.EMTEmailMessage,
	 *      java.lang.String)
	 */
	public void doTraining(EMTEmailMessage msgData, String Label) {
		
		
		
		LinkInfoDS[] links = LinkExtractor.extractAllLinksFromData(msgData.getBody());
		
		//totalseen+=links.length;
		
		if (links.length == 0) {
			return;//nothing to do.
		}

		//BagOfLinks current_bag = new BagOfLinks(links);
		int n;
		if(lookupClassLabelHelper.containsKey(Label)){
			n = ((Integer)lookupClassLabelHelper.get(Label)).intValue();
		}
		else{
		n = MLearner.getClassNumber(Label);
		lookupClassLabelHelper.put(Label,new Integer(n));
		}
	//trying
		/*classCounts[n]++;
		for(int i=0;i<links.length;i++){
			
			if(links[i].getLink()==null){
				continue;
			}
			
		if(!seenURLS[n].containsKey(links[i].getLink())){
			seenURLS[n].put(links[i].getLink(),"");
			}
		}
		
		if(classCounts[n]%500==0){
			System.out.println("["+n+" "+seenURLS[n].size()+"]");
		}
		
		*/
		if (classCounts[n] == 0) {
			System.out.println("right here "+n);
			classCounts[n]++;
			classLinkCollection[n].add(new BagOfLinks(links));//current_bag);
			return;
		}
		classCounts[n]++;
		
		
		//BagOfLinks current_bag = new BagOfLinks(links);
		int min = 1000;
		int position = -1;
		for (int i = 0; i < classLinkCollection[n].size(); i++) {
			
			int dis = ((BagOfLinks) classLinkCollection[n].get(i)).getDistance(links);
			
			if (dis < min) {
				min = dis;
				position = i;
			}
			
			if(dis==0)//find first closest
				{
				break;
				}
		}

		if (position == -1 || min > MIN_DISTANCE_TO_INCLUDE) {
			classLinkCollection[n].add(new BagOfLinks(links));
			return;
		}
		
		((BagOfLinks) classLinkCollection[n].get(position)).addLink(links);//nt_bag);

	
		
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metdemo.MachineLearning.MLearner#doLabeling(metdemo.Parser.EMTEmailMessage)
	 */
	public resultScores doLabeling(EMTEmailMessage msgData) throws machineLearningException {
		//basic idea:
		//		get the links out of this msg and find the closest matching distance
		LinkInfoDS[] links = LinkExtractor.extractAllLinksFromData(msgData.getBody());
		if (links.length == 0) {
			return new resultScores(MLearner.INTERESTING_CLASS, 0, msgData.getMailref()); //nothing
																	// to do.
		}
		BagOfLinks bg = new BagOfLinks(links);



		int predict = -1;
		int mindistance = 10000;

		for (int ii = 0; ii < classCounts.length; ii++) {
			if (classCounts[ii] == 0) {
				continue;
			}

			for (int i = 0; i < classLinkCollection[ii].size(); i++) {
				int dis = ((BagOfLinks) classLinkCollection[ii].get(i)).getDistance(bg);
				if (dis < mindistance) {
					mindistance = dis;
					predict = ii;
				}
				if(mindistance <= 10)
				{break;}
			}
		}

		if (predict == -1) {
			return new resultScores(MLearner.NOT_INTERESTING_CLASS, 9, msgData.getMailref()); //nothing
																	// to do.

		}

		//System.out.print(predict +" m"+mindistance+" ");
		mindistance = 12+ mindistance/5;
		if(mindistance>100)
			mindistance = 100;
		else if(mindistance<0){
			mindistance =0;
		}
		
		return new resultScores(MLearner.getClassString(predict), mindistance, msgData.getMailref());


	}



    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
      return ML_LINKS;
    }

}