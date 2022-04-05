package PingDisplay;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Window extends Canvas implements MouseMotionListener, MouseListener
{
	public static final int HEIGHT = 50 + 15, WIDTH = 100; //Dimensions of the whole window including the text area
	public static final int GRAPH_HEIGHT = 50, GRAPH_WIDTH = 100; //Dimensions of the graph
	public static final int FPS = 30;
	public static int SCREEN_HEIGHT, SCREEN_WIDTH;
	
	public static ArrayList<Long> pingsX = new ArrayList<Long>();
	public static ArrayList<Integer> pingsY = new ArrayList<Integer>();
	public static long start;
	
	public static volatile boolean hovering;
	public static volatile boolean mouseInside;
	public static volatile int mouseX, mouseY;
	
	public static double actualCamY;
	public static double targetCamY;
	
	public static BufferedImage close, closeHovered;
	public static BufferedReader in;
	public static Process process;
	
	JFrame frame;
	
	public Window()
	{
		Dimension size = new Dimension(WIDTH, HEIGHT);
		setPreferredSize(size);
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public static void main(String[] args)
	{
		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		SCREEN_WIDTH = res.width;
		SCREEN_HEIGHT = res.height;
		
		Window main = new Window();
		
		main.frame = new JFrame();
		main.frame.add(main);
		main.frame.setSize(WIDTH, HEIGHT);
		main.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.frame.setResizable(false);
		main.frame.setAlwaysOnTop(true);
		main.frame.setUndecorated(true);
		main.frame.setOpacity(0.7f);
		main.frame.getContentPane().setBackground(new Color(1.0f, 0.0f, 0.0f, 0.5f));
		main.frame.setTitle("Ping Display");
		try
		{
			main.frame.setIconImage(ImageIO.read(new File("res/icon.png")));
		}
		catch (IOException e3)
		{
			e3.printStackTrace();
		}
		main.frame.pack();
		main.frame.setVisible(true);
		
		try
		{
			close = ImageIO.read(new File("res/close.png"));
			closeHovered = ImageIO.read(new File("res/closeHovered.png"));
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		
		long lastTime = System.currentTimeMillis();
		start = System.currentTimeMillis();
		while (true)
		{
			//Limiting the fps to 60
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastTime > 1000 / FPS) //If it's been 1000/60 ms, that is, if it's been the amount of time taken up by one frame, then display another frame
			{
				try
				{
					if (in.ready())
					{
						String line = in.readLine(); //Reads all remaining bytes from input stream, so only the new lines will be read
						String word = "";
						int i = 0;
						while (i != line.length())
						{
							if (line.charAt(i) == ' ') //If character is space, interpret the current word, and then start making a new word
							{
								/*//We need to make sure length is greater than 6 atleast, otherwise we can include numbers from statements like "with 32 bytes of data"
								if (word.length() >= 6 && word.charAt(0) >= '0' && word.charAt(0) <= '9')
								{
									String IP = word;
									System.out.print(IP + " : ");
								}*/
								if (word.length() >= 5 && word.substring(0, 5).compareTo("time=") == 0) //Also NOTE: We need to check if word has "time=" or not, otherwise we might also include the word "timed"
								{
									//NOTE: We need to first get rid of "ms" from the string and then the "time", because otherwise getting rid of "time=" will change the length of the string, and word.length()-2 will actually be out of bounds
									int ping = (int)Float.parseFloat(word.substring(0, word.length() - 2).substring(5)); //Get rid of the "time=" and "ms" from the string and parse the integer to get the ping
									pingsX.add(System.currentTimeMillis() - start);
									pingsY.add(ping);
									if (pingsX.size() > 10)
									{
										pingsX.remove(0);
										pingsY.remove(0);
									}
								}
								//This means request timed out/General Failure/Destination Net Unreachable. Basically, if this happens, it basically means we lost a packet, so we can set ping to -1 so displayer knows that we dropped a packet here
								else if (word.compareTo("Request") == 0 || word.compareTo("General") == 0 || word.compareTo("Destination") == 0)
								{
									pingsX.add(System.currentTimeMillis() - start);
									pingsY.add(-1);
								}
								word = "";
							}
							else //If character is not space, add to the previous word
							{
								word += line.charAt(i);
							}
							i++;
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				/* For some reason, if we set background to any opacity less than 1, the frame stops getting repainted. We can see 
				 * this if we ALT+TAB to desktop and then ALT+TAB to open the java window again. Putting this frame repaint here
				 * makes it repaint and actually work again. If you don't understand what I'm saying, just remove this frame repaint,
				 * and try the above ALT+TABing
				 * 
				 * idk why I was using main.frame.repaint(), but on linux it doesn't work. You need to do main.repaint() on linux
				 */
				//main.frame.repaint();
				main.repaint();
				lastTime = System.currentTimeMillis();
			}
			else
			{
				try
				{
					//Wait until it's time to render the next frame
					Thread.sleep(1000 / FPS - (currentTime - lastTime));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	public void paint(Graphics og)
	{
		BufferedImage bufferImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bufferImg.getGraphics();
		//System.out.println("Rnedered");
		
		g2.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
		g2.fillRect(0, 0, WIDTH, HEIGHT);
		
		//Decorate the window by adding frames, which also separate graph and text area
		g2.setColor(new Color(0, 0, 0, 255));
		g2.drawRect(0, 0, WIDTH - 1, HEIGHT - 1); //We need to do -1 because otherwise the rectangle goes outside the window
		g2.drawRect(0, 0, GRAPH_WIDTH - 1, GRAPH_HEIGHT - 1);
		
		//Drawing the expected amount of ping (60ms) as a light blue line
		g2.setColor(new Color(0, 0, 255, 255));
		if (GRAPH_HEIGHT - 10 + (int) actualCamY <= GRAPH_HEIGHT)
			g2.drawLine(0, GRAPH_HEIGHT - 10 + (int) actualCamY, GRAPH_WIDTH, GRAPH_HEIGHT - 10 + (int) actualCamY); //Since expected ping is 60 ms, and bottom line is 50ms, y coordinate will be 50 - 10
			
		//Drawing the bad amount of ping (90 ms) as a yellow line
		g2.setColor(new Color(255, 255, 0, 255));
		if (GRAPH_HEIGHT - (90 - 50) + (int) actualCamY <= GRAPH_HEIGHT)
			g2.drawLine(0, GRAPH_HEIGHT - (90 - 50) + (int) actualCamY, GRAPH_WIDTH, GRAPH_HEIGHT - (90 - 50) + (int) actualCamY); //Since expected ping is 90 ms, and bottom line is 50ms, y coordinate will be GRAPH_HEIGHT - (90-50)
			
		//Drawing the graph
		long xPos = System.currentTimeMillis() - start; //Location of camera wrt start
		for (int i = 1; i < pingsX.size(); i++)
		{
			/* Below, we have assumed that bottom line is 50 ms. Thus, we get the formula (GRAPH_HEIGHT - (pings.get(i).y-50)) for the height
			 * of a recorded ping number. xPos represents location of the camera, so, when we do (xPos - pings.get(i).x)/60, we
			 * find out how far back the current ping point is. We divide by 60 to reduce the distance so it fits on the graph.
			 * Also, we do GRAPH_WIDTH - (xPos - pings.get(i).x)/60
			 * 
			 */
			if (pingsY.get(i - 1) != -1 && pingsY.get(i) != -1)
			{
				g2.setColor(new Color(0, 255, 0, 255));
				double x1 = (GRAPH_WIDTH - (xPos - pingsX.get(i - 1)) / 60);
				double y1 = (GRAPH_HEIGHT - (pingsY.get(i - 1) - 50) + (int) actualCamY);
				double x2 = (GRAPH_WIDTH - (xPos - pingsX.get(i)) / 60);
				double y2 = (GRAPH_HEIGHT - (pingsY.get(i) - 50) + (int) actualCamY);
				g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
			}
		}
		
		//Drawing a red line on any ping packet that was lost. We can't do this on the previous loop because we only go from i = 1 to the end
		for (int i = 0; i < pingsX.size(); i++)
		{
			if (pingsY.get(i) == -1)
			{
				g2.setColor(new Color(255, 0, 0, 255)); //Red line if any packet gets dropped
				g2.drawLine((int) (GRAPH_WIDTH - (xPos - pingsX.get(i)) / 60), GRAPH_HEIGHT, (int) (GRAPH_WIDTH - (xPos - pingsX.get(i)) / 60), 0);
			}
		}
		
		if (pingsY.size() > 0)
		{
			int ping = pingsY.get(pingsY.size() - 1);
			int xPixelsPerLetter = 7, yPixelsPerLetter = 9; //Will be used for centering the string
			if (ping > 0)
			{
				int shade = (int) (255 * (1 - 50.0 / ping)); //Amount of shade of red. If ping is high, this should be close to 255 * 2
				shade = (shade < 0) ? 0 : shade; //If ping goes less than 60, shade will become negative. In this case, we can just set shade to 0 so color becomes green for all ping below 60 
				/* Green color should remain 255 until red color becomes 255, at which point color will look yellow.
				 * After red becomes 255, green should start decreasing and become 0, at which point color will be red
				 */
				g2.setColor(new Color((shade < 255 / 2) ? (2 * shade) : 255, (shade > 255 / 2) ? (255 - 2 * (shade - 255 / 2)) : 255, 0, 255));
				String s = ping + "ms";
				/* We use s.length() to centre the string along the x axis. Assuming each letter is xPixelsPerLetter pixels long, to centre the string, we will
				 * need to subtract (s.length() * 4) / 2 to centre the string.
				 * We took a screenshot and we found out that every letter takes 7 pixels in the x-axis.
				 * We took another screenshot and also found out that every letter takes 9 pixels in the y-axis.
				 */
				
				g2.drawString(s, GRAPH_WIDTH / 2 - (s.length() * xPixelsPerLetter) / 2, GRAPH_HEIGHT + (HEIGHT - GRAPH_HEIGHT) / 2 + yPixelsPerLetter / 2);
			}
			else
			{
				String s = "Lost";
				g2.setColor(new Color(255, 0, 0, 255));
				g2.drawString(s, GRAPH_WIDTH / 2 - (s.length() * xPixelsPerLetter) / 2, GRAPH_HEIGHT + (HEIGHT - GRAPH_HEIGHT) / 2 + yPixelsPerLetter / 2); //We use s.length() to centre the string along the x axis
			}
			
			//If the last ping is above/below camera's field of view, move the camera
			if (pingsY.get(pingsY.size() - 1) != -1)
			{
				double y = (GRAPH_HEIGHT - (pingsY.get(pingsY.size() - 1) - 50)) + targetCamY;
				int delta = 15; //Amount of difference that should exist between ping and upper/lower boundary of graph
				if (y < delta)
				{
					//We do y-delta instead of just y beaue we don't want ping line to be just barely visible. Rather, we want it to be completely in sight
					targetCamY += -(y - delta); //If targetCamY is 2, it means cam is 2 units in the upward direction (opposite that of java coordinates). So, if y is -2, for eg, then we need camera to be 2 units upwards, so we add -y to cam
				}
				else if (y > GRAPH_HEIGHT - delta)
				{
					//We do GRAPH_HEIGHT + delta instead of just GRAPH_HEIGHT because we don't want ping line to be just barely visible. Rather, we want it to be completely in sight
					targetCamY -= y - (GRAPH_HEIGHT - delta);
				}
			}
		}
		
		//If mouse is inside the window, show close button
		if (mouseInside)
		{
			g2.drawImage(close, WIDTH - 16, 0, null);
		}
		if (hovering)
		{
			g2.drawImage(closeHovered, WIDTH - 16, 0, null);
		}
		
		actualCamY += (targetCamY - actualCamY) / 10;
		
		og.drawImage(bufferImg, 0, 0, WIDTH, HEIGHT, null);
	}

	@Override
	public void update(Graphics g) {
		//WTF. How was this working in windows without me having overriden the update method ??
		paint(g);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		int deltaX = e.getXOnScreen() - mouseX;
		int deltaY = e.getYOnScreen() - mouseY;
		int finX = frame.getLocationOnScreen().x + deltaX; //Final x coord of window
		int finY = frame.getLocationOnScreen().y + deltaY; //Final y coord of window
		
		//If window will go out of bounds, adjust final values of X and Y to ensure window sticks to the side and doesn't go further
		if (finX < 0)
			finX = 0;
		else if (finX > SCREEN_WIDTH - WIDTH)
			finX = SCREEN_WIDTH - WIDTH;
		if (finY < 0)
			finY = 0;
		else if (finY > SCREEN_HEIGHT - HEIGHT)
			finY = SCREEN_HEIGHT - HEIGHT;
		
		frame.setLocation(finX, finY);
		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
	}
	
	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
		if (e.getX() >= WIDTH - 16 && e.getY() <= 16)
		{
			hovering = true;
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else
		{
			hovering = false;
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public void mouseClicked(MouseEvent e)
	{
		if (e.getX() >= WIDTH - 16 && e.getY() <= 16)
		{
			System.exit(0);
		}
	}
	
	public void mousePressed(MouseEvent e)
	{
		
	}
	
	public void mouseReleased(MouseEvent e)
	{
		
	}
	
	public void mouseEntered(MouseEvent e)
	{
		mouseInside = true;
	}
	
	public void mouseExited(MouseEvent e)
	{
		mouseInside = false;
		hovering = false;
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
