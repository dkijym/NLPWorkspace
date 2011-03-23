/*
 * Created on Oct 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tools;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


/**
 * @author crash
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EMTnoHTMLCellRenderer extends DefaultListCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
		
		if(arg1==null)
		{
			return super.getListCellRendererComponent(arg0,arg1,arg2,arg3,arg4);
		}
		
		String temp = arg1.toString();
		if(temp.equalsIgnoreCase("<html>"))
		{
			
			temp = "<html>&lt;"+temp.substring(1,temp.length()-1)+"&gt;</html>";
		}
		return super.getListCellRendererComponent(arg0,temp,arg2,arg3,arg4);
		
	}

}
