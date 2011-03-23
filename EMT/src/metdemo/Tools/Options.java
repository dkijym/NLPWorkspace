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
import java.awt.event.*;
import javax.swing.*;

import metdemo.AlertTools.Alert;
import metdemo.Attach.AttachmentAlertOptions;
//import javax.swing.event.*;
//import java.util.*;
//import java.io.*;


public class Options extends JPanel
{
    public final static int INCIDENCE = 0;
    public final static int BIRTH_RATE = 1;
    public final static int LIFESPAN = 2;
    public final static int INCIDENT_RATE = 3;
    public final static int DEATH_RATE = 4;
    public final static int PREVELANCE = 5;
    public final static int THREAT = 6;
    public final static int SPREAD = 7;

    public final static int AND = 0;
    public final static int OR = 1;

    private final static String ATTACHMENT_INTRO = "Alert When:";
    private final static String BEHAVIOR_INTRO = "<html>Enter the number of standard deviations by which<p>use must exceed average for each class of alert.</html>";
    private final static String BURST_PERIODS[] = {"Minute",
						   "Hour",
						   "Day"};
    private final static String ALERT_LEVELS[] = {"Yellow",
						  "Red"};
    private final static int BURST_FIELD_WIDTH = 4;
    private final static double YELLOW_DEFAULT = 20;
    private final static double RED_DEFAULT = 40;

    private JTabbedPane m_tabs;

    private JTextField m_textBursts[];

    private double m_minYellow, m_minRed;
    private double m_hourYellow, m_hourRed;
    private double m_dayYellow, m_dayRed;

    private String m_wordlist;
    private String m_stoplist;
    private AttachmentAlertOptions m_attachmentAlerts;
    private KeywordPanel m_keywords;
    private KeywordPanel m_stopwords;

    public Options(String wordlist,String stoplist)
    {
	m_wordlist = wordlist;
	m_stoplist = stoplist;
	m_textBursts = new JTextField[6];

	GridBagLayout gridbag = new GridBagLayout();
	setLayout(gridbag);
	GridBagConstraints constraints = new GridBagConstraints();
	m_tabs = new JTabbedPane();

	// make attachment alerts panel
	m_attachmentAlerts = new AttachmentAlertOptions();
	m_tabs.addTab("Attachments", m_attachmentAlerts);

	// make user behavior alerts panel
	layoutBurstAlerts();

	// make keywords panel
	m_keywords = new KeywordPanel(wordlist);
	m_tabs.addTab("Keywords", m_keywords);
	//make stop panel
	m_stopwords = new KeywordPanel(stoplist);
	m_tabs.addTab("Stopwords", m_stopwords);
    
    
	// add tabbed pane to this panel
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	gridbag.setConstraints(m_tabs, constraints);
	add(m_tabs);

	// add ok and cancel buttons
	JButton buttonOk = new JButton("OK");
	buttonOk.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    if (validateInput())
			{
			    saveInput();
			    Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(JDialog.class, Options.this);
			    if (d == null)
				{
				    System.err.println("Could not close dialog!");
				}
			    else
				{
				    d.setVisible(false);
				    d.dispose();
				}
			}
		}
	    });
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.weightx = 1.0;
	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.anchor = GridBagConstraints.SOUTHEAST;
	gridbag.setConstraints(buttonOk, constraints);
	add(buttonOk);

	JButton buttonCancel = new JButton("Cancel");
	buttonCancel.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    cancelChanges();
		    Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(JDialog.class, Options.this);
		    if (d == null)
			{
			    System.err.println("Could not close dialog!");
			}
		    else
			{
			    d.setVisible(false);
			    d.dispose();
			}
		}
	    });
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.weightx = 0.0;
	gridbag.setConstraints(buttonCancel, constraints);
	add(buttonCancel);

	// initialize data
	m_minYellow = m_hourYellow = m_dayYellow = YELLOW_DEFAULT;
	m_minRed = m_hourRed = m_dayRed = RED_DEFAULT;
    }

    public void cancelChanges()
    {
	m_stopwords.cancelChanges();  
	m_keywords.cancelChanges();
	m_attachmentAlerts.cancelChanges();

	m_textBursts[0].setText(Double.toString(m_minYellow));
	m_textBursts[1].setText(Double.toString(m_minRed));
	m_textBursts[2].setText(Double.toString(m_hourYellow));
	m_textBursts[3].setText(Double.toString(m_hourRed));
	m_textBursts[4].setText(Double.toString(m_dayYellow));
	m_textBursts[5].setText(Double.toString(m_dayRed));
    }

    /**
     * @return true if all fields were valid 
     */
    public boolean validateInput()
    {
	if (!m_attachmentAlerts.validateInput())
	    {
		return false;
	    }
    
	String badField = "";
	try
	    {
		badField = "minute yellow";
		Double.parseDouble(m_textBursts[0].getText());
		badField = "minute red";
		Double.parseDouble(m_textBursts[1].getText());
		badField = "hour yellow";
		Double.parseDouble(m_textBursts[2].getText());
		badField = "hour red";
		Double.parseDouble(m_textBursts[3].getText());
		badField = "day yellow";
		Double.parseDouble(m_textBursts[4].getText());
		badField = "day red";
		Double.parseDouble(m_textBursts[5].getText());
	    }
	catch (NumberFormatException e)
	    {
		JOptionPane.showMessageDialog(this, "Please supply a valid value for the " + badField + " alert threshold.");
		return false;
	    }

	return true;
    }

    public void saveInput()
    {
	// error handling should be extraneous if called immediately after validate

	m_keywords.saveInput();
	m_attachmentAlerts.saveInput();

	String badField = "";
	try
	    {
		badField = "minute yellow";
		m_minYellow = Double.parseDouble(m_textBursts[0].getText());
		badField = "minute red";
		m_minRed = Double.parseDouble(m_textBursts[1].getText());
		badField = "hour yellow";
		m_hourYellow = Double.parseDouble(m_textBursts[2].getText());
		badField = "hour red";
		m_hourRed = Double.parseDouble(m_textBursts[3].getText());
		badField = "day yellow";
		m_dayYellow = Double.parseDouble(m_textBursts[4].getText());
		badField = "day red";
		m_dayRed = Double.parseDouble(m_textBursts[5].getText());
	    }
	catch (NumberFormatException e)
	    {
		JOptionPane.showMessageDialog(this, "Please supply a valid value for the " + badField + " alert threshold.");
	    }
    }

    private void layoutBurstAlerts()
    {
	JPanel burstAlerts = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	burstAlerts.setLayout(gridbag);
	GridBagConstraints constraints = new GridBagConstraints();

	JLabel labelIntro = new JLabel(BEHAVIOR_INTRO);
	constraints.weightx = 1.0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 3;
	constraints.insets = new Insets(5, 5, 20, 5);
	gridbag.setConstraints(labelIntro, constraints);
	burstAlerts.add(labelIntro);
    
	constraints.gridwidth = 1;
	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.gridy++;
	for (int i=0; i<BURST_PERIODS.length; i++, constraints.gridy++)
	    {
		JLabel labelPeriod = new JLabel(BURST_PERIODS[i]);
		constraints.gridx = 0;
		gridbag.setConstraints(labelPeriod, constraints);
		burstAlerts.add(labelPeriod);

		JLabel labelLevel1 = new JLabel(ALERT_LEVELS[0]);
		constraints.gridx = 1;
		gridbag.setConstraints(labelLevel1, constraints);
		burstAlerts.add(labelLevel1);

		m_textBursts[i*2] = new JTextField(Double.toString(YELLOW_DEFAULT), BURST_FIELD_WIDTH);
		constraints.gridx = 2;
		if (i > 1)
		    {
			gridbag.setConstraints(m_textBursts[i*2], constraints);
			burstAlerts.add(m_textBursts[i*2]);
		    }
		else
		    {
			// disable slow ones
			JLabel disabledLabel = new JLabel("disabled");
			gridbag.setConstraints(disabledLabel, constraints);
			burstAlerts.add(disabledLabel);
		    }

		JLabel labelLevel2 = new JLabel(ALERT_LEVELS[1]);
		constraints.gridx = 1;
		constraints.gridy++;
		gridbag.setConstraints(labelLevel2, constraints);
		burstAlerts.add(labelLevel2);

		m_textBursts[i*2+1] = new JTextField(Double.toString(RED_DEFAULT), BURST_FIELD_WIDTH);
		constraints.gridx = 2;
		if (i > 1)
		    {
			gridbag.setConstraints(m_textBursts[i*2+1], constraints);
			burstAlerts.add(m_textBursts[i*2+1]);
		    }
		else
		    {
			// disable slow ones
			JLabel disabledLabel = new JLabel("disabled");
			gridbag.setConstraints(disabledLabel, constraints);
			burstAlerts.add(disabledLabel);
		    }
	    }

	m_tabs.addTab("Account Behavior", burstAlerts);
    }

    // accessors
    public AttachmentAlertOptions getAttachmentAlertOptions()
    {
	return m_attachmentAlerts;
    }

    public double getMinYellowThreshold()
    {
	return m_minYellow;
    }
  
    public double getMinRedThreshold()
    {
	return m_minRed;
    }

    public double getHourYellowThreshold()
    {
	return m_hourYellow;
    }

    public double getHourRedThreshold()
    {
	return m_hourRed;
    }

    public double getDayYellowThreshold()
    {
	return m_dayYellow;
    }

    public double getDayRedThreshold()
    {
	return m_dayRed;
    }

    public double getThreshold(int level, int period)
    {
	if (Alert.ALERTLEVEL_YELLOW == level)
	    {
		switch(period)
		    {
		    case 60:
			return getMinYellowThreshold();
		    case 60*60:
			return getHourYellowThreshold();
		    case 60*60*24:
			return getDayYellowThreshold();
		    default:
			System.err.println("bad period in getThreshold");
			return 0;
		    }
	    }
	else // (Alert.ALERTLEVEL_RED == level)
	    {
		switch(period)
		    {
		    case 60:
			return getMinRedThreshold();
		    case 60*60:
			return getHourRedThreshold();
		    case 60*60*24:
			return getDayRedThreshold();
		    default:
			System.err.println("bad period in getThreshold");
			return 0;
		    }
	    }
    }


    private class AlertConstraint extends JPanel
    {
	JComboBox m_comboMetric;
	JTextField m_textValue;
    
	public AlertConstraint(String metrics[], String comparison)
	{
	    setLayout(new FlowLayout());

	    m_comboMetric = new JComboBox(metrics);
	    add(m_comboMetric);
      
	    add(new JLabel(comparison));

	    m_textValue = new JTextField(5);
	    add(m_textValue);
	}
    }





    /*public static void main(String[] argv)
    {
	JDialog dialog = new JDialog((Frame) null, true);
	dialog.setTitle("Alert Options");
	Options options = new Options(argv[0],argv[1]);

	dialog.setContentPane(options);
	dialog.pack();
	dialog.setLocationRelativeTo(null);
	dialog.setVisible(false);
	System.exit(0);
    }*/
}
