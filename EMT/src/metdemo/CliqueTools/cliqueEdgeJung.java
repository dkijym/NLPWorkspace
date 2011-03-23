/**
 * 
 */
package metdemo.CliqueTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;

/**
 * @author shlomo
 * NOTE: Assuming vertex members don't change over time
 */
public class cliqueEdgeJung extends UndirectedSparseEdge {

	private Color myColor;
	
	final static Color colorList[] = {Color.orange,Color.yellow,Color.red,Color.cyan,Color.blue,
		Color.magenta,Color.green,Color.darkGray,Color.pink,Color.black,Color.black};
	
	public cliqueEdgeJung(Vertex v1, Vertex v2) {
		super(v1,v2);
		if(v2.getClass() == cliqueVertex.class){
			myColor = calculateOverlap((cliqueVertex)v1,(cliqueVertex)v2);
		}else{
			myColor = colorList[10-v2.inDegree()%10];
		}
	}
	
	/**
	 * Return current edge's color
	 * @return
	 */
	public Color GetColor(){
		return myColor;
	}
	
	
	static public Color calculateOverlap(cliqueVertex v1,cliqueVertex v2){
	
		//will see how many members overlap and decide our color
	

		int totalMembers = v1.getSize() + v2.getSize();
		
		if(totalMembers < 2){
			return colorList[0];
		}
		
		int overlap = 0 ;
		HashMap<String,Integer> countingSeenUsersv1 = new HashMap<String, Integer>(v1.getSize());
		HashMap<String,Integer> countingSeenUsersv2 = new HashMap<String, Integer>(v2.getSize());
		//lets do the first simply dump the names (incase duplicates
		ArrayList<String> useremails = v1.getUserList();
		for(int num =0;num<useremails.size();num++){
			countingSeenUsersv1.put(useremails.get(num), 1);
		}
		//now for second guy
		useremails = v2.getUserList();
		for(int num =0;num<useremails.size();num++){
			countingSeenUsersv2.put(useremails.get(num), 1);
		}
		
		
		//set through and add anyone with count>1
		for(Iterator<String> seennames = countingSeenUsersv1.keySet().iterator();seennames.hasNext();){
			
			String aName = seennames.next();
			if(countingSeenUsersv2.containsKey(aName)){
				overlap++;
			}
		}	
		//lets do the colors
		/*
		 * Color	Percentage
Orange	<10%
Yellow	> 10%, <= 20%
Red	> 20%, <= 30%
Cyan	> 30%, <= 40%
Blue	> 40%, <= 50%
Magenta	> 50%, <= 60%
Green	> 60%, <= 70%
Gray	> 70%, <= 80%
Pink	> 80%, <=90%
Black	> 90%

		 */
		
		int percentage = (int)((10*overlap / totalMembers) );
		
		return colorList[(int)percentage];
		
	}
	
	
	
	
	
	
	
	
}
