package nlp.Old;

import org.apache.commons.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JFrame;
import java.io.*;


import java.util.*;
import edu.uci.ics.jung.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.decorators.*;
import java.awt.*;

import AceJet.*;
import Jet.Tipster.ExternalDocument;


//TODO Create xml importer

public class TestAnnotationtool {
		
	public interface NERengine
	{
		public AceDocument process(Object o);
	}
	
	public AceDocument recongizeEnties(Object o)
	{
		return null;
	}
	/**
	 * 
	 * @param NER
	 */
	static void runNER(Person p)
	{
		/*for(MimeMessage mm: p.emails)
		{
	//		recongizeEnties(mm.getContent());
		}
		*/
	}
	
	
	void initJet()
	{
		//doc = new ExternalDocument("sgml", docDir + currentDocPath + ".sgm");
	}
	/**
	 * This will run and create
	 * @param args
	 */
	public static void main(String args[]) {
		
		// Get all threads into Person graph
		// Run NER on person graph
		// Run Pat on person graph
		
		
		
		
		
		
		  
		  //System.out.println(message.getSubject()); //Email em=
		 

		// Pseudo-code
		// Create nodes 1...n with the following information
		// Name, ListOfEmails(with meta data)
		// Edges between people with number of emails sent back and forth
		//
		/*
		Graph<Person, CustomEdge> socialGraph = new SparseGraph<Person, CustomEdge>();
		    
		for(Person p: us)
		{
			socialGraph.addVertex(p);
		}
		
		socialGraph.addEdge(new CustomEdge("Hey"),
							new Person("reuben.doetsch@gmail.com"), new Person(
									"eliebleier@gmail.com"));

		showGraph(socialGraph);
		*/
		Jet.JetTest.initializeFromConfig("C:/Users/reuben/NLPResearch/jetApoorv/propsMEace06.properties","C:/Users/reuben/NLPResearch/jetApoorv/");
		Jet.Console c = new Jet.Console();
		
	}
	
	/**
	 * This shows a social graph
	 * @param g the graph to show
	 */
	private static void showGraph(Graph g) {
	//	try
		//{
	
		// The Layout<V, E> is parameterized by the vertex and edge types
//		Layout<Person, CustomEdge> layout = new CircleLayout<Person, CustomEdge>(
	//			g);
	//	layout.setSize(new Dimension(300, 300)); // sets the initial size of the
													// space
		// The BasicVisualizationServer<V,E> is parameterized by the edge types
	//	BasicVisualizationServer<Person, CustomEdge> vv = new BasicVisualizationServer<Person, CustomEdge>(
//				layout);
		/*vv.setPreferredSize(new Dimension(350, 350)); // Sets the viewing area
														// size
		  vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		} catch(Error e)
		{
			System.out.println(e.getMessage());
		}
		*/
//	}
	}
}
