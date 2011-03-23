/*
 * Copyright (c) 2004, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 * Created on Nov 7, 2004
 * 
 * Adapted by German Creamer, October 2005
 * copied and adopted by ryan summer 2006
 * further adopted by shlomo 2006-2007
 */
package metdemo.Finance;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import metdemo.Tables.SortTableModel;
import metdemo.Tables.md5CellRenderer;
import metdemo.dataStructures.sparseIntMatrix;
import metdemo.dataStructures.vipUser;
import metdemo.dataStructures.CliqueEdge;

import java.io.IOException;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.algorithms.importance.PageRank;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStringer;
import edu.uci.ics.jung.graph.decorators.ConstantVertexStringer;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgeFontFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.GradientEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.NumberVertexValue;
import edu.uci.ics.jung.graph.decorators.NumberVertexValueStringer;
import edu.uci.ics.jung.graph.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberVertexValue;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexFontFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.decorators.VertexStrokeFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.predicates.SelfLoopEdgePredicate;
import edu.uci.ics.jung.random.generators.BarabasiAlbertGenerator;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.HasGraphLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickedInfo;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.transform.LayoutTransformer;
import edu.uci.ics.jung.visualization.transform.Transformer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

/**
 * @author Danyel Fisher, Joshua O'Madadhain modified by Tom Nelson for zoom/pan
 *         with the mouse wheel and mouse modified by Ryan Rowe for Social
 *         Hierarchy visualization
 */
public class SHNetworks extends JFrame {
	protected JCheckBox v_color;
	protected JCheckBox e_color;
	protected JCheckBox v_stroke;
	protected JCheckBox e_uarrow_pred;
	protected JCheckBox e_darrow_pred;
	protected JCheckBox v_shape;
	protected JCheckBox v_size;
	protected JCheckBox v_aspect;
	protected JCheckBox v_labels;
	protected JRadioButton e_line;
	protected JRadioButton e_bent;
	protected JRadioButton e_wedge;
	protected JRadioButton e_quad;
	protected JRadioButton e_cubic;
	protected JCheckBox e_labels;
	protected JCheckBox font;
	protected JCheckBox e_show_d;
	protected JCheckBox e_show_u;
	protected JCheckBox v_small;
	protected JCheckBox zoom_at_mouse;
	protected JCheckBox fill_edges;
	protected JComboBox layoutTypeBox;
	protected JComboBox graphTypeBox;
	protected JTable m_timeTable;
	private JScrollPane tablePane;

	protected JRadioButton no_gradient;
	protected JRadioButton gradient_relative;

	protected static final int GRADIENT_NONE = 0;
	protected static final int GRADIENT_RELATIVE = 1;
	protected static int gradient_level = GRADIENT_RELATIVE;

	protected static final int STRAIGHT_SCALE = 0;
	protected static final int WEIGHTED_SCALE = 1;
	protected static final int ROWE_SCALE = 2;

	protected PluggableRenderer pr;
	protected SeedColor vcf;
	protected EdgeWeightStrokeFunction ewcs;
	protected VertexStrokeHighlight vsh;
	protected VertexStringer vs;
	protected VertexStringer vs_none;
	protected EdgeStringer es;
	protected EdgeStringer es_none;
	protected FontHandler ff;
	protected VertexShapeSizeAspect vssa;
	protected DirectionDisplayPredicate show_edge;
	protected DirectionDisplayPredicate show_arrow;
	protected VertexDisplayPredicate show_vertex;
	protected GradientPickedEdgePaintFunction edgePaint;

	protected final static String VOLTAGE_KEY = "voltages";
	protected final static String TRANSPARENCY = "transparency";
	protected final static String INDEX = "index";
	protected final static String DGRAPH_EDGE_WEIGHT_KEY = "dgraphEdgeWeight";
	protected final static String DGRAPH_INDEX = "dgraphIndex";

	protected NumberEdgeValue dgraphEdgeWeight = new UserDatumNumberEdgeValue(
			DGRAPH_EDGE_WEIGHT_KEY);
	protected NumberVertexValue voltages = new UserDatumNumberVertexValue(
			VOLTAGE_KEY);
	protected NumberVertexValue transparency = new UserDatumNumberVertexValue(
			TRANSPARENCY);
	// protected NumberVertexValue index = new
	// UserDatumNumberVertexValue(INDEX);
	// protected NumberVertexValue dgraphIndex = new
	// UserDatumNumberVertexValue(DGRAPH_INDEX);
	
	protected VisualizationViewer m_visualizationview;
	protected DefaultModalGraphMouse m_graphmouse;
	protected Transformer affineTransformer;
	private Layout m_layout;

	// private HashMap<String,vipUser> viphm_hashmap;
	// private ArrayList<String> accounts_arraylist;
	private HashMap<String, VIPVertex> vertices_hashmap;
	public Graph m_graph;
	// private int num_candidates = 0;
	private md5CellRenderer md5Renderer;
	
	// shlomo added: to allow quick find of specific graph
	JComboBox usercombolist;
	JCheckBox m_showLabels;
	
	
	/**
	 * Constructor with md5 cell renderer
	 * 
	 * @param md5r
	 */
	public SHNetworks(final md5CellRenderer md5r) {
		md5Renderer = md5r;
	}

	/**
	 * 
	 * @param fileNameNetwork
	 * @param hmgraph
	 * @param dgraph
	 * @param minEdgeWeight
	 * @param vips
	 * @param allAcctList
	 * @throws IOException
	 */
	public void startFunction(String fileNameNetwork,
			HashMap<String, HashMap> hmgraph, 
			int minEdgeWeight, HashMap<String, vipUser> vips) throws IOException {
		// allAccts = allAcctList;
		// viphm_hashmap = vips;
		m_graph = getGraph(fileNameNetwork, hmgraph, minEdgeWeight, vips);
		// dg = getDGraph(dgraph, minEdgeWeight);
		// calcPageRank(dg);

		pr = new PluggableRenderer();

		// create the OrgChart like "tree" layout
		m_layout = new FRLayout(m_graph);
		m_visualizationview = new VisualizationViewer(m_layout, pr);
		moveVertices();

		// add Shape based pick support
		m_visualizationview.setPickSupport(new ShapePickSupport());
		PickedState picked_state = m_visualizationview.getPickedState();
		picked_state.addItemListener(new PickedListItemListener());

		affineTransformer = m_visualizationview.getLayoutTransformer();

		// create decorators
		vcf = new SeedColor(picked_state);
		ewcs = new EdgeWeightStrokeFunction(dgraphEdgeWeight);
		vsh = new VertexStrokeHighlight(picked_state);
		vsh.setHighlight(true);
		ff = new FontHandler();
		vs_none = new ConstantVertexStringer(null);
		es_none = new ConstantEdgeStringer(null);
		vssa = new VertexShapeSizeAspect(voltages);
		show_edge = new DirectionDisplayPredicate(true, true);
		show_arrow = new DirectionDisplayPredicate(true, false);
		show_vertex = new VertexDisplayPredicate(false);

		// uses a gradient edge if unpicked, otherwise uses picked selection
		edgePaint = new GradientPickedEdgePaintFunction(
				new PickableEdgePaintFunction(picked_state, Color.black,
						Color.cyan), m_visualizationview, m_visualizationview,
				picked_state);

		pr.setVertexPaintFunction(vcf);
		pr.setVertexStrokeFunction(vsh);
		//pr.setVertexStringer(vs_none);
		pr.setVertexStringer(new VertexStringer() {

            public String getLabel(ArchetypeVertex v) {
               
            	//can check if global show label is checked off
            	if(m_showLabels.isSelected())
            	{
            		return v.toString();
            	}
            	
            	//want to only draw the string if we are in pick state
            	else if(m_visualizationview.getPickedState().isPicked(v)){
            		return v.toString();
				}
            	return "";
            }

			});

		
		
		
		// pr.setVertexFontFunction(ff);
		pr.setVertexShapeFunction(vssa);
		pr.setVertexIncludePredicate(show_vertex);

		pr.setEdgePaintFunction(edgePaint);
		pr.setEdgeStringer(es_none);
		// pr.setEdgeFontFunction(ff);
		pr.setEdgeStrokeFunction(ewcs);
		pr.setEdgeIncludePredicate(show_edge);
		pr.setEdgeShapeFunction(new EdgeShape.Line());
		pr.setEdgeArrowPredicate(show_arrow);
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());

		m_visualizationview.setBackground(Color.white);
		// Uncomment the following four lines, and comment the line
		// immediately following, to enable panning and zooming support
		// (including dragging of window to pan, and zooming with
		// mouse's scroll wheel).
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(
				m_visualizationview);
		jp.add(scrollPane);
		m_graphmouse = new DefaultModalGraphMouse();
		m_visualizationview.setGraphMouse(m_graphmouse);

		addBottomControls(jp, vips.keySet().toArray(
				new String[vips.keySet().size()]));
		vssa.setScaling(true);

		m_visualizationview.setToolTipFunction(new VoltageTips());
		// vv.setToolTipText("<html><center>Use the mouse wheel to zoom<p>Click
		// and Drag the mouse to pan<p>Shift-click and Drag to
		// Rotate</center></html>");

		JFrame jf = new JFrame();
		jf.getContentPane().add(jp);
		jf.pack();
		jf.setVisible(true);
	}

	/**
	 * 
	 * @param dgraph
	 * @param minEdgeWeight
	 * @param vips
	 * @param allUsers
	 * @return
	 */

	public static HashMap<String, Double> getPageRanking(
			sparseIntMatrix dgraph, int minEdgeWeight,
			HashMap<String, vipUser> vips, String[] allUsers) {
		// allAccts = allUsers;
		// viphm_hashmap = vips;
		/*
		 * Object []catcher = getDGraph(dgraph, minEdgeWeight,vips);
		 */
		DirectedGraph dg = getDGraph(dgraph, minEdgeWeight, vips);

		// m_directedgraph = (DirectedGraph)catcher[0];
		// Vertex[] dVerts = (Vertex[])catcher[1];
		return calcPageRank(dg, allUsers, vips);
	}

	/**
	 * This method does..
	 *
	 */
	protected void moveVertices() {

		// first, organize the vertices by SocialScore Level (5 different
		// stages)
		HashMap<Integer, ArrayList<VIPVertex>> levels = stepDivide(
				SHNetworks.STRAIGHT_SCALE);

		// next, place the vertices according to their echelon
		Iterator levelIter = levels.keySet().iterator();
		while (levelIter.hasNext()) {
			// first, get critical objects/information for the current level
			Integer level = (Integer) levelIter.next();
			ArrayList<VIPVertex> verts = levels.get(level);
			int numVerts = verts.size();

			// next, get the dimension attributes of the current window
			Dimension winSize = m_layout.getCurrentSize();
			double winHeight = winSize.getHeight();
			double winWidth = winSize.getWidth();

			// then, place each of the vertices according window/level size
			double curY = (winHeight / 6) * level + 20;
			double xInc = winWidth / (numVerts + 1);
			double curX = xInc;
			Iterator vertIter = verts.iterator();
			while (vertIter.hasNext()) {
				VIPVertex vert = (VIPVertex) vertIter.next();
				m_layout.forceMove(vert, curX, curY);
				curX += xInc;
			}
		}
	}

/**
 * 
 * @param divideType
 * @return
 */
	
	protected HashMap<Integer, ArrayList<VIPVertex>> stepDivide(
			final int divideType) {
		// first, initialize the HashMap<Integer,Vector> levels
		HashMap<Integer, ArrayList<VIPVertex>> levels = new HashMap<Integer, ArrayList<VIPVertex>>();
		for (int i = 1; i < 6; i++) {
			levels.put(i, new ArrayList<VIPVertex>());
		}

		// next, assign each vertex to a level using the specified divideType
		Iterator iter = vertices_hashmap.keySet().iterator();
		while (iter.hasNext()) {
			String acct = (String) iter.next();
			VIPVertex vert = vertices_hashmap.get(acct);
			vipUser vip = vert.insideData;// viphm_hashmap.get(acct);

			/*
			 * STRAIGHT_SCALE: Assigns employees to different social levels
			 * using a straight scale, dividing everyone into 5 groups with
			 * Social Scores 80-100, 60-79, 40-59, 20-39 and 0-19.
			 */
			if (divideType == SHNetworks.STRAIGHT_SCALE) {
				if (vip.getSocialScore() >= 80.0) {
					levels.get(1).add(vert);
				} else if (vip.getSocialScore() >= 60.0) {
					levels.get(2).add(vert);
				} else if (vip.getSocialScore() >= 40.0) {
					levels.get(3).add(vert);
				} else if (vip.getSocialScore() >= 20.0) {
					levels.get(4).add(vert);
				} else {
					levels.get(5).add(vert);
				}
			}
		}

		// finally, return the HashMap<Integer,Vector> levels
		return levels;
	}

	/**
	 * 
	 * @param jp
	 * @param usersarray
	 * @param viphm_hashmap
	 */
	protected void addBottomControls(final JPanel jp, String[] usersarray) {
		// create the control panel which will hold settings and picked list
		final JPanel control_panel = new JPanel();
		jp.add(control_panel, BorderLayout.SOUTH);
		control_panel.setLayout(new BorderLayout());

		// create the settings panel which will hold all of the settings
		Box settings_box = Box.createVerticalBox();
		settings_box.setBorder(BorderFactory
				.createTitledBorder("Display Settings"));
		control_panel.add(settings_box, BorderLayout.NORTH);
		JPanel settings_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,
				10));

		// add the zoom controls to the settings panel
		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// call listener in GraphMouse instead of manipulating vv scale
				// directly
				// this is so the crossover from zoom to scale works with the
				// buttons
				// as well as with the mouse wheel
				Dimension d = m_visualizationview.getSize();
				m_graphmouse.mouseWheelMoved(new MouseWheelEvent(
						m_visualizationview, MouseEvent.MOUSE_WHEEL, System
								.currentTimeMillis(), 0, d.width / 2,
						d.height / 2, 1, false,
						MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1));
			}
		});
		JButton minus = new JButton(" - ");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// call listener in GraphMouse instead of manipulating vv scale
				// directly
				// this is so the crossover from zoom to scale works with the
				// buttons
				// as well as with the mouse wheel
				Dimension d = m_visualizationview.getSize();
				m_graphmouse.mouseWheelMoved(new MouseWheelEvent(
						m_visualizationview, MouseEvent.MOUSE_WHEEL, System
								.currentTimeMillis(), 0, d.width / 2,
						d.height / 2, 1, false,
						MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, -1));
			}
		});
		Box zoomPanel = Box.createHorizontalBox();
		zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
		minus.setAlignmentX(Component.LEFT_ALIGNMENT);
		plus.setAlignmentX(Component.RIGHT_ALIGNMENT);
		zoomPanel.add(minus);
		zoomPanel.add(plus);
		settings_panel.add(zoomPanel);

		// add the mouse mode combo box to the settings panel
		JComboBox modeBox = m_graphmouse.getModeComboBox();
		modeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		m_graphmouse.setMode(ModalGraphMouse.Mode.PICKING);
		JPanel modePanel = new JPanel(new BorderLayout()) {
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modePanel.add(modeBox);
		settings_panel.add(modePanel);

		// add the display type combo box to the settings panel
		String[] layoutTypeStrings = {"OrgChart Layout", "FR Layout"};
		layoutTypeBox = new JComboBox(layoutTypeStrings);
		layoutTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (layoutTypeBox.getSelectedIndex() == 0)
					moveVertices();//viphm_hashmap);
				else
					m_visualizationview.restart();
			}
		});
		layoutTypeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel displayTypePanel = new JPanel(new BorderLayout()) {
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		displayTypePanel.setBorder(BorderFactory
				.createTitledBorder("Display Type"));
		displayTypePanel.add(layoutTypeBox);
		settings_panel.add(displayTypePanel);

		// add user search to the panel - SHLOMO
		JPanel searchPanel = new JPanel(new BorderLayout()) {
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		// take the users and organize it sorted
		/*
		 * ArrayList<String> ta = new ArrayList<String>(userlist); String []
		 * usersarray = (String[]) ta.toArray(new String[ta.size()]);
		 */Arrays.sort(usersarray);
		usercombolist = new JComboBox(usersarray);

		usercombolist.insertItemAt("Anyone", 0);
		usercombolist.setSelectedIndex(0);// show only anyone choice
		// lets add all current users to the list

		searchPanel.setBorder(BorderFactory.createTitledBorder("Search User"));
		searchPanel.add(usercombolist);
	
		//add a check box to show not show labels on all vertices
		m_showLabels = new JCheckBox("Show name?",false);
		settings_panel.add(m_showLabels);
		
		
		settings_panel.add(searchPanel);

		usercombolist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// will need to react to the choice list
				if (usercombolist.getSelectedIndex() == 0) {
					// might have to reset something in case they move back
					PickedState picked_state = m_visualizationview
							.getPickedState();
					picked_state.clearPickedVertices();

					return;
				}
				String username = (String) usercombolist.getSelectedItem();

				for (Iterator walker = m_layout.getVertexIterator(); walker
						.hasNext();) {
					VIPVertex V = (VIPVertex) walker.next();

					// Integer indexNum = (Integer)index.getNumber(V);
					String seeName = V.getAcct();// accounts_arraylist.get(indexNum);
					if (username.equals(seeName)) {
						PickedState picked_state = m_visualizationview
								.getPickedState();
						picked_state.clearPickedVertices();
						picked_state.pick(V, true);
						/*
						 * int x= (int)m_layout.getX(V); int y=
						 * (int)m_layout.getY(V); //lets trigger the event
						 * m_graphmouse.mousePressed(new
						 * MouseEvent(m_visualizationview,MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,x,y,2,false));
						 */return;
					}
				}

			}
		});

		// add the settings panel to the settings box
		settings_box.add(settings_panel);

		// add the vip table to the control panel
		SortTableModel model = new SortTableModel();
		model.addColumn("Account");
		model.addColumn("ResponseScore");
		model.addColumn("SocialScore");
		m_timeTable = new JTable(model);
		model.addMouseListenerToHeaderInTable(m_timeTable);
		m_timeTable.setRowSelectionAllowed(true);
		m_timeTable.setColumnSelectionAllowed(false);
		m_timeTable.getTableHeader().setReorderingAllowed(false);
		m_timeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_timeTable.setDefaultRenderer(model.getColumnClass(1), md5Renderer);
		tablePane = new JScrollPane(m_timeTable);
		tablePane.setPreferredSize(new Dimension(300, 150));
		control_panel.add(tablePane, BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @param dgm
	 * @param minEdgeWeight
	 * @return
	 */
	static DirectedGraph getDGraph(sparseIntMatrix dgm, int minEdgeWeight,
			HashMap<String, vipUser> vips) {
		// initialize the directed graph object and create all the vertices
		NumberVertexValue dgraphIndex = new UserDatumNumberVertexValue(
				DGRAPH_INDEX);

		HashMap<Integer, VIPVertex> vercounts = new HashMap<Integer, VIPVertex>();
		DirectedGraph dg = new DirectedSparseGraph();
		/*
		 * int size = dgm.getSize();// dgm.length; Vertex []dVerts = new
		 * Vertex[size];
		 *//*
			 * for(int i=0;i<size;i++){ Vertex v = (Vertex) dg.addVertex(new
			 * DirectedSparseVertex());//add each vertex to the dgraph
			 * 
			 * Vertex v = (Vertex) dg.addVertex(new VIPVertex());
			 * dgraphIndex.setNumber(v,i); //index the vertices one by one
			 * dVerts[i] = v; //save each vertex into the verts array }
			 */

		int i = 0;
		// walk over the vip list and add a vertex for each one
		for (Iterator vipwalker = vips.keySet().iterator(); vipwalker.hasNext();) {

			VIPVertex v = (VIPVertex) dg.addVertex(new VIPVertex(i,
					(vipUser) vips.get(vipwalker.next())));
			vercounts.put(i, v);
			i++;

		}

		// calculate the edge weights and build the directed graph
		for (i = 0; i < dgm.getSize(); i++) {
			VIPVertex v1 = vercounts.get(i);// dVerts[i];
			for (int j = 0; j < dgm.getSize(); j++) {
				VIPVertex v2 = vercounts.get(j);// dVerts[j];
				if (/* (int)dgm[i][j] */dgm.get(i, j) >= minEdgeWeight && i != j) {
					DirectedEdge e = (DirectedEdge) dg
							.addEdge(new DirectedSparseEdge(v1, v2));
				}
			}
		}
		/*
		 * Object []returnguy = new Object[3]; returnguy[0] = dg; returnguy[1] =
		 * dVerts; returnguy[2] = dgraphIndex;
		 */
		// returned the directed graph for analysis
		return dg;/* returnguy; */
	}

	/**
	 * 
	 * Method for calculating the PageRanks of each of the Accounts on the list
	 * 
	 * @param dg
	 * @param dVerts
	 * @param allAccts
	 * @param viphm_hashmap
	 * @return
	 */
	static HashMap<String, Double> calcPageRank(DirectedGraph dg, /*
																	 * Vertex[]
																	 * dVerts,
																	 */
			String[] allAccts, HashMap<String, vipUser> viphm_hashmap /*
																		 * NumberVertexValue
																		 * dgraphIndex
																		 */
	) {

		PageRank ranker = new PageRank(dg, 0.15);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
		HashMap<String, Double> pageRanks = new HashMap<String, Double>();

		Set verset = dg.getVertices();

		// int size = verset.size();//dVerts.length;
		for (Iterator veriter = verset.iterator(); veriter.hasNext();) {// int
																		// i=0;i<size;i++){
			VIPVertex vp = (VIPVertex) veriter.next();
			// int curAcctNum =
			// (Integer)dgraphIndex.getNumber(verset.//dVerts[i]);
			String curAcct = vp.getAcct();// allAccts[curAcctNum];
			double curRank = ranker.getRankScore(vp);// dVerts[i]);
			if (viphm_hashmap.containsKey(curAcct)) {
				pageRanks.put(curAcct, curRank);
				// System.out.println(curAcct+": "+curRank);
			}
		}
		return pageRanks;
	}

	/**
	 * 
	 * 
	 * Method for creating the JUNG graph object using the HashMap version of
	 * the graph
	 * 
	 * @param fileNameNetwork
	 * @param hmgraph
	 * @param minEdgeWeight
	 * @param viphm_hashmap
	 * @return
	 * @throws IOException
	 */
	public Graph getGraph(String fileNameNetwork,
			HashMap<String, HashMap> hmgraph, int minEdgeWeight,
			HashMap<String, vipUser> viphm_hashmap) throws IOException {
		// -- create the graph object ----------------------------------
		Graph g = new UndirectedSparseGraph();
		StringLabeller sl = StringLabeller.getLabeller(g);

		// -- create the connection matrix -----------------------------
		// first fill acconts ArrayList
		Set keySet = hmgraph.keySet();
		Iterator keyIter = keySet.iterator();
		ArrayList<String> accounts_arraylist = new ArrayList<String>();
		int i = 0;
		int j;
		while (keyIter.hasNext()) {
			String acct = (String) keyIter.next();
			if (viphm_hashmap.containsKey(acct)) {
				accounts_arraylist.add(i++, acct);
			}
		}

		int size = i;
		int[][] connMatrix = new int[size][size];

		// next, create the connection matrix
		for (i = 0; i < accounts_arraylist.size(); i++) {
			String acct1 = accounts_arraylist.get(i);
			HashMap<String, Edge> edges = hmgraph.get(acct1);
			Set edgeSet = edges.keySet();
			Iterator edgeIter = edgeSet.iterator();
			while (edgeIter.hasNext()) {
				String acct2 = (String) edgeIter.next();
				if (accounts_arraylist.contains(acct2)) {
					CliqueEdge edge = (CliqueEdge) edges.get(acct2);
					int acctPlace = accounts_arraylist.indexOf(acct2);
					connMatrix[i][acctPlace] = edge.getWeight();
				}
			}
		}

		// -- use the connection matrix to create the graph ------------
		// first, create all the vertices and put them into a HashMap
		vertices_hashmap = new HashMap<String, VIPVertex>();
		for (i = 0; i < size; i++) {
			/*
			 * Vertex v1 = (Vertex) g.addVertex(new UndirectedSparseVertex());
			 */
			VIPVertex v1 = (VIPVertex) g.addVertex(new VIPVertex(i,
					viphm_hashmap.get(accounts_arraylist.get(i))));

			double responseScore = viphm_hashmap.get(accounts_arraylist.get(i))
					.getResponseScore();
			double socialScore = (viphm_hashmap.get(accounts_arraylist.get(i))
					.getSocialScore()) / 100;
			transparency.setNumber(v1, responseScore);
			// index.setNumber(v1,i);
			voltages.setNumber(v1, socialScore);
			vertices_hashmap.put(accounts_arraylist.get(i), v1);
		}

		// next create the edges between the appropriate vertices using the
		// connMatrix
		for (i = 0; i < size; i++) {
			VIPVertex v1 = vertices_hashmap.get(accounts_arraylist.get(i));
			for (j = i; j < size; j++) {
				VIPVertex v2 = vertices_hashmap.get(accounts_arraylist.get(j));
				// System.out.println("Checked for Edge");
				if (connMatrix[i][j] > minEdgeWeight) {
					// System.out.println("Added Edge");
					// TODO CHECKTHE NEXT LINE IF DIR OR UNDIRECTED
					UndirectedEdge e = (UndirectedEdge) g
							.addEdge(new UndirectedSparseEdge(v1, v2));
				}
			}
		}

		// assign the voltages (as strings) to the vertices
		vs = new NumberVertexValueStringer(voltages);

		// return the complete graph
		return g;
	}

	private final class SeedColor implements VertexPaintFunction {
		protected PickedInfo pi;
		protected final static float dark_value = 0.8f;
		protected final static float light_value = 0.2f;
		protected boolean seed_coloring;

		public SeedColor(PickedInfo pi) {
			this.pi = pi;
			seed_coloring = false;
		}

		public void setSeedColoring(boolean b) {
			this.seed_coloring = b;
		}

		public Paint getDrawPaint(Vertex v) {
			return Color.BLACK;
		}

		public Paint getFillPaint(Vertex v) {
			float alpha = transparency.getNumber(v).floatValue();
			if (pi.isPicked(v)) {
				return new Color(1f, 1f, 0, alpha);
			} else {
				if (seed_coloring
						&& v.containsUserDatumKey(BarabasiAlbertGenerator.SEED)) {
					Color dark = new Color(0, 0, dark_value, alpha);
					Color light = new Color(0, 0, light_value, alpha);
					return new GradientPaint(0, 0, dark, 10, 0, light, true);
				} else
					return new Color(1f, 0, 0, alpha);
			}

		}
	}

	private final static class EdgeWeightStrokeFunction
			implements
				EdgeStrokeFunction {
		protected static final Stroke basic = new BasicStroke(1);
		protected static final Stroke heavy = new BasicStroke(2);
		protected static final Stroke dotted = PluggableRenderer.DOTTED;

		protected boolean weighted = false;
		protected NumberEdgeValue edge_weight;

		public EdgeWeightStrokeFunction(NumberEdgeValue edge_weight) {
			this.edge_weight = edge_weight;
		}

		public void setWeighted(boolean weighted) {
			this.weighted = weighted;
		}

		public Stroke getStroke(Edge e) {
			if (weighted) {
				if (drawHeavy(e))
					return heavy;
				else
					return dotted;
			} else
				return basic;
		}

		protected boolean drawHeavy(Edge e) {
			double value = edge_weight.getNumber(e).doubleValue();
			if (value > 0.7)
				return true;
			else
				return false;
		}

	}

	private final static class VertexStrokeHighlight
			implements
				VertexStrokeFunction {
		protected boolean highlight = false;
		protected Stroke heavy = new BasicStroke(5);
		protected Stroke medium = new BasicStroke(3);
		protected Stroke light = new BasicStroke(1);
		protected PickedInfo pi;

		public VertexStrokeHighlight(PickedInfo pi) {
			this.pi = pi;
		}

		public void setHighlight(boolean highlight) {
			this.highlight = highlight;
		}

		public Stroke getStroke(Vertex v) {
			if (highlight) {
				if (pi.isPicked(v))
					return heavy;
				else {
					for (Iterator iter = v.getNeighbors().iterator(); iter
							.hasNext();) {
						Vertex w = (Vertex) iter.next();
						if (pi.isPicked(w))
							return medium;
					}
					return light;
				}
			} else
				return light;
		}
	}

	private final static class FontHandler
			implements
				VertexFontFunction,
				EdgeFontFunction {
		protected boolean bold = false;
		Font f = new Font("Helvetica", Font.PLAIN, 12);
		Font b = new Font("Helvetica", Font.BOLD, 12);

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public Font getFont(Vertex v) {
			if (bold)
				return b;
			else
				return f;
		}

		public Font getFont(Edge e) {
			if (bold)
				return b;
			else
				return f;
		}
	}

	private final static class DirectionDisplayPredicate implements Predicate {
		protected boolean show_d;
		protected boolean show_u;

		public DirectionDisplayPredicate(boolean show_d, boolean show_u) {
			this.show_d = show_d;
			this.show_u = show_u;
		}

		public void showDirected(boolean b) {
			show_d = b;
		}

		public void showUndirected(boolean b) {
			show_u = b;
		}

		public boolean evaluate(Object arg0) {
			if (arg0 instanceof DirectedEdge && show_d)
				return true;
			if (arg0 instanceof UndirectedEdge && show_u)
				return true;
			return false;
		}
	}

	private final static class VertexDisplayPredicate implements Predicate {
		protected boolean filter_small;
		protected final static int MIN_DEGREE = 4;

		public VertexDisplayPredicate(boolean filter) {
			this.filter_small = filter;
		}

		public void filterSmall(boolean b) {
			filter_small = b;
		}

		public boolean evaluate(Object arg0) {
			Vertex v = (Vertex) arg0;
			if (filter_small)
				return (v.degree() >= MIN_DEGREE);
			else
				return true;
		}
	}

	/**
	 * Controls the shape, size, and aspect ratio for each vertex.
	 * 
	 * @author Joshua O'Madadhain
	 */
	private final static class VertexShapeSizeAspect
			extends
				AbstractVertexShapeFunction
			implements
				VertexSizeFunction,
				VertexAspectRatioFunction {
		protected boolean stretch = false;
		protected boolean scale = false;
		protected boolean funny_shapes = false;
		protected NumberVertexValue voltages;

		public VertexShapeSizeAspect(NumberVertexValue voltages) {
			this.voltages = voltages;
			setSizeFunction(this);
			setAspectRatioFunction(this);
		}

		public void setStretching(boolean stretch) {
			this.stretch = stretch;
		}

		public void setScaling(boolean scale) {
			this.scale = scale;
		}

		public void useFunnyShapes(boolean use) {
			this.funny_shapes = use;
		}

		public int getSize(Vertex v) {
			if (scale)
				return (int) (voltages.getNumber(v).doubleValue() * 30) + 20;
			else
				return 20;
		}

		public float getAspectRatio(Vertex v) {
			if (stretch)
				return (float) (v.inDegree() + 1) / (v.outDegree() + 1);
			else
				return 1.0f;
		}

		public Shape getShape(Vertex v) {
			if (funny_shapes) {
				if (v.degree() < 5) {
					int sides = Math.max(v.degree(), 3);
					return factory.getRegularPolygon(v, sides);
				} else
					return factory.getRegularStar(v, v.degree());
			} else
				return factory.getEllipse(v);
		}
	}

	/**
	 * Populates the list below the graphics with all picked vertices
	 * 
	 * @author Ryan Rowe
	 */
	private class PickedListItemListener implements ItemListener {

		// default constructor
		public PickedListItemListener() {
		}

		// itemStateChanged method required by ItemListener implementation
		public void itemStateChanged(ItemEvent e) {
			// final VisualizationViewer vv =
			// (VisualizationViewer)e.getSource();
			for (int i = 0; i < 10000; i++) {
				i += 2;
				i -= 2;
			}
			PickedState pickedState = m_visualizationview.getPickedState();
			Set verts = pickedState.getPickedVertices();
			int size = verts.size();
			vipUser[] pickedList = new vipUser[size];
			Iterator iter = verts.iterator();
			int i = 0;

			// fill a new array with just the picked vertices
			while (iter.hasNext()) {
				VIPVertex v = (VIPVertex) iter.next();
				/*
				 * Integer indexNum = (Integer)index.getNumber(v); String acct =
				 * accounts_arraylist.get(indexNum);
				 */
				pickedList[i++] = v.getVIPInside();/* viphm_hashmap.get(acct); */
			}

			// use this array to populate and refresh the list
			SortTableModel srt = (SortTableModel) m_timeTable.getModel();
			populateTable(srt, pickedList);
		}

		// populateTable method for add all picked vertices to the table
		private void populateTable(SortTableModel model, vipUser[] pickedList) {
			model.setRowCount(0);

			// check for something
			if (pickedList == null)
				return;
			Object[] curRow = new Object[3];
			for (int i = 0; i < pickedList.length; i++) {
				vipUser vip = pickedList[i];
				curRow[0] = vip.getAcct();
				curRow[1] = (new Double(vip.getResponseScore()));
				curRow[2] = (new Double(vip.getSocialScore()));
				model.addRow(curRow);
			}
		}
	}

	/**
	 * Displays ToolTip information about the vertex/edge hovered over
	 * 
	 * @author Ryan Rowe
	 */
	private class VoltageTips extends DefaultToolTipFunction {
		public String getToolTipText(Vertex v) {
			/*
			 * Integer indexNum = (Integer)index.getNumber(v); return
			 * accounts_arraylist.get(indexNum);
			 */
			return v.toString();
		}
		public String getToolTipText(Edge edge) {
			Pair accts = edge.getEndpoints();
			/*
			 * Vertex v1 = (Vertex)accts.getFirst(); Vertex v2 =
			 * (Vertex)accts.getSecond(); Integer index1 =
			 * (Integer)index.getNumber(v1); Integer index2 =
			 * (Integer)index.getNumber(v2); String acct1 =
			 * accounts_arraylist.get(index1); String acct2 =
			 * accounts_arraylist.get(index2); return acct1 + " - " + acct2;
			 */
			VIPVertex v1 = (VIPVertex) accts.getFirst();
			VIPVertex v2 = (VIPVertex) accts.getSecond();
			return v2 + " - " + v2;
		}
	}

	private class GradientPickedEdgePaintFunction
			extends
				GradientEdgePaintFunction {
		private PickedInfo pi;
		private EdgePaintFunction defaultFunc;
		private final Predicate self_loop = SelfLoopEdgePredicate.getInstance();
		protected boolean fill_edge = false;

		public GradientPickedEdgePaintFunction(
				EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv,
				LayoutTransformer transformer, PickedInfo pi) {
			super(Color.WHITE, Color.BLACK, vv, transformer);
			this.defaultFunc = defaultEdgePaintFunction;
			this.pi = pi;
		}

		public void useFill(boolean b) {
			fill_edge = b;
		}

		public Paint getDrawPaint(Edge e) {
			if (gradient_level == GRADIENT_NONE) {
				return defaultFunc.getDrawPaint(e);
			} else {
				return super.getDrawPaint(e);
			}
		}

		protected Color getColor2(Edge e) {
			return pi.isPicked(e) ? Color.CYAN : c2;
		}

		public Paint getFillPaint(Edge e) {
			if (self_loop.evaluate(e) || !fill_edge)
				return null;
			else
				return getDrawPaint(e);
		}
	}
}
