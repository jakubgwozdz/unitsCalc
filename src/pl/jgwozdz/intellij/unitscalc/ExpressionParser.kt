package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal

class ParseException(data:String, pos: Int, val detailedMessage : String) : java.text.ParseException(data, pos) {
    override fun toString(): String{
        val data = message?.substring(0, errorOffset) + "[]" + message?.substring(errorOffset)
        return "ParseException: $detailedMessage at pos $errorOffset: `$data`"
    }
}

class ExpressionParser {

    fun parse(data: String): Expression {
        val tokens: MutableList<Token> = tokenize(data)
        val expression: Expression = ParserProcess(tokens).process()
        return expression
    }

    fun tokenize(data: String): MutableList<Token> {
        val tokens: MutableList<Token> = TokenizeProcess(data).process()
        return tokens
    }

    interface Token
    class OpenBracket : Token
    class CloseBracket : Token
    class Plus : Token
    class Minus : Token
    data class NumberToken(val value: BigDecimal) : Token
    data class UnitsToken(val value: Units) : Token

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
                else -> throw ParseException(data, data.length - 1, "Unexpected end of data")
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

    class ParserProcess(val tokens: List<Token>) {
        enum class ParserState {START, AFTER_MEASUREMENT, AFTER_PLUS, AFTER_MINUS }

        fun process(): Expression {
            val iterator = tokens.listIterator()
            var result: Expression = combineExpression(iterator)
            if (iterator.hasNext()) {
                throw IllegalArgumentException("too many tokens, bracket mismatch maybe")   
            }
            return result
        }

        private fun combineExpression(iterator: ListIterator<Token>): Expression {
            var parsed: Expression? = null
            var state = ParserState.START
            while (iterator.hasNext()) {
                val token = iterator.next();
                when (token) {
                    is NumberToken -> when {
                        state == ParserState.START -> {
                            val expr = combineMeasurement(iterator, token.value)
                            parsed = expr
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        state == ParserState.AFTER_PLUS && parsed != null -> {
                            val expr = combineMeasurement(iterator, token.value)
                            parsed = Addition(parsed, expr)
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        state == ParserState.AFTER_MINUS && parsed != null -> {
                            val expr = combineMeasurement(iterator, token.value)
                            parsed = Subtraction(parsed, expr)
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        else -> throw IllegalArgumentException("$token in wrong place")
                    }
                    is OpenBracket -> when {
                        state == ParserState.START -> {
                            val expr = combineBrackets(iterator)
                            parsed = expr
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        state == ParserState.AFTER_PLUS && parsed != null -> {
                            val expr = combineBrackets(iterator)
                            parsed = Addition(parsed, expr)
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        state == ParserState.AFTER_MINUS && parsed != null -> {
                            val expr = combineBrackets(iterator)
                            parsed = Subtraction(parsed, expr)
                            state = ParserState.AFTER_MEASUREMENT
                        }
                        else -> throw IllegalArgumentException("$token in wrong place")
                    }
                    is Plus -> when {
                        state == ParserState.AFTER_MEASUREMENT && parsed != null -> state = ParserState.AFTER_PLUS
                        else -> throw IllegalArgumentException("$token in wrong place")
                    }
                    is Minus -> when {
                        state == ParserState.AFTER_MEASUREMENT && parsed != null -> state = ParserState.AFTER_MINUS
                        else -> throw IllegalArgumentException("$token in wrong place")
                    }
                    is CloseBracket -> when {state == ParserState.AFTER_MEASUREMENT && parsed != null -> {
                        iterator.previous()
                        return parsed
                    }
                        else -> throw IllegalArgumentException("$token in wrong place")
                    }
                    else -> throw IllegalArgumentException("$token in wrong place")
                }
            }
            if (parsed == null) throw IllegalArgumentException("no tokens")
            return parsed
        }

        private fun combineBrackets(iterator: ListIterator<Token>): Factor {
            val brackets = Brackets(combineExpression(iterator))
            if (!iterator.hasNext()) throw IllegalArgumentException("no closing bracket")
            val next = iterator.next()
            if (next !is CloseBracket) throw IllegalArgumentException("$next instead of closing bracket")

            val expr = if (brackets.expression is Measurement) brackets.expression else if (brackets.expression is Brackets) brackets.expression else brackets
            return expr
        }

        private fun combineMeasurement(iterator: ListIterator<Token>, amount: BigDecimal): Measurement {
            val token2 = iterator.next() as UnitsToken
            val expr = Measurement(amount, token2.value)
            return expr
        }

    }

}