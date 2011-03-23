/**
 * 
 */
package metdemo.DataBase;

/**
 * @author shlomo
 * 
 * basic idea is to help with sql statement cleanup, motivated by the problem that the underlying classes replaced the escape (slash) single quote in a sql statement with a double single quote....
 *
 */
public class sqlCleanupTools {

	
	public static String ReplaceQuoteSQL(String input){
		//should we check if there are replaced quote already??
		if(input == null){
			return null;
		}
		
		
		return input.replaceAll("'", "''");
	}
	
	
	
	
	
}
