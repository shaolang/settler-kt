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

package settler.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import kotlin.ranges.IntRange

val genCurrency = Arb.stringPattern("[A-Z0-9]{3}")

val genCurrencyPair = Arb.set(genCurrency, range = 2..2).map { ss ->
    ss.joinTo(StringBuilder(), separator = "").toString()
}

fun genHolidays(date: LocalDate): Arb<Set<LocalDate>> = genHolidays(date, 0..10)
fun genHolidays(date: LocalDate, range: IntRange): Arb<Set<LocalDate>> {
    return Arb.set(Arb.long(min = 1, max = 10), range).map { ns ->
        ns.map { n -> date.plusDays(n) }.toSet()
    }
}

val genTradeDate = Arb.localDate(minYear = 2020)
fun genTradeDate(allowWeekends: Boolean = true): Arb<LocalDate> {
    val gen = Arb.localDate(minYear = 2020)

    return if (allowWeekends) {
        gen
    } else {
        gen.map({ d ->
            d.datesUntil(d.plusDays(7L)).filter { x ->
                x.dayOfWeek !in setOf(SATURDAY, SUNDAY)
            }.findFirst().get()
        })
    }
}

fun <A, B> let(genA: Arb<A>, genBFn: (A) -> Arb<B>): Arb<Pair<A, B>> = arb {
    val iterA = genA.generate(it).iterator()

    generateSequence {
        val a = iterA.next().value
        val b = genBFn(a).generate(it).iterator().next().value

        Pair(a, b)
    }
}

fun <A, B, C> let(
    genA: Arb<A>,
    genBFn: (A) -> Arb<B>,
    genCFn: (A) -> Arb<C>
): Arb<Triple<A, B, C>> = arb {
    val iterA = genA.generate(it).iterator()

    generateSequence {
        val a = iterA.next().value
        val b = genBFn(a).generate(it).iterator().next().value
        val c = genCFn(a).generate(it).iterator().next().value

        Triple(a, b, c)
    }
}
