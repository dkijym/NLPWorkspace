/**
 * 
 */
package metdemo.CliqueTools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.GradientEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.predicates.SelfLoopEdgePredicate;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.DefaultSettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.HasGraphLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.MultiPickedState;
import edu.uci.ics.jung.visualization.PickedInfo;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.transform.LayoutTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * @author shlomo
 *
 */
public class cliqueHierarchyVisualiazerJung extends JPanel {

	private final static Color GRAPHBACKGROUNDCOLOR = Color.white;
	private Graph cliqueGraph;
	//helpers
	private VisualizationViewer VV;
	private PluggableRenderer mainRender;
	private Layout mainLayout;
	private	DefaultModalGraphMouse m_graphmouse;
	private DefaultSettableVertexLocationFunction vlf;
	
	//helpers
	private HashMap<Integer, Vertex> vertexmaps;
	int count=0;
	
	public void restart(){
		count=0;
		cliqueGraph.removeAllEdges();
		cliqueGraph.removeAllVertices();
		mainLayout.restart();
		VV.restart();
		vlf.reset();
//		rough idea to reset the graph in the screen
    	MutableTransformer mut = VV.getLayoutTransformer();
    	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
    	mut = VV.getViewTransformer();
    	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
	}
	
	public cliqueHierarchyVisualiazerJung() {
		
		vertexmaps = new HashMap<Integer, Vertex>();
		cliqueGraph = new UndirectedSparseGraph();//SparseGraph();
		//TestGraphs.getOneComponentGraph();//
	
		mainRender = new PluggableRenderer();
		vlf = new DefaultSettableVertexLocationFunction();
		mainLayout = new StaticLayout(cliqueGraph);
        ((AbstractLayout)mainLayout).initialize(new Dimension(500,500), vlf);
        
		//new CircleLayout(cliqueGraph);
		VV = new VisualizationViewer(mainLayout,mainRender,new Dimension(500,500));
		VV.setPickSupport(new ShapePickSupport());
		VV.setBackground(GRAPHBACKGROUNDCOLOR);

		m_graphmouse = new DefaultModalGraphMouse();
	    VV.setGraphMouse(m_graphmouse);
	    VV.setToolTipFunction(new DefaultToolTipFunction(){
	    	public String getToolTipText(Vertex v) {
	        	/*Integer indexNum = (Integer)index.getNumber(v);
	        	return accounts_arraylist.get(indexNum);*/
	    		return v.toString();
	        }
	        public String getToolTipText(Edge edge) {
	            Pair accts = edge.getEndpoints();
	            Vertex v1 = (Vertex)accts.getFirst();
	            Vertex v2 = (Vertex)accts.getSecond();
	           	return v1 + " - " + v2;
	        }
	    });
		
	    //size will depend on the number of cliques inside the vertice
	    mainRender.setVertexShapeFunction(new userNodeVertexShapeFunction());
	    mainRender.setVertexPaintFunction(new VertexPaintFunction(){

			public Paint getDrawPaint(Vertex arg0) {
				
				
				if(VV.getPickedState().isPicked(arg0)){
					return Color.yellow;
				}
				
				if(arg0.getClass() == cliqueVertex.class){
					return Color.BLUE;
				}
				return Color.black;
			}

			public Paint getFillPaint(Vertex arg0) {
				
				if(VV.getPickedState().isPicked(arg0)){
					return Color.yellow;
				}
				
				if(arg0.getClass() == cliqueVertex.class){
					return Color.BLUE;
				}
				return Color.black;
			}
	    	
	    });
	   
	    //show the name on the vertex (in our case v1..vn
	   mainRender.setVertexStringer(new VertexStringer() {

            public String getLabel(ArchetypeVertex v) {
               
            	//want to only draw the string if we are in pick state
            	if(VV.getPickedState().isPicked(v)){
            		return v.toString();
				}
            	
            	
            	/*if(v.getClass() == UserClique.class)
            		return v.toString();
            	else*/
            	return "";
            }

			});

	    VV.setPickedState(new MultiPickedState());
	    
	    
	    //setup the system to respond to the clicks on specific items
	    PickedState picked_state = VV.getPickedState();
	  /* 
        picked_state.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent arg0) {
				// will handle vertex vs edges differently.
				//need to cehck its a picked event
				//if not ignore....
				if(arg0.getStateChange() == ItemEvent.DESELECTED){
					//???
					return;
				}
				
				if(arg0.getItem().getClass() == cliqueVertex.class){
					//System.out.println(arg0.getItem().toString());
					
					if(isworking)
						return;
					
					PickedState ps = VV.getPickedState();
					Set<cliqueVertex> pickedVertices = ps.getPickedVertices();
					if(tempSeenVertSet.equals(pickedVertices.toString())){
						return;
					}
					tempSeenVertSet = pickedVertices.toString();
					ArrayList<cliqueVertex> stuffVerts = new ArrayList<cliqueVertex>(pickedVertices.size());
					
					for(Iterator<cliqueVertex> allverts = pickedVertices.iterator();allverts.hasNext();)
					{
						stuffVerts.add(allverts.next());
					}
				
					  updateInformationBoard(stuffVerts);
				}else if(arg0.getItem().getClass() == cliqueEdgeJung.class){
					 Pair accts = ((UndirectedSparseEdge)arg0.getItem()).getEndpoints();
					 cliqueVertex v1 = (cliqueVertex)accts.getFirst();
					 cliqueVertex v2 = (cliqueVertex)accts.getSecond();
			        
					 //unselect any vert
			            PickedState picked_state = VV.getPickedState();
	        		isworking = true;//signal not to redo the baord
			            picked_state.clearPickedVertices();  
			           picked_state.pick(v1,true);//set as picked
			           picked_state.pick(v2,true);//set as picked
					 isworking= false;
					 
					 
					 ArrayList<cliqueVertex> stuffVerts = new ArrayList<cliqueVertex>(2);
			            stuffVerts.add(v1);
			            stuffVerts.add(v2);
			          
			            
			            
			            
			            
				}else{
					System.out.println("woo?:"+arg0.getItem().getClass());
				}
			}
        	
        });*/
		//allow edges to light up
        //mainRender.setEdgePaintFunction(new PickableEdgePaintFunction(picked_state, Color.black, Color.red));

        mainRender.setEdgePaintFunction(new userGradientPickedEdgePaintFunction(new PickableEdgePaintFunction(picked_state,Color.RED,Color.cyan),
                VV, VV, picked_state));
        
        
        
        //lets color the edges appropriatly
        mainRender.setEdgeShapeFunction(new EdgeShape.Line(){
        	
        });
        
	    /*mainRender.setEdgePaintFunction(new EdgePaintFunction(){

			public Paint getDrawPaint(Edge arg0) {
				return ((cliqueEdgeJung)arg0).GetColor();
			}

			public Paint getFillPaint(Edge arg0) {
				// TODO Auto-generated method stub
				return GRAPHBACKGROUNDCOLOR;
			}
			
			

			
	    	
	    });*/
        
        
	    mainRender.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(new BasicStroke(1f)));
      
     
		this.add(VV);
		
		
	}
	
	
	public void unPickAll(){
		 PickedState picked_state = VV.getPickedState();
	     picked_state.clearPickedVertices();
	     picked_state.clearPickedEdges();
	}
	
	
	public void passUserList(ArrayList<String> usernames){
		
		//will run through all the vertex and update the users who are on this list
		
		HashMap<String, String> userUp = new HashMap<String, String>();
		
		for(int i=0;i<usernames.size();i++){
			
			userUp.put(usernames.get(i),"");
		}
		
		PickedState picked_state = VV.getPickedState();
		
		
		//now to loop through all the users
		for(Iterator walker = mainLayout.getVertexIterator();walker.hasNext();){
    		Vertex V = (Vertex)walker.next();
    		
    		if(V.getClass() == userVertex.class)
    		if(userUp.containsKey(V.toString())){
    			 
    			picked_state.pick(V,true);
    			
    			//now to set all edges as picked
    			for(Iterator<Edge> edgewalker = V.getOutEdges().iterator();edgewalker.hasNext();){
    				picked_state.pick((Edge)edgewalker.next(),true);
    			}
    			
    			
    			
    		}
    	}
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * return a vertex based on a number id
	 * @param num
	 * @return
	 */
		public Vertex getVertex(int num){
			return vertexmaps.get(num);
		}
		
		/**
		 * 
		 * @param item
		 * @return
		 */
		public int addVertex(Vertex item,int x,int y){
			vertexmaps.put(count++,item);
			
			Point2D location = new Point2D.Float(x, y);
           
            vlf.setLocation(item, location);
            cliqueGraph.addVertex(item);
			return count-1;
		}
		
		
		public void addEdge(Edge item){
			try{
			cliqueGraph.addEdge(item);
			}catch(Exception e){
				
			}
			
		}
		
		/**
		 * Trigger an update of the window
		 * @param shutdown should the x on top close the program
		 * @param ownwindow should it be launched in own window, or assumed to be part of another
		 */
		public void showSomething(boolean shutdown,boolean ownwindow){
			mainLayout.restart();
			if(ownwindow){
			JFrame testwindow = new JFrame();
			if(shutdown){
				testwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}else{
			testwindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
			testwindow.setLayout(new BorderLayout());
			testwindow.getContentPane().add(this,BorderLayout.CENTER);
			testwindow.getContentPane().add(getControlPanel(),BorderLayout.SOUTH);
			//testwindow.getContentPane().add(getInformationPanel(),BorderLayout.NORTH);
			testwindow.pack();//setSize(new Dimension(400,400));
			testwindow.setVisible(true);
			}
		}	
		
		public JPanel getControlPanel(){
			JPanel control_panel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));    
	        
	        //zoom controls
	        JButton plus = new JButton("+");
	        plus.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                // call listener in GraphMouse instead of manipulating vv scale directly
	                // this is so the crossover from zoom to scale works with the buttons
	                // as well as with the mouse wheel
	                Dimension d = VV.getSize();
	                m_graphmouse.mouseWheelMoved(new MouseWheelEvent(VV,MouseEvent.MOUSE_WHEEL,
	                        System.currentTimeMillis(),0,d.width/2,d.height/2,1,false,
	                        MouseWheelEvent.WHEEL_UNIT_SCROLL,1,1));
	             }
	        });
	        JButton minus = new JButton(" - ");
	        minus.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                // call listener in GraphMouse instead of manipulating vv scale directly
	                // this is so the crossover from zoom to scale works with the buttons
	                // as well as with the mouse wheel
	                Dimension d = VV.getSize();
	                m_graphmouse.mouseWheelMoved(new MouseWheelEvent(VV,MouseEvent.MOUSE_WHEEL,
	                        System.currentTimeMillis(),0,d.width/2,d.height/2,1,false,
	                        MouseWheelEvent.WHEEL_UNIT_SCROLL,1,-1));
	            }
	        }); 
	        JButton zerobutton = new JButton(" 0 ");
	        zerobutton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                // call listener in GraphMouse instead of manipulating vv scale directly
	                // this is so the crossover from zoom to scale works with the buttons
	                // as well as with the mouse wheel
	               /* Dimension d = VV.getSize();
	                m_graphmouse.mouseWheelMoved(new MouseWheelEvent(VV,MouseEvent.MOUSE_WHEEL,
	                        System.currentTimeMillis(),0,d.width/2,d.height/2,1,false,
	                        MouseWheelEvent.WHEEL_UNIT_SCROLL,1,-1));*/
	            	/*VV.(new Point(0,0));
	            	VV.repaint();*/
	          //  bird.resetLens();
	            			//vc.scale(VV, 1, new Point(250,250));
	            	//rough idea to reset the graph in the screen
	            	MutableTransformer mut = VV.getLayoutTransformer();
	            	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
	            	mut = VV.getViewTransformer();
	            	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
	            	
	            	
	            }
	        });
	        Box zoomPanel = Box.createHorizontalBox();
	        zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
	        minus.setAlignmentX(Component.LEFT_ALIGNMENT);
	        plus.setAlignmentX(Component.RIGHT_ALIGNMENT);
	        zoomPanel.add(minus);  
	        zoomPanel.add(plus);   
	        zoomPanel.add(zerobutton);
	        control_panel.add(zoomPanel);
	        //done zoom controls
	        //picking contols
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
	        control_panel.add(modePanel);
	        /*//searching for user
	        JPanel searchPanel = new JPanel();
	        searchPanel.setBorder(BorderFactory.createTitledBorder("Search User"));
	        searchPanel.add(new JTextField(20));
	        control_panel.add(searchPanel);
	        */
	        //add help button
	        //TODO:
	        
	        return control_panel;
		}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		create a test graph
		cliqueHierarchyVisualiazerJung JCV = new cliqueHierarchyVisualiazerJung();
		for(int i=0;i<10;i++)
		{
			JCV.addVertex(new userVertex(Math.random()+""),i*2,i+10);
		}
		
		/*for(int i=0;i<5;i++)
		{
			JCV.addEdge(new (JCV.getVertex((int)(Math.random()*10)),JCV.getVertex((int)(Math.random()*50))));
		}*/
		
		JCV.showSomething(true,true);
	}
	
	
	protected static final int GRADIENT_NONE = 0;
	protected static final int GRADIENT_RELATIVE = 1;
	
	private class userGradientPickedEdgePaintFunction extends GradientEdgePaintFunction
    {
		private int gradient_level = GRADIENT_RELATIVE;
        private PickedInfo pi;
        private EdgePaintFunction defaultFunc;
        private final Predicate self_loop = SelfLoopEdgePredicate.getInstance();
        protected boolean fill_edge = false;

        public userGradientPickedEdgePaintFunction( EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv,
                LayoutTransformer transformer, PickedInfo pi )
        {
            super(Color.WHITE, Color.BLACK, vv, transformer);
            this.defaultFunc = defaultEdgePaintFunction;
            this.pi = pi;
        }

        public void useFill(boolean b)
        {
            fill_edge = b;
        }

        public Paint getDrawPaint(Edge e) {
            if (gradient_level == GRADIENT_NONE) {
                return defaultFunc.getDrawPaint(e);
            } else {
                return super.getDrawPaint(e);
            }
        }

        protected Color getColor2(Edge e)
        {
        	c2 =  ((cliqueEdgeJung)e).GetColor();
        	
            return pi.isPicked(e)? Color.CYAN : c2;
        }

        public Paint getFillPaint(Edge e)
        {
            if (self_loop.evaluate(e) || !fill_edge)
                return null;
            else{
                
            	
            	return getDrawPaint(e);
                }
        }
    }
}


