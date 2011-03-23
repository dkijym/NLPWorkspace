
/** gui extension by shlomo
 * added tree and message views.

*/


package metdemo.CliqueTools;

import metdemo.winGui;
import metdemo.CliqueTools.CliqueFinder.AccountPoint;
import metdemo.CliqueTools.CliqueFinder.oneClique;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.AlternateColorTableRowsRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.Utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.sql.SQLException;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/** Class to display the cliques in text format with differnet parameters. 
 * adaptions by Shlomo
 *
 */
 

public class EnclaveCliques extends JPanel
{
    private CliqueFinder m_cliqueFinder;
    private JTextArea m_textArea;
    private JTextField m_textMinMsgs;
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private JLabel m_count;
    private JComboBox attach_type;
    private EMTDatabaseConnection jdbcView = null;
    private JTree g_tree;//gloabel tree
    private JScrollPane textScroll;
    private JPanel panel;
    //private String m_sql;
    private JTable m_messages;
    private DefaultTableModel m_messages_model;
   // private winGui m_winGui;
    private DefaultTableCellRenderer rowColorRenderer;
    
    public EnclaveCliques(winGui wg,EMTDatabaseConnection jdbc, String aliasFilename,String[]attachments) throws Exception
    {
	//m_winGui = wg;
    jdbcView = jdbc;
	m_cliqueFinder = new CliqueFinder(jdbc, aliasFilename);

	panel = this;

	panel.setLayout(new BorderLayout());

	JPanel butPanel = new JPanel();
	butPanel.setLayout(new FlowLayout());
	
	JPanel butPanel2 = new JPanel();
	butPanel2.setLayout(new FlowLayout());
	
	JPanel northPanel = new JPanel();
	northPanel.setLayout(new GridLayout(2,1));
    
	butPanel.add(new JLabel("Type of Attachment"));
	attach_type = new JComboBox();
	
	ComboBoxModel model = new DefaultComboBoxModel(attachments);
	attach_type = new JComboBox(model);
	attach_type.insertItemAt(new String("All types"),0);
	attach_type.insertItemAt(new String("All text types"),1);
	attach_type.setToolTipText("Choose type of email attachmant to analyze in message window");
	butPanel.add(attach_type);
	
	attach_type.setSelectedIndex(0);
	m_textMinMsgs = new JTextField("3", 3);
	butPanel.add(new JLabel("Min # Messages"));
    

	butPanel.add(m_textMinMsgs);
        m_textMinMsgs.setToolTipText("Min # of messages exchanged pairwise");
	JButton butRefresh = new JButton("Refresh");
	butRefresh.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try
			{
			    //clear refresh list
   			    m_messages_model.setRowCount(0);

			    String types = new String();
			    int minMessages = attach_type.getSelectedIndex();
			    if( minMessages ==0)
				types  = "";
			    else if(minMessages ==1)
				types = "text/%";
			    else
				types = (String)attach_type.getSelectedItem();


			    minMessages = Integer.parseInt(m_textMinMsgs.getText());
	    
			    Refresh(minMessages, 3, types);
			}
		    catch (NumberFormatException nfe)
			{
			    JOptionPane.showMessageDialog(EnclaveCliques.this, m_textMinMsgs.getText() + " is not a valid number");
			}
		}
	    });
	butPanel2.add(butRefresh);
	
	JButton statisticsb = new JButton("Get Statistical Info.");
	statisticsb.setBackground(new Color(204,0,153));
	statisticsb.setToolTipText("click to get the statistical information of sending email");
	statisticsb.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    showStatistics();
		}
	    });
	butPanel.add(statisticsb);
	
	//help button
	EMTHelp butHelp = new EMTHelp(EMTHelp.ENCLAVE);

	butPanel2.add(butHelp);
	//print button
	JButton butPrint = new JButton("Copy to Clipboard");
	butPrint.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    try
			{
	   
			    StringSelection data = new StringSelection(m_textArea.getText());
			    clipboard.setContents (data, data);
	
			    //RULEZ
			}
		    catch (Exception fe)
			{
			    //	    JOptionPane.showMessageDialog(Cliques.this, m_textMinMsgs.getText() + " is not a valid number");
			}
		}
	    });
	butPanel2.add(butPrint);
	//show number of cliques
	m_count = new JLabel("");
	m_count.setForeground(Color.blue);
	butPanel.add(m_count);

	northPanel.add(butPanel);
	northPanel.add(butPanel2);

	panel.add(northPanel, BorderLayout.NORTH);

	m_textArea = new JTextArea("Press Refresh to Generate Cliques");
	m_textArea.setEditable(false);

	//setup the jtree
	DefaultMutableTreeNode top = new DefaultMutableTreeNode("Email Cliques");
	
	g_tree = new JTree(top);
	//end tree
	g_tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
	


	rowColorRenderer = new AlternateColorTableRowsRenderer();
	


	//add table in south
	String [] columns = {"Ref","From", "To","Subject", "# Rcpt", "# Attach", "Date","Time" };
	m_messages_model = new DefaultTableModel(columns,0){
		public boolean isCellEditable(int row, int column){return false;} 
	    };
	m_messages = new JTable(m_messages_model){
		public TableCellRenderer getCellRenderer(int row, int column) 
		{
		    return rowColorRenderer;//will color each row 
		}
		
	    }; 
	m_messages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
	m_messages.addMouseListener(new MouseListener(){
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
    
		public final void mouseClicked(MouseEvent e)
		{
		    java.awt.Point point = e.getPoint();
		    int row = m_messages.rowAtPoint(point);

		    if (e.getClickCount() == 2)
			{
			    //RULEZ
			    String mailref = (String) m_messages.getValueAt(row,0);
			    String detail = "Mailref: " + mailref+ "\n";
			    detail += "Sender: " + ((String) m_messages.getValueAt(row,1 )) + "\n";
			    detail += "Date: " + (String) m_messages.getValueAt(row,6 ) + "\n";
			    detail += "Subject: " + (String) m_messages.getValueAt(row,3 ) + "\n";
			    Utils.popBody(EnclaveCliques.this,detail,mailref,jdbcView,true);
			    /*
			      String data[][];
			      //double click
			      String ref = (String)m_messages.getValueAt(row,0);
			      //System.out.println("mailref: " + ref);
			      // "Ref","From", "To","Subject", "# Rcpt", "# Attach", "Size", "Date","Time" };
			      String detail = "Mailref: " + ref + "\n";
			      detail += "Sender: " + ((String) m_messages.getValueAt(row,1 )) + "\n";
			      detail += "Date: " + (String) m_messages.getValueAt(row,7 ) + "\n";
			      detail += "Subject: " + (String) m_messages.getValueAt(row,3 ) + "\n";
			      try{
			      // get received headers, attachments
			      synchronized (m_jdbcView)
			      {
			      m_jdbcView.getSqlData("select hash,body,type,filename from message where mailref=\"" + ref + "\"");
			      data = m_jdbcView.getRowData();
			      }
			    	    			
			      for (int i=0; i<data.length; i++)
			      {
			      if ((data[i][1] != null) && (data[i][1].length() > 0))
			      {
			      detail += "Filename: " + data[i][3]+"\n";
			      detail += "Attachment Type: "  +data[i][2] +"\n";
			      detail += "Attachment: " + data[i][0] + "\n";
			      }
			      detail += "*****BODY - " + i  + "**********\n" + data[i][1]+"\n*****END BODY************\n";

			      }
	
			      }catch(SQLException ty){System.out.println(ty);}





			      DetailDialog dialog = new DetailDialog(null, false, ref+" Email", detail);
			      dialog.setLocationRelativeTo(Attachments.this);
			      dialog.show();
			    */
			}
		}});
				
	//m_messages.setPreferredSize(new Dimension(400,150));
	JScrollPane messages_scroll = new JScrollPane(m_messages);
	messages_scroll.setPreferredSize(new Dimension(400,150));
	

	panel.add(messages_scroll, BorderLayout.SOUTH);


	//JScrollPane textScroll = new JScrollPane(m_textArea);
	textScroll = new JScrollPane(g_tree);

	panel.add(textScroll, BorderLayout.CENTER);


	//setViewportView(panel);
    }

    /**
     * Call to generate cliques based on some set of parameters.
     *@param minMessages the min amount of traded messages
     *@param minSubjectWordLength is the min amount of subject words in common
     *@param types the type of attachment to consider
     */

    public final void Refresh(final int minMessages, final int minSubjectWordLength,final String types)
    {
	
	BusyWindow bw = new BusyWindow("Clique Finder" ,"Progress",true);
	try
	    {
	
		bw.pack();
		bw.setVisible(true);
		String qry = new String();
		if(types.length()<1)
		    qry = "select sender, rcpt, subject from email";
		else
		    qry = "select sender, rcpt, subject from email left join message on email.mailref = message.mailref where message.type like '"+types+"'";

		HashMap<String,HashMap> mapped = m_cliqueFinder.buildGraph(minMessages, minSubjectWordLength,qry);
		ArrayList vCliques = m_cliqueFinder.findCliques(mapped,false);
      
		//String str = new String();
		int max = vCliques.size();
		bw.setMax(max);
		
		for (int i=0; i<max; i++)
		    {
			
			bw.progress(i);
			//str += vCliques.get(i) + "\n";
			//System.out.println(i+" " +vCliques.get(i));
		    }
		m_count.setText("Number of Cliques: " +max);
		
		//added by shlomo
		/*
		  str += "\n\n\n=====Violations=====";

		  String str2 = new String();
		  Vector violations = m_cliqueFinder.findCliqueViolations();
		  Enumeration enum = violations.elements();
		  int i=0;
		  while (enum.hasMoreElements())
		  {
		  String t =(String) enum.nextElement()+"\n"; 
		  System.out.print((i++) + " "+ t);
		  str2 +=  t;
		  }
		*/

		//need to set up the tree called g_tree
		//for (int i=0; i<max; i++)
		// {
		//			str += vCliques.get(i) + "\n";
		//  }
		//setup the jtree
		String str2 = new String();
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Email Cliques Groups");
		for(int i=0;i<max;i++)
		    {
			//one clqieu is defined in cliquefinder class
			//top.add(new DefaultMutableTreeNode(new CliqueNode((oneClique)vCliques.get(i))));
			oneClique v = (oneClique)vCliques.get(i);
			ArrayList vec = v.getSubjectWords();
			str2 = new String("");
			for (int j=0; j<8 && j<vec.size(); j++)
			    {
				if (j != 0)
				    {
					str2 += ", ";
				    }
				
				str2 += vec.get(j);
			    }
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(str2);
			top.add(group);
		
			Iterator enumWalker = v.getPoints().iterator();
			while (enumWalker.hasNext())
			    {
				AccountPoint p = (AccountPoint) enumWalker.next();
				group.add(new DefaultMutableTreeNode(p.getAccount()));
			    }
		    }
	
		g_tree = new JTree(top);
		//end tree
		g_tree.getSelectionModel().setSelectionMode
		    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	
		g_tree.addTreeSelectionListener(new TreeSelectionListener()
		    {
			public void valueChanged(TreeSelectionEvent e) {
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				g_tree.getLastSelectedPathComponent();

			    if (node == null) 
				return;
		    
			    //Object nodeInfo = node.getUserObject();
			    //check if there are any siblings.
			    else if(node.getSiblingCount() ==1 || !node.isLeaf())
				return;

			    String query = new String("select distinct email.mailref,sender,rcpt,subject,numrcpt,numattach,dates,times from email left join message on email.mailref=message.mailref where sender = '"+node.getUserObject()+"' and rcpt in (" );


			    //DefaultMutableTreeNode node2;
			    TreeNode p= node.getParent();
			    //Enumeration enum = p.children();
			    int ignore = p.getIndex(node);
		   
			    int count=0;
			    for(int i=0;i<p.getChildCount();i++)
				{   
				    if(i!=ignore)
					{
					    if(count>0)
						query+=",";
					
					    query += "'"+ p.getChildAt(i)+"'"; 
					    count++;
					}
				}
			    query +=") ";
			    //add type of attach
			    
			    int minMessages = attach_type.getSelectedIndex();
			    if( minMessages ==0)
				{}
			    else if(minMessages ==1)
				query += " and message.type like 'text/%'";
			    else
				query += " and message.type like '"+(String)attach_type.getSelectedItem()+"'";
			    //end

//			    query += " group by email.mailref,sender,rcpt,subject,numrcpt,numattach,dates,times ";
			    String data[][]=null;
			    //clear the table
			    m_messages_model.setRowCount(0);
			    try{
				synchronized (jdbcView)
				    {
	//				System.out.println(query);
					data = jdbcView.getSQLData(query);
					}

				for (int i=0; i<data.length; i++)
				    {
					m_messages_model.addRow(data[i]);
					//sorter.reallocateIndexes();
				    }
			    }
			    catch(SQLException sep){System.out.println(sep);}





			    //String cl = (String)nodeInfo;
			    //System.out.println(cl);

			    //displayURL(book.bookURL);
			    //} 
			    //else {
			    //	displayURL(helpURL); 
			    //}
			}
		    });

	
		panel.remove(panel.getComponentCount()-1);
		textScroll = new JScrollPane(g_tree);
		panel.add(textScroll, BorderLayout.CENTER);
	
	


		//??m_textArea.setText(str);
		//??m_textArea.setCaretPosition(0);
	    }
	catch (Exception ex)
	    {
		JOptionPane.showMessageDialog(this, "Could not get cliques: " + ex);
	    }

	bw.setVisible(false);
	bw = null;

    }
    
    
    public void showStatistics(){
	double[] distri = countConnect();
	if(distri!=null && distri.length>0){
	    int above = 0;
	    int thr = Integer.parseInt(m_textMinMsgs.getText());
	    int max = (int)Math.round(distri[0]);
	    for(int i=0;i<distri.length;i++){
		int amount = (int)Math.round(distri[i]);
		if(max<amount)max = amount;
		if(amount>thr)above++;
	    }

	    double[] refined = refineCurve(distri, max);
	    double[] avg_std = getAvgStd(distri);
	    String s1 = String.valueOf(avg_std[0]);
	    String s2 = String.valueOf(avg_std[1]);
	    if(s1.indexOf(".")!=-1){
		if(s1.length()>s1.indexOf(".")+4)
		    s1 = s1.substring(0,s1.indexOf(".")+4);
	    }
	    if(s2.indexOf(".")!=-1){
		if(s2.length()>s2.indexOf(".")+4)
		    s2 = s2.substring(0,s2.indexOf(".")+4);
	    }
	    
	    String msg1 = "Average Connection: " + s1;
	    String msg2 = "Standard Deviation: " + s2;
	    String msg3 = "# of the Maximum connection of a single sender: "+ String.valueOf(max);
	    String msg4 = "# of senders that have more connections than the current threshold: "+above;
		

	    VCliqueInfo info = new VCliqueInfo(msg1, msg2, msg3, msg4, this);
	    info.setFigure("Email Sending Distribution"
			   ,"Sender Distribution"
			   ,"# of Emails"
			   ,info.distribution
			   ,Color.blue
			   ,refined);

	    info.setVisible(true);
	}
	else{
	    JOptionPane.showMessageDialog(this,
					  "no email data here.\n",
					  "Message",
					  JOptionPane.INFORMATION_MESSAGE);
	}
    }

    //change the style of curve to a better form
    public double[] refineCurve(double[] array, int max){
	int length = array.length;
	

	double[] result = new double[max+1];
	for(int i=0;i<length;i++){
	    int amount = (int)Math.round(array[i]);
	    result[amount]++;
	}

	return result;
    }

    public double[] countConnect(){
    String query = "select sender,dates,count(sender) from email group by sender,dates order by sender";

	String[][] data = runSql(query);

	//get the average of each single sender
	String currSender = "";
	double currCnt = 0.0;
	double singleCnt = 1.0;
	Vector cntArray = new Vector();
	for(int i=0;i<data.length;i++){
	    if(i==0){
		currSender = data[i][0];
		currCnt = Double.parseDouble(data[i][2]);
	    }
	    else if(currSender.equals(data[i][0])){
		currCnt += Double.parseDouble(data[i][2]);
		singleCnt += 1;
	    }
	    else{
		cntArray.add(String.valueOf(currCnt/singleCnt));
		currCnt = Double.parseDouble(data[i][2]);
		currSender = data[i][0];
		singleCnt = 1.0;
	    }

	    if(i==data.length-1)
		cntArray.add(String.valueOf(currCnt));
	}
	
	//get the average of all the senders
	int length = cntArray.size();
	double[] element = new double[length];
	for(int i=0;i<length;i++){
	    element[i] = Double.parseDouble((String)cntArray.get(i));
	}

	return element;
	
    }

    public double[] getAvgStd(double[] element){

	int length = element.length;
	double total = 0.0;
	for(int i=0;i<length;i++){
	    total += element[i];
	}
	double avg = total / length;

	double var = 0.0;
	for(int i=0;i<length;i++){
	    var += (element[i]-avg) * (element[i]-avg);
	}
	
	double std = Math.sqrt(var/length);
	
	double[] avg_std = {avg, std};

	return avg_std;
    }

    public String[][] runSql(String sql) {
        String[][] data = null;
        try {
            synchronized (jdbcView) {
                data = jdbcView.getSQLData(sql);
              
            }
        } catch (SQLException ex) {
            if(!ex.toString().equals("java.sql.SQLException: ResultSet is fromUPDATE. No Data")) {
                System.out.println("ex: "+ex);
                System.out.println("sql: " +sql);
	    }
        }
        return data;
    }
    /*
      private class CliqueNode{

      private String Subject;
      private String Users;

      public CliqueNode(String s,String u)
      {

      Subject = new String(s);
      Users = new String(u);
      }

      public CliqueNode(oneClique v)
      {
      Subject = (String)((Vector)v.getSubjectWords()).toString();
	    

      Enumeration enum = v.getPoints().elements();
      while (enum.hasMoreElements())
      {
      Point p = (Point) enum.nextElement();
      Users += p.getAccount() + "\n";
      }
      }


      public String toString()
      {
      return Subject;

      }



      }//end clique node
    */





}
