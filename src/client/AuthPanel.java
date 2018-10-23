package client;
 
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.Cursor;
import java.awt.event.KeyEvent; 
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import protocol.Protocol;

@SuppressWarnings("serial")
public class AuthPanel extends JPanel {
 
	private final Client client;
	
	private static final Cursor handCursor = new Cursor( Cursor.HAND_CURSOR );
	private static final Cursor defaultCursor = new Cursor( Cursor.DEFAULT_CURSOR );
	
	public final ImageLibrary imageLibrary;
	public final AudioLibrary audioLibrary;
	
	private Color primaryColor, secondaryColor, msgColor , errorColor, okColor;
	private Font buttonFont, msgFont, outlineFont, copyrightFont, labelFont, smallFont;
	
	private int activeInputField;
	
	private boolean msgVisible;
	private String message;
 
	private InputField[] fields;

	// Buttons (Login/Register)
	private boolean loginActive, registerActive;
	private boolean loginHover , registerHover;
	private boolean usernameHover , passwordHover;
	
	private String loginLabel, loginDefaultText, loginWaitText,
					registerLabel , registerDefaultText , registerWaitText;
	
	private int loginX, loginY, loginW, loginH = 0;
	private int registerX , registerY, registerW, registerH = 0;
	private int usernameFieldX, usernameFieldY, usernameFieldW, usernameFieldH = 0;
	private int passwordFieldX , passwordFieldY , passwordFieldW , passwordFieldH = 0;
	  
	protected final KeyListener keyListener;
	protected final MouseAdapter mouseAdapter;
	protected final MouseMotionListener mouseMotionListener;
	
	protected boolean enableInput = true;
	
	/**
	 * Constructor for LoginPanel class.
	 * Creates a JPanel instance and loads the necessary fonts/images/sounds for this screen.
	 */
	public AuthPanel( Client client ) {
		
		this.client = client;
		  
		this.setBackground( GUIConstants.screenColor );
		 
		
		// Set up colors
		primaryColor = Color.WHITE;
		secondaryColor = new Color( 30 , 30 , 30 );
		errorColor = new Color( 155 , 20 , 20 );
		okColor = new Color( 10 , 90 , 50 );
		
		// Set up fonts
		buttonFont = FontLibrary.newFont( "VCR OSD Mono", 21, false, false );
		msgFont = FontLibrary.newFont( "VCR OSD Mono", 18, false, false );
		outlineFont = FontLibrary.newFont( "VCR OSD Mono", 50 , false, false );
		copyrightFont = FontLibrary.newFont( "VCR OSD Mono", 16 , false, false );
		smallFont = FontLibrary.newFont( "VCR OSD Mono", 12 , false, false );
		labelFont = FontLibrary.newFont( "VCR OSD Mono", 20 , false, false );
		
		// Image library
		imageLibrary = new ImageLibrary();
		
		imageLibrary.load( "coin" , GUIConstants.markersPath + "coin.png" );
		imageLibrary.load( "logo" , GUIConstants.menusPath + "logo.png" );
		
		// Audio library
		audioLibrary = new AudioLibrary();
		
		audioLibrary.load( "switch", GUIConstants.soundsPath + "switch.wav" );
		audioLibrary.load( "type", GUIConstants.soundsPath + "type.wav" );
		audioLibrary.load( "invalid", GUIConstants.soundsPath + "invalid.wav" );
		audioLibrary.load( "error", GUIConstants.soundsPath + "error.wav" );
		audioLibrary.load( "ok", GUIConstants.soundsPath + "ok.wav" );
		audioLibrary.load( "select", GUIConstants.soundsPath + "select.wav" );
		
		msgVisible = false;
		message = "";
		activeInputField = 0;

		
		loginActive = registerActive = true;
		loginHover = registerHover = false;
		usernameHover = passwordHover = false;
		
		loginDefaultText = "LOGIN";
		loginWaitText =    "*WAIT*";
		loginLabel = loginDefaultText;
		
		registerDefaultText = "REGISTER";
		registerWaitText =    " *WAIT* ";
		registerLabel = registerDefaultText;
		
		// Input fields
		fields = new InputField[] {
			new InputField( "USERNAME" , 32 , 3 ),
			new InputField( "PASSWORD" , 32 , 3 )
		};
		 
		// Build event listeners (only done once)
		mouseAdapter = buildMouseAdapter(); 
		keyListener = buildKeyListener();
		mouseMotionListener = buildMouseMotionListener();

		addListeners();
		
		// NECESSARY FOR INPUT TO WORK!!!!!!
		this.setFocusable(true);
		
	}
	 
	
	protected void addListeners() {
		
		// Mouse clicked events
		this.addMouseListener( mouseAdapter );
		// Register any cursor movement and update button hovers
		this.addMouseMotionListener( mouseMotionListener );
		// Key input listener used to switch input fields, send a request or enter characters into the input fields
		this.addKeyListener( keyListener );
		
	}
	
	/**
	 * Mouse motion listener - detects mouse movement and changes input field/buttons HOVER properties.
	 * 
	 * @return The new instance of the mouseMotionListener
	 */
	private MouseMotionListener buildMouseMotionListener() {
		
		return new MouseMotionListener() {

		    @Override
		    public void mouseMoved(MouseEvent e) {
		    	
		    	if( !enableInput ) { return; }
		    	
		    	// Mouse position currently (event fired on every move of the mouse)
		        final int x = e.getX();
		        final int y = e.getY();
		        
		        // Mouse is within the bounds of a button
		        boolean login = inLoginButton( x , y );
		        boolean register = inRegisterButton( x , y );
		        
		        // Mouse is within the bounds of an input field
		        boolean username = inUsernameField( x , y );
           	 	boolean password = inPasswordField( x , y );
		        
           	 	
           	 	// If the mouse is over the username input field but it is not active, set it to active
	           	if( username && !password && activeInputField != 0 && !login && !register ) {
	           		 
	           		 if( !usernameHover ) {
	           			 
	           			 usernameHover = true;
	           			 setCursor( handCursor );
	           			 
	           		 }
	           		 
	            }
	           	// Reset the hover variable if the mouse is not over the username field
	           	if( !username && usernameHover ) { usernameHover = false; }
	           	
           	   // If the mouse is over the password input field but it is not active, set it to active
           	   if( !username && password && activeInputField != 1  && !login && !register ) {
           		 
           		  if( !passwordHover ) {
          			 
           			 passwordHover = true;
          			 setCursor( handCursor );
          			 
          		  }
           		 
           	   }
           	   // Reset hover for password field
           	   if( !password && passwordHover ) { passwordHover = false; }
           	   
           	   if( !passwordHover && !usernameHover && !login && !register  ) { 
           		   setCursor( defaultCursor );
           	   }
           	 	
           	 	
           	 	// If the login button is NOT disabled and the mouse is hovering over it
		        if( loginActive ) {
		        	
		        	if( login && !loginHover ) {
			        	
		        		loginHover = true;
			        	repaint();
			    		setCursor( handCursor );
			        	
			        } else if( !login && loginHover ){
			        	
			        	loginHover = false;
			        	repaint();
			        	setCursor( defaultCursor );
			        	
			        }
		        	
		        }
		        
		        // If the register button is NOT disabled and the mouse is hovering over it
		        if( registerActive ) {
		        	
			        if( register && !registerHover ) {
			        	
			        	registerHover = true;
			        	repaint();
			        	setCursor( handCursor );
			        	
			        } else if( !register && registerHover ){
			        	
			        	registerHover = false;
			        	repaint();
			        	setCursor( defaultCursor );
			        	
			        }
		        
		        }
 
		        
		    }

		    @Override
		    public void mouseDragged(MouseEvent e) {}
		    
		};
		
	}
	
	/**
	 * Create a key listener which takes in user input and adds/removes from an active input field.
	 * It also is used to switch active input fields and trigger login/register request methods.
	 * 
	 * @return The new instance of the KeyListener
	 */
	private KeyListener buildKeyListener() {
		
		return new KeyListener() {
			 
			private boolean ctrl, enter, pressed = false;

			@Override
			public void keyTyped( KeyEvent e ) {}

			@Override
			public void keyPressed( KeyEvent e ) {
				
				if( !enableInput ) { return; }
				
				InputField field = fields[ activeInputField ];
				
				int key = e.getKeyCode();
				char keyChar = e.getKeyChar();
				//String text = KeyEvent.getKeyText( key );
		 
				if( key == KeyEvent.VK_CONTROL && ctrl == false ) {
					
					ctrl = true;
					 
					nextInputField();
					
				}  
				
				// Delete string
				if( key == KeyEvent.VK_BACK_SPACE ) {
					
					boolean removed = field.removeLastChar();
					
					if( removed ) {
						
						clearMessage();
						
					} else {
						
						if( pressed == false ) {
						
							setMessage("THIS FIELD IS EMPTY!");
							audioLibrary.play("invalid");
							pressed = true;
							
						}
						
					}
					
				}
				
				// Submit login
				if( key == KeyEvent.VK_ENTER && enter == false ) {
					
					//audioLibrary.play("select");
					sendLoginRequest();
					repaint();
					enter = true;
					
				}
				
				// Add input to an active field
				boolean write = true;
				// IF any of the control keys are pressed, do not continue otherwise a contradiction is created in the isValidChar method.
				// This makes sure that the GUI doesn't render an error message every time you press any of those control keys (CTRL, SHIFT, ENTER, etc)
				if( key == KeyEvent.VK_CONTROL   || 
					key == KeyEvent.VK_SHIFT     || 
					key == KeyEvent.VK_BACK_SPACE || 
					key == KeyEvent.VK_ENTER 	  ||
					key == KeyEvent.VK_CAPS_LOCK
				) {
					write = false;
				}
				
				// Add user input to the active input field
				if( write == true ) {
					
					// Check if the character typed is acceptable for the input field type
					boolean isValid = field.isValidChar( keyChar );
					
					if( isValid ) {
						
						// Attempt to add the character to the input field
						int result = field.addChar( keyChar );
						
						// 1 means that the character has been added
						if( result == 1 ) {
							
							clearMessage();
							audioLibrary.play("type");
						
					    // Anything else means that the character hasn't been added, but the reasons could differ
						} else {
							
							// Only trigger the error message and sound event once
							if( pressed == false ) {
								
								// result == 0 means that the character limit for the input field has been reached
								if( result == 0 ) {
									setMessage("YOU HAVE REACHED THE MAXIMUM AMOUNT OF CHARACTERS ("+field.maxChars+")");
								}
								
								audioLibrary.play("invalid");
								pressed = true;
								
							}
							
						}
					
					// If the character is not a valid character for this input field
					} else {
						
						// Send an error message and render it once to the GUI
						if( pressed == false ) {
							
							setMessage("THIS KEY ["+keyChar+"] IS NOT ALLOWED!");
							
							audioLibrary.play("invalid");
							pressed = true;
							
						}
						
					}
			 
				 
				}
				 
			 
				
			}
			
			// Key release events - makes sure certain keys need to be released and then pressed again before registering their next event
			// That way you prevent flooding the server and GUI with requests
			@Override
			public void keyReleased( KeyEvent e ) {
				
				if( !enableInput ) { return; }
				
				int key = e.getKeyCode();
				
				if( key == KeyEvent.VK_CONTROL && ctrl == true ) {
					
					ctrl = false;
					
				}  
				
				if( key == KeyEvent.VK_ENTER && enter == true ) {
					
					enter = false;
					
				}
				
				pressed = false;
				
			}
			
			
		};
		
	}
	
	/**
	 * Mouse Adapter used to detect clicks of the mouse. If the user clicks over a certain element,
	 * such as an input field, it becomes active. The user can also click over buttons, which triggers their respective request methods.
	 * 
	 * @return The new MouseAdapter instance.
	 */
	private MouseAdapter buildMouseAdapter() {
		
		return new MouseAdapter() {
			
			@Override
            public void mouseClicked( MouseEvent e ) {
				
				 if( !enableInput ) { return; }
				
				 // Mouse position at the time of the click
            	 final int x = e.getX();
            	 final int y = e.getY();
            	 
            	 // Mouse is within any of the buttons 
            	 boolean login = inLoginButton( x , y );
            	 boolean register = inRegisterButton( x , y );
            	 
            	 // Mouse is within any of the input fields
            	 boolean username = inUsernameField( x , y );
            	 boolean password = inPasswordField( x , y );
            	 
            	 // If the mouse is over the login button and the button is NOT disabled => send login request
            	 if( login && loginActive ) {
            		 //audioLibrary.play("select");
 					 sendLoginRequest();
 					 repaint();
            		 setCursor( defaultCursor );
            	 }
            	 
            	 // If the mouse is over the register button adn the button is NOT disabled => send register request
            	 if( register && registerActive ) {
            		 sendRegisterRequest();
            		 repaint();
            		 setCursor( defaultCursor );
            		 
            	 }
            	 
            	 // If the mouse is over the username input field but it is not active, set it to active
            	 if( username && !password && activeInputField != 0 ) {
            		 
            		 nextInputField();
            		 setCursor( defaultCursor );
            		 
            	 }
            	 // If the mouse is over the password input field but it is not active, set it to active
            	 if( !username && password && activeInputField != 1 ) {
            		 
            		 nextInputField();
            		 setCursor( defaultCursor );
            		 
            	 }
            	 
            }
            
            //public void mouseEntered( MouseEvent e ) {}
            //public void mouseExited( MouseEvent e ) {}
            //public void mousePressed( MouseEvent e ) {}
            //public void mouseReleased( MouseEvent e ) {}
            
        };
		
	}
	
	
	/**
	 * Dummy method called every time the login button is pressed
	 * Use this method to send requests to the server, etc
	 */
	private void checkLogin() {
		
		String user = fields[ 0 ].getValue().trim();
		String pass = fields[ 1 ].getValue().trim();
		
		if( user.length() == 0 || pass.length() == 0 ) {
			
			audioLibrary.play("invalid");
			setMessage("USERNAME AND PASSWORD CAN NOT BE EMPTY!");
			restoreLogin();
			enableRegister();
			return;
			
		} else {
			
			ClientApp.frame.client.start();
			
			// Send to server.
			String[] response = client.signin( user , pass );

			// If authentication is successful [simulation]
			if( response[ 1 ].equals("true") ) {
				
				audioLibrary.play("ok");
				ClientApp.frame.username = user; // Set the local variable to store the username (used for visual purposes only)
				setMessage( response[ 2 ].toUpperCase() , true );
				//restoreLogin();
				//enableRegister();
				enableInput = false;
				
				// Switch to the lobby panel here (BELOW THIS LINE) - keep current connection
				ClientApp.frame.main.message = "WELCOME BACK, " + user + "!" ;
				ClientApp.frame.setMain();
 
				return;
			
			} 
			// If authentication failed
			else {
				
				audioLibrary.play("error");
				setMessage( response[ 2 ].toUpperCase() );
				restoreLogin();
				enableRegister();
				ClientApp.frame.client.stop();
				return;
				
			}
			
		}
		
	}
	
	/**
	 * Dummy method called every time the register button is pressed
	 * Use this method to send requests to the server, etc.
	 */
	private void checkRegister() {
		
		String user = fields[ 0 ].getValue().trim();
		String pass = fields[ 1 ].getValue().trim();
		
		if( user.length() == 0 || pass.length() == 0 ) {
			
			audioLibrary.play("invalid");
			setMessage("USERNAME AND PASSWORD CAN NOT BE EMPTY!");
			restoreRegister();
			enableLogin();
			return;
			
		} else {
			
			ClientApp.frame.client.start();
			
			// Send to server.
			String[] response = client.signup( user , pass );
			
			// If authentication is successful [simulation]
			if( response[ 1 ].equals("true") ) {
				
				audioLibrary.play("ok");
				setMessage( response[ 2 ].toUpperCase() , true );
				//restoreRegister();
				enableLogin();
				ClientApp.frame.client.stop();
				return;
			
			} 
			// If authentication failed
			else {
				
				audioLibrary.play("error");
				setMessage( response[ 2 ].toUpperCase() );
				restoreRegister();
				enableLogin();
				ClientApp.frame.client.stop();
				return;
				
			}
			
		}
		
	}
	
	/**
	 * Check if a mouse coordinate on the screen is within the boundaries of the LOGIN button
	 * 
	 * @param x Mouse coordinate x
	 * @param y Mouse coordinate y
	 * @return True if it is within bounds, false otherwise
	 */
	private boolean inLoginButton( int x , int y ) {
		
		 if( x >= loginX && x <= loginX+loginW && y >= loginY && y <= loginY+loginH ) {
     		
    		 return true;
    		 
    	 } 
		 
		 return false;
		
	}
	
	/**
	 * Check if a mouse coordinate on the screen is within the boundaries of the REGISTER button
	 * 
	 * @param x Mouse coordinate x
	 * @param y Mouse coordinate y
	 * @return True if it is within bounds, false otherwise
	 */
	private boolean inRegisterButton( int x , int y ) {
		
		if( x >= registerX && x <= registerX+registerW && y >= registerY && y <= registerY+registerH ) {
     		
   			return true;
   		 
   		} 
		
		return false;
		
	}
	
	/**
	 * Check if a mouse coordinate on the screen is within the boundaries of the USERNAME FIELD
	 * 
	 * @param x Mouse coordinate x
	 * @param y Mouse coordinate y
	 * @return True if it is within bounds, false otherwise
	 */
	private boolean inUsernameField( int x , int y ) {
		
		if( x >= usernameFieldX && x <= usernameFieldX+usernameFieldW && y >= usernameFieldY && y <= usernameFieldY+usernameFieldH ) {
     		
   			return true;
   		 
   		} 
		
		return false;
		
	}
	
	/**
	 * Check if a mouse coordinate on the screen is within the boundaries of the PASSWORD FIELD
	 * 
	 * @param x Mouse coordinate x
	 * @param y Mouse coordinate y
	 * @return True if it is within bounds, false otherwise
	 */
	private boolean inPasswordField( int x , int y ) {
		
		if( x >= passwordFieldX && x <= passwordFieldX+passwordFieldW && y >= passwordFieldY && y <= passwordFieldY+passwordFieldH ) {
     		
   			return true;
   		 
   		} 
		
		return false;
		
	}
	
	/**
	 * Set the flag for the login button as active
	 */
	private void enableLogin() {
		loginActive = true;
	}
	
	/**
	 * Set the flag for the login button as disabled and also nullify the Hover flag (necessary to avoid glitches)
	 */
	private void disableLogin() {
		loginActive = false;
		loginHover = false;
	}
	
	/**
	 * Restore the original text for the login button and make it clickable again
	 */
	protected void restoreLogin() {
		loginLabel = loginDefaultText;
		enableLogin();
	}
	
	/**
	 * Set the text for the login button to show that something is happening (waiting for server request for example)
	 * Also disable it so it can't be clicked until it is enabled again
	 */
	private void setLoginWait() {
		loginLabel = loginWaitText;
		disableLogin();
	}
	
	/**
	 * This method is called every time the LOGIN button is clicked (or ENTER is pressed, same thing)
	 * It makes the login (also making it change text) and register buttons disabled and calls the dummy checkLogin method().
	 */
	private void sendLoginRequest() {
		
		setLoginWait();
 
		disableRegister();
		 
		checkLogin();
		
	}
	
	/**
	 * This method is called every time the REGISTER button is clicked 
	 * It makes the login and register (also making it change text) buttons disabled and calls the dummy checkRegister method().
	 */
	private void sendRegisterRequest() {
		
		setRegisterWait();
		
		disableLogin();
		
		checkRegister();
		
	}
	
	/**
	 * Disable the register button and reset its hover state
	 */
	private void disableRegister() {
		registerActive = false;
		registerHover = false;
	}
	
	/**
	 * Enable the register button
	 */
	private void enableRegister() {
		registerActive = true;
	}
	
	/**
	 * Restore the register button to its original state - enabled and with its default text
	 */
	protected void restoreRegister() {
		registerLabel = registerDefaultText;
		enableRegister();
	}
	
	/**
	 * Make the registered button disabled and change its text to indicate that something is happening (e.g. waiting for a server request)
	 */
	private void setRegisterWait() {
		registerLabel = registerWaitText;
		disableRegister();
	}
	
	 
	/**
	 * Go to the next input field in the fields array (set it as active)
	 * If there is no next one, it loops back to the beginning
	 */
	private void nextInputField() {
		
		if( fields == null ) {
			return;
		}
		
		activeInputField++;
		
		if( activeInputField >= fields.length ) {
			activeInputField = 0;
		}
		
		audioLibrary.play("switch");
		
		this.repaint();
		
	}
	
	/**
	 * Set the message (shown above the login panel) and repaint the screen to make it visible
	 * 
	 * @param text The text for the messasge 
	 * @param isSuccess If set to true, the text will be colored green, otherwise it will be red
	 */
	protected void setMessage( String text , boolean isSuccess ) {
		
		if( isSuccess ) {
			msgColor = okColor;
		} else {
			msgColor = errorColor;
		}

		message = text;
		showMessage();	
		
	}
	
	/**
	 * Set the message (shown above the login panel) and repaint the screen to make it visible. By default the text will be red
	 * @param text The text for the message
	 */
	protected void setMessage( String text ) {

		setMessage( text , false );
		
	}
	
	/**
	 * Hide the current message
	 */
	protected void hideMessage() {
		msgVisible = false;
		this.repaint();
	}
	
	/**
	 * Show the current message
	 */
	protected void showMessage() {
		msgVisible = true;
		this.repaint();
	}
	
	/**
	 * Clear the current message
	 */
	protected void clearMessage() {
		message = "";
		hideMessage();
	}
	
	/**
	 * Static method used to convert a string to "*" characters only (for password fields)
	 * @param password The password string to convert
	 * @return The equivalent in length string replaced with * chars
	 */
	private static String hidePassword( String password ) {
		
		int len = password.length();
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0 ; i < len; i++){
		    sb.append('*');
		}
		return sb.toString();
		
	}
	
	/**
	 * Draw the GUI panel
	 */
	public void paintComponent( Graphics g ) {
		
		super.paintComponent( g );
		
		// Get the dimensions of the panel
		int sw = this.getWidth();
		int sh = this.getHeight();
		
		// Logo image
		int logoW = 0;
		int logoH = 0;
		int logoPaddingY = 20;
		
		if( imageLibrary.imageExists("logo") ) {
			
			BufferedImage logo = imageLibrary.getImage("logo");
			
			logoW = logo.getWidth();
			logoH = logo.getHeight();
			
			// Scale the image down with the screen if the screen is too small for the original image
			if( logoW > sw ) {
				 
				float logoAspectRatio = (float) logoW / logoH;
			 
				logoW = sw;
				logoH = (int) (logoW / logoAspectRatio ); 
				
			}
			
			g.drawImage( logo , sw/2 - logoW/2 , logoPaddingY , logoW , logoH , null );
			
		}
		
		// Draw Message (if there is any)
		int errorPaddingY = 0;
		
		g.setFont( msgFont );
		g.setColor( msgColor );
		
		FontMetrics fm = g.getFontMetrics();
		
		// These are defined even if there isn't an error message intentionally! They are used in the code below to maintain the same alignment with or without the message
		int errorWidth = fm.stringWidth( message );
		int errorHeight = fm.getHeight();
		
		if( msgVisible ) {
	
			g.drawString( message , sw/2 - errorWidth/2 , logoH + logoPaddingY + errorPaddingY );
			
		}
		
		// Draw outline box
		g.setFont( outlineFont );
		g.setColor( secondaryColor );
		fm = g.getFontMetrics();

		String line =  "*******************";
		String line2 = "*";
		
		int line2Width = fm.stringWidth( line2 );
		int lineWidth = fm.stringWidth( line );
		int lineHeight = (int)( fm.getHeight() / 1.5 );
		int outlineSX = sw/2 - lineWidth/2;
		int outlineSY = logoH + logoPaddingY + errorPaddingY + errorHeight + 40;
		
		// Number of line2 rows
		int lineRows = 9;
		// Draw top line of *********************
		g.drawString( line , outlineSX , outlineSY );
		// Draw the lines of *____________________*
		for( int i = 1 ; i < lineRows ; i++ ) {
			
			g.drawString( line2 , outlineSX , outlineSY + lineHeight * i );
			g.drawString( line2 , outlineSX + lineWidth - line2Width , outlineSY + lineHeight * i );
			
		}
		// Draw the bottom line of *******************
		g.drawString( line , outlineSX , outlineSY + lineHeight * lineRows );
		
		// ------------------------------ Draw input fields --------------------------------
		
		// Padding of content inside the box (added left AND right)
		int innerPaddingX = 50;
		
		g.setFont( labelFont );
		fm = g.getFontMetrics();
	
		int labelHeight = fm.getHeight();
		int inputSX = sw/2 - lineWidth/2 + innerPaddingX;
		int inputBorder = 2;
		
		// USERNAME FIELD
		usernameFieldX = inputSX;
		usernameFieldY = outlineSY + labelHeight + lineHeight;
		usernameFieldW = lineWidth - innerPaddingX * 2;
		usernameFieldH = lineHeight;
		
		if( activeInputField == 0 ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		// Draw the label
		g.drawString( fields[ 0 ].label , inputSX , outlineSY + lineHeight );
		// Draw the outline rectangle
		g.fillRect( usernameFieldX , usernameFieldY , usernameFieldW , usernameFieldH );
		// Draw the inner rectangle to create the border effect
		g.setColor( GUIConstants.screenColor );
		g.fillRect( usernameFieldX + inputBorder , usernameFieldY + inputBorder , usernameFieldW - inputBorder*2 , usernameFieldH - inputBorder*2 );
		
		 
		g.setFont( msgFont );
		g.setColor( primaryColor );
		g.drawString( fields[ 0 ].getValue()  , usernameFieldX + 15 , usernameFieldY + 23 );
		
		// PASSWORD FIELD
		passwordFieldX = usernameFieldX;
		passwordFieldY = usernameFieldY + lineHeight * 3;
		passwordFieldW = usernameFieldW;
		passwordFieldH = usernameFieldH;
		
		g.setFont( labelFont );
		if( activeInputField == 1 ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		// Draw the label
		g.drawString( fields[ 1 ].label , sw/2 - lineWidth/2 + 50, outlineSY + lineHeight * 4 );
		// Draw the outline rectangle
		g.fillRect( passwordFieldX , passwordFieldY , passwordFieldW , passwordFieldH );
		// Draw the inner rectangle to create the border effect
		g.setColor( GUIConstants.screenColor );
		g.fillRect( passwordFieldX + inputBorder , passwordFieldY + inputBorder , passwordFieldW - inputBorder*2 , passwordFieldH - inputBorder*2 );
		
		 
		g.setFont( msgFont );
		g.setColor( primaryColor );
		g.drawString( hidePassword( fields[ 1 ].getValue() ) , passwordFieldX + 15 , passwordFieldY + 23 );
		
		
		// LOGIN BUTTON
		int loginOffsetX = 40;
		
		loginW = 150;
		loginH = lineHeight;
		loginX = inputSX + loginOffsetX;
		loginY = outlineSY + labelHeight + lineHeight*5 + 20;
		
		if( loginHover ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		g.fillRect( loginX , loginY , loginW , loginH );
		g.setColor( GUIConstants.screenColor );
		g.fillRect( loginX + inputBorder , loginY + inputBorder , loginW - inputBorder*2 , loginH - inputBorder*2 );
		if( loginActive ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		g.setFont( buttonFont );
		g.drawString( loginLabel , loginX + 43 , loginY + 25 );
		
		
		// REGISTER BUTTON
		int registerOffsetX = 210;
		
		registerW = 180;
		registerH = lineHeight;
		registerX = inputSX + registerOffsetX;
		registerY = outlineSY + labelHeight + lineHeight*5 + 20;
		
		if( registerHover ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		g.fillRect( registerX , registerY , registerW , registerH );
		g.setColor( GUIConstants.screenColor );
		g.fillRect( registerX + inputBorder  , registerY + inputBorder , registerW - inputBorder*2 , registerH - inputBorder*2 );
		if( registerActive ) {
			g.setColor( primaryColor );
		} else {
			g.setColor( secondaryColor );
		}
		g.setFont( buttonFont );
		g.drawString( registerLabel , registerX + 40  , registerY + 25 );
		
		
		g.setFont( smallFont );
		g.setColor( secondaryColor );
		
		// Input instructions
		String instructions = "[CTRL] TOGGLE INPUT FIELD  |  [ENTER] LOGIN  |  [BACKSPACE] REMOVE LAST CHAR";
		g.drawString( instructions , outlineSX , outlineSY  + lineHeight * lineRows + 10 );
		
		g.setFont( copyrightFont );
		
		// Draw copyright
		String copyright = "COPYRIGHT I.E.LIKSOV 2018";
		 
		fm = g.getFontMetrics();
		int copyrightWidth = fm.stringWidth( copyright );
		int copyrightHeight = fm.getHeight();
		
		g.drawString( copyright , sw/2 - copyrightWidth/2 , sh - copyrightHeight - 10 );
		
	}
	
	 
	
}
