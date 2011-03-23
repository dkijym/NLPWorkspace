/*
 * Created on March 17, 2005
 *
 *
 */
package metdemo.MachineLearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Hashtable;

import metdemo.Parser.EMTEmailMessage;
import metdemo.Tools.Utils;

/**
 * @author Shlomo Hershkop
 * 
 * We would like to interface to spamassassin and create a learner to compare to
 * our stuff here.
 *  
 */

public class SpamAssassinLearner extends MLearner {

    final String dataDirectory = "spamassassin_temp_data";

    final String modelDirectory = "spamassassin_model_data";

    private Hashtable labelDirectory;

    int spamCount = 0;

    int goodCount = 0;

    boolean inTraining = true;

    public SpamAssassinLearner() {
        super.setID(MLearner.getTypeString(ML_SPAMASSASSIN));

        //need to empty the temp data directory
        deleteDirectoryContents(dataDirectory, true);

        labelDirectory = new Hashtable();

    }

    /**
     * Utility to demp the files in a specific directory
     * 
     * @param directory
     * @param shouldCreate
     */
    private void deleteDirectoryContents(String directory, boolean shouldCreate) {

        File delTarget = new File(directory);

        if (delTarget.exists()) {

            String file_names[] = delTarget.list();

            for (int i = 0; i < file_names.length; i++) {

                File del = new File(delTarget, file_names[i]);
                if (del.isDirectory()) {
                    deleteDirectoryContents(del.toString(), shouldCreate);
                }
                del.delete();
            }
        } else if (shouldCreate) {

            delTarget.mkdirs();

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#flushModel()
     */
    public void flushModel() {

        //todo: delete all files in data directory

        deleteDirectoryContents(dataDirectory, false);
        //deleteDirectoryContents(modelDirectory);
        labelDirectory.clear();

    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#save(java.lang.String)
     */
    public void save(String filename) throws IOException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#load(java.lang.String)
     */
    public void load(String filename) throws Exception {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#doTraining(metdemo.Parser.EMTEmailMessage,
     *      java.lang.String)
     */
    public void doTraining(EMTEmailMessage msgData, String Label) {

        //fake code to do nothing
        if(msgData!=null)
            return;
        inTraining = true;

        //check if directory for label file exist
        if (!labelDirectory.containsKey(Label)) {

            File tempoutputfile = new File(dataDirectory + File.separator
                    + Label + File.separator);
            tempoutputfile.mkdirs();
            labelDirectory.put(Label, "");
        }

        //Need to store individual email in the directory
        //assuming our counter is correct.

        File outputfile;

        if (Label.equals(MLearner.SPAM_LABEL)) {
            outputfile = new File(dataDirectory + File.separator + Label
                    + File.separator + spamCount++);
        } else {
            outputfile = new File(dataDirectory + File.separator + Label
                    + File.separator + goodCount++);
        }
        try {
            outputfile.createNewFile();
            FileWriter fout = new FileWriter(outputfile);

            fout.write("From: " + msgData.getFromEmail() + "\n");
            String dates = Utils.deSqlizeDateYEARTIME(
                    msgData.getDate() + " " + msgData.getTime()).toString();

            fout.write("Date: " + dates.substring(0, 3) + ","
                    + dates.substring(3, 11) + dates.substring(24)
                    + dates.substring(10, 19) + " -400");

            //          ??content-type
            //should date include time?

            ArrayList cclist = msgData.getRcptList();
            if (!(cclist == null)) {
                for (int i = 0; i < cclist.size(); i++) {
                    if (i == 0) {
                        fout.write("\nTo: ");
                        fout.write((String) cclist.get(i));
                    } else {
                        fout.write(", " + cclist.get(i));
                    }

                }
            }
            fout.write("\nSubject: " + msgData.getSubject() + "\n");
            fout.write("X-Mailer: " + msgData.getXMailer() + "\n");
            fout.write("Message-Id: " + msgData.getMailref() + "\n");

            fout.write("\n" + msgData.getBody() + "\n");
            fout.flush();
            fout.close();

        } catch (IOException eio) {
            eio.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#doLabeling(metdemo.Parser.EMTEmailMessage)
     */
    public resultScores doLabeling(EMTEmailMessage msgData)
            throws machineLearningException {

        if (inTraining) {
            //need to flush the trainer

            //need to retrain the classifier

            inTraining = false;
        }
        /*The protocol for communication between spamc/spamd is somewhat HTTP like.  The
conversation looks like:

               spamc --> PROCESS SPAMC/1.2
               spamc --> Content-length: <size>
  (optional)   spamc --> User: <username>
               spamc --> \r\n [blank line]
               spamc --> --message sent here--

               spamd --> SPAMD/1.1 0 EX_OK
               spamd --> Content-length: <size>
               spamd --> \r\n [blank line]
               spamd --> --processed message sent here--

After each side is done writing, it shuts down its side of the connection.

The first line from spamc is the command for spamd to execute (PROCESS a
message is the command in protocol<=1.2) followed by the protocol version.

The first line of the response from spamd is the protocol version (note this is
SPAMD here, where it was SPAMC on the other side) followed by a response code
from sysexits.h followed by a response message string which describes the error
if there was one.  If the response code is not 0, then the processed message
will not be sent, and the socket will be closed after the first line is sent.
*/
        
        try{
           StringBuffer outw = new StringBuffer(); 
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        boolean isSpam = false;
        double score = 0;
       
        try {
            echoSocket = new Socket("127.0.0.1",48373 );
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                                        echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: localhost.");
            System.exit(1);
        }

	//now to put together the message
        outw.append("From: " + msgData.getFromEmail() + "\n");
        String dates = Utils.deSqlizeDateYEARTIME(
                msgData.getDate() + " " + msgData.getTime()).toString();

        outw.append("Date: " + dates.substring(0, 3) + ","
                + dates.substring(3, 11) + dates.substring(24)
                + dates.substring(10, 19) + " -400");
        //??content-type
        //should date include time?

        ArrayList cclist = msgData.getRcptList();
        if (!(cclist == null)) {
            for (int i = 0; i < cclist.size(); i++) {
                if (i == 0) {
                    outw.append("\nTo: ");
                    outw.append((String) cclist.get(i));
                } else {
                    outw.append(", " + cclist.get(i));
                }

            }
        }
        outw.append("\nSubject: " + msgData.getSubject() + "\n");
        outw.append("X-Mailer: " + msgData.getXMailer() + "\n");
        outw.append("Message-Id: " + msgData.getMailref() + "\n");
        outw.append("\n" + msgData.getBody() + "\n");
        
        
        
        //wirte out the socket
        out.println("PROCESS SPAMC/1.2");
        out.println("Content-length: " + outw.length());
          //      (optional)   spamc --> User: <username>
        out.print("\r\n");
        out.println(outw);
        out.flush();
        //now to read the response
        String line = new String();
        
        while((line = in.readLine())!=null ){
	
            if (line.startsWith("X-Spam-Status:")) {

                    //is it spam or not
                    if (line.substring(15, 16).equals("Y")) {
                        isSpam = true;

                        score = Double.parseDouble(line.substring(26, line
                                .indexOf(" ", 26)));

                    } else {
                        score = Double.parseDouble(line.substring(25, line
                                .indexOf(" ", 25)));

                    }

                    //System.out.println(score + "\n" + line);
                    in.close();
                    break;
                }
        }
        out.close();
        
        score = (score + 20) * 3;
        if(score > 100){
            score = 100;
        }
        if (isSpam) {

            return new resultScores(MLearner.SPAM_CLASS, (int) score,
                    msgData.getMailref());

        }

        return new resultScores(MLearner.NOT_INTERESTING_CLASS,
                (int) score, msgData.getMailref());

//	while ((userInput = stdIn.readLine()) != null) {
//	    out.println(userInput);
//	    System.out.println("echo: " + in.readLine());
//	}
       
        
        
        }catch(Exception ioe2){
            ioe2.printStackTrace();
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        //dump email to temp file
        //called temp-spamassassin-email
        /*try {
            PrintWriter outw = new PrintWriter(new FileOutputStream(
                    "temp-spamassassin-email"));

            outw.write("From: " + msgData.getFromEmail() + "\n");
            //outw.write("Date: " + msgData.getDate());
            String dates = Utils.deSqlizeDateYEARTIME(
                    msgData.getDate() + " " + msgData.getTime()).toString();

            outw.write("Date: " + dates.substring(0, 3) + ","
                    + dates.substring(3, 11) + dates.substring(24)
                    + dates.substring(10, 19) + " -400");
            //??content-type
            //should date include time?

            ArrayList cclist = msgData.getRcptList();
            if (!(cclist == null)) {
                for (int i = 0; i < cclist.size(); i++) {
                    if (i == 0) {
                        outw.write("\nTo: ");
                        outw.write((String) cclist.get(i));
                    } else {
                        outw.write(", " + cclist.get(i));
                    }

                }
            }
            outw.write("\nSubject: " + msgData.getSubject() + "\n");
            outw.write("X-Mailer: " + msgData.getXMailer() + "\n");
            outw.write("Message-Id: " + msgData.getMailref() + "\n");

            outw.write("\n" + msgData.getBody() + "\n");
            outw.flush();
            outw.close();

            String cmds[] = new String[6];

            for (int i = 0; i < cmds.length; i++) {
                cmds[i] = new String();
            }
            
            //start spamd on cygwin : spamd -x -L -p 48373
            
//spamc.exe PROCESS 127.0.0.1 48373 gtube.txt out
            cmds[0] = "spamc.exe";//"spamassassin.bat";
            cmds[1] = "PROCESS";//"-L"; //local testing (no online tests)
            cmds[2] = "127.0.0.1";//"-x"; //mbox format
            cmds[3] = "48373";//"<"; //pipe file
            cmds[4] = "temp-spamassassin-email"; //will pipe message
            cmds[5] = "temp-spamassassin-out";
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmds);

            //final OutputStream stdin = proc.getOutputStream();
          //spamd  final InputStream stdout = proc.getInputStream(); //get the
                                                              // runnings output
            
            boolean isSpam = false;
            double score = 0;
           //spamd StringBuffer lineread = new StringBuffer();//buffer a line at a
                                                       // time.
         //spamd   char c;
//moved from below for spamd outfile
            proc.waitFor();

            BufferedReader isr = new BufferedReader(new FileReader("temp-spamassassin-out"));
//spamd            InputStreamReader isr = new InputStreamReader(stdout);
            String line;
            //spamd while ((c = (char) isr.read()) > -1) { //System.out.print((char)
            int count =0;                                       // c);
			while((line = isr.readLine())!=null && count++ < 8000   ){
			    //spamd if (c == '\n') {
                    //end of line
                   //spamd String line = lineread.toString();
                    if (line.startsWith("X-Spam-Status:")) {

                        //is it spam or not
                        if (line.substring(15, 16).equals("Y")) {
                            isSpam = true;

                            score = Double.parseDouble(line.substring(26, line
                                    .indexOf(" ", 26)));

                        } else {
                            score = Double.parseDouble(line.substring(25, line
                                    .indexOf(" ", 25)));

                        }

                        //System.out.println(score + "\n" + line);
                        isr.close();
                        break;
                    }
                //spamd    lineread = new StringBuffer();
                //spamd} else
                    //grab line
                //spamd    lineread.append(c);
            }

            //	} catch (IOException ioex) {
            //	ioex.printStackTrace();
            // ignore
            //}
            //	}
            //}).start();

            /*
             * (new Thread() { public void run() { try { int c;
             * InputStreamReader isr = new InputStreamReader(stderr);
             * 
             * while ((c = isr.read()) > -1) System.out.print((char) c); } catch
             * (IOException ioex) { // ignore } } }).start();
             */

            //	int exitVal =
            //System.out.println("Process exitValue: " + exitVal);

          /*  score = (score + 20) * 3;
            if(score > 100){
                score = 100;
            }
            if (isSpam) {

                return new resultScores(MLearner.SPAM_CLASS, (int) score,
                        msgData.getMailref());

            }

            return new resultScores(MLearner.NOT_INTERESTING_CLASS,
                    (int) score, msgData.getMailref());

            //			}
        } catch (FileNotFoundException fio) {
            fio.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }*/

        return new resultScores(0, 0, msgData.getMailref());
    }

    /*
     * static public boolean[] getfeaturesboolean(){ boolean features[] = new
     * boolean[EmailFigures.total_items]; for(int i=0;i <features.length;i++)
     * features[i] = false;
     * 
     * features[EmailFigures.MAILREF] = true;
     * 
     * features[EmailFigures.SENDER] = true;
     * 
     * features[EmailFigures.RCPT] = true;
     * 
     * features[EmailFigures.DATES] = true;
     * 
     * features[EmailFigures.SUBJECT] = true;
     * 
     * features[EmailFigures.MESSAGEBODY] = true;
     * 
     * return features; }
     */

}