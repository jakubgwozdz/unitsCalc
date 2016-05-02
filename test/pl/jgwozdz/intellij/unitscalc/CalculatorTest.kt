package pl.jgwozdz.intellij.unitscalc

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.math.BigDecimal

/**

 */
class MeasurementFormatterTest {
    val formatter = ExpressionFormatter()

    @Test fun toStringTest() {
        val measurement = Measurement(BigDecimal(".4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("0.457in"))
    }

    @Test fun toStringLargeValueTest() {
        val measurement = Measurement(BigDecimal("123.4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("123.457in"))
    }

    @Test fun toStringNegativeTest() {
        val measurement = Measurement(BigDecimal("-.4567"), Units.IN)
        assertThat(formatter.format(measurement), equalTo("-0.457in"))
    }

    @Test fun toStringRoundedTest() {
        val measurement = Measurement(BigDecimal("1234"), Units.PX)
        assertThat(formatter.format(measurement), equalTo("1234px"))
    }
}



class ExpressionFormatterTest {

    val formatter = ExpressionFormatter()

    @Test fun additionToStringTest() {
        val addition = Addition(Measurement(BigDecimal("123.4567"), Units.IN), Measurement(BigDecimal("-.4567"), Units.IN))
        assertThat(formatter.format(addition), equalTo("123.457in + (-0.457in)"))
    }
}