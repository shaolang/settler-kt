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

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * An object that determines the working week.
 *
 * @constructor Creates a work week with the specified days as weekends.
 */
class WorkWeek(private val weekends: Set<DayOfWeek>) {
    /**
     * Determines whether the given date is a working day.
     *
     * @param [date] the date to determine whether its a working day
     * @return true if the date given is a working day
     */
    fun isWorkingDay(date: LocalDate): Boolean {
        return date.dayOfWeek !in weekends
    }

    companion object {
        /**
         * The default work week that designates Saturdays and Sundays as
         * the weekend.
         */
        @JvmField val STANDARD_WORKWEEK =
            WorkWeek(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
    }
}
