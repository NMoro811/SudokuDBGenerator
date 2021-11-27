package SudokuSolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Create and set up a SQL database to store all the generated puzzles.
 * 
 * @author Nicolás Moro
 */

class DatabaseManagement {
	
	// JDBC and DB URL for PostgreSQL - to be modified if a different DB/connection port is used.
	static final String DRIVER = "org.postgresql.Driver";
	private static final String POSTGRES_DB_URL = "jdbc:postgresql://localhost:5433/postgres";
	static final String NEW_DB_URL = "jdbc:postgresql://localhost:5433/sudoku";

	/* Database credentials - to be introduced by the 
	 * user; forgetting to do so yields a SQLException. */
	static final String USER = "";
	static final String PASS = "";
	
	/**
	 * Creates a SQL database with 5 different tables, one per each difficulty level.
	 * Each table has 4 columns:
	 * <p><ul>
	 * <li> puzzle_id: A serial number to tell puzzles apart.
	 * <li> Puzzle: A Sudoku puzzle encoded as a string.
	 * <li> PuzzleSol: The solution of said puzzle, also as a string.
	 * <li> Rating: A randomly generated rating from 4 to 5.
	 * </ul><p>
	 * 
	 * @throws SQLException when connection to the database fails or the database already exists.
	 */

	static void createDB() throws SQLException{
		
		Connection connection = null;
		Statement stmt = null;
		
		try {
			
			System.out.println("Connecting to PostgreSQL Server...\n");
			
			// Register Driver and open a connection
			Class.forName(DRIVER);
			connection = DriverManager.getConnection(POSTGRES_DB_URL, USER, PASS);
			
			if(connection != null) {
				System.out.println("Connection established successfully");
			}
			
			stmt = connection.createStatement();
			
			// Create dedicated Sudoku DB
			System.out.println("Creating 'Sudoku' database...");
			String createStatement = "CREATE DATABASE SUDOKU";
			stmt.executeUpdate(createStatement);
			System.out.println("Database created successfully\n");
			
			// Connect to Sudoku DB now
			connection = DriverManager.getConnection(NEW_DB_URL, USER, PASS);
			
			if(connection != null) {
				System.out.println("Connected to new 'Sudoku' database");
			}
			
			// Create 5 separate tables - one per difficulty level
			stmt = connection.createStatement();
			String newTable;
			for (int diffLevel = 1; diffLevel <= 5; diffLevel++) {
				
				newTable = "CREATE TABLE IF NOT EXISTS level"+diffLevel+" (puzzle_id SERIAL PRIMARY KEY, Puzzle varchar(90) NOT NULL, PuzzleSol varchar(90) NOT NULL, Rating varchar(4) NOT NULL);";
				stmt.executeUpdate(newTable);
				System.out.println("Table 'Level"+diffLevel+"' created");
			}
			System.out.println();
			connection.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
}
