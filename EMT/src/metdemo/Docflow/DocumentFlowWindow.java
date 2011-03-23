/**
 * @(#)DocumentFlowWindow.java
 *
 *
 * @author Wei-Jen Li
 * @version 1.00 2007/3/23
 */
package metdemo.Docflow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.*;

import javax.swing.*;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import java.util.*;
import java.sql.SQLException;

import metdemo.winGui;
import metdemo.Tools.EMTHelp;
import metdemo.DataBase.EMTDatabaseConnection;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.DateFormat;


/**
 * @author shlomo
 *
 */
public class DocumentFlowWindow extends JScrollPane {

	private EMTDatabaseConnection dbHandle;
	private winGui winguiHandle;
	
	/** first panel to select specific user/type/etc */
	private JPanel jp_firstWindow = null;
	private JTextArea userNameListArea;	//collection of users seperated by \n....
	private ArrayList<String> userNameList; //it won't grow big, arraylist is ok
	private JComboBox attach_type;	//type of attachment we want, note 0,1 if chosen are special cases % or text/%
	private JRadioButton exclusiveButton;//choose if only interested in something exchanged between spepcific users sets
	private JRadioButton startButton;
	
	//weijen stuff
	//currently not used
	private JPanel jp_secondWindow = null;
	
	private JTabbedPane tabbedPane;
	private JPanel configP;
    private JScrollPane configSP;
    private JPanel displayP;
    private JScrollPane displaySP;
    
    private DocflowDisplay docflow;
	
	
	/**
	 * Constructor of class
	 * @param emtdb
	 * @param wingH
	 */
	public DocumentFlowWindow(EMTDatabaseConnection emtdb, winGui wingH){
		dbHandle = emtdb;
		winguiHandle = wingH;
		
		
		userNameListArea = new JTextArea(5,20);
		userNameListArea.setEnabled(false);
		userNameList = new ArrayList<String>();//will help in adding to user list
		
		tabbedPane = new javax.swing.JTabbedPane();
		configSP = new JScrollPane();
		displaySP = new JScrollPane();
		
		configSP.setViewportView(setupFirstWindow());
		displaySP.setViewportView(setupSecondWindow());
		tabbedPane.addTab("Config", configSP);
		tabbedPane.addTab("Display Flow", displaySP);
		
		setViewportView(tabbedPane);

		docflow = new DocflowDisplay(this);
		
		
	}
	
	public JPanel setupFirstWindow(){

		JPanel panelToSetup = new JPanel();
		//set up the layout to be gridbaglayout
		GridBagConstraints GBconstraints = new GridBagConstraints();
		GridBagLayout GBLayout = new GridBagLayout();
		
		panelToSetup.setLayout(GBLayout);
		
		//now to setup user names on top
		JLabel userNameLabel = new JLabel("Users:");
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 0;
		GBconstraints.gridwidth = 1;
		GBconstraints.insets = new Insets(5,5,5,5);
		GBLayout.setConstraints(userNameLabel, GBconstraints);
		panelToSetup.add(userNameLabel);

		//add user list to the pane
		JScrollPane userListpane = new JScrollPane(userNameListArea);
		
		GBconstraints.gridx = 1;
		GBconstraints.gridy = 0;
		//GBconstraints.weighty =2;
		GBconstraints.gridwidth = 2;
		GBLayout.setConstraints(userListpane, GBconstraints);
		panelToSetup.add(userListpane);
		
		//add user from above
		JButton addUserAbove = new JButton("Add above User");
		GBconstraints.gridx = 3;
		GBconstraints.gridy = 0;
		//GBconstraints.weighty =1;
		GBconstraints.gridwidth = 1;
		GBLayout.setConstraints(addUserAbove, GBconstraints);
		panelToSetup.add(addUserAbove);
		addUserAbove.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent arg0) {
				addUser((String)winguiHandle.getSelectedUser());		
			}
		
		});
		
		
		//clear button
		JButton clearUserButton = new JButton("Clear Users");
		GBconstraints.gridx = 4;
		GBconstraints.gridy = 0;
		//GBconstraints.weighty =1;
		GBconstraints.gridwidth = 1;
		GBLayout.setConstraints(clearUserButton, GBconstraints);
		panelToSetup.add(clearUserButton);
		clearUserButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent arg0) {
				userNameList.clear();
				userNameListArea.setText("");
			}
		
		});
		
		
		//next line type of document to analyze
		JLabel typeLabel = new JLabel("Type of Communication:");
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 1;
		GBconstraints.gridwidth = 1;
		
		GBLayout.setConstraints(typeLabel, GBconstraints);
		panelToSetup.add(typeLabel);
		
		//drop down list of stuff:
		
		ComboBoxModel model2 = new DefaultComboBoxModel(winguiHandle.getAttachments());
		attach_type = new JComboBox(model2);
		attach_type.insertItemAt(new String("All Attachment types"),0);
		attach_type.insertItemAt(new String("All text types"),1);
		attach_type.setToolTipText("Choose type of email attachmant to analyze in window");
		attach_type.setSelectedIndex(0);
		//sndBox.add(Box.createRigidArea(new Dimension(40, 10)));
		
		GBconstraints.gridx = 1;
		GBconstraints.gridy = 1;
		GBconstraints.gridwidth = 2;
		
		GBLayout.setConstraints(attach_type, GBconstraints);
		panelToSetup.add(attach_type);
		
		//next row: which direction is the communication in...
		JLabel dirLabel = new JLabel("Direction of Communication:");
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 2;
		GBconstraints.gridwidth = 1;
		
		GBLayout.setConstraints(dirLabel, GBconstraints);
		panelToSetup.add(dirLabel);
		
		exclusiveButton = new JRadioButton("Only Between These Users",true);
		startButton = new JRadioButton("Any Communication involving these users",false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(exclusiveButton);
		bg.add(startButton);
		GBconstraints.gridx = 1;
		GBconstraints.gridy = 2;
		GBconstraints.gridwidth = 2;
		
		GBLayout.setConstraints(exclusiveButton, GBconstraints);
		panelToSetup.add(exclusiveButton);
		
		GBconstraints.gridx = 3;
		GBconstraints.gridy = 2;
		GBconstraints.gridwidth = 2;
		
		GBLayout.setConstraints(startButton, GBconstraints);
		panelToSetup.add(startButton);
		
		//finally button to analyze everything
		JButton previewAnalysis = new JButton("Preview Analysis");
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 3;
		GBconstraints.gridwidth = 1;
		GBLayout.setConstraints(previewAnalysis, GBconstraints);
		panelToSetup.add(previewAnalysis);
	
		//weijen need to add code to get rough count
		previewAnalysis.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent arg0) {
				//call something here
				showEmailflow();
			}
		
		});
		
		
		
		JButton startAnalysis = new JButton("Analyze this!");
		GBconstraints.gridx = 4;
		GBconstraints.gridy = 3;
		GBconstraints.gridwidth = 1;
		GBLayout.setConstraints(startAnalysis, GBconstraints);
		panelToSetup.add(startAnalysis);
		
		startAnalysis.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent arg0) {
				
				
//				jp_secondWindow = setupSecondWindow(jp_secondWindow);
//				remove(jp_firstWindow);
//				add(jp_secondWindow,BorderLayout.CENTER);
//				repaint();
//				validate();
			}
		
		});
		
		EMTHelp emthelp = new EMTHelp(EMTHelp.DOCFLOW);
		GBconstraints.gridx = 5;
		GBconstraints.gridy = 3;
		GBconstraints.gridwidth = 1;
		GBLayout.setConstraints(emthelp, GBconstraints);
		panelToSetup.add(emthelp);
		
		return panelToSetup;
	}
	
	
	public JPanel setupSecondWindow(){
//		if(panelToSetup == null){
//			panelToSetup = new JPanel();
//		}
//		else{
//			panelToSetup.removeAll(); //empty it
//		}

		JPanel panelToSetup	= new JPanel();
		//etup layout
		GridBagConstraints GBconstraints = new GridBagConstraints();
		GridBagLayout GBLayout = new GridBagLayout();
		
		panelToSetup.setLayout(GBLayout);
		
		//button to return to first window
		JButton resartButton = new JButton("Restart!");
		GBconstraints.gridx = 0;
		GBconstraints.gridy = 0;
		GBconstraints.gridwidth = 1;
		GBconstraints.insets = new Insets(5,5,5,5);
		GBLayout.setConstraints(resartButton, GBconstraints);
		panelToSetup.add(resartButton);
		
		resartButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent arg0) {
				
				
//				jp_firstWindow = setupFirstWindow(jp_firstWindow);
//				remove(jp_secondWindow);
//				add(jp_firstWindow,BorderLayout.CENTER);
//				repaint();
//				validate();
			}
		
		});
		
		
		return panelToSetup;
	}
	
	
	
	
	
	/**
	 * 
	 * @param username
	 */
	public void addUser(String username){
		if(!userNameList.contains(username)){
			userNameList.add(username);
			userNameListArea.append(username+"\n");
		}
	}
	
	
	
	/**
	 * called from winGui, see there for how/who/why
	 * can disable if you want
	 */
	public void Execute(){
		
	}
	
	/**
	 * Called from the wingui class when going back to the class
	 *
	 */
	public void Refresh(){
		
	}
	
	/**
	 *	call the database
	 */
	public String[][] fetchDB(String query){
	
		String data[][]= null;
		try{
			return this.dbHandle.getSQLData(query);
		}catch(SQLException ee){
			System.out.println(ee);
			return null;
		}
	}
	
	/**
	 *	call the database
	 */
	public byte[] fetchBinaryDB(String query){
	
		
		try{
			byte[][] tempb = this.dbHandle.getBinarySQLData(query);
			ArrayList<byte[]> byteseq = new ArrayList<byte[]>();
			int len = 0;
			for(byte[] b:tempb){
				byteseq.add(b);
				len = b.length;
			}
				
			byte[] array = new byte[len*byteseq.size()];
			for(int i=0;i<byteseq.size();++i){
				byte[] b = byteseq.get(i);
				if(b.length != len)System.out.println("fetch error: len:"+len+" b.len:"+b.length);
				System.arraycopy(b, 0, array, i*len, len);
			}
			return array;
		}catch(SQLException ee){
			System.out.println(ee);
			return null;
		}
		
	}
	
	/**
	 *	display the emailflow
	 */
	public void showEmailflow(){
		//setup the flow(s) using a hashtable
		Hashtable eflow = getFlow();
		//Hashtable<String, OneFlow> eflow = null;
		
		//display it
		//the popup frame size
		int x = 800;
		int y = 600;
		docflow.restart();
		docflow.setPanel(x, y, eflow);
			
	}
	
	/**
	 *	setup the emailflows and put them in a hashtable
	 */
	public Hashtable getFlow(){
		//key is the subject, each object is a "flow" which contains
		//many nodes. Each node is an email
		Hashtable<String, OneFlow> allFlows = new Hashtable<String, OneFlow>();
			
		try{
			//get the data needed from db
			//use the query twice for correct email information
			//first get the mailred
			String query = getFlowQuery1(exclusiveButton.isSelected());
			//System.out.println(query);
			String[][] mailref = fetchDB(query);
			
			//second get the email information
			query = getFlowQuery2(mailref);
			//System.out.println(query);
			String[][] data = fetchDB(query);
			
			//some index...
			int senderIndex = 0;
			int rcptIndex = 1;
			int datesIndex = 2;
			int timesIndex = 3;
			int mailrefIndex = 4;
			int subjectIndex = 5;
			int attachIndex = 6;
			
			//compute the max and min time
			long mint = getTime(data[0][datesIndex], data[0][timesIndex]);
			long maxt = getTime(data[data.length-1][datesIndex], data[data.length-1][timesIndex]);
			
			String lastref = "";
			DocflowNode currentNode = null;
			
			//read the data and put them in the hashtable
			for(String[] record: data){

				//a new email
				if(!lastref.equals(record[mailrefIndex])){
					
					//if there is last one add last one as a node
					if(currentNode != null){
						addNodeToHash(allFlows, record[subjectIndex], currentNode);
					}
					//(re)set this node
					currentNode = null;
					//long curr = format.parse(record[datesIndex]+","+record[timesIndex]).getTime();
					long curr = getTime(record[datesIndex], record[timesIndex]);
					currentNode = new DocflowNode(record[senderIndex]
													, record[rcptIndex] 
													, record[datesIndex] 
													, record[timesIndex]
													, record[mailrefIndex]
													//, record[attachIndex]
													, getTimeratio(maxt, mint, curr));
													
					//need to update the list of files...
					//some text/plain and text/html attachments are not real attachments
					//do you have any better way?
					if(Integer.parseInt(record[attachIndex]) > 0)
						currentNode.setNumatt(getAttachmentInfo(record[mailrefIndex]));
													
					lastref = record[mailrefIndex];
				}
				//the same email but different rcpt
				else{
					currentNode.rcpt.add(record[rcptIndex]);
				}
			}
			
			//add the last one
			//if there is last one add last one as a node
			if(currentNode != null){
				addNodeToHash(allFlows, data[data.length-1][subjectIndex], currentNode);
			}
		
		}catch(Exception e){
			System.out.println("getFlow:"+e);
		}
		
		return allFlows;
	}
	
	/**
	 *	Only get the mailref here
	 *
	 */
	public String getFlowQuery1(final boolean onlySelected){
		
		StringBuffer query = new StringBuffer("select mailref ");
		query.append("from email where ");
		
		query.append("( (");
		if(onlySelected){
			int size = userNameList.size();
			for(int i=0;i<size;++i){
				for(int j=0;j<size;++j){
					if(i!=j){
						query.append("(sender='"+userNameList.get(i)
							+"' and rcpt='"+userNameList.get(j)+"') ");
						if(i!=size-1)query.append("or ");
							
					}
				}
			}
			
		}
		else{
			int size = userNameList.size();
			for(int i=0;i<size;++i){
				String useraccount = userNameList.get(i);
				query.append("ender='"+useraccount+"' or rcpt='"+useraccount+"' ");
				if(i!=size-1)query.append("or ");
			}
		}
		
		//from the top data bar
		String[] maxmin = winguiHandle.getDateRange_toptoolbar();
		query.append(") and (dates>'" +maxmin[0]+"' and dates<'"+maxmin[1]+"') ");
		
		query.append(") ");
		query.append("order by dates,times");
		return query.toString();
	}
	

	
	/**
	 *	get the other information
	 *
	 */
	public String getFlowQuery2(String[][] mailref){
		StringBuffer query = new StringBuffer("select sender,rcpt,dates,times,mailref,subject,numattach ");
		query.append("from email where ");
		
		for(int i=0;i<mailref.length;++i){
			query.append("mailref='"+mailref[i][0]+"' ");
			if(i!=mailref.length-1)query.append("or ");
		}
		
		query.append("order by dates,times");
		return query.toString();
	}
	
	/**
	 *	get the real count of attachment
	 *
	 */
	public int getAttachmentInfo(String mailref){
		String query = "select filename,type from message where mailref='"+mailref+"'";
		String[][] data = fetchDB(query);
		int count = 0;
		
		if(data == null)return count;

		//get the filename and hashcode
		for(int i=0;i<data.length;++i){
			if(!data[i][1].equals("text/plain") || !data[i][1].equals("text/html")){
				if(!data[i][0].equals(""))++count;
			}
		}
		return count;
	}
	
	/**
	 *	add a flow to the hashtable
	 */
	public void addNodeToHash(Hashtable<String, OneFlow> table, String subject, DocflowNode currentNode){
		//find it's group, by using subject
		//check the hashtable
		//add it to the group
		if(!table.containsKey(subject)){
			//if not contains this flow, create a new one
			OneFlow aflow 
				= new OneFlow(subject, currentNode, currentNode.timeratio);
			table.put(subject, aflow);
		}
		else{
			//if contains, add a node
			OneFlow aflow = table.remove(subject);
			aflow.flow.add(currentNode);
			table.put(subject, aflow);
		}
	}
	
	/**
	 *	read dates and times and compute the time in milisecond
	 */
	public long getTime(String date, String time){
		try{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
			return format.parse(date+","+time).getTime();
		}catch(Exception e){
			System.out.println(e);
			return 0;
		}
		
	}
	
	/**
	 *	find where the current time related to the whole time frame
	 *	for ploting the location of the flow
	 */
	public double getTimeratio(long max, long min, long current){
		return ((double)(current-min)) / ((double)(max-min));
	}
	
	
	public void getAttachment(){
		String data2[][];
		boolean all = false;
		//if (attachmentType.getSelectedIndex() == 0) {
		if(all){
		
			//data2 = m_jdbcUpdate.getSQLData("select distinct hash from message");
            //, filename, type, size from message where hash is not
            // null order by hash asc");
        } 
        else{
			//data2 = m_jdbcUpdate.getSQLData("select distinct hash from message where type = '"
            	 //+ (String) attachmentType.getSelectedItem() + "'");

        }
        
//        if (m_attachment_model.getRowCount() > 0) {
//                m_attachment_model.clear();
//                sorter.reallocateIndexes();
//            }

//			PreparedStatement pshelper = m_jdbcUpdate
//                    .prepareStatementHelper("select distinct hash, filename, type, size from message where hash = ?");
//            System.out.println("going to loop for data len:"+ data2.length);
//            
//            if(data2.length > 300){
//                int n = JOptionPane.showConfirmDialog(AttachmentWindow.this,"Please note: you will be fetching : "+ data2.length + " records");
//             
//                if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.NO_OPTION){
//                    return;
//                }
//            
//            }
//            
//            
//            
//            for (int i = 0; i < data2.length; i++) {
//                //if (m_threadStop) return;
//                /*
//                 * if (data[i].length != 3) { throw new SQLException("Database
//                 * returned wrong number of columns"); }
//                 */
//                bw.progress(i, data2.length);
//
//                //change this to pipe
//
//                //data = m_jdbcUpdate.getSQLData("select distinct hash,
//                // filename, type, size from message where hash = '"+
//                // data2[i][0] +"'");
//                pshelper.setString(1, data2[i][0]);
//                ResultSet rs = pshelper.executeQuery();
//
//                if (rs.next())//data[0][0] != null)
//                {
//                    Attachment att = new Attachment(rs.getString(1), rs.getString(2), rs.getString(3), m_alerts,
//                            m_alertOptions);
//                    att.setSize(rs.getInt(4));
//                    
//                    m_attachment_model.addRow(att);//since we emptied it.
//                    rs.close();
//                }
//            }
	}
	
	
	//for testing
	public void checkData(String[][] data){
		
		if(data != null){
			for(String[] s:data){
				for(String ss:s){
					System.out.print(ss+",");
				}
				System.out.println();
			}
		}
		else{
			System.out.println("Null data");
		}
	}
	
	

}
