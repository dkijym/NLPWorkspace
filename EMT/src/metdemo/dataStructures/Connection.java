package metdemo.dataStructures;

/**
 * @author Ryan Rowe July 12, 2006
 * Class used for describing a Clique connection between two users
 */

import java.util.AbstractMap;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.UndirectedEdge;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Vertex;

public class Connection{
	private String acct1;
	private String acct2;
	
	public Connection(String a1, String a2){
		acct1 = a1;
		acct2 = a2;
	}
	
	public boolean equals(Connection c){
		if(this.acct1.equals(c.acct1) && this.acct2.equals(c.acct2)){
			return true;
		} else if(this.acct1.equals(c.acct2) && this.acct2.equals(c.acct1)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean compare(Connection a, Connection b){
		if(a.acct1.equals(b.acct1) && a.acct2.equals(b.acct2)){
			return true;
		} else if(a.acct1.equals(b.acct2) && a.acct2.equals(b.acct1)) {
			return true;
		} else {
			return false;
		}
	}
}
