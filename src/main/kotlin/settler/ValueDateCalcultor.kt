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

import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class ValueDateCalculator {
    private val spotLags: MutableMap<String, Long> = mutableMapOf()
    private val holidays: MutableMap<String, Set<LocalDate>> = mutableMapOf()

    fun setSpotLag(pair: String, spotLag: Long): ValueDateCalculator {
        spotLags.put(pair.toUpperCase(), spotLag)
        return this
    }

    fun setHolidays(ccy: String, dates: Set<LocalDate>): ValueDateCalculator {
        holidays.put(ccy.toUpperCase(), dates)
        return this
    }

    fun spotFor(tradeDate: LocalDate, pair: String): LocalDate {
        val spotLag = spotLags.getOrDefault(pair, 2L)
        val baseCcy = pair.substring(0, 3)
        val baseHolidays = holidays.getOrDefault(baseCcy, NO_HOLIDAYS)
        val baseVD = nextBizDate(baseHolidays, emptySet(), tradeDate, spotLag)

        val termCcy = pair.substring(3, 6)
        val termHolidays = holidays.getOrDefault(termCcy, NO_HOLIDAYS)
        val termVD = nextBizDate(termHolidays, emptySet(), tradeDate, spotLag)
        val candidate = if (baseVD.isBefore(termVD)) { termVD } else { baseVD }

        return nextBizDate(baseHolidays, termHolidays, candidate, 0L)
    }

    private tailrec fun nextBizDate(
        hols1: Set<LocalDate>,
        hols2: Set<LocalDate>,
        date: LocalDate,
        addDays: Long
    ): LocalDate {
        return when {
            date.dayOfWeek in WEEKENDS ||
                hols1.contains(date) ||
                hols2.contains(date) ->
                nextBizDate(hols1, hols2, date.plusDays(1L), addDays)

            addDays > 0L ->
                nextBizDate(hols1, hols2, date.plusDays(1L), addDays - 1)

            else ->
                date
        }
    }

    private companion object {
        val WEEKENDS = setOf(SATURDAY, SUNDAY)
        val NO_HOLIDAYS: Set<LocalDate> = emptySet()
    }
}
