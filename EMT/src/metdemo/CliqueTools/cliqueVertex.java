/**
 * 
 */
package metdemo.CliqueTools;

import java.util.ArrayList;

import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;

/**
 * @author shlomo
 *
 */
public class cliqueVertex extends UndirectedSparseVertex {

	private ArrayList<String> userlist;
	private ArrayList<String> subjectwords;
	
	public cliqueVertex(ArrayList<String> users) {
		super();
		userlist = users;
		subjectwords = new ArrayList<String>();
	}
	
	public cliqueVertex(ArrayList<String> users,ArrayList<String> words) {
		super();
		userlist = users;
		subjectwords = words;
	}
	
	
	public String getNames(){
		StringBuffer names = new StringBuffer();
		for(int i=0;i<userlist.size();i++){
			names.append(userlist.get(i) +"\n");
		}
		return names.toString();
	}
	
	public ArrayList<String> getUserList(){
		return userlist;
	}
	
	public int getSize(){
		return userlist.size();
	}
	
	public void setSubject(ArrayList<String> words){
		subjectwords = new ArrayList<String>(words.size());
		
		for(int i=0;i<words.size();i++){
			subjectwords.add(words.get(i));
		}
	}
	
	public String getSubject(){
		StringBuffer words = new StringBuffer();
		for(int i=0;i<subjectwords.size();i++){
			words.append(subjectwords.get(i) +"\n");
		}
		return words.toString();
	}
}
