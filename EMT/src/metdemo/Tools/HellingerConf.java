/*
 * @version 1.x
 * @author Wei-Jen Li
 *
 * This is for the class 'AddressList'.
 * To setup information for the Hellinger Distance test.
 */

package metdemo.Tools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import metdemo.AlertTools.DetailDialog;
//import java.io.*;

public class HellingerConf extends JDialog{  

    /*
      For entering the testing information.
      Number of dummies, the first dummy location
      time interval, and Hellinger Distance.
    */
    private JTextField dummies, location, timeint, hdistance;

    /*
      Other information.
      block ==> the size for computing moving average xStandard Deviation
      alpah ==> a constant for the threshold
    */
    private JTextField block, alpha;

    /*
      Generate dummy by:
      1. Distince recepients.
      2. With/without attachment.
    */
    private JCheckBox distinct, attachment;

    //to close this panel
    boolean isset = false, isbreak = false;

    private JLabel l1,l2,l3,l4,l5,l6;

    /**
       Constructor

       @param parent the last panel that starts this one
       @param total total number of email data of this user
    */
    public HellingerConf(Component parent, String total){

	/* 
	   This is for configP 
	   It's a little complicated, since I use SpringLayout
	*/
	l1 = new JLabel("Number of Dummies:");
	l2 = new JLabel("Location ( from 0 to "+total+" ):");
	l3 = new JLabel("Time Interval ( in minutes ):");
	l4 = new JLabel("Testing Hellinger Distance:");
	l5 = new JLabel("Size of Moving Average:");
	l6 = new JLabel("Alpha:");
	dummies = new JTextField("50", 10);
	location = new JTextField("300", 10);
	timeint = new JTextField("0", 10);
	hdistance = new JTextField("50", 10);
	block = new JTextField("100", 10);
	alpha = new JTextField("0.2", 10);
	l1.setLabelFor(dummies);
	l2.setLabelFor(location);
	l3.setLabelFor(timeint);
	l4.setLabelFor(hdistance);
	l5.setLabelFor(block);
	l6.setLabelFor(alpha);

	distinct = new JCheckBox("Distinct Recipients");
	distinct.setSelected(true);
	attachment = new JCheckBox("With Attachment");
	attachment.setSelected(true);

	SpringLayout layout = new SpringLayout();
	JPanel configP = new JPanel(layout);
	configP.add(l1);
	configP.add(dummies);
	configP.add(l2);
	configP.add(location);
	configP.add(l3);
	configP.add(timeint);
	configP.add(l4);
	configP.add(hdistance);
	configP.add(l5);
	configP.add(block);
	configP.add(l6);
	configP.add(alpha);

	Spring xSpring = Spring.constant(5);
	Spring ySpring = Spring.constant(5);
	Spring xPadSpring = Spring.constant(5);
	Spring yPadSpring = Spring.constant(5);
	Spring negXPadSpring = Spring.constant(-5);
    
	Spring tmps1 = layout.getConstraint("East", l2);
	Spring tmps2 = layout.getConstraint("East", l3);
	Spring maxEastSpring = Spring.max(tmps1,tmps2);
	SpringLayout.Constraints lastConsL = null;
	SpringLayout.Constraints lastConsR = null;
	Spring parentWidth = layout.getConstraint("East", parent);
	Spring rWidth = null;
	Spring maxHeightSpring = null;
	Spring rX = Spring.sum(maxEastSpring, xPadSpring); //right col location
	Spring negRX = Spring.minus(rX); //negative of rX



	/**************** dummy *********************/
	SpringLayout.Constraints consL 
	    = layout.getConstraints(l1);
	SpringLayout.Constraints consR 
	    = layout.getConstraints(dummies);

	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	consL.setY(ySpring);
	consR.setY(ySpring);
	maxHeightSpring = Spring.sum(ySpring,
				     Spring.max(consL.getHeight(),
						consR.getHeight()));
	lastConsL = consL;
	lastConsR = consR;

	Spring y = null;
	/****************** location ********************/
	consL = layout.getConstraints(l2);
	consR = layout.getConstraints(location);
	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	y = Spring.sum(Spring.max(lastConsL.getConstraint("South"),
				  lastConsR.getConstraint("South")),
		       yPadSpring);
	consL.setY(y);
	consR.setY(y);
	maxHeightSpring = Spring.sum(yPadSpring,
				     Spring.sum(maxHeightSpring,
						Spring.max(
							   consL.getHeight(),
							   consR.getHeight())));
	lastConsL = consL;
	lastConsR = consR;
	/************** time interval *****************/
	consL = layout.getConstraints(l3);
	consR = layout.getConstraints(timeint);
	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	y = Spring.sum(Spring.max(lastConsL.getConstraint("South"),
				  lastConsR.getConstraint("South")),
		       yPadSpring);
	consL.setY(y);
	consR.setY(y);
	maxHeightSpring = Spring.sum(yPadSpring,
				     Spring.sum(maxHeightSpring,
						Spring.max(
							   consL.getHeight(),
							   consR.getHeight())));
	lastConsL = consL;
	lastConsR = consR;
	/*******************************************/


	/*************** Hellinger Distance *****************/
	consL = layout.getConstraints(l4);
	consR = layout.getConstraints(hdistance);
	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	y = Spring.sum(Spring.max(lastConsL.getConstraint("South"),
				  lastConsR.getConstraint("South")),
		       yPadSpring);
	consL.setY(y);
	consR.setY(y);
	maxHeightSpring = Spring.sum(yPadSpring,
				     Spring.sum(maxHeightSpring,
						Spring.max(
							   consL.getHeight(),
							   consR.getHeight())));
	lastConsL = consL;
	lastConsR = consR;
	/*******************************************/

	/************ Size of moving average xStandard Deviation **************/
	consL = layout.getConstraints(l5);
	consR = layout.getConstraints(block);
	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	y = Spring.sum(Spring.max(lastConsL.getConstraint("South"),
				  lastConsR.getConstraint("South")),
		       yPadSpring);
	consL.setY(y);
	consR.setY(y);
	maxHeightSpring = Spring.sum(yPadSpring,
				     Spring.sum(maxHeightSpring,
						Spring.max(
							   consL.getHeight(),
							   consR.getHeight())));
	lastConsL = consL;
	lastConsR = consR;
	/*******************************************/

	/**************** alpha ********************/
	consL = layout.getConstraints(l6);
	consR = layout.getConstraints(alpha);
	consL.setX(xSpring);
	consR.setX(rX);
	rWidth = consR.getWidth();
	consR.setWidth(Spring.sum(Spring.sum(parentWidth, negRX),
				  negXPadSpring));
	y = Spring.sum(Spring.max(lastConsL.getConstraint("South"),
				  lastConsR.getConstraint("South")),
		       yPadSpring);
	consL.setY(y);
	consR.setY(y);
	maxHeightSpring = Spring.sum(yPadSpring,
				     Spring.sum(maxHeightSpring,
						Spring.max(
							   consL.getHeight(),
							   consR.getHeight())));
	lastConsL = consL;
	lastConsR = consR;
	/*******************************************/

	SpringLayout.Constraints consParent = layout.getConstraints(configP);
	consParent.setConstraint("East",
				 Spring.sum(rX, Spring.sum(rWidth, 
							   xPadSpring)));
	consParent.setConstraint("South",
				 Spring.sum(maxHeightSpring, yPadSpring));

	getContentPane().add(configP, "Center");

	/* end of confitP */

	/* setP */
	JPanel setP = new JPanel();
	setP.setLayout(new GridLayout(3,1));
	setP.add(distinct);
	setP.add(attachment);

	//for the 'set' button
	JPanel withButton = new JPanel();
	JButton set = new JButton("Set");
	set.addActionListener(new ActionListener(){  
		public void actionPerformed(ActionEvent evt){ 
		    isset = true;
		} 
	    } );
	withButton.add(set);
	

	//the help information
	/*
	JButton help = new JButton("Help");
	help.setBackground(Color.orange);
	help.addActionListener(new ActionListener(){  
		public void actionPerformed(ActionEvent evt){ 
		    showhelp();
		    
		} 
	    } );
	withButton.add(help);
	*/
	EMTHelp emthelp = new EMTHelp(EMTHelp.HELLINGERCONF);
	withButton.add(emthelp);
	setP.add(withButton);
	getContentPane().add(setP, "South");

	setSize(330, 350);
	setTitle("Virus Simulation Configuration");
	setLocationRelativeTo(parent);
	addWindowListener(new WindowAdapter()
	    {  public void windowClosing(WindowEvent e)
		{  isbreak = true;
		}
	    } );
    }

    public void showhelp(){
	/*
	String file = "metdemo/HellingerConfHelp.txt";
	String message = "";
	try{
	    BufferedReader in = 
		new BufferedReader(new FileReader(file));
	    
	    while(in.ready()){
		message += in.readLine()+"\n";
	    }
	}
	catch(Exception e){ 
	    System.out.println("reaf file error:"+file+" "+e);
	}
	*/
	String message = EMTHelp.getHellingerConf();
	String title = "Help";
	DetailDialog dialog = new DetailDialog(null, false, title, message);
	dialog.setLocationRelativeTo(this);
	dialog.setVisible(true);
    }

    /**
       Return the number of dummies.
       @return number of dummies.
     */
    public int numOfDummy(){
	return Integer.parseInt(dummies.getText());
    }
    
    /**
       Return the location of the first dummy.
       @return location of the first dummy.
     */
    public int getLoc(){
	return Integer.parseInt(location.getText());
    }

    /**
       Return the time interval.
       @return time interval.
     */
    public long getT(){
	return Long.parseLong(timeint.getText());
    }

    /**
       Return the Hellinger Distance.
       @return Hellinger Distance.
     */
    public int getHD(){
	return Integer.parseInt(hdistance.getText());
    }

    /**
       Return the size for computing moving average xStandard Deviation.
       @return size for computing moving average xStandard Deviation.
     */
    public int getSDsize(){
	return Integer.parseInt(block.getText());
    }

    /**
       Return the Alpha value.
       @return Alpha.
     */
    public double getAlpha(){
	return Double.parseDouble(alpha.getText());
    }

    /**
       Return Distinct.
       @return Distinct.
     */
    public boolean isDist(){
	return distinct.isSelected();
    }
    
    /**
       Return with/without attachment.
       @return with/without attachment.
     */
    public boolean isAttach(){
	return attachment.isSelected();
    }

    /**
       Return whether the user press the 'set' button.
       @return press the 'set' button.
     */
    public boolean isSet(){
	return isset;
    }
    
    /**
       This window is closed.
       @return window is closed.
     */
    public boolean isBreak(){
	return isbreak;
    }

    /**
       To close this window.
     */
    public void close(){
	setVisible(false);
    }

}
