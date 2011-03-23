

import java.io.*;
import java.util.*;

/**
 * The class is for parsing file.
 *
 * @author Jessie Jie Song (js1727@columbia.edu)
 * @version 1.0
 */

public class JComposer
{
  String fileName;
  StringBuffer buf;

  public JComposer(String file)
  {
    fileName = file;
    compose();
  }

  private void compose()
  {
    try
    {
      buf=new StringBuffer("<begin>\n");
      Vector tagVec = new Vector();

      BufferedReader br = new BufferedReader(new FileReader(fileName));
      String line = br.readLine();
      StringTokenizer st = new StringTokenizer(line);

      while(st.hasMoreTokens())
      tagVec.add(st.nextToken());

      String input = br.readLine();
      while(input != null)
      {
        int i=0;
        //compose the xml message to Datawarehouse
        buf.append("<rec>");
        st = new StringTokenizer(input);
        while(st.hasMoreTokens())
        {
          String tag = (String)tagVec.elementAt(i);
          String data = st.nextToken();
          if(data.length()==8 && data.charAt(2)==':')     //time
	  buf.append("<"+tag+" ti>"+data+"</"+tag+">");
          else if(data.length()==10 &&data.charAt(4)=='-')   //date
	  buf.append("<"+tag+" d>"+data+"</"+tag+">");
          else
	  buf.append("<"+tag+" s>"+data+"</"+tag+">");
          i++;
        }
        buf.append("</rec>");

        input = br.readLine();
        //   input = null;
      }

      buf.append("<end>\n");
    }
    catch(IOException ioe)
    {
      System.err.println("Error! "+ioe);
      System.exit(0);
    }
  }
  public String getData()
  {
    return buf.toString();
  }

  public static void main(String[] args)
  {
    JComposer jc = new JComposer(args[0]);
    System.out.println(jc.getData());
  }

}





