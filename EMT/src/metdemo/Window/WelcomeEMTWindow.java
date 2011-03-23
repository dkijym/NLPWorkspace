/**
 * 
 */
package metdemo.Window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import metdemo.EMTConfiguration;
import metdemo.winGui;

// TODO: Auto-generated Javadoc
/**
 * The Class WelcomeEMTWindow.
 *
 * @author shlomo
 * 
 * This window will show all the other windows in a clickable format and small picture to allow
 * navigation by clustering similiar windows
 */
public class WelcomeEMTWindow extends JPanel implements ActionListener {

	/** The m_wingui handle. */
	private winGui m_winguiHandle;
//	need to add drop down list for choosing starting window
	/** The combo box items. */
private final String comboBoxItems[] = {winGui.st_WelcomeWindow , winGui.st_LoadWindow , winGui.st_MessageWindow ,
			winGui.st_ConfigWindow, winGui.st_UsageWindow , winGui.st_EnclaveWindow , winGui.st_UserCliqWindow,
			   winGui.st_SimilarWindow , winGui.st_VIPWindow , winGui.st_RCPTWindow , winGui.st_AttachWindow ,
			   winGui.st_FlowWindow , winGui.st_GraphicCliqWindow, winGui.st_VirusWindow, winGui.st_AttachVirAnalyzerWindow ,
			   winGui.st_ReportWindow , winGui.st_SQLWindow , winGui.st_ExperimentWindow , winGui.st_SearchWindow ,
			   winGui.st_SocNetWindow , winGui.st_SocHeirWindow, winGui.st_docFlowWindow	};
	
	/** The welcome combo. */
	final JComboBox welcomeCombo = new JComboBox(comboBoxItems);
	
	/**
	 * Main constr for the welcome screen.
	 *
	 * @param mainWingui the main wingui
	 * @param emtc the emtc
	 */
	public WelcomeEMTWindow(final winGui mainWingui,final EMTConfiguration emtc){
	
		m_winguiHandle = mainWingui;
		this.setLayout(new BorderLayout());
		
		JLabel toplabel = new JLabel("EMT Main Menu Screen");
		toplabel.setForeground(Color.BLUE);
		toplabel.setFont(new Font("Helvetica",Font.ITALIC + Font.BOLD,60));
		
		
		JPanel topPanel = new JPanel();
		topPanel.add(toplabel);
		this.add(topPanel,BorderLayout.NORTH);
		
		JPanel middlePanel = new JPanel();
		
		//this is for message based windows
		JPanel MessageBased = new JPanel();
		MessageBased.setLayout(new GridLayout(7,1));
		//MessageBased.setPreferredSize(new Dimension(250,300));
		Border bd1 =    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
		TitledBorder ttl1 = new TitledBorder(bd1,"EMail Message Based");
		MessageBased.setBorder(ttl1);
		JButton aButton = new JButton(winGui.st_LoadWindow);
		aButton.setToolTipText("Load raw data into EMT here");
		aButton.addActionListener(this); 
		MessageBased.add(aButton);
		aButton = new JButton(winGui.st_MessageWindow);
		aButton.setToolTipText("See and organize the messages");
		aButton.addActionListener(this);
		MessageBased.add(aButton);
		aButton= new JButton(winGui.st_AttachWindow);
		aButton.setToolTipText("Analyze specific message patterns");
		aButton.addActionListener(this); 
		MessageBased.add(aButton);
		aButton = new JButton(winGui.st_FlowWindow);
		aButton.setToolTipText("Load raw data into EMT here");
		aButton.addActionListener(this); 
		MessageBased.add(aButton);
		aButton = new JButton(winGui.st_AttachVirAnalyzerWindow);
		aButton.addActionListener(this); 
		MessageBased.add(aButton);
		aButton = new JButton(winGui.st_VirusWindow);
		aButton.addActionListener(this); 
		MessageBased.add(aButton);
		
		//this is for user based analyais
		JPanel userBased = new JPanel();
		userBased.setLayout(new GridLayout(7,1));
		//userBased.setPreferredSize(new Dimension(250,300));
		 bd1 =    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
		 ttl1 = new TitledBorder(bd1,"User Based");
		 userBased.setBorder(ttl1);

		 aButton = new JButton(winGui.st_UsageWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		aButton = new JButton(winGui.st_EnclaveWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		aButton = new JButton(winGui.st_UserCliqWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		aButton = new JButton(winGui.st_SimilarWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		aButton = new JButton(winGui.st_RCPTWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		aButton = new JButton(winGui.st_VIPWindow);
		aButton.addActionListener(this); 
		userBased.add(aButton);
		
		

//		this is for user based analyais
		JPanel socialBased = new JPanel();
		socialBased.setLayout(new GridLayout(7,1));
		//socialBased.setPreferredSize(new Dimension(250,300));
		 bd1 =    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
		 ttl1 = new TitledBorder(bd1,"Social Networks");
		 socialBased.setBorder(ttl1);

		 aButton = new JButton(winGui.st_GraphicCliqWindow);
		aButton.addActionListener(this); 
		socialBased.add(aButton);
		
		aButton = new JButton(winGui.st_SocNetWindow);
		aButton.addActionListener(this); 
		socialBased.add(aButton);
		
		aButton = new JButton(winGui.st_SocHeirWindow);
		aButton.addActionListener(this); 
		socialBased.add(aButton);
		
		aButton = new JButton(winGui.st_docFlowWindow);
		aButton.addActionListener(this); 
		socialBased.add(aButton);
		

		JPanel configBased = new JPanel();
		configBased.setLayout(new GridLayout(7,1));
		//configBased.setPreferredSize(new Dimension(250,300));
		 bd1 =    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
		 ttl1 = new TitledBorder(bd1,"Config and Misc");
		 configBased.setBorder(ttl1);

		 aButton = new JButton(winGui.st_ConfigWindow);
		aButton.addActionListener(this); 
		configBased.add(aButton);
		
		aButton = new JButton(winGui.st_SQLWindow);
		aButton.addActionListener(this); 
		configBased.add(aButton);
		
		aButton = new JButton(winGui.st_ExperimentWindow);
		aButton.addActionListener(this); 
		configBased.add(aButton);
		
		aButton = new JButton(winGui.st_ReportWindow);
		aButton.addActionListener(this); 
		configBased.add(aButton);
	
		//add in the panels
		middlePanel.add(MessageBased);
		middlePanel.add(userBased);
		middlePanel.add(socialBased);
		middlePanel.add(configBased);
		this.add(middlePanel,BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		 bd1 =    BorderFactory.createEtchedBorder(Color.white,new Color(0,0,0));
		 ttl1 = new TitledBorder(bd1,"Choose Starting Screen");
		 bottomPanel.setBorder(ttl1);
		
		
		
		
		
		int windowStart =0;
				
		if(emtc.getProperty(EMTConfiguration.STARTWINDOW)!=null){
			
			windowStart = Integer.parseInt(emtc.getProperty(EMTConfiguration.STARTWINDOW));
			
			
		}
		
		
		
		
		welcomeCombo.setSelectedIndex(windowStart);
		bottomPanel.add(new JLabel("Which window should EMT start in: "));
		bottomPanel.add(welcomeCombo);
		
		
		//define an action on the drop box
		
		welcomeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			
				int choose = welcomeCombo.getSelectedIndex();
				System.out.println("you choose:" + choose);
				emtc.putProperty(EMTConfiguration.STARTWINDOW, ""+choose);
                emtc.saveFileToDisk(emtc.getProperty(EMTConfiguration.CONFIGNAME));

			}
		});
		
		
		
		
		
		
		
		
		
		
		//add it to the screen
		this.add(bottomPanel,BorderLayout.SOUTH);
		
	}

	/**
	 * Gets the choose window.
	 *
	 * @return the choose window
	 */
	public int getChooseWindow(){
		return welcomeCombo.getSelectedIndex();
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
	
		m_winguiHandle.setWindow(arg0.getActionCommand());
		
	}
}
