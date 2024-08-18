package com.inout.fsrs.schedule

import com.inout.fsrs.base.addDays
import com.inout.fsrs.base.addMinutes
import com.inout.fsrs.base.diff
import com.inout.fsrs.model.Card
import com.inout.fsrs.model.RecordLog
import com.inout.fsrs.model.RecordLogItem
import com.inout.fsrs.model.ReviewLog
import com.inout.fsrs.model.enums.Grade
import com.inout.fsrs.model.enums.Rating
import com.inout.fsrs.model.enums.State
import java.util.*

class SchedulingCard(card: Card, now: Date) {
    var again: Card = card.copy()
    var hard: Card = card.copy()
    var good: Card = card.copy()
    var easy: Card = card.copy()
    var lastReview: Date = card.lastReview ?: card.due
    var lastElapsedDays: Int = card.elapsedDays

    init {
        card.elapsedDays = if (card.state == State.New) 0 else now.diff(card.lastReview ?: Date())
        card.lastReview = now
        card.reps += 1
    }

    fun updateState(state: State): SchedulingCard {
        when (state) {
            State.New -> {
                again.state = State.Learning
                hard.state = State.Learning
                good.state = State.Learning
                easy.state = State.Review
            }
            State.Learning, State.Relearning -> {
                again.state = state
                hard.state = state
                good.state = State.Review
                easy.state = State.Review
            }
            State.Review -> {
                again.state = State.Relearning
                hard.state = State.Review
                good.state = State.Review
                easy.state = State.Review
                again.lapses += 1
            }
        }
        return this
    }

    fun schedule(now: Date, hardInterval: Int, goodInterval: Int, easyInterval: Int): SchedulingCard {
        again.scheduledDays = 0
        hard.scheduledDays = hardInterval
        good.scheduledDays = goodInterval
        easy.scheduledDays = easyInterval
        again.due = now.addMinutes(5)
        hard.due = if (hardInterval > 0) now.addDays(hardInterval) else now.addMinutes(10)
        good.due = now.addDays(goodInterval)
        easy.due = now.addDays(easyInterval)
        return this
    }

    fun recordLog(card: Card, now: Date): RecordLog {
        return RecordLog(
            logs = mapOf(
                Grade.Again to RecordLogItem(
                    card = again,
                    log = ReviewLog(
                        rating = Rating.Again,
                        state = card.state,
                        due = lastReview,
                        stability = card.stability,
                        difficulty = card.difficulty,
                        elapsedDays = card.elapsedDays,
                        lastElapsedDays = lastElapsedDays,
                        scheduledDays = card.scheduledDays,
                        review = now
                    )
                ),
                Grade.Hard to RecordLogItem(
                    card = hard,
                    log = ReviewLog(
                        rating = Rating.Hard,
                        state = card.state,
                        due = lastReview,
                        stability = card.stability,
                        difficulty = card.difficulty,
                        elapsedDays = card.elapsedDays,
                        lastElapsedDays = lastElapsedDays,
                        scheduledDays = card.scheduledDays,
                        review = now
                    )
                ),
                Grade.Good to RecordLogItem(
                    card = good,
                    log = ReviewLog(
                        rating = Rating.Good,
                        state = card.state,
                        due = lastReview,
                        stability = card.stability,
                        difficulty = card.difficulty,
                        elapsedDays = card.elapsedDays,
                        lastElapsedDays = lastElapsedDays,
                        scheduledDays = card.scheduledDays,
                        review = now
                    )
                ),
                Grade.Easy to RecordLogItem(
                    card = easy,
                    log = ReviewLog(
                        rating = Rating.Easy,
                        state = card.state,
                        due = lastReview,
                        stability = card.stability,
                        difficulty = card.difficulty,
                        elapsedDays = card.elapsedDays,
                        lastElapsedDays = lastElapsedDays,
                        scheduledDays = card.scheduledDays,
                        review = now
                    )
                )
            )
        )
    }
}
