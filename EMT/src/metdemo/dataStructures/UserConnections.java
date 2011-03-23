package metdemo.dataStructures;

/**
 * @author Ryan Rowe July 12, 2006
 * Class used for describing a Clique connection between two users
 */

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.UndirectedEdge;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Vertex;

public class UserConnections extends HashMap<Connection,Edge>{
	public boolean containsKey(Connection c){
		Iterator iter = this.keySet().iterator();		
		while(iter.hasNext()){
			if(c.equals((Connection)iter.next())){
				return true;
			}
		}
		return false;
	}
}