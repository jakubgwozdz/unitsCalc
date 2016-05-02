package pl.jgwozdz.intellij.unitscalc

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.text.ParseException

/**

 */
class MeasurementFormatterTest {
    val formatter = ExpressionFormatter()

    @Test fun noLeadingZero() {
        val measurement = Measurement(BigDecimal(".4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("0.457in"))
    }

    @Test fun largeValue() {
        val measurement = Measurement(BigDecimal("123.4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("123.457in"))
    }

    @Test fun negative() {
        val measurement = Measurement(BigDecimal("-.4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("-0.457in"))
    }

    @Test fun roundNumber() {
        val measurement = Measurement(BigDecimal("1234"), Units.PX)
        assertThat(formatter.format(measurement), equalTo("1234px"))
    }
}



class ExpressionFormatterTest {

    val formatter = ExpressionFormatter()

    @Test fun additionWithSecondNegative() {
        val addition = Addition(Measurement(BigDecimal("123.4567"), Units.IN), Measurement(BigDecimal("-.4567"), Units.IN))
        assertThat(formatter.format(addition), equalTo("123.457in + (-0.457in)"))
    }
    @Test fun additionWithBothNegative() {
        val addition = Addition(Measurement(BigDecimal("-123.4567"), Units.IN), Measurement(BigDecimal("-.4567"), Units.IN))
        assertThat(formatter.format(addition), equalTo("-123.457in + (-0.457in)"))
    }
}

class TokenizeProcessTest {
    val parser = ExpressionParser();

    @Test(expected = ParseException::class) fun failOnEmpty() {
        val tokens = parser.tokenize("")
        assertThat(tokens.size, equalTo(0))
    }

    @Test fun simpleNumber() {
        val tokens = parser.tokenize("123")
        assertThat(tokens.size, equalTo(1))
        assertThat((tokens[0] as ExpressionParser.NumberToken).value.toString(), equalTo("123"))
    } 

    @Test fun simpleMeasurement() {
        val tokens = parser.tokenize("-123.4567in")
        assertThat(tokens.size, equalTo(2))
        assertThat((tokens[0] as ExpressionParser.NumberToken).value, equalTo(BigDecimal("-123.4567")))
        assertThat((tokens[1] as ExpressionParser.UnitsToken).value, equalTo(Units.IN))
    } 

    @Test fun largeFlatExpression() {
        val tokens = parser.tokenize("1cm + 2cm - 3cm + 4cm")
        assertThat(tokens.size, equalTo(11))
        val i = tokens.iterator()
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("1")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Plus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("2")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Minus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("3")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Plus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("4")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
    } 

    @Test fun complicatedExpression() {
        val tokens = parser.tokenize("  -123.4567mm+ .1cm+ -.1cm - (( ( 4cm --1cm)) -(0.4567mm ) )  ")
//        assertThat(tokens.size, equalTo(2)) 
        val i = tokens.iterator()
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("-123.4567")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.MM))
        assertThat(i.next() as ExpressionParser.Plus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("0.1")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Plus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("-0.1")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Minus, anything())
        assertThat(i.next() as ExpressionParser.OpenBracket, anything())
        assertThat(i.next() as ExpressionParser.OpenBracket, anything())
        assertThat(i.next() as ExpressionParser.OpenBracket, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("4")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.Minus, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("-1")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.CM))
        assertThat(i.next() as ExpressionParser.CloseBracket, anything())
        assertThat(i.next() as ExpressionParser.CloseBracket, anything())
        assertThat(i.next() as ExpressionParser.Minus, anything())
        assertThat(i.next() as ExpressionParser.OpenBracket, anything())
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("0.4567")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.MM))
        assertThat(i.next() as ExpressionParser.CloseBracket, anything())
        assertThat(i.next() as ExpressionParser.CloseBracket, anything())
        
        assertFalse(i.hasNext())
    } 
}

class ParseProcessTest {
    val parser = ExpressionParser();

    @Test(expected = Exception::class) fun failOnNoUnits() {
        parser.parse("123")
    } 

    @Test fun simpleMeasurement() {
        val expression = parser.parse("-123.4567in")
        assertThat((expression as Measurement).amount, equalTo(BigDecimal("-123.4567")))
        assertThat(expression.units, equalTo(Units.IN))
    } 

    @Test fun largeFlatExpression() {
        val expression = parser.parse("1cm+-2cm-3cm+4cm")
        val addition3 = expression as Addition
        val subtraction2 = addition3.term1 as Subtraction
        val addition1 = subtraction2.term1 as Addition
        val v1 = addition1.term1 as Measurement
        val v2 = addition1.term2 as Measurement
        val v3 = subtraction2.term2 as Measurement
        val v4 = addition3.term2 as Measurement
        assertThat(v1.amount, equalTo(BigDecimal("1")))
        assertThat(v2.amount, equalTo(BigDecimal("-2")))
        assertThat(v3.amount, equalTo(BigDecimal("3")))
        assertThat(v4.amount, equalTo(BigDecimal("4")))
    } 

    @Test fun complicatedExpression() {
        val expression = parser.parse("  -123.457mm+ .1cm+ -.1cm - (( ( 4cm --1cm)) -(0.457mm ) )  ")
        val formatted = ExpressionFormatter().format(expression)
        assertThat(formatted, equalTo("-123.457mm + 0.1cm + (-0.1cm) - ((4cm - (-1cm)) - 0.457mm)"))
        val expressionRecheck = parser.parse(formatted)
        val formattedRecheck = ExpressionFormatter().format(expression)
        assertThat(formattedRecheck, equalTo("-123.457mm + 0.1cm + (-0.1cm) - ((4cm - (-1cm)) - 0.457mm)"))
        assertEquals(expression, expressionRecheck)
    } 
}

class CalclulatorTest {
    val calculator = Calculator(ExpressionParser(), ExpressionFormatter())

    @Test fun simpleMeasurement() {
        val expression = calculator.analyze("-123.4567in")
        val result = calculator.calulate(expression, "in")
        assertThat(result, equalTo("-123.457in"))
    } 

    @Test fun largeFlatExpression() {
        val expression = calculator.analyze("1cm+-2cm-3cm+3.5cm")
        val result = calculator.calulate(expression, "cm")
        assertThat(result, equalTo("-0.5cm"))
    } 

    @Test fun largeFlatExpressionCmToMm() {
        val expression = calculator.analyze("1cm+-2cm-3cm+3.5cm")
        val result = calculator.calulate(expression, "mm")
        assertThat(result, equalTo("-5mm"))
    } 

    @Test fun mixedExpression() {
        val expression = calculator.analyze("1in+1cm")
        val result = calculator.calulate(expression, "mm")
        assertThat(result, equalTo("35.4mm"))
    } 

    @Test fun complicatedExpression() {
        val expression = calculator.analyze("  -123.457mm+ .1cm+ -.1cm - (( ( 4cm --1cm)) -(0.457mm ) )  ")
        val result = calculator.calulate(expression, "mm")
        assertThat(result, equalTo("-173mm"))

    } 

    @Test fun complicatedExpressionToIn() {
        val expression = calculator.analyze("  -123.457mm+ .1cm+ -.1cm - (( ( 4cm --1cm)) -(0.457mm ) )  ")
        val result = calculator.calulate(expression, "in")
        assertThat(result, equalTo("-6.811in"))
    } 
}