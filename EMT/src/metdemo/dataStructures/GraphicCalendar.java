/**
 * Class to display a calendar span and call up different events based on what things are set.
 */
package metdemo.dataStructures;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JComponent;

import metdemo.search.SearchEMT;

/**
 * @author Shlomo
 * 
 */
public class GraphicCalendar extends JComponent implements ItemListener, ActionListener, MouseListener {

    private int width = 430;
    private int height = 600;

    static final int tons = 30;
    static final int many = 15;

    static final int startx = 30;
    static final int starty = 50;
    static final int yoffset = 25;// 30
    static final int xoffset = 25;
    static final int box_width = 18;// 18
    static final int box_height = 16;// 18
    static final int right_side = 200;
    int max_y_level = 0;
    final static String monthNames[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    static final Font bolder = new Font("serif", Font.BOLD/* +Font.ITALIC */, 14);

    private String currentStartDate;
    private String startDate;
    private String endDate;
    private dateCollection seenDatesResults;
    private int numberSeenDates = 0;
    private int selected = -1;
    private GregorianCalendar rightNowselected = new GregorianCalendar();
    GregorianCalendar rightNow = new GregorianCalendar();
    
    private int back_x, forward_x, back_y, forward_y, back_w, back_h, forward_w, forward_h;
    
    private boolean inAnimationSequence = false;
    private boolean inCalendarMode = true;
    private ArrayList<String> infoSheet = new ArrayList<String>();
    private SearchEMT parent;

    public GraphicCalendar(SearchEMT parentSearch) {
    		this(430,600,parentSearch);
	}
    
    
    
    public GraphicCalendar(int w, int h,SearchEMT parentSearch) {
		parent = parentSearch;
    	width = w;
    	height = h;
        setPreferredSize(new Dimension(width, height));
        addMouseListener(this);
        startDate = "2004-01-01";
        endDate = "2004-08-01";
        currentStartDate = startDate;
        seenDatesResults = new dateCollection();

    }

    static final Color verylightGray = new Color(229, 229, 229);

    public void paintComponent(Graphics g) {

    	//lets grab our current size
        Dimension dim = getSize();
        
        //
        //g.setColor(Color.white);
        //g.fillRect(0, 0, dim.width, dim.height);

        g.setColor(verylightGray);
        g.fillRect(0, 0, dim.width , dim.height );

        if (numberSeenDates == 0)
            return;

        // g.fillRect(0, 0, width * 4, height * 4);
        max_y_level = 1;
        // draw a "button"
        g.setColor(Color.blue);
        Font defaultFont = g.getFont();
        // lets figure out where we are starting from
        rightNow.setTime(Date.valueOf(currentStartDate));

        int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);
        // we would like to start drawing on sunday, so make sure the day of
        // week is 1
        if (dayOfWeek != 1) {
            // adjust so we are drawing on sunday first
            // System.out.println("Adj:"+rightNow.getTime());
            rightNow.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - 1));
            // System.out.println("new:"+rightNow.getTime());

            currentStartDate = rightNow.get(Calendar.YEAR) + "-" + (1 + rightNow.get(Calendar.MONTH)) + "-"
                    + rightNow.get(Calendar.DAY_OF_MONTH);
            dayOfWeek = 1;
        }

        g.setFont(bolder);
        g.drawString("Calendar " + rightNow.get(Calendar.YEAR), (dim.width / 2) - 35, 20);

        // need to add day labels

        g.drawString("Su", startx, 40);
        g.drawString("Su", right_side + startx, 40);
        g.drawString("Mo", startx + xoffset, 40);
        g.drawString("Mo", right_side + startx + xoffset, 40);
        g.drawString("Tu", startx + 2 * xoffset, 40);
        g.drawString("Tu", right_side + startx + 2 * xoffset, 40);
        g.drawString("We", startx + 3 * xoffset, 40);
        g.drawString("We", right_side + startx + 3 * xoffset, 40);
        g.drawString("Th", startx + 4 * xoffset, 40);
        g.drawString("Th", right_side + startx + 4 * xoffset, 40);
        g.drawString("Fr", startx + 5 * xoffset, 40);
        g.drawString("Fr", right_side + startx + 5 * xoffset, 40);
        g.drawString("Sa", startx + 6 * xoffset, 40);
        g.drawString("Sa", right_side + startx + 6 * xoffset, 40);
        g.setFont(defaultFont);
        // these will keep track of the box locations
        int currentx = startx;
        int currenty = starty;

        int currentyear = rightNow.get(Calendar.YEAR);
        // will get a scheme for coloring in the boxes. based on dates which
        // have results
        int[] colorScheme = seenDatesResults.getYear("" + currentyear);

        int currentMonth = rightNow.get(Calendar.MONTH); // we want from 0-11

        
        
        
        // calendar squares
        int day = rightNow.get(Calendar.DAY_OF_MONTH);

        
        if(inCalendarMode){
        
        // need to set first x location
        currentx += (xoffset * (dayOfWeek - 1));

        for (; dayOfWeek < 134; dayOfWeek++) {

            // maybe draw3 outline
            if (dayOfWeek == selected) {
                g.setColor(Color.RED);
                g.drawRect(currentx - 3, currenty - 3, box_width + 5, box_height +5);

                g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height +3);
                g.drawRect(currentx - 1,currenty - 1,box_width + 1,box_height + 1);

            }
            // need to see what color to color the box
            if (colorScheme[currentMonth * 31 + day - 1] == 0) {
                g.setColor(Color.darkGray);
            } else if (colorScheme[currentMonth * 31 + day - 1] > tons) {
                g.setColor(Color.magenta);
            } else if (colorScheme[currentMonth * 31 + day - 1] > many) {
                g.setColor(Color.red);
            } else {
                g.setColor(Color.PINK);
            }

            g.fillRect(currentx, currenty, box_width, box_height);
            // for font and or month name
            g.setColor(Color.yellow);

            if (day < 10) {
                g.drawString("" + day, currentx + 5, currenty + 13);

                if (day == 9) {
                    g.setColor(Color.black);
                    g.setFont(bolder);
                    g.drawString(monthNames[currentMonth], 1, currenty + 23);
                    if (currentMonth == 12) {
                        currentMonth = 0;
                    }
                    g.setFont(defaultFont);
                }
            } else {
                g.drawString("" + day, currentx + 2, currenty + 13);
            }

            if (dayOfWeek % 7 == 0) {
                currenty += yoffset;
                currentx = startx;
                max_y_level++;
            } else {
                currentx += xoffset;
            }

            day++;
            // lets see if we need to inc the month
            if (day > 28) {
                if (currentMonth == 1) {
                    if (currentyear % 4 != 0) {
                        day = 1;
                        currentMonth++;
                    } else {
                        if (day > 29) {
                            day = 1;
                            currentMonth++;
                        }
                    }
                } else if (day > 31
                        && (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6
                                || currentMonth == 7 || currentMonth == 9 || currentMonth == 11)) {
                    day = 1;
                    currentMonth++;
                } else if (day > 30
                        && (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10)) {
                    day = 1;
                    currentMonth++;
                }

            }// end if day high

            // need to see if need to incr the year
            if (currentMonth == 12) {
                currentyear++;
                colorScheme = seenDatesResults.getYear("" + currentyear);
                currentMonth = 0;
            }

        }
        max_y_level--;
        // now to draw the right side of the calendar

        currentx = startx + right_side;
        currenty = starty;
        currentx += (xoffset * ((dayOfWeek % 7) - 1));
        for (; dayOfWeek < 267; dayOfWeek++) {
//          maybe draw3 outline
            if (dayOfWeek == selected) {
                g.setColor(Color.RED);
                g.drawRect(currentx - 3, currenty - 3, box_width + 5, box_height +5);
                g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height +3);
                g.drawRect(currentx - 1,currenty - 1,box_width + 1,box_height + 1);

            }
            // need to see what color to color the box
            if (colorScheme[currentMonth * 31 + day - 1] == 0) {
                g.setColor(Color.darkGray);
            } else if (colorScheme[currentMonth * 31 + day - 1] > tons) {
                g.setColor(Color.magenta);
            } else if (colorScheme[currentMonth * 31 + day - 1] > many) {
                g.setColor(Color.red);
            } else {
                g.setColor(Color.PINK);
            }

            g.fillRect(currentx, currenty, box_width, box_height);

            g.setColor(Color.yellow);

            if (day < 10) {
                g.drawString("" + day, currentx + 5, currenty + 13);

                if (day == 9) {
                    g.setColor(Color.black);
                    g.setFont(bolder);
                    g.drawString(monthNames[currentMonth], 3 + right_side + (xoffset * 8), currenty + 23);
                    if (currentMonth == 12) {
                        currentMonth = 0;
                    }
                    g.setFont(defaultFont);
                }
            } else {
                g.drawString("" + day, currentx + 2, currenty + 13);
            }
            if (dayOfWeek % 7 == 0) {
                currenty += yoffset;
                currentx = startx + right_side;
            } else {
                currentx += xoffset;
            }

            day++;
            // lets see if we need to inc the month
            if (day > 28) {
                if (currentMonth == 1) {
                    if (currentyear % 4 != 0) {
                        day = 1;
                        currentMonth++;
                    } else {
                        if (day > 29) {
                            day = 1;
                            currentMonth++;
                        }
                    }
                } else if (day > 31
                        && (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6
                                || currentMonth == 7 || currentMonth == 9 || currentMonth == 11)) {
                    day = 1;
                    currentMonth++;
                } else if (day > 30
                        && (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10)) {
                    day = 1;
                    currentMonth++;
                }

            }// end if day high

            // need to see if need to incr the year
            if (currentMonth == 12) {
                currentyear++;
                colorScheme = seenDatesResults.getYear("" + currentyear);
                currentMonth = 0;
            }

        }// done right side

        // add navigation buttons:

        // starty + max_y_level*yoofset+10;
        g.setColor(Color.BLUE);
        back_x = startx + 10;
        back_y = starty + max_y_level * yoffset + 20;
        back_w = 140;
        back_h = 30;
        forward_x = right_side + startx + 10;
        forward_y = starty + max_y_level * yoffset + 20;
        forward_w = 140;
        forward_h = 30;

        g.fillRect(back_x, back_y, back_w, back_h);
        g.fillRect(forward_x, forward_y, forward_w, forward_h);
        g.setFont(bolder);
        g.setColor(Color.white);
        g.drawString("Back X Months", startx + 35, starty + max_y_level * yoffset + 40);
        g.drawString("Forward X Months", right_side + startx + 30, starty + max_y_level * yoffset + 40);
        }//end calendar mode
        else {
        	//we are in non calendar mode, i.e we are drawing stats
        	
        	//draw top gray bar
        	g.setColor(Color.gray);
              g.fillRect(1,starty-10,width+20,box_height+20);
              	g.setFont(bolder);
              
              	//need to draw current number and month
              	g.setColor(Color.yellow);
              	g.drawString(monthNames[rightNowselected.get(GregorianCalendar.MONTH)]+" "+rightNowselected.get(GregorianCalendar.DAY_OF_MONTH),4,starty+20);
              	
              	
        	//add the current month and day
        	
        	//TODO: infosheety stats here
        	
              	g.setColor(Color.LIGHT_GRAY);
            	g.fillRect(1,starty+10+box_height,height+200,width+50);
            	
            	
        	
            	//only want max of 10 top words
              	int max = infoSheet.size();
            	if(max>30){
            		max = 30;
            	}
            	g.setColor(Color.yellow);
            	g.setFont(bolder);
            	for(int i=0;i<max;i++){
            		
            		g.drawString(infoSheet.get(i),5,starty+45+(i*15));
            		
            		
            		
            	}
          
        	
        	
        }
        
        
        
        
        
        
    }

    public void clear() {
        seenDatesResults.clear();
        numberSeenDates = 0;
    }

    public void addDate(String curDate) {
        seenDatesResults.addDate(curDate);
        numberSeenDates++;
    }

    public void setDateRange(String startD, String endD) {
    	selected = -1;
    	startDate = new String(startD);
        endDate = new String(endD);
        currentStartDate = startDate;
    }

    public void itemStateChanged(ItemEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub
      //dont want to process during animation
    	if(inAnimationSequence){
        	return;
        }
    	
    	
    	Point clickpoint = arg0.getPoint();

        boolean doubleClick = false;
        if(arg0.getClickCount()==2){
        doubleClick = true;
        }
        
        
        if(inCalendarMode){
        if (numberSeenDates == 0)
            return;

        // lets see if we are on the forward or back dates

        // g.fillRect(back_x, back_y, back_w, back_h);
        // g.fillRect(forward_x, forward_y, forward_w, forward_h);
        // lets see if we need to move back
        if (clickpoint.x >= back_x && clickpoint.x <= back_x + back_w && clickpoint.y >= back_y
                && clickpoint.y <= back_y + back_h) {

            GregorianCalendar rightNow = new GregorianCalendar();
            rightNow.setTime(Date.valueOf(currentStartDate));
            // int moreday = rightNow.get(Calendar.DAY_OF_WEEK);
            rightNow.add(Calendar.DAY_OF_MONTH, -133);
            currentStartDate = rightNow.get(Calendar.YEAR) + "-" + (1 + rightNow.get(Calendar.MONTH)) + "-"
                    + rightNow.get(Calendar.DAY_OF_MONTH);
            paintComponent(this.getGraphics());
            return;
        } else if (clickpoint.x >= forward_x && clickpoint.x <= forward_x + forward_w && clickpoint.y >= forward_y
                && clickpoint.y <= forward_y + forward_h) {

            GregorianCalendar rightNow = new GregorianCalendar();
            rightNow.setTime(Date.valueOf(currentStartDate));
            // int moreday = rightNow.get(Calendar.DAY_OF_WEEK);
            rightNow.add(Calendar.DAY_OF_MONTH, 133);

            currentStartDate = rightNow.get(Calendar.YEAR) + "-" + (1 + rightNow.get(Calendar.MONTH)) + "-"
                    + rightNow.get(Calendar.DAY_OF_MONTH);
            paintComponent(this.getGraphics());
            return;
        }

        //so we've clikced within the area
        
        
        // System.out.println("mc "+clickpoint.x +" "+ clickpoint.y);

        // need to decide if we have clicked on a square:
        boolean y_ok = false;
        boolean x_ok = false;
        boolean isRightSide = false;
        int yp = clickpoint.y - starty;
        int xp;
        int y_level = 0;
        int x_level = 0;
        // first lets check the y level
        while (yp >= 0) {
            y_level++;
            if (yp >= 0 && yp <= box_height) {
                y_ok = true;

                break;
            }
            // else chop off to next level
            yp -= (yoffset);

        }
        // System.out.println(yp +" "+ y_level +" " + y_ok);
        // to see if we are not clicking on a y location possibility
        if (y_level > max_y_level || !y_ok) {
            return;
        }

        // now to locate the x coordinate
        // first we need to see if we are the right or left side
        if (clickpoint.x > right_side) {
            xp = clickpoint.x - startx - right_side;
            isRightSide = true;
        } else {
            xp = clickpoint.x - startx;
        }

        while (xp >= 0) {
            x_level++;
            if (xp >= 0 && xp <= box_width) {
                x_ok = true;

                break;
            }
            // else chop off to next level
            xp -= (xoffset);

        }

        // System.out.println(xp +" "+ x_level +" " + x_ok);
        if (x_level > 7 || !x_ok) {
            return;
        }

        // System.out.println(y_level + " y is ok");
        // System.out.println(x_level + " x is ok");
        x_level--;
        y_level--;
        int currentx, currenty;
        Graphics g = this.getGraphics();
        
        int location = 7 * y_level + x_level;
        
        //since selected if offset by 1
        selected--;
        
        if (isRightSide) {
            location += 133;

           // need to see if to gray out old
            if (selected > -1) {
            	// draw a rectangle around it
                // first we need to gray out old one
                // lets figure out where we need to draw
               

                if (selected > 133) {
                    currentx = startx + ((selected-133 )% 7) * xoffset;
                    currenty = starty + ((selected-133 )/ 7) * yoffset;
                    currentx += right_side;
                }
                else{
                    currentx = startx + (selected % 7) * xoffset;
                    currenty = starty + (selected / 7) * yoffset;
                }
                g.setColor(verylightGray);
                //System.out.println(currentx + " "+ currenty);
                g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height + 3);
                g.drawRect(currentx - 1, currenty - 1, box_width + 1, box_height + 1);
            
            }// now to draw the new outlines of what we;ve just clikced on
            currentx = right_side + startx + x_level * xoffset;
            currenty = starty + y_level * yoffset;
            
            //System.out.println(currentx + " "+ currenty);
         if(!doubleClick)
            drawBlinkingSquares(g,currentx,currenty);
            
//            g.setColor(Color.blue);
  //          g.drawRect( currentx - 2, currenty - 2, box_width + 3, box_height + 3);
    //        g.drawRect(currentx - 1, currenty - 1, box_width + 1, box_height + 1);

        }
        else{
//          need to see if to gray out old
            if (selected > 0) {
                // draw a rectangle around it
                // first we need to gray out old one
                // lets figure out where we need to draw
                if (selected > 133) {
                    currentx = startx + ((selected-133 )% 7) * xoffset;//subtract 133 to componsate right side
                    currenty = starty + ((selected-133 )/ 7) * yoffset;
                    currentx += right_side;
                }
                else{
                    currentx = startx + (selected % 7) * xoffset;
                    currenty = starty + (selected / 7) * yoffset;
                }
                g.setColor(verylightGray);
                g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height + 3);
                g.drawRect(currentx - 1, currenty - 1, box_width + 1, box_height + 1);
            }// now to draw the new outlines of what we;ve just clikced on
            currentx = startx + x_level * xoffset;
            currenty = starty + y_level * yoffset;
            
            if(!doubleClick)
            drawBlinkingSquares(g,currentx,currenty);
            
            //      g.setColor(Color.blue);
        //    g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height + 3);
          //  g.drawRect(currentx - 1, currenty - 1, box_width + 1, box_height + 1);
        }
        selected++;
        
        // System.out.println("location:" + location);
        selected = location +1;

        // draw the square around the selected one.

        rightNowselected.setTime(Date.valueOf(currentStartDate));
        // int moreday = rightNow.get(Calendar.DAY_OF_WEEK);
        rightNowselected.add(Calendar.DAY_OF_MONTH, location);
        System.out.println("which is " + rightNowselected.getTime());

        //ok if we have double clicked : lets do something cool!
        if(!doubleClick)
        	return;
        
        //start animation sequence
        int numberPrint = rightNowselected.get(GregorianCalendar.DAY_OF_MONTH);
        bubbleUp(g,startx, starty, currentx,currenty,numberPrint);
        
        animateTop(g,numberPrint,rightNowselected.get(GregorianCalendar.MONTH));
        
        
        
        //add pause message
        inCalendarMode = false;
        
        
        }
        else
        {//so we are not in calendar mode, need to define click area affect
        	if(clickpoint.x >startx && clickpoint.y < starty-10){
        		inCalendarMode = true;
        		repaint();
        		return;
        	}
        }
        
        
        
    }

   /**
    * Will animate the selection bubbling upwards
    * @param g
    * @param topx
    * @param topy
    * @param cx
    * @param cy
    * @param now
    */
    private void bubbleUp(Graphics g,int topx, int topy, int cx, int cy, int numberPrint){
    	inAnimationSequence = true;
    	//lets create a buffered image
    	BufferedImage bufimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
    	Graphics drawing = bufimage.getGraphics();
    	
    	
    	
    	Font defaultFont = g.getFont();
    	int currentx = cx-5;
    	int currenty = cy-5;
    	
    	//check to make sure we havent crossd the boundary
    	if(currentx < topx){
    		currentx = topx;
    	}
    	if(currenty < topy){
    		currenty = topy;
    	}
    	
    	
    	
    	while (currenty > topy){
    		
    		paintComponent(drawing);
    		
    		drawCalendarBoxHelper(drawing,currentx,currenty,numberPrint,defaultFont);
    		
    		currenty-=10;
    		
    		//now to update the canvas
    		g.drawImage(bufimage,0,0,this);
    		
    		try{
    		Thread.sleep(20);
    		}catch(InterruptedException e){}
    	}
    	//now to make sure its on top:
    	currenty = topy;
    	paintComponent(drawing);
    	drawCalendarBoxHelper(drawing,currentx,currenty,numberPrint,defaultFont);
		//now to update the canvas
		g.drawImage(bufimage,0,0,this);
    	
    	//now lets bubble sideways
    	
		while (currentx > 1){
    		
    		paintComponent(drawing);
    		
    		drawCalendarBoxHelper(drawing,currentx,currenty,numberPrint,defaultFont);
    		
    		currentx-=5;
    		
    		//now to update the canvas
    		g.drawImage(bufimage,0,0,this);
    		
    		try{
    		Thread.sleep(20);
    		}catch(InterruptedException e){}
    	}
//		now to make sure its on top:
    	currentx = 1;
    	paintComponent(drawing);
    	drawCalendarBoxHelper(drawing,currentx,currenty,numberPrint,defaultFont);
		//now to update the canvas
		g.drawImage(bufimage,0,0,this);
    	
		inAnimationSequence = false;
    	
    }
    
    
    /**
     * Helper method to draw larger box to bubble upwards
     * @param drawing
     * @param currentx
     * @param currenty
     * @param numberPrint
     * @param defaultFont
     */
    private void drawCalendarBoxHelper(Graphics drawing,int currentx, int currenty , int numberPrint, Font defaultFont){
    	drawing.setColor(Color.gray);
		drawing.fillRect(currentx, currenty, box_width+5, box_height+5);
        // for font and or month name
		drawing.setColor(Color.yellow);
		drawing.setFont(defaultFont);
        if (numberPrint < 10) {
        	drawing.drawString("" + numberPrint, currentx + 8, currenty + 15);
        } else {
        	drawing.drawString("" + numberPrint, currentx + 4, currenty + 15);
        }
    }
    
    
    
    
    /**
     * will animate the sequence between the selection and keyword presentation
     * @param g
     * @param now
     */
    private void animateTop(Graphics g,int number,int month){
    	inAnimationSequence = true;
    	
    	
    	//lets setup the buffer image
    	BufferedImage bufimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
    	Graphics drawing = bufimage.getGraphics();
    	paintComponent(drawing);
    	
        
        try{
        
        for(int i=box_width;i<width+10;i+=20){
        	drawing.setColor(Color.gray);
        	drawing.fillRect(1,starty-10,i,box_height+20);
        	
        	//need to draw current number and month
        	drawing.setFont(bolder);
        	drawing.setColor(Color.yellow);
        	drawing.drawString(monthNames[month]+" "+number,4,starty+20);
        	//draw on the canvas
        	g.drawImage(bufimage,0,0,this);
        	//get some well deserved rest :)
        	Thread.sleep(30);
       }
        //now to gray out the bottom
        drawing.setColor(Color.LIGHT_GRAY);
    	drawing.fillRect(1,starty+10+box_height,height+200,width+50);
    	
    	//need to draw current number and month
    	drawing.setFont(bolder);
    	drawing.setColor(Color.yellow);
    	drawing.drawString(monthNames[month]+" "+number,4,starty+20);
    	//draw on the canvas
    	g.drawImage(bufimage,0,0,this);
    
    	infoSheet = parent.getStats(rightNowselected,3);
        
//    	only want max of 10 top words
    	int max = infoSheet.size();
    	if(max>30){
    		max = 30;
    	}
    	g.setFont(bolder);
    	g.setColor(Color.yellow);
    	for(int i=0;i<max;i++){
    		
    		g.drawString(infoSheet.get(i),5,starty+45+(i*15));
    		
    		
    		
    	}
    	
    	
        }catch(InterruptedException e){
        	e.printStackTrace();
        }
        
        inAnimationSequence = false;
    }
    
    
    
    /**
     * will draw a blinking box around the selected date
     * @param g
     * @param currentx
     * @param currenty
     */
    private void drawBlinkingSquares(Graphics g,int currentx,int currenty){
   
    	
    	for(int i=0;i<6;i++){
    	if(i%2==0){
    	g.setColor(Color.yellow);
    	}
    	else{
    		g.setColor(Color.RED);
    	}
    	
    	//TODO: check this out:
        g.drawRect(currentx - 3, currenty - 3, box_width + 5, box_height + 5);
    	g.drawRect(currentx - 2, currenty - 2, box_width + 3, box_height + 3);
        g.drawRect(currentx - 1, currenty - 1, box_width + 1, box_height + 1);
     try{
     	Thread.sleep(30);
     }catch(InterruptedException e){}
    	}
    }
    
   
    
    
    
    
    
    
    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub
        // Point clickpoint = arg0.getPoint();

        // System.out.println("mp "+clickpoint.x +" "+ clickpoint.y);

    }

    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

}
