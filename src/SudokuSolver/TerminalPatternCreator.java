package SudokuSolver;

import java.util.concurrent.ThreadLocalRandom;

public class TerminalPatternCreator {

	static int givens = 11; // Confirmed as best choice to diversify puzzles based on 1000 tests on n=6 to n=20 for time_limit=0.1s
	static long time_limit_per_pattern = 100;
	
	public static int[][] create_pattern() {
		
		int[][] empty_grid = {
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
		
		boolean under_time_limit = false;
		BoardState board = new BoardState(empty_grid);
		
		while (!under_time_limit) {
			
			long startTime = System.currentTimeMillis(); // start timer
			
			// Generate an initial random pattern of n givens
			
			int random_row;
			int random_col;
			int trial_num;
			
			int[] valid_nums;
			boolean valid_trial;
			int givens_count = 0;
			while (givens_count < givens) {
				
				random_row = ThreadLocalRandom.current().nextInt(0, 9);
				random_col = ThreadLocalRandom.current().nextInt(0, 9);
				trial_num = ThreadLocalRandom.current().nextInt(1, 10);
				
				valid_nums = board.getValidNumbers(random_row, random_col);
				
				valid_trial = false;
				for (int n : valid_nums) {
					if (n == trial_num) {
						valid_trial = true;
						break;
					}
				}
				
				if (valid_trial) {
					board.grid[random_row][random_col] = trial_num;
					givens_count++;
					
				}
			}
			
			long currentTime = System.currentTimeMillis();
			long time_limit = time_limit_per_pattern - (currentTime - startTime);
			
			// Use the BacktrackingSolver class to fill the grid up
			boolean solution_found = BacktrackingSolver.solve(board.grid, time_limit);
			
			if (solution_found) {
				// Terminal pattern generated successfully
				under_time_limit = true;
			}
		}
		return board.grid;
	}	
}
