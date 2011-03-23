/*
 * Created on Mar 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Shlomo Hershkop
 * 
 * Central class to draw laterante color grays on tables cells.
 */
public class AlternateColorTableRowsRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (0 == row % 2)
            this.setBackground(Color.WHITE);
        else
            this.setBackground(Color.lightGray);
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);

    }
}