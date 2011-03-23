/**
 * @(#)OneFlow.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/23
 */

package metdemo.Docflow;

import java.util.ArrayList;

public class OneFlow implements Comparable{

	String subject;
	ArrayList<DocflowNode> flow;
	double timeratio;//be used to decide where to locate it...
	
    public OneFlow(String s, DocflowNode node, double time){
    	this.subject = s;
    	this.timeratio = time;
    	this.flow = new ArrayList<DocflowNode>();
    	this.flow.add(node);
    }

	/**
	 *	get the y location of this node on the panel
	 */
	public int getLoc(int base, double size){
		long loc = Math.round(this.timeratio*size);
		if(loc > 30)loc -= 30;
		return base + (int)loc;
	}
	
	public int compareTo(Object another) throws ClassCastException {
    	if (!(another instanceof OneFlow))
      		throw new ClassCastException("Object doesn't match!");

    	double value = this.timeratio - ((OneFlow)another).timeratio;
    	if(value == 0)return 0;
    	else if(value > 0)return 1;
    	else return -1;
    }
}