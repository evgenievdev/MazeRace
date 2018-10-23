package server;

import java.util.Timer;
import java.util.ArrayList;

import server.mazegeneration.*;

public class Session {
	
	private Server server;
	
	protected Player[] players;
 
	protected SessionTimer readyCountdown, gameFinishCountdown, gameStartCountdown, returnCountdown;
	protected boolean readyCountdownStarted , gameFinishCountdownStarted , gameStartCountdownStarted, returnCountdownStarted;
	
	protected boolean gameRunning = false;
	protected boolean playersCanMove = false;
	protected boolean gameFinished = false;
	protected int[] finishPositions = new int[4]; // The session id's of the players in the order of finishing the race
	protected int numFinished = 0;
	
	protected String levelTheme;
	protected int quadSize;
	protected int timeLimit; // in seconds
	protected int maxCoins;
	protected int visibleRows, visibleCols;
	protected MazeGenerator maze;
	
	protected ArrayList<String[]> traps;
	
	public Session( Server server ) {
		
		this.server = server;
		
		// All values are null by default
		players = new Player[ 4 ];
		
		traps = new ArrayList<String[]>();
		
		readyCountdownStarted = false;
		readyCountdown = new SessionTimer( server , this , Constraints.lobbyCountdownTime , 0 );
		 
		levelTheme = server.getRandomTheme();
		maze = new MazeGenerator( server.randInt(25, 50) , false , true , false , true , true , true , true );
		quadSize = maze.getQuadSize(); // Make sure that we get the right quad size after the maze is generated (due to possible incrementation of even numbers)
		timeLimit = quadSize * Constraints.maxTimePerCell;
		maxCoins = maze.countCoins();
		visibleRows = visibleCols = 7;
		
		gameStartCountdown = new SessionTimer( server , this , Constraints.gameStartCountdown , 1 );
		gameStartCountdownStarted = false;
		
		gameFinishCountdown = new SessionTimer( server , this , timeLimit , 2 );
		gameFinishCountdownStarted = false;
		
		returnCountdown = new SessionTimer( server , this , Constraints.returnCountdown , 3 );
		returnCountdownStarted = false;
		
		// For inactive players, -1 is the code. All finish positions are -1 at the start until they finish the game individually
		for( int i = 0 ; i < 4 ; i++ ) {
			finishPositions[ i ] = -1;
		}
		
	}
 
	
	public Player[] getPlayers() {
		
		return players;
		
	}
	
	public Player getPlayer( int id ) {
		
		if( id < 0 || id >= players.length || players[ id ] == null ) {
			
			return null;
			
		}
		
		return players[ id ];
		
	}
	
	public boolean addPlayer( Player player , int id ) {
		
		if( player == null || id < 0 || id >= players.length ) {
			
			return false;
			
		}
		
		players[ id ] = player;
		
		return true;
		
	}
	
}
