package com.inout.fsrs.schedule

import com.inout.fsrs.base.plusDays
import com.inout.fsrs.base.plusMinutes
import com.inout.fsrs.model.Card
import com.inout.fsrs.model.enums.Grade
import com.inout.fsrs.model.enums.Rating
import com.inout.fsrs.model.enums.State
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SchedulingCardTest {
    val now: Instant = Instant.now()

    private fun createCard(): Card {
        return Card(
            state = State.New,
            due = now,
            stability = 0.0,
            difficulty = 0.0,
            elapsedDays = 0,
            scheduledDays = 0,
            reps = 0,
            lapses = 0,
            lastReview = null,
        )
    }

    @Nested
    inner class Init {
        @Test
        fun `should correctly set card's elapsed days`() {
            // given
            val newCard = createCard()
            val learningCard = createCard().copy(state = State.Learning, lastReview = now.plusMinutes(-60 * 24))

            // when
            SchedulingCard(newCard, now)
            SchedulingCard(learningCard, now)

            // then
            assertEquals(0, newCard.elapsedDays)
            assertEquals(1, learningCard.elapsedDays)
        }

        @Test
        fun `should correctly set card's last review date`() {
            // given
            val newCard = createCard()
            val learningCard = createCard().copy(state = State.Learning, lastReview = now.plusMinutes(-60 * 24))

            // when
            SchedulingCard(newCard, now)
            SchedulingCard(learningCard, now)

            // then
            assertEquals(now, newCard.lastReview)
            assertEquals(now, learningCard.lastReview)
        }

        @Test
        fun `should correctly set card's reps`() {
            // given
            val newCard = createCard()
            val learningCard =
                createCard().copy(state = State.Learning, lastReview = now.plusMinutes(-60 * 24), reps = 1)

            // when
            SchedulingCard(newCard, now)
            SchedulingCard(learningCard, now)

            // then
            assertEquals(1, newCard.reps)
            assertEquals(2, learningCard.reps)
        }

        @Test
        fun `should correctly set lastReview and lastElapsedDays`() {
            // given
            val lastReview = now.plusMinutes(-60 * 24 * 5)
            val learningCard =
                createCard().copy(
                    state = State.Learning,
                    lastReview = lastReview,
                    reps = 3,
                    elapsedDays = 5,
                )

            // when
            val schedulingCard = SchedulingCard(learningCard, now)

            // then
            assertEquals(lastReview, schedulingCard.lastReview)
            assertEquals(learningCard.elapsedDays, schedulingCard.lastElapsedDays)
        }

        @Test
        fun `should correctly set cards`() {
            // given
            val card = createCard()
            val toUpdateCard = card.copy()

            // when
            val schedulingCard = SchedulingCard(toUpdateCard, now)

            // then
            assertNotEquals(card, toUpdateCard)
            assertEquals(toUpdateCard, schedulingCard.again)
            assertEquals(toUpdateCard, schedulingCard.hard)
            assertEquals(toUpdateCard, schedulingCard.good)
            assertEquals(toUpdateCard, schedulingCard.easy)
        }
    }

    @Nested
    inner class UpdateState {
        @Test
        fun `updating to NEW state should correctly change all card's state`() {
            // given
            val schedulingCard = SchedulingCard(createCard(), now)

            // when
            schedulingCard.updateState(State.New)

            // then
            assertEquals(State.Learning, schedulingCard.again.state)
            assertEquals(State.Learning, schedulingCard.hard.state)
            assertEquals(State.Learning, schedulingCard.good.state)
            assertEquals(State.Review, schedulingCard.easy.state)
        }

        @Test
        fun `updating to LEARNING state should correctly change all card's state`() {
            // given
            val schedulingCard = SchedulingCard(createCard(), now)

            // when
            schedulingCard.updateState(State.Learning)

            // then
            assertEquals(State.Learning, schedulingCard.again.state)
            assertEquals(State.Learning, schedulingCard.hard.state)
            assertEquals(State.Review, schedulingCard.good.state)
            assertEquals(State.Review, schedulingCard.easy.state)
        }

        @Test
        fun `updating to RELEARNING state should correctly change all card's state`() {
            // given
            val schedulingCard = SchedulingCard(createCard(), now)

            // when
            schedulingCard.updateState(State.Relearning)

            // then
            assertEquals(State.Relearning, schedulingCard.again.state)
            assertEquals(State.Relearning, schedulingCard.hard.state)
            assertEquals(State.Review, schedulingCard.good.state)
            assertEquals(State.Review, schedulingCard.easy.state)
        }

        @Test
        fun `updating to REVIEW state should correctly change all card's state`() {
            // given
            val card = createCard()
            val schedulingCard = SchedulingCard(card, now)

            // when
            schedulingCard.updateState(State.Review)

            // then
            assertEquals(State.Relearning, schedulingCard.again.state)
            assertEquals(State.Review, schedulingCard.hard.state)
            assertEquals(State.Review, schedulingCard.good.state)
            assertEquals(State.Review, schedulingCard.easy.state)
            assertEquals(1, schedulingCard.again.lapses)
        }
    }

    @Nested
    inner class Schedule {
        @Test
        fun `should correctly set again information`() {
            // given
            val card = createCard().copy(scheduledDays = 1)
            val schedulingCard = SchedulingCard(card, now)

            // when
            schedulingCard.schedule(now, 1, 2, 3)

            // then
            assertEquals(0, schedulingCard.again.scheduledDays)
            assertEquals(now.plusMinutes(5), schedulingCard.again.due)
        }

        @Test
        fun `should correctly set hard information`() {
            // Scenario 1
            // given
            val card = createCard().copy(scheduledDays = 1)
            val schedulingCard = SchedulingCard(card, now)
            val hardInterval1 = 1

            // when
            schedulingCard.schedule(now, hardInterval1, 2, 3)

            // then
            assertEquals(hardInterval1, schedulingCard.hard.scheduledDays)
            assertEquals(now.plusDays(hardInterval1), schedulingCard.hard.due)

            // Scenario 2
            // given
            val card2 = createCard().copy(scheduledDays = 1)
            val schedulingCard2 = SchedulingCard(card2, now)
            val hardInterval2 = 0

            // when
            schedulingCard2.schedule(now, hardInterval2, 2, 3)

            // then
            assertEquals(hardInterval2, schedulingCard2.hard.scheduledDays)
            assertEquals(now.plusMinutes(10), schedulingCard2.hard.due)
        }

        @Test
        fun `should correctly set good information`() {
            // given
            val card = createCard().copy(scheduledDays = 1)
            val schedulingCard = SchedulingCard(card, now)
            val goodInterval = 2

            // when
            schedulingCard.schedule(now, 1, goodInterval, 3)

            // then
            assertEquals(goodInterval, schedulingCard.good.scheduledDays)
            assertEquals(now.plusDays(goodInterval), schedulingCard.good.due)
        }

        @Test
        fun `should correctly set easy information`() {
            // given
            val card = createCard().copy(scheduledDays = 1)
            val schedulingCard = SchedulingCard(card, now)
            val easyInterval = 3

            // when
            schedulingCard.schedule(now, 1, 2, easyInterval)

            // then
            assertEquals(easyInterval, schedulingCard.easy.scheduledDays)
            assertEquals(now.plusDays(easyInterval), schedulingCard.easy.due)
        }
    }

    @Nested
    inner class RecordLog {
        @Test
        fun `should create RecordLog with right grade`() {
            // given
            val card = createCard()
            val schedulingCard = SchedulingCard(card, now)

            // when
            val recordLog = schedulingCard.recordLog(card, now)

            // then
            assertEquals(4, recordLog.logs.size)
            assertEquals(setOf(Grade.Again, Grade.Hard, Grade.Good, Grade.Easy), recordLog.logs.keys.toSet())
            // check again
            assertEquals(schedulingCard.again, recordLog.logs[Grade.Again]?.card)
            assertEquals(Rating.Again, recordLog.logs[Grade.Again]?.log?.rating)
            assertEquals(State.New, recordLog.logs[Grade.Again]?.log?.state)
            assertEquals(now, recordLog.logs[Grade.Again]?.log?.due)
            assertEquals(0.0, recordLog.logs[Grade.Again]?.log?.stability)
            assertEquals(0.0, recordLog.logs[Grade.Again]?.log?.difficulty)
            assertEquals(0, recordLog.logs[Grade.Again]?.log?.elapsedDays)
            assertEquals(0, recordLog.logs[Grade.Again]?.log?.lastElapsedDays)
            assertEquals(0, recordLog.logs[Grade.Again]?.log?.scheduledDays)
            assertEquals(now, recordLog.logs[Grade.Again]?.log?.review)
            // check hard
            assertEquals(schedulingCard.hard, recordLog.logs[Grade.Hard]?.card)
            assertEquals(Rating.Hard, recordLog.logs[Grade.Hard]?.log?.rating)
            assertEquals(State.New, recordLog.logs[Grade.Hard]?.log?.state)
            assertEquals(now, recordLog.logs[Grade.Hard]?.log?.due)
            assertEquals(0.0, recordLog.logs[Grade.Hard]?.log?.stability)
            assertEquals(0.0, recordLog.logs[Grade.Hard]?.log?.difficulty)
            assertEquals(0, recordLog.logs[Grade.Hard]?.log?.elapsedDays)
            assertEquals(0, recordLog.logs[Grade.Hard]?.log?.lastElapsedDays)
            assertEquals(0, recordLog.logs[Grade.Hard]?.log?.scheduledDays)
            assertEquals(now, recordLog.logs[Grade.Hard]?.log?.review)
            // check good
            assertEquals(schedulingCard.good, recordLog.logs[Grade.Good]?.card)
            assertEquals(Rating.Good, recordLog.logs[Grade.Good]?.log?.rating)
            assertEquals(State.New, recordLog.logs[Grade.Good]?.log?.state)
            assertEquals(now, recordLog.logs[Grade.Good]?.log?.due)
            assertEquals(0.0, recordLog.logs[Grade.Good]?.log?.stability)
            assertEquals(0.0, recordLog.logs[Grade.Good]?.log?.difficulty)
            assertEquals(0, recordLog.logs[Grade.Good]?.log?.elapsedDays)
            assertEquals(0, recordLog.logs[Grade.Good]?.log?.lastElapsedDays)
            assertEquals(0, recordLog.logs[Grade.Good]?.log?.scheduledDays)
            assertEquals(now, recordLog.logs[Grade.Good]?.log?.review)
            // check easy
            assertEquals(schedulingCard.easy, recordLog.logs[Grade.Easy]?.card)
            assertEquals(Rating.Easy, recordLog.logs[Grade.Easy]?.log?.rating)
            assertEquals(State.New, recordLog.logs[Grade.Easy]?.log?.state)
            assertEquals(now, recordLog.logs[Grade.Easy]?.log?.due)
            assertEquals(0.0, recordLog.logs[Grade.Easy]?.log?.stability)
            assertEquals(0.0, recordLog.logs[Grade.Easy]?.log?.difficulty)
            assertEquals(0, recordLog.logs[Grade.Easy]?.log?.elapsedDays)
            assertEquals(0, recordLog.logs[Grade.Easy]?.log?.lastElapsedDays)
            assertEquals(0, recordLog.logs[Grade.Easy]?.log?.scheduledDays)
            assertEquals(now, recordLog.logs[Grade.Easy]?.log?.review)
        }
    }
}
