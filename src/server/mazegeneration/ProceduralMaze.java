package server.mazegeneration;

import java.util.ArrayList;

/**
 * Create a randomized rectangular grid maze with one single solution.
 * 
 * Maze generation algorithm adapted from the book: Introduction to Programming Using Java, Seventh Edition by David J. Eck
 * 
 * @author Iliya Liksov
 * 
 */
public class ProceduralMaze {
    
	
    int[][] maze;    
 
    private int rows;       // number of rows of cells in maze, including a wall around edges
    private int columns;    // number of columns of cells in maze, including a wall around edges
    
    /**
     * Create a single rectangular perfect "grid maze"
     * 
     * NOTE! the maze's dimensions must be ODD numbers due to constraints of the algorithm.
     * If you enter even numbers, the dimensions will be automatically incremented to ensure that they are odd.
     * This way no errors will be thrown.
     * 
     * There is also a minimum size of the maze specified in the static MazeConstants class.
     * 
     * @param rows The number of rows for the maze
     * @param columns The number of columns for the maze
     */
    public ProceduralMaze( int rows , int columns ) {
    	
    	if( rows < MazeConstants.MIN_ROWS ) {
    		
    		rows = MazeConstants.MIN_ROWS;
    		
    	}
    	
    	if( columns < MazeConstants.MIN_COLS ) {
    		
    		columns = MazeConstants.MIN_COLS;
    		
    	}
    	
    	if( rows % 2 == 0 ) {
    		rows++;
    	}
    	
    	if( columns % 2 == 0 ) {
    		columns++;
    	}
    	
    	this.rows = rows;
    	this.columns = columns;
    	
    	// Generate the maze's walls and walkable cells 
    	generateMaze();
    	
    	// Manually add the start and finish point
    	// Start is top left corner , Finish is bottom right.
    	maze[ 1 ][ 1 ] = MazeConstants.START;
        maze[ rows - 2 ][ columns - 2 ] = MazeConstants.FINISH;
    	
    }
    
    /**
     * Get the structure of the maze.
     * 
     * @return A 2D array of integers of the maze's structure.
     * @throws NullPointerException If the maze array is not initialized
     */
    public int[][] getMaze() {
    	
    	if( maze == null ) {
    		
    		throw new NullPointerException("The maze has not been initialized yet.");
    		
    	}
    	
    	return maze;
    	
    }
    
    /**
     * Get the number of rows in the maze
     * 
     * @return The number of rows in the maze
     */
    public int getRows() {
    	
    	return this.rows;
    	
    }
    
    /**
     * Get the number of columns in the maze
     * 
     * @return The number of columns in the maze
     */
    public int getColumns() {
    	
    	return this.columns;
    	
    }
     
    /**
     * Create a random maze. 
     * The strategy is to start with a grid of disconnected "rooms" separated by walls.
 	 * then look at each of the separating walls, in a random
     * order.  If tearing down a wall would not create a loop
     * in the maze, then tear it down.  Otherwise, leave it in place.
     * 
     */
    private void generateMaze() {
    	
        // If the maze array hasn't been initialized yet, initialize it with the appropriate dimensions
        if ( maze == null ) {
        	
            maze = new int[ rows ][ columns ];
            
        }
        
        int i,j;
        
        int emptyCt = 0; // number of rooms
        int wallCt = 0;  // number of walls
        
        int[] wallrow = new int[ ( rows*columns ) / 2 ];  // position of walls between rooms
        int[] wallcol = new int[ ( rows*columns ) / 2 ];
        
        // Make every cell a WALL at first
        for ( i = 0 ; i < rows ; i++ ) {  
        	
            for ( j = 0 ; j < columns ; j++ ) {
            	
                maze[i][j] = MazeConstants.WALL;
                
            }
            
        }
        
        // Make a grid of empty rooms (every other cell on every other row)
        for ( i = 1 ; i < rows-1 ; i += 2 ) { 
        	
            for ( j = 1 ; j < columns-1 ; j += 2 ) {
            	 
                emptyCt++;
                maze[ i ][ j ] = -emptyCt;  // each room is represented by a different negative number
                
                // Record info about wall below this room
                if ( i < rows-2 ) {   
                	
                    wallrow[ wallCt ] = i+1;
                    wallcol[ wallCt ] = j;
                    wallCt++;
                    
                }
                
                // Record info about wall to right of this room
                if ( j < columns-2 ) {   
                	
                    wallrow[ wallCt ] = i;
                    wallcol[ wallCt ] = j+1;
                    wallCt++;
                    
                }
                
            }
            
        }
    
        int r;
        
        for ( i = wallCt - 1 ; i > 0 ; i-- ) {
        	
        	// Choose a random cell that is a WALL and attempt to remove it and convert the cell to WALKABLE
            r = (int) ( Math.random() * i ); 
          
            tearDown( wallrow[ r ] , wallcol[ r ] );
            
            wallrow[ r ] = wallrow[ i ];
            wallcol[ r ] = wallcol[ i ];
            
        }
        
    	// Set any cell with a negative value as WALKABLE
        for ( i = 1 ; i < rows - 1 ; i++ ) {  
        	
            for ( j = 1 ; j < columns - 1 ; j++ ) {
            	
                if ( maze[ i ][ j ] < 0 ) {
                	
                    maze[ i ][ j ] = MazeConstants.WALKABLE;
                    
                }
                
            }
            
        }
         
    }
    
   /**
    * Tear down a wall
    * 
    * Tearing down a wall joins two "rooms" into one "room".  
    * (Rooms begin to look like corridors as they grow.)  
    * 
    * When a wall is torn down, the room codes on one side are converted to 
    * match those on the other side, so all the cells in a room have the same code.   
    * 
    * NOTE! If the room codes on both sides of a wall already have the same code, 
    * then tearing down that wall would create a loop, so the wall is left in place.
    * 
    * @param row The row index in the maze
    * @param col The column index in the maze
    */
   private void tearDown( int row , int col ) {
	    
	    boolean isOdd = row % 2 == 1 ? true : false;
	   
	    // row is odd; wall separates rooms horizontally
        if ( isOdd && maze[row][col-1] != maze[row][col+1] ) {
             
            fill(row, col-1, maze[row][col-1], maze[row][col+1]);
            maze[row][col] = maze[row][col+1];
         
           
        }
        
        // row is even; wall separates rooms vertically
        else if ( !isOdd && maze[row-1][col] != maze[row+1][col] ) {
             
            fill(row-1, col, maze[row-1][col], maze[row+1][col]);
            maze[row][col] = maze[row+1][col];
        
          
        }
        
    }
   	
   	/**
   	 * called by tearDown() to change "room codes".
   	 * 
   	 * @param row The row index in the maze
   	 * @param col The column index in the maze
   	 * @param replace The room code to search for
   	 * @param replaceWith The room code to replace this cell with
   	 */
    private void fill( int row , int col , int replace , int replaceWith ) {
    	 
    	// If this cell in the maze doesn't have the room code we are searching for, exit method 
        if ( maze[ row ][ col ] != replace ) {
        	
        	return;
        	
        }
        
        // Replace this cell's room code with the new one
        maze[ row ][ col ] = replaceWith;
        
        // Continue recursively in all directions
        fill( row+1 , col , replace , replaceWith );	// One row down , same column
        fill( row-1 , col , replace , replaceWith );	// One row up , same column
        fill( row , col+1 , replace , replaceWith );	// Same row , next column
        fill( row , col-1 , replace , replaceWith );	// Same row , previous column
         
    }
  
    
    /**
     * Private method used to copy the contents of a 2D Array to a new one.
     * 
     * @param data The 2D Array to copy
     * @return The new 2D Array
     */
    private static int[][] copyArray( int[][] data ) {
    	 
    	if( data == null ) {
    		
    		return data;
    		
    	}
    	
    	int rows = data.length;
    	int cols = data[ 0 ].length;
    	
    	int[][] result = new int[ rows ][ cols ];
    	
    	for( int i = 0 ; i < rows ; i++ ) {
    		
    		for( int j = 0 ; j < cols ; j++ ) {
    			
    			result[ i ][ j ] = data[ i ][ j ];
    			
    		}
    		
    	}
    	
    	return result;
    	
    }
    
    /**
     * Generate an ArrayList of Integer pairs with the solution path for this maze.
     * 
     * Each integer pair contains the row and column for that cell. 
     * 
     * By default the elements in the list are in order respective to the solution path.
     * 
     * This method makes a temporary copy of the maze's structure and applies the solveMazeUtil method to find the path.
     * It then traverses the maze again and adds each solution path cell to the list, including the START and FINISH points.
     * 
     * This method does NOT alter the original maze structure.
     * 
     * @return The ArrayList of Integer pairs formulating the maze's solution.
     */
    public static ArrayList<Integer[]> generateSolution( int[][] arr , int sx , int sy , int ex , int ey) {
    	
    	ArrayList<Integer[]> result = new ArrayList<Integer[]>();
    	
    	if( arr.length == 0 ) { return result; }
    	
    	int[][] temp = copyArray( arr );
    	//int[][] temp = this.maze.clone(); - DEBUG ONLY ; This will keep the same reference from the class instance
    	
    	// Solve this maze for the temporary copy of the maze array
    	// This is done so that the original maze array does not get altered
    	// solved will be true when a solution is found. False otherwise
    	boolean solved = solveMazeUtil( temp , sx , sy , ex , ey );
    	
    	// If there is no solution found
    	if( !solved ) {
    		
    		return result;
    		
    	}
    	
    	int rows = arr.length;
    	int columns = arr[0].length;
    	
    	// Cycle through the cells in the maze and add any cell that is part of the solution path to the 
    	for ( int i = 0 ; i < rows ; i++ ) { 
        	
            for ( int j = 0 ; j < columns ; j++ ) {
            	
            	// If this cell is a PATH (therefore WALKABLE, but adjusted by the solution algorithm) OR a START or FINISH point, add it to the solution path list
            	if( temp[i][j] == MazeConstants.START || temp[i][j] == MazeConstants.FINISH || temp[i][j] == MazeConstants.PATH ||
            		temp[i][j] == MazeConstants.FINISH_P1 || temp[i][j] == MazeConstants.FINISH_P2 || temp[i][j] == MazeConstants.FINISH_P3 || temp[i][j] == MazeConstants.FINISH_P4 ||
            		temp[i][j] == MazeConstants.START_P1 || temp[i][j] == MazeConstants.START_P2 || temp[i][j] == MazeConstants.START_P3 || temp[i][j] == MazeConstants.START_P4
            	) {
             
            		result.add( new Integer[] { i , j } );
            		
            	}
            	
            }
    	
    	}
    	
    	return result;
    	
    }
    
    protected ArrayList<Integer[]> generateSolution() {
    	
    	return generateSolution( this.maze , 1 , 1 , rows - 2 , columns - 2 );
    	
    }
 
    
    /**
     * Generate a solution for a grid maze
     * 
     * This is a static Utility method which will work on any maze that has a start and finish point defined and follows the MazeConstants structure.
     * 
     * NOTE! This method overwrites the structure of the data parameter given. 
     * Therefore if you use this to solve a maze, you should pass a copy of the original maze's data structure.
     * 
     * This method is recursive and it spreads in all directions from a specified point attempting to locate a walkable path.
     * Each node that has been visited is given a flag VISITED to make sure it is ignored in the next phase of the solver.
     * Once the solver has reached the finish point, it has constructed the walkable path cells with the index specified in MazeConstants.PATH.
	 *
     * @param data The maze's cells 2D array of integers 
     * @param row The starting point's row (where the player will begin)
     * @param col The starting point's column 
     * @param finishX The finish point's row
     * @param finishY The finish point's column
     * @return True if a solution has been found , False otherwise
     */
    protected static boolean solveMazeUtil( int[][] data , int row , int col , int finishX , int finishY ) {
    	
        // Try to solve the maze by continuing current path from position
        // (row,col).  Return true if a solution is found.  The maze is
        // considered to be solved if the path reaches the finish position (lower right cell by default).
     
        if ( data[row][col] == MazeConstants.WALKABLE || data[row][col] == MazeConstants.START || data[row][col] == MazeConstants.FINISH ) {
        	
        	data[row][col] = MazeConstants.PATH;      // add this cell to the path
         
            if ( row == finishX && col == finishY ) {
            	
                return true;  // path has reached goal
                
            }
            
             
            
            // Extend the current cell in all four directions and attempt to solve maze recursively
            if (
            	solveMazeUtil( data , row-1 , col , finishX , finishY ) || 
            	solveMazeUtil( data , row , col-1 , finishX , finishY ) || 
            	solveMazeUtil( data , row+1 , col , finishX , finishY ) || 
            	solveMazeUtil( data , row , col+1 , finishX , finishY ) 
            ) {
                
            	return true;
                
            }
            
            // maze can't be solved from this cell, so backtrack out of the cell
            data[row][col] = MazeConstants.VISITED;   // mark cell as having been visited
           
        }
        
        return false;
        
    }
    
    /**
     * Print the current maze to the console in a structured way
     */
    protected void print() {
    	
    	print( this.maze );
    	
    }
    
    /**
     * Print the contents of a 2D grid maze to the console in a readable format
     * 
     * This method also works with cropped parts of the maze
     * 
     * @param maze The 2D maze array
     */
    protected static void print( int[][] maze ) {
    	
    	if( maze == null ) {
    		
    		return;
    		
    	}
    	
    	int rows = maze.length;
    	int columns = maze[0].length;
    	
    	String sig = "";
    	
    	// Iterate through all cells in the maze		
    	for ( int i = 0; i < rows; i++ ) {
        	
            for ( int j = 0; j < columns; j++ ) {
            	
            	// Print the walls, walkable cells, start and finish points and solution paths
            	if( maze[ i ][ j ] == MazeConstants.WALKABLE || maze[ i ][ j ] == MazeConstants.VISITED ) {
            		
            		sig = "   ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.WALL ) {
            	
            		sig = " □ ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.PATH ) {
            		
            		sig = " * ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.START ) {
            		
            		sig = " S ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.FINISH ) {
            		
            		sig = " F ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.START_P1 ) {
            		
            		sig = "S1 ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.START_P2 ) {
            		
            		sig = "S2 ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.START_P3 ) {
             		
             		sig = "S3 ";
             		
             	} else if ( maze[ i ][ j ] == MazeConstants.START_P4 ) {
             		
             		sig = "S4 ";
             		
             	} else if ( maze[ i ][ j ] == MazeConstants.FINISH_P1 ) {
            		
            		sig = "F1 ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.FINISH_P2 ) {
            		
            		sig = "F2 ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.FINISH_P3 ) {
            		
            		sig = "F3 ";
            		
            	} else if ( maze[ i ][ j ] == MazeConstants.FINISH_P4 ) {
            		
            		sig = "F4 ";
            		
            	}
            	
                System.out.print( sig );
                
            }
         
            System.out.println();
            
        }
    	
    	
    }
    

}