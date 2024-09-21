package com.inout.apiserver.base.enums

import com.inout.fsrs.model.enums.Rating

enum class FsrsCardRating(val value: Int) {
    MANUAL(0),
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4),
    ;

    companion object {
        fun of(rating: Rating): FsrsCardRating =
            when (rating) {
                Rating.Manual -> MANUAL
                Rating.Again -> AGAIN
                Rating.Hard -> HARD
                Rating.Good -> GOOD
                Rating.Easy -> EASY
            }
    }
}
