package metdemo.Tools;

import javax.swing.JButton;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class which generates the help buttons and throws back correct help file for
 * each window.
 * <P>
 * Need to allow conformity in the EMT package
 * <P>
 */

public class EMTHelp extends JPanel {
	private int type;
	private JButton jp;
	JTextArea textArea;
	private JScrollPane areaScrollPane;
	static String HELP = new String("Help");

	/** constructor */
	/*
	 * public EMTHelp() { }
	 */
	/** constructor */
	public EMTHelp(int n) {
		type = n;
		textArea = new JTextArea(getMSG(type));
		//textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(550, 380));
		ImageIcon icon = Utils.createImageIcon("metdemo/images/helpbook.gif", "Help");

		jp = new JButton(HELP, icon);
		jp.setBackground(Color.orange);
		jp.setToolTipText("Click for get specific help");
		//"We know you didnt read the User Manual, so here is some help!");
		jp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOptionPane.showMessageDialog(EMTHelp.this.getParent(), areaScrollPane);
			}
		});
		this.add(jp);

	}

	public final void showME() {
		JOptionPane.showMessageDialog(EMTHelp.this, areaScrollPane);
	}

	/*
	 * public final void setMSG(int n) { textArea.setText(getMSG(type)); type =
	 * n; }
	 */

	public final String get() {
		String message = "";
		return message;
	}

	public static final int VISUALCLIQUE = 0;
	public static final int ADDRESSLIST = 1;
	public static final int HELLINGERCONF = 2;
	public static final int EMAILFLOW = 3;
	public static final int AVERAGECOMMTIME = 4;
	public static final int SIMILARUSERS = 5;
	public static final int USAGEHISTORGRAM = 6;
	public static final int MESSAGE = 7;
	public static final int LOADDB = 8;
	public static final int ATTACHSTATS = 9;
	public static final int SQLVIEW = 10;
	public static final int MODELINFO = 11;
	public static final int USERCLIQUES = 12;
	public static final int EMAILFEATURES = 13;
	public static final int ABOUT = 14;
	public static final int GENERAL = 15;
	public static final int HELLINGERCLIQUE = 16;
	public static final int DATABASESETUP = 17;
	public static final int VIRUSSCAN = 18;
	public static final int ENCLAVE = 19;
	public static final int REPORTS = 20;
	public static final int ABOUTEMT = 21;
	public static final int EMTHELP = 22;
	public static final int FANALYZER = 23;
	public static final int EMTPARSER = 24;
	public static final int EMTFORENSIC = 20;
    public static final int EMTSEARCH = 25;
    public static final int FINANCEHELP = 26;
	public static final int SHDETECTOR = 27;
	public static final int DOCFLOW = 28;
	public static final int CLEANDATA = 29;
    
    
    
	public final String getMSG(final int t) {

		switch (t) {
			case (VISUALCLIQUE) :
				return getVisualClique();
			case (ADDRESSLIST) :
				return getAddressList();
			case (HELLINGERCONF) :
				return getHellingerConf();
			case (EMAILFLOW) :
				return getEmailFlow();
			case (AVERAGECOMMTIME) :
				return getAverageCommTime();
			case (SHDETECTOR) :
				return getSHDetector();
			case (SIMILARUSERS) :
				return getSimilarUsers();
			case (USAGEHISTORGRAM) :
				return getUsageHistogram();
			case (MESSAGE) :
				return getMessage();
			case (LOADDB) :
				return getLoadDB();
			case (ATTACHSTATS) :
				return getAttachStats();
			case (SQLVIEW) :
				return getSQLView();
			case (MODELINFO) :
				return getModelInfo();
			case (USERCLIQUES) :
				return getUserCliques();
			case (EMAILFEATURES) :
				return getEmailFeatures();
			case (ABOUT) :
				return getAbout();
			case (GENERAL) :
				return getGeneral();
			case (HELLINGERCLIQUE) :
				return getHellingerClique();
			case (DATABASESETUP) :
				return getDatabaseSetup();
			case (VIRUSSCAN) :
				return getVirusScan();
			case (ENCLAVE) :
				return getEnclave();
			case (REPORTS) :
				return getReports();
			case (ABOUTEMT) :
				return getAboutEMT();
			case (EMTHELP) :
				return topEMThelp();
			case (FANALYZER) :
				return getFAnalyzer();
			case (EMTPARSER) :
				return getEMTPARSER();
            case(EMTSEARCH):
                return getEMTSEARCH();
            case(FINANCEHELP):
                return getFinance();
            case(DOCFLOW):
                return getDocFlow();
            case(CLEANDATA):
            	return getCleanData();
            
		}

		return "STD help info";
	}

	/** for visual clqiues */
	public static String getVisualClique() {
		return new String("(Please read EMTUserManual for more detail)\n" + "\n"
				+ "The Visual Clique window is a user interface that displays graphics\n"
				+ "showing the activities of a group of cliques and of a single clique. \n" + "\n" + "=====\n"
				+ " Run\n" + "=====\n" + "\n" + "Set up the configuration:\n"

				+ "1. Select an email account from the top drop-down list\n" + "\n"
				+ "2. Enter the range of date in two date boxes.\n" + "\n"
				+ "3. Click \"Get Statistical Info.\" button to see the statistical\n"
				+ "   information of selected account and threshold." + "\n"
				+ "4. Algorithm features two radio buttons that enable you to select which\n"
				+ "   algorithm you wish to apply. EMT utilizes two different algorithms to\n"
				+ "   calculate clique, With Keyword (Common Subject Wors) or Without.\n" + "\n"
				+ "5. Display Panel Size features three radio buttons that change the scale\n"
				+ "   of the graph. This is useful when there are too many cliques and\n"
				+ "   connections to  display clearly on a small screen. Select the scale,\n"
				+ "   either Small, Medium, or Large, to configure the graph size.\n" + "\n"
				+ "6. Minimum Connection text box sets a threshold for connection. EMT\n"
				+ "   creates a connection between two email account pairs  when they have\n"
				+ "   exchanged more emails than the minimum threshold.\n" + "\n"
				+ "7. Use Frequent Users checkbox configures EMT to display only cliques\n"
				+ "   calculated by the \"frequent email account.\" The Limit text box,\n"
				+ "   to the right of the Use Frequent Users checkbox, determines the\n"
				+ "   minimum number of emails in this test. For example, if you enter a\n"
				+ "   limit of 200, then EMT only displays  email accounts with more than\n"
				+ "   200 emails saved in the database.\n" + "\n"
				+ "8. Enclave Clique and User Clique buttons use the configurations in order\n"
				+ "   to create the graphs. To calculate and display the enclave clique,\n"
				+ "   click Enclave Clique. To calculate and display the user clique, click\n" + "   User Clique.\n"
				+ "\n" + "\n" + "\n" + "==============\n" + " Clique panel\n" + "==============\n" + "\n"
				+ "This panel displays results for all of the cliques that met your \n"
				+ "specifications. Each dot on the graph represents a clique (N.B. \n"
				+ "not an email account) and has an identifying reference number. \n"
				+ "The different sizes and shapes of the dots indicates  different \n"
				+ "types of cliques. Circles indicate 2-clique, triangles indicate \n"
				+ "3-clique, squares indicate 4-clique, and so on.\n" + "\n"
				+ "The lines portray the connection (i.e. the relationship) between\n"
				+ "cliques. A line connecting two dots signifies that these two \n"
				+ "cliques share the same email account(s) (i.e. clique members). \n"
				+ "The color of the lines represents the percentage of members that\n"
				+ "the two cliques share. The table below defines which percentage is\n"
				+ "associated with which color.\n" + "\n" + "Try to click on a dot or a line here!\n" + "\n" + "\n"
				+ "============\n" + " User panel\n" + "============\n" + "\n"
				+ "This graph displays all of the email accounts. The blue dots \n"
				+ "represent cliques; they are the same as the dots in the Clique \n"
				+ "panel,  The dots use the same identifying reference number as in \n"
				+ "the Clique graph; each number indicates a particular clique.\n" + "\n"
				+ "Black dots represent all of the email accounts. The leftmost column\n"
				+ "of black dots indicates that each of these email accounts is \n"
				+ "involved with only one clique. The second left column indicates \n"
				+ "that each of these email accounts is involved with two cliques,etc.\n"
				+ "The rightmost dot(s) (i.e. email accounts) are involved with the \n"
				+ "most number of cliques. Thus, EMT displays which email account(s) \n"
				+ "is involved with the greatest number of cliques. \n" + "\n"
				+ "The colored lines indicate connections between email accounts and \n"
				+ "cliques. A line connects an account with a cliquethus showing that\n"
				+ "the account is in this particular clique. A clique may contain more\n"
				+ "than one account, and an account may be involved in more than one \n"
				+ "clique. Different columns use different colors.\n" + "\n" + "\n" + "===================\n"
				+ " Information Board\n" + "===================\n" + "\n"
				+ "The Information Board enables you to retrieve more information about\n"
				+ "the cliques. Use your mouse to click on a particular clique in the\n"
				+ "Clique panel. Details about this clique will display in the Information\n" + "panel.\n" + "\n"
				+ "When you click on a dot (clique), the Information panel displays the\n"
				+ "email accounts and \"Common Subject Words\" associated with this clique.\n"
				+ "It generates a small picture showing what a clique looks like. If there\n"
				+ "are too many email accounts or \"Common Subject Words\", it also pops up\n"
				+ "a small window to display all the information.\n" + "\n"
				+ "In the Clique panel, the selected dot turns red (the others remain blue).\n"
				+ "In the User panel, the selected clique and its email accounts also turn\n"
				+ "red. Note that if you select Without Keyword, the program does not\n"
				+ "generate common subject words.\n" + "\n"
				+ "To learn which email account(s) that two cliques share, click on the\n"
				+ "connecting line. The Information Board displays information about the\n"
				+ "shared accounts, and the selected lines becomes black.\n" + "\n");
	}

	//1
	public final String getAddressList() {
		return new String("\n" + "1. Profile\n" + "\n"
				+ "The chart in the upper left side, called the Recipient Frequency Histogram,\n"
				+ "plots a bar graph indicating the frequency of each recipient to whom the \n"
				+ "selected user has ever sent an email. The table in the upper right side, \n"
				+ "called the Recipient List, displays the email address of each recipient \n"
				+ "along with the corresponding frequency of received emails from the selected \n"
				+ "user. This table is sorted is descending order, and each of its rows \n"
				+ "corresponds to a bar on the Recipient Frequency Histogram.\n" + "\n"
				+ "The chart on the lower left side is the Total Recipient Address List Size \n"
				+ "over Time chart. By \"address list,\" we mean all recipients who have received \n"
				+ "an email from the selected user between the two selected dates. Here, the \n"
				+ "chart plots the address list size, i.e. the number of recipients in the \n"
				+ "address list, over time. It starts at zero at the beginning of the dataset \n"
				+ "and grows as the selected user sends more emails to new recipients not \n" + "already listed. \n"
				+ "\n" + "The chart in the lower right side is called the # of distinct rcpt & # of \n"
				+ "attach per email blocks chart. It displays three variability metrics and the\n"
				+ "rolling average of the first two metrics.\n" + "\n"
				+ "Three radio buttons will appear in the upper right side of the window: in-bound,\n"
				+ "out-bound with attachment, and out-bound, which is selected by default. They\n"
				+ "enable the user to choose only emails sent by the user (out-bound), only the ones\n"
				+ "received by the user (in-bound), or only the ones sent with an attachment\n"
				+ "(out-bound with attachment).\n" + "\n" + "\n" + "2. Hellinger Distance\n" + "\n"
				+ "Click the Hellinger Distance button, at the top of the screen, to replace \n"
				+ "the default Total Recipient Address List Size over Time chart with a \n"
				+ "Hellinger distance plot. The text-field Hellinger distance size defaults \n"
				+ "at 50, but it may be changed to another value.\n" + "\n" + "\n" + "3. Chi Square\n" + "\n"
				+ "The Chi Square window is accessed by clicking the Chi Square button either\n"
				+ "from the Profile window or from the CS + cliques window.\n" + "\n"
				+ "The Chi Square window compares the selected user's profile between two time\n"
				+ "frames. On the upper side, the Recipient Frequency histogram and the Recipient\n"
				+ "List table are displayed, corresponding to the so-called \"Training Range.\"\n"
				+ "This screen is similar to the top part of the Profile window, but it only shows\n"
				+ "a range of records instead of the entire history.\n" + "\n"
				+ "The bottom half of the screen shows the Recipient Frequency histogram and the\n"
				+ "Recipient List table for the \"Testing Range.\" You may change both ranges by\n"
				+ "entering values into the corresponding text fields (the ranges may overlap or be\n"
				+ "non-adjacent) and then clicking the Run button.\n" + "\n"
				+ "The Diff Threshold field is set at five (5) by default and can be changed by\n"
				+ "entering another value and then clicking Run. It displays any cell in the diff\n"
				+ "(difference) column with an absolute value greater then the threshold in red.\n"
				+ "Also, it signals the largest deviation in recipient frequency between the\n"
				+ "training range and the testing range.\n" + "\n"
				+ "At the bottom of the window, a Chi Square statistic is calculated in order to\n"
				+ "evaluate the similarities between the frequency table of the \"Training Range\"\n"
				+ "and the frequency table of the \"Testing Range.\" In addition, the degrees of\n"
				+ "freedom and the p-value of the Chi-square test, as well as the Kolmogorov-Smirnov\n"
				+ "distance, are displayed at the bottom of the window.\n" + "\n"
				+ "In summary, you may use the Chi Square window to change the user, change the\n"
				+ "ranges, change the threshold, or move to another panel.\n" + "\n" + "\n"
				+ "4. Chi Square + Cliques\n" + "\n"
				+ "The Chi Square + Cliques window is accessed via the CS + cliques button. This\n"
				+ "window is very similar to the Chi Square window, but it incorporates user cliques\n"
				+ "in the recipient list.\n" + "\n"
				+ "The Chi Square + Cliques window applies the clique functionality of EMT to the\n"
				+ "recipient frequency metrics. Cliques are defined in more detail in the Technical\n"
				+ "Manual. Basically, cliques are groupings of users with large pair-wise email\n"
				+ "flows, thus representing tightly connected small groups of email users.\n" + "\n"
				+ "The Chi Square + Cliques window displays all cliques in the \"Recipient List\"\n"
				+ "table in green (but only if the selected user belongs to an observed clique).\n"
				+ "Double-clicking on any green cell that corresponds to a given clique (each clique\n"
				+ "is named clique[i], i=1,2,,,) displays a window listing the members of that\n"
				+ "clique. Also, you may change the overall clique threshold (as defined under the\n"
				+ "main Cliques window) by modifying the Clique Threshold field and clicking Run.\n"
				+ "Doing this recalculates the entire database clique list with the new threshold\n"
				+ "and displays the corresponding metrics.\n" + "\n" + "ps: the clique is for outbound only.\n" + "\n"
				+ "5. Virus Simulation \n" + "\n"
				+ "Clicking on the Test button in the Recipient Frequency window starts the \n"
				+ "virus simulation routine. It simulates the intrusion of a virus in a user's \n"
				+ "account, runs a detection algorithm, and specifies the emails believed to \n" + "be corrupt.\n");

	}
	//2
	public static String getHellingerConf() {
		return new String("\n" + "1. Configuration\n" + "\n"
				+ "a. Number of Dummies: number of corrupted emails sent by the virus.\n" + "\n"
				+ "b. Location: location of the virus attack in the database; this is \n"
				+ "   equivalent to the time of the infection in the past (parameterized by \n"
				+ "   record number).\n" + "\n"
				+ "c. Time Interval: virus propagation rate; for example, \"Time Interval \n"
				+ "   = 60\" means one corrupted email sent every 60 minutes.\n" + "\n"
				+ "d. Testing Hellinger Distance: length used for the Hellinger distance \n" + "   calculation.\n"
				+ "\n" + "e. Size of Moving Average: length of the moving averages used as thresholds.\n" + "\n"
				+ "f. Distinct Recipients: If selected, the virus only sends itself to \n"
				+ "   distinct recipients; if not, a given recipient could receive more then \n"
				+ "   one corrupted email.\n" + "\n"
				+ "g. With Attachment: if selected, each corrupted email contains an \n" + "   attachment file. \n"
				+ "\n" + "\n" + "\n" + "2. Statistical Analysis\n" + "\n"
				+ "This window contains a total of six charts; the three plots on the left \n"
				+ "side show how some of the metrics of this section have been modified by \n"
				+ "the simulated virus infection (the original file is represented by the \n"
				+ "metrics in yellow, while the corrupted ones are in red); the three plots \n"
				+ "on the right side repeat the metrics of the corrupted file as seen on the \n"
				+ "left side (in blue this time), with the addition of the dynamic threshold \n"
				+ "curves used by the algorithm to detect suspicious emails (in red). \n"
				+ "Whenever a metric crosses its threshold, an alert is triggered; the maximum \n"
				+ "level of alert is when the three metrics provide an alert at the same time.\n" + "\n" + "\n" + "\n"
				+ "3. Intrusion Alert\n" + "\n"
				+ "This window shows the final results of the detection algorithm; each email \n"
				+ "is labeled as either normal (in blue), or suspicious (in red); the usual \n"
				+ "classification between false, true, positive, negative is performed; the \n"
				+ "specific parameters related to the simulation that was just run are also \n"
				+ "displayed (like infection propagation rate); the plot at the center of \n"
				+ "the window shows how infection and alerts are located over time.\n" + "\n"
				+ "The following classification  is used:\n"
				+ "True Positive (TP): corrupted emails correctly identified as corrupted.\n"
				+ "True Negative (TN): normal emails correctly identified as normal.\n"
				+ "False Positive (FP): normal emails incorrectly identified as corrupted.\n"
				+ "False Negative (FP): corrupted emails incorrectly identified as normal.\n");
	}
	//3
	public static String getEmailFlow() {
		return new String("(Please read EMTUserManual for more detail)\n" + "\n"
				+ "The Email Flow window displays the series of emails sent \n"
				+ "between clique member and details the relationships of \n" + "email recipients and senders.\n"
				+ "\n" + "\n" + "=====\n" + " Run\n" + "=====\n" + "\n"
				+ "1. Setup the configuration at the upper part.\n" + "\n"
				+ "1.1 Select an email account from the combo box.\n" + "\n"
				+ "1.2 Enter the range of date in two date boxes.\n" + "\n"
				+ "1.3 Select the \"Group Type\", by subject or content.\n" + "\n"
				+ "1.4 Click the \"Update Email List\" button.\n" + "\n"
				+ "1.5 Enter the threshold. \"N-Gram threshold\" must be less than 1.0.\n"
				+ "    \"Length of Content\" must be an integer.\n" + "\n"
				+ "1.6 If the group type is \"By Subject\", select a subject from the list.\n"
				+ "    If the group type is \"By content\", select a mailref from the list. \n"
				+ "    If the group type is \"By Attachment\", select an attachment\n" + "\n"
				+ "1.7 Click on \"Run\" button.\n" + "\n" + "\n"
				+ "2. For more information of a single email, click on a row\n"
				+ "   in the \"User & Email Table\" panel.\n" + "\n" + "\n"
				+ "3. For more information of the relationship of email\n" + "   accounts:\n" + "\n"
				+ "3.1 In the \"Email Flow\" panel, click on any two dots. Then\n"
				+ "    Click on the blue area \"Click here for email exchange Info\".\n" + "\n" + "\n" + "\n"
				+ "====================\n" + " User & Email Table\n" + "====================\n" + "\n"
				+ "All of the emails for the selected user are displayed in \n"
				+ "this table. The emails are sorted by time. Each email has \n"
				+ "an index number, from 0 to N-1. Each column represents a \n"
				+ "user (i.e. email account). In the cells of this table, \n"
				+ "\"sender\" means that such user (in the particular column) \n"
				+ "is the sender of the email (represented by the row), \n"
				+ "\"rcpt\" means that the user is the recipient of the email,\n"
				+ "and \"s & r\" means that the user is both the sender and \n"
				+ "recipient of the email (i.e. he copies the email to himself).\n" + "\n"
				+ "To display details about a particular email, click\n"
				+ "on the corresponding row. The Detail window displays.\n" + "\n" + "\n" + "============\n"
				+ " Email Flow\n" + "============\n" + "\n"
				+ "Each dot represents a user (i.e. email account). Each user \n"
				+ "appears once in this graph. A line connecting two users \n"
				+ "indicates that the users have corresponded via email. \n"
				+ "Yellow lines represent a unidirectional connection (i.e. \n"
				+ "User A --> User B only). Blue lines represent a \n"
				+ "bi-directional connection (User A <--> User B). \n" + "\n"
				+ "The concentric circles indicate the time scale represented on\n"
				+ "this graph. The original sender, the first user, appears in \n"
				+ "the center of the circle. The outermost circles display users\n"
				+ "who appear later in the email flow. \n" + "\n");

	}
	//5
	public final String getSimilarUsers() {
		return new String("Welcome to Similar Users  tab!\n\n"

		+ "The Similar Users window is designed to find a group of email accounts that have\n"
				+ "similar usage behaviors with the selected account. The interface has three main\n"
				+ "parts: the upper part contains all choices for the operation; the middle part \n"
				+ "is a plot that shows the histogram of the selected metric for the selected \n"
				+ "account; and the lower part shows a list of similar users for the selected \n"
				+ "account and their corresponding histograms. \n\n"

				+ "=======\n" + "Run\n" + "=======\n\n"

				+ "1. Decide the type of the email accounts to consider by choosing one of the radio \n"
				+ "buttons on the top. \"all users\" means all users that appear in database; \"sender\"\n"
				+ "are only those email accounts for which we have all of their outbound emails in the \n"
				+ "database. Two radio buttons after the label 'Similar Acct Style' allow use of a\n"
				+ "different kind of histogram when finding similar user groups. \n\n"

				+ "2. Choose an email account as the target from the \"Select Account\" drop-down list.\n"
				+ "Then select the particular statistic used to find similar accounts; also choose the\n"
				+ "comparison method and the similarity level.\n\n"

				+ "3. Press \"Find Similar Accts\" button to start the process of finding similar accounts\n"
				+ "using the above choices. Or press \"Find All User Groups\" button to start the process\n"
				+ "of finding groups for all of the users, automatically clustering users into different\n"
				+ "groups based on their histograms of behavior. \n\n"

				+ "4. The plot in the middle will show the usage histogram for the selected account.\n\n"

				+ "5. The table in the lower left part of the window will list all the groups. Clicking on\n"
				+ "a row, the corresponding usage histogram for that account will be shown in the plot on\n"
				+ "the right side.\n\n"

				+ "Results may be copied to the clipboard, or try the right mouse button.\n\n"

				+ "For detail, please refer to User Manual.");

	}
	//4
	public final String getAverageCommTime() {
		return new String("Welcome to average communication tab!\n\n"

		+ "This tab compares the average times of how quickly a user responds to different\n"
				+ "people, thus computing who is the most important correspondent. EMT considers\n"
				+ "both the response time and the amount of emails exchanged between accounts to\n"
				+ "determine the importance of different email users to the selected user.\n\n"

				+ "=======\n" + "Run\n" + "=======\n\n"

				+ "1. Radio buttons, 'all users, 'only senders', and 'frequent users', enable you to\n"
				+ "specify the type of accounts that you wish to analyze.  If you select frequent users,\n"
				+ "specify the threshold for number of emails that appear in the database. All users will\n"
				+ "take the most amount of time. (Be patient.)\n\n"

				+ "2. Select Account drop-down list displays all of the email accounts in the database\n"
				+ "in alphabetical order. Click the email account that you wish to analyze. (Typing a\n"
				+ "letter will drop down to the first account starting with the letter. You can scroll\n"
				+ "from there.)\n\n"

				+ "3. Set the start and end dates of the period you want to analyze in the \"Select\n"
				+ "Compute Period\" fields.\n\n"

				+ "4. \"Run\" button computes the relative average communication time, amount of emails\n"
				+ "exchanged, and VIP rank of different accounts, relative to the chosen target account.\n"
				+ "The detailed results are displayed in the table below and corresponding data curves\n"
				+ "are displayed in the large plot.\n\n"

				+ "5. The two text fields:\"#email threshold\" and \"VIP rank threshold\" are used to\n"
				+ "specify the threshold for the number of emails and the VIP rank. The slider is used to\n"
				+ "modify the relative weight between the metrics of the number of emails and average\n"
				+ "communication time to compute the VIP ranking.\n\n"

				+ "6. \"Refresh Table\" button refreshes the view in the table and the large plot according\n"
				+ "to the conditions specified in the threshold text fields and the position of the slider.\n\n"

				+ "7. Click on one row in the table, a black spike will be drawn in the plot to highlight\n"
				+ "the position of that account.\n\n"

				+ "You can print results or copy to the clipboard. (Try the right mouse button.)\n\n"

				+ "For detail, please refer to User Manual.");

	}
	//27
	public final String getSHDetector() {
		return new String("Welcome the Social Hierarchy Detection tab!\n\n"

				+ "This is used to detect the Social Hierarchy in an organization using " 
				+ "a combination of Average Communication and Clique analysis.  EMT considers a number " 
				+ "of statisics when computing the overall Social score.  In the formula for the Social Score " 
				+ "of any given user, EMT takes into account a weighted amount of each of the following " 
				+ "statistics: number of emails to/from the user, the average time elapsed before a response " +
				"to a sent email is recieved, the number of responses, number of Cliques the user is in, how many people are in " 
				+ "each of these Cliques and the \"importance\" of the users in each of the Cliques (using "
				+ "number of emails and average communication time as a metric for importance.  Each "
				+ "of these contributing \"scores\" can then be weighted with respect to eachother by "
				+ "the user.\n\n"

				+ "=======\n" + "Run\n" + "=======\n\n"

				+ "1. Select a start date and end date within which to perform the analysis.  The default "
				+ "values are drawn from the date range of the dataset.\n\n"

				+ "2. Select a \"Min # of Messages.\" This will effect the clique calculations.  Only "
				+ "cliques within which users exchanged this number of emails will be returned when "
				+ "the cliques of the dataset are calculated.\n\n"

				+ "3. Press \"Run\" to run the analysis.  A progress window will track the progress of "
				+ "the calculation.  The time to perform this calculation will depend on the size of "
				+ "the dataset and the selected parameters.  When the analysis is complete, the table "
				+ "below will be populated with the results (a list of accounts, ranked by SocialScore).\n\n"

				+ "4. After the analysis has been performed and the table has been populated, a user can "
				+ "specify the thresholds for the number of emails, number of cliques and the Social Score.  "
				+ "The weight values can then be selected to weight each of the noted values with respect "
				+ "to one another when computing the TimeScore and SocialScore values. Press \"Refresh\" after " 
				+ "inputting the desired parameters to quickly recompute and refresh.\n\n"

				+ "5. After the appropriate weights have been determined,  the \"View Networks\" Button " +
				"will open a new window with a visualization of the hierarchy pertaining to the current " +
				"ranked list.  The Controls in the View Networks Window are as follows:\n\n" +
				
				"   i)   Hover over a node and the account name will pop up.\n\n" +
				"   ii)  Hover over an edge and the edge description (from - to)\n" +
				"        will come up.\n\n" +
				"   iii) Click on a node (or select a group of nodes) and they will\n" +
				"        populate the list at the bottom.\n\n" +
				"   iv)  In \"Picking\" Mode, move nodes around by clicking and\n" +
				"        dragging any node or group of nodes.\n\n" +
				"   v)   In transforming mode, move the entire graph within the\n" +
				"        window.\n\n" +
				"   vi)  Select the OrgChart Layout display mode for a hierarchical\n" +
				"        view, and the FR Layout for a clustered network graph\n" +
				"        view.\n\n\n" 
				
				+"6. There are two different ways one might save the information computed here for " +
				"further use. 1) Press the copy to Clipboard button and paste the contents " +
				"into a text file and save as a *.csv file. 2) Press the \"Save Talbe Data\" button " +
				"to save the table data to disk so that during your next EMT session, the \"Load Table " +
				"Data\" Button will reload this specific computed ranked list and all of its values.\n\n"
				
				+ "For help in more detail, please refer to User Manual.\n\n");
	}
	
	//6
	public final String getUsageHistogram() {
		return new String(
				"Welcome to Usage Histogram  tab!\n\n"

				+ "This window allows you to study the histogram usage of the user. A specific user can be chosen from the drop down list\n"
				+ "on top of the screen. To shorten the list, click on the 'email' button on top to constrain it in some way.\n"
				
				
				
						

						+ "2. From Account drop-down list displays all of the email accounts in the database in\n"
						+ "alphabetical order. Click the email account that you wish to analyze. (Typing a letter will\n"
						+ "drop down to the first account starting with the letter. You can scroll from there.)\n\n"

						+ "3. Set the respective start and end dates for the 'recent period' and the 'profile\n"
						+ "period' in the input fields labeled Select Recent Period and Select Profile Histogram Period.\n"
						+ "there are quick choices to help you choose uniform weeks/months\n"
						+ "Also set the alert level at one of the 11 levels using the slider labeled \"Alert Level\".\n\n"

						+ "4. Press \"Show Histograms\" button to display the histograms for recent and profile periods\n"
						+ "and begin the comparison once the account, the periods, and the alert level are chosen. Two\n"
						+ "plots show the usage histograms. The left plot is for the recent period, and the right plot\n"
						+ "is for the profile period.\n\n"

						+ "5. Or press \"Run for all users\" button to start the process of comparing all email accounts\n"
						+ "with the selected periods above. 	A table will display the result of the comparison. It\n"
						+ "has two columns, one for the email account and one for its corresponding \"difference\". You\n"
						+ "may click on an individual row in the database to see the recent and profile histograms for that user.\n\n"

						+ "6. Finally, the results of the comparison are displayed with the proper alert level.\n\n"
						+"You can also zoom into a user's behavior by clicking on 'study user profile' button on the right\n"
						+ "You can print results or copy to the clipboard. (Try the right mouse button.)\n\n"
						+ "\n\n"
						+ "Report Generation: This feature runs through each user and calculates the histrograms differences\n"
						+ "when appropriate, alerts are generated to the alert view window\n"
						+ "For detail, please refer to User Manual.");

	}
	//7
	public final String getMessage() {
		return new String(
				"Welcome to the help section. \nFull details in User Manual\n\n"
						+ "****************************\n"
						+ " This is the main window to view data.\n"
						+ " In order not to overload the user, data is viewed based on some constraints specified by \n"
						+ "         - Folder (source) name\n"
						+ "         - Time Based (from and to) or unbounded \n"
						+ "         - By User Name, and Then in bound, outbound, or both (default)\n"
						+ "         - by Label or none (mailref)\n"
						+ "         - by groups of similiar users calculated in the 'similiar user' window\n"
						+ "         - combination of above \n"
						+ " PLEASE NOTE: When message bodies are displayed, they are affected by 2 things. Force lowed case and stop listing (removing some words specified) these are both configurable in the Email Features window\n\n"
						+ " Additional Tasks can also be done here such as:\n"
						+ " 1) Label the data\na) manually for training\n"
						+ " If you feed the learner 2 labels, it will be a mutli model learner, one label for single class (experimental) threshold modeling\n"
						+ " - Please note that the ROC curves displayed on training completion reflect the following convention:\n"
						+ " Accuracy = total correct for the training, ie those which are correctly matched to the user specified labels.\n"
						+ " Detection = total we said is target class (specified in email feature window)/ total target\n"
						+ "  Example: total we said are spam/ total spam\n"
						+ "  False positive = total we said are not target / total non target\n"
						+ " b) Label the data using a model\n"
						+ " 2) Group/cluster the data either by:\n"
						+ " -subject line only (ngram length specified in the email features window)\n"
						+ " -content of email (subject + body using ngrams) \n"
						+ " -Keywords in the messages (see email features) \n"
						+ " -Character Frequency distributions (using edit distances over freq/hsitrogram strings)\n"
						+ " 3) Graphically explore the data (Experimental)\n"
						+ " 4) Get a count of the data before actually fetching it (the General info button)\n"
						+ " 5) Print out messages (single or group) (right click print) \n"
						+ " 6) Move messages between folder or into new ones. (right click move)\n"
						+ "***************************\n\n"
						+ "Also:\n"
						+ "Selecting by mouse dragging or single click. right clicking over selected rows, will pop up a menu\n"
						+ " 1) Can label the records.\n"
						+ " 2) Can print records\n"
						+ " 3) Can copy to memory\n"
						+ " 4) Can move to other folder (new or current)\n"
						+ "\n\nNotes: Model details in help in the create model box\n"
						+ "clicking top of the table column will sort each column.. hold SHIFT + left mouse click will reverse sort\n"

						+ "\n\nNo animals or non-computer science students were hurt in the production of EMT.");
	}
	//8
	public final String getLoadDB() {
		return new String(
				"Welcome to the help section. \nFull details in User Manual\n\n"
						+ "****************************\n"
						+ " This window allows email data to be imported into the database.\n\n"
						+ " There is a built in Parser written in Java, which can import both mbox style\n"
						+ " and outlook express files (converts into mbox using readdbx.exe then imports) into the database\n"
						
						+ "\n1) The target email file or directory of emails can be specified, either manually or by browsing in the browse button\n"
						+ " 2) To empty the database of all records, check off Empty db\n"
						+ " 3) To create a log (for problem solving) check off the log checkbox\n"
						+ " 4) click on 'import' to start loading in email files, a little popup will show the current file being processed\n"
						+ " 5) When it is done, you can speed things up greatly i.e. optimize the email data by going to the sqlview window and clicking on 'optimize data'\n"
						+ " \n\n\nTools are provided to clean up the domain names from the raw email files\n" +
						"For example: to convert YYY.columbia.edu and ZZZ.columbia.edu to only columbia.edu in the entire database, to make it easier to match users\n"
						+ " \nNote: The old perl parser has been left in (not used by default) for backwards compatbility\n"
						+ " for the perl parser, there is a location box which can be used to point to the parser\n"
						+ "Last but not least the report on the bottom of the screen can be copied to clipboard, and pasted anywhere else in windows\n");
	}

	//9
	public final String getAttachStats() {

		return new String("Attachment Statistical window\n"
				+ "\n\nThis window allows the user to calculate relevant statistics over email attachments\n"
				+ "Each statistic represent part of the greater picture on the movement of a particular attachment\n"
				+ "Attachments are represented by both a hash and filename\n"
				+ "\nIn addition the load and save button allow the computation to be saved between sessions\n");

	}

	//10
	public final String getSQLView() {
		return new String(
				"Welcome to the help section. \nFull details in User Manual\n\n"
						+ "****************************\n"
						+ " This window allows a few things\n"
						+ " 1) View the data in the database based on some constraints specified by \n"
						+ "    the sql statement in the box below.\n"
						+ " 2) Clean up time problems. what this is , certain spam email misconfigure the date part to get higher sort preferences. \nWe can run through the database and when a invalid date is seen (<1990 and >2004) replace with last valid date.\n"
						+ " 3) Speed things up by calling the sql 'optimize' command on the email and message tables.\n\n");
	}

	//11
	public final String getModelInfo() {
		return new String(
				"Welcome To help for the model types\n"
						+ " \n\n Ngram , TextClassifier, both run on the contents of the email. \n"
						+ " NGram uses N size window to form tokens to calculate a centriod used in trying to cluster new examples\n"
						+ " TextClassifier uses whole words as tokens ie it is a bag of words model\n"
						+ " Naive Bayes uses non body features, specified in the feature window, if body is used, then either ngram/text is used\n"
						+ " PGRam is an implimentation of paul graham's naive bayes, its only on the body part\n"
						+ " TF-IDF is another body only method used term freq over inverse doc, so the more often a word occurs in\n"
						+ " a set, the more important it is.\n"
						+ " outlook, uses 50 rules bundled with outlook - SPAM detection\n"
						+ " 1-coded is a random set of hard coded rules - SPAM detection\n");
	}

	//12
	public final String getUserCliques() {
		return new String(
				"User Clique Help:\n"
						+ "Here one can build a model and see the cliques of an individual account (user email)\n"
						+ "Threshold = min amount times seen, ie a group has been seen x times\n"
						+ "Clique violation button will display a list of groups not seen in training\n"
						+ "Clique sets are groups seen in training\n"
						+ "The display is shown by groups of 12 months. each group will have a solid black bar indicating years end\n"

		);

	}
	//13
	public final String getEmailFeatures() {

		return new String(
				"This window is to specify different features for other parts of the system\n\n"
						+ "The following features can be configured:\n\n"
						+ "1) N-GRAM length - this is the size of the consecutive grams used during different ngram analysis\n"
						+ "2) Stop list = if to use and what words are included on the stop list. A stop list are words to ignore when looking at the email body\n"
						+ "3) Lower Case use - when analyzing the email body, if to convert everything to lowercase\n"
						+ "4) Threshold - this threshold is used when grouping the emails.\n"
						+ "5) Target Analysis - choosing a target classification when doing machine learning in the Messages window\n"
						+ "6) Email body features - most of the window shows differnet feature of an email which can be used when doing the Naive Bayes analysis. The sample data drop list, shows the first few emails from the database which can be used for display purposes to see sample data\n");

	}
	//14
	public final String getAbout() {
		return new String("EMT The Email Mining Toolkit was developed at Columbia University\n"
				+ "At the computer science Data Mining Lab under Prof Salvatore J Stolfo\n"
				+ "The PhD studnets working on the project are\n" + "Shlomo Hershkop\n" + "Ke Wang\n" + "Weijen Lee");

	}

	//15
	public final String getGeneral() {
		return new String(
				"General Help:\n"
						+ "In each window you will find specific help. In addition read the User and Tech manual\n"
						+ "Here are some global hints:\n"
						+ "On the pretty graphs, you can right mouse click and send to printer or annotate and send to printer\n"
						+ "To capture a window to clipboard and paste into your favorite typing application:\n"
						+ "ALT=\"Print Screen\" buttons will capture the individual window. ctrl-v will paste it into a program\n"
						+ "In the message table, you can clikc and drag the mouse to highlight many rows. then right click anywhere on the\n"
						+ "shaded area to label\n");
	}

	public final String getHellingerClique() {
		return new String("Hellinger distance is the result of inspecting the aggregate behavior of a\n"
				+ "sequence of emails.  As such, it would not react immediately when a viral email\n"
				+ "appears. Similarly, it would keep setting alarms for a short while after a\n"
				+ "batch of viral emails has already been sent out.  On the other hand, user\n"
				+ "cliques could detect a suspicious viral email upon its first appearance.  It is\n"
				+ "worth mentioning that every time an email with a new address appears, user\n"
				+ "clique will treat it as a violation. Ideally, we want to take only the best\n"
				+ "features from each method and combine them to achieve better overall\n" + "performance.\n\n"
				+ "We propose a strategy we call the buffer-crawling method.  Emails are assumed\n"
				+ "to be buffered before they are actually sent out.  Such buffering could be\n"
				+ "hidden and unbeknownst to the user.  Actually, email is a store and forward\n"
				+ "technology.  There is no reason why it cannot be a \"store for a while, then\n"
				+ "forward\" strategy.  As far as the user is concerned, the email is sent from\n"
				+ "client to server and is delivered by the underlying communication system at\n"
				+ "some arbitrary future time.  Thus, the strategy of buffering and holding emails\n"
				+ "for some period of time allows sufficient statistics to be computed by the\n"
				+ "models and also benefits mitigation strategies to quarantine viral emails,\n"
				+ "limiting exposure of the enclave.\n\n"
				+ "This tab is a training function to test the Clique and Hellinger detection.\n"
				+ "Give the training data, which are normal emails. Then, give some testing data.\n"
				+ "This function returns labels that indicate whether the data are \"normal\" or\n"
				+ "\"suspect\".\n\n" + "The period of training data must be earlier than the period of testing data.\n"
				+ "Because the training data represent the user's previous normal behavior, and\n"
				+ "the testing data represent his future, incoming emails (the buffered data).\n"
				+ "After the test, each of the testing data will be labeled by \"normal\" or\n"
				+ "\"suspect\". Then, we can check manually whether they are correct or not.\n\n" + "\n\n\n"
				+ "=====\n" + " Run\n" + "=====\n\n" + "1. Select a user.\n\n"
				+ "2. Select the training type: inbound email or outbound email.\n\n"
				+ "3. Select \"Attachment\" or not. It's more accurate for Hellinger function, if it also\n"
				+ "   analyzes the attachment.\n\n"
				+ "4. Select the training data by entering the start and end date. Training data are normal\n"
				+ "   emails that could be the user's normal behavior.\n\n"
				+ "5. Select the testing data by entering the start and end date. Testing data are emails that\n"
				+ "   you don't know whether they are normal or suspect and you want to test.\n\n"
				+ "6. Use two \"Count\" button to check the number of emails in that area (time).\n\n"
				+ "7. Click \"Train\" to start training.\n\n" + "" + "" + "");
	}

	public final String getDatabaseSetup() {
		return new String(
				"This is the help file for setting up an initial database. \n\n"
						+ "The \"Setup Database\" window allows the user to easily setup a compatible database, without using any sql.\nIt requires knowing a root type of account which can create databases.\n"
						+ "i.e. The user and password provided need to be such that allows the creation of databases\n"
						+ "This doesnt mean only the root account, if a non root account has been given privileges\n"
						+ "The \"Step2-Database\" window allows a new databse to be create from some schema file and also to view current databases.\n"
						+ "either choose an existing database to update it's schema (and it will be erased in the meantime or choose \"Create new database\" which will later prompt for new name\n"
						+ "click on \"final setup\" for the database name and location of the schema file\n....Exceute DB will create it for you\n\n\n"
						+ "PLEASE NOTE: For General Preferences: Save settings is the only one which saves things...continue in to EMT doesnt save anything\n"

		);
	}

	public final String getVirusScan() {
		return new String(
				"This tab allows the user to run several virus scanning methods.\n"
						+ "Each of the method may generate inaccurate alarms. However, to combine all of the methods\n"
						+ "could give the user a good advice about whether emails are normal of malicious.\n\n"
						+ "Run the detection:\n"
						+ "1. Select to test inbound or outbound emails (or both in and outbound).\n"
						+ "2. Check \"Attachment Only\" to get emails with attachments only.\n"
						+ "3. Select an email account from the drop-down list.\n"
						+ "4. Select the proper starting and ending date.\n"
						+ "5. Click on \"Get Email\" to display the emails of this email account between these dates.\n"
						+ "6. Check the methods boxes to select the method(s) to be used.\n"
						+ "7. Each row represents a data record. An email may have multiple records (rows) because there"
						+ "   are more than one recipients or attachments.\n"
						+ "8. Click on a specific row or several rows at the \"Label\" column in the email table, and\n"
						+ "   click right button of your mouse to set it to be Unknown, Normal of Malicious.\n"
						+ "   \"Normal\" means this email is a normal email, \"Malicious\" means this email is a virus,\n"
						+ "   and \"Unknown\" means that we are going to use the methods to detect whether it's a virus\n"
						+ "   or not.\n"
						+ "9. Click on \"Detect\" to begin the test.\n"
						+ "10. The columns \"Hellinger\", \"Clique\" and \"MEF\" will display the result status of each email.\n"
						+ "11. Click \"Generate Reoprts\" to send a report to the \"Report\" tab.\n" + "\n\n"
						+ "Update the MEF model:\n" + "1. Select the emails that will be updated from the table.\n"
						+ "2. Set the label of these emails to be \"malicious\".\n"
						+ "3. Click on \"Update MEF Model\" button.\n" + "\n");
	}

	/*
	 * public final String getVirusSimulation(){ return new String("This tab
	 * displays the virus simulation. Users can setup the parameters and test a
	 * specific\n" +"email account with simulated viruses. The viral emails are
	 * generated at a random location\n" +"(i.e. a random time), and they look
	 * for victims from the account's previous emails. Then,\n" +"we use the
	 * combination of Hellinger, Clique, and Cumulative Attachments Analysis
	 * models\n" +"to detect the viruses. Moreover, this tab can also do real
	 * viral email detection using\n" +"the combination of models.\n\n\n"
	 * +"Testing of virus simulation:\n\n" +"Select the Simulation button.
	 * Select an email account, select inbound or outbound, enter\n" +"the
	 * proper parameters, and then click on the \"Run\" buttonTest. The results
	 * will be\n" +"shown in the Result window.\n\n" +"1. Detection Rate
	 * describes the ratio of number of viral emails be caught to total
	 * number\n" +" of viral emails.\n" +"2. False Positive describes the ratio
	 * of number of false alarm to the total number of\n" +" emails with
	 * attachment.\n" +"3. First Virus Missed describes the missed viral emails
	 * before we catch.\n" +"4. In the big table, each row is an email
	 * record.\n" +"5. numattach means the number of attachments in this
	 * email.\n" +"6. vlabel describes whether this email is normal or fake
	 * viral email, while T means\n" +" normal and F means fake.\n" +"7.
	 * hellinger column displays the alarm generated by Hellinger model.\n" +"8.
	 * clique column displays the alarm generated by Clique model.\n" +"9.
	 * combine column displays the alarm generate by all the combination of all
	 * the three\n" +" models.\n\n"
	 * 
	 * +"Setup the parameters of simulation:\n" +"1. \"propogation rate\"
	 * decides how fast a virus sends emails. It is counted in minutes.\n" +"
	 * For example, 10 means the virus picks up a random time between 0 and 10
	 * minutes and\n" +" sends each email.\n" +"2. \"# of recipient in a viral
	 * email\" decides the number of recipients in a single viral\n" +" email.
	 * For example, 3 means that there are 3 recipients (victims) in a viral
	 * email.\n" +"3. \"# of virus\" decides the total number of testing viral
	 * emails in this test.\n\n\n"
	 * 
	 * 
	 * +"Real virus detection:\n" +"Select the Real Emails button. Select an
	 * email account, select inbound or outbound,\n" +"select proper training
	 * and testing date, enter the proper threshold, and then click on\n" +"the
	 * \"Run\" buttonTest. The results will be shown in the Result window.\n"
	 * +"The result table is the same to virus simulation. Since the detected
	 * viral emails need\n" +"to be confirmed manually, the viral label is
	 * \"NA\".\n\n\n"); }
	 *  
	 */
	public final String getEnclave() {
		return new String(
				"This tab allows the user to run clique analysis and display cliques with common subject words shared among the groups\n"
						+ "We impliment the clique finding algorithm using the branch and bound method \n"
						+ " described in \"Finding All Cliques of an Undirected Graph\" by Bron & Kerbosch (1971)\n");
	}

	/** Report window help */
	public final String getReports() {
		return new String("This window has two main uses \n1) Allows the user to organize report alerts generated from the other windows\n2) Investigate email archives in a specific way\n\n\n" +
				"\n\n\nTo get started on forensics: click on 'Refresh Email info' which will allow to choose a specific set of emails\n" +
				"select a specfic user, and their overall usage history will be shown on the bottom left. if the 'get vip for user' is selected, it will fill the vip table on the bottom right\n" +
				"if the 'calculate' choice is selected, choosing a specific VIP user, will show the histogram of communication between the selected user and VIP user\n" +
				"and a distance metric will appear above the vip list\n" +
				"Clicking on the distance will create an alert");
	}

	public final String getAboutEMT() {
		return new String(
				"Email Mining Toolkit (EMT Version 3.6)\n"
						+ "Feb 10, 2005\n"
						+ "Columbia University, Computer Science Department\n"
						+ "The IDS Laboratory\n\n"
						+ "Welcome to the EMT walkthrough document\n"
						+ "This document is an overview to introduce the functions and features of EMT. This walkthrough will guide the user to the wide range of tools available in the EMT system. Further information is available in the User's manual and Technical manual.\n"
						+ "1.	Email Data\nThe first step to using EMT is to acquire email data to study. Using the \"Load Data\" window, email data is inserted into the database.\n"
						+ "The text box at the top of the screen labeled \"file to parse/process\" allows the user to indicate either an email file, or email directory. In case of directory, all files will be processed with the exception of zip files, files beginning with a period, and files ending with ~ (temporary emacs files). \n"
						+ "Click on \"browse\" and point to either an MBOX style email file, or .dbx (outlook express) file. Click on  \"OK\" to return to the EMT window. The text box at the top should now indicate the path and email file to parse (or directory). Please note that there is an option to erase the database before inserting new data by checking the \"Empty DB first\" box.\n"
						+ "By default the built-in Java-implemented parser is invoked to read and insert email data directly into the database.\n"
						+ "Now click on \"Import\" to start the process of loading email files into the database. To follow the progress, one can bring up the dos style command window to see the output of the java parser as it processes the input files. \n"
						+ "Once data is in the database, the following steps can be followed to prepare the data\n"
						+ "a) If the contents of a single file (folder) can be classified by a single class (example all spam) the user can click on \"Label Folder\" button and choose a label. Built-in class labels such as spam/not/interesting/virus/unknown are available. \n"
						+ "b) Next the data can be cleaned up by combining domain names which are the same but processed by multiple mail servers. For example some emails are labeled with userX@one.columbia.edu and userX@two.columbia.edu . If both emails belong to the same user one can replace all references to either domain with a single domain.\n"
						+ "First click on \"refresh domain list\" button. Choose relevant names and click on the \"copy to list\" button. This can also be done manually using the percent (%) as a wildcard. For example %.columbia.edu will replace \"*anything*.Columbia.edu\". Be sure to fill in the target domain box. For example, if you choose the target: single.columbia.edu  and click on \"Click to convert to\" button to enter your choice and this action will clean up the data. \n"
						+ "c) The user can switch to the SQL view window to run the \"clean data\" button. This will remove any invalid data (in case the parser was interrupted by unexpected characters or email formats. \n"
						+ "\nOn the bottom of the LoadDB window, a report is generated with an overview of the current database. The report is refreshed at every new parsing session, or can be done manually. The \"Report to clipboard\" moves the information to the system clipboard.\n"
						+ "2.	Message Window\n"
						+ "Click the Message window to reveal a view of the data. To show new data, choose a pattern of data and click Refresh. Patterns are used to load data into the Java window. By default the database will load up with a date range which includes around 400 messages.\n"
						+ "As an example pattern, specify a specific folder name (file parsed), either a date period or all dates, and click on \"refresh\".\n"
						+ "One can test a data view before actually fetching it by clicking on \"general info\" button. A window will appear showing how many records will be fetched by the refresh and some information on the current table size.\n"
						+ "In the case where the user clicks on \"refresh\" and no data appears, check that the period between the dates at the top of the window reflects the data in the database. If the data view is selected as \"date bounded\", and no data falls between the selected dates, no data will appear.\nOnce data appears a single left mouse click will refresh the message window on the bottom and double-clicking will bring up more information. \nMultiple records can be chosen either through shift click, mouse drag or ctrl+click. \nTo sort a specific column in ascending order, click on the column name. For example click on the subject column to sort all emails by subject line. To reverse sort, click on the column while holding down the \"shift\" key (any shift key).\nMore options are available when right mouse clicking on a selected record. For example a message or range of messages can be moved between folders, printed, copied to clipboard, or labeled.\n"
						+ "3.	Viewing Email Attachment Information \nAttachments can be studied in the \"Attachment statistics\" window. Click on \"attachment list\" button to refresh the attachment list, either at startup or when loading in new email data. The list is sorted in alphabetical order. \nChoosing an attachment, will update the metrics table, and \"seen in\" table. The metrics can be recalculated by click on Compute Statistics and Alerts to update the statistical information. This process may take a few minutes, during which the buttons becomes inactive and a popup progress bar will note progress.\nWhen the buttons becomes active again, choose an attachment from the list in order to see information about it stored in the database. The \"Null\" symbol represents emails with no attachments. You must update the alert function thresholds in order to generate the appropriate automatic alerts to the Alert Table window. This may be done through the top menu by clicking Tools Alert Options.  \n"
						+ "4.	Email Features\n"
						+ "The Email Feature window allows the user to specify system wide settings to affect modeling and other tasks in EMT. Among the options are:\nTarget class: this is the class label associated with a set of selected emails considered training data to learn a classifier. Although the classifier will learn (usually) two target classes, it is helpful to calculate how well it does vis a vis the target class label.\n"
						+ "\"Stop list\" is the list of words EMT will ignore when computing content based features. \"Keyword list\" is the list used to group the contents of the messages in the message grouping function. \"Type of attachment\" indicates the type of attachment to be analyzed in the learning models. \"Force lower case\" will force the email body content to be analyzed in lower case only. \nThere is a drop down list labeled \"select sample data\" which will affect the data displayed in the bottom panel. Here we are choosing the feature set to be used in the Naive Bayes modeling in the message window.\n"
						+ "5.	Learning in the Message Window.\nBy default, all new data are classified as unknown. This means that the class column in the database is set at \"?\" or \"u\". In this example, in order for EMT to perform machine learning, some of the data has to be labeled as a target class (example) \"spam\" (\"s\") while other data are labeled \"not interesting\" (\"n\").  The labeling process can be accomplished in one of three ways: via a manual setting, via the Message window, and via SQL changes to the database. \nBring up a folder in the window. Label all the examples, either through choosing a group of messages with the mouse and \"right click\" and choose \"label\". Or by manually labeling each one with the class column drop down list. That is by clicking on the current label (\"unknown\") and choosing an appropriate label.\n"
						+ "Before running a learning algorithm, switch to the \"email feature\" window, and make sure the current features are the ones you want. See the \"email feature\" section for more information.\nSide note: Certain features are inappropriate for Naive Bayes learning. For example, a feature such as mailref is not conducive to learning which email is \"interesting,\" since the mailref has an arbitrarily assigned code without meaning. The mailref is a unquie identifier used to differentiate distinct emails.\nClick on build model, specify a model name, choose learning type (see help button). Then click on train. A ROC curve showing how the model evaluated over the training data will appear at the end of the training process.\nTo evaluate a classifier or model, load up another view of unlabelled email data, choose \"eval mode\", specify the model name, and see how the chosen classifier labels the examples automatically.\n"
						+ "a.	Common Learning Errors\nSome of the learning algorithms only accept numeric features, while some only accept string features, and some accept both.  A pop-up window will appear with an error message if you try to run an algorithm that has incompatible features.  \n"
						+ "Each algorithm requires a minimal number of labeled messages as examples. If you have labeled too few messages, then you will see a pop-up window indicating that there is an insufficient amount of labeled data. \n"
						+ "6.	Interesting Users\n"
						+ "To locate \"Interesting\" users, open the Similar Users window. Select either senders or all users. Select a user from the drop-down list and click Find Similar Accts. You may be able to locate more users by adjusting the slider to a lower level. Click on the user account name to bring up a usage graph for each of the users determined to be similar to the target user's histogram. In addition, you can click Find All User Groups to find all groups of similar users. These options enable you to get a general idea about the users' email usage behavior. \n"
						+ "7.	Screen Capture / Printing\nThis version of EMT does not have screen capture capabilities.  To create a snapshot of what is seen on the screen, use the screen capture functionality available in Windows. Click the Alt-Print Screen buttons, and Windows will capture the current active window. In addition, you may click Print Screen to capture the entire screen.\nThere is also cut-and-paste functionality available in some of the table displays. Use the Windows keyboard shortcuts to copy (Ctrl-c) and paste (Ctrl-v) to copy selected data from a table to another application. You can also copy the entire test table to the clipboard as text by clicking the Copy to Clipboard button in the Testing Area window.\n"
						+ "8.	Usage histograms\nIn this window, one can track anomalous behavior over time. First choose the sender email. Note that by default the top \"only senders\" is checked off. We define sender here to be those email accounts which have a min of 200 outgoing emails. To see all email users click on \"all users\". \nSelect  a user's email, notice that the text label on the right of the email will now show how many emails and the range of available dates. Now click on show histogram. If there is an anomalous behavior, an alert will be shown at the bottom of the window, and alert generated in the alert table. \nThere is a slider labeled \"alert level\" which reflects a threshold of how different the recent actions can be in order to generate an alert. \nNow either choose the same user or another, click on \"Run against all users\". This will compare the current user to all others.\n\"Generate report\" will run a report using the training/testing range for each of the users.\nIf any user's recent email behavior is deemed anomalous, an alert will be reported to the report window.\n"
						+ "9.	Cliques of users\nEMT allows the exploration of cliques generated by everyday usage of email systems. The basic concept encompasses the idea that email systems reflect an underlying social structure between users. Three windows, allow the user to view email cliques in somewhat different views. \n"
						+ "a.	Enclave cliques\nThis window, implements \"Finding All Cliques of an Undirected Graph\" by Bron & Kerbosch (1971). The basic idea is to find all groups of user exchanging at least \"X\" emails pairwise. The parameter X can be set in the top of the window.  \nTo see the cliques, hit the \"Refresh\" button. This can take a few minutes on a very large data set. \nWhat is displayed is a list of users who appear in each clique, along with common subject words which appear on all the emails.\n"
						+ "b.	User Cliques\nThese cliques are from a user's point of view. By choosing an individual user, one can see how their email is spread over time. Each black bar is a year division in the graph. \nIf the user list is empty, click on \"refresh users\". Now choose a user. The email history will appear to show the distributions of emails from a particular email user.\n\"clique violations\" shows all violated cliques in the testing period compared to what is seen in the training period. \"clique sets\" show all cliques seen during the training session. In addition a weight is shown which calculates how often a single user has been seen in all email correspondence. A clique composed of frequent users will have greater weight than a clique with infrequent users.\n"
						+ "c.	Graphic Cliques\nThis is a visual representation of the enclave cliques. In addition a second somewhat faster but simpler clique algorithm is implemented instead of what is used in the enclave clique window.\n"
						+ "10.	Email Flow\nThis section allows one to follow the path an email and the subsequent replies it generates over time. The user selects a particular user account, and a particular email sent from that account. EMT will automatically find all \"similar\" emails exchanged between any email accounts in the future. The display shows the original seed email as the center dot. Each concentric ring corresponds to the time of the next email appearing in the flow. Each subsequent dot and arc displays the entire flow. The user can navigate thru this flow simply by clicking on nodes and arcs and reading the emails in the pane at the bottom of the window. \n"
						+ "11.	Recipient frequency \nThis window allows one to profile an email account to look for interaction of a single user to all recipients of that user.\nChoose an email address to study. Click on profile to see the profile of the user. \nOn the top left you see a histogram of the frequency of communication for each recipient. The table on the top right displays the statistics that are plotted on the left. \nOn the bottom left, is a plot of how new addresses are seen over time of the email history. On the bottom right is a plot of attachment behavior over time. \nNow click on \"chi square\" to compare a training period against a test period for the user. This functions depends on the amount of data available for the user. The basic idea is to choose a period of time as the \"normal time\" of the user that will be tested against a user chosen \"test period\" to determine if the user's email behavior has changed unexpectedly.\nNow click on the \"cs+cliques\" button. On first use, it will compute the cliques, and then display the user data based on cliques. (see clique section). \nFinally the \"hellinger\" button creates a hellinger distance measurement to spot unexpected email behavior (rather than using the Chi Square test). \nThe \"test\" button on the top of the window, allows one to simulate a virus propagation in the user's email. Different metrics are used and displayed showing how to detect the simulated viral emails interspersed in the user's email data.\nTo get an interesting users, click on \"sort users desc\" button to sort the most freq user first. This will reorder the list in order of number of records. The more records, the more accurate picture can be built to profile a user.\n"
						+ "12.	Average communication time\nThis section allows the user to examine which accounts are more important to the email user based on response behaviors. The theory is the quicker one responds to a person's email, the more important that person may be. \nChoose a user name, choose a date interval, click on run.\nThe plot is an ordered set of recipients, which the sender responds to an average email. You may expect to see at the top of the list the most important people of the selected user. \n"
						+ "13.  Report and forensic\n This section allows to review reports from other windows, and to explore all accounts in a way which ties them to each other through usage\n"
						+ "14.	SQL View\nThis window allows SQL queries. You can leave the default SQL that will fetch the first 50 records. Click on \"fetch\" to execute the current SQL command.\nFirst see if your data has any date issues, click on \"clean invalid dates\" and check the output.\nNow click on \"clean invalid data\" to clean out inconsistent data from the database. This action can sometimes take a while so please be patient.\nNow click on \"Optimize tables\" this will issue an SQL statement to recomputed the keys in the database. This will speed up some of the operations of EMT.\n\n");

	}

	public final String topEMThelp() {
		return new String("Welcome to EMT\nThe top toolbar allows you to do the following:\n"
				+ "-Switch between windows\n" + "-Choose specific users in certain windows\n"
				+ "-View Specific User period stats\n" + "-Choose User periods to try out window actions on\n"
				+ "Window overview:\n" + "\n\n" + "Load Data - Loads up data\n"
				+ "Message   - View data, and train/test machine learning models\n"
				+ "Email Features- set gloabl features\n" + "Usage Histogram- user usages\n"
				+ "Enclave Cliques- show group communications\n" + "User Cliques- show groups over specific periods\n"
				+ "Similar Users - show similiar users\n" + "Average Comm Time - study response times\n"
				+ "Recipient Frequency - in and outbound models\n" + "Attachment Statistics - study the attachments\n"
				+ "Email Flow - show flow of messages\n" + "Graphic Cliques - graphically show groups\n"
				+ "Virus Scanning - find new viri\n" + "Reports - prganize the reports\n"
				+ "SQL view - use sql to show stats/data\n"

		);
	}
	public static String getFAnalyzer() {
		return new String("This tab displays the Attachment Analyzer. Users can select emails\n"
				+ "and build models from the attachments. The attachment files are classified\n"
				+ "by the file extensions. Users can also test new attachments by using\n" + "pre-built models.\n\n"
				+ "Attachment Analyzer tab includes the following components:\n"
				+ "1. Email Type allows the user to select the type of emails to be analyzed.\n"
				+ "2. Users can enter the Truncate Ratio that EMT will only read the first x%\n"
				+ "   bytes of an attachment, where x is the value in the box.\n"
				+ "3. Users can select 1-gram or 2-gram to analyze. To test new email attachments,\n"
				+ "   the testing gram size must be the same to the training (pre-built model)\n" + "   gram size.\n"
				+ "4. Select a proper user and the date, and click on Refresh Email button to\n"
				+ "   display the emails in the Email Table.\n"
				+ "5. Click on Load Model to load a model to the memory.\n"
				+ "6. Select some emails from the Email Table and right click the mouse button to\n"
				+ "   set them to \"Mark\".\n"
				+ "   Click on Save Model. These selected emails will be built to a model and saved.\n"
				+ "   Users can also select to save a new model or append these emails to an exits\n" + "   model.\n"
				+ "7. Click on Merge Model to merge multiple models. Hold the Ctrl or Shift key\n"
				+ "   to select more than one files in the File Chooser.\n"
				+ "8. Click on Display Model to display the byte distribution of current (loaded)\n" + "   model.\n"
				+ "9. Click on Test Unknown to test marked email attachments.\n\n\n\n"
				+ "How to test email attachment:\n"
				+ "Select an email account, proper date range and the type of emails (in/outbound)\n"
				+ "and click on Refresh Table. The email records will be displayed in the Email Table.\n"
				+ "Select a record or multiple records and click on the right button of the mouse\n"
				+ "to set the proper labels (i.e. NA or Mark) of the emails.\n\n"
				+ "Set the emails that you want to test to \"Mark\" in Email Table.\n" + "Load a model.\n"
				+ "Click on Test Unknown.\n" + "The result will be shown in a popup window.\n");

	}

	public static String getEMTPARSER() {
		return new String(
				"Welcome to the help file for the Parser Window\n\n\n" +
				"This file is meant to help you import data into EMT\n" +
				"The parser assumes two important things\n" +
				"1) The Email is either in MBOX or Outlook .dbx format\n" +
				"2) A valid database already setup with the correct schema is " +
				"available\n\n" +
				"If this is not the case, please convert the database to mbox" +
				" (in case of outlook, confirm the readdbx.exe file is near the emt.jar files)\n\n" +
				"In case of database schema, see install files and database tools at startup (Click on Tools->Preferences->Database, then reboot\n\nIn addition to standard mbox formatting, the division between emails can be specified as a regular expression (java style) by unclicking on the \"std from\" checkbox\n\nClick on test to see how many emails the parser will parse out (output in command line)\nIn case of problems, check create log, which will create a log file of processing\n\n");

	}

    
    public static String getEMTSEARCH() {
        return new String(
                "Welcome to the help file for the Search Window\n\n\n" +
                "To be able to search one needs to create an index of the date to be searched\n" +
                "1) choose an index name (fine to leave default)\n" +
                "Note: for a name abc, a directory dbname_ind_abc will be created to house the index data structures.\n" +
                "2) specify the data criteria (all data, vs sql, vs specific user)\n" +
                "note: for sql , the box will highlight, and can use the test button to make sure it works.\n" +
                "3) Click on execute to index the data. This can take a while for a lot of email.\n" +
                "4) Searching is done by filling in a keyword and clicking on search.\n" +
                "NOTE: to force all keyword, that is to have the intersection include a plus symbol before the keyword (or minus for without)\n" +
                "\n");

    }
    
	public final String getFinance() {
		return new String(
				"Welcome to the help file for the social network section.\n" +
				"\n" +
				"**************************** \n" +
				"\n" +
				"The social networks module allows the user to generate social networks in the following ways:\n" +
				"I. Using database with file creation:\n" +
				"1. Include name of file with a ‘txt’ extension in the field “Filename”.\n" +
				"\n" +
				"2. Select the appropriate options (same options as top panel of “Experiments” section). \n" +
				"The best alternative to generate social networks with the database is selecting 'Use Message Table' and 'Unique MailRef in File'.\n" +
				"\n" +
				"3. Choose any SQL statement necessary for your query in the field ‘SQL where condition’.\n" +
				"\n" +
				"4. Push ‘Execute SQL/Create file’ bottom.\n" +
				"\n" +
				"5. Select text file (‘txt’ extension) to convert to Pajek format (’net’ extension) or bipartite format (‘bp’ extension). If using database information, it is the filename introduced in 1. \n" +
				"The text file has two columns separated by a tab. Each row indicates a relationship between two sets to generate the social networks. For instance, the file may look like:\n" +
				"\n" +
				"A010014	P10078\n" +
				"A070706	P10078\n" +
				"A071851	P10078\n" +
				"\n" +
				"6. Push ‘Txt2Pajek’ bottom to convert to Pajek or ‘Txt2Bipartite’ to convert to bipartite graph  (this format is necessary for transformation to unimodal graph).\n" +
				"\n" +
				"7.Select Pajek or bipartite file.\n" +
				"\n" +
				"8.Select graph format (according to previous selections):  \n" +
				"	a.Pajek: more generic and adequate for most of the cases \n" +
				"	b.Bipartite\n" +
				"	c.Bipartite2unimodal: reads a bipartite graph and converts into a unimodal graph.\n" +
				"	d.Bipartite2unimodalMaxWeakComp: reads a bipartite graph, converts into a unimodal graph, and selects the largest weak component.\n" +
				"Weak component is a subgraph where every vertex v can reach the rest of the vertices in this component regardless of the edge’s direction. A strong component takes into account the edge’s direction. In an undirected graph, a strong and a weak component are equivalent.\n" +
				"\n" +
				"9. Get graph and statistics: graph will be loaded and statistics of network saved in the file ‘metdemo/datasets/socNetIndic.txt’. The statistics calculated for each vertex are: \n" +
				"	•Average distance (avgDistance): For each vertex v in the graph G(V,E), calculates the average shortest path length (average distance)  from v to all other vertices in G, ignoring edge weights.\n" +
				"	•Cluster coefficient (clusterCoeff)\n" +
				"	•Degree: number of vertices that a vertex v is connected to.\n" +
				"	•Rank score of degree distribution (DegreeRankScore)\n" +
				"	•Rank score based on betweenness centrality (BetweennessRankScore)\n" +
				"	•Rank score of “hubs-and-authorities” importance (HITSrank): \n" +
				"\n" +
				"10. Choose type of network:\n" +
				"	a) Random mixed: this is an experimental module that does not need input file. This option creates a random mixed-mode graph with random edge weights. It applies the voltage clustering algorithm by Wu and Huberman [6] to this graph, using half of the 'seed' vertices from the random graph generation as voltage sources, and half of them as voltage sinks.\n" +
				"The following options require that the bottom “Get graph and statistics” was already selected:\n" +
				"	b) Simple network\n" +
				"	c) Cluster network:\n" +
				"		c.1) Edge-betweenness clusterer: generate clusters using edge-betweenness. Visualize the graph using the Fruchtermain-Rheingold layout, and provide a slider so that the user can adjust the clustering granularity.\n" +
				"		c.2) Voltage clusterer: This algorithm discovers communities and is based on voltage drops across networks using the Wu and Huberman [6] algorithm. It provides a slider so that the user can adjust the number of clusters to be generated.  It also has an option to group the clusters. It is necessary to have run first Edge-betweenness clusterer in order to run voltage clusterer properly.\n" +
				"This module uses the libraries of the Jung project http://jung.sourceforge.net. These libraries can generate very large graphs.  However, large graphs (larger than 4,000 vertices) may require increasing the amount of memory assigned to run EMT. \n" +
				"\n" +
				"\n" +
				"II. Using database without file creation: \n" +
				"This option selects the Pajek format by default.\n" +
				"Execute only steps 2, 3, 4, and 10.\n" +
				"\n" +
				"\n" +
				"III. Using external datasets: \n" +
				"	If user has a text file in the format presented in step 5, start in step 5, and complete the next steps. \n" +
				"	If user has a file with Pajek or bipartite format, start in step 6, and complete the next steps.\n");
						}
						
	public final String getDocFlow(){
		return new String("DocFlow Window:\n\n"
			+"This window graphically displays the emailflow information and compare the differerce between two\n"
			+"selected emails. \n\n"
			+"****************************\n"
			+"   Show email flows\n"
			+"****************************\n"
			+"\n"
			+"1. Select email an account from the top dropdown list, and click \"Add above User\" to add\n"
			+"    the account to the list.\n"
			+"2. Repeat step 1 until all of the email accounts of interest are selected.\n"
			+"3. Use \"Clear Users\" to reset the list.\n"
			+"4. Select the range of date to analysis from the top date bars.\n"
			+"4. Select the type of attachment to analyze. (Currently this window only supports \"all types\")\n"
			+"5. Select the emails which \"Only Between These Users\" or \"Any Communication Involving These\n"
			+"    Users\"\n"
			+"6. Click \"Preview Analysis\" to show the email flow\n"
			+"7. There will be a popup window graphically displays the email flows\n"
			+"\n\n****************************\n"
			+"   Understand the email flows\n"
			+"****************************\n"
			+"\n"
			+"1. In the email flow window, each column represents an email flow, which is a group of emails\n"
			+"    that have the same subject.\n"
			+"2. In an email flow, the top pink node is the index. Click on it to show the subject of this email\n"
			+"    flow.\n"
			+"3. All of the other nodes, light blue or dark blue, represents emails. The light blue nodes represent\n"
			+"    emails without attachments and dark blue nodes represent emails containing attachments\n"
			+"4. From top to down, the emails are sorted by time. An arrow starts from an email to its following\n"
			+"    email. Longer arrows represent longer time interval.\n"
			+"5. Select a node or multiple nodes to see the detailed email information such as the sender and the\n"
			+"    time."
			+"5. Finally, the shape of the nodes tells the number of recipients. For example, an email contains\n"
			+"    3 recipients is a triangle, 4 recipients is a square, less or equals to 2 is a circle.\n"
			+"\n\n****************************\n"
			+"   Compare emails\n"
			+"****************************\n"
			+"\n"
			+"1. In the email flow window, select any two email nodes. (If more than two email nodes are selected\n"
			+"    only the first two will be considered)\n"
			+"2. In the \"Comapre area\" at the bottom, select the type of content to compare, the email text,\n"
			+"    the byte content of the attachments, or the parsed Word .doc files. (Currently, the last\n"
			+"    function does not work)\n"
			+"2. Click the \"Compare\" button.\n"
			+"\n\n****************************\n"
			+"   Understand the comparison window\n"
			+"****************************\n"
			+"\n"
			+"1. The first chart shows the exact byte value of the text (or the attachment, depends on which type\n"
			+"    is selected in the email flow window) of these two emails. Red is the content of the first email\n"
			+"    and blue is the second.\n"
			+"2. The second chart is similar to the first chart, but it shows the entropy value of the content.\n"
			+"3. The bottom panel shows the detailed content. The bold characters are the ngrams of the content\n"
			+"    of the second email that do not exist in the first email.\n"
			+"\n"
			+"\n"
			+"\n"
			);
	}

	public static String getCleanData() {
		return new String("The clean data window allows cleanup of mangled domains or to consolidate domain names to be mapped to single name\n\nFor example if you have emails from XX.domain.com and YY.domain.com and want them to all appear as foo.com\nthis tool was designed for you.!");
	}
}

