package metdemo.dataStructures;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * capsule class for user name and incoming and outgoing levels.
 * 
 * @author Shlomo Hershkop
 * 
 */

public class userShortProfile implements Serializable {

    String emailname;

    String startDate;

    String endDate;

    int countSent = 0;

    int countRcpt = 0;

    static final int highLevel = 200;

    static final int midLevel = 100;

    static final int lowLevel = 5;

    
    public userShortProfile(String nameS, String startD, String endD, int countS, int countR) {

        emailname = nameS;

        if(emailname.indexOf('\'')>0 || emailname.indexOf('"')>0)
		{
        	emailname = emailname.replaceAll("'", "\\\\'");
        	emailname = emailname.replaceAll("\"","\\\\\"");
		}
        
        
        startDate = startD;

        endDate = endD;

        countSent = countS;

        countRcpt = countR;

    }

    public String toString() {
        return emailname;
    }

    
    public String getName(){
        return emailname;
    }
    
    public String getStartDate(){
        return startDate;
    }
    
    public String getEndDate(){
        return endDate;
    }
    
    public void setSentCount(int s){
        countSent = s;
    }
    
    
    public int getSentCount(){
        return countSent;
    }
    
    public void setRcptCount(int r){
        countRcpt = r;
    }
    
    
    public int getRcptCount(){
        return countRcpt;
    }
    
    public int getTotalCount(){
        return countSent + countRcpt;
    }
    
    /**
     * color chart as follows:
     *      L      m    H       rec
     *    ------------------------
     * s L| ltgry|gray|orange
     *   M| dkgry|whte|yellow
     *   H| pink|red|purple
     * @return
     */
    public Color getColor() {

        if (countSent <= lowLevel) {
            if (countRcpt <= lowLevel) {
                return Color.LIGHT_GRAY;
            }
            else if(countRcpt <= midLevel){
                return Color.gray;
            }
            return Color.ORANGE;
            
        } else if (countSent <= midLevel) {
            if (countRcpt <= lowLevel) {
                return Color.darkGray;
            }
            else if(countRcpt <= midLevel){
                return Color.white;
            }
            return Color.yellow;
            
        } else {
            if (countRcpt <= lowLevel) {
                return Color.pink;
            }
            else if(countRcpt <= midLevel){
                return Color.red;
            }
            return Color.magenta;
            // if

        }

    }
    
    private void writeObject(ObjectOutputStream out)
    throws IOException{
    	 out.writeObject(emailname);

    	 out.writeObject(startDate);

    	 out.writeObject(endDate);
    	   out.writeInt(countSent);
    	   out.writeInt(countRcpt);
    }

    
    
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException{
   	 emailname = (String) in.readObject();//out.writeObject(emailname);
startDate = (String) in.readObject();//	 out.writeObject(startDate);
endDate = (String) in.readObject();//	 out.writeObject(endDate);
	   countSent = in.readInt();//out.writeInt(countSent);
	   countRcpt = in.readInt();//out.writeInt(countRcpt);	
	}

    
    static final long ONE_HOUR = 60 * 60 * 1000L;
	static public long daysBetween(final Date d1, final Date d2){
	    return ( (d2.getTime() - d1.getTime() + ONE_HOUR) / 
	                  (ONE_HOUR * 24));
	     }   

}
