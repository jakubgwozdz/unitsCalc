package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal
import java.math.BigDecimal.*


val defaultExpressionFormatter = ExpressionFormatter();

enum class Units { MM, CM, IN, PX, PT }

interface Expression {
    fun values(): Map<Units, BigDecimal>
};

data class Addition(val term1: Expression, val term2: Term) : Expression {
    override fun values(): Map<Units, BigDecimal> {
        val result = mutableMapOf<Units, BigDecimal>()
        for ((units, amount) in term1.values()) {
            result.put(units, amount)
        }
        for ((units, amount) in term2.values()) {
            result.put(units, result.getOrElse(units, { ZERO }).add(amount))
        }
        return result
    }
}

data class Subtraction(val term1: Expression, val term2: Term) : Expression {
    override fun values(): Map<Units, BigDecimal> {
        val result = mutableMapOf<Units, BigDecimal>()
        for ((units, amount) in term1.values()) {
            result.put(units, amount)
        }
        for ((units, amount) in term2.values()) {
            result.put(units, result.getOrElse(units, { ZERO }).subtract(amount))
        }
        return result
    }
}

interface Term : Expression

//data class Multiplication(val factor: BigDecimal, val expression: Factor) : Term // future use
//data class Division(val expression: Factor, val divisor: BigDecimal) : Term // future use
interface Factor : Term

data class Measurement(val amount: BigDecimal, val units: Units) : Factor {
    override fun values(): Map<Units, BigDecimal> {
        return mapOf(units to amount)
    }
}

data class Brackets(val expression: Expression) : Factor {
    override fun values(): Map<Units, BigDecimal> {
        return expression.values()
    }
}

val ONE_0 = BigDecimal("1.000000")
val IN_MM = BigDecimal("25.40000")
val CM_MM = BigDecimal("10.00000")
val IN_PT = BigDecimal("72.00000")
val IN_PX = BigDecimal("300.0000")

class Calculator(val parser: ExpressionParser, val formatter: ExpressionFormatter) {
    
    val RATIOS : Map<Pair<Units, Units>, BigDecimal> = mapOf(
            (Units.CM to Units.CM) to ONE,
            (Units.MM to Units.CM) to ONE_0 / CM_MM,
            (Units.IN to Units.CM) to IN_MM / CM_MM,
            (Units.PT to Units.CM) to IN_MM / IN_PT / CM_MM,
            (Units.PX to Units.CM) to IN_MM / IN_PX / CM_MM,

            (Units.CM to Units.MM) to CM_MM,
            (Units.MM to Units.MM) to ONE,
            (Units.IN to Units.MM) to IN_MM,
            (Units.PT to Units.MM) to IN_MM / IN_PT,
            (Units.PX to Units.MM) to IN_MM / IN_PX,

            (Units.CM to Units.IN) to CM_MM / IN_MM,
            (Units.MM to Units.IN) to ONE_0 / IN_MM,
            (Units.IN to Units.IN) to ONE,
            (Units.PT to Units.IN) to ONE_0 / IN_PT,
            (Units.PX to Units.IN) to ONE_0 / IN_PX,

            (Units.CM to Units.PT) to IN_PT * CM_MM / IN_MM ,
            (Units.MM to Units.PT) to IN_PT / IN_MM,
            (Units.IN to Units.PT) to IN_PT,
            (Units.PT to Units.PT) to ONE,
            (Units.PX to Units.PT) to IN_PT / IN_PX,

            (Units.CM to Units.PX) to IN_PX * CM_MM / IN_MM,
            (Units.MM to Units.PX) to IN_PX / IN_MM,
            (Units.IN to Units.PX) to IN_PX,
            (Units.PT to Units.PX) to IN_PX / IN_PT,
            (Units.PX to Units.PX) to ONE
    )
    
    fun calulate(input: Expression, units: String): String {
        val values = input.values()
        var total = ZERO;
        val targetUnits = Units.valueOf(units.toUpperCase())
        for ((partialUnits, partialAmount) in values) {
            total += partialAmount * RATIOS.getOrElse(partialUnits to targetUnits, { throw IllegalArgumentException("$partialUnits to $targetUnits") })
        }

        return formatter.format(Measurement(total, targetUnits))
    }
    
    fun prettyPrint(input: Expression): String {
        return formatter.format(input)
    }

    fun analyze(input: String): Expression {
        return parser.parse(input)
    }

}

