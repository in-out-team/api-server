package com.inout.fsrs

import com.inout.fsrs.base.addDays
import com.inout.fsrs.base.addMinutes
import com.inout.fsrs.model.Card
import com.inout.fsrs.model.FSRSParameters
import com.inout.fsrs.model.enums.Grade
import com.inout.fsrs.model.enums.State
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class FSRSTest {
    private fun createFSRS(): FSRS {
        val param = FSRSParameters()
        return FSRS(param)
    }

    private fun createCard(): Card {
        return Card.createEmptyCard()
    }

    @Nested
    inner class Repeat {
        @Test
        fun `check first repeat`() {
            // given
            val fsrs = createFSRS()
            val card = createCard()
            val firstDue =
                Calendar.getInstance().apply {
                    set(2024, 0, 1, 0, 0, 0)
                }.time

            // when
            val logs = fsrs.repeat(card, firstDue).logs

            // then
            val againLog = logs[Grade.Again]!!
            assertEquals(State.Learning, againLog.card.state)
            assertEquals(firstDue.addMinutes(1), againLog.card.due)
            assertEquals(0, againLog.card.elapsedDays)
            assertEquals(0, againLog.card.scheduledDays)
            assertEquals(1, againLog.card.reps)
            assertEquals(0, againLog.card.lapses)
            assertEquals(firstDue, againLog.card.lastReview)

            val hardLog = logs[Grade.Hard]!!
            assertEquals(State.Learning, hardLog.card.state)
            assertEquals(firstDue.addMinutes(5), hardLog.card.due)
            assertEquals(0, hardLog.card.elapsedDays)
            assertEquals(0, hardLog.card.scheduledDays)
            assertEquals(1, hardLog.card.reps)
            assertEquals(0, hardLog.card.lapses)
            assertEquals(firstDue, hardLog.card.lastReview)

            val goodLog = logs[Grade.Good]!!
            assertEquals(State.Learning, goodLog.card.state)
            assertEquals(firstDue.addMinutes(10), goodLog.card.due)
            assertEquals(0, goodLog.card.elapsedDays)
            assertEquals(0, goodLog.card.scheduledDays)
            assertEquals(1, goodLog.card.reps)
            assertEquals(0, goodLog.card.lapses)
            assertEquals(firstDue, goodLog.card.lastReview)

            val easyLog = logs[Grade.Easy]!!
            assertEquals(State.Review, easyLog.card.state)
            assertEquals(firstDue.addDays(11), easyLog.card.due)
            assertEquals(0, easyLog.card.elapsedDays)
            assertEquals(11, easyLog.card.scheduledDays)
            assertEquals(1, easyLog.card.reps)
            assertEquals(0, easyLog.card.lapses)
            assertEquals(firstDue, easyLog.card.lastReview)
        }

        @Test
        fun `repeat with again`() {
            // given
            val fsrs = createFSRS()
            val card = createCard()
            val firstDue =
                Calendar.getInstance().apply {
                    set(2024, 0, 1, 0, 0, 0)
                }.time

            // when
            val firstLogs = fsrs.repeat(card, firstDue).logs
            val secondDue = firstLogs[Grade.Again]!!.card.due
            val secondLogs = fsrs.repeat(firstLogs[Grade.Again]!!.card, secondDue).logs

            // then
            val againLog = secondLogs[Grade.Again]!!
            assertEquals(State.Learning, againLog.card.state)
            assertEquals(secondDue.addMinutes(5), againLog.card.due)
            assertEquals(0, againLog.card.elapsedDays)
            assertEquals(0, againLog.card.scheduledDays)
            assertEquals(2, againLog.card.reps)
            assertEquals(0, againLog.card.lapses)
            assertEquals(secondDue, againLog.card.lastReview)

            val hardLog = secondLogs[Grade.Hard]!!
            assertEquals(State.Learning, hardLog.card.state)
            assertEquals(secondDue.addMinutes(10), hardLog.card.due)
            assertEquals(0, hardLog.card.elapsedDays)
            assertEquals(0, hardLog.card.scheduledDays)
            assertEquals(2, hardLog.card.reps)
            assertEquals(0, hardLog.card.lapses)
            assertEquals(secondDue, hardLog.card.lastReview)

            val goodLog = secondLogs[Grade.Good]!!
            assertEquals(State.Review, goodLog.card.state)
            assertEquals(secondDue.addDays(1), goodLog.card.due)
            assertEquals(0, goodLog.card.elapsedDays)
            assertEquals(1, goodLog.card.scheduledDays)
            assertEquals(2, goodLog.card.reps)
            assertEquals(0, goodLog.card.lapses)
            assertEquals(secondDue, goodLog.card.lastReview)

            val easyLog = secondLogs[Grade.Easy]!!
            assertEquals(State.Review, easyLog.card.state)
            assertEquals(secondDue.addDays(2), easyLog.card.due)
            assertEquals(0, easyLog.card.elapsedDays)
            assertEquals(2, easyLog.card.scheduledDays)
            assertEquals(2, easyLog.card.reps)
            assertEquals(0, easyLog.card.lapses)
            assertEquals(secondDue, easyLog.card.lastReview)
        }

        @Test
        fun `repeat with hard`() {
            // given
            val fsrs = createFSRS()
            val card = createCard()
            val firstDue =
                Calendar.getInstance().apply {
                    set(2024, 0, 1, 0, 0, 0)
                }.time

            // when
            val firstLogs = fsrs.repeat(card, firstDue).logs
            val secondDue = firstLogs[Grade.Hard]!!.card.due
            val secondLogs = fsrs.repeat(firstLogs[Grade.Hard]!!.card, secondDue).logs

            // then
            val againLog = secondLogs[Grade.Again]!!
            assertEquals(State.Learning, againLog.card.state)
            assertEquals(secondDue.addMinutes(5), againLog.card.due)
            assertEquals(0, againLog.card.elapsedDays)
            assertEquals(0, againLog.card.scheduledDays)
            assertEquals(2, againLog.card.reps)
            assertEquals(0, againLog.card.lapses)
            assertEquals(secondDue, againLog.card.lastReview)

            val hardLog = secondLogs[Grade.Hard]!!
            assertEquals(State.Learning, hardLog.card.state)
            assertEquals(secondDue.addMinutes(10), hardLog.card.due)
            assertEquals(0, hardLog.card.elapsedDays)
            assertEquals(0, hardLog.card.scheduledDays)
            assertEquals(2, hardLog.card.reps)
            assertEquals(0, hardLog.card.lapses)
            assertEquals(secondDue, hardLog.card.lastReview)

            val goodLog = secondLogs[Grade.Good]!!
            assertEquals(State.Review, goodLog.card.state)
            assertEquals(secondDue.addDays(1), goodLog.card.due)
            assertEquals(0, goodLog.card.elapsedDays)
            assertEquals(1, goodLog.card.scheduledDays)
            assertEquals(2, goodLog.card.reps)
            assertEquals(0, goodLog.card.lapses)
            assertEquals(secondDue, goodLog.card.lastReview)

            val easyLog = secondLogs[Grade.Easy]!!
            assertEquals(State.Review, easyLog.card.state)
            assertEquals(secondDue.addDays(2), easyLog.card.due)
            assertEquals(0, easyLog.card.elapsedDays)
            assertEquals(2, easyLog.card.scheduledDays)
            assertEquals(2, easyLog.card.reps)
            assertEquals(0, easyLog.card.lapses)
            assertEquals(secondDue, easyLog.card.lastReview)
        }

        @Test
        fun `repeat with good`() {
            // given
            val fsrs = createFSRS()
            val card = createCard()
            val firstDue =
                Calendar.getInstance().apply {
                    set(2024, 0, 1, 0, 0, 0)
                }.time

            // when
            val firstLogs = fsrs.repeat(card, firstDue).logs
            val secondDue = firstLogs[Grade.Good]!!.card.due
            val secondLogs = fsrs.repeat(firstLogs[Grade.Good]!!.card, secondDue).logs

            // then
            val againLog = secondLogs[Grade.Again]!!
            assertEquals(State.Learning, againLog.card.state)
            assertEquals(secondDue.addMinutes(5), againLog.card.due)
            assertEquals(0, againLog.card.elapsedDays)
            assertEquals(0, againLog.card.scheduledDays)
            assertEquals(2, againLog.card.reps)
            assertEquals(0, againLog.card.lapses)
            assertEquals(secondDue, againLog.card.lastReview)

            val hardLog = secondLogs[Grade.Hard]!!
            assertEquals(State.Learning, hardLog.card.state)
            assertEquals(secondDue.addMinutes(10), hardLog.card.due)
            assertEquals(0, hardLog.card.elapsedDays)
            assertEquals(0, hardLog.card.scheduledDays)
            assertEquals(2, hardLog.card.reps)
            assertEquals(0, hardLog.card.lapses)
            assertEquals(secondDue, hardLog.card.lastReview)

            val goodLog = secondLogs[Grade.Good]!!
            assertEquals(State.Review, goodLog.card.state)
            assertEquals(secondDue.addDays(4), goodLog.card.due)
            assertEquals(0, goodLog.card.elapsedDays)
            assertEquals(4, goodLog.card.scheduledDays)
            assertEquals(2, goodLog.card.reps)
            assertEquals(0, goodLog.card.lapses)
            assertEquals(secondDue, goodLog.card.lastReview)

            val easyLog = secondLogs[Grade.Easy]!!
            assertEquals(State.Review, easyLog.card.state)
            assertEquals(secondDue.addDays(5), easyLog.card.due)
            assertEquals(0, easyLog.card.elapsedDays)
            assertEquals(5, easyLog.card.scheduledDays)
            assertEquals(2, easyLog.card.reps)
            assertEquals(0, easyLog.card.lapses)
            assertEquals(secondDue, easyLog.card.lastReview)
        }

        @Test
        fun `repeat with easy`() {
            // given
            val fsrs = createFSRS()
            val card = createCard()
            val firstDue =
                Calendar.getInstance().apply {
                    set(2024, 0, 1, 0, 0, 0)
                }.time

            // when
            val firstLogs = fsrs.repeat(card, firstDue).logs
            val secondDue = firstLogs[Grade.Easy]!!.card.due
            val secondLogs = fsrs.repeat(firstLogs[Grade.Easy]!!.card, secondDue).logs

            // then
            val againLog = secondLogs[Grade.Again]!!
            assertEquals(State.Relearning, againLog.card.state)
            assertEquals(secondDue.addMinutes(5), againLog.card.due)
            assertEquals(11, againLog.card.elapsedDays)
            assertEquals(0, againLog.card.scheduledDays)
            assertEquals(2, againLog.card.reps)
            assertEquals(1, againLog.card.lapses)
            assertEquals(secondDue, againLog.card.lastReview)

            val hardLog = secondLogs[Grade.Hard]!!
            assertEquals(State.Review, hardLog.card.state)
            assertEquals(secondDue.addDays(18), hardLog.card.due)
            assertEquals(11, hardLog.card.elapsedDays)
            assertEquals(18, hardLog.card.scheduledDays)
            assertEquals(2, hardLog.card.reps)
            assertEquals(0, hardLog.card.lapses)
            assertEquals(secondDue, hardLog.card.lastReview)

            val goodLog = secondLogs[Grade.Good]!!
            assertEquals(State.Review, goodLog.card.state)
            assertEquals(secondDue.addDays(42), goodLog.card.due)
            assertEquals(11, goodLog.card.elapsedDays)
            assertEquals(42, goodLog.card.scheduledDays)
            assertEquals(2, goodLog.card.reps)
            assertEquals(0, goodLog.card.lapses)
            assertEquals(secondDue, goodLog.card.lastReview)

            val easyLog = secondLogs[Grade.Easy]!!
            assertEquals(State.Review, easyLog.card.state)
            assertEquals(secondDue.addDays(98), easyLog.card.due)
            assertEquals(11, easyLog.card.elapsedDays)
            assertEquals(98, easyLog.card.scheduledDays)
            assertEquals(2, easyLog.card.reps)
            assertEquals(0, easyLog.card.lapses)
            assertEquals(secondDue, easyLog.card.lastReview)
        }
    }
}
