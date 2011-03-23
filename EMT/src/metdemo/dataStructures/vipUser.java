package metdemo.dataStructures;

import metdemo.CliqueTools.CliqueFinder.oneClique;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * @author Ryan Rowe June 23, 2006
 *
 */
/*
account with calculated avg comm time
*/
public class vipUser implements Serializable,Comparable<vipUser>{
	
	private String acct; //email account
	private int numEmails; //how many emails exchanged
	private int numResponses; //number of responses calculation is based on
	private double avgResponseTime; //avg response time
	//private double orderScore; //score based on average response orders (PROTOTYPE - NOT YET USED)
	private double responseScore; //score combining number of responses and avgResponsetime
	private int numCliques; //total number of cliques
	private int rawCliqueScore; //cliques weighted by size only
	private int weightedCliqueScore; //cliques weighted by size and avg time
	private double pageRank;
	private double socialScore = -1; //score combining all above statistics
	private ArrayList<oneClique> cliqueList; //saved cliques for this account
	
	public vipUser(String a, int ne, double art){
		acct = a;
		numEmails = ne;
		avgResponseTime = art;
		cliqueList = new ArrayList<oneClique>();
	}
	
	public vipUser(String a, int ne, int nr, double art){
		acct = a;
		numEmails = ne;
		numResponses = nr;
		avgResponseTime = art;
		cliqueList = new ArrayList<oneClique>();
	}
	
	public vipUser(userComm uc){
		acct = uc.acct;
		numEmails = uc.number;
		avgResponseTime = uc.time;
		cliqueList = new ArrayList<oneClique>();
	}
	
	public String getAcct(){
		return this.acct;
	}
	
	public int getNumEmails(){
		return this.numEmails;
	}
		
	public int getNumResponses(){
		return this.numResponses;
	}
	
	public int getNumCliques(){
		return this.numCliques;
	}
	
	public int getRawCliqueScore(){
		return this.rawCliqueScore;
	}
	
	public int getWeightedCliqueScore(){
		return this.weightedCliqueScore;
	}
	
	public double getAvgResponseTime(){
		return this.avgResponseTime;
	}
	
	/*public double getOrderScore(){
		return this.orderScore;
	}*/
	
	public double getResponseScore(){
		return this.responseScore;
	}
	
	public double getPageRank(){
		return this.pageRank;
	}
	
	public double getSocialScore(){
		return this.socialScore;
	}
	
	public ArrayList<oneClique> getCliqueList(){
		return this.cliqueList;
	}
	
	public void setAcct(String s){
		this.acct = s;
	}
	
	public void setNumEmails(int n){
		this.numEmails = n;
	}
	
	public void setNumResponses(int n){
		this.numResponses = n;
	}
	
	public void setNumCliques(int n){
		this.numCliques = n;
	}
	
	public void setRawCliqueScore(int n){
		this.rawCliqueScore = n;
	}
	
	public void setWeightedCliqueScore(int n){
		this.weightedCliqueScore = n;
	}
	
	public void setAvgResponseTime(double d){
		this.avgResponseTime = d;
	}
	
	/*public void setOrderScore(double d){
		this.orderScore = d;
	}*/
	
	public void setResponseScore(double d){
		this.responseScore = d;
	}
	
	public void setPageRank(double d){
		this.pageRank = d;
	}
	
	public void setSocialScore(double d){
		this.socialScore = d;
	}
	
	public void setCliqueList(ArrayList<oneClique> al){
		this.cliqueList = al;
	}

	public int compareTo(vipUser o) {
		if(this.socialScore<o.getSocialScore())
		{	return 1;	}
		else if (this.socialScore>o.getSocialScore()){
			return -1;	
		}
		return 0;
	}
}