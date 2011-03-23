/*
 * Author: Wei-Jen Li
 * Created: 07/30/2002 19:56:05
 *
 * This class is a tab of the eMail Figures
 * Select the attributes and press 'GO' to start it
 */

/*
 *
 * extended by shlomo hershkop nov 2002
 *
 */

package metdemo.Window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;
import javax.swing.border.*;

import metdemo.EMTConfiguration;
import metdemo.winGui;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.KeywordPanel;

import java.util.*;
import java.sql.SQLException;
//import javax.swing.JSpinner.NumberEditor;
//import javax.swing.JSpinner.*;
//import java.awt.FlowLayout;
import java.awt.event.ActionListener;

// TODO: Auto-generated Javadoc
/**
 * This class allows different setting to be decided by the user for the EMT
 * system. For example, choosing Naive bayes features, body stop lists etc
 */

public class EmailInternalConfigurationWindow extends JPanel implements ActionListener {

	//check box for all the attributes
	/** The sender. */
	private JCheckBox sender;
	
	/** The Constant SENDER. */
	public final static int SENDER = 0;
	
	/** The sname. */
	private JCheckBox sname;
	
	/** The Constant SNAME. */
	public final static int SNAME = 1;
	
	/** The sdom. */
	private JCheckBox sdom;
	
	/** The Constant SDOM. */
	public final static int SDOM = 2;
	
	/** The szn. */
	private JCheckBox szn;
	
	/** The Constant SZN. */
	public final static int SZN = 3;
	
	/** The rcpt. */
	private JCheckBox rcpt;
	
	/** The Constant RCPT. */
	public final static int RCPT = 4;
	
	/** The rname. */
	private JCheckBox rname;
	
	/** The Constant RNAME. */
	public final static int RNAME = 5;
	
	/** The rdom. */
	private JCheckBox rdom;
	
	/** The Constant RDOM. */
	public final static int RDOM = 6;
	
	/** The rzn. */
	private JCheckBox rzn;
	
	/** The Constant RZN. */
	public final static int RZN = 7;
	
	/** The numrcpt. */
	private JCheckBox numrcpt;
	
	/** The Constant NUMRCPT. */
	public final static int NUMRCPT = 8;
	
	/** The numattach. */
	private JCheckBox numattach;
	
	/** The Constant NUMATTACH. */
	public final static int NUMATTACH = 9;
	
	/** The size. */
	private JCheckBox size;
	
	/** The Constant SIZE. */
	public final static int SIZE = 10;
	
	/** The mailref. */
	private JCheckBox mailref;
	
	/** The Constant MAILREF. */
	public final static int MAILREF = 11;
	
	/** The xmailer. */
	private JCheckBox xmailer;
	
	/** The Constant XMAILER. */
	public final static int XMAILER = 12;
	
	/** The dates. */
	private JCheckBox dates;
	
	/** The Constant DATES. */
	public final static int DATES = 13;
	
	/** The times. */
	private JCheckBox times;
	
	/** The Constant TIMES. */
	public final static int TIMES = 14;
	
	/** The flags. */
	private JCheckBox flags;
	
	/** The Constant FLAGS. */
	public final static int FLAGS = 15;
	
	/** The msghash. */
	private JCheckBox msghash;
	
	/** The Constant MSGHASH. */
	public final static int MSGHASH = 16;
	
	/** The rplyto. */
	private JCheckBox rplyto;
	
	/** The Constant RPLYTO. */
	public final static int RPLYTO = 17;
	
	/** The forward. */
	private JCheckBox forward;
	
	/** The Constant FORWARD. */
	public final static int FORWARD = 18;
	
	/** The type. */
	private JCheckBox type;
	
	/** The Constant TYPE. */
	public final static int TYPE = 19;
	
	/** The recnt. */
	private JCheckBox recnt;
	
	/** The Constant RECNT. */
	public final static int RECNT = 20;
	
	/** The subject. */
	private JCheckBox subject;
	
	/** The Constant SUBJECT. */
	public final static int SUBJECT = 21;
	
	/** The timeadj. */
	private JCheckBox timeadj;
	
	/** The Constant TIMEADJ. */
	public final static int TIMEADJ = 22;
	
	/** The folder. */
	private JCheckBox folder;
	
	/** The Constant FOLDER. */
	public final static int FOLDER = 23;
	
	/** The utime. */
	private JCheckBox utime;
	
	/** The Constant UTIME. */
	public final static int UTIME = 24;
	
	/** The sender loc. */
	private JCheckBox senderLoc;
	
	/** The Constant SENDERLOC. */
	public final static int SENDERLOC = 25;
	
	/** The rcpt loc. */
	private JCheckBox rcptLoc;
	
	/** The Constant RCPTLOC. */
	public final static int RCPTLOC = 26;
	
	/** The insert time. */
	private JCheckBox insertTime;
	
	/** The Constant INSERTTIME. */
	public final static int INSERTTIME = 27;
	
	/** The hour time. */
	private JCheckBox hourTime;
	
	/** The Constant HOURTIME. */
	public final static int HOURTIME = 28;
	
	/** The month time. */
	private JCheckBox monthTime;
	
	/** The Constant MONTHTIME. */
	public final static int MONTHTIME = 29;
	
	/** The message body. */
	private JCheckBox messageBody;
	
	/** The Constant MESSAGEBODY. */
	public final static int MESSAGEBODY = 30;

	//private JCheckBox body;

	/** The Constant total_items. */
	public static final int total_items = 31;
	
	/** The ngram len. */
	private JTextField ngramLen;
	//the buttons
	/** The GO. */
	private JButton GO;
	
	/** The ALL. */
	private JButton ALL;
	
	/** The UALL. */
	private JButton UALL;
	
	/** The CLEAR. */
	private JButton CLEAR;
	
	/** The REFRESH. */
	private JButton REFRESH;
	
	/** The mails. */
	private JComboBox mails;

	//emails from the database
	/** The m_jdbc view. */
	private EMTDatabaseConnection m_jdbcView;
	
	/** The data. */
	private String[][] data;
	
	/** The records. */
	int records;//amount of avaiable records
	//the current UID that selected in the combo box
	/** The curr uid. */
	private int currUID = 1;

	//for showing the selected figures
	/** The selected. */
	private JTextField[] selected = new JTextField[total_items];
	
	/** The selected features boolean. */
	private boolean[] selectedFeaturesBoolean = new boolean[total_items];
	
	/** The selected figures. */
	private Vector selectedFigures = new Vector();
	//static Trainer NB=null;
	/** The spinner threshold. */
	private JSpinner spinnerThreshold;
	
	/** The m_spinner number. */
	private SpinnerNumberModel m_spinnerNumber;
	
	/** The m_target. */
	private JComboBox m_target;
	
	/** The m_wg. */
	winGui m_wg = null;
	
	/** The m_boollower. */
	private boolean m_boolstoplist, m_boollower = true;
	
	/** The stop hash. */
	private Hashtable stopHash;
	
	/** The lcheck. */
	private JCheckBox slcheck, lcheck;
	
	/** The attach_type. */
	private JComboBox attach_type;
	
	/** The m_email configuration. */
	private EMTConfiguration m_emailConfiguration;

	//constructor
	/**
	 * Instantiates a new email internal configuration window.
	 *
	 * @param emtc the emtc
	 * @param jdbcView the jdbc view
	 * @param wg the wg
	 * @throws NullPointerException the null pointer exception
	 */
	public EmailInternalConfigurationWindow(EMTConfiguration emtc, EMTDatabaseConnection jdbcView, winGui wg) throws NullPointerException {

		if ((jdbcView == null)) {
			throw new NullPointerException();
		}

		m_emailConfiguration = emtc;
		m_jdbcView = jdbcView;
		m_wg = wg;
		stopHash = new Hashtable();
		//set up the text field and the vector for the figures
		for (int i = 0; i < total_items; i++) {
			selected[i] = new JTextField(30);
			//selectedFigures[i] = "";
		}

		JPanel panel = this;

		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout GridBag = new GridBagLayout();
		panel.setLayout(GridBag);

		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.anchor = GridBagConstraints.WEST;

		//panel.setLayout(new GridBagLayout());

		//the panel of the check boxes
		JPanel checkPanel = new JPanel();
		//checkPanel.setLayout(new GridLayout(28, 2));

		GridBagLayout gbl = new GridBagLayout();
		checkPanel.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();

		Border bd = BorderFactory.createEtchedBorder(Color.white, new Color(134, 134, 134));
		TitledBorder ttl = new TitledBorder(bd, "Features");
		checkPanel.setBorder(ttl);
		int p = 0;
		//add the check boxes and the textfield for the figures
		sender = addCheckBox(checkPanel, "Sender's email", "Sender's email address", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		sname = addCheckBox(checkPanel, "Sender Name", "Sender's name", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		sdom = addCheckBox(checkPanel, "Sender Domain", "Domain of sender's email address", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		szn = addCheckBox(checkPanel, "Sender Zone", "Sender's top level domain (.com, .net, ...)", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rcpt = addCheckBox(checkPanel, "Recipient Email", "Recipient's email address", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rname = addCheckBox(checkPanel, "Recipient Name", "Recipient's name", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rdom = addCheckBox(checkPanel, "Recipient Domain", "Domain of recipient's email address", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rzn = addCheckBox(checkPanel, "Recipient Zone", "Recipient's top level domain (.com, .net, ...)", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		numrcpt = addCheckBox(checkPanel, "Number of rcpt's", "Number of recipients", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		numattach = addCheckBox(checkPanel, "Number of Attachments", "Number of attachments", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		size = addCheckBox(checkPanel, "Email Size", "Size of the message", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		mailref = addCheckBox(checkPanel, "Email ID", "Unique ID assigned by mail server to each email", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		xmailer = addCheckBox(checkPanel, "Email Client Program", "Program used to send the email", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		dates = addCheckBox(checkPanel, "Date", "Date received", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		times = addCheckBox(checkPanel, "Time", "Time received", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		flags = addCheckBox(checkPanel, "Flags", "unused", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		msghash = addCheckBox(checkPanel, "message hash", "md5 hash of email body", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rplyto = addCheckBox(checkPanel, "Flag rplyto", "Whether the email is a reply", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		forward = addCheckBox(checkPanel, "Flag forward", "Whether email is a forward", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		type = addCheckBox(checkPanel, "Email MIME type", "Email MIME type", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		recnt = addCheckBox(checkPanel, "Number of RE in subject", "Number of times RE appears in the subject", gbc, 0,
				p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		subject = addCheckBox(checkPanel, "Email Subject Line", "Email subject line", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		timeadj = addCheckBox(checkPanel, "Time zone", "Time zone of sender", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		folder = addCheckBox(checkPanel, "folder", "", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		utime = addCheckBox(checkPanel, "Unix time representation", "Unix timestamp of received date and time", gbc, 0,
				p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		senderLoc = addCheckBox(checkPanel, "Sender Location (internal/external)",
				"Whether sender is internal or external", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		rcptLoc = addCheckBox(checkPanel, "rcpt Location (internal/external)",
				"Whether recipient is internal or external", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		insertTime = addCheckBox(checkPanel, "Date Time ", "Time inserted into database", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		hourTime = addCheckBox(checkPanel, "Hour Time ", "Hour inserted into database", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		monthTime = addCheckBox(checkPanel, "Month", "Month inserted into database", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;
		messageBody = addCheckBox(checkPanel, "Email Body", "Text Body of Email", gbc, 0, p);
		selected[p] = addTextField(checkPanel, gbc, 1, p);
		p++;

		JScrollPane p1 = new JScrollPane(checkPanel);
		//panel.add(p1, BorderLayout.CENTER);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 6;
		constraints.weighty = 7;
		constraints.fill = GridBagConstraints.BOTH;

		GridBag.setConstraints(p1, constraints);
		panel.add(p1);
		constraints.weighty = 0;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		//the panel for the 'GO' button
		//	Box goPanel = Box.createHorizontalBox();
		GO = new JButton("Save Changes");
		GO.setEnabled(false);
		GO.setMnemonic(KeyEvent.VK_G);
		GO.addActionListener(this);
		ALL = new JButton("Select All");
		ALL.setMnemonic(KeyEvent.VK_S);
		ALL.addActionListener(this);
		UALL = new JButton("Unselect All");
		UALL.setMnemonic(KeyEvent.VK_U);
		UALL.addActionListener(this);
		CLEAR = new JButton("Clear");
		CLEAR.setMnemonic(KeyEvent.VK_C);
		CLEAR.addActionListener(this);
		REFRESH = new JButton("Default Values");
		REFRESH.setMnemonic(KeyEvent.VK_C);
		REFRESH.addActionListener(this);

		constraints.gridx = 0;
		constraints.gridy = 4;
		GridBag.setConstraints(ALL, constraints);
		panel.add(ALL);

		constraints.gridx = 1;
		constraints.gridy = 4;
		GridBag.setConstraints(UALL, constraints);
		panel.add(UALL);

		constraints.gridx = 2;
		constraints.gridy = 4;
		GridBag.setConstraints(CLEAR, constraints);
		panel.add(CLEAR);

		constraints.gridx = 3;
		constraints.gridy = 4;
		GridBag.setConstraints(GO, constraints);
		panel.add(GO);

		constraints.gridx = 4;
		constraints.gridy = 4;
		GridBag.setConstraints(REFRESH, constraints);
		panel.add(REFRESH);

		//	goPanel.add(ALL);
		//goPanel.add(Box.createRigidArea(new Dimension(30, 10)));
		//goPanel.add(UALL);
		//goPanel.add(Box.createRigidArea(new Dimension(30, 10)));
		//goPanel.add(CLEAR);
		//goPanel.add(Box.createGlue());
		//goPanel.add(GO);
		//goPanel.add(Box.createRigidArea(new Dimension(30, 10)));
		//goPanel.add(REFRESH);

		//constraints.gridx = 0;
		//constraints.gridy = 3;
		//GridBag.setConstraints(goPanel, constraints);
		//panel.add(goPanel);
		//	panel.add(goPanel, BorderLayout.SOUTH);
		records = 0;
		try {

			synchronized (m_jdbcView) {
				int minuid = 0;
				//need to get the min uid from the records
				data = m_jdbcView.getSQLData("select min(uid) from email");
				//data = m_jdbcView.getRowData();
				try {
					minuid = Integer.parseInt(data[0][0]);
				} catch (NumberFormatException e2) {
					System.out.println("Might have empty db -" + e2);

				}

				minuid += 19;

				data = m_jdbcView
						.getSQLData("select count(*) from email,message  where email.mailref=message.mailref AND UID <="
								+ minuid);
				//data = m_jdbcView.getRowData();
				try {
					if (data != null)
						records = Integer.parseInt(data[0][0]);
				} catch (NumberFormatException e) {
					//JOptionPane.showMessageDialog(this,"Error: " +
					// e,"Error",JOptionPane.ERROR_MESSAGE);
					System.out.println("Email2 fig:" + e);

				}
				if (records != 0) {
					if( jdbcView.getTypeDB().equals(DatabaseManager.sMYSQL)){
						System.out.println("MYSQL detected fine");
						data = m_jdbcView.getSQLData("select e.sender,e.sname,e.sdom,e.szn,e.rcpt,e.rname,e.rdom,e.rzn,e.numrcpt,e.numattach,e.size,e.mailref,e.xmailer,e.dates,e.times,e.flags,e.msghash,e.rplyto,e.forward,e.type,e.recnt,e.subject,e.timeadj,e.folder,e.utime,e.senderLoc,e.rcptLoc,e.inserttime+0,ghour,gmonth from email e, message m where e.mailref=m.mailref AND UID <= "
									+ minuid);
					}else if( jdbcView.getTypeDB().equals(DatabaseManager.sPOSTGRES)){
						System.out.println("POSTGRES detected fine");
						data = m_jdbcView
						.getSQLData("select e.sender,e.sname,e.sdom,e.szn,e.rcpt,e.rname,e.rdom,e.rzn,e.numrcpt,e.numattach,e.size,e.mailref,e.xmailer,e.dates,e.times,e.flags,e.msghash,e.rplyto,e.forward,e.type,e.recnt,e.subject,e.timeadj,e.folder,e.utime,e.senderLoc,e.rcptLoc,e.inserttime,ghour,gmonth from email e, message m where e.mailref=m.mailref AND UID <= "
								+ minuid);
					}else{
						System.out.println("derby assumed");
						data = m_jdbcView
						.getSQLData("select e.sender,e.sname,e.sdom,e.szn,e.rcpt,e.rname,e.rdom,e.rzn,e.numrcpt,e.numattach,e.size,e.mailref,e.xmailer,e.dates,e.times,e.flags,e.msghash,e.rplyto,e.forward,e.type,e.recnt,e.subject,e.timeadj,e.folder,e.utime,e.senderLoc,e.rcptLoc,e.inserttime,ghour,gmonth from email e, message m where e.mailref=m.mailref AND UID <= "
								+ minuid);
					}
					//data = m_jdbcView.getRowData();
				} else
					data = null;
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "4 -Error getting details from database: " + ex, "Error",
					JOptionPane.ERROR_MESSAGE);
			data = null;
		}

		//top part of panel
		//the panel of the mail list
		//JPanel mailPanel = new JPanel();
		//mailPanel.setLayout(new FlowLayout());
		//mailPanel.add(

		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		JLabel ssd = new JLabel("Select Sample Data:");
		mails = new JComboBox();

		if (records > 50)
			records = 23;

		if (data != null)
			for (int i = 0; i < records; i++) {
				mails.addItem(data[i][4]); //argh i hate hard coded stuff
			}

		mails.addActionListener(this);
		//mailPanel.add(mails);
		//mailPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		constraints.gridx = 0;
		constraints.gridy = 0;
		GridBag.setConstraints(ssd, constraints);
		panel.add(ssd);
		constraints.gridx = 1;
		constraints.gridy = 0;
		GridBag.setConstraints(mails, constraints);
		panel.add(mails);

		ngramLen = new JTextField("8");
		ngramLen.setText("   5   ");
		//mailPanel.add(ngramLen);

		Box NGbox = Box.createHorizontalBox();
		NGbox.setToolTipText("Length of NGRAM model");
		JLabel ngl = new JLabel("N-Gram Length  ");
		NGbox.add(ngl);
		NGbox.add(ngramLen);
		//mailPanel.add(NGbox);
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		GridBag.setConstraints(NGbox, constraints);
		panel.add(NGbox);
		constraints.gridwidth = 1;

		Box Tbox = Box.createHorizontalBox();
		Tbox.setToolTipText("Threshold for general user");
		JLabel tl = new JLabel("Threshold Levels  ");
		Tbox.add(tl);

		//	constraints.gridx = 2;
		//constraints.gridy = 1;
		//GridBag.setConstraints(tl, constraints);
		//panel.add(tl);

		m_spinnerNumber = new SpinnerNumberModel(0.555, 0.000, 1.000, 0.01);
		spinnerThreshold = new JSpinner(m_spinnerNumber);
		JSpinner.NumberEditor editorNum = new JSpinner.NumberEditor(spinnerThreshold, "0.000");
		spinnerThreshold.setEditor(editorNum);

		Tbox.add(spinnerThreshold);
		constraints.gridwidth = 2;
		constraints.gridx = 2;
		constraints.gridy = 1;
		GridBag.setConstraints(Tbox, constraints);
		panel.add(Tbox);

		Box TarBox = Box.createHorizontalBox();
		TarBox.setToolTipText("choose target class to learn");
		JLabel tc = new JLabel("Target Class  ");
		TarBox.add(tc);

		m_target = new JComboBox();
		m_target.setBackground(Color.green);

		m_target.addItem("Unknown");
		m_target.addItem("Interesting");
		m_target.addItem("Spam");
		m_target.addItem("Virus");
		m_target.addItem("Not Interesting");
		m_target.setSelectedIndex(2); //set it to spam
		m_target.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(m_wg!=null)
					m_wg.setTargetClass("" + getTargetClass());
			}
		});

		TarBox.add(m_target);
		constraints.gridwidth = 2;
		constraints.gridx = 4;
		constraints.gridy = 0;
		GridBag.setConstraints(TarBox, constraints);
		panel.add(TarBox);

		//setup the content stuff.
		Box SlistBox = Box.createHorizontalBox();
		SlistBox.setBorder(new BevelBorder(BevelBorder.LOWERED));
		SlistBox.setToolTipText("Length of NGRAM model");

		m_boolstoplist = false;
		slcheck = new JCheckBox("Use Stop List", false);
		slcheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_boollower = lcheck.isSelected();
				m_boolstoplist = slcheck.isSelected();
				if(m_wg!=null)
				m_wg.setBooleans(m_boolstoplist, m_boollower);

			}
		});

		SlistBox.add(slcheck);
		JButton sedit = new JButton("Edit Stop List");
		sedit.setToolTipText("Edit current stop word list");
		sedit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				KeywordPanel kp = new KeywordPanel(m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST));

				//int vs[] =
				// this.groupContentWithList(data,threshold,kp.getList());
				String msgs = new String("Please confirm Stop list for message bodies");

				Object stuff[] = {msgs, kp};
				int c = JOptionPane.showConfirmDialog(EmailInternalConfigurationWindow.this, stuff, "Setup Stopwords",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				//"Data to be fetched into table will be " + data.length
				// +"\nShould we continue??","Really Big Data
				// Ahead!",JOptionPane.YES_NO_OPTION);
				if (c == JOptionPane.CANCEL_OPTION) {
					if (stopHash.isEmpty())
						stopHash = kp.getHash(m_boollower);
					return;//no grouping to do.
				}
				kp.saveInput();
				stopHash = kp.getHash(m_boollower);

				m_emailConfiguration.putProperty(EMTConfiguration.STOPLIST, kp.getFileName());

			}
		});

		SlistBox.add(sedit);
		JButton kedit = new JButton("Edit Keyword List");
		kedit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				KeywordPanel kp = new KeywordPanel(m_emailConfiguration.getProperty(EMTConfiguration.WORDLIST));

				//int vs[] =
				// this.groupContentWithList(data,threshold,kp.getList());
				String msgs = new String("Please confirm Keyword list for message bodies");

				Object stuff[] = {msgs, kp};
				int c = JOptionPane.showConfirmDialog(EmailInternalConfigurationWindow.this, stuff, "Setup Keywords",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				//"Data to be fetched into table will be " + data.length
				// +"\nShould we continue??","Really Big Data
				// Ahead!",JOptionPane.YES_NO_OPTION);
				if (c == JOptionPane.CANCEL_OPTION) {
					return;//no grouping to do.
				}
				kp.saveInput();

				m_emailConfiguration.putProperty(EMTConfiguration.WORDLIST, kp.getFileName());
				//stopHash = kp.getHash(m_boollower);
			}
		});

		SlistBox.add(kedit);

		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 1;
		GridBag.setConstraints(SlistBox, constraints);
		panel.add(SlistBox);
		constraints.gridwidth = 1;
		m_boollower = true;
		lcheck = new JCheckBox("Force Lower Case", true);
		//lcheck.setBorder(new BevelBorder(BevelBorder.LOWERED));
		lcheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_boolstoplist = slcheck.isSelected();
				m_boollower = lcheck.isSelected();
				if(m_wg!=null)
				m_wg.setBooleans(m_boolstoplist, m_boollower);
			}
		});

		constraints.gridx = 4;
		constraints.gridy = 1;
		GridBag.setConstraints(lcheck, constraints);
		panel.add(lcheck);

		//mailPanel.add(TarBox);
		EMTHelp et = new EMTHelp(EMTHelp.EMAILFEATURES);
		//mailPanel.add(et);
		constraints.gridwidth = 1;
		constraints.gridx = 5;
		constraints.gridy = 1;
		GridBag.setConstraints(et, constraints);
		panel.add(et);
		//what type of attachment body ie text etc
		JLabel types = new JLabel("Choose type of body to use: ");
		constraints.gridx = 0;
		constraints.gridy = 2;
		GridBag.setConstraints(types, constraints);
		panel.add(types);
		//RULEZ
		/*
		 * String data2[][] = null; synchronized (m_jdbcView){ try{
		 * m_jdbcView.getSqlData("select distinct type from message"); data2 =
		 * m_jdbcView.getRowData(); }catch(SQLException
		 * t){System.out.println("t");data2=null;} }
		 */
		
		ComboBoxModel model = null;
		
		if(m_wg!=null)
			{model = new DefaultComboBoxModel(m_wg.getAttachments());
			attach_type = new JComboBox(model);
			}
		else{
			attach_type = new JComboBox();
		}
		attach_type.insertItemAt(new String("All types"), 0);
		attach_type.insertItemAt(new String("All text types"), 1);
		attach_type.setToolTipText("Choose type of email attachmant to analyze in message window");
		/*
		 * if(data2!=null) for(int i=0;i <data2.length;i++)
		 * attach_type.addItem(data2[i][0]);
		 */
		attach_type.setSelectedIndex(1);

		constraints.gridx = 1;
		constraints.gridy = 2;
		GridBag.setConstraints(attach_type, constraints);
		panel.add(attach_type);

		//constraints.gridx = 0;
		//constraints.gridy = 0;
		//GridBag.setConstraints(mailPanel, constraints);
		//panel.add(mailPanel);

		//panel.add(mailPanel, BorderLayout.NORTH);

		//default all selected
		setDefaultSelect(true);

		showFigures();

	}

	/**
	 * Refresh.
	 */
	public void Refresh() {
		try {
			synchronized (m_jdbcView) {
				int minuid = 0;
				//need to get the min uid from the records
				data = m_jdbcView.getSQLData("select min(uid) from email");
				//data = m_jdbcView.getRowData();
				try {
					minuid = Integer.parseInt(data[0][0]);
				} catch (NumberFormatException e2) {
					System.out.println("Emailf fig:" + e2);

				}

				minuid += 19;

				data = m_jdbcView.getSQLData("select count(*) from email where UID <=" + minuid);
				//data = m_jdbcView.getRowData();
				try {
					records = Integer.parseInt(data[0][0]);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					//System.out.println("Error while refreshing Email
					// Features:" + e);
				}

				data = m_jdbcView.getSQLData("select *,ghour,gmonth from email where UID <=" + minuid);
				//data = m_jdbcView.getRowData();
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "5 - Error getting details from database: " + ex);
		}

		//the panel of the mail list
		mails.removeAllItems();

		for (int i = 0; i < records; i++) {
			mails.addItem(data[i][4]);
		}

		//default all selected
		setDefaultSelect(true);
		showFigures();

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

		String arg = evt.getActionCommand();

		if (evt.getSource() == GO) {
			//System.out.println("You select:");
			showFigures();
			//	System.out.println("\n");
			GO.setEnabled(false);
		} else if (evt.getSource() instanceof JCheckBox) {
			GO.setEnabled(true);
		}

		else if (arg.equals("Select All")) {
			setSelect(true);
			GO.setEnabled(true);
		}

		else if (arg.equals("Unselect All")) {
			setSelect(false);
			GO.setEnabled(true);
		} else if (arg.equals("Refresh")) {

			Refresh();
		} else if (arg.equals("Clear")) {
			for (int i = 0; i < total_items; i++) {
				selected[i].setText("");
			}
		}

		else if (arg.equals("Default Values")) {
			setDefaultSelect(true);
			GO.setEnabled(true);
		}

		else if (arg.equals("comboBoxChanged")) {
			JComboBox source = (JComboBox) evt.getSource();
			int index = source.getSelectedIndex(); //the UID
			currUID = index + 1;
			//System.out.println("You select:");
			showFigures();
			//System.out.println("\n");
		} else {
			showFigures();
		}
	}

	/**
	 * Adds the check box.
	 *
	 * @param p the p
	 * @param name the name
	 * @param tooltip the tooltip
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @return the j check box
	 */
	public JCheckBox addCheckBox(JPanel p, String name, String tooltip, GridBagConstraints g, int x, int y) {
		JCheckBox c = new JCheckBox(name);
		c.addActionListener(this);

		//set the tool tip for the check box
		c.setToolTipText(tooltip);

		g.anchor = GridBagConstraints.WEST;
		g.gridx = x;
		g.gridy = y;
		g.gridwidth = 1;
		g.gridheight = 1;
		p.add(c, g);
		return c;
	}

	/**
	 * Gets the selected features boolean.
	 *
	 * @return the selected features boolean
	 */
	public final boolean[] getselectedFeaturesBoolean() {
		return selectedFeaturesBoolean;
	}

	/**
	 * Gets the selected figures.
	 *
	 * @return the selected figures
	 */
	public final Vector getSelectedFigures() {
		return selectedFigures;
	}
	/*
	 * public void setNB(Trainer n) { NB = n; }
	 */
	//so old code wont complain
	//    public void setNB(NBayes n)
	//{}

	
	
	/**
	 * Checks if is feature discrete.
	 *
	 * @param n the n
	 * @return true, if is feature discrete
	 */
	public static boolean isFeatureDiscrete(int n) {
		switch (n) {
			case (8) :
			case (9) :
			case (10) :
			case (20) :
			case (22) :
			case (24) :
			case (28) :
			case (29) :
				return false;
			default :
				return true;
		}
	}
	
	
	
	
	
	
	/*
	 * public void updateNB() { if(NB!=null)
	 * NB.setFeatures(NB.vecToStr3(selectedFigures)); }
	 */

	/**
	 * Gets the features.
	 *
	 * @return the features
	 */
	public final String getFeatures() {

		//return (vecToStr3(selectedFigures));
		// }
		//    public final String vecToStr3(Vector v)
		//{
		//int i=v.size();
		String s = new String("");
		for (int j = 0; j < selectedFigures.size(); j++) {
			if (j == 0)
				s += selectedFigures.elementAt(j);
			else
				s += "," + selectedFigures.elementAt(j);
		}
		//	System.out.println("called getfeatures in email figures "+s);
		return s;

	}

	/**
	 * fast gui addition to window.
	 *
	 * @param p the p
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @return the j text field
	 */
	public JTextField addTextField(JPanel p, GridBagConstraints g, int x, int y) {
		JTextField t = new JTextField(50);
		t.setEditable(false);
		t.addActionListener(this);
		g.gridx = x;
		g.gridy = y;
		g.gridwidth = 2;
		g.gridheight = 1;
		p.add(t, g);
		return t;
	}

	/**
	 * set all to value of b.
	 *
	 * @param b is the boolean to set them all to
	 */
	public void setSelect(boolean b) {
		sender.setSelected(b);
		sname.setSelected(b);
		sdom.setSelected(b);
		szn.setSelected(b);
		rcpt.setSelected(b);
		rname.setSelected(b);
		rdom.setSelected(b);
		rzn.setSelected(b);
		numrcpt.setSelected(b);
		numattach.setSelected(b);
		size.setSelected(b);
		mailref.setSelected(b);
		xmailer.setSelected(b);
		dates.setSelected(b);
		times.setSelected(b);
		flags.setSelected(b);
		msghash.setSelected(b);
		rplyto.setSelected(b);
		forward.setSelected(b);
		type.setSelected(b);
		recnt.setSelected(b);
		subject.setSelected(b);
		timeadj.setSelected(b);
		folder.setSelected(b);
		utime.setSelected(b);
		senderLoc.setSelected(b);
		rcptLoc.setSelected(b);
		insertTime.setSelected(b);
		hourTime.setSelected(b);
		monthTime.setSelected(b);
		messageBody.setSelected(b);
	}

	/**
	 * sets the default set of features.
	 *
	 * @param b the new default select
	 */

	public void setDefaultSelect(boolean b) {
		sender.setSelected(!b);
		sname.setSelected(b);
		sdom.setSelected(b);
		szn.setSelected(b);
		rcpt.setSelected(!b);
		rname.setSelected(b);
		rdom.setSelected(b);
		rzn.setSelected(b);
		numrcpt.setSelected(b);
		numattach.setSelected(b);
		size.setSelected(b);
		mailref.setSelected(!b);
		xmailer.setSelected(!b);
		dates.setSelected(!b);
		times.setSelected(!b);
		flags.setSelected(!b);
		msghash.setSelected(!b);
		rplyto.setSelected(!b);
		forward.setSelected(!b);
		type.setSelected(b);
		recnt.setSelected(b);
		subject.setSelected(!b);
		timeadj.setSelected(!b);
		folder.setSelected(!b);
		utime.setSelected(!b);
		senderLoc.setSelected(!b);
		rcptLoc.setSelected(!b);
		insertTime.setSelected(!b);
		//test
		hourTime.setSelected(!b);
		monthTime.setSelected(!b);
		messageBody.setSelected(!b);
	}
	
	
	/**
	 * Sets the default spamassassin select.
	 */
	public void setDefaultSpamassassinSelect() {
		sender.setSelected(true);
		sname.setSelected(false);
		sdom.setSelected(false);
		szn.setSelected(false);
		rcpt.setSelected(true);
		rname.setSelected(false);
		rdom.setSelected(false);
		rzn.setSelected(false);
		numrcpt.setSelected(false);
		numattach.setSelected(false);
		size.setSelected(false);
		mailref.setSelected(true);
		xmailer.setSelected(true);
		dates.setSelected(true);
		times.setSelected(true);
		flags.setSelected(false);
		msghash.setSelected(false);
		rplyto.setSelected(false);
		forward.setSelected(false);
		type.setSelected(true);
		recnt.setSelected(false);
		subject.setSelected(false);
		timeadj.setSelected(false);
		folder.setSelected(false);
		utime.setSelected(false);
		senderLoc.setSelected(false);
		rcptLoc.setSelected(false);
		insertTime.setSelected(false);
		//test
		hourTime.setSelected(false);
		monthTime.setSelected(false);
		messageBody.setSelected(true);
	}

	/**
	 * sets the view of the features avaibable for nbayes.
	 */

	public void showFigures() {

		if (records == 0)
			return;
		try {

			selectedFigures.clear();

			if (sender.isSelected()) {
				selected[SENDER].setText(data[currUID - 1][SENDER]);
				selectedFigures.add("e.sender");
				selectedFeaturesBoolean[SENDER] = true;
				//System.out.print("1 " + selectedFigures[0] + " is selected");
			} else {
				//selectedFigures[0] = "";
				selectedFeaturesBoolean[SENDER] = false;
				selected[SENDER].setText("");
			}
			if (sname.isSelected()) {
				selected[SNAME].setText(data[currUID - 1][1]);
				selectedFigures.add("e.sname");
				selectedFeaturesBoolean[SNAME] = true;
				//System.out.print("2 " + selectedFigures[1] + " is selected");
			} else {
				selectedFeaturesBoolean[SNAME] = false;
				selected[SNAME].setText("");
			}
			if (sdom.isSelected()) {
				selected[2].setText(data[currUID - 1][2]);
				selectedFigures.add("e.sdom");
				selectedFeaturesBoolean[SDOM] = true;
				//System.out.print("3 " + selectedFigures[2] + " is selected");
			} else {
				selectedFeaturesBoolean[SDOM] = false;
				//selectedFigures[2] = "";
				selected[SDOM].setText("");
			}
			if (szn.isSelected()) {
				selected[3].setText(data[currUID - 1][3]);
				selectedFigures.add("e.szn");
				selectedFeaturesBoolean[SZN] = true;
				//System.out.print("4 " + selectedFigures[3] + " is selected");
			} else {
				//selectedFigures[3] = "";
				selectedFeaturesBoolean[SZN] = false;
				selected[3].setText("");
			}
			if (rcpt.isSelected()) {
				selected[4].setText(data[currUID - 1][4]);
				selectedFigures.add("e.rcpt");
				selectedFeaturesBoolean[RCPT] = true;
				//System.out.print("5 " + selectedFigures[4] + " is selected");
			} else {
				selectedFeaturesBoolean[RCPT] = false;
				selected[4].setText("");
			}
			if (rname.isSelected()) {
				selected[5].setText(data[currUID - 1][5]);
				selectedFigures.add("e.rname");
				selectedFeaturesBoolean[RNAME] = true;
				//System.out.print("6 " + selectedFigures[5] + " is selected");
			} else {
				selectedFeaturesBoolean[RNAME] = false;
				//	selectedFigures[5] = "";
				selected[5].setText("");
			}
			if (rdom.isSelected()) {
				selectedFeaturesBoolean[RDOM] = true;
				selected[6].setText(data[currUID - 1][6]);
				selectedFigures.add("e.rdom");
				//	System.out.print("7 " + selectedFigures[6] + " is selected");
			} else {
				selectedFeaturesBoolean[RDOM] = false;
				//	selectedFigures[6] = "";
				selected[6].setText("");
			}
			if (rzn.isSelected()) {
				selected[7].setText(data[currUID - 1][7]);
				selectedFigures.add("e.rzn");
				selectedFeaturesBoolean[RZN] = true;
				//System.out.print("8 " + selectedFigures[7] + " is selected");
			} else {
				selectedFeaturesBoolean[RZN] = false;
				//selectedFigures[7] = "";
				selected[7].setText("");
			}
			if (numrcpt.isSelected()) {
				selected[8].setText(data[currUID - 1][8]);
				selectedFigures.add("e.numrcpt");
				selectedFeaturesBoolean[NUMRCPT] = true;
				//System.out.print("9 " + selectedFigures[8] + " is selected");
			} else {
				selectedFeaturesBoolean[NUMRCPT] = false;
				//selectedFigures[8] = "";
				selected[8].setText("");
			}
			if (numattach.isSelected()) {
				selectedFeaturesBoolean[NUMATTACH] = true;
				selectedFigures.add("e.numattach");
				selected[9].setText(data[currUID - 1][9]);
				//System.out.print("10 " + selectedFigures[9] + " is
				// selected");
			} else {
				selectedFeaturesBoolean[NUMATTACH] = false;
				//selectedFigures[9] = "";
				selected[9].setText("");
			}
			if (size.isSelected()) {
				selectedFeaturesBoolean[SIZE] = true;
				selectedFigures.add("e.size");
				selected[10].setText(data[currUID - 1][10]);
				//System.out.print("11 " + selectedFigures[10] + " is
				// selected");
			} else {
				selectedFeaturesBoolean[SIZE] = false;
				selected[10].setText("");
			}
			if (mailref.isSelected()) {
				selectedFeaturesBoolean[MAILREF] = true;
				selectedFigures.add("e.mailref");
				selected[11].setText(data[currUID - 1][11]);
			} else {
				selectedFeaturesBoolean[MAILREF] = false;
				selected[11].setText("");
			}
			if (xmailer.isSelected()) {
				selectedFeaturesBoolean[XMAILER] = true;
				selectedFigures.add("e.xmailer");
				selected[12].setText(data[currUID - 1][12]);
			} else {
				selectedFeaturesBoolean[XMAILER] = false;
				selected[12].setText("");
			}
			if (dates.isSelected()) {
				selectedFeaturesBoolean[DATES] = true;
				selectedFigures.add("e.dates");
				selected[13].setText(data[currUID - 1][13]);
			} else {
				selectedFeaturesBoolean[DATES] = false;
				selected[13].setText("");
			}
			if (times.isSelected()) {
				selectedFeaturesBoolean[TIMES] = true;
				selectedFigures.add("e.times");
				selected[14].setText(data[currUID - 1][14]);
			} else {
				selectedFeaturesBoolean[TIMES] = false;
				selected[14].setText("");
			}
			if (flags.isSelected()) {
				selectedFeaturesBoolean[FLAGS] = true;
				selectedFigures.add("e.flags");
				selected[15].setText(data[currUID - 1][15]);
			} else {
				selectedFeaturesBoolean[FLAGS] = false;
				selected[15].setText("");
			}
			if (msghash.isSelected()) {
				selectedFeaturesBoolean[MSGHASH] = true;
				selectedFigures.add("e.msghash");
				selected[16].setText(data[currUID - 1][16]);
			} else {
				selectedFeaturesBoolean[MSGHASH] = false;
				selected[16].setText("");
			}
			if (rplyto.isSelected()) {
				selectedFeaturesBoolean[RPLYTO] = true;
				selectedFigures.add("e.rplyto");
				selected[17].setText(data[currUID - 1][17]);
			} else {
				selectedFeaturesBoolean[RPLYTO] = false;
				selected[17].setText("");
			}
			if (forward.isSelected()) {
				selectedFigures.add("e.forward");
				selectedFeaturesBoolean[FORWARD] = true;
				selected[18].setText(data[currUID - 1][18]);
			} else {
				selectedFeaturesBoolean[FORWARD] = false;
				selected[18].setText("");
			}
			if (type.isSelected()) {
				selectedFeaturesBoolean[TYPE] = true;
				selectedFigures.add("e.type");
				selected[19].setText(data[currUID - 1][19]);
			} else {
				selectedFeaturesBoolean[TYPE] = false;
				selected[19].setText("");
			}
			if (recnt.isSelected()) {
				selectedFeaturesBoolean[RECNT] = true;
				selectedFigures.add("e.recnt");
				selected[20].setText(data[currUID - 1][20]);
			} else {
				selectedFeaturesBoolean[RECNT] = false;
				selected[20].setText("");
			}
			if (subject.isSelected()) {
				selectedFeaturesBoolean[SUBJECT] = true;
				selectedFigures.add("e.subject");
				selected[21].setText(data[currUID - 1][21]);
			} else {
				selectedFeaturesBoolean[SUBJECT] = false;
				selected[21].setText("");
			}
			if (timeadj.isSelected()) {
				selectedFeaturesBoolean[TIMEADJ] = true;
				selectedFigures.add("e.timeadj");
				selected[22].setText(data[currUID - 1][22]);
			} else {
				selectedFeaturesBoolean[TIMEADJ] = false;
				selected[22].setText("");
			}
			if (folder.isSelected()) {
				selectedFeaturesBoolean[FOLDER] = true;
				selectedFigures.add("e.folder");
				selected[23].setText(data[currUID - 1][23]);
				//	    System.out.print("24 " + selectedFigures[23] + " is
				// selected");
			} else {
				selectedFeaturesBoolean[FOLDER] = false;
				//selectedFigures[23] = "";
				selected[23].setText("");
			}

			if (utime.isSelected()) {
				selectedFeaturesBoolean[UTIME] = true;
				selectedFigures.add("e.utime");
				selected[24].setText(data[currUID - 1][24]);
			} else {
				selectedFeaturesBoolean[UTIME] = false;
				selected[24].setText("");
			}
			if (senderLoc.isSelected()) {
				selectedFeaturesBoolean[SENDERLOC] = true;
				selectedFigures.add("e.senderLoc");
				selected[25].setText(data[currUID - 1][25]);

			} else {
				selectedFeaturesBoolean[SENDERLOC] = false;
				selected[25].setText("");
			}
			if (rcptLoc.isSelected()) {
				selectedFeaturesBoolean[RCPTLOC] = true;
				selectedFigures.add("e.rcptLoc");
				selected[26].setText(data[currUID - 1][26]);

			} else {
				selectedFeaturesBoolean[RCPTLOC] = false;
				selected[26].setText("");
			}
			if (insertTime.isSelected()) {
				selectedFigures.add("e.insertTime");
				selected[27].setText(data[currUID - 1][27]);
				selectedFeaturesBoolean[INSERTTIME] = true;
			} else {
				selectedFeaturesBoolean[INSERTTIME] = false;
				selected[27].setText("");
			}
			if (hourTime.isSelected()) {
				selectedFeaturesBoolean[HOURTIME] = true;
				selectedFigures.add("e.ghour");
				selected[28].setText(data[currUID - 1][28]);

			} else {
				selectedFeaturesBoolean[HOURTIME] = false;
				selected[28].setText("");
			}
			if (monthTime.isSelected()) {
				selectedFigures.add("e.gmonth");
				selected[29].setText(data[currUID - 1][29]);
				selectedFeaturesBoolean[MONTHTIME] = true;
			} else {
				selectedFeaturesBoolean[MONTHTIME] = false;
				selected[29].setText("");
			}
			if (messageBody.isSelected()) {
				selectedFeaturesBoolean[MESSAGEBODY] = true;
				selectedFigures.add("m.body");
				selected[30].setText("Email body would go here");//data[currUID-1][30]);

			} else {
				selectedFeaturesBoolean[MESSAGEBODY] = false;
				selected[30].setText("");
			}

		} catch (Exception e) {
			System.out.println("change data detected" + e);
			//problem with changed data.
		}

		//update the NB window
		//updateNB();
	}

	/**
	 * Visibility changed.
	 *
	 * @param visible the visible
	 */
	public final void visibilityChanged(boolean visible) {
		if (visible) {
			mails.setEnabled(mails.getItemCount() > 0);
		}
	}

	/**
	 * Gets the n gram.
	 *
	 * @return the n gram
	 */
	public final int getNGram() {

		int nglen = 5;
		try {
			nglen = Integer.parseInt(ngramLen.getText().trim());
		} catch (Exception t5) {
			JOptionPane.showMessageDialog(this, "Error getting ngram: " + t5, "Error", JOptionPane.ERROR_MESSAGE);
			//    System.out.println("Alert: prblem with grabing ngram length off
			// gui, defaults to 5");
		}
		return nglen;
	}

	/**
	 * Gets the threshold.
	 *
	 * @return the threshold
	 */
	public final double getThreshold() {

		return m_spinnerNumber.getNumber().doubleValue();
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public final String getTarget() {
		return (String) m_target.getSelectedItem();
	}

	/**
	 * Gets the target class.
	 *
	 * @return the target class
	 */
	public final String getTargetClass() {
		return ((String) m_target.getSelectedItem());
	}

	/**
	 * Checks if is stopwords.
	 *
	 * @return true, if is stopwords
	 */
	public final boolean isStopwords() {
		return m_boolstoplist;
	}

	/**
	 * Checks if is lower.
	 *
	 * @return true, if is lower
	 */
	public final boolean isLower() {
		return m_boollower;
	}

	/**
	 * get the type of attachments to use.
	 *
	 * @return the attach type
	 */
	public final String getAttachType() {

		int n = attach_type.getSelectedIndex();
		if (n == 0)
			return "%";
		else if (n == 1)
			return "text%";
		else
			return (String) attach_type.getSelectedItem();

	}

	/**
	 * Gets the hash.
	 *
	 * @return the hash
	 */
	public final Hashtable getHash() {
		if (stopHash.isEmpty()) {
			KeywordPanel kp = new KeywordPanel(m_emailConfiguration.getProperty(EMTConfiguration.STOPLIST));
			stopHash = kp.getHash(m_boollower);
		}
		return stopHash;
	}

}