package client;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;

/**
 * 
 * @author Iliya Liksov
 *
 */
public class FontLibrary {
	
	private static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	
	private HashMap< String , Font > fonts;
	
	/**
	 * Constructor for font library
	 */
	public FontLibrary() {
		
		fonts = new HashMap< String , Font >();
		
	}
	
	/**
	 * Add a font to the library
	 * 
	 * NOTE! You can combine bold and italic together
	 * 
	 * @param key The key reference for this font
	 * @param name The name of this font (e.g. Arial)
	 * @param size The size of the font in pixels
	 * @param bold True if you want it to be bold
	 * @param italic True if you want it to be italic
	 */
	public void add( String key , String name , int size , boolean bold , boolean italic ) {
		
		Font font = newFont( name , size , bold , italic );
		
		fonts.put( key , font );
		
	}
	
	/**
	 * Check if a font exists in the library
	 * 
	 * @param key The font key
	 * @return True if font exists, False otherwise
	 */
	public boolean fontExists( String key ) {
		
		if( !fonts.containsKey(key) ) {
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * Get a Font instance from the font library
	 * 
	 * @param key The font key
	 * @return The Font instance 
	 * @throws NullPointerException If the font doesnt exist
	 */
	public Font getFont( String key ) {
		
		if( !fonts.containsKey(key) ) {
			throw new NullPointerException("The font ("+key+") doesn't exist in this library");
		}
		
		return fonts.get( key );
		
	}
	
	/**
	 * Register a font file (true-type) to this GraphicsEnvironment
	 * This is a static method which can be accessed from anywhere
	 * 
	 * The font registered can then be used by new Font() with its respective name
	 * 
	 * NOTE! Sometimes the filename doesn't actually correspond to the internal name of the font
	 * You must know this name to use it in new Font()
	 * E.g. the file VCR_OSD_MONO_1.001.ttf is named "VCR OSD Mono" internally
	 * 
	 * @param filename The file path of this font
	 * @return True if loading is successful, False otherwise
	 */
	public static boolean registerFont( String filename ) {
		
		// Attempt to load the custom font
		try {
			 
		     File fontFile = new File( filename );
		     
		     ge.registerFont( Font.createFont( Font.TRUETYPE_FONT, fontFile ) );
		     
		     return true;
		     
		} catch (IOException|FontFormatException e) {
			
		     //Handle exception
			// e.printStackTrace();
			
			return false;
			
		}
		
	}
	
	/**
	 * Generate a new Font instance 
	 * This is a static method which can be used anywhere
	 * 
	 * NOTE! You can have a font that is both bold and italic
	 * 
	 * @param name The name of the font
	 * @param size The size of the font in pixels
	 * @param bold True if you want it to be bold
	 * @param italic True if you want it to be italic
	 * @return The new Font instance
	 */
	public static Font newFont( String name , int size , boolean bold , boolean italic ) {
		
		int type = Font.PLAIN;
		
		if( bold && italic ) {
			type = Font.ITALIC|Font.BOLD;
		} else if( bold && !italic ) {
			type = Font.BOLD;
		} else if( !bold && italic ) {
			type = Font.ITALIC;
		}
		
		return new Font( name , type , size );
		
	}
	
}
