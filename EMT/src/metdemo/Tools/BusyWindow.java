//shlomo hershkop
//columbia university
//summer 2002

package metdemo.Tools;

import javax.swing.*;
import java.awt.*;
import javax.swing.JProgressBar;
import java.awt.event.*;

//import java.awt.Dimension;

/**
 * This Class is meant to be used as a busy window
 * <P>
 * It will show a title, progress bar and cancel button
 * 
 * @author Shlomo Hershkop
 */

public class BusyWindow extends JFrame implements ActionListener
// JFrame//JDialog
{
    private JLabel label;
    private JProgressBar progressBar;
    private JButton m_cancel = new JButton("CANCEL");
    private boolean kill;
    static int m_x = 300, m_y = 50;
    private int max = 100;


/**
 * Default constructor to call
 * @param title to show on top
 * @param msg to display while busy
 * @param knowntask if we know how long it will take (else it will flash 
 */
    public BusyWindow(final String title, final String msg, boolean knowntask) {

        this(null, title, msg, knowntask);

    }
    
    /**
     * this will trigger a repaint ona current section of the window
     * @param progressRect
     */
    /*private void letsPaint(Rectangle progressRect){
        progressRect.x = 0;
        progressRect.y = 0;
        progressBar.paintImmediately( progressRect );
    }*/
    

    public BusyWindow(final Component parent, final String title, final String msg,boolean knowntask) {
        setTitle(title);
        setSize(new Dimension(450, 130));
        progressBar = new JProgressBar(1, max);
        progressBar.setVisible(true);
        if(!knowntask)
        {
        	progressBar.setIndeterminate(true);//dont know how ling its going to take
        }
        progressBar.setStringPainted(true);
        m_cancel.addActionListener(this);
        label = new JLabel(msg);

        getContentPane().add(label, BorderLayout.NORTH);
        getContentPane().add(progressBar, BorderLayout.CENTER);
        getContentPane().add(m_cancel, BorderLayout.SOUTH);

        // Rectangle bounds = getBounds();
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
        Point p = getLocation();
        setLocation(m_x - 300 + p.x, m_y - 50 + p.y);
        updatexy();
        kill = false;
        pack();
        //letsPaint(progressBar.getBounds());

    }

    /** update the x y so that subsequent windows dont cover each other up */
    public final void updatexy() {
        m_x += 30;
        m_y += 5;
        if (m_x > 500)
            m_x = 301;
        if (m_y > 70)
            m_y = 51;
    }

    /** sets up the message */
    public final void setMSG(final String s) {
        label.setText(s);
        getContentPane().validate();
        getContentPane().repaint();
    }

    /** set max on the progress bar */
    public final void setMax(final int m) {

        if (m > 0) {
            max = m;
            progressBar.setMaximum(m);
        }

        getContentPane().validate();
        getContentPane().repaint();
    }

    /** update progress with some max specified earlier else defaults out of 100 */
    public final void progress(final int i) {
        progressBar.setValue(i);

   //     letsPaint(progressBar.getBounds());
    }

    public final void progress(final int i, final int max) {

        if (progressBar.getMaximum() != max) {
            progressBar.setMaximum(max);
            // progressBar = new JProgressBar(0,max);
            progressBar.setValue(i);
            progressBar.setStringPainted(true);

        } else {
            progressBar.setValue(i);
        }
     //   letsPaint(progressBar.getBounds());

    }

    public final boolean isAlive() {
        return !kill;
    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub

        if (arg0.getSource() == m_cancel) {
            m_cancel.setEnabled(false);
            kill = true;

        }
    }

}
