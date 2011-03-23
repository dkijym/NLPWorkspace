/**
 * SysdMetMessage.java
 *
 *
 * Created: Thu Dec  6 18:33:33 2001
 *
 * @author Omar Saeed Stevens
 * @version
 */

package metdemo;

public class SysdMetMessage
{

  private String md5sum,origin,detectedAt,recipient,ID,keywords;


  public SysdMetMessage(String m,String detected,String o,String recipient)
  {
    md5sum = m;
    detectedAt=detected;
    origin=o;
    this.recipient = recipient;
    this.ID = "message ID";
    this.keywords = "keywords";
  }

  /*Accessor*/

  public String getmd5sum()
  {
    return md5sum;
  }

  public String getOrigin()
  {
    return origin;
  }

  public String getDetectedAt()
  {
    return detectedAt;
  }

  public String getRecipient()
  {
    return recipient;
  }

  public String getID()
  {
    return ID;
  }

  public String getKeywords()
  {
    return keywords;
  }

  /* modifiers */

  public void setRecipient(String r)
  {
    recipient = r;
  }

  public void setDetectedAt(String s)
  {
    detectedAt =s;
  }

  public void setmd5sum(String s)
  {
    md5sum=s;
  }

  public void setOrigin(String o)
  {
    origin = o;
  }

  public void setID(String s)
  {
    ID = s;
  }

  public void setKeywords(String s)
  {
    keywords = s;
  }



} // VirusNode
