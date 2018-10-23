package server;

import java.util.TimerTask;
import java.util.Timer;


import protocol.Protocol;
 
public class SessionTimer {
	
	protected Timer t;
	protected TimerTask task;
	protected int countdown;
	protected Server server;
	protected Session session;
	protected int type;
	 
	
	public SessionTimer( Server server , Session session , int countdown , int type ) {
		
		this.countdown = countdown;
		t = new Timer();
		this.server = server;
		this.session = session;
		this.type = type;
		
		 
		 
	}
	
	/**
	 * The countdown used when the lobby players are ready to play, before they are sent to the maze panel
	 */
	private void lobbyCountdown() {
		
		String message;
		
		if( countdown < 0 ) {
			
			//System.out.println("DONE");
			session.gameRunning = true;
			session.playersCanMove = false;
			server.redirectSessionPlayers( session );
			
			t.cancel();
			return;
			
		}
		
		//System.out.println(countdown);
		message = Protocol.encode( Protocol.P_LOBBY_COUNTDOWN_RUNNING , "GAME WILL START IN : "+countdown + " SECONDS." );
		server.notifySessionPlayers( session , message );
		
		countdown--;
		
	}
	 
	
	/**
	 * 
	 */
	private void gameStartCountdown() {
		
		String message;
		
		// Game over
		if( countdown < 0 ) {
			
			// Start game
			
			server.setPlayersStartTime( session , System.currentTimeMillis() );
			
			session.playersCanMove = true;
			
			session.gameFinishCountdownStarted = true;
			session.gameFinishCountdown.start();
			
			message = Protocol.encode( Protocol.P_GAME_START_COUNTDOWN , "true" );
			server.notifySessionPlayers( session , message );
			
			t.cancel();
			return;
			
		}
	 
		message = Protocol.encode( Protocol.P_GAME_START_COUNTDOWN , "false" , ""+countdown );
		server.notifySessionPlayers( session , message );
		
		countdown--;
		
	}
	
	/**
	 * The countdown used when the game actually starts. This is the maximum time limit per maze (dependent on the size of the maze)
	 */
	private void gameFinishCountdown() {
		
		String message;
		
		// Game over
		if( countdown < 0 ) {
			
			 
			session.playersCanMove = false;
			session.gameFinished = true;
			server.stopRemainingPlayers( session );
			 
			session.returnCountdown.start();	
			session.returnCountdownStarted = true;
			
			t.cancel();
			return;
			
		}
		 
		message = Protocol.encode( Protocol.P_GAME_COUNTDOWN_RUNNING , ""+countdown );
		server.notifySessionPlayers( session , message );
		
		countdown--;
		
		 
		
	}
	
	/**
	 * Return the players back to the main panel.
	 */
	private void gameReturnCountdown() {
		
		String message;
		
		// Game over
		if( countdown < 0 ) {
			
			server.destroyLobby( session , "The game session has ended." );
			
			t.cancel();
			return;
			
		}
		 
		message = Protocol.encode( Protocol.P_GAME_RETURN_COUNTDOWN , ""+countdown );
		server.notifySessionPlayers( session , message );
		
		 
		
		countdown--;
		
	}
	
	 
	
	protected void start() {
		
		if( task != null ) { return; }
		
		this.task = new TimerTask() {
			
			@Override
			public void run() {
				
				
				if( type == 0 ) {				// Lobby timer
					
					lobbyCountdown();
					
				} else if( type == 1 ) {		// The initial countdown before the players can move
					
					gameStartCountdown();
					
				} else if( type == 2 ) {		// The time left to finish the maze
					
					gameFinishCountdown();
					
				} else if ( type == 3 ) {		// The time after which the players will be returned to main menu from the scores table
					
					gameReturnCountdown();
					
				}
				
				 
				
			}
			
		};
		
		t.scheduleAtFixedRate( task , 0, 1000);
		
	}
	
	protected void stop() {
		
		if( t == null ) { return; }
		
		t.cancel();
		
	}
	
	
	 

}
