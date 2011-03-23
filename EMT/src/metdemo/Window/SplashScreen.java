package metdemo.Window;



import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import com.sun.java.swing.plaf.windows.resources.windows;

// TODO: Auto-generated Javadoc
/**
 * Animated SplashScreen displays logo, version and changing animation.
 *
 * @author Shlomo Hershkop
 */


public final class SplashScreen extends JWindow implements WindowListener{

	/** The subscreen. */
	private SplashScreenAnimation subscreen;
	
	/** The progress report. */
	private JProgressBar progressReport;
	
	/** The progress title. */
	private JLabel progressTitle;


	/**
	 * Instantiates a new splash screen.
	 */
	public SplashScreen (){

		//set the progress report to unlimited
		progressReport = new JProgressBar();
		progressReport.setIndeterminate(true);

		progressTitle = new JLabel("Processing...");

		JPanel progressPanel = new JPanel();
		progressPanel.add(progressTitle);
		progressPanel.add(progressReport);
		progressPanel.setBackground(Color.orange);
		setLayout(new BorderLayout());
		subscreen = new SplashScreenAnimation(this);
		add(BorderLayout.CENTER,subscreen);
		add(BorderLayout.SOUTH,progressPanel);
		pack();

		final Dimension screenDim = getToolkit().getScreenSize();

		/* Center the window */
		final Rectangle winDim = getBounds();
	 //  System.out.println("\nwindim" + winDim);
		setLocation((screenDim.width - winDim.width) / 2,
				(screenDim.height - winDim.height) / 2);
		
	//	System.out.println("now: " + getBounds());
		addWindowListener(this);
		/*addWindowListener(new WindowListener() {
			
		});*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		subscreen.hardRefresh(); 
		subscreen.repaint();
		subscreen.validate();
	}

	

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
		subscreen.hardRefresh(); 
		subscreen.repaint();
		subscreen.validate();
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
		subscreen.hardRefresh(); 
		subscreen.repaint();
		subscreen.validate();
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose(){
		subscreen.dispose();
		
	}


	/**
	 * Sets the mSG.
	 *
	 * @param s the new mSG
	 */
	public void setMSG(String s){
		progressTitle.setText(s);
	}


	/**
	 * Progress.
	 *
	 * @param count the count
	 * @param max the max
	 */
	public void progress(int count,int max){
		progressReport.setMaximum(max);
		progressReport.setValue(count);
		progressReport.setIndeterminate(false);

	}


}

class SplashScreenAnimation extends Canvas
{

	private static final int EMTSC_WIDTH = 415;
	private static final int EMTSC_HEIGHT = 400;
	final static BasicStroke wideStroke = new BasicStroke(8.0f);
	final static BasicStroke regularStroke = new BasicStroke(4.0f);
	final static long sleepAmount = 500;//every second animate was 500
	private float alphas[] = new float[40];//random grid
	private float incr[] = new float[40];//which way to do the randomness
	private int alerts[] = new int[40];
	final static float diff = 0.05f;
	private Font font = new Font("serif", Font.PLAIN, 45);
	static final GradientPaint bg2 = new GradientPaint(0,0,Color.black,EMTSC_HEIGHT, EMTSC_WIDTH/2,Color.blue);
	private BufferedImage bufImage;
	private BufferedImage backup_bufImage;
	private Graphics2D g2;
	private AlphaComposite ac;
	private boolean working = true;
	//private final Image splashImage;
	final Font fontsmall;// = new Font("serif", Font.BOLD+Font.ITALIC , 16);
	final Font fontverysmall;// = new Font("serif", Font.BOLD , 12);
	final Font fontsmall2;// = new Font("serif", Font.BOLD+Font.ITALIC , 16);
	FontRenderContext frc;
	final RoundRectangle2D.Double R2;
	final TextLayout layout_spam;
	final TextLayout layout_found;// = new TextLayout("Found", fontverysmall, frc);
	final TextLayout layout_virus;// = new TextLayout("VIRUS", fontverysmall, frc);
	final TextLayout layout_emtversion;// = new TextLayout("EMT Version 3.9.1", font, frc);
	final TextLayout layout_emt;// = new TextLayout("Email Mining Toolkit", fontsmall, frc);
	final TextLayout layout_copyright;// = new TextLayout("Copyright 2001-2007 Columbia University", fontsmall2, frc);
	final TextLayout layout_idslab;// = new TextLayout("Intrusion Detection Lab", fontsmall2, frc);
	private Robot robbyRobot;	
private  Rectangle rect2;
private Component parenthandle;
//private boolean counter =true;
	/**
	 * Construct and display the SplashScreen.
	 *
	 */
	public SplashScreenAnimation(Component parentComp)//final URL splashUrl) 
	{
		
		parenthandle = parentComp;
		/*
		 *     /*
		 * // use ImageIcon, so we don't need to use MediaTracker
    Image image = new ImageIcon(imageFile).getImage();
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    if (imageWidth > 0 && imageHeight > 0) {
      
      // a Rectangle centered on screen
      rect = new Rectangle((screenWidth - imageWidth) / 2, (screenHeight - imageHeight) / 2,
          imageWidth, imageHeight);
      // the critical lines, create a screen shot
      try {
        bufImage = new Robot().createScreenCapture(rect);
      } catch (AWTException e) {
        e.printStackTrace();
      }
      // obtain the graphics context from the BufferedImage
      Graphics2D g2D = bufImage.createGraphics();
      // Draw the image over the screen shot
      g2D.drawImage(image, 0, 0, this);
      // draw the modified BufferedImage back into the same space
      setBounds(rect);
		 */
//grab screen shot
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
	    int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		//System.out.println(screenWidth + " " + screenHeight);
		/*Rectangle winDim = this.getBounds();
	    System.out.println("\nwindim" + winDim.getWidth() + " "+ winDim.getHeight());
	 */ //  Point p = getLocation();
	      rect2 = new Rectangle((screenWidth-EMTSC_WIDTH)/2,(screenHeight-EMTSC_HEIGHT)/2-13,EMTSC_WIDTH,EMTSC_HEIGHT+26);
	    	  //new Rectangle((screenWidth - EMTSC_WIDTH) / 2, 299 ,
	    		 //EMTSC_WIDTH, 426);
	    // System.out.println("Rect2: " + rect2);
	          // the critical lines, create a screen shot
	          try {
	        	  robbyRobot = new Robot();
	           backup_bufImage = robbyRobot.createScreenCapture(rect2);
	          } catch (AWTException e) {
	            e.printStackTrace();
	            backup_bufImage = new BufferedImage(EMTSC_WIDTH, EMTSC_HEIGHT, BufferedImage.TYPE_INT_RGB);
	          }

	    //lets get a backup of the screen capture
	         
	       /*   bufImage = new BufferedImage(backup_bufImage.getWidth(), backup_bufImage.getHeight(), backup_bufImage.getType());
	          Graphics gbackup = bufImage.createGraphics();
	          gbackup.drawImage(backup_bufImage, 0, 0, null);
*/
       bufImage = new BufferedImage(EMTSC_WIDTH, EMTSC_HEIGHT, BufferedImage.TYPE_INT_RGB);

	   
		
		g2 = bufImage.createGraphics();
	
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
		g2.drawImage(backup_bufImage, 0, 0, null);	
		frc  = g2.getFontRenderContext();

		fontsmall = new Font("serif", Font.BOLD+Font.ITALIC , 16);
		fontverysmall = new Font("serif", Font.BOLD , 12);
		fontsmall2 = new Font("serif", Font.BOLD+Font.ITALIC , 16);

		layout_spam = new TextLayout("SPAM", fontverysmall, frc);

		layout_found = new TextLayout("Found", fontverysmall, frc);
		layout_virus = new TextLayout("VIRUS", fontverysmall, frc);
		layout_emtversion = new TextLayout("EMT Ver 3.9.1", font, frc);
		layout_emt = new TextLayout("Email Mining Toolkit", fontsmall, frc);
		layout_copyright = new TextLayout("Copyright 2001-2007 Columbia University", fontsmall2, frc);
		layout_idslab = new TextLayout("Intrusion Detection Lab", fontsmall2, frc);
		R2 = new RoundRectangle2D.Double(0, 0, 45, 45, 15, 15);



		//g2.setBackground(Color.lightGray);
		g2.setStroke(regularStroke);//for outlines
		//set up the initial fade value for the squares.
		for(int i=0;i<alphas.length;i++)
		{ alphas[i] = (float)Math.random();

		if(alphas[i]>=0.5f)
			incr[i] = diff;
		else
			incr[i] = -diff;

		if(Math.random()>=.90)
		{alerts[i] =0;
		}else
		{alerts[i] = -1;
		}

		}


		//show waiting. 
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		/* Grab the height and width of the windows from the image */
		setSize(EMTSC_WIDTH,EMTSC_HEIGHT);

		//splashImage.getWidth(this), splashImage.getHeight(this));

		(new Thread(new Runnable()
		{
			public void run()
			{

				try{
					while(working){
						
						repaint();
						Thread.sleep(sleepAmount);
					}
					//System.out.println("splash screen closing");
				}catch(InterruptedException y){System.out.println("splashscreen interrupt is " +y);}
			}
		})).start();
	}

	//fixs starting empty
	public void paint(Graphics g){
		update(g);
	}

public void hardRefresh(){
	System.out.println("hard refresh");
	Dimension psize = parenthandle.getSize();
	Point p = parenthandle.getLocation();
    
//	System.out.println(p + " " + psize);
	
	/*parenthandle.setSize(0,0);
    Point p = parenthandle.getLocation();
    rect2 = new Rectangle(p.x,p.y,
  		 EMTSC_WIDTH, 426);
    
    System.out.println(rect2);
    backup_bufImage = robbyRobot.createScreenCapture(rect2);
   parenthandle.setSize(psize);*/
    
}
	/**
	 * Overidden to display the image.
	 *
	 * @param g to draw on the buffered image
	 */
	public final void update(final Graphics g) 
	{	
	//ignore the next mess in middle of trying to get moving windows behind us to show up correctly
//		g2 = bufImage.createGraphics();
//		g2.setBackground(Color.lightGray);
//		g2.setStroke(regularStroke);//for outlines
//		
//	   //lets get a backup of the screen capture
//  
//	       backup_bufImage = new BufferedImage(bufImage.getWidth(), bufImage.getHeight(), bufImage.getType());
//	          Graphics gbackup = backup_bufImage.getGraphics();
//	          gbackup.drawImage(bufImage, 0, 0, null);
//
//		
		//need to shrink to nothing, capture and then make big again
	/*	if(counter)
		{
			counter = false;
			Dimension psize = parenthandle.getSize();
		      parenthandle.setSize(0,0);
		      backup_bufImage = robbyRobot.createScreenCapture(rect2);
		      parenthandle.setSize(psize);
		}else{
		//counter = true;
		}*/
		
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2.setComposite(ac);
		
	//	Dimension psize = parenthandle.getSize();
	//parenthandle.setSize(0,0);
	//parenthandle.setVisible(false);
	//	parenthandle.setSize(psize);
		
		
		//parenthandle.setVisible(false);
		
		//backup_bufImage = robbyRobot.createScreenCapture(rect2);
		//parenthandle.setVisible(true); 
		g2.drawImage(backup_bufImage, 0, 0, null);
		

		g2.setPaint(Color.orange);
		int nw = (EMTSC_HEIGHT+20) /2;
		g2.fill(new RoundRectangle2D.Double(40, nw+50, EMTSC_WIDTH-80,75,20,20));
		g2.fill(new RoundRectangle2D.Double(60, nw+100, EMTSC_WIDTH-120,100,20,20));
		
		g2.fill(new RoundRectangle2D.Double(0, nw+130, EMTSC_WIDTH,100,20,20));
		
	//clearRect(0, 0, EMTSC_WIDTH, (EMTSC_HEIGHT+20)/2);
		g2.setPaint(Color.black);


		layout_emtversion.draw(g2, 70f,300f);//(float)loc.getX(), (float)loc.getY());
		g2.setPaint(Color.red);

		layout_emt.draw(g2, 140f,325f);//(float)loc.getX()+100, (float)loc.getY()+25);
		g2.setPaint(Color.black);
		layout_copyright.draw(g2, 65f,350f);//(float)loc.getX()+35, (float)loc.getY()+50);
		layout_idslab.draw(g2, 120f,370f);//(float)loc.getX()+75, (float)loc.getY()+70);

		//draw bunch of round rectangles. on upper part. 
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		int num =0;
		for(int x=10,i=0;x<370;x+=50,i++)
		{	
			for(int y=10;y<200;y+=50,i++)
			{
				num++;
				
				if(num%8 == 0 || num%9 ==0 || num % 15 ==0){
					continue;
				}
				
				alphas[i] +=incr[i];
				if(alphas[i] > 1.0)
				{
					incr[i] = -diff;
					alphas[i]+=incr[i];
				}
				else if(alphas[i] <0)
				{
					incr[i] = diff;
					alphas[i]+=incr[i];
				}

				if(alerts[i] == 0)
					g2.setPaint(Color.red);
				else
					g2.setPaint(bg2);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alphas[i]));
				R2.x = x;
				R2.y=y;
				g2.fill(R2);

				//alert shades:
				if(alerts[i] == 0 && alphas[i] > .60 )
				{
					int mv2 = (int)((alphas[i]-.5f)/.05f );

					if(mv2%2==0)
						g2.setPaint(Color.black);
					else
						g2.setPaint(Color.white);
					if(i%2==0)
					{

						layout_spam.draw(g2, x+6, y+22);

						layout_found.draw(g2, x+7, y+36);
					}
					else
					{
						layout_virus.draw(g2, x+5, y+22);
						layout_found.draw(g2, x+6, y+36);
					}
					g2.setPaint(Color.red);


					if(mv2%2==1)
						for(int mv=6;mv>0;mv--)
						{
							g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f-mv*.05f));
							g2.draw(new RoundRectangle2D.Double(x-mv, y-mv, 45+mv+mv, 45+mv+mv, 20, 20));
						}
				}

			}
		}
		//draw alerts.
//		this draws the buffer onto the screen
		g.drawImage(bufImage,0,0,this);
		

		//restore
	//	parenthandle.setSize(psize);
	
		
	}//end paint



	public void setWorking(boolean new_work){
		working = new_work;
	}


	/**
	 * dispose the window. can do cleanup here
	 */
	public final void dispose()
	{
		working = false; //close thread
	}

}
