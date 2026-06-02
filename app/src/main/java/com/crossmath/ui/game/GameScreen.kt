package com.crossmath.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crossmath.engine.PuzzleValidator
import com.crossmath.model.Difficulty
import com.crossmath.model.Puzzle

// ── Top-level Game Screen ──────────────────────────────────────

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val puzzle = viewModel.puzzle
    val playerEntries = viewModel.playerEntries
    val selectedCell = viewModel.selectedCell
    val validationResult = viewModel.validationResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("CrossMath", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

        key(puzzle.size) {
            PuzzleGridView(
                puzzle = puzzle,
                playerEntries = playerEntries,
                selectedCell = selectedCell,
                validationResult = validationResult,
                onCellClick = { r, c -> viewModel.selectCell(r, c) }
            )
        }

        StatusBar(validationResult)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { viewModel.newGame(size = 3, difficulty = Difficulty.EASY) }) {
                Text("3×3 Easy")
            }
            Button(onClick = { viewModel.newGame(size = 4, difficulty = Difficulty.MEDIUM) }) {
                Text("4×4 Medium")
            }
            Button(onClick = { viewModel.check() }) {
                Text("Check")
            }
        }

        NumberPadView(
            onNumber = { viewModel.enterNumber(it) },
            onErase = { viewModel.erase() }
        )
    }
}

@Composable
private fun StatusBar(result: PuzzleValidator.ValidationResult?) {
    when {
        result == null -> Text(
            "Tap a blank cell, then a number",
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
        result.isCorrect -> Text(
            "✓ Correct! All equations valid!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        else -> {
            val rowMsg = if (result.rowErrors.isNotEmpty()) "rows: ${result.rowErrors.joinToString(",")}" else ""
            val colMsg = if (result.colErrors.isNotEmpty()) "cols: ${result.colErrors.joinToString(",")}" else ""
            Text(
                "✗ Errors — $rowMsg $colMsg",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
        }
    }
}

// ── Colors ─────────────────────────────────────────────────────

private val GridBg = Color(0xFFE8E0D0)
private val CellBorder = Color(0xFF333333)
private val CellBorderSelected = Color(0xFFFF6F00)
private val CellBgGiven = Color(0xFFFFFFFF)
private val CellBgBlank = Color(0xFFE3F2FD)
private val CellBgSelected = Color(0xFFFFF3E0)
private val CellBgError = Color(0xFFFFEBEE)
private val CellBgSuccess = Color(0xFFE8F5E9)
private val OpColor = Color(0xFFE65100)
private val TargetColor = Color(0xFF1565C0)
private val TextGiven = Color(0xFF212121)
private val TextPlayerEntry = Color(0xFF1565C0)

@Composable
fun PuzzleGridView(
    puzzle: Puzzle,
    playerEntries: Map<String, Int>,
    selectedCell: Pair<Int, Int>?,
    validationResult: PuzzleValidator.ValidationResult?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val size = puzzle.size

    val cellWidth = when {
        size <= 3 -> 64.dp
        size <= 4 -> 56.dp
        else -> 48.dp
    }
    val opWidth = when {
        size <= 3 -> 36.dp
        size <= 4 -> 32.dp
        else -> 28.dp
    }

    Column(
        modifier = modifier
            .background(GridBg, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        for (r in 0 until size) {
            // ── Number row ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0 until size) {
                    val isGiven = puzzle.given[r][c]
                    val entryKey = "$r,$c"
                    val displayValue = if (isGiven) puzzle.numbers[r][c]
                    else playerEntries[entryKey]

                    val isSelected = selectedCell == (r to c)
                    val hasRowError = validationResult?.rowErrors?.contains(r) == true
                    val hasColError = validationResult?.colErrors?.contains(c) == true
                    val hasError = hasRowError || hasColError

                    val bgColor = when {
                        isSelected -> CellBgSelected
                        hasError -> CellBgError
                        isGiven -> CellBgGiven
                        validationResult?.isCorrect == true -> CellBgSuccess
                        else -> CellBgBlank
                    }
                    val borderColor = if (isSelected) CellBorderSelected else CellBorder
                    val borderWidth = if (isSelected) 2.dp else 1.dp

                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .aspectRatio(1f)
                            .border(borderWidth, borderColor)
                            .background(bgColor)
                            .clickable(enabled = !isGiven) { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayValue?.toString() ?: "",
                            fontSize = if (size <= 3) 28.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGiven) TextGiven else TextPlayerEntry
                        )
                    }

                    // ── Row operator ──
                    if (c < size - 1) {
                        Box(
                            modifier = Modifier
                                .width(opWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = puzzle.rowOperators[r][c].symbol,
                                fontSize = if (size <= 3) 24.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OpColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Row target
                Box(
                    modifier = Modifier
                        .width(cellWidth * 0.8f + 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "=${puzzle.rowTargets[r]}",
                        fontSize = if (size <= 3) 18.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TargetColor
                    )
                }
            }

            // ── Column operators ──
            if (r < size - 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (c in 0 until size) {
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .aspectRatio(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = puzzle.colOperators[r][c].symbol,
                                fontSize = if (size <= 3) 24.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OpColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (c < size - 1) {
                            Spacer(modifier = Modifier.width(opWidth))
                        }
                    }
                    Spacer(modifier = Modifier.width(cellWidth * 0.8f + 4.dp))
                }
            }
        }

        // ── Column targets ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(cellWidth * 0.8f + 4.dp))
            for (c in 0 until size) {
                Text(
                    text = "=${puzzle.colTargets[c]}",
                    fontSize = if (size <= 3) 18.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(cellWidth)
                )
                if (c < size - 1) {
                    Spacer(modifier = Modifier.width(opWidth))
                }
            }
        }
    }
}

// ── Number Pad ────────────────────────────────────────────────

@Composable
fun NumberPadView(
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Three rows of 3
        for (rowStart in 1..9 step 3) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (n in rowStart..rowStart + 2) {
                    NumberButton(n, onNumber)
                }
            }
        }
        // Erase
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onErase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Erase", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NumberButton(num: Int, onClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color(0xFF1565C0))
            .clickable { onClick(num) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$num",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
