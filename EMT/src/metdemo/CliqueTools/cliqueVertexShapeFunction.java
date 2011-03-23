/**
 * 
 */
package metdemo.CliqueTools;

import java.awt.Shape;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;

/**
 * @author shlomo
 *
 */
public class cliqueVertexShapeFunction extends AbstractVertexShapeFunction  implements VertexSizeFunction/*, VertexAspectRatioFunction*/{

	
	public cliqueVertexShapeFunction() {
		// TODO Auto-generated constructor stub
	
		setSizeFunction(this);
		//setAspectRatioFunction(this);
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.graph.decorators.VertexShapeFunction#getShape(edu.uci.ics.jung.graph.Vertex)
	 */
	public Shape getShape(Vertex arg0) {
		if (arg0 instanceof cliqueVertex) {
			
			int size = ((cliqueVertex)arg0).getSize();
			if (size > 2 && size < 6) {

				return factory.getRegularPolygon(arg0, size);
			}else if(size >5){
				return factory.getRegularStar(arg0, size);
			} 
		}
		//default
		return factory.getEllipse(arg0);
	}

	public int getSize(Vertex arg0) {
		if (arg0 instanceof cliqueVertex) {
			int edges = ((cliqueVertex) arg0).getOutEdges().size();
			edges = edges > 15 ? 15 : edges; //if greater than 15 leave at 15
			return 15+1 * edges;
		} else
		
			return 10;

	}


	

}
