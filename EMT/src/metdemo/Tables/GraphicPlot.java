package metdemo.Tables;

import java.awt.*;
/*
 import java.awt.event.*;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.QuadCurve2D;
 import java.awt.geom.CubicCurve2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.FlatteningPathIterator;
 import java.awt.font.TextLayout;
 import java.awt.font.FontRenderContext;
 */
import javax.swing.*;

import javax.swing.event.*;

import metdemo.Tools.Utils;

//import java.awt.GridBagConstraints;
import java.util.Hashtable;
import java.awt.event.MouseListener;
//import java.awt.geom.GeneralPath;
//import java.awt.Rectangle;
//import java.awt.Scrollbar;

/**
 * class to draw out a historgram and do some fancy things to it
 * 
 * 
 * @author Shlomo Hershkop
 */
public class GraphicPlot extends JPanel {

	private GraphicPlot2 m_gp2;
	private JPanel m_controls;
	private JSlider m_bin_choose;
	private int MAX = 60;

	public GraphicPlot() {
		this.setBackground(Color.white);

		//setup the contorl parts
		m_controls = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		m_controls.setLayout(gridbag);
		//the slider to control the histograms.
		m_bin_choose = new JSlider(1, MAX, MAX);//its percentage so only makes
												// sense 50 etc
		m_bin_choose.setMajorTickSpacing(3);
		m_bin_choose.setSnapToTicks(true);
		m_bin_choose.setPaintTicks(true);
		m_bin_choose.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				changeBins();
			}
		});
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(MAX), new JLabel("More"));
		labelTable.put(new Integer(1), new JLabel("Less"));
		m_bin_choose.setLabelTable(labelTable);
		m_bin_choose.setPaintLabels(true);
		m_bin_choose.setToolTipText("Specify bin size");

		constraints.gridx = 0;
		constraints.gridy = 0;
		//constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);

		gridbag.setConstraints(m_bin_choose, constraints);
		m_controls.add(m_bin_choose);

		m_gp2 = new GraphicPlot2();

		this.setLayout(new BorderLayout());
		this.add(m_gp2, BorderLayout.CENTER);
		this.add(m_controls, BorderLayout.SOUTH);

	}

	//redo historgrams
	public final void changeBins() {
		m_gp2.changeBins(m_bin_choose.getValue());
	}

	public final void setTitle(final String s) {
		m_gp2.setTitle(s);
	}

	public final void setXLabel(final String s) {
		m_gp2.setXLabel(s);
	}

	public final void setYLabel(final String s) {
		m_gp2.setYLabel(s);
	}

	public final void setStuff(final double a[], final int n) {
		m_gp2.setStuff(a, n);
		m_bin_choose.setValue(MAX);
	}

	private class GraphicPlot2 extends JPanel implements MouseListener {

		private int x1, y1;
		private int x2, y2;
		private double stuff[] = {3, 18, 17, 13, 10, 7, 4, 5, 6, 7, 10, 2, 5, 1, 16, 15, 11};
		private int bins = 16;
		private int stuff_length = 16; //max bins
		private String m_title, m_xlabel, m_ylabel;

		/** set the num bins */
		public final void setBins(final int a) {
			bins = a;
		}

		public final void setStuff(final double[] d, final int len) {
			//System.out.println("in set stuff with len : " + len);

			stuff = new double[len];
			for (int i = 0; i < len; i++) {
				stuff[i] = d[i];
			}
			stuff_length = len;
			bins = len;
			repaint();
		}

		public GraphicPlot2() {
			//        setLayout(new BorderLayout());
			setBackground(Color.white);
			addMouseListener(this);
			x1 = -1;
			y1 = -1;
			m_title = new String();
			m_xlabel = new String();
			m_ylabel = new String();

		}

		public final void changeBins(final int n) {

			double per = (double) n / 100;
			int b = (int) (stuff_length * per);
			//System.out.println("n is " + n + " and b is " + b);
			if (b < 1)
				b = 1;

			if (b >= 1 && b <= stuff_length) {
				bins = b;
				repaint();
			}
		}

		public final void setTitle(final String s) {
			m_title = s;
		}

		public final void setXLabel(final String s) {
			m_xlabel = s;
		}

		public final void setYLabel(final String s) {
			m_ylabel = s;
		}

		public final void drawDemo(final int w, final int h, final Graphics2D g2) {

			int y = 0;

			// draws the word EMT DEMO

			/*
			 * FontRenderContext frc = g2.getFontRenderContext(); TextLayout tl =
			 * new TextLayout("EMT Data", g2.getFont(), frc); float xx = (float)
			 * (w*.5-tl.getBounds().getWidth()/2); tl.draw(g2, xx,
			 * 12);//getAscent());
			 *  // draws the word "CubicCurve2D" tl = new
			 * TextLayout("HISTOGRAM", g2.getFont(), frc); xx = (float)
			 * (w*.5-tl.getBounds().getWidth()/2); tl.draw(g2, xx, h*.75f -3);
			 * g2.setStroke(new BasicStroke(5.0f)); //m_init=true;
			 */
			//	g2.setColor(Color.white);
			//g2.fill(new Rectangle(4,4,w-4,(int)(h*.75)));
			g2.setColor(Color.black);
			g2.drawString(m_title, (int) (w * .45), (int) (h * .08));
			/*
			 * int eofill = GeneralPath.WIND_EVEN_ODD; GeneralPath p = new
			 * GeneralPath(eofill); //border aournd p.moveTo(1,1); p.lineTo(w
			 * -1, 1);//top right // p.lineTo(w-4,h*.75f); //p.lineTo(4,h*.75f);
			 * //p.lineTo(w-4,h*.75f); p.lineTo(w-1,h-1);//bottom right
			 * p.lineTo(1, h-1);//bottom rleft p.lineTo(1,1); //top left again
			 * p.closePath(); BasicStroke bs; float dash[] = { w / 10.0f }; int
			 * penwidth = 1,caps =
			 * BasicStroke.CAP_ROUND,join=BasicStroke.JOIN_ROUND; bs = new
			 * BasicStroke((float) penwidth, caps, join, 10.0f, dash, 0.0f);
			 * g2.draw(bs.createStrokedShape(p));
			 */
			//	g2.setColor(colors[Math.abs(x1%3)]);
			g2.setColor(Color.blue);
			double sum = 0;
			double max = 0;
			double temp = 0, temp2 = 0;
			int i = 0;
			int bunch = 0;
			//need to draw stuff
			//first to see average. that will be the scale to show up at the
			// middle
			bunch = (stuff_length / bins);
			int c = 0;

			for (c = 0, i = 0; i < stuff_length; c++, i = i + bunch) {
				temp = 0;

				for (int j = 0; j < bunch; j++) {
					if (i + j < stuff_length) {
						temp += stuff[i + j];
						sum += stuff[i + j];
					}
				}
				temp = temp / bunch;//average out
				if (temp > max)
					max = temp;
			}
			double num, average = sum / stuff_length;
			int h1, h2, startx, starty, incx, maxy;

			starty = (int) (h * .9);
			startx = (int) (w * .1);

			incx = (int) ((w - (2 * startx)) / (c));
			//(int)( (double)(w-(2*startx))/(double)bins+1);
			//	System.out.println("bunch is "+ bunch+" bins : " + bins +" w is "
			// + w+ " startx "+startx +" incx "+incx);
			maxy = (int) (h * .8);
			//vertical line
			g2.fillRect(startx - 10, starty - maxy - 10, 5, maxy + 10); //x y w
																		// h

			//now to draw numbers on the vertical part
			for (i = 0; i < 10; i++) {
				num = Utils.round(max - (max * i / 10), 2);
				h2 = (i * ((maxy) / 10) + starty - maxy);
				g2.drawString("" + num, startx - 50, h2); //the num as a
														  // portion of max
				g2.fillRect(startx, h2, 20 + (w - (2 * startx)), 1);//x y w h
			}
			//horiz bar under the histogram
			g2.fillRect(startx - 10, starty, 20 + (w - (2 * startx)), 5);
			//put in the histogram
			g2.drawString(m_xlabel, (int) (w * .45), starty + 30);
			if (incx < 2)
				incx = 2;

			for (c = 0, i = 0; i < stuff_length; c++, i += bunch) {

				temp = 0;
				temp2 = 0;
				for (int j = 0; j < bunch; j++) {
					if (i + j < stuff_length) {
						temp += stuff[i + j];
						sum += stuff[i + j];
					}
				}
				temp2 = temp;
				temp = temp / bunch;//average out

				h1 = (int) (maxy * (temp / max)); //use it to flip the box
												  // upwards

				if (h1 < 5 && temp2 > 0) //min bar height if have any emails
					h1 = 5;

				g2.setColor(Color.yellow);

				g2.fillRect(startx + c * incx, starty - h1, incx - 1, h1);//x y
																		  // w h
				if (c < 50 && c % 2 == 1) {
					g2.setColor(Color.black);
					g2.drawString("" + (1 + c), (startx + c * incx) + (.25f * incx), starty + 20);
				} else {
					if (c % 70 == 0) {
						g2.setColor(Color.black);
						g2.drawString("" + (1 + c), (startx + c * incx) + (.25f * incx), starty + 20);

					}

				}

			}
			g2.drawString(m_ylabel, 15, 20);
			//scroll bars
			//	g2.draw( ranger = new Scrollbar(Scrollbar.HORIZONTAL, 0, 60, 0,
			// 300));
			//System.out.println("c is " + c);

			/*
			 * g2.setColor(Color.blue);
			 * g2.fillRect(startx-10,(int)(h*.85),(int)(w*.25),5);//bar
			 * 
			 * num = startx-10+(int)(w*.25);
			 * 
			 * 
			 * //System.out.println("1: " + (startx-10) + " 2:" + (int)num + "
			 * 3:" + (int)(num/2)); g2.drawString("1", startx-10,(int)(h*.91) );
			 * g2.drawString(""+stuff_length,(int)num ,(int)(h*.91) );
			 * g2.drawString(""+(stuff_length/2),(int)((startx-10)+((w*.25)/2))
			 * ,(int)(h*.91) );
			 * 
			 * 
			 * num = (num *bins)/stuff_length ; g2.setColor(Color.red);
			 * g2.fillOval( (int)num-2 ,(int)(h*.84)-1,20,20);//x y w h
			 */

			/*
			 * if(x1!=-1 && x2!=-1) {
			 * 
			 * int t; if(x2> x1) x2 = x2-x1; else { t = x1; x1 = x2; x2 = t; x2 =
			 * x2-x1; }
			 * 
			 * if(y2>y1) y2 = y2-y1; else {t = y1; y1 = y2; y2 = t; y2 = y2-y1; }
			 * 
			 * g2.fill(new Rectangle2D.Float(x1, y1, x2, y2)); x1=-1; y1=-1;
			 * x2=-1; y2=-1;
			 * 
			 *  }
			 */

		}//end draw demo

		public void paint(Graphics g) {

			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(getBackground());

			g2.clearRect(0, 0, d.width, d.height);
			drawDemo(d.width, d.height, g2);
		}

		/** process mouse events */
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			if (x1 == -1) {
				x1 = x;
				y1 = y;
			} else {
				x2 = x;
				y2 = y;
			}

			System.out.println("Got a click: " + evt.getSource());
			repaint();
		}

		public void mousePressed(java.awt.event.MouseEvent e) {
		}

		public void mouseReleased(java.awt.event.MouseEvent e) {

		}

		public void mouseEntered(java.awt.event.MouseEvent e) {
		}
		public void mouseExited(java.awt.event.MouseEvent e) {

		}

	}
}//end graphic plot
