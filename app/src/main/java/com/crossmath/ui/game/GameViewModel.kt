package com.crossmath.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.crossmath.engine.PuzzleGenerator
import com.crossmath.engine.PuzzleValidator
import com.crossmath.model.Difficulty
import com.crossmath.model.Puzzle

/**
 * Manages puzzle state: player entries, cell selection, validation.
 */
class GameViewModel : ViewModel() {

    var puzzle by mutableStateOf(PuzzleGenerator.generate(size = 3, difficulty = Difficulty.EASY))
        private set

    /** Player's number entries for blank cells. Key = "row,col", value = 1-9 */
    var playerEntries by mutableStateOf(mutableMapOf<String, Int>())
        private set

    /** Currently selected cell, or null */
    var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
        private set

    /** Null = not yet checked, otherwise the last validation result */
    var validationResult by mutableStateOf<PuzzleValidator.ValidationResult?>(null)
        private set

    /** All blank cells have been filled (not necessarily correctly) */
    val isAllFilled: Boolean
        get() {
            val blankCount = puzzle.numbers.indices.sumOf { r ->
                puzzle.numbers[r].indices.count { c -> !puzzle.given[r][c] }
            }
            return playerEntries.size == blankCount
        }

    // ── Actions ────────────────────────────────────────────────

    fun selectCell(row: Int, col: Int) {
        if (row in puzzle.numbers.indices && col in puzzle.numbers[row].indices && !puzzle.given[row][col]) {
            selectedCell = if (selectedCell == row to col) null else (row to col)
            validationResult = null
        }
    }

    fun enterNumber(num: Int) {
        val cell = selectedCell ?: return
        val (r, c) = cell
        if (puzzle.given[r][c]) return

        playerEntries = playerEntries.toMutableMap().apply {
            put("$r,$c", num)
        }
        // Keep selection so the user can tap another number quickly

        // Auto-check when all filled
        if (isAllFilled) check()
    }

    fun erase() {
        val cell = selectedCell ?: return
        val (r, c) = cell
        if (puzzle.given[r][c]) return

        playerEntries = playerEntries.toMutableMap().apply {
            remove("$r,$c")
        }
        validationResult = null
    }

    fun check() {
        val fullNumbers = puzzle.numbers.mapIndexed { r, row ->
            row.mapIndexed { c, _ ->
                if (puzzle.given[r][c]) puzzle.numbers[r][c]
                else playerEntries["$r,$c"]
            }
        }
        val testPuzzle = puzzle.copy(numbers = fullNumbers)
        validationResult = PuzzleValidator.validate(testPuzzle)
    }

    fun newGame(size: Int = 3, difficulty: Difficulty = Difficulty.EASY) {
        puzzle = PuzzleGenerator.generate(size, difficulty)
        playerEntries = mutableMapOf()
        selectedCell = null
        validationResult = null
    }
}
