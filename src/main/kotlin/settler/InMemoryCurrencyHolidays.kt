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

class InMemoryCurrencyHolidays {
    private val holidays: MutableMap<String, Set<LocalDate>> = mutableMapOf()

    fun setHolidays(ccy: String, holidays: Set<LocalDate>) {
        this.holidays.put(ccy, holidays)
    }

    fun isHoliday(ccy: String, date: LocalDate): Boolean {
        return holidays.getOrDefault(ccy, NO_HOLIDAYS).contains(date)
    }

    private companion object {
        @JvmField val NO_HOLIDAYS: Set<LocalDate> = emptySet()
    }
}
