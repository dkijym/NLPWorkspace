package metdemo.Finance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JTextField;

import metdemo.Tools.BusyWindow;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.DegreeDistributionRanker;
import edu.uci.ics.jung.algorithms.importance.HITS;
import edu.uci.ics.jung.algorithms.importance.NodeRanking;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.KPartiteGraph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.impl.KPartiteSparseGraph;
import edu.uci.ics.jung.graph.predicates.UserDatumVertexPredicate;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.statistics.GraphStatistics;
import edu.uci.ics.jung.utils.GraphUtils;

/**
 * 
 * @author G Creamer
 * 
 * Class to convert a text to graph text representation
 *
 */

public class pajekUtilities {

	
	public static HashMap<Integer, String> convertTextToPreGraph(BufferedReader fileNameNetwork_input, boolean isPAJEK, PrintWriter outFile2) throws IOException {
	
	
	
	HashMap<Integer, String> vertexLabel = new HashMap<Integer, String>();
		
		
		//i starts with 1 because it is the value of the first vertex.
		int i = 1, i1 = 1, i2 = 1;

		String[] line2, line3;
		SortedMap<String, Integer> m = new TreeMap<String, Integer>();
		SortedMap<String, Integer> m2 = new TreeMap<String, Integer>();
		SortedMap<String, Integer> m3 = new TreeMap<String, Integer>();

		//Saves edges to print at the end of the program:
		ArrayList<String> m4 = new ArrayList<String>();
		
		String line;
		while ((line = fileNameNetwork_input.readLine()) != null) {
			
			line2 = line.split("\t");
			//check for parse problems
			if(line2.length<2)
				continue;
			if(line2[0].length()<1 || line2[1].length()<1)
				continue;
			
			
			for (int j = 0; j < 2; j++) {
				
				if (!m.containsKey(line2[j])) {
					m.put(line2[j], i);
					i++;
				}

				//This is only to count number of vertices that corresponds to companies
				if (!m2.containsKey(line2[j]) && (j == 1)) {
					m2.put(line2[j], i2);
					i2++;
				}

			}

			m4.add(line);
		
		}

		int begin2ndGroup = m.size() - m2.size();

		Set<String> keySet = m.keySet();

		if (isPAJEK) {
			outFile2.println("*Vertices " + m.size() + ' ' + begin2ndGroup);
		}

		//to make things faster copied pasted code into if
		if (isPAJEK) {
			for (String key : keySet) {
				
				m3.put(key, i1);
				
				vertexLabel.put(i1, key);
				//if (isPAJEK) {
				outFile2.println(i1 + " \"" + key + "\"");
				//}
				i1++;
			}
			
			outFile2.println("*Edges ");
		}else{
			for (String key : keySet) {
				
				m3.put(key, i1);
				
				vertexLabel.put(i1, key);
				
				i1++;
			}
		}

		//Print the edges (Edges can be repeated. Jung will calculate the importance of each vertex according to this):
		Iterator i4 = m4.iterator();

		while (i4.hasNext()) {
			String nextPair = (String) i4.next();
			line3 = nextPair.split("\t");

			outFile2.println(m3.get(line3[0]) + " " + m3.get(line3[1]));
		}

		//System.out.println("Finish file conversion.");
		return vertexLabel;
	}
	
	 public final static String PARTITION = "emt:Finance";
	public final static UserDatumVertexPredicate PART_A = 
        new UserDatumVertexPredicate(PARTITION, "vertex_ID_A");
    public final static UserDatumVertexPredicate PART_B = 
        new UserDatumVertexPredicate(PARTITION, "vertex_ID_B");
    
	public static KPartiteGraph LoadBipartiteGraph(String filename) throws FileNotFoundException{
		 
		
		List predicates = new LinkedList();
	    predicates.add(PART_A);
	    predicates.add(PART_B);
	        
	    KPartiteGraph kbgraph = new KPartiteSparseGraph(predicates, true);
	    
	    BufferedReader breader = new BufferedReader(new FileReader(filename));
	    
	    String line = null;
	    //lets read through the file
	 /*   while()
	    
	        
	        
	        
	     return kbgraph;   */
	    
	    return null;
	}
	
	
	
	
	
	
	
	
	
	
	//private HashMap<Integer, String> vertexLabel; //stores vertex labels for bipartite lookup


	/**
	 * 
	 * @param fileNameNetwork_input
	 * @param isPAJEK Boolean variable PAJEK True creates a Pajek file. False creates a bipartite graph.
	 * @param outFile2
	 * @return
	 * @throws IOException
	 */	
	public static HashMap<Integer, String> convertTextToPreGraph(String fileNameNetwork_input, boolean isPAJEK, String outFile2, JTextField decidedName) throws IOException {
	
		//to spead up reading on large files (shlomo) added bufferedreader
		BufferedReader inputStream = new BufferedReader(new FileReader(fileNameNetwork_input));
	
		//Scanner inputStream = new Scanner(in);
		if(outFile2 == null ||  outFile2.length()<1){
			outFile2 = fileNameNetwork_input; //will be adding ending to be different next
		}
		
		int offset = outFile2.lastIndexOf(".txt");
		if(offset>0){
			outFile2 = outFile2.substring(0,offset);
		}else{
			outFile2 = outFile2;
		}
			

		//String outFile;
		if (isPAJEK) {
			outFile2 = outFile2 + ".net";
		} else {
			outFile2 = outFile2 + ".bp";
		}

		System.out.println("will be writing to : " + outFile2);
		decidedName.setText(outFile2);
		PrintWriter outputStream = new PrintWriter(outFile2);
		
		HashMap<Integer, String> vertexLabel = convertTextToPreGraph(inputStream, isPAJEK, outputStream);
		
		inputStream.close();
		outputStream.flush();
		outputStream.close();

		
		return vertexLabel;
		
	}

	/*public HashMap<Integer, String> getLabels() {
		return vertexLabel;
	}*/
	
	
	
	
	
	static public Object[] getAndBuildGraph(String fileName, String graphType, boolean maxWeakComponentBool)
			throws IOException {

		InputStream is = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		// String[] prefix;
		Graph graph1;
		StringLabeller tmplblr1;

		/*
		 * if(!kpartite.equalsIgnoreCase("PAJEK")) {prefix =
		 * fileNameNetwork.split(".bp");} else{ prefix =
		 * fileNameNetwork.split(".net"); }
		 */
		if (graphType.equals(Finance.labelBIPART)) {
			EMTBipartGraphMaker pnr = new EMTBipartGraphMaker();
			KPartiteGraph graph1a = pnr.load(br);
			tmplblr1 = StringLabeller.getLabeller(graph1a);
			graph1 = graph1a;
			
		} else if (graphType.equals(Finance.labelBIPART2UNI)) {
			EMTBipartGraphMaker pnr = new EMTBipartGraphMaker();
			KPartiteGraph bpg = pnr.load(br);
			FoldingTransformer kpf = new FoldingTransformer(false); // will not
																	// create
																	// parallel
																	// edges
			graph1 = kpf.fold(bpg, EMTBipartGraphMaker.PART_B);
			StringLabeller tmplblr = StringLabeller.getLabeller(bpg,
					EMTBipartGraphMaker.PART_B);
			tmplblr1 = StringLabeller.getLabeller(graph1);
			try {
				GraphUtils.copyLabels(tmplblr, tmplblr1);
			} catch (StringLabeller.UniqueLabelException e) {
				System.out.println(e);
			}
		} else { /* if(kpartite=="PAJEK"){ */
			//TODO: REPLACE ALL THISS TOO
			
			PajekNetReader pnr = new PajekNetReader(true, false);
			graph1 = pnr.load(br);
			tmplblr1 = StringLabeller.getLabeller(graph1, PajekNetReader.LABEL);

		}

		// This function transforms it into a weak component cluster.
		if (maxWeakComponentBool) {
			graph1 = graphUtilities.weakComponentClusterer(graph1, tmplblr1);
		}

		System.out.println("Complete getGraph");
		//lblr = tmplblr1;
		Object []returnthing = new Object[2];
		returnthing[0] = graph1;
		returnthing[1] = tmplblr1;
		
		return returnthing;//graph1;
	}
	
	/**
	 * 
	 * @param graph
	 * @param vertexLabel1
	 * @param lblr1
	 * @param resultsFile
	 * @param kpartite2
	 * @return
	 * @throws IOException
	 */
	public static boolean socNetworkStatistics(Graph graph,
			/*HashMap vertexLabel1,*/ StringLabeller lblr1, String resultsFile,
			String kpartite2) throws IOException {

		Double sumDegree = 0.0, sumRankDegreeScore = 0.0, sumRankerBC = 0.0, sumRankerHITS = 0.0, nVertex = 0.0, sumClusterCoeff = 0.0, sumAvgDist = 0.0;

		//Printing statistics:
		if (graph != null) {
			System.out.println("Graph is not null");
		} else {
			System.out.println("Graph is null");
		}
		
		
		BusyWindow BW = new BusyWindow("Graph Computation", "Working on Graph",
				false);
		BW.setVisible(true);
		PrintWriter outputStreamIndic = null;

		try {
			outputStreamIndic = new PrintWriter(new FileOutputStream(
					resultsFile)); //to append: ,true
		} catch (FileNotFoundException e) {
			String message1 = "Error opening file(s) for indicators: "
					+ resultsFile;
			System.out.println(message1);
			return false;
		}
		System.out
				.println("Writing indicators to file: datasets/socNetIndic.txt.");
		//For each vertex v in g, calculates the average shortest path length from v to all other vertices in g, ignoring edge weights:
		System.out.println("Calculating average distances.");
		Map mapAvgDist = GraphStatistics.averageDistances(graph);

		System.out.println("Calculating cluster coefficients");

		//Cluster coefficient:
		Map mapClusterCoeff = GraphStatistics.clusteringCoefficients(graph);

		//Calculates the "hubs-and-authorities" importance measures for each node in a graph. These measures are defined recursively as follows:
		//* The *hubness* of a node is the degree to which a node links to other important authorities
		//* The *authoritativeness* of a node is the degree to which a node is pointed to by important hubs
		System.out
				.println("Calculating the hubs-and-authorities importance measures for each node ");
		HITS rankerHITS = new HITS(graph);
		rankerHITS.setRemoveRankScoresOnFinalize(false);
		rankerHITS.evaluate();

		//Betweeness centrality:Computes betweenness centrality for each vertex and edge in the graph. The result is that each vertex and edge has a UserData element of type MutableDouble whose key is 'centrality.RelativeBetweennessCentrality' Note: Many social network researchers like to normalize the betweenness values by dividing the values by (n-1)(n-2)/2. The values given here are unnormalized.
		//Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177, 2001."
		System.out.println("Calculating betweenness centrality");
		BetweennessCentrality rankerBC = new BetweennessCentrality(graph);
		rankerBC.setRemoveRankScoresOnFinalize(false);
		rankerBC.evaluate();

		//Degree distribution:A simple node importance ranker based on the degree of the node. The user can specify whether s/he wants to use the indegree or the outdegree as the metric. If the graph is undirected this option is effectively ignored. So for example, if the graph is directed and the user chooses to use in-degree, nodes with the highest in-degree will be ranked highest and similarly nodes with the lowest in-degree will be ranked lowest.
		System.out.println("Calculating degree distribution ranker");
		DegreeDistributionRanker rankerDegreeDist = new DegreeDistributionRanker(
				graph);
		rankerDegreeDist.setRemoveRankScoresOnFinalize(false);
		rankerDegreeDist.evaluate();

		System.out.println("now here");
		/**
		 * Print the rankings to standard out in descending order of rank score:
		 * vertex label, degree, rank degree score, rank betweenness centrality score, rank "hubs-and-authorities" score, average distance, clustering coefficient
		 */
		outputStreamIndic
				.println("VertexName,degree,DegreeRankScore,BetweennessRankScore,HITSrank,AvgDistance,ClusteringCoefficient");

		//set boolean to true so the merging can be performed
		/*
		 * shlomo 
		 * 
		 * if (kpartite2.equals(Finance.labelBIPART2UNI)) {
			Finance.gotStats = true;
			Finance.companyStats = new HashMap();
		}*/
		//StringLabeller lblr=StringLabeller.getLabeller(graph, PajekNetReader.LABEL);
		Vertex v;
		//	System.out.println("Size of hash map:" + Finance.vertexLabel.size());

		for (Iterator it = rankerDegreeDist.getRankings().iterator(); it.hasNext();) {
			
			Ranking currentRanking = (Ranking) it.next();
			double rankDegreeScore = currentRanking.rankScore;

			v = ((NodeRanking) currentRanking).vertex;

			if (kpartite2.equals(Finance.labelBIPART)) {
				
				if (StringLabeller.hasStringLabeller(graph,
						EMTBipartGraphMaker.PART_A)) {

					System.out.println("graph has labeler of part a");
					
					lblr1 = StringLabeller.getLabeller(graph,
							EMTBipartGraphMaker.PART_A);
					
					if (lblr1 == null
							|| (lblr1.getLabel(v) == null && StringLabeller
									.hasStringLabeller(graph,
											EMTBipartGraphMaker.PART_B))) {
					
						lblr1 = StringLabeller.getLabeller(graph, EMTBipartGraphMaker.PART_B);
					}
					
					
					
				} else if (kpartite2.equals(Finance.labelBIPART2UNI)) {
					
					System.out.println("graph has labeler of part b");
					lblr1 = StringLabeller.getLabeller(graph,
							EMTBipartGraphMaker.PART_B);
					if (lblr1 == null
							|| (lblr1.getLabel(v) == null && StringLabeller
									.hasStringLabeller(graph,
											EMTBipartGraphMaker.PART_A))) {
						lblr1 = StringLabeller.getLabeller(graph,
								EMTBipartGraphMaker.PART_A);
					} else {
						System.out.println("Not valid option");
					}
				}

				// else  //if(kpartite=="PAJEK")
				//  {
				//   outputStreamIndic.println(lblr.getLabel(v) + "," + v.degree() + "," + rankDegreeScore + "," + rankerBC.getRankScore(v) + "," + rankerHITS.getRankScore(v) + "," + mapAvgDist.get(v) + "," + mapClusterCoeff.get(v));
				//  }
				/*try{
				String correctLabel = (String) (vertexLabel1.get(new Integer(
						lblr1.getLabel(v))));
				
					lblr1.setLabel(v, correctLabel);
				} catch (StringLabeller.UniqueLabelException e) {
					System.out.println(e);
				}catch(NumberFormatException nf){
					System.out.print("nf");
					//String correctLabel = lblr1.getLabel(v);
				}*/
			}
			
			outputStreamIndic.println(lblr1.getLabel(v) + "," + v.degree()
					+ "," + rankDegreeScore + "," + rankerBC.getRankScore(v)
					+ "," + rankerHITS.getRankScore(v) + ","
					+ mapAvgDist.get(v) + "," + mapClusterCoeff.get(v));

			sumDegree += v.degree();
			sumRankDegreeScore += rankDegreeScore;
			sumRankerBC += rankerBC.getRankScore(v);
			sumRankerHITS += rankerHITS.getRankScore(v);

			sumAvgDist += (Double) mapAvgDist.get(v);
			sumClusterCoeff += (Double) mapClusterCoeff.get(v);
			nVertex++;
		}
		double avgDegree = sumDegree / nVertex;
		double avgRankDegreeScore = sumRankDegreeScore / nVertex;
		double avgRankerBC = sumRankerBC / nVertex;
		double avgRankerHITS = sumRankerHITS / nVertex;
		double avgDist = sumAvgDist / nVertex;
		double avgClusterCoeff = sumClusterCoeff / nVertex;

		outputStreamIndic.println("\nAverages:," + avgDegree + ","
				+ avgRankDegreeScore + "," + avgRankerBC + "," + avgRankerHITS
				+ "," + avgDist + "," + avgClusterCoeff);

		double Lrandom = Math.log(nVertex) / Math.log(avgDegree);
		double Crandom = avgDegree / nVertex;
		double SWindex = avgClusterCoeff / avgDist * (Lrandom / Crandom);
		outputStreamIndic.println("SW index:" + SWindex);

		/*set the appropriate graphloaded button on to inform the user which type of graph has been loaded
		if (kpartite2.equals(Finance.labelPAJEK)) {
			graphLoaded[0].setVisible(false);
			graphLoaded[2].setVisible(false);
			graphLoaded[3].setVisible(false);
			graphLoaded[4].setVisible(false);
			graphLoaded[1].setVisible(true);
		} else if (kpartite2.equals(Finance.labelBIPART)) {
			graphLoaded[0].setVisible(false);
			graphLoaded[1].setVisible(false);
			graphLoaded[3].setVisible(false);
			graphLoaded[4].setVisible(false);
			graphLoaded[2].setVisible(true);
		} else if (kpartite2.equals(Finance.labelBIPART2UNI)) {
			graphLoaded[0].setVisible(false);
			graphLoaded[2].setVisible(false);
			graphLoaded[1].setVisible(false);
			graphLoaded[4].setVisible(false);
			graphLoaded[3].setVisible(true);
		}*/
		outputStreamIndic.close();

		//visualizeResults(resultsFile);
		System.out.println("Complete social network statistics");
		BW.setVisible(false);
		return true;
	}
	
}
