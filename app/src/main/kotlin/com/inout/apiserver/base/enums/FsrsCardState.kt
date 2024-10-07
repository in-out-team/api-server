package com.inout.apiserver.base.enums

import com.inout.fsrs.model.enums.State

enum class FsrsCardState(val value: Int) {
    NEW(0),
    LEARNING(1),
    REVIEW(2),
    RELEARNING(3),
    ;

    companion object {
        fun of(state: State): FsrsCardState =
            when (state) {
                State.New -> NEW
                State.Learning -> LEARNING
                State.Review -> REVIEW
                State.Relearning -> RELEARNING
            }

        fun toFsrsState(fsrsCardState: FsrsCardState): State =
            when (fsrsCardState) {
                NEW -> State.New
                LEARNING -> State.Learning
                REVIEW -> State.Review
                RELEARNING -> State.Relearning
            }
    }
}
