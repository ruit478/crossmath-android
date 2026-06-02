package com.crossmath.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.crossmath.engine.PuzzleGenerator
import com.crossmath.engine.PuzzleValidator
import com.crossmath.model.Difficulty
import com.crossmath.model.Puzzle

data class GameState(
    val puzzle: Puzzle,
    val playerEntries: Map<String, Int> = emptyMap(),
    val selectedCell: Pair<Int, Int>? = null,
    val validationResult: PuzzleValidator.ValidationResult? = null
) {
    val isAllFilled: Boolean
        get() {
            val blankCount = puzzle.numbers.indices.sumOf { r ->
                puzzle.numbers[r].indices.count { c -> !puzzle.given[r][c] }
            }
            return playerEntries.size == blankCount
        }
}

class GameViewModel : ViewModel() {

    var state by mutableStateOf(
        GameState(puzzle = PuzzleGenerator.generate(size = 3, difficulty = Difficulty.EASY))
    )
        private set

    // ── Debounce ───────────────────────────────────────────────
    private var lastSwap = 0L
    private companion object {
        private const val DEBOUNCE_MS = 400L
    }

    // ── Convenience delegates ──

    val puzzle get() = state.puzzle
    val playerEntries get() = state.playerEntries
    val selectedCell get() = state.selectedCell
    val validationResult get() = state.validationResult

    // ── Actions ────────────────────────────────────────────────

    fun selectCell(row: Int, col: Int) {
        val s = state
        if (row !in s.puzzle.numbers.indices) return
        if (col !in s.puzzle.numbers[row].indices) return
        if (s.puzzle.given[row][col]) return
        state = s.copy(
            selectedCell = if (s.selectedCell == row to col) null else row to col,
            validationResult = null
        )
    }

    fun enterNumber(num: Int) {
        val s = state
        val cell = s.selectedCell ?: return
        val (r, c) = cell
        if (r !in s.puzzle.numbers.indices || c !in s.puzzle.numbers[r].indices) return
        if (s.puzzle.given[r][c]) return
        state = s.copy(
            playerEntries = s.playerEntries + ("$r,$c" to num)
        )
    }

    fun erase() {
        val s = state
        val cell = s.selectedCell ?: return
        val (r, c) = cell
        if (r !in s.puzzle.numbers.indices || c !in s.puzzle.numbers[r].indices) return
        if (s.puzzle.given[r][c]) return
        state = s.copy(
            playerEntries = s.playerEntries - "$r,$c",
            validationResult = null
        )
    }

    fun check() {
        val s = state
        if (!s.isAllFilled) return

        try {
            val fullNumbers = s.puzzle.numbers.mapIndexed { r, row ->
                row.mapIndexed { c, _ ->
                    if (s.puzzle.given[r][c]) s.puzzle.numbers[r][c]
                    else s.playerEntries["$r,$c"]
                }
            }
            val testPuzzle = s.puzzle.copy(numbers = fullNumbers)
            state = s.copy(validationResult = PuzzleValidator.validate(testPuzzle))
        } catch (e: Exception) {
            android.util.Log.e("CrossMath", "check failed", e)
        }
    }

    fun newGame(size: Int, difficulty: Difficulty) {
        val now = System.currentTimeMillis()
        if (now - lastSwap < DEBOUNCE_MS) return
        lastSwap = now

        try {
            state = GameState(
                puzzle = PuzzleGenerator.generate(size, difficulty)
            )
        } catch (e: Exception) {
            android.util.Log.e("CrossMath", "newGame crash: $size $difficulty", e)
        }
    }
}
