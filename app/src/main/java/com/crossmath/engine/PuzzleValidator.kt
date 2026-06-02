package com.crossmath.engine

import com.crossmath.model.Puzzle

/**
 * Validates a puzzle solution by checking every row and column equation.
 */
object PuzzleValidator {

    /**
     * Result of validating a puzzle.
     */
    data class ValidationResult(
        val isCorrect: Boolean,
        val rowErrors: Set<Int>,
        val colErrors: Set<Int>
    ) {
        val hasErrors: Boolean get() = rowErrors.isNotEmpty() || colErrors.isNotEmpty()
    }

    /**
     * Check whether the current entry is correct.
     * Returns which rows and columns have mismatches.
     */
    fun validate(puzzle: Puzzle): ValidationResult {
        val numbers = puzzle.numbers
        val rowErrors = mutableSetOf<Int>()
        val colErrors = mutableSetOf<Int>()

        // Check rows
        for (r in puzzle.numbers.indices) {
            val row = puzzle.numbers[r]
            if (row.any { it == null }) {
                rowErrors.add(r)
                continue
            }
            val nums = row.map { it!! }
            val result = ExpressionEvaluator.evaluate(nums, puzzle.rowOperators[r])
            if (result != puzzle.rowTargets[r]) {
                rowErrors.add(r)
            }
        }

        // Check columns
        for (c in 0 until puzzle.size) {
            val col = (0 until puzzle.size).map { puzzle.numbers[it][c] }
            if (col.any { it == null }) {
                colErrors.add(c)
                continue
            }
            val nums = col.map { it!! }
            val ops = (0 until puzzle.size - 1).map { puzzle.colOperators[it][c] }
            val result = ExpressionEvaluator.evaluate(nums, ops)
            if (result != puzzle.colTargets[c]) {
                colErrors.add(c)
            }
        }

        return ValidationResult(
            isCorrect = rowErrors.isEmpty() && colErrors.isEmpty(),
            rowErrors = rowErrors,
            colErrors = colErrors
        )
    }
}
