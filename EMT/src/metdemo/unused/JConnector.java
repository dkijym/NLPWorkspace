/**
 * This class is in charge of connecting to server
 *
 * @author Jessie Jie Song (js1727@columbia.edu)
 * @version 1.0
 */


import java.io.*;
import java.util.*;
import java.net.*;

public class JConnector
{
  BufferedReader fromServer;
  PrintWriter toServer;
  Socket client;

  public JConnector(String serverName, int port)
  {
    try
    {
      client = new Socket(InetAddress.getByName(serverName),port);
      toServer = new PrintWriter(client.getOutputStream());
      fromServer = new BufferedReader(new
	InputStreamReader(client.getInputStream()));
    }
    catch(Exception e)
    {
      System.out.println("Exception: " + e);
    }

    //System.out.println("JC connect");

  }


  public String readLine() throws IOException
  {
    return fromServer.readLine();
  }

  public void write(String data)
  {
    toServer.print(data);
    toServer.flush();
  }

  public void writeLine(String data)
  {
    toServer.println(data);
    toServer.flush();
  }

  public void close()
  {
    //System.out.println("JC close()");
    try
    {
      toServer.close();
      fromServer.close();
      if(client!=null)
      client.close();

      System.out.println("Closed Connection");
    }
    catch(Exception e)
    {
      System.out.println("Error Closing: " + e);
    }
  }

  public static void main(String[] args)
  {
    JConnector jc = new JConnector( "172.16.1.1", 3000);

    while(true);

  }

}




































