package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*


//val defaultExpressionFormatter = ExpressionFormatter();
//fun Expression.toString():String {
//    return defaultExpressionFormatter.format(this)
//}
//

enum class Units { MM, CM, IN, PX, PT }

interface Expression

data class Addition(val term1: Expression, val term2: Term) : Expression
data class Subtraction(val minuend: Expression, val substrahend: Term) : Expression
interface Term : Expression

//data class Multiplication(val factor: BigDecimal, val expression: Factor) : Term // future use
//data class Division(val expression: Factor, val divisor: BigDecimal) : Term // future use
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

class ParseException(data:String, pos: Int, val detailedMessage : String) : java.text.ParseException(data, pos) {
    override fun toString(): String{
        val data = message?.substring(0, errorOffset) + "[]" + message?.substring(errorOffset)  
        return "ParseException: $detailedMessage at pos $errorOffset: `$data`"
    }
}


class ExpressionParser {

    fun parse(data: String): Expression {
        val tokens: MutableList<Token> = tokenize(data)
        for (token in tokens) {
            
        }
        throw UnsupportedOperationException()
    }

    fun tokenize(data: String): MutableList<Token> {
        val tokens: MutableList<Token> = TokenizeProcess(data).process()
        return tokens
    }

    class TokenizeProcess(val data: String) {
        enum class TokienizerState {START, IN_NUMBER_JUST_AFTER_MINUS, IN_NUMBER_BEFORE_DOT, IN_NUMBER_AFTER_DOT, IN_NUMBER_JUST_AFTER_DOT, AFTER_NUMBER, IN_UNITS, AFTER_UNITS }
        enum class CharClass { SPACE, DIGIT, MINUS, PLUS, POINT, LETTER, OPENBRACKET, CLOSEBRACKET }

        private val tokens: MutableList<Token> = mutableListOf()
        private var state = TokienizerState.START;
        private var buffer = StringBuilder()

        fun process(): MutableList<Token> {

            for ((pos, c) in data.toCharArray().withIndex()) {
                val charClass = when {
                    c.isWhitespace() -> CharClass.SPACE
                    c.isDigit() -> CharClass.DIGIT
                    c.isLetter() -> CharClass.LETTER
                    c == '(' -> CharClass.OPENBRACKET
                    c == ')' -> CharClass.CLOSEBRACKET
                    c == '-' -> CharClass.MINUS
                    c == '+' -> CharClass.PLUS
                    c == '.' -> CharClass.POINT
                    else -> throw ParseException(data, pos, "Expected space, digit, letter, '(', ')', '-', '+' or '.'")
                }
                when (state) {
                    TokienizerState.START -> when (charClass) {
                        CharClass.SPACE -> {
                            state = TokienizerState.START
                        }
                        CharClass.OPENBRACKET -> {
                            tokens.add(OpenBracket())
                            state = TokienizerState.START
                        }
                        CharClass.MINUS -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_JUST_AFTER_MINUS
                        }
                        CharClass.DIGIT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_BEFORE_DOT
                        }
                        CharClass.POINT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_JUST_AFTER_DOT
                        }
                        else -> throw ParseException(data, pos, "Expected space, digit, '(', '-', or '.'")
                    }
                    TokienizerState.IN_NUMBER_JUST_AFTER_MINUS -> when (charClass) {
                        CharClass.DIGIT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_BEFORE_DOT
                        }
                        CharClass.POINT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_JUST_AFTER_DOT
                        }
                        else -> throw ParseException(data, pos, "Expected digit or '.'")
                    }
                    TokienizerState.IN_NUMBER_BEFORE_DOT -> when (charClass) {
                        CharClass.DIGIT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_BEFORE_DOT
                        }
                        CharClass.POINT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_JUST_AFTER_DOT
                        }
                        CharClass.SPACE -> {
                            finishNumberToken()
                            state = TokienizerState.AFTER_NUMBER
                        }
                        CharClass.LETTER -> {
                            finishNumberToken()
                            buffer.append(c)
                            state = TokienizerState.IN_UNITS
                        }
                        else -> throw ParseException(data, pos, "Expected space, digit, letter or '.'")
                    }
                    TokienizerState.IN_NUMBER_JUST_AFTER_DOT -> when (charClass) {
                        CharClass.DIGIT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_AFTER_DOT
                        }
                        else -> throw ParseException(data, pos, "Expected digit")
                    }
                    TokienizerState.IN_NUMBER_AFTER_DOT -> when (charClass) {
                        CharClass.DIGIT -> {
                            buffer.append(c)
                            state = TokienizerState.IN_NUMBER_AFTER_DOT
                        }
                        CharClass.SPACE -> {
                            finishNumberToken()
                            state = TokienizerState.AFTER_NUMBER
                        }
                        CharClass.LETTER -> {
                            finishNumberToken()
                            buffer.append(c)
                            state = TokienizerState.IN_UNITS
                        }
                        else -> throw ParseException(data, pos, "Expected space, digit or letter")
                    }
                    TokienizerState.AFTER_NUMBER -> when (charClass) {
                        CharClass.SPACE -> {
                            state = TokienizerState.AFTER_NUMBER
                        }
                        CharClass.LETTER -> {
                            buffer.append(c)
                            state = TokienizerState.IN_UNITS
                        }
                        else -> throw ParseException(data, pos, "Expected space or letter")
                    }
                    TokienizerState.IN_UNITS -> when (charClass) {
                        CharClass.LETTER -> {
                            buffer.append(c)
                            state = TokienizerState.IN_UNITS
                        }
                        CharClass.SPACE -> {
                            finishUnitsToken()
                            state = TokienizerState.AFTER_UNITS
                        }
                        CharClass.PLUS -> {
                            finishUnitsToken()
                            tokens.add(Plus())
                            state = TokienizerState.START
                        }
                        CharClass.MINUS -> {
                            finishUnitsToken()
                            tokens.add(Minus())
                            state = TokienizerState.START
                        }
                        CharClass.CLOSEBRACKET -> {
                            finishUnitsToken()
                            tokens.add(CloseBracket())
                            state = TokienizerState.AFTER_UNITS
                        }
                        else -> throw ParseException(data, pos, "Expected space, letter, ')', '-' or '+'")
                    }
                    TokienizerState.AFTER_UNITS -> when (charClass) {
                        CharClass.SPACE -> {
                            state = TokienizerState.AFTER_UNITS
                        }
                        CharClass.PLUS -> {
                            tokens.add(Plus())
                            state = TokienizerState.START
                        }
                        CharClass.MINUS -> {
                            tokens.add(Minus())
                            state = TokienizerState.START
                        }
                        CharClass.CLOSEBRACKET -> {
                            tokens.add(CloseBracket())
                            state = TokienizerState.AFTER_UNITS
                        }
                        else -> throw ParseException(data, pos, "Expected space, ')', '-' or '+'")
                    }
                }
            }
            when (state) {
                TokienizerState.IN_UNITS -> finishUnitsToken()
                TokienizerState.IN_NUMBER_BEFORE_DOT, TokienizerState.IN_NUMBER_AFTER_DOT -> finishNumberToken()
                TokienizerState.AFTER_NUMBER, TokienizerState.AFTER_UNITS -> {}
                else -> throw ParseException(data, data.length-1, "Unexpected end of data")
            }
            return tokens
        }

        private fun finishUnitsToken() {
            val units = Units.valueOf(buffer.toString().toUpperCase())
            tokens.add(UnitsToken(units))
            buffer = StringBuilder()
        }

        private fun finishNumberToken() {
            val amount = BigDecimal(buffer.toString())
            tokens.add(NumberToken(amount))
            buffer = StringBuilder()
        }
    }

    interface Token
    class OpenBracket : Token
    class CloseBracket : Token
    class Plus : Token
    class Minus : Token
    data class NumberToken(val value: BigDecimal) : Token
    data class UnitsToken(val value: Units) : Token

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