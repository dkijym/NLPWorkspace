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
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * @author shlomo
 *
 */
public class JungCliquesVisualizer extends JPanel {

	private final static Color GRAPHBACKGROUNDCOLOR = Color.white;
	private Graph cliqueGraph;
	//helpers
	private VisualizationViewer VV;
	private PluggableRenderer mainRender;
	private Layout mainLayout;
	private	DefaultModalGraphMouse m_graphmouse;
	//speedups
	int count=0;
	private HashMap<Integer, Vertex> vertexmaps;

	private JScrollPane m_informationPanel;
	private JTextArea p_userinformationList;
	private JTextArea p_userinformationSubject;
	private JTextArea p_userinformationSharing;
	private boolean isworking = false;//used on multi threaded parts so do less work :)
	private String tempSeenVertSet = new String(); //for seen vertices sets
	//  private BirdsEyeVisualizationViewer bird; 
	
	//this allows click on users to be updated on other panel
	private cliqueHierarchyVisualiazerJung friendHandle = null;
	
	
	
	/**
	 * restart the class
	 *
	 */
	public void restart(){
		count=0;
		vertexmaps.clear();
		cliqueGraph.removeAllEdges();
		cliqueGraph.removeAllVertices();
		
		mainLayout.restart();
		VV.restart();
//		rough idea to reset the graph in the screen
    	MutableTransformer mut = VV.getLayoutTransformer();
    	mut.setScale(1.0, 1.0, new Point2D.Float(250,250));
    	mut = VV.getViewTransformer();
    	mut.setScale(1.0, 1.0, new Point2D.Float(250,250));
		updateInformationBoard(null);
	}
	
	
	
	
	JungCliquesVisualizer(cliqueHierarchyVisualiazerJung friendh){
		this();
		setFriend(friendh);
	}
	
	JungCliquesVisualizer(){
		
		setupInformationPanel();
		vertexmaps = new HashMap<Integer, Vertex>();
		cliqueGraph = new UndirectedSparseGraph();//SparseGraph();
		//TestGraphs.getOneComponentGraph();//
	
		mainRender = new PluggableRenderer();
		mainLayout = new CircleLayout(cliqueGraph);
		VV = new VisualizationViewer(mainLayout,mainRender,new Dimension(500,500));
		VV.setPickSupport(new ShapePickSupport());
	//	VV.setPreferredSize(new Dimension(500,500));
		VV.setBackground(GRAPHBACKGROUNDCOLOR);
//		VV.setForeground(Color.white);
		//VV.setPickSupport(new ShapePickSupport());
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
	            /*Integer index1 = (Integer)index.getNumber(v1);
	            Integer index2 = (Integer)index.getNumber(v2);
	            String acct1 = accounts_arraylist.get(index1);
	            String acct2 = accounts_arraylist.get(index2);*/
	        	return v1 + " - " + v2;
	        }
	    });
		
	    //size will depend on the number of cliques inside the vertice
	    mainRender.setVertexShapeFunction(new cliqueVertexShapeFunction());
	    
	   
	    
	    //show the name on the vertex (in our case v1..vn
	    mainRender.setVertexStringer(new VertexStringer() {

            public String getLabel(ArchetypeVertex v) {
                return v.toString();
            }

			});

	    //VV.setPickedState(new MultiPickedState());
	    
	    
	    //setup the system to respond to the clicks on specific items
	    PickedState picked_state = VV.getPickedState();
	   
        picked_state.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent arg0) {
				// will handle vertex vs edges differently.
				//need to cehck its a picked event
				//if not ignore....
				if(arg0.getStateChange() == ItemEvent.DESELECTED){
					updateInformationBoard(null);
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
			            updateInformationBoard(stuffVerts);
			            
			            
			            
			            
				}else{
					System.out.println("woot?:"+arg0.getItem().getClass());
				}
			}
        	
        });
		//allow edges to light up
        //mainRender.setEdgePaintFunction(new PickableEdgePaintFunction(picked_state, Color.black, Color.red));

        //lets color the edges appropriatly
        mainRender.setEdgeShapeFunction(new EdgeShape.Line(){
        	
        });
        
	    mainRender.setEdgePaintFunction(new EdgePaintFunction(){

			public Paint getDrawPaint(Edge arg0) {
				return ((cliqueEdgeJung)arg0).GetColor();
			}

			public Paint getFillPaint(Edge arg0) {
				// TODO Auto-generated method stub
				return GRAPHBACKGROUNDCOLOR;
			}
			
			

			
	    	
	    });
	    mainRender.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(new BasicStroke(1.5f)));
      
       // bird = new BirdsEyeVisualizationViewer(VV, 0.25f, 0.25f);
        
		///VV.init();
		
        //bird.initLens();
		this.add(VV);
		
		
		
		
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
	public int addVertex(cliqueVertex item){
		vertexmaps.put(count++,item);
		cliqueGraph.addVertex(item);
			
		//need to re layout the components
		//mainLayout.restart();
		//ScalingControl scaler = new ViewScalingControl();
		//scaler.scale(VV, .9f, VV.getCenter());
		return count-1;
	}
	
	/**
	 * 
	 * @param item
	 */
	public void addEdge(cliqueEdgeJung item){
		try{
		cliqueGraph.addEdge(item);
		}catch(Exception e){
			
		}
		//mainLayout.restart();
	}
	
	
	private void setupInformationPanel(){
		
		m_informationPanel = new JScrollPane();

		 p_userinformationList = new JTextArea(4,18);
		 p_userinformationSubject = new JTextArea(4,18);
		 p_userinformationSharing = new JTextArea(4,18);
		
		 p_userinformationList.setEditable(false); 
		 p_userinformationSubject.setEditable(false);
		 p_userinformationSharing.setEditable(false);
	  
		 p_userinformationList.setBorder( BorderFactory.createTitledBorder("Users"));
		 p_userinformationSubject.setBorder( BorderFactory.createTitledBorder("Common Words"));
		 p_userinformationSharing.setBorder( BorderFactory.createTitledBorder("Shared Users"));
		 
		 
		/* JScrollPane uinfo = new JScrollPane();*/
		 m_informationPanel.setPreferredSize(new Dimension(700,100));
	/*	 
		 uinfo.add(p_userinformationList);
			uinfo.add(p_userinformationSubject);
			uinfo.add(p_userinformationSharing);
		 m_informationPanel.add(uinfo);*/
		 JPanel infopanel = new JPanel(new GridLayout(1,3));
		 infopanel.add(p_userinformationList);
		 infopanel.add(p_userinformationSubject);
		 infopanel.add(p_userinformationSharing);
		
		 
		m_informationPanel.add(infopanel);
		m_informationPanel.setViewportView(infopanel);
	}
	
	public JScrollPane getInformationPanel(){
		return m_informationPanel;
	}
	
	
	public void setFriend(cliqueHierarchyVisualiazerJung friendperson){
		friendHandle = friendperson;
	}
	
	
	public void updateInformationBoard(ArrayList<cliqueVertex> listverts){
	
		//signal to clear the board
		if(listverts == null){
			 p_userinformationList.setText("");
			 p_userinformationSubject.setText("");
			 p_userinformationSharing.setText("");
			return;
		}
		
		
		
		
		HashMap<String,Integer> countingSeenUsers = new HashMap<String, Integer>();
		
//this is to helpupdate the left side panel of users
		if(friendHandle!=null){
			friendHandle.unPickAll();
		}
		
		StringBuffer userinfo = new StringBuffer();
		StringBuffer subjectinfo = new StringBuffer();
		userinfo.append("choose: " + listverts.size()+"\n");
		for(int i=0;i<listverts.size();i++){
			cliqueVertex currentVert = listverts.get(i);
			
//			vertex info
	
			userinfo.append("Vertex: " + currentVert.toString());
			userinfo.append(" size: " + currentVert.getSize()+"\n");
			userinfo.append(currentVert.getNames()+"\n");
			
			//subject info
			subjectinfo.append("Vertex: " + currentVert.toString()+"\n");
			subjectinfo.append(currentVert.getSubject()+"\n");
			
			//count all usrnames to see overlap
			ArrayList<String> useremails = currentVert.getUserList();
			
			if(friendHandle!=null){
				friendHandle.passUserList(useremails);
			}
			
			
			for(int num =0;num<useremails.size();num++){
				String currentName = useremails.get(num);
				if(!countingSeenUsers.containsKey(currentName)){
					countingSeenUsers.put(currentName, 1);
				}else{
					countingSeenUsers.put(currentName, countingSeenUsers.get(currentName)+1);
				}
			}
			
			
		}
		
		
		StringBuffer crossSeenNames = new StringBuffer();
		//TODO: calculate overlap user by looking at count +1
	if(listverts.size()>1){
		//set through and add anyone with count>1
		for(Iterator<String> seennames = countingSeenUsers.keySet().iterator();seennames.hasNext();){
			
			String aName = seennames.next();
			if(countingSeenUsers.get(aName) > 1){
				crossSeenNames.append(aName+"\n");
			}
		}
	}
	
		 p_userinformationList.setText(userinfo.toString());
		 p_userinformationList.setCaretPosition(0);
		 
		 p_userinformationSubject.setText(subjectinfo.toString());
		  p_userinformationSubject.setCaretPosition(0);
		 p_userinformationSharing.setText(crossSeenNames.toString());
		 p_userinformationSharing.setCaretPosition(0);
	
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
            	mut.setScale(1.0, 1.0, new Point2D.Float(250,250));
            	mut = VV.getViewTransformer();
            	mut.setScale(1.0, 1.0, new Point2D.Float(250,250));
            	
            	
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
        //searching for user
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search User"));
        searchPanel.add(new JTextField(20));
        //control_panel.add(searchPanel);
        
        //add help button
        //TODO:
        
        return control_panel;
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
		testwindow.getContentPane().add(getInformationPanel(),BorderLayout.NORTH);
		testwindow.pack();//setSize(new Dimension(400,400));
		testwindow.setVisible(true);
		}
	}	

public static void main(String[] args) {
	
	//create a test graph
	JungCliquesVisualizer JCV = new JungCliquesVisualizer();
	for(int i=0;i<100;i++)
	{
		JCV.addVertex(new cliqueVertex(JungCliquesVisualizer.generateRandomArrays()));
	}
	
	for(int i=0;i<500;i++)
	{
		JCV.addEdge(new cliqueEdgeJung(JCV.getVertex((int)(Math.random()*10)),JCV.getVertex((int)(Math.random()*50))));
	}
	
	JCV.showSomething(true,true);
}


static ArrayList<String> generateRandomArrays(){
	ArrayList<String> A1 = new ArrayList<String>();
	
	int num = (int)(Math.random() * 10);
	for(int i=0;i<num;i++){
		A1.add("the " + i + " ");
	}
	
	return A1;
	
}

}
