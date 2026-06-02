package com.crossmath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossmath.engine.PuzzleGenerator
import com.crossmath.engine.PuzzleValidator
import com.crossmath.model.Difficulty
import com.crossmath.model.Operator
import com.crossmath.model.Puzzle
import com.crossmath.ui.theme.CrossMathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrossMathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F0E8)
                ) {
                    PuzzleDemo()
                }
            }
        }
    }
}

@Composable
fun PuzzleDemo() {
    val puzzle = remember {
        PuzzleGenerator.generate(size = 3, difficulty = Difficulty.EASY)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CrossMath — Part 2",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF1565C0)
        )

        Text(
            text = "3×3 Easy puzzle",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            color = Color(0xFF757575)
        )

        PuzzleGrid(puzzle = puzzle)

        Text(
            text = "Row targets: ${puzzle.rowTargets.joinToString(", ")}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp),
            color = Color(0xFF1565C0)
        )

        Text(
            text = "Col targets: ${puzzle.colTargets.joinToString(", ")}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF1565C0)
        )

        // Verify correctness
        val result = remember { PuzzleValidator.validate(puzzle) }
        Text(
            text = if (result.isCorrect) "✓ All equations valid" else "✗ Has errors",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
            color = if (result.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
fun PuzzleGrid(puzzle: Puzzle) {
    val size = puzzle.size
    val cellSize = 56.dp

    Column(
        modifier = Modifier
            .background(Color(0xFFE8E0D0), shape = MaterialTheme.shapes.medium)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        for (r in 0 until size) {
            // Row of number cells
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                for (c in 0 until size) {
                    val value = puzzle.numbers[r][c]
                    val isGiven = puzzle.given[r][c]
                    Box(
                        modifier = Modifier
                            .widthIn(min = cellSize)
                            .aspectRatio(1f)
                            .border(1.dp, Color(0xFF333333))
                            .background(
                                if (isGiven) Color(0xFFFFFFFF) else Color(0xFFE3F2FD)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value?.toString() ?: "?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGiven) Color(0xFF212121) else Color(0xFF1565C0)
                        )
                    }

                    // Operator between numbers
                    if (c < size - 1) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = 32.dp)
                                .aspectRatio(0.6f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = puzzle.rowOperators[r][c].symbol,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Row target
                Box(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .aspectRatio(1f)
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "= ${puzzle.rowTargets[r]}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            // Column operators between rows
            if (r < size - 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    for (c in 0 until size) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = cellSize)
                                .aspectRatio(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = puzzle.colOperators[r][c].symbol,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Spacer between numbers (same width as operator cells)
                        if (c < size - 1) {
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 32.dp)
                                    .aspectRatio(0.6f)
                            )
                        }
                    }
                    // Target spacer
                    Box(
                        modifier = Modifier
                            .widthIn(min = 48.dp)
                            .aspectRatio(1f)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }

    // Column targets
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (c in 0 until size) {
            Text(
                text = "= ${puzzle.colTargets[c]}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}
