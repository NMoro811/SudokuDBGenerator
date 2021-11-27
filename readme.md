# Sudoku Puzzle Generator

This project consists of a Java package that is able to generate hundreds of thousands of distinct Sudoku puzzles per minute and for different levels of difficulty. 

### Features:

* A Backtracking Algorithm to solve all kinds of Sudoku puzzles and check whether they have unique solutions or determine their difficulty level.
* A Las Vegas algorithm that generates fully-solved Sudoku grids (called "terminal patterns") under a given time limit.
* An algorithm to generate puzzles of 5 different levels of difficulty from a terminal pattern and create up to billions of combinations of the same puzzle.
* Every puzzle and its corresponding solution are both stored in a dedicated PostgreSQL database as strings for easy accessibility across all kinds of larger-sized projects. Moreover, each puzzle is assigned an initial random rating out of 5.

# Literature

Main reference: http://zhangroup.aporc.org/images/files/Paper_3485.pdf