# CrossMath

A math puzzle game for Android — fill the grid so every row and column evaluates to its target.

```
  2  +   ?  -  5  =  1         Fill the blank cells with
  +     ×     +               numbers 1–9 so that each
  ?  +  4  ÷  2  =  6         row and column, evaluated
  -     +     -               left-to-right, matches
  8  -  3  +  ?  =  7         the target.
  =     =     =
  5    12     0
```

## How it works

Each puzzle is a grid of numbers and operators. Rows and columns form chains — every chain has a **target** on the right or bottom. You fill in the blank cells so the arithmetic checks out.

- **Operators:** `+` `-` `×` `÷`
- **Evaluation:** strictly left-to-right (no operator precedence)
- **Division:** only appears when it divides evenly — puzzles are always solvable with integers

## Difficulty

| Level | Grid | Blanks |
|-------|------|--------|
| Easy | 3×3 | ~30% |
| Medium | 3×3 – 7×7 | ~50% |
| Hard | 3×3 – 7×7 | ~65% |

Larger grids and more blank cells = higher difficulty. The generator picks a random size within range for Medium and Hard.

## Puzzle generation

Puzzles are generated, not hand-crafted:

1. Fill the grid with random numbers 1–9
2. Pick row operators (safe division only)
3. Pick column operators (safe division only)
4. Evaluate rows and columns → targets
5. Blank a fraction of cells based on difficulty

Every generated puzzle has **at least one valid solution** (the original numbers). The validator checks if the player's answer also works.

## Validation

When you tap **Check**, the validator evaluates every row and column with the current entries. Correct cells turn green, incorrect turn red. Targets show green when the full row/column matches.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Min SDK:** 26 (Android 8.0)
- **Build:** Gradle 8.4, AGP 8.2.2

## Build

### Local

```bash
git clone https://github.com/ruit478/crossmath-android.git
cd crossmath-android
./gradlew assembleDebug
```

Requires JDK 17.

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### CI

GitHub Actions builds a debug APK on every push to `main`. Artifacts are available for 7 days.

[![Build Debug APK](https://github.com/ruit478/crossmath-android/actions/workflows/build.yml/badge.svg)](https://github.com/ruit478/crossmath-android/actions/workflows/build.yml)

## Project structure

```
app/src/main/java/com/crossmath/
├── engine/
│   ├── PuzzleGenerator.kt     # Random puzzle generation
│   ├── ExpressionEvaluator.kt # Left-to-right evaluation
│   └── PuzzleValidator.kt     # Answer checking
├── model/
│   ├── Puzzle.kt              # Grid data class
│   ├── Operator.kt            # +, -, ×, ÷
│   └── Difficulty.kt          # Easy / Medium / Hard
├── ui/
│   ├── game/
│   │   ├── GameScreen.kt      # Main game composable
│   │   └── GameViewModel.kt   # Game state management
│   └── theme/                 # Material 3 theming
└── MainActivity.kt            # Single-activity entry point
```
