package com.clap2esp.app

class DecisionSmoother {

    private val history = ArrayDeque<Boolean>()

    private val historySize = 4

    fun accept(candidate: Boolean): Boolean {

        history.addLast(candidate)

        if (history.size > historySize) {
            history.removeFirst()
        }

        var positives = 0

        for (v in history) {
            if (v) positives++
        }

        return positives >= 2
    }

    fun clear() {
        history.clear()
    }
}
