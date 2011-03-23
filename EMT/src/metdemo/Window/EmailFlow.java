
package metdemo.Window;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.JPanel;
import java.sql.SQLException;
import java.util.*;
import metdemo.winGui;
import metdemo.AlertTools.DetailDialog;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.DataBase.sqlCleanupTools;
import metdemo.MachineLearning.NGram;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.EMTHelp;
import metdemo.dataStructures.contentDis;
import java.text.SimpleDateFormat;


// TODO: Auto-generated Javadoc
/**
 * Class to study the flow behaviour of email visually.
 *
 * @author weijen lee
 * @author shlomo 2007, fixed up things to work for postgres
 */

public class EmailFlow extends JScrollPane implements ActionListener, MouseListener {

    /** The parent. */
    private winGui parent;
    
    /** The panel. */
    private JPanel panel;
    
    /** The scru. */
    private JScrollPane scrd, scru;
    
    /** The date start. */
    private SpinnerDateModel dateStart;
    
    /** The date end. */
    private SpinnerDateModel dateEnd;
    
    /** The sub list. */
    private JList subList;
    
    /** The emailmessage. */
    private JTextArea emailmessage;

    /** The update list. */
    private JButton exe, reset, exemsg, help, getArc, copy, updateList;
    
    /** The body threshold. */
    private JTextField ngramThreshold, bodyThreshold;

    // the display size
    /** The large. */
    private JRadioButton small, medium, large;

    // ngram group type
    /** The ngramattach. */
    private JRadioButton ngramsubject, ngramcontent, ngramattach;
    
    /** The ngram type. */
    private int ngramType = 1;
    
    /** The ngram type curr. */
    private int ngramTypeCurr = 1;

    // the animation
    /** The show animation. */
    private JCheckBox showAnimation;
    
    /** The check flow. */
    private JButton checkFlow;
    
    /** The CHEC k_ flow. */
    private final String CHECK_FLOW = "Step Through Email Flow";
    
    /** The jdbc. */
    private EMTDatabaseConnection jdbc = null;
    // private EMTDatabaseConnection jdbcUpdate=null;

    /** The display. */
    private DisplayEmailFlow display;
    
    /** The usertable. */
    private JTable usertable = null;
    
    /** The tablemodel. */
    private DefaultTableModel tablemodel;
    
    /** The all subject. */
    private String[] allSubject = null;
    
    /** The all body. */
    private String[] allBody = null;
    
    /** The selected subject. */
    private String selectedSubject = "";

    // solve a graphical bug
    /** The tmp width. */
    private int tmpWidth = 500;
    
    /** The tmp height. */
    private int tmpHeight = 500;

    /** The index table. */
    private EmailFlowNode[] indexTable = null;

    /** The m_md5 renderer. */
    private md5CellRenderer m_md5Renderer;
    
    /** The clipboard. */
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * Main constructor.
     *
     * @param jdbcV the jdbc v
     * @param m the m
     * @param w the w
     */
    public EmailFlow(EMTDatabaseConnection jdbcV, md5CellRenderer m, winGui w) {
        parent = w;
        m_md5Renderer = m;
        jdbc = jdbcV;
      
        // the main panel
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();

        panel = new JPanel();
        panel.setLayout(gridbag);
        
        small = new JRadioButton("small");
        small.setActionCommand("small");
        small.addActionListener(this);
        small.setSelected(true);
        medium = new JRadioButton("medium");
        medium.setActionCommand("medium");
        medium.addActionListener(this);
        large = new JRadioButton("large");
        large.setActionCommand("large");
        large.addActionListener(this);
        ButtonGroup sizebg = new ButtonGroup();
        sizebg.add(small);
        sizebg.add(medium);
        sizebg.add(large);
        small.setToolTipText("Set the size of display panel");
        medium.setToolTipText("Set the size of display panel");
        large.setToolTipText("Set the size of display panel");

        // showAnimation = new JCheckBox("Show Animation");

        ngramThreshold = new JTextField("0.8", 5);
        ngramThreshold.setToolTipText("NGram Threshold for Subject");

        bodyThreshold = new JTextField("80", 5);
        bodyThreshold.setToolTipText("NGram Threshold for the length of content");

        ngramsubject = new JRadioButton("By Subject");
        ngramsubject.setActionCommand("ngramsubject");
        ngramsubject.addActionListener(this);
        ngramsubject.setSelected(true);
        ngramcontent = new JRadioButton("By Content");
        ngramcontent.setActionCommand("ngramcontent");
        ngramcontent.addActionListener(this);
        ngramattach = new JRadioButton("By Attachment");
        ngramattach.setActionCommand("ngramattach");
        ngramattach.addActionListener(this);
        ButtonGroup ngrambg = new ButtonGroup();
        ngrambg.add(ngramsubject);
        ngrambg.add(ngramcontent);
        ngrambg.add(ngramattach);
        ngramsubject.setToolTipText("Group Emails by Subject");
        ngramcontent.setToolTipText("Group Emails by Content");
        ngramattach.setToolTipText("Group Emails by Attachment");

        exe = new JButton("Run");
        exe.setBackground(Color.green);
        exe.addActionListener(this);
        exe.setToolTipText("Display the email flow");

        reset = new JButton("Clean");
        reset.addActionListener(this);
        reset.setToolTipText("Clean the panel");

        checkFlow = new JButton(CHECK_FLOW);
        // checkFlow.setBackground(Color.magenta);
        checkFlow.addActionListener(this);
        checkFlow.setToolTipText("See the emain \"flow\"");
        /*
         * exemsg = new JButton("Get User from Msg. Tab");
         * exemsg.setBackground(Color.red); exemsg.addActionListener(this);
         * exemsg.setToolTipText("Get the email account from Message Tab");
         */
        copy = new JButton("Copy Email Table");
        copy.setToolTipText("Copy the email table contents to system clipboard");
        copy.addActionListener(this);

        updateList = new JButton("Update Email List");
        updateList.setToolTipText("Update the Email List");
        updateList.addActionListener(this);
        updateList.setBackground(Color.yellow);

        EMTHelp emthelp = new EMTHelp(EMTHelp.EMAILFLOW);

        subList = new JList();

        subList.setToolTipText("Email Subjects");
        JScrollPane listpane = new JScrollPane(subList);
        listpane.setPreferredSize(new Dimension(200, 100));

        display = new DisplayEmailFlow(this);
        scrd = new JScrollPane(display);
        scrd.setPreferredSize(new Dimension(550, 550));
        Border bd1 = BorderFactory.createEtchedBorder(Color.white, new Color(0, 0, 0));
        TitledBorder ttl1 = new TitledBorder(bd1, "Email Flow");
        scrd.setBorder(ttl1);

        usertable = new JTable();

        JPanel emailmsgPane = new JPanel();
        GridBagConstraints gbc2 = new GridBagConstraints();
        GridBagLayout gridbag2 = new GridBagLayout();
        emailmsgPane.setLayout(gridbag2);

        // emailmessage = new JTextArea("Message body will go here.......");
        // JScrollPane areaScrollPane = new JScrollPane(emailmessage);
        // emailmessage.setLineWrap(true);
        // emailmessage.setWrapStyleWord(true);

        usertable.addMouseListener(this);
        JScrollPane tables = new JScrollPane(usertable);

        scru = new JScrollPane(tables);
        scru.setPreferredSize(new Dimension(400, 550));
        Border bd2 = BorderFactory.createEtchedBorder(Color.white, new Color(0, 0, 0));

        TitledBorder ttl2 = new TitledBorder(bd1, "User & Email Table");
        scru.setBorder(ttl2);

        /** ********************************** */
        // add components
        gbc.insets = new Insets(3, 3, 3, 3);
        
        gbc.gridheight = 1;
        gbc.gridwidth = 1;

        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(new JLabel("Display Size:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        panel.add(small, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        panel.add(medium, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        panel.add(large, gbc);
        /*
         * gbc.gridx = 6; gbc.gridy = 0; panel.add(showAnimation, gbc);
         */
        gbc.gridwidth = 2;
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(new JLabel("N-Gram Threshold for Subject:"), gbc);
        /*
         * gbc.gridx = 3; gbc.gridy = 1; panel.add(new JLabel("Subject:"), gbc);
         */
        gbc.gridwidth = 1;
        gbc.gridx = 4;
        gbc.gridy = 1;
        panel.add(ngramThreshold, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 5;
        gbc.gridy = 1;
        panel.add(new JLabel("Length of Content to be Check:"), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 7;
        gbc.gridy = 1;
        panel.add(bodyThreshold, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        panel.add(new JLabel("Group Type:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        panel.add(ngramsubject, gbc);

        gbc.gridx = 4;
        gbc.gridy = 2;
        panel.add(ngramcontent, gbc);

        gbc.gridx = 5;
        gbc.gridy = 2;
        panel.add(ngramattach, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 2;
        gbc.gridy = 3;
        panel.add(exe, gbc);

        /*
         * gbc.gridx = 5; gbc.gridy = 2; panel.add(reset, gbc);
         */

        gbc.gridx = 3;
        gbc.gridy = 3;
        // panel.add(help, gbc);
        panel.add(emthelp, gbc);

        gbc.gridx = 4;
        gbc.gridy = 3;
        panel.add(checkFlow, gbc);

        /*
         * gbc.gridheight = 1; gbc.gridwidth = 2; gbc.gridx = 2; gbc.gridy = 3;
         * panel.add(exemsg, gbc);
         */
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(updateList, gbc);

        gbc.gridheight = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(listpane, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(scru, gbc);
        // panel.add(emailmsgPane, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = 7;
        gbc.gridx = 4;
        gbc.gridy = 4;
        panel.add(scrd, gbc);

        setViewportView(panel);
    }

    /**
     * Gets the today.
     *
     * @return the today
     */
    public final int[] getToday() {
        // get the date of today, the end of testing
        Calendar rightNow = Calendar.getInstance();
        Date d = rightNow.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(d);

        String[] seg = today.split("-");
        int[] date = new int[3];
        date[0] = Integer.parseInt(seg[0]);
        date[1] = Integer.parseInt(seg[1]);
        date[2] = Integer.parseInt(seg[2]);
        return date;
    }

    /**
     * method to get the content movement.
     *
     * @return the content flow
     */

    public final boolean getContentFlow() {
        String sub = (String) subList.getSelectedValue();
        int n = sub.lastIndexOf("(");
        
        if(n>0)
        	sub = sub.substring(0, n - 1);
        else{//we want to check if its blank subject
        	if(n==0){
        		sub = "";
        	}
        }
        
        selectedSubject = sub;
        // get NGram group
        double thr = Double.parseDouble(ngramThreshold.getText());
        NGram ng = new NGram();
        ArrayList<String> group = null;
        String data[][] = null;
        if (ngramTypeCurr == 1) {

            // get ngram group
        	group = ng.groupContentWithPatternRemoveNumbers(allSubject, thr, sub);
            // if get an empty group
            if (group == null || group.size() == 0)
            { 
            	System.out.println("No groups returned");
            	return false;
            
            }
//          query
            String query = "select sender,rcpt,UTIME,mailref from email where ";

            String tmps = sqlCleanupTools.ReplaceQuoteSQL(group.get(0));
            query += "subject='" + tmps + "' ";
            for (int i = 1; i < group.size(); i++) {
                tmps =  sqlCleanupTools.ReplaceQuoteSQL(group.get(i));
                query += "or subject='" + tmps + "' "; //tmps.replaceAll("'", "\\\\'")
            }
            query += "order by UTIME";
           
            data = runSql(query);
        } else if (ngramTypeCurr == 2) {// group by content
            // get ngram group
            String[] trimedAllBody = new String[allBody.length];
            int bodythr = Integer.parseInt(bodyThreshold.getText());
            for (int i = 0; i < allBody.length; i++) {
                int tmpthr = bodythr;
                if (tmpthr > allBody[i].length())
                    tmpthr = allBody[i].length();
                // System.out.println(tmpthr+" "+allBody[i].length());
                if (allBody[i].length() < 2)
                    trimedAllBody[i] = allBody[i];
                else
                    trimedAllBody[i] = allBody[i].substring(0, tmpthr - 1);

            }
            ArrayList tmpgroup = ng.groupContent(trimedAllBody, thr, true);
            int id = subList.getSelectedIndex();
            boolean isbreak = false;
            int k = 0;

            // so many loops, too bad~~~
            while (k < tmpgroup.size() && !isbreak) {
                Vector tmp2 = (Vector) tmpgroup.get(k);
                for (int j = 0; j < tmp2.size(); j++) {
                    contentDis obj = (contentDis) tmp2.get(j);
                    // if find the group, add the group members to "group"
                    if (obj.id == id) {
                        group = new ArrayList<String>();
                        for (int l = 0; l < tmp2.size(); l++) {
                            contentDis tmp3 = (contentDis) tmp2.get(l);
                            group.add(allSubject[tmp3.id]);
                        }
                        isbreak = true;
                        break;
                    }
                }
                k++;
            }

            // query
            String query = "select sender,rcpt,insertTime,mailref from email where ";

            // if get an empty group
            if (group == null || group.size() == 0)
                return false;

            query += "mailref='" + (String) group.get(0) + "' ";
            for (int i = 1; i < group.size(); i++) {
                query += "or mailref='" + group.get(i) + "' ";
            }
            query += "order by insertTime";
            data = runSql(query);
        } else if (ngramTypeCurr == 3) {// group by attachment

            if (sub.indexOf("Emails with no attachment") != -1) {
                // String tmpuser = (String)userCombo.getSelectedItem();
                String tmpuser = (String) parent.getSelectedUser();
                String query = "select e.sender,e.rcpt,e.UTIME,m.mailref from email e,message m where e.mailref=m.mailref and m.filename=''"
                        + "and (sender='" + tmpuser + "' or rcpt='" + tmpuser + "')";

                query += " order by UTIME";
                data = runSql(query);
            } else {
                // get ngram group
                group = ng.groupContentWithPattern(allSubject, thr, sub);

                // query
                String query = "select e.sender,e.rcpt,e.UTIME,m.mailref from email e,message m where e.mailref=m.mailref and (";

                // if get an empty group
                if (group == null || group.size() == 0)
                    return false;
                String tmps =  sqlCleanupTools.ReplaceQuoteSQL(group.get(0));
                query += "m.filename='" + tmps + "' ";
                for (int i = 1; i < group.size(); i++) {
                    tmps =  sqlCleanupTools.ReplaceQuoteSQL(group.get(i));
                    query += "or filename='" + tmps + "' ";
                }
                query += ") order by UTIME";
                data = runSql(query);
            }
        }

        // order the data
        // save the "nodes", for the plot
        HashMap table = new HashMap();
        String head = "";
        if (data.length > 0)
            head = data[0][0];

        // sometimes, a user may only receive emails
        HashMap senderlist = new HashMap();

        for (int i = 0; i < data.length; i++) {
            String[] sname = data[i][0].split("@");
            String key = sname[0] + data[i][2];// sender+insertTime
            if (table.containsKey(key)) {
                EmailFlowNode node = (EmailFlowNode) table.remove(key);
                node.rcpt.add(data[i][1]);
                table.put(key, node);
            } else {
                EmailFlowNode node = new EmailFlowNode(data[i][0]);
                node.mailref = data[i][3];
                node.time = data[i][2];
                node.rcpt.add(data[i][1]);
                table.put(key, node);
            }

            // System.out.println(data[i][0]+" "+data[i][1]+" "+data[i][2]);
        }

        // sort the table by time
        EmailFlowNode[] nodes = display.getMap(table);
        Arrays.sort(nodes);

        // call to display the graphics
        if (!table.isEmpty()) {
            // for the table
            buildTable(data, nodes);
            display.displayFlow(nodes, head);
            display.draw();
        }
        return true;
    }

    // public void buildTable(String[][] users, Map table){
    /**
     * Builds the table.
     *
     * @param users the users
     * @param nodes the nodes
     */
    public final void buildTable(String[][] users, EmailFlowNode[] nodes) {
        try {
            Vector tmpuser = new Vector();
            for (int i = 0; i < users.length; i++) {

                if (users[i][0].indexOf("@") >= 1 && users[i][1].indexOf("@") >= 1) {
                    String[] userseg1 = users[i][0].split("@");
                    String[] userseg2 = users[i][1].split("@");

                    if (!tmpuser.contains(userseg1[0]))
                        tmpuser.add(userseg1[0]);
                    if (!tmpuser.contains(userseg2[0]))
                        tmpuser.add(userseg2[0]);
                }
                // if(!tmpuser1.contains(users[i][0]))tmpuser1.add(users[i][0]);
                // if(!tmpuser1.contains(users[i][1]))tmpuser1.add(users[i][1]);
            }

            String[] tmpuser2 = new String[tmpuser.size() + 1];
            tmpuser2[0] = "Index";
            for (int i = 1; i < tmpuser2.length; i++) {
                tmpuser2[i] = (String) tmpuser.get(i - 1);
            }

            // EmailFlowNode[] nodes = display.getMap(table);
            indexTable = nodes;
            String[][] elements = new String[nodes.length][tmpuser2.length];
            for (int i = 0; i < elements.length; i++) {

                elements[i][0] = String.valueOf(i);
                String[] segsender = nodes[i].sender.split("@");
                int s = tmpuser.indexOf(segsender[0]) + 1;
                elements[i][s] = "sender";
                for (int j = 0; j < nodes[i].rcpt.size(); j++) {
                    String rcpt = (String) nodes[i].rcpt.get(j);
                    String[] segrcpt = rcpt.split("@");
                    int r = tmpuser.indexOf(segrcpt[0]) + 1;
                    if (r != s)
                        elements[i][r] = "rcpt";
                    else
                        elements[i][r] = "s & r";
                }
            }

            tablemodel = new DefaultTableModel(elements, tmpuser2) {
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            // usertable = new JTable(elements,tmpuser2);
            usertable = new JTable(tablemodel);
            usertable.addMouseListener(this);
            // usertable.setEnabled(false);
            TableColumn getaColumn;

            for (int i = 0; i < usertable.getColumnCount(); i++) {
                String cname = usertable.getColumnName(i);
                getaColumn = usertable.getColumn(cname);
                getaColumn.setCellRenderer(m_md5Renderer);
            }

            JPanel emailmsgPane = new JPanel();

            emailmsgPane.setLayout(new BorderLayout());
            emailmessage = new JTextArea("Message body will go here...", 4, 30);
            emailmessage.setLineWrap(true);
            emailmessage.setWrapStyleWord(true);
            JScrollPane ppm = new JScrollPane(emailmessage);
            ppm.setPreferredSize(new Dimension(300, 180));
            emailmessage.setLineWrap(true);
            emailmessage.setWrapStyleWord(true);

            JScrollPane pp1 = new JScrollPane(usertable);
            pp1.setPreferredSize(new Dimension(50 * (tmpuser2.length), 18 * elements.length));
            JScrollPane pp2 = new JScrollPane(pp1);
            pp2.setPreferredSize(new Dimension(300, 300));

            JPanel copyPane = new JPanel();
            copyPane.add(copy);

            emailmsgPane.add(copyPane, BorderLayout.NORTH);
            emailmsgPane.add(pp2, BorderLayout.CENTER);
            emailmsgPane.add(ppm, BorderLayout.SOUTH);
            scru.setViewportView(emailmsgPane);
            // setViewportView(panel);
            repaint();
        } catch (Exception e) {
            System.out.println("built table warning:" + e);
        }
    }

    /**
     * Builds the null table.
     */
    public final void buildNullTable() {
        usertable = new JTable();
        JScrollPane pp = new JScrollPane(usertable);
        scru.setViewportView(pp);
        setViewportView(panel);
    }

    /*
     * //change OOOO-OO-OO XX:XX:XX to Long value public final long
     * getMill(String in){ String[] dt=in.split(" "); String[]
     * date=dt[0].split("-"); String[] time=dt[1].split(":");
     * if(time[2].indexOf(".") != -1){ time[2] =
     * time[2].substring(0,time[2].indexOf(".")); } Calendar
     * c=Calendar.getInstance(); c.set(Integer.parseInt(date[0])
     * ,Integer.parseInt(date[1])-1 ,Integer.parseInt(date[2])
     * ,Integer.parseInt(time[0]) ,Integer.parseInt(time[1])
     * ,Integer.parseInt(time[2]));
     * 
     * long ms = c.getTimeInMillis(); return ms; }
     */
    // execute the sql command
    /**
     * Run sql.
     *
     * @param sql the sql
     * @return the string[][]
     */
    public final String[][] runSql(String sql) {
        String[][] data = null;
        try {
            synchronized (jdbc) {
                data = jdbc.getSQLData(sql);
            }
        } catch (SQLException ex) {
            if (!ex.toString().equals("java.sql.SQLException: ResultSet is from UPDATE. No Data")) {
                System.out.println("ex: " + ex);
                System.out.println("sql:" + sql);
            }
        }
        return data;
    }
    
    /**
     * Run sql first.
     *
     * @param sql the sql
     * @return the string[]
     */
    public final String[] runSqlFirst(String sql) {
        String[] data = null;
        try {
            synchronized (jdbc) {
                data = jdbc.getSQLDataByColumn(sql,1);
            }
        } catch (SQLException ex) {
            if (!ex.toString().equals("java.sql.SQLException: ResultSet is from UPDATE. No Data")) {
                System.out.println("ex: " + ex);
                System.out.println("sql:" + sql);
            }
        }
        return data;
    }

    /**
     * Gets the email info.
     *
     * @param user the user
     * @param dbType the db type
     * @return the email info
     */
    private final String[] getEmailInfo(final String user,final String dbType) {
        try {
            String[] date = getDate();
            if (ngramType == 1) {
                ngramTypeCurr = ngramType;
                String query = null;
                if(dbType.equals(DatabaseManager.sMYSQL) || dbType.equals(DatabaseManager.sCMYSQL) )
                {
                	query = "select CONCAT(subject,' (',count(*),')'),count(*) as cnt from email where DATES>'" + date[0] + "' and DATES<'"
                    + date[1] + "' and (sender='" + user + "' or rcpt='" + user
                    + "') group by subject order by cnt desc";
                	
                	
                }else if ( dbType.equals(DatabaseManager.sPOSTGRES)){
                	query = "select subject || '(' || count(*) || ')' ,count(*) as cnt from email where DATES>'" + date[0] + "' and DATES<'"
                    + date[1] + "' and (sender='" + user + "' or rcpt='" + user
                    + "') group by subject order by cnt desc";
                }else{
                	//assume derby
                	query = "select subject || '(' || CHAR(count(*))||')' ,count(*) as cnt from email where DATES>'" + date[0] + "' and DATES<'"
                    + date[1] + "' and (sender='" + user + "' or rcpt='" + user
                    + "') group by subject order by cnt desc";
                	
                }
                
                
                
                
                /*String query = "select subject,count(*) as cnt from email where DATES>'" + date[0] + "' and DATES<'"
                        + date[1] + "' and (sender='" + user + "' or rcpt='" + user
                        + "') group by subject order by cnt desc";
*/
                //String[][] data = runSql(query);
                //String[] subject = new String[data.length];
               // allSubject = new String[data.length];
               // for (int i = 0; i < data.length; i++) {
               //     subject[i] = data[i][0] + " (" + data[i][1] + ")";
               //    allSubject[i] = data[i][0];
               // }
                allSubject = runSqlFirst(query);
                return allSubject;
            } else if (ngramType == 2) {
                ngramTypeCurr = ngramType;
                String query = "select e.mailref,m.body,e.subject,count(*) as cnt from email e,message m where e.DATES>'"
                        + date[0]
                        + "' and e.DATES<'"
                        + date[1]
                        + "' and e.mailref=m.mailref and (sender='"
                        + user
                        + "' or rcpt='" + user + "') group by e.mailref,m.body,e.subject order by cnt desc";

                // System.out.println("query:"+query);
                String[][] data = runSql(query);
                String[] mailref = new String[data.length];
               allSubject = new String[data.length];
                allBody = new String[data.length];
                for (int i = 0; i < data.length; i++) {
                    mailref[i] = data[i][2] + " MAILREF:" + data[i][0] + " (" + data[i][3] + ")";
                    // mailref[i] = data[i][0] + " (" + data[i][3] + ")";
                  allSubject[i] = data[i][0];
                    allBody[i] = data[i][1];
                }
                return mailref;
            } else if (ngramType == 3) {// test1
                ngramTypeCurr = ngramType;
                String query = "select m.filename,count(*) as cnt from email e,message m where e.DATES>'" + date[0]
                        + "' and e.DATES<'" + date[1] + "' and e.mailref=m.mailref and (e.sender='" + user
                        + "' or e.rcpt='" + user + "') group by m.filename order by cnt desc";
                String[][] data = runSql(query);
                String[] filenames = new String[data.length];
                allSubject = new String[data.length];

                for (int i = 0; i < data.length; i++) {// skip the empty
                                                        // filename
                    if (!data[i][0].equals("")) {
                        filenames[i] = data[i][0] + " (" + data[i][1] + ")";
                        allSubject[i] = data[i][0];
                    } else {
                        filenames[i] = "Emails with no attachment" + " (" + data[i][1] + ")";
                        allSubject[i] = "Emails with no attachment";
                    }
                }

                return filenames;
            } else
                return null;
        } catch (Exception e) {
            System.out.println("getEmail error:" + e);
            return null;
        }
    }

    /**
     * Update subject.
     *
     * @param user the user
     */
    public final void updateSubject(String user) {
       
    	subList.setListData(getEmailInfo(user,jdbc.getTypeDB()));
        setViewportView(panel);
    }

    // get the date as yy-mm-dd
    /**
     * Gets the date.
     *
     * @return the date
     */
    public final String[] getDate() {
        /*
         * SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         * String[] startAndEnd = new String[2]; startAndEnd[0] =
         * format.format(dateStart.getDate()); startAndEnd[1] =
         * format.format(dateEnd.getDate());
         * 
         * return startAndEnd;
         */
        return parent.getDateRange_toptoolbar();
    }

    /*
     * public void getAccount(String account){
     * if(display.points.containsKey(account)){ EmailFlowNode node =
     * (EmailFlowNode)display.points.remove(account);
     * if(display.lastNode!=null){ display.points.remove(display.lastNode.name);
     *  } display.lastNode = node; node.size = 14; node.color = Color.red; } }
     */

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        String arg = evt.getActionCommand();
        // System.out.println("action:"+arg);

        if (arg.equals("Run")) {
            if (allSubject != null && subList.getSelectedIndex() != -1) {
                display.width = tmpWidth;
                display.height = tmpHeight;
                // BusyWindow bw = new BusyWindow("Running...","Progress");
                // bw.show();
                boolean ok = getContentFlow();
                if (!ok) {
                    display.displayNullFlow();
                    display.draw();
                    buildNullTable();

                    // some message
                    String msg = "Null Flow!\n\n";
                    msg += "1. No Emails.\n";
                    msg += "or\n";
                    msg += "2. Try a lower Threshold.";
                    JOptionPane.showMessageDialog(this, msg, "Hi there!", JOptionPane.INFORMATION_MESSAGE);
                }

                /*
                 * if(showAnimation.isSelected()){ for(int i=0;i<display.animateNodes.size();i++){
                 * //usertable.setEditingRow(i);
                 * //usertable.setSelectionBackground(Color.red); //String tmpv =
                 * (String)usertable.getValueAt(i, 0);
                 * //usertable.setValueAt("Selecting", i, 0);
                 * display.animateIndex = i; display.ani(); try{
                 * Thread.sleep(700); } catch(Exception e){}
                 * //usertable.setValueAt(tmpv, i, 0);
                 *  } }
                 */
                // bw.hide();
            } else {
                JOptionPane.showMessageDialog(EmailFlow.this,
                        "Please clikc update email list to have something to choose from.");
            }
        } else if (arg.equals(CHECK_FLOW)) {
            if (allSubject != null && subList.getSelectedIndex() != -1 && usertable.getRowCount() > 0) {
                int row = usertable.getSelectedRow() + 1;
                if (row >= usertable.getRowCount())
                    row = 0;
                // usertable.addRowSelectionInterval(row, row);
                usertable.changeSelection(row, 0, false, false);
                usertable.setSelectionBackground(Color.red);
                if (indexTable != null) {
                    showEmailBody(indexTable[row], true);
                }
                display.animateIndex = row;
                display.draw();
            }
        } else if (arg.equals("Update Email List")) {
            if (parent.getSelectedUserIndex() >= 0) {

                String user = (String) parent.getSelectedUser();
                updateSubject(user);
            }
        } else if (arg.equals("ngramsubject")) {
            ngramType = 1;
        } else if (arg.equals("ngramcontent")) {
            ngramType = 2;
        } else if (arg.equals("ngramattach")) {
            ngramType = 3;
        }

        // set size
        else if (arg.equals("small")) {
            tmpWidth = 500;
            tmpHeight = 500;
        } else if (arg.equals("medium")) {
            tmpWidth = 1000;
            tmpHeight = 1000;
        } else if (arg.equals("large")) {
            tmpWidth = 2000;
            tmpHeight = 2000;
        }

        else if (arg.equals("Clean")) {
            // display.resetClickedAccount();
            display.resetOnArc();
            display.draw();
        }
        /*
         * else if(arg.equals("Get User from Msg. Tab")){ String user =
         * parent.getMsgTabUser(); if(user!=null){
         * userCombo.setSelectedItem(user); updateSubject(user); } else{ String
         * message = "Please select a user from Message Tab first.";
         * JOptionPane.showMessageDialog(this, message); } }
         */
        else if (arg.equals("Help")) {
            showHelp();
        } else if (arg.equals("Copy Email Table")) {
            if (tablemodel != null)
                setCopy();
        }

    }

    /**
     * Sets the copy.
     */
    public final void setCopy() {

        Vector select = tablemodel.getDataVector();
        String neat = new String();
        for (int i = 0; i < usertable.getColumnCount(); i++) {
            neat += (String) usertable.getColumnName(i);
            if (i != usertable.getColumnCount() - 1)
                neat += ",";
        }
        neat += "\n";

        int j = 0;
        for (int i = 0; i < select.size(); i++) {
            Vector v = (Vector) select.elementAt(i);
            for (j = 0; j < v.size() - 1; j++)
                neat += (String) v.elementAt(j) + ",";
            neat += v.elementAt(j) + "\n";
        }
        StringSelection data = new StringSelection(neat);
        clipboard.setContents(data, data);
    }

    /**
     * Show help.
     */
    public final void showHelp() {
        /*
         * String file = "metdemo/EmailFlowHelp.txt"; String message = ""; try{
         * BufferedReader in = new BufferedReader(new FileReader(file));
         * 
         * while(in.ready()){ message += in.readLine()+"\n"; } } catch(Exception
         * e){ System.out.println("reaf file error:"+file+" "+e); }
         */
        String message = EMTHelp.getEmailFlow();
        String title = "Help";
        DetailDialog dialog = new DetailDialog(null, false, title, message);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Show email body.
     *
     * @param node the node
     * @param showWindow the show window
     * @return the string
     */
    public final String showEmailBody(EmailFlowNode node, boolean showWindow) {
        String message = "";

        message += "Sender: " + node.sender + "\n";

        message += "Rcpt: ";
        for (int i = 0; i < node.rcpt.size(); i++) {
            message += (String) node.rcpt.get(i);
            if (i != node.rcpt.size() - 1)
                message += ", ";
        }
        message += "\n";

        message += "Time: " + node.time + "\n";
        message += "Subject: " + selectedSubject;
        message += "\nBody:\n";

        String query = "select type,filename,body from message where mailref='" + node.mailref + "'";

        String[][] content = runSql(query);
        // message += "*******************";
        if (content != null && content.length > 0) {
            message += "Type: " + content[0][0] + "\n";
            message += "Filename: " + content[0][1] + "\n";
            message += "Content:\n";
            message += content[0][2] + "\n";
        }

        // System.out.println(message);
        // JOptionPane pane = new
        // JOptionPane(message,JOptionPane.INFORMATION_MESSAG);
        if (showWindow) {
            /*
             * String title = "Email Detail"; DetailDialog dialog = new
             * DetailDialog(null, false, title, message);
             * dialog.setLocationRelativeTo(this); dialog.show();
             */
            emailmessage.setText(message);
            emailmessage.setCaretPosition(0);
        }
        return message;
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        // System.out.println(1);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        // System.out.println(2);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        // System.out.println(3);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // System.out.println(4);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        // if(e.getClickCount()==2){
        Point p = e.getPoint();
        int row = usertable.rowAtPoint(p);
        usertable.setSelectionBackground(Color.red);
        // int row = usertable.getSelectedRow();
        if (indexTable != null) {
            showEmailBody(indexTable[row], true);
        }
        display.animateIndex = row;
        display.draw();
        // display.ani2();
        // }
    }

}

class EmailFlowNode implements Comparable {

    String mailref = "";
    String time = "";
    // String subject = "";

    // elements in the table
    String sender;
    Vector rcpt;

    // for the graphics
    String name;
    // points
    int size;
    int x;
    int y;
    Color color;

    // lines
    int x1;
    int y1;
    int x2;
    int y2;
    int weight;

    // for scale circle
    int diameter;

    // for the animation
    Vector lines = new Vector();// vector of vector
    Vector rcptNodes = new Vector();// vector of vector

    public EmailFlowNode(String s) {
        sender = s;
        rcpt = new Vector();
    }

    public EmailFlowNode(int s, int p1, int p2, Color c, String n) {
        size = s;
        x = p1;
        y = p2;
        color = c;
        name = n;
    }

    public EmailFlowNode(int p1, int p2, int p3, int p4, Color c) {
        x1 = p1;
        y1 = p2;
        x2 = p3;
        y2 = p4;
        color = c;
        weight = 1;
    }

    public EmailFlowNode(int p1, int p2, int d, Color c) {
        x = p1;
        y = p2;
        color = c;
        diameter = d;
    }

    public EmailFlowNode(String n, int p1, int p2) {
        name = n;
        x = p1;
        y = p2;
    }

    public int compareTo(Object o) {
        EmailFlowNode other = (EmailFlowNode) o;
        int compared = time.compareTo(other.time);
        return compared;
    }
}

class DisplayEmailFlow extends JPanel implements MouseListener {

    EmailFlow parent = null;

    EmailFlowNode[] clonedTable = null;

    int height = 500;
    int width = 500;
    final int base = 15;
    EmailFlowNode lastNode = null;

    HashMap points = new HashMap();
    EmailFlowNode[] pointsArray = null;
    HashMap lines = new HashMap();
    EmailFlowNode[] linesArray = null;
    int NumOfScaleCircle = 0;
    // Vector clickedAccount = new Vector();
    Vector onArc = new Vector();

    Vector animateNodes = new Vector();// EmailFlowNode
    int animateIndex = -1;

    public DisplayEmailFlow(EmailFlow ef) {
        parent = ef;

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
            }

            public void mouseClicked(MouseEvent evt) {
                int x = evt.getX();
                int y = evt.getY();
                int button = evt.getButton();
                if (x >= 5 && x <= 200 && y >= 5 && y <= 20) {
                    if (onArc.size() >= 2)
                        popupArc();
                }

                EmailFlowNode check = checkNode(x, y);
                if (check != null) {
                    // check if already selected
                    boolean isSelected = false;
                    for (int i = 0; i < onArc.size(); i++) {
                        EmailFlowNode test = (EmailFlowNode) onArc.get(i);
                        if (test.name.equals(check.name))
                            isSelected = true;
                    }

                    if (!isSelected) {
                        if (onArc.size() >= 2)
                            onArc.remove(0);
                        onArc.add(new EmailFlowNode(check.name, check.x, check.y));
                        draw();
                    }// if(!isSelected){
                }// if(check!=null){
            }// public void mouseClicked(MouseEvent evt){
        });
        addMouseListener(this);
        setPreferredSize(new Dimension(width * 4, height * 4));
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, width * 4, height * 4);

        // draw a "button"
        g.setColor(Color.cyan);
        g.fillRect(5, 5, 200, 15);
        g.setColor(Color.black);
        g.drawString("Click here for email exchange info.", 7, 18);

        if (!points.isEmpty() && !lines.isEmpty()) {
            drawLines(g);
            drawPoints(g);
            if (NumOfScaleCircle != 0)
                drawScaleCircle(g);

            // if(clickedAccount!=null)displayClickedAccount(g);
            if (onArc != null)
                displayOnArc(g);
        }

        if (animateIndex != -1) {
            ani2(g);

        }
    }

    // the animation
    public void ani() {
        Graphics gg = getGraphics();
        gg.setColor(Color.white);
        gg.fillRect(0, 0, width * 4, height * 4);

        if (!points.isEmpty() && !lines.isEmpty()) {
            drawLines(gg);
            drawPoints(gg);
            if (NumOfScaleCircle != 0)
                drawScaleCircle(gg);

            if (onArc != null)
                displayOnArc(gg);
        }
        ani2(gg);

    }

    // for convenience
    public void ani2(Graphics gg) {
        // Graphics gg=getGraphics();
        // for animation
        EmailFlowNode tmpnode = (EmailFlowNode) animateNodes.get(animateIndex);
        fillCircle(gg, tmpnode.x, tmpnode.y, tmpnode.size, tmpnode.color);
        for (int j = 0; j < tmpnode.rcptNodes.size(); j++) {
            EmailFlowNode tmprcpt = (EmailFlowNode) tmpnode.rcptNodes.get(j);
            fillCircle(gg, tmprcpt.x, tmprcpt.y, tmprcpt.size, tmprcpt.color);
        }
        for (int j = 0; j < tmpnode.lines.size(); j++) {
            EmailFlowNode tmpline = (EmailFlowNode) tmpnode.lines.get(j);
            gg.setColor(tmpline.color);
            gg.drawLine(tmpline.x1, tmpline.y1, tmpline.x2, tmpline.y2);
        }
    }

    /*
     * public void displayClickedAccount(Graphics g){ int base = 15; int y = 35;
     * int x = 15; g.setColor(Color.red); for(int i=0;i<clickedAccount.size();i++){
     * EmailFlowNode node = (EmailFlowNode)clickedAccount.get(i);
     * g.drawString(node.name , x, y); drawCircle(g, node.x, node.y, 12,
     * Color.red); y+=base; } }
     */
    public void displayOnArc(Graphics g) {
        int base = 15;
        int y = 35;
        int x = 15;
        g.setColor(Color.red);
        for (int i = 0; i < onArc.size(); i++) {
            EmailFlowNode node = (EmailFlowNode) onArc.get(i);
            drawCircle(g, node.x, node.y, 12, Color.red);
            g.drawString(node.name, x, y);
            y += base;
        }
    }

    public final void popupArc() {
        // get needed emails
        Vector toDisplay = new Vector();
        EmailFlowNode n1 = (EmailFlowNode) onArc.get(0);
        EmailFlowNode n2 = (EmailFlowNode) onArc.get(1);
        String u1 = n1.name;
        String u2 = n2.name;
        for (int i = 0; i < clonedTable.length; i++) {
            if (clonedTable[i].sender.equals(u1)) {
                for (int j = 0; j < clonedTable[i].rcpt.size(); j++) {
                    String rcpt = (String) clonedTable[i].rcpt.get(j);
                    if (rcpt.equals(u2)) {
                        toDisplay.add(clonedTable[i]);
                        break;
                    }
                }
            } else if (clonedTable[i].sender.equals(u2)) {
                for (int j = 0; j < clonedTable[i].rcpt.size(); j++) {
                    String rcpt = (String) clonedTable[i].rcpt.get(j);
                    if (rcpt.equals(u1)) {
                        toDisplay.add(clonedTable[i]);
                        break;
                    }
                }
            }
        }

        int size = toDisplay.size();
        String message = size + " emails between \"" + u1 + "\" and \"" + u2 + "\"";
        message += "\n\n";
        for (int i = 0; i < size; i++) {
            EmailFlowNode node = (EmailFlowNode) toDisplay.get(i);
            String msg = parent.showEmailBody(node, false);
            message += "========================================\n";
            message += " *** #" + (i + 1) + " ***\n" + msg + "\n\n";
        }

        String title = "Email Detail";
        DetailDialog dialog = new DetailDialog(null, false, title, message);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /*
     * public void resetClickedAccount(){ clickedAccount = new Vector(); }
     */
    public final void resetOnArc() {
        onArc = new Vector();
    }

    public void draw() {
        super.repaint();
    }

    public final EmailFlowNode[] getMap(HashMap map) {
        // read them out
        EmailFlowNode[] nodes = new EmailFlowNode[map.size()];

        int index = 0;
        // read the data in the hashtable
        Set entries = map.entrySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = (Object) entry.getKey();
            nodes[index] = (EmailFlowNode) entry.getValue();
            index++;
        }

        return nodes;
    }

    public final void displayNullFlow() {
        points.clear();
        lines.clear();
        lastNode = null;
        onArc = new Vector();
    }

    public final void displayFlow(EmailFlowNode[] nodes, String head) {
        points.clear();
        lines.clear();
        lastNode = null;
        animateNodes = new Vector();
        animateIndex = -1;
        // clickedAccount = new Vector();
        onArc = new Vector();

        // EmailFlowNode[] nodes = getMap(table);

        // build graphics
        double centerx = (double) width / 2;
        double centery = (double) height / 2;
        points.put(head, new EmailFlowNode(7, (int) centerx, (int) centery, Color.black, head));

        int size = nodes.length;
        int numOfFlow = getNumOfFlow(nodes);
        // int numOfFlow = size;
        NumOfScaleCircle = numOfFlow;
        double radiusInterval = (double) ((height - (2 * base)) / numOfFlow) / 2;
        double radius = radiusInterval;
        double radiumStartInterval = (2.0 * Math.PI) / ((double) (size));

        // assign location
        clonedTable = new EmailFlowNode[size];
        for (int i = 0; i < size; i++) {
            clonedTable[i] = nodes[i];
            boolean newuser = false;

            String sender = nodes[i].sender;
            int rcptsize = nodes[i].rcpt.size();
            double radiumInterval = (2.0 * Math.PI) / ((double) rcptsize + 1);
            if (rcptsize == 1)
                radiumInterval = Math.PI;
            double radium = (double) i * radiumStartInterval;

            // the sender, the point

            // animation
            EmailFlowNode aninode = null;

            if (!points.containsKey(sender)) {
                int x = (int) (centerx + (radius * Math.cos(radium)));
                int y = (int) (centery + (radius * Math.sin(radium)));
                points.put(sender, new EmailFlowNode(7, x, y, getColor(i), sender));
                radium += radiumInterval;
                newuser = true;

                // animation
                aninode = new EmailFlowNode(10, x, y, Color.red, sender);
            } else {// animation
                EmailFlowNode tmpn = (EmailFlowNode) points.get(sender);
                aninode = new EmailFlowNode(10, tmpn.x, tmpn.y, Color.red, sender);
            }
            // recipients and lines
            for (int j = 0; j < rcptsize; j++) {
                String rcpt = (String) nodes[i].rcpt.get(j);
                // points
                if (!points.containsKey(rcpt)) {
                    int x = (int) (centerx + (radius * Math.cos(radium)));
                    int y = (int) (centery + (radius * Math.sin(radium)));
                    points.put(rcpt, new EmailFlowNode(7, x, y, getColor(i), rcpt));
                    radium += radiumInterval;
                    newuser = true;

                    EmailFlowNode tmpnode;
                    // animation
                    if (rcpt.equals(sender))
                        tmpnode = new EmailFlowNode(10, x, y, Color.red, rcpt);
                    else
                        tmpnode = new EmailFlowNode(10, x, y, Color.green, rcpt);
                    aninode.rcptNodes.add(tmpnode);
                } else {// animation
                    EmailFlowNode tmpn = (EmailFlowNode) points.get(rcpt);
                    EmailFlowNode tmpn2 = new EmailFlowNode(10, tmpn.x, tmpn.y, Color.green, rcpt);
                    aninode.rcptNodes.add(tmpn2);
                }

                // lines
                if (!lines.containsKey(sender + " " + rcpt) && !lines.containsKey(rcpt + " " + sender)) {
                    EmailFlowNode p1 = (EmailFlowNode) points.get(sender);
                    EmailFlowNode p2 = (EmailFlowNode) points.get(rcpt);
                    lines.put(sender + " " + rcpt, new EmailFlowNode(p1.x, p1.y, p2.x, p2.y, Color.orange));

                    // animation
                    aninode.lines.add(new EmailFlowNode(p1.x, p1.y, p2.x, p2.y, Color.magenta));
                } else if (lines.containsKey(rcpt + " " + sender)) {
                    EmailFlowNode line = (EmailFlowNode) lines.remove(rcpt + " " + sender);
                    line.color = Color.cyan;
                    line.weight++;
                    lines.put(rcpt + " " + sender, line);

                    // animation
                    aninode.lines.add(new EmailFlowNode(line.x1, line.y1, line.x2, line.y2, Color.magenta));
                } else {
                    EmailFlowNode line = (EmailFlowNode) lines.remove(sender + " " + rcpt);
                    line.weight++;
                    // lines.put(rcpt+" "+sender, line);
                    lines.put(sender + " " + rcpt, line);

                    // animation
                    aninode.lines.add(new EmailFlowNode(line.x1, line.y1, line.x2, line.y2, Color.magenta));
                }

            }// for(int i=0;i<size;i++)

            // for animation
            animateNodes.add(aninode);
            if (newuser)
                radius += radiusInterval;
        }// for(int i=0;i<size;i++){

        pointsArray = getMap(points);
        linesArray = getMap(lines);
    }

    // reduce some empty flow
    public final int getNumOfFlow(EmailFlowNode[] nodes) {
        int size = 0;
        HashMap testtable = new HashMap();
        for (int i = 0; i < nodes.length; i++) {
            boolean newuser = false;
            String s = nodes[i].sender;
            if (!testtable.containsKey(s)) {
                testtable.put(s, s);
                newuser = true;
            }

            for (int j = 0; j < nodes[i].rcpt.size(); j++) {
                String r = (String) nodes[i].rcpt.get(j);
                if (!testtable.containsKey(r)) {
                    testtable.put(r, r);
                    newuser = true;
                }
            }

            if (newuser)
                size++;
        }

        return size;
    }

    public final void drawPoints(Graphics g) {
        // EmailFlowNode[] nodes = getMap(points);

        for (int i = 0; i < pointsArray.length; i++) {
            fillCircle(g, pointsArray[i].x, pointsArray[i].y, pointsArray[i].size, pointsArray[i].color);
        }
    }

    public final void drawLines(Graphics g) {
        // EmailFlowNode[] nodes = getMap(lines);
        for (int i = 0; i < linesArray.length; i++) {
            g.setColor(linesArray[i].color);
            g.drawLine(linesArray[i].x1, linesArray[i].y1, linesArray[i].x2, linesArray[i].y2);
        }
    }

    public final void drawScaleCircle(Graphics g) {
        double radiusInterval = (double) ((height - (2 * base)) / NumOfScaleCircle) / 2;
        double radius = 0;
        for (int i = 0; i < NumOfScaleCircle; i++) {
            radius += radiusInterval;
            int x = width / 2;
            int y = height / 2;
            drawCircle(g, x, y, (int) radius * 2, Color.gray);
        }
    }

    public final void drawCircle(Graphics g, int x, int y, int size, Color c) {
        int rx = x - size / 2;
        int ry = y - size / 2;
        g.setColor(c);
        g.drawOval(rx, ry, size, size);
    }

    public final void fillCircle(Graphics g, int x, int y, int size, Color c) {
        int rx = x - size / 2;
        int ry = y - size / 2;
        g.setColor(c);
        g.fillOval(rx, ry, size, size);
    }

    public final Color getColor(int i) {
        if (i % 7 == 0)
            return Color.blue;
        else if (i % 7 == 1)
            return Color.green;
        else if (i % 7 == 2)
            return Color.magenta;
        else if (i % 7 == 3)
            return Color.gray;
        else if (i % 7 == 4)
            return Color.pink;
        else if (i % 7 == 5)
            return Color.yellow;
        else
            return Color.red;
    }

    public final EmailFlowNode checkNode(int x, int y) {

        // int selected = -1;

        EmailFlowNode selected = null;
        EmailFlowNode[] nodes = getMap(points);
        for (int i = 0; i < nodes.length; i++) {
            if (x >= nodes[i].x - nodes[i].size / 2 && x <= nodes[i].x + nodes[i].size / 2
                    && y >= nodes[i].y - nodes[i].size / 2 && y <= nodes[i].y + nodes[i].size / 2) {
                // selected = i;
                selected = nodes[i];
                return selected;
                // break;
            }
        }

        return null;
    }

    // public void mouseMoved(MouseEvent evt){ }
    // public void mouseDragged(MouseEvent evt){ }
    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public final void animation(Graphics g, int x, int y, int size, Color c) {

    }
}
