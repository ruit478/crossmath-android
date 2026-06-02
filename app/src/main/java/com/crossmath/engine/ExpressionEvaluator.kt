package com.crossmath.engine

import com.crossmath.model.Operator

/**
 * Evaluates expressions left-to-right (no PEMDAS / operator precedence).
 *
 * Example: evaluate(listOf(3, 4, 2), listOf(PLUS, MULTIPLY))
 *   = (3 + 4) × 2 = 14
 */
object ExpressionEvaluator {

    /**
     * Evaluate an expression left-to-right.
     * Returns null if the expression is invalid (div by zero, non-exact division).
     */
    fun evaluate(numbers: List<Int>, operators: List<Operator>): Int? {
        if (numbers.size != operators.size + 1) return null
        var result = numbers[0]
        for (i in operators.indices) {
            val next = apply(result, operators[i], numbers[i + 1]) ?: return null
            result = next
        }
        return result
    }

    private fun apply(a: Int, op: Operator, b: Int): Int? {
        return when (op) {
            Operator.PLUS -> a + b
            Operator.MINUS -> a - b
            Operator.MULTIPLY -> a * b
            Operator.DIVIDE -> {
                if (b == 0) return null
                if (a % b != 0) return null
                a / b
            }
        }
    }
}
