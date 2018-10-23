package server.mazegeneration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

class MazeGenerationTests {

	@Rule
	private final ExpectedException exception = ExpectedException.none();

// ProceduralMaze
	// getRows()
	// getColumns()
	@Test
	void test() {
		// minimum case
		ProceduralMaze mazeTest = new ProceduralMaze(9, 9);
		int expectedRows = 9;
		int expectedColumns = 9;
		assertEquals(expectedRows, mazeTest.getRows());
		assertEquals(expectedColumns, mazeTest.getColumns());
		// less than minimum
		ProceduralMaze mazeTest1 = new ProceduralMaze(1, 1);
		assertEquals(expectedRows, mazeTest1.getRows());
		assertEquals(expectedColumns, mazeTest1.getColumns());
		// even number given
		ProceduralMaze mazeTest2 = new ProceduralMaze(10, 10);
		int expectedRows2 = 11;
		int expectedColumns2 = 11;
		assertEquals(expectedRows2, mazeTest2.getRows());
		assertEquals(expectedColumns2, mazeTest2.getColumns());
	}

// MazeGenerator
	// intToChar2DArray
	@Test
	public void test2() {
		//test exception
		exception.expect(NullPointerException.class);
		int[][] empty = {{}};
		MazeGenerator.intToChar2DArray(empty);
		//test small int[][]
		int[][] test = {{0,1,2}, {3,4,5}, {6,7,8}};
		assertEquals(3, MazeGenerator.intToChar2DArray(test).length);
		assertEquals(3, MazeGenerator.intToChar2DArray(test)[0].length);
	}

	//getCroppedBoundaries
	@Test
	public void test3() {
		MazeGenerator test = new MazeGenerator(16);
		int[] expected = {0,8,0,8};
		int [] obtained = test.getCroppedBoundaries(4, 4, 4, 4);
		assertArrayEquals(expected, obtained);
	}
	
	
	
	/*
	 * public char[][] getMazeChar() public int[] 
	 * 
	 * public static int[] getAdjustedPlayerPosition( int x , int y , int[] boundaries ) 
	 * public int[][] getCroppedMaze( int[] boundaries ) 
	 * public boolean[][] getCoins() 
	 * public int countCoins() public boolean[][]
	 * getCroppedCoins( int[] boundaries ) 
	 * public boolean collectCoin( int x , int y) 
	 * public ArrayList<Integer[]> getTopLeftSolution() 
	 * public ArrayList<Integer[]> getTopRightSolution() 
	 * public ArrayList<Integer[]> getBottomLeftSolution() 
	 * public ArrayList<Integer[]> getBottomRightSolution()
	 * protected static int[][] mirrorArrayHorizontal( int[][] data ) - test with simple array[][] data 
	 * protected static int[][] mirrorArrayVertical( int[][] data ) 
	 * protected static int[][] mirrorArrayHorizontalAndVertical( int[][] data ) 
	 * protected static int[][] mirrorArray( int[][] data , int method )
	 * protected ArrayList<Integer[]> mirrorArrayList( ArrayList<Integer[]> data , int method ) 
	 * protected ArrayList<Integer[]> mirrorArrayListHorizontal(ArrayList<Integer[]> data ) 
	 * protected ArrayList<Integer[]> mirrorArrayListVertical( ArrayList<Integer[]> data ) 
	 * protected ArrayList<Integer[]> mirrorArrayListHorizontalAndVertical(ArrayList<Integer[]> data ) 
	 * public static String convertMazeToString( int[][] data , char rowDelimiter , char colDelimiter ) 
	 * public static String convertCoinsToString( boolean[][] data , char rowDelimiter , char colDelimiter )
	 */

}
