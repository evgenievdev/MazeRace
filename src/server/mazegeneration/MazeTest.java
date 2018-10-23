package server.mazegeneration;
import java.util.Arrays;

public class MazeTest {

	public static void main( String[] args ) {
		
	
		// Record the current system time BEFORE a maze has been created
		long startTime = System.currentTimeMillis();
		
		// Create a random maze with 4 quadrants (Asymmetrical because it's cooler..)
		// Parameters: 
		// int quadSize , boolean symmetrical , boolean ensureFairness , boolean uniquePoints , boolean removeMiddleHorizontalBorder , boolean removeMiddleVerticalBorder
		MazeGenerator maze = new MazeGenerator( 25 , false , true , false , true , true, true, true);
		System.out.println( maze.getQuadSize());
		System.out.println(maze.getMazeSize());
		System.out.println( maze.convertCoinsToString( maze.getCoins() , '|',','));
		
		/*
		// Record the current system time AFTER a maze has been created
		long endTime = System.currentTimeMillis();
		
		// Print the time it took to create the maze 
		// (NOTE! If you make an asymmetrical maze and use ensureFairness as true, expect exponentially slower generation the larger it is)
		System.out.println( "Maze Generation Time = " + ( endTime - startTime ) + "ms" );
		System.out.println( "Maze Size = " + maze.getMazeSize() );
		System.out.println( "Quadrant Size = " + maze.getQuadSize() );
	 
		
		// Crop maze on different parts
		
		// Center of the maze + 3 cells in each direction
		int[] boundaries1 = maze.getCroppedBoundaries( maze.getQuadSize() - 1 , maze.getQuadSize() - 1 , 3 , 3 );
		
		// Get the cropped maze based on the supplied boundaries
		int[][] crop1 = maze.getCroppedMaze( boundaries1 );
		
		// Get the adjusted player position based on the cropped maze above and its boundaries (NOTE! if the position is not within the boundaries -1,-1 will be returned)
		// The position in this case is dead in the middle of the maze (technically it isn't possible, but who cares)
		int playerX = maze.getQuadSize() - 1;
		int playerY = maze.getQuadSize() - 1;
		int[] adjusted = MazeGenerator.getAdjustedPlayerPosition( playerX , playerY , boundaries1 );
		
		System.out.println("\n Center of Maze + 3 cells each direction : ");
		ProceduralMaze.print( crop1 ); // Print the maze in a structured way
		System.out.println("Adjusted player position from [" + playerX + " ; "+ playerY +"] = " + Arrays.toString( adjusted ) );
 
		// ---------------------------------------------- Other random tests --------------------------------------------------------
		
		System.out.println("\n Top Left corner + 5 cells each direction : ");
		ProceduralMaze.print( maze.getCroppedMaze( maze.getCroppedBoundaries( 0 ,  0 , 5 , 5 ) ) );
		
		System.out.println("\n Bottom Right corner + 5 cells left/right + 3 cells up/down : ");
		ProceduralMaze.print( maze.getCroppedMaze( maze.getCroppedBoundaries( maze.getMazeSize() - 1 ,  maze.getMazeSize() - 1 , 5 , 3 ) ) );
		
		System.out.println("\n Entire maze : ");
		ProceduralMaze.print( maze.getMaze() );
		*/
		 
	}
	
}
