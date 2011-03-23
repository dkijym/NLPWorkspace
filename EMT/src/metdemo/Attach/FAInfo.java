package metdemo.Attach;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import chapman.graphics.JPlot2D;
//import java.util.*;

class FAInfo extends JDialog{

    private boolean isbreak = false;

    //For the diagrams.
    JPlot2D distribution;
    JPlot2D[] plots;

    //The panel for the buttons and for the diagrams
    private JPanel buttonP, figureP, msgP;
    private JScrollPane centerP;
    
    /**
       constructor
    */
    public FAInfo(String[] msg, Component parent
		  , double[][] arrays, String[] titles1, int[] titles2
		  ,int numOfPlots){

	msgP = new JPanel();
	
	int size = msg.length;
	msgP.setLayout(new GridLayout(size+2,1));
	JLabel[] label = new JLabel[size];
	
	for(int i=0;i<size;i++){
		label[i] = new JLabel(msg[i]);
		label[i].setForeground(Color.blue);
		msgP.add(label[i]);
	}

	//setup the diagrams
	figureP = new JPanel();
	int height = (numOfPlots+1)/2;
	figureP.setLayout(new GridLayout(height,2));
			
	plots = new JPlot2D[numOfPlots];
	
	for(int i=0;i<plots.length;i++){
	    plots[i] = new JPlot2D();
	    plots[i].setPreferredSize(new Dimension(300,280));
	    setFigure("Extension: \""+titles1[i]+"\" "
		      +"   # of files: "+titles2[i]
		      , "Byte Value", "Occurence", plots[i]
		      , Color.blue, arrays[i]);
	    figureP.add(plots[i]);
	}
	
	centerP = new JScrollPane(figureP);
	buttonP = new JPanel();
	JButton close = new JButton("Close");
	close.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent evt){ 
		    close();
		} 
	    } );
		
	buttonP.add(close);

	getContentPane().add(msgP, "North");
	getContentPane().add(centerP, "Center");
	getContentPane().add(buttonP, "South");
	setTitle("File Binary Information");
	setSize(640, 600);
	//setLocationRelativeTo(parent);
	setLocation(100,50);
	//pack();
	addWindowListener(new WindowAdapter()
	    {  public void windowClosing(WindowEvent e)
		{  isbreak = true;
		}
	    } );
    }
	
    //a second constructor
    public FAInfo(String msg, Component parent){

        msgP = new JPanel();

	//setup the diagrams
        figureP = new JPanel();
	figureP.add(new JTextArea(msg));
	centerP = new JScrollPane(figureP);
        buttonP = new JPanel();
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    close();
                }
            } );

        buttonP.add(close);

        getContentPane().add(msgP, "North");
        getContentPane().add(centerP, "Center");
        getContentPane().add(buttonP, "South");
        setTitle("Testing Report");
        setSize(640, 600);
        //setLocationRelativeTo(parent);
        setLocation(100,50);
        //pack();
        addWindowListener(new WindowAdapter()
            {  public void windowClosing(WindowEvent e)
                {  isbreak = true;
                }
            } );

    }

    /**
       Set the diagrams.
       @param title The title of this diagram
       @param x Label of X
       @param y Label of Y
       @param plot This plot(JPlot2D)
       @param color the color of this curve
       @param value The data of this curve
    */
    public void setFigure(String title, String x, String y, JPlot2D plot, Color color, double[] value){
	plot.addCurve(value);
	plot.setLineColor(color);
	plot.setFillColor(color);
	plot.setGridState(JPlot2D.GRID_ON);
	plot.setPlotType(JPlot2D.BAR);
	plot.setTitle(title);
	plot.setXLabel(x);
	plot.setYLabel(y);
    }
	
    /**
       Add curve of a diagram.
       @param plot This plot
       @param value The curve
       @param color The color of this curve
    */
    public void addCurve(JPlot2D plot, double[] value, Color color){
	plot.addCurve(value);
	plot.setLineColor(color);
    }

    /**
      You know what? Repaint it!
    */	
    public void redraw(){
	figureP.repaint();
    }
    
    /**
       Is closed or not.
       @return closed or not.
    */
    public boolean isBreak(){
	return isbreak;
    }
	
    /**
       To close it.
     */
    public void close(){
	setVisible(false);
    }

}

