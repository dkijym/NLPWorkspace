/**
 *
 *
 * @(#)DocflowDisplay.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/23
 */
package metdemo.Docflow;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Shape;

import java.util.*;

import metdemo.Tables.SortTableModel;
import metdemo.Tables.md5CellRenderer;
import metdemo.dataStructures.sparseIntMatrix;
import metdemo.dataStructures.vipUser;
import metdemo.dataStructures.CliqueEdge;
import metdemo.Finance.*;

import java.io.IOException;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.algorithms.importance.PageRank;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.graph.predicates.*;
import edu.uci.ics.jung.random.generators.*;
import edu.uci.ics.jung.utils.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.transform.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocflowDisplay extends JFrame {
	
	private final static Color GRAPHBACKGROUNDCOLOR = Color.white;
	private Graph flowGraph;
	private VisualizationViewer VV;
	private PluggableRenderer mainRender;
	private Layout mainLayout;
	private	DefaultModalGraphMouse m_graphmouse;
	private DefaultSettableVertexLocationFunction vlf;
		
	private DocumentFlowWindow parent;
	//the control panel
	JPanel control_panel;
	JComboBox compareType;
	JButton compareButton;
	DocflowCompare toCompare;
	
	public DocflowDisplay(DocumentFlowWindow p) {
		parent = p;
		toCompare = new DocflowCompare();
		flowGraph = new DirectedSparseGraph();		
		mainRender = new PluggableRenderer();
		vlf = new DefaultSettableVertexLocationFunction();
		mainLayout = new StaticLayout(flowGraph);
		VV = new VisualizationViewer(mainLayout,mainRender);
	}

	/**
	 *	setup this panel
	 */
	public void setPanel(int size_x, int size_y, Hashtable<String, OneFlow> eflow){
		
		flowGraph = new DirectedSparseGraph();		
		mainRender = new PluggableRenderer();
		
		//layout and location?
		vlf = new DefaultSettableVertexLocationFunction();
		mainLayout = new StaticLayout(flowGraph);
        ((AbstractLayout)mainLayout).initialize(new Dimension(size_x+50,size_y+50), vlf);
        
        //have to add all nodes here
        setFlow(eflow, (double)size_x, (double)size_y);
        
        //viewer
        VV = new VisualizationViewer(mainLayout,mainRender, new Dimension(size_x+50,size_y+50));
		VV.setPickSupport(new ShapePickSupport());
		VV.setBackground(Color.white);

        
		//mouse and tool tip
		m_graphmouse = new DefaultModalGraphMouse();
	    VV.setGraphMouse(m_graphmouse);
	    m_graphmouse.setMode(ModalGraphMouse.Mode.PICKING);
	    VV.setToolTipFunction(new DefaultToolTipFunction(){
	    	public String getToolTipText(Vertex v) {
	    		return v.toString();
	        }
	        public String getToolTipText(Edge edge) {
	            Pair accts = edge.getEndpoints();
	            Vertex v1 = (Vertex)accts.getFirst();
	            Vertex v2 = (Vertex)accts.getSecond();
	           	return v1 + " - " + v2;
	        }
	    });
	    
	    
	    //node color
	    mainRender.setVertexPaintFunction(new VertexPaintFunction(){
			
			public Paint getDrawPaint(Vertex arg0) {
				
				return Color.black;
			}

			public Paint getFillPaint(Vertex arg0) {
				
				if(VV.getPickedState().isPicked(arg0)){
					if (arg0 instanceof DocflowNode) {	
						return Color.yellow;
					}
					else if (arg0 instanceof FlowHeader) {
						return Color.pink;
					}
					
					return Color.black;
				}
				
				if (arg0 instanceof DocflowNode) {	
					//if contains attachment...
					int numatt = ((DocflowNode)arg0).numattach;
					if(numatt>0)return Color.blue;
					else return Color.cyan;
				}
				else if (arg0 instanceof FlowHeader) {
					if(((FlowHeader)arg0).isheader)
						return Color.pink;
				}
				
				return Color.black;
				
			}
	    	
	    });
	    
	    //size will depend on the number of cliques inside the vertice
	    mainRender.setVertexShapeFunction(new FlowNodeShape());
	    
	    //show the name on the vertex
	    mainRender.setVertexStringer(new VertexStringer() {
		  
            public String getLabel(ArchetypeVertex v) {
               
            	//want to only draw the string if we are in pick state
            	if(VV.getPickedState().isPicked(v)){
            		return v.toString();
				}

            	return "";
            }

			});
		
		VV.setPickedState(new MultiPickedState());
	    //todo: only pick two
	     
	    PickedState picked_state = VV.getPickedState();
	     
	    //edge color
	    mainRender.setEdgePaintFunction(new PickableEdgePaintFunction(picked_state, Color.black, Color.red));

		 /*
		  *mainRender.setEdgePaintFunction(new EdgePaintFunction(){

			public Paint getDrawPaint(Edge arg0) {
				return ((cliqueEdgeJung)arg0).GetColor();
			}

			public Paint getFillPaint(Edge arg0) {
				// TODO Auto-generated method stub
				return GRAPHBACKGROUNDCOLOR;
			}
	    });*/

         //lets color the edges appropriatly
        mainRender.setEdgeShapeFunction(new EdgeShape.Line(){});

		
		displayPanel();
	}
	
	/**
	 *	display it
	 */
	public void displayPanel(){
		
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(VV);
		jp.add(scrollPane);
		
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.getContentPane().add(jp,BorderLayout.CENTER);
		
		//the control panel
		setControlPanel();
		this.getContentPane().add(control_panel,BorderLayout.SOUTH);
		
		this.pack();
		this.setVisible(true);

	}

	/**
	 *	setup the location of the vertics
	 */
	public void setFlow(final Hashtable<String, OneFlow> eflow, double size_x, double size_y){
		
		//some setup for the plot, the base line
		int base_x = 0;
		int base_y = 0;
		
		int interval_x = (int)Math.round(size_x/(double)eflow.size());//no minimun value
		int interval_y = 30;//minimun interval
		
		//sort the flows by time
		Object[] allflow = getMap(eflow);
		Arrays.sort(allflow);
		
		//setup the timeline...
//		FlowHeader v1 = new FlowHeader();
//		FlowHeader v2 = new FlowHeader();
//		addVertex(v1, 0, base_y);
//		addVertex(v2, (int)size_x, base_y);
//		addEdge(v1, v2);
		
		for(int i=0;i<allflow.length;++i){
			OneFlow oneflow = (OneFlow)allflow[i];
			int x = base_x + interval_x * i;
			int y = base_y;
			
			//set the header
			FlowHeader header = new FlowHeader(oneflow.subject);
			int hy = oneflow.getLoc(base_y, size_y);
			if(hy - base_y <= interval_y) hy = base_y + interval_y;
			addVertex(header, x, hy);
			int lasty = hy;
			
			//set the emails
			for(int j=0;j<oneflow.flow.size();++j){
				DocflowNode node = oneflow.flow.get(j);
				int locy = node.getLoc(base_y, size_y);
				
				//I do this just to let the nodes not too close
				if(locy - lasty <= interval_y) locy = lasty + interval_y;
				
				addVertex(node, x, locy);
				if(j==0)addEdge(header, node);
				else addEdge(oneflow.flow.get(j-1), node);
				
				lasty = locy;
			}
			
		}
		
	}

	/**
	 *	add a vertex
	 */
	public void addVertex(Vertex item, int x, int y){
		Point2D location = new Point2D.Float(x, y);
        vlf.setLocation(item, location);
        flowGraph.addVertex(item);
	}
	
	/**
	 *	add an edge
	 */
	public void addEdge(Vertex from, Vertex to){
		try{
			flowGraph.addEdge(new DirectedSparseEdge(from, to));
		}catch(Exception e){
			
		}
		
	}
	
	/**
	 *	read the hashtable and put them into an array
	 */
	public static Object[] getMap(final Hashtable map){
        Object[] values = new Object[map.size()];
        int index = 0;
        Set entries = map.entrySet();
        Iterator iter = entries.iterator();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            values[index] = entry.getValue();
            index++;
        }
        return values;
    }
    
    /**
     *	the control panel of the jung plot	
     *	a copy from shlomo's code
     */
    public void setControlPanel(){
		control_panel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));    
        
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
        
        //add the comparison actions
        Box comparePanel = Box.createHorizontalBox();
        comparePanel.setBorder(BorderFactory.createTitledBorder("Compare"));
        //currently, only compare the first attachment if there are more than one
        //if select many emails, only compare the first two
        String[] types = {"Text", "Attachment", "Word doc"};
        compareType = new JComboBox(types);
		compareButton = new JButton("Compare");
		compareButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //compare
                compareDiff();
            }
        }); 
        comparePanel.add(compareType);
        comparePanel.add(compareButton);
		control_panel.add(comparePanel);
        //PickedState VisualizationViewer.getPickedState()
        //Set PickedState.getPickedVertices()
        
	}
	
	/**************************************************************/
	/**
	 *	compare the difference of two emails or two attachments
	 */
	public void compareDiff(){
		
		//clean up if it is used before
		toCompare = null;
		toCompare = new DocflowCompare();
		
		PickedState state =  VV.getPickedState();
		Set set = state.getPickedVertices();
		Object[] vertices = set.toArray();
		
		//if more than two vertices are selected, only compare the first two
		//need at least two vertices
		//don't take the header nodes
		DocflowNode[] mailrefs = new DocflowNode[2];
		int nodesIndex = 0;
		for(int i=0;i<vertices.length;++i){
			if(vertices[i] instanceof DocflowNode){
				mailrefs[nodesIndex++] = (DocflowNode)vertices[i];
			}
			if(nodesIndex > 1)break;
		}
		
		if(nodesIndex > 1){
			
			//get the time order of the select emails
			long t1 = parent.getTime(mailrefs[0].date, mailrefs[0].time);
			long t2 = parent.getTime(mailrefs[1].date, mailrefs[1].time);
			boolean order = true;
			if(t2>t1)order = false;
			
			//compare the text
			if(compareType.getSelectedIndex() == 0){
				if(order){
					toCompare.compareText(getEmailBody(mailrefs[0].mailref)
									, getEmailBody(mailrefs[1].mailref)
									, mailrefs[0].mailref
									, mailrefs[1].mailref
									, "Email Text");
				}
				else{
					toCompare.compareText(getEmailBody(mailrefs[1].mailref)
									, getEmailBody(mailrefs[0].mailref)
									, mailrefs[1].mailref
									, mailrefs[0].mailref
									, "Email Text");
				}

			}
			//compare the attachment
			else{
				boolean checkdoc = false;
				if(compareType.getSelectedIndex() == 2)checkdoc = true;
				
				if(order){
					byte[] b1 = getAttachment(mailrefs[0].mailref, checkdoc);
					if(b1 == null)System.out.println("b1 is empty");
					byte[] b2 = getAttachment(mailrefs[1].mailref, checkdoc);
					if(b2 == null)System.out.println("b2 is empty");
					
					if(b1 != null && b2 != null)
						toCompare.compareAtt(b1, b2
							, mailrefs[0].mailref, mailrefs[1].mailref
							, "Attachment Binary Content");
					
				}
				else{
					byte[] b1 = getAttachment(mailrefs[1].mailref, checkdoc);
					byte[] b2 = getAttachment(mailrefs[0].mailref, checkdoc);
					
					if(b1 == null)System.out.println("b1 is empty");
					if(b2 == null)System.out.println("b2 is empty");
					
					if(b1 != null && b2 != null)
						toCompare.compareAtt(b1, b2
							, mailrefs[1].mailref, mailrefs[0].mailref
							, "Attachment Binary Content");
				}
				
			}
		}
		

	}

	public char[] getEmailBody(String mailref){
		String query = "select body from message where mailref='"+mailref+"'";
		String[][] data = parent.fetchDB(query);
		
		StringBuffer buffer = new StringBuffer(data[0][0]);
		char[] array = new char[buffer.length()];
		buffer.getChars(0, buffer.length(), array, 0);
		
		return array;
	}
	
	public byte[] getAttachment(String mailref, boolean checkdoc){
		
		//get the correct filename
		String query = "select filename,hash,type from message where mailref='"+mailref+"'";
		String[][] data = parent.fetchDB(query);
		if(data == null)return null;

		//get the filename and hashcode
		String filename = "";
		String hash = null;
		for(int i=0;i<data.length;++i){
			
			if(!data[i][0].equals("")){
				
				//check whether it is a Word document
				if(checkdoc){
					filename = data[i][0];
					filename = filename.toLowerCase();
					if(filename.endsWith(".doc")){
						filename = data[i][0];
						hash = data[i][1];
						break;
					}
				}
				else{
					filename = data[i][0];
					hash = data[i][1];
					break;
				}
			}
		}

		if(hash == null)return null;
		
		//get the byte sequence
		query = "select body from message where hash='"+hash+"'";
        return parent.fetchBinaryDB(query);
        

	}
	/**************************************************************/
	
	/** some basic usage */
	public void restart(){
		flowGraph.removeAllEdges();
		flowGraph.removeAllVertices();
		mainLayout.restart();
		VV.restart();
		vlf.reset();
		//rough idea to reset the graph in the screen
//    	MutableTransformer mut = VV.getLayoutTransformer();
//    	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
//    	mut = VV.getViewTransformer();
//    	mut.setScale(1.0, 1.0, new Point2D.Float(0,0));
	}
//	
//	public void unPickAll(){
//		 PickedState picked_state = VV.getPickedState();
//	     picked_state.clearPickedVertices();
//	     picked_state.clearPickedEdges();
//	}

	/**
	 *	the shape of the nodes, represents the number of recepients
	 */
	private class FlowNodeShape extends AbstractVertexShapeFunction  implements VertexSizeFunction{

		public FlowNodeShape() {
			setSizeFunction(this);
		}
	
		public Shape getShape(Vertex arg0) {
			//if(arg0.getClass() == DocflowNode.class){
			if (arg0 instanceof DocflowNode) {
				int size = ((DocflowNode)arg0).rcpt.size();
				if (size > 2 && size < 10) {
					return factory.getRegularPolygon(arg0, size);
				}
			}

			//default
			return factory.getEllipse(arg0);
		}

		public int getSize(Vertex arg0) {
			return 10;
		}	
	}
	
	
}
