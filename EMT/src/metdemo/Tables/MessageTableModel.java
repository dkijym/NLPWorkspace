/*
 * Created on Jan 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tables;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageTableModel extends metdemo.Tables.TableSorter {
	MessageSubModel m_model;
	
	Vector colmns;
	
	public MessageTableModel(final String columnNames[], final int rows) {
		super();
		colmns = new Vector<String>();
		for(int i=0;i<columnNames.length;i++){
			colmns.add(columnNames[i]);
		}
		m_model = new MessageSubModel(columnNames, rows);
		setModel(m_model);
		m_model.addTableModelListener(this);
		setSortColumn(MessageWindow.i_utimeColumn); // sort by date initially
	}//TODO:

	public final boolean isCellEditable(final int rowIndex, final int columnIndex) {
		if (columnIndex == MessageWindow.i_labelColumn)
		//hard coded to get speed
		//((String)getColumnName(columnIndex)).equals("Label") )
		// //((MessageSubModel)model).
		{

			return true;

		}// else
		return false;
	}

	public synchronized Vector getRowData(final int rownumber)
	{
		return (Vector)((Vector)m_model.getDataVector()).elementAt(rownumber);
	}
	
	public synchronized Vector getAllData() {
		return m_model.getDataVector();
	}
	
	public synchronized void setData(Vector datavec) {
		
		for(int i=0;i<datavec.size();i++){
		
		((MessageSubModel) model).addRow((Vector)datavec.get(i));
		}
	}
	public synchronized void setRowData(Vector datavec) {
		
			
		((MessageSubModel) model).addRow(datavec);
		
	}
	public synchronized void addData(final Object[][] rowData) {
		for (int i = 0; i < rowData.length; i++)
			((MessageSubModel) model).addRow(rowData[i]);
	}

	public synchronized void addData(final Object[][] rowData, int view) {
		if (view == 1) { //ie unique mailref
			String mailref = new String("");

			for (int i = 0; i < rowData.length; i++) {
				if (!mailref.equals(rowData[i][0])) {
					((MessageSubModel) model).addRow(rowData[i]);
					mailref = (String) rowData[i][0];
				}
			}
		} else {
			for (int i = 0; i < rowData.length; i++)
				((MessageSubModel) model).addRow(rowData[i]);
		}
	}

	public synchronized void addRow(final Object[] rowData) {

		((MessageSubModel) model).addRow(rowData);
	}

	public synchronized void removeRow(int n) {

		m_model.removeRow(n);
	}

	public synchronized void addColumn(final String name, final Object[] Data) {
		((MessageSubModel) model).addColumn(name, Data);
	}

	public synchronized void clear() {

		m_model.setRowCount(0);
		reallocateIndexes();
		//while (m_model.getRowCount() > 0)
		//	{
		//    m_model.removeRow(0);
		//	    reallocateIndexes();
		//	}

	}

	public synchronized Object getValueAt(final int row, final int column) {
		Object ret = super.getValueAt(row, column);

		//for sorting last 2 columns...not sure worth bottleneck
		if (column == MessageWindow.i_labelColumn) {
			//String ret2 = new
			// String(""+(Double)super.getValueAt(row,10));

			String sRet = ((String) ret).toLowerCase();
			if (sRet.equals("u") == true || sRet.equals("?") == true)// ||
			// sRet.equals("u")==true)
			{
				return ("Unknown");//_"+ret2);
			} else if (sRet.equals("i") == true)//||
			// sRet.equals("i")==true)
			{
				return ("Interesting");//+"_"+ret2);
			} else if (sRet.equals("n") == true)// ||
			// sRet.equals("n")==true)
			{
				return ("Not Interesting");//_"+ret2);
			} else if (sRet.equals("s") == true)// ||
			// sRet.equals("S")==true)
			{
				return ("Spam");//_"+ret2);
			} else if (sRet.equals("v") == true)// ||
			// sRet.equals("v")==true)
			{
				return ("Virus");//_"+ret2);
			} else {
				return ret;
			}
		} //else {
		return ret;
		//}

	}
	//no render
	
	class MessageSubModel extends DefaultTableModel {
		public MessageSubModel(String[] columnNames, int rows) {
			super(columnNames, rows);
		}

		public Class getColumnClass(final int column) {
			//return getValueAt(0, column).getClass();
			//shlomo summer 02 changed from being hardcoded
			//String sname = ((String)getColumnName(column));

			//	        final private String []COLUMN_NAMES = new String[]{"Mailref",
			// "Sender", "Recipient", "Subject", "# Rcpt", "# Attach",
			// "Size","Group", "Date", "Label", "Score"};

			if (column == MessageWindow.i_labelColumn)// sname.equals("Label") )
			//column ==
			// 7)//((TableColumn)(super.getColumn("Interest"))).modelIndex)//==
			// 7)
			{

				return javax.swing.JComboBox.class; //.lang.String.class;
			} else if (column == MessageWindow.i_sizeColumn || column == MessageWindow.i_groupColumn || column == MessageWindow.i_attachmentColumn
					|| column == MessageWindow.i_ccColumn || column == MessageWindow.i_falsepositiveColumn || column == MessageWindow.i_scoreColumn)
			//sname.equals("# Rcpt") || sname.equals("# Attach") ||
			// sname.equals("Size") || sname.equals("Group") )
			{
				return java.lang.Integer.class;
			} /*
			   * else if (column == i_scoreColumn)//sname.equals("Score") ) {
			   * return Integer.class; }
			   */else {
				return String.class;
			}

		}

		public final Object getValueAt(final int row, final int column) {
			// if(column==7)
			//return super.getValueAt(row,column);
			//   //return (javax.swing.JComboBox)super.getValueAt(row,column);
			//String sname = ((String)getColumnName(column));
			//	        final private String []COLUMN_NAMES = new String[]{"Mailref",
			// "Sender", "Recipient", "Subject", "# Rcpt", "# Attach",
			// "Size","Group", "Date", "Label", "Score"};

			if (column == MessageWindow.i_sizeColumn || column == MessageWindow.i_groupColumn || column == MessageWindow.i_attachmentColumn
					|| column == MessageWindow.i_ccColumn || column == MessageWindow.i_falsepositiveColumn || column == MessageWindow.i_scoreColumn)
			// sname.equals("# Rcpt") || sname.equals("# Attach") ||
			// sname.equals("Size") || sname.equals("Group")) //(column > 2) &&
			// (column < 6))
			{
				try {
					return (new Integer((String) super.getValueAt(row, column)));
				} catch (NumberFormatException ex) {
					return null;
				}
			}
			/*
			 * else if (column == i_scoreColumn)// sname.equals("Score")) { try {
			 * return (new Double(Utils.round( Double.parseDouble((String)
			 * super.getValueAt(row, column)), 4)));
			 * 
			 * //return(new Double((String)super.getValueAt(row,column))); }
			 * catch (NumberFormatException ex) { return null; } }
			 */else {
				return super.getValueAt(row, column);
			}

		}
	}
}
