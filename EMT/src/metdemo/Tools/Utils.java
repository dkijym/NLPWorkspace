

package metdemo.Tools;

import java.lang.String;
import java.util.Date;
import java.util.Hashtable;
//import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.PrintJob;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.MachineLearning.MLearner;
//import metdemo.Tables.Messages;

import java.sql.*;
import java.util.StringTokenizer;




//import drasys.or.prob.*;



/** 
 *  Utilities usefull for other classes. 
 *  list of utils:
 *   millisToTimeString(long millis)
 *long getMill(final String in) tkaes 0000-00-00 11:11:11
 *long getMill2(String in) takes 0000-00-00
 *double round(final double value, int decimalPlace)
 *pdf
 *kernelDensityEstimator
 *normal distrbution
 *bunch of weka stuff to suppport normal distributions.
 *popBody(mailref,jdbc handle,boolean takelock)
 */


public class Utils
{
    static final double CONST_SDP = Math.sqrt(2.0 * Math.PI);
    //static NormalDistribution nd = new NormalDistribution(0,1);
    static final double CONST2 = (1e-6)/2;
    static final double CONST3 = (1e-6)/(2*3);
    private static double MAX_ERROR = 0.01;
    public final static double m_Precision = 0.01;//??
    //weka stuff
    protected static final double SQRTH  =  7.07106781186547524401E-1;
    protected static final double MAXLOG =  7.09782712893383996732E2;
     
      
      /** Returns an ImageIcon, or null if the path was invalid. */
	   public static ImageIcon createImageIcon(String path,
	                                              String description)  {
	   		java.net.URL imgURL = ClassLoader.getSystemResource(path);
	   		if (imgURL != null) {
	   			return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imgURL), description);
	       }
	   		
	           System.err.println("In " + System.getProperty("user.dir")+ " - Couldn't find file: " +path);
	           return null;
	       
	   }
    /**
     *  converts from millisecs to string
     *  to hour:minute:second
     */

    public static String millisToTimeString(long millis)
    {
	long total = millis/1000;

	long hours = total/3600;
	long min = (total/60) - (hours * 60);
	long sec = total - (min * 60) - (hours * 3600);

	return((hours<10?"0":"") + hours + ":" + (min<10?"0":"") + min + ":" + (sec<10?"0":"") + sec);
    }
	
    
    
    public static double[] s1by24 = { 1, 24 }; //for xscale

	public static double[] x_24 = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
			10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0,
			21.0, 22.0, 23.0 };
    /**
       convert time from millisecond, to "2xxx-xx-xx "
       @param time in millisecond
    */
    public static String millisToTimeString2(long in){
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(in);
        Date d = c.getTime();
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")\
;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(d);
        return time;
    }

    /** change OOOO-OO-OO XX:XX:XX to Long value */
    public static long getMill(final String in)
    {
	String[] dt=in.split(" ");
	String[] date=dt[0].split("-");
	String[] time=dt[1].split(":");
	if(time[2].indexOf(".") != -1){
	    time[2] = time[2].substring(0,time[2].indexOf("."));
	}
	Calendar c=Calendar.getInstance();
	c.set(Integer.parseInt(date[0])
	      ,Integer.parseInt(date[1])-1
	      ,Integer.parseInt(date[2])
	      ,Integer.parseInt(time[0])
	      ,Integer.parseInt(time[1])
	      ,Integer.parseInt(time[2]));
		
	return c.getTimeInMillis();
	}

    /** change OOOO-OO-OO to Long value*/
    public static long getMill2(String in){
	String[] dt=in.split(" ");
	String[] date=dt[0].split("-");

	Calendar c=Calendar.getInstance();
	c.set(Integer.parseInt(date[0])
	      ,Integer.parseInt(date[1])-1
	      ,Integer.parseInt(date[2]));
		
	long ms = c.getTimeInMillis();
	return ms;
    }
    
    public static String reverseString(String input){
    	
    	char carray[] = input.toCharArray();
    	char tempc;
    	for(int j=0,i=carray.length-1;j<i;j++,i--){
    		tempc = carray[j];
    		carray[j] = carray[i];
    		carray[i] = tempc;
    	}
    	String result =  new String(carray);
    	
    	return result;
    	
    }
    
    
    
    
    /** found on net will round to lower*/
    public static double round(final double value, int decimalPlace) {
	double power_of_ten = 1;
	while (decimalPlace-- > 0)
	    power_of_ten *= 10.0;
	return Math.round(value * power_of_ten) 
	    / power_of_ten;
    }


    /** prob density function for gaussian (normal) distribution 
     * source: "Estimating Continuous Distributions in Baysian Classifiers 
     * G John, P Langley 
     * @author shlomo hershkop
     **/
    public static double pdf(final double mean, final double var, final double x)
    {
	//formula:
	//   a*b
	//a = 1/sqrt(2 PI var)
	//b = e^-(c)
	//c = (x-men)^2  / 2(mean)^2
	
	//shortcut log it all :)
	// log a + log b
	double c = (  ((x-mean)*(x-mean)) / ( 2.0*(var*var) ) );
	return (   Math.exp( Math.log(1.0 / (CONST_SDP *var)) -c));
    }

    /** Kernel Density Estimation
     *this impliments the kernel estimation guassian, ie multiple guassian models
     * the formula is in : Estimatin Continuous Distributions in Baysian Classifiers
     * by G John and P Langley
     * p(x) = 1/total  * sum(pdf(x,Xi,Varc)
     * varc = 1/sqrt(total)     
     * ASSUME: all weights =1
     *BASed on what weka impliments
     *XS is SORTED...SAVE CODING HERE
     **/
    
    public static double kernelDensityEstimator(double data, int m_NumValues, double m_Values[],double m_Weights[],double m_StandardDev,int start, double m_SumOfWeights)
    {
	double delta = 0, sum = 0, currentProb = 0;
	double zLower = 0, zUpper = 0;
	double weightSum = 0;


	for (int i = start; i < m_NumValues; i++) {
	    delta = m_Values[i] - data;
	    zLower = (delta - (m_Precision / 2)) / m_StandardDev;
	    zUpper = (delta + (m_Precision / 2)) / m_StandardDev;
	    currentProb = normalProbability(zUpper)
			   - normalProbability(zLower);
	    sum += currentProb * m_Weights[i];

	    weightSum += m_Weights[i];
	    if (currentProb * (m_SumOfWeights - weightSum) < sum * MAX_ERROR) {
		break;
	    }
	}
	for (int i = start - 1; i >= 0; i--) {
	    delta = m_Values[i] - data;
	    zLower = (delta - (m_Precision / 2)) / m_StandardDev;
	    zUpper = (delta + (m_Precision / 2)) / m_StandardDev;
	    currentProb = normalProbability(zUpper)
			   - normalProbability(zLower);
	    sum += currentProb * m_Weights[i];
	    weightSum += m_Weights[i];
	    if (currentProb * (m_SumOfWeights - weightSum) < sum * MAX_ERROR) {
		break;
	    }
	}
	return sum / m_SumOfWeights;


    }

    /**
       //	double range = xs[total-1] - xs[0];
       //double stdDev = Math.max( range/Math.sqrt(total) , CONST3);
       //double result2=0;
       double delta,zUpper,zLower,result =0;
       //	double Varc = 1.0 / Math.sqrt(total); 
       for(int i=0;i<total;i++)
       {
       delta = xs[i] - x;
       zLower = (delta - CONST2)/stdDev;
       zUpper = (delta + CONST2)/stdDev;
       //result2 += (nd.pdf(zUpper) - nd.pdf(zLower));

       result +=  (pdf(0,1,zUpper)- pdf(0,1,zLower));
       //pdf(x,xs[i],Varc);
		
		
       }
       //System.out.println(result);
       return (result / total);
       }
    */

    //paste in from weka:
    
  /**
   * Returns the area under the Normal (Gaussian) probability density
   * function, integrated from minus infinity to <tt>x</tt>
   * (assumes mean is zero, variance is one).
   * <pre>
   *                            x
   *                             -
   *                   1        | |          2
   *  normal(x)  = ---------    |    exp( - t /2 ) dt
   *               sqrt(2pi)  | |
   *                           -
   *                          -inf.
   *
   *             =  ( 1 + erf(z) ) / 2
   *             =  erfc(z) / 2
   * </pre>
   * where <tt>z = x/sqrt(2)</tt>.
   * Computation is via the functions <tt>errorFunction</tt> and <tt>errorFunctionComplement</tt>.
   *
   * @param a the z-value
   * @return the probability of the z value according to the normal pdf
   */
  public static double normalProbability(double a) { 

    double x, y, z;
 
    x = a * SQRTH;
    z = Math.abs(x);
 
    if( z < SQRTH ) y = 0.5 + 0.5 * errorFunction(x);
    else {
      y = 0.5 * errorFunctionComplemented(z);
      if( x > 0 )  y = 1.0 - y;
    } 
    return y;
  }

  /**
   * Returns the error function of the normal distribution.
   * The integral is
   * <pre>
   *                           x 
   *                            -
   *                 2         | |          2
   *   erf(x)  =  --------     |    exp( - t  ) dt.
   *              sqrt(pi)   | |
   *                          -
   *                           0
   * </pre>
   * <b>Implementation:</b>
   * For <tt>0 <= |x| < 1, erf(x) = x * P4(x**2)/Q5(x**2)</tt>; otherwise
   * <tt>erf(x) = 1 - erfc(x)</tt>.
   * <p>
   * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">
   * Java 2D Graph Package 2.4</A>,
   * which in turn is a port from the
   * <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A>
   * Math Library (C).
   *
   * @param a the argument to the function.
   */
  static double errorFunction(double x) { 
    double y, z;
    final double T[] = {
      9.60497373987051638749E0,
      9.00260197203842689217E1,
      2.23200534594684319226E3,
      7.00332514112805075473E3,
      5.55923013010394962768E4
    };
    final double U[] = {
      //1.00000000000000000000E0,
      3.35617141647503099647E1,
      5.21357949780152679795E2,
      4.59432382970980127987E3,
      2.26290000613890934246E4,
      4.92673942608635921086E4
    };
  
    if( Math.abs(x) > 1.0 ) return( 1.0 - errorFunctionComplemented(x) );
    z = x * x;
    y = x * polevl( z, T, 4 ) / p1evl( z, U, 5 );
    return y;
  }

  /**
   * Returns the complementary Error function of the normal distribution.
   * <pre>
   *  1 - erf(x) =
   *
   *                           inf. 
   *                             -
   *                  2         | |          2
   *   erfc(x)  =  --------     |    exp( - t  ) dt
   *               sqrt(pi)   | |
   *                           -
   *                            x
   * </pre>
   * <b>Implementation:</b>
   * For small x, <tt>erfc(x) = 1 - erf(x)</tt>; otherwise rational
   * approximations are computed.
   * <p>
   * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">
   * Java 2D Graph Package 2.4</A>,
   * which in turn is a port from the
   * <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A>
   * Math Library (C).
   *
   * @param a the argument to the function.
   */
  static double errorFunctionComplemented(double a) { 
    double x,y,z,p,q;
  
    double P[] = {
      2.46196981473530512524E-10,
      5.64189564831068821977E-1,
      7.46321056442269912687E0,
      4.86371970985681366614E1,
      1.96520832956077098242E2,
      5.26445194995477358631E2,
      9.34528527171957607540E2,
      1.02755188689515710272E3,
      5.57535335369399327526E2
    };
    double Q[] = {
      //1.0
      1.32281951154744992508E1,
      8.67072140885989742329E1,
      3.54937778887819891062E2,
      9.75708501743205489753E2,
      1.82390916687909736289E3,
      2.24633760818710981792E3,
      1.65666309194161350182E3,
      5.57535340817727675546E2
    };
  
    double R[] = {
      5.64189583547755073984E-1,
      1.27536670759978104416E0,
      5.01905042251180477414E0,
      6.16021097993053585195E0,
      7.40974269950448939160E0,
      2.97886665372100240670E0
    };
    double S[] = {
      //1.00000000000000000000E0, 
      2.26052863220117276590E0,
      9.39603524938001434673E0,
      1.20489539808096656605E1,
      1.70814450747565897222E1,
      9.60896809063285878198E0,
      3.36907645100081516050E0
    };
  
    if( a < 0.0 )   x = -a;
    else            x = a;
  
    if( x < 1.0 )   return 1.0 - errorFunction(a);
  
    z = -a * a;
  
    if (z < -MAXLOG) {
			if (a < 0)
				return (2.0);
			return (0.0);
		}
  
    z = Math.exp(z);
  
    if( x < 8.0 ) {
      p = polevl( x, P, 8 );
      q = p1evl( x, Q, 8 );
    } else {
      p = polevl( x, R, 5 );
      q = p1evl( x, S, 6 );
    }
  
    y = (z * p)/q;
  
    if( a < 0 ) y = 2.0 - y;
  
    if (y == 0.0) {
			if (a < 0)
				return 2.0;
			return (0.0);
		}
    return y;
  }


  /**
   * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
   * Evaluates polynomial when coefficient of N is 1.0.
   * Otherwise same as <tt>polevl()</tt>.
   * <pre>
   *                     2          N
   * y  =  C  + C x + C x  +...+ C x
   *        0    1     2          N
   *
   * Coefficients are stored in reverse order:
   *
   * coef[0] = C  , ..., coef[N] = C  .
   *            N                   0
   * </pre>
   * The function <tt>p1evl()</tt> assumes that <tt>coef[N] = 1.0</tt> and is
   * omitted from the array.  Its calling arguments are
   * otherwise the same as <tt>polevl()</tt>.
   * <p>
   * In the interest of speed, there are no checks for out of bounds arithmetic.
   *
   * @param x argument to the polynomial.
   * @param coef the coefficients of the polynomial.
   * @param N the degree of the polynomial.
   */
  static double p1evl( double x, double coef[], int N ) {
  
    double ans;
    ans = x + coef[0];
  
    for(int i=1; i<N; i++) ans = ans*x+coef[i];
  
    return ans;
  }

  /**
   * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
   * <pre>
   *                     2          N
   * y  =  C  + C x + C x  +...+ C x
   *        0    1     2          N
   *
   * Coefficients are stored in reverse order:
   *
   * coef[0] = C  , ..., coef[N] = C  .
   *            N                   0
   * </pre>
   * In the interest of speed, there are no checks for out of bounds arithmetic.
   *
   * @param x argument to the polynomial.
   * @param coef the coefficients of the polynomial.
   * @param N the degree of the polynomial.
   */
  static double polevl( double x, double coef[], int N ) {

    double ans;
    ans = coef[0];
  
    for(int i=1; i<=N; i++) ans = ans*x+coef[i];
  
    return ans;
  }

    
  /**
   * pop up a window and show the mail message associated with a mailref
   * 
   * @param parent
   *            relative window
   * @param premessage
   *            preamble message to show first
   * @param mailref
   *            the mailref to retrieve the message of
   * @param m_jdbcView
   *            message handle
   * @param getLock
   */
    static public final void popBody(Component parent,String premessage,String mailref,EMTDatabaseConnection m_jdbcView,boolean getLock)
    {
	
	String detail = premessage + "\n"+Utils.getBody(mailref,true,m_jdbcView);
	JTextArea textArea = new JTextArea(detail);
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);
	JScrollPane m_areaScrollPane = new JScrollPane(textArea);
	m_areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	m_areaScrollPane.setPreferredSize(new Dimension(550, 380));
	//ok we've setup the body text
	Object [] option_array = {"Print Me","Copy Clipboard","Close"};
	int c = JOptionPane.showOptionDialog(null, m_areaScrollPane,"Email Body",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE,null,option_array,option_array[2]);

	if(c==JOptionPane.YES_OPTION)
	    {
		Component c2 = parent.getParent();
		while (c2!=null && !(c2 instanceof Frame))
		    c2=c2.getParent();
		PrintJob pjob = c2.getToolkit().getPrintJob((Frame)c2,"Message Printing",null);
				
		if(pjob!=null)
		    {
			Graphics pg = pjob.getGraphics();
			if(pg!=null)
			    {
				printLongString (pjob, pg, detail);
				pg.dispose();
			    }
			pjob.end();

		    }
	    }
	else if(c==JOptionPane.NO_OPTION)
	    {
		StringSelection data2 = new StringSelection(detail);
		//for copying to clip
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();    
   
		clipboard.setContents (data2, data2);
	    }
	

    }


	static public final String getBody(final String mailref,final boolean keys,EMTDatabaseConnection m_jdbcView)
    {
	String detail = new String();			
	String data[][];
	try{
	    
	    synchronized (m_jdbcView)
		{
		   // m_jdbcView.getSqlData("select rcpt from email where mailref = '" + mailref + "'");
		    //data = m_jdbcView.getRowData();
	    	data = m_jdbcView.getSQLData("select rcpt,folder from email where mailref = '" + mailref + "'");
				
	    if(data.length>0){
	    	detail += "Folder: "+ data[0][1]+"\n";
	    }
	    	
	    	
	    for (int i=0; i<data.length; i++)
		{
		   detail += "Recipient: " + data[i][0] + "\n";
		}
	    // get received headers, attachments
	    	data = m_jdbcView.getSQLData("select received,hash,body,type from message where mailref='" + mailref + "'");
		    //System.out.println("done1");
		    //m_jdbcView.getSqlData("select received,hash,body,type from message where mailref=\"" + mailref + "\"");
		    //data = m_jdbcView.getRowData();
		}//end synchronized
				
	    /* 
	     * get received line 
	     * (there's an identical one for each row selected, so just get first)
	     */
	    if (data.length > 0)
		{
		    //no render
		    //detail += "Received: " + privatizeReceived(data[0][0]) + "\n";
		    detail += "Received: " + data[0][0] + "\n";
		}
	    else
		{
		    detail += "Received: <Not Found!>\n";
		}
				
	    detail += "\n";
				
	    for (int i=0; i<data.length; i++)
		{
		    if ((data[i][1] != null) && (data[i][1].length() > 0))
			{
			    detail += "Attachment Type: "  +data[i][3] +"\n";
			    detail += "Attachment: " + data[i][1] + "\n";
			}
		    detail += "****BEGIN*BODY**********\n" + data[i][2]+"\n*****END*BODY************\n";

		}
	    if(keys)
		{
		    synchronized (m_jdbcView)
			{
		    	data = m_jdbcView.getSQLData("select hotwords from email left join kwords on email.mailref=kwords.mailref where email.mailref='" + mailref + "'");
				   
			   // m_jdbcView.getSqlData("select hotwords from email left join kwords on email.mailref=kwords.mailref where email.mailref='" + mailref + "'");
			   // data = m_jdbcView.getRowData();
			}

	    
		    if ((data.length > 0) && (data[0].length > 0) && (data[0][0].length() > 0) && (!data[0][0].equals("null")))
			{
			    detail += "Keywords: \n";
					
			    String words = data[0][0];
			    StringTokenizer strTok = new StringTokenizer(words, "|");
			    while (strTok.hasMoreTokens())
				{
				    String tok = strTok.nextToken();
				    String[] parts = tok.split(":");
				    if (parts.length != 2)
					{
					    detail += "  <Malformed!>\n";
					}
				    else
					{
					    detail += "  " + parts[0] + " (" + parts[1] + ")\n";
					}
				}
			}
		}

	    return detail;

	}catch(SQLException ex)
	    {
		System.out.println("ex123"+ex);
		//JOptionPane.showMessageDialog(this, "Error getting details from database: " + ex,"Problem",JOptionPane.ERROR_MESSAGE);
		return detail;}


		
    }


    //from java awt book found on line on orielly site
    // Print string to graphics via printjob
    // Does not deal with word wrap or tabs
   static public void printLongString (PrintJob pjob, Graphics pg, String s) {
	int pageNum = 1;
	int linesForThisPage = 0;
	int linesForThisJob = 0;
	// Note: String is immutable so won't change while printing.
	if (!(pg instanceof PrintGraphics)) {
	    throw new IllegalArgumentException ("Graphics context not PrintGraphics");
	}
	StringReader sr = new StringReader (s);
	LineNumberReader lnr = new LineNumberReader (sr);
	String nextLine;
	int pageHeight = pjob.getPageDimension().height;
	Font helv = new Font("Helvetica", Font.PLAIN, 12);
	//have to set the font to get any output
	pg.setFont (helv);
	FontMetrics fm = pg.getFontMetrics(helv);
	int fontHeight = fm.getHeight();
	int fontDescent = fm.getDescent();
	int curHeight = 0;
	try {
	    do {
		nextLine = lnr.readLine();
		if (nextLine != null) {         
		    if ((curHeight + fontHeight) > pageHeight) {
			// New Page
			System.out.println ("" + linesForThisPage + " lines printed for page " + pageNum);
			pageNum++;
			linesForThisPage = 0;
			pg.dispose();
			pg = pjob.getGraphics();
			if (pg != null) {
			    pg.setFont (helv);
			}
			curHeight = 0;
		    }
		    curHeight += fontHeight;
		    if (pg != null) {
			pg.drawString (nextLine, 0, curHeight - fontDescent);
			linesForThisPage++;
			linesForThisJob++;
		    } else {
			System.out.println ("pg null");
		    }
		}
	    } while (nextLine != null);
	} catch (EOFException eof) {
	    // Fine, ignore
	} catch (Throwable t) { // Anything else
	    System.out.println("problem with longstring\n " + t);
	}
    }

   static final long ONE_HOUR = 60 * 60 * 1000L;
   /**
    * Assume that the second is later
    * @param earlyDate
    * @param laterDate
    * @return
    */
   public static long daysBetween(Date earlyDate, Date laterDate){
     return ( (laterDate.getTime() - earlyDate.getTime() + ONE_HOUR) / 
                   (ONE_HOUR * 24));
      }   

   /**
    * retunr the date roolbacked by this interval of days
    * @param startdate
    * @param number
    * @return
    */
   public static final String backIntervalFunction(String startdate,int number)
   {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try{
  Date d = sdf.parse(startdate);
   d = new Date(d.getTime() - number *86400000 );
   	
   	return sdf.format(d);
   	
    }catch(ParseException p){
    	System.out.println(p);
    	return null;
    }
   }
   
   /** ******************************* */
	/* the part for using stop words */
	/** ******************************* */

	//replace the stopwords in old by r.
	//r can be * or nothing
	//then later can ingore r when do accounting about words
	static public final String replaceStop(final String old,final Hashtable m_hash_stopwords) {
		if (old.length() < 1)
			return old;

		

		StringTokenizer st = new StringTokenizer(old, MLearner.tokenizer, true);
		// " \t\n\r\f"+".,;?!'`~", true);
		StringBuffer s = new StringBuffer();
		while (st.hasMoreTokens()) {
			String t = st.nextToken();

			if (!m_hash_stopwords.containsKey(t))
				s.append(t);
			else {//need to also forget next empty spot after dropped
				// word...better for us
				if (st.hasMoreTokens())
					if ((st.nextToken()).equals("\n"))
						s.append("\n");

			}

		}

		return s.toString();
	}

	static SimpleDateFormat formatYEAR = new SimpleDateFormat("yyyy-MM-dd");	
	/** turns sql date into string */
	
	public static final  String mySqlizeDate2YEAR(Date date) {
		return formatYEAR.format(date);
	}
	/** turns sql date into regular date */
	public static final Date deSqlizeDateYEAR(String s) {
	try {
			return formatYEAR.parse(s);
		} catch (ParseException pe) {
			System.err.println("parse error for sql style string" + pe);
			return null;
		}
	}
	/** turns sql date into regular date */
	public static final int getSQLyear(String s) {
	try {
			return Integer.parseInt(s.substring(0,4));
		} catch (NumberFormatException pe) {
			System.err.println("parse error for year sql style string" + pe);
			return 0;
		}
	}
	public static final int getSQLmonth(String s) {
		try {
				return Integer.parseInt(s.substring(5,7));
			} catch (NumberFormatException pe) {
				System.err.println("parse error for month sql style string" + pe);
				return 0;
			}
		}
	static SimpleDateFormat formatYEARTIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Date deSqlizeDateYEARTIME(String s)
    {
		
		try{
		   return formatYEARTIME.parse(s);
		}
		catch(ParseException pe){
		    System.err.println("parse error for sql style string"+pe);
		    return null;
		}

		
    }
	
//	*****************************
	// Compute Levenshtein distance
	//*****************************

	public static final int LevenshteinDistance(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		// Step 1

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4

			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5

				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}

				// Step 6
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);

			}

		}

		// Step 7

		return d[n][m];

	}

	/** return the min of 3 */
	static int Minimum(int a, int b, int c) {
		int mi;

		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;

	}

	//print out time normally
	static public String stringTime(final long now){
	    
	    long timen = now/1000;
	    String returns = new String((timen/3600) + ":");
	    timen = timen%3600;
		return returns+((timen / 60)+ ":" + (timen % 60));
	}
	
	/**
	 * Pure java method for performaning rm -rf
	 * @param startDirectory where to start the crawl
	 * @param startpattern if we want to include specific start patterns
	 * @param endpattern if we want to look for specific end patterns
	 * @param includeAll include all files found
	 * @param recurse if we want to crawl or only look local
	 * @return
	 */
static public  boolean SystemIndependantRecursiveDeleteMethod(final String startDirectory,final String startpattern,final String endpattern, boolean includeAll, boolean recurse){
		
		
		File currentdir = new File(startDirectory);//System.getProperty("user.dir"));
		if(currentdir.isDirectory()){}
		else{
			currentdir = currentdir.getParentFile();
		}
		//we know we are at a direct
		
		File []filesindir = currentdir.listFiles();
		for(int i=0;i<filesindir.length;i++){
			//need to check if we want to go recursive
			if( recurse && filesindir[i].isDirectory()){
				SystemIndependantRecursiveDeleteMethod( startDirectory + File.separator + filesindir[i].getName(), startpattern, endpattern,includeAll,recurse);
			}
			//either we are including all files or specific patterns
			if(includeAll){
				if(filesindir[i].delete()){
					System.out.println("removed: " + filesindir[i]);
				}
			}
			else {
				if(startpattern!=null){
			
					if(filesindir[i].getName().toLowerCase().startsWith(startpattern)){
						if(filesindir[i].delete()){
							System.out.println("removed: " + filesindir[i]);
						}
					}
				}
			//now lto look for end patterns
				if(endpattern!= null){
					if(filesindir[i].getName().toLowerCase().endsWith(endpattern)){
						if(filesindir[i].delete()){
							System.out.println("removed: " + filesindir[i]);
					}
				}
				
				}
		}
		}
		return true;
	}
	
	
  /* public static void main(String args[])
   {
   	System.out.println("testing something");
   	
   	System.out.println(backIntervalFunction("2004-05-01",7));
   	
   	
   }
   */
   
   
   
}//end class utils










