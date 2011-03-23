package metdemo.Tables;


import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Utilites for manipulating JTables.
 *
 * @author Marc Hedlund
 *   <a href="mailto:marc@precipice.org">&lt;marc@precipice.org&gt;</a>
* @version $Revision: 1.1 $
 */

public class TableUtils  {

  // This is based on code found at
  // <http://java.sun.com/docs/books/tutorial/ui/swingComponents/table.html>,
  // somewhat modified.

  public static void setPreferredCellSizes(JTable table) {
    TableModel model = table.getModel();
    TableColumnModel colModel = table.getColumnModel();

    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumn column = colModel.getColumn(i);

      int longestCell = 0;
      int highestCell = 0;

      for (int j = 0; j < model.getRowCount(); j++) {
        Object value = model.getValueAt(j, i);
        if (value == null) continue;

        Component cell =
          table.getDefaultRenderer(model.getColumnClass(i)).
          getTableCellRendererComponent(table, value,
                                        false, false, j, i);

        int width = cell.getPreferredSize().width + 10;
        int height = cell.getPreferredSize().height;

        if (width > longestCell) longestCell = width;         
        if (height > highestCell) highestCell = height;
      }

      int preferredHeight = highestCell;
      column.setPreferredWidth(longestCell);

      TableCellRenderer headerRenderer = column.getHeaderRenderer();
      if (headerRenderer != null)
      {
	Component headerComp = headerRenderer.
	  getTableCellRendererComponent(table, column.getHeaderValue(),
					false, false, 0, 0);

	int headerWidth = headerComp.getPreferredSize().width;
	int headerHeight = headerComp.getPreferredSize().height;

	column.setPreferredWidth(Math.max(headerWidth, longestCell));

	preferredHeight = Math.max(headerHeight, highestCell);
      }	

      int currentHeight = table.getRowHeight();

      table.setRowHeight(Math.max(currentHeight, preferredHeight));
    }
  }                                                                   

/**
   * Takes a column in an existing table and makes it fixed-width.
   * Specifically, it sets the column's minimum and maximum widths to
   * its preferred width, and disables auto-resize for the table as a
   * whole.
   *
   * <p>
*
   * Later on this should take a column array for efficiency.
   *
   * @param table JTable The table to modify
   * @param colIndex int Which column to fix
   * @return int The width of the column as it was fixed
   */

  public static int fixColumnToPreferredWidth(JTable table, int colIndex) {
    TableColumnModel tcm = table.getColumnModel();
    TableColumn col = tcm.getColumn(colIndex);
    int width = col.getPreferredWidth();

    col.setMaxWidth(width);
    col.setMinWidth(width);    

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    return width;
  }
}

