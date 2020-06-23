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
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import io.kotest.property.forAll
import settler.generators.genCurrency
import settler.generators.genCurrencyPair
import settler.generators.genHolidays
import settler.generators.genTradeDate
import settler.generators.let
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class ValueDateCalculatorTest : StringSpec({
    "spot does not value on weekends" {
        forAll(genTradeDate, genCurrencyPair) { tradeDate, pair ->
            val calc = ValueDateCalculator()

            calc.spotFor(tradeDate, pair).dayOfWeek !in setOf(SATURDAY, SUNDAY)
        }
    }

    "spot is normally T+2 from trade (assuming no holidays)" {
        checkAll(genTradeDate, genCurrencyPair) { tradeDate, pair ->
            val calc = ValueDateCalculator()
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
        checkAll(
            genCurrencyPair,
            let(genTradeDate, ::genHolidays, ::genHolidays)
        ) { pair, (tradeDate, baseHolidays, termHolidays) ->
            val calc = ValueDateCalculator()
            val baseCcy = pair.substring(0, 3)
            val termCcy = pair.substring(3, 6)

            calc.setHolidays(baseCcy, baseHolidays)
            calc.setHolidays(termCcy, termHolidays)

            val allHolidays = baseHolidays.toSet().union(termHolidays)

            allHolidays shouldNot contain(calc.spotFor(tradeDate, pair))
        }
    }

    "USD holidays on T+1 is considered as good biz day for pairs with USD" {
        checkAll(genTradeDate(false), genCurrency) { tradeDate, ccy ->
            val calc = ValueDateCalculator()
            val usdHolidays = setOf(tradeDate.plusDays(1L))

            calc.setHolidays("USD", usdHolidays)

            val valueDate = calc.spotFor(tradeDate, "USD$ccy")
            val days = tradeDate.datesUntil(valueDate.plusDays(1L))
                .filter({ d: LocalDate ->
                    d.dayOfWeek !in setOf(SATURDAY, SUNDAY)
                })
                .count() - 1

            days shouldBe 2L
        }
    }

    "spot never falls on USD holidays even for crosses" {
        checkAll(
            genCurrencyPair,
            let(genTradeDate, ::genHolidays, ::genHolidays)
        ) { pair, (tradeDate, usdHolidays, baseHolidays) ->
            val calc = ValueDateCalculator()
            val baseCcy = pair.substring(0, 3)

            calc.setHolidays("USD", usdHolidays)
            calc.setHolidays(baseCcy, baseHolidays)

            val allHolidays = baseHolidays.toSet().union(usdHolidays)

            allHolidays shouldNot contain(calc.spotFor(tradeDate, pair))
        }
    }
})
