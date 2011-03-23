/**
 * 
 */
package metdemo.Finance;

import metdemo.dataStructures.vipUser;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;

/**
 * @author shlomo
 * written to optimize shnetwork - ryans stuf
 *
 */
public class VIPVertex extends UndirectedSparseVertex {
    
	
	vipUser insideData;
	int id;
	
	VIPVertex(int number,vipUser vips) {
		super();
		insideData = vips;
		id=number;
	}
	
	
	
	public int getID(){
		return id;
	}
	/**
	 * 
	 * @return null if not set else the acct name
	 */
	public String getAcct(){
		return insideData == null ? null : insideData.getAcct();
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return insideData.getAcct();
	}
	
	public vipUser getVIPInside(){
		return insideData;
	}
	
}
