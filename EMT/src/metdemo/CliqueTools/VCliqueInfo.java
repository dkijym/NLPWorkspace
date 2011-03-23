/**
   @version 1.x (How do I know how many times I changed it?)
   @author Wei-Jen Li

   This dialog displays the statistical infomation in Graphic Clique
*/

package metdemo.CliqueTools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import chapman.graphics.JPlot2D;
import java.util.*;

public class VCliqueInfo extends JDialog{

    private boolean isbreak = false;

    //For the diagrams.
    public JPlot2D distribution;
	public JPlot2D[] plots;

    //The panel for the buttons and for the diagrams
    private JPanel buttonP, figureP, msgP;
    private JScrollPane centerP;
    
	
    /**
       The constructor
    */
    public VCliqueInfo(String msg1, String msg2, String msg3, String msg4, Component parent){

	msgP = new JPanel();
	msgP.setLayout(new GridLayout(4,1));
	JLabel l1 = new JLabel(msg1);
	JLabel l2 = new JLabel(msg2);
	JLabel l3 = new JLabel(msg3);
	JLabel l4 = new JLabel(msg4);
	l1.setForeground(Color.blue);
	l2.setForeground(Color.blue);
	l3.setForeground(Color.blue);
	l4.setForeground(Color.blue);
	msgP.add(l1);
	msgP.add(l2);
	msgP.add(l3);
	msgP.add(l4);

	figureP = new JPanel();
	//constraint = new GridBagConstraints();
	//gridbag = new GridBagLayout();
	//figureP.setLayout(gridbag);

	//setup the diagrams		
	distribution = new JPlot2D();
	distribution.setPreferredSize(new Dimension(400,300));    
	//gridbag.setConstraints(distribution,constraint);
	figureP.add(distribution);
		
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
	setTitle("Statistical Infomation");
	//setSize(600, 800);
	//setLocationRelativeTo(parent);
	setLocation(200,50);
	pack();
	addWindowListener(new WindowAdapter()
	    {  public void windowClosing(WindowEvent e)
		{  isbreak = true;
		}
	    } );
    }
    
    /**
       Tsecond constructor
    */
    public VCliqueInfo(String[] msg, Component parent
		       , ArrayList outYear, ArrayList inYear
		       , ArrayList outMonth, ArrayList inMonth
		       , ArrayList outDay, ArrayList inDay){

	msgP = new JPanel();
	int size = msg.length;
	msgP.setLayout(new GridLayout(size+2,1));
	JLabel[] label = new JLabel[size];
	
	for(int i=0;i<size;i++){
		label[i] = new JLabel(msg[i]);
		//label[i].setForeground(Color.blue);
		msgP.add(label[i]);
	}
	

	//the list of details...
	JComboBox y1 = new JComboBox(outYear.toArray());
	JComboBox m1 = new JComboBox(outMonth.toArray());
	JComboBox d1 = new JComboBox(outDay.toArray());
	JComboBox y2 = new JComboBox(inYear.toArray());
	JComboBox m2 = new JComboBox(inMonth.toArray());
	JComboBox d2 = new JComboBox(inDay.toArray());
	y1.setBackground(Color.blue);
	y2.setBackground(Color.red);
	m1.setBackground(Color.blue);
	m2.setBackground(Color.red);
	d1.setBackground(Color.blue);
	d2.setBackground(Color.red);

	JPanel detailP = new JPanel();
	detailP.setLayout(new GridLayout(1,4));
	JLabel outbound = new JLabel("Outbound distribution: ");
	outbound.setForeground(Color.blue);
	detailP.add(outbound);
	detailP.add(y1);
	detailP.add(m1);
	detailP.add(d1);
	JPanel detailP2 = new JPanel();
	detailP2.setLayout(new GridLayout(1,4));
	JLabel inbound = new JLabel("Inbound distribution: ");
	inbound.setForeground(Color.red);
	detailP2.add(inbound);
	detailP2.add(y2);
	detailP2.add(m2);
	detailP2.add(d2);

	msgP.add(detailP);
	msgP.add(detailP2);

	//setup the diagrams
	figureP = new JPanel();
	figureP.setLayout(new GridLayout(2,3));
			
	plots = new JPlot2D[6];
	
	for(int i=0;i<plots.length;i++){
		plots[i] = new JPlot2D();
		plots[i].setPreferredSize(new Dimension(250,250));
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
	setTitle("Statistical Infomation");
	//setSize(600, 800);
	//setLocationRelativeTo(parent);
	setLocation(100,50);
	pack();
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

