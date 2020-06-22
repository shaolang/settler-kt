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

    fun setSpotLag(pair: String, spotLag: Long): ValueDateCalculator {
        spotLags.put(pair.toUpperCase(), spotLag)
        return this
    }

    fun spotFor(tradeDate: LocalDate, pair: String): LocalDate {
        val spotLag = spotLags.getOrElse(pair) { 2L }
        return nextBizDate(tradeDate, spotLag)
    }

    private tailrec fun nextBizDate(date: LocalDate, addDays: Long): LocalDate {
        return when {
            date.dayOfWeek in WEEKENDS -> nextBizDate(date.plusDays(1L), addDays)
            addDays == 0L -> date
            else -> nextBizDate(date.plusDays(1L), addDays - 1)
        }
    }

    private companion object {
        val WEEKENDS = setOf(SATURDAY, SUNDAY)
    }
}
