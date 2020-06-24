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

/**
 * A calculator for FX value dates with sensible defaults.
 *
 * Unless necessary, `ValueDateCalculator` requires very little configuration:
 * it assumes Saturdays and Sundays as the standard workends (codified by
 * [settler.WorkWeek.STANDARD_WORKWEEK]) and
 * [T+2]( https://en.wikipedia.org/wiki/T%2B2) as the standard spot
 * settlement day. Use the method [setWorkWeek] to support currencies with
 * different work weeks; use [setSpotLag] to support pairs with different
 * spot settlements.
 *
 * All currencies and currency pairs should be in lower- or upper-case
 * consistently, i.e., `ValueDateCalculator` does not defensively normalize
 * them. Currency pairs should also be in the format of "xxxyyy" where "xxx"
 * is the base currency and "yyy" is the term currency, e.g., "USDSGD",
 * "USDJPY" are acceptable, but not "USD/CAD" and "AUD-SGD".
 *
 * `ValueDateCalculator` also does not handle automatic trade date rollover.
 * The calling application should track such rollover and pass the
 * trade date to `ValueDateCalculator` for determining the value date.
 * This deliberate omission allows using `ValueDateCalculator` to calculate
 * value dates with historical and future dates.
 */
class ValueDateCalculator(private val ccyHolidays: CurrencyHolidays) {
    /**
     * Sets the spot settlement (spot lag) for the specified currency pair.
     *
     * @param [pair] the currency pair to use the specific spot lag
     * @param [spotLag] the number of business days between the trade date
     * and value date
     * @return the receiving `ValueDateCalculator` instance for chaining setups
     */
    fun setSpotLag(pair: String, spotLag: Long): ValueDateCalculator {
        spotLags.put(pair, spotLag)
        return this
    }

    fun setWorkWeek(ccy: String, workweek: WorkWeek): ValueDateCalculator {
        workweeks.put(ccy, workweek)

        if (ccy == "USD") {
            usdWorkWeek = workweek
            isUsdNonBizDay = nonBizDayPredicate("USD")
        }

        return this
    }

    fun spotFor(pair: String, tradeDate: LocalDate): LocalDate {
        val spotLag = spotLags.getOrDefault(pair, DEFAULT_SPOT_LAG)
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
        const val DEFAULT_SPOT_LAG: Long = 2L
    }
}
