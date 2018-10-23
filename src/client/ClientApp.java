package client;

import java.awt.EventQueue;
import java.awt.FontMetrics;

import javax.swing.JFrame;

import protocol.Protocol;

import java.awt.image.BufferedImage;
import java.lang.Runtime;

@SuppressWarnings("serial")
public class ClientApp extends JFrame {
	
	public Client client = new Client();
	public static ClientApp frame = null;
	
	// Game panels
	protected final IntroPanel intro;
	protected final AuthPanel auth;
	protected final MainPanel main;
	protected final QueuePanel queue;
	protected final GamePanel game;
	protected final LobbyPanel lobby;
	
	public final BufferedImage logo;
	
	private final Thread shutdownThread;
	
	protected String username;
	
	public static void main(String[] args)  {

		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					
					String host = "localhost";
					int port = 8000;
					
					if( args.length >= 2 ) {
						host = args[0];
						port = Integer.parseInt( args[1] );
					}
					
					frame = new ClientApp();
					frame.setVisible( true );
					frame.setResizable(false);
					frame.client.setHost( host );
					frame.client.setPort( port );
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		});
		
	}

	
	public ClientApp() {
		
		// Load the logo for the client once.. we don't need to load the same thing over and over
		logo = ImageLibrary.load( GUIConstants.menusPath + "logo.png" );
		
		// Register a custom font for this client to be used with the GUI
		FontLibrary.registerFont( GUIConstants.fontsPath + "VCD_OSD_MONO.ttf" );
		
		setSize( GUIConstants.screenWidth , GUIConstants.screenHeight );
		setTitle("Maze Race");
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// -----------------Add the different panels to the frame------------------
		
		// Introduction screen
		intro = new IntroPanel();
		add(intro);
		// Login/Registration
		auth = new AuthPanel( client );
		add(auth);
		// Main panel
		main = new MainPanel( client );
		add( main );
		// Queue 
		queue = new QueuePanel( client );
		add(queue);
		// Lobby
		lobby = new LobbyPanel( client );
		add(lobby);
		// Game instance (session)
		game = new GamePanel( client );
		add(game);
		
		
		// Set this as the first panel
		setIntro();
	 
		 
		// TODO Complete shutdown hook to make sure no open connections remain
		// NOTE! This does not cover force stopping the app through task manager
		shutdownThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				System.out.println("Shutdown thread executed");
				
				if( client != null ) {
				
					// Call any methods to close sockets and make sure connections don't stay open
					if( client.clientSocket != null ) {
						
						if( !client.clientSocket.isClosed() ) {
							
							if( client.outToServer != null ) {
								
								client.outToServer.println( Protocol.encode( Protocol.P_QUIT , "terminate" ) );
								client.outToServer.flush();
								
							}
							// If there is an open socket, send a server request to shutdown the client's socket
							client.stop();
						
						}
						
					}  
				
				}
			
			}
		
		});
		
		Runtime.getRuntime().addShutdownHook( shutdownThread );
		
		
	}
	
	protected void setIntro() {
		
		auth.setVisible(false);
		queue.setVisible(false);
		lobby.setVisible(false);
		game.setVisible(false);
		main.setVisible(false);
		
		setTitle("Maze Race");
		setContentPane(intro);
		intro.requestFocusInWindow();
		intro.setVisible(true);
		
		intro.addListener();
		
	}
	
	protected void setAuth() {
		
		intro.setVisible(false);
		queue.setVisible(false);
		lobby.setVisible(false);
		game.setVisible(false);
		main.setVisible(false);
		
		intro.removeKeyListener();
		auth.enableInput = true;
		auth.restoreLogin();
		auth.restoreRegister();
		
		setTitle("Authentication - login/register");
		setContentPane(auth);
		// DO NOT REMOVE THIS OR THE INPUT WILL BREAK
		auth.setFocusable(true);
		auth.requestFocusInWindow();
		auth.setVisible( true );
		
		client.stopPingThread();
		client.stopConnectionCheck();
		
		lobby.bReady.setEnabled(true);
		 
		
	}
	
	protected void setMain() {
		
		intro.setVisible(false);
		queue.setVisible(false);
		auth.setVisible(false);
		lobby.setVisible(false);
		game.setVisible(false);
		 
		
		setTitle("Main Menu");
		setContentPane(main);
		main.requestFocusInWindow();
		main.setVisible( true );
		// Make sure the client doesn't render the leaderboard when switching panels
		main.leaderboard = false;
		game.audioLibrary.stopSequence();
		game.audioLibrary.clearSequence();
		// At this point in the application start the server ping thread
		// This thread will be used for the queue,lobby and game session
		client.startPingThread();
		client.startConnectionCheck();
		
	}
	
	protected void setQueue() {
		
		intro.setVisible(false);
		lobby.setVisible(false);
		auth.setVisible(false);
		game.setVisible(false);
		main.setVisible(false);
		
		setTitle("Queue - waiting for 4 players to start a lobby");
		setContentPane(queue);
		queue.requestFocusInWindow();
		queue.setVisible(true);
		game.audioLibrary.stopSequence();
		game.audioLibrary.clearSequence(); 
		
	}
	
	protected void setLobby() {
		
		intro.setVisible(false);
		queue.setVisible(false);
		auth.setVisible(false);
		game.setVisible(false);
		main.setVisible(false);
		
		setTitle("Lobby - All players waiting to join a session");
		setContentPane(lobby);
		lobby.requestFocusInWindow();
		lobby.setVisible(true);
		
		game.timeLeft = -1;
		game.startCountdown = -1;
		game.collectedCoins = 0;
		game.audioLibrary.stopSequence();
		game.audioLibrary.clearSequence(); 
		
	}
	
	protected void setGame( String level ) {
		
		intro.setVisible(false);
		queue.setVisible(false);
		auth.setVisible(false);
		lobby.setVisible(false);
		main.setVisible(false);
		 
		// Load the components
		game.loadSounds();
		game.loadFonts();
		game.setTheme( level ); // also loads the textures if necessary
		
		setTitle("Game session");
		setContentPane( game );
		game.setVisible(true);
		game.addListener();
		game.requestFocusInWindow();
		
		
		 
	}
	
	protected static String buildStarLine( FontMetrics fm , int width , int max ) {
		
		String res = "";
		 
		while( fm.stringWidth( res ) < width && fm.stringWidth( res ) < max ) {
			
			res += '*';
			
		}
		
		return res;
		
	}

}
