package metdemo;


public class NBayesException extends Exception
{
    private String reason;
	public NBayesException(String reason)
		{
            this.reason=new String(reason);

		}
	public String getReason()
	{
        return reason;
	}
     
	public String toString()
	{
        return reason;
	}
     
  



}











