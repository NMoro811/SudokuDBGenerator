package SudokuSolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * This class is able to generate hundreds of thousands of combinations of distinct
 * Sudoku puzzles of varying levels of difficulty, classifying them and storing them
 * in a dedicated SQL database. These can later be used to build software or mobile
 * Sudoku games and can be imported to be used in any multi-purpose programming language
 * since both the puzzles and their solutions are encoded as strings in the database.
 * The fields {@code SEED_PUZZLES} and {@code maxPuzzles} can be adjusted to fit the
 * user's needs and their PC's performance when generating the puzzles.
 * 
 * @author Nicolás Moro
 */

class Main{
	
	// Determine number of puzzles to be created per difficulty level
	private final static long SEED_PUZZLES = 10; // number of generated seed puzzles
	private static long maxPuzzles = 1000; // maximum number of generated puzzles: up to 26,127,360
	private static int lvl; // level of difficulty
	
	private static int countPuzzles; // per pattern
	private static int totalCount; // per difficulty level
	private static boolean maxReached; // stop the program
	
	private static int[][] grid; // store puzzle
	private static int[][] solvedGrid; // store solution
	private static Connection conn = null;
	private static PreparedStatement st;
	
	private static Random r = new Random();
	private static String rating; // random initial rating for each puzzle
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	
	/**
	 * Generate the desired number of puzzles and store them in the database.
	 * 
	 * @param args  The command line arguments.
	 * @throws SQLException when connection to the database fails or the database already exists.
	 * @throws ClassNotFoundException when failed to set up driver.
	 */
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		
		// Create dedicated DB to store all the puzzles and their solutions
		DatabaseManagement.createDB();
		
		long startTime = System.currentTimeMillis();
		System.out.println("Generating Sudoku puzzles and storing them in the Database...\n");
		boolean generatedOnTime;
		
		// Setting up new connection to store puzzles
		try {
			Class.forName(DatabaseManagement.DRIVER);
			conn = DriverManager.getConnection(DatabaseManagement.NEW_DB_URL, DatabaseManagement.USER, DatabaseManagement.PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Check maxReached input and correct it if necessary (there are only so many puzzles each seed can generate)
		final long MAX_PROPAGATIONS = 4*3*6*factorial(9);
		if (maxPuzzles == 0) {
			System.out.println("Note: maximum number of puzzles per seed is being generated; this may take a while...");
			maxPuzzles = MAX_PROPAGATIONS;
		} else if (maxPuzzles > MAX_PROPAGATIONS) {
			System.out.println("Warning: "+maxPuzzles+" exceeds the maximum number of possible puzzles/seed.\nReadjusting to "+MAX_PROPAGATIONS+"...");
			maxPuzzles = MAX_PROPAGATIONS;
		}
		
		// Iterate through all difficulty levels
		for(lvl=1; lvl<=5; lvl++) {
			System.out.println("\nLevel "+lvl+" puzzles: \n");
			totalCount = 0;
			for (int seed=1; seed<=SEED_PUZZLES; seed++) {
				
				countPuzzles = 0; // reset counter
				maxReached = false;
				
				/* Retry generating seed puzzle if previous attempt went over time. */
				
				generatedOnTime = false;
				while (!generatedOnTime) {
					
					// Create a terminal pattern using a Las Vegas algorithm for n givens
					solvedGrid = TerminalPattern.createPattern();
					
					// create deepCopy to input in PuzzleGenerator
					grid = GeneratingAlgorithm.deepCopy(solvedGrid);
					// Generate a puzzle of the desired level of difficulty
					grid = GeneratingAlgorithm.generatePuzzle(grid, lvl);
					
					if (grid[0][0] != -1) {
						generatedOnTime = true;
					}
					
				}
				
				// Propagate --> Store all copies in an external database in a suitable format
				// x4 combinations
				for (int rot=1; rot<=4 && !maxReached; rot++) {
					grid = rotateClockwise(grid);
					solvedGrid = rotateClockwise(solvedGrid);
					
					// x3 combinations
					for (int block=0; block<=2 && !maxReached; block++) {
						// x6 combinations
						for (int i=0; i<=1; i++) {
							// Swap second and third columns
							grid = swapColumns(grid, block, 1, 2);
							solvedGrid = swapColumns(solvedGrid, block, 1, 2);
							for (int j=0; j<=2 && !maxReached; j++) {
								
								// Move each column to the right
								grid = swapColumns(grid, block, 0, 2);
								grid = swapColumns(grid, block, 1, 2);
								solvedGrid = swapColumns(solvedGrid, block, 0, 2);
								solvedGrid = swapColumns(solvedGrid, block, 1, 2);
								
								// Convert each propagated puzzle to String and store it in a dedicated DB
								exchangeDigitsAndStore(grid, solvedGrid, 1); //x(9!) combinations
							}
						}
					}
				}
			}
		}
				
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Finished in "+(endTime-startTime)+" ms. "+5*SEED_PUZZLES*maxPuzzles+" puzzles generated and stored in the database.");
	}
	
	/* OPERATOR 5 */
	
	private final static int[] SUDOKU_NUMS = {1,2,3,4,5,6,7,8,9}; // Used as the default order of Sudoku symbols (i.e., numbers)
	private static int[][] newSol;
	private static int[][] newPuzzle;
	// To be stored in the database for each new puzzle
	private static String stringPuzzle;
	private static String stringSol;
	
	/*
	 * Different propagation methods described in the literature
	 * (excl. swapping blocks of columns; deemed unnecessary).
	 */
	
	/**
	 * Recursively generates up to 9! permutations off the same puzzle 
	 * by interchanging the order of the grid's digits. For instance, if
	 * the digits of a puzzle are displayed with respect to
	 * {1,2,3,4,5,6,7,8,9}, interchanging 1 and 2 and then 2 and 9 produces
	 * {9,1,3,4,5,6,7,8,2}, which displays a different puzzle.
	 * <p>
	 * The program stops either when all 9! puzzles have been generated or
	 * when the desired number of distinct puzzles has been reached. Afterwards,
	 * this method also takes care of storing all the generated puzzles in the DB.
	 * 
	 * @param grid  Propagated, original puzzle as a 2D array.
	 * @param solution  Original terminal pattern as a 2D array.
	 * @param currentDigit  Used for recursion.
	 * @throws SQLException when connection to the database fails.
	 */
	
	private static void exchangeDigitsAndStore(int[][] grid, int[][] solution, int currentDigit) throws SQLException {
		
		// New puzzle created -> Store to database
		if (currentDigit == 9) {
			
			// Make new copy of Grid re-utilising the function from PuzzleGenerator class
			newPuzzle = GeneratingAlgorithm.deepCopy(grid);
			newSol = GeneratingAlgorithm.deepCopy(solution);
			
			// Swap digits in grid using new permutation
			int currentNum;
			int newNum;
			for (int r=0; r<9; r++) {
				for (int c=0; c<9; c++) {
					currentNum = newPuzzle[r][c];
					currentNum = newSol[r][c];
					if (currentNum != 0) {
						newNum = SUDOKU_NUMS[currentNum-1];
						if (newNum != currentNum) {
							newPuzzle[r][c] = newNum;
							newSol[r][c] = newNum;
						}
					}
				}
			}

			// Convert to string and generate a random rating between 4.00 and 5.00
			stringPuzzle = convertPuzzleToString(newPuzzle);
			stringSol = convertPuzzleToString(newSol);
			rating = DF.format(4.00 + (r.nextInt(11) / 10.0));
			
			/* Store stringPuzzle in the dedicated SQL database */
			
			try {
				st = conn.prepareStatement("INSERT INTO level"+lvl+" (Puzzle, PuzzleSol, Rating) VALUES (?, ?, ?)");
				st.setString(1, stringPuzzle);
				st.setString(2, stringSol);
				st.setString(3, rating);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			countPuzzles++;
			totalCount++;
			long one_percent = (SEED_PUZZLES*maxPuzzles)/100;
			if (totalCount % one_percent == 0) {
				System.out.println((totalCount/one_percent)+"%");
			}
			// Stop the program immediately if the desired number of maxPuzzles has been generated and stored
			if (countPuzzles == maxPuzzles) {
				maxReached = true;
			}

		}
		
		// Find all 9! permutations of Sudoku numbers and use them to exchange corresp. digits in grid
		int temp;
		for (int i = currentDigit; i<=9 && !maxReached; i++) {
			// Swap two digits
			temp = SUDOKU_NUMS[currentDigit-1];
			SUDOKU_NUMS[currentDigit-1] = SUDOKU_NUMS[i-1];
			SUDOKU_NUMS[i-1] = temp;
	        
			// Move on to next digit
			exchangeDigitsAndStore(grid, solvedGrid, currentDigit+1);
			
			// Un-swap the two initial digits
			temp = SUDOKU_NUMS[currentDigit-1];
			SUDOKU_NUMS[currentDigit-1] = SUDOKU_NUMS[i-1];
			SUDOKU_NUMS[i-1] = temp;
		}
	}
	
	/**
	 * Swap digits of two cells.
	 * 
	 * @param grid  Sudoku puzzle as a 2D array.
	 * @param r1  Row coordinate of cell 1.
	 * @param c1  Column coordinate of cell 1.
	 * @param r2  Row coordinate of cell 2.
	 * @param c2  Column coordinate of cell 2.
	 * @return  Modified grid with the two cells swapped.
	 */
	
	private static int[][] swapTwoCells(int[][] grid, int r1, int c1, int r2, int c2) {
		
		grid[r1][c1] = grid[r1][c1] + grid[r2][c2];
		grid[r2][c2] = grid[r1][c1] - grid[r2][c2];
		grid[r1][c1] = grid[r1][c1] - grid[r2][c2];
		
		return grid;
	}
	
	/**
	 * Swap two columns of the same column block.
	 * 
	 * @param grid  Sudoku grid.
	 * @param blockCol  Index of the column block (from 0 to 2).
	 * @param c1  Index of column 1 within said block.
	 * @param c2  Index of column 2 within said block.
	 * @return  Modified grid.
	 */
	
	private static int[][] swapColumns(int[][] grid, int blockCol, int c1, int c2) {
		
		boolean invalidInput = blockCol > 2 || c1 > 2 || c2 > 2 || c1 == c2;
		if (!invalidInput) {
			for (int r=0; r<=8; r++) {
				grid = swapTwoCells(grid, r, 3*blockCol+c1, r, 3*blockCol+c2);
			}
		}
		return grid; // If the input is invalid, the initial grid is returned unchanged
	}
	
	/**
	 * Transpose the grid to generate a new puzzle.
	 * 
	 * @return Rotated grid.
	 */
	
	private static int[][] rotateClockwise(int[][] grid) {
		
		int[][] rotatedGrid = new int[9][9];
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				rotatedGrid[c][8-r] = grid[r][c];
			}
		}
		return rotatedGrid;
	}
	
	/**
	 * Encode a given puzzle as a string for easier and more
	 * accessible storage in the database.
	 * 
	 * @param puzzle  Puzzle to be transformed.
	 * @return A string containing the digits of the puzzle in
	 * order, separating rows by a semicolon.
	 */
	
	private static String convertPuzzleToString(int[][] puzzle) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				sb.append(puzzle[r][c]);
			}
			sb.append(";");
		}
		return sb.toString();
	}
	
	/**
	 * Calculate factorial of a given integer.
	 * 
	 * @param number  Argument of the factorial.
	 * @return Number in format 'long' as the factorial of the argument.
	 */
	private static long factorial(int number) {
	    long result = 1;

	    for (int factor = number; factor >= 2; factor--) {
	        result *= factor;
	    }
	    return result;
	}
	
}
