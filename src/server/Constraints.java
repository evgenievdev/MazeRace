package server;
 
public class Constraints {
	 
	// How many people can connect to the server at one time (this only applies to login)
	public static final int maxPlayerConnections = 1;
	// The minimum number of players required to start a game
	public static final int minPlayersRequired = 3;
	// How many players at least need to be ready in order for the lobby countdown to start
	public static final int minReadyPlayersRequired = 2;
	// How many seconds it takes after enough players are ready to play to start the game.
	public static final int lobbyCountdownTime = 10;
	// How many seconds it takes for the game to start once all players are connected
	public static final int gameStartCountdown = 5;
	// How many seconds it takes after the first player finishes the maze, to end the game
	public static final int gameFinalCountdown = 30;
	// How much time is allowed per cell (used to calculate the maximum time limit for a maze)
	public static final int maxTimePerCell = 10;
	// How long it takes before the players are returned to main menu after they've finished a game.
	public static final int returnCountdown = 10;
	// How many coins can a player lose if they hit a trap
	public static final int coinsLostPerTrap = 20;
	
	
	// The character length minimums and maximums for the players during authentication
	public static final int usernameMinLength = 5,
			 				usernameMaxLength = 32,
			 				passwordMinLength = 6,
			 				passwordMaxLength = 32;
	
	public static boolean validFormatAlphaNum( String data ) {
		
		if( data == null ) {
			return false;
		}
		
		int len = data.length();
		
		char c;
		
		for( int i = 0 ; i < len ; i++ ) {
			
			c = data.charAt( i );
			
			if( !Character.isLetterOrDigit( c ) ) {
				
				return false;
				
			}
			
		}
		
		return true;
		
	}
 
	
	
}
