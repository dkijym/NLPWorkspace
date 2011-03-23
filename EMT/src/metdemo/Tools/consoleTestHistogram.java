/**
 * 
 */
package metdemo.Tools;

import java.sql.SQLException;

import metdemo.DataBase.DatabaseConnectionException;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.DataBase.EMTMysqlConnection;

/**
 * @author shlomo
 * 
 */
public class consoleTestHistogram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String data[] = null;
		EMTDatabaseConnection emtconnect = new EMTMysqlConnection("127.0.0.1",
				"newenron", "root", "");
		try {
			emtconnect.connect(false);
		} catch (DatabaseConnectionException de) {
			de.printStackTrace();
			System.exit(0);
		}

		try

		{
			data = emtconnect.getSQLDataByColumn(
					"select distinct sender from email where sdom = 'enron.com'", 1);
		} catch (SQLException se) {
			se.printStackTrace();
		}
		HistogramCalculator hc = new HistogramCalculator(emtconnect);
		
		for (int i = 0; i < data.length; i++) {
			System.out.println(data[i]);
			
			/*hc.getHistogram(data[*/
		}

	}

}
