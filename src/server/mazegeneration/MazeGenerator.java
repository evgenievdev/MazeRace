package server.mazegeneration;

import java.util.ArrayList;
import java.util.Random;

/**
 * Generate a square grid maze from 4 quadrants (mini-mazes) with individual solution paths converging in the middle of the maze.
 * 
 * @author Iliya Liksov
 *
 */
public class MazeGenerator {
	
	protected int[][] cells;
	protected boolean[][] coins;
	private int mazeSize;
	private int quadSize;
	
	public ProceduralMaze topLeftMaze, topRightMaze , bottomRightMaze , bottomLeftMaze;
	
	// Top Left , Top Right , Bottom Left , Bottom Right quadrant solution paths
	public ArrayList<Integer[]> TLSolution, TRSolution, BLSolution, BRSolution;
	 
	/**
	 * Generate a square maze from a specified quadrant size.
	 * 
	 * Quadrant size MUST be an ODD number due to algorithm constraints.
	 * If you enter an EVEN number, the algorithm will automatically increment the size to an ODD number.
	 * 
	 * This constructor ensures that the maze by default is SYMMETRICAL and each START/FINISH points
	 * on the maze has a unique index (See MazeConstraints Class for more information)
	 * 
	 * @param quadSize The size of the maze's quadrants
	 */
	public MazeGenerator( int quadSize ) {
		
		 this( quadSize , true , true , true , false , false , false,  true );
		
	}
	
	/**
	 * Alternative constructor for maze generator
	 * 
	 * You can specify if you want the maze quadrants to be symmetrical
	 * 
	 * By default, the constructor ensures that each quadrant has a fair solution path to a certain extent (See MazeConstants for information)
	 * 
	 * @param quadSize The size of the maze's quadrants
	 * @param symmetrical
	 */
	public MazeGenerator( int quadSize , boolean symmetrical ) {
		
		this( quadSize , symmetrical , true , true , false , false , false , true );
		
	}
	
	/**
	 * The complete constructor for the Maze generator.
	 * 
	 * IMPORTANT! If you make an ASYMMETRICAL maze and use ensureFairness as TRUE, expect exponentially slower generation the larger it is
	 * E.g. A 100x100 maze (50x50 quadsize) typically might take half a second to generate.
	 * Keep in mind that the extent to which you ensure fairness (defined in MazeConstants.MAX_SOLUTION_DIFFERENCE) has an effect on performance.
	 * 
	 * @param quadSize The size of the maze's quadrants. NOTE! This number must be an ODD number. If you write an EVEN number, the generator will increment it automatically.
	 * @param symmetrical Set to TRUE if you want each quadrant to have it's own unique structure and solution. Set to FALSE if you want all quadrants to be mirror copies of the Top Left quad.
	 * @param ensureFairness Set to TRUE if you want each quadrant to have a fair solution path (ONLY APPLIES TO ASYMMETRICAL MAZES). Set to FALSE if you wish to leave it to fate.
	 * @param uniquePoints Set to TRUE if you want the maze to have unique numbers for each START and FINISH point. Set to FALSE if you want each quadrant to share START/FINISH IDs
	 * @param removeMiddleHorizontalBorder Set to TRUE if you want to remove the middle HORIZONTAL border cells between quadrants, ONLY if their TOP and BOTTOM neighbor cells are WALKABLE
	 * @param removeMiddleVerticalBorder  Set to TRUE if you want to remove the middle VERTICAL border cells between quadrants, ONLY if their LEFT and RIGHT neighbor cells are WALKABLE
	 */
	public MazeGenerator( int quadSize , boolean symmetrical , boolean ensureFairness , 
						  boolean uniquePoints , boolean removeMiddleHorizontalBorder , 
						  boolean removeMiddleVerticalBorder , boolean convergeFinish ,
						  boolean generateCoins 
						) {
	 	
		// Due to some constraints of the maze generation algorithm and the way quads are stitched together, only odd numbers are allowed
		if( quadSize % 2 == 0 ) {
			
			quadSize++;
			
		}
		
		
		this.quadSize = quadSize;
		
		// The overall size of the maze. Since there are 4 quadrants, multiply by 2. 
		// Remove 1 at the end because we are going to crunch the middle borders together, therefore making the last row/column empty => useless
		mazeSize = quadSize * 2 - 1;
		
		// Initialize the maze's cell array
		cells = new int[ mazeSize ][ mazeSize ];
		 
		// Create an instance of the base quad maze, which will be mirrored to create the 4-way symmetrical maze
		topLeftMaze = new ProceduralMaze( quadSize , quadSize );
		
		// Initialize 2D Arrays for each quadrant
		int[][] topLeftQuad , topRightQuad , bottomRightQuad , bottomLeftQuad;
		
		// Top Left Quadrant (Always the same instance, regardless if the entire maze is symmetrical or not)
		topLeftQuad = topLeftMaze.getMaze();
		 
		// Save the solution for this quadrant
		TLSolution = topLeftMaze.generateSolution();
		
		 
		// If the maze is not supposed to be symmetrical, create 4 unique quadrants and flip them accordingly so that the START and FINISH points are in the right places
		if( symmetrical == false ) {
			
			// Create the remaining 3 maze quadrants with unique data from the first quadrant
			 
			topRightMaze = new ProceduralMaze( quadSize , quadSize );
			bottomRightMaze = new ProceduralMaze( quadSize , quadSize );
			bottomLeftMaze = new ProceduralMaze( quadSize , quadSize );
			
			// Create array lists containing the solution paths for each quadrant (used for balancing asymmetrical mazes)
			TRSolution = topRightMaze.generateSolution();
			BLSolution = bottomLeftMaze.generateSolution();
			BRSolution = bottomRightMaze.generateSolution();
			
			// Make sure that the mazes are fair to a certain extent by comparing the length of each solution path
			int TLPathSize = TLSolution.size();
			int TRPathSize = TRSolution.size();
			int BRPathSize = BRSolution.size();
			int BLPathSize = BLSolution.size();
			
			// Make sure that each quadrant has a balanced solution
			// Take the First (top left) quadrant as a baseline and balance the rest along that criteria
			if( ensureFairness ) {

				// Balance Top Right
				while( Math.abs( TLPathSize - TRPathSize ) > MazeConstants.MAX_SOLUTION_DIFFERENCE ) {
					
					topRightMaze = new ProceduralMaze( quadSize , quadSize );
					TRSolution = topRightMaze.generateSolution();
					TRPathSize = TRSolution.size();
					
				}
				
				// Balance Bottom Right
				while( Math.abs( TLPathSize - BRPathSize ) > MazeConstants.MAX_SOLUTION_DIFFERENCE ) {
					
					bottomRightMaze = new ProceduralMaze( quadSize , quadSize );
					BRSolution = bottomRightMaze.generateSolution();
					BRPathSize = BRSolution.size();
					
				}
				
				// Balance Bottom Left
				while( Math.abs( TLPathSize - BLPathSize ) > MazeConstants.MAX_SOLUTION_DIFFERENCE ) {
					
					bottomLeftMaze = new ProceduralMaze( quadSize , quadSize );
					BLSolution = bottomLeftMaze.generateSolution();
					BLPathSize = BLSolution.size();
					
				}
			
			}
			
			//System.out.println( "Top Left Solution Cells = " + TLPathSize );
			//System.out.println( "Top Right Solution Cells = " + TRPathSize );
			//System.out.println( "Bottom Left Solution Cells = " + BLPathSize );
			//System.out.println( "Bottom Right Solution Cells = " + BRPathSize );
			
			// Top Right Quadrant
			topRightQuad = mirrorArrayHorizontal( topRightMaze.getMaze() );
			
			// Bottom Right Quadrant
			bottomRightQuad = mirrorArrayHorizontalAndVertical( bottomRightMaze.getMaze() );
			
			// Bottom Left Quadrant
			bottomLeftQuad = mirrorArrayVertical( bottomLeftMaze.getMaze() ); 
			
			// Fix the Solution paths for the remaining 3 quadrants in the ASYMMETRICAL maze AFTER we have mirrored the quadrants
			TRSolution = mirrorArrayListHorizontal( TRSolution );
			BRSolution = mirrorArrayListHorizontalAndVertical( BRSolution );
			BLSolution = mirrorArrayListVertical( BLSolution );
			
		} else {
 
			// Top Right Quadrant
			topRightQuad = mirrorArrayHorizontal( topLeftQuad );
			
			// Bottom Right Quadrant
			bottomRightQuad = mirrorArrayVertical( topRightQuad );
			
			// Bottom Left Quadrant
			bottomLeftQuad = mirrorArrayVertical( topLeftQuad ); 
			
			// Generate solution paths for the remaining 3 quadrants in the SYMMETRICAL maze based on mirrored versions of the 1st quadrant (TOP LEFT)
			TRSolution = mirrorArrayListHorizontal( TLSolution );
			BRSolution = mirrorArrayListHorizontalAndVertical( TLSolution );
			BLSolution = mirrorArrayListVertical( TLSolution );
		 
		}
		
		// Fix the offsets of each quadrant's solution paths in the ArrayLists
		// Since the Solution paths are relative to the quadrants, when the quadrants are stitched together to make a larger maze
		// The solution paths must be edited accordingly to fit their respective quadrant
		// Otherwise all solution paths will remain in the top left quadrant
		// Only the Top Right, Bottom Left and Bottom Right solutions need to be modified
		solutionPathAddOffset( TRSolution , 0 , quadSize - 1 );
		solutionPathAddOffset( BRSolution , quadSize - 1 , quadSize - 1 );
		solutionPathAddOffset( BLSolution , quadSize - 1 , 0 );
		 
		// Join all quadrants into maze
		// NOTE: the middle borders are crunched together 
		for( int i = 0 ; i < quadSize ; i++ ) {
			
			for( int j = 0 ; j < quadSize ; j++ ) {
				
				// Top left quad
				cells[ i ][ j ] = topLeftQuad[ i ][ j ];
				
				// Top right quad
				cells[ i ][ j + quadSize - 1 ] = topRightQuad[ i ][ j ];
				
				// Bottom left quad
				cells[ i + quadSize - 1 ][ j ] = bottomLeftQuad[ i ][ j ];
				
				// Bottom right quad
				cells[ i + quadSize - 1 ][ j + quadSize - 1 ] = bottomRightQuad[ i ][ j ];
				
			}
			
		}
		
		/* Debug ArrayList mirroring and offset of solution paths 
	 
		Integer[] node = new Integer[2];
		for( int i = 0 ; i < BRSolution.size(); i++ ) {
			node = BRSolution.get(i);
			cells[ node[ 0 ] ][ node[ 1 ] ] = MazeConstants.PATH;
		}
		
		*/
		 
		
		// Generate wall outline for the entire maze (not necessary by default, since quads already have borders)
		// createBorders();
		 
		// Give each quadrant a unique START and FINISH index
		if( uniquePoints ) {
		
			createUniqueStartAndFinish();
		
		}
		
		// Destroy any single wall cells in the middle of the entire maze to allow players to cross over into each-other's quadrants
		if( removeMiddleHorizontalBorder ) {
			
			destroyMiddleHorizontalBorder();
			
		} 
		if( removeMiddleVerticalBorder ) {
		
			destroyMiddleVerticalBorder();
		
		}
		 
		// If the middle borders are destroyed the players can easily walk across quadrants which makes it silly to have 4 finish points
		// Therefore if convergeFinish is set to true, the finish points are fixed to one location in the middle of the maze
		if( removeMiddleHorizontalBorder && removeMiddleVerticalBorder && convergeFinish ) {
			 
			convergeFinish();
			
		}
		
		// Generate random coins on the maze (Call this AFTER convergeFinish, otherwise a coin may be spawned over the finish point)
		if( generateCoins ) {
			
			generateCoins( 2 , true );
			
		}
		 
	}
	
	
	// ------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------- PUBLIC METHODS ----------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Get the adjusted overall size of the maze 
	 * 
	 * @return The overall size of the maze
	 */
	public int getMazeSize() {
		
		return mazeSize;
		
	}
	
	/**
	 * Get the size of each quadrant in the maze
	 * 
	 * @return The maze's quadrant size
	 */
	public int getQuadSize() {
		
		return quadSize;
		
	}
	
	/**
	 * Get the entire maze's structure as a 2D array of integers (default format)
	 * 
	 * @return The maze's 2D array of integers
	 */
	public int[][] getMaze() {
		
		return cells;
		
	}
	
	/**
	 * Convert a 2D array of integers into a 2D array of chars
	 * 
	 * Static method.
	 * 
	 * @param data the 2D array of integers
	 * @return The newly created 2D array of chars
	 * @throws NullPointerException If data parameter is null
	 */
	public static char[][] intToChar2DArray( int[][] data ) {
		
		if( data == null ) {
			
			throw new NullPointerException();
			
		}
		
		int rows = data.length;
		int cols = data[ 0 ].length;
		
		char[][] result = new char[ rows ][ cols ];
		
		for( int i = 0 ; i < rows ; i++ ) {
			
			for( int j = 0 ; j < cols ; j++ ) {
				
				result[ i ][ j ] = (char) data[ i ][ j ];
				
			}
			
		}
		
		return result;
		
	}
	

	/**
	 * Get the entire maze's structure converted to a 2D array of chars
	 * 
	 * @return The maze's 2D array of chars
	 */
	public char[][] getMazeChar() {
		
		return intToChar2DArray( this.cells );
		
	}
	

	/**
	 * 
	 * This method takes a coordinate within the maze (x and y) (intended to be a player's position),
	 * and generates a rectangle around that position based on a specific limit (i.e. number of cells in either direction)
	 * 
	 * The intention is to use a player's position within the maze to create a small rectangle around them which
	 * essentially shows only the part of the maze which the rectangle corresponds to. 
	 * 
	 * The boundaries returned are in the format of an array of 4 integers in the following order : top , bottom , left , right
	 * The boundaries returned can be used directly to crop off the cells[][] array
	 * 
	 * Example: boundaries[ 1,3,3,6 ] means that the rectangle will only show the maze cells from columns 3 to 6 in rows 1 to 3
	 * 
	 * To only display those cells use a nested for loop like so:
	 * 
	 * for( int i = boundaries[ 0 ] ; i < boundaries[ 1 ] ; i++ ) {
	 * 
	 * 	  for( int j = boundaries[ 2 ] ; j < boundaries[ 3 ] ; j++ ) {
	 * 	
	 * 		 // ... cells[ i ][ j ] ...
	 * 		
	 * 	  }
	 * 
	 * }
	 * 
	 * 
	 * @param x The player's current x position (row)
	 * @param y The player's current y position (column)
	 * @param xLimit The number of cells in both directions (up and down) from the player's position to show
	 * @param yLimit The number of cells in both directions (left and right) from the player's position to show
	 * @return The array of 4 indices giving the top,bottom,left and right boundaries of the rectangle
	 * @throws NullPointerException If the maze cells array is not initialized yet
	 * @throws ArrayIndexOutOfBoundsException If the player's x and y coordinates are not within the boundaries of the maze
	 */
	public int[] getCroppedBoundaries( int x , int y , int xLimit , int yLimit ) {
		
		if( this.cells == null ) {
			
			throw new NullPointerException("Maze array is not initialized.");
			
		}
		
		if( x < 0 || y < 0 || x >= mazeSize || y >= mazeSize ) {
			
			throw new ArrayIndexOutOfBoundsException("The position of the player is not within the boundaries of the maze.");
			
		}
		 
		// Make sure at least one neighboring cell is visible in each direction from the player
		if( xLimit < 1 ) {
			xLimit = 1;
		}
		
		if( yLimit < 1 ) {
			yLimit = 1;
		}
		 
		// Make sure the boundaries of the crop rectangle do not violate the boundaries of the maze
		
		// Limit the left boundary to the first element in the maze array if it attempts to go out of bounds
		int leftLimit = y - yLimit;
		// Limit the right boundary to the last element in the maze array if it attempts to go out of bounds
		int rightLimit = y + yLimit;
		
		if( leftLimit < 0 ) { leftLimit = 0; }
		if( rightLimit >= mazeSize ) { rightLimit = mazeSize - 1; } 
		
		// Limit the top boundary to the first element in the maze array  
		int topLimit = x - xLimit;
		if( topLimit < 0 ) { topLimit = 0; }
		
		 
		// Limit the bottom boundary to the last element in the maze array
		int bottomLimit = x + xLimit;
		if( bottomLimit >= mazeSize ) { bottomLimit = mazeSize - 1; }
	 
		// Adjustments - always show the set amount of rows and columns even if you are near a border of the maze
		// This is done by expanding one side when the other is shorter than expected.
		if( y - leftLimit < yLimit ) {
			
			rightLimit += yLimit - (y - leftLimit);
			
		}
		
		if( rightLimit - y < yLimit ) {
			
			leftLimit -= yLimit - (rightLimit - y);
			
		}
		
		if( x - topLimit < xLimit ) {
			
			bottomLimit += xLimit - (x - topLimit);
			
		}
		
		if( bottomLimit - x < xLimit ) {
			
			topLimit -= xLimit - (bottomLimit - x);
			
		}
		 
		
		
		// An array of 4 integers indicating the boundaries of the maze which should be rendered
		// Since the X property defines the row , use the top and bottom limits to define that first
		// Then use the left and right limits to define the column within the row
		int[] result = new int[] { topLimit , bottomLimit , leftLimit , rightLimit };
		
		return result;
		 
		
	}
	
	/**
	 * Get an adjusted player position based on visible rectangle boundaries (cropped section of the maze).
	 * The result is an array of 2 integers calibrated for client side rendering.
	 * The first element in the array is the row and the second the column.
	 * 
	 * These integers are intended for use within the array returned by getCroppedMaze();
	 * 
	 * NOTE! Returns -1,-1 if the player is not within the cropped maze boundary => when rendering you should check if the return is not -1,-1
	 * 
	 * @param x The player's x position (row)
	 * @param y The player's y position (column)
	 * @param boundaries An array of 4 integers calculated using the getCroppedBoundaries() method
	 * @return An array of 2 integers containing the adjusted row and column of the player
	 */
	public static int[] getAdjustedPlayerPosition( int x , int y , int[] boundaries ) {
		 
		// If the player position given is not within the boundaries calculated, return [-1,-1]
		if( x < boundaries[ 0 ] || x > boundaries[ 1 ] || y < boundaries[ 2 ] || y > boundaries[ 3 ] ) {
			
			return new int[] {-1,-1};
			
		}
		
		int[] result = new int[ 2 ];
		
		// Adjusted row
		result[ 0 ] = x - boundaries[ 0 ];
		
		// Adjusted column
		result[ 1 ] = y - boundaries[ 2 ];
		
		return result;
		
	}
	
	/**
	 * Get the cells in the maze only limited to the cropped rectangle generated by getCroppedBoundaries()
	 * 
	 * NOTE: This method does not alter the original maze in any way.
	 * 
	 * @param boundaries The array of 4 integers specifying the boundaries of the maze
	 * @return The cropped 2D array of the maze 
	 */
	public int[][] getCroppedMaze( int[] boundaries ) {
		
		int rows = (boundaries[1] - boundaries[0]) + 1;
		int cols = (boundaries[3] - boundaries[2]) + 1;
		
		int[][] cropped = new int[ rows ][ cols ];

				
		int tr = 0;
		int tc = 0;
		for( int i = boundaries[ 0 ] ; i <= boundaries[ 1 ] ; i++ ) {
			 			  
		  	 for( int j = boundaries[ 2 ] ; j <= boundaries[ 3 ] ; j++ ) {
		  		 
		  		 cropped[ tr ][ tc ] = cells[ i ][ j ];
		  		
		  		 tc++;
		  		 
		  	 }
		  	 
		  	tc = 0;
		  	tr++;
		  
		}
		
		return cropped;
		
	}
	
	/**
	 * Generate a 2D array of booleans used to express the cells in the maze which have coins to collect.
	 * Element where value is true means there is a coin in that cell. False if there isn't one
	 * 
	 * NOTE! Coins only spawn on cells which are walkable (or set as path if the maze has solution printing)
	 * 
	 * NOTE! Java initializes boolean arrays to false by default, so no need to set that manually
	 * 
	 * @param interval The rate of dispersion of the coins 
	 * @param randomize Set to true if you want to use a random check to see if a coin should be spawned in a given cell
	 */
	public void generateCoins( int interval , boolean randomize ) {
		
		if( cells == null ) {
			return;
		}
		
		int r = cells.length;
		int c = cells[ 0 ].length;
		
		// Create coins on the maze to collect
		coins = new boolean[ r ][ c ];
		
		// Make sure the interval is not negative
		if( interval < 0 ) { interval = 0; }
		
		int counter = 0;
		int random = 0;
		
		for( int i = 0 ; i < r ; i++ ) {
			
			for( int j = 0 ; j < c ; j++ ) {
				
				if( cells[ i ][ j ] == MazeConstants.WALKABLE || cells[ i ][ j ] == MazeConstants.PATH ) {
					
					counter++;
					
					if( counter > interval ) {
						
						if( randomize == true ) {
							
							random = randInt( 0 , 10 );
							if( random < 5 ) {
								
								coins[ i ][ j ] = true;
								
							}
							
						} else {
						
							coins[ i ][ j ] = true;
						
						}
						
						counter = 0;
						
					}
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Get the 2D boolean array specifying where the coins are
	 * 
	 * IMPORTANT! It is possible that the coins array can be null if it was not initialized.
	 * Therefore always check for a null reference when using this getter.
	 * 
	 * @return 2D array of booleans with the same size as the maze
	 */
	public boolean[][] getCoins() {
		 
		return coins;
		
	}
	
	/**
	 * How many uncollected coins are there 
	 * 
	 * @return The number of uncollected coins
	 */
	public int countCoins() {
		
		if( coins == null ) {
			return 0;
		}
		
		int r = coins.length;
		int c = coins[ 0 ].length;
		
		int count = 0;
		
		for( int i = 0 ; i < r ; i++ ) {
			
			for( int j = 0 ; j < c ; j++ ) {
				
				if( coins[ i ][ j ] == true ) {
					count++;
				}
				
			}
			
		}
		
		return count;
		
	}
	
	/**
	 * Get the coins in the maze only limited to the cropped rectangle generated by getCroppedBoundaries()
	 * 
	 * NOTE: This method does not alter the original coins array in any way.
	 * 
	 * @param boundaries The array of 4 integers specifying the boundaries of the maze
	 * @return The cropped 2D array derived from the entire coins array 
	 */
	public boolean[][] getCroppedCoins( int[] boundaries ) {
		
		int rows = (boundaries[1] - boundaries[0]) + 1;
		int cols = (boundaries[3] - boundaries[2]) + 1;
		
		boolean[][] cropped = new boolean[ rows ][ cols ];

		int tr = 0;
		int tc = 0;
		for( int i = boundaries[ 0 ] ; i <= boundaries[ 1 ] ; i++ ) {
			 			  
		  	 for( int j = boundaries[ 2 ] ; j <= boundaries[ 3 ] ; j++ ) {
		  		 
		  		 cropped[ tr ][ tc ] = coins[ i ][ j ];
		  		
		  		 tc++;
		  		 
		  	 }
		  	 
		  	tc = 0;
		  	tr++;
		  
		}
		
		return cropped;
		
	}
	
	/**
	 * Take a coin away from the coins array 
	 * The element will still remain in the array, but the state of the element will change to false if the coin has been collected.
	 * 
	 * This method is useful on the server to add to the coin counter of the player.
	 * 
	 * @param x The row in the maze (& coin array)
	 * @param y The column in the maze (& coin array)
	 * @return True if the coin has been collected, False otherwise.
	 */
	public boolean collectCoin( int x , int y ) {
		
		// If the coins array is null or the x,y parameters are out of bounds or the element in the coins array is not a coin
		if( coins == null || x < 0 || x >= coins.length || y < 0 || y >= coins[ 0 ].length || coins[ x ][ y ] == false ) {
			
			return false;
			
		}
		
		// Remove the coin from the array
		coins[ x ][ y ] = false;
		
		// Return true to indicate that coin has been taken
		return true;
		
	}
	
	/**
	 * Get the ArrayList of Integer pairs containing the solution path for the TOP LEFT quadrant
	 * 
	 * @return The ArrayList of Integer pairs
	 */
	public ArrayList<Integer[]> getTopLeftSolution() {
		
		return TLSolution;
		
	}
	
	/**
	 * Get the ArrayList of Integer pairs containing the solution path for the TOP RIGHT quadrant
	 * 
	 * @return The ArrayList of Integer pairs
	 */
	public ArrayList<Integer[]> getTopRightSolution() {
		
		return TRSolution;
		
	}

	/**
	 * Get the ArrayList of Integer pairs containing the solution path for the BOTTOM LEFT quadrant
	 * 
	 * @return The ArrayList of Integer pairs
	 */
	public ArrayList<Integer[]> getBottomLeftSolution() {
		
		return BLSolution;
		
	}
	
	/**
	 * Get the ArrayList of Integer pairs containing the solution path for the BOTTOM RIGHT quadrant
	 * 
	 * @return The ArrayList of Integer pairs
	 */
	public ArrayList<Integer[]> getBottomRightSolution() {
		
		return BRSolution;
		
	}
	
	/**
	 * Generate a random integer between a set interval
	 * 
	 * @param min The minimum integer value
	 * @param max The maximum integer value
	 * @return The randomly generated integer
	 */
	public static int randInt( int min, int max ) {
 
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------
	// --------------------------------------------------------- PROTECTED METHODS --------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Make the cells on the outline of the entire maze of type WALL.
	 * 
	 * In general it is not needed because the quadrants already have borders, but it can be used in case something
	 * in the maze's structure is altered and needs to be reconstructed again.
	 */
	protected void createBorders() {
	 
		for( int i = 0 ; i < mazeSize ; i++ ) {
			
			// Top horizontal line
			cells[ i ][ 0 ] = MazeConstants.WALL;
			// Bottom horizontal line
			cells[ i ][ mazeSize - 1 ] = MazeConstants.WALL;
			// Left vertical line
			cells[ 0 ][ i ] = MazeConstants.WALL;
			// Right vertical line
			cells[ mazeSize - 1 ][ i ] = MazeConstants.WALL;
			
		}
	 
		
	}
	
	protected void convergeFinish() {
		
		if( cells == null ) {
			return;
		}
		
		// If the center of the maze is a wall you can not convert it to a finish point, because it indicates that the borders still exist
		if( cells[ quadSize - 1 ][ quadSize - 1 ] == MazeConstants.WALL ) {
			return;
		}
		
		// Make the center of the maze the finish point
		cells[ quadSize - 1 ][ quadSize - 1 ] = MazeConstants.FINISH;
		
		// Top Left
		cells[ quadSize - 2 ][ quadSize - 2 ] = MazeConstants.WALKABLE;
		// Top Right
		cells[ quadSize - 2 ][ quadSize ] = MazeConstants.WALKABLE;
		// Bottom Left
		cells[ quadSize ][ quadSize - 2 ] = MazeConstants.WALKABLE;
		// Bottom Right
		cells[ quadSize ][ quadSize ] = MazeConstants.WALKABLE;
		
	}
	
	/**
	 * Mirror the contents of a 2D Array Horizontally.
	 * The original array is not altered.
	 * 
	 * @param data the 2D Array
	 * @return A new 2D array 
	 */ 
	protected static int[][] mirrorArrayHorizontal( int[][] data ) {
		
		return mirrorArray( data , 0 );
		
	}
	
	/**
	 * Mirror the contents of a 2D Array Vertically.
	 * The original array is not altered.
	 * 
	 * @param data the 2D Array
	 * @return A new 2D array 
	 */
	protected static int[][] mirrorArrayVertical( int[][] data ) {
		
		return mirrorArray( data , 1 );
		
	}
	
	/**
	 * Mirror the contents of a 2D Array both Horizontally and Vertically
	 * 
	 * This method uses one pass only
	 * 
	 * @param data the 2D Array
	 * @return A new 2D Array
	 */
	protected static int[][] mirrorArrayHorizontalAndVertical( int[][] data ) {
		
		return mirrorArray( data , 2 );
		
	}
	
	/**
	 * Mirror the contents of a 2D Array of integers horizontally, vertically or both at the same time
	 * 
	 * This method returns a new 2D Array and does not alter the original one.
	 * 
	 * @param data The 2D Array to mirror
	 * @param method For horizontal mirror - 0 ; vertical - 1 ; 2 (or anything else) - both
	 * @return The new mirrored 2D Array of integers
	 * @throws NullPointerException If the array has not been initialized
	 */
	protected static int[][] mirrorArray( int[][] data , int method ) {
		
		if( data == null ) {
			
			throw new NullPointerException("This array has not been initialized.");
			
		}
		
		int[][] result = new int[ data.length ][ data[0].length ];
		
		for( int i = 0 ; i < data.length ; i++ ) {
			
			for( int j = 0 ; j < data[ 0 ].length ; j++ ) {
				
				if( method == 0 ) {
					
					result[ i ][ j ] = data[ i ][ data.length - 1 - j ];
					
				} else if ( method == 1 ) {
					
					result[ i ][ j ] = data[ data.length - 1 - i ][ j ];
					
				} else {
					
					result[ i ][ j ] = data[ data.length - 1 - i ][ data.length - 1 - j ];
					
				}
					 
				
			}
			
		}
		
		return result;
		
	}
	
	/**
	 * Mirror an ArrayList of Integer pairs containing x and y coordinates horizontally, vertically or both at the same time.
	 * 
	 * This method is used to mirror the solution paths for the maze quadrants.
	 * 
	 * Each element in the ArrayList is a Integer[] array with 2 elements.
	 * The first element contains the row and the second element the column.
	 * 
	 * This method returns a new ArrayList and does not alter the original one.
	 * 
	 * @param data The original arrayList 
	 * @param method Set to 0 if you want to mirror horizontally, 1 for vertical and 2 for both at the same time
	 * @return The new mirrored ArrayList of Integer pairs
	 */
	protected ArrayList<Integer[]> mirrorArrayList( ArrayList<Integer[]> data , int method ) {
		
		ArrayList<Integer[]> result = new ArrayList<Integer[]>();
		
		int size = data.size();
		
		if( size == 0 ) {
			
			return result;
			
		}
		
		// index 0 = row , index 1 = column
		Integer[] node;
		Integer[] current;
		
		for( int i = 0 ; i < size ; i++ ) {
			
			node = new Integer[ 2 ];
			current = data.get( i );
			
			if( method == 0 ) {
				
				node[ 0 ] = current[ 0 ];
				node[ 1 ] = this.quadSize - current[ 1 ] - 1;
				
			} else if( method == 1 ) {
				
				node[ 0 ] = this.quadSize - current[ 0 ] - 1;
				node[ 1 ] = current[ 1 ];
				
			} else {
				
				node[ 0 ] = this.quadSize - current[ 0 ] - 1;
				node[ 1 ] = this.quadSize - current[ 1 ] - 1;
				
			}
			
			result.add( node );
			
		}
		
		return result;
		
	}
	
	/**
	 * Mirror the contents of an ArrayList with Integer pair elements horizontally
	 * 
	 * @param data The ArrayList to mirror
	 * @return The newly mirrored ArrayList
	 */
	protected ArrayList<Integer[]> mirrorArrayListHorizontal( ArrayList<Integer[]> data ) {
		
		return mirrorArrayList( data , 0 );
		
	}
	
	/**
	 * Mirror the contents of an ArrayList with Integer pair elements vertically
	 * 
	 * @param data The ArrayList to mirror
	 * @return The newly mirrored ArrayList
	 */
	protected ArrayList<Integer[]> mirrorArrayListVertical( ArrayList<Integer[]> data ) {
		
		return mirrorArrayList( data , 1 );
		
	}
	
	/**
	 * Mirror the contents of an ArrayList with Integer pair elements both horizontally and vertically
	 * 
	 * This method uses one pass only
	 * 
	 * @param data The ArrayList to mirror
	 * @return The newly mirrored ArrayList
	 */
	protected ArrayList<Integer[]> mirrorArrayListHorizontalAndVertical( ArrayList<Integer[]> data ) {
		
		return mirrorArrayList( data , 2 );
		
	}

	
	// ------------------------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------- PRIVATE METHODS ---------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * This method makes the START and FINISH point for each quadrant a unique number.
	 * 
	 * By default, each quadrant in the maze has an identical index for the START point and also for the FINISH point.
	 * I.E. Each quadrant by default has a start point of MazeConstants.START and a finish of MazeConstants.FINISH.
	 * This may cause problems with player positioning.
	 * 
	 * Also if you want the players to be able to cross into each other's quadrants, 
	 * this method allows them to not be able to finish the maze through someone else's finish point
	 * 
	 *  
	 */
	private void createUniqueStartAndFinish() {
		
		// Give each START and FINISH a unique ID 
		// Order: top left , top right , bottom left , bottom right
		cells[ 1  ][ 1 ] = MazeConstants.START_P1;
		cells[ 1 ][ mazeSize - 2 ] = MazeConstants.START_P2;
		cells[ mazeSize - 2 ][ 1 ] = MazeConstants.START_P3;
		cells[ mazeSize - 2 ][ mazeSize - 2 ] = MazeConstants.START_P4;
		
		cells[ quadSize - 2 ][ quadSize - 2 ] = MazeConstants.FINISH_P1;
		cells[ quadSize - 2 ][ quadSize ] = MazeConstants.FINISH_P2;
		cells[ quadSize ][ quadSize - 2 ] = MazeConstants.FINISH_P3;
		cells[ quadSize ][ quadSize ] = MazeConstants.FINISH_P4;
		
	}
	
	 
	
	/**
	 * Destroy any cells that are in the center of the entire maze on the HORIZONTAL line, 
	 * As long as they are of type WALL and are surrounded by cells that are NOT walls (UP and DOWN).
	 * 
	 * This allows the players to cross into each other's quadrants.
	 * 
	 */
	private void destroyMiddleHorizontalBorder() {
		 
		for( int i = 0 ; i < mazeSize; i++ ) {
			
			// If the current cell in the middle line (horizontal) is a wall and its neighbors above and below are not walls, remove the wall
			if( cells[ quadSize - 1 ][ i ] == MazeConstants.WALL && cells[ quadSize ][ i ] != MazeConstants.WALL && cells[ quadSize - 2 ][ i ] != MazeConstants.WALL ) {
				
				cells[ quadSize - 1 ][ i ] = MazeConstants.WALKABLE;
				
			}
			
		}
		
	}
	
	/**
	 * Destroy any cells that are in the center of the entire maze on the VERTICAL line, 
	 * As long as they are of type WALL and are surrounded by cells that are NOT walls (LEFT and RIGHT).
	 * 
	 * This allows the players to cross into each other's quadrants.
	 * 
	 */
	private void destroyMiddleVerticalBorder() {
		 
		for( int i = 0 ; i < mazeSize; i++ ) {
			
			// If the current cell in the middle line (vertical) is a wall and its neighbors left and right are not walls, remove the wall
			if( cells[ i ][ quadSize - 1 ] == MazeConstants.WALL && cells[ i ][ quadSize ] != MazeConstants.WALL && cells[ i ][ quadSize - 2 ] != MazeConstants.WALL ) {
				
				cells[ i ][ quadSize - 1 ] = MazeConstants.WALKABLE;
				
			}
			
		}
		
	}
	
	/**
	 * Add an offset to each position pair in a quadrant's solution path (ArrayList)
	 * 
	 * This is done to calibrate the solution paths after each quadrant has been added to its correct position in the maze.
	 * 
	 * WARNING! This method overwrites the contents of the arrayList supplied in the data parameter!
	 * 
	 * @param data The quadrant's solution path ArrayList
	 * @param offsetX The row offset
	 * @param offsetY The column offset
	 */
	private static void solutionPathAddOffset( ArrayList<Integer[]> data , int offsetX , int offsetY ) {
		
		int size = data.size();
		
		if( size == 0 ) {
			
			return;
			
		}
		
		Integer[] current;
		Integer[] newnode;
		
		for( int i = 0 ; i < size ; i++ ) {
			
			newnode = new Integer[2];
			current = data.get( i );
			
			newnode[ 0 ] = current[ 0 ] + offsetX;
			newnode[ 1 ] = current[ 1 ] + offsetY;
			
			data.set( i , newnode );
			
		}
		
	}
	
	public static String convertMazeToString( int[][] data , char rowDelimiter , char colDelimiter ) {
		 
		StringBuilder result = new StringBuilder();
		
		if( data == null ) {
			return "";
		}
		
		int rows = data.length;
		int cols = data[ 0 ].length;
	 
		for( int i = 0 ; i < rows ; i++ ) {
			
			for( int j = 0 ; j < cols ; j++ ) {
				
				result.append(data[ i ][ j ]);
				
				// Add column delimiter to every element except the last one
				if( j < cols - 1 ) {
					
					result.append( colDelimiter );
					
				}
				
			}
			
			// Add row delimiter at the end to every row except the last one 
			if( i < rows - 1 ) {
				
				result.append( rowDelimiter );
				
			}
			
		}
		
		return result.toString();
		
	}
	
	public static String convertCoinsToString( boolean[][] data , char rowDelimiter , char colDelimiter ) {
		
		StringBuilder result = new StringBuilder();
		
		if( data == null ) {
			return "";
		}
		
		int rows = data.length;
		int cols = data[ 0 ].length;
		int val;
		for( int i = 0 ; i < rows ; i++ ) {
			
			for( int j = 0 ; j < cols ; j++ ) {
				
				val = data[ i ][ j ] == true ? 1 : 0;
				result.append( val );
				
				// Add column delimiter to every element except the last one
				if( j < cols - 1 ) {
					
					result.append( colDelimiter );
					
				}
				
			}
			
			// Add row delimiter at the end to every row except the last one 
			if( i < rows - 1 ) {
				
				result.append( rowDelimiter );
				
			}
			
		}
		
		return result.toString();
		
	}
	
	
}
