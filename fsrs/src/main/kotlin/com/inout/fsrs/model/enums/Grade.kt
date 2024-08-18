package com.inout.fsrs.model.enums

sealed class Grade(val value: Int) {
    data object Again : Grade(1)
    data object Hard : Grade(2)
    data object Good : Grade(3)
    data object Easy : Grade(4)

    companion object {
        fun fromRating(rating: Rating): Grade {
            return when (rating) {
                Rating.Again -> Again
                Rating.Hard -> Hard
                Rating.Good -> Good
                Rating.Easy -> Easy
                else -> throw IllegalArgumentException("Invalid rating")
            }
        }
    }
}
