package client;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

/**
 * The lobby panel GUI
 * 
 * @author Iliya Liksov
 *
 */
@SuppressWarnings("serial")
public class LobbyPanel extends JPanel {
private Client client;
	
	protected final AudioLibrary audioLibrary;
	protected final Font keyFont;
	protected final Font titleFont;
	protected final Font starFont;
	protected final Font headerFont;
	protected int fontSize;
	protected FontMetrics fm;
	protected String text;
	protected int textWidth;
	protected String starLine;
	
	protected String timerData = "";
	protected String playerData = "";
	protected String levelTheme = "";
	protected String mazeSize = "";
	protected String timeLimit = "";
	protected String coinCount = "";
	
	protected BufferedImage header;

	
	protected JButton bReady; // Ready to play
	protected JButton bBack; // Back to queue
	
	public LobbyPanel( Client client ) {
		
		this.client = client;
		
		this.setLayout(null);
		
		  
		this.setBackground( GUIConstants.screenColor );
		this.setSize( GUIConstants.screenWidth, GUIConstants.screenHeight );
		
		audioLibrary = new AudioLibrary();
		
		audioLibrary.load("select", GUIConstants.soundsPath + "select2.wav" );
		
		fontSize = 23;
		titleFont = FontLibrary.newFont("VCR OSD Mono", 30, false, false);
		keyFont = FontLibrary.newFont("VCR OSD Mono", fontSize, false, false);
		starFont = FontLibrary.newFont("VCR OSD Mono", 20 , false, false);
		headerFont = FontLibrary.newFont("VCR OSD Mono", 23 , false, false);
		 
		bReady = new JButton("  READY  ");
		bReady.setFont( keyFont );
		bReady.setForeground( Color.white );
		//bReady.setBorderPainted(false);
		bReady.setBorder(BorderFactory.createMatteBorder(
                                    2, 2, 2, 2, Color.white));
		bReady.setFocusPainted(false);
		bReady.setContentAreaFilled(false);
		bReady.setLayout(null);
 
		
		bBack = new JButton("  LEAVE  ");
		bBack.setFont( keyFont );
		bBack.setForeground( Color.white );
		//bBack.setBorderPainted(false);
		bBack.setBorder(BorderFactory.createMatteBorder(
                2, 2, 2, 2, Color.white));
		bBack.setFocusPainted(false);
		bBack.setContentAreaFilled(false);
		bBack.setLayout(null);
		 

		// Ready button event handler
		bReady.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
			
				client.setready();
			    
				bReady.setEnabled(false);
				
		    }  
			
		});  
		
		bBack.addActionListener(new ActionListener(){ 
			 
			public void actionPerformed(ActionEvent e){ 
				
				client.leavelobby();
			    
				//bBack.setEnabled(false);
				
		    }  
				
		});
		
		bReady.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		bBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		add( bReady );
		add( bBack );
		 
	}
	
	protected void loadHeader() {
		
		if( levelTheme == null || levelTheme.length() == 0 ) { return; }
		
		header = ImageLibrary.load( GUIConstants.levelsPath + levelTheme + "/header.jpg" );
		
	}
	 
	public void paintComponent( Graphics g ) {
			
		super.paintComponent( g );
		
		// Get the dimensions of the panel
		int sw = this.getWidth(); 
		int sh = this.getHeight();
		
		int frameMaxWidth = 1200;
		
	 
		
		g.setFont( starFont );
		fm = g.getFontMetrics();
		starLine = ClientApp.buildStarLine( fm , this.getWidth() - 80 , frameMaxWidth );
		int lineW = fm.stringWidth( starLine );
		
		bBack.setBounds( sw/2 - lineW/2  , 20 , 150 , 40 ); 
		bReady.setBounds( sw/2 + lineW/2 - 150 , 20 , 150 , 40  ); 
		
		g.setColor( Color.white );
		
		g.drawString( starLine , sw/2 - lineW/2 , 100 );
		g.drawString( starLine , sw/2 - lineW/2 , 400 );
		g.drawString( starLine , sw/2 - lineW/2 , 400 + 60 );
		
		g.drawString( starLine , sw/2 - lineW/2 , 700 );
		
		if( header != null ) {
			g.drawImage( header , sw/2 - lineW/2 , 110 , null );
		}
		
		g.setColor( Color.white );
		g.setFont( headerFont );
		
		int hY = 180;
		g.drawString( "LEVEL:        "+levelTheme , sw/2 - lineW/2 + 560 , hY );
		g.drawString( "MAZE SIZE:    "+mazeSize+"x"+mazeSize , sw/2 - lineW/2 + 560 , hY + 40 );
		g.drawString( "TIME LIMIT:   "+timeLimit+" SEC." , sw/2 - lineW/2 + 560 , hY + 80 );
		g.drawString( "COINS:        "+coinCount , sw/2 - lineW/2 + 560 , hY + 120 );
		
		g.drawString( "PLAYERS", sw/2 - lineW/2 ,  427 );
		g.drawString( "STATUS", sw/2 + lineW/10 ,  427 );
		
		if( playerData.length() > 0 ) {
			
			String[] rows = playerData.split("\\|");
			
			String col[];
			String status;
			for( int i = 0 ; i < rows.length; i++ ) {
				
				col = rows[ i ].split("\\,");
				
				g.setColor( Color.white );
				g.drawString( col[0] , sw/2 - lineW/2 , 500 + i*50 );
				
				
				if( col[1].equals("false") ) {
					g.setColor( Color.red );
					status = "**NOT READY**";
				} else {
					g.setColor( Color.green );
					status = "READY";
				}
				g.drawString( status , sw/2 + lineW/10 , 500 + i*50 );
				
			}
			
		}
		
		
		g.setFont( titleFont );
		fm = g.getFontMetrics();
		text = "< GAME LOBBY >";
		textWidth = fm.stringWidth( text );
		g.setColor( Color.WHITE );
		g.drawString( text , sw/2 - textWidth/2 , 55 );
		
		//timerData = "GAME WILL START IN 5 SECONDS.";
		if( timerData.length() > 0 ) {
		
			g.drawString( timerData , sw/2 - fm.stringWidth(timerData)/2 , 760 );
	 
		}
		
			
	}
	
}
