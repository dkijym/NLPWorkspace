/*
 * Created on Mar 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

/**
 * @author Shlomo
 *
 */
public class ThinDoubleArrayWrapper {

    double []data;
    
    public ThinDoubleArrayWrapper(double []incomingd)
    {
        data = incomingd;
    }
    
    public double[] get(){
        return data;
    }
    
}
