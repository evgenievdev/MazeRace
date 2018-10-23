package server;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import server.Constraints;

import server.PasswordHash;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Database {
	
	private static final String playersTable = "players";
	private static final String scoresTable = "scores";
	
	private static Connection connection = null;
	private static Statement statement = null;
	private ResultSet resultSet = null;
	 
	private String dbHost, dbName , dbUser, dbPass;
	
	//
		 
		/* ------------- SQL STRUCTURE FOR DATABASE -----------
		 
		 CREATE TABLE players
		(
		  id serial NOT NULL,
		  username character varying(32) NOT NULL,
		  password character varying(256) NOT NULL,
		  "loggedIn" boolean NOT NULL DEFAULT false,
		  "lastLogin" timestamp with time zone NOT NULL,
		  "totalCoins" integer NOT NULL DEFAULT 0,
		  "gamesPlayed" integer NOT NULL DEFAULT 0,
		  "gamesFinished" integer NOT NULL DEFAULT 0,
		  "gamesWon" integer NOT NULL DEFAULT 0,
		  "totalPoints" integer NOT NULL DEFAULT 0,
		  CONSTRAINT players_pkey PRIMARY KEY (id)
		);
		
		 CREATE TABLE scores
		(
		  username character varying(32) NOT NULL,
		  mazesize integer NOT NULL,
		  level character varying(64) NOT NULL,
		  completiontime integer NOT NULL DEFAULT 0,
		  finishposition integer NOT NULL DEFAULT 0,
		  score integer NOT NULL DEFAULT 0,
		  coins integer NOT NULL DEFAULT 0,
		  date timestamp with time zone NOT NULL,
		  CONSTRAINT scores_pkey PRIMARY KEY (username)
		);
		 
		 */
	 
		// My local Postgres server 
		//Database db = new Database( "localhost" , "mazerace" , "postgres" , "admin" );
		
		//System.out.println( "Register user : " + db.register( "andreyx" , "password" ) );
		
		//System.out.println( "Attempt to login with wrong password : " +  db.login( "andreyx", "password" ));
		
	//	
	 
	public Database( String dbHost , String dbName , String dbUser , String dbPass ) {
		 
		try {
			
			this.dbHost = dbHost;
			this.dbName = dbName;
			this.dbUser = dbUser;
			this.dbPass = dbPass;
			
			System.setProperty("jdbc.drivers", "org.postgresql.Driver");
			String dbAddress = "jdbc:postgresql://" + dbHost + "/" + dbName;
			
			connection = DriverManager.getConnection( dbAddress , dbUser , dbPass );
			statement = connection.createStatement();

		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
	

		if ( connection != null ) {
			System.out.println("Database accessed!");
		} else {
			System.out.println("Failed to make connection");
		}
		
	}
	
	protected static int validateUsername( String username ) {
		
		// If the username is null reference => Exit method and return 0
		if( username == null ) {
			
			return 0;
			
		}
		
		// Check the length of the username
		int userLen = username.length();
		// If the length of either one is not within the desired boundaries => Exit method and return 0
		if( userLen < Constraints.usernameMinLength || userLen > Constraints.usernameMaxLength ) {
			
			return 0;
			
		}
		
		// Make sure that the username and password are of valid format (alpha-numeric)
		boolean userOK = Constraints.validFormatAlphaNum( username );
		
		// If the username is NOT alphanumeric then we have a problem => Exit method and return -1
		if( !userOK ) {
			
			return -1;
			
		}
		
		return 1;
		
	}
	
	/**
	 * Validate the format of the username and password parameters.
	 * This is a helper method used by the register and login methods.
	 * 
	 * If all is well, returns 1.
	 * Otherwise it returns numbers below 1 depending on the error type.
	 * 
	 * @param username The username to check
	 * @param password The password to check
	 * @return 1 : all OK 
	 * 		   0 : username or password are not within length limits 
	 *  	  -1 : username or password is not alphanumeric
	 */
	protected static int validateUsernameAndPassword( String username , String password ) {
		
		// If the username or password are null reference => Exit method and return 0
		if( username == null || password == null ) {
			
			return 0;
			
		}
		
		// Check the length of the username and password
		int userLen = username.length();
		int passLen = password.length();
		// If the length of either one is not within the desired boundaries => Exit method and return 0
		if( userLen < Constraints.usernameMinLength || userLen > Constraints.usernameMaxLength || 
			passLen < Constraints.passwordMinLength || passLen > Constraints.passwordMaxLength 
		  ) {
			
			return 0;
			
		}
		
		// Make sure that the username and password are of valid format (alpha-numeric)
		boolean userOK = Constraints.validFormatAlphaNum( username );
		boolean passOK = Constraints.validFormatAlphaNum( password );
		
		// If the username or the password are NOT alphanumeric then we have a problem => Exit method and return -1
		if( !userOK || !passOK ) {
			
			return -1;
			
		}
		
		return 1;
		
	}
	
	/**
	 * Register a new player.
	 * 
	 * @param username Their username
	 * @param password Their password (raw data)
	 * @return 1 : The registration is successful,
	 * 		   0 : The user or pass is not within the length limits
	 * 		  -1 : The user or pass are not alphanumeric
	 * 		  -2 : The username is already taken
	 * 		  -3 : There is a problem with the SQL queries
	 * 		  -4 : There is a problem with the Hashing algorithm in PasswordHash class
	 */
	public int register( String username, String password ) {
		
		// Call a reusable method to validate the username and password input
		int validateParams = validateUsernameAndPassword( username , password );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		
		try {
			
			// After all the checks above, before we start adding to the database, we have to lastly check if the current user exists
			String checkQuery = "SELECT username FROM " + playersTable + " WHERE username=?;";
			
			statement = connection.createStatement();
			
			PreparedStatement checkUser = connection.prepareStatement( checkQuery );
			checkUser.setString( 1 , username );
			
			ResultSet found = checkUser.executeQuery();
		 
			// If there are any results found from the query (i.e. there is a next result), this means the username is taken => Exit method and return -2
			int count = 0;
			while( found.next() ) {
				count++;
				if( count >= 1 ) {
					return -2;
				}
			}
			
			 
			
			// If this point is reached, everything is fine and the user can be created
			// IMPORTANT!!! POSTGRES CONVERTS COLUMN NAMES TO LOWERCASE IF THEY ARE NOT IN QUOTES
			// IMPORTANT2!!! ID must be defined as a SERIAL type and set as a PRIMARY KEY to not need to be intialized within the java sql query (auto incremented)
			String insertQuery = "INSERT INTO " + playersTable + "( username, password, \"loggedIn\", \"lastLogin\", \"totalCoins\", \"gamesPlayed\", \"gamesFinished\", \"gamesWon\" , \"totalPoints\" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?);";
			// Create a timestamp from the current time in milliseconds
			Timestamp time = new Timestamp( System.currentTimeMillis() );
			// Hash the password using the PBKDF2 algorithm
			String hashedPassword = PasswordHash.createHash( password );
			
			PreparedStatement newUser = connection.prepareStatement( insertQuery );
			newUser.setString( 1, username );
			newUser.setString( 2, hashedPassword );	
			newUser.setBoolean( 3 , false );	// logged in
			newUser.setTimestamp( 4 , time );	// last login time
			newUser.setInt( 5 , 0 );			// total coins
			newUser.setInt( 6 , 0 );			// games played
			newUser.setInt( 7 , 0 );			// games finished
			newUser.setInt( 8 , 0 );			// games won
			newUser.setInt( 9 , 0 );			// total points
			newUser.executeUpdate();

			System.out.println("User ["+username+"] has been registered successfully.");
			 
			// Success of the query
			return 1;
			
		} catch ( SQLException e ) {
			 
			e.printStackTrace();
			// SQL Problem
			return -3;
			
		} catch ( InvalidKeySpecException|NoSuchAlgorithmException e ) {
			
			e.printStackTrace();
			return -4;
			
		}

	}
	
	/**
	 * Attempt to login a user
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public int login( String username, String password , int localPort ) {
		
		// Call a reusable method to validate the username and password input
		int validateParams = validateUsernameAndPassword( username , password );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		

		try {
			
			String findQuery = "SELECT * FROM " + playersTable + " WHERE username=?;";
			// statement must be scrollable, otherwise we can't go back to the first row in the resultset
			PreparedStatement findUser = connection.prepareStatement( findQuery , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
			findUser.setString( 1 , username );
			
			ResultSet found = findUser.executeQuery();
	 
			String passHash;
			boolean passMatches;
		 
			// If there are no matching results, this means the username is not in the database
			int count = 0;
			while( found.next() ) {
				count++;
				if( count > 1 ) {
					break; // no need to continue further
				}
			}
			if( count == 0 ) {
				return -2;
			}
			
			// Go back to the first row in the result set after checking the count
			found.first();
			 
			
			// At this point the username does exist, but the question remains whether the password matches the hash or not
			passHash = found.getString( 3 );
			passMatches = PasswordHash.validatePassword( password , passHash );
			
			// If the password matches, this is the correct user
			if( passMatches ) {
				
				// -- may be redundant in the end. Too complicated to figure out if a client is still active --
				// Not so fast... we need to check if the user has already logged in from another client
				if( found.getBoolean( 4 ) ) {
					// If the user is already logged in, return -3
					return -3;
					
				}
				
				
				// OK, all is well, continue with the login
				String updateQuery = "UPDATE " + playersTable + " SET \"loggedIn\" = ?, \"lastLogin\" = ? WHERE id = ?;";
				
				// Create a timestamp from the current time in milliseconds
				Timestamp time = new Timestamp( System.currentTimeMillis() );
				
				PreparedStatement updateUser = connection.prepareStatement( updateQuery );
				updateUser.setBoolean( 1 , true );
				updateUser.setTimestamp( 2 , time );
				updateUser.setInt( 3 , found.getInt( 1 ) ); // !IMPORTANT! id must be the first column, otherwise index 0 will correspond to something else
				updateUser.executeUpdate();
				
				return 1;
			 
				
			} 

			// At this point in the code it can only be that the password doesn't match the user's record in the database
			return -4;
			 
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return -5;
			
		} catch ( InvalidKeySpecException|NoSuchAlgorithmException e ) {
			
			e.printStackTrace();
			return -6;
			
		}
		
	}
	
	/**
	 * Find a user to logout
	 * @param username The user's name
	 * @return 
	 */
	public int logout( String username ) {
		
		// Call a reusable method to validate the username 
		int validateParams = validateUsername( username );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		

		try {
			
			String findQuery = "SELECT * FROM " + playersTable + " WHERE username=? AND \"loggedIn\"=?;";
			// statement must be scrollable, otherwise we can't go back to the first row in the resultset
			PreparedStatement findUser = connection.prepareStatement( findQuery , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
			findUser.setString( 1 , username );
			findUser.setBoolean( 2 , true );
			
			
			ResultSet found = findUser.executeQuery();
	  
		 
			// If there are no matching results, this means the username is not in the database
			int count = 0;
			while( found.next() ) {
				count++;
				if( count > 1 ) {
					break; // no need to continue further
				}
			}
			if( count == 0 ) {
				return -2;
			}
			
			// Go back to the first row in the result set after checking the count
			found.first();
			 
			String updateQuery = "UPDATE " + playersTable + " SET \"loggedIn\" = ? WHERE id = ?;";
			
			PreparedStatement updateUser = connection.prepareStatement( updateQuery );
			updateUser.setBoolean( 1 , false );
			updateUser.setInt( 2 , found.getInt( 1 ) ); // !IMPORTANT! id must be the first column, otherwise index 0 will correspond to something else
			updateUser.executeUpdate();
			
			return 1;
			 
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return -5;
			
		}  
		
	}
	
	protected void logoutAll() {
	
		try {
			
			String updateQuery = "UPDATE " + playersTable + " SET \"loggedIn\" = ?;";
			
			PreparedStatement updateUser = connection.prepareStatement( updateQuery );
			updateUser.setBoolean( 1 , false );
			updateUser.executeUpdate();

			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			
		}  
		
	}
	
	/**
	 * Update a player's stats property 
	 * 
	 * @param username The player's username
	 * @param property The property to update
	 * @param change The value by which to increment/decrement the current value
	 * @return 1 If successful, anything below that is a failure.
	 */
	protected int updateStats( String username , String property , int change ) {
		
		// Call a reusable method to validate the username 
		int validateParams = validateUsername( username );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		
		try {
			
			if( !property.equals("gamesPlayed") && !property.equals("gamesFinished") && !property.equals("gamesWon") && !property.equals("totalCoins") && !property.equals("totalPoints") ) {
				return -3;
			}
				
			String updateQuery = "UPDATE " + playersTable + " SET \""+property+"\" = \""+property+"\" + ? WHERE username = ?;";
			
			PreparedStatement updateUser = connection.prepareStatement( updateQuery );
			updateUser.setInt( 1 , change );
			updateUser.setString( 2 , username );
			updateUser.executeUpdate();
			
			return 1;
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return -4;
			
		} 
		
	}
	
	protected int updateStats( String username , int dWon , int dFinished , int dCoins , int dPoints ) {
		
		// Call a reusable method to validate the username 
		int validateParams = validateUsername( username );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		
		try {
			 
			String updateQuery = "UPDATE " + playersTable + " SET \"gamesWon\" = \"gamesWon\" + ?, \"gamesFinished\" = \"gamesFinished\" + ?, \"totalCoins\" = \"totalCoins\" + ?, \"totalPoints\" = \"totalPoints\" + ? WHERE username = ?;";
			
			PreparedStatement updateUser = connection.prepareStatement( updateQuery );
			updateUser.setInt( 1 , dWon );
			updateUser.setInt( 2 , dFinished );
			updateUser.setInt( 3 , dCoins );
			updateUser.setInt( 4 , dPoints );
			updateUser.setString( 5 , username );
			updateUser.executeUpdate();
			
			return 1;
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return -4;
			
		} 
		
	}
	
	protected int insertScore( String username , String level , int mazeSize , int completionTime , int finishPosition , int score , int coinCount ) {
		
		// Call a reusable method to validate the username 
		int validateParams = validateUsername( username );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return validateParams;
		}
		
		try {
			 
			String updateQuery = "INSERT INTO " + scoresTable + " ( username , level , mazesize , completiontime , finishposition , score , coins , date ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? )";
			
			Timestamp time = new Timestamp( System.currentTimeMillis() );
			
			PreparedStatement updateUser = connection.prepareStatement( updateQuery );
			updateUser.setString( 1 , username );
			updateUser.setString( 2 , level );
			updateUser.setInt( 3, mazeSize );
			updateUser.setInt( 4 , completionTime );
			updateUser.setInt( 5 , finishPosition );
			updateUser.setInt( 6, score );
			updateUser.setInt(7 , coinCount );
			updateUser.setTimestamp( 8 , time );
			updateUser.executeUpdate();
			
			return 1;
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return -4;
			
		} 
		
	}
	
	protected String[] getUserData( String username ) {
		
		// Call a reusable method to validate the username 
		int validateParams = validateUsername( username );
		
		// If all is OK 1 is returned. Anything below that value is a problem.
		if( validateParams < 1 ) {
			return null;
		}
		
		try {
			 
			String findQuery = "SELECT * FROM " + playersTable + " WHERE username=?;";
			// statement must be scrollable, otherwise we can't go back to the first row in the resultset
			PreparedStatement findUser = connection.prepareStatement( findQuery , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
			findUser.setString( 1 , username );
			 
			ResultSet found = findUser.executeQuery();
	  
		 
			// If there are no matching results, this means the username is not in the database
			int count = 0;
			while( found.next() ) {
				count++;
				if( count > 1 ) {
					break; // no need to continue further
				}
			}
			if( count == 0 ) {
				return null;
			}
			
			// Go back to the first row in the result set after checking the count
			found.first();
			
			
			int totalcoins = found.getInt( 6 );
			int played = found.getInt( 7 );
			int finished = found.getInt( 8 );
			int won = found.getInt( 9 );
			int totalscore = found.getInt(10);
			double wlRatio;
			if( finished > 0 ) {
				wlRatio = (double) won / finished;
			} else {
				wlRatio = 0.0;
			}
			
			String stats = ""+played+","+finished+","+won+","+wlRatio+","+totalcoins+","+totalscore; 
			
			
			 
			
			
			String scoreQuery = "SELECT * FROM " + scoresTable + " WHERE username=? ORDER BY date DESC LIMIT 1;";
			// statement must be scrollable, otherwise we can't go back to the first row in the resultset
			PreparedStatement findScore = connection.prepareStatement( scoreQuery , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
			findScore.setString( 1 , username );
			 
			found = findScore.executeQuery();
			
			count = 0;
			while( found.next() ) {
				count++;
				if( count > 1 ) {
					break; // no need to continue further
				}
			}
			if( count == 0 ) {
				return new String[] { stats };
			}
			
			// Go back to the first row in the result set after checking the count
			found.first();
			
			String level = found.getString(3);
			int mazesize = found.getInt(2);
			int completiontime = found.getInt(4) / 1000;
			int finishposition = found.getInt(5);
			int score = found.getInt(6);
			int coins = found.getInt(7);
			Timestamp time = found.getTimestamp( 8 );
			
			String last = level+","+mazesize+","+finishposition+","+completiontime+","+coins+","+score+","+time;
					
			return new String[] { stats , last };
			
		} catch ( SQLException e ) {
		
			e.printStackTrace();
			return null;
			
		}
		
	}
	
	protected String getTopScores( int type ) {
		
		String query;
		int field = 0;
		
		if( type == 0 ) {
			
			field = 6;
			query = "SELECT * FROM " + playersTable + " ORDER BY \"totalCoins\" DESC LIMIT 5";
			
		} else if( type == 1 ) {
			
			field = 10;
			query = "SELECT * FROM " + playersTable + " ORDER BY \"totalPoints\" DESC LIMIT 5";
			
		} else if( type == 2 ) {
			
			field = 9;
			query = "SELECT * FROM " + playersTable + " ORDER BY \"gamesWon\" DESC LIMIT 5";
			
		} else if( type == 3 ) {
			
			query = "SELECT * FROM " + playersTable + " ORDER BY COALESCE( CAST( \"gamesWon\" AS FLOAT ) / NULLIF( \"gamesFinished\" , 0 ) , 0 ) DESC LIMIT 5";
			
		} else {
			return null;
		}
		
		String result = "";
		
		try {
			
			// statement must be scrollable, otherwise we can't go back to the first row in the resultset
			PreparedStatement find = connection.prepareStatement( query , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
			ResultSet found = find.executeQuery();
			
			int count = 0;
			while( found.next() ) {
				
				
				result += found.getString(2) + ",";
				if( type == 3 ) {
				
					int finished = found.getInt( 8 );
					int won = found.getInt( 9 );
					double wlRatio;
					if( finished > 0 ) {
						wlRatio = (double) won / finished;
					} else {
						wlRatio = 0.0;
					}
					result += wlRatio;
					
				} else {
					
					result += found.getInt( field );
					
				}
				
				if( !found.isLast() ) {
					result += "|";
				}
				
				count++;
				
			}
			
			if( count == 0 ) {
				return null;
			}
			  
			return result;
		
		} catch ( SQLException e ) {
			
			e.printStackTrace();
			return null;
			
		}
		
	}
	

}