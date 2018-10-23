package client;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * 
 * @author Iliya Liksov
 *
 */
public class GUIConstants {
 
	
	// Local folder structure
	public static final String dataPath = "./src/client/Data/";
		public static final String fontsPath = dataPath + "Fonts/";
		public static final String soundsPath = dataPath + "Sounds/";
		public static final String texturePath = dataPath + "Textures/";
			public static final String levelsPath = texturePath + "Levels/";
				public static final String themeName = "Forest/";
			public static final String playersPath = texturePath + "Players/";
			public static final String markersPath = texturePath + "Markers/";
			public static final String menusPath = texturePath + "Menus/";
			 
	
	// The width and height of the client frame in pixels
	public static final int screenWidth = 1280;
	public static final int screenHeight = 960;
	
	// The size of each maze tile in pixels
	public static final int mazeTileX = 35;
	public static final int mazeTileY = 35;
	
	// The maze's offset in the JPanel
	public static final int mazeOffsetX = 50;
	public static final int mazeOffsetY = 0;
	
	// HTML/CSS like padding for the maze
	public static final int mazePaddingX = 50;
	public static final int mazePaddingY = 50;
	
	// Screen background color
	public static final Color screenColor = new Color( 0 , 0 , 0 );
	
	// An array of player colors (NOTE! Requires import of java.awt.Color; library)
	public static final Color[] playerColors = new Color[] {
		new Color( 128,0,0 ),
		new Color( 128,0,128 ),
		new Color( 0,128,128 ),
		new Color( 64,128,255 )
	};
	
	// Maze tile color types : walkable path , wall , start point , finish point
	public static final Color pathColor = new Color( 15, 15, 15 );
	public static final Color wallColor = new Color( 34, 139, 34 );
	public static final Color startColor = new Color( 205, 190, 112 );
	public static final Color finishColor = new Color(180, 20, 20);
	public static final Color coinColor = new Color( 255 , 215 , 0);
	
	// InputListener key bindings : Up = up arrow , Down = down arrow , etc. 
	public static final int upKey = KeyEvent.VK_UP;
	public static final int downKey = KeyEvent.VK_DOWN;
	public static final int leftKey = KeyEvent.VK_LEFT;
	public static final int rightKey = KeyEvent.VK_RIGHT;
	public static final int fireKey = KeyEvent.VK_SPACE;
	public static final int quitKey = KeyEvent.VK_Q; // keyCodes
	
	
}
