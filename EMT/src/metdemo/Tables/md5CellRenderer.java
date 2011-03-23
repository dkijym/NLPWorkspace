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


package metdemo.Tables;

import java.security.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class md5CellRenderer extends DefaultTableCellRenderer implements ListCellRenderer
{
  private boolean m_bPrivatized;
  private DefaultListCellRenderer m_listRenderer;

  /**
   * @param privatized initial privacy setting
   */
  public md5CellRenderer(boolean privatized)
  {
    m_bPrivatized = privatized;
    m_listRenderer = new DefaultListCellRenderer();
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
						 boolean hasFocus, int row, int column)
  {

      //      if(isSelected)
      //	  setBackground(Color.yellow);
      //else
	  if (1==row % 2)
	      this.setBackground(Color.lightGray);//isSelected ? Color.yellow :
	  else
	      this.setBackground(Color.WHITE);
      //isSelected ? Color.yellow :
    //Component comp;
    return super.getTableCellRendererComponent(table, 
							 (m_bPrivatized && (value != null)) ? md5sum((String) value) : value, 
							 isSelected, hasFocus, row, column);
    

    //return comp;
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, 
						boolean isSelected, boolean cellHasFocus) 
  {
      //Component comp =
      return  m_listRenderer.getListCellRendererComponent(list, 
								 (m_bPrivatized && (value != null)) ? md5sum((String) value) : value, 
								 index, isSelected, cellHasFocus);
      //return comp;
  }

  public final void setPrivatized(boolean privatized)
  {
    m_bPrivatized = privatized;
  }

  public final boolean getPrivatized()
  {
    return m_bPrivatized;
  }

  // returns a hash only if we're in private mode
  public final String privatize(String s)
  {
    return(m_bPrivatized ? md5sum(s) : s);
  }

  private final String md5sum(String inputtext)
  {
    try
    {
      // get Instance from Java Security Classes
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      
      StringBuffer sb = new StringBuffer();

      byte[] md5rslt = md5.digest( inputtext.getBytes() );

      for (int i=0 ; i<md5rslt.length ; i++)
      {
	if ((0xff & md5rslt[i]) < 16)
	{
	  // add leading zero if value is a single digit
	  sb.append("0");
	}
	sb.append(Integer.toHexString( (0xff & md5rslt[i])));
      } 

      return sb.toString();
    }
    catch(NoSuchAlgorithmException ex)
    {
      System.err.println(ex);
      return null;
    }
  }
  


}
