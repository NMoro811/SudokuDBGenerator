package SudokuSolver;

import java.util.HashSet;

public class BacktrackingSolver {
	
	int [][] testing_puzzle = {
									{0, 0, 0, 0, 0, 0, 0, 0, 0}, 
									{0, 0, 0, 0, 0, 0, 5, 2, 3}, 
									{0, 0, 0, 0, 0, 0, 0, 1, 8},
									{0, 0, 0, 0, 0, 0, 0, 0, 0},
									{0, 0, 9, 0, 7, 4, 0, 6, 0},
									{0, 0, 4, 6, 1, 0, 0, 0, 7},
									{0, 5, 8, 0, 4, 3, 0, 0, 0},
									{0, 4, 0, 0, 2, 0, 0, 3, 0},
									{0, 6, 7, 0, 8, 1, 0, 9, 4},
								};

	public static boolean solve(int[][] sudoku_puzzle) {
		
		// Start of run timing
		// long startTime = System.nanoTime();
		
		BoardState board = new BoardState(sudoku_puzzle); // MAY SLOW PROGRAM DOWN, BUT WILL KEEP FOR NOW
		
		// Display initial board state
		// System.out.println("Beginning problem: ");
		board.display();
		// Store locations of each initially empty cell and map them to a list of previously tried candidates for that cell
		board.storeEmptyCells();
		
		// Depth-first search algorithm to solve the given Sudoku puzzle - Backtracking method
		int row_coord;
		int col_coord;
		int[] valid_nums;
		HashSet<Integer> previous_candidates;
		
		// int numbers_used = 0; // TESTING PURPOSES ONLY
		
		int cell_index = 0;
		int empty_cells = board.num_empty_cells;
		
		while (cell_index < empty_cells) {
			
			// numbers_used++; // TESTING PURPOSES ONLY
			
			// For the given empty cell, identify which numbers are valid and which ones have been used before and do not solve the puzzle
			int[] coords = board.listEmptyCells[cell_index];
			row_coord = coords[0];
			col_coord = coords[1];
			board.grid[row_coord][col_coord] = 0; // Empty the cell if filled with a wrong number before.
			valid_nums = board.getValidNumbers(row_coord, col_coord); // initially valid
			
			if (!board.valid_grid) {
				break;
			}
			
			previous_candidates = board.backtrackingMap.get(coords); // have been tried before but do not lead to a solution
			
			// Set candidate_num to the next integer to try and fill the present cell. 
			// Set it to 0 if we need to backtrack, i.e., none of the numbers in this cell lead to a solution, so a previous entry has to be changed
			int candidate_num = 0;
			for(Integer i : valid_nums) {
				if (!previous_candidates.contains(i)) {
					candidate_num = i;
					break;
				}
			}
			
			// If successful, fill the candidate number in the empty cell and add it to the corresponding HashMap entry of "previously tried"
			// Else, empty the "previously tried" list and go to the cell we have filled prior to this one
			if (candidate_num != 0) { 
				board.grid[row_coord][col_coord] = candidate_num;
				previous_candidates.add(candidate_num);
				board.backtrackingMap.put(coords, previous_candidates);
				cell_index++;
			} else if (candidate_num == 0 && cell_index == 0) {
				break; // Another option: no candidates left in the very first cell, cannot go back -> Unsolvable
			} else {
				board.backtrackingMap.put(coords, new HashSet<Integer>());
				cell_index--;
			}
		}
		
		// Either the puzzle cannot be solved or the solution is given
		// System.out.println();
		if (cell_index == 0 || !board.valid_grid) {
			// System.out.println("The Sudoku puzzle you have introduced cannot be solved!");
			return false;
		} else {
			// System.out.println("The puzzle has been successfully solved! Solution: ");
			// board.display();
			return true;
		}
		
		// End of run timing
		
		// long endTime   = System.nanoTime();
		// long totalTime = (endTime - startTime)/1000000;
		// System.out.println("Total running time: "+totalTime+"ms.");
		// System.out.println("Numbers used in test: "+numbers_used);
		
	}

}
