/**
  * each row TableCellEditor
  *
  * @version 1.0 10/20/98
  * @author Nobuo Tamemasa
  */

package metdemo.Tables;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;

public class EachRowEditor implements TableCellEditor 
{
  protected Hashtable editors;
  protected TableCellEditor editor, defaultEditor;

  /**
   * Constructs a EachRowEditor.
   * create default editor 
   *
   * @see TableCellEditor
   * @see DefaultCellEditor
   */ 
  public EachRowEditor() {
    editors = new Hashtable();
    defaultEditor = new DefaultCellEditor(new JTextField());
    editor = defaultEditor;
  }
  
  /**
   * @param row    table row
   * @param editor table cell editor
   */
  public void add(int row, TableCellEditor editor) {
    editors.put(new Integer(row),editor);
  }
  
  public Component getTableCellEditorComponent(JTable table,
					       Object value, boolean isSelected, int row, int column) {
    editor = (TableCellEditor)editors.get(new Integer(row));
    if (editor == null) {
      editor = defaultEditor;
    }
    return editor.getTableCellEditorComponent(table,
					      value, isSelected, row, column);
  }

  public Object getCellEditorValue() {
    return editor.getCellEditorValue();
  }
  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }
  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }
  public boolean isCellEditable(EventObject anEvent) {
    return editor.isCellEditable(anEvent);
  }
  public void addCellEditorListener(CellEditorListener l) {
    editor.addCellEditorListener(l);
  }
  public void removeCellEditorListener(CellEditorListener l) {
    editor.removeCellEditorListener(l);
  }
  public boolean shouldSelectCell(EventObject anEvent) {
    return editor.shouldSelectCell(anEvent);
  }
}
