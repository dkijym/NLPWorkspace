
import java.io.*;
import java.util.*;
import java.net.*;
/**
 * @author  Jessie Jie Song (js1727@columbia.edu)
 * @version 1.0
 */

public class JMet
{
  String fName, password;
  JConnector jc;
  JComposer composer;

  public JMet(String fileName, String pw)
  {
    this(fileName, "172.16.1.1", 3000 , pw);
  }

  public JMet(String fileName, String serverName, int port, String pw)
  {
    try
    {
      composer = new JComposer(fileName);
      jc = new JConnector(serverName, port);
      password = pw;
      String output = jc.readLine();
      System.out.println(output);
      String input = "password "+pw+"\nquickinsert\n"
	+pw+"\n"+composer.getData()+"\nquit\n";
      jc.write(input);

      System.out.println(input);

      System.out.println(jc.readLine());

      jc.close();

    }
    catch(IOException e)
    {
      System.err.println("Error: "+e);
      System.exit(0);
    }
  }


  public static void main(String[] args)
  {
    JMet m = new JMet(args[0], args[1]);
  }

}



/*public class Met
  {
  BufferedReader fromServer;
  PrintWriter toServer;
  String fileName;
  public static StringBuffer put;
  
  public Met(String f){
  fileName = f;
  }
 
  public void runClient(){
  put=new StringBuffer("<begin>\n");
  try{      
  String tag[] = new String[6];
  BufferedReader br = new BufferedReader(new FileReader(fileName));
  String line = br.readLine();
  StringTokenizer st = new StringTokenizer(line);
  int i=0;
  while(st.hasMoreTokens())
  tag[i++]= st.nextToken();
 
  boolean EOF = false;
  while(!EOF){
  String input = br.readLine();
  if(input != null ){
  i=0;
  //compose the xml message to Datawarehouse
  put.append("<rec>");
  st = new StringTokenizer(input);
  while(st.hasMoreTokens()){
  String data = st.nextToken();
  if(i == 3)     //time
  put.append("<"+tag[i]+" ti>"+data+"</"+tag[i]+">");
  else if(i==4)   //date
  put.append("<"+tag[i]+" d>"+data+"</"+tag[i]+">");
  else
  put.append("<"+tag[i]+" s>"+data+"</"+tag[i]+">");
  i++;
  }put.append("</rec>");
  }
  else{
  //end of file
  EOF = true;
  }  
  }
  put.append("<end>\n"); 
  // System.out.print(put+ " ");
  }catch(IOException ioe){
  System.err.println("Error! "+ioe);
  System.exit(0);
  }
    
  Socket client=null;
  String inputLine;
  try {
      
  client = new
  Socket(InetAddress.getByName("172.16.1.1"),3000);
	    
  System.out.println("Connected to: " +
  client.getInetAddress().getHostName());
 
  toServer = new PrintWriter(client.getOutputStream());
 
  fromServer = new BufferedReader(new
  InputStreamReader(client.getInputStream()));
  inputLine=fromServer.readLine();
  System.out.println("From Server: " + inputLine);
  System.out.println("Sending to server");
  long timer = System.currentTimeMillis();
  //insert data to DWH      
  toServer.println("password met\nquickinsert\nmet\n"+put+"quit\n");
  toServer.flush();
  System.out.println(put);
      
  System.out.println(fromServer.readLine());
  System.out.println(System.currentTimeMillis() - timer +"ms");
	   
  }
  catch(Exception e)
  {
  System.out.println("Exception: " + e);
  }
  finally
  {
  try{
  toServer.close();
  fromServer.close();
  if(client!=null)
  client.close();
 
  System.out.println("Closed Connection");
  }
  catch(Exception e){
  System.out.println("Error Closing: " + e);
  }
  }
  }
 
  public static void main(String args[])
  {
  Met app = new Met("data");
  app.runClient();
  }
  }
  
 
*/
