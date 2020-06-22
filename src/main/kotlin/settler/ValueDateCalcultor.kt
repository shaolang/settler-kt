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
    fun spotFor(tradeDate: LocalDate, _pair: String): LocalDate {
        return nextBizDate(tradeDate)
    }

    private tailrec fun nextBizDate(date: LocalDate): LocalDate {
        return if (date.dayOfWeek in setOf(SATURDAY, SUNDAY)) {
            nextBizDate(date.plusDays(1L))
        } else {
            date
        }
    }
}
