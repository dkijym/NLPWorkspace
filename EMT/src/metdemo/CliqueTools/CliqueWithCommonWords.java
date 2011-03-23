/**
   This class is a simple object of cliques.
   A CliqeuWithCommonWords is a clique. 
   It includes its users and common words
   
   
   
   shlomo tried fixing it in 2007 by making it more oo
*/
package metdemo.CliqueTools;
import java.util.*;

public class CliqueWithCommonWords{
    private ArrayList<String> users;
    private ArrayList<String> commonWords;

    public CliqueWithCommonWords(){
	users = new ArrayList<String>();
	commonWords = new ArrayList<String>();
    }
    
    public void addCommonWord(String s){
    	commonWords.add(s);
    }
    
    public void addUser(String u){
    	users.add(u);
    }
    
    public String getUser(int i){
    	return users.get(i);
    }
    
    public String getCommon(int i){
    	return commonWords.get(i);
    }
    
    public int getNumCommon(){
    	return commonWords.size();
    }
    
    public int getNumUsers(){
    	return users.size();
    }
    
    public ArrayList<String> getAllUsers(){
    	return users;
    }
    
    public ArrayList<String> getAllCommon(){
    	return commonWords;
    }
    

}
