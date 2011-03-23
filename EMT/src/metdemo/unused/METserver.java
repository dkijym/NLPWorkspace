
import java.util.*;
import java.io.*;

/**
 * METserver.java
 *-parses the data stored in the MET Server and calculates the
 * corresponding metrics.
 *
 * Created: Tue Feb 12 02:18:34 2002
 *
 * @author Edwing A Padilla
 * @version 1.0
 */

public class METserver
{

  private BufferedReader in;
  private Vector clientData;
  private Vector serverStat;
  private String filename;

  public METserver(String file)
  {
    filename = file;
    clientData = new Vector();
    serverStat = new Vector();
    parse();
  }

  private void parse()
  {
    String input = "";
    File dataFile = new File(filename);

    try
    {
      in = new BufferedReader(new FileReader(dataFile));
    }
    catch(FileNotFoundException e)
    {
      System.err.println(e);
      System.exit(0);
    }
    catch(IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }


    boolean EOF = false;

    while(!EOF)
    {
      try
      {
        //read line of input
        input = in.readLine();
        //System.out.println("TEST input: " + input);

        if(input != null && input.trim().length() != 0)
        {
          StringTokenizer st = new StringTokenizer(input);
          Vector rows = new Vector();
          while(st.hasMoreTokens())
          {
            String temp = st.nextToken();
            System.out.println(temp);
            rows.add(temp);
          }// while

          clientData.add(rows);
        }// if
        else
        {
          EOF = true;  // end of file
        }


      }// try
      catch(EOFException e)
      {
        System.err.println(e);
        System.exit(1);
      }
      catch(IOException e)
      {
        System.err.println(e);
        System.exit(1);
      }
    }// while

    //close the file
    try
    {
      in.close();
    }
    catch(IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }

    System.out.println("Size of Data=" +clientData.size());  //test

  } //parse()


  public Vector getData()
  {
    return clientData;
  } //getData()

  public void calcMetrics()
  {
    int total = clientData.size();
    System.out.println("total ="+total);     //test
    Random gen = new Random();

    for(int i = 1; total > i; i++)
    {
      Vector temp = new Vector();
      temp = (Vector)clientData.get(i);
      //System.out.println("TEST: i=" + i + ", tempsize=" + (temp == null ? "null" : temp.size()+""));
      System.out.println("temp =" + temp.elementAt(4));    //test

      if(((String)(temp.elementAt(4))).equals("M"))
      {
        Vector metric = new Vector();
        metric.add(temp.get(0));                  //MD5
        metric.add(temp.get(1));                  //Incident Rate
        metric.add(temp.get(2));                  //Death Rate
        Integer num = new Integer(gen.nextInt(3) + 1);
        metric.add(num);                          //Prevalance

        double iRate = Double.valueOf((String)metric.get(1)).doubleValue();
        double prevalance = ((Integer)metric.get(3)).doubleValue();
        double hosts = 10.0;
        double viruses= (new Double(total)).doubleValue();
        double threat = (iRate + prevalance) / (hosts + viruses);
        metric.add(new Double(threat));            //Threat
        metric.add(temp.get(3));                   //Spread

        System.out.println("size of metric =" + metric.size());  //test
        serverStat.add(metric);
      }//if

    }//for


    System.out.println("Size in server =" +serverStat.size());

  } //calcMetrics()

  public Vector getMetrics()
  {

    return serverStat;

  } //getMetrics()

} // METserver






















