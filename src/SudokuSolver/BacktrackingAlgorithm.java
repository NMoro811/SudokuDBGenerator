package SudokuSolver;

import java.util.HashSet;

/**
 * The problem is as follows: Given any 9x9 grid representing a Sudoku puzzle, determine whether or not
 * this puzzle can be solved (under the desired time limit) by using a depth-first search algorithm.
 * Overall, this is used throughout the package not only to solve puzzles but to check if they are
 * valid and have unique solutions.
 * 
 * @author Nicolás Moro
 */

class BacktrackingAlgorithm {
	
	/**
	 * Returns a boolean indicating whether a solution has been found within the desired time limit.
	 * 
	 * @param sudoku_puzzle  A 2D (9x9) grid representing a Sudoku puzzle. Empty cells are filled by 0.
	 * @param timeLimit  Desired time of execution in miliseconds. If set to 0, there is no upper bound for the computing time.
	 * @return A boolean that indicates whether said puzzle has been solved under the desired time limit (true).
	 * The program also returns 'false' if the grid is determined invalid in {@link BoardState}.
	 */
	
	static boolean gridSolver(int[][] sudoku_puzzle, long timeLimit) {
		
		final long START_TIME = System.currentTimeMillis();
		
		BoardState board = new BoardState(sudoku_puzzle);

		board.storeEmptyCells();
		
		int rowCoord;
		int colCoord;
		int[] validNums;
		HashSet<Integer> previousCandidates; // To be used to store previously used candidates to fill a cell which do not yield a (unique) solution.
		
		int cellIndex = 0;
		int emptyCells = board.numEmptyCells;
		boolean continueSearch = cellIndex < emptyCells;
		
		// For the given empty cell, identify which numbers are valid and which ones have been used before and do not solve the puzzle
		while (continueSearch) {
			
			int[] coords = board.listEmptyCells[cellIndex];
			rowCoord = coords[0];
			colCoord = coords[1];
			board.grid[rowCoord][colCoord] = 0; // Empty the cell if filled with a wrong number before.
			validNums = board.getValidNumbers(rowCoord, colCoord); // Initially valid
			
			if (!board.validGrid) {
				break; // If puzzle determined invalid in BoardState class, immediately stop the program
			}
			
			previousCandidates = board.backtrackingMap.get(coords); // have been tried before but do not lead to a solution
			
			/*
			 * Set candidateNum to the next integer to try and fill the present cell.
			 * Set it to 0 if we need to backtrack, i.e., none of the numbers in this cell lead to a solution, so a previous entry has to be changed.
			 */
			
			int candidateNum = 0;
			for(Integer i : validNums) {
				if (!previousCandidates.contains(i)) {
					candidateNum = i;
					break;
				}
			}
			
			/*
			 * If successful, fill the candidate number in the empty cell and add it to the corresponding HashMap entry of "previously tried".
			 * Else, empty the "previously tried" list and go to the cell we have filled prior to this one.
			 */
			
			if (candidateNum != 0) { 
				board.grid[rowCoord][colCoord] = candidateNum;
				previousCandidates.add(candidateNum);
				board.backtrackingMap.put(coords, previousCandidates);
				cellIndex++;
			} else if (candidateNum == 0 && cellIndex == 0) {
				break; // Another option: no candidates left in the very first cell, cannot go back -> Unsolvable
			} else {
				board.backtrackingMap.put(coords, new HashSet<Integer>());
				cellIndex--;
			}
			
			// Check if backtracking continues or if the desired time was exceeded
			if (timeLimit == 0) {
				continueSearch = cellIndex < emptyCells;
			} else {
				continueSearch = (cellIndex < emptyCells) && (System.currentTimeMillis()-START_TIME)<timeLimit; 
			}
			
		}
		
		// Either the puzzle has been solved (in time) or return 'false'

		if (cellIndex == 0 || !board.validGrid) {
			return false;
		} else {
			return true;
		}
	}
}