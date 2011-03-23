/**
   @version 1.x (How do I know how many times I changed it?)
   @author Wei-Jen Li

   This is for the class 'AddressList'.
   To show diagrams for the Dummy test.
   To simulate and detect the email virus.
*/

package metdemo.Tools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import chapman.graphics.JPlot2D;


public class HellingerTest extends JDialog{  

    private boolean isbreak = false;

    //For the diagrams.
    public JPlot2D hellinger1, hellinger2;//the top 2 diagrams
   public JPlot2D distRcpt50_1, distRcpt50_2;//the middle 2 diagrams
   public JPlot2D distRcpt20_1, distRcpt20_2;//no use now
   public  JPlot2D attach1, attach2;//the buttom 2 diagrams

    GridBagConstraints constraint;
    GridBagLayout gridbag;

    //The panel for the buttons and for the diagrams
    private JPanel buttonP, figureP;
    private JScrollPane centerP;
    
    //The message at the top
    private MessagePane msgP;
	
    /**
       The constructor
       @param msg1 some message shown in the upper panel.
       @param msg2 some message shown in the upper panel.
       @param msg3 some message shown in the upper panel.
       @param value Hellinger Distance of the dummy
    */
    public HellingerTest(String msg1, String msg2
			 , String msg3, double[] value){

	msgP = new MessagePane(msg1, msg2, msg3);
		
	figureP = new JPanel();
	constraint = new GridBagConstraints();
	gridbag = new GridBagLayout();
	figureP.setLayout(gridbag);

	//setup the diagrams		
	hellinger1 = new JPlot2D();
	hellinger2 = new JPlot2D();
	distRcpt50_1 = new JPlot2D();
	distRcpt50_2 = new JPlot2D();
	//distRcpt20_1 = new JPlot2D();
	//distRcpt20_2 = new JPlot2D();
	attach1 = new JPlot2D();
	attach2 = new JPlot2D();
		
	hellinger1.setPreferredSize(new Dimension(250,200));    
	hellinger2.setPreferredSize(new Dimension(250,200));
	distRcpt50_1.setPreferredSize(new Dimension(250,200));
	distRcpt50_2.setPreferredSize(new Dimension(250,200));
	//distRcpt20_1.setPreferredSize(new Dimension(250,200));
	//distRcpt20_2.setPreferredSize(new Dimension(250,200));
	attach1.setPreferredSize(new Dimension(250,200));
	attach2.setPreferredSize(new Dimension(250,200));
		
	constraint.gridx = 0;
	constraint.gridy = 0;
	constraint.insets = new Insets(3,3,3,3);     
	gridbag.setConstraints(hellinger1,constraint);
	figureP.add(hellinger1);
		
		
	constraint.gridx = 1;
	constraint.gridy = 0;
	constraint.insets = new Insets(3,3,3,3);     
	gridbag.setConstraints(hellinger2,constraint);
	figureP.add(hellinger2);
		
	//distRcpt20 is not used now
	/*
	  constraint.gridx = 0;
	  constraint.gridy = 1;
	  constraint.insets = new Insets(3,3,3,3);     
	  gridbag.setConstraints(distRcpt20_1,constraint);
	  figureP.add(distRcpt20_1);
		
	  constraint.gridx = 1;
	  constraint.gridy = 1;
	  constraint.insets = new Insets(3,3,3,3);     
	  gridbag.setConstraints(distRcpt20_2,constraint);
	  figureP.add(distRcpt20_2);
	*/
	constraint.gridx = 0;
	constraint.gridy = 1;
	constraint.insets = new Insets(3,3,3,3);     
	gridbag.setConstraints(distRcpt50_1,constraint);
	figureP.add(distRcpt50_1);
		
	constraint.gridx = 1;
	constraint.gridy = 1;
	constraint.insets = new Insets(3,3,3,3);     
	gridbag.setConstraints(distRcpt50_2,constraint);
	figureP.add(distRcpt50_2);
		
	constraint.gridx = 0;
	constraint.gridy = 2;
	constraint.insets = new Insets(3,3,3,3);     
	gridbag.setConstraints(attach1,constraint);
	figureP.add(attach1);
		
	constraint.gridx = 1;
	constraint.gridy = 2;
	constraint.insets = new Insets (3,3,3,3);     
	gridbag.setConstraints(attach2,constraint);
	figureP.add(attach2);
		
	centerP = new JScrollPane(figureP);
	buttonP = new JPanel();
	JButton close = new JButton("Close");
	close.addActionListener(new ActionListener(){  
		public void actionPerformed(ActionEvent evt){ 
		    close();
		} 
	    } );
		
	buttonP.add(close);
	/*
	JButton result = new JButton("Show Result");
	result.addActionListener(new ActionListener(){  
		public void actionPerformed(ActionEvent evt){ 
		}
	    } );
	buttonP.add(result);
	*/
	getContentPane().add(msgP, "North");
	getContentPane().add(centerP, "Center");
	getContentPane().add(buttonP, "South");
	setTitle("Statistical Analysis");
	//setSize(600, 800);
	setLocation(50,50);
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
	plot.setGridState(JPlot2D.GRID_ON);
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

/**
   Just make the GUI looks better
*/
class MessagePane extends JPanel{
    private String msg1, msg2, msg3;
	
    public MessagePane(String m1, String m2, String m3){
	msg1 = m1;
	msg2 = m2;
	msg3 = m3;
	setLayout(new GridLayout(8,1));
	JLabel l1 = new JLabel(msg1);
	JLabel l2 = new JLabel(msg2);
	JLabel l3 = new JLabel(msg3);
	l1.setForeground(Color.blue);
	l2.setForeground(Color.blue);
	l3.setForeground(Color.red);
	add(l1);
	add(l2);
	add(l3);
	add(new JLabel(""));
	add(new JLabel(""));
	add(new JLabel(""));
	add(new JLabel(""));
	add(new JLabel(""));
    }

    public void paintComponent(Graphics g){
	g.setColor(Color.black);
	g.drawString("Original Emails",75,65);
	g.drawString("Threshold (Moving Average)",75,80);
	g.drawString("Malicious Emails",75,95);
	g.setColor(Color.orange);
	g.fillRect(10,60,50,3);
	g.setColor(Color.blue);
	g.fillRect(10,75,50,3);
	g.setColor(Color.red);
	g.fillRect(10,90,50,3);
    }
    public void draw(){
	repaint();
    }
}

