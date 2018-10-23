package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import protocol.Protocol;
import server.mazegeneration.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
 

/**
 * 
 * @author Iliya Liksov
 *
 */
public class Server {
	
	protected Database db;
	private ExecutorService pool;
	 
	protected List<Session> sessions = Collections.synchronizedList( new ArrayList<Session>() );
	protected List<Player> queue = Collections.synchronizedList( new ArrayList<Player>() );
	protected List<Player> active = Collections.synchronizedList( new ArrayList<Player>() );
	
	private int port = -1;
	private String address = "";
	private ServerSocket serverSocket = null;
	private Thread shutdownThread;
	
	// Available themes for the mazes (The Duplicates are not a mistake, they increase the probability of a level being picked)
	private static final String[] mazeThemes = new String[] {
		"Gardens",
		"Forest",
		"Desert",
		"Dungeon",
		"Gardens",
		"Underworld",
		"Winter",
		"Gardens"
	};
	
	/**
	 * Start the server as soon as this class is executed
	 * By default if no port is supplied, the server will start on port 8000
	 * 
	 * @param args The first element is the server port.
	 */
	public static void main(String[] args) {
		
		int defaultPort = 8000;
		
		int serverPort = ( args.length > 0 && Integer.parseInt( args[0] ) > 0 ) ? Integer.parseInt( args[0] ) : defaultPort;
		 
		Server server = new Server( serverPort );
	 
		
	}
	
	/**
	 * Constructor for Server
	 * 
	 * @param serverPort The port to start the server on
	 */
	public Server( int serverPort ) {

		try {
			
			this.port = serverPort;
			this.address = InetAddress.getLocalHost().getHostAddress();
			
			// Create a server socket on the specified port 
			// Use default value of backlog by setting it to 0
			serverSocket = new ServerSocket( port );
			
			System.out.println( "Server started - " + address + ":" + port );
			
			// Create the database connection (first row is my local db)
			db = new Database( "localhost" , "mazerace" , "postgres" , "admin" );
		 
			// Logout all users on server start (hack to avoid the circumvention of the shutdown hook)
			if( db != null ) {
				
				db.logoutAll();
				
			}
			
			// Create thread pool
			pool = Executors.newCachedThreadPool();
			 
			shutdownThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					System.out.println("Shutdown thread executed");
					  
				
				}
			
			});
			
			Runtime.getRuntime().addShutdownHook( shutdownThread );
			
			// Heartbeat sensor
			Timer t = new Timer();
			t.scheduleAtFixedRate( new TimerTask() {
				
				public void run() {
					
					connectionTest();
					 				
				}
				
			}, 0, 2000);
			
			
			// Wait for connections
			while( true ) {
				
				// Got one - accept socket
				Socket socket = serverSocket.accept();
				System.out.println("Connection accepted - " + socket);
				
				pool.execute( new ClientHandler( this , socket ) );
				
				
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Remove a player waiting in the queue. 
	 * The client in question will be prompted to return to the main panel
	 * The remaining clients in the queue will be notified of the change
	 * 
	 * @param player The player object
	 */
	protected synchronized void leaveQueue( Player player ) {
		  
		queue.remove( player );
		
		String output = Protocol.encode( Protocol.P_RETURN_TO_MAIN );
		
		player.toClient.println( output );
		player.toClient.flush();
		
		updateQueueList();
		
	}
	
	/**
	 * Test the connection between server and client. 
	 * Used in conjunction with the server's TimerTask.
	 * A Blank request is sent to the client.
	 * If no response is returned, the client is considered to be disconnected.
	 * Therefore, log them out and close the sockets.
	 */
	protected synchronized void connectionTest() {
		
		int numActive = active.size();	
		if( numActive == 0 ) { return; }
		
		Player p;
		int ai = 0; // Adjusted index to make sure there are no out of bounds exceptions when removing from active list
		for( int i = 0 ; i < numActive; i++ ) {
			
			p = active.get( ai );
			
			if( p.socket == null ) { continue; }
			
			try {
				
				p.toClient.println( Protocol.encode( Protocol.P_CONNECTION_TEST ) );
				p.toClient.flush();
				p.fromClient.readLine();
				
				ai++;
				
			} catch ( IOException e ) {
				 
				// No connection to client
				db.logout( p.username );
				active.remove( p );
				removePlayer( p );
				
				try {
					 
					p.socket.close();
					p.thread.stop();
					System.out.println("PLAYER ["+p.username+"] DISCONNECTED.");
					
				} catch ( IOException e2 ) {
					e.printStackTrace();
				}
				
			}
			
		}
		
		 
		
	}
	
	protected synchronized void getPlayerStats( Player player ) {
		
		String[] result = db.getUserData( player.username );
		
		if( result.length < 1 ) { return; }
		
		String last = result.length >= 2 ? result[1] : "null";
		
		String output = Protocol.encode( Protocol.P_GET_STATS , result[0] , last );
		player.toClient.println( output );
		player.toClient.flush();
		
	}
	
	protected synchronized void getTopScores( Player player ) {
		
		String mostCoins = db.getTopScores( 0 );
		String mostPoints = db.getTopScores( 1 );
		String mostWins = db.getTopScores( 2 );
		String bestWLRatio = db.getTopScores( 3 );
		
		String[] args = new String[ 5 ];
		args[ 0 ] = Protocol.P_GET_TOPSCORES;
		args[ 1 ] = mostCoins == null ? "none" : mostCoins;
		args[ 2 ] = mostPoints == null ? "none" : mostPoints;
		args[ 3 ] = mostWins == null ? "none" : mostWins;
		args[ 4 ] = bestWLRatio == null ? "none" : bestWLRatio;
		
		String output = Protocol.encode( args );
		player.toClient.println( output );
		player.toClient.flush();
		
		System.out.println(mostCoins);
		System.out.println(mostPoints);
		System.out.println(mostWins);
		System.out.println(bestWLRatio);
		
		
	}
	
	/**
	 * Calculate the points for a player based on an arbitrary formula
	 * 
	 * @param completionTime The time it took the player to complete the maze
	 * @param timeLimit The maximum time allocated for the maze
	 * @param coinCount The number of coins the player collected
	 * @param maxCoins The total amount of coins the maze had 
	 * @return The points for the player
	 */
	protected int calculatePoints( int completionTime , int timeLimit , int coinCount , int maxCoins ) {
		 
		double tf, cf;
	 
		tf = (double) (timeLimit - completionTime) / timeLimit;

	    cf = (double) coinCount / maxCoins; // On average you get about 20 coins.
		 
		// The completion time matters the most, then the coins. The ratio is 2:1
		int result = (int) (tf * 100 + cf * 50);
		
		return result;
		
	}
	
	/**
	 * Add a player to the queue arrayList.
	 * Then notify all clients in the queue of the new player (this includes the player itself)
	 * Finally attempt to create a new lobby.
	 * If the number of people in the queue is enough, a lobby will be created and the players will be thrown to a new session.
	 * If not, nothing happens.
	 * 
	 * @param player
	 */
	protected synchronized void addPlayerToQueue( Player player ) {
		
		queue.add( player );
	
		// Send back the message that the user has logged in so that they can switch panels
		String output = Protocol.encode( Protocol.P_MOVE_TO_QUEUE );
		player.toClient.println( output );
		player.toClient.flush();
		
		// Notify all clients in the queue of any changes to the player list 
		updateQueueList();
		
		// Attempt to create a lobby.
		createLobby();
		
		
	}
	 
	/**
	 * Notify all clients currently waiting in the queue of something.
	 * Use the protocol's encode as the parameter.
	 * 
	 * @param notify The message you want to send to the clients
	 */
	public synchronized void notifyQueue( String notify ) {
		
		if( notify == null ) { return; }
		
		// When a player joins, send a message to all clients in the queue to update their view
		for( int i = 0 ; i < queue.size() ; i++ ) {
			
			Player player = queue.get( i );
			
			player.toClient.println( notify );
			player.toClient.flush();
			
		}
		
	}
	
 
	
	/**
	 * Create a new session on the server.
	 * This is where the queue players will be added so they can play together.
	 * A session contains a list of player object references and many other variables.
	 * Check the Session class for more information.
	 * 
	 * The new session is added to the sessions arrayList.
	 * Players are added to the session from the queue.
	 * Once a player has been added, they are removed from the queue.
	 * 
	 * @return The new Session object (reference to).
	 */
	public synchronized Session createSession() {
		
		// Create a new session
		Session session = new Session( this );
		// The player id in the players session array
		int pid = 0;
		
		int maxPlayers = queue.size() > Constraints.minPlayersRequired ? Constraints.minPlayersRequired : queue.size();
		
		// Get the 4 players in the queue and create their threads + create a session for them
		for( int i = 0 ; i < maxPlayers ; i++ ) {
			
			// Get the player object from the queue
			Player player = queue.get( 0 );
			// Set the player's position in the session's players list
			player.sessionPosition = pid;
			player.session = session;
			// Set the additional variables to their default state
			setPlayerDefaults( player );
			
			// Add player to session
			session.addPlayer( player , pid );
			// Remove them from the queue
			queue.remove( 0 );
			
			pid++;
			
		}
		
		// Add session (finalize)
		sessions.add( session );
		
		return session;
		
	}
	
	/**
	 * Set the properties of this player to their default state.
	 * This method is used when creating a new session.
	 * 
	 * @param player The Player object
	 */
	protected void setPlayerDefaults( Player player ) {
		
		player.ready = false;
		player.coins = 0;
		player.points = 0;
		player.x = -1;
		player.y = -1;
		player.orientation = 0;
		player.finished = false;
		player.sessionTime = 0;
		player.endTime = 0;
		player.startTime = 0;
		player.firesLeft = 1;
		
	}
	
	 
	
	/**
	 * Create a lobby by polling the queue list.
	 * If there are enough players in the queue, start a lobby and create a session
	 * Then transfer all the players from the queue to the lobby
	 * After the session is created, send the response to all clients in the session so that the gui can switch panels
	 * 
	 * @return true if successful , false otherwise
	 */
	public synchronized boolean createLobby() {
		
		// Not enough people in the queue to make a lobby
		if( queue.size() < Constraints.minPlayersRequired ) { return false; }
		 
		/* 
		 * Create a session with the 4 players in the queue
		 * Automatically removes them from the queue
		 * createSession sets the session reference to the player object internally
		 * This way you avoid having the first 3 players with no session references
		*/
		Session sess = createSession(); // returns reference to session object
		 
		// Set the player's session object reference
		String sessStartMsg = Protocol.encode( Protocol.P_SESSION_START , lobbyPlayerStatusesToString( sess  , '|' ) , sess.levelTheme , ""+sess.maze.getMazeSize() , ""+sess.timeLimit , ""+sess.maze.countCoins() );
		notifySessionPlayers( sess , sessStartMsg );
		 
		return true;
		
	}
	
	/**
	 * Construct a string of the statuses of all the players in a lobby.
	 * Format: username,true|username,false|...
	 * 
	 * @param sess The session object
	 * @param delimiter The delimiter used for each player (default is | )
	 * @return The string representation of the player statuses
	 */
	public synchronized String lobbyPlayerStatusesToString( Session sess , char delimiter ) {
		
		String res="";
		
		int pLen = sess.players.length;
		
		Player p;
		for( int i = 0 ; i < pLen ; i++ ) {
			
			p = sess.players[ i ];
			
			if( p == null ) { continue; }
			
			res += p.username + "," + String.valueOf( p.ready );
			
			if( i < pLen - 1 ) {
				res += delimiter;
			}
		
		}
		
		return res;
		
	}
	
	/**
	 * Generate a list of players in the queue and send it to all clients that are in the queue panel waiting
	 */
	public synchronized void updateQueueList() {
		
		// Create a string to represent the current queue as a list of players
		String queueList = generateQueueList();
		String notify = Protocol.encode( Protocol.P_QUEUE_UPDATE , queueList );
		// Notify all players in the queue of the newly joined player (including himself)
		notifyQueue( notify );
		
	}
	
	/**
	 * Count the number of players in a session who have clicked "READY".
	 * 
	 * @param sess The session object
	 * @return The number of ready players
	 */
	public synchronized int countReadyPlayers( Session sess ) {
		
		if( sess == null || sess.players == null ) { return 0; }
		
		int count = 0;
		Player p;
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			// If the player is null (could occur if the client disconnects mid-game)
			if( p == null ) { continue; }
			
			if( p.ready ) {
				count++;
			}
			
		}
		
		return count;
		
	}
	
	/**
	 * Count the number of players in a session.
	 * This is irrespective of what their status in the session is.
	 * If they are active, they are counted.
	 * 
	 * @param sess The session object
	 * @return The number of players in the session.
	 */
	public synchronized int countSessionPlayers( Session sess ) {
		
		if( sess == null || sess.players == null ) { return 0; }
		
		int count = 0;
		Player p;
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			// If the player is null (could occur if the client disconnects mid-game)
			if( p == null ) { continue; }
			
			count++;
		 
		}
		
		return count;
		
	}
	
	/**
	 * Global method used to remove a player from wherever they are on the server.
	 * If the player is in the queue, remove them and notify the list of clients in the queue of the change.
	 * 
	 * If the player is not in a queue, they are probably in a session (but not necessarily, they could be chillin' at the main menu)
	 * 
	 * @param player The player object
	 */
	public synchronized void removePlayer( Player player ) {
		
		if( player == null ) { return; }
		

		// Find the username in the queue (if it is there)
		// DO THIS BEFORE REMOVING THE PLAYER FROM THE QUEUE
		int pQueueID = playerQueueID( player.username );
		
		// IF the player is actually in a queue
		if( pQueueID > -1 ) {
			
			queue.remove( player );
			updateQueueList();
		 
		}
		// Otherwise they are probably in a lobby/game session
		else {
			 
			removePlayerFromSession( player );
		 
			
		}
		
	}
	
	/**
	 * Remove a player from a session. 
	 * This method is used primarily by the shutdown hook an
	 * 
	 * @param player The player object
	 */
	public synchronized void removePlayerFromSession( Player player ) {
		
		removePlayerFromSession( player , false );
		
	}
	
	public synchronized void removePlayerFromSession( Player player , boolean returnToMain ) {
		
		if( player.session == null ) { return; }
		
		// Remove the player reference from the session
		player.session.players[ player.sessionPosition ] = null;
		
		// How many active players are in this lobby (this includes the actual game session)
		int lobbyPlayers = countSessionPlayers( player.session );
		
		
		// If there is a game running, remove this player from the game
		if( player.session.gameRunning ) {
			
			 // If there aren't enough players to continue playing.
			 if( lobbyPlayers < Constraints.minReadyPlayersRequired ) {
				 
				 player.session.gameFinished = true;
				 player.session.playersCanMove = false;
				 
				 destroyLobby( player.session );
				 return;
				 
			 } else { // Otherwise there are enough players left to continue playing.
				 
				 
				 
			 }
			 
			  
			
		} 
		// Otherwise the player is in the lobby
		else {
			
			// Get the number of players in the lobby who have pressed the ready button (before the game has started)
			int readyPlayers = countReadyPlayers( player.session );
			 
			
			// If there are still enough players in the lobby to play a game, only reset the countdown timer if it is running
			if( lobbyPlayers >= Constraints.minReadyPlayersRequired ) {
				
				// If a countdown has started
				if( readyPlayers < Constraints.minReadyPlayersRequired && 
					player.session.readyCountdownStarted == true
				) {
					
					// Reset the timer 
					player.session.readyCountdown.countdown = 1;
					player.session.readyCountdown.stop();
					player.session.readyCountdown = new SessionTimer( this , player.session , Constraints.lobbyCountdownTime , 0 );
					player.session.readyCountdownStarted = false;
				 
				
				}
	 
				// Notify session players of the changes
				notifySessionPlayers( player.session , Protocol.encode( Protocol.P_LOBBY_COUNTDOWN_RUNNING , "PLAYER ["+player.username+"] HAS LEFT THE LOBBY." , lobbyPlayerStatusesToString( player.session  , '|' ) ) );
				
				if( returnToMain == true ) {
					// Temporarily bring back the player reference in the session (returnPlayerToQueue removes it)
					player.session.players[ player.sessionPosition ] = player;
					 
					returnPlayerToMain( player.session , player.sessionPosition , "You have chosen to leave the game session." );
					 
					 
				}
				
			} else {
				
				Session tempSess = player.session;
				
				if( returnToMain == true ) {
					// Temporarily bring back the player reference in the session (returnPlayerToQueue removes it)
					player.session.players[ player.sessionPosition ] = player;
					 
					returnPlayerToMain( player.session , player.sessionPosition , "You have chosen to leave the game session." );
					
				}
				
				// Otherwise there aren't enough players in this lobby => Destroy the lobby (session)
				destroyLobby( tempSess );
				 
		 
				
			}
			
			 
			  
		}
		
		 
		 
		
	}
 
	
	/**
	 * Return all players in a session to the main menu
	 * The reason parameter is a message which will be displayed at the main menu screen.
	 * You don't need to use any protocol for that message, it is already integrated
	 * 
	 * @param sess The session object
	 * @param reason The reason for returning them.
	 */
	public synchronized void returnPlayersToMain( Session sess , String reason ) {
		
		if( sess == null || sess.players == null ) {
			return;
		}
		
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			returnPlayerToMain( sess , i , reason );
			
		}
		
	}
	
	/**
	 * Retrun a player from a session back to the main menu.
	 * The player is taken out of the session.
	 * A response is sent back to the client to prompt a panel switch.
	 * The reason parameter is a message which will be displayed at the top of the client's panel.
	 * 
	 * @param sess The session object
	 * @param i The index of the player in the session.players array
	 * @param reason The reason for removing them.
	 */
	public synchronized void returnPlayerToMain( Session sess , int i , String reason ) {
		
		if( sess == null || sess.players == null || i < 0 || i >= sess.players.length ) {
			return;
		}
		
		if( sess.players[i] == null ) {
			return;
		}
		 
		// Add player to queue
		Player p;
		 
		p = sess.players[i];
		 
		// Send the return to queue response to the client
		String message = Protocol.encode( Protocol.P_RETURN_TO_MAIN , reason );
		p.toClient.println( message );
		p.toClient.flush();
		// Remove the reference to player in the session
		sess.players[i] = null;
		p.session = null;
	  
	}
	
	/**
	 * Destroy a lobby (session).
	 * 
	 * Throw all the players back into the queue.
	 * 
	 * @param sess
	 */
	public synchronized void destroyLobby( Session sess , String reason ) {
		
		if( sess == null || sess.players == null ) { return; }
		
		int numPlayers = countSessionPlayers( sess );
		
		if( sess.gameStartCountdown != null ) { sess.gameStartCountdown.stop(); }
		if( sess.gameFinishCountdown != null ) { sess.gameFinishCountdown.stop(); }
		if( sess.returnCountdown != null ) { sess.returnCountdown.stop(); }
		if( sess.readyCountdown != null ) { sess.readyCountdown.stop(); }
		
		if( numPlayers == 0 ) {
			
			sessions.remove( sess );
			return;
			
		}
		 
		returnPlayersToMain( sess , reason );
		sessions.remove( sess );
		return;
		
	}
	
	public synchronized void destroyLobby( Session sess ) {
		
		destroyLobby( sess , "Session canceled. There aren't enough players left in the lobby." );
		
	}
	
	/**
	 * Method called by SessionTimer when the countdown reaches 0 to redirect the players in the session.
	 * If a player has pressed ready, as long as there are at least 2 players who have selected ready,
	 * They will be send to a game instance.
	 * 
	 * If a player is not ready and the countdown runs out, they will be thrown back into the lobby, 
	 * where they will wait for other players to join a different session.
	 * 
	 * @param sess The session object reference
	 */
	public synchronized void redirectSessionPlayers( Session sess ) {
		
		if( sess == null || sess.players == null ) { return; }
		
		String message;
		Player p;
		
		// First we need to count how many players are active and how many are ready/not ready
		int readyCount = countReadyPlayers( sess );
		 
		setInitialPlayerPositions( sess );
		
		int cReady = 0;
		
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			// If the player is null (could occur if the client disconnects mid-game)
			if( p == null ) { continue; }
			
			if( p.ready ) { cReady++; }
			
			// If this player is ready and there are at least a total of 2 ready players, send the ready players to the game
			if( p.ready && readyCount >= Constraints.minReadyPlayersRequired ) {
				 
				
				// Update the database to increase the number of started games for this player
				int statsUpdated = db.updateStats( p.username , "gamesPlayed" , 1 );
				
				String[] args = constructGameStartArguments( sess , p );
				
				message = Protocol.encode( args );
				
				// Send the response to all clients
				p.toClient.println( message );
				p.toClient.flush();
				
				// If this is the last ready player, start the game timer.
				if( cReady >= readyCount ) {
					
					sess.gameStartCountdownStarted = true;
					sess.gameStartCountdown.start();
					
				}
				
			} 
			// The other players who are not ready get sent back to the queue to wait for another lobby to join
			else {
				
				returnPlayerToMain( sess , i , "You didn't press ready." );
		 
				
			}
			
			 
			
		}
		
	}
	
	/**
	 * Once all players enter a game, set their current time in milliseconds to match. 
	 * 
	 * @param sess the session object
	 * @param time The time in milliseconds - use System.currentTimeInMillis();
	 */
	public void setPlayersStartTime( Session sess , long time ) {
		
		if( sess == null || sess.players == null ) { return; }
		 
		Player p;
		for( int i = 0 ; i < sess.players.length; i++ ) {

			p = sess.players[ i ];
			
			if( p == null ) { continue; }
			
			p.startTime = time;
			
			
		}
		
	}
	
	public void setInitialPlayerPositions( Session sess ) {
		
		if( sess == null || sess.players == null ) { return; }
		
		Player p;
		for( int i = 0 ; i < sess.players.length; i++ ) {

			p = sess.players[ i ];
			
			if( p == null ) { continue; }
			
			int[] pPos = mazeStartingPoint( sess.maze.getMazeSize() , i );
			
			p.x = pPos[0];
			p.y = pPos[1];
			
			if( i >= 0 && i < 2 ) { 
				p.orientation = 2;
			}
		
		}
		
	}
		
	public String[] constructGameStartArguments( Session sess , Player p ) {
		
		/* Construct initial game message
		 * format: 
		 * 0 - header , 1 - level theme , 2 - countdown timer 
		 * 3- slotID , 4 - player1 starting pos (adjusted according to their view) , 5- player1 maze[][] , 6 - player1 coins[][]
		 */
		String[] args = new String[ 9 ];
		
		args[ 0 ] = Protocol.P_GAME_START;
		args[ 1 ] = sess.levelTheme;
		args[ 2 ] = Integer.toString( sess.timeLimit );
		args[ 3 ] = Integer.toString( p.sessionPosition );
		 
		// Add player data
		int aid = 4;
 
		
		int boundaries[] = sess.maze.getCroppedBoundaries( p.x , p.y , sess.visibleRows , sess.visibleCols );
		int[][] croppedMaze = sess.maze.getCroppedMaze( boundaries );
		boolean[][] croppedCoins = sess.maze.getCroppedCoins( boundaries );
		
		// Player's relative position on screen based on their viewport
		//int[] adjustedPos = sess.maze.getAdjustedPlayerPosition( pPos[ 0 ], pPos[ 1 ], boundaries );
		 
		args[ aid ] = generateRelativePlayerPositions( sess , '|' , boundaries );
		args[ aid + 1 ] = MazeGenerator.convertMazeToString( croppedMaze , '|' , ',' );
		args[ aid + 2 ] = MazeGenerator.convertCoinsToString( croppedCoins, '|', ',' ); 
		args[ aid + 3 ] = playerPropertyToString( sess , '|' , "status" , "." );
		args[ aid + 4 ] = playerPropertyToString( sess , '|' , "coins" , "." );
		
		return args;
		
	}
	
	public String[] constructMoveArguments( Session sess , Player p , String username , String message ) {
		
		String[] args = new String[ 12 ];
		
		args[ 0 ] = Protocol.P_MOVE;
		args[ 1 ] = username;
		args[ 2 ] = playerPropertyToString( sess , '|' , "coins" , "." );
		 
		// Add player data
		int aid = 3;
	 
		
		int boundaries[] = sess.maze.getCroppedBoundaries( p.x , p.y , sess.visibleRows , sess.visibleCols );
		int[][] croppedMaze = sess.maze.getCroppedMaze( boundaries );
		boolean[][] croppedCoins = sess.maze.getCroppedCoins( boundaries );
	 
		args[ aid ] = generateRelativePlayerPositions( sess , '|' , boundaries  );
		args[ aid + 1 ] = MazeGenerator.convertMazeToString( croppedMaze , '|' , ',' );
		args[ aid + 2 ] = MazeGenerator.convertCoinsToString( croppedCoins, '|', ',' ); 
		args[ aid + 3 ] = playerPropertyToString( sess , '|' , "status" , "." );
		args[ aid + 4 ] = playerPropertyToString( sess , '|' , "orientation" , "." );
		args[ aid + 5 ] = ""+p.coins;
		args[ aid + 6 ] = trapsToString( sess , '|' , boundaries );
		args[ aid + 7 ] = ""+p.firesLeft;
		args[ aid + 8 ] = message;
			
		
		return args;
		
	}
	
	protected void movePlayer( Session sess , Player player , int direction ) {
		
		// If the game start countdown is still going, don't allow anyone to exploit the game. I.e. no moves can be made
		if( sess.gameStartCountdownStarted && sess.gameStartCountdown.countdown > 0 ) {
			return;
		}
		
		int px = player.x;
		int py = player.y;
		
		int nx = px, ny = py;
		int orientation = 0;
		  
		
		// Set the new position we want to move the player to  
		if( direction == 0 ) {				// Move up
			nx -= 1;
			orientation = 0;
		} else if( direction == 1 ) {		// Move down
			nx += 1;
			orientation = 2;
		} else if( direction == 2 ) {		// Move left
			ny -= 1;
			orientation = 1;
		} else if( direction == 3 ) {		// Move right
			ny += 1;
			orientation = 3;
		}
		
		int msize = sess.maze.getMazeSize();
		// The new position is out of bounds for the maze
		if( nx < 0 || ny < 0 || nx >= msize || ny >= msize ) {
			
			return;
		}
		
		int[][] maze = sess.maze.getMaze();
		boolean[][] coins = sess.maze.getCoins();
		
		// Check to see if this cell in the maze is a wall => illegal move
		if( maze[ nx ][ ny ] == MazeConstants.WALL ) {
			
			return;
			
		} 
		
		// Anything other than a wall is fair game, the question is, is it a start/finish cell or simply a walkable one
		
		// Set the player's new position since this new cell is ok
		player.x = nx;
		player.y = ny;
		player.orientation = orientation;
		
		// Player collects this coin
		if( coins[ nx ][ ny ] == true ) {
			
			sess.maze.collectCoin( nx , ny );
			player.coins++;
			
		}
		
		 
		 
		if( maze[ nx ][ ny ] == MazeConstants.WALKABLE || maze[ nx ][ ny ] == MazeConstants.START || maze[ nx ][ ny ] == MazeConstants.FINISH ) {
			
			String message = " ";
			
			int trapHere = trapAtPos( sess , nx , ny );
			 
			// You got hit with a trap, some of your coins were taken away
			if( trapHere > -1 ) {
				String from = sess.traps.get( trapHere )[2];
				// IF this is your own trap, you can't be caught in it
				if( !from.equals( player.username ) ) {
					 
					int before = player.coins;
					
					player.coins -= Constraints.coinsLostPerTrap;
					if( player.coins < 0 ) { player.coins = 0; }
					
					// remove trap from list
					sess.traps.remove( trapHere );
					
					message = "[HIT] You walked over a trap placed by ["+from+"]. You lost " + (before-player.coins) + " coins.";
					
				} 
				
			}
			
			notifyPlayersOfMove( sess , player.username , message );
			
			// The player has finished the maze
			if( maze[ nx ][ ny ] == MazeConstants.FINISH ) {
				
				// Set this player as finished
				calculateGameTime( player );
				int finishPosition = setFinished( player.session , player.sessionPosition );
				player.finished = true;
				  
				int numFinished = countFinished( player.session );
				int activePlayers = countSessionPlayers( player.session );
				
				int won = 0;
				if( numFinished == 1 ) {						// You are the winner
					 
					won = 1;
					
					// Change the timer to the final countdown if there is too much time left.
					if( sess.gameFinishCountdown.countdown > Constraints.gameFinalCountdown ) {
						
						sess.gameFinishCountdown.countdown = Constraints.gameFinalCountdown;
						
					}
					 
				} else if( numFinished == activePlayers ) {		// All players have finished
					
					sess.playersCanMove = false;
					sess.gameFinished = true;
				 
					sess.returnCountdown.start();	
					sess.returnCountdownStarted = true;
					
				}
				
				int coinsCollected = player.coins * (won + 1);
				int ptime = (int) player.sessionTime;
				 
				int score = calculatePoints( ptime/1000 , player.session.timeLimit , coinsCollected , player.session.maxCoins );
				player.points = score;
				
				db.updateStats( player.username, won , 1 , coinsCollected , score );
				db.insertScore( player.username , player.session.levelTheme , player.session.maze.getMazeSize() , ptime , finishPosition, score, coinsCollected );
				
				String finished = finishedPlayersToString( player.session , '|' );
				notifyPlayerHasFinished( player.session , player , finished );
				
			}
			
		}
		
	}
	
	/**
	 * Create a coin catcher trap at your current position. Each player only has one.
	 * A coin catcher steals a certain amount of coins from an opponent who walks into it.
	 * 
	 * @param sess The session object
	 * @param player The player object firing the trap
	 */
	protected synchronized void fireTrap( Session sess , Player player ) {
		
		if( sess == null || sess.players == null || sess.players.length == 0 || player == null ) { return; }
		
		// You can't fire anything anymore
		if( player.firesLeft < 1 ) { return; }
		
		int px = player.x;
		int py = player.y;
		
		int msize = sess.maze.getMazeSize();
		// The new position is out of bounds for the maze
		if( px < 0 || py < 0 || px >= msize || py >= msize ) {
			return;
		}
		
		int trapPos = trapAtPos( sess , px , py );
		if( trapPos > 0 ) { return; }
		
		int[][] maze = sess.maze.getMaze();
		
		// You can not place traps at the start of finish of a maze
		if( maze[ px ][ py ] == MazeConstants.START || maze[ px ][ py ] == MazeConstants.FINISH ) {
			
			return;
			
		} 
		
		player.firesLeft--;
		
		sess.traps.add( new String[] { ""+px , ""+py , player.username } );
		
		notifyPlayersOfMove( sess , player.username , "A trap was left somewhere by ["+player.username+"]!" );
		
	}
	
	/**
	 * Get the index of the trap placed at a given position in the maze.
	 * 
	 * @param sess The session object
	 * @param x The row
	 * @param y The column
	 * @return >= 0 If a trap is found, -1 if nothing is found
	 */
	protected synchronized int trapAtPos( Session sess , int x , int y ) {
		
		int size = sess.traps.size();
		if( size == 0 ) { return -1; }
		
		String[] trap;
		int tx,ty;
		for( int i = 0 ; i < size ; i ++ ) {
			
			trap = sess.traps.get(i);
			tx = Integer.parseInt( trap[0] );
			ty = Integer.parseInt( trap[1] );
			
			if( tx == x && ty == y ) {
				return i;
			}
			
		}
		
		return -1;
		
	}
	
	protected synchronized String trapsToString( Session sess , char delimiter , int[] boundaries ) {
		
		String result = "";
		
		if( sess == null || sess.players == null ) {
			return result;
		}
		
		int num = sess.traps.size();
		 
		String[] trap;
		int tx,ty;
		for( int i = 0 ; i < num ; i++ ) {
			
			trap = sess.traps.get(i);
			
			// Trap positions are kept as string so that the array can also keep the username of the person who drops the trap => Need conversion to ints
			tx = Integer.parseInt( trap[0] );
			ty = Integer.parseInt( trap[1] );
			
			// We don't want to send data to the players that is outside of their viewports
			if( tx < boundaries[0] || tx > boundaries[1] || ty < boundaries[2] || ty > boundaries[3] ) {
				continue;
			}
			
			result += ""+(tx-boundaries[0])+","+(ty-boundaries[2])+","+trap[2];
			
			if( i < num - 1 ) {
				result += delimiter;
			}
			
		}
		
		return result;
		
	}
	
	protected long calculateGameTime( Player player ) {
		
		player.endTime = System.currentTimeMillis();
		player.sessionTime = player.endTime - player.startTime;
		
		return player.sessionTime;
		
	}
	
	/**
	 * If the game time runs out, stop all players that haven't finished
	 * 
	 * They don't get any coins, nor are they considered to have finished the maze.
	 * 
	 * You snooze you lose o_O
	 * 
	 * @param sess The session object
	 */
	protected synchronized void stopRemainingPlayers( Session sess ) {
		
		int pLen = sess.players.length;
		Player p;
	 
		for( int i = 0 ; i < pLen ; i++ ) {
			 
			p = sess.players[ i ];
			
			if( p == null ) { continue; }
			
			if( p.finished ) { continue; }
			
			// DNF
			p.sessionTime = -1;
			setFinished( p.session , p.sessionPosition );
			p.finished = true;
			
			
		}
		 
		String finished = finishedPlayersToString( sess , '|' );
		
		for( int i = 0 ; i < pLen ; i++ ) {
			
			p = sess.players[ i ];
			
			if( p == null ) { continue; }
				
			p.toClient.println( Protocol.encode( Protocol.P_GAME_COUNTDOWN_RESULT, finished ) );
			p.toClient.flush();
		
		}
		
	}
	 
	
	/**
	 * Sort an array of player stats from within the game. 
	 * Compares the player's solution path distances. 
	 * If two or more players are equally as far away from the finish, the amount of coins collected is compared
	 * 
	 * Time complexity: O(n^2)
	 * 
	 * @param arr a 2D array of integers containing player data
	 * @return the sorted 2D array. The first element is the top player (from the ones who haven't finished in time that is)
	 */
	private int[][] sortDistances( int[][] arr ) {
		
		int[][] temp = new int[4][3];
		
		int pCount = 0;
		for( int i = 0 ; i < arr.length; i++ ) {
			
			// Start with some default values for any player that isn't active or has finished already.
			temp[i][0] = -1;
			temp[i][1] = -1;
			temp[i][2] = -1;
			
			// Skip this element
			if( arr[i][0] < 0 || arr[i][1] < 0 ) { continue; }
			
			pCount++;
			
			boolean better = true;
			int index = 0;
			for( int j = 0 ; j < arr.length; j++ ) {
				
				// Don't compare yourself to yourself..
				if( i == j ) { continue; }
				
				// Compare the values in the array. If a player has an equal time as another, then compare the coins.
				if( arr[i][1] < arr[j][1] ) {
					better = true;
				} else if( arr[i][1] > arr[j][1] ) {
					better = false;
					index++;
				} else {
					if( arr[i][2] > arr[j][2] ) {
						better = true;
					} else {
						better = false;
						index++;
					}
				}
				
			}
			
			// Add this element to its appropriate place.
			temp[ index ] = arr[i];
			
		}
		
		return temp;
		
	}
	
	/**
	 * Notify the players in the game that a player has moved.
	 * 
	 * @param sess The session object
	 * @param username The username of the player that made the move
	 */
	protected synchronized void notifyPlayersOfMove( Session sess , String username , String message ) {
		
		int pLen = sess.players.length;
		Player p;
	 
		for( int i = 0 ; i < pLen ; i++ ) {
						
			p = sess.players[ i ];
			
			if( p == null ) { continue; }
			
			String args = Protocol.encode( constructMoveArguments( sess , p , username , message ) );	
			p.toClient.println( args );
			p.toClient.flush();
		
		}
		
	}
	
	/**
	 * Create a string representation of all players who have finished the game.
	 * Includes the player's name, the time it took them to complete the maze and the coins they collected.
	 * 
	 * @param sess The session object
	 * @param delimiter The delimiter for each player (default is | )
	 * @return The string of player statuses
	 */
	protected synchronized String finishedPlayersToString( Session sess , char delimiter ) {
		
		String output = "";
		
		int pLen = sess.players.length;
		Player p;
		int fid = 0;
		
		for( int i = 0 ; i < pLen ; i++ ) {
			
			fid = sess.finishPositions[ i ];
			
			if( fid < 0 ) { continue; }
			
			p = sess.players[ fid ];
			
			if( p == null ) { continue; }
			
			long time = p.sessionTime > 0 ? p.sessionTime/1000 : -1;
			
			output += ""+ p.username + "," + time + "," + p.coins + "," + p.points;
					
			if( i < pLen - 1 ) {
				output += delimiter;
			}
		
		}
		
		return output;
		
	}
	
	/**
	 * Notify all active players in a gmae session that a certain player has finished.
	 * 
	 * @param sess
	 * @param player
	 */
	protected void notifyPlayerHasFinished( Session sess , Player player , String finished ) {
		
		Player p;
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			if( p == null ) { continue; }
				
			p.toClient.println( Protocol.encode( Protocol.P_PLAYER_FINISHED , player.username , finished ) );
			p.toClient.flush();
		
		}
		
	}
	
	protected synchronized int setFinished( Session sess , int pSessID ) {
		
		sess.finishPositions[ sess.numFinished ] = pSessID;
		sess.numFinished++;
		
		return sess.numFinished;
				
	}
	
	protected synchronized int countFinished( Session sess ) {
		
		int count = 0;
		for( int i = 0 ; i < sess.finishPositions.length ; i++ ) {
			
			if( sess.finishPositions[i] > -1 ) {
				count++;
			}
			
		}
		
		return count;
		
	}
	
	protected synchronized String playerPropertyToString( Session sess , char delimiter , String property , String empty ) {
		
		String result = "";
		
		if( sess == null || sess.players == null ) {
			return result;
		}
		
		Player p;
		int numP = sess.players.length;
		for( int i = 0 ; i < numP ; i++ ) {
			
			p = sess.players[ i ];
			
			if( p == null ) {
				
				result += empty;
				
			} else {
				
				if( property.equals("status") ) {
					result += p.username;
				} else if( property.equals("orientation") ) {
					result += p.orientation;
				} else if( property.equals("coins")) {
					result += p.coins;
				}
				
			}
			
			if( i < numP - 1 ) {
				result += delimiter;
			}
			
		}
		
		return result;
		
	}
 
	
	public int[] mazeStartingPoint( int mazeSize , int startID ) {
		
		// Top left
		if( startID == 0 ) {
			
			return new int[] { 1 , 1 };
		
		// Top right
		} else if( startID == 1 ) {
			
			return new int[] { 1 , mazeSize - 2 };
			
		} else if( startID == 2 ) {
			
			return new int[] { mazeSize - 2 , 1 };
			
		} else if( startID == 3 ) {
			
			return new int[] { mazeSize - 2 , mazeSize - 2 };
			
		} else {
			
			return new int[] { -1, -1 };
			
		}
		
		
	}
	
	public String playerPositionToString( int[] pos ) {
		
		return pos[ 0 ] + "," + pos[ 1 ];
		
	}
	
	public synchronized String generateRelativePlayerPositions( Session sess , char delimiter , int[] boundaries ) {
		
		String result = "";
		
		if( sess == null || sess.players == null ) {
			return result;
		}
		
		Player p;
		int numP = sess.players.length;
		for( int i = 0 ; i < numP ; i++ ) {
			
			p = sess.players[ i ];
			
			if( p == null ) {
				
				result += "-1,-1";
				
			} else {
				
				//int[] pos = mazeStartingPoint( sess.maze.getMazeSize() , i );
				result += playerPositionToString( sess.maze.getAdjustedPlayerPosition( p.x , p.y , boundaries ) );
			}
			
			if( i < numP - 1 ) {
				result += delimiter;
			}
			
		}
		
		return result;
		
	}
	
	 
	
	public synchronized void notifySessionPlayers( Session sess , String message ) {
		 
		if( sess == null || message == null || sess.players == null ) { return; }
		
		Player p;
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			// If the player is null (could occur if the client disconnects mid-game)
			if( p == null ) {
				continue;
			}
			p.toClient.println( message );
			p.toClient.flush();
			
		}
		
	}
	
	public synchronized void notifySessionPlayers( int id , String message ) {
		
		// Out of bounds
		if( sessions == null || id < 0 || id >= sessions.size() ) {
			return;
		}
		
		Session sess = sessions.get( id );
		
		// Make sure the players list isn't null
		if( sess.players == null || message == null ) {
			return;
		}
		
		Player p;
		for( int i = 0 ; i < sess.players.length ; i++ ) {
			
			p = sess.players[ i ];
			
			// If the player is null (could occur if the client disconnects mid-game)
			if( p == null ) {
				continue;
			}
			p.toClient.println( message );
			p.toClient.flush();
			
		}
		
	}
	
	/*
	 * Create a simple list in string format of the player names and a time of login
	 */
	public String generateQueueList() {
		
		String result = "";
		
		Player player;
		
		int size = queue.size();
		
		for( int i = 0 ; i < size; i++ ) {
			
			player = queue.get( i );
			
			result += player.username + "," + player.timestamp; 
					
			if( i < size - 1 ) {
				result += "|";
			}
			
		}
		
		return result;
		
	}
	
	public boolean checkClientConnection( Socket socket ) {
		return true;
	}
	
	public synchronized int playerQueueID( String username ) {
		

		if( Database.validateUsername( username ) < 1 ) {
			return -1;
		}
		
		Player player;
		for( int i = 0 ; i < queue.size(); i++ ) {
		
			player = queue.get( i );
			
			if( player.username.equals( username ) ) {
				
				return i;
				
			}
			
		}
		
		return -1;
	}
	
	/**
	 * Get the username of a registered user based on a supplied local port (that they are currently connected on)
	 * 
	 * @param localPort The user's local port on the server
	 * @return The username if any is found. Otherwise null is returned
	 */
	public synchronized String getUserFromPort( int localPort ) {
	 
		for( Player player : active ) {
			 
			if( player.localPort == localPort ) {
				
				return player.username;
				
			}
			
		}
		
		return null;
			
	}
	
	
	/**
	 * Get the current time in Hours:Minutes:Seconds format.
	 * This is used by the addMessage() method to save the time when the message was created/sent
	 * 
	 * @return The string representation of the current time
	 */
	public static String getCurrentTimeStamp() {
		
	    return new SimpleDateFormat("HH:mm:ss").format(new Date());
	    
	}
	
	public String getRandomTheme() {
		
		return mazeThemes[ randInt( 0 , mazeThemes.length - 1 ) ];
		
	}
	
	public static int randInt(int min, int max) {
		 
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
}
