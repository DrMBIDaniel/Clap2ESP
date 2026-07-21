package com.clap2esp.app

class AdaptiveThreshold {

    private var threshold = 5

    fun update(score: Int) {

        when {

            score >= 8 -> {
                threshold++
            }

            score <= 3 -> {
                threshold--
            }
        }

        threshold = threshold.coerceIn(4, 8)
    }

    fun currentThreshold(): Int {
        return threshold
    }
}
