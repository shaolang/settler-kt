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
import io.kotest.property.Arb
import io.kotest.property.forAll
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY

class ValueDateCalculatorTest : StringSpec({
    "spot does not value on weekends" {
        val calc = ValueDateCalculator()

        forAll(genTradeDate, genCurrencyPair) { tradeDate, pair ->
            calc.spotFor(tradeDate, pair).dayOfWeek !in setOf(SATURDAY, SUNDAY)
        }
    }
})

// generators

private val genTradeDate = Arb.localDate(minYear = 2020)

private val genCurrencyPair = Arb.string(minSize = 6, maxSize = 6).map { s ->
    s.toUpperCase()
}
