# This is my Sudoku project. Currently in progress.

Current stage of the project:

I have created a Sudoku puzzle generator based on 5 different levels of difficulty. This program creates thousands of valid terminal patterns from an empty grid based on a Las Vegas algorithm as described in the literature. A upper bound of 0.1s is set on the creation of each full grid. Then, it uses a modified version of the algorithm in the article below to output a new puzzle corresponding to the introduced level of difficulty. The created puzzles comply with all the requirements and seem reasonable from the user's perspective.

# Next step

Propagate generated puzzles into millions of combinations. Set up a database to store them.

# Literature

Main reference: http://zhangroup.aporc.org/images/files/Paper_3485.pdf