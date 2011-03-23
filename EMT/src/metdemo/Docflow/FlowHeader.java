/**
 * @(#)FlowHeader.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/24
 */

package metdemo.Docflow;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

/**
 *	the header info. for an emailflow
 */
public class FlowHeader extends DirectedSparseVertex{
	
	//I don't use private variables here, just for convenience
	String subject = null;
	
	//the header of an emailflow or just the timeline
	boolean isheader = true;
	
	
	public FlowHeader(){
		isheader = false;
		subject = "";
	}
	
	public FlowHeader(String s){
		subject = s;
	}
	
	@Override
	public String toString() {
		return subject;
	}
}