/*
 * @version 1.3
 * @author Wei-Jen Li
 *
 * This is for the class 'AddressList'.
 * To show result of the dummy testing.
 */

package metdemo.Tools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import metdemo.AlertTools.AlertPoint;

public class HellingerResult extends JDialog{  
	
	private JScrollPane scrP, forChart;
	
	private JPanel buttonP, outputP;
	//private JPanel chart;
	private ChartPane chart;
	
	public HellingerResult(Component parent, String[] result
	, String msg1, String msg2, String msg3){
	    //public HellingerResult(String[] result
	    //	, String msg1, String msg2, String msg3){
		
		outputP = new JPanel();
		outputP.setLayout(new GridLayout(result.length,1));
		for(int i=0; i<result.length; i++){
			JLabel label = new JLabel(result[i]);
			if(result[i].indexOf("Alert")!=-1){
				label.setForeground(Color.red);
			}
			else{
				label.setForeground(Color.blue);
			}
			outputP.add(label);
		}
		scrP = new JScrollPane(outputP);
		
		buttonP = new JPanel();
		//for the 'close' button
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){  
				public void actionPerformed(ActionEvent evt){ 
					setVisible(false);
				} 
			} );
		buttonP.add(close);
		setSize(700, 500);
		setTitle("Intrusion Alert");
		setLocationRelativeTo(parent);
		addWindowListener(new WindowAdapter()
			{  public void windowClosing(WindowEvent e)
				{  setVisible(false);
				}
			} );
		
		chart = new ChartPane(result, msg1, msg2, msg3);
		forChart = new JScrollPane(chart);
		/*****************/
		//chart.draw();
		getContentPane().add(forChart, "Center");
		getContentPane().add(scrP, "East");
		getContentPane().add(buttonP, "South");
		
		
	}
	
	
	public static void main(String[] args){
		String[] msg = new String[1234];
		for(int i=0; i<msg.length; i++){
			
			int ran = (int)(100*Math.random());
			if(ran%20 ==0){
				msg[i] = "Alert";
			}
			else{
				msg[i] = "";
			}
			msg[i] += String.valueOf(i)+"  xxxxxxxxxx";
		}
		
		for(int i=350; i<400; i++)
			msg[i] += "Virus";
		
		for(int i=355; i<402; i++)
			msg[i] += "Alert";
		//msg[350] += "Alert";
		//msg[352] += "Alert";
		//HellingerResult result = new HellingerResult(msg, "aaaaaaaaa", "bbbbbbbbbbbbb","ccccccccccc");
		//result.show();
		
	}
	
}

class ChartPane extends JPanel{
	
	private int length;
	private final int SHIFT_HEIGHT = 200;
	private final int NUM_OF_UNIT = 5;
	private final int CHART_WIDTH = 400;
	private final int CHART_HEIGHT = 100;
	private final int BASE = 20;
	private final int MED_LINE = 70 + SHIFT_HEIGHT;
	private final int END = 420;
	private final int UNIT_LENGTH = CHART_WIDTH/NUM_OF_UNIT;
	private AlertPoint[] TP;//true positive
	private AlertPoint[] FP;//false positive
	private AlertPoint[] VIRUS;//real virus
	private int endpoint = 0;
	private final int TRUE_POSITIVE = 1;
	private final int FALSE_POSITIVE = 2;
	private final int REAL_VIRUS = 3;
	private int numOfTP = 0;
	private int numOfFP = 0;
	private int numOfVirus = 0;
	private String msg1, msg2, msg3;
	
	public ChartPane(String[] result,String m1,String m2,String m3){
		
		length = result.length;
		msg1 = m1;
		msg2 = m2;
		msg3 = m3;
		
		parseMsg(result);
	}
	
	
	public void paintComponent(Graphics g){
		System.out.println("paint...");
		
		//initialize
		endpoint = 0;
		numOfTP = 0;
		numOfFP = 0;
		numOfVirus = 0;
		
		//compute something first
		endpoint = getEndpoint();
		
		//cell labels
		int[] unitLabel = new int[NUM_OF_UNIT];
		for(int i=0; i<NUM_OF_UNIT; i++)
			unitLabel[i] = (endpoint/5)*(i+1);
		
		//the white background
		g.setColor(Color.white);
		g.fillRect(0,SHIFT_HEIGHT-100,CHART_WIDTH+BASE+50,CHART_HEIGHT+BASE+100);
		drawRanges(g);
		background(g, unitLabel);
		drawMsgUp(g);
		drawMsgDown(g);
	}
	
	public void drawMsgUp(Graphics g){
		
		g.setColor(Color.black);
		g.drawString("Correct Alert",75,140);
		g.drawString("False Positive",75,155);
		g.drawString("Real Malicious Emails",75,170);
		g.setColor(Color.blue);
		g.fillRect(10,135,50,3);
		g.setColor(Color.orange);
		g.fillRect(10,150,50,3);
		g.setColor(Color.red);
		g.fillRect(10,165,50,3);
		
		g.setColor(Color.blue);
		String total = String.valueOf(length);
		String fp = String.valueOf(numOfFP);
		g.drawString("Number of total emails:"+total,10,20);
		g.drawString("Number of false positive:"+fp,10,35);
		
		String vs = String.valueOf(numOfVirus);
		g.drawString("Number of real malicious emails:"+vs,10,50);
		String tp = String.valueOf(numOfTP);
		g.drawString("Number of correct alert:"+ tp,10,65);
		
		
		float tprate = (float)numOfTP/(float)numOfVirus;
		float fprate = (float)numOfFP/(float)length;
		float tnrate = 1-fprate;
		float fnrate = 1-tprate;
		
		String TPrate = String.valueOf(tprate);
		String FPrate = String.valueOf(fprate);
		String TNrate = String.valueOf(tnrate);
		String FNrate = String.valueOf(fnrate);
		if(TPrate.length()>6)TPrate=TPrate.substring(0,6);
		if(FPrate.length()>6)FPrate=FPrate.substring(0,6);
		if(TNrate.length()>6)TNrate=TNrate.substring(0,6);
		if(FNrate.length()>6)FNrate=FNrate.substring(0,6);
		
		g.setColor(Color.red);
		g.drawString("TP:"+TPrate,250,20);
		g.drawString("TN:"+TNrate,250,35);
		g.drawString("FP:"+FPrate,250,50);
		g.drawString("FN:"+FNrate,250,65);
	}
	
	public void drawMsgDown(Graphics g){
		g.setColor(Color.blue);
		g.drawString(msg1,10,350);
		g.drawString(msg2,10,370);
		g.setColor(Color.red);
		g.drawString(msg3,10,390);
	}
	
	public void drawRanges(Graphics g){
		
		g.setColor(Color.blue);
		for(int i=0; i<TP.length; i++){
			drawRange(g, TP[i], TRUE_POSITIVE);
			numOfTP += TP[i].end - TP[i].start + 1;
			//System.out.print(TP[i].start+"-"+TP[i].end+" ");
		}
		//System.out.println("\n");
		g.setColor(Color.orange);
		for(int i=0; i<FP.length; i++){
			drawRange(g, FP[i], FALSE_POSITIVE);
			numOfFP += FP[i].end - FP[i].start + 1;
			//System.out.print(FP[i].start+"-"+FP[i].end+" ");
		}
		
		//System.out.println("\n"+FP.length);
		g.setColor(Color.red);
		for(int i=0; i<VIRUS.length; i++){
			drawRange(g, VIRUS[i], REAL_VIRUS);
			numOfVirus += VIRUS[i].end - VIRUS[i].start + 1;
			//System.out.print(VIRUS[i].start+"-"+VIRUS[i].end+" ");
		}
		//System.out.println("done!!");
	}
	
	public void drawRange(Graphics g, AlertPoint p, int type){
		int start = getPos(p.start);
		int end = getPos(p.end);
		int width = end-start;
		if(width == 0) width++;
			
		if(type == TRUE_POSITIVE){
			g.fillRect(start+BASE,MED_LINE-40,width,40);
		}
		else if(type == FALSE_POSITIVE){
			g.fillRect(start+BASE,MED_LINE-40,width,40);
			//System.out.print(start+"-"+end+"::");
		}
		else{//REAL_VIRUS
			g.fillRect(start+BASE,MED_LINE-20,width,20);
		}
	}
	
	public int getPos(int position){
		double tmppos = ((double)position/(double)endpoint)*(double)CHART_WIDTH;
		return (int)Math.round(tmppos);
	}
	
	public void background(Graphics g, int[] unitLabel){
		//basic lines
		g.setColor(Color.black);
		g.drawLine(BASE,BASE+SHIFT_HEIGHT,BASE,CHART_HEIGHT+SHIFT_HEIGHT);
		g.drawLine(BASE,MED_LINE,CHART_WIDTH,MED_LINE);
		g.drawString("Emails sorted by time",BASE+100,MED_LINE+30);
		
		//for the cells
		//cell lines
		for(int i=1; i<=NUM_OF_UNIT; i++)
			g.drawLine(BASE+UNIT_LENGTH*i,MED_LINE
			,BASE+UNIT_LENGTH*i,MED_LINE-10);
		
		//cell values
		for(int i=0; i<NUM_OF_UNIT; i++)
			g.drawString(String.valueOf(unitLabel[i])
			,UNIT_LENGTH*(i+1)+10,MED_LINE+10);
		
		//the latest point
		int lengthPos = getPos(length);
		g.setColor(Color.magenta);
		g.drawLine(BASE+lengthPos,MED_LINE-10
			,BASE+lengthPos,MED_LINE+10);
		g.drawString(String.valueOf(length)
			,lengthPos+5,MED_LINE+20);
	}
	
	public int getEndpoint(){
		//how many digits
		int digits = (int)(Math.log((double)length) / Math.log(10));
		int base1 = (int)Math.pow(10,(double)digits); //1000....000
		int base2 = length/base1;
		int base3 = length%base1;
		if(base3 > base1/2){
			return base1 * (base2+1);
		}
		else{
			return base1 * base2 + base1/2;
		}
	}
	
	public void parseMsg(String[] msg){
		
		boolean[] tp = new boolean[length];//true positive
		boolean[] fp = new boolean[length];//false positive
		boolean[] virus = new boolean[length];//real virus
		
		for(int i=0;i<length;i++){
			String tmps = msg[i];
			if(tmps.indexOf("Alert")!=-1){
				if(tmps.indexOf("Virus")!=-1){
					tp[i] = true;
				}
				else{
					fp[i] = true;
				}
			}
			
			if(tmps.indexOf("Virus")!=-1){
				virus[i] = true;
			}
		}
		
		TP = new AlertPoint[length];
		FP = new AlertPoint[length];
		VIRUS = new AlertPoint[length];
		
		TP = getAlertPoint(tp);
		FP = getAlertPoint(fp);
		VIRUS = getAlertPoint(virus);
	}
	
	public AlertPoint[] getAlertPoint(boolean[] flags){
		AlertPoint[] alert = new AlertPoint[flags.length];
		int alertIndex = 0;
		
		boolean flag = false;
		AlertPoint tmp = new AlertPoint();
		for(int i=0; i<flags.length; i++){
			
			if(flags[i] == true && flag == false){
				tmp.addStart(i);
				flag = true;
			}
			else if(flags[i] == false && flag == true){
				tmp.addEnd(i-1);
				alert[alertIndex] = tmp;
				alertIndex++;
				flag = false;
				tmp = new AlertPoint();
			}
			
		}
		
		AlertPoint[] toReturn = new AlertPoint[alertIndex];
		System.arraycopy(alert,0,toReturn,0,alertIndex);
		return toReturn;
	}
	
	public void draw(){
		repaint();
	}
}
