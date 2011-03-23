/*
 * Created on Jan 5, 2005
 *
 *@Auhtor Shlomo Hershkop
 *
 * 
 *moved out from the message table class. so that we can generalize it elsewhere. 
 *  
 */
package metdemo.MachineLearning;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import metdemo.winGui;
import metdemo.Tables.MessageWindow;
import metdemo.Tools.EMTHelp;

/**
 * @author Shlomo Hershkop
 * @date jan 5 2005.
 *
 * 
 * Will allow the user to specify specific feature which will control the machine leared model building process.
 */

public class ModelBuilder extends JDialog implements ActionListener {

	public static final int BUILD = 0;
	public static final int EVALUATE = 1;
	private JTextField model_name;
	private JButton m_go, openButton;
	private MessageWindow parent;
	private JComboBox comboBox;
	private JFileChooser m_fc;
	private JButton timespam_button;
	private int current_mode;
	private JSlider sThreshold;

	JCheckBox c_spam, c_interesting, c_notinteresting, c_unknown, c_virus;

	
	/**
	 * Constructor to display a model builder window.
	 * @param parent1 who is calling the model builder
	 * @param mode which mode are we operating in. BUILD is for building, EVALUTE for training mode
	 * @param TargetClass what is the target class which we will pass to the models.
	 */

	public ModelBuilder(MessageWindow parent1, int mode, String TargetClass) {

		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout GridBag = new GridBagLayout();
		getContentPane().setLayout(GridBag);

		setSize(new Dimension(450, 200));
		setLocationRelativeTo(parent);
		parent = parent1;
		current_mode = mode;
		if (current_mode == BUILD) {
			setTitle("Model Chooser");
		} else {
			setTitle("Model Evaluator");
		}
		JLabel A;
		A = new JLabel("Choose Model to use");

		model_name = new JTextField(17);
		if (current_mode == BUILD) {
			m_go = new JButton("Build Model");
		} else {
			m_go = new JButton("Use Model");
		}
		m_go.setToolTipText("Execute model");

		timespam_button = new JButton("Run time xprmt");
		timespam_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//TODO: TimeSpam(comboBox.getSelectedIndex());
			}
		});

		if (current_mode == BUILD)//to build models
		{
			m_go.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					final boolean[] results = getClassChoices();
					//m_spinnerNumber.getNumber().doubleValue()
					(new Thread(new Runnable() {
						public void run() {

							parent.Enabled(false);
							setVisible(false);//called from m_mb...so will
							// hide it

							parent.askTrain(model_name.getText(), comboBox.getSelectedIndex(), sThreshold.getValue(),
									results, true);
							//TODO: that last true is if to flush the model
							parent.Enabled(true);
						}
					})).start();
					//m_spinnerNumber.getNumber().intValue());

				}
			});
		} else {
			m_go.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {

					if (parent.getViewIndex() == 1)

						(new Thread(new Runnable() {
							public void run() {
								parent.setEnabled(false);
								parent.askEval(model_name.getText(),/* comboBox.getSelectedIndex(), */
								1);
								parent.setEnabled(true);
							}
						})).start();
					else

						(new Thread(new Runnable() {
							public void run() {
								parent.setEnabled(false);
								parent.askEval(model_name.getText(), 0);
								parent.setEnabled(true);
							}
						})).start();
				}
			});
		}
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(2, 2, 2, 2);
		GridBag.setConstraints(A, constraints);
		getContentPane().add(A);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		GridBag.setConstraints(model_name, constraints);
		getContentPane().add(model_name);

		//get more info
		JButton checkModel = new JButton("Check File Info");
		checkModel.setToolTipText("Click here to get some information on the current file name specified");
		checkModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(getContentPane(), modelHelper.fileInfo(model_name.getText()));
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 1;
		GridBag.setConstraints(checkModel, constraints);
		getContentPane().add(checkModel);

		//add a browse button
		m_fc = new JFileChooser();
		m_fc.setMultiSelectionEnabled(false);//cant choose more than one
		openButton = new JButton("Browse...");
		openButton.addActionListener(this);
		constraints.gridx = 2;
		constraints.gridy = 1;
		GridBag.setConstraints(openButton, constraints);
		getContentPane().add(openButton);
		//need to setup in classify since something sets to correct leanrer
		// elsewhere
		comboBox = winGui.buildJComboModels();
		comboBox.setSelectedIndex(3);
		comboBox.setBackground(Color.green);
		if (mode == BUILD)//training mode
		{
			constraints.gridx = 0;
			constraints.gridy = 2;
			GridBag.setConstraints(comboBox, constraints);
			getContentPane().add(comboBox);
			//also to add help button

			//target label
			JLabel targetLabel = new JLabel("Target Learning:" + TargetClass);
			targetLabel.setToolTipText("This is set in the configuration window. Is used in calc accuracy");
			constraints.gridwidth = 2;
			constraints.gridx = 1;
			constraints.gridy = 3;
			constraints.anchor = GridBagConstraints.EAST;
			GridBag.setConstraints(targetLabel, constraints);
			getContentPane().add(targetLabel);

			//add percentage training:
			JLabel percentageLabel = new JLabel("Percentage Training to be Target Class");

			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 4;
			constraints.anchor = GridBagConstraints.EAST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			GridBag.setConstraints(percentageLabel, constraints);
			getContentPane().add(percentageLabel);

			constraints.gridx = 1;

			sThreshold = new JSlider(-1, 100, -1);
			sThreshold.setMajorTickSpacing(5);
			//sThreshold.setMinorTickSpacing(1);
			//sThreshold.setSnapToTicks(true);
			sThreshold.setPaintTicks(true);
			Hashtable labelTable = new Hashtable();
			labelTable.put(new Integer(100), new JLabel("100%"));
			labelTable.put(new Integer(50), new JLabel("50%"));
			labelTable.put(new Integer(-1), new JLabel("Ignore"));
			//labelTable.put( new Integer( 100 ), new JLabel("Groups") );
			sThreshold.setLabelTable(labelTable);
			sThreshold.setPaintLabels(true);
			sThreshold.setToolTipText("Percentage of training to be target class");
			/*
			 * m_spinnerNumber = new SpinnerNumberModel(-1,-1,100,1);
			 * spinnerThreshold = new JSpinner(m_spinnerNumber);
			 * JSpinner.NumberEditor editorNum = new
			 * JSpinner.NumberEditor(spinnerThreshold, "0");
			 * spinnerThreshold.setEditor(editorNum);
			 * spinnerThreshold.setToolTipText("Percentage of Training to be
			 * target class");
			 * 
			 * GridBag.setConstraints(spinnerThreshold, constraints);
			 * getContentPane().add(spinnerThreshold);
			 */
			constraints.gridwidth = 2;
			GridBag.setConstraints(sThreshold, constraints);
			getContentPane().add(sThreshold);
			//				list of type of classification to use in the training.
			JLabel listOfClassesLabel = new JLabel("Classes to use as part of the model:");
			listOfClassesLabel.setToolTipText("Allows inclusion/exclusion of certain labeled examples");
			constraints.gridx = 0;
			constraints.gridy = 5;
			constraints.gridwidth = 3;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			GridBag.setConstraints(listOfClassesLabel, constraints);
			getContentPane().add(listOfClassesLabel);

			JPanel listOfClasses = new JPanel();
			c_spam = new JCheckBox("Spam", true);
			c_virus = new JCheckBox("Virus", true);
			c_interesting = new JCheckBox("Interesting", true);
			c_notinteresting = new JCheckBox("not Interesting", true);
			c_unknown = new JCheckBox("Unknown", false);
			listOfClasses.add(c_interesting);
			listOfClasses.add(c_unknown);
			listOfClasses.add(c_spam);
			listOfClasses.add(c_virus);
			listOfClasses.add(c_notinteresting);

			constraints.gridx = 0;
			constraints.gridy = 6;
			constraints.gridwidth = 3;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			GridBag.setConstraints(listOfClasses, constraints);
			getContentPane().add(listOfClasses);

		}
		//create help display
		EMTHelp helpb = new EMTHelp(11);
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		GridBag.setConstraints(helpb, constraints);
		getContentPane().add(helpb);

		constraints.gridx = 1;
		constraints.gridy = 3;
		GridBag.setConstraints(timespam_button, constraints);
		//if(c==0)
		//getContentPane().add(timespam_button);

		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		GridBag.setConstraints(m_go, constraints);
		model_name.setText("model.mod");
		getContentPane().add(m_go);

		pack();
	}

	public void actionPerformed(ActionEvent e) {
		//Handle open button action.
		if (e.getSource() == openButton) {
			int returnVal = m_fc.showOpenDialog(ModelBuilder.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				//File file = fc.getSelectedFile();
				model_name.setText(m_fc.getSelectedFile().getPath());
				//This is where a real application would open the file.
				//log.append("Opening: " + file.getName() + "." + newline);
			} else {
				//log.append("Open command cancelled by user." + newline);
			}
		}// else if (e.getSource() == popCStart) {
	//		System.out.println("popup start returned:" + popCStart.getCalendar().toString());
		//} else if (e.getSource() == popCEnd) {

		//}

		setVisible(true);
	}

	/**
	 * returns the true/false of each type of class in order of unknown,
	 * interesting, spam, virus, notinteresting
	 * 
	 * @return array of booleans
	 */
	public final boolean[] getClassChoices() {
		boolean[] results = new boolean[5];
		results[0] = c_unknown.isSelected();
		results[1] = c_interesting.isSelected();
		results[2] = c_spam.isSelected();
		results[3] = c_virus.isSelected();
		results[4] = c_notinteresting.isSelected();

		return results;
	}

}