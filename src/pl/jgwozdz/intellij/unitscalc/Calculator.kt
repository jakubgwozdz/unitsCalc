package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal
import java.math.BigDecimal.*


/**
 * Supported units
 */
enum class Units { MM, CM, IN, PX, PT }

/**
 * const "1" with scale, for better divisioning. Six digits is more than required for this calculations
 */
val ONE_0 = BigDecimal("1.000000")

/**
 * ratio for millimeters in inch
 */
val IN_MM = BigDecimal("25.40000")

/**
 * ratio for millimeters in centimeter, duh
 */
val CM_MM = BigDecimal("10.00000")

/**
 * hardcoded ratio for points in inch
 */
val IN_PT = BigDecimal("72.00000")

/**
 * hardcoded ratio for pixels in inch (dpi?)
 * TODO: make this dynamic
 */
val IN_PX = BigDecimal("300.0000")


/**
 * base interface for all types of expressions, very lame grammar
 */
interface Expression {
    /**
     * returns the result of expression split by units.
     * e.g. "20mm + 10mm - 2in" becomes (MM, 30), (IN, -2)
     */
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

/**
 * main class - calls parser; sums and converts values with different units; calls formatter
 */
class Calculator(val parser: ExpressionParser, val formatter: ExpressionFormatter) {

    // I hope I haven't made any mistake here. this should really be populated with some smart algorithm
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

    /**
     * sums and converts values with different units
     */
    fun calulate(input: Expression, units: String): Measurement {
        val values = input.values()
        var total = ZERO;
        val targetUnits = Units.valueOf(units.toUpperCase())
        for ((partialUnits, partialAmount) in values) {
            total += partialAmount * RATIOS.getOrElse(partialUnits to targetUnits, { throw IllegalArgumentException("$partialUnits to $targetUnits") })
        }

        return Measurement(total, targetUnits)
    }

    /**
     * calls formatter
     */
    fun prettyPrint(input: Expression): String {
        return formatter.format(input)
    }

    /**
     * calls parser
     */
    fun analyze(input: String): Expression {
        return parser.parse(input)
    }

}

