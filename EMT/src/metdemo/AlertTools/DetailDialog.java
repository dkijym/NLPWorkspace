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


package metdemo.AlertTools;

import java.awt.*;
import javax.swing.*;

/** class to show the alert class in a frame
 */


public class DetailDialog extends JDialog
{
  private static final String TITLE = "Alert Detail";

  private JTextArea m_text;

  public DetailDialog(Frame owner, boolean modal, String detail)
  {
    this(owner, modal, TITLE, detail);
  }

  public DetailDialog(Frame owner, boolean modal, String title, String detail)
  {
    super(owner, title, modal);
    
    m_text = new JTextArea(detail);
    m_text.setLineWrap(false);
    JScrollPane scrollPane = new JScrollPane(m_text);
    getContentPane().add(scrollPane);
    setSize(new Dimension(400,300));
  }

  
}











