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

class ValueDateCalculator(private val ccyHolidays: CurrencyHolidays) {
    private val spotLags: MutableMap<String, Long> = mutableMapOf()

    fun setSpotLag(pair: String, spotLag: Long): ValueDateCalculator {
        spotLags.put(pair, spotLag)
        return this
    }

    fun spotFor(tradeDate: LocalDate, pair: String): LocalDate {
        val spotLag = spotLags.getOrDefault(pair, 2L)
        val baseCcy = pair.substring(0, 3)
        val baseVD = nextBizDate(baseCcy, "", false, tradeDate, spotLag)
        val termCcy = pair.substring(3, 6)
        val termVD = nextBizDate(termCcy, "", false, tradeDate, spotLag)
        val candidate = if (baseVD.isBefore(termVD)) termVD else baseVD

        return nextBizDate(baseCcy, termCcy, true, candidate, 0L)
    }

    private tailrec fun nextBizDate(
        ccy1: String,
        ccy2: String,
        checkUSDHoliday: Boolean,
        date: LocalDate,
        addDays: Long
    ): LocalDate {
        return when {
            date.dayOfWeek !in WEEKENDS && ccy1 == "USD" && addDays == 1L ->
                nextBizDate(
                    ccy1, ccy2, checkUSDHoliday, date.plusDays(1L), addDays - 1
                )

            date.dayOfWeek in WEEKENDS ||
                ccyHolidays.isHoliday(ccy1, date) ||
                ccyHolidays.isHoliday(ccy2, date) ->
                nextBizDate(
                    ccy1, ccy2, checkUSDHoliday, date.plusDays(1L), addDays
                )

            addDays > 0L ->
                nextBizDate(
                    ccy1, ccy2, checkUSDHoliday, date.plusDays(1L), addDays - 1
                )

            checkUSDHoliday && ccyHolidays.isHoliday("USD", date) ->
                nextBizDate(ccy1, ccy2, checkUSDHoliday, date.plusDays(1L), 0)

            else ->
                date
        }
    }

    private companion object {
        @JvmField val WEEKENDS = setOf(SATURDAY, SUNDAY)
    }
}
