/**
 * 
 */
package metdemo.Finance;

import java.io.IOException;

import edu.uci.ics.jung.algorithms.cluster.ClusterSet;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.utils.GraphUtils;

/**
 * @author shlomo
 *
 */
public abstract class graphUtilities {
	
	
	
	public static Graph weakComponentClusterer(Graph graph1,StringLabeller tmplblr1) throws IOException {
		
		WeakComponentClusterer wcc = new WeakComponentClusterer();
		
		ClusterSet componentList =wcc.extract(graph1);
		componentList.sort();
		
		Graph graphweak = componentList.getClusterAsNewSubGraph(0);
		
		StringLabeller lblr2 = StringLabeller.getLabeller(graphweak);
		
		try{
						// copy tmplblr1 to lblr
	            		GraphUtils.copyLabels(tmplblr1, lblr2);
	           	}
	            	catch(StringLabeller.UniqueLabelException e){
	            			System.out.println(e);
	            	}

					System.out.println("Complete max weak component cluster");
					return graphweak;
			
	}

}
