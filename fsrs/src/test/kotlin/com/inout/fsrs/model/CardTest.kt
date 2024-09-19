package com.inout.fsrs.model

import com.inout.fsrs.base.diffInMillis
import com.inout.fsrs.model.enums.State
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class CardTest {
    @Nested
    inner class CreateEmptyCard {
        @Test
        fun `should create an empty card with given date`() {
            // Given
            val now = Instant.now()

            // When
            val card = Card.createEmptyCard(now)

            // Then
            assertEquals(State.New, card.state)
            assertEquals(now, card.due)
            assertEquals(0.0, card.stability)
            assertEquals(0.0, card.difficulty)
            assertEquals(0, card.elapsedDays)
            assertEquals(0, card.scheduledDays)
            assertEquals(0, card.reps)
            assertEquals(0, card.lapses)
            assertNull(card.lastReview)
        }

        @Test
        fun `should create an empty card with current date if no date is given`() {
            // Given
            val now = Instant.now()

            // When
            val card = Card.createEmptyCard()

            // Then
            assertEquals(State.New, card.state)
            assertTrue(now.diffInMillis(card.due) < 10)
            assertEquals(0.0, card.stability)
            assertEquals(0.0, card.difficulty)
            assertEquals(0, card.elapsedDays)
            assertEquals(0, card.scheduledDays)
            assertEquals(0, card.reps)
            assertEquals(0, card.lapses)
            assertNull(card.lastReview)
        }
    }
}
