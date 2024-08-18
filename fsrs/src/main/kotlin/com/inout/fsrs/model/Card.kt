package com.inout.fsrs.model

import com.inout.fsrs.model.enums.State
import java.util.*

data class Card(
    var state: State,
    var due: Date,
    var stability: Double,
    var difficulty: Double,
    var elapsedDays: Int,
    var scheduledDays: Int,
    var reps: Int,
    var lapses: Int,
    var lastReview: Date?
) {
    companion object {
        fun <T : Card> createEmptyCard(
            now: Date? = Date(),
            afterHandler: ((Card) -> T)? = null
        ): T {
            val card = Card(
                state = State.New,
                due = now ?: Date(),
                stability = 0.0,
                difficulty = 0.0,
                elapsedDays = 0,
                scheduledDays = 0,
                reps = 0,
                lapses = 0,
                lastReview = null,
            )
            return afterHandler?.invoke(card) ?: card as T
        }
    }
}
