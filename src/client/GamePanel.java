package client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent; 
import java.awt.event.KeyListener;
 
import server.mazegeneration.MazeConstants;
 
@SuppressWarnings("serial")
public class GamePanel extends JPanel {

	Client client;
	
	private int[][] maze;
	private int[][] coins;
	
	private KeyListener inputListener;
 
	
	// Library objects
	public AudioLibrary audioLibrary;
	public ImageLibrary imageLibrary;
	public FontLibrary fontLibrary;
	 
	protected ArrayList<int[]> playerPositions = new ArrayList<int[]>();
 
	protected String playerStatuses = "";
	protected String playerOrientations = "";
	protected String playerCoins = "";
	protected String trapsList = "";
	protected String message = "";
	
	protected int collectedCoins = 0;
	protected int firesLeft = 0;
	
	// This client's index
	protected int playerIndex = 0;
	protected int orientation = 0;
	 
	protected boolean STARTED = false;
	protected boolean FINISHED = false;
	
	protected int timeLeft = -1;
	protected int startCountdown = -1;
	protected int returnCountdown = -1;
	
	protected FontMetrics fm;
	
	protected String[][] scores = new String[0][0];
	
	protected JButton bBack;
	
	// The current theme (used by constructor)
	protected String theme = "";
	// Available themes
	private static final String[] themes = new String[] {
		"Gardens",
		"Forest",
		"Desert",
		"Dungeon",
		"Underworld",
		"Winter"
	};
	
	private Font scoresFont, starFont, startFont;
	private Color startOverlay;
	
	public GamePanel( Client client ) {
	 
		this.client = client;
		
		this.setBackground( GUIConstants.screenColor );
		 
		// Load sounds (Must be .wav only)
		// If a sound can not be loaded it won't interrupt the game in any way
		audioLibrary = new AudioLibrary();
		// Load all images for the client
		imageLibrary = new ImageLibrary();
		// Font Library for this panel
		fontLibrary = new FontLibrary();
		
		scoresFont = FontLibrary.newFont( "VCR OSD Mono", 30, false, false );
		starFont = FontLibrary.newFont( "VCR OSD Mono", 20, false, false );
		
		startFont = FontLibrary.newFont( "VCR OSD Mono", 45, false, false );
		startOverlay = new Color( 0 , 0 , 0 , 220 );
		
		Font keyFont = FontLibrary.newFont("VCR OSD Mono", 23, false, false);
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
		bBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		
		// Ready button event handler
		bBack.addActionListener( new ActionListener(){ 
		 
			public void actionPerformed(ActionEvent e){ 
			
				ClientApp.frame.lobby.bReady.setEnabled(true);
			
				ClientApp.frame.main.message = "";
				
				ClientApp.frame.setMain();
				ClientApp.frame.main.repaint();
			     
				
		    }  
			
		}); 
		
		
	}
	
	protected void decodeScores( String data , char delimiter ) {
		
		if( data == null || data.length() == 0 ) { return; }
		
		String[] rows = data.split("\\"+delimiter); 
		if( rows.length == 0 ) { return; }
		
		String[] cols = rows[ 0 ].split("\\,");
		if( cols.length == 0 ) { return; }
		// Initialize the scores array
		scores = new String[ rows.length ][ cols.length ];
		
		for( int i = 0 ; i < rows.length; i ++ ) {
			
			cols = rows[i].split("\\,");
			
			for( int j = 0 ; j < cols.length ; j++ ) {
				
				scores[ i ][ j ] = cols[ j ];
				
			}
			
		}
		
	}
	
	protected void setPlayerPositions( String data , char delimiter ) {
		
		if( data == null || data.length() == 0 ) { return; }
		
		String[] players = data.split("\\"+delimiter);
		
		if( players.length == 0 ) { return; }
		
		playerPositions = new ArrayList<int[]>();
		
		String[] pos;
		for( int i = 0 ; i < players.length; i ++ ) {
			
			pos = players[i].split("\\,");
			if( pos.length < 2 ) { continue; }
			
			int x = Integer.parseInt( pos[0] );
			int y = Integer.parseInt( pos[1] );
			
			playerPositions.add( new int[] { x , y } );
			
		}
		
	}
	
	public void removeListener() {
		
		if( inputListener == null ) { return; }
		removeKeyListener( inputListener );
		
	}
	
	public void addListener() {
		
		if( inputListener != null ) { return; }
		
		// Create input listeners for this client
		inputListener = getInputListener();
		addKeyListener( inputListener );
		this.setFocusable(true);
		
	}
	
	public void setTheme( String levelTheme ) {
 
		// The name of the theme to load textures from (if the name is invalid, use the default (1st theme)
		String newTheme = inArray( themes , levelTheme ) ? levelTheme : themes[ 0 ];
		
		if( theme == null || !theme.equals( newTheme ) ) {
			
			theme = newTheme;
			// Reload textures for new theme
			imageLibrary.removeAll();
			loadTextures();
			
		}
		
	}
	
	public void loadSounds() {
		
		audioLibrary.load( "coin" , GUIConstants.soundsPath + "collected.wav" );
		audioLibrary.load( "finish" , GUIConstants.soundsPath + "finish.wav" );
		audioLibrary.load( "move" , GUIConstants.soundsPath + "move.wav" );
		audioLibrary.load( "fire" , GUIConstants.soundsPath + "place.wav" );
		audioLibrary.load( "hit" , GUIConstants.soundsPath + "hit.wav" );
		
		audioLibrary.load( "ost_shell_shock" , GUIConstants.soundsPath + "ost_shell_shock.wav" );
		audioLibrary.load( "ost_top_city" , GUIConstants.soundsPath + "ost_top_city.wav" );
		audioLibrary.load( "ost_failien_funk" , GUIConstants.soundsPath + "ost_failien_funk.wav" );
		audioLibrary.load( "ost_8bit_empire" , GUIConstants.soundsPath + "ost_8bit_empire.wav" );
		
		 
		//audioLibrary.play("music1");
		 
		
	}
	
	public void soundtrack() {
		String[] soundtrack = new String[] { "ost_shell_shock" , "ost_top_city" , "ost_failien_funk" , "ost_8bit_empire" };
		Collections.shuffle( Arrays.asList( soundtrack ) );	// Shuffle the songs order
		audioLibrary.newSequence( soundtrack ); // Make a simple sequence of songs to play in rotation
		audioLibrary.playSequence( true ); // Call this the first time then rely on the input class to loop around 
	}
	
	public void loadTextures() {
		
		imageLibrary.load( "wall" , GUIConstants.levelsPath + theme + "/wall.jpg" );
		imageLibrary.load( "tree1" , GUIConstants.levelsPath + theme + "/tree_a.gif" );
		//imageLibrary.load( "tree2" , GUIConstants.levelsPath + theme + "/tree_b.png" );
		
		imageLibrary.load( "path" , GUIConstants.levelsPath + theme + "/path_a.jpg" );
		imageLibrary.load( "finish" , GUIConstants.levelsPath + theme + "/finish.jpg" );
		
		imageLibrary.load( "coin" , GUIConstants.markersPath + "coin.png" );
		imageLibrary.load( "trap" , GUIConstants.markersPath + "trap.png" );
		
		imageLibrary.load( "player0" , GUIConstants.playersPath + "0.png" );
		imageLibrary.load( "player1" , GUIConstants.playersPath + "1.png" );
		imageLibrary.load( "player2" , GUIConstants.playersPath + "2.png" );
		imageLibrary.load( "player3" , GUIConstants.playersPath + "3.png" );
		
	}
	
	public void loadFonts() {
		
		fontLibrary.add( "retro", "VCR OSD Mono", 18, false , false );
		fontLibrary.add( "retroBig", "VCR OSD Mono", 36, false , false );
		
	}
	
	public void loadResources() {
		
		loadSounds(); 
		
		loadTextures();
 
		loadFonts();
		
	}
	
	public KeyListener getInputListener() {
		
		return new KeyListener() {
			
			private boolean up, down, left, right, fire, quit = false;
			
			private boolean validInput = false;
			
			int orient = 0; // up , down , left , right
			 
			void look(int dir) {
				
				audioLibrary.playSequence( true );
				
				if( dir == orient ) {
					
					client.move(dir);
					
				} else {
					
					orient = dir;
					orientation = dir;
					repaint();
					
				}
				
				
			}
			
			@Override
			public void keyTyped( KeyEvent e ) {
				
			}

			@Override
			public void keyPressed( KeyEvent e ) {
				
				if( FINISHED || !STARTED ) { return; }
				
				int key = e.getKeyCode();
				//String key = KeyEvent.getKeyText( e.getKeyCode() );
			  
				validInput = false;
		 
				// Note it is very possible that getKeyText doesn't work on Mac/Linux properly. 
				// Might have to use keyCodes instead, but those differ from keyboard layout to layout
				if( key == GUIConstants.upKey && up == false ) {
					
					up = true;
					
					validInput = true;
					
					//client.move(0);
					look(0);
					
				} else if( key == GUIConstants.downKey && down == false ) {
					
					down = true;
					
					validInput = true;
					
					//client.move(1);
					look(1);
					
				} else if( key == GUIConstants.leftKey && left == false ) {
					
					left = true;
					
					validInput = true;
					
					//client.move(2);
					look(2);
						
				} else if( key == GUIConstants.rightKey && right == false ) {
					
					right = true;
					
					validInput = true;
					
					//client.move(3);
					look(3);
					
				} else if( key == GUIConstants.fireKey && fire == false ) {
					
					fire = true;
					
					validInput = true;
					
					client.fire();
					
				} else if ( key == GUIConstants.quitKey && quit == false ) { // For some unknown reason Q has to be read as a keyCode [=81] instead of a String... ?!!?!?!?!
					
					quit = true;
					
					audioLibrary.stopSequence();
					
					// Send server request to close application
					//request.sendQuitRequest();
					
				}
			 
				if( validInput ) {
					
					audioLibrary.playSequence( true );
					
				}
			 
				
			}

			@Override
			public void keyReleased( KeyEvent e ) {
				
				int key = e.getKeyCode();
				//String key = KeyEvent.getKeyText( e.getKeyCode() );
				
				if( key == GUIConstants.upKey && up == true ) {
					
					up = false;
					
				} else if( key == GUIConstants.downKey && down == true ) {
					
					down = false;
					
				} else if( key == GUIConstants.leftKey && left == true ) {
					
					left = false;
					
				} else if( key == GUIConstants.rightKey && right == true ) {
					
					right = false;
					
				} else if( key == GUIConstants.quitKey && quit == true ) {
					
					quit = false;
					
				} else if( key == GUIConstants.fireKey && fire == true ) {
					
					fire = false;
					
				}
				 
				
			}
			
		};
		
	}
	
	 
	
	public void setData( int[][] maze , int[][] coins ) {
 
		this.maze = maze;
		this.coins = coins;
		this.repaint();
		
	}
	 
	public void paintComponent( Graphics g ) {
		
		// Record the current system time BEFORE a maze has been created
		long startTime = System.currentTimeMillis();
		 
		super.paintComponent( g );
		
		// Panel dimensions
		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();
		
		bBack.setVisible(false);
		
		// Draw the Stats panel if the game is finished.
		if( FINISHED == true ) {
			audioLibrary.stopSequence();
			audioLibrary.clearSequence(); 
			g.setColor(Color.white);
			
			if( scores.length > 0 ) {
				 
				int rows = scores.length;
				int cols = scores[0].length;
				
				g.setFont( starFont );
				fm = g.getFontMetrics();
				String starLine = ClientApp.buildStarLine( fm , panelWidth/2 , 1000 );
				int lineW = fm.stringWidth( starLine );
				
				g.drawString( starLine , panelWidth/2 - lineW/2 , 250 );
				g.drawString( starLine , panelWidth/2 - lineW/2 , 300 );
				g.drawString( starLine , panelWidth/2 - lineW/2 , 700 );
				 
				g.setFont(scoresFont);
				fm = g.getFontMetrics();
				int colW = lineW/cols;
				
				String title = "";
				title += "THE GAME HAS FINISHED.";
				
				if( scores[0][0].equals( ClientApp.frame.username ) ) {
					title = "CONGRATULATIONS! YOU WON THE RACE!";
				}
				
				g.drawString( title , panelWidth/2 - fm.stringWidth(title)/2 , 200 );
				
				if( returnCountdown > -1 ) {
					
					String returntxt = "RETURNING BACK TO THE MAIN MENU IN: " + returnCountdown;
					g.drawString( returntxt , panelWidth/2 - fm.stringWidth(returntxt)/2 , 750 );
					
					if( returnCountdown == 0 ) {
						bBack.setVisible(true);
						bBack.setBounds( 100 , 50 , 150 , 50 );
					}  
					
				}
				
				g.drawString( "PLAYER ", panelWidth/2 - lineW/2 , 275 );
				g.drawString( "TIME ", panelWidth/2 - lineW/2 + 40 + colW  , 275 );
				g.drawString( "COINS", panelWidth/2 - lineW/2  + 40 + 2 * colW, 275 );
				g.drawString( "POINTS", panelWidth/2 - lineW/2  + 40 + 3 * colW, 275 );
				
				String multiplier, col;
				
				for( int i = 0 ; i < rows ; i++ ) {
					
					if( scores[i][0].equals( ClientApp.frame.username ) ) {
						g.setColor(Color.green);
					} else {
						g.setColor(Color.white);
					}
					
					g.drawString( "" + (i+1) + ". " , panelWidth/2 - lineW/2 , 350 + i*80 ); 
					
					for( int j = 0 ; j < cols ; j++ ) {
						
						if( j == 2 && i == 0 ) {
							multiplier = " (x2)";
						} else {
							multiplier = "";
						}
						
						col = scores[ i ][ j ];
						
						 					
						if( j == 1 && col.equals("-1") ) {
							g.setColor(Color.red);
							col = "DNF";
						} else {
							if( scores[i][0].equals( ClientApp.frame.username ) ) {
								g.setColor(Color.green);
							} else {
								g.setColor(Color.white);
							}
							if( j == 1 ) {
								col += " SEC.";
							}
						}
						
						if( j == 0 ) {
							if( col.length() > 10 ) {
								col = col.substring( 0 , 10 ) + "..";
							}
						}
						
						g.drawString( col + multiplier , panelWidth/2 - lineW/2 + 40 + j * colW , 350 + i*80 );
						
					}
					
				}
				
			}
			
			return;
			
		}
		 
		
		if( maze == null || coins == null ) { return; }
		 
		int xTileSize = GUIConstants.mazeTileX;
		int yTileSize = GUIConstants.mazeTileY;
		int offsetX = GUIConstants.mazeOffsetX;
		int offsetY = GUIConstants.mazeOffsetY;
		
		int mazeLengthX = maze.length;
		int mazeLengthY = maze[ 0 ].length;
		 
		int adjustX = 0;
		int adjustY = 0;
		
		// Panel aspect ratio (width/height)
		float aspectRatio = (float) panelWidth / panelHeight;
		 
		// Dynamic tile sizing (must be defined before calculating offsets!)
		xTileSize = (panelWidth - GUIConstants.mazePaddingX*2) / mazeLengthX;
		float fixTileSize = xTileSize / aspectRatio;
		xTileSize = (int) fixTileSize; // NOTE! Casting from floats to integers causes some slight discrepancy, but Java GUI isn't well made
		yTileSize = (panelHeight - GUIConstants.mazePaddingY*2) / mazeLengthY;
		
		// Center the maze in the panel on the X axis (Y axis is a different issue)
		//adjustX = offsetX + panelWidth / 2 - ((mazeLengthX) * xTileSize)/2;
	    adjustY = offsetY + panelHeight / 2 - ((mazeLengthY) * yTileSize)/2;
		adjustX = offsetX;
		 
	  
		for ( int i = 0; i < mazeLengthX; i++ ) {
			
			for ( int j = 0; j < mazeLengthY; j++ ) {
				
				if ( maze[i][j] == MazeConstants.WALL ) {

					if( imageLibrary.imageExists("wall") ) {
						// Base wall background
						g.drawImage( imageLibrary.getImage("wall") , adjustX + j * xTileSize , adjustY + i * yTileSize , xTileSize , yTileSize,  null );
					} else {
						// Backup wall
						g.setColor( GUIConstants.wallColor );
						g.fill3DRect( adjustX + j * xTileSize, adjustY + i * yTileSize, xTileSize, yTileSize, true );
					}
					
				} else if ( maze[i][j] == MazeConstants.WALKABLE ) {

					if( imageLibrary.imageExists("path") ) {
						// Path image
						g.drawImage( imageLibrary.getImage("path") , adjustX + j * xTileSize , adjustY + i * yTileSize , xTileSize , yTileSize,  null );
					} else {
						// Backup path
						g.setColor( GUIConstants.pathColor );
						g.fill3DRect( adjustX + j * xTileSize, adjustY + i * yTileSize, xTileSize, yTileSize, true );
					}
					
				} else if ( 
						
						maze[i][j] == MazeConstants.START || 
						maze[i][j] == MazeConstants.START_P1 ||
						maze[i][j] == MazeConstants.START_P1 ||
						maze[i][j] == MazeConstants.START_P3 ||
						maze[i][j] == MazeConstants.START_P4 
					
				) {
					
					if( imageLibrary.imageExists("path") ) {
						// Path image
						g.drawImage( imageLibrary.getImage("path") , adjustX + j * xTileSize , adjustY + i * yTileSize , xTileSize , yTileSize,  null );
					} else {
						// Backup path
						g.setColor( GUIConstants.startColor );
						g.fill3DRect( adjustX + j * xTileSize, adjustY + i * yTileSize, xTileSize, yTileSize, true );
					}
					
					
				} else if ( maze[i][j] == MazeConstants.FINISH ) {
					
					if( imageLibrary.imageExists("finish") ) {
						// Path image
						g.drawImage( imageLibrary.getImage("finish") , adjustX + j * xTileSize , adjustY + i * yTileSize , xTileSize , yTileSize,  null );
					} else {
						// Backup path
						g.setColor( GUIConstants.finishColor );
						g.fill3DRect( adjustX + j * xTileSize, adjustY + i * yTileSize, xTileSize, yTileSize, true );
					}
					
				}
				
				// Draw Coins
				if( coins[ i ][ j ] == 1 ) {
					
					if( imageLibrary.imageExists("coin") ) {
						// Draw coin
						g.drawImage( imageLibrary.getImage("coin") , adjustX + j * xTileSize , adjustY + i * yTileSize , xTileSize , yTileSize,  null );
					} else {
						// Backup coin drawing
						g.setColor( GUIConstants.coinColor );
						g.fillOval( adjustX + j * xTileSize + xTileSize/3, adjustY + i * yTileSize + yTileSize/3 , xTileSize/4, yTileSize/4 );
					}
					
				}
				 
				
			}
			
		}
		
		// Draw traps
		String[] traps = trapsList.split("\\|");
		
		boolean trapIconExists = imageLibrary.imageExists("trap");
		if( traps.length > 0 && trapIconExists ) {
			
			BufferedImage trapIcon = imageLibrary.getImage("trap");
			int tx , ty;
			String col[];
			
			for( int i = 0 ; i < traps.length; i ++ ) { 
				
				col = traps[ i ].split("\\,");
				if( col.length < 3 ) { continue; }
				
				tx = Integer.parseInt( col[0] );
				ty = Integer.parseInt( col[1] );
				g.drawImage( trapIcon , adjustX + ty * xTileSize , adjustY + tx * yTileSize , xTileSize , yTileSize,  null );
	 
			}
			
		}
		
		// Draw Timer
		if( timeLeft >= 0 ) {
			
			String hurry = "";
			if( timeLeft > 30 ) {
				g.setColor(Color.white);
			} else {
				g.setColor(Color.red);
				hurry = " HURRY!";
			}
			g.setFont( scoresFont );
			g.drawString( ""+timeLeft + " sec. left." + hurry , adjustX + mazeLengthY * xTileSize + 20 ,  70 );
			
		}
		
		
		
		// Draw players (MUST BE BEFORE TREES)
		int[] playerPos;
		int playerX , playerY , playerOrientation = 0;
	 
		// Get the sprite frame based on the player's orientation
		BufferedImage playerImage;
		
		int playerCount = playerPositions.size();
		
		// A hack to make sure that the player sprite is rendered above all others
		// It creates a temporary array and swaps the player's element and the last one, 
		// making sure that when the draw loop runs player sprite will be drawn last
		int[] aid = new int[ playerCount ];
		for( int i = 0 ; i < playerCount; i++) {
			aid[i]=i;
		}
		
		String[] orientations = playerOrientations.split("\\|");

		for( int i = 0 ; i < playerCount ; i++ ) {
			
			if( i < playerCount - 1 && i == playerIndex ) {
				aid[ playerCount-1 ] = i;
				aid[ i ] = playerCount-1;
			}
			
			playerPos = playerPositions.get( aid[i] );
			playerX = playerPos[ 0 ];
			playerY = playerPos[ 1 ];
			
			// -1 indicates that the player is either in a position outside the range of the maze rectangle OR they are inactive
			// Therefore skip this element and don't draw them
			if( playerX == -1 || playerY == -1 ) {
				continue;
			}
			
			if( orientations.length == playerCount ) {
				if( orientations[ aid[i] ].equals(".") ) {
					continue;
				}
				playerOrientation = Integer.parseInt( orientations[ aid[i] ] );
			}
		 
			if( playerIndex == aid[i] ) {
				playerOrientation = orientation;
				// Inconsistency between sprite frame directions and server logic [HACK]
				if( orientation == 1 ) { playerOrientation = 2; } 
				else if( orientation == 2 ) { playerOrientation = 1; }
			}
			
			if( imageLibrary.imageExists( "player"+aid[i] ) ) {
			
				playerImage = ImageLibrary.getSpriteFrame( imageLibrary.getImage( "player"+aid[i] ) , 0 , playerOrientation , 64 , 64 );
				g.drawImage( playerImage , adjustX + playerY * xTileSize , adjustY + playerX * yTileSize , xTileSize , yTileSize , null );
			
			} else {
				
				// Backup player drawing
				g.setColor( GUIConstants.playerColors[ aid[i] ] );
				g.fillOval( adjustX + playerY * xTileSize + xTileSize/4, adjustY + playerX * yTileSize + yTileSize/4 , xTileSize/2, yTileSize/2 );
				
			}
			
		}
		
		// It looks better if trees are scaled up slightly
		int overlaySizeX = (int)(xTileSize*1.4);
		int overlaySizeY = (int)(yTileSize*1.4);
		
		// Second pass, draw overlay components such as trees,etc
		for ( int i = 0; i < mazeLengthX; i++ ) {
					
			for ( int j = 0; j < mazeLengthY; j++ ) {
				
				if ( maze[i][j] != MazeConstants.WALL ) {
					continue;
				}
					
				if( imageLibrary.imageExists("tree1") ) {
				
					g.drawImage( imageLibrary.getImage("tree1") , adjustX + j * xTileSize - overlaySizeX/6 , adjustY + i * yTileSize - overlaySizeY/2 + 10 , overlaySizeX , overlaySizeY ,  null );
			
				}
				
			}
			
		}
		
		 
		// Draw player legend
		Font legendFont; 

		if( fontLibrary.fontExists("retro") ) {
			legendFont = fontLibrary.getFont("retro");
		} else {
			legendFont = FontLibrary.newFont("Arial", 18, false, false);
		}
 
		g.setFont( legendFont );
		g.setColor( Color.WHITE );
	 
		int playerFieldWidth = 200;
		int playerIconSize = 40;
		
		String[] statuses = playerStatuses.split("\\|");
		String[] pCoins = playerCoins.split("\\|");
		
		String name, coins = "";
		int activeCount = 0;
		for( int i = 0 ; i < playerCount; i++ ) {
 
		
			if( i == playerIndex ) {
				g.setColor( Color.green );
			} else {
				g.setColor( Color.white );
			}
			
			name = "Player " + (i+1);
			
			// Only draw the legend of the active players in the game. Use activeCount to keep track of the increments instead of i.
			if( statuses.length == playerCount ) {
				if( statuses[ i ].equals(".") ) {
					//g.setColor( Color.darkGray );
					continue;
				} else {
					name = statuses[ i ];
					if( name.length() > 10 ) {
						name = name.substring( 0 , 10 ) + "..";
					}
					activeCount++;
				}
				
			}
			
			
			if( pCoins.length == playerCount ) {
				if( pCoins[ i ].equals(".") ) {
					continue;
				}
				coins = pCoins[ i ];
			}
			  
			
			int legendX = adjustX + mazeLengthY * xTileSize + 20;
			g.drawString( name , legendX + 48 , 100 + 60 * activeCount + 32  );
		 
			g.drawImage( imageLibrary.getImage("coin") , legendX + 190, 100 + 60 * activeCount + 7 , playerIconSize , playerIconSize , null );
			g.drawString( coins , legendX + 235 , 100 + 60 * activeCount + 32  );
			
			
			if( imageLibrary.imageExists( "player"+i ) ) {
				
				playerImage = ImageLibrary.getSpriteFrame( imageLibrary.getImage( "player"+i ) , 0 , 2 , 64 , 64 );
				g.drawImage( playerImage , legendX , 100 + 60 * activeCount + 4 , playerIconSize , playerIconSize , null );
			
			} else {
				
				// Backup player drawing
				g.setColor( GUIConstants.playerColors[ i ] );
				g.fillOval( playerFieldWidth * i + panelWidth/2 - (playerFieldWidth*playerCount)/2 , panelHeight - GUIConstants.mazePaddingY + playerIconSize/4 , playerIconSize/2 , playerIconSize/2 );
				
			}
			
			 
			
		}
		
		// Draw error message
		g.setColor(Color.red);
		g.drawString( message , 100 , panelHeight - 30 );
		 
		if( STARTED == false && startCountdown >= 0 ) {
			
			g.setFont( startFont );
			g.setColor( startOverlay );
			
			g.fillRect( 0, 0, panelWidth, panelHeight );
			
			g.setColor(Color.white);
			
			fm = g.getFontMetrics();
			
			
			String txt1 = "GAME WILL START IN";
			String txt2 = "" + startCountdown;
			
			g.drawString( txt1, panelWidth/2 - fm.stringWidth(txt1)/2 , panelHeight/2 - 65 );
			g.drawString( txt2 , panelWidth/2 -  fm.stringWidth(txt2)/2 , panelHeight/2 );
			
			if( startCountdown == 0 ) {
				soundtrack();
			}
			
		}
		
		// Record the current system time AFTER a maze has been created
		long endTime = System.currentTimeMillis();
	
		System.out.println( "Game Render time = " + ( endTime - startTime ) + "ms" );
		 
	}
	 
	
	public static int randInt(int min, int max) {
 
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	/**
	 * Check if a String is contained in an array of Strings
	 * 
	 * @param arr The array of String
	 * @param key The String to search for
	 * @return true if String is found, false otherwise
	 */
	private static boolean inArray( String[] arr , String key ) {
		
		for( String c : arr ) {
			
			if( c.equals(key) ) {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	 

}
