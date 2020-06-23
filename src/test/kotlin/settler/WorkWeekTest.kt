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
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import settler.generators.let
import java.time.DayOfWeek
import java.time.LocalDate

class WorkWeekTest : StringSpec({
    "isWorkingDay returns false when date is a weekend" {
        checkAll(
            let(
                Arb.set(Exhaustive.enum<DayOfWeek>(), range = 1..6),
                genWeekendDate
            )
        ) { (weekends, date) ->
            val ww = WorkWeek(weekends)

            ww.isWorkingDay(date) shouldBe false
        }
    }

    "isWorkingDay returns true when date is a weekend" {
        checkAll(
            let(
                Arb.set(Exhaustive.enum<DayOfWeek>(), range = 1..6),
                genWeekdayDate
            )
        ) { (weekends, date) ->
            val ww = WorkWeek(weekends)

            ww.isWorkingDay(date) shouldBe true
        }
    }

    "standard workweek treats sat and sun as weekends" {
        val weekends = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        checkAll(genWeekendDate(weekends)) { date ->
            WorkWeek.STANDARD_WORKWEEK.isWorkingDay(date) shouldBe false
        }
    }

    "standard workweek treats mon to fri as working days" {
        val weekends = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        checkAll(genWeekdayDate(weekends)) { date ->
            WorkWeek.STANDARD_WORKWEEK.isWorkingDay(date) shouldBe true
        }
    }
})

// generators, not used anywhere else

val genWeekdayDate = genDate({ date, weekends -> date.dayOfWeek !in weekends })
val genWeekendDate = genDate({ date, weekends -> date.dayOfWeek in weekends })

private typealias WeekendPredicate = (LocalDate, Set<DayOfWeek>) -> Boolean
private typealias GenDateArbFn = (Set<DayOfWeek>) -> Arb<LocalDate>
private fun genDate(predicate: WeekendPredicate): GenDateArbFn {
    return { weekends: Set<DayOfWeek> ->
        Arb.localDate(minYear = 2020)
            .map { date ->
                (1L..8L).map { date.plusDays(it) }
                    .filter { predicate(it, weekends) }
                    .elementAt<LocalDate>(0)
            }
    }
}
