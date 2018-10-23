package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.net.ConnectException;

import protocol.Protocol;

// updated 09 Mar 2018

public class Client {

	protected Socket clientSocket; // socket connecting to server
	protected PrintWriter outToServer; // output stream to server
	protected BufferedReader inFromServer; // input stream from server
								// latest message
	private String host = ""; // IP address of server
	private Integer port = 0; // Port number of server

	private Thread pingThread;
	private boolean pingThreadStarted = false;
	 
	protected boolean sent = false;
	protected long sentTime = System.currentTimeMillis();
	protected int sentFrom = 0;
	
	protected Timer t;
	
	ClientApp app = ClientApp.frame;
	
	protected void setSent( int from ) {
		sent = true;
		sentTime = System.currentTimeMillis();
		sentFrom = from;
	}
	
	/*
	 * Read a line from server and unpack it using SimpleProtocol
	 */
	protected String[] getResponse() {
		try {
			return Protocol.decode(inFromServer.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Send sign-up request to server, return the response to GuiSignUp
	 */
	protected String[] signup(String user, String pass) {
		String string = Protocol.encode( Protocol.P_SIGNUP , user, pass);
		outToServer.println(string);
		outToServer.flush();
		String[] response = this.getResponse();
		return response;

	}

	/*
	 * Send sign-in request to server, return the response to GuiSignIn
	 */
	protected String[] signin(String user, String pass) {
		
		String string = Protocol.encode( Protocol.P_LOGIN , user, pass);
	 
		outToServer.println(string);
		outToServer.flush();
	 
		String[] response = this.getResponse();
		if (response[1].equals("true")) {
		 
			System.out.println("Sign-in successful.");
			
		} else {
			
			System.out.println(response[2]);
			
		}
		return response;
		
	}
	
	protected void sendReq( String data ) {
		
		outToServer.println( data );
		outToServer.flush();
		
	}
	
	/**
	 * Move the player that just logged in to the queue
	 */
	protected void moveToQueue() {
		
		setSent( 1 );
		
		String string = Protocol.encode( Protocol.P_MOVE_TO_QUEUE );
		
		sendReq( string );
		
		 
		
	}
	
	protected void leaveQueue() {
		
		setSent( 2 );
		
		String string = Protocol.encode( Protocol.P_LEAVE_QUEUE );
		
		sendReq( string );
		
	}
	
 
	/**
	 * Send a ready state for this client to the server
	 * Called when the client is in a lobby waiting to start a game and presses the "READY" button
	 */
	protected void setready() {
		
		setSent( 2 );
		
		String string = Protocol.encode( Protocol.P_LOBBY_READY );
		
		sendReq( string );
	
		
	}
	
	/**
	 * Get the player's stats and add them to the leaderboard panel
	 */
	protected void requestStats() {
		
		setSent( 2 );
		
		String string = Protocol.encode( Protocol.P_GET_STATS );
		
		sendReq( string );
		
	}
	
	/**
	 * Get the top scores on the server and add them to the leaderboard panel
	 */
	protected void requestTopScores() {
		
		setSent( 2 );
		
		String string = Protocol.encode( Protocol.P_GET_TOPSCORES );
		
		sendReq( string );
		
	}
	
	/**
	 * Leave a lobby and go back to the main menu
	 */
	protected void leavelobby() {
		
		setSent( 2 );
		
		//String string = Protocol.encode( Protocol.P_RETURN_TO_QUEUE );
		String string = Protocol.encode( Protocol.P_RETURN_TO_MAIN );
		
		sendReq( string );
		
	}
	
	/**
	 * Attempt to move the player in a certain direction (while in-game)
	 * 
	 * @param dir The direction in which to move (0 - up, 1 - down, 2 - left, 3 - right)
	 */
	protected void move( int dir ) {
		
		if( dir < 0 || dir > 4 ) { return; }
		
		setSent( 3 );
		
		String string = Protocol.encode( Protocol.P_MOVE , ""+dir );
		
		sendReq( string );
		
	}
	
	/**
	 * Place a projectile on the map
	 */
	protected void fire() {
		
		if( app.game.firesLeft < 1 ) { return; }
		
		setSent(3);
		
		String string = Protocol.encode( Protocol.P_FIRE );
		
		sendReq( string );
		
	}
	
	protected void stopConnectionCheck() {
		
		if( t == null ) { return; }
		
		t.cancel();
		
		t = null;
		
	}
	 
	protected void startConnectionCheck() {
		
		if( t != null ) { return; }
		
		t = new Timer();
		
		t.scheduleAtFixedRate( new TimerTask() {
			
			public void run() {
			
				checkConnection();
				
			}
			
		}, 0, 50);
		
	}
	
	protected void checkConnection() {
		
		 //System.out.println(GUIConstants.sent );
		 if( sent ) {  
			  
			 if( System.currentTimeMillis() - sentTime > 2000 ) {
				 
				 // At this point we assume there is a potential problem with the server, but it is not certain yet
				 sent = false;
				 app.auth.setMessage("NO CONNECTION TO SERVER. YOU HAVE BEEN LOGGED OUT.");
				 app.setAuth();
				 // Therefore we send a dummy request and wait for a response. If nothing comes, then the connection is broken.
				  try {
					 
					 outToServer.println( Protocol.encode( Protocol.P_CONNECTION_TEST ) );
					 outToServer.flush();
					 inFromServer.readLine();
					 
				 } catch ( IOException e ) {
					   
					 e.printStackTrace();
					 
				 }
		 
				 
			 }
		 
		 }
		
	}
	
	protected void stopPingThread() {
		
		if( pingThread != null ) {
			
			pingThread.interrupt();
			
		}
		pingThreadStarted = false;
		pingThread = null;
		
	}
	
	protected void startPingThread() {
		
		// We do not want to initialize the thread multiple times
		// This is useful when going back and forth between queue, lobby and game
		if( pingThread != null ) { return; }
		
		pingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				 
				// Start a check on the queue for any server messages
				pingServer();
				
			
			}
		
		});
		pingThreadStarted = true;
		pingThread.start();
		
	}
	
	protected void pingServer() {
		 
		// This loop needs to be interrupted if the connection to the server is lost and the player is thrown back to the auth panel
		while( !clientSocket.isClosed() && pingThreadStarted == true ) {
	 
			try {
				
				String in = inFromServer.readLine();
				
				if( in == null ) {
					continue;
				}
				
				String[] decoded = Protocol.decode( in );
				 
				if( decoded.length < 1 ) {
					continue;
				}
				
				// When a response is received, set the sent flag to false
				sent = false;
				
				String channel = decoded[ 0 ];
				
				if( channel.equals( Protocol.P_CONNECTION_TEST ) ) {
					
					sendReq( Protocol.encode( Protocol.P_CONNECTION_TEST ) );
					
				}
				
				// The player has been moved to the queue. Therefore switch panels from main to queue
				if( channel.equals( Protocol.P_MOVE_TO_QUEUE ) ) {
					
					app.setQueue();
					
				} else if( channel.equals( Protocol.P_QUEUE_UPDATE ) ) {
					   
					app.queue.queueData = decoded[ 1 ];
					app.queue.repaint();
					
				} else if( channel.equals( Protocol.P_SESSION_START ) ) {
					// Go to the lobby JPanel
					app.lobby.timerData = "WELCOME TO THE LOBBY.";
					app.lobby.playerData = decoded[ 1 ];
					
					app.lobby.levelTheme = decoded[2];
					app.lobby.mazeSize = decoded[3];
					app.lobby.timeLimit = decoded[4];
					app.lobby.coinCount = decoded[5];
					 
					app.lobby.loadHeader();
					
					app.setLobby();
					app.queue.repaint();
					
				} else if ( channel.equals( Protocol.P_LOBBY_READY ) ) {
				
					app.lobby.timerData = "PLAYER ["+decoded[2]+"] IS READY.";
					app.lobby.playerData = decoded[ 1 ];
					
					app.lobby.repaint();
					
				} else if ( channel.equals( Protocol.P_LOBBY_COUNTDOWN_RUNNING ) ) {
					 
					app.lobby.timerData = decoded[ 1 ];
					// This channel is ALSO used when a player disconnects from the lobby. The protocol sends an additional message
					if( decoded.length > 2 ) {
						app.lobby.playerData = decoded[ 2 ];
					}
					app.lobby.repaint();					 
					
				} else if( channel.equals( Protocol.P_RETURN_TO_MAIN ) ) {
					
					app.lobby.bReady.setEnabled(true);
					if( decoded.length > 1 ) {
						app.main.message = decoded[1];
					}
					app.setMain();
					app.main.repaint();
					
				} else if ( channel.equals( Protocol.P_GAME_START ) ) {
					
					int pIndex = Integer.parseInt( decoded[ 3 ] );
					int[][] maze = convertTo2DArray( decoded[ 5 ] , '|' , ',' );
					int[][] coins = convertTo2DArray( decoded[ 6 ] , '|' , ',' );
					
					app.game.playerStatuses = decoded[ 7 ];
					app.game.playerCoins = decoded[8];
					 
					 
					app.setGame( decoded[ 1 ] );
					
					app.game.playerIndex = pIndex;
					app.game.setPlayerPositions( decoded[ 4 ] , '|' );
					app.game.setData( maze , coins );
					
					app.game.STARTED = false;
					app.game.FINISHED = false;
					
					//app.game.repaint();
					
				} else if( channel.equals( Protocol.P_GAME_START_COUNTDOWN ) ) {
					
					app.game.returnCountdown = -1;
					
					// The countdown has finished and the game can start
					if( decoded[1].equals("true") ) {
						
						app.game.STARTED = true;
						app.game.startCountdown = -1;
						app.game.firesLeft = 1;
						 
					} else {
						
						app.game.startCountdown = Integer.parseInt( decoded[2] );
						
					}
					
					app.game.repaint();
					
				} else if( channel.equals( Protocol.P_GAME_COUNTDOWN_RUNNING ) ) {
					 
					int timeLeft = Integer.parseInt(decoded[1]);
					
					app.game.timeLeft = timeLeft;
					if( timeLeft <= 0 ) {
						
					}
					
					app.game.repaint();
					
				}  else if( channel.equals( Protocol.P_GAME_COUNTDOWN_RESULT ) ) {
					 
					app.game.FINISHED = true;
					app.game.firesLeft = 0;
					app.game.decodeScores( decoded[1] , '|' );
					app.game.repaint();
					
				} else if ( channel.equals( Protocol.P_GAME_RETURN_COUNTDOWN ) ) {
					
					int returnCountdown = Integer.parseInt( decoded[1] );
					app.game.returnCountdown = returnCountdown;
					app.game.repaint();
					 
				} else if( channel.equals( Protocol.P_MOVE ) ) {
					 
					int[][] maze = convertTo2DArray( decoded[ 4 ] , '|' , ',' );
					int[][] coins = convertTo2DArray( decoded[ 5 ] , '|' , ',' );
					
					app.game.playerStatuses = decoded[ 6 ];
					app.game.playerOrientations = decoded[ 7 ];
					
					app.game.trapsList = decoded[9];
					
					int firesLeft = Integer.parseInt(decoded[10]);
					if( app.game.firesLeft > firesLeft ) {
						app.game.audioLibrary.play("fire");
					}
					app.game.firesLeft = firesLeft;
					
					app.game.setPlayerPositions( decoded[ 3 ] , '|' );
					app.game.setData( maze , coins );
					
					if( decoded[1].equals( app.username ) ) {
						app.game.audioLibrary.play("move");
					}
					
					if( decoded.length >= 12 ) {
						app.game.message = decoded[11];
						if( decoded[11].length() > 5 ) {
							if( decoded[11].substring(0,5).equals("[HIT]") ) {
								app.game.audioLibrary.play("hit");
							}
						}
					}
					
					app.game.playerCoins = decoded[ 2 ];

					int newCoins = Integer.parseInt( decoded[ 8 ] );
					// Collect coin sound
					if( app.game.collectedCoins < newCoins ) {
						app.game.audioLibrary.play("coin");
					}
					app.game.collectedCoins = newCoins;
					 
					
				} else if ( channel.equals( Protocol.P_PLAYER_FINISHED ) ) {
					
					// You just finished
					if( decoded[1].equals( app.username ) ) {
						
						app.game.FINISHED = true;
						app.game.audioLibrary.play("finish");
						
					}
					
					app.game.decodeScores( decoded[2] , '|' );
					app.game.repaint();
					
				}  else if ( channel.equals( Protocol.P_GET_STATS ) ) {
					
					app.main.playerStats = decoded[1];
					app.main.playerLast = decoded[2];
					
					app.main.repaint();
					
				}  else if ( channel.equals( Protocol.P_GET_TOPSCORES ) ) {
					
					if( !decoded[1].equals("none") ) {
						app.main.mostCoins = decoded[1];
					}
					
					if( !decoded[2].equals("none") ) {
						app.main.mostPoints = decoded[2];
					}
					
					if( !decoded[3].equals("none") ) {
						app.main.mostWins = decoded[3];
					}
					
					if( !decoded[4].equals("none") ) {
						app.main.bestWLRatio = decoded[4];
					}
					
					app.main.repaint();
					
				}
			
			} catch ( IOException e ) {
				
				e.printStackTrace();
				stop();
				app.setAuth();
				app.auth.setMessage("NO CONNECTION TO SERVER.");
				
				
			}
			
		}
		
	}
	
	private static int[][] convertTo2DArray( String data , char rowDelimiter , char colDelimiter ) {
		
		String[] rows = data.split("\\"+rowDelimiter);
		String[] cols = rows[0].split("\\"+colDelimiter);
		
		int[][] result = new int[ rows.length ][ cols.length ];
		
		for( int i = 0 ; i < rows.length ; i ++ ) {
			
			cols = rows[ i ].split("\\"+colDelimiter);
			
			for( int j = 0 ; j < cols.length; j++ ) {
				
				result[ i ][ j ] = Integer.parseInt( cols[ j ] );
				
			}
			
		}
		
		return result;
		
	}
	
	/*
	 * Initialise socket and input/output streams
	 */
	public void start() {
		
		try {
			
			app = ClientApp.frame;
			clientSocket = new Socket(this.host, this.port);
			outToServer = new PrintWriter(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
		} catch ( UnknownHostException e ) {
			
			app.auth.setMessage( "UNKNOWN HOST." );
			app.auth.restoreRegister();
			app.auth.restoreLogin();
			e.printStackTrace();
			
		} catch ( ConnectException e ) {
			
			app.auth.setMessage( "NO CONNECTION TO SERVER." );
			app.auth.restoreRegister();
			app.auth.restoreLogin();
			e.printStackTrace();
			
		} catch ( IOException e ) {
			
			app.auth.setMessage( "IO ERROR." );
			app.auth.restoreRegister();
			app.auth.restoreLogin();
			e.printStackTrace();
			
		}
	}

	/*
	 * Close socket
	 */
	public void stop() {
		
		try {
			
			clientSocket.close();
			
		} catch ( IOException e ) {
			
			e.printStackTrace();
			
		}
		
	}

	public void setHost( String host ) {
		
		this.host = host;
		
	}

	public void setPort( Integer port ) {
		
		this.port = port;
		
	}

}