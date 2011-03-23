/**
 * Copyright Columbia University 2003/2004
 */
package metdemo.Tools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.event.*;
// import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

import metdemo.EMTConfiguration;
import metdemo.DataBase.DataImportExport;
import metdemo.DataBase.DatabaseConnectionException;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;

import java.sql.SQLException;

/**
 * Prefernces setup using a gui window
 * 
 * Also database manipulation and setup using an internal database manager code
 * representation.
 * 
 * @author Shlomo Hershkop Columbia University 2003/2004
 */

public class Preferences extends JPanel {
	private String DOS_BATCH_FILE = "emtclient.bat";
	// private String CONFIGFILE = EMTConfiguration.DEFAULT_CONF_FILE;
	private static final String BAK = ".bak";

	private static final String createNEW = "CREATE_NEW";

	// private static final int NAME = 0;
	// private static final int VALUE = 1;

	private JTabbedPane m_tabs;
	private GeneralConfig m_genConfig;
	private DatabaseConfig m_database;
	private String m_status = new String("unknown");
	private EMTConfiguration m_emtconfig;
	private JButton buttonContinue;
	//based on later setting
	private final int i_derby =0;
	private final int i_postgres =1;
	private final int i_mysql =2;
	private final int i_mssql = 3;
	
	
	/**
	 * default constructor will use default config files.
	 */

	/** constructor with batch file names */
	public Preferences(EMTConfiguration emtconfig, boolean useStartButton) {
		// String dos, String bash) {
		// if (dos != null)
		// DOS_BATCH_FILE = new String(dos);

		// if (bash != null)
		// BASH_FILE = new String(bash);

		// repeat above stuff
		m_emtconfig = emtconfig;
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		GridBagConstraints constraints = new GridBagConstraints();
		m_tabs = new JTabbedPane();
		m_tabs.setPreferredSize(new Dimension(600, 400));
		m_genConfig = new GeneralConfig(emtconfig);
		m_database = new DatabaseConfig();

		m_tabs.addTab("General Setup", m_genConfig);
		m_tabs.addTab("Setup Database Schema", m_database);
		m_tabs.addTab("Import/Export Database", new DataImportExport());
		// add tabbed pane to this panel
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		gridbag.setConstraints(m_tabs, constraints);
		add(m_tabs);
		// add contnue button

		buttonContinue = new JButton(
				"Start EMT (will ignore any changes to above)");
		buttonContinue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(
						JDialog.class, Preferences.this);
				if (d == null) {
					System.err.println("Could not close dialog!");
				} else {
					m_status = "continue";
					d.setVisible(false);
					d.dispose();
				}
			}
		});
		if (!useStartButton) {
			buttonContinue.setEnabled(false);
		}
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.SOUTHEAST;
		gridbag.setConstraints(buttonContinue, constraints);
		add(buttonContinue);

		// add ok and cancel buttons
		JButton buttonOk = new JButton("Save changes to file");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (validateInput()) {
					saveChanges();
					Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(
							JDialog.class, Preferences.this);
					JOptionPane
							.showMessageDialog(
									Preferences.this,
									"You must restart EMT for your changes to take effect\nPlease Use the correct batch file (example "
											+ m_emtconfig
													.getProperty(EMTConfiguration.DOS_BATCH_FILE)
											+ ") to run EMT now");
					if (d == null) {
						System.err.println("Could not close dialog!");
					} else {
						d.setVisible(false);
						d.dispose();
					}
				}
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 1;
		gridbag.setConstraints(buttonOk, constraints);
		add(buttonOk);

		JButton buttonCancel = new JButton("Cancel/Quit");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(
						JDialog.class, Preferences.this);
				if (d == null) {
					System.err.println("Could not close dialog!");
				} else {
					d.setVisible(false);
					d.dispose();
				}
			}
		});
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weightx = 0.0;
		gridbag.setConstraints(buttonCancel, constraints);
		add(buttonCancel);
		buttonContinue.grabFocus();

	}
	/**
	 * to allow ease of use to grab the continue button and make it default
	 * focus....
	 * 
	 */
	public void grabFocusButton() {
		buttonContinue.grabFocus();
	}

	public final String getStatus() {
		return m_status;
	}

	private boolean validateInput() {
		return m_genConfig.validateInput();
	}

	private void saveChanges() {
		m_genConfig.saveChanges();
	}

	public void readSettingsFromFile() {
		m_genConfig.readSettingsFromFile();
	}

	private class DatabaseConfig extends JPanel {
		private JTextField m_user;
		private JPasswordField m_password;
		private JTextField m_host;
		private JComboBox m_chooseDB2;
	/*	private JRadioButton m_choosemysql2;
		private JRadioButton m_choosederby2;
		private JRadioButton m_choosePOSTGRES;*/
		// private JTextField m_database;
		// private JTextField m_schemafile;
		private JTextField m_newdb;
		private JButton m_step2;
		private JButton m_step1;
		private JButton m_schemasetup;
		private JButton m_execute;
		private EMTDatabaseConnection m_jdbc;
		private JComboBox m_dbs;

		public DatabaseConfig() {
			m_user = new JTextField(20);
			m_password = new JPasswordField(20);
			// m_database = new JTextField(20);
			// m_schemafile = new JTextField(20);
			// m_schemafile.setText("misc/schema.sql ");
			
			m_chooseDB2 = new JComboBox();
			m_chooseDB2.addItem("Java Based DB");
			m_chooseDB2.addItem("PostGres DB");
			m_chooseDB2.addItem("MYSQL DB");
			m_chooseDB2.addItem("Microsoft SQL");
			
			/*m_choosederby2 = new JRadioButton("Java Based DB");
			m_choosederby2.setSelected(false);
			m_choosePOSTGRES = new JRadioButton("PostGres DB");
			m_choosePOSTGRES.setSelected(false);
			m_choosemysql2 = new JRadioButton("MYSQL DB");
			m_choosemysql2.setSelected(false);
			ButtonGroup bg = new ButtonGroup();
			bg.add(m_choosederby2);
			bg.add(m_choosemysql2);
			bg.add(m_choosePOSTGRES);*/

			// set one of the buttons:
			if (m_emtconfig.getProperty(EMTConfiguration.DBTYPE).equals(
					DatabaseManager.sMYSQL)) {
				/*m_choosemysql2.setSelected(true);*/
				m_chooseDB2.setSelectedIndex(i_mysql);
			}
			else if (m_emtconfig.getProperty(EMTConfiguration.DBTYPE).equals(
					DatabaseManager.sPOSTGRES)) {
				/*m_choosePOSTGRES.setSelected(true);*/
				m_chooseDB2.setSelectedIndex(i_postgres);
			} else if (m_emtconfig.getProperty(EMTConfiguration.DBTYPE).equals(DatabaseManager.sMICROSOFTSQL)){
			
			m_chooseDB2.setSelectedIndex(i_mssql);
			}
			else {
				/*m_choosederby2.setSelected(true);*/
				m_chooseDB2.setSelectedIndex(i_derby);
			}

			m_host = new JTextField(20);
			m_host.setText("127.0.0.1");
			m_dbs = new JComboBox();
			m_newdb = new JTextField(20);
			m_step1 = new JButton("Step 1");
			m_step1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setupFirst();
				}
			});
			m_step2 = new JButton("Step2 Database");
			m_step2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					// check if java database
					if (/*m_choosederby2.isSelected()*/m_chooseDB2.getSelectedIndex() == i_derby) {
						JOptionPane.showMessageDialog(Preferences.this,
								"No need to setup schema for java db",
								"Notice", JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					try {
						// check if to switch
						if (m_user.getText().trim().length() > 1
								&& m_host.getText().trim().length() > 1)
							setupDatabase();
						else
							JOptionPane.showMessageDialog(Preferences.this,
									"Need user,[password,] and host", "Error",
									JOptionPane.ERROR_MESSAGE);
					} catch (DatabaseConnectionException dbc) {
						dbc.printStackTrace();
						JOptionPane.showMessageDialog(Preferences.this,
								"problem with db setup", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			m_schemasetup = new JButton("Final Setup");
			m_schemasetup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setupSchema();
				}
			});

			m_execute = new JButton("Execute and Create DB");
			m_execute.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					executeSchema();
				}
			});

			setupFirst();

		}

		/** setup up the first window which takes access info */
		private void setupFirst() {
			this.removeAll();
			GridBagLayout gridbag = new GridBagLayout();
			setLayout(gridbag);

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			/*addSetting("DB:", m_choosederby2, gridbag, constraints);
			addSetting("DB:", m_choosemysql2, gridbag, constraints);
			addSetting("DB:", m_choosePOSTGRES, gridbag, constraints);*/
			addSetting("db type", m_chooseDB2, gridbag, constraints);
			addSetting("User:", m_user, gridbag, constraints);
			addSetting("Password:", m_password, gridbag, constraints);
			addSetting("Host", m_host, gridbag, constraints);
			addSetting("", new EMTHelp(EMTHelp.DATABASESETUP), gridbag,
					constraints);
			addSetting("Next step, Login:", m_step2, gridbag, constraints);

			validate();
			repaint();

		}

		/** do acutal command to create/alter db */
		private void executeSchema() {

			// first try to get the schema file
			/*
			 * BufferedReader reader; try {
			 * 
			 * reader = new BufferedReader(new FileReader(new
			 * File(m_schemafile.getText().trim())));
			 *  } catch (IOException ee) {
			 * JOptionPane.showMessageDialog(Preferences.this, "Problem opening " +
			 * m_schemafile.getText() + "\n" + ee, "Error",
			 * JOptionPane.ERROR_MESSAGE); return; }
			 */
			// FETCH NEW DB NAME
			String name = m_newdb.getText().trim();
			name = name.replaceAll(" ", "");// clean out spaces
			name = name.replaceAll("\"", "");// no "
			name = name.replaceAll("'", "");
			name = name.replaceAll(",", "");

			// now to execute the schema file

			m_jdbc.switchCreate(name);
			// m_jdbc.updateSQLData("use " + name);

			if (m_jdbc.createNewEMTDatabase(name)) {
				JOptionPane.showMessageDialog(Preferences.this,
						"Successfully setup " + name);
			} else {
				JOptionPane.showMessageDialog(Preferences.this,
						"Problem in setup " + name);

			}

			setupFirst();
		}

		/** allows creation of db based on some schema */
		private void setupSchema() {

			this.removeAll();
			GridBagLayout gridbag = new GridBagLayout();
			setLayout(gridbag);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;

			if (m_dbs.getSelectedIndex() > 0)
				m_newdb.setText((String) m_dbs.getSelectedItem());

			addSetting("New Database:", m_newdb, gridbag, constraints);
			// addSetting("Schema File: ", m_schemafile, gridbag, constraints);
			addSetting("Execute:", m_execute, gridbag, constraints);
			addSetting("Restart", m_step1, gridbag, constraints);

			validate();
			repaint();
		}

		/** allows choosing a db to use */
		private void setupDatabase() throws DatabaseConnectionException {
			this.removeAll();
			GridBagLayout gridbag = new GridBagLayout();
			setLayout(gridbag);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			String data[][];
			// setup a connection and fetch databases;
			// TODO: add host location to all this

			// will be based on choice in previous window
			switch(m_chooseDB2.getSelectedIndex()){
				case i_derby:
					m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
							m_user.getText().trim(), new String(m_password
									.getPassword()).trim(), DatabaseManager.sDERBY);// "org.gjt.mm.mysql.Driver"
						break;
				case i_postgres:
					m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
							m_user.getText().trim(), new String(m_password
									.getPassword()).trim(),
							DatabaseManager.sPOSTGRES);
						break;
				case i_mysql:
					m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
							m_user.getText().trim(), new String(m_password
									.getPassword()).trim(), DatabaseManager.sMYSQL);// "org.gjt.mm.mysql.Driver"

			
						break;
				case i_mssql:
					m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
							m_user.getText().trim(), new String(m_password
									.getPassword()).trim(),
							DatabaseManager.sMICROSOFTSQL);
					break;
				default:
					throw new DatabaseConnectionException("Unknown db selected");	
			}
			
			
			
			
			
			/*if (m_choosederby2.isSelected()) {
				m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
						m_user.getText().trim(), new String(m_password
								.getPassword()).trim(), DatabaseManager.sDERBY);// "org.gjt.mm.mysql.Driver"
			} else if (m_choosemysql2.isSelected()) {
				m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
						m_user.getText().trim(), new String(m_password
								.getPassword()).trim(), DatabaseManager.sMYSQL);// "org.gjt.mm.mysql.Driver"

			} else if (m_choosePOSTGRES.isSelected()) {
				m_jdbc = DatabaseManager.LoadDB(m_host.getText().trim(), "",
						m_user.getText().trim(), new String(m_password
								.getPassword()).trim(),
						DatabaseManager.sPOSTGRES);
			} else {
				throw new DatabaseConnectionException("Unknown db selected");
			}*/
			boolean connected = false;
			try {
				connected = m_jdbc.connect(true);
			} catch (DatabaseConnectionException d) {
				System.out.println(d);
			}
			if (connected == false) {
				JOptionPane.showMessageDialog(Preferences.this,
						"Could not connect with user,password,host", "Error",
						JOptionPane.ERROR_MESSAGE);
				setupFirst();

			} else {
				// setup the buttons.
				try {
					data = m_jdbc.getAllDatabases();
					// data = m_jdbc.getRowData();
					m_dbs.removeAllItems();
					m_dbs.addItem("Create New DB");
					if (data != null && data.length > 0) {

						for (int i = 0; i < data.length; i++) {
							if (!data[i][0].equals("mysql"))// mysql is reserved
								m_dbs.addItem(data[i][0]);
						}
					}
					m_dbs.setSelectedIndex(0);

				} catch (SQLException sl) {
					System.out.println("problem sql: " + sl);

				}

				JButton m_describe = new JButton("Describe Current DB & Schema");
				m_describe.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// get the name of the db

						if (m_jdbc == null) {
							JOptionPane.showMessageDialog(Preferences.this,
									"Need Valid db connection", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						String name = ((String) m_dbs.getSelectedItem()).trim();
						if (name == null) {
							JOptionPane.showMessageDialog(Preferences.this,
									"Problem fetch the db name", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (name.equals("Create New DB")) {
							JOptionPane.showMessageDialog(Preferences.this,
									"The DB will be created in next step",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;

						}
						// so we have valid name and connection at this point
						try {
							String data[][];
							String subdata[][];
							StringBuffer report = new StringBuffer();
							data = m_jdbc
									.getSQLData("show tables from " + name);
							// data = m_jdbc.getRowData();

							if (data.length > 0) {
								for (int i = 0; i < data.length; i++) {
									report
											.append("Table: " + data[i][0]
													+ "\n");
									subdata = m_jdbc.getSQLData("describe "
											+ name + "." + data[i][0].trim());
									// subdata = m_jdbc.getRowData();
									if (subdata.length > 0)
										for (int j = 0; j < subdata.length; j++)
											report.append("   " + subdata[j][0]
													+ "---" + subdata[j][1]
													+ "\n");

								}

								JTextArea textArea = new JTextArea(report
										.toString());
								textArea.setLineWrap(true);
								textArea.setWrapStyleWord(true);
								JScrollPane areaScrollPane = new JScrollPane(
										textArea);
								areaScrollPane
										.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
								areaScrollPane.setPreferredSize(new Dimension(
										550, 380));

								JOptionPane.showMessageDialog(Preferences.this,
										areaScrollPane);

							} else
								report.append("No Tables in database");

						} catch (SQLException s2) {
							JOptionPane.showMessageDialog(Preferences.this, s2,
									"Error", JOptionPane.ERROR_MESSAGE);

						}

					}
				});

				addSetting("Choose Database", m_dbs, gridbag, constraints);
				addSetting("", m_describe, gridbag, constraints);
				addSetting("Next->Create/Alter Database", m_schemasetup,
						gridbag, constraints);
				addSetting("Back", m_step1, gridbag, constraints);

				validate();
				repaint();
			}
		}

		private void addSetting(String title, Component comp,
				GridBagLayout gridbag, GridBagConstraints constraints) {
			constraints.gridx = 0;
			JLabel label = new JLabel(title);
			gridbag.setConstraints(label, constraints);
			add(label);

			constraints.gridx = 1;
			gridbag.setConstraints(comp, constraints);
			add(comp);

			constraints.gridy++;
		}

	}// end databse config class

	private class GeneralConfig extends JPanel {
		private static final int FIELD_WIDTH = 20;
	/*	private JRadioButton m_choosemysql;
		private JRadioButton m_choosederby;
		private JRadioButton m_choosePOSTGRES;
		
		private JRadioButton m_choosecachemysql;
*/
		JComboBox m_chooseDB;
		private JTextField m_textJava;
		private JTextField m_textDb;
		private JTextField m_textDbHost;
		private JTextField m_textDbUser;
		private JPasswordField m_textDbPassword;
		private JTextField m_textMySql;
		private JTextField m_textAliases;
		private JTextField m_textHotwords;
		private JTextField m_Stopwords;
		private JTextField m_Cache;

		private JComboBox m_EMTScheme;
		private JCheckBox m_Show;
		private JTextField m_ConfigFile;
		private JTextField m_DosBatch;
		EMTConfiguration emtc;

		public GeneralConfig(EMTConfiguration emtconfig) {
			GridBagLayout gridbag = new GridBagLayout();
			setLayout(gridbag);
			GridBagConstraints constraints = new GridBagConstraints();
			emtc = emtconfig;
			constraints.gridx = 0;
			constraints.gridy = 0;

			m_Show = new JCheckBox("Display this preference window on startup",
					false);

			// addSetting("", m_Show, gridbag, constraints);

			// lets add it to the top
			constraints.gridx = 0;
			constraints.gridwidth = 2;
			gridbag.setConstraints(m_Show, constraints);
			add(m_Show);

			constraints.gridwidth = 1;
			constraints.gridy++;

			//datbase type drop down list:
			m_chooseDB = new JComboBox();
			m_chooseDB.addItem("Java Based DB");
			m_chooseDB.addItem("PostGres DB");
			m_chooseDB.addItem("MYSQL DB");
			m_chooseDB.addItem("Microsoft SQL");
			/*m_choosederby = new JRadioButton("Java Based DB");
			m_choosederby.setSelected(false);*/
			
		/*	m_choosePOSTGRES = new JRadioButton("PostGres DB");
			m_choosePOSTGRES.setSelected(false);*/
			
			
			/*m_choosemysql = new JRadioButton("MYSQL DB");

			m_choosemysql.setSelected(false);
			m_choosecachemysql = new JRadioButton("MYSQL Cache DB");
			m_choosecachemysql.setEnabled(false);// will remove till we have
													// it riht
			m_choosecachemysql.setSelected(false);*/

			/*ButtonGroup bg = new ButtonGroup();
			bg.add(m_choosederby);
			bg.add(m_choosePOSTGRES);
			bg.add(m_choosemysql);
			bg.add(m_choosecachemysql);*/

			// addSetting("",m_choosederby,gridbag,constraints);
			constraints.gridx = 1;
			JPanel sub2 = new JPanel();
			/*sub2.add(m_choosederby);
			sub2.add(m_choosePOSTGRES);*/
			sub2.add(m_chooseDB);
			
			gridbag.setConstraints(sub2, constraints);
			add(sub2);
			
			
			
						
			// add test button

			JButton testDB = new JButton(
					"<HTML>Test Database<BR> Connection</html>");
			testDB
					.setToolTipText("Click to test your settings by trying to establish db connection");
			constraints.gridx = 0;
			constraints.gridheight = 2;
			gridbag.setConstraints(testDB, constraints);
			add(testDB);
			constraints.gridheight = 1;

			constraints.gridx = 1;
			// todo check and try to connect to database
			testDB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String l_dbtype = getDBType();
					String l_dbname = getDb();
					String l_host = getDbHost();
					String l_dbps = getDbPassword();
					String l_dbuser = getDbUser();

					// check for missing info
					if (l_dbuser.length() < 1) {
						JOptionPane.showMessageDialog(Preferences.this,
								"Problem with no user name");
						return;
					}

					if (l_host.length() < 1) {
						JOptionPane.showMessageDialog(Preferences.this,
								"Problem with blank host name");
						return;
					}

					// try to make a connection:
					try {
						System.out.println("test type: " + l_dbtype);
						System.out.println("test name: " + l_dbname);
						System.out.println("test host: " + l_host);
						System.out.println("test pass: " + l_dbps);
						System.out.println("test user: " + l_dbuser);
						
						
						EMTDatabaseConnection jdbcView = DatabaseManager
								.LoadDB(l_host, l_dbname, l_dbuser, l_dbps,
										l_dbtype);
						// check for failure
						if (jdbcView.connect(false) == false) {
							JOptionPane
									.showMessageDialog(Preferences.this,
											"Error Establishing connection with current settings");
							return;
						}

					} catch (Exception e2) {
						System.err
								.println("Couldn't connect to database with these paramaters, check the batch file");
						return;
					}

					JOptionPane.showMessageDialog(Preferences.this,
							"Created Connection to the underlying database.");

				}
			});

			constraints.gridy++;

			// addSetting("",m_choosemysql,gridbag,constraints);

			JPanel sub = new JPanel();
			sub.add(m_chooseDB);
			/*sub.add(m_choosemysql);
			sub.add(m_choosecachemysql);
*/
			gridbag.setConstraints(sub, constraints);
			add(sub);

			constraints.gridy++;

			if (emtconfig.hasProperty(EMTConfiguration.DBTYPE)) {
				if (emtconfig.getProperty(EMTConfiguration.DBTYPE).equals(
						DatabaseManager.sDERBY)) {
					/*m_choosederby.setSelected(true);*/
					m_chooseDB.setSelectedIndex(i_derby);
				}else if (emtconfig.getProperty(EMTConfiguration.DBTYPE).equals(
						DatabaseManager.sPOSTGRES)) {
					/*m_choosePOSTGRES.setSelected(true);*/
					m_chooseDB.setSelectedIndex(i_postgres);
				}  else if (emtconfig.getProperty(EMTConfiguration.DBTYPE)
						.equals(DatabaseManager.sMICROSOFTSQL)) {
					/*m_choosecachemysql.setSelected(true);*/
					m_chooseDB.setSelectedIndex(i_mssql);
				} else {
					/*m_choosemysql.setSelected(true);*/
					m_chooseDB.setSelectedIndex(i_mysql);
				}
			}

			m_ConfigFile = new JTextField(FIELD_WIDTH);
			setConfig(emtconfig.getProperty(EMTConfiguration.CONFIGNAME));
			addSetting("Config File", m_ConfigFile, gridbag, constraints);

			m_DosBatch = new JTextField(FIELD_WIDTH);
			setDosBatch(emtconfig.getProperty(EMTConfiguration.DOS_BATCH_FILE));
			addSetting("EMT Batch File", m_DosBatch, gridbag, constraints);

			// setBash(BASH_FILE);
			// addSetting(BASH_FILE, m_Bash, gridbag, constraints);

			m_textJava = new JTextField(FIELD_WIDTH);
			// addSetting("Java Location", m_textJava, gridbag, constraints);

			m_textDb = new JTextField(FIELD_WIDTH);
			addSetting("Database Name", m_textDb, gridbag, constraints);

			m_textDbHost = new JTextField(FIELD_WIDTH);
			addSetting("Database Host", m_textDbHost, gridbag, constraints);

			m_textDbUser = new JTextField(FIELD_WIDTH);
			addSetting("Database User", m_textDbUser, gridbag, constraints);

			m_textDbPassword = new JPasswordField(FIELD_WIDTH);

			addSetting("Database Password", m_textDbPassword, gridbag,
					constraints);

			m_textMySql = new JTextField(FIELD_WIDTH);
			// addSetting("MySql Location", m_textMySql, gridbag, constraints);

			m_textAliases = new JTextField(FIELD_WIDTH);
			addSetting("Aliases File", m_textAliases, gridbag, constraints);

			m_textHotwords = new JTextField(FIELD_WIDTH);
			addSetting("Hotword File", m_textHotwords, gridbag, constraints);

			m_Stopwords = new JTextField(FIELD_WIDTH);
			addSetting("Stopword File", m_Stopwords, gridbag, constraints);

			m_Cache = new JTextField(FIELD_WIDTH);
			addSetting("Cache Location", m_Cache, gridbag, constraints);

			m_EMTScheme = new JComboBox();
			m_EMTScheme.addItem("Java Default");
			m_EMTScheme.addItem("System Like");
			m_EMTScheme.addItem("Substance light blue");
			m_EMTScheme.addItem("Substance Office 2007 blue");

			addSetting("EMT GUI Color Scheme", m_EMTScheme, gridbag, constraints);

		}

		private void addSetting(String title, Component comp,
				GridBagLayout gridbag, GridBagConstraints constraints) {
			constraints.gridx = 0;
			JLabel label = new JLabel(title);
			gridbag.setConstraints(label, constraints);
			add(label);

			constraints.gridx = 1;
			gridbag.setConstraints(comp, constraints);
			add(comp);

			constraints.gridy++;
		}

		public boolean validateInput() {
			return true;
		}

		public void saveChanges() {

			DOS_BATCH_FILE = getDosBatch();
			// BASH_FILE = getBash();
			// DOS
			try {
				// make backup of file first
				File oldFile = new File(DOS_BATCH_FILE);
				if (oldFile.exists()) {
					oldFile.renameTo(new File(DOS_BATCH_FILE + BAK));
				}

				// write new file
				File file = new File(DOS_BATCH_FILE);
				file.createNewFile();
				PrintWriter writer = new PrintWriter(new FileWriter(file));
				writer.println("java -Xmx512M -jar emt.jar -c " + getConfig());

				/*
				 * writer.println("@echo off"); writer.println("set DOS_BATCH=" +
				 * getDosBatch()); writer.println("set BASH_BATCH=" +
				 * getBash()); writer.println("set SHOW=" + getShow());
				 * writer.println("set JAVA=" + getJava()); writer.println("set
				 * CLASSPATH=" + getClasspath().replaceAll(":", ";"));
				 * writer.println("set DB=" + getDb()); writer.println("set
				 * DBHOST=" + getDbHost()); writer.println("set DBUSER=" +
				 * getDbUser()); writer.println("set DBPASSWORD=" +
				 * getDbPassword()); writer.println("set MYSQL=" + getMySql());
				 * writer.println("set ALIASES=" + getAliases());
				 * writer.println("set HOTWORDS=" + getHotwords());
				 * writer.println("set STOPWORDS=" + getStopwords());
				 * writer.println(""); // writer.println(":: check java
				 * version"); //writer.println("%JAVA% -version 2>
				 * javaver.txt"); //writer.println("find \"1.4.0\" javaver.txt >
				 * devnull"); //writer.println("if errorlevel 1 goto
				 * WRONGJAVA"); writer.println(""); writer .println("%JAVA%
				 * -Xmx128m -classpath %CLASSPATH% metdemo.winGui -v -h %DBHOST%
				 * -db %DB% -u %DBUSER% " + "-p %DBPASSWORD% -mysql %MYSQL%
				 * -alias %ALIASES% -w %HOTWORDS% -s %STOPWORDS% -dosb
				 * %DOS_BATCH% -bb %BASH_BATCH% -sh %SHOW%");
				 * writer.println("");
				 */
				// writer.println("goto DONE");
				// writer.println("");
				// writer.println(":WRONGJAVA");
				// writer.println("echo Trying to run with the wrong version of
				// java. Edit emtclient.bat and change \"set JAVA=java\" to
				// something like \"set
				// JAVA=c:\\j2sdk1.4.0_03\\bin\\java.exe\".");
				// writer.println("");
				// writer.println(":DONE");
				// writer.println("del javaver.txt");
				// writer.println("del devnull");
				writer.close();
				writer = null;
				emtc.putProperty(EMTConfiguration.CONFIGNAME, getConfig());
				if (getAliases().length() > 1) {
					emtc.putProperty(EMTConfiguration.ALIASFILENAME,
							getAliases());
				}
				if (getDbHost().length() > 1) {
					emtc.putProperty(EMTConfiguration.DBHOST, getDbHost());
				}
				if (getDb().length() > 1) {
					emtc.putProperty(EMTConfiguration.DBNAME, getDb());
				}

				emtc.putProperty(EMTConfiguration.DBPASSWORD, getDbPassword());

				if (getDbUser().length() > 1) {
					emtc.putProperty(EMTConfiguration.DBUSERNAME, getDbUser());
				}
				if (getDosBatch().length() > 1) {
					emtc.putProperty(EMTConfiguration.DOS_BATCH_FILE,
							getDosBatch());
				}
				emtc.putProperty(EMTConfiguration.SHOW, getShow());
				if (getStopwords().length() > 1) {
					emtc.putProperty(EMTConfiguration.STOPLIST, getStopwords());
				}
				if (getCache().length() > 1) {
					emtc.putProperty(EMTConfiguration.CACHE, getCache());
				}
				if (getHotwords().length() > 1) {
					emtc.putProperty(EMTConfiguration.WORDLIST, getHotwords());
				}
				switch(m_chooseDB.getSelectedIndex()){
					case i_derby:
						emtc.putProperty(EMTConfiguration.DBTYPE,
								DatabaseManager.sDERBY);
						break;
					case i_mysql:
						emtc.putProperty(EMTConfiguration.DBTYPE,
								DatabaseManager.sMYSQL);
						break;
					case i_mssql:
						emtc.putProperty(EMTConfiguration.DBTYPE,
								DatabaseManager.sMICROSOFTSQL);
						break;
					case i_postgres:
						emtc.putProperty(EMTConfiguration.DBTYPE,
								DatabaseManager.sPOSTGRES);
						break;
					default:
						emtc.putProperty(EMTConfiguration.DBTYPE,
								DatabaseManager.sDERBY);
						break;
				}
				
				
				
				/*if (m_choosederby.isSelected()) {
					emtc.putProperty(EMTConfiguration.DBTYPE,
							DatabaseManager.sDERBY);
				} else if (m_choosecachemysql.isSelected()) {
					emtc.putProperty(EMTConfiguration.DBTYPE,
							DatabaseManager.sCMYSQL);
				} else if (m_choosePOSTGRES.isSelected()) {
					emtc.putProperty(EMTConfiguration.DBTYPE,
							DatabaseManager.sPOSTGRES);
				} else {

					emtc.putProperty(EMTConfiguration.DBTYPE,
							DatabaseManager.sMYSQL);
				}*/
				emtc.putProperty(EMTConfiguration.SCHEME_COLOR, ""
						+ getScheme());

				emtc.saveFileToDisk((emtc
						.getProperty(EMTConfiguration.CONFIGNAME)));
			} catch (java.io.IOException ioex) {
				JOptionPane.showMessageDialog(this,
						"Error while saving changes: " + ioex);
			}
			/*
			 * // Bash try { // make backup of file first File oldFile = new
			 * File(BASH_FILE); if (oldFile.exists()) { oldFile.renameTo(new
			 * File(BASH_FILE + BAK)); } // write new file File file = new
			 * File(BASH_FILE); file.createNewFile(); PrintWriter writer = new
			 * PrintWriter(new FileWriter(file));
			 * 
			 * writer.println("#!/bin/bash"); writer.println("#");
			 * writer.println("# MET gui startup script for development (can use
			 * but doesn't require jar)"); writer.println("#");
			 * writer.println("");
			 * 
			 * writer.println("DOS_BATCH=" + getDosBatch());
			 * writer.println("BASH_BATCH=" + getBash()); writer.println("SHOW=" +
			 * getShow()); writer.println("JAVA=" + getJava());
			 * writer.println("DBHOST=" + getDbHost()); writer.println("DBNAME=" +
			 * getDb()); writer.println("USER=" + getDbUser());
			 * writer.println("PASS=" + getDbPassword());
			 * writer.println("MYSQL=" + getMySql()); writer.println("WORDLIST=" +
			 * getHotwords()); writer.println("STOPLIST=" + getStopwords());
			 * 
			 * writer.println("ALIASES=" + getAliases());
			 * 
			 * writer.println(""); writer.println("export CLASSPATH=" +
			 * getClasspath().replaceAll(";", ":")); writer.println(""); writer
			 * .println("$JAVA metdemo/winGui -h $DBHOST -db $DBNAME -u $USER -p
			 * $PASS -mysql $MYSQL -alias $ALIASES -w $WORDLIST -s $STOPLIST -sh
			 * $SHOW -bb $BASH_BATCH -dosb $DOS_BATCH $@");
			 * 
			 * writer.close(); writer = null;
			 * 
			 * try { // try to set the permissions. this will fail silently on //
			 * windows. Runtime.getRuntime().exec("chmod u+x emtclient"); }
			 * catch (Exception ex) { // ignore } } catch (java.io.IOException
			 * ioex) { JOptionPane.showMessageDialog(this, "Error while saving
			 * changes: " + ioex); }
			 */

		}

		public void readSettingsFromFile() {
			// parse the file
			// try {
			// EMTConfiguration emtc2 = new EMTConfiguration();
			// BufferedReader reader = null;
			// will confirm the batch file name

			String configname = emtc.getProperty(EMTConfiguration.CONFIGNAME);
			String msg = new String("\n");
			if (configname == null) {
				msg = "Please hit save button when done to create a config file\n";
				configname = EMTConfiguration.DEFAULT_CONF_FILE;
			}

			int i, currentNumber = 0;
			String[] configs = EMTConfiguration.getAllConfigFiles();

			String[] configs2 = new String[configs.length + 1];
			for (i = 0; i < configs.length; i++) {
				// check for matching current
				if (configs[i].equalsIgnoreCase(configname)) {
					currentNumber = i;
				}
				configs2[i] = configs[i];
			}
			configs2[i] = createNEW;

			String choose = (String) JOptionPane
					.showInputDialog(
							Preferences.this,
							msg
									+ "Please confirm your current EMT configuration file\nYou can switch to another by using the drop down list\nOr create new one (indicated in the list):\n\nCurrent configuration is "
									+ configname + "\n", "Choose Config",
							JOptionPane.PLAIN_MESSAGE, null, configs2,
							configs2[currentNumber]);

			if (choose != null) {
				// try {

				if (choose.equals(createNEW)) {

					// need to fetch the name here.
					choose = JOptionPane.showInputDialog(
							"Please Enter a config name:", "config.txt");

				}

				if (!emtc.loadFileFromDisk((choose))) {
					emtc.loadFileFromDisk((EMTConfiguration.DEFAULT_CONF_FILE));
				}

				/*
				 * reader = new BufferedReader(new FileReader(new
				 * File(choose))); DOS_BATCH_FILE = choose;
				 */
				// CONFIGFILE = choose;
				setConfig(choose);
				// } catch (java.io.IOException ioex2) {
				// JOptionPane.showMessageDialog(this, "Error reading config
				// file: " + ioex2 + " will try default");
				// }
			} else {
				System.exit(-1);
			}

			setDb(emtc.getProperty(EMTConfiguration.DBNAME));
			setDbHost(emtc.getProperty(EMTConfiguration.DBHOST));
			setDbUser(emtc.getProperty(EMTConfiguration.DBUSERNAME));
			setDbPassword(emtc.getProperty(EMTConfiguration.DBPASSWORD));
			setMySql(emtc.getProperty(EMTConfiguration.DBPATH));
			setAliases(emtc.getProperty(EMTConfiguration.ALIASFILENAME));
			setHotwords(emtc.getProperty(EMTConfiguration.WORDLIST));
			setStopwords(emtc.getProperty(EMTConfiguration.STOPLIST));
			setCache(emtc.getProperty(EMTConfiguration.CACHE));
			setDosBatch(emtc.getProperty(EMTConfiguration.DOS_BATCH_FILE));
			setShow(emtc.getProperty(EMTConfiguration.SHOW));
			setScheme(Integer.parseInt(emtc
					.getProperty(EMTConfiguration.SCHEME_COLOR)));
		}

		/*
		 * if (reader == null) reader = new BufferedReader(new FileReader(new
		 * File(DOS_BATCH_FILE))); String line = null; while ((line =
		 * reader.readLine()) != null) { if (line.startsWith("set ")) { String
		 * nameValue[] = line.substring(4).split("="); if (nameValue.length > 1) {
		 * if (nameValue[NAME].equals("JAVA")) {
		 * setJava(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("CLASSPATH")) { // ignore } else if
		 * (nameValue[NAME].equals("DB")) { setDb(nameValue[VALUE].trim()); }
		 * else if (nameValue[NAME].equals("DBHOST")) {
		 * setDbHost(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("DBUSER")) {
		 * setDbUser(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("DBPASSWORD")) {
		 * setDbPassword(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("MYSQL")) {
		 * setMySql(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("ALIASES")) {
		 * setAliases(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("HOTWORDS")) {
		 * setHotwords(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("STOPWORDS")) {
		 * setStopwords(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("DOS_BATCH")) {
		 * setDosBatch(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("CONFIG")) {
		 * setConfig(nameValue[VALUE].trim()); } else if
		 * (nameValue[NAME].equals("SHOW")) { setShow(nameValue[VALUE].trim()); }
		 * else { System.err.println("Unrecognized variable: " +
		 * nameValue[NAME]); } } } }
		 */

		public void cancelChanges() {
			return;
		}

		public void setJava(String s) {
			m_textJava.setText(s);
			m_textJava.setCaretPosition(0);
		}

		public String getJava() {
			return m_textJava.getText().trim();
		}

		public String getClasspath() {
			return (".;lib/mmmysql2.jar;lib/chapman.jar;lib/mail.jar;lib/activation.jar;lib/emt.jar");
		}

		public void setDb(String s) {
			m_textDb.setText(s);
			m_textDb.setCaretPosition(0);
		}

		public String getDb() {
			return m_textDb.getText().trim();
		}

		public void setDbHost(String s) {
			m_textDbHost.setText(s);
			m_textDbHost.setCaretPosition(0);
		}

		public String getDbHost() {
			return m_textDbHost.getText().trim();
		}

		public void setDbUser(String s) {
			m_textDbUser.setText(s);
			m_textDbUser.setCaretPosition(0);
		}

		public String getDbUser() {
			return m_textDbUser.getText().trim();
		}

		public void setDbPassword(String s) {
			m_textDbPassword.setText(s);
			m_textDbPassword.setCaretPosition(0);
		}

		public String getDbPassword() {
			return new String(m_textDbPassword.getPassword()).trim();
		}

		public void setMySql(String s) {
			m_textMySql.setText(s);
			m_textMySql.setCaretPosition(0);
		}

		public String getMySql() {
			return m_textMySql.getText().trim();
		}

		public void setAliases(String s) {
			m_textAliases.setText(s);
			m_textAliases.setCaretPosition(0);
		}

		public String getAliases() {
			return m_textAliases.getText().trim();
		}

		public void setHotwords(String s) {
			m_textHotwords.setText(s);
			m_textHotwords.setCaretPosition(0);
		}

		public final String getHotwords() {
			return m_textHotwords.getText().trim();
		}
		public final void setStopwords(String s) {
			m_Stopwords.setText(s);
			m_Stopwords.setCaretPosition(0);
		}

		public final void setCache(String s) {
			m_Cache.setText(s);
			m_Cache.setCaretPosition(0);
		}

		public final String getStopwords() {
			return m_Stopwords.getText().trim();
		}

		public final String getCache() {
			return m_Cache.getText().trim();
		}

		public final void setDosBatch(String s) {
			m_DosBatch.setText(s);
			m_DosBatch.setCaretPosition(0);
		}

		public final String getDosBatch() {
			return m_DosBatch.getText().trim();
		}

		public final void setConfig(String s) {
			m_ConfigFile.setText(s);
			m_ConfigFile.setCaretPosition(0);
		}

		/*public final void setDBType(boolean b) {

			m_choosederby.setSelected(b);

		}*/

		public final String getConfig() {
			return m_ConfigFile.getText().trim();
		}

		public final String getDBType() {
			switch(m_chooseDB.getSelectedIndex()){
				case i_derby:
					return DatabaseManager.sDERBY;
				case i_mysql:
					return DatabaseManager.sMYSQL;
				case i_mssql:
					return DatabaseManager.sMICROSOFTSQL;
				case i_postgres:
					return DatabaseManager.sPOSTGRES;
				
			}
			return null;
			
			
			/*if (m_choosederby.isSelected()) {
				return DatabaseManager.sDERBY;
			}

			return DatabaseManager.sMYSQL;*/
		}

		public final int getScheme() {
			return m_EMTScheme.getSelectedIndex();
		}

		public final void setScheme(int n) {
			try {
				m_EMTScheme.setSelectedIndex(n);
			} catch (IllegalArgumentException e) {
				m_EMTScheme.setSelectedIndex(0);
			}
		}

		public final void setShow(String s) {

			if (s == null || !s.toLowerCase().equals("false"))
				m_Show.setSelected(true);
			else
				m_Show.setSelected(false);

			//m_Show.setText(s);
			//m_Show.setCaretPosition(0);
		}

		public final String getShow() {
			return "" + m_Show.isSelected();
			//return m_Show.getText().trim();
		}

	}
}