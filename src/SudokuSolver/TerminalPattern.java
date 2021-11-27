package SudokuSolver;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A Las Vegas algorithm is used to generated valid Sudoku terminal patterns. A 'terminal pattern' is
 * a fully solved Sudoku grid which can be then "dug" (by emptying some of its cells) until a new puzzle
 * is obtained, the solution of which is the pattern itself. This class provides a method that can fulfill
 * this task in a matter of milliseconds.
 * 
 * @author Nicolás Moro
 */

class TerminalPattern {
	
	private static int givens = 11; // Confirmed as best choice to diversify puzzles based on 1000 tests on n=6 to n=20 for timeLimit=100
	private static long timeLimitPerPattern = 100; // in milliseconds
	
	/**
	 * Attempts to generate a terminal pattern of n givens (default: 11) under the specified time (default: 100ms).
	 * If the process fails, a new attempt is made.
	 * 
	 * @return  A full terminal pattern.
	 */
	
	static int[][] createPattern() {
		
		int[][] emptyGrid = {
								{0, 0, 0, 0, 0, 0, 0, 0, 0}, 
								{0, 0, 0, 0, 0, 0, 0, 0, 0}, 
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0},
							};
		
		boolean underTimeLimit = false;
		BoardState board = new BoardState(emptyGrid);
		
		while (!underTimeLimit) {
			
			final long START_TIME = System.currentTimeMillis(); // start timer
			
			/* Generate an initial random pattern of n givens that satisfies Sudoku's rules */
			
			int randomRow;
			int randomCol;
			int trialNum;
			
			int[] validNums;
			boolean validTrial;
			int givensCount = 0;
			while (givensCount < givens) {
				
				randomRow = ThreadLocalRandom.current().nextInt(0, 9);
				randomCol = ThreadLocalRandom.current().nextInt(0, 9);
				trialNum = ThreadLocalRandom.current().nextInt(1, 10);
				
				validNums = board.getValidNumbers(randomRow, randomCol);
				
				/* Try each of the possible numbers in this position. If none found, try with new coordinates. */
				
				validTrial = false;
				for (int n : validNums) {
					if (n == trialNum) {
						validTrial = true;
						break;
					}
				}
				
				if (validTrial) {
					board.grid[randomRow][randomCol] = trialNum;
					givensCount++;
					
				}
			}
			
			long currentTime = System.currentTimeMillis();
			long timeLimit = timeLimitPerPattern - (currentTime - START_TIME);
			
			/*Use the BacktrackingSolver class to fill the grid up*/
			
			boolean solutionFound;
			if (timeLimit > 0) {
				solutionFound = BacktrackingAlgorithm.gridSolver(board.grid, timeLimit);
			} else {
				solutionFound = false;
			}
			
			if (solutionFound) {
				underTimeLimit = true; // Terminal pattern generated successfully
			}
		}
		return board.grid;
	}	
}
