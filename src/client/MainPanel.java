package client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.util.TimerTask;
import java.util.Timer;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private Client client;
	
	protected final AudioLibrary audioLibrary;
	protected final Font keyFont, messageFont;
	protected int fontSize;
	protected FontMetrics fm;
	protected String message = "";
	protected int textWidth;
	protected JButton bQueue, bLeader, bQuit, bLeaderBack;
	
	protected BufferedImage coin;
	protected int coinY = 0;
	 
	protected String playerStats = "";
	protected String playerLast = "";
	protected String mostCoins = "";
	protected String mostPoints = "";
	protected String mostWins = "";
	protected String bestWLRatio = "";
	protected Font legendLabel, legendText;
	
	protected boolean leaderboard = false;
	
	public MainPanel( Client client ) {
		
		this.client = client;
	 
		this.setBackground( GUIConstants.screenColor );
		
		audioLibrary = new AudioLibrary();
		audioLibrary.load( "click", GUIConstants.soundsPath + "ok.wav" );
		audioLibrary.load( "hover", GUIConstants.soundsPath + "switch.wav" );
		
		coin = ImageLibrary.load( GUIConstants.markersPath +"coin.png" );
		
		fontSize = 30;
		keyFont = FontLibrary.newFont("VCR OSD Mono", fontSize, false, false);
		messageFont = FontLibrary.newFont("VCR OSD Mono", 20, false, false);
		
		legendLabel = FontLibrary.newFont("VCR OSD Mono", 20, false, false);
		legendText = FontLibrary.newFont("VCR OSD Mono", 18, false, false);
		
		bQueue = new JButton(" PLAY ");
		bQueue.setFont( keyFont );
		bQueue.setForeground( Color.white );
		bQueue.setBorderPainted(false);
		//bQueue.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
		bQueue.setFocusPainted(false);
		bQueue.setContentAreaFilled(false);
		bQueue.setLayout(null);
		 
		
		bLeader = new JButton(" LEADERBOARD ");
		bLeader.setFont( keyFont );
		bLeader.setForeground( Color.white );
		bLeader.setBorderPainted(false);
		//bLeader.setBorder(BorderFactory.createMatteBorder(3, 3,3, 3, Color.white));
		bLeader.setFocusPainted(false);
		bLeader.setContentAreaFilled(false);
		bLeader.setLayout(null);
		 
		
		bQuit = new JButton(" EXIT ");
		bQuit.setFont( keyFont );
		bQuit.setForeground( Color.white );
		bQuit.setBorderPainted(false);
		//bQuit.setBorder(BorderFactory.createMatteBorder( 3,3,3,3, Color.white));
		bQuit.setFocusPainted(false);
		bQuit.setContentAreaFilled(false);
		bQuit.setLayout(null); 
		
		bLeaderBack = new JButton(" < BACK ");
		bLeaderBack.setFont( keyFont );
		bLeaderBack.setForeground( Color.white );
		//bLeaderBack.setBorderPainted(false);
		bLeaderBack.setBorder(BorderFactory.createMatteBorder(3, 3,3, 3, Color.white));
		bLeaderBack.setFocusPainted(false);
		bLeaderBack.setContentAreaFilled(false);
		bLeaderBack.setLayout(null);
		
		// Switch back from leaderboard to main menu
		bLeaderBack.addActionListener( new ActionListener(){ 
			 
			public void actionPerformed(ActionEvent e){ 
			
				leaderboard = false;
				repaint();
				
		    }  
			
		});  
		 
		
		// Quit app
		bQuit.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
			
				System.exit(1);
				
		    }  
			
		});  
		
		// Ready button event handler
		bQueue.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
				
				audioLibrary.play("click");
				client.moveToQueue();
				
		    }  
			
		});  
		
		// Ready button event handler
		bLeader.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
				
				audioLibrary.play("click");
				client.requestStats();
				client.requestTopScores();
				
				leaderboard = true;
				repaint();
				
		    }  
			
		}); 
		
		bQueue.addMouseListener(new MouseAdapter() {
		    public void mouseEntered(MouseEvent evt) {
		    	bQueue.setForeground(Color.GREEN);
		    	setButtonFocus(420);
		    }
		    public void mouseExited(MouseEvent evt) {
		    	bQueue.setForeground(Color.white);
		    }
		});
		bLeader.addMouseListener(new MouseAdapter() {
		    public void mouseEntered(MouseEvent evt) {
		    	
		    	setButtonFocus(500);
		    }
		    public void mouseExited(MouseEvent evt) {
		    	
		    }
		});
		 
		bQuit.addMouseListener(new MouseAdapter() {
		    public void mouseEntered(MouseEvent evt) {
		    	bQuit.setForeground(Color.RED);
		    	setButtonFocus(580);
		    }
		    public void mouseExited(MouseEvent evt) {
		    	bQuit.setForeground(Color.WHITE);
		    }
		});
		
		bQueue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		bLeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		bQuit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		bLeaderBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		add( bQueue );
		add( bLeader );
		add( bQuit );
		add( bLeaderBack );
		
	}
	
	protected void setButtonFocus( int y ) {
		
		coinY = y;
		audioLibrary.play("hover");
		repaint();
		
	}
	
	public void paintComponent( Graphics g ) {
		
		super.paintComponent( g );
		
		// Get the dimensions of the panel
		int sw = this.getWidth();
		int sh = this.getHeight();
		
		BufferedImage logo = ClientApp.frame.logo;
		
		// Logo image
		int logoW = 0;
		int logoH = 0;
		
		if( logo != null ) {
		  
			 
			 
			logoW = (int) (logo.getWidth()/1.5);
			logoH = (int) (logo.getHeight()/1.5);
			
			// Scale the image down with the screen if the screen is too small for the original image
			if( logoW > sw ) {
				 
				float logoAspectRatio = (float) logoW / logoH;
			 
				logoW = sw;
				logoH = (int) (logoW / logoAspectRatio ); 
				
			}
			
			g.drawImage( logo , sw/2 - logoW/2 , 20 , logoW , logoH , null );
				
		 
		}
		
		bQueue.setBounds( sw/2 - 150  , 420 , 300 , 50 ); 
		bLeader.setBounds( sw/2 - 150  , 500 , 300 , 50 ); 
		bQuit.setBounds( sw/2 - 150  , 580 , 300 , 50 ); 
		bLeaderBack.setBounds( 100 , 50 , 150 , 50 );
		
		// Leaderboard code
		if( leaderboard == true ) {
			
			// Hide the buttons from the main menu
			bQueue.setVisible(false);
			bLeader.setVisible(false);
			bQuit.setVisible(false);
			// Show the back button for the leaderboard panel
			bLeaderBack.setVisible(true);
			
			// DRAW LEADERBOARD HERE
			String[] mystats = playerStats.split("\\,");
			String[] lastgame = playerLast.split("\\,");
			if( mystats.length < 6 ) { return; }
			
			g.setColor(Color.white);
			g.setFont( legendText );
			fm = g.getFontMetrics();
			
			String bigline = ClientApp.buildStarLine( fm , 1100 , 1100 );
			int blinew = fm.stringWidth( bigline );
			
			String leftline = ClientApp.buildStarLine( fm , 780 , 780 );
			int llinew = fm.stringWidth( leftline );
			
			String rightline = ClientApp.buildStarLine( fm , 260 , 260 );
			int rlinew = fm.stringWidth( rightline );
			
			g.drawString( leftline, sw/2 - blinew/2, 270 );
			g.drawString( leftline, sw/2 - blinew/2, 310 );
			
			g.drawString( rightline, sw/2 - blinew/2 + llinew + 40 , 270 );
			g.drawString( rightline, sw/2 - blinew/2 + llinew + 40 , 310 );
			
			g.drawString( bigline ,  sw/2 - blinew/2, 520 );
			g.drawString( bigline ,  sw/2 - blinew/2, 560 );
			
			int colw = 780 / 6;
			int heady = 287;
			int rowy = 340;
			
			g.drawString( "PLAYED", sw/2 - blinew/2 , heady );
			g.drawString( "FINISHED", sw/2 - blinew/2 + colw , heady );
			g.drawString( "WON", sw/2 - blinew/2 + colw*2 , heady );
			g.drawString( "WIN/LOSS", sw/2 - blinew/2 + colw*3 , heady );
			g.drawString( "COINS", sw/2 - blinew/2 + colw*4 , heady );
			g.drawString( "POINTS", sw/2 - blinew/2 + colw*5 , heady );
			
			int col2w = 1100/4;
			int head2y = 537;
			g.drawString( "MOST COINS", sw/2 - blinew/2 , head2y );
			g.drawString( "MOST POINTS", sw/2 - blinew/2 + col2w , head2y );
			g.drawString( "MOST WINS", sw/2 - blinew/2 + col2w*2 , head2y );
			g.drawString( "BEST W/L RATIO", sw/2 - blinew/2 + col2w*3 , head2y );
			
			g.drawString( mystats[0], sw/2 - blinew/2 , rowy );
			g.drawString( mystats[1], sw/2 - blinew/2 + colw , rowy );
			g.drawString( mystats[2], sw/2 - blinew/2 + colw*2 , rowy );
			g.drawString( String.format("%.3f", Float.parseFloat(mystats[3]) ), sw/2 - blinew/2 + colw*3 , rowy );
			g.drawString( mystats[4] , sw/2 - blinew/2 + colw*4 , rowy );
			g.drawString( mystats[5] , sw/2 - blinew/2 + colw*5 , rowy );
			
			g.setColor(Color.green);
			g.setFont(legendLabel);
			g.drawString( "MY GAME STATS ["+ClientApp.frame.username+"]", sw/2 - blinew/2, 230);
			g.drawString( "LAST GAME", sw/2 - blinew/2 + llinew + 40 , 230);
			g.drawString( "LEADERBOARD - TOP 5", sw/2 - blinew/2, 480 );
			
			g.setColor(Color.white);
			g.setFont(legendText);
			if( lastgame.length < 7 ) {
				g.drawString( " -- none yet --", sw/2 - blinew/2 + llinew + 40, heady );
				
			} else {
			
				g.drawString( lastgame[6] , sw/2 - blinew/2 + llinew + 40, heady );
				
				int pos = Integer.parseInt( lastgame[2] );
				String suffix = "th";
				if( pos == 1 ) { suffix = "st"; } 
				else if( pos == 2 ) { suffix = "nd"; }
				else if( pos == 3 ) { suffix = "rd"; }
				
				g.drawString( "LEVEL: " + lastgame[0] , sw/2 - blinew/2 + llinew + 40, rowy );
				g.drawString( "SIZE: " + lastgame[1] + "x" + lastgame[1] , sw/2 - blinew/2 + llinew + 40, rowy + 25 );
				g.drawString( "POSITION: " + lastgame[2] + suffix , sw/2 - blinew/2 + llinew + 40, rowy +50 );
				g.drawString( "TIME: " + lastgame[3] + " SEC." , sw/2 - blinew/2 + llinew + 40, rowy +75);
				g.drawString( "COINS: " + lastgame[4] , sw/2 - blinew/2 + llinew + 40, rowy +100);
				g.drawString( "POINTS: " + lastgame[5] , sw/2 - blinew/2 + llinew + 40, rowy +125 );
				
			}
			
			drawColumn( g , mostCoins , sw/2 - blinew/2 , 600 , 60 , Color.white , Color.yellow );
			drawColumn( g , mostPoints , sw/2 - blinew/2 + col2w , 600 , 60 , Color.white , new Color(255,100,0) );
			drawColumn( g , mostWins , sw/2 - blinew/2 + col2w*2 , 600 , 60 , Color.white , Color.cyan );
			drawColumn( g , bestWLRatio , sw/2 - blinew/2 + col2w*3 , 600 , 60 , Color.white , new Color(0,120,255) );
			
			return;
			
		}  
		
		// Show the buttons for the main menu
		bQueue.setVisible(true);
		bLeader.setVisible(true);
		bQuit.setVisible(true);
		// Hide leaderboard panel
		bLeaderBack.setVisible(false);
			
		 
		
		if( coin != null && coinY > 0 ) {
			
			g.drawImage( coin , sw/2 - 250 , coinY , null);
			
		}
		
		if( message.length() > 0 ) {
			
			g.setFont( messageFont );
			fm = g.getFontMetrics();
			 
			textWidth = fm.stringWidth( message );
			g.setColor( Color.WHITE );
			g.drawString( message , sw/2 - textWidth/2 , 300 );
		
		}
			
	}
	
	private void drawColumn( Graphics g , String data , int sx , int sy , int rowh , Color col1 , Color col2 ) {
		
		if( mostCoins.length() == 0 )  { return; }
		
		String[] rows = data.split("\\|");
		 
		
		for( int i = 0 ; i < rows.length; i ++ ) {
			
			String[] col = rows[i].split("\\,");
			if( col.length < 2 ) { continue; }
			
			g.setColor(col1);
			g.drawString( ""+(i+1)+". " + col[0] , sx , sy + rowh*i );
			g.setColor(col2);
			g.drawString( "   " + col[1] , sx, sy + rowh*i + 30 );

			
		}
		
		
	}
	
}
