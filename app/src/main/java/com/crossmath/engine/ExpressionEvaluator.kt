package com.crossmath.engine

import com.crossmath.model.Operator

/**
 * Evaluates expressions left-to-right (no PEMDAS / operator precedence).
 *
 * Example: evaluate(listOf(3, 4, 2), listOf(PLUS, MULTIPLY))
 *   = (3 + 4) × 2 = 14
 */
object ExpressionEvaluator {

    fun evaluate(numbers: List<Int>, operators: List<Operator>): Int {
        require(numbers.size == operators.size + 1) {
            "Need one more number than operators: ${numbers.size} numbers, ${operators.size} ops"
        }
        var result = numbers[0]
        for (i in operators.indices) {
            result = apply(result, operators[i], numbers[i + 1])
        }
        return result
    }

    private fun apply(a: Int, op: Operator, b: Int): Int {
        return when (op) {
            Operator.PLUS -> a + b
            Operator.MINUS -> a - b
            Operator.MULTIPLY -> a * b
            Operator.DIVIDE -> {
                require(b != 0) { "Division by zero" }
                require(a % b == 0) { "Division must be exact: $a / $b" }
                a / b
            }
        }
    }
}
