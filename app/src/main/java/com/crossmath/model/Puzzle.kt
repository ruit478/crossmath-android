package com.crossmath.model

/**
 * A Crossmath puzzle.
 *
 * The grid is `size × size` number cells. Operators sit between the numbers:
 *   - rowOperators[r][c] is the operator between numbers[r][c] and numbers[r][c+1]
 *   - colOperators[r][c] is the operator between numbers[r][c] and numbers[r+1][c]
 *
 * Each row and column forms an equation evaluated left-to-right.
 * The target is the expected result.
 *
 * `given[r][c] == true` means the cell starts filled and can't be changed.
 * `given[r][c] == false` means the cell is blank and the player fills it in.
 * When `given[r][c] == false`, numbers[r][c] holds the correct answer.
 */
data class Puzzle(
    val size: Int,
    val numbers: List<List<Int?>>,
    val rowOperators: List<List<Operator>>,
    val colOperators: List<List<Operator>>,
    val rowTargets: List<Int>,
    val colTargets: List<Int>,
    val given: List<List<Boolean>>
)
