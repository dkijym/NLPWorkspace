// Jon Sajdak

package metdemo;

import java.io.*;
import java.util.*;

public class Parser
{
  // name of filename to parse from

  private BufferedReader in;

  public Parser(String str)
  {
    filename = str;

  }
  private String filename;


  public Data readFile()
  {
    String input = "";
    int lineNum, fieldNum;
    lineNum = 0;
    fieldNum = 0;
    File myFile = new File(filename);
    Data myData = new Data();

    try
    {
      in = new BufferedReader( new FileReader(myFile) );
    }
    catch (FileNotFoundException e)
    {
      System.err.println( e );
      System.exit( 0 );
    }
    catch ( IOException e )
    {
      System.err.println( e );
      System.exit( 1 );
    }

    boolean EOF = false;

    // while theres file to be read, do some reading
    while(!EOF)
    {
      try
      {
        // read a whole line of input
        input = in.readLine();
        if(input != null)
        {
          fieldNum = 0;
          // split input by whitespace, feed into datastructure
          StringTokenizer st = new StringTokenizer(input);
          while (st.hasMoreTokens())
          {
            myData.storeValue(lineNum,fieldNum++,st.nextToken());
          }
        }
        else
        {
          // signal end of file
          EOF = true;
        }
        lineNum++;
      }
      catch ( EOFException e )
      {
        EOF = true;
      }
      catch ( IOException e )
      {
        System.err.println( e );
        System.exit( 1 );
      }
    }

    // close the file
    try
    {
      in.close();
    }
    catch ( IOException e )
    {
      System.err.println( e );
      System.exit( 1 );
    }

    //return two dimensional array
    return myData;
  }
}







