package com.crossmath.ui.game

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.crossmath.engine.PuzzleGenerator
import com.crossmath.engine.PuzzleValidator
import com.crossmath.model.Difficulty
import com.crossmath.model.Puzzle

/**
 * Immutable snapshot of all game state — swapped atomically so Compose
 * never sees a stale mix of puzzle vs entries vs selection.
 */
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

/**
 * Manages puzzle state via atomic snapshot swaps.
 * loading=true hides the grid while a new puzzle generates.
 */
class GameViewModel : ViewModel() {

    var state by mutableStateOf(
        GameState(puzzle = PuzzleGenerator.generate(size = 3, difficulty = Difficulty.EASY))
    )
        private set

    /** True while a new puzzle is being generated — grid is hidden. */
    var loading by mutableStateOf(false)
        private set

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

    /**
     * Split into two frames:
     * 1. Set loading=true → grid replaced by spinner
     * 2. Post to Handler — generate puzzle, swap state, hide spinner
     * Never renders the grid during a transition.
     */
    fun newGame(size: Int, difficulty: Difficulty) {
        if (loading) return

        loading = true  // triggers recomposition → spinner visible

        Handler(Looper.getMainLooper()).post {
            val newPuzzle = PuzzleGenerator.generate(size, difficulty)
            state = GameState(puzzle = newPuzzle)
            loading = false
        }
    }
}
