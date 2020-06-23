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
import java.time.LocalDate

class InMemoryCurrencyHolidaysTest : StringSpec({
    lateinit var holidays: InMemoryCurrencyHolidays
    beforeTest {
        holidays = InMemoryCurrencyHolidays()
        holidays.setHolidays("ABC", setOf(LocalDate.of(2020, 1, 2)))
        holidays.setHolidays("DEF", setOf(LocalDate.of(2020, 1, 9)))
    }

    "isHoliday returns true when holidays set include the date" {
        holidays.isHoliday("ABC", LocalDate.of(2020, 1, 2)) shouldBe true
    }

    "isHoliday returns false when holidays set doesn't include the date" {
        holidays.isHoliday("DEF", LocalDate.of(2222, 1, 9)) shouldBe false
    }
})
