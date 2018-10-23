package client;


import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.util.Random;
import java.util.HashMap;

/**
 * 
 * @author Iliya Liksov
 *
 */
public class ImageLibrary {

	private HashMap< String , BufferedImage > images;
	
	public ImageLibrary() {
		
		images = new HashMap< String , BufferedImage >();
		
	}
	
	/**
	 * Load an image in this library and give it a key reference
	 * 
	 * @param key The key reference 
	 * @param filename The path to the file
	 * @return True if loading successful , False otherwise
	 */
	public boolean load( String key , String filename ) {
		
		try {
			
			BufferedImage file = ImageIO.read( new File( filename ) );
			
			images.put( key , file );
			
			return true;
			
		} catch( IOException e ) {
			
			return false;
			
		}
		
	}
	
	/**
	 * Simple static method to load an image directly
	 * @param filename The file path and name
	 * @return The BufferedImage object
	 */
	public static BufferedImage load( String filename ) {
		
		try {
			
			BufferedImage file = ImageIO.read( new File( filename ) );
			
			return file;
			
		} catch( IOException e ) {
			
			return null;
			
		}
		
	}
	
	/**
	 * Remove an image from the library
	 * 
	 * @param key The image's key reference 
	 * @return True if successful, False otherwise (if the key doesn't exist)
	 */
	public boolean remove( String key ) {
		
		if( !images.containsKey( key ) ) {
			return false;
		}
		
		images.remove( key );
		
		return true;
		
	}
	
	/**
	 * Clear image library
	 */
	public void removeAll() {
		 
		images.clear();
		
	}
	
	/**
	 * Get an image from the library
	 * 
	 * @param key The image's key reference to check
	 * @return The BufferedImage reference to the image 
	 * @throws NullPointerException If the image does not exist in the library
	 */
	public BufferedImage getImage( String key ) {
		 
		if( !images.containsKey( key ) ) {
			throw new NullPointerException("This image does not exist in the library");
		}
		
		return images.get( key );
		
	}
	
	/**
	 * See if an image exists in the library
	 * 
	 * @param key The image's key reference to check
	 * @return True if the image exists, False otherwise
	 */
	public boolean imageExists( String key ) {
		
		return images.containsKey( key );
		
	}
	
	// --------------------------------------------- STATIC METHODS ---------------------------------------------------
	
	/**
	 * Return part of a spritesheet (a frame)
	 * 
	 * Used in cases where you have an animated character, etc and you have several frames for this character in one texture file.
	 * Each frame is equally spaced out from its neighbors making it easy to read.
	 * 
	 * NOTE! All frames must have the same dimensions, otherwise errors will occur.
	 * 
	 * @param spriteSheet The sprite sheet BufferedImage
	 * @param xGrid The x frame
	 * @param yGrid The y frame
	 * @param tileSizeX The width of each frame in the sprite sheet
	 * @param tileSizeY The height of each frame in the sprite sheet
	 * @return The cropped frame (BufferedImage)
	 */
	public static BufferedImage getSpriteFrame( BufferedImage spriteSheet , int xGrid, int yGrid , int tileSizeX , int tileSizeY ) {
		
		int w = spriteSheet.getWidth();
		int h = spriteSheet.getHeight();
		
		if( tileSizeX > w ) {
			tileSizeX = w;
		}
		
		if( tileSizeY > h ) {
			tileSizeY = h;
		}
		
		int sx = xGrid * tileSizeX;
		int sy = yGrid * tileSizeY;
		
		if( sx >= w || sy >= h ) {
			
			sx = 0;
			sy = 0;
			
		}
		
        return spriteSheet.getSubimage( sx , sy , tileSizeX , tileSizeY );
        
    }
	
	/**
	 * Generate a random integer in a specified range 
	 * 
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return The random integer ranging from min to max
	 */
	public static int randInt(int min, int max) {
 
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	
}
