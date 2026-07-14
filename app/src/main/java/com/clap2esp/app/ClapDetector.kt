package com.clap2esp.app

class ClapDetector {

    private var waitingSecondClap = false

    private var firstClapTime = 0L

    private var pendingSingle = false

    private val minDoubleDelay = 80L

    private val maxDoubleDelay = 450L

    private val singleTimeout = 500L

    fun detect(features: SignalFeatures): ClapType {

        if (!looksLikeClap(features)) {
            return ClapType.NONE
        }

        val now = System.currentTimeMillis()

        if (!waitingSecondClap) {

            waitingSecondClap = true
            pendingSingle = true
            firstClapTime = now

            Logger.log(
                "Candidate clap  peak=${features.peak}  HF=${"%.2f".format(features.highFrequencyRatio)}"
            )

            return ClapType.NONE
        }

        val delay = now - firstClapTime

        if (delay in minDoubleDelay..maxDoubleDelay) {

            waitingSecondClap = false
            pendingSingle = false

            Logger.log("Double clap delay=${delay}ms")

            return ClapType.DOUBLE_CLAP
        }

        firstClapTime = now
        pendingSingle = true

        return ClapType.NONE
    }

    fun checkSingleClapTimeout(): ClapType {

        if (!waitingSecondClap)
            return ClapType.NONE

        val now = System.currentTimeMillis()

        if (pendingSingle &&
            now - firstClapTime >= singleTimeout
        ) {

            waitingSecondClap = false
            pendingSingle = false

            Logger.log("Single clap detected")

            return ClapType.SINGLE_CLAP
        }

        return ClapType.NONE
    }

    private fun looksLikeClap(
        f: SignalFeatures
    ): Boolean {

        if (f.peak < 9000)
            return false

        if (f.rms < 1800)
            return false

        if (f.highFrequencyRatio < 0.35)
            return false

        if (f.zeroCrossings < 20)
            return false

        if (f.attack > 120)
            return false

        if (f.decay > 400)
            return false

        return true
    }

}
