package pl.jgwozdz.intellij.unitscalc

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

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
        val t1 = format(value.term1)
        val t2 = format(value.term2)
        return String.format("%s - %s", t1, if (value.term2 is Measurement && t2.startsWith("-")) "($t2)" else t2)
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