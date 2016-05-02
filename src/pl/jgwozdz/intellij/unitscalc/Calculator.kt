package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal


//val defaultExpressionFormatter = ExpressionFormatter();
//fun Expression.toString():String {
//    return defaultExpressionFormatter.format(this)
//}
//

enum class Units { MM, CM, IN, PX, PT }

interface Expression

data class Addition(val term1: Expression, val term2: Term) : Expression
data class Subtraction(val term1: Expression, val term2: Term) : Expression
interface Term : Expression

//data class Multiplication(val factor: BigDecimal, val expression: Factor) : Term // future use
//data class Division(val expression: Factor, val divisor: BigDecimal) : Term // future use
interface Factor : Term

data class Measurement(val amount: BigDecimal, val units: Units) : Factor
data class Brackets(val expression: Expression) : Factor


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