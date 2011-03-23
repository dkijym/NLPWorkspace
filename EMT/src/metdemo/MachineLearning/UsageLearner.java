/*
 * Created on Mar 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.MachineLearning;

import java.io.IOException;

import metdemo.Parser.EMTEmailMessage;

/**
 * @author crash
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UsageLearner extends MLearner {

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#flushModel()
     */
    public void flushModel() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#getIDNumber()
     */
    public int getIDNumber() {
       return MLearner.ML_UsageLearner;
    }
    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#save(java.lang.String)
     */
    public void save(String filename) throws IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#load(java.lang.String)
     */
    public void load(String filename) throws Exception {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#doTraining(metdemo.Parser.EMTEmailMessage, java.lang.String)
     */
    public void doTraining(EMTEmailMessage msgData, String Label) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see metdemo.MachineLearning.MLearner#doLabeling(metdemo.Parser.EMTEmailMessage)
     */
    public resultScores doLabeling(EMTEmailMessage msgData)
            throws machineLearningException {
        // TODO Auto-generated method stub
        return null;
    }

}
