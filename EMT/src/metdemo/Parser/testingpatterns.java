/**
 * 
 */
package metdemo.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shlomo
 *
 */
public class testingpatterns {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern p_address1 = Pattern.compile("^.*?([^\"'\\s<]*)@([^>\\s'\"\\);]*).*$");//("^.*[<]?(\\S+)\\@(\\S+)[>]?.*$");//"^\\s*([^\\s]*)\\@([^\\s]*)\\s*$"//  ,Pattern.CASE_INSENSITIVE);
		
		String test1 = new String("\"'Vern Paxson (vern@icir.org)'\" <vern@icir.org>");
		String test2 = new String("\"Cng-All@Cs. Columbia. Edu\" <cng-all@cs.columbia.edu>");
		String test3 = new String(" \"Andrew T. Campbell\" <campbell@comet.columbia.edu>");
		
		//"Comet@Comet. Columbia. Edu" <comet@comet.columbia.edu>,
		System.out.println("Test of parser");
		
		System.out.println("This is what I get from :" + test1);
		
		Matcher matcherPerson2 = p_address1.matcher(test1);
		if (matcherPerson2.matches()) //NEED THIS OR WILL NOT
						 // WORK>
		{
		//	s_fname = matcherPerson2.group(1).trim().toLowerCase();
		//	s_fdom = matcherPerson2.group(2).trim().toLowerCase();
		
		
		System.out.println("woop: " + matcherPerson2.group(2).trim().toLowerCase());
		}
		
		System.out.println("This is what I get from :" + test2);
		
		matcherPerson2 = p_address1.matcher(test2.replaceAll("\".*\"", ""));
		if (matcherPerson2.matches()) //NEED THIS OR WILL NOT
						 // WORK>
		{
		//	s_fname = matcherPerson2.group(1).trim().toLowerCase();
		//	s_fdom = matcherPerson2.group(2).trim().toLowerCase();
		
		
		System.out.println("woop: " + matcherPerson2.group(2).trim().toLowerCase());
		}
		System.out.println("This is what I get from :" + test3);
		
		matcherPerson2 = p_address1.matcher(test3);
		if (matcherPerson2.matches()) //NEED THIS OR WILL NOT
						 // WORK>
		{
		//	s_fname = matcherPerson2.group(1).trim().toLowerCase();
		//	s_fdom = matcherPerson2.group(2).trim().toLowerCase();
		
		
		System.out.println("woop: " + matcherPerson2.group(2).trim().toLowerCase());
		}
	}

}
