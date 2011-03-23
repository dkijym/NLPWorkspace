/*
 * ++Copyright SYSDETECT++
 * 
 * Copyright (c) 2002 System Detection.  All rights reserved.
 * 
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SYSTEM DETECTION.
 * The copyright notice above does not evidence any actual
 * or intended publication of such source code.
 * 
 * Only properly authorized employees and contractors of System Detection
 * are authorized to view, posses, to otherwise use this file.
 * 
 * System Detection
 * 5 West 19th Floor 2 Suite K
 * New York, NY 10011-4240
 * 
 * +1 212 242 2970
 * <sysdetect@sysdetect.org>
 * 
 * --Copyright SYSDETECT--
 */


package metdemo.Tools;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;

public class NonModalMessage extends JDialog
{
  private JOptionPane m_pane;

  public NonModalMessage(String title, String message)
  {
    super();
    
    setTitle(title);

      getContentPane().setLayout(new BorderLayout(0,0));
      m_pane = new JOptionPane(message);
      this.setContentPane(m_pane);
      pack();

      m_pane.addPropertyChangeListener(new PropertyChangeListener()
        {
	  public void propertyChange(PropertyChangeEvent event)
	  {
	    if (event.getPropertyName().equals("value"))
	    {
	      dispose();
	    }
	  }
        });
  }

  public static void showMessageDialog(Component parent, String title, String message)
  {
    NonModalMessage msg = new NonModalMessage(title, message);
    msg.setLocationRelativeTo(parent);
    msg.setVisible(true);
  }

  /*public static void main(String args[])
  {
    NonModalMessage.showMessageDialog(null, "Alert", "hello");
  }*/
}
