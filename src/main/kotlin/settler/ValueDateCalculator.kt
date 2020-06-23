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

import java.time.LocalDate

private typealias NonBizDayPred = (LocalDate) -> Boolean

class ValueDateCalculator(private val ccyHolidays: CurrencyHolidays) {
    fun setSpotLag(pair: String, spotLag: Long): ValueDateCalculator {
        spotLags.put(pair, spotLag)
        return this
    }

    fun spotFor(tradeDate: LocalDate, pair: String): LocalDate {
        val spotLag = spotLags.getOrDefault(pair, 2L)
        val baseCcy = pair.substring(0, 3)
        val basePred = nonBizDayPredicate(baseCcy)
        val baseVD = nextBizDate(
            baseCcy, basePred, WORKING, WORKING, tradeDate, spotLag
        )
        val termCcy = pair.substring(3, 6)
        val termPred = nonBizDayPredicate(termCcy)
        val termVD = nextBizDate(
            termCcy, termPred, WORKING, WORKING, tradeDate, spotLag
        )
        val candidate = if (baseVD.isBefore(termVD)) termVD else baseVD

        return nextBizDate(
            baseCcy, basePred, termPred, isUsdNonBizDay, candidate, 0L
        )
    }

    private val spotLags: MutableMap<String, Long> = mutableMapOf()
    private val workweeks: MutableMap<String, WorkWeek> = mutableMapOf()
    private var usdWorkWeek = WorkWeek.STANDARD_WORKWEEK
    private var isUsdNonBizDay = nonBizDayPredicate("USD")

    private tailrec fun nextBizDate(
        ccy: String,
        isCcy1NonBizDay: NonBizDayPred,
        isCcy2NonBizDay: NonBizDayPred,
        isUsdNonBizDay: NonBizDayPred,
        date: LocalDate,
        addDays: Long
    ): LocalDate {
        return when {
            ccy == "USD" && usdWorkWeek.isWorkingDay(date) && addDays == 1L ->
                nextBizDate(
                    ccy, isCcy1NonBizDay, isCcy2NonBizDay,
                    isUsdNonBizDay, date.plusDays(1L), addDays - 1
                )

            isCcy1NonBizDay(date) || isCcy2NonBizDay(date) ->
                nextBizDate(
                    ccy, isCcy1NonBizDay, isCcy2NonBizDay,
                    isUsdNonBizDay, date.plusDays(1L), addDays
                )

            addDays > 0L ->
                nextBizDate(
                    ccy, isCcy1NonBizDay, isCcy2NonBizDay,
                    isUsdNonBizDay, date.plusDays(1L), addDays - 1
                )

            isUsdNonBizDay(date) ->
                nextBizDate(
                    ccy, isCcy1NonBizDay, isCcy2NonBizDay,
                    isUsdNonBizDay, date.plusDays(1L), 0
                )

            else ->
                date
        }
    }

    private fun nonBizDayPredicate(ccy: String): NonBizDayPred {
        val workweek = workweeks.getOrDefault(ccy, WorkWeek.STANDARD_WORKWEEK)

        return { !workweek.isWorkingDay(it) || ccyHolidays.isHoliday(ccy, it) }
    }

    private companion object {
        @JvmField val WORKING: NonBizDayPred = { false }
    }
}
