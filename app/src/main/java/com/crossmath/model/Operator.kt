package com.crossmath.model

enum class Operator(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("×"),
    DIVIDE("÷");

    companion object {
        fun fromChar(c: Char): Operator? = entries.find { it.symbol.first() == c }
    }
}
