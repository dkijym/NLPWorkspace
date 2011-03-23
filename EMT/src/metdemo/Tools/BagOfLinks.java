/*
 * Created on Jan 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tools;

//import java.io.IOException;
//import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import metdemo.dataStructures.LinkInfoDS;

/**
 * @author Shlomo Hershkop
 * 
 * Class to handle bunch of link
 */
public class BagOfLinks /*implements Serializable*/ {

	static final int MAX_DISTANCE = 1000;
	static final int MIN_DISTANCE = 0;
	HashMap bagOLinks;



	public BagOfLinks() {
		bagOLinks = new HashMap();
	}

	public BagOfLinks(LinkInfoDS links[]) {
		this();
		addLink(links);
	}

	public final HashMap getBag() {
		return bagOLinks;
	}
/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(bagOLinks);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		bagOLinks = (HashMap) in.readObject();
	}
*/
		/**
	 * Adds a link to the current collection of links
	 * 
	 * @param singleLink
	 */
	public final void addLink(LinkInfoDS singleLink) {
		if (bagOLinks.containsKey(singleLink.getLink())) {
			//????LinkInfoDS current = (LinkInfoDS) bagOLinks.get(singleLink.getLink());
			//????current.addToCurrentLink(singleLink);
			//????bagOLinks.put(singleLink.getLink(), current);

		} else {
			bagOLinks.put(singleLink.getLink(), singleLink);
			//System.out.print(bagOLinks.size()+ " ");
		}
	}

	/**
	 * Add an array of links to the current collection of links Just calls
	 * add(single) for each one
	 * 
	 * @param linkCollection
	 */
	public final void addLink(LinkInfoDS linkCollection[]) {
		for (int i = 0; i < linkCollection.length; i++) {
			addLink(linkCollection[i]);
		}
	}

	/**
	 * Merge two bags
	 * 
	 * @param current
	 */
	public final void addLink(BagOfLinks current) {
		HashMap linksMap = current.getBag();

		for (Iterator vals = linksMap.keySet().iterator(); vals.hasNext();) {
			addLink((LinkInfoDS) linksMap.get(vals.next()));
		}
	}


	public final int getDistance(LinkInfoDS []lists) {
		

		int total = 0;
		int num = lists.length;
		
		if(num ==0){
			return 0;
		}
		
		for (int i=0;i<num;i++) {

			total += getDistance(lists[i]);

		}
		return total / num;
	}
	
	
	public final int getDistance(BagOfLinks bagcompare) {
		HashMap linksMap = bagcompare.getBag();

		int total = 0;
		int num = linksMap.size();
		
		if(num ==0){
			return 0;
		}
		
		for (Iterator vals = linksMap.keySet().iterator(); vals.hasNext();) {

			total += getDistance((LinkInfoDS) linksMap.get(vals.next()));

		}
		return total / num;
	}

	public final int getDistance(LinkInfoDS singleLink) {
		//return 0 if we have same
		if (bagOLinks.containsKey(singleLink.getLink())) {
			return 0;
		}
		else if (bagOLinks.size() == 0) {
			return MIN_DISTANCE; //?/?
		}
		//distance as a few components: 0-1000

		//if the types are diffent, add them together then * 4;

		//get quick edit distance (number of spots different) from each other
		//if totally different should end up adding 500 to score

		//measure how close the labels are to each other
		//if totally different should end up adding 400 to score
		int type = singleLink.getType();
		String url = singleLink.getLink();


		
		int total = 0;
		//for each link
		for (Iterator linkiter = bagOLinks.keySet().iterator(); linkiter.hasNext();) {
			LinkInfoDS current = (LinkInfoDS) bagOLinks.get(linkiter.next());

			if (current.getType() != type) {
				total += 4 * (current.getType() + type);
			}

			total += LinkExtractor.urlDistance(url, current.getLink());

			//TODO: ngram label comparison
		}
//		System.out.println("pretotoal:"+total +" size:"+bagOLinks.size());
		return total / (bagOLinks.size());

	}





}