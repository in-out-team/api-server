package com.inout.fsrs

import com.inout.fsrs.base.addDays
import com.inout.fsrs.base.addMinutes
import com.inout.fsrs.base.diff
import com.inout.fsrs.model.*
import com.inout.fsrs.model.algorithm.FSRSAlgorithm
import com.inout.fsrs.model.enums.Rating
import com.inout.fsrs.model.enums.State
import com.inout.fsrs.schedule.SchedulingCard
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

class FSRS(param: FSRSParameters) : FSRSAlgorithm(param) {
    fun repeat(
        card: Card,
        now: Date,
        afterHandler: ((RecordLog) -> RecordLog)? = null
    ): RecordLog {
        val schedulingCard = SchedulingCard(card, now)
        val interval = card.elapsedDays
        when (card.state) {
            State.New -> {
                initDS(schedulingCard)
                schedulingCard.again.due = now.addMinutes(1)
                schedulingCard.hard.due = now.addMinutes(5)
                schedulingCard.good.due = now.addMinutes(10)

                val easyInterval = nextInterval(schedulingCard.easy.stability, interval)
                schedulingCard.easy.scheduledDays = easyInterval
                schedulingCard.easy.due = now.addDays(easyInterval)
            }
            State.Learning, State.Relearning -> {
                val hardInterval = 0
                val goodInterval = nextInterval(schedulingCard.good.stability, interval)
                val easyInterval = maxOf(nextInterval(schedulingCard.easy.stability, interval), goodInterval + 1)

                schedulingCard.schedule(now, hardInterval, goodInterval, easyInterval)
            }
            State.Review -> {
                val lastDifficulty = card.difficulty
                val lastStability = card.stability
                val retrievability = forgettingCurve(interval, lastStability)

                nextDS(schedulingCard, lastDifficulty, lastStability, retrievability)

                var hardInterval = nextInterval(schedulingCard.hard.stability, interval)
                var goodInterval = nextInterval(schedulingCard.good.stability, interval)
                hardInterval = minOf(hardInterval, goodInterval)
                goodInterval = maxOf(goodInterval, hardInterval + 1)
                val easyInterval = maxOf(nextInterval(schedulingCard.easy.stability, interval), goodInterval + 1)
                schedulingCard.schedule(now, hardInterval, goodInterval, easyInterval)
            }
        }

        val recordLog = schedulingCard.recordLog(card, now)
        return afterHandler?.invoke(recordLog) ?: recordLog
    }

    fun getRetrievability(card: Card, now: Date): Double? {
        if (card.state != State.Review || card.lastReview == null) {
            return null
        }

        val elapsedDays = maxOf(now.diff(card.lastReview!!), 0)
        return forgettingCurve(elapsedDays, round(card.stability))
    }

    fun rollback(
        card: Card,
        log: ReviewLog,
        afterHandler: ((Card) -> Card)? = null
    ): Card {
        if (log.rating == Rating.Manual) {
            throw IllegalArgumentException("Cannot rollback a manual rating")
        }

        val (lastDue, lastReview, lastLapses) = when (log.state) {
            State.New -> Triple(log.due, null, 0)
            State.Learning, State.Review, State.Relearning -> Triple(
                log.review,
                log.due,
                card.lapses - if (log.rating == Rating.Again && log.state == State.Review) 1 else 0
            )
        }

        val prevCard = card.copy(
            due = lastDue,
            stability = log.stability,
            difficulty = log.difficulty,
            elapsedDays = log.lastElapsedDays,
            scheduledDays = log.scheduledDays,
            reps = maxOf(card.reps - 1, 0),
            lapses = maxOf(lastLapses, 0),
            state = log.state,
            lastReview = lastReview,
        )
        return afterHandler?.invoke(prevCard) ?: prevCard
    }

    fun forget(
        card: Card,
        now: Date,
        resetCount: Boolean = false,
        afterHandler: ((RecordLogItem) -> RecordLogItem)? = null
    ) : RecordLogItem {
        val scheduledDays = if (card.state != State.New) 0 else now.diff(card.lastReview!!)
        val forgetLog = ReviewLog(
            rating = Rating.Manual,
            state = card.state,
            due = card.due,
            stability = card.stability,
            difficulty = card.difficulty,
            elapsedDays = 0,
            lastElapsedDays = card.elapsedDays,
            scheduledDays = scheduledDays,
            review = now,
        )
        val forgetCard = card.copy(
            due = now,
            stability = 0.0,
            difficulty = 0.0,
            elapsedDays = 0,
            scheduledDays = 0,
            reps = if (resetCount) 0 else card.reps,
            lapses = if (resetCount) 0 else card.lapses,
            state = State.New,
            lastReview = card.lastReview,
        )
        val recordLogItem = RecordLogItem(forgetCard, forgetLog)

        return afterHandler?.invoke(recordLogItem) ?: recordLogItem
    }

    fun reschedule(
        cards: List<Card>,
        options: RescheduleOptions = RescheduleOptions(),
    ): List<Card> {
        if (cards.isEmpty()) {
            throw IllegalArgumentException("cards must be an array")
        }
        val processedCards = mutableListOf<Card>()
        for (card in cards) {
            if (card.state != State.Review || card.lastReview == null) continue

            val scheduledDays = card.scheduledDays
            val nextInterval = nextInterval(
                card.stability.roundToInt().toDouble(),
                card.elapsedDays,
                options.enableFuzz ?: true
            )
            if (nextInterval == scheduledDays || nextInterval == 0) continue

            val processedCard = card.copy(scheduledDays = nextInterval)
            val newDue = card.lastReview!!.addDays(nextInterval)
            processedCard.due = options.dateHandler?.invoke(newDue) ?: newDue
            processedCards.add(processedCard)
        }

        return processedCards
    }
}
