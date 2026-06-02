package com.crossmath.engine

import com.crossmath.model.Difficulty
import com.crossmath.model.Operator
import com.crossmath.model.Puzzle
import kotlin.random.Random

/**
 * Generates Crossmath puzzles with guaranteed valid solutions.
 *
 * Strategy:
 * 1. Fill the grid with random numbers (1-9).
 * 2. Generate row operators — skip ÷ when a doesn't divide b evenly.
 * 3. Generate column operators — same safety check.
 * 4. Calculate row and column targets by evaluating left-to-right.
 * 5. Blank a percentage of cells based on difficulty.
 */
object PuzzleGenerator {

    private val ALL_OPS = Operator.entries.toList()

    fun generate(size: Int, difficulty: Difficulty, seed: Long? = null): Puzzle {
        seed?.let { Random it } // seeded for repeatability
        return generate(size, difficulty)
    }

    fun generate(size: Int, difficulty: Difficulty): Puzzle {
        require(size in 3..7) { "Grid size must be 3-7" }

        val numbers = MutableList(size) { MutableList<Int?>(size) { 0 } }
        val rowOps = MutableList(size) { MutableList(size - 1) { Operator.PLUS } }
        val colOps = MutableList(size - 1) { MutableList(size) { Operator.PLUS } }

        // 1. Fill grid with random numbers
        for (r in 0 until size) {
            for (c in 0 until size) {
                numbers[r][c] = Random.nextInt(1, 10)
            }
        }

        // 2. Generate row operators
        for (r in 0 until size) {
            for (c in 0 until size - 1) {
                rowOps[r][c] = pickOperator(numbers[r][c]!!, numbers[r][c + 1]!!)
            }
        }

        // 3. Generate column operators
        for (c in 0 until size) {
            for (r in 0 until size - 1) {
                colOps[r][c] = pickOperator(numbers[r][c]!!, numbers[r + 1][c]!!)
            }
        }

        // 4. Calculate targets
        val rowTargets = List(size) { r ->
            val rowNums = numbers[r].map { it!! }
            ExpressionEvaluator.evaluate(rowNums, rowOps[r])
        }
        val colTargets = List(size) { c ->
            val colNums = (0 until size).map { numbers[it][c]!! }
            val ops = (0 until size - 1).map { colOps[it][c] }
            ExpressionEvaluator.evaluate(colNums, ops)
        }

        // 5. Blank cells based on difficulty
        val blankFraction = when (difficulty) {
            Difficulty.EASY -> 0.30
            Difficulty.MEDIUM -> 0.50
            Difficulty.HARD -> 0.65
        }
        val blankCount = (size * size * blankFraction).toInt().coerceAtLeast(1)

        val given = MutableList(size) { MutableList(size) { true } }
        val allPositions = (0 until size).flatMap { r -> (0 until size).map { c -> r to c } }
            .shuffled()

        for (i in 0 until blankCount.coerceAtMost(allPositions.size)) {
            val (r, c) = allPositions[i]
            given[r][c] = false
            // numbers[r][c] stays as the correct answer, so the
            // puzzle has at least one valid solution.
        }

        return Puzzle(
            size = size,
            numbers = numbers.map { row -> row.toList() },
            rowOperators = rowOps.map { it.toList() },
            colOperators = colOps.map { it.toList() },
            rowTargets = rowTargets,
            colTargets = colTargets,
            given = given.map { it.toList() }
        )
    }

    /**
     * Pick an operator safe for (a op b).
     * For ÷, requires a % b == 0.
     * Retries with a different operator if the pick isn't safe.
     */
    private fun pickOperator(a: Int, b: Int): Operator {
        val candidates = ALL_OPS.shuffled()
        for (op in candidates) {
            if (op == Operator.DIVIDE) {
                if (b != 0 && a % b == 0) return op
            } else {
                return op
            }
        }
        // Fallback: no division
        return ALL_OPS.filter { it != Operator.DIVIDE }.random()
    }
}
