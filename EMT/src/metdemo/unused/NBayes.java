/*
Shlomo Hershkop
summer 2002
use of NBays from weka
*/



import weka.classifiers.*;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.sql.SQLException;
//import chapman.graphics.*;

public class NBayes extends JScrollPane implements MouseListener
{
    private JDBCConnect m_jdbcUpdate, m_jdbcView;
    private EmailFigures EmailFig;
    private Messages Message;
    JButton showItems;
    JButton goBuildModels;
    JButton goLoadModels;
    JButton goEvalModels;
    JButton goBuildTest;
    JButton goReport;
    JButton goUpdateDB;
    JTextField ModelNames;
    JTextArea Features;
    JTextField TrainNames;
    JTextField TestNames;
    JTextArea Training;   //sql statement to fetch train
    JTextField reportClass,reportThreshold;
    JTextArea Testing;   //sql statement to fetch test cases
    Vector V;
    JPanel panel;
    JTextArea trainCondition;
    //stuff to show traing file
    boolean trainwin;   //if window exists
    JFrame Twin;        //window to show training file
    JTextArea trainmsg;
    JComboBox comboBox;
    int LearnChoice =0;//default Naive Bayes
    char currentclass[];    //each examples current class in database
    char learnedclass[];   //each examples learned class
    double confidence[];   //confidence on such
    String subject[],mailref[];
    int examplesUID[];  //maybe long?
    int numExamples=0;  //keep track for last 4 guys
    MessageTableModel m_tableModel;
    JTable m_messages;
    String []colnames = {"Predict Class","Confidence","Class (in DB)","Mailref","Subject","Email UID"};
    Alerts m_alerts;
    JLabel m_status;
    private winGui m_winGui;
    double threshold = 0.5;

    //constructor
    public NBayes(JDBCConnect jdbcUpdate, JDBCConnect jdbcView, EmailFigures em, Messages ms, winGui winGui, Alerts alerts, JLabel status) throws SQLException
    {
	if((jdbcUpdate==null)||(jdbcView==null))
	    {
		throw new NullPointerException();
	    }
	m_winGui = winGui;
	m_alerts = alerts;
	m_status = status;
 
	//used from message class from brians work
	m_tableModel = new MessageTableModel(colnames, 0);
	m_messages = new JTable(m_tableModel);
	m_tableModel.addMouseListenerToHeaderInTable(m_messages);
	
	JScrollPane sPane = new JScrollPane(m_messages);
	sPane.setPreferredSize(new Dimension(500,380));
	m_messages.addMouseListener(this);
	
	m_jdbcUpdate = jdbcUpdate;
	m_jdbcView = jdbcView;
	EmailFig = em;
	Message = ms;
	EmailFig.setNB(this);
	//m_status.setText("NBayes: Learning is busy digging up the records");
	//init the variables
	//	System.out.print("\nTest of V cnvert: " + vecToStr(V));

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	JPanel main = new JPanel();//new JScrollPane();
	main.setLayout(gridbag);
	JPanel model2 = new JPanel();
	JLabel model = new JLabel("Name of Model File:");
	
	Training = new JTextArea(5,45);
       	Features = new JTextArea(5,25);
	trainCondition = new JTextArea(5,50);
	Testing = new JTextArea("uid < 1450",5,45);   //sql statement to fetch test cases
	
	ModelNames = new JTextField(20);
	//show learners
	comboBox = new JComboBox();
	comboBox.addItem("Naive Bayes");
	comboBox.addItem("ID3 Decision Tree");
	comboBox.addItem("LogitBoost");
	comboBox.addItem("LinearRegression");
	comboBox.addItem("Prism Classifier");
	comboBox.addItem("VotedPerceptron");
	comboBox.addItem("SMO sequential minimal optimization");
	comboBox.addItem("KernelDensity");
	comboBox.addItem("How about you Guess, First?!?");
        comboBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent evt)
		{
		    
		    LearnChoice = (comboBox.getSelectedIndex());
		    
		}
	    });


	JPanel learner = new JPanel();
	learner.add(new JLabel("Learning Method:"));
	learner.add(comboBox);
	constraints.gridx = 0;
	constraints.gridy = 0;
	vertFlowAdd(main, learner, gridbag, constraints);
	
	model2.add(model);
	ModelNames.setText("model.mod");
	
	model2.add(ModelNames);
	JButton checkModel = new JButton("Check Model Info");
        checkModel.setToolTipText("Click here to get some information on the current model named in the model");




	checkModel.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    System.out.println("getting info on " + getModelName());
		    showMSG(fileInfo(getModelName()));
		    //goEvalModels.setEnabled(true);

	        }
	    }   );
	model2.add(checkModel);
	vertFlowAdd(main, model2, gridbag, constraints);
    
	JPanel train2 = new JPanel();
	train2.add(new  JLabel("Name of Training File:"));
	TrainNames = new JTextField(20);
	TrainNames.setText("train.rtf");
	train2.add(TrainNames);
    
	JButton checkTrain = new JButton("Check Training File Header");
	checkTrain.setToolTipText("Click here to display the beginning of the training file, details include how it was created");

        checkTrain.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    showTrain(getTrainName());
		    goEvalModels.setEnabled(true);

	        }
	    }   );
    
	train2.add(checkTrain);			     			     			     
	vertFlowAdd(main, train2, gridbag, constraints);
	//now for test file creation

	JPanel test1 = new JPanel();
	test1.add(new  JLabel("Name of Testing File:"));
	
	TestNames = new JTextField(20);
	TestNames.setText("testing.rtf");
	test1.add(TestNames);
    
	JButton checkTest = new JButton("Check Testing File Header");
	checkTest.setToolTipText("Click to see the header of the file that will be used for training the model");
        checkTest.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    showTrain(getTestName());
		    goEvalModels.setEnabled(true);

	        }
	    }   );
    
	test1.add(checkTest);			     		
	vertFlowAdd(main, test1, gridbag, constraints);
	

	JPanel f = new JPanel();
	JButton chooseFeature = new JButton("Choose Features");
	chooseFeature.setToolTipText("Click to bring up the features window, one can also click on the email feature tab above");
        chooseFeature.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    // EmailFig.grabFocus();;
		    BusyWindow G = new BusyWindow("features","");
		    
		    G.getContentPane().add( EmailFig);
		    G.pack();
		    G.show();
		    //		    EmailFig.requestFocusInWindow();
	        }
	    }   );
	f.add(chooseFeature);
	JLabel f3 = new JLabel("Features being used:");
	f3.setToolTipText("These are the features used from the data in the database");
	f.add(f3);

	
	setFeatures(vecToStr3(EmailFig.getSelectedFigures()));
	JScrollPane Features2 = new JScrollPane(Features);
	Features2.setBorder(new BevelBorder(BevelBorder.LOWERED));
	f.add(Features2);
	vertFlowAdd(main, f, gridbag, constraints);
	
	//UID in " + uids
	//UID in " + vecToStr(V)
	JPanel trainc = new JPanel();
	trainc.add(new JLabel("Condition for Training cases"));
	trainCondition.setText("class = 'S' OR class ='N'"); //"UID in " + vecToStr(V)+'\n');

	JScrollPane tc2 = new JScrollPane(trainCondition);
	tc2.setBorder(new BevelBorder(BevelBorder.LOWERED));


	trainc.add(tc2);
	vertFlowAdd(main, trainc, gridbag, constraints);

	JPanel strain = new JPanel();	
	JLabel t = new JLabel("SQL for creating Training Cases");
	t.setToolTipText("criteria for getting cases into flat file");
	strain.add(t);


	setTesting(trainCondition.getText());
	JScrollPane Training2 = new JScrollPane(Training);
	Training2.setBorder(new BevelBorder(BevelBorder.LOWERED));

	strain.add(Training2);
	vertFlowAdd(main, strain, gridbag, constraints);
	
	JPanel stest = new JPanel();
	
	stest.add(new JLabel("Which cases to compare to current model:"));
      
	
	JScrollPane Testing2 = new JScrollPane(Testing);
	Testing2.setBorder(new BevelBorder(BevelBorder.LOWERED));


	stest.add(Testing2); 
	vertFlowAdd(main, stest, gridbag, constraints);

	JPanel reporting = new JPanel();
	reporting.add(new JLabel("Threshold to create Alerts:"));
	reportThreshold = new JTextField(10);
	reportThreshold.setText("0.89");
	reporting.add(reportThreshold);
	reporting.add(new JLabel("Class Alerting on:"));
	reportClass = new JTextField(5);
	reportClass.setText("s");
	reporting.add(reportClass);
	vertFlowAdd(main, reporting, gridbag, constraints);
	
	
	goBuildModels = new JButton("Build Training Files");
        goBuildModels.setToolTipText("Click here to create a training file using the features above");

	goBuildModels.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		  (new Thread(new Runnable() 
		    {
		      public void run()
		      {
			m_winGui.setEnabled(false);
			get_data(getTrainName(),trainCondition.getText(),Features.getText(),true);
			//goEvalModels.setEnabled(true);
			m_winGui.setEnabled(true);
		      }
		    })).start();
	        }
	    }
					);
	goLoadModels = new JButton("Build Models from Training");
	goLoadModels.setToolTipText("create a model, named as above, using the current learning scheme");
        goLoadModels.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		  (new Thread(new Runnable()
		    {
		      public void run()
		      {
			m_winGui.setEnabled(false);
			build_model(getTrainName(),getModelName());
			goEvalModels.setEnabled(true);
			m_winGui.setEnabled(true);
			
		      }
		    })).start();
	        }
	  }
				       );
	goEvalModels = new JButton("Evaluate Models over Test Cases");
	goEvalModels.setToolTipText("run the model over the Test Cases and display result"); 
	//goEvalModels.setEnabled(false);
        goEvalModels.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		  (new Thread(new Runnable() 
		    {
		      public void run()
		      {
			m_winGui.setEnabled(false);
			run_analysis(getTestName(),getModelName());
			m_winGui.setEnabled(true);
		      }
		    })).start();
	        }
	  }
				       );

	goBuildTest = new JButton("Build Test Cases");
        goBuildTest.setToolTipText("Build the test cases using information provided by the panel");
	goBuildTest.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		  (new Thread(new Runnable() 
		    {
		      public void run()
		      {
			m_winGui.setEnabled(false);
			get_data(getTestName(), Testing.getText(),Features.getText(),false);
			goEvalModels.setEnabled(true);
			m_winGui.setEnabled(true);
		      }
		    })).start();
	        }
	    }
				      );
	goReport = new JButton("Build Alerts from Table");
        goReport.setToolTipText("Create Alerts using some threshold on some class");
	goReport.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    makeAlert(); 
		    
	        }
	    }
				   );
	goUpdateDB = new JButton("ReClassify DB");
        goUpdateDB.setToolTipText("Use current model to classify DB");
	goUpdateDB.addActionListener(new ActionListener() 
	  {
	    public void actionPerformed(ActionEvent e) 
	    {
	      (new Thread(new Runnable() 
		{
		  public void run()
		  {
		    m_winGui.setEnabled(false);
		    updateDB(getModelName());
		    m_winGui.setEnabled(true);
		  }
		})).start();
	    }
	  });

	JPanel pan = new JPanel();
	JPanel pan2 = new JPanel();
	pan.add(goBuildModels);
	pan.add(goLoadModels);
	pan.add(goBuildTest);
	pan2.add(goEvalModels);
	pan2.add(goReport);
	pan2.add(goUpdateDB);
	vertFlowAdd(main, pan, gridbag, constraints);
	vertFlowAdd(main, pan2, gridbag, constraints);
	vertFlowAdd(main, sPane, gridbag, constraints);

	setViewportView(main);
	
	trainwin=false; //not setup yet
	trainmsg = new JTextArea(25,25);
	trainmsg.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

  private static void vertFlowAdd(Container cont, Component comp, GridBagLayout gridbag, GridBagConstraints constraints)
  {
    gridbag.setConstraints(comp, constraints);
    cont.add(comp);
    constraints.gridy++;
  }

    //set the feautres window
    public void setFeatures(String data)
    { 
	Features.setText(data+",class");
	setTesting( trainCondition.getText());//"class = 'I' OR class ='N'");
	// + vecToStr(V));//sql statement to fetch train

    }

    public void setTesting(String s)
    {
	Training.setText("select " +  Features.getText() + " from email where "+s);
    }


    /*will diplay some file information
     */

    public String fileInfo(String name)
    {
	
	String info = new String();
	File check = new File(name);
	if(check.exists() != true)
	    {return info;
	    }

	try{
	    info += "Name: " + name ;
	    info += "\nLast modified: " ;
	    if(check.lastModified()!= 0L )
		info += check.lastModified();
	    else
		info += "might be STILL OPEN";
	    info += "\nLength: " + check.length();
	    info += "\nHash: " + check.hashCode();
	}catch(Exception e){info+=e;}
	
	return info;
    }

    /* convert vector to string of values comma deliminted
     */
    public String vecToStr(Vector v)
    {
	int i=v.size();
	String s= new String("(");
	for(int j=0;j<i;j++)
	    {
		if(j==0)
		    s+=v.elementAt(j);
		else
		    s+="," + v.elementAt(j);
	    }
	s+= ")";
	return s;

    }
    public String vecToStr2(Vector v)
    {
	int i=v.size();
	String s= new String("{'");
	for(int j=0;j<i;j++)
	    {
		if(j==0)
		    s+=v.elementAt(j)+"'";
		else
		    s+=",'"+v.elementAt(j);
	    }
	s+= "'}";
	return s;

    }

    public String vecToStr3(Vector v)
    {
	int i=v.size();
	String s= new String("");
	for(int j=0;j<i;j++)
	    {
		if(j==0)
		    s+=v.elementAt(j);
		else
		    s+="," + v.elementAt(j);
	    }
	
	return s;

    }
    
    /*
     *will create the alert reports
     *uses the threshold and the classification from the gui
     */
    
    public void makeAlert()
    {
	//double threshold = 0.45;
	System.out.println("going to make a report");
	try{

	    threshold = Double.parseDouble(reportThreshold.getText());
	}catch(NumberFormatException t){
	    JOptionPane.showMessageDialog(this, "Error getting threshold from gui: " + t);
	}
	String rclass = reportClass.getText().toLowerCase().trim();
	int classIndex = m_messages.getColumn("Predict Class").getModelIndex();
	int scoreIndex = m_messages.getColumn("Confidence").getModelIndex();
	int uidIndex = m_messages.getColumn("Email UID").getModelIndex();
	    
	String tempClass = new String();
	double tempScore;
	
	int count=0;
	int length = m_messages.getModel().getRowCount();
	for(int i=0;i< length;i++)
	    {
		tempClass = (String) m_messages.getValueAt(i, classIndex);
		if(rclass.equals(tempClass))
		    {
			try{
			    tempScore = ((Double) m_messages.getValueAt(i, scoreIndex)).doubleValue() ;
			    //  System.out.print(tempScore + ' ');
			    if(tempScore > threshold)
				{//need to make a report if this row
				   m_alerts.alert(Alert.ALERTLEVEL_RED,"report from Learning Window: " + m_messages.getValueAt(i,uidIndex) + " scored " + tempScore, "Threshold was " +threshold +" .....here we can have whole body of email " +m_messages.getValueAt(i,m_messages.getColumn("Mailref").getModelIndex())); 

				   count++;
				}
			
			}catch(NumberFormatException tt){ System.out.println("Problem in checking a threshold in makealert");}
			

		    }
	    }
	//now to show something to the user:
	JOptionPane.showMessageDialog(this, "Number of records updated to Alert Table: " + count);


    }//end makealert




    /* will return the name of thr training file
     */

    public String getTrainName()
    {
        return TrainNames.getText();
    }

    public String getTestName()
    {
        return TestNames.getText();
    }
    
    public String getModelName()
    {
	return ModelNames.getText();
    }

    //will try to show the header part of the training file	
    public void showTrain(String file)
    {
	//will need to try to open the file specified:
	
	String r = new String();
	String s = new String();
	
	try{
	    BufferedReader in  = new BufferedReader(new FileReader(file));
	    

	    while((r = in.readLine()) != null )
		{ 
		    if(r.startsWith("%")==true || r.startsWith("@") == true)
			s+="\n" + r;
		    else if(r.startsWith("@data"))
			break;
		}
	    in.close();
	
	
	}catch( Exception e ){ showMSG(e.toString()); return;}
	showMSG(s);
    }//end show training file

    
    //Method to draw the help instructions
    // To make it generic it takes a string and puts it on the screen
    

    public void showMSG(String file){
	String S = new String(file);

	//JOptionPane.showMessageDialog(this, file);
	//use a boolean helpwin which is set if window in existance, then only need to refocus
	if(trainwin) {
	    trainmsg.setText(S +"\n");
	    Twin.repaint();
	    Twin.requestFocus();
	    return;
	} 
	//so need to make the window
	Twin = new JFrame("Message Window");
	Twin.setSize(500,400);
	Twin.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {trainwin=false;Twin.dispose(); }
	    });
	    
	//main.setSize(200,200);
	Rectangle bounds = Twin.getBounds();
	Twin.setLocation(300 + bounds.x, 50 + bounds.y);
	trainmsg.setText(S +"\n");   // the slash n is so that it shows up on the left side of the viewport

	JScrollPane jp = new JScrollPane();
	
	jp.setViewportView(trainmsg);
	Twin.getContentPane().add(jp);
	
	
	Twin.setVisible(true);
	trainwin=true; //set the boolean to true
         
	Twin.requestFocus();
	
    }//end show MSG
	



    /**
     * @params Vector of Integer elements which are the UIDs
     *         and string of column name to select
     **/
 
    public void get_data(final String fname, final String cond, final String feats, final boolean showclass) 
    {
      System.out.println("cond: " + cond);
      System.out.println("featu:" + feats);
	
      m_status.setText("NBayes: Getting data");
      //String uids = new String(v.toString());//might have to clean this up
      //TODO chanhe this to parse it out
      //	String uids = (vecToStr(v));//new String ( "(" + ((String)v.toString()).substring(1));
	
	
      String data[][];
      //	int l = ((String)Features.getText()).length();
      String fields = new String(feats);//.substring(1,l-1));//new String("sender,rcpt,size,utime,class");
	
      int elements;// = v.size();//assume all in here
      String querry = new String();
      PrintWriter out;
      //although bad idea will read off the feature string and buil vector
      StringTokenizer tok = new StringTokenizer(feats,"(,)");
      Vector tvec = new Vector();//temp vec
      while(tok.hasMoreTokens())
      {
	tvec.add(tok.nextToken());
      }
      String temp = new String();  
      //	System.out.println("fields:" + fields);
      int numfields = tvec.size();
	

      try{
	out = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
	    
	out.println("%automatic file generated for training the classifier");
	out.println("%using learning algorithm: "+LearnChoice + " from weka");
	out.println("%SQL used: select " +fields+ " from email where " +  
		    cond);
	out.println("@relation email-emt-data");
	//this has to be generalized
	int count=0;
	
	    
	while( tvec.size() > 0)
	{ 
	  temp = (String)tvec.elementAt(0);
	  out.print("@attribute '" + fixUp(temp) + "' ");
	  //		    System.out.println("@attribute '" + temp + "' ");


	  if(temp.equals("size") || temp.equals("utime") || temp.equals("numrcpt") || temp.equals("numattach") || temp.equals("recnt"))
	  { out.println(" real");  }
		
	  else
	  {
	    out.print(" {");
	    synchronized (m_jdbcView)
	    {	
	      if(temp.equals("class"))
	      m_jdbcView.getSqlData("select distinct " + tvec.elementAt(0) + " from email");//where "+ trainCondition.getText() );
	      else
	      m_jdbcView.getSqlData("select distinct " + tvec.elementAt(0) + " from email");// where " +  cond);
				   
				       
	      data = m_jdbcView.getRowData();
	      count = m_jdbcView.getResultSize();
	      //	    System.out.println("size gottne : " + count);
	    }
	    for(int k=0;k<count;k++)
	    {
	      if(data[k][0].trim().length() >0)
	      {if(k==0) 
	      out.print("'" +fixUp(data[k][0].trim().toLowerCase()) + "'");
	      else
	      out.print(",'" +fixUp(data[k][0].trim().toLowerCase()) + "'");
	      }
			
	    }
	    out.println("}");
	  } 
	  tvec.removeElementAt(0); 

	}

	System.out.println("done printing header");
	//note: UId added to allow us to save a reference to databse number
	querry = new String("select " + fields+",UID,subject,mailref  from email where " + cond);//"select " +fields+ " from email where UID in " + uids);
	out.println("@data");
	synchronized (m_jdbcView)
	{
	  m_jdbcView.getSqlData(querry);
	  data = m_jdbcView.getRowData();
	  elements = m_jdbcView.getResultSize();
	}
	    
	//for(l=0;l<elements;l++)
	//	mailref[l] = new String();
	subject = new String[elements+1];
	mailref = new String[elements+1];
	currentclass = new char[elements+1];
	examplesUID = new int[elements+1];
	confidence = new double[elements+1];
	learnedclass = new char[elements+1];
	numExamples = elements;
	for (int i=0, j=0;j<elements;j++)
	{    for(i=0;i<numfields-1;i++)
	{
	  if(data[j][i].trim().length() > 0)
	  out.print(fixUp(data[j][i].trim().toLowerCase())  + ", ");
	  else
	  out.print("?,");

	}


	if(showclass==false) //during test case generation
	{out.println("?");
	currentclass[j] = (data[j][i++].trim().toLowerCase()).charAt(0);
	examplesUID[j] = Integer.parseInt(data[j][i++]);
	subject[j] = new String(data[j][i++]);
	mailref[j] = new String(data[j][i]);

	}
	else
	out.println(fixUp(data[j][i].trim().toLowerCase()));


		    

	}//end for loop

	out.close();
	    
      }catch (Exception e)
      {
	showMSG("Couldn't connect to database in NBayes or " + e);
	//System.exit(1);
      }
      m_status.setText(" ");
    }//end get data


    //this create the model and passes it to another method....
    //that way we can reuse the model for other
    //analysis
    public void build_model (final String train_file, final String model_file)
    {
      m_status.setText("NBayes: Building Model");
	
      String todo[] = new String[8];
      todo[0]= new String("-t");
      todo[1]= new String(train_file);
      todo[2]= new String("-d");
      todo[3]= new String(model_file);
      todo[4] = new String("-v");   //no output
	
      todo[5]= new String("");
      todo[6]= new String("");
      todo[7]= new String("");
       
      try{
	/*comboBox.addItem("KernelDensity");
	  comboBox.addItem("ID3 Decision Tree");
	  comboBox.addItem("LogitBoost");
	  comboBox.addItem("LinearRegression");
	  comboBox.addItem("Prism Classifier");
	  comboBox.addItem("VotedPerceptron");
	  comboBox.addItem("SMO sequential minimal optimization");
	*/

	if(LearnChoice == 0){
		
	  showMSG(Evaluation.evaluateModel(new NaiveBayes(),todo));
				
	  System.out.println("back from nb");
	}
	else if(LearnChoice ==1)
	{ 
	  showMSG(Evaluation.evaluateModel(new Id3(),todo));
	}
	else if(LearnChoice ==2)
	{
	  todo[5] = new String("-W");
	  todo[6] = new String("weka.classifiers.DecisionStump");

	  showMSG(Evaluation.evaluateModel(new LogitBoost(),todo));
	}
	else if(LearnChoice ==3)
	{ showMSG(Evaluation.evaluateModel(new LinearRegression(),todo));
	}
	else if(LearnChoice ==4)
	{ showMSG(Evaluation.evaluateModel(new Prism(),todo));
	}
	else if(LearnChoice ==5)
	{ showMSG(Evaluation.evaluateModel(new VotedPerceptron(),todo));
	}
	else if(LearnChoice ==6)
	{ showMSG(Evaluation.evaluateModel(new SMO(),todo));
	}
	else if(LearnChoice ==7)
	{ showMSG(Evaluation.evaluateModel(new KernelDensity(),todo));
	}

	
      }catch (Exception e) {showMSG("Learning Component wwww threw exception:\n" + e );
	    
      }
	
      m_status.setText(" ");
    }
    
    public void updateDB(String modelname)
    {
	//best thing is to se how many records in the db and process 10k at a time
	//shouldnt be too much of overhead
	
	String data[][];
	int count=0;
	try{
	synchronized (m_jdbcView)	    {	
	    
		m_jdbcView.getSqlData("select count(*) from email");
		data = m_jdbcView.getRowData();
	    }
	
        count = Integer.parseInt(data[0][0].trim());    //the amount of records in db
	}catch(Exception e){System.out.println("problem with db counting " + e);}

	if (count <1) //nothgin to do
	    return;
	

	System.out.println("will now process all " + count+ " records in dB");
	get_data("temp_for_updatedb.rtf","uid >=0",Features.getText(),false);
	run_DBanalysis("temp_for_updatedb.rtf",getModelName());
	





    }//end updatedb




    public void run_analysis (final String eval_file, final String model_file)
    {
      m_status.setText("Learning: running analysis");

      String todo[] = new String[8];
	
      todo[0]= new String("-T");
      todo[1]= new String(eval_file);
      todo[2]= new String("-l");
      todo[3]= new String(model_file);
      todo[4]= new String("-p");
      todo[5]= new String("");
      todo[6]= new String("");
      todo[7]= new String("");
       
      try{

	if(LearnChoice == 0){
	  parseResults(Evaluation.evaluateModel(new NaiveBayes(),todo));
	}
	else if(LearnChoice ==1)
	{ 
	  parseResults(Evaluation.evaluateModel(new Id3(),todo));
	}
	else if(LearnChoice ==2)
	{ 
	  //todo[5] = new String("-W");
	  //todo[6] = new String("weka.classifiers.DecisionStump");
		    
	  parseResults(Evaluation.evaluateModel(new LogitBoost(),todo));
	}
	else if(LearnChoice ==3)
	{ parseResults(Evaluation.evaluateModel(new LinearRegression(),todo));
	}
	else if(LearnChoice ==4)
	{ parseResults(Evaluation.evaluateModel(new Prism(),todo));
	}
	else if(LearnChoice ==5)
	{ parseResults(Evaluation.evaluateModel(new VotedPerceptron(),todo));
	}
	else if(LearnChoice ==6)
	{ parseResults(Evaluation.evaluateModel(new SMO(),todo));
	}
	else if(LearnChoice ==7)
	{ parseResults(Evaluation.evaluateModel(new KernelDensity(),todo));
	}

      }
      catch (Exception e) 
      {
	showMSG("Learning component sss threw Exception:\n" + e );
      }
      m_status.setText(" ");
    }
    
    public void parseResults(String s)
    {
      //showMSG(s);
	System.out.println("IN Parse Results ");
	StringTokenizer tok = new StringTokenizer(s,"\n");
	StringTokenizer tok2;
	int i=0;
	//DefaultTableModel model = (DefaultTableModel)m_messages.getModel();
	
	// clear table
	int length = m_messages.getModel().getRowCount();
	if(length > 0)
	    m_tableModel.clear();

	while(tok.hasMoreTokens())
	    {
		tok2 = new StringTokenizer(tok.nextToken()," ");
		tok2.nextToken();
		learnedclass[i] = tok2.nextToken().charAt(0);
		try{
		    confidence[i++] = Double.parseDouble(tok2.nextToken());
		}catch(Exception e){System.out.print(".");
		confidence[i++] = 0.0;	
	    
		}
	    }

	String []temp = new String[6];
	temp[0]= new String();
	temp[1]= new String();
	temp[2]= new String();
	temp[3]= new String();
	temp[4] = new String();
	temp[5] = new String();
	//better way to sort would be to adopt the message table....future
	

	System.out.println("in parse");
	for(int j=0;j<numExamples;j++)
	    {
		//	System.out.print(".");
		if(learnedclass[j]=='i')
		    {
			temp[0] =""+ learnedclass[j];
			temp[1] =""+ confidence[j];
			temp[2] =""+ currentclass[j];
			temp[3] = mailref[j];
			temp[4] = subject[j];
			temp[5] = "" +examplesUID[j];
			m_tableModel.addRow(temp);
		    }
	    }

	

	for(int j=0;j<numExamples;j++)
	    {
		if(learnedclass[j]=='n')
		    {
			temp[0] =""+ learnedclass[j];
			temp[1] =""+ confidence[j];
			temp[2] =""+ currentclass[j];
		
			temp[3] = mailref[j];
			temp[4] = subject[j];
			temp[5] = "" +examplesUID[j];
		
		
			m_tableModel.addRow(temp);
		    }
	    }

	for(int j=0;j<numExamples;j++)
	    {
		if(learnedclass[j]!='i' && learnedclass[j]!='n' )
		    {
			temp[0] =""+ learnedclass[j];
			temp[1] =""+ confidence[j];
			temp[2] =""+ currentclass[j];
	temp[3] = mailref[j];
			temp[4] = subject[j];
			temp[5] = "" +examplesUID[j];
		
			m_tableModel.addRow(temp);
		    }
	    }
	  
	  
    }

    public void run_DBanalysis (final String eval_file, final String model_file)
    {
      try{
	threshold = Double.parseDouble(reportThreshold.getText());
      }catch(Exception t){};

      m_status.setText("Learning: running analysis");
      String todo[] = new String[8];
	
      todo[0]= new String("-T");
      todo[1]= new String(eval_file);
      todo[2]= new String("-l");
      todo[3]= new String(model_file);
      todo[4]= new String("-p");
      todo[5]= new String("");
      todo[6]= new String("");
      todo[7]= new String("");
       
      try{

	if(LearnChoice == 0){
		

	  DBparseResults(Evaluation.evaluateModel(new NaiveBayes(),todo));

	}
	else if(LearnChoice ==1)
	{ 
		    
		  
	  DBparseResults(Evaluation.evaluateModel(new Id3(),todo));
	}
	else if(LearnChoice ==2)
	{ 
	  //todo[5] = new String("-W");
	  //todo[6] = new String("weka.classifiers.DecisionStump");

		    
	  DBparseResults(Evaluation.evaluateModel(new LogitBoost(),todo));
	}
	else if(LearnChoice ==3)
	{ DBparseResults(Evaluation.evaluateModel(new LinearRegression(),todo));
	}
	else if(LearnChoice ==4)
	{ DBparseResults(Evaluation.evaluateModel(new Prism(),todo));
	}
	else if(LearnChoice ==5)
	{ DBparseResults(Evaluation.evaluateModel(new VotedPerceptron(),todo));
	}
	else if(LearnChoice ==6)
	{ DBparseResults(Evaluation.evaluateModel(new SMO(),todo));
	}
	else if(LearnChoice ==7)
	{ DBparseResults(Evaluation.evaluateModel(new KernelDensity(),todo));
	}

      }catch (Exception e) {showMSG("Learning component sss threw Exception:\n" + e );}
      m_status.setText(" ");
    }

    public void DBparseResults(String s)
    {
	//	showMSG(s);
	System.out.println("IN DB - Parse Results ");
	StringTokenizer tok = new StringTokenizer(s,"\n");
	StringTokenizer tok2;
	int i=0;

	while(tok.hasMoreTokens())
	    {   
		tok2 = new StringTokenizer(tok.nextToken()," ");
		tok2.nextToken();
		learnedclass[i] = tok2.nextToken().charAt(0);
		try{
		    confidence[i++] = Double.parseDouble(tok2.nextToken());
		}catch(Exception e){System.out.print(".");
		confidence[i++] = 0.0;	
	    
		}
	    }

	for(int j=0;j<numExamples;j++)
	    {
	
		synchronized (m_jdbcUpdate)
		    {
			try{
			    if(confidence[j] >=  threshold)
			    m_jdbcUpdate.executeQuery("update email set class = '"+learnedclass[j]+"' where uid=" + examplesUID[j] );
		    
			}catch(Exception e3){System.out.println(e3);}
		    }
		
	    }
    }



    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	
    public void mouseClicked(MouseEvent e)
    {
	if (e.getClickCount() == 2)
	    {
		try
		    {
			Point p = e.getPoint();
			int row = m_messages.rowAtPoint(p);
			String UID =  new String("" + m_messages.getValueAt(row, m_messages.getColumn("Email UID").getModelIndex()));
			String detail = new String("uid click on : " +UID);
			//System.out.println("UID: " + UID);
			String data[][];
			synchronized (m_jdbcView)
			    {
				m_jdbcView.getSqlData("select mailref,sender,rcpt,size,subject from email where uid = " + UID);
				data = m_jdbcView.getRowData();
			    }
			detail += "\nMailref: " + data[0][0].trim();
			detail += "\nSender: " + data[0][1].trim();
			detail += "\nReciepent:"+ data[0][2].trim();
			detail += "\nSize:"+ data[0][3].trim();
			detail += "\nSubject:"+ data[0][4].trim();
			//			detail += "\nBody:" + data[0][4];
			
			synchronized (m_jdbcView)
			    {
				m_jdbcView.getSqlData("select body from message where mailref = '" + data[0][0].trim() + "'");
				data = m_jdbcView.getRowData();
			    }
			if(data.length>0)
			detail += "\n****Body****\n" + data[0][0].trim() +"\n****Body****\n";



	
				// get frame ancestor
			Component c = this;
			while ((c != null) && !(c instanceof Frame))
			    {
				c = c.getParent();
			    }
				
			DetailDialog detailDialog;
			if (c == null)
			    {
				detailDialog = new DetailDialog(null, false, detail);
			    }
			else
			    {
				detailDialog = new DetailDialog((Frame) c, false, detail);
			    }
				
			detailDialog.setLocationRelativeTo(this);
			detailDialog.show();
		    }catch (SQLException ex)
			{
			    JOptionPane.showMessageDialog(this, "6 - Error getting details from database: " + ex);
			}
	    }
    }
    ///added from sysd messgae class
    private class MessageTableModel extends TableSorter
    {
	MessageSubModel m_model;
		
	public MessageTableModel(String columnNames[], int rows)
	{
	    super();
	    m_model = new MessageSubModel(columnNames, rows);
	    setModel(m_model);
	    m_model.addTableModelListener(this);
	    m_sortColumn = 4;
	}
		
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
	    if( ((String)getColumnName(columnIndex)).equals("Interest")  ) //((MessageSubModel)model).  
		{
		    
		    return true;
		
		}
	    else	    
		return false;
	}
		
	public synchronized void addRow(Object[] rowData)
	{
	    
	    ((MessageSubModel)model).addRow(rowData);
	    
	    

	}
		
	public synchronized void addColumn(String name, Object[] Data)
	{
	    
	    ((MessageSubModel)model).addColumn(name, Data);
	}
		
	public synchronized void clear()
	{
	    while (m_model.getRowCount() > 0)
		{
		    m_model.removeRow(0);
		    reallocateIndexes();
		}
	}
		
	public synchronized Object getValueAt(int row, int column)
	{
	    return super.getValueAt(row, column);
	}
    }
	
    private class MessageSubModel extends DefaultTableModel
    {
	public MessageSubModel(String[] columnNames, int rows)
	{
	    super(columnNames, rows);
	}
		
	public Class getColumnClass(int column)
	{
	    //return getValueAt(0, column).getClass();
	    //shlomo summer 02 changed from being hardcoded
	    String sname  = ((String)getColumnName(column));        
	    if( sname.equals("Interest") )
		//column == 7)//((TableColumn)(super.getColumn("Interest"))).modelIndex)//== 7)
		{
		    return javax.swing.JComboBox.class; //.lang.String.class;
		}    
	    if ( sname.equals("# Rcpt") || sname.equals("# Attach") || sname.equals("Size") || sname.equals("Email UID") )
		{
		    return java.lang.Integer.class;
		}
	    else if(   sname.equals("Confidence"))
		{
		    return java.lang.Double.class;
		}
	    else
		{
		    return java.lang.String.class;
		}
	    
	}

		
	public Object getValueAt(int row, int column) 
	{
	    // if(column==7)
	    //return super.getValueAt(row,column);
	    //   //return (javax.swing.JComboBox)super.getValueAt(row,column);
	    String sname  = ((String)getColumnName(column));        
	    
	    if ( sname.equals("# Rcpt") || sname.equals("# Attach") || sname.equals("Size") || sname.equals("Email UID") ) 
		{
		    try
			{
			    return(new Integer((String)super.getValueAt(row,column)));
			}
		    catch (NumberFormatException ex)
			{
			    System.out.print("number format probems here  ");
			    return null;
			}
		}
	    else if(  sname.equals("Confidence"))
		{		    try
		    {
			return(new Double((String)super.getValueAt(row,column)));
		    }
		catch (NumberFormatException ex)
		    {
			System.out.print("double-number format probems here  ");
			return null;
		    }
		}
	    else
		{
		    return super.getValueAt(row, column);
		}
	    
	}
    }	


  public String fixUp(String s)
  {
    s = s.replace('%', '#');
    s = s.replace('{', '#');
    s = s.replace('}', '#');
    s = s.replace('\'', '#');
    s = s.replace('\\', '#');
    s = s.replace('"', '#');
    s = s.replace('`', '#');
    s = s.replace(',', '#');
    s = Pattern.compile("@attribute", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("#attribute");
    s = Pattern.compile("@relation", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("#relation");
    s = Pattern.compile("@data", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("#data");
    s = s.replaceAll("\\s", "");

    return s;
  }
	 


}//end class









