package metdemo.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import metdemo.Tools.Utils;

/**
 * Optimize the walk across the mbox format by prefetching one message at a time.
 * 
 * @author Shlomo Hershkop Aug 2005
 * 
 */

public class MBOXIterator implements EmailMessageIterator {

	private int seenSoFar;

	private boolean haveMessageWaiting = false;

	private EMTEmailMessage currentMessage;

	private BufferedReader emailFileHandle;

	private String messageFromLine;

	public MBOXIterator(BufferedReader emailfile) throws EmailFileOpenException {
		seenSoFar = 0;
		messageFromLine = new String();
		emailFileHandle = emailfile;

//		 for first message
		try {
			String line = emailFileHandle.readLine();
			if (line != null) {
				messageFromLine = line;
				setupNextMessage();
				
				//to check if its a dummy message
				if(currentMessage.getFromName().startsWith("mailer-daemon")){
					next();
				}
				
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new EmailFileOpenException(ioe.toString());		}
	}

	public boolean hasNext() {
		return haveMessageWaiting;
	}

	public Object next() {
		EMTEmailMessage returningMessage = currentMessage;
		try {
			setupNextMessage();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
		return (Object) returningMessage;
	}

	public void remove() {
		// TODO Auto-generated method stub

	}

	/**
	 * variables for running through email box
	 * 
	 */
	static final String GENERALFROM = "^From ([^,])*:[0-9]+:(.*)$";

	Pattern p_topfrom = Pattern.compile(GENERALFROM, Pattern.CASE_INSENSITIVE);

	// these Patterns are for parts of the body
	final Pattern body_div = Pattern.compile("^\\s+([^\\s].*)*$");

	// final Pattern body_div = Pattern.compile("^\\s+([^\\s].*)*$");
	final Pattern body_end = Pattern.compile("^\\s*$");// added extra 010

	final Pattern p_return_path = Pattern.compile("^Return-Path: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern rec_line = Pattern.compile("^Received: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_newsgrp = Pattern.compile("^Newsgroups: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_date = Pattern.compile("^Date: (.*):(.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_content = Pattern.compile("^Content-Type:(.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_midmsgid = Pattern.compile(".*Message-ID: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_midstat = Pattern.compile("^.*Status: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_subject = Pattern.compile("^Subject:(.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_rcpt = Pattern.compile("^(To|B?cc):(.*)", Pattern.CASE_INSENSITIVE);
	final Pattern p_delivered = Pattern.compile("Delivered-To: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_fromline = Pattern.compile("^From:(.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_status = Pattern.compile("^Status: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_msgid = Pattern.compile("^Message-ID: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_mimever = Pattern.compile("^MIME-Version: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_delto = Pattern.compile("^Delivered-To: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_inrply = Pattern.compile("^In-Reply-To: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_xmailer = Pattern.compile("^X-Mail.*: (.*)", Pattern.CASE_INSENSITIVE);

	final Pattern p_xstuff = Pattern.compile("^X-.*$", Pattern.CASE_INSENSITIVE);

	final Pattern p_misc = Pattern.compile("^[\\w|-]+: .*");// ,Pattern.CASE_INSENSITIVE);

	final Pattern p_boundry = Pattern.compile(".*boundary=\\\"([^\\\"]*)\\\".*");

	final Pattern p_address1 = Pattern.compile("^\\s*([^\\s]*)\\@([^\\s]*)\\s*$");// ,Pattern.CASE_INSENSITIVE);

	final Pattern p_isprint = Pattern.compile("^([[:print:]].*)?$"); // is it

	// printable
	// characters....from
	// perl,
	// hope it
	// works
	// here.
	// else
	// will
	// need to
	// do
	// something
	// else
	final Pattern p_recount = Pattern.compile("Re:", Pattern.CASE_INSENSITIVE);

	final Pattern p_fwdcount = Pattern.compile("fwd*:", Pattern.CASE_INSENSITIVE);

	final Pattern p_quotes = Pattern.compile("[\\\"']");

	final Pattern sqldate = Pattern.compile("^(.*) .* at (.*) .*$");

	final Pattern p_cleanfile = Pattern.compile("^[0-9]{2}\\.[0-9]{2}\\/.*");

	final Pattern p_dots = Pattern.compile("(.*)\\.(.*)\\/");

	final Pattern p_base64 = Pattern.compile("Encoding: base64", Pattern.CASE_INSENSITIVE);// |Pattern.MULTILINE

	/**
	 * Will setup the next message into current message and set the havemesagewaiting flag if there is no more messages
	 * we will set current message to null and flag to false.
	 * @preconditions messageFromLine is the beginning of the email message and we have read it already
	 * 
	 */
	private void setupNextMessage() throws IOException {

		haveMessageWaiting = false;
		
		EMTEmailMessage nextmessage = new EMTEmailMessage(null);
		boolean temp_toread = true;

		// will read from the buffered reader and grab a message
		String line = emailFileHandle.readLine();
		String temps;

		// TODO: set buffers here
		// buffers
		String rec_path_SBuf = new 	String();
		String dateline_SBuf = new String();
		String s_mid_SBuf = new String();
		Hashtable cclist = new Hashtable();

		Matcher m, n, o;
		StringBuffer header2 = new StringBuffer(4096);
		header2.append(messageFromLine +"\n");

		if (line != null) {

			line = line.replaceAll("\\015", ""); // tr/\015//d;
			line = line.replaceAll("\\012", "");
		}

		while (line != null) {

			// if($_ =~ /^From (.*):(.*):(.*)/ || $once eq "true")
			m = p_topfrom.matcher(line);
			if (m.matches()) {
				haveMessageWaiting = true;
//				System.out.println("matched begin new email");
				// need to grab emailfrom line and set new one and inc seen
				messageFromLine = new String(line);
				// move buffers into object

				nextmessage.setReceivedLine(rec_path_SBuf);
				ArrayList rcptsList = new ArrayList();
				Enumeration enm = cclist.keys();

				while (enm.hasMoreElements()) {
					rcptsList.add(enm.nextElement());
				}
				nextmessage.setRcptsList(rcptsList);
				nextmessage.setRawBODY(header2.toString());
				nextmessage.setSize(header2.length());
				nextmessage.setDate(dateline_SBuf);
				currentMessage = nextmessage;
				seenSoFar++;
				return;
				// we print out old info since we are beginning nect email
				// .if (seenSoFar >= 0 || (seenSoFar == 1 && !line.matches(".*MAILER-DAEMON.*"))) {

				// .output(s_filein);
				// .}
				// . count++;

				// if (m.groupCount() > 1) {
				// realdate = m.group(1).trim() + ":" + m.group(2).trim();

			}// end setting up next message by matching next from line
			// .fromline = new String(realdate);//incase we need to see the
			// fromemail here
			// .realdate = realdate.replaceFirst("^.*@", "@");//repalce all
			// till at
			// .realdate = realdate.replaceAll("^[\\S\\@]+ ", "");//clean
			// out
			// emails
			// .realdate = realdate.replaceAll("\\s+", " ");//clean out
			// double space
			// .realdate = realdate.replaceFirst("^\\s-\\s", "");//clean
			// out
			// slash
			// when no
			// email

			/***********************************************************************************************************
			 * // DO INSERT STUFF HERE CAUSE WE ARE GOING TO START ANOTHER MESSAGE
			 **********************************************************************************************************/
			// new email start

			header2.append(line);

			while (true) {
				// idea is to consolidate lines whcih begin with XXX:
				// something
				// and wrap to next line space space seomthing2
				// save to temp space

				temps = new String(line);
				while (true) {
					// # Begin slurping body here.
					line = emailFileHandle.readLine();
					// append to the header save part
					header2.append(line + "\n");
					if(line!=null){
					line = line.replaceAll("\\015", ""); // clean out cR
					line = line.replaceAll("\\012", ""); // clean out lf
					}
					
					
					
					
					
					o = body_div.matcher(line); // check if should end
					if (!o.matches()) {
						// msg = line;
						// msg = line2; //? +\n //might be beginning of
						// the body
						break;
					}
					temps = temps.concat(line);

				}// end inner while(true) ie one line of header data
				// 2004
				n = p_topfrom.matcher(line);
				if (n.matches()) {// ???2004
					// if(start == 0 )
					// start = header2.length() - line.length();
					break;
				}
				// not sure how to translate

				m = rec_line.matcher(temps);
				if (m.matches()) {
					rec_path_SBuf += m.group(1) + "\t";
					// !!header += temp +"\n";
					// System.out.println("got recieved line" +
					// rec_path);
					continue;
				}
				// date tag
				m = p_date.matcher(temps);
				if (m.matches()) {
					// System.out.println("in date processing");
					// !!header +=temp+"\n";
					dateline_SBuf = m.group(1).trim() + ":" + m.group(2);
					if (dateline_SBuf.indexOf(',') < 0) {
						dateline_SBuf = dateline_SBuf.trim();
						int num = dateline_SBuf.indexOf(' ');
						if (num > 0)
							dateline_SBuf = dateline_SBuf.substring(0, num) + "," + dateline_SBuf.substring(num);// want
						// space
						// after
						// comma
					}
					// System.out.println(datel);
					continue;
				}
				// caontent type
				m = p_content.matcher(temps);
				if (m.matches()) {
					// System.out.println("content type here");
					// !!header+=temp+"\n";
					nextmessage.setType(m.group(1).trim());
					n = p_midstat.matcher(temps);
					if (n.matches()) {
						nextmessage.setFlags(n.group(1));
						continue;
					}
					n = p_midmsgid.matcher(temps);
					if (n.matches()) {
						s_mid_SBuf = n.group(1);
						continue;
					}
					// ($boundary = $_) =~
					// s/.*boundary=\"([^\"]*)\".*/$1/i;
					// $boundary = "" if ($boundary eq $_);
					continue;
				}// end content types

				// subject line
				m = p_subject.matcher(temps);
				if (m.matches()) {
					String subject = new String(m.group(1)); // save the subject line
					// want to count re's
					int recount = 0;
					n = p_recount.matcher(subject); // look for re's
					if (n.find()) {
						recount++;
						while (n.find())
							recount++;
						subject = n.replaceAll("");// will replace all
						// re's so clean
						// subjet

					}
					int fcount = 0;
					o = p_fwdcount.matcher(subject); // look for re's
					if (o.find()) {
						fcount++;
						while (o.find())
							fcount++;

						subject = o.replaceAll("");// will replace all
						// re's so clean
						// subjet
						// System.out.println(fcount + " " +s_subject);
					}
					nextmessage.setSubject(subject.trim());
					nextmessage.setReCOUNT(recount);
					nextmessage.setFWDCount(fcount);

					continue;
				}// end subject matching

				// DONE: have combined the patterns to make it go
				// faster.
				// FIX ME!! no idea on how to combine/fix and make it
				// work :)
				// m = p_to.matcher(temp);
				// n = p_cc.matcher(temp);
				m = p_rcpt.matcher(temps);
				if (m.matches())// m.matches() || n.matches() ||
				// o.matches())
				{

					String temp23[] = m.group(2).trim().split("[<, >]");
					for (int i = 0; i < temp23.length; i++) {
						m = p_quotes.matcher(temp23[i]);
						if (temp23[i].indexOf('@') > 0 && !m.matches()) {
							String rcpt1 = temp23[i].replaceAll("\\[|\\]|\\(|\\)|<|>", "");

							cclist.put(rcpt1, "1");
						}
					}

					continue;
				}

				// delivered-to line
				m = p_delto.matcher(temps);
				if (m.matches()) {
					// s_delto = m.group(1);
					// need to weed out multiple emails here
					String temp23[] = m.group(1).split("[<, >]");
					for (int i = 0; i < temp23.length; i++) {
						m = p_quotes.matcher(temp23[i]);
						if (temp23[i].indexOf('@') > 0 && !m.matches())
							cclist.put(temp23[i], "1");
						// dellist.put(temp23[i], "1");
					}

					continue;
				}

				// from: line
				m = p_fromline.matcher(temps);
				if (m.matches()) {
//					System.out.println("*"+temps+"*");
					String temp2 = new String();
					// !!header += temp + "\n";
					if (m.groupCount() > 0)
						temp2 = m.group(1).trim();// stuff after the
					// from: ...
					// SHOULD CHECK (NEW) IF HAS @ SIGN
					if (temp2.indexOf('@') > 0) {
						// cleaning up
						// NOTE: SOME EMAIL DONT HAVE <...@...>
						if (temp2.indexOf('<') < 0) {

							if (temp2.indexOf(' ') < 0) {
								temps = "<" + temp2 + ">";
							} else {

								// need to insert into the string
								int num = temp2.indexOf('@');
								int b = temp2.indexOf(' ', num);// first
								// space
								// after
								// the at
								// sign
								if (b == -1)
									temp2 += ">";
								else if (b == temp2.length())
									temp2 = temp2.substring(0, b - 1) + ">";
								else
									temp2 = temp2.substring(0, b - 1) + ">" + temp2.substring(b + 1);// temp2.setCharAt(b,'>');

								b = (temp2.substring(0, num)).lastIndexOf(' ');

								if (b == -1)
									temp2 = "<" + temp2;
								else if (b == 0)
									temp2 = "<" + temp2.substring(1);
								else
									temp2 = temp2.substring(0, b - 1) + "<" + temp2.substring(b + 1);// temp2.setCharAt(b,'<');
							}
						}

						temp2 = temp2.replaceFirst("\\(.*\\)$", "");
						temp2 = temp2.replaceFirst("^.*<", "");
						temp2 = temp2.replaceFirst("^.*\"", "");
						temp2 = temp2.replaceFirst("^.*'", "");
						temp2 = temp2.replaceFirst("^.*:", "");
						temp2 = temp2.replaceFirst(" \\(.*\\)$", "");
						temp2 = temp2.replaceFirst(">.*$", "");

						// check if we have an email address here
						// and not delete by mistake
						if (temp2.indexOf('@') > 0) {
							n = p_address1.matcher(temp2);
							if (n.matches()) // NEED THIS OR WILL NOT
							// WORK>
							// System.out.println(n.groupCount());
							{
								nextmessage.setFromName(n.group(1).trim().toLowerCase());
								nextmessage.setFromDomain(n.group(2).trim().toLowerCase());
								nextmessage.setFromEmail(nextmessage.getFromName() + "@" + nextmessage.getFromDomain());// can
								// now to get zone
								nextmessage.setFromZN(nextmessage.getFromDomain().substring(
										(nextmessage.getFromDomain().lastIndexOf(".")) + 1));
							}
						}

					}// end if has @

					// make sure we had captured something
					if (nextmessage.getFromEmail().length() < 2 || temp2.indexOf('@') == -1) {

						// paste
						temp2 = new String(messageFromLine);// stuff after the
						// from: ...
						// SHOULD CHECK (NEW) IF HAS @ SIGN
						if (temp2.indexOf('@') > 0) {
							// cleaning up
							temp2 = temp2.replaceFirst("\\(.*\\)$", "");
							temp2 = temp2.replaceFirst("^.*<", "");
							temp2 = temp2.replaceFirst("^.*\"", "");
							temp2 = temp2.replaceFirst("^.*'", "");
							temp2 = temp2.replaceFirst("^.*:", "");
							temp2 = temp2.replaceFirst(" \\(.*\\)$", "");
							temp2 = temp2.replaceFirst(">.*$", "");

							// check if we have an email address here
							// and not delete by mistake
							if (temp2.indexOf('@') > 0) {
								n = p_address1.matcher(temp2);
								if (n.matches()) // NEED THIS OR WILL
								// NOT WORK>
								{

									nextmessage.setFromName(n.group(1).trim().toLowerCase());
									nextmessage.setFromDomain(n.group(2).trim().toLowerCase());
									nextmessage.setFromEmail(nextmessage.getFromName() + "@"
											+ nextmessage.getFromDomain());// can

									// .s_fname = n.group(1).trim().toLowerCase();
									// .s_fdom = n.group(2).trim().toLowerCase();
									// .s_femail = s_fname + "@" + s_fdom;//can
									// prob
									// leave
									// this
									// till
									// end.

									// now to get zone
									// s_fzn = s_fdom.substring((s_fdom.lastIndexOf(".")) + 1);
									nextmessage.setFromZN(nextmessage.getFromDomain().substring(
											(nextmessage.getFromDomain().lastIndexOf(".")) + 1));

								}
							} else// its unknown
							{
								// System.out.println("temp2:" + temp2);
								// s_femail = "XX@XX.X";
								// s_fdom = "XX";
								// s_fzn = "X";
								// s_fname ="X";
							}

							// end paste

						} else {

							temps = temps.replaceAll(" ", "");

							nextmessage.setFromName(temps.replaceFirst("From:", "").toLowerCase());
							nextmessage.setFromDomain("unknown.com");
							nextmessage.setFromEmail(temps + "@" + nextmessage.getFromDomain());
							nextmessage.setFromZN(".com");
							// s_femail = "XX@XX.X";
							// s_fdom = "XX";
							// s_fzn = "X";
							// s_fname ="X";

						}
					}

					continue;
				}// end from: line
				//System.out.print("2223");
				// status:
				m = p_status.matcher(temps);
				if (m.matches()) {
					// !!header +=temp+"\n";
					// s_stat = m.group(1);
					nextmessage.setFlags(m.group(1));
					continue;
				}

				// message-id:
				m = p_msgid.matcher(temps);
				if (m.matches()) {
					// !!header += temp + "\n";
					s_mid_SBuf = m.group(1);

					continue;
				}

				// mime version
				m = p_mimever.matcher(temps);
				if (m.matches()) {
					// !!header+=temp+"\n";
					continue;
				}

				// delivered-to
				m = p_delto.matcher(temps);
				if (m.matches()) {
					nextmessage.setForward(m.group(1));
					continue;
				}

				m = p_inrply.matcher(temps);
				if (m.matches()) {

					// do we even use this?
					// s_irpto =
					nextmessage.setRPLYTO(m.group(1).replaceAll("[<()>,]", ""));
					continue;
				}

				// xmail tag
				m = p_xmailer.matcher(temps);
				if (m.matches()) {
					// !!header +=temp+"\n";
					// .s_xmail =
					nextmessage.setXMailer(m.group(1));
					// -- s_xmail = m.group(1).replaceAll(","," ");
					// //cant have commas, or wont insert.
					// -- s_xmail = s_xmail.replaceAll("'","");
					// -- s_xmail = s_xmail.replaceAll("\"","");
					continue;
				}
				// clean out x-garbage
				m = p_xstuff.matcher(temps);
				if (m.matches()) {

					continue;
				}
				// rest of stuff...if want to add features add CODE
				// here.
				m = p_misc.matcher(temps);
				if (m.matches()) {
					// !!header +=temp+"\n";
					continue;
				}

				break;// this should fix missing emails in outlook.

			} // end outer while(true) i.e while processing header data

			// didnt match the from line so in middle of message
			// .else
			// .{

			//System.out.println("@@@'"+line+"'");

			// FILEZ?!?!?!?!?
			// .header2.append(line2 + "\n");

			// header2 += line2 + "\n";

			// SPEEDUP: use concat takes from 6-> down to one sec !!! on
			// 100K test file.
			// tight loop no idea why concat is better than +=
			// on header part above concat slows it down actually :)
			if (p_topfrom.matcher(line).matches())// !m.matches())
			{
				// line2 = new String(line);
				break;
			}
			// faster still: string buffer append!
			while (line.length() > 1) {

				if ((line = emailFileHandle.readLine()) == null)
					break;
				// header2= header2.concat(line +"\n");
				
				if(line!=null){
					line = line.replaceAll("\\015", ""); // clean out cR
					line = line.replaceAll("\\012", ""); // clean out lf
				}
				
				header2.append(line + "\n");
				// msg = msg.concat(line + "\n");
				// m = p_isprint.matcher(line);
				// found: a bit slower if next line is split up
				// check for next email:
				if (p_topfrom.matcher(line).matches())// !m.matches())
				{
					temp_toread = false;
					// line2 = new String(line);
					break;
				}
				// shlomo: march 2004 replaced below with above because
				// of no newlines on some formats.
				// will stop when see the from of next message
				// if(! ((Matcher)p_isprint.matcher(line)).matches()
				// )//!m.matches())
				// continue;

				// line = line.replaceFirst("\\015","");

			}// end while
			// msg += "\n";
			// header2+="\n";
			// msg = header2.substring(start);

			// header2 = header2.concat(msg);

			// temp_toread is a flag used to show if we should input a readline, or let the loop process it on the top
			// of the loop
			// becuase we are holding the beginning of the next message
			if (temp_toread) {
				line = emailFileHandle.readLine();
				//header2.append(line);
				if (line != null) {
					// line2 = new String(line);
					line = line.replaceAll("\\015", ""); // tr/\015//d;
					line = line.replaceAll("\\012", "");
				}
			}
			temp_toread = true;

		} // end while reading the file loop

		/*
		 * will need to setup buffers to message in order to have it ready when called TODO:
		 */
		
		if(!rec_path_SBuf.equals("")){
			
		haveMessageWaiting = true;
		nextmessage.setReceivedLine(rec_path_SBuf);
		ArrayList rcptsList = new ArrayList();
		Enumeration enm = cclist.keys();

		while (enm.hasMoreElements()) {
			rcptsList.add(enm.nextElement());
		}
		nextmessage.setRcptsList(rcptsList);
		nextmessage.setRawBODY(header2.toString());
		nextmessage.setSize(header2.length());
		nextmessage.setDate(dateline_SBuf);
		currentMessage = nextmessage;
		seenSoFar++;
		}
	}//end nextmessagesutip


/**
 * Main test section for running a small controlled test
 * @param args
 */
public static void main(String args[]){
	String filepath ="c:\\emaildata\\ai"; //sp04-3137"; //ai";
	System.out.println("welcome to the test program");
	
	int count=0;
	long now = System.currentTimeMillis();
	try{
	BufferedReader buf = new BufferedReader(new FileReader(filepath));
	
	
	
	MBOXIterator mbxinstance = new MBOXIterator(buf);
	System.out.println("going to loop now");
	while(mbxinstance.hasNext())
	{ 
		EMTEmailMessage emtmail  = (EMTEmailMessage)mbxinstance.next();
		count++;
		System.out.println(count + " size: " + emtmail.getSize() + " Subject: " + emtmail.getSubject());
		//System.out.println("****************************END");
		
		//if(count ==145){
		//	System.out.println(emtmail.getRawBODY());
		//}
			//System.exit(1);
	}
	}catch(FileNotFoundException fio)
	
	{
		fio.printStackTrace();
	}
	catch(EmailFileOpenException ioe){
		ioe.printStackTrace();
	}
	
	long totaltime = System.currentTimeMillis() - now;
	System.out.println("total messages found:" + count + "in " + Utils.millisToTimeString(totaltime));
}


/**
 * @see EmailMessageIterator#seenSoFar()
 */
public int seenSoFar() {
	return seenSoFar;
}



}
