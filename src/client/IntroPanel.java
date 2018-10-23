package client;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.Font;

import java.util.TimerTask;
import java.util.Timer;

import javax.swing.JPanel;

import protocol.Protocol;

@SuppressWarnings("serial")
public class IntroPanel extends JPanel {

	public final AudioLibrary audioLibrary;
	public final Font keyFont;
	public final Font creditsFont;
	public int fontSize;
	public FontMetrics fm;
	public String text;
	public int textWidth;
	
	private KeyListener keyListener;
	
	private Timer t;
	
	private Color buttonColor = Color.WHITE;
	private int buttonAlpha = 255;
	private int fadeDir = -1;
	
	private int introText = 0;
	private boolean enableInput = false;
	
	private String[][] lines;
	
	public IntroPanel() {
		
		this.setBackground( GUIConstants.screenColor );
		
		audioLibrary = new AudioLibrary();
		
		audioLibrary.load("select", GUIConstants.soundsPath + "select2.wav" );
		
		fontSize = 30;
		keyFont = FontLibrary.newFont("VCR OSD Mono", fontSize, false, false);
		creditsFont = FontLibrary.newFont("VCR OSD Mono", 20, false, false);
		
		lines = new String[][] {
			{ "University of Birmingham" , "2018" },
			{ "Created by" , "*******************" , "I.E.Liksov" },
			{ "Credits" , "**************************" , "Artwork: OpenGameArt.org" , "Music: Ozzed.net" , "Misc: Freesound.org" }
		};
		
		
		Timer t2 = new Timer();
		
		t2.scheduleAtFixedRate( new TimerTask() {
			
			@Override
			public void run() {
				 
				 
				if( introText >= lines.length - 1) {
					
					enableInput = true;
					blinkText();
					introText = 0;
					t2.cancel();
					return;
				
				} else {
					
					repaint();
					
				}
				
				introText++;
				
				
			}
			
		} , 2000, 2000 );
		
		addListener();
		
	}
	
	protected void blinkText() {
		
		if( t != null ) {
			t.cancel();
		}
		
		t = new Timer();
		
		t.scheduleAtFixedRate( new TimerTask() {
			
			@Override
			public void run() {
				 
				buttonAlpha += 5 * fadeDir;
				if( buttonAlpha < 0 ) {
					buttonAlpha = 0;
					fadeDir = 1;
				} else if( buttonAlpha > 255 ) {
					buttonAlpha = 255;
					fadeDir = -1;
				}
				
				buttonColor = new Color( 255 , 255 , 255 , buttonAlpha );
				repaint();
				
			}
			
		}, 0, 20 );
		
	}
	
	public void addListener() {
		
		keyListener = newKeyListener();
		addKeyListener( keyListener );
		
	}
	
	public void removeKeyListener() {
		
		if( keyListener == null ) { return; }
		
		removeKeyListener( keyListener );
		
	}
	
	public KeyListener newKeyListener() {
	
		return new KeyListener() {
			
			private boolean enter = false;
			
			
			@Override
			public void keyTyped( KeyEvent e ) {}

			@Override
			public void keyPressed( KeyEvent e ) {
				
				int key = e.getKeyCode();
				
				if( key == KeyEvent.VK_ENTER && !enter ) {
					// Play a little jingle
					audioLibrary.play("select");
					// Cancel blinking text
					if( t != null ) { t.cancel(); }
					// Go to the next panel
					ClientApp.frame.setAuth();
					
					enter = true;
					
				}
				
			}
			
			@Override
			public void keyReleased( KeyEvent e ) {
				
				int key = e.getKeyCode();
				
				if( key == KeyEvent.VK_ENTER && enter ) {
					
					enter = false;
					
				}
				
			}
			
		};
	
	}
	
	public void paintComponent( Graphics g ) {
			
		super.paintComponent( g );
		
		// Get the dimensions of the panel
		int sw = this.getWidth();
		int sh = this.getHeight();
		
		BufferedImage logo = ClientApp.frame.logo;
		
		if( logo != null ) {
		  
			// Logo image
			int logoW = 0;
			int logoH = 0;
			 
			logoW = logo.getWidth();
			logoH = logo.getHeight();
			
			// Scale the image down with the screen if the screen is too small for the original image
			if( logoW > sw ) {
				 
				float logoAspectRatio = (float) logoW / logoH;
			 
				logoW = sw;
				logoH = (int) (logoW / logoAspectRatio ); 
				
			}
			
			g.drawImage( logo , sw/2 - logoW/2 , sh/2 - logoH/2 - 200 , logoW , logoH , null );
				
		 
		}
		
		if( enableInput == true ) {
			
			g.setFont( keyFont );
			fm = g.getFontMetrics();
			text = "PRESS [ENTER] TO CONTINUE";
			textWidth = fm.stringWidth( text );
			g.setColor( buttonColor );
			g.drawString( text , sw/2 - textWidth/2 , sh/2 + 200 - fontSize/2 );
		
		} else {
			
			if( introText < lines.length ) {
				
				g.setFont( creditsFont );
				g.setColor( Color.white );
				fm = g.getFontMetrics();
				
				String txt;
				for( int i = 0 ; i < lines[ introText ].length ; i++ ) {
					
					txt = lines[ introText ][ i ];
					g.drawString( txt , sw/2 - fm.stringWidth( txt )/2 , sh/2 + 50 + 30*i );
					
				}
				
			}
			
			
		}
	 
			
	}
	
}
