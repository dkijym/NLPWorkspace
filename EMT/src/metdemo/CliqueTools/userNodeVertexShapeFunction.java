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
public class userNodeVertexShapeFunction extends AbstractVertexShapeFunction
		implements VertexSizeFunction {

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.graph.decorators.VertexSizeFunction#getSize(edu.uci.ics.jung.graph.Vertex)
	 */
	public int getSize(Vertex arg0) {
		if (arg0 instanceof cliqueVertex) {
			return 15;
		} 
			return 10;
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
}
