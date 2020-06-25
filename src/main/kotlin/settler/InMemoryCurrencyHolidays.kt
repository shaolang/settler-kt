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

/**
 * An object that can determine whether the given date is a currency holiday
 * of the given currency. InMemoryCurrencyHolidays does not persist any data
 * when the calling application ends.
 *
 * InMemoryCurrencyHolidays does not defensively normalize the given currencies
 * to lower- or upper-case when registering holidays. It also does not
 * defensively copy the given set of holidays when registering.
 *
 * @constructor Creates an InMemoryCurrencyHolidays with no registered holidays.
 */
class InMemoryCurrencyHolidays : CurrencyHolidays {
    private val holidays: MutableMap<String, Set<LocalDate>> = mutableMapOf()

    /**
     * Registers the holidays of the given currency.
     *
     * This method does not defensively normalize the given currency
     * to lower- or upper-case when registering holidays. It also does not
     * defensively copy the given set of holidays when registering.
     *
     * @param [ccy] the currency for registering the holidays with
     * @param [holidays] the set of dates designated as holidays for the given
     * currency
     */
    fun setHolidays(ccy: String, holidays: Set<LocalDate>) {
        this.holidays.put(ccy, holidays)
    }

    /**
     * Determines whether the given date is a holiday for the given currency.
     *
     * This method does not defensively normalize the given currency
     * to lower- or upper-case when checking.
     *
     * @param [ccy] the currency to determine whether the date is a holiday
     * @param [date] the date to determine whether it's a holiday for the
     * given currency
     * @return true when the currency and date combination is a registered
     * holiday in the registry
     */
    override fun isHoliday(ccy: String, date: LocalDate): Boolean {
        return holidays.getOrDefault(ccy, NO_HOLIDAYS).contains(date)
    }

    private companion object {
        @JvmField val NO_HOLIDAYS: Set<LocalDate> = emptySet()
    }
}
