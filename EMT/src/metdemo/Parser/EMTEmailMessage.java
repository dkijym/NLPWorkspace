/*
 * Created on Nov 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Parser;

import java.util.ArrayList;

/**
 * @author Shlomo Hershkop
 * 
 * class to represent the EMT email message
 */
public class EMTEmailMessage {

	private String mailref;
	private String body;
	private String subject;
	private int numberRcpts;
	private int numberAttach;
	private String fromEmailAddress;
	private ArrayList rcpts;
	boolean isEmpty = true;
	private int length;
	private String label;
	private String fromName;
	private String fromDomain;
	private String fromZN;
	private String recName;
	private String recDomain;
	private String recZN;
	private String xMailer;
	private String dates;
	private String times;
	private String msgHash;
	private String flags;
	private String type;
	private String rplyTo;
	private String forward;
	private int reCount;
	private int fwdCount;
	private long timeADJ;
	private String folderName;
	private int GMONTH;
	private int GHOUR;
	private long utime;
	private char sLOC;
	private char rLOC;

	private String rawBODY;
	private String insertTime;

	private String receivedLine;
	
	
	public EMTEmailMessage(String mlrf) {
		mailref = mlrf;
		fromEmailAddress = new String();
	}

	public final String getMailref() {
		return mailref;
	}

	public void setFromName(String s) {
		fromName = s;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromDomain(String s) {
		fromDomain = s;
	}

	public String getFromDomain() {
		return fromDomain;
	}
	public void setFromZN(String s) {
		fromZN = s;
	}

	public String getFromZN() {
		return fromZN;
	}

	public void setRecName(String s) {
		recName = s;
	}

	public String getRecName() {
		return recName;
	}

	public void setRecDomain(String s) {
		recDomain = s;
	}

	public String getRecDomain() {
		return recDomain;
	}

	public void setRecZN(String s) {
		recZN = s;
	}

	public String getRecZN() {
		return recZN;
	}

	public final boolean isEmpty() {
		return isEmpty;
	}

	public final void setBody(String bdy) {
		if (bdy == null)
			body = "";
		else
			body = bdy;
		isEmpty = false;

	}

	public final void setBody(String bdy[]) {
		isEmpty = false;
		if (bdy == null || bdy.length==0)
			body = "";
		else {
			body = new String(bdy[0]);
			for (int i = 1; i < bdy.length; i++) {
				body += "\n" + bdy[i];
			}
		}
	}
	
	public final void setBody(String bdy[][]) {
		isEmpty = false;
		if (bdy == null || bdy.length==0)
			body = "";
		else {
			body = new String(bdy[0][0]);
			for (int i = 1; i < bdy.length; i++) {
				body += "\n" + bdy[i][0];
			}
		}
	}

	
	public final void setRawBODY(String rbody){
		rawBODY = rbody;
	}
	
	public String getRawBODY(){
		return rawBODY;
	}
	
	public final String getLabel() {
		return label;
	}

	public void setLabel(String labelclass) {
		label = labelclass;
	}

	public final String getBody() {
		return body;
	}

	public final void setSize(int len) {
		length = len;
	}

	public final int getSize() {
		return length;
	}

	public final void setFromEmail(String fromaddress) {
		fromEmailAddress = fromaddress;
	}

	public final String getFromEmail() {
		return fromEmailAddress;
	}

	/**
	 * Sets the subject of this email
	 * 
	 * @param sbjct
	 */
	public final void setSubject(String sbjct) {
		subject = sbjct;
	}

	/**
	 * 
	 * @return The email's subject string
	 */
	public final String getSubject() {
		return subject;
	}

	public void setNumberAttached(int n) {
		numberAttach = n;
	}

	public int getNumberAttach() {
		return numberAttach;
	}

	public final void setNumRcpts(int rcpts) {
		numberRcpts = rcpts;
	}

	public final int getRcptsNumber() {
		return numberRcpts;
	}

	public final void setRcptsList(ArrayList rcp) {
		//should maybe add to list
		rcpts = rcp; //??
	}

	public void setXMailer(String s) {
		xMailer = s;
	}

	public String getXMailer() {
		return xMailer;
	}

	public void setDate(String s) {
		dates = s;
	}

	public String getDate() {
		return dates;
	}

	public void setTime(String s) {
		times = s;
	}

	public String getTime() {
		return times;
	}

	public void setMsgHash(String s) {
		msgHash = s;
	}

	public String getMsgHash() {
		return msgHash;
	}

	public void setFlags(String s) {
		flags = s;
	}

	public String getFlags() {
		return flags;
	}

	public void setRPLYTO(String s) {
		rplyTo = s;
	}

	public String getRPLYTO() {
		return rplyTo;
	}

	public void setForward(String s) {
		forward = s;
	}

	public String getForward() {
		return forward;
	}

	public void setType(String s) {
		type = s;
	}

	public String getType() {
		return type;
	}

	public void setReCOUNT(int n) {
		reCount = n;
	}

	public void setFWDCount(int n){
		fwdCount = n;
	}
	
	public int getFWDCount(){
		return fwdCount;
	}
	
	public int getReCount() {
		return reCount;
	}

	/**
	 * Adds a rcpt to the list and increment number of RCPTS
	 * 
	 * @param rcpt
	 *            to be added
	 */
	public final void addRCPT(String rcpt) {
		if (numberRcpts == 0) {
			rcpts = new ArrayList();
		}

		numberRcpts++;

		//TODO: check that its not in already
		rcpts.add(rcpt);
	}

	/**
	 * Get the first RCPT
	 * 
	 * @return the email address of first set rcpt
	 */
	public final String getRCPT() {
		if (numberRcpts == 0) {
			return null;
		}

		return (String) rcpts.get(0);
	}

	public final ArrayList getRcptList() {
		return rcpts;
	}

	public void setGHour(int n) {
		GHOUR = n;
	}

	public int getGHour() {
		return GHOUR;
	}

	public void setGMonth(int n) {
		GMONTH = n;
	}

	public final int getGMonth() {
		return GMONTH;
	}

	public void setFolder(String fold) {
		folderName = fold;
	}

	public String getFolder() {
		return folderName;
	}

	public void setTimeADJ(long n) {
		timeADJ = n;
	}

	public final long getTimeADJ() {
		return timeADJ;
	}

	public final void setSenderLOC(String s) {
		sLOC = s.charAt(0);
	}

	public char getSenderLoc() {
		return sLOC;
	}

	public final void setRCPTLOC(String s) {
		rLOC = s.charAt(0);
	}

	public char getRCPTLoc() {
		return rLOC;
	}

	public void setUtime(long n) {
		utime = n;
	}

	public final long getUtime() {
		return utime;
	}

	public void setInsertTime(String s) {
		insertTime = s;
	}

	public String getInsertTime() {
		return insertTime;
	}

	public void setReceivedLine(String recline){
		this.receivedLine = recline;
	}
	
	public String getReceivedLine(){
		return receivedLine;
	}
	
	
}