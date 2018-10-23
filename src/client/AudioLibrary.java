package client;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * 
 * @author Iliya Liksov
 *
 */
public class AudioLibrary {

	private HashMap<String,Clip> clips;
	
	private ArrayList<String> sequence;
	private int sequenceCurrentID;
	private boolean sequenceStarted;
 
	/**
	 * Constructor for AudioLibrary class
	 */
	public AudioLibrary() {
		
		clips = new HashMap<String,Clip>();
		sequence = new ArrayList<String>();
		sequenceCurrentID = 0;
		sequenceStarted = false;
		
	}
	
	/**
	 * Get the HashMap of audio clips in this library 
	 * 
	 * @return The audio library's HashMap
	 */
	public HashMap<String,Clip> getClips() {
		
		return clips;
		
	}
	
	/**
	 * Get an audio clip object 
	 * 
	 * @param key The key used to access this audio clip
	 * @return The Clip object reference for this audio clip
	 * @throws NullPointerException If the audio clip does not exist
	 */
	public Clip getClip( String key ) {
		
		if( !clips.containsKey( key ) ) {
			
			throw new NullPointerException("Audio clip doesn't exist in this library");
			
		}
		
		return clips.get( key );
		
	}
	
	public void newSequence( String[] keys ) {
		
		// If there was a sequence previously, stop all clips and clear the queue
		clearSequence();
		
		// Add new items in sequence
		for( String key : keys ) {
			
			// Skip this key if it doesn't exist in the library
			if( !clips.containsKey( key ) ) {
				continue;
			}
			
			sequence.add( key );
			
		}
		
		 
		
	}
	
	/**
	 * Play (continuously) a music sequence
	 * This method must be called in a loop to check if the current clip has finished,
	 * so that it can automatically proceed to the next one. 
	 * 
	 * Preferably, use this when the server sends back a request to the client or whenever the client presses a key.
	 * 
	 * @param loop Set to true if you want the sequence to loop back to the start when it is finished
	 */
	public void playSequence( boolean loop ) {
		
		int size = sequence.size();
		
		if( size == 0 || sequenceCurrentID < 0 ) {
			return;
		}
		
		if( sequenceCurrentID == 0 && !sequenceStarted ) {
			
			Clip first = clips.get( sequence.get( 0 ) );
			
			first.start();
			
			sequenceStarted = true;
			
			return;
			
		}
	 
		String keyCurrent = sequence.get( sequenceCurrentID );
		
		Clip clipCurrent = clips.get( keyCurrent );
		
		if( clipCurrent.isRunning() ) {
			return;
		}
		
		 
		if( ( !loop && sequenceCurrentID < size - 1 ) || loop ) {
			
			sequenceCurrentID++;
			
			// If loop flag is set to true, once the sequence reaches the end, loop back to the beginning
			if( loop && sequenceCurrentID >= size ) {
				
				sequenceCurrentID = 0;
				sequenceStarted = false;
				
			}
			
		}
		
		// Play the next clip
		clips.get( sequence.get( sequenceCurrentID ) ).start(); 

		
	}
	
	/**
	 * Stop a sequence from playing (but can be resumed via playSequence)
	 */
	public void stopSequence() {
		
		int size = sequence.size();
		
		if( size == 0 || sequenceCurrentID < 0 ) {
			return;
		}
		
		// Stop any clips in the currently existing sequence 
		for( int i = 0 ; i < size ; i++ ) {
		
			stop( sequence.get( i ) );
			
		}
		
		if( sequenceCurrentID == 0 && sequenceStarted ) {
			sequenceStarted = false;
		}
					
		
	}
	
	/**
	 * Stops the current sequence and clears all of its data
	 */
	public void clearSequence() {
		
		stopSequence();
		
		// Reset sequence
		sequence = new ArrayList<String>();
		
		sequenceCurrentID = 0;
		sequenceStarted = false;
		
	}
 
	
	/**
	 * Load a sound clip in the library with a given key for later access
	 * 
	 * @param key The key used to access this audio clip
	 * @param filename The path to the audio clip file
	 * @return True if load successful, False otherwise
	 */
	public boolean load( String key , String filename ) {
	 
		Clip in = null;

	    try {
	    	
	    	File wav = new File( filename );
	        AudioInputStream audioIn = AudioSystem.getAudioInputStream( wav );
	        in = AudioSystem.getClip();
	        in.open( audioIn );
	        
	        clips.put( key , in );
	        
	        return true;
	        
	    } catch( Exception e ) {
	    	
	        return false;
	        
	    }
 
	}
	
	/**
	 * Remove an audio clip from the library.
	 * If the audio clip is playing, stop it first.
	 * 
	 * @param key The key reference for this clip in the library
	 * @return True if removal is successful , False otherwise
	 */
	public boolean remove( String key ) {
		
		if( !clips.containsKey( key ) ) {
			return false;
		}
		
		Clip clip = clips.get( key );
		
		if( clip.isRunning() ) {
			clip.stop();
		}
		
		clips.remove( key );
		
		return true;
		
	}
	
	/**
	 * Play an audio clip
	 * 
	 * @param key The key reference for this clip in the library
	 */
	public void play( String key ) {
		
		if( !clips.containsKey( key ) ) {
			return;
		}
		
		Clip clip = clips.get( key );
		
	    if( clip.isRunning() ) { 
	    	clip.stop();
	    }

	    clip.setFramePosition( 0 );
	    clip.start();
	    
	}
	
	/**
	 * Stop an audio clip from playing
	 * 
	 * @param key The key reference for this clip in the library
	 */
	public void stop( String key ) {
		
		if( !clips.containsKey( key ) ) {
			return;
		}
		
		Clip clip = clips.get( key );
	
	    clip.stop();

	}
	
}
