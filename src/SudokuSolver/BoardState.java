package SudokuSolver;

import java.util.HashMap;
import java.util.HashSet;

/**
* Stores a puzzle's grid and contains useful methods to solve it or check if a given solution is unique. 
*
* @author Nicolás Moro.
*/

class BoardState {
	
	int[][] grid;
	BoardState(int[][] grid) {
		
		this.grid = grid;
	}
	
	boolean validGrid = true; // any given grid is considered valid until proven otherwise
	int numEmptyCells = 0; // stores the number of empty cells to measure a puzzle's difficulty in GeneratingAlgorithm
	int[][] listEmptyCells; // list of all coordinates without a number
	HashMap<int[], HashSet<Integer>> backtrackingMap = new HashMap<int[], HashSet<Integer>>(); // maps a set of coordinates {row,col} to an array of previously tried candidates
	
	/**
	* Display the current state of the board. Mostly used for testing. 
	*
	* @param None.
	* @return No return value.
	*/
	
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
	}
	
	/**
	* Determines the number of empty cells and stores the coordinates of the empty ones.
	*
	* @return No return value.
	*/
	
	void storeEmptyCells() {
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				if(this.grid[r][c] == 0) {
					numEmptyCells++;
			}
				else {
				}
			}
		}

		/** Note: for computational efficiency's sake, it is crucial to fill the cells in order (L -> R and T -> B), which
		 * is why they must be stored in order in an int[][] (as opposed to storing them in a HashSet and then converting toArray). */
		
		listEmptyCells = new int[this.numEmptyCells][2];
		int indx = 0;
		
		for (int r=0; r<=8; r++) {
			for (int c=0; c<=8; c++) {
				if(this.grid[r][c] == 0) {
					int[] key = {r,c};
					this.listEmptyCells[indx] = key;
					indx++;
					
					/*
					 * Map each "empty coordinate" to an empty array that will contain previous attempts 
					 * to fill its corresponding cell.
					 */
					
					this.backtrackingMap.put(key, new HashSet<Integer>());
					
				}
			}
		}
	}
	
	/**
	* Stores all the numbers used in the given row. 
	* It also determines whether a grid is valid (i.e., has no repeated numbers in that row).
	*
	* @param r  Row of the grid to be scanned.
	* @return A set containing all used numbers in this row.
	*/
	
	HashSet<Integer> getRowConstraints(int r) {
		
		HashSet<Integer> rowConstraints = new HashSet<Integer>();
		
		for (int c=0; c<=8; c++) {
			
			if (rowConstraints.contains(this.grid[r][c])) {
				this.validGrid = false;
			}
			
			if(this.grid[r][c] != 0) {
				rowConstraints.add(this.grid[r][c]);
			}
		}
		return rowConstraints;
	}
	
	/**
	* Stores all the numbers used in the given column. 
	* It also determines whether a grid is valid (i.e., has no repeated numbers in that column).
	*
	* @param c  Column of the grid to be scanned.
	* @return A set containing all used numbers in this column.
	*/
	
	HashSet<Integer> getColConstraints(int c) {
		
		HashSet<Integer> colConstraints = new HashSet<Integer>();
		
		for (int r=0; r<=8; r++) {
			
			if (colConstraints.contains(this.grid[r][c])) {
				this.validGrid = false;
			}
			
			if(this.grid[r][c] != 0) {
				colConstraints.add(this.grid[r][c]);
			}
		}
		return colConstraints;
	}
	
	/**
	* Stores all the numbers used in the block of the given coordinates (numbered L -> R, T -> B, starting from 0). 
	* It also determines whether a grid is valid (i.e., has no repeated numbers in that block).
	*
	* @param r  Row coordinate in the grid.
	* @param c  Column coordinate in the grid.
	* @return A set containing all used numbers in this block.
	*/
	
	HashSet<Integer> getBlockConstraints(int r, int c) {
		
		HashSet<Integer> blockConstraints = new HashSet<Integer>();
		
		final int[][] ROW_COL_GROUPS = {
											{0,1,2},
											{3,4,5},
											{6,7,8}
										};				// Numbering of the blocks
		
		int[] colsOfBlock = ROW_COL_GROUPS[(c-(c%3))/3]; // stores all columns of the block
		int[] rowsOfBlock = ROW_COL_GROUPS[(r-(r%3))/3]; // stores all rows of the block
		
		for(Integer col : colsOfBlock) {
			for (Integer row : rowsOfBlock) {
				
				if (blockConstraints.contains(this.grid[r][c])) {
					this.validGrid = false;
				}
				
				if(this.grid[row][col] != 0) {
					blockConstraints.add(this.grid[row][col]);
				}
			}
		}
		return blockConstraints;
		
	}
	
	/**
	* Given a set of row-col coordinates, it returns what numbers can be used to fill the corresponding cell.
	* It also determines whether a grid is valid by performing individual checks in the previous three functions.
	*
	* @param r  Row coordinate in the grid.
	* @param c  Column coordinate in the grid.
	* @return An array of possible numbers to fill the cell. If the cell has already been filled, an empty set is returned.
	*/
	
	int[] getValidNumbers(int r, int c) {
		
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
        int[] validNums = new int[validNumbers.size()];
        int indx = 0;
        for (Integer i : validNumbers) {
        	  validNums[indx++] = i;
        	}
		return validNums;
		
	}
	
}
