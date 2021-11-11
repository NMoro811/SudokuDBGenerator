# This is my Sudoku project. Currently in progress.

Current stage of the project:

I have created a Sudoku puzzle generator based on 5 different levels of difficulty. This program takes as (currently manual) input a fully solved Sudoku grid and uses a modified version of the algorithm in the article below to output a new puzzle corresponding to the introduced level of difficulty. Levels 1 to 4 take ca. 10^1 to 10^2 ms in generating a new puzzle; evil-leveled puzzles can take from 10^3 ms up to the order of 10^5 ms. The created puzzles comply with all the requirements and seem reasonable from the user's perspective.

# Next step

Propagate generated puzzles into millions of combinations. Set up a database to store them.

# Literature

Main reference: http://zhangroup.aporc.org/images/files/Paper_3485.pdf