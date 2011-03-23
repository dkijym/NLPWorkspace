/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.Tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

//import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

public class popupCalendar extends JFrame implements ActionListener {

	private JButton btns[] = new JButton[49];
	private JButton btnNext = new JButton("Next Month");
	private JButton btnNextYear = new JButton("Next Year");
	private JButton btnLast = new JButton("Last Month");
	private JButton btnLastYear = new JButton("Last Year");
	private JLabel lblMth = new JLabel();
	private JLabel lblYr = new JLabel();
	private Calendar cal;// = Calendar.getInstance();
	private static String months[] = {"January", "February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December"};
	private int mth = 0;
	private int yr = 0;

	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	private int selected = 0;
	private JButton dateinfo;

	public popupCalendar(JButton jl, Calendar c, int day, String t) {

		super(t);
		dateinfo = jl;
		cal = c;
		dateinfo.setText(sdf.format(cal.getTime()));

		selected = day - 1; //its -1 cause of starting from 0th
		//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Container frame = getContentPane();
		getContentPane().setLayout(new BorderLayout());
		setSize(400, 300);
		mth = cal.get(Calendar.MONTH);
		yr = cal.get(Calendar.YEAR);

		JPanel top = new JPanel(new FlowLayout());
		top.add(lblMth);
		top.add(lblYr);

		JPanel mid = new JPanel(new GridLayout(7, 7));
		String days[] = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
		for (int i = 0; i < btns.length; i++) {
			btns[i] = new JButton();
			btns[i].setFont(new Font("Arial", Font.BOLD, 12));
			btns[i].setPreferredSize(new Dimension(10, 10));
			if (i < 7) {
				btns[i].setText(days[i]);
			} else {
				btns[i].addActionListener(this);
				btns[i].setFont(new Font("Arial", Font.BOLD, 12));
			}
			mid.add(btns[i]);
		}
		btnNext.addActionListener(this);
		btnLast.addActionListener(this);
		btnNextYear.addActionListener(this);
		btnLastYear.addActionListener(this);

		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(btnLastYear);
		bottom.add(btnLast);
		bottom.add(btnNext);
		bottom.add(btnNextYear);
		getContentPane().add(top, BorderLayout.NORTH);
		getContentPane().add(mid, BorderLayout.CENTER);
		getContentPane().add(bottom, BorderLayout.SOUTH);
		setDates();
		//	pack();
		//setContentPane(frame);
		setVisible(false);

	}

	public Calendar getCalendar() {
		return cal;
	}

	private void setDates() {
		for (int i = 7; i < btns.length; i++) {
			btns[i].setText("");
		}
		cal.set(yr, mth, selected+1);
		lblMth.setText(months[cal.get(Calendar.MONTH)]);
		lblYr.setText(String.valueOf(cal.get(Calendar.YEAR)));
		int firstDay = cal.get(Calendar.DAY_OF_WEEK) - ((selected) % 7) -1;
		if(firstDay<0)
			firstDay+=7;
		
		firstDay +=7;//since we have days marked there on the array
		int ii = 1;
		int daysmonth = 30;//feb will be wrong sometimes.
		//1 3 5 7 8 10 12
		if (mth == 0 || mth == 2 || mth == 4 || mth == 6 || mth == 7 || mth == 9 || mth == 11) {
			daysmonth = 31;
		}
		else if(mth==1 && yr%4!=0){
			daysmonth = 29;
		}
		
		for (int i = firstDay; i < firstDay + daysmonth; i++) {

			if (i == firstDay + selected)
				btns[i].setBackground(Color.RED);
			else
				btns[i].setBackground(Color.gray);
			btns[i].setText(String.valueOf(ii));

			ii++;
			//cal.add(Calendar.DATE, 1);
			//if (cal.get(Calendar.MONTH) != Math.abs((120 + mth) % 12))
			//	break; //only go
			// back 10
			// years
		}
		//cal.set(yr, mth, selected + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnLast) {
			mth--;
			setDates();
		} else if (ae.getSource() == btnNext) {
			mth++;
			setDates();
		} else if (ae.getSource() == btnLastYear) {
			yr--;
			setDates();
		} else if (ae.getSource() == btnNextYear) {
			yr++;
			setDates();
		} else {
			for (int i = 7; i < btns.length; i++) {
				if (ae.getSource() == btns[i]) {
					//Calendar cal2 = Calendar.getInstance();
					//check if clikced on empty button
					if (btns[i].getText().equals("")) {
						return;
					}


					cal.set(yr, mth, Integer.parseInt(btns[i].getText()));
					selected = cal.get(Calendar.DAY_OF_MONTH) - 1;
					String title = sdf.format(cal.getTime());

					//JOptionPane.showMessageDialog(null, "You selected "
					//		+ title);
					setDates();
					this.setVisible(false);
					//ActionEvent evt = new ActionEvent(this, 0, "Data
					// Loaded");
					//TODO find better way
					dateinfo.setText(title);
					//					FontMetrics fm = getFontMetrics(dateinfo.getFont());
					//				dateinfo.setMaximumSize(new
					// Dimension(fm.stringWidth(title), fm.getAscent() +
					// fm.getDescent()));

					//dateinfo.setText(title);
					dateinfo.repaint();
					//action(new Event("date change",0,"date"),this);
					break;
				}
			}
		}

	}
}