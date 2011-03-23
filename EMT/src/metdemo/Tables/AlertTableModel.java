/*
 * ++Copyright SYSDETECT++
 * 
 * Copyright (c) 2002 System Detection. All rights reserved.
 * 
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SYSTEM DETECTION. The
 * copyright notice above does not evidence any actual or intended publication
 * of such source code.
 * 
 * Only properly authorized employees and contractors of System Detection are
 * authorized to view, posses, to otherwise use this file.
 * 
 * System Detection 5 West 19th Floor 2 Suite K New York, NY 10011-4240
 * 
 * +1 212 242 2970 <sysdetect@sysdetect.org>
 * 
 * --Copyright SYSDETECT--
 */


package metdemo.Tables;


import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import metdemo.AlertTools.Alert;

import java.util.*;


public class AlertTableModel implements TableModel {

	Vector m_vListeners;
	final String[] m_astrColumnNames = {"Level", "Alert"};

	Vector m_vAlerts;

	public AlertTableModel() {
		m_vListeners = new Vector();
		m_vAlerts = new Vector();
	}

	
	public synchronized void clearTable(){
		m_vListeners.clear();
		m_vAlerts.clear();
	}
	
	public synchronized int getRowCount() {
		return m_vAlerts.size();
	}

	public int getColumnCount() {
		return m_astrColumnNames.length;
	}

	public String getColumnName(int columnIndex) {
		return m_astrColumnNames[columnIndex];
	}


	public Class getColumnClass(int columnIndex) {
		try {
			if (0 == columnIndex) {
				return Class.forName("java.lang.Number");
			} 
			return Class.forName("java.lang.String");
			
		} catch (java.lang.ClassNotFoundException e) {
			System.err.println(e);
			System.exit(1);

			return null; // compiler requires this
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		Alert alert = (Alert) m_vAlerts.get(rowIndex);

		switch (columnIndex) {
			case 0 :
				return new Integer(alert.getLevel());
			case 1 :
				return alert.getBrief();
			case 2 :
				return alert.getDetailed();
			default :
				return null;
		}
	}

	public synchronized void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex >= m_vAlerts.size()) {
			return;
		}

		Alert alert = (Alert) m_vAlerts.get(rowIndex);

		switch (columnIndex) {
			case 0 :
				alert.setLevel(((Integer) aValue).intValue());
				break;
			case 1 :
				alert.setBrief((String) aValue);
				break;
			default :
				return;
		}

		TableModelEvent evt = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
		for (int i = 0; i < m_vListeners.size(); i++) {
			((TableModelListener) m_vListeners.get(i)).tableChanged(evt);
		}
	}

	public void addTableModelListener(TableModelListener l) {
		m_vListeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		m_vListeners.remove(l);
	}


	public synchronized Alert getAlert(int i) {
		return (Alert) m_vAlerts.get(i);
	}

	public synchronized void addAlert(Alert alert) {
		m_vAlerts.add(alert);

		TableModelEvent evt = new TableModelEvent(this, m_vAlerts.size() - 1, m_vAlerts.size() - 1,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		for (int i = 0; i < m_vListeners.size(); i++) {
			((TableModelListener) m_vListeners.get(i)).tableChanged(evt);
		}
	}

}