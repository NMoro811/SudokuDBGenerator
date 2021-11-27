package SudokuSolver;

import java.util.Arrays;
import java.util.Random;

/**
 * This class solves the following problem: given a fully solved Sudoku grid, how can we generate a puzzle
 * of the desired level of difficulty (ranging from 1-"Very Easy" to 5-"Evil") in a matter of (milli)seconds?
 * 
 * To this end, the algorithm provided in Page 12 of the following article is used.
 * @see <a href="http://zhangroup.aporc.org/images/files/Paper_3485.pdf">Referenced article</a>.
 * 
 * @author Nicolás Moro.
 */

class GeneratingAlgorithm {
	
	// Declaring restriction parameters on the puzzle's generation (see OPERATOR 2).
	private static int totalBound;
	private static int rowcolBound;
	private final static long PUZZLE_TIME_LIM = 1000; // Time of 1 second; can be shortened, if desired.
	private static boolean exceedTimeLim;
	
	/**
	 * Returns a valid Sudoku puzzle satisfying the desired difficulty requirements.
	 * 
	 * @param grid  A fully-solved puzzle, i.e., a terminal pattern from {@link TerminalPattern}.
	 * @param level  From 1 to 5, corresponding to those described in the aforementioned article.
	 * @return A valid Sudoku puzzle as a 2D array, if the process finished under the desired limit. 
	 * Else, return a partially-dug grid with a "-1" in the 00-coordinate, indicating the Main method 
	 * that this attempt has failed.
	 */

	static int[][] generatePuzzle(int[][] grid, int level) {
		
		final long START_TIME = System.currentTimeMillis(); // start timer
		exceedTimeLim = false;
		
		int[][] diggingPattern = generateDiggingPattern(level); // Obtain digging sequence as a list of 81 coordinates
		restrictOnGivens(level); // Obtain relevant difficulty requirements
		// Keep track of how many givens there are per row and column
		int[] rowGivens = {9,9,9,9,9,9,9,9,9};
		int[] colGivens = {9,9,9,9,9,9,9,9,9};
		
		int dugCells = 0;
		int indx = 0;
		while (dugCells < 81-totalBound && indx <= 80 && !exceedTimeLim) {

			int[] nextCell = diggingPattern[indx];
			int r = nextCell[0];
			int c = nextCell[1];
			
			// Check if digging this cell would result in a harder puzzle than desired
			boolean violatesRestriction = (rowGivens[r]-1 < rowcolBound) || (colGivens[c]-1 < rowcolBound); 
			
			if (!violatesRestriction) {
				
				// Computationally longest step: check if digging the cell at {r,c} would still yield a unique solution
				boolean canBeDug = checkUniqueness(grid, r, c, START_TIME);
				
				// If so, dig the cell. Else, move on to the next one.
				if (canBeDug) {
					grid[r][c] = 0;
					rowGivens[r]--;
					colGivens[c]--;
					dugCells++;
				}
			}
			indx++;
			long currentTime = System.currentTimeMillis();
			exceedTimeLim = (currentTime-START_TIME) >= PUZZLE_TIME_LIM; // Check if time limit has been exceeded
		}
		
		if (exceedTimeLim) {
			grid[0][0] = -1; // Notifies Main that a Puzzle could not be generated in the desired time
		}
		
		return grid;
	}

	/* OPERATOR 1 */
	
	/**
	 * Given a difficulty level, it returns the order in which cells are to be scanned.
	 * 
	 * @param level  From 1 (easiest) to 5 (hardest)
	 * @return An array of 81 sets of {row,col}-coordinates in the corresponding order.
	 */
	
	private static int[][] generateDiggingPattern(int level) {
		
		int[][] diggingPattern = new int[81][2];
		
		/*
		 * Note: Modified algorithm here; randomising globally is MUCH faster in creating evil-level puzzles
		 * and equally fast (or faster) and generates better puzzles for levels 3 & 4 than the alternative
		 * methods proposed in the paper. This block of code has been maintained in this method for future
		 * potential modifications.
		 */
		
		diggingPattern = fillLeftRightTopBottom(diggingPattern);
		shufflePattern(diggingPattern);
		
		return diggingPattern;
	}
	
	/**
	 * Returns a list of all the coordinates of the grid in order, from left to right and top to bottom.
	 * 
	 * @param pattern  An empty list to be filled with the coordinates.
	 * @return  A list of 81 sets of coordinates in order.
	 */
	
	private static int[][] fillLeftRightTopBottom(int[][] pattern) {
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				int indx = r*9+c;
				pattern[indx] = new int[]{r,c};
			}
		}
		return pattern;
	}
	
	/**
	 * Randomly shuffle an array corresponding to an ordered search pattern.
	 * 
	 * @param An array containing coordinates of the grid.
	 */
	
	private static void shufflePattern(int[][] array) {
		
		Random random = new Random();
		
		for (int i = array.length - 1; i > 0; i--) {
			int m = random.nextInt(i + 1);
			
			int[] temp = array[i];
			array[i] = array[m];
			array[m] = temp;
	    }
	}
	
	/* OPERATOR 2 */
	
	/**
	 * Modifies fields {@code totalBound}, indicating the maximum number of total 
	 * givens, and {@code rowcolBound}, denoting the maximum number of givens per
	 * row and column.
	 * 
	 * @param level  Level of difficulty from 1 (easiest) to 5 (hardest).
	 */
	
	private static void restrictOnGivens(int level) {
		
		Random random = new Random();
		int totalMin;
		int totalMax;
		
		/* Values provided in the literature */
		
		switch(level) {
			case 1:
				totalMin = 50;
				totalMax = 65;
				totalBound = random.nextInt(totalMax - totalMin) + totalMin;
				rowcolBound = 5;
				break;
			case 2:
				totalMin = 36;
				totalMax = 49;
				totalBound = random.nextInt(totalMax+1 - totalMin) + totalMin;
				rowcolBound = 4;
				break;
			case 3:
				totalMin = 32;
				totalMax = 35;
				totalBound = random.nextInt(totalMax+1 - totalMin) + totalMin;
				rowcolBound = 3;
				break;
			case 4:
				totalMin = 28;
				totalMax = 31;
				totalBound = random.nextInt(totalMax+1 - totalMin) + totalMin;
				rowcolBound = 2;
				break;
			case 5:
				totalMin = 22;
				totalMax = 27;
				totalBound = random.nextInt(totalMax+1 - totalMin) + totalMin;
				rowcolBound = 0;
				break;
		}
	}
	
	/* OPERATOR 3 & 4 */
	
	/**
	 * Check whether a given grid still has a unique solution after substituting the
	 * number in a cell for any other potential candidates. If so, the cell at said
	 * coordinates cannot be dug (i.e., would not yield a unique solution).
	 * 
	 * @param currentGrid  The Sudoku grid to be checked, with the original number still in place.
	 * @param r  Row coordinate of the cell under scrutiny
	 * @param c  Column coordinate of the cell under scrutiny
	 * @param START_TIME  Time at which {@code generatePuzzle()} was called.
	 * @return A boolean indicating whether the cell at the given coordinates can be dug
	 * to yield a uniquely solvable puzzle.
	 */
	
	private static boolean checkUniqueness(int[][] currentGrid, int r, int c, long START_TIME) {
		
		int currentNum = currentGrid[r][c]; // Current number in place
		boolean solutionFound = false;
		int trialNum = 1;
		int[][] gridCopy = deepCopy(currentGrid); // Deep-copy to leave original grid unchanged.
		
		/* Attempt to solve the same puzzle after substituting currentNum by any other potential candidate */
		
		while (trialNum <= 9 && !exceedTimeLim) {
			
			if (trialNum != currentNum) {
				gridCopy[r][c] = trialNum;
				
				long currentTime = System.currentTimeMillis();
				long timeLim = PUZZLE_TIME_LIM - (currentTime - START_TIME);
				
				if (timeLim > 0) {
					solutionFound = BacktrackingAlgorithm.gridSolver(gridCopy, timeLim);
				} else {
					exceedTimeLim = true;
				}
				
				if (solutionFound) {
					break;
				}
			}
			trialNum++;
		}
		return !solutionFound;
	}
	
	/**
	 * Generate a deep copy of a 2D grid.
	 * 
	 * @param original  Array to be cloned.
	 * @return A copy of the original array.
	 */
	
	static int[][] deepCopy(int[][] original) {
		
	    final int[][] result = new int[original.length][];
	    for (int i = 0; i < original.length; i++) {
	        result[i] = Arrays.copyOf(original[i], original[i].length);
	    }
	    return result;
	}
}
