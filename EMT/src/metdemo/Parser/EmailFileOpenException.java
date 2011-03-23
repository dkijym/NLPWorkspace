package metdemo.Parser;


/**
 * Class thrown when there is a specific problem opening a message store file
 * @author Shlomo
 *
 */
public class EmailFileOpenException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public EmailFileOpenException(String message)
	{
		super(message);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
