/*
 * Created on Mar 2, 2005
 *
 */
package metdemo.MachineLearning;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import metdemo.Parser.EMTEmailMessage;
import metdemo.dataStructures.singleMessage;

/**
 * Learning class to compute the vip class
 * <P>
 * Given a set of user data, compute each user's vip table, and for a new email,
 * compute its vip setting.
 * 
 * @author Shlomo Hershkop
 */
public class VIPLearner extends MLearner {

    HashMap DataStorage; //map for each user, with map of each rcpt profile

    HashMap preInsertCache; //map of 2arraylists object called twolevelarray

    // based on messages recieved for each user (map
    // key).
    static final String spamCOUNT = "@SPAMCOUNT@";

    static final String goodCOUNT = "@NONSPAMCOUNT";

    public VIPLearner() {

        DataStorage = new HashMap();
        preInsertCache = new HashMap();
        super.setID("VIP Learner");
    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#flushModel()
     */
    public void flushModel() {
        DataStorage.clear();
        preInsertCache.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#save(java.lang.String)
     */
    public void save(String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        GZIPOutputStream out2 = new GZIPOutputStream(out);
        ObjectOutputStream s = new ObjectOutputStream(out2);

        s.writeObject(getID());
        s.writeObject(preInsertCache);
        s.writeObject(DataStorage);
        
        s.flush();
        s.close();

    }
    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
      return ML_VIPLearner;
    }
    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#load(java.lang.String)
     */
    public void load(String filename) throws Exception {
        FileInputStream in = new FileInputStream(filename);
        GZIPInputStream gin = new GZIPInputStream(in);

        ObjectInputStream s = new ObjectInputStream(gin);
        
        setID((String)s.readObject());
        preInsertCache = (HashMap) s.readObject();
        DataStorage = (HashMap) s.readObject();

        s.close();
    }

    /*
     * Idea in the training: <P> for a target user: <BR> if its a rec email,
     * store in a cache of seen emails <BR> else <P> change all msgs in the
     * cache to the current time stamp <P> put into wait queue <P> if any user
     * in the wait queue matches the current message user <P> update average
     * time and count info for target and other <P>
     * 
     * 
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#doTraining(metdemo.Parser.EMTEmailMessage,
     *      java.lang.String)
     */
    public void doTraining(EMTEmailMessage msgData, String Label) {

        //extract this user's hashmap from the datastorage

        String fromUser = msgData.getFromEmail();
        ArrayList rcptArray = msgData.getRcptList();
        //first if it is a rcpt email just store in the cache
        //we have a prelevel cache list which means they are incoming emails
        // but waiting for first response to
        //tag correct timestamp so can easily estimate response time
        for (int i = 0; i < rcptArray.size(); i++) {
            //ignore self
            String rcpt = (String) rcptArray.get(i);

            if (rcpt.equals(fromUser)) {
                continue;
            }
            singleMessage single = new singleMessage(fromUser, msgData
                    .getUtime(), singleMessage.RECEIVE);
            //else see if we have data
            if (preInsertCache.containsKey(rcpt)) {

                twoLevelArray listinfo = ((twoLevelArray) preInsertCache
                        .get(rcpt));
                listinfo.preLevel.add(single);
                preInsertCache.put(rcpt, listinfo);
            } else {
                twoLevelArray listinfo = new twoLevelArray();
                listinfo.preLevel.add(single);
                preInsertCache.put(rcpt, listinfo);
            }

        }
        //We need to upate the sender side view
        //see if we have any info in the pre cache or wait queue
        //first flush the precache
        twoLevelArray listinfo = null;
        long currentstamp = msgData.getUtime();
        if (preInsertCache.containsKey(fromUser)) {

            listinfo = ((twoLevelArray) preInsertCache.get(fromUser));

            for (int i = 0; i < listinfo.preLevel.size(); i++) {

                singleMessage msg = (singleMessage) listinfo.preLevel.get(i);
                msg.datestamp = currentstamp;
                listinfo.waitList.add(msg);
            }
            preInsertCache.put(fromUser, listinfo);
        } else {
            //nothing else to do
            return;
        }
        int addGlobalTime = 0;
        int addGlobalCount = 0;
        HashMap userinfo = null;
        if (!DataStorage.containsKey(fromUser)) {
            //add in global tats for this user using its own name
            userinfo = new HashMap();
            statsInfo ginfo = new statsInfo();//global info
            userinfo.put(fromUser, ginfo);
            userinfo.put(spamCOUNT, new Integer(0));
            userinfo.put(goodCOUNT, new Integer(0));
            DataStorage.put(fromUser, userinfo);
        } else {
            userinfo = (HashMap) DataStorage.get(fromUser);
        }
        //now for each rcpt in the current email, see if we have a match int he
        // wait list, and update the individual records
        for (int i = 0; i < rcptArray.size(); i++) {
            //ignore self
            String rcpt = (String) rcptArray.get(i);
            //ignore self
            if (rcpt.equals(fromUser)) {
                continue;
            }

            for (int j = 0; j < listinfo.waitList.size(); j++) {
                singleMessage single = (singleMessage) listinfo.waitList.get(j);
                if (single.userName.equals(rcpt)) {

                    //see if we have any past info
                    statsInfo stats;
                    if (userinfo.containsKey(rcpt)) {
                        stats = (statsInfo) userinfo.get(rcpt);
                    } else {
                        stats = new statsInfo();
                    }
                    stats.counts++;
                    addGlobalCount++;
                    addGlobalTime += (currentstamp - single.datestamp);
                    stats.totaltime += (currentstamp - single.datestamp);
                    userinfo.put(rcpt, stats);
                    // need to get the average and update it
                }

            }

        }
        //add the spam counting
        if (MLearner.getClassNumber(Label) == MLearner.SPAM_CLASS) {
            Integer n = (Integer) userinfo.get(spamCOUNT);
            userinfo.put(spamCOUNT, new Integer(n.intValue() + 1));
        } else {
            Integer n = (Integer) userinfo.get(goodCOUNT);
            userinfo.put(goodCOUNT, new Integer(n.intValue() + 1));
        }

        //now to add to the user global info
        statsInfo globalinfo = (statsInfo) userinfo.get(fromUser);
        globalinfo.counts += addGlobalCount;
        globalinfo.totaltime += addGlobalTime;
        userinfo.put(fromUser, globalinfo);
        DataStorage.put(fromUser, userinfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see metdemo.MachineLearning.MLearner#doLabeling(metdemo.Parser.EMTEmailMessage)
     */
    static final int level = 8;

    public resultScores doLabeling(EMTEmailMessage msgData)
            throws machineLearningException {

        //we need to calculate the score for the specific sender rcpt
        //TODO: multiple rcpts.
        statsInfo globalstats = null;
        statsInfo localstats = null;
        HashMap userDB = null;
        int spamCount = 0;
        int goodCount = 0;
        String sender = msgData.getFromEmail();
        String rcpt = (String) msgData.getRcptList().get(0);
        if (DataStorage.containsKey(rcpt)) {

            userDB = (HashMap) DataStorage.get(rcpt);

            globalstats = (statsInfo) userDB.get(rcpt);

            spamCount = ((Integer) userDB.get(spamCOUNT)).intValue();
            goodCount = ((Integer) userDB.get(goodCOUNT)).intValue();

            if (userDB.containsKey(sender)) {

                localstats = (statsInfo) userDB.get(sender);

            } else {

                //no info on sender
                if (spamCount > goodCount) {
                    return new resultScores(MLearner.SPAM_LABEL,
                            (100 * spamCount / (spamCount + goodCount)), msgData
                                    .getMailref());
                }
                return new resultScores(MLearner.NOTINTERESTING_LABEL,
                        (100 * goodCount / (spamCount + goodCount)), msgData
                                .getMailref());

            }

        } else {

            // no info on rcpt!
            return new resultScores(MLearner.SPAM_LABEL, 0, msgData
                    .getMailref());
        }

        double avgNum = globalstats.counts / (1 + userDB.size() );//totalNum/size;
        // //3 for
        // self,
        // spamcount,goodcount
        double avgTime = globalstats.totaltime / (1 + userDB.size() );//totalTime/total;
        double local_avgTime = localstats.totaltime / (1+localstats.counts);
        //System.out.println("avgNum:"+avgNum+", avgTime="+avgTime);

        double factor1 = 40*(11 - level) / (double) 12;
        double factor2 = 40*(1 + level) / (double) 12;
        //for test
        //calculate the vip score
        double VIPscore = factor1 * (localstats.counts) / avgNum + factor2
                * avgTime / (local_avgTime + avgTime / 10);

        if (spamCount > goodCount) {
            return new resultScores(MLearner.SPAM_LABEL, (int) VIPscore,
                    msgData.getMailref());
        }
        return new resultScores(MLearner.NOTINTERESTING_LABEL, (int) VIPscore,
                msgData.getMailref());

    }

}

class twoLevelArray implements Serializable {

    public ArrayList preLevel; //used for the cache

    public ArrayList waitList; //used for waiting

    public twoLevelArray() {

        preLevel = new ArrayList();
        waitList = new ArrayList();

    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
       out.writeObject(preLevel);
       out.writeObject(waitList);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        preLevel = (ArrayList)in.readObject();
        waitList = (ArrayList)in.readObject();
    }
    
    
    
    
}

class statsInfo implements Serializable{

    public int totaltime;

    public int counts;

    statsInfo() {
        totaltime = 0;
        counts = 0;
    }

    statsInfo(int t, int c) {
        totaltime = t;
        counts = c;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(totaltime);
        out.writeInt(counts);
     }

     private void readObject(java.io.ObjectInputStream in) throws IOException,
             ClassNotFoundException {
         totaltime = in.readInt();
         counts = in.readInt();
     }

}