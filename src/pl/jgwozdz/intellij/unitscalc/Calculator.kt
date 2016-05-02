package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal
import java.text.NumberFormat
import java.text.ParseException
import java.util.*


//val defaultExpressionFormatter = ExpressionFormatter();
//fun Expression.toString():String {
//    return defaultExpressionFormatter.format(this)
//}
//

enum class Units { MM, CM, IN, PX, PT }

interface Expression

data class Addition(val term1: Term, val term2: Expression) : Expression
data class Subtraction(val minuend: Term, val substrahend: Expression) : Expression
interface Term : Expression

data class Multiplication(val factor: BigDecimal, val expression: Measurement) : Term // future use
data class Division(val expression: Measurement, val divisor: BigDecimal) : Term // future use
interface Factor : Term

data class Measurement(val amount: BigDecimal, val units: Units) : Factor
data class Brackets(val expression: Expression) : Factor


class ExpressionFormatter {
    val numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH)

    init {
        numberFormat.maximumFractionDigits = 3
        numberFormat.isGroupingUsed = false
    }

    fun format(value: BigDecimal): String {
        return numberFormat.format(value)
    }

    fun format(value: Measurement): String {
        return String.format("%s%s", format(value.amount), value.units.name.toLowerCase())
    }

    fun format(value: Addition): String {
        val t1 = format(value.term1)
        val t2 = format(value.term2)
        return String.format("%s + %s", t1, if (value.term2 is Measurement && t2.startsWith("-")) "($t2)" else t2)
    }

    fun format(value: Subtraction): String {
        val t1 = format(value.minuend)
        val t2 = format(value.substrahend)
        return String.format("%s + %s", t1, if (value.substrahend is Measurement && t2.startsWith("-")) "($t2)" else t2)
    }

    fun format(value: Brackets): String {
        return String.format("(%s)", format(value.expression))
    }

    fun format(value: Expression): String = when (value) {
        is Measurement -> format(value)
        is Addition -> format(value)
        is Subtraction -> format(value)
        is Brackets -> format(value)
        else -> "Unsupported Expression '$value'";
    //        else -> throw UnsupportedOperationException(value.toString());
    }

}

class ExpressionParser {

    fun parse(data: String): Expression {
        val tokens: MutableList<Token> = tokenize(data)
        for (token in tokens) {
        }
        throw UnsupportedOperationException()
    }

    private fun tokenize(data: String): MutableList<Token> {
        val tokens: MutableList<Token> = mutableListOf()
        var state = TokienizerState.START;
        var value = StringBuilder()
        for (c in data.toCharArray()) {
            when (state) {
                TokienizerState.START -> when {
                    Character.isWhitespace(c) -> {
                    }
                    c == '(' -> {
                        tokens.add(OpenBracket()); state = TokienizerState.START
                    }
                    c == '-' -> {
                        value.append(c); state = TokienizerState.IN_NUMBER_JUST_AFTER_MINUS
                    }
                    Character.isDigit(c) -> {
                        value.append(c); state = TokienizerState.IN_NUMBER_BEFORE_DOT
                    }
                    c == '.' -> {
                        value.append(c) ; state = TokienizerState.IN_NUMBER_AFTER_DOT
                    }
                    else -> throw ParseException(data, 0)
                }
                TokienizerState.IN_NUMBER_JUST_AFTER_MINUS -> when {
                    Character.isDigit(c) -> {
                        value.append(c) ; state = TokienizerState.IN_NUMBER_BEFORE_DOT
                    }
                    c == '.' -> {
                        value.append(c) ; state = TokienizerState.IN_NUMBER_AFTER_DOT
                    }
                    else -> throw ParseException(data, 0)
                }
                TokienizerState.IN_NUMBER_BEFORE_DOT -> when {
                    Character.isWhitespace(c) -> { // TODO : unfinished
                        tokens.add(NumberToken(BigDecimal(value.toString()))); value = StringBuilder(); state = TokienizerState.AFTER_NUMBER
                    }
                    Character.isDigit(c) -> {
                        value.append(c) ; state = TokienizerState.IN_NUMBER_BEFORE_DOT
                    }
                    c == '.' -> {
                        value.append(c) ; state = TokienizerState.IN_NUMBER_AFTER_DOT
                    }
                    else -> throw ParseException(data, 0)
                }
            }
        }
        return tokens
    }

    enum class TokienizerState {START, IN_NUMBER_JUST_AFTER_MINUS, IN_NUMBER_BEFORE_DOT, IN_NUMBER_AFTER_DOT, IN_NUMBER_JUST_AFTER_DOT, AFTER_NUMBER, IN_UNITS, AFTER_UNITS }

    interface Token
    class OpenBracket : Token
    class CloseBracket : Token
    class Plus : Token
    class Minus : Token
    data class NumberToken(val value: BigDecimal) : Token
    data class UnitsToken(val value: BigDecimal) : Token

}


class Calculator {
    fun calulate(input: TmpExpression, units: String): String {
        return input.evaluate(units);
    }

    fun analyze(input: String): TmpExpression {
        return TmpExpression(input)
    }

}

class TmpExpression(val input: String) {
    override fun toString(): String {
        return "Expression(input='$input')"
    }

    fun evaluate(units: String): String {
        return toString() + units
    }
}