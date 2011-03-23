/**
 * 
 */
package metdemo.Tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * @author Shlomo
 *
 */
public class overviewHistoryPanel extends JPanel implements MouseMotionListener {

	private ArrayList<double[]> historicalData;
	private int CurrentMonth = -1;
	private int maxmonth =0;
	private TimeSeries jfplot_series,jfplot_seriesLeft,jfplot_seriesRight;
	private JFreeChart jfplotMain, jfplotLeft,jfplotRight;
	private double[]stddev;
	private double[]distance;
	private int offsetMonth =0;
	//new XYSeries("Email");
	//HistogramDataset dataset;
	// private TimeSeries jfplot_series = new TimeSeries("Emails", "Month-Year", "Count", Month.class);
	
	public overviewHistoryPanel(final ArrayList<double[]> insertData,int monthcount, String startDatePeriod,double []thestddev) {
		//first lets grab the data
		historicalData = insertData;
		jfplot_series =   new	TimeSeries("Email profile from: " + startDatePeriod, "Hour", "Count", Hour.class);
		jfplot_seriesLeft =   new	TimeSeries("Email profile from: " + startDatePeriod, "Hour", "Count", Hour.class);
		jfplot_seriesRight =   new	TimeSeries("Email profile from: " + startDatePeriod, "Hour", "Count", Hour.class);
		offsetMonth = Utils.getSQLmonth(startDatePeriod)-1;
		int startyear = Utils.getSQLyear(startDatePeriod);
		stddev = new double[thestddev.length];
		distance = new double[monthcount];
		distance[0] = 0;//since no background to compare
		for(int i=0;i<thestddev.length;i++){
			stddev[i] = thestddev[i];
		}
		
		
		
		setLayout(new BorderLayout());
		
		JPanel buttonpanel = new JPanel();
		
		buttonpanel.setLayout(new GridLayout(1,monthcount));
		
		maxmonth = monthcount;
		
		for(int i=0;i<monthcount;i++){
			
			JButton tempbut = new JButton(MonthLookup(offsetMonth+i) + " " + (startyear+((offsetMonth+i)/12)));
			tempbut.setName(""+i);
			tempbut.addMouseMotionListener(this);
			buttonpanel.add(tempbut);
			
			//calculate difference between the months
			if(i>0){
			
			distance[i] = DistanceCompute.getDistance(historicalData.get(i-1), historicalData.get(i), stddev, "mahalanobis");
			
			}
			
			
		}
		
		JScrollPane buttonscroller = new JScrollPane(buttonpanel);
		buttonscroller.setPreferredSize(new Dimension(400,100));
		add(buttonscroller,BorderLayout.SOUTH);
		
		//need to setup the jfreechart
		 jfplotMain = ChartFactory.createXYBarChart(
                "Per Month Email History",
                "Hourly",
                true,
                "% of Email",
                new TimeSeriesCollection(jfplot_series),
                PlotOrientation.VERTICAL,
                true,
                false,
                false
            );	
		 
		jfplotLeft = ChartFactory.createXYBarChart(
                    "Per Month Email History",
                    "Hourly",
                    true,
                    "% of Email",
                    new TimeSeriesCollection(jfplot_seriesLeft),
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false
                );
		jfplotRight = ChartFactory.createXYBarChart(
                "Per Month Email History",
                "Hourly",
                true,
                "% of Email",
                new TimeSeriesCollection(jfplot_seriesRight),
                PlotOrientation.VERTICAL,
                true,
                false,
                false
            );
		
		
		
		 jfplotMain.setBackgroundPaint(Color.white);
		 jfplotRight.setBackgroundPaint(Color.white);
		 jfplotLeft.setBackgroundPaint(Color.white);
		 
	        XYPlot plot = jfplotMain.getXYPlot();
	      //  XYItemRenderer renderer = plot.getRenderer();
	       /* StandardXYToolTipGenerator generator = new StandardXYToolTipGenerator(
	            "{1} = {2}", new SimpleDateFormat("HH"), new DecimalFormat("0"));
	        renderer.setToolTipGenerator(generator);*/
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setRangeGridlinePaint(Color.white);
	        DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
	        axis.setLowerMargin(0.01);
	        axis.setUpperMargin(0.01);
		        
		 ChartPanel chartholder = new ChartPanel(jfplotMain);       
		 	        chartholder.setPreferredSize(new Dimension(300,200));
		add(chartholder,BorderLayout.CENTER);
		//now for left side
		plot = jfplotLeft.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        axis = (DateAxis) plot.getDomainAxis();
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        axis.setLowerMargin(0.01);
        axis.setUpperMargin(0.01);

        ChartPanel chartholder2 = new ChartPanel(jfplotLeft);       
        chartholder2.setPreferredSize(new Dimension(300,200));
		add(chartholder2,BorderLayout.WEST);
		//now for right
		plot = jfplotRight.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        axis = (DateAxis) plot.getDomainAxis();
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        axis.setLowerMargin(0.01);
        axis.setUpperMargin(0.01);

        ChartPanel chartholder3 = new ChartPanel(jfplotRight);       
        chartholder3.setPreferredSize(new Dimension(300,200));
		add(chartholder3,BorderLayout.EAST);
		
		
		setPreferredSize(new Dimension(900,400));
		
	}


	private String MonthLookup(int i){
		switch (i%12) {
		case 11:
			return "Dec";
		case 10:
			return "Nov";
		case 9:
			return "Oct";
		case 8:
			return "Sep";
		case 7:
			return "Aug";
		case 6:
			return "Jul";
		case 5:
			return "Jun";
		case 4:
			return "May";
		case 3:
			return "Apr";
		case 2:
			return "Mar";
		case 1:
			return "Feb";
		default:
			return "Jan";
		}
	}
	
	
	
	
	
	
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
		int selected = Integer.parseInt(((JButton)e.getSource()).getName());;
		if(selected == this.CurrentMonth){
			return;
		}
		
		//else we need to draw it
		
		// dataset.addSeries("H1", values, 100, 2.0, 8.0);
		/*dataset.addSeries("h", historicalData.get(selected),24,0,100 );
		
		dseries.removeAllSeries();
		dseries.addSeries(new HistogramDataset())*/
		jfplot_seriesRight.clear();
		jfplot_seriesLeft.clear();
		//mainplot
		jfplot_series.clear();
		//jfplotMain.setTitle("Month: " + MonthLookup(selected));
		jfplotMain.setTitle(new TextTitle(
				"Month: " + MonthLookup(offsetMonth+selected) +"\nDistance to last Month" + distance[selected],
                new Font("Dialog", Font.ITALIC, 10)));
		double []dataitems = historicalData.get(selected);
		Day x = new Day();
		for(int i=0;i<dataitems.length;i++){
			jfplot_series.add(new Hour(1+i,1,selected%12+1,2006),dataitems[i]);
        	
		}
		//left side
		if(selected > 0){
			
			jfplotLeft.setTitle("History Month: " + MonthLookup(offsetMonth+selected-1));
			dataitems = historicalData.get(selected-1);
			x = new Day();
			for(int i=0;i<dataitems.length;i++){
				jfplot_seriesLeft.add(new Hour(1+i,1,(selected-1)%12+1,2006),dataitems[i]);
	        	
			}
		}else{
			jfplotLeft.setTitle("");
		}
		if(selected < maxmonth-1){
			
			jfplotRight.setTitle("History Month: " + MonthLookup(offsetMonth+selected+1));
			dataitems = historicalData.get(selected+1);
			x = new Day();
			for(int i=0;i<dataitems.length;i++){
				jfplot_seriesRight.add(new Hour(1+i,1,(selected+1)%12+1,2006),dataitems[i]);
	        	
			}
		}else{
			jfplotRight.setTitle("");
		}
		
		
		
	}
	
	
}
