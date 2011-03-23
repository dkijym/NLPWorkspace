package metdemo.DataBase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import metdemo.Tools.Preferences;
import metdemo.Window.LoadDB;

public class DataImportExport extends JPanel {
	
	JButton importerButton;
	JButton exporterButton;
	JButton restartButton;
	JComboBox dbchoices;
	public DataImportExport(){
		InitialSetup();
		
		//also need to setup the restart button
		restartButton = new JButton("Restart from beginning");
		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				InitialSetup();
			}
		});
		
		
	}
	
	
	/**
	 * setup the initial window of choosing between import and export
	 *
	 */
	private void InitialSetup(){
		removeAll();
		
		importerButton = new JButton("Import Data");
		importerButton.setToolTipText("Click here to bring in a database");
		importerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setupImport1();
			}
		});
		
		exporterButton = new JButton("Export Data");
		exporterButton.setToolTipText("Click here to save your db to a file");
		exporterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setupExport1();
			}
		});
		add(importerButton);
		add(exporterButton);
		
		validate();
		repaint();
		
	}
	
	
	private void setupImport1(){
		removeAll();
		add(Box.createHorizontalStrut(150));
		add(new JLabel("Welcome to DB import"));
		add(Box.createHorizontalStrut(250));
		JButton mysqlimport = new JButton("Import Mysql DATABASE");
		
		
		mysqlimport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				//need to get the file
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileHidingEnabled(false);
				chooser.setMultiSelectionEnabled(false);
				
				int returnVal = chooser.showOpenDialog(DataImportExport.this);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					InitialSetup();
					return;
				
				}				
				File filechosen = chooser.getSelectedFile();
				
				
				
				//need to ask about what the db should be called
				//should check if it exists
//				if mysql, do a call to a db using user, pass, and dbname
				
				//if valid, sign in and ask for data directory
				String user = JOptionPane.showInputDialog(DataImportExport.this,"Please enter DB user name","root");
				JPasswordField passfld = new JPasswordField(10);
				JOptionPane.showMessageDialog(
						DataImportExport.this,
						passfld,
						"Please enter your password",
						JOptionPane.PLAIN_MESSAGE);
				
	
				//now to test for a connection
//				try to make a connection:
				try {
					EMTDatabaseConnection jdbcView = DatabaseManager.LoadDB("127.0.0.1", "", user,new String(passfld.getPassword()) , DatabaseManager.sMYSQL);
		            //check for failure
					if (jdbcView.connect(false) == false) {
						JOptionPane.showMessageDialog(DataImportExport.this,"Error Establishing connection with current settings");
						InitialSetup();
						return;
					}
					
					//now to figure out which dbs we can get to 
					
					
						//need to dig out where the data is sitting
						//show variables like 'max_allowed_packe%'
						String [][]data2 = jdbcView.getSQLData("show variables like 'datadir'");
						if(data2!=null){
					
						
						
						
						String targetdb = JOptionPane.showInputDialog(DataImportExport.this, "Please enter a db name to unzip to");
						
						if(targetdb == null)
						{
							return;
						}
						
						//need to check if it exists
						
						File targetdbfile = new File(data2[0][1],targetdb);
						
						if(targetdbfile.exists()){
							int option = JOptionPane.showConfirmDialog(DataImportExport.this, "Do you want to overwrite current info in: " + targetdb);
						
							if(option == JOptionPane.CANCEL_OPTION || option == JOptionPane.NO_OPTION){
								return;
							}
						}else{
							//will create the directories here
							targetdbfile.mkdirs();
						}
							JOptionPane.showMessageDialog(DataImportExport.this, "Going to unzip");
							unzipBunchFiles(filechosen.getAbsolutePath(), targetdbfile.getAbsolutePath());
							JOptionPane.showMessageDialog(DataImportExport.this, "done opening the file");
							
							
							
							
						
						
						
						
						
						}//end if choice
					
					
					

				} catch (Exception e2) {
					System.err.println("Couldn't connect to database with these paramaters, check the batch file");
					JOptionPane.showMessageDialog(DataImportExport.this,"Error Establishing connection with current settings\n"+e2);
					
					InitialSetup();
					return;
				}

				//need to inform about permissions
				
				
				
			
		}});
		
		
		
		
		add(mysqlimport);
		add(Box.createHorizontalStrut(250));
		add(restartButton);
		validate();
		repaint();
	}
	
	
	private void setupExport1(){
		removeAll();
		
		add(new JLabel("Welcome to Export EMT database setup - For large DB's this might take a while"));
		
		String[] listDB= {"Mysql Database","Derby (Java) Database","PostGress DB"};
		
		dbchoices = new JComboBox(listDB);
		dbchoices.setToolTipText("Choose which type Database you want to save");
		add(dbchoices);
		JButton browserbutton = new JButton("Select DB name");
		browserbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//need to see which db we are dealing with
				
				//if mysql, do a call to a db using user, pass, and dbname
				if(dbchoices.getSelectedIndex() == 0){
				//if valid, sign in and ask for data directory
				String user = JOptionPane.showInputDialog(DataImportExport.this,"Please enter DB user name","root");
				JPasswordField passfld = new JPasswordField(10);
				JOptionPane.showMessageDialog(
						DataImportExport.this,
						passfld,
						"Please enter your password",
						JOptionPane.PLAIN_MESSAGE);
				
	
				//now to test for a connection
//				try to make a connection:
				try {
					EMTDatabaseConnection jdbcView = DatabaseManager.LoadDB("127.0.0.1", "", user,new String(passfld.getPassword()) , DatabaseManager.sMYSQL);
		            //check for failure
					if (jdbcView.connect(false) == false) {
						JOptionPane.showMessageDialog(DataImportExport.this,"Error Establishing connection with current settings");
						InitialSetup();
						return;
					}
					
					//now to figure out which dbs we can get to 
					
					String data[] = jdbcView.getSQLDataByColumn("show databases", 1);
					/*for(int i=0;i<data.length;i++)
					{
						System.out.println("data:" + data[i]);
					}*/
					String choice = (String)JOptionPane.showInputDialog(DataImportExport.this,
							"Please choose db to export",
							"Choose Database",
							 JOptionPane.PLAIN_MESSAGE,
							 null,
							data,
							data[0]);
					
					if(choice!=null){
						//need to dig out where the data is sitting
						//show variables like 'max_allowed_packe%'
						String [][]data2 = jdbcView.getSQLData("show variables like 'datadir'");
						if(data2!=null){
						System.out.println("got:"+ data2[0][1] + choice);
							String outputFilename = JOptionPane.showInputDialog(DataImportExport.this, "Please enter the output zip file for the db (it will be zipped)","c:\\emtdatabase"+choice+".mtzp");
							
							File listedfile = new File(data2[0][1],choice);
							System.out.println("listing:" + listedfile);
							ZipBunchFiles(listedfile.listFiles(),outputFilename);
							JOptionPane.showMessageDialog(DataImportExport.this, "done zipping the file");
						}//end if data
					}//end if choice
					
					
					

				} catch (Exception e2) {
					System.err.println("Couldn't connect to database with these paramaters, check the batch file");
					JOptionPane.showMessageDialog(DataImportExport.this,"Error Establishing connection with current settings\n"+e2);
					
					InitialSetup();
					return;
				}
				
				
				
				
				}else if(dbchoices.getSelectedIndex() == 1){
					JOptionPane.showMessageDialog(DataImportExport.this, "Please note that databases are simply directories off of EMT main directory\nTo backup simply zip the db named directory\nto Use, simply unzip and use the name of the directory to access the db\n");
				}else{
					JOptionPane.showMessageDialog(DataImportExport.this, "Please note that postgres has its own tools to deal with moving databases.\n");
					
				}
				//for java based, allow user to list the db name and then validate if directory exists
				
				
				
			}
		});
		add(Box.createHorizontalStrut(20));
		add(browserbutton);
		add(Box.createHorizontalStrut(200));
		add(Box.createHorizontalStrut(100));
		add(restartButton);
		validate();
		repaint();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//derby see
//	http://db.apache.org/derby/docs/dev/adminguide/adminguide-single.html
	
	

	
	
	/**
	 * 
	 * @param filenames These are the files to include in the ZIP file
	 * @param outFilename Create the ZIP file
	 */
	private void ZipBunchFiles(File[] filenames,String outFilename){
//		 
	 if(filenames == null || outFilename == null){
		 return;
	 }
	    
	    // Create a buffer for reading the files
	    byte[] buf = new byte[8192];
	    
	    try {
	        // Create the ZIP file
	      
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
	    
	        // Compress the files
	        for (int i=0; i<filenames.length; i++) {
	            FileInputStream in = new FileInputStream(filenames[i]);
	    
	            // Add ZIP entry to output stream.
	            out.putNextEntry(new ZipEntry(filenames[i].getName()));
	    
	            // Transfer bytes from the file to the ZIP file
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	    
	            // Complete the entry
	            out.closeEntry();
	            in.close();
	        }
	    
	        // Complete the ZIP file
	        out.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	
	private void unzipBunchFiles(String zipfile, String targetDirectory){
		 try {
		      
			 
			
			        // Open the ZIP file
			        ZipFile zf = new ZipFile(zipfile);
			    
			        // Enumerate each entry
			        for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
			            
			        	  ZipEntry entry = (ZipEntry)entries.nextElement();
			        	// Get the entry name
			            String zipEntryName = entry.getName();
			        
			            //open output file
			            System.out.println(targetDirectory+File.separatorChar+zipEntryName);
			            OutputStream out = new FileOutputStream(targetDirectory+File.separatorChar+zipEntryName );
			        //start writing out
			            byte[] buf = new byte[8192];
				        int len;
				        InputStream in = zf.getInputStream(entry);
				        while ((len = in.read(buf)) > 0) {
				            out.write(buf, 0, len);
				        }
				    
				        // Close the streams
				        out.close();
			        
			        
			        
			        }
			    
			 
			 
			 
			 
			 
			 
			 // close the ZIP file
		      
		       zf.close();
		       
		    } catch (IOException e) {
		    e.printStackTrace();
		    }
	}
	
	
}
