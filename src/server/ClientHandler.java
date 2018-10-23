package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import protocol.Protocol;

/**
 * Handler class created when a new thread (user connection) is established
 * 
 * @author Iliya Liksov
 *
 */
public class ClientHandler implements Runnable {
	
	private Socket socket;
	private final Server server;
	private Player player;
	
	private boolean loggedIn;
	
	/**
	 * Constructor for handler class
	 * 
	 * @param server The reference to the server object
	 * @param socket The reference to the client's socket object
	 */
	public ClientHandler( Server server , Socket socket ) {
 
		
		this.server = server;
		this.socket = socket;
		this.player = new Player( socket , null , this , server.getCurrentTimeStamp() );
		
		this.loggedIn = false;
		
	}
	
	public void stop() {
		Thread.currentThread().interrupt();
	}
	
	
	/**
	 * Run method for the Handler. Executed when the new thread is created.
	 */
	@Override
	public void run() {
		
		
		System.out.println("Starting thread runnable...");
		
		try {
			
			// Input and Output pairs
			PrintWriter toClient = new PrintWriter( socket.getOutputStream() );
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			 
			// Output message to send to the client (declared once to avoid re-allocating memory)
			String request, output;
			
			
			// Listen for input streams
			while( !socket.isClosed() ) {
			 
				// If the current input stream is not ready yet, skip this iteration
				if( !fromClient.ready() ) { continue; }
				
				request = fromClient.readLine();
				 
				// Skip this iteration if the input stream is null
				if( request == null ) { continue; }
				
				// Get the client's local port 
				int localPort = socket.getPort();
				
				// Decode the message into an array using the protocol
				String decoded[] = Protocol.decode( request );
				
				// No null messages or messages with no header allowed
				if( decoded == null || decoded.length < 1 ) { continue; }
							
				String requestType = decoded[0];
				  
				// Every iteration of the loop, reset the outputData string
				output = "";
				 
				// If the user is not logged in, consider the login/register requests
				if( !loggedIn ) {
				
					String username = decoded[1].trim();	// remove whitespace from edges of strings
					String password = decoded[2].trim();
					
					// If user login attempt
					if( requestType.equals( Protocol.P_LOGIN ) ) {
						 
						// Attempt to login this user
						int loginAttempt = server.db.login( username , password , localPort );
						
						// If login is successful => Add player to lobby queue and start their thread
						if( loginAttempt == 1 ) {
							
							// Set the username for this player (on the thread)
							player.username = username;
							player.timestamp = server.getCurrentTimeStamp(); 
							// Important flag. Makes sure the thread skips over the auth section if the user is logged in already
							loggedIn = true;
							 
							// Add player to active players list
							int numActive = 0;
							synchronized( server.active ) {
								server.active.add( player );
								numActive = server.active.size();
							}
							
							// Send back the message that the user has logged in so that they can switch panels
							output = Protocol.encode( Protocol.P_LOGIN , "true" , "Login successful. Welcome, " + username + "!" , ""+numActive );
							toClient.println( output );
							toClient.flush();
							
							continue;
							  
							
						} // If login failed => return message to client and close socket
						else if( loginAttempt < 1 ) {
							
							if( loginAttempt == 0 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , 
														  "false" , 
														  "Username must be between " 
														  + Constraints.usernameMinLength 
														  + " and " 
														  + Constraints.usernameMaxLength
														  + " characters, and password between "
														  + Constraints.passwordMinLength + " and "
														  + Constraints.passwordMaxLength
														);
	
								
							} else if ( loginAttempt == -1 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "The username and password must be alphanumeric!" );
								
							} else if( loginAttempt == -2 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "This username does not exist in the database." );
								
							} else if( loginAttempt == -3 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "This user has already connected from a different client." );
								
							} else if( loginAttempt == -4 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "The password for this username is incorrect!" );
								
							} else if( loginAttempt == -5 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "There was a problem with the database." );
								
							} else if( loginAttempt == -6 ) {
								
								output = Protocol.encode( Protocol.P_LOGIN , "false" , "There was a problem with input processing." );
								
							}
							
							// Do not send empty messages
							if( output.length() > 0 ) {
								// Send the message back to the client GUI
								toClient.println( output );
								toClient.flush();
								// Close the socket if the login has not been successful
								socket.close();
								break; // Needs testing - is it necessary?
		
							}
							 
						}
						
						 
						
					} // If user sign in attempt
					else if( requestType.equals( Protocol.P_SIGNUP ) ) {
						
						// Attempt to register this user
						int regAttempt = server.db.register( username , password );
						
						// If registration is successful
						if( regAttempt == 1 ) {
							
							output = Protocol.encode( Protocol.P_SIGNUP, "true" , "Registration successful." );
							
						} 
						// If registration is not successful
						else if( regAttempt < 1 ) {
							
							if( regAttempt == 0 ) {
								
								output = Protocol.encode( Protocol.P_SIGNUP, 
														  "false" , 
														  "Username must be between " 
														  + Constraints.usernameMinLength 
														  + " and " 
														  + Constraints.usernameMaxLength
														  + " characters, and password between "
														  + Constraints.passwordMinLength + " and "
														  + Constraints.passwordMaxLength 
														);
								
							} else if( regAttempt == -1 ) {
								
								output = Protocol.encode( Protocol.P_SIGNUP, "false" , "The username and password must be alphanumeric!" );
								
							} else if( regAttempt == -2 ) {
								
								output = Protocol.encode( Protocol.P_SIGNUP, "false" , "The username is already taken!" );
								
							} else if( regAttempt == -3 ) {
								
								output = Protocol.encode( Protocol.P_SIGNUP, "false" , "There was a problem with the database." );
								
							} else if( regAttempt == -4 ) {
								
								output = Protocol.encode( Protocol.P_SIGNUP, "false" , "There was a problem with input processing." );
								
							} 
							
						}
						
						// Do not send empty messages
						if( output.length() > 0 ) {
							// Send the message back to the client GUI
							toClient.println( output );
							toClient.flush();
							// Close the socket after registration request regardless of the outcome
							socket.close();
							break; // Needs testing - is it necessary?
	
						}
						
						
						
					}
				
				
				} 
				// If the user is logged in, check for 
				else {
					
					// The client wants to terminate the connection
					if( decoded[ 0 ].equals( Protocol.P_QUIT ) ) {
						
						// Log this user out from the database
						server.db.logout( player.username );
						loggedIn = false;
						
						// Remove the player from the active players list
						synchronized( server.active ) {
							server.active.remove( player );
						}
						 
						// Remove the player from wherever he is in the server (queue/lobby/etc)
						server.removePlayer( player );
					 
						 
						// Close the socket finally and interrupt the thread						
						socket.close();
						Thread.currentThread().interrupt();
						break;
						
					} else if( decoded[0].equals( Protocol.P_CONNECTION_TEST ) ) {
							
						toClient.println( Protocol.encode( Protocol.P_CONNECTION_TEST ) );
						toClient.flush();
						
					}  
				

				}
				
				// ------------------------------------------[LOBBY CODE]-----------------------------------------------
				
				if( loggedIn && player.session == null ) {
					
					if( decoded[0].equals( Protocol.P_MOVE_TO_QUEUE ) ) {
						
						server.addPlayerToQueue( player );
						
					} else if( decoded[0].equals( Protocol.P_LEAVE_QUEUE ) ) {
						
						server.leaveQueue( player );
						
					} else if( decoded[0].equals( Protocol.P_GET_STATS ) ) {
						
						server.getPlayerStats( player );
						
					} else if( decoded[0].equals( Protocol.P_GET_TOPSCORES ) ) {
						
						server.getTopScores( player );
						
					}
					
					
				}
				
				if( loggedIn && player.session != null ) {
					
					// Session requests
					if( decoded[ 0 ].equals( Protocol.P_LOBBY_READY ) ) {
						
						System.out.println("Ready: " + player.username);
					 
						// Set player to ready for the session to begin
						// Does this overwrite session.player[i].ready?
						player.ready = true;
						player.session.players[ player.sessionPosition ].ready = true;
						 
						// Send response back to player stating their status is set to ready
						String readyMessage = Protocol.encode( Protocol.P_LOBBY_READY , server.lobbyPlayerStatusesToString( player.session , '|' ) , player.username );
						server.notifySessionPlayers( player.session , readyMessage );
						
						int numReady = server.countReadyPlayers( player.session );
						 
						// Start the countdown before the game begins
						if( numReady >= Constraints.minReadyPlayersRequired ) {
							
							if( player.session.readyCountdownStarted == false ) {
								
								player.session.readyCountdownStarted = true;
								player.session.readyCountdown.start();
							
							}
							// If all the players in the lobby click ready, don't waste time, start the game
							if( numReady >= server.countSessionPlayers( player.session ) ) {
								
								player.session.readyCountdown.countdown = 0;
								
							}
							
						}
						
						 
						
						
						
					} else if( decoded[0].equals( Protocol.P_RETURN_TO_MAIN ) ) {
						
						// RETURN THE PLAYER TO THE MAIN PANEL AFTER THEY LEAVE A LOBBY
						server.removePlayerFromSession( player , true );
						
					} else if( decoded[0].equals( Protocol.P_MOVE ) && player.session.gameRunning == true && player.session.playersCanMove && player.session.gameFinished == false ) {
						
						int direction = Integer.parseInt( decoded[1] );
						
						if( direction < 0 || direction > 4 ) { continue; }
						
						server.movePlayer( player.session , player , direction );
						
					} else if( decoded[0].equals( Protocol.P_FIRE ) ) {
						
						server.fireTrap( player.session , player );
						
					}
					
					
					
				
				}
				
				// ----------------------------------------------------------------------------------------------------------
				
				
			}
				
				
		 
			
		} catch ( IOException e ) {
			 
			e.printStackTrace();
			
		} finally {
			
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
					
	}

}
