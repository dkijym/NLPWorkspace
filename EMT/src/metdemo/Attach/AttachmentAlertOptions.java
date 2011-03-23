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


package metdemo.Attach;


import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import metdemo.Tables.EachRowEditor;
import metdemo.Tables.TableUtils;


public class AttachmentAlertOptions extends JPanel
{
  // if you change the order of these, be sure to change validateAndSave()
  private final static String ATTACHMENT_METRICS[] = {"overall birthrate/min",
						      "overall birthrate/hour",
						      "overall birthrate/day",
						      "internal birthrate/min",
						      "internal birthrate/hour",
						      "internal birthrate/day",
						      "internal speed (incidents/min)",
						      "overall speed (incidents/min)",
						      "internal saturation",
						      "overall saturation",
						      "size (bytes)",
						      "lifespan (minutes)",
						      "origin"};


  private AttachmentOptionsTableModel m_tableModel;
  private JComboBox m_comboOriginYellow;
  private JComboBox m_comboOriginRed;

  // each array has two slots 0=>YELLOW 1=>RED
  private long[] m_ovBirthrateMin = {Long.MAX_VALUE, Long.MAX_VALUE}; 
  private long[] m_ovBirthrateHour = {Long.MAX_VALUE, Long.MAX_VALUE};
  private long[] m_ovBirthrateDay = {Long.MAX_VALUE, Long.MAX_VALUE};
  private long[] m_inBirthrateMin = {Long.MAX_VALUE, Long.MAX_VALUE};
  private long[] m_inBirthrateHour = {Long.MAX_VALUE, Long.MAX_VALUE};
  private long[] m_inBirthrateDay = {Long.MAX_VALUE, Long.MAX_VALUE};
  private double[] m_inSaturation = {Double.MAX_VALUE, Double.MAX_VALUE};
  private double[] m_ovSaturation = {Double.MAX_VALUE, Double.MAX_VALUE};
  private double[] m_inSpeed = {Double.MAX_VALUE, Double.MAX_VALUE};
  private double[] m_ovSpeed = {Double.MAX_VALUE, Double.MAX_VALUE};
  private double[] m_size = {0, 0};
  private long[] m_lifespan = {Long.MAX_VALUE, Long.MAX_VALUE};
  private boolean[] m_mustBeExternal = {false, false};
  private boolean[] m_mustBeInternal = {false, false};

  public AttachmentAlertOptions()
  {
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);
    GridBagConstraints constraints = new GridBagConstraints();

    JLabel labelIntro = new JLabel("Alert When: ");
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.insets = new Insets(5, 5, 20, 5);
    gridbag.setConstraints(labelIntro, constraints);
    add(labelIntro);

    m_tableModel = new AttachmentOptionsTableModel();
    JTable table = new JTable(m_tableModel);
    for (int i=0; i<table.getColumnCount(); i++)
    {
      table.getColumnModel().getColumn(i).setCellRenderer(new AttachmentOptionsCellRenderer());
    }

    m_comboOriginYellow = new JComboBox();
    m_comboOriginYellow.addItem("External");
    m_comboOriginYellow.addItem("Internal");
    m_comboOriginYellow.addItem("Either");

    m_comboOriginRed = new JComboBox();
    m_comboOriginRed.addItem("External");
    m_comboOriginRed.addItem("Internal");
    m_comboOriginRed.addItem("Either");

    EachRowEditor rowEditorYellow = new EachRowEditor();
    rowEditorYellow.add(ATTACHMENT_METRICS.length - 1, new DefaultCellEditor(m_comboOriginYellow));
    table.getColumnModel().getColumn(2).setCellEditor(rowEditorYellow);

    EachRowEditor rowEditorRed = new EachRowEditor();
    rowEditorRed.add(ATTACHMENT_METRICS.length - 1, new DefaultCellEditor(m_comboOriginRed));
    table.getColumnModel().getColumn(3).setCellEditor(rowEditorRed);

    table.setValueAt("Either", ATTACHMENT_METRICS.length - 1, 2);
    table.setValueAt("Either", ATTACHMENT_METRICS.length - 1, 3);

    // put dummy values in last columns for layout
    m_tableModel.setValueAt("12345678", 0, 2);
    m_tableModel.setValueAt("12345678", 0, 3);
    TableUtils.setPreferredCellSizes(table);
    m_tableModel.setValueAt("", 0, 2);
    m_tableModel.setValueAt("", 0, 3);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.insets = new Insets(5, 5, 5, 5);
    gridbag.setConstraints(table, constraints);
    add(table);
  }

  /**
   * @return true if all fields were valid
   */
  public boolean validateInput()
  {
    // all fields are already validated as they are entered
    return true;
  }

  public void saveInput()
  {
    int row = 0;
    try
    {
      for (int i=0; i<2; i++)
      {
	row = 0;
	m_ovBirthrateMin[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_ovBirthrateHour[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_ovBirthrateDay[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_inBirthrateMin[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_inBirthrateHour[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_inBirthrateDay[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	m_inSpeed[i] = m_tableModel.getDoubleAt(row++, i+2, Double.MAX_VALUE);
	m_ovSpeed[i] = m_tableModel.getDoubleAt(row++, i+2, Double.MAX_VALUE);
	m_inSaturation[i] = m_tableModel.getDoubleAt(row++, i+2, Double.MAX_VALUE);
	m_ovSaturation[i] = m_tableModel.getDoubleAt(row++, i+2, Double.MAX_VALUE);
	m_size[i] = m_tableModel.getDoubleAt(row++, i+2, 0);
	m_lifespan[i] = m_tableModel.getLongAt(row++, i+2, Long.MAX_VALUE);
	String origin = (String) m_tableModel.getValueAt(row++, i+2);
	m_mustBeExternal[i] = origin.equals("External");
	m_mustBeInternal[i] = origin.equals("Internal");
      }
    }
    catch (NumberFormatException ex)
    {
      JOptionPane.showMessageDialog(this, "Invalid value at row " + (row-1) + " of the attachment options table");
    }
    catch (ClassCastException ex)
    {
      JOptionPane.showMessageDialog(this, "Unexpected bad value for attachment origin " + ex);
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Unexpected error: " + ex);
    }
  }

  public void cancelChanges()
  {
    for (int i=0; i<2; i++)
    {
      int row = 0;
      m_tableModel.setValueAt(Long.toString(m_ovBirthrateMin[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_ovBirthrateHour[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_ovBirthrateDay[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_inBirthrateMin[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_inBirthrateHour[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_inBirthrateDay[i]), row++, i+2);
      m_tableModel.setValueAt(Double.toString(m_inSpeed[i]), row++, i+2);
      m_tableModel.setValueAt(Double.toString(m_ovSpeed[i]), row++, i+2);
      m_tableModel.setValueAt(Double.toString(m_inSaturation[i]), row++, i+2);
      m_tableModel.setValueAt(Double.toString(m_ovSaturation[i]), row++, i+2);
      m_tableModel.setValueAt(Double.toString(m_size[i]), row++, i+2);
      m_tableModel.setValueAt(Long.toString(m_lifespan[i]), row++, i+2);
      if (m_mustBeExternal[i])
      {
	m_tableModel.setValueAt("External", row++, i+2);
      }
      else if (m_mustBeInternal[i])
      {
	m_tableModel.setValueAt("Internal", row++, i+2);
      }
      else
      {
	m_tableModel.setValueAt("Either", row++, i+2);
      }
    }
  }


  // accessors
  public long[] getOvBirthrateMin()
  {
    return m_ovBirthrateMin;
  }

  public long[] getOvBirthrateHour()
  {
    return m_ovBirthrateHour;
  }

  public long[] getOvBirthrateDay()
  {
    return m_ovBirthrateDay;
  }

  public long[] getInBirthrateMin()
  {
    return m_inBirthrateMin;
  }

  public long[] getInBirthrateHour()
  {
    return m_inBirthrateHour;
  }

  public long[] getInBirthrateDay()
  {
    return m_inBirthrateDay;
  }

  public long[] getLifespan()
  {
    return m_lifespan;
  }

  public double[] getInSaturation()
  {
    return m_inSaturation;
  }

  public double[] getOvSaturation()
  {
    return m_ovSaturation;
  }

  public double[] getInSpeed()
  {
    return m_inSpeed;
  }

  public double[] getOvSpeed()
  {
    return m_ovSpeed;
  }

  public double[] getAttSize()
  {
    return m_size;
  }

  public boolean[] getMustBeExternal()
  {
    return m_mustBeExternal;
  }

  public boolean[] getMustBeInternal()
  {
    return m_mustBeInternal;
  }

  

  private class AttachmentOptionsTableModel extends DefaultTableModel
  {
    public AttachmentOptionsTableModel()
    {
      super(ATTACHMENT_METRICS.length, 4);

      for (int i=0; i<ATTACHMENT_METRICS.length; i++)
      {
	if (i == 0)
	{
	  setValueAt("( " + ATTACHMENT_METRICS[i], i, 0);
	}
	else if (i < ATTACHMENT_METRICS.length - 3)
	{
	  setValueAt("OR " + ATTACHMENT_METRICS[i], i, 0);
	}
	else if (i == ATTACHMENT_METRICS.length - 3)
	{
	  setValueAt(") AND " + ATTACHMENT_METRICS[i], i, 0);	  
	}
	else
	{
	  setValueAt("AND " + ATTACHMENT_METRICS[i], i, 0);
	}

	setValueAt(">=", i, 1);
      }
      setValueAt("<=", ATTACHMENT_METRICS.length-2, 1);
      setValueAt("=", ATTACHMENT_METRICS.length-1, 1);
    }

    public boolean isCellEditable(int row, int column)
    {
      if (column > 1)
      {
	return true;
      }
      else
      {
	return false;
      }
    }

    public void setValueAt(Object value, int row, int column)
    {
      if ((column > 1) && (row < ATTACHMENT_METRICS.length - 1) && (((String) value).length() > 0))
      {
	try
	{
	  if ((row < 6) || (row == (ATTACHMENT_METRICS.length - 2)))
	  {
	    // birthrates, lifespan
	    long l = Long.parseLong((String) value);
	    if (l == Long.MAX_VALUE)
	    {
	      value = "";
	    }
	  }
	  else if (row == (ATTACHMENT_METRICS.length - 3))
	  {
	    // size
	    double d = Double.parseDouble((String) value);
	    if (d == 0)
	    {
	      value = "";
	    }
	  }
	  else
	  {
	    double d = Double.parseDouble((String) value);
	    if (d == Double.MAX_VALUE)
	    {
	      value = "";
	    }
	  }
	}
	catch (NumberFormatException ex)
	{
	  JOptionPane.showMessageDialog(null, "Please enter a valid number");
	  return;
	}
      }
      
      super.setValueAt(value, row, column);
    }

    public double getDoubleAt(int row, int column, double def) throws NumberFormatException
    {
      try
      {
	String s = (String) getValueAt(row, column);
	
	if ((s == null) || (0 == s.length()))
	{
	  return def;
	}

	return Double.parseDouble(s);
      }
      catch (ClassCastException ex)
      {
	return def;
      }
    }

    public long getLongAt(int row, int column, long def) throws NumberFormatException
    {
      try
      {
	String s = (String) getValueAt(row, column);
	
	if ((s == null) || (0 == s.length()))
	{
	  return def;
	}

	return Long.parseLong(s);
      }
      catch (ClassCastException ex)
      {
	return def;
      }
    }
  }


  private class AttachmentOptionsCellRenderer extends DefaultTableCellRenderer
  {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
						   boolean hasFocus, int row, int column)
    {
      Component comp = super.getTableCellRendererComponent(table, value, false, false, row, column);

      switch (column)
      {
	case 1:
	  if (value instanceof java.lang.String)
	  {
	    return new JLabel((String) value, JLabel.CENTER);
	  }
	  break;
	case 2:
	  comp.setBackground(Color.YELLOW);
	  break;
	case 3:
	  comp.setForeground(Color.WHITE);
	  comp.setBackground(Color.RED);
	  break;
	default:
      }
      
      return comp;
    }
  }
}
