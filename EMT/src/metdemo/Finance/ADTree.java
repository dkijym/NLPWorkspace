package metdemo.Finance;

//import junit.framework.TestCase;
import java.awt.Container;
import java.io.IOException;

import javax.swing.JFrame;
import java.io.*;
import java.util.Vector;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseTree;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 *
 * @author 
 */
public class ADTree {

	public void setUp () throws IOException {
	SparseTree st =	createADTree();
	VisualizationViewer vv = new VisualizationViewer(new SpringLayout(st), new PluggableRenderer());
    JFrame jf = new JFrame();
    jf.getContentPane().add(vv);
//	jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	jf.pack();
	jf.setVisible(true);
}
	
    public SparseTree createADTree() {
        Vertex root = new SparseVertex();
        Vertex l = new SparseVertex();
        Vertex r = new SparseVertex();
        SparseTree st = new SparseTree( root );
        st.addVertex(l);
        st.addVertex(r);
//        assertSame( st.getRoot(), root );
        st.addEdge(new DirectedSparseEdge(root, l ));
        st.addEdge(new DirectedSparseEdge(root, r ));
        
        Vertex v1 = st.addVertex( new SparseVertex());
        Vertex v2 = st.addVertex( new SparseVertex());
        try {
            st.addEdge( new DirectedSparseEdge( l, v1 ));
            st.addEdge( new DirectedSparseEdge( l, v2 ));
     
        } catch (Exception e ) {
        	System.err.println("Nasty islands!");   }
        
        return st;
    }}
