package protocol;

/**
 * Used to encode and decode Strings for communicating across the network between client and server.<br>
 * Use the set of constants to prefix each message over the network. P_ comes before a command from Player to Server,
 * S_ comes before a command from Server to Player.
 * <br>
 * Protocol is a static class that does not need to be instantiated.
 * <br>
 * Useful methods: encode(String[] args), decode(String message)
 * @author Team Wigan
 * @version 1.0
 */
public class Protocol {
	
	public static final String P_CONNECTION_TEST = "99";
	
	/*
	 * Indicates to the clients who are part of the queue when its time to switch to the session panel
	 */
	public static final String P_SESSION_START = "20";
	
	public static final String P_PLAYER_FINISHED = "29";
	 
	
	public static final String P_GAME_START = "22";
	// The countdown timer before the players can actually make moves
	public static final String P_GAME_START_COUNTDOWN = "32";
	
	public static final String P_GAME_COUNTDOWN_RUNNING = "30";
	public static final String P_GAME_COUNTDOWN_RESULT = "31";
	public static final String P_GAME_RETURN_COUNTDOWN = "33";
	
	public static final String P_LOBBY_READY = "21";
	public static final String P_LOBBY_COUNTDOWN_RUNNING = "23";
 
	public static final String P_LEAVE_QUEUE = "28";
	
	// This is used as a response to the client leaving a lobby and returning back to the main panel
	public static final String P_RETURN_TO_MAIN = "27";
	
	// This is used as a response from the server to move the player to the queue from the main panel
	public static final String P_MOVE_TO_QUEUE = "26";
	
	public static final String P_GET_STATS = "36";
	public static final String P_GET_TOPSCORES = "37";
	
	/**
	 * Player requests a move
	 */
	public static final String P_MOVE = "0";
	
	// Fire a projectile 
	public static final String P_FIRE = "35";
	
	/**
	 * Player wants to sign out and disconnect
	 */
	public static final String P_QUIT = "1";
	
	/**
	 * Player wants to login with given details: [username][password]
	 */
	public static final String P_LOGIN = "2";
	
	/**
	 * Player wants to log out and return to the main menu
	 */
	public static final String P_LOGOUT = "3";
	
	// A Queue update is happening (i.e. a new player has joined the server and we want to notify the other people in the queue)
	public static final String P_QUEUE_UPDATE = "15";
	
	/**
	 * Player wants to sign up with given details: [username][password]
	 */
	public static final String P_SIGNUP = "4";
	 
	private static String escape(String str){
		str = str.replace("\\", "\\\\");
		str = str.replace("_", "\\_");
		str = str.replace("\n", "\\n");
		return str;
	}
	
	private static String unescape(String str){
		str = str.replace("\\\\", "\\");
		str = str.replace("\\_", "_");
		str = str.replace("\\n", "\n");
		return str;
	}
	
	/**
	 * Encode the given arguments into a single message.
	 * @param args An array of Strings, or repeated Strings separated by commas.
	 * @return A single encoded String containing all elements of args.
	 */
	public static String encode (String... args) {
		String result = "";
		for(String str: args){
			if(result!="") result = result + ":__:";
			result = result + escape(str);
		}
		//System.out.println("send: " + result);
		return result;
	}
	
	/**
	 * Convert the given encoded message into an array of Strings.
	 * @param message a message that was encoded using this protocol
	 * @return A String array with the correct number of elements.
	 */
	public static String[] decode (String str) {
		//System.out.println("receive: " + str);
		if(str == null || str.equals("")){
			return new String[0];
		}
		String[] messages = str.split(":__:");
		for(int i=0; i<messages.length; i++){
			messages[i] = unescape(messages[i]);
		}
		if(messages.length == 1 && messages[0].equals("")){
			return new String[0];
		}
		return messages;
	}
	
	 
}
