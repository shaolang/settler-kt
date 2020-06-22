/* Copyright 2020 Shaolang Ai
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package settler

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.contain
import io.kotest.matchers.longs.exactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.kotest.property.forAll
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class ValueDateCalculatorTest : StringSpec({
    "spot does not value on weekends" {
        val calc = ValueDateCalculator()

        forAll(genTradeDate, genCurrencyPair) { tradeDate, pair ->
            calc.spotFor(tradeDate, pair).dayOfWeek !in setOf(SATURDAY, SUNDAY)
        }
    }

    "spot is normally T+2 from trade (assuming no holidays)" {
        val calc = ValueDateCalculator()

        checkAll(genTradeDate, genCurrencyPair) { tradeDate, pair ->
            val valueDate = calc.spotFor(tradeDate, pair)
            val days = tradeDate.datesUntil(valueDate.plusDays(1L))
                .filter({ d: LocalDate ->
                    d.dayOfWeek !in setOf(SATURDAY, SUNDAY)
                })
                .count() - 1

            days shouldBe exactly(2L)
        }
    }

    "USDCAD is normally T+1 from trade (assuming no holidays)" {
        val calc = ValueDateCalculator().setSpotLag("USDCAD", 1L)
        val tradeDate = LocalDate.of(2020, 6, 1) // Monday
        val valueDate = calc.spotFor(tradeDate, "USDCAD")

        valueDate shouldBe LocalDate.of(2020, 6, 2)
    }

    "spot never falls on holidays" {
        val calc = ValueDateCalculator()

        checkAll(
            genCurrencyPair,
            let(genTradeDate, ::genHolidays, ::genHolidays)
        ) { pair, (tradeDate, baseHolidays, termHolidays) ->
            val baseCcy = pair.substring(0, 3)
            val termCcy = pair.substring(3, 6)

            calc.setHolidays(baseCcy, baseHolidays)
            calc.setHolidays(termCcy, termHolidays)

            val allHolidays = baseHolidays.toSet().union(termHolidays)

            allHolidays shouldNot contain(calc.spotFor(tradeDate, pair))
        }
    }
})

// generators

private val genTradeDate = Arb.localDate(minYear = 2020)

private val genCurrency = Arb.stringPattern("[A-Z0-9]{3}")

private val genCurrencyPair = Arb.set(genCurrency, range = 2..2).map { ss ->
    ss.joinTo(StringBuilder(), separator = "").toString()
}

private fun genHolidays(date: LocalDate): Arb<Set<LocalDate>> {
    return Arb.set(Arb.long(min = 1, max = 10), range = 0..10).map { ns ->
        ns.map { n -> date.plusDays(n) }.toSet()
    }
}

private fun <A, B, C> let(
    genA: Arb<A>,
    genBFn: (A) -> Arb<B>,
    genCFn: (A) -> Arb<C>
): Arb<Triple<A, B, C>> = arb {
    val iterA = genA.generate(it).iterator()

    generateSequence {
        val a = iterA.next().value
        val b = genBFn(a).generate(it).iterator().next().value
        val c = genCFn(a).generate(it).iterator().next().value

        Triple(a, b, c)
    }
}
