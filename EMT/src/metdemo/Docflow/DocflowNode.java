/**
 * @(#)DocflowNode.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/23
 */
package metdemo.Docflow;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import java.util.ArrayList;


public class DocflowNode extends DirectedSparseVertex{
	
	//I don't use private variables here, just for convenience
	String sender = null;
	String date = null;
	String time = null;
	String filename = null;
	String mailref = null;
	double timeratio;//be used to decide where to locate it... for the plot
	int numattach = 0;
	
	ArrayList<String> rcpt;//a list of recepients
	
	public DocflowNode(){}
	
	public DocflowNode(final String s, final String r
						, final String d, final String t
						, final String ref//, final String att
						, final double tr){
		this.sender = s;
		this.date = d;
		this.time = t;
		this.mailref = ref;
		//this.numattach = Integer.parseInt(att);
		this.timeratio = tr;
			
		this.rcpt = new ArrayList<String>();
		rcpt.add(r);
	}
	
	/**
	 *	get the y location of this node on the panel
	 */
	public int getLoc(int base, double size){
		long loc = Math.round(this.timeratio*size);
		return base + (int)loc;
	}
	
	public void setNumatt(int att){
		numattach = att;
	}
	
	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer(sender);
		String space = ", ";
		msg.append(space);
		msg.append(date);
		msg.append(space);
		msg.append(time);
		if(filename != null){
			msg.append(space+" att:");
			msg.append(filename);
		}
		return msg.toString();

	}
}