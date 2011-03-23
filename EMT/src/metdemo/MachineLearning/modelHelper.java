/*
 * Created on Nov 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.MachineLearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * @author Shlomo Hershkop
 * 
 * function to help manipulate models in EMT
 */
public class modelHelper {

    public static final int MODEL = 0;

    public static final int MOD_NUM = 1;

    /**
     * gets model information
     * 
     * @param name
     * @return
     */
    public static String fileInfo(String name) {

        String info = new String();
        File check = new File(name.trim());
        if (check.exists() != true) {
            return "File NOT Found!!\n";
        }

        try {
            info += "Name: " + name;
            info += "\nLast modified: ";
            if (check.lastModified() != 0L)
                info += new Date(check.lastModified()).toString();
            else
                info += "might be STILL OPEN";
            info += "\nLength : " + check.length() + " bytes";
            info += "\nHash Code of Path: " + check.hashCode();
            FileInputStream in = new FileInputStream(name.trim());
            GZIPInputStream in2 = new GZIPInputStream(in);
            ObjectInputStream s = new ObjectInputStream(in2);

            info += "\nType: " + (String) s.readObject();
            info += "\nFeatures: " + (String) s.readObject();
            info += "\nPerformance: " + (String) s.readObject();

            s.close();

            //	    info += "\nfrom model itself features: \n" + .getFeature();
            //info += "\nPerformance: " +
        } catch (Exception e) {
            info += e;
        }

        return info;
    }

    /**
     * method to load up the model from disk.
     * @param filename of the model to load
     * @return the model or null if not found
     */
    public static final MLearner goLoadModel(final String filename) {
        MLearner m_mlearner = null;

        String modeltype = new String();
        try {
            //need to open the models which are saved as zipped
            FileInputStream in = new FileInputStream(filename.trim());
            GZIPInputStream in2 = new GZIPInputStream(in);
            ObjectInputStream s = new ObjectInputStream(in2);
           
            modeltype = ((String) s.readObject()).toLowerCase();
            
            s.close();
            // System.out.println("going to load : '" + filename+ "' as..." + modeltype);
            //loadModel.setBackground(Color.yellow);
            //havemodel=false;
            System.out.println("loading...." + filename);
            if (modeltype.startsWith("naive bayes + ngram")) {
                m_mlearner = new NBayesClassifier(
                        NBayesClassifier.SUBTYPE_NGRAM, null);
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_NBAYES_NGRAM;

            } else if (modeltype.startsWith("naive bayes + t")) {
                m_mlearner = new NBayesClassifier(
                        NBayesClassifier.SUBTYPE_TXTCLASSIFIER, null);
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_NBAYES_TXTCLASS;

            } else if (modeltype.startsWith("ngram")) {
                m_mlearner = new NGram();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_NGRAM;
            } else if (modeltype.startsWith("limited")) {
                m_mlearner = new NGramLimited();
                m_mlearner.load(filename); //will beloading up a new model.
                //  model_num = MLearner.ML_LIMITEDNGRAM;
            } else if (modeltype.startsWith("link")) {
                m_mlearner = new LinkAnalysisLearner();
                m_mlearner.load(filename); //will beloading up a new model.
                //  model_num = MLearner.ML_LINKS;
            } else if (modeltype.startsWith("text")) {
                m_mlearner = new TextClassifier();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_TEXTCLASSIFIER;
            } else if (modeltype.startsWith("corr")) {
                m_mlearner = new CorModel();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_EWCOMBINATION;
            } else if (modeltype.startsWith("outlook")) {
                m_mlearner = new OutlookModel();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_OUTLOOK;
            } else if (modeltype.startsWith("coded")) {
                m_mlearner = new CodedModel_1();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_HARDCODED;
            } else if (modeltype.startsWith("pgram")) {
                m_mlearner = new PGram();
                m_mlearner.load(filename); //will beloading up a new model.
                // model_num = MLearner.ML_PGRAM;
            } else if (modeltype.startsWith("tfidf")) {
                m_mlearner = new Tfidf();
                m_mlearner.load(filename); //will beloading up a new model.
                //   model_num = MLearner.ML_TFIDF;
            } else if(modeltype.startsWith("vip")){
                m_mlearner = new VIPLearner();
                m_mlearner.load(filename);
            }
            else {
                System.out.println("Have been asked to load up : " + filename
                        + "\nwith type = " + modeltype);
                throw new Exception("Error with filename");
            }
        } catch (Exception e) {
            System.out
                    .println("problem in model loader");
            //only print if not filenot found
            if(!(e instanceof FileNotFoundException))
                e.printStackTrace();
            return null;
        }
        System.out.println("model threshold: " + m_mlearner.getThreshold());
        //return m_mlearner;
        //Vector results = new Vector(2);
        //results.add(m_mlearner);
        //results.add(new Integer(model_num));
        return m_mlearner;//results;
    }

}