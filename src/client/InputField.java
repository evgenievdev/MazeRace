package client;

/**
 * 
 * @author Iliya Liksov
 *
 */
public class InputField {
	
	// Special permitted characters used by field type 4
	private static final char[] special = new char[] {
		'-',
		'_',
		'.'
	};
	
	public final String label;
	
	public final int maxChars;
	public final int type; // 0 - any input , 1 - numbers , 2 - letters , 3 - alphanumeric , 4 - alphanumeric + special
	
	private String value;
	
	 
	
	public InputField( String label , int maxChars , int type ) {
		
		this.label = label;
		this.maxChars = ( maxChars > 0 ) ? maxChars : 1;
		this.type = ( type < 0 || type > 4 ) ? 0 : type;
		this.value = "";
		
	}
	
	/**
	 * Get the current value from this input field
	 * 
	 * @return The input field's value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Get the current number of characters in the input
	 * @return The number of entered characters so far in this input field
	 */
	public int getValueLength() {
		return value.length();
	}
	
	/**
	 * Add a character to this input field's value
	 * 
	 * @param key The character to add
	 * @return 1 if successful , 0 - max characters reached (fail) , -1 to -4 if the input char is invalid for the field type.
	 */
	public int addChar( char key ) {
		 
		if( value.length() >= maxChars ) {
			
			return 0;
			
		}
		
		boolean isDigit = Character.isDigit( key );
		boolean isLetter = Character.isLetter( key );
		
		if( type == 1 && !isDigit ) {
			
			return -1;
			
		}
		
		if( type == 2 && !isLetter ) {
			
			return -2;
			
		}
		
		if( type == 3 && !isDigit && !isLetter ) {
			
			return -3;
			
		}
		
		if( type == 4 && !isDigit && !isLetter && !inArray( special , key ) ) {
			
			return -4;
		
		}
		
		value += key;
		
		return 1;
		
	}
	
	/**
	 * Check to see if a character is a valid type which can be added to this input field's value
	 * 
	 * @param key The char to check
	 * @return True if is valid, false otherwise.
	 */
	public boolean isValidChar( char key ) {
		
		boolean isDigit = Character.isDigit( key );
		boolean isLetter = Character.isLetter( key );
		
		if( type == 1 && !isDigit ) {
			
			return false;
			
		}
		
		if( type == 2 && !isLetter ) {
			
			return false;
			
		}
		
		if( type == 3 && !isDigit && !isLetter ) {
			
			return false;
			
		}
		
		if( type == 4 && !isDigit && !isLetter && !inArray( special , key ) ) {
			
			return false;
		
		}
		
		return true;
		
	}
	
	/**
	 * Remove the last character from an input field
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean removeLastChar() {
		  
		if( value.length() == 0 ) {
			
			return false;
			
		}
		 
		value = value.substring( 0 , value.length() - 1 );
		
		return true;
		
	}
	
	/**
	 * Check if a character is contained in an array of characters (internal method used by addChar() )
	 * 
	 * @param arr The array of characters
	 * @param key The character to search for
	 * @return true if character is found, false otherwise
	 */
	private static boolean inArray( char[] arr , char key ) {
		
		for( char c : arr ) {
			
			if( c == key ) {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
}
