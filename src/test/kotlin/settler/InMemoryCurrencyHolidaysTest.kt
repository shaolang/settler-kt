/* Copyright 2020 Shaolang Ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package settler

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import settler.generators.genCurrency
import settler.generators.genHolidays
import settler.generators.let
import java.time.LocalDate

class InMemoryCurrencyHolidaysTest : StringSpec({
    "isHoliday returns true when holidays set include the date" {
        val holidays = InMemoryCurrencyHolidays()

        forAll(
            genCurrency,
            let(
                genHolidays(LocalDate.now(), range = 1..10),
                { ds -> Arb.element(ds) }
            )
        ) { ccy, (dateSet: Set<LocalDate>, selectedDate: LocalDate) ->
            holidays.setHolidays(ccy, dateSet)

            holidays.isHoliday(ccy, selectedDate)
        }
    }
})
