package SudokuSolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;

public class Main {
	
	static int lvl = 5;
	
	// Generate billions of distinct Sudoku puzzles per level of difficulty and store them in a dedicated SQL database
	
	static long seed_puzzles = 10; // number of generated seed puzzles
	static long max_puzzles = 1000000; // maximum number of generated puzzles: up to 26,127,360
	
	static int count_puzzles;
	static int total_count;
	static boolean max_reached;
	
	static int[][] grid;
	static int[][] solved_grid;
	static Connection conn = null;
	static PreparedStatement st;
	
	static Random r = new Random();
	static String rating;
	public static final DecimalFormat df = new DecimalFormat("0.00");
	
	public static void main(String[] args) {
		
		// Create dedicated DB to store all the puzzles and their solutions
		DBManager.createDB();
		
		long startTime = System.currentTimeMillis();
		System.out.println("Generating Sudoku puzzles and storing them in the Database...\n");
		boolean generated_under_time_limit;
		
		// Setting up new connection to store puzzles
		try {
			Class.forName(DBManager.DRIVER);
			conn = DriverManager.getConnection(DBManager.NEW_DB_URL, DBManager.USER, DBManager.PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Check max_reached input and correct it if necessary (there are only so many puzzles each seed can generate)
		long max_propagations = 4*3*6*factorial(9);
		if (max_puzzles == 0 || max_puzzles > max_propagations) {
			max_puzzles = max_propagations;
		}
		
		for (int seed=1; seed<=seed_puzzles; seed++) {
			
			count_puzzles = 0; // reset counter
			max_reached = false;
			
			generated_under_time_limit = false;
			while (!generated_under_time_limit) {
				
				// Create a terminal pattern using a Las Vegas algorithm for n=11 givens
				solved_grid = TerminalPatternCreator.create_pattern();
				
				// create deepCopy to input in PuzzleGenerator
				grid = PuzzleGenerator.deepCopy(solved_grid);
				// Generate a puzzle of the desired level of difficulty
				grid = PuzzleGenerator.generate(grid, lvl);
				
				if (grid[0][0] != -1) {
					generated_under_time_limit = true;
				}
				
			}
			
			// Propagate --> Store all copies in an external database in a suitable format
			// x4 combinations
			for (int rot=1; rot<=4 && !max_reached; rot++) {
				grid = rotateClockwise(grid);
				solved_grid = rotateClockwise(solved_grid);
				
				// x3 combinations
				for (int block=0; block<=2 && !max_reached; block++) {
					// x6 combinations
					for (int i=0; i<=1; i++) {
						// Swap second and third columns
						grid = swapColumns(grid, block, 1, 2);
						solved_grid = swapColumns(solved_grid, block, 1, 2);
						for (int j=0; j<=2 && !max_reached; j++) {
							
							// Move each column to the right
							grid = swapColumns(grid, block, 0, 2);
							grid = swapColumns(grid, block, 1, 2);
							solved_grid = swapColumns(solved_grid, block, 0, 2);
							solved_grid = swapColumns(solved_grid, block, 1, 2);
							
							// Convert each propagated puzzle to String and store it in a dedicated DB
							exchangeTwoDigits(grid, solved_grid, 1); //x(9!) combinations
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
		System.out.println("Finished in "+(endTime-startTime)+" ms. "+seed_puzzles*max_puzzles+" puzzles generated and stored in the database.");
	}
	
	// OPERATOR 5
	static int[] sudoku_nums = {1,2,3,4,5,6,7,8,9};
	static int[][] new_sol;
	static int[][] new_puzzle;
	static String string_puzzle; // To be stored in the database for each new puzzle
	static String string_sol;
	
	// Different propagation methods (excl. swapping blocks of columns; deemed unnecessary)
	public static void exchangeTwoDigits(int[][] grid, int[][] solution, int current_digit) {
		
		// New puzzle created! -> Store to database
		if (current_digit == 9) {
			
			// Make new copy of Grid reutilising the function from PuzzleGenerator class
			new_puzzle = PuzzleGenerator.deepCopy(grid);
			new_sol = PuzzleGenerator.deepCopy(solution);
			
			// Swap digits in grid using new permutation
			int current_num;
			int new_num;
			for (int r=0; r<9; r++) {
				for (int c=0; c<9; c++) {
					current_num = new_puzzle[r][c];
					current_num = new_sol[r][c];
					if (current_num != 0) {
						new_num = sudoku_nums[current_num-1];
						if (new_num != current_num) {
							new_puzzle[r][c] = new_num;
							new_sol[r][c] = new_num;
						}
					}
				}
			}

			// Convert to string
			string_puzzle = puzzleToString(new_puzzle);
			string_sol = puzzleToString(new_sol);
			rating = df.format(4.00 + (r.nextInt(11) / 10.0)); // generate a random rating between 4.00 and 5.00
			
			// Store string_puzzle in the dedicated SQL database
			
			try {
				st = conn.prepareStatement("INSERT INTO level"+lvl+" (Puzzle, PuzzleSol, Rating) VALUES (?, ?, ?)");
				st.setString(1, string_puzzle);
				st.setString(2, string_sol);
				st.setString(3, rating);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			count_puzzles++;
			total_count++;
			if (total_count % 100000 == 0) {
				System.out.println(total_count);
			}
			// Stop the program immediately if the desired number of max_puzzles has been generated and stored
			if (count_puzzles == max_puzzles) {
				max_reached = true;
			}

		}
		
		// Find all 9! permutations of Sudoku numbers and use them to exchange corresp. digits in grid
		int temp; // May get rid of in the future
		for (int i = current_digit; i<=9 && !max_reached; i++) {
			// Swap two digits
			temp = sudoku_nums[current_digit-1];
			sudoku_nums[current_digit-1] = sudoku_nums[i-1];
			sudoku_nums[i-1] = temp;
	        
			// Move on to next digit
			exchangeTwoDigits(grid, solved_grid, current_digit+1);
			
			// Un-swap the two initial digits
			temp = sudoku_nums[current_digit-1];
			sudoku_nums[current_digit-1] = sudoku_nums[i-1];
			sudoku_nums[i-1] = temp;
		}
	}
	
	public static int[][] swapTwoCells(int[][] grid, int r1, int c1, int r2, int c2) {
		
		grid[r1][c1] = grid[r1][c1] + grid[r2][c2];
		grid[r2][c2] = grid[r1][c1] - grid[r2][c2];
		grid[r1][c1] = grid[r1][c1] - grid[r2][c2];
		
		return grid;
	}
	
	public static int[][] swapColumns(int[][] grid, int block_col, int c1, int c2) {
		
		boolean invalid_input = block_col > 2 || c1 > 2 || c2 > 2 || c1 == c2;
		if (!invalid_input) {
			for (int r=0; r<=8; r++) {
				grid = swapTwoCells(grid, r, 3*block_col+c1, r, 3*block_col+c2);
			}
		}
		return grid; // If the input is invalid, the initial grid is returned unchanged
	}
	
	public static int[][] rotateClockwise(int[][] grid) {
		
		int[][] rotated_grid = new int[9][9];
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				rotated_grid[c][8-r] = grid[r][c];
			}
		}
		return rotated_grid;
	}
	
	// Convert Sudoku puzzles to Strings of a specific format
	public static String puzzleToString(int[][] puzzle) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				sb.append(puzzle[r][c]);
			}
			sb.append(";");
		}
		return sb.toString();
	}
	
	// Calculate factorial
	public static long factorial(int number) {
	    long result = 1;

	    for (int factor = number; factor >= 2; factor--) {
	        result *= factor;
	    }
	    return result;
	}
	
}
