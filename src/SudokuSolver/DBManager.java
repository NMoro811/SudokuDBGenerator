package SudokuSolver;

import java.sql.*;

public class DBManager {
	
	// JDBC and DB URL for PostgreSQL 
	static final String DRIVER = "org.postgresql.Driver";
	static final String POSTGRES_DB_URL = "jdbc:postgresql://localhost:5433/postgres";
	static final String NEW_DB_URL = "jdbc:postgresql://localhost:5433/sudoku";

	 //  Database credentials
	 static final String USER = "postgres";
	 static final String PASS = "password";

	public static void createDB() {
		
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
			String create_db = "CREATE DATABASE SUDOKU";
			stmt.executeUpdate(create_db);
			System.out.println("Database created successfully\n");
			
			// Connect to Sudoku DB now
			connection = DriverManager.getConnection(NEW_DB_URL, USER, PASS);
			
			if(connection != null) {
				System.out.println("Connected to new 'Sudoku' database");
			}
			
			// Create 5 separate tables - one per difficulty level
			stmt = connection.createStatement();
			String create_table;
			for (int diff_level = 1; diff_level <= 5; diff_level++) {
				
				create_table = "CREATE TABLE IF NOT EXISTS level"+diff_level+" (puzzle_id SERIAL PRIMARY KEY, Puzzle varchar(90) NOT NULL, PuzzleSol varchar(90) NOT NULL, Rating varchar(4) NOT NULL);";
				stmt.executeUpdate(create_table);
				System.out.println("Table 'Level"+diff_level+"' created");
			}
			System.out.println();
			connection.close();
			
			// Dropping DB
			// stmt.executeUpdate("DROP DATABASE SUDOKU");
			// System.out.println("Database dropped");
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
}
