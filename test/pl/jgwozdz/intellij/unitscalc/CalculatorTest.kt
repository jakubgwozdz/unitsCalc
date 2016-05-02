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

    @Test fun complicatedExpression() {
        val tokens = parser.tokenize("  -123.4567mm+ -.1cm - (( ( 4cm --1cm)) -0.4567mm )  ")
//        assertThat(tokens.size, equalTo(2)) 
        val i = tokens.iterator()
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("-123.4567")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.MM))
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
        assertThat((i.next() as ExpressionParser.NumberToken).value, equalTo(BigDecimal("0.4567")))
        assertThat((i.next() as ExpressionParser.UnitsToken).value, equalTo(Units.MM))
        assertThat(i.next() as ExpressionParser.CloseBracket, anything())
        
        assertFalse(i.hasNext())
    } 
}