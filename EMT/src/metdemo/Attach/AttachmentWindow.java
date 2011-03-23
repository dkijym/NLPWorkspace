package metdemo.Attach;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import metdemo.winGui;
import metdemo.AlertTools.ReportForensicWindow;
import metdemo.DataBase.DatabaseManager;
import metdemo.DataBase.EMTDatabaseConnection;
import metdemo.Tables.AlternateColorTableRowsRenderer;
import metdemo.Tables.TableSorter;
import metdemo.Tables.md5CellRenderer;
import metdemo.Tools.BusyWindow;
import metdemo.Tools.EMTHelp;
import metdemo.Tools.EMTLimitedComboSpaceRenderer;
import metdemo.Tools.Utils;

import java.util.*;
import java.text.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;

/**
 * Class written to implment original MET ideas
 * <p>
 * Attachment flow calculations.
 * 
 * added new gui interface to play attachment stats. in version 3.2+
 * 
 * @author shlomo hershkop
 */

public class AttachmentWindow extends JScrollPane //implements
                                                  // TickListener//,Serializable
{
    private final String METRICS[] = { "filename", "signature", "classification", "mime type", "overall birthrate/min",
            "overall birthrate/hour", "overall birthrate/day", "internal birthrate/min", "internal birthrate/hour",
            "internal birthrate/day", "overall speed", "internal speed", "overall saturation", "internal saturation",
            "lifespan", "size", "origin" };

    //  private static final String INITIAL_MESSAGE = "Click button to populate";
    private static final String START_ALERTS = "Compute Statistics and Alerts";

    private static final String STOP_ALERTS = "Stop Computing";

    private static final String SAVE = "Save Computation";

    private static final String LOAD = "Load Computation";

    private static final String SAVE_FILE = "saved_hashinfo.zip";

    private EMTDatabaseConnection /*m_jdbcView,*/ m_jdbcUpdate;

    private JLabel m_status;

    //private JComboBox m_comboAttachments;
    private JTable m_tableMetrics;

    private JTable m_messages;

    private JTable m_attachment;

    private DefaultTableModel m_messages_model;

    private AttachmentTableModel m_attachment_model;

    private DefaultTableCellRenderer rowColorRenderer;

    //Messages m_messages;
    //private Hashtable m_htAttachments; // attachments we've seen
    private JButton m_butAlerts, m_Grouping;

    private winGui m_winGui;

    private ReportForensicWindow m_alerts;

    private AttachmentAlertOptions m_alertOptions;

    private Thread m_statsThread = null;

    private boolean m_threadStop;

    private long m_nextUpdateUtime;

    private boolean m_repopulating; // true if currently repopulating combo box

    private boolean m_benchmark;

    private long m_timeLastLogged; // for messages table

    private long m_before;

    private static final DecimalFormat m_percentFormat = new DecimalFormat("#0.00");

    private long m_ticksSoFar, m_totalTicks;

    private TableSorter sorter;

    private boolean m_useContent = false;

    private String m_current_hash; //keeps trakc of current hash selected in
                                   // the upper left table

    private JComboBox attachmentType;

    public AttachmentWindow(EMTDatabaseConnection jdbcUpdate,
            ReportForensicWindow alerts, AttachmentAlertOptions alertOptions, md5CellRenderer md5Renderer,
            JLabel status, winGui top, boolean verbose) {

        //m_jdbcView = jdbcView;
        m_jdbcUpdate = jdbcUpdate;
        m_alerts = alerts;
        m_alertOptions = alertOptions;
        m_status = status;
        m_winGui = top;
        m_benchmark = verbose;

        m_nextUpdateUtime = 0;
        m_repopulating = false;

        m_timeLastLogged = 0;

        //m_htAttachments = new Hashtable();

        m_threadStop = false;

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        panel.setLayout(gridbag);

        JButton refreshListButton = new JButton("Refresh Attachments List");
        refreshListButton.setToolTipText("Click to reload the attachment list");
        refreshListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateAttachmentList();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(refreshListButton, constraints);
        panel.add(refreshListButton);

        attachmentType = new JComboBox();
        attachmentType.addItem(new String("All"));
        //limit size so it doesnt go over page
        attachmentType.setRenderer(new EMTLimitedComboSpaceRenderer(40));

        //get all types
        setupTypeList(attachmentType);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        gridbag.setConstraints(attachmentType, constraints);
        panel.add(attachmentType);

        String[] columnsa = { "Name", "Size", "Type", "Hash", "Group" };

        //will define the table to be a attachment vector

        m_attachment_model = new AttachmentTableModel(columnsa);

        //renderer to do different color rows.
        rowColorRenderer = new AlternateColorTableRowsRenderer();

        /*
         * DefaultTableCellRenderer() { public Component
         * getTableCellRendererComponent(JTable table, Object value, boolean
         * isSelected, boolean hasFocus, int row, int column) {
         * 
         * if (1==row % 4) this.setBackground(Color.lightGray);//isSelected ?
         * Color.yellow : else this.setBackground(Color.WHITE); return
         * super.getTableCellRendererComponent(table,value,isSelected, hasFocus,
         * row, column);
         *  } };
         */
        sorter = new TableSorter(m_attachment_model);

        m_attachment = new JTable(sorter) {//m_attachment_model){

            protected void processKeyEvent(KeyEvent evt) {
                //int column = table.getSelectedColumn();
                //int row = table.getSelectedRow();
                if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    updateStatisticsTable(m_attachment.getSelectedRow());
                    updateMessagesTable(m_attachment.getSelectedRow());
                }

                super.processKeyEvent(evt);
            }

            /** to render every other 4th row a color */
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column != 4) {
                    return rowColorRenderer;//will color each row
                }
                return super.getCellRenderer(row, column);
            }

        };
        m_attachment.getTableHeader().setReorderingAllowed(false);
        sorter.addMouseListenerToHeaderInTable(m_attachment);
        sorter.reallocateIndexes();
        m_attachment.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_attachment.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public final void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = m_attachment.rowAtPoint(point);

                if (e.getClickCount() == 1) {
                    //System.out.println(""+row);
                    if (!m_repopulating) {
                        updateStatisticsTable(row);
                        updateMessagesTable(row);
                    }

                    //  System.out.println("double clikc");
                }
            }
        });

        //	m_attachment.setPreferredSize(new Dimension(300,100));

        //	TableColumn tbc = m_attachment.getColumn("Ref");
        //tbc.setMaxWidth(0);
        //tbc.setMinWidth(0);
        //tbc.setPreferredWidth(0);
        TableColumn tbc = m_attachment.getColumn("Size");
        tbc.setPreferredWidth(30);
        tbc = m_attachment.getColumn("Name");
        tbc.setPreferredWidth(100);
        tbc = m_attachment.getColumn("Type");
        tbc.setPreferredWidth(30);

        tbc = m_attachment.getColumn("Group");
        tbc.setPreferredWidth(30);
        tbc.setMaxWidth(30);
        tbc.setMinWidth(30);

        DefaultTableCellRenderer groupColumnRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {

                int cellValue = (value instanceof Integer) ? ((Integer) value).intValue() : 0;

                setBackground((cellValue % 2 == 1) ? Color.cyan : Color.yellow);
                setText((value == null) ? "" : value.toString());
            }
        };
        groupColumnRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tbc.setCellRenderer(groupColumnRenderer);

        //tbc.setMinWidth(10);
        //tbc.setMaxWidth(10);
        //tbc.setPreferredWidth(25);
        tbc = m_attachment.getColumn("Hash");
        tbc.setMinWidth(0);
        tbc.setMaxWidth(0);
        tbc.setPreferredWidth(0);

        JScrollPane attachment_scroll = new JScrollPane(m_attachment);
        attachment_scroll.setPreferredSize(new Dimension(350, 270));

        //m_comboAttachments = new JComboBox();
        //m_comboAttachments.setPreferredSize(new Dimension(280, 25));
        //m_comboAttachments.addItem(INITIAL_MESSAGE);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(5, 5, 5, 5);//was 5,5,5,20
        //	gridbag.setConstraints(m_comboAttachments, constraints);
        //panel.add(m_comboAttachments);
        constraints.gridwidth = 2;
        gridbag.setConstraints(attachment_scroll, constraints);
        panel.add(attachment_scroll);

        constraints.gridwidth = 1;
        m_butAlerts = new JButton(START_ALERTS);
        m_butAlerts.addActionListener(new ActionListener()
        //addMouseListener(new MouseAdapter()
                {
                    public void actionPerformed(ActionEvent evt)
                    //public void mouseClicked(MouseEvent e)
                    {
                        if (m_statsThread == null) {
                            //need to setup things like max time etc
                            long currentUtime;
                            synchronized (m_jdbcUpdate) {
                                //m_jdbcUpdate.getSqlData("select max(utime) from email");
                                //data = m_jdbcUpdate.getRowData();
                               
                                try {
                                    String []d = m_jdbcUpdate.getSQLDataByColumn("select max(utime) from email",1);
                                    currentUtime = Long.parseLong(d[0]);

                                    // don't do this in forensic mode
                                    //  	if (m_nextUpdateUtime == 0)
                                    //  	{
                                    //  	  m_nextUpdateUtime = currentUtime - 60;
                                    //  	}
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    currentUtime = System.currentTimeMillis();
                                }
                            }
                            
                            m_statsThread = new Thread(new AttachStatsThread(true,currentUtime,false));
                            m_statsThread.start();
                            m_status.setText("Calculating attachment metrics...");
                        } else {
                            m_threadStop = true;
                        }
                    }
                });
        m_butAlerts.setEnabled(false);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 5, 5, 10);
        constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(m_butAlerts, constraints);
        panel.add(m_butAlerts);

        m_Grouping = new JButton("Similiar Grouping:");
        m_Grouping.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                if (m_useContent) {
                    (new Thread(new Runnable() {
                        public void run() {
                            groupAttachments(m_useContent);
                        }
                    })).start();
                } else
                    groupAttachments(m_useContent);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(m_Grouping, constraints);
        panel.add(m_Grouping);

        m_Grouping.setEnabled(false);

        //type of grouping
        JRadioButton button_n = new JRadioButton("By Name", true);
        JRadioButton button_c = new JRadioButton("By Content");
        button_n.setToolTipText("Group using the name as a similarity measure");
        button_c.setToolTipText("Group using some content as a similarity measure");
        button_c.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                m_useContent = true;
            }
        });
        button_n.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                m_useContent = false;
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(button_n);
        bg.add(button_c);

        JPanel buttons = new JPanel();
        buttons.add(button_n);
        buttons.add(button_c);
        constraints.gridx = 2;
        constraints.gridy = 2;

        gridbag.setConstraints(buttons, constraints);
        panel.add(buttons);

        constraints.anchor = GridBagConstraints.CENTER;
        JLabel labelMetrics = new JLabel("Metrics");
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        gridbag.setConstraints(labelMetrics, constraints);
        panel.add(labelMetrics);

        m_tableMetrics = new JTable(METRICS.length, 2) {
            protected void configureEnclosingScrollPane() {
                Container p = getParent();
                if (p instanceof JViewport) {
                    Container gp = p.getParent();
                    if (gp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) gp;
                        // Make certain we are the viewPort's view and not, for
                        // example, the rowHeaderView of the scrollPane -
                        // an implementor of fixed columns might do this.
                        JViewport viewport = scrollPane.getViewport();
                        if (viewport == null || viewport.getView() != this) {
                            return;
                        }
                        //                scrollPane.setColumnHeaderView(getTableHeader());
                        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                        scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
                    }
                }
            }
        };

        m_tableMetrics.setEnabled(false);
        for (int i = 0; i < METRICS.length; i++) {
            m_tableMetrics.setValueAt(METRICS[i], i, 0);
        }
        //JScrollPane spMetrics = new JScrollPane(m_tableMetrics);
        //spMetrics.setColumnHeaderView(null);
        m_tableMetrics.setPreferredSize(new Dimension(350, 270));
        //spMetrics.setPreferredSize(new Dimension(350,250));
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        //	constraints.gridheight = 1;//was 3
        //gridbag.setConstraints(spMetrics, constraints);
        //panel.add(spMetrics);
        gridbag.setConstraints(m_tableMetrics, constraints);
        panel.add(m_tableMetrics);

        constraints.gridwidth = 1;

        final AttachmentWindow me = this; //self ref for sub calls to stuff
        //m_htAttachments hash to save
        JButton saver = new JButton(SAVE);
        saver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    FileOutputStream out = new FileOutputStream(SAVE_FILE);
                    GZIPOutputStream out2 = new GZIPOutputStream(out);
                    ObjectOutputStream s = new ObjectOutputStream(out2);
                    //ObjectOutputStream s = new ObjectOutputStream( new
                    // FileOutputStream("saved_hashinfo"));
                    //s.writeObject((Hashtable)m_htAttachments);
                    //run through hash table and for each call save and write
                    // that out.

                    //s.writeInt(m_htAttachments.size());
                    s.writeInt(m_attachment_model.getRowCount());
                    for (int i = 0; i < m_attachment_model.getRowCount(); i++)
                        s.writeObject(m_attachment_model.getItemAt(i).Save());

                    /*
                     * Enumeration enum = m_htAttachments.elements(); while
                     * (enum.hasMoreElements()) { Attachment att = (Attachment)
                     * enum.nextElement(); s.writeObject((String[])att.Save()); }
                     */
                    s.flush();
                    //			out2.close();
                    s.close();
                    //out.close();

                    JOptionPane.showMessageDialog(me, "done saving computation");

                } catch (Exception e1) {
                    System.out.println(e1);
                }
            }
        });
        saver.setBackground(Color.yellow);
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(saver, constraints);
        panel.add(saver);

        JButton loader = new JButton(LOAD);
        loader.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BusyWindow bw = new BusyWindow("Loading saved Hashes", "Progress",true);
                    boolean before = m_repopulating;
                    m_repopulating = true;
                    //need to check if anything in the drop down list.

                    m_attachment_model.clear();
                    sorter.reallocateIndexes();
                    /*
                     * if(m_comboAttachments.getItemCount() <=1) {
                     * m_comboAttachments.removeAllItems(); }
                     */

                    FileInputStream in = new FileInputStream(SAVE_FILE);//"saved_hashinfo.zip");
                    GZIPInputStream gin = new GZIPInputStream(in);
                    ObjectInputStream s = new ObjectInputStream(gin);
                    //FileInputStream in = new
                    // FileInputStream("saved_hashinfo");
                    //	ObjectInputStream s = new ObjectInputStream(in);
                    //WARNING..should prob just add instead of replacing.
                    //m_htAttachments = new Hashtable();
                    final int size = s.readInt();
                    bw.progress(0, size);
                    bw.setVisible(true);
                    for (int i = 0; i < size; i++) {
                        bw.progress(i, size);
                        Attachment att = new Attachment(m_alerts, m_alertOptions);//,me
                                                                                  // );
                        att.Load((String[]) s.readObject());
                        //check duplicates...dont load those up
                        /*
                         * if (!m_htAttachments.containsKey(att.getSignature())) {
                         * m_htAttachments.put(att.getSignature(),att);
                         *  // find right place in combo box /* if
                         * (m_comboAttachments.getItemCount() > 0) { int j = 0;
                         * while ((j <m_comboAttachments.getItemCount()) &&
                         * ((att.getSignature()).compareTo(((Attachment)m_comboAttachments.getItemAt(j)).getSignature()) >
                         * 0)) { j++; } m_comboAttachments.insertItemAt(att, j); }
                         * else { m_comboAttachments.addItem(att); }
                         *  }
                         */
                        m_attachment_model.addRow(att);

                    }
                    //spring 2005
                    sorter.reallocateIndexes();

                    sorter.sortByColumn(0);//will show results
                    m_repopulating = before;
                    s.close();
                    //in.close();
                    bw.setVisible(false);
                    bw = null;
                    resetHolddowns();
                    JOptionPane.showMessageDialog(me, "done loading computation");
                } catch (Exception e2) {
                    System.out.println(e2);
                }

                m_butAlerts.setEnabled(true);
                m_Grouping.setEnabled(true);
            }

        });

        loader.setBackground(Color.yellow);
        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(loader, constraints);
        panel.add(loader);

        JButton gsort = new JButton("Open Attachment");
        gsort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                launchAttachment(getCurrentAttachment());

            }
        });
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        gridbag.setConstraints(gsort, constraints);
        panel.add(gsort);

        EMTHelp et = new EMTHelp(EMTHelp.ATTACHSTATS);
        constraints.gridx = 3;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        gridbag.setConstraints(et, constraints);
        panel.add(et);

        JLabel labelMessages = new JLabel("Seen In");
        constraints.gridx = 0;
        constraints.gridy = 7;//was4
        constraints.gridwidth = 4;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(labelMessages, constraints);
        panel.add(labelMessages);
        String[] columns = { "Ref", "From", "To", "Subject", "# Rcpt", "# Attach", "Size", "Date", "Time" };
        m_messages_model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        m_messages = new JTable(m_messages_model) {
            public TableCellRenderer getCellRenderer(int row, int column) {
                return rowColorRenderer;//will color each row
            }

        };
        m_messages.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public final void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = m_messages.rowAtPoint(point);

                if (e.getClickCount() == 2) {
                    //String data[][];
                    //double click
                    String ref = (String) m_messages.getValueAt(row, 0);
                    //System.out.println("mailref: " + ref);
                    // "Ref","From", "To","Subject", "# Rcpt", "# Attach",
                    // "Size", "Date","Time" };
                    String detail = "Mailref: " + ref + "\n";
                    detail += "Sender: " + ((String) m_messages.getValueAt(row, 1)) + "\n";
                    detail += "Date: " + (String) m_messages.getValueAt(row, 7) + "\n";
                    detail += "Subject: " + (String) m_messages.getValueAt(row, 3) + "\n";
                    Utils.popBody(m_winGui, detail, ref, m_jdbcUpdate, true);
                }
            }
        });
        //new Messages(m_jdbcUpdate, m_jdbcView, md5Renderer, m_alerts, -1,
        // false,"words",top);
        m_messages.setPreferredSize(new Dimension(600, 200));
        //m_messages.getPreferredSize().width,
        // m_messages.getPreferredSize().width*3/4));

        tbc = m_messages.getColumn("Ref");
        tbc.setMaxWidth(0);
        tbc.setMinWidth(0);
        tbc.setPreferredWidth(0);
        tbc = m_messages.getColumn("Time");
        tbc.setMaxWidth(0);
        tbc.setMinWidth(0);
        tbc.setPreferredWidth(0);
        tbc = m_messages.getColumn("Subject");
        tbc.setPreferredWidth(150);
        tbc = m_messages.getColumn("# Rcpt");
        tbc.setPreferredWidth(40);
        tbc = m_messages.getColumn("# Attach");
        tbc.setPreferredWidth(40);

        JScrollPane messages_scroll = new JScrollPane(m_messages);
        messages_scroll.setPreferredSize(new Dimension(700, 200));
        constraints.gridx = 0;
        constraints.gridy = 8;//was 5
        constraints.gridwidth = 4;
        constraints.insets = new Insets(5, 5, 15, 5);
        gridbag.setConstraints(messages_scroll, constraints);
        panel.add(messages_scroll);

        setViewportView(panel);
    }

    private void updateStatisticsTable(int select) {

        Attachment att = m_attachment_model.getItemAt(sorter.translate(select));
        //m_comboAttachments.getSelectedItem();
        if (att != null) {
            int i = 0;
            m_tableMetrics.setValueAt(att.getFilename(), i++, 1);
            m_tableMetrics.setValueAt(att.getSignature(), i++, 1);
            m_tableMetrics.setValueAt(att.getClassificationAsString(), i++, 1);
            m_tableMetrics.setValueAt(att.getMimeType(), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getOverallBirthratePerMin()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getOverallBirthratePerHour()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getOverallBirthratePerDay()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getInternalBirthratePerMin()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getInternalBirthratePerHour()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getInternalBirthratePerDay()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getOverallSpeed()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getInternalSpeed()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getOverallSaturation()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getInternalSaturation()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getLifespan()), i++, 1);
            m_tableMetrics.setValueAt(String.valueOf(att.getSize()), i++, 1);
            m_tableMetrics.setValueAt(att.hasExternalOrigin() ? "External" : "Internal", i++, 1);
        }
    }

    /**
     * will save the current attachment to a temp file using these rules:
     * TEMP_ATT_ + first 10 of hash +filename
     */

    public final String getCurrentAttachment() {
        if (m_current_hash == null)
            return null;

        String tempfile = "TEMP_ATT_";

        try {
            //get the filename,body off this hash
            String data[][];
            File fi;

            synchronized (m_jdbcUpdate) {
                data = m_jdbcUpdate.getSQLData("select filename from message where hash = '" + m_current_hash + "'");

                // m_jdbcUpdate.getSqlData("select filename from message where
                // hash = '"+m_current_hash+"'");
                // data = m_jdbcUpdate.getRowData();

                if (data[0][0] == null)
                    return null;

                if (m_current_hash.length() > 10)
                    tempfile += m_current_hash.substring(0, 10);
                else
                    tempfile += m_current_hash;

                data[0][0] = data[0][0].replaceAll(" ", "");

                tempfile += data[0][0].trim();
                System.out.println("will check: " + tempfile);
                //can check if file exists??
                fi = new File(tempfile);
                if (fi.exists()){
                	System.out.println("found the file, so no need to get it again");
                	return tempfile;
                }
                //data = m_jdbcUpdate.getRowData();

                FileOutputStream fout = new FileOutputStream(fi);
                
                byte[][] tempb = m_jdbcUpdate.getBinarySQLData("select body from message where hash = '"
                        + m_current_hash + "'");
                for (int i = 0; i < tempb.length; i++) {
                    fout.write(tempb[i]);
                }
                //data[0][0].getBytes());

                fout.flush();
                fout.close();
                System.out.println("done writing: " + tempfile);

            }

            ////dump the body to temp file

            //close temp file

            //return temp name

        } catch (SQLException s) {
            System.out.println("s is : " + s);
            return null;
        } catch (IOException e) {
            System.out.println("e is " + e);
            return null;
        }

        return tempfile;

    }

    /**
     * will launch the native windows system to launch application attachmnets
     */
    public final void launchAttachment(String file_to_open) {

        if (file_to_open == null)
            return;

        try {
            String cmds[] = new String[2];
            cmds[0] = "Explorer.exe";//"AcroRd32.exe";
            cmds[1] = "\"" + file_to_open + "\"";

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmds);
            int exitVal = proc.waitFor();

            System.out.println("Process exitValue: " + exitVal);

        } catch (IOException eeee) {
            System.out.println("eee" + eeee);
        } catch (InterruptedException e23) {
            System.out.println(e23);
        }
    }

    /**
     * will group the attachmnets using a simalirty measure to track similiar
     * attachmnets
     */
    //RULEZ
    public final void groupAttachments(boolean doContent) {

        m_winGui.setEnabled(false);
        //need to get table size
        //Attachment att;
        int translate[] = sorter.getTranslate();
        int rows = m_attachment_model.getRowCount();
        int group = 0;
        BusyWindow bw = new BusyWindow("group attachments", "Progress",true);
        bw.progress(1, rows);
        bw.setVisible(true);
        sorter.sortByColumn(0);
        String[] al_names = new String[rows];
        String[] al_types = new String[rows];
        HashMap al_maphash = new HashMap();
        String[] al_hash = new String[rows];
        String temphash;
        String data[];
        int done[] = new int[rows];//keep track of done groups
        try {
            synchronized (m_jdbcUpdate) {

                for (int i = 0; i < rows; i++) {
                    bw.progress(i);
                    done[i] = -1;
                    //att =
                    // (Attachment)m_attachment_model.getItemAt(translate[i]);
                    al_names[i] = ((String) m_attachment_model.getValueAt(translate[i], 0)).toLowerCase();
                    //att.getFileName());
                    al_types[i] = ((String) m_attachment_model.getValueAt(translate[i], 2)).toLowerCase();

                    if (doContent) {
                        temphash = (String) m_attachment_model.getValueAt(translate[i], 3);
                        al_hash[i] = temphash;
                        if (!al_maphash.containsKey(temphash)) {
                            if (m_jdbcUpdate.getTypeDB().equals(DatabaseManager.sDERBY)) {
                                data = m_jdbcUpdate.getSQLDataByColumn("select body from message where hash ='"
                                        + temphash + "'", 1);

                            } else
                                data = m_jdbcUpdate.getSQLDataByColumn(
                                        "select substring(body,1,400) from message where hash ='" + temphash + "'", 1);
                            //m_jdbcUpdate.getSqlData("select
                            // substring(body,1,200) from message where hash
                            // ='"+ temphash+"'");
                            //data = m_jdbcUpdate.getColumnData(0);
                            if (data != null)
                                al_maphash.put(temphash, data[0]);
                        }

                    }
                }
            }
        } catch (SQLException s) {
            System.out.println("grouping attachments: " + s);
            return;
        }
        //run down each name, looking forward only
        //if futre not done already
        //and type = current type
        //and name close
        //same group as current
        for (int i = 0; i < rows; i++) {
            bw.progress(i);
            for (int j = i; j < rows; j++) {
                //ith one is the current one we are using
                if (i == j && done[j] == -1) {
                    done[j] = 1;
                    m_attachment_model.setGroup(translate[j], ++group);

                } else if (done[j] == -1)
                    if (al_types[i].equals(al_types[j]))
                        if (!doContent) {
                            if (distanceFilename(al_names[i], al_names[j]) > .5) {
                                done[j] = 1;
                                m_attachment_model.setGroup(translate[j], group);

                            }
                        } else {
                            if (distanceFilename((String) al_maphash.get(al_hash[i]), (String) al_maphash
                                    .get(al_hash[j])) > .35) {

                                done[j] = 1;
                                m_attachment_model.setGroup(translate[j], group);

                            }
                        }

            }
        }
        bw.setVisible(false);
        System.out.println("done grouping");
        sorter.reallocateIndexes();
        sorter.sortByColumn(4);
        m_winGui.setEnabled(true);
    }

    /** compute the distance between two filenames */
    static final double distanceFilename(String one, String two) {

        byte[] first = one.getBytes();
        byte[] second = two.getBytes();

        double score = 0;

        int min = one.length();
        if (two.length() < min)
            min = two.length();

        for (int i = 0; i < min; i++) {

            if (first[i] == second[i])
                score++;

        }
        double result = score / min;
        //	System.out.println("compare: " + one +" and "+ two +" = " + result);
        return result;

    }

    /** compute distance between two bodies */
    /*
     * static final double distanceBody(String one,String two) {
     * 
     * 
     * 
     * 
     * 
     * 
     *  }
     */

    /**
     * refresh the list of current attachments in the db
     *  
     */

    public final void updateAttachmentList() {
        System.out.println("update attachment list");
        BusyWindow bw = new BusyWindow("update attachment list", "Please wait",true);
        bw.setVisible(true);
        try {
            //String data[][];
            String data2[][];
            //long currentUtime;
            m_status.setText("Populating attachment list...");

            // get current db time
            //synchronized (m_jdbcUpdate)
            {
                //m_jdbcUpdate.getSqlData("select max(utime) from email");
                //data = m_jdbcUpdate.getRowData();
                //data = m_jdbcUpdate.getSQLData("select max(utime) from
                // email");
                // try
                {
                    //   currentUtime = Long.parseLong(data[0][0]);

                    // don't do this in forensic mode
                    //  	if (m_nextUpdateUtime == 0)
                    //  	{
                    //  	  m_nextUpdateUtime = currentUtime - 60;
                    //  	}
                }
                //  catch (Exception ex)
                {
                    //    JOptionPane.showMessageDialog(this, "Error while getting
                    // current time from db: " + ex);
                    //    return;
                }

                // get the user count (need this later)
                //REMOVED DURING DERBY WORK!!!!
                //long[] userCount = getUserCount();
                //   System.out.println("done");
                // get all attachment hashes
                //m_jdbcUpdate.getSqlData("select distinct hash, filename,
                // type, size from message where (hash!='') and (hash!='null')
                // and (!isnull(hash)) order by hash asc");
                //data = m_jdbcUpdate.getRowData();
                //spring 2005 data = m_jdbcUpdate.getSQLData("select distinct
                // hash, filename, type, size from message where (hash!='') and
                // hash is not null order by hash asc");

                //check if any of the attachment types are chosen
                if (attachmentType.getSelectedIndex() == 0) {

                    data2 = m_jdbcUpdate.getSQLData("select distinct hash from message");
                    //, filename, type, size from message where hash is not
                    // null order by hash asc");
                } else {

                    data2 = m_jdbcUpdate.getSQLData("select distinct hash from message where type = '"
                            + (String) attachmentType.getSelectedItem() + "'");

                }
            }
            //if (m_threadStop) return;
            /*
             * // remember what was selected before Attachment attCurrent =
             * (Attachment) m_comboAttachments.getSelectedItem(); String
             * strCurrent = new String(""); if (attCurrent != null) { strCurrent =
             * attCurrent.getSignature(); }
             * 
             * if (m_threadStop) return;
             */
            //System.out.println("here1");
            //m_repopulating = true;
            if (m_attachment_model.getRowCount() > 0) {
                m_attachment_model.clear();
                sorter.reallocateIndexes();
            }

            // insert all attachments
            //Attachment att;
            //int prevCount = m_comboAttachments.getItemCount();
            PreparedStatement pshelper = m_jdbcUpdate
                    .prepareStatementHelper("select distinct hash, filename, type, size from message where hash = ?");
            System.out.println("going to loop for data len:"+ data2.length);
            
            if(data2.length > 300){
                int n = JOptionPane.showConfirmDialog(AttachmentWindow.this,"Please note: you will be fetching : "+ data2.length + " records");
             
                if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.NO_OPTION){
                    return;
                }
            
            }
            
            
            
            for (int i = 0; i < data2.length; i++) {
                //if (m_threadStop) return;
                /*
                 * if (data[i].length != 3) { throw new SQLException("Database
                 * returned wrong number of columns"); }
                 */
                bw.progress(i, data2.length);

                //change this to pipe

                //data = m_jdbcUpdate.getSQLData("select distinct hash,
                // filename, type, size from message where hash = '"+
                // data2[i][0] +"'");
                pshelper.setString(1, data2[i][0]);
                ResultSet rs = pshelper.executeQuery();

                if (rs.next())//data[0][0] != null)
                {
                    //if (!m_htAttachments.containsKey(data[i][0]))
                    //{
                    Attachment att = new Attachment(rs.getString(1), rs.getString(2), rs.getString(3), m_alerts,
                            m_alertOptions);
                    //data[0][0], data[0][1], data[0][2], m_alerts,
                    // m_alertOptions);//, this);
                    //try{
                    att.setSize(rs.getInt(4));
                    //Integer.parseInt(data[0][3]));
                    //}catch(NumberFormatException nm){}
                    //    m_htAttachments.put(data[i][0], att);
                    /*
                     * // find right place in combo box if
                     * (m_comboAttachments.getItemCount() > 0) { int j = 0;
                     * while ((j <m_comboAttachments.getItemCount()) &&
                     * (data[i][0].compareTo(((Attachment)m_comboAttachments.getItemAt(j)).getSignature()) >
                     * 0)) { j++; } m_comboAttachments.insertItemAt(att, j); }
                     * else { m_comboAttachments.addItem(att); }
                     * 
                     * 
                     * m_attachment_model.addRow(att);//since we emptied it. // } //
                     * else
                     */
                    //	{
                    //	m_attachment_model.addRow((Attachment)m_htAttachments.get(data[i][0]));//since
                    // we emptied it.
                    //  }
                    m_attachment_model.addRow(att);//since we emptied it.
                   
                    //}
                    rs.close();
                }
            }
            
        } catch (SQLException n) {
            System.out.println("problem updating the attachment list");
        }finally{
            bw.setVisible(false);
            sorter.reallocateIndexes();
            m_butAlerts.setEnabled(true);
            m_Grouping.setEnabled(true);
            
            sorter.sortByColumn(0);
        }
    }

    /**
     * Sets the nextUpdateUtime to the time of the first message in db
     * 
     * @return true on success, false on failure
     */
    public boolean resetUpdateUtime() {
        String data[][];
        try {
            synchronized (m_jdbcUpdate) {
                //		m_jdbcView.getSqlData("select min(utime) from email");
                //		data = m_jdbcView.getRowData();
                data = m_jdbcUpdate.getSQLData("select min(utime) from email");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to get time of first message: " + ex);
            return false;
        }

        if ((data.length != 1) || (data[0].length != 1)) {
            JOptionPane.showMessageDialog(this, "Database returned wrong amount of data");
            return false;
        }

        try {
            m_nextUpdateUtime = Long.parseLong(data[0][0]);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Database returned invalid timestamp: " + nfe);
            return false;
        }

        return true;
    }

    public boolean resetHolddowns() {
        return m_attachment_model.resetHolddowns();
        /*
         * Enumeration enum = m_htAttachments.elements(); while
         * (enum.hasMoreElements()) { Attachment att = (Attachment)
         * enum.nextElement(); att.setClassification(Attachment.BENIGN); }
         * 
         * return true;
         */
    }

    private void updateMessagesTable(int select) {

        // Show messages for selected attachment
        Attachment att = m_attachment_model.getItemAt(sorter.translate(select));
        //m_comboAttachments.getSelectedItem();
        m_current_hash = att.getSignature(); //to knwo current hash
        m_messages_model.setRowCount(0);

        if (att != null) {
            //note if date in future will not show normal stuff. cantbe helped
            // for now.
            String query = "select email.mailref,sender, rcpt, subject, numrcpt, numattach, email.size, dates ,utime  ";
            query += "from message left join email ";
            query += "on email.mailref=message.mailref where hash='" + att.getSignature() + "' ";
            //query += "and insertTime>=" + (m_timeLastLogged + 1) + " ";//and
            // insertTime<(now() - 1) ";
            query += "order by dates asc";
            String data[][];
            try {
                // use jdbcView since this is triggered by user input
                synchronized (m_jdbcUpdate) {
                    //m_jdbcView.getSqlData(query);
                    //data = m_jdbcView.getRowData();
                    data = m_jdbcUpdate.getSQLData(query);
                }

                //	m_messages.clear();

                for (int i = 0; i < data.length; i++) {
                    m_messages_model.addRow(data[i]);
                    //sorter.reallocateIndexes();
                }

                if (data.length > 0) {
                    m_timeLastLogged = Long.parseLong(data[data.length - 1][8]);//was
                                                                                  // 9
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AttachmentWindow.this, "Failed to retreive message list: " + ex);
            }
        }
    }
    /**
     * called by the system for running emt
     *
     */
    public void Execute(){
    	setupTypeList(attachmentType);
    }
    
    
    
    private void setupTypeList(JComboBox attachmentType){
        synchronized (this.m_jdbcUpdate) {

            try {
                String[] data = m_jdbcUpdate.getSQLDataByColumn("select distinct type from message", 1);

                if (data != null) {
                    for (int offset = 0; offset < data.length; offset++) {
                        attachmentType.addItem(data[offset]);
                    }
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }
    public void Refresh(boolean quellAlerts) throws SQLException {
        
        String data[][];
        long currentUtime;

        m_status.setText("Populating attachment list...");

        // get current db time
        synchronized (m_jdbcUpdate) {
            //m_jdbcUpdate.getSqlData("select max(utime) from email");
            //data = m_jdbcUpdate.getRowData();
            data = m_jdbcUpdate.getSQLData("select max(utime) from email");
            try {
                currentUtime = Long.parseLong(data[0][0]);

                // don't do this in forensic mode
                //  	if (m_nextUpdateUtime == 0)
                //  	{
                //  	  m_nextUpdateUtime = currentUtime - 60;
                //  	}
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error while getting current time from db: " + ex);
                return;
            }
            // get the user count (need this later)
            long[] userCount = getUserCount();

            // get all attachment hashes
            //m_jdbcUpdate.getSqlData("select distinct hash, filename, type,
            // size from message where (hash!='') and (hash!='null') and
            // (!isnull(hash)) order by hash asc");
            //data = m_jdbcUpdate.getRowData();
            data = m_jdbcUpdate
                    .getSQLData("select distinct hash, filename, type, size from message where (hash!='') and (hash!='null') and hash is not null order by hash asc");

            if (m_threadStop)
                return;

            // remember what was selected before
            /*
             * Attachment attCurrent = (Attachment)
             * m_comboAttachments.getSelectedItem(); String strCurrent = new
             * String(""); if (attCurrent != null) { strCurrent =
             * attCurrent.getSignature(); }
             * 
             * if (m_threadStop) return;
             */
            //m_repopulating = true;
            // insert all attachments
            //int prevCount = m_comboAttachments.getItemCount();
            for (int i = 0; i < data.length; i++) {
                if (m_threadStop)
                    return;

                /*
                 * if (data[i].length != 3) { throw new SQLException("Database
                 * returned wrong number of columns"); }
                 */
                if (!((data[i][0] == null) || (data[i][0].length() == 0))) {

                    //if (!m_htAttachments.containsKey(data[i][0]))
                    //   {
                    Attachment att = new Attachment(data[i][0], data[i][1], data[i][2], m_alerts, m_alertOptions);//,
                                                                                                                  // this);
                    try {
                        att.setSize(Integer.parseInt(data[i][3]));
                    } catch (NumberFormatException nm) {
                    }

                    //	m_htAttachments.put(data[i][0], att);
                    /*
                     * // find right place in combo box if
                     * (m_comboAttachments.getItemCount() > 0) { int j = 0;
                     * while ((j <m_comboAttachments.getItemCount()) &&
                     * (data[i][0].compareTo(((Attachment)m_comboAttachments.getItemAt(j)).getSignature()) >
                     * 0)) { j++; } m_comboAttachments.insertItemAt(att, j); }
                     * else { m_comboAttachments.addItem(att); }
                     */
                    m_attachment_model.addRow(att);
                    //sorter.reallocateIndexes();
                    //  }
                }
            }

            calculateMetrics(currentUtime, userCount, quellAlerts);

            // tell tables to update their values
            updateStatisticsTable(0);
            updateMessagesTable(0);
        }

        m_nextUpdateUtime = currentUtime + 1;
        m_repopulating = false;
    }

    private void calculateMetrics(long currentUtime, long[] userCount, boolean quellAlerts)
            throws SQLException {
        //long elapsed = 0;
       // long before;//, after;
        BusyWindow bw = new BusyWindow("Calc Metrics", "Progress",true);
        bw.setVisible(true);
        m_status.setText("Calculating origin and lifespan...");
        int max = m_attachment_model.getRowCount();
        for (int i = 0; i < max; i++)
        //m_comboAttachments.getItemCount(); i++)
        {
            if (m_threadStop)
                return;

            Attachment att = m_attachment_model.getItemAt(i);
            // m_comboAttachments.getItemAt(i);
            bw.progress(i,max);
            // calculate easy attachment metrics
            //--before = System.currentTimeMillis();
            att.calculateEasyMetrics(m_jdbcUpdate);
            //after = 
           //-- elapsed += System.currentTimeMillis() - before;
        }

        // calculate speed
        if (m_benchmark) {
            System.out.print("    speed... ");
        }
        bw.setMSG("speed");
        bw.progress(1, 5);
        m_status.setText("Calculating speed...");
       long before = System.currentTimeMillis();
        String[][] incidences = Attachment.calcOverallIncidencesForAll(m_jdbcUpdate);
        for (int i = 0; i < incidences.length; i++) {
            if (m_threadStop)
                return;

            Attachment att = m_attachment_model.getByHash(incidences[i][0]);
            
            try {
                if(att!=null)
                    att.calcOverallSpeed(Long.parseLong(incidences[i][1]), quellAlerts);
            } catch (NumberFormatException ex) {
                throw new SQLException("Database returned invalid incident count: " + ex);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        String[][] incidencesInt = Attachment.calcInternalIncidencesForAll(m_jdbcUpdate);
        for (int i = 0; i < incidencesInt.length; i++) {
            if (m_threadStop)
                return;

            Attachment att = m_attachment_model.getByHash(incidencesInt[i][0]);
            
            try {
            if(att!=null)    
                att.calcInternalSpeed(Long.parseLong(incidencesInt[i][1]), quellAlerts);
            } catch (NumberFormatException ex) {
                throw new SQLException("Database returned invalid incident count: " + ex);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        //after = System.currentTimeMillis();
        if (m_benchmark) {
            System.out.println(((System.currentTimeMillis() - before) / 1000.0) + " sec");
        }

        // calculate saturation
        if (m_benchmark) {
            System.out.print("    saturation... ");
        }
        bw.setMSG("Saturation");
        bw.progress(2, 5);
        m_status.setText("Calculating saturation...");
        before = System.currentTimeMillis();
        for (int i = 0; i < m_attachment_model.getRowCount(); i++)
        //m_comboAttachments.getItemCount(); i++)
        {
            if (m_threadStop)
                return;

            Attachment att = m_attachment_model.getItemAt(i);
            //m_comboAttachments.getItemAt(i);
            if(att!=null)
            att.calcSaturation(m_jdbcUpdate, userCount, quellAlerts);
        }
        //after = System.currentTimeMillis();
        if (m_benchmark) {
            System.out.println(((System.currentTimeMillis() - before) / 1000.0) + " sec");
        }

        // calculate birthrates
        if (m_benchmark) {
            System.out.print("    birthrates (pre)... ");
        }
        bw.setMSG("Pre BirthRate");
        bw.progress(3, 5);
        m_status.setText("Doing preliminary birthrate calculations...");
        before = System.currentTimeMillis();
        // calculate maximum overall and interal birthrate for each attachment
        // for each period
        String[][] brOvMin = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60, currentUtime,
                false);
        if (m_threadStop)
            return;
        String[][] brOvHour = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60 * 60,
                currentUtime, false);
        if (m_threadStop)
            return;
        String[][] brOvDay = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60 * 60 * 24,
                currentUtime, false);
        if (m_threadStop)
            return;
        String[][] brInMin = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60, currentUtime,
                true);
        if (m_threadStop)
            return;
        String[][] brInHour = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60 * 60,
                currentUtime, true);
        if (m_threadStop)
            return;
        String[][] brInDay = Attachment.calcPeriodBirthrateForAll(m_jdbcUpdate, m_nextUpdateUtime - 60 * 60 * 24,
                currentUtime, true);
        if (m_threadStop)
            return;
        //after = System.currentTimeMillis();
        if (m_benchmark) {
            System.out.println(((System.currentTimeMillis() - before) / 1000.0) + " sec");
        }

        if (m_benchmark) {
            System.out.print("    birthrates... ");
        }
        bw.setMSG("Birthrate");
        bw.progress(4, 5);
        before = System.currentTimeMillis();
        m_before = before;
        long secTicks = currentUtime - m_nextUpdateUtime;
        m_ticksSoFar = -1;
        m_totalTicks = brOvMin.length * (secTicks / 60 + secTicks / 3600 + secTicks / (3600 * 24)) * 2;
        tick();
        for (int i = 0; i < brOvMin.length; i++) {
            if (m_threadStop)
                return;

            Attachment att = m_attachment_model.getByHash(brOvMin[i][0]);
            
            try {
                if(att!=null)
                att.calcBirthrates(m_jdbcUpdate, m_nextUpdateUtime, currentUtime, Long.parseLong(brOvMin[i][1]), Long
                        .parseLong(brOvHour[i][1]), Long.parseLong(brOvDay[i][1]), Long.parseLong(brInMin[i][1]), Long
                        .parseLong(brInHour[i][1]), Long.parseLong(brInDay[i][1]), quellAlerts);
            } catch (NumberFormatException ex) {
                throw new SQLException("Database returned invalid birthrate: " + ex);
            }

            catch (NullPointerException ex) {
                // <TODO>Investigate why you might get a null pointer excpetion
                // here</TODO
            }
        }
        //after = System.currentTimeMillis();
        if (m_benchmark) {
            System.out.println(((System.currentTimeMillis() - before) / 1000.0) + " sec");
        }
        m_status.setText("Done");
        System.out.println("Done..");
        bw.setVisible(false);
        bw = null;
    }

    public void tick() {
        tick(1);
    }

    public void tick(long i) {
        m_ticksSoFar += i;
        if ((m_ticksSoFar % 100) != 0) {
            // don't waste too much time on status
            return;
        }

        long calcElapsed = System.currentTimeMillis() - m_before;
        String elapsedStr = Utils.millisToTimeString(calcElapsed);
        double percentDone = m_ticksSoFar / (double) m_totalTicks;
        String remainingStr = "?";
        if (m_ticksSoFar > 0) {
            long remaining = (long) java.lang.Math.floor((calcElapsed / (double) m_ticksSoFar)
                    * (m_totalTicks - m_ticksSoFar));
            remainingStr = Utils.millisToTimeString(remaining);
        }
        m_status.setText("Calculating Birthrates (" + elapsedStr + " elapsed, "
                + m_percentFormat.format(100 * percentDone) + "% complete, " + remainingStr + " remaining)");
    }

    /**
     * @returns count of all users in slot 0, count of intenral users in slot 1
     */
    private long[] getUserCount() throws SQLException {
        long[] ret = new long[2];

        HashMap hashmapUsers = new HashMap();
        HashMap hashmapInternalUsers = new HashMap();
        synchronized (m_jdbcUpdate) {
            PreparedStatement ps = m_jdbcUpdate
                    .prepareStatementHelper("select distinct sender,senderLoc,rcpt,rcptLoc from email");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sender = rs.getString(1);
                String senderLoc = rs.getString(2);//char??

                String rcpt = rs.getString(3);
                String rcptLoc = rs.getString(4);
                hashmapUsers.put(sender, "");
                hashmapUsers.put(rcpt, "");

                if (senderLoc.equals("I")) {
                    hashmapInternalUsers.put(sender, "");
                }
                if (rcptLoc.equals("I")) {
                    hashmapInternalUsers.put(rcpt, "");
                }

            }
            rs.close();
        }
        //--String query;

        //--query = "create temporary table AttachmentMetricsTemp (account
        // varchar(255), location char)";

        //--m_jdbcUpdate.updateSQLData(query);

        //--query = "insert into AttachmentMetricsTemp ";
        //--query += "select distinct sender, senderLoc ";
        //--query += "from email ";
        //--m_jdbcUpdate.updateSQLData(query);

        //--query = "insert into AttachmentMetricsTemp ";
        //--query += "select distinct rcpt, rcptLoc ";
        //--query += "from email ";
        //--m_jdbcUpdate.updateSQLData(query);

        //--query = "select count(distinct account) from
        // AttachmentMetricsTemp;";
        //m_jdbcUpdate.getSqlData(query);
        //--String data[][] = m_jdbcUpdate.getSQLData(query);
        //--if ((data.length != 1) || (data[0].length != 1))
        //-- {
        //-- throw new SQLException("Could not retrieve user count");
        //-- }

        ret[0] = hashmapUsers.size();
        ret[1] = hashmapInternalUsers.size();

        //--try
        //		-- {
        //		-- ret[0] = Long.parseLong(data[0][0]);
        //		-- }
        //		--catch (NumberFormatException ex)
        //		-- {
        //		-- throw new SQLException("Could not retrieve user count: " + ex);
        //		-- }
        //		--
        //		-- query = "select count(distinct account) from AttachmentMetricsTemp
        // where location=\"I\";";
        //		-- //m_jdbcUpdate.getSqlData(query);
        //		-- data = m_jdbcUpdate.getSQLData(query);
        //		-- if ((data.length != 1) || (data[0].length != 1))
        //		-- {
        //		-- throw new SQLException("Could not retrieve user count");
        //		-- }

        //		-- try
        //		-- {
        //		-- ret[1] = Long.parseLong(data[0][0]);
        //		-- }
        //		--//-- catch (NumberFormatException ex)
        //		-- {
        //		-- throw new SQLException("Could not retrieve user count: " + ex);
        //		-- }

        return ret;
    }

    //--finally {
    //-- if (!m_jdbcUpdate.getTypeDB().equals(DatabaseManager.sDERBY)) {
    //-- m_jdbcUpdate.updateSQLData("drop table AttachmentMetricsTemp");
    //-- }
    //-- }
    //	-- }

    private class AttachmentTableModel extends DefaultTableModel {
        //Vector dataVector;
        Hashtable m_htAttachments; // attachments we've seen

        String[] columnsAttach;

        int c_rows = 0;

        Vector dataVector;

        public AttachmentTableModel(String[] cls) {
            super(cls, 1);
            //	    System.out.println("2");
            dataVector = new Vector();

            //dataVector = new Vector();
            m_htAttachments = new Hashtable();
            columnsAttach = new String[cls.length];
            for (int i = 0; i < cls.length; i++) {
                columnsAttach[i] = cls[i];

            }
            //System.out.println("dne");
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public int getColumnCount() {
            return 5;
        }

        public int getRowCount() {
            return c_rows;
        }

        //	    	    return ((Vector)super.getValueAt(0,0)).size();}
        public String getColumnName(int column) {
            return columnsAttach[column];
        }

        public Class getColumnClass(final int column) {
            //if(column == 1)
            //    return java.lang.Double.class;
            //else
            if (column == 4) {
                return java.lang.Integer.class;
            }
            return java.lang.String.class;
        }

        public Object getValueAt(int row, int col) {
            Attachment temp = (Attachment) dataVector.get(row);
            if (col == 0)
                return temp.getFilename();
            else if (col == 1)
                return "" + temp.getSize();
            else if (col == 2)
                return "" + temp.getMimeType();
            else if (col == 3)
                return temp.getSignature();
            else
                return new Integer(temp.getGroup());
        }

        public final Attachment getByHash(String hash) {
            
            if(m_htAttachments.containsKey(hash)){
                return (Attachment) dataVector.get(((Integer)m_htAttachments.get(hash)).intValue());
            }
            return null;
           // int n = Integer.parseInt((String) m_htAttachments.get(hash));
           // return (Attachment) dataVector.get(n);

        }

        public final void setGroup(int row, int group) {
            ((Attachment) dataVector.elementAt(row)).setGroup(group);
        }

        public Attachment getItemAt(int row) {
            return (Attachment) dataVector.get(row);
        }

        public void clear() {
            m_htAttachments.clear();
            dataVector.clear();
            c_rows = 0;

        }

        public boolean resetHolddowns() {
            for (int i = 0; i < c_rows; i++) {
                ((Attachment) dataVector.elementAt(i)).setClassification(Attachment.BENIGN);
                //Attachment att = (Attachment) enum.nextElement();
                //att.setClassification(Attachment.BENIGN);
            }
            return true;
        }

        public void addRow(Attachment att) {

            if (!m_htAttachments.containsKey(att.getSignature())) {
                //att.setGroup(c_rows);
                m_htAttachments.put(att.getSignature(),new Integer(c_rows));

                c_rows++;
                dataVector.add(att);
            }

        }

    }

    private class AttachStatsThread implements Runnable {

        boolean emt = true;
        long currentUtime;
      
        boolean quellAlerts;
        
        public AttachStatsThread(boolean running,long _currentUtime,  boolean _quellAlerts){
            emt= running;
            currentUtime = _currentUtime;
            
            quellAlerts = _quellAlerts;
            
        }
        
        
        
        public void run() {
            m_threadStop = false;

            m_winGui.setEnabled(false);
            m_butAlerts.setText(STOP_ALERTS);
            m_butAlerts.setEnabled(true); // except keep button enabled

            //m_comboAttachments.removeItem(INITIAL_MESSAGE);
            try {
                if (resetUpdateUtime() && resetHolddowns()) {
                             
                    if(emt){
                        calculateMetrics(currentUtime, getUserCount(), false);

                        // tell tables to update their values
                        updateStatisticsTable(0);
                        updateMessagesTable(0);
                    }
                    else{
                    Refresh(false);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(m_winGui, "MET test failed: " + ex);
            }
            m_butAlerts.setText(START_ALERTS);
            if (m_threadStop) {
                m_status.setText("Cancelled");
            } else {
                m_status.setText("Done");
            }
            m_winGui.setEnabled(true);
            m_statsThread = null;
            m_threadStop = false;
        }
    }
}
