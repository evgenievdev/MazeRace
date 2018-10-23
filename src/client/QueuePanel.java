package client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class QueuePanel extends JPanel {
	
	private Client client;
	
	protected final AudioLibrary audioLibrary;
	protected final Font keyFont;
	protected int fontSize;
	protected FontMetrics fm;
	protected String text;
	protected int textWidth;
	protected JButton bBack;
	
	protected String queueData;
	
	private KeyListener keyListener;
	
	
	public QueuePanel( Client client ) {
		
		this.client = client;
		
		queueData = "";
		
		this.setBackground( GUIConstants.screenColor );
		
		audioLibrary = new AudioLibrary();
		
		audioLibrary.load("select", GUIConstants.soundsPath + "select2.wav" );
		
		fontSize = 23;
		keyFont = FontLibrary.newFont("VCR OSD Mono", fontSize, false, false);
		
		bBack = new JButton("  LEAVE  ");
		bBack.setFont( keyFont );
		bBack.setForeground( Color.white );
		//bBack.setBorderPainted(false);
		bBack.setBorder(BorderFactory.createMatteBorder(
                2, 2, 2, 2, Color.white));
		bBack.setFocusPainted(false);
		bBack.setContentAreaFilled(false);
		bBack.setLayout(null);
		 
		add( bBack );
		
		// Ready button event handler
		bBack.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
			
				client.leaveQueue();
			     
				
		    }  
			
		}); 
		
		bBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
	}
	
	   
	
	public void drawQueueData( Graphics g , int sx , int sy , int sx2 ) {
		
		if( queueData == null || queueData.length() == 0 ) {
			return;
		}
		
		String[] rows = queueData.split("\\|");
		
		if( rows.length == 0 ) { return; }
		
		int numPlayers = rows.length;
		
		g.setColor( Color.RED );
		//g.drawString( "" , x, y);
		
		String[] row;
		for( int i = 0 ; i < numPlayers ; i++ ) {
			
			row = rows[ i ].split("\\,");
			
			if( ClientApp.frame.username.equals( row[0]) ) {
				g.setColor( Color.green );
			} else {
				g.setColor( Color.white );
			}
			
			g.drawString( row[ 0 ] , sx , sy + 30 * i );
			g.drawString( row[ 1 ] , sx2 , sy + 30 * i );
			
		}
		
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
		
		g.setFont( keyFont );
		fm = g.getFontMetrics();
		text = "LIST OF PLAYERS WAITING IN THE QUEUE";
		textWidth = fm.stringWidth( text );
		g.setColor( Color.WHITE );
		g.drawString( text , sw/2 - textWidth/2 , sh/2 - fontSize/2 );
		
		text = "****************************************************";
		textWidth = fm.stringWidth( text );
		g.drawString( text , sw/2 - textWidth/2 , sh/2 + fontSize );
		
		g.drawString( "Name" , sw/2 - textWidth/2 , sh/2 + fontSize*2 );
		g.drawString( "Joined" , sw/2 , sh/2 + fontSize*2 );
		
		g.drawString( text , sw/2 - textWidth/2 , sh/2 + fontSize*3 + fontSize/2 );
		
		drawQueueData( g , sw/2 - textWidth/2 , sh/2 + fontSize*4 + fontSize/2 , sw/2 );
		
		bBack.setBounds( 100 , 50 , 150 , 50 );
			
	}
	
}