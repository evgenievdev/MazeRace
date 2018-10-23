package server.mazegeneration;
 
/**
 * 
 * A class containing static properties used by the maze generator classes
 * 
 * @author Iliya Liksov
 *
 */
public class MazeConstants {
	
	// GENERAL NOTE! DO NOT USE NEGATIVE NUMBERS. Otherwise they will clash with the maze generation algorithm and cause errors.
	
	// Minimum dimensions for a maze (due to constraints in the algorithm, maze dimensions must be odd)
	public static int MIN_ROWS = 9;
	public static int MIN_COLS = 9;
	
	// Base values for the maze's cells 
	public static int WALKABLE = 1;
	public static int WALL = 0;
	public static int PATH = 2;	// Only used by the solver method
	public static int VISITED = 3; // Only used by the solver method
	
	// Generic Start and Finish index points - these are the default values when creating a maze with ProceduralMaze or MazeGenerator (only when uniquePoints is set to FALSE)
	public static int START = 4;
	public static int FINISH = 5;
	
	// 4 Player unique Start and Finish points - these are the values used by MazeGenerator if uniquePoints is set to TRUE
	public static int START_P1 = 6;
	public static int START_P2 = 7;
	public static int START_P3 = 8;
	public static int START_P4 = 9;
	
	public static int FINISH_P1 = 10;
	public static int FINISH_P2 = 11;
	public static int FINISH_P3 = 12;
	public static int FINISH_P4 = 13;
	
	// Maximum solution path difference
	// The higher the value the less fair the maze quadrants will be in an Asymmetrical maze, 
	// BUT the maze generation time will be faster (unless you are very lucky) 
	protected static int MAX_SOLUTION_DIFFERENCE = 3;
	
}
