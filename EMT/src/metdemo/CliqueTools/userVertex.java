/**
 * 
 */
package metdemo.CliqueTools;

import edu.uci.ics.jung.graph.impl.SparseVertex;

/**
 * @author shlomo
 *
 */
public class userVertex extends SparseVertex {

	String label;
	
	
	userVertex(String username) {
		super();
		label = username;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return label;
	}
	
}
