package SudokuSolver;

import java.util.HashMap;
import java.util.HashSet;

public class BoardState {
	
	public int[][] grid;
	public boolean valid_grid = true; // by default
	public int num_empty_cells = 0;
	public int[][] listEmptyCells;
	public HashMap<int[], HashSet<Integer>> backtrackingMap = new HashMap<int[], HashSet<Integer>>();
	
	BoardState(int[][] grid) {
		this.grid = grid;
	}
	
	// To be executed at the beginning; corrects the value of num_empty_cells (MAY CHANGE THIS LATER)
	void display() {
		for (int r=0; r<=8; r++) {
			System.out.println();
			for (int c=0; c<=8; c++) {
				if(this.grid[r][c] == 0) {
					System.out.print("#");
			}
				else {
					System.out.print(this.grid[r][c]);
				}
			}
		}
		System.out.println();
		System.out.println();
	}
	
	void storeEmptyCells() {
		
		// Determine number of empty cells
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				if(this.grid[r][c] == 0) {
					num_empty_cells++;
			}
				else {
				}
			}
		}
		
		// Note: for computational efficiency's sake, it is crucial to fill the cells in order (L -> R and T -> B), which
		// is why they must be stored in order in an int[][] (as opposed to storing them in a HashSet and then converting toArray).
		
		listEmptyCells = new int[this.num_empty_cells][2];
		int indx = 0;
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				if(this.grid[r][c] == 0) {
					int[] key = {r,c};
					this.listEmptyCells[indx] = key;
					indx++;
					this.backtrackingMap.put(key, new HashSet<Integer>());
				}
			}
		}
	}
	
	public HashSet<Integer> getRowConstraints(int r) {
		HashSet<Integer> rowConstraints = new HashSet<Integer>();
		
		for (int c=0; c<=8; c++) {
			
			if (rowConstraints.contains(this.grid[r][c])) {
				this.valid_grid = false;
			}
			
			if(this.grid[r][c] != 0) {
				rowConstraints.add(this.grid[r][c]);
			}
		}
		return rowConstraints;
	}
	
	public HashSet<Integer> getColConstraints(int c) {
		HashSet<Integer> colConstraints = new HashSet<Integer>();
		
		for (int r=0; r<=8; r++) {
			
			if (colConstraints.contains(this.grid[r][c])) {
				this.valid_grid = false;
			}
			
			if(this.grid[r][c] != 0) {
				colConstraints.add(this.grid[r][c]);
			}
		}
		return colConstraints;
	}
	
	public HashSet<Integer> getBlockConstraints(int r, int c) {
		HashSet<Integer> blockConstraints = new HashSet<Integer>();
		
		int[][] row_col_groups = {
									{0,1,2},
									{3,4,5},
									{6,7,8}
								};
		
		int[] cols_of_block = row_col_groups[(c-(c%3))/3];
		int[] rows_of_block = row_col_groups[(r-(r%3))/3];
		
		for(Integer col : cols_of_block) {
			for (Integer row : rows_of_block) {
				
				if (blockConstraints.contains(this.grid[r][c])) {
					this.valid_grid = false;
				}
				
				if(this.grid[row][col] != 0) {
					blockConstraints.add(this.grid[row][col]);
				}
			}
		}
		return blockConstraints;
		
	}
	
	public int[] getValidNumbers(int r, int c) {
		
		HashSet<Integer> validNumbers = new HashSet<Integer>();
		
		if (this.grid[r][c] == 0) {
			
			HashSet<Integer> allConstraints = new HashSet<Integer>();
			allConstraints.addAll(getRowConstraints(r));
			allConstraints.addAll(getColConstraints(c));
			allConstraints.addAll(getBlockConstraints(r,c));
			
			for(int i=1; i<=9; i++) {
				if (!allConstraints.contains(i)) {
					validNumbers.add(i);
				}
			}
		}
		// Converting HashSet to Array by looping over all elements
        int[] valid_nums = new int[validNumbers.size()];
        int indx = 0;
        for (Integer i : validNumbers) {
        	  valid_nums[indx++] = i;
        	}
		return valid_nums;
	}
	
}
