package SudokuSolver;

public class Main {

	static int[][] grid;
	
	public static void main(String[] args) {
		
		// Create a terminal pattern using a Las Vegas algorithm for n=11 givens
		grid = TerminalPatternCreator.create_pattern();
		
		// Generate a puzzle of the desired level of difficulty
		grid = PuzzleGenerator.generate(grid, 5);
		
		// Propagate --> Store all copies in an external database in a suitable format
		; // TODO
		
	}
	
	// OPERATOR 5
	public static void propagatePuzzle(int[][] grid) {
		final int[][] pattern = grid;
		
		// TODO
		
	}
	
	public static void swapTwoCells(int[][] grid, int r1, int c1, int r2, int c2) {
		
		grid[r1][c1] = grid[r1][c1] + grid[r2][c2];
		grid[r2][c2] = grid[r1][c1] - grid[r2][c2];
		grid[r1][c1] = grid[r1][c1] - grid[r2][c2];
	}
	
	// Different propagation methods (excl. swapping blocks of columns; deemed unnecessary)
	public static void exchangeTwoDigits(int[][] grid, int i, int j) {
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				if (grid[r][c] == i) {
					grid[r][c] = j;
				} else if (grid[r][c] == j) {
					grid[r][c] = i;
				}
			}
		}		
	}
	
	public static void swapColumns(int[][] grid, int c1, int c2) {

		for (int r=0; r<=8; r++) {
			swapTwoCells(grid, r, c1, r, c2);
		}
	}
	
	public static void rotateClockwise(int[][] grid) {
		
		int[][] rotated_grid = new int[9][9];
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				rotated_grid[c][8-r] = grid[r][c];
			}
		}
		grid = rotated_grid;
	}
}
