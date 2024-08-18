package com.inout.fsrs.model.algorithm

import com.inout.fsrs.base.getFuzzRange
import com.inout.fsrs.model.FSRSParameters
import com.inout.fsrs.model.enums.Grade
import com.inout.fsrs.schedule.SchedulingCard
import java.util.*
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

const val DECAY: Double = -0.5
const val FACTOR: Double = 19.0 / 81.0

open class FSRSAlgorithm(params: FSRSParameters) {
    private var param: FSRSParameters = params
    private val intervalModifier: Double

    init {
        intervalModifier = calculateIntervalModifier(param.requestRetention)
    }

    private fun calculateIntervalModifier(requestRetention: Double): Double {
        require(requestRetention > 0 && requestRetention <= 1) {
            "Requested retention rate should be in the range (0,1]"
        }

        return ((requestRetention.pow(1 / DECAY) - 1) / FACTOR)
    }

    var parameters: FSRSParameters
        get() = param
        set(value) { param = value }

    fun initDS(schedulingCard: SchedulingCard) {
        schedulingCard.again.difficulty = initDifficulty(Grade.Again)
        schedulingCard.again.stability = initStability(Grade.Again)
        schedulingCard.hard.difficulty = initDifficulty(Grade.Hard)
        schedulingCard.hard.stability = initStability(Grade.Hard)
        schedulingCard.good.difficulty = initDifficulty(Grade.Good)
        schedulingCard.good.stability = initStability(Grade.Good)
        schedulingCard.easy.difficulty = initDifficulty(Grade.Easy)
        schedulingCard.easy.stability = initStability(Grade.Easy)
    }

    fun nextDS(
        schedulingCard: SchedulingCard,
        lastDifficulty: Double,
        lastStability: Double,
        retrievability: Double
    ) {
        schedulingCard.again.difficulty = nextDifficulty(lastDifficulty, Grade.Again)
        schedulingCard.again.stability = nextForgetStability(lastDifficulty, lastStability, retrievability)
        schedulingCard.hard.difficulty = nextDifficulty(lastDifficulty, Grade.Hard)
        schedulingCard.hard.stability = nextRecallStability(lastDifficulty, lastStability, retrievability, Grade.Hard)
        schedulingCard.good.difficulty = nextDifficulty(lastDifficulty, Grade.Good)
        schedulingCard.good.stability = nextRecallStability(lastDifficulty, lastStability, retrievability, Grade.Good)
        schedulingCard.easy.difficulty = nextDifficulty(lastDifficulty, Grade.Easy)
        schedulingCard.easy.stability = nextRecallStability(lastDifficulty, lastStability, retrievability, Grade.Easy)
    }

    private fun initStability(grade: Grade): Double {
        return maxOf(param.weights[grade.value - 1], 0.1)
    }

    private fun initDifficulty(grade: Grade): Double {
        return minOf(
            maxOf(param.weights[4] - (grade.value - 3) * param.weights[5], 1.0),
            10.0
        )
    }

    private fun nextDifficulty(lastDifficulty: Double, grade: Grade): Double {
        val newDifficulty = lastDifficulty - param.weights[6] * (grade.value - 3)
        val meanReversion = param.weights[7] * param.weights[4] + (1 - param.weights[7]) * newDifficulty

        return minOf(maxOf(meanReversion, 1.0), 10.0)
    }

    private fun nextForgetStability(lastDifficulty: Double, lastStability: Double, retrievability: Double): Double {
        return param.weights[11] *
                (lastDifficulty.pow(-param.weights[12])) *
                ((lastStability + 1).pow(param.weights[13]) - 1) *
                exp((1 - retrievability) * param.weights[14])
    }

    private fun nextRecallStability(
        lastDifficulty: Double,
        lastStability: Double,
        retrievability: Double,
        grade: Grade
    ): Double {
        val hardPenalty = if (grade == Grade.Hard) param.weights[15] else 1.0
        val easyBound = if (grade == Grade.Easy) param.weights[16] else 1.0
        return lastStability * (1 +
                exp(param.weights[8]) *
                (11 - lastDifficulty) *
                (lastStability.pow(-param.weights[9])) *
                ((exp((1 - retrievability) * param.weights[10])) - 1) *
                hardPenalty * easyBound
                )
    }

    fun applyFuzz(interval: Double, elapsedDays: Int, enableFuzz: Boolean?): Int {
        if (enableFuzz != true || interval < 2.5) return interval.roundToInt()

        val fuzzFactor = Random().nextDouble()
        val (minInterval, maxInterval) = getFuzzRange(interval, elapsedDays, param.maximumInterval)
        return ((fuzzFactor * (maxInterval - minInterval + 1) + minInterval).toInt())
    }

    fun nextInterval(
        stability: Double,
        elapsedDays: Int,
        enableFuzz: Boolean = param.enableFuzz
    ): Int {
        val newInterval = minOf(maxOf(1, (stability * intervalModifier).roundToInt()), param.maximumInterval)
        return applyFuzz(newInterval.toDouble(), elapsedDays, enableFuzz)
    }

    fun forgettingCurve(elapsedDays: Int, stability: Double): Double {
        return (1 + (FACTOR * elapsedDays) / stability).pow(DECAY)
    }
}
