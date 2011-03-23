/**
 * JParser.java : parse the selected virus tuple to get local metrics
 *                from DWH
 *
 * @author Jessie Jie Song
 * @version 1.0
 */

import java.io.*;
import java.util.*;


public class JParser
{

  String fName, password, virusName;
  JConnector jc;
  Attachment jl;

  public JParser(String fileName, String pw, String v)
  {
    this(fileName, "172.16.1.1", 3000 , pw, v);

  }
  public JParser(String fileName, String serverName, int port, String pw, String v)
  {
    try
    {
      virusName = v;
      jc = new JConnector(serverName, port);
      password = pw;
      String output = jc.readLine();
      System.out.println(output);
      System.out.println("Virus Name: "+virusName);

      String input = "password local_metric\ngetsql\nlocal_metric\nselect distinct * from local_metric where MD5=\""+virusName+"\"\n";
      jc.write(input);

      System.out.println(input);
      //      while(jc.readLine()!=null){

      //jc.readLine();
      jc.readLine();
      String rec = jc.readLine();

      jl = new Attachment(rec);


      // System.out.println(jc.readLine());
      //    }
      jc.write("quit\n");
      jc.close();

    }
    catch(IOException e)
    {
      System.err.println("Error: "+e);
      System.exit(0);
    }
  }

  public Attachment getLocalMetric()
  {
    return jl;
  }


  public static void main(String[] args)
  {
    JParser m = new JParser(args[0], args[1], "");
  }

}











