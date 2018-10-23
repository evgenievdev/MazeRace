package server;

import java.awt.*;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * @author Iliya Liksov
 */
public class Player {

	protected Socket socket;
	protected PrintWriter toClient;
	protected BufferedReader fromClient;
	
	protected int localPort;
	protected String username; 
	protected String timestamp;
	protected int sessionPosition = -1;
	protected boolean ready = false;
	protected Session session;
	
	protected int coins = 0;
	protected int points = 0;
	protected int x = -1;
	protected int y = -1;
	protected int orientation = 0;
	protected int firesLeft = 1;
	
	protected long startTime, endTime;
	protected long sessionTime = 0; // The time it took the player to finish the game (in milliseconds)
	
	protected boolean finished = false;
	
	protected ClientHandler thread;
	
	
	public Player( Socket socket , String username , ClientHandler thread ,  String timestamp ) {
		
		this.socket = socket;
		this.localPort = socket.getLocalPort();
		this.username = username;
		this.timestamp = timestamp;
		this.session = null;
		this.thread = thread;
		
		try {
		
			toClient = new PrintWriter( socket.getOutputStream() );
			fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		 
		} catch ( IOException e ) {
			
			e.printStackTrace();
			
		}
		
	}
 
		
}
