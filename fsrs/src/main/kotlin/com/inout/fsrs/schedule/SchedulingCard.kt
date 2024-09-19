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
    var again: Card
    var hard: Card
    var good: Card
    var easy: Card
    var lastReview: Date = card.lastReview ?: card.due
    var lastElapsedDays: Int = card.elapsedDays

    init {
        card.elapsedDays = if (card.state == State.New) 0 else now.diff(card.lastReview ?: Date())
        card.lastReview = now
        card.reps += 1
        again = card.copy()
        hard = card.copy()
        good = card.copy()
        easy = card.copy()
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
        val baseReviewLog =
            ReviewLog(
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
        return RecordLog(
            logs = mapOf(
                Grade.Again to RecordLogItem(
                    card = again,
                    log = baseReviewLog
                ),
                Grade.Hard to RecordLogItem(
                    card = hard,
                    log = baseReviewLog.copy(rating = Rating.Hard)
                ),
                Grade.Good to RecordLogItem(
                    card = good,
                    log = baseReviewLog.copy(rating = Rating.Good)
                ),
                Grade.Easy to RecordLogItem(
                    card = easy,
                    log = baseReviewLog.copy(rating = Rating.Easy)
                )
            )
        )
    }
}
