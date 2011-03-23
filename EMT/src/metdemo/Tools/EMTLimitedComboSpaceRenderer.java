/*
 * Created on Oct 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tools;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * @author Shlomo Hershkop
 *
 * will only render the first 25 characters
 */
public class EMTLimitedComboSpaceRenderer extends BasicComboBoxRenderer {
	private int max_length = 25;
	
	public EMTLimitedComboSpaceRenderer(int n)
	{
		max_length = n;
	}
	
	public EMTLimitedComboSpaceRenderer()
	{
	
	}
	
	public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
		
		if(arg1==null)
		{
			return super.getListCellRendererComponent(arg0,arg1,arg2,arg3,arg4);
		}
		
		String temp = arg1.toString();
		int l = temp.length();
		if(l>max_length)
		{
			temp =  "..." + temp.substring(l - max_length) ;
		}
		return super.getListCellRendererComponent(arg0,temp,arg2,arg3,arg4);
		
	}
}
