/*
 * Created on Jan 17, 2005
 *
 * Class to represent the spefic URL extracted
 */
package metdemo.dataStructures;

//import java.io.IOException;
//import java.io.Serializable;

/**
 * @author Shlomo Hershkop
 *
 */
public class LinkInfoDS /*implements Serializable*/ {

	/* the actual URL */
	private String link;
	/* sub link */
	//private String target;
	/* the label which is shown outside the url ie to the user */
	//private String label;
	/* what type of anchor is it */
	private int type;
	/* count number of time seen */
	private int count;
	
	
	/* types of links */
	public static final int ANCHOR = 0; //<a href....> ......</a>
	public static final int FORM_SUBMISSION =1;// <form ......   </form>
	public static final int EMAIL = 2;  
	public static final int FTP = 3;
	public static final int JAVASCRIPT = 4;//??
	
	public static final int AREA = 5;  //<area *>
	//AREA???
	public static final int STYLESHEET = 6;
	public static final int RELATED_LINK = 7; //<LINK .......>
	public static final int IMAGE = 8;		//<img.....>
	public static final int APPLICATION = 9;
	public static final int OTHER = 10;
	public static final int GOPHER = 11;
	public static final int FREE_WEB = 12;
	
/*	private void writeObject(java.io.ObjectOutputStream out)
    throws IOException{
		out.writeObject(link);
		//out.writeObject(target);
		//out.writeObject(label);
		out.writeInt(type);
		out.writeInt(count);
		
	}

	private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException{
		link = (String)in.readObject();
		//target = (String) in.readObject();
		//label = (String)in.readObject();
		type = in.readInt();
		count = in.readInt();
	}
	
	*/
	public LinkInfoDS()
	{
		//link = new String();
		//label = new String();
		count = 0;
	}
	
	public LinkInfoDS(int stype)
	{
		this();
		type = stype;
	}
	
	public LinkInfoDS(String slink,String starget,String slabel, int stype){
		setLink(slink);
		//target = starget;
		//label = slabel;
		type = stype;
		count = 1;
	}
	
	public final void setLink(String lin){
		
		if(lin.endsWith("/"))
		{
			lin = lin.substring(0,lin.length()-1);
		}
		
		
		//clean out http://www.
		if(lin.startsWith("http://")){
				lin = lin.substring(7);
		}else if(lin.startsWith("https://")){
			
			lin = lin.substring(8);
		}
		
		if(lin.startsWith("www")){
			int n= lin.indexOf(".");
			if(n>0){
				lin = lin.substring(n+1);
			}
			else{
				System.out.println("!!!"+lin);
			}
		}
		
		//now to make ending clean
		int n = lin.lastIndexOf("/");
		if(n > 0 && n < lin.length()-1){
			//need to save somewher e TODO:
			String ending =lin.substring(n); 
			if( ending.indexOf("?")>=0 || ending.indexOf("=")>=0 ||ending.indexOf("&")>=0||ending.indexOf("#")>=0||ending.indexOf("!")>=0)
				{
					link = new String(lin.substring(0,n));
				}
			else
			{
				link = new String(lin);
			}
		}
		else
		{
			
			link = new String(lin);
		
		}
	}
	
	public final void setLink(String slink,String starget){
		setLink(slink);
		//target = starget;
	}
	
	
	
	public final void setLabel(String lab){
		//label = lab;
	}//
	
	public final String getLink(){
		return link;
	}
	
	public final String getLabel(){
		return null;//return label;
	}
	
	public final void setType(int t){
		type = t;
	}
	
	public final int getType(){
		return type;
	}
	
	public final void setTarget(String t){
		//target = t;
	}
	
	public final String getTarget(){
		return null;
		//return target;
	}
	
	public final String toString(){
		return "type:" + getType() + " count: "+count+" url:" + getLink();
	}
	/**
	 * Adds n to current count
	 * @param n
	 */
	public final void addToCount(int n){
		count+=n;
	}
	
	public final int getCount(){
		return count;
	}
	
	public final void setCount(int n){
		count = n;
	}
	
	public void addToCurrentLink(LinkInfoDS newlink){
		count += newlink.getCount();
		//label += " "+newlink.getLabel();
	}
}
