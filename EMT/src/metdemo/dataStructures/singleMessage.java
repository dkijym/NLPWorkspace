/*
 * Created on Mar 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author shlomo
 * 
 * Represents a single flow from/to a particular user
 */
public class singleMessage implements Serializable {

    public final static int SEND = 0;
    public final static int RECEIVE = 1;

    //private String time;
    public int type;
    public long datestamp;
    public String userName = null;
    
    public singleMessage(String timestamp, int type) {
        //time=t;
        this.type = type;
        datestamp = Long.parseLong(timestamp);//Utils.deSqlizeDateYEARTIME(t);
    }
    
    public singleMessage(String user, long timestamp, int type) {
        //time=t;
        userName = user;
        this.type = type;
        datestamp = timestamp;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(userName);
        out.writeInt(type);
        out.writeLong(datestamp);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        userName = (String)in.readObject();
        type = in.readInt();
        datestamp = in.readLong();
    }

}