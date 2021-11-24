package SudokuSolver;

import java.util.Arrays; // TESTING PURPOSES
import java.util.Random;

public class PuzzleGenerator {
	
	// Declaring restriction parameters on the puzzle's generation (see OPERATOR 2)
	static int totalBound;
	static int rowcolBound;
	static long time_limit_per_puzzle = 1000;
	static boolean exceed_time_limit;
	
	// Main method: generates a puzzle for the desired level of difficulty

	public static int[][] generate(int[][] grid, int level) {
		
		long startTime = System.currentTimeMillis(); // start timer
		exceed_time_limit = false;
		
		int[][] digging_pattern = generateDiggingPattern(level);
		restrictionOnGivens(level);
		int[] row_givens = {9,9,9,9,9,9,9,9,9};
		int[] col_givens = {9,9,9,9,9,9,9,9,9};
		
		int dug_cells = 0;
		int indx = 0;
		while (dug_cells < 81-totalBound && indx <= 80 && !exceed_time_limit) {

			int[] next_cell = digging_pattern[indx];
			int r = next_cell[0];
			int c = next_cell[1];
			
			boolean violates_restriction = (row_givens[r]-1 < rowcolBound) || (col_givens[c]-1 < rowcolBound); 
			
			if (!violates_restriction) {
				
				boolean can_be_dug = checkUniqueness(grid, r, c, startTime); // computationally longest step

				if (can_be_dug) {
					grid[r][c] = 0;
					row_givens[r]--;
					col_givens[c]--;
					dug_cells++;
				}
			}
			indx++;
			long currentTime = System.currentTimeMillis();
			exceed_time_limit = (currentTime-startTime) >= time_limit_per_puzzle;
		}
		
		if (exceed_time_limit) {
			grid[0][0] = -1; // notifies Main that a Puzzle could not be generated in the desired time, so a new attempt is made
		}
		
		return grid;
	}

	// OPERATOR 1
	
	public static int[][] generateDiggingPattern(int level) {
		
		int[][] digging_pattern = new int[81][2];
		
		// NOTE: Modified algorithm here; randomising globally is MUCH faster in creating evil-level puzzles
		// and equally fast (or faster) and generates better puzzles for levels 3 & 4
		switch (level) {
		case 1:
		case 2:
			digging_pattern = leftRightTopBottom(digging_pattern); // order of 10^2 us
			shufflePattern(digging_pattern);
			break;
		case 3:
			// NOTE: Modified algorithm here; randomizing globally is equally fast (or faster) and generates better puzzles
			digging_pattern = leftRightTopBottom(digging_pattern); // order of 10^2 us
			shufflePattern(digging_pattern);
			// digging_pattern = jumpOneCell(digging_pattern); // order of 10^1 us
			break;
		case 4:
			// NOTE: Modified algorithm here; randomizing globally is equally fast (or faster) and generates better puzzles
			digging_pattern = leftRightTopBottom(digging_pattern); // order of 10^2 us
			shufflePattern(digging_pattern);
			// digging_pattern = wanderAlongS(digging_pattern); // order of 10^1 us
			break;
		case 5:
			digging_pattern = leftRightTopBottom(digging_pattern); // order of 10^1 us
			shufflePattern(digging_pattern);
			break;
		}
		return digging_pattern;
	}
	
	public static void shufflePattern(int[][] array) {
		
		Random random = new Random();
		
		for (int i = array.length - 1; i > 0; i--) {
			int m = random.nextInt(i + 1);
			
			int[] temp = array[i];
			array[i] = array[m];
			array[m] = temp;
	    }
	}
	
	public static int[][] leftRightTopBottom(int[][] pattern) {
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				int indx = r*9+c;
				pattern[indx] = new int[]{r,c};
			}
		}
		return pattern;
	}
	
	// OPERATOR 2
	
	public static void restrictionOnGivens(int level) {
		
		Random random = new Random();
		int totalMin;
		int totalMax;
		
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
	
	// OPERATOR 3 & 4
	
	public static boolean checkUniqueness(int[][] current_grid, int r, int c, long startTime) {
		
		int current_number = current_grid[r][c];
		boolean solution_found = false;
		int trial_num = 1;
		int[][] grid_clone = deepCopy(current_grid);
		
		while (trial_num <= 9 && !exceed_time_limit) {
			
			if (trial_num != current_number) {
				grid_clone[r][c] = trial_num;
				
				long currentTime = System.currentTimeMillis();
				long time_limit = time_limit_per_puzzle - (currentTime - startTime);
				
				if (time_limit > 0) {
					solution_found = BacktrackingSolver.solve(grid_clone, time_limit);
				} else {
					exceed_time_limit = true;
				}
				
				if (solution_found) {
					break;
				}
			}
			trial_num++;
		}
		return !solution_found;
	}
	
	public static int[][] deepCopy(int[][] original) {
		
	    final int[][] result = new int[original.length][];
	    for (int i = 0; i < original.length; i++) {
	        result[i] = Arrays.copyOf(original[i], original[i].length);
	    }
	    return result;
	}
}
